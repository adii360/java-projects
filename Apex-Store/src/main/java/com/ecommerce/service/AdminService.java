package com.ecommerce.service;

import com.ecommerce.dao.CategoryDAO;
import com.ecommerce.dao.OrderDAO;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.UserDAO;
import com.ecommerce.model.*;

import java.math.BigDecimal;
import java.util.List;

public class AdminService {
    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final OrderDAO orderDAO;
    private final UserDAO userDAO;

    public AdminService() {
        this.productDAO = new ProductDAO();
        this.categoryDAO = new CategoryDAO();
        this.orderDAO = new OrderDAO();
        this.userDAO = new UserDAO();
    }

    public AdminService(ProductDAO productDAO, CategoryDAO categoryDAO, OrderDAO orderDAO, UserDAO userDAO) {
        this.productDAO = productDAO;
        this.categoryDAO = categoryDAO;
        this.orderDAO = orderDAO;
        this.userDAO = userDAO;
    }

    public record DashboardStats(
            int totalProducts,
            int totalCustomers,
            int totalOrders,
            BigDecimal totalRevenue,
            List<Order> recentOrders,
            List<Product> lowStockProducts
    ) {}

    public DashboardStats getDashboardStats() {
        int totalProducts = productDAO.countAll(false);
        int totalCustomers = userDAO.countCustomers();
        int totalOrders = orderDAO.countAllOrders();
        BigDecimal revenue = orderDAO.calculateTotalRevenue();
        List<Order> recentOrders = orderDAO.getRecentOrders(5);
        List<Product> lowStock = productDAO.getLowStockProducts(5);

        return new DashboardStats(totalProducts, totalCustomers, totalOrders, revenue, recentOrders, lowStock);
    }

    // Product Management
    public List<Product> getAllProducts(int offset, int limit) {
        return productDAO.findAll(false, offset, limit);
    }

    public int getTotalProductCount() {
        return productDAO.countAll(false);
    }

    public boolean createProduct(Product product) {
        return productDAO.createProduct(product);
    }

    public boolean updateProduct(Product product) {
        return productDAO.updateProduct(product);
    }

    public boolean deleteProduct(int productId) {
        return productDAO.deleteProduct(productId);
    }

    public boolean updateStock(int productId, int quantity) {
        return productDAO.updateStockQuantity(productId, quantity);
    }

    // Category Management
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    public boolean createCategory(Category category) {
        return categoryDAO.createCategory(category);
    }

    public boolean updateCategory(Category category) {
        return categoryDAO.updateCategory(category);
    }

    public boolean deleteCategory(int categoryId) {
        return categoryDAO.deleteCategory(categoryId);
    }

    // Order Management
    public List<Order> getAllOrders(int offset, int limit) {
        return orderDAO.findAllOrders(offset, limit);
    }

    public int getTotalOrderCount() {
        return orderDAO.countAllOrders();
    }

    public boolean updateOrderStatus(int orderId, OrderStatus orderStatus, PaymentStatus paymentStatus) {
        return orderDAO.updateOrderStatus(orderId, orderStatus, paymentStatus);
    }

    // Customer Management
    public List<User> getAllUsers() {
        return userDAO.findAllUsers();
    }
}
