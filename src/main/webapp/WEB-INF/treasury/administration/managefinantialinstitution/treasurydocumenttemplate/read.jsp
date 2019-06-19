<%@page import="org.fenixedu.treasury.ui.administration.managefinantialinstitution.TreasuryDocumentTemplateController"%>
<%@page import="org.fenixedu.treasury.domain.document.TreasuryDocumentTemplateFile"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>

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
    href="//cdn.datatables.net/responsive/1.0.4/css/dataTables.responsive.css"
    rel="stylesheet" />
<script
    src="//cdn.datatables.net/responsive/1.0.4/js/dataTables.responsive.js"></script>
<link
    href="//cdn.datatables.net/tabletools/2.2.3/css/dataTables.tableTools.css"
    rel="stylesheet" />
<script
    src="//cdn.datatables.net/tabletools/2.2.3/js/dataTables.tableTools.min.js"></script>
<link
    href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0-rc.1/css/select2.min.css"
    rel="stylesheet" />
<script
    src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0-rc.1/js/select2.min.js"></script>
<script
    src="${pageContext.request.contextPath}/static/treasury/js/bootbox.min.js"></script>
<script
    src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<div class="modal fade" id="uploadModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="uploadForm" action="${pageContext.request.contextPath}<%= TreasuryDocumentTemplateController.SEARCH_UPLOAD_URL %>${documentTemplate.externalId}" method="POST" enctype="multipart/form-data">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title">
                        <spring:message code="label.DocumentTemplateFile.upload" />
                    </h4>
                </div>
                <div class="modal-body">
                    <input type="file" name="documentTemplateFile" accept="<%=TreasuryDocumentTemplateFile.CONTENT_TYPE%>" />
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <spring:message code="label.cancel" />
                    </button>
                    <button id="uploadButton" class="btn btn-primary" type="submit">
                        <spring:message code="label.upload" />
                    </button>
                </div>
            </form>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->

<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message
            code="label.administration.manageFinantialInstitution.readDocumentTemplate" />
        <small></small>
    </h1>
</div>
<div class="modal fade" id="deleteModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm"
                action="${pageContext.request.contextPath}<%= TreasuryDocumentTemplateController.DELETE_URL %>${documentTemplate.externalId}"
                method="POST">
                <div class="modal-header">
                    <button type="button" class="close"
                        data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title">
                        <spring:message code="label.confirmation" />
                    </h4>
                </div>
                <div class="modal-body">
                    <p>
                        <spring:message
                            code="label.administration.manageFinantialInstitution.readDocumentTemplate.confirmDelete" />
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default"
                        data-dismiss="modal">
                        <spring:message code="label.close" />
                    </button>
                    <button id="deleteButton" class="btn btn-danger"
                        type="submit">
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
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a
        class=""
        href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/finantialinstitution/read/${documentTemplate.finantialEntity.finantialInstitution.externalId}"><spring:message
            code="label.event.back" /></a> &nbsp;|&nbsp; <span
        class="glyphicon glyphicon-trash" aria-hidden="true"></span>&nbsp;<a
        class="" href="#" data-toggle="modal" data-target="#deleteModal"><spring:message
            code="label.event.delete" /></a> &nbsp;|&nbsp; <span
        class="glyphicon glyphicon-upload" aria-hidden="true"></span>&nbsp;<a
        class="" href="#" data-toggle="modal" data-target="#uploadModal"><spring:message
            code="label.event.upload" /></a> &nbsp;
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
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.DocumentTemplate.finantialDocumentTypes" /></th>
                        <td><c:out
                                value='${documentTemplate.finantialDocumentType.type.descriptionI18N.content}' />
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.DocumentTemplate.finantialEntity" /></th>
                        <td><c:out
                                value='${documentTemplate.finantialEntity.name.content}' />
                        </td>
                    </tr>
                </tbody>
            </table>
        </form>
    </div>
</div>

<c:choose>
    <c:when
        test="${ not empty documentTemplate.treasuryDocumentTemplateFilesSet }">
        <div class="panel panel-primary">
            <div class="panel-heading">
                <h3 class="panel-title">
                    <spring:message
                        code="label.DocumentTemplateFile.history" />
                </h3>
            </div>
            <div class="panel-body">
                <form method="post" class="form-horizontal">
                    <table class="table">
                        <tbody>
                            <tr>
                                <th><spring:message
                                        code="label.DocumentTemplateFile.date" /></th>
                                <th><spring:message
                                        code="label.DocumentTemplateFile.name" /></th>
                            </tr>
                            <c:forEach
                                items="${ documentTemplate.treasuryDocumentTemplateFilesSet }"
                                var="submittedFile">
                                <tr>
                                    <td><joda:format value='${submittedFile.creationDate}' style='S-' /></td>
                                    <td><c:out
                                            value='${submittedFile.getFilename()}' />
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </form>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <div class="alert alert-info" role="alert">
            <spring:message
                code="label.documentTemplateFile.noResultsFound" />
        </div>
    </c:otherwise>
</c:choose>

<script>
	$(document).ready(function() {

	});
</script>
