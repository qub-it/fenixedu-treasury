package org.fenixedu.treasury.ui.document.forwardpayments;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.springframework.ui.Model;

public interface IForwardPaymentController {

    public String processforwardpayment(final ForwardPayment forwardPayment, final Model model,
            final HttpServletResponse response, final HttpSession session);
}
