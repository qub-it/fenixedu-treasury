package org.fenixedu.treasury.ui.document.forwardpayments.implementations.onlinepaymentsgateway.sibs;

import static java.lang.String.format;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.domain.forwardpayments.implementations.onlinepaymentsgateway.sibs.SibsOnlinePaymentsGatewayForwardImplementation;
import org.fenixedu.treasury.dto.forwardpayments.ForwardPaymentStatusBean;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.ui.document.forwardpayments.IForwardPaymentController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import pt.ist.fenixframework.FenixFramework;

@SpringFunctionality(app = TreasuryController.class, title = "label.title.returnSibsOnlinePaymentsGatewayForwardPayment",
        accessGroup = "logged")
@RequestMapping(SibsOnlinePaymentsGatewayForwardPaymentController.CONTROLLER_URL)
public class SibsOnlinePaymentsGatewayForwardPaymentController implements IForwardPaymentController {

    public static final String CONTROLLER_URL = "/treasury/document/forwardpayments/sibsonlinepaymentsgateway";
    private static final String JSP_PATH =
            "/treasury/document/forwardpayments/forwardpayment/implementations/sibsonlinepaymentsgateway";

    private static final String PROCESS_FORWARD_PAYMENT_URI = "/processforwardpayment";

    private static final Logger logger = LoggerFactory.getLogger(SibsOnlinePaymentsGatewayForwardPaymentController.class);
    
    @Override
    public String processforwardpayment(final ForwardPayment forwardPayment, final Model model,
            final HttpServletResponse response, final HttpSession session) {

        try {
            final SibsOnlinePaymentsGatewayForwardImplementation impl =
                    (SibsOnlinePaymentsGatewayForwardImplementation) forwardPayment.getForwardPaymentConfiguration().implementation();
            
            final ForwardPaymentStatusBean bean = impl.prepareCheckout(forwardPayment);
            
            if (!bean.isInvocationSuccess()) {
                return format("redirect:%s", forwardPayment.getForwardPaymentInsuccessUrl());
            }
            
            model.addAttribute("forwardPaymentConfiguration", forwardPayment.getForwardPaymentConfiguration());
            model.addAttribute("debtAccount", forwardPayment.getDebtAccount());
            model.addAttribute("checkoutId", forwardPayment.getSibsCheckoutId());
            model.addAttribute("shopperResultUrl", impl.getReturnURL(forwardPayment));
            model.addAttribute("paymentBrands", bean.getSibsOnlinePaymentBrands());
            
            return jspPage(PROCESS_FORWARD_PAYMENT_URI);
            
        } catch(Exception e) {
            logger.error(e.getLocalizedMessage(), e);

            return String.format("redirect:%s", forwardPayment.getForwardPaymentInsuccessUrl());
        } 
    }
    
    private static final String RETURN_FORWARD_PAYMENT_URI = "/returnforwardpayment";
    public static final String RETURN_FORWARD_PAYMENT_URL = CONTROLLER_URL + RETURN_FORWARD_PAYMENT_URI;

    @RequestMapping(value = RETURN_FORWARD_PAYMENT_URI + "/{forwardPaymentId}", method = RequestMethod.GET)
    public String returnforwardpayment(@PathVariable("forwardPaymentId") final ForwardPayment forwardPayment,
            @RequestParam("id") final String sibsCheckoutId, final Model model, final HttpServletResponse response) {
        try {

            final SibsOnlinePaymentsGatewayForwardImplementation impl =
                    (SibsOnlinePaymentsGatewayForwardImplementation) forwardPayment.getForwardPaymentConfiguration().implementation();
            
            final ForwardPaymentStatusBean bean = impl.paymentStatusByCheckoutId(forwardPayment);

            // First of all save sibsTransactionId
            FenixFramework.atomic(() -> {
                forwardPayment.setSibsTransactionId(bean.getTransactionId());
            });
            
            if (bean.isInPayedState()) {
                FenixFramework.atomic(() -> {
                    forwardPayment.advanceToPayedState(bean.getStatusCode(), bean.getStatusMessage(), bean.getPayedAmount(),
                            bean.getTransactionDate(), bean.getTransactionId(), null, bean.getRequestBody(), bean.getResponseBody(),
                            "");
                });

                return String.format("redirect:%s", forwardPayment.getForwardPaymentSuccessUrl());
            } else {
                FenixFramework.atomic(() -> {
                    forwardPayment.reject(bean.getStatusCode(), bean.getStatusMessage(), bean.getRequestBody(),
                            bean.getResponseBody());
                });
                
                return String.format("redirect:%s", forwardPayment.getForwardPaymentInsuccessUrl());
            }
        } catch(Exception e) {
            logger.error(e.getLocalizedMessage(), e);

            return String.format("redirect:%s", forwardPayment.getForwardPaymentInsuccessUrl());
        }
    }

    private String jspPage(final String page) {
        return JSP_PATH + page;
    }

}
