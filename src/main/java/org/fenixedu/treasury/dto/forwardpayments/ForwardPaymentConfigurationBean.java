package org.fenixedu.treasury.dto.forwardpayments;

import org.fenixedu.treasury.domain.PaymentMethod;
import org.fenixedu.treasury.domain.document.Series;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPaymentConfiguration;

public class ForwardPaymentConfigurationBean {

    private boolean active;
    private String name;
    private String paymentURL;
    private String returnURL;
    private String virtualTPAMOXXURL;
    private String virtualTPAMerchantId;
    private String virtualTPAId;
    private String virtualTPAKeyStoreName;
    private String virtualTPACertificateAlias;
    private String virtualTPACertificatePassword;
    private String implementation;
    private String paylineMerchantId;
    private String paylineMerchantAccessKey;
    private String paylineContractNumber;
    
    private Series series;
    private PaymentMethod paymentMethod;

    private String reimbursementPolicyJspFile;
    private String privacyPolicyJspFile;
    
    public ForwardPaymentConfigurationBean() {
    }
    
    public ForwardPaymentConfigurationBean(final ForwardPaymentConfiguration configuration) {
        setActive(configuration.isActive());
        setName(configuration.getName());
        setPaymentURL(configuration.getPaymentURL());
        setReturnURL(configuration.getReturnURL());
        setVirtualTPAMOXXURL(configuration.getVirtualTPAMOXXURL());
        setVirtualTPAMerchantId(configuration.getVirtualTPAMerchantId());
        setVirtualTPAId(configuration.getVirtualTPAId());
        setVirtualTPAKeyStoreName(configuration.getVirtualTPAKeyStoreName());
        setVirtualTPACertificateAlias(configuration.getVirtualTPACertificateAlias());
        setVirtualTPACertificatePassword(configuration.getVirtualTPACertificatePassword());
        setImplementation(configuration.getImplementation());
        setPaylineMerchantId(configuration.getPaylineMerchantId());
        setPaylineMerchantAccessKey(configuration.getPaylineMerchantAccessKey());
        setPaylineContractNumber(configuration.getPaylineContractNumber());
        
        setSeries(configuration.getSeries());
        setPaymentMethod(configuration.getPaymentMethod());
        
        setReimbursementPolicyJspFile(configuration.getReimbursementPolicyJspFile());
        setPrivacyPolicyJspFile(configuration.getPrivacyPolicyJspFile());
    }

    // @formatter:off
    /* *****************
     * GETTERS & SETTERS
     * *****************
     */
    // @formatter:on

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPaymentURL() {
        return paymentURL;
    }

    public void setPaymentURL(String paymentURL) {
        this.paymentURL = paymentURL;
    }

    public String getReturnURL() {
        return returnURL;
    }

    public void setReturnURL(String returnURL) {
        this.returnURL = returnURL;
    }

    public String getVirtualTPAMOXXURL() {
        return virtualTPAMOXXURL;
    }

    public void setVirtualTPAMOXXURL(String virtualTPAMOXXURL) {
        this.virtualTPAMOXXURL = virtualTPAMOXXURL;
    }

    public String getVirtualTPAMerchantId() {
        return virtualTPAMerchantId;
    }

    public void setVirtualTPAMerchantId(String virtualTPAMerchantId) {
        this.virtualTPAMerchantId = virtualTPAMerchantId;
    }

    public String getVirtualTPAId() {
        return virtualTPAId;
    }

    public void setVirtualTPAId(String virtualTPAId) {
        this.virtualTPAId = virtualTPAId;
    }

    public String getVirtualTPAKeyStoreName() {
        return virtualTPAKeyStoreName;
    }

    public void setVirtualTPAKeyStoreName(String virtualTPAKeyStoreName) {
        this.virtualTPAKeyStoreName = virtualTPAKeyStoreName;
    }

    public String getVirtualTPACertificateAlias() {
        return virtualTPACertificateAlias;
    }

    public void setVirtualTPACertificateAlias(String virtualTPACertificateAlias) {
        this.virtualTPACertificateAlias = virtualTPACertificateAlias;
    }

    public String getVirtualTPACertificatePassword() {
        return virtualTPACertificatePassword;
    }

    public void setVirtualTPACertificatePassword(String virtualTPACertificatePassword) {
        this.virtualTPACertificatePassword = virtualTPACertificatePassword;
    }

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    public String getPaylineMerchantId() {
        return paylineMerchantId;
    }

    public void setPaylineMerchantId(String paylineMerchantId) {
        this.paylineMerchantId = paylineMerchantId;
    }

    public String getPaylineMerchantAccessKey() {
        return paylineMerchantAccessKey;
    }

    public void setPaylineMerchantAccessKey(String paylineMerchantAccessKey) {
        this.paylineMerchantAccessKey = paylineMerchantAccessKey;
    }

    public String getPaylineContractNumber() {
        return paylineContractNumber;
    }

    public void setPaylineContractNumber(String paylineContractNumber) {
        this.paylineContractNumber = paylineContractNumber;
    }
    
    public Series getSeries() {
        return series;
    }
    
    public void setSeries(Series series) {
        this.series = series;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReimbursementPolicyJspFile() {
        return reimbursementPolicyJspFile;
    }

    public void setReimbursementPolicyJspFile(String reimbursementPolicyJspFile) {
        this.reimbursementPolicyJspFile = reimbursementPolicyJspFile;
    }

    public String getPrivacyPolicyJspFile() {
        return privacyPolicyJspFile;
    }

    public void setPrivacyPolicyJspFile(String privacyPolicyJspFile) {
        this.privacyPolicyJspFile = privacyPolicyJspFile;
    }
    
}
