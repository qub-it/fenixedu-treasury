package org.fenixedu.treasury.domain.forwardpayments;

import org.fenixedu.bennu.core.domain.Bennu;
import org.joda.time.DateTime;

public class ForwardPaymentLog extends ForwardPaymentLog_Base {
    
    private ForwardPaymentLog() {
        super();
        setBennu(Bennu.getInstance());
    }
    
    ForwardPaymentLog(final ForwardPayment forwardPayment, final ForwardPaymentStateType type, final DateTime whenOccured) {
        this();
        setForwardPayment(forwardPayment);
        setType(type);
        setWhenOccured(whenOccured);
    }
    
}
