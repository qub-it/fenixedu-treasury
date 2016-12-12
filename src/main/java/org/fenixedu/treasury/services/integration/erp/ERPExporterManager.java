package org.fenixedu.treasury.services.integration.erp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;

public class ERPExporterManager {

//    private static Semaphore lockingSemaphore = new Semaphore(0);
//    private static Thread worker;
//    private static Boolean exporterActive = true;
//
//    public static Boolean getERPExporterActive() {
//        return exporterActive;
//    }
//
//    public static void setPushSyncManagerActive(Boolean value) {
//        exporterActive = value;
//        if (exporterActive == true) {
//            initERPExporter();
//        } else {
//            stopPushSyncManager();
//        }
//    }
//
//    private static void initERPExporter() {
//        if (worker == null || worker.isAlive() == false) {
//            worker = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        while (true) {
//
//                            // Stops waiting for lock-release
//                            lockingSemaphore.acquire();
//
//                            if (getERPExporterActive()) {
//                                internalExecute();
//                            } else {
//                                // The exporterManager is marked as inactive, so we
//                                // don't do anything..
//                            }
//
//                            // Clear all Permits
//                            lockingSemaphore.drainPermits();
//
//                        }
//                    } catch (InterruptedException e1) {
//                        // TODO Auto-generated catch block
////                        Log.info(Operations.SYNC, "SYNC_PUSH exiting from task due to Interrupt");
//                    }
//                }
//            });
//            worker.setDaemon(false);
//            worker.setName("Sync Push Notifications Manager Thread");
//            worker.start();
////            Log.info(Operations.SYNC, "SYNC_PUSH Notifier Manager started");
//        } else {
////            Log.info(Operations.SYNC, "SYNC_PUSH Notifier Manager already started");
//        }
//    }
//
//    public static void stopPushSyncManager() {
//
//        if (worker != null && worker.isAlive()) {
//            worker.interrupt();
//            worker = null;
//        }
////        Log.info(Operations.SYNC, "SYNC_PUSH Notifier Manager stopped");
//    }
//
//    public static void signalPushSyncManager() {
//        // Log.info(Operations.SYNC, "SYNC_PUSH Notifier Manager signaled");
//        lockingSemaphore.release();
//    }
//
//    @Atomic
//    private static void internalExecute() {
//
//        try {
//            FinantialInstitution.findAll().forEach(finInstitution -> {
//                // Sleeps always 1 second betweens runs
//                    try {
//                        exportPendingDocumentsForFinantialInstitution(finInstitution);
//                        Thread.sleep(1000);
//                    } catch (Exception e) {
//                        return;
//                    }
//                });
//        } catch (Exception ex) {
////            Log.error(Operations.SYNC, "SYNC_PUSH error executing task " + ex.getMessage(), ex);
//        }
//
//    }

    public static List<ERPExportOperation> exportPendingDocumentsForFinantialInstitution(
            FinantialInstitution finantialInstitution) {
        List<ERPExportOperation> result = new ArrayList<ERPExportOperation>();

        if (finantialInstitution.getErpIntegrationConfiguration().getActive() == false) {
            return result;
        }

        Set<FinantialDocument> pendingDocuments = finantialInstitution.getFinantialDocumentsPendingForExportationSet().stream()
                .filter(x -> x.isAnnulled() || x.isClosed()).limit(100).collect(Collectors.toSet());

        Comparator<FinantialDocument> sortingComparator = new Comparator<FinantialDocument>() {

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
        List<FinantialDocument> sortedDocuments =
                pendingDocuments.stream().sorted(sortingComparator).collect(Collectors.toList());

        //HACK : LIMIT MAX OF 100 DOCUMENTS TO EXPORT!!!
        sortedDocuments = sortedDocuments.stream().limit(100).collect(Collectors.toList());

        if (pendingDocuments.isEmpty() == false) {

            if (finantialInstitution.getErpIntegrationConfiguration().getExportOnlyRelatedDocumentsPerExport()) {
                while (sortedDocuments.isEmpty() == false) {
                    FinantialDocument doc = sortedDocuments.iterator().next();
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

    }

    public static void requestPendingDocumentStatus(FinantialInstitution finantialInstitution) {
        final IERPExporter erpExporter = finantialInstitution.getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPExporter();
        erpExporter.requestPendingDocumentStatus(finantialInstitution);

    }
}
