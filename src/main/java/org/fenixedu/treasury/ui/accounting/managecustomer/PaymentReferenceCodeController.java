package org.fenixedu.treasury.ui.accounting.managecustomer;

import static org.fenixedu.treasury.util.TreasuryConstants.treasuryBundle;

import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCode;
import org.fenixedu.treasury.dto.document.managepayments.PaymentReferenceCodeBean;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Component("org.fenixedu.treasury.ui.accounting.managecustomer")
@BennuSpringController(value = CustomerController.class)
@RequestMapping(PaymentReferenceCodeController.CONTROLLER_URL)
public class PaymentReferenceCodeController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/accounting/managecustomer/paymentreferencecode";
    private static final String JSP_PATH = "treasury/accounting/managecustomer/paymentreferencecode";

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/";
    }

    private static final String _CREATEPAYMENTCODEFORSEVERALDEBITENTRIES_URI = "/createpaymentcodeforseveraldebitentries";
    public static final String CREATEPAYMENTCODEFORSEVERALDEBITENTRIES_URL = CONTROLLER_URL
            + _CREATEPAYMENTCODEFORSEVERALDEBITENTRIES_URI;

    @RequestMapping(value = _CREATEPAYMENTCODEFORSEVERALDEBITENTRIES_URI + "/{debtAccountId}", method = RequestMethod.GET)
    public String createPaymentCodeForSeveralDebitEntries(@PathVariable("debtAccountId") final DebtAccount debtAccount,
            final Model model) {
        
        assertUserIsFrontOfficeMember(debtAccount.getFinantialInstitution(), model);
        
        final PaymentReferenceCodeBean bean = new PaymentReferenceCodeBean(debtAccount);

        return _createPaymentCodeForSeveralDebitEntries(debtAccount, bean, model);
    }

    private String _createPaymentCodeForSeveralDebitEntries(final DebtAccount debtAccount, final PaymentReferenceCodeBean bean,
            final Model model) {

        model.addAttribute("debtAccount", debtAccount);
        model.addAttribute("bean", bean);
        model.addAttribute("beanJson", getBeanJson(bean));

        return jspPage("createpaymentcodeforseveraldebitentries");
    }

    private static final String _CREATEPAYMENTCODEFORSEVERALDEBITENTRIESPOSTBACK_URI =
            "/createpaymentcodeforseveraldebitentriespostback";
    public static final String CREATEPAYMENTCODEFORSEVERALDEBITENTRIESPOSTBACK_URL = CONTROLLER_URL
            + _CREATEPAYMENTCODEFORSEVERALDEBITENTRIESPOSTBACK_URI;

    @RequestMapping(value = _CREATEPAYMENTCODEFORSEVERALDEBITENTRIESPOSTBACK_URI + "/{debtAccountId}",
            method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> createPaymentCodeForSeveralDebitEntriesPostback(
            @PathVariable("debtAccountId") final DebtAccount debtAccount,
            @RequestParam("bean") final PaymentReferenceCodeBean bean, final Model model) {

        bean.updateAmountOnSelectedDebitEntries();

        return new ResponseEntity<String>(getBeanJson(bean), HttpStatus.OK);
    }

    @RequestMapping(value = _CREATEPAYMENTCODEFORSEVERALDEBITENTRIES_URI + "/{debtAccountId}", method = RequestMethod.POST)
    public String createPaymentCodeForSeveralDebitEntries(@PathVariable("debtAccountId") final DebtAccount debtAccount,
            @RequestParam("bean") final PaymentReferenceCodeBean bean, final Model model,
            final RedirectAttributes redirectAttributes) {

        assertUserIsFrontOfficeMember(debtAccount.getFinantialInstitution(), model);

        try {

            if(bean.getSelectedDebitEntries() == null || bean.getSelectedDebitEntries().isEmpty()) {
                addErrorMessage(treasuryBundle("error.MultipleEntriesPaymentCode.select.at.least.one.debit.entry"), model);

                return _createPaymentCodeForSeveralDebitEntries(debtAccount, bean, model);
            }
            
            if(bean.getPaymentCodePool() == null) {
                addErrorMessage(treasuryBundle("error.MultipleEntriesPaymentCode.payment.code.pool.required"), model);
                
                return _createPaymentCodeForSeveralDebitEntries(debtAccount, bean, model);
            }
            
            assertUserIsFrontOfficeMember(bean.getSelectedDebitEntries().iterator().next().getDebtAccount().getFinantialInstitution(),
                    model);

            PaymentReferenceCode.createPaymentReferenceCodeForMultipleDebitEntries(debtAccount, bean);

            addInfoMessage(treasuryBundle("label.document.managepayments.success.create.reference.code.selected.debit.entries"), model);

            return redirect(String.format(DebtAccountController.READ_URL + "/%s", debtAccount.getExternalId()), model,
                    redirectAttributes);
        } catch (final TreasuryDomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);

            return _createPaymentCodeForSeveralDebitEntries(debtAccount, bean, model);
        }
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }

}
