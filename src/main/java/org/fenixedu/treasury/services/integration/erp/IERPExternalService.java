package org.fenixedu.treasury.services.integration.erp;

import java.util.List;
import java.util.function.UnaryOperator;

import oecd.standardauditfile_tax.pt_1.AuditFile;

import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentStatusWS;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationInput;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationOutput;

public interface IERPExternalService {

    public DocumentsInformationOutput sendInfoOnline(final FinantialInstitution finantialInstitution,
            final DocumentsInformationInput documentsInformation);

    public String sendInfoOffline(DocumentsInformationInput documentsInformation);

    public List<DocumentStatusWS> getIntegrationStatusFor(String finantialInstiution, List<String> documentInformaton);

    public UnaryOperator<AuditFile> getAuditFilePreProcessOperator();

}