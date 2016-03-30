package org.fenixedu.treasury.ui.document.forwardpayments;

import java.util.Map;

import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

@SpringFunctionality(app = TreasuryController.class, title = "label.title.returnForwardPayment", accessGroup = "logged")
@RequestMapping(ReturnForwardPaymentController.CONTROLLER_URL)
public class ReturnForwardPaymentController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/document/forwardpayments/returnforwardpayment";

    private static final String RETURN_FORWARD_PAYMENT_URI = "/returnforwardpayment";
    public static final String RETURN_FORWARD_PAYMENT_URL = CONTROLLER_URL + RETURN_FORWARD_PAYMENT_URI;

    @RequestMapping(value = RETURN_FORWARD_PAYMENT_URI + "/{forwardPaymentId}", method = RequestMethod.POST,
            produces = "text/html")
    public @ResponseBody String returnforwardpayment(@PathVariable("forwardPaymentId") final ForwardPayment forwardPayment,
            @RequestParam final Map<String, String> responseData, final Model model) {
        final String externalId = forwardPayment.getExternalId();
        new Thread() {

            @Atomic
            public void run() {
                ((ForwardPayment) FenixFramework.getDomainObject(externalId)).execute(responseData);
            };

        }.start();

        final String url = request.getContextPath() + ForwardPaymentController.WAITING_FOR_PAYMENT_URL + "/"
                + forwardPayment.getExternalId();

        return String.format("<html><body><script>window.top.location='%s';</script></body></html>", url);
    }
    
}
