package org.fenixedu.treasury.domain.forwardpayments;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.PaymentMethod;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.AdvancedPaymentCreditNote;
import org.fenixedu.treasury.domain.document.CreditEntry;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.document.PaymentEntry;
import org.fenixedu.treasury.domain.document.SettlementEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.settings.TreasurySettings;
import org.fenixedu.treasury.dto.SettlementNoteBean.DebitEntryBean;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ForwardPayment extends ForwardPayment_Base {

    private static final Comparator<ForwardPayment> ORDER_COMPARATOR = new Comparator<ForwardPayment>() {

        @Override
        public int compare(final ForwardPayment o1, final ForwardPayment o2) {
            return ((Long) o1.getOrderNumber()).compareTo(o2.getOrderNumber());
        }
    };

    private ForwardPayment() {
        super();
        setBennu(Bennu.getInstance());
    }

    public ForwardPayment(final ForwardPaymentConfiguration forwardPaymentConfiguration, final DebtAccount debtAccount,
            final Set<DebitEntry> debitEntriesSet) {
        this();

        setForwardPaymentConfiguration(forwardPaymentConfiguration);
        setDebtAccount(debtAccount);
        getDebitEntriesSet().addAll(debitEntriesSet);

        setCurrentState(ForwardPaymentStateType.CREATED);
        setWhenOccured(new DateTime());

        final BigDecimal amount = debitEntriesSet.stream().map(DebitEntry::getOpenAmountWithInterests).reduce((a, c) -> a.add(c))
                .orElse(BigDecimal.ZERO);
        setAmount(debtAccount.getFinantialInstitution().getCurrency().getValueWithScale(amount));
        setOrderNumber(lastForwardPayment().isPresent() ? lastForwardPayment().get().getOrderNumber() + 1 : 1);
        log();

        checkRules();
    }

    public void reject(final String statusCode, final String errorMessage, final String requestBody, final String responseBody) {
        setCurrentState(ForwardPaymentStateType.REJECTED);
        setRejectionCode(statusCode);
        setRejectionLog(errorMessage);

        log(statusCode, errorMessage, requestBody, responseBody);
        checkRules();
    }

    public void advanceToRequestState(final String statusCode, final String statusMessage, final String requestBody,
            final String responseBody) {
        setCurrentState(ForwardPaymentStateType.REQUESTED);
        log(statusCode, statusMessage, requestBody, responseBody);

        checkRules();
    }

    public void advanceToAuthenticatedState(final String statusCode, final String statusMessage, final String requestBody,
            final String responseBody) {
        setCurrentState(ForwardPaymentStateType.AUTHENTICATED);
        log(statusCode, statusMessage, requestBody, responseBody);

        checkRules();
    }

    public void advanceToAuthorizedState(final String statusCode, final String errorMessage, final String requestBody,
            final String responseBody) {
        if (!isActive()) {
            throw new TreasuryDomainException("error.ForwardPayment.not.in.active.state");
        }

        if (isInAuthorizedState()) {
            throw new TreasuryDomainException("error.ForwardPayment.already.authorized");
        }

        if (isInPayedState()) {
            throw new TreasuryDomainException("error.ForwardPayment.already.payed");
        }

        setCurrentState(ForwardPaymentStateType.AUTHORIZED);
        log(statusCode, errorMessage, requestBody, responseBody);

        checkRules();
    }

    private static final Comparator<DebitEntry> COMPARE_DEBIT_ENTRIES = new Comparator<DebitEntry>() {

        @Override
        public int compare(final DebitEntry o1, final DebitEntry o2) {
            final Product interestProduct = TreasurySettings.getInstance().getInterestProduct();
            if (o1.getProduct() == interestProduct && o2.getProduct() != interestProduct) {
                return -1;
            }

            if (o1.getProduct() != interestProduct && o2.getProduct() == interestProduct) {
                return 1;
            }

            // compare by openAmount. First higher amounts then lower amounts
            int compareByOpenAmount = o1.getOpenAmount().compareTo(o2.getOpenAmount());

            if (compareByOpenAmount != 0) {
                return compareByOpenAmount * -1;
            }

            return o1.getExternalId().compareTo(o2.getExternalId());
        }

    };

    public void advanceToPayedState(final String statusCode, final String statusMessage, final BigDecimal payedAmount,
            final DateTime transactionDate, final String transactionId, final String authorizationNumber,
            final String requestBody, final String responseBody) {

        if (!isActive()) {
            throw new TreasuryDomainException("error.ForwardPayment.not.in.active.state");
        }

        if (isInPayedState()) {
            throw new TreasuryDomainException("error.ForwardPayment.already.payed");
        }

        setTransactionId(transactionId);
        setAuthorizationId(authorizationNumber);
        setTransactionDate(transactionDate);
        setPayedAmount(payedAmount);
        setCurrentState(ForwardPaymentStateType.PAYED);

        log(statusCode, statusMessage, requestBody, responseBody);

        final FinantialInstitution finantialInstitution = getDebtAccount().getFinantialInstitution();
        final DocumentNumberSeries settlementSeries =
                DocumentNumberSeries.findUniqueDefault(FinantialDocumentType.findForSettlementNote(), finantialInstitution).get();
        this.setSettlementNote(SettlementNote.create(getDebtAccount(), settlementSeries, new DateTime(), transactionDate,
                String.valueOf(getOrderNumber())));

        final DocumentNumberSeries debitNoteSeries =
                DocumentNumberSeries.findUniqueDefault(FinantialDocumentType.findForDebitNote(), finantialInstitution).get();

        BigDecimal amountToConsume = payedAmount;

        // Order entries from the highest to the lowest, first the debts and then interests
        final List<DebitEntry> orderedEntries = Lists.newArrayList(getDebitEntriesSet());
        Collections.sort(orderedEntries, COMPARE_DEBIT_ENTRIES);

        PaymentEntry.create(getForwardPaymentConfiguration().getPaymentMethod(), getSettlementNote(), amountToConsume, null);

        for (final DebitEntry debitEntry : orderedEntries) {

            if (debitEntry.getFinantialDocument() == null) {
                final DebitNote debitNote = DebitNote.create(getDebtAccount(), debitNoteSeries, new DateTime());
                debitNote.addDebitNoteEntries(Lists.newArrayList(debitEntry));
                debitNote.closeDocument();
            }

            if (org.fenixedu.treasury.util.Constants.isGreaterThan(debitEntry.getOpenAmount(), amountToConsume)) {
                break;
            }

            amountToConsume = amountToConsume.subtract(debitEntry.getOpenAmount());

            SettlementEntry.create(debitEntry, getSettlementNote(), debitEntry.getOpenAmount(), debitEntry.getDescription(),
                    new DateTime(), true);
        }

        // settle interest debit entries
        for (final DebitEntry de : orderedEntries) {
            for (DebitEntry interestDebitEntry : de.getInterestDebitEntriesSet()) {
                if (org.fenixedu.treasury.util.Constants.isGreaterThan(interestDebitEntry.getOpenAmount(), amountToConsume)) {
                    break;
                }

                if (interestDebitEntry.getFinantialDocument() == null) {
                    final DebitNote debitNote = DebitNote.create(getDebtAccount(), debitNoteSeries, new DateTime());
                    debitNote.addDebitNoteEntries(Lists.newArrayList(interestDebitEntry));
                    debitNote.closeDocument();
                }

                amountToConsume = amountToConsume.subtract(interestDebitEntry.getOpenAmount());
                SettlementEntry.create(interestDebitEntry, getSettlementNote(), interestDebitEntry.getOpenAmount(),
                        interestDebitEntry.getDescription(), new DateTime(), true);
            }
        }

        if (org.fenixedu.treasury.util.Constants.isPositive(amountToConsume)) {
            getSettlementNote().createAdvancedPaymentCreditNote(amountToConsume,
                    Constants.bundle("label.ForwardPayment.advancedpayment", String.valueOf(getOrderNumber())),
                    String.valueOf(getOrderNumber()));
        }

        getSettlementNote().closeDocument();

        checkRules();
    }

    public boolean isActive() {
        return getCurrentState() != ForwardPaymentStateType.REJECTED;
    }

    public boolean isInCreatedState() {
        return getCurrentState() == ForwardPaymentStateType.CREATED;
    }

    public boolean isInAuthorizedState() {
        return getCurrentState() == ForwardPaymentStateType.AUTHORIZED;
    }

    public boolean isInPayedState() {
        return getCurrentState() == ForwardPaymentStateType.PAYED;
    }

    public boolean isInAuthenticatedState() {
        return getCurrentState() == ForwardPaymentStateType.AUTHENTICATED;
    }

    public boolean isInRequestedState() {
        return getCurrentState() == ForwardPaymentStateType.REQUESTED;
    }

    public String getReferenceNumber() {
        return String.valueOf(getOrderNumber());
    }

    private void checkRules() {
        if (isInPayedState() && getSettlementNote() == null) {
            throw new TreasuryDomainException("error.ForwardPayment.settlementNote.required");
        }
    }

    private ForwardPaymentLog log(final String statusCode, final String statusMessage, final String requestBody,
            final String responseBody) {
        final ForwardPaymentLog log = log();

        log.setStatusCode(statusCode);
        log.setStatusLog(statusMessage);

        if (!Strings.isNullOrEmpty(requestBody)) {
            ForwardPaymentLogFile.createForRequestBody(log, requestBody.getBytes());
        }

        if (!Strings.isNullOrEmpty(responseBody)) {
            ForwardPaymentLogFile.createForResponseBody(log, responseBody.getBytes());
        }

        return log;
    }

    private ForwardPaymentLog log() {
        return new ForwardPaymentLog(this, getCurrentState(), getWhenOccured());
    }

    public static ForwardPayment create(final ForwardPaymentConfiguration forwardPaymentConfiguration,
            final DebtAccount debtAccount, final Set<DebitEntry> debitEntriesToPay) {
        return new ForwardPayment(forwardPaymentConfiguration, debtAccount, debitEntriesToPay);
    }

    private static Optional<ForwardPayment> lastForwardPayment() {
        return Bennu.getInstance().getForwardPaymentsSet().stream().max(ORDER_COMPARATOR);
    }

}
