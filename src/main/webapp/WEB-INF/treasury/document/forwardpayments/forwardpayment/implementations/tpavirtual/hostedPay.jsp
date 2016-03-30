<%@page import="org.fenixedu.treasury.ui.document.forwardpayments.ForwardPaymentController"%>
<%@page import="java.util.Map"%>
<%@page import="org.fenixedu.treasury.domain.forwardpayments.ForwardPayment"%>
<%@page import="org.fenixedu.treasury.domain.forwardpayments.implementations.TPAVirtualImplementation"%>
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

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%-- ${portal.toolkit()} --%>
${portal.angularToolkit()}


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

<script
    src="${pageContext.request.contextPath}/webjars/angular-sanitize/1.3.11/angular-sanitize.js"></script>
<link rel="stylesheet" type="text/css"
    href="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.css" />
<script
    src="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.js"></script>

<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message
            code="label.administration.manageCustomer.createSettlementNote.chooseInvoiceEntries" />
        <small></small>
    </h1>

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

<div>
    <p>
        1. <spring:message code="label.ForwardPaymentController.chooseInvoiceEntries" />
        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span> 
        2. <spring:message code="label.ForwardPaymentController.confirmPayment" />
        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span> 
        <strong>3. <spring:message code="label.ForwardPaymentController.enterPaymentDetails" /></strong>
        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span> 
        4. <spring:message code="label.ForwardPaymentController.paymentConfirmation" />
    </p>
</div>

<script type="text/javascript">
var CCom = "${forwardPayment.forwardPaymentConfiguration.merchantId}";
var A001 = "${forwardPayment.forwardPaymentConfiguration.virtualTPAId}";

/*	
	CCom - Número do cartão do comerciante
	A001 - Número do TPA Virtual
	C007 - Referência do pagamento
	A061 - Montante da operação
	C046 - URL do CSS
	C012 - URL da página de confirmação da encomenda do Comerciante
	iFrame - Nome do iFrame onde será apresentado o formulário (Campo não obrigatório)
	popup - Utilizar popup? (true / false)
	popupHeight - Largura da popup
	popupWidth - Altura da popup
*/ 
function submitPaymentByFrame(){
	submitPayment('netcaixa');
}

function submitPayment(iFrame, popup, popupHeight, popupWidth){

	var hiddenForm = document.createElement('FORM');
	hiddenForm.name = "frmPayment";
	hiddenForm.method = "POST";
	
	var height = 570; 
	var width = 470;
	if(popupHeight != undefined){
		height = popupHeight;
	}
	
	if(popupWidth != undefined){
		width = popupWidth;
	}
	
	if(iFrame != null && iFrame != "" && iFrame != undefined){
		hiddenForm.target = iFrame;
	}else if(popup != undefined && popup){
		var popwindow = window.open('', 'formpopup', config='height=' + height + ', width=' + width + ', toolbar=no, menubar=no, scrollbars=no, resizable=no, location=no, directories=no, status=no');
    	hiddenForm.target = 'formpopup';
		popwindow.focus();
	}

	hiddenForm.action = '${forwardPayment.paymentURL}';
	var hiddenInput = null;

<%
	final ForwardPayment forwardPayment = (ForwardPayment) request.getAttribute("forwardPayment");
	final TPAVirtualImplementation implementation = (TPAVirtualImplementation) forwardPayment.getForwardPaymentConfiguration().implementation();
	final Map<String, String> requestMap = implementation.mapAuthenticationRequest(forwardPayment);
	for(final Map.Entry<String, String> e : requestMap.entrySet()) {
%>

	hiddenInput = document.createElement("INPUT");
	hiddenInput.type = "hidden";
	hiddenInput.id = "<%= e.getKey() %>";
	hiddenInput.name = "<%= e.getKey() %>";
	hiddenInput.value = "<%= e.getValue() %>";
	hiddenForm.appendChild(hiddenInput);

<% } %>
 
	document.body.appendChild(hiddenForm);
	hiddenForm.submit();
}

$(document).ready(function() {
	submitPaymentByFrame();
});
</script>

<form id="waitingForPaymentURL" action="<%= ForwardPaymentController.WAITING_FOR_PAYMENT_URL %>/${forwardPayment.externalId}">
</form>

<iframe id="netcaixa" name="netcaixa" style="width:500px; height:600px;" scrolling="no" frameborder="0" ></iframe>
