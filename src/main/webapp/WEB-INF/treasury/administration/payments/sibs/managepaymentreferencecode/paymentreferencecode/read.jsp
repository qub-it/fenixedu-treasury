<%@page import="org.fenixedu.treasury.ui.document.manageinvoice.DebitEntryController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<%@ taglib prefix="datatables" uri="http://github.com/dandelion/datatables"%>

<%@page import="org.fenixedu.treasury.ui.document.manageinvoice.DebitNoteController"%>

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
        <spring:message code="label.administration.payments.sibs.managePaymentReferenceCode.readPaymentReferenceCode" />
        <small></small>
    </h1>
</div>
<div class="modal fade" id="anullModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm"
                action="${pageContext.request.contextPath}/treasury/administration/payments/sibs/managepaymentreferencecode/paymentreferencecode/read/${paymentReferenceCode.externalId}/anull"
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
                        <spring:message code="label.administration.payments.sibs.managePaymentReferenceCode.readPaymentReferenceCode.confirmAnull" />
                    </p>
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
<!-- /.modal -->
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a class=""
        href="${pageContext.request.contextPath}/treasury/administration/payments/sibs/managepaymentreferencecode/paymentreferencecode/"><spring:message code="label.event.back" /></a>
    &nbsp;
    <c:if test="${paymentReferenceCode.state!='ANNULLED'}">
    |&nbsp;<span class="glyphicon glyphicon-trash" aria-hidden="true"></span>&nbsp;<a class="" href="#" data-toggle="modal" data-target="#anullModal"><spring:message
                code="label.annull" /></a>
    </c:if>
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
                        <th scope="row" class="col-xs-3"><spring:message code="label.PaymentCodePool.finantialInstitution" /></th>
                        <td><c:out value='${paymentReferenceCode.paymentCodePool.finantialInstitution.name}' /></td>
                    </tr>
                <c:if test="${not empty paymentReferenceCode.targetPayment.debtAccount}">
                    <th scope="row" class="col-xs-3"><spring:message code="label.PaymentReferenceCode.client.name" /></th>
                    <td><c:out value='${paymentReferenceCode.targetPayment.debtAccount.customer.name}' /></td>
                </c:if>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.PaymentReferenceCode.paymentCodePool" /></th>
                        <td><c:out value='${paymentReferenceCode.paymentCodePool.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.PaymentReferenceCode.paymentCodePool" /></th>
                        <td><c:out value='${paymentReferenceCode.paymentCodePool.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.PaymentCodePool.entityReferenceCode" /></th>
                        <td><c:out value='${paymentReferenceCode.paymentCodePool.entityReferenceCode}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.PaymentReferenceCode.referenceCode" /></th>
                        <td><c:out value='${paymentReferenceCode.formattedCode}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.PaymentReferenceCode.payableAmount" /></th>
                        <td><c:out value='${paymentReferenceCode.paymentCodePool.finantialInstitution.currency.getValueFor(paymentReferenceCode.payableAmount)}' /></td>
                    </tr>
                    <c:if test='${not paymentReferenceCode.paymentCodePool.useCheckDigit }'>
	                    <tr>
	                        <th scope="row" class="col-xs-3"><spring:message code="label.PaymentReferenceCode.beginDate" /></th>
	                        <td><c:out value='${paymentReferenceCode.beginDate}' /></td>
	                    </tr>
	                    <tr>
	                        <th scope="row" class="col-xs-3"><spring:message code="label.PaymentReferenceCode.endDate" /></th>
	                        <td><c:out value='${paymentReferenceCode.endDate}' /></td>
	                    </tr>
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.PaymentReferenceCode.minAmount" /></th>
                            <td><c:out value='${paymentReferenceCode.minAmount}' /></td>
                        </tr>
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.PaymentReferenceCode.maxAmount" /></th>
                            <td><c:out value='${paymentReferenceCode.maxAmount}' /></td>
                        </tr>
                    </c:if>

                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.PaymentReferenceCode.state" /></th>
                        <td><c:if test="${paymentReferenceCode.state=='USED'}">
                                <span class="label label-primary">
                            </c:if> <c:if test="${paymentReferenceCode.state=='ANNULLED'}">
                                <span class="label label-danger">
                            </c:if> <c:if test="${paymentReferenceCode.state=='UNUSED'}">
                                <span class="label label-default">
                            </c:if> <c:if test="${paymentReferenceCode.state=='PROCESSED'}">
                                <span class="label label-success">
                            </c:if> <c:out value="${paymentReferenceCode.state.descriptionI18N.content}" /> </span></td>

                    </tr>
                    <c:if test='${not empty paymentReferenceCode.targetPayment }'>
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.PaymentReferenceCode.targetPayment" /></th>
                            <td>
                            	<c:if test='${paymentReferenceCode.targetPayment.isFinantialDocumentPaymentCode() }'>
                                    <ul>
                                        <li><a target="blank_"
                                            href="${pageContext.request.contextPath}<%=DebitNoteController.READ_URL %>${paymentReferenceCode.targetPayment.finantialDocument.externalId}"><c:out
                                                    value='${paymentReferenceCode.targetPayment.finantialDocument.uiDocumentNumber}' /></a></li>
                                        <li><c:out
                                                value='${paymentReferenceCode.targetPayment.finantialDocument.currency.getValueFor(paymentReferenceCode.targetPayment.finantialDocument.openAmountWithInterests)}' />
                                        </li>
                                    </ul>
                                </c:if>
                                <c:if test='${paymentReferenceCode.targetPayment.isMultipleEntriesPaymentCode() }'>
                                    <ul>
                                    	<c:forEach items="${paymentReferenceCode.targetPayment.getOrderedInvoiceEntries()}" var="invoiceEntry">
                                        <li><a target="blank_"
                                            href="${pageContext.request.contextPath}<%=DebitEntryController.READ_URL %>${invoiceEntry.externalId}">
                                            	<c:out value='${invoiceEntry.finantialDocument.uiDocumentNumber}' /> - <c:out value='${invoiceEntry.description}' /></a>
                                            	[<c:out value='${invoiceEntry.debtAccount.finantialInstitution.currency.getValueFor(invoiceEntry.openAmount)}' />]
                                        </li>
                                        </c:forEach>
                                    </ul>
                                </c:if></td>
                        </tr>
                    </c:if>

                </tbody>
            </table>
        </form>
        
	</div>
</div>

		<c:if test='${not empty sibsTransactionDetails}'>
		    <div class="panel-heading">
		        <h3 class="panel-title">
		            <spring:message code="label.SibsTransactionDetail" />
		        </h3>
		    </div>

			<datatables:table id="sibsTransactionDetails" row="detail"
				data="${sibsTransactionDetails}"
				cssClass="table responsive table-bordered table-hover" cdn="false"
				cellspacing="2">

				<datatables:column cssStyle="width:10%">
					<datatables:columnHead>
						<spring:message code="label.SibsTransactionDetail.creationDate" />
					</datatables:columnHead>
					<c:out value='${detail.versioningCreationDate.toString("YYYY-MM-dd HH:mm:ss")}' />
				</datatables:column>

				<datatables:column cssStyle="width:10%">
					<datatables:columnHead>
						<spring:message code="label.SibsTransactionDetail.creator" />
					</datatables:columnHead>
					<c:out value='${detail.versioningCreator}' />
				</datatables:column>

				<datatables:column cssStyle="width:10%">
					<datatables:columnHead>
						<spring:message code="label.SibsTransactionDetail.whenProcessed" />
					</datatables:columnHead>
					<c:out value='${detail.whenProcessed.toString("YYYY-MM-dd")}' />
				</datatables:column>

				<datatables:column cssStyle="width:10%">
					<datatables:columnHead>
						<spring:message code="label.SibsTransactionDetail.whenRegistered" />
					</datatables:columnHead>
					<c:out value='${detail.whenRegistered.toString("YYYY-MM-dd HH:mm:ss")}' />
				</datatables:column>

				<datatables:column cssStyle="width:10%">
					<datatables:columnHead>
						<spring:message code="label.SibsTransactionDetail.amountPayed" />
					</datatables:columnHead>
					<c:out value='${paymentReferenceCode.paymentCodePool.finantialInstitution.currency.getValueFor(detail.amountPayed)}' />
				</datatables:column>

				<datatables:column cssStyle="width:10%">
					<datatables:columnHead>
						<spring:message
							code="label.SibsTransactionDetail.businessIdentification" />
					</datatables:columnHead>
					<c:out value="${detail.businessIdentification}" />
				</datatables:column>

				<datatables:column cssStyle="width:10%">
					<datatables:columnHead>
						<spring:message code="label.SibsTransactionDetail.fiscalNumber" />
					</datatables:columnHead>
					<c:out value="${detail.fiscalNumber}" />
				</datatables:column>

				<datatables:column cssStyle="width:10%">
					<datatables:columnHead>
						<spring:message code="label.SibsTransactionDetail.customerName" />
					</datatables:columnHead>
					<c:out value="${detail.customerName}" />
				</datatables:column>

				<datatables:column cssStyle="width:10%">
					<datatables:columnHead>
						<spring:message
							code="label.SibsTransactionDetail.settlementDocumentNumber" />
					</datatables:columnHead>
					<c:out value="${detail.settlementDocumentNumber}" />
				</datatables:column>

				<datatables:column cssStyle="width:30%">
					<datatables:columnHead>
						<spring:message code="label.SibsTransactionDetail.comments" />
					</datatables:columnHead>
					<c:out value="${detail.comments}" />
				</datatables:column>

			</datatables:table>

	        <script>
										createDataTables(
												'sibsTransactionDetails',
												false,
												false,
												true,
												"${pageContext.request.contextPath}",
												"${datatablesI18NUrl}");
			</script>

		</c:if>


<script>
	$(document).ready(function() {

	});
</script>
