/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Academic.
 *
 * FenixEdu Academic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Academic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Academic.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.treasury.services.payments.sibs.incomming;

import java.math.BigDecimal;

public class SibsIncommingPaymentFileFooter {

    private static final int[] FIELD_SIZES = new int[] { 1, 8, 17, 12, 12, 50 };

    private BigDecimal transactionsTotalAmount;

    private BigDecimal totalCost;

    public SibsIncommingPaymentFileFooter(BigDecimal transactionsTotalAmount, BigDecimal totalCost) {
        this.transactionsTotalAmount = transactionsTotalAmount;
        this.totalCost = totalCost;
    }

    public static SibsIncommingPaymentFileFooter buildFrom(String rawLine) {
        final String[] fields = splitLine(rawLine);
        return new SibsIncommingPaymentFileFooter(getTransactionsTotalAmountFrom(fields), getCostFrom(fields));
    }

    private static BigDecimal getCostFrom(String[] fields) {
        return BigDecimal.valueOf(Double.parseDouble(fields[3].substring(0, 10) + "." + fields[3].substring(10)));
    }

    private static BigDecimal getTransactionsTotalAmountFrom(String[] fields) {
        return BigDecimal.valueOf(Double.parseDouble(fields[2].substring(0, 15) + "." + fields[2].substring(15)));
    }

    private final static String[] splitLine(final String line) {
        int lastIndex = 0;
        final String[] result = new String[FIELD_SIZES.length];
        for (int i = 0; i < FIELD_SIZES.length; i++) {
            result[i] = line.substring(lastIndex, lastIndex + FIELD_SIZES[i]);
            lastIndex += FIELD_SIZES[i];
        }
        return result;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public BigDecimal getTransactionsTotalAmount() {
        return transactionsTotalAmount;
    }

}
