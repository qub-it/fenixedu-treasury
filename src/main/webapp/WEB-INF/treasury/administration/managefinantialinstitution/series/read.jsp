<%@page import="org.fenixedu.treasury.ui.document.managepayments.PaymentReferenceCodeController"%>
<%@page import="org.fenixedu.treasury.domain.accesscontrol.TreasuryAccessControl"%>
<%@page import="org.fenixedu.treasury.domain.FinantialInstitution"%>
<%@page import="org.fenixedu.treasury.domain.document.Series"%>
<%@page import="org.fenixedu.treasury.ui.administration.managefinantialinstitution.DocumentNumberSeriesController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>

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
        <spring:message code="label.administration.manageFinantialInstitution.readSeries" />
        <small></small>
    </h1>
</div>



<div class="modal fade" id="createdebitnoteforpendingdebitentriesModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/series/read/${series.externalId}/createdebitnoteforpendingdebitentries" method="POST">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title">
                        <spring:message code="label.confirmation" />
                    </h4>
                </div>
                <div class="modal-body">
                    <p>
                        <spring:message code="label.administration.manageFinantialInstitution.readSeries.confirm.createdebitnoteforpendingdebitentries" arguments='${series.name.content }' />
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <spring:message code="label.cancel" />
                    </button>
                    <button id="deleteButton" class="btn btn-primary" type="submit">
                        <spring:message code="label.confirm" />
                    </button>
                </div>
            </form>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>



<div class="modal fade" id="deleteModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/series/delete/${series.externalId}" method="POST">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title">
                        <spring:message code="label.confirmation" />
                    </h4>
                </div>
                <div class="modal-body">
                    <p>
                        <spring:message code="label.administration.manageFinantialInstitution.readSeries.confirmDelete" arguments='${series.name.content }' />
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <spring:message code="label.close" />
                    </button>
                    <button id="deleteButton" class="btn btn-danger" type="submit">
                        <spring:message code="label.delete" />
                    </button>
                </div>
            </form>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->

<div class="modal fade" id="deleteDocumentNumberSeriesModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteDocumentNumberSeriesForm" action="#" method="POST">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title">
                        <spring:message code="label.confirmation" />
                    </h4>
                </div>
                <div class="modal-body">
                    <p>
                        <spring:message code="label.administration.document.manageDocumentNumberSeries.readDocumentNumberSeries.confirmDelete" />
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <spring:message code="label.close" />
                    </button>
                    <button id="deleteButton" class="btn btn-danger" type="submit">
                        <spring:message code="label.delete" />
                    </button>
                </div>
            </form>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->

<script type="text/javascript">
      function processDelete(externalId) {
        url = "${pageContext.request.contextPath}<%=DocumentNumberSeriesController.DELETE_URL%>
	"
				+ externalId;
		$("#deleteDocumentNumberSeriesForm").attr("action", url);
		$('#deleteDocumentNumberSeriesModal').modal('toggle')
	}
</script>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a class=""
        href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/finantialinstitution/read/${series.finantialInstitution.externalId}"><spring:message
            code="label.event.back" /></a> &nbsp;

    <%
        Series series = (Series) request.getAttribute("series");
    			FinantialInstitution finantialInstitution = series
    					.getFinantialInstitution();
    			if (TreasuryAccessControl.getInstance().isBackOfficeMember(
    					finantialInstitution)
    					 ) {
    %>


    |&nbsp; <span class="glyphicon glyphicon-trash" aria-hidden="true"></span>&nbsp;<a class="" href="#" data-toggle="modal" data-target="#deleteModal"><spring:message
            code="label.event.delete" /></a> &nbsp;|&nbsp; <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>&nbsp;<a class=""
        href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/series/update/${series.externalId}"><spring:message
            code="label.event.update" /></a> &nbsp;|&nbsp; <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a a class="" href="#" data-toggle="modal" data-target="#createdebitnoteforpendingdebitentriesModal"><spring:message
            code="label.event.administration.managefinantialinstitution.series.createdebitnoteforpendingdebitentries" /></a> &nbsp;
            |&nbsp; <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a class="" href="${pageContext.request.contextPath}<%=PaymentReferenceCodeController.CREATEPAYMENTCODEINSERIES_URL %>?series=${series.externalId}" ><spring:message
            code="label.event.administration.managefinantialinstitution.series.createpaymentreferencecode" /></a> &nbsp;
            
    <%
        }
    %>
</div>
<c:if test="${not empty infoMessages}">
    <div class="alert alert-info" role="alert">

        <c:forEach items="${infoMessages}" var="message">
            <p>${message}</p>
        </c:forEach>

    </div>
</c:if>
<c:if test="${not empty warningMessages}">
    <div class="alert alert-warning" role="alert">

        <c:forEach items="${warningMessages}" var="message">
            <p>${message}</p>
        </c:forEach>

    </div>
</c:if>
<c:if test="${not empty errorMessages}">
    <div class="alert alert-danger" role="alert">

        <c:forEach items="${errorMessages}" var="message">
            <p>${message}</p>
        </c:forEach>

    </div>
</c:if>

<div class="panel panel-primary">
    <div class="panel-heading">
        <h3 class="panel-title">
            <spring:message code="label.details" />
        </h3>
    </div>
    <div class="panel-body">
        <form method="post" class="form-horizontal">
            <table class="table">
                <tbody>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Series.finantialInstitution" /></th>
                        <td><c:out value='${series.finantialInstitution.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Series.code" /></th>
                        <td><c:out value='${series.code}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Series.name" /></th>
                        <td><c:out value='${series.name.content}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Series.active" /></th>
                        <td><c:if test="${series.active}">
                                <spring:message code="label.true" />
                            </c:if> <c:if test="${not series.active}">
                                <spring:message code="label.false" />
                            </c:if></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Series.externSeries" /></th>
                        <td><c:if test="${series.externSeries}">
                                <spring:message code="label.true" />
                            </c:if> <c:if test="${not series.externSeries}">
                                <spring:message code="label.false" />
                            </c:if></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Series.certificated" /></th>
                        <td><c:if test="${series.certificated}">
                                <spring:message code="label.true" />
                            </c:if> <c:if test="${not series.certificated}">
                                <spring:message code="label.false" />
                            </c:if></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Series.legacy" /></th>
                        <td><c:if test="${series.legacy}">
                                <spring:message code="label.true" />
                            </c:if> <c:if test="${not series.legacy}">
                                <spring:message code="label.false" />
                            </c:if></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Series.defaultSeries" /></th>
                        <td><c:if test="${series.defaultSeries}">
                                <spring:message code="label.true" />
                            </c:if> <c:if test="${not series.defaultSeries}">
                                <spring:message code="label.false" />
                            </c:if></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Series.selectable" /></th>
                        <td>
                        	<c:if test="${series.selectable}">
                                <spring:message code="label.true" />
                            </c:if>
                            <c:if test="${not series.selectable}">
                                <spring:message code="label.false" />
                            </c:if>
                        </td>
                    </tr>
                </tbody>
            </table>
        </form>
    </div>
</div>
<h3>
    <spring:message code="label.Series.DocumentNumberSeries"></spring:message>
</h3>

<%
    if (TreasuryAccessControl.getInstance().isBackOfficeMember(finantialInstitution)) {
%>

<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-plus-sign" aria-hidden="true"></span>&nbsp;<a class=""
        href="${pageContext.request.contextPath}<%=DocumentNumberSeriesController.CREATE_URL%>/series${series.externalId}"><spring:message code="label.event.create" /></a>
</div>
<%
    }
%>
<c:choose>
    <c:when test="${not empty series.documentNumberSeriesSet}">
        <table id="documentNumberSeriesTable" class="table responsive table-bordered table-hover" width="100%">
            <thead>
                <tr>
                    <%--!!!  Field names here --%>
                    <th><spring:message code="label.DocumentNumberSeries.finantialDocumentType" /></th>
                    <th><spring:message code="label.DocumentNumberSeries.sequenceNumber" /></th>
                    <%-- Operations Column --%>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${series.documentNumberSeriesSet}" var="documentNumberSeries">
                    <tr>
                        <td><c:out value="${documentNumberSeries.finantialDocumentType.name.content}" /></td>
                        <td><c:out value="${documentNumberSeries.preparingDocumentsCount}/${documentNumberSeries.closedDocumentsCount}" /></td>
                        <td>
                            <!--  ACTIONS --> <a class="btn btn-default btn-xs"
                            href="${pageContext.request.contextPath}<%=DocumentNumberSeriesController.READ_URL%>${documentNumberSeries.externalId}"> <spring:message
                                    code='label.view' />
                        </a> <%
     if (TreasuryAccessControl.getInstance()
 								.isBackOfficeMember(finantialInstitution)
 								|| TreasuryAccessControl.getInstance()
 										.isManager()) {
 %> <a class="btn btn-default btn-xs btn-warning" href="#" onClick="javascript:processDelete('${documentNumberSeries.externalId}')"> <spring:message code='label.delete' />
                        </a> <%
     }
 %>
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
	$(document)
			.ready(
					function() {

						var table = $('#documentNumberSeriesTable')
								.DataTable(
										{
											language : {
												url : "${datatablesI18NUrl}",
											},
											//CHANGE_ME adjust the actions column width if needed
											"columnDefs" : [
											//54
											{
												"width" : "120px",
												"targets" : 2
											} ],
											//Documentation: https://datatables.net/reference/option/dom
											//"dom": '<"col-sm-6"l><"col-sm-3"f><"col-sm-3"T>rtip', //FilterBox = YES && ExportOptions = YES
											//"dom": 'T<"clear">lrtip', //FilterBox = NO && ExportOptions = YES
											//"dom": '<"col-sm-6"l><"col-sm-6"f>rtip', //FilterBox = YES && ExportOptions = NO
											//"dom": '<"col-sm-6"l>rtip', // FilterBox = NO && ExportOptions = NO
											"tableTools" : {
												"sSwfPath" : "${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/swf/copy_csv_xls_pdf.swf"
											}
										});
						table.columns.adjust().draw();

						$('#documentNumberSeriesTable tbody').on('click', 'tr',
								function() {
									$(this).toggleClass('selected');
								});

					});
</script>

