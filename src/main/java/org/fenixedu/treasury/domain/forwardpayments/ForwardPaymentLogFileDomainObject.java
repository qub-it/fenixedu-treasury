package org.fenixedu.treasury.domain.forwardpayments;

import org.fenixedu.bennu.core.domain.User;
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
    
    public static ForwardPaymentLogFileDomainObject copyAndAssociate(final ForwardPaymentLogFile forwardPaymentLogFile) {
        ForwardPaymentLogFileDomainObject domainObject = new ForwardPaymentLogFileDomainObject();
        
        domainObject.setForwardPaymentLogsForRequest(forwardPaymentLogFile.getForwardPaymentLogsForRequest());
        domainObject.setForwardPaymentLogsForResponse(forwardPaymentLogFile.getForwardPaymentLogsForResponse());
        
        return domainObject;
    }

}
