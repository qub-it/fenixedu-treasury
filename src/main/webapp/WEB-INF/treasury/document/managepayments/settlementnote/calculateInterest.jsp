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

<link href="${pageContext.request.contextPath}/static/treasury/css/dataTables.responsive.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/static/treasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<script src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<!--  ${portal.toolkit()} -->
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
        <spring:message code="label.administration.manageCustomer.createSettlementNote.calculateInterest" />
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
                <strong><spring:message code="label.Customer.fiscalNumber" />:</strong>&nbsp;<c:out value="${settlementNoteBean.debtAccount.customer.uiFiscalNumber}" />
            </p>
        </div>

    </div>
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
	angular.module('angularAppSettlementNote',
			[ 'ngSanitize', 'ui.select', 'bennuToolkit' ]).controller(
			'SettlementNoteController', [ '$scope', function($scope) {
				$scope.object = angular.fromJson('${settlementNoteBeanJson}');
                if($scope.object.previousStates[$scope.object.previousStates.length - 1] != 1) {
                    $scope.object.previousStates.push(1);
                }
				$scope.processBackSubmit = function(contextPath) {
                    $scope.object.previousStates.pop();
                    var path = contextPath + $scope.object.settlementNoteStateUrls[$scope.object.previousStates.pop()];
                    $("#calculateInterestForm").attr("action", path);
                    //Timeout necessary to make angular update the previousStates array before submit
                    setTimeout(function() { $("#calculateInterestForm").submit() }, 10);                    
                }
			} ]);
</script>

<script type="text/javascript">
	function processSubmit(url) {
		$("#calculateInterestForm").attr("action", url);
		$("#calculateInterestForm").submit();
	}
</script>

<div>
    <p>
        1.
        <spring:message code="label.administration.manageCustomer.createSettlementNote.chooseInvoiceEntries" />
        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span> <b>2. <spring:message
                code="label.administration.manageCustomer.createSettlementNote.calculateInterest" /></b> <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span> 3.
        <spring:message code="label.administration.manageCustomer.createSettlementNote.createDebitNote" />
        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span> 4.
        <spring:message code="label.administration.manageCustomer.createSettlementNote.insertpayment" />
        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span> 5.
        <spring:message code="label.administration.manageCustomer.createSettlementNote.summary" />
    </p>
</div>

<form id='calculateInterestForm' name='form' method="post" class="form-horizontal" ng-app="angularAppSettlementNote" ng-controller="SettlementNoteController"
    action='${pageContext.request.contextPath}<%= SettlementNoteController.CALCULATE_INTEREST_URL %>'>

    <input name="bean" type="hidden" value="{{ object }}" />

    <div class="panel panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title">
                <spring:message code="label.InterestEntry" />
            </h3>
            <p>
                <spring:message code="label.InterestEntry.choose" />
            </p>
        </div>
        <div class="panel-body">
            <table id="debitEntriesTable" class="table responsive table-bordered table-hover" width="100%">
                <col style="width: 3%" />
                <thead>
                    <tr>
                        <%-- Check Column --%>
                        <th style="min-width: 35px"></th>
                        <th><spring:message code="label.InterestEntry.description" /></th>
                        <th><spring:message code="label.InterestEntry.interestDescription" /></th>
                        <th><spring:message code="label.InterestEntry.date" /></th>
                        <th><spring:message code="label.InterestEntry.amount" /></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${ settlementNoteBean.interestEntries}" var="interestEntryBean" varStatus="loop">
                        <tr>
                            <td><input class="form-control" ng-model="object.interestEntries[${ loop.index }].isIncluded" type="checkbox" /></td>
                            <td><spring:message code="label.InterestEntry.interest" />: &nbsp;<c:out value="${ interestEntryBean.debitEntry.description }" /></td>
                            <td>
                                <p>
                                    <strong><spring:message code="label.InterestEntry.calculatedInterest" /> </strong>
                                </p>
                                <p>&nbsp;</p> <c:forEach var="detail" items="${interestEntryBean.interest.interestInformationList}">
                                    <p>
                                        [
                                        <c:out value="${detail.begin}" />
                                        -
                                        <c:out value="${detail.end}"  />
                                        ]: ${settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor(detail.amount, 4)}
                                    </p>
                                    <p style="">
                                        <em><spring:message code="label.InterestEntry.affectedAmount.description"
                                                arguments="${settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor(detail.affectedAmount)},${detail.numberOfDays}" /></em>
                                    </p>
                                    <p>&nbsp;</p>
                                </c:forEach>
                                <p>&nbsp;</p> <c:if test='${ not empty  interestEntryBean.interest.createdInterestEntriesList}'>
                                    <p>
                                        <strong><spring:message code="label.InterestEntry.createdInterest" /> </strong>
                                    </p>
                                    <p>&nbsp;</p>
                                    <c:forEach var="interestEntry" items="${interestEntryBean.interest.createdInterestEntriesList}">
                                        <p>
                                            [
                                            <joda:format value="${interestEntry.entryDate}" style="S-" />
                                            ]: ${settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor(interestEntry.amount)}
                                        </p>
                                    </c:forEach>
                                </c:if>
                            </td>
                            <td><c:out value="${ interestEntryBean.documentDueDate}" /></td>
                            <td><c:out value="${ settlementNoteBean.debtAccount.finantialInstitution.currency.getValueFor(interestEntryBean.interest.interestAmount) }" /></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        <div class="panel-footer">
            <button type="button" class="btn btn-default"
                ng-click="processBackSubmit('${pageContext.request.contextPath}')">
                <span class="glyphicon glyphicon-chevron-left" aria-hidden="true" >&nbsp;</span><spring:message code="label.event.back" />
            </button>
            <button type="button" class="btn btn-primary"
                onClick="javascript:processSubmit('${pageContext.request.contextPath}<%= SettlementNoteController.CALCULATE_INTEREST_URL %>')">
                 <spring:message code="label.continue" />&nbsp;<span class="glyphicon glyphicon-chevron-right" aria-hidden="true" >
            </button>
        </div>
    </div>
</form>

<script>
	$(document).ready(function() {
		// Put here the initializing code for page
	});
</script>
