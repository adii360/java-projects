package com.ecommerce.servlet;

import com.ecommerce.dao.WishlistDAO;
import com.ecommerce.model.User;
import com.ecommerce.util.CsrfUtil;
import com.ecommerce.util.ValidationUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "AddToWishlistServlet", urlPatterns = "/wishlist/add")
public class AddToWishlistServlet extends HttpServlet {
    private final WishlistDAO wishlistDAO = new WishlistDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/wishlist");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!CsrfUtil.isValidToken(request)) {
            response.sendRedirect(request.getContextPath() + "/wishlist?error=csrf");
            return;
        }

        int productId = ValidationUtil.parseIntOrDefault(request.getParameter("productId"), -1);

        HttpSession session = request.getSession(true);
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            if (productId > 0) {
                session.setAttribute("pendingAddToWishlistProductId", productId);
                session.setAttribute("redirectAfterLogin", request.getContextPath() + "/wishlist");
            }
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return;
        }
        if (productId > 0) {
            wishlistDAO.addItem(currentUser.getId(), productId);
            session.setAttribute("flashSuccess", "Added to wishlist.");
        }

        String referer = request.getHeader("Referer");
        if (referer != null) {
            response.sendRedirect(referer);
        } else {
            response.sendRedirect(request.getContextPath() + "/wishlist");
        }
    }
}
