package org.fenixedu.treasury.domain.document.reimbursement;

import java.util.Comparator;
import java.util.stream.Stream;

import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.joda.time.DateTime;

import com.google.common.base.Strings;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class ReimbursementProcessStateLog extends ReimbursementProcessStateLog_Base {

    public static final Comparator<ReimbursementProcessStateLog> COMPARE_BY_VERSIONING_DATE =
            new Comparator<ReimbursementProcessStateLog>() {

                @Override
                public int compare(final ReimbursementProcessStateLog o1, final ReimbursementProcessStateLog o2) {
                    int c = TreasuryPlataformDependentServicesFactory.implementation().versioningCreationDate(o1)
                            .compareTo(TreasuryPlataformDependentServicesFactory.implementation().versioningCreationDate(o2));
                    return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
                }
            };

    public ReimbursementProcessStateLog() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

    protected ReimbursementProcessStateLog(final SettlementNote settlementNote,
            final ReimbursementProcessStatusType reimbursementProcessStatusType, final String statusId, final DateTime statusDate,
            final String remarks) {
        this();

        setSettlementNote(settlementNote);
        setReimbursementProcessStatusType(reimbursementProcessStatusType);
        setStatusId(statusId);
        setStatusDate(statusDate);
        setRemarks(remarks);

        checkRules();
    }

    private void checkRules() {

        if (getDomainRoot() == null) {
            throw new TreasuryDomainException("error.ReimbursementProcessStateLog.bennu.required");
        }

        if (getReimbursementProcessStatusType() == null) {
            throw new TreasuryDomainException("error.ReimbursementProcessStateLog.reimbursementProcessStatusType.required");
        }

        if (Strings.isNullOrEmpty(getStatusId())) {
            throw new TreasuryDomainException("error.ReimbursementProcessStateLog.statusId.required");
        }

        if (getStatusDate() == null) {
            throw new TreasuryDomainException("error.ReimbursementProcessStateLog.statusDate.required");
        }
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:off

    public static Stream<ReimbursementProcessStateLog> findAll() {
        return FenixFramework.getDomainRoot().getReimbursementProcessStateLogsSet().stream();
    }

    public static Stream<ReimbursementProcessStateLog> find(final SettlementNote settlementNote) {
        return settlementNote.getReimbursementProcessStateLogsSet().stream();
    }

    @Atomic
    public static ReimbursementProcessStateLog create(final SettlementNote settlementNote,
            final ReimbursementProcessStatusType reimbursementProcessStatusType, final String statusId, final DateTime statusDate,
            final String remarks) {

        return new ReimbursementProcessStateLog(settlementNote, reimbursementProcessStatusType, statusId, statusDate, remarks);
    }

}
