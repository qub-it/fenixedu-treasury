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
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCode;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCodeStateType;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;

public class SequentialPaymentWithCheckDigitCodeGenerator extends PaymentCodeGenerator {

    private static final String CODE_FILLER = "0";
    private static final int NUM_CONTROL_DIGITS = 2;
    private static final int NUM_SEQUENTIAL_NUMBERS = 7;

    private final PaymentCodePool referenceCodePool;

    public SequentialPaymentWithCheckDigitCodeGenerator(PaymentCodePool pool) {
        super();
        this.referenceCodePool = pool;
    }

    @Override
    public boolean canGenerateNewCode(boolean forceGeneration) {
        if(!this.referenceCodePool.isGenerateReferenceCodeOnDemand() && !forceGeneration) {
            return false;
        }
        
        final PaymentReferenceCode lastPaymentCode = findLastPaymentReferenceCode();
        return lastPaymentCode == null ? true : Integer.valueOf(getSequentialNumber(lastPaymentCode)) < referenceCodePool
                .getMaxReferenceCode();
    }

    @Override
    @Atomic
    public PaymentReferenceCode generateNewCodeFor(final BigDecimal amount, LocalDate validFrom, LocalDate validTo,
            boolean useFixedAmount) {
        return generateNewCodeFor(amount, validFrom, validTo, useFixedAmount, false);
    }
    
    @Override
    @Atomic
    public PaymentReferenceCode generateNewCodeFor(final BigDecimal amount, LocalDate validFrom, LocalDate validTo,
            boolean useFixedAmount, final boolean forceGeneration) {

        if (validFrom == null) {
            validFrom = referenceCodePool.getValidFrom();
        }
        if (validTo == null) {
            validTo = referenceCodePool.getValidTo();
        }
        PaymentReferenceCode lastReferenceCode = findLastPaymentReferenceCode();
        long nextReferenceCode = 0;
        if (lastReferenceCode == null) {
            nextReferenceCode = referenceCodePool.getMinReferenceCode();
        } else {

            String lastReferenceCodeString = lastReferenceCode.getReferenceCodeWithoutCheckDigits();
            Long lastReferenceCodeValue = Long.parseLong(lastReferenceCodeString);
            if (lastReferenceCodeValue < referenceCodePool.getMaxReferenceCode()) {
                nextReferenceCode = lastReferenceCodeValue + 1;
            }
        }

        if (nextReferenceCode > referenceCodePool.getMaxReferenceCode()) {
            //The pool is "OVER"... Try to get the first unused code

            PaymentReferenceCode availableReferenceCode =
                    this.referenceCodePool.getPaymentReferenceCodesSet().stream().filter(x -> x.isAvailableForReuse())
                            .findFirst().orElse(null);
            if (availableReferenceCode != null) {
                nextReferenceCode = Long.parseLong(availableReferenceCode.getReferenceCodeWithoutCheckDigits());
            } else {
                throw new TreasuryDomainException("error.PaymentCodeGenerator.not.paymentreferences.available.in.pool");
            }
        }

        String referenceCodeString = "";

//        if (Boolean.TRUE.equals(referenceCodePool.getUseAmountToValidateCheckDigit())) {
        referenceCodeString =
                CheckDigitGenerator.generateReferenceCodeWithCheckDigit(referenceCodePool.getEntityReferenceCode(), ""
                        + nextReferenceCode, amount);
//        } else {
//            referenceCodeString =
//                    CheckDigitGenerator.generateReferenceCodeWithCheckDigit(referenceCodePool.getEntityReferenceCode(), ""
//                            + nextReferenceCode, BigDecimal.ZERO);
//        }

        BigDecimal minAmount = referenceCodePool.getMinAmount();
        BigDecimal maxAmount = referenceCodePool.getMaxAmount();
        if (useFixedAmount) {
            minAmount = amount;
            maxAmount = amount;
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

    protected PaymentReferenceCode findLastPaymentReferenceCode() {
        //Sort the payment referenceCodes
        final List<PaymentReferenceCode> paymentCodes =
                allPaymentCodes(referenceCodePool)
                        .stream()
                        .sorted((x, y) -> Long.valueOf(x.getReferenceCodeWithoutCheckDigits()).compareTo(
                                Long.valueOf(y.getReferenceCodeWithoutCheckDigits()))).collect(Collectors.toList());

        PaymentReferenceCode last = null;
        for (PaymentReferenceCode referenceCode : paymentCodes) {
            if (last == null) {
                last = referenceCode;
            } else {
                //if there is an emptyspace in the pool, use the emptySpace
                if (Long.parseLong(referenceCode.getReferenceCodeWithoutCheckDigits()) > Long.parseLong(last
                        .getReferenceCodeWithoutCheckDigits()) + 1) {
                    break;
                } else {
                    last = referenceCode;
                }
            }
        }
        return last;
    }

    protected Set<PaymentReferenceCode> allPaymentCodes(PaymentCodePool referenceCodePool) {
        return PaymentReferenceCode.findAll().filter(x -> x.getPaymentCodePool().equals(referenceCodePool))
                .collect(Collectors.toSet());
    }

//
//    @Override
//    public String generateNewCodeFor(PaymentCodeType paymentCodeType, Person person) {
//
//        if (!canGenerateNewCode(paymentCodeType, person)) {
//            throw new RuntimeException("Cannot generate new payment codes");
//        }
//
//        final PaymentCode lastPaymentCode = findLastPaymentCode();
//        int nextSequentialNumber = lastPaymentCode != null ? Integer.valueOf(getSequentialNumber(lastPaymentCode)) + 1 : 0;
//
//        String sequentialNumberPadded =
//                StringUtils.leftPad(String.valueOf(nextSequentialNumber), NUM_SEQUENTIAL_NUMBERS, CODE_FILLER);
//        String controDigitsPadded =
//                StringUtils.leftPad(String.valueOf((new Random()).nextInt(99)), NUM_CONTROL_DIGITS, CODE_FILLER);
//
//        return sequentialNumberPadded + controDigitsPadded;
//    }
//

//
    private static String getSequentialNumber(PaymentReferenceCode paymentCode) {
        return paymentCode.getReferenceCode().substring(0, paymentCode.getReferenceCode().length() - NUM_CONTROL_DIGITS);
    }

}