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
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.StringNormalizer;
import org.fenixedu.treasury.domain.Currency;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.document.FinantialDocumentStateType;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.dto.InterestRateBean;
import org.fenixedu.treasury.dto.SettlementNoteBean.InterestEntryBean;
import org.fenixedu.treasury.services.integration.erp.ERPExporterManager;
import org.fenixedu.treasury.services.integration.erp.IERPExporter;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController;
import org.fenixedu.treasury.ui.administration.managefinantialinstitution.FinantialInstitutionController;
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
@SpringFunctionality(app = TreasuryController.class, title = "label.title.document.manageInvoice.debitNote",
        accessGroup = "treasuryFrontOffice")
@RequestMapping(DebitNoteController.CONTROLLER_URL)
public class DebitNoteController extends TreasuryBaseController {
    public static final String CONTROLLER_URL = "/treasury/document/manageinvoice/debitnote";
    private static final String SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + SEARCH_URI;
    private static final String UPDATE_URI = "/update/";
    public static final String UPDATE_URL = CONTROLLER_URL + UPDATE_URI;
    private static final String CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + CREATE_URI;
    private static final String READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + READ_URI;

    public static final long SEARCH_DEBIT_NOTE_LIST_LIMIT_SIZE = 500;

    @RequestMapping
    public String home(Model model) {
        return "forward:" + SEARCH_URL;
    }

    private DebitNote getDebitNote(Model model) {
        return (DebitNote) model.asMap().get("debitNote");
    }

    private void setDebitNote(DebitNote debitNote, Model model) {
        model.addAttribute("debitNote", debitNote);
    }

    @Atomic
    public void deleteDebitNote(DebitNote debitNote) {
        debitNote.delete(false);
    }

    @RequestMapping(value = "/delete/{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") DebitNote debitNote, Model model, RedirectAttributes redirectAttributes) {
        setDebitNote(debitNote, model);
        DebtAccount debtAccount = debitNote.getDebtAccount();
        try {
            assertUserIsFrontOfficeMember(debitNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);
            deleteDebitNote(debitNote);
            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.delete"), model);
            return redirect(DebtAccountController.READ_URL + debtAccount.getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + ex.getLocalizedMessage(), model);
        }
        return redirect(READ_URL + debitNote.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") DebitNote debitNote, Model model, RedirectAttributes redirectAttributes) {

        try {
            assertUserIsFrontOfficeMember(debitNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);

            setDebitNote(debitNote, model);

            if (debitNote.isClosed() && debitNote.getDocumentNumberSeries().getSeries().getCertificated()) {
                model.addAttribute("anullDebitNoteMessage", BundleUtil.getString(Constants.BUNDLE,
                        "label.document.manageInvoice.readDebitNote.confirmAnullWithCreditNote"));
            }

            return "treasury/document/manageinvoice/debitnote/read";
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }
        return redirect(FinantialInstitutionController.SEARCH_URL, model, redirectAttributes);

    }

    @RequestMapping(value = SEARCH_URI)
    public String search(@RequestParam(value = "payordebtaccount", required = false) DebtAccount payorDebtAccount,
            @RequestParam(value = "finantialdocumenttype", required = false) FinantialDocumentType finantialDocumentType,
            @RequestParam(value = "debtaccount", required = false) DebtAccount debtAccount,
            @RequestParam(value = "documentnumberseries", required = false) DocumentNumberSeries documentNumberSeries,
            @RequestParam(value = "currency", required = false) Currency currency,
            @RequestParam(value = "documentnumber", required = false) String documentNumber,
            @RequestParam(value = "documentdatefrom",
                    required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate documentDateFrom,
            @RequestParam(value = "documentdateto",
                    required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate documentDateTo,
            @RequestParam(value = "documentduedate",
                    required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate documentDueDate,
            @RequestParam(value = "origindocumentnumber", required = false) String originDocumentNumber,
            @RequestParam(value = "state", required = false) FinantialDocumentStateType state, Model model) {

        List<DebitNote> searchdebitnoteResultsDataSet =
                filterSearchDebitNote(payorDebtAccount, finantialDocumentType, debtAccount, documentNumberSeries, currency,
                        documentNumber, documentDateFrom, documentDateTo, documentDueDate, originDocumentNumber, state);
        model.addAttribute("listSize", searchdebitnoteResultsDataSet.size());
        searchdebitnoteResultsDataSet =
                searchdebitnoteResultsDataSet.stream().limit(SEARCH_DEBIT_NOTE_LIST_LIMIT_SIZE).collect(Collectors.toList());

        model.addAttribute("searchdebitnoteResultsDataSet", searchdebitnoteResultsDataSet);
        model.addAttribute("DebitNote_payorDebtAccount_options", org.fenixedu.treasury.domain.debt.DebtAccount.findAll()
                .filter(x -> x.getCustomer().isAdhocCustomer()).collect(Collectors.toList()));
//        model.addAttribute("DebitNote_finantialDocumentType_options", org.fenixedu.treasury.domain.document.FinantialDocumentType
//                .findAll().collect(Collectors.toList()));
//        model.addAttribute("DebitNote_debtAccount_options", new ArrayList<org.fenixedu.treasury.domain.debt.DebtAccount>()); // CHANGE_ME
//        model.addAttribute("DebitNote_debtAccount_options",
//                org.fenixedu.treasury.domain.debt.DebtAccount.findAll().collect(Collectors.toList()));
//        model.addAttribute("DebitNote_documentNumberSeries_options", org.fenixedu.treasury.domain.document.DocumentNumberSeries
//                .findAll().collect(Collectors.toList()));
//        model.addAttribute("DebitNote_currency_options",
//                org.fenixedu.treasury.domain.Currency.findAll().collect(Collectors.toList()));
        model.addAttribute("stateValues", org.fenixedu.treasury.domain.document.FinantialDocumentStateType.findAll());

        return "treasury/document/manageinvoice/debitnote/search";
    }

    private List<DebitNote> getSearchUniverseSearchDebitNoteDataSet() {
        return DebitNote.findAll().collect(Collectors.toList());
    }

    private List<DebitNote> filterSearchDebitNote(DebtAccount payorDebtAccount, FinantialDocumentType finantialDocumentType,
            DebtAccount debtAccount, DocumentNumberSeries documentNumberSeries, Currency currency,
            java.lang.String documentNumber, LocalDate documentDateFrom, LocalDate documentDateTo, LocalDate documentDueDate,
            String originDocumentNumber, FinantialDocumentStateType state) {

        return getSearchUniverseSearchDebitNoteDataSet().stream()
                .filter(debitNote -> payorDebtAccount == null || payorDebtAccount == debitNote.getPayorDebtAccount())
                .filter(debitNote -> finantialDocumentType == null
                        || finantialDocumentType == debitNote.getFinantialDocumentType())
                .filter(debitNote -> debtAccount == null || debtAccount == debitNote.getDebtAccount())
                .filter(debitNote -> documentNumberSeries == null || documentNumberSeries == debitNote.getDocumentNumberSeries())
                .filter(debitNote -> currency == null || currency == debitNote.getCurrency())
                .filter(debitNote -> documentNumber == null || documentNumber.length() == 0
                        || debitNote.getDocumentNumber() != null && debitNote.getDocumentNumber().length() > 0
                                && debitNote.getUiDocumentNumber().toLowerCase().contains(documentNumber.toLowerCase()))
                .filter(debitNote -> documentDateFrom == null
                        || debitNote.getDocumentDate().toLocalDate().isEqual(documentDateFrom)
                        || debitNote.getDocumentDate().toLocalDate().isAfter(documentDateFrom))
                .filter(debitNote -> documentDateTo == null || debitNote.getDocumentDate().toLocalDate().isEqual(documentDateTo)
                        || debitNote.getDocumentDate().toLocalDate().isBefore(documentDateTo))
                .filter(debitNote -> documentDueDate == null || documentDueDate.equals(debitNote.getDocumentDueDate()))
                .filter(debitNote -> originDocumentNumber == null || originDocumentNumber.length() == 0
                        || debitNote.getOriginDocumentNumber() != null && debitNote.getOriginDocumentNumber().length() > 0
                                && debitNote.getOriginDocumentNumber().toLowerCase().contains(originDocumentNumber.toLowerCase()))
                .filter(debitNote -> state == null || state.equals(debitNote.getState())).collect(Collectors.toList());
    }

    @RequestMapping(value = "/search/view/{oid}")
    public String processSearchToViewAction(@PathVariable("oid") DebitNote debitNote, Model model,
            RedirectAttributes redirectAttributes) {
        return redirect(READ_URL + debitNote.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.GET)
    public String create(
            @RequestParam(value = "documentnumberseries", required = false) DocumentNumberSeries documentNumberSeries,
            @RequestParam(value = "debtaccount", required = false) DebtAccount debtAccount,
            @RequestParam(value = "debitEntry", required = false) DebitEntry debitEntry, Model model,
            RedirectAttributes redirectAttributes) {
        try {

            if (debitEntry != null) {
                debtAccount = debitEntry.getDebtAccount();
                model.addAttribute("debitEntry", debitEntry);
            }
            FinantialInstitution finantialInstitution = null;
            if (documentNumberSeries == null && debtAccount == null) {
                addErrorMessage(BundleUtil.getString(Constants.BUNDLE,
                        "label.error.document.manageinvoice.finantialinstitution.mismatch.debtaccount.series"), model);
                return redirect(SEARCH_URL, model, redirectAttributes);
            }
            assertUserIsAllowToModifyInvoices(debtAccount.getFinantialInstitution(), model);

            if (documentNumberSeries != null && debtAccount != null) {
                if (!documentNumberSeries.getSeries().getFinantialInstitution().getCode()
                        .equals(debtAccount.getFinantialInstitution().getCode())) {
                    addErrorMessage(
                            BundleUtil.getString(Constants.BUNDLE,
                                    "label.error.document.manageinvoice.finantialinstitution.mismatch.debtaccount.series"),
                            model);
                    return redirect(DebtAccountController.READ_URL + debtAccount.getExternalId(), model, redirectAttributes);
                }
            }

            if (documentNumberSeries != null) {
                finantialInstitution = documentNumberSeries.getSeries().getFinantialInstitution();
            }
            if (debtAccount != null) {
                finantialInstitution = debtAccount.getFinantialInstitution();
            }

            model.addAttribute("DebitNote_payorDebtAccount_options",
                    DebtAccount.find(finantialInstitution).filter(x -> x.getCustomer().isAdhocCustomer())
                            .sorted((x, y) -> x.getCustomer().getName().compareToIgnoreCase(y.getCustomer().getName()))
                            .collect(Collectors.toList()));

            if (debtAccount != null) {
                model.addAttribute("DebitNote_debtAccount_options", Collections.singleton(debtAccount));
            } else {
                model.addAttribute("DebitNote_debtAccount_options",
                        DebtAccount.find(finantialInstitution).collect(Collectors.toList()));
            }
            if (documentNumberSeries != null) {
                model.addAttribute("DebitNote_documentNumberSeries_options", Collections.singletonList(documentNumberSeries));

            } else {
                List<DocumentNumberSeries> availableSeries = org.fenixedu.treasury.domain.document.DocumentNumberSeries
                        .find(FinantialDocumentType.findForDebitNote(), debtAccount.getFinantialInstitution())
                        .collect(Collectors.toList());

                if (availableSeries.size() > 0) {
                    model.addAttribute("DebitNote_documentNumberSeries_options", DocumentNumberSeries
                            .applyActiveSelectableAndDefaultSorting(availableSeries.stream()).collect(Collectors.toList()));
                } else {
                    addErrorMessage(BundleUtil.getString(Constants.BUNDLE,
                            "label.error.document.manageinvoice.finantialinstitution.no.available.series.found"), model);
                    return redirect(DebtAccountController.READ_URL + debtAccount.getExternalId(), model, redirectAttributes);
                }
            }
            return "treasury/document/manageinvoice/debitnote/create";
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }
        return redirect(FinantialInstitutionController.SEARCH_URL, model, redirectAttributes);

    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "payordebtaccount", required = false) DebtAccount payorDebtAccount,
            @RequestParam(value = "debtaccount") DebtAccount debtAccount,
            @RequestParam(value = "debitentry") DebitEntry debitEntry,
            @RequestParam(value = "documentnumberseries") DocumentNumberSeries documentNumberSeries,
            @RequestParam(value = "documentdate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate documentDate,
            @RequestParam(value = "documentduedate",
                    required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate documentDueDate,
            @RequestParam(value = "origindocumentnumber", required = false) String originDocumentNumber,
            @RequestParam(value = "documentobservations", required = false) String documentObservations, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            assertUserIsAllowToModifyInvoices(documentNumberSeries.getSeries().getFinantialInstitution(), model);
            DebitNote debitNote = createDebitNote(payorDebtAccount, debtAccount, documentNumberSeries, documentDate,
                    documentDueDate, originDocumentNumber, documentObservations);

            if (debitEntry != null && debitEntry.getFinantialDocument() == null) {
                addDebitEntryToDebitNote(debitEntry, debitNote);
            }

            model.addAttribute("debitNote", debitNote);
            return redirect(READ_URL + getDebitNote(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + tde.getLocalizedMessage(), model);
        } catch (Exception de) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + de.getLocalizedMessage(), model);
        }
        return create(documentNumberSeries, debtAccount, debitEntry, model, redirectAttributes);
    }

    @Atomic
    private void addDebitEntryToDebitNote(DebitEntry debitEntry, DebitNote debitNote) {
        debitEntry.setFinantialDocument(debitNote);
    }

    @Atomic
    public DebitNote createDebitNote(DebtAccount payorDebtAccount, DebtAccount debtAccount,
            DocumentNumberSeries documentNumberSeries, LocalDate documentDate, LocalDate documentDueDate,
            String originDocumentNumber, String documentObservations) {
        if (payorDebtAccount == null) {
            payorDebtAccount = debtAccount;
        }

        DebitNote debitNote = DebitNote.create(debtAccount, payorDebtAccount, documentNumberSeries,
                documentDate.toDateTimeAtCurrentTime(), documentDueDate, originDocumentNumber);
        debitNote.setDocumentObservations(documentObservations);

        return debitNote;
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") DebitNote debitNote, Model model, RedirectAttributes redirectAttributes) {
        try {
            assertUserIsAllowToModifyInvoices(debitNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);

            model.addAttribute("stateValues", org.fenixedu.treasury.domain.document.FinantialDocumentStateType.values());
            setDebitNote(debitNote, model);
            return "treasury/document/manageinvoice/debitnote/update";
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }
        return redirect(FinantialInstitutionController.SEARCH_URL, model, redirectAttributes);
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") DebitNote debitNote,
            @RequestParam(value = "documentdate",
                    required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate documentDate,
            @RequestParam(value = "documentduedate",
                    required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate documentDueDate,
            @RequestParam(value = "origindocumentnumber", required = false) String originDocumentNumber,
            @RequestParam(value = "documentobservations", required = false) String documentObservations, Model model,
            RedirectAttributes redirectAttributes) {

        setDebitNote(debitNote, model);

        try {
            assertUserIsAllowToModifyInvoices(debitNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);
            updateDebitNote(documentDate, documentDueDate, originDocumentNumber, documentObservations, model);

            return redirect(READ_URL + getDebitNote(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + tde.getLocalizedMessage(), model);
        } catch (Exception de) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + de.getLocalizedMessage(), model);
        }
        return update(debitNote, model, redirectAttributes);
    }

    @Atomic
    public void updateDebitNote(LocalDate documentDate, LocalDate documentDueDate, String originDocumentNumber,
            String documentObservations, Model model) {
        DebitNote note = getDebitNote(model);
        if (note.isPreparing()) {
            note.edit(note.getPayorDebtAccount(), documentDate, documentDueDate, originDocumentNumber);
        } else {
            note.edit(note.getPayorDebtAccount(), note.getDocumentDate().toLocalDate(), note.getDocumentDueDate(),
                    originDocumentNumber);
        }
        note.setDocumentObservations(documentObservations);
    }

    @RequestMapping(value = "/read/{oid}/addentry")
    public String processReadToAddEntry(@PathVariable("oid") DebitNote debitNote, Model model,
            RedirectAttributes redirectAttributes) {
        setDebitNote(debitNote, model);
        return redirect(DebitEntryController.CREATE_URL + getDebitNote(model).getDebtAccount().getExternalId() + "?debitNote="
                + debitNote.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/closedebitnote", method = RequestMethod.POST)
    public String processReadToCloseDebitNote(@PathVariable("oid") DebitNote debitNote, Model model,
            RedirectAttributes redirectAttributes) {
        setDebitNote(debitNote, model);

        try {

            // assertUserIsAllowToModifyInvoices(debitNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);

            debitNote.closeDocument();

            addInfoMessage(Constants.bundle("label.document.manageinvoice.DebitNote.document.closed.sucess"), model);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + tde.getLocalizedMessage(), model);
        } catch (Exception de) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + de.getLocalizedMessage(), model);
        }
        return redirect(DebitNoteController.READ_URL + getDebitNote(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/anulldebitnote", method = RequestMethod.POST)
    public String processReadToAnullDebitNote(@PathVariable("oid") DebitNote debitNote,
            @RequestParam("reason") String anullReason, Model model, RedirectAttributes redirectAttributes) {
        setDebitNote(debitNote, model);
        try {
            assertUserIsAllowToModifyInvoices(debitNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);

            //Check if any possible InterestBean is in preparing state
            for (DebitEntry entry : debitNote.getDebitEntriesSet()) {
                if (entry.getInterestDebitEntriesSet().isEmpty() == false) {
                    if (entry.getInterestDebitEntriesSet().stream()
                            .anyMatch(x -> x.getFinantialDocument() == null || x.getFinantialDocument().isPreparing())) {
                        throw new TreasuryDomainException("error.DebitNote.creating.credit.note.with.pending.interest.entries");
                    }
                }
            }

            debitNote.anullDebitNoteWithCreditNote(anullReason, false);

            addInfoMessage(
                    BundleUtil.getString(Constants.BUNDLE, "label.document.manageinvoice.DebitNote.document.anulled.sucess"),
                    model);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + tde.getLocalizedMessage(), model);
        } catch (Exception de) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + de.getLocalizedMessage(), model);
        }

        return redirect(DebitNoteController.READ_URL + getDebitNote(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/addpendingentries")
    public String processReadToAddPendingEntries(@PathVariable("oid") DebitNote debitNote, Model model,
            RedirectAttributes redirectAttributes) {
        setDebitNote(debitNote, model);
        return redirect(DebitEntryController.SEARCHPENDINGENTRIES_URL + "?debitnote=" + debitNote.getExternalId(), model,
                redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/createcreditnote")
    public String processReadToCreateCreditNote(@PathVariable("oid") DebitNote debitNote, Model model,
            RedirectAttributes redirectAttributes) {
        setDebitNote(debitNote, model);
        return redirect(CreditNoteController.CREATE_URL + "?debitNote=" + debitNote.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/exportintegrationfile", produces = "text/xml;charset=Windows-1252")
    public void processReadToExportIntegrationFile(@PathVariable("oid") DebitNote debitNote, Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) {
        try {
            assertUserIsFrontOfficeMember(debitNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);
            final IERPExporter erpExporter = debitNote.getDebtAccount().getFinantialInstitution().getErpIntegrationConfiguration()
                    .getERPExternalServiceImplementation().getERPExporter();

            String output =
                    erpExporter
                            .exportFinantialDocumentToXML(debitNote.getDebtAccount().getFinantialInstitution(),
                                    debitNote
                                            .findRelatedDocuments(new HashSet<FinantialDocument>(),
                                                    debitNote.getDebtAccount().getFinantialInstitution()
                                                            .getErpIntegrationConfiguration().getExportAnnulledRelatedDocuments())
                                            .stream().collect(Collectors.toList()));

            response.setContentType("text/xml");
            response.setCharacterEncoding("Windows-1252");
            String filename = URLEncoder.encode(StringNormalizer
                    .normalizePreservingCapitalizedLetters((debitNote.getDebtAccount().getFinantialInstitution().getFiscalNumber()
                            + "_" + debitNote.getUiDocumentNumber() + ".xml").replaceAll("/", "_").replaceAll("\\s", "_")
                                    .replaceAll(" ", "_")),
                    "Windows-1252");
            response.setHeader("Content-disposition", "attachment; filename=" + filename);

            response.getOutputStream().write(output.getBytes("Windows-1252"));
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            try {
                response.sendRedirect(redirect(READ_URL + debitNote.getExternalId(), model, redirectAttributes));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/read/{oid}/calculateinterestvalue", method = RequestMethod.GET)
    public String processReadToCalculateInterestValue(@PathVariable("oid") DebitNote debitNote,
            @RequestParam("paymentdate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate paymentDate, Model model,
            RedirectAttributes redirectAttributes) {

        try {
            assertUserIsFrontOfficeMember(debitNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);

            if (paymentDate == null) {
                throw new TreasuryDomainException("error.label.DebitNote.create.interest.note.invalid.payment.date");
            }

            List<InterestEntryBean> allInterests = new ArrayList<InterestEntryBean>();

            for (DebitEntry entry : debitNote.getDebitEntriesSet()) {
                InterestRateBean calculateUndebitedInterestValue = entry.calculateUndebitedInterestValue(paymentDate);

                if (Constants.isGreaterThan(calculateUndebitedInterestValue.getInterestAmount(), BigDecimal.ZERO)) {
                    InterestEntryBean entryBean = new InterestEntryBean(entry, calculateUndebitedInterestValue);
                    allInterests.add(entryBean);
                }
            }

            List<DocumentNumberSeries> availableSeries = org.fenixedu.treasury.domain.document.DocumentNumberSeries
                    .find(FinantialDocumentType.findForDebitNote(),
                            debitNote.getDocumentNumberSeries().getSeries().getFinantialInstitution())
                    .filter(x -> x.getSeries().getActive() == true).collect(Collectors.toList());

            availableSeries = DocumentNumberSeries.applyActiveSelectableAndDefaultSorting(availableSeries.stream())
                    .collect(Collectors.toList());
            if (availableSeries.size() > 0) {
                model.addAttribute("DebitNote_documentNumberSeries_options", availableSeries);
            } else {
                addErrorMessage(BundleUtil.getString(Constants.BUNDLE,
                        "label.error.document.manageinvoice.finantialinstitution.no.available.series.found"), model);
                return redirect(DebitNoteController.READ_URL + debitNote.getExternalId(), model, redirectAttributes);
            }

            setDebitNote(debitNote, model);
            model.addAttribute("interestRateValues", allInterests);

            addWarningMessage(
                    BundleUtil.getString(Constants.BUNDLE, "label.warning.document.manageinvoice.calculateinterestvalue.line1"),
                    model);
            addWarningMessage(
                    BundleUtil.getString(Constants.BUNDLE, "label.warning.document.manageinvoice.calculateinterestvalue.line2"),
                    model);
            return "treasury/document/manageinvoice/debitnote/calculateinterestvalue";
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            return redirect(DebitNoteController.READ_URL + debitNote.getExternalId(), model, redirectAttributes);
        }
    }

    @RequestMapping(value = "/read/{oid}/calculateinterestvalue", method = RequestMethod.POST)
    public String processReadToCalculateInterestValue(@PathVariable("oid") DebitNote debitNote,
            @RequestParam("paymentdate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate paymentDate,
            @RequestParam("documentnumberseries") DocumentNumberSeries documentNumberSeries,
            @RequestParam("documentobservations") String documentObservations, Model model,
            RedirectAttributes redirectAttributes) {

        try {
            assertUserIsFrontOfficeMember(debitNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);

            if (paymentDate == null) {
                throw new TreasuryDomainException("error.label.DebitNote.create.interest.note.invalid.payment.date");
            }

            if (debitNote.getDocumentNumberSeries().getSeries().getFinantialInstitution() != documentNumberSeries.getSeries()
                    .getFinantialInstitution()) {
                throw new TreasuryDomainException("error.DebitNote.invalid.series.for.interest.debit.note.creation");
            }
            DebitNote interestDebitNote =
                    createInterestDebitNoteForDebitNote(debitNote, paymentDate, documentNumberSeries, documentObservations);

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE,
                    "info.document.manageinfoice.debitnote.success.calculate.interest.value"), model);

            return redirect(DebitNoteController.READ_URL + interestDebitNote.getExternalId(), model, redirectAttributes);

        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            return "treasury/document/manageinvoice/debitnote/calculateinterestvalue";
        }
    }

    @Atomic
    private DebitNote createInterestDebitNoteForDebitNote(DebitNote debitNote, LocalDate paymentDate,
            DocumentNumberSeries documentNumberSeries, String documentObservations) {
        DebitNote interestDebitNote;
        if (documentNumberSeries.getSeries().getCertificated()) {
            interestDebitNote =
                    DebitNote.createInterestDebitNoteForDebitNote(debitNote, documentNumberSeries, new DateTime(), paymentDate);
        } else {
            interestDebitNote = DebitNote.createInterestDebitNoteForDebitNote(debitNote, documentNumberSeries,
                    paymentDate.toDateTimeAtStartOfDay(), paymentDate);
        }
        interestDebitNote.setDocumentObservations(documentObservations);
        return interestDebitNote;
    }

    @RequestMapping(value = "/read/{oid}/exportintegrationonline")
    public String processReadToExportIntegrationOnline(@PathVariable("oid") DebitNote debitNote, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(debitNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);

            try {
                final IERPExporter erpExporter = debitNote.getDebtAccount().getFinantialInstitution()
                        .getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPExporter();

                //Force a check status first of the document 
                erpExporter.checkIntegrationDocumentStatus(debitNote);
            } catch (Exception ex) {

            }

            List<FinantialDocument> documentsToExport = Collections.singletonList(debitNote);
            final IERPExporter erpExporter = debitNote.getDebtAccount().getFinantialInstitution().getErpIntegrationConfiguration()
                    .getERPExternalServiceImplementation().getERPExporter();

            ERPExportOperation output = erpExporter.exportFinantialDocumentToIntegration(
                    debitNote.getDebtAccount().getFinantialInstitution(), documentsToExport);
            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.integration.erp.exportoperation.success"), model);
            return redirect(ERPExportOperationController.READ_URL + output.getExternalId(), model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.integration.erp.exportoperation.error")
                    + ex.getLocalizedMessage(), model);
        }
        setDebitNote(debitNote, model);
        return read(debitNote, model, redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/cleardocumenttoexport", method = RequestMethod.POST)
    public String cleardocumenttoexport(@PathVariable("oid") final DebitNote debitNote,
            @RequestParam(value = "reason", required = false) final String reason, final Model model,
            final RedirectAttributes redirectAttributes) {
        try {

            if (!debitNote.isDocumentToExport()) {
                addErrorMessage(Constants.bundle("error.FinantialDocument.document.not.marked.to.export"), model);
                return redirect(READ_URL + debitNote.getExternalId(), model, redirectAttributes);
            }

            if (Strings.isNullOrEmpty(reason)) {
                addErrorMessage(Constants.bundle("error.FinantialDocument.clear.document.to.export.requires.reason"), model);
                return redirect(READ_URL + debitNote.getExternalId(), model, redirectAttributes);
            }

            assertUserIsBackOfficeMember(model);

            debitNote.clearDocumentToExport(reason);

            return redirect(READ_URL + debitNote.getExternalId(), model, redirectAttributes);
        } catch (final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
            return redirect(READ_URL + debitNote.getExternalId(), model, redirectAttributes);
        }
    }

    private static final String UPDATE_PAYOR_DEBT_ACCOUNT_URI = "/updatepayordebtaccount";
    public static final String UPDATE_PAYOR_DEBT_ACCOUNT_URL = CONTROLLER_URL + UPDATE_PAYOR_DEBT_ACCOUNT_URI;

    @RequestMapping(value = UPDATE_PAYOR_DEBT_ACCOUNT_URI + "/{oid}", method = RequestMethod.GET)
    public String updatepayordebtaccount(@PathVariable("oid") final DebitNote debitNote, final Model model,
            final RedirectAttributes redirectAttributes) {
        try {
            assertUserIsAllowToModifyInvoices(debitNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);

            if (!debitNote.isPreparing() && !debitNote.isClosed()) {
                addErrorMessage(Constants.bundle("error.DebitNote.updatePayorDebtAccount.not.preparing.nor.closed"), model);
                return redirect(READ_URL + debitNote.getExternalId(), model, redirectAttributes);
            }

            setDebitNote(debitNote, model);

            model.addAttribute("DebitNote_payorDebtAccount_options",
                    DebtAccount.findAdhocDebtAccountsSortedByCustomerName(debitNote.getDebtAccount().getFinantialInstitution()));

            model.addAttribute("stateValues", org.fenixedu.treasury.domain.document.FinantialDocumentStateType.values());

            return "treasury/document/manageinvoice/debitnote/updatepayordebtaccount";
        } catch (final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
        }

        return redirect(READ_URL + debitNote.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = UPDATE_PAYOR_DEBT_ACCOUNT_URI + "/{oid}", method = RequestMethod.POST)
    public String updatepayordebtaccount(@PathVariable("oid") final DebitNote debitNote,
            @RequestParam(value = "payordebtaccount", required = false) DebtAccount payorDebtAccount, final Model model,
            final RedirectAttributes redirectAttributes) {
        try {
            final DebtAccount oldPayorDebtAccount = debitNote.getPayorDebtAccount();

            final DebitNote newDebitNote = debitNote.updatePayorDebtAccount(payorDebtAccount);

            if (oldPayorDebtAccount != newDebitNote.getPayorDebtAccount()) {
                addInfoMessage(Constants.bundle("label.DebitNote.update.payorDebtAccount.success"), model);
                return redirect(READ_URL + newDebitNote.getExternalId(), model, redirectAttributes);
            } else {
                addErrorMessage(Constants.bundle("error.DebitNote.updatePayorDebtAccount.payor.not.changed"), model);
            }
        } catch (final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
        }

        return updatepayordebtaccount(debitNote, model, redirectAttributes);
    }

    private static final String VIEW_ERP_CUSTOMER_DATA_URI = "/viewerpcustomerfields";
    public static final String VIEW_ERP_CUSTOMER_DATA_URL = CONTROLLER_URL + VIEW_ERP_CUSTOMER_DATA_URI;

    @RequestMapping(value = VIEW_ERP_CUSTOMER_DATA_URI + "/{oid}", method = RequestMethod.GET)
    public String viewerpcustomerdata(@PathVariable("oid") final DebitNote debitNote, final Model model,
            final RedirectAttributes redirectAttributes) {
        try {
            assertUserIsAllowToModifyInvoices(debitNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);

            model.addAttribute("invoice", debitNote);
            model.addAttribute("backUrl", READ_URL + "/" + debitNote.getExternalId());

            return "treasury/document/manageinvoice/erpcustomerfields/read";
        } catch (final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
        }

        return redirect(READ_URL + debitNote.getExternalId(), model, redirectAttributes);
    }

    private static final String _DOWNLOAD_CERTIFIED_DOCUMENT_PRINT_URI = "/downloadcertifieddocumentprint";
    public static final String DOWNLOAD_CERTIFIED_DOCUMENT_PRINT_URL = CONTROLLER_URL + _DOWNLOAD_CERTIFIED_DOCUMENT_PRINT_URI;

    @RequestMapping(value = _DOWNLOAD_CERTIFIED_DOCUMENT_PRINT_URI + "/{oid}", method = RequestMethod.GET)
    public String downloadcertifieddocumentprint(@PathVariable("oid") final DebitNote debitNote, final Model model,
            final RedirectAttributes redirectAttributes, final HttpServletResponse response) {

        try {
            final byte[] contents = ERPExporterManager.downloadCertifiedDocumentPrint(debitNote);
            
            response.setContentType("application/pdf");
            String filename = URLEncoder.encode(StringNormalizer
                    .normalizePreservingCapitalizedLetters((debitNote.getDebtAccount().getFinantialInstitution().getFiscalNumber()
                            + "_" + debitNote.getUiDocumentNumber() + ".pdf").replaceAll("/", "_").replaceAll("\\s", "_")
                                    .replaceAll(" ", "_")),
                    "Windows-1252");
            
            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            response.getOutputStream().write(contents);
            
            return null;
        } catch (final TreasuryDomainException | IOException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
            return redirect(READ_URL + "/" + debitNote.getExternalId(), model, redirectAttributes);
        }
    }

}
