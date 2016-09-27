package org.fenixedu.treasury.domain.document.reimbursement;

import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;

import com.google.common.base.Strings;

public class ReimbursementProcessStatusType extends ReimbursementProcessStatusType_Base {
    
    public ReimbursementProcessStatusType() {
        super();
        setBennu(Bennu.getInstance());
    }
    
    protected ReimbursementProcessStatusType(final String code, final String description) {
        this();
        super.setCode(code);
        super.setDescription(description);
        
        checkRules();
    }

    private void checkRules() {
        
        if(getBennu() == null) {
            throw new TreasuryDomainException("error.ReimbursementProcessStatusType.bennu.required");
        }
        
        if(Strings.isNullOrEmpty(getCode())) {
            throw new TreasuryDomainException("error.ReimbursementProcessStatusType.code.required");
        }
        
        if(Strings.isNullOrEmpty(getDescription())) {
            throw new TreasuryDomainException("error.ReimbursementProcessStatusType.description.required");
        }
        
        if(findByCode(getCode()).count() > 1) {
            throw new TreasuryDomainException("error.ReimbursementProcessStatusType.code.already.defined", getCode());
        }
        
        
    }
    
    
    // @formatter:off
    /* --------
     * SERVICES
     * --------
     */
    // @formatter:on

    public Stream<ReimbursementProcessStatusType> findAll() {
        return Bennu.getInstance().getReimbursementProcessStatusTypesSet().stream();
    }
    
    public Stream<ReimbursementProcessStatusType> findByCode(final String code) {
        return findAll().filter(r -> code.equals(r.getCode()));
    }
    
    public Optional<ReimbursementProcessStatusType> findUniqueByCode(final String code) {
        return findByCode(code).findFirst();
    }
    
    public ReimbursementProcessStatusType create(final String code, final String description) {
        return new ReimbursementProcessStatusType(code, description);
    }
    
    
    
}
