<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Manage Products | ApexStore Admin" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <div>
            <div style="font-size: 0.85rem; font-weight: 700; color: var(--primary); text-transform: uppercase;">Catalog Management</div>
            <h1 style="font-size: 2rem; font-weight: 800; letter-spacing: -0.02em;">Products Directory</h1>
        </div>
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-secondary btn-sm">
            <i class="fa-solid fa-arrow-left"></i> Back to Dashboard
        </a>
    </div>

    <!-- Add or Edit Product Form -->
    <div class="card" style="margin-bottom: 2.5rem; background: var(--bg-surface);">
        <h3 class="card-title">
            <c:choose>
                <c:when test="${not empty editProduct}">
                    <i class="fa-solid fa-pen-to-square"></i> Edit Product: ${editProduct.name}
                </c:when>
                <c:otherwise>
                    <i class="fa-solid fa-plus"></i> Create New Product
                </c:otherwise>
            </c:choose>
        </h3>

        <form action="${pageContext.request.contextPath}/admin/products" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <input type="hidden" name="action" value="${not empty editProduct ? 'update' : 'create'}">
            <c:if test="${not empty editProduct}">
                <input type="hidden" name="id" value="${editProduct.id}">
            </c:if>

            <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 1rem;">
                <div class="form-group">
                    <label class="form-label">Product Name</label>
                    <input type="text" name="name" value="${editProduct.name}" class="form-control" required placeholder="e.g. ProBook Ultra 16-inch">
                </div>

                <div class="form-group">
                    <label class="form-label">Category</label>
                    <select name="categoryId" class="form-control" required>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat.id}" ${editProduct.categoryId == cat.id ? 'selected' : ''}>${cat.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr 2fr; gap: 1rem;">
                <div class="form-group">
                    <label class="form-label">Price (₹)</label>
                    <input type="number" step="0.01" name="price" value="${editProduct.price}" class="form-control" required placeholder="2999.00">
                </div>

                <div class="form-group">
                    <label class="form-label">Stock Quantity</label>
                    <input type="number" name="stockQuantity" value="${editProduct != null ? editProduct.stockQuantity : 10}" class="form-control" required min="0">
                </div>

                <div class="form-group">
                    <label class="form-label">Image URL</label>
                    <input type="url" name="imageUrl" value="${editProduct.imageUrl}" class="form-control" placeholder="https://images.unsplash.com/...">
                </div>
            </div>

            <div class="form-group">
                <label class="form-label">Full Description</label>
                <textarea name="description" rows="3" class="form-control" placeholder="Specifications, warranty, materials...">${editProduct.description}</textarea>
            </div>

            <div class="form-group" style="display: flex; align-items: center; gap: 0.5rem;">
                <input type="checkbox" name="active" value="true" id="active" ${editProduct == null || editProduct.active ? 'checked' : ''}>
                <label for="active" style="font-size: 0.88rem; cursor: pointer;">Active (Visible in customer store catalog)</label>
            </div>

            <div style="display: flex; gap: 1rem;">
                <button type="submit" class="btn btn-primary btn-sm">
                    ${not empty editProduct ? 'Update Product' : 'Create Product'}
                </button>
                <c:if test="${not empty editProduct}">
                    <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-secondary btn-sm">Cancel Edit</a>
                </c:if>
            </div>
        </form>
    </div>

    <!-- Product Table -->
    <div class="card" style="padding: 0; overflow: hidden;">
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Product</th>
                        <th>Category</th>
                        <th>Price</th>
                        <th>Stock</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${products}">
                        <tr>
                            <td>#${p.id}</td>
                            <td>
                                <div style="display: flex; align-items: center; gap: 0.75rem;">
                                    <img src="${p.imageUrl}" alt="${p.name}" style="width: 44px; height: 44px; object-fit: cover; border-radius: var(--radius-sm);">
                                    <strong style="font-size: 0.95rem;">${p.name}</strong>
                                </div>
                            </td>
                            <td>${p.categoryName}</td>
                            <td style="font-weight: 700;">₹<fmt:formatNumber value="${p.price}" pattern="#,##0.00"/></td>
                            <td>
                                <span class="badge ${p.stockQuantity > 5 ? 'badge-success' : 'badge-danger'}">
                                    ${p.stockQuantity} in stock
                                </span>
                            </td>
                            <td>
                                <span class="badge ${p.active ? 'badge-primary' : 'badge-warning'}">
                                    ${p.active ? 'Active' : 'Hidden'}
                                </span>
                            </td>
                            <td>
                                <div style="display: flex; gap: 0.5rem;">
                                    <a href="${pageContext.request.contextPath}/product?id=${p.id}" target="_blank" class="btn btn-secondary btn-sm" title="View in Customer Store">
                                        <i class="fa-solid fa-arrow-up-right-from-square"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/products?editId=${p.id}" class="btn btn-secondary btn-sm" title="Edit">
                                        <i class="fa-solid fa-pen-to-square"></i>
                                    </a>
                                    <form action="${pageContext.request.contextPath}/admin/products" method="POST" style="margin: 0;">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${p.id}">
                                        <button type="submit" class="btn btn-danger btn-sm" data-confirm="Delete this product?">
                                            <i class="fa-solid fa-trash"></i>
                                        </button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp"/>
