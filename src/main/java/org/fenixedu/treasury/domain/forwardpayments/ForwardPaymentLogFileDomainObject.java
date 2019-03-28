package org.fenixedu.treasury.domain.forwardpayments;

import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.joda.time.DateTime;

import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;

public class ForwardPaymentLogFileDomainObject extends ForwardPaymentLogFileDomainObject_Base implements IGenericFile {

    public static final String CONTENT_TYPE = "text/plain";
    
    private ForwardPaymentLogFileDomainObject() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

//    private ForwardPaymentLogFileDomainObject(final String fileName, final byte[] content) {
//        this();
//        
//        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
//        services.createFile(this, fileName, CONTENT_TYPE, content);
//    }

    @Override
    public boolean isAccessible(final String username) {
        return TreasuryAccessControlAPI.isBackOfficeMember(username);
    }
    
    public String getContentAsString() {
        if(getContent() != null) {
            return new String(getContent());
        }
        
        return null;
    }
    
    @Override
    public void delete() {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
        
        this.setDomainRoot(null);
        this.setForwardPaymentLogsForRequest(null);
        this.setForwardPaymentLogsForResponse(null);
        
        // services.deleteFile(this);
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on

//    public static ForwardPaymentLogFileDomainObject createForRequestBody(final ForwardPaymentLog log, final byte[] content) {
//        final ForwardPaymentLogFileDomainObject logFile = new ForwardPaymentLogFileDomainObject(
//                String.format("requestBody_%s_%s.txt", new DateTime().toString("yyyyMMddHHmmss"), log.getExternalId()), content);
//        logFile.setForwardPaymentLogsForRequest(log);
//
//        return logFile;
//    }
//
//    public static ForwardPaymentLogFileDomainObject createForResponseBody(final ForwardPaymentLog log, final byte[] content) {
//        final ForwardPaymentLogFileDomainObject logFile = new ForwardPaymentLogFileDomainObject(
//                String.format("responseBody_%s_%s.txt", new DateTime().toString("yyyyMMddHHmmss"), log.getExternalId()), content);
//        logFile.setForwardPaymentLogsForResponse(log);
//
//        return logFile;
//    }

    public static ForwardPaymentLogFileDomainObject createFromForwardPaymentLogFile(final ForwardPaymentLogFile file) {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
        
        final ForwardPaymentLogFileDomainObject result = new ForwardPaymentLogFileDomainObject();
        
        result.setForwardPaymentLogsForRequest(file.getForwardPaymentLogsForRequest());
        result.setForwardPaymentLogsForResponse(file.getForwardPaymentLogsForResponse());
        result.setTreasuryFile(file);
        result.setCreationDate(file.getCreationDate());
        result.setCreator(services.versioningCreatorUsername(file));
        result.setFileId(file.getExternalId());
        
        return result;
    }
    
    public static Stream<ForwardPaymentLogFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getForwardPaymentLogFileDomainObjectsSet().stream();
    }
    
    public static Optional<ForwardPaymentLogFileDomainObject> findUniqueByForwardPaymentLogFile(final ForwardPaymentLogFile file) {
        return findAll().filter(o -> o.getTreasuryFile() == file).findFirst();
    }
    
}
