<%@page
    import="org.fenixedu.treasury.ui.managetreasuryexemption.TreasuryExemptionController"%>
<%@page
    import="org.fenixedu.treasury.ui.accounting.managecustomer.TreasuryEventController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="pf" uri="http://example.com/placeFunctions"%>

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
            code="label.manageTreasuryExemption.createTreasuryExemption" />
        <small></small>
    </h1>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>
    &nbsp;<a
        href="${pageContext.request.contextPath}<%= TreasuryEventController.READ_URL %>${debtAccount.externalId}/${treasuryEvent.externalId}">
        <spring:message code="label.event.back" />
    </a> &nbsp;
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

<script>
	angular
			.module('angularAppTreasuryExemption',
					[ 'ngSanitize', 'ui.select', 'bennuToolkit' ])
			.controller(
					'TreasuryExemptionController',
					[
							'$scope',
							function($scope) {

								$scope.object = angular
										.fromJson('${treasuryExemptionBeanJson}');
								$scope.postBack = createAngularPostbackFunction($scope);

								$scope.onChange = function(model) {
									$scope.postBack(model);
								};
								
								$scope.submitForm = function(model) {
									$scope.object.treasuryExemptionTypes = undefined;
									$scope.object.products = undefined;
								}

							} ]);
</script>

<form name ="form" method="post" class="form-horizontal"
    ng-app="angularAppTreasuryExemption"
    ng-controller="TreasuryExemptionController"
    ng-submit="submitForm($model)"
    action='${pageContext.request.contextPath}<%= TreasuryExemptionController.CREATE_URL %>${debtAccount.externalId}'>

    <input type="hidden" name="postback"
        value='${pageContext.request.contextPath}<%= TreasuryExemptionController.CREATEPOSTBACK_URL %>${debtAccount.externalId}' />

    <input name="bean" type="hidden" value="{{ object }}" />

    <div class="panel panel-default">
        <div class="panel-body">
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.TreasuryExemption.treasuryExemptionType" />
                </div>
                <div class="col-sm-10">
                    <ui-select
                        id="treasuryExemption_treasuryExemptionType"
                        ng-model="$parent.object.treasuryExemptionType"
                        theme="bootstrap" on-select="onChange($model)"
                        style="width:100%" ng-disabled="disabled">
                    <ui-select-match>{{$select.selected.text}}</ui-select-match>
                    <ui-select-choices
                        repeat="treasuryexemptiontype.id as treasuryexemptiontype in object.treasuryExemptionTypes| filter: $select.search">
                    <span
                        ng-bind-html="treasuryexemptiontype.text | highlight: $select.search"></span>
                    </ui-select-choices> </ui-select>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.TreasuryExemption.debitEntry" />
                </div>
                <div class="col-sm-10">
                    <ui-select id="treasuryExemption_debitEntry"
                        ng-model="$parent.object.debitEntry"
                        theme="bootstrap" on-select="onChange($model)"
                        style="width:100%" ng-disabled="disabled">
                    <ui-select-match>{{$select.selected.text}}</ui-select-match>
                    <ui-select-choices
                        repeat="debitEntry.id as debitEntry in object.debitEntries| filter: $select.search">
                    <span
                        ng-bind-html="debitEntry.text | highlight: $select.search"></span>
                    </ui-select-choices> </ui-select>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.TreasuryExemption.valueToExempt" />
                </div>
                <div class="input-group col-sm-4" style="padding-right: 15px; padding-left: 15px;">
                    <div class=" input-group-addon">
                        <c:out
                            value="${treasuryEvent.debtAccount.finantialInstitution.currency.symbol}" />
                    </div>
                    <input id="treasuryExemption_valueToExempt"
                        class="form-control"
                        ng-model="object.valuetoexempt" type="text" name="valuetoexempt"
                        ng-pattern="/^(0*\.(0[1-9]|[1-9][0-9]?)|[1-9][0-9]*(\.[0-9]{1,2})?)$/"
                        value='<c:out value='${ param.valuetoexempt }'/>' />
                </div>
            </div>
            <div class="form-group row" ng-show="form.valuetoexempt.$error.pattern">
                <div class="col-sm-2 control-label">
                </div>
                <div class="col-sm-10">
                <p class="alert alert-danger" >
                    <spring:message
                        code="error.invalid.format.input" />
                </p>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.TreasuryExemption.reason" />
                </div>

                <div class="col-sm-10">
                    <input id="treasuryExemption_reason"
                        class="form-control" ng-model="object.reason"
                        type="text"
                        value='<c:out value='${param.reason}'/>' />
                </div>
            </div>
        </div>
        <div class="panel-footer">
            <input type="submit" class="btn btn-default" role="button"
                value="<spring:message code="label.submit" />" />
        </div>
    </div>
</form>

<script>
$(document).ready(function() {
});
</script>
