package scripts;

import static org.fenixedu.treasury.domain.document.TreasuryDocumentTemplateFileDomainObject.createFromTreasuryDocumentTemplateFile;
import static org.fenixedu.treasury.domain.document.TreasuryDocumentTemplateFileDomainObject.findUniqueByTreasuryDocumentTemplateFile;
import static org.fenixedu.treasury.domain.forwardpayments.ForwardPaymentConfigurationFileDomainObject.createFromForwardPaymentConfigurationFile;
import static org.fenixedu.treasury.domain.forwardpayments.ForwardPaymentConfigurationFileDomainObject.findUniqueFromForwardPaymentConfigurationFile;
import static org.fenixedu.treasury.domain.forwardpayments.ForwardPaymentLogFileDomainObject.createFromForwardPaymentLogFile;
import static org.fenixedu.treasury.domain.forwardpayments.ForwardPaymentLogFileDomainObject.findUniqueByForwardPaymentLogFile;
import static org.fenixedu.treasury.domain.forwardpayments.PostForwardPaymentsReportFileDomainObject.createFromPostForwardPaymentsReportFile;
import static org.fenixedu.treasury.domain.forwardpayments.PostForwardPaymentsReportFileDomainObject.findUniqueByPostForwardPaymentsReportFile;
import static org.fenixedu.treasury.domain.integration.OperationFileDomainObject.createFromOperationFile;
import static org.fenixedu.treasury.domain.integration.OperationFileDomainObject.findUniqueByOperationFile;
import static org.fenixedu.treasury.domain.paymentcodes.SibsInputFileDomainObject.createFromSibsInputFile;
import static org.fenixedu.treasury.domain.paymentcodes.SibsInputFileDomainObject.findUniqueBySibsInputFile;
import static org.fenixedu.treasury.domain.paymentcodes.SibsOutputFileDomainObject.createFromSibsOutputFile;
import static org.fenixedu.treasury.domain.paymentcodes.SibsOutputFileDomainObject.findUniqueBySibsOutputFile;
import static org.fenixedu.treasury.domain.paymentcodes.SibsReportFileDomainObject.createFromSibsReportFile;
import static org.fenixedu.treasury.domain.paymentcodes.SibsReportFileDomainObject.findUniqueBySibsReportFile;

import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.treasury.domain.document.TreasuryDocumentTemplateFile;
import org.fenixedu.treasury.domain.document.TreasuryDocumentTemplateFileDomainObject;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPaymentConfigurationFile;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPaymentConfigurationFileDomainObject;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPaymentLogFile;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPaymentLogFileDomainObject;
import org.fenixedu.treasury.domain.forwardpayments.PostForwardPaymentsReportFile;
import org.fenixedu.treasury.domain.forwardpayments.PostForwardPaymentsReportFileDomainObject;
import org.fenixedu.treasury.domain.integration.OperationFile;
import org.fenixedu.treasury.domain.integration.OperationFileDomainObject;
import org.fenixedu.treasury.domain.paymentcodes.SibsInputFile;
import org.fenixedu.treasury.domain.paymentcodes.SibsInputFileDomainObject;
import org.fenixedu.treasury.domain.paymentcodes.SibsOutputFile;
import org.fenixedu.treasury.domain.paymentcodes.SibsOutputFileDomainObject;
import org.fenixedu.treasury.domain.paymentcodes.SibsReportFile;
import org.fenixedu.treasury.domain.paymentcodes.SibsReportFileDomainObject;

public class UL_101_CreateDomainObjectsFromGenericFilesCustomTask extends CustomTask {

    @Override
    public void runTask() throws Exception {
        if (true) {
            throw new RuntimeException("abort");
        }

        doIt();

    }

    private void doIt() {

        /* TreasuryDocumentTemplateFile */

        TreasuryDocumentTemplateFile.findAll().filter(f -> !findUniqueByTreasuryDocumentTemplateFile(f).isPresent())
                .forEach(f -> {
                    createFromTreasuryDocumentTemplateFile(f);
                });

        /* SibsReportFile */
        SibsReportFile.findAll().filter(f -> !findUniqueBySibsReportFile(f).isPresent()).forEach(f -> {
            createFromSibsReportFile(f);
        });

        SibsInputFile.findAll().filter(f -> !findUniqueBySibsInputFile(f).isPresent()).forEach(f -> {
            createFromSibsInputFile(f);
        });

        SibsOutputFile.findAll().filter(f -> !findUniqueBySibsOutputFile(f).isPresent()).forEach(f -> {
            createFromSibsOutputFile(f);
        });

        ForwardPaymentConfigurationFile.findAll().filter(f -> !findUniqueFromForwardPaymentConfigurationFile(f).isPresent())
                .forEach(f -> {
                    createFromForwardPaymentConfigurationFile(f);
                });

        OperationFile.findAll().filter(f -> !findUniqueByOperationFile(f).isPresent()).forEach(f -> {
            createFromOperationFile(f);
        });

        ForwardPaymentLogFile.findAll().filter(f -> !findUniqueByForwardPaymentLogFile(f).isPresent()).forEach(f -> {
            createFromForwardPaymentLogFile(f);
        });

        PostForwardPaymentsReportFile.findAll().filter(f -> !findUniqueByPostForwardPaymentsReportFile(f).isPresent())
                .forEach(f -> {
                    createFromPostForwardPaymentsReportFile(f);
                });
        
    }

}
