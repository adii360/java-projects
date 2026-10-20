package com.ecommerce.servlet;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.PaymentStatus;
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

@WebServlet(name = "AdminOrderServlet", urlPatterns = "/admin/orders")
public class AdminOrderServlet extends HttpServlet {
    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = ValidationUtil.parseIntOrDefault(request.getParameter("page"), 1);
        int pageSize = 15;
        int offset = (page - 1) * pageSize;

        List<Order> orders = adminService.getAllOrders(offset, pageSize);
        int totalOrders = adminService.getTotalOrderCount();
        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);

        request.setAttribute("orders", orders);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.getRequestDispatcher("/WEB-INF/views/admin/orders.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValidToken(request)) {
            response.sendRedirect(request.getContextPath() + "/admin/orders?error=csrf");
            return;
        }

        int orderId = ValidationUtil.parseIntOrDefault(request.getParameter("orderId"), -1);
        String orderStatusStr = request.getParameter("orderStatus");
        String paymentStatusStr = request.getParameter("paymentStatus");

        if (orderId > 0 && orderStatusStr != null && paymentStatusStr != null) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(orderStatusStr);
                PaymentStatus paymentStatus = PaymentStatus.valueOf(paymentStatusStr);
                adminService.updateOrderStatus(orderId, orderStatus, paymentStatus);
                request.getSession().setAttribute("flashSuccess", "Order #" + orderId + " updated.");
            } catch (IllegalArgumentException e) {
                request.getSession().setAttribute("flashError", "Invalid status values.");
            }
        }

        response.sendRedirect(request.getContextPath() + "/admin/orders");
    }
}
