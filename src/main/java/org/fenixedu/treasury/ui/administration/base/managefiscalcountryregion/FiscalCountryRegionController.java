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
package org.fenixedu.treasury.ui.administration.base.managefiscalcountryregion;

import static org.fenixedu.treasury.util.Constants.treasuryBundle;

import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.FiscalCountryRegion;
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

//@Component("org.fenixedu.treasury.ui.administration.base.manageFiscalCountryRegion") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.administration.base.manageFiscalCountryRegion",
        accessGroup = "treasuryManagers")
// CHANGE_ME accessGroup = "group1 | group2 | groupXPTO"
@RequestMapping("/treasury/administration/base/managefiscalcountryregion/fiscalcountryregion")
public class FiscalCountryRegionController extends TreasuryBaseController {
    public static final String CONTROLLER_URL = "/treasury/administration/base/managefiscalcountryregion/fiscalcountryregion";
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
        return "forward:/treasury/administration/base/managefiscalcountryregion/fiscalcountryregion/";
    }

    private FiscalCountryRegion getFiscalCountryRegion(Model m) {
        return (FiscalCountryRegion) m.asMap().get("fiscalCountryRegion");
    }

    private void setFiscalCountryRegion(FiscalCountryRegion fiscalCountryRegion, Model m) {
        m.addAttribute("fiscalCountryRegion", fiscalCountryRegion);
    }

    @Atomic
    public void deleteFiscalCountryRegion(FiscalCountryRegion fiscalCountryRegion) {
        fiscalCountryRegion.delete();
    }

    @RequestMapping(value = SEARCH_URI)
    public String search(@RequestParam(value = "fiscalCode", required = false) java.lang.String fiscalCode, @RequestParam(
            value = "name", required = false) org.fenixedu.commons.i18n.LocalizedString name, Model model) {
        List<FiscalCountryRegion> searchfiscalcountryregionResultsDataSet = filterSearchFiscalCountryRegion(fiscalCode, name);

        //add the results dataSet to the model
        model.addAttribute("searchfiscalcountryregionResultsDataSet", searchfiscalcountryregionResultsDataSet);
        return "treasury/administration/base/managefiscalcountryregion/fiscalcountryregion/search";
    }

    private List<FiscalCountryRegion> getSearchUniverseSearchFiscalCountryRegionDataSet() {
        return FiscalCountryRegion.findAll().collect(Collectors.toList()); //CHANGE_ME
    }

    private List<FiscalCountryRegion> filterSearchFiscalCountryRegion(java.lang.String fiscalCode,
            org.fenixedu.commons.i18n.LocalizedString name) {

        return getSearchUniverseSearchFiscalCountryRegionDataSet()
                .stream()
                .filter(fiscalCountryRegion -> fiscalCode == null || fiscalCode.length() == 0
                        || fiscalCountryRegion.getFiscalCode() != null && fiscalCountryRegion.getFiscalCode().length() > 0
                        && fiscalCountryRegion.getFiscalCode().toLowerCase().contains(fiscalCode.toLowerCase()))
                .filter(fiscalCountryRegion -> name == null
                        || name.isEmpty()
                        || name.getLocales()
                                .stream()
                                .allMatch(
                                        locale -> fiscalCountryRegion.getName().getContent(locale) != null
                                                && fiscalCountryRegion.getName().getContent(locale).toLowerCase()
                                                        .contains(name.getContent(locale).toLowerCase())))
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/search/view/{oid}")
    public String processSearchToViewAction(@PathVariable("oid") FiscalCountryRegion fiscalCountryRegion, Model model,
            RedirectAttributes redirectAttributes) {

        return redirect("/treasury/administration/base/managefiscalcountryregion/fiscalcountryregion/read" + "/"
                + fiscalCountryRegion.getExternalId(), model, redirectAttributes);
    }

//				
    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") FiscalCountryRegion fiscalCountryRegion, Model model) {
        setFiscalCountryRegion(fiscalCountryRegion, model);
        return "treasury/administration/base/managefiscalcountryregion/fiscalcountryregion/read";
    }

    @RequestMapping(value = DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") FiscalCountryRegion fiscalCountryRegion, Model model,
            RedirectAttributes redirectAttributes) {

        setFiscalCountryRegion(fiscalCountryRegion, model);
        try {
            assertUserIsBackOfficeMember(model);

            deleteFiscalCountryRegion(fiscalCountryRegion);

            addInfoMessage(treasuryBundle("label.success.delete"), model);
            return redirect("/treasury/administration/base/managefiscalcountryregion/fiscalcountryregion/", model,
                    redirectAttributes);

        } catch (DomainException ex) {
            //Add error messages to the list
            addErrorMessage(treasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);

        } catch (Exception ex) {
            //Add error messages to the list
            addErrorMessage(treasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);
        }

        //The default mapping is the same Read View
        return redirect("/treasury/administration/base/managefiscalcountryregion/fiscalcountryregion/read/"
                + getFiscalCountryRegion(model).getExternalId(), model, redirectAttributes);
    }

//				
    @RequestMapping(value = CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {
        return "treasury/administration/base/managefiscalcountryregion/fiscalcountryregion/create";
    }

//				
    @RequestMapping(value = CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "fiscalCode", required = false) java.lang.String fiscalCode, @RequestParam(
            value = "name", required = false) org.fenixedu.commons.i18n.LocalizedString name, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            assertUserIsBackOfficeMember(model);

            FiscalCountryRegion fiscalCountryRegion = createFiscalCountryRegion(fiscalCode, name);

            model.addAttribute("fiscalCountryRegion", fiscalCountryRegion);

            return redirect("/treasury/administration/base/managefiscalcountryregion/fiscalcountryregion/read/"
                    + getFiscalCountryRegion(model).getExternalId(), model, redirectAttributes);

        } catch (DomainException tde) {

            addErrorMessage(treasuryBundle("label.error.update") + tde.getLocalizedMessage(), model);
            return create(model);

        } catch (Exception tde) {

            addErrorMessage(treasuryBundle("label.error.update") + tde.getLocalizedMessage(), model);
            return create(model);
        }
    }

    @Atomic
    public FiscalCountryRegion createFiscalCountryRegion(java.lang.String fiscalCode,
            org.fenixedu.commons.i18n.LocalizedString name) {
        FiscalCountryRegion fiscalCountryRegion = FiscalCountryRegion.create(fiscalCode, name);
        return fiscalCountryRegion;
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") FiscalCountryRegion fiscalCountryRegion, Model model) {
        setFiscalCountryRegion(fiscalCountryRegion, model);
        return "treasury/administration/base/managefiscalcountryregion/fiscalcountryregion/update";
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") FiscalCountryRegion fiscalCountryRegion, @RequestParam(value = "fiscalCode",
            required = false) java.lang.String fiscalCode,
            @RequestParam(value = "name", required = false) org.fenixedu.commons.i18n.LocalizedString name, Model model,
            RedirectAttributes redirectAttributes) {

        setFiscalCountryRegion(fiscalCountryRegion, model);

        try {
            assertUserIsBackOfficeMember(model);

            updateFiscalCountryRegion(fiscalCode, name, model);

            return redirect("/treasury/administration/base/managefiscalcountryregion/fiscalcountryregion/read/"
                    + getFiscalCountryRegion(model).getExternalId(), model, redirectAttributes);

        } catch (DomainException de) {
            addErrorMessage(treasuryBundle("label.error.update") + de.getLocalizedMessage(), model);
            return update(fiscalCountryRegion, model);

        } catch (Exception de) {
            addErrorMessage(treasuryBundle("label.error.update") + de.getLocalizedMessage(), model);
            return update(fiscalCountryRegion, model);
        }
    }

    @Atomic
    public void updateFiscalCountryRegion(java.lang.String fiscalCode, org.fenixedu.commons.i18n.LocalizedString name, Model m) {
        getFiscalCountryRegion(m).setFiscalCode(fiscalCode);
        getFiscalCountryRegion(m).setName(name);
    }

}
