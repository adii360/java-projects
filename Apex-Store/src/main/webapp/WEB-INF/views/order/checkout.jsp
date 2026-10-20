<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Secure Checkout | ApexStore" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container" style="max-width: 1040px;">
    <h1 style="font-size: 2rem; font-weight: 800; letter-spacing: -0.02em; margin-bottom: 2rem;">
        Secure Checkout
    </h1>

    <form action="${pageContext.request.contextPath}/orders/place" method="POST">
        <input type="hidden" name="csrfToken" value="${csrfToken}">

        <div style="display: grid; grid-template-columns: 1fr 380px; gap: 2.5rem; align-items: start;">
            <!-- Left: Shipping & Payment -->
            <div>
                <!-- Shipping Address Section -->
                <div class="card">
                    <h3 class="card-title"><i class="fa-solid fa-location-dot" style="color: var(--primary);"></i> 1. Shipping Address</h3>

                    <c:choose>
                        <c:when test="${not empty addresses}">
                            <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 1rem;">Select one of your saved delivery addresses:</p>
                            <div style="display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 1.5rem;">
                                <c:forEach var="addr" items="${addresses}" varStatus="status">
                                    <label style="border: 2px solid ${addr['default'] || status.first ? 'var(--primary)' : 'var(--border)'}; border-radius: var(--radius-md); padding: 1rem; display: flex; gap: 1rem; align-items: flex-start; cursor: pointer; background: white;">
                                        <input type="radio" name="addressId" value="${addr.id}" ${addr['default'] || status.first ? 'checked' : ''} style="margin-top: 0.25rem;">
                                        <div>
                                            <strong style="display: block; font-size: 0.95rem;">${addr.recipientName} (${addr.phone})</strong>
                                            <span style="font-size: 0.85rem; color: var(--text-muted);">
                                                ${addr.addressLine}, ${addr.city}, ${addr.state} ${addr.postalCode}, ${addr.country}
                                            </span>
                                            <c:if test="${addr['default']}">
                                                <span class="badge badge-primary" style="margin-left: 0.5rem; font-size: 0.65rem;">DEFAULT</span>
                                            </c:if>
                                        </div>
                                    </label>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <!-- Prompt for new address -->
                            <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 1rem;">No saved address found. Please enter your delivery address below:</p>
                        </c:otherwise>
                    </c:choose>

                    <!-- Add New Address Toggle / Form -->
                    <div style="margin-top: 1.5rem; padding-top: 1.5rem; border-top: 1px solid var(--border);">
                        <h4 style="font-size: 0.95rem; font-weight: 700; margin-bottom: 1rem; color: var(--text-main);">
                            ${not empty addresses ? 'Or Ship to a New Address:' : 'Delivery Details:'}
                        </h4>

                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                            <div class="form-group">
                                <label class="form-label" style="font-size: 0.82rem;">Recipient Name</label>
                                <input type="text" name="newRecipientName" class="form-control" placeholder="Full Name">
                            </div>
                            <div class="form-group">
                                <label class="form-label" style="font-size: 0.82rem;">Phone</label>
                                <input type="tel" name="newPhone" class="form-control" placeholder="+1-555-0100">
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="form-label" style="font-size: 0.82rem;">Street Address</label>
                            <input type="text" name="newAddressLine" class="form-control" placeholder="123 Main St, Apt 4B">
                        </div>

                        <div style="display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 1rem;">
                            <div class="form-group">
                                <label class="form-label" style="font-size: 0.82rem;">City</label>
                                <input type="text" name="newCity" class="form-control" placeholder="City">
                            </div>
                            <div class="form-group">
                                <label class="form-label" style="font-size: 0.82rem;">State</label>
                                <input type="text" name="newState" class="form-control" placeholder="State/Prov">
                            </div>
                            <div class="form-group">
                                <label class="form-label" style="font-size: 0.82rem;">Postal Code</label>
                                <input type="text" name="newPostalCode" class="form-control" placeholder="ZIP">
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Payment Method Section -->
                <div class="card">
                    <h3 class="card-title"><i class="fa-solid fa-credit-card" style="color: var(--primary);"></i> 2. Payment Method</h3>
                    <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 1rem;">Choose how you would like to pay for your order:</p>

                    <div style="display: flex; flex-direction: column; gap: 1rem;">
                        <!-- Cash on Delivery -->
                        <div class="payment-method-card" id="pay_cod_wrapper" style="border: 2px solid var(--success); border-radius: var(--radius-md); padding: 1.25rem; background: var(--success-light); display: flex; align-items: flex-start; gap: 1rem; cursor: pointer;">
                            <input type="radio" name="paymentMethod" value="COD" checked id="pay_cod" style="margin-top: 0.25rem;">
                            <label for="pay_cod" style="cursor: pointer; flex: 1;">
                                <div style="display: flex; align-items: center; justify-content: space-between;">
                                    <strong style="color: #065f46; font-size: 1rem;"><i class="fa-solid fa-money-bill-wave"></i> Cash on Delivery (COD)</strong>
                                    <span class="badge badge-success">Zero Risk</span>
                                </div>
                                <p style="font-size: 0.82rem; color: #047857; margin-top: 0.25rem;">
                                    Pay securely in cash or via mobile UPI upon doorstep package delivery.
                                </p>
                            </label>
                        </div>

                        <!-- Online Payment Option -->
                        <div class="payment-method-card" id="pay_online_wrapper" style="border: 2px solid var(--border); border-radius: var(--radius-md); padding: 1.25rem; background: white; display: flex; flex-direction: column; gap: 0.75rem; cursor: pointer;">
                            <div style="display: flex; align-items: flex-start; gap: 1rem;">
                                <input type="radio" name="paymentMethod" value="ONLINE" id="pay_online" style="margin-top: 0.25rem;">
                                <label for="pay_online" style="cursor: pointer; flex: 1;">
                                    <div style="display: flex; align-items: center; justify-content: space-between;">
                                        <strong style="color: var(--text-main); font-size: 1rem;"><i class="fa-solid fa-bolt" style="color: var(--primary);"></i> Online Payment (Instant & Secure)</strong>
                                        <span class="badge badge-primary">Recommended</span>
                                    </div>
                                    <p style="font-size: 0.82rem; color: var(--text-muted); margin-top: 0.25rem;">
                                        Pay instantly using UPI (Google Pay, PhonePe, Paytm), Credit/Debit Card, or Net Banking.
                                    </p>
                                </label>
                            </div>

                            <!-- Online Payment Details Box (Visible when Online is selected) -->
                            <div id="online_payment_details" style="display: none; padding-top: 1rem; border-top: 1px solid var(--border); margin-top: 0.5rem;">
                                <div style="display: flex; gap: 0.5rem; margin-bottom: 1rem;">
                                    <label class="online-type-chip active" style="flex: 1; text-align: center; padding: 0.6rem 0.5rem; border: 1px solid var(--primary); border-radius: var(--radius-sm); background: rgba(99, 102, 241, 0.08); cursor: pointer; font-size: 0.82rem; font-weight: 700;">
                                        <input type="radio" name="onlinePaymentType" value="UPI" checked style="display: none;">
                                        <i class="fa-solid fa-mobile-screen"></i> UPI
                                    </label>
                                    <label class="online-type-chip" style="flex: 1; text-align: center; padding: 0.6rem 0.5rem; border: 1px solid var(--border); border-radius: var(--radius-sm); background: var(--bg-main); cursor: pointer; font-size: 0.82rem; font-weight: 700;">
                                        <input type="radio" name="onlinePaymentType" value="CARD" style="display: none;">
                                        <i class="fa-solid fa-credit-card"></i> Card
                                    </label>
                                    <label class="online-type-chip" style="flex: 1; text-align: center; padding: 0.6rem 0.5rem; border: 1px solid var(--border); border-radius: var(--radius-sm); background: var(--bg-main); cursor: pointer; font-size: 0.82rem; font-weight: 700;">
                                        <input type="radio" name="onlinePaymentType" value="NETBANKING" style="display: none;">
                                        <i class="fa-solid fa-building-columns"></i> Net Banking
                                    </label>
                                </div>

                                <div id="upi_box" class="online-subbox">
                                    <label class="form-label" style="font-size: 0.82rem;">Enter UPI ID / VPA</label>
                                    <input type="text" name="upiId" class="form-control" placeholder="yourname@okhdfcbank or 9876543210@paytm">
                                    <div style="display: flex; gap: 0.5rem; margin-top: 0.5rem; font-size: 0.78rem; color: var(--text-muted);">
                                        <span><i class="fa-solid fa-shield-halved" style="color: var(--success);"></i> 256-Bit Bank Encryption</span>
                                        <span>• Instant Verification</span>
                                    </div>
                                </div>

                                <div id="card_box" class="online-subbox" style="display: none;">
                                    <div class="form-group" style="margin-bottom: 0.75rem;">
                                        <label class="form-label" style="font-size: 0.82rem;">Card Number</label>
                                        <input type="text" name="cardNumber" class="form-control" placeholder="4532 •••• •••• 8892" maxlength="19">
                                    </div>
                                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem;">
                                        <div class="form-group" style="margin-bottom: 0;">
                                            <label class="form-label" style="font-size: 0.82rem;">Expiry (MM/YY)</label>
                                            <input type="text" name="cardExpiry" class="form-control" placeholder="12/28" maxlength="5">
                                        </div>
                                        <div class="form-group" style="margin-bottom: 0;">
                                            <label class="form-label" style="font-size: 0.82rem;">CVV</label>
                                            <input type="password" name="cardCvv" class="form-control" placeholder="•••" maxlength="4">
                                        </div>
                                    </div>
                                </div>

                                <div id="netbanking_box" class="online-subbox" style="display: none;">
                                    <label class="form-label" style="font-size: 0.82rem;">Select Your Bank</label>
                                    <select name="bankName" class="form-control">
                                        <option value="HDFC">HDFC Bank</option>
                                        <option value="SBI">State Bank of India (SBI)</option>
                                        <option value="ICICI">ICICI Bank</option>
                                        <option value="AXIS">Axis Bank</option>
                                        <option value="KOTAK">Kotak Mahindra Bank</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Right: Order Summary -->
            <div class="order-summary-box">
                <h3 style="font-size: 1.2rem; font-weight: 800; margin-bottom: 1.25rem;">Order Review</h3>

                <div style="max-height: 240px; overflow-y: auto; margin-bottom: 1.25rem; padding-right: 0.5rem;">
                    <c:forEach var="item" items="${cartItems}">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; font-size: 0.88rem;">
                            <div style="max-width: 200px;">
                                <strong style="display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${item.product.name}</strong>
                                <span style="font-size: 0.75rem; color: var(--text-muted);">${item.quantity} × ₹<fmt:formatNumber value="${item.product.price}" pattern="#,##0.00"/></span>
                            </div>
                            <span style="font-weight: 700;">₹<fmt:formatNumber value="${item.lineTotal}" pattern="#,##0.00"/></span>
                        </div>
                    </c:forEach>
                </div>

                <div class="summary-row">
                    <span>Subtotal</span>
                    <span>₹<fmt:formatNumber value="${subtotal}" pattern="#,##0.00"/></span>
                </div>

                <c:if test="${discount > 0}">
                    <div class="summary-row" style="color: var(--success); font-weight: 600;">
                        <span>Discount (${couponCode})</span>
                        <span>-₹<fmt:formatNumber value="${discount}" pattern="#,##0.00"/></span>
                    </div>
                </c:if>

                <div class="summary-row">
                    <span>Shipping Fee</span>
                    <span>
                        <c:choose>
                            <c:when test="${shippingFee == 0}">
                                <strong style="color: var(--success);">FREE</strong>
                            </c:when>
                            <c:otherwise>
                                ₹<fmt:formatNumber value="${shippingFee}" pattern="#,##0.00"/>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>

                <div class="summary-row total">
                    <span>Grand Total</span>
                    <span>₹<fmt:formatNumber value="${total}" pattern="#,##0.00"/></span>
                </div>

                <button type="submit" id="checkout_submit_btn" class="btn btn-primary btn-block btn-lg" style="margin-top: 1.5rem;">
                    <i class="fa-solid fa-check"></i> Complete & Place Order
                </button>
            </div>
        </div>
    </form>
</div>

<jsp:include page="../common/footer.jsp"/>
