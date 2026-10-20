package com.ecommerce.dao;

import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.util.DBConnection;

import com.ecommerce.util.LocalStorageManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    private static final ConcurrentHashMap<Integer, User> fallbackUsersById = LocalStorageManager.loadUsers();
    private static final ConcurrentHashMap<String, User> fallbackUsersByEmail = new ConcurrentHashMap<>();
    private static final AtomicInteger fallbackIdCounter;

    static {
        int maxId = 2;
        for (User u : fallbackUsersById.values()) {
            if (u.getEmail() != null) {
                fallbackUsersByEmail.put(u.getEmail().trim().toLowerCase(), u);
            }
            if (u.getId() > maxId) {
                maxId = u.getId();
            }
        }
        fallbackIdCounter = new AtomicInteger(maxId + 1);
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT id, name, email, password_hash, phone, role, created_at, updated_at FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by id: " + id, e);
        }
        return Optional.ofNullable(fallbackUsersById.get(id));
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String sql = "SELECT id, name, email, password_hash, phone, role, created_at, updated_at FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by email: " + email, e);
        }
        return Optional.ofNullable(fallbackUsersByEmail.get(email.trim().toLowerCase()));
    }

    public boolean emailExists(String email) {
        if (email == null) return false;
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if email exists: " + email, e);
        }
        return fallbackUsersByEmail.containsKey(email.trim().toLowerCase());
    }

    public boolean createUser(User user) {
        String sql = "INSERT INTO users (name, email, password_hash, phone, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail().trim().toLowerCase());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getRole() != null ? user.getRole().name() : Role.CUSTOMER.name());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating user in database: " + user.getEmail() + ", storing in local session store", e);
        }
        // Fallback local memory persistence when database is not connected
        if (user.getId() <= 0) {
            user.setId(fallbackIdCounter.getAndIncrement());
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }
        if (user.getUpdatedAt() == null) {
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        }
        fallbackUsersById.put(user.getId(), user);
        fallbackUsersByEmail.put(user.getEmail().trim().toLowerCase(), user);
        LocalStorageManager.saveUsers(fallbackUsersById);
        return true;
    }

    public boolean updateProfile(User user) {
        String sql = "UPDATE users SET name = ?, phone = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getPhone());
            ps.setInt(3, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating profile for user id: " + user.getId(), e);
        }
        User existing = fallbackUsersById.get(user.getId());
        if (existing != null) {
            existing.setName(user.getName());
            existing.setPhone(user.getPhone());
            existing.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            LocalStorageManager.saveUsers(fallbackUsersById);
            return true;
        }
        return false;
    }

    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating password for user id: " + userId, e);
        }
        User existing = fallbackUsersById.get(userId);
        if (existing != null) {
            existing.setPasswordHash(newPasswordHash);
            existing.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            LocalStorageManager.saveUsers(fallbackUsersById);
            return true;
        }
        return false;
    }

    public List<User> findAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, name, email, password_hash, phone, role, created_at, updated_at FROM users ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all users", e);
        }
        if (list.isEmpty()) {
            return new ArrayList<>(fallbackUsersById.values());
        }
        return list;
    }

    public int countCustomers() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int c = rs.getInt(1);
                if (c > 0) return c;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting customers", e);
        }
        return (int) fallbackUsersById.values().stream().filter(u -> u.getRole() == Role.CUSTOMER).count();
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setPhone(rs.getString("phone"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setUpdatedAt(rs.getTimestamp("updated_at"));
        return u;
    }
}
