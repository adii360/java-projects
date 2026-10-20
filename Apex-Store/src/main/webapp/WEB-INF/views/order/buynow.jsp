<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Buy Now: ${product.name} | ApexStore" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container" style="max-width: 1040px;">
    <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 1.5rem;">
        <a href="${pageContext.request.contextPath}/product?id=${product.id}" class="btn btn-secondary btn-sm">
            <i class="fa-solid fa-arrow-left"></i> Back to Product
        </a>
        <span style="font-size: 0.85rem; color: var(--text-muted);">Direct Instant Checkout</span>
    </div>

    <h1 style="font-size: 2rem; font-weight: 800; letter-spacing: -0.02em; margin-bottom: 2rem; display: flex; align-items: center; gap: 0.75rem;">
        <span style="display: inline-flex; align-items: center; justify-content: center; width: 2.5rem; height: 2.5rem; border-radius: 50%; background: linear-gradient(135deg, #f59e0b, #ea580c); color: white; font-size: 1.2rem;">
            <i class="fa-solid fa-bolt"></i>
        </span>
        Instant Buy Now
    </h1>

    <form action="${pageContext.request.contextPath}/buynow" method="POST" id="directBuyNowForm">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="productId" value="${product.id}">

        <div style="display: grid; grid-template-columns: 1fr 380px; gap: 2.5rem; align-items: start;">
            <!-- Left: Location & Payment -->
            <div>
                <!-- 1. Customer Location / Shipping Address -->
                <div class="card">
                    <h3 class="card-title">
                        <i class="fa-solid fa-location-dot" style="color: var(--primary);"></i> 1. Delivery Location
                    </h3>

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
                            <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 1rem;">
                                Enter your delivery location where you would like this order shipped:
                            </p>
                        </c:otherwise>
                    </c:choose>

                    <!-- Location Input Fields -->
                    <div style="${not empty addresses ? 'margin-top: 1.5rem; padding-top: 1.5rem; border-top: 1px solid var(--border);' : ''}">
                        <h4 style="font-size: 0.95rem; font-weight: 700; margin-bottom: 1rem; color: var(--text-main);">
                            ${not empty addresses ? 'Or Deliver to a New Location:' : 'Recipient & Address Details:'}
                        </h4>

                        <c:if test="${empty sessionScope.currentUser}">
                            <div class="form-group" style="margin-bottom: 1rem;">
                                <label class="form-label" style="font-size: 0.82rem;">Your Email Address <span style="color:var(--danger)">*</span></label>
                                <input type="email" name="email" class="form-control" placeholder="you@example.com" required>
                                <span style="font-size: 0.75rem; color: var(--text-muted);">We will email your receipt and live tracking updates here.</span>
                            </div>
                        </c:if>

                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                            <div class="form-group">
                                <label class="form-label" style="font-size: 0.82rem;">Recipient Full Name <span style="color:var(--danger)">*</span></label>
                                <input type="text" name="recipientName" value="${sessionScope.currentUser.name}" class="form-control" placeholder="Full Name" ${empty addresses ? 'required' : ''}>
                            </div>
                            <div class="form-group">
                                <label class="form-label" style="font-size: 0.82rem;">Phone Number <span style="color:var(--danger)">*</span></label>
                                <input type="tel" name="phone" value="${sessionScope.currentUser.phone}" class="form-control" placeholder="+1-555-0100" ${empty addresses ? 'required' : ''}>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="form-label" style="font-size: 0.82rem;">Street Address / Apartment / Suite <span style="color:var(--danger)">*</span></label>
                            <input type="text" name="addressLine" class="form-control" placeholder="House #, Street Address, Flat/Floor" ${empty addresses ? 'required' : ''}>
                        </div>

                        <div style="display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 1rem;">
                            <div class="form-group">
                                <label class="form-label" style="font-size: 0.82rem;">City <span style="color:var(--danger)">*</span></label>
                                <input type="text" name="city" class="form-control" placeholder="City" ${empty addresses ? 'required' : ''}>
                            </div>
                            <div class="form-group">
                                <label class="form-label" style="font-size: 0.82rem;">State / Prov <span style="color:var(--danger)">*</span></label>
                                <input type="text" name="state" class="form-control" placeholder="State" ${empty addresses ? 'required' : ''}>
                            </div>
                            <div class="form-group">
                                <label class="form-label" style="font-size: 0.82rem;">Postal Code <span style="color:var(--danger)">*</span></label>
                                <input type="text" name="postalCode" class="form-control" placeholder="ZIP / PIN" ${empty addresses ? 'required' : ''}>
                            </div>
                        </div>

                        <div class="form-group" style="margin-bottom: 0;">
                            <label class="form-label" style="font-size: 0.82rem;">Country</label>
                            <input type="text" name="country" value="United States" class="form-control" placeholder="Country">
                        </div>
                    </div>
                </div>

                <!-- 2. Payment Method: COD and Online Both -->
                <div class="card">
                    <h3 class="card-title">
                        <i class="fa-solid fa-credit-card" style="color: var(--primary);"></i> 2. Select Payment Method
                    </h3>
                    <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 1rem;">Choose between Cash on Delivery or Secure Online Payment:</p>

                    <div style="display: flex; flex-direction: column; gap: 1rem;">
                        <!-- Cash on Delivery -->
                        <div class="payment-method-card" id="bn_pay_cod_wrapper" style="border: 2px solid var(--success); border-radius: var(--radius-md); padding: 1.25rem; background: var(--success-light); display: flex; align-items: flex-start; gap: 1rem; cursor: pointer;">
                            <input type="radio" name="paymentMethod" value="COD" checked id="bn_pay_cod" style="margin-top: 0.25rem;">
                            <label for="bn_pay_cod" style="cursor: pointer; flex: 1;">
                                <div style="display: flex; align-items: center; justify-content: space-between;">
                                    <strong style="color: #065f46; font-size: 1rem;"><i class="fa-solid fa-money-bill-wave"></i> Cash on Delivery (COD)</strong>
                                    <span class="badge badge-success">Zero Risk</span>
                                </div>
                                <p style="font-size: 0.82rem; color: #047857; margin-top: 0.25rem;">
                                    Inspect your package at your doorstep and pay with cash or mobile UPI upon delivery.
                                </p>
                            </label>
                        </div>

                        <!-- Online Payment Option -->
                        <div class="payment-method-card" id="bn_pay_online_wrapper" style="border: 2px solid var(--border); border-radius: var(--radius-md); padding: 1.25rem; background: white; display: flex; flex-direction: column; gap: 0.75rem; cursor: pointer;">
                            <div style="display: flex; align-items: flex-start; gap: 1rem;">
                                <input type="radio" name="paymentMethod" value="ONLINE" id="bn_pay_online" style="margin-top: 0.25rem;">
                                <label for="bn_pay_online" style="cursor: pointer; flex: 1;">
                                    <div style="display: flex; align-items: center; justify-content: space-between;">
                                        <strong style="color: var(--text-main); font-size: 1rem;"><i class="fa-solid fa-bolt" style="color: var(--primary);"></i> Online Payment (Instant & Secure)</strong>
                                        <span class="badge badge-primary">Recommended</span>
                                    </div>
                                    <p style="font-size: 0.82rem; color: var(--text-muted); margin-top: 0.25rem;">
                                        Pay instantly using UPI (Google Pay, PhonePe, Paytm), Credit/Debit Card, or Net Banking.
                                    </p>
                                </label>
                            </div>

                            <!-- Online Payment Details Box -->
                            <div id="bn_online_details" style="display: none; padding-top: 1rem; border-top: 1px solid var(--border); margin-top: 0.5rem;">
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

                                <div id="bn_upi_box" class="online-subbox">
                                    <label class="form-label" style="font-size: 0.82rem;">Enter UPI ID / VPA</label>
                                    <input type="text" name="upiId" class="form-control" placeholder="yourname@okhdfcbank or 9876543210@paytm">
                                    <div style="display: flex; gap: 0.5rem; margin-top: 0.5rem; font-size: 0.78rem; color: var(--text-muted);">
                                        <span><i class="fa-solid fa-shield-halved" style="color: var(--success);"></i> 256-Bit Bank Encryption</span>
                                        <span>• Instant Verification</span>
                                    </div>
                                </div>

                                <div id="bn_card_box" class="online-subbox" style="display: none;">
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

                                <div id="bn_netbanking_box" class="online-subbox" style="display: none;">
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

            <!-- Right: Product Summary & Checkout Action -->
            <div class="order-summary-box">
                <h3 style="font-size: 1.2rem; font-weight: 800; margin-bottom: 1.25rem;">Product & Summary</h3>

                <!-- Product Preview Card -->
                <div style="display: flex; gap: 1rem; align-items: center; margin-bottom: 1.25rem; padding-bottom: 1.25rem; border-bottom: 1px solid var(--border);">
                    <img src="${product.imageUrl}" alt="${product.name}" style="width: 70px; height: 70px; object-fit: cover; border-radius: var(--radius-md); border: 1px solid var(--border);">
                    <div style="flex: 1; min-width: 0;">
                        <span class="badge badge-primary" style="font-size: 0.65rem; margin-bottom: 0.25rem;">${product.categoryName}</span>
                        <strong style="display: block; font-size: 0.95rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: var(--text-main);">
                            ${product.name}
                        </strong>
                        <div style="display: flex; align-items: center; justify-content: space-between; margin-top: 0.25rem;">
                            <span style="font-weight: 700; color: var(--text-main);" id="unitPriceDisplay" data-price="${product.price}">
                                ₹<fmt:formatNumber value="${product.price}" pattern="#,##0.00"/>
                            </span>
                            <span style="font-size: 0.75rem; color: var(--success); font-weight: 600;">In Stock (${product.stockQuantity})</span>
                        </div>
                    </div>
                </div>

                <!-- Quantity Controller -->
                <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.25rem;">
                    <span style="font-size: 0.88rem; font-weight: 600;">Quantity:</span>
                    <div class="qty-control" style="padding: 0.15rem;">
                        <button type="button" class="qty-btn" id="bn_minus_btn">-</button>
                        <input type="number" name="quantity" id="bn_qty_input" value="${quantity}" min="1" max="${product.stockQuantity}" class="qty-input" style="width: 45px;">
                        <button type="button" class="qty-btn" id="bn_plus_btn">+</button>
                    </div>
                </div>

                <div class="summary-row">
                    <span>Subtotal</span>
                    <span id="bn_subtotal_display">₹<fmt:formatNumber value="${subtotal}" pattern="#,##0.00"/></span>
                </div>

                <div class="summary-row">
                    <span>Shipping Fee</span>
                    <span id="bn_shipping_display">
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

                <div class="summary-row total" style="margin-top: 0.75rem; padding-top: 0.75rem;">
                    <span>Grand Total</span>
                    <span id="bn_total_display">₹<fmt:formatNumber value="${total}" pattern="#,##0.00"/></span>
                </div>

                <button type="submit" class="btn btn-primary btn-block btn-lg" id="bn_submit_btn" style="margin-top: 1.5rem; background: linear-gradient(135deg, #f59e0b, #ea580c); border: none; font-weight: 800; font-size: 1.05rem;">
                    <i class="fa-solid fa-bolt"></i> Place Order with Cash on Delivery
                </button>

                <div style="text-align: center; margin-top: 1rem; font-size: 0.78rem; color: var(--text-muted); display: flex; align-items: center; justify-content: center; gap: 0.5rem;">
                    <i class="fa-solid fa-lock" style="color: var(--success);"></i> 100% Guaranteed Safe & Encrypted Checkout
                </div>
            </div>
        </div>
    </form>
</div>

<jsp:include page="../common/footer.jsp"/>
