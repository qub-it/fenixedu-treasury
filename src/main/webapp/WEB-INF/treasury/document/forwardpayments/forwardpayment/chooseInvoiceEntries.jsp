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
    <h1>
        <spring:message code="label.administration.manageCustomer.createSettlementNote.chooseInvoiceEntries" />
        <small></small>
    </h1>
    <div>
        <div class="well well-sm">
            <p>
                <strong><spring:message code="label.DebtAccount.finantialInstitution" />: </strong>${settlementNoteBean.debtAccount.finantialInstitution.name}</p>
            <p>
                <strong><spring:message code="label.DebtAccount.customer" />: </strong><a
                    href="${pageContext.request.contextPath}<%=DebtAccountController.READ_URL%>${settlementNoteBean.debtAccount.externalId}">${settlementNoteBean.debtAccount.customer.businessIdentification}
                    - ${settlementNoteBean.debtAccount.customer.name}</a>
            </p>
            <p>
                <strong><spring:message code="label.Customer.fiscalNumber" />: </strong>${ settlementNoteBean.debtAccount.customer.fiscalNumber }</p>
        </div>
    </div>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a class=""
        href="${pageContext.request.contextPath}<%= DebtAccountController.READ_URL %>${settlementNoteBean.debtAccount.externalId}"><spring:message code="label.event.back" /></a>
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
        <strong>1. <spring:message code="label.ForwardPaymentController.chooseInvoiceEntries" /></strong>
        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span> 
        2. <spring:message code="label.ForwardPaymentController.confirmPayment" />
        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span> 
        3. <spring:message code="label.ForwardPaymentController.enterPaymentDetails" />
        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span> 
        4. <spring:message code="label.ForwardPaymentController.paymentConfirmation" />
    </p>
</div>

<form name='form' method="post" class="form-horizontal" ng-app="angularAppSettlementNote" ng-controller="SettlementNoteController"
    action='${pageContext.request.contextPath}<%= ForwardPaymentController.CHOOSE_INVOICE_ENTRIES_URL %>'>

    <input name="bean" type="hidden" value="{{ object }}" />

<%-- 
    <div class="panel panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title">
                <spring:message code="label.SettlementNote.header" />
            </h3>
            <p>
                <spring:message code="label.SettlementNote.headerDetails" />
            </p>
        </div>
        <div class="panel-body">
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.date" />
                </div>
                <div class="col-sm-4">
                    <input class="form-control" type="text" bennu-date="object.date" />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.SettlementNote.documentNumberSeries" />
                </div>
                <div class="col-sm-4">
                    <ui-select id="settlementNote_documentNumberSeries" ng-model="$parent.object.docNumSeries" theme="bootstrap" ng-disabled="disabled" ng-required> <ui-select-match>{{$select.selected.text}}</ui-select-match>
                    <ui-select-choices repeat="docNumSeries.id as docNumSeries in object.documentNumberSeries | filter: $select.search"> <span
                        ng-bind-html="docNumSeries.text"></span> </ui-select-choices> </ui-select>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.SettlementNote.originDocumentNumber" />
                </div>
                <div class="col-sm-4">
                    <input class="form-control" type="text" ng-model="object.originDocumentNumber" />
                </div>
            </div>
        </div>
    </div>
--%>

    <div class="panel panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title">
                <spring:message code="label.DebitEntry" />
            </h3>
            <p>
                <spring:message code="label.DebitEntry.choose" />
            </p>
        </div>
        <div class="panel-body">
            <table id="debitEntriesTable" class="table responsive table-bordered table-hover" width="100%">
                <col style="width: 3%" />
                <thead>
                    <tr>
                        <%-- Check Column --%>
                        <th style="min-width: 35px;"></th>
                        <th class="col-sm-2"><spring:message code="label.DebitEntry.documentNumber" /></th>
                        <th><spring:message code="label.DebitEntry.description" /></th>
                        <th class="col-sm-1"><spring:message code="label.DebitEntry.date" /></th>
                        <th class="col-sm-1"><spring:message code="label.DebitEntry.totalAmount" /></th>
                        <th class="col-sm-1"><spring:message code="label.DebitEntry.openAmount" /></th>
                        <th class="col-sm-1"><spring:message code="label.DebitEntry.vat" /></th>
                        <th class="col-sm-2"><spring:message code="label.DebitEntry.debtAmount" /></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${ settlementNoteBean.debitEntries}" var="debitEntryBean" varStatus="loop">
                        <c:if test="${ debitEntryBean.notValid }">
                            <tr class="alert alert-danger">
                        </c:if>
                        <c:if test="${ not debitEntryBean.notValid }">
                            <tr>
                        </c:if>

                        <td><span class="glyphicon glyphicon-remove-circle" ng-show="object.debitEntries[${ loop.index }].isNotValid"></span> <input class="form-control"
                            ng-model="object.debitEntries[${ loop.index }].isIncluded" type="checkbox" /></td>
                        <td><c:out value="${ debitEntryBean.debitEntry.finantialDocument.uiDocumentNumber }" /></td>
                        <td><c:out value="${ debitEntryBean.debitEntry.description }" /></td>
                        <td><c:out value="${ debitEntryBean.documentDueDate }" /></td>
                        <td><c:out value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( debitEntryBean.debitEntry.amountWithVat ) }" /></td>
                        <td><c:out value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( debitEntryBean.debitEntry.openAmount ) }" /></td>
                        <td><c:out value="${ debitEntryBean.debitEntry.vat.taxRate }" /></td>
                        <c:if test="${ not settlementNoteBean.reimbursementNote }">
                            <td>
                                <div class="input-group col-sm-12">
                                    <div class=" input-group-addon">
                                        <c:out value="${settlementNoteBean.debtAccount.finantialInstitution.currency.symbol}" />
                                    </div>
                                    <input class="form-control" name="debtAmount${ loop.index }" ng-model="object.debitEntries[${ loop.index }].debtAmount" type="text"
                                        ng-disabled="!object.debitEntries[${ loop.index }].isIncluded" ng-pattern="/^(0*\.(0[1-9]|[1-9][0-9]?)|[1-9][0-9]*(\.[0-9]{1,2})?)$/"
                                        value='0.00' />
                                </div>
                                <p class="alert alert-danger" ng-show="form.debtAmount${ loop.index }.$error.pattern && object.debitEntries[${ loop.index }].isIncluded">
                                    <spring:message code="error.invalid.format.input" />
                                </p>
                            </td>
                        </c:if>
                        <c:if test="${ settlementNoteBean.reimbursementNote }">
                            <td><c:out value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( debitEntryBean.debtAmount ) }"></c:out></td>
                        </c:if>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

<%--
    <div class="panel panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title">
                <spring:message code="label.CreditEntry" />
            </h3>
            <p>
                <spring:message code="label.CreditEntry.choose" />
            </p>
        </div>
        <c:choose>
            <c:when test="${not empty settlementNoteBean.creditEntries}">

                <div class="panel-body">
                    <table id="creditEntriesTable" class="table responsive table-bordered table-hover" width="100%">
                        <col style="width: 3%" />
                        <thead>
                            <tr>
                                <th style="min-width: 35px;"></th>
                                <th class="col-sm-2"><spring:message code="label.CreditEntry.documentNumber" /></th>
                                <th><spring:message code="label.CreditEntry.motive" /></th>
                                <th class="col-sm-1"><spring:message code="label.CreditEntry.date" /></th>
                                <th class="col-sm-1"><spring:message code="label.DebitEntry.vat" /></th>
                                <th class="col-sm-2"><spring:message code="label.CreditEntry.totalAmount" /></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${ settlementNoteBean.creditEntries}" var="creditEntryBean" varStatus="loop">
                                <tr>
                                    <td><input class="form-control" ng-model="object.creditEntries[${ loop.index }].isIncluded" type="checkbox" /></td>
                                    <td><c:out value="${ creditEntryBean.creditEntry.finantialDocument.uiDocumentNumber }" /></td>
                                    <td><c:out value="${ creditEntryBean.creditEntry.description }" /></td>
                                    <td><c:out value="${ creditEntryBean.documentDueDate }" /></td>
                                    <td><c:out value="${ creditEntryBean.creditEntry.vat.taxRate }" /></td>
                                    <td>- <c:out
                                            value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( creditEntryBean.creditEntry.openAmount ) }" />
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-warning" role="alert">

                    <p>
                        <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
                        <spring:message code="label.noResultsFound" />
                    </p>

                </div>

            </c:otherwise>
        </c:choose>
    </div>
--%>

    <div class="panel-footer">
        <button type="submit" class="btn btn-primary">
            </span>
            <spring:message code="label.continue" />
            &nbsp;<span class="glyphicon glyphicon-chevron-right" aria-hidden="true">
        </button>
    </div>
</form>

<script>
	$(document).ready(function() {
		// Put here the initializing code for page
	});
</script>
