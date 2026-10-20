<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ApexStore | Next-Gen E-Commerce" scope="request"/>
<jsp:include page="common/header.jsp"/>

<div class="container">
    <!-- Hero Banner -->
    <section class="hero-banner">
        <div class="hero-text">
            <span class="hero-badge"><i class="fa-solid fa-sparkles"></i> Spring Flagship 2026 Collection</span>
            <h1 class="hero-title">Future-Ready Tech & Modern Essentials.</h1>
            <p class="hero-subtitle">
                Discover exceptional hardware, studio audio, and tailored styles built with uncompromised engineering and artisanal detail.
            </p>
            <div class="btn-group">
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-lg">
                    <i class="fa-solid fa-bag-shopping"></i> Shop Now
                </a>
                <a href="${pageContext.request.contextPath}/products?category=1" class="btn btn-secondary btn-lg">
                    <i class="fa-solid fa-bolt"></i> View Electronics
                </a>
            </div>
        </div>
    </section>

    <!-- Coupon Highlights Strip -->
    <div style="background: linear-gradient(135deg, #4f46e5, #7c3aed); border-radius: var(--radius-lg); padding: 1.25rem 2rem; color: white; margin-bottom: 3.5rem; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem; box-shadow: var(--shadow-md);">
        <div style="display: flex; align-items: center; gap: 1rem;">
            <i class="fa-solid fa-tag" style="font-size: 1.8rem; color: #fde047;"></i>
            <div>
                <strong style="font-size: 1.1rem; display: block;">Special Launch Offer!</strong>
                <span style="font-size: 0.88rem; color: #e0e7ff;">Use coupon code <code style="background: rgba(0,0,0,0.25); padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: 700; color: #fde047;">WELCOME10</code> for 10% off on your first order.</span>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-secondary btn-sm" style="font-weight: 700;">Claim Discount</a>
    </div>

    <!-- Category Showcase -->
    <section>
        <div class="section-header">
            <div>
                <span style="font-size: 0.85rem; font-weight: 700; color: var(--primary); text-transform: uppercase; letter-spacing: 0.05em; display: block; margin-bottom: 0.25rem;">
                    <i class="fa-solid fa-layer-group"></i> Curated Collections
                </span>
                <h2 class="section-title">Shop by Category</h2>
                <p class="section-desc">Browse our curated selection across popular lifestyle & tech divisions</p>
            </div>
            <a href="${pageContext.request.contextPath}/products" class="view-all-link">Browse All <i class="fa-solid fa-arrow-right"></i></a>
        </div>

        <div class="category-grid">
            <c:forEach var="cat" items="${categories}">
                <a href="${pageContext.request.contextPath}/products?category=${cat.id}" class="category-card">
                    <div class="category-thumb-wrapper">
                        <img src="${cat.imageUrl}" alt="${cat.name}" class="category-image" loading="lazy">
                        <div class="category-overlay"></div>
                    </div>
                    <span class="category-badge-chip">
                        <i class="fa-solid ${cat.iconClass}"></i>
                        <c:choose>
                            <c:when test="${cat.itemCount > 0}">${cat.itemCount} Items</c:when>
                            <c:otherwise>Explore</c:otherwise>
                        </c:choose>
                    </span>
                    <div class="category-content">
                        <div class="category-icon-bubble">
                            <i class="fa-solid ${cat.iconClass}"></i>
                        </div>
                        <h3 class="category-name">${cat.name}</h3>
                        <p class="category-desc">${cat.description}</p>
                        <span class="category-cta">
                            Shop Department <i class="fa-solid fa-arrow-right"></i>
                        </span>
                    </div>
                </a>
            </c:forEach>
        </div>
    </section>

    <!-- Featured Products Grid -->
    <section>
        <div class="section-header">
            <div>
                <h2 class="section-title">Featured Products</h2>
                <p class="section-desc">Top rated hand-selected picks with guaranteed authenticity</p>
            </div>
            <a href="${pageContext.request.contextPath}/products" class="view-all-link">View Catalog <i class="fa-solid fa-arrow-right"></i></a>
        </div>

        <div class="product-grid">
            <c:forEach var="prod" items="${featuredProducts}">
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
                            <span class="rating-count">(${prod.reviewCount} reviews)</span>
                        </div>

                        <div class="product-footer">
                            <span class="product-price">₹<fmt:formatNumber value="${prod.price}" pattern="#,##0.00"/></span>

                            <div class="product-card-actions">
                                <a href="${pageContext.request.contextPath}/product?id=${prod.id}" class="btn-view-details" title="View Product Details">
                                    <i class="fa-regular fa-eye"></i> Details
                                </a>

                                <form action="${pageContext.request.contextPath}/cart/add" method="POST" style="margin: 0;">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                    <input type="hidden" name="productId" value="${prod.id}">
                                    <input type="hidden" name="quantity" value="1">
                                    <button type="submit" class="btn btn-secondary btn-sm" title="Add to Cart">
                                        <i class="fa-solid fa-plus"></i> Add
                                    </button>
                                </form>

                                <button type="button" class="btn btn-buy-now btn-sm"
                                        data-id="${prod.id}"
                                        data-name="<c:out value='${prod.name}'/>"
                                        data-price="${prod.price}"
                                        data-image="${prod.imageUrl}"
                                        data-stock="${prod.stockQuantity}"
                                        title="Instant Buy Now (COD & Online Payment)">
                                    <i class="fa-solid fa-bolt"></i> Buy Now
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </section>

    <!-- Value Propositions -->
    <section style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1.5rem; margin-top: 2rem; margin-bottom: 3rem;">
        <div class="card" style="display: flex; gap: 1rem; align-items: center; margin-bottom: 0;">
            <div style="font-size: 2rem; color: var(--primary);"><i class="fa-solid fa-truck-fast"></i></div>
            <div>
                <h4 style="font-weight: 700;">Fast & Free Shipping</h4>
                <p style="font-size: 0.85rem; color: var(--text-muted);">Complimentary delivery on all orders over ₹999</p>
            </div>
        </div>
        <div class="card" style="display: flex; gap: 1rem; align-items: center; margin-bottom: 0;">
            <div style="font-size: 2rem; color: var(--success);"><i class="fa-solid fa-handshake-simple"></i></div>
            <div>
                <h4 style="font-weight: 700;">Cash on Delivery</h4>
                <p style="font-size: 0.85rem; color: var(--text-muted);">Inspect before paying at your doorstep</p>
            </div>
        </div>
        <div class="card" style="display: flex; gap: 1rem; align-items: center; margin-bottom: 0;">
            <div style="font-size: 2rem; color: var(--warning);"><i class="fa-solid fa-shield-halved"></i></div>
            <div>
                <h4 style="font-weight: 700;">Secure Transactions</h4>
                <p style="font-size: 0.85rem; color: var(--text-muted);">Strict JDBC atomic isolation & transactions</p>
            </div>
        </div>
        <div class="card" style="display: flex; gap: 1rem; align-items: center; margin-bottom: 0;">
            <div style="font-size: 2rem; color: var(--accent);"><i class="fa-solid fa-headset"></i></div>
            <div>
                <h4 style="font-weight: 700;">24/7 Dedicated Support</h4>
                <p style="font-size: 0.85rem; color: var(--text-muted);">Round-the-clock friendly technical help</p>
            </div>
        </div>
    </section>
</div>

<jsp:include page="common/footer.jsp"/>
