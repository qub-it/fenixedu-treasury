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
package org.fenixedu.treasury.services.integration.erp.sap;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.treasury.domain.AdhocCustomer;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.Vat;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.AdvancedPaymentCreditNote;
import org.fenixedu.treasury.domain.document.CreditEntry;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.document.FinantialDocumentEntry;
import org.fenixedu.treasury.domain.document.Invoice;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.document.PaymentEntry;
import org.fenixedu.treasury.domain.document.ReimbursementEntry;
import org.fenixedu.treasury.domain.document.SettlementEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.integration.ERPConfiguration;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.domain.integration.IntegrationOperationLogBean;
import org.fenixedu.treasury.domain.integration.OperationFile;
import org.fenixedu.treasury.generated.sources.saft.sap.AddressStructure;
import org.fenixedu.treasury.generated.sources.saft.sap.AddressStructurePT;
import org.fenixedu.treasury.generated.sources.saft.sap.AuditFile;
import org.fenixedu.treasury.generated.sources.saft.sap.Header;
import org.fenixedu.treasury.generated.sources.saft.sap.MovementTax;
import org.fenixedu.treasury.generated.sources.saft.sap.OrderReferences;
import org.fenixedu.treasury.generated.sources.saft.sap.PaymentMethod;
import org.fenixedu.treasury.generated.sources.saft.sap.ReimbursementStatusType;
import org.fenixedu.treasury.generated.sources.saft.sap.SAFTPTMovementTaxType;
import org.fenixedu.treasury.generated.sources.saft.sap.SAFTPTPaymentType;
import org.fenixedu.treasury.generated.sources.saft.sap.SAFTPTSettlementType;
import org.fenixedu.treasury.generated.sources.saft.sap.SAFTPTSourceBilling;
import org.fenixedu.treasury.generated.sources.saft.sap.SAFTPTSourcePayment;
import org.fenixedu.treasury.generated.sources.saft.sap.SourceDocuments;
import org.fenixedu.treasury.generated.sources.saft.sap.SourceDocuments.Payments;
import org.fenixedu.treasury.generated.sources.saft.sap.SourceDocuments.Payments.Payment;
import org.fenixedu.treasury.generated.sources.saft.sap.SourceDocuments.Payments.Payment.AdvancedPaymentCredit;
import org.fenixedu.treasury.generated.sources.saft.sap.SourceDocuments.Payments.Payment.Line.SourceDocumentID;
import org.fenixedu.treasury.generated.sources.saft.sap.SourceDocuments.Payments.Payment.ReimbursementProcess;
import org.fenixedu.treasury.generated.sources.saft.sap.SourceDocuments.WorkingDocuments.WorkDocument;
import org.fenixedu.treasury.generated.sources.saft.sap.SourceDocuments.WorkingDocuments.WorkDocument.AdvancedPayment;
import org.fenixedu.treasury.generated.sources.saft.sap.SourceDocuments.WorkingDocuments.WorkDocument.Line.Metadata;
import org.fenixedu.treasury.generated.sources.saft.sap.Tax;
import org.fenixedu.treasury.generated.sources.saft.sap.TaxTableEntry;
import org.fenixedu.treasury.services.integration.erp.IERPExporter;
import org.fenixedu.treasury.services.integration.erp.IERPExternalService;
import org.fenixedu.treasury.services.integration.erp.SaftConfig;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentStatusWS;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationInput;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationOutput;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import fr.opensagres.xdocreport.core.io.internal.ByteArrayOutputStream;
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
public class SAPExporter implements IERPExporter {

    private static final String MORADA_DESCONHECIDO = "Desconhecido";
    private static final int MAX_ADDRESS_DETAIL = 100;
    private static final int MAX_CITY = 50;
    private static final int MAX_ZIPCODE = 20;
    private static final int MAX_REGION = 50;
    private static final int MAX_STREE_NAME = 90;
    
    private static final String SAFT_PT_ENCODING = "UTF-8";
    private static Logger logger = LoggerFactory.getLogger(SAPExporter.class);
    public final static String ERP_HEADER_VERSION_1_00_00 = "1.0.3";

    private String generateERPFile(FinantialInstitution institution, DateTime fromDate, DateTime toDate,
            List<? extends FinantialDocument> allDocuments, Boolean generateAllCustomers, Boolean generateAllProducts,
            java.util.function.UnaryOperator<AuditFile> preProcessFunctionBeforeSerialize) {

        checkForUnsetDocumentSeriesNumberInDocumentsToExport(allDocuments);

        // Build SAFT-AuditFile
        AuditFile auditFile = new AuditFile();
        // ThreadInformation information = 
        // SaftThreadRegister.retrieveCurrentThreadInformation();

        // Build SAFT-HEADER (Chapter 1 in AuditFile)
        Header header = this.createSAFTHeader(fromDate, toDate, institution, ERP_HEADER_VERSION_1_00_00);
        // SetHeader
        auditFile.setHeader(header);

        // Build Master-Files
        org.fenixedu.treasury.generated.sources.saft.sap.AuditFile.MasterFiles masterFiles =
                new org.fenixedu.treasury.generated.sources.saft.sap.AuditFile.MasterFiles();

        // SetMasterFiles
        auditFile.setMasterFiles(masterFiles);

        // Build SAFT-MovementOfGoods (Customer and Products are built inside)
        // ProductsTable (Chapter 2.4 in AuditFile)
        List<org.fenixedu.treasury.generated.sources.saft.sap.Product> productList = masterFiles.getProduct();
        Map<String, org.fenixedu.treasury.generated.sources.saft.sap.Product> productMap =
                new HashMap<String, org.fenixedu.treasury.generated.sources.saft.sap.Product>();
        Set<String> productCodes = new HashSet<String>();

        // ClientsTable (Chapter 2.2 in AuditFile)
        List<org.fenixedu.treasury.generated.sources.saft.sap.Customer> customerList = masterFiles.getCustomer();
        Map<String, org.fenixedu.treasury.generated.sources.saft.sap.Customer> customerMap =
                new HashMap<String, org.fenixedu.treasury.generated.sources.saft.sap.Customer>();

        // Readd All  Clients if needed
        if (generateAllCustomers) {
            logger.info("Reading all Customers in Institution " + institution.getCode());

            Set<Customer> allCustomers = new HashSet<Customer>();
            for (DebtAccount debt : institution.getDebtAccountsSet()) {
                allCustomers.add(debt.getCustomer());
            }

            // Update the Total Objects Count
            // information.setTotalCounter(allCustomers.size() +
            // allProducts.size() + allDocuments.size() * 10);

            int i = 0;
            for (Customer customer : allCustomers) {
                org.fenixedu.treasury.generated.sources.saft.sap.Customer saftCustomer =
                        this.convertCustomerToSAFTCustomer(customer);
                // information.setCurrentCounter(information.getCurrentCounter()
                // + 1);
                customerMap.put(saftCustomer.getCustomerID(), saftCustomer);
                i++;
                if (i % 100 == 0) {
                    logger.info(
                            "Processing " + i + "/" + allCustomers.size() + " Customers in Institution " + institution.getCode());
                }
            }
        }
        // Readd All Products if needed
        if (generateAllProducts) {

            logger.info("Reading all Customers in Institution " + institution.getCode());
            Set<Product> allProducts = institution.getAvailableProductsSet();
            int i = 0;
            for (Product product : allProducts) {
                if (!productCodes.contains(product.getCode())) {
                    org.fenixedu.treasury.generated.sources.saft.sap.Product saftProduct =
                            this.convertProductToSAFTProduct(product);
                    productCodes.add(product.getCode());
                    productMap.put(saftProduct.getProductCode(), saftProduct);
                }

                i++;
                if (i % 100 == 0) {
                    logger.info(
                            "Processing " + i + "/" + allProducts.size() + " Products in Institution " + institution.getCode());
                }
            }
        }

        // TaxTable (Chapter 2.5 in AuditFile)
        org.fenixedu.treasury.generated.sources.saft.sap.TaxTable taxTable =
                new org.fenixedu.treasury.generated.sources.saft.sap.TaxTable();
        masterFiles.setTaxTable(taxTable);

        for (Vat vat : institution.getVatsSet()) {
            if (vat.isActiveNow()) {
                taxTable.getTaxTableEntry().add(this.convertVATtoTaxTableEntry(vat, institution));
            }
        }

        // Set MovementOfGoods in SourceDocuments(AuditFile)
        org.fenixedu.treasury.generated.sources.saft.sap.SourceDocuments sourceDocuments =
                new org.fenixedu.treasury.generated.sources.saft.sap.SourceDocuments();
        auditFile.setSourceDocuments(sourceDocuments);

        SourceDocuments.SalesInvoices invoices = new SourceDocuments.SalesInvoices();
        SourceDocuments.WorkingDocuments workingDocuments = new SourceDocuments.WorkingDocuments();
        Payments paymentsDocuments = new Payments();

        BigInteger numberOfPaymentsDocuments = BigInteger.ZERO;
        BigDecimal totalDebitOfPaymentsDocuments = BigDecimal.ZERO;
        BigDecimal totalCreditOfPaymentsDocuments = BigDecimal.ZERO;

        BigInteger numberOfWorkingDocuments = BigInteger.ZERO;
        BigDecimal totalDebitOfWorkingDocuments = BigDecimal.ZERO;
        BigDecimal totalCreditOfWorkingDocuments = BigDecimal.ZERO;

        invoices.setNumberOfEntries(BigInteger.ZERO);
        invoices.setTotalCredit(BigDecimal.ZERO);
        invoices.setTotalDebit(BigDecimal.ZERO);

        for (FinantialDocument document : allDocuments) {
            if ((document.isCreditNote() || document.isDebitNote()) && (document.isClosed() || document.isAnnulled())) {
                try {
                    WorkDocument workDocument = convertToSAFTWorkDocument((Invoice) document, customerMap, productMap);
                    workingDocuments.getWorkDocument().add(workDocument);

                    // AcumulateValues
                    numberOfWorkingDocuments = numberOfWorkingDocuments.add(BigInteger.ONE);
                    if (!document.isAnnulled()) {
                        if (document.isDebitNote()) {
                            totalDebitOfWorkingDocuments =
                                    totalDebitOfWorkingDocuments.add(workDocument.getDocumentTotals().getNetTotal());
                        } else if (document.isCreditNote()) {
                            totalCreditOfWorkingDocuments =
                                    totalCreditOfWorkingDocuments.add(workDocument.getDocumentTotals().getNetTotal());
                        }
                    }

                } catch (Exception ex) {
                    logger.error("Error processing document " + document.getUiDocumentNumber() + ": " + ex.getLocalizedMessage());
                    throw ex;
                }
            } else {
                if (!document.isSettlementNote()) {
                    logger.info("Ignoring document " + document.getUiDocumentNumber() + " because is not closed yet.");
                }
            }
        }
        // Update Totals of Workingdocuments
        workingDocuments.setNumberOfEntries(numberOfWorkingDocuments);
        workingDocuments.setTotalCredit(totalCreditOfWorkingDocuments.setScale(2, RoundingMode.HALF_EVEN));
        workingDocuments.setTotalDebit(totalDebitOfWorkingDocuments.setScale(2, RoundingMode.HALF_EVEN));

        sourceDocuments.setWorkingDocuments(workingDocuments);

        //PROCESSING PAYMENTS TABLE

        paymentsDocuments.setNumberOfEntries(BigInteger.ZERO);
        paymentsDocuments.setTotalCredit(BigDecimal.ZERO);
        paymentsDocuments.setTotalDebit(BigDecimal.ZERO);
        for (FinantialDocument document : allDocuments) {
            checkForUnsetDocumentSeriesNumberInDocumentsToExport(Lists.newArrayList(document));

            if (document.isSettlementNote() && (document.isClosed() || document.isAnnulled())) {
                try {
                    Payment paymentDocument = convertToSAFTPaymentDocument((SettlementNote) document, customerMap, productMap);
                    paymentsDocuments.getPayment().add(paymentDocument);

                    // AcumulateValues
                    numberOfPaymentsDocuments = numberOfPaymentsDocuments.add(BigInteger.ONE);
                    if (!document.isAnnulled()) {
                        totalCreditOfPaymentsDocuments =
                                totalCreditOfPaymentsDocuments.add(((SettlementNote) document).getTotalCreditAmount());
                        totalDebitOfPaymentsDocuments =
                                totalDebitOfPaymentsDocuments.add(((SettlementNote) document).getTotalDebitAmount());
                    }
                } catch (Exception ex) {
                    logger.error("Error processing document " + document.getUiDocumentNumber() + ": " + ex.getLocalizedMessage());
                    throw ex;
                }
            } else {
                if (document.isSettlementNote()) {
                    logger.info("Ignoring document " + document.getUiDocumentNumber() + " because is not closed yet.");
                }
            }

        }

        // Update Totals of Payment Documents
        paymentsDocuments.setNumberOfEntries(numberOfPaymentsDocuments);
        paymentsDocuments.setTotalCredit(totalCreditOfPaymentsDocuments.setScale(2, RoundingMode.HALF_EVEN));
        paymentsDocuments.setTotalDebit(totalDebitOfPaymentsDocuments.setScale(2, RoundingMode.HALF_EVEN));
        sourceDocuments.setPayments(paymentsDocuments);

        // Update the Customer Table in SAFT
        for (org.fenixedu.treasury.generated.sources.saft.sap.Customer customer : customerMap.values()) {
            customerList.add(customer);
        }

        // Update the Product Table in SAFT
        for (org.fenixedu.treasury.generated.sources.saft.sap.Product product : productMap.values()) {
            productList.add(product);
        }

        if (preProcessFunctionBeforeSerialize != null) {
            auditFile = preProcessFunctionBeforeSerialize.apply(auditFile);
        }
        String xml = exportAuditFileToXML(auditFile);

        logger.info("SAFT File export concluded with success.");
        return xml;
    }

    private Payment convertToSAFTPaymentDocument(SettlementNote document,
            Map<String, org.fenixedu.treasury.generated.sources.saft.sap.Customer> baseCustomers,
            Map<String, org.fenixedu.treasury.generated.sources.saft.sap.Product> productMap) {
        Payment payment = new Payment();

        // Find the Customer in BaseCustomers
        org.fenixedu.treasury.generated.sources.saft.sap.Customer customer = null;

        if (baseCustomers.containsKey(document.getDebtAccount().getCustomer().getCode())) {
            customer = baseCustomers.get(document.getDebtAccount().getCustomer().getCode());
        } else {
            // If not found, create a new one and add it to baseCustomers
            customer = convertCustomerToSAFTCustomer(document.getDebtAccount().getCustomer());
            baseCustomers.put(customer.getCustomerID(), customer);
        }

        // MovementDate
        DatatypeFactory dataTypeFactory;
        try {
            dataTypeFactory = DatatypeFactory.newInstance();
            DateTime documentDate = document.getDocumentDate();

            // SystemEntryDate
            payment.setSystemEntryDate(convertToXMLDateTime(dataTypeFactory, documentDate));

            /* ANIL: 2015/10/20 converted from dateTime to Date */
            payment.setTransactionDate(convertToXMLDate(dataTypeFactory, documentDate));
            
            /* SAP: 2016/09/19 This element is required */
            payment.setPaymentType(SAFTPTPaymentType.RG);

            // DocumentNumber
            payment.setPaymentRefNo(document.getUiDocumentNumber());

            // Finantial Transaction Reference
            payment.setFinantialTransactionReference(!Strings.isNullOrEmpty(document.getFinantialTransactionReference()) ? document.getFinantialTransactionReference() : "");

            //OriginDocumentNumber
            payment.setSourceID(!Strings.isNullOrEmpty(document.getVersioningCreator()) ? document.getVersioningCreator() : " ");

            // CustomerID
            payment.setCustomerID(document.getDebtAccount().getCustomer().getCode());

            // DocumentStatus
            /*
             * Deve ser preenchido com: ?N? ? Normal; Texto 1 ?T? ? Por conta de
             * terceiros; ?A? ? Documento anulado.
             */
            SourceDocuments.Payments.Payment.DocumentStatus status = new SourceDocuments.Payments.Payment.DocumentStatus();
            if (document.isAnnulled()) {
                status.setPaymentStatus("A");
            } else {
                status.setPaymentStatus("N");
            }
            if (document.getVersioningUpdateDate() != null) {
                status.setPaymentStatusDate(convertToXMLDateTime(dataTypeFactory, document.getVersioningUpdateDate().getDate()));
                // Utilizador responsável pelo estado atual do docu-mento.
                status.setSourceID(document.getVersioningUpdatedBy().getUsername());
            } else {
                status.setPaymentStatusDate(payment.getSystemEntryDate());
                // Utilizador responsável pelo estado atual do docu-mento.
                status.setSourceID(" ");
            }
            status.setReason(document.getDocumentObservations());
            // Deve ser preenchido com:
            // 'P' - Documento produzido na aplicacao;
            if (Boolean.TRUE.equals(document.getDocumentNumberSeries().getSeries().getExternSeries())
                    || Boolean.TRUE.equals(document.getDocumentNumberSeries().getSeries().getLegacy())) {
                status.setSourcePayment(SAFTPTSourcePayment.I);
            } else {
                status.setSourcePayment(SAFTPTSourcePayment.P);
            }

            payment.setDocumentStatus(status);

            //Check if is Rehimbursement/Payment
            if (Constants.isPositive(document.getTotalPayedAmount())) {
                //PaymentMethods
                for (PaymentEntry paymentEntry : document.getPaymentEntriesSet()) {
                    PaymentMethod method = new PaymentMethod();
                    method.setPaymentAmount(paymentEntry.getPayedAmount().setScale(2, RoundingMode.HALF_EVEN));

                    /* ANIL: 2015/10/20 converted from dateTime to Date */
                    method.setPaymentDate(convertToXMLDate(dataTypeFactory, document.getPaymentDate()));

                    method.setPaymentMechanism(convertToSAFTPaymentMechanism(paymentEntry.getPaymentMethod()));
                    method.setPaymentMethodReference(!Strings.isNullOrEmpty(paymentEntry.getPaymentMethodId()) ? paymentEntry.getPaymentMethodId() : "");
                    payment.getPaymentMethod().add(method);
                }
                payment.setSettlementType(SAFTPTSettlementType.NL);
            } else if (Constants.isPositive(document.getTotalReimbursementAmount())) {
                //Reimbursments
                for (ReimbursementEntry reimbursmentEntry : document.getReimbursementEntriesSet()) {
                    PaymentMethod method = new PaymentMethod();
                    method.setPaymentAmount(reimbursmentEntry.getReimbursedAmount().setScale(2, RoundingMode.HALF_EVEN));

                    /* ANIL: 2015/10/20 converted from dateTime to Date */
                    method.setPaymentDate(convertToXMLDate(dataTypeFactory, document.getPaymentDate()));

                    method.setPaymentMechanism(convertToSAFTPaymentMechanism(reimbursmentEntry.getPaymentMethod()));
                    method.setPaymentMethodReference(!Strings.isNullOrEmpty(reimbursmentEntry.getReimbursementMethodId()) ? reimbursmentEntry.getReimbursementMethodId() : "");

                    payment.getPaymentMethod().add(method);
                    payment.setSettlementType(SAFTPTSettlementType.NR);
                }

                // Fill reimbursement process status
                ReimbursementProcess reimbursementProcess = new ReimbursementProcess();
                reimbursementProcess.setStatusDate(convertToXMLDate(dataTypeFactory, document.getDocumentDate()));
                reimbursementProcess.setStatus(ReimbursementStatusType.PENDING);

                payment.setReimbursementProcess(reimbursementProcess);
            } else {
                PaymentMethod voidMethod = new PaymentMethod();
                voidMethod.setPaymentAmount(BigDecimal.ZERO);

                /* ANIL: 2015/10/20 converted from dateTime to Date */
                voidMethod.setPaymentDate(convertToXMLDate(dataTypeFactory, document.getPaymentDate()));

                voidMethod.setPaymentMechanism("OU");
                voidMethod.setPaymentMethodReference("");

                payment.getPaymentMethod().add(voidMethod);
                payment.setSettlementType(SAFTPTSettlementType.NN);
            }


            // DocumentTotals
            SourceDocuments.Payments.Payment.DocumentTotals docTotals = new SourceDocuments.Payments.Payment.DocumentTotals();

            //Lines
            BigInteger i = BigInteger.ONE;
            for (SettlementEntry settlementEntry : document.getSettlemetEntriesSet()) {
                SourceDocuments.Payments.Payment.Line line = new SourceDocuments.Payments.Payment.Line();
                line.setLineNumber(i);
                //SourceDocument
                SourceDocumentID sourceDocument = new SourceDocumentID();
                sourceDocument.setLineNumber(BigInteger.valueOf(settlementEntry.getInvoiceEntry().getEntryOrder()));
                sourceDocument.setOriginatingON(settlementEntry.getInvoiceEntry().getFinantialDocument().getUiDocumentNumber());

                /* ANIL: 2015/10/20 converted from dateTime to Date */
                sourceDocument.setInvoiceDate(convertToXMLDate(dataTypeFactory,
                        settlementEntry.getInvoiceEntry().getFinantialDocument().getDocumentDate()));

                sourceDocument.setDescription(settlementEntry.getDescription());
                line.getSourceDocumentID().add(sourceDocument);
                //SettlementAmount
                line.setSettlementAmount(BigDecimal.ZERO);
                if (settlementEntry.getInvoiceEntry().isDebitNoteEntry()) {
                    line.setDebitAmount(settlementEntry.getTotalAmount());
                } else if (settlementEntry.getInvoiceEntry().isCreditNoteEntry()) {
                    line.setCreditAmount(settlementEntry.getTotalAmount());
                }
                payment.getLine().add(line);
                i = i.add(BigInteger.ONE);
            }

            //If there is an Advanced Payment Credit Note for this Settlement
            if (document.getAdvancedPaymentCreditNote() != null) {
                payment.setAdvancedPaymentCredit(new AdvancedPaymentCredit());
                payment.getAdvancedPaymentCredit().setCreditAmount(
                        document.getAdvancedPaymentCreditNote().getTotalAmount().setScale(2, RoundingMode.HALF_EVEN));
                payment.getAdvancedPaymentCredit()
                        .setOriginatingON(document.getAdvancedPaymentCreditNote().getUiDocumentNumber());
                payment.getAdvancedPaymentCredit()
                        .setDescription(document.getAdvancedPaymentCreditNote().getDocumentObservations());
            }

            docTotals.setGrossTotal(document.getTotalAmount().setScale(2, RoundingMode.HALF_EVEN));
            docTotals.setNetTotal(document.getTotalAmount().setScale(2, RoundingMode.HALF_EVEN));
            docTotals.setTaxPayable(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
            payment.setDocumentTotals(docTotals);

            // Period
            /*
             * Per?odo contabil?stico (Period) . . . . . . . . . . Deve ser
             * indicado o n?mero do m?s do per?odo de tributa??o, de ?1? a ?12?,
             * contado desde a data do in?cio. Pode ainda ser preenchido com
             * ?13?, ?14?, ?15? ou ?16? para movimentos efectuados no ?ltimo m?s
             * do per?odo de tributa??o, relacionados com o apuramento do
             * resultado. Ex.: movimentos de apuramentos de invent?rios,
             * deprecia??es, ajustamentos ou apuramentos de resultados.
             */
            payment.setPeriod(document.getDocumentDate().getMonthOfYear());

        } catch (DatatypeConfigurationException e) {

            e.printStackTrace();
        }

        return payment;
    }

    private XMLGregorianCalendar convertToXMLDateTime(DatatypeFactory dataTypeFactory, DateTime documentDate) {
        return dataTypeFactory.newXMLGregorianCalendar(documentDate.getYear(), documentDate.getMonthOfYear(),
                documentDate.getDayOfMonth(), documentDate.getHourOfDay(), documentDate.getMinuteOfHour(),
                documentDate.getSecondOfMinute(), 0, DatatypeConstants.FIELD_UNDEFINED);
    }

    private XMLGregorianCalendar convertToXMLDate(DatatypeFactory dataTypeFactory, DateTime date) {
        return dataTypeFactory.newXMLGregorianCalendarDate(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(),
                DatatypeConstants.FIELD_UNDEFINED);
    }

    private String convertToSAFTPaymentMechanism(org.fenixedu.treasury.domain.PaymentMethod paymentMethod) {
        return paymentMethod.getCode();
    }

    private WorkDocument convertToSAFTWorkDocument(Invoice document,
            Map<String, org.fenixedu.treasury.generated.sources.saft.sap.Customer> baseCustomers,
            Map<String, org.fenixedu.treasury.generated.sources.saft.sap.Product> baseProducts) {
        WorkDocument workDocument = new WorkDocument();

        // Find the Customer in BaseCustomers
        org.fenixedu.treasury.generated.sources.saft.sap.Customer customer = null;

        if (baseCustomers.containsKey(document.getDebtAccount().getCustomer().getCode())) {
            customer = baseCustomers.get(document.getDebtAccount().getCustomer().getCode());
        } else {
            // If not found, create a new one and add it to baseCustomers
            customer = convertCustomerToSAFTCustomer(document.getDebtAccount().getCustomer());
            baseCustomers.put(customer.getCustomerID(), customer);
        }

        if (document instanceof AdvancedPaymentCreditNote
                && ((AdvancedPaymentCreditNote) document).getAdvancedPaymentSettlementNote() != null) {
            AdvancedPayment advancedPayment = new AdvancedPayment();
            advancedPayment.setDescription("");
            advancedPayment.setOriginatingON(
                    ((AdvancedPaymentCreditNote) document).getAdvancedPaymentSettlementNote().getUiDocumentNumber());
            workDocument.setAdvancedPayment(advancedPayment);
        }

        //check the PayorDebtAccount
        if (document.getPayorDebtAccount() != null) {
            if (baseCustomers.containsKey(document.getPayorDebtAccount().getCustomer().getCode())) {
                //do nothing
            } else {
                // If not found, create a new one and add it to baseCustomers
                org.fenixedu.treasury.generated.sources.saft.sap.Customer payorCustomer =
                        convertCustomerToSAFTCustomer(document.getPayorDebtAccount().getCustomer());
                baseCustomers.put(payorCustomer.getCustomerID(), payorCustomer);
            }
        }

        // MovementDate
        DatatypeFactory dataTypeFactory;
        try {
            dataTypeFactory = DatatypeFactory.newInstance();
            DateTime documentDate = document.getDocumentDate();

            /* Anil: 14/06/2016: Fill with 0's the Hash element */
            workDocument.setDueDate(convertToXMLDate(dataTypeFactory, document.getDocumentDueDate().toDateTimeAtStartOfDay()));

            /* Anil: 14/06/2016: Fill with 0's the Hash element */
            workDocument.setHash(Strings.repeat("0", 172));

            // SystemEntryDate
            workDocument.setSystemEntryDate(convertToXMLDateTime(dataTypeFactory, documentDate));

            /* ANIL: 2015/10/20 converted from dateTime to Date */
            workDocument.setWorkDate(convertToXMLDate(dataTypeFactory, documentDate));

            // DocumentNumber
            workDocument.setDocumentNumber(document.getUiDocumentNumber());

            // CustomerID
            workDocument.setCustomerID(document.getDebtAccount().getCustomer().getCode());

            //PayorID
            if (document.getPayorDebtAccount() != null) {
                workDocument.setPayorCustomerID(document.getPayorDebtAccount().getCustomer().getCode());
            }

            // DocumentStatus
            /*
             * Deve ser preenchido com: ?N? ? Normal; Texto 1 ?T? ? Por conta de
             * terceiros; ?A? ? Documento anulado.
             */
            SourceDocuments.WorkingDocuments.WorkDocument.DocumentStatus status =
                    new SourceDocuments.WorkingDocuments.WorkDocument.DocumentStatus();
            if (document.isAnnulled()) {
                status.setWorkStatus("A");
            } else {
                status.setWorkStatus("N");
            }

            if (document.getVersioningUpdateDate() != null) {
                status.setWorkStatusDate(convertToXMLDateTime(dataTypeFactory, document.getVersioningUpdateDate().getDate()));
                // Utilizador responsável pelo estado atual do docu-mento.
                status.setSourceID(document.getVersioningUpdatedBy().getUsername());
            } else {
                status.setWorkStatusDate(workDocument.getSystemEntryDate());
                // Utilizador responsável pelo estado atual do docu-mento.
                status.setSourceID(" ");
            }
            // status.setReason("");
            // Deve ser preenchido com:
            // 'P' - Documento produzido na aplicacao;
            if (Boolean.TRUE.equals(document.getDocumentNumberSeries().getSeries().getExternSeries())
                    || Boolean.TRUE.equals(document.getDocumentNumberSeries().getSeries().getLegacy())) {
                status.setSourceBilling(SAFTPTSourceBilling.I);
            } else {
                status.setSourceBilling(SAFTPTSourceBilling.P);
            }

            workDocument.setDocumentStatus(status);

            // DocumentTotals
            SourceDocuments.WorkingDocuments.WorkDocument.DocumentTotals docTotals =
                    new SourceDocuments.WorkingDocuments.WorkDocument.DocumentTotals();
            docTotals.setGrossTotal(document.getTotalAmount().setScale(2, RoundingMode.HALF_EVEN));
            docTotals.setNetTotal(document.getTotalNetAmount().setScale(2, RoundingMode.HALF_EVEN));
            docTotals.setTaxPayable(
                    document.getTotalAmount().subtract(document.getTotalNetAmount()).setScale(2, RoundingMode.HALF_EVEN));
            workDocument.setDocumentTotals(docTotals);

            // WorkType
            /*
             * Deve ser preenchido com: Texto 2 "DC" — Documentos emitidos que
             * sejam suscetiveis de apresentacao ao cliente para conferencia de
             * entrega de mercadorias ou da prestacao de servicos. "FC" — Fatura
             * de consignacao nos termos do artigo 38º do codigo do IVA.
             */
            workDocument.setWorkType("DC");

            // Period
            /*
             * Per?odo contabil?stico (Period) . . . . . . . . . . Deve ser
             * indicado o n?mero do m?s do per?odo de tributa??o, de ?1? a ?12?,
             * contado desde a data do in?cio. Pode ainda ser preenchido com
             * ?13?, ?14?, ?15? ou ?16? para movimentos efectuados no ?ltimo m?s
             * do per?odo de tributa??o, relacionados com o apuramento do
             * resultado. Ex.: movimentos de apuramentos de invent?rios,
             * deprecia??es, ajustamentos ou apuramentos de resultados.
             */
            workDocument.setPeriod(document.getDocumentDate().getMonthOfYear());

            // SourceID
            workDocument.setSourceID(!Strings.isNullOrEmpty(document.getVersioningCreator()) ? document.getVersioningCreator() : "");

        } catch (DatatypeConfigurationException e) {

            e.printStackTrace();
        }

        List<org.fenixedu.treasury.generated.sources.saft.sap.SourceDocuments.WorkingDocuments.WorkDocument.Line> productLines =
                workDocument.getLine();

        // Process individual
        BigInteger i = BigInteger.ONE;
        for (FinantialDocumentEntry docLine : document.getFinantialDocumentEntriesSet()) {
            InvoiceEntry orderNoteLine = (InvoiceEntry) docLine;
            org.fenixedu.treasury.generated.sources.saft.sap.SourceDocuments.WorkingDocuments.WorkDocument.Line line =
                    convertToSAFTWorkDocumentLine(orderNoteLine, baseProducts);

            // LineNumber
            line.setLineNumber(BigInteger.valueOf(orderNoteLine.getEntryOrder().intValue()));

            // Add to productLines
            i = i.add(BigInteger.ONE);
            productLines.add(line);
        }

        return workDocument;
    }

    private org.fenixedu.treasury.generated.sources.saft.sap.SourceDocuments.WorkingDocuments.WorkDocument.Line convertToSAFTWorkDocumentLine(
            InvoiceEntry entry, Map<String, org.fenixedu.treasury.generated.sources.saft.sap.Product> baseProducts) {
        org.fenixedu.treasury.generated.sources.saft.sap.Product currentProduct = null;

        Product product = entry.getProduct();

        if (product.getCode() != null && baseProducts.containsKey(product.getCode())) {
            currentProduct = baseProducts.get(product.getCode());
        } else {
            currentProduct = convertProductToSAFTProduct(product);
            baseProducts.put(currentProduct.getProductCode(), currentProduct);
        }
        XMLGregorianCalendar documentDateCalendar = null;
        try {
            DatatypeFactory dataTypeFactory = DatatypeFactory.newInstance();
            DateTime documentDate = entry.getFinantialDocument().getDocumentDate();

            /* ANIL: 2015/10/20 converted from dateTime to Date */
            documentDateCalendar = convertToXMLDate(dataTypeFactory, documentDate);

        } catch (DatatypeConfigurationException e) {

            e.printStackTrace();
        }

        org.fenixedu.treasury.generated.sources.saft.sap.SourceDocuments.WorkingDocuments.WorkDocument.Line line =
                new org.fenixedu.treasury.generated.sources.saft.sap.SourceDocuments.WorkingDocuments.WorkDocument.Line();

        if (entry.isCreditNoteEntry()) {
            line.setCreditAmount(entry.getAmount().setScale(2, RoundingMode.HALF_EVEN));
        } else if (entry.isDebitNoteEntry()) {
            line.setDebitAmount(entry.getAmount().setScale(2, RoundingMode.HALF_EVEN));
        }

        // Description
        line.setDescription(entry.getDescription());
        List<OrderReferences> orderReferences = line.getOrderReferences();

        //Add the references on the document creditEntries <-> debitEntries
        if (entry.isCreditNoteEntry()) {
            CreditEntry creditEntry = (CreditEntry) entry;
            if (creditEntry.getDebitEntry() != null) {
                //Metadata
                Metadata metadata = new Metadata();
                metadata.setDescription(creditEntry.getDebitEntry().getERPIntegrationMetadata());
                line.setMetadata(metadata);

                OrderReferences reference = new OrderReferences();
                reference.setOriginatingON(creditEntry.getDebitEntry().getFinantialDocument().getUiDocumentNumber());
                reference.setOrderDate(documentDateCalendar);
                reference.setLineNumber(BigInteger.ONE);
                
                orderReferences.add(reference);
            }

        } else if (entry.isDebitNoteEntry()) {
            DebitEntry debitEntry = (DebitEntry) entry;

            Metadata metadata = new Metadata();
            metadata.setDescription(debitEntry.getERPIntegrationMetadata());
            line.setMetadata(metadata);
        }

        // ProductCode
        line.setProductCode(currentProduct.getProductCode());

        // ProductDescription
        line.setProductDescription(currentProduct.getProductDescription());

        // Quantity
        line.setQuantity(entry.getQuantity());

        // SettlementAmount
        line.setSettlementAmount(BigDecimal.ZERO);

        // Tax
        line.setTax(getSAFTWorkingDocumentsTax(product, entry));

        line.setTaxPointDate(documentDateCalendar);

        // TaxExemptionReason
        /*
         * Motivo da isen??o de imposto (TaxExemptionReason). Campo de
         * preenchimento obrigat?rio, quando os campos percentagem da taxa de
         * imposto (TaxPercentage) ou montante do imposto (TaxAmount) s?o iguais
         * a zero. Deve ser referido o preceito legal aplic?vel. . . . . . . . .
         * . Texto 60
         */
        if (Constants.isEqual(line.getTax().getTaxPercentage(), BigDecimal.ZERO)
                || (line.getTax().getTaxAmount() != null && Constants.isEqual(line.getTax().getTaxAmount(), BigDecimal.ZERO))) {
            if (product.getVatExemptionReason() != null) {
                line.setTaxExemptionReason(
                        product.getVatExemptionReason().getCode() + "-" + product.getVatExemptionReason().getName().getContent());
            } else {
                // HACK : DEFAULT
                line.setTaxExemptionReason(Constants.bundle("warning.ERPExporter.vat.exemption.unknown"));
            }
        }

        // UnitOfMeasure
        line.setUnitOfMeasure(product.getUnitOfMeasure().getContent());
        // UnitPrice
        line.setUnitPrice(entry.getAmount().setScale(2, RoundingMode.HALF_EVEN));

        return line;
    }

    private Tax getSAFTWorkingDocumentsTax(Product product, final InvoiceEntry entry) {
        Vat vat = entry.getVat();

        Tax tax = new Tax();

        // VatType vat = product.getVatType();
        // Tax-TaxCode
        tax.setTaxCode(vat.getVatType().getCode());

        tax.setTaxCountryRegion("PT");

        // Tax-TaxPercentage
        tax.setTaxPercentage(vat.getTaxRate());

        // Tax-TaxType
        tax.setTaxType("IVA");

        // TODO: Fill with vat amount
        //tax.setTaxAmount(entry.getVatAmount());

        return tax;
    }

    private TaxTableEntry convertVATtoTaxTableEntry(Vat vat, FinantialInstitution finantialInstitution) {
        TaxTableEntry entry = new TaxTableEntry();
        entry.setTaxType("IVA");
        entry.setTaxCode(vat.getVatType().getName().getContent());
        if (finantialInstitution.getFiscalNumber() != null) {
            entry.setTaxCountryRegion(finantialInstitution.getFiscalCountryRegion().getFiscalCode());
            entry.setDescription(finantialInstitution.getFiscalCountryRegion().getName().getContent() + "-"
                    + vat.getVatType().getName().getContent());
        } else {
            entry.setTaxCountryRegion("PT");
            entry.setDescription("");
        }
        entry.setTaxCode(vat.getVatType().getCode());
        entry.setTaxPercentage(vat.getTaxRate());

        if (Strings.isNullOrEmpty(entry.getDescription())) {
            entry.setDescription(entry.getTaxCode() + "_" + entry.getTaxCountryRegion());
        }
        return entry;
    }

    private Header createSAFTHeader(DateTime startDate, DateTime endDate, FinantialInstitution finantialInstitution,
            String auditVersion) {

        Header header = new Header();
        DatatypeFactory dataTypeFactory;
        try {

            dataTypeFactory = DatatypeFactory.newInstance();

            // AuditFileVersion
            header.setAuditFileVersion(auditVersion);
            header.setIdProcesso(finantialInstitution.getErpIntegrationConfiguration().getErpIdProcess());

            // BusinessName - Nome da Empresa
            header.setBusinessName(finantialInstitution.getCompanyName());
            header.setCompanyName(finantialInstitution.getName());

            // CompanyAddress
            AddressStructurePT companyAddress = null;
            //TODOJN Locale por resolver
            companyAddress = convertAddressToAddressPT(finantialInstitution.getAddress(), finantialInstitution.getZipCode(),
                    finantialInstitution.getMunicipality() != null ? finantialInstitution.getMunicipality()
                            .getLocalizedName(new Locale("pt")) : "---",
                    finantialInstitution.getAddress());
            header.setCompanyAddress(companyAddress);

            // CompanyID
            /*
             * Obtem -se pela concatena??o da conservat?ria do registo comercial
             * com o n?mero do registo comercial, separados pelo car?cter
             * espa?o. Nos casos em que n?o existe o registo comercial, deve ser
             * indicado o NIF.
             */
            header.setCompanyID(finantialInstitution.getComercialRegistrationCode());

            // CurrencyCode
            /*
             * 1.11 * C?digo de moeda (CurrencyCode) . . . . . . . Preencher com
             * ?EUR?
             */
            header.setCurrencyCode(finantialInstitution.getCurrency().getCode());

            // DateCreated
            DateTime now = new DateTime();

            /* ANIL: 2015/10/20 converted from dateTime to Date */
            header.setDateCreated(convertToXMLDate(dataTypeFactory, now));

            // Email
            // header.setEmail(StringUtils.EMPTY);

            // EndDate

            /* ANIL: 2015/10/20 converted from dateTime to Date */
            header.setEndDate(convertToXMLDate(dataTypeFactory, endDate));

            // Fax
            // header.setFax(StringUtils.EMPTY);

            // FiscalYear
            /*
             * Utilizar as regras do c?digo do IRC, no caso de per?odos
             * contabil?sticos n?o coincidentes com o ano civil. (Ex: per?odo de
             * tributa??o de 01 -10 -2008 a 30 -09 -2009 corresponde FiscalYear
             * 2008). Inteiro 4
             */
            header.setFiscalYear(endDate.getYear());

            // Ir obter a data do ?ltimo
            // documento(por causa de submeter em janeiro, documentos de
            // dezembro)

            // HeaderComment
            // header.setHeaderComment(org.apache.commons.lang.StringUtils.EMPTY);

            // ProductCompanyTaxID
            // Preencher com o NIF da entidade produtora do software
            header.setProductCompanyTaxID(SaftConfig.PRODUCT_COMPANY_TAX_ID());

            // ProductID
            /*
             * 1.16 * Nome do produto (ProductID). . . . . . . . . . . Nome do
             * produto que gera o SAF -T (PT) . . . . . . . . . . . Deve ser
             * indicado o nome comercial do software e o da empresa produtora no
             * formato ?Nome produto/nome empresa?.
             */
            header.setProductID(SaftConfig.PRODUCT_ID());

            // Product Version
            header.setProductVersion(SaftConfig.PRODUCT_VERSION());

            // SoftwareCertificateNumber
            /* Changed to 0 instead of -1 decribed in SaftConfig.SOFTWARE_CERTIFICATE_NUMBER() */
            header.setSoftwareCertificateNumber(BigInteger.valueOf(0));

            // StartDate
            header.setStartDate(dataTypeFactory.newXMLGregorianCalendarDate(startDate.getYear(), startDate.getMonthOfYear(),
                    startDate.getDayOfMonth(), DatatypeConstants.FIELD_UNDEFINED));

            // TaxAccountingBasis
            /*
             * Deve ser preenchido com: contabilidade; facturao; ?I? ? dados
             * integrados de factura??o e contabilidade; ?S? ? autofactura??o;
             * ?P? ? dados parciais de factura??o
             */
            header.setTaxAccountingBasis("P");

            // TaxEntity
            /*
             * Identifica??o do estabelecimento (TaxEntity) No caso do ficheiro
             * de factura??o dever? ser especificado a que estabelecimento diz
             * respeito o ficheiro produzido, se aplic?vel, caso contr?rio,
             * dever? ser preenchido com a especifica??o ?Global?. No caso do
             * ficheiro de contabilidade ou integrado, este campo dever? ser
             * preenchido com a especifica??o ?Sede?. Texto 20
             */
            header.setTaxEntity("Global");

            // TaxRegistrationNumber
            /*
             * N?mero de identifica??o fiscal da empresa
             * (TaxRegistrationNumber). Preencher com o NIF portugu?s sem
             * espa?os e sem qualquer prefixo do pa?s. Inteiro 9
             */
            try {
                header.setTaxRegistrationNumber(Integer.parseInt(finantialInstitution.getFiscalNumber()));
            } catch (Exception ex) {
                throw new RuntimeException("Invalid Fiscal Number.");
            }

            // header.setTelephone(finantialInstitution.get);

            // header.setWebsite(finantialInstitution.getEmailContact());

            return header;
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private AddressStructurePT convertAddressToAddressPT(final String addressDetail, final String zipCode, final String zipCodeRegion,
            final String street) {
        final AddressStructurePT companyAddress = new AddressStructurePT();

        companyAddress.setCountry("PT");
        
        if(addressDetail != null && addressDetail.length() > MAX_ADDRESS_DETAIL) {
            throw new TreasuryDomainException("error.SAPExporter.addressDetail.more.than.allowed", String.valueOf(MAX_ADDRESS_DETAIL));
        }
        companyAddress.setAddressDetail(!Strings.isNullOrEmpty(addressDetail) ? addressDetail : MORADA_DESCONHECIDO);

        if(zipCodeRegion != null && zipCodeRegion.length() > MAX_CITY) {
            throw new TreasuryDomainException("error.SAPExporter.city.more.than.allowed", String.valueOf(MAX_CITY));
        }
        companyAddress.setCity(!Strings.isNullOrEmpty(zipCodeRegion) ? zipCodeRegion : MORADA_DESCONHECIDO);
        
        if(zipCode != null && zipCode.length() > MAX_ZIPCODE) {
            throw new TreasuryDomainException("error.SAPExporter.zipCode.more.than.allowed", String.valueOf(MAX_ZIPCODE));
        }
        
        companyAddress.setPostalCode(!Strings.isNullOrEmpty(zipCode) ? zipCode : MORADA_DESCONHECIDO);

        if(zipCodeRegion != null && zipCodeRegion.length() > MAX_REGION) {
            throw new TreasuryDomainException("error.SAPExporter.region.more.than.allowed", String.valueOf(MAX_REGION));
        }
        companyAddress.setRegion(!Strings.isNullOrEmpty(zipCodeRegion) ? zipCodeRegion : MORADA_DESCONHECIDO);

        companyAddress.setStreetName(Splitter.fixedLength(MAX_STREE_NAME).splitToList(street).get(0));

        return companyAddress;
    }

    private String exportAuditFileToXML(AuditFile auditFile) {
        try {
            final String cleanXMLAnotations = "xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"";
            final String cleanXMLAnotations2 = "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
            final String cleanXMLAnotations3 = "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xsi:type=\"xs:string\"";
            final String cleanDateTimeMiliseconds = ".000<";
            final String cleanStandaloneAnnotation = "standalone=\"yes\"";

            final JAXBContext jaxbContext = JAXBContext.newInstance(AuditFile.class);
            Marshaller marshaller = jaxbContext.createMarshaller();

            ByteArrayOutputStream writer = new ByteArrayOutputStream();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, SAFT_PT_ENCODING);
            marshaller.marshal(auditFile, writer);

            Charset charset = Charset.forName(SAFT_PT_ENCODING);

            String xml = new String(writer.toByteArray(), SAFT_PT_ENCODING);
            xml = xml.replace(cleanXMLAnotations, "");
            xml = xml.replace(cleanXMLAnotations2, "");
            xml = xml.replace(cleanXMLAnotations3, "");
            xml = xml.replace(cleanDateTimeMiliseconds, "<");
            xml = xml.replace(cleanStandaloneAnnotation, "");

            try {
                MessageDigest md = MessageDigest.getInstance("SHA1");
                md.update(("SALTING WITH QUB:" + xml).getBytes(SAFT_PT_ENCODING));
                byte[] output = md.digest();
                String digestAscii = bytesToHex(output);
                xml = xml + "<!-- QUB-IT (remove this line,add the qubSALT, save with UTF-8 encode): " + digestAscii
                        + " -->\n";
            } catch (Exception ex) {

            }
            return xml;
        } catch (JAXBException e) {
            return org.apache.commons.lang.StringUtils.EMPTY;
        } catch (UnsupportedEncodingException jex) {
            return org.apache.commons.lang.StringUtils.EMPTY;
        }
    }

    public static String bytesToHex(byte[] b) {
        char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        StringBuffer buf = new StringBuffer();
        for (byte element : b) {
            buf.append(hexDigit[element >> 4 & 0x0f]);
            buf.append(hexDigit[element & 0x0f]);
        }
        return buf.toString();
    }

    private org.fenixedu.treasury.generated.sources.saft.sap.Customer convertCustomerToSAFTCustomer(Customer customer) {
        org.fenixedu.treasury.generated.sources.saft.sap.Customer c =
                new org.fenixedu.treasury.generated.sources.saft.sap.Customer();

        c.setDisable("");

        // AccountID
        /*
         * Deve ser indicada a respectiva conta corrente do cliente no plano de
         * contas da contabilidade, caso esteja definida. Caso contr?rio dever?
         * ser preenchido com a designa??o ?Desconhecido?.
         */

        if (customer instanceof AdhocCustomer) {
            c.setAccountID("ADHOC");
        } else {
            c.setAccountID("STUDENT");
        }

        // BillingAddress
        // List<PhysicalAddress> addresses = customer
        // .getPartyContacts(PhysicalAddress.class);
        // if (addresses.size() > 0) {
        // c.setBillingAddress(convertToSAFTAddressStructure(addresses.get(0)));
        // } else {
        // PhysicalAddress addr = new PhysicalAddress();
        
        // Ensure address is filled to avoid errors in invoices
        
        if(Strings.isNullOrEmpty(customer.getCountryCode())) {
            throw new TreasuryDomainException("error.SapExporter.address.countryCode.not.filled");
        } else if(Strings.isNullOrEmpty(customer.getAddress())) {
            throw new TreasuryDomainException("error.SapExporter.address.address.not.filled");
        } else if(Strings.isNullOrEmpty(customer.getZipCode())) {
            throw new TreasuryDomainException("error.SapExporter.address.zipCode.not.filled");
        } else if(Strings.isNullOrEmpty(customer.getDistrictSubdivision())) {
            throw new TreasuryDomainException("error.SapExporter.address.districtSubdivision.not.filled");
        }
        
        c.setBillingAddress(convertAddressToSAFTAddress(customer.getCountryCode(), customer.getAddress(), customer.getZipCode(),
                customer.getDistrictSubdivision(), customer.getAddress()));
                // }

        // CompanyName
        c.setCompanyName(customer.getName());

        // Contact
        c.setContact(Splitter.fixedLength(50).splitToList(customer.getName()).get(0));

        // CustomerID
        c.setCustomerID(customer.getCode());

        c.setCustomerBusinessID(customer.getBusinessIdentification());

        // CustomerTaxID
        if (Strings.isNullOrEmpty(customer.getFiscalNumber())) {
            c.setCustomerTaxID(Customer.DEFAULT_FISCAL_NUMBER);
        } else {
            c.setCustomerTaxID(customer.getFiscalNumber());
        }
        // Email
        // c.setEmail("");

        // Fax
        // c.setFax("");

        // SelfBillingIndicator
        /*
         * Indicador da exist?ncia de acordo de autofactura??o entre o cliente e
         * o fornecedor. Deve ser preenchido com ?1? se houver acordo e com ?0?
         * (zero) no caso contr?rio.
         */
        c.setSelfBillingIndicator(0);

        // Telephone
        // c.setTelephone("");

        c.setFiscalCountry(customer.getFiscalCountry());
        c.setNationality(customer.getNationalityCountryCode());

        return c;
    }

    private AddressStructure convertAddressToSAFTAddress(String country, String addressDetail, String zipCode,
            String zipCodeRegion, String street) {
        final AddressStructure companyAddress = new AddressStructure();

        companyAddress.setCountry(!Strings.isNullOrEmpty(country) ? country : MORADA_DESCONHECIDO);
        
        if(addressDetail != null && addressDetail.length() > MAX_ADDRESS_DETAIL) {
            throw new TreasuryDomainException("error.SAPExporter.addressDetail.more.than.allowed", String.valueOf(MAX_ADDRESS_DETAIL));
        }
        
        companyAddress.setAddressDetail(!Strings.isNullOrEmpty(addressDetail) ? addressDetail : MORADA_DESCONHECIDO);

        if(zipCodeRegion != null && zipCodeRegion.length() > MAX_CITY) {
            throw new TreasuryDomainException("error.SAPExporter.city.more.than.allowed", String.valueOf(MAX_CITY));
        }
        companyAddress.setCity(!Strings.isNullOrEmpty(zipCodeRegion) ? zipCodeRegion : MORADA_DESCONHECIDO);


        if(zipCode != null && zipCode.length() > MAX_ZIPCODE) {
            throw new TreasuryDomainException("error.SAPExporter.zipCode.more.than.allowed", String.valueOf(MAX_ZIPCODE));
        }
        
        companyAddress.setPostalCode(!Strings.isNullOrEmpty(zipCode) ? zipCode : MORADA_DESCONHECIDO);

        if(zipCodeRegion != null && zipCodeRegion.length() > MAX_REGION) {
            throw new TreasuryDomainException("error.SAPExporter.region.more.than.allowed", String.valueOf(MAX_REGION));
        }
        companyAddress.setRegion(!Strings.isNullOrEmpty(zipCodeRegion) ? zipCodeRegion : MORADA_DESCONHECIDO);

        companyAddress.setStreetName(Splitter.fixedLength(MAX_STREE_NAME).splitToList(street).get(0));
        return companyAddress;
    }

    private org.fenixedu.treasury.generated.sources.saft.sap.Product convertProductToSAFTProduct(Product product) {
        org.fenixedu.treasury.generated.sources.saft.sap.Product p =
                new org.fenixedu.treasury.generated.sources.saft.sap.Product();

        // ProductCode
        p.setProductCode(product.getCode());

        // ProductDescription
        p.setProductDescription(product.getName().getContent());

        // ProductGroup
        if (product.getProductGroup() != null) {
            p.setProductGroup(product.getProductGroup().getName().getContent());
        }

        // ProductNumberCode
        p.setProductNumberCode(p.getProductCode());

        // ProductType
        /*
         * Deve ser preenchido com: ?P? ? produtos; ?S? ? servi?os; ?O? ? outros
         * (ex: portes debitados); ?I? ? impostos, taxas e encargos parafiscais
         * (excepto IVA e IS que dever?o ser reflectidos na tabela de impostos ?
         * TaxTable). Texto 1
         */
        p.setProductType("S");

        return p;
    }

    private MovementTax getSAFTMovementTax(Product product, Vat vat) {
        MovementTax tax = new MovementTax();

        tax.setTaxCode(vat.getVatType().getCode());

        tax.setTaxCountryRegion("PT");

        // Tax-TaxPercentage
        tax.setTaxPercentage(vat.getTaxRate());

        // Tax-TaxType
        tax.setTaxType(SAFTPTMovementTaxType.IVA);

        return tax;
    }

    private Tax getSAFTInvoiceTax(Product product, Vat vat) {
        Tax tax = new Tax();
        // Tax-TaxCode

        tax.setTaxCode(vat.getVatType().getCode());

        tax.setTaxCountryRegion("PT");

        // Tax-TaxPercentage
        tax.setTaxPercentage(vat.getTaxRate());

        // Tax-TaxType
        tax.setTaxType("IVA");

        return tax;
    }

    public ERPExportOperation exportFullToIntegration(FinantialInstitution institution, DateTime fromDate, DateTime toDate,
            String username, Boolean includeMovements) {

        final IntegrationOperationLogBean logBean = new IntegrationOperationLogBean();

        final ERPExportOperation operation = createSaftExportOperation(null, institution, new DateTime());
        try {
            SAPExporter saftExporter = new SAPExporter();
            List<FinantialDocument> documents =
                    new ArrayList<FinantialDocument>(institution.getExportableDocuments(fromDate, toDate));
            documents = processCreditNoteSettlementsInclusion(documents);

            logger.info("Collecting " + documents.size() + " documents to export to institution " + institution.getCode());
            UnaryOperator<AuditFile> auditFilePreProcess = getAuditFilePreProcessOperator(institution);
            String xml = saftExporter.generateERPFile(institution, fromDate, toDate, documents, true, true, auditFilePreProcess);

            OperationFile operationFile = writeContentToExportOperation(xml, operation);

            boolean success = sendDocumentsInformationToIntegration(institution, operationFile, logBean);

            operation.getFinantialDocumentsSet().addAll(documents);
            operation.setSuccess(success);

        } catch (Throwable t) {
            writeError(operation, logBean, t);
        } finally {
            operation.appendLog(logBean.getErrorLog(), logBean.getIntegrationLog(), logBean.getSoapInboundMessage(),
                    logBean.getSoapOutboundMessage());
        }

        return operation;
    }

    @Override
    public void requestPendingDocumentStatus(final FinantialInstitution institution) {
        ERPConfiguration erpIntegrationConfiguration = institution.getErpIntegrationConfiguration();
        if (erpIntegrationConfiguration == null) {
            throw new TreasuryDomainException("error.ERPExporter.invalid.erp.configuration");
        }

        if (erpIntegrationConfiguration.getActive() == false) {
            return;
        }
        IERPExternalService service = erpIntegrationConfiguration.getERPExternalServiceImplementation();

        List<String> documentNumbers = institution.getFinantialDocumentsPendingForExportationSet().stream()
                .map(doc -> doc.getUiDocumentNumber()).collect(Collectors.toList());
        List<DocumentStatusWS> integrationStatusFor =
                service.getIntegrationStatusFor(institution.getFiscalNumber(), documentNumbers);
        for (DocumentStatusWS documentStatus : integrationStatusFor) {
            if (documentStatus.isIntegratedWithSuccess()) {
                String message =
                        Constants.bundle("info.ERPExporter.sucess.integrating.document", documentStatus.getDocumentNumber());
                FinantialDocument document = institution.getFinantialDocumentsPendingForExportationSet().stream()
                        .filter(x -> x.getUiDocumentNumber().equals(documentStatus.getDocumentNumber())).findFirst().orElse(null);
                if (document != null) {
                    document.clearDocumentToExport(message);
                }
            }
        }
    }

    private boolean sendDocumentsInformationToIntegration(final FinantialInstitution institution,
            final OperationFile operationFile, final IntegrationOperationLogBean logBean) throws MalformedURLException {
        boolean success = true;
        ERPConfiguration erpIntegrationConfiguration = institution.getErpIntegrationConfiguration();
        if (erpIntegrationConfiguration == null) {
            throw new TreasuryDomainException("error.ERPExporter.invalid.erp.configuration");
        }

        if (erpIntegrationConfiguration.getActive() == false) {
            logBean.appendErrorLog(Constants.bundle("info.ERPExporter.configuration.inactive"));
            return false;
        }

        IERPExternalService service = erpIntegrationConfiguration.getERPExternalServiceImplementation();
        logBean.appendIntegrationLog(Constants.bundle("info.ERPExporter.sending.inforation"));

        DocumentsInformationInput input = new DocumentsInformationInput();
        if (operationFile.getSize() <= erpIntegrationConfiguration.getMaxSizeBytesToExportOnline()) {
            input.setData(operationFile.getContent());
            DocumentsInformationOutput sendInfoOnlineResult = service.sendInfoOnline(input);
            logBean.appendIntegrationLog(
                    Constants.bundle("info.ERPExporter.sucess.sending.inforation.online", sendInfoOnlineResult.getRequestId()));

            //if we have result in online situation, then check the information of integration STATUS
            for (DocumentStatusWS status : sendInfoOnlineResult.getDocumentStatus()) {
                if (status.isIntegratedWithSuccess()) {

                    FinantialDocument document =
                            FinantialDocument.findByUiDocumentNumber(institution, status.getDocumentNumber());
                    if (document != null) {
                        final String message =
                                Constants.bundle("info.ERPExporter.sucess.integrating.document", document.getUiDocumentNumber());
                        logBean.appendIntegrationLog(message);
                        document.clearDocumentToExport(message);
                    } else {
                        success = false;
                        logBean.appendIntegrationLog(Constants.bundle("info.ERPExporter.error.integrating.document",
                                status.getDocumentNumber(), status.getErrorDescription()));
                        logBean.appendErrorLog(Constants.bundle("info.ERPExporter.error.integrating.document",
                                status.getDocumentNumber(), status.getErrorDescription()));
                    }
                } else {
                    success = false;
                    logBean.appendIntegrationLog(Constants.bundle("info.ERPExporter.error.integrating.document",
                            status.getDocumentNumber(), status.getErrorDescription()));
                    logBean.appendErrorLog(Constants.bundle("info.ERPExporter.error.integrating.document",
                            status.getDocumentNumber(), status.getErrorDescription()));

                }
            }

            logBean.defineSoapInboundMessage(sendInfoOnlineResult.getSoapInboundMessage());
            logBean.defineSoapOutboundMessage(sendInfoOnlineResult.getSoapOutboundMessage());

        } else {
            throw new TreasuryDomainException(
                    "error.ERPExporter.sendDocumentsInformationToIntegration.maxSizeBytesToExportOnline.exceeded");
        }

        return success;
    }

    private void writeError(final ERPExportOperation operation, final IntegrationOperationLogBean logBean,
            final Throwable t) {
        final StringWriter out = new StringWriter();
        final PrintWriter writer = new PrintWriter(out);
        t.printStackTrace(writer);

        logBean.appendErrorLog(out.toString());

        operation.setProcessed(true);
    }

    // SERVICE
    @Atomic(mode = TxMode.WRITE)
    private ERPExportOperation createSaftExportOperation(byte[] data, FinantialInstitution institution, DateTime when) {
        String filename = institution.getFiscalNumber() + "_" + when.toString() + ".xml";
        ERPExportOperation operation = ERPExportOperation.create(data, filename, institution, when, false, false, false);
        return operation;
    }

    // SERVICE
    @Atomic
    private OperationFile writeContentToExportOperation(String content, ERPExportOperation operation) {
        byte[] bytes = null;
        try {
            bytes = content.getBytes(SAFT_PT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String fileName = operation.getFinantialInstitution().getFiscalNumber() + "_"
                + operation.getExecutionDate().toString("ddMMyyyy_hhmm") + ".xml";
        OperationFile binaryStream = new OperationFile(fileName, bytes);
        if (operation.getFile() != null) {
            operation.getFile().delete();
        }
        operation.setFile(binaryStream);

        return binaryStream;
    }

    // SERVICE
    @Override
    public String exportFinantialDocumentToXML(final FinantialInstitution finantialInstitution,
            final List<FinantialDocument> documents) {
        UnaryOperator<AuditFile> auditFilePreProcess = getAuditFilePreProcessOperator(finantialInstitution);

        return exportFinantialDocumentToXML(finantialInstitution, documents, auditFilePreProcess);
    }

    private String exportFinantialDocumentToXML(final FinantialInstitution finantialInstitution,
            final List<FinantialDocument> documents, final UnaryOperator<AuditFile> preProcessFunctionBeforeSerialize) {

        checkForUnsetDocumentSeriesNumberInDocumentsToExport(documents);

        documents = processCreditNoteSettlementsInclusion(documents);
        
        SAPExporter saftExporter = new SAPExporter();
        DateTime beginDate =
                documents.stream().min((x, y) -> x.getDocumentDate().compareTo(y.getDocumentDate())).get().getDocumentDate();
        DateTime endDate =
                documents.stream().max((x, y) -> x.getDocumentDate().compareTo(y.getDocumentDate())).get().getDocumentDate();
        return saftExporter.generateERPFile(finantialInstitution, beginDate, endDate, documents, false, false,
                preProcessFunctionBeforeSerialize);
    }

    
    private List<FinantialDocument> processCreditNoteSettlementsInclusion(List<FinantialDocument> documents) {
        final List<FinantialDocument> result = Lists.newArrayList(documents);
        
        // Ensure settlement entries of credit entries include credits notes to export
        
        for(final FinantialDocument finantialDocument : documents) {
            if(finantialDocument.isSettlementNote()) {
                final SettlementNote settlementNote = (SettlementNote) finantialDocument;
                
                for (final SettlementEntry settlementEntry : settlementNote.getSettlemetEntriesSet()) {
                    if(settlementEntry.getInvoiceEntry().isCreditNoteEntry()) {
                        if(!result.contains(settlementEntry.getInvoiceEntry().getFinantialDocument())) {
                            result.add(settlementEntry.getInvoiceEntry().getFinantialDocument());
                        }
                    }
                }
            }
        }
        
        return result;
    }

    private static void checkForUnsetDocumentSeriesNumberInDocumentsToExport(List<? extends FinantialDocument> documents) {
        for (final FinantialDocument finantialDocument : documents) {
            if (!finantialDocument.isDocumentSeriesNumberSet()) {
                throw new TreasuryDomainException("error.ERPExporter.document.without.number.series");
            }
        }
    }

    @Override
    public String exportsProductsToXML(FinantialInstitution finantialInstitution) {
        UnaryOperator<AuditFile> auditFilePreProcess = getAuditFilePreProcessOperator(finantialInstitution);
        return exportsProductsToXML(finantialInstitution, auditFilePreProcess);
    }

    protected String exportsProductsToXML(FinantialInstitution finantialInstitution,
            UnaryOperator<AuditFile> preProcessFunctionBeforeSerialize) {
        SAPExporter saftExporter = new SAPExporter();
        return saftExporter.generateERPFile(finantialInstitution, new DateTime(), new DateTime(),
                new ArrayList<FinantialDocument>(), false, true, preProcessFunctionBeforeSerialize);
    }

    @Override
    public String exportsCustomersToXML(FinantialInstitution finantialInstitution) {
        UnaryOperator<AuditFile> auditFilePreProcess = getAuditFilePreProcessOperator(finantialInstitution);
        return exportCustomersToXML(finantialInstitution, auditFilePreProcess);
    }

    protected String exportCustomersToXML(FinantialInstitution finantialInstitution,
            UnaryOperator<AuditFile> preProcessFunctionBeforeSerialize) {
        SAPExporter saftExporter = new SAPExporter();
        return saftExporter.generateERPFile(finantialInstitution, new DateTime(), new DateTime(),
                new ArrayList<FinantialDocument>(), true, false, preProcessFunctionBeforeSerialize);

    }

//    private static final DateTime ERP_END_DATE = DateTimeFormat.forPattern(Constants.STANDARD_DATE_FORMAT_YYYY_MM_DD)
//            .parseLocalDate("2017-01-01").toDateTimeAtStartOfDay();

    @Atomic(mode = TxMode.WRITE)
    @Override
    public ERPExportOperation exportFinantialDocumentToIntegration(final FinantialInstitution institution,
            List<FinantialDocument> documents) {

        checkForUnsetDocumentSeriesNumberInDocumentsToExport(documents);

        //Filter only anulled or closed documents
        documents = documents.stream().filter(x -> x.isAnnulled() || x.isClosed()).collect(Collectors.toList());

        if (!institution.getErpIntegrationConfiguration().isIntegratedDocumentsExportationEnabled()) {
            // Filter documents already exported
            documents = documents.stream().filter(x -> x.isDocumentToExport()).collect(Collectors.toList());
        }

        // TODO: For ERP replacement only allow documents which close date is less than 01/01/2017
        // documents = documents.stream().filter(x -> ERP_END_DATE.isAfter(x.getCloseDate())).collect(Collectors.toList());

        final IntegrationOperationLogBean logBean = new IntegrationOperationLogBean();
        final ERPExportOperation operation = createSaftExportOperation(null, institution, new DateTime());
        documents.forEach(document -> operation.addFinantialDocuments(document));
        try {
            logBean.appendIntegrationLog(Constants.bundle("label.ERPExporter.starting.finantialdocuments.integration"));
            UnaryOperator<AuditFile> preProcessFunctionBeforeSerialize = getAuditFilePreProcessOperator(institution);

            String xml = exportFinantialDocumentToXML(institution, documents, preProcessFunctionBeforeSerialize);
            logBean.appendIntegrationLog(Constants.bundle("label.ERPExporter.erp.xml.content.generated"));

            OperationFile operationFile = writeContentToExportOperation(xml, operation);

            boolean success = sendDocumentsInformationToIntegration(institution, operationFile, logBean);
            operation.getFinantialDocumentsSet().addAll(documents);
            operation.setSuccess(success);

        } catch (Exception ex) {
            writeError(operation, logBean, ex);
        } finally {
            logBean.appendIntegrationLog(Constants.bundle("label.ERPExporter.finished.finantialdocuments.integration"));
            operation.appendLog(logBean.getErrorLog(), logBean.getIntegrationLog(), logBean.getSoapInboundMessage(),
                    logBean.getSoapOutboundMessage());
        }

        return operation;
    }

    @Atomic(mode = TxMode.WRITE)
    @Override
    public ERPExportOperation exportCustomersToIntegration(FinantialInstitution institution) {

        final IntegrationOperationLogBean logBean = new IntegrationOperationLogBean();
        final ERPExportOperation operation = createSaftExportOperation(null, institution, new DateTime());
        try {
            logBean.appendIntegrationLog(Constants.bundle("label.ERPExporter.starting.customers.integration"));

            UnaryOperator<AuditFile> preProcessFunctionBeforeSerialize = getAuditFilePreProcessOperator(institution);
            String xml = exportCustomersToXML(institution, preProcessFunctionBeforeSerialize);
            logBean.appendIntegrationLog(Constants.bundle("label.ERPExporter.erp.xml.content.generated"));

            final OperationFile operationFile = writeContentToExportOperation(xml, operation);

            boolean success = sendDocumentsInformationToIntegration(institution, operationFile, logBean);
            logBean.appendIntegrationLog(Constants.bundle("label.ERPExporter.finished.customers.integration"));

            operation.setSuccess(success);
        } catch (Exception ex) {
            writeError(operation, logBean, ex);
        } finally {
            operation.appendLog(logBean.getErrorLog(), logBean.getIntegrationLog(), logBean.getSoapInboundMessage(),
                    logBean.getSoapOutboundMessage());
        }

        return operation;
    }

    @Atomic(mode = TxMode.WRITE)
    @Override
    public ERPExportOperation exportProductsToIntegration(FinantialInstitution institution) {

        final IntegrationOperationLogBean logBean = new IntegrationOperationLogBean();
        final ERPExportOperation operation = createSaftExportOperation(null, institution, new DateTime());
        try {
            UnaryOperator<AuditFile> preProcessFunctionBeforeSerialize = getAuditFilePreProcessOperator(institution);
            logBean.appendIntegrationLog(Constants.bundle("label.ERPExporter.starting.products.integration"));

            String xml = exportsProductsToXML(institution, preProcessFunctionBeforeSerialize);
            logBean.appendIntegrationLog(Constants.bundle("label.ERPExporter.erp.xml.content.generated"));

            final OperationFile operationFile = writeContentToExportOperation(xml, operation);

            boolean success = sendDocumentsInformationToIntegration(institution, operationFile, logBean);
            logBean.appendIntegrationLog(Constants.bundle("label.ERPExporter.finished.products.integration"));

            operation.setSuccess(success);
        } catch (Exception ex) {
            writeError(operation, logBean, ex);
        } finally {
            operation.appendLog(logBean.getErrorLog(), logBean.getIntegrationLog(), logBean.getSoapInboundMessage(),
                    logBean.getSoapOutboundMessage());
        }

        return operation;
    }

    @Atomic
    @Override
    public ERPExportOperation retryExportToIntegration(final ERPExportOperation eRPExportOperation) {
        if (eRPExportOperation.getFinantialDocumentsSet().isEmpty()) {
        	final IntegrationOperationLogBean logBean = new IntegrationOperationLogBean();
            final ERPExportOperation operation = createSaftExportOperation(eRPExportOperation.getFile().getContent(),
                    eRPExportOperation.getFinantialInstitution(), new DateTime());
            try {
                logBean.appendIntegrationLog(Constants.bundle("label.ERPExporter.starting.retry.integration"));
                for (FinantialDocument document : eRPExportOperation.getFinantialDocumentsSet()) {
                    if (eRPExportOperation.getFinantialInstitution().getErpIntegrationConfiguration()
                            .isIntegratedDocumentsExportationEnabled() || document.isDocumentToExport()) {
                        // Filter documents already exported
                        operation.addFinantialDocuments(document);
                    }
                }

                boolean success = sendDocumentsInformationToIntegration(eRPExportOperation.getFinantialInstitution(),
                        eRPExportOperation.getFile(), logBean);
                operation.setSuccess(success);

            } catch (Exception ex) {
                writeError(operation, logBean, ex);
            } finally {
                logBean.appendIntegrationLog(Constants.bundle("label.ERPExporter.finished.retry.integration"));
                operation.appendLog(logBean.getErrorLog(), logBean.getIntegrationLog(), logBean.getSoapInboundMessage(),
                        logBean.getSoapOutboundMessage());
            }

            return operation;

        } else {
            List<FinantialDocument> allDocuments = new ArrayList<>(eRPExportOperation.getFinantialDocumentsSet());
            ERPExportOperation operation =
                    exportFinantialDocumentToIntegration(eRPExportOperation.getFinantialInstitution(), allDocuments);
            
            final IntegrationOperationLogBean logBean = new IntegrationOperationLogBean();
            logBean.appendIntegrationLog(Constants.bundle("label.ERPExporter.finished.retry.integration"));
            operation.appendLog("", logBean.getIntegrationLog(), "", "");

            return operation;
        }
    }

    @Override
    public void testExportToIntegration(final FinantialInstitution institution) {
        ERPConfiguration erpIntegrationConfiguration = institution.getErpIntegrationConfiguration();
        if (erpIntegrationConfiguration == null) {
            throw new TreasuryDomainException("error.ERPExporter.invalid.erp.configuration");
        }
    }

    @Override
    public void checkIntegrationDocumentStatus(final FinantialDocument document) {
        ERPConfiguration erpIntegrationConfiguration =
                document.getDebtAccount().getFinantialInstitution().getErpIntegrationConfiguration();
        if (erpIntegrationConfiguration == null) {
            throw new TreasuryDomainException("error.ERPExporter.invalid.erp.configuration");
        }

        if (!erpIntegrationConfiguration.getActive()) {
            return;
        }
        IERPExternalService service = erpIntegrationConfiguration.getERPExternalServiceImplementation();
        List<String> documentsList = new ArrayList<String>();
        documentsList.add(document.getUiDocumentNumber());
        List<DocumentStatusWS> integrationStatusFor = service
                .getIntegrationStatusFor(document.getDebtAccount().getFinantialInstitution().getFiscalNumber(), documentsList);
        for (DocumentStatusWS documentStatus : integrationStatusFor) {
            if (documentStatus.getDocumentNumber().equals(document.getUiDocumentNumber())
                    && documentStatus.isIntegratedWithSuccess()) {
                final String message =
                        Constants.bundle("info.ERPExporter.sucess.integrating.document", document.getUiDocumentNumber());

                document.clearDocumentToExport(message);
            }
        }
    }

    private UnaryOperator<AuditFile> getAuditFilePreProcessOperator(final FinantialInstitution finantialInstitution) {
        return (AuditFile x) -> {
            return x;
        };
    }

}
