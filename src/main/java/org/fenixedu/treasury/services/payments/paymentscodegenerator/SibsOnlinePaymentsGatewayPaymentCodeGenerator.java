package org.fenixedu.treasury.services.payments.paymentscodegenerator;

import java.math.BigDecimal;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.fenixedu.onlinepaymentsgateway.api.CustomerDataInputBean;
import org.fenixedu.onlinepaymentsgateway.api.MbCheckoutResultBean;
import org.fenixedu.onlinepaymentsgateway.api.MbPrepareCheckoutInputBean;
import org.fenixedu.onlinepaymentsgateway.api.OnlinePaymentServiceFactory;
import org.fenixedu.onlinepaymentsgateway.api.SIBSInitializeServiceBean;
import org.fenixedu.onlinepaymentsgateway.api.SIBSOnlinePaymentsGatewayService;
import org.fenixedu.onlinepaymentsgateway.exceptions.OnlinePaymentsGatewayCommunicationException;
import org.fenixedu.treasury.domain.Currency;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCode;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCodeStateType;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;
import org.fenixedu.treasury.domain.sibsonlinepaymentsgateway.SibsOnlinePaymentsGateway;
import org.fenixedu.treasury.domain.sibsonlinepaymentsgateway.SibsOnlinePaymentsGatewayLog;
import org.fenixedu.treasury.dto.document.managepayments.PaymentReferenceCodeBean;
import org.joda.time.LocalDate;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.FenixFramework;

public class SibsOnlinePaymentsGatewayPaymentCodeGenerator implements IPaymentCodeGenerator {

    private PaymentCodePool paymentCodePool;

    public SibsOnlinePaymentsGatewayPaymentCodeGenerator(final PaymentCodePool paymentCodePool) {
        this.paymentCodePool = paymentCodePool;
    }

    @Override
    @Atomic(mode = TxMode.READ)
    public PaymentReferenceCode createPaymentReferenceCode(final DebtAccount debtAccount, final PaymentReferenceCodeBean bean) {

        final BigDecimal paymentAmount = bean.getPaymentAmount();

        final PaymentReferenceCode paymentReferenceCode = generateNewCodeFor(debtAccount, paymentAmount, new LocalDate(),
                new LocalDate().plusMonths(1) /* bean.getEndDate()*/);

        FenixFramework.atomic(() -> {
            paymentReferenceCode.createPaymentTargetTo(Sets.newHashSet(bean.getSelectedDebitEntries()), paymentAmount);
        });

        return paymentReferenceCode;
    }

    private PaymentReferenceCode generateNewCodeFor(final DebtAccount debtAccount, final BigDecimal amount, LocalDate validFrom,
            LocalDate validTo) {
        final SibsOnlinePaymentsGateway sibsGateway = this.paymentCodePool.getSibsOnlinePaymentsGateway();
        final String merchantId = sibsGateway.generateNewMerchantTransactionId();

        final SibsOnlinePaymentsGatewayLog log = createLog(sibsGateway, debtAccount);

        try {
            final Currency currency = this.paymentCodePool.getFinantialInstitution().getCurrency();

            SIBSInitializeServiceBean sibsInitializeServiceBean = new SIBSInitializeServiceBean(sibsGateway.getSibsEntityId(),
                    sibsGateway.getSibsEndpointUrl(), this.paymentCodePool.getEntityReferenceCode(), currency.getIsoCode());

            sibsInitializeServiceBean.setBearerToken(sibsGateway.getBearerToken());

            final MbPrepareCheckoutInputBean inputBean = new MbPrepareCheckoutInputBean(amount, null /* merchantId */,
                    validFrom.toDateTimeAtStartOfDay(), validTo.toDateTimeAtStartOfDay().plusDays(1).minusSeconds(1));
            // Customer data will not be sent due to GDPR
            final CustomerDataInputBean customerInputBean = null;

            final SIBSOnlinePaymentsGatewayService gatewayService =
                    OnlinePaymentServiceFactory.createSIBSOnlinePaymentGatewayService(sibsInitializeServiceBean);

            FenixFramework.atomic(() -> {
                log.saveMerchantTransactionId(merchantId);
                log.logRequestSendDate();
            });

            final MbCheckoutResultBean requestResult = gatewayService.generateMBPaymentReference(inputBean, customerInputBean);

            FenixFramework.atomic(() -> {
                log.logRequestReceiveDateAndData(requestResult.getId(), requestResult.isOperationSuccess(),
                        true /*  requestResult.isPaid() */ );
                log.saveRequestAndResponsePayload(requestResult.getRequestLog(), requestResult.getResponseLog());

            });

            if (!requestResult.isOperationSuccess()) {
                throw new TreasuryDomainException(
                        "error.SibsOnlinePaymentsGatewayPaymentCodeGenerator.generateNewCodeFor.request.not.successful");
            }

            final String paymentCode = requestResult.getPaymentReference();

            if (Strings.isNullOrEmpty(paymentCode)) {
                throw new TreasuryDomainException(
                        "error.SibsOnlinePaymentsGatewayPaymentCodeGenerator.generateNewCodeFor.reference.not.empty");
            }

            return createPaymentReferenceCode(amount, validFrom, validTo, log, paymentCode);

        } catch (final Exception e) {
            e.printStackTrace();

            final String exceptionLog = String.format("%s\n%s", ExceptionUtils.getFullStackTrace(e), e.getLocalizedMessage());

            FenixFramework.atomic(() -> {

                log.logRequestReceiveDateAndData(null, false, false);
                log.markExceptionOccuredAndSaveLog(exceptionLog);

                if (e instanceof OnlinePaymentsGatewayCommunicationException) {
                    log.saveRequestAndResponsePayload(((OnlinePaymentsGatewayCommunicationException) e).getRequestLog(),
                            ((OnlinePaymentsGatewayCommunicationException) e).getResponseLog());
                }
                
            });

            if (e instanceof OnlinePaymentsGatewayCommunicationException) {
                throw new TreasuryDomainException(
                        "error.SibsOnlinePaymentsGatewayPaymentCodeGenerator.generateNewCodeFor.gateway.communication");
            } else {
                throw new TreasuryDomainException(
                        "error.SibsOnlinePaymentsGatewayPaymentCodeGenerator.generateNewCodeFor.unknown");
            }
        }

    }

    @Atomic(mode = TxMode.WRITE)
    private PaymentReferenceCode createPaymentReferenceCode(final BigDecimal amount, final LocalDate validFrom,
            final LocalDate validTo, final SibsOnlinePaymentsGatewayLog log, final String paymentCode) throws Exception {
        log.savePaymentCode(paymentCode);

        return PaymentReferenceCode.create(paymentCode, validFrom, validTo, PaymentReferenceCodeStateType.USED,
                SibsOnlinePaymentsGatewayPaymentCodeGenerator.this.paymentCodePool, amount, amount);
    }

    @Atomic(mode = TxMode.WRITE)
    private SibsOnlinePaymentsGatewayLog createLog(final SibsOnlinePaymentsGateway sibsGateway, final DebtAccount debtAccount) {
        return SibsOnlinePaymentsGatewayLog.createLogForRequestPaymentCode(sibsGateway, debtAccount);
    }

    @Override
    public PaymentCodePool getReferenceCodePool() {
        return this.paymentCodePool;
    }

}
