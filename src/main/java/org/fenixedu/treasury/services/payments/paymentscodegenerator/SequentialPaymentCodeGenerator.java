package org.fenixedu.treasury.services.payments.paymentscodegenerator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCode;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCodeStateType;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;

public class SequentialPaymentCodeGenerator extends PaymentCodeGenerator {

    public static Comparator<PaymentReferenceCode> COMPARATOR_BY_PAYMENT_SEQUENTIAL_DIGITS =
            new Comparator<PaymentReferenceCode>() {
                @Override
                public int compare(PaymentReferenceCode leftPaymentCode, PaymentReferenceCode rightPaymentCode) {
                    final String leftSequentialNumber = getSequentialNumber(leftPaymentCode);
                    final String rightSequentialNumber = getSequentialNumber(rightPaymentCode);

                    int comparationResult = leftSequentialNumber.compareTo(rightSequentialNumber);

                    return comparationResult == 0 ? leftPaymentCode.getExternalId().compareTo(rightPaymentCode.getExternalId()) : comparationResult;
                }
            };

    private final PaymentCodePool referenceCodePool;

    public SequentialPaymentCodeGenerator(PaymentCodePool pool) {
        super();
        this.referenceCodePool = pool;
    }

    private static final String CODE_FILLER = "0";
    private static final int NUM_CONTROL_DIGITS = 2;
    private static final int NUM_SEQUENTIAL_NUMBERS = 7;

    @Override
    public boolean canGenerateNewCode(boolean forceGeneration) {
        if(!this.referenceCodePool.isGenerateReferenceCodeOnDemand() && !forceGeneration) {
            return false;
        }
        
        final PaymentReferenceCode lastPaymentCode = findLastPaymentCode();
        return lastPaymentCode == null ? true : Integer.valueOf(getSequentialNumber(lastPaymentCode)) < referenceCodePool
                .getMaxReferenceCode();
    }

    protected PaymentReferenceCode findLastPaymentCode() {
        final Set<PaymentReferenceCode> paymentCodes = allPaymentCodes();
        return paymentCodes.isEmpty() ? null : Collections.max(paymentCodes, COMPARATOR_BY_PAYMENT_SEQUENTIAL_DIGITS);
    }

    protected Set<PaymentReferenceCode> allPaymentCodes() {
        return this.referenceCodePool.getPaymentReferenceCodesSet();
    }

    @Override
    @Atomic
    public PaymentReferenceCode generateNewCodeFor(BigDecimal amount, LocalDate validFrom, LocalDate validTo,
            boolean useFixedAmount) {
        return generateNewCodeFor(amount, validFrom, validTo, useFixedAmount, false);
    }
    
    @Override
    @Atomic
    public PaymentReferenceCode generateNewCodeFor(BigDecimal amount, LocalDate validFrom, LocalDate validTo,
            boolean useFixedAmount, final boolean forceGeneration) {

        if(!forceGeneration) {
            // First find unused payment code reference
            for (final PaymentReferenceCode paymentReferenceCode : this.referenceCodePool.getPaymentReferenceCodesSet()) {
                if(!paymentReferenceCode.isNew()) {
                    continue;
                }
                
                // Check if is associated with debt account
                //if(paymentReferenceCode.getDebtAccount() != null) {
                //    continue;
                //}
                
                if(Constants.isGreaterThan(amount, paymentReferenceCode.getMaxAmount())) {
                    continue;
                }
                
                if(Constants.isLessThan(amount, paymentReferenceCode.getMinAmount())) {
                    continue;
                }
                
                if(paymentReferenceCode.getTargetPayment() != null) {
                    continue;
                }
                
                if(validTo != null && !paymentReferenceCode.getValidInterval().contains(validTo.toDateTimeAtStartOfDay())) {
                    continue;
                } else if(!paymentReferenceCode.getValidInterval().contains(new DateTime())) {
                    continue;
                }
                
                if(validFrom != null && !paymentReferenceCode.getValidInterval().contains(validFrom.toDateTimeAtStartOfDay())) {
                    continue;
                } else if(!paymentReferenceCode.getValidInterval().contains(new DateTime())) {
                    continue;
                }
                
                paymentReferenceCode.setPayableAmount(amount);
                return paymentReferenceCode;
            }
        }
        
        if (!canGenerateNewCode(forceGeneration)) {
            throw new RuntimeException("Cannot generate new payment codes");
        }

        final PaymentReferenceCode lastPaymentCode = findLastPaymentCode();
        long nextSequentialNumber =
                lastPaymentCode != null ? Integer.valueOf(getSequentialNumber(lastPaymentCode)) + 1 : referenceCodePool
                        .getMinReferenceCode();

        String sequentialNumberPadded =
                StringUtils.leftPad(String.valueOf(nextSequentialNumber), NUM_SEQUENTIAL_NUMBERS, CODE_FILLER);
        String controDigitsPadded =
                StringUtils.leftPad(String.valueOf(new Random().nextInt(99)), NUM_CONTROL_DIGITS, CODE_FILLER);

        String referenceCodeString = sequentialNumberPadded + controDigitsPadded;

        BigDecimal minAmount = referenceCodePool.getMinAmount();
        BigDecimal maxAmount = referenceCodePool.getMaxAmount();
        if (useFixedAmount) {
            minAmount = amount;
            maxAmount = amount;
        } else {
            //Correct max amount if needed
            if (Constants.isGreaterThan(amount, maxAmount)) {
                maxAmount = amount;
            }
        }

        PaymentReferenceCode newPaymentReference =
                PaymentReferenceCode.create(referenceCodeString, validFrom, validTo, PaymentReferenceCodeStateType.UNUSED,
                        referenceCodePool, minAmount, maxAmount);

        newPaymentReference.setPayableAmount(amount);
        return newPaymentReference;
    }

    @Override
    public boolean isCodeMadeByThisFactory(PaymentReferenceCode paymentCode) {
        return paymentCode.getPaymentCodePool().equals(this);
    }

    private static String getSequentialNumber(PaymentReferenceCode paymentCode) {
        return paymentCode.getReferenceCode().substring(0, paymentCode.getReferenceCode().length() - NUM_CONTROL_DIGITS);
    }

}
