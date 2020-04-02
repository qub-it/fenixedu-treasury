/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and ServiÃ§os Partilhados da
 * Universidade de Lisboa:
 *  - Copyright Â© 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright Â© 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: ricardo.pedro@qub-it.com
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
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.util.TreasuryConstants;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class GlobalInterestRate extends GlobalInterestRate_Base {

    public static final Comparator<? super GlobalInterestRate> FIRST_DATE_COMPARATOR = (o1, o2) -> {
        final int c = o1.getFirstDay().compareTo(o2.getFirstDay());

        return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
    };

    public static Comparator<GlobalInterestRate> COMPARATOR_BY_YEAR = (o1, o2) -> {
        final int c = Integer.compare(o1.getYear(), o2.getYear());

        return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
    };

    protected GlobalInterestRate() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

    protected void init(LocalDate firstDay, final LocalizedString description, final BigDecimal rate, final boolean applyPaymentMonth,
            final boolean applyInFirstWorkday) {
        setFirstDay(firstDay);
        setYear(firstDay.getYear());
        setDescription(description);
        setRate(rate);
        setApplyPaymentMonth(applyPaymentMonth);
        setApplyInFirstWorkday(applyInFirstWorkday);

        checkRules();
    }

    private void checkRules() {
        if(getFirstDay() == null) {
            throw new TreasuryDomainException("error.GlobalInterestRate.firstDay.required");
        }
        
        if (findByFirstDay(getFirstDay()).count() > 1) {
            throw new TreasuryDomainException("error.GlobalInterestRate.firstDay.duplicated");
        }
        
        if(getRate() == null) {
            throw new TreasuryDomainException("error.GlobalInterestRate.rate.with.valid.value.required");
        }
        
        if(StringUtils.isEmpty(getDescription().getContent())) {
            throw new TreasuryDomainException("error.GlobalInterestRate.description.required");
        }
        
        if(TreasuryConstants.isLessThan(getRate(), BigDecimal.ZERO) || 
                TreasuryConstants.isGreaterThan(getRate(), TreasuryConstants.HUNDRED_PERCENT)) {
            throw new TreasuryDomainException("error.GlobalInterestRate.rate.with.valid.value.required");
        }
    }

    @Atomic
    public void edit(LocalDate firstDay, final LocalizedString description, final BigDecimal rate, final boolean applyPaymentMonth,
            final boolean applyInFirstWorkday) {
        setYear(firstDay.getYear());
        setFirstDay(firstDay);
        setDescription(description);
        setRate(rate);
        setApplyPaymentMonth(applyPaymentMonth);
        setApplyInFirstWorkday(applyInFirstWorkday);

        checkRules();
    }

    @Deprecated
    public int getYear() {
        return super.getYear();
    }

    public boolean isApplyPaymentMonth() {
        return super.getApplyPaymentMonth();
    }

    public boolean isDeletable() {
        return true;
    }

    @Atomic
    public void delete() {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.GlobalInterestRate.cannot.delete");
        }

        setDomainRoot(null);

        deleteDomainObject();
    }

    @Atomic
    public static GlobalInterestRate create(LocalDate firstDay, final LocalizedString description, final BigDecimal rate,
            boolean applyPaymentMonth, boolean applyInFirstWorkday) {
        GlobalInterestRate globalInterestRate = new GlobalInterestRate();
        globalInterestRate.init(firstDay, description, rate, applyPaymentMonth, applyInFirstWorkday);
        return globalInterestRate;
    }

    public static Stream<GlobalInterestRate> findAll() {
        return FenixFramework.getDomainRoot().getGlobalInterestRatesSet().stream();
    }

    public static Stream<GlobalInterestRate> findByYear(final int year) {
        return findAll().filter(i -> year == i.getYear());
    }

    public static Optional<GlobalInterestRate> findUniqueByYear(final int year) {
        return findByYear(year).findFirst();
    }

    public static Stream<GlobalInterestRate> findByDescription(final LocalizedString description) {
        return findAll().filter(i -> description.equals(i.getDescription()));
    }

    public static Stream<GlobalInterestRate> findByRate(final BigDecimal rate) {
        return findAll().filter(i -> rate.equals(i.getRate()));
    }
    
    public static Stream<GlobalInterestRate> findByFirstDay(LocalDate date) {
        return findAll().filter(r -> r.getFirstDay().equals(date));
    }
    
    public static Optional<GlobalInterestRate> findUniqueAppliedForDate(LocalDate date) {
        return findAll().filter(r -> !r.getFirstDay().isAfter(date)).sorted(FIRST_DATE_COMPARATOR.reversed()).findFirst();
    }

}
