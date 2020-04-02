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
        <spring:message code="label.document.manageInvoice.updateDebitEntry" />
        <small></small>
    </h1>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;
    <c:if test="${ not empty debitEntry.finantialDocument}">
        <a class="" href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${debitEntry.finantialDocument.externalId}"><spring:message
                code="label.event.back" /></a>
    </c:if>
    <c:if test="${ empty debitEntry.finantialDocument}">
        <a class="" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${debitEntry.debtAccount.externalId}"><spring:message
                code="label.event.back" /></a>
    </c:if>

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
	angular.module('angularAppDebitEntry', [ 'ngSanitize', 'ui.select','bennuToolkit' ])
			.controller('DebitEntryController', [ '$scope', function($scope) {
                
				$scope.booleanvalues = [ {
                    name : '<spring:message code="label.no"/>',
                    value : false
                }, {
                    name : '<spring:message code="label.yes"/>',
                    value : true
                } ];

	           
				$scope.object = angular.fromJson('${debitEntryBeanJson}');
				$scope.postBack = createAngularPostbackFunction($scope);

				$scope.clear = function($event) {
					   $event.stopPropagation(); 
					   $scope.object.treasuryEvent = undefined;
					};
					
					
				//Begin here of Custom Screen business JS - code

			} ]);
</script>

<form name='form' method="post" class="form-horizontal" ng-app="angularAppDebitEntry" ng-controller="DebitEntryController"
    action='${pageContext.request.contextPath}/treasury/document/manageinvoice/debitentry/update/${debitEntry.externalId}'>

    <input type="hidden" name="postback" value='${pageContext.request.contextPath}treasury/document/manageinvoice/debitentry/updatepostback/${debitEntry.externalId}' /> <input
        name="bean" type="hidden" value="{{ object }}" />

    <div class="panel panel-default">
        <div class="panel-body">
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebtAccount.finantialInstitution" />
                </div>

                <div class="col-sm-10">
                    <div class="form-control">
                        <c:out value="${debitEntryBean.debtAccount.finantialInstitution.name}" />
                    </div>
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebitEntry.debtAccount" />
                </div>

                <div class="col-sm-10">
                    <div class="form-control">
                        <c:out value="${debitEntryBean.debtAccount.customer.businessIdentification} - ${debitEntryBean.debtAccount.customer.name}" />
                    </div>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebitEntry.finantialDocument" />
                </div>

                <div class="col-sm-10">
                    <div class="form-control">
                        <c:if test="${not empty debitEntry.finantialDocument}">
                            <c:out value='${debitEntry.finantialDocument.uiDocumentNumber}' />
                        </c:if>
                        <c:if test="${empty debitEntry.finantialDocument}">
                            <spring:message code="label.DebitEntry.debitentry.with.no.document" />
                        </c:if>
                    </div>
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.FinantialDocumentEntry.entryDateTime" />
                </div>

                <div class="col-sm-10">
                    <div class="form-control">
                        <c:out value='${debitEntry.entryDateTime.toString("YYYY-MM-dd HH:mm")}' />
                    </div>
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebitEntry.product" />
                </div>

                <div class="col-sm-10">
                    <div class="form-control">
                        <c:out value="${debitEntryBean.product.name.content}" />
                    </div>
                </div>
            </div>

            <c:if test='${not debitEntryBean.isAmountValuesEditable() }'>
                <div class="form-group row">
                    <div class="col-sm-2 control-label">
                        <spring:message code="label.DebitEntry.description" />
                    </div>

                    <div class="col-sm-10">
                        <div class="form-control">
                            <c:out value='${debitEntryBean.description}' />
                        </div>
                    </div>
                </div>
                <div class="form-group row">
                    <div class="col-sm-2 control-label">
                            <spring:message code="label.DebitEntry.amount" />
                    </div>

                    <div class="col-sm-10">
                        <div class="form-control">
                            <c:out value='${debitEntryBean.debtAccount.finantialInstitution.currency.getValueFor(debitEntryBean.amount)}' />
                        </div>
                    </div>
                </div>
                <div class="form-group row">
                    <div class="col-sm-2 control-label">
                        <spring:message code="label.InvoiceEntry.quantity" />
                    </div>

                    <div class="col-sm-10">
                        <div class="form-control">
                            <c:out value='${debitEntryBean.quantity}' />
                        </div>
                    </div>
                </div>
                
                <div class="form-group row">
                    <div class="col-sm-2 control-label">
                        <spring:message code="label.InvoiceEntry.totalAmount" />
                    </div>

                    <div class="col-sm-10">
                        <div class="form-control">
                            <c:out value='${debitEntryBean.debtAccount.finantialInstitution.currency.getValueFor(debitEntryBean.debitEntry.totalAmount)}' />
                        </div>
                    </div>
                </div>
            </c:if>

            <!-- <div class="form-group row"> -->
            <%-- <div class="col-sm-2 control-label"><spring:message code="label.DebitEntry.product"/></div>  --%>

            <!-- <div class="col-sm-4"> -->
            <%-- Relation to side 1 drop down rendered in input --%>
            <!-- 		<ui-select id="debitEntry_product" class="form-control" name="product" ng-model="$parent.object.product" theme="bootstrap" ng-disabled="disabled" > -->
            <!--     						<ui-select-match >{{$select.selected.text}}</ui-select-match> -->
            <!--     						<ui-select-choices repeat="product.id as product in object.productDataSource | filter: $select.search"> -->
            <!--       							<span ng-bind-html="product.text | highlight: $select.search"></span> -->
            <!--     						</ui-select-choices> -->
            <!--   						</ui-select>				 -->
            <!-- 				</div> -->
            <!-- </div>		 -->


        </div>
    </div>
    <div class="panel panel-default">
        <div class="panel-body">

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebitEntry.dueDate" />
                </div>

                <div class="col-sm-4">
                    <input id="debitEntry_dueDate" class="form-control" type="text" bennu-date="object.dueDate" />
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebitEntry.event" />
                </div>

                <div class="col-sm-10">
                    <ui-select ng-model="$parent.object.treasuryEvent" theme="bootstrap" ng-disabled="disabled"> 
                    <ui-select-match>
                    <a   class="btn btn-xs clear" ng-click="clear($event)">X</a>
                    <span>{{$select.selected.text}}</span>
                    </ui-select-match>
                    <ui-select-choices repeat="event.id as event in object.treasuryEventDataSource| filter: $select.search"> <span
                        ng-bind-html="event.text | highlight: $select.search"></span> </ui-select-choices> </ui-select>
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.FixedTariff.applyInterests" />
                </div>
                <div class="col-sm-2">
                    <select id="debitEntry_applyInterests" name="applyinterests" class="form-control" ng-model="object.applyInterests"
                        ng-options="bvalue.value as bvalue.name for bvalue in booleanvalues">
                    </select>
                </div>
            </div>

            <div class="form-group row" ng-show="object.applyInterests">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.FixedTariff.interestType" />
                </div>

                <div class="col-sm-4">
                    <%-- Relation to side 1 drop down rendered in input --%>
                    <ui-select id="debitEntry_interestType" name="vattype" ng-model="$parent.object.interestRate.interestType" theme="bootstrap" ng-disabled="disabled">
                    <ui-select-match>{{$select.selected.text}}</ui-select-match> 
                    <ui-select-choices
                        repeat="interestType.id as interestType in object.interestRate.interestTypeDataSource | filter: $select.search"> <span
                        ng-bind-html="interestType.text | highlight: $select.search"></span> </ui-select-choices> </ui-select>
                </div>
            </div>
<!--             <div class="form-group row" ng-show="object.applyInterests && object.interestRate.interestType=='DAILY'"> -->
<!--                 <div class="col-sm-2 control-label"> -->
<%--                     <spring:message code="label.InterestRate.numberOfDaysAfterDueDate" /> --%>
<!--                 </div> -->
<!--                 <div class="col-sm-4"> -->
<!--                     <input id="debitEntry_numberOfDaysAfterDueDate" class="form-control" type="text" ng-model="object.interestRate.numberOfDaysAfterDueDate" -->
<!--                         name="numberOfDaysAfterDueDate" pattern="^\d+$" /> -->
<!--                 </div> -->
<!--             </div> -->

            <div class="form-group row" ng-show="object.applyInterests && object.interestRate.interestType=='DAILY'">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.InterestRate.applyInFirstWorkday" />
                </div>
                <div class="col-sm-4">
                    <select id="debitEntry_applyInFirstWorkday" name="applyInFirstWorkday" class="form-control" ng-model="object.interestRate.applyInFirstWorkday"
                        ng-options="bvalue.value as bvalue.name for bvalue in booleanvalues">
                    </select>
                </div>
            </div>

            <div class="form-group row" ng-show="object.applyInterests && object.interestRate.interestType=='DAILY'">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.InterestRate.maximumDaysToApplyPenalty" />
                </div>
                <div class="col-sm-4">
                    <input id="debitEntry_maximumDaysToApplyPenalty" class="form-control" type="text" ng-model="object.interestRate.maximumDaysToApplyPenalty"
                        name="maximumDaysToApplyPenalty" pattern="^\d+$" />
                </div>
            </div>

            <div class="form-group row" ng-show="object.applyInterests && object.interestRate.interestType=='FIXED_AMOUNT'">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.InterestRate.interestFixedAmount" />
                </div>
                <div class="col-sm-4">
                    <div class="input-group">
                        <div class="input-group-addon">
                            <c:out value="${debitEntry.currency.symbol}" />
                        </div>
                        <input id="debitEntry_interestFixedAmount" class="form-control" type="text" ng-model="object.interestRate.interestFixedAmount" name="interestFixedAmount"
                            pattern="[0-9]+(\.[0-9]{1,3})?" />
                    </div>
                </div>
            </div>

            <div class="form-group row" ng-show="object.interestRate.interestType != 'FIXED_AMOUNT' && object.applyInterests && object.interestRate.interestType != 'GLOBAL_RATE'">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.InterestRate.rate" />
                </div>
                <div class="col-sm-4">
                    <input id="debitEntry_rate" class="form-control" type="text" ng-model="object.interestRate.rate" name="rate"
                        pattern="^100(\.0{1,2})?|[0-9]{1,2}(\.[0-9]{1,2})?$" />
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebitEntry.academicalActBlockingSuspension" />
                </div>
                <div class="col-sm-2">
                    <select id="debitEntry_academicalActBlockingSuspension" name="academicalactblockingsuspension" class="form-control" 
                    	ng-model="object.academicalActBlockingSuspension"
                        ng-options="bvalue.value as bvalue.name for bvalue in booleanvalues">
                    </select>
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebitEntry.blockAcademicActsOnDebt" />
                </div>
                <div class="col-sm-2">
                    <select id="debitEntry_blockAcademicActsOnDebt" name="blockacademicactsondebt" class="form-control" 
                    	ng-model="object.blockAcademicActsOnDebt"
                        ng-options="bvalue.value as bvalue.name for bvalue in booleanvalues">
                    </select>
                </div>
            </div>
        </div>
        
        <div class="panel-footer">
            <input type="submit" class="btn btn-default" role="button" value="<spring:message code="label.submit" />" />
        </div>
    </div>
</form>

<script>
	$(document).ready(function() {


	});
</script>
