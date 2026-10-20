<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Manage Categories | ApexStore Admin" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container" style="max-width: 1040px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <div>
            <div style="font-size: 0.85rem; font-weight: 700; color: var(--primary); text-transform: uppercase;">Store Taxonomy</div>
            <h1 style="font-size: 2rem; font-weight: 800; letter-spacing: -0.02em;">Category Management</h1>
        </div>
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-secondary btn-sm">
            <i class="fa-solid fa-arrow-left"></i> Back to Dashboard
        </a>
    </div>

    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 2rem; align-items: start;">
        <!-- Categories Table -->
        <div class="card" style="padding: 0; overflow: hidden;">
            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Category</th>
                            <th>Slug</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="cat" items="${categories}">
                            <tr>
                                <td>#${cat.id}</td>
                                <td>
                                    <strong style="font-size: 0.95rem;">${cat.name}</strong>
                                    <div style="font-size: 0.8rem; color: var(--text-muted); display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden;">${cat.description}</div>
                                </td>
                                <td><code style="font-size: 0.8rem;">${cat.slug}</code></td>
                                <td>
                                    <div style="display: flex; gap: 0.5rem;">
                                        <a href="${pageContext.request.contextPath}/admin/categories?editId=${cat.id}" class="btn btn-secondary btn-sm" title="Edit">
                                            <i class="fa-solid fa-pen"></i>
                                        </a>
                                        <form action="${pageContext.request.contextPath}/admin/categories" method="POST" style="margin: 0;">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${cat.id}">
                                            <button type="submit" class="btn btn-danger btn-sm" data-confirm="Delete category?">
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

        <!-- Add/Edit Category Form -->
        <div class="card">
            <h3 class="card-title">
                <c:choose>
                    <c:when test="${not empty editCategory}">
                        Edit Category: ${editCategory.name}
                    </c:when>
                    <c:otherwise>
                        Create New Category
                    </c:otherwise>
                </c:choose>
            </h3>

            <form action="${pageContext.request.contextPath}/admin/categories" method="POST">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="action" value="${not empty editCategory ? 'update' : 'create'}">
                <c:if test="${not empty editCategory}">
                    <input type="hidden" name="id" value="${editCategory.id}">
                </c:if>

                <div class="form-group">
                    <label class="form-label">Category Name</label>
                    <input type="text" name="name" value="${editCategory.name}" class="form-control" required placeholder="e.g. Smart Wearables">
                </div>

                <div class="form-group">
                    <label class="form-label">Image URL</label>
                    <input type="url" name="imageUrl" value="${editCategory.imageUrl}" class="form-control" placeholder="https://images.unsplash.com/...">
                </div>

                <div class="form-group">
                    <label class="form-label">Description</label>
                    <textarea name="description" rows="3" class="form-control" placeholder="Short description of products in this category...">${editCategory.description}</textarea>
                </div>

                <div style="display: flex; gap: 1rem;">
                    <button type="submit" class="btn btn-primary btn-sm">
                        ${not empty editCategory ? 'Update Category' : 'Save Category'}
                    </button>
                    <c:if test="${not empty editCategory}">
                        <a href="${pageContext.request.contextPath}/admin/categories" class="btn btn-secondary btn-sm">Cancel</a>
                    </c:if>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp"/>
