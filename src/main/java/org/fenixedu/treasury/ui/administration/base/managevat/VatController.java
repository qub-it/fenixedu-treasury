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
package org.fenixedu.treasury.ui.administration.base.managevat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.Vat;
import org.fenixedu.treasury.domain.VatExemptionReason;
import org.fenixedu.treasury.domain.VatType;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

//@Component("org.fenixedu.treasury.ui.administration.base.manageVat") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.administration.base.manageVat",
        accessGroup = "treasuryManagers")
// CHANGE_ME accessGroup = "group1 | group2 | groupXPTO"
@RequestMapping(VatController.CONTROLLER_URL)
public class VatController extends TreasuryBaseController {
    public static final String CONTROLLER_URL = "/treasury/administration/base/managevat/vat";
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
        return "forward:/treasury/administration/base/managevat/vat/";
    }

    private Vat getVat(Model m) {
        return (Vat) m.asMap().get("vat");
    }

    private void setVat(Vat vat, Model m) {
        m.addAttribute("vat", vat);
    }

    @Atomic
    public void deleteVat(Vat vat) {
        // CHANGE_ME: Do the processing for deleting the vat
        // Do not catch any exception here

        vat.delete();
    }

//				
    @RequestMapping(value = SEARCH_URI)
    public String search(@RequestParam(value = "vatType", required = false) VatType vatType, @RequestParam(
            value = "finantialInstitution", required = false) FinantialInstitution finantialInstitution, @RequestParam(
            value = "onlyActive", required = false) Boolean onlyActive, Model model) {
        List<Vat> searchvatResultsDataSet = filterSearchVat(vatType, finantialInstitution, onlyActive);
        model.addAttribute("vatTypeList", VatType.findAll().collect(Collectors.toList()));
        model.addAttribute("finantialInstitutionList", FinantialInstitution.findAll().collect(Collectors.toList()));

        checkVatRules(model);

        //add the results dataSet to the model
        model.addAttribute("searchvatResultsDataSet", searchvatResultsDataSet);
        return "treasury/administration/base/managevat/vat/search";
    }

    private void checkVatRules(Model model) {

        FinantialInstitution.findAll().forEach(
                inst -> {
                    VatType.findAll().forEach(
                            vatType -> {
                                if (!Vat.findActiveUnique(vatType, inst, new DateTime()).isPresent()) {
                                    addErrorMessage(
                                            BundleUtil.getString(Constants.BUNDLE, "label.Vat.missing.vattype.for",
                                                    inst.getName(), vatType.getName().getContent()), model);
                                }
                            });
                });
    }

    private Set<Vat> getSearchUniverseSearchVatDataSet() {
        //
        //The initialization of the result list must be done here
        //
        //
        return Vat.findAll().collect(Collectors.toSet()); //CHANGE_ME
    }

    private List<Vat> filterSearchVat(VatType vatType, FinantialInstitution finantialInstitution, Boolean onlyActive) {

        return getSearchUniverseSearchVatDataSet().stream()
                .filter(x -> finantialInstitution == null || finantialInstitution.equals(x.getFinantialInstitution()))
                .filter(x -> vatType == null || vatType.equals(x.getVatType())).collect(Collectors.toList());
    }

    @RequestMapping(value = "/search/view/{oid}")
    public String processSearchToViewAction(@PathVariable("oid") Vat vat, Model model, RedirectAttributes redirectAttributes) {

        // CHANGE_ME Insert code here for processing viewAction
        // If you selected multiple exists you must choose which one to use below	 
        return redirect("/treasury/administration/base/managevat/vat/read" + "/" + vat.getExternalId(), model, redirectAttributes);
    }

//				
    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") Vat vat, Model model) {
        setVat(vat, model);
        return "treasury/administration/base/managevat/vat/read";
    }

//
    @RequestMapping(value = DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") Vat vat, Model model, RedirectAttributes redirectAttributes) {

        setVat(vat, model);
        try {
            assertUserIsFrontOfficeMember(vat.getFinantialInstitution(), model);

            deleteVat(vat);

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.delete"), model);
            return redirect("/treasury/administration/base/managevat/vat/", model, redirectAttributes);

        } catch (DomainException ex) {
            //Add error messages to the list
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + ex.getLocalizedMessage(), model);

        } catch (Exception ex) {
            //Add error messages to the list
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + ex.getLocalizedMessage(), model);
        }

        //The default mapping is the same Read View
        return redirect("/treasury/administration/base/managevat/vat/read/" + getVat(model).getExternalId(), model,
                redirectAttributes);
    }

//				
    @RequestMapping(value = CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {
        model.addAttribute("vatTypeList", VatType.findAll().collect(Collectors.toList()));
        model.addAttribute("finantialInstitutionList", FinantialInstitution.findAll().collect(Collectors.toList()));
        model.addAttribute("vatExemptionReasonList", VatExemptionReason.findAll().collect(Collectors.toList()));

        return "treasury/administration/base/managevat/vat/create";
    }

//				
    @RequestMapping(value = CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "vatType", required = false) VatType vatType, @RequestParam(
            value = "finantialInstitution", required = false) FinantialInstitution finantialInstitution, @RequestParam(
            value = "vatExemptionReason", required = false) VatExemptionReason vatExemptionReason, @RequestParam(
            value = "taxrate", required = false) java.math.BigDecimal taxRate, @RequestParam(value = "begindate",
            required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") org.joda.time.DateTime beginDate, @RequestParam(
            value = "enddate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") org.joda.time.DateTime endDate,
            Model model, RedirectAttributes redirectAttributes) {

        try {
            assertUserIsFrontOfficeMember(finantialInstitution, model);

            Vat vat = createVat(vatType, finantialInstitution, vatExemptionReason, taxRate, beginDate, endDate);

            model.addAttribute("vat", vat);

            return redirect("/treasury/administration/base/managevat/vat/read/" + getVat(model).getExternalId(), model,
                    redirectAttributes);

        } catch (DomainException de) {

            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + de.getLocalizedMessage(), model);
            return create(model);

        } catch (Exception de) {

            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + de.getLocalizedMessage(), model);
            return create(model);
        }
    }

    @Atomic
    public Vat createVat(VatType vatType, FinantialInstitution finantialInstitution, VatExemptionReason vatExemptionReason,
            java.math.BigDecimal taxRate, org.joda.time.DateTime beginDate, org.joda.time.DateTime endDate) {
        Vat vat = Vat.create(vatType, finantialInstitution, vatExemptionReason, taxRate, beginDate, endDate);
        return vat;
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") Vat vat, Model model) {
        setVat(vat, model);
        model.addAttribute("vatTypeList", VatType.findAll().collect(Collectors.toList()));
        model.addAttribute("finantialInstitutionList", FinantialInstitution.findAll().collect(Collectors.toList()));
        model.addAttribute("vatExemptionReasonList", VatExemptionReason.findAll().collect(Collectors.toList()));

        return "treasury/administration/base/managevat/vat/update";
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(
            @RequestParam(value = "vatExemptionReason", required = false) VatExemptionReason vatExemptionReason,
            @PathVariable("oid") Vat vat,
            @RequestParam(value = "taxrate", required = false) java.math.BigDecimal taxRate,
            @RequestParam(value = "begindate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") org.joda.time.DateTime beginDate,
            @RequestParam(value = "enddate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") org.joda.time.DateTime endDate,
            Model model, RedirectAttributes redirectAttributes) {

        setVat(vat, model);

        try {
            assertUserIsFrontOfficeMember(vat.getFinantialInstitution(), model);

            updateVat(vatExemptionReason, taxRate, beginDate, endDate, model);

            return redirect("/treasury/administration/base/managevat/vat/read/" + getVat(model).getExternalId(), model,
                    redirectAttributes);

        } catch (DomainException de) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + de.getLocalizedMessage(), model);
            return update(vat, model);

        } catch (Exception de) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + de.getLocalizedMessage(), model);
            return update(vat, model);
        }
    }

    @Atomic
    public void updateVat(VatExemptionReason vatExemptionReason, java.math.BigDecimal taxRate, org.joda.time.DateTime beginDate,
            org.joda.time.DateTime endDate, Model m) {
        getVat(m).edit(taxRate, vatExemptionReason, beginDate, endDate);
    }

}
