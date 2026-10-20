<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Manage Orders | ApexStore Admin" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <div>
            <div style="font-size: 0.85rem; font-weight: 700; color: var(--primary); text-transform: uppercase;">Fulfillment Operations</div>
            <h1 style="font-size: 2rem; font-weight: 800; letter-spacing: -0.02em;">Order Management</h1>
        </div>
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-secondary btn-sm">
            <i class="fa-solid fa-arrow-left"></i> Back to Dashboard
        </a>
    </div>

    <div class="card" style="padding: 0; overflow: hidden;">
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>Order</th>
                        <th>Customer</th>
                        <th>Date</th>
                        <th>Amount</th>
                        <th>Payment Status</th>
                        <th>Fulfillment Status</th>
                        <th>Update Status</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="o" items="${orders}">
                        <tr>
                            <td>
                                <a href="${pageContext.request.contextPath}/orders/detail?id=${o.id}" style="color: var(--primary); font-weight: 700;">
                                    ${o.orderNumber}
                                </a>
                            </td>
                            <td>
                                <strong>${o.customerName}</strong>
                                <div style="font-size: 0.8rem; color: var(--text-muted);">${o.customerEmail}</div>
                            </td>
                            <td style="font-size: 0.85rem; color: var(--text-muted);">
                                <fmt:formatDate value="${o.createdAt}" pattern="MMM dd, yyyy HH:mm"/>
                            </td>
                            <td style="font-weight: 800;">
                                ₹<fmt:formatNumber value="${o.totalAmount}" pattern="#,##0.00"/>
                            </td>
                            <td>
                                <span class="badge ${o.paymentStatus == 'PAID' ? 'badge-success' : 'badge-warning'}">
                                    ${o.paymentStatus}
                                </span>
                            </td>
                            <td>
                                <span class="badge ${o.orderStatus == 'DELIVERED' ? 'badge-success' : (o.orderStatus == 'CANCELLED' ? 'badge-danger' : 'badge-warning')}">
                                    ${o.orderStatus}
                                </span>
                            </td>
                            <td>
                                <form action="${pageContext.request.contextPath}/admin/orders" method="POST" style="margin: 0; display: flex; gap: 0.4rem; align-items: center;">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                    <input type="hidden" name="orderId" value="${o.id}">

                                    <select name="orderStatus" class="form-control" style="padding: 0.25rem 0.5rem; font-size: 0.8rem; width: auto;">
                                        <option value="PENDING" ${o.orderStatus == 'PENDING' ? 'selected' : ''}>PENDING</option>
                                        <option value="PROCESSING" ${o.orderStatus == 'PROCESSING' ? 'selected' : ''}>PROCESSING</option>
                                        <option value="SHIPPED" ${o.orderStatus == 'SHIPPED' ? 'selected' : ''}>SHIPPED</option>
                                        <option value="DELIVERED" ${o.orderStatus == 'DELIVERED' ? 'selected' : ''}>DELIVERED</option>
                                        <option value="CANCELLED" ${o.orderStatus == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                                    </select>

                                    <select name="paymentStatus" class="form-control" style="padding: 0.25rem 0.5rem; font-size: 0.8rem; width: auto;">
                                        <option value="PENDING" ${o.paymentStatus == 'PENDING' ? 'selected' : ''}>PENDING</option>
                                        <option value="PAID" ${o.paymentStatus == 'PAID' ? 'selected' : ''}>PAID</option>
                                        <option value="REFUNDED" ${o.paymentStatus == 'REFUNDED' ? 'selected' : ''}>REFUNDED</option>
                                    </select>

                                    <button type="submit" class="btn btn-primary btn-sm" style="padding: 0.25rem 0.6rem;">Save</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp"/>
