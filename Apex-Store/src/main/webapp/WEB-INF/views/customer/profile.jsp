<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Account Settings | ApexStore" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container" style="max-width: 800px;">
    <h1 style="font-size: 2rem; font-weight: 800; letter-spacing: -0.02em; margin-bottom: 2rem;">
        Account Settings
    </h1>

    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 2rem;">
        <!-- Profile Info Form -->
        <div class="card">
            <h3 class="card-title"><i class="fa-regular fa-id-badge"></i> Profile Details</h3>
            <form action="${pageContext.request.contextPath}/profile" method="POST">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="action" value="updateProfile">

                <div class="form-group">
                    <label class="form-label">Full Name</label>
                    <input type="text" name="name" value="${sessionScope.currentUser.name}" class="form-control" required>
                </div>

                <div class="form-group">
                    <label class="form-label">Email Address</label>
                    <input type="email" value="${sessionScope.currentUser.email}" class="form-control" disabled style="background: var(--bg-surface-alt); cursor: not-allowed;">
                    <div class="form-text">Email address cannot be changed.</div>
                </div>

                <div class="form-group">
                    <label class="form-label">Phone Number</label>
                    <input type="tel" name="phone" value="${sessionScope.currentUser.phone}" class="form-control" placeholder="+1-555-0199">
                </div>

                <button type="submit" class="btn btn-primary btn-block" style="margin-top: 1rem;">
                    Save Profile Changes
                </button>
            </form>
        </div>

        <!-- Change Password Form -->
        <div class="card">
            <h3 class="card-title"><i class="fa-solid fa-lock"></i> Security & Password</h3>
            <form action="${pageContext.request.contextPath}/profile" method="POST">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="action" value="changePassword">

                <div class="form-group">
                    <label class="form-label">Current Password</label>
                    <input type="password" name="currentPassword" class="form-control" required placeholder="••••••••">
                </div>

                <div class="form-group">
                    <label class="form-label">New Password</label>
                    <input type="password" name="newPassword" class="form-control" required minlength="6" placeholder="••••••••">
                </div>

                <div class="form-group">
                    <label class="form-label">Confirm New Password</label>
                    <input type="password" name="confirmPassword" class="form-control" required minlength="6" placeholder="••••••••">
                </div>

                <button type="submit" class="btn btn-secondary btn-block" style="margin-top: 1rem;">
                    Update Password
                </button>
            </form>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp"/>
