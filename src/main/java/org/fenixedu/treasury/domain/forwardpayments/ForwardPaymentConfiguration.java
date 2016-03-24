package org.fenixedu.treasury.domain.forwardpayments;

import java.util.Map;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.Series;
import org.fenixedu.treasury.domain.forwardpayments.implementations.IForwardPaymentImplementation;

public class ForwardPaymentConfiguration extends ForwardPaymentConfiguration_Base {

    private ForwardPaymentConfiguration() {
        super();
        setBennu(Bennu.getInstance());
    }

    private ForwardPaymentConfiguration(final FinantialInstitution finantialInstitution, final Series series, final String paymentURL,
            final String returnURL, final String merchantId, final String virtualTPAId, final String implementation) {
        this();
        
        setFinantialInstitution(finantialInstitution);
        setSeries(series);
        setPaymentURL(paymentURL);
        setReturnURL(returnURL);
        setMerchantId(merchantId);
        setVirtualTPAId(virtualTPAId);
        setImplementation(implementation);

        checkRules();
    }

    private void checkRules() {

    }

    public String paymentURL(final ForwardPayment forwardPayment) {
        return implementation().getPaymentURL(forwardPayment);
    }

    public String returnURL(final ForwardPayment forwardPayment) {
        return implementation().getReturnURL(forwardPayment);
    }

    public String paymentPage(final ForwardPayment forwardPayment) {
        return implementation().getPaymentPage(forwardPayment);
    }
    
    public String formattedAmount(final ForwardPayment forwardPayment) {
        return implementation().getFormattedAmount(forwardPayment);
    }

    private IForwardPaymentImplementation implementation() {
        try {
            return (IForwardPaymentImplementation) Class.forName(getImplementation()).newInstance();
        } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Object execute(ForwardPayment forwardPayment, final Map<String, String> paymentData) {
        return implementation().execute(forwardPayment, paymentData);
    }

    public static ForwardPaymentConfiguration create(final FinantialInstitution finantialInstitution, final Series series, final String paymentURL,
            final String returnURL, final String merchantId, final String virtualTPAId, final String implementation) {
        return new ForwardPaymentConfiguration(finantialInstitution, series, paymentURL, returnURL, merchantId, virtualTPAId,
                implementation);
    }

}
