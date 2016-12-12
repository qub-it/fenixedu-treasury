package org.fenixedu.treasury.services.integration.erp.tasks;

import java.util.Collections;

import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.services.integration.erp.IERPExporter;

import com.google.common.base.Strings;

import pt.ist.fenixframework.FenixFramework;

@Task(englishTitle = "Export Single Doument to ERP Integration", readOnly = true)
public class ERPExportSingleDocumentsTask extends CronTask {

    private String externalId;

    public ERPExportSingleDocumentsTask(final String documentExternalId) {
        externalId = documentExternalId;
    }

    @Override
    public void runTask() throws Exception {

        if (Strings.isNullOrEmpty(externalId)) {
            taskLog("External ID empty, not exporting any document");
            return;
        }

        FinantialDocument document = FenixFramework.getDomainObject(externalId);
        if (document == null) {
            return;
        }
        
        if (document.isPreparing()) {
            taskLog("Ignored, trying to export a PREPARING document, oid: " + externalId);
            return;
        }

        final FinantialInstitution finantialInstitution = document.getDocumentNumberSeries().getSeries().getFinantialInstitution();
        final IERPExporter erpExporter =
                finantialInstitution.getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPExporter();

        try {
            final ERPExportOperation exportOperation =
                    erpExporter.exportFinantialDocumentToIntegration(finantialInstitution, Collections.singletonList(document));

            taskLog(String.format("Exported document: %s => %s", document.getUiDocumentNumber(),
                    (exportOperation.getSuccess() ? "OK" : "NOK")));

//                        int MAX_DOCUMENTS_TO_CALL_EXPORT_PENDING_TASK = 10;
//
//                        if (finantialInstitution.getFinantialDocumentsPendingForExportationSet().size() > 0
//                                && finantialInstitution.getFinantialDocumentsPendingForExportationSet()
//                                        .size() <= MAX_DOCUMENTS_TO_CALL_EXPORT_PENDING_TASK) {
//
//                            //Try to Call ERP Export PendingDocumentsTasks
//                            new Thread() {
//                                @Override
//                                @Atomic
//                                public void run() {
//                                    try {
//                                        Thread.sleep(1000);
//                                    } catch (InterruptedException e) {
//                                    }
//                                    SchedulerSystem.queue(new TaskRunner(new ERPExportPendingDocumentsTask()));
//                                };
//                            }.start();
//                        }
        } catch (final Exception ex) {
            taskLog("Error exporting document: " + ex.getMessage());
            for (StackTraceElement el : ex.getStackTrace()) {
                taskLog(el.toString());
            }
        }
    }
    
}
