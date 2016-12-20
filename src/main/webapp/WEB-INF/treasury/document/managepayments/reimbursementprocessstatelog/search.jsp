<%@page import="org.fenixedu.treasury.ui.document.manageinvoice.CreditNoteController"%>
<%@page import="org.fenixedu.treasury.ui.document.manageinvoice.DebitNoteController"%>
<%@page import="org.fenixedu.treasury.ui.document.managepayments.SettlementNoteController"%>
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

<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.ReimbursementProcessStateLog.title" />
        <small></small>
    </h1>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;
    <a href="${pageContext.request.contextPath}<%= SettlementNoteController.READ_URL %>${settlementNote.externalId}">
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
                        <td>
                        	<c:if test="${settlementNote.isAnnulled()}">
                                <span class="label label-danger">
                            </c:if> 
                            <c:if test="${settlementNote.isPreparing() }">
                                <span class="label label-warning">
                            </c:if> 
                            <c:if test="${settlementNote.isClosed()}">
                                <span class="label label-primary">
                            </c:if> 
                            <c:out value='${settlementNote.state.descriptionI18N.content}' /></span>
                            <c:if test="${not settlementNote.isPreparing() and settlementNote.isDocumentToExport()}">
	                            &nbsp;
	                            <span class="label label-warning">
	                            	<spring:message code="label.FinantialDocument.document.is.pending.to.export" />
	                            </span>
                            </c:if>
                            
                            <c:if test="${settlementNote.reimbursement and settlementNote.currentReimbursementProcessStatus != null}">
                            &nbsp;
                            <c:if test="${settlementNote.reimbursementPending}">
                                <span class="label label-warning">
                            </c:if>
                            <c:if test="${settlementNote.reimbursementConcluded}">
                                <span class="label label-primary">
                            </c:if>
                            <c:if test="${settlementNote.reimbursementRejected}">
                                <span class="label label-danger">
                            </c:if>
                            	<c:out value='${settlementNote.currentReimbursementProcessStatus.description}' />
                            	</span>
                            </c:if>
                       </td>
                    </tr>
                </tbody>
            </table>
        </form>
    </div>
</div>

<h2><spring:message code="label.ReimbursementProcessStateLog.logs" /></h2>

<c:choose>
    <c:when test="${not empty logs}">
        <datatables:table id="logs" row="l" data="${logs}" cssClass="table responsive table-bordered table-hover" cdn="false" cellspacing="2">
            <datatables:column cssStyle="width:15%">
                <datatables:columnHead>
                    <spring:message code="label.ReimbursementProcessStateLog.versioningCreationDate" />
                </datatables:columnHead>
				<c:out value='${l.versioningCreationDate.toString("YYYY-MM-dd HH:mm:ss")}' />
            </datatables:column>
            <datatables:column>
                <datatables:columnHead>
                    <spring:message code="label.ReimbursementProcessStateLog.reimbursementProcessStatusType" />
                </datatables:columnHead>
                <c:out value="${l.reimbursementProcessStatusType.description}" />
            </datatables:column>

			<%-- 
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.ReimbursementProcessStateLog.statusId" />
                </datatables:columnHead>
                <c:out value="${l.statusId}" />
            </datatables:column>
			--%>
			
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.ReimbursementProcessStateLog.statusDate" />
                </datatables:columnHead>
                <c:out value='${l.statusDate.toString("YYYY-MM-dd HH:mm:ss")}' />
            </datatables:column>

            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.ReimbursementProcessStateLog.remarks" />
                </datatables:columnHead>
                <c:out value="${l.remarks}" />
            </datatables:column>

        </datatables:table>
        <script>
			createDataTables('logs', false, false, false, "${pageContext.request.contextPath}", "${datatablesI18NUrl}");
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
			Omnis.block('exportCreditNoteIntegrationOnline');			
		}
	});
</script>
