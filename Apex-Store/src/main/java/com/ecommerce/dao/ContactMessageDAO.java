package com.ecommerce.dao;

import com.ecommerce.model.ContactMessage;
import com.ecommerce.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContactMessageDAO {
    private static final Logger LOGGER = Logger.getLogger(ContactMessageDAO.class.getName());

    public boolean createMessage(ContactMessage msg) {
        String sql = "INSERT INTO contact_messages (name, email, subject, message) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, msg.getName());
            ps.setString(2, msg.getEmail());
            ps.setString(3, msg.getSubject());
            ps.setString(4, msg.getMessage());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        msg.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving contact message", e);
        }
        return false;
    }

    public List<ContactMessage> findAllMessages() {
        List<ContactMessage> list = new ArrayList<>();
        String sql = "SELECT id, name, email, subject, message, created_at FROM contact_messages ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ContactMessage m = new ContactMessage();
                m.setId(rs.getInt("id"));
                m.setName(rs.getString("name"));
                m.setEmail(rs.getString("email"));
                m.setSubject(rs.getString("subject"));
                m.setMessage(rs.getString("message"));
                m.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(m);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching contact messages", e);
        }
        return list;
    }

    public boolean deleteMessage(int id) {
        String sql = "DELETE FROM contact_messages WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting contact message id: " + id, e);
            return false;
        }
    }
}
