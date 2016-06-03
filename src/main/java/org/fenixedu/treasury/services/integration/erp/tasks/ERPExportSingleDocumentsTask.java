package org.fenixedu.treasury.services.integration.erp.tasks;

import java.util.Collections;

import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.TaskRunner;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.bennu.scheduler.domain.SchedulerSystem;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.services.integration.erp.IERPExporter;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

@Task(englishTitle = "Export Single Doument to ERP Integration", readOnly = true)
public class ERPExportSingleDocumentsTask extends CronTask {

    String externalId = "";

    public ERPExportSingleDocumentsTask() {
        super();
    }

    public ERPExportSingleDocumentsTask(String documentExternalId) {
        externalId = documentExternalId;
    }

    @Override
    public void runTask() throws Exception {

        if (externalId.equals("")) {
            taskLog("External ID empty, not exporting any document");
        } else {
            try {
                FinantialDocument document = FenixFramework.getDomainObject(externalId);
                if (document != null)
                {
                    if (document.isPreparing())
                    {
                        taskLog("Ignored, trying to export a PREPARING document, oid: " + externalId);
                    }
                    else
                    {
                FinantialInstitution finantialInstitution =
                        document.getDocumentNumberSeries().getSeries().getFinantialInstitution();
                final IERPExporter erpExporter = finantialInstitution.getErpIntegrationConfiguration()
                        .getERPExternalServiceImplementation().getERPExporter();

                ERPExportOperation exportOperation =
                        erpExporter.exportFinantialDocumentToIntegration(finantialInstitution,
                                Collections.singletonList(document));
                taskLog("Exported document: " + document.getUiDocumentNumber() + "=>"
                        + (exportOperation.getSuccess() ? "OK" : "NOK"));

                int MAX_DOCUMENTS_TO_CALL_EXPORT_PENDING_TASK = 10;

                if (finantialInstitution.getFinantialDocumentsPendingForExportationSet().size() > 0
                        && finantialInstitution.getFinantialDocumentsPendingForExportationSet().size() <= MAX_DOCUMENTS_TO_CALL_EXPORT_PENDING_TASK) {

                    //Try to Call ERP Export PendingDocumentsTasks
                    new Thread() {
                        @Override
                        @Atomic
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                            SchedulerSystem.queue(new TaskRunner(new ERPExportPendingDocumentsTask()));
                        };
                    }.start();
                }
                    }
                }
                else{
                    taskLog("Exported document not found oid: " + externalId);
                }
            } catch (Exception ex) {
                taskLog("Error exporting document: " + ex.getMessage());
                for (StackTraceElement el : ex.getStackTrace()) {
                    taskLog(el.toString());
                }
            }
        }
    }
}
