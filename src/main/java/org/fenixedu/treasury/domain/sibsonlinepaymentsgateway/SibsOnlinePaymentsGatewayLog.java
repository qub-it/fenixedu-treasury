package org.fenixedu.treasury.domain.sibsonlinepaymentsgateway;

import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.joda.time.DateTime;

import com.google.common.base.Strings;

import pt.ist.fenixframework.FenixFramework;

public class SibsOnlinePaymentsGatewayLog extends SibsOnlinePaymentsGatewayLog_Base {

    public static final String REQUEST_PAYMENT_CODE = "REQUEST_PAYMENT_CODE";
    public static final String ONLINE_PAYMENT_PREPARE_CHECKOUT = "ONLINE_PAYMENT_PREPARE_CHECKOUT";

    public SibsOnlinePaymentsGatewayLog() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

    protected SibsOnlinePaymentsGatewayLog(final SibsOnlinePaymentsGateway sibsOnlinePaymentsGateway, final String operationCode,
            final DebtAccount debtAccount) {
        this();

        setCreationDate(new DateTime());
        setResponsibleUsername(TreasuryPlataformDependentServicesFactory.implementation().getLoggedUsername());

        setSibsOnlinePaymentsGateway(sibsOnlinePaymentsGateway);
        setOperationCode(operationCode);

        setCustomerFiscalNumber(debtAccount.getCustomer().getUiFiscalNumber());
        setCustomerName(debtAccount.getCustomer().getName());
        setCustomerBusinessIdentification(debtAccount.getCustomer().getBusinessIdentification());

        checkRules();
    }

    protected SibsOnlinePaymentsGatewayLog(final SibsOnlinePaymentsGateway sibsOnlinePaymentsGateway, final String operationCode,
            final ForwardPayment forwardPayment) {

        this();

        setCreationDate(new DateTime());
        setResponsibleUsername(TreasuryPlataformDependentServicesFactory.implementation().getLoggedUsername());

        setSibsOnlinePaymentsGateway(sibsOnlinePaymentsGateway);
        setOperationCode(operationCode);

        setCustomerFiscalNumber(forwardPayment.getDebtAccount().getCustomer().getUiFiscalNumber());
        setCustomerName(forwardPayment.getDebtAccount().getCustomer().getName());
        setCustomerBusinessIdentification(forwardPayment.getDebtAccount().getCustomer().getBusinessIdentification());

        setForwardPaymentOrderNumber(String.valueOf(forwardPayment.getOrderNumber()));
        
        checkRules();
    }

    private void checkRules() {
        if (getDomainRoot() == null) {
            throw new TreasuryDomainException("error.SibsOnlinePaymentsGatewayLog.domainRoot.required");
        }

        if (getSibsOnlinePaymentsGateway() == null) {
            throw new TreasuryDomainException("error.SibsOnlinePaymentsGatewayLog.sibsOnlinePaymentsGateway.required");
        }

        if (Strings.isNullOrEmpty(getOperationCode())) {
            throw new TreasuryDomainException("error.SibsOnlinePaymentsGatewayLog.operationCode.required");
        }
    }

    public void logRequestSendDate() {
        setRequestSendDate(new DateTime());
    }

    public void logRequestReceiveDateAndData(final String transactionId, final boolean operationSuccess,
            final boolean transactionPaid) {
        setRequestReceiveDate(new DateTime());
        setTransactionId(transactionId);
        setOperationSuccess(operationSuccess);
        setTransactionPaid(transactionPaid);
    }

    public void saveRequestAndResponsePayload(final String requestPayload, final String responsePayload) {
        setRequestPayload(requestPayload);
        setResponsePayload(responsePayload);
    }

    public void markExceptionOccuredAndSaveLog(final String exceptionLog) {
        setExceptionOccured(true);
        setExceptionLog(exceptionLog);
    }

    public void savePaymentCode(final String paymentCode) {
        setPaymentCode(paymentCode);
    }

    public void saveMerchantTransactionId(final String merchantTransactionId) {
        setMerchantTransactionId(merchantTransactionId);
    }

    public static SibsOnlinePaymentsGatewayLog createLogForRequestPaymentCode(
            final SibsOnlinePaymentsGateway sibsOnlinePaymentsGateway, final DebtAccount debtAccount) {
        return new SibsOnlinePaymentsGatewayLog(sibsOnlinePaymentsGateway, REQUEST_PAYMENT_CODE, debtAccount);
    }

    public static SibsOnlinePaymentsGatewayLog createLogForOnlinePaymentPrepareCheckout(
            final SibsOnlinePaymentsGateway sibsOnlinePaymentsGateway, final ForwardPayment forwardPayment) {
        return new SibsOnlinePaymentsGatewayLog(sibsOnlinePaymentsGateway, ONLINE_PAYMENT_PREPARE_CHECKOUT, forwardPayment);
    }

}
