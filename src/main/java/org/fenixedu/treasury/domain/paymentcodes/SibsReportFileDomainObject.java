package org.fenixedu.treasury.domain.paymentcodes;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.services.payments.sibs.SIBSPaymentsImporter.ProcessResult;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class SibsReportFileDomainObject extends SibsReportFileDomainObject_Base {

    public static final String CONTENT_TYPE = "text/plain";
    public static final String FILE_EXTENSION = ".idm";

    protected SibsReportFileDomainObject() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
        setCreationDate(new DateTime());
        setCreator(Authenticate.getUser().getUsername());
    }

    protected SibsReportFileDomainObject(final DateTime whenProcessedBySibs, final BigDecimal transactionsTotalAmount,
            final BigDecimal totalCost, final String displayName, final String fileName, final byte[] content) {
        this();
        this.init(whenProcessedBySibs, transactionsTotalAmount, totalCost, displayName, fileName, content);

        checkRules();
    }

    protected void init(final DateTime whenProcessedBySibs, final BigDecimal transactionsTotalAmount, final BigDecimal totalCost,
            final String displayName, final String fileName, final byte[] content) {

        setWhenProcessedBySibs(whenProcessedBySibs);
        setTransactionsTotalAmount(transactionsTotalAmount);
        setTotalCost(totalCost);

        checkRules();
    }

    private void checkRules() {
    }

    @Atomic
    public void edit(final DateTime whenProcessedBySibs, final BigDecimal transactionsTotalAmount, final BigDecimal totalCost) {
        setWhenProcessedBySibs(whenProcessedBySibs);
        setTransactionsTotalAmount(transactionsTotalAmount);
        setTotalCost(totalCost);
        
        checkRules();
    }

    public boolean isDeletable() {
        return getReferenceCodesSet().isEmpty() && getSibsTransactionsSet().isEmpty();
    }

    @Atomic
    public void delete() {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.SibsReportFileDomainObject.cannot.delete");
        }

        setDomainRoot(null);

        deleteDomainObject();
    }

    @Atomic
    public static SibsReportFileDomainObject copyAndAssociate(final SibsReportFile sibsReportFile) {
        
        if(sibsReportFile.getSibsReportFile() != null)  {
            throw new TreasuryDomainException("error.TreasuryDocumentTemplateFileDomainObject.already.with.copy");
        }
        
        final DateTime whenProcessedBySibs = sibsReportFile.getWhenProcessedBySibs();
        final BigDecimal transactionsTotalAmount = sibsReportFile.getTransactionsTotalAmount();
        
        final BigDecimal totalCost = sibsReportFile.getTotalCost(); 
        final String displayName = sibsReportFile.getDisplayName(); 
        final String fileName = sibsReportFile.getFilename(); 
        
        final SibsReportFileDomainObject domainObject = new SibsReportFileDomainObject(whenProcessedBySibs, transactionsTotalAmount, totalCost, displayName, fileName, null);

        domainObject.setCreationDate(sibsReportFile.getCreationDate());
        domainObject.setCreator(sibsReportFile.getVersioningCreator());
        domainObject.setInfoLog(sibsReportFile.getInfoLog());
        domainObject.setErrorLog(sibsReportFile.getErrorLog());
        domainObject.getSibsTransactionsSet().addAll(sibsReportFile.getSibsTransactionsSet());
        
        domainObject.setTreasuryFile(sibsReportFile);
        
        return domainObject;
    }

    @Atomic
    public void updateLogMessages(ProcessResult result) {
        StringBuilder build = new StringBuilder();
        for (String s : result.getErrorMessages()) {
            build.append(s + "\n");
        }
        this.setErrorLog(build.toString());
        build = new StringBuilder();
        for (String s : result.getActionMessages()) {
            build.append(s + "\n");
        }
        this.setInfoLog(build.toString());
    }
    
    public static Stream<SibsReportFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getSibsReportFilesDomainObjectSet().stream();
    }
    
}
