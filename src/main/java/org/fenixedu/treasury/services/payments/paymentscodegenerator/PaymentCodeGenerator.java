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
package org.fenixedu.treasury.services.payments.paymentscodegenerator;

import java.math.BigDecimal;

import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCode;
import org.joda.time.LocalDate;

public abstract class PaymentCodeGenerator {
    public abstract boolean canGenerateNewCode(boolean forceGeneration);

    public abstract PaymentReferenceCode generateNewCodeFor(BigDecimal amount, LocalDate validFrom,
            LocalDate validTo, boolean useFixedAmount);

    public abstract PaymentReferenceCode generateNewCodeFor(BigDecimal amount, LocalDate validFrom,
            LocalDate validTo, boolean useFixedAmount, boolean forceGeneration);
    
    public abstract boolean isCodeMadeByThisFactory(final PaymentReferenceCode paymentCode);

//    public abstract void refreshReferenceCodeGenerator();
}
