<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Order ${order.orderNumber} | ApexStore" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container" style="max-width: 960px;">
    <c:if test="${isNewlyPlaced}">
        <div style="background: var(--success-light); border: 2px solid #a7f3d0; border-radius: var(--radius-lg); padding: 2rem; text-align: center; margin-bottom: 2.5rem; animation: slideDown 0.4s ease;">
            <div style="width: 4rem; height: 4rem; background: var(--success); color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 2rem; margin: 0 auto 1rem;">
                <i class="fa-solid fa-check"></i>
            </div>
            <h1 style="font-size: 1.8rem; font-weight: 800; color: #065f46; margin-bottom: 0.5rem;">Order Successfully Placed!</h1>
            <p style="color: #047857; font-size: 0.95rem; margin-bottom: 1rem;">
                Thank you for your purchase! Payment method: <strong><c:choose><c:when test="${order.paymentMethod == 'COD'}">Cash on Delivery (COD)</c:when><c:otherwise>Online Payment (${order.paymentMethod})</c:otherwise></c:choose></strong>.
            </p>
            <c:if test="${order.orderStatus == 'PENDING' || order.orderStatus == 'PROCESSING'}">
                <div style="margin-top: 1rem; padding-top: 1rem; border-top: 1px dashed #6ee7b7; display: flex; align-items: center; justify-content: center; gap: 0.75rem;">
                    <span style="font-size: 0.88rem; color: #065f46;">Need to change your mind?</span>
                    <form id="cancelOrderBannerForm" action="${pageContext.request.contextPath}/orders/cancel" method="POST" style="margin: 0;">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="orderId" value="${order.id}">
                        <button type="submit" id="cancelOrderBannerBtn" class="btn btn-sm btn-danger" style="display: inline-flex; align-items: center; gap: 0.4rem; font-weight: 700; background: #dc2626; border-color: #dc2626;">
                            <i class="fa-solid fa-ban" id="cancelOrderBannerIcon"></i>
                            <span id="cancelOrderBannerText">Cancel Order</span>
                        </button>
                    </form>
                </div>
            </c:if>
        </div>
    </c:if>

    <c:if test="${isNewlyCancelled or order.orderStatus == 'CANCELLED'}">
        <div style="background: #fee2e2; border: 2px solid #fca5a5; border-radius: var(--radius-lg); padding: 1.75rem 2rem; text-align: center; margin-bottom: 2.5rem; animation: slideDown 0.4s ease;">
            <div style="width: 3.5rem; height: 3.5rem; background: var(--danger, #ef4444); color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.75rem; margin: 0 auto 0.75rem;">
                <i class="fa-solid fa-ban"></i>
            </div>
            <h2 style="font-size: 1.6rem; font-weight: 800; color: #991b1b; margin-bottom: 0.4rem;">Order Cancelled</h2>
            <p style="color: #b91c1c; font-size: 0.95rem; margin: 0;">
                Order #${order.orderNumber} has been cancelled.<c:choose><c:when test="${order.paymentStatus == 'REFUNDED'}"> Payment refund has been processed.</c:when><c:otherwise> Reserved stock has been returned to store inventory.</c:otherwise></c:choose>
            </p>
        </div>
    </c:if>

    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
        <div>
            <div style="font-size: 0.85rem; color: var(--text-muted);">
                Placed on <fmt:formatDate value="${order.createdAt}" pattern="MMMM dd, yyyy 'at' hh:mm a"/>
            </div>
            <h2 style="font-size: 1.7rem; font-weight: 800; color: var(--text-main); margin-top: 0.2rem;">
                Order #${order.orderNumber}
            </h2>
        </div>
        <div style="display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap;">
            <c:choose>
                <c:when test="${order.orderStatus == 'CANCELLED'}">
                    <span class="badge badge-danger" style="font-size: 0.9rem; padding: 0.4rem 1rem;">
                        <i class="fa-solid fa-ban"></i> Status: CANCELLED
                    </span>
                </c:when>
                <c:when test="${order.orderStatus == 'DELIVERED'}">
                    <span class="badge badge-success" style="font-size: 0.9rem; padding: 0.4rem 1rem;">
                        <i class="fa-solid fa-check-double"></i> Status: DELIVERED
                    </span>
                </c:when>
                <c:when test="${order.orderStatus == 'SHIPPED'}">
                    <span class="badge badge-primary" style="font-size: 0.9rem; padding: 0.4rem 1rem;">
                        <i class="fa-solid fa-truck"></i> Status: SHIPPED
                    </span>
                </c:when>
                <c:otherwise>
                    <span class="badge badge-warning" style="font-size: 0.9rem; padding: 0.4rem 1rem;">
                        <i class="fa-solid fa-clock"></i> Status: ${order.orderStatus}
                    </span>
                </c:otherwise>
            </c:choose>

            <span class="badge ${order.paymentStatus == 'PAID' ? 'badge-success' : (order.paymentStatus == 'REFUNDED' ? 'badge-danger' : 'badge-primary')}" style="font-size: 0.9rem; padding: 0.4rem 1rem;">
                Payment: ${order.paymentMethod} (${order.paymentStatus})
            </span>

            <c:if test="${order.orderStatus == 'PENDING' || order.orderStatus == 'PROCESSING'}">
                <form id="cancelOrderHeaderForm" action="${pageContext.request.contextPath}/orders/cancel" method="POST" style="margin: 0;">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="orderId" value="${order.id}">
                    <button type="submit" id="cancelOrderHeaderBtn" class="btn btn-sm btn-danger" style="display: inline-flex; align-items: center; gap: 0.4rem; font-weight: 700; background: #ef4444; border: 1px solid #dc2626; cursor: pointer; box-shadow: 0 2px 6px rgba(239,68,68,0.25);">
                        <i class="fa-solid fa-ban" id="cancelOrderHeaderIcon"></i>
                        <span id="cancelOrderHeaderText">Cancel Order</span>
                    </button>
                </form>
            </c:if>
        </div>
    </div>

    <!-- Order Items List -->
    <div class="card" style="padding: 0; overflow: hidden; margin-bottom: 2rem;">
        <div style="padding: 1.25rem 1.5rem; border-bottom: 1px solid var(--border); font-weight: 700;">
            Items in this Order (${order.items.size()})
        </div>
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>Item</th>
                        <th>Price</th>
                        <th>Qty</th>
                        <th>Line Total</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="item" items="${order.items}">
                        <tr>
                            <td>
                                <div style="display: flex; align-items: center; gap: 1rem;">
                                    <c:if test="${not empty item.productImageUrl}">
                                        <c:choose>
                                            <c:when test="${not empty item.productId}">
                                                <a href="${pageContext.request.contextPath}/product?id=${item.productId}" title="${item.productName}">
                                                    <img src="${item.productImageUrl}" alt="${item.productName}" style="width: 50px; height: 50px; object-fit: cover; border-radius: var(--radius-sm);">
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${item.productImageUrl}" alt="${item.productName}" style="width: 50px; height: 50px; object-fit: cover; border-radius: var(--radius-sm);">
                                            </c:otherwise>
                                        </c:choose>
                                    </c:if>
                                    <div>
                                        <c:choose>
                                            <c:when test="${not empty item.productId}">
                                                <a href="${pageContext.request.contextPath}/product?id=${item.productId}" style="text-decoration: none;">
                                                    <strong style="font-size: 0.95rem; color: var(--text-main);">${item.productName}</strong>
                                                </a>
                                                <div>
                                                    <a href="${pageContext.request.contextPath}/product?id=${item.productId}" style="font-size: 0.8rem; color: var(--primary);">View Product Page</a>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <strong style="font-size: 0.95rem; color: var(--text-main);">${item.productName}</strong>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </td>
                            <td>₹<fmt:formatNumber value="${item.unitPrice}" pattern="#,##0.00"/></td>
                            <td>${item.quantity}</td>
                            <td style="font-weight: 800;">₹<fmt:formatNumber value="${item.lineTotal}" pattern="#,##0.00"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Order Metadata Split -->
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 2rem; align-items: start;">
        <!-- Shipping Address -->
        <div class="card">
            <h4 style="font-size: 1rem; font-weight: 700; margin-bottom: 0.75rem;"><i class="fa-solid fa-location-dot" style="color: var(--primary);"></i> Shipping Destination</h4>
            <p style="color: var(--text-muted); font-size: 0.95rem; line-height: 1.6;">
                ${order.shippingAddressText}
            </p>
        </div>

        <!-- Price Breakdown -->
        <div class="card">
            <h4 style="font-size: 1rem; font-weight: 700; margin-bottom: 1rem;">Payment Breakdown</h4>
            <div class="summary-row">
                <span>Subtotal</span>
                <span>₹<fmt:formatNumber value="${order.subtotal}" pattern="#,##0.00"/></span>
            </div>
            <c:if test="${order.discount > 0}">
                <div class="summary-row" style="color: var(--success); font-weight: 600;">
                    <span>Discount</span>
                    <span>-₹<fmt:formatNumber value="${order.discount}" pattern="#,##0.00"/></span>
                </div>
            </c:if>
            <div class="summary-row">
                <span>Shipping Fee</span>
                <span>${order.shippingFee == 0 ? 'FREE' : '₹'.concat(order.shippingFee)}</span>
            </div>
            <div class="summary-row total" style="margin-top: 0.75rem; padding-top: 0.75rem;">
                <span>Total Amount</span>
                <span>₹<fmt:formatNumber value="${order.totalAmount}" pattern="#,##0.00"/></span>
            </div>
        </div>
    </div>

    <div style="margin-top: 2.5rem; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
        <a href="${pageContext.request.contextPath}/orders" class="btn btn-secondary btn-sm">
            <i class="fa-solid fa-arrow-left"></i> Back to Orders
        </a>
        <div style="display: flex; gap: 1rem; align-items: center;">
            <c:if test="${order.orderStatus == 'PENDING' || order.orderStatus == 'PROCESSING'}">
                <form id="cancelOrderBottomForm" action="${pageContext.request.contextPath}/orders/cancel" method="POST" style="margin: 0;">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="orderId" value="${order.id}">
                    <button type="submit" id="cancelOrderBottomBtn" class="btn btn-danger btn-sm" style="display: inline-flex; align-items: center; gap: 0.5rem; font-weight: 700; background: #ef4444; border-color: #dc2626;">
                        <i class="fa-solid fa-ban" id="cancelOrderBottomIcon"></i>
                        <span id="cancelOrderBottomText">Cancel Order</span>
                    </button>
                </form>
            </c:if>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-sm">
                Continue Shopping
            </a>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    function setupCancelButton(formId, btnId, iconId, textId) {
        var form = document.getElementById(formId);
        if (!form) return;
        form.addEventListener('submit', function(e) {
            var confirmed = confirm('Are you sure you want to cancel Order #${order.orderNumber}? This will release the reserved items back to inventory.');
            if (!confirmed) {
                e.preventDefault();
                return false;
            }
            var btn = document.getElementById(btnId);
            var icon = document.getElementById(iconId);
            var text = document.getElementById(textId);
            if (btn && text) {
                btn.style.pointerEvents = 'none';
                btn.style.opacity = '0.7';
                if (icon) {
                    icon.className = 'fa-solid fa-spinner fa-spin';
                }
                text.textContent = 'Cancelling...';
            }
        });
    }

    setupCancelButton('cancelOrderBannerForm', 'cancelOrderBannerBtn', 'cancelOrderBannerIcon', 'cancelOrderBannerText');
    setupCancelButton('cancelOrderHeaderForm', 'cancelOrderHeaderBtn', 'cancelOrderHeaderIcon', 'cancelOrderHeaderText');
    setupCancelButton('cancelOrderBottomForm', 'cancelOrderBottomBtn', 'cancelOrderBottomIcon', 'cancelOrderBottomText');
});
</script>

<jsp:include page="../common/footer.jsp"/>
