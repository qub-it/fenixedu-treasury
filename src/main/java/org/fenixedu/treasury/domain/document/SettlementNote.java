/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: ricardo.pedro@qub-it.com, anil.mamede@qub-it.com
 * 
 *
 * 
 * This file is part of FenixEdu Treasury.
 *
 * FenixEdu Treasury is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Treasury is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Treasury.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.treasury.domain.document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.treasury.domain.Currency;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.reimbursement.ReimbursementProcessStateLog;
import org.fenixedu.treasury.domain.document.reimbursement.ReimbursementProcessStatusType;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.dto.SettlementNoteBean;
import org.fenixedu.treasury.dto.SettlementNoteBean.CreditEntryBean;
import org.fenixedu.treasury.dto.SettlementNoteBean.DebitEntryBean;
import org.fenixedu.treasury.dto.SettlementNoteBean.InterestEntryBean;
import org.fenixedu.treasury.dto.SettlementNoteBean.PaymentEntryBean;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

public class SettlementNote extends SettlementNote_Base {

    protected SettlementNote() {
        super();
        setBennu(Bennu.getInstance());
    }

    protected SettlementNote(final DebtAccount debtAccount, final DocumentNumberSeries documentNumberSeries,
            final DateTime documentDate, final DateTime paymentDate, final String originDocumentNumber,
            final String finantialTransactionReference) {
        this();
        init(debtAccount, documentNumberSeries, documentDate, paymentDate, originDocumentNumber, finantialTransactionReference);
    }

    protected void init(final DebtAccount debtAccount, final DocumentNumberSeries documentNumberSeries,
            final DateTime documentDate, final DateTime paymentDate, final String originDocumentNumber,
            final String finantialTransactionReference) {
        setFinantialTransactionReference(finantialTransactionReference);
        setOriginDocumentNumber(originDocumentNumber);
        if (paymentDate == null) {
            setPaymentDate(documentDate);
        } else {
            setPaymentDate(paymentDate);
        }
        super.init(debtAccount, documentNumberSeries, documentDate);

        checkRules();
    }

    @Override
    public boolean isSettlementNote() {
        return true;
    }

    public boolean isReimbursement() {
        return getDocumentNumberSeries().getFinantialDocumentType() == FinantialDocumentType.findForReimbursementNote();
    }

    protected BigDecimal checkDiferenceInAmount() {
        BigDecimal result = this.getTotalDebitAmount().subtract(this.getTotalCreditAmount());

        if (this.getAdvancedPaymentCreditNote() != null) {
            result = result.add(this.getAdvancedPaymentCreditNote().getTotalAmount());
        }
        if (Constants.isZero(this.getTotalReimbursementAmount())) {
            return this.getTotalPayedAmount().subtract(result);
        } else {
            return this.getTotalReimbursementAmount().add(result);
        }
    }

    @Override
    protected void checkRules() {
        super.checkRules();

        if (getPaymentDate().isAfter(getDocumentDate())) {
            throw new TreasuryDomainException("error.SettlementNote.invalid.payment.date.after.document.date");

        }

        if (!getDocumentNumberSeries().getFinantialDocumentType().getType().equals(FinantialDocumentTypeEnum.SETTLEMENT_NOTE)
                && !getDocumentNumberSeries().getFinantialDocumentType().getType()
                        .equals(FinantialDocumentTypeEnum.REIMBURSEMENT_NOTE)) {
            throw new TreasuryDomainException("error.FinantialDocument.finantialDocumentType.invalid");
        }

        if (isClosed() && isReimbursement() && getCurrentReimbursementProcessStatus() == null) {
            throw new TreasuryDomainException("error.integration.erp.invalid.reimbursementNote.current.status.invalid");
        }

        if (isClosed()) {
            for (final SettlementEntry settlementEntry : getSettlemetEntriesSet()) {
                if (!settlementEntry.getInvoiceEntry().isCreditNoteEntry()) {
                    continue;
                }

                if (!settlementEntry.getInvoiceEntry().getFinantialDocument().isClosed()) {
                    throw new TreasuryDomainException("error.SettlementNote.settlement.entry.for.credit.entry.not.closed");
                }
            }
        }
    }

    @Atomic
    public void edit(final FinantialDocumentType finantialDocumentType, final DebtAccount debtAccount,
            final DocumentNumberSeries documentNumberSeries, final Currency currency, final java.lang.String documentNumber,
            final org.joda.time.DateTime documentDate, final DateTime paymentDate, final java.lang.String originDocumentNumber,
            final org.fenixedu.treasury.domain.document.FinantialDocumentStateType state) {
        setFinantialDocumentType(finantialDocumentType);
        setDebtAccount(debtAccount);
        setDocumentNumberSeries(documentNumberSeries);
        setCurrency(currency);
        setDocumentNumber(documentNumber);
        setDocumentDate(documentDate);
        setDocumentDueDate(documentDate.toLocalDate());
        setOriginDocumentNumber(originDocumentNumber);
        setState(state);
        setPaymentDate(paymentDate);
        checkRules();
    }

    @Atomic
    public void updateSettlementNote(java.lang.String originDocumentNumber, String documentObservations) {
        setOriginDocumentNumber(originDocumentNumber);
        setDocumentObservations(documentObservations);

        checkRules();
    }

    @Override
    public boolean isDeletable() {
        //We can only "delete" a settlement note if is in "Preparing"
        if (this.isPreparing()) {
            //if is preparing, the AdvancedPaymentCreditNote if exists, must be deletable
            if (getAdvancedPaymentCreditNote() != null) {
                return getAdvancedPaymentCreditNote().isDeletable();
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean isAdvancePaymentSetByUser() {
        return getAdvancePaymentSetByUser();
    }

    @Override
    @Atomic
    public void delete(boolean deleteEntries) {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.SettlementNote.cannot.delete");
        }

        for (PaymentEntry paymentEntry : getPaymentEntriesSet()) {
            this.removePaymentEntries(paymentEntry);
            if (deleteEntries) {
                paymentEntry.delete();
            } else {
                paymentEntry.setSettlementNote(null);
            }
        }
        for (ReimbursementEntry entry : getReimbursementEntriesSet()) {
            this.removeReimbursementEntries(entry);
            if (deleteEntries) {
                entry.delete();
            } else {
                entry.setSettlementNote(null);
            }
        }

        if (getAdvancedPaymentCreditNote() != null) {
            getAdvancedPaymentCreditNote().delete(true);
        }
        super.delete(deleteEntries);
    }

    @Atomic
    public void processSettlementNoteCreation(SettlementNoteBean bean) {
        processInterestEntries(bean);
        closeDebitNotes(bean);
        closeCreditNotes(bean);
        if (bean.isReimbursementNote()) {
            processReimbursementEntries(bean);
        } else {
            processPaymentEntries(bean);
        }

        processAdvancePayments(bean);

        setAdvancePaymentSetByUser(bean.isAdvancePayment());
    }

    private void processAdvancePayments(SettlementNoteBean bean) {
        if (bean.isReimbursementNote()) {
            return;
        }

        if (!bean.isAdvancePayment()) {
            return;
        }

        final BigDecimal debitSum =
                bean.isReimbursementNote() ? bean.getDebtAmountWithVat().negate() : bean.getDebtAmountWithVat();
        final BigDecimal paymentSum = bean.getPaymentAmount();

        final BigDecimal availableAmount = paymentSum.subtract(debitSum);

        if (!Constants.isPositive(availableAmount)) {
            return;
        }

        final String comments = String.format("%s [%s]", Constants.bundle("label.SettlementNote.advancedpayment"),
                getPaymentDate().toString(Constants.DATE_FORMAT));

        createAdvancedPaymentCreditNote(availableAmount, comments, getExternalId());
    }

    private void processReimbursementEntries(SettlementNoteBean bean) {
        for (PaymentEntryBean paymentEntryBean : bean.getPaymentEntries()) {
            ReimbursementEntry.create(this, paymentEntryBean.getPaymentMethod(), paymentEntryBean.getPaymentAmount(),
                    paymentEntryBean.getPaymentMethodId());
        }
    }

    private void processPaymentEntries(SettlementNoteBean bean) {
        for (PaymentEntryBean paymentEntryBean : bean.getPaymentEntries()) {
            PaymentEntry.create(paymentEntryBean.getPaymentMethod(), this, paymentEntryBean.getPaymentAmount(),
                    paymentEntryBean.getPaymentMethodId());
        }
    }

    private void processInterestEntries(SettlementNoteBean bean) {

        DocumentNumberSeries debitNoteSeries = DocumentNumberSeries
                .find(FinantialDocumentType.findForDebitNote(), bean.getDebtAccount().getFinantialInstitution())
                .filter(x -> Boolean.TRUE.equals(x.getSeries().getDefaultSeries())).findFirst().orElse(null);
        if (bean.getInterestEntries().size() == 0) {
            return;
        }

        DebitNote interestDebitNote = DebitNote.create(bean.getDebtAccount(), debitNoteSeries, new DateTime());

        for (InterestEntryBean interestEntryBean : bean.getInterestEntries()) {

            DebitEntry interestDebitEntry =
                    interestEntryBean.getDebitEntry().createInterestRateDebitEntry(interestEntryBean.getInterest(),
                            bean.getDate().toDateTimeAtStartOfDay(), Optional.<DebitNote> ofNullable(interestDebitNote));

            if (interestEntryBean.isIncluded()) {
                interestDebitNote.closeDocument();
                SettlementEntry.create(interestDebitEntry, this, interestEntryBean.getInterest().getInterestAmount(),
                        interestDebitEntry.getDescription(), bean.getDate().toDateTimeAtStartOfDay(), false);
            }
        }
    }

    private void closeCreditNotes(SettlementNoteBean bean) {
        for (CreditEntryBean creditEntryBean : bean.getCreditEntries()) {
            if (creditEntryBean.isIncluded()) {
                final CreditEntry creditEntry = creditEntryBean.getCreditEntry();

                if (!creditEntry.getFinantialDocument().isClosed()) {
                    if (Constants.isLessThan(creditEntryBean.getCreditAmountWithVat(), creditEntry.getOpenAmount())) {
                        creditEntry
                                .splitCreditEntry(creditEntry.getOpenAmount().subtract(creditEntryBean.getCreditAmountWithVat()));
                    }

                    creditEntry.getFinantialDocument().closeDocument();
                }

                SettlementEntry.create(creditEntryBean, this, bean.getDate().toDateTimeAtStartOfDay());
            }
        }
    }

    private void closeDebitNotes(SettlementNoteBean bean) {
        DocumentNumberSeries debitNoteSeries = DocumentNumberSeries
                .find(FinantialDocumentType.findForDebitNote(), bean.getDebtAccount().getFinantialInstitution())
                .filter(x -> Boolean.TRUE.equals(x.getSeries().getDefaultSeries())).findFirst().orElse(null);

        List<DebitEntry> untiedDebitEntries = new ArrayList<DebitEntry>();
        for (DebitEntryBean debitEntryBean : bean.getDebitEntries()) {
            if (debitEntryBean.isIncluded()) {
                if (debitEntryBean.getDebitEntry().getFinantialDocument() == null) {
                    untiedDebitEntries.add(debitEntryBean.getDebitEntry());
                } else {
                    if (!debitEntryBean.getDebitEntry().getFinantialDocument().isClosed()) {
                        debitEntryBean.getDebitEntry().getFinantialDocument().closeDocument();
                    }
                }
                SettlementEntry.create(debitEntryBean, this, bean.getDate().toDateTimeAtStartOfDay());
            }
        }
        if (untiedDebitEntries.size() != 0) {
            DebitNote debitNote =
                    DebitNote.create(bean.getDebtAccount(), debitNoteSeries, bean.getDate().toDateTimeAtStartOfDay());
            debitNote.addDebitNoteEntries(untiedDebitEntries);
            debitNote.closeDocument();
        }
    }

    @Atomic
    public static SettlementNote create(final DebtAccount debtAccount, final DocumentNumberSeries documentNumberSeries,
            final DateTime documentDate, final DateTime paymentDate, final String originDocumentNumber,
            final String finantialTransactionReference) {
        SettlementNote settlementNote = new SettlementNote(debtAccount, documentNumberSeries, documentDate, paymentDate,
                originDocumentNumber, finantialTransactionReference);

        return settlementNote;
    }

    public static Stream<SettlementNote> findAll() {
        return Bennu.getInstance().getFinantialDocumentsSet().stream().filter(x -> x instanceof SettlementNote)
                .map(SettlementNote.class::cast);
    }

    public static Stream<SettlementNote> findByFinantialDocumentType(final FinantialDocumentType finantialDocumentType) {
        return findAll().filter(i -> finantialDocumentType.equals(i.getFinantialDocumentType()));
    }

    public static Stream<SettlementNote> findByDebtAccount(final DebtAccount debtAccount) {
        return findAll().filter(i -> debtAccount.equals(i.getDebtAccount()));
    }

    public static Stream<SettlementNote> findByDocumentNumberSeries(final DocumentNumberSeries documentNumberSeries) {
        return findAll().filter(i -> documentNumberSeries.equals(i.getDocumentNumberSeries()));
    }

    public static Stream<SettlementNote> findByCurrency(final Currency currency) {
        return findAll().filter(i -> currency.equals(i.getCurrency()));
    }

    public static Stream<SettlementNote> findByDocumentNumber(final java.lang.String documentNumber) {
        return findAll().filter(i -> documentNumber.equalsIgnoreCase(i.getDocumentNumber()));
    }

    public static Stream<SettlementNote> findByDocumentDate(final org.joda.time.DateTime documentDate) {
        return findAll().filter(i -> documentDate.equals(i.getDocumentDate()));
    }

    public static Stream<SettlementNote> findByDocumentDueDate(final org.joda.time.DateTime documentDueDate) {
        return findAll().filter(i -> documentDueDate.equals(i.getDocumentDueDate()));
    }

    public static Stream<SettlementNote> findByOriginDocumentNumber(final java.lang.String originDocumentNumber) {
        return findAll().filter(i -> originDocumentNumber.equalsIgnoreCase(i.getOriginDocumentNumber()));
    }

    public static Stream<SettlementNote> findByState(
            final org.fenixedu.treasury.domain.document.FinantialDocumentStateType state) {
        return findAll().filter(i -> state.equals(i.getState()));
    }

    public Stream<SettlementEntry> getSettlemetEntries() {
        return this.getFinantialDocumentEntriesSet().stream().map(SettlementEntry.class::cast);
    }

    public Set<SettlementEntry> getSettlemetEntriesSet() {
        return getSettlemetEntries().collect(Collectors.toSet());
    }

    @Override
    public Set<FinantialDocument> findRelatedDocuments(Set<FinantialDocument> documentsBaseList,
            Boolean includeAnulledDocuments) {

        documentsBaseList.add(this);

        for (SettlementEntry entry : getSettlemetEntriesSet()) {
            if (entry.getInvoiceEntry() != null && entry.getInvoiceEntry().getFinantialDocument() != null) {
                if (includeAnulledDocuments == true || this.isAnnulled() == false) {
                    if (documentsBaseList.contains(entry.getInvoiceEntry().getFinantialDocument()) == false) {
                        documentsBaseList.addAll(entry.getInvoiceEntry().getFinantialDocument()
                                .findRelatedDocuments(documentsBaseList, includeAnulledDocuments));
                    }
                }
            }
        }
        return documentsBaseList;

    }

    @Atomic
    public void anullDocument(final String anulledReason, final boolean markDocumentToExport) {
        if (this.isPreparing()) {
            this.delete(true);
        } else if (this.isClosed()) {
            setState(FinantialDocumentStateType.ANNULED);
            setAnnulledReason(anulledReason);

            // Settlement note can never free entries 
            if (markDocumentToExport) {
                this.markDocumentToExport();
            }

            //if we have advanced payments, we must "anull" the "advanced payments"
            if (this.getAdvancedPaymentCreditNote() != null) {
                //only "disconnect" the advanced payment credit note
                this.setAdvancedPaymentCreditNote(null);
                // this.getAdvancedPaymentCreditNote().anullDocument(freeEntries, anulledReason);
            }

            checkRules();
        } else {
            throw new TreasuryDomainException(
                    BundleUtil.getString(Constants.BUNDLE, "error.FinantialDocumentState.invalid.state.change.request"));
        }

    }

    public BigDecimal getTotalDebitAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (SettlementEntry entry : this.getSettlemetEntriesSet()) {
            if (entry.getInvoiceEntry().isDebitNoteEntry()) {
                total = total.add(entry.getTotalAmount());
            }
        }
        return total;
    }

    @Override
    public void closeDocument(boolean markDocumentToExport) {

        //Validate the settlement entries can be used, since multiple entries to the same settlement Note
        for (SettlementEntry settlementEntry : getSettlemetEntriesSet()) {
            if (Constants.isGreaterThan(settlementEntry.getAmount(), settlementEntry.getInvoiceEntry().getOpenAmount())) {
                throw new TreasuryDomainException("error.SettlementNote.invalid.settlement.entry.amount.for.invoice.entry");
            }
        }

        if (Constants.isZero(checkDiferenceInAmount()) == false) {
            throw new TreasuryDomainException("error.SettlementNote.invalid.amounts.in.settlement.note");
        }

        if (this.getAdvancedPaymentCreditNote() != null) {
            this.getAdvancedPaymentCreditNote().closeDocument();
        }

        if (isReimbursement()) {
            processReimbursementStateChange(ReimbursementProcessStatusType.findUniqueByInitialStatus().get(),
                    String.valueOf(getDocumentDate().getYear()), new DateTime());
        }

        super.closeDocument(markDocumentToExport);

        checkRules();
    }

    @Atomic
    public void processReimbursementStateChange(final ReimbursementProcessStatusType reimbursementStatus,
            final String exerciseYear, final DateTime reimbursementStatusDate) {

        if (reimbursementStatus == null) {
            throw new TreasuryDomainException("error.integration.erp.invalid.reimbursementStatus");
        }

        if (!isReimbursement()) {
            throw new TreasuryDomainException("error.integration.erp.invalid.settlementNote");
        }

        if (!isClosed() && !reimbursementStatus.isInitialStatus()) {
            throw new TreasuryDomainException("error.integration.erp.invalid.reimbursementNote.state");
        }

        if (!reimbursementStatus.isInitialStatus() && getCurrentReimbursementProcessStatus() == null) {
            throw new TreasuryDomainException("error.SettlementNote.currentReimbursementProcessStatus.invalid");
        }

        if (getCurrentReimbursementProcessStatus() != null
                && !reimbursementStatus.isAfter(getCurrentReimbursementProcessStatus())) {
            throw new TreasuryDomainException("error.integration.erp.invalid.reimbursementNote.current.status.invalid");
        }

        if (getCurrentReimbursementProcessStatus() != null && getCurrentReimbursementProcessStatus().isFinalStatus()) {
            throw new TreasuryDomainException("error.integration.erp.invalid.reimbursementNote.current.status.is.final");
        }

        setCurrentReimbursementProcessStatus(reimbursementStatus);

        ReimbursementProcessStateLog.create(this, reimbursementStatus, UUID.randomUUID().toString(), reimbursementStatusDate,
                exerciseYear);

        if (getCurrentReimbursementProcessStatus() == null) {
            throw new TreasuryDomainException("error.SettlementNote.currentReimbursementProcessStatus.invalid");
        }

        if (getCurrentReimbursementProcessStatus().isAnnuledStatus() && isClosed()) {
            anullDocument(Constants.bundle("label.ReimbursementProcessStatusType.annuled.reimbursement.by.annuled.process"),
                    false);
        }
    }

    public BigDecimal getTotalCreditAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (SettlementEntry entry : this.getSettlemetEntriesSet()) {
            if (entry.getInvoiceEntry().isCreditNoteEntry()) {
                total = total.add(entry.getTotalAmount());
            }
        }
        return total;
    }

    public BigDecimal getTotalPayedAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (PaymentEntry entry : this.getPaymentEntriesSet()) {
            total = total.add(entry.getPayedAmount());
        }
        return total;
    }

    public BigDecimal getTotalReimbursementAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (ReimbursementEntry reimbursementEntry : getReimbursementEntriesSet()) {
            total = total.add(reimbursementEntry.getReimbursedAmount());
        }
        return total;
    }

    @Override
    public BigDecimal getTotalAmount() {
        return this.getTotalDebitAmount().subtract(this.getTotalDebitAmount());
    }

    @Override
    public BigDecimal getTotalNetAmount() {
        throw new TreasuryDomainException("error.SettlementNote.totalNetAmount.not.available");
    }

    @Atomic
    public void createAdvancedPaymentCreditNote(BigDecimal availableAmount, String comments, String originalNumber) {
        if (FinantialDocumentType.findForCreditNote() == null) {
            throw new TreasuryDomainException("error.SettlementNote.non-existing.credit.note.document.type");
        }
        //Create the CreditNote for this amount and
        DocumentNumberSeries documentNumberSeries =
                DocumentNumberSeries.find(FinantialDocumentType.findForCreditNote(), this.getDocumentNumberSeries().getSeries());

        AdvancedPaymentCreditNote creditNote = AdvancedPaymentCreditNote.createCreditNoteForAdvancedPayment(documentNumberSeries,
                this.getDebtAccount(), availableAmount, this.getDocumentDate(), comments, originalNumber);

        this.setAdvancedPaymentCreditNote(creditNote);
    }

    public boolean hasAdvancedPayment() {
        return getAdvancedPaymentCreditNote() != null;
    }

    @Override
    protected boolean isDocumentEmpty() {
        if (this.getAdvancedPaymentCreditNote() != null) {
            return this.getAdvancedPaymentCreditNote().isDocumentEmpty() && this.getFinantialDocumentEntriesSet().isEmpty();
        }
        return this.getFinantialDocumentEntriesSet().isEmpty();
    }
}
