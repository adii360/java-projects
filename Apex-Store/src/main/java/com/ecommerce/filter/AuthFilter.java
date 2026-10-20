package com.ecommerce.filter;

import com.ecommerce.model.User;
import com.ecommerce.util.ValidationUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(filterName = "AuthFilter", urlPatterns = {
        "/cart", "/cart/*",
        "/wishlist", "/wishlist/*",
        "/checkout",
        "/orders", "/orders/*",
        "/profile",
        "/addresses",
        "/reviews"
})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            String method = httpRequest.getMethod();
            String targetUri = httpRequest.getRequestURI();
            String contextPath = httpRequest.getContextPath();

            HttpSession newSession = httpRequest.getSession(true);

            // Handle POST actions so we never redirect via GET to a POST-only action URL
            if ("POST".equalsIgnoreCase(method)) {
                if (targetUri.endsWith("/cart/add")) {
                    int productId = ValidationUtil.parseIntOrDefault(httpRequest.getParameter("productId"), -1);
                    int quantity = ValidationUtil.parseIntOrDefault(httpRequest.getParameter("quantity"), 1);
                    if (productId > 0) {
                        newSession.setAttribute("pendingAddToCartProductId", productId);
                        newSession.setAttribute("pendingAddToCartQuantity", quantity > 0 ? quantity : 1);
                    }
                    newSession.setAttribute("redirectAfterLogin", contextPath + "/cart");
                } else if (targetUri.endsWith("/wishlist/add")) {
                    int productId = ValidationUtil.parseIntOrDefault(httpRequest.getParameter("productId"), -1);
                    if (productId > 0) {
                        newSession.setAttribute("pendingAddToWishlistProductId", productId);
                    }
                    newSession.setAttribute("redirectAfterLogin", contextPath + "/wishlist");
                } else {
                    String referer = httpRequest.getHeader("Referer");
                    if (referer != null && !referer.isEmpty() && referer.contains(contextPath)) {
                        newSession.setAttribute("redirectAfterLogin", referer);
                    } else if (targetUri.contains("/cart")) {
                        newSession.setAttribute("redirectAfterLogin", contextPath + "/cart");
                    } else if (targetUri.contains("/wishlist")) {
                        newSession.setAttribute("redirectAfterLogin", contextPath + "/wishlist");
                    } else if (targetUri.contains("/orders") || targetUri.contains("/checkout")) {
                        newSession.setAttribute("redirectAfterLogin", contextPath + "/checkout");
                    } else {
                        newSession.setAttribute("redirectAfterLogin", contextPath + "/");
                    }
                }
            } else {
                // GET requests: ensure target is not a POST-only action URL mistakenly requested
                if (targetUri.endsWith("/cart/add") || targetUri.endsWith("/cart/remove") || targetUri.endsWith("/cart/update")) {
                    newSession.setAttribute("redirectAfterLogin", contextPath + "/cart");
                } else if (targetUri.endsWith("/wishlist/add") || targetUri.endsWith("/wishlist/remove")) {
                    newSession.setAttribute("redirectAfterLogin", contextPath + "/wishlist");
                } else if (targetUri.endsWith("/orders/place")) {
                    newSession.setAttribute("redirectAfterLogin", contextPath + "/checkout");
                } else {
                    String queryString = httpRequest.getQueryString();
                    if (queryString != null && !queryString.isEmpty()) {
                        targetUri += "?" + queryString;
                    }
                    newSession.setAttribute("redirectAfterLogin", targetUri);
                }
            }

            httpResponse.sendRedirect(contextPath + "/login?error=auth_required");
            return;
        }

        chain.doFilter(request, response);
    }
}
