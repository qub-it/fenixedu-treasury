package org.fenixedu.treasury.domain.sibsonlinepaymentsgateway;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.onlinepaymentsgateway.api.CheckoutResultBean;
import org.fenixedu.onlinepaymentsgateway.api.CustomerDataInputBean;
import org.fenixedu.onlinepaymentsgateway.api.MbPrepareCheckoutInputBean;
import org.fenixedu.onlinepaymentsgateway.api.OnlinePaymentServiceFactory;
import org.fenixedu.onlinepaymentsgateway.api.PaymentStatusBean;
import org.fenixedu.onlinepaymentsgateway.api.PrepareCheckoutInputBean;
import org.fenixedu.onlinepaymentsgateway.api.SIBSInitializeServiceBean;
import org.fenixedu.onlinepaymentsgateway.api.SIBSOnlinePaymentsGatewayService;
import org.fenixedu.onlinepaymentsgateway.exceptions.OnlinePaymentsGatewayCommunicationException;
import org.fenixedu.onlinepaymentsgateway.sibs.sdk.TransactionReportBean;
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

        return String.format("%s-%s", getMerchantTransactionIdPrefix(), StringUtils.leftPad(String.valueOf(value), 9, '0'));
    }

    private long incrementAndGetMerchantTransactionIdCounter() {
        setMerchantTransactionIdCounter(getMerchantTransactionIdCounter() + 1);
        return getMerchantTransactionIdCounter();
    }

    @Atomic(mode=TxMode.READ)
    public PaymentStatusBean getPaymentStatusBySibsCheckoutId(final String checkoutId) {
        final SIBSOnlinePaymentsGatewayService gatewayService = gatewayService();
        
        try {
            return gatewayService.getPaymentStatus(checkoutId);
        } catch (OnlinePaymentsGatewayCommunicationException e) {
            throw new TreasuryDomainException(e, "error.SibsOnlinePaymentsGateway.getPaymentStatusBySibsTransactionId.communication.error");
        }
    }

    @Atomic(mode=TxMode.READ)
    public TransactionReportBean getPaymentStatusBySibsTransactionId(final String transactionId) {
        final SIBSOnlinePaymentsGatewayService gatewayService = gatewayService();
        
        try {
            return gatewayService.getPaymentTransactionReport(transactionId);
        } catch (OnlinePaymentsGatewayCommunicationException e) {
            throw new TreasuryDomainException(e, "error.SibsOnlinePaymentsGateway.getPaymentStatusBySibsTransactionId.communication.error");
        }
    }

    @Atomic(mode=TxMode.READ)
    public TransactionReportBean getPaymentStatusBySibsMerchantId(final String merchantId) {
        final SIBSOnlinePaymentsGatewayService gatewayService = gatewayService();
        
        try {
            return gatewayService.getPaymentTransactionReport(merchantId);
        } catch (OnlinePaymentsGatewayCommunicationException e) {
            throw new TreasuryDomainException(e, "error.SibsOnlinePaymentsGateway.getPaymentStatusBySibsTransactionId.communication.error");
        }
    }

    @Atomic(mode=TxMode.READ)
    public CheckoutResultBean prepareCheckout(final BigDecimal amount, final String returnUrl) {
        final SIBSOnlinePaymentsGatewayService gatewayService = gatewayService();

        try {

            final PrepareCheckoutInputBean bean = new PrepareCheckoutInputBean(amount, generateNewMerchantTransactionId(), 
                    returnUrl, new DateTime(), new DateTime().plusDays(7));
            
            bean.setUseCreditCard(true);
            //prepareCheckoutInputBean.setUseMB(true);
            //bean.setUseMBway(true);
            
            CheckoutResultBean resultBean = gatewayService.prepareOnlinePaymentCheckout(bean);

            return resultBean;
        } catch (OnlinePaymentsGatewayCommunicationException e) {
            // Log
            throw new TreasuryDomainException("error.SibsOnlinePaymentsGateway.getPaymentStatusBySibsTransactionId.communication.error");
        }

    }

    private SIBSOnlinePaymentsGatewayService gatewayService() {
        SIBSInitializeServiceBean initializeServiceBean = new SIBSInitializeServiceBean(getSibsEntityId(), getSibsEndpointUrl(),
                getPaymentCodePool().getEntityReferenceCode(), getPaymentCodePool().getFinantialInstitution().getCurrency().getIsoCode());

        initializeServiceBean.setBearerToken(getBearerToken());
        SIBSOnlinePaymentsGatewayService gatewayService = OnlinePaymentServiceFactory.createSIBSOnlinePaymentGatewayService(initializeServiceBean);
        return gatewayService;
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on

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
