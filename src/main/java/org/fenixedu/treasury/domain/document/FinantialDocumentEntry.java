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
package org.fenixedu.treasury.domain.document;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.util.Constants;

import pt.ist.fenixframework.Atomic;

public abstract class FinantialDocumentEntry extends FinantialDocumentEntry_Base {

    public abstract BigDecimal getTotalAmount();

    protected FinantialDocumentEntry() {
        super();
        setBennu(Bennu.getInstance());
    }

    protected void init(final FinantialDocument finantialDocument, final FinantialEntryType finantialEntryType,
            final BigDecimal amount, String description) {
        setFinantialDocument(finantialDocument);
        setFinantialEntryType(finantialEntryType);
        setAmount(amount);
        setDescription(description);
    }

    public void checkRules() {
        if (isFinantialDocumentRequired() && getFinantialDocument() == null) {
            throw new TreasuryDomainException("error.FinantialDocumentEntry.finantialDocument.required");
        }

        if (getFinantialEntryType() == null) {
            throw new TreasuryDomainException("error.FinantialDocumentEntry.finantialEntryType.required");
        }

        if (getAmount() == null) {
            throw new TreasuryDomainException("error.FinantialDocumentEntry.amount.required");
        }

        if (getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new TreasuryDomainException("error.FinantialDocumentEntry.amount.less.than.zero");
        }
    }

    public boolean isFinantialDocumentRequired() {
        return true;
    }

    @Override
    protected void checkForDeletionBlockers(Collection<String> blockers) {
        if (getFinantialDocument() != null && getFinantialDocument().getState() != FinantialDocumentStateType.PREPARING) {
            blockers.add(BundleUtil.getString(Constants.BUNDLE,
                    "error.finantialdocumententry.cannot.be.deleted.document.is.not.preparing"));
        }
    }

    @Atomic
    public void delete() {
        TreasuryDomainException.throwWhenDeleteBlocked(getDeletionBlockers());

        setBennu(null);
        if (getFinantialDocument() != null) {
            getFinantialDocument().removeFinantialDocumentEntries(this);
        }

        setFinantialDocument(null);

        deleteDomainObject();
    }

    // @formatter: off
    /************
     * UTILS *
     ************/
    // @formatter: on

    protected boolean isNegative(final BigDecimal value) {
        return !isZero(value) && !isPositive(value);
    }

    protected boolean isZero(final BigDecimal value) {
        return BigDecimal.ZERO.compareTo(value) == 0;
    }

    protected boolean isPositive(final BigDecimal value) {
        return BigDecimal.ZERO.compareTo(value) < 0;
    }

    protected boolean isGreaterThan(final BigDecimal v1, final BigDecimal v2) {
        return v1.compareTo(v2) > 0;
    }

    // @formatter: off
    /************
     * SERVICES *
     ************/
    // @formatter: on

    public static Stream<? extends FinantialDocumentEntry> findAll() {
        return Bennu.getInstance().getFinantialDocumentEntriesSet().stream();
    }

}
