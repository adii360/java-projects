<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Create Account | ApexStore" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container" style="max-width: 520px; margin-top: 2rem; margin-bottom: 3rem;">
    <div class="card" style="box-shadow: var(--shadow-lg); padding: 2.5rem 2rem;">
        <div style="text-align: center; margin-bottom: 2rem;">
            <div class="brand-icon" style="margin: 0 auto 1rem; width: 3rem; height: 3rem; font-size: 1.5rem;">
                <i class="fa-solid fa-user-plus"></i>
            </div>
            <h1 style="font-size: 1.8rem; font-weight: 800; letter-spacing: -0.02em;">Create Your Account</h1>
            <p style="color: var(--text-muted); font-size: 0.9rem; margin-top: 0.25rem;">Join ApexStore to place orders, track shipments & save favorites</p>
        </div>

        <form action="${pageContext.request.contextPath}/register" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}">

            <div class="form-group">
                <label class="form-label" for="name">Full Name</label>
                <input type="text" id="name" name="name" value="${name}" class="form-control" placeholder="Jane Doe" required autofocus>
            </div>

            <div class="form-group">
                <label class="form-label" for="email">Email Address</label>
                <input type="email" id="email" name="email" value="${email}" class="form-control" placeholder="jane@example.com" required>
            </div>

            <div class="form-group">
                <label class="form-label" for="phone">Phone Number</label>
                <input type="tel" id="phone" name="phone" value="${phone}" class="form-control" placeholder="+1-555-0199">
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                <div class="form-group">
                    <label class="form-label" for="password">Password</label>
                    <input type="password" id="password" name="password" class="form-control" placeholder="••••••••" minlength="6" required>
                    <div class="form-text">Min 6 characters</div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="confirmPassword">Confirm Password</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" placeholder="••••••••" minlength="6" required>
                </div>
            </div>

            <button type="submit" class="btn btn-primary btn-block" style="padding: 0.85rem; font-size: 1rem; margin-top: 1rem;">
                <i class="fa-solid fa-user-check"></i> Register Account
            </button>
        </form>

        <div style="margin-top: 1.75rem; padding-top: 1.5rem; border-top: 1px solid var(--border); font-size: 0.88rem; text-align: center; color: var(--text-muted);">
            Already have an account?
            <a href="${pageContext.request.contextPath}/login" style="color: var(--primary); font-weight: 700; margin-left: 0.25rem;">Sign In</a>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp"/>
