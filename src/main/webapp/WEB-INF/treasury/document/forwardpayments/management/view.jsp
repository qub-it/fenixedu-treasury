<%@page import="org.fenixedu.treasury.domain.FinantialInstitution"%>
<%@page import="org.fenixedu.treasury.domain.forwardpayments.ForwardPayment"%>
<%@page import="org.fenixedu.bennu.core.security.Authenticate"%>
<%@page import="org.fenixedu.treasury.domain.accesscontrol.TreasuryAccessControl"%>
<%@page import="org.fenixedu.treasury.ui.document.forwardpayments.ManageForwardPaymentsController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<%@ taglib prefix="datatables" uri="http://github.com/dandelion/datatables"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js" />
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css" />
<link rel="stylesheet" href="${datatablesCssUrl}" />
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json" />

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css" />

<link href="${pageContext.request.contextPath}/static/treasury/css/dataTables.responsive.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/static/treasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<script src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<%-- TITLE --%>
<div class="page-header">
	<h1>
		<spring:message code="label.ManageForwardPayments.search" />
		<small></small>
	</h1>
</div>


<!-- /.modal -->
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
	<span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;
	<a class="" href="${pageContext.request.contextPath}<%= ManageForwardPaymentsController.SEARCH_URL %>">
		<spring:message code="label.event.back" />
	</a> 
</div>

<c:if test="${not empty infoMessages}">
	<div class="alert alert-info" role="alert">

		<c:forEach items="${infoMessages}" var="message">
			<p>
				<span class="glyphicon glyphicon glyphicon-ok-sign" aria-hidden="true">&nbsp;</span> ${message}
			</p>
		</c:forEach>
	</div>
</c:if>
<c:if test="${not empty warningMessages}">
	<div class="alert alert-warning" role="alert">

		<c:forEach items="${warningMessages}" var="message">
			<p>
				<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span> ${message}
			</p>
		</c:forEach>
	</div>
</c:if>
<c:if test="${not empty errorMessages}">
	<div class="alert alert-danger" role="alert">

		<c:forEach items="${errorMessages}" var="message">
			<p>
				<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span> ${message}
			</p>
		</c:forEach>
	</div>
</c:if>

<%
	ForwardPayment forwardPayment = (ForwardPayment) request.getAttribute("forwardPayment");
	FinantialInstitution finantialInstitution = forwardPayment.getDebtAccount().getFinantialInstitution();
%>

<div class="panel panel-primary">
	<div class="panel-heading">
		<h3 class="panel-title">
			<spring:message code="label.details" />
		</h3>
	</div>

	<div class="panel-body">
		<form method="post" class="form-horizontal">
			<table class="table">
				<tbody>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.platform" /></th>
                        <td><c:out value='${forwardPayment.forwardPaymentConfiguration.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.whenOccured" /></th>
                        <td><c:out value='${forwardPayment.whenOccured.toString("yyyy-MM-dd HH:mm:ss")}' /></td>
                    </tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.orderNumber" /></th>
						<td><c:out value='${forwardPayment.orderNumber}' /></td>
					</tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.customerName" /></th>
						<td><c:out value='${forwardPayment.debtAccount.customer.name}' /></td>
					</tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.businessCustomerId" /></th>
						<td><c:out value='${forwardPayment.debtAccount.customer.businessIdentification}' /></td>
					</tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.amount" /></th>
						<td><c:out value='${forwardPayment.debtAccount.finantialInstitution.currency.getValueFor(forwardPayment.amount)}' /></td>
					</tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.currentState" /></th>
						<td><c:out value='${forwardPayment.currentState.localizedName.content}' /></td>
					</tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.transactionId" /></th>
                        <td><c:out value='${forwardPayment.transactionId}' /></td>
                    </tr>
                    
<%
	if (TreasuryAccessControl.getInstance().isAllowToModifyInvoices(Authenticate.getUser(), finantialInstitution)) {
%>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.forwardPaymentSuccessUrl" /></th>
                        <td><c:out value='${forwardPayment.forwardPaymentSuccessUrl}' /></td>
                    </tr>                    
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.forwardPaymentInsuccessUrl" /></th>
                        <td><c:out value='${forwardPayment.forwardPaymentInsuccessUrl}' /></td>
                    </tr>
<%
	}
%>
				</tbody>
			</table>
		</form>
	</div>
</div>

<h2>
	<spring:message code="label.ManageForwardPayments.states" />
</h2>

<%
	if (TreasuryAccessControl.getInstance().isAllowToModifyInvoices(Authenticate.getUser(), finantialInstitution)) {
%>

<c:forEach var="log" items="${forwardPayment.orderedForwardPaymentLogs}">
	<div class="panel panel-primary">
		<div class="panel-body">
			<form method="post" class="form-horizontal">
				<table class="table">
					<tbody>
	                    <tr>
	                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentLog.type" /></th>
	                        <td><c:out value='${log.type.localizedName.content}' /></td>
	                    </tr>
	                    <tr>
	                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentLog.whenOccured" /></th>
	                        <td><c:out value='${log.whenOccured.toString("yyyy-MM-dd HH:mm:ss")}' /></td>
	                    </tr>
						<tr>
							<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentLog.statusCode" /></th>
							<td><c:out value='${log.statusCode}' /></td>
						</tr>
						<tr>
							<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentLog.statusLog" /></th>
							<td><c:out value='${log.statusLog}' /></td>
						</tr>
						<tr>
							<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentLog.requestLogFile" /></th>
							<td><c:out value='${log.requestLogFile.contentAsString}' /></td>
						</tr>
						<tr>
							<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentLog.responseLogFile" /></th>
							<td><c:out value='${log.responseLogFile.contentAsString}' /></td>
						</tr>
					</tbody>
				</table>
			</form>
		</div>
	</div>
</c:forEach>
<%
	}
%>
