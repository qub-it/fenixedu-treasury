package org.fenixedu.treasury.domain.forwardpayments;

import org.fenixedu.bennu.core.domain.User;

public class ForwardPaymentLogFile extends ForwardPaymentLogFile_Base {
    
    public ForwardPaymentLogFile() {
        super();
    }
    
    public ForwardPaymentLogFile(String fileName, byte[] content) {
        this();
        this.init(fileName, fileName, content);
    }

    @Override
    // TODO: Implement
    public boolean isAccessible(User arg0) {
        throw new RuntimeException("not implemented");
    }
    
    
}
