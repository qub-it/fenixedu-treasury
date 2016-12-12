package org.fenixedu.treasury.services.integration.erp.ERPExternalServiceImplementation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import javax.xml.ws.BindingProvider;

import org.fenixedu.treasury.generated.sources.saft.singap.siag.AuditFile;
import org.fenixedu.treasury.services.integration.erp.IERPExporter;
import org.fenixedu.treasury.services.integration.erp.IERPExternalService;
import org.fenixedu.treasury.services.integration.erp.IERPImporter;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentStatusWS;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentStatusWS.StatusType;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationInput;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationOutput;
import org.fenixedu.treasury.services.integration.erp.singap.siag.SingapSiagExporter;
import org.fenixedu.treasury.services.integration.erp.singap.siag.SingapSiagImporter;
import org.fenixedu.treasury.services.integration.erp.singapWCF.ArrayOfResposta;
import org.fenixedu.treasury.services.integration.erp.singapWCF.Authentication;
import org.fenixedu.treasury.services.integration.erp.singapWCF.IWCFServiceWSF;
import org.fenixedu.treasury.services.integration.erp.singapWCF.Response;
import org.fenixedu.treasury.services.integration.erp.singapWCF.Resposta;
import org.fenixedu.treasury.services.integration.erp.singapWCF.WCFServiceWSF;

import com.qubit.solution.fenixedu.bennu.webservices.services.client.BennuWebServiceClient;
import com.sun.xml.ws.client.BindingProviderProperties;

import pt.ist.fenixframework.Atomic;

public class SINGAPWCFExternalService extends BennuWebServiceClient<IWCFServiceWSF> implements IERPExternalService {

    static {
        //HACK:only for "creation of webserviceclient-configuration"
        createStaticInitializer();
    }

    public SINGAPWCFExternalService() {
    }

    @Atomic
    private static void createStaticInitializer() {
        new SINGAPWCFExternalService();

    }

    @Override
    public IWCFServiceWSF getClient() {
        final IWCFServiceWSF client = super.getClient();

        return client;
    }

    @Override
    public DocumentsInformationOutput sendInfoOnline(DocumentsInformationInput documentsInformation) {
        DocumentsInformationOutput output = new DocumentsInformationOutput();
        output.setDocumentStatus(new ArrayList<DocumentStatusWS>());
        final IWCFServiceWSF client = getClient();

        final SOAPLoggingHandler loggingHandler = SOAPLoggingHandler.createLoggingHandler((BindingProvider) client);

        //Set Timeout for the client
        Map<String, Object> requestContext = ((BindingProvider) client).getRequestContext();
        requestContext.put(BindingProviderProperties.REQUEST_TIMEOUT, 15000); // Timeout in millis
        requestContext.put(BindingProviderProperties.CONNECT_TIMEOUT, 2000); // Timeout in millis

        ArrayOfResposta carregarSAFTON = client.carregarSAFTON(documentsInformation.getData());

//        output.setSoapInboundMessage(loggingHandler.getInboundMessage());
//        output.setSoapOutboundMessage(loggingHandler.getOutboundMessage());
        output.setSoapInboundMessage("");
        output.setSoapOutboundMessage("");

        for (Resposta resposta : carregarSAFTON.getResposta()) {
            output.setRequestId(resposta.getChavePrimaria().getValue());
            DocumentStatusWS status = new DocumentStatusWS();
            status.setDocumentNumber(resposta.getChavePrimaria().getValue());
            status.setErrorDescription(String.format("[STATUS: %s] - %s", resposta.getStatus().getValue(), resposta.getMensagem()
                    .getValue()));
            status.setIntegrationStatus(covertToStatusType(resposta.getStatus().getValue()));
            output.getDocumentStatus().add(status);
        }

        return output;
    }

    @Override
    public String sendInfoOffline(DocumentsInformationInput documentsInformation) {
        throw new RuntimeException("not.implemented");
    }

    @Override
    public List<DocumentStatusWS> getIntegrationStatusFor(String finantialInstitution, List<String> documentsInformation) {
        List<DocumentStatusWS> statusList = new ArrayList<DocumentStatusWS>();

        for (String docId : documentsInformation) {
            Authentication auth = new Authentication();
            Response verificaEstado = getClient().verificaEstado(auth, docId);
            DocumentStatusWS docStatus = new DocumentStatusWS();
            docStatus.setDocumentNumber(docId);
            docStatus.setErrorDescription(verificaEstado.getMessage().getValue());
            docStatus.setIntegrationStatus(covertToStatusType(verificaEstado.getStatus().value()));
            statusList.add(docStatus);
        }

        return statusList;
    }

    private StatusType covertToStatusType(String status) {
        if ("OK".equals(status)) {
            return StatusType.SUCCESS;
        } else {
            return StatusType.ERROR;
        }
    }

    @Override
    protected BindingProvider getService() {
        BindingProvider prov = (BindingProvider) new WCFServiceWSF().getBasicHttpBindingIWCFServiceWSF();
        return prov;
    }

    public UnaryOperator<AuditFile> getAuditFilePreProcessOperator() {

        return (AuditFile x) -> {
            return x;
        };
    }

    @Override
    public IERPExporter getERPExporter() {
        return new SingapSiagExporter();
    }

    @Override
    public IERPImporter getERPImporter(final InputStream inputStream) {
        return new SingapSiagImporter(inputStream);
    }
    
}