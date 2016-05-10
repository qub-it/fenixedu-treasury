<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<%@ taglib prefix="datatables" uri="http://github.com/dandelion/datatables"%>

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
			code="label.administration.payments.sibs.managePaymentReferenceCode.searchPaymentReferenceCode" />
		<small></small>
	</h1>
</div>
<%-- NAVIGATION 
<div class="well well-sm" style="display:inline-block">
	<span class="glyphicon glyphicon-plus-sign" aria-hidden="true"></span>&nbsp;<a class="" href="${pageContext.request.contextPath}/treasury/administration/payments/sibs/managepaymentreferencecode/paymentreferencecode/create"   ><spring:message code="label.event.create" /></a>
&nbsp;|&nbsp;</div>
--%>
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
					<spring:message code="label.PaymentReferenceCode.referenceCode" />
				</div>

				<div class="col-sm-10">
					<input id="paymentReferenceCode_referenceCode" class="form-control"
						type="text" name="referencecode"
						value='<c:out value='${not empty param.referencecode ? param.referencecode : paymentReferenceCode.referenceCode }'/>' />
				</div>
			</div>
			
			<%--
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.PaymentReferenceCode.beginDate" />
				</div>

				<div class="col-sm-4">
					<input id="paymentReferenceCode_beginDate" class="form-control"
						type="text" name="begindate" bennu-date
						value='<c:out value='${not empty param.begindate ? param.begindate : paymentReferenceCode.beginDate.toString("yyyy-MM-dd") }'/>' />
				</div>
			</div>
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.PaymentReferenceCode.endDate" />
				</div>

				<div class="col-sm-4">
					<input id="paymentReferenceCode_endDate" class="form-control"
						type="text" name="enddate" bennu-date
						value='<c:out value='${not empty param.enddate ? param.enddate : paymentReferenceCode.endDate.toString("yyyy-MM-dd") }'/>' />
				</div>
			</div>
			--%>
			
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.PaymentReferenceCode.state" />
				</div>

				<div class="col-sm-4">
					<select id="paymentReferenceCode_state" class="form-control"
						name="state">
						<option value="">&nbsp;</option>
						<%-- empty option remove it if you don't want to have it or give it a label CHANGE_ME--%>
						<c:forEach items="${stateValues}" var="field">
							<option value='<c:out value='${field}'/>'><c:out value='${field.descriptionI18N.content}' /></option>
						</c:forEach>
					</select>
					<script>
		$("#paymentReferenceCode_state").val('<c:out value='${not empty param.state ? param.state : paymentReferenceCode.state }'/>');
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


<c:choose>
	<c:when test="${not empty searchpaymentreferencecodeResultsDataSet}">
		<datatables:table id="searchpaymentreferencecodeTable" row="referenceCode" data="${searchpaymentreferencecodeResultsDataSet}"
            cssClass="table responsive table-bordered table-hover"
            cdn="false" cellspacing="2">
		
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.PaymentReferenceCode.referenceCode" />
                </datatables:columnHead>
                <c:out value="${referenceCode.formattedCode}" />
            </datatables:column>
			
            <datatables:column>
                <datatables:columnHead>
                    <spring:message code="label.PaymentReferenceCode.state" />
                </datatables:columnHead>
                
                <c:if test="${referenceCode.state=='USED'}">
                    <c:set var="referenceCodeStateCss" scope="request" value="label label-primary" />
                </c:if>
                <c:if test="${referenceCode.state=='ANNULLED'}">
                    <c:set var="referenceCodeStateCss" scope="request" value="label label-danger" />
                </c:if>
                <c:if test="${referenceCode.state=='UNUSED'}">
                    <c:set var="referenceCodeStateCss" scope="request" value="label label-default" />
                </c:if>
                <c:if test="${referenceCode.state=='PROCESSED'}">
                    <c:set var="referenceCodeStateCss" scope="request" value="label label-success" />
                </c:if>
                  
				<span class="${referenceCodeStateCss}" >
					<c:out value="${referenceCode.state.descriptionI18N.content}" />
				</span>
            </datatables:column>

			<datatables:column>
                <datatables:columnHead>
                    <spring:message code="label.PaymentReferenceCode.client.name" />
                </datatables:columnHead>
                				
 				<c:if test="${referenceCode.targetPayment != null && referenceCode.targetPayment.debtAccount != null}">
					<c:out value="${referenceCode.targetPayment.debtAccount.customer.name}" />
 				</c:if>
			</datatables:column>

			<datatables:column>
                <datatables:columnHead>
                    <spring:message code="label.PaymentReferenceCode.client.vatNumber" />
                </datatables:columnHead>
				
 				<c:if test="${referenceCode.targetPayment != null && referenceCode.targetPayment.debtAccount != null}">
					<c:out value="${referenceCode.targetPayment.debtAccount.customer.fiscalNumber}" />
				</c:if>
			</datatables:column>

			<datatables:column>
                <datatables:columnHead>
                    <spring:message code="label.PaymentReferenceCode.client.businessNumber" />
                </datatables:columnHead>
		
 				<c:if test="${referenceCode.targetPayment != null && referenceCode.targetPayment.debtAccount != null}">
					<c:out value="${referenceCode.targetPayment.debtAccount.customer.businessIdentification}" />
				</c:if>
			</datatables:column>

			
            <datatables:column cssStyle="width:10%">
                <a class="btn btn-default btn-xs" href="${pageContext.request.contextPath}/treasury/administration/payments/sibs/managepaymentreferencecode/paymentreferencecode/search/view/${referenceCode.externalId}">
                        <spring:message code="label.view" />
                </a>
			</datatables:column>
			
		</datatables:table>

        <script>
			createDataTables(
					'searchpaymentreferencecodeTable',
					false,
					false,
					true,
					"${pageContext.request.contextPath}",
					"${datatablesI18NUrl}");
		</script>

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
