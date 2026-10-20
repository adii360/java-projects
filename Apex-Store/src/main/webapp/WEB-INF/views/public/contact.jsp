<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Contact Us & FAQ | ApexStore" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container" style="max-width: 960px;">
    <div style="text-align: center; margin-bottom: 3rem;">
        <h1 style="font-size: 2.2rem; font-weight: 800; letter-spacing: -0.02em;">We're Here to Help</h1>
        <p style="color: var(--text-muted); font-size: 1rem; margin-top: 0.5rem;">Have questions about products, shipping, or returns? Reach out to our dedicated support team.</p>
    </div>

    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 3rem; align-items: start; margin-bottom: 4rem;">
        <!-- Contact Form -->
        <div class="card" style="padding: 2rem;">
            <h3 class="card-title"><i class="fa-regular fa-paper-plane"></i> Send Us a Message</h3>
            <form action="${pageContext.request.contextPath}/contact" method="POST">
                <input type="hidden" name="csrfToken" value="${csrfToken}">

                <div class="form-group">
                    <label class="form-label">Your Name</label>
                    <input type="text" name="name" class="form-control" placeholder="Jane Doe" required>
                </div>

                <div class="form-group">
                    <label class="form-label">Email Address</label>
                    <input type="email" name="email" class="form-control" placeholder="jane@example.com" required>
                </div>

                <div class="form-group">
                    <label class="form-label">Subject</label>
                    <input type="text" name="subject" class="form-control" placeholder="Order inquiry, tech question..." required>
                </div>

                <div class="form-group">
                    <label class="form-label">Message</label>
                    <textarea name="message" rows="5" class="form-control" placeholder="How can our customer service assist you?" required></textarea>
                </div>

                <button type="submit" class="btn btn-primary btn-block">
                    Submit Message
                </button>
            </form>
        </div>

        <!-- Support Info & Direct Channels -->
        <div>
            <div class="card" style="margin-bottom: 1.5rem;">
                <h4 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 1rem;">Customer Support Center</h4>
                <div style="display: flex; flex-direction: column; gap: 1rem; font-size: 0.95rem;">
                    <div style="display: flex; gap: 1rem; align-items: center;">
                        <i class="fa-solid fa-phone" style="color: var(--primary); font-size: 1.2rem;"></i>
                        <div>
                            <strong>Phone Support</strong>
                            <div style="color: var(--text-muted);">+1-800-555-0100 (Mon - Fri: 8am - 8pm EST)</div>
                        </div>
                    </div>

                    <div style="display: flex; gap: 1rem; align-items: center;">
                        <i class="fa-solid fa-envelope" style="color: var(--secondary); font-size: 1.2rem;"></i>
                        <div>
                            <strong>Email Inquiries</strong>
                            <div style="color: var(--text-muted);">support@ecommerce.com</div>
                        </div>
                    </div>

                    <div style="display: flex; gap: 1rem; align-items: center;">
                        <i class="fa-solid fa-location-dot" style="color: var(--accent); font-size: 1.2rem;"></i>
                        <div>
                            <strong>Headquarters</strong>
                            <div style="color: var(--text-muted);">742 Evergreen Terrace, Springfield, OR 97477</div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="card">
                <h4 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 1rem;">Cash on Delivery Guarantee</h4>
                <p style="color: var(--text-muted); font-size: 0.9rem; line-height: 1.6;">
                    All orders qualify for doorstep payment via COD. You inspect your package before handing payment to the delivery representative.
                </p>
            </div>
        </div>
    </div>

    <!-- Frequently Asked Questions -->
    <section class="card" style="padding: 2.5rem;">
        <h3 style="font-size: 1.5rem; font-weight: 800; margin-bottom: 1.5rem; text-align: center;">Frequently Asked Questions</h3>

        <div style="display: flex; flex-direction: column; gap: 1.25rem;">
            <div style="border-bottom: 1px solid var(--border); padding-bottom: 1rem;">
                <h4 style="font-size: 1.05rem; font-weight: 700; margin-bottom: 0.4rem; color: var(--text-main);">
                    How does Cash on Delivery (COD) work?
                </h4>
                <p style="color: var(--text-muted); font-size: 0.92rem; line-height: 1.6;">
                    Simply select Cash on Delivery during checkout. Our courier will contact you before delivery. You pay in cash or via mobile scan when your parcel arrives.
                </p>
            </div>

            <div style="border-bottom: 1px solid var(--border); padding-bottom: 1rem;">
                <h4 style="font-size: 1.05rem; font-weight: 700; margin-bottom: 0.4rem; color: var(--text-main);">
                    When is shipping free?
                </h4>
                <p style="color: var(--text-muted); font-size: 0.92rem; line-height: 1.6;">
                    Shipping is completely FREE on all orders with a subtotal of ₹999.00 or higher. For orders below ₹999.00, a flat standard shipping fee of ₹99.00 applies.
                </p>
            </div>

            <div style="border-bottom: 1px solid var(--border); padding-bottom: 1rem;">
                <h4 style="font-size: 1.05rem; font-weight: 700; margin-bottom: 0.4rem; color: var(--text-main);">
                    How do I apply a discount coupon?
                </h4>
                <p style="color: var(--text-muted); font-size: 0.92rem; line-height: 1.6;">
                    In your Shopping Cart page, enter your code (such as <code style="font-weight: 700; color: var(--primary);">WELCOME10</code> or <code style="font-weight: 700; color: var(--primary);">SAVE20</code>) in the Promo Coupon box and click Apply. The discount will calculate on the server immediately.
                </p>
            </div>
        </div>
    </section>
</div>

<jsp:include page="../common/footer.jsp"/>
