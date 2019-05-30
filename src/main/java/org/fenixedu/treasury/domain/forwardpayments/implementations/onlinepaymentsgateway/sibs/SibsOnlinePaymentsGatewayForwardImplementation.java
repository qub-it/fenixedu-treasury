package org.fenixedu.treasury.domain.forwardpayments.implementations.onlinepaymentsgateway.sibs;

import java.math.BigDecimal;

import org.fenixedu.onlinepaymentsgateway.api.CheckoutResultBean;
import org.fenixedu.onlinepaymentsgateway.api.PaymentStatusBean;
import org.fenixedu.onlinepaymentsgateway.api.PrepareCheckoutInputBean;
import org.fenixedu.onlinepaymentsgateway.api.SIBSOnlinePaymentsGatewayService;
import org.fenixedu.onlinepaymentsgateway.sibs.sdk.SibsResultCodeType;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPaymentStateType;
import org.fenixedu.treasury.domain.forwardpayments.implementations.IForwardPaymentImplementation;
import org.fenixedu.treasury.domain.forwardpayments.implementations.PostProcessPaymentStatusBean;
import org.fenixedu.treasury.domain.sibsonlinepaymentsgateway.SibsOnlinePaymentsGateway;
import org.fenixedu.treasury.dto.forwardpayments.ForwardPaymentStatusBean;
import org.fenixedu.treasury.ui.document.forwardpayments.IForwardPaymentController;
import org.fenixedu.treasury.ui.document.forwardpayments.implementations.onlinepaymentsgateway.sibs.SibsOnlinePaymentsGatewayForwardPaymentController;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.Atomic.TxMode;

public class SibsOnlinePaymentsGatewayForwardImplementation implements IForwardPaymentImplementation {

    @Override
    public IForwardPaymentController getForwardPaymentController(ForwardPayment forwardPayment) {
        return new SibsOnlinePaymentsGatewayForwardPaymentController();
    }

    @Override
    public String getPaymentURL(ForwardPayment forwardPayment) {
        // TODO Auto-generated method stub
        return null;
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

    @Atomic(mode = TxMode.READ)
    public ForwardPaymentStatusBean prepareCheckout(final ForwardPayment forwardPayment) {
        final SibsOnlinePaymentsGateway gateway = forwardPayment.getForwardPaymentConfiguration().getSibsOnlinePaymentsGateway();

        try {

        final CheckoutResultBean checkoutBean =
                gateway.prepareCheckout(forwardPayment.getAmount(), gateway.getForwardPaymentConfiguration().getReturnURL());

        final ForwardPaymentStateType type =
                translateForwardPaymentStateType(checkoutBean.getOperationResultType(), false /* resultBean.isPaid() */);
        final ForwardPaymentStatusBean result =
                new ForwardPaymentStatusBean(checkoutBean.isOperationSuccess(), type, checkoutBean.getPaymentGatewayResultCode(),
                        checkoutBean.getPaymentGatewayResultDescription(), checkoutBean.getRequestLog(), checkoutBean.getResponseLog());

        if (!result.isInvocationSuccess() || (result.getStateType() == ForwardPaymentStateType.REJECTED)) {
            FenixFramework.atomic(() -> {
                forwardPayment.reject(checkoutBean.getPaymentGatewayResultCode(), checkoutBean.getPaymentGatewayResultDescription(),
                        checkoutBean.getResponseLog(), checkoutBean.getResponseLog());
            });
            
            return result;
        }

        FenixFramework.atomic(() -> {
            forwardPayment.setSibsMerchantTransactionId(checkoutBean.getMerchantTransactionId());
            forwardPayment.setSibsTransactionId(checkoutBean.getId());
            forwardPayment.advanceToRequestState(checkoutBean.getPaymentGatewayResultCode(), checkoutBean.getPaymentGatewayResultDescription(),
                    checkoutBean.getResponseLog(), checkoutBean.getResponseLog());
        });
        
        return result;
        
        } catch (OnlinePaymentsGatewayCommunicationException e) {
            throw new TreasuryDomainException("error.SibsOnlinePaymentsGateway.getPaymentStatusBySibsTransactionId.communication.error");
        }

    }

    @Override
    public ForwardPaymentStatusBean paymentStatus(final ForwardPayment forwardPayment) {
        SibsOnlinePaymentsGateway sibsOnlinePaymentsGateway =
                forwardPayment.getForwardPaymentConfiguration().getSibsOnlinePaymentsGateway();
        PaymentStatusBean paymentStatusBean =
                sibsOnlinePaymentsGateway.getPaymentStatusBySibsTransactionId(forwardPayment.getTransactionId());

        final String requestLog = paymentStatusBean.getRequestLog();
        final String responseLog = paymentStatusBean.getResponseLog();

        ForwardPaymentStateType type = translateForwardPaymentStateType(paymentStatusBean.getOperationResultType(),
                false /* paymentStatusBean.isPaid() */);
        ForwardPaymentStatusBean bean = new ForwardPaymentStatusBean(paymentStatusBean.isOperationSuccess(), type,
                paymentStatusBean.getPaymentGatewayResultCode(), paymentStatusBean.getPaymentGatewayResultDescription(),
                requestLog, responseLog);

        bean.editTransactionDetails(paymentStatusBean.getId(), bean.getTransactionDate(), bean.getPayedAmount());

        return bean;
    }

    private ForwardPaymentStateType translateForwardPaymentStateType(final SibsResultCodeType operationResultType,
            final boolean isPaid) {

        if (operationResultType == null) {
            throw new TreasuryDomainException("error.SibsOnlinePaymentsGatewayForwardImplementation.unknown.payment.state");
        }

        if (operationResultType == SibsResultCodeType.PENDING_TRANSACTION) {
            return ForwardPaymentStateType.REQUESTED;
        } else if (operationResultType == SibsResultCodeType.SUCCESSFUL_TRANSACTION
                || operationResultType == SibsResultCodeType.SUCESSFUL_PROCESSED_TRANSACTION_FOR_REVIEW) {
            return ForwardPaymentStateType.PAYED;

//            if(paymentStatusBean.isPaid()) {
//                return ForwardPaymentStateType.PAYED;
//            } else {
//                throw new TreasuryDomainException("error.SibsOnlinePaymentsGatewayForwardImplementation.payment.appears.to.be.successful.but.not.marked.as.paid");
//            }
        }

        return ForwardPaymentStateType.REJECTED;
    }

    @Override
    public PostProcessPaymentStatusBean postProcessPayment(ForwardPayment forwardPayment, String justification) {
        // TODO Auto-generated method stub
        return null;
    }

}
