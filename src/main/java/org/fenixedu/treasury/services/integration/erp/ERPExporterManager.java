package org.fenixedu.treasury.services.integration.erp;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.scheduler.TaskRunner;
import org.fenixedu.bennu.scheduler.domain.SchedulerSystem;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.services.integration.erp.tasks.ERPExportSingleDocumentsTask;

import com.google.common.collect.Lists;

import pt.ist.fenixframework.Atomic;

public class ERPExporterManager {

    private static final int WAIT_TRANSACTION_TO_FINISH_MS = 500;

    private static final Comparator<FinantialDocument> COMPARE_BY_DOCUMENT_TYPE = new Comparator<FinantialDocument>() {
        @Override
        public int compare(FinantialDocument o1, FinantialDocument o2) {
            if (o1.getFinantialDocumentType().equals(o2.getFinantialDocumentType())) {
                return o1.getUiDocumentNumber().compareTo(o2.getUiDocumentNumber());
            } else {
                if (o1.isDebitNote()) {
                    return -2;
                } else if (o1.isCreditNote()) {
                    return -1;
                } else if (o1.isSettlementNote()) {
                    return 1;
                }
            }
            return 0;
        }
    };

    public static List<ERPExportOperation> exportPendingDocumentsForFinantialInstitution(
            final FinantialInstitution finantialInstitution) {

        final IERPExporter erpExporter =
                finantialInstitution.getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPExporter();

        if (!finantialInstitution.getErpIntegrationConfiguration().getActive()) {
            return Lists.newArrayList();
        }

        final List<FinantialDocument> sortedDocuments = finantialInstitution.getFinantialDocumentsPendingForExportationSet()
                .stream().filter(x -> !x.isCreditNote() && (x.isAnnulled() || x.isClosed())).collect(Collectors.toSet()).stream()
                .sorted(COMPARE_BY_DOCUMENT_TYPE).collect(Collectors.toList());

        if (sortedDocuments.isEmpty()) {
            return Lists.newArrayList();
        }

        if (finantialInstitution.getErpIntegrationConfiguration().getExportOnlyRelatedDocumentsPerExport()) {
            final List<ERPExportOperation> result = Lists.newArrayList();
            
            while (!sortedDocuments.isEmpty()) {
                final FinantialDocument doc = sortedDocuments.iterator().next();

                //remove the related documents from the original Set
                sortedDocuments.remove(doc);

                result.add(
                        erpExporter.exportFinantialDocumentToIntegration(finantialInstitution, Collections.singletonList(doc)));
            }

            return result;
        }

        return Lists.newArrayList(erpExporter.exportFinantialDocumentToIntegration(finantialInstitution, sortedDocuments));
    }


    public static void scheduleSingleDocument(final FinantialDocument finantialDocument) {
        if (finantialDocument.isCreditNote()) {
            // With SAP Credit notes are exported with settlement notes
            return;
        }

        final String externalId = finantialDocument.getExternalId();

        new Thread() {

            @Override
            @Atomic
            public void run() {
                try {
                    Thread.sleep(WAIT_TRANSACTION_TO_FINISH_MS);
                } catch (InterruptedException e) {
                }

                SchedulerSystem.queue(new TaskRunner(new ERPExportSingleDocumentsTask(externalId)));
            };

        }.start();
    }

    public static void requestPendingDocumentStatus(FinantialInstitution finantialInstitution) {
        final IERPExporter erpExporter =
                finantialInstitution.getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPExporter();
        erpExporter.requestPendingDocumentStatus(finantialInstitution);

    }
    
    public static ERPExportOperation retryExportToIntegration(final ERPExportOperation eRPExportOperation) {
        final IERPExporter erpExporter = eRPExportOperation.getFinantialInstitution().getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPExporter();
        
        ERPExportOperation retryExportOperation = erpExporter.retryExportToIntegration(eRPExportOperation);
        
        return retryExportOperation;
    }
}
