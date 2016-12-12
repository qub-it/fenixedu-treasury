<%@page import="org.fenixedu.treasury.ui.document.manageinvoice.CreditNoteController"%>
<%@page import="org.fenixedu.bennu.core.security.Authenticate"%>
<%@page import="org.fenixedu.treasury.domain.accesscontrol.TreasuryAccessControl"%>
<%@page import="org.fenixedu.treasury.domain.FinantialInstitution"%>
<%@page import="org.fenixedu.treasury.domain.document.DebitEntry"%>
<%@page import="org.fenixedu.treasury.ui.document.manageinvoice.DebitNoteController"%>
<%@page import="org.fenixedu.treasury.ui.administration.payments.sibs.managepaymentreferencecode.PaymentReferenceCodeController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>

<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
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
        <spring:message code="label.document.manageInvoice.readDebitEntry" />
        <small></small>
    </h1>
</div>

<%
DebitEntry debitEntry = (DebitEntry) request.getAttribute("debitEntry");
FinantialInstitution finantialInstitution = (FinantialInstitution) debitEntry.getDebtAccount().getFinantialInstitution();
%>
    
<% 
	if (TreasuryAccessControl.getInstance().isAllowToModifyInvoices(Authenticate.getUser(), finantialInstitution)) {
%>  
<div class="modal fade" id="deleteModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitentry/delete/${debitEntry.externalId}" method="POST">
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
                        <spring:message code="label.document.manageInvoice.readDebitEntry.confirmDelete" />
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

<!-- /RemoveFromDocument modal -->
<div class="modal fade" id="removeFromDocumentModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitentry/read/${debitEntry.externalId}/removefromdocument"
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
                        <spring:message code="label.document.manageInvoice.readDebitEntry.confirmRemoveFromDocument" />
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <spring:message code="label.close" />
                    </button>
                    <button id="deleteButton" class="btn btn-danger" type="submit">
                        <spring:message code="label.event.delete" />
                    </button>
                </div>
            </form>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->

<!-- /AnnulFromDocument modal -->
<div class="modal fade" id="annulDebitEntryModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitentry/read/${debitEntry.externalId}/annuldebitentry"
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
                    <p><spring:message code="label.document.manageInvoice.readDebitEntry.confirmAnnulDebitEntry" /></p>
                    <p>&nbsp;</p>
                    
		            <div class="form-group row">
		                <div class="col-sm-2 control-label">
		                    <spring:message code="label.DebitEntry.annul.debit.entry.reason" />
		                </div>
		
		                <div class="col-sm-10">
		                    <input id="" class="form-control" type="text" name="annulDebitEntryReason" />
		                </div>
		            </div>
                    
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <spring:message code="label.close" />
                    </button>
                    <button id="annulButton" class="btn btn-danger" type="submit">
                        <spring:message code="label.annul" />
                    </button>
                </div>
            </form>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->



<%} %>
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <c:if test="${not empty debitEntry.finantialDocument}">
        <span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span>&nbsp;<a class=""
            href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${debitEntry.finantialDocument.externalId}"><spring:message
                code="label.document.manageInvoice.event.backToDebitNote" /></a>&nbsp|&nbsp; 
	</c:if>
    <span class="glyphicon glyphicon-user" aria-hidden="true"></span>&nbsp;<a class=""
        href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${debitEntry.debtAccount.externalId}"><spring:message
            code="label.document.manageInvoice.readDebitEntry.event.backToDebtAccount" /></a> &nbsp;
<% 
                if (TreasuryAccessControl.getInstance().isAllowToModifyInvoices(Authenticate.getUser(), finantialInstitution)) {
%>          
 
    <c:if test="${empty debitEntry.finantialDocument}">
        |&nbsp;<span class="glyphicon glyphicon-trash" aria-hidden="true"></span>&nbsp;
        <a class="" href="#" data-toggle="modal" data-target="#annulDebitEntryModal">
        	<spring:message code="label.annul" />
        </a>&nbsp;
    </c:if>

    <c:if test="${empty debitEntry.finantialDocument || not debitEntry.finantialDocument.isAnnulled()}">
        |&nbsp;<span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>&nbsp;
        <a class="" href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitentry/update/${debitEntry.externalId}">
        	<spring:message code="label.event.update" />
        </a>
		&nbsp;
	</c:if>

    <c:if test="${not empty debitEntry.finantialDocument && debitEntry.finantialDocument.isPreparing()}">
        |&nbsp;<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a class="" href="#" data-toggle="modal" data-target="#removeFromDocumentModal"><spring:message
                code="label.event.document.manageinvoice.debitentry.removefromdocument" /></a>
    	&nbsp;
    </c:if>


<%} %>
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
                        <th scope="row" class="col-xs-3"><spring:message code="label.InvoiceEntry.debtAccount" /></th>
                        <td><c:out value='${debitEntry.debtAccount.customer.businessIdentification} - ${debitEntry.debtAccount.customer.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialDocumentEntry.finantialDocument" /></th>
                        <td><c:if test="${not empty debitEntry.finantialDocument}">
                                <c:out value='${debitEntry.finantialDocument.uiDocumentNumber}' />
                            </c:if> <c:if test="${empty debitEntry.finantialDocument}">
                                <span class="label label-warning"> <spring:message code="label.DebitEntry.debitentry.with.no.document" />
                                </span> &nbsp;
<% 
                if (TreasuryAccessControl.getInstance().isAllowToModifyInvoices(Authenticate.getUser(), finantialInstitution)) {
%>
                                <a class="btn btn-xs btn-primary" href="${pageContext.request.contextPath}<%=DebitNoteController.CREATE_URL%>?debitEntry=${debitEntry.externalId}"><span
                                    class="glyphicon glyphicon-plus-sign" aria-hidden="true"></span>&nbsp;<spring:message code="label.DebitEntry.create.debitNote" /></a>
<%} %>                                    
                            </c:if></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialDocumentEntry.entryDate" /></th>
                        <td><joda:format value="${debitEntry.entryDateTime}" style="S-" /> <%--                         <c:out value='${debitEntry.entryDateTime}' /> --%></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebitEntry.dueDate" /></th>
                        <td><joda:format value="${debitEntry.dueDate}" style="S-" /> <%--                         <c:out value='${debitEntry.dueDate}' /> --%></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.InvoiceEntry.description" /></th>
                        <td><c:out value='${debitEntry.product.code} - ${debitEntry.description}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.InvoiceEntry.quantity" /></th>
                        <td><c:out value='${debitEntry.quantity}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebitEntry.amount" /></th>
                        <td><c:out value='${debitEntry.currency.getValueFor(debitEntry.amount)}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebitEntry.vat" /></th>
                        <td><c:out value='${debitEntry.vat.taxRate} % ' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebitEntry.totalAmount" /></th>
                        <td><c:out value='${debitEntry.currency.getValueFor(debitEntry.totalAmount)}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebitEntry.openAmount" /></th>
                        <td><c:out value='${debitEntry.currency.getValueFor(debitEntry.openAmountWithInterests)}' /></td>
                    </tr>
                    <c:if test="${debitEntry.pendingInterestAmount.unscaledValue() != 0 }">
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.DebitEntry.pendingInterestAmount" /></th>
                            <td><c:out value='${debitEntry.currency.getValueFor(debitEntry.pendingInterestAmount)}' /> <span class="label label-info"><spring:message
                                        code="label.DebtAccount.interestIncludedInDebtAmount" /></span></td>
                        </tr>
                    </c:if>

                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebitEntry.exemptedAmount" /></th>
                        <td><c:out value='${debitEntry.currency.getValueFor(debitEntry.exemptedAmount)}' /></td>
                    </tr>

                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Versioning.creator" /></th>
                        <td>[<c:out value='${debitEntry.getVersioningCreator()}' />] <joda:format value="${debitEntry.getVersioningCreationDate()}" style="SS" /></td>
                    </tr>


                    <c:if test='${ not empty debitEntry.interestRate}'>
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.InterestRate.interestType" /></th>
                            <td><c:out value='${debitEntry.interestRate.uiFullDescription}' /></td>
                        </tr>
                    </c:if>
                    <c:if test='${empty debitEntry.interestRate}'>
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.InterestRate.interestType" /></th>
                            <td><spring:message code="label.DebitEntry.no.interest.rate.applies" /></td>
                        </tr>
                    </c:if>
                    
                                        <c:if test="${not empty debitEntry.creditEntries}">
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.relatedCreditEntries" /></th>
                            <td>
                                <ul>

                                    <c:forEach var="creditEntry" items="${debitEntry.creditEntries}">
                                        <li><c:out value='${creditEntry.entryDateTime.toString("YYYY-MM-dd")} => '/><a target="_blank"
                                            href="${pageContext.request.contextPath}<%=CreditNoteController.READ_URL %>${creditEntry.finantialDocument.externalId}"><c:out
                                                    value='${creditEntry.finantialDocument.uiDocumentNumber}' /></a> <c:out
                                                value=' [${ creditEntry.finantialDocument.debtAccount.finantialInstitution.currency.getValueFor(creditEntry.amount)}]' /></li>
                                    </c:forEach>
                                </ul>
                            </td>
                        </tr>
                    </c:if>
                    
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.DebitEntry.paymentCodes" /></th>
                            <td>

                                <ul>
                                    <c:forEach var="paymentcode" items="${debitEntry.paymentCodesSet}">
                                        <li><a target="#"
                                            href="${pageContext.request.contextPath}<%=PaymentReferenceCodeController.READ_URL %>${paymentcode.paymentReferenceCode.externalId}">
                                                <c:out
                                                    value="[${paymentcode.paymentReferenceCode.paymentCodePool.entityReferenceCode}] ${paymentcode.paymentReferenceCode.formattedCode}" />
                                        </a> &nbsp; <c:if test="${paymentcode.paymentReferenceCode.state=='USED'}">
                                                <span class="label label-primary">
                                            </c:if> <c:if test="${paymentcode.paymentReferenceCode.state=='ANNULLED'}">
                                                <span class="label label-danger">
                                            </c:if> <c:if test="${paymentcode.paymentReferenceCode.state=='UNUSED'}">
                                                <span class="label label-default">
                                            </c:if> <c:if test="${paymentcode.paymentReferenceCode.state=='PROCESSED'}">
                                                <span class="label label-success">
                                            </c:if> <c:out value="${paymentcode.paymentReferenceCode.state.descriptionI18N.content}" /> </span></li>
                                    </c:forEach>
                                </ul>
                            </td>
                        </tr>
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.DebitEntry.academicalActBlockingOff" /></th>
                            <td>
								<c:if test="${debitEntry.academicalActBlockingSuspension}">
									<spring:message code="label.true" />
								</c:if>
								<c:if test="${not debitEntry.academicalActBlockingSuspension}">
									<spring:message code="label.false" />
								</c:if>
                            </td>
                        </tr>
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.DebitEntry.blockAcademicActsOnDebt" /></th>
                            <td>
								<c:if test="${debitEntry.blockAcademicActsOnDebt}">
									<spring:message code="label.true" />
								</c:if>
								<c:if test="${not debitEntry.blockAcademicActsOnDebt}">
									<spring:message code="label.false" />
								</c:if>
                            </td>
                        </tr>
                </tbody>
            </table>

        </form>
    </div>

</div>

<c:if test="${ not empty debitEntry.propertiesMap }">
    <table id="treasuryEventTableMap" class="table responsive table-bordered table-hover" width="100%">

        <c:forEach var="property" items="${debitEntry.propertiesMap}">
            <tr>
                <th><c:out value="${property.key}" /></th>
                <td><c:out value="${property.value}" /></td>
            </tr>
        </c:forEach>
    </table>
</c:if>

<script>
	$(document).ready(function() {

	});
</script>
