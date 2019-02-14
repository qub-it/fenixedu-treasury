<%@page import="org.fenixedu.treasury.ui.document.managesettlementannulment.ManageSettlementAnnulmentLimitDateController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>

<script type="text/javascript" src="https://cdn.datatables.net/1.10.12/js/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/select/1.2.0/js/dataTables.select.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/buttons/1.2.2/js/dataTables.buttons.min.js"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jszip/2.5.0/jszip.min.js"></script>
<script type="text/javascript" src="https://cdn.rawgit.com/bpampuch/pdfmake/0.1.18/build/pdfmake.min.js"></script>
<script type="text/javascript" src="https://cdn.rawgit.com/bpampuch/pdfmake/0.1.18/build/vfs_fonts.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/buttons/1.2.2/js/buttons.html5.min.js"></script>

<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/buttons/1.2.2/css/buttons.dataTables.min.css" />


<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css" />

<link rel="stylesheet" href="${datatablesCssUrl}" />
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css" />

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
${portal.angularToolkit()}
<%-- ${portal.toolkit()} --%>

<link href="${pageContext.request.contextPath}/static/fenixedu-ulisboa-specifications/css/dataTables.responsive.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/static/fenixedu-ulisboa-specifications/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<script src="${pageContext.request.contextPath}/static/fenixedu-ulisboa-specifications/js/omnis.js"></script>

<script src="${pageContext.request.contextPath}/webjars/angular-sanitize/1.3.11/angular-sanitize.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.css" />
<script src="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.js"></script>



<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.ManageSettlementAnnulmentLimitDateController.search" />
        <small></small>
    </h1>
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

<%-- Choose Finantial Institution --%>
<div ng-app="App" ng-controller="Controller" style="margin-bottom: 20px" class="row">
    <div class="col-xs-1">
        <strong><spring:message code="label.ManageSettlementAnnulmentLimitDateController.finantialInstitution" /></strong>
    </div>
    <div class="col-xs-4">
        <select id="finantialInstitutionOptions" class="js-example-basic-single form-control" name="finantialInstitutionId"
            ng-change="change(finantialInstitutionId, '{{ finantialInstitutionId }}')" ng-model="finantialInstitutionId">
            <option value=""></option>
            <c:forEach items="${finantialInstitutionOptions}" var="e">
                <option value="${e.externalId}">${e.fiscalNumber} - ${e.name}</option>
            </c:forEach>
        </select>
    </div>
</div>

<script type="text/javascript">

angular.module('App', ['bennuToolkit']).controller('Controller', ['$scope', function($scope) {
	var postbackUrl = "${pageContext.request.contextPath}<%= ManageSettlementAnnulmentLimitDateController.SEARCH_URL %>";
	
	$scope.finantialInstitutionId = ${finantialInstitution.externalId};
	$scope.change = function(newValue, oldValue) {

		if(oldValue !== "" && newValue !== oldValue) {
			document.location.href=postbackUrl + "/" + newValue;
		}
	};
}]);

</script>

<c:choose>
    <c:when test="${not empty result}">
        <table id="tableId" class="display table  table-bordered table-hover responsive" width="100%">
            <thead>
                <tr>
                    <%--!!!  Field names here --%>
                    <th><spring:message code="label.ManageSettlementAnnulmentLimitDateController.year" /></th>
                    <th><spring:message code="label.ManageSettlementAnnulmentLimitDateController.settlementAnnulmentLimitDate" /></th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
				<c:forEach var="r" items="${result}">
					<tr>
						<td><c:out value='${r.year}' /></td>
						<td><c:out value='${r.settlementAnnulmentLimitDate.toString("yyyy-MM-dd")}' /></td>	
						<td>
							<a class="btn btn-default" href="${pageContext.request.contextPath}<%= ManageSettlementAnnulmentLimitDateController.UPDATE_URL %>/${finantialInstitution.externalId}/${r.externalId}">
								<span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>&nbsp;
								<spring:message code="label.update" />
							</a>
						</td>
					</tr>
				</c:forEach>
            </tbody>
        </table>
    </c:when>
    <c:otherwise>
        <div class="alert alert-warning" role="alert">
            <p><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
            	<spring:message code="label.noResultsFound" />
            </p>
        </div>
    </c:otherwise>
</c:choose>

<script>

	$(document).ready(function() {
		
		var table = $('#tableId').DataTable({language : {
			url : "${datatablesI18NUrl}",	
			responsive: true
		},
		
		"order": [[ 0, "desc" ]],
		//CHANGE_ME adjust the actions column width if needed
		"columnDefs": [
		],
        dom: '<"col-sm-5"l><"col-sm-3"f><"col-sm-3"B>rtip', //FilterBox = YES && ExportOptions = YES
        buttons: [
            'copyHtml5',
            'excelHtml5',
            'csvHtml5',
            'pdfHtml5'
        ],
		"tableTools": {
            "sSwfPath": "${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/swf/copy_csv_xls_pdf.swf"
        }
		});
		table.columns.adjust().draw();
		
		$('#searchcustomerTable tbody').on('click', 'tr', function () {
			$(this).toggleClass('selected');
		});
	}); 
	
</script>
