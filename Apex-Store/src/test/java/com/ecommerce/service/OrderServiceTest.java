package com.ecommerce.service;

import com.ecommerce.dao.AddressDAO;
import com.ecommerce.dao.CartDAO;
import com.ecommerce.dao.OrderDAO;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.model.Address;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest {

    static class StubAddressDAO extends AddressDAO {
        private final Address address;

        StubAddressDAO(Address address) {
            this.address = address;
        }

        @Override
        public Optional<Address> findByIdAndUserId(int addressId, int userId) {
            if (address != null && address.getId() == addressId && address.getUserId() == userId) {
                return Optional.of(address);
            }
            return Optional.empty();
        }

        @Override
        public Optional<Address> findById(int addressId) {
            if (address != null && address.getId() == addressId) {
                return Optional.of(address);
            }
            return Optional.empty();
        }
    }

    static class StubCartDAO extends CartDAO {
        private final List<CartItem> items = new ArrayList<>();

        void setItems(List<CartItem> list) {
            items.clear();
            items.addAll(list);
        }

        @Override
        public List<CartItem> getCartItems(int userId) {
            return new ArrayList<>(items);
        }
    }

    static class StubProductDAO extends ProductDAO {
        private Product product;

        void setProduct(Product product) {
            this.product = product;
        }

        @Override
        public Optional<Product> findById(int id) {
            if (product != null && product.getId() == id) {
                return Optional.of(product);
            }
            return Optional.empty();
        }
    }

    private StubAddressDAO addressDAO;
    private StubCartDAO cartDAO;
    private StubProductDAO productDAO;
    private OrderDAO orderDAO;
    private CartService cartService;
    private OrderService orderService;

    @BeforeEach
    public void setup() {
        Address addr = new Address();
        addr.setId(1);
        addr.setUserId(10);
        addr.setRecipientName("Test Customer");
        addr.setPhone("+1-555-0100");
        addr.setAddressLine("123 Test St");
        addr.setCity("Test City");
        addr.setState("CA");
        addr.setPostalCode("90001");
        addr.setCountry("USA");

        addressDAO = new StubAddressDAO(addr);
        cartDAO = new StubCartDAO();
        productDAO = new StubProductDAO();
        orderDAO = new OrderDAO();
        cartService = new CartService(cartDAO, productDAO, new com.ecommerce.dao.CouponDAO());
        orderService = new OrderService(orderDAO, cartDAO, productDAO, addressDAO, cartService);
    }

    @Test
    public void testPlaceOrderFailsWhenAddressInvalid() {
        var res = orderService.placeOrder(10, 999, null, "COD");
        assertFalse(res.success());
        assertTrue(res.message().contains("Invalid shipping address"));
    }

    @Test
    public void testPlaceOrderFailsWhenCartIsEmpty() {
        // Address 1 exists, but cart is empty
        var res = orderService.placeOrder(10, 1, null, "COD");
        assertFalse(res.success());
        assertTrue(res.message().contains("cart is empty"));
    }

    @Test
    public void testPlaceOrderFailsWhenProductOutOfStock() {
        Product p = new Product();
        p.setId(101);
        p.setName("Out of Stock Phone");
        p.setPrice(new BigDecimal("500.00"));
        p.setStockQuantity(2); // Only 2 in stock
        p.setActive(true);
        productDAO.setProduct(p);

        CartItem item = new CartItem();
        item.setId(1);
        item.setUserId(10);
        item.setProductId(101);
        item.setQuantity(5); // Customer wants 5!
        item.setProduct(p);
        cartDAO.setItems(List.of(item));

        var res = orderService.placeOrder(10, 1, null, "COD");
        assertFalse(res.success());
        assertTrue(res.message().contains("Insufficient stock"));
    }

    @Test
    public void testPlaceDirectOrderWithCod() {
        Product p = new Product();
        p.setId(201);
        p.setName("Wireless Noise-Canceling Headphones");
        p.setPrice(new BigDecimal("1500.00"));
        p.setStockQuantity(10);
        p.setActive(true);
        productDAO.setProduct(p);

        var res = orderService.placeDirectOrder(10, 1, 201, 1, null, "COD");
        assertTrue(res.success());
        assertNotNull(res.order());
        assertEquals("COD", res.order().getPaymentMethod());
        assertEquals(com.ecommerce.model.PaymentStatus.PENDING, res.order().getPaymentStatus());
        assertEquals(new BigDecimal("1500.00"), res.order().getSubtotal());
        assertEquals(BigDecimal.ZERO, res.order().getShippingFee()); // Free shipping over ₹999
        assertEquals(new BigDecimal("1500.00"), res.order().getTotalAmount());
    }

    @Test
    public void testPlaceDirectOrderWithOnline() {
        Product p = new Product();
        p.setId(202);
        p.setName("Mechanical Gaming Keyboard");
        p.setPrice(new BigDecimal("499.00"));
        p.setStockQuantity(15);
        p.setActive(true);
        productDAO.setProduct(p);

        var res = orderService.placeDirectOrder(10, 1, 202, 1, null, "ONLINE_UPI");
        assertTrue(res.success());
        assertNotNull(res.order());
        assertEquals("ONLINE_UPI", res.order().getPaymentMethod());
        assertEquals(com.ecommerce.model.PaymentStatus.PAID, res.order().getPaymentStatus());
        assertEquals(com.ecommerce.model.OrderStatus.PROCESSING, res.order().getOrderStatus());
        assertEquals(new BigDecimal("499.00"), res.order().getSubtotal());
        assertEquals(new BigDecimal("99.00"), res.order().getShippingFee()); // Shipping fee applies under ₹999
        assertEquals(new BigDecimal("598.00"), res.order().getTotalAmount());
    }

    @Test
    public void testCancelOrderSuccess() {
        Product p = new Product();
        p.setId(203);
        p.setName("Ultra-slim Laptop");
        p.setPrice(new BigDecimal("899.00"));
        p.setStockQuantity(5);
        p.setActive(true);
        productDAO.setProduct(p);

        var placeRes = orderService.placeDirectOrder(10, 1, 203, 1, null, "ONLINE_CARD");
        assertTrue(placeRes.success());
        int orderId = placeRes.order().getId();

        var cancelRes = orderService.cancelOrder(orderId, 10, false);
        assertTrue(cancelRes.success());
        assertEquals(com.ecommerce.model.OrderStatus.CANCELLED, cancelRes.order().getOrderStatus());
        assertEquals(com.ecommerce.model.PaymentStatus.REFUNDED, cancelRes.order().getPaymentStatus());
    }

    @Test
    public void testCancelOrderUnauthorized() {
        Product p = new Product();
        p.setId(204);
        p.setName("Wireless Mouse");
        p.setPrice(new BigDecimal("29.99"));
        p.setStockQuantity(20);
        p.setActive(true);
        productDAO.setProduct(p);

        var placeRes = orderService.placeDirectOrder(10, 1, 204, 1, null, "COD");
        assertTrue(placeRes.success());
        int orderId = placeRes.order().getId();

        // User 99 is not owner and not admin
        var cancelRes = orderService.cancelOrder(orderId, 99, false);
        assertFalse(cancelRes.success());
    }
}
