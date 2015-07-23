/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and ServiÃ§os Partilhados da
 * Universidade de Lisboa:
 *  - Copyright Â© 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright Â© 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: ricardo.pedro@qub-it.com
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
package org.fenixedu.treasury.ui.administration.managefinantialinstitution;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.commons.StringNormalizer;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.document.TreasuryDocumentTemplate;
import org.fenixedu.treasury.domain.document.TreasuryDocumentTemplateFile;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.util.Constants;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

@BennuSpringController(value = FinantialInstitutionController.class)
@RequestMapping(TreasuryDocumentTemplateController.CONTROLLER_URL)
public class TreasuryDocumentTemplateController extends TreasuryBaseController {
    public static final String CONTROLLER_URL = "/treasury/administration/managefinantialinstitution/treasurydocumenttemplate";
    private static final String READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + READ_URI;
    private static final String DELETE_URI = "/delete/";
    public static final String DELETE_URL = CONTROLLER_URL + DELETE_URI;

    @RequestMapping
    public String home(Model model) {
        if (model.containsAttribute("finantialInstitutionId")) {
            return "forward:" + FinantialInstitutionController.READ_URL + model.asMap().get("finantialInstitutionId");
        }
        return "forward:" + FinantialInstitutionController.SEARCH_URL;
    }

    private TreasuryDocumentTemplate getDocumentTemplate(Model model) {
        return (TreasuryDocumentTemplate) model.asMap().get("documentTemplate");
    }

    private void setDocumentTemplate(TreasuryDocumentTemplate documentTemplate, Model model) {
        model.addAttribute("documentTemplate", documentTemplate);
    }

    @Atomic
    public void deleteDocumentTemplate(TreasuryDocumentTemplate documentTemplate) {
        documentTemplate.delete();
    }

    @RequestMapping(value = "/search/view/{oid}")
    public String processSearchToViewAction(@PathVariable("oid") TreasuryDocumentTemplate documentTemplate, Model model,
            RedirectAttributes redirectAttributes) {
        return redirect(READ_URL + documentTemplate.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") TreasuryDocumentTemplate documentTemplate, Model model) {
        setDocumentTemplate(documentTemplate, model);
        return "treasury/administration/managefinantialinstitution/treasurydocumenttemplate/read";
    }

    @RequestMapping(value = DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") TreasuryDocumentTemplate documentTemplate, Model model,
            RedirectAttributes redirectAttributes) {
        setDocumentTemplate(documentTemplate, model);
        try {
            FinantialInstitution finantialInstitution = documentTemplate.getFinantialEntity().getFinantialInstitution();

            assertUserIsFrontOfficeMember(finantialInstitution, model);

            deleteDocumentTemplate(documentTemplate);
            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.delete"), model);
            return redirect(FinantialInstitutionController.READ_URL + finantialInstitution.getExternalId(), model,
                    redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + ex.getLocalizedMessage(), model);
        }
        return redirect(READ_URL + getDocumentTemplate(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = "/create")
    public String create(
            @RequestParam(value = "finantialdocumenttypeid", required = true) FinantialDocumentType finantialDocumentTypes,
            @RequestParam(value = "finantialentityid", required = true) FinantialEntity finantialEntity, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(finantialEntity.getFinantialInstitution(), model);

            TreasuryDocumentTemplate documentTemplate = createDocumentTemplate(finantialDocumentTypes, finantialEntity);
            model.addAttribute("documentTemplate", documentTemplate);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + ex.getLocalizedMessage(), model);
        }
        return redirect(FinantialInstitutionController.READ_URL + finantialEntity.getFinantialInstitution().getExternalId(),
                model, redirectAttributes);
    }

    @Atomic
    public TreasuryDocumentTemplate createDocumentTemplate(FinantialDocumentType finantialDocumentTypes,
            FinantialEntity finantialEntity) {
        return TreasuryDocumentTemplate.create(finantialDocumentTypes, finantialEntity);
    }

    private static final String SEARCH_UPLOAD_URI = "/search/upload/";
    public static final String SEARCH_UPLOAD_URL = CONTROLLER_URL + SEARCH_UPLOAD_URI;

    @RequestMapping(value = SEARCH_UPLOAD_URI + "{oid}", method = RequestMethod.POST)
    public String processSearchToUploadAction(@PathVariable("oid") TreasuryDocumentTemplate documentTemplate, @RequestParam(
            value = "documentTemplateFile", required = true) MultipartFile documentTemplateFile, Model model,
            RedirectAttributes redirectAttributes) {
        setDocumentTemplate(documentTemplate, model);
        try {
            assertUserIsFrontOfficeMember(documentTemplate.getFinantialEntity().getFinantialInstitution(), model);

            uploadDocumentTemplateFile(documentTemplate, documentTemplateFile, model);
            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.upload"), model);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.upload") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.upload") + ex.getLocalizedMessage(), model);
        }
        return redirect(READ_URL + getDocumentTemplate(model).getExternalId(), model, redirectAttributes);
    }

    public void uploadDocumentTemplateFile(TreasuryDocumentTemplate documentTemplate, MultipartFile requestFile, Model model) {
        if (!requestFile.getOriginalFilename().endsWith(TreasuryDocumentTemplateFile.FILE_EXTENSION)) {
            throw new TreasuryDomainException("error.file.different.content.type");
        }

        documentTemplate.addFile(documentTemplate, requestFile.getName(), requestFile.getOriginalFilename(),
                getContent(requestFile));
    }

    //TODOJN - how to handle this exception
    private byte[] getContent(MultipartFile requestFile) {
        try {
            return requestFile.getBytes();
        } catch (IOException e) {
            return null;
        }
    }

    @RequestMapping(value = "/search/download/{documentTemplateFileId}", method = RequestMethod.GET)
    public void processSearchToDownloadAction(
            @PathVariable("documentTemplateFileId") TreasuryDocumentTemplateFile documentTemplateFile,
            HttpServletResponse response) {
        try {
            response.setContentType(documentTemplateFile.getContentType());
            String filename =
                    URLEncoder.encode(StringNormalizer.normalizePreservingCapitalizedLetters(documentTemplateFile.getFilename())
                            .replaceAll("\\s", "_"), "UTF-8");
            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            response.getOutputStream().write(documentTemplateFile.getContent());
        } catch (IOException e) {
            // TODOJN
            throw new RuntimeException(e);
        }
    }
}
