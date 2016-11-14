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

<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.Invoice.erpCustomerFields.title" />
        <small></small>
    </h1>
</div>

<%-- NAVIGATION --%>
<form>
    <div class="well well-sm" style="display: inline-block">
        <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>
        <a href="${pageContext.request.contextPath}${backUrl}">
            <spring:message code="label.event.back" />
        </a>
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
                        <td><c:out value='${invoice.documentNumberSeries.series.finantialInstitution.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebtAccount.customer" /></th>
                        <td><c:out value='${invoice.debtAccount.customer.businessIdentification} - ${invoice.debtAccount.customer.name}' /></td>
                    </tr>
                    <c:if test='${not empty invoice.payorDebtAccount}'>
                        <c:if test='${not invoice.payorDebtAccount.equals(invoice.debtAccount)}'>
                            <tr>
                                <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.payorDebtAccount" /></th>
                                <td><c:out value='${invoice.payorDebtAccount.customer.businessIdentification} - ${invoice.payorDebtAccount.customer.name}' /></td>
                            </tr>
                        </c:if>
                    </c:if>

                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.documentNumber" /></th>
                        <td><strong><c:out value='${invoice.uiDocumentNumber}' /></strong></td>
                    </tr>

                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.state" /></th>
                        <td>
                        	<c:if test="${invoice.isAnnulled()}">
                                <span class="label label-danger">
                            </c:if> 
                            <c:if test="${invoice.isPreparing() }">
                                <span class="label label-warning">
                            </c:if> 
                            <c:if test="${invoice.isClosed()}">
                                <span class="label label-primary">
                            </c:if> <c:out value='${invoice.state.descriptionI18N.content}' /> </span>                            
                            <c:if test="${not invoice.isPreparing() }">
                                <c:if test="${invoice.isDocumentToExport() }">
                                    &nbsp;<span class="label label-warning"><spring:message code="label.FinantialDocument.document.is.pending.to.export" /></span>
                                </c:if>
                            </c:if>
                        </td>
                    </tr>
                </tbody>
            </table>
        </form>
    </div>
</div>

<div class="panel panel-primary">
    <div class="panel-heading">
        <h3 class="panel-title">
            <spring:message code="label.Invoice.erpCustomerFields.customer.details" />
        </h3>
    </div>
    <div class="panel-body">
        <form method="post" class="form-horizontal">
            <table class="table">
                <tbody>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerBusinessId" /></th>
                        <td><c:out value='${invoice.customerBusinessId}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerFiscalCountry" /></th>
                        <td><c:out value='${invoice.customerFiscalCountry}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerNationality" /></th>
                        <td><c:out value='${invoice.customerNationality}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerId" /></th>
                        <td><c:out value='${invoice.customerId}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerAccountId" /></th>
                        <td><c:out value='${invoice.customerAccountId}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerFiscalNumber" /></th>
                        <td><c:out value='${invoice.customerFiscalNumber}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerName" /></th>
                        <td><c:out value='${invoice.customerName}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerContact" /></th>
                        <td><c:out value='${invoice.customerContact}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerStreetName" /></th>
                        <td><c:out value='${invoice.customerStreetName}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerAddressDetail" /></th>
                        <td><c:out value='${invoice.customerAddressDetail}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerCity" /></th>
                        <td><c:out value='${invoice.customerCity}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerZipCode" /></th>
                        <td><c:out value='${invoice.customerZipCode}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerRegion" /></th>
                        <td><c:out value='${invoice.customerRegion}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerCountry" /></th>
                        <td><c:out value='${invoice.customerCountry}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerFields.update.info" /></th>
                        <td>[<c:out value='${invoice.customerFieldsUpdateUser}' />] <c:out value='${invoice.customerFieldsUpdateDate}' /></td>
					</tr>

                </tbody>
            </table>
        </form>
    </div>
</div>

<div class="panel panel-primary">
    <div class="panel-heading">
        <h3 class="panel-title">
            <spring:message code="label.Invoice.erpCustomerFields.payorCustomer.details" />
        </h3>
    </div>
    <div class="panel-body">
        <form method="post" class="form-horizontal">
            <table class="table">
                <tbody>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerBusinessId" /></th>
                        <td><c:out value='${invoice.payorCustomerBusinessId}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerFiscalCountry" /></th>
                        <td><c:out value='${invoice.payorCustomerFiscalCountry}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerNationality" /></th>
                        <td><c:out value='${invoice.payorCustomerNationality}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerId" /></th>
                        <td><c:out value='${invoice.payorCustomerId}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerAccountId" /></th>
                        <td><c:out value='${invoice.payorCustomerAccountId}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerFiscalNumber" /></th>
                        <td><c:out value='${invoice.payorCustomerFiscalNumber}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerName" /></th>
                        <td><c:out value='${invoice.payorCustomerName}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerContact" /></th>
                        <td><c:out value='${invoice.payorCustomerContact}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerStreetName" /></th>
                        <td><c:out value='${invoice.payorCustomerStreetName}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerAddressDetail" /></th>
                        <td><c:out value='${invoice.payorCustomerAddressDetail}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerCity" /></th>
                        <td><c:out value='${invoice.payorCustomerCity}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerZipCode" /></th>
                        <td><c:out value='${invoice.payorCustomerZipCode}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerRegion" /></th>
                        <td><c:out value='${invoice.payorCustomerRegion}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerCountry" /></th>
                        <td><c:out value='${invoice.payorCustomerCountry}' /></td>
					</tr>

					<tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Invoice.erpCustomerFields.customerFields.update.info" /></th>
                        <td>[<c:out value='${invoice.payorCustomerFieldsUpdateUser}' />] <c:out value='${invoice.payorCustomerFieldsUpdateDate}' /></td>
					</tr>

                </tbody>
            </table>
        </form>
    </div>
</div>
