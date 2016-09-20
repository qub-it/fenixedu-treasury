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

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<link href="${pageContext.request.contextPath}/static/treasury/css/dataTables.responsive.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/static/treasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<script src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>



<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.integration.erp.updateERPConfiguration" />
        <small></small>
    </h1>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a class=""
        href="${pageContext.request.contextPath}/treasury/integration/erp/erpconfiguration/read/${eRPConfiguration.externalId}"><spring:message code="label.event.back" /></a>
    &nbsp;</a>
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

<form method="post" class="form-horizontal">

    <div class="panel panel-default">
        <div class="panel-body">
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ERPConfiguration.active" />
                </div>

                <div class="col-sm-2">
                    <select id="eRPConfiguration_active" name="active" class="form-control">
                        <option value="false"><spring:message code="label.no" /></option>
                        <option value="true"><spring:message code="label.yes" /></option>
                    </select>
                    <script>
        $("#eRPConfiguration_active").val('<c:out value='${not empty param.active ? param.active : eRPConfiguration.active }'/>');
    </script>
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ERPConfiguration.exportAnnulledRelatedDocuments" />
                </div>

                <div class="col-sm-2">
                    <select id="eRPConfiguration_exportAnnulledRelatedDocuments" name="exportannulledrelateddocuments" class="form-control">
                        <option value="false"><spring:message code="label.no" /></option>
                        <option value="true"><spring:message code="label.yes" /></option>
                    </select>
                    <script>
		$("#eRPConfiguration_exportAnnulledRelatedDocuments").val('<c:out value='${not empty param.exportannulledrelateddocuments ? param.exportannulledrelateddocuments : eRPConfiguration.exportAnnulledRelatedDocuments }'/>');
	</script>
                </div>
            </div>


            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ERPConfiguration.exportOnlyRelatedDocumentsPerExport" />
                </div>

                <div class="col-sm-2">
                    <select id="eRPConfiguration_exportOnlyRelatedDocumentsPerExport" name="exportonlyrelateddocumentsperexport" class="form-control">
                        <option value="false"><spring:message code="label.no" /></option>
                        <option value="true"><spring:message code="label.yes" /></option>
                    </select>
                    <script>
        $("#eRPConfiguration_exportOnlyRelatedDocumentsPerExport").val('<c:out value='${not empty param.exportonlyrelateddocumentsperexport ? param.exportonlyrelateddocumentsperexport : eRPConfiguration.exportOnlyRelatedDocumentsPerExport }'/>');
    </script>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ERPConfiguration.externalURL" />
                </div>

                <div class="col-sm-10">
                    <input id="eRPConfiguration_externalURL" class="form-control" type="text" name="externalurl"
                        value='<c:out value='${not empty param.externalurl ? param.externalurl : eRPConfiguration.externalURL }'/>' />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ERPConfiguration.username" />
                </div>

                <div class="col-sm-10">
                    <input id="eRPConfiguration_username" class="form-control" type="text" name="username"
                        value='<c:out value='${not empty param.username ? param.username : eRPConfiguration.username }'/>' />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ERPConfiguration.password" />
                </div>

                <div class="col-sm-10">
                    <input id="eRPConfiguration_password" class="form-control" type="text" name="password"
                        value='<c:out value='${not empty param.password ? param.password : eRPConfiguration.password }'/>' />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ERPConfiguration.paymentsIntegrationSeries" />
                </div>

                <div class="col-sm-4">
                    <%-- Relation to side 1 drop down rendered in input --%>
                    <select id="eRPConfiguration_paymentsIntegrationSeries" class="js-example-basic-single" name="paymentsintegrationseries">
                        <option value="">&nbsp;</option>
                        <%-- empty option remove it if you don't want to have it or give it a label CHANGE_ME --%>
                    </select>
                </div>
            </div>


            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ERPConfiguration.implementationClassName" />
                </div>

                <div class="col-sm-10">
                    <input id="eRPConfiguration_implementationClassName" class="form-control" type="text" name="implementationclassname"
                        value='<c:out value='${not empty param.implementationclassname ? param.implementationclassname : eRPConfiguration.implementationClassName }'/>' />
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ERPConfiguration.maxSizeBytesToExportOnline" />
                </div>

                <div class="col-sm-10">
                    <input id="eRPConfiguration_maxSizeBytesToExportOnline" class="form-control" type="number" min="0" step="1" name="maxsizebytestoexportonlineModel"
                        value='<c:out value='${not empty param.maxsizebytestoexportonline ? param.maxsizebytestoexportonline : eRPConfiguration.maxSizeBytesToExportOnline }'/>' />
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ERPConfiguration.erpIdProcess" />
                </div>

                <div class="col-sm-10">
                    <input id="eRPConfiguration_erpIdProcess" class="form-control" type="text" name="erpidprocess"
                        value='<c:out value='${not empty param.erpidprocess ? param.erpidprocess : eRPConfiguration.erpIdProcess }'/>' />
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

<%-- Block for providing paymentsIntegrationSeries options --%>
<%-- CHANGE_ME --%> <%-- INSERT YOUR FORMAT FOR element --%>
paymentsIntegrationSeries_options = [
	<c:forEach items="${ERPConfiguration_paymentsIntegrationSeries_options}" var="element"> 
		{
			text : "<c:out value='${element.code} - ${element.name.content}'/>", 
			id : "<c:out value='${element.externalId}'/>"
		},
	</c:forEach>
];

$("#eRPConfiguration_paymentsIntegrationSeries").select2(
	{
		data : paymentsIntegrationSeries_options,
	}	  
		    );
		    
		    
		    $("#eRPConfiguration_paymentsIntegrationSeries").select2().select2('val', '<c:out value='${not empty param.paymentsintegrationseries ? param.paymentsintegrationseries : eRPConfiguration.paymentsIntegrationSeries.externalId }'/>');
		    <%-- End block for providing paymentsIntegrationSeries options --%>

	});
</script>
