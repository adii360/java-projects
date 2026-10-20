package com.ecommerce.dao;

import com.ecommerce.model.Address;
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

public class AddressDAO {
    private static final Logger LOGGER = Logger.getLogger(AddressDAO.class.getName());

    private static final ConcurrentHashMap<Integer, Address> fallbackAddressesById = LocalStorageManager.loadAddresses();
    private static final AtomicInteger fallbackAddressIdCounter;

    static {
        int maxId = 100;
        for (Address a : fallbackAddressesById.values()) {
            if (a.getId() > maxId) {
                maxId = a.getId();
            }
        }
        fallbackAddressIdCounter = new AtomicInteger(maxId + 1);
    }

    public List<Address> findByUserId(int userId) {
        List<Address> list = new ArrayList<>();
        String sql = "SELECT id, user_id, recipient_name, phone, address_line, city, state, postal_code, country, is_default, created_at " +
                     "FROM addresses WHERE user_id = ? ORDER BY is_default DESC, created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAddress(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching addresses for user id: " + userId, e);
        }

        if (list.isEmpty()) {
            for (Address a : fallbackAddressesById.values()) {
                if (a.getUserId() == userId) {
                    list.add(a);
                }
            }
        }
        return list;
    }

    public Optional<Address> findByIdAndUserId(int addressId, int userId) {
        String sql = "SELECT id, user_id, recipient_name, phone, address_line, city, state, postal_code, country, is_default, created_at " +
                     "FROM addresses WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, addressId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAddress(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching address id " + addressId + " for user " + userId, e);
        }

        Address fallback = fallbackAddressesById.get(addressId);
        if (fallback != null && fallback.getUserId() == userId) {
            return Optional.of(fallback);
        }
        return Optional.empty();
    }

    public Optional<Address> findById(int addressId) {
        String sql = "SELECT id, user_id, recipient_name, phone, address_line, city, state, postal_code, country, is_default, created_at " +
                     "FROM addresses WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, addressId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAddress(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching address id " + addressId, e);
        }
        return Optional.ofNullable(fallbackAddressesById.get(addressId));
    }

    public boolean createAddress(Address address) {
        String sql = "INSERT INTO addresses (user_id, recipient_name, phone, address_line, city, state, postal_code, country, is_default) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, address.getUserId());
            ps.setString(2, address.getRecipientName());
            ps.setString(3, address.getPhone());
            ps.setString(4, address.getAddressLine());
            ps.setString(5, address.getCity());
            ps.setString(6, address.getState());
            ps.setString(7, address.getPostalCode());
            ps.setString(8, address.getCountry());
            ps.setBoolean(9, address.isDefault());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        address.setId(generatedKeys.getInt(1));
                    }
                }
                if (address.getCreatedAt() == null) {
                    address.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                }
                fallbackAddressesById.put(address.getId(), address);
                LocalStorageManager.saveAddresses(fallbackAddressesById);
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating address, using local fallback", e);
        }

        // Resilient in-memory fallback
        if (address.getId() <= 0) {
            address.setId(fallbackAddressIdCounter.incrementAndGet());
        }
        if (address.getCreatedAt() == null) {
            address.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }
        fallbackAddressesById.put(address.getId(), address);
        LocalStorageManager.saveAddresses(fallbackAddressesById);
        return true;
    }

    public boolean updateAddress(Address address) {
        String sql = "UPDATE addresses SET recipient_name = ?, phone = ?, address_line = ?, city = ?, state = ?, postal_code = ?, country = ?, is_default = ? " +
                     "WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, address.getRecipientName());
            ps.setString(2, address.getPhone());
            ps.setString(3, address.getAddressLine());
            ps.setString(4, address.getCity());
            ps.setString(5, address.getState());
            ps.setString(6, address.getPostalCode());
            ps.setString(7, address.getCountry());
            ps.setBoolean(8, address.isDefault());
            ps.setInt(9, address.getId());
            ps.setInt(10, address.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating address id: " + address.getId(), e);
        }
        fallbackAddressesById.put(address.getId(), address);
        LocalStorageManager.saveAddresses(fallbackAddressesById);
        return true;
    }

    public boolean deleteAddress(int addressId, int userId) {
        String sql = "DELETE FROM addresses WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, addressId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting address id: " + addressId, e);
        }
        fallbackAddressesById.remove(addressId);
        LocalStorageManager.saveAddresses(fallbackAddressesById);
        return true;
    }

    public void clearDefaultAddress(int userId) {
        String sql = "UPDATE addresses SET is_default = FALSE WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error resetting default addresses for user " + userId, e);
        }
        for (Address a : fallbackAddressesById.values()) {
            if (a.getUserId() == userId) {
                a.setDefault(false);
            }
        }
    }

    private Address mapResultSetToAddress(ResultSet rs) throws SQLException {
        Address a = new Address();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setRecipientName(rs.getString("recipient_name"));
        a.setPhone(rs.getString("phone"));
        a.setAddressLine(rs.getString("address_line"));
        a.setCity(rs.getString("city"));
        a.setState(rs.getString("state"));
        a.setPostalCode(rs.getString("postal_code"));
        a.setCountry(rs.getString("country"));
        a.setDefault(rs.getBoolean("is_default"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }
}
