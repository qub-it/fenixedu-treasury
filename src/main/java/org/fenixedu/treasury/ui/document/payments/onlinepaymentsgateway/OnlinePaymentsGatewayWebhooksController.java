package org.fenixedu.treasury.ui.document.payments.onlinepaymentsgateway;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.onlinepaymentsgateway.sibs.sdk.NotificationBean;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.ui.document.forwardpayments.ForwardPaymentController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@SpringFunctionality(title="label.OnlinePaymentsGatewayWebhooksController.title", app=TreasuryController.class)
@RequestMapping(OnlinePaymentsGatewayWebhooksController.CONTROLLER_URL)
public class OnlinePaymentsGatewayWebhooksController extends TreasuryBaseController {

    private static final Logger logger = LoggerFactory.getLogger(ForwardPaymentController.class);
    
    public static final String CONTROLLER_URL = "/treasury/document/payments/onlinepaymentsgateway";

    private static final String NOTIFICATION_URI = "/notification";
    public static final String NOTIFICATION_URL = CONTROLLER_URL + NOTIFICATION_URI;
    
    @RequestMapping(path=NOTIFICATION_URI, method=RequestMethod.GET)
    public void notify(final HttpServletRequest request, final HttpServletResponse response) {
        
        try {

            NotificationBean notificationBean = null; // Invoke online payments gateway library
            
            response.setStatus(HttpServletResponse.SC_OK);
        } catch(Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
    }
    
}
