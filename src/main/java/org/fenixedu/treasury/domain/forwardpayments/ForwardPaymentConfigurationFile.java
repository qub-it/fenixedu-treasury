package org.fenixedu.treasury.domain.forwardpayments;

import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;

public class ForwardPaymentConfigurationFile extends ForwardPaymentConfigurationFile_Base {
    
    protected ForwardPaymentConfigurationFile() {
        super();
        setBennu(Bennu.getInstance());
    }
    
    @Override
    public boolean isAccessible(User arg0) {
        return isAccessible(arg0.getUsername());
    }
    
    public boolean isAccessible(final String username) {
        return TreasuryAccessControlAPI.isManager(username);
    }
    

    public static ForwardPaymentConfigurationFile create(final String filename, final byte[] contents) {
        final ForwardPaymentConfigurationFile file = new ForwardPaymentConfigurationFile();
        
        file.init(filename, filename, contents);
        
        ForwardPaymentConfigurationFileDomainObject.createFromForwardPaymentConfigurationFile(file);
        
        return file;
    }
    
    public static Stream<ForwardPaymentConfigurationFile> findAll() {
        return Bennu.getInstance().getVirtualTPACertificateSet().stream();
    }
    
    @Override
    public void delete() {
        setBennu(null);
        
        ForwardPaymentConfigurationFileDomainObject.findUniqueFromForwardPaymentConfigurationFile(this).get().delete();
        
        super.delete();
    }
    
}
