package com.ecommerce.servlet;

import com.ecommerce.dao.ContactMessageDAO;
import com.ecommerce.model.ContactMessage;
import com.ecommerce.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "ContactServlet", urlPatterns = "/contact")
public class ContactServlet extends HttpServlet {
    private final ContactMessageDAO contactMessageDAO = new ContactMessageDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/public/contact.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValidToken(request)) {
            request.setAttribute("errorMessage", "Invalid security token. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/public/contact.jsp").forward(request, response);
            return;
        }

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String subject = request.getParameter("subject");
        String message = request.getParameter("message");

        if (name == null || email == null || message == null) {
            request.setAttribute("errorMessage", "Please fill in all required fields.");
            request.getRequestDispatcher("/WEB-INF/views/public/contact.jsp").forward(request, response);
            return;
        }

        ContactMessage msg = new ContactMessage();
        msg.setName(name.trim());
        msg.setEmail(email.trim());
        msg.setSubject(subject != null ? subject.trim() : "General Inquiry");
        msg.setMessage(message.trim());

        contactMessageDAO.createMessage(msg);
        request.setAttribute("successMessage", "Thank you! Your message has been received. Our support team will get back to you shortly.");
        request.getRequestDispatcher("/WEB-INF/views/public/contact.jsp").forward(request, response);
    }
}
