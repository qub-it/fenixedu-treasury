package org.fenixedu.treasury.domain.forwardpayments;

import org.fenixedu.bennu.core.domain.User;
import org.joda.time.DateTime;

public class ForwardPaymentLogFile extends ForwardPaymentLogFile_Base {

    private ForwardPaymentLogFile() {
        super();
    }

    private ForwardPaymentLogFile(final String fileName, final byte[] content) {
        this();
        this.init(fileName, fileName, content);
    }

    @Override
    public boolean isAccessible(final User user) {
        throw new RuntimeException("not implemented");
    }
    
    public String getContentAsString() {
        if(getContent() != null) {
            return new String(getContent());
        }
        
        return null;
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on

    public static ForwardPaymentLogFile createForRequestBody(final ForwardPaymentLog log, final byte[] content) {
        final ForwardPaymentLogFile logFile = new ForwardPaymentLogFile(
                String.format("requestBody_%s_%s.txt", new DateTime().toString("yyyyMMddHHmmss"), log.getExternalId()), content);
        logFile.setForwardPaymentLogsForRequest(log);
        
        ForwardPaymentLogFileDomainObject.copyAndAssociate(logFile);
        
        return logFile;
    }

    public static ForwardPaymentLogFile createForResponseBody(final ForwardPaymentLog log, final byte[] content) {
        final ForwardPaymentLogFile logFile = new ForwardPaymentLogFile(
                String.format("responseBody_%s_%s.txt", new DateTime().toString("yyyyMMddHHmmss"), log.getExternalId()), content);
        logFile.setForwardPaymentLogsForResponse(log);

        ForwardPaymentLogFileDomainObject.copyAndAssociate(logFile);

        return logFile;
    }

}
