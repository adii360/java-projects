package com.ecommerce.dao;

import com.ecommerce.model.Product;
import com.ecommerce.model.WishlistItem;
import com.ecommerce.util.DBConnection;
import com.ecommerce.util.LocalStorageManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WishlistDAO {
    private static final Logger LOGGER = Logger.getLogger(WishlistDAO.class.getName());

    private static final ConcurrentHashMap<Integer, List<WishlistItem>> fallbackWishlistByUser = LocalStorageManager.loadWishlist();
    private static final AtomicInteger fallbackWishlistIdCounter;

    static {
        int maxId = 1;
        for (List<WishlistItem> list : fallbackWishlistByUser.values()) {
            if (list != null) {
                synchronized (list) {
                    for (WishlistItem wi : list) {
                        if (wi.getId() > maxId) {
                            maxId = wi.getId();
                        }
                    }
                }
            }
        }
        fallbackWishlistIdCounter = new AtomicInteger(maxId + 1);
    }

    public List<WishlistItem> getWishlistItems(int userId) {
        List<WishlistItem> items = new ArrayList<>();
        String sql = "SELECT w.id, w.user_id, w.product_id, w.created_at, " +
                     "p.name AS product_name, p.slug AS product_slug, p.price, p.stock_quantity, p.image_url, p.active " +
                     "FROM wishlist_items w " +
                     "JOIN products p ON w.product_id = p.id " +
                     "WHERE w.user_id = ? " +
                     "ORDER BY w.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WishlistItem item = new WishlistItem();
                    item.setId(rs.getInt("id"));
                    item.setUserId(rs.getInt("user_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setCreatedAt(rs.getTimestamp("created_at"));

                    Product p = new Product();
                    p.setId(rs.getInt("product_id"));
                    p.setName(rs.getString("product_name"));
                    p.setSlug(rs.getString("product_slug"));
                    p.setPrice(rs.getBigDecimal("price"));
                    p.setStockQuantity(rs.getInt("stock_quantity"));
                    p.setImageUrl(rs.getString("image_url"));
                    p.setActive(rs.getBoolean("active"));

                    item.setProduct(p);
                    items.add(item);
                }
            }
            return items;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching wishlist for user: " + userId + ", using local fallback store", e);
        }

        List<WishlistItem> fallback = fallbackWishlistByUser.get(userId);
        if (fallback != null) {
            synchronized (fallback) {
                ProductDAO productDAO = new ProductDAO();
                for (WishlistItem item : fallback) {
                    if (item.getProduct() == null) {
                        productDAO.findById(item.getProductId()).ifPresent(item::setProduct);
                    }
                }
                return new ArrayList<>(fallback);
            }
        }
        return items;
    }

    public boolean addItem(int userId, int productId) {
        String sql = "INSERT IGNORE INTO wishlist_items (user_id, product_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding item to wishlist in database, saving to local fallback store", e);
        }

        List<WishlistItem> list = fallbackWishlistByUser.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>()));
        synchronized (list) {
            for (WishlistItem item : list) {
                if (item.getProductId() == productId) {
                    return true;
                }
            }
            WishlistItem newItem = new WishlistItem();
            newItem.setId(fallbackWishlistIdCounter.getAndIncrement());
            newItem.setUserId(userId);
            newItem.setProductId(productId);
            newItem.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            new ProductDAO().findById(productId).ifPresent(newItem::setProduct);
            list.add(0, newItem);
            LocalStorageManager.saveWishlist(fallbackWishlistByUser);
        }
        return true;
    }

    public boolean removeItem(int userId, int productId) {
        String sql = "DELETE FROM wishlist_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing item from wishlist in database, removing from local fallback store", e);
        }

        List<WishlistItem> list = fallbackWishlistByUser.get(userId);
        if (list != null) {
            synchronized (list) {
                boolean removed = list.removeIf(item -> item.getProductId() == productId);
                if (removed) {
                    LocalStorageManager.saveWishlist(fallbackWishlistByUser);
                }
                return removed;
            }
        }
        return true;
    }

    public boolean isInWishlist(int userId, int productId) {
        String sql = "SELECT 1 FROM wishlist_items WHERE user_id = ? AND product_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking wishlist status in database, checking local fallback store", e);
        }

        List<WishlistItem> list = fallbackWishlistByUser.get(userId);
        if (list != null) {
            synchronized (list) {
                return list.stream().anyMatch(item -> item.getProductId() == productId);
            }
        }
        return false;
    }

    public int getWishlistCount(int userId) {
        String sql = "SELECT COUNT(*) FROM wishlist_items WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting wishlist items in database, counting from local fallback store", e);
        }

        List<WishlistItem> list = fallbackWishlistByUser.get(userId);
        if (list != null) {
            synchronized (list) {
                return list.size();
            }
        }
        return 0;
    }
}
