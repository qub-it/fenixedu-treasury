package org.fenixedu.treasury.domain.forwardpayments;

import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.treasury.domain.accesscontrol.TreasuryAccessControl;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.joda.time.DateTime;

import pt.ist.fenixframework.FenixFramework;

public class ForwardPaymentConfigurationFileDomainObject extends ForwardPaymentConfigurationFileDomainObject_Base {
    
    protected ForwardPaymentConfigurationFileDomainObject() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
        setCreationDate(new DateTime());
        setCreator(Authenticate.getUser().getUsername());
    }
    
    public boolean isAccessible(User arg0) {
        return TreasuryAccessControl.getInstance().isManager(arg0);
    }
    
    public void delete() {
        setTreasuryFile(null);
        setDomainRoot(null);
        
        deleteDomainObject();
    }

    public static Stream<ForwardPaymentConfigurationFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getVirtualTPACertificateDomainObjectSet().stream();
    }
    
    public static ForwardPaymentConfigurationFileDomainObject copyAndAssociate(ForwardPaymentConfigurationFile forwardPaymentConfigurationFile) {

        if(forwardPaymentConfigurationFile.getVirtualTPACertificate() != null)  {
            throw new TreasuryDomainException("error.ForwardPaymentConfigurationFileDomainObject.already.with.copy");
        }
        
        final ForwardPaymentConfigurationFileDomainObject domainObject = new ForwardPaymentConfigurationFileDomainObject();
        
        domainObject.setCreationDate(forwardPaymentConfigurationFile.getCreationDate());
        domainObject.setCreator(forwardPaymentConfigurationFile.getVersioningCreator());
        domainObject.setTreasuryFile(forwardPaymentConfigurationFile);
        
        domainObject.getForwardPaymentConfigurationSet().addAll(forwardPaymentConfigurationFile.getForwardPaymentConfigurationSet());
        
        return domainObject;
    }
    
}
