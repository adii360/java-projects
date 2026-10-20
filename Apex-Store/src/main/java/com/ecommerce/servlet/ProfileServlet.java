package com.ecommerce.servlet;

import com.ecommerce.dao.AddressDAO;
import com.ecommerce.model.Address;
import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import com.ecommerce.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "ProfileServlet", urlPatterns = "/profile")
public class ProfileServlet extends HttpServlet {
    private final UserService userService = new UserService();
    private final AddressDAO addressDAO = new AddressDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User sessionUser = (User) session.getAttribute("currentUser");

        Optional<User> freshUserOpt = userService.getUserById(sessionUser.getId());
        User user = freshUserOpt.orElse(sessionUser);
        session.setAttribute("currentUser", user);

        List<Address> addresses = addressDAO.findByUserId(user.getId());
        request.setAttribute("addresses", addresses);

        String updated = request.getParameter("updated");
        if ("profile".equals(updated)) {
            request.setAttribute("successMessage", "Profile updated successfully.");
        } else if ("password".equals(updated)) {
            request.setAttribute("successMessage", "Password updated successfully.");
        }

        request.getRequestDispatcher("/WEB-INF/views/customer/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValidToken(request)) {
            request.setAttribute("errorMessage", "Invalid security token. Please try again.");
            doGet(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        User sessionUser = (User) session.getAttribute("currentUser");
        String action = request.getParameter("action");

        if ("updateProfile".equals(action)) {
            String name = request.getParameter("name");
            String phone = request.getParameter("phone");

            if (name == null || name.trim().isEmpty()) {
                request.setAttribute("errorMessage", "Name cannot be empty.");
                doGet(request, response);
                return;
            }

            sessionUser.setName(name.trim());
            sessionUser.setPhone(phone != null ? phone.trim() : "");
            boolean ok = userService.updateProfile(sessionUser);

            if (ok) {
                session.setAttribute("currentUser", sessionUser);
                response.sendRedirect(request.getContextPath() + "/profile?updated=profile");
            } else {
                request.setAttribute("errorMessage", "Failed to update profile.");
                doGet(request, response);
            }
        } else if ("changePassword".equals(action)) {
            String currentPassword = request.getParameter("currentPassword");
            String newPassword = request.getParameter("newPassword");
            String confirmPassword = request.getParameter("confirmPassword");

            if (newPassword == null || newPassword.length() < 6) {
                request.setAttribute("errorMessage", "New password must be at least 6 characters.");
                doGet(request, response);
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                request.setAttribute("errorMessage", "New passwords do not match.");
                doGet(request, response);
                return;
            }

            boolean ok = userService.changePassword(sessionUser.getId(), currentPassword, newPassword);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/profile?updated=password");
            } else {
                request.setAttribute("errorMessage", "Current password is incorrect.");
                doGet(request, response);
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/profile");
        }
    }
}
