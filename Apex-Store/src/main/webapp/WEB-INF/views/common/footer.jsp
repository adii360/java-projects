</main> <!-- End of main-content -->

<footer class="site-footer">
    <div class="container">
        <div class="footer-grid">
            <div>
                <div class="footer-brand">ApexStore</div>
                <p class="footer-desc">
                    Your premier destination for high-end electronics, modern lifestyle essentials, and curated designer
                    products. Built purely with Java Servlets, JDBC, and MySQL.
                </p>
                <div style="margin-top: 1.5rem; display: flex; gap: 1rem; font-size: 1.2rem;">
                    <a href="#" style="color:#94a3b8;"><i class="fa-brands fa-github"></i></a>
                    <a href="#" style="color:#94a3b8;"><i class="fa-brands fa-twitter"></i></a>
                    <a href="#" style="color:#94a3b8;"><i class="fa-brands fa-linkedin"></i></a>
                </div>
            </div>

            <div>
                <h4 class="footer-heading">Shop Categories</h4>
                <ul class="footer-links">
                    <li><a href="${pageContext.request.contextPath}/products?category=1">Electronics</a></li>
                    <li><a href="${pageContext.request.contextPath}/products?category=2">Fashion & Apparel</a></li>
                    <li><a href="${pageContext.request.contextPath}/products?category=3">Home & Kitchen</a></li>
                    <li><a href="${pageContext.request.contextPath}/products?category=4">Audio & Accessories</a></li>
                    <li><a href="${pageContext.request.contextPath}/products?category=5">Sports & Fitness</a></li>
                </ul>
            </div>

            <div>
                <h4 class="footer-heading">Customer Care</h4>
                <ul class="footer-links">
                    <li><a href="${pageContext.request.contextPath}/orders">Track Your Order</a></li>
                    <li><a href="${pageContext.request.contextPath}/addresses">Shipping Addresses</a></li>
                    <li><a href="${pageContext.request.contextPath}/contact">Contact Support</a></li>
                    <li><a href="${pageContext.request.contextPath}/contact">FAQs & Help</a></li>
                </ul>
            </div>

            <div>
                <h4 class="footer-heading">Payment & Security</h4>
                <p style="font-size: 0.85rem; line-height: 1.6; margin-bottom: 1rem;">
                    Cash on Delivery (COD) supported nationwide. Zero risk, pay at your doorstep.
                </p>
                <div style="display: flex; gap: 0.75rem; align-items: center; font-size: 1.8rem; color: #cbd5e1;">
                    <i class="fa-solid fa-money-bill-wave" title="Cash On Delivery"></i>
                    <i class="fa-solid fa-shield-halved" title="Secure PreparedStatement JDBC"></i>
                    <i class="fa-solid fa-lock" title="BCrypt Salted Passwords"></i>
                </div>
            </div>
        </div>

        <div class="footer-bottom">
            <p>&copy; 2026 ApexStore. All Rights Reserved. | Designed and Developed by Aditya.</p>
            <p>Enterprise Grade MVC Architecture</p>
        </div>
    </div>
</footer>

<jsp:include page="buy_now_modal.jsp" />
<script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
</body>

</html>