<%@page import="org.fenixedu.treasury.ui.document.managesettlementannulment.ManageSettlementAnnulmentEndDateController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="datatables" uri="http://github.com/dandelion/datatables"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js"/>
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css"/>

<link rel="stylesheet" href="${datatablesCssUrl}"/>
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json"/>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css"/>

${portal.angularToolkit()} 

<link href="${pageContext.request.contextPath}/static/treasury/css/dataTables.responsive.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/static/treasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>						
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js" ></script>
<script src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>

<script src="${pageContext.request.contextPath}/webjars/angular-sanitize/1.3.11/angular-sanitize.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.css" />
<script src="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.js"></script>

<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.ManageSettlementAnnulmentEndDateController.update" />
        <small></small>
    </h1>
</div>

<c:if test="${not empty infoMessages}">
	<div class="alert alert-info" role="alert">
		
		<c:forEach items="${infoMessages}" var="message"> 
			<p> <span class="glyphicon glyphicon glyphicon-ok-sign" aria-hidden="true">&nbsp;</span>
						${message}
					</p>
		</c:forEach>
		
	</div>	
</c:if>
<c:if test="${not empty warningMessages}">
	<div class="alert alert-warning" role="alert">
		
		<c:forEach items="${warningMessages}" var="message"> 
			<p> <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
						${message}
					</p>
		</c:forEach>
		
	</div>	
</c:if>
<c:if test="${not empty errorMessages}">
	<div class="alert alert-danger" role="alert">
		
		<c:forEach items="${errorMessages}" var="message"> 
			<p> <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
						${message}
					</p>
		</c:forEach>
		
	</div>	
</c:if>

<script>

angular.module('App', ['ngSanitize', 'ui.select','bennuToolkit']).controller('Controller', ['$scope', function($scope) {

	$scope.settlementAnnulmentEndDate = "${fiscalYear.settlementAnnulmentEndDate.toString('yyyy-MM-dd')}";

	$scope.submit = function() {
		$('#form').submit();
	}

}]);
</script>

<form id="backForm" method="get"
	action='${pageContext.request.contextPath}<%= ManageSettlementAnnulmentEndDateController.SEARCH_URL %>/${finantialInstitution.externalId}'>
</form>

<form id="form" name='form' method="post" class="form-horizontal" ng-app="App" ng-controller="Controller"
	action='${pageContext.request.contextPath}<%= ManageSettlementAnnulmentEndDateController.UPDATE_URL %>/${finantialInstitution.externalId}/${fiscalYear.externalId}'>

	<div class="panel panel-default">
		<div class="panel-body">
					
			<div class="form-group row">
				<div class="col-sm-3 control-label">
					<spring:message code="label.ManageSettlementAnnulmentEndDateController.settlementAnnulmentEndDate" />
				</div>

				<div class="col-sm-2">
					<input class="form-control" type="text" name="settlementAnnulmentEndDate" bennu-date="settlementAnnulmentEndDate" ng-required="true" />
				</div>
			</div>

		</div>
		
		<div class="panel-footer">

			<button type="submit" class="btn btn-primary" form="form" ng-click="">
				<span class="glyphicon glyphicon-ok" aria-hidden="true"></span>&nbsp;
				<spring:message code="label.submit" />
			</button>
			
			<button type="submit" class="btn btn-default" form="backForm">
				<spring:message code="label.cancel" />
			</button>

		</div>
		
	</div>
</form>

<script>
	$(document).ready(function() {});
</script>
