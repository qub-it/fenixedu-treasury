package org.fenixedu.treasury.domain.forwardpayments;

import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class PostForwardPaymentsReportFileDomainObject extends PostForwardPaymentsReportFileDomainObject_Base implements IGenericFile {

    public static final String CONTENT_TYPE = "application/octet-stream";
    
    private PostForwardPaymentsReportFileDomainObject() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }
    
    private PostForwardPaymentsReportFileDomainObject(final DateTime postForwardPaymentsExecutionDate, 
            final DateTime beginDate, final DateTime endDate,
            final String filename, final byte[] content) {
        this();

        setPostForwardPaymentsExecutionDate(postForwardPaymentsExecutionDate);
        setBeginDate(beginDate);
        setEndDate(endDate);
        
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
        
        services.createFile(this, filename, CONTENT_TYPE, content);
    }

    
    @Override
    public boolean isAccessible(final String username) {
        return  TreasuryAccessControlAPI.isBackOfficeMember(username);
    }
    
    @Override
    public void delete() {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
        
        setDomainRoot(null);
        
        // services.deleteFile(this);
        
        super.deleteDomainObject();
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on
    
    public static Stream<PostForwardPaymentsReportFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getPostForwardPaymentsReportFileDomainObjectsSet().stream();
    }
    
    public static Optional<PostForwardPaymentsReportFileDomainObject> findUniqueByPostForwardPaymentsReportFile(final PostForwardPaymentsReportFile file) {
        return findAll().filter(o -> o.getTreasuryFile() == file).findFirst();
    }

//    @Atomic
//    public static PostForwardPaymentsReportFile create(final DateTime postForwardPaymentsExecutionDate, final DateTime beginDate, final DateTime endDate, 
//            final String filename, final byte[] content) {
//        return new PostForwardPaymentsReportFile(postForwardPaymentsExecutionDate, beginDate, endDate, filename, content);
//    }

    public static PostForwardPaymentsReportFileDomainObject createFromPostForwardPaymentsReportFile(final PostForwardPaymentsReportFile file) {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        final PostForwardPaymentsReportFileDomainObject result = new PostForwardPaymentsReportFileDomainObject();
        
        result.setPostForwardPaymentsExecutionDate(file.getPostForwardPaymentsExecutionDate());
        result.setBeginDate(file.getBeginDate());
        result.setEndDate(file.getEndDate());
        
        result.setTreasuryFile(file);
        result.setCreationDate(file.getCreationDate());
        result.setCreator(services.versioningCreatorUsername(file));
        result.setFileId(file.getExternalId());
        
        return result;
    }
    
}
