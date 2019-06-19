package org.fenixedu.treasury.domain.forwardpayments;

import java.util.stream.Stream;

import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.joda.time.DateTime;

import pt.ist.fenixframework.FenixFramework;

public class ForwardPaymentConfigurationFile extends ForwardPaymentConfigurationFile_Base implements IGenericFile {

    public static final String CONTENT_TYPE = "application/octet-stream";

    public ForwardPaymentConfigurationFile() {
        super();
        
        setDomainRoot(FenixFramework.getDomainRoot());
        setCreationDate(new DateTime());
    }

    @Override
    public boolean isAccessible(final String username) {
        return TreasuryAccessControlAPI.isManager(username);
    }

    @Override
    public void delete() {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        setDomainRoot(null);

        services.deleteFile(this);

        super.deleteDomainObject();
    }
    
    public static ForwardPaymentConfigurationFile create(final String filename, final byte[] contents) {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        final ForwardPaymentConfigurationFile file = new ForwardPaymentConfigurationFile();
        services.createFile(file, filename, CONTENT_TYPE, contents);

        return file;
    }

    public static Stream<ForwardPaymentConfigurationFile> findAll() {
        return FenixFramework.getDomainRoot().getVirtualTPACertificateSet().stream();
    }

}
