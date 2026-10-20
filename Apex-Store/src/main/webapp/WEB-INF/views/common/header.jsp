<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle != null ? pageTitle : 'ApexStore | Premium Tech & Lifestyle'}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=2.3">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
</head>
<body>

<header class="site-header">
    <div class="container nav-wrapper">
        <!-- Logo -->
        <a href="${pageContext.request.contextPath}/" class="brand-logo">
            <span class="brand-icon"><i class="fa-solid fa-cube"></i></span>
            <span>Apex<span class="brand-accent">Store</span></span>
        </a>

        <!-- Search Bar -->
        <div class="nav-search">
            <form action="${pageContext.request.contextPath}/products" method="GET" class="search-form">
                <input type="text" name="q" value="${param.q}" placeholder="Search laptops, audio, fashion, gaming..." class="search-input" required>
                <button type="submit" class="search-btn"><i class="fa-solid fa-magnifying-glass"></i> Search</button>
            </form>
        </div>

        <!-- Navigation Actions -->
        <nav class="nav-actions">
            <a href="${pageContext.request.contextPath}/products" class="nav-link">
                <i class="fa-solid fa-compass"></i> Explore
            </a>

            <c:choose>
                <c:when test="${not empty sessionScope.currentUser}">
                    <a href="${pageContext.request.contextPath}/wishlist" class="nav-link" title="Wishlist">
                        <i class="fa-regular fa-heart"></i>
                        <span class="nav-link-text">Wishlist</span>
                    </a>

                    <a href="${pageContext.request.contextPath}/cart" class="nav-link" title="Cart">
                        <i class="fa-solid fa-cart-shopping"></i>
                        <span class="nav-link-text">Cart</span>
                    </a>

                    <!-- User Dropdown Menu -->
                    <div class="user-dropdown">
                        <button class="user-btn">
                            <i class="fa-regular fa-circle-user"></i>
                            <span>${sessionScope.currentUser.name}</span>
                            <i class="fa-solid fa-chevron-down" style="font-size: 0.7rem;"></i>
                        </button>
                        <div class="dropdown-menu">
                            <c:if test="${sessionScope.currentUser.admin}">
                                <a href="${pageContext.request.contextPath}/admin/dashboard" class="dropdown-item" style="color: var(--primary); font-weight: 700;">
                                    <i class="fa-solid fa-gauge-high"></i> Admin Dashboard
                                </a>
                                <div class="dropdown-divider"></div>
                            </c:if>
                            <a href="${pageContext.request.contextPath}/orders" class="dropdown-item">
                                <i class="fa-solid fa-box-archive"></i> My Orders
                            </a>
                            <a href="${pageContext.request.contextPath}/addresses" class="dropdown-item">
                                <i class="fa-solid fa-location-dot"></i> Saved Addresses
                            </a>
                            <a href="${pageContext.request.contextPath}/profile" class="dropdown-item">
                                <i class="fa-solid fa-id-badge"></i> Account Settings
                            </a>
                            <div class="dropdown-divider"></div>
                            <a href="${pageContext.request.contextPath}/logout" class="dropdown-item" style="color: var(--danger);">
                                <i class="fa-solid fa-arrow-right-from-bracket"></i> Sign Out
                            </a>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login" class="nav-link">
                        <i class="fa-solid fa-arrow-right-to-bracket"></i> Sign In
                    </a>
                    <a href="${pageContext.request.contextPath}/register" class="btn btn-primary btn-sm">
                        Create Account
                    </a>
                </c:otherwise>
            </c:choose>
        </nav>
    </div>

    <!-- Category bar -->
    <div class="category-bar">
        <div class="container">
            <a href="${pageContext.request.contextPath}/products" class="cat-link ${empty param.category ? 'active' : ''}"><i class="fa-solid fa-border-all"></i> All Categories</a>
            <a href="${pageContext.request.contextPath}/products?category=1" class="cat-link ${param.category == '1' ? 'active' : ''}"><i class="fa-solid fa-laptop"></i> Electronics</a>
            <a href="${pageContext.request.contextPath}/products?category=2" class="cat-link ${param.category == '2' ? 'active' : ''}"><i class="fa-solid fa-shirt"></i> Fashion</a>
            <a href="${pageContext.request.contextPath}/products?category=3" class="cat-link ${param.category == '3' ? 'active' : ''}"><i class="fa-solid fa-kitchen-set"></i> Home & Kitchen</a>
            <a href="${pageContext.request.contextPath}/products?category=4" class="cat-link ${param.category == '4' ? 'active' : ''}"><i class="fa-solid fa-headphones"></i> Audio</a>
            <a href="${pageContext.request.contextPath}/products?category=5" class="cat-link ${param.category == '5' ? 'active' : ''}"><i class="fa-solid fa-dumbbell"></i> Sports</a>
            <a href="${pageContext.request.contextPath}/contact" class="cat-link" style="margin-left: auto;"><i class="fa-regular fa-comment-dots"></i> Support & FAQ</a>
        </div>
    </div>
</header>

<!-- Global Notifications & Flash messages -->
<div class="container" style="margin-top: 1.5rem;">
    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success">
            <span><i class="fa-solid fa-circle-check"></i> ${flashSuccess}</span>
            <button onclick="this.parentElement.remove()" style="background:none; border:none; cursor:pointer;"><i class="fa-solid fa-xmark"></i></button>
        </div>
        <c:remove var="flashSuccess" scope="session"/>
    </c:if>

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            <span><i class="fa-solid fa-circle-check"></i> ${successMessage}</span>
            <button onclick="this.parentElement.remove()" style="background:none; border:none; cursor:pointer;"><i class="fa-solid fa-xmark"></i></button>
        </div>
    </c:if>

    <c:if test="${not empty flashError}">
        <div class="alert alert-danger">
            <span><i class="fa-solid fa-circle-exclamation"></i> ${flashError}</span>
            <button onclick="this.parentElement.remove()" style="background:none; border:none; cursor:pointer;"><i class="fa-solid fa-xmark"></i></button>
        </div>
        <c:remove var="flashError" scope="session"/>
    </c:if>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            <span><i class="fa-solid fa-circle-exclamation"></i> ${errorMessage}</span>
            <button onclick="this.parentElement.remove()" style="background:none; border:none; cursor:pointer;"><i class="fa-solid fa-xmark"></i></button>
        </div>
    </c:if>
</div>

<main class="main-content">
