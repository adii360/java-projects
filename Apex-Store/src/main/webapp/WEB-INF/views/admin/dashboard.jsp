<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Admin Dashboard | ApexStore" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
        <div>
            <div style="font-size: 0.85rem; font-weight: 700; color: var(--primary); text-transform: uppercase; letter-spacing: 0.05em;">Administration Center</div>
            <h1 style="font-size: 2rem; font-weight: 800; letter-spacing: -0.02em;">Store Overview & Analytics</h1>
        </div>
        <div style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
            <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-secondary btn-sm"><i class="fa-solid fa-boxes-stacked"></i> Products</a>
            <a href="${pageContext.request.contextPath}/admin/categories" class="btn btn-secondary btn-sm"><i class="fa-solid fa-tags"></i> Categories</a>
            <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-secondary btn-sm"><i class="fa-solid fa-receipt"></i> Orders</a>
            <a href="${pageContext.request.contextPath}/admin/inventory" class="btn btn-secondary btn-sm"><i class="fa-solid fa-warehouse"></i> Inventory</a>
            <a href="${pageContext.request.contextPath}/admin/customers" class="btn btn-secondary btn-sm"><i class="fa-solid fa-users"></i> Customers</a>
        </div>
    </div>

    <!-- Metric Cards -->
    <div class="admin-grid">
        <div class="metric-card">
            <div>
                <span class="metric-label">Total Revenue</span>
                <div class="metric-val" style="color: #059669;">
                    ₹<fmt:formatNumber value="${stats.totalRevenue}" pattern="#,##0.00"/>
                </div>
            </div>
            <div class="metric-icon" style="background: var(--success-light); color: var(--success);">
                <i class="fa-solid fa-indian-rupee-sign"></i>
            </div>
        </div>

        <div class="metric-card">
            <div>
                <span class="metric-label">Total Orders</span>
                <div class="metric-val" style="color: var(--primary);">
                    ${stats.totalOrders}
                </div>
            </div>
            <div class="metric-icon" style="background: var(--primary-light); color: var(--primary);">
                <i class="fa-solid fa-bag-shopping"></i>
            </div>
        </div>

        <div class="metric-card">
            <div>
                <span class="metric-label">Total Customers</span>
                <div class="metric-val" style="color: var(--secondary);">
                    ${stats.totalCustomers}
                </div>
            </div>
            <div class="metric-icon" style="background: #e0f2fe; color: var(--secondary);">
                <i class="fa-solid fa-users"></i>
            </div>
        </div>

        <div class="metric-card">
            <div>
                <span class="metric-label">Active Products</span>
                <div class="metric-val" style="color: #7c3aed;">
                    ${stats.totalProducts}
                </div>
            </div>
            <div class="metric-icon" style="background: #f5f3ff; color: #7c3aed;">
                <i class="fa-solid fa-box-open"></i>
            </div>
        </div>
    </div>

    <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 2rem; align-items: start;">
        <!-- Recent Orders -->
        <div class="card" style="padding: 0; overflow: hidden;">
            <div style="padding: 1.25rem 1.5rem; border-bottom: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center;">
                <h3 style="font-size: 1.1rem; font-weight: 700;">Recent Store Orders</h3>
                <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-secondary btn-sm">View All Orders</a>
            </div>

            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>Order</th>
                            <th>Customer</th>
                            <th>Total</th>
                            <th>Status</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="o" items="${stats.recentOrders}">
                            <tr>
                                <td>
                                    <strong style="color: var(--primary); font-size: 0.9rem;">${o.orderNumber}</strong>
                                    <div style="font-size: 0.75rem; color: var(--text-muted);"><fmt:formatDate value="${o.createdAt}" pattern="MMM dd, HH:mm"/></div>
                                </td>
                                <td>${o.customerName}</td>
                                <td style="font-weight: 700;">₹<fmt:formatNumber value="${o.totalAmount}" pattern="#,##0.00"/></td>
                                <td>
                                    <span class="badge ${o.orderStatus == 'DELIVERED' ? 'badge-success' : 'badge-warning'}">
                                        ${o.orderStatus}
                                    </span>
                                </td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/orders/detail?id=${o.id}" class="btn btn-secondary btn-sm">Inspect</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Low Stock Alerts -->
        <div class="card">
            <h3 class="card-title" style="color: var(--danger);"><i class="fa-solid fa-triangle-exclamation"></i> Low Inventory Alerts</h3>
            <c:choose>
                <c:when test="${not empty stats.lowStockProducts}">
                    <div style="display: flex; flex-direction: column; gap: 0.75rem;">
                        <c:forEach var="lsp" items="${stats.lowStockProducts}">
                            <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.75rem 1rem; background: var(--danger-light); border: 1px solid #fecaca; border-radius: var(--radius-md);">
                                <div>
                                    <strong style="font-size: 0.9rem; color: #991b1b; display: block;">${lsp.name}</strong>
                                    <span style="font-size: 0.75rem; color: #b91c1c;">Category: ${lsp.categoryName}</span>
                                </div>
                                <span class="badge badge-danger" style="font-size: 0.8rem;">
                                    ${lsp.stockQuantity} left
                                </span>
                            </div>
                        </c:forEach>
                    </div>
                    <div style="margin-top: 1rem;">
                        <a href="${pageContext.request.contextPath}/admin/inventory" class="btn btn-secondary btn-sm btn-block">Manage Inventory</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <p style="color: var(--text-muted); font-size: 0.9rem;">All inventory levels are healthy!</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp"/>
