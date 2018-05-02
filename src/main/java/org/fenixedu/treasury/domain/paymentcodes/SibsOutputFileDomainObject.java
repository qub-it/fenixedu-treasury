package org.fenixedu.treasury.domain.paymentcodes;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;
import org.fenixedu.treasury.services.payments.sibs.outgoing.SibsOutgoingPaymentFile;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.Atomic.TxMode;

public class SibsOutputFileDomainObject extends SibsOutputFileDomainObject_Base {

    public SibsOutputFileDomainObject() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
        setCreationDate(new DateTime());
        setCreator(Authenticate.getUser().getUsername());
    }

    private void checkRules() {
        if (getFinantialInstitution() == null) {
            throw new TreasuryDomainException("error.SibsOutputFile.finantialInstitution.required");
        }
    }

    public boolean isAccessible(User arg0) {
        return true;
    }

    public void edit(final FinantialInstitution finantialInstitution, final java.lang.String errorLog,
            final java.lang.String infoLog, final java.lang.String printedPaymentCodes) {
        setFinantialInstitution(finantialInstitution);
        setErrorLog(errorLog);
        setInfoLog(infoLog);
        setPrintedPaymentCodes(printedPaymentCodes);
        
        checkRules();
    }

    @Override
    protected void checkForDeletionBlockers(Collection<String> blockers) {
        super.checkForDeletionBlockers(blockers);
    }

    public void delete() {
        TreasuryDomainException.throwWhenDeleteBlocked(getDeletionBlockers());

        setFinantialInstitution(null);
        deleteDomainObject();
    }
    
    public static Stream<SibsOutputFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getSibsOutputFilesDomainObjectSet().stream();
    }

    public static SibsOutputFileDomainObject copyAndAssociate(final SibsOutputFile sibsOutputFile) {
        final FinantialInstitution finantialInstitution = sibsOutputFile.getFinantialInstitution();
        final DateTime lastSuccessfulSentDateTime = sibsOutputFile.getLastSuccessfulExportation();
        
        SibsOutputFileDomainObject domainObject = new SibsOutputFileDomainObject();
        
        domainObject.setCreationDate(sibsOutputFile.getCreationDate());
        domainObject.setCreator(sibsOutputFile.getVersioningCreator());
        domainObject.setFinantialInstitution(sibsOutputFile.getFinantialInstitution());
        domainObject.setErrorLog(sibsOutputFile.getErrorLog());
        domainObject.setInfoLog(sibsOutputFile.getInfoLog());
        domainObject.setPrintedPaymentCodes(sibsOutputFile.getPrintedPaymentCodes());
        domainObject.setLastSuccessfulExportation(sibsOutputFile.getLastSuccessfulExportation());
        
        domainObject.setTreasuryFile(sibsOutputFile);
        
        return domainObject;
    }
    
}
