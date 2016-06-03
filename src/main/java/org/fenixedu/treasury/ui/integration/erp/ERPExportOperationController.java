/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: ricardo.pedro@qub-it.com, anil.mamede@qub-it.com
 *
 * 
 * This file is part of FenixEdu Treasury.
 *
 * FenixEdu Treasury is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Treasury is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Treasury.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.treasury.ui.integration.erp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.services.integration.erp.IERPExporter;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

import com.google.common.base.Strings;

//@Component("org.fenixedu.treasury.ui.integration.erp") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.integration.erp.export",
        accessGroup = "treasuryManagers")
@RequestMapping(ERPExportOperationController.CONTROLLER_URL)
public class ERPExportOperationController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/integration/erp/erpexportoperation";

    public static final long SEARCH_OPERATION_LIST_LIMIT_SIZE = 3000;

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/";
    }

    private ERPExportOperation getERPExportOperation(Model model) {
        return (ERPExportOperation) model.asMap().get("eRPExportOperation");
    }

    private void setERPExportOperation(ERPExportOperation eRPExportOperation, Model model) {
        model.addAttribute("eRPExportOperation", eRPExportOperation);
    }

    @Atomic
    public void deleteERPExportOperation(ERPExportOperation eRPExportOperation) {
        eRPExportOperation.delete();
    }

//				
    private static final String _SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI)
    public String search(
            @RequestParam(value = "finantialinstitution", required = false) FinantialInstitution finantialInstitution,
            @RequestParam(value = "fromexecutiondate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime fromExecutionDate,
            @RequestParam(value = "toexecutiondate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime toExecutionDate,
            @RequestParam(value = "success", required = false) Boolean success, @RequestParam(value = "documentnumber",
                    required = false) String documentNumber, Model model) {
        List<ERPExportOperation> searcherpexportoperationResultsDataSet =
                filterSearchERPExportOperation(finantialInstitution, fromExecutionDate, toExecutionDate, success, documentNumber);
        model.addAttribute("limit_exceeded", searcherpexportoperationResultsDataSet.size() > SEARCH_OPERATION_LIST_LIMIT_SIZE);
        model.addAttribute("searchoperationResultsDataSet_totalCount", searcherpexportoperationResultsDataSet.size());
        searcherpexportoperationResultsDataSet =
                searcherpexportoperationResultsDataSet.stream().limit(SEARCH_OPERATION_LIST_LIMIT_SIZE)
                        .collect(Collectors.toList());

        model.addAttribute("finantialInstitutionList", FinantialInstitution.findAll().collect(Collectors.toList()));
        model.addAttribute("searcherpexportoperationResultsDataSet", searcherpexportoperationResultsDataSet);
        return "treasury/integration/erp/erpexportoperation/search";
    }

    private Stream<ERPExportOperation> getSearchUniverseSearchERPExportOperationDataSet() {
        return ERPExportOperation.findAll();

    }

    private List<ERPExportOperation> filterSearchERPExportOperation(FinantialInstitution finantialInstitution,
            DateTime fromExecutionDate, DateTime toExecutionDate, Boolean success, String documentNumber) {

        if (Strings.isNullOrEmpty(documentNumber)) {

            return getSearchUniverseSearchERPExportOperationDataSet()
                    .filter(eRPExportOperation -> finantialInstitution == null
                            || eRPExportOperation.getFinantialInstitution().equals(finantialInstitution))
                    .filter(eRPExportOperation -> fromExecutionDate == null || toExecutionDate == null
                            || eRPExportOperation.getExecutionDate().isAfter(fromExecutionDate)
                            && eRPExportOperation.getExecutionDate().isBefore(toExecutionDate))
                    .filter(eRPExportOperation -> success == null || eRPExportOperation.getSuccess() == success)
//                    .limit(EXPORT_OPERATIONS_MAX_SIZE)
                    .collect(Collectors.toList());
        } else {
            FinantialDocument document = FinantialDocument.findByUiDocumentNumber(finantialInstitution, documentNumber);
            if (document != null) {
                return document
                        .getErpExportOperationsSet()
                        .stream()
                        .filter(eRPExportOperation -> fromExecutionDate == null || toExecutionDate == null
                                || eRPExportOperation.getExecutionDate().isAfter(fromExecutionDate)
                                && eRPExportOperation.getExecutionDate().isBefore(toExecutionDate))
                        .filter(eRPExportOperation -> success == null || eRPExportOperation.getSuccess() == success)
//                        .limit(EXPORT_OPERATIONS_MAX_SIZE)
                        .collect(Collectors.toList());
            } else {
                return new ArrayList<ERPExportOperation>();
            }
        }
    }

    private static final String _SEARCH_TO_DELETEMULTIPLE_URI = "/search/deletemultiple";
    public static final String SEARCH_TO_DELETEMULTIPLE_URL = CONTROLLER_URL + _SEARCH_TO_DELETEMULTIPLE_URI;

    @RequestMapping(value = _SEARCH_TO_DELETEMULTIPLE_URI)
    public String processSearchToDeleteMultiple(
            @RequestParam("eRPExportOperations") List<ERPExportOperation> eRPExportOperations, Model model,
            RedirectAttributes redirectAttributes) {

        return redirect(SEARCH_URL, model, redirectAttributes);
    }

    private static final String _SEARCH_TO_VIEW_ACTION_URI = "/search/view/";
    public static final String SEARCH_TO_VIEW_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_VIEW_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_VIEW_ACTION_URI + "{oid}")
    public String processSearchToViewAction(@PathVariable("oid") ERPExportOperation eRPExportOperation, Model model,
            RedirectAttributes redirectAttributes) {
        return redirect(READ_URL + eRPExportOperation.getExternalId(), model, redirectAttributes);
    }

    private static final String _READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + _READ_URI;

    @RequestMapping(value = _READ_URI + "{oid}")
    public String read(@PathVariable("oid") ERPExportOperation eRPExportOperation, Model model) {
        setERPExportOperation(eRPExportOperation, model);
        return "treasury/integration/erp/erpexportoperation/read";
    }

    private static final String _DELETE_URI = "/delete/";
    private static final String _DELETE_MULTIPLE_URI = "/deletemultiple/";
    public static final String DELETE_URL = CONTROLLER_URL + _DELETE_URI;

    @RequestMapping(value = _DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") ERPExportOperation eRPExportOperation, Model model,
            RedirectAttributes redirectAttributes) {

        setERPExportOperation(eRPExportOperation, model);
        try {
            assertUserIsBackOfficeMember(eRPExportOperation.getFinantialInstitution(), model);

            deleteERPExportOperation(eRPExportOperation);

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.delete"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + ex.getLocalizedMessage(), model);
        }

        return redirect(READ_URL + getERPExportOperation(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = _DELETE_MULTIPLE_URI, method = RequestMethod.POST)
    public String deleteMultiple(@RequestParam("operations") List<ERPExportOperation> eRPExportOperations, Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (eRPExportOperations.isEmpty() == false) {
                assertUserIsBackOfficeMember(eRPExportOperations.get(0).getFinantialInstitution(), model);
            }
            for (ERPExportOperation operation : eRPExportOperations) {
                deleteERPExportOperation(operation);
            }
            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.delete"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + ex.getLocalizedMessage(), model);
        }
        return redirect(SEARCH_URL, model, redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/downloadfile")
    public void processReadToDownloadFile(@PathVariable("oid") ERPExportOperation eRPExportOperation, Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) {
        setERPExportOperation(eRPExportOperation, model);
        try {
            assertUserIsFrontOfficeMember(eRPExportOperation.getFinantialInstitution(), model);

            response.setContentType(eRPExportOperation.getFile().getContentType());
            String filename = eRPExportOperation.getFile().getFilename();
            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            response.getOutputStream().write(eRPExportOperation.getFile().getContent());
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            try {
                response.sendRedirect(redirect(READ_URL + getERPExportOperation(model).getExternalId(), model, redirectAttributes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/read/{oid}/retryimport")
    public String processReadToRetryImport(@PathVariable("oid") ERPExportOperation eRPExportOperation, Model model,
            RedirectAttributes redirectAttributes) {
        setERPExportOperation(eRPExportOperation, model);
        try {
            assertUserIsFrontOfficeMember(eRPExportOperation.getFinantialInstitution(), model);
            final IERPExporter erpExporter = eRPExportOperation.getFinantialInstitution().getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPExporter();
            
            ERPExportOperation retryExportOperation = erpExporter.retryExportToIntegration(eRPExportOperation);
            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.integration.erp.exportoperation.success"), model);

            return redirect(READ_URL + retryExportOperation.getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + ex.getLocalizedMessage(), model);
        }
        return redirect(READ_URL + eRPExportOperation.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = "/soapoutboundmessage/{oid}")
    public void soapOutboundMessage(@PathVariable("oid") ERPExportOperation eRPExportOperation, Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) {
        setERPExportOperation(eRPExportOperation, model);
        try {
            assertUserIsFrontOfficeMember(eRPExportOperation.getFinantialInstitution(), model);

            response.setContentType(com.google.common.net.MediaType.XML_UTF_8.toString());
            response.setHeader("Content-disposition",
                    String.format("attachment; filename=SOAP_Outbound_Message_%s.xml", eRPExportOperation.getExternalId()));
            response.getWriter().write(
                    eRPExportOperation.getSoapOutboundMessage() != null ? eRPExportOperation.getSoapOutboundMessage() : "");
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            try {
                response.sendRedirect(redirect(READ_URL + getERPExportOperation(model).getExternalId(), model, redirectAttributes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/soapinboundmessage/{oid}")
    public void soapInboundMessage(@PathVariable("oid") ERPExportOperation eRPExportOperation, Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) {
        setERPExportOperation(eRPExportOperation, model);
        try {
            assertUserIsFrontOfficeMember(eRPExportOperation.getFinantialInstitution(), model);

            response.setContentType(com.google.common.net.MediaType.XML_UTF_8.toString());
            response.setHeader("Content-disposition",
                    String.format("attachment; filename=SOAP_Inbound_Message_%s.xml", eRPExportOperation.getExternalId()));
            response.getWriter().write(
                    eRPExportOperation.getSoapInboundMessage() != null ? eRPExportOperation.getSoapInboundMessage() : "");
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            try {
                response.sendRedirect(redirect(READ_URL + getERPExportOperation(model).getExternalId(), model, redirectAttributes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
