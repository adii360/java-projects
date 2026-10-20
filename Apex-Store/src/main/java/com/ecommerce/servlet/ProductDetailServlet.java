package com.ecommerce.servlet;

import com.ecommerce.dao.WishlistDAO;
import com.ecommerce.model.Product;
import com.ecommerce.model.Review;
import com.ecommerce.model.User;
import com.ecommerce.service.ProductService;
import com.ecommerce.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "ProductDetailServlet", urlPatterns = {"/product", "/product/*"})
public class ProductDetailServlet extends HttpServlet {
    private final ProductService productService = new ProductService();
    private final WishlistDAO wishlistDAO = new WishlistDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            idStr = request.getParameter("productId");
        }
        String slug = request.getParameter("slug");

        // Support pathInfo (e.g. /product/1 or /product/probook-ultra-16-m3)
        String pathInfo = request.getPathInfo();
        if ((idStr == null || idStr.trim().isEmpty()) && (slug == null || slug.trim().isEmpty()) && pathInfo != null && pathInfo.length() > 1) {
            String pathParam = pathInfo.substring(1).trim();
            if (pathParam.matches("\\d+")) {
                idStr = pathParam;
            } else {
                slug = pathParam;
            }
        }

        // If no ID or slug provided, redirect to catalog instead of throwing 404
        if ((idStr == null || idStr.trim().isEmpty()) && (slug == null || slug.trim().isEmpty())) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        Optional<Product> productOpt = Optional.empty();
        if (idStr != null && !idStr.trim().isEmpty()) {
            int id = ValidationUtil.parseIntOrDefault(idStr.trim(), -1);
            if (id > 0) {
                productOpt = productService.getProductById(id);
            }
        } else if (slug != null && !slug.trim().isEmpty()) {
            productOpt = productService.getProductBySlug(slug.trim());
        }

        if (productOpt.isEmpty()) {
            request.setAttribute("errorMessage", "The requested product could not be found.");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.getRequestDispatcher("/WEB-INF/views/error/404.jsp").forward(request, response);
            return;
        }

        Product product = productOpt.get();
        List<Review> reviews = productService.getProductReviews(product.getId());

        HttpSession session = request.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
        boolean canReview = false;
        boolean inWishlist = false;

        if (currentUser != null) {
            canReview = productService.canUserReview(currentUser.getId(), product.getId());
            inWishlist = wishlistDAO.isInWishlist(currentUser.getId(), product.getId());
        }

        request.setAttribute("product", product);
        request.setAttribute("reviews", reviews);
        request.setAttribute("canReview", canReview);
        request.setAttribute("inWishlist", inWishlist);

        request.getRequestDispatcher("/WEB-INF/views/catalog/detail.jsp").forward(request, response);
    }
}
