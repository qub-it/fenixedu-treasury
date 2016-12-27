<%@page import="org.fenixedu.treasury.ui.document.forwardpayments.ForwardPaymentController"%>
<%@page import="org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController"%>
<%@page import="org.fenixedu.treasury.ui.integration.erp.ERPExportOperationController"%>
<%@page import="org.fenixedu.bennu.core.security.Authenticate"%>
<%@page import="org.fenixedu.treasury.domain.accesscontrol.TreasuryAccessControl"%>
<%@page import="org.fenixedu.treasury.domain.FinantialInstitution"%>
<%@page import="org.fenixedu.treasury.domain.document.SettlementNote"%>
<%@page import="org.fenixedu.treasury.domain.forwardpayments.ForwardPayment"%>
<%@page
    import="org.fenixedu.treasury.ui.document.managepayments.SettlementNoteController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<%@ taglib prefix="datatables"
    uri="http://github.com/dandelion/datatables"%>

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

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<c:set var="settlementNote" value="${forwardPayment.settlementNote}" /> 

<%
    ForwardPayment forwardPayment = (ForwardPayment) request.getAttribute("forwardPayment");
	FinantialInstitution finantialInstitution = forwardPayment.getDebtAccount().getFinantialInstitution();
%>



<%-- TITLE --%>
<div class="page-header">
    <h1><spring:message code="label.ForwardPaymentController.onlinePayment" /></h1>
    <h1><small><spring:message code="label.ForwardPaymentController.paymentConfirmation" /></small></h1>
    <div>
        <div class="well well-sm">
            <p>
                <strong><spring:message code="label.DebtAccount.finantialInstitution" />: </strong>
                <c:out value="${settlementNote.debtAccount.finantialInstitution.name}" />
            </p>
            <p>
                <strong><spring:message code="label.DebtAccount.customer" />: </strong>
               	<c:out value='${settlementNote.debtAccount.customer.businessIdentification} - ${settlementNote.debtAccount.customer.name}' />
            </p>
            <p>
                <strong><spring:message code="label.Customer.fiscalNumber" />: </strong>
                <c:out value='${settlementNote.debtAccount.customer.fiscalCountry}:${settlementNote.debtAccount.customer.fiscalNumber}' />
            </p>
            <p>
            	<strong><spring:message code="label.Customer.address" />: </strong>
            	<c:out value='${settlementNote.debtAccount.customer.address}' />
            </p>
        </div>
    </div>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
	<span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>
	&nbsp;
	<a class="" href="${pageContext.request.contextPath}${debtAccountUrl}${forwardPayment.debtAccount.externalId}">
		<spring:message code="label.event.back" />
	</a>
	&nbsp;|&nbsp;
    <span class="glyphicon glyphicon-print" aria-hidden="true"></span>&nbsp; 
    <a class="" id="printLabel2" target="_blank" href="${pageContext.request.contextPath}${printSettlementNoteUrl}/${forwardPayment.externalId}">
        <spring:message code="label.print" />
    </a>
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
        3. <spring:message code="label.ForwardPaymentController.enterPaymentDetails" />
        <span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span> 
        <strong>4. <spring:message code="label.ForwardPaymentController.paymentConfirmation" /></strong>
    </p>
</div>

<div class="panel panel-primary">
    <div class="panel-heading">
        <h3 class="panel-title">
            <spring:message code="label.details" />
        </h3>
    </div>
    <div class="panel-body">
		<div class="alert alert-info" role="alert">
			<h5>
				<spring:message code="label.ForwardPayment.forward.payment.concluded.success" />
			</h5>
		</div>
    
        <form method="post" class="form-horizontal">
            <table class="table">
                <tbody>
<% if (TreasuryAccessControl.getInstance().isAllowToModifySettlements(Authenticate.getUser(), finantialInstitution)) { %>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Series.finantialInstitution" /></th>
                        <td><c:out value='${settlementNote.documentNumberSeries.series.finantialInstitution.name}' /></td>
                    </tr>
<% } %>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.debtAccount" /></th>
                        <td><c:out value='${settlementNote.debtAccount.customer.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.documentNumber" /></th>
                        <td><c:out value='${settlementNote.uiDocumentNumber}' /></td>
                    </tr>

                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.documentDate" /></th>
                        <td><joda:format value="${settlementNote.documentDate}" style="S-" /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.paymentDate" /></th>
                        <td><joda:format value="${settlementNote.paymentDate}" style="S-" /></td>
                    </tr>
<% if (TreasuryAccessControl.getInstance().isAllowToModifySettlements(Authenticate.getUser(), finantialInstitution)) { %>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.originDocumentNumber" /></th>
                        <td><c:out value='${settlementNote.originDocumentNumber}' /></td>
                    </tr>
<% } %>

                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.totalDebitAmount" /></th>
                        <td><c:out value='${settlementNote.currency.getValueFor(settlementNote.totalDebitAmount)}' /></td>
                    </tr>

                    <tr>
                        <c:if test="${ not empty settlementNote.paymentEntriesSet }">
                            <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.totalPayedAmount" /></th>
                            <td><c:out value='${settlementNote.currency.getValueFor(settlementNote.totalPayedAmount)}' /></td>
                        </c:if>
                    </tr>
<% if (TreasuryAccessControl.getInstance().isAllowToModifySettlements(Authenticate.getUser(), finantialInstitution)) { %>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Versioning.creator" /></th>
                        <td>[<c:out value='${settlementNote.getVersioningCreator()}' />] <joda:format value="${settlementNote.getVersioningCreationDate()}" style="SS" /></td>
                    </tr>
<% } %>

                </tbody>
            </table>
        </form>
    </div>
</div>

<% if (TreasuryAccessControl.getInstance().isAllowToModifySettlements(Authenticate.getUser(), finantialInstitution)) { %>
<p></p>
<p></p>
<h2>
    <spring:message code="label.SettlementNote.settlementEntries" />
</h2>

<c:choose>
    <c:when test="${not empty settlementNote.finantialDocumentEntriesSet}">
        <datatables:table id="settlementEntries" row="settlementEntry" data="${settlementNote.finantialDocumentEntriesSet}" cssClass="table responsive table-bordered table-hover"
            cdn="false" cellspacing="2">
            <datatables:column cssStyle="width:15%">
                <datatables:columnHead>
                    <spring:message code="label.InvoiceEntry.document" />
                </datatables:columnHead>
                <c:out value="${settlementEntry.invoiceEntry.finantialDocument.uiDocumentNumber}" />
            </datatables:column>
            <datatables:column>
                <datatables:columnHead>
                    <spring:message code="label.SettlementEntry.description" />
                </datatables:columnHead>
                <c:out value="${settlementEntry.description}" />
            </datatables:column>
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.DebitEntry.amount" />
                </datatables:columnHead>
                <c:out value="${settlementNote.currency.getValueFor(settlementEntry.invoiceEntry.totalAmount)}" />
            </datatables:column>

            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.SettlementEntry.amount" />
                </datatables:columnHead>
                <c:out value="${settlementNote.currency.getValueFor(settlementEntry.totalAmount)}" />
            </datatables:column>
            <datatables:column cssStyle="width:1%">
                <c:if test="${settlementEntry.invoiceEntry.isDebitNoteEntry()}">
                    <c:out value=" [D] " />
                </c:if>
                <c:if test="${settlementEntry.invoiceEntry.isCreditNoteEntry()}">
                    <c:out value=" [C] " />
                </c:if>
            </datatables:column>

        </datatables:table>
        <script>
									createDataTables(
											'settlementEntries',
											false,
											false,
											false,
											"${pageContext.request.contextPath}",
											"${datatablesI18NUrl}");
								</script>
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
<% } %>

<p></p>
<p></p>

<c:if test="${not empty settlementNote.advancedPaymentCreditNote}">
    <h2>
        <spring:message code="label.SettlementNote.advancedPaymentCreditNote" />
    </h2>

    <datatables:table id="advancedPaymentEntries" row="advancedPaymentEntry" data="${settlementNote.advancedPaymentCreditNote.creditEntriesSet}"
        cssClass="table responsive table-bordered table-hover" cdn="false" cellspacing="2">
        <datatables:column cssStyle="width:10%">
            <datatables:columnHead>
                <spring:message code="label.InvoiceEntry.document" />
            </datatables:columnHead>
            <c:out value="${advancedPaymentEntry.finantialDocument.uiDocumentNumber}" />
        </datatables:column>
        <datatables:column>
            <datatables:columnHead>
                <spring:message code="label.SettlementEntry.description" />
            </datatables:columnHead>
            <c:out value="${advancedPaymentEntry.description}" />
        </datatables:column>
        <datatables:column cssStyle="width:10%">
            <datatables:columnHead>
                <spring:message code="label.DebitEntry.amount" />
            </datatables:columnHead>
            <c:out value="${settlementNote.currency.getValueFor(advancedPaymentEntry.totalAmount)}" />
        </datatables:column>
        <datatables:column cssStyle="width:10%">
            <datatables:columnHead>
                <spring:message code="label.SettlementEntry.amount" />
            </datatables:columnHead>
            <c:out value="${settlementNote.currency.getValueFor(advancedPaymentEntry.totalAmount)}" />
        </datatables:column>
        <datatables:column cssStyle="width:1%">
            <c:out value=" [C] " />
        </datatables:column>
    </datatables:table>
    <script>
					createDataTables('advancedPaymentEntries', false, false,
							false, "${pageContext.request.contextPath}",
							"${datatablesI18NUrl}");
				</script>
</c:if>

<p></p>
<p></p>

<% if (TreasuryAccessControl.getInstance().isAllowToModifySettlements(Authenticate.getUser(), finantialInstitution)) { %>
<c:choose>
    <c:when test="${not empty settlementNote.paymentEntriesSet}">
        <h2>
            <spring:message code="label.SettlementNote.paymentEntries" />
        </h2>
        <datatables:table id="paymentEntries" row="payemntEntry" data="${settlementNote.paymentEntriesSet}" cssClass="table responsive table-bordered table-hover" cdn="false"
            cellspacing="2">
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.document.managepayments.settlementnote.PaymentMethod" />
                </datatables:columnHead>
                <c:out value="${payemntEntry.paymentMethod.name.content}" />
            </datatables:column>
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.PaymentEntry.payedAmount" />
                </datatables:columnHead>
                <c:out value="${settlementNote.currency.getValueFor(payemntEntry.payedAmount)}" />
            </datatables:column>
        </datatables:table>
        <script>
			createDataTables(
					'paymentEntries',
					false,
					false,
					false,
					"${pageContext.request.contextPath}",
					"${datatablesI18NUrl}");
		</script>
    </c:when>
</c:choose>
<% } %>

<jsp:include page="${logosPage}" />