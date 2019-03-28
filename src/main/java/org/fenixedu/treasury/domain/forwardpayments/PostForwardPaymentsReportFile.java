package org.fenixedu.treasury.domain.forwardpayments;

import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

public class PostForwardPaymentsReportFile extends PostForwardPaymentsReportFile_Base {
    
    private PostForwardPaymentsReportFile(final DateTime postForwardPaymentsExecutionDate, 
            final DateTime beginDate, final DateTime endDate,
            final String filename, final byte[] content) {
        super();

        setBennu(Bennu.getInstance());
        setPostForwardPaymentsExecutionDate(postForwardPaymentsExecutionDate);
        setBeginDate(beginDate);
        setEndDate(endDate);
        
        init(filename, filename, content);
    }

    @Override
    public boolean isAccessible(final User user) {
        return isAccessible(user.getUsername());
    }
    
    public boolean isAccessible(final String username) {
        return  TreasuryAccessControlAPI.isBackOfficeMember(username);
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on
    
    public static Stream<PostForwardPaymentsReportFile> findAll() {
        return Bennu.getInstance().getPostForwardPaymentsReportFilesSet().stream();
    }

    @Atomic
    public static PostForwardPaymentsReportFile create(final DateTime postForwardPaymentsExecutionDate, final DateTime beginDate, final DateTime endDate, 
            final String filename, final byte[] content) {
        PostForwardPaymentsReportFile file = new PostForwardPaymentsReportFile(postForwardPaymentsExecutionDate, beginDate, endDate, filename, content);
        
        PostForwardPaymentsReportFileDomainObject.createFromPostForwardPaymentsReportFile(file);
        
        return file;
    }

}
