<%@page import="org.fenixedu.treasury.ui.document.managepayments.SettlementNoteController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
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

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message
            code="label.document.managePayments.searchSettlementNote" />
        <small></small>
    </h1>
</div>
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span>
    &nbsp;
    <a class="" href="${pageContext.request.contextPath}<%= SettlementNoteController.TRANSACTIONS_SUMMARY_URL %>">
        <spring:message code="label.document.managePayments.event.transactions.summary" />
    </a>
    &nbsp;
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

<div class="panel panel-default">
    <form method="get" class="form-horizontal">
        <div class="panel-body">
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.SettlementNote.debtAccount" />
                </div>

                <div class="col-sm-10">
                    <%-- Relation to side 1 drop down rendered in input --%>
                    <select id="settlementNote_debtAccount"
                        class="select2 col-sm-10" name="debtaccount">
                        <option value="">&nbsp;</option>
                    </select>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.SettlementNote.documentNumber" />
                </div>

                <div class="col-sm-10">
                    <input id="settlementNote_documentNumber"
                        class="form-control" type="text"
                        name="documentnumber"
                        value='<c:out value='${not empty param.documentnumber ? param.documentnumber : settlementNote.documentNumber }'/>' />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-4 control-label">
                    <spring:message
                        code="label.SettlementNote.documentDate" />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.SettlementNote.documentDateFrom" />
                </div>

                <div class="col-sm-4">
                    <input id="settlementNote_documentDate"
                        class="form-control" type="text"
                        name="documentdatefrom" bennu-date
                        value='<c:out value='${param.documentdatefrom }'/>' />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.SettlementNote.documentDateTo" />
                </div>

                <div class="col-sm-4">
                    <input id="settlement_documentDate"
                        class="form-control" type="text"
                        name="documentdateto" bennu-date
                        value='<c:out value='${param.documentdateto }'/>' />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.SettlementNote.originDocumentNumber" />
                </div>

                <div class="col-sm-10">
                    <input id="settlementNote_originDocumentNumber"
                        class="form-control" type="text"
                        name="origindocumentnumber"
                        value='<c:out value='${not empty param.origindocumentnumber ? param.origindocumentnumber : settlementNote.originDocumentNumber }'/>' />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.SettlementNote.state" />
                </div>

                <div class="col-sm-4">
                    <select id="settlementNote_state" class="form-control"
                        name="state">
                        <option value="">&nbsp;</option>
                        <%-- empty option remove it if you don't want to have it or give it a label CHANGE_ME--%>
                        <c:forEach items="${stateValues}" var="field">
                            <option value='<c:out value='${field}'/>'><c:out
                                    value='${field.descriptionI18N.content}' /></option>
                        </c:forEach>
                    </select>
                    <script>
        $("#settlementNote_state").val('<c:out value='${not empty param.state ? param.state : settlementNote.state }'/>');
    </script>
                </div>
            </div>
        </div>
        <div class="panel-footer">
            <input type="submit" class="btn btn-default" role="button"
                value="<spring:message code="label.search" />" />
        </div>
    </form>
</div>

<c:set var="limit"><%=SettlementNoteController.SEARCH_SETTLEMENT_NOTE_LIST_LIMIT_SIZE%></c:set>
<c:if test="${listSize > limit}">
    <div class="alert alert-warning" role="alert">
        <p>
            <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
            <spring:message code="label.limitexceeded" arguments="${limit},${listSize}" />
        </p>
    </div>
</c:if>

<c:choose>
    <c:when test="${not empty searchsettlementnoteResultsDataSet}">
        <table id="searchsettlementnoteTable"
            class="table responsive table-bordered table-hover" width="100%">
            <thead>
                <tr>
                    <%--!!!  Field names here --%>
                    <th><spring:message
                            code="label.SettlementNote.documentDate" /></th>
                    <th><spring:message
                            code="label.SettlementNote.documentNumber" /></th>
                    <th><spring:message
                            code="label.SettlementNote.debtAccount" /></th>
                    <th><spring:message
                            code="label.SettlementNote.state" /></th>
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
	var searchsettlementnoteDataSet = [
			<c:forEach items="${searchsettlementnoteResultsDataSet}" var="searchResult">
				<%-- Field access / formatting  here CHANGE_ME --%>
				{
				"DT_RowId" : '<c:out value='${searchResult.externalId}'/>',
"debtaccount" : "<c:out value=' ${searchResult.debtAccount.customer.name}'/>",
"documentnumber" : "<c:out value='${searchResult.uiDocumentNumber}'/>",
"documentdate" : '<c:out value='${searchResult.documentDate.toString("YYYY-MM-dd HH:mm")}' />',
<c:if test = "${searchResult.isAnnulled()}">
"state" : "<span class=\"label label-danger\"><c:out value='${searchResult.state.descriptionI18N.content}' /></span>",
</c:if>
<c:if test = "${searchResult.isPreparing() }">
"state" : "<span class=\"label label-warning\"><c:out value='${searchResult.state.descriptionI18N.content}' /></span>",
</c:if>
<c:if test = "${searchResult.isClosed()}">
"state" : "<span class=\"label label-primary\"><c:out value='${searchResult.state.descriptionI18N.content}' /></span>",
</c:if>
"actions" :
" <a  class=\"btn btn-default btn-xs\" href=\"${pageContext.request.contextPath}/treasury/document/managepayments/settlementnote/search/view/${searchResult.externalId}\"><spring:message code='label.view'/></a>" +
                "" },
            </c:forEach>
    ];
	
$(document).ready(function() {

    
    $("#settlementNote_debtAccount").select2({
          ajax: {
            url: "${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/autocompletehelper",
            dataType: 'json',
            delay: 250,
            contentType: 'application/json;charset=UTF-8',
            data: function (params) {
              return {
                q: params.term, // search term
                page: params.page
              };
            },
            processResults: function (data, page) {
              return {
                results: data
              };
            },
            cache: true
          },
          escapeMarkup: function (markup) { return markup; }, 
          minimumInputLength: 3,
        });

	
	var table = $('#searchsettlementnoteTable').DataTable({
		language : {
			  url : "${datatablesI18NUrl}",			
	    },
	    "columns": [
    		{ data: 'documentdate' },
            { data: 'documentnumber' },
            { data: 'debtaccount' },
    		{ data: 'state' },
    		{ data: 'actions',className:"all" }		
    	],
    	//CHANGE_ME adjust the actions column width if needed
    	"columnDefs": [
            { "width": "54px", "targets": 3 } 
        ],
	    "data" : searchsettlementnoteDataSet,
    	//Documentation: https://datatables.net/reference/option/dom
        //"dom": '<"col-sm-6"l><"col-sm-3"f><"col-sm-3"T>rtip', //FilterBox = YES && ExportOptions = YES
        "dom": 'T<"clear">lrtip', //FilterBox = NO && ExportOptions = YES
        //"dom": '<"col-sm-6"l><"col-sm-6"f>rtip', //FilterBox = YES && ExportOptions = NO
        //"dom": '<"col-sm-6"l>rtip', // FilterBox = NO && ExportOptions = NO
        "tableTools": {
            "sSwfPath": "${pageContext.request.contextPath}/static/treasury/swf/copy_csv_xls_pdf.swf"
        }
	});
	table.columns.adjust().draw();	
	$('#searchsettlementnoteTable tbody').on( 'click', 'tr', function () {
	    $(this).toggleClass('selected');
	} );
		  
}); 
</script>

