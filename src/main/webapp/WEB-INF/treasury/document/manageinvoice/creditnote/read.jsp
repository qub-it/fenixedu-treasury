<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js"/>
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css"/>

<link rel="stylesheet" href="${datatablesCssUrl}"/>
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json"/>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css"/>

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<link href="${pageContext.request.contextPath}/static/treasury/css/dataTables.responsive.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/static/treasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>						
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js" ></script>
<script src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>



<%-- TITLE --%>
<div class="page-header">
	<h1><spring:message code="label.document.manageInvoice.readCreditNote" />
		<small></small>
	</h1>
</div>
<%-- NAVIGATION --%>
<div class="well well-sm" style="display:inline-block">
	<span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a class="" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/customer/read"  ><spring:message code="label.event.back" /></a>
|&nbsp;&nbsp;	<span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>&nbsp;<a class="" href="${pageContext.request.contextPath}/treasury/document/manageinvoice/creditnote/update/${creditNote.externalId}"  ><spring:message code="label.event.update" /></a>
|&nbsp;&nbsp;	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a class="" href="${pageContext.request.contextPath}/treasury/document/manageinvoice/creditnote/read/${creditNote.externalId}/closecreditnote"  ><spring:message code="label.event.document.manageInvoice.closeCreditNote" /></a>	|&nbsp;&nbsp;
	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a class="" href="${pageContext.request.contextPath}/treasury/document/manageinvoice/creditnote/read/${creditNote.externalId}/anullcreditnote"  ><spring:message code="label.event.document.manageInvoice.anullCreditNote" /></a>	|&nbsp;&nbsp;
	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a class="" href="${pageContext.request.contextPath}/treasury/document/manageinvoice/creditnote/read/${creditNote.externalId}/addentry"  ><spring:message code="label.event.document.manageInvoice.addEntry" /></a>	
</div>
	<c:if test="${not empty infoMessages}">
				<div class="alert alert-info" role="alert">
					
					<c:forEach items="${infoMessages}" var="message"> 
						<p> <span class="glyphicon glyphicon glyphicon-ok-sign" aria-hidden="true">&nbsp;</span>
  							${message}
  						</p>
					</c:forEach>
					
				</div>	
			</c:if>
			<c:if test="${not empty warningMessages}">
				<div class="alert alert-warning" role="alert">
					
					<c:forEach items="${warningMessages}" var="message"> 
						<p> <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
  							${message}
  						</p>
					</c:forEach>
					
				</div>	
			</c:if>
			<c:if test="${not empty errorMessages}">
				<div class="alert alert-danger" role="alert">
					
					<c:forEach items="${errorMessages}" var="message"> 
						<p> <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
  							${message}
  						</p>
					</c:forEach>
					
				</div>	
			</c:if>

<div class="panel panel-primary">
	<div class="panel-heading">
		<h3 class="panel-title"><spring:message code="label.details"/></h3>
	</div>
	<div class="panel-body">
<form method="post" class="form-horizontal">
<table class="table">
		<tbody>
<tr>
	<th scope="row" class="col-xs-3"><spring:message code="label.CreditNote.documentDate"/></th> 
	<td>
		<c:out value='${creditNote.documentDate}'/>
	</td> 
</tr>
<tr>
	<th scope="row" class="col-xs-3"><spring:message code="label.CreditNote.documentDueDate"/></th> 
	<td>
		<c:out value='${creditNote.documentDueDate}'/>
	</td> 
</tr>
<tr>
	<th scope="row" class="col-xs-3"><spring:message code="label.CreditNote.documentNumber"/></th> 
	<td>
		<c:out value='${creditNote.documentNumber}'/>
	</td> 
</tr>
<tr>
	<th scope="row" class="col-xs-3"><spring:message code="label.CreditNote.originDocumentNumber"/></th> 
	<td>
		<c:out value='${creditNote.originDocumentNumber}'/>
	</td> 
</tr>
<tr>
	<th scope="row" class="col-xs-3"><spring:message code="label.CreditNote.state"/></th> 
	<td>
		<c:out value='${creditNote.state}'/>
	</td> 
</tr>
</tbody>
</table>
</form>
</div>
</div>
<h2>
	<spring:message code="label.CreditNote.creditEntries" />
</h2>

<%-- NAVIGATION --%>
<c:if test="${creditNote.isPreparing()}">

	<div class="well well-sm" style="display: inline-block">
		<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a
			href="${pageContext.request.contextPath}/treasury/document/manageinvoice/creditnote/read/${creditNote.externalId}/addentry"><spring:message
				code="label.event.document.manageInvoice.addEntry" /></a> &nbsp;|
	</div>
</c:if>
<c:choose>
	<c:when test="${not empty creditNote.creditEntriesSet}">
		<datatables:table id="creditEntries" row="creditEntry" data="${creditNote.creditEntriesSet}" cssClass="table responsive table-bordered table-hover" cdn="false" cellspacing="2">
			<datatables:column cssStyle="width:10%">
				<datatables:columnHead>
					<spring:message code="label.CreditEntry.quantity" />
				</datatables:columnHead>
				<c:out value="${creditEntry.quantity}" />
			</datatables:column>
			<datatables:column>
				<datatables:columnHead>
					<spring:message code="label.CreditEntry.description" />
				</datatables:columnHead>
				<c:out value="${creditEntry.description}" />
			</datatables:column>
			<datatables:column cssStyle="width:10%">
				<datatables:columnHead>
					<spring:message code="label.CreditEntry.amount" />
				</datatables:columnHead>
				<c:out value="${creditEntry.totalAmount}" />
			</datatables:column>
			<datatables:column cssStyle="width:10%">
				<datatables:columnHead>
					<spring:message code="label.DebitEntry.vat" />
				</datatables:columnHead>
				<c:out value="${creditEntry.vat.taxRate}" />
			</datatables:column>
			<datatables:column cssStyle="width:10%">
				<form method="get" action="${pageContext.request.contextPath}/treasury/document/manageinvoice/creditentry/read/${creditEntry.externalId}">
					<button type="submit" class="btn btn-default btn-xs">
						<spring:message code="label.view" />
					</button>
				</form>
			</datatables:column>
		</datatables:table>
		<script>
			createDataTables('creditEntries', false, false, false,
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

	
	});
</script>
