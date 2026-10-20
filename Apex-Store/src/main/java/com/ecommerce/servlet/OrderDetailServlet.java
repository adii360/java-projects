package com.ecommerce.servlet;

import com.ecommerce.model.Order;
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
import java.util.Optional;

@WebServlet(name = "OrderDetailServlet", urlPatterns = "/orders/detail")
public class OrderDetailServlet extends HttpServlet {
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
        Integer guestUserId = session != null ? (Integer) session.getAttribute("guestUserId") : null;

        if (currentUser == null && guestUserId == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return;
        }

        int orderId = ValidationUtil.parseIntOrDefault(request.getParameter("id"), -1);
        String orderNumber = request.getParameter("orderNumber");

        Optional<Order> orderOpt = Optional.empty();
        if (orderId > 0) {
            orderOpt = orderService.getOrderById(orderId);
        } else if (orderNumber != null && !orderNumber.trim().isEmpty()) {
            orderOpt = orderService.getOrderByNumber(orderNumber.trim());
        }

        if (orderOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
            return;
        }

        Order order = orderOpt.get();

        // Enforce authorization: only owner or ADMIN can view order details
        boolean isOwner = (currentUser != null && order.getUserId() == currentUser.getId())
                || (guestUserId != null && order.getUserId() == guestUserId);
        boolean isAdmin = currentUser != null && currentUser.getRole() == Role.ADMIN;

        if (!isAdmin && !isOwner) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to view this order.");
            return;
        }

        request.setAttribute("order", order);
        request.setAttribute("isNewlyPlaced", "true".equalsIgnoreCase(request.getParameter("placed")));
        request.setAttribute("isNewlyCancelled", "true".equalsIgnoreCase(request.getParameter("cancelled")));

        if (session != null) {
            if (session.getAttribute("flashSuccess") != null) {
                request.setAttribute("flashSuccess", session.getAttribute("flashSuccess"));
                session.removeAttribute("flashSuccess");
            }
            if (session.getAttribute("flashError") != null) {
                request.setAttribute("flashError", session.getAttribute("flashError"));
                session.removeAttribute("flashError");
            }
        }

        request.getRequestDispatcher("/WEB-INF/views/order/detail.jsp").forward(request, response);
    }
}
