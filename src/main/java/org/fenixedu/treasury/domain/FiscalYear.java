package org.fenixedu.treasury.domain;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

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
    
    public FiscalYear(final FinantialInstitution finantialInstitution, final int year, final LocalDate settlementAnnulmentLimitDate) {
        this();
        setFinantialInstitution(finantialInstitution);
        setYear(year);
        setSettlementAnnulmentLimitDate(settlementAnnulmentLimitDate);
        
        checkRules();
    }
    
    public void checkRules() {
        if(getDomainRoot() == null) {
            throw new TreasuryDomainException("error.FiscalYear.domainRoot.required");
        }
        
        if(getFinantialInstitution() == null) {
            throw new TreasuryDomainException("error.FiscalYear.finantialInstitution.required");
        }
        
        if(getSettlementAnnulmentLimitDate() == null) {
            throw new TreasuryDomainException("error.FiscalYear.settlementAnnulmentLimitDate.required");
        }
        
        if(FiscalYear.find(getFinantialInstitution(), getYear()).count() > 1) {
            throw new TreasuryDomainException("error.FiscalYear.already.defined.for.finantial.institution.and.year");
        }
        
    }

    @Atomic
    public void editSettlementAnnulmentLimitDate(final LocalDate limitDate) {
        setSettlementAnnulmentLimitDate(limitDate);
        
        checkRules();
    }
    
    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on
    
    public static Stream<FiscalYear> findAll() {
        return FenixFramework.getDomainRoot().getFiscalYearsSet().stream();
    }
    
    public static Stream<FiscalYear> find(final FinantialInstitution finantialInstitution, final int year) {
        return finantialInstitution.getFiscalYearsSet().stream().filter(fy -> fy.getYear() == year);
    }
    
    public static Optional<FiscalYear> findUnique(final FinantialInstitution finantialInstitution, final int year) {
        return find(finantialInstitution, year).findFirst();
    }
    
    @Atomic
    public static FiscalYear create(final FinantialInstitution finantialInstitution, final int year, final LocalDate settlementAnnulmentLimitDate) {
        return new FiscalYear(finantialInstitution, year, settlementAnnulmentLimitDate);
    }
}
