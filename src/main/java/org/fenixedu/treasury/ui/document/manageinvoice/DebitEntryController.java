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
package org.fenixedu.treasury.ui.document.manageinvoice;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.Vat;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.event.TreasuryEvent;
import org.fenixedu.treasury.domain.tariff.FixedTariff;
import org.fenixedu.treasury.domain.tariff.InterestRate;
import org.fenixedu.treasury.domain.tariff.Tariff;
import org.fenixedu.treasury.dto.DebitEntryBean;
import org.fenixedu.treasury.dto.FixedTariffInterestRateBean;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController;
import org.fenixedu.treasury.ui.administration.managefinantialinstitution.FinantialInstitutionController;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

//@Component("org.fenixedu.treasury.ui.document.manageInvoice") <-- Use for duplicate controller name disambiguation
//@SpringFunctionality(app = TreasuryController.class, title = "label.title.document.manageInvoice",accessGroup = "#managers")// CHANGE_ME accessGroup = "group1 | group2 | groupXPTO"
//or
@BennuSpringController(value = DebitNoteController.class)
@RequestMapping(DebitEntryController.CONTROLLER_URL)
public class DebitEntryController extends TreasuryBaseController {
    public static final String CONTROLLER_URL = "/treasury/document/manageinvoice/debitentry";
    private static final String SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + SEARCH_URI;
    private static final String UPDATE_URI = "/update/";
    public static final String UPDATE_URL = CONTROLLER_URL + UPDATE_URI;
    private static final String CREATE_URI = "/create/";
    public static final String CREATE_URL = CONTROLLER_URL + CREATE_URI;
    private static final String READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + READ_URI;

//

    @RequestMapping
    public String home(Model model) {
        return "forward:" + DebitNoteController.SEARCH_URL;
    }

    private DebitEntryBean getDebitEntryBean(Model model) {
        return (DebitEntryBean) model.asMap().get("debitEntryBean");
    }

    private void setDebitEntryBean(DebitEntryBean bean, Model model) {
        model.addAttribute("debitEntryBeanJson", getBeanJson(bean));
        model.addAttribute("debitEntryBean", bean);
    }

    private DebitEntry getDebitEntry(Model model) {
        return (DebitEntry) model.asMap().get("debitEntry");
    }

    private void setDebitEntry(DebitEntry debitEntry, Model model) {
        model.addAttribute("debitEntry", debitEntry);
    }

    @Atomic
    public void deleteDebitEntry(DebitEntry debitEntry) {
        debitEntry.delete();
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") DebitEntry debitEntry, Model model, RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(debitEntry.getDebtAccount().getFinantialInstitution(), model);
            setDebitEntry(debitEntry, model);
            return "treasury/document/manageinvoice/debitentry/read";
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }
        return redirect(FinantialInstitutionController.SEARCH_URL, model, redirectAttributes);
    }

    //
    private static final String _DELETE_URI = "/delete/";
    public static final String DELETE_URL = CONTROLLER_URL + _DELETE_URI;

    @RequestMapping(value = _DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") DebitEntry debitEntry, Model model, RedirectAttributes redirectAttributes) {

        setDebitEntry(debitEntry, model);
        DebitNote note = (DebitNote) debitEntry.getFinantialDocument();
        DebtAccount account = debitEntry.getDebtAccount();
        try {
            assertUserIsAllowToModifyInvoices(debitEntry.getDebtAccount().getFinantialInstitution(), model);
            //call the Atomic delete function
            deleteDebitEntry(debitEntry);

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.delete"), model);
            if (note != null) {
                return redirect(DebitNoteController.READ_URL + note.getExternalId(), model, redirectAttributes);
            } else {
                return redirect(DebtAccountController.READ_URL + account.getExternalId(), model, redirectAttributes);
            }
        } catch (DomainException ex) {
            //Add error messages to the list
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + ex.getLocalizedMessage(), model);
        }

        if (note != null) {
            return redirect(DebitNoteController.READ_URL + note.getExternalId(), model, redirectAttributes);
        } else {
            return redirect(DebtAccountController.READ_URL + account.getExternalId(), model, redirectAttributes);
        }
    }

//				
    @RequestMapping(value = CREATE_URI + "{oid}", method = RequestMethod.GET)
    public String create(@PathVariable("oid") DebtAccount debtAccount,
            @RequestParam(value = "debitNote", required = false) DebitNote debitNote, Model model,
            RedirectAttributes redirectAttributes) {

        try {
            assertUserIsAllowToModifyInvoices(debtAccount.getFinantialInstitution(), model);
            if (debitNote != null && !debitNote.isPreparing()) {
                addWarningMessage(BundleUtil.getString(Constants.BUNDLE,
                        "label.error.document.manageinvoice.debitentry.invalid.state.add.debitentry"), model);
                redirect(DebitNoteController.READ_URL + debitNote.getExternalId(), model, redirectAttributes);
            }

            DebitEntryBean bean = new DebitEntryBean();

            bean.setProductDataSource(Product.findAllActive().collect(Collectors.toList()));
            bean.setDebtAccount(debtAccount);
            bean.setFinantialDocument(debitNote);
            bean.setCurrency(debtAccount.getFinantialInstitution().getCurrency());
            if (debitNote != null) {
                bean.setDueDate(debitNote.getDocumentDueDate());
                bean.setEntryDate(debitNote.getDocumentDate());
            } else {
                bean.setDueDate(new LocalDate());
                bean.setEntryDate(new DateTime());
            }
            this.setDebitEntryBean(bean, model);

            model.addAttribute("DebitEntry_event_options",
                    TreasuryEvent.find(debtAccount.getCustomer()).collect(Collectors.<TreasuryEvent> toList()));

            if (debitNote == null) {
                addInfoMessage(BundleUtil.getString(Constants.BUNDLE,
                        "label.document.manageInvoice.createDebitEntry.entry.with.no.document"), model);
            }

            return "treasury/document/manageinvoice/debitentry/create";
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }
        return redirect(FinantialInstitutionController.SEARCH_URL, model, redirectAttributes);
    }

    // @formatter: off

    @RequestMapping(value = "/createpostback", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> createpostback(@RequestParam(value = "bean", required = true) DebitEntryBean bean,
            Model model) {

        bean.refreshProductsDataSource();

        Product product = bean.getProduct();
        if (product != null) {
            bean.setVat(bean.getDebtAccount().getFinantialInstitution().getActiveVat(product.getVatType(), new DateTime()));
            Tariff tariff = product.getActiveTariffs(bean.getDebtAccount().getFinantialInstitution(), new DateTime()).findFirst()
                    .orElse(null);

            if (tariff != null) {
                if (tariff instanceof FixedTariff) {
                    bean.setAmount(bean.getDebtAccount().getFinantialInstitution().getCurrency()
                            .getValueWithScale(((FixedTariff) tariff).getAmount()));
                    bean.setDueDate(((FixedTariff) tariff).calculateDueDate(bean.getFinantialDocument()));
                    if (tariff.getInterestRate() != null) {
                        bean.setApplyInterests(true);
                        bean.setInterestRate(new FixedTariffInterestRateBean(tariff.getInterestRate()));
                    } else {
                        bean.setApplyInterests(false);
                        bean.setInterestRate(new FixedTariffInterestRateBean());
                    }
                } else {
                    bean.setAmount(
                            bean.getDebtAccount().getFinantialInstitution().getCurrency().getValueWithScale(BigDecimal.ZERO));
                    bean.setDueDate(new LocalDate());
                }
            } else {
                return new ResponseEntity<String>(BundleUtil.getString(Constants.BUNDLE, "label.Tariff.no.valid.fixed"),
                        HttpStatus.BAD_REQUEST);
            }
            bean.setDescription(product.getName().getContent());
        }
        return new ResponseEntity<String>(getBeanJson(bean), HttpStatus.OK);
    }

    @RequestMapping(value = CREATE_URI + "{oid}", method = RequestMethod.POST)
    public String create(@RequestParam(value = "bean", required = false) DebitEntryBean bean,
            @PathVariable("oid") DebtAccount debtAccount, Model model, RedirectAttributes redirectAttributes) {

        try {
            assertUserIsAllowToModifyInvoices(debtAccount.getFinantialInstitution(), model);
            if (bean.getFinantialDocument() != null && !bean.getFinantialDocument().isPreparing()) {
                addWarningMessage(BundleUtil.getString(Constants.BUNDLE,
                        "label.error.document.manageinvoice.debitentry.invalid.state.add.debitentry"), model);
                redirect(DebitNoteController.READ_URL + bean.getFinantialDocument().getExternalId(), model, redirectAttributes);
            }

            DebitEntry debitEntry = createDebitEntry(bean.getFinantialDocument(), bean.getDebtAccount(), bean.getDescription(),
                    bean.getProduct(), bean.getAmount(), bean.getQuantity(), bean.getDueDate(), bean.getEntryDate(),
                    bean.getTreasuryEvent(), bean.isApplyInterests(), bean.getInterestRate());

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.create"), model);

            //Success Validation
            //Add the bean to be used in the View
            setDebitEntry(debitEntry, model);

            if (getDebitEntry(model).getFinantialDocument() != null) {
                return redirect(DebitNoteController.READ_URL + getDebitEntry(model).getFinantialDocument().getExternalId(), model,
                        redirectAttributes);
            } else {
                return redirect(DebtAccountController.READ_URL + getDebitEntry(model).getDebtAccount().getExternalId(), model,
                        redirectAttributes);
            }
        } catch (Exception de) {

            /*
             * If there is any error in validation 
             *
             * Add a error / warning message
             * 
             * addErrorMessage(BundleUtil.getString(TreasurySpringConfiguration.BUNDLE, "label.error.create") + de.getLocalizedMessage(),model);
             * addWarningMessage(" Warning creating due to "+ ex.getLocalizedMessage(),model); */

            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + de.getLocalizedMessage(), model);
            this.setDebitEntryBean(bean, model);
            return "treasury/document/manageinvoice/debitentry/create";
        }
    }

    // @formatter: on

//				

    @Atomic
    public DebitEntry createDebitEntry(DebitNote debitNote, DebtAccount debtAccount, java.lang.String description,
            org.fenixedu.treasury.domain.Product product, java.math.BigDecimal amount, java.math.BigDecimal quantity,
            LocalDate dueDate, DateTime entryDateTime, final TreasuryEvent treasuryEvent, boolean applyInterests,
            final FixedTariffInterestRateBean interestRateBean) {

        // @formatter: off

        /*
         * Modify the creation code here if you do not want to create
         * the object with the default constructor and use the setter
         * for each field
         * 
         */

        // CHANGE_ME It's RECOMMENDED to use "Create service" in DomainObject
        //DebitEntry debitEntry = debitEntry.create(fields_to_create);

        //Instead, use individual SETTERS and validate "CheckRules" in the end
        // @formatter: on

        Optional<Tariff> tariff = product.getActiveTariffs(debtAccount.getFinantialInstitution(), new DateTime()).findFirst();

        Optional<Vat> activeVat =
                Vat.findActiveUnique(product.getVatType(), debtAccount.getFinantialInstitution(), new DateTime());

        DebitEntry debitEntry = DebitEntry.create(Optional.<DebitNote> ofNullable(debitNote), debtAccount, treasuryEvent,
                activeVat.orElse(null), amount, dueDate, null, product, description, quantity, null, entryDateTime);

        if (applyInterests) {
            InterestRate interestRate = InterestRate.createForDebitEntry(debitEntry, interestRateBean.getInterestType(),
                    interestRateBean.getNumberOfDaysAfterDueDate(), interestRateBean.getApplyInFirstWorkday(),
                    interestRateBean.getMaximumDaysToApplyPenalty(), interestRateBean.getMaximumMonthsToApplyPenalty(),
                    interestRateBean.getInterestFixedAmount(), interestRateBean.getRate());
            debitEntry.changeInterestRate(interestRate);
        }

        return debitEntry;
    }

//

//               THIS SHOULD BE USED ONLY WHEN USING ANGULAR 
//
    // @formatter: off

    @RequestMapping(value = "/updatepostback/{oid}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody String updatepostback(@PathVariable("oid") DebitEntry debitEntry,
            @RequestParam(value = "bean", required = false) DebitEntryBean bean, Model model) {

        // Do validation logic ?!?!
        this.setDebitEntryBean(bean, model);
        return getBeanJson(bean);
    }

//  
    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") DebitEntry debitEntry, Model model, RedirectAttributes redirectAttributes) {
        assertUserIsAllowToModifyInvoices(debitEntry.getDebtAccount().getFinantialInstitution(), model);

        if (debitEntry.getFinantialDocument() == null || !debitEntry.getFinantialDocument().isAnnulled()) {
            setDebitEntryBean(new DebitEntryBean(debitEntry), model);
//            model.addAttribute("DebitEntry_event_options",
//                    TreasuryEvent.findActiveBy(debitEntry.getDebtAccount()).collect(Collectors.<TreasuryEvent> toList()));
            setDebitEntry(debitEntry, model);
            return "treasury/document/manageinvoice/debitentry/update";
        } else {
            return redirect(DebitEntryController.READ_URL + debitEntry.getExternalId(), model, redirectAttributes);
        }
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") DebitEntry debitEntry,
            @RequestParam(value = "bean", required = false) DebitEntryBean bean, Model model,
            RedirectAttributes redirectAttributes) {
        setDebitEntry(debitEntry, model);

        if (!debitEntry.equals(bean.getDebitEntry())) {
            addWarningMessage(BundleUtil.getString(Constants.BUNDLE,
                    "label.error.document.manageinvoice.debitentry.invalid.state.add.debitentry"), model);
            redirect(DebitNoteController.READ_URL + debitEntry.getFinantialDocument().getExternalId(), model, redirectAttributes);

        }

//        if (debitEntry.getFinantialDocument() != null && !debitEntry.getFinantialDocument().isPreparing()) {
//            addWarningMessage(BundleUtil.getString(Constants.BUNDLE,
//                    "label.error.document.manageinvoice.debitentry.invalid.state.add.debitentry"), model);
//            redirect(DebitNoteController.READ_URL + debitEntry.getFinantialDocument().getExternalId(), model, redirectAttributes);
//        }

        try {
            assertUserIsAllowToModifyInvoices(debitEntry.getDebtAccount().getFinantialInstitution(), model);
            /*
            *  UpdateLogic here
            */

            //event if the document is in preparing, debts cannot change their amount
            updateDebitEntry(debitEntry.getDescription(), bean.getTreasuryEvent(), bean.isApplyInterests(),
                    bean.getInterestRate(), bean.getDueDate(), !bean.isAcademicalActBlockingSuspension(),
                    bean.isBlockAcademicActsOnDebt(), model);
            /*Succes Update */

            if (debitEntry.getFinantialDocument() != null) {
                return redirect(DebitNoteController.READ_URL + debitEntry.getFinantialDocument().getExternalId(), model,
                        redirectAttributes);
            } else {
                return redirect(DebtAccountController.READ_URL + debitEntry.getDebtAccount().getExternalId(), model,
                        redirectAttributes);
            }
        } catch (Exception de) {

            /*
            * If there is any error in validation 
            *
            * Add a error / warning message
            * 
            * addErrorMessage(BundleUtil.getString(TreasurySpringConfiguration.BUNDLE, "label.error.update") + de.getLocalizedMessage(),model);
            * addWarningMessage(" Warning updating due to " + de.getLocalizedMessage(),model);
            */

            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + de.getLocalizedMessage(), model);
            setDebitEntryBean(bean, model);
            setDebitEntry(debitEntry, model);
            return "treasury/document/manageinvoice/debitentry/update";
        }
    }

    @Atomic
    public void updateDebitEntry(java.lang.String description, final TreasuryEvent treasuryEvent, boolean applyInterests,
            FixedTariffInterestRateBean interestRateBean, LocalDate dueDate, boolean academicalActBlockingSuspension,
            boolean blockAcademicActsOnDebt, Model model) {

        // @formatter: off				
        /*
         * Modify the update code here if you do not want to update
         * the object with the default setter for each field
         */

        // CHANGE_ME It's RECOMMENDED to use "Edit service" in DomainObject
        //getDebitEntry(model).edit(fields_to_edit);

        //Instead, use individual SETTERS and validate "CheckRules" in the end
        // @formatter: on

        DebitEntry debitEntry = getDebitEntry(model);
        debitEntry.edit(description, treasuryEvent, dueDate, academicalActBlockingSuspension, blockAcademicActsOnDebt);

        if (applyInterests) {
            if (debitEntry.getInterestRate() == null) {
                InterestRate interestRate = InterestRate.createForDebitEntry(debitEntry, interestRateBean.getInterestType(),
                        interestRateBean.getNumberOfDaysAfterDueDate(), interestRateBean.getApplyInFirstWorkday(),
                        interestRateBean.getMaximumDaysToApplyPenalty(), interestRateBean.getMaximumMonthsToApplyPenalty(),
                        interestRateBean.getInterestFixedAmount(), interestRateBean.getRate());

            } else {
                InterestRate rate = debitEntry.getInterestRate();
                rate.edit(interestRateBean.getInterestType(), interestRateBean.getNumberOfDaysAfterDueDate(),
                        interestRateBean.getApplyInFirstWorkday(), interestRateBean.getMaximumDaysToApplyPenalty(),
                        interestRateBean.getMaximumMonthsToApplyPenalty(), interestRateBean.getInterestFixedAmount(),
                        interestRateBean.getRate());
            }
        } else {
            if (debitEntry.getInterestRate() != null) {
                debitEntry.getInterestRate().delete();
            }
        }
    }

    private static final String _SEARCHPENDINGENTRIES_URI = "/searchpendingentries/";
    public static final String SEARCHPENDINGENTRIES_URL = CONTROLLER_URL + _SEARCHPENDINGENTRIES_URI;

    @RequestMapping(value = _SEARCHPENDINGENTRIES_URI)
    public String searchPendingEntries(@RequestParam("debitnote") DebitNote debitNote, Model model) {
        List<DebitEntry> searchpendingentriesResultsDataSet = filterSearchPendingEntries(debitNote.getDebtAccount());

        //add the results dataSet to the model
        model.addAttribute("searchpendingentriesResultsDataSet", searchpendingentriesResultsDataSet);
        if (debitNote != null) {
            model.addAttribute("debitNote", debitNote);
        }
        return "treasury/document/manageinvoice/debitentry/searchpendingentries";
    }

    private Stream<DebitEntry> getSearchUniverseSearchPendingEntriesDataSet(DebtAccount debtAccount) {
        return debtAccount.getInvoiceEntrySet().stream().filter(x -> x.isDebitNoteEntry())
                .filter(x -> x.getFinantialDocument() == null).map(DebitEntry.class::cast);
    }

    private List<DebitEntry> filterSearchPendingEntries(DebtAccount debtAccount) {

        return getSearchUniverseSearchPendingEntriesDataSet(debtAccount).collect(Collectors.toList());
    }

    private static final String _SEARCHPENDINGENTRIES_TO_VIEW_ACTION_URI = "/searchpendingentries/view/";
    public static final String SEARCHPENDINGENTRIES_TO_VIEW_ACTION_URL =
            CONTROLLER_URL + _SEARCHPENDINGENTRIES_TO_VIEW_ACTION_URI;

    @RequestMapping(value = _SEARCHPENDINGENTRIES_TO_VIEW_ACTION_URI + "{oid}")
    public String processSearchPendingEntriesToViewAction(@PathVariable("oid") DebitEntry debitEntry, Model model,
            RedirectAttributes redirectAttributes) {

        return redirect(DebitEntryController.READ_URL + debitEntry.getExternalId(), model, redirectAttributes);
    }

    private static final String _SEARCHPENDINGENTRIES_TO_ADDENTRIES_URI = "/searchpendingentries/addentries";
    public static final String SEARCHPENDINGENTRIES_TO_ADDENTRIES_URL = CONTROLLER_URL + _SEARCHPENDINGENTRIES_TO_ADDENTRIES_URI;

    @RequestMapping(value = _SEARCHPENDINGENTRIES_TO_ADDENTRIES_URI, method = RequestMethod.POST)
    public String processSearchPendingEntriesToAddEntries(@RequestParam("debitNote") DebitNote debitNote,
            @RequestParam("debitEntrys") List<DebitEntry> debitEntries, Model model, RedirectAttributes redirectAttributes) {

        if (debitNote != null && !debitNote.isPreparing()) {
            addWarningMessage(BundleUtil.getString(Constants.BUNDLE,
                    "label.error.document.manageinvoice.debitentry.invalid.state.add.debitentry"), model);
            redirect(DebitNoteController.READ_URL + debitNote.getExternalId(), model, redirectAttributes);
        }
        try {
            assertUserIsAllowToModifyInvoices(debitNote.getDebtAccount().getFinantialInstitution(), model);
            debitNote.addDebitNoteEntries(debitEntries);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + ex.getLocalizedMessage(), model);
        }

        addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.update"), model);
        return redirect(DebitNoteController.READ_URL + debitNote.getExternalId(), model, redirectAttributes);
    }

    private static final String _READ_TO_REMOVEFROMDOCUMENT_URI = "/read/{oid}/removefromdocument";

    @RequestMapping(value = _READ_TO_REMOVEFROMDOCUMENT_URI, method = RequestMethod.POST)
    public String processReadToRemoveFromDocument(@PathVariable("oid") DebitEntry debitEntry, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            assertUserIsAllowToModifyInvoices(debitEntry.getDebtAccount().getFinantialInstitution(), model);
            if (debitEntry.getFinantialDocument() != null && debitEntry.getFinantialDocument().isPreparing()) {
                addInfoMessage(BundleUtil.getString(Constants.BUNDLE,
                        "label.error.document.manageinvoice.debitentry.sucess.remove.debitentry"), model);
                FinantialDocument debitNote = debitEntry.getFinantialDocument();
                removeFromDocument(debitEntry);
                return redirect(DebitNoteController.READ_URL + debitNote.getExternalId(), model, redirectAttributes);
            }

            addWarningMessage(BundleUtil.getString(Constants.BUNDLE,
                    "label.error.document.manageinvoice.debitentry.invalid.state.remove.debitentry"), model);
            return redirect(DebitEntryController.READ_URL + debitEntry.getExternalId(), model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }
        return redirect(DebitEntryController.READ_URL + debitEntry.getExternalId(), model, redirectAttributes);
    }

    private static final String _ANNUL_DEBIT_ENTRY_URI = "/read/{oid}/annuldebitentry";

    @RequestMapping(value = _ANNUL_DEBIT_ENTRY_URI, method = RequestMethod.POST)
    public String annuldebitentry(@PathVariable("oid") final DebitEntry debitEntry,
            @RequestParam(value = "annulDebitEntryReason", required = false) final String annulDebitEntryReason,
            final Model model, final RedirectAttributes redirectAttributes) {

        try {

            assertUserIsAllowToModifyInvoices(debitEntry.getDebtAccount().getFinantialInstitution(), model);
            if (debitEntry.getFinantialDocument() == null) {

                debitEntry.annulDebitEntry(annulDebitEntryReason);

                return redirect(DebitNoteController.READ_URL + debitEntry.getFinantialDocument().getExternalId(), model,
                        redirectAttributes);
            }

        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }

        return redirect(DebitEntryController.READ_URL + debitEntry.getExternalId(), model, redirectAttributes);
    }

    @Atomic
    private void removeFromDocument(DebitEntry debitEntry) {
        debitEntry.setFinantialDocument(null);
    }
}
