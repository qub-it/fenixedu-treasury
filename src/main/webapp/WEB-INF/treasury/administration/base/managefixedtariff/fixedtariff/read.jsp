<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>

<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js" />
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css" />

<link rel="stylesheet" href="${datatablesCssUrl}" />
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css" />

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
${portal.angularToolkit()}
<%--${portal.toolkit()}--%>

<link href="${pageContext.request.contextPath}/static/treasury/css/dataTables.responsive.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/static/treasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<script src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>

<script src="${pageContext.request.contextPath}/webjars/angular-sanitize/1.3.11/angular-sanitize.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.css" />
<script src="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.js"></script>


<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.administration.base.manageFixedTariff.readFixedTariff" />
        <small></small>
    </h1>
</div>
<div class="modal fade" id="deleteModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/administration/base/managefixedtariff/fixedtariff/delete/${fixedTariff.externalId}"
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
                        <spring:message code="label.administration.base.manageFixedTariff.readFixedTariff.confirmDelete" />
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <spring:message code="label.close" />
                    </button>
                    <button id="deleteButton" class="btn btn-danger" type="submit">
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
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a class=""
        href="${pageContext.request.contextPath}/treasury/administration/base/manageproduct/product/read/${fixedTariff.product.externalId}"><spring:message
            code="label.event.back" /></a> &nbsp;|&nbsp; <span class="glyphicon glyphicon-trash" aria-hidden="true"></span>&nbsp;<a class="" href="#" data-toggle="modal"
        data-target="#deleteModal"><spring:message code="label.event.delete" /></a> &nbsp;|&nbsp; <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>&nbsp;<a
        class="" href="${pageContext.request.contextPath}/treasury/administration/base/managefixedtariff/fixedtariff/update/${fixedTariff.externalId}"><spring:message
            code="label.event.update" /></a> &nbsp;
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
                        <th scope="row" class="col-xs-3"><spring:message code="label.FixedTariff.finantialEntity" /></th>
                        <td><c:out value='${fixedTariff.finantialEntity.name.content}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FixedTariff.product" /></th>
                        <td><c:out value='${fixedTariff.product.code} - ${fixedTariff.product.name.content}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FixedTariff.amount" /></th>
                        <td><c:out value='${fixedTariff.uiTariffDescription.content}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FixedTariff.vatType" /></th>
                        <td><c:out value='${fixedTariff.product.vatType.name.content}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FixedTariff.beginDate" /></th>
                        <td><joda:format value="${fixedTariff.beginDate}" style="S-" /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FixedTariff.endDate" /></th>
                        <td><joda:format value="${fixedTariff.endDate}" style="S-" /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FixedTariff.dueDateCalculationType" /></th>
                        <td><c:out value='${fixedTariff.dueDateCalculationType.descriptionI18N.content}' /></td>
                    </tr>
                    <c:if test="${ fixedTariff.dueDateCalculationType == 'FIXED_DATE'}">
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.FixedTariff.fixedDueDate" /></th>
                            <td><c:out value='${fixedTariff.fixedDueDate}' /></td>
                        </tr>
                    </c:if>
                    <c:if test="${ fixedTariff.dueDateCalculationType == 'DAYS_AFTER_CREATION'}">
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.FixedTariff.numberOfDaysAfterCreationForDueDate" /></th>
                            <td><c:out value='${fixedTariff.numberOfDaysAfterCreationForDueDate}' /></td>
                        </tr>
                    </c:if>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.FixedTariff.applyInterests" /></th>
                        <td><c:if test="${fixedTariff.applyInterests}">
                                <spring:message code="label.true" />
                            </c:if> <c:if test="${not fixedTariff.applyInterests}">
                                <spring:message code="label.false" />
                            </c:if></td>
                    </tr>
                    <c:if test="${ fixedTariff.applyInterests}">
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.FixedTariff.interestType" /></th>
                            <td><c:out value='${fixedTariff.interestRate.interestType.descriptionI18N.content}' /></td>
                        </tr>
                        <c:if test="${fixedTariff.interestRate.interestType=='DAILY'}">
                            <tr>
                                <th scope="row" class="col-xs-3"><spring:message code="label.FixedTariff.numberOfDaysAfterDueDate" /></th>
                                <td><c:out value='${fixedTariff.interestRate.numberOfDaysAfterDueDate}' /></td>
                            </tr>
                            <tr>
                                <th scope="row" class="col-xs-3"><spring:message code="label.InterestRate.applyInFirstWorkday" /></th>
                                <td><c:if test="${fixedTariff.interestRate.applyInFirstWorkday}">
                                        <spring:message code="label.true" />
                                    </c:if> <c:if test="${not fixedTariff.interestRate.applyInFirstWorkday}">
                                        <spring:message code="label.false" />
                                    </c:if></td>
                            </tr>
                            <tr>
                                <th scope="row" class="col-xs-3"><spring:message code="label.InterestRate.maximumDaysToApplyPenalty" /></th>
                                <td><c:out value='${fixedTariff.interestRate.maximumDaysToApplyPenalty}' /></td>
                            </tr>
                        </c:if>
                        <c:if test="${fixedTariff.interestRate.interestType=='FIXED_AMOUNT' }">
                            <tr>
                                <th scope="row" class="col-xs-3"><spring:message code="label.InterestRate.interestFixedAmount" /></th>
                                <td><c:out value='${fixedTariff.interestRate.interestFixedAmount}' /></td>
                            </tr>
                        </c:if>

                        <c:if test="${ fixedTariff.interestRate.interestType != 'FIXED_AMOUNT' }">
                            <c:if test="${ fixedTariff.interestRate.interestType != 'GLOBAL_RATE' }">
                                <tr>
                                    <th scope="row" class="col-xs-3"><spring:message code="label.InterestRate.rate" /></th>
                                    <td><c:out value='${fixedTariff.interestRate.rate}' /></td>
                                </tr>
                            </c:if>
                        </c:if>
                    </c:if>
                </tbody>
            </table>
        </form>
    </div>
</div>

<script>
	$(document).ready(function() {

	});
</script>
