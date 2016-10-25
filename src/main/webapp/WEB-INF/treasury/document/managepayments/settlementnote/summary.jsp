<%@page
    import="org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController"%>
<%@page import="java.math.BigDecimal"%>
<%@page
    import="org.fenixedu.treasury.ui.document.managepayments.SettlementNoteController"%>
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
    <h1>
        <spring:message
            code="label.administration.manageCustomer.createSettlementNote.chooseInvoiceEntries" />
        <small></small>
    </h1>
    <div>
        <div class="well well-sm">
            <p>
                <strong><spring:message
                        code="label.DebtAccount.finantialInstitution" />:
                </strong>${settlementNoteBean.debtAccount.finantialInstitution.name}</p>
            <p>
                <strong><spring:message
                        code="label.DebtAccount.customer" />: </strong><a
                    href="${pageContext.request.contextPath}<%=DebtAccountController.READ_URL%>${settlementNoteBean.debtAccount.externalId}">${settlementNoteBean.debtAccount.customer.businessIdentification}
                    - ${settlementNoteBean.debtAccount.customer.name}</a>
            </p>
            <p>
                <strong><spring:message
                        code="label.Customer.fiscalNumber" />: </strong>${ settlementNoteBean.debtAccount.customer.fiscalNumber }</p>
        </div>

    </div>
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
        <spring:message
            code="label.administration.manageCustomer.createSettlementNote.chooseInvoiceEntries" />
        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span>
        2.
        <spring:message
            code="label.administration.manageCustomer.createSettlementNote.calculateInterest" />
        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span>
        3.
        <spring:message
            code="label.administration.manageCustomer.createSettlementNote.createDebitNote" />
        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span>
        4.
        <spring:message
            code="label.administration.manageCustomer.createSettlementNote.insertpayment" />
        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span>
        <b>5. <spring:message
                code="label.administration.manageCustomer.createSettlementNote.summary" /></b>
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
				                $scope.processBackSubmit = function(contextPath) {
				                	$scope.object.previousStates.pop();
				                    var path = contextPath + $scope.object.settlementNoteStateUrls[$scope.object.previousStates.pop()];
				                    $("#summaryForm").attr("action", path);
				                    //Timeout necessary to make angular update the previousStates array before submit
				                    setTimeout(function() { $("#summaryForm").submit() }, 10);                                       
				                }								
								$scope.currencySymbol = "${ settlementNoteBean.debtAccount.finantialInstitution.currency.symbol }";
							} ]);
</script>

<script type="text/javascript">
	function processSubmit(url) {
		$("#summaryForm").attr("action", url);
		$("#summaryForm").submit();
	}
</script>


<div class="panel panel-primary">
    <div class="panel-heading">
        <h3 class="panel-title">
            <spring:message code="label.Customer" />
        </h3>
    </div>
    <div class="panel-body">
        <table class="table">
            <tbody>
                <tr>
                    <th scope="row" class="col-xs-3"><spring:message
                            code="label.Customer.name" /></th>
                    <td><c:out
                            value='${settlementNoteBean.debtAccount.customer.name}' />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="col-xs-3"><spring:message
                            code="label.Customer.fiscalNumber" /></th>
                    <td><c:out
                            value='${settlementNoteBean.debtAccount.customer.fiscalNumber}' />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="col-xs-3"><spring:message
                            code="label.Customer.address" /></th>
                    <td><c:out
                            value='${settlementNoteBean.debtAccount.customer.address}' />
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</div>

<div class="panel panel-primary ">
    <div class="panel-heading">
        <h3 class="panel-title">
            <spring:message
                code="label.document.managepayments.settlementnote.InvoiceEntries" />
        </h3>
    </div>
    <div class="panel-body">
        <table id="debitNoteTable"
            class="table responsive table-bordered table-hover" width="100%">
            <thead>
                <tr>
                    <th><spring:message
                            code="label.DebitEntry.documentNumber" /></th>
                    <th><spring:message
                            code="label.DebitEntry.description" /></th>
                    <th><spring:message
                            code="label.DebitEntry.dueDate" /></th>
                    <th><spring:message code="label.DebitEntry.vat" /></th>
                    <th><spring:message
                            code="label.DebitEntry.amountWithVat" /></th>
                </tr>
            </thead>
            <tbody>
                <c:set var="debitNoteDate"
                    value='${settlementNoteBean.debitNoteDate.toString("yyyy-MM-dd")}' />
                <c:forEach items="${ settlementNoteBean.debitEntries }"
                    var="debitEntryBean">
                    <c:if
                        test="${ debitEntryBean.included && empty debitEntryBean.debitEntry.finantialDocument  }">
                        <tr>
                            <td>---</td>
                            <td><c:out
                                    value="${ debitEntryBean.debitEntry.description }" />
                            </td>
                            <td><c:out value='${ debitNoteDate }' />
                            </td>
                            <td><c:out
                                    value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueWithScale( debitEntryBean.debitEntry.vat.taxRate) }" />
                            </td>
                            <td><c:out
                                    value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( debitEntryBean.debtAmountWithVat ) }" />
                            </td>
                        </tr>
                    </c:if>
                </c:forEach>
                <c:forEach
                    items="${ settlementNoteBean.interestEntries }"
                    var="interestEntryBean">
                    <c:if test="${ interestEntryBean.included  }">
                        <tr>
                            <td>---</td>
                            <td><spring:message code="label.InterestEntry.interest" />
                                       &nbsp; <c:out
                                    value="${ interestEntryBean.debitEntry.description }" />
                            </td>
                            <td><c:out value='${ debitNoteDate }' />
                            </td>
                            <td>0.00</td>
                            <td><c:out
                                    value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( interestEntryBean.interest.interestAmount ) }" />
                            </td>
                        </tr>
                    </c:if>
                </c:forEach>
                <c:forEach items="${ settlementNoteBean.debitEntries }"
                    var="debitEntryBean">
                    <c:if
                        test="${ debitEntryBean.included && not empty debitEntryBean.debitEntry.finantialDocument  }">
                        <tr>
                            <td><c:out
                                    value="${ debitEntryBean.debitEntry.finantialDocument.uiDocumentNumber }" />
                            </td>
                            <td><c:out
                                    value="${ debitEntryBean.debitEntry.description }" />
                            </td>
                            <td><c:out
                                    value="${ debitEntryBean.documentDueDate }" />
                            </td>
                            <td><c:out
                                    value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueWithScale( debitEntryBean.debitEntry.vat.taxRate) }" />
                            </td>
                            <td><c:out
                                    value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( debitEntryBean.debtAmountWithVat ) }" />
                            </td>
                        </tr>
                    </c:if>
                </c:forEach>
                <c:forEach items="${ settlementNoteBean.creditEntries}"
                    var="creditEntryBean" varStatus="loop">
                    <c:if test="${ creditEntryBean.included }">
                        <tr>
                            <td><c:out
                                    value="${ creditEntryBean.creditEntry.finantialDocument.uiDocumentNumber }" />
                            </td>
                            <td><c:out
                                    value="${ creditEntryBean.creditEntry.description }" />
                            </td>
                            <td><c:out
                                    value="${ creditEntryBean.documentDueDate }" />
                            </td>
                            <td><c:out
                                    value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueWithScale( creditEntryBean.creditEntry.vat.taxRate ) }" />
                            </td>
                            <td>- <c:out
                                    value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( creditEntryBean.creditAmountWithVat ) }" />
                            </td>
                        </tr>
                    </c:if>
                </c:forEach>
            </tbody>
        </table>
        <div class="panel-footer">
            <c:if test="${ settlementNoteBean.reimbursementNote }">
                <p align="right">
                    <b><spring:message
                            code="label.document.managepayments.settlementnote.reimbursementTotal" /></b>:
                    ${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( settlementNoteBean.debtAmountWithVat.negate() ) }
                </p>
            </c:if>
            <c:if
                test="${ not settlementNoteBean.reimbursementNote }">
                <p align="right">
                    <b><spring:message
                            code="label.document.managepayments.settlementnote.paymentTotal" /></b>:
                    ${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( settlementNoteBean.debtAmountWithVat ) }
                </p>
            </c:if>
        </div>
    </div>
</div>

<div class="panel panel-primary">
    <div class="panel-heading">
        <h3 class="panel-title">
            <c:if test="${ settlementNoteBean.reimbursementNote }">
                <spring:message
                    code="label.document.managepayments.settlementnote.ReimbursementMethod" />
            </c:if>
            <c:if test="${ not settlementNoteBean.reimbursementNote }">
                <spring:message
                    code="label.document.managepayments.settlementnote.PaymentMethod" />
            </c:if>
        </h3>
    </div>
    <div class="panel-body">
        <table id="paymentTableTable"
            class="table responsive table-bordered table-hover" width="100%">
            <thead>
                <tr>
                    <c:if
                        test="${ settlementNoteBean.reimbursementNote }">
                        <th><spring:message
                                code="label.document.managepayments.settlementnote.ReimbursementMethod" /></th>
                    </c:if>
                    <c:if
                        test="${ not settlementNoteBean.reimbursementNote }">
                        <th><spring:message
                                code="label.document.managepayments.settlementnote.PaymentMethod" /></th>
                    </c:if>
                    <th><spring:message
                            code="label.PaymentMethod.value" /></th>
                </tr>
            </thead>
            <tbody>
                <c:forEach
                    items="${ settlementNoteBean.paymentEntries }"
                    var="paymentEntry">
                    <tr>
                        <td><c:out
                                value="${ paymentEntry.paymentMethod.name.content }" />
                        </td>
                        <td><c:out
                                value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( paymentEntry.paymentAmount ) }" />
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
        <div class="panel-footer">
            <c:if test="${ settlementNoteBean.reimbursementNote }">
                <p align="right">
                    <b><spring:message
                            code="label.SettlementNote.reimbursementTotal" /></b>:
                    ${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( settlementNoteBean.paymentAmount ) }
                </p>
            </c:if>
            <c:if
                test="${ not settlementNoteBean.reimbursementNote }">
                <p align="right">
                    <b><spring:message
                            code="label.SettlementNote.paymentTotal" /></b>:
                    ${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( settlementNoteBean.paymentAmount ) }
                </p>
            </c:if>
        </div>
    </div>
</div>

<!-- <div class="panel panel-primary">     -->
<!--     <div class="panel panel-heading"> -->
<!--         <h3 class="panel-title"> -->
<%--             <spring:message code="label.Vat.Summary" /> --%>
<!--         </h3> -->
<!--     </div> -->
<!--     <div class="panel-body"> -->
<!--         <div class="row"> -->
<!--             <div class="col-md-6 col-lg-6"> -->
<!--                 <table id="vatTable" -->
<!--                     class="table responsive table-bordered table-hover" width="100%"> -->
<!--                     <thead> -->
<!--                         <tr> -->
<%--                             <th><spring:message code="label.VatType" /></th> --%>
<%--                             <th><spring:message code="label.VatType.valueWithoutVat" /></th> --%>
<%--                             <th><spring:message code="label.VatType.valueOfVat" /></th> --%>
<!--                         </tr> -->
<!--                     </thead> -->
<!--                     <tbody> -->
<%--                         <c:set var="sumAmount" value="<%= BigDecimal.ZERO %>" /> --%>
<%--                         <c:set var="sumAmountWithVat" value="<%= BigDecimal.ZERO %>" />                             --%>
<%--                         <c:forEach items="${ settlementNoteBean.valuesByVat }" var="entry"> --%>
<%--                             <c:set var="sumAmount" value="${ sumAmount.add(entry.value.amount) }" /> --%>
<%--                             <c:set var="sumAmountWithVat" value="${ sumAmountWithVat.add(entry.value.amountWithVat) }" />                 --%>
<!--                             <tr> -->
<!--                                 <td> -->
<%--                                     <c:out value="${ entry.key }" /> --%>
<!--                                 </td> -->
<!--                                 <td> -->
<%--                                     <c:out value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( entry.value.amount ) }" /> --%>
<!--                                 </td> -->
<!--                                 <td> -->
<%--                                     <c:out value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( entry.value.amountWithVat.subtract(entry.value.amount) ) }" /> --%>
<!--                                 </td> -->
<!--                             </tr> -->
<%--                         </c:forEach> --%>
<!--                     </tbody> -->
<!--                 </table> -->
<!--             </div> -->
<!--             <div class="col-md-6 col-lg-6"> -->
<!--                 <p align="center"> -->
<%--                     <b><spring:message code="label.summary"/></b> --%>
<!--                 </p> -->
<!--                 <br> -->
<!--                 <table id="vatSummaryTable" -->
<!--                     class="table responsive table-bordered table-hover" width="100%"> -->
<!--                     <tbody> -->
<!--                         <tr> -->
<!--                             <th scope="row" class="col-xs-3"> -->
<%--                                 <spring:message code="label.VatType.sumValueWithoutVat" /> --%>
<!--                             </th> -->
<!--                             <td> -->
<%--                                 <c:out value='${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( sumAmount ) }' /> --%>
<!--                             </td> -->
<!--                         </tr> -->
<!--                         <tr> -->
<!--                             <th scope="row" class="col-xs-3"> -->
<%--                                 <spring:message code="label.VatType.sumValueOfVat" /> --%>
<!--                             </th> -->
<!--                             <td> -->
<%--                                 <c:out value='${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( sumAmountWithVat.subtract(sumAmount) ) }' /> --%>
<!--                             </td> -->
<!--                         </tr> -->
<!--                         <tr> -->
<!--                             <th scope="row" class="col-xs-3"> -->
<%--                                 <spring:message code="label.VatType.sumValueWithVat" /> --%>
<!--                             </th> -->
<!--                             <td> -->
<%--                                 <c:out value='${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( sumAmountWithVat ) }' /> --%>
<!--                             </td> -->
<!--                         </tr> -->
<!--                     </tbody> -->
<!--                 </table> -->
<!--             </div> -->
<!--         </div>  -->
<!--     </div> -->
<!-- </div> -->

<form id='summaryForm' name='form' method="post" class="form-horizontal"
    ng-app="angularAppSettlementNote"
    ng-controller="SettlementNoteController"
    action='${pageContext.request.contextPath}<%= SettlementNoteController.SUMMARY_URL %>'>

    <input name="bean" type="hidden" value="{{ object }}" />

    <div class="panel-footer">
        <button type="button" class="btn btn-default"
            ng-click="processBackSubmit('${pageContext.request.contextPath}')">
            <span class="glyphicon glyphicon-chevron-left" aria-hidden="true" > </span>&nbsp;<spring:message code="label.event.back" />
        </button>
        <button type="button" class="btn btn-primary"
            onClick="javascript:processSubmit('${pageContext.request.contextPath}<%= SettlementNoteController.SUMMARY_URL %>')">
            <span class="glyphicon glyphicon-ok" aria-hidden="true" >&nbsp;</span><spring:message code="label.finish" />
        </button>
    </div>
</form>

<script>
	$(document).ready(function() {

	});
</script>
