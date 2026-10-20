package com.ecommerce.servlet;

import com.ecommerce.model.Category;
import com.ecommerce.service.ProductService;
import com.ecommerce.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet(name = "ProductListServlet", urlPatterns = "/products")
public class ProductListServlet extends HttpServlet {
    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("q");
        String categoryIdStr = request.getParameter("category");
        String minPriceStr = request.getParameter("minPrice");
        String maxPriceStr = request.getParameter("maxPrice");
        String sortBy = request.getParameter("sortBy");
        String sortOrder = request.getParameter("sortOrder");
        String pageStr = request.getParameter("page");

        Integer categoryId = null;
        if (categoryIdStr != null && !categoryIdStr.trim().isEmpty()) {
            int parsed = ValidationUtil.parseIntOrDefault(categoryIdStr, -1);
            if (parsed > 0) {
                categoryId = parsed;
            }
        }

        BigDecimal minPrice = null;
        if (minPriceStr != null && !minPriceStr.trim().isEmpty()) {
            minPrice = ValidationUtil.parseBigDecimalOrDefault(minPriceStr, null);
        }

        BigDecimal maxPrice = null;
        if (maxPriceStr != null && !maxPriceStr.trim().isEmpty()) {
            maxPrice = ValidationUtil.parseBigDecimalOrDefault(maxPriceStr, null);
        }

        int page = ValidationUtil.parseIntOrDefault(pageStr, 1);
        int pageSize = 9;

        ProductService.ProductPageResult pageResult = productService.searchAndFilterProducts(
                keyword, categoryId, minPrice, maxPrice, sortBy, sortOrder, page, pageSize
        );

        List<Category> categories = productService.getAllCategories();
        Category activeCategory = null;
        if (categoryId != null) {
            for (Category c : categories) {
                if (c.getId() == categoryId) {
                    activeCategory = c;
                    break;
                }
            }
        }

        request.setAttribute("products", pageResult.products());
        request.setAttribute("totalCount", pageResult.totalCount());
        request.setAttribute("currentPage", pageResult.currentPage());
        request.setAttribute("totalPages", pageResult.totalPages());
        request.setAttribute("categories", categories);
        request.setAttribute("selectedCategory", categoryId);
        request.setAttribute("activeCategory", activeCategory);
        request.setAttribute("keyword", keyword);
        request.setAttribute("minPrice", minPrice);
        request.setAttribute("maxPrice", maxPrice);
        request.setAttribute("sortBy", sortBy != null ? sortBy : "p.created_at");
        request.setAttribute("sortOrder", sortOrder != null ? sortOrder : "DESC");

        request.getRequestDispatcher("/WEB-INF/views/catalog/list.jsp").forward(request, response);
    }
}
