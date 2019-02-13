package org.fenixedu.treasury.domain;

import java.util.Comparator;

import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class FiscalYear extends FiscalYear_Base {

    public static final Comparator<FiscalYear> COMPARE_BY_YEAR = (o1, o2) -> {
        int c = Integer.compare(o1.getYear(), o2.getYear());
        
        if(c != 0) {
            return c;
        }
        
        return o1.getExternalId().compareTo(o2.getExternalId());
    };
    
    public FiscalYear() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }
    
    public FiscalYear(final FinantialInstitution finantialInstitution, final int year, final LocalDate settlementAnnulmentEndDate) {
        this();
        setFinantialInstitution(finantialInstitution);
        setYear(year);
        setSettlementAnnulmentEndDate(settlementAnnulmentEndDate);
        
        checkRules();
    }
    
    public void checkRules() {
        if(getDomainRoot() == null) {
            throw new TreasuryDomainException("error.FiscalYear.domainRoot.required");
        }
        
        if(getFinantialInstitution() == null) {
            throw new TreasuryDomainException("error.FiscalYear.finantialInstitution.required");
        }
        
        if(getSettlementAnnulmentEndDate() == null) {
            throw new TreasuryDomainException("error.FiscalYear.settlementAnnulmentEndDate.required");
        }
    }

    @Atomic
    public void editSettlementAnnulmentEndDate(final LocalDate endDate) {
        setSettlementAnnulmentEndDate(endDate);
        
        checkRules();
    }
    
    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on
    
    @Atomic
    public static FiscalYear create(final FinantialInstitution finantialInstitution, final int year, final LocalDate settlementAnnulmentEndDate) {
        return new FiscalYear(finantialInstitution, year, settlementAnnulmentEndDate);
    }
}
