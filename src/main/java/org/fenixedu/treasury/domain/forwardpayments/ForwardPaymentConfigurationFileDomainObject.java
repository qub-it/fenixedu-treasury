package org.fenixedu.treasury.domain.forwardpayments;

import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;

import pt.ist.fenixframework.FenixFramework;

public class ForwardPaymentConfigurationFileDomainObject extends ForwardPaymentConfigurationFileDomainObject_Base
        implements IGenericFile {

    public static final String CONTENT_TYPE = "application/octet-stream";

    protected ForwardPaymentConfigurationFileDomainObject() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

    @Override
    public boolean isAccessible(final String username) {
        return TreasuryAccessControlAPI.isManager(username);
    }

//    public static ForwardPaymentConfigurationFileDomainObject create(final String filename, final byte[] contents) {
//        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
//
//        final ForwardPaymentConfigurationFileDomainObject file = new ForwardPaymentConfigurationFileDomainObject();
//        
//        services.createFile(file, filename, CONTENT_TYPE, contents);
//        
//        return file;
//    }

    public static ForwardPaymentConfigurationFileDomainObject createFromForwardPaymentConfigurationFile(
            ForwardPaymentConfigurationFile file) {
        final ForwardPaymentConfigurationFileDomainObject result = new ForwardPaymentConfigurationFileDomainObject();

        result.setCreationDate(file.getCreationDate());
        result.setCreator(result.getCreator());
        result.setFileId(result.getExternalId());
        result.setTreasuryFile(file);

        return result;
    }
    
    public static Stream<ForwardPaymentConfigurationFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getVirtualTPACertificateDomainObjectSet().stream();
    }

    public static Optional<ForwardPaymentConfigurationFileDomainObject> findUniqueFromForwardPaymentConfigurationFile(
            final ForwardPaymentConfigurationFile file) {
        return findAll().filter(o -> o.getTreasuryFile() == file).findFirst();
    }

    @Override
    public void delete() {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        setDomainRoot(null);

        // services.deleteFile(this);

        super.deleteDomainObject();
    }

}
