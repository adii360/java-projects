<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="My Orders | ApexStore" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container">
    <div class="section-header">
        <div>
            <h1 style="font-size: 2rem; font-weight: 800; letter-spacing: -0.02em;">Order History</h1>
            <p style="color: var(--text-muted); font-size: 0.95rem;">Track shipments, review purchases, and view past invoices</p>
        </div>
    </div>

    <c:choose>
        <c:when test="${not empty orders}">
            <div class="card" style="padding: 0; overflow: hidden;">
                <div class="table-responsive">
                    <table class="table">
                        <thead>
                            <tr>
                                <th>Order Number</th>
                                <th>Date</th>
                                <th>Items</th>
                                <th>Total Amount</th>
                                <th>Payment</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="o" items="${orders}">
                                <tr>
                                    <td>
                                        <strong style="font-size: 0.95rem; color: var(--primary);">${o.orderNumber}</strong>
                                    </td>
                                    <td style="color: var(--text-muted); font-size: 0.88rem;">
                                        <fmt:formatDate value="${o.createdAt}" pattern="MMM dd, yyyy HH:mm"/>
                                    </td>
                                    <td>
                                        <span style="font-weight: 600;">${o.items.size()} item(s)</span>
                                    </td>
                                    <td style="font-weight: 800; font-size: 1.05rem;">
                                        ₹<fmt:formatNumber value="${o.totalAmount}" pattern="#,##0.00"/>
                                    </td>
                                    <td>
                                        <span class="badge ${o.paymentStatus == 'PAID' ? 'badge-success' : 'badge-warning'}">
                                            ${o.paymentMethod} (${o.paymentStatus})
                                        </span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${o.orderStatus == 'DELIVERED'}">
                                                <span class="badge badge-success">${o.orderStatus}</span>
                                            </c:when>
                                            <c:when test="${o.orderStatus == 'CANCELLED'}">
                                                <span class="badge badge-danger">${o.orderStatus}</span>
                                            </c:when>
                                            <c:when test="${o.orderStatus == 'SHIPPED'}">
                                                <span class="badge badge-primary">${o.orderStatus}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-warning">${o.orderStatus}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div style="display: flex; gap: 0.5rem; align-items: center;">
                                            <a href="${pageContext.request.contextPath}/orders/detail?id=${o.id}" class="btn btn-secondary btn-sm">
                                                View Details
                                            </a>
                                            <c:if test="${o.orderStatus == 'PENDING' || o.orderStatus == 'PROCESSING'}">
                                                <form action="${pageContext.request.contextPath}/orders/cancel" method="POST" style="margin: 0;" onsubmit="return handleCancelHistoryOrder(event, this, '${o.orderNumber}');">
                                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                    <input type="hidden" name="orderId" value="${o.id}">
                                                    <button type="submit" class="btn btn-sm btn-danger" style="background: #ef4444; border-color: #dc2626; padding: 0.35rem 0.75rem; font-weight: 600; display: inline-flex; align-items: center; gap: 0.3rem;">
                                                        <i class="fa-solid fa-ban"></i>
                                                        <span>Cancel</span>
                                                    </button>
                                                </form>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
            <script>
            function handleCancelHistoryOrder(event, form, orderNumber) {
                if (!confirm('Are you sure you want to cancel Order #' + orderNumber + '? This will release reserved stock back to store inventory.')) {
                    event.preventDefault();
                    return false;
                }
                var btn = form.querySelector('button[type="submit"]');
                if (btn) {
                    btn.style.pointerEvents = 'none';
                    btn.style.opacity = '0.7';
                    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Cancelling...';
                }
                return true;
            }
            </script>
        </c:when>
        <c:otherwise>
            <div class="card" style="text-align: center; padding: 4rem 2rem;">
                <div style="font-size: 3rem; color: var(--text-light); margin-bottom: 1rem;"><i class="fa-solid fa-box-open"></i></div>
                <h3 style="font-size: 1.3rem; font-weight: 700; margin-bottom: 0.5rem;">No orders found</h3>
                <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.5rem;">You have not placed any orders yet.</p>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-sm">Start Shopping</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../common/footer.jsp"/>
