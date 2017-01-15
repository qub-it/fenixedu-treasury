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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.Vat;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.event.TreasuryEvent;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.exemption.TreasuryExemption;
import org.fenixedu.treasury.domain.paymentcodes.MultipleEntriesPaymentCode;
import org.fenixedu.treasury.domain.settings.TreasurySettings;
import org.fenixedu.treasury.domain.tariff.InterestRate;
import org.fenixedu.treasury.dto.InterestRateBean;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import pt.ist.fenixframework.Atomic;
import static org.fenixedu.treasury.util.Constants.isPositive;

public class DebitNote extends DebitNote_Base {

    protected DebitNote(final DebtAccount debtAccount, final DocumentNumberSeries documentNumberSeries,
            final DateTime documentDate) {
        super();
        this.init(debtAccount, documentNumberSeries, documentDate);
    }

    protected DebitNote(final DebtAccount debtAccount, final DebtAccount payorDebtAccount,
            final DocumentNumberSeries documentNumberSeries, final DateTime documentDate) {
        super();

        this.init(debtAccount, payorDebtAccount, documentNumberSeries, documentDate);
    }

    @Override
    public boolean isDebitNote() {
        return true;
    }

    @Override
    protected void checkRules() {
        if (!getDocumentNumberSeries().getFinantialDocumentType().getType().equals(FinantialDocumentTypeEnum.DEBIT_NOTE)) {
            throw new TreasuryDomainException("error.DebitNote.finantialDocumentType.invalid");
        }

        if (getPayorDebtAccount() != null
                && !getPayorDebtAccount().getFinantialInstitution().equals(getDebtAccount().getFinantialInstitution())) {
            throw new TreasuryDomainException("error.DebitNote.finantialinstitution.mismatch");
        }

        super.checkRules();
    }

    @Override
    @Atomic
    public void delete(boolean deleteEntries) {
        super.delete(deleteEntries);
    }

    @Override
    public BigDecimal getOpenAmount() {
        if (this.isAnnulled()) {
            return BigDecimal.ZERO;
        }
        BigDecimal amount = BigDecimal.ZERO;
        for (DebitEntry entry : getDebitEntriesSet()) {
            amount = amount.add(entry.getOpenAmount());
        }
        return getDebtAccount().getFinantialInstitution().getCurrency().getValueWithScale(amount);
    }

    @Override
    public BigDecimal getOpenAmountWithInterests() {
        if (this.getState().isPreparing() || this.getState().isClosed()) {
            if (Constants.isEqual(getOpenAmount(), BigDecimal.ZERO)) {
                return BigDecimal.ZERO;
            } else {
                return getDebtAccount().getFinantialInstitution().getCurrency()
                        .getValueWithScale(getOpenAmount().add(getPendingInterestAmount()));
            }
        } else {
            return BigDecimal.ZERO;
        }
    }

    public Stream<? extends DebitEntry> getDebitEntries() {
        return DebitEntry.find(this);
    }

    public Set<? extends DebitEntry> getDebitEntriesSet() {
        return this.getDebitEntries().collect(Collectors.<DebitEntry> toSet());
    }

    public BigDecimal getDebitAmount() {
        return this.getTotalAmount();
    }

    public BigDecimal getCreditAmount() {
        return BigDecimal.ZERO;
    }

    @Atomic
    public void edit(final LocalDate documentDate, LocalDate documentDueDate, final String originDocumentNumber) {

        setDocumentDate(documentDate.toDateTimeAtStartOfDay());
        setDocumentDueDate(documentDueDate);
        setOriginDocumentNumber(originDocumentNumber);

        checkRules();
    }

    // @formatter:off
    /* **********
     * DEBIT NOTE
     * **********
     */
    // @formatter:on

    public static Stream<DebitNote> findAll() {
        return FinantialDocument.findAll().filter(x -> x instanceof DebitNote).map(DebitNote.class::cast);
    }

    @Atomic
    public static DebitNote create(final DebtAccount debtAccount, final DocumentNumberSeries documentNumberSeries,
            final DateTime documentDate) {
        DebitNote note = new DebitNote(debtAccount, documentNumberSeries, documentDate);
        note.setFinantialDocumentType(FinantialDocumentType.findForDebitNote());
        note.setOriginDocumentNumber("");
        note.setDocumentDueDate(documentDate.toLocalDate());
        note.checkRules();
        return note;
    }

    @Atomic
    public static DebitNote create(final DebtAccount debtAccount, final DebtAccount payorDebtAccount,
            final DocumentNumberSeries documentNumberSeries, final DateTime documentDate, final LocalDate documentDueDate,
            final String originNumber) {

        DebitNote note = new DebitNote(debtAccount, payorDebtAccount, documentNumberSeries, documentDate);
        note.setFinantialDocumentType(FinantialDocumentType.findForDebitNote());
        note.setOriginDocumentNumber(originNumber);
        note.setDocumentDueDate(documentDueDate);
        note.checkRules();

        return note;
    }

    @Atomic
    public void addDebitNoteEntries(List<DebitEntry> debitEntries) {
        debitEntries.forEach(x -> this.addFinantialDocumentEntries(x));
        checkRules();
    }

    @Override
    public Set<FinantialDocument> findRelatedDocuments(Set<FinantialDocument> documentsBaseList,
            Boolean includeAnulledDocuments) {
        documentsBaseList.add(this);

        for (DebitEntry entry : getDebitEntriesSet()) {
            for (CreditEntry creditEntry : entry.getCreditEntriesSet()) {
                if (creditEntry.getFinantialDocument() != null && !creditEntry.getFinantialDocument().isPreparing()) {
                    if (includeAnulledDocuments == true || this.isAnnulled() == false) {
                        if (documentsBaseList.contains(creditEntry.getFinantialDocument()) == false) {
                            documentsBaseList.addAll(creditEntry.getFinantialDocument().findRelatedDocuments(documentsBaseList,
                                    includeAnulledDocuments));
                        }
                    }
                }
            }
        }

        for (DebitEntry entry : getDebitEntriesSet()) {
            for (SettlementEntry settlementEntry : entry.getSettlementEntriesSet()) {
                if (settlementEntry.getFinantialDocument() != null && !settlementEntry.getFinantialDocument().isPreparing()) {
                    if (includeAnulledDocuments == true || settlementEntry.getFinantialDocument().isAnnulled() == false) {
                        if (documentsBaseList.contains(settlementEntry.getFinantialDocument()) == false) {
                            documentsBaseList.addAll(settlementEntry.getFinantialDocument()
                                    .findRelatedDocuments(documentsBaseList, includeAnulledDocuments));
                        }
                    }
                }
            }

        }

        return documentsBaseList;
    }

    public BigDecimal getPendingInterestAmount() {
        return getPendingInterestAmount(new LocalDate());
    }

    public BigDecimal getPendingInterestAmount(LocalDate whenToCalculate) {
        BigDecimal interest = BigDecimal.ZERO;
        for (DebitEntry entry : this.getDebitEntriesSet()) {
            interest = interest.add(entry.calculateUndebitedInterestValue(whenToCalculate).getInterestAmount());
        }
        return interest;
    }

    @Atomic
    public void anullDebitNoteWithCreditNote(String reason, boolean anullGeneratedInterests) {

        if (this.getFinantialDocumentEntriesSet().size() > 0 && this.isClosed()) {

            final DateTime now = new DateTime();

            //1. criar nota de acerto
            //2. percorrer os itens de divida, criar correspondente item de acerto com o valor "aberto"
            //2.1 verificar se existiram "juros" gerados correspondentes
            //2.2 Libertar o tipo de juro a aplicar para não continuar a "calcular juro"
            //3. fechar nota de acerto
            //4. criar settlement note
            //5. adicionar itens de divida com cada valor open amount
            //5.1 adicionar itens de dívida com cada valor open amount dos juros
            //6. adicionar itens de acerto por cada valor open amount de item de divida
            //7. fechar settlement note

            // No final podem sobrar itens de acerto com valor pendente de utilizacao, que representam os valores ja pagos nos itens de dividas correspondentes
            createEquivalentCreditNote(now, reason, anullGeneratedInterests);

            //Clear the InterestRate for DebitEntry
            for (final DebitEntry debitEntry : this.getDebitEntriesSet()) {
                // Annul payment reference codes
                for (final MultipleEntriesPaymentCode paymentCode : debitEntry.getPaymentCodesSet()) {
                    if (paymentCode.getPaymentReferenceCode().isNew() || paymentCode.getPaymentReferenceCode().isUsed()) {
                        paymentCode.getPaymentReferenceCode().anullPaymentReferenceCode();
                    }
                }

                debitEntry.clearInterestRate();

                // Also remove from treasury event
                if (debitEntry.getTreasuryEvent() != null) {
                    debitEntry.annulOnEvent();
                }

                // Remove treasury exemption
                if (debitEntry.getTreasuryExemption() != null) {
                    debitEntry.getTreasuryExemption().delete();
                }

                for (final CreditEntry creditEntry : debitEntry.getCreditEntriesSet()) {
                    debitEntry.closeCreditEntryIfPossible(reason, now, creditEntry);
                }
            }

            this.setAnnulledReason(reason);
        } else if (isPreparing()) {
            if (!getCreditNoteSet().isEmpty()) {
                throw new TreasuryDomainException("error.DebitNote.creditNote.not.empty");
            }

            for (DebitEntry debitEntry : this.getDebitEntriesSet()) {
                if (isPositive(debitEntry.getExemptedAmount())) {
                    throw new TreasuryDomainException("error.DebitNote.annul.not.possible.remove.exemption.on.debit.entry",
                            debitEntry.getDescription());
                }

                // Also remove from treasury event
                if (debitEntry.getTreasuryEvent() != null) {
                    debitEntry.annulOnEvent();
                }

                // Remove treasury exemption
                if (debitEntry.getTreasuryExemption() != null) {
                    debitEntry.getTreasuryExemption().delete();
                }

                for (final MultipleEntriesPaymentCode paymentCode : debitEntry.getPaymentCodesSet()) {
                    if (paymentCode.getPaymentReferenceCode().isNew() || paymentCode.getPaymentReferenceCode().isUsed()) {
                        paymentCode.getPaymentReferenceCode().anullPaymentReferenceCode();
                    }
                }
            }

            this.setState(FinantialDocumentStateType.ANNULED);

            if (Authenticate.getUser() != null) {
                setAnnulledReason(reason + " - [" + Authenticate.getUser().getUsername() + "]"
                        + new DateTime().toString("YYYY-MM-dd HH:mm:ss"));
            } else {
                setAnnulledReason(reason + " - " + new DateTime().toString("YYYY-MM-dd HH:mm:ss"));
            }
        } else {
            throw new TreasuryDomainException("error.DebitNote.cannot.anull.is.empty");
        }
    }

    @Atomic
    public void createEquivalentCreditNote(final DateTime documentDate, final String documentObservations,
            final boolean createForInterestRateEntries) {
        for (DebitEntry entry : this.getDebitEntriesSet()) {
            //Get the amount for credit without tax, and considering the credit quantity FOR ONE
            final BigDecimal amountForCredit =
                    entry.getCurrency().getValueWithScale(Constants.divide(entry.getAvailableAmountForCredit(),
                            BigDecimal.ONE.add(Constants.divide(entry.getVatRate(), BigDecimal.valueOf(100)))));
            entry.createCreditEntry(documentDate, entry.getDescription(), documentObservations, amountForCredit, null, null);
        }

        if (!createForInterestRateEntries) {
            return;
        }

        for (final DebitEntry entry : this.getDebitEntriesSet()) {
            for (DebitEntry interestEntry : entry.getInterestDebitEntriesSet()) {
                final BigDecimal amountForCredit = interestEntry.getCurrency()
                        .getValueWithScale(Constants.divide(interestEntry.getAvailableAmountForCredit(),
                                BigDecimal.ONE.add(Constants.divide(entry.getVatRate(), BigDecimal.valueOf(100)))));
                interestEntry.createCreditEntry(documentDate, entry.getDescription(), documentObservations, amountForCredit,
                        null, null);
            }
        }
    }

    public Set<CreditEntry> getRelatedCreditEntriesSet() {
        Set<CreditEntry> result = new HashSet<CreditEntry>();
        for (DebitEntry debit : this.getDebitEntriesSet()) {
            result.addAll(debit.getCreditEntriesSet());
        }
        return result;
    }

    @Atomic
    public DebitNote updatePayorDebtAccount(final DebtAccount payorDebtAccount) {
        if (!isPreparing() && !isClosed()) {
            throw new TreasuryDomainException("error.DebitNote.updatePayorDebtAccount.not.preparing.nor.closed");
        }

        if (getPayorDebtAccount() == payorDebtAccount) {
            throw new TreasuryDomainException("error.DebitNote.updatePayorDebtAccount.payor.not.changed");
        }

        final DebitNote updatingDebitNote = isPreparing() ? this : anullAndCopyDebitNote(
                Constants.bundle("label.DebitNote.updatePayorDebtAccount.anull.reason"));

        updatingDebitNote.setPayorDebtAccount(payorDebtAccount);

        for (DebitEntry debitEntry : this.getDebitEntriesSet()) {
            for (final MultipleEntriesPaymentCode paymentCode : debitEntry.getPaymentCodesSet()) {
                if (paymentCode.getPaymentReferenceCode().isNew() || paymentCode.getPaymentReferenceCode().isUsed()) {
                    paymentCode.getPaymentReferenceCode().anullPaymentReferenceCode();
                }
            }
        }

        return updatingDebitNote;
    }

    private DebitNote anullAndCopyDebitNote(final String reason) {
        if (!isClosed()) {
            throw new TreasuryDomainException("error.DebitNote.anullAndCopyDebitNote.copy.only.on.closed.debit.note");
        }

        final DebitNote newDebitNote = DebitNote.create(getDebtAccount(), getDocumentNumberSeries(), new DateTime());

        newDebitNote.setOriginDocumentNumber(getOriginDocumentNumber());
        for (final FinantialDocumentEntry finantialDocumentEntry : getFinantialDocumentEntriesSet()) {
            final DebitEntry debitEntry = (DebitEntry) finantialDocumentEntry;

            DebitEntry newDebitEntry = DebitEntry.create(Optional.of(newDebitNote), debitEntry.getDebtAccount(),
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

        anullDebitNoteWithCreditNote(reason, false);

        return newDebitNote;
    }

    public static DebitNote createInterestDebitNoteForDebitNote(DebitNote debitNote, DocumentNumberSeries documentNumberSeries,
            DateTime documentDate, LocalDate paymentDate) {

        DebitNote interestDebitNote = DebitNote.create(debitNote.getDebtAccount(), documentNumberSeries, documentDate);
        for (DebitEntry entry : debitNote.getDebitEntriesSet()) {
            InterestRateBean calculateUndebitedInterestValue = entry.calculateUndebitedInterestValue(paymentDate);
            if (Constants.isGreaterThan(calculateUndebitedInterestValue.getInterestAmount(), BigDecimal.ZERO)) {
                entry.createInterestRateDebitEntry(calculateUndebitedInterestValue, documentDate,
                        Optional.<DebitNote> of(interestDebitNote));
            }
        }

        if (Constants.isEqual(interestDebitNote.getTotalAmount(), BigDecimal.ZERO)) {
            interestDebitNote.delete(true);
            throw new TreasuryDomainException(BundleUtil.getString(Constants.BUNDLE, "error.DebitNote.no.interest.to.generate"));
        }
        return interestDebitNote;
    }

    public static DebitEntry createBalanceTransferDebit(final DebtAccount debtAccount, final DateTime entryDate,
            final LocalDate dueDate, final String originNumber, final BigDecimal amountWithVat,
            final DebtAccount payorDebtAccount, String entryDescription) {
        final FinantialInstitution finantialInstitution = debtAccount.getFinantialInstitution();
        final Series regulationSeries = finantialInstitution.getRegulationSeries();
        final DocumentNumberSeries numberSeries =
                DocumentNumberSeries.find(FinantialDocumentType.findForDebitNote(), regulationSeries);
        final Product balanceTransferProduct = TreasurySettings.getInstance().getTransferBalanceProduct();
        final Vat transferVat = Vat.findActiveUnique(balanceTransferProduct.getVatType(), finantialInstitution, entryDate).get();

        if(Strings.isNullOrEmpty(entryDescription)) {
            entryDescription = balanceTransferProduct.getName().getContent();
        }
        
        final DebitNote debitNote = DebitNote.create(debtAccount, payorDebtAccount, numberSeries, new DateTime(),
                new DateTime().toLocalDate(), originNumber);

        final BigDecimal amountWithoutVat = Constants.divide(amountWithVat, BigDecimal.ONE.add(transferVat.getTaxRate()));
        return DebitEntry.create(Optional.of(debitNote), debtAccount, null, transferVat, amountWithoutVat, dueDate,
                Maps.newHashMap(), balanceTransferProduct, entryDescription, BigDecimal.ONE, null,
                entryDate);
    }

}
