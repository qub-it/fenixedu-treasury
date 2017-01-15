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

<link href="${pageContext.request.contextPath}/static/treasury/css/dataTables.responsive.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/static/treasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<script src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.document.manageInvoice.createDebitNote" />
        <small></small>
    </h1>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;
    <c:if test="${empty param.debtaccount }">
        <a class="" href="${pageContext.request.contextPath}/treasury/document/manageinvoice/debitnote/"><spring:message code="label.event.back" /></a>
    </c:if>
    <c:if test="${not empty param.debtaccount }">
        <a class="" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${param.debtaccount}"><spring:message code="label.event.back" /></a>
    </c:if>
    &nbsp;
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

<form method="post" class="form-horizontal">

    <input type="hidden" name="debitentry" value="${debitEntry.externalId}" />
    <div class="panel panel-default">
        <div class="panel-body">
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebitNote.debtAccount" />
                </div>

                <div class="col-sm-4">
                    <%-- Relation to side 1 drop down rendered in input --%>
                    <select id="debitNote_debtAccount" class="js-example-basic-single" name="debtaccount">
                    </select>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebitNote.documentNumberSeries" />
                </div>

                <div class="col-sm-4">
                    <%-- Relation to side 1 drop down rendered in input --%>
                    <select id="debitNote_documentNumberSeries" class="js-example-basic-single" name="documentnumberseries">
                    </select>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebitNote.documentDate" />
                </div>

                <div class="col-sm-4">
                    <c:if test='${not empty debitEntry }'>
                        <input id="debitNote_documentDate" class="form-control" type="text" name="documentdate" bennu-date required
                            value='<%=new org.joda.time.LocalDate().toString("YYYY-MM-dd")%>' />
                    </c:if>
                    <c:if test='${empty debitEntry }'>
                        <c:if test='${not empty param.documentdate}'>
                            <input id="debitNote_documentDate" class="form-control" type="text" name="documentdate" bennu-date required value="${ param.documentdate}" />
                        </c:if>
                        <c:if test='${empty param.documentdate}'>
                            <input id="debitNote_documentDate" class="form-control" type="text" name="documentdate" bennu-date required
                                value="<%=new org.joda.time.LocalDate()
							.toString("YYYY-MM-dd")%>" />
                        </c:if>
                    </c:if>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebitNote.documentDueDate" />
                </div>

                <div class="col-sm-4">
                    <c:if test='${not empty debitEntry }'>
                        <input id="debitNote_documentDueDate" class="form-control" type="text" name="documentduedate" bennu-date required
                            value='<%= new org.joda.time.LocalDate().toString("YYYY-MM-dd") %>' />
                    </c:if>
                    <c:if test='${empty debitEntry }'>
                        <c:if test='${not empty param.documentduedate}'>
                            <input id="debitNote_documentDueDate" class="form-control" type="text" name="documentduedate" bennu-date required value="${ param.documentduedate}" />
                        </c:if>
                        <c:if test='${empty param.documentduedate}'>
                            <input id="debitNote_documentDueDate" class="form-control" type="text" name="documentduedate" bennu-date required
                                value="<%=new org.joda.time.LocalDate()
							.toString("YYYY-MM-dd")%>" />
                        </c:if>
                    </c:if>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebitNote.originDocumentNumber" />
                </div>

                <div class="col-sm-10">
                    <input id="debitNote_originDocumentNumber" class="form-control" type="text" name="origindocumentnumber"
                        value='<c:out value='${not empty param.origindocumentnumber ? param.origindocumentnumber : debitNote.originDocumentNumber }'/>' />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebitNote.documentObservations" />
                </div>

                <div class="col-sm-10">
                    <input id="debitNote_documentObservations" class="form-control" type="text" name="documentobservations"
                        value='<c:out value='${not empty param.documentobservations ? param.documentobservations : debitNote.documentObservations }'/>' />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.DebitNote.payorDebtAccount" />
                </div>

                <div class="col-sm-4">
                    <%-- Relation to side 1 drop down rendered in input --%>
                    <select id="debitNote_payorDebtAccount" class="js-example-basic-single" name="payordebtaccount">
                        <option value="">&nbsp;</option>
                        <%-- empty option remove it if you don't want to have it or give it a label CHANGE_ME --%>
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
		payorDebtAccount_options = [
			<c:forEach items="${DebitNote_payorDebtAccount_options}" var="element"> 
				{
					text : "<c:out value='${element.customer.uiFiscalNumber} - ${element.customer.name}'/>",  
					id : "<c:out value='${element.externalId}'/>"
				},
			</c:forEach>
		];
		
		$("#debitNote_payorDebtAccount").select2(
			{
				data : payorDebtAccount_options,
			}	  
	    );
	    
	    
	    
	    $("#debitNote_payorDebtAccount").select2().select2('val', '<c:out value='${param.payordebtaccount}'/>');
	
	
		<%-- End block for providing finantialDocumentType options --%>
		<%-- Block for providing debtAccount options --%>
		<%-- CHANGE_ME --%> <%-- INSERT YOUR FORMAT FOR element --%>
		debtAccount_options = [
			<c:forEach items="${DebitNote_debtAccount_options}" var="element"> 
				{
					text : "<c:out value='${element.customer.businessIdentification} - ${element.customer.name}'/>",  
					id : "<c:out value='${element.externalId}'/>"
				},
			</c:forEach>
		];
		
		$("#debitNote_debtAccount").select2(
			{
				data : debtAccount_options,
			}	  
	    );
	    
	    
	    
	    $("#debitNote_debtAccount").select2().select2('val', '<c:out value='${param.debtaccount}'/>');
	
		<%-- End block for providing debtAccount options --%>
		<%-- Block for providing documentNumberSeries options --%>
		<%-- CHANGE_ME --%> <%-- INSERT YOUR FORMAT FOR element --%>
		documentNumberSeries_options = [
			<c:forEach items="${DebitNote_documentNumberSeries_options}" var="element"> 
				{
					text : "<c:out value='${element.series.code} - ${element.series.name.content}'/>",  
					id : "<c:out value='${element.externalId}'/>"
				},
			</c:forEach>
		];
		
		$("#debitNote_documentNumberSeries").select2(
			{
				data : documentNumberSeries_options,
			}	  
	    );
	    
	    
	    
	    $("#debitNote_documentNumberSeries").select2().select2('val', '<c:out value='${param.documentnumberseries}'/>');
	
		<%-- End block for providing documentNumberSeries options --%>
		<%-- Block for providing currency options --%>
		<%-- CHANGE_ME --%> <%-- INSERT YOUR FORMAT FOR element --%>
		currency_options = [
			<c:forEach items="${DebitNote_currency_options}" var="element"> 
				{
					text : "<c:out value='${element}'/>",  
					id : "<c:out value='${element.externalId}'/>"
				},
			</c:forEach>
		];
		
		$("#debitNote_currency").select2(
			{
				data : currency_options,
			}	  
	    );
	    
	    
	    
	    $("#debitNote_currency").select2().select2('val', '<c:out value='${param.currency}'/>');
	
		<%-- End block for providing currency options --%>
	
	
	});
</script>
