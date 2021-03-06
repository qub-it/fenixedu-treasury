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
package org.fenixedu.treasury.domain.tariff;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.treasury.domain.Currency;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.dto.InterestRateBean;
import org.fenixedu.treasury.util.TreasuryConstants;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class InterestRate extends InterestRate_Base {

    private static final int MAX_YEARS = 5;

    protected InterestRate() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

    protected InterestRate(final Tariff tariff, final DebitEntry debitEntry, final InterestType interestType,
            final int numberOfDaysAfterDueDate, final boolean applyInFirstWorkday, final int maximumDaysToApplyPenalty,
            final BigDecimal interestFixedAmount, final BigDecimal rate) {
        this();

        setTariff(tariff);
        setDebitEntry(debitEntry);
        setInterestType(interestType);
        //HACK: Override the numberOfDaysAfterDueDate
        setNumberOfDaysAfterDueDate(1);
        setApplyInFirstWorkday(applyInFirstWorkday);
        setMaximumDaysToApplyPenalty(maximumDaysToApplyPenalty);
        setInterestFixedAmount(interestFixedAmount);
        setRate(rate);

        checkRules();
    }

    private void checkRules() {

        if (getTariff() == null && getDebitEntry() == null) {
            throw new TreasuryDomainException("error.InterestRate.product.or.debit.entry.required");
        }

        if (getTariff() != null && getDebitEntry() != null) {
            throw new TreasuryDomainException("error.InterestRate.product.or.debit.entry.only.one");
        }

        if (getInterestType() == null) {
            throw new TreasuryDomainException("error.InterestRate.interestType.required");
        }

        if (getInterestType().isDaily() && getRate() == null) {
            throw new TreasuryDomainException("error.InterestRate.rate.required");
        }

        if (getInterestType().isFixedAmount() && getInterestFixedAmount() == null) {
            throw new TreasuryDomainException("error.InterestRate.interestFixedAmount.required");
        }
    }

    public boolean isMaximumDaysToApplyPenaltyApplied() {
        return getMaximumDaysToApplyPenalty() > 0;
    }

    public boolean isApplyInFirstWorkday() {
        return getApplyInFirstWorkday();
    }

    @Atomic
    public void edit(InterestType interestType, int numberOfDaysAfterDueDate, boolean applyInFirstWorkday,
            int maximumDaysToApplyPenalty, BigDecimal interestFixedAmount, BigDecimal rate) {

        setInterestType(interestType);
        //HACK: For now override the NumberOfDays - 01/07/2015
        setNumberOfDaysAfterDueDate(1);
        setApplyInFirstWorkday(applyInFirstWorkday);
        setMaximumDaysToApplyPenalty(maximumDaysToApplyPenalty);
        setInterestFixedAmount(interestFixedAmount);
        setRate(rate);

        checkRules();
    }

    public InterestRateBean calculateInterests(LocalDate paymentDate, boolean withAllInterestValues) {
        if (getInterestType().isFixedAmount()) {
            return calculateForFixedAmount(withAllInterestValues);
        }

        return calculateInterestAmount(withAllInterestValues, calculateEvents(paymentDate));
    }

    private InterestRateBean calculateInterestAmount(boolean withAllInterestValues,
            NavigableMap<LocalDate, InterestCalculationEvent> orderedEvents) {
        InterestRateBean result = new InterestRateBean();

        BigDecimal totalInterestAmount = BigDecimal.ZERO;
        int totalOfDays = 0;

        LocalDate key = orderedEvents.firstKey();
        while (orderedEvents.higherKey(key) != null) {
            LocalDate eventDate = orderedEvents.higherKey(key);
            InterestCalculationEvent event = orderedEvents.get(key);

            BigDecimal daysInYear = new BigDecimal(TreasuryConstants.numberOfDaysInYear(key.getYear()));
            BigDecimal amountPerDay = TreasuryConstants.divide(event.amountToPay, daysInYear);
            BigDecimal numberOfDays = new BigDecimal(Days.daysBetween(key, eventDate).getDays());
            BigDecimal partialInterestAmount = event.interestRate.multiply(amountPerDay).multiply(numberOfDays);

            result.addDetail(partialInterestAmount, key, eventDate.minusDays(1), amountPerDay, event.amountToPay,
                    TreasuryConstants.defaultScale(event.interestRate).multiply(TreasuryConstants.HUNDRED_PERCENT).setScale(4,
                            RoundingMode.HALF_EVEN));

            totalInterestAmount = totalInterestAmount.add(partialInterestAmount);
            totalOfDays += numberOfDays.intValue();

            key = eventDate;
        }

        if (!withAllInterestValues) {
            for (final Entry<LocalDate, BigDecimal> entry : createdInterestEntriesMap().entrySet()) {
                result.addCreatedInterestEntry(entry.getKey(), entry.getValue());
                totalInterestAmount = totalInterestAmount.subtract(entry.getValue());
            }
        }
        
        if(TreasuryConstants.isNegative(totalInterestAmount)) {
            totalInterestAmount = BigDecimal.ZERO;
        }

        result.setInterestAmount(getRelatedCurrency().getValueWithScale(totalInterestAmount));
        result.setNumberOfDays(totalOfDays);

        return result;
    }

    private TreeMap<LocalDate, BigDecimal> createdInterestEntriesMap() {
        TreeMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();

        for (DebitEntry interestDebitEntry : getDebitEntry().getInterestDebitEntriesSet()) {
            if (interestDebitEntry.isAnnulled()) {
                continue;
            }
            
            if(!TreasuryConstants.isPositive(interestDebitEntry.getAvailableAmountForCredit())) {
                continue;
            }
            
            LocalDate interestEntryDateTime = interestDebitEntry.getEntryDateTime().toLocalDate();
            result.putIfAbsent(interestEntryDateTime, BigDecimal.ZERO);
            result.put(interestEntryDateTime, result.get(interestEntryDateTime).add(interestDebitEntry.getAvailableAmountForCredit()));
        }

        return result;
    }

    private NavigableMap<LocalDate, InterestCalculationEvent> calculateEvents(LocalDate paymentDate) {
        NavigableMap<LocalDate, BigDecimal> paymentsMap = createPaymentsMap(paymentDate);
        LocalDate lastPayment = paymentsMap.lastKey();

        final LocalDate firstDayToChargeInterests = calculateFirstDayToChargeInterests(lastPayment);
        final LocalDate nextDayOfInterestsCharge =
                calculateLastDayToChargeInterests(lastPayment, firstDayToChargeInterests).plusDays(1);

        BigDecimal amountToPayAtFirstDay = amountInDebtAtDay(paymentsMap, firstDayToChargeInterests.minusDays(1));
        BigDecimal interestRateAtFirstDay = interestRateValue(firstDayToChargeInterests);

        NavigableMap<LocalDate, InterestCalculationEvent> result = new TreeMap<>();
        result.put(firstDayToChargeInterests, new InterestCalculationEvent(amountToPayAtFirstDay, interestRateAtFirstDay));
        result.put(nextDayOfInterestsCharge,
                new InterestCalculationEvent(BigDecimal.ZERO, interestRateValue(nextDayOfInterestsCharge)));

        paymentsMap.forEach((settlementPaymentDate, paidAmount) -> {
            LocalDate eventDate = settlementPaymentDate.plusDays(1);
            if (eventDate.isBefore(firstDayToChargeInterests)) {
                return;
            }
            
            if(eventDate.isAfter(nextDayOfInterestsCharge)) {
                return;
            }

            result.putIfAbsent(eventDate,
                    new InterestCalculationEvent(amountInDebtAtDay(paymentsMap, eventDate), interestRateValue(eventDate)));
        });

        GlobalInterestRate.findAll().filter(r -> !r.getFirstDay().isBefore(firstDayToChargeInterests))
                .filter(r -> !r.getFirstDay().isAfter(nextDayOfInterestsCharge)).forEach(r -> {
                    LocalDate eventDate = r.getFirstDay();
                    result.putIfAbsent(eventDate, new InterestCalculationEvent(amountInDebtAtDay(paymentsMap, eventDate),
                            interestRateValue(eventDate)));
                });

        return result;
    }

    private LocalDate calculateFirstDayToChargeInterests(LocalDate lastPayment) {
        LocalDate firstDayToChargeInterests =
                applyOnFirstWorkdayIfNecessary(getDebitEntry().getDueDate().plusDays(numberOfDaysAfterDueDate()));

        if (firstDayToChargeInterests.isBefore(lastPayment.minusYears(MAX_YEARS))) {
            firstDayToChargeInterests = lastPayment.minusYears(MAX_YEARS).plusDays(1);
        }

        return firstDayToChargeInterests;
    }

    private LocalDate calculateLastDayToChargeInterests(LocalDate lastPayment, LocalDate firstDayToChargeInterests) {
        LocalDate nextDayOfPaymentDate = lastPayment;
        if (!isApplyPaymentMonth(lastPayment)) {
            nextDayOfPaymentDate = nextDayOfPaymentDate.withDayOfMonth(1).minusDays(1);
        }

        if (isMaximumDaysToApplyPenaltyApplied()
                && Days.daysBetween(firstDayToChargeInterests, nextDayOfPaymentDate).getDays() > getMaximumDaysToApplyPenalty()) {
            nextDayOfPaymentDate = firstDayToChargeInterests.plusDays(getMaximumDaysToApplyPenalty() - 1);
        }
        return nextDayOfPaymentDate;
    }

    private BigDecimal amountInDebtAtDay(NavigableMap<LocalDate, BigDecimal> paymentsMap, LocalDate eventDate) {
        BigDecimal amountToPay = getDebitEntry().getAmountWithVat();

        for (Entry<LocalDate, BigDecimal> entry : paymentsMap.entrySet()) {
            if (!entry.getKey().isBefore(eventDate)) {
                break;
            }

            amountToPay = amountToPay.subtract(entry.getValue());
        }

        return amountToPay;
    }

    private NavigableMap<LocalDate, BigDecimal> createPaymentsMap(LocalDate paymentDate) {
        NavigableMap<LocalDate, BigDecimal> result = new TreeMap<>();

        getDebitEntry().getSettlementEntriesSet().stream().filter(s -> !s.isAnnulled()).forEach(s -> {
            LocalDate settlementPaymentDate = s.getSettlementNote().getPaymentDate().toLocalDate();
            result.putIfAbsent(settlementPaymentDate, BigDecimal.ZERO);
            result.put(settlementPaymentDate, result.get(settlementPaymentDate).add(s.getAmount()));
        });

        result.putIfAbsent(paymentDate, BigDecimal.ZERO);
        result.put(paymentDate, result.get(paymentDate).add(getDebitEntry().getOpenAmount()));

        return result;
    }

    private boolean isApplyPaymentMonth(final LocalDate date) {
        if (!getInterestType().isGlobalRate()) {
            return false;
        }

        Optional<GlobalInterestRate> globalRate = GlobalInterestRate.findUniqueAppliedForDate(date);
        if (!globalRate.isPresent()) {
            throw new TreasuryDomainException("error.InterestRate.rate.not.defined.for.date",
                    date.toString(TreasuryConstants.DATE_FORMAT_YYYY_MM_DD));
        }

        return globalRate.get().isApplyPaymentMonth();
    }

    private BigDecimal interestRateValue(LocalDate date) {
        if (getInterestType().isGlobalRate()) {
            Optional<GlobalInterestRate> globalRate = GlobalInterestRate.findUniqueAppliedForDate(date);
            if (!globalRate.isPresent()) {
                throw new TreasuryDomainException("error.InterestRate.rate.not.defined.for.date",
                        date.toString(TreasuryConstants.DATE_FORMAT_YYYY_MM_DD));
            }
            return TreasuryConstants.divide(globalRate.get().getRate(), TreasuryConstants.HUNDRED_PERCENT);
        }

        return TreasuryConstants.divide(getRate(), TreasuryConstants.HUNDRED_PERCENT);
    }

    private int numberOfDaysAfterDueDate() {
        if (getInterestType().isGlobalRate()) {
            return 1;
        }

        return getNumberOfDaysAfterDueDate();
    }

    private Currency getRelatedCurrency() {
        if (getTariff() != null) {
            return getTariff().getFinantialEntity().getFinantialInstitution().getCurrency();
        } else if (getDebitEntry() != null) {
            return getDebitEntry().getCurrency();
        }
        return null;
    }

    private InterestRateBean calculateForFixedAmount(boolean withAllInterestValues) {
        final InterestRateBean result = new InterestRateBean(getInterestType());
        BigDecimal totalInterestAmount = this.getRelatedCurrency().getValueWithScale(getInterestFixedAmount());

        if (!withAllInterestValues) {
            for (final Entry<LocalDate, BigDecimal> entry : createdInterestEntriesMap().entrySet()) {
                result.addCreatedInterestEntry(entry.getKey(), entry.getValue());
                totalInterestAmount = totalInterestAmount.subtract(entry.getValue());
            }
        }
        
        if(TreasuryConstants.isNegative(totalInterestAmount)) {
            totalInterestAmount = BigDecimal.ZERO;
        }
        
        result.setInterestAmount(getRelatedCurrency().getValueWithScale(totalInterestAmount));
        return result;
    }

    private LocalDate applyOnFirstWorkdayIfNecessary(final LocalDate date) {
        boolean applyInFirstWorkday = isApplyInFirstWorkday();

        if (getInterestType().isGlobalRate()) {
            Optional<GlobalInterestRate> globalRate = GlobalInterestRate.findUniqueAppliedForDate(date);
            if (!globalRate.isPresent()) {
                throw new TreasuryDomainException("error.InterestRate.rate.not.defined.for.date",
                        date.toString(TreasuryConstants.DATE_FORMAT_YYYY_MM_DD));
            }

            applyInFirstWorkday = globalRate.get().getApplyInFirstWorkday();
        }

        if (applyInFirstWorkday && isSaturday(date)) {
            return date.plusDays(2);
        } else if (applyInFirstWorkday && isSunday(date)) {
            return date.plusDays(1);
        }

        return date;
    }

    public boolean isDeletable() {
        return true;
    }

    @Atomic
    public void delete() {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.InterestRate.cannot.delete");
        }

        setDomainRoot(null);
        setTariff(null);
        setDebitEntry(null);
        deleteDomainObject();
    }

    private boolean isSaturday(final LocalDate date) {
        return date.getDayOfWeek() == DateTimeConstants.SATURDAY;
    }

    private boolean isSunday(final LocalDate date) {
        return date.getDayOfWeek() == DateTimeConstants.SUNDAY;
    }

    // @formatter: off
    /************
     * SERVICES *
     ************/
    // @formatter: on

    public static Stream<InterestRate> findAll() {
        return FenixFramework.getDomainRoot().getInterestRatesSet().stream();
    }

    @Atomic
    public static InterestRate createForTariff(final Tariff tariff, final InterestType interestType,
            final int numberOfDaysAfterDueDate, final boolean applyInFirstWorkday, final int maximumDaysToApplyPenalty,
            final BigDecimal interestFixedAmount, final BigDecimal rate) {
        return new InterestRate(tariff, null, interestType, numberOfDaysAfterDueDate, applyInFirstWorkday,
                maximumDaysToApplyPenalty, interestFixedAmount, rate);
    }

    public String getUiFullDescription() {
        //HACK: This should be moved to the Presentation Layer, but here is easier
        switch (this.getInterestType()) {
        case DAILY:
            return this.getInterestType().getDescriptionI18N().getContent() + "-" + this.getRate() + "% (Max. Dias="
                    + this.getMaximumDaysToApplyPenalty() + ", Aplica 1º Dia Útil=" + this.getApplyInFirstWorkday()
                    + ", Dias após Vencimento=" + this.getNumberOfDaysAfterDueDate() + ")";
        case FIXED_AMOUNT:
            return this.getInterestType().getDescriptionI18N().getContent() + "-"
                    + getRelatedCurrency().getValueFor(this.getInterestFixedAmount());
        case GLOBAL_RATE:
            return this.getInterestType().getDescriptionI18N().getContent();
        default:
            return this.getInterestType().getDescriptionI18N().getContent();
        }
    }

    @Atomic
    public static InterestRate createForDebitEntry(DebitEntry debitEntry, InterestRate interestRate) {
        if (interestRate != null) {
            return new InterestRate(null, debitEntry, interestRate.getInterestType(), interestRate.getNumberOfDaysAfterDueDate(),
                    interestRate.getApplyInFirstWorkday(), interestRate.getMaximumDaysToApplyPenalty(),
                    interestRate.getInterestFixedAmount(), interestRate.getRate());
        }
        return null;
    }

    @Atomic
    public static InterestRate createForDebitEntry(final DebitEntry debitEntry, final InterestType interestType,
            final int numberOfDaysAfterDueDate, final boolean applyInFirstWorkday, final int maximumDaysToApplyPenalty,
            final BigDecimal interestFixedAmount, final BigDecimal rate) {
        return new InterestRate(null, debitEntry, interestType, numberOfDaysAfterDueDate, applyInFirstWorkday,
                maximumDaysToApplyPenalty, interestFixedAmount, rate);
    }

    private class InterestCalculationEvent {
        private BigDecimal amountToPay;
        private BigDecimal interestRate;

        InterestCalculationEvent(BigDecimal amountToPay, BigDecimal interestRateAtEventDate) {
            this.amountToPay = amountToPay;
            this.interestRate = interestRateAtEventDate;
        }
    }
}
