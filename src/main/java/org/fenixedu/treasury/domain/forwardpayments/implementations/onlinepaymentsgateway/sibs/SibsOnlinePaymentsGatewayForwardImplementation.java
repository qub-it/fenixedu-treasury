package org.fenixedu.treasury.domain.forwardpayments.implementations.onlinepaymentsgateway.sibs;

import java.math.BigDecimal;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.fenixedu.onlinepaymentsgateway.api.CheckoutResultBean;
import org.fenixedu.onlinepaymentsgateway.api.PaymentStatusBean;
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

        final SibsOnlinePaymentsGatewayLog log = createLog(gateway, forwardPayment);
        
        try {

            final CheckoutResultBean checkoutBean =
                    gateway.prepareCheckout(forwardPayment.getAmount(), getReturnURL(forwardPayment));

            
            final ForwardPaymentStateType type = translateForwardPaymentStateType(checkoutBean.getOperationResultType(), false);
            final ForwardPaymentStatusBean result = new ForwardPaymentStatusBean(checkoutBean.isOperationSuccess(), type,
                    checkoutBean.getPaymentGatewayResultCode(), checkoutBean.getPaymentGatewayResultDescription(),
                    checkoutBean.getRequestLog(), checkoutBean.getResponseLog());

            FenixFramework.atomic(() -> {
                log.saveMerchantTransactionId(checkoutBean.getMerchantTransactionId());
                
                forwardPayment.setSibsMerchantTransactionId(checkoutBean.getMerchantTransactionId());
                forwardPayment.setSibsCheckoutId(checkoutBean.getId() /* getCheckoutId() */ );
            });
            
            if (!result.isInvocationSuccess() || (result.getStateType() == ForwardPaymentStateType.REJECTED)) {
                FenixFramework.atomic(() -> {
                    log.logRequestReceiveDateAndData(checkoutBean.getId(), false, false);
                    log.saveRequestAndResponsePayload(checkoutBean.getRequestLog(), checkoutBean.getResponseLog());
                    
                    forwardPayment.reject(checkoutBean.getPaymentGatewayResultCode(),
                            checkoutBean.getPaymentGatewayResultDescription(), checkoutBean.getRequestLog(),
                            checkoutBean.getResponseLog());
                });

            } else {
                FenixFramework.atomic(() -> {
                    log.logRequestReceiveDateAndData(checkoutBean.getId(), true, false);
                    log.saveRequestAndResponsePayload(checkoutBean.getRequestLog(), checkoutBean.getResponseLog());
                    
                    forwardPayment.advanceToRequestState(checkoutBean.getPaymentGatewayResultCode(),
                            checkoutBean.getPaymentGatewayResultDescription(), checkoutBean.getRequestLog(),
                            checkoutBean.getResponseLog());
                });
            }
            
            result.defineSibsOnlinePaymentBrands(checkoutBean.getPaymentBrands());

            return result;

        } catch (final Exception e) {
            final String exceptionLog = String.format("%s\n%s", ExceptionUtils.getFullStackTrace(e), e.getLocalizedMessage());

            FenixFramework.atomic(() -> {

                log.logRequestReceiveDateAndData(null, false, false);
                log.markExceptionOccuredAndSaveLog(exceptionLog);

                if (e instanceof OnlinePaymentsGatewayCommunicationException) {
                    log.saveRequestAndResponsePayload(((OnlinePaymentsGatewayCommunicationException) e).getRequestLog(),
                            ((OnlinePaymentsGatewayCommunicationException) e).getResponseLog());
                }
            });
            
            throw new TreasuryDomainException(
                    "error.SibsOnlinePaymentsGateway.getPaymentStatusBySibsTransactionId.communication.error");
        }

    }

    @Atomic(mode = TxMode.WRITE)
    private SibsOnlinePaymentsGatewayLog createLog(final SibsOnlinePaymentsGateway sibsGateway, final ForwardPayment forwardPayment) {
        return SibsOnlinePaymentsGatewayLog.createLogForOnlinePaymentPrepareCheckout(sibsGateway, forwardPayment);
    }
    
    public ForwardPaymentStatusBean checkoutPaymentStatus(final ForwardPayment forwardPayment) {
        SibsOnlinePaymentsGateway sibsOnlinePaymentsGateway =
                forwardPayment.getForwardPaymentConfiguration().getSibsOnlinePaymentsGateway();
        PaymentStatusBean paymentStatusBean =
                sibsOnlinePaymentsGateway.getPaymentStatusBySibsCheckoutId(forwardPayment.getSibsCheckoutId());

        final String requestLog = paymentStatusBean.getRequestLog();
        final String responseLog = paymentStatusBean.getResponseLog();

        final ForwardPaymentStateType type =
                translateForwardPaymentStateType(paymentStatusBean.getOperationResultType(), paymentStatusBean.isPaid());

        final ForwardPaymentStatusBean bean = new ForwardPaymentStatusBean(paymentStatusBean.isOperationSuccess(), type,
                paymentStatusBean.getPaymentGatewayResultCode(), paymentStatusBean.getPaymentGatewayResultDescription(),
                requestLog, responseLog);

        bean.editTransactionDetails(paymentStatusBean.getId() /* getTransactionId */, new DateTime(), paymentStatusBean.getPaymentAmount() != null ? new BigDecimal(paymentStatusBean.getPaymentAmount()) : null);

        return bean;
    }
    
    @Override
    public ForwardPaymentStatusBean paymentStatus(final ForwardPayment forwardPayment) {
        SibsOnlinePaymentsGateway sibsOnlinePaymentsGateway =
                forwardPayment.getForwardPaymentConfiguration().getSibsOnlinePaymentsGateway();
        PaymentStatusBean paymentStatusBean =
                sibsOnlinePaymentsGateway.getPaymentStatusBySibsCheckoutId(forwardPayment.getSibsTransactionId());

        final String requestLog = paymentStatusBean.getRequestLog();
        final String responseLog = paymentStatusBean.getResponseLog();

        final ForwardPaymentStateType type =
                translateForwardPaymentStateType(paymentStatusBean.getOperationResultType(), paymentStatusBean.isPaid());

        final ForwardPaymentStatusBean bean = new ForwardPaymentStatusBean(paymentStatusBean.isOperationSuccess(), type,
                paymentStatusBean.getPaymentGatewayResultCode(), paymentStatusBean.getPaymentGatewayResultDescription(),
                requestLog, responseLog);

        bean.editTransactionDetails(paymentStatusBean.getId(), new DateTime(), paymentStatusBean.getPaymentAmount() != null ? new BigDecimal(paymentStatusBean.getPaymentAmount()) : null);

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
