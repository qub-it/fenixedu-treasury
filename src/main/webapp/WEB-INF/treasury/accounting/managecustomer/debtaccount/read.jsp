<%@page import="org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController"%>
<%@page import="org.fenixedu.treasury.ui.document.forwardpayments.ManageForwardPaymentsController"%>
<%@page import="org.fenixedu.treasury.domain.forwardpayments.ForwardPaymentConfiguration"%>
<%@page import="org.fenixedu.treasury.ui.accounting.managecustomer.PaymentReferenceCodeController"%>
<%@page import="org.fenixedu.bennu.core.security.Authenticate"%>
<%@page import="org.fenixedu.treasury.domain.accesscontrol.TreasuryAccessControl"%>
<%@page import="org.fenixedu.treasury.domain.FinantialInstitution"%>
<%@page import="org.fenixedu.treasury.domain.debt.DebtAccount"%>
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
${portal.angularToolkit()}
<%--${portal.toolkit()}--%>

<link href="${pageContext.request.contextPath}/static/treasury/css/dataTables.responsive.css" rel="stylesheet" />
<link href="${pageContext.request.contextPath}/static/treasury/css/dropdown.multi.level.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/static/treasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<link href="${pageContext.request.contextPath}/static/treasury/css/omnis.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>

<script src="${pageContext.request.contextPath}/webjars/angular-sanitize/1.3.11/angular-sanitize.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.css" />
<script src="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.js"></script>

<style type="text/css">

.my-table-option .input-group-addon {
	border: 0px solid #ccc;
	border-radius: 0px;
}

.my-table-option .input-group .form-control {
	border: 0px solid #ccc;
	border-radius: 0px;
}

</style>

<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.accounting.manageCustomer.readDebtAccount" />
        <small></small>
    </h1>
</div>

<%
    DebtAccount debtAccount = (DebtAccount) request
					.getAttribute("debtAccount");
			FinantialInstitution finantialInstitution = (FinantialInstitution) debtAccount
					.getFinantialInstitution();
%>
<div class="modal fade" id="deleteModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/delete/${debtAccount.externalId}" method="POST">
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
                        <spring:message code="label.accounting.manageCustomer.readDebtAccount.confirmDelete" />
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
<!-- /.modal -->

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;
	<c:if test='${debtAccount.customer.active}'>
	    <a href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/customer/read/${debtAccount.customer.externalId}">
	    	<spring:message code="label.event.back" />
	    </a>
   	</c:if>
	<c:if test='${not debtAccount.customer.active}'>
	    <a href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/customer/read/${debtAccount.customer.personForInactivePersonCustomer.personCustomer.externalId}">
	    	<spring:message code="label.event.back" />
	    </a>
   	</c:if>
    &nbsp;

    <%
        if (TreasuryAccessControl.getInstance().isAllowToModifySettlements(
    					Authenticate.getUser(), finantialInstitution)) {
    %>
    <div class="btn-group">
        <button type="button" class=" btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
            <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;
            <spring:message code="label.event.accounting.manageCustomer.payments" />
            <span class="caret"></span>
        </button>
        <ul class="dropdown-menu">

            <li><a class="" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${debtAccount.externalId}/createpayment"><span
                    class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<spring:message code="label.event.accounting.manageCustomer.createPayment" /></a></li>
			
			<% if(ForwardPaymentConfiguration.isActive(debtAccount.getFinantialInstitution())) { %>
			
            <li><a class="" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${debtAccount.externalId}/forwardpayment"><span
                    class="glyphicon glyphicon-shopping-cart" aria-hidden="true"></span>&nbsp;<spring:message code="label.event.accounting.manageCustomer.forwardPayment" /></a></li>

			<% } %>

            <li><a class="" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${debtAccount.externalId}/createreimbursement"><span
                    class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<spring:message code="label.event.accounting.manageCustomer.createReimbursement" /></a></li>
        </ul>
    </div>
    <%
        }
    %>
    <c:if test='${not debtAccount.getClosed()}'>
        <%
            if (TreasuryAccessControl.getInstance().isAllowToModifyInvoices(Authenticate.getUser(), finantialInstitution) && debtAccount.getCustomer().isActive()) {
        %>
        <div class="btn-group">
            <button type="button" class=" btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;
                <spring:message code="label.event.accounting.manageCustomer.debits" />
                <span class="caret"></span>
            </button>
            <ul class="dropdown-menu">
                <li><a class="" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${debtAccount.externalId}/createdebtentry"><span
                        class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<spring:message code="label.event.accounting.manageCustomer.createDebtEntry" /></a></li>
                <li><a class="" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${debtAccount.externalId}/createdebitnote"><span
                        class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<spring:message code="label.event.accounting.manageCustomer.createDebitNote" /></a></li>
                <li class="dropdown-submenu"><a class="" href="#"> <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp; <spring:message
                            code="label.event.accounting.manageCustomer.createDebt" />
                </a>
                    <ul class="dropdown-menu">
                        <li><a href="${pageContext.request.contextPath}/academictreasury/othertuitiondebtcreation/tuitiondebtcreationbean/createregistration/${debtAccount.externalId}"> <span
                                class="glyphicon glyphicon-bookmark" aria-hidden="true"></span> <spring:message code="label.TuitionDebtCreationBean.create.tuition.debts" />
                        </a></li>

                        <li><a
                            href="${pageContext.request.contextPath}/academictreasury/othertuitiondebtcreation/tuitiondebtcreationbean/createstandalone/${debtAccount.externalId}">
                                <span class="glyphicon glyphicon-bookmark" aria-hidden="true"></span>
                                &nbsp;<spring:message code="label.TuitionDebtCreationBean.create.standalonetuition.debts" />
                        </a></li>

                        <li><a
                            href="${pageContext.request.contextPath}/academictreasury/othertuitiondebtcreation/tuitiondebtcreationbean/createextracurricular/${debtAccount.externalId}">
                                <span class="glyphicon glyphicon-bookmark" aria-hidden="true"></span>
                                &nbsp;<spring:message code="label.TuitionDebtCreationBean.create.extracurriculartuition.debts" />
                        </a></li>

                        <li><a href="${pageContext.request.contextPath}/academictreasury/academictaxdebtcreation/academictaxdebtcreationbean/create/${debtAccount.externalId}">
                                <span class="glyphicon glyphicon-book" aria-hidden="true"></span>
                                &nbsp;<spring:message code="label.AcademicTaxDebtCreationBean.create.academictax.debts" />
                        </a></li>

                        <li><a
                            href="${pageContext.request.contextPath}/academictreasury/academicservicerequestdebtcreation/academicservicerequestdebtcreationbean/create/${debtAccount.externalId}">
                                <span class="glyphicon glyphicon-book" aria-hidden="true"></span>
                                &nbsp;<spring:message code="label.AcademicServiceRequestDebtCreationBean.create.academicservicerequest.debts" />
                        </a></li>
                        
                        <li><a
                            href="${pageContext.request.contextPath}/academictreasury/academicdebtgenerationregistration/academicdebtgenerationregistration/create/${debtAccount.externalId}">
                                <span class="glyphicon glyphicon-book" aria-hidden="true"></span>
                                &nbsp;<spring:message code="label.AcademicDebtGenerationRegistration.run.rules" />
                        </a></li>
                        
                        
                    </ul>
                    </li>
                    
                    <li>
						<a class="" href="${pageContext.request.contextPath}<%= PaymentReferenceCodeController.CREATEPAYMENTCODEFORSEVERALDEBITENTRIES_URL %>/${debtAccount.externalId}"><span
			                    class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<spring:message code="label.event.accounting.manageCustomer.createPaymentCodeReference.several.debit.entries" /></a>
                    </li>
            </ul>
        </div>
        <%
            }
        %>

    </c:if>
    <c:if test='${debtAccount.getClosed() }'>
     |&nbsp;
     </c:if>
    <%
        if (TreasuryAccessControl.getInstance().isBackOfficeMember(Authenticate.getUser(), finantialInstitution)) {
    %>

    <div class="btn-group">
        <button type="button" class=" btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
            <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;
            <spring:message code="label.event.accounting.manageCustomer.extraOptions" />
            <span class="caret"></span>
        </button>

        <ul class="dropdown-menu">
            <li><a class="" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${debtAccount.externalId}/readevent"> <span
                    class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp; <spring:message code="label.event.accounting.manageCustomer.readEvent" />
            </a></li>
            <c:if test="${debtAccount.customer.isPersonCustomer() }">
                <li><a class=""
                    href="${pageContext.request.contextPath}/academictreasury/manageacademicactblockingsuspension/academicactblockingsuspension/search/${debtAccount.externalId}">
                        <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp; <spring:message code="label.AcademicActBlockingSuspensionController.link" />
                </a></li>
            </c:if>
            <c:if test="${validAddress}">
            <li>
            	<a id="exportintegrationline" class="" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${debtAccount.externalId}/exportintegrationonline">
            	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;
            	<spring:message code="label.event.accounting.manageCustomer.exportintegrationline" />
            	</a>
            </li>
            </c:if>
            
			<% if(ForwardPaymentConfiguration.isActive(debtAccount.getFinantialInstitution())) { %>
            <li>
            	<a id="exportintegrationline"
            		href="${pageContext.request.contextPath}<%= ManageForwardPaymentsController.SEARCH_URL %>?customerBusinessId=${debtAccount.customer.businessIdentification}">
            		<span class="glyphicon glyphicon-shopping-cart" aria-hidden="true" target="_blank"></span>&nbsp;
            		<spring:message code="label.ManageForwardPayments.search" />
               	</a>
            </li>
			<% } %>
			
			<% if(TreasuryAccessControl.getInstance().isManager(Authenticate.getUser())) { %>
            <li>
            	<a href="${pageContext.request.contextPath}/academictreasury/erptuitioninfo/create/${debtAccount.customer.externalId}">
            		<span class="glyphicon glyphicon-upload" aria-hidden="true" target="_blank"></span>&nbsp;
            		<spring:message code="label.ERPTuitionInfo.create" />
               	</a>
            </li>
			<% } %>
        </ul>
    </div>
    <%
        }
    %>
    <%
        if (TreasuryAccessControl.getInstance().isManager(Authenticate.getUser())) {
    %>

 |&nbsp; <span class="glyphicon glyphicon-print" aria-hidden="true"></span> &nbsp; <a class="" id="printLabel2" target="_blank" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${debtAccount.externalId}/printpaymentplan">
        <spring:message
            code="label.event.accounting.manageCustomer.printpaymentplan" />
    </a> &nbsp;
    <%
        }
    %>

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
                    <c:if test='${ debtAccount.getClosed() }'>
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.DebtAccount.closed" /></th>
                            <td><span class="label label-warning"><spring:message code="warning.DebtAccount.is.closed" /></span></td>
                        </tr>
                    </c:if>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.fiscalNumber" /></th>
                        <td><c:out value='${debtAccount.customer.uiFiscalNumber}' /></td>
                    </tr>

                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebtAccount.customer" /></th>
                        <td><c:out value='${debtAccount.customer.businessIdentification}' /> - <c:out value='${debtAccount.customer.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebtAccount.finantialInstitution" /></th>
                        <td><c:out value='${debtAccount.finantialInstitution.name}' /></td>
                    </tr>
                    <c:if test="${debtAccount.customer.personCustomer}">
                   	<c:if test="${not empty debtAccount.customer.person.inactivePersonCustomers || debtAccount.customer.personForInactivePersonCustomer != null}">
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebtAccount.globalBalance" /></th>
                        <td>
                        	<c:out value="${debtAccount.finantialInstitution.currency.getValueFor(debtAccount.customer.globalBalance)}" />
                        </td>
                   	</c:if>
                   	</c:if>
                    <tr>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebtAccount.balance" /></th>
                        <td><c:out value="${debtAccount.finantialInstitution.currency.getValueFor(debtAccount.totalInDebt + debtAccount.calculatePendingInterestAmount())}" />
                            <c:if test='${ debtAccount.calculatePendingInterestAmount() > 0}'>
                                    &nbsp;&nbsp; &nbsp;   (<spring:message code="label.DebtAccount.balanceWithoutInterests" />
                                <c:out value="${debtAccount.finantialInstitution.currency.getValueFor(debtAccount.totalInDebt)}" /> )
                                </c:if> <c:if test="${debtAccount.totalInDebt < 0 }">
                                <span class="label label-warning"> <spring:message code="label.DebtAccount.customerHasAmountToRehimburse" />
                                </span>
                            </c:if></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebtAccount.pendingInterestAmount" /></th>
                        <td><c:out value="${debtAccount.finantialInstitution.currency.getValueFor(debtAccount.calculatePendingInterestAmount())}" /> <c:if
                                test='${ debtAccount.calculatePendingInterestAmount() > 0}'>
                                <span class="label label-info"><spring:message code="label.DebtAccount.interestIncludedInDebtAmount" /></span>
                            </c:if></td>
                    </tr>
                </tbody>
            </table>
        </form>
    </div>
</div>

<c:if test="${invalidFiscalCode}">
	<div class="alert alert-danger" role="alert">
	    <p>
	    	<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
			<spring:message code="label.DebtAccountController.invalidFiscalCode" />
		</p>
	</div>
</c:if>

<c:if test="${not validAddress}">
	<div class="alert alert-danger" role="alert">
	    <p>
	    	<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
			<spring:message code="label.DebtAccountController.incompleteAddress" />
		</p>
		
	<c:forEach items="${addressErrorMessages}" var="m">
		<p>
	    	<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
			<c:out value="${m}" />
		</p>
	</c:forEach>
	</div>	
</c:if>

<h2>
    <spring:message code="label.DebtAccount" />
</h2>

<c:if test="${!debtAccount.customer.active}">
    <div class="alert alert-warning">
    	<span class="glyphicon glyphicon-exclamation-sign"></span>
    	<strong><spring:message code="warning.Customer.is.inactive.due.merge.or.fiscal.change.message" /></strong>
   	</div>
</c:if>

<div id="content">
    <ul id="tabs" class="nav nav-tabs" data-tabs="tabs">

        <li class="active"><a href="#pending" data-toggle="tab"><spring:message code="label.DebtAccount.pendingDocumentEntries" /></a></li>
        <li><a href="#details" data-toggle="tab"><spring:message code="label.DebtAccount.allDocumentEntries" /></a></li>
        <li><a href="#payments" data-toggle="tab"><spring:message code="label.DebtAccount.payments" /></a></li>
        <li><a href="#paymentReferenceCodes" data-toggle="tab"><spring:message code="label.DebtAccount.paymentReferenceCodes" /></a></li>
    </ul>
    
    <div id="my-tab-content" class="tab-content">
        <div class="tab-pane active" id="pending">
            <!--             <h3>Docs. Pendentes</h3> -->
            <p></p>
            <c:choose>
                <c:when test="${not empty pendingDocumentsDataSet}">
                    <datatables:table id="pendingDocuments" row="pendingEntry" data="${pendingDocumentsDataSet}" cssClass="table table-bordered table-hover" cdn="false"
                        cellspacing="2" sort="false">
                        <datatables:column cssStyle="width:80px;align:right">
                            <datatables:columnHead>
                                <spring:message code="label.InvoiceEntry.date" />
                            </datatables:columnHead>
                            <c:out value='${pendingEntry.entryDateTime.toString("YYYY-MM-dd")}' />
                            <%--                             <joda:format value="${pendingEntry.entryDateTime}" style="S-" /> --%>
                        </datatables:column>
                        <datatables:column cssStyle="width:80px;align:right">
                            <datatables:columnHead>
                                <spring:message code="label.DebitNote.dueDate" />
                            </datatables:columnHead>
                            <c:out value='${pendingEntry.dueDate.toString("YYYY-MM-dd")}' />
                            <%--                             <joda:format value="${pendingEntry.entryDateTime}" style="S-" /> --%>
                        </datatables:column>

                        <datatables:column cssStyle="width:100px;">
                            <datatables:columnHead>
                                <spring:message code="label.InvoiceEntry.finantialDocument" />
                            </datatables:columnHead>
                            <c:if test="${not empty pendingEntry.finantialDocument }">
                                <c:if test="${pendingEntry.isDebitNoteEntry() }">
                                    <a target="_blank"
                                        href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${pendingEntry.finantialDocument.externalId}"> <c:out
                                            value="${pendingEntry.finantialDocument.uiDocumentNumber}" />
                                    </a>
                                    
									<c:if test="${pendingEntry.finantialDocument.isAnnulled()}">
	                                <p><span class="label label-danger"><c:out value='${pendingEntry.finantialDocument.state.descriptionI18N.content}' /></span>
		                            </c:if>
                                                                
                                </c:if>
                                <c:if test="${pendingEntry.isCreditNoteEntry() }">
                                    <a target="_blank"
                                        href="${pageContext.request.contextPath}/treasury/document/manageinvoice/creditnote/read/${pendingEntry.finantialDocument.externalId}">
                                        <c:out value="${pendingEntry.finantialDocument.uiDocumentNumber}" />
                                    </a>
                                </c:if>
                            </c:if>
                            <c:if test="${empty pendingEntry.finantialDocument }">
							---
							</c:if>
                        </datatables:column>
                        <datatables:column>
                            <datatables:columnHead>
                                <spring:message code="label.InvoiceEntry.description" />
                                
                            </datatables:columnHead>
                            <p>
                            	<c:out value="${pendingEntry.description}" />
                            </p>
                        	<c:if test="${pendingEntry.finantialDocument != null}">
                        	<c:if test="${pendingEntry.finantialDocument.forPayorDebtAccount}">
                        		<p>
                        			<em>
                        				<strong><spring:message code="label.Invoice.payorDebtAccount" />:</strong> 
                        				<span><c:out value="${pendingEntry.finantialDocument.payorDebtAccount.customer.fiscalNumber}" /></span>
                        				&nbsp;-&nbsp;
                        				<span><c:out value="${pendingEntry.finantialDocument.payorDebtAccount.customer.name}" /></span>
                        			</em>
                        		</p>
                        	</c:if>
                        	</c:if>
                            
							<c:if test="${pendingEntry.isDebitNoteEntry() && pendingEntry.academicalActBlockingSuspension }">
							<p><span class="label label-warning"><spring:message code="label.DebitEntry.academicalActBlockingOff" /></span></p>
							</c:if>
							<c:if test="${pendingEntry.isDebitNoteEntry() && pendingEntry.blockAcademicActsOnDebt }">
							<p><span class="label label-warning"><spring:message code="label.DebitEntry.blockAcademicActsOnDebt" /></span></p>
							</c:if>
                        </datatables:column>
                        <datatables:column cssStyle="width:10%;align:right">
                            <datatables:columnHead>
                                <spring:message code="label.InvoiceEntry.totalAmount" />
                            </datatables:columnHead>
                            <div align=right>
                                <c:if test="${pendingEntry.isCreditNoteEntry() }">-</c:if>
                                <c:out value="${pendingEntry.debtAccount.finantialInstitution.currency.getValueFor(pendingEntry.totalAmount)}" />
                            </div>
                        </datatables:column>
                        <datatables:column cssStyle="width:10%;align:right">
                            <datatables:columnHead>
                                <spring:message code="label.InvoiceEntry.openAmount" />
                            </datatables:columnHead>
                            <div align=right>
                                <c:if test="${pendingEntry.isCreditNoteEntry() }">-</c:if>
                                <c:out value="${pendingEntry.debtAccount.finantialInstitution.currency.getValueFor(pendingEntry.openAmountWithInterests)}" />
                                <c:if test="${not (pendingEntry.getOpenAmountWithInterests().compareTo(pendingEntry.getOpenAmount()) == 0) }">(*)</c:if>
                            </div>
                        </datatables:column>
                        <datatables:column>
                            <c:if test="${pendingEntry.isDebitNoteEntry() }">
                                                        <c:if test="${empty pendingEntry.finantialDocument }">
                            
                                <a class="btn btn-default btn-xs"
                                    href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitentry/read/${pendingEntry.externalId}">
                                    
                                    </c:if>
                                                                <c:if test="${not empty pendingEntry.finantialDocument }">
                                <a class="btn btn-default btn-xs"
                                    href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${pendingEntry.finantialDocument.externalId}">
                                    
                                    </c:if>
                            </c:if>
                            <c:if test="${pendingEntry.isCreditNoteEntry() }">
                             <c:if test="${empty pendingEntry.finantialDocument }">
                                <a class="btn btn-default btn-xs"
                                    href="${pageContext.request.contextPath}/treasury/document/manageinvoice/creditentry/read/${pendingEntry.externalId}">
                                      </c:if>
                                                                <c:if test="${not empty pendingEntry.finantialDocument }">
                                                                <a class="btn btn-default btn-xs"
                                    href="${pageContext.request.contextPath}/treasury/document/manageinvoice/creditnote/read/${pendingEntry.finantialDocument.externalId}">
                                    
                                                                </c:if>
                            </c:if>
                            <spring:message code="label.view" />
                            </a>
                        </datatables:column>
                    </datatables:table>
                    <script>
						createDataTables(
								'pendingDocuments',
								true,
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
        </div>
        <div class="tab-pane" id="details">
            <!--             <h3>Extracto</h3> -->
            <p></p>
            <c:choose>
                <c:when test="${not empty allDocumentsDataSet}">
                
					<div class="my-table-option row">
					  <div class="col-xs-12">
					    <div class="input-group">
					      <span class="input-group-addon">
					        <input id="includeAnnuledCheckbox" type="checkbox" aria-label="...">
					      </span>
					      <input type="text" class="form-control" aria-label="..." value="<spring:message code="label.DebtAccountController.show.annuled" />" disabled />
					    </div>
					  </div>
					</div>
					<div class="my-table-option row">
					  <div class="col-xs-12">
					    <div class="input-group">
					      <span class="input-group-addon">
					        <input id="filterSettledItemsWithoutPayments" type="checkbox" aria-label="..." >
					      </span>
					      <input type="text" class="form-control" aria-label="..." value="Exluir items liquidados sem pagamentos" disabled />
					    </div>
					  </div>
					</div>
                	
                    <datatables:table id="allDocuments" row="entry" data="${allDocumentsDataSet}" cssClass="table table-bordered table-hover" cdn="false" cellspacing="2">
	                   <datatables:column><c:out value="${not empty entry.finantialDocument && entry.finantialDocument.isAnnulled()}" /></datatables:column>
	                   <datatables:column><c:out value="${not empty entry.finantialDocument && entry.finantialDocument.totalSettledWithoutPaymentEntries}" /></datatables:column>
                        <datatables:column cssStyle="width:80px">
                            <datatables:columnHead>
                                <spring:message code="label.InvoiceEntry.date" />
                            </datatables:columnHead>
                            <c:out value='${entry.entryDateTime.toString("YYYY-MM-dd")}' />
                            <%--                             <joda:format value="${entry.entryDateTime}" style="S-" /> --%>
                        </datatables:column>
                        <datatables:column cssStyle="width:80px">
                            <datatables:columnHead>
                                <spring:message code="label.DebitNote.dueDate" />
                            </datatables:columnHead>
                            <c:out value='${entry.dueDate.toString("YYYY-MM-dd")}' />
                            <%--                             <joda:format value="${entry.entryDateTime}" style="S-" /> --%>
                        </datatables:column>
                        <datatables:column cssStyle="width:100px;">
                            <datatables:columnHead>
                                <spring:message code="label.InvoiceEntry.finantialDocument" />
                            </datatables:columnHead>
                            <c:if test="${not empty entry.finantialDocument }">
                                <c:if test="${entry.isDebitNoteEntry() }">
                                    <a target="_blank"
                                        href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${entry.finantialDocument.externalId}"> <c:out
                                            value="${entry.finantialDocument.uiDocumentNumber}" />
                                    </a>
                                    
									<c:if test="${entry.finantialDocument.isAnnulled()}">
	                                <p><span class="label label-danger"><c:out value='${entry.finantialDocument.state.descriptionI18N.content}' /></span>
		                            </c:if>
                                    
                                </c:if>
                                <c:if test="${entry.isCreditNoteEntry() }">
                                    <a target="_blank"
                                        href="${pageContext.request.contextPath}/treasury/document/manageinvoice/creditnote/read/${entry.finantialDocument.externalId}"> <c:out
                                            value="${entry.finantialDocument.uiDocumentNumber}" />
                                    </a>
                                </c:if>
                            </c:if>
                            <c:if test="${empty entry.finantialDocument }">
                            ---
                            </c:if>
                        </datatables:column>
                        <datatables:column>
                            <datatables:columnHead>
                                <spring:message code="label.InvoiceEntry.description" />
                            </datatables:columnHead>
                            <p>
                            	<c:out value="${entry.description}" />
                            </p>
                        	<c:if test="${entry.finantialDocument != null}">
                        	<c:if test="${entry.finantialDocument.forPayorDebtAccount}">
                        		<p>
                        			<em>
                        				<strong><spring:message code="label.Invoice.payorDebtAccount" />:</strong> 
                        				<span><c:out value="${entry.finantialDocument.payorDebtAccount.customer.fiscalNumber}" /></span>
                        				&nbsp;-&nbsp;
                        				<span><c:out value="${entry.finantialDocument.payorDebtAccount.customer.name}" /></span>
                        			</em>
                        	</c:if>
                        	</c:if>
							
							<c:if test="${entry.isDebitNoteEntry() && entry.academicalActBlockingSuspension}">
							<p><span class="label label-warning"><spring:message code="label.DebitEntry.academicalActBlockingOff" /></span></p>
							</c:if>
							<c:if test="${entry.isDebitNoteEntry() && entry.blockAcademicActsOnDebt}">
							<p><span class="label label-warning"><spring:message code="label.DebitEntry.blockAcademicActsOnDebt" /></span></p>
							</c:if>
                        </datatables:column>
                        <datatables:column cssStyle="width:10%;align:right">
                            <datatables:columnHead>
                                <spring:message code="label.Invoice.totalAmount" />
                            </datatables:columnHead>
                            <div align=right>
                                <c:if test="${entry.isCreditNoteEntry() }">-</c:if>
                                <c:out value="${entry.debtAccount.finantialInstitution.currency.getValueFor(entry.totalAmount)}" />
                            </div>
                        </datatables:column>
                        <%-- 						<datatables:column> --%>
                        <%-- 							<datatables:columnHead> --%>
                        <%-- 								<spring:message code="label.InvoiceEntry.creditAmount" /> --%>
                        <%-- 							</datatables:columnHead> --%>
                        <!-- 							<div align=right> -->
                        <%-- 								<c:out value="${entry.debtAccount.finantialInstitution.currency.getValueFor(pendingEntry.creditAmount)}" /> --%>
                        <!-- 							</div> -->
                        <%-- 						</datatables:column> --%>
                        <datatables:column cssStyle="width:10%;align:right">
                            <datatables:columnHead>
                                <spring:message code="label.InvoiceEntry.openAmount" />
                            </datatables:columnHead>
                            <div align=right>
                                <c:if test="${entry.isCreditNoteEntry() }">-</c:if>
                                <c:out value="${entry.debtAccount.finantialInstitution.currency.getValueFor(entry.openAmount)}" />
                            </div>
                        </datatables:column>
                        <datatables:column>
                            <c:if test="${entry.isDebitNoteEntry() }">
                                <a href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitentry/read/${entry.externalId}">
                            </c:if>
                            <c:if test="${entry.isCreditNoteEntry() }">
                                <a href="${pageContext.request.contextPath}/treasury/document/manageinvoice/creditentry/read/${entry.externalId}">
                            </c:if>
                            <button type="submit" class="btn btn-default btn-xs">
                                <spring:message code="label.view" />
                            </button>
                            <%-- 				<form method="post" action="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${debitNote.externalId}/deleteentry/${debitEntry.externalId}"> --%>
                            <!-- 					<button type="submit" class="btn btn-default btn-xs"> -->
                            <!-- 						<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp; -->
                            <%-- 						<spring:message code="label.event.document.manageInvoice.deleteEntry" /> --%>
                            <!-- 					</button> -->
                            </a>
                        </datatables:column>
                    </datatables:table>
					<script>
                    	$(document).ready(function() {
                    		var table = $('#allDocuments').DataTable({
                    			language : { url : "${datatablesI18NUrl}" },
	                    		"dom": '<"col-sm-6"l><"col-sm-6"f>rtip', //FilterBox = YES && ExportOptions = NO
	                			"bDeferRender" : true,
	                			"bPaginate" : false,
	                	        "columnDefs": [ 
									{ "targets": [0], "visible": false, "searchable": true },
									{ "targets": [1], "visible": false, "searchable": true } 
	                	        ],
	                            "tableTools": {
	                                "sSwfPath": "${pageContext.request.contextPath}/static/treasury/swf/copy_csv_xls_pdf.swf"
	                            }
                    		});
                    		
                    		table.columns.adjust().draw();
                    		
							$('#allDocuments tbody').on( 'click', 'tr', function () {
							      $(this).toggleClass('selected');
							});
							
							$.fn.dataTable.ext.search.push(
							    function( settings, data, dataIndex ) {
							        var includeAnnuledChecked = $('#includeAnnuledCheckbox').is(':checked');
							        var annuled = (data[0].trim() === "true");
							        
							        var filterSettledItemsWithoutPayments = $('#filterSettledItemsWithoutPayments').is(':checked');
							        var totalSettledWithoutPaymentEntries = (data[1].trim() === "true");
							        
							        return (!annuled || includeAnnuledChecked) && (!totalSettledWithoutPaymentEntries || !filterSettledItemsWithoutPayments);
							    }
							);
							
							$('#includeAnnuledCheckbox').click(function() {
								table.columns.adjust().draw();
							});
							
							$('#filterSettledItemsWithoutPayments').click(function() {
								table.columns.adjust().draw();
							});
                    	});
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
        </div>
        <div class="tab-pane" id="payments">
            <!--             <h3>Pagamentos</h3> -->
            <p></p>
            <c:choose>
                <c:when test="${not empty paymentsDataSet}">
                    <datatables:table id="paymentsDataSet" row="payment" data="${paymentsDataSet}" cssClass="table table-bordered table-hover" cdn="false" cellspacing="2">
                        <datatables:column>
                            <datatables:columnHead>
                                <spring:message code="label.FinantialDocument.documentDate" />
                            </datatables:columnHead>
                            <c:out value='${payment.documentDate.toString("YYYY-MM-dd")}' />
                            <%--                             <joda:format value="${payment.documentDate}" style="S-" /> --%>
                        </datatables:column>
                        <datatables:column>
                            <datatables:columnHead>
                                <spring:message code="label.SettlementNote.paymentDate" />
                            </datatables:columnHead>
                            <c:if test="${not payment.reimbursement}">
	                            <c:out value='${payment.paymentDate.toString("YYYY-MM-dd")}' />
                            </c:if>
                        </datatables:column>
                        <datatables:column>
                            <datatables:columnHead>
                                <spring:message code="label.SettlementEntry.finantialDocument" />
                            </datatables:columnHead>
                            <a target="_blank" href="${pageContext.request.contextPath}/treasury/document/managepayments/settlementnote/read/${payment.externalId}"> <c:out
                                    value="${payment.uiDocumentNumber}" />
                                    
							<c:if test="${payment.isAnnulled()}">
                               <p><span class="label label-danger"><c:out value='${payment.state.descriptionI18N.content}' /></span>
                            </c:if>
                                    
                        </datatables:column>
                        <datatables:column>
                            <datatables:columnHead>
                                <spring:message code="label.SettlementNote.settlementEntries" />
                            </datatables:columnHead>
                            <ul>
                                <c:forEach var="settlementEntry" items="${payment.settlemetEntriesSet}">
                                    <c:if test="${settlementEntry.invoiceEntry.isDebitNoteEntry() }">
                                        <li><c:out value="[ ${payment.currency.getValueFor(settlementEntry.amount)} ] ${settlementEntry.description}" /></li>
                                    </c:if>
                                    <c:if test="${settlementEntry.invoiceEntry.isCreditNoteEntry() }">
                                        <li><c:out value="[ -${payment.currency.getValueFor(settlementEntry.amount)} ] ${settlementEntry.description}    " /></li>
                                    </c:if>
                                </c:forEach>
                                <c:if test='${not empty payment.advancedPaymentCreditNote }'>
                                    <c:forEach var="advancedPaymentEntry" items="${payment.advancedPaymentCreditNote.creditEntriesSet}">
                                        <li><c:out value="[ -${payment.currency.getValueFor(advancedPaymentEntry.amount)} ] ${advancedPaymentEntry.description}    " /></li>
                                    </c:forEach>
                                </c:if>
                            </ul>
                        </datatables:column>
                        <datatables:column>
                            <datatables:columnHead>
                                <spring:message code="label.SettlementNote.paymentEntries" />
                            </datatables:columnHead>
                            <ul>
                                <c:forEach var="paymentEntry" items="${payment.paymentEntriesSet}">
                                    <li><c:out value="[ ${payment.currency.getValueFor(paymentEntry.payedAmount)} ] ${paymentEntry.paymentMethod.name.content} " /></li>
                                </c:forEach>
                                <c:if test="${not empty payment.reimbursementEntriesSet }">
                                    <span class="label label-warning"><spring:message code="FinantialDocumentTypeEnum.REIMBURSEMENT_NOTE" /></span>
                                    <c:forEach var="reimbursementEntry" items="${payment.reimbursementEntriesSet}">
                                        <li><c:out
                                                value="[ ${payment.currency.getValueFor(reimbursementEntry.reimbursedAmount)} ] ${reimbursementEntry.paymentMethod.name.content} " /></li>
                                    </c:forEach>
                                </c:if>
                            </ul>
                        </datatables:column>
                        <datatables:column>
                            <a href="${pageContext.request.contextPath}/treasury/document/managepayments/settlementnote/read/${payment.externalId}">
                                <button type="submit" class="btn btn-default btn-xs">
                                    <spring:message code="label.view" />
                                </button>
                            </a>
                        </datatables:column>
                    </datatables:table>
                    <script>
						createDataTables(
								'paymentsDataSet',
								true,
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
        </div>
        
        <div class="tab-pane" id="paymentReferenceCodes">
            <c:choose>
                <c:when test="${not empty usedPaymentCodeTargets}">
                    <datatables:table id="usedPaymentCodeTargets" row="target" data="${usedPaymentCodeTargets}" cssClass="table table-bordered table-hover" cdn="false" cellspacing="2">
                        <datatables:column cssStyle="width:5%">
	                        <datatables:columnHead>
	                            <spring:message code="label.DebitNote.dueDate" />
	                    	</datatables:columnHead>
	
                        	<c:out value='${target.dueDate.toString("YYYY-MM-dd")}' />
                        </datatables:column>
                        	
                        <datatables:column cssStyle="width:55%">
	                        <datatables:columnHead>
	                            <spring:message code="label.InvoiceEntry.description" />
	                    	</datatables:columnHead>
	
                        	<c:if test="${target.finantialDocumentPaymentCode}">
								<ul>
									<c:forEach items="${target.finantialDocument.finantialDocumentEntriesSet}" var="entry">
										<li><c:out value="${entry.description}" /></li>
									</c:forEach>
								</ul>
                        	</c:if>
                        	
                        	<c:if test="${target.multipleEntriesPaymentCode}">
								<ul>
									<c:forEach items="${target.orderedInvoiceEntries}" var="invoiceEntry">
									<li><c:out value="${invoiceEntry.description}" /></li>
									</c:forEach>
								</ul>
                        	</c:if>
                        	
                        </datatables:column>

                        <datatables:column cssStyle="width:30%">
                            <datatables:columnHead>
                                <spring:message code="label.PaymentReferenceCode" />
                        	</datatables:columnHead>
                        	
                             <div>
                                 <strong><spring:message code="label.customer.PaymentReferenceCode.entity" />: </strong>
                                 <c:out value="[${target.paymentReferenceCode.paymentCodePool.entityReferenceCode}]" />
                                 </br> <strong><spring:message code="label.customer.PaymentReferenceCode.reference" />: </strong>
                                 <c:out value="${target.paymentReferenceCode.formattedCode}" />
                                 </br> <strong><spring:message code="label.customer.PaymentReferenceCode.amount" />: </strong>
                                 <c:out value="${debtAccount.finantialInstitution.currency.getValueFor(target.paymentReferenceCode.payableAmount)}" />
                             </div>

						</datatables:column>
						
						<datatables:column cssStyle="width:10%">
							<a href="${pageContext.request.contextPath}<%= org.fenixedu.treasury.ui.administration.payments.sibs.managepaymentreferencecode.PaymentReferenceCodeController.READ_URL %>/${target.paymentReferenceCode.externalId}" 
								target="_blank" class="btn btn-default">
								<spring:message code="label.view" />
							</a>
						</datatables:column>
					</datatables:table>                
                    <script>
						createDataTables(
								'usedPaymentCodeTargets',
								true,
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
        
		</div>
        
    </div>
</div>

<script>
	$(document).ready(function() {

		//Enable Bootstrap Tabs
		$('#tabs').tab();

		if(Omnis && Omnis.block) {
			Omnis.block('exportintegrationline');			
		}
		
	});
</script>