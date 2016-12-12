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
            code="label.administration.manageFinantialInstitution.createSeries" />
        <small></small>
    </h1>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a
        class=""
        href="${pageContext.request.contextPath}/treasury/administration/managefinantialinstitution/finantialinstitution/read/${finantialInstitutionId}"><spring:message
            code="label.event.back" /></a> &nbsp;
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

<form method="post" class="form-horizontal">
    <div class="panel panel-default">
        <div class="panel-body">
            <input type="hidden" name="finantialInstitutionId"
                value="${ finantialInstitutionId }" />
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.Series.code" />
                </div>

                <div class="col-sm-10">
                    <input id="series_code" class="form-control"
                        type="text" name="code"
                        value='<c:out value='${not empty param.code ? param.code : series.code }'/>'
                        required />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.Series.name" />
                </div>

                <div class="col-sm-10">
                    <input id="series_name" class="form-control"
                        type="text" name="name" bennu-localized-string
                        value='${not empty param.name ? param.name : "{}" }'  required />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.Series.externSeries" />
                </div>
                
                <div class="col-sm-2">
                    <select id="series_externSeries" name="externseries"
                        class="form-control">
                        <option value="false"><spring:message
                                code="label.no" /></option>
                        <option value="true"><spring:message
                                code="label.yes" /></option>
                    </select>
                    <script>
	                    $(document).ready(function() {
		                    $("#series_externSeries").select2().select2("val", "<c:out value='${not empty param.externseries ? param.externseries : series.externSeries }' />");
	                    });
                    </script>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.Series.certificated" />
                </div>

                <div class="col-sm-2">
                    <select id="series_certificated" name="certificated"
                        class="form-control">
                        <option value="false"><spring:message
                                code="label.no" /></option>
                        <option value="true"><spring:message
                                code="label.yes" /></option>
                    </select>
                    <script>
	                    $(document).ready(function() {
		                    $("#series_certificated").select2().select2("val", "<c:out value='${not empty param.certificated ? param.certificated : series.certificated }' />");
	                    });
                    </script>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.Series.legacy" />
                </div>

                <div class="col-sm-2">
                    <select id="series_legacy" name="legacy"
                        class="form-control">
                        <option value="false">
                        	<spring:message code="label.no" />
                        </option>
                        <option value="true">
                        	<spring:message code="label.yes" />
                        </option>
                    </select>
                    <script>
	                    $(document).ready(function() {
		                    $("#series_legacy").select2().select2("val", "<c:out value='${not empty param.legacy ? param.legacy : series.legacy }' />");
	                    });
                    </script>
                </div>
            </div>
            <div>
                <div class="col-sm-2 control-label">
                    <spring:message code="label.Series.selectable" />
                </div>
                <div class="col-sm-2">
                    <select id="series_selectable" name="selectable"
                        class="form-control">
                        <option value="false">
                        	<spring:message code="label.no" />
                        </option>
                        <option value="true">
                        	<spring:message code="label.yes" />
                        </option>
                    </select>
                    <script>
	                    $(document).ready(function() {
		                    $("#series_selectable").select2().select2("val", "<c:out value='${not empty param.selectable ? param.selectable : series.selectable }' />");
	                    });
                    </script>
                </div>
            </div>
        </div>
        <div class="panel-footer">
            <input type="submit" class="btn btn-default" role="button"
                value="<spring:message code="label.submit" />" />
        </div>
    </div>
</form>

<script>
</script>
