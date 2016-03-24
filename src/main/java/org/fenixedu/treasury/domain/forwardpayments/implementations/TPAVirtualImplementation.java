package org.fenixedu.treasury.domain.forwardpayments.implementations;

import java.util.Map;

import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;

public class TPAVirtualImplementation implements IForwardPaymentImplementation {

    static final String MESSAGE_CODE_FIELD = "A030";
    static final String TPA_VIRTUAL_ID_FIELD = "A001";
    static final String REFERENCE_CODE_FIELD = "C007";
    static final String CURRENCY_FIELD = "A105";
    static final String AMOUNT_FIELD = "A061";
    static final String PAN_FIELD = "C003";
    static final String EXPIRATION_FIELD = "C004";
    static final String OPERATION_STATUS_FIELD = "C016";
    static final String SECURE_HASH_CODE_FIELD = "C013";

    public static final String AUTHENTICATION_REQUEST_MESSAGE = "H3D0";
    static final String AUTHENTICATION_RESPONSE_MESSAGE = "MH05";
    static final String C016_AUTHENTICATION_REGISTERED_CODE = "01";
    static final String C016_AUTHORIZATION_ACCEPTED_CODE = "02";
    static final String C016_PAYMENT_ACCEPTED_CODE = "03";
    static final String C016_AUTHORIZATION_CANCELLED = "04";
    static final String C016_UNABLE_TO_CONTACT_HOST_RESPONSE_CODE = "99";

    static final String AUTHORIZATION_REQUEST_MESSAGE = "M001";
    static final String AUTHORIZATION_RESPONSE_MESSAGE = "M101";

    static final String PAYMENT_REQUEST_MESSAGE = "M002";
    static final String PAYMENT_RESPONSE_MESSAGE = "M102";

    static final String CURRENCY_EURO_CODE = "9782";

    @Override
    public Object execute(final ForwardPayment forwardPayment, final Map<String, String> paymentData) {
        if (forwardPayment.isCreated() && isAuthenticationResponseMessage(paymentData)) {
            requestPaymentAuthorization(forwardPayment, paymentData);
        }

        return null;
    }

    private void requestPaymentAuthorization(final ForwardPayment forwardPayment, final Map<String, String> paymentData) {
        final TPAInvocationUtil tpa = new TPAInvocationUtil(forwardPayment, null, null);
        tpa.postAuthorizationRequest();
    }

    private boolean isAuthenticationResponseMessage(Map<String, String> paymentData) {
        return paymentData.containsKey(MESSAGE_CODE_FIELD)
                && AUTHENTICATION_RESPONSE_MESSAGE.equals(paymentData.get(MESSAGE_CODE_FIELD));
    }

    @Override
    public String getPaymentURL(final ForwardPayment forwardPayment) {
        return forwardPayment.getForwardPaymentConfiguration().getPaymentURL();
    }

    @Override
    public String getReturnURL(final ForwardPayment forwardPayment) {
        return forwardPayment.getForwardPaymentConfiguration().getReturnURL() + "/" + forwardPayment.getExternalId();
    }

    @Override
    public String getPaymentPage(final ForwardPayment forwardPayment) {
        return "/treasury/document/forwardpayments/forwardpayment/implementations/tpavirtual/hostedPay";
    }

    @Override
    public String getFormattedAmount(final ForwardPayment forwardPayment) {
        return forwardPayment.getAmount().toString();
    }

}
