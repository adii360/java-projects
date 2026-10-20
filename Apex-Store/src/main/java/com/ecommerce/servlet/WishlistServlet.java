package com.ecommerce.servlet;

import com.ecommerce.dao.WishlistDAO;
import com.ecommerce.model.User;
import com.ecommerce.model.WishlistItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "WishlistServlet", urlPatterns = "/wishlist")
public class WishlistServlet extends HttpServlet {
    private final WishlistDAO wishlistDAO = new WishlistDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        List<WishlistItem> items = wishlistDAO.getWishlistItems(currentUser.getId());
        request.setAttribute("wishlistItems", items);

        request.getRequestDispatcher("/WEB-INF/views/customer/wishlist.jsp").forward(request, response);
    }
}
