<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
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
        <spring:message code="label.document.manageInvoice.updateDebitNote" />
        <small></small>
    </h1>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a class=""
        href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/read/${debitNote.externalId}"><spring:message code="label.event.back" /></a> &nbsp;
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


<div class="panel panel-default">
	<div class="panel-body">
		<div class="form-group row">
			<div class="col-sm-2 control-label">
				<spring:message code="label.DebtAccount.finantialInstitution" />
			</div>
		
			<div class="col-sm-10">
				<div class="form-control">
					<c:out value="${debitNote.debtAccount.finantialInstitution.name}" />
				</div>
			</div>
		</div>
		
		<div class="form-group row">
			<div class="col-sm-2 control-label">
				<spring:message code="label.DebitEntry.debtAccount" />
			</div>
			
			<div class="col-sm-10">
				<div class="form-control">
					<c:out value="${debitNote.debtAccount.customer.businessIdentification} - ${debitNote.debtAccount.customer.name}" />
				</div>
			</div>
		</div>
		<div class="form-group row">
			<div class="col-sm-2 control-label">
				<spring:message code="label.DebitEntry.finantialDocument" />
			</div>
			
			<div class="col-sm-10">
				<div class="form-control">
					<c:out value='${debitNote.uiDocumentNumber}' />
				</div>
			</div>
		</div>
		
		<div class="form-group row">
			<div class="col-sm-2 control-label">
				<spring:message code="label.DebitNote.payorDebtAccount" />
			</div>
			
			<div class="col-sm-10">
				<div class="form-control">
					<c:out value='${debitNote.debtAccount.customer.fiscalNumber} - ${debitNote.debtAccount.customer.name}' />
				</div>
			</div>
		</div>
		
	<c:if test="${debitNote.closed}">
		<div class="form-group row">
			<div class="col-sm-2 control-label">
				&nbsp;
			</div>
			
			<div class="col-sm-10">
				<span class="label label-warning">
					<spring:message code="label.DebitNote.updatePayorDebtAccount.on.closed.debit.note" />
				</span>
			</div>
		</div>
	</c:if>
	</div>
</div>
<form method="post" class="form-horizontal">

    <div class="panel panel-default">
        <div class="panel-body">

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebitNote.payorDebtAccount" />
                </div>

                <div class="col-sm-4">
                    <%-- Relation to side 1 drop down rendered in input --%>
                    <select id="debitNote_payorDebtAccount" class="js-example-basic-single" name="payordebtaccount">
                        <option value="">&nbsp;</option>
						<c:forEach items="${DebitNote_payorDebtAccount_options}" var="element">
							<option value="${element.externalId}"><c:out value='${element.customer.uiFiscalNumber} - ${element.customer.name}'/></option>
						</c:forEach>
                    </select>
                </div>
            </div>
        </div>

        <div class="panel-footer">
            <input type="submit" class="btn btn-default" role="button" value="<spring:message code="label.submit" />" />
        </div>
    </div>
</form>

<script>
$(document).ready(function() {
	<%-- Block for providing payorDebtAccount options --%>
	<%-- CHANGE_ME --%> <%-- INSERT YOUR FORMAT FOR element --%>

	$("#debitNote_payorDebtAccount").select2();
    $("#debitNote_payorDebtAccount").select2().select2('val', '<c:out value='${not empty param.payordebtaccount ? param.payordebtaccount : debitNote.payorDebtAccount.externalId }'/>');
    <%-- End block for providing payorDebtAccount options --%>

		});

</script>
