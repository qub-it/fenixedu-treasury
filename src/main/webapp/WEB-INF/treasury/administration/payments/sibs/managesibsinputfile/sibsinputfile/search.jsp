<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
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
            code="label.administration.payments.sibs.manageSibsInputFile.searchSibsInputFile" />
        <small></small>
    </h1>
</div>
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-plus-sign" aria-hidden="true"></span>&nbsp;<a
        class=""
        href="${pageContext.request.contextPath}/treasury/administration/payments/sibs/managesibsinputfile/sibsinputfile/create"><spring:message
            code="label.event.create" /></a> &nbsp;|&nbsp;
            
	<span class="glyphicon glyphicon-download-alt" aria-hidden="true"></span>&nbsp;<a
        class=""
        href="${pageContext.request.contextPath}/treasury/administration/payments/sibs/sibspaymentsbroker"><spring:message
            code="label.SibsPaymentsBroker.import" /></a>
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



<!-- <div class="panel panel-default"> -->
<!-- <form method="get" class="form-horizontal"> -->
<!-- <div class="panel-body"> -->
<!-- <div class="form-group row"> -->
<%-- <div class="col-sm-2 control-label"><spring:message code="label.SibsInputFile.whenProcessedBySibs"/></div>  --%>

<!-- <div class="col-sm-4"> -->
<!--    <input id="sibsInputFile_whenProcessedBySibs" class="form-control" type="text" name="whenprocessedbysibs"  bennu-datetime  -->
<%--    value = '<c:out value='${not empty param.whenprocessedbysibs ? param.whenprocessedbysibs : sibsInputFile.whenProcessedBySibs }'/>' /> --%>
<!-- </div> -->
<!-- </div>      -->
<!-- <div class="form-group row"> -->
<%-- <div class="col-sm-2 control-label"><spring:message code="label.SibsInputFile.transactionsTotalAmount"/></div>  --%>

<!-- <div class="col-sm-10"> -->
<%--    <input id="sibsInputFile_transactionsTotalAmount" class="form-control" type="text" name="transactionstotalamount"  value='<c:out value='${not empty param.transactionstotalamount ? param.transactionstotalamount : sibsInputFile.transactionsTotalAmount }'/>' /> --%>
<!-- </div>  -->
<!-- </div>      -->
<!-- <div class="form-group row"> -->
<%-- <div class="col-sm-2 control-label"><spring:message code="label.SibsInputFile.totalCost"/></div>  --%>

<!-- <div class="col-sm-10"> -->
<%--    <input id="sibsInputFile_totalCost" class="form-control" type="text" name="totalcost"  value='<c:out value='${not empty param.totalcost ? param.totalcost : sibsInputFile.totalCost }'/>' /> --%>
<!-- </div>  -->
<!-- </div>      -->
<!-- </div> -->
<!-- <div class="panel-footer"> -->
<%--    <input type="submit" class="btn btn-default" role="button" value="<spring:message code="label.search" />"/> --%>
<!-- </div> -->
<!-- </form> -->
<!-- </div> -->


<c:choose>
    <c:when test="${not empty searchsibsinputfileResultsDataSet}">
        <table id="searchsibsinputfileTable"
            class="table responsive table-bordered table-hover" width="100%">
            <thead>
                <tr>
                    <%--!!!  Field names here --%>
                    <th><spring:message
                            code="label.SibsInputFile.whenUploaded" /></th>
                    <th><spring:message
                            code="label.SibsInputFile.whenProcessedBySibs" /></th>
                    <th><spring:message
                            code="label.SibsInputFile.uploader" /></th>
                    <%-- Operations Column --%>
                    <th></th>
                </tr>
            </thead>
            <tbody>

            </tbody>
        </table>
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
    var searchsibsinputfileDataSet = [
            <c:forEach items="${searchsibsinputfileResultsDataSet}" var="searchResult">
                <%-- Field access / formatting  here CHANGE_ME --%>
                {
                "DT_RowId" : '<c:out value='${searchResult.externalId}'/>',
                "whencreated" : "<c:out value='${searchResult.versioningCreationDate.toString("YYYY-MM-dd HH:mm:ss")}'/>",
                "whenprocessedbysibs" : "<c:out value='${searchResult.whenProcessedBySibs.toString("YYYY-MM-dd HH:mm:ss")}'/>",
"uploader" : "<c:out value='${searchResult.uploader.name}'/>",
"actions" :
" <a  class=\"btn btn-default btn-xs\" href=\"${pageContext.request.contextPath}/treasury/administration/payments/sibs/managesibsinputfile/sibsinputfile/search/view/${searchResult.externalId}\"><spring:message code='label.view'/></a>" +
                "" 
            },
            </c:forEach>
    ];
    
    $(document).ready(function() {

    


        var table = $('#searchsibsinputfileTable').DataTable({language : {
            url : "${datatablesI18NUrl}",           
        },
        "columns": [
{ data: 'whencreated' },
            { data: 'whenprocessedbysibs' },
            { data: 'uploader' },
            { data: 'actions',className:"all" }
            
        ],
        //CHANGE_ME adjust the actions column width if needed
        "columnDefs": [
        //54
                       { "width": "54px", "targets": 3 } 
                     ],
        "data" : searchsibsinputfileDataSet,
        //Documentation: https://datatables.net/reference/option/dom
"dom": '<"col-sm-6"l><"col-sm-3"f><"col-sm-3"T>rtip', //FilterBox = YES && ExportOptions = YES
// "dom": 'T<"clear">lrtip', //FilterBox = NO && ExportOptions = YES
//"dom": '<"col-sm-6"l><"col-sm-6"f>rtip', //FilterBox = YES && ExportOptions = NO
//"dom": '<"col-sm-6"l>rtip', // FilterBox = NO && ExportOptions = NO
        "tableTools": {
            "sSwfPath": "${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/swf/copy_csv_xls_pdf.swf"            
        }
        });
        table.columns.adjust().draw();
        
          $('#searchsibsinputfileTable tbody').on( 'click', 'tr', function () {
                $(this).toggleClass('selected');
            } );
          
    }); 
</script>

