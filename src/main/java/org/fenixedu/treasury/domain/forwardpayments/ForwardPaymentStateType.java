package org.fenixedu.treasury.domain.forwardpayments;

import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.util.Constants;

public enum ForwardPaymentStateType {
    CREATED,
    REQUESTED,
    AUTHENTICATED,
    AUTHORIZED,
    PAYED,
    REJECTED;
    
    public LocalizedString getLocalizedName() {
        return Constants.bundleI18N(getClass().getSimpleName() + "." + name());
    }
}
