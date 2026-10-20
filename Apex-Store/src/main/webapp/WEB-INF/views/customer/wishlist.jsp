<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="My Wishlist | ApexStore" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container">
    <div class="section-header">
        <div>
            <h1 style="font-size: 2rem; font-weight: 800; letter-spacing: -0.02em;">My Wishlist</h1>
            <p style="color: var(--text-muted); font-size: 0.95rem;">Saved favorite items to purchase later</p>
        </div>
    </div>

    <c:choose>
        <c:when test="${not empty wishlistItems}">
            <div class="product-grid">
                <c:forEach var="w" items="${wishlistItems}">
                    <div class="product-card" data-href="${pageContext.request.contextPath}/product?id=${w.productId}">
                        <div class="product-thumb">
                            <a href="${pageContext.request.contextPath}/product?id=${w.productId}" class="product-thumb-link" title="${w.product.name}">
                                <img src="${w.product.imageUrl}" alt="${w.product.name}">
                                <div class="product-thumb-overlay">
                                    <span><i class="fa-regular fa-eye"></i> View Details</span>
                                </div>
                            </a>

                            <form action="${pageContext.request.contextPath}/wishlist/remove" method="POST" style="margin: 0;">
                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                <input type="hidden" name="productId" value="${w.productId}">
                                <button type="submit" class="wishlist-toggle active" title="Remove from Wishlist">
                                    <i class="fa-solid fa-heart" style="color: var(--accent);"></i>
                                </button>
                            </form>
                        </div>

                        <div class="product-info">
                            <a href="${pageContext.request.contextPath}/product?id=${w.productId}">
                                <h3 class="product-name">${w.product.name}</h3>
                            </a>

                            <div class="product-footer" style="margin-top: 1rem;">
                                <span class="product-price">₹<fmt:formatNumber value="${w.product.price}" pattern="#,##0.00"/></span>

                                <div class="product-card-actions">
                                    <a href="${pageContext.request.contextPath}/product?id=${w.productId}" class="btn-view-details" title="View Product Details">
                                        <i class="fa-regular fa-eye"></i> Details
                                    </a>

                                    <form action="${pageContext.request.contextPath}/cart/add" method="POST" style="margin: 0;">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="productId" value="${w.productId}">
                                        <input type="hidden" name="quantity" value="1">
                                        <button type="submit" class="btn btn-secondary btn-sm" title="Move to Cart">
                                            <i class="fa-solid fa-cart-plus"></i> Move to Cart
                                        </button>
                                    </form>

                                    <button type="button" class="btn btn-buy-now btn-sm"
                                            data-id="${w.productId}"
                                            data-name="<c:out value='${w.product.name}'/>"
                                            data-price="${w.product.price}"
                                            data-image="${w.product.imageUrl}"
                                            data-stock="${w.product.stockQuantity}"
                                            title="Instant Buy Now (COD & Online Payment)">
                                        <i class="fa-solid fa-bolt"></i> Buy Now
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <div class="card" style="text-align: center; padding: 4rem 2rem;">
                <div style="font-size: 3rem; color: var(--text-light); margin-bottom: 1rem;"><i class="fa-regular fa-heart"></i></div>
                <h3 style="font-size: 1.3rem; font-weight: 700; margin-bottom: 0.5rem;">Your wishlist is empty</h3>
                <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.5rem;">Tap the heart icon on any product to save it here.</p>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-sm">Browse Products</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../common/footer.jsp"/>
