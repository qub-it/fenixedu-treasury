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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.CreditEntry;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.exemption.TreasuryExemption;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

import pt.ist.fenixframework.Atomic;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public abstract class TreasuryEvent extends TreasuryEvent_Base {
    // @formatter:off
    public static enum TreasuryEventKeys {
        EXECUTION_YEAR, EXECUTION_SEMESTER, DEGREE_CODE;

        public LocalizedString getDescriptionI18N() {
            return BundleUtil.getLocalizedString(Constants.BUNDLE, "label." + TreasuryEvent.class.getSimpleName() + "." + name());
        }

    }

    public abstract String getERPIntegrationMetadata();

    protected TreasuryEvent() {
        super();
        setBennu(Bennu.getInstance());
    }

    protected void init(final DebtAccount debtAccount, final Product product, final LocalizedString description) {
        setDebtAccount(debtAccount);
        setProduct(product);

        setDescription(description);
    }

    protected void checkRules() {
        if (getDebtAccount() == null) {
            throw new TreasuryDomainException("error.TreasuryEvent.debtAccount.required");
        }
    }

    /* -----------------------------
     * FINANTIAL INFORMATION RELATED
     * -----------------------------
     */

    public boolean isExempted(final Product product) {
        return TreasuryExemption.findUnique(this, product).isPresent();
    }

    public boolean isChargedWithDebitEntry() {
        return isChargedWithDebitEntry(null);
    }

    public boolean isChargedWithDebitEntry(final Product product) {
        if (product != null) {
            return DebitEntry.findActive(this, product).count() > 0;
        }

        return DebitEntry.findActive(this).filter(d -> !d.isEventAnnuled()).count() > 0;
    }

    // TODO: getTotalAmount()
    public BigDecimal getAmountToPay() {
        return getAmountToPay(null);
    }

    // TODO: getTotalAmount()
    public BigDecimal getAmountToPay(final Product product) {
        final BigDecimal result =
                (product != null ? DebitEntry.findActive(this, product) : DebitEntry.findActive(this))
                        .map(d -> d.getTotalAmount()).reduce((x, y) -> x.add(y)).orElse(BigDecimal.ZERO)
                        .subtract(getCreditAmount(product));

        return Constants.isPositive(result) ? result : BigDecimal.ZERO;
    }

    public BigDecimal getCreditAmount() {
        return getCreditAmount(null);
    }

    public BigDecimal getCreditAmount(final Product product) {
        return (product != null ? CreditEntry.findActive(this, product) : CreditEntry.findActive(this))
                .map(c -> c.getAmountWithVat()).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);
    }

    public BigDecimal getPayedAmount() {
        return getAmountToPay().subtract(getRemainingAmountToPay());
    }

    public BigDecimal getRemainingAmountToPay() {
        return getRemainingAmountToPay(null);
    }

    public BigDecimal getRemainingAmountToPay(final Product product) {
        BigDecimal result = BigDecimal.ZERO;

        for (final DebitEntry debitEntry : DebitEntry.findActive(this).collect(Collectors.<DebitEntry> toSet())) {
            result = result.add(debitEntry.getOpenAmount());
        }

        return Constants.isPositive(result) ? result : BigDecimal.ZERO;
    }

    public BigDecimal getExemptedAmount() {
        BigDecimal result =
                DebitEntry.findActive(this).map(l -> l.getExemptedAmount()).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);

        result =
                result.add(CreditEntry.findActive(this).filter(l -> l.isFromExemption()).map(l -> l.getAmountWithVat())
                        .reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO));

        return result;
    }

    public BigDecimal getExemptedAmount(final DebitEntry debitEntry) {
        BigDecimal result = debitEntry.getExemptedAmount();

        result =
                result.add(debitEntry.getCreditEntriesSet().stream().map(l -> l.getAmountWithVat())
                        .reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO));

        return result;
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
            return new HashMap<String, String>();
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

    public abstract LocalDate getTreasuryEventDate();

    @Atomic
    public void delete() {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.TreasuryEvent.cannot.delete");
        }

        setBennu(null);

        super.deleteDomainObject();
    }

    @Atomic
    public void annulAllDebitEntries(final String reason) {

        if (isAbleToDeleteAllDebitEntries()) {
            DebitEntry.findActive(this).forEach(x -> x.delete());
        } else {
            final String reasonDescription =
                    Constants.bundle("label.TreasuryEvent.credit.by.annulAllDebitEntries.reason", reason);

            final Set<DebitEntry> unprocessedDebitEntries = Sets.newHashSet();
            while(DebitEntry.findActive(this).map(DebitEntry.class::cast).count() > 0) {
                final DebitEntry debitEntry = DebitEntry.findActive(this).map(DebitEntry.class::cast).findFirst().get();

                if(Constants.isEqual(debitEntry.getAvailableAmountForCredit(), BigDecimal.ZERO)) {
                    throw new TreasuryDomainException("error.TreasuryEvent.annulAllDebitEntries.debitEntry.nothing.to.credit", debitEntry.getDescription());
                }
                
                if (debitEntry.isAnnulled()) {
                    continue;
                }

                if (!debitEntry.isProcessedInDebitNote()) {
                    unprocessedDebitEntries.add(debitEntry);
                    // Remove from active debit entries
                    debitEntry.annulOnEvent();
                    continue;
                }

                if (!debitEntry.isProcessedInClosedDebitNote()) {
                    debitEntry.getFinantialDocument().closeDocument();
                }

                // ensure interest debit entries are closed in document entry
                for (final DebitEntry otherDebitEntry : ((DebitNote) debitEntry.getFinantialDocument()).getDebitEntriesSet()) {
                    for (final DebitEntry interestDebitEntry : otherDebitEntry.getInterestDebitEntriesSet()) {
                        if (!interestDebitEntry.isProcessedInDebitNote()) {
                            final DebitNote debitNoteForUnprocessedEntries = DebitNote.create(
                                    getDebtAccount(),
                                    DocumentNumberSeries.findUniqueDefault(FinantialDocumentType.findForDebitNote(),
                                            getDebtAccount().getFinantialInstitution()).get(), new DateTime());
                            interestDebitEntry.setFinantialDocument(debitNoteForUnprocessedEntries);
                        }

                        if (!interestDebitEntry.isProcessedInClosedDebitNote()) {
                            interestDebitEntry.getFinantialDocument().closeDocument();
                        }
                    }
                }
                
                ((DebitNote) debitEntry.getFinantialDocument()).anullDebitNoteWithCreditNote(reasonDescription);

                for (final DebitEntry otherDebitEntry : ((DebitNote) debitEntry.getFinantialDocument()).getDebitEntriesSet()) {
                    for (final DebitEntry interestDebitEntry : otherDebitEntry.getInterestDebitEntriesSet()) {
                        interestDebitEntry.annulOnEvent();
                    }
                    
                    otherDebitEntry.annulOnEvent();
                }
            }

            if (!unprocessedDebitEntries.isEmpty()) {
                final DebitNote debitNoteForUnprocessedEntries =
                        DebitNote.create(
                                getDebtAccount(),
                                DocumentNumberSeries.findUniqueDefault(FinantialDocumentType.findForDebitNote(),
                                        getDebtAccount().getFinantialInstitution()).get(), new DateTime());

                debitNoteForUnprocessedEntries.addDebitNoteEntries(Lists.newArrayList(unprocessedDebitEntries));
                debitNoteForUnprocessedEntries.closeDocument();
                debitNoteForUnprocessedEntries.anullDebitNoteWithCreditNote(reasonDescription);
            }

            for (final DebitEntry debitEntry : getDebitEntriesSet()) {
                for (final DebitEntry interestDebitEntry : debitEntry.getInterestDebitEntriesSet()) {
                    interestDebitEntry.annulOnEvent();
                }
                
                debitEntry.annulOnEvent();
            }
        }

        while (!getTreasuryExemptionsSet().isEmpty()) {
            getTreasuryExemptionsSet().iterator().next().delete();
        }
    }

    @Atomic
    public void transferToDebtAccount(final DebtAccount debtAccount) {
        if(!getDebitEntriesSet().isEmpty()) {
            throw new TreasuryDomainException("error.TreasuryEvent.transferToDebtAccount.not.possible.debit.entries.not.empty");
        }
        
        if(debtAccount.getFinantialInstitution() != getDebtAccount().getFinantialInstitution()) {
            throw new TreasuryDomainException("error.TreasuryEvent.transferToDebtAccount.debtAccounts.with.same.finantial.institution");
        }
        
        setDebtAccount(debtAccount);
        checkRules();
    }
    
    
//    private void closeDebitEntry(final DebitEntry debitEntry, final CreditEntry creditEntry, final String reasonDescription) {
//        final SettlementNote settlementNote =
//                SettlementNote.create(
//                        debitEntry.getDebtAccount(),
//                        DocumentNumberSeries.findUniqueDefault(FinantialDocumentType.findForSettlementNote(),
//                                getDebtAccount().getFinantialInstitution()).get(), new DateTime(), null);
//
//        SettlementEntry.create(debitEntry, settlementNote, creditEntry.getOpenAmount(), debitEntry.getDescription(),
//                new DateTime(), false);
//        SettlementEntry.create(creditEntry, settlementNote, debitEntry.getOpenAmount(), creditEntry.getDescription(),
//                new DateTime(), false);
//
//        settlementNote.setDocumentObservations(reasonDescription);
//        settlementNote.closeDocument();
//    }

    public boolean isAbleToDeleteAllDebitEntries() {
        return DebitEntry.findActive(this).map(l -> l.isDeletable()).reduce((a, c) -> a && c).orElse(Boolean.TRUE);
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
