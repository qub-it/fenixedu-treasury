package org.fenixedu.treasury.domain.forwardpayments;

import static org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory.treasuryPlatformServices;

import java.io.InputStream;
import java.util.stream.Stream;

import org.fenixedu.bennu.io.domain.IGenericFile;
import org.joda.time.DateTime;

import pt.ist.fenixframework.FenixFramework;

public class ForwardPaymentConfigurationFile extends ForwardPaymentConfigurationFile_Base implements IGenericFile {
    
    public static final String CONTENT_TYPE = "application/octet-stream";
    
    protected ForwardPaymentConfigurationFile() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }
    
    public static ForwardPaymentConfigurationFile create(final String filename, final byte[] contents) {
        final ForwardPaymentConfigurationFile file = new ForwardPaymentConfigurationFile();
        
        treasuryPlatformServices().createFile(file, filename, CONTENT_TYPE, contents);
        
        return file;
    }
    
    @Override
    public void delete() {
        setDomainRoot(null);
        getForwardPaymentConfigurationSet().clear();
        
        getTreasuryFile().delete();
        
        super.deleteDomainObject();
    }

    /* FROM IGenericFile */
    
    @Override
    public byte[] getContent() {
        return treasuryPlatformServices().getFileContent(this);
    }

    @Override
    public Long getSize() {
        return treasuryPlatformServices().getFileSize(this);
    }

    @Override
    public DateTime getCreationDate() {
        return treasuryPlatformServices().getFileCreationDate(this);
    }

    @Override
    public String getFilename() {
        return treasuryPlatformServices().getFilename(this);
    }

    @Override
    public InputStream getStream() {
        return treasuryPlatformServices().getFileStream(this);
    }

    @Override
    public String getContentType() {
        return treasuryPlatformServices().getFileContentType(this);
    }

    @Override
    public boolean isAccessible(String username) {
        throw new RuntimeException("not implemented");
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on
    
    public static Stream<ForwardPaymentConfigurationFile> findAll() {
        return FenixFramework.getDomainRoot().getVirtualTPACertificateSet().stream();
    }
    
}
