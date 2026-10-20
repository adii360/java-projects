package com.ecommerce.servlet;

import com.ecommerce.dao.AddressDAO;
import com.ecommerce.model.Address;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.User;
import com.ecommerce.service.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet(name = "CheckoutServlet", urlPatterns = "/checkout")
public class CheckoutServlet extends HttpServlet {
    private final CartService cartService = new CartService();
    private final AddressDAO addressDAO = new AddressDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        List<CartItem> cartItems = cartService.getCart(currentUser.getId());
        if (cartItems.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        List<Address> addresses = addressDAO.findByUserId(currentUser.getId());
        BigDecimal subtotal = cartService.getCartSubtotal(currentUser.getId());

        String couponCode = (String) session.getAttribute("appliedCouponCode");
        BigDecimal discount = BigDecimal.ZERO;
        if (couponCode != null && !couponCode.isEmpty()) {
            CartService.CouponResult cRes = cartService.validateCoupon(couponCode, subtotal);
            if (cRes.valid()) {
                discount = cRes.discountAmount();
            }
        }

        BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("999.00")) >= 0 ? BigDecimal.ZERO : new BigDecimal("99.00");
        BigDecimal total = subtotal.subtract(discount).add(shippingFee).max(BigDecimal.ZERO);

        request.setAttribute("cartItems", cartItems);
        request.setAttribute("addresses", addresses);
        request.setAttribute("subtotal", subtotal);
        request.setAttribute("discount", discount);
        request.setAttribute("shippingFee", shippingFee);
        request.setAttribute("total", total);
        request.setAttribute("couponCode", couponCode);

        request.getRequestDispatcher("/WEB-INF/views/order/checkout.jsp").forward(request, response);
    }
}
