package org.fenixedu.treasury.services.integration.erp;

import java.util.List;

import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationInput;
import org.fenixedu.treasury.services.integration.erp.dto.IntegrationStatusOutput;

public interface IERPExternalService {

    public String sendInfoOnline(DocumentsInformationInput documentsInformation);

    public String sendInfoOffline(DocumentsInformationInput documentsInformation);

    public List<IntegrationStatusOutput> getIntegrationStatusFor(String requestIdentification);

}