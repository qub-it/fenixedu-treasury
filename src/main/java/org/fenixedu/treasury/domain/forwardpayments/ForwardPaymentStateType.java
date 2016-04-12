package org.fenixedu.treasury.domain.forwardpayments;

public enum ForwardPaymentStateType {
    CREATED,
    REQUESTED,
    AUTHENTICATED,
    AUTHORIZED,
    PAYED,
    CANCELLED,
    REJECTED;
}
