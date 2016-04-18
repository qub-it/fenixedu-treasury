<%@page import="org.fenixedu.treasury.ui.document.forwardpayments.ManageForwardPaymentsController"%>
<%@page import="org.fenixedu.treasury.domain.accesscontrol.TreasuryAccessControl"%>
<%@page import="org.fenixedu.treasury.ui.accounting.managecustomer.AdhocCustomerController"%>
<%@page import="org.fenixedu.treasury.ui.accounting.managecustomer.CustomerController"%>
<%@page import="org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController"%>
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
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.ManageForwardPayments.search" />
        <small></small>
    </h1>
</div>
<%-- NAVIGATION --%>
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

<div class="panel panel-default">
    <form method="get" class="form-horizontal">
        <div class="panel-body">

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ManageForwardPayments.beginDate" />
                </div>

                <div class="col-sm-4">
                    <input id="beginDate" class="form-control" type="text"
                        name="beginDate" bennu-date value='<c:out value='${param.beginDate}'/>' />
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ManageForwardPayments.endDate" />
                </div>

                <div class="col-sm-4">
                    <input id="endDate" class="form-control" type="text"
                        name="endDate" bennu-date value='<c:out value='${param.endDate}'/>' />
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ManageForwardPayments.customerName" />
                </div>

                <div class="col-sm-10">
                    <input id="customerName" class="form-control" type="text" name="customerName" 
                    	value='<c:out value='${param.customerName}'/>' />
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ManageForwardPayments.businessCustomerId" />
                </div>

                <div class="col-sm-10">
                    <input id="businessCustomerId" class="form-control" type="text" name="businessCustomerId" 
                    	value='<c:out value='${param.businessCustomerId}'/>' />
                </div>
            </div>

        </div>
        <div class="panel-footer">
            <input type="submit" class="btn btn-default" role="button" 
            	value="<spring:message code="label.search" />" />
        </div>
    </form>
</div>

<c:choose>
    <c:when test="${not empty forwardPayments}">
        <table id="tableId" class="display table  table-bordered table-hover responsive" width="100%">
            <thead>
                <tr>
                    <%--!!!  Field names here --%>
                    <th><spring:message code="label.ForwardPayment.whenOccured" /></th>
                    <th><spring:message code="label.ForwardPayment.orderNumber" /></th>
                    <th><spring:message code="label.ForwardPayment.customerName" /></th>
                    <th><spring:message code="label.ForwardPayment.amount" /></th>
                    <th><spring:message code="label.ForwardPayment.currentState" /></th>
                    <th><spring:message code="label.ForwardPayment.transactionId" /></th>
                    <%-- Operations Column --%>
                    <th></th>
                </tr>
            </thead>
            <tbody>
				<c:forEach var="f" items="${forwardPayments}">
					<tr>
						<td><c:out value='${f.whenOccured.toString("yyyy-MM-dd")}' /></td>
						<td><c:out value="${f.orderNumber}" /></td>
						<td><c:out value="${f.debtAccount.customer.name} [${f.debtAccount.customer.businessIdentification}]" /></td>
						<td><c:out value="${f.debtAccount.finantialInstitution.currency.getValueFor(f.amount)}" /></td>
						<td><c:out value="${f.currentState.localizedName.content}" /></td>
						<td><c:out value="${f.transactionId}" /></td>
						<td>
							<a href="${pageContext.request.contextPath}<%= ManageForwardPaymentsController.VIEW_URL %>/${f.externalId}" class="btn btn-default">
								<spring:message code="label.view" />
							</a>
						</td>
					</tr>
				</c:forEach>
            </tbody>
        </table>
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

<script>

	$(document).ready(function() {
		
		var table = $('#tableId').DataTable({language : {
			url : "${datatablesI18NUrl}",	
			responsive: true
		},
		
		"columns": [
			{ data: 'whenOccured' },
			{ data: 'orderNumber' },
			{ data: 'customerName' },
			{ data: 'amount' },
			{ data: 'currentState' },
			{ data: 'transactionId' },
			{ data: 'actions',className:"all" }
			
		],
		"order": [[ 1, "desc" ], [2, "asc" ]],
		//CHANGE_ME adjust the actions column width if needed
		"columnDefs": [
			{ "width": "54px", "targets": 5 } 
		],
		"dom": '<"col-sm-6"l><"col-sm-3"f><"col-sm-3"T>rtip', //FilterBox = YES && ExportOptions = YES
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

