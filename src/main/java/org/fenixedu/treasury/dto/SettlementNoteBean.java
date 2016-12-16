package org.fenixedu.treasury.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import org.fenixedu.bennu.IBean;
import org.fenixedu.bennu.TupleDataSourceBean;
import org.fenixedu.treasury.domain.PaymentMethod;
import org.fenixedu.treasury.domain.VatType;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.CreditEntry;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.ui.document.managepayments.SettlementNoteController;
import org.joda.time.LocalDate;

public class SettlementNoteBean implements IBean, Serializable {

    private static final long serialVersionUID = 1L;

    private boolean reimbursementNote;

    private DebtAccount debtAccount;

    private LocalDate date;

    private DocumentNumberSeries docNumSeries;

    private String originDocumentNumber;

    private List<CreditEntryBean> creditEntries;

    private List<DebitEntryBean> debitEntries;

    private List<InterestEntryBean> interestEntries;

    private List<PaymentEntryBean> paymentEntries;

    private List<TupleDataSourceBean> paymentMethods;

    private List<TupleDataSourceBean> documentNumberSeries;

    private List<String> settlementNoteStateUrls;

    private Stack<Integer> previousStates;
    
    private String finantialTransactionReferenceYear;
    
    private String finantialTransactionReference;

    private boolean advancePayment;

    public SettlementNoteBean() {
        creditEntries = new ArrayList<CreditEntryBean>();
        debitEntries = new ArrayList<DebitEntryBean>();
        interestEntries = new ArrayList<InterestEntryBean>();
        paymentEntries = new ArrayList<PaymentEntryBean>();
        date = new LocalDate();
        previousStates = new Stack<Integer>();
        this.setPaymentMethods(PaymentMethod.findAvailableForPaymentInApplication().collect(Collectors.toList()));

        this.advancePayment = false;
        this.finantialTransactionReferenceYear = String.valueOf((new LocalDate()).getYear());
    }

    public SettlementNoteBean(DebtAccount debtAccount, boolean reimbursementNote) {
        this();
        this.debtAccount = debtAccount;
        this.reimbursementNote = reimbursementNote;
        for (InvoiceEntry invoiceEntry : debtAccount.getPendingInvoiceEntriesSet().stream()
                .filter(ie -> ie.hasPreparingSettlementEntries() == false)
                .sorted((x, y) -> x.getDueDate().compareTo(y.getDueDate())).collect(Collectors.<InvoiceEntry> toList())) {
            if (invoiceEntry instanceof DebitEntry) {
                debitEntries.add(new DebitEntryBean((DebitEntry) invoiceEntry));
            } else {
                creditEntries.add(new CreditEntryBean((CreditEntry) invoiceEntry));
            }
        }

        setDocumentNumberSeries(debtAccount, reimbursementNote);

        settlementNoteStateUrls =
                Arrays.asList(
                        SettlementNoteController.CHOOSE_INVOICE_ENTRIES_URL + debtAccount.getExternalId() + "/"
                                + reimbursementNote,
                        SettlementNoteController.CHOOSE_INVOICE_ENTRIES_URL, SettlementNoteController.CALCULATE_INTEREST_URL,
                        SettlementNoteController.CREATE_DEBIT_NOTE_URL, SettlementNoteController.INSERT_PAYMENT_URL,
                        SettlementNoteController.SUMMARY_URL);
        
        this.advancePayment = false;
        this.finantialTransactionReferenceYear = String.valueOf((new LocalDate()).getYear());
    }

    private void setDocumentNumberSeries(DebtAccount debtAccount, boolean reimbursementNote) {
        FinantialDocumentType finantialDocumentType = (reimbursementNote) ? FinantialDocumentType
                .findForReimbursementNote() : FinantialDocumentType.findForSettlementNote();

        List<DocumentNumberSeries> availableSeries = DocumentNumberSeries
                .find(finantialDocumentType, debtAccount.getFinantialInstitution()).collect(Collectors.toList());

        this.setDocumentNumberSeries(DocumentNumberSeries.applyActiveSelectableAndDefaultSorting(availableSeries.stream())
                .collect(Collectors.toList()));
    }

    public DebtAccount getDebtAccount() {
        return debtAccount;
    }

    public void setDebtAccount(DebtAccount debtAccount) {
        this.debtAccount = debtAccount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<CreditEntryBean> getCreditEntries() {
        return creditEntries;
    }

    public void setCreditEntries(List<CreditEntryBean> creditEntries) {
        this.creditEntries = creditEntries;
    }

    public List<DebitEntryBean> getDebitEntries() {
        return debitEntries;
    }

    public void setDebitEntries(List<DebitEntryBean> debitEntries) {
        this.debitEntries = debitEntries;
    }

    public List<InterestEntryBean> getInterestEntries() {
        return interestEntries;
    }

    public void setInterestEntries(List<InterestEntryBean> interestEntries) {
        this.interestEntries = interestEntries;
    }

    public boolean isAdvancePayment() {
        return this.advancePayment;
    }

    public void setAdvancePayment(final boolean advancePayment) {
        this.advancePayment = advancePayment;
    }

    public LocalDate getDebitNoteDate() {
        LocalDate lowerDate = new LocalDate();
        for (DebitEntryBean debitEntryBean : getDebitEntries()) {
            if (debitEntryBean.isIncluded() && debitEntryBean.getDocumentDueDate().isBefore(lowerDate)) {
                lowerDate = debitEntryBean.getDocumentDueDate();
            }
        }
        for (InterestEntryBean interestEntryBean : getInterestEntries()) {
            if (interestEntryBean.isIncluded() && interestEntryBean.getDocumentDueDate().isBefore(lowerDate)) {
                lowerDate = interestEntryBean.getDocumentDueDate();
            }
        }
        return lowerDate;
    }

    public BigDecimal getDebtAmount() {
        BigDecimal sum = BigDecimal.ZERO;
        for (DebitEntryBean debitEntryBean : getDebitEntries()) {
            if (debitEntryBean.isIncluded()) {
                sum = sum.add(debitEntryBean.getDebtAmount());
            }
        }
        for (InterestEntryBean interestEntryBean : getInterestEntries()) {
            if (interestEntryBean.isIncluded()) {
                sum = sum.add(interestEntryBean.getInterest().getInterestAmount());
            }
        }
        for (CreditEntryBean creditEntryBean : getCreditEntries()) {
            if (creditEntryBean.isIncluded()) {
                sum = sum.subtract(creditEntryBean.getCreditAmount());
            }
        }
        return sum;
    }

    public BigDecimal getDebtAmountWithVat() {
        BigDecimal sum = BigDecimal.ZERO;
        for (DebitEntryBean debitEntryBean : getDebitEntries()) {
            if (debitEntryBean.isIncluded()) {
                sum = sum.add(debitEntryBean.getDebtAmountWithVat());
            }
        }
        for (InterestEntryBean interestEntryBean : getInterestEntries()) {
            if (interestEntryBean.isIncluded()) {
                //Interest doesn't have vat
                sum = sum.add(interestEntryBean.getInterest().getInterestAmount());
            }
        }
        for (CreditEntryBean creditEntryBean : getCreditEntries()) {
            if (creditEntryBean.isIncluded()) {
                sum = sum.subtract(creditEntryBean.getCreditAmountWithVat());
            }
        }
        return sum;
    }

    public BigDecimal getVatAmount() {
        return getDebtAmountWithVat().subtract(getDebtAmount());
    }

    public Map<String, VatAmountBean> getValuesByVat() {
        Map<String, VatAmountBean> sumByVat = new HashMap<String, VatAmountBean>();
        for (VatType vatType : VatType.findAll().collect(Collectors.toList())) {
            sumByVat.put(vatType.getName().getContent(), new VatAmountBean(BigDecimal.ZERO, BigDecimal.ZERO));
        }
        for (DebitEntryBean debitEntryBean : getDebitEntries()) {
            if (debitEntryBean.isIncluded()) {
                String vatType = debitEntryBean.getDebitEntry().getVat().getVatType().getName().getContent();
                sumByVat.get(vatType).addAmount(debitEntryBean.getDebtAmount());
                sumByVat.get(vatType).addAmountWithVat(debitEntryBean.getDebtAmountWithVat());
            }
        }
        for (InterestEntryBean interestEntryBean : getInterestEntries()) {
            if (interestEntryBean.isIncluded()) {
                String vatType = interestEntryBean.getDebitEntry().getVat().getVatType().getName().getContent();
                sumByVat.get(vatType).addAmount(interestEntryBean.getInterest().getInterestAmount());
                sumByVat.get(vatType).addAmountWithVat(interestEntryBean.getInterest().getInterestAmount());
            }
        }
        for (CreditEntryBean creditEntryBean : getCreditEntries()) {
            if (creditEntryBean.isIncluded()) {
                String vatType = creditEntryBean.getCreditEntry().getVat().getVatType().getName().getContent();
                sumByVat.get(vatType).subtractAmount(creditEntryBean.getCreditAmount());
                sumByVat.get(vatType).subtractAmountWithVat(creditEntryBean.getCreditAmountWithVat());
            }
        }
        return sumByVat;
    }

    public List<PaymentEntryBean> getPaymentEntries() {
        return paymentEntries;
    }

    public void setPaymentEntries(List<PaymentEntryBean> paymentEntries) {
        this.paymentEntries = paymentEntries;
    }

    public List<TupleDataSourceBean> getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(List<PaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods.stream().map(paymentMethod -> {
            TupleDataSourceBean tuple = new TupleDataSourceBean();
            tuple.setText(paymentMethod.getName().getContent());
            tuple.setId(paymentMethod.getExternalId());
            return tuple;
        }).collect(Collectors.toList());
    }

    public BigDecimal getPaymentAmount() {
        BigDecimal paymentAmount = BigDecimal.ZERO;
        for (PaymentEntryBean paymentEntryBean : getPaymentEntries()) {
            paymentAmount = paymentAmount.add(paymentEntryBean.getPaymentAmount());
        }
        return paymentAmount;
    }

    public DocumentNumberSeries getDocNumSeries() {
        return docNumSeries;
    }

    public void setDocNumSeries(DocumentNumberSeries docNumSeries) {
        this.docNumSeries = docNumSeries;
    }

    public List<TupleDataSourceBean> getDocumentNumberSeries() {
        return documentNumberSeries;
    }

    public void setDocumentNumberSeries(List<DocumentNumberSeries> documentNumberSeries) {
        this.documentNumberSeries = documentNumberSeries.stream().map(docNumSeries -> {
            TupleDataSourceBean tuple = new TupleDataSourceBean();
            tuple.setText(docNumSeries.getSeries().getCode() + " - " + docNumSeries.getSeries().getName().getContent());
            tuple.setId(docNumSeries.getExternalId());
            return tuple;
        }).collect(Collectors.toList());
    }

    public String getOriginDocumentNumber() {
        return originDocumentNumber;
    }

    public void setOriginDocumentNumber(String originDocumentNumber) {
        this.originDocumentNumber = originDocumentNumber;
    }

    public boolean isReimbursementNote() {
        return reimbursementNote;
    }

    public void setReimbursementNote(boolean reimbursementNote) {
        this.reimbursementNote = reimbursementNote;
    }

    public Stack<Integer> getPreviousStates() {
        return previousStates;
    }

    public void setPreviousStates(Stack<Integer> previousStates) {
        this.previousStates = previousStates;
    }

    public List<String> getSettlementNoteStateUrls() {
        return settlementNoteStateUrls;
    }

    public void setSettlementNoteStateUrls(List<String> settlementNoteStateUrls) {
        this.settlementNoteStateUrls = settlementNoteStateUrls;
    }

    public boolean hasEntriesWithoutDocument() {
        return debitEntries.stream().anyMatch(deb -> deb.isIncluded() && deb.getDebitEntry().getFinantialDocument() == null)
                || interestEntries.stream().anyMatch(deb -> deb.isIncluded());
    }
    
    public String getFinantialTransactionReference() {
        return finantialTransactionReference;
    }
    
    public void setFinantialTransactionReference(String finantialTransactionReference) {
        this.finantialTransactionReference = finantialTransactionReference;
    }
    
    public String getFinantialTransactionReferenceYear() {
        return finantialTransactionReferenceYear;
    }
    
    public void setFinantialTransactionReferenceYear(String finantialTransactionReferenceYear) {
        this.finantialTransactionReferenceYear = finantialTransactionReferenceYear;
    }

    public class DebitEntryBean implements IBean, Serializable {

        private static final long serialVersionUID = 1L;

        private DebitEntry debitEntry;

        private boolean isIncluded;

        private boolean isNotValid;

        private BigDecimal debtAmount;

        public DebitEntryBean() {

        }

        public DebitEntryBean(DebitEntry debitEntry) {
            this.debitEntry = debitEntry;
            this.isIncluded = false;
            this.isNotValid = false;
            this.debtAmount = debitEntry.getOpenAmount();
        }

        public DebitEntry getDebitEntry() {
            return debitEntry;
        }

        public void setDebitEntry(DebitEntry debitEntry) {
            this.debitEntry = debitEntry;
        }

        public String getDocumentNumber() {
            return debitEntry.getFinantialDocument() != null ? debitEntry.getFinantialDocument().getDocumentNumber() : null;
        }

        public LocalDate getDocumentDueDate() {
            return debitEntry.getDueDate();
        }

        public boolean isIncluded() {
            return isIncluded;
        }

        public void setIncluded(boolean isIncluded) {
            this.isIncluded = isIncluded;
        }

        public BigDecimal getDebtAmount() {
            if(debtAmount == null) {
                return null;
            }
            
            return debitEntry.getDebtAccount().getFinantialInstitution().getCurrency().getValueWithScale(debtAmount);
        }

        public BigDecimal getDebtAmountWithVat() {
            if(debtAmount == null) {
                return null;
            }
            
            return debitEntry.getDebtAccount().getFinantialInstitution().getCurrency().getValueWithScale(debtAmount);
        }

        public void setDebtAmount(BigDecimal debtAmount) {
            this.debtAmount = debtAmount;
        }

        public boolean isNotValid() {
            return isNotValid;
        }

        public void setNotValid(boolean notValid) {
            this.isNotValid = notValid;
        }
    }

    public class CreditEntryBean implements IBean, Serializable {

        private static final long serialVersionUID = 1L;

        private CreditEntry creditEntry;

        private boolean isIncluded;

        private boolean isNotValid;

        private BigDecimal creditAmount;

        public CreditEntryBean() {
        }

        public CreditEntryBean(CreditEntry creditEntry) {
            this.creditEntry = creditEntry;
            this.isIncluded = false;
            this.isNotValid = false;
            this.creditAmount = creditEntry.getOpenAmount();
        }

        public CreditEntry getCreditEntry() {
            return creditEntry;
        }

        public void setCreditEntry(CreditEntry creditEntry) {
            this.creditEntry = creditEntry;
        }

        public String getDocumentNumber() {
            return creditEntry.getFinantialDocument() != null ? creditEntry.getFinantialDocument().getDocumentNumber() : null;
        }

        public LocalDate getDocumentDueDate() {
            return creditEntry.getFinantialDocument() != null ? creditEntry.getFinantialDocument()
                    .getDocumentDueDate() : creditEntry.getEntryDateTime().toLocalDate();
        }

        public boolean isIncluded() {
            return isIncluded;
        }

        public void setIncluded(boolean isIncluded) {
            this.isIncluded = isIncluded;
        }

        public BigDecimal getCreditAmount() {
            if(creditAmount == null) {
                return null;
            }
            
            return creditEntry.getDebtAccount().getFinantialInstitution().getCurrency().getValueWithScale(creditAmount);
        }

        public BigDecimal getCreditAmountWithVat() {
            if(creditAmount == null) {
                return null;
            }
            
            return creditEntry.getDebtAccount().getFinantialInstitution().getCurrency().getValueWithScale(creditAmount);
        }

        public void setCreditAmount(BigDecimal creditAmount) {
            this.creditAmount = creditAmount;
        }

        public boolean isNotValid() {
            return isNotValid;
        }

        public void setNotValid(boolean notValid) {
            this.isNotValid = notValid;
        }
    }

    public static class InterestEntryBean implements IBean, Serializable {

        private static final long serialVersionUID = 1L;

        private DebitEntry debitEntry;

        private boolean isIncluded;

        private InterestRateBean interest;

        public InterestEntryBean() {
            this.isIncluded = false;
        }

        public InterestEntryBean(DebitEntry debitEntry, InterestRateBean interest) {
            this();
            this.debitEntry = debitEntry;
            this.interest = interest;
        }

        public InterestRateBean getInterest() {
            return interest;
        }

        public void setInterest(InterestRateBean interest) {
            this.interest = interest;
        }

        public DebitEntry getDebitEntry() {
            return debitEntry;
        }

        public void setDebitEntry(DebitEntry debitEntry) {
            this.debitEntry = debitEntry;
        }

        public LocalDate getDocumentDueDate() {
            return debitEntry.getFinantialDocument() != null ? debitEntry.getFinantialDocument().getDocumentDueDate() : debitEntry
                    .getDueDate();
        }

        public boolean isIncluded() {
            return isIncluded;
        }

        public void setIncluded(boolean isIncluded) {
            this.isIncluded = isIncluded;
        }

    }

    public class PaymentEntryBean implements IBean, Serializable {

        private static final long serialVersionUID = 1L;

        private BigDecimal paymentAmount;

        private PaymentMethod paymentMethod;

        private String paymentMethodId;
        
        public PaymentEntryBean() {
            this.paymentAmount = BigDecimal.ZERO;
        }

        public PaymentEntryBean(BigDecimal paymentAmount, PaymentMethod paymentMethod) {
            this.paymentAmount = paymentAmount;
            this.paymentMethod = paymentMethod;
        }

        public BigDecimal getPaymentAmount() {
            return paymentAmount;
        }

        public void setPaymentAmount(BigDecimal paymentAmount) {
            this.paymentAmount = paymentAmount;
        }

        public PaymentMethod getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getPaymentMethodId() {
            return paymentMethodId;
        }

        public void setPaymentMethodId(String paymentMethodId) {
            this.paymentMethodId = paymentMethodId;
        }
        
    }

    public class VatAmountBean implements IBean, Serializable {

        private static final long serialVersionUID = 1L;

        private BigDecimal amount;

        private BigDecimal amountWithVat;

        public VatAmountBean(BigDecimal amount, BigDecimal amountWithVat) {
            this.amount = amount;
            this.amountWithVat = amountWithVat;
        }

        public VatAmountBean() {
            this.amount = BigDecimal.ZERO;
            this.amountWithVat = BigDecimal.ZERO;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public void addAmount(BigDecimal partialAmount) {
            this.amount = amount.add(partialAmount);
        }

        public void subtractAmount(BigDecimal partialAmount) {
            this.amount = amount.subtract(partialAmount);
        }

        public BigDecimal getAmountWithVat() {
            return amountWithVat;
        }

        public void setAmountWithVat(BigDecimal amountWithVat) {
            this.amountWithVat = amountWithVat;
        }

        public void addAmountWithVat(BigDecimal partialAmountWithVat) {
            this.amountWithVat = amountWithVat.add(partialAmountWithVat);
        }

        public void subtractAmountWithVat(BigDecimal partialAmountWithVat) {
            this.amountWithVat = amountWithVat.subtract(partialAmountWithVat);
        }
    }
}
