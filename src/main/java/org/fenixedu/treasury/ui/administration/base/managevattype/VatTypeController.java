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
package org.fenixedu.treasury.ui.administration.base.managevattype;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.VatType;
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

//@Component("org.fenixedu.treasury.ui.administration.base.manageVatType") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.administration.base.manageVatType",
        accessGroup = "treasuryManagers")
// CHANGE_ME accessGroup = "group1 | group2 | groupXPTO"
@RequestMapping(VatTypeController.CONTROLLER_URL)
public class VatTypeController extends TreasuryBaseController {
    public static final String CONTROLLER_URL = "/treasury/administration/base/managevattype/vattype";
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
        // this is the default behaviour, for handling in a Spring Functionality
        return "forward:/treasury/administration/base/managevattype/vattype/";
    }

    private VatType getVatType(Model m) {
        return (VatType) m.asMap().get("vatType");
    }

    private void setVatType(VatType vatType, Model m) {
        m.addAttribute("vatType", vatType);
    }

    @Atomic
    public void deleteVatType(VatType vatType) {
        // Do not catch any exception here

        vatType.delete();
    }

    //
    @RequestMapping(value = SEARCH_URI)
    public String search(@RequestParam(value = "code", required = false) java.lang.String code, @RequestParam(value = "name",
            required = false) org.fenixedu.commons.i18n.LocalizedString name, Model model) {
        List<VatType> searchvattypeResultsDataSet = filterSearchVatType(code, name);

        // add the results dataSet to the model
        model.addAttribute("searchvattypeResultsDataSet", searchvattypeResultsDataSet);
        return "treasury/administration/base/managevattype/vattype/search";
    }

    private Stream<VatType> getSearchUniverseSearchVatTypeDataSet() {
        return VatType.findAll();
    }

    private List<VatType> filterSearchVatType(java.lang.String code, org.fenixedu.commons.i18n.LocalizedString name) {

        return getSearchUniverseSearchVatTypeDataSet()
                .filter(vatType -> code == null || code.length() == 0 || vatType.getCode() != null
                        && vatType.getCode().length() > 0 && vatType.getCode().toLowerCase().contains(code.toLowerCase()))
                .filter(vatType -> name == null
                        || name.isEmpty()
                        || name.getLocales()
                                .stream()
                                .allMatch(
                                        locale -> vatType.getName().getContent(locale) != null
                                                && vatType.getName().getContent(locale).toLowerCase()
                                                        .contains(name.getContent(locale).toLowerCase())))
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/search/view/{oid}")
    public String processSearchToViewAction(@PathVariable("oid") VatType vatType, Model model,
            RedirectAttributes redirectAttributes) {
        return redirect("/treasury/administration/base/managevattype/vattype/read" + "/" + vatType.getExternalId(), model,
                redirectAttributes);
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") VatType vatType, Model model) {
        setVatType(vatType, model);
        return "treasury/administration/base/managevattype/vattype/read";
    }

    @RequestMapping(value = DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") VatType vatType, Model model, RedirectAttributes redirectAttributes) {

        setVatType(vatType, model);
        try {
            assertUserIsFrontOfficeMember(model);

            deleteVatType(vatType);

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.delete"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);

        } catch (DomainException ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + ex.getLocalizedMessage(), model);

        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + ex.getLocalizedMessage(), model);
        }

        return redirect(READ_URL + getVatType(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {
        return "treasury/administration/base/managevattype/vattype/create";
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "code", required = false) java.lang.String code, @RequestParam(value = "name",
            required = false) org.fenixedu.commons.i18n.LocalizedString name, Model model, RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(model);

            VatType vatType = createVatType(code, name);

            model.addAttribute("vatType", vatType);

            return redirect("/treasury/administration/base/managevattype/vattype/read/" + getVatType(model).getExternalId(),
                    model, redirectAttributes);

        } catch (DomainException de) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + de.getLocalizedMessage(), model);
            return create(model);

        } catch (Exception de) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + de.getLocalizedMessage(), model);
            return create(model);
        }
    }

    @Atomic
    public VatType createVatType(java.lang.String code, org.fenixedu.commons.i18n.LocalizedString name) {
        VatType vatType = VatType.create(code, name);
        return vatType;
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") VatType vatType, Model model) {
        setVatType(vatType, model);
        return "treasury/administration/base/managevattype/vattype/update";
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") VatType vatType,
            @RequestParam(value = "code", required = false) java.lang.String code,
            @RequestParam(value = "name", required = false) org.fenixedu.commons.i18n.LocalizedString name, Model model,
            RedirectAttributes redirectAttributes) {

        setVatType(vatType, model);

        try {
            assertUserIsFrontOfficeMember(model);

            updateVatType(code, name, model);

            return redirect("/treasury/administration/base/managevattype/vattype/read/" + getVatType(model).getExternalId(),
                    model, redirectAttributes);

        } catch (DomainException de) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + de.getLocalizedMessage(), model);
            return update(vatType, model);

        } catch (Exception de) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + de.getLocalizedMessage(), model);
            return update(vatType, model);

        }
    }

    @Atomic
    public void updateVatType(java.lang.String code, org.fenixedu.commons.i18n.LocalizedString name, Model m) {
        getVatType(m).setCode(code);
        getVatType(m).setName(name);
    }

}
