package org.fenixedu.treasury.services.integration.erp;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.scheduler.TaskRunner;
import org.fenixedu.bennu.scheduler.domain.SchedulerSystem;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.document.SettlementEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.document.reimbursement.ReimbursementProcessStateLog;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.integration.ERPConfiguration;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.services.integration.erp.ERPExternalServiceImplementation.ReimbursementStateBean;
import org.fenixedu.treasury.services.integration.erp.sap.SAPExporter;
import org.fenixedu.treasury.services.integration.erp.tasks.ERPExportSingleDocumentsTask;

import com.google.common.collect.Lists;

import pt.ist.fenixframework.Atomic;

public class ERPExporterManager {

    private static final int WAIT_TRANSACTION_TO_FINISH_MS = 500;

    public static final Comparator<FinantialDocument> COMPARE_BY_DOCUMENT_TYPE = new Comparator<FinantialDocument>() {
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

    public static String saftEncoding(final FinantialInstitution finantialInstitution) {
        final IERPExporter erpExporter =
                finantialInstitution.getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPExporter();

        return erpExporter.saftEncoding();
    }

    @Atomic
    public static String exportFinantialDocumentToXML(final FinantialDocument finantialDocument) {
        final FinantialInstitution finantialInstitution = finantialDocument.getDebtAccount().getFinantialInstitution();
        final IERPExporter erpExporter = finantialDocument.getDebtAccount().getFinantialInstitution()
                .getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPExporter();

        return erpExporter.exportFinantialDocumentToXML(finantialInstitution, Lists.newArrayList(finantialDocument));
    }

    public static List<ERPExportOperation> exportPendingDocumentsForFinantialInstitution(
            final FinantialInstitution finantialInstitution) {
        return exportPendingDocumentsForFinantialInstitution(finantialInstitution, null);
    }

    public static List<ERPExportOperation> exportPendingDocumentsForFinantialInstitution(
            final FinantialInstitution finantialInstitution, Integer numberOfMaxExportsAccepted) {

        final IERPExporter erpExporter =
                finantialInstitution.getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPExporter();

        if (!finantialInstitution.getErpIntegrationConfiguration().getActive()) {
            return Lists.newArrayList();
        }

        final List<FinantialDocument> sortedDocuments = filterDocumentsToExport(
                finantialInstitution.getFinantialDocumentsPendingForExportationSet().stream(), numberOfMaxExportsAccepted);

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

        // return Lists.newArrayList(erpExporter.exportFinantialDocumentToIntegration(finantialInstitution, sortedDocuments));
        return Lists.newArrayList();
    }

    private static final int LIMIT = 200;

    public static List<ERPExportOperation> exportPendingDocumentsForDebtAccount(final DebtAccount debtAccount) {
        final FinantialInstitution finantialInstitution = debtAccount.getFinantialInstitution();

        final List<FinantialDocument> sortedDocuments = filterDocumentsToExport(debtAccount.getFinantialDocumentsSet().stream());

        if (sortedDocuments.isEmpty()) {
            return Lists.newArrayList();
        }

        final IERPExporter erpExporter = debtAccount.getFinantialInstitution().getErpIntegrationConfiguration()
                .getERPExternalServiceImplementation().getERPExporter();

        if (finantialInstitution.getErpIntegrationConfiguration().getExportOnlyRelatedDocumentsPerExport()) {
            final List<ERPExportOperation> result = Lists.newArrayList();

            int i = 0;
            while (!sortedDocuments.isEmpty()) {
                final FinantialDocument doc = sortedDocuments.iterator().next();

                //remove the related documents from the original Set
                sortedDocuments.remove(doc);

                result.add(
                        erpExporter.exportFinantialDocumentToIntegration(finantialInstitution, Collections.singletonList(doc)));

                /* For now limit to 200 finantial documents */
                if (++i >= LIMIT) {
                    System.out.println("ERPExporterManager: Limit " + LIMIT + " finantial documents.");
                    break;
                }
            }

            return result;
        }

        return Lists.newArrayList();
    }

    public static void scheduleSingleDocument(final FinantialDocument finantialDocument) {
        final List<FinantialDocument> documentsToExport =
                filterDocumentsToExport(Collections.singletonList(finantialDocument).stream());

        if (documentsToExport.isEmpty()) {
            return;
        }

        final String externalId = documentsToExport.iterator().next().getExternalId();

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

    public static ERPExportOperation exportSingleDocument(final FinantialDocument finantialDocument) {
        final List<FinantialDocument> documentsToExport =
                filterDocumentsToExport(Collections.singletonList(finantialDocument).stream());

        if (documentsToExport.isEmpty()) {
            return null;
        }

        final FinantialInstitution finantialInstitution = finantialDocument.getDebtAccount().getFinantialInstitution();
        final ERPConfiguration erpIntegrationConfiguration = finantialInstitution.getErpIntegrationConfiguration();

        final IERPExporter erpExporter = erpIntegrationConfiguration.getERPExternalServiceImplementation().getERPExporter();

        return erpExporter.exportFinantialDocumentToIntegration(finantialInstitution, documentsToExport);
    }

    public static ERPExportOperation exportSettlementNote(final SettlementNote settlementNote) {
        List<FinantialDocument> documentsToExport = filterDocumentsToExport(Collections.singletonList(settlementNote).stream());

        if (documentsToExport.isEmpty()) {
            return null;
        }

        for (final SettlementEntry settlementEntry : settlementNote.getSettlemetEntriesSet()) {
            if (settlementEntry.getInvoiceEntry().isDebitNoteEntry()) {
                documentsToExport.add(settlementEntry.getInvoiceEntry().getFinantialDocument());
            }
        }

        documentsToExport = filterDocumentsToExport(documentsToExport.stream());

        final FinantialInstitution finantialInstitution = settlementNote.getDebtAccount().getFinantialInstitution();
        final ERPConfiguration erpIntegrationConfiguration = finantialInstitution.getErpIntegrationConfiguration();

        final IERPExporter erpExporter = erpIntegrationConfiguration.getERPExternalServiceImplementation().getERPExporter();

        if (finantialInstitution.getErpIntegrationConfiguration().getExportOnlyRelatedDocumentsPerExport()) {

            ERPExportOperation settlementExportOperation = null;

            while (!documentsToExport.isEmpty()) {
                final FinantialDocument doc = documentsToExport.iterator().next();

                //remove the related documents from the original Set
                documentsToExport.remove(doc);

                ERPExportOperation exportOperation =
                        erpExporter.exportFinantialDocumentToIntegration(finantialInstitution, Collections.singletonList(doc));
                if (settlementNote == doc) {
                    settlementExportOperation = exportOperation;
                }
            }

            return settlementExportOperation;
        } else {
            // Review
            return null;
        }
    }

    public static void requestPendingDocumentStatus(FinantialInstitution finantialInstitution) {
        final IERPExporter erpExporter =
                finantialInstitution.getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPExporter();
        erpExporter.requestPendingDocumentStatus(finantialInstitution);
    }

    public static ERPExportOperation retryExportToIntegration(final ERPExportOperation eRPExportOperation) {
        final List<FinantialDocument> documentsToExport =
                filterDocumentsToExport(eRPExportOperation.getFinantialDocumentsSet().stream());

        return exportSingleDocument(documentsToExport.iterator().next());
    }

    public static byte[] downloadCertifiedDocumentPrint(final FinantialDocument finantialDocument) {
        final FinantialInstitution finantialInstitution = finantialDocument.getDebtAccount().getFinantialInstitution();

        final IERPExporter erpExporter =
                finantialInstitution.getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPExporter();

        if (!finantialInstitution.getErpIntegrationConfiguration().getActive()) {
            throw new TreasuryDomainException("error.ERPExporterManager.integration.not.active");
        }

        return erpExporter.downloadCertifiedDocumentPrint(finantialDocument);
    }

    public static void updateReimbursementState(final SettlementNote reimbursementNote) {
        final FinantialInstitution finantialInstitution = reimbursementNote.getDebtAccount().getFinantialInstitution();

        final IERPExporter erpExporter =
                finantialInstitution.getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPExporter();

        if (!finantialInstitution.getErpIntegrationConfiguration().getActive()) {
            throw new TreasuryDomainException("error.ERPExporterManager.integration.not.active");
        }

        if (!reimbursementNote.isReimbursement()) {
            throw new RuntimeException("error");
        }

        final ReimbursementStateBean reimbursementStateBean = erpExporter.checkReimbursementState(reimbursementNote);
        if (reimbursementStateBean == null) {
            throw new TreasuryDomainException("error.ERPExporterManager.reimbursementStatusBean.null");
        }

        if (reimbursementStateBean.getReimbursementProcessStatus() == null) {
            throw new TreasuryDomainException("error.ERPExporterManager.reimbursementStatus.unknown");
        }

        ReimbursementProcessStateLog.create(reimbursementNote, reimbursementStateBean.getReimbursementProcessStatus(),
                UUID.randomUUID().toString(), reimbursementStateBean.getReimbursementStateDate(),
                reimbursementStateBean.getExerciseYear());

        reimbursementNote.processReimbursementStateChange(reimbursementStateBean.getReimbursementProcessStatus(),
                reimbursementStateBean.getExerciseYear(), reimbursementStateBean.getReimbursementStateDate());
    }

    public static List<FinantialDocument> filterDocumentsToExport(
            final Stream<? extends FinantialDocument> finantialDocumentsStream) {
        return filterDocumentsToExport(finantialDocumentsStream, null);
    }

    // @formatter:off
    public static List<FinantialDocument> filterDocumentsToExport(
            final Stream<? extends FinantialDocument> finantialDocumentsStream, Integer maxNumberAllowed) {
        return finantialDocumentsStream.filter(d -> d.isDocumentToExport()).filter(d -> !d.isCreditNote())
                .filter(d -> d.isAnnulled() || d.isClosed()).filter(d -> d.isDocumentSeriesNumberSet())
                .filter(x -> x.getCloseDate() != null)
                .filter(x -> x.isDebitNote()
                        || (x.isSettlementNote() && !x.getCloseDate().isBefore(SAPExporter.ERP_INTEGRATION_START_DATE)))
                .filter(x -> maxNumberAllowed == null || x.getErpExportOperationsSet().size() < maxNumberAllowed)
                //.filter(x -> x.isDebitNote() || (x.isSettlementNote() && !((SettlementNote) x).isReimbursement()))
                .sorted(COMPARE_BY_DOCUMENT_TYPE).collect(Collectors.<FinantialDocument> toList());
    }
    // @formatter:on

}
