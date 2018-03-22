package org.fenixedu.treasury.domain.bennu.signals;

import org.fenixedu.bennu.core.signals.DomainObjectEvent;
import org.fenixedu.bennu.core.signals.Signal;
import org.fenixedu.treasury.domain.document.SettlementNote;

public class BennuSignalsServices {
    
    public static final String SETTLEMENT_EVENT = "SETTLEMENT_EVENT";
    
    public static void emitSignalForSettlement(final SettlementNote settlementNote) {
        Signal.emit(SETTLEMENT_EVENT, new DomainObjectEvent<SettlementNote>(settlementNote));
    }
    
    public synchronized static void registerSettlementEventHandler(final Object handler) {
        Signal.register(SETTLEMENT_EVENT, handler);
    }
    
    public synchronized static void unregisterSettlementEventHandler(final Object handler) {
        Signal.unregister(SETTLEMENT_EVENT, handler);
    }
    
}
