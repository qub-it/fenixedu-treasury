package org.fenixedu.treasury.domain.paymentcodes;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.document.FinantialDocumentEntry;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.event.TreasuryEvent;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;

public class MultipleEntriesPaymentCode extends MultipleEntriesPaymentCode_Base {

    protected MultipleEntriesPaymentCode(final Set<DebitEntry> debitNoteEntries, final PaymentReferenceCode paymentReferenceCode,
            final boolean valid) {
        super();
        init(debitNoteEntries, paymentReferenceCode, valid);
    }

    protected void init(final Set<DebitEntry> debitNoteEntries, final PaymentReferenceCode paymentReferenceCode,
            final boolean valid) {
        getInvoiceEntriesSet().addAll(debitNoteEntries);
        setPaymentReferenceCode(paymentReferenceCode);
        setValid(valid);
        checkRules();
    }

    private void checkRules() {
        if (getPaymentReferenceCode() == null) {
            throw new TreasuryDomainException("error.MultipleEntriesPaymentCode.paymentReferenceCode.required");
        }

        // Is associated with one debit entry
        if (getInvoiceEntriesSet().isEmpty()) {
            throw new TreasuryDomainException("error.MultipleEntriesPaymentCode.debitEntries.empty");
        }

        final DebtAccount debtAccount = getReferenceDebtAccount();
        for (final InvoiceEntry invoiceEntry : getInvoiceEntriesSet()) {
            final DebitEntry debitEntry = (DebitEntry) invoiceEntry;

            // Ensure all debit entries are the same debt account
            if (debitEntry.getDebtAccount() != debtAccount) {
                throw new TreasuryDomainException("error.MultipleEntriesPaymentCode.debit.entry.not.same.debt.account");
            }

            // Ensure debit entries have payable amount
            if (!Constants.isGreaterThan(debitEntry.getOpenAmount(), BigDecimal.ZERO)) {
                throw new TreasuryDomainException(
                        "error.MultipleEntriesPaymentCode.debit.entry.open.amount.must.be.greater.than.zero");
            }

            // Ensure debit entries are closed in finantial documents
            if (!(debitEntry.getFinantialDocument() != null && debitEntry.getFinantialDocument().isClosed())) {
                throw new TreasuryDomainException(
                        "error.MultipleEntriesPaymentCode.debit.entry.not.finantial.document.nor.closed");
            }

            // Ensure that there is only one payment code active reference for debit entry

            if (FinantialDocumentPaymentCode.findNewByFinantialDocument(debitEntry.getFinantialDocument()).count() > 0
                    || FinantialDocumentPaymentCode.findUsedByFinantialDocument(debitEntry.getFinantialDocument()).count() > 0) {
                throw new TreasuryDomainException(
                        "error.MultipleEntriesPaymentCode.debit.entry.finantial.with.active.payment.code", debitEntry
                                .getFinantialDocument().getUiDocumentNumber());
            }

            final long activePaymentCodesOnDebitEntryCount =
                    MultipleEntriesPaymentCode.findNewByDebitEntry(debitEntry).count()
                            + MultipleEntriesPaymentCode.findUsedByDebitEntry(debitEntry).count();

            if (activePaymentCodesOnDebitEntryCount > 1) {
                throw new TreasuryDomainException("error.MultipleEntriesPaymentCode.debit.entry.with.active.payment.code",
                        debitEntry.getDescription(), debitEntry.getFinantialDocument().getUiDocumentNumber());
            }
        }
    }

    @Override
    public boolean isMultipleEntriesPaymentCode() {
        return true;
    }

    @Override
    protected DocumentNumberSeries getDocumentSeriesForPayments() {
        return this.getPaymentReferenceCode().getPaymentCodePool().getDocumentSeriesForPayments();
    }

    @Override
    public DebtAccount getReferenceDebtAccount() {
        return getInvoiceEntriesSet().iterator().next().getDebtAccount();
    }

    @Override
    public String getDescription() {
        final StringBuilder builder = new StringBuilder();
        for (FinantialDocumentEntry entry : getOrderedInvoiceEntries()) {
            builder.append(entry.getDescription()).append("\n");
        }
        return builder.toString();
    }

    @Override
    public SettlementNote processPayment(final User person, final BigDecimal amountToPay, final DateTime whenRegistered,
            final String sibsTransactionId, final String comments) {

        Set<InvoiceEntry> invoiceEntriesToPay =
                this.getInvoiceEntriesSet().stream().sorted((x, y) -> y.getOpenAmount().compareTo(x.getOpenAmount()))
                        .collect(Collectors.toSet());

        return internalProcessPayment(person, amountToPay, whenRegistered, sibsTransactionId, comments, invoiceEntriesToPay);
    }

    public static Comparator<DebitEntry> COMPARE_BY_EXTERNAL_ID = new Comparator<DebitEntry>() {

        @Override
        public int compare(final DebitEntry o1, final DebitEntry o2) {
            return o1.getExternalId().compareTo(o2.getExternalId());
        }

    };

    public TreeSet<DebitEntry> getOrderedInvoiceEntries() {
        final TreeSet<DebitEntry> result = new TreeSet<DebitEntry>(COMPARE_BY_EXTERNAL_ID);
        result.addAll(getInvoiceEntriesSet().stream().map(DebitEntry.class::cast).collect(Collectors.<DebitEntry> toSet()));

        return result;
    }

    @Override
    protected DocumentNumberSeries getDocumentSeriesInterestDebits() {
        return DocumentNumberSeries.find(FinantialDocumentType.findForDebitNote(), this.getPaymentReferenceCode()
                .getPaymentCodePool().getDocumentSeriesForPayments().getSeries());
    }

    @Override
    public boolean isPaymentCodeFor(final TreasuryEvent event) {
        return getInvoiceEntriesSet().stream().map(DebitEntry.class::cast)
                .anyMatch(x -> x.getTreasuryEvent() != null && x.getTreasuryEvent().equals(event));
    }

    @Override
    protected void checkForDeletionBlockers(Collection<String> blockers) {
        super.checkForDeletionBlockers(blockers);

        //add more logical tests for checking deletion rules
        //if (getXPTORelation() != null)
        //{
        //    blockers.add(BundleUtil.getString(Bundle.APPLICATION, "error.MultipleEntriesPaymentCode.cannot.be.deleted"));
        //}
    }

    @Atomic
    public void delete() {
        TreasuryDomainException.throwWhenDeleteBlocked(getDeletionBlockers());

        if (!isDeletable()) {
            throw new TreasuryDomainException("error.MultipleEntriesPaymentCode.cannot.delete");
        }

        deleteDomainObject();
    }

    private boolean isDeletable() {
        return false;
    }

    @Override
    public LocalDate getDueDate() {
        return getInvoiceEntriesSet().stream().map(InvoiceEntry::getDueDate).sorted().findFirst().orElse(null);
    }
    
    @Atomic
    public static MultipleEntriesPaymentCode create(final Set<DebitEntry> debitNoteEntries,
            final PaymentReferenceCode paymentReferenceCode, final boolean valid) {
        return new MultipleEntriesPaymentCode(debitNoteEntries, paymentReferenceCode, valid);
    }

    // @formatter: off
    /************
     * SERVICES *
     ************/
    // @formatter: on

    public static Stream<MultipleEntriesPaymentCode> findAll(FinantialInstitution finantialInstitution) {
        Set<MultipleEntriesPaymentCode> entries = new HashSet<MultipleEntriesPaymentCode>();
        for (PaymentCodePool pool : finantialInstitution.getPaymentCodePoolsSet()) {
            for (PaymentReferenceCode code : pool.getPaymentReferenceCodesSet()) {
                if (code.getTargetPayment() != null && code.getTargetPayment() instanceof MultipleEntriesPaymentCode) {
                    entries.add((MultipleEntriesPaymentCode) code.getTargetPayment());
                }
            }
        }
        return entries.stream();
    }

    public static Stream<MultipleEntriesPaymentCode> find(final DebitEntry debitEntry) {
        return debitEntry.getPaymentCodesSet().stream();
    }

    public static Stream<MultipleEntriesPaymentCode> findByValid(FinantialInstitution finantialInstitution, final boolean valid) {
        return findAll(finantialInstitution).filter(i -> valid == i.getValid());
    }

    public static Stream<MultipleEntriesPaymentCode> findNewByDebitEntry(final DebitEntry debitEntry) {
        return find(debitEntry).filter(p -> p.getPaymentReferenceCode().isNew());
    }

    public static Stream<MultipleEntriesPaymentCode> findUsedByDebitEntry(final DebitEntry debitEntry) {
        return find(debitEntry).filter(p -> p.getPaymentReferenceCode().isUsed());
    }

}
