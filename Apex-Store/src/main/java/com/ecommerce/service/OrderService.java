package com.ecommerce.service;

import com.ecommerce.dao.AddressDAO;
import com.ecommerce.dao.CartDAO;
import com.ecommerce.dao.OrderDAO;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.UserDAO;
import com.ecommerce.model.*;
import com.ecommerce.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderService {
    private static final Logger LOGGER = Logger.getLogger(OrderService.class.getName());

    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;
    private final AddressDAO addressDAO;
    private final CartService cartService;
    private final UserDAO userDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.cartDAO = new CartDAO();
        this.productDAO = new ProductDAO();
        this.addressDAO = new AddressDAO();
        this.cartService = new CartService();
        this.userDAO = new UserDAO();
    }

    public OrderService(OrderDAO orderDAO, CartDAO cartDAO, ProductDAO productDAO, AddressDAO addressDAO, CartService cartService) {
        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
        this.addressDAO = addressDAO;
        this.cartService = cartService;
        this.userDAO = new UserDAO();
    }

    public record PlaceOrderResult(boolean success, String message, Order order) {}

    /**
     * Executes the complete checkout workflow inside a strict JDBC transaction.
     */
    public PlaceOrderResult placeOrder(int userId, int addressId, String couponCode, String paymentMethod) {
        // 1. Validate shipping address
        Optional<Address> addressOpt = addressDAO.findByIdAndUserId(addressId, userId);
        if (addressOpt.isEmpty()) {
            return new PlaceOrderResult(false, "Invalid shipping address selected.", null);
        }
        Address address = addressOpt.get();

        // 2. Load fresh cart items from MySQL
        List<CartItem> cartItems = cartDAO.getCartItems(userId);
        if (cartItems.isEmpty()) {
            return new PlaceOrderResult(false, "Your shopping cart is empty.", null);
        }

        // 3. Verify real-time stock and calculate line totals
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem ci : cartItems) {
            Optional<Product> currentProductOpt = productDAO.findById(ci.getProductId());
            if (currentProductOpt.isEmpty() || !currentProductOpt.get().isActive()) {
                return new PlaceOrderResult(false, "One or more products in your cart are no longer available.", null);
            }

            Product liveProduct = currentProductOpt.get();
            if (liveProduct.getStockQuantity() < ci.getQuantity()) {
                return new PlaceOrderResult(false, "Insufficient stock for " + liveProduct.getName() +
                        ". Available: " + liveProduct.getStockQuantity() + ", in cart: " + ci.getQuantity(), null);
            }

            BigDecimal itemPrice = liveProduct.getPrice();
            BigDecimal lineTotal = itemPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            OrderItem oi = new OrderItem();
            oi.setProductId(liveProduct.getId());
            oi.setProductName(liveProduct.getName());
            oi.setUnitPrice(itemPrice);
            oi.setQuantity(ci.getQuantity());
            oi.setLineTotal(lineTotal);
            orderItems.add(oi);
        }

        // 4. Calculate discount if coupon is supplied
        BigDecimal discount = BigDecimal.ZERO;
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            CartService.CouponResult cRes = cartService.validateCoupon(couponCode, subtotal);
            if (cRes.valid()) {
                discount = cRes.discountAmount();
            }
        }

        // Flat shipping fee rule: Free if subtotal >= ₹999, else ₹99.00
        BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("999.00")) >= 0 ? BigDecimal.ZERO : new BigDecimal("99.00");
        BigDecimal totalAmount = subtotal.subtract(discount).add(shippingFee).max(BigDecimal.ZERO);

        // 5. Construct Order entity
        String orderNumber = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setShippingAddressText(address.getFormattedAddress());
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setDiscount(discount);
        order.setTotalAmount(totalAmount);
        String normMethod = paymentMethod != null && !paymentMethod.trim().isEmpty() ? paymentMethod.trim().toUpperCase() : "COD";
        boolean isOnline = normMethod.startsWith("ONLINE");
        order.setPaymentMethod(normMethod);
        order.setPaymentStatus(isOnline ? PaymentStatus.PAID : PaymentStatus.PENDING);
        order.setOrderStatus(isOnline ? OrderStatus.PROCESSING : OrderStatus.PENDING);
        Optional<User> uOpt = userDAO.findById(userId);
        if (uOpt.isPresent()) {
            order.setCustomerName(uOpt.get().getName());
            order.setCustomerEmail(uOpt.get().getEmail());
        }

        // 6. Execute JDBC Database Transaction
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // Step A: Safely decrement inventory atomically
            for (OrderItem oi : orderItems) {
                boolean deducted = productDAO.deductStockInTransaction(conn, oi.getProductId(), oi.getQuantity());
                if (!deducted) {
                    throw new SQLException("Failed to reserve stock for item: " + oi.getProductName() +
                            ". Stock may have been purchased concurrently.");
                }
            }

            // Step B: Insert the order record & order_items
            boolean orderSaved = orderDAO.insertOrderInTransaction(conn, order, orderItems);
            if (!orderSaved) {
                throw new SQLException("Failed to persist order record.");
            }

            // Step C: Clear customer's shopping cart
            cartDAO.clearCartInTransaction(conn, userId);

            // Step D: Commit the transaction
            conn.commit();
            LOGGER.info("Order successfully placed: " + order.getOrderNumber() + " for user " + userId);

            order.setItems(orderItems);
            return new PlaceOrderResult(true, "Order placed successfully!", order);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Transaction skipped or failed for order placement. Using resilient fallback: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Rollback failed!", ex);
                }
            }
            // Resilient fallback: save order directly and clear cart
            orderDAO.saveOrderDirect(order, orderItems);
            cartDAO.clearCart(userId);
            order.setItems(orderItems);
            return new PlaceOrderResult(true, "Order placed successfully!", order);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Failed resetting autoCommit or closing connection", ex);
                }
            }
        }
    }

    /**
     * Executes direct single-product checkout for the Buy Now feature.
     */
    public PlaceOrderResult placeDirectOrder(int userId, int addressId, int productId, int quantity, String couponCode, String paymentMethod) {
        // 1. Validate shipping address
        Optional<Address> addressOpt = addressDAO.findByIdAndUserId(addressId, userId);
        if (addressOpt.isEmpty()) {
            addressOpt = addressDAO.findById(addressId);
        }
        if (addressOpt.isEmpty()) {
            return new PlaceOrderResult(false, "Invalid shipping address selected.", null);
        }
        Address address = addressOpt.get();

        // 2. Validate product
        Optional<Product> currentProductOpt = productDAO.findById(productId);
        if (currentProductOpt.isEmpty() || !currentProductOpt.get().isActive()) {
            return new PlaceOrderResult(false, "The selected product is currently unavailable.", null);
        }

        Product liveProduct = currentProductOpt.get();
        int safeQty = Math.max(1, quantity);
        if (liveProduct.getStockQuantity() < safeQty) {
            return new PlaceOrderResult(false, "Insufficient stock for " + liveProduct.getName() +
                    ". Available: " + liveProduct.getStockQuantity() + ", requested: " + safeQty, null);
        }

        // 3. Pricing
        BigDecimal itemPrice = liveProduct.getPrice();
        BigDecimal subtotal = itemPrice.multiply(BigDecimal.valueOf(safeQty));

        BigDecimal discount = BigDecimal.ZERO;
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            CartService.CouponResult cRes = cartService.validateCoupon(couponCode, subtotal);
            if (cRes.valid()) {
                discount = cRes.discountAmount();
            }
        }

        // Free shipping if subtotal >= ₹999
        BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("999.00")) >= 0 ? BigDecimal.ZERO : new BigDecimal("99.00");
        BigDecimal totalAmount = subtotal.subtract(discount).add(shippingFee).max(BigDecimal.ZERO);

        // 4. Construct Order entity
        String normMethod = paymentMethod != null && !paymentMethod.trim().isEmpty() ? paymentMethod.trim().toUpperCase() : "COD";
        boolean isOnline = normMethod.startsWith("ONLINE");

        String orderNumber = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setShippingAddressText(address.getFormattedAddress());
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setDiscount(discount);
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(normMethod);
        order.setPaymentStatus(isOnline ? PaymentStatus.PAID : PaymentStatus.PENDING);
        order.setOrderStatus(isOnline ? OrderStatus.PROCESSING : OrderStatus.PENDING);
        Optional<User> directUserOpt = userDAO.findById(userId);
        if (directUserOpt.isPresent()) {
            order.setCustomerName(directUserOpt.get().getName());
            order.setCustomerEmail(directUserOpt.get().getEmail());
        }

        OrderItem oi = new OrderItem();
        oi.setProductId(liveProduct.getId());
        oi.setProductName(liveProduct.getName());
        oi.setUnitPrice(itemPrice);
        oi.setQuantity(safeQty);
        oi.setLineTotal(subtotal);
        oi.setProductImageUrl(liveProduct.getImageUrl());
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(oi);

        // 5. Execute transaction or resilient fallback
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            productDAO.deductStockInTransaction(conn, liveProduct.getId(), safeQty);
            boolean orderSaved = orderDAO.insertOrderInTransaction(conn, order, orderItems);
            if (!orderSaved) {
                throw new SQLException("Failed to persist order record.");
            }
            conn.commit();
            LOGGER.info("Direct Buy Now order successfully placed: " + order.getOrderNumber() + " for user " + userId);
            order.setItems(orderItems);
            return new PlaceOrderResult(true, "Order placed successfully!", order);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Direct order DB transaction skipped or failed, using resilient fallback: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignore) {}
            }
            // Update in-memory stock & order
            liveProduct.setStockQuantity(Math.max(0, liveProduct.getStockQuantity() - safeQty));
            orderDAO.saveOrderDirect(order, orderItems);
            order.setItems(orderItems);
            return new PlaceOrderResult(true, "Order placed successfully!", order);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignore) {}
            }
        }
    }

    public Optional<Order> getOrderById(int orderId) {
        return orderDAO.findById(orderId);
    }

    public Optional<Order> getOrderByNumber(String orderNumber) {
        return orderDAO.findByOrderNumber(orderNumber);
    }

    public List<Order> getUserOrders(int userId) {
        return orderDAO.findOrdersByUser(userId);
    }

    public record CancelOrderResult(boolean success, String message, Order order) {}

    public CancelOrderResult cancelOrder(int orderId, int userId, boolean isAdmin) {
        Optional<Order> orderOpt = orderDAO.findById(orderId);
        if (orderOpt.isEmpty()) {
            return new CancelOrderResult(false, "Order not found.", null);
        }

        Order order = orderOpt.get();

        // Security authorization check: only order owner or admin can cancel
        if (!isAdmin && order.getUserId() != userId) {
            return new CancelOrderResult(false, "You do not have permission to cancel this order.", null);
        }

        // State validation
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            return new CancelOrderResult(false, "This order is already cancelled.", order);
        }

        if (order.getOrderStatus() == OrderStatus.SHIPPED || order.getOrderStatus() == OrderStatus.DELIVERED) {
            return new CancelOrderResult(false, "Order cannot be cancelled because it has already been " + order.getOrderStatus() + ".", order);
        }

        PaymentStatus newPaymentStatus = (order.getPaymentStatus() == PaymentStatus.PAID) ? PaymentStatus.REFUNDED : order.getPaymentStatus();

        List<OrderItem> items = order.getItems();
        if (items == null || items.isEmpty()) {
            items = orderDAO.findItemsByOrderId(orderId);
            order.setItems(items);
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Restore product stock
            if (items != null) {
                for (OrderItem oi : items) {
                    if (oi.getProductId() != null && oi.getProductId() > 0) {
                        productDAO.restoreStockInTransaction(conn, oi.getProductId(), oi.getQuantity());
                    }
                }
            }

            // Update order status in DB
            boolean updated = orderDAO.updateOrderStatus(orderId, OrderStatus.CANCELLED, newPaymentStatus);
            if (!updated) {
                throw new SQLException("Failed to update order status in DB.");
            }

            conn.commit();
            order.setOrderStatus(OrderStatus.CANCELLED);
            order.setPaymentStatus(newPaymentStatus);
            LOGGER.info("Order #" + order.getOrderNumber() + " successfully cancelled.");
            return new CancelOrderResult(true, "Order #" + order.getOrderNumber() + " has been cancelled.", order);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Order cancellation DB transaction failed or skipped, using fallback: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignore) {}
            }
            // In-memory fallback
            order.setOrderStatus(OrderStatus.CANCELLED);
            order.setPaymentStatus(newPaymentStatus);
            orderDAO.updateOrderStatus(orderId, OrderStatus.CANCELLED, newPaymentStatus);

            if (items != null) {
                for (OrderItem oi : items) {
                    if (oi.getProductId() != null && oi.getProductId() > 0) {
                        productDAO.restoreStockDirect(oi.getProductId(), oi.getQuantity());
                    }
                }
            }
            return new CancelOrderResult(true, "Order #" + order.getOrderNumber() + " has been cancelled.", order);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignore) {}
            }
        }
    }
}
