package org.fenixedu.treasury.services.integration.erp.ERPExternalServiceImplementation;

import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.document.reimbursement.ReimbursementProcessStatusType;
import org.joda.time.DateTime;

public class ReimbursementStateBean {

    private SettlementNote reimbursementNote;
    private ReimbursementProcessStatusType reimbursementProcessStatus;
    private String exerciseYear;
    private DateTime reimbursementStateDate;
    private boolean success;
    
    public ReimbursementStateBean(final SettlementNote reimbursementNote,
            final ReimbursementProcessStatusType reimbursementProcessStatus, final String exerciseYear,
            final DateTime reimbursementStateDate, final boolean success) {
        this.reimbursementNote = reimbursementNote;
        this.reimbursementProcessStatus = reimbursementProcessStatus;
        this.exerciseYear = exerciseYear;
        this.reimbursementStateDate = reimbursementStateDate;
        this.success = success;
    }

    // @formatter:off
    /* *****************
     * GETTERS & SETTERS
     * *****************
     */
    // @formatter:on

    public SettlementNote getReimbursementNote() {
        return reimbursementNote;
    }

    public ReimbursementProcessStatusType getReimbursementProcessStatus() {
        return reimbursementProcessStatus;
    }

    public String getExerciseYear() {
        return exerciseYear;
    }

    public DateTime getReimbursementStateDate() {
        return reimbursementStateDate;
    }
    
    public boolean isSuccess() {
        return success;
    }
}
