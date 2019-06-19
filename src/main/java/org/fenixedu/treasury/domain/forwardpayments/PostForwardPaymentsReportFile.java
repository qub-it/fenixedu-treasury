package org.fenixedu.treasury.domain.forwardpayments;


import java.util.stream.Stream;

import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class PostForwardPaymentsReportFile extends PostForwardPaymentsReportFile_Base implements IGenericFile {

    public static final String CONTENT_TYPE = "application/octet-stream";

    private PostForwardPaymentsReportFile() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
        setCreationDate(new DateTime());
    }

    private PostForwardPaymentsReportFile(final DateTime postForwardPaymentsExecutionDate, final DateTime beginDate,
            final DateTime endDate, final String filename, final byte[] content) {
        this();

        setPostForwardPaymentsExecutionDate(postForwardPaymentsExecutionDate);
        setBeginDate(beginDate);
        setEndDate(endDate);

        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        services.createFile(this, filename, CONTENT_TYPE, content);
    }

    @Override
    public boolean isAccessible(final String username) {
        return TreasuryAccessControlAPI.isBackOfficeMember(username);
    }

    @Override
    public void delete() {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        setDomainRoot(null);

        services.deleteFile(this);

        super.deleteDomainObject();
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
    public static PostForwardPaymentsReportFile create(final DateTime postForwardPaymentsExecutionDate, final DateTime beginDate,
            final DateTime endDate, final String filename, final byte[] content) {
        PostForwardPaymentsReportFile file =
                new PostForwardPaymentsReportFile(postForwardPaymentsExecutionDate, beginDate, endDate, filename, content);

        return file;
    }

}
