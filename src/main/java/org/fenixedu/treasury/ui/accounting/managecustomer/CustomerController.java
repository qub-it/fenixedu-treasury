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
package org.fenixedu.treasury.ui.accounting.managecustomer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.CustomerType;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

//@Component("org.fenixedu.treasury.ui.accounting.manageCustomer") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.accounting.manageCustomer",
        accessGroup = "treasuryFrontOffice")
@RequestMapping(CustomerController.CONTROLLER_URI)
public class CustomerController extends TreasuryBaseController {
    public static final String CONTROLLER_URI = "/treasury/accounting/managecustomer/customer";
    private static final String SEARCH_URI = "/";
    public static final String SEARCH_FULL_URI = CONTROLLER_URI + SEARCH_URI;

    private static final String READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URI + READ_URI;

    public static final long SEARCH_CUSTOMER_LIST_LIMIT_SIZE = 500;

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URI + SEARCH_URI;
    }

    private Customer getCustomer(Model model) {
        return (Customer) model.asMap().get("customer");
    }

    private void setCustomer(Customer customer, Model model) {
        model.addAttribute("customer", customer);
    }

    @Atomic
    public void deleteCustomer(Customer customer) {
    }

    @RequestMapping(value = SEARCH_URI)
    public String search(@RequestParam(value = "finantialInstitution", required = false) FinantialInstitution institution,
            @RequestParam(value = "customertype", required = false) CustomerType customerType, @RequestParam(value = "customer",
                    required = false) String customer, Model model) {
        List<Customer> searchcustomerResultsDataSet = filterSearchCustomer(institution, customerType, customer);
        model.addAttribute("limit_exceeded", searchcustomerResultsDataSet.size() > SEARCH_CUSTOMER_LIST_LIMIT_SIZE);
        model.addAttribute("searchcustomerResultsDataSet_totalCount", searchcustomerResultsDataSet.size());
        searchcustomerResultsDataSet =
                searchcustomerResultsDataSet.stream().limit(SEARCH_CUSTOMER_LIST_LIMIT_SIZE).collect(Collectors.toList());

        model.addAttribute("Customer_customerType_options", CustomerType.findAll().collect(Collectors.toList()));
        model.addAttribute("finantialinstitution_options", FinantialInstitution.findAll().collect(Collectors.toList()));

        model.addAttribute("searchcustomerResultsDataSet", searchcustomerResultsDataSet);

        return "treasury/accounting/managecustomer/customer/search";
    }

    private Stream<? extends Customer> getSearchUniverseSearchCustomerDataSet() {
        return Customer.findAll().sorted((x, y) -> x.getName().compareToIgnoreCase(y.getName()));
    }

    private List<Customer> filterSearchCustomer(FinantialInstitution institution, CustomerType customerType, String customerString) {
        return getSearchUniverseSearchCustomerDataSet()
                .filter(customer -> customerType == null || customerType == customer.getCustomerType())
                .filter(customer -> customerString == null || customer.matchesMultiFilter(customerString))
                .filter(customer -> institution == null
                        || customer.getDebtAccountsSet().stream()
                                .anyMatch(debtAccount -> debtAccount.getFinantialInstitution().equals(institution)))
                .collect(Collectors.<Customer> toList());
    }

    public static final String SEARCH_TO_VIEW_ACTION_URI = "/search/view/";

    @RequestMapping(value = SEARCH_TO_VIEW_ACTION_URI + "{oid}")
    public String processSearchToViewAction(@PathVariable("oid") Customer customer, Model model,
            RedirectAttributes redirectAttributes) {
        return redirect(READ_URL + customer.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") final Customer customer, final Model model, final RedirectAttributes redirectAttributes) {
        if(!customer.isActive() && customer.isUiOtherRelatedCustomerActive()) {
            return redirect(customer.uiRedirectToActiveCustomer(READ_URL), model, redirectAttributes);
        }
        
        setCustomer(customer, model);

//        //If the customer has only one debtAccount, redirect to the Read Of DebtAccount
//        if (FinantialInstitution.findAll().count() <= 1) {
//            DebtAccount debtAccount = customer.getDebtAccountsSet().iterator().next();
//            return redirect(DebtAccountController.READ_URL + debtAccount.getExternalId(), model, redirectAttributes);
//        }

        List<InvoiceEntry> pendingInvoiceEntries = new ArrayList<InvoiceEntry>();
        for (DebtAccount debtAccount : customer.getDebtAccountsSet()) {
            pendingInvoiceEntries.addAll(debtAccount.getPendingInvoiceEntriesSet());
        }
        model.addAttribute(
                "pendingDocumentsDataSet",
                pendingInvoiceEntries
                        .stream()
                        .sorted(InvoiceEntry.COMPARE_BY_ENTRY_DATE.reversed().thenComparing(
                                InvoiceEntry.COMPARE_BY_DUE_DATE.reversed())).collect(Collectors.toList()));

        return "treasury/accounting/managecustomer/customer/read";
    }

}
