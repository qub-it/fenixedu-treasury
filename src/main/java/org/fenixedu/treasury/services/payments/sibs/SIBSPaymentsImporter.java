/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Academic.
 *
 * FenixEdu Academic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Academic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Academic.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.treasury.services.payments.sibs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCode;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCodeStateType;
import org.fenixedu.treasury.domain.paymentcodes.SibsInputFile;
import org.fenixedu.treasury.domain.paymentcodes.SibsReportFile;
import org.fenixedu.treasury.services.payments.sibs.incomming.SibsIncommingPaymentFile;
import org.fenixedu.treasury.services.payments.sibs.incomming.SibsIncommingPaymentFileDetailLine;
import org.fenixedu.treasury.util.Constants;

public class SIBSPaymentsImporter {

    static private final String PAYMENT_FILE_EXTENSION = "INP";

    public class ProcessResult {

        List<String> actionMessages = new ArrayList<String>();
        List<String> errorMessages = new ArrayList<String>();
        SibsReportFile reportFile;

        public List<String> getActionMessages() {
            return actionMessages;
        }

        public List<String> getErrorMessages() {
            return errorMessages;
        }

//        private final TreasuryBaseController baseController;
        private boolean processFailed = false;

        public void addMessage(String message, String... args) {
            actionMessages.add(BundleUtil.getString(Constants.BUNDLE, message, args));
        }

        public void addError(String message, String... args) {
            errorMessages.add(BundleUtil.getString(Constants.BUNDLE, message, args));
            reportFailure();
        }

        protected void reportFailure() {
            processFailed = true;
        }

        public boolean hasFailed() {
            return processFailed;
        }

        public void setReportFile(SibsReportFile reportFile) {
            this.reportFile = reportFile;
        }

        public SibsReportFile getReportFile() {

            return reportFile;
        }
    }

    public ProcessResult processSIBSPaymentFiles(SibsInputFile inputFile) throws IOException {
        ProcessResult result = new ProcessResult();

        if (StringUtils.endsWithIgnoreCase(inputFile.getFilename(), PAYMENT_FILE_EXTENSION)) {
            result.addMessage("label.manager.SIBS.processingFile", inputFile.getFilename());
            try {
                processFile(inputFile, result);
            } catch (FileNotFoundException e) {
                throw new TreasuryDomainException("error.manager.SIBS.zipException", getMessage(e));
            } catch (IOException e) {
                throw new TreasuryDomainException("error.manager.SIBS.IOException", getMessage(e));
            } catch (Exception e) {
                throw new TreasuryDomainException("error.manager.SIBS.fileException", getMessage(e));
            } finally {
            }
        } else {
            throw new TreasuryDomainException("error.manager.SIBS.notSupportedExtension", inputFile.getFilename());
        }
        return result;
    }

//    private PaymentReferenceCode getPaymentReferenceCode(final FinantialInstitution finantialInstitution, final String code,
//            ProcessResult result) {
//
//        return PaymentReferenceCode.findByReferenceCode(code, finantialInstitution).findFirst().orElse(null);
//    }

    protected String getMessage(Exception ex) {
        String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();

        return message;
    }

    protected void processFile(SibsInputFile inputFile, ProcessResult processResult) throws IOException {
        processResult.addMessage("label.manager.SIBS.processingFile", inputFile.getFilename());

        InputStream fileInputStream = null;
        try {
            fileInputStream = inputFile.getStream();
            final User person = Authenticate.getUser();
            final SibsIncommingPaymentFile sibsFile = SibsIncommingPaymentFile.parse(inputFile.getFilename(), fileInputStream);

            processResult.addMessage("label.manager.SIBS.linesFound", String.valueOf(sibsFile.getDetailLines().size()));
            processResult.addMessage("label.manager.SIBS.startingProcess");

            for (final SibsIncommingPaymentFileDetailLine detailLine : sibsFile.getDetailLines()) {
                try {
                    processCode(detailLine, person, processResult, inputFile.getFinantialInstitution(), inputFile.getFilename()
                            .replace("\\.inp", ""));
                } catch (Exception e) {
                    processResult.addError("error.manager.SIBS.processException", detailLine.getCode(), getMessage(e));
                }
            }

            processResult.addMessage("label.manager.SIBS.creatingReport");

            if (processResult.hasFailed()) {
                processResult.addError("error.manager.SIBS.nonProcessedCodes");
            }

            try {
                final SIBSImportationFileDTO reportDTO =
                        new SIBSImportationFileDTO(sibsFile, inputFile.getFinantialInstitution());
                SibsReportFile reportFile = SibsReportFile.processSIBSIncommingFile(reportDTO);
                processResult.addMessage("label.manager.SIBS.reportCreated");
                processResult.setReportFile(reportFile);

            } catch (Exception ex) {
                ex.printStackTrace();
                processResult.addError("error.manager.SIBS.reportException", getMessage(ex));
            }

            processResult.addMessage("label.manager.SIBS.done");

        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    protected void processCode(SibsIncommingPaymentFileDetailLine detailLine, User person, ProcessResult result,
            FinantialInstitution finantialInstitution, final String sibsImportationFile) throws Exception {

        final PaymentReferenceCode paymentCode = getPaymentCode(detailLine.getCode(), finantialInstitution);

        if (paymentCode == null) {
            result.addMessage("error.manager.SIBS.codeNotFound", detailLine.getCode());
            throw new Exception();
        }

        final PaymentReferenceCode codeToProcess = getPaymentCodeToProcess(paymentCode, result);

        if (codeToProcess.getState() == PaymentReferenceCodeStateType.ANNULLED) {
            result.addMessage("warning.manager.SIBS.anulledCode", codeToProcess.getReferenceCode());
        }

        if (codeToProcess.isProcessed()
                && codeToProcess.getVersioningUpdateDate().isBefore(detailLine.getWhenOccuredTransaction())) {
            result.addMessage("warning.manager.SIBS.codeAlreadyProcessed", codeToProcess.getReferenceCode());
        }

        SettlementNote settlementNote =
                codeToProcess.processPayment(person, detailLine.getAmount(), detailLine.getWhenOccuredTransaction(),
                        detailLine.getSibsTransactionId(), sibsImportationFile);

        if (settlementNote != null) {
            //HURRAY!!! Payment received....
        }
    }

    /**
     * Copied from head
     */
    private PaymentReferenceCode getPaymentCodeToProcess(final PaymentReferenceCode paymentCode, ProcessResult result) {

        final PaymentReferenceCode codeToProcess;
//        if (mapping != null) {
//
//            result.addMessage("warning.manager.SIBS.foundMapping", paymentCode.getReferenceCode(), mapping.getNewPaymentCode()
//                    .getCode());
//            result.addMessage("warning.manager.SIBS.invalidating", paymentCode.getReferenceCode());
//
//            codeToProcess = mapping.getNewPaymentCode();
//            paymentCode.setState(PaymentReferenceCodeStateType.ANNULLED);
//
//        } else {
        codeToProcess = paymentCode;
//        }

        return codeToProcess;
    }

    /**
     * Copied from head
     */
    private PaymentReferenceCode getPaymentCode(final String code, FinantialInstitution finantialInstitution) {
        /*
         * TODO:
         * 
         * 09/07/2009 - Payments are not related only to students. readAll() may
         * be heavy to get the PaymentCode.
         * 
         * 
         * Ask Nadir and Joao what is best way to deal with PaymentCode
         * retrieval.
         */

        return PaymentReferenceCode.findByReferenceCode(code, finantialInstitution).findFirst().orElse(null);
    }

}
