package org.fenixedu.treasury.services.integration.erp.tasks;

import java.util.List;

import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.services.integration.erp.ERPExporterManager;

@Task(englishTitle = "Export Pending Documents to ERP Integration", readOnly = true)
public class ERPExportPendingDocumentsTask extends CronTask {

    @Override
    public void runTask() throws Exception {

        FinantialInstitution.findAll().forEach(x -> {

            taskLog("Start Exporting Pending Documents for : " + x.getName());
            try {
                if (!x.getErpIntegrationConfiguration().getActive()) {
                    return;
                }

                List<ERPExportOperation> exportPendingDocumentsForFinantialInstitution =
                        ERPExporterManager.exportPendingDocumentsForFinantialInstitution(x);

                for (ERPExportOperation exportOperation : exportPendingDocumentsForFinantialInstitution) {
                    for (FinantialDocument doc : exportOperation.getFinantialDocumentsSet()) {
                        taskLog(String.format("Exported document: %s => %s", doc.getUiDocumentNumber(),
                                (exportOperation.getSuccess() ? "OK" : "NOK")));
                    }
                }

                int documentsCount = exportPendingDocumentsForFinantialInstitution.stream()
                        .mapToInt(oper -> oper.getFinantialDocumentsSet().size()).sum();

                taskLog("Finished Exporting %d Pending Documents for : %s", documentsCount, x.getName());

            } catch (Exception ex) {
                taskLog("Error exporting pending documents: " + ex.getMessage());
                for (StackTraceElement el : ex.getStackTrace()) {
                    taskLog(el.toString());
                }
            }
        });
    }
}
