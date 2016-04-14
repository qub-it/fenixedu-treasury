<%@page import="org.fenixedu.treasury.ui.document.forwardpayments.ForwardPaymentController"%>
<%@page import="org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController"%>
<%@page import="org.fenixedu.treasury.ui.document.managepayments.SettlementNoteController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js" />
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css" />

<link rel="stylesheet" href="${datatablesCssUrl}" />
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css" />

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
${portal.angularToolkit()}


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
	<h1><spring:message code="label.ForwardPaymentController.onlinePayment" /></h1>
    <h1><small><spring:message code="label.ForwardPaymentController.chooseInvoiceEntries" /></small></h1>
	
	<div>
		<div class="well well-sm">
			<p>
				<strong><spring:message code="label.DebtAccount.finantialInstitution" />:</strong>
				${settlementNoteBean.debtAccount.finantialInstitution.name}
			</p>
			<p>
				<strong><spring:message code="label.DebtAccount.customer" />:</strong>
				<a href="${pageContext.request.contextPath}<%=DebtAccountController.READ_URL%>${settlementNoteBean.debtAccount.externalId}">${settlementNoteBean.debtAccount.customer.businessIdentification}
					- ${settlementNoteBean.debtAccount.customer.name}</a>
			</p>
			<p>
				<strong><spring:message code="label.Customer.fiscalNumber" />:</strong>
				${ settlementNoteBean.debtAccount.customer.fiscalNumber }
			</p>
		</div>
	</div>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
	<span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>
	&nbsp;
	<a class="" href="${pageContext.request.contextPath}<%= DebtAccountController.READ_URL %>${settlementNoteBean.debtAccount.externalId}">
		<spring:message code="label.event.back" />
	</a>
	&nbsp;
</div>
<c:if test="${not empty infoMessages}">
	<div class="alert alert-info" role="alert">
		<c:forEach items="${infoMessages}" var="message">
			<p>
				<span class="glyphicon glyphicon glyphicon-ok-sign" aria-hidden="true">&nbsp;</span>
				${message}
			</p>
		</c:forEach>
	</div>
</c:if>
<c:if test="${not empty warningMessages}">
	<div class="alert alert-warning" role="alert">
		<c:forEach items="${warningMessages}" var="message">
			<p>
				<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
				${message}
			</p>
		</c:forEach>
	</div>
</c:if>
<c:if test="${not empty errorMessages}">
	<div class="alert alert-danger" role="alert">
		<c:forEach items="${errorMessages}" var="message">
			<p>
				<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
				${message}
			</p>
		</c:forEach>
	</div>
</c:if>


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
								if ($scope.object.previousStates.length == 0
										|| $scope.object.previousStates[$scope.object.previousStates.length - 1] != 0) {
									$scope.object.previousStates.push(0);
								}
							} ]);
</script>


<div>
	<p>
		<strong>
			1.
			<spring:message code="label.ForwardPaymentController.chooseInvoiceEntries" />
		</strong>
		<span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span>
		2.
		<spring:message code="label.ForwardPaymentController.confirmPayment" />
		<span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span>
		3.
		<spring:message code="label.ForwardPaymentController.enterPaymentDetails" />
		<span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span>
		4.
		<spring:message code="label.ForwardPaymentController.paymentConfirmation" />
	</p>
</div>

<form name='form' method="post" class="form-horizontal" ng-app="angularAppSettlementNote" ng-controller="SettlementNoteController"
	action='${pageContext.request.contextPath}<%= ForwardPaymentController.CHOOSE_INVOICE_ENTRIES_URL %>'>

	<input name="bean" type="hidden" value="{{ object }}" />

	<div class="panel panel-primary">
		<div class="panel-heading">
			<h3 class="panel-title">
				<spring:message code="label.ForwardPaymentController.debitEntries" />
			</h3>
		</div>
		<div class="panel-body">
			<div class="alert alert-warning" role="alert">
				<h5>
					<spring:message code="label.ForwardPaymentController.debitEntries.choose" />
				</h5>
			</div>
			
			<table id="debitEntriesTable" class="table responsive table-bordered table-hover" width="100%">
				<col style="width: 3%" />
				<thead>
					<tr>
						<%-- Check Column --%>
						<th style="min-width: 35px;"></th>
						<th class="col-sm-2">
							<spring:message code="label.DebitEntry.documentNumber" />
						</th>
						<th>
							<spring:message code="label.DebitEntry.description" />
						</th>
						<th class="col-sm-1">
							<spring:message code="label.DebitEntry.date" />
						</th>
						<th class="col-sm-1">
							<spring:message code="label.DebitEntry.totalAmount" />
						</th>
						<th class="col-sm-1">
							<spring:message code="label.DebitEntry.openAmount" />
						</th>
						<th class="col-sm-1">
							<spring:message code="label.DebitEntry.vat" />
						</th>
						<th class="col-sm-2">
							<spring:message code="label.DebitEntry.debtAmount" />
						</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${ settlementNoteBean.debitEntries}" var="debitEntryBean" varStatus="loop">
					<tr>
						<c:if test="${ debitEntryBean.notValid }">
							<tr class="alert alert-danger">
						</c:if>
						<c:if test="${ not debitEntryBean.notValid }">
							<tr>
						</c:if>

						<td>
							<span class="glyphicon glyphicon-remove-circle" ng-show="object.debitEntries[${ loop.index }].isNotValid"></span>
							<input class="form-control" ng-model="object.debitEntries[${ loop.index }].isIncluded" type="checkbox" />
						</td>
						<td>
							<c:out value="${ debitEntryBean.debitEntry.finantialDocument.uiDocumentNumber }" />
						</td>
						<td>
							<c:out value="${ debitEntryBean.debitEntry.description }" />
						</td>
						<td>
							<c:out value="${ debitEntryBean.documentDueDate }" />
						</td>
						<td>
							<c:out value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( debitEntryBean.debitEntry.amountWithVat ) }" />
						</td>
						<td>
							<c:out value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( debitEntryBean.debitEntry.openAmount ) }" />
						</td>
						<td>
							<c:out value="${ debitEntryBean.debitEntry.vat.taxRate }" />
						</td>
						<td>
							<c:out value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor(debitEntryBean.debitEntry.openAmount) }" />
						</td>
					</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</div>

	<div class="panel-footer">
		<button type="submit" class="btn btn-primary">
			<spring:message code="label.continue" />
			&nbsp;<span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
		</button>
	</div>
</form>

<jsp:include page="${logosPage}" />

<script>
	$(document).ready(function() {
		// Put here the initializing code for page
	});
</script>
