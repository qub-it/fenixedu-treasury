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
package org.fenixedu.treasury.ui.document.managepayments;

import static org.fenixedu.treasury.util.TreasuryConstants.treasuryBundle;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.StringNormalizer;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.PaymentMethod;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.CreditNote;
import org.fenixedu.treasury.domain.document.FinantialDocumentStateType;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.document.PaymentEntry;
import org.fenixedu.treasury.domain.document.ReimbursementEntry;
import org.fenixedu.treasury.domain.document.SettlementEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.dto.ISettlementInvoiceEntryBean;
import org.fenixedu.treasury.dto.InterestRateBean;
import org.fenixedu.treasury.dto.SettlementNoteBean;
import org.fenixedu.treasury.dto.SettlementNoteBean.CreditEntryBean;
import org.fenixedu.treasury.dto.SettlementNoteBean.DebitEntryBean;
import org.fenixedu.treasury.dto.SettlementNoteBean.InterestEntryBean;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.fenixedu.treasury.services.integration.erp.ERPExporterManager;
import org.fenixedu.treasury.services.integration.erp.IERPExporter;
import org.fenixedu.treasury.services.reports.DocumentPrinter;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController;
import org.fenixedu.treasury.ui.integration.erp.ERPExportOperationController;
import org.fenixedu.treasury.util.TreasuryConstants;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qubit.terra.docs.util.ReportGenerationException;

import pt.ist.fenixframework.Atomic;

//@Component("org.fenixedu.treasury.ui.document.managePayments") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.document.managePayments",
        accessGroup = "treasuryFrontOffice")
@RequestMapping(SettlementNoteController.CONTROLLER_URL)
public class SettlementNoteController extends TreasuryBaseController {
    public static final String CONTROLLER_URL = "/treasury/document/managepayments/settlementnote";
    private static final String SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + SEARCH_URI;
    private static final String UPDATE_URI = "/update/";
    public static final String UPDATE_URL = CONTROLLER_URL + UPDATE_URI;
    private static final String CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + CREATE_URI;
    private static final String READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + READ_URI;
    private static final String DELETE_URI = "/delete/";
    public static final String DELETE_URL = CONTROLLER_URL + DELETE_URI;
    private static final String CHOOSE_INVOICE_ENTRIES_URI = "/chooseInvoiceEntries/";
    public static final String CHOOSE_INVOICE_ENTRIES_URL = CONTROLLER_URL + CHOOSE_INVOICE_ENTRIES_URI;
    private static final String CALCULATE_INTEREST_URI = "/calculateInterest/";
    public static final String CALCULATE_INTEREST_URL = CONTROLLER_URL + CALCULATE_INTEREST_URI;
    private static final String CREATE_DEBIT_NOTE_URI = "/createDebitNote/";
    public static final String CREATE_DEBIT_NOTE_URL = CONTROLLER_URL + CREATE_DEBIT_NOTE_URI;
    private static final String INSERT_PAYMENT_URI = "/insertpayment/";
    public static final String INSERT_PAYMENT_URL = CONTROLLER_URL + INSERT_PAYMENT_URI;
    private static final String SUMMARY_URI = "/summary/";
    public static final String SUMMARY_URL = CONTROLLER_URL + SUMMARY_URI;
    private static final String TRANSACTIONS_SUMMARY_URI = "/transactions/summary/";
    public static final String TRANSACTIONS_SUMMARY_URL = CONTROLLER_URL + TRANSACTIONS_SUMMARY_URI;

    public static final long SEARCH_LIMIT_SIZE = 500;
    public static final long SEARCH_SETTLEMENT_ENTRY_LIMIT_DAYS_PERIOD = 30;

    @RequestMapping
    public String home(Model model) {
        return "forward:" + SEARCH_URL;
    }

    private void setSettlementNoteBean(SettlementNoteBean bean, Model model) {
        model.addAttribute("settlementNoteBeanJson", getBeanJson(bean));
        model.addAttribute("settlementNoteBean", bean);
    }

    private SettlementNote getSettlementNote(Model model) {
        return (SettlementNote) model.asMap().get("settlementNote");
    }

    private void setSettlementNote(SettlementNote settlementNote, Model model) {
        model.addAttribute("settlementNote", settlementNote);
    }

    @Atomic
    public void deleteSettlementNote(SettlementNote settlementNote) {
        settlementNote.delete(true);
    }

    @RequestMapping(value = CHOOSE_INVOICE_ENTRIES_URI + "{debtAccountId}/{reimbursementNote}")
    public String chooseInvoiceEntries(@PathVariable(value = "debtAccountId") DebtAccount debtAccount,
            @PathVariable(value = "reimbursementNote") boolean reimbursementNote,
            @RequestParam(value = "bean", required = false) SettlementNoteBean bean, Model model) {
        assertUserIsAllowToModifySettlements(debtAccount.getFinantialInstitution(), model);
        if (bean == null) {
            bean = new SettlementNoteBean(debtAccount, reimbursementNote, false);
        }
        setSettlementNoteBean(bean, model);
        return "treasury/document/managepayments/settlementnote/chooseInvoiceEntries";
    }

    @RequestMapping(value = CHOOSE_INVOICE_ENTRIES_URI, method = RequestMethod.POST)
    public String chooseInvoiceEntries(@RequestParam(value = "bean", required = true) SettlementNoteBean bean, Model model) {
        BigDecimal debitSum = BigDecimal.ZERO;
        BigDecimal creditSum = BigDecimal.ZERO;
        boolean error = false;
        assertUserIsAllowToModifySettlements(bean.getDebtAccount().getFinantialInstitution(), model);

        final Set<InvoiceEntry> invoiceEntriesSet = Sets.newHashSet();
        for (int i = 0; i < bean.getDebitEntries().size(); i++) {
            DebitEntryBean debitEntryBean = bean.getDebitEntries().get(i);
            if (debitEntryBean.isIncluded()) {
                invoiceEntriesSet.add(debitEntryBean.getDebitEntry());

                if (debitEntryBean.getDebtAmountWithVat() == null
                        || debitEntryBean.getDebtAmountWithVat().compareTo(BigDecimal.ZERO) <= 0) {
                    debitEntryBean.setNotValid(true);
                    error = true;
                    addErrorMessage(treasuryBundle("error.DebitEntry.debtAmount.equal.zero", Integer.toString(i + 1)), model);
                } else if (debitEntryBean.getDebtAmountWithVat().compareTo(debitEntryBean.getDebitEntry().getOpenAmount()) > 0) {
                    debitEntryBean.setNotValid(true);
                    error = true;
                    addErrorMessage(treasuryBundle("error.DebitEntry.exceeded.openAmount", Integer.toString(i + 1)), model);
                } else {
                    debitEntryBean.setNotValid(false);
                }

                if (!debitEntryBean.isNotValid()) {
                    //Always perform the sum, in order to verify if creditSum is not higher than debitSum
                    debitSum = debitSum.add(debitEntryBean.getDebtAmountWithVat());
                }
            } else {
                debitEntryBean.setNotValid(false);
            }
        }
        for (int i = 0; i < bean.getCreditEntries().size(); i++) {
            final CreditEntryBean creditEntryBean = bean.getCreditEntries().get(i);

            if (creditEntryBean.isIncluded()) {
                invoiceEntriesSet.add(creditEntryBean.getCreditEntry());

                if (creditEntryBean.getCreditAmountWithVat() == null
                        || creditEntryBean.getCreditAmountWithVat().compareTo(BigDecimal.ZERO) <= 0) {
                    creditEntryBean.setNotValid(true);
                    error = true;
                    addErrorMessage(treasuryBundle("error.CreditEntry.creditAmount.equal.zero", Integer.toString(i + 1)), model);
                } else if (creditEntryBean.getCreditAmountWithVat()
                        .compareTo(creditEntryBean.getCreditEntry().getOpenAmount()) > 0) {
                    creditEntryBean.setNotValid(true);
                    error = true;
                    addErrorMessage(treasuryBundle("error.CreditEntry.exceeded.openAmount", Integer.toString(i + 1)), model);
                } else {
                    creditEntryBean.setNotValid(false);
                }

                if (!creditEntryBean.isNotValid()) {
                    creditSum = creditSum.add(creditEntryBean.getCreditAmountWithVat());
                }
            } else {
                creditEntryBean.setNotValid(false);
            }
        }

        if (bean.isReimbursementNote() && creditSum.compareTo(debitSum) < 0) {
            error = true;
            addErrorMessage(treasuryBundle("error.SettlementNote.positive.payment.value"), model);
        }

        if (!bean.isReimbursementNote() && creditSum.compareTo(debitSum) > 0) {
            error = true;
            addErrorMessage(treasuryBundle("error.SettlementNote.negative.payment.value"), model);
        }

        if (bean.isReimbursementNote() && creditSum.compareTo(BigDecimal.ZERO) == 0) {
            error = true;
            addErrorMessage(treasuryBundle("error.CreditEntry.no.creditEntries.selected"), model);
        }

        if (!bean.isReimbursementNote() && !bean.isAdvancePayment() && debitSum.compareTo(BigDecimal.ZERO) == 0) {
            error = true;
            addErrorMessage(treasuryBundle("error.DebiEntry.no.debitEntries.selected"), model);
        }

        if (bean.getDate().isAfter(new LocalDate())) {
            error = true;
            addErrorMessage(treasuryBundle("error.SettlementNote.date.is.after"), model);
        }

        if (bean.getDocNumSeries() == null) {
            error = true;
            addErrorMessage(treasuryBundle("error.SettlementNote.need.documentSeries"), model);
        }

        if (bean.getReferencedCustomers().size() > 1) {
            error = true;
            addErrorMessage(treasuryBundle("error.SettlementNote.referencedCustomers.only.one.allowed"), model);
        }

        if (!error && bean.isReimbursementNote() && bean.getCreditEntries().stream().filter(ce -> ce.isIncluded()).count() != 1) {
            error = true;
            addErrorMessage(treasuryBundle("error.SettlementNote.reimbursement.supports.only.one.settlement.entry"), model);
        }

        if (!bean.isReimbursementNote() && !bean.checkAdvancePaymentCreditsWithPaymentDate()) {
            final SettlementNote settlementNote = bean.getLastPaidAdvancedCreditSettlementNote().get();

            error = true;
            addErrorMessage(treasuryBundle("error.SettlementNote.advancedPaymentCredit.originSettlementNote.payment.date.after",
                    settlementNote.getAdvancedPaymentCreditNote().getUiDocumentNumber(),
                    settlementNote.getPaymentDate().toLocalDate().toString(TreasuryConstants.STANDARD_DATE_FORMAT_YYYY_MM_DD)),
                    model);
        }

        try {
            SettlementNote.checkMixingOfInvoiceEntriesExportedInLegacyERP(invoiceEntriesSet);
        } catch (final TreasuryDomainException e) {
            error = true;
            addErrorMessage(e.getLocalizedMessage(), model);
        }

        if (error) {
            setSettlementNoteBean(bean, model);
            return "treasury/document/managepayments/settlementnote/chooseInvoiceEntries";
        }

        bean.setInterestEntries(new ArrayList<InterestEntryBean>());
        for (DebitEntryBean debitEntryBean : bean.getDebitEntries()) {
            if (debitEntryBean.isIncluded() && TreasuryConstants.isEqual(debitEntryBean.getDebitEntry().getOpenAmount(),
                    debitEntryBean.getDebtAmount())) {

                //Calculate interest only if we are making a FullPayment
                InterestRateBean debitInterest = debitEntryBean.getDebitEntry().calculateUndebitedInterestValue(bean.getDate());
                if (TreasuryConstants.isPositive(debitInterest.getInterestAmount())) {
                    InterestEntryBean interestEntryBean = new InterestEntryBean(debitEntryBean.getDebitEntry(), debitInterest);
                    bean.getInterestEntries().add(interestEntryBean);
                }
            }
        }
        if (bean.getInterestEntries().size() == 0) {
            return calculateInterest(bean, model);
        }
        setSettlementNoteBean(bean, model);
        return "treasury/document/managepayments/settlementnote/calculateInterest";
    }

    @RequestMapping(value = CALCULATE_INTEREST_URI, method = RequestMethod.POST)
    public String calculateInterest(@RequestParam(value = "bean", required = true) SettlementNoteBean bean, Model model) {
        try {
            assertUserIsAllowToModifySettlements(bean.getDebtAccount().getFinantialInstitution(), model);

            SettlementNote.checkMixingOfInvoiceEntriesExportedInLegacyERP(bean.getIncludedInvoiceEntryBeans());

            for (DebitEntryBean debitEntryBean : bean.getDebitEntries()) {
                if (debitEntryBean.isIncluded() && debitEntryBean.getDebitEntry().getFinantialDocument() == null) {
                    setSettlementNoteBean(bean, model);
                    return "treasury/document/managepayments/settlementnote/createDebitNote";
                }
            }

            for (InterestEntryBean interestEntryBean : bean.getInterestEntries()) {
                if (interestEntryBean.isIncluded()) {
                    setSettlementNoteBean(bean, model);
                    return "treasury/document/managepayments/settlementnote/createDebitNote";
                }
            }
        } catch (TreasuryDomainException tde) {
            addErrorMessage(tde.getLocalizedMessage(), model);

            setSettlementNoteBean(bean, model);
            return "treasury/document/managepayments/settlementnote/calculateInterest";
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);

            setSettlementNoteBean(bean, model);
            return "treasury/document/managepayments/settlementnote/calculateInterest";
        }

        return createDebitNote(bean, model);
    }

    @RequestMapping(value = CREATE_DEBIT_NOTE_URI, method = RequestMethod.POST)
    public String createDebitNote(@RequestParam(value = "bean", required = true) SettlementNoteBean bean, Model model) {
        if (!bean.isAdvancePayment() && bean.getDebtAmountWithVat().compareTo(BigDecimal.ZERO) == 0) {
            return insertPayment(bean, model);
        }
        setSettlementNoteBean(bean, model);

        if (bean.isReimbursementWithCompensation()) {
            return "treasury/document/managepayments/settlementnote/reimbursementWithCompensation";
        }

        return "treasury/document/managepayments/settlementnote/insertPayment";
    }

    private static final String CONTINUE_TO_INSERT_PAYMENT_URI = "/continueToInsertPayment/";
    public static final String CONTINUE_TO_INSERT_PAYMENT_URL = CONTROLLER_URL + CONTINUE_TO_INSERT_PAYMENT_URI;

    @RequestMapping(value = CONTINUE_TO_INSERT_PAYMENT_URI, method = RequestMethod.POST)
    public String continueToInsertPayment(@RequestParam(value = "bean", required = true) SettlementNoteBean bean, Model model) {
        setSettlementNoteBean(bean, model);
        return "treasury/document/managepayments/settlementnote/insertPayment";
    }

    @RequestMapping(value = INSERT_PAYMENT_URI, method = RequestMethod.POST)
    public String insertPayment(@RequestParam(value = "bean", required = true) SettlementNoteBean bean, Model model) {
        assertUserIsAllowToModifySettlements(bean.getDebtAccount().getFinantialInstitution(), model);
        BigDecimal debitSum = bean.isReimbursementNote() ? bean.getDebtAmountWithVat().negate() : bean.getDebtAmountWithVat();
        BigDecimal paymentSum = bean.getPaymentAmount();
        boolean error = false;

        if (bean.getPaymentEntries().size() > 1) {
            error = true;
            addErrorMessage(treasuryBundle("error.SettlementNote.only.one.payment.method.is.supported"), model);
        }

        if (bean.getPaymentEntries().stream().anyMatch(peb -> TreasuryConstants.isZero(peb.getPaymentAmount()))) {
            error = true;
            String errorMessage = bean
                    .isReimbursementNote() ? "error.SettlementNote.reimbursement.equal.zero" : "error.SettlementNote.payment.equal.zero";
            addErrorMessage(treasuryBundle(errorMessage), model);
        }

        if (bean.isAdvancePayment() && TreasuryConstants.isLessThan(paymentSum, debitSum)) {
            error = true;
            final String errorMessage = bean
                    .isReimbursementNote() ? "error.SettlementNote.no.match.reimbursement.credit" : "error.SettlementNote.no.match.payment.debit";
            addErrorMessage(treasuryBundle(errorMessage), model);
        } else if (!bean.isAdvancePayment() && !TreasuryConstants.isEqual(paymentSum, debitSum)) {
            error = true;
            final String errorMessage = bean
                    .isReimbursementNote() ? "error.SettlementNote.no.match.reimbursement.credit" : "error.SettlementNote.no.match.payment.debit";
            addErrorMessage(treasuryBundle(errorMessage), model);
        }

        if (error) {
            setSettlementNoteBean(bean, model);
            return "treasury/document/managepayments/settlementnote/insertPayment";
        }

        setSettlementNoteBean(bean, model);
        return "treasury/document/managepayments/settlementnote/summary";
    }

    @RequestMapping(value = SUMMARY_URI, method = RequestMethod.POST)
    public String summary(@RequestParam(value = "bean", required = true) SettlementNoteBean bean, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            assertUserIsAllowToModifySettlements(bean.getDebtAccount().getFinantialInstitution(), model);
            SettlementNote settlementNote = processSettlementNoteCreation(bean);
            addInfoMessage(treasuryBundle("label.SettlementNote.create.success"), model);
            return redirect(READ_URL + settlementNote.getExternalId(), model, redirectAttributes);
        } catch (final TreasuryDomainException tde) {
            addErrorMessage(tde.getLocalizedMessage(), model);
        }
        setSettlementNoteBean(bean, model);
        return "treasury/document/managepayments/settlementnote/summary";
    }

    @Atomic
    public SettlementNote processSettlementNoteCreation(SettlementNoteBean bean) {
        DateTime documentDate = new DateTime();
        if (bean.getDocNumSeries().getSeries().getCertificated() == false) {
            documentDate = bean.getDate().toDateTimeAtStartOfDay();
        }
        SettlementNote settlementNote = SettlementNote.create(bean.getDebtAccount(), bean.getDocNumSeries(), documentDate,
                bean.getDate().toDateTimeAtStartOfDay(), bean.getOriginDocumentNumber(),
                !Strings.isNullOrEmpty(bean.getFinantialTransactionReference()) ? bean.getFinantialTransactionReferenceYear()
                        + "/" + bean.getFinantialTransactionReference() : "");
        settlementNote.processSettlementNoteCreation(bean);
        settlementNote.closeDocument();
        return settlementNote;
    }

    @RequestMapping(value = SEARCH_URI)
    public String search(@RequestParam(value = "debtaccount", required = false) DebtAccount debtAccount,
            @RequestParam(value = "documentnumber", required = false) String documentNumber,
            @RequestParam(value = "documentdatefrom",
                    required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate documentDateFrom,
            @RequestParam(value = "documentdateto",
                    required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate documentDateTo,
            @RequestParam(value = "origindocumentnumber", required = false) String originDocumentNumber,
            @RequestParam(value = "state", required = false) FinantialDocumentStateType state, Model model) {

        final List<SettlementNote> result =
                filterSearch(debtAccount, documentNumber, documentDateFrom, documentDateTo, originDocumentNumber, state);
        model.addAttribute("limit_exceeded", result.size() > SEARCH_LIMIT_SIZE);
        model.addAttribute("searchsettlementnoteResultsDataSet_totalCount", result.size());
        model.addAttribute("searchsettlementnoteResultsDataSet",
                result.stream().limit(SEARCH_LIMIT_SIZE).collect(Collectors.toList()));

        model.addAttribute("stateValues", FinantialDocumentStateType.findAll());

        return "treasury/document/managepayments/settlementnote/search";
    }

    static private List<SettlementNote> getSearchUniverse(final DebtAccount debtAccount) {
        final Stream<SettlementNote> result =
                debtAccount == null ? SettlementNote.findAll() : SettlementNote.findByDebtAccount(debtAccount);
        return result.collect(Collectors.toList());
    }

    private List<SettlementNote> filterSearch(DebtAccount debtAccount, String documentNumber, LocalDate documentDateFrom,
            LocalDate documentDateTo, String originDocumentNumber, FinantialDocumentStateType state) {

        final List<SettlementNote> result = Lists.newArrayList();
        // FIXME legidio, wish there was a way to test an empty Predicate
        boolean filter = false;

        Predicate<SettlementNote> predicate = i -> FinantialDocumentType.findForSettlementNote() == i.getFinantialDocumentType()
                || FinantialDocumentType.findForReimbursementNote() == i.getFinantialDocumentType();
        if (debtAccount != null) {
            filter = true;
        }
        if (!Strings.isNullOrEmpty(documentNumber)) {
            filter = true;
            predicate = predicate.and(i -> !Strings.isNullOrEmpty(i.getDocumentNumber())
                    && i.getUiDocumentNumber().toLowerCase().contains(documentNumber.toLowerCase()));
        }
        if (documentDateFrom != null) {
            filter = true;
            predicate = predicate.and(i -> i.getDocumentDate().toLocalDate().isEqual(documentDateFrom)
                    || i.getDocumentDate().toLocalDate().isAfter(documentDateFrom));
        }
        if (documentDateTo != null) {
            filter = true;
            predicate = predicate.and(i -> i.getDocumentDate().toLocalDate().isEqual(documentDateTo)
                    || i.getDocumentDate().toLocalDate().isBefore(documentDateTo));
        }
        if (!StringUtils.isEmpty(originDocumentNumber)) {
            filter = true;
            predicate = predicate.and(i -> !StringUtils.isEmpty(i.getOriginDocumentNumber())
                    && i.getOriginDocumentNumber().toLowerCase().contains(originDocumentNumber.trim().toLowerCase()));
        }
        if (state != null) {
            filter = true;
            predicate = predicate.and(i -> state == i.getState());
        }

        if (filter) {
            getSearchUniverse(debtAccount).stream().filter(predicate).collect(Collectors.toCollection(() -> result));
        }

        return result;
    }

    @RequestMapping(value = "/search/view/{oid}")
    public String processSearchToViewAction(@PathVariable("oid") SettlementNote settlementNote, Model model,
            RedirectAttributes redirectAttributes) {
        return redirect(READ_URL + settlementNote.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") final SettlementNote settlementNote, final Model model) {
        setSettlementNote(settlementNote, model);

        final String loggedUsername = TreasuryPlataformDependentServicesFactory.implementation().getLoggedUsername();
        final boolean allowToConditionallyAnnulSettlementNote = TreasuryAccessControlAPI.isAllowToConditionallyAnnulSettlementNote(
                loggedUsername, settlementNote);

        final boolean allowToAnnulSettlementNoteWithoutAnyRestriction = TreasuryAccessControlAPI.isAllowToAnnulSettlementNoteWithoutAnyRestriction(loggedUsername, settlementNote);
        
        model.addAttribute("allowToConditionallyAnnulSettlementNote", allowToConditionallyAnnulSettlementNote);
        model.addAttribute("allowToAnnulSettlementNoteWithoutAnyRestriction", allowToAnnulSettlementNoteWithoutAnyRestriction);
        
        return "treasury/document/managepayments/settlementnote/read";
    }

    @RequestMapping(value = DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") SettlementNote settlementNote, Model model, RedirectAttributes redirectAttributes) {

        setSettlementNote(settlementNote, model);
        try {
            DebtAccount debtAccount = settlementNote.getDebtAccount();

            assertUserIsAllowToModifySettlements(debtAccount.getFinantialInstitution(), model);

            deleteSettlementNote(settlementNote);

            addInfoMessage(treasuryBundle("label.success.delete"), model);
            return redirect(DebtAccountController.READ_URL + debtAccount.getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(treasuryBundle("label.error.delete") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(treasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);
        }

        return redirect(READ_URL + getSettlementNote(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") SettlementNote settlementNote, Model model) {
        model.addAttribute("SettlementNote_finantialDocumentType_options",
                org.fenixedu.treasury.domain.document.FinantialDocumentType.findAll());
        model.addAttribute("SettlementNote_debtAccount_options", new ArrayList<org.fenixedu.treasury.domain.debt.DebtAccount>()); // CHANGE_ME
        model.addAttribute("SettlementNote_documentNumberSeries_options",
                org.fenixedu.treasury.domain.document.DocumentNumberSeries.findAll());
        model.addAttribute("SettlementNote_currency_options", org.fenixedu.treasury.domain.Currency.findAll());
        model.addAttribute("stateValues", org.fenixedu.treasury.domain.document.FinantialDocumentStateType.values());

        setSettlementNote(settlementNote, model);

        return "treasury/document/managepayments/settlementnote/update";
    }

    //
    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") SettlementNote settlementNote,
            @RequestParam(value = "origindocumentnumber", required = false) java.lang.String originDocumentNumber,
            @RequestParam(value = "documentobservations", required = false) java.lang.String documentObservations, Model model,
            RedirectAttributes redirectAttributes) {

        setSettlementNote(settlementNote, model);

        try {
            assertUserIsAllowToModifySettlements(settlementNote.getDebtAccount().getFinantialInstitution(), model);

            getSettlementNote(model).updateSettlementNote(originDocumentNumber, documentObservations);

            return redirect(READ_URL + getSettlementNote(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(treasuryBundle("label.error.update") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(treasuryBundle("label.error.update") + ex.getLocalizedMessage(), model);
        }
        return update(settlementNote, model);
    }

    @RequestMapping(value = "/read/{oid}/anullsettlement", method = RequestMethod.POST)
    public String processReadToAnullSettlementNote(@PathVariable("oid") SettlementNote settlementNote,
            @RequestParam("anullReason") String anullReason, Model model, RedirectAttributes redirectAttributes) {
        final String loggedUsername = TreasuryPlataformDependentServicesFactory.implementation().getLoggedUsername();

        setSettlementNote(settlementNote, model);

        try {
            if (settlementNote.isReimbursement()) {
                throw new TreasuryDomainException("error.SettlementNote.reimbursement.must.be.rejected.in.erp");
            }

            assertUserIsAllowToAnnulSettlementNote(settlementNote, model);

            anullReason = anullReason + " - [" + loggedUsername + "] " + new DateTime().toString("YYYY-MM-dd HH:mm");

            settlementNote.anullDocument(anullReason, true);
            addInfoMessage(treasuryBundle("label.document.managepayments.SettlementNote.document.anulled.sucess"), model);
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }
        return redirect(READ_URL + getSettlementNote(model).getExternalId(), model, redirectAttributes);
    }

    protected void assertUserIsAllowToAnnulSettlementNote(final SettlementNote settlementNote, final Model model) {
        final String loggedUsername = TreasuryPlataformDependentServicesFactory.implementation().getLoggedUsername();

        if (TreasuryAccessControlAPI.isAllowToConditionallyAnnulSettlementNote(loggedUsername, settlementNote)) {
            return;
        }

        if (TreasuryAccessControlAPI.isAllowToAnnulSettlementNoteWithoutAnyRestriction(loggedUsername, settlementNote)) {
            return;
        }

        addErrorMessage(treasuryBundle("error.authorization.not.allow.to.annul.settlements"), model);
        throw new SecurityException(treasuryBundle("error.authorization.not.allow.to.annul.settlements"));
    }

    @RequestMapping(value = "/read/{oid}/exportintegrationfile", produces = "text/xml;charset=Windows-1252")
    public void processReadToExportIntegrationFile(@PathVariable("oid") SettlementNote settlementNote, Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) {
        try {
            assertUserIsFrontOfficeMember(settlementNote.getDebtAccount().getFinantialInstitution(), model);
            final String saftEncoding =
                    ERPExporterManager.saftEncoding(settlementNote.getDebtAccount().getFinantialInstitution());

            final String output = ERPExporterManager.exportFinantialDocumentToXML(settlementNote);

            response.setContentType("text/xml");
            response.setCharacterEncoding(saftEncoding);
            String filename = URLEncoder.encode(StringNormalizer.normalizePreservingCapitalizedLetters(
                    (settlementNote.getDebtAccount().getFinantialInstitution().getFiscalNumber() + "_"
                            + settlementNote.getUiDocumentNumber() + ".xml").replaceAll("/", "_").replaceAll("\\s", "_")
                                    .replaceAll(" ", "_")),
                    saftEncoding);
            response.setHeader("Content-disposition", "attachment; filename=" + filename);

            response.getOutputStream().write(output.getBytes(saftEncoding));
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            try {
                response.sendRedirect(redirect(READ_URL + settlementNote.getExternalId(), model, redirectAttributes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/read/{oid}/printdocument", produces = DocumentPrinter.PDF)
    public Object processReadToPrintDocument(@PathVariable("oid") SettlementNote settlementNote, Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) {
        try {
            assertUserIsFrontOfficeMember(settlementNote.getDebtAccount().getFinantialInstitution(), model);
            byte[] report = DocumentPrinter.printFinantialDocument(settlementNote, DocumentPrinter.PDF);
            return new ResponseEntity<byte[]>(report, HttpStatus.OK);
        } catch (ReportGenerationException rex) {
            addErrorMessage(rex.getLocalizedMessage(), model);
            addErrorMessage(rex.getCause().getLocalizedMessage(), model);
            return redirect(READ_URL + settlementNote.getExternalId(), model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            return redirect(READ_URL + settlementNote.getExternalId(), model, redirectAttributes);
        }
    }

    @RequestMapping(value = "/read/{oid}/closesettlementnote", method = RequestMethod.POST)
    public String processReadToCloseSettlementtNote(@PathVariable("oid") SettlementNote settlementNote, Model model,
            RedirectAttributes redirectAttributes) {
        setSettlementNote(settlementNote, model);

        try {
            assertUserIsFrontOfficeMember(settlementNote.getDebtAccount().getFinantialInstitution(), model);

            settlementNote.closeDocument();

            addInfoMessage(treasuryBundle("label.document.manageinvoice.Settlement.document.closed.sucess"), model);
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }

        return redirect(SettlementNoteController.READ_URL + getSettlementNote(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = TRANSACTIONS_SUMMARY_URI, method = RequestMethod.GET)
    public String processSearchToTransactionsSummary(Model model) {
        model.addAttribute("finantial_institutions_options", FinantialInstitution.findAll().collect(Collectors.toList()));
        return "treasury/document/managepayments/settlementnote/transactionsSummary";
    }

    @RequestMapping(value = TRANSACTIONS_SUMMARY_URI, method = RequestMethod.POST)
    public String processSearchToTransactionsSummary(
            @RequestParam(value = "finantialInstitution", required = true) FinantialInstitution finantialInstitution,
            @RequestParam(value = "documentdatefrom",
                    required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate documentDateFrom,
            @RequestParam(value = "documentdateto",
                    required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate documentDateTo,
            Model model) {
        if (Days.daysBetween(documentDateFrom, documentDateTo).getDays() > SEARCH_SETTLEMENT_ENTRY_LIMIT_DAYS_PERIOD) {
            addErrorMessage(treasuryBundle("error.SettlementNote.day.limit.exceeded",
                    String.valueOf(SEARCH_SETTLEMENT_ENTRY_LIMIT_DAYS_PERIOD)), model);
        } else {
            //TODO: THE FILTER TO INTERNAL SERIES SHOULD BE A GET PARAMETER
            List<SettlementNote> notes =
                    filterSearchSettlementNote(finantialInstitution, documentDateFrom, documentDateTo, false);
            model.addAttribute("settlementEntriesDataSet", getSettlementEntriesDataSet(notes));
            model.addAttribute("advancedPaymentEntriesDataSet", getAdvancedPaymentEntriesDataSet(notes));
            model.addAttribute("advancedPaymentTotal", getAdvancedPaymentTotal(notes));
            model.addAttribute("finantialInstitution", finantialInstitution);

            populateSummaryTransactions(model, notes, documentDateFrom, documentDateTo);
        }
        model.addAttribute("finantial_institutions_options", FinantialInstitution.findAll().collect(Collectors.toList()));
        return "treasury/document/managepayments/settlementnote/transactionsSummary";
    }

    private BigDecimal getAdvancedPaymentTotal(List<SettlementNote> notes) {
        BigDecimal val = BigDecimal.ZERO;
        for (SettlementNote note : notes) {
            if (note.getAdvancedPaymentCreditNote() != null) {
                val = val.add(note.getAdvancedPaymentCreditNote().getTotalAmount());
            }
        }
        return val;
    }

    private List<CreditNote> getAdvancedPaymentEntriesDataSet(List<SettlementNote> notes) {
        List<CreditNote> val = new ArrayList<CreditNote>();
        for (SettlementNote note : notes) {
            if (note.getAdvancedPaymentCreditNote() != null) {
                val.add(note.getAdvancedPaymentCreditNote());
            }
        }
        return val.stream().sorted((x, y) -> x.getDocumentDate().compareTo(y.getDocumentDate())).collect(Collectors.toList());
    }

    private void populateSummaryTransactions(Model model, List<SettlementNote> notes, LocalDate beginDate, LocalDate endDate) {
        Map<PaymentMethod, BigDecimal> payments =
                getPaymentEntriesDataSet(notes).stream().collect(Collectors.groupingBy(PaymentEntry::getPaymentMethod,
                        Collectors.reducing(BigDecimal.ZERO, PaymentEntry::getPayedAmount, BigDecimal::add)));
        Map<PaymentMethod, BigDecimal> reimbursements =
                getReimbursementEntriesDataSet(notes).stream().collect(Collectors.groupingBy(ReimbursementEntry::getPaymentMethod,
                        Collectors.reducing(BigDecimal.ZERO, ReimbursementEntry::getReimbursedAmount, BigDecimal::add)));

        Map<PaymentMethod, BigDecimal> paymentsOutOfTimeWindow = getPaymentEntriesDataSet(notes).stream()
                .filter(x -> x.getSettlementNote().getPaymentDate().toLocalDate().isBefore(beginDate))
                .collect(Collectors.groupingBy(PaymentEntry::getPaymentMethod,
                        Collectors.reducing(BigDecimal.ZERO, PaymentEntry::getPayedAmount, BigDecimal::add)));
        Map<PaymentMethod, BigDecimal> reimbursementsOutOfTimeWindow = getReimbursementEntriesDataSet(notes).stream()
                .filter(x -> x.getSettlementNote().getPaymentDate().toLocalDate().isBefore(beginDate))
                .collect(Collectors.groupingBy(ReimbursementEntry::getPaymentMethod,
                        Collectors.reducing(BigDecimal.ZERO, ReimbursementEntry::getReimbursedAmount, BigDecimal::add)));

        PaymentMethod.findAll().forEach(pm -> {
            if (payments.get(pm) == null) {
                payments.put(pm, BigDecimal.ZERO);
            }
            if (reimbursements.get(pm) == null) {
                reimbursements.put(pm, BigDecimal.ZERO);
            }
        });
        model.addAttribute("paymentsDataSet", payments);
        model.addAttribute("paymentsOutOfTimeWindowDataSet", paymentsOutOfTimeWindow);
        model.addAttribute("reimbursementsDataSet", reimbursements);
        model.addAttribute("reimbursementsOutOfTimeWindowDataSet", reimbursementsOutOfTimeWindow);
    }

    private List<SettlementNote> filterSearchSettlementNote(FinantialInstitution finantialInstitution, LocalDate documentDateFrom,
            LocalDate documentDateTo, boolean filterInternalSeriesOnly) {

        //Only filter de SettlementNotes from the "internal" series and from the final institution
        // And appy TimeWindow to the "DOCUMENT_DATE"
        Stream<SettlementNote> streamFilter = SettlementNote.findAll().filter(note -> note.isClosed())
                .filter(note -> note.getDebtAccount().getFinantialInstitution().equals(finantialInstitution))
                .filter(note -> note.getDocumentDate().toLocalDate().isAfter(documentDateFrom)
                        || note.getDocumentDate().toLocalDate().isEqual(documentDateFrom))
                .filter(note -> note.getDocumentDate().toLocalDate().isBefore(documentDateTo)
                        || note.getDocumentDate().toLocalDate().isEqual(documentDateTo));

        if (filterInternalSeriesOnly == true) {
            streamFilter = streamFilter.filter(note -> note.getDocumentNumberSeries().getSeries().getExternSeries() == false
                    && note.getDocumentNumberSeries().getSeries().getLegacy() == false);

        }

        return streamFilter.collect(Collectors.toList());
    }

    private List<SettlementEntry> getSettlementEntriesDataSet(List<SettlementNote> notes) {
        return notes.stream()
                //Filter only settlement Entries where there was payments / rehimbursments
                .filter(x -> TreasuryConstants.isPositive(x.getTotalPayedAmount())
                        || TreasuryConstants.isPositive(x.getTotalReimbursementAmount()))
                .map(note -> note.getSettlemetEntries().collect(Collectors.toList()))
                .reduce(new ArrayList<SettlementEntry>(), (t, u) -> {
                    t.addAll(u);
                    return t;
                });
    }

    private List<PaymentEntry> getPaymentEntriesDataSet(List<SettlementNote> notes) {
        return notes.stream().map(note -> note.getPaymentEntriesSet().stream().collect(Collectors.toList()))
                .reduce(new ArrayList<PaymentEntry>(), (t, u) -> {
                    t.addAll(u);
                    return t;
                });
    }

    private List<ReimbursementEntry> getReimbursementEntriesDataSet(List<SettlementNote> notes) {
        return notes.stream().map(note -> note.getReimbursementEntriesSet().stream().collect(Collectors.toList()))
                .reduce(new ArrayList<ReimbursementEntry>(), (t, u) -> {
                    t.addAll(u);
                    return t;
                });
    }

    @RequestMapping(value = "/read/{oid}/exportintegrationonline")
    public String processReadToExportIntegrationOnline(@PathVariable("oid") final SettlementNote settlementNote, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(settlementNote.getDebtAccount().getFinantialInstitution(), model);

            try {
                final IERPExporter erpExporter = settlementNote.getDebtAccount().getFinantialInstitution()
                        .getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPExporter();
                //Force a check status first of the document 
                erpExporter.checkIntegrationDocumentStatus(settlementNote);
            } catch (Exception ex) {
            }

            final ERPExportOperation output = ERPExporterManager.exportSettlementNote(settlementNote);
            if (output == null) {
                addInfoMessage(treasuryBundle("label.integration.erp.document.not.exported"), model);
                return read(settlementNote, model);
            }

            addInfoMessage(treasuryBundle("label.integration.erp.exportoperation.success"), model);
            return redirect(ERPExportOperationController.READ_URL + output.getExternalId(), model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(treasuryBundle("label.integration.erp.exportoperation.error") + ex.getLocalizedMessage(), model);
        }

        return read(settlementNote, model);
    }

    @RequestMapping(value = "/read/{oid}/cleardocumenttoexport", method = RequestMethod.POST)
    public String cleardocumenttoexport(@PathVariable("oid") final SettlementNote settlementNote,
            @RequestParam(value = "reason", required = false) final String reason, final Model model,
            final RedirectAttributes redirectAttributes) {
        try {

            if (!settlementNote.isDocumentToExport()) {
                addErrorMessage(treasuryBundle("error.FinantialDocument.document.not.marked.to.export"), model);
                return redirect(READ_URL + settlementNote.getExternalId(), model, redirectAttributes);
            }

            if (Strings.isNullOrEmpty(reason)) {
                addErrorMessage(treasuryBundle("error.FinantialDocument.clear.document.to.export.requires.reason"), model);
                return redirect(READ_URL + settlementNote.getExternalId(), model, redirectAttributes);
            }

            assertUserIsBackOfficeMember(model);

            settlementNote.clearDocumentToExport(reason);

            return redirect(READ_URL + settlementNote.getExternalId(), model, redirectAttributes);
        } catch (final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
            return redirect(READ_URL + settlementNote.getExternalId(), model, redirectAttributes);
        }
    }

    private static final String _DOWNLOAD_CERTIFIED_DOCUMENT_PRINT_URI = "/downloadcertifieddocumentprint";
    public static final String DOWNLOAD_CERTIFIED_DOCUMENT_PRINT_URL = CONTROLLER_URL + _DOWNLOAD_CERTIFIED_DOCUMENT_PRINT_URI;

    @RequestMapping(value = _DOWNLOAD_CERTIFIED_DOCUMENT_PRINT_URI + "/{oid}", method = RequestMethod.GET)
    public String downloadcertifieddocumentprint(@PathVariable("oid") final SettlementNote settlementNote, final Model model,
            final RedirectAttributes redirectAttributes, final HttpServletResponse response) {

        try {

            final byte[] contents = ERPExporterManager.downloadCertifiedDocumentPrint(settlementNote);

            response.setContentType("application/pdf");
            String filename = URLEncoder.encode(StringNormalizer.normalizePreservingCapitalizedLetters(
                    (settlementNote.getDebtAccount().getFinantialInstitution().getFiscalNumber() + "_"
                            + settlementNote.getUiDocumentNumber() + ".pdf").replaceAll("/", "_").replaceAll("\\s", "_")
                                    .replaceAll(" ", "_")),
                    "Windows-1252");

            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            response.getOutputStream().write(contents);

            return null;
        } catch (final Exception e) {
            e.printStackTrace();
            addErrorMessage(e.getLocalizedMessage(), model);
            return read(settlementNote, model);
        }
    }

    private static final String _UPDATE_REIMBURSEMENT_STATE_URI = "/updatereimbursementstate";
    public static final String UPDATE_REIMBURSEMENT_STATE_URL = CONTROLLER_URL + _UPDATE_REIMBURSEMENT_STATE_URI;

    @RequestMapping(value = _UPDATE_REIMBURSEMENT_STATE_URI + "/{oid}", method = RequestMethod.POST)
    public String updatereimbursementstate(@PathVariable("oid") final SettlementNote settlementNote, final Model model,
            final RedirectAttributes redirectAttributes) {
        try {
            ERPExporterManager.updateReimbursementState(settlementNote);
            return redirect(READ_URL + settlementNote.getExternalId(), model, redirectAttributes);
        } catch (final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
            return read(settlementNote, model);
        }

    }

}
