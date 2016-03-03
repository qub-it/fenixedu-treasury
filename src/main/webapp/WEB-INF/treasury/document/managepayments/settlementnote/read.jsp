<%@page import="org.fenixedu.bennu.core.security.Authenticate"%>
<%@page import="org.fenixedu.treasury.domain.accesscontrol.TreasuryAccessControl"%>
<%@page import="org.fenixedu.treasury.domain.FinantialInstitution"%>
<%@page import="org.fenixedu.treasury.domain.document.SettlementNote"%>
<%@page import="org.fenixedu.treasury.ui.integration.erp.ERPExportOperationController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="datatables" uri="http://github.com/dandelion/datatables"%>
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
<link href="${pageContext.request.contextPath}/static/treasury/css/omnis.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>

<%
        SettlementNote settlementNote= (SettlementNote) request
                        .getAttribute("settlementNote");
FinantialInstitution finantialInstitution = (FinantialInstitution) settlementNote.getDebtAccount().getFinantialInstitution();
    %>
    <% 
                if (TreasuryAccessControl.getInstance().isAllowToModifySettlements(Authenticate.getUser(), finantialInstitution)) {
%> 
<div class="modal fade" id="anullModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/document/managepayments/settlementnote/read/${settlementNote.externalId}/anullsettlement"
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
                        <spring:message code="label.document.managePayments.readSettlementNote.confirmAnull" />
                    </p>
                    <br /> <br />
                    <div class="form">
                        <div class="form-group row">
                            <div class="col-sm-4 control-label">
                                <spring:message code="label.SettlementNote.annulledReason" />
                            </div>

                            <div class="col-sm-8">
                                <input id="settlementNote_anullReason" class="form-control" type="text" name="anullReason" required value='' />
                            </div>
                        </div>
                    </div>

                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <spring:message code="label.cancel" />
                    </button>
                    <button id="deleteButton" class="btn btn-danger" type="submit">
                        <spring:message code="label.annull" />
                    </button>
                </div>
            </form>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>

<div class="modal fade" id="closeModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/document/managepayments/settlementnote/read/${settlementNote.externalId}/closesettlementnote"
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
                        <spring:message code="label.document.manageInvoice.readSettlementNote.confirmClose" />
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <spring:message code="label.cancel" />
                    </button>
                    <button id="deleteButton" class="btn btn-primary" type="submit">
                        <spring:message code="label.close" />
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
<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.document.managePayments.readSettlementNote" />
        <small></small>
    </h1>
</div>
<% 
                if (TreasuryAccessControl.getInstance().isAllowToModifySettlements(Authenticate.getUser(), finantialInstitution)) {
%> 
<div class="modal fade" id="deleteModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/document/managepayments/settlementnote/delete/${settlementNote.externalId}" method="POST">
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
                        <spring:message code="label.document.managePayments.readSettlementNote.confirmDelete" />
                    </p>
                    <input id="settlementNote_anullReason" class="form-control" type="hidden" name="anullReason" required value='.' />


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
<%} %>
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-user" aria-hidden="true"></span>&nbsp;<a class=""
        href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${settlementNote.debtAccount.externalId}"><spring:message
            code="label.document.manageInvoice.readDebitEntry.event.backToDebtAccount" /></a> &nbsp;
<% 
                if (TreasuryAccessControl.getInstance().isAllowToModifySettlements(Authenticate.getUser(), finantialInstitution)) {
%>             
            |&nbsp; <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>&nbsp;<a
        class="" href="${pageContext.request.contextPath}/treasury/document/managepayments/settlementnote/update/${settlementNote.externalId}"><spring:message
            code="label.event.update" /></a> &nbsp;
    <c:if test="${settlementNote.isPreparing()}">
        |&nbsp;<span class="glyphicon glyphicon-trash" aria-hidden="true"></span>&nbsp;<a class="" href="#" data-toggle="modal" data-target="#deleteModal"><spring:message
                code="label.event.delete" /></a> &nbsp;|&nbsp; 
        <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
        <a class="" href="#" data-toggle="modal" data-target="#closeModal"> <spring:message code="label.event.document.manageInvoice.closeSettlementNote" />
        </a> &nbsp;
    </c:if>
    <c:if test="${settlementNote.isClosed()}">
        |&nbsp;<span class="glyphicon glyphicon-remove-circle" aria-hidden="true"></span>
        <a class="" href="#" data-toggle="modal" data-target="#anullModal"> <spring:message code="label.event.document.managePayments.anullSettlementNote" />
        </a> &nbsp;      
    </c:if>
    <%} %>
    <c:if test="${not settlementNote.isPreparing()}">
|
            <div class="btn-group">
            <button class="btn btn-default dropdown-toggle" type="button" id="dropdownMenu1" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
                <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;
                <spring:message code="label.event.administration.managefinantialinstitution.finantialinstitution.erpoptions">
                </spring:message>
                <span class="caret"></span>
            </button>
            <ul class="dropdown-menu" aria-labelledby="dropdownMenu1">
                <li><a id="exportCreditNoteIntegrationOnline" class=""
                    href="${pageContext.request.contextPath}/treasury/document/managepayments/settlementnote/read/${settlementNote.externalId}/exportintegrationonline"><span
                        class="glyphicon glyphicon-cog" aria-hidden="true"></span> <spring:message code="label.event.document.manageInvoice.exportCreditNoteIntegrationOnline" /></a></li>
                <li><a class=""
                    href="${pageContext.request.contextPath}/treasury/document/managepayments/settlementnote/read/${settlementNote.externalId}/exportintegrationfile"> <span
                        class="glyphicon glyphicon-export" aria-hidden="true"></span> <spring:message code="label.event.document.manageInvoice.exportIntegrationFile" />
                </a></li>
                <li><a class="" href="${pageContext.request.contextPath}<%= ERPExportOperationController.SEARCH_URL %>?finantialinstitution=${settlementNote.debtAccount.finantialInstitution.externalId}&documentnumber=${settlementNote.uiDocumentNumber}">
                        <span class="glyphicon glyphicon-export" aria-hidden="true"></span> <spring:message code="label.event.document.manageInvoice.searchExportOperations" />
                </a></li>
            </ul>
        </div>
    </c:if>
<!--     |&nbsp; <span class="glyphicon glyphicon-print" aria-hidden="true"></span> &nbsp; <a class="" id="printLabel2" href="#" -->
<%--         onclick="document.getElementById('accordion').style.display = 'none';window.print();document.getElementById('accordion').style.display = 'block';"> <spring:message --%>
<%--             code="label.print" /> --%>
    |&nbsp; <span class="glyphicon glyphicon-print" aria-hidden="true"></span> &nbsp; <a class="" id="printLabel2" target="_blank" href="${pageContext.request.contextPath}/treasury/document/managepayments/settlementnote/read/${settlementNote.externalId}/printdocument">
        <spring:message
            code="label.print" />
    </a> &nbsp;
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
                        <th scope="row" class="col-xs-3"><spring:message code="label.Series.finantialInstitution" /></th>
                        <td><c:out value='${settlementNote.documentNumberSeries.series.finantialInstitution.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.debtAccount" /></th>
                        <td><c:out value='${settlementNote.debtAccount.customer.businessIdentification} - ${settlementNote.debtAccount.customer.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.documentNumber" /></th>
                        <td><c:out value='${settlementNote.uiDocumentNumber}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.state" /></th>
                        <td><c:if test="${settlementNote.isAnnulled()}">
                                <span class="label label-danger">
                            </c:if> <c:if test="${settlementNote.isPreparing() }">
                                <span class="label label-warning">
                            </c:if> <c:if test="${settlementNote.isClosed()}">
                                <span class="label label-primary">
                            </c:if> <c:out value='${settlementNote.state.descriptionI18N.content}' /></span> <c:if test="${not settlementNote.isPreparing() }">
                                <c:if test="${settlementNote.isDocumentToExport() }">
                                    &nbsp;<span class="label label-warning"> <spring:message code="label.FinantialDocument.document.is.pending.to.export" />
                                    </span>
                                </c:if>
                            </c:if></td>
                    </tr>

                    <c:if test="${settlementNote.isAnnulled()}">
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.annulledReason" /></th>
                            <td><c:out value='${settlementNote.annulledReason}' /></td>
                        </tr>
                    </c:if>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.documentDate" /></th>
                        <td><joda:format value="${settlementNote.documentDate}" style="S-" /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.paymentDate" /></th>
                        <td><joda:format value="${settlementNote.paymentDate}" style="S-" /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.originDocumentNumber" /></th>
                        <td><c:out value='${settlementNote.originDocumentNumber}' /></td>
                    </tr>
                    <c:if test="${not empty  settlementNote.documentObservations}">
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.documentObservations" /></th>
                            <td><c:out value='${settlementNote.documentObservations}' /></td>
                        </tr>
                    </c:if>

                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.totalDebitAmount" /></th>
                        <td><c:out value='${settlementNote.currency.getValueFor(settlementNote.totalDebitAmount)}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.totalCreditAmount" /></th>
                        <td><c:out value='${settlementNote.currency.getValueFor(settlementNote.totalCreditAmount)}' /></td>
                    </tr>
                    <tr>
                        <c:if test="${ not empty settlementNote.paymentEntriesSet }">
                            <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.totalPayedAmount" /></th>
                            <td><c:out value='${settlementNote.currency.getValueFor(settlementNote.totalPayedAmount)}' /></td>
                        </c:if>
                        <c:if test="${ not empty settlementNote.reimbursementEntriesSet }">
                            <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.totalReimbursementAmount" /></th>
                            <td><c:out value='${settlementNote.currency.getValueFor(settlementNote.totalReimbursementAmount)}' /></td>
                        </c:if>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Versioning.creator" /></th>
                        <td>[<c:out value='${settlementNote.getVersioningCreator()}' />] <joda:format value="${settlementNote.getVersioningCreationDate()}" style="SS" /></td>
                    </tr>

					<c:if test="${not empty settlementNote.lastExportationDate}">
					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FinantialDocument.lastExportationDate" /></th>
                        <td><joda:format value="${settlementNote.lastExportationDate}" style="SS" /></td>                        
					</tr>
					</c:if>

                </tbody>
            </table>
        </form>
    </div>
</div>

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
            <%--             <datatables:column cssStyle="width:10%"> --%>
            <%--                 <datatables:columnHead> --%>
            <%--                     <spring:message code="label.DebitEntry.vat" /> --%>
            <%--                 </datatables:columnHead> --%>
            <%--                 <c:out value="${settlementEntry.invoiceEntry.vat.taxRate}" /> --%>
            <%--             </datatables:column> --%>
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
            <%--             <datatables:column cssStyle="width:10%"> --%>
            <%--                 <form method="get" action="${pageContext.request.contextPath}/treasury/document/manageinvoice/settlemententry/read/${settlementEntry.externalId}"> --%>
            <!--                     <button type="submit" class="btn btn-default btn-xs"> -->
            <%--                         <spring:message code="label.view" /> --%>
            <!--                     </button> -->
            <!--                 </form> -->
            <%--             </datatables:column> --%>
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
    <c:when test="${not empty settlementNote.reimbursementEntriesSet}">
        <h2>
            <spring:message code="label.SettlementNote.reimbursementEntries" />
        </h2>
        <datatables:table id="reimbursementEntries" row="reimbursementEntry" data="${settlementNote.reimbursementEntriesSet}" cssClass="table responsive table-bordered table-hover"
            cdn="false" cellspacing="2">
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.document.managepayments.settlementnote.ReimbursementMethod" />
                </datatables:columnHead>
                <c:out value="${reimbursementEntry.paymentMethod.name.content}" />
            </datatables:column>
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.PaymentEntry.reimbursementAmount" />
                </datatables:columnHead>
                <c:out value="${settlementNote.currency.getValueFor(reimbursementEntry.reimbursedAmount)}" />
            </datatables:column>
        </datatables:table>
        <script>
									createDataTables(
											'reimbursementEntries',
											false,
											false,
											false,
											"${pageContext.request.contextPath}",
											"${datatablesI18NUrl}");
								</script>
    </c:when>
    <c:otherwise>
        <h2>
            <spring:message code="label.SettlementNote.reimbursementEntries" />
        </h2>
        <div class="alert alert-warning" role="alert">
            <p>
                <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
                <spring:message code="label.noResultsFound" />
            </p>
        </div>

    </c:otherwise>
</c:choose>

<script>
	$(document).ready(function() {

		if(Omnis && Omnis.block) {
			Omnis.block('exportCreditNoteIntegrationOnline');			
		}
		

	});
</script>
