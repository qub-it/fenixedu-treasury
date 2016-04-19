package org.fenixedu.treasury.domain.forwardpayments;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.Series;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.forwardpayments.implementations.IForwardPaymentImplementation;
import org.fenixedu.treasury.domain.forwardpayments.implementations.PaylineImplementation;
import org.fenixedu.treasury.domain.forwardpayments.implementations.TPAVirtualImplementation;
import org.fenixedu.treasury.ui.document.forwardpayments.IForwardPaymentController;

public class ForwardPaymentConfiguration extends ForwardPaymentConfiguration_Base {

    private ForwardPaymentConfiguration() {
        super();
        setBennu(Bennu.getInstance());
    }

    private ForwardPaymentConfiguration(final FinantialInstitution finantialInstitution, final Series series,
            final String paymentURL, final String returnURL, final String virtualTPAmerchantId, final String virtualTPAId) {
        this();

        setFinantialInstitution(finantialInstitution);
        setSeries(series);
        setPaymentURL(paymentURL);
        setReturnURL(returnURL);
        setVirtualTPAMerchantId(virtualTPAmerchantId);
        setVirtualTPAId(virtualTPAId);
        setImplementation(TPAVirtualImplementation.class.getName());

        checkRules();
    }

    private ForwardPaymentConfiguration(final FinantialInstitution finantialInstitution, final Series series,
            final String paymentURL, final String returnURL, final String paylineMerchantId,
            final String paylineMerchantAccessKey, final String paylineContractNumber) {

        this();

        setFinantialInstitution(finantialInstitution);
        setSeries(series);
        setPaymentURL(paymentURL);
        setReturnURL(returnURL);
        setPaylineMerchantId(paylineMerchantId);
        setPaylineMerchantAccessKey(paylineMerchantAccessKey);
        setPaylineContractNumber(paylineContractNumber);
        setImplementation(PaylineImplementation.class.getName());

        checkRules();
    }

    private void checkRules() {
        if (getFinantialInstitution() == null) {
            throw new TreasuryDomainException("error.ForwardPaymentConfiguration.finantialInstitution.required");
        }

        if (getFinantialInstitution().getForwardPaymentConfigurationsSet().size() > 1) {
            throw new TreasuryDomainException("error.ForwardPaymentConfiguration.finantialInstitution.only.one.allowed");
        }
    }

    public boolean isActive() {
        return getActive();
    }

    public String formattedAmount(final ForwardPayment forwardPayment) {
        return implementation().getFormattedAmount(forwardPayment);
    }

    public IForwardPaymentController getForwardPaymentController(final ForwardPayment forwardPayment) {
        return implementation().getForwardPaymentController(forwardPayment);
    }

    public IForwardPaymentImplementation implementation() {
        try {
            return (IForwardPaymentImplementation) Class.forName(getImplementation()).newInstance();
        } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static ForwardPaymentConfiguration createForTPAVirtual(final FinantialInstitution finantialInstitution,
            final Series series, final String paymentURL, final String returnURL, final String merchantId,
            final String virtualTPAId, final String implementation) {
        return new ForwardPaymentConfiguration(finantialInstitution, series, paymentURL, returnURL, merchantId, virtualTPAId,
                implementation);
    }

    public static ForwardPaymentConfiguration createForPayline(final FinantialInstitution finantialInstitution,
            final Series series, final String paymentURL, final String returnURL, final String paylineMerchantId,
            final String paylineMerchantAccessKey, final String paylineContractNumber) {

        return new ForwardPaymentConfiguration(finantialInstitution, series, paymentURL, returnURL, paylineMerchantId,
                paylineMerchantAccessKey, paylineContractNumber);
    }

    public static boolean isActive(final FinantialInstitution finantialInstitution) {
        if (finantialInstitution.getForwardPaymentConfigurationsSet().isEmpty()) {
            return false;
        }

        return finantialInstitution.getForwardPaymentConfigurationsSet().iterator().next().isActive();
    }

}
