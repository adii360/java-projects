package com.ecommerce.servlet;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.service.AdminService;
import com.ecommerce.service.ProductService;
import com.ecommerce.util.CsrfUtil;
import com.ecommerce.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet(name = "AdminProductServlet", urlPatterns = "/admin/products")
public class AdminProductServlet extends HttpServlet {
    private final AdminService adminService = new AdminService();
    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = ValidationUtil.parseIntOrDefault(request.getParameter("page"), 1);
        int pageSize = 15;
        int offset = (page - 1) * pageSize;

        List<Product> products = adminService.getAllProducts(offset, pageSize);
        int totalProducts = adminService.getTotalProductCount();
        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);

        List<Category> categories = productService.getAllCategories();

        request.setAttribute("products", products);
        request.setAttribute("categories", categories);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        String editIdStr = request.getParameter("editId");
        if (editIdStr != null) {
            int editId = ValidationUtil.parseIntOrDefault(editIdStr, -1);
            productService.getProductById(editId).ifPresent(p -> request.setAttribute("editProduct", p));
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/products.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValidToken(request)) {
            response.sendRedirect(request.getContextPath() + "/admin/products?error=csrf");
            return;
        }

        String action = request.getParameter("action");

        if ("create".equals(action)) {
            String name = request.getParameter("name");
            int categoryId = ValidationUtil.parseIntOrDefault(request.getParameter("categoryId"), -1);
            BigDecimal price = ValidationUtil.parseBigDecimalOrDefault(request.getParameter("price"), BigDecimal.ZERO);
            int stockQuantity = ValidationUtil.parseIntOrDefault(request.getParameter("stockQuantity"), 0);
            String description = request.getParameter("description");
            String imageUrl = request.getParameter("imageUrl");
            boolean active = "true".equalsIgnoreCase(request.getParameter("active"));

            Product p = new Product();
            p.setCategoryId(categoryId);
            p.setName(name != null ? name.trim() : "Unnamed");
            p.setSlug(ValidationUtil.toSlug(p.getName()) + "-" + System.currentTimeMillis());
            p.setDescription(description != null ? description.trim() : "");
            p.setPrice(price);
            p.setStockQuantity(stockQuantity);
            p.setImageUrl(imageUrl != null && !imageUrl.trim().isEmpty() ? imageUrl.trim() : "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&q=80");
            p.setActive(active);

            adminService.createProduct(p);
            request.getSession().setAttribute("flashSuccess", "Product created successfully.");

        } else if ("update".equals(action)) {
            int id = ValidationUtil.parseIntOrDefault(request.getParameter("id"), -1);
            int categoryId = ValidationUtil.parseIntOrDefault(request.getParameter("categoryId"), -1);
            String name = request.getParameter("name");
            BigDecimal price = ValidationUtil.parseBigDecimalOrDefault(request.getParameter("price"), BigDecimal.ZERO);
            int stockQuantity = ValidationUtil.parseIntOrDefault(request.getParameter("stockQuantity"), 0);
            String description = request.getParameter("description");
            String imageUrl = request.getParameter("imageUrl");
            boolean active = "true".equalsIgnoreCase(request.getParameter("active"));

            productService.getProductById(id).ifPresent(p -> {
                p.setCategoryId(categoryId);
                p.setName(name != null ? name.trim() : p.getName());
                p.setPrice(price);
                p.setStockQuantity(stockQuantity);
                p.setDescription(description != null ? description.trim() : "");
                p.setImageUrl(imageUrl != null && !imageUrl.trim().isEmpty() ? imageUrl.trim() : p.getImageUrl());
                p.setActive(active);
                adminService.updateProduct(p);
            });
            request.getSession().setAttribute("flashSuccess", "Product updated.");

        } else if ("delete".equals(action)) {
            int id = ValidationUtil.parseIntOrDefault(request.getParameter("id"), -1);
            if (id > 0) {
                adminService.deleteProduct(id);
                request.getSession().setAttribute("flashSuccess", "Product deleted.");
            }
        }

        response.sendRedirect(request.getContextPath() + "/admin/products");
    }
}
