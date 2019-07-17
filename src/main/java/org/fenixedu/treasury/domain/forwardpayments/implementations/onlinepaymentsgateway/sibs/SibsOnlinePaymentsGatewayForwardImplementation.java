package org.fenixedu.treasury.domain.forwardpayments.implementations.onlinepaymentsgateway.sibs;

import java.math.BigDecimal;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.fenixedu.onlinepaymentsgateway.api.CheckoutResultBean;
import org.fenixedu.onlinepaymentsgateway.api.PaymentStateBean;
import org.fenixedu.onlinepaymentsgateway.api.PrepareCheckoutInputBean;
import org.fenixedu.onlinepaymentsgateway.api.SIBSOnlinePaymentsGatewayService;
import org.fenixedu.onlinepaymentsgateway.exceptions.OnlinePaymentsGatewayCommunicationException;
import org.fenixedu.onlinepaymentsgateway.sibs.sdk.SibsResultCodeType;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPaymentStateType;
import org.fenixedu.treasury.domain.forwardpayments.implementations.IForwardPaymentImplementation;
import org.fenixedu.treasury.domain.forwardpayments.implementations.PostProcessPaymentStatusBean;
import org.fenixedu.treasury.domain.sibsonlinepaymentsgateway.SibsOnlinePaymentsGateway;
import org.fenixedu.treasury.domain.sibsonlinepaymentsgateway.SibsOnlinePaymentsGatewayLog;
import org.fenixedu.treasury.dto.forwardpayments.ForwardPaymentStatusBean;
import org.fenixedu.treasury.ui.document.forwardpayments.IForwardPaymentController;
import org.fenixedu.treasury.ui.document.forwardpayments.implementations.onlinepaymentsgateway.sibs.SibsOnlinePaymentsGatewayForwardPaymentController;
import org.joda.time.DateTime;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.Atomic.TxMode;

public class SibsOnlinePaymentsGatewayForwardImplementation implements IForwardPaymentImplementation {

    public static final String ONLINE_PAYMENTS_GATEWAY = "ONLINE-PAYMENTS-GATEWAY";

    @Override
    public IForwardPaymentController getForwardPaymentController(ForwardPayment forwardPayment) {
        return new SibsOnlinePaymentsGatewayForwardPaymentController();
    }

    @Override
    public String getPaymentURL(ForwardPayment forwardPayment) {
        throw new RuntimeException("not applied");
    }

    @Override
    public String getFormattedAmount(ForwardPayment forwardPayment) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLogosJspPage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getWarningBeforeRedirectionJspPage() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getReturnURL(final ForwardPayment forwardPayment) {
        return String.format("%s%s/%s", forwardPayment.getForwardPaymentConfiguration().getReturnURL(),
                SibsOnlinePaymentsGatewayForwardPaymentController.RETURN_FORWARD_PAYMENT_URL, forwardPayment.getExternalId());
    }

    @Atomic(mode = TxMode.READ)
    public ForwardPaymentStatusBean prepareCheckout(final ForwardPayment forwardPayment) {
        final SibsOnlinePaymentsGateway gateway = forwardPayment.getForwardPaymentConfiguration().getSibsOnlinePaymentsGateway();

        final String merchantTransactionId = gateway.generateNewMerchantTransactionId();

        FenixFramework.atomic(() -> {
            forwardPayment.setSibsMerchantTransactionId(merchantTransactionId);
        });
        
        try {
            final CheckoutResultBean checkoutBean =
                    gateway.prepareCheckout(merchantTransactionId, forwardPayment.getAmount(), getReturnURL(forwardPayment));

            final ForwardPaymentStateType type = translateForwardPaymentStateType(checkoutBean.getOperationResultType(), false);
            final ForwardPaymentStatusBean result = new ForwardPaymentStatusBean(checkoutBean.isOperationSuccess(), type,
                    checkoutBean.getPaymentGatewayResultCode(), checkoutBean.getPaymentGatewayResultDescription(),
                    checkoutBean.getRequestLog(), checkoutBean.getResponseLog());

            FenixFramework.atomic(() -> {
                forwardPayment.setSibsCheckoutId(checkoutBean.getCheckoutId());
            });

            FenixFramework.atomic(() -> {
                if (!result.isInvocationSuccess() || (result.getStateType() == ForwardPaymentStateType.REJECTED)) {
                        forwardPayment.reject(checkoutBean.getPaymentGatewayResultCode(),
                                checkoutBean.getPaymentGatewayResultDescription(), checkoutBean.getRequestLog(),
                                checkoutBean.getResponseLog());
                } else {
                        forwardPayment.advanceToRequestState(checkoutBean.getPaymentGatewayResultCode(),
                                checkoutBean.getPaymentGatewayResultDescription(), checkoutBean.getRequestLog(),
                                checkoutBean.getResponseLog());
                }
            });

            result.defineSibsOnlinePaymentBrands(checkoutBean.getPaymentBrands());

            return result;

        } catch (final Exception e) {

            FenixFramework.atomic(() -> {
                String requestBody = null;
                String responseBody = null;
                
                if (e instanceof OnlinePaymentsGatewayCommunicationException) {
                    requestBody = ((OnlinePaymentsGatewayCommunicationException) e).getRequestLog();
                    responseBody = ((OnlinePaymentsGatewayCommunicationException) e).getResponseLog();
                }
                
                forwardPayment.logException(e, requestBody, responseBody);
            });

            throw new TreasuryDomainException(e,
                    "error.SibsOnlinePaymentsGateway.getPaymentStatusBySibsTransactionId.communication.error");
        }

    }

    public ForwardPaymentStatusBean paymentStatusByCheckoutId(final ForwardPayment forwardPayment) {
        final SibsOnlinePaymentsGateway gateway = forwardPayment.getForwardPaymentConfiguration().getSibsOnlinePaymentsGateway();

        try {
            PaymentStateBean paymentStateBean = gateway.getPaymentStatusBySibsCheckoutId(forwardPayment.getSibsCheckoutId());

            final String requestLog = paymentStateBean.getRequestLog();
            final String responseLog = paymentStateBean.getResponseLog();

            final ForwardPaymentStateType type =
                    translateForwardPaymentStateType(paymentStateBean.getOperationResultType(), paymentStateBean.isPaid());

            final ForwardPaymentStatusBean bean = new ForwardPaymentStatusBean(paymentStateBean.isOperationSuccess(), type,
                    paymentStateBean.getPaymentGatewayResultCode(), paymentStateBean.getPaymentGatewayResultDescription(), 
                    requestLog, responseLog);

            bean.editTransactionDetails(paymentStateBean.getTransactionId(), paymentStateBean.getPaymentDate(),
                    paymentStateBean.getAmount());

            return bean;
        } catch (final Exception e) {
            FenixFramework.atomic(() -> {
                String requestBody = null;
                String responseBody = null;
                
                if (e instanceof OnlinePaymentsGatewayCommunicationException) {
                    requestBody = ((OnlinePaymentsGatewayCommunicationException) e).getRequestLog();
                    responseBody = ((OnlinePaymentsGatewayCommunicationException) e).getResponseLog();
                }
                
                forwardPayment.logException(e, requestBody, responseBody);
            });

            throw new TreasuryDomainException(e,
                    "error.SibsOnlinePaymentsGateway.getPaymentStatusBySibsTransactionId.communication.error");
        }
    }

    @Override
    public ForwardPaymentStatusBean paymentStatus(final ForwardPayment forwardPayment) {
        final SibsOnlinePaymentsGateway gateway = forwardPayment.getForwardPaymentConfiguration().getSibsOnlinePaymentsGateway();

        try {
            
            PaymentStateBean paymentStateBean;
            if(!Strings.isNullOrEmpty(forwardPayment.getSibsTransactionId())) {
                paymentStateBean = gateway.getPaymentStatusBySibsTransactionId(forwardPayment.getSibsTransactionId());
            } else {
                paymentStateBean = gateway.getPaymentStatusBySibsMerchantId(forwardPayment.getSibsMerchantTransactionId());
            }

            final String requestLog = paymentStateBean.getRequestLog();
            final String responseLog = paymentStateBean.getResponseLog();

            final ForwardPaymentStateType type =
                    translateForwardPaymentStateType(paymentStateBean.getOperationResultType(), paymentStateBean.isPaid());

            final ForwardPaymentStatusBean bean = new ForwardPaymentStatusBean(paymentStateBean.isOperationSuccess(), type,
                    paymentStateBean.getPaymentGatewayResultCode(), paymentStateBean.getPaymentGatewayResultDescription(), requestLog,
                    responseLog);

            bean.editTransactionDetails(paymentStateBean.getTransactionId(), paymentStateBean.getPaymentDate(),
                    paymentStateBean.getAmount());

            return bean;
        } catch (final Exception e) {

            FenixFramework.atomic(() -> {
                String requestBody = null;
                String responseBody = null;
                
                if (e instanceof OnlinePaymentsGatewayCommunicationException) {
                    requestBody = ((OnlinePaymentsGatewayCommunicationException) e).getRequestLog();
                    responseBody = ((OnlinePaymentsGatewayCommunicationException) e).getResponseLog();
                }
                
                forwardPayment.logException(e, requestBody, responseBody);
            });

            throw new TreasuryDomainException(e,
                    "error.SibsOnlinePaymentsGateway.getPaymentStatusBySibsTransactionId.communication.error");
        }
    }

    private ForwardPaymentStateType translateForwardPaymentStateType(final SibsResultCodeType operationResultType,
            final boolean paid) {

        if (operationResultType == null) {
            throw new TreasuryDomainException("error.SibsOnlinePaymentsGatewayForwardImplementation.unknown.payment.state");
        }

        if(paid) {
            if(operationResultType != SibsResultCodeType.SUCCESSFUL_TRANSACTION
                    && operationResultType != SibsResultCodeType.SUCESSFUL_PROCESSED_TRANSACTION_FOR_REVIEW) {
                throw new TreasuryDomainException("error.SibsOnlinePaymentsGatewayForwardImplementation.payment.appears.paid.but.inconsistent.with.result.code");
            }
            
            return ForwardPaymentStateType.PAYED;
        } else if (operationResultType == SibsResultCodeType.PENDING_TRANSACTION) {
            return ForwardPaymentStateType.REQUESTED;
        }

        return ForwardPaymentStateType.REJECTED;
    }

    @Override
    public PostProcessPaymentStatusBean postProcessPayment(ForwardPayment forwardPayment, String justification) {
        
        final ForwardPaymentStateType previousState = forwardPayment.getCurrentState();

        final ForwardPaymentStatusBean paymentStatusBean =
                forwardPayment.getForwardPaymentConfiguration().implementation().paymentStatus(forwardPayment);

        if (!forwardPayment.getCurrentState().isInStateToPostProcessPayment()) {
            throw new TreasuryDomainException("error.ManageForwardPayments.forwardPayment.not.created.nor.requested",
                    String.valueOf(forwardPayment.getOrderNumber()));
        }
        

        if (Strings.isNullOrEmpty(justification)) {
            throw new TreasuryDomainException("label.ManageForwardPayments.postProcessPayment.justification.required");
        }

        if(Lists.newArrayList(ForwardPaymentStateType.CREATED, ForwardPaymentStateType.REQUESTED).contains(paymentStatusBean.getStateType())) {
            // Do nothing
            return new PostProcessPaymentStatusBean(paymentStatusBean, previousState, false);
        }
        
        final boolean success = paymentStatusBean.isInPayedState();

        if(!paymentStatusBean.isInvocationSuccess()) {
            throw new TreasuryDomainException("error.ManageForwardPayments.postProcessPayment.invocation.unsucessful", 
                    String.valueOf(forwardPayment.getOrderNumber()));
        }
        
        if (success) {
            FenixFramework.atomic(() -> {
                forwardPayment.advanceToPayedState(paymentStatusBean.getStatusCode(), paymentStatusBean.getStatusMessage(),
                        paymentStatusBean.getPayedAmount(), paymentStatusBean.getTransactionDate(), paymentStatusBean.getTransactionId(),
                        paymentStatusBean.getAuthorizationNumber(), paymentStatusBean.getRequestBody(),
                        paymentStatusBean.getResponseBody(), justification);
            });

        } else {
            FenixFramework.atomic(() -> {
                forwardPayment.reject(paymentStatusBean.getStatusCode(), paymentStatusBean.getStatusMessage(), 
                        paymentStatusBean.getRequestBody(), paymentStatusBean.getResponseBody());
            });
        }

        return new PostProcessPaymentStatusBean(paymentStatusBean, previousState, true);
    }

    @Override
    public String getImplementationCode() {
        return ONLINE_PAYMENTS_GATEWAY;
    }

}
