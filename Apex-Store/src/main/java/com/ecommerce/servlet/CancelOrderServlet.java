package com.ecommerce.servlet;

import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.service.OrderService;
import com.ecommerce.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "CancelOrderServlet", urlPatterns = "/orders/cancel")
public class CancelOrderServlet extends HttpServlet {
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int orderId = ValidationUtil.parseIntOrDefault(request.getParameter("orderId"), -1);
        if (orderId > 0) {
            response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + orderId);
        } else {
            response.sendRedirect(request.getContextPath() + "/orders");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
        Integer guestUserId = session != null ? (Integer) session.getAttribute("guestUserId") : null;

        if (currentUser == null && guestUserId == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return;
        }

        int effectiveUserId = currentUser != null ? currentUser.getId() : guestUserId;
        boolean isAdmin = currentUser != null && currentUser.getRole() == Role.ADMIN;

        int orderId = ValidationUtil.parseIntOrDefault(request.getParameter("orderId"), -1);
        if (orderId <= 0) {
            if (session != null) {
                session.setAttribute("flashError", "Invalid order specified for cancellation.");
            }
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }

        OrderService.CancelOrderResult result = orderService.cancelOrder(orderId, effectiveUserId, isAdmin);

        if (session != null) {
            if (result.success()) {
                session.setAttribute("flashSuccess", result.message());
            } else {
                session.setAttribute("flashError", result.message());
            }
        }

        response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + orderId + (result.success() ? "&cancelled=true" : ""));
    }
}
