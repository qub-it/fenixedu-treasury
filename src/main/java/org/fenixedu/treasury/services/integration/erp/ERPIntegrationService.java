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
package org.fenixedu.treasury.services.integration.erp;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.document.FinantialDocumentEntry;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.integration.ERPConfiguration;
import org.fenixedu.treasury.domain.integration.ERPImportOperation;
import org.fenixedu.treasury.dto.InterestRateBean;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentStatusWS;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentStatusWS.StatusType;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationInput;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationOutput;
import org.fenixedu.treasury.services.integration.erp.dto.IntegrationStatusOutput;
import org.fenixedu.treasury.services.integration.erp.dto.InterestRequestValueInput;
import org.fenixedu.treasury.services.integration.erp.dto.InterestRequestValueOuptut;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;

import com.google.common.io.Files;
import com.qubit.solution.fenixedu.bennu.webservices.services.server.BennuWebService;

@WebService
public class ERPIntegrationService extends BennuWebService {

    public static boolean validate(String username, String password) {
        Optional<FinantialInstitution> findUniqueByFiscalCode = FinantialInstitution.findUniqueByFiscalCode(username);
        if (findUniqueByFiscalCode.isPresent()) {

            ERPConfiguration configuration = findUniqueByFiscalCode.get().getErpIntegrationConfiguration();
            if (configuration != null) {
                return password.compareToIgnoreCase(configuration.getPassword()) == 0;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.fenixedu.treasury.services.integration.erp.IERPIntegrationService#sendInfoOnline(org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationInput)
     */
    @WebMethod
    public DocumentsInformationOutput sendInfoOnline(DocumentsInformationInput documentsInformation) {
        validateRequestHeader(documentsInformation.getFinantialInstitution());

        FinantialInstitution finantialInstitution = validateFinantialInstitution(documentsInformation);
        //Integrate the information from XML SAFT
        DateTime now = new DateTime();
        String filename = finantialInstitution.getFiscalNumber() + "_" + now.toString() + ".xml";
        ERPImportOperation operation = ERPImportOperation.create(filename, documentsInformation.getData(), finantialInstitution,
                now, false, false, false, null);

        final IERPImporter erpImporter = finantialInstitution.getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPImporter(operation.getFile().getStream());
        final DocumentsInformationOutput result = erpImporter.processAuditFile(operation);
        return result;
    }

    private FinantialInstitution validateFinantialInstitution(DocumentsInformationInput documentsInformation) {
        FinantialInstitution finantialInstitution =
                FinantialInstitution.findUniqueByFiscalCode(documentsInformation.getFinantialInstitution()).orElse(null);

        if (finantialInstitution == null) {
            throw new TreasuryDomainException("error.integration.erp.invalid.fiscalinstitution");
        }
        return finantialInstitution;
    }

    /* (non-Javadoc)
     * @see org.fenixedu.treasury.services.integration.erp.IERPIntegrationService#sendInfoOffline(org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationInput)
     */
    @WebMethod
    public String sendInfoOffline(DocumentsInformationInput documentsInformation) {
        validateRequestHeader(documentsInformation.getFinantialInstitution());

        FinantialInstitution finantialInstitution = validateFinantialInstitution(documentsInformation);
        //Integrate the information from XML SAFT
        DateTime now = new DateTime();
        String filename = finantialInstitution.getFiscalNumber() + "_" + now.toString() + ".xml";
        ERPImportOperation operation = null;
        try {
            File externalFile = new File(documentsInformation.getDataURI());
            byte[] bytes = Files.toByteArray(externalFile);
            operation = ERPImportOperation.create(filename, bytes, finantialInstitution, now, false, false, false, null);
            
            IERPImporter erpImporter = finantialInstitution.getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPImporter(operation.getFile().getStream());
            erpImporter.processAuditFile(operation);
            return operation.getExternalId();
        } catch (Exception e) {
            if (operation != null) {
                operation.appendErrorLog(e.getLocalizedMessage());
                return operation.getExternalId();
            }
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.fenixedu.treasury.services.integration.erp.IERPIntegrationService#getIntegrationStatusFor(java.lang.String)
     */
    @WebMethod
    public IntegrationStatusOutput getIntegrationStatusFor(String finantialInstitution, List<String> documentNumbers) {

        validateRequestHeader(finantialInstitution);
        IntegrationStatusOutput result = new IntegrationStatusOutput();
        List<DocumentStatusWS> statusList = new ArrayList<DocumentStatusWS>();
        for (String documentNumber : documentNumbers) {
//            IntegrationStatusOutput status = new IntegrationStatusOutput();
            DocumentStatusWS docStatus = new DocumentStatusWS();
            FinantialDocument document = FinantialDocument.findByUiDocumentNumber(
                    FinantialInstitution.findUniqueByFiscalCode(finantialInstitution).orElse(null), documentNumber);

            if (document == null) {
                docStatus.setIntegrationStatus(StatusType.ERROR);
            } else {
                docStatus.setIntegrationStatus(StatusType.SUCCESS);
            }
//            status.setDocumentStatus(docStatus);
            statusList.add(docStatus);
        }
        result.setDocumentStatus(statusList);
        result.setRequestId(finantialInstitution);
        return result;
    }

    @WebMethod
    public InterestRequestValueOuptut getInterestValueFor(InterestRequestValueInput interestRequest) {
        final InterestRequestValueOuptut bean = new InterestRequestValueOuptut();
        validateRequestHeader(interestRequest.getFinantialInstitutionFiscalNumber());

        //1. Check if the the lineNumber+DebitNoteNumber is for the Customer of the FinantialInstitution
        final Optional<? extends FinantialDocument> optionalFinantialDocument =
                FinantialDocument.findUniqueByDocumentNumber(interestRequest.getDebitNoteNumber());

        if (!optionalFinantialDocument.isPresent()) {
            throw new RuntimeException(
                    Constants.bundle("error.ERPIntegrationService.debit.note.not.found", interestRequest.getDebitNoteNumber()));
        }

        final FinantialDocument finantialDocument = optionalFinantialDocument.get();

        if (!finantialDocument.isDebitNote()) {
            throw new RuntimeException(Constants.bundle("error.ERPIntegrationService.document.is.not.debit.note",
                    interestRequest.getDebitNoteNumber()));
        }

        if (!finantialDocument.getDebtAccount().getFinantialInstitution().getFiscalNumber()
                .equals(interestRequest.getFinantialInstitutionFiscalNumber())) {
            throw new RuntimeException(Constants.bundle("error.ERPIntegrationService.finantial.institution.fiscal.number.invalid",
                    interestRequest.getFinantialInstitutionFiscalNumber()));
        }

        if (!finantialDocument.getDebtAccount().getCustomer().getCode().equals(interestRequest.getCustomerCode())) {
            throw new RuntimeException(
                    Constants.bundle("error.ERPIntegrationService.customer.code.invalid", interestRequest.getCustomerCode()));
        }

        //2. Check if the lineNumber+DebitNoteNumber Amount is correct
        final Optional<? extends FinantialDocumentEntry> optionalDebitEntry =
                FinantialDocumentEntry.findUniqueByEntryOrder(finantialDocument, interestRequest.getLineNumber());

        if (!optionalDebitEntry.isPresent()) {
            throw new RuntimeException(Constants.bundle("error.ERPIntegrationService.debit.entry.not.found",
                    String.valueOf(interestRequest.getLineNumber() != null ? interestRequest.getLineNumber() : "null"),
                    finantialDocument.getUiDocumentNumber()));
        }

        FinantialDocumentEntry finantialDocumentEntry = optionalDebitEntry.get();

        if (!(finantialDocumentEntry instanceof DebitEntry)) {
            throw new RuntimeException(Constants.bundle("error.ERPIntegrationService.finantial.document.entry.is.not.debit.entry",
                    String.valueOf(finantialDocumentEntry.getEntryOrder()),
                    String.valueOf(finantialDocumentEntry.getFinantialDocument().getUiDocumentNumber())));
        }

        final DebitEntry debitEntry = (DebitEntry) finantialDocumentEntry;

        final BigDecimal amountInDebt = debitEntry.amountInDebt(interestRequest.convertPaymentDateToLocalDate());

        if (!Constants.isPositive(amountInDebt)) {
            throw new RuntimeException(Constants.bundle("error.ERPIntegrationService.debit.entry.with.no.debt",
                    String.valueOf(finantialDocumentEntry.getEntryOrder()),
                    String.valueOf(finantialDocumentEntry.getFinantialDocument().getUiDocumentNumber())));
        }

        if (!Constants.isEqual(amountInDebt, interestRequest.getAmount())) {
            throw new RuntimeException(Constants.bundle("error.ERPIntegrationService.debit.entry.amount.is.not.equal", 
                    interestRequest.getAmount() != null ? interestRequest.getAmount().toString() : "null",
                    String.valueOf(finantialDocumentEntry.getEntryOrder()),
                    String.valueOf(finantialDocumentEntry.getFinantialDocument().getUiDocumentNumber()),
                    amountInDebt.toString()));
        }

        if (interestRequest.getGenerateInterestDebitNote() == true
                && interestRequest.convertPaymentDateToLocalDate().isAfter(new LocalDate())) {
            throw new RuntimeException("Cannot generate interest in the FUTURE. Please call without genereateInterestDebitNote");
        }

        //3 . calculate the amount of interest for the DebitEntry
        final InterestRateBean interestRateBean =
                debitEntry.calculateAllInterestValue(interestRequest.convertPaymentDateToLocalDate());

        //calculate the amount of UNDEBITTED interest for the DebitEntry
        final InterestRateBean undebittedInterestRateBean =
                debitEntry.calculateUndebitedInterestValue(interestRequest.convertPaymentDateToLocalDate());

        bean.setInterestAmount(interestRateBean.getInterestAmount());
        bean.setDescription(interestRateBean.getDescription());

        //If we have undebittedInterestDebit and we want to create, genererate debitEntries
        if (Constants.isGreaterThan(undebittedInterestRateBean.getInterestAmount(), BigDecimal.ZERO)
                && interestRequest.getGenerateInterestDebitNote()) {
            processInterestEntries(debitEntry, undebittedInterestRateBean, interestRequest.convertPaymentDateToLocalDate());
        }

        final List<FinantialDocument> interestFinantialDocumentsSet = debitEntry.getInterestDebitEntriesSet().stream()
                .filter(l -> l.isProcessedInClosedDebitNote()).map(l -> l.getFinantialDocument()).collect(Collectors.toList());

        if (interestFinantialDocumentsSet.size() > 0) {
            final IERPExporter erpExporter = debitEntry.getDebtAccount().getFinantialInstitution().getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPExporter();
            final String saftResult = erpExporter.exportFinantialDocumentToXML(
                    debitEntry.getDebtAccount().getFinantialInstitution(), interestFinantialDocumentsSet);

            try {
                bean.setInterestDocumentsContent(saftResult.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        return bean;
    }

    @Atomic
    private void processInterestEntries(final DebitEntry debitEntry, final InterestRateBean interestRateBean,
            final LocalDate paymentDate) {
        DocumentNumberSeries debitNoteSeries = DocumentNumberSeries.find(FinantialDocumentType.findForDebitNote(),
                debitEntry.getFinantialDocument().getDocumentNumberSeries().getSeries());

        final DebitNote interestDebitNote =
                DebitNote.create(debitEntry.getDebtAccount(), debitNoteSeries, paymentDate.toDateTimeAtStartOfDay());

        debitEntry.createInterestRateDebitEntry(interestRateBean, paymentDate.toDateTimeAtStartOfDay(),
                Optional.<DebitNote> ofNullable(interestDebitNote));
        String documentObservations =
                BundleUtil.getString(Constants.BUNDLE, "info.ERPIntegrationService.interest.rate.created.by.ERP.Integration");
        documentObservations = documentObservations + " - "
                + BundleUtil.getString(Constants.BUNDLE, "info.ERPIntegrationService.interest.rate.payment.date")
                + paymentDate.toString("YYYY-MM-dd");
        interestDebitNote.setDocumentObservations(documentObservations);
        interestDebitNote.closeDocument();
    }

    private void validateRequestHeader(String finantialInstitution) {
//        if (finantialInstitution == null || getSecurityHeader() == null
//                || !finantialInstitution.equalsIgnoreCase(getSecurityHeader().getUsername())) {
//            throw new SecurityException("invalid request permission");
//        }
    }

}
