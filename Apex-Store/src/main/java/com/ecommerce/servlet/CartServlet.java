package com.ecommerce.servlet;

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

@WebServlet(name = "CartServlet", urlPatterns = "/cart")
public class CartServlet extends HttpServlet {
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        List<CartItem> cartItems = cartService.getCart(currentUser.getId());
        BigDecimal subtotal = cartService.getCartSubtotal(currentUser.getId());

        // Check for applied coupon in session
        String couponCode = (String) session.getAttribute("appliedCouponCode");
        BigDecimal discount = BigDecimal.ZERO;
        String couponMessage = null;

        if (couponCode != null && !couponCode.isEmpty()) {
            CartService.CouponResult cRes = cartService.validateCoupon(couponCode, subtotal);
            if (cRes.valid()) {
                discount = cRes.discountAmount();
                couponMessage = cRes.message();
            } else {
                session.removeAttribute("appliedCouponCode");
                request.setAttribute("couponError", cRes.message());
            }
        }

        BigDecimal shippingFee = (subtotal.compareTo(new BigDecimal("999.00")) >= 0 || subtotal.compareTo(BigDecimal.ZERO) == 0)
                ? BigDecimal.ZERO : new BigDecimal("99.00");
        BigDecimal total = subtotal.subtract(discount).add(shippingFee).max(BigDecimal.ZERO);

        request.setAttribute("cartItems", cartItems);
        request.setAttribute("subtotal", subtotal);
        request.setAttribute("discount", discount);
        request.setAttribute("shippingFee", shippingFee);
        request.setAttribute("total", total);
        request.setAttribute("couponCode", couponCode);
        request.setAttribute("couponMessage", couponMessage);

        request.getRequestDispatcher("/WEB-INF/views/cart/cart.jsp").forward(request, response);
    }
}
