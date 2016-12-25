<%@page
    import="org.fenixedu.treasury.ui.accounting.managecustomer.TreasuryEventController"%>
<%@page
    import="org.fenixedu.treasury.ui.managetreasuryexemption.TreasuryExemptionController"%>
<%@page
    import="org.fenixedu.treasury.domain.exemption.TreasuryExemption"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="datatables"
    uri="http://github.com/dandelion/datatables"%>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>

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
            code="label.viewCustomerTreasuryEvents.readTreasuryEvent" />
        <small></small>
    </h1>
</div>
<div class="modal fade" id="annulDebitEntriesModal"> 
   <div class="modal-dialog"> 
     <div class="modal-content">

       <div class="modal-header">
         <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button> 
         <h4 class="modal-title"><spring:message code="label.TreasuryEvent.annul.all.debit.entries.title"/></h4>
       </div> 

     <form id ="annulDebitEntriesModalForm" action="${pageContext.request.contextPath}<%= TreasuryEventController.ANNULALLDEBITENTRIES_URL %>${debtAccount.externalId}/${treasuryEvent.externalId}"  method="POST">

	       <div class="modal-body"> 
       
		        <p><em><spring:message code = "label.TreasuryEvent.annul.debit.entry.reason.confirm"/></em></p>
	            <p>&nbsp;</p>
	            <div class="form-group row">
	                <div class="col-sm-2 control-label">
	                    <spring:message code="label.TreasuryEvent.annul.debit.entry.reason" />
	                </div>
	
	                <div class="col-sm-10">
	                    <input id="" class="form-control" type="text" name="treasuryEventAnullDebitEntriesReason" />
	                </div>
	            </div>
		       </div> 
		       
		       <div class="modal-footer"> 
		         <button type="button" class="btn btn-default" data-dismiss="modal"><spring:message code = "label.close"/></button>
		         <button id="anullButton" class ="btn btn-danger" type="submit"><spring:message code = "label.annul"/></button>
		       </div> 
       </form> 
       
     </div> 
   </div> 
 </div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>
	&nbsp;
	<a class=""
		href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/treasuryevent/?debtaccount=${debtAccount.externalId}">
		<spring:message code="label.event.back" />
	</a>
	&nbsp;|&nbsp;
    <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp; 
   	<a class="" href="#" data-toggle="modal" data-target="#annulDebitEntriesModal"> 
       	<spring:message code="label.event.anull.all.debit.entries" />
    </a>

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
                <c:if test='${ debtAccount.getClosed() }'>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebtAccount.closed" /></th>
                        <td><span class="label label-warning"><spring:message code="warning.DebtAccount.is.closed" /></span></td>
                    </tr>
                </c:if>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.fiscalNumber" /></th>
                        <td><c:out value='${debtAccount.customer.fiscalNumber}' /></td>
                    </tr>

                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebtAccount.customer" /></th>
                        <td><c:out value='${debtAccount.customer.businessIdentification}' /> - <c:out value='${debtAccount.customer.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.DebtAccount.finantialInstitution" /></th>
                        <td><c:out value='${debtAccount.finantialInstitution.name}' /></td>
                    </tr>
                
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.TreasuryEvent.description" /></th>
                        <td><c:out
                                value='${treasuryEvent.description.content}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.TreasuryEvent.amountToPay" />
                        </th>
                        <td><c:out
                                value='${debtAccount.finantialInstitution.currency.getValueFor(treasuryEvent.amountToPay)}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.TreasuryEvent.treasuryEventDate" />
                        </th>
                        <td>
                        	<joda:format value="${treasuryEvent.treasuryEventDate}" style="S-" />
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3">
                        	Nome do aluno
                        </th>
                        <td>
                        	<c:out value="${treasuryEvent.person.name}" />
                        </td>
                    </tr>
               </tbody>
            </table>
        </form>
    </div>
</div>

<h2>
    <spring:message code="label.TreasuryEvent.treasuryExemption" />
</h2>

<p>
	<a class="btn btn-default" href="${pageContext.request.contextPath}<%= TreasuryExemptionController.CREATE_URL %>/${debtAccount.externalId}/${treasuryEvent.externalId}">
		<span class="glyphicon glyphicon-plus-sign" aria-hidden="true">&nbsp;</span>
		<spring:message code="label.event.create" />
	</a>
</p>
<c:if test="${not empty treasuryEvent.treasuryExemptionsSet}">

    <script type="text/javascript">
		function processDelete(externalId) {
			url = '${pageContext.request.contextPath}<%=TreasuryExemptionController.SEARCH_TO_DELETE_ACTION_URL%>${debtAccount.externalId}/' + externalId;
			$("#deleteForm").attr("action", url);
			$('#deleteModal').modal('toggle')
		}
  
	</script>

    <div class="modal fade" id="deleteModal">
        <div class="modal-dialog">
            <div class="modal-content">
                <form id="deleteForm" action="" method="POST">
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
                                code="label.manageTreasuryExemption.searchTreasuryExemption.confirmDelete" />
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

    <datatables:table id="exemptionsTable" row="exemption"
        data="${treasuryEvent.treasuryExemptionsSet}"
        cssClass="table responsive table-bordered table-hover"
        cdn="false" cellspacing="2">
        <datatables:column cssStyle="width:15%">
            <datatables:columnHead>
                <spring:message
                    code="label.TreasuryExemption.treasuryExemptionType" />
            </datatables:columnHead>
            <c:out
                value="${exemption.treasuryExemptionType.name.content}" />
        </datatables:column>
        <datatables:column cssStyle="width:10%">
            <datatables:columnHead>
                <spring:message
                    code="label.TreasuryExemption.exemptByPercentage" />
            </datatables:columnHead>
            <p align=center>
                <c:if test="${exemption.exemptByPercentage}">
                    <spring:message code="label.yes" />
                </c:if>

                <c:if test="${not exemption.exemptByPercentage}">
                    <spring:message code="label.no" />
                </c:if>
            </p>
        </datatables:column>
        <datatables:column cssStyle="width:10%">
            <datatables:columnHead>
                <spring:message
                    code="label.TreasuryExemption.valueToExempt" />
            </datatables:columnHead>
            <c:if test="${exemption.exemptByPercentage}">
                <c:out value="${exemption.valueToExempt}" /> %
		</c:if>

            <c:if test="${not exemption.exemptByPercentage}">
                <c:out
                    value="${debtAccount.finantialInstitution.currency.getValueFor(exemption.valueToExempt)}" />
            </c:if>
        </datatables:column>
        <datatables:column cssStyle="width:30%">
            <datatables:columnHead>
                <spring:message code="label.TreasuryExemption.debitEntry" />
            </datatables:columnHead>
            <p align=left>
                <c:out value='${exemption.debitEntry.description}' />
            </p>
        </datatables:column>
        <datatables:column cssStyle="width:25%">
            <datatables:columnHead>
                <spring:message code="label.TreasuryExemption.reason" />
            </datatables:columnHead>
            <p align=left>
                <c:out value='${exemption.reason}' />
            </p>
        </datatables:column>
        <datatables:column cssStyle="width:10%">
             <a class="btn btn-danger" href="#" data-toggle="modal" data-target="#deleteModal" onclick="processDelete('${exemption.externalId}')">
                <span class="glyphicon glyphicon-trash" aria-hidden="true"></span>&nbsp; 
				<spring:message code="label.event.delete" />
			</a>
        </datatables:column>
    </datatables:table>

</c:if>

<c:if test="${not empty treasuryEvent.propertiesMap}">
    <h2>
        <spring:message code="label.TreasuryEvent.propertiesJsonMap" />
    </h2>

    <table id="treasuryEventTableMap"
        class="table responsive table-bordered table-hover" width="100%">

        <c:forEach var="property" items="${treasuryEvent.propertiesMap}">
            <tr>
                <th><c:out value="${property.key}" /></th>
                <td><c:out value="${property.value}" /></td>
            </tr>
        </c:forEach>
    </table>

</c:if>

<h2>
    <spring:message code="label.TreasuryEvent.allDebitEntries" />
</h2>
<div class="tab-pane" id="allDebitEntries">
    <p></p>
    <c:choose>
        <c:when test="${not empty allActiveDebitEntriesDataSet}">
        
        <script type="text/javascript">
        function processAnnul(externalId) {
            url = '${pageContext.request.contextPath}<%=TreasuryEventController.ANNULDEBITENTRY_URL%>${debtAccount.externalId}/' + externalId;
                            $("#annulForm").attr("action", url);
                            $('#annulModal').modal('toggle')
                        }
        
        function processRevert(externalId) {
            url = '${pageContext.request.contextPath}<%=TreasuryEventController.REVERTANNULDEBITENTRY_URL%>${debtAccount.externalId}/' + externalId;
                            $("#revertForm").attr("action", url);
                            $('#revertModal').modal('toggle')
                        }
        
        </script>
        
    <div class="modal fade" id="annulModal">
        <div class="modal-dialog">
            <div class="modal-content">
                <form id="annulForm" action="" method="POST">
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
                                code="label.TreasuryEvent.annulDebitEntry.confirmRemove" />
                        </p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default"
                            data-dismiss="modal">
                            <spring:message code="label.close" />
                        </button>
                        <button id="deleteButton" class="btn btn-danger"
                            type="submit">
                            <spring:message code="label.TreasuryExemption.removeFromEvent" />
                        </button>
                    </div>
                </form>
            </div>
            <!-- /.modal-content -->
        </div>
        <!-- /.modal-dialog -->
    </div>
    <div class="modal fade" id="revertModal">
        <div class="modal-dialog">
            <div class="modal-content">
                <form id="revertForm" action="" method="POST">
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
                                code="label.TreasuryEvent.revertDebitEntry.confirmAnnul" />
                        </p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default"
                            data-dismiss="modal">
                            <spring:message code="label.close" />
                        </button>
                        <button id="deleteButton" class="btn btn-danger"
                            type="submit">
                            <spring:message code="label.revert" />
                        </button>
                    </div>
                </form>
            </div>
            <!-- /.modal-content -->
        </div>
        <!-- /.modal-dialog -->
    </div>
        
            <datatables:table id="allDebitEntriesTable" row="debitEntry"
                data="${allActiveDebitEntriesDataSet}"
                cssClass="table responsive table-bordered table-hover"
                cdn="false" cellspacing="2">
                <datatables:column cssStyle="width:10%">
                    <datatables:columnHead>
                        <spring:message
                            code="label.TreasuryEvent.allDebitEntries.documentNumber" />
                    </datatables:columnHead>
                    <c:out
                        value="${debitEntry.finantialDocument.uiDocumentNumber}" />
					<c:if test="${not empty debitEntry.finantialDocument && debitEntry.finantialDocument.isAnnulled()}">
						<span class="label label-danger"><c:out value='${debitEntry.finantialDocument.state.descriptionI18N.content}' /></span>
					</c:if>
                </datatables:column>
                <datatables:column cssStyle="width:10%">
                    <datatables:columnHead>
                        <spring:message
                            code="label.TreasuryEvent.allDebitEntries.dueDate" />
                    </datatables:columnHead>
                    <p align=center>
                    <c:out value="${debitEntry.dueDate}" />
                    	<%-- 
                        <joda:format value="${debitEntry.dueDate}"
                            style="S-" />
                            --%>
                    </p>
                </datatables:column>
                <datatables:column cssStyle="width:50%">
                    <datatables:columnHead>
                        <spring:message
                            code="label.TreasuryEvent.allDebitEntries.description" />
                    </datatables:columnHead>
                    <p>
                        <c:out value="${debitEntry.description}" />
                    </p>
                    <p>
                    	<em><c:out value="${debitEntry.debtAccount.customer.fiscalCountry} - ${debitEntry.debtAccount.customer.fiscalNumber}" /></em>
                    </p>
					<p>
	                    <c:if test="${debitEntry.eventAnnuled}">
	                        <span class="label label-danger"><spring:message code="label.TreasuryExemption.removedFromEvent" /></span>
	                    </c:if>
                    </p>
                </datatables:column>
                <datatables:column cssStyle="width:10%">
                    <datatables:columnHead>
                        <spring:message
                            code="label.TreasuryEvent.allDebitEntries.amount" />
                    </datatables:columnHead>
                    <p align=right>
                        <c:out
                            value="${debtAccount.finantialInstitution.currency.getValueFor(debitEntry.amount)}" />
                    </p>
                </datatables:column>
                <datatables:column cssStyle="width:10%">
                    <datatables:columnHead>
                        <spring:message
                            code="label.TreasuryEvent.allDebitEntries.exemptedAmount" />
                    </datatables:columnHead>
                    <p align=right>
                        <c:out
                            value="${debtAccount.finantialInstitution.currency.getValueFor(debitEntry.exemptedAmount)}" />
                    </p>
                </datatables:column>
                <datatables:column cssStyle="width:10%">
                        <c:if test="${!debitEntry.eventAnnuled}">
                            <a class="btn btn-default" data-toggle="modal" data-target="#annulModal" onclick="processAnnul('${treasuryEvent.externalId}/${debitEntry.externalId}')"
                                	href="${pageContext.request.contextPath}<%= TreasuryEventController.ANNULDEBITENTRY_URL %>${treasuryEvent.externalId}/${debitEntry.externalId}">
                                <span aria-hidden="true" class="glyphicon glyphicon-remove-circle" ></span>
                                <spring:message code="label.TreasuryExemption.removeFromEvent" />
                            </a>
                        </c:if>
                        <c:if test="${debitEntry.eventAnnuled && (empty debitEntry.finantialDocument || !debitEntry.finantialDocument.isAnnulled())}">

						<a class="btn btn-default" data-toggle="modal" data-target="#revertModal"
                    		onclick="processRevert('${treasuryEvent.externalId}/${debitEntry.externalId}')"
                                href="${pageContext.request.contextPath}<%= TreasuryEventController.REVERTANNULDEBITENTRY_URL %>${treasuryEvent.externalId}/${debitEntry.externalId}">
		                        <span aria-hidden="true" class="glyphicon glyphicon-retweet" ></span>&nbsp;
                                <spring:message code="label.revert" />
                        </a>
                        </c:if>
                </datatables:column>
            </datatables:table>
            <script>
				createDataTables(
						'allDebitEntriesTable',
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
                    <span class="glyphicon glyphicon-exclamation-sign"
                        aria-hidden="true">&nbsp;</span>
                    <spring:message code="label.noResultsFound" />
                </p>
            </div>
        </c:otherwise>
    </c:choose>
</div>
<c:choose>
    <c:when test="${not empty allActiveCreditEntriesDataSet}">
        <h2>
            <spring:message
                code="label.TreasuryEvent.allActiveCreditEntries" />
        </h2>
        <div class="tab-pane" id="allActiveCreditEntriesDataSet">
            <p></p>
            <datatables:table id="allActiveCreditEntriesDataSetTable"
                row="creditEntry"
                data="${allActiveCreditEntriesDataSet}"
                cssClass="table responsive table-bordered table-hover"
                cdn="false" cellspacing="2">
                <datatables:column cssStyle="width:10%">
                    <datatables:columnHead>
                        <spring:message
                            code="label.TreasuryEvent.allDebitEntries.documentNumber" />
                    </datatables:columnHead>
                    <c:out
                        value="${creditEntry.finantialDocument.uiDocumentNumber}" />
                </datatables:column>
                <datatables:column cssStyle="width:15%">
                    <datatables:columnHead>
                        <spring:message
                            code="label.TreasuryEvent.allDebitEntries.entryDateTime" />
                    </datatables:columnHead>
                    <p align=center>
                        <joda:format
                            value="${creditEntry.entryDateTime}"
                            style="S-" />
                    </p>
                </datatables:column>
                <datatables:column cssStyle="width:60%">
                    <datatables:columnHead>
                        <spring:message
                            code="label.TreasuryEvent.allDebitEntries.description" />
                    </datatables:columnHead>
                    <c:out value="${creditEntry.description}" />
                </datatables:column>
                <datatables:column cssStyle="width:10%">
                    <datatables:columnHead>
                        <spring:message
                            code="label.TreasuryEvent.allDebitEntries.amount" />
                    </datatables:columnHead>
                    <p align=right>
                        -
                        <c:out
                            value="${creditEntry.debtAccount.finantialInstitution.currency.getValueFor(creditEntry.amountWithVat)}" />
                    </p>
                </datatables:column>
            </datatables:table>
            <script>
				createDataTables(
						'allActiveCreditEntriesDataSetTable',
						true,
						false,
						false,
						"${pageContext.request.contextPath}",
						"${datatablesI18NUrl}");
			</script>
        </div>
    </c:when>
</c:choose>

<script>
	$(document).ready(function() {
	});
</script>
