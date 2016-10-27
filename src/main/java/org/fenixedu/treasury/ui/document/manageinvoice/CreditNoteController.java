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
package org.fenixedu.treasury.ui.document.manageinvoice;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.StringNormalizer;
import org.fenixedu.treasury.domain.Currency;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.CreditNote;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.document.FinantialDocumentStateType;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.services.integration.erp.IERPExporter;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController;
import org.fenixedu.treasury.ui.integration.erp.ERPExportOperationController;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Strings;

import pt.ist.fenixframework.Atomic;

//@Component("org.fenixedu.treasury.ui.document.manageInvoice") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.document.manageCreditNote",
        accessGroup = "treasuryFrontOffice")
@RequestMapping(CreditNoteController.CONTROLLER_URL)
public class CreditNoteController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/document/manageinvoice/creditnote";

    public static final long SEARCH_CREDIT_NOTE_LIST_LIMIT_SIZE = 500;

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/";
    }

    private CreditNote getCreditNote(Model model) {
        return (CreditNote) model.asMap().get("creditNote");
    }

    private void setCreditNote(CreditNote creditNote, Model model) {
        model.addAttribute("creditNote", creditNote);
    }

    @Atomic
    public void deleteCreditNote(CreditNote creditNote) {
        creditNote.delete(true);
    }

    private static final String _READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + _READ_URI;

    @RequestMapping(value = _READ_URI + "{oid}")
    public String read(@PathVariable("oid") CreditNote creditNote, Model model) {
        setCreditNote(creditNote, model);
        return "treasury/document/manageinvoice/creditnote/read";
    }

    @RequestMapping(value = "/read/{oid}/closecreditnote", method = RequestMethod.POST)
    public String processReadToCloseCreditNote(@PathVariable("oid") CreditNote creditNote, Model model,
            RedirectAttributes redirectAttributes) {
        setCreditNote(creditNote, model);
        try {
            assertUserIsAllowToModifyInvoices(creditNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);

            creditNote.closeDocument();

            addInfoMessage(Constants.bundle("label.document.manageinvoice.CreditNote.document.closed.sucess"), model);
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }
        return redirect(CreditNoteController.READ_URL + getCreditNote(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/anullcreditnote", method = RequestMethod.POST)
    public String processReadToAnullCreditNote(@PathVariable("oid") CreditNote creditNote,
            @RequestParam("anullReason") String anullReason, Model model, RedirectAttributes redirectAttributes) {
        setCreditNote(creditNote, model);
        try {
            assertUserIsAllowToModifyInvoices(creditNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);
            creditNote.anullDocument(anullReason);
            addInfoMessage(
                    Constants.bundle("label.document.manageinvoice.CreditNote.document.anulled.sucess"),
                    model);
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }

        return redirect(CreditNoteController.READ_URL + getCreditNote(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = "/delete/{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") CreditNote creditNote, Model model, RedirectAttributes redirectAttributes) {
        setCreditNote(creditNote, model);
        DebtAccount debtAccount = creditNote.getDebtAccount();
        try {
            assertUserIsAllowToModifyInvoices(creditNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);

            deleteCreditNote(creditNote);
            addInfoMessage(Constants.bundle("label.success.delete"), model);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(Constants.bundle("label.error.delete") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(Constants.bundle("label.error.delete") + ex.getLocalizedMessage(), model);
        }
        return redirect(DebtAccountController.READ_URL + debtAccount.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/addentry")
    public String processReadToAddEntry(@PathVariable("oid") CreditNote creditNote, Model model,
            RedirectAttributes redirectAttributes) {
        setCreditNote(creditNote, model);
        return redirect(CreditEntryController.CREATE_URL + "?creditnote=" + getCreditNote(model).getExternalId(), model,
                redirectAttributes);
    }

    private static final String _UPDATE_URI = "/update/";
    public static final String UPDATE_URL = CONTROLLER_URL + _UPDATE_URI;

    @RequestMapping(value = _UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") CreditNote creditNote, Model model) {
        model.addAttribute("stateValues", org.fenixedu.treasury.domain.document.FinantialDocumentStateType.values());
        setCreditNote(creditNote, model);
        return "treasury/document/manageinvoice/creditnote/update";
    }

    @RequestMapping(value = _UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") CreditNote creditNote, @RequestParam(value = "origindocumentnumber",
            required = false) String originDocumentNumber,
            @RequestParam(value = "documentobservations", required = false) String documentObservations, Model model,
            RedirectAttributes redirectAttributes) {

        setCreditNote(creditNote, model);

        try {
            assertUserIsAllowToModifyInvoices(creditNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);

            getCreditNote(model).updateCreditNote(originDocumentNumber, documentObservations);

            return redirect(CreditNoteController.READ_URL + getCreditNote(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(Constants.bundle("label.error.update") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(Constants.bundle("label.error.update") + ex.getLocalizedMessage(), model);
        }
        return update(creditNote, model);
    }

    private static final String _SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI)
    public String search(
            @RequestParam(value = "debitnote", required = false) DebitNote debitNote,
            @RequestParam(value = "payordebtaccount", required = false) DebtAccount payorDebtAccount,
            @RequestParam(value = "finantialdocumenttype", required = false) FinantialDocumentType finantialDocumentType,
            @RequestParam(value = "debtaccount", required = false) DebtAccount debtAccount,
            @RequestParam(value = "documentnumberseries", required = false) DocumentNumberSeries documentNumberSeries,
            @RequestParam(value = "currency", required = false) Currency currency,
            @RequestParam(value = "documentnumber", required = false) String documentNumber,
            @RequestParam(value = "documentdatefrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate documentDateFrom,
            @RequestParam(value = "documentdateto", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate documentDateTo,
            @RequestParam(value = "documentduedate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime documentDueDate,
            @RequestParam(value = "origindocumentnumber", required = false) String originDocumentNumber, @RequestParam(
                    value = "state", required = false) FinantialDocumentStateType state, Model model) {
        List<CreditNote> searchcreditnoteResultsDataSet =
                filterSearchCreditNote(debitNote, payorDebtAccount, finantialDocumentType, debtAccount, documentNumberSeries,
                        currency, documentNumber, documentDateFrom, documentDateTo, documentDueDate, originDocumentNumber, state);
        model.addAttribute("listSize", searchcreditnoteResultsDataSet.size());
        searchcreditnoteResultsDataSet =
                searchcreditnoteResultsDataSet.stream().limit(SEARCH_CREDIT_NOTE_LIST_LIMIT_SIZE).collect(Collectors.toList());

        model.addAttribute("searchcreditnoteResultsDataSet", searchcreditnoteResultsDataSet);
//        model.addAttribute("CreditNote_debitNote_options", new ArrayList<DebitNote>());
//        model.addAttribute("CreditNote_payorDebtAccount_options", new ArrayList<DebtAccount>());
//        model.addAttribute("CreditNote_finantialDocumentType_options", new ArrayList<FinantialDocumentType>());
//        model.addAttribute("CreditNote_debtAccount_options", new ArrayList<DebtAccount>());
//        model.addAttribute("CreditNote_documentNumberSeries_options", new ArrayList<DocumentNumberSeries>());
//        model.addAttribute("CreditNote_currency_options", new ArrayList<Currency>());
        model.addAttribute("stateValues", FinantialDocumentStateType.findAll());

        return "treasury/document/manageinvoice/creditnote/search";
    }

    private List<CreditNote> getSearchUniverseSearchCreditNoteDataSet() {
        return CreditNote.findAll().collect(Collectors.<CreditNote> toList());
    }

    private List<CreditNote> filterSearchCreditNote(DebitNote debitNote, DebtAccount payorDebtAccount,
            FinantialDocumentType finantialDocumentType, DebtAccount debtAccount, DocumentNumberSeries documentNumberSeries,
            Currency currency, String documentNumber, LocalDate documentDateFrom, LocalDate documentDateTo,
            DateTime documentDueDate, String originDocumentNumber, FinantialDocumentStateType state) {

        return getSearchUniverseSearchCreditNoteDataSet()
                .stream()
                .filter(creditNote -> debitNote == null || debitNote == creditNote.getDebitNote())
                .filter(creditNote -> payorDebtAccount == null || payorDebtAccount == creditNote.getPayorDebtAccount())
                .filter(creditNote -> finantialDocumentType == null
                        || finantialDocumentType == creditNote.getFinantialDocumentType())
                .filter(creditNote -> debtAccount == null || debtAccount == creditNote.getDebtAccount())
                .filter(creditNote -> documentNumberSeries == null
                        || documentNumberSeries == creditNote.getDocumentNumberSeries())
                .filter(creditNote -> currency == null || currency == creditNote.getCurrency())
                .filter(creditNote -> documentNumber == null || documentNumber.length() == 0
                        || creditNote.getDocumentNumber() != null && creditNote.getDocumentNumber().length() > 0
                        && creditNote.getUiDocumentNumber().toLowerCase().contains(documentNumber.toLowerCase()))
                .filter(creditNote -> documentDateFrom == null
                        || creditNote.getDocumentDate().toLocalDate().isEqual(documentDateFrom)
                        || creditNote.getDocumentDate().toLocalDate().isAfter(documentDateFrom))
                .filter(creditNote -> documentDateTo == null
                        || creditNote.getDocumentDate().toLocalDate().isEqual(documentDateTo)
                        || creditNote.getDocumentDate().toLocalDate().isBefore(documentDateTo))
                .filter(creditNote -> documentDueDate == null || documentDueDate.equals(creditNote.getDocumentDueDate()))
                .filter(creditNote -> originDocumentNumber == null || originDocumentNumber.length() == 0
                        || creditNote.getOriginDocumentNumber() != null && creditNote.getOriginDocumentNumber().length() > 0
                        && creditNote.getOriginDocumentNumber().toLowerCase().contains(originDocumentNumber.toLowerCase()))
                .filter(creditNote -> state == null || state.equals(creditNote.getState())).collect(Collectors.toList());
    }

    private static final String _SEARCH_TO_VIEW_ACTION_URI = "/search/view/";
    public static final String SEARCH_TO_VIEW_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_VIEW_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_VIEW_ACTION_URI + "{oid}")
    public String processSearchToViewAction(@PathVariable("oid") CreditNote creditNote, Model model,
            RedirectAttributes redirectAttributes) {

        return redirect(CreditNoteController.READ_URL + creditNote.getExternalId(), model, redirectAttributes);
    }

    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.GET)
    public String create(@RequestParam(value = "debtaccount", required = false) DebtAccount debtAccount, @RequestParam(
            value = "debitnote", required = false) DebitNote debitNote, Model model, RedirectAttributes redirectAttributes) {

        FinantialInstitution finantialInstitution = null;
//        DocumentNumberSeries documentNumberSeries = null;
        if (debtAccount == null && debitNote == null) {
            addErrorMessage(Constants.bundle(
                    "label.error.document.manageinvoice.finantialinstitution.mismatch.debtaccount.series"), model);
            return redirectToReferrer(model, redirectAttributes);
        }

        if (debitNote != null && debtAccount != null) {
            if (!debitNote.getDebtAccount().equals(debtAccount)) {
                addErrorMessage(Constants.bundle(
                        "label.error.document.manageinvoice.finantialinstitution.mismatch.debtaccount.series"), model);
                return redirectToReferrer(model, redirectAttributes);
            }
        }

        if (debtAccount != null) {
            finantialInstitution = debtAccount.getFinantialInstitution();
        }
        if (debitNote != null) {
            finantialInstitution = debitNote.getDebtAccount().getFinantialInstitution();
            debtAccount = debitNote.getDebtAccount();
//            documentNumberSeries =
//                    DocumentNumberSeries.find(FinantialDocumentType.findForCreditNote(), debitNote.getDocumentNumberSeries()
//                            .getSeries());
        }

//        if (documentNumberSeries != null) {
//            model.addAttribute("CreditNote_documentNumberSeries_options", Collections.singletonList(documentNumberSeries));
//
//        } else {
        List<DocumentNumberSeries> availableSeries =
                org.fenixedu.treasury.domain.document.DocumentNumberSeries
                        .find(FinantialDocumentType.findForCreditNote(), finantialInstitution)
                        .filter(x -> x.getSeries().getActive() == true).collect(Collectors.toList());

        availableSeries =
                DocumentNumberSeries.applyActiveSelectableAndDefaultSorting(availableSeries.stream()).collect(Collectors.toList());
        if (availableSeries.size() > 0) {
            model.addAttribute("CreditNote_documentNumberSeries_options", availableSeries);
        } else {
            addErrorMessage(Constants.bundle(
                    "label.error.document.manageinvoice.finantialinstitution.no.available.series.found"), model);
            return redirect(DebtAccountController.READ_URL + debtAccount.getExternalId(), model, redirectAttributes);
        }
//        }

        model.addAttribute("debitNote", debitNote);
        model.addAttribute("debtAccount", debtAccount);

        if (debitNote == null) {
            this.addWarningMessage(
                    BundleUtil.getString(Constants.BUNDLE, "label.document.manageinvoice.creditnote.without.debitnote"), model);
        }
        return "treasury/document/manageinvoice/creditnote/create";
    }

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.POST)
    public String create(
                @RequestParam(value = "debitnote", required = false) DebitNote debitNote, 
                @RequestParam(value = "debtaccount", required = false) DebtAccount debtAccount,
                @RequestParam(value = "documentnumberseries") DocumentNumberSeries documentNumberSeries, 
                @RequestParam(value = "documentdate") @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime documentDate, 
                @RequestParam(value = "origindocumentnumber", required = false) String originDocumentNumber, 
                @RequestParam(value = "documentobservations", required = false) String documentObservations, 
                Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {

        if (debtAccount == null && debitNote == null) {
            addErrorMessage(Constants.bundle(
                    "label.error.document.manageinvoice.finantialinstitution.mismatch.debtaccount.series"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        }
        
        if(debtAccount == null) {
            debtAccount = debitNote.getDebtAccount();
        }
        
        if (documentNumberSeries != null && debtAccount != null) {
            if (!documentNumberSeries.getSeries().getFinantialInstitution().equals(debtAccount.getFinantialInstitution())) {
                addErrorMessage(Constants.bundle(
                        "label.error.document.manageinvoice.finantialinstitution.mismatch.debtaccount.series"), model);
                return redirect(DebtAccountController.READ_URL + debtAccount.getExternalId(), model, redirectAttributes);
            }
        }

        try {
            assertUserIsAllowToModifyInvoices(documentNumberSeries.getSeries().getFinantialInstitution(), model);

            debitNote.createEquivalentCreditNote(documentDate, documentObservations, false);

            return redirect(DebtAccountController.READ_URL + debtAccount.getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(Constants.bundle("label.error.create") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(Constants.bundle("label.error.create") + ex.getLocalizedMessage(), model);
        }
        
        return create(debtAccount, debitNote, model, redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/exportintegrationfile", produces = "text/xml;charset=Windows-1252")
    public void processReadToExportIntegrationFile(@PathVariable("oid") CreditNote creditNote, Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) {
        try {
            assertUserIsFrontOfficeMember(creditNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);

            creditNote.recalculateAmountValues();

            final IERPExporter erpExporter = creditNote.getDebtAccount().getFinantialInstitution().getErpIntegrationConfiguration()
                    .getERPExternalServiceImplementation().getERPExporter();

            String output =
                    erpExporter.exportFinantialDocumentToXML(
                            creditNote.getDebtAccount().getFinantialInstitution(),
                            creditNote
                                    .findRelatedDocuments(
                                            new HashSet<FinantialDocument>(),
                                            creditNote.getDebtAccount().getFinantialInstitution()
                                                    .getErpIntegrationConfiguration().getExportAnnulledRelatedDocuments())
                                    .stream().collect(Collectors.toList()));
            response.setContentType("text/xml");
            response.setCharacterEncoding("Windows-1252");
            String filename =
                    URLEncoder.encode(
                            StringNormalizer.normalizePreservingCapitalizedLetters((creditNote.getDebtAccount()
                                    .getFinantialInstitution().getFiscalNumber()
                                    + "_" + creditNote.getUiDocumentNumber() + ".xml").replaceAll("/", "_")
                                    .replaceAll("\\s", "_").replaceAll(" ", "_")), "Windows-1252");
            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            response.getOutputStream().write(output.getBytes("Windows-1252"));
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            try {
                response.sendRedirect(redirect(READ_URL + creditNote.getExternalId(), model, redirectAttributes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/read/{oid}/exportintegrationonline")
    public String processReadToExportIntegrationOnline(@PathVariable("oid") CreditNote creditNote, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(creditNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);

            //Force a check status first of the document
            try {
                final IERPExporter erpExporter = creditNote.getDebtAccount().getFinantialInstitution().getErpIntegrationConfiguration()
                        .getERPExternalServiceImplementation().getERPExporter();

                erpExporter.checkIntegrationDocumentStatus(creditNote);
            } catch (Exception ex) {

            }
            
            final List<FinantialDocument> documentsToExport = Collections.singletonList(creditNote);
            final IERPExporter erpExporter = creditNote.getDebtAccount().getFinantialInstitution().getErpIntegrationConfiguration()
                    .getERPExternalServiceImplementation().getERPExporter();

            ERPExportOperation output =
                    erpExporter.exportFinantialDocumentToIntegration(creditNote.getDebtAccount().getFinantialInstitution(),
                            documentsToExport);
            addInfoMessage(Constants.bundle("label.integration.erp.exportoperation.success"), model);
            return redirect(ERPExportOperationController.READ_URL + output.getExternalId(), model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(
                    Constants.bundle("label.integration.erp.exportoperation.error")
                            + ex.getLocalizedMessage(), model);
        }
        return read(creditNote, model);
    }

    @RequestMapping(value = "/read/{oid}/cleardocumenttoexport", method = RequestMethod.POST)
    public String cleardocumenttoexport(@PathVariable("oid") final CreditNote creditNote,
            @RequestParam(value = "reason", required = false) final String reason, final Model model,
            final RedirectAttributes redirectAttributes) {
        try {
            
            if(!creditNote.isDocumentToExport()) {
                addErrorMessage(Constants.bundle("error.FinantialDocument.document.not.marked.to.export"), model);
                return redirect(READ_URL + creditNote.getExternalId(), model, redirectAttributes);
            }
            
            if(Strings.isNullOrEmpty(reason)) {
                addErrorMessage(Constants.bundle("error.FinantialDocument.clear.document.to.export.requires.reason"), model);
                return redirect(READ_URL + creditNote.getExternalId(), model, redirectAttributes);
            }
            
            assertUserIsBackOfficeMember(model);

            creditNote.clearDocumentToExport(reason);

            return redirect(READ_URL + creditNote.getExternalId(), model, redirectAttributes);
        } catch (final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
            return redirect(READ_URL + creditNote.getExternalId(), model, redirectAttributes);
        }
    }
    
}
