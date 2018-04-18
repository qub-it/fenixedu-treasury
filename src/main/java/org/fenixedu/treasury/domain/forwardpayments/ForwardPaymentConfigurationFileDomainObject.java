package org.fenixedu.treasury.domain.forwardpayments;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.domain.accesscontrol.TreasuryAccessControl;

import pt.ist.fenixframework.FenixFramework;

public class ForwardPaymentConfigurationFileDomainObject extends ForwardPaymentConfigurationFileDomainObject_Base {
    
    protected ForwardPaymentConfigurationFileDomainObject() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }
    
    public boolean isAccessible(User arg0) {
        return TreasuryAccessControl.getInstance().isManager(arg0);
    }

    public static ForwardPaymentConfigurationFileDomainObject create(ForwardPaymentConfigurationFile forwardPaymentConfigurationFile) {
        final ForwardPaymentConfigurationFileDomainObject domainObject = new ForwardPaymentConfigurationFileDomainObject();
        
        domainObject.setTreasuryFile(forwardPaymentConfigurationFile);
        
        return domainObject;
    }
    
    public void delete() {
        setTreasuryFile(null);
        setDomainRoot(null);
        
        deleteDomainObject();
    }
    
}
