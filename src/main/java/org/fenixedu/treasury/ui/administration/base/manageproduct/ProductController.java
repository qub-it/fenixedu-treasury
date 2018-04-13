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
package org.fenixedu.treasury.ui.administration.base.manageproduct;

import static org.fenixedu.treasury.util.Constants.treasuryBundle;

import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.ProductGroup;
import org.fenixedu.treasury.domain.VatExemptionReason;
import org.fenixedu.treasury.domain.VatType;
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

//@Component("org.fenixedu.treasury.ui.administration.base.manageProduct") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.administration.base.manageProduct",
        accessGroup = "treasuryManagers")
@RequestMapping(ProductController.CONTROLLER_URL)
public class ProductController extends TreasuryBaseController {
    public static final String CONTROLLER_URL = "/treasury/administration/base/manageproduct/product";
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

    private Product getProduct(Model m) {
        return (Product) m.asMap().get("product");
    }

    private void setProduct(Product product, Model m) {
        m.addAttribute("product", product);
    }

    @Atomic
    public void deleteProduct(Product product) {
        product.delete();
    }

    @RequestMapping(value = SEARCH_URI)
    public String search(@RequestParam(value = "productgroup", required = false) ProductGroup productGroup, Model model) {
        List<Product> searchproductResultsDataSet = filterSearchProduct(productGroup);

        model.addAttribute("searchproductResultsDataSet", searchproductResultsDataSet);
        model.addAttribute("Product_productGroup_options", ProductGroup.readAll());
        return "treasury/administration/base/manageproduct/product/search";
    }

    private List<Product> getSearchUniverseSearchProductDataSet() {
        return Product.findAll().sorted((x, y) -> x.getName().getContent().compareTo(y.getName().getContent()))
                .collect(Collectors.toList());
    }

    private List<Product> filterSearchProduct(ProductGroup productGroup) {
        return getSearchUniverseSearchProductDataSet().stream()
                .filter(product -> productGroup == null || productGroup == product.getProductGroup())
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/search/view/{oid}")
    public String processSearchToViewAction(@PathVariable("oid") Product product, Model model,
            RedirectAttributes redirectAttributes) {
        return redirect(READ_URL + product.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") Product product, Model model) {
        setProduct(product, model);
        return "treasury/administration/base/manageproduct/product/read";
    }

    @RequestMapping(value = DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") Product product, Model model, RedirectAttributes redirectAttributes) {
        setProduct(product, model);
        try {
            assertUserIsBackOfficeMember(model);

            deleteProduct(product);

            addInfoMessage(treasuryBundle("label.success.delete"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (DomainException ex) {
            addErrorMessage(treasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(treasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);
        }
        return redirect(READ_URL + getProduct(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {
        model.addAttribute("productGroupList", ProductGroup.readAll());
        model.addAttribute("vattype_options", VatType.findAll().collect(Collectors.toList()));
        model.addAttribute("finantial_institutions_options", FinantialInstitution.findAll().collect(Collectors.toList()));
        model.addAttribute("vatExemptionReasonList",
                VatExemptionReason.findAll().sorted((x, y) -> (x.getCode().compareTo(y.getCode()))).collect(Collectors.toList()));
        return "treasury/administration/base/manageproduct/product/create";
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "productGroup", required = false) ProductGroup productGroup,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) LocalizedString name,
            @RequestParam(value = "unitofmeasure", required = false) LocalizedString unitOfMeasure,
            @RequestParam(value = "active", required = false) boolean active,
            @RequestParam(value = "legacy", required = false) boolean legacy,
            @RequestParam(value = "tuitionInstallmentOrder", required = false) int tuitionInstallmentOrder,
            @RequestParam(value = "vatExemptionReason", required = false) VatExemptionReason vatExemptionReason,
            @RequestParam(value = "vattype", required = false) VatType vatType,
            @RequestParam(value = "finantialInstitution", required = false) List<FinantialInstitution> finantialInstitutions,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            assertUserIsBackOfficeMember(model);

            Product product = createProduct(productGroup, code, name, unitOfMeasure, active, legacy, tuitionInstallmentOrder,
                    vatType, finantialInstitutions, vatExemptionReason);

            model.addAttribute("product", product);
            return redirect(READ_URL + getProduct(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(tde.getLocalizedMessage(), model);
        } catch (Exception tde) {
            addErrorMessage(tde.getLocalizedMessage(), model);
        }

        return create(model);
    }

    @Atomic
    public Product createProduct(ProductGroup productGroup, String code, LocalizedString name, LocalizedString unitOfMeasure,
            boolean active, final boolean legacy, final int tuitionInstallmentOrder, VatType vatType,
            List<FinantialInstitution> finantialInstitutions, VatExemptionReason vatExemptionReason) {
        Product product = Product.create(productGroup, code, name, unitOfMeasure, active, legacy, tuitionInstallmentOrder,
                vatType, finantialInstitutions, vatExemptionReason);
        return product;
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") Product product, Model model) {
        setProduct(product, model);
        model.addAttribute("productGroupList", ProductGroup.readAll());
        model.addAttribute("finantial_institutions_options", FinantialInstitution.findAll().collect(Collectors.toList()));
        model.addAttribute("vattype_options", VatType.findAll().collect(Collectors.toList()));
        model.addAttribute("vatExemptionReasonList",
                VatExemptionReason.findAll().sorted((x, y) -> (x.getCode().compareTo(y.getCode()))).collect(Collectors.toList()));
        return "treasury/administration/base/manageproduct/product/update";
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@RequestParam(value = "productGroup", required = false) ProductGroup productGroup,
            @PathVariable("oid") Product product, @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) LocalizedString name,
            @RequestParam(value = "unitofmeasure", required = false) LocalizedString unitOfMeasure,
            @RequestParam(value = "vatExemptionReason", required = false) VatExemptionReason vatExemptionReason,
            @RequestParam(value = "active", required = false) boolean active,
            @RequestParam(value = "legacy", required = false) boolean legacy,
            @RequestParam(value = "tuitionInstallmentOrder", required=false) int tuitionInstallmentOrder,
            @RequestParam(value = "vatType", required = false) VatType vatType,
            @RequestParam(value = "finantialInstitution", required = false) List<FinantialInstitution> finantialInstitutions,
            Model model, RedirectAttributes redirectAttributes) {
        setProduct(product, model);
        try {
            assertUserIsBackOfficeMember(model);

            updateProduct(productGroup, code, name, unitOfMeasure, active, legacy, tuitionInstallmentOrder, finantialInstitutions, vatType,
                    vatExemptionReason, model);

            return redirect(READ_URL + getProduct(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(treasuryBundle("label.error.update") + tde.getLocalizedMessage(), model);
        } catch (Exception de) {
            addErrorMessage(treasuryBundle("label.error.update") + de.getLocalizedMessage(), model);
        }
        return update(product, model);
    }

    @RequestMapping(value = "/deleteorphanproducts", method = RequestMethod.POST)
    public String processSearchToDeleteOrphanProducts(Model model, RedirectAttributes redirectAttributes) {
        try {
            assertUserIsBackOfficeMember(model);

            int deletedCount = Product.deleteOrphanProducts();

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE,
                    "label.info.administration.base.manageproduct.product.deleteorphanproducts") + deletedCount, model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (Exception de) {
            addErrorMessage(treasuryBundle("label.error.deleteorphanproducts") + de.getLocalizedMessage(),
                    model);
        }
        return redirect(SEARCH_URL, model, redirectAttributes);
    }

    @Atomic
    public void updateProduct(ProductGroup productGroup, String code, LocalizedString name, LocalizedString unitOfMeasure,
            boolean active, boolean legacy, final int tuitionInstallmentOrder, List<FinantialInstitution> finantialInstitutions, VatType vatType,
            VatExemptionReason vatExemptionReason, Model m) {
        getProduct(m).edit(code, name, unitOfMeasure, active, legacy, tuitionInstallmentOrder, vatType, productGroup, finantialInstitutions,
                vatExemptionReason);
    }

}
