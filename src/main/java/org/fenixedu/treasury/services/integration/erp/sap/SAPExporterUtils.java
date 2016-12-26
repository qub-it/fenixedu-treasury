package org.fenixedu.treasury.services.integration.erp.sap;

import static org.fenixedu.treasury.util.Constants.isPositive;
import static org.fenixedu.treasury.util.Constants.divide;

import java.math.BigDecimal;

import org.fenixedu.treasury.domain.Currency;
import org.fenixedu.treasury.domain.document.FinantialDocumentEntry;
import org.fenixedu.treasury.domain.document.Invoice;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.document.SettlementEntry;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;

public class SAPExporterUtils {
    
    public static BigDecimal openAmountAtDate(final InvoiceEntry invoiceEntry, final DateTime when) {
        final Currency currency = invoiceEntry.getDebtAccount().getFinantialInstitution().getCurrency();
        
        if (invoiceEntry.isAnnulled()) {
            return BigDecimal.ZERO;
        }

        final BigDecimal openAmount = invoiceEntry.getAmountWithVat().subtract(payedAmountAtDate(invoiceEntry, when));

        return currency.getValueWithScale(isPositive(openAmount) ? openAmount : BigDecimal.ZERO);
    }
    
    public static BigDecimal payedAmountAtDate(final InvoiceEntry invoiceEntry, final DateTime when) {
        BigDecimal amount = BigDecimal.ZERO;
        for (final SettlementEntry entry : invoiceEntry.getSettlementEntriesSet()) {
            if(entry.getEntryDateTime().isAfter(when)) {
                continue;
            }
            
            if (entry.getFinantialDocument() != null && entry.getFinantialDocument().isClosed()) {
                amount = amount.add(entry.getTotalAmount());
            }
        }
        
        return amount;
    }
    
    public static BigDecimal amountAtDate(final Invoice invoice, final DateTime when) {
        BigDecimal amount = BigDecimal.ZERO;
        for (FinantialDocumentEntry entry : invoice.getFinantialDocumentEntriesSet()) {
            amount = amount.add(openAmountAtDate((InvoiceEntry) entry, when));
        }

        return invoice.getDebtAccount().getFinantialInstitution().getCurrency().getValueWithScale(amount);
    }

    public static BigDecimal netAmountAtDate(final Invoice invoice, final DateTime when) {
        BigDecimal amount = BigDecimal.ZERO;
        for (FinantialDocumentEntry entry : invoice.getFinantialDocumentEntriesSet()) {
            BigDecimal entryAmountAtDate = openAmountAtDate((InvoiceEntry) entry, when);
            entryAmountAtDate = divide(entry.getNetAmount().multiply(entryAmountAtDate), entry.getTotalAmount());
            
            amount = amount.add(entryAmountAtDate);
        }

        return invoice.getDebtAccount().getFinantialInstitution().getCurrency().getValueWithScale(amount);
    }
    
}
