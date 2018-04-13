/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and ServiÃ§os Partilhados da
 * Universidade de Lisboa:
 *  - Copyright Â© 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright Â© 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: ricardo.pedro@qub-it.com, anil.mamede@qub-it.com
 *
 * 
 * This file is part of FenixEdu Treasury.
 *
 * FenixEdu Treasury is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Treasury is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Treasury.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.treasury.ui.administration.payments.sibs.managesibsinputfile;

import static org.fenixedu.treasury.util.Constants.treasuryBundle;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.SibsInputFile;
import org.fenixedu.treasury.domain.paymentcodes.SibsReportFile;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;
import org.fenixedu.treasury.services.payments.sibs.SIBSPaymentsImporter;
import org.fenixedu.treasury.services.payments.sibs.SIBSPaymentsImporter.ProcessResult;
import org.fenixedu.treasury.services.payments.sibs.incomming.SibsIncommingPaymentFile;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.ui.administration.payments.sibs.managesibsreportfile.SibsReportFileController;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

//@Component("org.fenixedu.treasury.ui.administration.payments.sibs.manageSibsInputFile") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.administration.payments.sibs.manageSibsInputFile",
        accessGroup = "treasuryBackOffice")
@RequestMapping(SibsInputFileController.CONTROLLER_URL)
public class SibsInputFileController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/administration/payments/sibs/managesibsinputfile/sibsinputfile";

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/";
    }

    private SibsInputFile getSibsInputFile(Model model) {
        return (SibsInputFile) model.asMap().get("sibsInputFile");
    }

    private void setSibsInputFile(SibsInputFile sibsInputFile, Model model) {
        model.addAttribute("sibsInputFile", sibsInputFile);
    }

    @Atomic
    public void deleteSibsInputFile(SibsInputFile sibsInputFile) {
        sibsInputFile.delete();
    }

    private static final String _SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI)
    public String search(@RequestParam(value = "whenprocessedbysibs", required = false) @DateTimeFormat(
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ") LocalDate whenProcessedBySibs, Model model) {
        List<SibsInputFile> searchsibsinputfileResultsDataSet = filterSearchSibsInputFile(whenProcessedBySibs);

        model.addAttribute("searchsibsinputfileResultsDataSet", searchsibsinputfileResultsDataSet);
        
        final Boolean brokerActive = FinantialInstitution.findAll().map(f -> f.getSibsConfiguration().isPaymentsBrokerActive()).reduce((a , c) -> a || c).orElse(Boolean.FALSE); 
        model.addAttribute("brokerActive", brokerActive);

        return "treasury/administration/payments/sibs/managesibsinputfile/sibsinputfile/search";
    }

    private Stream<SibsInputFile> getSearchUniverseSearchSibsInputFileDataSet() {
        return SibsInputFile.findAll();
    }

    private List<SibsInputFile> filterSearchSibsInputFile(org.joda.time.LocalDate whenProcessedBySibs) {
        return getSearchUniverseSearchSibsInputFileDataSet().filter(
                sibsInputFile -> whenProcessedBySibs == null
                        || whenProcessedBySibs.equals(sibsInputFile.getWhenProcessedBySibs())).collect(Collectors.toList());
    }

    private static final String _SEARCH_TO_VIEW_ACTION_URI = "/search/view/";
    public static final String SEARCH_TO_VIEW_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_VIEW_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_VIEW_ACTION_URI + "{oid}")
    public String processSearchToViewAction(@PathVariable("oid") SibsInputFile sibsInputFile, Model model,
            RedirectAttributes redirectAttributes) {

        return redirect(READ_URL + sibsInputFile.getExternalId(), model, redirectAttributes);
    }

    private static final String _READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + _READ_URI;

    @RequestMapping(value = _READ_URI + "{oid}")
    public String read(@PathVariable("oid") SibsInputFile sibsInputFile, Model model) {
        setSibsInputFile(sibsInputFile, model);
        return "treasury/administration/payments/sibs/managesibsinputfile/sibsinputfile/read";
    }

    private static final String _DELETE_URI = "/delete/";
    public static final String DELETE_URL = CONTROLLER_URL + _DELETE_URI;

    @RequestMapping(value = _DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") SibsInputFile sibsInputFile, Model model, RedirectAttributes redirectAttributes) {

        setSibsInputFile(sibsInputFile, model);
        try {
            assertUserIsFrontOfficeMember(sibsInputFile.getFinantialInstitution(), model);

            deleteSibsInputFile(sibsInputFile);

            addInfoMessage(Constants.treasuryBundle("label.success.delete"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (TreasuryDomainException tex) {
            addErrorMessage(Constants.treasuryBundle("label.error.delete") + tex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(Constants.treasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);
        }

        return redirect(READ_URL + getSibsInputFile(model).getExternalId(), model, redirectAttributes);
    }

    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {

        return "treasury/administration/payments/sibs/managesibsinputfile/sibsinputfile/create";
    }

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "whenprocessedbysibs", required = false) @DateTimeFormat(
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ") DateTime whenProcessedBySibs, @RequestParam(value = "documentSibsInputFile",
            required = true) MultipartFile documentSibsInputFile, Model model, RedirectAttributes redirectAttributes) {

        try {
            assertUserIsFrontOfficeMember(model);

            SibsInputFile sibsInputFile = createSibsInputFile(whenProcessedBySibs, documentSibsInputFile);

            if (sibsInputFile.getWhenProcessedBySibs().compareTo(whenProcessedBySibs) != 0) {
                addWarningMessage(BundleUtil.getString(Constants.BUNDLE,
                        "warning.SibsInputFileController.whenprocessedbysibs.different.in.file", sibsInputFile
                                .getWhenProcessedBySibs().toString()), model);
            }
            model.addAttribute("sibsInputFile", sibsInputFile);
            return redirect(READ_URL + getSibsInputFile(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tex) {
            addErrorMessage(Constants.treasuryBundle("label.error.create") + tex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(Constants.treasuryBundle("label.error.create") + ex.getLocalizedMessage(), model);
        }
        return create(model);
    }

    @Atomic
    public SibsInputFile createSibsInputFile(DateTime whenProcessedBySibs, MultipartFile documentSibsInputFile) {

        PaymentCodePool pool = null;

        try {
            SibsIncommingPaymentFile file =
                    SibsIncommingPaymentFile.parse(documentSibsInputFile.getOriginalFilename(),
                            documentSibsInputFile.getInputStream());
            if (file.getHeader().getWhenProcessedBySibs().toDateTimeAtMidnight().compareTo(whenProcessedBySibs) != 0) {
                whenProcessedBySibs = file.getHeader().getWhenProcessedBySibs().toDateTimeAtMidnight();
            }

            String entityCode = file.getHeader().getEntityCode();

            pool = PaymentCodePool.findByEntityCode(entityCode).findFirst().orElse(null);
        } catch (IOException e) {
            throw new TreasuryDomainException(
                    "label.error.administration.payments.sibs.managesibsinputfile.error.in.sibs.inputfile",
                    e.getLocalizedMessage());
        } catch (RuntimeException ex) {
            throw new TreasuryDomainException(
                    "label.error.administration.payments.sibs.managesibsinputfile.error.in.sibs.inputfile",
                    ex.getLocalizedMessage());
        }

        if (pool == null) {
            throw new TreasuryDomainException(
                    "label.error.administration.payments.sibs.managesibsinputfile.error.in.sibs.inputfile.poolNull");
        }

        SibsInputFile sibsInputFile =
                SibsInputFile.create(pool.getFinantialInstitution(), whenProcessedBySibs, documentSibsInputFile.getName(),
                        documentSibsInputFile.getOriginalFilename(), getContent(documentSibsInputFile), Authenticate.getUser());
        return sibsInputFile;

    }

    private static final String _PROCESS_URI = "/read/process/";
    public static final String PROCESS_URL = CONTROLLER_URL + _PROCESS_URI;

    @RequestMapping(value = _PROCESS_URI + "{oid}", method = RequestMethod.POST)
    public String processReadToProcessFile(@PathVariable("oid") SibsInputFile sibsInputFile, Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) {
        setSibsInputFile(sibsInputFile, model);
        try {
            assertUserIsFrontOfficeMember(sibsInputFile.getFinantialInstitution(), model);

            SIBSPaymentsImporter importer = new SIBSPaymentsImporter();
            SibsReportFile reportFile = null;
            try {
                ProcessResult result = importer.processSIBSPaymentFiles(sibsInputFile);
                if (result.getErrorMessages().isEmpty()) {
                    addInfoMessage(treasuryBundle("label.success.upload"), model);
                } else {
                    addErrorMessage(treasuryBundle("label.error.upload"), model);
                }
                reportFile = result.getReportFile();
                if (result.getReportFile() == null) {
                    return redirect(READ_URL + sibsInputFile.getExternalId(), model, redirectAttributes);
                } else {
                    reportFile.updateLogMessages(result);
                }
            } catch (IOException e) {
                throw new TreasuryDomainException("error.SibsInputFile.error.processing.sibs.input.file");
            }

            return redirect(SibsReportFileController.READ_URL + reportFile.getExternalId(), model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            return redirect(READ_URL + sibsInputFile.getExternalId(), model, redirectAttributes);
        }
    }

    private static final String _DOWNLOAD_URI = "/read/download/";
    public static final String DOWNLOAD_URL = CONTROLLER_URL + _DOWNLOAD_URI;

    @RequestMapping(value = _DOWNLOAD_URI + "{oid}", method = RequestMethod.GET)
    public void processReadToDownloadFile(@PathVariable("oid") SibsInputFile sibsInputFile, Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) {
        setSibsInputFile(sibsInputFile, model);
        try {
            assertUserIsFrontOfficeMember(sibsInputFile.getFinantialInstitution(), model);

            response.setContentType(sibsInputFile.getContentType());
            String filename = sibsInputFile.getFilename();
            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            response.getOutputStream().write(sibsInputFile.getContent());
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            try {
                response.sendRedirect(redirect(READ_URL + getSibsInputFile(model).getExternalId(), model, redirectAttributes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //TODOJN - how to handle this exception
    private byte[] getContent(MultipartFile requestFile) {
        try {
            return requestFile.getBytes();
        } catch (IOException e) {
            return null;
        }
    }

    private static final String _UPDATE_URI = "/update/";
    public static final String UPDATE_URL = CONTROLLER_URL + _UPDATE_URI;

    @RequestMapping(value = _UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") SibsInputFile sibsInputFile, Model model) {
        setSibsInputFile(sibsInputFile, model);
        return "treasury/administration/payments/sibs/managesibsinputfile/sibsinputfile/update";
    }

    @RequestMapping(value = _UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") SibsInputFile sibsInputFile, @RequestParam(value = "whenprocessedbysibs",
            required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ") DateTime whenProcessedBySibs,
            @RequestParam(value = "transactionstotalamount", required = false) BigDecimal transactionsTotalAmount, @RequestParam(
                    value = "totalcost", required = false) BigDecimal totalCost, Model model,
            RedirectAttributes redirectAttributes) {

        setSibsInputFile(sibsInputFile, model);

        try {
            assertUserIsFrontOfficeMember(sibsInputFile.getFinantialInstitution(), model);

            updateSibsInputFile(whenProcessedBySibs, transactionsTotalAmount, totalCost, model);

            return redirect(READ_URL + getSibsInputFile(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tex) {
            addErrorMessage(Constants.treasuryBundle("label.error.update") + tex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(Constants.treasuryBundle("label.error.update") + ex.getLocalizedMessage(), model);
        }
        return update(sibsInputFile, model);
    }

    @Atomic
    public void updateSibsInputFile(org.joda.time.DateTime whenProcessedBySibs, java.math.BigDecimal transactionsTotalAmount,
            java.math.BigDecimal totalCost, Model model) {
        getSibsInputFile(model).setWhenProcessedBySibs(whenProcessedBySibs);
    }
}
