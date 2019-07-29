<%@page import="org.fenixedu.treasury.ui.accounting.managecustomer.CustomerController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js" />
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css" />

<link rel="stylesheet" href="${datatablesCssUrl}" />
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json" />

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css" />

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%-- ${portal.angularToolkit()}--%>
${portal.toolkit()}

<link href="${pageContext.request.contextPath}/static/treasury/css/dataTables.responsive.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/static/treasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<script src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>

<script src="${pageContext.request.contextPath}/webjars/angular-sanitize/1.3.11/angular-sanitize.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.css" />
<script src="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.js"></script>


<%-- TITLE --%>
<div class="page-header">
	<h1>
		<spring:message code="label.Customer.changeFiscalNumber.title" />&nbsp;(1/2)
		<small></small>
	</h1>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
	<span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;
	<a class="" href="${pageContext.request.contextPath}<%= CustomerController.READ_URL %>${customer.externalId}">
			<spring:message code="label.event.back" />
		</a>
	&nbsp;
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
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.code" /></th>
                        <td><c:out value='${customer.code}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.name" /></th>
                        <td><c:out value='${customer.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.addressCountryCode" /></th>
                        <td><c:out value='${customer.addressCountryCode}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.fiscalNumber" /></th>
                        <td><c:out value='${customer.fiscalNumber}' /></td>
                    </tr>
                </tbody>
            </table>

        </form>
    </div>
</div>

<c:choose>
	<c:when test="${fiscalNumberValid}">
		<div class="alert alert-danger" role="alert">
			<span class="glyphicon glyphicon-remove-sign" aria-hidden="true">&nbsp;</span>
			
			<spring:message code="error.Customer.changeFiscalNumber.already.valid" />
		</div>
	</c:when>
	
	
	
	<c:otherwise>

		<form id="cancelForm" name='form' method="get" class="form-horizontal" 
			action='${pageContext.request.contextPath}<%= CustomerController.READ_URL %>${customer.externalId}'>
		</form>
		
		<form name='form' method="post" class="form-horizontal" 
			action='${pageContext.request.contextPath}${changeFiscalNumberActionFormURI}/changefiscalnumberform/${customer.externalId}'>
		
			<div class="alert alert-warning" role="alert">
				<span class="glyphicon glyphicon-warning-sign" aria-hidden="true">&nbsp;</span>
				
				<spring:message code="message.Customer.changeFiscalNumber.read.first" />
			</div>
			
			<ol>
				<li><spring:message code="message.Customer.changeFiscalNumber.functionality.purpose" /></li>
				<li><spring:message code="message.Customer.changeFiscalNumber.functionality.use.only.for.customer.integration.error" />
				<li><spring:message code="message.Customer.changeFiscalNumber.check.customer.in.erp" /></li>
				<li><spring:message code="message.Customer.changeFiscalNumber.may.have.certified.documents.in.legacy.erp" />
			</ol>
	
			<p>&nbsp;</p>
			<p><spring:message code="message.Customer.changeFiscalNumber.proceed" /></p>
			<p>&nbsp;</p>
				
			<div>
				<input type="submit" class="btn btn-default" role="button" value="<spring:message code="label.continue" /> &raquo;" />
			</div>
	
		</form>

	</c:otherwise>
</c:choose>


<script>
	$(document).ready(function() {});
</script>
