package org.fenixedu.treasury.ui.administration.payments.sibs.managesibsinputfile;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.accesscontrol.TreasuryAccessControl;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.SibsReportFile;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;
import org.fenixedu.treasury.services.payments.sibs.SIBSPaymentsImporter;
import org.fenixedu.treasury.services.payments.sibs.SibsPaymentsBrokerService;
import org.fenixedu.treasury.services.payments.sibs.incomming.SibsIncommingPaymentFile;
import org.fenixedu.treasury.services.payments.sibs.SIBSPaymentsImporter.ProcessResult;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.administration.payments.sibs.managesibsreportfile.SibsReportFileController;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@BennuSpringController(value = SibsInputFileController.class)
@RequestMapping(SibsPaymentsBrokerController.CONTROLLER_URL)
public class SibsPaymentsBrokerController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/administration/payments/sibs/sibspaymentsbroker";
    private static final String JSP_PATH = "treasury/administration/payments/sibs/sibspaymentsbroker";

    @RequestMapping
    public String home(Model model) {
        return "redirect:/treasury/administration/payments/sibs/sibspaymentsbroker/invokebroker";
    }

    private static final String INVOKEBROKER_URI = "invokebroker";
    public static final String INVOKEBROKER_URL = CONTROLLER_URL + INVOKEBROKER_URI;

    @RequestMapping(value = INVOKEBROKER_URI, method = RequestMethod.GET)
    public String invokebroker(final Model model) {
        model.addAttribute("paymentCodePoolList", PaymentCodePool.findAll().collect(Collectors.toList()));
        return jspPage("invokebroker");
    }

    public static final String FORM_ACTION_IMPORT = "FORM_ACTION_IMPORT";
    public static final String FORM_ACTION_DEBUG = "FORM_ACTION_DEBUG";
    
    @RequestMapping(value = INVOKEBROKER_URI, method = RequestMethod.POST)
    public String invokebroker(@RequestParam(value = "paymentCodePool", required = false) final PaymentCodePool paymentCodePool,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate toDate,
            @RequestParam(value = "removeInexistentReferenceCodes") final boolean removeInexistentReferenceCodes,
            @RequestParam(value = "removeAlreadyProcessedCodes") final boolean removeAlreadyProcessedCodes, 
            @RequestParam(value = "formAction") final String formAction,
            final Model model,
            final RedirectAttributes redirectAttributes,
            final HttpServletResponse response) {

        try {
            assertUserIsFrontOfficeMember(paymentCodePool.getFinantialInstitution(), model);

            if(FORM_ACTION_DEBUG.equals(formAction)) {
                
                final String sibsContentFile = SibsPaymentsBrokerService.getPaymentsFromBroker(paymentCodePool.getFinantialInstitution(), fromDate, toDate,
                                removeInexistentReferenceCodes, removeAlreadyProcessedCodes);
                
                response.setContentType("text/plain");
                response.setHeader("Content-disposition","attachment; filename=sibs_content_file.txt");
                response.getWriter().write(sibsContentFile);
                response.getOutputStream().flush();
                
                return null;
            } else if(FORM_ACTION_IMPORT.equals(formAction)) {
                
                final SibsIncommingPaymentFile sibsFile =
                        SibsPaymentsBrokerService.readPaymentsFromBroker(paymentCodePool.getFinantialInstitution(), fromDate, toDate,
                                removeInexistentReferenceCodes, removeAlreadyProcessedCodes);
                
                SIBSPaymentsImporter importer = new SIBSPaymentsImporter();
                SibsReportFile reportFile = null;
                try {
                    ProcessResult result = importer.processSIBSPaymentFiles(sibsFile, paymentCodePool.getFinantialInstitution());
                    if (result.getErrorMessages().isEmpty()) {
                        addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.upload"), model);
                    } else {
                        addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.upload"), model);
                    }
                    reportFile = result.getReportFile();
                    if (result.getReportFile() == null) {
                        return redirect(INVOKEBROKER_URI, model, redirectAttributes);
                    } else {
                        reportFile.updateLogMessages(result);
                    }
                } catch (IOException e) {
                    throw new TreasuryDomainException("error.SibsInputFile.error.processing.sibs.input.file");
                }
                
                return redirect(SibsReportFileController.READ_URL + reportFile.getExternalId(), model, redirectAttributes);
            } else {
                throw new TreasuryDomainException("error.SibsPaymentsBroker.unknown.action");
            }
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            return redirect(INVOKEBROKER_URI, model, redirectAttributes);
        }
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }

    protected void assertUserIsFrontOfficeMember(FinantialInstitution finantialInstitution, Model model) {
        if (TreasuryAccessControl.getInstance().isFrontOfficeMember(Authenticate.getUser(), finantialInstitution)) {
            return;
        } else {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "error.authorization.not.frontoffice"), model);
            throw new SecurityException(BundleUtil.getString(Constants.BUNDLE, "error.authorization.not.frontoffice"));
        }
    }

}
