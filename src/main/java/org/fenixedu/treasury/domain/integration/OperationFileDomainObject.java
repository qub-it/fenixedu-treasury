package org.fenixedu.treasury.domain.integration;

import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class OperationFileDomainObject extends OperationFileDomainObject_Base {
    

    public OperationFileDomainObject() {
        super();
        this.setDomainRoot(FenixFramework.getDomainRoot());
        setCreationDate(new DateTime());
        setCreator(Authenticate.getUser().getUsername());
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
        this.setDomainRoot(null);
        this.setTreasuryFile(null);
        
        deleteDomainObject();
    }
    
    public static Stream<OperationFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getOperationFileDomainObjectSet().stream();
    }
    
    public static OperationFileDomainObject copyAndAssociate(final OperationFile operationFile) {
        
        if(operationFile.getOperationFile() != null)  {
            throw new TreasuryDomainException("error.OperationFileDomainObject.already.with.copy");
        }
        
        final OperationFileDomainObject domainObject = new OperationFileDomainObject();
        
        if(operationFile.getCreationDate() != null) {
            domainObject.setCreationDate(operationFile.getCreationDate());
        }
        
        if(operationFile.getVersioningCreator() != null) {
            domainObject.setCreator(operationFile.getVersioningCreator());
        }
        
        domainObject.setIntegrationOperation(operationFile.getIntegrationOperation());
        domainObject.setLogIntegrationOperation(operationFile.getLogIntegrationOperation());
        
        domainObject.setTreasuryFile(operationFile);
        
        return domainObject;
    }

}
