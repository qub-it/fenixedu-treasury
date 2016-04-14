package org.fenixedu.treasury.domain.forwardpayments.implementations;

import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.ui.document.forwardpayments.IForwardPaymentController;

public interface IForwardPaymentImplementation {

    public IForwardPaymentController getForwardPaymentController(final ForwardPayment forwardPayment);

    public String getPaymentURL(final ForwardPayment forwardPayment);

    public String getFormattedAmount(final ForwardPayment forwardPayment);
    
    public String getLogosJspPage();

}
