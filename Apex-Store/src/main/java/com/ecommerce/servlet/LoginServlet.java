package com.ecommerce.servlet;

import com.ecommerce.dao.WishlistDAO;
import com.ecommerce.model.Role;
import com.ecommerce.service.CartService;
import com.ecommerce.service.UserService;
import com.ecommerce.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
    private final UserService userService = new UserService();
    private final CartService cartService = new CartService();
    private final WishlistDAO wishlistDAO = new WishlistDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        String errorParam = request.getParameter("error");
        if ("auth_required".equals(errorParam)) {
            request.setAttribute("errorMessage", "Please log in to continue.");
        } else if ("admin_required".equals(errorParam)) {
            request.setAttribute("errorMessage", "Administrator privileges are required for that page.");
        }

        if (session != null && session.getAttribute("flashSuccess") != null) {
            request.setAttribute("successMessage", session.getAttribute("flashSuccess"));
            session.removeAttribute("flashSuccess");
        }

        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValidToken(request)) {
            request.setAttribute("errorMessage", "Invalid security token. Please refresh and try again.");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        UserService.AuthResult result = userService.login(email, password);

        if (result.success()) {
            HttpSession existingSession = request.getSession(false);
            Integer pendingProdId = existingSession != null ? (Integer) existingSession.getAttribute("pendingAddToCartProductId") : null;
            Integer pendingQty = existingSession != null ? (Integer) existingSession.getAttribute("pendingAddToCartQuantity") : null;
            Integer pendingWishlistId = existingSession != null ? (Integer) existingSession.getAttribute("pendingAddToWishlistProductId") : null;
            String redirectUrl = existingSession != null ? (String) existingSession.getAttribute("redirectAfterLogin") : null;

            // Protect against session fixation: rotate session ID
            request.changeSessionId();
            HttpSession session = request.getSession(true);
            session.setAttribute("currentUser", result.user());

            if (pendingProdId != null) session.setAttribute("pendingAddToCartProductId", pendingProdId);
            if (pendingQty != null) session.setAttribute("pendingAddToCartQuantity", pendingQty);
            if (pendingWishlistId != null) session.setAttribute("pendingAddToWishlistProductId", pendingWishlistId);
            if (redirectUrl != null) session.setAttribute("redirectAfterLogin", redirectUrl);

            // Fulfill pending add to cart if user clicked Add to Cart before login
            if (pendingProdId != null && pendingProdId > 0) {
                int qty = (pendingQty != null && pendingQty > 0) ? pendingQty : 1;
                cartService.addToCart(result.user().getId(), pendingProdId, qty);
                session.removeAttribute("pendingAddToCartProductId");
                session.removeAttribute("pendingAddToCartQuantity");
                session.setAttribute("flashSuccess", "Item successfully added to your cart!");
            }

            // Fulfill pending wishlist item if user clicked Add to Wishlist before login
            if (pendingWishlistId != null && pendingWishlistId > 0) {
                wishlistDAO.addItem(result.user().getId(), pendingWishlistId);
                session.removeAttribute("pendingAddToWishlistProductId");
                if (session.getAttribute("flashSuccess") == null) {
                    session.setAttribute("flashSuccess", "Item added to your wishlist!");
                }
            }

            // Check if there was a saved URL to redirect to
            redirectUrl = (String) session.getAttribute("redirectAfterLogin");
            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                session.removeAttribute("redirectAfterLogin");
                // Safety check: ensure we never redirect to a POST-only action URL
                if (redirectUrl.endsWith("/cart/add") || redirectUrl.endsWith("/cart/update") || redirectUrl.endsWith("/cart/remove")) {
                    redirectUrl = request.getContextPath() + "/cart";
                } else if (redirectUrl.endsWith("/wishlist/add") || redirectUrl.endsWith("/wishlist/remove")) {
                    redirectUrl = request.getContextPath() + "/wishlist";
                } else if (redirectUrl.endsWith("/orders/place") || redirectUrl.endsWith("/orders/cancel")) {
                    redirectUrl = request.getContextPath() + "/orders";
                }
                response.sendRedirect(redirectUrl);
                return;
            }

            // Redirect admin to dashboard, customer to home
            if (result.user().getRole() == Role.ADMIN) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/");
            }
        } else {
            request.setAttribute("errorMessage", result.message());
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
        }
    }
}
