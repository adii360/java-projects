package com.ecommerce.service;

import com.ecommerce.dao.CartDAO;
import com.ecommerce.dao.CouponDAO;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Coupon;
import com.ecommerce.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CartService {
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;
    private final CouponDAO couponDAO;

    public CartService() {
        this.cartDAO = new CartDAO();
        this.productDAO = new ProductDAO();
        this.couponDAO = new CouponDAO();
    }

    public CartService(CartDAO cartDAO, ProductDAO productDAO, CouponDAO couponDAO) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
        this.couponDAO = couponDAO;
    }

    public List<CartItem> getCart(int userId) {
        return cartDAO.getCartItems(userId);
    }

    public record CartOpResult(boolean success, String message) {}

    public CartOpResult addToCart(int userId, int productId, int quantity) {
        if (quantity <= 0) {
            return new CartOpResult(false, "Quantity must be at least 1.");
        }

        Optional<Product> prodOpt = productDAO.findById(productId);
        if (prodOpt.isEmpty()) {
            return new CartOpResult(false, "Product not found.");
        }

        Product p = prodOpt.get();
        if (!p.isActive()) {
            return new CartOpResult(false, "This product is currently unavailable.");
        }

        if (p.getStockQuantity() < quantity) {
            return new CartOpResult(false, "Insufficient stock. Only " + p.getStockQuantity() + " available.");
        }

        boolean added = cartDAO.addItem(userId, productId, quantity);
        if (added) {
            return new CartOpResult(true, "Product added to cart successfully.");
        } else {
            return new CartOpResult(false, "Failed to add product to cart.");
        }
    }

    public CartOpResult updateQuantity(int userId, int cartItemId, int quantity) {
        if (quantity <= 0) {
            boolean removed = cartDAO.removeItem(userId, cartItemId);
            return new CartOpResult(removed, removed ? "Item removed from cart." : "Failed to remove item.");
        }

        boolean updated = cartDAO.updateQuantity(userId, cartItemId, quantity);
        return new CartOpResult(updated, updated ? "Quantity updated." : "Failed to update quantity.");
    }

    public boolean removeFromCart(int userId, int cartItemId) {
        return cartDAO.removeItem(userId, cartItemId);
    }

    public boolean clearCart(int userId) {
        return cartDAO.clearCart(userId);
    }

    public int getCartItemCount(int userId) {
        return cartDAO.getCartItemCount(userId);
    }

    public BigDecimal getCartSubtotal(int userId) {
        return cartDAO.calculateCartSubtotal(userId);
    }

    public record CouponResult(boolean valid, String message, Coupon coupon, BigDecimal discountAmount) {}

    public CouponResult validateCoupon(String code, BigDecimal subtotal) {
        if (code == null || code.trim().isEmpty()) {
            return new CouponResult(false, "Please enter a coupon code.", null, BigDecimal.ZERO);
        }

        Optional<Coupon> couponOpt = couponDAO.findByCode(code.trim());
        if (couponOpt.isEmpty()) {
            return new CouponResult(false, "Invalid or expired coupon code.", null, BigDecimal.ZERO);
        }

        Coupon coupon = couponOpt.get();
        if (!coupon.isActive()) {
            return new CouponResult(false, "This coupon is inactive.", null, BigDecimal.ZERO);
        }

        if (coupon.isExpired()) {
            return new CouponResult(false, "This coupon has expired.", null, BigDecimal.ZERO);
        }

        if (coupon.getMinimumOrderAmount() != null && subtotal.compareTo(coupon.getMinimumOrderAmount()) < 0) {
            return new CouponResult(false, "Minimum order amount of ₹" + coupon.getMinimumOrderAmount() + " required.", null, BigDecimal.ZERO);
        }

        BigDecimal discount = coupon.calculateDiscount(subtotal);
        return new CouponResult(true, "Coupon applied successfully!", coupon, discount);
    }
}
