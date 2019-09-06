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
package org.fenixedu.treasury.services.payments.paymentscodegenerator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCode;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCodeStateType;
import org.fenixedu.treasury.domain.paymentcodes.UnusedPaymentCodesBucket;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;
import org.fenixedu.treasury.dto.document.managepayments.PaymentReferenceCodeBean;
import org.joda.time.LocalDate;

import com.google.common.collect.Sets;

import pt.ist.fenixframework.Atomic;

public class SequentialPaymentWithCheckDigitCodeGenerator implements IPaymentCodeGenerator {

    private final PaymentCodePool referenceCodePool;

    @Override
    public PaymentCodePool getReferenceCodePool() {
        return referenceCodePool;
    }

    public SequentialPaymentWithCheckDigitCodeGenerator(PaymentCodePool pool) {
        super();
        this.referenceCodePool = pool;
    }

    @Atomic
    public PaymentReferenceCode generateNewCodeFor(final BigDecimal amount, LocalDate validFrom, LocalDate validTo,
            boolean useFixedAmount) {
        return generateNewCodeFor(amount, validFrom, validTo, useFixedAmount, false);
    }

    private PaymentReferenceCode findNewReferenceCodeWithCustomerBusinessId(final DebtAccount debtAccount,
            final BigDecimal amount, LocalDate validFrom, LocalDate validTo, boolean useFixedAmount) {
        if (!debtAccount.getCustomer().isPersonCustomer()) {
            return null;
        }

        for (Customer customer : debtAccount.getCustomer().getAllCustomers()) {
            final DebtAccount db = customer.getDebtAccountFor(debtAccount.getFinantialInstitution());
            if (db == null) {
                continue;
            }

            final Optional<PaymentReferenceCode> findNewPaymentReferenceCode =
                    findNewPaymentReferenceCodePreAllocatedInSomeBucket(db);
            if (findNewPaymentReferenceCode.isPresent()) {
                final PaymentReferenceCode paymentReferenceCode = findNewPaymentReferenceCode.get();

                useNewReferenceCode(amount, useFixedAmount, paymentReferenceCode);

                return paymentReferenceCode;
            }

        }

        // Fall to new reference codes not allocated to any debt account
        final Optional<PaymentReferenceCode> findNewPaymentReferenceCode = findNewPaymentReferenceCode();
        if (findNewPaymentReferenceCode.isPresent()) {
            final PaymentReferenceCode paymentReferenceCode = findNewPaymentReferenceCode.get();

            useNewReferenceCode(amount, useFixedAmount, paymentReferenceCode);

            return paymentReferenceCode;
        }

        return null;
    }

    private void useNewReferenceCode(final BigDecimal amount, boolean useFixedAmount,
            final PaymentReferenceCode paymentReferenceCode) {
        Long nextReferenceCode = Long.parseLong(paymentReferenceCode.getReferenceCodeWithoutCheckDigits());

        final String referenceCodeString = CheckDigitGenerator
                .generateReferenceCodeWithCheckDigit(referenceCodePool.getEntityReferenceCode(), "" + nextReferenceCode, amount);

        BigDecimal minAmount = referenceCodePool.getMinAmount();
        BigDecimal maxAmount = referenceCodePool.getMaxAmount();
        if (useFixedAmount) {
            minAmount = amount;
            maxAmount = amount;
        }

        paymentReferenceCode.useNewReferenceCodeWithAmountAndCheckDigit(referenceCodeString, minAmount, maxAmount, amount);
    }

    @Atomic
    public PaymentReferenceCode generateNewCodeFor(final BigDecimal amount, LocalDate validFrom, LocalDate validTo,
            boolean useFixedAmount, final boolean forceGeneration) {

        if (validFrom == null) {
            validFrom = referenceCodePool.getValidFrom();
        }
        if (validTo == null) {
            validTo = referenceCodePool.getValidTo();
        }

        Long nextReferenceCode = referenceCodePool.getAndIncrementNextReferenceCode();
        if (nextReferenceCode > referenceCodePool.getMaxReferenceCode()) {
            //The pool is "OVER"... Try to get the first unused code

            PaymentReferenceCode availableReferenceCode = this.referenceCodePool.getPaymentReferenceCodesSet().stream()
                    .filter(x -> x.isAvailableForReuse()).findFirst().orElse(null);
            if (availableReferenceCode != null) {
                nextReferenceCode = Long.parseLong(availableReferenceCode.getReferenceCodeWithoutCheckDigits());
            } else {
                throw new TreasuryDomainException("error.PaymentCodeGenerator.not.paymentreferences.available.in.pool");
            }
        }

        final String referenceCodeString = CheckDigitGenerator
                .generateReferenceCodeWithCheckDigit(referenceCodePool.getEntityReferenceCode(), "" + nextReferenceCode, amount);

        BigDecimal minAmount = referenceCodePool.getMinAmount();
        BigDecimal maxAmount = referenceCodePool.getMaxAmount();
        if (useFixedAmount) {
            minAmount = amount;
            maxAmount = amount;
        }

        PaymentReferenceCode newPaymentReference = PaymentReferenceCode.create(referenceCodeString, validFrom, validTo,
                PaymentReferenceCodeStateType.UNUSED, referenceCodePool, minAmount, maxAmount);
        newPaymentReference.setPayableAmount(amount);

        return newPaymentReference;
    }

    private Optional<PaymentReferenceCode> findNewPaymentReferenceCodePreAllocatedInSomeBucket(DebtAccount debtAccount) {
        {
            List<UnusedPaymentCodesBucket> bucketList = UnusedPaymentCodesBucket.findAll()
                    .sorted(UnusedPaymentCodesBucket.COMPARE_BY_BUCKET_INDEX).collect(Collectors.toList());

            if (bucketList.isEmpty()) {
                return Optional.empty();
            }

            int bucketIndexToUse = (int) (Long.valueOf(debtAccount.getExternalId()) % bucketList.size());

            final List<PaymentReferenceCode> list = bucketList.get(bucketIndexToUse).getPreAllocatedPaymentReferenceCodesSet()
                    .stream().filter(p -> p.getPaymentCodePool() == this.referenceCodePool).filter(p -> p.isNew())
                    .filter(p -> p.getTargetPayment() == null).collect(Collectors.toList());

            if (!list.isEmpty()) {
                return Optional.of(list.get(0));
            }
        }

        return Optional.empty();
    }

    private Optional<PaymentReferenceCode> findNewPaymentReferenceCode() {
        final List<PaymentReferenceCode> list = this.referenceCodePool.getPaymentReferenceCodesSet().stream()
                .filter(p -> p.isNew()).filter(p -> p.getTargetPayment() == null)
                .collect(Collectors.toList());

        if (!list.isEmpty()) {
            return Optional.of(list.get(0));
        }

        return Optional.empty();
    }

    protected Set<PaymentReferenceCode> allPaymentCodes(PaymentCodePool referenceCodePool) {
        return referenceCodePool.getPaymentReferenceCodesSet();
    }

    @Override
    @Atomic
    public PaymentReferenceCode createPaymentReferenceCode(final DebtAccount debtAccount, final PaymentReferenceCodeBean bean) {
        PaymentReferenceCode paymentReferenceCode = findNewReferenceCodeWithCustomerBusinessId(debtAccount,
                bean.getPaymentAmount(), bean.getBeginDate(), bean.getEndDate(), bean.getPaymentCodePool().getIsFixedAmount());

        if (paymentReferenceCode == null) {
            paymentReferenceCode = generateNewCodeFor(bean.getPaymentAmount(), bean.getBeginDate(), bean.getEndDate(),
                    bean.getPaymentCodePool().getIsFixedAmount());
        }

        paymentReferenceCode.createPaymentTargetTo(Sets.newHashSet(bean.getSelectedDebitEntries()), bean.getPaymentAmount());

        return paymentReferenceCode;
    }

    @Override
    public boolean isSibsMerchantTransactionAndReferenceIdRequired() {
        return false;
    }

}
