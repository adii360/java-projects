package com.ecommerce.dao;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.util.DBConnection;
import com.ecommerce.util.LocalStorageManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CartDAO {
    private static final Logger LOGGER = Logger.getLogger(CartDAO.class.getName());

    private static final ConcurrentHashMap<Integer, List<CartItem>> fallbackCartByUser = LocalStorageManager.loadCart();
    private static final AtomicInteger fallbackCartItemId;

    static {
        int maxId = 1;
        for (List<CartItem> list : fallbackCartByUser.values()) {
            if (list != null) {
                synchronized (list) {
                    for (CartItem ci : list) {
                        if (ci.getId() > maxId) {
                            maxId = ci.getId();
                        }
                    }
                }
            }
        }
        fallbackCartItemId = new AtomicInteger(maxId + 1);
    }

    public List<CartItem> getCartItems(int userId) {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT ci.id, ci.user_id, ci.product_id, ci.quantity, ci.created_at, " +
                     "p.name AS product_name, p.slug AS product_slug, p.price, p.stock_quantity, p.image_url, p.active " +
                     "FROM cart_items ci " +
                     "JOIN products p ON ci.product_id = p.id " +
                     "WHERE ci.user_id = ? " +
                     "ORDER BY ci.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem ci = new CartItem();
                    ci.setId(rs.getInt("id"));
                    ci.setUserId(rs.getInt("user_id"));
                    ci.setProductId(rs.getInt("product_id"));
                    ci.setQuantity(rs.getInt("quantity"));
                    ci.setCreatedAt(rs.getTimestamp("created_at"));

                    Product p = new Product();
                    p.setId(rs.getInt("product_id"));
                    p.setName(rs.getString("product_name"));
                    p.setSlug(rs.getString("product_slug"));
                    p.setPrice(rs.getBigDecimal("price"));
                    p.setStockQuantity(rs.getInt("stock_quantity"));
                    p.setImageUrl(rs.getString("image_url"));
                    p.setActive(rs.getBoolean("active"));

                    ci.setProduct(p);
                    items.add(ci);
                }
            }
            return items;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving cart for user: " + userId + ", using local cart", e);
        }

        List<CartItem> fallback = fallbackCartByUser.get(userId);
        if (fallback != null) {
            synchronized (fallback) {
                ProductDAO productDAO = new ProductDAO();
                for (CartItem item : fallback) {
                    if (item.getProduct() == null) {
                        productDAO.findById(item.getProductId()).ifPresent(item::setProduct);
                    }
                }
                return new ArrayList<>(fallback);
            }
        }
        return items;
    }

    public boolean addItem(int userId, int productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        // MySQL ON DUPLICATE KEY UPDATE increments existing quantity
        String sql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding item to cart for user " + userId + ", product " + productId + ", saving to local cart", e);
        }

        List<CartItem> list = fallbackCartByUser.computeIfAbsent(userId, k -> java.util.Collections.synchronizedList(new ArrayList<>()));
        synchronized (list) {
            for (CartItem item : list) {
                if (item.getProductId() == productId) {
                    item.setQuantity(item.getQuantity() + quantity);
                    LocalStorageManager.saveCart(fallbackCartByUser);
                    return true;
                }
            }
            CartItem newItem = new CartItem();
            newItem.setId(fallbackCartItemId.getAndIncrement());
            newItem.setUserId(userId);
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            newItem.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            new ProductDAO().findById(productId).ifPresent(newItem::setProduct);
            list.add(newItem);
            LocalStorageManager.saveCart(fallbackCartByUser);
        }
        return true;
    }

    public boolean updateQuantity(int userId, int cartItemId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeItem(userId, cartItemId);
        }
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, cartItemId);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating cart quantity, updating local cart", e);
        }

        List<CartItem> list = fallbackCartByUser.get(userId);
        if (list != null) {
            synchronized (list) {
                for (CartItem item : list) {
                    if (item.getId() == cartItemId) {
                        item.setQuantity(newQuantity);
                        LocalStorageManager.saveCart(fallbackCartByUser);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean removeItem(int userId, int cartItemId) {
        String sql = "DELETE FROM cart_items WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartItemId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing cart item " + cartItemId + ", updating local cart", e);
        }

        List<CartItem> list = fallbackCartByUser.get(userId);
        if (list != null) {
            synchronized (list) {
                boolean removed = list.removeIf(item -> item.getId() == cartItemId);
                if (removed) {
                    LocalStorageManager.saveCart(fallbackCartByUser);
                }
                return removed;
            }
        }
        return false;
    }

    public boolean clearCart(int userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error clearing cart for user: " + userId + ", clearing local cart", e);
        }

        List<CartItem> list = fallbackCartByUser.get(userId);
        if (list != null) {
            list.clear();
            LocalStorageManager.saveCart(fallbackCartByUser);
        }
        return true;
    }

    public boolean clearCartInTransaction(Connection conn, int userId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() >= 0;
        }
    }

    public int getCartItemCount(int userId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting cart items for user: " + userId + ", using local cart", e);
        }

        List<CartItem> list = fallbackCartByUser.get(userId);
        if (list != null) {
            synchronized (list) {
                return list.stream().mapToInt(CartItem::getQuantity).sum();
            }
        }
        return 0;
    }

    public BigDecimal calculateCartSubtotal(int userId) {
        String sql = "SELECT COALESCE(SUM(ci.quantity * p.price), 0) " +
                     "FROM cart_items ci " +
                     "JOIN products p ON ci.product_id = p.id " +
                     "WHERE ci.user_id = ? AND p.active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating cart subtotal for user: " + userId + ", using local cart", e);
        }

        List<CartItem> list = fallbackCartByUser.get(userId);
        if (list != null) {
            synchronized (list) {
                BigDecimal total = BigDecimal.ZERO;
                ProductDAO productDAO = new ProductDAO();
                for (CartItem item : list) {
                    if (item.getProduct() == null) {
                        productDAO.findById(item.getProductId()).ifPresent(item::setProduct);
                    }
                    if (item.getProduct() != null && item.getProduct().getPrice() != null) {
                        total = total.add(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                }
                return total;
            }
        }
        return BigDecimal.ZERO;
    }
}
