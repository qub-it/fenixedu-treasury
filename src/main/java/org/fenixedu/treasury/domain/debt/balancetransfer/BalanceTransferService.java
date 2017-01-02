package org.fenixedu.treasury.domain.debt.balancetransfer;

import static org.fenixedu.treasury.util.Constants.bundle;
import static org.fenixedu.treasury.util.Constants.isEqual;
import static org.fenixedu.treasury.util.Constants.isGreaterThan;
import static org.fenixedu.treasury.util.Constants.isPositive;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.Vat;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.CreditEntry;
import org.fenixedu.treasury.domain.document.CreditNote;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentEntry;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.document.Series;
import org.fenixedu.treasury.domain.document.SettlementEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.exemption.TreasuryExemption;
import org.fenixedu.treasury.domain.settings.TreasurySettings;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import pt.ist.fenixframework.Atomic;

public class BalanceTransferService {

    @Atomic
    public void transferBalance(final DebtAccount objectDebtAccount, final DebtAccount destinyDebtAccount) {
        final FinantialInstitution finantialInstitution = objectDebtAccount.getFinantialInstitution();
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
            } else if (invoiceEntry.isCreditNoteEntry()) {
                transferCreditEntry((CreditEntry) invoiceEntry, destinyDebtAccount);
            }
        }

        for (final DebitNote debitNote : pendingDebitNotes) {
            transferDebitEntries(debitNote, destinyDebtAccount);
        }
    }

    private void transferCreditEntry(final CreditEntry invoiceEntry, final DebtAccount destinyDebtAccount) {

        final DebtAccount objectDebtAccount = invoiceEntry.getDebtAccount();
        final FinantialInstitution finantialInstitution = objectDebtAccount.getFinantialInstitution();
        final Series defaultSeries = Series.findUniqueDefault(finantialInstitution).get();
        final Series regulationSeries = finantialInstitution.getRegulationSeries();
        final DocumentNumberSeries debitNoteSeries =
                DocumentNumberSeries.find(FinantialDocumentType.findForDebitNote(), regulationSeries);
        final DocumentNumberSeries creditNoteSeries =
                DocumentNumberSeries.find(FinantialDocumentType.findForCreditNote(), regulationSeries);
        final DocumentNumberSeries settlementNoteSeries =
                DocumentNumberSeries.find(FinantialDocumentType.findForSettlementNote(), defaultSeries);
        final Product balanceTransferProduct = TreasurySettings.getInstance().getTransferBalanceProduct();
        final DateTime now = new DateTime();
        final Vat transferVat = Vat.findActiveUnique(balanceTransferProduct.getVatType(), finantialInstitution, now).get();

        final CreditEntry creditEntry = (CreditEntry) invoiceEntry;
        final BigDecimal creditOpenAmount = creditEntry.getOpenAmount();
        final BigDecimal creditOpenAmountWithoutVat =
                creditOpenAmount.subtract(creditOpenAmount.multiply(transferVat.getTaxRate()));

        final DebitNote regulationDebitNote = DebitNote.create(objectDebtAccount, debitNoteSeries, now);

        DebitEntry regulationDebitEntry = DebitEntry.create(Optional.of(regulationDebitNote), objectDebtAccount, null,
                transferVat, creditOpenAmount, now.toLocalDate(), null, balanceTransferProduct,
                balanceTransferProduct.getName().getContent(Constants.DEFAULT_LANGUAGE), BigDecimal.ONE, null, now);

        regulationDebitNote.closeDocument();
        final CreditNote regulationCreditNote =
                CreditNote.create(destinyDebtAccount, creditNoteSeries, now, null, regulationDebitNote.getUiDocumentNumber());
        CreditEntry.create(regulationCreditNote, balanceTransferProduct.getName().getContent(), balanceTransferProduct,
                transferVat, creditOpenAmountWithoutVat, now, null, BigDecimal.ONE);

        if (((CreditNote) creditEntry.getFinantialDocument()).isForPayorDebtAccount()) {
            regulationCreditNote.editPayorDebtAccount(((CreditNote) creditEntry.getFinantialDocument()).getPayorDebtAccount());
        }

        final SettlementNote settlementNote =
                SettlementNote.create(objectDebtAccount, settlementNoteSeries, now, now, null, null);

        creditEntry.getFinantialDocument().closeDocument();
        SettlementEntry.create(regulationDebitEntry, settlementNote, regulationDebitEntry.getOpenAmount(),
                balanceTransferProduct.getName().getContent(), now, false);
        SettlementEntry.create(creditEntry, settlementNote, creditOpenAmount, balanceTransferProduct.getName().getContent(), now,
                false);

        settlementNote.closeDocument();
    }

    private void transferDebitEntries(final DebitNote objectDebitNote, final DebtAccount destinyDebtAccount) {
        final FinantialInstitution finantialInstitution = objectDebitNote.getDebtAccount().getFinantialInstitution();
        final DebtAccount objectDebtAccount = objectDebitNote.getDebtAccount();
        final DocumentNumberSeries documentNumberSeries =
                DocumentNumberSeries.findUniqueDefault(FinantialDocumentType.findForDebitNote(), finantialInstitution).get();
        final DateTime now = new DateTime();

        if (objectDebitNote.isPreparing()) {
            final DebitNote newDebitNote = DebitNote.create(destinyDebtAccount, documentNumberSeries, new DateTime());

            for (final FinantialDocumentEntry objectEntry : objectDebitNote.getFinantialDocumentEntriesSet()) {
                if (!isPositive(((DebitEntry) objectEntry).getOpenAmount())) {
                    continue;
                }

                final DebitEntry debitEntry = (DebitEntry) objectEntry;
                
                if (debitEntry.getTreasuryEvent() != null) {
                    debitEntry.annulOnEvent();
                }
                
                DebitEntry newDebitEntry = DebitEntry.create(Optional.of(newDebitNote), destinyDebtAccount,
                        debitEntry.getTreasuryEvent(), debitEntry.getVat(),
                        debitEntry.getAmount().add(debitEntry.getExemptedAmount()), debitEntry.getDueDate(),
                        debitEntry.getPropertiesMap(), debitEntry.getProduct(), debitEntry.getDescription(), debitEntry.getQuantity(),
                        debitEntry.getInterestRate(), debitEntry.getEntryDateTime());

                if (debitEntry.getTreasuryExemption() != null) {
                    final TreasuryExemption treasuryExemption = debitEntry.getTreasuryExemption();
                    TreasuryExemption.create(treasuryExemption.getTreasuryExemptionType(), debitEntry.getTreasuryEvent(),
                            treasuryExemption.getReason(), treasuryExemption.getValueToExempt(), newDebitEntry);
                }

                newDebitEntry.edit(newDebitEntry.getDescription(), newDebitEntry.getTreasuryEvent(), newDebitEntry.getDueDate(),
                        debitEntry.isAcademicalActBlockingSuspension(), debitEntry.isBlockAcademicActsOnDebt());
                
            }
            
            objectDebitNote.anullDebitNoteWithCreditNote(bundle("label.BalanceTransferService.annuled.reason"), false);
        } else if (objectDebitNote.isClosed()) {
            final DebitNote destinyDebitNote = DebitNote.create(destinyDebtAccount, objectDebitNote.getPayorDebtAccount(),
                    documentNumberSeries, now, now.toLocalDate(), objectDebitNote.getUiDocumentNumber());

            final SettlementNote settlementNote =
                    SettlementNote.create(objectDebtAccount, documentNumberSeries, now, now, null, null);
            for (final FinantialDocumentEntry objectEntry : objectDebitNote.getFinantialDocumentEntriesSet()) {
                if (!isPositive(((DebitEntry) objectEntry).getOpenAmount())) {
                    continue;
                }

                final DebitEntry debitEntry = (DebitEntry) objectEntry;
                
                if (debitEntry.getTreasuryEvent() != null) {
                    debitEntry.annulOnEvent();
                }
                
                final BigDecimal openAmountWithoutVat =
                        debitEntry.getOpenAmount().subtract(debitEntry.getOpenAmount().multiply(debitEntry.getVatRate()));

                DebitEntry newDebitEntry = DebitEntry.create(Optional.of(destinyDebitNote), debitEntry.getDebtAccount(),
                        debitEntry.getTreasuryEvent(), debitEntry.getVat(), openAmountWithoutVat, debitEntry.getDueDate(),
                        debitEntry.getPropertiesMap(), debitEntry.getProduct(), debitEntry.getDescription(),
                        debitEntry.getQuantity(), debitEntry.getInterestRate(), debitEntry.getEntryDateTime());

                if (debitEntry.getTreasuryExemption() != null) {
                    final TreasuryExemption treasuryExemption = debitEntry.getTreasuryExemption();
                    TreasuryExemption.create(treasuryExemption.getTreasuryExemptionType(), debitEntry.getTreasuryEvent(),
                            treasuryExemption.getReason(), treasuryExemption.getValueToExempt(), newDebitEntry);
                }

                newDebitEntry.edit(newDebitEntry.getDescription(), newDebitEntry.getTreasuryEvent(), newDebitEntry.getDueDate(),
                        debitEntry.isAcademicalActBlockingSuspension(), debitEntry.isBlockAcademicActsOnDebt());

                final BigDecimal openAmount = debitEntry.getOpenAmount();
                final BigDecimal availableCreditAmount = debitEntry.getAvailableAmountForCredit();
                if (isGreaterThan(availableCreditAmount, openAmount) || isEqual(availableCreditAmount, openAmount)) {
                    final CreditEntry newCreditEntry =
                            debitEntry.createCreditEntry(now, debitEntry.getDescription(), null, openAmount, null);
                    newCreditEntry.getFinantialDocument().closeDocument();

                    SettlementEntry.create(debitEntry, settlementNote, openAmount, debitEntry.getDescription(), now, false);
                    SettlementEntry.create(newCreditEntry, settlementNote, openAmount, newCreditEntry.getDescription(), now,
                            false);
                } else {
                    {
                        CreditEntry newCreditEntry =
                                debitEntry.createCreditEntry(now, debitEntry.getDescription(), null, availableCreditAmount, null);
                        newCreditEntry.getFinantialDocument().closeDocument();

                        SettlementEntry.create(debitEntry, settlementNote, availableCreditAmount, debitEntry.getDescription(),
                                now, false);
                        SettlementEntry.create(newCreditEntry, settlementNote, availableCreditAmount,
                                newCreditEntry.getDescription(), now, false);
                    }
                    {
                        final Series regulationSeries = finantialInstitution.getRegulationSeries();
                        final DocumentNumberSeries creditNoteSeries =
                                DocumentNumberSeries.find(FinantialDocumentType.findForCreditNote(), regulationSeries);
                        final Product balanceTransferProduct = TreasurySettings.getInstance().getTransferBalanceProduct();
                        final Vat transferVat =
                                Vat.findActiveUnique(balanceTransferProduct.getVatType(), finantialInstitution, now).get();

                        final BigDecimal differenceAmount = openAmount.subtract(availableCreditAmount);
                        final BigDecimal differenceAmountWithoutVat =
                                differenceAmount.subtract(differenceAmount.multiply(transferVat.getTaxRate()));

                        final CreditNote regulationCreditNote = CreditNote.create(destinyDebtAccount, creditNoteSeries, now, null,
                                objectDebitNote.getUiDocumentNumber());
                        CreditEntry regulationCreditEntry = CreditEntry.create(regulationCreditNote,
                                balanceTransferProduct.getName().getContent(), balanceTransferProduct, transferVat,
                                differenceAmountWithoutVat, now, null, BigDecimal.ONE);

                        if (objectDebitNote.isForPayorDebtAccount()) {
                            regulationCreditNote.editPayorDebtAccount(objectDebitNote.getPayorDebtAccount());
                        }

                        SettlementEntry.create(debitEntry, settlementNote, differenceAmount, debitEntry.getDescription(), now,
                                false);
                        SettlementEntry.create(regulationCreditEntry, settlementNote, differenceAmount,
                                regulationCreditEntry.getDescription(), now, false);
                    }
                }
            }
        }
    }
}
