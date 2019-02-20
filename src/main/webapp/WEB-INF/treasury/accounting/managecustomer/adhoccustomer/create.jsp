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
${portal.angularToolkit()}
<%-- ${portal.toolkit()}--%>

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
		<spring:message code="label.accounting.manageCustomer.createAdhocCustomer" />
		<small></small>
	</h1>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
	<span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a class=""
		href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/customer/"><spring:message code="label.event.back" /></a>
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
	angular.module('angularAppAdhocCustomer', [ 'ngSanitize', 'ui.select' ,'bennuToolkit']).controller('AdhocCustomerController', [ '$scope', function($scope) {

		$scope.object = ${adhocCustomerBeanJson};
		$scope.postBack = createAngularPostbackFunction($scope);

		//Begin here of Custom Screen business JS - code
		$scope.onCountryChange = function(country, model) {
			$scope.postBack(model);
		}
	}]);
</script>

<form name='form' method="post" class="form-horizontal" ng-app="angularAppAdhocCustomer" ng-controller="AdhocCustomerController"
	action='${pageContext.request.contextPath}/treasury/accounting/managecustomer/adhoccustomer/create'>

	<input type="hidden" name="postback" value='${pageContext.request.contextPath}/treasury/accounting/managecustomer/adhoccustomer/createpostback' />
	<input name="bean" type="hidden" value="{{ object }}" />

	<div class="panel panel-default">
		<div class="panel-body">

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.DebtAccount.finantialInstitution" />
				</div>
				<div class="col-sm-4">
					<ui-select id="adhocCustomer_finantialInstitutions" ng-model="$parent.object.finantialInstitutions" theme="bootstrap" ng-disabled="disabled" multiple ng-required="true">
					<ui-select-match>{{$item.text}}</ui-select-match>
					<ui-select-choices repeat="institution.id as institution in object.finantialInstitutionsDataSource| filter: $select.search"> <span
						ng-bind-html="institution.text | highlight: $select.search"></span> </ui-select-choices> </ui-select>

				</div>
			</div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.Customer.customerType" />
                </div>
                <div class="col-sm-4">
                    <ui-select id="adhocCustomer_customerType"
                        ng-model="$parent.object.customerType"
                        theme="bootstrap" ng-required="true">
                        <ui-select-match allow-clear="true">{{$select.selected.text}}</ui-select-match>
	                    <ui-select-choices repeat="customerType.id as customerType in object.customerTypesDataSource| filter: $select.search">
	                    	<span ng-bind-html="customerType.text | highlight: $select.search"></span>
                    	</ui-select-choices>
                    </ui-select>

                </div>
            </div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.AdhocCustomer.name" />
				</div>

				<div class="col-sm-10">
					<input id="adhocCustomer_name" class="form-control" type="text" ng-model="object.name" name="name"
						value='<c:out value='${not empty param.name ? param.name : adhocCustomer.name }'/>' ng-required="true" />
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.AdhocCustomer.identificationNumber" />
				</div>

				<div class="col-sm-10">
					<input id="adhocCustomer_identificationNumber" class="form-control" type="text" ng-model="object.identificationNumber" name="identificationnumber"
						value='<c:out value='${not empty param.identificationnumber ? param.identificationnumber : adhocCustomer.identificationNumber }'/>' />
				</div>
			</div>
			
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.AdhocCustomer.fiscalNumber" />
				</div>

				<div class="col-sm-10">
					<input id="adhocCustomer_fiscalNumber" class="form-control" type="text" ng-model="object.fiscalNumber" name="fiscalnumber"
						value='<c:out value='${not empty param.fiscalnumber ? param.fiscalnumber : adhocCustomer.fiscalNumber }'/>' ng-required="true" />
				</div>
			</div>
			
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.AdhocCustomer.addressCountryCode" />
                </div>

                <div class="col-sm-10">
                    <ui-select id="adhocCustomer_addressCountryCode" name="addressCountryCode" ng-model="$parent.object.addressCountryCode" 
                    	theme="bootstrap" ng-required="true" ng-change="onCountryChange($addressCountryCode, $model);"> 
                        <ui-select-match allow-clear="true">{{$select.selected.text}}</ui-select-match>
                        <ui-select-choices repeat="c.id as c in object.countryCodesDataSource| filter: $select.search">
		                    <span ng-bind-html="c.text | highlight: $select.search"></span>
                    	</ui-select-choices>
                    </ui-select>
                </div>
            </div>
            
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.AdhocCustomer.address" />
                </div>

                <div class="col-sm-10">
	                 <input id="adhocCustomer_address" class="form-control" type="text" ng-model="object.address" name="fiscalnumber"
	                        value='<c:out value='${not empty param.address ? param.address : adhocCustomer.address }'/>' ng-required="true" />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.AdhocCustomer.districtSubdivision" />
                </div>

                <div class="col-sm-10">
	                  <input id="adhocCustomer_districtSubdivision" class="form-control" type="text" ng-model="object.districtSubdivision" name="districtsubdivision"
	                      value='<c:out value='${not empty param.districtsubdivision ? param.districtsubdivision : adhocCustomer.districtSubdivision}'/>' ng-required="true" />
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.AdhocCustomer.zipCode" />
                </div>

                <div class="col-sm-10">
	                <input id="adhocCustomer_zipCode" class="form-control" type="text" ng-model="object.zipCode" name="zipcode"
		                value='<c:out value='${not empty param.zipcode ? param.zipcode : adhocCustomer.zipCode }'/>' 
		                ng-required="object.addressCountryDefault" />
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
