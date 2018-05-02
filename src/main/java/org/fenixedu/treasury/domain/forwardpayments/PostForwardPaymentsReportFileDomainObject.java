package org.fenixedu.treasury.domain.forwardpayments;

import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class PostForwardPaymentsReportFileDomainObject extends PostForwardPaymentsReportFileDomainObject_Base {
    
    private PostForwardPaymentsReportFileDomainObject(final DateTime postForwardPaymentsExecutionDate, 
            final DateTime beginDate, final DateTime endDate) {
        super();

        setDomainRoot(FenixFramework.getDomainRoot());
        setPostForwardPaymentsExecutionDate(postForwardPaymentsExecutionDate);
        setBeginDate(beginDate);
        setEndDate(endDate);
    }

    public boolean isAccessible(final User user) {
        return TreasuryAccessControlAPI.isBackOfficeMember(user);
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on
    
    public static Stream<PostForwardPaymentsReportFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getPostForwardPaymentsReportFilesDomainObjectSet().stream();
    }
    
    public static PostForwardPaymentsReportFileDomainObject copyAndAssociate(final PostForwardPaymentsReportFile file) {
        
        DateTime postForwardPaymentsExecutionDate = file.getPostForwardPaymentsExecutionDate();
        DateTime beginDate = file.getBeginDate();
        DateTime endDate = file.getEndDate();
        
        PostForwardPaymentsReportFileDomainObject domainObject = new PostForwardPaymentsReportFileDomainObject(postForwardPaymentsExecutionDate,
                beginDate, endDate);
        
        domainObject.setCreationDate(file.getCreationDate());
        domainObject.setCreator(file.getVersioningCreator());
        domainObject.setTreasuryFile(file);
        
        return domainObject;
    }
    
    
}
