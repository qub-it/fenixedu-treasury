package org.fenixedu.treasury.domain.integration;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class OperationFileDomainObject extends OperationFileDomainObject_Base {
    

    public OperationFileDomainObject() {
        super();
        this.setDomainRoot(FenixFramework.getDomainRoot());
    }

    // TODO: Implement
    public boolean isAccessible(User arg0) {
        throw new RuntimeException("not implemented");
    }

    private void checkRules() {
        //
        // CHANGE_ME add more busines validations
        //

        // CHANGE_ME In order to validate UNIQUE restrictions
    }

    @Atomic
    public void edit() {
        checkRules();
    }

    public boolean isDeletable() {
        return true;
    }

    @Atomic
    public void delete() {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.OperationFile.cannot.delete");
        }

        this.setLogIntegrationOperation(null);
        this.setIntegrationOperation(null);

        deleteDomainObject();
    }
    
    public static OperationFileDomainObject copyAndAssociate(final OperationFile operationFile) {

        OperationFileDomainObject domainObject = new OperationFileDomainObject();
        domainObject.setIntegrationOperation(operationFile.getIntegrationOperation());
        domainObject.setLogIntegrationOperation(operationFile.getLogIntegrationOperation());
        
        domainObject.setTreasuryFile(operationFile);
        
        return domainObject;
    }

}
