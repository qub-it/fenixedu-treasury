/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
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
package org.fenixedu.treasury.ui.document.manageexemption;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.exemption.TreasuryExemptionType;
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

//@Component("org.fenixedu.treasury.ui.document.manageExemption") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.document.manageExemptionType",
        accessGroup = "treasuryManagers")
@RequestMapping(TreasuryExemptionTypeController.CONTROLLER_URL)
public class TreasuryExemptionTypeController extends TreasuryBaseController {
    public static final String CONTROLLER_URL = "/treasury/document/manageexemption/treasuryexemptiontype";
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

    @RequestMapping
    public String home(Model model) {
        return "forward:" + SEARCH_URL;
    }

    private TreasuryExemptionType getTreasuryExemptionType(final Model model) {
        return (TreasuryExemptionType) model.asMap().get("treasuryExemptionType");
    }

    private void setTreasuryExemptionType(final TreasuryExemptionType treasuryExemptionType, final Model model) {
        model.addAttribute("treasuryExemptionType", treasuryExemptionType);
    }

    @Atomic
    public void deleteTreasuryExemptionType(TreasuryExemptionType treasuryExemptionType) {
        treasuryExemptionType.delete();
    }

    @RequestMapping(value = SEARCH_URI)
    public String search(@RequestParam(value = "code", required = false) String code, @RequestParam(value = "name",
            required = false) LocalizedString name,
            @RequestParam(value = "discountrate", required = false) BigDecimal discountRate, Model model) {
        List<TreasuryExemptionType> searchtreasuryexemptiontypeResultsDataSet =
                filterSearchTreasuryExemptionType(code, name, discountRate);
        model.addAttribute("searchtreasuryexemptiontypeResultsDataSet", searchtreasuryexemptiontypeResultsDataSet);
        return "treasury/document/manageexemption/treasuryexemptiontype/search";
    }

    private List<TreasuryExemptionType> getSearchUniverseSearchtreasuryExemptionTypeDataSet() {
        return TreasuryExemptionType.findAll().collect(Collectors.toList());
    }

    private List<TreasuryExemptionType> filterSearchTreasuryExemptionType(String code, LocalizedString name,
            BigDecimal discountRate) {

        return getSearchUniverseSearchtreasuryExemptionTypeDataSet()
                .stream()
                .filter(treasuryExemptionType -> code == null || code.length() == 0 || treasuryExemptionType.getCode() != null
                        && treasuryExemptionType.getCode().length() > 0
                        && treasuryExemptionType.getCode().toLowerCase().contains(code.toLowerCase()))
                .filter(treasuryExemptionType -> name == null
                        || name.isEmpty()
                        || name.getLocales()
                                .stream()
                                .allMatch(
                                        locale -> treasuryExemptionType.getName().getContent(locale) != null
                                                && treasuryExemptionType.getName().getContent(locale).toLowerCase()
                                                        .contains(name.getContent(locale).toLowerCase())))
                .filter(treasuryExemptionType -> discountRate == null
                        || discountRate.equals(treasuryExemptionType.getDefaultExemptionPercentage()))
                .collect(Collectors.toList());
    }

    private static final String SEARCH_VIEW_URI = "/search/view/";
    public static final String SEARCH_VIEW_URL = CONTROLLER_URL + SEARCH_VIEW_URI;

    @RequestMapping(value = SEARCH_VIEW_URI + "{oid}")
    public String processSearchToViewAction(@PathVariable("oid") TreasuryExemptionType treasuryExemptionType, Model model,
            RedirectAttributes redirectAttributes) {
        return redirect(READ_URL + treasuryExemptionType.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") TreasuryExemptionType treasuryExemptionType, Model model) {
        setTreasuryExemptionType(treasuryExemptionType, model);
        return "treasury/document/manageexemption/treasuryexemptiontype/read";
    }

    @RequestMapping(value = DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") TreasuryExemptionType treasuryExemptionType, Model model,
            RedirectAttributes redirectAttributes) {
        setTreasuryExemptionType(treasuryExemptionType, model);
        try {
            assertUserIsFrontOfficeMember(model);
            deleteTreasuryExemptionType(treasuryExemptionType);

            addInfoMessage(Constants.treasuryBundle("label.success.delete"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(Constants.treasuryBundle("label.error.delete") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(Constants.treasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);
        }
        return redirect(READ_URL + getTreasuryExemptionType(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {
        return "treasury/document/manageexemption/treasuryexemptiontype/create";
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "code", required = false) String code, @RequestParam(value = "name",
            required = false) LocalizedString name,
            @RequestParam(value = "defaultexemptionpercentage", required = false) BigDecimal defaultExemptionPercentage,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            TreasuryExemptionType treasuryExemptionType =
                    TreasuryExemptionType.create(code, name, defaultExemptionPercentage, true);

            model.addAttribute("treasuryExemptionType", treasuryExemptionType);

            return redirect(READ_URL + getTreasuryExemptionType(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(Constants.treasuryBundle("label.error.create") + tde.getLocalizedMessage(), model);
        } catch (Exception de) {
            addErrorMessage(Constants.treasuryBundle("label.error.create") + de.getLocalizedMessage(), model);
        }
        return create(model);
    }

    @Atomic
    public TreasuryExemptionType createTreasuryExemptionType(String code, LocalizedString name, BigDecimal discountRate) {
        TreasuryExemptionType treasuryExemptionType = TreasuryExemptionType.create(code, name, discountRate, true);
        return treasuryExemptionType;
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") TreasuryExemptionType treasuryExemptionType, Model model) {
        setTreasuryExemptionType(treasuryExemptionType, model);
        return "treasury/document/manageexemption/treasuryexemptiontype/update";
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") TreasuryExemptionType treasuryExemptionType, @RequestParam(value = "code",
            required = false) String code, @RequestParam(value = "name", required = false) LocalizedString name, @RequestParam(
            value = "defaultexemptionpercentage", required = false) BigDecimal defaultExemptionPercentage, Model model,
            RedirectAttributes redirectAttributes) {

        setTreasuryExemptionType(treasuryExemptionType, model);

        try {
            getTreasuryExemptionType(model).edit(code, name, defaultExemptionPercentage, true);

            return redirect(READ_URL + getTreasuryExemptionType(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(Constants.treasuryBundle("label.error.update") + tde.getLocalizedMessage(), model);
        } catch (Exception de) {
            addErrorMessage(Constants.treasuryBundle("label.error.update") + de.getLocalizedMessage(), model);
        }
        return update(treasuryExemptionType, model);
    }

    @Atomic
    public void updateTreasuryExemptionType(String code, LocalizedString name, BigDecimal discountRate, Model m) {
        getTreasuryExemptionType(m).edit(code, name, discountRate, true);
    }
}
