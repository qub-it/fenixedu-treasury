<%@page import="org.fenixedu.treasury.domain.accesscontrol.TreasuryAccessControl"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="datatables" uri="http://github.com/dandelion/datatables"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>

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
            code="label.administration.payments.sibs.managePaymentCodePool.readPaymentCodePool" />
        <small></small>
    </h1>
</div>
<div class="modal fade" id="deleteModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm"
                action="${pageContext.request.contextPath}/treasury/administration/payments/sibs/managepaymentcodepool/paymentcodepool/delete/${paymentCodePool.externalId}"
                method="POST">
                <div class="modal-header">
                    <button type="button" class="close"
                        data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title">
                        <spring:message code="label.confirmation" />
                    </h4>
                </div>
                <div class="modal-body">
                    <p>
                        <spring:message
                            code="label.administration.payments.sibs.managePaymentCodePool.readPaymentCodePool.confirmDelete" />
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default"
                        data-dismiss="modal">
                        <spring:message code="label.close" />
                    </button>
                    <button id="deleteButton" class="btn btn-danger"
                        type="submit">
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
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a
        class=""
        href="${pageContext.request.contextPath}/treasury/administration/payments/sibs/managepaymentcodepool/paymentcodepool/"><spring:message
            code="label.event.back" /></a>
    <%-- &nbsp;|&nbsp;<span class="glyphicon glyphicon-trash" aria-hidden="true"></span>&nbsp;<a class="" href="#" data-toggle="modal" data-target="#deleteModal"><spring:message code="label.event.delete" /></a> --%>


<%
    if (TreasuryAccessControl.getInstance().isManager()) {
%>

    &nbsp;|&nbsp;<span class="glyphicon glyphicon-pencil"
        aria-hidden="true"></span>&nbsp;<a class=""
        href="${pageContext.request.contextPath}/treasury/administration/payments/sibs/managepaymentcodepool/paymentcodepool/update/${paymentCodePool.externalId}"><spring:message
            code="label.event.update" /></a> &nbsp;

<%
    }
%>

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
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.PaymentCodePool.finantialInstitution" /></th>
                        <td><c:out
                                value='${paymentCodePool.finantialInstitution.name}' />
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.PaymentCodePool.name" /></th>
                        <td><c:out value='${paymentCodePool.name}' />
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.PaymentCodePool.entityReferenceCode" /></th>
                        <td><c:out
                                value='${paymentCodePool.entityReferenceCode}' />
                        </td>
                    </tr>
                                        <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.PaymentCodePool.active" /></th>
                        <td><c:if test="${paymentCodePool.active}">
                        <span class="label label-primary">
                                <spring:message code="label.true" /></span>
                            </c:if> <c:if test="${not paymentCodePool.active}">
                                                    <span class="label label-danger">
                                <spring:message code="label.false" /></span>
                            </c:if></td>
                    </tr>
                    
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.PaymentCodePool.minReferenceCode" /></th>
                        <td><c:out
                                value='${paymentCodePool.minReferenceCode}' />
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.PaymentCodePool.maxReferenceCode" /></th>
                        <td><c:out
                                value='${paymentCodePool.maxReferenceCode}' />
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.PaymentCodePool.minAmount" /></th>
                        <td><c:out
                                value='${paymentCodePool.minAmount} ${paymentCodePool.finantialInstitution.currency.symbol}' />
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.PaymentCodePool.maxAmount" /></th>
                        <td><c:out
                                value='${paymentCodePool.maxAmount} ${paymentCodePool.finantialInstitution.currency.symbol}' />
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.PaymentCodePool.validFrom" /></th>
                        <td><c:out
                                value='${paymentCodePool.validFrom}' />
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.PaymentCodePool.validTo" /></th>
                        <td><c:out
                                value='${paymentCodePool.validTo}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.PaymentCodePool.useCheckDigit" /></th>
                        <td><c:if
                                test="${paymentCodePool.useCheckDigit}">
                                <spring:message code="label.true" />
                            </c:if> <c:if
                                test="${not paymentCodePool.useCheckDigit}">
                                <spring:message code="label.false" />
                            </c:if></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.PaymentCodePool.documentSeriesForPayments" /></th>
                        <td><c:out
                                value="${paymentCodePool.documentSeriesForPayments.series.name.content}" />
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.PaymentCodePool.paymentMethod" /></th>
                        <td><c:out
                                value="${paymentCodePool.paymentMethod.name.content}" />
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.PaymentCodePool.generateReferenceCodeOnDemand" /></th>
                        <td><c:if
                                test="${paymentCodePool.generateReferenceCodeOnDemand}">
                                <spring:message code="label.true" />
                            </c:if> <c:if
                                test="${not paymentCodePool.generateReferenceCodeOnDemand}">
                                <spring:message code="label.false" />
                            </c:if>
                        </td>
                    </tr>
                </tbody>
            </table>
        </form>
    </div>
</div>

<p></p>
<h2>
    <spring:message code="label.PaymentCodePool.paymentReferenceCodes" />
</h2>

<%-- NAVIGATION --%>
<%-- <c:if test="${debitNote.isPreparing()}"> --%>

<!--     <div class="well well-sm" style="display: inline-block"> -->
<!--         <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a -->
<%--             href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${debitNote.externalId}/addentry"><spring:message --%>
<%--                 code="label.event.document.manageInvoice.addEntry" /></a> &nbsp;|&nbsp; <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a class="" --%>
<%--             href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${debitNote.externalId}/addpendingentries"><spring:message --%>
<%--                 code="label.event.document.manageInvoice.addPendingEntries" /></a> --%>
<!--     </div> -->
<%-- </c:if> --%>
<c:choose>
    <c:when test="${not empty paymentCodePool.paymentReferenceCodesSet}">
        <datatables:table id="referenceCodes" row="referenceCode"
            data="${ paymentCodePool.paymentReferenceCodesSet}"
            cssClass="table responsive table-bordered table-hover"
            cdn="false" cellspacing="2">
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message
                        code="label.PaymentReferenceCode.referenceCode" />
                </datatables:columnHead>
                <c:out value="${referenceCode.formattedCode}" />
            </datatables:column>
            <datatables:column>
                <datatables:columnHead>
                    <spring:message
                        code="label.PaymentReferenceCode.description" />
                </datatables:columnHead>
                <c:out value="${referenceCode.description}" />
            </datatables:column>
            <datatables:column>
                <datatables:columnHead>
                    <spring:message
                        code="label.PaymentReferenceCode.state" />
                </datatables:columnHead>
                <c:if test="${referenceCode.state=='USED'}">
                    <span class="label label-primary">
                </c:if>
                <c:if test="${referenceCode.state=='ANNULLED'}">
                    <span class="label label-danger">
                </c:if>
                <c:if test="${referenceCode.state=='UNUSED'}">
                    <span class="label label-default">
                </c:if>
                <c:if test="${referenceCode.state=='PROCESSED'}">
                    <span class="label label-success">
                </c:if>  
                <c:out value="${referenceCode.state.descriptionI18N.content}" />
                </span>
            </datatables:column>
			
			<datatables:column>
                <datatables:columnHead>
                    <spring:message code="label.PaymentReferenceCode.client.name" />
                </datatables:columnHead>
                				
 				<c:if test="${referenceCode.targetPayment != null && referenceCode.targetPayment.referenceDebtAccount != null}">
					<c:out value="${referenceCode.targetPayment.referenceDebtAccount.customer.name}" />
 				</c:if>
			</datatables:column>

			<datatables:column>
                <datatables:columnHead>
                    <spring:message code="label.PaymentReferenceCode.client.vatNumber" />
                </datatables:columnHead>
				
 				<c:if test="${referenceCode.targetPayment != null && referenceCode.targetPayment.referenceDebtAccount != null}">
					<c:out value="${referenceCode.targetPayment.referenceDebtAccount.customer.fiscalNumber}" />
				</c:if>
			</datatables:column>

			<datatables:column>
                <datatables:columnHead>
                    <spring:message code="label.PaymentReferenceCode.client.businessNumber" />
                </datatables:columnHead>
		
 				<c:if test="${referenceCode.targetPayment != null && referenceCode.targetPayment.referenceDebtAccount != null}">
					<c:out value="${referenceCode.targetPayment.referenceDebtAccount.customer.businessIdentification}" />
				</c:if>
			</datatables:column>

            <datatables:column cssStyle="width:10%">
                <a class="btn btn-default btn-xs" href="${pageContext.request.contextPath}/treasury/administration/payments/sibs/managepaymentreferencecode/paymentreferencecode/read/${referenceCode.externalId}">
<!--                     <button type="submit" class="btn btn-default btn-xs"> -->
                        <spring:message code="label.view" />
<!--                     </button> -->
                </a>
            </datatables:column>
        </datatables:table>
        <script>
									createDataTables(
											'referenceCodes',
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



<script>
	$(document).ready(function() {

	});
</script>
