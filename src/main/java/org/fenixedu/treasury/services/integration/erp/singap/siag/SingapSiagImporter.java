/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: ricardo.pedro@qub-it.com, anil.mamede@qub-it.com
 * 
 *
 * 
 * This file is part of FenixEdu Treasury.
 *
 * FenixEdu Treasury is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Treasury is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Treasury.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.treasury.services.integration.erp.singap.siag;

import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.document.Invoice;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.document.PaymentEntry;
import org.fenixedu.treasury.domain.document.ReimbursementEntry;
import org.fenixedu.treasury.domain.document.SettlementEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.integration.ERPConfiguration;
import org.fenixedu.treasury.domain.integration.ERPImportOperation;
import org.fenixedu.treasury.domain.integration.IntegrationOperationLogBean;
import org.fenixedu.treasury.generated.sources.saft.singap.siag.AuditFile;
import org.fenixedu.treasury.generated.sources.saft.singap.siag.PaymentMethod;
import org.fenixedu.treasury.generated.sources.saft.singap.siag.SAFTPTSettlementType;
import org.fenixedu.treasury.generated.sources.saft.singap.siag.SourceDocuments.Payments.Payment;
import org.fenixedu.treasury.generated.sources.saft.singap.siag.SourceDocuments.Payments.Payment.Line;
import org.fenixedu.treasury.generated.sources.saft.singap.siag.SourceDocuments.WorkingDocuments.WorkDocument;
import org.fenixedu.treasury.services.integration.erp.IERPImporter;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentStatusWS;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentStatusWS.StatusType;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationOutput;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

// ******************************************************************************************************************************
// http://info.portaldasfinancas.gov.pt/NR/rdonlyres/3B4FECDB-2380-45D7-9019-ABCA80A7E99E/0/Comunicacao_Dados_Doc_Transporte.pdf
// http://info.portaldasfinancas.gov.pt/NR/rdonlyres/15D18787-8AA9-4060-90D5-79F168A927A4/0/Portaria_11922009.pdf
// (Documento Original)
// http://dre.pt/pdf1sdip/2012/11/22700/0672406740.pdf (Adenda para os
// Documentos de Transporte)
// Versão 1.0.3
// https://info.portaldasfinancas.gov.pt/NR/rdonlyres/BA9FB096-D482-445D-A5DB-C05B1980F7D7/0/Portaria_274_2013_21_09.pdf
// ******************************************************************************************************************************
public class SingapSiagImporter implements IERPImporter {

    private static JAXBContext jaxbContext = null;
    private static Logger logger = LoggerFactory.getLogger(SingapSiagImporter.class);
    private InputStream fileStream;

    public SingapSiagImporter(InputStream fileStream) {
        this.fileStream = fileStream;
    }

    public AuditFile readAuditFileFromXML() {
        try {

            if (jaxbContext == null) {
                jaxbContext = JAXBContext.newInstance(AuditFile.class);
            }
            javax.xml.bind.Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            StringWriter writer = new StringWriter();

            AuditFile auditFile = (AuditFile) jaxbUnmarshaller.unmarshal(fileStream);
            return auditFile;
        } catch (JAXBException e) {
            return null;
        }
    }

    @Atomic(mode = TxMode.WRITE)
    @Override
    public DocumentsInformationOutput processAuditFile(final ERPImportOperation eRPImportOperation) {
        final IntegrationOperationLogBean logBean = new IntegrationOperationLogBean();

        DocumentsInformationOutput result = new DocumentsInformationOutput();
        result.setDocumentStatus(new ArrayList<DocumentStatusWS>());
        result.setRequestId(eRPImportOperation.getExternalId());
        try {
            AuditFile auditFile = readAuditFileFromXML();
            BigDecimal totalCredit = BigDecimal.ZERO;
            BigDecimal totalDebit = BigDecimal.ZERO;
            BigInteger totalPayments = BigInteger.ZERO;
            for (Payment payment : auditFile.getSourceDocuments().getPayments().getPayment()) {
                DocumentStatusWS docStatus = new DocumentStatusWS();
                logBean.appendIntegrationLog(
                        Constants.bundle("info.ERPImporter.processing.payment", payment.getPaymentRefNo()));
                SettlementNote note = null;
                try {
                    note = processErpPayment(payment, eRPImportOperation, logBean);
                    if (note != null) {
                        if (Strings.isNullOrEmpty(note.getOriginDocumentNumber())) {
                            docStatus.setDocumentNumber(note.getUiDocumentNumber());

                        } else {
                            docStatus.setDocumentNumber(note.getOriginDocumentNumber());
                        }
                        docStatus.setIntegrationStatus(StatusType.SUCCESS);
                        totalPayments = totalPayments.add(BigInteger.ONE);
                        totalCredit = totalCredit.add(note.getTotalCreditAmount());
                        totalDebit = totalDebit.add(note.getTotalDebitAmount());

                        if (note.isPreparing()) {
                            note.closeDocument(false);
                        }
                        eRPImportOperation.addFinantialDocuments(note);
                    } else {
                        throw new TreasuryDomainException("error.ERPImporter.processing.payment", payment.getPaymentRefNo());
                    }
                } catch (Exception ex) {
                    logBean.appendIntegrationLog(ex.getLocalizedMessage());
                    logBean.appendErrorLog(ex.getLocalizedMessage());
                    int count = 0;
                    for (StackTraceElement el : ex.getStackTrace()) {
                        logBean.appendErrorLog(el.toString());
                        if (count++ >= 10) {
                            break;
                        }
                    }
                    logBean.appendIntegrationLog(Constants.bundle(
                            "error.ERPImporter.processing.payment", payment.getPaymentRefNo()));
                    logBean.appendErrorLog(Constants.bundle(
                            "error.ERPImporter.processing.payment", payment.getPaymentRefNo()));
                    docStatus.setDocumentNumber(payment.getPaymentRefNo());
                    docStatus.setErrorDescription("Error: " + ex.getLocalizedMessage());
                    docStatus.setIntegrationStatus(StatusType.ERROR);
                }
                result.getDocumentStatus().add(docStatus);
            }
//            if (totalPayments.compareTo(auditFile.getSourceDocuments().getPayments().getNumberOfEntries()) != 0) {
//                throw new TreasuryDomainException("label.error.integration.erpimporter.invalid.number.of.payments");
//            }
//            if (totalDebit.compareTo(auditFile.getSourceDocuments().getPayments().getTotalDebit()) != 0) {
//                throw new TreasuryDomainException("label.error.integration.erpimporter.invalid.total.debit");
//            }
//            if (totalCredit.compareTo(auditFile.getSourceDocuments().getPayments().getTotalCredit()) != 0) {
//                throw new TreasuryDomainException("label.error.integration.erpimporter.invalid.total.credit");
//            }

            if (eRPImportOperation.getProcessed() == true) {
                //this is a re-process. set as "corrected"
                eRPImportOperation.setCorrected(true);
            }
            eRPImportOperation.setProcessed(true);
            eRPImportOperation.setExecutionDate(new DateTime());
            if (eRPImportOperation.getErrorLog() == null || eRPImportOperation.getErrorLog().isEmpty()) {
                eRPImportOperation.setSuccess(true);
            }

        } catch (Exception ex) {

            logBean.appendErrorLog(ex.getLocalizedMessage());
            int count = 0;
            for (StackTraceElement el : ex.getStackTrace()) {
                logBean.appendErrorLog(el.toString());
                if (count++ >= 10) {
                    break;
                }
            }

            logBean.appendErrorLog(ex.getLocalizedMessage());
            eRPImportOperation.setProcessed(true);
            eRPImportOperation.setCorrected(false);
            eRPImportOperation.setExecutionDate(new DateTime());
            eRPImportOperation.setSuccess(false);
        } finally {
            eRPImportOperation.appendLog(logBean.getErrorLog(), logBean.getIntegrationLog(), logBean.getSoapInboundMessage(),
                    logBean.getSoapOutboundMessage());
        }
        return result;
    }

    @Atomic
    private SettlementNote processErpPayment(Payment payment, ERPImportOperation eRPImportOperation,
            final IntegrationOperationLogBean logBean) {
        boolean newSettlementNoteCreated = false;
        ERPConfiguration integrationConfig = eRPImportOperation.getFinantialInstitution().getErpIntegrationConfiguration();
        DocumentNumberSeries seriesToIntegratePayments = DocumentNumberSeries.find(FinantialDocumentType.findForSettlementNote(),
                integrationConfig.getPaymentsIntegrationSeries());

        //if is a reimbursement, then we must find the correct document series
        if (payment.getSettlementType() != null && payment.getSettlementType().equals(SAFTPTSettlementType.NR)) {
            seriesToIntegratePayments = DocumentNumberSeries.find(FinantialDocumentType.findForReimbursementNote(),
                    integrationConfig.getPaymentsIntegrationSeries());
        }

        if (seriesToIntegratePayments == null || seriesToIntegratePayments.getSeries().getExternSeries() == false) {
            throw new TreasuryDomainException("label.error.integration.erpimporter.invalid.series.to.integrate.payments");
        }

        //Get the ExternalNumber 
        String externalNumber = payment.getPaymentRefNo();
        Customer customer = Customer.findByCode(payment.getCustomerID()).findFirst().orElse(null);
        SettlementNote settlementNote = null;
        DebtAccount customerDebtAccount = null;
        if (customer != null) {
            customerDebtAccount = DebtAccount.findUnique(eRPImportOperation.getFinantialInstitution(), customer).orElse(null);
            if (customerDebtAccount != null) {
                SettlementNote existingSettlementNote = SettlementNote.findByDocumentNumberSeries(seriesToIntegratePayments)
                        .filter(x -> x.getOriginDocumentNumber() != null && x.getOriginDocumentNumber().equals(externalNumber))
                        .findFirst().orElse(null);

                //if we couldn't find from the ExternalNumber, then try to find if SOURCE_ID has value
                if (existingSettlementNote == null && !Strings.isNullOrEmpty(payment.getSourceID())) {
                    existingSettlementNote = SettlementNote.findAll()
                            .filter(x -> x.getUiDocumentNumber().equals(payment.getSourceID())).findFirst().orElse(null);
                    //Update the OriginDocumentNumber
                    if (existingSettlementNote != null) {
                        existingSettlementNote.setOriginDocumentNumber(payment.getPaymentRefNo());
                    }
                }

                if (existingSettlementNote != null) {
                    //Already exists... //Update ?!??!!?
                    settlementNote = existingSettlementNote;
                    if (!settlementNote.getDebtAccount().equals(customerDebtAccount)) {
                        throw new TreasuryDomainException(
                                "label.error.integration.erpimporter.invalid.debtaccount.existing.payment");
                    }

                    if (payment.getDocumentStatus().getPaymentStatus().equals("A")) {
                        if (settlementNote.isAnnulled()) {
                            //Already annulled
                        } else {
                            //The Settlement note must be annulled
                            settlementNote.anullDocument(BundleUtil.getString(Constants.BUNDLE,
                                    "label.info.integration.erpimporter.annulled.by.integration") + " - ["
                                    + new DateTime().toString("YYYY-MM-dd HH:mm:ss") + "]", false);
                        }
                        return settlementNote;
                    } else {
                        //HACK: DONT Accept repeting Documents for (UPDATE)
                        logBean.appendIntegrationLog("label.error.integration.erpimporter.invalid.already.existing.payment.ignored");
                        return settlementNote;
                        //throw new TreasuryDomainException("label.error.integration.erpimporter.invalid.already.existing.payment");
                    }
                } else {
                    if (payment.getDocumentStatus().getPaymentStatus().equals("A")) {
                        //It's a Settlement note that we don't have and want's to be anulled...
                        throw new TreasuryDomainException(
                                "label.error.integration.erpimporter.invalid.payment.received.document.annulled.not.exists");
                    } else {
                        DateTime documentDate = new org.joda.time.DateTime(payment.getTransactionDate().toGregorianCalendar());
                        DateTime paymentDate = documentDate;
                        if (payment.getPaymentMethod().size() > 0) {
                            if (payment.getPaymentMethod().get(0).getPaymentDate() != null) {
                                try {
                                    paymentDate = new org.joda.time.DateTime(
                                            payment.getPaymentMethod().get(0).getPaymentDate().toGregorianCalendar());
                                } catch (Exception ex) {
                                    //ignore error on getting payment date
                                }
                            }
                        }
                        //Create a new SettlementNote
                        settlementNote = SettlementNote.create(customerDebtAccount, seriesToIntegratePayments, documentDate,
                                paymentDate, externalNumber, null);
                        newSettlementNoteCreated = true;
                    }
                }
            } else {
                throw new TreasuryDomainException(
                        "label.error.integration.erpimporter.invalid.debtaccount.to.integrate.payments");
            }
        } else {
            throw new TreasuryDomainException("label.error.integration.erpimporter.invalid.customer.to.integrate.payments");
        }

        try {
            //Continue processing the Payment Document entries (New or Updating??!?!)
            for (Line paymentLine : payment.getLine()) {
                if (paymentLine.getSourceDocumentID().size() != 1) {
                    throw new TreasuryDomainException("label.error.integration.erpimporter.invalid.line.source.in.payment");
                }
                String invoiceReferenceNumber = paymentLine.getSourceDocumentID().get(0).getOriginatingON();
                FinantialDocument referenceDocument = FinantialDocument
                        .findByUiDocumentNumber(eRPImportOperation.getFinantialInstitution(), invoiceReferenceNumber);
                if (referenceDocument == null || ((referenceDocument instanceof Invoice) == false)) {
                    throw new TreasuryDomainException("label.error.integration.erpimporter.invalid.line.source.in.payment");
                }
                Invoice referenceInvoice = (Invoice) referenceDocument;
                if (!referenceInvoice.getDebtAccount().equals(customerDebtAccount)) {
                    throw new TreasuryDomainException("label.error.integration.erpimporter.invalid.line.debtaccount.in.payment");
                }
                InvoiceEntry invoiceEntry =
                        referenceInvoice.getEntryInOrder(paymentLine.getSourceDocumentID().get(0).getLineNumber().intValue());
                if (invoiceEntry == null) {
                    throw new TreasuryDomainException("label.error.integration.erpimporter.invalid.line.source.in.payment");
                }

                BigDecimal paymentAmount = paymentLine.getDebitAmount();

                //if it is a SettlementEntry for a CreditEntry, then we must get the "credit amount" of the SAFT paymentLine
                if (invoiceEntry.isCreditNoteEntry()) {
                    paymentAmount = paymentLine.getCreditAmount();
                }

                if (invoiceEntry.getOpenAmount().compareTo(paymentAmount) < 0) {
                    throw new TreasuryDomainException("label.error.integration.erpimporter.invalid.line.amount.in.payment");
                }

                //Create a new settlement entry for this payment
                XMLGregorianCalendar paymentStatusDate = payment.getDocumentStatus().getPaymentStatusDate();
                DateTime paymentDate = new DateTime(paymentStatusDate.toGregorianCalendar());
                SettlementEntry settlementEntry = SettlementEntry.create(invoiceEntry, settlementNote, paymentAmount,
                        invoiceEntry.getDescription(), paymentDate, false);

                //Update the PaymentDate
                if (paymentDate.isBefore(settlementNote.getPaymentDate())) {
                    settlementNote.setPaymentDate(paymentDate);
                }
            }

            if (payment.getSettlementType() != null && payment.getSettlementType().equals(SAFTPTSettlementType.NR)) {
                //Continue processing the Reimbursment Methods (New or Updating??!?!)
                for (PaymentMethod paymentMethod : payment.getPaymentMethod()) {
                    ReimbursementEntry reimbursmentEntry = ReimbursementEntry.create(settlementNote,
                            convertFromSAFTPaymentMethod(paymentMethod.getPaymentMechanism()), paymentMethod.getPaymentAmount(), null);
                }
            } else {
                //Continue processing the Payment Methods (New or Updating??!?!)
                for (PaymentMethod paymentMethod : payment.getPaymentMethod()) {
                    PaymentEntry paymentEntry =
                            PaymentEntry.create(convertFromSAFTPaymentMethod(paymentMethod.getPaymentMechanism()), settlementNote,
                                    paymentMethod.getPaymentAmount(), null, Maps.newHashMap());
                }
            }

            //Process possible Advance Payment Credit
            //HACK: DISABLED FOR NOW!!!! FENIXEDU ONLY ACCEPTS CREATION OF PAYMENTS. WE CANNOT INTEGRATE ADVANCE PAYMENTS DUE TO DocumentNumber 
            //error when trying to back-reference the credit note in payments.
            //
            if (payment.getAdvancedPaymentCredit() != null) {
                throw new TreasuryDomainException("label.error.integration.erpimporter.advanced.payment.credit.cannot.integrate");
//            settlementNote.createAdvancedPaymentCreditNote(payment.getAdvancedPaymentCredit().getCreditAmount(), payment
//                    .getAdvancedPaymentCredit().getDescription(), payment.getAdvancedPaymentCredit().getOriginatingON());
            }
        } catch (Exception ex) {
            //Catch and rethrow exception 
            //if a new SettlementNote created, then delete 
            if (newSettlementNoteCreated && settlementNote != null && settlementNote.isDeletable()) {
                settlementNote.delete(true);
            }
            throw ex;
        }
        return settlementNote;
    }

    //convert the saft payment method to fenixEdu payment entry
    private org.fenixedu.treasury.domain.PaymentMethod convertFromSAFTPaymentMethod(String paymentMechanism) {
        org.fenixedu.treasury.domain.PaymentMethod paymentMethod =
                org.fenixedu.treasury.domain.PaymentMethod.findByCode(paymentMechanism);
        if (paymentMethod == null) {
            throw new TreasuryDomainException("error.ERPImporter.unkown.payment.method", paymentMechanism);
        }
        return paymentMethod;
    }

    public Set<String> getRelatedDocumentsNumber() {
        Set<String> result = new HashSet<String>();

        AuditFile file = readAuditFileFromXML();

        for (WorkDocument w : file.getSourceDocuments().getWorkingDocuments().getWorkDocument()) {
            result.add(w.getDocumentNumber());
        }
        for (org.fenixedu.treasury.generated.sources.saft.singap.siag.SourceDocuments.SalesInvoices.Invoice i : file
                .getSourceDocuments().getSalesInvoices().getInvoice()) {
            result.add(i.getInvoiceNo());
        }
        for (Payment p : file.getSourceDocuments().getPayments().getPayment()) {
            result.add(p.getPaymentRefNo());
        }
        return result;
    }

    @Override
    public String readTaxRegistrationNumberFromAuditFile() {
        final AuditFile auditFile = readAuditFileFromXML();
        return auditFile.getHeader().getTaxRegistrationNumber() + "";
    }

}
