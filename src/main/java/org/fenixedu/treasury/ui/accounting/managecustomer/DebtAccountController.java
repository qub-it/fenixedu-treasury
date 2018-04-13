/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: ricardo.pedro@qub-it.com
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
package org.fenixedu.treasury.ui.accounting.managecustomer;

import static org.fenixedu.treasury.util.Constants.treasuryBundle;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.treasury.dto.TreasuryTupleDataSourceBean;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.ERPCustomerFieldsBean;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.exemption.TreasuryExemption;
import org.fenixedu.treasury.domain.paymentcodes.FinantialDocumentPaymentCode;
import org.fenixedu.treasury.domain.paymentcodes.MultipleEntriesPaymentCode;
import org.fenixedu.treasury.domain.paymentcodes.PaymentCodeTarget;
import org.fenixedu.treasury.domain.tariff.GlobalInterestRate;
import org.fenixedu.treasury.services.reports.DocumentPrinter;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.document.forwardpayments.ForwardPaymentController;
import org.fenixedu.treasury.ui.document.manageinvoice.CreditNoteController;
import org.fenixedu.treasury.ui.document.manageinvoice.DebitEntryController;
import org.fenixedu.treasury.ui.document.manageinvoice.DebitNoteController;
import org.fenixedu.treasury.ui.document.managepayments.SettlementNoteController;
import org.fenixedu.treasury.util.Constants;
import org.fenixedu.treasury.util.FiscalCodeValidation;
import org.joda.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qubit.terra.docs.util.ReportGenerationException;

import pt.ist.fenixframework.Atomic;

//@Component("org.fenixedu.treasury.ui.accounting.manageCustomer") <-- Use for duplicate controller name disambiguation
@BennuSpringController(value = CustomerController.class)
@RequestMapping(DebtAccountController.CONTROLLER_URL)
public class DebtAccountController extends TreasuryBaseController {
    public static final String CONTROLLER_URL = "/treasury/accounting/managecustomer/debtaccount";
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

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CustomerController.SEARCH_FULL_URI;
    }

    private DebtAccount getDebtAccount(Model model) {
        return (DebtAccount) model.asMap().get("debtAccount");
    }

    private void setDebtAccount(DebtAccount debtAccount, Model model) {
        model.addAttribute("debtAccount", debtAccount);
    }

    @Atomic
    public void deleteDebtAccount(DebtAccount debtAccount) {
        // debtAccount.delete();
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") final DebtAccount debtAccount, final Model model,
            final RedirectAttributes redirectAttributes) {

        boolean filterAnnuledValue = false;

        assertUserIsFrontOfficeMember(debtAccount.getFinantialInstitution(), model);
        setDebtAccount(debtAccount, model);
        checkFinantialInstitutionData(model);
        List<InvoiceEntry> allInvoiceEntries = new ArrayList<InvoiceEntry>();
        List<SettlementNote> paymentEntries = new ArrayList<SettlementNote>();
        List<TreasuryExemption> exemptionEntries = new ArrayList<TreasuryExemption>();
        List<InvoiceEntry> pendingInvoiceEntries = new ArrayList<InvoiceEntry>();
        allInvoiceEntries.addAll(debtAccount.getInvoiceEntrySet().stream().collect(Collectors.toList()));

        paymentEntries = SettlementNote.findByDebtAccount(debtAccount).collect(Collectors.toList());

        exemptionEntries.addAll(TreasuryExemption.findByDebtAccount(debtAccount).collect(Collectors.toList()));

        pendingInvoiceEntries.addAll(debtAccount.getPendingInvoiceEntriesSet());

        model.addAttribute("pendingDocumentsDataSet",
                pendingInvoiceEntries.stream().sorted(
                        InvoiceEntry.COMPARE_BY_ENTRY_DATE.reversed().thenComparing(InvoiceEntry.COMPARE_BY_DUE_DATE.reversed()))
                        .collect(Collectors.toList()));
        model.addAttribute("allDocumentsDataSet",
                allInvoiceEntries.stream().sorted(
                        InvoiceEntry.COMPARE_BY_ENTRY_DATE.reversed().thenComparing(InvoiceEntry.COMPARE_BY_DUE_DATE.reversed()))
                        .collect(Collectors.toList()));
        model.addAttribute("paymentsDataSet", paymentEntries.stream()
                .sorted((x, y) -> y.getDocumentDate().compareTo(x.getDocumentDate())).collect(Collectors.toList()));
        model.addAttribute("exemptionDataSet", exemptionEntries);

        final Set<PaymentCodeTarget> usedPaymentCodeTargets = Sets.newHashSet();
        for (final InvoiceEntry invoiceEntry : pendingInvoiceEntries) {
            if (!invoiceEntry.isDebitNoteEntry()) {
                continue;
            }

            usedPaymentCodeTargets.addAll(
                    MultipleEntriesPaymentCode.findUsedByDebitEntry((DebitEntry) invoiceEntry).collect(Collectors.toSet()));

            if (invoiceEntry.getFinantialDocument() != null) {
                usedPaymentCodeTargets
                        .addAll(FinantialDocumentPaymentCode.findUsedByFinantialDocument(invoiceEntry.getFinantialDocument())
                                .collect(Collectors.<PaymentCodeTarget> toSet()));
            }
        }

        checkIncompleteAddress(debtAccount, model);

        model.addAttribute("usedPaymentCodeTargets", usedPaymentCodeTargets);

        model.addAttribute("invalidFiscalCode", isInvalidFiscalCode(debtAccount));

        return "treasury/accounting/managecustomer/debtaccount/read";
    }

    private void checkIncompleteAddress(final DebtAccount debtAccount, final Model model) {
        final List<String> errorMessages = Lists.newArrayList();
        final boolean validAddress =
                ERPCustomerFieldsBean.checkIncompleteAddressForDebtAccountAndPayors(debtAccount, errorMessages);

        model.addAttribute("validAddress", validAddress);
        model.addAttribute("addressErrorMessages", errorMessages);
    }

    private boolean isInvalidFiscalCode(final DebtAccount debtAccount) {
        Customer customer = debtAccount.getCustomer();
        return !customer.isFiscalCodeValid();
    }

    @RequestMapping(value = "/read/{oid}/createreimbursement")
    public String processReadToCreateReimbursement(@PathVariable("oid") DebtAccount debtAccount, Model model,
            RedirectAttributes redirectAttributes) {
        setDebtAccount(debtAccount, model);
        return redirect(SettlementNoteController.CHOOSE_INVOICE_ENTRIES_URL + getDebtAccount(model).getExternalId() + "/" + true,
                model, redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/createpayment")
    public String processReadToCreatePayment(@PathVariable("oid") DebtAccount debtAccount, Model model,
            RedirectAttributes redirectAttributes) {
        setDebtAccount(debtAccount, model);
        return redirect(SettlementNoteController.CHOOSE_INVOICE_ENTRIES_URL + getDebtAccount(model).getExternalId() + "/" + false,
                model, redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/forwardpayment")
    public String processReadToForwardPayment(@PathVariable("oid") DebtAccount debtAccount, final Model model,
            final RedirectAttributes redirectAttributes) {
        setDebtAccount(debtAccount, model);
        return redirect(ForwardPaymentController.CHOOSE_INVOICE_ENTRIES_URL + getDebtAccount(model).getExternalId(), model,
                redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/createdebtentry")
    public String processReadToCreateDebtEntry(@PathVariable("oid") DebtAccount debtAccount, Model model,
            RedirectAttributes redirectAttributes) {
        setDebtAccount(debtAccount, model);
        return redirect(DebitEntryController.CREATE_URL + getDebtAccount(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/createdebitnote")
    public String processReadToCreateDebitNote(@PathVariable("oid") DebtAccount debtAccount, Model model,
            RedirectAttributes redirectAttributes) {
        setDebtAccount(debtAccount, model);
        return redirect(DebitNoteController.CREATE_URL + "?debtaccount=" + getDebtAccount(model).getExternalId(), model,
                redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/createcreditnote")
    public String processReadToCreateCreditNote(@PathVariable("oid") DebtAccount debtAccount, Model model,
            RedirectAttributes redirectAttributes) {
        setDebtAccount(debtAccount, model);
        return redirect(CreditNoteController.CREATE_URL + "?debtaccount=" + getDebtAccount(model).getExternalId(), model,
                redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/readevent")
    public String processReadToReadEvent(@PathVariable("oid") DebtAccount debtAccount, Model model,
            RedirectAttributes redirectAttributes) {
        setDebtAccount(debtAccount, model);
        return redirect(TreasuryEventController.SEARCH_URL + "?debtaccount=" + getDebtAccount(model).getExternalId(), model,
                redirectAttributes);
    }

    @RequestMapping(value = "/autocompletehelper", produces = "application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<List<TreasuryTupleDataSourceBean>> processReadToReadEvent(
            @RequestParam(value = "q", required = true) String searchField, Model model, RedirectAttributes redirectAttributes) {
        final String searchFieldDecoded = URLDecoder.decode(searchField).trim();

        List<TreasuryTupleDataSourceBean> bean = new ArrayList<TreasuryTupleDataSourceBean>();
        List<DebtAccount> debtAccounts = DebtAccount.findAll().filter(x -> x.getCustomer().matchesMultiFilter(searchFieldDecoded))
                .sorted((x, y) -> x.getCustomer().getName().compareToIgnoreCase(y.getCustomer().getName()))
                .collect(Collectors.toList());

        for (DebtAccount debt : debtAccounts) {
            bean.add(new TreasuryTupleDataSourceBean(debt.getExternalId(),
                    debt.getCustomer().getName() + " [" + debt.getFinantialInstitution().getCode() + "] (#"
                            + debt.getCustomer().getBusinessIdentification() + ") ("
                            + debt.getCustomer().getIdentificationNumber() + ")"));
        }
        return new ResponseEntity<List<TreasuryTupleDataSourceBean>>(bean, HttpStatus.OK);
    }

    private static final String _SEARCHOPENDEBTACCOUNTS_URI = "/searchopendebtaccounts";
    public static final String SEARCHOPENDEBTACCOUNTS_URL = CONTROLLER_URL + _SEARCHOPENDEBTACCOUNTS_URI;

    @RequestMapping(value = _SEARCHOPENDEBTACCOUNTS_URI)
    public String searchOpenDebtAccounts(Model model) {
        List<DebtAccount> searchopendebtaccountsResultsDataSet = filterSearchOpenDebtAccounts();
        checkFinantialInstitutionData(model);
        //add the results dataSet to the model
        model.addAttribute("searchopendebtaccountsResultsDataSet", searchopendebtaccountsResultsDataSet);
        return "treasury/accounting/managecustomer/debtaccount/searchopendebtaccounts";
    }

    private Stream<DebtAccount> getSearchUniverseSearchOpenDebtAccountsDataSet() {
        return DebtAccount.findAll().filter(x -> x.getPendingInvoiceEntriesSet().isEmpty() == false);
    }

    private List<DebtAccount> filterSearchOpenDebtAccounts() {

        return getSearchUniverseSearchOpenDebtAccountsDataSet().collect(Collectors.toList());
    }

    private static final String _SEARCHOPENDEBTACCOUNTS_TO_VIEW_ACTION_URI = "/searchopendebtaccounts/view/";
    public static final String SEARCHOPENDEBTACCOUNTS_TO_VIEW_ACTION_URL =
            CONTROLLER_URL + _SEARCHOPENDEBTACCOUNTS_TO_VIEW_ACTION_URI;

    @RequestMapping(value = _SEARCHOPENDEBTACCOUNTS_TO_VIEW_ACTION_URI + "{oid}")
    public String processSearchOpenDebtAccountsToViewAction(@PathVariable("oid") DebtAccount debtAccount, Model model,
            RedirectAttributes redirectAttributes) {

        return redirect(READ_URL + debtAccount.getExternalId(), model, redirectAttributes);
    }

    private void checkFinantialInstitutionData(Model model) {
        //Make some check info for ALERTING USER
        LocalDate now = new LocalDate();

        if (GlobalInterestRate.findByYear(now.getYear()).count() == 0) {
            addWarningMessage(
                    treasuryBundle("warning.GlobalInterestRate.no.interest.rate.for.current.year"),
                    model);
        }

        if (now.getMonthOfYear() == 12 && now.getDayOfMonth() >= 15) {
            if (GlobalInterestRate.findByYear(now.getYear() + 1).count() == 0) {
                addWarningMessage(
                        treasuryBundle("warning.GlobalInterestRate.no.interest.rate.for.next.year"),
                        model);

            }
        }
    }

    @RequestMapping(value = "/read/{oid}/exportintegrationonline")
    public String processReadToExportIntegrationOnline(@PathVariable("oid") final DebtAccount debtAccount, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(debtAccount.getFinantialInstitution(), model);
            throw new TreasuryDomainException("Desactivado");

//            
//            ERPExporterManager.exportPendingDocumentsForDebtAccount(debtAccount);
//
//            addInfoMessage(Constants.treasuryBundle("label.integration.erp.exportoperation.success"), model);
//            return redirect(READ_URL + debtAccount.getExternalId(), model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(treasuryBundle("label.integration.erp.exportoperation.error")
                    + ex.getLocalizedMessage(), model);
        }
        return read(debtAccount, model, redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/printpaymentplan", produces = DocumentPrinter.PDF)
    public Object processReadToPrintDocument(@PathVariable("oid") DebtAccount debtAccount, Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) {
        try {
            assertUserIsFrontOfficeMember(debtAccount.getFinantialInstitution(), model);
            byte[] report = DocumentPrinter.printDebtAccountPaymentPlan(debtAccount, DocumentPrinter.PDF);
            return new ResponseEntity<byte[]>(report, HttpStatus.OK);
        } catch (ReportGenerationException rex) {
            addErrorMessage(rex.getLocalizedMessage(), model);
            addErrorMessage(rex.getCause().getLocalizedMessage(), model);
            return redirect(READ_URL + debtAccount.getExternalId(), model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            return redirect(READ_URL + debtAccount.getExternalId(), model, redirectAttributes);
        }
    }

    private static final String _TRANSFERBALANCE_URI = "/transferbalance";
    public static final String TRANSFERBALANCE_URL = CONTROLLER_URL + _TRANSFERBALANCE_URI;

    @RequestMapping(value = _TRANSFERBALANCE_URI + "/{oid}")
    public String transferbalance(@PathVariable("oid") final DebtAccount debtAccount, final Model model,
            final RedirectAttributes redirectAttributes) {
        try {
            if (debtAccount.getCustomer().isActive()) {
                throw new TreasuryDomainException("error.DebtAccount.transfer.from.must.not.be.active");
            }

            final FinantialInstitution finantialInstitution = debtAccount.getFinantialInstitution();

            Optional<DebtAccount> activeDebtAccount =
                    DebtAccount.findUnique(finantialInstitution, debtAccount.getCustomer().getActiveCustomer());

            if (!activeDebtAccount.isPresent()) {
                throw new TreasuryDomainException("error.DebtAccount.active.debt.account.not.found");
            }

            debtAccount.transferBalance(activeDebtAccount.get());
            return redirect(READ_URL + debtAccount.getExternalId(), model, redirectAttributes);
        } catch (final Exception ex) {
            ex.printStackTrace();
            addErrorMessage(ex.getLocalizedMessage(), model);
            return read(debtAccount, model, redirectAttributes);
        }
    }

}
