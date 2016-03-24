<%@page
    import="org.fenixedu.treasury.ui.document.managepayments.SettlementNoteController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<%@ taglib prefix="datatables"
    uri="http://github.com/dandelion/datatables"%>

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

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message
            code="label.document.managePayments.transactionsSummary" />
        <small></small>
    </h1>
</div>
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>
    &nbsp; <a class=""
        href="${pageContext.request.contextPath}<%= SettlementNoteController.SEARCH_URL %>">
        <spring:message code="label.back" />
    </a> &nbsp;|&nbsp; <span class="glyphicon glyphicon-print"
        aria-hidden="true"></span> &nbsp; <a class="" id="printLabel2"
        href="#"
        onclick="document.getElementById('accordion').style.display = 'none';window.print();document.getElementById('accordion').style.display = 'block';">
        <spring:message code="label.print" />
    </a> &nbsp;
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

<div class="panel panel-default">
    <form method="post" class="form-horizontal">
        <div class="panel-body">
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.SettlementNote.finantialInstitution" />
                </div>

                <div class="col-sm-10">
                    <%-- Relation to side 1 drop down rendered in input --%>
                    <select id="finantial_institutions"
                        class="select2 col-sm-10"
                        name="finantialInstitution" required>
                    </select>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-4 control-label">
                    <spring:message
                        code="label.SettlementNote.documentDate" />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.SettlementNote.documentDateFrom" />
                </div>

                <%
                    pageContext.setAttribute("now",
                					new org.joda.time.DateTime().toString("yyyy-MM-dd"));
                %>

                <div class="col-sm-4">
                    <input id="settlementNote_documentDate"
                        class="form-control" type="text"
                        name="documentdatefrom" bennu-date required
                        value='<c:out value='${not empty param.documentdatefrom ? param.documentdatefrom : now }'/>' />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.SettlementNote.documentDateTo" />
                </div>

                <div class="col-sm-4">
                    <input id="settlement_documentDate"
                        class="form-control" type="text"
                        name="documentdateto" bennu-date required
                        value='<c:out value='${not empty param.documentdateto ? param.documentdateto : now }'/>' />
                </div>
            </div>
        </div>
        <div class="panel-footer">
            <input type="submit" class="btn btn-default" role="button"
                value="<spring:message code="label.process" />" />
        </div>
    </form>
</div>

<h2>
    <spring:message code="label.DebtAccount" />
</h2>
<div id="content">

    <c:choose>
        <c:when test="${not empty settlementEntriesDataSet}">
            <h3>
                <spring:message
                    code="label.administration.manageCustomer.createSettlementNote.summary" />
            </h3>
            <div class="row">
                <div class="col-sm-5">
                    <table id="paymentEntries"
                        class="table responsive table-bordered table-hover"
                        width="100%">
                        <tr>
                            <th><spring:message
                                    code="label.PaymentMethod" /></th>
                            <th align="right"><spring:message
                                    code="label.PaymentMethod.value" />
                            </th>
                        </tr>
                        <c:set var="sum" value="${ 0 }" />
                        <c:forEach var="entry"
                            items="${paymentsDataSet}">
                            <tr>
                                <td><c:out
                                        value="${ entry.key.name.content }" /></td>
                                <td align="right"><c:out
                                        value="${ finantialInstitution.currency.getValueFor(entry.value) }" /></td>
                            </tr>
                            <c:set var="sum"
                                value="${ sum + entry.value }" />
                        </c:forEach>
                        <tr>
                            <th><spring:message
                                    code="label.document.managepayments.settlementnote.transactionsSummary.paymentTotal" /></th>
                            <th align="right"><c:out
                                    value="${ finantialInstitution.currency.getValueFor(sum) }" /></th>
                        </tr>
                    </table>
                </div>
                <div class="col-sm-5">
                    <table id="reimbursementsEntries"
                        class="table responsive table-bordered table-hover"
                        width="100%">
                        <tr>
                            <th><spring:message
                                    code="label.ReimbursementMethod" />
                            </th>
                            <th><spring:message
                                    code="label.PaymentMethod.value" />
                            </th>
                        </tr>
                        <c:set var="sum" value="${ 0 }" />
                        <c:forEach var="entry"
                            items="${reimbursementsDataSet}">
                            <tr>
                                <td><c:out
                                        value="${ entry.key.name.content }" /></td>
                                <td align="right"><c:out
                                        value="${ finantialInstitution.currency.getValueFor( entry.value ) }" /></td>
                            </tr>
                            <c:set var="sum"
                                value="${ sum + entry.value }" />
                        </c:forEach>
                        <tr>
                            <th><spring:message
                                    code="label.document.managepayments.settlementnote.transactionsSummary.reimbursementTotal" /></th>
                            <th align="right"><c:out
                                    value="${ finantialInstitution.currency.getValueFor(sum) }" /></th>
                        </tr>
                    </table>
                </div>
            </div>

            <%--         <c:if test='${ not paymentsOutOfTimeWindowDataSet.isEmpty() || not reimbursementsOutOfTimeWindowDataSet.isEmpty()}'> --%>
            <%--         <h3><spring:message code="label.administration.manageCustomer.createSettlementNote.summaryOutOfTimeWindow"/></h3> --%>
            <!--         <div class="row"> -->
            <!--             <div class="col-sm-5"> -->
            <!--                 <table id="paymentEntriesOutOfTimeWindow" -->
            <!--                    class="table responsive table-bordered table-hover" width="100%"> -->
            <!--                 <tr> -->
            <!--                     <th> -->
            <%--                         <spring:message code="label.PaymentMethod" /> --%>
            <!--                     </th> -->
            <!--                     <th align="right"> -->
            <%--                         <spring:message code="label.PaymentMethod.value" /> --%>
            <!--                     </th > -->
            <!--                 </tr> -->
            <%--                 <c:set var ="sum" value="${ 0 }"/> --%>
            <%--                 <c:forEach var="entry" items="${paymentsOutOfTimeWindowDataSet}"> --%>
            <!--                     <tr> -->
            <%--                         <td><c:out value="${ entry.key.name.content }" /></td> --%>
            <%--                         <td align="right"><c:out value="${ finantialInstitution.currency.getValueFor(entry.value) }" /></td> --%>
            <!--                     </tr> -->
            <%--                     <c:set var ="sum" value="${ sum + entry.value }"/> --%>
            <%--                 </c:forEach> --%>
            <!--                     <tr>  -->
            <%--                         <th><spring:message code="label.document.managepayments.settlementnote.transactionsSummary.paymentTotal" /></th> --%>
            <%--                         <th align="right"><c:out value="${ finantialInstitution.currency.getValueFor(sum) }" /></th> --%>
            <!--                     </tr> -->
            <!--                 </table> -->
            <!--             </div> -->
            <!--             <div class="col-sm-5"> -->
            <!--                 <table id="reimbursementsEntriesOutOfTimeWindow" -->
            <!--                    class="table responsive table-bordered table-hover" width="100%"> -->
            <!--                 <tr> -->
            <!--                     <th> -->
            <%--                         <spring:message code="label.ReimbursementMethod" /> --%>
            <!--                     </th> -->
            <!--                     <th> -->
            <%--                         <spring:message code="label.PaymentMethod.value" /> --%>
            <!--                     </th> -->
            <!--                 </tr> -->
            <%--                 <c:set var ="sum" value="${ 0 }"/> --%>
            <%--                 <c:forEach var="entry" items="${reimbursementsOutOfTimeWindowDataSet}"> --%>
            <!--                     <tr> -->
            <%--                         <td><c:out value="${ entry.key.name.content }" /></td> --%>
            <%--                         <td align="right"><c:out value="${ finantialInstitution.currency.getValueFor( entry.value ) }" /></td> --%>
            <!--                     </tr> -->
            <%--                     <c:set var ="sum" value="${ sum + entry.value }"/> --%>
            <%--                 </c:forEach> --%>
            <!--                     <tr> -->
            <%--                         <th><spring:message code="label.document.managepayments.settlementnote.transactionsSummary.reimbursementTotal" /></th> --%>
            <%--                         <th align="right"><c:out value="${ finantialInstitution.currency.getValueFor(sum) }" /></th> --%>
            <!--                     </tr> -->
            <!--                 </table> -->
            <!--             </div> -->
            <!--         </div> -->
            <%--         </c:if> --%>

            <h3>
                <spring:message
                    code="label.document.managepayments.settlementnote.advancedPaymentsDetails" />
            </h3>
            <div class="row">
                <div class="col-sm-5">
                    <table id="reimbursementsEntries"
                            class="table responsive table-bordered table-hover"
                            width="100%">
                        <tr>
                            <th><spring:message code="label.document.managepayments.settlementnote.advancedPaymentsTotal" /></th>
                            <th><c:out value="${finantialInstitution.currency.getValueFor(advancedPaymentTotal)}" /></th>
                        </tr>
                    </table>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-10">
                    <datatables:table id="advancedPaymentEntries"
                        row="entry"
                        data="${advancedPaymentEntriesDataSet}"
                        cssClass="table table-bordered table-hover"
                        cdn="false" cellspacing="2" sort="false">
                        <datatables:column
                            cssStyle="width:80px;align:right">
                            <datatables:columnHead>
                                <spring:message
                                    code="label.InvoiceEntry.date" />
                            </datatables:columnHead>
                            <c:out
                                value='${entry.documentDate.toString("YYYY-MM-dd")}' />
                            <%--                             <joda:format value="${pendingEntry.entryDateTime}" style="S-" /> --%>
                        </datatables:column>

                        <datatables:column cssStyle="width:100px;">
                            <datatables:columnHead>
                                <spring:message
                                    code="label.Customer.studentNumber" />
                            </datatables:columnHead>
                            <c:out
                                value="${ entry.debtAccount.customer.businessIdentification }" />
                        </datatables:column>

                        <datatables:column cssStyle="width:100px;">
                            <datatables:columnHead>
                                <spring:message
                                    code="label.Customer.identificationNumber.short" />
                            </datatables:columnHead>
                            <c:out
                                value="${ entry.debtAccount.customer.identificationNumber }" />
                        </datatables:column>

                        <datatables:column cssStyle="width:140px;">
                            <datatables:columnHead>
                                <spring:message code="label.Customer" />
                            </datatables:columnHead>
                            <c:out
                                value="${ entry.debtAccount.customer.name }" />
                        </datatables:column>
                        <datatables:column cssStyle="width:140px;">
                            <datatables:columnHead>
                                <spring:message
                                    code="label.InvoiceEntry.finantialDocument" />
                            </datatables:columnHead>
                            <c:out value="${ entry.uiDocumentNumber }" />
                        </datatables:column>
                        <datatables:column cssStyle="width:140px;">
                            <datatables:columnHead>
                                <spring:message
                                    code="label.FinantialDocument.PaymentNote" />
                            </datatables:columnHead>
                            <c:out value="${ entry.advancedPaymentSettlementNote.uiDocumentNumber }" />
                        </datatables:column>
                        <datatables:column
                            cssStyle="width:10%;align:right">
                            <datatables:columnHead>
                                <spring:message
                                    code="label.InvoiceEntry.totalAmount" />
                            </datatables:columnHead>
                            <div align=right>
                                <c:out
                                    value="${entry.debtAccount.finantialInstitution.currency.getValueFor(entry.totalAmount)}" />
                            </div>
                        </datatables:column>
                    </datatables:table>
                    <script>
                createDataTables(
                        'advancedPaymentEntries',
                        true,
                        true,
                        true,
                        "${pageContext.request.contextPath}",
                        "${datatablesI18NUrl}");
                   </script>
                </div>
            </div>

            <h3>
                <spring:message code="label.document.managepayments.settlementnote.settlementDetails" />
            </h3>
            <div class="row">
                <div class="col-sm-10">
                    <datatables:table id="settlementEntries" row="entry"
                        data="${settlementEntriesDataSet}"
                        cssClass="table table-bordered table-hover"
                        cdn="false" cellspacing="2" sort="false">
                        <datatables:column
                            cssStyle="width:90px;align:right">
                            <datatables:columnHead>
                                <spring:message
                                    code="label.InvoiceEntry.date" />
                            </datatables:columnHead>
                            <c:out
                                value='${entry.finantialDocument.documentDate.toString("YYYY-MM-dd")}' />
                            <%--                             <joda:format value="${pendingEntry.entryDateTime}" style="S-" /> --%>
                        </datatables:column>
                        <datatables:column
                            cssStyle="width:80px;align:right">
                            <datatables:columnHead>
                                <spring:message
                                    code="label.SettlementNote.paymentDate" />
                            </datatables:columnHead>
                            <c:out
                                value='${entry.finantialDocument.paymentDate.toString("YYYY-MM-dd")}' />
                        </datatables:column>

                        <datatables:column cssStyle="width:140px;">
                            <datatables:columnHead>
                                <spring:message
                                    code="label.Customer.studentNumber" />
                            </datatables:columnHead>
                            <c:out
                                value="${ entry.finantialDocument.debtAccount.customer.businessIdentification }" />
                        </datatables:column>

                        <datatables:column cssStyle="width:140px;">
                            <datatables:columnHead>
                                <spring:message
                                    code="label.Customer.identificationNumber.short" />
                            </datatables:columnHead>
                            <c:out
                                value="${ entry.finantialDocument.debtAccount.customer.identificationNumber }" />
                        </datatables:column>

                        <datatables:column cssStyle="width:140px;">
                            <datatables:columnHead>
                                <spring:message code="label.Customer" />
                            </datatables:columnHead>
                            <c:out
                                value="${ entry.finantialDocument.debtAccount.customer.name }" />
                        </datatables:column>
                        <datatables:column cssStyle="width:140px;">
                            <datatables:columnHead>
                                <spring:message
                                    code="label.InvoiceEntry.finantialDocument" />
                            </datatables:columnHead>
                            <c:out
                                value="${ entry.finantialDocument.uiDocumentNumber }" />
                        </datatables:column>
                        <datatables:column>
                            <datatables:columnHead>
                                <spring:message
                                    code="label.InvoiceEntry.description" />
                            </datatables:columnHead>
                            <c:out value="${ entry.description}" />
                        </datatables:column>
                       

                        <datatables:column>
                            <datatables:columnHead>
                                <spring:message
                                    code="label.PaymentEntry.paymentMethod" />
                            </datatables:columnHead>
                            <c:out value="" />
                            <c:forEach var="pm"
                                items="${entry.finantialDocument.paymentEntriesSet}">
                                <p>
                                    <c:out
                                        value="${pm.paymentMethod.name.content}" />
                                </p>
                            </c:forEach>
                        </datatables:column>

                        <datatables:column
                            cssStyle="width:10%;align:right">
                            <datatables:columnHead>
                                <spring:message
                                    code="label.InvoiceEntry.totalAmount" />
                            </datatables:columnHead>
                            <div align=right>
                                <c:if
                                    test="${ not entry.invoiceEntry.isDebitNoteEntry() }">
                                    <c:out
                                        value="${entry.finantialDocument.debtAccount.finantialInstitution.currency.getValueFor(entry.totalAmount.negate())}" />
                                </c:if>
                                <c:if
                                    test="${ entry.invoiceEntry.isDebitNoteEntry() }">
                                    <c:out
                                        value="${entry.finantialDocument.debtAccount.finantialInstitution.currency.getValueFor(entry.totalAmount)}" />
                                </c:if>
                            </div>
                        </datatables:column>
                    </datatables:table>
                    <script>
                createDataTables(
                        'settlementEntries',
                        true,
                        true,
                        true,
                        "${pageContext.request.contextPath}",
                        "${datatablesI18NUrl}");
            </script>
                </div>
            </div>
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

<script>	

$(document).ready(function() {
    var finantial_institutions_options = [
      <c:forEach items="${finantial_institutions_options}" var="element"> 
      {
          text :"<c:out value='${element.name}'/>",  
          id : "<c:out value='${element.externalId}'/>" 
      },
      </c:forEach>
    ];
    
    $("#finantial_institutions").select2(
        {
            data : finantial_institutions_options,
        }     
    );
    
    $("#finantial_institutions").select2().select2('val', '<c:out value='${param.vattype}'/>');	
}); 
</script>

