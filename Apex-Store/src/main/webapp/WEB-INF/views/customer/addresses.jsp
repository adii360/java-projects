<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Saved Addresses | ApexStore" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container" style="max-width: 960px;">
    <h1 style="font-size: 2rem; font-weight: 800; letter-spacing: -0.02em; margin-bottom: 2rem;">
        Shipping Addresses
    </h1>

    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 2rem; align-items: start;">
        <!-- Left: Saved Addresses List -->
        <div>
            <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 1rem;">Saved Addresses (${addresses.size()})</h3>
            <c:choose>
                <c:when test="${not empty addresses}">
                    <div style="display: flex; flex-direction: column; gap: 1rem;">
                        <c:forEach var="addr" items="${addresses}">
                            <div class="card" style="margin-bottom: 0; position: relative;">
                                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
                                    <strong style="font-size: 1.05rem;">${addr.recipientName}</strong>
                                    <c:if test="${addr['default']}">
                                        <span class="badge badge-primary">DEFAULT</span>
                                    </c:if>
                                </div>
                                <p style="color: var(--text-muted); font-size: 0.9rem; line-height: 1.5; margin-bottom: 1rem;">
                                    ${addr.addressLine}<br>
                                    ${addr.city}, ${addr.state} ${addr.postalCode}<br>
                                    ${addr.country}<br>
                                    Phone: ${addr.phone}
                                </p>
                                <div style="display: flex; gap: 0.75rem; border-top: 1px solid var(--border); padding-top: 0.75rem;">
                                    <c:if test="${!addr['default']}">
                                        <form action="${pageContext.request.contextPath}/addresses" method="POST" style="margin: 0;">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="action" value="setDefault">
                                            <input type="hidden" name="addressId" value="${addr.id}">
                                            <button type="submit" class="btn btn-secondary btn-sm">Set as Default</button>
                                        </form>
                                    </c:if>
                                    <form action="${pageContext.request.contextPath}/addresses" method="POST" style="margin: 0;">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="addressId" value="${addr.id}">
                                        <button type="submit" class="btn btn-danger btn-sm" data-confirm="Delete this address?">Delete</button>
                                    </form>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <p style="color: var(--text-muted); font-size: 0.9rem;">You have no saved addresses yet.</p>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Right: Add New Address Form -->
        <div class="card">
            <h3 class="card-title"><i class="fa-solid fa-plus"></i> Add New Address</h3>
            <form action="${pageContext.request.contextPath}/addresses" method="POST">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="action" value="add">

                <div class="form-group">
                    <label class="form-label">Recipient Name</label>
                    <input type="text" name="recipientName" class="form-control" required placeholder="John Doe">
                </div>

                <div class="form-group">
                    <label class="form-label">Phone Number</label>
                    <input type="tel" name="phone" class="form-control" required placeholder="+1-555-0100">
                </div>

                <div class="form-group">
                    <label class="form-label">Address Line</label>
                    <input type="text" name="addressLine" class="form-control" required placeholder="Apartment, suite, street">
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem;">
                    <div class="form-group">
                        <label class="form-label">City</label>
                        <input type="text" name="city" class="form-control" required placeholder="City">
                    </div>
                    <div class="form-group">
                        <label class="form-label">State</label>
                        <input type="text" name="state" class="form-control" required placeholder="State">
                    </div>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem;">
                    <div class="form-group">
                        <label class="form-label">Postal Code</label>
                        <input type="text" name="postalCode" class="form-control" required placeholder="ZIP Code">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Country</label>
                        <input type="text" name="country" class="form-control" value="United States" required>
                    </div>
                </div>

                <div class="form-group" style="display: flex; align-items: center; gap: 0.5rem;">
                    <input type="checkbox" name="isDefault" value="true" id="isDefault">
                    <label for="isDefault" style="font-size: 0.88rem; cursor: pointer;">Set as default delivery address</label>
                </div>

                <button type="submit" class="btn btn-primary btn-block" style="margin-top: 1rem;">
                    Save Address
                </button>
            </form>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp"/>
