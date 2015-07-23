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
package org.fenixedu.treasury.ui.administration.payments.sibs.managesibsoutputfile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.SibsOutputFile;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

//@Component("org.fenixedu.treasury.ui.administration.payments.sibs.manageSibsOutputFile") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.administration.payments.sibs.manageSibsOutputFile",
        accessGroup = "treasuryBackOffice")
@RequestMapping(SibsOutputFileController.CONTROLLER_URL)
public class SibsOutputFileController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/administration/payments/sibs/managesibsoutputfile/sibsoutputfile";

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/";
    }

    private SibsOutputFile getSibsOutputFile(Model model) {
        return (SibsOutputFile) model.asMap().get("sibsOutputFile");
    }

    private void setSibsOutputFile(SibsOutputFile sibsOutputFile, Model model) {
        model.addAttribute("sibsOutputFile", sibsOutputFile);
    }

    @Atomic
    public void deleteSibsOutputFile(SibsOutputFile sibsOutputFile) {
        sibsOutputFile.delete();
    }

    private static final String _SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI)
    public String search(@RequestParam(value = "whenprocessedbysibs", required = false) @DateTimeFormat(
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ") LocalDate whencreated, Model model) {
        List<SibsOutputFile> searchsibsoutputfileResultsDataSet = filterSearchSibsOutputFile(whencreated);

        model.addAttribute("searchsibsoutputfileResultsDataSet", searchsibsoutputfileResultsDataSet);
        return "treasury/administration/payments/sibs/managesibsoutputfile/sibsoutputfile/search";
    }

    private Stream<SibsOutputFile> getSearchUniverseSearchSibsOutputFileDataSet() {
        return SibsOutputFile.findAll();
    }

    private List<SibsOutputFile> filterSearchSibsOutputFile(org.joda.time.LocalDate whencreated) {

        return getSearchUniverseSearchSibsOutputFileDataSet().filter(
                sibsOutputFile -> whencreated == null
                        || whencreated.equals(sibsOutputFile.getVersioningCreationDate().toLocalDate())).collect(
                Collectors.toList());
    }

    private static final String _SEARCH_TO_VIEW_ACTION_URI = "/search/view/";
    public static final String SEARCH_TO_VIEW_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_VIEW_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_VIEW_ACTION_URI + "{oid}")
    public String processSearchToViewAction(@PathVariable("oid") SibsOutputFile sibsOutputFile, Model model,
            RedirectAttributes redirectAttributes) {

        return redirect(READ_URL + sibsOutputFile.getExternalId(), model, redirectAttributes);
    }

    private static final String _READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + _READ_URI;

    @RequestMapping(value = _READ_URI + "{oid}")
    public String read(@PathVariable("oid") SibsOutputFile sibsOutputFile, Model model) {
        setSibsOutputFile(sibsOutputFile, model);
        return "treasury/administration/payments/sibs/managesibsoutputfile/sibsoutputfile/read";
    }

    private static final String _DELETE_URI = "/delete/";
    public static final String DELETE_URL = CONTROLLER_URL + _DELETE_URI;

    @RequestMapping(value = _DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") SibsOutputFile sibsOutputFile, Model model, RedirectAttributes redirectAttributes) {

        setSibsOutputFile(sibsOutputFile, model);
        try {
            assertUserIsFrontOfficeMember(sibsOutputFile.getFinantialInstitution(), model);

            deleteSibsOutputFile(sibsOutputFile);

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.delete"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (TreasuryDomainException tex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + tex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + ex.getLocalizedMessage(), model);
        }

        return redirect(READ_URL + getSibsOutputFile(model).getExternalId(), model, redirectAttributes);
    }

    private static final String _DOWNLOAD_URI = "/read/download/";
    public static final String DOWNLOAD_URL = CONTROLLER_URL + _DOWNLOAD_URI;

    @RequestMapping(value = _DOWNLOAD_URI + "{oid}", method = RequestMethod.GET)
    public void processReadToDownloadFile(@PathVariable("oid") SibsOutputFile sibsOutputFile, Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) {
        setSibsOutputFile(sibsOutputFile, model);
        try {
            assertUserIsFrontOfficeMember(sibsOutputFile.getFinantialInstitution(), model);

            response.setContentType(sibsOutputFile.getContentType());
            String filename = sibsOutputFile.getFilename();
            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            response.getOutputStream().write(sibsOutputFile.getContent());
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            try {
                response.sendRedirect(redirect(READ_URL + getSibsOutputFile(model).getExternalId(), model, redirectAttributes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {
        model.addAttribute("finantialinstitution_options", FinantialInstitution.findAll().collect(Collectors.toList()));

        return "treasury/administration/payments/sibs/managesibsoutputfile/sibsoutputfile/create";
    }

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "lastsuccessfulexportation", required = true) @DateTimeFormat(
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ") DateTime lastSuccessfulExportation, @RequestParam(
            value = "finantialinstitution", required = true) FinantialInstitution finantialInstitution, Model model,
            RedirectAttributes redirectAttributes) {
        if (finantialInstitution.getSibsConfiguration() == null || finantialInstitution.getSibsConfiguration().isValid() == false) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE,
                    "error.administration.payments.sibs.managesibsoutputfile.sibsconfiguration.invalid"), model);
            return create(model);
        }

        try {
            assertUserIsFrontOfficeMember(finantialInstitution, model);

            SibsOutputFile sibsOutputFile = createSibsOutputFile(lastSuccessfulExportation, finantialInstitution);

            model.addAttribute("sibsOutputFile", sibsOutputFile);
            return redirect(READ_URL + getSibsOutputFile(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + tex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + ex.getLocalizedMessage(), model);
        }
        return create(model);
    }

    @Atomic
    public SibsOutputFile createSibsOutputFile(org.joda.time.DateTime lastSuccessfulExportation,
            FinantialInstitution finantialInstitution) {

        SibsOutputFile sibsOutputFile = SibsOutputFile.create(finantialInstitution, lastSuccessfulExportation);
        return sibsOutputFile;
    }
}
