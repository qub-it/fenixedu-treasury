package org.fenixedu.treasury.services.integration.erp.ERPExternalServiceImplementation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.ws.BindingProvider;

import org.fenixedu.treasury.domain.document.CreditNote;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.document.reimbursement.ReimbursementProcessStatusType;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.integration.ERPConfiguration;
import org.fenixedu.treasury.domain.integration.IntegrationOperationLogBean;
import org.fenixedu.treasury.services.integration.erp.IERPExporter;
import org.fenixedu.treasury.services.integration.erp.IERPExternalService;
import org.fenixedu.treasury.services.integration.erp.IERPImporter;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentStatusWS;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentStatusWS.StatusType;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationInput;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationOutput;
import org.fenixedu.treasury.services.integration.erp.sap.SAPExporter;
import org.fenixedu.treasury.services.integration.erp.sap.SAPImporter;
import org.fenixedu.treasury.services.integration.erp.sap.ZULWSFATURACAOCLIENTESBLK;
import org.fenixedu.treasury.services.integration.erp.sap.ZULWSFATURACAOCLIENTESBLK_Service;
import org.fenixedu.treasury.services.integration.erp.sap.ZulfwscustomersReturn1S;
import org.fenixedu.treasury.services.integration.erp.sap.ZulwsDocumentosInput;
import org.fenixedu.treasury.services.integration.erp.sap.ZulwsDocumentosOutput;
import org.fenixedu.treasury.services.integration.erp.sap.ZulwsReembolsosInput;
import org.fenixedu.treasury.services.integration.erp.sap.ZulwsReembolsosOutput;
import org.fenixedu.treasury.services.integration.erp.sap.ZulwsdocumentStatusWs1;
import org.fenixedu.treasury.services.integration.erp.sap.ZulwsfaturacaoClientesIn;
import org.fenixedu.treasury.services.integration.erp.sap.ZulwsfaturacaoClientesOut;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;

import com.google.common.base.Strings;
import com.qubit.solution.fenixedu.bennu.webservices.services.client.BennuWebServiceClient;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.fault.ServerSOAPFaultException;

public class SAPExternalService extends BennuWebServiceClient<ZULWSFATURACAOCLIENTESBLK> implements IERPExternalService {

    @Override
    public DocumentsInformationOutput sendInfoOnline(DocumentsInformationInput documentsInformation) {
        DocumentsInformationOutput output = new DocumentsInformationOutput();
        output.setDocumentStatus(new ArrayList<DocumentStatusWS>());
        final ZULWSFATURACAOCLIENTESBLK client = getClient();

        final SOAPLoggingHandler loggingHandler = SOAPLoggingHandler.createLoggingHandler((BindingProvider) client);

        //Set Timeout for the client
        Map<String, Object> requestContext = ((BindingProvider) client).getRequestContext();
        requestContext.put(BindingProviderProperties.REQUEST_TIMEOUT, 0); // Timeout in millis
        requestContext.put(BindingProviderProperties.CONNECT_TIMEOUT, 0); // Timeout in millis

        ZulwsfaturacaoClientesIn auditFile = new ZulwsfaturacaoClientesIn();
        auditFile.setFinantialInstitution(documentsInformation.getFinantialInstitution());
        auditFile.setData(documentsInformation.getData());

        ZulwsfaturacaoClientesOut zulwsfaturacaoClientesOut = client.zulfmwsFaturacaoClientes(auditFile);

        output.setRequestId(zulwsfaturacaoClientesOut.getRequestId());
        for (ZulwsdocumentStatusWs1 item : zulwsfaturacaoClientesOut.getDocumentStatus().getItem()) {
            DocumentStatusWS status = new DocumentStatusWS();
            status.setDocumentNumber(item.getDocumentNumber());
            status.setErrorDescription(
                    String.format("[STATUS: %s] - %s", item.getIntegrationStatus(), item.getErrorDescription()));
            status.setIntegrationStatus(
                    convertToStatusType(item.getIntegrationStatus(), item.getDocumentNumber(), item.getSapDocumentNumber()));
            output.getDocumentStatus().add(status);
        }

        for (final ZulfwscustomersReturn1S item : zulwsfaturacaoClientesOut.getCustomers().getItem()) {
            final String otherMessage = String.format("%s (SAP nº %s): [%s] %s",
                    Constants.bundle("label.SAPExternalService.customer.integration.result"),
                    !Strings.isNullOrEmpty(item.getCustomerIdSap()) ? item.getCustomerIdSap() : "", item.getIntegrationStatus(),
                    item.getReturnMsg());

            output.getOtherMessages().add(otherMessage);
        }

        output.setSoapInboundMessage(loggingHandler.getInboundMessage());
        output.setSoapOutboundMessage(loggingHandler.getOutboundMessage());

        return output;
    }

    private StatusType convertToStatusType(final String status, String documentNumber, final String sapDocumentNumber) {
        if (documentNumber.startsWith("NR INT")) {
            return "S".equals(status) ? StatusType.SUCCESS : StatusType.ERROR;
        }

        if (!Strings.isNullOrEmpty(sapDocumentNumber) && "S".equals(status)) {
            return StatusType.SUCCESS;
        }

        return StatusType.ERROR;
    }

    @Override
    public String sendInfoOffline(DocumentsInformationInput documentsInformation) {
        throw new RuntimeException("not.implemented");
    }

    @Override
    public List<DocumentStatusWS> getIntegrationStatusFor(String finantialInstiution, List<String> documentInformaton) {
        throw new RuntimeException("not.implemented");
    }

    @Override
    public byte[] downloadCertifiedDocumentPrint(final String finantialInstitution, final String finantialDocumentNumber,
            String erpIdProcess) {

        final ZULWSFATURACAOCLIENTESBLK client = getClient();

        //Set Timeout for the client
        Map<String, Object> requestContext = ((BindingProvider) client).getRequestContext();
        requestContext.put(BindingProviderProperties.REQUEST_TIMEOUT, 0); // Timeout in millis
        requestContext.put(BindingProviderProperties.CONNECT_TIMEOUT, 0); // Timeout in millis

        ZulwsDocumentosInput input = new ZulwsDocumentosInput();

        input.setTaxRegistrationNumber(finantialInstitution);
        input.setFinantialDocumentNumber(finantialDocumentNumber);
        input.setIdProcesso(erpIdProcess);

        try {
            final ZulwsDocumentosOutput zulwsDocumentos = client.zulwsDocumentos(input);
            final StatusType status =
                    convertToStatusType(zulwsDocumentos.getStatus(), finantialDocumentNumber, finantialDocumentNumber);

            if (status != StatusType.SUCCESS) {
                throw new TreasuryDomainException(
                        "error.IERPExternalService.getCertifiedDocumentPrinted.unable.to.retrieve.document",
                        zulwsDocumentos.getErrorDescription());
            }

            return zulwsDocumentos.getBinary();
        } catch (final ServerSOAPFaultException e) {
            e.printStackTrace();
            throw new TreasuryDomainException(
                    "error.IERPExternalService.getCertifiedDocumentPrinted.unable.to.retrieve.document");
        }

    }

    public ReimbursementStateBean checkReimbursementState(final SettlementNote reimbursementNote,
            final IntegrationOperationLogBean logBean) {
        final ERPConfiguration erpConfiguration =
                reimbursementNote.getDebtAccount().getFinantialInstitution().getErpIntegrationConfiguration();
        final ZULWSFATURACAOCLIENTESBLK client = getClient();

        final SOAPLoggingHandler loggingHandler = SOAPLoggingHandler.createLoggingHandler((BindingProvider) client);

        //Set Timeout for the client
        Map<String, Object> requestContext = ((BindingProvider) client).getRequestContext();
        requestContext.put(BindingProviderProperties.REQUEST_TIMEOUT, 0); // Timeout in millis
        requestContext.put(BindingProviderProperties.CONNECT_TIMEOUT, 0); // Timeout in millis

        final CreditNote creditNote =
                (CreditNote) reimbursementNote.getSettlemetEntries().findFirst().get().getInvoiceEntry().getFinantialDocument();

        ZulwsReembolsosInput input = new ZulwsReembolsosInput();
        input.setFinantialDocumentNumber(creditNote.getUiDocumentNumber());
        input.setIdProcesso(erpConfiguration.getErpIdProcess());
        input.setTaxRegistrationNumber(reimbursementNote.getDebtAccount().getFinantialInstitution().getFiscalNumber());

        final ZulwsReembolsosOutput zulwsReembolsos = client.zulwsReembolsos(input);
        logBean.defineSoapInboundMessage(loggingHandler.getInboundMessage());
        logBean.defineSoapOutboundMessage(loggingHandler.getOutboundMessage());

        final Optional<ReimbursementProcessStatusType> reimbursementStatus =
                ReimbursementProcessStatusType.findUniqueByCode(zulwsReembolsos.getReimbursementStatusCode());

        boolean success = true;
        if (!reimbursementStatus.isPresent()) {
            success = false;
            logBean.appendErrorLog(
                    String.format("Erro na leitura do estado do reembolso: '%s'", zulwsReembolsos.getReimbursementStatusCode()));
        }

        DateTime reimbursementStatusDate = null;
        try {
            reimbursementStatusDate =
                    new DateTime(zulwsReembolsos.getReimbursementStatusDate()).toLocalDate().toDateTimeAtStartOfDay();
        } catch (final IllegalArgumentException e) {
            success = false;
            logBean.appendErrorLog("Erro na leitura da data: " + zulwsReembolsos.getReimbursementStatusDate());
        }

        if (Strings.isNullOrEmpty(zulwsReembolsos.getExerciseYear())) {
            success = false;
            logBean.appendErrorLog("Erro na leitura do ano de exercicio: " + zulwsReembolsos.getExerciseYear());
        }

        final ReimbursementStateBean stateBean = new ReimbursementStateBean(reimbursementNote, reimbursementStatus.orElse(null),
                zulwsReembolsos.getExerciseYear(), reimbursementStatusDate, success);

        return stateBean;
    }

    @Override
    public IERPExporter getERPExporter() {
        return new SAPExporter();
    }

    @Override
    public IERPImporter getERPImporter(InputStream inputStream) {
        return new SAPImporter(inputStream);
    }

    @Override
    protected BindingProvider getService() {
        BindingProvider prov = (BindingProvider) new ZULWSFATURACAOCLIENTESBLK_Service().getZULWSFATURACAOCLIENTESBLK();
        return prov;
    }

}
