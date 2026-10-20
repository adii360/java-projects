<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Customer Directory | ApexStore Admin" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="container">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <div>
            <div style="font-size: 0.85rem; font-weight: 700; color: var(--primary); text-transform: uppercase;">User Accounts</div>
            <h1 style="font-size: 2rem; font-weight: 800; letter-spacing: -0.02em;">Customer Accounts Directory</h1>
        </div>
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-secondary btn-sm">
            <i class="fa-solid fa-arrow-left"></i> Back to Dashboard
        </a>
    </div>

    <div class="card" style="padding: 0; overflow: hidden;">
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Role</th>
                        <th>Member Since</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="u" items="${users}">
                        <tr>
                            <td>#${u.id}</td>
                            <td>
                                <strong>${u.name}</strong>
                            </td>
                            <td>${u.email}</td>
                            <td>${not empty u.phone ? u.phone : '<span style="color:var(--text-light);">-</span>'}</td>
                            <td>
                                <span class="badge ${u.admin ? 'badge-primary' : 'badge-success'}">
                                    ${u.role}
                                </span>
                            </td>
                            <td style="font-size: 0.85rem; color: var(--text-muted);">
                                <fmt:formatDate value="${u.createdAt}" pattern="MMM dd, yyyy"/>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp"/>
