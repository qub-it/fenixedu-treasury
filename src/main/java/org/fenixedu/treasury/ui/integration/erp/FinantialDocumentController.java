/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: xpto@qub-it.com
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.services.integration.erp.ERPExporterManager;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.document.manageinvoice.CreditNoteController;
import org.fenixedu.treasury.ui.document.manageinvoice.DebitNoteController;
import org.fenixedu.treasury.ui.document.managepayments.SettlementNoteController;
import org.fenixedu.treasury.util.Constants;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

//@Component("org.fenixedu.treasury.ui.integration.erp") <-- Use for duplicate controller name disambiguation
//@SpringFunctionality(app = TreasuryController.class, title = "label.title.integration.erp",accessGroup = "logged")// CHANGE_ME accessGroup = "group1 | group2 | groupXPTO"
//or
@BennuSpringController(value = ERPExportOperationController.class)
@RequestMapping(FinantialDocumentController.CONTROLLER_URL)
public class FinantialDocumentController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/integration/erp/finantialdocument";

//

    @RequestMapping
    public String home(Model model) {
        //this is the default behaviour, for handling in a Spring Functionality
        return "forward:" + CONTROLLER_URL + "/";
    }

    private FinantialDocument getFinantialDocument(Model model) {
        return (FinantialDocument) model.asMap().get("finantialDocument");
    }

    private void setFinantialDocument(FinantialDocument finantialDocument, Model model) {
        model.addAttribute("finantialDocument", finantialDocument);
    }

    @Atomic
    public void deleteFinantialDocument(FinantialDocument finantialDocument) {
        // CHANGE_ME: Do the processing for deleting the finantialDocument
        // Do not catch any exception here

        // finantialDocument.delete();
    }

//				
    private static final String _SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI)
    public String search(
            @RequestParam(value = "finantialinstitution", required = false) FinantialInstitution finantialInstitution,
            Model model, RedirectAttributes redirectAttributes) {
        if (finantialInstitution == null) {
            finantialInstitution = FinantialInstitution.findAll().findFirst().orElse(null);
            return redirect(SEARCH_URL + "?finantialinstitution=" + finantialInstitution.getExternalId(), model,
                    redirectAttributes);
        }
        Set<FinantialDocument> searchfinantialdocumentResultsDataSet = filterSearchFinantialDocument(finantialInstitution);

        //add the results dataSet to the model
        model.addAttribute("searchfinantialdocumentResultsDataSet", searchfinantialdocumentResultsDataSet);
        model.addAttribute("FinantialDocument_finantialInstitution_options",
                FinantialInstitution.findAll().collect(Collectors.toList()));
        return "treasury/integration/erp/finantialdocument/search";
    }

    private Set<FinantialDocument> filterSearchFinantialDocument(FinantialInstitution finantialInstitution) {

        return finantialInstitution.getFinantialDocumentsPendingForExportationSet();
    }

    private static final String _SEARCH_TO_VIEW_ACTION_URI = "/search/view/";
    public static final String SEARCH_TO_VIEW_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_VIEW_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_VIEW_ACTION_URI + "{oid}")
    public String processSearchToViewAction(@PathVariable("oid") FinantialDocument finantialDocument, Model model,
            RedirectAttributes redirectAttributes) {

        // CHANGE_ME Insert code here for processing viewAction
        // If you selected multiple exists you must choose which one to use below
        if (finantialDocument.isDebitNote()) {
            return redirect(DebitNoteController.READ_URL + finantialDocument.getExternalId(), model, redirectAttributes);

        } else if (finantialDocument.isCreditNote()) {
            return redirect(CreditNoteController.READ_URL + finantialDocument.getExternalId(), model, redirectAttributes);

        } else if (finantialDocument.isSettlementNote()) {
            return redirect(SettlementNoteController.READ_URL + finantialDocument.getExternalId(), model, redirectAttributes);
        }

        addWarningMessage(Constants.treasuryBundle("warning.integration.erp.invalid.document.type"), model);
        return search(finantialDocument.getInstitutionForExportation(), model, redirectAttributes);
    }

//

    //
    // This is the EventforceIntegrationExport Method for Screen 
    //
    private static final String _SEARCH_TO_FORCEINTEGRATIONEXPORT_URI = "/search/forceintegrationexport";
    public static final String SEARCH_TO_FORCEINTEGRATIONEXPORT_URL = CONTROLLER_URL + _SEARCH_TO_FORCEINTEGRATIONEXPORT_URI;

    @RequestMapping(value = _SEARCH_TO_FORCEINTEGRATIONEXPORT_URI)
    public String processSearchToForceIntegrationExport(
            @RequestParam(value = "finantialinstitution", required = true) FinantialInstitution finantialInstitution, Model model,
            RedirectAttributes redirectAttributes) {
//
        try {
            assertUserIsManager(model);
            
//            assertUserIsBackOfficeMember(finantialInstitution, model);

            List<ERPExportOperation> exportPendingDocumentsForFinantialInstitution =
                    ERPExporterManager.exportPendingDocumentsForFinantialInstitution(finantialInstitution);
            if (exportPendingDocumentsForFinantialInstitution.size() == 0) {
                addWarningMessage(Constants.treasuryBundle("warning.integration.erp.no.documents.to.export"),
                        model);
                return redirect(SEARCH_URL, model, redirectAttributes);
            } else if (exportPendingDocumentsForFinantialInstitution.size() == 1) {
                addInfoMessage(Constants.treasuryBundle("info.integration.erp.success.export"), model);
                return redirect(
                        ERPExportOperationController.READ_URL
                                + exportPendingDocumentsForFinantialInstitution.get(0).getExternalId(),
                        model, redirectAttributes);
            } else {
                addInfoMessage(Constants.treasuryBundle("info.integration.erp.multiple.success.export"), model);
                return redirect(ERPExportOperationController.SEARCH_URL, model, redirectAttributes);
            }
        } catch (Exception ex) {
            addErrorMessage(ex.getMessage(), model);
        }
        return this.search(finantialInstitution, model, redirectAttributes);

    }

    //
    // This is the EventforceIntegrationExport Method for Screen 
    //
    private static final String _SEARCH_TO_FORCECHECK_STATUS_URI = "/search/forcecheckstatus";
    public static final String _SEARCH_TO_FORCECHECK_STATUS_URL = CONTROLLER_URL + _SEARCH_TO_FORCECHECK_STATUS_URI;

    @RequestMapping(value = _SEARCH_TO_FORCECHECK_STATUS_URI)
    public String processSearchToForceCehckStatus(
            @RequestParam(value = "finantialinstitution", required = true) FinantialInstitution finantialInstitution, Model model,
            RedirectAttributes redirectAttributes) {
//
        try {
            assertUserIsBackOfficeMember(finantialInstitution, model);

            ERPExporterManager.requestPendingDocumentStatus(finantialInstitution);

        } catch (Exception ex) {
            addErrorMessage(ex.getMessage(), model);
        }
        return this.search(finantialInstitution, model, redirectAttributes);

    }

    private static final String _SEARCH_TO_MARK_MULTIPLE_EXPORTED_URI = "/search/markmultipleexported";
    public static final String SEARCH_TO_MARK_MULTIPLE_EXPORTED_URL = CONTROLLER_URL + _SEARCH_TO_MARK_MULTIPLE_EXPORTED_URI;

    @RequestMapping(value = _SEARCH_TO_MARK_MULTIPLE_EXPORTED_URI, method = RequestMethod.POST)
    public String processSearchToMarkMultipleAsExported(@RequestParam("document") List<FinantialDocument> finantialDocuments,
            Model model, RedirectAttributes redirectAttributes) {

        try {
            assertUserIsManager(model);
            for (FinantialDocument document : finantialDocuments) {
                // document.clearDocumentToExport();
            }
            addInfoMessage(Constants.treasuryBundle("label.success.create"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(Constants.treasuryBundle("label.error.create") + ex.getLocalizedMessage(), model);
        }
        return redirect(SEARCH_URL, model, redirectAttributes);
    }

    private static final String _READ_FINANTIAL_DOCUMENT_URI = "/readfinantialdocument";
    public static final String READ_FINANTIAL_DOCUMENT_URL = CONTROLLER_URL + _READ_FINANTIAL_DOCUMENT_URI;

    @RequestMapping(value = _READ_FINANTIAL_DOCUMENT_URI + "/{id}", method = RequestMethod.GET)
    public String readfinantialdocument(@PathVariable("id") final FinantialDocument finantialDocument, final Model model,
            final RedirectAttributes redirectAttributes) {
        if (finantialDocument.isDebitNote()) {
            return redirect(DebitNoteController.READ_URL + "/" + finantialDocument.getExternalId(), model, redirectAttributes);
        } else if (finantialDocument.isCreditNote()) {
            return redirect(CreditNoteController.READ_URL + "/" + finantialDocument.getExternalId(), model, redirectAttributes);
        } else if (finantialDocument.isSettlementNote()) {
            return redirect(SettlementNoteController.READ_URL + "/" + finantialDocument.getExternalId(), model, redirectAttributes);
        }

        return null;
    }
}
