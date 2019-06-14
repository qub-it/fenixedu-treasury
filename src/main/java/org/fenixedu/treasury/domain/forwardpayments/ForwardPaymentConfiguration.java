package org.fenixedu.treasury.domain.forwardpayments;

import java.util.Optional;

import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.forwardpayments.implementations.IForwardPaymentImplementation;
import org.fenixedu.treasury.dto.forwardpayments.ForwardPaymentConfigurationBean;
import org.fenixedu.treasury.ui.document.forwardpayments.IForwardPaymentController;

import com.google.common.base.Strings;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class ForwardPaymentConfiguration extends ForwardPaymentConfiguration_Base {

    private ForwardPaymentConfiguration() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

    private ForwardPaymentConfiguration(final FinantialInstitution finantialInstitution,
            final ForwardPaymentConfigurationBean bean) {

        this();

        setFinantialInstitution(finantialInstitution);

        setActive(bean.isActive());
        setName(bean.getName());
        setPaymentURL(bean.getPaymentURL());
        setReturnURL(bean.getReturnURL());
        setVirtualTPAMOXXURL(bean.getVirtualTPAMOXXURL());
        setVirtualTPAMerchantId(bean.getVirtualTPAMerchantId());
        setVirtualTPAId(bean.getVirtualTPAId());
        setVirtualTPAKeyStoreName(bean.getVirtualTPAKeyStoreName());
        setVirtualTPACertificateAlias(bean.getVirtualTPACertificateAlias());
        setVirtualTPACertificatePassword(bean.getVirtualTPACertificatePassword());
        setImplementation(bean.getImplementation());
        setPaylineMerchantId(bean.getPaylineMerchantId());
        setPaylineMerchantAccessKey(bean.getPaylineMerchantAccessKey());
        setPaylineContractNumber(bean.getPaylineContractNumber());

        setSeries(bean.getSeries());
        setPaymentMethod(bean.getPaymentMethod());
        
        setReimbursementPolicyJspFile(bean.getReimbursementPolicyJspFile());
        setPrivacyPolicyJspFile(bean.getPrivacyPolicyJspFile());
        
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

    @Atomic
    public void edit(final ForwardPaymentConfigurationBean bean) {

        setActive(bean.isActive());
        setName(bean.getName());
        setPaymentURL(bean.getPaymentURL());
        setReturnURL(bean.getReturnURL());
        setVirtualTPAMOXXURL(bean.getVirtualTPAMOXXURL());
        setVirtualTPAMerchantId(bean.getVirtualTPAMerchantId());
        setVirtualTPAId(bean.getVirtualTPAId());
        setVirtualTPAKeyStoreName(bean.getVirtualTPAKeyStoreName());
        setVirtualTPACertificateAlias(bean.getVirtualTPACertificateAlias());
        setVirtualTPACertificatePassword(bean.getVirtualTPACertificatePassword());
        setImplementation(bean.getImplementation());
        setPaylineMerchantId(bean.getPaylineMerchantId());
        setPaylineMerchantAccessKey(bean.getPaylineMerchantAccessKey());
        setPaylineContractNumber(bean.getPaylineContractNumber());

        setSeries(bean.getSeries());
        setPaymentMethod(bean.getPaymentMethod());

        setReimbursementPolicyJspFile(bean.getReimbursementPolicyJspFile());
        setPrivacyPolicyJspFile(bean.getPrivacyPolicyJspFile());
        
        checkRules();
    }
    
    @Atomic
    public void saveVirtualTPACertificate(final String filename, final byte[] contents) {
        if(getVirtualTPACertificate() != null) {
            ForwardPaymentConfigurationFile virtualTPACertificate = getVirtualTPACertificate();
            setVirtualTPACertificate(null);
            virtualTPACertificate.delete();
        }
        
        ForwardPaymentConfigurationFile file = ForwardPaymentConfigurationFile.create(filename, contents);
        setVirtualTPACertificate(file);
    }

    public boolean isActive() {
        return getActive();
    }
    
    public boolean isReimbursementPolicyTextDefined() {
        return !Strings.isNullOrEmpty(getReimbursementPolicyJspFile());
    }
    
    public boolean isPrivacyPolicyTextDefined() {
        return !Strings.isNullOrEmpty(getPrivacyPolicyJspFile());
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

    @Atomic
    public static ForwardPaymentConfiguration create(final FinantialInstitution finantialInstitution,
            final ForwardPaymentConfigurationBean bean) {
        return new ForwardPaymentConfiguration(finantialInstitution, bean);
    }
    
    public static Optional<ForwardPaymentConfiguration> find(final FinantialInstitution finantialInstitution) {
        return finantialInstitution.getForwardPaymentConfigurationsSet().stream().findFirst();
    }

    public static boolean isActive(final FinantialInstitution finantialInstitution) {
        if(!find(finantialInstitution).isPresent()) {
            return false;
        }
        
        return find(finantialInstitution).get().isActive();
    }

}
