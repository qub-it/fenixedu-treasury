package org.fenixedu.treasury.services.integration.erp.tasks;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.services.integration.erp.IERPExporter;

import com.google.common.collect.Lists;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

@Task(englishTitle = "Export All Pending Documents to ERP Integration", readOnly = true)
public class ERPExportAllPendingDocumentsTask extends CronTask {

    private static final int LIMIT = 5000;

    @Override
    public void runTask() throws Exception {
        taskLog("Start");
        try {
            final ExportThread e = new ExportThread(this, false);

            e.start();
            e.join();
        } catch (InterruptedException e) {
        }

        try {
            final ExportThread e = new ExportThread(this, true);

            e.start();
            e.join();
        } catch (InterruptedException e) {
        }

        taskLog("End");
    }

    private static class ExportThread extends Thread {

        ERPExportAllPendingDocumentsTask task;
        boolean exportSettlementNotes = false;

        public ExportThread(ERPExportAllPendingDocumentsTask task, final boolean exportSettlementNotes) {
            this.task = task;
            this.exportSettlementNotes = exportSettlementNotes;
        }

        @Override
        public void run() {
            try {
                FenixFramework.getTransactionManager().withTransaction(new Callable() {

                    @Override
                    public Object call() throws Exception {
                        exportPendingDocumentsForFinantialInstitution(task, exportSettlementNotes);

                        return null;
                    }
                }, new Atomic() {

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return null;
                    }

                    @Override
                    public boolean flattenNested() {
                        return false;
                    }

                    @Override
                    public TxMode mode() {
                        return TxMode.READ;
                    }
                });
            } catch (final Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public static List<ERPExportOperation> exportPendingDocumentsForFinantialInstitution(
            final ERPExportAllPendingDocumentsTask task, final boolean exportSettlementNotes) {
        try {

            FinantialInstitution finantialInstitution = FinantialInstitution.findAll().findFirst().orElse(null);

            if (finantialInstitution == null) {
                return Lists.newArrayList();
            }

            List<ERPExportOperation> result = new ArrayList<ERPExportOperation>();

            if (finantialInstitution.getErpIntegrationConfiguration().getActive() == false) {
                return result;
            }

            Set<FinantialDocument> pendingDocuments = finantialInstitution.getFinantialDocumentsPendingForExportationSet()
                    .stream().filter(x -> x.isAnnulled() || x.isClosed())
                    .filter(x -> x.isSettlementNote() == exportSettlementNotes).collect(Collectors.toSet());

            List<FinantialDocument> sortedDocuments = pendingDocuments.stream().collect(Collectors.toList());

            sortedDocuments = sortedDocuments.stream().collect(Collectors.toList());

            int count = 0;
            if (pendingDocuments.isEmpty() == false) {

                if (finantialInstitution.getErpIntegrationConfiguration().getExportOnlyRelatedDocumentsPerExport()) {
                    while (sortedDocuments.isEmpty() == false) {
                        FinantialDocument doc = sortedDocuments.iterator().next();

                        count++;

                        if ((count % 100) == 0) {

                            task.taskLog("Sended %d\n", count);
                        }

                        //remove the related documents from the original Set
                        sortedDocuments.remove(doc);

                        //Create a ExportOperation
                        final IERPExporter erpExporter = finantialInstitution.getErpIntegrationConfiguration()
                                .getERPExternalServiceImplementation().getERPExporter();

                        ERPExportOperation exportFinantialDocumentToIntegration = erpExporter
                                .exportFinantialDocumentToIntegration(finantialInstitution, Collections.singletonList(doc));
                        result.add(exportFinantialDocumentToIntegration);
                    }
                } else {
                    final IERPExporter erpExporter = finantialInstitution.getErpIntegrationConfiguration()
                            .getERPExternalServiceImplementation().getERPExporter();

                    ERPExportOperation exportFinantialDocumentToIntegration =
                            erpExporter.exportFinantialDocumentToIntegration(finantialInstitution, sortedDocuments);

                    result.add(exportFinantialDocumentToIntegration);
                }
            }

            return result;
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}
