package org.fenixedu.treasury.domain.document;

import java.util.List;

import org.fenixedu.treasury.domain.AdhocCustomer;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.util.Constants;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class ERPCustomerFieldsBean {

    private static final String MORADA_DESCONHECIDO = "Desconhecido";
    private static final int MAX_ADDRESS_DETAIL = 100;
    private static final int MAX_CITY = 50;
    private static final int MAX_ZIPCODE = 20;
    private static final int MAX_REGION = 50;

    private static final int MAX_FISCAL_NUM = 20;
    private static final int MAX_CONTACT = 50;
    private static final int MAX_NAME = 100;
    private static final int MAX_STREET_NAME = 90;

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

        if (!validateFiscalNumber(customer.getFiscalCountry(), customer.getFiscalNumber())) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.invalid.fiscal.number");
        }

        final List<String> errorMessages = Lists.newArrayList();
        if (!validateAddress(customer.getCountryCode(), customer.getAddress(), customer.getZipCode(),
                customer.getDistrictSubdivision(), customer.getFiscalCountry(), errorMessages)) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.invalid.address");
        }

        convertAddress(bean, customer.getCountryCode(), customer.getAddress(), customer.getZipCode(),
                customer.getDistrictSubdivision(), customer.getAddress());

        // CompanyName
        if (customer.getName().length() > MAX_NAME) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.customerName.more.than.allowed",
                    String.valueOf(MAX_NAME));
        }

        bean.setCustomerName(customer.getName());

        // Contact
        bean.setCustomerContact(Splitter.fixedLength(MAX_CONTACT).splitToList(customer.getName()).get(0));

        bean.setCustomerId(customer.getCode());

        bean.setCustomerBusinessId(customer.getBusinessIdentification());

        if (!Strings.isNullOrEmpty(customer.getFiscalNumber()) && customer.getFiscalNumber().length() > MAX_FISCAL_NUM) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.fiscalNumber.more.than.allowed",
                    String.valueOf(MAX_FISCAL_NUM));
        }

        // CustomerTaxID
        bean.setCustomerFiscalNumber(customer.getFiscalNumber());

        bean.setCustomerFiscalCountry(customer.getFiscalCountry());
        bean.setCustomerNationality(customer.getNationalityCountryCode());

        bean.checkRules();

        return bean;
    }

    private static boolean validateFiscalNumber(final String fiscalCountryCode, final String fiscalNumber) {
        return !Strings.isNullOrEmpty(fiscalNumber);
    }

    public static ERPCustomerFieldsBean fillFromDebitNote(final DebitNote debitNote) {
        return fillFromCustomer(debitNote.getDebtAccount().getCustomer());
    }

    public static ERPCustomerFieldsBean fillPayorFromDebitNote(final DebitNote debitNote) {
        final Customer payorCustomer = debitNote.getPayorDebtAccount().getCustomer();

        final List<String> errorMessages = Lists.newArrayList();
        if (!validateAddress(payorCustomer.getCountryCode(), payorCustomer.getAddress(), payorCustomer.getZipCode(),
                payorCustomer.getDistrictSubdivision(), payorCustomer.getFiscalCountry(), errorMessages)) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.invalid.address");
        }

        return fillFromCustomer(payorCustomer);
    }

    public static ERPCustomerFieldsBean fillFromCreditNote(final CreditNote creditNote) {
        if (creditNote.getDebitNote() == null) {
            return fillFromCustomer(creditNote.getDebtAccount().getCustomer());
        }

        final DebitNote debitNote = creditNote.getDebitNote();

        final ERPCustomerFieldsBean bean = new ERPCustomerFieldsBean();
        bean.setCustomerId(debitNote.getCustomerId());

        bean.setCustomerBusinessId(debitNote.getCustomerBusinessId());
        bean.setCustomerFiscalCountry(debitNote.getCustomerFiscalCountry());
        bean.setCustomerNationality(debitNote.getCustomerNationality());
        bean.setCustomerAccountId(debitNote.getCustomerAccountId());

        if (!validateFiscalNumber(debitNote.getCustomerCountry(), debitNote.getCustomerFiscalNumber())) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.invalid.fiscal.number");
        }

        if (!Strings.isNullOrEmpty(debitNote.getCustomerFiscalNumber())
                && debitNote.getCustomerFiscalNumber().length() > MAX_FISCAL_NUM) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.fiscalNumber.more.than.allowed",
                    String.valueOf(MAX_FISCAL_NUM));
        }
        bean.setCustomerFiscalNumber(debitNote.getCustomerFiscalNumber());

        if (debitNote.getCustomerName().length() > MAX_NAME) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.customerName.more.than.allowed",
                    String.valueOf(MAX_NAME));
        }
        bean.setCustomerName(debitNote.getCustomerName());

        bean.setCustomerContact(debitNote.getCustomerContact());

        final List<String> errorMessages = Lists.newArrayList();
        if (!validateAddress(debitNote.getCustomerCountry(), debitNote.getCustomerAddressDetail(), debitNote.getCustomerZipCode(),
                debitNote.getCustomerRegion(), debitNote.getCustomerFiscalCountry(), errorMessages)) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.invalid.address");
        }

        convertAddress(bean, debitNote.getCustomerCountry(), debitNote.getCustomerAddressDetail(), debitNote.getCustomerZipCode(),
                debitNote.getCustomerRegion(), debitNote.getCustomerAddressDetail());

        bean.checkRules();

        return bean;
    }

    public static ERPCustomerFieldsBean fillPayorFromCreditNote(final CreditNote creditNote) {
        if (creditNote.getDebitNote() == null) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean");
        }

        final DebitNote debitNote = creditNote.getDebitNote();

        final ERPCustomerFieldsBean bean = new ERPCustomerFieldsBean();
        bean.setCustomerId(debitNote.getPayorCustomerId());

        bean.setCustomerBusinessId(debitNote.getPayorCustomerBusinessId());
        bean.setCustomerFiscalCountry(debitNote.getPayorCustomerFiscalCountry());
        bean.setCustomerNationality(debitNote.getPayorCustomerNationality());
        bean.setCustomerAccountId(debitNote.getPayorCustomerAccountId());

        if (!validateFiscalNumber(debitNote.getPayorCustomerCountry(), debitNote.getPayorCustomerFiscalNumber())) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.invalid.fiscal.number");
        }

        if (!Strings.isNullOrEmpty(debitNote.getPayorCustomerFiscalNumber())
                && debitNote.getPayorCustomerFiscalNumber().length() > MAX_FISCAL_NUM) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.payorFiscalNumber.more.than.allowed",
                    String.valueOf(MAX_FISCAL_NUM));
        }
        bean.setCustomerFiscalNumber(debitNote.getPayorCustomerFiscalNumber());

        if (debitNote.getPayorCustomerName().length() > MAX_NAME) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.payorCustomerName.more.than.allowed",
                    String.valueOf(MAX_NAME));
        }

        bean.setCustomerName(debitNote.getPayorCustomerName());
        bean.setCustomerContact(debitNote.getPayorCustomerContact());

        final List<String> errorMessages = Lists.newArrayList();
        if (!validateAddress(debitNote.getPayorCustomerCountry(), debitNote.getPayorCustomerAddressDetail(),
                debitNote.getPayorCustomerZipCode(), debitNote.getPayorCustomerRegion(),
                debitNote.getPayorCustomerFiscalCountry(), errorMessages)) {
            throw new TreasuryDomainException("error.ERPCustomerFieldsBean.invalid.payor.address");
        }

        convertAddress(bean, debitNote.getPayorCustomerCountry(), debitNote.getPayorCustomerAddressDetail(),
                debitNote.getPayorCustomerZipCode(), debitNote.getPayorCustomerRegion(),
                debitNote.getPayorCustomerAddressDetail());

        bean.checkRules();
        return bean;
    }

    private static void convertAddress(final ERPCustomerFieldsBean bean, final String country, final String addressDetail,
            final String zipCode, final String zipCodeRegion, final String street) {

        bean.setCustomerCountry(!Strings.isNullOrEmpty(country) ? country : MORADA_DESCONHECIDO);
        bean.setCustomerAddressDetail(!Strings.isNullOrEmpty(addressDetail) ? addressDetail : MORADA_DESCONHECIDO);
        bean.setCustomerCity(!Strings.isNullOrEmpty(zipCodeRegion) ? zipCodeRegion : MORADA_DESCONHECIDO);
        bean.setCustomerZipCode(!Strings.isNullOrEmpty(zipCode) ? zipCode : MORADA_DESCONHECIDO);
        bean.setCustomerRegion(!Strings.isNullOrEmpty(zipCodeRegion) ? zipCodeRegion : MORADA_DESCONHECIDO);
        bean.setCustomerStreetName(Splitter.fixedLength(MAX_STREET_NAME).splitToList(street).get(0));
    }

    public static boolean validateAddress(final Customer customer, final List<String> errorMessages) {
        return validateAddress(customer.getCountryCode(), customer.getAddress(), customer.getZipCode(),
                customer.getDistrictSubdivision(), customer.getFiscalCountry(), errorMessages);
    }

    private static boolean validateAddress(final String addressCountryCode, final String address, final String zipCode,
            final String districtSubdivision, final String fiscalCountryCode, final List<String> errorMessages) {
        if (Strings.isNullOrEmpty(addressCountryCode)) {
            errorMessages.add(Constants.bundle("error.ERPCustomerFieldsBean.address.countryCode.not.filled"));
        }

        if (Strings.isNullOrEmpty(address)) {
            errorMessages.add(Constants.bundle("error.ERPCustomerFieldsBean.address.address.not.filled"));
        }

        if (Strings.isNullOrEmpty(zipCode)) {
            errorMessages.add(Constants.bundle("error.ERPCustomerFieldsBean.address.zipCode.not.filled"));
        }

        if (Strings.isNullOrEmpty(districtSubdivision)) {
            errorMessages.add(Constants.bundle("error.ERPCustomerFieldsBean.address.districtSubdivision.not.filled"));
        }

        if (!Strings.isNullOrEmpty(address) && address.length() > MAX_ADDRESS_DETAIL) {
            errorMessages.add(Constants.bundle("error.ERPCustomerFieldsBean.addressDetail.more.than.allowed",
                    String.valueOf(MAX_ADDRESS_DETAIL), address));
        }

        if (!Strings.isNullOrEmpty(districtSubdivision) && districtSubdivision.length() > MAX_CITY) {
            errorMessages.add(Constants.bundle("error.ERPCustomerFieldsBean.city.more.than.allowed", String.valueOf(MAX_CITY),
                    districtSubdivision));
        }

        if (!Strings.isNullOrEmpty(zipCode) && zipCode.length() > MAX_ZIPCODE) {
            errorMessages.add(Constants.bundle("error.ERPCustomerFieldsBean.zipCode.more.than.allowed",
                    String.valueOf(MAX_ZIPCODE), zipCode));
        }

        if (!Strings.isNullOrEmpty(districtSubdivision) && districtSubdivision.length() > MAX_REGION) {
            errorMessages.add(Constants.bundle("error.ERPCustomerFieldsBean.region.more.than.allowed", String.valueOf(MAX_REGION),
                    districtSubdivision));
        }

        if (!Strings.isNullOrEmpty(addressCountryCode) && !addressCountryCode.equals(fiscalCountryCode)) {
            errorMessages.add(Constants.bundle("error.ERPCustomerFieldsBean.fiscal.country.not.equals.to.address",
                    String.valueOf(MAX_REGION), addressCountryCode, fiscalCountryCode));
        }

        return errorMessages.isEmpty();
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
