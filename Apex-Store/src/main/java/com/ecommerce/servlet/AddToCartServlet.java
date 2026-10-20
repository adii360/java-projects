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

@WebServlet(name = "AddToCartServlet", urlPatterns = "/cart/add")
public class AddToCartServlet extends HttpServlet {
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // If accessed via GET (e.g. page refresh, back navigation, or bookmark),
        // redirect gracefully to /cart instead of crashing with HTTP 405 Method Not Allowed
        response.sendRedirect(request.getContextPath() + "/cart");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValidToken(request)) {
            response.sendRedirect(request.getContextPath() + "/cart?error=csrf");
            return;
        }

        int productId = ValidationUtil.parseIntOrDefault(request.getParameter("productId"), -1);
        int quantity = ValidationUtil.parseIntOrDefault(request.getParameter("quantity"), 1);

        HttpSession session = request.getSession(true);
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            if (productId > 0) {
                session.setAttribute("pendingAddToCartProductId", productId);
                session.setAttribute("pendingAddToCartQuantity", quantity > 0 ? quantity : 1);
                session.setAttribute("redirectAfterLogin", request.getContextPath() + "/cart");
            }
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return;
        }

        if (productId <= 0 || quantity <= 0) {
            response.sendRedirect(request.getContextPath() + "/products?error=invalid_item");
            return;
        }

        CartService.CartOpResult result = cartService.addToCart(currentUser.getId(), productId, quantity);

        if (result.success()) {
            session.setAttribute("flashSuccess", result.message());
        } else {
            session.setAttribute("flashError", result.message());
        }

        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/product")) {
            response.sendRedirect(referer);
        } else {
            response.sendRedirect(request.getContextPath() + "/cart");
        }
    }
}
