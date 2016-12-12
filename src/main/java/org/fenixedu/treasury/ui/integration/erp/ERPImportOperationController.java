/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and ServiÃ§os Partilhados da
 * Universidade de Lisboa:
 *  - Copyright Â© 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright Â© 2015 Universidade de Lisboa (after any Go-Live phase)
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
import org.fenixedu.treasury.domain.integration.ERPImportOperation;
import org.fenixedu.treasury.services.integration.erp.IERPImporter;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Strings;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

//@Component("org.fenixedu.treasury.ui.integration.erp") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.integration.erp.import",
        accessGroup = "treasuryManagers")
@RequestMapping(ERPImportOperationController.CONTROLLER_URL)
public class ERPImportOperationController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/integration/erp/erpimportoperation";

    public static final long SEARCH_OPERATION_LIST_LIMIT_SIZE = 3000;

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/";
    }

    private ERPImportOperation getERPImportOperation(Model model) {
        return (ERPImportOperation) model.asMap().get("eRPImportOperation");
    }

    private void setERPImportOperation(ERPImportOperation eRPImportOperation, Model model) {
        model.addAttribute("eRPImportOperation", eRPImportOperation);
    }

    @Atomic
    public void deleteERPImportOperation(ERPImportOperation eRPImportOperation) {
        eRPImportOperation.delete();
    }

    private static final String _SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI)
    public String search(
            @RequestParam(value = "finantialinstitution", required = false) FinantialInstitution finantialInstitution,
            @RequestParam(value = "fromexecutiondate",
                    required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime fromExecutionDate,
            @RequestParam(value = "toexecutiondate",
                    required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime toExecutionDate,
            @RequestParam(value = "success", required = false) Boolean success,
            @RequestParam(value = "documentnumber", required = false) String documentNumber, Model model) {
        List<ERPImportOperation> searcherpimportoperationResultsDataSet =
                filterSearchERPImportOperation(finantialInstitution, fromExecutionDate, toExecutionDate, success, documentNumber);
        model.addAttribute("limit_exceeded", searcherpimportoperationResultsDataSet.size() > SEARCH_OPERATION_LIST_LIMIT_SIZE);
        model.addAttribute("searchoperationResultsDataSet_totalCount", searcherpimportoperationResultsDataSet.size());
        searcherpimportoperationResultsDataSet = searcherpimportoperationResultsDataSet.stream()
                .limit(SEARCH_OPERATION_LIST_LIMIT_SIZE).collect(Collectors.toList());

        model.addAttribute("finantialInstitutionList", FinantialInstitution.findAll().collect(Collectors.toList()));
        model.addAttribute("searcherpimportoperationResultsDataSet", searcherpimportoperationResultsDataSet);
        return "treasury/integration/erp/erpimportoperation/search";
    }

    private Stream<ERPImportOperation> getSearchUniverseSearchERPImportOperationDataSet() {
        return ERPImportOperation.findAll();
    }

    private List<ERPImportOperation> filterSearchERPImportOperation(FinantialInstitution finantialInstitution,
            DateTime fromExecutionDate, DateTime toExecutionDate, Boolean success, String documentNumber) {

        if (Strings.isNullOrEmpty(documentNumber)) {

            return getSearchUniverseSearchERPImportOperationDataSet()
                    .filter(eRPImportOperation -> finantialInstitution == null
                            || eRPImportOperation.getFinantialInstitution().equals(finantialInstitution))
                    .filter(eRPImportOperation -> fromExecutionDate == null || toExecutionDate == null
                            || eRPImportOperation.getExecutionDate().isAfter(fromExecutionDate)
                                    && eRPImportOperation.getExecutionDate().isBefore(toExecutionDate))
                    .filter(eRPImportOperation -> success == null || eRPImportOperation.getSuccess() == success)
                    .collect(Collectors.toList());
        } else {
            FinantialDocument document = FinantialDocument.findByUiDocumentNumber(finantialInstitution, documentNumber);
            if (document != null) {
                return document.getErpImportOperationsSet().stream()
                        .filter(eRPImportOperation -> fromExecutionDate == null || toExecutionDate == null
                                || eRPImportOperation.getExecutionDate().isAfter(fromExecutionDate)
                                        && eRPImportOperation.getExecutionDate().isBefore(toExecutionDate))
                        .filter(eRPImportOperation -> success == null || eRPImportOperation.getSuccess() == success)
                        .collect(Collectors.toList());
            } else {
                return new ArrayList<ERPImportOperation>();
            }
        }
    }

    private static final String _SEARCH_TO_DELETEMULTIPLE_URI = "/search/deletemultiple";
    public static final String SEARCH_TO_DELETEMULTIPLE_URL = CONTROLLER_URL + _SEARCH_TO_DELETEMULTIPLE_URI;

    @RequestMapping(value = _SEARCH_TO_DELETEMULTIPLE_URI)
    public String processSearchToDeleteMultiple(@RequestParam("eRPImportOperations") List<ERPImportOperation> eRPImportOperations,
            Model model, RedirectAttributes redirectAttributes) {

        try {
            if (eRPImportOperations.isEmpty() == false) {
                assertUserIsBackOfficeMember(eRPImportOperations.get(0).getFinantialInstitution(), model);
            }
            for (ERPImportOperation operation : eRPImportOperations) {
                deleteERPImportOperation(operation);
            }
            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.delete"), model);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + ex.getLocalizedMessage(), model);
        }

        return redirect(SEARCH_URL, model, redirectAttributes);
    }

    private static final String _SEARCH_TO_VIEW_ACTION_URI = "/search/view/";
    public static final String SEARCH_TO_VIEW_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_VIEW_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_VIEW_ACTION_URI + "{oid}")
    public String processSearchToViewAction(@PathVariable("oid") ERPImportOperation eRPImportOperation, Model model,
            RedirectAttributes redirectAttributes) {

        return redirect(READ_URL + eRPImportOperation.getExternalId(), model, redirectAttributes);
    }

    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {
        model.addAttribute("finantialInstitutionList", FinantialInstitution.findAll().collect(Collectors.toList()));
        return "treasury/integration/erp/erpimportoperation/create";
    }

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.POST)
    public String create(
            @RequestParam(value = "finantialinstitution", required = true) final FinantialInstitution finantialInstitution,
            @RequestParam(value = "file", required = true) MultipartFile file, Model model,
            RedirectAttributes redirectAttributes) {
        try {

            ERPImportOperation eRPImportOperation = createERPImportOperation(finantialInstitution, file);

            final IERPImporter erpImporter = finantialInstitution.getErpIntegrationConfiguration()
                    .getERPExternalServiceImplementation().getERPImporter(file.getInputStream());
            erpImporter.processAuditFile(eRPImportOperation);
            model.addAttribute("eRPImportOperation", eRPImportOperation);

            return redirect(READ_URL + getERPImportOperation(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + ex.getLocalizedMessage(), model);
        }
        return redirect(SEARCH_URL, model, redirectAttributes);
    }

    @Atomic(mode = TxMode.WRITE)
    public ERPImportOperation createERPImportOperation(final FinantialInstitution finantialInstitution, final MultipartFile file)
            throws IOException {
        IERPImporter erpImporter = finantialInstitution.getErpIntegrationConfiguration().getERPExternalServiceImplementation()
                .getERPImporter(file.getInputStream());
        FinantialInstitution finantialInstitutionFromFile =
                FinantialInstitution.findUniqueByFiscalCode(erpImporter.readTaxRegistrationNumberFromAuditFile()).orElse(null);

        if (finantialInstitution != finantialInstitutionFromFile) {
            throw new TreasuryDomainException("label.error.integration.erp.erpimportoperation.invalid.fiscalinstitution.file");
        }

        ERPImportOperation eRPImportOperation = ERPImportOperation.create(file.getOriginalFilename(), file.getBytes(),
                finantialInstitution, null, new DateTime(), false, false, false);

        return eRPImportOperation;
    }

    private static final String _READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + _READ_URI;

    @RequestMapping(value = _READ_URI + "{oid}")
    public String read(@PathVariable("oid") ERPImportOperation eRPImportOperation, Model model) {
        setERPImportOperation(eRPImportOperation, model);
        return "treasury/integration/erp/erpimportoperation/read";
    }

    private static final String _DELETE_URI = "/delete/";
    public static final String DELETE_URL = CONTROLLER_URL + _DELETE_URI;

    @RequestMapping(value = _DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") ERPImportOperation eRPImportOperation, Model model,
            RedirectAttributes redirectAttributes) {

        setERPImportOperation(eRPImportOperation, model);
        try {
            assertUserIsBackOfficeMember(eRPImportOperation.getFinantialInstitution(), model);

            deleteERPImportOperation(eRPImportOperation);

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.delete"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + ex.getLocalizedMessage(), model);
        }

        return redirect(READ_URL + getERPImportOperation(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/downloadfile")
    public void processReadToDownloadFile(@PathVariable("oid") ERPImportOperation eRPImportOperation, Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) {
        setERPImportOperation(eRPImportOperation, model);
        try {
            assertUserIsFrontOfficeMember(eRPImportOperation.getFinantialInstitution(), model);

            response.setContentType(eRPImportOperation.getFile().getContentType());
            String filename = eRPImportOperation.getFile().getFilename();
            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            response.getOutputStream().write(eRPImportOperation.getFile().getContent());
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            try {
                response.sendRedirect(
                        redirect(READ_URL + getERPImportOperation(model).getExternalId(), model, redirectAttributes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    @RequestMapping(value = "/read/{oid}/retryimport")
    public String processReadToRetryImport(@PathVariable("oid") ERPImportOperation eRPImportOperation, Model model,
            RedirectAttributes redirectAttributes) {
        setERPImportOperation(eRPImportOperation, model);
        try {
            assertUserIsFrontOfficeMember(eRPImportOperation.getFinantialInstitution(), model);

            final ERPImportOperation newERPImportOperation = cloneERPImportOperation(eRPImportOperation);

            final IERPImporter erpImporter = eRPImportOperation.getFinantialInstitution().getErpIntegrationConfiguration()
                    .getERPExternalServiceImplementation().getERPImporter(eRPImportOperation.getFile().getStream());
            erpImporter.processAuditFile(newERPImportOperation);

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.integration.erp.importoperation.success"), model);
            return redirect(READ_URL + newERPImportOperation.getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + ex.getLocalizedMessage(), model);
        }
        return redirect(READ_URL + getERPImportOperation(model).getExternalId(), model, redirectAttributes);
    }

    @Atomic
    private ERPImportOperation cloneERPImportOperation(ERPImportOperation erpImportOperation) {
        ERPImportOperation newERPImportOperation =
                ERPImportOperation.create(erpImportOperation.getFile().getFilename(), erpImportOperation.getFile().getContent(),
                        erpImportOperation.getFinantialInstitution(), erpImportOperation.getErpOperationId(), new DateTime(), false, false, false);
        return newERPImportOperation;
    }
}
