package org.fenixedu.treasury.domain.forwardpayments;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.joda.time.DateTime;

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

    public String getPaymentURL() {
        return getForwardPaymentConfiguration().paymentURL(this);
    }

    public String getReturnURL() {
        return getForwardPaymentConfiguration().returnURL(this);
    }

    public String getPaymentPage() {
        return getForwardPaymentConfiguration().paymentPage(this);
    }

    public String getFormattedAmount() {
        return getForwardPaymentConfiguration().formattedAmount(this);
    }

    public void execute(final Map<String, String> returnPaymentData) {
        getForwardPaymentConfiguration().execute(this, returnPaymentData);
    }

    public boolean isCreated() {
        return getCurrentState() == ForwardPaymentStateType.CREATED;
    }

    public boolean isActive() {
        return getCurrentState() != ForwardPaymentStateType.REJECTED && getCurrentState() != ForwardPaymentStateType.CANCELLED;
    }

    public boolean isAuthorized() {
        return getCurrentState() == ForwardPaymentStateType.AUTHORIZED;
    }

    public boolean isPaid() {
        return getCurrentState() == ForwardPaymentStateType.PAID;
    }
    
    public String getReferenceNumber() {
        return String.valueOf(getOrderNumber());
    }

    private void checkRules() {
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
