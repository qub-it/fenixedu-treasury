package org.fenixedu.treasury.domain.sibsonlinepaymentsgateway;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.onlinepaymentsgateway.api.CheckoutResultBean;
import org.fenixedu.onlinepaymentsgateway.api.CustomerDataInputBean;
import org.fenixedu.onlinepaymentsgateway.api.MbCheckoutResultBean;
import org.fenixedu.onlinepaymentsgateway.api.MbPrepareCheckoutInputBean;
import org.fenixedu.onlinepaymentsgateway.api.OnlinePaymentServiceFactory;
import org.fenixedu.onlinepaymentsgateway.api.PaymentStateBean;
import org.fenixedu.onlinepaymentsgateway.api.PrepareCheckoutInputBean;
import org.fenixedu.onlinepaymentsgateway.api.SIBSInitializeServiceBean;
import org.fenixedu.onlinepaymentsgateway.api.SIBSOnlinePaymentsGatewayService;
import org.fenixedu.onlinepaymentsgateway.exceptions.OnlinePaymentsGatewayCommunicationException;
import org.fenixedu.onlinepaymentsgateway.sibs.sdk.SibsEnvironmentMode;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.common.base.Strings;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.FenixFramework;

public class SibsOnlinePaymentsGateway extends SibsOnlinePaymentsGateway_Base {

    public SibsOnlinePaymentsGateway() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

    protected SibsOnlinePaymentsGateway(PaymentCodePool paymentCodePool, String sibsEntityId, String sibsEndpointUrl,
            String merchantTransactionIdPrefix, final String bearerToken) {
        this();

        setPaymentCodePool(paymentCodePool);
        setSibsEntityId(sibsEntityId);
        setSibsEndpointUrl(sibsEndpointUrl);
        setMerchantTransactionIdPrefix(merchantTransactionIdPrefix);
        setBearerToken(bearerToken);

        checkRules();
    }

    private void checkRules() {

        if (getDomainRoot() == null) {
            throw new TreasuryDomainException("error.SibsOnlinePaymentsGateway.domainRoot.required");
        }

        if (Strings.isNullOrEmpty(getSibsEntityId())) {
            throw new TreasuryDomainException("error.SibsOnlinePaymentsGateway.sibsEntityId.required");
        }

        if (Strings.isNullOrEmpty(getSibsEndpointUrl())) {
            throw new TreasuryDomainException("error.SibsOnlinePaymentsGateway.sibsEndpointUrl.required");
        }

        if (Strings.isNullOrEmpty(getMerchantTransactionIdPrefix())) {
            throw new TreasuryDomainException("error.SibsOnlinePaymentsGateway.merchantTransactionIdPrefix.required");
        }
    }

    @Atomic(mode = TxMode.WRITE)
    public String generateNewMerchantTransactionId() {
        final long value = incrementAndGetMerchantTransactionIdCounter();

        return String.format("%s-%s-%s", getMerchantTransactionIdPrefix(), StringUtils.leftPad(String.valueOf(value), 9, '0'),
                DateTime.now().toString("yyyyMMddHHmmss"));
    }

    private long incrementAndGetMerchantTransactionIdCounter() {
        setMerchantTransactionIdCounter(getMerchantTransactionIdCounter() + 1);
        return getMerchantTransactionIdCounter();
    }

    @Atomic(mode = TxMode.READ)
    public PaymentStateBean getPaymentStatusBySibsCheckoutId(final String checkoutId)
            throws OnlinePaymentsGatewayCommunicationException {
        final SIBSOnlinePaymentsGatewayService gatewayService = gatewayService();

        try {
            return gatewayService.getPaymentStatusByCheckoutId(checkoutId);
        } catch (OnlinePaymentsGatewayCommunicationException e) {
            throw new TreasuryDomainException(e,
                    "error.SibsOnlinePaymentsGateway.getPaymentStatusBySibsTransactionId.communication.error");
        }
    }

    @Atomic(mode = TxMode.READ)
    public PaymentStateBean getPaymentStatusBySibsTransactionId(final String transactionId)
            throws OnlinePaymentsGatewayCommunicationException {
        final SIBSOnlinePaymentsGatewayService gatewayService = gatewayService();

        return gatewayService.getPaymentTransactionReportByTransactionId(transactionId);
    }

    @Atomic(mode = TxMode.READ)
    public PaymentStateBean getPaymentStatusBySibsMerchantId(final String merchantId)
            throws OnlinePaymentsGatewayCommunicationException {
        final SIBSOnlinePaymentsGatewayService gatewayService = gatewayService();

        return gatewayService.getPaymentTransactionReportByMerchantId(merchantId);
    }

    @Atomic(mode = TxMode.READ)
    public CheckoutResultBean prepareCheckout(final String merchantTransactionId, final BigDecimal amount, final String returnUrl)
            throws OnlinePaymentsGatewayCommunicationException {
        final SIBSOnlinePaymentsGatewayService gatewayService = gatewayService();

        final PrepareCheckoutInputBean bean = new PrepareCheckoutInputBean(amount, merchantTransactionId, returnUrl,
                new DateTime(), new DateTime().plusDays(7));

        bean.setUseCreditCard(true);

        CheckoutResultBean resultBean = gatewayService.prepareOnlinePaymentCheckout(bean);

        return resultBean;
    }

    @Atomic(mode = TxMode.READ)
    public MbCheckoutResultBean generateMBPaymentReference(final BigDecimal amount, final DateTime validFrom, final DateTime validTo, final String merchantTransactionId) throws OnlinePaymentsGatewayCommunicationException {
        final SIBSOnlinePaymentsGatewayService gatewayService = gatewayService();
        
        final MbPrepareCheckoutInputBean inputBean = new MbPrepareCheckoutInputBean(amount, merchantTransactionId,
                validFrom, validTo);
        
        // Customer data will not be sent due to GDPR
        final CustomerDataInputBean customerInputBean = null;

        final MbCheckoutResultBean requestResult = gatewayService.generateMBPaymentReference(inputBean, customerInputBean);
        
        return requestResult;
    }

    private SIBSOnlinePaymentsGatewayService gatewayService() {
        final SIBSInitializeServiceBean initializeServiceBean = new SIBSInitializeServiceBean(getSibsEntityId(), getBearerToken(),
                getSibsEndpointUrl(), getPaymentCodePool().getEntityReferenceCode(),
                getPaymentCodePool().getFinantialInstitution().getCurrency().getIsoCode(),
                SibsEnvironmentMode.TEST_MODE_EXTERNAL);

        final SIBSOnlinePaymentsGatewayService gatewayService =
                OnlinePaymentServiceFactory.createSIBSOnlinePaymentGatewayService(initializeServiceBean);

        return gatewayService;
    }

    /* ************/
    /* * SERVICES */
    /* ************/

    public static SibsOnlinePaymentsGateway create(final PaymentCodePool paymentCodePool, final String sibsEntityId,
            final String sibsEndpointUrl, final String merchantIdPrefix, final String bearerToken) {
        return new SibsOnlinePaymentsGateway(paymentCodePool, sibsEntityId, sibsEndpointUrl, merchantIdPrefix, bearerToken);
    }

    public static Stream<SibsOnlinePaymentsGateway> findAll() {
        return FenixFramework.getDomainRoot().getSibsOnlinePaymentsGatewaySet().stream();
    }

    public static Stream<SibsOnlinePaymentsGateway> findByMerchantIdPrefix(final String merchantIdPrefix) {
        return findAll().filter(e -> merchantIdPrefix.toLowerCase().equals(e.getMerchantTransactionIdPrefix().toLowerCase()));
    }

}
