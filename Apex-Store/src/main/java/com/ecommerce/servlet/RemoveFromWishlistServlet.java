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

@WebServlet(name = "RemoveFromWishlistServlet", urlPatterns = "/wishlist/remove")
public class RemoveFromWishlistServlet extends HttpServlet {
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

        HttpSession session = request.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return;
        }

        int productId = ValidationUtil.parseIntOrDefault(request.getParameter("productId"), -1);
        if (productId > 0) {
            wishlistDAO.removeItem(currentUser.getId(), productId);
            session.setAttribute("flashSuccess", "Removed from wishlist.");
        }

        response.sendRedirect(request.getContextPath() + "/wishlist");
    }
}
