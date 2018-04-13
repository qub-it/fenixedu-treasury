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
package org.fenixedu.treasury.ui.administration.document.managefinantialdocumenttype;

import static org.fenixedu.treasury.util.Constants.treasuryBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.util.Constants;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

//@Component("org.fenixedu.treasury.ui.administration.document.manageFinantialDocumentType") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.administration.document.manageFinantialDocumentType",
        accessGroup = "treasuryManagers")
// CHANGE_ME accessGroup = "group1 | group2 | groupXPTO"
//or
//@BennuSpringController(value=TreasuryController.class) 
@RequestMapping(FinantialDocumentTypeController.CONTROLLER_URL)
public class FinantialDocumentTypeController extends TreasuryBaseController {
    public static final String CONTROLLER_URL =
            "/treasury/administration/document/managefinantialdocumenttype/finantialdocumenttype";
    private static final String SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + SEARCH_URI;
    private static final String UPDATE_URI = "/update/";
    public static final String UPDATE_URL = CONTROLLER_URL + UPDATE_URI;
    private static final String CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + CREATE_URI;
    private static final String READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + READ_URI;
    private static final String DELETE_URI = "/delete/";
    public static final String DELETE_URL = CONTROLLER_URL + DELETE_URI;

//

    @RequestMapping
    public String home(Model model) {
        //this is the default behaviour, for handling in a Spring Functionality
        return "forward:" + SEARCH_URL;
    }

    private FinantialDocumentType getFinantialDocumentType(Model model) {
        return (FinantialDocumentType) model.asMap().get("finantialDocumentType");
    }

    private void setFinantialDocumentType(FinantialDocumentType finantialDocumentType, Model model) {
        model.addAttribute("finantialDocumentType", finantialDocumentType);
    }

    @Atomic
    public void deleteFinantialDocumentType(FinantialDocumentType finantialDocumentType) {
        finantialDocumentType.delete();
    }

    @RequestMapping(value = SEARCH_URI)
    public String search(
            @RequestParam(value = "type", required = false) org.fenixedu.treasury.domain.document.FinantialDocumentTypeEnum type,
            @RequestParam(value = "code", required = false) java.lang.String code,
            @RequestParam(value = "name", required = false) org.fenixedu.commons.i18n.LocalizedString name, @RequestParam(
                    value = "documentnumberseriesprefix", required = false) java.lang.String documentNumberSeriesPrefix,
            @RequestParam(value = "invoice", required = false) boolean invoice, Model model) {
        List<FinantialDocumentType> searchfinantialdocumenttypeResultsDataSet =
                filterSearchFinantialDocumentType(type, code, name, documentNumberSeriesPrefix, invoice);

        //add the results dataSet to the model
        model.addAttribute("searchfinantialdocumenttypeResultsDataSet", searchfinantialdocumenttypeResultsDataSet);
        model.addAttribute("typeValues", org.fenixedu.treasury.domain.document.FinantialDocumentTypeEnum.values());
        return "treasury/administration/document/managefinantialdocumenttype/finantialdocumenttype/search";
    }

    private List<FinantialDocumentType> getSearchUniverseSearchFinantialDocumentTypeDataSet() {
        return FinantialDocumentType.findAll().collect(Collectors.toList());
    }

    private List<FinantialDocumentType> filterSearchFinantialDocumentType(
            org.fenixedu.treasury.domain.document.FinantialDocumentTypeEnum type, java.lang.String code,
            org.fenixedu.commons.i18n.LocalizedString name, java.lang.String documentNumberSeriesPrefix, boolean invoice) {

        return getSearchUniverseSearchFinantialDocumentTypeDataSet().stream().collect(Collectors.toList());
    }

    @RequestMapping(value = "/search/view/{oid}")
    public String processSearchToViewAction(@PathVariable("oid") FinantialDocumentType finantialDocumentType, Model model,
            RedirectAttributes redirectAttributes) {
        return redirect(READ_URL + finantialDocumentType.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") FinantialDocumentType finantialDocumentType, Model model) {
        setFinantialDocumentType(finantialDocumentType, model);
        return "treasury/administration/document/managefinantialdocumenttype/finantialdocumenttype/read";
    }

    @RequestMapping(value = DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") FinantialDocumentType finantialDocumentType, Model model,
            RedirectAttributes redirectAttributes) {

        setFinantialDocumentType(finantialDocumentType, model);
        try {
            assertUserIsBackOfficeMember(model);

            deleteFinantialDocumentType(finantialDocumentType);

            addInfoMessage(treasuryBundle("label.success.delete"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);

        } catch (DomainException ex) {
            addErrorMessage(treasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);

        } catch (Exception ex) {
            addErrorMessage(treasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);
        }

        return redirect(READ_URL + getFinantialDocumentType(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {
        model.addAttribute("typeValues", org.fenixedu.treasury.domain.document.FinantialDocumentTypeEnum.values());
        model.addAttribute("FinantialDocumentType_bennu_options", new ArrayList<org.fenixedu.bennu.core.domain.Bennu>()); // CHANGE_ME - MUST DEFINE RELATION
        return "treasury/administration/document/managefinantialdocumenttype/finantialdocumenttype/create";
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.POST)
    public String create(
            @RequestParam(value = "type", required = false) org.fenixedu.treasury.domain.document.FinantialDocumentTypeEnum type,
            @RequestParam(value = "code", required = false) java.lang.String code,
            @RequestParam(value = "name", required = false) org.fenixedu.commons.i18n.LocalizedString name, @RequestParam(
                    value = "documentnumberseriesprefix", required = false) java.lang.String documentNumberSeriesPrefix,
            @RequestParam(value = "invoice", required = false) boolean invoice,
            @RequestParam(value = "bennu", required = false) org.fenixedu.bennu.core.domain.Bennu bennu, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            assertUserIsBackOfficeMember(model);

            FinantialDocumentType finantialDocumentType =
                    createFinantialDocumentType(type, code, name, documentNumberSeriesPrefix, invoice, bennu);

            model.addAttribute("finantialDocumentType", finantialDocumentType);
            return redirect(READ_URL + getFinantialDocumentType(model).getExternalId(), model, redirectAttributes);

        } catch (DomainException de) {
            addErrorMessage(treasuryBundle("label.error.create") + de.getLocalizedMessage(), model);

        } catch (Exception de) {
            addErrorMessage(treasuryBundle("label.error.create") + de.getLocalizedMessage(), model);
        }
        return create(model);
    }

    @Atomic
    public FinantialDocumentType createFinantialDocumentType(
            org.fenixedu.treasury.domain.document.FinantialDocumentTypeEnum type, java.lang.String code,
            org.fenixedu.commons.i18n.LocalizedString name, java.lang.String documentNumberSeriesPrefix, boolean invoice,
            org.fenixedu.bennu.core.domain.Bennu bennu) {

        FinantialDocumentType finantialDocumentType = null;
        switch (type) {
        case CREDIT_NOTE:
            finantialDocumentType = FinantialDocumentType.createForCreditNote(code, name, documentNumberSeriesPrefix, invoice);
            break;
        case REIMBURSEMENT_NOTE:
            finantialDocumentType =
                    FinantialDocumentType.createForReimbursementNote(code, name, documentNumberSeriesPrefix, invoice);
            break;
        case DEBIT_NOTE:
            finantialDocumentType = FinantialDocumentType.createForDebitNote(code, name, documentNumberSeriesPrefix, invoice);
            break;
        case SETTLEMENT_NOTE:
            finantialDocumentType =
                    FinantialDocumentType.createForSettlementNote(code, name, documentNumberSeriesPrefix, invoice);
            break;
        }
        return finantialDocumentType;
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") FinantialDocumentType finantialDocumentType, Model model) {
        setFinantialDocumentType(finantialDocumentType, model);
        return "treasury/administration/document/managefinantialdocumenttype/finantialdocumenttype/update";
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") FinantialDocumentType finantialDocumentType, @RequestParam(value = "code",
            required = false) java.lang.String code,
            @RequestParam(value = "name", required = false) org.fenixedu.commons.i18n.LocalizedString name, Model model,
            RedirectAttributes redirectAttributes) {

        setFinantialDocumentType(finantialDocumentType, model);

        try {
            assertUserIsBackOfficeMember(model);

            updateFinantialDocumentType(code, name, model);

            return redirect(READ_URL + getFinantialDocumentType(model).getExternalId(), model, redirectAttributes);

        } catch (DomainException de) {
            addErrorMessage(treasuryBundle("label.error.update") + de.getLocalizedMessage(), model);
        } catch (Exception de) {
            addErrorMessage(treasuryBundle("label.error.update") + de.getLocalizedMessage(), model);
        }
        return update(finantialDocumentType, model);
    }

    @Atomic
    public void updateFinantialDocumentType(String code, LocalizedString name, Model model) {

        getFinantialDocumentType(model).edit(code, name);
    }

}
