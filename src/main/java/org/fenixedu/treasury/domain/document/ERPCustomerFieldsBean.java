package org.fenixedu.treasury.domain.document;

import org.fenixedu.treasury.domain.AdhocCustomer;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

public class ERPCustomerFieldsBean {

    private static final String MORADA_DESCONHECIDO = "Desconhecido";
    private static final int MAX_ADDRESS_DETAIL = 100;
    private static final int MAX_CITY = 50;
    private static final int MAX_ZIPCODE = 20;
    private static final int MAX_REGION = 50;

    private static final int MAX_FISCAL_NUM = 20;
    
    private static final int MAX_CONTACT = 50;

    private String customerId;

    private String customerBusinessId;
    private String customerFiscalCountry;
    private String customerNationality;
    private String customerAccountId;
    private String customerFiscalNumber;
    private String customerName;
    private String customerContact;
    private String customerStreetName;
    private String customerAddressDetail;
    private String customerCity;
    private String customerZipCode;
    private String customerRegion;
    private String customerCountry;

    public ERPCustomerFieldsBean() {
    }
    
    private void checkRules() {
    
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on

    public static ERPCustomerFieldsBean fillFromCustomer(final Customer customer) {
        final ERPCustomerFieldsBean bean = new ERPCustomerFieldsBean();
        
        // AccountID
        /*
         * Deve ser indicada a respectiva conta corrente do cliente no plano de
         * contas da contabilidade, caso esteja definida. Caso contr?rio dever?
         * ser preenchido com a designa??o ?Desconhecido?.
         */

        if (customer instanceof AdhocCustomer) {
            bean.setCustomerAccountId("ADHOC");
        } else {
            bean.setCustomerAccountId("STUDENT");
        }

        if (Strings.isNullOrEmpty(customer.getCountryCode())) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.address.countryCode.not.filled");
        } else if (Strings.isNullOrEmpty(customer.getAddress())) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.address.address.not.filled");
        } else if (Strings.isNullOrEmpty(customer.getZipCode())) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.address.zipCode.not.filled");
        } else if (Strings.isNullOrEmpty(customer.getDistrictSubdivision())) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.address.districtSubdivision.not.filled");
        }

        convertAddress(bean, customer.getCountryCode(), customer.getAddress(), customer.getZipCode(),
                customer.getDistrictSubdivision(), customer.getAddress());

        // CompanyName
        bean.setCustomerName(customer.getName());

        // Contact
        bean.setCustomerContact(Splitter.fixedLength(MAX_CONTACT).splitToList(customer.getName()).get(0));

        bean.setCustomerId(customer.getCode());

        bean.setCustomerBusinessId(customer.getBusinessIdentification());

        if (!Strings.isNullOrEmpty(customer.getFiscalNumber()) && customer.getFiscalNumber().length() > MAX_FISCAL_NUM) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.fiscalNumber.more.than.allowed");
        }

        // CustomerTaxID
        bean.setCustomerFiscalNumber(customer.getFiscalNumber());

        bean.setCustomerFiscalCountry(customer.getFiscalCountry());
        bean.setCustomerNationality(customer.getNationalityCountryCode());
        
        bean.checkRules();
        
        return bean;
    }

    public static ERPCustomerFieldsBean fillFromDebitNote(final DebitNote debitNote) {
        return fillFromCustomer(debitNote.getDebtAccount().getCustomer());
    }
    
    public static ERPCustomerFieldsBean fillPayorFromDebitNote(final DebitNote debitNote) {
        return fillFromCustomer(debitNote.getPayorDebtAccount().getCustomer());
    }

    public static ERPCustomerFieldsBean fillFromCreditNote(final CreditNote creditNote) {
        if(creditNote.getDebitNote() == null) {
            return fillFromCustomer(creditNote.getDebtAccount().getCustomer());
        }
        
        final DebitNote debitNote = creditNote.getDebitNote();
        
        final ERPCustomerFieldsBean bean = new ERPCustomerFieldsBean();
        bean.setCustomerId(debitNote.getCustomerId());

        bean.setCustomerBusinessId(debitNote.getCustomerBusinessId());
        bean.setCustomerFiscalCountry(debitNote.getCustomerFiscalCountry());
        bean.setCustomerNationality(debitNote.getCustomerNationality());
        bean.setCustomerAccountId(debitNote.getCustomerAccountId());
        bean.setCustomerFiscalNumber(debitNote.getCustomerFiscalNumber());
        bean.setCustomerName(debitNote.getCustomerName());
        bean.setCustomerContact(debitNote.getCustomerContact());
        bean.setCustomerStreetName(debitNote.getCustomerStreetName());
        bean.setCustomerAddressDetail(debitNote.getCustomerAddressDetail());
        bean.setCustomerCity(debitNote.getCustomerCity());
        bean.setCustomerZipCode(debitNote.getCustomerZipCode());
        bean.setCustomerRegion(debitNote.getCustomerRegion());
        bean.setCustomerCountry(debitNote.getCustomerCountry());
        
        bean.checkRules();
        
        return bean;
    }

    public static ERPCustomerFieldsBean fillPayorFromCreditNote(final CreditNote creditNote) {
        if(creditNote.getDebitNote() == null) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean");
        }
        
        final DebitNote debitNote = creditNote.getDebitNote();
        
        final ERPCustomerFieldsBean bean = new ERPCustomerFieldsBean();
        bean.setCustomerId(debitNote.getPayorCustomerId());

        bean.setCustomerBusinessId(debitNote.getPayorCustomerBusinessId());
        bean.setCustomerFiscalCountry(debitNote.getPayorCustomerFiscalCountry());
        bean.setCustomerNationality(debitNote.getPayorCustomerNationality());
        bean.setCustomerAccountId(debitNote.getPayorCustomerAccountId());
        bean.setCustomerFiscalNumber(debitNote.getPayorCustomerFiscalNumber());
        bean.setCustomerName(debitNote.getPayorCustomerName());
        bean.setCustomerContact(debitNote.getPayorCustomerContact());
        bean.setCustomerStreetName(debitNote.getPayorCustomerStreetName());
        bean.setCustomerAddressDetail(debitNote.getPayorCustomerAddressDetail());
        bean.setCustomerCity(debitNote.getPayorCustomerCity());
        bean.setCustomerZipCode(debitNote.getPayorCustomerZipCode());
        bean.setCustomerRegion(debitNote.getPayorCustomerRegion());
        bean.setCustomerCountry(debitNote.getPayorCustomerCountry());
        
        bean.checkRules();
        
        return bean;
    }    
    
    private static void convertAddress(final ERPCustomerFieldsBean bean, final String country, final String addressDetail,
            final String zipCode, final String zipCodeRegion, final String street) {

        bean.setCustomerCountry(!Strings.isNullOrEmpty(country) ? country : MORADA_DESCONHECIDO);

        if (addressDetail != null && addressDetail.length() > MAX_ADDRESS_DETAIL) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.addressDetail.more.than.allowed",
                    String.valueOf(MAX_ADDRESS_DETAIL));
        }

        bean.setCustomerAddressDetail(!Strings.isNullOrEmpty(addressDetail) ? addressDetail : MORADA_DESCONHECIDO);

        if (zipCodeRegion != null && zipCodeRegion.length() > MAX_CITY) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.city.more.than.allowed", String.valueOf(MAX_CITY));
        }
        bean.setCustomerCity(!Strings.isNullOrEmpty(zipCodeRegion) ? zipCodeRegion : MORADA_DESCONHECIDO);

        if (zipCode != null && zipCode.length() > MAX_ZIPCODE) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.zipCode.more.than.allowed", String.valueOf(MAX_ZIPCODE));
        }

        bean.setCustomerZipCode(!Strings.isNullOrEmpty(zipCode) ? zipCode : MORADA_DESCONHECIDO);

        if (zipCodeRegion != null && zipCodeRegion.length() > MAX_REGION) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.region.more.than.allowed", String.valueOf(MAX_REGION));
        }
        bean.setCustomerRegion(!Strings.isNullOrEmpty(zipCodeRegion) ? zipCodeRegion : MORADA_DESCONHECIDO);
    }

    // @formatter:off
    /* *****************
     * GETTERS & SETTERS
     * *****************
     */
    // @formatter:on

    public String getCustomerBusinessId() {
        return customerBusinessId;
    }

    public void setCustomerBusinessId(String customerBusinessId) {
        this.customerBusinessId = customerBusinessId;
    }

    public String getCustomerFiscalCountry() {
        return customerFiscalCountry;
    }

    public void setCustomerFiscalCountry(String customerFiscalCountry) {
        this.customerFiscalCountry = customerFiscalCountry;
    }

    public String getCustomerNationality() {
        return customerNationality;
    }

    public void setCustomerNationality(String customerNationality) {
        this.customerNationality = customerNationality;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerAccountId() {
        return customerAccountId;
    }

    public void setCustomerAccountId(String customerAccountId) {
        this.customerAccountId = customerAccountId;
    }

    public String getCustomerFiscalNumber() {
        return customerFiscalNumber;
    }

    public void setCustomerFiscalNumber(String customerFiscalNumber) {
        this.customerFiscalNumber = customerFiscalNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }

    public String getCustomerStreetName() {
        return customerStreetName;
    }

    public void setCustomerStreetName(String custometStreetName) {
        this.customerStreetName = custometStreetName;
    }

    public String getCustomerAddressDetail() {
        return customerAddressDetail;
    }

    public void setCustomerAddressDetail(String customerAddressDetail) {
        this.customerAddressDetail = customerAddressDetail;
    }

    public String getCustomerCity() {
        return customerCity;
    }

    public void setCustomerCity(String customerCity) {
        this.customerCity = customerCity;
    }

    public String getCustomerZipCode() {
        return customerZipCode;
    }

    public void setCustomerZipCode(String customerZipCode) {
        this.customerZipCode = customerZipCode;
    }

    public String getCustomerRegion() {
        return customerRegion;
    }

    public void setCustomerRegion(String customerRegion) {
        this.customerRegion = customerRegion;
    }

    public String getCustomerCountry() {
        return customerCountry;
    }

    public void setCustomerCountry(String customerCountry) {
        this.customerCountry = customerCountry;
    }

}
