package org.fenixedu.treasury.domain.forwardpayments.implementations;

import java.util.Map;

import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

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
    static final String RESPONSE_CODE_FIELD = "A038";
    static final String AUTHORIZATION_SIBS_DATE_FIELD = "A037";

    public static final String AUTHENTICATION_REQUEST_MESSAGE = "H3D0";
    static final String AUTHENTICATION_RESPONSE_MESSAGE = "MH05";
    static final String C016_AUTHENTICATION_REGISTERED_CODE = "01";
    static final String C016_AUTHORIZATION_ACCEPTED_CODE = "02";
    static final String C016_PAYMENT_ACCEPTED_CODE = "03";
    static final String C016_AUTHORIZATION_CANCELLED = "04";
    static final String C016_UNABLE_TO_CONTACT_HOST_RESPONSE_CODE = "99";

    static final String AUTHORIZATION_REQUEST_MESSAGE = "M001";
    static final String AUTHORIZATION_RESPONSE_MESSAGE = "M101";
    static final String AUTHORIZATION_SUCCESS_RESPONSE_CODE = "000";

    static final String PAYMENT_REQUEST_MESSAGE = "M002";
    static final String PAYMENT_RESPONSE_MESSAGE = "M102";
    static final String PAYMENT_SUCCESS_RESPONSE_CODE = "000";
    
    public static final String CURRENCY_EURO_CODE = "9782";

    @Override
    public Object execute(final ForwardPayment forwardPayment, final Map<String, String> requestData, final Map<String, String> responseData) {
        if (forwardPayment.isInCreatedState() && isAuthenticationResponseMessage(responseData)) {
            requestPaymentAuthorization(forwardPayment);
        }

        return null;
    }
    
    public Map<String, String> mapAuthenticationRequest(final ForwardPayment forwardPayment) {
        final TPAInvocationUtil tpa = new TPAInvocationUtil(forwardPayment, null, null);
        return tpa.mapAuthenticationRequest();
    }

    private void requestPaymentAuthorization(final ForwardPayment forwardPayment) {
        final TPAInvocationUtil tpa = new TPAInvocationUtil(forwardPayment, null, null);
        final Map<String, String> authResult = tpa.postAuthorizationRequest();

        if (!isResponseAuthorized(authResult) || authorizationSibsDate(authResult) == null) {
            forwardPayment.reject(errorMessage(authResult), null, authResult.toString());
            return;
        }

        forwardPayment.advanceToAuthorizedState();
        final DateTime authorizedDate = authorizationSibsDate(authResult);

        requestPaymentTransfer(forwardPayment, authorizedDate);
    }

    private DateTime authorizationSibsDate(final Map<String, String> authResult) {
        if (!authResult.containsKey(AUTHORIZATION_SIBS_DATE_FIELD)) {
            return null;
        }

        return DateTimeFormat.forPattern("YYYYMMddHHmmss").parseDateTime(authResult.get(AUTHORIZATION_SIBS_DATE_FIELD));
    }

    private void requestPaymentTransfer(ForwardPayment forwardPayment, final DateTime authorizationDate) {
        final TPAInvocationUtil tpa = new TPAInvocationUtil(forwardPayment, null, null);
        final Map<String, String> paymentResult = tpa.postPaymentRequest(authorizationDate);

        if (!isPaymentSuccess(paymentResult)) {
            forwardPayment.reject(errorMessage(paymentResult), null, paymentResult.toString());
            return;
        }
        
        // Verify payment amount
        
        // forwardPayment.pay();
    }

    private boolean isPaymentSuccess(final Map<String, String> paymentResult) {
        return paymentResult.containsKey(MESSAGE_CODE_FIELD)
                && PAYMENT_RESPONSE_MESSAGE.equals(paymentResult.get(MESSAGE_CODE_FIELD))
                && paymentResult.containsKey(RESPONSE_CODE_FIELD)
                && PAYMENT_SUCCESS_RESPONSE_CODE.equals(paymentResult.get(RESPONSE_CODE_FIELD));
    }

    private String errorMessage(final Map<String, String> authResult) {
        return null;
    }

    private boolean isResponseAuthorized(Map<String, String> authResult) {
        return authResult.containsKey(MESSAGE_CODE_FIELD)
                && AUTHORIZATION_RESPONSE_MESSAGE.equals(authResult.get(MESSAGE_CODE_FIELD))
                && authResult.containsKey(RESPONSE_CODE_FIELD)
                && AUTHORIZATION_SUCCESS_RESPONSE_CODE.equals(authResult.get(RESPONSE_CODE_FIELD));
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
