package com.ecommerce.servlet;

import com.ecommerce.model.Product;
import com.ecommerce.service.AdminService;
import com.ecommerce.util.CsrfUtil;
import com.ecommerce.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AdminInventoryServlet", urlPatterns = "/admin/inventory")
public class AdminInventoryServlet extends HttpServlet {
    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Product> products = adminService.getAllProducts(0, 100);
        request.setAttribute("products", products);

        request.getRequestDispatcher("/WEB-INF/views/admin/inventory.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValidToken(request)) {
            response.sendRedirect(request.getContextPath() + "/admin/inventory?error=csrf");
            return;
        }

        int productId = ValidationUtil.parseIntOrDefault(request.getParameter("productId"), -1);
        int quantity = ValidationUtil.parseIntOrDefault(request.getParameter("quantity"), -1);

        if (productId > 0 && quantity >= 0) {
            adminService.updateStock(productId, quantity);
            request.getSession().setAttribute("flashSuccess", "Inventory updated for product #" + productId);
        }

        response.sendRedirect(request.getContextPath() + "/admin/inventory");
    }
}
