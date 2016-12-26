package org.fenixedu.treasury.services.integration.erp;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.fenixedu.bennu.scheduler.TaskRunner;
import org.fenixedu.bennu.scheduler.domain.SchedulerSystem;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.document.reimbursement.ReimbursementProcessStateLog;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.integration.ERPConfiguration;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.services.integration.erp.ERPExternalServiceImplementation.ReimbursementStateBean;
import org.fenixedu.treasury.services.integration.erp.tasks.ERPExportSingleDocumentsTask;
import org.fenixedu.treasury.util.Constants;

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

    public static String saftEncoding(final FinantialInstitution finantialInstitution) {
        final IERPExporter erpExporter = finantialInstitution
                .getErpIntegrationConfiguration().getERPExternalServiceImplementation().getERPExporter();

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
        final IERPExporter erpExporter = eRPExportOperation.getFinantialInstitution().getErpIntegrationConfiguration()
                .getERPExternalServiceImplementation().getERPExporter();

        ERPExportOperation retryExportOperation = erpExporter.retryExportToIntegration(eRPExportOperation);

        return retryExportOperation;
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

}
