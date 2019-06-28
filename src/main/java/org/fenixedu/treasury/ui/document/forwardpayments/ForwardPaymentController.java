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
package org.fenixedu.treasury.ui.document.forwardpayments;

import static org.fenixedu.treasury.util.TreasuryConstants.treasuryBundle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPaymentConfiguration;
import org.fenixedu.treasury.domain.forwardpayments.implementations.IForwardPaymentImplementation;
import org.fenixedu.treasury.dto.InterestRateBean;
import org.fenixedu.treasury.dto.SettlementNoteBean;
import org.fenixedu.treasury.dto.SettlementNoteBean.CreditEntryBean;
import org.fenixedu.treasury.dto.SettlementNoteBean.DebitEntryBean;
import org.fenixedu.treasury.dto.SettlementNoteBean.InterestEntryBean;
import org.fenixedu.treasury.dto.forwardpayments.ForwardPaymentStatusBean;
import org.fenixedu.treasury.services.reports.DocumentPrinter;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.accounting.managecustomer.CustomerController;
import org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController;
import org.fenixedu.treasury.util.TreasuryConstants;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qubit.terra.docs.util.ReportGenerationException;

import pt.ist.fenixframework.Atomic;

@BennuSpringController(CustomerController.class)
@RequestMapping(ForwardPaymentController.CONTROLLER_URL)
public class ForwardPaymentController extends TreasuryBaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(ForwardPaymentController.class);
    
    public static final String CONTROLLER_URL = "/treasury/document/forwardpayments/forwardpayment";
    private static final String JSP_PATH = "/treasury/document/forwardpayments/forwardpayment";

    private void setSettlementNoteBean(SettlementNoteBean bean, Model model) {
        final IForwardPaymentImplementation implementation = ForwardPaymentConfiguration
                .findUniqueActive(bean.getDebtAccount().getFinantialInstitution()).get().implementation();

        model.addAttribute("settlementNoteBeanJson", getBeanJson(bean));
        model.addAttribute("settlementNoteBean", bean);
        model.addAttribute("logosPage", implementation.getLogosJspPage());
        model.addAttribute("warningBeforeRedirectionPage", implementation.getWarningBeforeRedirectionJspPage());
        model.addAttribute("localeCode", I18N.getLocale().getLanguage());
        model.addAttribute("chooseInvoiceEntriesUrl", readChooseInvoiceEntriesUrl());
        model.addAttribute("summaryUrl", readSummaryUrl());
        model.addAttribute("debtAccountUrl", readDebtAccountUrl());
    }

    private static final String CHOOSE_INVOICE_ENTRIES_URI = "/chooseInvoiceEntries/";
    public static final String CHOOSE_INVOICE_ENTRIES_URL = CONTROLLER_URL + CHOOSE_INVOICE_ENTRIES_URI;

    protected String readChooseInvoiceEntriesUrl() {
        return CHOOSE_INVOICE_ENTRIES_URL;
    }

    protected void checkPermissions(DebtAccount debtAccount, Model model) {
        assertUserIsAllowToModifySettlements(debtAccount.getFinantialInstitution(), model);
    }

    protected String redirectToDebtAccountUrl(final DebtAccount debtAccount, final Model model,
            final RedirectAttributes redirectAttributes) {
        return redirect(readDebtAccountUrl() + debtAccount.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = CHOOSE_INVOICE_ENTRIES_URI + "{debtAccountId}", method = RequestMethod.GET)
    public String chooseInvoiceEntries(@PathVariable(value = "debtAccountId") DebtAccount debtAccount,
            @RequestParam(value = "bean", required = false) SettlementNoteBean bean, Model model,
            final RedirectAttributes redirectAttributes) {
        try {
            checkPermissions(debtAccount, model);
        } catch (SecurityException e) {
            addErrorMessage(treasuryBundle("error.ForwardPaymentController.payment.not.accessible.for.debt.account"), model);
            return redirectToDebtAccountUrl(debtAccount, model, redirectAttributes);
        }

        if (bean == null) {
            bean = new SettlementNoteBean(debtAccount, false, true);
        }

        setSettlementNoteBean(bean, model);

        final ForwardPaymentConfiguration forwardPaymentConfiguration =
                ForwardPaymentConfiguration.findUniqueActive(debtAccount.getFinantialInstitution()).get();
        model.addAttribute("forwardPaymentConfiguration", forwardPaymentConfiguration);

        return jspPage("chooseInvoiceEntries");
    }

    @RequestMapping(value = CHOOSE_INVOICE_ENTRIES_URI, method = RequestMethod.POST)
    public String chooseInvoiceEntries(@RequestParam(value = "bean", required = true) SettlementNoteBean bean, Model model,
            final RedirectAttributes redirectAttributes) {

        final DebtAccount debtAccount = bean.getDebtAccount();
        final ForwardPaymentConfiguration forwardPaymentConfiguration =
                ForwardPaymentConfiguration.findUniqueActive(debtAccount.getFinantialInstitution()).get();
        model.addAttribute("forwardPaymentConfiguration", forwardPaymentConfiguration);

        BigDecimal debitSum = BigDecimal.ZERO;
        BigDecimal creditSum = BigDecimal.ZERO;
        boolean error = false;

        try {
            checkPermissions(bean.getDebtAccount(), model);
        } catch (SecurityException e) {
            addErrorMessage(treasuryBundle("error.ForwardPaymentController.payment.not.accessible.for.debt.account"), model);
            return redirectToDebtAccountUrl(debtAccount, model, redirectAttributes);
        }

        final Set<InvoiceEntry> invoiceEntriesSet = Sets.newHashSet();
        for (int i = 0; i < bean.getDebitEntries().size(); i++) {
            DebitEntryBean debitEntryBean = bean.getDebitEntries().get(i);
            if (debitEntryBean.isIncluded()) {
                invoiceEntriesSet.add(debitEntryBean.getDebitEntry());

                if (debitEntryBean.getDebtAmountWithVat().compareTo(BigDecimal.ZERO) == 0) {
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
                //Always perform the sum, in order to verify if creditSum is not higher than debitSum
                debitSum = debitSum.add(debitEntryBean.getDebtAmountWithVat());
            } else {
                debitEntryBean.setNotValid(false);
            }
        }
        for (CreditEntryBean creditEntryBean : bean.getCreditEntries()) {
            if (creditEntryBean.isIncluded()) {
                creditSum = creditSum.add(creditEntryBean.getCreditAmountWithVat());
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
        if (!bean.isReimbursementNote() && debitSum.compareTo(BigDecimal.ZERO) == 0) {
            error = true;
            addErrorMessage(treasuryBundle("error.DebiEntry.no.debitEntries.selected"), model);
        }
        if (bean.getDate().isAfter(new LocalDate())) {
            error = true;
            addErrorMessage(treasuryBundle("error.SettlementNote.date.is.after"), model);
        }

        try {
            SettlementNote.checkMixingOfInvoiceEntriesExportedInLegacyERP(invoiceEntriesSet);
        } catch (final TreasuryDomainException e) {
            error = true;
            addErrorMessage(e.getLocalizedMessage(), model);
        }

        if (error) {
            setSettlementNoteBean(bean, model);
            return jspPage("chooseInvoiceEntries");
        }

        bean.includeAllInterestOfSelectedDebitEntries();        
        setSettlementNoteBean(bean, model);

        boolean hasPaymentInStateOfPostPaymentAndPayedOnPlatformWarningMessage = false;
        for (int i = 0; i < bean.getDebitEntries().size(); i++) {
            DebitEntryBean debitEntryBean = bean.getDebitEntries().get(i);
            if (debitEntryBean.isIncluded()) {
                hasPaymentInStateOfPostPaymentAndPayedOnPlatformWarningMessage |=
                        hasForwardPaymentInStateOfPostPaymentAndPayedOnPlatform(debitEntryBean.getDebitEntry());
            }
        }

        try {
            SettlementNote.checkMixingOfInvoiceEntriesExportedInLegacyERP(bean.getIncludedInvoiceEntryBeans());
        } catch (final TreasuryDomainException e) {
            error = true;
            addErrorMessage(e.getLocalizedMessage(), model);
        }

        if (error) {
            setSettlementNoteBean(bean, model);
            return jspPage("chooseInvoiceEntries");
        }

        model.addAttribute("paymentInStateOfPostPaymentAndPayedOnPlatformWarningMessage",
                hasPaymentInStateOfPostPaymentAndPayedOnPlatformWarningMessage);

        return jspPage("summary");
    }

    private boolean hasForwardPaymentInStateOfPostPaymentAndPayedOnPlatform(final DebitEntry debitEntry) {
        for (final ForwardPayment forwardPayment : debitEntry.getForwardPaymentsSet()) {
            try {

                if (!forwardPayment.getCurrentState().isInStateToPostProcessPayment()) {
                    continue;
                }
                
                if (!forwardPayment.getForwardPaymentConfiguration().isActive()) {
                    continue;
                }
                
                final IForwardPaymentImplementation implementation = forwardPayment.getForwardPaymentConfiguration().implementation();
                
                final ForwardPaymentStatusBean paymentStatusBean = implementation.paymentStatus(forwardPayment);
                if (paymentStatusBean.isInPayedState()) {
                    return true;
                }
                
            } catch(Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }

        return false;
    }

    private static final String SUMMARY_URI = "/summary/";
    public static final String SUMMARY_URL = CONTROLLER_URL + SUMMARY_URI;

    protected String readSummaryUrl() {
        return SUMMARY_URL;
    }

    @RequestMapping(value = SUMMARY_URI, method = RequestMethod.POST)
    public String summary(@RequestParam(value = "bean", required = true) SettlementNoteBean bean, Model model,
            RedirectAttributes redirectAttributes) {

        final DebtAccount debtAccount = bean.getDebtAccount();
        final ForwardPaymentConfiguration forwardPaymentConfiguration =
                ForwardPaymentConfiguration.findUniqueActive(debtAccount.getFinantialInstitution()).get();
        model.addAttribute("forwardPaymentConfiguration", forwardPaymentConfiguration);

        try {
            try {
                checkPermissions(bean.getDebtAccount(), model);
            } catch (SecurityException e) {
                addErrorMessage(treasuryBundle("error.ForwardPaymentController.payment.not.accessible.for.debt.account"), model);
                return redirectToDebtAccountUrl(bean.getDebtAccount(), model, redirectAttributes);
            }

            final ForwardPayment forwardPayment = ForwardPayment.create(bean, 
                    (ForwardPayment p) -> forwardPaymentSuccessUrl(p), 
                    (ForwardPayment p) -> forwardPaymentInsuccessUrl(p));
            
            return redirect(readProcessForwardPaymentUrl() + "/" + forwardPayment.getExternalId(), model, redirectAttributes);
        } catch (final TreasuryDomainException tde) {
            addErrorMessage(tde.getLocalizedMessage(), model);
        }

        setSettlementNoteBean(bean, model);
        return jspPage("summary");
    }

    private static final String PROCESS_FORWARD_PAYMENT_URI = "/processforwardpayment";
    public static final String PROCESS_FORWARD_PAYMENT_URL = CONTROLLER_URL + PROCESS_FORWARD_PAYMENT_URI;

    public String readProcessForwardPaymentUrl() {
        return PROCESS_FORWARD_PAYMENT_URL;
    }

    @RequestMapping(value = PROCESS_FORWARD_PAYMENT_URI + "/{forwardPaymentId}", method = RequestMethod.GET)
    public String processforwardpayment(@PathVariable("forwardPaymentId") final ForwardPayment forwardPayment, final Model model,
            final HttpServletResponse response, final HttpSession session) {
        return forwardPayment.getForwardPaymentConfiguration().getForwardPaymentController(forwardPayment)
                .processforwardpayment(forwardPayment, model, response, session);
    }

    protected String forwardPaymentInsuccessUrl(final ForwardPayment forwardPayment) {
        return FORWARD_PAYMENT_INSUCCESS_URL + "/" + forwardPayment.getExternalId();
    }

    protected String forwardPaymentSuccessUrl(final ForwardPayment forwardPayment) {
        return FORWARD_PAYMENT_SUCCESS_URL + "/" + forwardPayment.getExternalId();
    }

    private static final String FORWARD_PAYMENT_SUCCESS_URI = "/forwardpaymentsuccess";
    public static final String FORWARD_PAYMENT_SUCCESS_URL = CONTROLLER_URL + FORWARD_PAYMENT_SUCCESS_URI;

    @RequestMapping(value = FORWARD_PAYMENT_SUCCESS_URI + "/{forwardPaymentId}", method = RequestMethod.GET)
    public String forwardpaymentsuccess(@PathVariable("forwardPaymentId") final ForwardPayment forwardPayment,
            final Model model) {
        final ForwardPaymentConfiguration forwardPaymentConfiguration = forwardPayment.getForwardPaymentConfiguration();

        model.addAttribute("forwardPaymentConfiguration", forwardPaymentConfiguration);
        model.addAttribute("forwardPayment", forwardPayment);
        model.addAttribute("settlementNote", forwardPayment.getSettlementNote());
        model.addAttribute("logosPage", forwardPayment.getForwardPaymentConfiguration().implementation().getLogosJspPage());
        model.addAttribute("debtAccountUrl", readDebtAccountUrl());
        model.addAttribute("printSettlementNoteUrl", readPrintSettlementNoteUrl());
        return jspPage("paymentSuccess");
    }

    private static final String FORWARD_PAYMENT_INSUCCESS_URI = "/forwardpaymentinsuccess";
    public static final String FORWARD_PAYMENT_INSUCCESS_URL = CONTROLLER_URL + FORWARD_PAYMENT_INSUCCESS_URI;

    @RequestMapping(value = FORWARD_PAYMENT_INSUCCESS_URI + "/{forwardPaymentId}", method = RequestMethod.GET)
    public String forwardpaymentinsuccess(@PathVariable("forwardPaymentId") final ForwardPayment forwardPayment,
            final Model model) {
        final ForwardPaymentConfiguration forwardPaymentConfiguration = forwardPayment.getForwardPaymentConfiguration();

        model.addAttribute("forwardPaymentConfiguration", forwardPaymentConfiguration);
        model.addAttribute("forwardPayment", forwardPayment);
        model.addAttribute("logosPage", forwardPaymentConfiguration.implementation().getLogosJspPage());
        model.addAttribute("debtAccountUrl", readDebtAccountUrl());

        return jspPage("paymentInsuccess");
    }

    private static final String PRINT_SETTLEMENT_NOTE_URI = "/printsettlementnote";
    public static final String PRINT_SETTLEMENT_NOTE_URL = CONTROLLER_URL + PRINT_SETTLEMENT_NOTE_URI;

    protected String readPrintSettlementNoteUrl() {
        return PRINT_SETTLEMENT_NOTE_URL;
    }

    protected String readDebtAccountUrl() {
        return DebtAccountController.READ_URL;
    }

    @RequestMapping(value = PRINT_SETTLEMENT_NOTE_URI + "/{forwardPaymentId}", produces = "application/pdf")
    @ResponseBody
    public Object printsettlementnote(@PathVariable("forwardPaymentId") final ForwardPayment forwardPayment, final Model model,
            final RedirectAttributes redirectAttributes) {
        try {
            if (!forwardPayment.isInPayedState()) {
                throw new TreasuryDomainException("error.ForwardPayment.print.not.possible.not.in.payed.state");
            }

            byte[] report = DocumentPrinter.printFinantialDocument(forwardPayment.getSettlementNote(), DocumentPrinter.PDF);
            return new ResponseEntity<byte[]>(report, HttpStatus.OK);
        } catch (ReportGenerationException rex) {
            addErrorMessage(rex.getLocalizedMessage(), model);
            addErrorMessage(rex.getCause().getLocalizedMessage(), model);

            return redirect(readDebtAccountUrl() + forwardPayment.getExternalId(), model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);

            return redirect(readDebtAccountUrl() + forwardPayment.getExternalId(), model, redirectAttributes);
        }
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }

}
