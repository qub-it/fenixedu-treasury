package org.fenixedu.treasury.domain.forwardpayments.implementations;

import java.math.BigDecimal;
import java.util.Map;

import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.ui.document.forwardpayments.IForwardPaymentController;
import org.fenixedu.treasury.ui.document.forwardpayments.implementations.PaylineController;

public class PaylineImplementation implements IForwardPaymentImplementation {

    public static final String ACTION_RETURN_URL = "return";
    public static final String ACTION_CANCEL_URL = "cancel";
    
    @Override
    public String getPaymentURL(final ForwardPayment forwardPayment) {
        throw new RuntimeException("not applied");
    }

    @Override
    public String getReturnURL(final ForwardPayment forwardPayment) {
        return String.format("%s/%s/%s", forwardPayment.getForwardPaymentConfiguration().getReturnURL(),
                forwardPayment.getExternalId(), ACTION_RETURN_URL);
    }

    public String getCancelURL(final ForwardPayment forwardPayment) {
        return String.format("%s/%s/%s", forwardPayment.getForwardPaymentConfiguration().getReturnURL(),
                forwardPayment.getExternalId(), ACTION_CANCEL_URL);
    }

    @Override
    public String getFormattedAmount(final ForwardPayment forwardPayment) {
        return forwardPayment.getAmount().multiply(new BigDecimal(100)).setScale(0).toString();
    }

    @Override
    public IForwardPaymentController getForwardPaymentController(final ForwardPayment forwardPayment) {
        return new PaylineController();
    }

}
