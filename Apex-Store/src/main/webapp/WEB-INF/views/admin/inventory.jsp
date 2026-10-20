<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Inventory Control | ApexStore Admin" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <div>
            <div style="font-size: 0.85rem; font-weight: 700; color: var(--primary); text-transform: uppercase;">Stock Matrix</div>
            <h1 style="font-size: 2rem; font-weight: 800; letter-spacing: -0.02em;">Inventory & Replenishment</h1>
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
                        <th>Product</th>
                        <th>Category</th>
                        <th>Price</th>
                        <th>Current Units</th>
                        <th>Status</th>
                        <th>Update Quantity</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${products}">
                        <tr>
                            <td>
                                <strong style="font-size: 0.95rem;">${p.name}</strong>
                                <div style="font-size: 0.8rem; color: var(--text-muted);">Slug: ${p.slug}</div>
                            </td>
                            <td>${p.categoryName}</td>
                            <td>₹<fmt:formatNumber value="${p.price}" pattern="#,##0.00"/></td>
                            <td style="font-weight: 800; font-size: 1.1rem; color: ${p.stockQuantity <= 5 ? 'var(--danger)' : 'var(--text-main)'};">
                                ${p.stockQuantity}
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${p.stockQuantity == 0}">
                                        <span class="badge badge-danger">OUT OF STOCK</span>
                                    </c:when>
                                    <c:when test="${p.stockQuantity <= 5}">
                                        <span class="badge badge-warning">LOW STOCK (&le; 5)</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-success">HEALTHY</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <form action="${pageContext.request.contextPath}/admin/inventory" method="POST" style="margin: 0; display: flex; gap: 0.5rem; align-items: center;">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                    <input type="hidden" name="productId" value="${p.id}">
                                    <input type="number" name="quantity" value="${p.stockQuantity}" min="0" class="form-control" style="width: 80px; padding: 0.25rem 0.5rem; font-size: 0.88rem;">
                                    <button type="submit" class="btn btn-primary btn-sm">Update</button>
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
