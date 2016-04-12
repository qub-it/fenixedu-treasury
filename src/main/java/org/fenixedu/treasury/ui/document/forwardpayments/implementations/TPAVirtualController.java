package org.fenixedu.treasury.ui.document.forwardpayments.implementations;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.domain.forwardpayments.implementations.TPAVirtualImplementation;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.ui.document.forwardpayments.IForwardPaymentController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

@SpringFunctionality(app = TreasuryController.class, title = "label.title.tpaReturnForwardPayment", accessGroup = "logged")
@RequestMapping(TPAVirtualController.CONTROLLER_URL)
public class TPAVirtualController extends TreasuryBaseController implements IForwardPaymentController {

    public static final String CONTROLLER_URL = "/treasury/document/forwardpayments/tpavirtual";
    private static final String JSP_PATH = "/treasury/document/forwardpayments/forwardpayment/implementations/tpavirtual";

    public String processforwardpayment(final ForwardPayment forwardPayment, final Model model,
            final HttpServletResponse response) {
        model.addAttribute("forwardPayment", forwardPayment);

        return jspPage("hostedPay");
    }

    private static final String RETURN_FORWARD_PAYMENT_URI = "/returnforwardpayment";
    public static final String RETURN_FORWARD_PAYMENT_URL = CONTROLLER_URL + RETURN_FORWARD_PAYMENT_URI;

    @RequestMapping(value = RETURN_FORWARD_PAYMENT_URI + "/{forwardPaymentId}", method = RequestMethod.POST,
            produces = "text/html")
    @ResponseBody
    public String returnforwardpayment(@PathVariable("forwardPaymentId") final ForwardPayment forwardPayment,
            @RequestParam final Map<String, String> responseData, final Model model, final HttpServletResponse response) {
        TPAVirtualImplementation implementation =
                (TPAVirtualImplementation) forwardPayment.getForwardPaymentConfiguration().implementation();
        
        boolean status = implementation.executePayment(forwardPayment, responseData);

        return jspPage(status ? "tpaSuccess" : "tpaInsuccess");
    }

    private static final String WAITING_FOR_PAYMENT_URI = "/waitingforpayment";
    public static final String WAITING_FOR_PAYMENT_URL = CONTROLLER_URL + WAITING_FOR_PAYMENT_URI;

    @RequestMapping(value = WAITING_FOR_PAYMENT_URI + "/{forwardPaymentId}", method = RequestMethod.GET)
    public String waitingforpayment(@PathVariable("forwardPaymentId") final ForwardPayment forwardPayment, final Model model) {
        model.addAttribute("forwardPayment", forwardPayment);

        return jspPage("waitingforpayment");
    }

    private static final String CURRENT_FORWARD_PAYMENT_STATE_URI = "/currentforwardpaymentstate";
    public static final String CURRENT_FORWARD_PAYMENT_STATE_URL = CONTROLLER_URL + CURRENT_FORWARD_PAYMENT_STATE_URI;

    @RequestMapping(value = CURRENT_FORWARD_PAYMENT_STATE_URI + "/{forwardPaymentId}", method = RequestMethod.POST)
    @ResponseBody
    public String currentforwardpaymentstate(@PathVariable("forwardPaymentId") final ForwardPayment forwardPayment) {
        return forwardPayment.getCurrentState().toString();
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }

}
