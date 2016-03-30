package org.fenixedu.treasury.domain.forwardpayments.implementations;

import java.util.Map;

import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;

public interface IForwardPaymentImplementation {
    
    public Object execute(final ForwardPayment forwardPayment, final Map<String, String> requestData, final Map<String, String> responseData);
    public String getPaymentURL(final ForwardPayment forwardPayment);
    public String getReturnURL(final ForwardPayment forwardPayment);
    public String getPaymentPage(final ForwardPayment forwardPayment);
    public String getFormattedAmount(final ForwardPayment forwardPayment);
    
}
