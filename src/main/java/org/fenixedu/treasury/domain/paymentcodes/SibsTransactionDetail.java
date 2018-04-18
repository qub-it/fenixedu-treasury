package org.fenixedu.treasury.domain.paymentcodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

public class SibsTransactionDetail extends SibsTransactionDetail_Base {

    protected SibsTransactionDetail() {
        super();
    }

    protected void init(final SibsReportFile sibsReport, final String comments, final DateTime whenProcessed,
            final DateTime whenRegistered, final java.math.BigDecimal amountPayed, final String sibsEntityReferenceCode,
            final String sibsPaymentReferenceCode, final String sibsTransactionId, final String debtAccountId,
            final String customerId, final String businessIdentification, final String fiscalNumber, final String customerName,
            final String settlementDocumentNumber) {
        setSibsReport(sibsReport);
        
        setSibsReportDomainObject(sibsReport.getSibsReportFile());
        
        setComments(comments);
        setWhenProcessed(whenProcessed);
        setWhenRegistered(whenRegistered);
        setAmountPayed(amountPayed);
        setSibsEntityReferenceCode(sibsEntityReferenceCode);
        setSibsPaymentReferenceCode(sibsPaymentReferenceCode);
        setSibsTransactionId(sibsTransactionId);
        setDebtAccountId(debtAccountId);
        setCustomerId(customerId);
        setBusinessIdentification(businessIdentification);
        setFiscalNumber(fiscalNumber);
        setCustomerName(customerName);
        setSettlementDocumentNumber(settlementDocumentNumber);

        checkRules();
    }

    private void checkRules() {
        //
        //CHANGE_ME add more busines validations
        //
        if (getSibsReport() == null) {
            throw new TreasuryDomainException("error.SibsTransactionDetail.sibsReport.required");
        }

        //CHANGE_ME In order to validate UNIQUE restrictions
        //if (findBySibsReport(getSibsReport().count()>1)
        //{
        //  throw new TreasuryDomainException("error.SibsTransactionDetail.sibsReport.duplicated");
        //} 
        //if (findByComments(getComments().count()>1)
        //{
        //  throw new TreasuryDomainException("error.SibsTransactionDetail.comments.duplicated");
        //} 
        //if (findByWhenProcessed(getWhenProcessed().count()>1)
        //{
        //  throw new TreasuryDomainException("error.SibsTransactionDetail.whenProcessed.duplicated");
        //} 
        //if (findByWhenRegistered(getWhenRegistered().count()>1)
        //{
        //  throw new TreasuryDomainException("error.SibsTransactionDetail.whenRegistered.duplicated");
        //} 
        //if (findByAmountPayed(getAmountPayed().count()>1)
        //{
        //  throw new TreasuryDomainException("error.SibsTransactionDetail.amountPayed.duplicated");
        //} 
        //if (findBySibsEntityReferenceCode(getSibsEntityReferenceCode().count()>1)
        //{
        //  throw new TreasuryDomainException("error.SibsTransactionDetail.sibsEntityReferenceCode.duplicated");
        //} 
        //if (findBySibsPaymentReferenceCode(getSibsPaymentReferenceCode().count()>1)
        //{
        //  throw new TreasuryDomainException("error.SibsTransactionDetail.sibsPaymentReferenceCode.duplicated");
        //} 
        //if (findBySibsTransactionId(getSibsTransactionId().count()>1)
        //{
        //  throw new TreasuryDomainException("error.SibsTransactionDetail.sibsTransactionId.duplicated");
        //} 
    }

    @Atomic
    public void edit(final SibsReportFile sibsReport, final String comments, final DateTime whenProcessed,
            final DateTime whenRegistered, final java.math.BigDecimal amountPayed, final String sibsEntityReferenceCode,
            final String sibsPaymentReferenceCode, final String sibsTransactionId, final String debtAccountId,
            final String customerId, final String businessIdentification, final String fiscalNumber, final String customerName,
            final String settlementDocumentNumber) {
        setSibsReport(sibsReport);

        setSibsReportDomainObject(sibsReport.getSibsReportFile());
        
        setComments(comments);
        setWhenProcessed(whenProcessed);
        setWhenRegistered(whenRegistered);
        setAmountPayed(amountPayed);
        setSibsEntityReferenceCode(sibsEntityReferenceCode);
        setSibsPaymentReferenceCode(sibsPaymentReferenceCode);
        setSibsTransactionId(sibsTransactionId);
        setDebtAccountId(debtAccountId);
        setCustomerId(customerId);
        setBusinessIdentification(businessIdentification);
        setFiscalNumber(fiscalNumber);
        setCustomerName(customerName);
        setSettlementDocumentNumber(settlementDocumentNumber);

        checkRules();
    }

    @Override
    protected void checkForDeletionBlockers(Collection<String> blockers) {
        super.checkForDeletionBlockers(blockers);

        //add more logical tests for checking deletion rules
        //if (getXPTORelation() != null)
        //{
        //    blockers.add(BundleUtil.getString(Bundle.APPLICATION, "error.SibsTransactionDetail.cannot.be.deleted"));
        //}
    }

    @Atomic
    public void delete() {
        TreasuryDomainException.throwWhenDeleteBlocked(getDeletionBlockers());
        super.setSibsReport(null);
        
        super.setSibsReportDomainObject(null);
        
        deleteDomainObject();
    }

    @Atomic
    public static SibsTransactionDetail create(final SibsReportFile sibsReport, final String comments,
            final DateTime whenProcessed, final DateTime whenRegistered, final java.math.BigDecimal amountPayed,
            final String sibsEntityReferenceCode, final String sibsPaymentReferenceCode, final String sibsTransactionId,
            final String debtAccountId, final String customerId, final String businessIdentification, final String fiscalNumber,
            final String customerName, final String settlementDocumentNumber) {
        SibsTransactionDetail sibsTransactionDetail = new SibsTransactionDetail();

        sibsTransactionDetail.init(sibsReport, comments, whenProcessed, whenRegistered, amountPayed, sibsEntityReferenceCode,
                sibsPaymentReferenceCode, sibsTransactionId, debtAccountId, customerId, businessIdentification, fiscalNumber,
                customerName, settlementDocumentNumber);
        return sibsTransactionDetail;
    }

    // @formatter: off
    /************
     * SERVICES *
     ************/
    // @formatter: on

    public static Stream<SibsTransactionDetail> findAll() {

        List<SibsTransactionDetail> allDetails = new ArrayList<SibsTransactionDetail>();
        List<SibsReportFile> reports = SibsReportFile.findAll().collect(Collectors.toList());
        for (SibsReportFile report : reports) {
            allDetails.addAll(report.getSibsTransactionsSet());
        }
        return allDetails.stream();
    }

    public static Stream<SibsTransactionDetail> findBySibsReport(final SibsReportFile sibsReport) {
        return sibsReport.getSibsTransactionsSet().stream();
    }

    public static Stream<SibsTransactionDetail> findByComments(final String comments) {
        return findAll().filter(i -> comments.equalsIgnoreCase(i.getComments()));
    }

    public static Stream<SibsTransactionDetail> findByWhenProcessed(final DateTime whenProcessed) {
        return findAll().filter(i -> whenProcessed.equals(i.getWhenProcessed()));
    }

    public static Stream<SibsTransactionDetail> findByWhenRegistered(final DateTime whenRegistered) {
        return findAll().filter(i -> whenRegistered.equals(i.getWhenRegistered()));
    }

    public static Stream<SibsTransactionDetail> findByAmountPayed(final java.math.BigDecimal amountPayed) {
        return findAll().filter(i -> amountPayed.equals(i.getAmountPayed()));
    }

    public static Stream<SibsTransactionDetail> findBySibsEntityReferenceCode(final String sibsEntityReferenceCode) {
        return findAll().filter(i -> sibsEntityReferenceCode.equalsIgnoreCase(i.getSibsEntityReferenceCode()));
    }

    public static Stream<SibsTransactionDetail> findBySibsPaymentReferenceCode(final String sibsPaymentReferenceCode) {
        return findAll().filter(i -> sibsPaymentReferenceCode.equalsIgnoreCase(i.getSibsPaymentReferenceCode()));
    }

    public static Stream<SibsTransactionDetail> findBySibsTransactionId(final String sibsTransactionId) {
        return findAll().filter(i -> sibsTransactionId.equalsIgnoreCase(i.getSibsTransactionId()));
    }

    public static Stream<SibsTransactionDetail> findBySibsEntityAndReferenceCode(final String sibsEntityReferenceCode,
            final String sibsPaymentReferenceCode) {
        return findBySibsEntityReferenceCode(sibsEntityReferenceCode)
                .filter(i -> sibsPaymentReferenceCode.equalsIgnoreCase(i.getSibsPaymentReferenceCode()));
    }

    public static boolean isReferenceProcessingDuplicate(String referenceCode, String entityReferenceCode,
            DateTime whenRegistered) {

        return findAll().anyMatch(x -> x.getSibsEntityReferenceCode().equals(entityReferenceCode)
                && x.getSibsPaymentReferenceCode().equals(referenceCode) && x.getWhenRegistered().equals(whenRegistered));
    }

}
