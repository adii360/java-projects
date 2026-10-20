package com.ecommerce.servlet;

import com.ecommerce.model.User;
import com.ecommerce.service.CartService;
import com.ecommerce.util.CsrfUtil;
import com.ecommerce.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet(name = "UpdateCartServlet", urlPatterns = "/cart/update")
public class UpdateCartServlet extends HttpServlet {
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/cart");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValidToken(request)) {
            response.sendRedirect(request.getContextPath() + "/cart?error=csrf");
            return;
        }

        HttpSession session = request.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return;
        }
        String action = request.getParameter("action");

        if ("applyCoupon".equals(action)) {
            String code = request.getParameter("couponCode");
            BigDecimal subtotal = cartService.getCartSubtotal(currentUser.getId());
            CartService.CouponResult res = cartService.validateCoupon(code, subtotal);
            if (res.valid()) {
                session.setAttribute("appliedCouponCode", res.coupon().getCode());
                session.setAttribute("flashSuccess", res.message());
            } else {
                session.removeAttribute("appliedCouponCode");
                session.setAttribute("flashError", res.message());
            }
        } else if ("removeCoupon".equals(action)) {
            session.removeAttribute("appliedCouponCode");
            session.setAttribute("flashSuccess", "Coupon removed.");
        } else {
            // Quantity update
            int cartItemId = ValidationUtil.parseIntOrDefault(request.getParameter("cartItemId"), -1);
            int quantity = ValidationUtil.parseIntOrDefault(request.getParameter("quantity"), 1);

            if (cartItemId > 0) {
                cartService.updateQuantity(currentUser.getId(), cartItemId, quantity);
            }
        }

        response.sendRedirect(request.getContextPath() + "/cart");
    }
}
