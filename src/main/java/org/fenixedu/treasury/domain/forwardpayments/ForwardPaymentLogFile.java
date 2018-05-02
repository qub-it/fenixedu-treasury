package org.fenixedu.treasury.domain.forwardpayments;

import static org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory.treasuryPlatformServices;

import java.io.InputStream;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.joda.time.DateTime;

import pt.ist.fenixframework.FenixFramework;

public class ForwardPaymentLogFile extends ForwardPaymentLogFile_Base implements IGenericFile {

    public static final String CONTENT_TYPE = "text/plain";
    
    private ForwardPaymentLogFile() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

    private ForwardPaymentLogFile(final String fileName, final byte[] content) {
        this();
        
        treasuryPlatformServices().createFile(this, fileName, CONTENT_TYPE, content);
        
    }

    public String getContentAsString() {
        if(getContent() != null) {
            return new String(getContent());
        }
        
        return null;
    }
    
    @Override
    public void delete() {
        setDomainRoot(null);
        
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

    public static Stream<ForwardPaymentLogFile> findAll() {
        return FenixFramework.getDomainRoot().getForwardPaymentLogFileSet().stream();
    }
    
    public static ForwardPaymentLogFile createForRequestBody(final ForwardPaymentLog log, final byte[] content) {
        final ForwardPaymentLogFile logFile = new ForwardPaymentLogFile(
                String.format("requestBody_%s_%s.txt", new DateTime().toString("yyyyMMddHHmmss"), log.getExternalId()), content);
        logFile.setForwardPaymentLogsForRequest(log);
        
        return logFile;
    }

    public static ForwardPaymentLogFile createForResponseBody(final ForwardPaymentLog log, final byte[] content) {
        final ForwardPaymentLogFile logFile = new ForwardPaymentLogFile(
                String.format("responseBody_%s_%s.txt", new DateTime().toString("yyyyMMddHHmmss"), log.getExternalId()), content);
        logFile.setForwardPaymentLogsForResponse(log);

        return logFile;
    }

}
