<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="${product.name} | ApexStore" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container" style="margin-top: 1.5rem;">
    <!-- Breadcrumb -->
    <nav style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 2rem;">
        <a href="${pageContext.request.contextPath}/">Home</a> &gt;
        <a href="${pageContext.request.contextPath}/products?category=${product.categoryId}">${product.categoryName}</a> &gt;
        <span style="color: var(--text-main); font-weight: 600;">${product.name}</span>
    </nav>

<style>
/* Single unified gallery box with decreased, well-proportioned photo size */
.product-showcase-grid {
    display: grid;
    grid-template-columns: 420px 1fr;
    gap: 3rem;
    margin-bottom: 3.5rem;
    align-items: start;
}

@media (max-width: 960px) {
    .product-showcase-grid {
        grid-template-columns: 1fr;
        gap: 2rem;
    }
}

.single-gallery-box {
    background: #ffffff;
    border: 1px solid var(--border, #e2e8f0);
    border-radius: 16px;
    padding: 0.9rem;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
    width: 100%;
    max-width: 420px;
    margin: 0 auto;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
}

.gallery-main-view {
    position: relative;
    width: 100%;
    height: 310px;
    background: #f8fafc;
    border-radius: 12px;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid #f1f5f9;
}

.gallery-main-img {
    max-width: 100%;
    max-height: 100%;
    width: 100%;
    height: 100%;
    object-fit: contain;
    transition: opacity 0.2s ease, transform 0.2s ease;
    user-select: none;
    display: block;
}

.gallery-arrow-btn {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.92);
    backdrop-filter: blur(4px);
    -webkit-backdrop-filter: blur(4px);
    border: 1px solid #e2e8f0;
    color: #1e293b;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-size: 0.75rem;
    transition: all 0.2s ease;
    box-shadow: 0 2px 6px rgba(0,0,0,0.12);
    z-index: 5;
    opacity: 0.85;
}

.gallery-arrow-btn:hover {
    background: #ffffff;
    color: var(--primary, #4f46e5);
    opacity: 1;
    transform: translateY(-50%) scale(1.08);
}

.gallery-arrow-btn.prev {
    left: 8px;
}

.gallery-arrow-btn.next {
    right: 8px;
}

.gallery-tag {
    position: absolute;
    bottom: 8px;
    right: 8px;
    background: rgba(15, 23, 42, 0.75);
    backdrop-filter: blur(4px);
    -webkit-backdrop-filter: blur(4px);
    color: #ffffff;
    font-size: 0.72rem;
    font-weight: 600;
    padding: 0.25rem 0.55rem;
    border-radius: 20px;
    display: inline-flex;
    align-items: center;
    gap: 0.35rem;
    pointer-events: none;
}

.gallery-thumbs-strip {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 0.6rem;
    margin-top: 0.85rem;
    padding-top: 0.85rem;
    border-top: 1px solid #f1f5f9;
}

.gallery-mini-thumb {
    width: 56px;
    height: 56px;
    border-radius: 8px;
    border: 2px solid #e2e8f0;
    background: #f8fafc;
    padding: 2px;
    cursor: pointer;
    transition: all 0.18s ease;
    outline: none;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    flex-shrink: 0;
}

.gallery-mini-thumb img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 5px;
    display: block;
}

.gallery-mini-thumb:hover {
    border-color: var(--primary, #4f46e5);
    transform: translateY(-2px);
}

.gallery-mini-thumb.active {
    border-color: var(--primary, #4f46e5);
    box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.3);
    transform: scale(1.05);
}
</style>

    <!-- Product Showcase -->
    <div class="product-showcase-grid">
        <!-- Left: Image Gallery (Single Unified Box, Decreased Size) -->
        <div class="single-gallery-box">
            <div class="gallery-main-view">
                <img id="mainProductImage" 
                     src="${not empty product.images ? product.images[0] : product.imageUrl}" 
                     alt="${fn:escapeXml(product.name)}" 
                     class="gallery-main-img">

                <c:if test="${not empty product.images && fn:length(product.images) > 1}">
                    <button type="button" class="gallery-arrow-btn prev" onclick="navigateGallery(-1)" aria-label="Previous photo">
                        <i class="fa-solid fa-chevron-left"></i>
                    </button>
                    <button type="button" class="gallery-arrow-btn next" onclick="navigateGallery(1)" aria-label="Next photo">
                        <i class="fa-solid fa-chevron-right"></i>
                    </button>
                    <div class="gallery-tag" id="galleryCounterTag">
                        <i class="fa-solid fa-camera"></i> <span id="galleryCounterText">1 / ${fn:length(product.images)}</span>
                    </div>
                </c:if>
            </div>

            <c:if test="${not empty product.images && fn:length(product.images) > 1}">
                <div class="gallery-thumbs-strip" role="tablist" aria-label="Product image thumbnails">
                    <c:forEach var="imgUrl" items="${product.images}" varStatus="status">
                        <button type="button" 
                                class="gallery-mini-thumb ${status.first ? 'active' : ''}" 
                                onclick="switchProductImageByIndex(this.dataset.index)"
                                data-index="${status.index}"
                                title="Photo ${status.count}">
                            <img src="${imgUrl}" alt="${fn:escapeXml(product.name)} thumbnail ${status.count}" loading="lazy">
                        </button>
                    </c:forEach>
                </div>
            </c:if>
        </div>

        <!-- Right: Information & Actions -->
        <div>
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 0.75rem;">
                <span class="product-cat" style="font-size: 0.85rem;">${product.categoryName}</span>
                <c:choose>
                    <c:when test="${product.inStock}">
                        <span class="badge badge-success"><i class="fa-solid fa-check"></i> In Stock (${product.stockQuantity} available)</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge badge-danger"><i class="fa-solid fa-xmark"></i> Out of Stock</span>
                    </c:otherwise>
                </c:choose>
            </div>

            <h1 style="font-size: 2.2rem; font-weight: 800; line-height: 1.2; letter-spacing: -0.02em; margin-bottom: 1rem;">
                ${product.name}
            </h1>

            <div class="product-rating" style="font-size: 0.95rem; margin-bottom: 1.5rem;">
                <div style="color: #f59e0b; display: inline-flex; gap: 2px;">
                    <c:forEach begin="1" end="5" var="star">
                        <c:choose>
                            <c:when test="${star <= product.averageRating}">
                                <i class="fa-solid fa-star" style="color: #f59e0b;"></i>
                            </c:when>
                            <c:otherwise>
                                <i class="fa-solid fa-star" style="color: #e2e8f0;"></i>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </div>
                <strong style="margin-left: 0.5rem;"><fmt:formatNumber value="${product.averageRating}" pattern="0.0"/></strong>
                <span class="rating-count">(${product.reviewCount} customer reviews)</span>
            </div>

            <div style="font-size: 2.4rem; font-weight: 800; color: var(--text-main); margin-bottom: 1.75rem;">
                ₹<fmt:formatNumber value="${product.price}" pattern="#,##0.00"/>
            </div>

            <div style="border-top: 1px solid var(--border); border-bottom: 1px solid var(--border); padding: 1.5rem 0; margin-bottom: 2rem;">
                <h4 style="font-size: 0.95rem; font-weight: 700; margin-bottom: 0.5rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted);">Overview</h4>
                <p style="color: var(--text-muted); line-height: 1.7; font-size: 1rem;">
                    ${product.description}
                </p>
            </div>

            <c:choose>
                <c:when test="${product.inStock}">
                    <form action="${pageContext.request.contextPath}/cart/add" method="POST">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="productId" value="${product.id}">

                        <div style="display: flex; gap: 1rem; align-items: center; margin-bottom: 1.5rem;">
                            <label style="font-weight: 700; font-size: 0.9rem;">Quantity:</label>
                            <div class="qty-control" style="padding: 0.2rem;">
                                <button type="button" class="qty-btn btn-minus">-</button>
                                <input type="number" name="quantity" value="1" min="1" max="${product.stockQuantity}" class="qty-input">
                                <button type="button" class="qty-btn btn-plus">+</button>
                            </div>
                        </div>

                        <div style="display: flex; flex-direction: column; gap: 0.75rem; margin-top: 1rem;">
                            <div style="display: flex; gap: 1rem;">
                                <button type="submit" class="btn btn-secondary btn-lg" style="flex: 1;">
                                    <i class="fa-solid fa-cart-plus"></i> Add to Cart
                                </button>

                                <c:choose>
                                    <c:when test="${not empty sessionScope.currentUser}">
                                        <c:choose>
                                            <c:when test="${inWishlist}">
                                                <button type="submit" formaction="${pageContext.request.contextPath}/wishlist/remove" class="btn btn-secondary btn-lg" title="Remove from Wishlist" style="color: var(--accent); border-color: var(--accent);">
                                                    <i class="fa-solid fa-heart" style="color: var(--accent);"></i>
                                                </button>
                                            </c:when>
                                            <c:otherwise>
                                                <button type="submit" formaction="${pageContext.request.contextPath}/wishlist/add" class="btn btn-secondary btn-lg" title="Save to Wishlist">
                                                    <i class="fa-regular fa-heart"></i>
                                                </button>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="${pageContext.request.contextPath}/login" class="btn btn-secondary btn-lg" title="Sign in to save to Wishlist" style="display: inline-flex; align-items: center; justify-content: center;">
                                            <i class="fa-regular fa-heart"></i>
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <button type="button" class="btn btn-buy-now btn-lg btn-block detail-buy-now-btn"
                                    id="detailBuyNowBtn"
                                    data-id="${product.id}"
                                    data-name="${fn:escapeXml(product.name)}"
                                    data-price="${product.price}"
                                    data-image="${product.imageUrl}"
                                    data-stock="${product.stockQuantity}"
                                    title="Buy Now with Cash on Delivery or Online Payment">
                                <i class="fa-solid fa-bolt"></i> Buy Now (COD & Online Payment)
                            </button>
                        </div>
                    </form>
                </c:when>
                <c:otherwise>
                    <div style="display: flex; gap: 1rem; margin-top: 1rem;">
                        <button type="button" class="btn btn-secondary btn-lg" disabled style="flex: 1; opacity: 0.65; cursor: not-allowed;">
                            <i class="fa-solid fa-ban"></i> Currently Out of Stock
                        </button>
                        <c:choose>
                            <c:when test="${not empty sessionScope.currentUser}">
                                <c:choose>
                                    <c:when test="${inWishlist}">
                                        <form action="${pageContext.request.contextPath}/wishlist/remove" method="POST" style="margin: 0;">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="productId" value="${product.id}">
                                            <button type="submit" class="btn btn-secondary btn-lg" title="Remove from Wishlist" style="color: var(--accent); border-color: var(--accent);">
                                                <i class="fa-solid fa-heart" style="color: var(--accent);"></i>
                                            </button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <form action="${pageContext.request.contextPath}/wishlist/add" method="POST" style="margin: 0;">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="productId" value="${product.id}">
                                            <button type="submit" class="btn btn-secondary btn-lg" title="Save to Wishlist">
                                                <i class="fa-regular fa-heart"></i>
                                            </button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:otherwise>
                                <a href="${pageContext.request.contextPath}/login" class="btn btn-secondary btn-lg" title="Sign in to save to Wishlist" style="display: inline-flex; align-items: center; justify-content: center;">
                                    <i class="fa-regular fa-heart"></i>
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- Customer Reviews Section -->
    <section class="card" style="padding: 2.5rem; margin-top: 3rem;">
        <div class="section-header" style="margin-bottom: 2rem;">
            <div>
                <h3 style="font-size: 1.6rem; font-weight: 800;">Customer Reviews</h3>
                <p style="color: var(--text-muted); font-size: 0.9rem;">Real feedback from authenticated store shoppers</p>
            </div>
        </div>

        <c:if test="${canReview}">
            <!-- Review submission form -->
            <div style="background: var(--bg-surface-alt); border-radius: var(--radius-lg); padding: 1.75rem; margin-bottom: 2.5rem; border: 1px solid var(--border);">
                <h4 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 1rem;">Leave a Product Review</h4>
                <form action="${pageContext.request.contextPath}/reviews" method="POST">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="productId" value="${product.id}">

                    <div class="form-group">
                        <label class="form-label">Your Rating (1 to 5 Stars)</label>
                        <select name="rating" class="form-control" style="max-width: 200px;">
                            <option value="5">★★★★★ 5 - Excellent</option>
                            <option value="4">★★★★☆ 4 - Good</option>
                            <option value="3">★★★☆☆ 3 - Average</option>
                            <option value="2">★★☆☆☆ 2 - Poor</option>
                            <option value="1">★☆☆☆☆ 1 - Terrible</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Review Comment</label>
                        <textarea name="comment" rows="4" class="form-control" placeholder="Share your hands-on experience with this product..." required></textarea>
                    </div>

                    <button type="submit" class="btn btn-primary btn-sm">Submit Review</button>
                </form>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${not empty reviews}">
                <div style="display: flex; flex-direction: column; gap: 1.5rem;">
                    <c:forEach var="rev" items="${reviews}">
                        <div style="border-bottom: 1px solid var(--border); padding-bottom: 1.5rem;">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                                <div>
                                    <strong style="font-size: 1rem;">${rev.userName}</strong>
                                    <span class="badge badge-success" style="font-size: 0.7rem; margin-left: 0.5rem;">
                                        <i class="fa-solid fa-circle-check"></i> Verified Buyer
                                    </span>
                                </div>
                                <span style="font-size: 0.8rem; color: var(--text-muted);"><fmt:formatDate value="${rev.createdAt}" pattern="MMM dd, yyyy"/></span>
                            </div>
                            <div style="color: #f59e0b; font-size: 0.85rem; margin-bottom: 0.5rem;">
                                <c:forEach begin="1" end="${rev.rating}">
                                    <i class="fa-solid fa-star"></i>
                                </c:forEach>
                            </div>
                            <p style="color: var(--text-main); font-size: 0.95rem; line-height: 1.6;">
                                ${rev.comment}
                            </p>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <p style="color: var(--text-muted); font-size: 0.95rem; text-align: center; padding: 2rem 0;">
                    No reviews yet. Be the first to purchase and review this product!
                </p>
            </c:otherwise>
        </c:choose>
    </section>
</div>

<!-- Gallery Images Config (Kept in HTML to prevent script syntax errors in IDE) -->
<div id="galleryData" style="display:none;" data-default-image="${product.imageUrl}">
    <c:forEach var="img" items="${product.images}">
        <span class="gallery-item-src" data-src="${img}"></span>
    </c:forEach>
</div>

<script>
(function() {
    var galleryImages = [];
    var itemNodes = document.querySelectorAll('#galleryData .gallery-item-src');
    itemNodes.forEach(function(node) {
        if (node.dataset && node.dataset.src) {
            galleryImages.push(node.dataset.src);
        }
    });
    if (galleryImages.length === 0) {
        var fallbackContainer = document.getElementById('galleryData');
        var fallbackImg = fallbackContainer && fallbackContainer.dataset ? fallbackContainer.dataset.defaultImage : '';
        if (fallbackImg) {
            galleryImages.push(fallbackImg);
        }
    }
    var currentIndex = 0;

    window.switchProductImageByIndex = function(index) {
        index = Number(index);
        if (isNaN(index) || index < 0 || index >= galleryImages.length) return;
        currentIndex = index;
        var mainImg = document.getElementById('mainProductImage');
        if (!mainImg) return;

        mainImg.style.opacity = '0.3';
        mainImg.style.transform = 'scale(0.97)';
        setTimeout(function() {
            mainImg.src = galleryImages[index];
            mainImg.style.opacity = '1';
            mainImg.style.transform = 'scale(1)';
        }, 120);

        var thumbs = document.querySelectorAll('.gallery-mini-thumb');
        thumbs.forEach(function(t, i) {
            if (i === index) {
                t.classList.add('active');
            } else {
                t.classList.remove('active');
            }
        });

        var counterText = document.getElementById('galleryCounterText');
        if (counterText) {
            counterText.textContent = (index + 1) + ' / ' + galleryImages.length;
        }
    };

    window.navigateGallery = function(direction) {
        if (galleryImages.length <= 1) return;
        var next = currentIndex + Number(direction);
        if (next < 0) next = galleryImages.length - 1;
        if (next >= galleryImages.length) next = 0;
        window.switchProductImageByIndex(next);
    };
})();
</script>

<jsp:include page="../common/footer.jsp"/>
