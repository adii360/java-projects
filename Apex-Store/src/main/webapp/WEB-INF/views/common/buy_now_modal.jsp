<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!-- Global Instant Buy Now Modal -->
<div id="buyNowModalOverlay" class="buy-now-modal-overlay" aria-hidden="true" style="display: none;">
    <div class="buy-now-modal-backdrop" id="buyNowModalBackdrop"></div>
    <div class="buy-now-modal-dialog">
        <div class="buy-now-modal-header">
            <div class="buy-now-modal-title">
                <span class="buy-now-modal-icon"><i class="fa-solid fa-bolt"></i></span>
                <div>
                    <h3 style="margin: 0; font-size: 1.25rem; font-weight: 800; color: var(--text-main);">Instant Buy Now</h3>
                    <p style="margin: 0; font-size: 0.8rem; color: var(--text-muted);">Quick delivery setup & checkout</p>
                </div>
            </div>
            <button type="button" class="buy-now-modal-close" id="closeBuyNowModal" aria-label="Close modal">&times;</button>
        </div>

        <form action="${pageContext.request.contextPath}/buynow" method="POST" id="modalBuyNowForm">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <input type="hidden" name="productId" id="modalProductId" value="">

            <div class="buy-now-modal-body">
                <!-- Product Snippet -->
                <div class="modal-product-snippet">
                    <img id="modalProductImg" src="" alt="Product Image" class="modal-product-thumb">
                    <div class="modal-product-meta">
                        <h4 id="modalProductName" class="modal-product-name">Product Name</h4>
                        <div class="modal-price-row">
                            <span id="modalProductPrice" class="modal-product-price">₹0.00</span>
                            <div class="qty-control" style="padding: 0.1rem;">
                                <button type="button" class="qty-btn" id="modalQtyMinus">-</button>
                                <input type="number" name="quantity" id="modalProductQty" value="1" min="1" max="99" class="qty-input" style="width: 40px; height: 28px; font-size: 0.85rem;">
                                <button type="button" class="qty-btn" id="modalQtyPlus">+</button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Step 1: Customer Location -->
                <div class="modal-section">
                    <div class="modal-section-header">
                        <i class="fa-solid fa-location-dot" style="color: var(--primary);"></i>
                        <span>1. Delivery Location</span>
                    </div>

                    <c:if test="${empty sessionScope.currentUser}">
                        <div class="form-group" style="margin-bottom: 0.65rem;">
                            <label class="form-label" style="font-size: 0.78rem;">Email Address <span style="color: var(--danger)">*</span></label>
                            <input type="email" name="email" class="form-control form-control-sm" placeholder="your.email@example.com" required>
                        </div>
                    </c:if>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.65rem; margin-bottom: 0.65rem;">
                        <div class="form-group" style="margin-bottom: 0;">
                            <label class="form-label" style="font-size: 0.78rem;">Recipient Name <span style="color: var(--danger)">*</span></label>
                            <input type="text" name="recipientName" value="${sessionScope.currentUser.name}" class="form-control form-control-sm" placeholder="Full Name" required>
                        </div>
                        <div class="form-group" style="margin-bottom: 0;">
                            <label class="form-label" style="font-size: 0.78rem;">Phone Number <span style="color: var(--danger)">*</span></label>
                            <input type="tel" name="phone" value="${sessionScope.currentUser.phone}" class="form-control form-control-sm" placeholder="+1-555-0100" required>
                        </div>
                    </div>

                    <div class="form-group" style="margin-bottom: 0.65rem;">
                        <label class="form-label" style="font-size: 0.78rem;">Street Address / House / Flat <span style="color: var(--danger)">*</span></label>
                        <input type="text" name="addressLine" class="form-control form-control-sm" placeholder="House #, Street name, Area" required>
                    </div>

                    <div style="display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 0.65rem;">
                        <div class="form-group" style="margin-bottom: 0;">
                            <label class="form-label" style="font-size: 0.78rem;">City <span style="color: var(--danger)">*</span></label>
                            <input type="text" name="city" class="form-control form-control-sm" placeholder="City" required>
                        </div>
                        <div class="form-group" style="margin-bottom: 0;">
                            <label class="form-label" style="font-size: 0.78rem;">State <span style="color: var(--danger)">*</span></label>
                            <input type="text" name="state" class="form-control form-control-sm" placeholder="State" required>
                        </div>
                        <div class="form-group" style="margin-bottom: 0;">
                            <label class="form-label" style="font-size: 0.78rem;">ZIP / PIN <span style="color: var(--danger)">*</span></label>
                            <input type="text" name="postalCode" class="form-control form-control-sm" placeholder="ZIP" required>
                        </div>
                    </div>
                </div>

                <!-- Step 2: Payment Method (COD and Online Both) -->
                <div class="modal-section" style="margin-top: 1rem;">
                    <div class="modal-section-header">
                        <i class="fa-solid fa-credit-card" style="color: var(--success);"></i>
                        <span>2. Payment Method</span>
                    </div>

                    <div class="modal-payment-options">
                        <!-- COD Option -->
                        <label class="modal-payment-card active" id="modalPayCodCard">
                            <input type="radio" name="paymentMethod" value="COD" checked id="modalPayCod">
                            <div class="modal-payment-card-body">
                                <div class="modal-payment-title-row">
                                    <strong><i class="fa-solid fa-money-bill-wave"></i> Cash on Delivery</strong>
                                    <span class="badge badge-success" style="font-size: 0.65rem;">Zero Risk</span>
                                </div>
                                <span class="modal-payment-subtext">Pay cash or mobile UPI at doorstep</span>
                            </div>
                        </label>

                        <!-- Online Option -->
                        <label class="modal-payment-card" id="modalPayOnlineCard">
                            <input type="radio" name="paymentMethod" value="ONLINE" id="modalPayOnline">
                            <div class="modal-payment-card-body">
                                <div class="modal-payment-title-row">
                                    <strong><i class="fa-solid fa-bolt" style="color: #6366f1;"></i> Online Payment</strong>
                                    <span class="badge badge-primary" style="font-size: 0.65rem;">Instant</span>
                                </div>
                                <span class="modal-payment-subtext">UPI, Credit/Debit Card, Net Banking</span>
                            </div>
                        </label>
                    </div>

                    <!-- Online Sub-Options (UPI / Card / Net Banking) -->
                    <div id="modalOnlineDetails" style="display: none; margin-top: 0.75rem; padding: 0.75rem; border: 1px solid var(--border); border-radius: var(--radius-md); background: var(--bg-main);">
                        <div style="display: flex; gap: 0.4rem; margin-bottom: 0.75rem;">
                            <label class="modal-tab-chip active" id="modalTabUpi">
                                <input type="radio" name="onlinePaymentType" value="UPI" checked style="display: none;">
                                <i class="fa-solid fa-mobile-screen"></i> UPI
                            </label>
                            <label class="modal-tab-chip" id="modalTabCard">
                                <input type="radio" name="onlinePaymentType" value="CARD" style="display: none;">
                                <i class="fa-solid fa-credit-card"></i> Card
                            </label>
                            <label class="modal-tab-chip" id="modalTabNetBanking">
                                <input type="radio" name="onlinePaymentType" value="NETBANKING" style="display: none;">
                                <i class="fa-solid fa-building-columns"></i> Net Banking
                            </label>
                        </div>

                        <!-- UPI Input -->
                        <div id="modalSubboxUpi">
                            <input type="text" name="modalUpiId" class="form-control form-control-sm" placeholder="Enter UPI ID (e.g. mobile@upi or name@okaxis)">
                            <div style="display: flex; gap: 0.4rem; margin-top: 0.4rem; font-size: 0.72rem; color: var(--text-muted); align-items: center;">
                                <i class="fa-solid fa-shield-halved" style="color: var(--success);"></i>
                                <span>Google Pay, PhonePe, Paytm, BHIM supported</span>
                            </div>
                        </div>

                        <!-- Card Inputs -->
                        <div id="modalSubboxCard" style="display: none;">
                            <div class="form-group" style="margin-bottom: 0.4rem;">
                                <input type="text" name="modalCardNumber" class="form-control form-control-sm" placeholder="Card Number (16 Digits)" maxlength="19">
                            </div>
                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.4rem;">
                                <input type="text" name="modalCardExpiry" class="form-control form-control-sm" placeholder="MM / YY" maxlength="5">
                                <input type="password" name="modalCardCvv" class="form-control form-control-sm" placeholder="CVV" maxlength="4">
                            </div>
                        </div>

                        <!-- Net Banking Input -->
                        <div id="modalSubboxNetBanking" style="display: none;">
                            <select name="modalBank" class="form-control form-control-sm">
                                <option value="HDFC">HDFC Bank</option>
                                <option value="SBI">State Bank of India</option>
                                <option value="ICICI">ICICI Bank</option>
                                <option value="AXIS">Axis Bank</option>
                                <option value="KOTAK">Kotak Mahindra Bank</option>
                            </select>
                        </div>
                    </div>
                </div>

                <!-- Price Summary Bar -->
                <div class="modal-summary-bar">
                    <div class="modal-summary-line">
                        <span>Items Total:</span>
                        <span id="modalSubtotalVal">₹0.00</span>
                    </div>
                    <div class="modal-summary-line">
                        <span>Delivery Fee:</span>
                        <span id="modalShippingVal" style="color: var(--success); font-weight: 700;">FREE</span>
                    </div>
                    <div class="modal-summary-line modal-total-line">
                        <span>Grand Total:</span>
                        <strong id="modalGrandTotalVal">₹0.00</strong>
                    </div>
                </div>
            </div>

            <div class="buy-now-modal-footer">
                <button type="button" class="btn btn-secondary btn-sm" id="cancelBuyNowBtn">Cancel</button>
                <button type="submit" class="btn btn-primary btn-lg" id="modalSubmitBtn" style="flex: 1; background: linear-gradient(135deg, #f59e0b, #ea580c); border: none; font-weight: 800;">
                    <i class="fa-solid fa-bolt"></i> Place Order with COD
                </button>
            </div>
        </form>
    </div>
</div>
