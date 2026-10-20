package com.ecommerce.util;

import com.ecommerce.model.Address;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.model.WishlistItem;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * File-based permanent local storage manager.
 * Persists users, orders, addresses, cart items, and wishlist items directly to disk in the user's home directory.
 * Ensures user registrations, carts, wishlists, and placed orders are never lost across restarts, redeployments, or logouts.
 */
public class LocalStorageManager {
    private static final Logger LOGGER = Logger.getLogger(LocalStorageManager.class.getName());
    private static final File DATA_DIR;

    static {
        File dir = new File(System.getProperty("user.home"), ".ecommerce_data");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        DATA_DIR = dir;
    }

    private static File getFile(String fileName) {
        return new File(DATA_DIR, fileName);
    }

    @SuppressWarnings("unchecked")
    private static synchronized <T> T readObject(String fileName) {
        File file = getFile(fileName);
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            return (T) ois.readObject();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not read persisted data from " + file.getAbsolutePath() + ": " + e.getMessage());
            return null;
        }
    }

    private static synchronized void writeObject(String fileName, Object data) {
        File file = getFile(fileName);
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(data);
            oos.flush();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not persist data to " + file.getAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    public static ConcurrentHashMap<Integer, User> loadUsers() {
        Map<Integer, User> map = readObject("users.dat");
        ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<>();
        if (map != null && !map.isEmpty()) {
            users.putAll(map);
        } else {
            // Seed default users if empty
            User admin = new User(1, "Administrator", "admin@ecommerce.com",
                    "$2a$10$3zR1iGv5X/6yWwJjZ8k5fehV30yK3J.u1q0v5Dq6QZ2Y5XwR6F2O.",
                    "+1-800-555-0100", Role.ADMIN,
                    new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
            users.put(admin.getId(), admin);

            User customer = new User(2, "Jane Customer", "jane@example.com",
                    "$2a$10$3zR1iGv5X/6yWwJjZ8k5fehV30yK3J.u1q0v5Dq6QZ2Y5XwR6F2O.",
                    "+1-555-0199", Role.CUSTOMER,
                    new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
            users.put(customer.getId(), customer);

            saveUsers(users);
        }
        return users;
    }

    public static void saveUsers(Map<Integer, User> users) {
        writeObject("users.dat", new HashMap<>(users));
    }

    public static ConcurrentHashMap<Integer, Order> loadOrders() {
        Map<Integer, Order> map = readObject("orders.dat");
        ConcurrentHashMap<Integer, Order> orders = new ConcurrentHashMap<>();
        if (map != null) {
            orders.putAll(map);
        }
        return orders;
    }

    public static void saveOrders(Map<Integer, Order> orders) {
        writeObject("orders.dat", new HashMap<>(orders));
    }

    public static ConcurrentHashMap<Integer, Address> loadAddresses() {
        Map<Integer, Address> map = readObject("addresses.dat");
        ConcurrentHashMap<Integer, Address> addresses = new ConcurrentHashMap<>();
        if (map != null) {
            addresses.putAll(map);
        }
        return addresses;
    }

    public static void saveAddresses(Map<Integer, Address> addresses) {
        writeObject("addresses.dat", new HashMap<>(addresses));
    }

    public static ConcurrentHashMap<Integer, List<CartItem>> loadCart() {
        Map<Integer, List<CartItem>> map = readObject("cart.dat");
        ConcurrentHashMap<Integer, List<CartItem>> cartMap = new ConcurrentHashMap<>();
        if (map != null) {
            for (Map.Entry<Integer, List<CartItem>> entry : map.entrySet()) {
                if (entry.getValue() != null) {
                    cartMap.put(entry.getKey(), java.util.Collections.synchronizedList(new ArrayList<>(entry.getValue())));
                }
            }
        }
        return cartMap;
    }

    public static void saveCart(Map<Integer, List<CartItem>> cart) {
        Map<Integer, List<CartItem>> copy = new HashMap<>();
        for (Map.Entry<Integer, List<CartItem>> entry : cart.entrySet()) {
            if (entry.getValue() != null) {
                synchronized (entry.getValue()) {
                    copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                }
            }
        }
        writeObject("cart.dat", copy);
    }

    public static ConcurrentHashMap<Integer, List<WishlistItem>> loadWishlist() {
        Map<Integer, List<WishlistItem>> map = readObject("wishlist.dat");
        ConcurrentHashMap<Integer, List<WishlistItem>> wishlistMap = new ConcurrentHashMap<>();
        if (map != null) {
            for (Map.Entry<Integer, List<WishlistItem>> entry : map.entrySet()) {
                if (entry.getValue() != null) {
                    wishlistMap.put(entry.getKey(), java.util.Collections.synchronizedList(new ArrayList<>(entry.getValue())));
                }
            }
        }
        return wishlistMap;
    }

    public static void saveWishlist(Map<Integer, List<WishlistItem>> wishlist) {
        Map<Integer, List<WishlistItem>> copy = new HashMap<>();
        for (Map.Entry<Integer, List<WishlistItem>> entry : wishlist.entrySet()) {
            if (entry.getValue() != null) {
                synchronized (entry.getValue()) {
                    copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                }
            }
        }
        writeObject("wishlist.dat", copy);
    }
}
