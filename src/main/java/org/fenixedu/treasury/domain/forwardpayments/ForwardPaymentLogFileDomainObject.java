package org.fenixedu.treasury.domain.forwardpayments;

import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.joda.time.DateTime;

import pt.ist.fenixframework.FenixFramework;

public class ForwardPaymentLogFileDomainObject extends ForwardPaymentLogFileDomainObject_Base {

    private ForwardPaymentLogFileDomainObject() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

    public boolean isAccessible(final User user) {
        throw new RuntimeException("not implemented");
    }
    
    public String getContentAsString() {
//        if(getContent() != null) {
//            return new String(getContent());
//        }
        
        return null;
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on

    public static Stream<ForwardPaymentLogFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getForwardPaymentLogFileDomainObjectSet().stream();
    }
    
    public static ForwardPaymentLogFileDomainObject copyAndAssociate(final ForwardPaymentLogFile forwardPaymentLogFile) {
        
        if(forwardPaymentLogFile.getFileForwardPaymentLogFile() != null) {
            throw new TreasuryDomainException("error.ForwardPaymentLogFileDomainObject.already.with.copy");
        }
        
        ForwardPaymentLogFileDomainObject domainObject = new ForwardPaymentLogFileDomainObject();
        
        domainObject.setCreationDate(forwardPaymentLogFile.getCreationDate());
        domainObject.setCreator(forwardPaymentLogFile.getVersioningCreator());
        domainObject.setForwardPaymentLogsForRequest(forwardPaymentLogFile.getForwardPaymentLogsForRequest());
        domainObject.setForwardPaymentLogsForResponse(forwardPaymentLogFile.getForwardPaymentLogsForResponse());
        
        return domainObject;
    }

}
