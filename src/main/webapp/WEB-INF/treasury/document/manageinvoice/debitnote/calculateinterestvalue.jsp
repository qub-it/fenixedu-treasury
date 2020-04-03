<%@page import="org.fenixedu.treasury.ui.document.manageinvoice.DebitNoteController"%>
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
<script src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.document.manageInvoice.calculateInterestValue" />
        <small></small>
    </h1>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;
        <a class="" href="${pageContext.request.contextPath}<%=DebitNoteController.READ_URL%>${debitNote.externalId}"><spring:message code="label.event.back" /></a>
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

<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">
            <spring:message code="label.details" />
        </h3>
    </div>
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
                <spring:message code="label.CreditNote.debtAccount" />
            </div>

            <div class="col-sm-10">
                <div class="form-control">
                    <c:out value="${debitNote.debtAccount.customer.businessIdentification} - ${debitNote.debtAccount.customer.name}" />
                </div>
            </div>
        </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.CreditNote.debitNote" />
                </div>

                <div class="col-sm-10">
                    <div class="form-control">
                        <c:out value='${debitNote.uiDocumentNumber}' />
                    </div>
                </div>
            </div>

    </div>
</div>

<c:choose>
    <c:when test="${not empty interestRateValues}">
    
                                
    
    
        <datatables:table id="interestRateValues" row="interestEntryBean" data="${interestRateValues}" cssClass="table responsive table-bordered table-hover" cdn="false" cellspacing="2">
            <datatables:column sortInit="desc">
                <datatables:columnHead>
                        <spring:message code="label.InterestEntry.description" />
                </datatables:columnHead>
                <spring:message code="label.InterestEntry.interest" />: &nbsp;<c:out value="${ interestEntryBean.debitEntry.description }" />
            </datatables:column>

            <datatables:column cssStyle="width:25%">
                <datatables:columnHead>
                        <spring:message code="label.InterestEntry.interestDescription" />
                </datatables:columnHead>
                                <p>
                                    <strong><spring:message code="label.InterestEntry.calculatedInterest" /> </strong>
                                </p>
                                <p>&nbsp;</p> <c:forEach var="detail" items="${interestEntryBean.interest.interestInformationList}">
                                    <p>
                                        [
                                        <joda:format value="${detail.begin}" style="S-" />
                                        -
                                        <joda:format value="${detail.end}" style="S-" />
                                        ]: ${debitNote.debtAccount.finantialInstitution.currency.getValueFor(detail.amount, 4)}
                                    </p>
                                    <p style="">
                                        <em><spring:message code="label.InterestEntry.affectedAmount.description"
                                                arguments="${debitNote.debtAccount.finantialInstitution.currency.getValueFor(detail.affectedAmount)},${detail.numberOfDays},${detail.interestRate}" /></em>
                                    </p>
                                    <p>&nbsp;</p>
                                </c:forEach>
                                <p>&nbsp;</p> <c:if test='${ not empty  interestEntryBean.interest.createdInterestEntriesList}'>
                                    <p>
                                        <strong><spring:message code="label.InterestEntry.createdInterest" /> </strong>
                                    </p>
                                    <p>&nbsp;</p>
                                    <c:forEach var="interestEntry" items="${interestEntryBean.interest.createdInterestEntriesList}">
                                        <p>
                                            [
                                            <joda:format value="${interestEntry.entryDate}" style="S-" />
                                            ]: ${debitNote.debtAccount.finantialInstitution.currency.getValueFor(interestEntry.amount)}
                                        </p>
                                    </c:forEach>
                                </c:if>
            </datatables:column>

            <datatables:column  cssStyle="width:10%">
                <datatables:columnHead>
                        <spring:message code="label.InterestEntry.date" />
                </datatables:columnHead>
                            <c:out value="${interestEntryBean.documentDueDate}" />
            </datatables:column>

            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                        <spring:message code="label.InterestEntry.amount" />
                </datatables:columnHead>
                            <c:out value="${ debitNote.debtAccount.finantialInstitution.currency.getValueFor(interestEntryBean.interest.interestAmount) }" />
            </datatables:column>
        </datatables:table>

        <script>
                                    createDataTables(
                                            'interestRateValues',
                                            false,
                                            false,
                                            false,
                                            "${pageContext.request.contextPath}",
                                            "${datatablesI18NUrl}");
                                </script>
                   </br></br>             
                                <form method="post" class="form-horizontal">
<input type="hidden" name="paymentdate" value="${paymentDate.toString("YYYY-MM-dd")}" />
<input type="hidden" name="debitnote" value="${debitNote.externalId}" />
    <div class="panel panel-primary">
    <div class="panel-heading ">
    <spring:message code="label.DebitNote.calculateInterestValueHeader" />
    </div>
        <div class="panel-body">
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
                    <spring:message code="label.DebitNote.documentObservations" />
                </div>

                <div class="col-sm-10">
                    <input id="debitNote_documentObservations" class="form-control" type="text" name="documentobservations"
                       required value='' />
                </div>
            </div>

        </div>

        <div class="panel-footer">
        
        <a href="${pageContext.request.contextPath}<%=DebitNoteController.READ_URL%>${debitNote.externalId}" class="btn btn-default"> 
        <span class="glyphicon glyphicon-remove" aria-hidden="true" >&nbsp;</span><spring:message code="label.cancel" /> 
        </a>
        
        <button type="submit" class="btn btn-primary">
            <span class="glyphicon glyphicon-ok" aria-hidden="true" >&nbsp;</span><spring:message code="label.DebitNote.generateInterest" />
        </button>
        </div>
    </div>
</form>


<script>
    $(document).ready(function() {
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
        
        
        
        $("#debitNote_documentNumberSeries").select2();
    
        <%-- End block for providing documentNumberSeries options --%>
    });
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


