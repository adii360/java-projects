package com.ecommerce.servlet;

import com.ecommerce.model.Category;
import com.ecommerce.service.AdminService;
import com.ecommerce.util.CsrfUtil;
import com.ecommerce.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AdminCategoryServlet", urlPatterns = "/admin/categories")
public class AdminCategoryServlet extends HttpServlet {
    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Category> categories = adminService.getAllCategories();
        request.setAttribute("categories", categories);

        String editIdStr = request.getParameter("editId");
        if (editIdStr != null) {
            int editId = ValidationUtil.parseIntOrDefault(editIdStr, -1);
            categories.stream()
                    .filter(c -> c.getId() == editId)
                    .findFirst()
                    .ifPresent(c -> request.setAttribute("editCategory", c));
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/categories.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValidToken(request)) {
            response.sendRedirect(request.getContextPath() + "/admin/categories?error=csrf");
            return;
        }

        String action = request.getParameter("action");

        if ("create".equals(action)) {
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String imageUrl = request.getParameter("imageUrl");

            if (name != null && !name.trim().isEmpty()) {
                Category c = new Category();
                c.setName(name.trim());
                c.setSlug(ValidationUtil.toSlug(name.trim()));
                c.setDescription(description != null ? description.trim() : "");
                c.setImageUrl(imageUrl != null ? imageUrl.trim() : "");
                adminService.createCategory(c);
                request.getSession().setAttribute("flashSuccess", "Category created.");
            }

        } else if ("update".equals(action)) {
            int id = ValidationUtil.parseIntOrDefault(request.getParameter("id"), -1);
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String imageUrl = request.getParameter("imageUrl");

            if (id > 0 && name != null && !name.trim().isEmpty()) {
                Category c = new Category();
                c.setId(id);
                c.setName(name.trim());
                c.setSlug(ValidationUtil.toSlug(name.trim()));
                c.setDescription(description != null ? description.trim() : "");
                c.setImageUrl(imageUrl != null ? imageUrl.trim() : "");
                adminService.updateCategory(c);
                request.getSession().setAttribute("flashSuccess", "Category updated.");
            }

        } else if ("delete".equals(action)) {
            int id = ValidationUtil.parseIntOrDefault(request.getParameter("id"), -1);
            if (id > 0) {
                boolean deleted = adminService.deleteCategory(id);
                if (deleted) {
                    request.getSession().setAttribute("flashSuccess", "Category deleted.");
                } else {
                    request.getSession().setAttribute("flashError", "Cannot delete category with associated products.");
                }
            }
        }

        response.sendRedirect(request.getContextPath() + "/admin/categories");
    }
}
