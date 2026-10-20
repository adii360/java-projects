package com.ecommerce.servlet;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "HomeServlet", urlPatterns = {"", "/home"})
public class HomeServlet extends HttpServlet {
    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Category> categories = productService.getAllCategories();
        List<Product> featuredProducts = productService.getFeaturedProducts(12);

        request.setAttribute("categories", categories);
        request.setAttribute("featuredProducts", featuredProducts);

        request.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(request, response);
    }
}
