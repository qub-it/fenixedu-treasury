package org.fenixedu.treasury.dto.forwardpayments;

import java.math.BigDecimal;

import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPaymentStateType;
import org.joda.time.DateTime;

public class ForwardPaymentStatusBean {

    private boolean invocationSuccess;

    private ForwardPaymentStateType stateType;

    private String authorizationNumber;
    private DateTime authorizationDate;

    private String transactionId;
    private DateTime transactionDate;
    private BigDecimal payedAmount;

    private String statusCode;
    private String statusMessage;
    private String requestBody;
    private String responseBody;

    
    // Necessary for SIBS Online Payment Gateway
    
    private String sibsOnlinePaymentBrands;
    
    public ForwardPaymentStatusBean(final boolean invocationSuccess, final ForwardPaymentStateType type, final String statusCode,
            final String statusMessage, final String requestBody, final String responseBody) {
        this.invocationSuccess = invocationSuccess;
        this.stateType = type;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
    }

    public void editTransactionDetails(final String transactionId, final DateTime transactionDate, final BigDecimal payedAmount) {
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.payedAmount = payedAmount;
    }

    public void editAuthorizationDetails(final String authorizationNumber, final DateTime authorizationDate) {
        this.authorizationNumber = authorizationNumber;
        this.authorizationDate = authorizationDate;
    }

    public boolean isInPayedState() {
        return getStateType() != null && getStateType().isPayed();
    }
    
    public boolean isAbleToRegisterPostPayment(final ForwardPayment forwardPayment) {
        return (forwardPayment.getCurrentState().isInStateToPostProcessPayment() || forwardPayment.getCurrentState().isRequested()) 
                && getStateType() != null && getStateType().isPayed();
    }
    
    public void defineSibsOnlinePaymentBrands(final String paymentBrands) {
        this.sibsOnlinePaymentBrands = paymentBrands;
    }
    
    // @formatter:off
    /* *****************
     * GETTERS & SETTERS
     * *****************
     */
    // @formatter:on

    public boolean isInvocationSuccess() {
        return invocationSuccess;
    }

    public ForwardPaymentStateType getStateType() {
        return stateType;
    }

    public String getAuthorizationNumber() {
        return authorizationNumber;
    }

    public DateTime getAuthorizationDate() {
        return authorizationDate;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public DateTime getTransactionDate() {
        return transactionDate;
    }

    public BigDecimal getPayedAmount() {
        return payedAmount;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
    
    public String getSibsOnlinePaymentBrands() {
        return sibsOnlinePaymentBrands;
    }

}
