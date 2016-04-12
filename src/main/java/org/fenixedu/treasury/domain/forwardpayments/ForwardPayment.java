package org.fenixedu.treasury.domain.forwardpayments;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.PaymentMethod;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.AdvancedPaymentCreditNote;
import org.fenixedu.treasury.domain.document.CreditEntry;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.document.PaymentEntry;
import org.fenixedu.treasury.domain.document.SettlementEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.settings.TreasurySettings;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.sun.xml.ws.util.Constants;

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

        final BigDecimal amount =
                debitEntriesSet.stream().map(DebitEntry::getOpenAmount).reduce((a, c) -> a.add(c)).orElse(BigDecimal.ZERO);
        setAmount(debtAccount.getFinantialInstitution().getCurrency().getValueWithScale(amount));
        setOrderNumber(lastForwardPayment().isPresent() ? lastForwardPayment().get().getOrderNumber() + 1 : 1);
        log();

        checkRules();
    }

    public void reject(final String statusCode, final String errorMessage, final String requestBody, final String responseBody) {
        setCurrentState(ForwardPaymentStateType.REJECTED);
        log(statusCode, errorMessage, requestBody, responseBody);
    }

    public void advanceToAuthorizedState(final String statusCode, final String errorMessage, final String requestBody,
            final String responseBody) {
        setCurrentState(ForwardPaymentStateType.AUTHORIZED);
        log(statusCode, errorMessage, requestBody, responseBody);
    }

    public void advanceToPayedState(final String statusCode, final String statusMessage, final BigDecimal payedAmount,
            final DateTime transactionDate, final String transactionId, final String authorizationNumber) {
        setCurrentState(ForwardPaymentStateType.PAYED);

        final FinantialInstitution finantialInstitution = getDebtAccount().getFinantialInstitution();
        final DocumentNumberSeries series =
                DocumentNumberSeries.findUniqueDefault(FinantialDocumentType.findForSettlementNote(), finantialInstitution).get();
        final SettlementNote settlement = SettlementNote.create(getDebtAccount(), series, new DateTime(), transactionDate, null);

        BigDecimal amountToConsume = payedAmount;

        // Order entries from the highest to the lowest, first the debts and then interests
        final List<DebitEntry> orderedEntries = Lists.newArrayList(getDebitEntriesSet());

        PaymentEntry.create(PaymentMethod.findAll().findAny().get(), settlement, amountToConsume, null);
        
        for (final DebitEntry debitEntry : orderedEntries) {
            if (org.fenixedu.treasury.util.Constants.isGreaterThan(debitEntry.getOpenAmount(), amountToConsume)) {
                break;
            }

            amountToConsume = amountToConsume.subtract(debitEntry.getOpenAmount());

            SettlementEntry.create(debitEntry, settlement, debitEntry.getOpenAmount(), debitEntry.getDescription(),
                    new DateTime(), true);
        }

        if (org.fenixedu.treasury.util.Constants.isPositive(amountToConsume)) {
            final DocumentNumberSeries advancedSeries =
                    DocumentNumberSeries.findUniqueDefault(FinantialDocumentType.findForCreditNote(), finantialInstitution).get();
            AdvancedPaymentCreditNote advancedNote =
                    AdvancedPaymentCreditNote.create(getDebtAccount(), advancedSeries, new DateTime());

            CreditEntry.create(advancedNote, "TODO: Pagamento em avanço",
                    TreasurySettings.getInstance().getAdvancePaymentProduct(), null, amountToConsume, new DateTime(), null,
                    org.fenixedu.treasury.util.Constants.DEFAULT_QUANTITY);
        }
        

        settlement.closeDocument();
    }

    public void advanceToRequestState(final String statusCode, final String statusMessage) {
        setCurrentState(ForwardPaymentStateType.REQUESTED);
        log(statusCode, statusMessage, null, null);
    }

    public boolean isInCreatedState() {
        return getCurrentState() == ForwardPaymentStateType.CREATED;
    }

    public boolean isActive() {
        return getCurrentState() != ForwardPaymentStateType.REJECTED && getCurrentState() != ForwardPaymentStateType.CANCELLED;
    }

    public boolean isInAuthorizedState() {
        return getCurrentState() == ForwardPaymentStateType.AUTHORIZED;
    }

    public boolean isPaid() {
        return getCurrentState() == ForwardPaymentStateType.PAYED;
    }

    public String getReferenceNumber() {
        return String.valueOf(getOrderNumber());
    }

    private void checkRules() {
    }

    private ForwardPaymentLog log(final String statusCode, final String statusMessage, final String requestBody,
            final String responseBody) {
        final ForwardPaymentLog log = log();

        log.setStatusCode(statusCode);
        log.setStatusLog(statusMessage);

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
