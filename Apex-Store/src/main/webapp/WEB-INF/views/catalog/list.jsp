<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Explore Products | ApexStore" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <div>
            <h1 style="font-size: 2rem; font-weight: 800; letter-spacing: -0.02em;">Product Catalog</h1>
            <p style="color: var(--text-muted); font-size: 0.95rem;">Showing ${totalCount} available items</p>
        </div>
        <c:if test="${not empty keyword || not empty selectedCategory || not empty minPrice || not empty maxPrice}">
            <a href="${pageContext.request.contextPath}/products" class="btn btn-secondary btn-sm">
                <i class="fa-solid fa-rotate-left"></i> Reset Filters
            </a>
        </c:if>
    </div>

    <div style="display: grid; grid-template-columns: 260px 1fr; gap: 2rem; align-items: start;">
        <!-- Filters Sidebar -->
        <aside class="card" style="padding: 1.5rem; position: sticky; top: 5.5rem;">
            <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 1.25rem; display: flex; align-items: center; gap: 0.5rem;">
                <i class="fa-solid fa-sliders"></i> Filter Results
            </h3>

            <form action="${pageContext.request.contextPath}/products" method="GET">
                <!-- Search term -->
                <div class="form-group">
                    <label class="form-label" style="font-size: 0.8rem;">Keyword</label>
                    <input type="text" name="q" value="${keyword}" class="form-control" placeholder="Search title, spec...">
                </div>

                <!-- Category -->
                <div class="form-group">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.4rem;">
                        <label class="form-label" style="font-size: 0.8rem; margin-bottom: 0;">Department / Category</label>
                        <c:if test="${not empty selectedCategory}">
                            <a href="${pageContext.request.contextPath}/products" style="font-size: 0.75rem; color: var(--primary); font-weight: 600;">Clear</a>
                        </c:if>
                    </div>
                    <select name="category" class="form-control" onchange="this.form.submit()">
                        <option value="">All Categories</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat.id}" ${selectedCategory == cat.id ? 'selected' : ''}>${cat.name}</option>
                        </c:forEach>
                    </select>

                    <!-- Sidebar Category Quick List -->
                    <div class="category-filter-list">
                        <a href="${pageContext.request.contextPath}/products" class="category-filter-item ${empty selectedCategory ? 'active' : ''}">
                            <span class="category-filter-item-left">
                                <i class="fa-solid fa-border-all"></i>
                                <span>All Categories</span>
                            </span>
                        </a>
                        <c:forEach var="cat" items="${categories}">
                            <a href="${pageContext.request.contextPath}/products?category=${cat.id}" class="category-filter-item ${selectedCategory == cat.id ? 'active' : ''}">
                                <span class="category-filter-item-left">
                                    <i class="fa-solid ${cat.iconClass}"></i>
                                    <span>${cat.name}</span>
                                </span>
                                <c:if test="${cat.itemCount > 0}">
                                    <span class="category-filter-count">${cat.itemCount}</span>
                                </c:if>
                            </a>
                        </c:forEach>
                    </div>
                </div>

                <!-- Price Min & Max -->
                <div class="form-group">
                    <label class="form-label" style="font-size: 0.8rem;">Price Range (₹)</label>
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem;">
                        <input type="number" step="0.01" name="minPrice" value="${minPrice}" placeholder="Min ₹" class="form-control" style="padding: 0.5rem 0.75rem;">
                        <input type="number" step="0.01" name="maxPrice" value="${maxPrice}" placeholder="Max ₹" class="form-control" style="padding: 0.5rem 0.75rem;">
                    </div>
                </div>

                <!-- Sort By -->
                <div class="form-group">
                    <label class="form-label" style="font-size: 0.8rem;">Sort By</label>
                    <select name="sortBy" class="form-control">
                        <option value="p.created_at" ${sortBy == 'p.created_at' ? 'selected' : ''}>Newest Arrivals</option>
                        <option value="p.price" ${sortBy == 'p.price' && sortOrder == 'ASC' ? 'selected' : ''}>Price: Low to High</option>
                        <option value="p.price" ${sortBy == 'p.price' && sortOrder == 'DESC' ? 'selected' : ''}>Price: High to Low</option>
                        <option value="p.name" ${sortBy == 'p.name' ? 'selected' : ''}>Product Name</option>
                    </select>
                </div>

                <!-- Sort Order hidden or mapped -->
                <input type="hidden" name="sortOrder" value="${sortBy == 'p.price' && sortOrder == 'ASC' ? 'ASC' : (sortBy == 'p.name' ? 'ASC' : 'DESC')}">

                <button type="submit" class="btn btn-primary btn-block" style="padding: 0.65rem; margin-top: 0.5rem;">
                    Apply Filters
                </button>
            </form>
        </aside>

        <!-- Product Listing Grid -->
        <div>
            <!-- Horizontal Category Filter Pills Bar -->
            <div class="category-pills-bar">
                <a href="${pageContext.request.contextPath}/products" class="category-pill ${empty selectedCategory ? 'active' : ''}">
                    <i class="fa-solid fa-border-all"></i>
                    <span>All Products</span>
                    <span class="pill-count">${totalCount}</span>
                </a>
                <c:forEach var="cat" items="${categories}">
                    <a href="${pageContext.request.contextPath}/products?category=${cat.id}" class="category-pill ${selectedCategory == cat.id ? 'active' : ''}">
                        <i class="fa-solid ${cat.iconClass}"></i>
                        <span>${cat.name}</span>
                        <c:if test="${cat.itemCount > 0}">
                            <span class="pill-count">${cat.itemCount}</span>
                        </c:if>
                    </a>
                </c:forEach>
            </div>

            <!-- Active Category Spotlight Banner -->
            <c:if test="${not empty activeCategory}">
                <div class="category-header-banner">
                    <div class="category-banner-icon">
                        <i class="fa-solid ${activeCategory.iconClass}"></i>
                    </div>
                    <div class="category-banner-content">
                        <div style="font-size: 0.78rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; color: #a5b4fc; margin-bottom: 0.2rem;">
                            Department Collection
                        </div>
                        <h2 class="category-banner-title">${activeCategory.name}</h2>
                        <p class="category-banner-desc">${activeCategory.description}</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/products" class="btn btn-secondary btn-sm" style="font-weight: 700; background: rgba(255, 255, 255, 0.18); border-color: rgba(255,255,255,0.3); color: white;">
                        <i class="fa-solid fa-xmark"></i> Clear Category
                    </a>
                </div>
            </c:if>
            <c:choose>
                <c:when test="${not empty products}">
                    <div class="product-grid" style="margin-bottom: 2rem;">
                        <c:forEach var="prod" items="${products}">
                            <div class="product-card" data-href="${pageContext.request.contextPath}/product?id=${prod.id}">
                                <div class="product-thumb">
                                    <a href="${pageContext.request.contextPath}/product?id=${prod.id}" class="product-thumb-link" title="${prod.name}">
                                        <img src="${prod.imageUrl}" alt="${prod.name}">
                                        <div class="product-thumb-overlay">
                                            <span><i class="fa-regular fa-eye"></i> View Details</span>
                                        </div>
                                    </a>
                                    <span class="product-badge">${prod.categoryName}</span>

                                    <c:choose>
                                        <c:when test="${not empty sessionScope.currentUser}">
                                            <form action="${pageContext.request.contextPath}/wishlist/add" method="POST" style="margin: 0;">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                <input type="hidden" name="productId" value="${prod.id}">
                                                <button type="submit" class="wishlist-toggle" title="Add to Wishlist">
                                                    <i class="fa-regular fa-heart"></i>
                                                </button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="${pageContext.request.contextPath}/login" class="wishlist-toggle" title="Sign in to save to Wishlist">
                                                <i class="fa-regular fa-heart"></i>
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="product-info">
                                    <span class="product-cat">${prod.categoryName}</span>
                                    <a href="${pageContext.request.contextPath}/product?id=${prod.id}">
                                        <h3 class="product-name">${prod.name}</h3>
                                    </a>

                                    <div class="product-rating">
                                        <i class="fa-solid fa-star"></i>
                                        <span><fmt:formatNumber value="${prod.averageRating}" pattern="0.0"/></span>
                                        <span class="rating-count">(${prod.reviewCount})</span>
                                        <c:choose>
                                            <c:when test="${prod.inStock}">
                                                <span style="margin-left: auto; font-size: 0.75rem; color: var(--success); font-weight: 700;">In Stock</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="margin-left: auto; font-size: 0.75rem; color: var(--danger); font-weight: 700;">Out of Stock</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="product-footer">
                                        <span class="product-price">₹<fmt:formatNumber value="${prod.price}" pattern="#,##0.00"/></span>

                                        <div class="product-card-actions">
                                            <a href="${pageContext.request.contextPath}/product?id=${prod.id}" class="btn-view-details" title="View Product Details">
                                                <i class="fa-regular fa-eye"></i> Details
                                            </a>

                                            <c:if test="${prod.inStock}">
                                                <form action="${pageContext.request.contextPath}/cart/add" method="POST" style="margin: 0;">
                                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                    <input type="hidden" name="productId" value="${prod.id}">
                                                    <input type="hidden" name="quantity" value="1">
                                                    <button type="submit" class="btn btn-secondary btn-sm" title="Add to Cart">
                                                        <i class="fa-solid fa-cart-plus"></i> Add
                                                    </button>
                                                </form>

                                                <button type="button" class="btn btn-buy-now btn-sm"
                                                        data-id="${prod.id}"
                                                        data-name="${prod.name}"
                                                        data-price="${prod.price}"
                                                        data-image="${prod.imageUrl}"
                                                        data-stock="${prod.stockQuantity}"
                                                        title="Instant Buy Now (COD & Online Payment)">
                                                    <i class="fa-solid fa-bolt"></i> Buy Now
                                                </button>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>

                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <div style="display: flex; justify-content: center; gap: 0.5rem; margin-top: 2rem;">
                            <c:if test="${currentPage > 1}">
                                <a href="${pageContext.request.contextPath}/products?page=${currentPage - 1}&q=${keyword}&category=${selectedCategory}&minPrice=${minPrice}&maxPrice=${maxPrice}&sortBy=${sortBy}&sortOrder=${sortOrder}"
                                   class="btn btn-secondary btn-sm"><i class="fa-solid fa-chevron-left"></i> Prev</a>
                            </c:if>

                            <c:forEach begin="1" end="${totalPages}" var="p">
                                <a href="${pageContext.request.contextPath}/products?page=${p}&q=${keyword}&category=${selectedCategory}&minPrice=${minPrice}&maxPrice=${maxPrice}&sortBy=${sortBy}&sortOrder=${sortOrder}"
                                   class="btn ${p == currentPage ? 'btn-primary' : 'btn-secondary'} btn-sm" style="min-width: 2.2rem;">
                                    ${p}
                                </a>
                            </c:forEach>

                            <c:if test="${currentPage < totalPages}">
                                <a href="${pageContext.request.contextPath}/products?page=${currentPage + 1}&q=${keyword}&category=${selectedCategory}&minPrice=${minPrice}&maxPrice=${maxPrice}&sortBy=${sortBy}&sortOrder=${sortOrder}"
                                   class="btn btn-secondary btn-sm">Next <i class="fa-solid fa-chevron-right"></i></a>
                            </c:if>
                        </div>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <!-- Empty State -->
                    <div class="card" style="text-align: center; padding: 4rem 2rem;">
                        <div style="font-size: 3rem; color: var(--text-light); margin-bottom: 1rem;"><i class="fa-solid fa-box-open"></i></div>
                        <h3 style="font-size: 1.3rem; font-weight: 700; margin-bottom: 0.5rem;">No products found</h3>
                        <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.5rem;">Try adjusting your keywords, price limits, or selecting a different category.</p>
                        <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-sm">View All Products</a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp"/>
