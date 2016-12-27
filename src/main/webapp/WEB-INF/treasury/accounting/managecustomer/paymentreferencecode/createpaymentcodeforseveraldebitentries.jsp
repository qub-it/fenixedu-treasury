<%@page import="org.fenixedu.treasury.ui.accounting.managecustomer.PaymentReferenceCodeController"%>
<%@page import="org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
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
<%--${portal.toolkit()}--%>

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
        <spring:message code="label.document.managePayments.createPaymentCodeForSeveralDebitEntries" />
        <small></small>
    </h1>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a class=""
        href="${pageContext.request.contextPath}<%= DebtAccountController.READ_URL %>/${debtAccount.externalId}"><spring:message
            code="label.event.back" /></a> &nbsp;
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
	angular
.module('angularAppPaymentReferenceCode',
		[ 'ngSanitize', 'ui.select', 'bennuToolkit' ])
.controller(
		'PaymentReferenceCodeController',
		[
				'$scope',
				function($scope) {
					$scope.booleanvalues = [
							{
								name : '<spring:message code="label.no"/>',
								value : false
							},
							{
								name : '<spring:message code="label.yes"/>',
								value : true
							} ];

					$scope.object = angular.fromJson('${beanJson}');
					$scope.postBack = createAngularPostbackFunction($scope);

					//Begin here of Custom Screen business JS - code

					$scope.toggleDebitEntries = function toggleSelection(debitEntryId) {
						if($scope.object.selectedDebitEntries === undefined) {
							$scope.object.selectedDebitEntries = [];
						}
						
						var idx = $scope.object.selectedDebitEntries.indexOf(debitEntryId);
						
						// is currently selected
						if (idx > -1) {
						  $scope.object.selectedDebitEntries.splice(idx, 1);
						} else {
							// is newly selected
						  $scope.object.selectedDebitEntries.push(debitEntryId);
						}
						
						$scope.postBack();
					};
					
				} ]);
</script>

<form name='form' method="post" class="form-horizontal" ng-app="angularAppPaymentReferenceCode" ng-controller="PaymentReferenceCodeController"
    action='${pageContext.request.contextPath}<%= PaymentReferenceCodeController.CREATEPAYMENTCODEFORSEVERALDEBITENTRIES_URL %>/${debtAccount.externalId}'>

    <input type="hidden" name="postback" value='${pageContext.request.contextPath}<%= PaymentReferenceCodeController.CREATEPAYMENTCODEFORSEVERALDEBITENTRIESPOSTBACK_URL %>/${debtAccount.externalId}' />
    <input name="bean" type="hidden" value="{{ object }}" />

    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">
                <spring:message code="label.document.managePayments.DebitEntry.select" />
            </h3>
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
                        <th class="col-sm-1"><spring:message code="label.DebitEntry.openAmount" /></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${bean.openDebitEntries}" var="debitEntry" varStatus="loop">
						<tr>
	                        <td>
	                        	<input class="form-control" name="${debitEntry.externalId}" 
	                        		id="${debitEntry.externalId}" 
	                        		ng-checked="object.selectedDebitEntries.indexOf('${debitEntry.externalId}') > -1" 
	                        		ng-click="toggleDebitEntries('${debitEntry.externalId}')" type="checkbox" />
	                        </td>
	                        <td><c:out value="${ debitEntry.finantialDocument.uiDocumentNumber }" /></td>
	                        <td>
	                        	<p><c:out value="${ debitEntry.description }" /></p>
	                        	<c:if test="${debitEntry.finantialDocument != null}">
	                        	<c:if test="${debitEntry.finantialDocument.forPayorDebtAccount}">
	                        		<p>
	                        			<em>
	                        				<strong><spring:message code="label.Invoice.payorDebtAccount" />:</strong> 
	                        				<span><c:out value="${debitEntry.finantialDocument.payorDebtAccount.customer.fiscalNumber}" /></span>
	                        				&nbsp;-&nbsp;
	                        				<span><c:out value="${debitEntry.finantialDocument.payorDebtAccount.customer.name}" /></span>
	                        			</em>
	                        	</c:if>
	                        	</c:if>
	                        </td>
	                        <td><c:out value="${ debtAccount.finantialInstitution.currency.getValueFor(debitEntry.openAmount) }" /></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="panel panel-default">
        <div class="panel-body">
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.PaymentCodePool.finantialInstitution" />
                </div>

                <div class="col-sm-10">
                    <input class="col-sm-12" type="text" value="<c:out value='${debtAccount.finantialInstitution.name}'/>" disabled />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebtAccount.customer" />
                </div>

                <div class="col-sm-10">
                    <input class="col-sm-12" type="text"
                        value="<c:out value='${debtAccount.customer.businessIdentification} - ${debtAccount.customer.name}'/>"
                        disabled />
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.PaymentReferenceCode.paymentCodePool" />
                </div>

                <div class="col-sm-10">
                    <%-- Relation to side 1 drop down rendered in input --%>
                    <ui-select id="paymentReferenceCode_paymentCodePool" name="paymentcodepool" on-select="onPoolChange($item, $model)" ng-model="$parent.object.paymentCodePool"
                        theme="bootstrap" ng-disabled="disabled"> <ui-select-match>{{$select.selected.text}}</ui-select-match> <ui-select-choices
                        repeat="paymentCodePool.id as paymentCodePool in object.paymentCodePoolDataSource | filter: $select.search"> <span
                        ng-bind-html="paymentCodePool.text | highlight: $select.search"></span> </ui-select-choices> </ui-select>
                </div>
            </div>

            <div class="form-group row" ng-show=" object.usePaymentAmountWithInterests == false ">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.PaymentReferenceCode.payableAmount" />
                </div>
                <div class="col-sm-10">
                    <div class="input-group">
                        <div class="input-group-addon">
                            <c:out value="${debtAccount.finantialInstitution.currency.symbol}" />
                        </div>
                        <input  class="" type="text" ng-model="object.paymentAmount" ng-readonly="object.useCustomPaymentAmount == false" />
                    </div>
                </div>
            </div>
        </div>

        <div class="panel-footer">
            <input type="submit" class="btn btn-default" role="button" value="<spring:message code="label.cancel" />" />
            <input type="submit" class="btn btn-default" role="button" value="<spring:message code="label.submit" />" />
        </div>
    </div>

</form>

<script>
	$(document).ready(function() {

	});
</script>
