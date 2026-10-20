package com.ecommerce.service;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    static class FakeUserDAO extends UserDAO {
        private final Map<String, User> usersByEmail = new HashMap<>();
        private final Map<Integer, User> usersById = new HashMap<>();
        private int sequence = 1;

        @Override
        public boolean emailExists(String email) {
            return usersByEmail.containsKey(email.trim().toLowerCase());
        }

        @Override
        public boolean createUser(User user) {
            user.setId(sequence++);
            usersByEmail.put(user.getEmail().trim().toLowerCase(), user);
            usersById.put(user.getId(), user);
            return true;
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(usersByEmail.get(email.trim().toLowerCase()));
        }

        @Override
        public Optional<User> findById(int id) {
            return Optional.ofNullable(usersById.get(id));
        }

        @Override
        public boolean updatePassword(int userId, String newPasswordHash) {
            User user = usersById.get(userId);
            if (user != null) {
                user.setPasswordHash(newPasswordHash);
                return true;
            }
            return false;
        }
    }

    private FakeUserDAO fakeUserDAO;
    private UserService userService;

    @BeforeEach
    public void setup() {
        fakeUserDAO = new FakeUserDAO();
        userService = new UserService(fakeUserDAO);
    }

    @Test
    public void testRegisterValidationRejectsInvalidInputs() {
        // Missing name
        var res1 = userService.register("", "test@example.com", "password123", "password123", "123");
        assertFalse(res1.success());
        assertEquals("Name is required.", res1.message());

        // Invalid email
        var res2 = userService.register("John", "invalid-email", "password123", "password123", "123");
        assertFalse(res2.success());
        assertTrue(res2.message().contains("valid email"));

        // Short password
        var res3 = userService.register("John", "john@example.com", "123", "123", "123");
        assertFalse(res3.success());
        assertTrue(res3.message().contains("at least 6 characters"));

        // Mismatched passwords
        var res4 = userService.register("John", "john@example.com", "password123", "password456", "123");
        assertFalse(res4.success());
        assertEquals("Passwords do not match.", res4.message());
    }

    @Test
    public void testRegisterRejectsDuplicateEmail() {
        // First registration succeeds
        var res1 = userService.register("Jane", "existing@example.com", "password123", "password123", "123");
        assertTrue(res1.success());

        // Second registration with same email should fail
        var res2 = userService.register("Jane Again", "EXISTING@example.com", "password123", "password123", "123");
        assertFalse(res2.success());
        assertTrue(res2.message().contains("already exists"));
    }

    @Test
    public void testRegisterSuccessfullyCreatesCustomer() {
        var res = userService.register("Alice", "alice@example.com", "Secret@123", "Secret@123", "+123456789");
        assertTrue(res.success());
        assertNotNull(res.user());
        assertEquals("alice@example.com", res.user().getEmail());
        assertEquals(Role.CUSTOMER, res.user().getRole());
        assertTrue(res.user().getPasswordHash().startsWith("$2a$"));
    }

    @Test
    public void testLoginAuthentication() {
        // Register user
        userService.register("Bob", "bob@example.com", "Pass@1234", "Pass@1234", "987654321");

        // Successful login
        var successLogin = userService.login("bob@example.com", "Pass@1234");
        assertTrue(successLogin.success());
        assertNotNull(successLogin.user());
        assertEquals("Bob", successLogin.user().getName());

        // Wrong password
        var failedLogin = userService.login("bob@example.com", "WrongPassword");
        assertFalse(failedLogin.success());
        assertEquals("Invalid email or password.", failedLogin.message());

        // Non-existent user
        var nonExistent = userService.login("ghost@example.com", "Pass@1234");
        assertFalse(nonExistent.success());
    }
}
