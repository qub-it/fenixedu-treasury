package org.fenixedu.treasury.services.integration.erp;

import org.fenixedu.treasury.domain.integration.ERPImportOperation;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationOutput;

public interface IERPImporter {

    public DocumentsInformationOutput processAuditFile(final ERPImportOperation operation);
    
    public String readTaxRegistrationNumberFromAuditFile();
}
