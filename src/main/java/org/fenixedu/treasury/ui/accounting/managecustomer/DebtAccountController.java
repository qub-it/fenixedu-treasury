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

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.TupleDataSourceBean;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.exemption.TreasuryExemption;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.services.integration.erp.ERPExporter;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.administration.managefinantialinstitution.FinantialInstitutionController;
import org.fenixedu.treasury.ui.document.manageinvoice.CreditNoteController;
import org.fenixedu.treasury.ui.document.manageinvoice.DebitEntryController;
import org.fenixedu.treasury.ui.document.manageinvoice.DebitNoteController;
import org.fenixedu.treasury.ui.document.managepayments.SettlementNoteController;
import org.fenixedu.treasury.ui.integration.erp.ERPExportOperationController;
import org.fenixedu.treasury.util.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String read(@PathVariable("oid") DebtAccount debtAccount, Model model, RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(debtAccount.getFinantialInstitution(), model);
            setDebtAccount(debtAccount, model);

            List<InvoiceEntry> allInvoiceEntries = new ArrayList<InvoiceEntry>();
            List<SettlementNote> paymentEntries = new ArrayList<SettlementNote>();
            List<TreasuryExemption> exemptionEntries = new ArrayList<TreasuryExemption>();
            List<InvoiceEntry> pendingInvoiceEntries = new ArrayList<InvoiceEntry>();
            allInvoiceEntries.addAll(debtAccount.getActiveInvoiceEntries().collect(Collectors.toList()));
            paymentEntries = SettlementNote.findByDebtAccount(debtAccount).filter(x -> x.isClosed() || x.isPreparing())
//                            .filter(x -> !x.getPaymentEntriesSet().isEmpty() || !x.getReimbursementEntriesSet().isEmpty())
                    .collect(Collectors.toList());

            exemptionEntries.addAll(TreasuryExemption.findByDebtAccount(debtAccount).collect(Collectors.toList()));

            pendingInvoiceEntries.addAll(debtAccount.getPendingInvoiceEntriesSet());

            model.addAttribute(
                    "pendingDocumentsDataSet",
                    pendingInvoiceEntries
                            .stream()
                            .sorted(InvoiceEntry.COMPARE_BY_ENTRY_DATE.reversed().thenComparing(
                                    InvoiceEntry.COMPARE_BY_DUE_DATE.reversed())).collect(Collectors.toList()));
            model.addAttribute(
                    "allDocumentsDataSet",
                    allInvoiceEntries
                            .stream()
                            .sorted(InvoiceEntry.COMPARE_BY_ENTRY_DATE.reversed().thenComparing(
                                    InvoiceEntry.COMPARE_BY_DUE_DATE.reversed())).collect(Collectors.toList()));
            model.addAttribute(
                    "paymentsDataSet",
                    paymentEntries.stream().sorted((x, y) -> y.getDocumentDate().compareTo(x.getDocumentDate()))
                            .collect(Collectors.toList()));
            model.addAttribute("exemptionDataSet", exemptionEntries);

            return "treasury/accounting/managecustomer/debtaccount/read";
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }
        return redirect(FinantialInstitutionController.SEARCH_URL, model, redirectAttributes);
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
        return redirect(
                SettlementNoteController.CHOOSE_INVOICE_ENTRIES_URL + getDebtAccount(model).getExternalId() + "/" + false, model,
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
    public @ResponseBody ResponseEntity<List<TupleDataSourceBean>> processReadToReadEvent(@RequestParam(value = "q",
            required = true) String searchField, Model model, RedirectAttributes redirectAttributes) {
        final String searchFieldDecoded = URLDecoder.decode(searchField);

        List<TupleDataSourceBean> bean = new ArrayList<TupleDataSourceBean>();
        List<DebtAccount> debtAccounts =
                DebtAccount.findAll().filter(x -> x.getCustomer().matchesMultiFilter(searchFieldDecoded))
                        .sorted((x, y) -> x.getCustomer().getName().compareToIgnoreCase(y.getCustomer().getName()))
                        .collect(Collectors.toList());

        for (DebtAccount debt : debtAccounts) {
            bean.add(new TupleDataSourceBean(debt.getExternalId(), debt.getCustomer().getName() + " ["
                    + debt.getFinantialInstitution().getCode() + "] (#" + debt.getCustomer().getBusinessIdentification() + ") ("
                    + debt.getCustomer().getIdentificationNumber() + ")"));
        }
        return new ResponseEntity<List<TupleDataSourceBean>>(bean, HttpStatus.OK);
    }

    private static final String _SEARCHOPENDEBTACCOUNTS_URI = "/searchopendebtaccounts";
    public static final String SEARCHOPENDEBTACCOUNTS_URL = CONTROLLER_URL + _SEARCHOPENDEBTACCOUNTS_URI;

    @RequestMapping(value = _SEARCHOPENDEBTACCOUNTS_URI)
    public String searchOpenDebtAccounts(Model model) {
        List<DebtAccount> searchopendebtaccountsResultsDataSet = filterSearchOpenDebtAccounts();

        //add the results dataSet to the model
        model.addAttribute("searchopendebtaccountsResultsDataSet", searchopendebtaccountsResultsDataSet);
        return "treasury/accounting/managecustomer/debtaccount/searchopendebtaccounts";
    }

    private Stream<DebtAccount> getSearchUniverseSearchOpenDebtAccountsDataSet() {
        return DebtAccount.findAll().filter(x -> x.getTotalInDebt().compareTo(BigDecimal.ZERO) != 0);
    }

    private List<DebtAccount> filterSearchOpenDebtAccounts() {

        return getSearchUniverseSearchOpenDebtAccountsDataSet().collect(Collectors.toList());
    }

    private static final String _SEARCHOPENDEBTACCOUNTS_TO_VIEW_ACTION_URI = "/searchopendebtaccounts/view/";
    public static final String SEARCHOPENDEBTACCOUNTS_TO_VIEW_ACTION_URL = CONTROLLER_URL
            + _SEARCHOPENDEBTACCOUNTS_TO_VIEW_ACTION_URI;

    @RequestMapping(value = _SEARCHOPENDEBTACCOUNTS_TO_VIEW_ACTION_URI + "{oid}")
    public String processSearchOpenDebtAccountsToViewAction(@PathVariable("oid") DebtAccount debtAccount, Model model,
            RedirectAttributes redirectAttributes) {

        return redirect(READ_URL + debtAccount.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = "/read/{oid}/exportintegrationonline")
    public String processReadToExportIntegrationOnline(@PathVariable("oid") DebtAccount debtAccount, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(debtAccount.getFinantialInstitution(), model);
            List<FinantialDocument> pendingDocuments = new ArrayList(debtAccount.getFinantialDocumentsSet());
            if (pendingDocuments.size() > 0) {
                ERPExportOperation output =
                        ERPExporter.exportFinantialDocumentToIntegration(debtAccount.getFinantialInstitution(), pendingDocuments);
                addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.integration.erp.exportoperation.success"), model);
                return redirect(ERPExportOperationController.READ_URL + output.getExternalId(), model, redirectAttributes);
            }
            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.integration.erp.exportoperation.no.documents"), model);
        } catch (Exception ex) {
            addErrorMessage(
                    BundleUtil.getString(Constants.BUNDLE, "label.integration.erp.exportoperation.error")
                            + ex.getLocalizedMessage(), model);
        }
        return read(debtAccount, model, redirectAttributes);
    }

}
