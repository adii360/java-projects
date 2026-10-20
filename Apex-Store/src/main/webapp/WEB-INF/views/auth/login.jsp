<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Sign In | ApexStore" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container" style="max-width: 480px; margin-top: 2rem; margin-bottom: 3rem;">
    <div class="card" style="box-shadow: var(--shadow-lg); padding: 2.5rem 2rem;">
        <div style="text-align: center; margin-bottom: 2rem;">
            <div class="brand-icon" style="margin: 0 auto 1rem; width: 3rem; height: 3rem; font-size: 1.5rem;">
                <i class="fa-solid fa-cube"></i>
            </div>
            <h1 style="font-size: 1.8rem; font-weight: 800; letter-spacing: -0.02em;">Welcome Back</h1>
            <p style="color: var(--text-muted); font-size: 0.9rem; margin-top: 0.25rem;">Sign in to access your orders, cart, and wishlist</p>
        </div>

        <form action="${pageContext.request.contextPath}/login" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}">

            <div class="form-group">
                <label class="form-label" for="email">Email Address</label>
                <div style="position: relative;">
                    <input type="email" id="email" name="email" value="${email}" class="form-control" placeholder="name@example.com" required autofocus>
                </div>
            </div>

            <div class="form-group">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.4rem;">
                    <label class="form-label" for="password" style="margin-bottom: 0;">Password</label>
                </div>
                <input type="password" id="password" name="password" class="form-control" placeholder="••••••••" required>
            </div>

            <button type="submit" class="btn btn-primary btn-block" style="padding: 0.85rem; font-size: 1rem; margin-top: 1rem;">
                <i class="fa-solid fa-arrow-right-to-bracket"></i> Sign In
            </button>
        </form>

        <div style="margin-top: 1.75rem; padding-top: 1.5rem; border-top: 1px solid var(--border); font-size: 0.88rem; text-align: center; color: var(--text-muted);">
            Don't have an account yet?
            <a href="${pageContext.request.contextPath}/register" style="color: var(--primary); font-weight: 700; margin-left: 0.25rem;">Create Account</a>
        </div>

        <!-- Demo Accounts Hint for Testing -->
        <div style="margin-top: 1.5rem; background: var(--bg-surface-alt); border: 1px dashed var(--border); border-radius: var(--radius-md); padding: 1rem; font-size: 0.8rem; color: var(--text-muted);">
            <strong style="color: var(--text-main); display: block; margin-bottom: 0.35rem;"><i class="fa-solid fa-circle-info"></i> Demo Credentials:</strong>
            <div><strong>Admin:</strong> admin@ecommerce.com / Password@123</div>
            <div><strong>Customer:</strong> jane@example.com / Password@123</div>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp"/>
