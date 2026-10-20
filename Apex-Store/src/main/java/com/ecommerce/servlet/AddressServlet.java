package com.ecommerce.servlet;

import com.ecommerce.dao.AddressDAO;
import com.ecommerce.model.Address;
import com.ecommerce.model.User;
import com.ecommerce.util.CsrfUtil;
import com.ecommerce.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AddressServlet", urlPatterns = "/addresses")
public class AddressServlet extends HttpServlet {
    private final AddressDAO addressDAO = new AddressDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        List<Address> addresses = addressDAO.findByUserId(currentUser.getId());
        request.setAttribute("addresses", addresses);

        request.getRequestDispatcher("/WEB-INF/views/customer/addresses.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValidToken(request)) {
            response.sendRedirect(request.getContextPath() + "/addresses?error=csrf");
            return;
        }

        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");
        String action = request.getParameter("action");

        if ("add".equals(action)) {
            String recipientName = request.getParameter("recipientName");
            String phone = request.getParameter("phone");
            String addressLine = request.getParameter("addressLine");
            String city = request.getParameter("city");
            String state = request.getParameter("state");
            String postalCode = request.getParameter("postalCode");
            String country = request.getParameter("country");
            boolean isDefault = "true".equalsIgnoreCase(request.getParameter("isDefault"));

            if (recipientName == null || addressLine == null || city == null || state == null || postalCode == null) {
                session.setAttribute("flashError", "Please fill in all address fields.");
                response.sendRedirect(request.getContextPath() + "/addresses");
                return;
            }

            if (isDefault) {
                addressDAO.clearDefaultAddress(currentUser.getId());
            }

            Address addr = new Address();
            addr.setUserId(currentUser.getId());
            addr.setRecipientName(recipientName.trim());
            addr.setPhone(phone != null ? phone.trim() : "");
            addr.setAddressLine(addressLine.trim());
            addr.setCity(city.trim());
            addr.setState(state.trim());
            addr.setPostalCode(postalCode.trim());
            addr.setCountry(country != null && !country.trim().isEmpty() ? country.trim() : "United States");
            addr.setDefault(isDefault);

            addressDAO.createAddress(addr);
            session.setAttribute("flashSuccess", "Address added successfully.");

        } else if ("delete".equals(action)) {
            int addressId = ValidationUtil.parseIntOrDefault(request.getParameter("addressId"), -1);
            if (addressId > 0) {
                addressDAO.deleteAddress(addressId, currentUser.getId());
                session.setAttribute("flashSuccess", "Address deleted.");
            }
        } else if ("setDefault".equals(action)) {
            int addressId = ValidationUtil.parseIntOrDefault(request.getParameter("addressId"), -1);
            if (addressId > 0) {
                addressDAO.clearDefaultAddress(currentUser.getId());
                addressDAO.findByIdAndUserId(addressId, currentUser.getId()).ifPresent(a -> {
                    a.setDefault(true);
                    addressDAO.updateAddress(a);
                });
                session.setAttribute("flashSuccess", "Default address updated.");
            }
        }

        String redirect = request.getParameter("redirect");
        if ("checkout".equals(redirect)) {
            response.sendRedirect(request.getContextPath() + "/checkout");
        } else {
            response.sendRedirect(request.getContextPath() + "/addresses");
        }
    }
}
