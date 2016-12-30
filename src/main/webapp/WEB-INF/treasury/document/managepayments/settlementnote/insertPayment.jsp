<%@page
    import="org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController"%>
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
                <strong><spring:message code="label.Customer.fiscalNumber" />:</strong>&nbsp;<c:out value="${settlementNoteBean.debtAccount.customer.uiFiscalNumber}" />
            </p>
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
        <b>4. <spring:message
                code="label.administration.manageCustomer.createSettlementNote.insertpayment" /></b>
        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span>
        5.
        <spring:message
            code="label.administration.manageCustomer.createSettlementNote.summary" />
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
								if ($scope.object.previousStates[$scope.object.previousStates.length - 1] != 3) {
									$scope.object.previousStates.push(3);
								}
								$scope.getTotal = function() {
									var total = 0;
									for (var i = 0; i < $scope.object.paymentEntries.length; i++) {
										total += parseFloat($scope.object.paymentEntries[i].paymentAmount);
									}
									return total.toFixed(2);
								}
								$scope.addPaymentMethod = function() {
									if ($scope.paymentAmount === undefined) {
										return;
									}
									$scope.object.paymentEntries.push({
										paymentAmount : parseFloat($scope.paymentAmount).toFixed(2),
										paymentMethod : $scope.paymentMethod.id,
										paymentMethodId: $scope.paymentMethodId
									});
									$scope.paymentAmount = undefined;
								}
								$scope.getPaymentName = function(id) {
									var name;
									angular.forEach(
											$scope.object.paymentMethods,
											function(paymentMethod) {
												if (paymentMethod.id == id) {
													name = paymentMethod.text;
												}
											}, id, name)
									return name;
								}
								$scope.processBackSubmit = function(contextPath) {
									$scope.object.previousStates.pop();
									var path = contextPath
											+ $scope.object.settlementNoteStateUrls[$scope.object.previousStates
													.pop()];
									$("#insertPaymentForm")
											.attr("action", path);
									//Timeout necessary to make angular update the previousStates array before submit
									setTimeout(function() {
										$("#insertPaymentForm").submit()
									}, 10);
								}
								$scope.currencySymbol = "${ settlementNoteBean.debtAccount.finantialInstitution.currency.symbol }";
							} ]);
</script>

<script type="text/javascript">
	function processSubmit(url) {
		$("#insertPaymentForm").attr("action", url);
		$("#insertPaymentForm").submit();
	}
</script>

<form id='insertPaymentForm' name='form' method="post"
    class="form-horizontal" ng-app="angularAppSettlementNote"
    ng-controller="SettlementNoteController"
    action='${pageContext.request.contextPath}<%= SettlementNoteController.CREATE_DEBIT_NOTE_URL %>'>

    <input name="bean" type="hidden" value="{{ object }}" />

    <div class="panel panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title">
                <spring:message
                    code="label.document.managepayments.settlementnote.InvoiceEntries" />
            </h3>
        </div>
        <div class="panel-body">
            <c:if test="${ settlementNoteBean.hasEntriesWithoutDocument() }">
                <table id="debitNoteTable"
                    class="table responsive table-bordered table-hover" width="100%">
                    <col style="width: 20%" />
                    <col style="width: 40%" />
                    <col style="width: 15%" />
                    <col style="width: 10%" />
                    <col style="width: 15%" />
                    <thead>
                        <tr>
                            <th><spring:message
                                    code="label.DebitEntry.documentNumber" /></th>
                            <th><spring:message
                                    code="label.DebitEntry.description" /></th>
                            <th><spring:message
                                    code="label.DebitEntry.dueDate" /></th>
                            <th><spring:message
                                    code="label.DebitEntry.vat" /></th>
                            <th><spring:message
                                    code="label.DebitEntry.amountWithVat" /></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:set var="debitNoteDate"
                            value='${settlementNoteBean.debitNoteDate.toString("yyyy-MM-dd")}' />
                        <c:forEach
                            items="${ settlementNoteBean.debitEntries }"
                            var="debitEntryBean">
                            <c:if
                                test="${ debitEntryBean.included && empty debitEntryBean.debitEntry.finantialDocument  }">
                                <tr>
                                    <td>---</td>
                                    <td>
                                    	<p><c:out value="${ debitEntryBean.debitEntry.description }" /></p>
			                        	<c:if test="${debitEntryBean.debitEntry.finantialDocument != null}">
			                        	<c:if test="${debitEntryBean.debitEntry.finantialDocument.forPayorDebtAccount}">
			                        		<p>
			                        			<em>
			                        				<strong><spring:message code="label.Invoice.payorDebtAccount" />:</strong> 
			                        				<span><c:out value="${debitEntryBean.debitEntry.finantialDocument.payorDebtAccount.customer.fiscalNumber}" /></span>
			                        				&nbsp;-&nbsp;
			                        				<span><c:out value="${debitEntryBean.debitEntry.finantialDocument.payorDebtAccount.customer.name}" /></span>
			                        			</em>
			                        	</c:if>
			                        	</c:if>
                                    </td>
                                    <td><c:out
                                            value='${ debitNoteDate }' /></td>
                                    <td><c:out
                                            value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueWithScale(  debitEntryBean.debitEntry.vat.taxRate ) }" /></td>
                                    <td><c:out
                                            value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( debitEntryBean.debtAmountWithVat ) }" /></td>
                                </tr>
                            </c:if>
                        </c:forEach>
                        <c:forEach
                            items="${ settlementNoteBean.interestEntries }"
                            var="interestEntryBean">
                            <c:if
                                test="${ interestEntryBean.included  }">
                                <tr>
                                    <td>---</td>
                                    <td>
                                    	<p><spring:message code="label.InterestEntry.interest" /> &nbsp; <c:out value="${ interestEntryBean.debitEntry.description }" /></p>
			                        	<c:if test="${interestEntryBean.debitEntry.finantialDocument != null}">
			                        	<c:if test="${interestEntryBean.debitEntry.finantialDocument.forPayorDebtAccount}">
			                        		<p>
			                        			<em>
			                        				<strong><spring:message code="label.Invoice.payorDebtAccount" />:</strong> 
			                        				<span><c:out value="${interestEntryBean.debitEntry.finantialDocument.payorDebtAccount.customer.fiscalNumber}" /></span>
			                        				&nbsp;-&nbsp;
			                        				<span><c:out value="${interestEntryBean.debitEntry.finantialDocument.payorDebtAccount.customer.name}" /></span>
			                        			</em>
			                        	</c:if>
			                        	</c:if>
                                    </td>
                                    <td><c:out
                                            value='${ debitNoteDate }' /></td>
                                    <td>0.00</td>
                                    <td><c:out
                                            value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( interestEntryBean.interest.interestAmount ) }" />
                                    </td>
                                </tr>
                            </c:if>
                        </c:forEach>
                    </tbody>
                </table>
            </c:if>
            <table id="debitEntriesTable"
                class="table responsive table-bordered table-hover" width="100%">
                <col style="width: 20%" />
                <col style="width: 40%" />
                <col style="width: 15%" />
                <col style="width: 10%" />
                <col style="width: 15%" />
                <thead>
                    <tr>
                        <th><spring:message
                                code="label.DebitEntry.documentNumber" /></th>
                        <th><spring:message
                                code="label.DebitEntry.description" /></th>
                        <th><spring:message
                                code="label.DebitEntry.dueDate" /></th>
                        <th><spring:message
                                code="label.DebitEntry.vat" /></th>
                        <th><spring:message
                                code="label.DebitEntry.amountWithVat" /></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach
                        items="${ settlementNoteBean.debitEntries }"
                        var="debitEntryBean">
                        <c:if
                            test="${ debitEntryBean.included && not empty debitEntryBean.debitEntry.finantialDocument  }">
                            <tr>
                                <td><c:out
                                        value="${ debitEntryBean.debitEntry.finantialDocument.uiDocumentNumber }" /></td>
                                <td>
                                	<p><c:out value="${ debitEntryBean.debitEntry.description }" /></p>
		                        	<c:if test="${debitEntryBean.debitEntry.finantialDocument != null}">
		                        	<c:if test="${debitEntryBean.debitEntry.finantialDocument.forPayorDebtAccount}">
		                        		<p>
		                        			<em>
		                        				<strong><spring:message code="label.Invoice.payorDebtAccount" />:</strong> 
		                        				<span><c:out value="${debitEntryBean.debitEntry.finantialDocument.payorDebtAccount.customer.fiscalNumber}" /></span>
		                        				&nbsp;-&nbsp;
		                        				<span><c:out value="${debitEntryBean.debitEntry.finantialDocument.payorDebtAccount.customer.name}" /></span>
		                        			</em>
		                        	</c:if>
		                        	</c:if>
                                </td>
                                <td><c:out
                                        value="${ debitEntryBean.documentDueDate }" /></td>
                                <td><c:out
                                        value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueWithScale( debitEntryBean.debitEntry.vat.taxRate ) }" />
                                </td>
                                <td><c:out
                                        value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor( debitEntryBean.debtAmountWithVat ) }" /></td>
                            </tr>
                        </c:if>
                    </c:forEach>
                    <c:forEach
                        items="${ settlementNoteBean.creditEntries}"
                        var="creditEntryBean" varStatus="loop">
                        <c:if test="${ creditEntryBean.included }">
                            <tr>
                                <td><c:out value="${ creditEntryBean.creditEntry.finantialDocument.uiDocumentNumber }" /></td>
                                <td>
                                	<p><c:out value="${ creditEntryBean.creditEntry.description }" /></p>
		                        	<c:if test="${creditEntryBean.creditEntry.finantialDocument != null}">
		                        	<c:if test="${creditEntryBean.creditEntry.finantialDocument.forPayorDebtAccount}">
		                        		<p>
		                        			<em>
		                        				<strong><spring:message code="label.Invoice.payorDebtAccount" />:</strong> 
		                        				<span><c:out value="${creditEntryBean.creditEntry.finantialDocument.payorDebtAccount.customer.fiscalNumber}" /></span>
		                        				&nbsp;-&nbsp;
		                        				<span><c:out value="${creditEntryBean.creditEntry.finantialDocument.payorDebtAccount.customer.name}" /></span>
		                        			</em>
		                        	</c:if>
		                        	</c:if>
                                </td>
                                <td><c:out
                                        value="${ creditEntryBean.documentDueDate }" /></td>
                                <td><c:out
                                        value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueWithScale( creditEntryBean.creditEntry.vat.taxRate ) }" /></td>
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
                <c:if
                    test="${ not settlementNoteBean.reimbursementNote }">
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
                        <c:if test="${ settlementNoteBean.reimbursementNote }">
                            <th><spring:message code="label.ReimbursementMethod" /></th>
                        </c:if>
                        <c:if test="${ not settlementNoteBean.reimbursementNote }">
                            <th><spring:message code="label.PaymentMethod" /></th>
                        </c:if>
                        <th><spring:message code="label.PaymentMethod.value" /></th>
						<th><spring:message code="label.PaymentMethod.paymentMethodId" /></th>
                        <!-- operation column -->
                        <th style="width: 15%"></th>
                    </tr>
                </thead>
                <tbody>
                    <tr
                        ng-repeat="paymentEntryBean in object.paymentEntries">
                        <td>{{ getPaymentName(paymentEntryBean.paymentMethod ) }}</td>
                        <td>{{ paymentEntryBean.paymentAmount + " " + currencySymbol }}</td>
                        <td>{{ paymentEntryBean.paymentMethodId }}</td>
                        <td>
                            <button type="button"
                                class="btn btn-default"
                                ng-click="object.paymentEntries.splice($index,1);">
                                <span class="glyphicon glyphicon-trash"
                                    aria-hidden="true"></span> &nbsp;
                                <spring:message
                                    code="label.event.delete" />
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
            <div class="panel-footer">
                <c:if test="${ settlementNoteBean.reimbursementNote }">
                    <p align="right">
                        <b><spring:message
                                code="label.SettlementNote.reimbursementTotal" /></b>:
                        {{ getTotal() + " " + currencySymbol }}
                    </p>
                </c:if>
                <c:if
                    test="${ not settlementNoteBean.reimbursementNote }">
                    <p align="right">
                        <b><spring:message
                                code="label.SettlementNote.paymentTotal" /></b>:
                        {{ getTotal() + " " + currencySymbol }}
                    </p>
                </c:if>
            </div>
        </div>
        <div class="panel-body">
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <c:if
                        test="${ settlementNoteBean.reimbursementNote }">
                        <spring:message code="label.ReimbursementMethod" />
                    </c:if>
                    <c:if
                        test="${ not settlementNoteBean.reimbursementNote }">
                        <spring:message code="label.PaymentMethod" />
                    </c:if>
                </div>
                <div class="col-sm-4">
                    <ui-select id="settlementNote_paymentMethod"
                        ng-model="$parent.paymentMethod"
                        theme="bootstrap" ng-disabled="disabled">
                    <ui-select-match>{{$select.selected.text}}</ui-select-match>
                    <ui-select-choices
                        repeat="paymentMethod as paymentMethod in object.paymentMethods | filter: $select.search">
                    <span ng-bind-html="paymentMethod.text"></span> </ui-select-choices> </ui-select>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.PaymentMethod.base.value" />
                </div>
                <div class="col-sm-4">
                    <div class="input-group">
                        <div class=" input-group-addon">
                            <c:out
                                value="${settlementNoteBean.debtAccount.finantialInstitution.currency.symbol}" />
                        </div>
                        <input id="settlementNote_paymentAmount" class="form-control" type="text" name="paymentAmount" ng-model="paymentAmount"
                            ng-pattern="/^(0*\.(0[1-9]|[1-9][0-9]?)|[1-9][0-9]*(\.[0-9]{1,2})?)$/" />
                    </div>
                </div>
            </div>
            <div class="form-group row">
            	<div class="col-sm-2 control-label">
            		<spring:message code="label.PaymentMethod.paymentMethodId" />
            	</div>
            	<div class="col-sm-4">
	                <input id="settlementNote_paymentMethodId" class="form-control" type="text" name="paymentMethodId" ng-model="paymentMethodId" />
            	</div>
            </div>
            <p class="alert alert-danger"
                ng-show="form.paymentAmount.$error.pattern">
                <spring:message code="error.invalid.format.input" />
            </p>
            <div class="panel-footer">
                <button type="button" class="btn btn-default" ng-click="addPaymentMethod()" ng-disabled="form.paymentAmount.$error.pattern || !paymentAmount.length">
                    <span class="glyphicon glyphicon-plus-sign" aria-hidden="true" ></span> &nbsp;<spring:message code="label.event.add" />
                </button>
            </div>
        </div>
    </div>
    <div class="panel-footer">
        <button type="button" class="btn btn-default"
            ng-click="processBackSubmit('${pageContext.request.contextPath}')">
            <span class="glyphicon glyphicon-chevron-left"
                aria-hidden="true"></span> &nbsp;
            <spring:message code="label.event.back" />
        </button>
        <button type="button" class="btn btn-primary"
            onClick="javascript:processSubmit('${pageContext.request.contextPath}<%= SettlementNoteController.INSERT_PAYMENT_URL %>')">
            <spring:message code="label.continue" />
            &nbsp;<span class="glyphicon glyphicon-chevron-right"
                aria-hidden="true"></span>
        </button>
    </div>
</form>

<script>
	$(document).ready(function() {
	});
</script>
