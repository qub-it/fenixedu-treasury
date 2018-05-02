package org.fenixedu.treasury.domain.forwardpayments;

import static org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory.treasuryPlatformServices;

import java.io.InputStream;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class PostForwardPaymentsReportFile extends PostForwardPaymentsReportFile_Base implements IGenericFile {
    
    public static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    
    private PostForwardPaymentsReportFile(final DateTime postForwardPaymentsExecutionDate, 
            final DateTime beginDate, final DateTime endDate,
            final String filename, final byte[] content) {
        super();
        
        setDomainRoot(FenixFramework.getDomainRoot());
        
        setPostForwardPaymentsExecutionDate(postForwardPaymentsExecutionDate);
        setBeginDate(beginDate);
        setEndDate(endDate);
        
        treasuryPlatformServices().createFile(this, filename, CONTENT_TYPE, content);
    }
    
    @Override
    public void delete() {
        
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
        return TreasuryAccessControlAPI.isBackOfficeMember(User.findByUsername(username));
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on
    
    public static Stream<PostForwardPaymentsReportFile> findAll() {
        return FenixFramework.getDomainRoot().getPostForwardPaymentsReportFilesSet().stream();
    }

    @Atomic
    public static PostForwardPaymentsReportFile create(final DateTime postForwardPaymentsExecutionDate, final DateTime beginDate, final DateTime endDate, 
            final String filename, final byte[] content) {
        PostForwardPaymentsReportFile result = new PostForwardPaymentsReportFile(postForwardPaymentsExecutionDate, beginDate, endDate, filename, content);
        
        return result;
    }

}
