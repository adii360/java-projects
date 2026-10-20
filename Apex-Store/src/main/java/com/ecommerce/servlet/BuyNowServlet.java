package com.ecommerce.servlet;

import com.ecommerce.dao.AddressDAO;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.UserDAO;
import com.ecommerce.model.Address;
import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.service.OrderService;
import com.ecommerce.util.CsrfUtil;
import com.ecommerce.util.PasswordUtil;
import com.ecommerce.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@WebServlet(name = "BuyNowServlet", urlPatterns = {"/buynow", "/orders/buynow", "/buy-now"})
public class BuyNowServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(BuyNowServlet.class.getName());

    private final ProductDAO productDAO = new ProductDAO();
    private final AddressDAO addressDAO = new AddressDAO();
    private final UserDAO userDAO = new UserDAO();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int productId = ValidationUtil.parseIntOrDefault(request.getParameter("productId"), -1);
        if (productId <= 0) {
            productId = ValidationUtil.parseIntOrDefault(request.getParameter("id"), -1);
        }

        if (productId <= 0) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        Optional<Product> prodOpt = productDAO.findById(productId);
        if (prodOpt.isEmpty() || !prodOpt.get().isActive()) {
            HttpSession session = request.getSession(true);
            session.setAttribute("flashError", "The requested product is not available.");
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        Product product = prodOpt.get();
        int quantity = Math.max(1, ValidationUtil.parseIntOrDefault(request.getParameter("quantity"), 1));

        HttpSession session = request.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;

        List<Address> addresses = List.of();
        if (currentUser != null) {
            addresses = addressDAO.findByUserId(currentUser.getId());
        }

        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("999.00")) >= 0 ? BigDecimal.ZERO : new BigDecimal("99.00");
        BigDecimal total = subtotal.add(shippingFee);

        request.setAttribute("product", product);
        request.setAttribute("quantity", quantity);
        request.setAttribute("addresses", addresses);
        request.setAttribute("subtotal", subtotal);
        request.setAttribute("shippingFee", shippingFee);
        request.setAttribute("total", total);

        request.getRequestDispatcher("/WEB-INF/views/order/buynow.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);

        if (!CsrfUtil.isValidToken(request)) {
            session.setAttribute("flashError", "Security session expired. Please try again.");
            String redirectUrl = request.getHeader("Referer");
            if (redirectUrl != null && !redirectUrl.isBlank()) {
                response.sendRedirect(redirectUrl);
            } else {
                response.sendRedirect(request.getContextPath() + "/products");
            }
            return;
        }

        int productId = ValidationUtil.parseIntOrDefault(request.getParameter("productId"), -1);
        if (productId <= 0) {
            session.setAttribute("flashError", "Invalid product selected.");
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        int quantity = Math.max(1, ValidationUtil.parseIntOrDefault(request.getParameter("quantity"), 1));

        // 1. Resolve User (Logged-in or Guest Customer)
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            String guestEmail = request.getParameter("email");
            if (guestEmail == null || guestEmail.trim().isEmpty()) {
                guestEmail = request.getParameter("guestEmail");
            }

            String guestName = request.getParameter("recipientName");
            if (guestName == null || guestName.trim().isEmpty()) {
                guestName = request.getParameter("newRecipientName");
            }
            if (guestName == null || guestName.trim().isEmpty()) {
                guestName = "Valued Customer";
            }

            if (guestEmail == null || guestEmail.trim().isEmpty()) {
                guestEmail = "customer_" + System.currentTimeMillis() + "@apexstore.com";
            } else {
                guestEmail = guestEmail.trim().toLowerCase();
            }

            Optional<User> existingUser = userDAO.findByEmail(guestEmail);
            if (existingUser.isPresent()) {
                currentUser = existingUser.get();
            } else {
                User guestUser = new User();
                guestUser.setName(guestName.trim());
                guestUser.setEmail(guestEmail);
                guestUser.setPasswordHash("GUEST_" + PasswordUtil.hashPassword(UUID.randomUUID().toString()));
                guestUser.setPhone(request.getParameter("phone") != null ? request.getParameter("phone").trim() : "+1-555-0100");
                guestUser.setRole(Role.CUSTOMER);
                guestUser.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                guestUser.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

                userDAO.createUser(guestUser);
                currentUser = guestUser;
            }
            // Auto-login so order confirmation and tracking are accessible
            session.setAttribute("currentUser", currentUser);
        }

        // 2. Resolve Shipping Address (Location)
        int addressId = ValidationUtil.parseIntOrDefault(request.getParameter("addressId"), -1);

        if (addressId <= 0) {
            String recipientName = request.getParameter("recipientName");
            if (recipientName == null || recipientName.trim().isEmpty()) {
                recipientName = request.getParameter("newRecipientName");
            }
            if (recipientName == null || recipientName.trim().isEmpty()) {
                recipientName = currentUser.getName();
            }

            String phone = request.getParameter("phone");
            if (phone == null || phone.trim().isEmpty()) {
                phone = request.getParameter("newPhone");
            }

            String addressLine = request.getParameter("addressLine");
            if (addressLine == null || addressLine.trim().isEmpty()) {
                addressLine = request.getParameter("newAddressLine");
            }

            String city = request.getParameter("city");
            if (city == null || city.trim().isEmpty()) {
                city = request.getParameter("newCity");
            }

            String state = request.getParameter("state");
            if (state == null || state.trim().isEmpty()) {
                state = request.getParameter("newState");
            }

            String postalCode = request.getParameter("postalCode");
            if (postalCode == null || postalCode.trim().isEmpty()) {
                postalCode = request.getParameter("newPostalCode");
            }

            String country = request.getParameter("country");
            if (country == null || country.trim().isEmpty()) {
                country = request.getParameter("newCountry");
            }
            if (country == null || country.trim().isEmpty()) {
                country = "United States";
            }

            // Validate mandatory address inputs
            if (addressLine == null || addressLine.trim().isEmpty() ||
                city == null || city.trim().isEmpty() ||
                state == null || state.trim().isEmpty() ||
                postalCode == null || postalCode.trim().isEmpty()) {

                session.setAttribute("flashError", "Please provide complete delivery location details (Street, City, State, Postal Code).");
                response.sendRedirect(request.getContextPath() + "/buynow?productId=" + productId + "&quantity=" + quantity);
                return;
            }

            Address addr = new Address();
            addr.setUserId(currentUser.getId());
            addr.setRecipientName(recipientName != null ? recipientName.trim() : "Valued Customer");
            addr.setPhone(phone != null ? phone.trim() : "");
            addr.setAddressLine(addressLine.trim());
            addr.setCity(city.trim());
            addr.setState(state.trim());
            addr.setPostalCode(postalCode.trim());
            addr.setCountry(country.trim());
            addr.setDefault(false);

            if (addressDAO.createAddress(addr)) {
                addressId = addr.getId();
            }
        }

        if (addressId <= 0) {
            session.setAttribute("flashError", "Failed to save or locate delivery address. Please check your location details.");
            response.sendRedirect(request.getContextPath() + "/buynow?productId=" + productId + "&quantity=" + quantity);
            return;
        }

        // 3. Payment Method: COD vs ONLINE
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

        String couponCode = request.getParameter("couponCode");

        // 4. Place the Direct Order
        OrderService.PlaceOrderResult result = orderService.placeDirectOrder(
                currentUser.getId(), addressId, productId, quantity, couponCode, paymentMethod
        );

        if (result.success()) {
            String methodDesc = paymentMethod.startsWith("ONLINE") ? "Online Payment" : "Cash on Delivery (COD)";
            session.setAttribute("flashSuccess", "Order placed successfully via " + methodDesc + "!");
            response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + result.order().getId() + "&placed=true");
        } else {
            session.setAttribute("flashError", result.message());
            response.sendRedirect(request.getContextPath() + "/buynow?productId=" + productId + "&quantity=" + quantity);
        }
    }
}
