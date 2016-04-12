package org.fenixedu.treasury.domain.forwardpayments.implementations;

import java.math.BigDecimal;
import java.util.Map;

import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.ui.document.forwardpayments.IForwardPaymentController;
import org.fenixedu.treasury.ui.document.forwardpayments.implementations.TPAVirtualController;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.google.common.base.Strings;

import pt.ist.fenixframework.Atomic;

public class TPAVirtualImplementation implements IForwardPaymentImplementation {

    static final String A030 = "A030";
    static final String TPA_VIRTUAL_ID_FIELD = "A001";
    static final String REFERENCE_CODE_FIELD = "C007";
    static final String CURRENCY_FIELD = "A105";
    static final String AMOUNT_FIELD = "A061";
    static final String PAN_FIELD = "C003";
    static final String EXPIRATION_FIELD = "C004";
    static final String OPERATION_STATUS_FIELD = "C016";
    static final String SECURE_HASH_CODE_FIELD = "C013";
    static final String A037 = "A037";

    public static final String AUTHENTICATION_REQUEST_MESSAGE = "H3D0";
    static final String AUTHENTICATION_RESPONSE_MESSAGE = "MH05";
    static final String C016_AUTHENTICATION_REGISTERED_CODE = "01";
    static final String C016_AUTHORIZATION_ACCEPTED_CODE = "02";
    static final String C016_PAYMENT_ACCEPTED_CODE = "03";
    static final String C016_AUTHORIZATION_CANCELLED = "04";
    static final String C016_UNABLE_TO_CONTACT_HOST_RESPONSE_CODE = "99";

    static final String RESPONSE_CODE_FIELD = "A038";
    static final String RESPONSE_CODE_SUCCESS = "000";

    static final String M020 = "M020";
    static final String M120 = "M120";
    static final String C016 = "C016";

    static final String M001 = "M001";
    static final String M101 = "M101";

    static final String M002 = "M002";
    static final String M102 = "M102";

    public static final String EURO_CODE = "9782";

    @Override
    public IForwardPaymentController getForwardPaymentController(final ForwardPayment forwardPayment) {
        return new TPAVirtualController();
    }

    @Atomic
    public boolean executePayment(final ForwardPayment forwardPayment, final Map<String, String> responseData) {
        if (forwardPayment.isInCreatedState() && !isAuthenticationResponseMessage(responseData)) {
            forwardPayment.reject("CODIGO A PREENCHER: FALHA", "falha na autenticacao", null, null);
            return false;
        }

        return requestPaymentStatus(forwardPayment);
    }

    public Map<String, String> mapAuthenticationRequest(final ForwardPayment forwardPayment) {
        final TPAInvocationUtil tpa = new TPAInvocationUtil(forwardPayment, null, null);
        return tpa.mapAuthenticationRequest();
    }

    private boolean requestPaymentStatus(final ForwardPayment forwardPayment) {
        final TPAInvocationUtil tpa = new TPAInvocationUtil(forwardPayment, null, null);
        Map<String, String> responseMap = tpa.postPaymentStatus();

        if (!isPaymentStatusSuccess(responseMap)) {
            final String responseCode = responseCode(responseMap);
            forwardPayment.reject(responseCode, errorMessage(responseMap), null, responseMap.toString());
            return false;
        }

        int resultCode = Integer.parseInt(responseMap.get(C016));

        if (resultCode == 1) {
            // Invocar a autorização

            responseMap = tpa.postAuthorizationRequest();

            if (!isAuthorizationSuccess(responseMap)) {
                final String responseCode = responseCode(responseMap);
                forwardPayment.reject(responseCode, errorMessage(responseMap), null, responseMap.toString());
                return false;
            }

            resultCode = Integer.parseInt(responseMap.get(C016));
        }

        if (resultCode == 2) {
            // Executar o pagamento
            responseMap = tpa.postPaymentStatus();

            if (!isPaymentStatusSuccess(responseMap)) {
                final String responseCode = responseCode(responseMap);
                forwardPayment.reject(responseCode, errorMessage(responseMap), null, responseMap.toString());
                return false;
            }

            forwardPayment.advanceToAuthorizedState(String.valueOf(resultCode), "Payment Authorized", null, null);

            final DateTime authorizationDate = authorizationSibsDate(responseMap);
            responseMap = tpa.postPayment(authorizationDate);

            if (!isPaymentSuccess(responseMap)) {
                final String responseCode = responseCode(responseMap);
                forwardPayment.reject(responseCode, errorMessage(responseMap), null, responseMap.toString());
                return false;
            }

            resultCode = Integer.parseInt(responseMap.get(C016));
        }

        if (resultCode == 3) {
            // Pagamento efetuado
            responseMap = tpa.postPaymentStatus();

            if (!isPaymentStatusSuccess(responseMap)) {
                final String responseCode = responseCode(responseMap);
                forwardPayment.reject(responseCode, errorMessage(responseMap), null, responseMap.toString());
                return false;
            }

            final String transactionId = transactionId(responseMap);
            final DateTime transactionDate = authorizationSibsDate(responseMap);
            final BigDecimal payedAmount = payedAmount(responseMap);

            forwardPayment.advanceToPayedState(String.valueOf(resultCode), "Pagamento realizado", payedAmount, transactionDate,
                    transactionId, null);

            return true;
        }

        if (resultCode == 4) {
            forwardPayment.reject(String.valueOf(resultCode), "Authorization cancelled", null, null);
        } else {
            forwardPayment.reject(String.valueOf(resultCode), "SIBS communication error", null, null);
        }

        return false;
    }

    private BigDecimal payedAmount(Map<String, String> responseData) {
        if (!Strings.isNullOrEmpty(responseData.get(AMOUNT_FIELD))) {
            return new BigDecimal(responseData.get(AMOUNT_FIELD)).divide(new BigDecimal(100));
        }

        return null;
    }

    private String transactionId(Map<String, String> responseData) {
        if (!Strings.isNullOrEmpty(responseData.get("C026"))) {
            return responseData.get("C026");
        }

        return null;
    }

    private DateTime authorizationSibsDate(final Map<String, String> authResult) {
        if (!authResult.containsKey(A037)) {
            return null;
        }

        return DateTimeFormat.forPattern("YYYYMMddHHmmss").parseDateTime(authResult.get(A037));
    }

    private boolean isResponseSuccess(final Map<String, String> paymentResult) {
        return paymentResult.get(RESPONSE_CODE_FIELD) != null
                && RESPONSE_CODE_SUCCESS.equals(paymentResult.get(RESPONSE_CODE_FIELD));
    }

    private boolean isPaymentStatusSuccess(final Map<String, String> paymentResult) {
        return isResponseSuccess(paymentResult) && paymentResult.get(A030) != null && M120.equals(paymentResult.get(A030));
    }

    private boolean isAuthorizationSuccess(Map<String, String> authResult) {
        return isResponseSuccess(authResult) && authResult.containsKey(A030) && M101.equals(authResult.get(A030));
    }

    private boolean isPaymentSuccess(final Map<String, String> paymentResult) {
        return isResponseSuccess(paymentResult) && paymentResult.containsKey(A030) && M102.equals(paymentResult.get(A030));
    }

    private String errorMessage(final Map<String, String> authResult) {
        return null;
    }

    private boolean isAuthenticationResponseMessage(Map<String, String> paymentData) {
        return paymentData.containsKey(A030) && AUTHENTICATION_RESPONSE_MESSAGE.equals(paymentData.get(A030));
    }

    private String responseCode(final Map<String, String> authResult) {
        if (!authResult.containsKey(RESPONSE_CODE_FIELD)) {
            return null;
        }

        return authResult.get(RESPONSE_CODE_FIELD);
    }

    @Override
    public String getPaymentURL(final ForwardPayment forwardPayment) {
        return forwardPayment.getForwardPaymentConfiguration().getPaymentURL();
    }

    @Override
    public String getReturnURL(final ForwardPayment forwardPayment) {
        return String.format("http://localhost:8080%s/%s", TPAVirtualController.RETURN_FORWARD_PAYMENT_URL,
                forwardPayment.getExternalId());
    }

    @Override
    public String getFormattedAmount(final ForwardPayment forwardPayment) {
        return forwardPayment.getAmount().toString();
    }

}
