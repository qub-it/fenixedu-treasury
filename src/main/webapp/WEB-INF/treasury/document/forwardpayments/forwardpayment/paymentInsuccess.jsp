<%@page import="org.fenixedu.treasury.ui.document.forwardpayments.ForwardPaymentController"%>
<%@page
    import="org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController"%>
<%@page import="java.math.BigDecimal"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<spring:url var="datatablesUrl"
    value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js" />
<spring:url var="datatablesBootstrapJsUrl"
    value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl"
    value="/CSS/dataTables/dataTables.bootstrap.min.css" />

<link rel="stylesheet" href="${datatablesCssUrl}" />
<spring:url var="datatablesI18NUrl"
    value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json" />
<link rel="stylesheet" type="text/css"
    href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css" />

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%-- ${portal.toolkit()} --%>
${portal.angularToolkit()}


<link
    href="${pageContext.request.contextPath}/static/treasury/css/dataTables.responsive.css"
    rel="stylesheet" />
<script
    src="${pageContext.request.contextPath}/static/treasury/js/dataTables.responsive.js"></script>
<link
    href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css"
    rel="stylesheet" />
<script
    src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link
    href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css"
    rel="stylesheet" />
<script
    src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript"
    src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<script
    src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>

<script
    src="${pageContext.request.contextPath}/webjars/angular-sanitize/1.3.11/angular-sanitize.js"></script>
<link rel="stylesheet" type="text/css"
    href="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.css" />
<script
    src="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.js"></script>

<%-- TITLE --%>
<div class="page-header">
    <h1><spring:message code="label.ForwardPaymentController.onlinePayment" /></h1>
    <h1><small><spring:message code="label.ForwardPaymentController.paymentInsuccess" /></small></h1>
    <div>
        <div class="well well-sm">
            <p>
                <strong><spring:message code="label.DebtAccount.finantialInstitution" />:</strong>
                ${forwardPayment.debtAccount.finantialInstitution.name}
            </p>
            <p>
                <strong><spring:message code="label.DebtAccount.customer" />: </strong>
                <c:out value='${forwardPayment.debtAccount.customer.businessIdentification} - ${paymentForward.debtAccount.customer.name}' />
            </p>
            <p>
                <strong><spring:message code="label.Customer.fiscalNumber" />: </strong>
                <c:out value='${forwardPayment.debtAccount.customer.fiscalNumber}' />
            </p>
            <p>
            	<strong><spring:message code="label.Customer.address" />: </strong>
            	<c:out value='${forwardPayment.debtAccount.customer.address}' />
            </p>
        </div>
    </div>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
	<span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>
	&nbsp;
	<a class="" href="${pageContext.request.contextPath}${debtAccountUrl}${forwardPayment.debtAccount.externalId}">
		<spring:message code="label.event.back" />
	</a>
	&nbsp;
</div>
<c:if test="${not empty infoMessages}">
    <div class="alert alert-info" role="alert">
        <c:forEach items="${infoMessages}" var="message">
            <p>
                <span class="glyphicon glyphicon glyphicon-ok-sign"
                    aria-hidden="true">&nbsp;</span> ${message}
            </p>
        </c:forEach>
    </div>
</c:if>
<c:if test="${not empty warningMessages}">
    <div class="alert alert-warning" role="alert">
        <c:forEach items="${warningMessages}" var="message">
            <p>
                <span class="glyphicon glyphicon-exclamation-sign"
                    aria-hidden="true">&nbsp;</span> ${message}
            </p>
        </c:forEach>
    </div>
</c:if>
<c:if test="${not empty errorMessages}">
    <div class="alert alert-danger" role="alert">
        <c:forEach items="${errorMessages}" var="message">
            <p>
                <span class="glyphicon glyphicon-exclamation-sign"
                    aria-hidden="true">&nbsp;</span> ${message}
            </p>
        </c:forEach>
    </div>
</c:if>

<div>
    <p>
	        1. 
	        <spring:message code="label.ForwardPaymentController.chooseInvoiceEntries" />
	        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span> 
	        2.
	        <spring:message code="label.ForwardPaymentController.confirmPayment" />
	        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span>
	        3. 
	        <spring:message code="label.ForwardPaymentController.enterPaymentDetails" />
        <strong>
	        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span> 
	        4.
	        <spring:message code="label.ForwardPaymentController.paymentInsuccess" />
        </strong>
    </p>
</div>

<script>
	angular.isUndefinedOrNull = function(val) {
		return angular.isUndefined(val) || val === null
	};
	angular
			.module('angularAppSettlementNote',
					[ 'ngSanitize', 'ui.select', 'bennuToolkit' ])
			.controller(
					'SettlementNoteController',
					[
							'$scope',
							function($scope) {
								$scope.object = angular
										.fromJson('${settlementNoteBeanJson}');
				                if($scope.object.previousStates[$scope.object.previousStates.length - 1] != 4) {
				                    $scope.object.previousStates.push(4);
				                }
								$scope.getTotal = function() {
									var total = 0;
									for (var i = 0; i < $scope.object.paymentEntries.length; i++) {
										total += parseFloat($scope.object.paymentEntries[i].paymentAmount);
									}
									return total.toFixed(2);
								}
							} ]);
</script>

<div class="panel panel-primary ">
    <div class="panel-heading">
        <h3 class="panel-title">
            <spring:message
                code="label.ForwardPaymentController.debitEntries.to.confirm" />
        </h3>
    </div>
    <div class="panel-body">
		<h5 class="alert alert-danger">
	        <span class="glyphicon glyphicon-warning-sign" aria-hidden="true"></span>&nbsp;
			<spring:message code="error.ForwardPaymentController.paymentInsuccess.title" />
		</h5>
			
		<p>&nbsp;</p>
		<p><strong><spring:message code="error.ForwardPaymentController.paymentInsuccess" />:</strong></p>
		<p><c:out value="${forwardPayment.rejectionLog}" /></p>

    </div>
	<div class="panel-footer">
	</div>
</div>

<jsp:include page="${logosPage}" /> 

<script>
	$(document).ready(function() { });
</script>
