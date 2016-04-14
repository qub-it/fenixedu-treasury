package org.fenixedu.treasury.ui.document.forwardpayments.implementations;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.domain.forwardpayments.implementations.PaylineImplementation;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.ui.document.forwardpayments.IForwardPaymentController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@SpringFunctionality(app = TreasuryController.class, title = "label.title.paylineReturnForwardPayment", accessGroup = "logged")
@RequestMapping(PaylineController.CONTROLLER_URL)
public class PaylineController extends TreasuryBaseController implements IForwardPaymentController {

    public static final String CONTROLLER_URL = "/treasury/document/forwardpayments/payline";
    private static final String JSP_PATH = "/treasury/document/forwardpayments/forwardpayment/implementations/payline";

    @Override
    public String processforwardpayment(final ForwardPayment forwardPayment, final Model model,
            final HttpServletResponse response) {
        try {
            final PaylineImplementation paylineImplementation =
                    (PaylineImplementation) forwardPayment.getForwardPaymentConfiguration().implementation();
            final boolean paylineSucess = paylineImplementation.doWebPayment(forwardPayment);

            if (!paylineSucess) {
                model.addAttribute("forwardPayment", forwardPayment);
                return jspPage("paylineRequestInsuccess");
            }

            return "redirect:" + forwardPayment.getPaylineRedirectUrl();
        } catch (Exception e) {
            addErrorMessage(e.getLocalizedMessage(), model);
            model.addAttribute("forwardPayment", forwardPayment);
            return jspPage("paylineRequestInsuccess");
        }
    }

    private static final String RETURN_FORWARD_PAYMENT_URI = "/returnforwardpayment";
    public static final String RETURN_FORWARD_PAYMENT_URL = CONTROLLER_URL + RETURN_FORWARD_PAYMENT_URI;

    @RequestMapping(value = RETURN_FORWARD_PAYMENT_URI + "/{forwardPaymentId}/{action}", method = RequestMethod.GET)
    public String returnforwardpayment(@PathVariable("forwardPaymentId") final ForwardPayment forwardPayment,
            @RequestParam final Map<String, String> responseData, @PathVariable("action") final String action, final Model model,
            final HttpServletResponse response) {
        try {

            final PaylineImplementation paylineImplementation =
                    (PaylineImplementation) forwardPayment.getForwardPaymentConfiguration().implementation();
            boolean success = paylineImplementation.processPayment(forwardPayment, action);

            if(success) {
                return String.format("redirect:%s/%s", VIEW_SETTLEMENT_URL, forwardPayment.getExternalId());
            }
            
            return String.format("redirect:%s/%s", PAYMENT_INSUCCESS_URL, forwardPayment.getExternalId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final String PAYMENT_INSUCCESS_URI = "/paymentinsuccess";
    public static final String PAYMENT_INSUCCESS_URL = CONTROLLER_URL + PAYMENT_INSUCCESS_URI;
    
    @RequestMapping(value = PAYMENT_INSUCCESS_URL + "/{forwardPaymentId}", method = RequestMethod.GET)
    public String paymentinsuccess(@PathVariable("forwardPaymentId") final ForwardPayment forwardPayment, final Model model) {
        model.addAttribute("forwardPayment", forwardPayment);
        return jspPage("paylineRequestInsuccess");
    }
    
    private static final String VIEW_SETTLEMENT_URI = "/viewsettlement";
    public static final String VIEW_SETTLEMENT_URL = CONTROLLER_URL + VIEW_SETTLEMENT_URI;

    @RequestMapping(value = VIEW_SETTLEMENT_URI + "/{forwardPaymentId}", method = RequestMethod.GET)
    public String viewsettlement(@PathVariable("forwardPaymentId") final ForwardPayment forwardPayment, final Model model) {
        model.addAttribute("forwardPayment", forwardPayment);
        return jspPage("paylinePaymentSuccess");
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }

}
