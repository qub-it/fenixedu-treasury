/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: ricardo.pedro@qub-it.com
 *
 * 
 * This file is part of FenixEdu Treasury.
 *
 * FenixEdu Treasury is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Treasury is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Treasury.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fenixedu.treasury.dto;

import static org.fenixedu.treasury.util.TreasuryConstants.isDefaultCountry;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.CustomerType;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.util.TreasuryConstants;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import pt.ist.standards.geographic.Country;
import pt.ist.standards.geographic.Planet;

public class AdhocCustomerBean implements ITreasuryBean {

    private CustomerType customerType;
    private String code;
    private String fiscalNumber;
    private String identificationNumber;
    private String name;
    private String address;
    private String districtSubdivision;
    private String region;
    private String zipCode;
    private String addressCountryCode;
    private List<FinantialInstitution> finantialInstitutions;

    private List<TreasuryTupleDataSourceBean> finantialInstitutionsDataSource;
    private List<TreasuryTupleDataSourceBean> customerTypesDataSource;
    private List<TreasuryTupleDataSourceBean> countryCodesDataSource;
    
    private boolean changeFiscalNumberConfirmed;
    private boolean addressCountryDefault;

    public AdhocCustomerBean() {
        this.setFinantialInstitutionsDataSource(FinantialInstitution.findAll().collect(Collectors.toList()));
        this.setCustomerTypesDataSource(CustomerType.findAll().collect(Collectors.toList()));
        this.setCountryCodesDataSource(Lists.newArrayList(Planet.getEarth().getPlaces()));
        this.update();
    }

    public AdhocCustomerBean(Customer customer) {
        this();
        this.setCustomerType(customer.getCustomerType());
        this.code = customer.getCode();
        this.setFiscalNumber(customer.getFiscalNumber());
        this.setIdentificationNumber(customer.getIdentificationNumber());
        this.setName(customer.getName());
        this.setAddress(customer.getAddress());
        this.setDistrictSubdivision(customer.getDistrictSubdivision());
        this.setRegion(customer.getRegion());
        this.setZipCode(customer.getZipCode());
        this.setAddressCountryCode(customer.getAddressCountryCode());
        this.setFinantialInstitutions(customer.getDebtAccountsSet().stream().filter(x -> x.getClosed() == false)
                .map(x -> x.getFinantialInstitution()).collect(Collectors.toList()));
        
        this.update();
    }

    public boolean isAddressValid() {
        boolean valid = true;
        
        valid &= !Strings.isNullOrEmpty(this.getAddressCountryCode());
        valid &= !Strings.isNullOrEmpty(this.getAddress());
        valid &= !Strings.isNullOrEmpty(this.getDistrictSubdivision());
        
        if(isDefaultCountry(this.getAddressCountryCode())) {
            valid &= !Strings.isNullOrEmpty(this.getZipCode());
            valid &= !Strings.isNullOrEmpty(this.getRegion());
        }
        
        if(isDefaultCountry(this.getAddressCountryCode()) && !Strings.isNullOrEmpty(this.getZipCode())) {
            valid &= this.getZipCode().matches("\\d{4}-\\d{3}");
        }
        
        return valid;
    }
    
    public String getCode() {
        return code;
    }

    public java.lang.String getFiscalNumber() {
        return fiscalNumber;
    }

    public void setFiscalNumber(java.lang.String value) {
        fiscalNumber = value;
    }

    public java.lang.String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(java.lang.String value) {
        identificationNumber = value;
    }

    public java.lang.String getName() {
        return name;
    }

    public void setName(java.lang.String value) {
        name = value;
    }

    public java.lang.String getAddress() {
        return address;
    }

    public void setAddress(java.lang.String value) {
        address = value;
    }

    public java.lang.String getDistrictSubdivision() {
        return districtSubdivision;
    }

    public void setDistrictSubdivision(java.lang.String value) {
        districtSubdivision = value;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }

    public java.lang.String getZipCode() {
        return zipCode;
    }

    public void setZipCode(java.lang.String value) {
        zipCode = value;
    }
    
    public String getAddressCountryCode() {
        return addressCountryCode;
    }
    
    public void setAddressCountryCode(String addressCountryCode) {
        this.addressCountryCode = addressCountryCode;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
    }
    
    public boolean isChangeFiscalNumberConfirmed() {
        return changeFiscalNumberConfirmed;
    }
    
    public void setChangeFiscalNumberConfirmed(final boolean value) {
        changeFiscalNumberConfirmed = value;
    }

    public void update() {
        this.addressCountryDefault = TreasuryConstants.DEFAULT_COUNTRY.toUpperCase().equals(getAddressCountryCode());
    }
    
    public boolean isAddressCountryDefault() {
        return this.addressCountryDefault;
    }

    public List<FinantialInstitution> getFinantialInstitutions() {
        return finantialInstitutions;
    }

    public void setFinantialInstitutions(List<FinantialInstitution> finantialInstitutions) {
        this.finantialInstitutions = finantialInstitutions;
    }

    public List<TreasuryTupleDataSourceBean> getFinantialInstitutionsDataSource() {
        return finantialInstitutionsDataSource;
    }

    public void setFinantialInstitutionsDataSource(List<FinantialInstitution> finantialInstitutionsDataSource) {
        this.finantialInstitutionsDataSource = finantialInstitutionsDataSource.stream().map(x -> {
            TreasuryTupleDataSourceBean inst = new TreasuryTupleDataSourceBean();
            inst.setId(x.getExternalId());
            inst.setText(x.getName());
            return inst;
        }).collect(Collectors.toList());
    }

    public List<TreasuryTupleDataSourceBean> getCustomerTypesDataSource() {
        return customerTypesDataSource;
    }

    public void setCustomerTypesDataSource(List<CustomerType> customerTypesDataSource) {
        this.customerTypesDataSource = customerTypesDataSource.stream().map(customerType -> {
            TreasuryTupleDataSourceBean customerTypeDataSource = new TreasuryTupleDataSourceBean();
            customerTypeDataSource.setId(customerType.getExternalId());
            customerTypeDataSource.setText(customerType.getName().getContent());
            return customerTypeDataSource;
        }).collect(Collectors.toList());
    }
    
    public List<TreasuryTupleDataSourceBean> getCountryCodesDataSource() {
        return countryCodesDataSource;
    }
    
    public void setCountryCodesDataSource(final List<Country> countries) {
        this.countryCodesDataSource = countries.stream().map(c -> new TreasuryTupleDataSourceBean(c.alpha2, c.getLocalizedName(I18N.getLocale()))).collect(Collectors.toList());
        
        Collections.sort(this.countryCodesDataSource, TreasuryTupleDataSourceBean.COMPARE_BY_TEXT);
    }
}
