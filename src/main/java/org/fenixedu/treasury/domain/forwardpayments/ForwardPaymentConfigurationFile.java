package org.fenixedu.treasury.domain.forwardpayments;

import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.domain.accesscontrol.TreasuryAccessControl;

public class ForwardPaymentConfigurationFile extends ForwardPaymentConfigurationFile_Base {
    
    protected ForwardPaymentConfigurationFile() {
        super();
        setBennu(Bennu.getInstance());
    }
    
    @Override
    public boolean isAccessible(User arg0) {
        return TreasuryAccessControl.getInstance().isManager(arg0);
    }

    public static ForwardPaymentConfigurationFile create(final String filename, final byte[] contents) {
        final ForwardPaymentConfigurationFile file = new ForwardPaymentConfigurationFile();
        
        file.init(filename, filename, contents);
        
        ForwardPaymentConfigurationFileDomainObject.copyAndAssociate(file);
        
        return file;
    }
    
    @Override
    public void delete() {
        if(getVirtualTPACertificate() != null) {
            getVirtualTPACertificate().delete(); 
        }

        setBennu(null);
        super.delete();
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on
    
    
    public static Stream<ForwardPaymentConfigurationFile> findAll() {
        return Bennu.getInstance().getVirtualTPACertificateSet().stream();
    }
    
}
