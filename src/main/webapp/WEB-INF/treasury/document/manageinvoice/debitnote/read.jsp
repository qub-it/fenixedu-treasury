<%@page import="org.fenixedu.treasury.ui.document.manageinvoice.DebitNoteController"%>
<%@page import="org.fenixedu.treasury.ui.document.manageinvoice.CreditNoteController"%>
<%@page import="org.fenixedu.bennu.core.security.Authenticate"%>
<%@page import="org.fenixedu.treasury.domain.accesscontrol.TreasuryAccessControl"%>
<%@page import="org.fenixedu.treasury.domain.FinantialInstitution"%>
<%@page import="org.fenixedu.treasury.domain.document.DebitNote"%>
<%@page import="org.fenixedu.treasury.ui.integration.erp.ERPExportOperationController"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="org.fenixedu.treasury.ui.document.managepayments.SettlementNoteController"%>
<%@page import="org.fenixedu.treasury.ui.administration.payments.sibs.managepaymentreferencecode.PaymentReferenceCodeController"%>
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

<link href="${pageContext.request.contextPath}/static/treasury/css/dataTables.responsive.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/static/treasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<link href="${pageContext.request.contextPath}/static/treasury/css/omnis.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<%
        DebitNote debitNote = (DebitNote) request
                        .getAttribute("debitNote");
FinantialInstitution finantialInstitution = (FinantialInstitution) debitNote.getDebtAccount().getFinantialInstitution();
    %>
<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.document.manageInvoice.readDebitNote" />
        <small></small>
    </h1>
</div>

<% 
                if (TreasuryAccessControl.getInstance().isFrontOfficeMember(Authenticate.getUser(), finantialInstitution)) {
%>

<div class="modal fade" id="closeModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${debitNote.externalId}/closedebitnote" method="POST">
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
                        <spring:message code="label.document.manageInvoice.readDebitNote.confirmClose" />
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

<div class="modal fade" id="calculateInterestValueModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${debitNote.externalId}/calculateinterestvalue" method="GET">
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

                        <spring:message code="label.document.manageInvoice.readDebitNote.confirmCalculateInterestValue" />
                        
                    </p>
                    <br/>
                    
                    <div class="form-group row">
                            <div class="col-sm-4 control-label">
                                <spring:message code="label.SettlementNote.paymentDate" />
                            </div>

                            <div class="col-sm-8">
                                <input id="debitNote_paymentDate" class="form-control" type="text" bennu-date name="paymentdate" required value='' />
                            </div>
                        </div>
                    
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <spring:message code="label.cancel" />
                    </button>
                    <button id="deleteButton" class="btn btn-primary" type="submit">
                        <spring:message code="label.ok" />
                    </button>
                </div>
            </form>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->




<%
} 
%>

<% 
                if (TreasuryAccessControl.getInstance().isAllowToModifyInvoices(Authenticate.getUser(), finantialInstitution)) {
%>

<div class="modal fade" id="deleteModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/delete/${debitNote.externalId}" method="POST">
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

                        <spring:message code="label.document.manageInvoice.readDebitNote.confirmDelete" />
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



<div class="modal fade" id="anullModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${debitNote.externalId}/anulldebitnote" method="POST">
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
                        <%--                         <spring:message code="label.document.manageInvoice.readDebitNote.confirmAnull" /> --%>
                        ${ anullDebitNoteMessage }
                    </p>
                    <br /> <br />
                    <div class="form">
                        <div class="form-group row">
                            <div class="col-sm-4 control-label">
                                <spring:message code="label.DebitNote.annulledReason" />
                            </div>

                            <div class="col-sm-8">
                                <input id="debitNote_anullReason" class="form-control" type="text" name="reason" required value='' />
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



<%} %>

<% 
	if (TreasuryAccessControl.getInstance().isBackOfficeMember(Authenticate.getUser(), finantialInstitution)) {
%>

<div class="modal fade" id="clearDocumentToExport">
    <div class="modal-dialog">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${debitNote.externalId}/cleardocumenttoexport" method="post">
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
                    	<spring:message code="label.document.manageInvoice.clearDocumentToExport" />
                    </p>
                    
					<div class="form-group row">
						<div class="col-sm-12">
							<input class="form-control" type="text" name="reason" />
						</div>
					</div>
                    
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <spring:message code="label.cancel" />
                    </button>
                    <button id="deleteButton" class="btn btn-primary" type="submit">
                        <spring:message code="label.ok" />
                    </button>
                </div>
            </form>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->

<% 
	}
%>

<%-- NAVIGATION --%>
<form>
    <div class="well well-sm" style="display: inline-block">
        <span class="glyphicon glyphicon-user" aria-hidden="true"></span><a class=""
            href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${debitNote.debtAccount.externalId}"> <spring:message
                code="label.document.manageInvoice.readDebitEntry.event.backToDebtAccount" />
        </a> 
<% 
if (TreasuryAccessControl.getInstance().isAllowToModifyInvoices(Authenticate.getUser(), finantialInstitution)) {
%>        
            <div class="btn-group">
                <button class="btn btn-default dropdown-toggle" type="button" id="dropdownMenu2" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
                    <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
                    <spring:message code="label.event.document.manageinvoice.debitnote.options">
                    </spring:message>
                    <span class="caret"></span>
                </button>
                
<ul class="dropdown-menu" aria-labelledby="dropdownMenu2">
        <c:if test="${debitNote.isPreparing() || debitNote.isClosed()}">
			<li>            
	            <a class="" href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/update/${debitNote.externalId}">
	            	<span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>&nbsp;<spring:message code="label.event.update" />
	            </a>
            </li>
            <li>
	            <a class="" href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/updatepayordebtaccount/${debitNote.externalId}">
	            	<span class="glyphicon glyphicon-user" aria-hidden="true"></span>&nbsp;<spring:message code="label.DebitNote.event.updatePayorDebtAccount" />
	            </a>
            </li>
		</c:if>

        <c:if test="${debitNote.isPreparing()}">
            <li>
	            <a class="" href="#" data-toggle="modal" data-target="#closeModal">
	            	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span> 
	            	<spring:message code="label.event.document.manageInvoice.closeDebitNote" />
	            </a>
            </li> 
           
		</c:if>

        <c:if test="${debitNote.isPreparing() || debitNote.isClosed()}">

            <li>
                <a class="" href="#" data-toggle="modal" data-target="#anullModal"> 
                	<span class="glyphicon glyphicon-remove-circle" aria-hidden="true"></span>
                	<spring:message code="label.event.document.manageInvoice.anullDebitNote" />
	            </a>
            </li>

        <c:if test="${debitNote.isClosed()}">
            <li>
	            <a class="" href="${pageContext.request.contextPath}/treasury/document/manageinvoice/creditnote/create?debitnote=${debitNote.externalId}">
	            	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
	            	<spring:message code="label.event.document.manageInvoice.createCreditNote" />
	            </a>
            </li>
		</c:if>        
		    
            <c:if test="${debitNote.isClosed() }">
                    <li>
            
                    <a class="" href="#" data-toggle="modal" data-target="#calculateInterestValueModal"> <span class="glyphicon glyphicon-cog" aria-hidden="true"></span> <spring:message
                    code="label.event.document.manageInvoice.debitNote.calculateInterestValue" />
                    </a>
                </li>
            </c:if>
        </c:if>
        	
        	<%--
	        <li>
		        <a class="" id="printLabel2" href="#"
		            onclick="document.getElementById('accordion').style.display = 'none';window.print();document.getElementById('accordion').style.display = 'block';"> 
			        <span class="glyphicon glyphicon-print" aria-hidden="true"></span>
	            	<spring:message code="label.print" />
		        </a>
	        </li>
	        --%>
        
        <c:if test="${debitNote.isPreparing() || debitNote.isClosed()}">
			<li>            
	            <a class="" href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/viewerpcustomerfields/${debitNote.externalId}">
	            	<span class="glyphicon glyphicon-file" aria-hidden="true"></span>&nbsp;
	            	<spring:message code="label.Invoice.erpCustomerFields.title" />
	            </a>
            </li>
		</c:if>        
</ul>

        </div>
<%} else { %>
            <div class="btn-group">
                <button class="btn btn-default dropdown-toggle" type="button" id="dropdownMenu2" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
                    <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
                    <spring:message code="label.event.document.manageinvoice.debitnote.options">
                    </spring:message>
                    <span class="caret"></span>
                </button>
				<ul class="dropdown-menu" aria-labelledby="dropdownMenu2">
				        <c:if test="${debitNote.isPreparing()}">
				            <li>
					            <a class="" href="#" data-toggle="modal" data-target="#closeModal">
					            	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
					            	<spring:message code="label.event.document.manageInvoice.closeDebitNote" />
					            </a>
				            </li>            
				        </c:if>
				
				 </ul>
			</div>
<%} %>
        <c:if test="${debitNote.documentSeriesNumberSet}">
|
            <div class="btn-group">
                <button class="btn btn-default dropdown-toggle" type="button" id="dropdownMenu1" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
                    <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
                    <spring:message code="label.event.administration.managefinantialinstitution.finantialinstitution.erpoptions">
                    </spring:message>
                    <span class="caret"></span>
                </button>
                <ul class="dropdown-menu" aria-labelledby="dropdownMenu1">
					<c:if test="${debitNote.documentToExport}">
					<c:if test="${validAddress}">
                    <li>
                    	<a id="exportDebitNoteIntegrationOnline" href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${debitNote.externalId}/exportintegrationonline">
	                    	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
	                    	<spring:message code="label.event.document.manageInvoice.exportDebitNoteIntegrationOnline" />
                    	</a>
                    </li>
					</c:if>
                    </c:if>
                            
                    <li><a href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${debitNote.externalId}/exportintegrationfile">
                            <span class="glyphicon glyphicon-export" aria-hidden="true"></span> <spring:message code="label.event.document.manageInvoice.exportIntegrationFile" />
                    </a></li>
                    <li><a href="${pageContext.request.contextPath}<%= ERPExportOperationController.SEARCH_URL %>?finantialinstitution=${debitNote.debtAccount.finantialInstitution.externalId}&documentnumber=${debitNote.uiDocumentNumber}">
                    <span class="glyphicon glyphicon-export" aria-hidden="true"></span> <spring:message code="label.event.document.manageInvoice.searchExportOperations" />
                    </a>
                    </li>

<% 
	if (debitNote.isDocumentToExport() && TreasuryAccessControl.getInstance().isBackOfficeMember(Authenticate.getUser(), finantialInstitution)) {
%>
                    <li>
                    	<a href="#" data-toggle="modal" data-target="#clearDocumentToExport">
                    		<span class="glyphicon glyphicon-ok-circle" aria-hidden="true"></span>
                    		<spring:message code="label.event.document.manageInvoice.clearDocumentToExport" />
                    	</a>
                    </li>
<%} %>
                    
                </ul>
            </div>
        </c:if>
	<c:if test="${debitNote.isClosed() && not debitNote.documentToExport}">
       	&nbsp;|&nbsp;
        <span class="glyphicon glyphicon-print" aria-hidden="true"></span>
        <a href="${pageContext.request.contextPath}<%= DebitNoteController.DOWNLOAD_CERTIFIED_DOCUMENT_PRINT_URL %>/${debitNote.externalId}">
        	<spring:message code="label.FinantialDocument.download.invoice" />
        </a>
	</c:if>
    </div>
</form>


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
                        <td><c:out value='${debitNote.documentNumberSeries.series.finantialInstitution.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebtAccount.customer" /></th>
                        <td><c:out value='${debitNote.debtAccount.customer.businessIdentification} - ${debitNote.debtAccount.customer.name}' /></td>
                    </tr>
                    <c:if test='${not empty debitNote.payorDebtAccount}'>
                        <c:if test='${not debitNote.payorDebtAccount.equals(debitNote.debtAccount)}'>
                            <tr>
                                <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.payorDebtAccount" /></th>
                                <td><c:out value='${debitNote.payorDebtAccount.customer.businessIdentification} - ${debitNote.payorDebtAccount.customer.name}' /></td>
                            </tr>
                        </c:if>
                    </c:if>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.documentNumber" /></th>
                        <td><strong><c:out value='${debitNote.uiDocumentNumber}' /></strong></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.documentDate" /></th>
                        <td><joda:format value="${debitNote.documentDate}" style="S-" /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.documentDueDate" /></th>
                        <td><joda:format value="${debitNote.documentDueDate}" style="S-" /></td>
                    </tr>
                    <c:if test="${not empty  debitNote.originDocumentNumber}">
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.originDocumentNumber" /></th>
                            <td><c:out value='${debitNote.originDocumentNumber}' /></td>
                        </tr>
                    </c:if>
                    <c:if test="${not empty  debitNote.documentObservations}">
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.documentObservations" /></th>
                            <td><c:out value='${debitNote.documentObservations}' /></td>
                        </tr>
                    </c:if>
                    <c:if test="${not empty  debitNote.clearDocumentToExportReason}">
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.clearDocumentToExportReason" /></th>
                            <td><c:out value='${debitNote.clearDocumentToExportReason}' /></td>
                        </tr>
                    </c:if>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.state" /></th>
                        <td><c:if test="${debitNote.isAnnulled()}">
                                <span class="label label-danger">
                            </c:if> <c:if test="${debitNote.isPreparing() }">
                                <span class="label label-warning">
                            </c:if> <c:if test="${debitNote.isClosed()}">
                                <span class="label label-primary">
                            </c:if> <c:out value='${debitNote.state.descriptionI18N.content}' /> </span>                            
                            <c:if test="${not debitNote.isPreparing() }">
                                <c:if test="${debitNote.isDocumentToExport() }">
                                    &nbsp;<span class="label label-warning"> <spring:message code="label.FinantialDocument.document.is.pending.to.export" />
                                    </span>
                                </c:if>
                            </c:if></td>
                    </tr>
                    <c:if test="${not empty debitNote.annulledReason}">
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.annulledReason" /></th>
                            <td><c:out value='${debitNote.annulledReason}' /></td>
                        </tr>
                    </c:if>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.totalNetAmount" /></th>
                        <td><c:out value='${debitNote.debtAccount.finantialInstitution.currency.getValueFor(debitNote.totalNetAmount)}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.totalVatAmount" /></th>
                        <td><c:out value='${debitNote.debtAccount.finantialInstitution.currency.getValueFor(debitNote.totalVatAmount)}' /></td>
                    </tr>

                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.totalAmount" /></th>
                        <td><c:out value='${debitNote.debtAccount.finantialInstitution.currency.getValueFor(debitNote.totalAmount)}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.openAmount" /></th>
                        <td><c:out value='${debitNote.debtAccount.finantialInstitution.currency.getValueFor(debitNote.openAmount+ debitNote.pendingInterestAmount)}' /></td>
                    </tr>
                    <c:if test="${debitNote.pendingInterestAmount.unscaledValue() != 0 }">
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.pendingInterestAmount" /></th>
                            <td><c:out value='${debitNote.debtAccount.finantialInstitution.currency.getValueFor(debitNote.pendingInterestAmount)}' /> <span
                                class="label label-info"><spring:message code="label.DebtAccount.interestIncludedInDebtAmount" /></span></td>
                        </tr>
                    </c:if>
<%--                     <c:if test="${debitNote.paymentCodesSet.size()>0 && debitNote.openAmount > 0  }"> --%>
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.payemntCodes" /></th>
                            <td>

                                <ul>
                                    <c:forEach var="paymentcode" items="${debitNote.paymentCodesSet}">
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
<%--                     </c:if> --%>
                    <c:if test="${not empty debitNote.relatedSettlementEntries }">
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.relatedSettlementEntries" /></th>
                            <td>
                                <ul>

                                    <c:forEach var="settlementEntry" items="${debitNote.relatedSettlementEntries}">
                                        <li><c:out value='${settlementEntry.entryDateTime.toString("YYYY-MM-dd")} - '/> <a target="_blank"
                                            href="${pageContext.request.contextPath}<%=SettlementNoteController.READ_URL %>${settlementEntry.finantialDocument.externalId}"><c:out
                                                    value='${settlementEntry.finantialDocument.uiDocumentNumber}' /></a> <c:out
                                                value=' [${ settlementEntry.finantialDocument.debtAccount.finantialInstitution.currency.getValueFor(settlementEntry.amount)}]' /></li>
                                    </c:forEach>
                                </ul>
                            </td>
                        </tr>
                    </c:if>

                    <c:if test="${not empty debitNote.relatedCreditEntriesSet }">
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.DebitNote.relatedCreditEntries" /></th>
                            <td>
                                <ul>

                                    <c:forEach var="creditEntry" items="${debitNote.relatedCreditEntriesSet}">
                                        <li><c:out value='${creditEntry.entryDateTime.toString("YYYY-MM-dd")} - '/> <a target="_blank"
                                            href="${pageContext.request.contextPath}<%=CreditNoteController.READ_URL %>${creditEntry.finantialDocument.externalId}"><c:out
                                                    value='${creditEntry.finantialDocument.uiDocumentNumber}' /></a> <c:out
                                                value=' [${ creditEntry.finantialDocument.debtAccount.finantialInstitution.currency.getValueFor(creditEntry.amount)}]' /></li>
                                    </c:forEach>
                                </ul>
                            </td>
                        </tr>
                    </c:if>


                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Versioning.creator" /></th>
                        <td>[<c:out value='${debitNote.getVersioningCreator()}' />] <joda:format value="${debitNote.getVersioningCreationDate()}" style="SS" /></td>
                    </tr>

                </tbody>
            </table>
        </form>
    </div>
</div>

<p></p>
<p></p>

<h2>
    <spring:message code="label.DebitNote.debitEntries" />
</h2>

<% 
                if (TreasuryAccessControl.getInstance().isAllowToModifyInvoices(Authenticate.getUser(), finantialInstitution)) {
%>  
<%-- NAVIGATION --%>
<c:if test="${debitNote.isPreparing()}">

    <div class="well well-sm" style="display: inline-block">
        <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a
            href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${debitNote.externalId}/addentry"><spring:message
                code="label.event.document.manageInvoice.addEntry" /></a> &nbsp;|&nbsp; <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a class=""
            href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${debitNote.externalId}/addpendingentries"><spring:message
                code="label.event.document.manageInvoice.addPendingEntries" /></a>
    </div>
</c:if>
<%} %>
<c:choose>
    <c:when test="${not empty debitNote.debitEntriesSet}">
        <datatables:table id="debitEntries" row="debitEntry" data="${debitNote.debitEntriesSet}" cssClass="table responsive table-bordered table-hover" cdn="false" cellspacing="2">
<%--             <datatables:column cssStyle="width:5%" sortInit="asc"> --%>
<%--                 <c:out value='${debitEntry.entryOrder}' /> --%>
<%--             </datatables:column> --%>
            <datatables:column cssStyle="width:10%" sortInit="desc">
                <datatables:columnHead>
                    <spring:message code="label.DebitNote.dueDate" />
                </datatables:columnHead>
                <c:out value='${debitEntry.dueDate.toString("YYYY-MM-dd")}' />
            </datatables:column>
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.InvoiceEntry.quantity" />
                </datatables:columnHead>
                <c:out value="${debitEntry.quantity}" />
            </datatables:column>
            <datatables:column>
                <datatables:columnHead>
                    <spring:message code="label.DebitEntry.description" />
                </datatables:columnHead>
                
                <c:out value="${debitEntry.description}" />
				<c:if test="${debitEntry.academicalActBlockingSuspension}">
				<p><span class="label label-warning"><spring:message code="label.DebitEntry.academicalActBlockingOff" /></span></p>
				</c:if>
				<c:if test="${debitEntry.blockAcademicActsOnDebt}">
				<p><span class="label label-warning"><spring:message code="label.DebitEntry.blockAcademicActsOnDebt" /></span></p>
				</c:if>
            </datatables:column>
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.DebitEntry.amount" />
                </datatables:columnHead>
                <c:out value="${debitEntry.totalAmount}" />
            </datatables:column>
            <datatables:column cssStyle="width:8%">
                <datatables:columnHead>
                    <spring:message code="label.DebitEntry.vat" />
                </datatables:columnHead>
                <c:out value="${debitEntry.vat.taxRate}" />
            </datatables:column>
            <datatables:column cssStyle="width:10%">
                <a href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitentry/read/${debitEntry.externalId}" type="submit" class="btn btn-default btn-xs">
                    <spring:message code="label.view" />
                </a>
            </datatables:column>
        </datatables:table>
        <script>
									createDataTables(
											'debitEntries',
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



<script>
	$(document).ready(function() {

		if(Omnis && Omnis.block) {
			Omnis.block('exportDebitNoteIntegrationOnline');			
		}
		
	});
</script>
