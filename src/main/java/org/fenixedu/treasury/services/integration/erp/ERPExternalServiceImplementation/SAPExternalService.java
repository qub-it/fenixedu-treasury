package org.fenixedu.treasury.services.integration.erp.ERPExternalServiceImplementation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import org.fenixedu.treasury.services.integration.erp.IERPExporter;
import org.fenixedu.treasury.services.integration.erp.IERPExternalService;
import org.fenixedu.treasury.services.integration.erp.IERPImporter;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentStatusWS;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentStatusWS.StatusType;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationInput;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationOutput;
import org.fenixedu.treasury.services.integration.erp.sap.SAPExporter;
import org.fenixedu.treasury.services.integration.erp.sap.SAPImporter;
import org.fenixedu.treasury.services.integration.erp.sap.ZWSFATURACAOCLIENTES;
import org.fenixedu.treasury.services.integration.erp.sap.ZulwsdocumentStatusWs1;
import org.fenixedu.treasury.services.integration.erp.sap.ZulwsfaturacaoClientesIn;
import org.fenixedu.treasury.services.integration.erp.sap.ZulwsfaturacaoClientesOut;
import org.fenixedu.treasury.services.integration.erp.sap.ZwsFaturacaoclientes;

import com.qubit.solution.fenixedu.bennu.webservices.services.client.BennuWebServiceClient;
import com.sun.xml.ws.client.BindingProviderProperties;

public class SAPExternalService extends BennuWebServiceClient<ZwsFaturacaoclientes> implements IERPExternalService {

    @Override
    public DocumentsInformationOutput sendInfoOnline(DocumentsInformationInput documentsInformation) {
        DocumentsInformationOutput output = new DocumentsInformationOutput();
        output.setDocumentStatus(new ArrayList<DocumentStatusWS>());
        final ZwsFaturacaoclientes client = getClient();

        final SOAPLoggingHandler loggingHandler = SOAPLoggingHandler.createLoggingHandler((BindingProvider) client);

        //Set Timeout for the client
        Map<String, Object> requestContext = ((BindingProvider) client).getRequestContext();
        requestContext.put(BindingProviderProperties.REQUEST_TIMEOUT, 15000); // Timeout in millis
        requestContext.put(BindingProviderProperties.CONNECT_TIMEOUT, 2000); // Timeout in millis

        ZulwsfaturacaoClientesIn auditFile = new ZulwsfaturacaoClientesIn();
        auditFile.setFinantialInstitution(documentsInformation.getFinantialInstitution());
        auditFile.setData(documentsInformation.getData());

        ZulwsfaturacaoClientesOut zulwsfaturacaoClientesOut = client.zulfmwsFaturacaoClientes(auditFile);
        
        output.setRequestId(zulwsfaturacaoClientesOut.getRequestId());
        for(ZulwsdocumentStatusWs1 item : zulwsfaturacaoClientesOut.getDocumentStatus().getItem()) {
            DocumentStatusWS status = new DocumentStatusWS();
            status.setDocumentNumber(item.getDocumentNumber());
            status.setErrorDescription(String.format("[STATUS: %s] - %s", item.getIntegrationStatus(), item.getErrorDescription()));
            status.setIntegrationStatus(covertToStatusType(item.getIntegrationStatus()));
            output.getDocumentStatus().add(status);
        }
        
        output.setSoapInboundMessage(loggingHandler.getInboundMessage());
        output.setSoapOutboundMessage(loggingHandler.getOutboundMessage());

        return output;
    }

    private StatusType covertToStatusType(String status) {
        if ("OK".equals(status)) {
            return StatusType.SUCCESS;
        } else {
            return StatusType.ERROR;
        }
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
    public IERPExporter getERPExporter() {
        return new SAPExporter();
    }

    @Override
    public IERPImporter getERPImporter(InputStream inputStream) {
        return new SAPImporter(inputStream);
    }

    @Override
    protected BindingProvider getService() {
        BindingProvider prov = (BindingProvider) new ZWSFATURACAOCLIENTES().getZWSFATURACAOCLIENTESSoap12();
        return prov;
    }

}
