<%@page import="org.fenixedu.treasury.ui.administration.forwardpayments.ManageForwardPaymentConfigurationController"%>
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
		<spring:message code="label.ManageForwardPaymentConfiguration.edit" />
		<small></small>
	</h1>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
	<span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;
	<a class="" href="${pageContext.request.contextPath}<%= ManageForwardPaymentConfigurationController.VIEW_URL %>/${finantialInstitution.externalId}/${forwardPaymentConfiguration.externalId}">
		<spring:message code="label.event.back" />
	</a>
	&nbsp;
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
					<spring:message code="label.ForwardPaymentConfiguration.active" />
				</div>

				<div class="col-sm-2">
					<select id="forwardpaymentconfiguration_active" name="active" class="form-control">
						<option value="false"><spring:message code="label.no" /></option>
						<option value="true"><spring:message code="label.yes" /></option>
					</select>
					<script>
						$("#forwardpaymentconfiguration_active").select2().select2('val', '<c:out value='${not empty param.active ? param.active : bean.active }' />');
					</script>
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.name" />
				</div>

				<div class="col-sm-10">
					<input id="forwardpaymentconfiguration_name" class="form-control" type="text" name="name" value='<c:out value='${not empty param.code ? param.code : bean.name }'/>' />
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.paymentURL" />
				</div>

				<div class="col-sm-10">
					<input id="forwardpaymentconfiguration_paymentURL" class="form-control" type="text" name="paymentURL" value='<c:out value='${not empty param.paymentURL ? param.paymentURL : bean.paymentURL }'/>' />
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.returnURL" />
				</div>

				<div class="col-sm-10">
					<input id="forwardpaymentconfiguration_returnURL" class="form-control" type="text" name="returnURL" value='<c:out value='${not empty param.returnURL ? param.returnURL : bean.returnURL }'/>' />
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.virtualTPAMOXXURL" />
				</div>

				<div class="col-sm-10">
					<input id="forwardpaymentconfiguration_virtualTPAMOXXURL" class="form-control" type="text" name="virtualTPAMOXXURL" value='<c:out value='${not empty param.virtualTPAMOXXURL ? param.virtualTPAMOXXURL : bean.virtualTPAMOXXURL }'/>' />
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.virtualTPAMerchantId" />
				</div>

				<div class="col-sm-10">
					<input id="forwardpaymentconfiguration_virtualTPAMerchantId" class="form-control" type="text" name="virtualTPAMerchantId" value='<c:out value='${not empty param.virtualTPAMerchantId ? param.virtualTPAMerchantId : bean.virtualTPAMerchantId }'/>' />
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.virtualTPAId" />
				</div>

				<div class="col-sm-10">
					<input id="forwardpaymentconfiguration_virtualTPAId" class="form-control" type="text" name="virtualTPAId" value='<c:out value='${not empty param.virtualTPAId ? param.virtualTPAId : bean.virtualTPAId }'/>' />
				</div>
			</div>

			<%-- 
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.virtualTPAKeyStoreName" />
				</div>

				<div class="col-sm-10">
					<input id="forwardpaymentconfiguration_virtualTPAKeyStoreName" class="form-control" type="text" name="virtualTPAKeyStoreName" value='<c:out value='${not empty param.virtualTPAKeyStoreName ? param.virtualTPAKeyStoreName : bean.virtualTPAKeyStoreName }'/>' />
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.virtualTPACertificateAlias" />
				</div>

				<div class="col-sm-10">
					<input id="forwardpaymentconfiguration_virtualTPACertificateAlias" class="form-control" type="text" name="virtualTPACertificateAlias" value='<c:out value='${not empty param.virtualTPACertificateAlias ? param.virtualTPACertificateAlias : bean.virtualTPACertificateAlias }'/>' />
				</div>
			</div>
			--%>
			
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.virtualTPACertificatePassword" />
				</div>

				<div class="col-sm-10">
					<input id="forwardpaymentconfiguration_virtualTPACertificatePassword" class="form-control" type="text" name="virtualTPACertificatePassword" value='<c:out value='${not empty param.virtualTPACertificatePassword ? param.virtualTPACertificatePassword : bean.virtualTPACertificatePassword }'/>' />
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.implementation" />
				</div>

				<div class="col-sm-10">
					<input id="forwardpaymentconfiguration_implementation" class="form-control" type="text" name="implementation" value='<c:out value='${not empty param.implementation ? param.implementation : bean.implementation }'/>' />
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.paylineMerchantId" />
				</div>

				<div class="col-sm-10">
					<input id="forwardpaymentconfiguration_paylineMerchantId" class="form-control" type="text" name="paylineMerchantId" value='<c:out value='${not empty param.paylineMerchantId ? param.paylineMerchantId : bean.paylineMerchantId }'/>' />
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.paylineMerchantAccessKey" />
				</div>

				<div class="col-sm-10">
					<input id="forwardpaymentconfiguration_paylineMerchantAccessKey" class="form-control" type="text" name="paylineMerchantAccessKey" value='<c:out value='${not empty param.paylineMerchantAccessKey ? param.paylineMerchantAccessKey : bean.paylineMerchantAccessKey }'/>' />
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.paylineContractNumber" />
				</div>

				<div class="col-sm-10">
					<input id="forwardpaymentconfiguration_paylineContractNumber" class="form-control" type="text" name="paylineContractNumber" value='<c:out value='${not empty param.paylineContractNumber ? param.paylineContractNumber : bean.paylineContractNumber }'/>' />
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.series" />
				</div>

				<div class="col-sm-10">
					<select id="forwardpaymentconfiguration_series" class="js-example-basic-single" name="series" required>
						<c:forEach items="${series_options}" var="s">
							<option value="${s.externalId}"><c:out value="${s.name.content}" /></option>
						</c:forEach>
					</select>
					
					<script>
						$(document).ready(function() {
							$("#forwardpaymentconfiguration_series").select2().select2('val','${param.series != null ? param.series : bean.series.externalId}');		
						});
					</script>
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.paymentMethod" />
				</div>

				<div class="col-sm-10">
					<select id="forwardpaymentconfiguration_paymentMethod" class="js-example-basic-single" name="paymentMethod" required>
						<c:forEach items="${paymentMethod_options}" var="p">
							<option value="${p.externalId}"><c:out value="${p.name.content}" /></option>
						</c:forEach>
					</select>
					<script>
						$(document).ready(function() {
							$("#forwardpaymentconfiguration_paymentMethod").select2().select2('val','${param.paymentMethod != null ? param.paymentMethod : bean.paymentMethod.externalId}');		
						});
					</script>
				</div>
			</div>
			
			
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.reimbursementPolicyJspFile" />
				</div>

				<div class="col-sm-10">
					<input id="forwardpaymentconfiguration_reimbursementPolicyJspFile" class="form-control" type="text" name="reimbursementPolicyJspFile" value='<c:out value='${not empty param.reimbursementPolicyJspFile ? param.reimbursementPolicyJspFile : bean.reimbursementPolicyJspFile }'/>' />
				</div>
			</div>
			
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ForwardPaymentConfiguration.privacyPolicyJspFile" />
				</div>

				<div class="col-sm-10">
					<input id="forwardpaymentconfiguration_privacyPolicyJspFile" class="form-control" type="text" name="privacyPolicyJspFile" value='<c:out value='${not empty param.privacyPolicyJspFile ? param.privacyPolicyJspFile : bean.privacyPolicyJspFile }'/>' />
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

});

</script>
