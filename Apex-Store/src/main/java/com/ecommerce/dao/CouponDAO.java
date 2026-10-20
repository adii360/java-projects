package com.ecommerce.dao;

import com.ecommerce.model.Coupon;
import com.ecommerce.model.DiscountType;
import com.ecommerce.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CouponDAO {
    private static final Logger LOGGER = Logger.getLogger(CouponDAO.class.getName());

    private static final List<Coupon> FALLBACK_COUPONS = List.of(
        new Coupon(1, "WELCOME10", DiscountType.PERCENTAGE, new BigDecimal("10.00"), new BigDecimal("999.00"), Date.valueOf("2028-12-31"), true),
        new Coupon(2, "SAVE20", DiscountType.FIXED, new BigDecimal("200.00"), new BigDecimal("1999.00"), Date.valueOf("2028-12-31"), true),
        new Coupon(3, "MEGA50", DiscountType.FIXED, new BigDecimal("500.00"), new BigDecimal("4999.00"), Date.valueOf("2028-12-31"), true)
    );

    public Optional<Coupon> findByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        String sql = "SELECT id, code, discount_type, discount_value, minimum_order_amount, expiry_date, active " +
                     "FROM coupons WHERE UPPER(code) = UPPER(?) AND active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCoupon(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding coupon: " + code, e);
        }
        return FALLBACK_COUPONS.stream()
                .filter(c -> c.getCode().equalsIgnoreCase(code.trim()) && c.isActive())
                .findFirst();
    }

    public List<Coupon> findAll() {
        List<Coupon> list = new ArrayList<>();
        String sql = "SELECT id, code, discount_type, discount_value, minimum_order_amount, expiry_date, active FROM coupons ORDER BY expiry_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToCoupon(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all coupons", e);
        }
        return list;
    }

    public boolean createCoupon(Coupon coupon) {
        String sql = "INSERT INTO coupons (code, discount_type, discount_value, minimum_order_amount, expiry_date, active) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, coupon.getCode().trim().toUpperCase());
            ps.setString(2, coupon.getDiscountType().name());
            ps.setBigDecimal(3, coupon.getDiscountValue());
            ps.setBigDecimal(4, coupon.getMinimumOrderAmount());
            ps.setDate(5, coupon.getExpiryDate());
            ps.setBoolean(6, coupon.isActive());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        coupon.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating coupon: " + coupon.getCode(), e);
        }
        return false;
    }

    public boolean updateCoupon(Coupon coupon) {
        String sql = "UPDATE coupons SET discount_type = ?, discount_value = ?, minimum_order_amount = ?, expiry_date = ?, active = ? " +
                     "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, coupon.getDiscountType().name());
            ps.setBigDecimal(2, coupon.getDiscountValue());
            ps.setBigDecimal(3, coupon.getMinimumOrderAmount());
            ps.setDate(4, coupon.getExpiryDate());
            ps.setBoolean(5, coupon.isActive());
            ps.setInt(6, coupon.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating coupon id: " + coupon.getId(), e);
            return false;
        }
    }

    public boolean deleteCoupon(int id) {
        String sql = "DELETE FROM coupons WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting coupon id: " + id, e);
            return false;
        }
    }

    private Coupon mapResultSetToCoupon(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setDiscountType(DiscountType.valueOf(rs.getString("discount_type")));
        c.setDiscountValue(rs.getBigDecimal("discount_value"));
        c.setMinimumOrderAmount(rs.getBigDecimal("minimum_order_amount"));
        c.setExpiryDate(rs.getDate("expiry_date"));
        c.setActive(rs.getBoolean("active"));
        return c;
    }
}
