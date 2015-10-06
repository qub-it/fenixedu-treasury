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

<link href="${pageContext.request.contextPath}/static/treasury/css/dataTables.responsive.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/static/treasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>						
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js" ></script>
<script src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<%-- TITLE --%>
<div class="page-header">
	<h1>
		<spring:message
			code="label.administration.base.managePaymentMethod.searchPaymentMethod" />
		<small></small>
	</h1>
</div>
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
	<span class="glyphicon glyphicon-plus-sign" aria-hidden="true"></span>&nbsp;<a
		class=""
		href="${pageContext.request.contextPath}/treasury/administration/base/managepaymentmethod/paymentmethod/create"><spring:message
			code="label.event.create" /></a> &nbsp;
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


<%--
<div class="panel panel-default">
<form method="get" class="form-horizontal">
<div class="panel-body">
<div class="form-group row">
<div class="col-sm-2 control-label"><spring:message code="label.PaymentMethod.code"/></div> 

<div class="col-sm-10">
	<input id="paymentMethod_code" class="form-control" type="text" name="code"  value='<c:out value='${not empty param.code ? param.code : paymentMethod.code }'/>' />
</div>	
</div>		
<div class="form-group row">
<div class="col-sm-2 control-label"><spring:message code="label.PaymentMethod.name"/></div> 

<div class="col-sm-10">
<input id="paymentMethod_name" class="form-control" type="text" name="name"  bennu-localized-string value='${not empty param.name ? param.name : "{}" } '/> 
</div>
</div>		
</div>
<div class="panel-footer">
	<input type="submit" class="btn btn-default" role="button" value="<spring:message code="label.search" />"/>
</div>
</form>
</div>
 --%>

<c:choose>
	<c:when test="${not empty searchpaymentmethodResultsDataSet}">
		<table id="searchpaymentmethodTable"
			class="table responsive table-bordered table-hover" width="100%">
			<thead>
				<tr>
					<%--!!!  Field names here --%>
					<th><spring:message code="label.PaymentMethod.code" /></th>
					<th><spring:message code="label.PaymentMethod.name" /></th>
					<th><spring:message code="label.PaymentMethod.availableForPaymentInApplication" /></th>
					<%-- Operations Column --%>
					<th></th>
				</tr>
			</thead>
			<tbody>

			</tbody>
		</table>
	</c:when>
	<c:otherwise>
		<div class="alert alert-warning" role="alert">
			<p> <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>			<spring:message code="label.noResultsFound" /></p>
		</div>

	</c:otherwise>
</c:choose>

<script>
	var searchpaymentmethodDataSet = [
			<c:forEach items="${searchpaymentmethodResultsDataSet}" var="searchResult">
				<%-- Field access / formatting  here CHANGE_ME --%>
				{
				"DT_RowId" : '<c:out value='${searchResult.externalId}'/>',
"code" : "<c:out value='${searchResult.code}'/>",
"name" : "<c:out value='${searchResult.name.content}'/>",
"availableForPaymentInApplication" : "<c:if test='${searchResult.availableForPaymentInApplication}'><spring:message code='label.true' /></c:if><c:if test='${!searchResult.availableForPaymentInApplication}'><spring:message code='label.false' /></c:if>",
"actions" :
" <a  class=\"btn btn-default btn-xs\" href=\"${pageContext.request.contextPath}/treasury/administration/base/managepaymentmethod/paymentmethod/search/view/${searchResult.externalId}\"><spring:message code='label.view'/></a>" +
                "" },
            </c:forEach>
    ];
	
	$(document).ready(function() {

	


		var table = $('#searchpaymentmethodTable').DataTable({language : {
			url : "${datatablesI18NUrl}",			
		},
		"columns": [
			{ data: 'code' },
			{ data: 'name' },
			{ data: 'availableForPaymentInApplication' },			
			{ data: 'actions',className:"all" }
			
		],
		"columnDefs": [
			       		//54
			       		//128
			       		               { "width": "54px", "targets": 2 } 
			       		             ],
		"data" : searchpaymentmethodDataSet,
		//Documentation: https://datatables.net/reference/option/dom
//"dom": '<"col-sm-6"l><"col-sm-3"f><"col-sm-3"T>rtip', //FilterBox = YES && ExportOptions = YES
// "dom": 'T<"clear">lrtip', //FilterBox = NO && ExportOptions = YES
"dom": '<"col-sm-6"l><"col-sm-6"f>rtip', //FilterBox = YES && ExportOptions = NO
//"dom": '<"col-sm-6"l>rtip', // FilterBox = NO && ExportOptions = NO
        "tableTools": {
            "sSwfPath": "${pageContext.request.contextPath}/static/treasury/swf/copy_csv_xls_pdf.swf"
        }
		});
		table.columns.adjust().draw();
		
		  $('#searchpaymentmethodTable tbody').on( 'click', 'tr', function () {
		        $(this).toggleClass('selected');
		    } );
		  
	}); 
</script>

