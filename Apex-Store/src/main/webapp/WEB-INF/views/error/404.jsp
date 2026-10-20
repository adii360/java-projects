<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Page Not Found (404) | ApexStore" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container" style="text-align: center; padding: 5rem 1.5rem;">
    <div style="font-size: 5rem; font-weight: 800; color: var(--primary); line-height: 1; margin-bottom: 1rem;">
        404
    </div>
    <h1 style="font-size: 2rem; font-weight: 800; margin-bottom: 1rem;">Page Not Found</h1>
    <p style="color: var(--text-muted); font-size: 1.05rem; max-width: 500px; margin: 0 auto 2rem;">
        The product, category, or page you are looking for may have been moved, renamed, or is temporarily unavailable.
    </p>
    <div class="btn-group" style="justify-content: center;">
        <a href="${pageContext.request.contextPath}/" class="btn btn-primary btn-lg">
            <i class="fa-solid fa-house"></i> Return Home
        </a>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-secondary btn-lg">
            <i class="fa-solid fa-magnifying-glass"></i> Browse Catalog
        </a>
    </div>
</div>

<jsp:include page="../common/footer.jsp"/>
