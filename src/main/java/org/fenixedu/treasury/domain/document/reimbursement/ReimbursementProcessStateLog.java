package org.fenixedu.treasury.domain.document.reimbursement;

import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.joda.time.DateTime;

import com.google.common.base.Strings;

public class ReimbursementProcessStateLog extends ReimbursementProcessStateLog_Base {

    public ReimbursementProcessStateLog() {
        super();
        setBennu(Bennu.getInstance());
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

        if (getBennu() == null) {
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
        return Bennu.getInstance().getReimbursementProcessStateLogsSet().stream();
    }
    
    public static Stream<ReimbursementProcessStateLog> find(final SettlementNote settlementNote) {
        return settlementNote.getReimbursementProcessStateLogsSet().stream();
    }
    
    public static ReimbursementProcessStateLog create(final SettlementNote settlementNote, final ReimbursementProcessStatusType reimbursementProcessStatusType, final String statusId,
            final DateTime statusDate, final String remarks) {
        
        return new ReimbursementProcessStateLog(settlementNote, reimbursementProcessStatusType, statusId, statusDate, remarks);
    }
    
}
