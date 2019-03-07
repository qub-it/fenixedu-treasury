<%@page import="org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory"%>
<%@page import="org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI"%>
<%@page import="java.security.AccessControlContext"%>
<%@page import="org.fenixedu.bennu.core.groups.Group"%>
<%@page import="org.fenixedu.bennu.core.security.Authenticate"%>
<%@page import="org.fenixedu.treasury.ui.accounting.managecustomer.CustomerController"%>
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
        <spring:message code="label.accounting.manageCustomer.readCustomer" />
    </h1>
    <small></small>
</div>
<div class="modal fade" id="deleteModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/accounting/managecustomer/customer/delete/${customer.externalId}" method="POST">
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
                        <spring:message code="label.accounting.manageCustomer.readCustomer.confirmDelete" />
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
<form>
    <div class="well well-sm" style="display: inline-block">
        <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>
        &nbsp;
        <a href="${pageContext.request.contextPath}<%= CustomerController.SEARCH_FULL_URI %>">
            <spring:message code="label.back" />
        </a>
        &nbsp;|&nbsp;
        <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>
        &nbsp;
        <a href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/adhoccustomer/update/${customer.externalId}">
            <spring:message code="label.event.update" />
        </a>
        <% if(Group.parse("#accountManagers").isMember( Authenticate.getUser() )) { %>
            &nbsp;|&nbsp;              
            <span class="glyphicon glyphicon-trash" aria-hidden="true"></span>
            &nbsp;
            <a class="" href="#" data-toggle="modal" data-target="#deleteModal">
                <spring:message code="label.event.delete" />
            </a>        
        <% } %>
	</div>
</form>

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
                    <c:if test='${customer.isPersonCustomer() }'>
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.Customer.code" /></th>
                            <td><c:out value='${customer.businessIdentification}' /></td>
                        </tr>
                    </c:if>
                    <c:if test='${customer.isAdhocCustomer() }'>
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.Customer.code" /></th>
                            <td><c:out value='${customer.code}' /></td>
                        </tr>
                    </c:if>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.name" /></th>
                        <td><c:out value='${customer.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.identificationNumber" /></th>
                        <td><c:out value='${customer.identificationNumber}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.countryCode" /></th>
                        <td><c:out value='${customer.fiscalCountry}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.fiscalNumber" /></th>
                        <td><c:out value='${customer.fiscalNumber}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.addressCountryCode" /></th>
                        <td><c:out value='${customer.addressCountryCode}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.address" /></th>
                        <td><c:out value='${customer.address}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.districtSubdivision" /></th>
                        <td><c:out value='${customer.districtSubdivision}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.zipCode" /></th>
                        <td><c:out value='${customer.zipCode}' /></td>
                    </tr>
                    
                    <c:if test="${customer.isIbanDefined()}">
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.iban" /></th>
                        <td>
                        	<c:out value='${customer.iban}' />
                        	&nbsp;
                        	<em>(<spring:message code="label.Customer.iban.remarks" />)</em>
                        </td>
                    </tr>
                    </c:if>
                    
                </tbody>
            </table>

        </form>
    </div>
</div>

<div class="panel panel-primary">
    <div class="panel-heading">
        <h3 class="panel-title">
            <spring:message code="label.Customer.debtAccountsBalances" />
        </h3>
    </div>
    <div class="panel-body">
        <form method="post" class="form-horizontal">
            <table class="table">
                <tbody>
			                   <c:forEach var="debtAccount" items='${customer.debtAccountsSet}'>
			                       <tr>
			                           <th scope="row" class="col-xs-2">
			                           	<c:out value="${debtAccount.finantialInstitution.name}" />
			                           </th>
			                           <td class="col-xs-1">
			                           		<p><c:out value="${debtAccount.customer.uiFiscalNumber}" /></p>
			                           		<c:if test="${debtAccount.customer.personCustomer}">
			                           		<c:if test="${debtAccount.customer.fromPersonMerge}">
				                           		<p><small><em>[<spring:message code="label.Customer.fromPersonMerge" />]</em></small></p>
			                           		</c:if>
			                           		</c:if>
			                           </td>
			                           <td class="col-xs-4">
			                           		<c:out value="${debtAccount.customer.uiCompleteAddress}" />
			                           </td>
			                           <td class="col-xs-1">
		                                   <span>
		                                   	<c:out value="${debtAccount.finantialInstitution.currency.getValueFor(debtAccount.totalInDebt + debtAccount.calculatePendingInterestAmount())}" />
			                               </span>
			                               <c:if test="${debtAccount.totalInDebt < 0 }">
			                                   <p>
			                                   	<span class="label label-primary">
			                                   		<spring:message code="label.DebtAccount.customerHasAmountToRehimburse" />
			                                   	</span>
			                                   </p>
			                               </c:if> 
			                           </td>
			                           <td class="col-xs-2">
			                               	<a class="btn btn-primary btn-xs" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${debtAccount.externalId}">
				                               	<span class="glyphicon glyphicon-user">&nbsp;</span>
				                               	<spring:message code="label.customer.read.showdebtaccount"></spring:message>
			                               	</a>
			                               
											<c:if test="${debtAccount.customer.ableToChangeFiscalNumber}">
											<% if (TreasuryAccessControlAPI.isBackOfficeMember(TreasuryPlataformDependentServicesFactory.implementation().getLoggedUsername())) { %>
											
											<c:choose>
												<c:when test="${debtAccount.customer.personCustomer}">
													<c:set var="changeFiscalNumberUrl" value="/academictreasury/accounting/managecustomer/changefiscalnumber/changefiscalnumberactionconfirm" />
												</c:when>
												<c:when test="${debtAccount.customer.adhocCustomer}">
													<c:set var="changeFiscalNumberUrl" value="/treasury/accounting/managecustomer/changefiscalnumber/changefiscalnumberactionconfirm" />
												</c:when>
											</c:choose>
											
											&nbsp;
											<a class="btn btn-primary btn-xs" 
												href="${pageContext.request.contextPath}${changeFiscalNumberUrl}/${debtAccount.customer.externalId}">
												<span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>
												&nbsp;
												<spring:message code="label.Customer.changeFiscalNumber" />
											</a>
											<% } %>
											</c:if>
			                                
			                               <c:if test="${debtAccount.closed}">
			                                   <span class="label label-warning"><spring:message code="warning.DebtAccount.is.closed" /></span>
			                               </c:if>
			                           </td>
			                       </tr>
			                   </c:forEach>
			                   
						<c:if test="${customer.personCustomer}">
			                   <c:forEach var="inactiveCustomer" items='${customer.person.inactivePersonCustomersSet}'>
				                   <c:forEach var="debtAccount" items='${inactiveCustomer.debtAccountsSet}'>
				                       <tr>
				                           <th scope="row" class="col-xs-2">
				                           	<c:out value="${debtAccount.finantialInstitution.name}" />
			                               <c:if test="${!inactiveCustomer.active}">
			                                   <p>
													<span class="label label-warning">
														<spring:message code="warning.Customer.is.inactive.due.merge.or.fiscal.change" />
													</span>
			                                   </p>
			                               </c:if>
				                           </th>
				                           <td class="col-xs-1">
				                           		<p>
					                           		<c:out value="${debtAccount.customer.uiFiscalNumber}" />
				                           		</p>
				                           		<c:if test="${debtAccount.customer.personCustomer}">
				                           		<c:if test="${debtAccount.customer.fromPersonMerge}">
				                           		<p><small><em>[<spring:message code="label.Customer.fromPersonMerge" />]</em></small></p>
				                           		</c:if>
				                           		</c:if>
				                           </td>
					                       <td class="col-xs-4">
					                       		<c:out value="${debtAccount.customer.uiCompleteAddress}" />
					                       </td>
					                       <td class="col-xs-1">
			                                   <span>
			                                   	<c:out value="${debtAccount.finantialInstitution.currency.getValueFor(debtAccount.totalInDebt + debtAccount.calculatePendingInterestAmount())}" />
			                                   </span>
												<c:if test="${debtAccount.totalInDebt < 0 }">
													<p>
														<span class="label label-primary">
															<spring:message code="label.DebtAccount.customerHasAmountToRehimburse" />
														</span>
													</p>
				                               </c:if> 
					                       </td>
				                           <td class="col-xs-2">
				                               <a class="btn btn-primary btn-xs" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${debtAccount.externalId}">
					                               <span class="glyphicon glyphicon-user" >&nbsp;</span>
					                               <spring:message code="label.customer.read.showdebtaccount"></spring:message>
												</a>
												
			                               		<c:if test="${debtAccount.customer.personCustomer}">
												<% if (TreasuryAccessControlAPI.isBackOfficeMember(TreasuryPlataformDependentServicesFactory.implementation().getLoggedUsername())) { %>
												<c:if test="${debtAccount.customer.ableToChangeFiscalNumber}">
												&nbsp;
												
												<c:choose>
													<c:when test="${debtAccount.customer.personCustomer}">
														<c:set var="changeFiscalNumberUrl" value="/academictreasury/accounting/managecustomer/changefiscalnumber/changefiscalnumberactionconfirm" />
													</c:when>
													<c:when test="${debtAccount.customer.adhocCustomer}">
														<c:set var="changeFiscalNumberUrl" value="/treasury/accounting/managecustomer/changefiscalnumber" />
													</c:when>
												</c:choose>
												
												<a class="btn btn-primary btn-xs" 
													href="${pageContext.request.contextPath}${changeFiscalNumberUrl}/${debtAccount.customer.externalId}">
													<span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>
													&nbsp;
													<spring:message code="label.Customer.changeFiscalNumber" />
												</a>
												
												</c:if>
												<% } %>

												&nbsp;
												<a class="btn btn-primary btn-xs" 
													href="${pageContext.request.contextPath}/academictreasury/accounting/managecustomer/changefiscalnumber/updateFiscalAddress/${debtAccount.customer.externalId}">
													<span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>
													&nbsp;
													<spring:message code="label.PersonCustomer.update.fiscal.address" />
												</a>

												</c:if>
												 
				                               <c:if test="${debtAccount.closed}">
				                                   <span class="label label-warning"><spring:message code="warning.DebtAccount.is.closed" /></span>
				                               </c:if>
				                           </td>
				                       </tr>
				                   </c:forEach>
			                   </c:forEach>
						</c:if>
			                   
                </tbody>
            </table>

        </form>
    </div>
</div>

<script>
	$(document).ready(function() {});
</script>
