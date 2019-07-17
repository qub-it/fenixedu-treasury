<%@page import="org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory"%>
<%@page import="org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI"%>
<%@page import="org.fenixedu.treasury.domain.FinantialInstitution"%>
<%@page import="org.fenixedu.treasury.domain.forwardpayments.ForwardPayment"%>
<%@page import="org.fenixedu.treasury.ui.document.forwardpayments.ManageForwardPaymentsController"%>
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
		<spring:message code="label.ManageForwardPayments.verifyForwardPayment" />
		<small></small>
	</h1>
</div>


<div class="modal fade" id="registerPaymentModal"> 
   <div class="modal-dialog"> 
     <div class="modal-content">

       <div class="modal-header">
         <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button> 
         <h4 class="modal-title"><spring:message code="label.ManageForwardPayments.registerPayment.title"/></h4>
       </div> 

     <form id ="registerPaymentModalForm" action="${pageContext.request.contextPath}<%= ManageForwardPaymentsController.REGISTER_PAYMENT_URL %>/${forwardPayment.externalId}"  method="POST">

	       <div class="modal-body"> 
       
		        <p><em><spring:message code = "label.ManageForwardPayments.registerPayment.message.confirm"/></em></p>
	            <p>&nbsp;</p>
	            <div class="form-group row">
	                <div class="col-sm-2 control-label">
	                    <spring:message code="label.ManageForwardPayments.registerPayment.justitication" />
	                </div>
	
	                <div class="col-sm-10">
	                    <input class="form-control" type="text" name="justification" />
	                </div>
	            </div>
		       </div> 
		       
		       <div class="modal-footer"> 
		         <button type="button" class="btn btn-default" data-dismiss="modal"><spring:message code = "label.close"/></button>
		         <button id="registerButton" class ="btn btn-danger" type="submit"><spring:message code = "label.ManageForwardPayments.register.button"/></button>
		       </div> 
       </form> 
       
     </div> 
   </div> 
 </div>

<!-- /.modal -->
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
	<span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;
	<a class="" href="${pageContext.request.contextPath}<%= ManageForwardPaymentsController.SEARCH_URL %>">
		<spring:message code="label.event.back" />
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

<%
	ForwardPayment forwardPayment = (ForwardPayment) request.getAttribute("forwardPayment");
	FinantialInstitution finantialInstitution = forwardPayment.getDebtAccount().getFinantialInstitution();
%>

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
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.platform" /></th>
                        <td><c:out value='${forwardPayment.forwardPaymentConfiguration.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.whenOccured" /></th>
                        <td><c:out value='${forwardPayment.whenOccured.toString("yyyy-MM-dd HH:mm:ss")}' /></td>
                    </tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.orderNumber" /></th>
						<td><c:out value='${forwardPayment.orderNumber}' /></td>
					</tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.customerName" /></th>
						<td><c:out value='${forwardPayment.debtAccount.customer.name}' /></td>
					</tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.businessCustomerId" /></th>
						<td><c:out value='${forwardPayment.debtAccount.customer.businessIdentification}' /></td>
					</tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.amount" /></th>
						<td><c:out value='${forwardPayment.debtAccount.finantialInstitution.currency.getValueFor(forwardPayment.amount)}' /></td>
					</tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.currentState" /></th>
						<td><c:out value='${forwardPayment.currentState.localizedName.content}' /></td>
					</tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.transactionId" /></th>
                        <td><c:out value='${forwardPayment.transactionId}' /></td>
                    </tr>
                    
<%
if (TreasuryAccessControlAPI.isManager(TreasuryPlataformDependentServicesFactory.implementation().getLoggedUsername())) {
%>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.forwardPaymentSuccessUrl" /></th>
                        <td><c:out value='${forwardPayment.forwardPaymentSuccessUrl}' /></td>
                    </tr>                    
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.forwardPaymentInsuccessUrl" /></th>
                        <td><c:out value='${forwardPayment.forwardPaymentInsuccessUrl}' /></td>
                    </tr>
<%
}
%>
					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.debitEntries" /></th>
                        <td>
                        	<ul>
                        		<c:forEach var="d" items="${forwardPayment.debitEntriesSet}">
	                        		<li><c:out value="${d.description}" /></li>
                        		</c:forEach>
                        	</ul>
                        </td>
					</tr>
					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPayment.justification" /></th>
                        <td><c:out value='${forwardPayment.justification}' /></td>
					</tr>
				</tbody>
			</table>
		</form>
	</div>
</div>

<h2>
	<spring:message code="label.ManageForwardPayments.forwardPaymentStatus" />
</h2>

<p><strong><em><spring:message code="label.ManageForwardPayments.forwardPaymentStatus.message" /></em></strong></p>

<div class="panel panel-primary">
	<div class="panel-body">
		<form method="post" class="form-horizontal">
			<table class="table">
				<tbody>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentStatusBean.invocationSuccess" /></th>
                        <td>
                        	<c:if test="${paymentStatusBean.invocationSuccess}">
                        		<spring:message code="label.true" />
                        	</c:if> 
                        	<c:if test="${not paymentStatusBean.invocationSuccess}">
                        		<spring:message code="label.true" />
                        	</c:if> 
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentStatusBean.type" /></th>
                        <td><c:out value='${paymentStatusBean.stateType.localizedName.content}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentStatusBean.authorizationNumber" /></th>
                        <td><c:out value='${paymentStatusBean.authorizationNumber}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentStatusBean.authorizationDate" /></th>
                        <td><c:out value='${paymentStatusBean.authorizationDate.toString("yyyy-MM-dd HH:mm:ss")}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentStatusBean.transactionId" /></th>
                        <td><c:out value='${paymentStatusBean.transactionId}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentStatusBean.transactionDate" /></th>
                        <td><c:out value='${paymentStatusBean.transactionDate.toString("yyyy-MM-dd HH:mm:ss")}' /></td>
                    </tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentStatusBean.statusCode" /></th>
						<td><c:out value='${paymentStatusBean.statusCode}' /></td>
					</tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentStatusBean.statusMessage" /></th>
						<td><c:out value='${paymentStatusBean.statusMessage}' /></td>
					</tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentStatusBean.requestLogFile" /></th>
						<td><c:out value='${paymentStatusBean.requestBody}' /></td>
					</tr>
					<tr>
						<th scope="row" class="col-xs-3"><spring:message code="label.ForwardPaymentStatusBean.responseLogFile" /></th>
						<td><c:out value='${paymentStatusBean.responseBody}' /></td>
					</tr>
				</tbody>
			</table>
		</form>
	</div>
</div>

<c:if test="${paymentStatusBean.isAbleToRegisterPostPayment(forwardPayment)}">
	
	<p><strong><spring:message code="label.ManageForwardPayments.register.payment.message" /></strong></p>
	
	<button class="btn btn-primary" data-toggle="modal" data-target="#registerPaymentModal"> 
		<spring:message code="label.ManageForwardPayments.register.button" />
	</button>

</c:if>

