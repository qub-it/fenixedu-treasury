package org.fenixedu.treasury.services.integration.erp;

import java.io.InputStream;
import java.util.List;

import org.fenixedu.treasury.services.integration.erp.dto.DocumentStatusWS;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationInput;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationOutput;

public interface IERPExternalService {

    public DocumentsInformationOutput sendInfoOnline(DocumentsInformationInput documentsInformation);

    public String sendInfoOffline(DocumentsInformationInput documentsInformation);

    public List<DocumentStatusWS> getIntegrationStatusFor(String finantialInstiution, List<String> documentInformaton);

    public IERPExporter getERPExporter();
    
    public IERPImporter getERPImporter(final InputStream inputStream);
}