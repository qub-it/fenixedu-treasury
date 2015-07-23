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
package org.fenixedu.treasury.ui.administration.base.manageproductgroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.ProductGroup;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
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

//@Component("org.fenixedu.treasury.ui.administration.base.manageProductGroup") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.administration.base.manageProductGroup",
        accessGroup = "treasuryManagers")
@RequestMapping(ProductGroupController.CONTROLLER_URL)
public class ProductGroupController extends TreasuryBaseController {
    public static final String CONTROLLER_URL = "/treasury/administration/base/manageproductgroup/productgroup";
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

    private ProductGroup getProductGroup(Model m) {
        return (ProductGroup) m.asMap().get("productGroup");
    }

    private void setProductGroup(ProductGroup productGroup, Model m) {
        m.addAttribute("productGroup", productGroup);
    }

    @Atomic
    public void deleteProductGroup(ProductGroup productGroup) {
        productGroup.delete();
    }

    @RequestMapping(value = SEARCH_URI)
    public String search(@RequestParam(value = "code", required = false) String code, @RequestParam(value = "name",
            required = false) LocalizedString name, Model model) {
        List<ProductGroup> searchproductgroupResultsDataSet = filterSearchProductGroup(code, name);
        model.addAttribute("searchproductgroupResultsDataSet", searchproductgroupResultsDataSet);
        return "treasury/administration/base/manageproductgroup/productgroup/search";
    }

    private List<ProductGroup> getSearchUniverseSearchProductGroupDataSet() {
        return new ArrayList<ProductGroup>(ProductGroup.readAll());
    }

    private List<ProductGroup> filterSearchProductGroup(java.lang.String code, org.fenixedu.commons.i18n.LocalizedString name) {
        return getSearchUniverseSearchProductGroupDataSet()
                .stream()
                .filter(productGroup -> code == null || code.length() == 0 || productGroup.getCode() != null
                        && productGroup.getCode().length() > 0
                        && productGroup.getCode().toLowerCase().contains(code.toLowerCase()))
                .filter(productGroup -> name == null
                        || name.isEmpty()
                        || name.getLocales()
                                .stream()
                                .allMatch(
                                        locale -> productGroup.getName().getContent(locale) != null
                                                && productGroup.getName().getContent(locale).toLowerCase()
                                                        .contains(name.getContent(locale).toLowerCase())))
                .collect(Collectors.toList());
    }

    private static final String SEARCH_VIEW_URI = "/search/view/";
    public static final String SEARCH_VIEW_URL = CONTROLLER_URL + SEARCH_VIEW_URI;

    @RequestMapping(value = SEARCH_VIEW_URI + "{oid}")
    public String processSearchToViewAction(@PathVariable("oid") ProductGroup productGroup, Model model,
            RedirectAttributes redirectAttributes) {
        return redirect(READ_URL + "/" + productGroup.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") ProductGroup productGroup, Model model) {
        setProductGroup(productGroup, model);
        return "treasury/administration/base/manageproductgroup/productgroup/read";
    }

    @RequestMapping(value = DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") ProductGroup productGroup, Model model, RedirectAttributes redirectAttributes) {
        setProductGroup(productGroup, model);
        try {
            assertUserIsFrontOfficeMember(model);

            deleteProductGroup(productGroup);

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.delete"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + ex.getLocalizedMessage(), model);
        }
        return redirect(READ_URL + getProductGroup(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {
        return "treasury/administration/base/manageproductgroup/productgroup/create";
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "code", required = false) String code, @RequestParam(value = "name",
            required = false) LocalizedString name, Model model, RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(model);

            ProductGroup productGroup = createProductGroup(code, name);
            model.addAttribute("productGroup", productGroup);

            return redirect(READ_URL + getProductGroup(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + tde.getLocalizedMessage(), model);
        } catch (Exception de) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + de.getLocalizedMessage(), model);
        }
        return create(model);
    }

    @Atomic
    public ProductGroup createProductGroup(String code, LocalizedString name) {
        ProductGroup productGroup = ProductGroup.create(code, name);
        return productGroup;
    }

//				
    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") ProductGroup productGroup, Model model) {
        setProductGroup(productGroup, model);
        return "treasury/administration/base/manageproductgroup/productgroup/update";
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") ProductGroup productGroup,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) LocalizedString name, Model model,
            RedirectAttributes redirectAttributes) {
        setProductGroup(productGroup, model);
        try {
            assertUserIsFrontOfficeMember(model);

            updateProductGroup(code, name, model);
            return redirect(READ_URL + getProductGroup(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + tde.getLocalizedMessage(), model);
            return update(productGroup, model);
        } catch (Exception de) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + de.getLocalizedMessage(), model);
            return update(productGroup, model);
        }
    }

    @Atomic
    public void updateProductGroup(java.lang.String code, org.fenixedu.commons.i18n.LocalizedString name, Model m) {
        getProductGroup(m).edit(code, name);
    }
}
