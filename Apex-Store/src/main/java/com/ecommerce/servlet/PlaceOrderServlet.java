package com.ecommerce.servlet;

import com.ecommerce.dao.AddressDAO;
import com.ecommerce.model.Address;
import com.ecommerce.model.User;
import com.ecommerce.service.OrderService;
import com.ecommerce.util.CsrfUtil;
import com.ecommerce.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "PlaceOrderServlet", urlPatterns = "/orders/place")
public class PlaceOrderServlet extends HttpServlet {
    private final OrderService orderService = new OrderService();
    private final AddressDAO addressDAO = new AddressDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/checkout");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValidToken(request)) {
            response.sendRedirect(request.getContextPath() + "/checkout?error=csrf");
            return;
        }

        HttpSession session = request.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return;
        }

        int addressId = ValidationUtil.parseIntOrDefault(request.getParameter("addressId"), -1);

        // If new address was submitted directly during checkout
        if (addressId <= 0) {
            String recipientName = request.getParameter("newRecipientName");
            String addressLine = request.getParameter("newAddressLine");
            String city = request.getParameter("newCity");
            String state = request.getParameter("newState");
            String postalCode = request.getParameter("newPostalCode");
            String phone = request.getParameter("newPhone");
            String country = request.getParameter("newCountry");

            if (recipientName != null && !recipientName.trim().isEmpty() &&
                addressLine != null && !addressLine.trim().isEmpty() &&
                city != null && !city.trim().isEmpty() &&
                state != null && !state.trim().isEmpty() &&
                postalCode != null && !postalCode.trim().isEmpty()) {

                Address addr = new Address();
                addr.setUserId(currentUser.getId());
                addr.setRecipientName(recipientName.trim());
                addr.setPhone(phone != null ? phone.trim() : "");
                addr.setAddressLine(addressLine.trim());
                addr.setCity(city.trim());
                addr.setState(state.trim());
                addr.setPostalCode(postalCode.trim());
                addr.setCountry(country != null && !country.trim().isEmpty() ? country.trim() : "United States");
                addr.setDefault(false);

                if (addressDAO.createAddress(addr)) {
                    addressId = addr.getId();
                }
            }
        }

        if (addressId <= 0) {
            session.setAttribute("flashError", "Please select or provide a valid shipping address.");
            response.sendRedirect(request.getContextPath() + "/checkout");
            return;
        }

        String couponCode = (String) session.getAttribute("appliedCouponCode");
        String paymentMethod = request.getParameter("paymentMethod");
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "COD";
        } else {
            paymentMethod = paymentMethod.trim().toUpperCase();
        }

        String onlineType = request.getParameter("onlinePaymentType");
        if (paymentMethod.startsWith("ONLINE") && onlineType != null && !onlineType.trim().isEmpty()) {
            paymentMethod = "ONLINE_" + onlineType.trim().toUpperCase();
        }

        OrderService.PlaceOrderResult result = orderService.placeOrder(
                currentUser.getId(), addressId, couponCode, paymentMethod
        );

        if (result.success()) {
            session.removeAttribute("appliedCouponCode");
            String methodDesc = paymentMethod.startsWith("ONLINE") ? "Online Payment" : "Cash on Delivery (COD)";
            session.setAttribute("flashSuccess", "Your order has been placed successfully via " + methodDesc + "!");
            response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + result.order().getId() + "&placed=true");
        } else {
            session.setAttribute("flashError", result.message());
            response.sendRedirect(request.getContextPath() + "/checkout");
        }
    }
}
