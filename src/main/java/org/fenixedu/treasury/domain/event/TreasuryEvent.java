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
package org.fenixedu.treasury.domain.event;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.CreditEntry;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.exemption.TreasuryExemptionType;
import org.fenixedu.treasury.util.Constants;
import org.springframework.util.StringUtils;

import pt.ist.fenixframework.Atomic;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public abstract class TreasuryEvent extends TreasuryEvent_Base {

    protected TreasuryEvent() {
        super();
        setBennu(Bennu.getInstance());
    }

    protected void init(final DebtAccount debtAccount, final Product product) {
        setDebtAccount(debtAccount);
        setProduct(product);

        setDescription(product.getName());
    }

    protected void checkRules() {
        if (getDebtAccount() == null) {
            throw new TreasuryDomainException("error.TreasuryEvent.debtAccount.required");
        }

        if (getProduct() == null) {
            throw new TreasuryDomainException("error.TreasuryEvent.product.required");
        }
    }

    /* -----------------------------
     * FINANTIAL INFORMATION RELATED
     * -----------------------------
     */

    public boolean isChargedWithDebitEntry() {
        return isChargedWithDebitEntry(null);
    }

    public boolean isChargedWithDebitEntry(final Product product) {
        if (product != null) {
            return DebitEntry.findActive(this, product).count() > 0;
        }

        return DebitEntry.findActive(this).filter(d -> !d.isEventAnnuled()).count() > 0;
    }

    public boolean isAnyDebitEntryWithCreditApplied() {
        return isAnyDebitEntryWithCreditApplied(null);
    }

    public boolean isAnyDebitEntryWithCreditApplied(final Product product) {
        if (product != null) {
            return CreditEntry.findActive(this, product).count() > 0;
        }

        return CreditEntry.findActive(this).count() > 0;
    }

    public BigDecimal getAmountToPay() {
        return getAmountToPay(null);
    }

    public BigDecimal getAmountToPay(final Product product) {
        final BigDecimal result =
                (product != null ? DebitEntry.findActive(this, product) : DebitEntry.findActive(this))
                        .map(d -> d.getAmountWithVat()).reduce((x, y) -> x.add(y)).orElse(BigDecimal.ZERO)
                        .subtract(getCreditAmount());

        return Constants.isPositive(result) ? result : BigDecimal.ZERO;
    }

    public BigDecimal getCreditAmount() {
        return getCreditAmount(null);
    }

    public BigDecimal getCreditAmount(final Product product) {
        return (product != null ? CreditEntry.findActive(this) : CreditEntry.findActive(this, product))
                .map(c -> c.getAmountWithVat()).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);
    }

    public BigDecimal getPayedAmount() {
        return DebitEntry.payedAmount(this);
    }

    public BigDecimal getRemainingAmountToPay() {
        return DebitEntry.remainingAmountToPay(this);
    }

    protected String propertiesMapToJson(final Map<String, String> propertiesMap) {
        final GsonBuilder builder = new GsonBuilder();

        final Gson gson = builder.create();
        final Type stringStringMapType = new TypeToken<Map<String, String>>() {
        }.getType();

        return gson.toJson(propertiesMap, stringStringMapType);
    }

    public Map<String, String> getPropertiesMap() {
        if (StringUtils.isEmpty(getPropertiesJsonMap())) {
            return null;
        }

        final GsonBuilder builder = new GsonBuilder();

        final Gson gson = builder.create();
        final Type stringStringMapType = new TypeToken<Map<String, String>>() {
        }.getType();

        final Map<String, String> propertiesMap = gson.fromJson(getPropertiesJsonMap(), stringStringMapType);

        return propertiesMap;
    }

    public Set<Product> getPossibleProductsToExempt() {
        return Sets.newHashSet(getProduct());
    }
    
    public boolean isDeletable() {
        return true;
    }

    @Atomic
    public void delete() {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.TreasuryEvent.cannot.delete");
        }

        setBennu(null);

        super.deleteDomainObject();
    }

    // @formatter: off
    /************
     * SERVICES *
     ************/
    // @formatter: on

    public static Stream<? extends TreasuryEvent> findAll() {
        return Bennu.getInstance().getTreasuryEventsSet().stream();
    }

    public static Stream<? extends TreasuryEvent> findActiveBy(DebtAccount debtAccount) {
        return findAll().filter(x -> x.getDebtAccount().equals(debtAccount));
    }


}
