
<%@page import="org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory"%>
<%@page import="org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI"%>
<%@page import="org.fenixedu.treasury.ui.administration.forwardpayments.ManageForwardPaymentConfigurationController"%>
<%@page import="org.fenixedu.treasury.domain.forwardpayments.ForwardPaymentConfiguration"%>
<%@page import="org.fenixedu.treasury.domain.FinantialInstitution"%>
<%@page import="org.fenixedu.treasury.ui.administration.managefinantialinstitution.TreasuryDocumentTemplateController"%>
<%@page import="org.fenixedu.treasury.domain.document.TreasuryDocumentTemplateFile"%>
<%@page import="org.fenixedu.treasury.domain.document.FinantialDocumentTypeEnum"%>
<%@page import="org.fenixedu.treasury.domain.document.FinantialDocumentType"%>
<%@page import="org.fenixedu.treasury.ui.document.manageinvoice.DebitNoteController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="pf" uri="http://example.com/placeFunctions"%>
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
        <spring:message code="label.administration.manageFinantialInstitution.readFinantialInstitution" />
        <small></small>
    </h1>
</div>

<script type="text/javascript">
      function processUpload(externalId) {
        url = "${pageContext.request.contextPath}<%=TreasuryDocumentTemplateController.SEARCH_UPLOAD_URL%>" + externalId;
        $("#uploadForm").attr("action", url);
        $('#uploadModal').modal('toggle')
      }
</script>

<div class="modal fade" id="uploadModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="uploadForm" action="#" method="POST" enctype="multipart/form-data">
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

<div class="modal fade" id="deleteModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm"
                action="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/finantialinstitution/delete/${finantialInstitution.externalId}"
                method="POST">
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
                        <spring:message code="label.administration.manageFinantialInstitution.readFinantialInstitution.confirmDelete" arguments='${finantialInstitution.name }' />
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
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a class=""
        href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/finantialinstitution/"><spring:message code="label.event.back" /></a> &nbsp;
    <%
        FinantialInstitution finantialInstitution = (FinantialInstitution) request.getAttribute("finantialInstitution");
    	if (TreasuryAccessControlAPI.isBackOfficeMember(TreasuryPlataformDependentServicesFactory.implementation().getLoggedUsername(), finantialInstitution)) {
    %>
    |&nbsp; <span class="glyphicon glyphicon-trash" aria-hidden="true"></span>&nbsp;<a class="" href="#" data-toggle="modal" data-target="#deleteModal"><spring:message
            code="label.event.delete" /></a> &nbsp;| &nbsp; <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>&nbsp;<a class=""
        href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/finantialinstitution/update/${finantialInstitution.externalId}"><spring:message
            code="label.event.update" /></a> 
            
            |&nbsp; 
            <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;
            <a class="" href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/finantialinstitution/read/${finantialInstitution.externalId}/sibsconfigurationupdate">
        		<spring:message code="label.event.administration.managefinantialinstitution.finantialinstitution.sibsConfigurationUpdate" />
    		</a>
    		|&nbsp;
            <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;
            <a href="${pageContext.request.contextPath}<%= ManageForwardPaymentConfigurationController.VIEW_URL %>/${finantialInstitution.externalId}"
            	class="" target="_blank">
        		<spring:message code="label.event.administration.managefinantialinstitution.finantialinstitution.forwardPaymentConfiguration" />
    		</a>&nbsp;


    <div class="btn-group">
        <button class="btn btn-default dropdown-toggle" type="button" id="dropdownMenu1" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
            <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;
            <spring:message code="label.event.administration.managefinantialinstitution.finantialinstitution.erpoptions">
            </spring:message>
            <span class="caret"></span>
        </button>
        <ul class="dropdown-menu" aria-labelledby="dropdownMenu1">
            <li><a class=""
                href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/finantialinstitution/read/${finantialInstitution.externalId}/exportproductsintegrationfile">
                    <span class="glyphicon glyphicon-export" aria-hidden="true"></span>&nbsp; <spring:message
                        code="label.event.administration.managefinantialinstitution.finantialinstitution.exportProductERP" />
            </a></li>
            <li><a class=""
                href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/finantialinstitution/read/${finantialInstitution.externalId}/exportproductsintegrationonline">
                    <span class="glyphicon glyphicon-export" aria-hidden="true"></span>&nbsp; <spring:message
                        code="label.event.administration.managefinantialinstitution.finantialinstitution.exportProductERPOnline" />
            </a></li>
            <li><a class=""
                href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/finantialinstitution/read/${finantialInstitution.externalId}/exportcustomersintegrationfile">
                    <span class="glyphicon glyphicon-export" aria-hidden="true"></span>&nbsp; <spring:message
                        code="label.event.administration.managefinantialinstitution.finantialinstitution.exportCustomersERP" />
            </a></li>
            <li><a class=""
                href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/finantialinstitution/read/${finantialInstitution.externalId}/exportcustomersintegrationonline">
                    <span class="glyphicon glyphicon-export" aria-hidden="true"></span>&nbsp; <spring:message
                        code="label.event.administration.managefinantialinstitution.finantialinstitution.exportCustomersERPOnline" />
            </a></li>
            <li><a class=""
                href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/finantialinstitution/read/${finantialInstitution.externalId}/erpconfigurationupdate">
                    <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<spring:message
                        code="label.event.administration.managefinantialinstitution.finantialinstitution.erpConfigurationUpdate" />
            </a></li>
        </ul>
    </div>
    <%
        }
    %>

</div>
<c:if test="${not empty infoMessages}">
    <div class="alert alert-info" role="alert">

        <c:forEach items="${infoMessages}" var="message">
        
            <p><span class="glyphicon glyphicon-ok-sign"
                    aria-hidden="true">&nbsp;</span> ${message}</p>
        </c:forEach>

    </div>
</c:if>
<c:if test="${not empty warningMessages}">
    <div class="alert alert-warning" role="alert">

        <c:forEach items="${warningMessages}" var="message">
            <p><span class="glyphicon glyphicon-exclamation-sign"
                    aria-hidden="true">&nbsp;</span> ${message}</p>
        </c:forEach>

    </div>
</c:if>
<c:if test="${not empty errorMessages}">
    <div class="alert alert-danger" role="alert">

        <c:forEach items="${errorMessages}" var="message">
            <p><span class="glyphicon glyphicon-exclamation-sign"
                    aria-hidden="true">&nbsp;</span> ${message}</p>
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
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialInstitution.fiscalCountryRegion" /></th>
                        <td><c:out value='${finantialInstitution.fiscalCountryRegion.name.content}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialInstitution.currency" /></th>
                        <td><c:out value='${finantialInstitution.currency.isoCode}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialInstitution.code" /></th>
                        <td><c:out value='${finantialInstitution.code}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialInstitution.fiscalNumber" /></th>
                        <td><c:out value='${finantialInstitution.fiscalNumber}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialInstitution.companyId" /></th>
                        <td><c:out value='${finantialInstitution.companyId}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialInstitution.name" /></th>
                        <td><c:out value='${finantialInstitution.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialInstitution.companyName" /></th>
                        <td><c:out value='${finantialInstitution.companyName}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialInstitution.address" /></th>
                        <td><c:out value='${finantialInstitution.address}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialInstitution.country" /></th>
                        <td><pf:placeName place="${finantialInstitution.country}" /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialInstitution.district" /></th>
                        <td><pf:placeName place="${finantialInstitution.district}" /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialInstitution.municipality" /></th>
                        <td><pf:placeName place="${finantialInstitution.municipality}" /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialInstitution.locality" /></th>
                        <td><c:out value='${finantialInstitution.locality}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialInstitution.zipCode" /></th>
                        <td><c:out value='${finantialInstitution.zipCode}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialInstitution.telephoneContact" /></th>
                        <td><c:out value='${finantialInstitution.telephoneContact}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialInstitution.email" /></th>
                        <td><c:out value='${finantialInstitution.email}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialInstitution.webAddress" /></th>
                        <td><c:out value='${finantialInstitution.webAddress}' /></td>
                    </tr>
                </tbody>
            </table>
        </form>
    </div>
</div>

<!-- Finantial Entity section -->
<h2>
    <spring:message code="label.administration.manageFinantialInstitution.searchFinantialEntity" />
</h2>

<%
    if (TreasuryAccessControlAPI.isBackOfficeMember(TreasuryPlataformDependentServicesFactory.implementation().getLoggedUsername(), finantialInstitution)) {
%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-plus-sign" aria-hidden="true"></span> &nbsp; <a class=""
        href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/finantialentity/create?finantialInstitutionId=${finantialInstitution.externalId }">
        <spring:message code="label.event.create" />
    </a> &nbsp;
</div>
<%
    }
%>
<c:choose>
    <c:when test="${not empty finantialInstitution.finantialEntitiesSet}">
        <table id="searchfinantialentityTable" class="table responsive table-bordered table-hover" width="100%">
            <thead>
                <tr>
                    <th><spring:message code="label.FinantialEntity.name" /></th>
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
            <spring:message code="label.noResultsFound" />
        </div>
    </c:otherwise>
</c:choose>

<!-- Documents Series section -->

<h2>
    <spring:message code="label.administration.manageFinantialInstitution.searchSeries" />
</h2>

<%
    if (TreasuryAccessControlAPI.isBackOfficeMember(TreasuryPlataformDependentServicesFactory.implementation().getLoggedUsername(), finantialInstitution)) {
%>

<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-plus-sign" aria-hidden="true"></span> &nbsp; <a class=""
        href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/series/create?finantialInstitutionId=${finantialInstitution.externalId }"> <spring:message
            code="label.event.create" />
    </a> &nbsp;
</div>

<%
    }
%>
<c:choose>
    <c:when test="${not empty finantialInstitution.seriesSet}">
        <table id="searchseriesTable" class="table responsive table-bordered table-hover" width="100%">
            <thead>
                <tr>
                    <th><spring:message code="label.Series.code" /></th>
                    <th><spring:message code="label.Series.name" /></th>
                    <th><spring:message code="label.Series.active" /></th>
                    <th><spring:message code="label.Series.legacy" /></th>
                    <th><spring:message code="label.Series.externSeries" /></th>
                    <th><spring:message code="label.Series.defaultSeries" /></th>
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
            <spring:message code="label.noResultsFound" />
        </div>
    </c:otherwise>
</c:choose>

<!-- Document Templates section -->

<h2>
    <spring:message code="label.administration.manageFinantialInstitution.searchDocumentTemplate" />
</h2>
<c:choose>
    <c:when test="${not empty finantialInstitution.finantialEntitiesSet}">
        <table id="searchDocumentTemplateTable" class="table responsive table-bordered table-hover" width="100%">
            <thead>
                <tr>
                    <th><spring:message code="label.DocumentTemplate.finantialDocumentTypes" /></th>
                    <th><spring:message code="label.DocumentTemplate.finantialEntity" /></th>
                    <%-- Operations Column --%>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${finantialDocumentTypeSet}" var="type">
                    <tr>
                        <td><c:out value="${ type.type.descriptionI18N.content }" /></td>
                        <td>
                            <table>
                                <c:forEach items="${ finantialInstitution.finantialEntitiesSet }" var="entity">
                                    <c:set var="documentTemplateFile" value="${ entity.hasDocumentTemplate(type).ativeDocumentTemplateFile }" />
                                    <tr>
                                        <th><c:out value="${ entity.name.content }" />
                                        <th>
                                    </tr>
                                    <tr>
                                        <td><c:if test="${not empty documentTemplateFile }">
                                                <a
                                                    href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/treasurydocumenttemplate/search/download/${documentTemplateFile.externalId}">
                                                    <c:out value="${ documentTemplateFile.getFilename() }" />
                                                </a>
                                            &nbsp;-&nbsp;
                                            <fmt:formatNumber var="documentTemplateFileSize" value="${ documentTemplateFile.getSize() / 1024 }" maxFractionDigits="1" />
                                                <c:out value="${ documentTemplateFileSize }" />
                                            KB
                                        </c:if> <c:if test="${empty documentTemplateFile }">
                                                <span style="color: red; font-style: italic"> <spring:message code="label.DocumentTemplate.not.defined" />
                                                </span>
                                            </c:if></td>
                                    </tr>
                                </c:forEach>
                            </table>
                        </td>
                        <td>
                            <table>
                                <c:forEach items="${ finantialInstitution.finantialEntitiesSet }" var="entity">
                                    <c:set var="documentTemplate" value="${ entity.hasDocumentTemplate(type) }" />
                                    <tr>
                                        <td>
                                            <%
                                                if (TreasuryAccessControlAPI.isBackOfficeMember(TreasuryPlataformDependentServicesFactory.implementation().getLoggedUsername(), finantialInstitution)) {
                                            %> <c:if test="${ empty documentTemplate }">
                                                <a class="btn btn-default btn-xs"
                                                    href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/treasurydocumenttemplate/create?finantialdocumenttypeid=${ type.externalId }&finantialentityid=${entity.externalId}"><spring:message
                                                        code='label.create' /></a>
                                            </c:if> <%
     }
 %> <c:if test="${ not empty documentTemplate }">
                                                <a class="btn btn-default btn-xs"
                                                    href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/treasurydocumenttemplate/search/view/${documentTemplate.externalId}"><spring:message
                                                        code='label.view' /></a>
                                                <a class="btn btn-default btn-xs" href="#" onClick="javascript:processUpload('${documentTemplate.externalId}')"><spring:message
                                                        code='label.upload' /></a>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </table>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </c:when>
    <c:otherwise>
        <div class="alert alert-warning" role="alert">
            <spring:message code="label.noResultsFound" />
        </div>
    </c:otherwise>
</c:choose>

<script>
var searchfinantialentityDataSet = [
    <c:forEach items="${finantialInstitution.finantialEntitiesSet}" var="searchResult">
    {
        "name" : "<c:out value='${searchResult.name.content}'/>",
        "actions" :
             " <a  class=\"btn btn-default btn-xs\" href=\"${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/finantialentity/search/view/${searchResult.externalId}\"><spring:message code='label.view'/></a>"
    },
    </c:forEach>
];

var searchseriesDataSet = [
    <c:forEach items="${finantialInstitution.seriesSet}" var="searchResult">
    {
        "code" : "<c:out value='${searchResult.code}'/>",
        "name" : "<c:out value='${searchResult.name.content}'/>",
        "active" : <c:if test="${searchResult.active}">
                    "<spring:message code='label.true' />"
                    </c:if>
                    <c:if test="${not searchResult.active}">
                    "<spring:message code='label.false' />"
                    </c:if>,
        "legacy" : <c:if test="${searchResult.legacy}">
                      "<spring:message code='label.true' />"
                    </c:if>
                    <c:if test="${not searchResult.legacy}">
                    "<spring:message code='label.false' />"
                </c:if>,
        "externSeries" : <c:if test="${searchResult.externSeries}">
                              "<spring:message code='label.true' />"
                          </c:if>
                          <c:if test="${not searchResult.externSeries}">
                              "<spring:message code='label.false' />"
                          </c:if>,
                          <%if (TreasuryAccessControlAPI.isBackOfficeMember(TreasuryPlataformDependentServicesFactory.implementation().getLoggedUsername(), finantialInstitution)) {%>
        "defaultSeries" :<c:if test="${searchResult.defaultSeries}">
                "<spring:message code='label.true' />"
                          </c:if>
                          <c:if test="${not searchResult.defaultSeries}">
                              "(<spring:message code='label.false' />) <a href=\"${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/series/search/editDefault/${searchResult.externalId}\"><spring:message code='label.Series.makeDefaultSeries'/></a>"
                          </c:if>
                          ,
                          <%} else {%>
                          "defaultSeries" :<c:if test="${searchResult.defaultSeries}">
                          "<spring:message code='label.true' />"
                                    </c:if>
                                    <c:if test="${not searchResult.defaultSeries}">
                                        "<spring:message code='label.false' />"
                                    </c:if>
                                    ,                          
                          <%}%>
        "actions" :
             " <a  class=\"btn btn-default btn-xs\" href=\"${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/series/search/view/${searchResult.externalId}\"><spring:message code='label.view'/></a>"             
    },
    </c:forEach>
];



$(document).ready(function() {
    var table = $('#searchfinantialentityTable').DataTable({
    	language : {
    	    url : "${datatablesI18NUrl}",           
    	},
        "columns": [
            { data: 'name' },
            { data: 'actions',className:"all" }
        ],
        //CHANGE_ME adjust the actions column width if needed
        "columnDefs": [
            { "width": "54px", "targets": 1 } 
        ],
        "data" : searchfinantialentityDataSet,
        //Documentation: https://datatables.net/reference/option/dom
        //"dom": '<"col-sm-6"l><"col-sm-3"f><"col-sm-3"T>rtip', //FilterBox = YES && ExportOptions = YES
        //"dom": 'T<"clear">lrtip', //FilterBox = NO && ExportOptions = YES
        "dom": '', 
        //"dom": '<"col-sm-6"l>rtip', // FilterBox = NO && ExportOptions = NO
    });
    var table2 = $('#searchseriesTable').DataTable({
        language : {
            url : "${datatablesI18NUrl}",           
        },
        "columns": [
            { data: 'code' },
            { data: 'name' },
            { data: 'active' },
            { data: 'externSeries' },
            { data: 'legacy' },
            { data: 'defaultSeries' },
            { data: 'actions',className:"all" }                    
        ],
        //CHANGE_ME adjust the actions column width if needed
        "columnDefs": [
            { "width": "54px", "targets": 4 } 
        ],
        "data" : searchseriesDataSet,
        //Documentation: https://datatables.net/reference/option/dom
        "dom": '', 
        //"dom": 'T<"clear">lrtip', //FilterBox = NO && ExportOptions = YES
        //"dom": '<"col-sm-6"l><"col-sm-6"f>rtip', //FilterBox = YES && ExportOptions = NO
        //"dom": '<"col-sm-6"l>rtip', // FilterBox = NO && ExportOptions = NO
    });
    table2.columns.adjust().draw();
    
    var table3 = $('#searchDocumentTemplateTable').DataTable({
        language : {
            url : "${datatablesI18NUrl}",           
        },
        //CHANGE_ME adjust the actions column width if needed
        "columnDefs": [
			{ "width": "205px", "targets": 0 }, 
            { "width": "80px", "targets": 2 } 
        ],
        "dom": '', 
    });

    table3.columns.adjust().draw();
    $('#searchfinantialentityTable tbody').on( 
	  	'click', 
		'tr', 
		function () {
		    $(this).toggleClass('selected');
        } 
	);    
    table.columns.adjust().draw();
    $('#searchseriesTable tbody').on( 
        'click', 
        'tr', 
        function () {
            $(this).toggleClass('selected');
        } 
    );  
}); 
</script>
