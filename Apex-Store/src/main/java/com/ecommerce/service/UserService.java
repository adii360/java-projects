package com.ecommerce.service;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.util.PasswordUtil;
import com.ecommerce.util.ValidationUtil;

import java.util.Optional;

public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public record AuthResult(boolean success, String message, User user) {}

    public AuthResult register(String name, String email, String password, String confirmPassword, String phone) {
        if (name == null || name.trim().isEmpty()) {
            return new AuthResult(false, "Name is required.", null);
        }
        if (email == null || !ValidationUtil.isValidEmail(email)) {
            return new AuthResult(false, "Please provide a valid email address.", null);
        }
        if (password == null || password.length() < 6) {
            return new AuthResult(false, "Password must be at least 6 characters.", null);
        }
        if (!password.equals(confirmPassword)) {
            return new AuthResult(false, "Passwords do not match.", null);
        }

        String cleanEmail = email.trim().toLowerCase();
        Optional<User> existingOpt = userDAO.findByEmail(cleanEmail);
        if (existingOpt.isPresent()) {
            User existing = existingOpt.get();
            if (existing.getPasswordHash() != null && existing.getPasswordHash().startsWith("GUEST_")) {
                existing.setName(name.trim());
                existing.setPasswordHash(PasswordUtil.hashPassword(password));
                if (phone != null && !phone.trim().isEmpty()) {
                    existing.setPhone(phone.trim());
                }
                userDAO.updatePassword(existing.getId(), existing.getPasswordHash());
                userDAO.updateProfile(existing);
                new com.ecommerce.dao.OrderDAO().linkOrdersToUser(cleanEmail, existing.getId());
                return new AuthResult(true, "Registration successful! Your past orders are now saved to your account.", existing);
            }
            return new AuthResult(false, "An account with this email already exists. Please log in or use another email.", null);
        }

        String passwordHash = PasswordUtil.hashPassword(password);
        User user = new User();
        user.setName(name.trim());
        user.setEmail(cleanEmail);
        user.setPasswordHash(passwordHash);
        user.setPhone(phone != null ? phone.trim() : "");
        user.setRole(Role.CUSTOMER); // Public registration can ONLY create CUSTOMER role

        boolean created = userDAO.createUser(user);
        if (created) {
            new com.ecommerce.dao.OrderDAO().linkOrdersToUser(user.getEmail(), user.getId());
            return new AuthResult(true, "Registration successful!", user);
        } else {
            return new AuthResult(false, "Registration failed due to a server error. Please try again.", null);
        }
    }

    public AuthResult login(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            return new AuthResult(false, "Email and password are required.", null);
        }

        Optional<User> userOpt = userDAO.findByEmail(email.trim().toLowerCase());
        if (userOpt.isEmpty()) {
            return new AuthResult(false, "Invalid email or password.", null);
        }

        User user = userOpt.get();
        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            return new AuthResult(false, "Invalid email or password.", null);
        }

        new com.ecommerce.dao.OrderDAO().linkOrdersToUser(user.getEmail(), user.getId());
        return new AuthResult(true, "Login successful!", user);
    }

    public Optional<User> getUserById(int id) {
        return userDAO.findById(id);
    }

    public boolean updateProfile(User user) {
        return userDAO.updateProfile(user);
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        if (!PasswordUtil.checkPassword(oldPassword, user.getPasswordHash())) {
            return false;
        }
        if (newPassword == null || newPassword.length() < 6) {
            return false;
        }
        String newHash = PasswordUtil.hashPassword(newPassword);
        return userDAO.updatePassword(userId, newHash);
    }
}
