<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Your Shopping Cart | ApexStore" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container">
    <h1 style="font-size: 2rem; font-weight: 800; letter-spacing: -0.02em; margin-bottom: 2rem;">
        Shopping Cart
    </h1>

    <c:choose>
        <c:when test="${not empty cartItems}">
            <div class="cart-layout">
                <!-- Items Table -->
                <div class="card" style="padding: 0; overflow: hidden;">
                    <div class="table-responsive">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>Product</th>
                                    <th>Price</th>
                                    <th>Quantity</th>
                                    <th>Subtotal</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${cartItems}">
                                    <tr>
                                        <td>
                                            <div style="display: flex; align-items: center; gap: 1rem;">
                                                <a href="${pageContext.request.contextPath}/product?id=${item.productId}" title="${item.product.name}">
                                                    <img src="${item.product.imageUrl}" alt="${item.product.name}" style="width: 70px; height: 70px; object-fit: cover; border-radius: var(--radius-md);">
                                                </a>
                                                <div>
                                                    <a href="${pageContext.request.contextPath}/product?id=${item.productId}" style="font-weight: 700; color: var(--text-main); font-size: 0.95rem;">
                                                        ${item.product.name}
                                                    </a>
                                                    <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.2rem;">
                                                        Stock: ${item.product.stockQuantity} available
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                        <td style="font-weight: 600;">
                                            ₹<fmt:formatNumber value="${item.product.price}" pattern="#,##0.00"/>
                                        </td>
                                        <td>
                                            <form action="${pageContext.request.contextPath}/cart/update" method="POST" style="margin: 0; display: inline-flex;">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                <input type="hidden" name="cartItemId" value="${item.id}">
                                                <div class="qty-control">
                                                    <button type="submit" name="quantity" value="${item.quantity - 1}" class="qty-btn" ${item.quantity <= 1 ? 'disabled' : ''}>-</button>
                                                    <span style="padding: 0.4rem 0.8rem; font-weight: 700; font-size: 0.9rem;">${item.quantity}</span>
                                                    <button type="submit" name="quantity" value="${item.quantity + 1}" class="qty-btn" ${item.quantity >= item.product.stockQuantity ? 'disabled' : ''}>+</button>
                                                </div>
                                            </form>
                                        </td>
                                        <td style="font-weight: 800; font-size: 1.05rem;">
                                            ₹<fmt:formatNumber value="${item.lineTotal}" pattern="#,##0.00"/>
                                        </td>
                                        <td>
                                            <form action="${pageContext.request.contextPath}/cart/remove" method="POST" style="margin: 0;">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                <input type="hidden" name="cartItemId" value="${item.id}">
                                                <button type="submit" style="background: none; border: none; color: var(--danger); cursor: pointer; font-size: 1.1rem;" title="Remove Item">
                                                    <i class="fa-regular fa-trash-can"></i>
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Summary Sidebar -->
                <div>
                    <!-- Coupon Card -->
                    <div class="card" style="padding: 1.5rem; margin-bottom: 1.5rem;">
                        <h4 style="font-size: 1rem; font-weight: 700; margin-bottom: 0.75rem;">Promo Coupon</h4>
                        <form action="${pageContext.request.contextPath}/cart/update" method="POST">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">

                            <c:choose>
                                <c:when test="${not empty couponCode}">
                                    <div style="background: var(--success-light); border: 1px solid #a7f3d0; border-radius: var(--radius-md); padding: 0.75rem 1rem; display: flex; justify-content: space-between; align-items: center;">
                                        <div>
                                            <strong style="color: #065f46; font-size: 0.9rem;">${couponCode}</strong>
                                            <div style="font-size: 0.75rem; color: #047857;">Coupon applied</div>
                                        </div>
                                        <button type="submit" name="action" value="removeCoupon" class="btn btn-sm btn-danger" style="padding: 0.25rem 0.6rem;">Remove</button>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <input type="hidden" name="action" value="applyCoupon">
                                    <div style="display: flex; gap: 0.5rem;">
                                        <input type="text" name="couponCode" class="form-control" placeholder="Code (e.g. WELCOME10)" required style="text-transform: uppercase;">
                                        <button type="submit" class="btn btn-secondary btn-sm" style="white-space: nowrap;">Apply</button>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </form>
                    </div>

                    <!-- Order Total Card -->
                    <div class="order-summary-box">
                        <h3 style="font-size: 1.2rem; font-weight: 800; margin-bottom: 1.25rem;">Order Summary</h3>

                        <div class="summary-row">
                            <span>Subtotal</span>
                            <span>₹<fmt:formatNumber value="${subtotal}" pattern="#,##0.00"/></span>
                        </div>

                        <c:if test="${discount > 0}">
                            <div class="summary-row" style="color: var(--success); font-weight: 600;">
                                <span>Discount (${couponCode})</span>
                                <span>-₹<fmt:formatNumber value="${discount}" pattern="#,##0.00"/></span>
                            </div>
                        </c:if>

                        <div class="summary-row">
                            <span>Shipping Fee</span>
                            <span>
                                <c:choose>
                                    <c:when test="${shippingFee == 0}">
                                        <strong style="color: var(--success);">FREE</strong>
                                    </c:when>
                                    <c:otherwise>
                                        ₹<fmt:formatNumber value="${shippingFee}" pattern="#,##0.00"/>
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </div>

                        <div class="summary-row total">
                            <span>Estimated Total</span>
                            <span>₹<fmt:formatNumber value="${total}" pattern="#,##0.00"/></span>
                        </div>

                        <a href="${pageContext.request.contextPath}/checkout" class="btn btn-primary btn-block btn-lg" style="margin-top: 1.5rem;">
                            <i class="fa-solid fa-lock"></i> Proceed to Checkout
                        </a>

                        <div style="text-align: center; margin-top: 1rem;">
                            <a href="${pageContext.request.contextPath}/products" style="color: var(--text-muted); font-size: 0.85rem; font-weight: 600;">
                                <i class="fa-solid fa-arrow-left"></i> Continue Shopping
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <!-- Empty Cart -->
            <div class="card" style="text-align: center; padding: 4.5rem 2rem;">
                <div style="font-size: 3.5rem; color: var(--text-light); margin-bottom: 1.25rem;"><i class="fa-solid fa-cart-shopping"></i></div>
                <h2 style="font-size: 1.5rem; font-weight: 800; margin-bottom: 0.5rem;">Your cart is empty</h2>
                <p style="color: var(--text-muted); font-size: 0.95rem; margin-bottom: 1.75rem;">Looks like you haven't added anything to your cart yet.</p>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-lg">Explore Catalog</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../common/footer.jsp"/>
