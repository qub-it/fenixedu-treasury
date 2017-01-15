package org.fenixedu.treasury.domain.debt.balancetransfer;

import static org.fenixedu.treasury.util.Constants.bundle;
import static org.fenixedu.treasury.util.Constants.isEqual;
import static org.fenixedu.treasury.util.Constants.isGreaterOrEqualThan;
import static org.fenixedu.treasury.util.Constants.isPositive;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import org.fenixedu.treasury.domain.Currency;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.CreditEntry;
import org.fenixedu.treasury.domain.document.CreditNote;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentEntry;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.document.Invoice;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.document.Series;
import org.fenixedu.treasury.domain.document.SettlementEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.exemption.TreasuryExemption;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import pt.ist.fenixframework.Atomic;

public class BalanceTransferService {

    private DebtAccount objectDebtAccount;
    private DebtAccount destinyDebtAccount;

    public BalanceTransferService(final DebtAccount objectDebtAccount, final DebtAccount destinyDebtAccount) {
        this.objectDebtAccount = objectDebtAccount;
        this.destinyDebtAccount = destinyDebtAccount;
    }

    @Atomic
    public void transferBalance() {
        final BigDecimal initialGlobalBalance = objectDebtAccount.getCustomer().getGlobalBalance();

        final FinantialInstitution finantialInstitution = objectDebtAccount.getFinantialInstitution();
        final Currency currency = finantialInstitution.getCurrency();
        final DocumentNumberSeries documentNumberSeries =
                DocumentNumberSeries.findUniqueDefault(FinantialDocumentType.findForDebitNote(), finantialInstitution).get();
        final DateTime now = new DateTime();

        final Set<DebitNote> pendingDebitNotes = Sets.newHashSet();
        for (final InvoiceEntry invoiceEntry : objectDebtAccount.getPendingInvoiceEntriesSet()) {

            if (invoiceEntry.isDebitNoteEntry()) {
                final DebitEntry debitEntry = (DebitEntry) invoiceEntry;
                if (debitEntry.getFinantialDocument() == null) {
                    final DebitNote debitNote = DebitNote.create(objectDebtAccount, documentNumberSeries, now);
                    debitNote.addDebitNoteEntries(Lists.newArrayList(debitEntry));
                }

                pendingDebitNotes.add((DebitNote) debitEntry.getFinantialDocument());
            }
        }

        for (final DebitNote debitNote : pendingDebitNotes) {
            transferDebitEntries(debitNote);
        }

        for (final InvoiceEntry invoiceEntry : objectDebtAccount.getPendingInvoiceEntriesSet()) {
            if (invoiceEntry.isCreditNoteEntry()) {
                transferCreditEntry((CreditEntry) invoiceEntry);
            }
        }

        final BigDecimal finalGlobalBalance = objectDebtAccount.getCustomer().getGlobalBalance();

        if (!isEqual(initialGlobalBalance, finalGlobalBalance)) {
            throw new TreasuryDomainException("error.BalanceTransferService.initial.and.final.global.balance",
                    currency.getValueFor(initialGlobalBalance), currency.getValueFor(finalGlobalBalance));
        }
    }

    private void transferCreditEntry(final CreditEntry invoiceEntry) {
        final FinantialInstitution finantialInstitution = objectDebtAccount.getFinantialInstitution();
        final Series defaultSeries = Series.findUniqueDefault(finantialInstitution).get();
        final DocumentNumberSeries settlementNoteSeries =
                DocumentNumberSeries.find(FinantialDocumentType.findForSettlementNote(), defaultSeries);
        final DateTime now = new DateTime();

        final CreditEntry creditEntry = (CreditEntry) invoiceEntry;
        final BigDecimal creditOpenAmount = creditEntry.getOpenAmount();

        final String originNumber = creditEntry.getFinantialDocument() != null
                && invoiceEntry.getFinantialDocument().isClosed() ? creditEntry.getFinantialDocument().getUiDocumentNumber() : "";
        final DebtAccount payorDebtAccount =
                creditEntry.getFinantialDocument() != null && ((Invoice) creditEntry.getFinantialDocument())
                        .isForPayorDebtAccount() ? ((Invoice) creditEntry.getFinantialDocument()).getPayorDebtAccount() : null;
        DebitEntry regulationDebitEntry = DebitNote.createBalanceTransferDebit(objectDebtAccount, now, now.toLocalDate(),
                originNumber, creditOpenAmount, payorDebtAccount, null);

        regulationDebitEntry.getFinantialDocument().closeDocument();
        CreditNote.createBalanceTransferCredit(destinyDebtAccount, now, originNumber, creditOpenAmount, payorDebtAccount,
                creditEntry.getDescription());

        final SettlementNote settlementNote =
                SettlementNote.create(objectDebtAccount, settlementNoteSeries, now, now, null, null);

        if (creditEntry.getFinantialDocument().isPreparing()) {
            creditEntry.getFinantialDocument().closeDocument();
        }

        SettlementEntry.create(regulationDebitEntry, settlementNote, regulationDebitEntry.getOpenAmount(),
                regulationDebitEntry.getDescription(), now, false);
        SettlementEntry.create(creditEntry, settlementNote, creditOpenAmount, creditEntry.getDescription(), now, false);

        settlementNote.closeDocument();
    }

    private void transferDebitEntries(final DebitNote objectDebitNote) {
        final FinantialInstitution finantialInstitution = objectDebitNote.getDebtAccount().getFinantialInstitution();
        final DocumentNumberSeries settlementNumberSeries =
                DocumentNumberSeries.findUniqueDefault(FinantialDocumentType.findForSettlementNote(), finantialInstitution).get();
        final DateTime now = new DateTime();

        if (objectDebitNote.isPreparing()) {
            anullPreparingDebitNote(objectDebitNote);
        } else if (objectDebitNote.isClosed()) {
            final DebtAccount payorDebtAccount =
                    objectDebitNote.isForPayorDebtAccount() ? objectDebitNote.getPayorDebtAccount() : null;

            final DebitNote destinyDebitNote = DebitNote.create(destinyDebtAccount, payorDebtAccount,
                    objectDebitNote.getDocumentNumberSeries(), now, now.toLocalDate(), objectDebitNote.getUiDocumentNumber());

            final SettlementNote settlementNote =
                    SettlementNote.create(objectDebtAccount, settlementNumberSeries, now, now, null, null);
            for (final FinantialDocumentEntry objectEntry : objectDebitNote.getFinantialDocumentEntriesSet()) {
                if (!isPositive(((DebitEntry) objectEntry).getOpenAmount())) {
                    continue;
                }

                final DebitEntry debitEntry = (DebitEntry) objectEntry;

                final BigDecimal openAmount = debitEntry.getOpenAmount();
                final BigDecimal availableCreditAmount = debitEntry.getAvailableAmountForCredit();

                if (isGreaterOrEqualThan(availableCreditAmount, openAmount)) {
                    final CreditEntry newCreditEntry =
                            debitEntry.createCreditEntry(now, debitEntry.getDescription(), null, openAmount, null, null);
                    newCreditEntry.getFinantialDocument().closeDocument();

                    SettlementEntry.create(debitEntry, settlementNote, openAmount, debitEntry.getDescription(), now, false);
                    SettlementEntry.create(newCreditEntry, settlementNote, openAmount, newCreditEntry.getDescription(), now,
                            false);
                    createDestinyDebitEntry(destinyDebitNote, debitEntry);

                } else {

//                    if (isPositive(availableCreditAmount)) {
//                        CreditEntry newCreditEntry =
//                                debitEntry.createCreditEntry(now, debitEntry.getDescription(), null, availableCreditAmount, null);
//                        newCreditEntry.getFinantialDocument().closeDocument();
//
//                        SettlementEntry.create(debitEntry, settlementNote, availableCreditAmount, debitEntry.getDescription(),
//                                now, false);
//                        SettlementEntry.create(newCreditEntry, settlementNote, availableCreditAmount,
//                                newCreditEntry.getDescription(), now, false);
//                    }

                    {
                        //final BigDecimal differenceAmount = openAmount.subtract(availableCreditAmount);
                        final CreditEntry regulationCreditEntry = CreditNote.createBalanceTransferCredit(objectDebtAccount, now,
                                objectDebitNote.getUiDocumentNumber(), openAmount, payorDebtAccount, null);

                        regulationCreditEntry.getFinantialDocument().closeDocument();

                        SettlementEntry.create(debitEntry, settlementNote, openAmount, debitEntry.getDescription(), now, false);
                        SettlementEntry.create(regulationCreditEntry, settlementNote, openAmount,
                                regulationCreditEntry.getDescription(), now, false);

                        final DebitEntry regulationDebitEntry = DebitNote.createBalanceTransferDebit(destinyDebtAccount,
                                debitEntry.getEntryDateTime(), debitEntry.getDueDate(),
                                regulationCreditEntry.getFinantialDocument().getUiDocumentNumber(), openAmount, payorDebtAccount,
                                debitEntry.getDescription());

                        regulationDebitEntry.getFinantialDocument().closeDocument();
                    }
                }
            }

            settlementNote.closeDocument();
        }
    }

    private DebitEntry createDestinyDebitEntry(final DebitNote destinyDebitNote, final DebitEntry debitEntry) {
        final BigDecimal openAmountWithoutVat =
                Constants.divide(debitEntry.getOpenAmount(), BigDecimal.ONE.add(debitEntry.getVatRate()));

        final DebitEntry newDebitEntry = DebitEntry.create(Optional.of(destinyDebitNote), destinyDebtAccount,
                debitEntry.getTreasuryEvent(), debitEntry.getVat(), openAmountWithoutVat, debitEntry.getDueDate(),
                debitEntry.getPropertiesMap(), debitEntry.getProduct(), debitEntry.getDescription(), debitEntry.getQuantity(),
                debitEntry.getInterestRate(), debitEntry.getEntryDateTime());

        newDebitEntry.edit(newDebitEntry.getDescription(), newDebitEntry.getTreasuryEvent(), newDebitEntry.getDueDate(),
                debitEntry.isAcademicalActBlockingSuspension(), debitEntry.isBlockAcademicActsOnDebt());

        return newDebitEntry;
    }

    private void anullPreparingDebitNote(final DebitNote objectDebitNote) {
        final DateTime now = new DateTime();
        final DebitNote newDebitNote = DebitNote.create(destinyDebtAccount, objectDebitNote.getPayorDebtAccount(),
                objectDebitNote.getDocumentNumberSeries(), now, now.toLocalDate(), "");

        for (final FinantialDocumentEntry objectEntry : objectDebitNote.getFinantialDocumentEntriesSet()) {
            final DebitEntry debitEntry = (DebitEntry) objectEntry;
            final BigDecimal amountWithExemptedAmount = debitEntry.getAmount().add(debitEntry.getExemptedAmount());

            if (!isPositive(amountWithExemptedAmount)) {
                continue;
            }

            if (debitEntry.getTreasuryEvent() != null) {
                debitEntry.annulOnEvent();
            }

            DebitEntry newDebitEntry = DebitEntry.create(Optional.of(newDebitNote), destinyDebtAccount,
                    debitEntry.getTreasuryEvent(), debitEntry.getVat(), amountWithExemptedAmount, debitEntry.getDueDate(),
                    debitEntry.getPropertiesMap(), debitEntry.getProduct(), debitEntry.getDescription(), debitEntry.getQuantity(),
                    debitEntry.getInterestRate(), debitEntry.getEntryDateTime());

            if (debitEntry.getTreasuryExemption() != null) {
                final TreasuryExemption treasuryExemption = debitEntry.getTreasuryExemption();
                TreasuryExemption.create(treasuryExemption.getTreasuryExemptionType(), debitEntry.getTreasuryEvent(),
                        treasuryExemption.getReason(), treasuryExemption.getValueToExempt(), newDebitEntry);

                treasuryExemption.delete();
            }

            newDebitEntry.edit(newDebitEntry.getDescription(), newDebitEntry.getTreasuryEvent(), newDebitEntry.getDueDate(),
                    debitEntry.isAcademicalActBlockingSuspension(), debitEntry.isBlockAcademicActsOnDebt());

        }

        objectDebitNote.anullDebitNoteWithCreditNote(bundle("label.BalanceTransferService.annuled.reason"), false);
    }

}
