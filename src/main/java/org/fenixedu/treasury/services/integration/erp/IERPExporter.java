package org.fenixedu.treasury.services.integration.erp;

import java.util.List;

import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;

public interface IERPExporter {

    public ERPExportOperation exportFinantialDocumentToIntegration(final FinantialInstitution finantialInstitution,
            List<FinantialDocument> documents);

    public String exportFinantialDocumentToXML(final FinantialInstitution finantialInstitution, final List<FinantialDocument> documents);

    public void checkIntegrationDocumentStatus(final FinantialDocument finantialDocument);

    public String exportsProductsToXML(final FinantialInstitution finantialInstitution);
    public String exportsCustomersToXML(final FinantialInstitution finantialInstitution);

    public ERPExportOperation exportProductsToIntegration(final FinantialInstitution finantialInstitution);
    public ERPExportOperation exportCustomersToIntegration(final FinantialInstitution finantialInstitution);

    public void testExportToIntegration(final FinantialInstitution finantialInstitution);

    public ERPExportOperation retryExportToIntegration(final ERPExportOperation eRPExportOperation);

    public void requestPendingDocumentStatus(final FinantialInstitution finantialInstitution);

}
