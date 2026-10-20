package com.ecommerce.dao;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.PaymentStatus;
import com.ecommerce.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import com.ecommerce.util.LocalStorageManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderDAO {
    private static final Logger LOGGER = Logger.getLogger(OrderDAO.class.getName());

    private static final ConcurrentHashMap<Integer, Order> fallbackOrdersById = LocalStorageManager.loadOrders();
    private static final ConcurrentHashMap<String, Order> fallbackOrdersByNumber = new ConcurrentHashMap<>();
    private static final AtomicInteger fallbackOrderIdCounter;
    private static final AtomicInteger fallbackOrderItemIdCounter;

    static {
        int maxOrderId = 500;
        int maxItemId = 1000;
        for (Order o : fallbackOrdersById.values()) {
            if (o.getOrderNumber() != null) {
                fallbackOrdersByNumber.put(o.getOrderNumber(), o);
            }
            if (o.getId() > maxOrderId) {
                maxOrderId = o.getId();
            }
            if (o.getItems() != null) {
                for (OrderItem oi : o.getItems()) {
                    if (oi.getId() > maxItemId) {
                        maxItemId = oi.getId();
                    }
                }
            }
        }
        fallbackOrderIdCounter = new AtomicInteger(maxOrderId + 1);
        fallbackOrderItemIdCounter = new AtomicInteger(maxItemId + 1);
    }

    public boolean saveOrderDirect(Order order, List<OrderItem> items) {
        if (order.getId() <= 0) {
            order.setId(fallbackOrderIdCounter.incrementAndGet());
        }
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }
        for (OrderItem oi : items) {
            if (oi.getId() <= 0) {
                oi.setId(fallbackOrderItemIdCounter.incrementAndGet());
            }
            oi.setOrderId(order.getId());
        }
        order.setItems(new ArrayList<>(items));
        fallbackOrdersById.put(order.getId(), order);
        fallbackOrdersByNumber.put(order.getOrderNumber(), order);
        LocalStorageManager.saveOrders(fallbackOrdersById);
        return true;
    }

    /**
     * Inserts an order and its line items inside an existing JDBC transaction.
     */
    public boolean insertOrderInTransaction(Connection conn, Order order, List<OrderItem> items) throws SQLException {
        String orderSql = "INSERT INTO orders (order_number, user_id, address_id, shipping_address_text, subtotal, " +
                          "shipping_fee, discount, total_amount, payment_method, payment_status, order_status) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement psOrder = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
            psOrder.setString(1, order.getOrderNumber());
            psOrder.setInt(2, order.getUserId());
            if (order.getAddressId() != null) {
                psOrder.setInt(3, order.getAddressId());
            } else {
                psOrder.setNull(3, Types.INTEGER);
            }
            psOrder.setString(4, order.getShippingAddressText());
            psOrder.setBigDecimal(5, order.getSubtotal());
            psOrder.setBigDecimal(6, order.getShippingFee());
            psOrder.setBigDecimal(7, order.getDiscount());
            psOrder.setBigDecimal(8, order.getTotalAmount());
            psOrder.setString(9, order.getPaymentMethod());
            psOrder.setString(10, order.getPaymentStatus().name());
            psOrder.setString(11, order.getOrderStatus().name());

            int affected = psOrder.executeUpdate();
            if (affected == 0) {
                return false;
            }

            try (ResultSet generatedKeys = psOrder.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Failed to retrieve generated order id.");
                }
            }
        }

        // Insert order items
        String itemSql = "INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement psItem = conn.prepareStatement(itemSql)) {
            for (OrderItem item : items) {
                psItem.setInt(1, order.getId());
                if (item.getProductId() != null) {
                    psItem.setInt(2, item.getProductId());
                } else {
                    psItem.setNull(2, Types.INTEGER);
                }
                psItem.setString(3, item.getProductName());
                psItem.setBigDecimal(4, item.getUnitPrice());
                psItem.setInt(5, item.getQuantity());
                psItem.setBigDecimal(6, item.getLineTotal());
                psItem.addBatch();
            }
            int[] results = psItem.executeBatch();
            for (int r : results) {
                if (r == Statement.EXECUTE_FAILED) {
                    throw new SQLException("Failed to insert one of the order items.");
                }
            }
        }

        // Keep cached in fallback map as well
        order.setItems(new ArrayList<>(items));
        fallbackOrdersById.put(order.getId(), order);
        fallbackOrdersByNumber.put(order.getOrderNumber(), order);
        LocalStorageManager.saveOrders(fallbackOrdersById);

        return true;
    }

    public Optional<Order> findById(int orderId) {
        String sql = "SELECT o.id, o.order_number, o.user_id, o.address_id, o.shipping_address_text, " +
                     "o.subtotal, o.shipping_fee, o.discount, o.total_amount, o.payment_method, " +
                     "o.payment_status, o.order_status, o.created_at, " +
                     "u.name AS customer_name, u.email AS customer_email " +
                     "FROM orders o " +
                     "JOIN users u ON o.user_id = u.id " +
                     "WHERE o.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(findItemsByOrderId(order.getId()));
                    return Optional.of(order);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding order by id: " + orderId, e);
        }
        return Optional.ofNullable(fallbackOrdersById.get(orderId));
    }

    public Optional<Order> findByOrderNumber(String orderNumber) {
        String sql = "SELECT o.id, o.order_number, o.user_id, o.address_id, o.shipping_address_text, " +
                     "o.subtotal, o.shipping_fee, o.discount, o.total_amount, o.payment_method, " +
                     "o.payment_status, o.order_status, o.created_at, " +
                     "u.name AS customer_name, u.email AS customer_email " +
                     "FROM orders o " +
                     "JOIN users u ON o.user_id = u.id " +
                     "WHERE o.order_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(findItemsByOrderId(order.getId()));
                    return Optional.of(order);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding order by number: " + orderNumber, e);
        }
        return Optional.ofNullable(fallbackOrdersByNumber.get(orderNumber));
    }

    public List<Order> findOrdersByUser(int userId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.id, o.order_number, o.user_id, o.address_id, o.shipping_address_text, " +
                     "o.subtotal, o.shipping_fee, o.discount, o.total_amount, o.payment_method, " +
                     "o.payment_status, o.order_status, o.created_at, " +
                     "u.name AS customer_name, u.email AS customer_email " +
                     "FROM orders o " +
                     "JOIN users u ON o.user_id = u.id " +
                     "WHERE o.user_id = ? " +
                     "ORDER BY o.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = mapResultSetToOrder(rs);
                    o.setItems(findItemsByOrderId(o.getId()));
                    list.add(o);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching orders for user: " + userId, e);
        }
        if (list.isEmpty()) {
            UserDAO userDAO = new UserDAO();
            Optional<com.ecommerce.model.User> uOpt = userDAO.findById(userId);
            String userEmail = uOpt.map(com.ecommerce.model.User::getEmail).orElse(null);

            for (Order o : fallbackOrdersById.values()) {
                if (o.getUserId() == userId ||
                    (userEmail != null && o.getCustomerEmail() != null && o.getCustomerEmail().trim().equalsIgnoreCase(userEmail.trim()))) {
                    list.add(o);
                }
            }
        }
        return list;
    }

    public void linkOrdersToUser(String email, int userId) {
        if (email == null || email.trim().isEmpty() || userId <= 0) {
            return;
        }
        String cleanEmail = email.trim().toLowerCase();
        boolean changed = false;
        for (Order o : fallbackOrdersById.values()) {
            if (o.getCustomerEmail() != null && o.getCustomerEmail().trim().equalsIgnoreCase(cleanEmail)) {
                if (o.getUserId() != userId) {
                    o.setUserId(userId);
                    changed = true;
                }
            }
        }
        if (changed) {
            LocalStorageManager.saveOrders(fallbackOrdersById);
        }
        String sql = "UPDATE orders SET user_id = ? WHERE LOWER(customer_email) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, cleanEmail);
            ps.executeUpdate();
        } catch (SQLException ignore) {}
    }

    public List<Order> findAllOrders(int offset, int limit) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.id, o.order_number, o.user_id, o.address_id, o.shipping_address_text, " +
                     "o.subtotal, o.shipping_fee, o.discount, o.total_amount, o.payment_method, " +
                     "o.payment_status, o.order_status, o.created_at, " +
                     "u.name AS customer_name, u.email AS customer_email " +
                     "FROM orders o " +
                     "JOIN users u ON o.user_id = u.id " +
                     "ORDER BY o.created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = mapResultSetToOrder(rs);
                    o.setItems(findItemsByOrderId(o.getId()));
                    list.add(o);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all orders", e);
        }
        return list;
    }

    public List<OrderItem> findItemsByOrderId(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.id, oi.order_id, oi.product_id, oi.product_name, oi.unit_price, oi.quantity, oi.line_total, " +
                     "p.image_url AS product_image_url " +
                     "FROM order_items oi " +
                     "LEFT JOIN products p ON oi.product_id = p.id " +
                     "WHERE oi.order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    int pId = rs.getInt("product_id");
                    if (!rs.wasNull()) {
                        item.setProductId(pId);
                    }
                    item.setProductName(rs.getString("product_name"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setLineTotal(rs.getBigDecimal("line_total"));
                    item.setProductImageUrl(rs.getString("product_image_url"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching items for order id: " + orderId, e);
        }
        if (items.isEmpty()) {
            Order o = fallbackOrdersById.get(orderId);
            if (o != null && o.getItems() != null && !o.getItems().isEmpty()) {
                return new ArrayList<>(o.getItems());
            }
        }
        return items;
    }

    public boolean updateOrderStatus(int orderId, OrderStatus orderStatus, PaymentStatus paymentStatus) {
        Order fo = fallbackOrdersById.get(orderId);
        if (fo != null) {
            fo.setOrderStatus(orderStatus);
            fo.setPaymentStatus(paymentStatus);
            LocalStorageManager.saveOrders(fallbackOrdersById);
        }
        String sql = "UPDATE orders SET order_status = ?, payment_status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderStatus.name());
            ps.setString(2, paymentStatus.name());
            ps.setInt(3, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating status for order id: " + orderId, e);
            return fo != null;
        }
    }

    public int countAllOrders() {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting total orders", e);
        }
        return 0;
    }

    public BigDecimal calculateTotalRevenue() {
        // Revenue from orders that are not CANCELLED
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE order_status != 'CANCELLED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating total revenue", e);
        }
        return BigDecimal.ZERO;
    }

    public List<Order> getRecentOrders(int limit) {
        return findAllOrders(0, limit);
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setOrderNumber(rs.getString("order_number"));
        o.setUserId(rs.getInt("user_id"));
        int addrId = rs.getInt("address_id");
        if (!rs.wasNull()) {
            o.setAddressId(addrId);
        }
        o.setShippingAddressText(rs.getString("shipping_address_text"));
        o.setSubtotal(rs.getBigDecimal("subtotal"));
        o.setShippingFee(rs.getBigDecimal("shipping_fee"));
        o.setDiscount(rs.getBigDecimal("discount"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        o.setOrderStatus(OrderStatus.valueOf(rs.getString("order_status")));
        o.setCreatedAt(rs.getTimestamp("created_at"));

        o.setCustomerName(rs.getString("customer_name"));
        o.setCustomerEmail(rs.getString("customer_email"));
        return o;
    }
}
