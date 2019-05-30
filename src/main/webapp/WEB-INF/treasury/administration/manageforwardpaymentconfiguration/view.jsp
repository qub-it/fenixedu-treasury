<%@page import="org.fenixedu.treasury.ui.administration.forwardpayments.ManageForwardPaymentConfigurationController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<%@ taglib prefix="datatables" uri="http://github.com/dandelion/datatables"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

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
		<spring:message code="label.ManageForwardPaymentConfiguration.view" />
		<small></small>
	</h1>
</div>

<script type="text/javascript">
	function processUpload() {
		$('#uploadModal').modal('toggle')
	}
</script>

<div class="modal fade" id="uploadModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="uploadForm" method="POST" enctype="multipart/form-data"
            	action="${pageContext.request.contextPath}<%=ManageForwardPaymentConfigurationController.UPLOAD_VIRTUAL_TPA_CERTIFICATE_URL %>/${finantialInstitution.externalId}/${forwardPaymentConfiguration.externalId}">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title">
                        <spring:message code="label.ManageForwardPaymentConfiguration.upload.certificate" />
                    </h4>
                </div>
                <div class="modal-body">
                    <input type="file" name="certificateFile" />
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


<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
	<span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;
	<a class="" href="${pageContext.request.contextPath}<%= ManageForwardPaymentConfigurationController.SEARCH_URL %>/${finantialInstitution.externalId}">
		<spring:message code="label.event.back" />
	</a>
	&nbsp;|&nbsp;
	
	<span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>&nbsp;
	<a class="" href="${pageContext.request.contextPath}<%= ManageForwardPaymentConfigurationController.EDIT_URL %>/${finantialInstitution.externalId}/${forwardPaymentConfiguration.externalId}">
		<spring:message code="label.event.update" />
	</a>
	&nbsp;|&nbsp;
	
	<span class="glyphicon glyphicon-upload" aria-hidden="true"></span>&nbsp;
	<a href="#" onClick="javascript:processUpload();">
		<spring:message code="label.ManageForwardPaymentConfiguration.upload.tpa.certificate" />
	</a>
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
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.active" /></th>
						<td>
							<c:if test="${forwardPaymentConfiguration.active}">
								<spring:message code="label.true" />
							</c:if>
							<c:if test="${not forwardPaymentConfiguration.active}">
								<spring:message code="label.false" />
							</c:if>
						</td>
                    </tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.name" /></th>
						<td><c:out value='${forwardPaymentConfiguration.name}' /></td>
					</tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.paymentURL" /></th>
						<td><c:out value='${forwardPaymentConfiguration.paymentURL}' /></td>
					</tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.returnURL" /></th>
						<td><c:out value='${forwardPaymentConfiguration.returnURL}' /></td>
					</tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.virtualTPAMOXXURL" /></th>
						<td><c:out value='${forwardPaymentConfiguration.virtualTPAMOXXURL}' /></td>
					</tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.virtualTPAMerchantId" /></th>
                        <td><c:out value='${forwardPaymentConfiguration.virtualTPAMerchantId}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.virtualTPAId" /></th>
                        <td><c:out value='${forwardPaymentConfiguration.virtualTPAId}' /></td>
                    </tr>
                    <%-- 
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.virtualTPAKeyStoreName" /></th>
                        <td><c:out value='${forwardPaymentConfiguration.virtualTPAKeyStoreName}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.virtualTPACertificateAlias" /></th>
                        <td><c:out value='${forwardPaymentConfiguration.virtualTPACertificateAlias}' /></td>
                    </tr>
                    --%>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.virtualTPACertificatePassword" /></th>
                        <td><c:out value='${forwardPaymentConfiguration.virtualTPACertificatePassword}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.implementation" /></th>
                        <td><c:out value='${forwardPaymentConfiguration.implementation}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.paylineMerchantId" /></th>
                        <td><c:out value='${forwardPaymentConfiguration.paylineMerchantId}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.paylineMerchantAccessKey" /></th>
                        <td><c:out value='${forwardPaymentConfiguration.paylineMerchantAccessKey}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.paylineContractNumber" /></th>
                        <td><c:out value='${forwardPaymentConfiguration.paylineContractNumber}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.series" /></th>
                        <td><c:out value='${forwardPaymentConfiguration.series.name.content}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.paymentMethod" /></th>
                        <td><c:out value='${forwardPaymentConfiguration.paymentMethod.name.content}' /></td>
                    </tr>
                    <c:if test="${not empty forwardPaymentConfiguration.virtualTPACertificate}">
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.virtualTPACertificate" /></th>
                        <td>
                        	<a href="${pageContext.request.contextPath}<%= ManageForwardPaymentConfigurationController.DOWNLOAD_VIRTUAL_TPA_CERTIFICATE_URL %>/${finantialInstitution.externalId}/${forwardPaymentConfiguration.externalId}">
	                        	<c:out value='${forwardPaymentConfiguration.virtualTPACertificate.getFilename()}' />
                        	</a>
                        </td>
                    </tr>
                    </c:if>


                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.reimbursementPolicyJspFile" /></th>
                        <td><c:out value='${forwardPaymentConfiguration.reimbursementPolicyJspFile}' /></td>
                    </tr>

                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentConfiguration.privacyPolicyJspFile" /></th>
                        <td><c:out value='${forwardPaymentConfiguration.privacyPolicyJspFile}' /></td>
                    </tr>
				</tbody>
			</table>
		</form>
	</div>
</div>

<script>
</script>
