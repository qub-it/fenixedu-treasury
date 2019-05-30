package org.fenixedu.treasury.ui.document.forwardpayments.implementations.onlinepaymentsgateway.sibs;

import static java.lang.String.format;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.onlinepaymentsgateway.api.CheckoutResultBean;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.domain.forwardpayments.implementations.IForwardPaymentImplementation;
import org.fenixedu.treasury.domain.forwardpayments.implementations.onlinepaymentsgateway.sibs.SibsOnlinePaymentsGatewayForwardImplementation;
import org.fenixedu.treasury.domain.sibsonlinepaymentsgateway.SibsOnlinePaymentsGateway;
import org.fenixedu.treasury.dto.forwardpayments.ForwardPaymentStatusBean;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.ui.document.forwardpayments.IForwardPaymentController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import pt.ist.fenixframework.Atomic;

@SpringFunctionality(app = TreasuryController.class, title = "label.title.sibsOnlinePaymentsGatewayForwardPayment",
        accessGroup = "logged")
@RequestMapping(SibsOnlinePaymentsGatewayForwardPaymentController.CONTROLLER_URL)
public class SibsOnlinePaymentsGatewayForwardPaymentController implements IForwardPaymentController {

    public static final String CONTROLLER_URL = "/treasury/document/forwardpayments/sibsonlinepaymentsgateway";
    private static final String JSP_PATH =
            "/treasury/document/forwardpayments/forwardpayment/implementations/sibsonlinepaymentsgateway";

    private static final String PROCESS_FORWARD_PAYMENT_URI = "/processforwardpayment";

    @Override
    public String processforwardpayment(final ForwardPayment forwardPayment, final Model model,
            final HttpServletResponse response, final HttpSession session) {

        final SibsOnlinePaymentsGatewayForwardImplementation impl =
                (SibsOnlinePaymentsGatewayForwardImplementation) forwardPayment.getForwardPaymentConfiguration().implementation();

        final ForwardPaymentStatusBean bean = impl.prepareCheckout(forwardPayment);

        if (!bean.isInvocationSuccess()) {
            return format("redirect:%s", forwardPayment.getForwardPaymentInsuccessUrl());
        }

        model.addAttribute("forwardPaymentConfiguration", forwardPayment.getForwardPaymentConfiguration());
        model.addAttribute("debtAccount", forwardPayment.getDebtAccount());
        model.addAttribute("sibsTransactionId", forwardPayment.getSibsTransactionId());
        model.addAttribute("shopperResultUrl", format("%s/%s", forwardPayment.getForwardPaymentConfiguration().getReturnURL(), forwardPayment.getExternalId()));
        
        return jspPage(PROCESS_FORWARD_PAYMENT_URI);
    }

    private static final String RETURN_FORWARD_PAYMENT_URI = "/returnforwardpayment";
    public static final String RETURN_FORWARD_PAYMENT_URL = CONTROLLER_URL + RETURN_FORWARD_PAYMENT_URI;

    @RequestMapping(value = RETURN_FORWARD_PAYMENT_URI + "/{forwardPaymentId}", method = RequestMethod.GET)
    public String returnforwardpayment(@PathVariable("forwardPaymentId") final ForwardPayment forwardPayment, final Model model,
            final HttpServletResponse response) {
        final SibsOnlinePaymentsGatewayForwardImplementation impl =
                (SibsOnlinePaymentsGatewayForwardImplementation) forwardPayment.getForwardPaymentConfiguration().implementation();

        ForwardPaymentStatusBean bean = impl.paymentStatus(forwardPayment);

        if (bean.isInPayedState()) {
            return String.format("redirect:%s", forwardPayment.getForwardPaymentSuccessUrl());
        }

        return String.format("redirect:%s", forwardPayment.getForwardPaymentInsuccessUrl());
    }

    private String jspPage(final String page) {
        return JSP_PATH + page;
    }

}
