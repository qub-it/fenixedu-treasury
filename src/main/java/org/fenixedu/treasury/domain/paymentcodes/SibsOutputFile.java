package org.fenixedu.treasury.domain.paymentcodes;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.fenixedu.treasury.services.payments.sibs.outgoing.SibsOutgoingPaymentFile;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.FenixFramework;

public class SibsOutputFile extends SibsOutputFile_Base implements IGenericFile {

    public static final String CONTENT_TYPE = "text/plain";

    public SibsOutputFile() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

    public static SibsOutputFile create(FinantialInstitution finantialInstitution, DateTime lastSuccessfulSentDateTime) {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        SibsOutputFile file = new SibsOutputFile();

        try {
            StringBuilder errorsBuilder = new StringBuilder();
            byte[] paymentFileContents =
                    file.createPaymentFile(finantialInstitution, lastSuccessfulSentDateTime, errorsBuilder).getBytes("ASCII");

            services.createFile(file, file.outgoingFilename(), CONTENT_TYPE, paymentFileContents);

            file.setFinantialInstitution(finantialInstitution);
            file.setLastSuccessfulExportation(lastSuccessfulSentDateTime);
            file.setErrorLog(errorsBuilder.toString());
        } catch (Exception e) {
            StringBuilder builder = new StringBuilder();
            builder.append(e.getLocalizedMessage()).append("\n");
            for (StackTraceElement el : e.getStackTrace()) {
                builder.append(el.toString()).append("\n");
            }

            services.createFile(file, file.outgoingFilename(), CONTENT_TYPE, new byte[0]);

            file.setFinantialInstitution(finantialInstitution);
            file.setLastSuccessfulExportation(lastSuccessfulSentDateTime);
            file.setErrorLog(builder.toString());
        }

        return file;
    }

    protected String createPaymentFile(final FinantialInstitution finantialInstiution, final DateTime lastSuccessfulSentDateTime,
            final StringBuilder errorsBuilder) {
        SibsOutgoingPaymentFile sibsOutgoingPaymentFile =
                new SibsOutgoingPaymentFile(finantialInstiution.getSibsConfiguration().getSourceInstitutionId(),
                        finantialInstiution.getSibsConfiguration().getDestinationInstitutionId(),
                        finantialInstiution.getSibsConfiguration().getEntityReferenceCode(), lastSuccessfulSentDateTime);

        for (PaymentReferenceCode referenceCode : getNotPayedReferenceCodes(finantialInstiution, errorsBuilder)) {
            addCalculatedPaymentCodesFromEvent(sibsOutgoingPaymentFile, referenceCode, errorsBuilder);
        }

        this.setPrintedPaymentCodes(sibsOutgoingPaymentFile.getAssociatedPaymentCodes().exportAsString());
        invalidateOldPaymentCodes(sibsOutgoingPaymentFile, finantialInstiution, errorsBuilder);

        return sibsOutgoingPaymentFile.render();
    }

    private Set<PaymentReferenceCode> getNotPayedReferenceCodes(FinantialInstitution finantialInstitution,
            StringBuilder errorsBuilder) {
        Set<PaymentReferenceCode> result = new HashSet<PaymentReferenceCode>();
        for (PaymentCodePool pool : finantialInstitution.getPaymentCodePoolsSet()) {
            List<PaymentReferenceCode> paymentCodesToExport = pool.getPaymentCodesToExport(new LocalDate());
            result.addAll(paymentCodesToExport);
        }
        return result;
    }

    private void invalidateOldPaymentCodes(SibsOutgoingPaymentFile sibsOutgoingPaymentFile,
            FinantialInstitution finantialInstitution, StringBuilder errorsBuilder) {
        Set<PaymentReferenceCode> result = new HashSet<PaymentReferenceCode>();
        for (PaymentCodePool pool : finantialInstitution.getPaymentCodePoolsSet()) {
            List<PaymentReferenceCode> paymentCodesToExport = pool.getAnnulledPaymentCodesToExport(new LocalDate());
            result.addAll(paymentCodesToExport);
        }
        for (PaymentReferenceCode oldCode : result) {
            sibsOutgoingPaymentFile.addLine(oldCode.getReferenceCode(), BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.01),
                    new DateTime().minusDays(5).toLocalDate(), new DateTime().minusDays(5).toLocalDate());
        }

    }

    protected void addPaymentCode(final SibsOutgoingPaymentFile file, final PaymentReferenceCode paymentCode,
            StringBuilder errorsBuilder) {
        try {
            file.addAssociatedPaymentCode(paymentCode);
            file.addLine(paymentCode.getReferenceCode(), paymentCode.getMinAmount(), paymentCode.getMaxAmount(),
                    paymentCode.getBeginDate(), paymentCode.getEndDate());
        } catch (Throwable e) {
            appendToErrors(errorsBuilder, paymentCode.getExternalId(), e);
        }
    }

    private void appendToErrors(StringBuilder errorsBuilder, String externalId, Throwable e) {
        errorsBuilder.append("Error in : " + externalId + "-" + e.getLocalizedMessage()).append("\n");

        this.setErrorLog(errorsBuilder.toString());
    }

    protected void addCalculatedPaymentCodesFromEvent(final SibsOutgoingPaymentFile file,
            final PaymentReferenceCode referenceCode, StringBuilder errorsBuilder) {
        try {
            CalculatePaymentCodes thread = new CalculatePaymentCodes(referenceCode.getExternalId(), errorsBuilder, file);
            thread.start();
            thread.join();
        } catch (Throwable e) {
            appendToErrors(errorsBuilder, referenceCode.getExternalId(), e);
        }
    }

    private String outgoingFilename() {
        return String.format("SIBS-%s.txt", new DateTime().toString("dd-MM-yyyy_H_m_s"));
    }

    private class CalculatePaymentCodes extends Thread {
        private final String paymentReferenceCodeId;
        private final StringBuilder errorsBuilder;
        private final SibsOutgoingPaymentFile sibsFile;

        public CalculatePaymentCodes(String paymentReferenceCodeId, StringBuilder errorsBuilder,
                SibsOutgoingPaymentFile sibsFile) {
            this.paymentReferenceCodeId = paymentReferenceCodeId;
            this.errorsBuilder = errorsBuilder;
            this.sibsFile = sibsFile;
        }

        @Override
        @Atomic(mode = TxMode.READ)
        public void run() {
            try {
                txDo();
            } catch (Throwable e) {
                appendToErrors(errorsBuilder, paymentReferenceCodeId, e);
            }
        }

        @Atomic
        private void txDo() {
            PaymentReferenceCode referenceCode = FenixFramework.getDomainObject(paymentReferenceCodeId);

            this.sibsFile.addAssociatedPaymentCode(referenceCode);
            sibsFile.addLine(referenceCode.getReferenceCode(), referenceCode.getMinAmount(), referenceCode.getMaxAmount(),
                    referenceCode.getBeginDate(), referenceCode.getEndDate());
        }
    }

    @Override
    public boolean isAccessible(final String username) {
        return true;
    }

    protected void init(final FinantialInstitution finantialInstitution, final java.lang.String errorLog,
            final java.lang.String infoLog, final java.lang.String printedPaymentCodes) {
        setFinantialInstitution(finantialInstitution);
        setErrorLog(errorLog);
        setInfoLog(infoLog);
        setPrintedPaymentCodes(printedPaymentCodes);

        checkRules();
    }

    private void checkRules() {
        if (getFinantialInstitution() == null) {
            throw new TreasuryDomainException("error.SibsOutputFile.finantialInstitution.required");
        }
    }

    @Atomic
    public void edit(final FinantialInstitution finantialInstitution, final String errorLog, final String infoLog,
            final String printedPaymentCodes) {
        setFinantialInstitution(finantialInstitution);
        setErrorLog(errorLog);
        setInfoLog(infoLog);
        setPrintedPaymentCodes(printedPaymentCodes);

        checkRules();
    }

    @Override
    protected void checkForDeletionBlockers(Collection<String> blockers) {
        super.checkForDeletionBlockers(blockers);
    }

    @Override
    @Atomic
    public void delete() {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        TreasuryDomainException.throwWhenDeleteBlocked(getDeletionBlockers());

        setDomainRoot(null);
        setFinantialInstitution(null);

        services.deleteFile(this);

        super.deleteDomainObject();
    }

    public static Stream<SibsOutputFile> findAll() {
        return FenixFramework.getDomainRoot().getSibsOutputFilesSet().stream();
    }

    public static Stream<SibsOutputFile> findByFinantialInstitution(final FinantialInstitution finantialInstitution) {
        return finantialInstitution.getSibsOutputFilesSet().stream();
    }

    public static Stream<SibsOutputFile> findByErrorLog(final java.lang.String errorLog) {
        return findAll().filter(i -> errorLog.equalsIgnoreCase(i.getErrorLog()));
    }

    public static Stream<SibsOutputFile> findByInfoLog(final java.lang.String infoLog) {
        return findAll().filter(i -> infoLog.equalsIgnoreCase(i.getInfoLog()));
    }

    public static Stream<SibsOutputFile> findByPrintedPaymentCodes(final java.lang.String printedPaymentCodes) {
        return findAll().filter(i -> printedPaymentCodes.equalsIgnoreCase(i.getPrintedPaymentCodes()));
    }
    
}
