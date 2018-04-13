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
package org.fenixedu.treasury.ui.administration.base.managepaymentmethod;

import static org.fenixedu.treasury.util.Constants.treasuryBundle;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.PaymentMethod;
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

//@Component("org.fenixedu.treasury.ui.administration.base.managePaymentMethod") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.administration.base.managePaymentMethod",
        accessGroup = "treasuryManagers")
@RequestMapping(PaymentMethodController.CONTROLLER_URL)
public class PaymentMethodController extends TreasuryBaseController {
    public static final String CONTROLLER_URL = "/treasury/administration/base/managepaymentmethod/paymentmethod";
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

    private PaymentMethod getPaymentMethod(Model m) {
        return (PaymentMethod) m.asMap().get("paymentMethod");
    }

    private void setPaymentMethod(PaymentMethod paymentMethod, Model m) {
        m.addAttribute("paymentMethod", paymentMethod);
    }

    @Atomic
    public void deletePaymentMethod(PaymentMethod paymentMethod) {
        paymentMethod.delete();
    }

    @RequestMapping(value = SEARCH_URI)
    public String search(@RequestParam(value = "code", required = false) String code, @RequestParam(value = "name",
            required = false) LocalizedString name, Model model) {
        List<PaymentMethod> searchpaymentmethodResultsDataSet = filterSearchPaymentMethod(code, name);
        model.addAttribute("searchpaymentmethodResultsDataSet", searchpaymentmethodResultsDataSet);
        return "treasury/administration/base/managepaymentmethod/paymentmethod/search";
    }

    private Stream<PaymentMethod> getSearchUniverseSearchPaymentMethodDataSet() {
        return PaymentMethod.findAll();
    }

    private List<PaymentMethod> filterSearchPaymentMethod(String code, LocalizedString name) {
        return getSearchUniverseSearchPaymentMethodDataSet()
                .filter(paymentMethod -> code == null || code.length() == 0 || paymentMethod.getCode() != null
                        && paymentMethod.getCode().length() > 0
                        && paymentMethod.getCode().toLowerCase().contains(code.toLowerCase()))
                .filter(paymentMethod -> name == null
                        || name.isEmpty()
                        || name.getLocales()
                                .stream()
                                .allMatch(
                                        locale -> paymentMethod.getName().getContent(locale) != null
                                                && paymentMethod.getName().getContent(locale).toLowerCase()
                                                        .contains(name.getContent(locale).toLowerCase())))
                .collect(Collectors.toList());
    }

    private static final String SEARCH_VIEW_URI = "/search/view/";
    public static final String SEARCH_VIEW_URL = CONTROLLER_URL + SEARCH_VIEW_URI;

    @RequestMapping(value = SEARCH_VIEW_URI + "{oid}")
    public String processSearchToViewAction(@PathVariable("oid") PaymentMethod paymentMethod, Model model,
            RedirectAttributes redirectAttributes) {
        return redirect(READ_URL + paymentMethod.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") PaymentMethod paymentMethod, Model model) {
        setPaymentMethod(paymentMethod, model);
        return "treasury/administration/base/managepaymentmethod/paymentmethod/read";
    }

    @RequestMapping(value = DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") PaymentMethod paymentMethod, Model model, RedirectAttributes redirectAttributes) {
        setPaymentMethod(paymentMethod, model);
        try {
            assertUserIsBackOfficeMember(model);

            deletePaymentMethod(paymentMethod);

            addInfoMessage(treasuryBundle("label.success.delete"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(treasuryBundle("label.error.delete") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(treasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);
        }
        return redirect(READ_URL + getPaymentMethod(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {
        return "treasury/administration/base/managepaymentmethod/paymentmethod/create";
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "code", required = false) String code, @RequestParam(value = "name",
            required = false) LocalizedString name, @RequestParam(value = "availableForPaymentInApplication") boolean availableForPaymentInApplication, Model model, RedirectAttributes redirectAttributes) {
        try {
            assertUserIsBackOfficeMember(model);

            PaymentMethod paymentMethod = createPaymentMethod(code, name, availableForPaymentInApplication);
            model.addAttribute("paymentMethod", paymentMethod);

            return redirect(READ_URL + getPaymentMethod(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(treasuryBundle("label.error.create") + tde.getLocalizedMessage(), model);
        } catch (Exception de) {
            addErrorMessage(treasuryBundle("label.error.create") + de.getLocalizedMessage(), model);
        }
        return create(model);
    }

    @Atomic
    public PaymentMethod createPaymentMethod(String code, LocalizedString name, boolean availableForPaymentInApplication) {
        PaymentMethod paymentMethod = PaymentMethod.create(code, name, availableForPaymentInApplication);
        return paymentMethod;
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") PaymentMethod paymentMethod, Model model) {
        setPaymentMethod(paymentMethod, model);
        return "treasury/administration/base/managepaymentmethod/paymentmethod/update";
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") PaymentMethod paymentMethod,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) LocalizedString name, 
            @RequestParam(value = "availableForPaymentInApplication") boolean availableForPaymentInApplication, Model model,
            RedirectAttributes redirectAttributes) {
        setPaymentMethod(paymentMethod, model);
        try {
            assertUserIsBackOfficeMember(model);

            updatePaymentMethod(code, name, availableForPaymentInApplication, model);
            return redirect(READ_URL + getPaymentMethod(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(treasuryBundle("label.error.update") + tde.getLocalizedMessage(), model);
        } catch (Exception de) {
            addErrorMessage(treasuryBundle("label.error.update") + de.getLocalizedMessage(), model);
        }
        return update(paymentMethod, model);
    }

    @Atomic
    public void updatePaymentMethod(final String code, final LocalizedString name, final boolean availableForPaymentInApplication, Model m) {
        getPaymentMethod(m).edit(code, name, availableForPaymentInApplication);
    }

}
