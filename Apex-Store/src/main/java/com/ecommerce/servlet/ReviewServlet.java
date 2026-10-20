package com.ecommerce.servlet;

import com.ecommerce.model.User;
import com.ecommerce.service.ProductService;
import com.ecommerce.util.CsrfUtil;
import com.ecommerce.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "ReviewServlet", urlPatterns = "/reviews")
public class ReviewServlet extends HttpServlet {
    private final ProductService productService = new ProductService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValidToken(request)) {
            response.sendRedirect(request.getContextPath() + "/products?error=csrf");
            return;
        }

        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        int productId = ValidationUtil.parseIntOrDefault(request.getParameter("productId"), -1);
        int rating = ValidationUtil.parseIntOrDefault(request.getParameter("rating"), 5);
        String comment = request.getParameter("comment");

        if (productId <= 0) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        if (!productService.canUserReview(currentUser.getId(), productId)) {
            session.setAttribute("flashError", "Only customers who have purchased this product can leave a review.");
            response.sendRedirect(request.getContextPath() + "/product?id=" + productId);
            return;
        }

        boolean ok = productService.addProductReview(currentUser.getId(), productId, rating, comment);
        if (ok) {
            session.setAttribute("flashSuccess", "Thank you! Your review has been published.");
        } else {
            session.setAttribute("flashError", "Failed to submit review.");
        }

        response.sendRedirect(request.getContextPath() + "/product?id=" + productId);
    }
}
