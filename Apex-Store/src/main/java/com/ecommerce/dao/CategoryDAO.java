package com.ecommerce.dao;

import com.ecommerce.model.Category;
import com.ecommerce.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CategoryDAO {
    private static final Logger LOGGER = Logger.getLogger(CategoryDAO.class.getName());

    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, slug, description, image_url, created_at FROM categories ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all categories", e);
        }
        if (list.isEmpty()) {
            return getDefaultCategories();
        }
        return list;
    }

    public Optional<Category> findById(int id) {
        String sql = "SELECT id, name, slug, description, image_url, created_at FROM categories WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching category by id: " + id, e);
        }
        return getDefaultCategories().stream().filter(c -> c.getId() == id).findFirst();
    }

    public Optional<Category> findBySlug(String slug) {
        String sql = "SELECT id, name, slug, description, image_url, created_at FROM categories WHERE slug = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slug);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching category by slug: " + slug, e);
        }
        if (slug != null) {
            return getDefaultCategories().stream().filter(c -> slug.equalsIgnoreCase(c.getSlug())).findFirst();
        }
        return Optional.empty();
    }

    public static List<Category> getDefaultCategories() {
        List<Category> defaults = new ArrayList<>();
        Category c1 = new Category(1, "Electronics", "electronics",
                "High performance laptops, smartphones, headphones, and modern gadgets.",
                "https://images.unsplash.com/photo-1498049794561-7780e7231661?w=600&q=80",
                new Timestamp(System.currentTimeMillis()));
        c1.setItemCount(5);

        Category c2 = new Category(2, "Fashion & Apparel", "fashion-apparel",
                "Trending apparel, footwear, accessories, and designer essentials.",
                "https://images.unsplash.com/photo-1445205170230-053b83016050?w=600&q=80",
                new Timestamp(System.currentTimeMillis()));
        c2.setItemCount(5);

        Category c3 = new Category(3, "Home & Kitchen", "home-kitchen",
                "Smart home appliances, modern ergonomic furniture, and premium kitchenware.",
                "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=600&q=80",
                new Timestamp(System.currentTimeMillis()));
        c3.setItemCount(5);

        Category c4 = new Category(4, "Audio & Accessories", "audio-accessories",
                "Noise-canceling headphones, Bluetooth speakers, and studio microphones.",
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&q=80",
                new Timestamp(System.currentTimeMillis()));
        c4.setItemCount(5);

        Category c5 = new Category(5, "Sports & Fitness", "sports-fitness",
                "Workout gear, smart wearables, yoga mats, and athletic essentials.",
                "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600&q=80",
                new Timestamp(System.currentTimeMillis()));
        c5.setItemCount(5);

        defaults.add(c1);
        defaults.add(c2);
        defaults.add(c3);
        defaults.add(c4);
        defaults.add(c5);
        return defaults;
    }

    public boolean createCategory(Category category) {
        String sql = "INSERT INTO categories (name, slug, description, image_url) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getSlug());
            ps.setString(3, category.getDescription());
            ps.setString(4, category.getImageUrl());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        category.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating category: " + category.getName(), e);
        }
        return false;
    }

    public boolean updateCategory(Category category) {
        String sql = "UPDATE categories SET name = ?, slug = ?, description = ?, image_url = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getSlug());
            ps.setString(3, category.getDescription());
            ps.setString(4, category.getImageUrl());
            ps.setInt(5, category.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating category id: " + category.getId(), e);
            return false;
        }
    }

    public boolean deleteCategory(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting category id: " + id, e);
            return false;
        }
    }

    public int countCategories() {
        String sql = "SELECT COUNT(*) FROM categories";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting categories", e);
        }
        return 0;
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setSlug(rs.getString("slug"));
        c.setDescription(rs.getString("description"));
        c.setImageUrl(rs.getString("image_url"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        return c;
    }
}
