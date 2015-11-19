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
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

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



<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message
            code="label.viewCustomerTreasuryEvents.searchTreasuryEvents" />
        <small></small>
    </h1>
</div>
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a
        class=""
        href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${debtAccount.externalId}"><spring:message
            code="label.event.back" /></a> &nbsp;
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


<script type="text/javascript">
	  function processDelete(externalId) {
	    url = "${pageContext.request.contextPath}/treasury/viewcustomertreasuryevents/treasuryevent/searchtreasuryeventssearchTreasuryEvents/delete/" + externalId;
	    $("#deleteForm").attr("action", url);
	    $('#deleteModal').modal('toggle')
	  }
</script>


<!-- <div class="modal fade" id="deleteModal"> -->
<!--   <div class="modal-dialog"> -->
<!--     <div class="modal-content"> -->
<!--     <form id ="deleteForm" action="#" method="POST"> -->
<!--       <div class="modal-header"> -->
<!--         <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button> -->
<%--         <h4 class="modal-title"><spring:message code="label.confirmation"/></h4> --%>
<!--       </div> -->
<!--       <div class="modal-body"> -->
<%--         <p><spring:message code = "label.viewCustomerTreasuryEvents.searchTreasuryEvents.confirmDelete"/></p> --%>
<!--       </div> -->
<!--       <div class="modal-footer"> -->
<%--         <button type="button" class="btn btn-default" data-dismiss="modal"><spring:message code = "label.close"/></button> --%>
<%--         <button id="deleteButton" class ="btn btn-danger" type="submit"> <spring:message code = "label.delete"/></button> --%>
<!--       </div> -->
<!--       </form> -->
<!--     </div>/.modal-content -->
<!--   </div>/.modal-dialog -->
<!-- </div>/.modal -->



<c:choose>
    <c:when test="${not empty searchtreasuryeventsResultsDataSet}">
        <table id="searchtreasuryeventsTable"
            class="table responsive table-bordered table-hover" width="100%">
            <thead>
                <tr>
                    <%--!!!  Field names here --%>
                    <th><spring:message
                            code="label.TreasuryEvent.description" /></th>
                    <th><spring:message
                            code="label.TreasuryEvent.treasuryEventDate" /></th>
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

            <p>
                <span class="glyphicon glyphicon-exclamation-sign"
                    aria-hidden="true">&nbsp;</span>
                <spring:message code="label.noResultsFound" />
            </p>

        </div>

    </c:otherwise>
</c:choose>

<script>
	var searchtreasuryeventsDataSet = [
			<c:forEach items="${searchtreasuryeventsResultsDataSet}" var="searchResult">
				<%-- Field access / formatting  here CHANGE_ME --%>
				{
				"DT_RowId" : '<c:out value='${searchResult.externalId}'/>',
"description" : "<c:out value='${searchResult.description.content}'/>",
"treasuryEventDate" : "<c:out value='${searchResult.treasuryEventDate.toString()}'/>", 
"actions" :
" <a  class=\"btn btn-default btn-xs\" href=\"${pageContext.request.contextPath}/treasury/accounting/managecustomer/treasuryevent/search/view/${searchResult.externalId}\"><spring:message code='label.view'/></a>" +
				"" 
			},
            </c:forEach>
    ];
	
	$(document).ready(function() {

	


		var table = $('#searchtreasuryeventsTable').DataTable({language : {
			url : "${datatablesI18NUrl}",			
		},
		"columns": [
			{ data: 'description' },
			{ data: 'treasuryEventDate' },
			{ data: 'actions',className:"all" }
			
		],
		//CHANGE_ME adjust the actions column width if needed
		"columnDefs": [
		//54
		               { "width": "54px", "targets": 1 } 
		             ],
		"data" : searchtreasuryeventsDataSet,
		//Documentation: https://datatables.net/reference/option/dom
"dom": '<"col-sm-6"l><"col-sm-3"f><"col-sm-3"T>rtip', //FilterBox = YES && ExportOptions = YES
//"dom": 'T<"clear">lrtip', //FilterBox = NO && ExportOptions = YES
//"dom": '<"col-sm-6"l><"col-sm-6"f>rtip', //FilterBox = YES && ExportOptions = NO
//"dom": '<"col-sm-6"l>rtip', // FilterBox = NO && ExportOptions = NO
        "tableTools": {
            "sSwfPath": "${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/swf/copy_csv_xls_pdf.swf"        	
        }
		});
		table.columns.adjust().draw();
		
		  $('#searchtreasuryeventsTable tbody').on( 'click', 'tr', function () {
		        $(this).toggleClass('selected');
		    } );
		  
	}); 
</script>

