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
package org.fenixedu.treasury.ui.administration.base.managecurrency;

import static org.fenixedu.treasury.util.Constants.treasuryBundle;

import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.Currency;
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

//@Component("org.fenixedu.treasury.ui.administration.base.manageCurrency") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.administration.base.manageCurrency",
        accessGroup = "treasuryManagers")
@RequestMapping(CurrencyController.CONTROLLER_URL)
public class CurrencyController extends TreasuryBaseController {
    public static final String CONTROLLER_URL = "/treasury/administration/base/managecurrency/currency";
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

    private Currency getCurrency(Model m) {
        return (Currency) m.asMap().get("currency");
    }

    private void setCurrency(Currency currency, Model m) {
        m.addAttribute("currency", currency);
    }

    @Atomic
    public void deleteCurrency(Currency currency) {
        currency.delete();
    }

    @RequestMapping(value = SEARCH_URI)
    public String search(@RequestParam(value = "code", required = false) String code, @RequestParam(value = "name",
            required = false) LocalizedString name, @RequestParam(value = "isocode", required = false) String isoCode,
            @RequestParam(value = "symbol", required = false) String symbol, Model model) {
        List<Currency> searchcurrencyResultsDataSet = filterSearchCurrency(code, name, isoCode, symbol);
        model.addAttribute("searchcurrencyResultsDataSet", searchcurrencyResultsDataSet);
        return "treasury/administration/base/managecurrency/currency/search";
    }

    private List<Currency> getSearchUniverseSearchCurrencyDataSet() {
        return Currency.findAll().collect(Collectors.toList());
    }

    private List<Currency> filterSearchCurrency(String code, LocalizedString name, String isoCode, String symbol) {
        return getSearchUniverseSearchCurrencyDataSet()
                .stream()
                .filter(currency -> code == null || code.length() == 0 || currency.getCode() != null
                        && currency.getCode().length() > 0 && currency.getCode().toLowerCase().contains(code.toLowerCase()))
                .filter(currency -> name == null
                        || name.isEmpty()
                        || name.getLocales()
                                .stream()
                                .allMatch(
                                        locale -> currency.getName().getContent(locale) != null
                                                && currency.getName().getContent(locale).toLowerCase()
                                                        .contains(name.getContent(locale).toLowerCase())))
                .filter(currency -> isoCode == null || isoCode.length() == 0 || currency.getIsoCode() != null
                        && currency.getIsoCode().length() > 0
                        && currency.getIsoCode().toLowerCase().contains(isoCode.toLowerCase()))
                .filter(currency -> symbol == null || symbol.length() == 0 || currency.getSymbol() != null
                        && currency.getSymbol().length() > 0 && currency.getSymbol().toLowerCase().contains(symbol.toLowerCase()))
                .collect(Collectors.toList());
    }

    private static final String SEARCH_VIEW_URI = "/search/view/";
    public static final String SEARCH_VIEW_URL = CONTROLLER_URL + SEARCH_VIEW_URI;

    @RequestMapping(value = SEARCH_VIEW_URI + "{oid}")
    public String processSearchToViewAction(@PathVariable("oid") Currency currency, Model model,
            RedirectAttributes redirectAttributes) {
        return redirect(READ_URL + currency.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") Currency currency, Model model) {
        setCurrency(currency, model);
        return "treasury/administration/base/managecurrency/currency/read";
    }

    @RequestMapping(value = DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") Currency currency, Model model, RedirectAttributes redirectAttributes) {
        setCurrency(currency, model);
        try {
            assertUserIsBackOfficeMember(model);
            deleteCurrency(currency);

            addInfoMessage(treasuryBundle("label.success.delete"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(treasuryBundle("label.error.delete") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(treasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);
        }
        return redirect(READ_URL + getCurrency(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {
        return "treasury/administration/base/managecurrency/currency/create";
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "code", required = false) String code, @RequestParam(value = "name",
            required = false) LocalizedString name, @RequestParam(value = "isocode", required = false) String isoCode,
            @RequestParam(value = "symbol", required = false) String symbol, Model model, RedirectAttributes redirectAttributes) {
        try {
            assertUserIsBackOfficeMember(model);

            Currency currency = createCurrency(code, name, isoCode, symbol);
            model.addAttribute("currency", currency);

            return redirect(READ_URL + getCurrency(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(treasuryBundle("label.error.create") + tde.getLocalizedMessage(), model);
        } catch (Exception de) {
            addErrorMessage(treasuryBundle("label.error.create") + de.getLocalizedMessage(), model);
        }
        return create(model);
    }

    @Atomic
    public Currency createCurrency(String code, LocalizedString name, String isoCode, String symbol) {
        Currency currency = Currency.create(code, name, isoCode, symbol);
        return currency;
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") Currency currency, Model model) {
        setCurrency(currency, model);
        return "treasury/administration/base/managecurrency/currency/update";
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") Currency currency, @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) LocalizedString name, @RequestParam(value = "isocode",
                    required = false) String isoCode, @RequestParam(value = "symbol", required = false) String symbol,
            Model model, RedirectAttributes redirectAttributes) {
        setCurrency(currency, model);
        try {
            assertUserIsBackOfficeMember(model);

            updateCurrency(code, name, isoCode, symbol, model);
            return redirect(READ_URL + getCurrency(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(treasuryBundle("label.error.update") + tde.getLocalizedMessage(), model);
        } catch (Exception de) {
            addErrorMessage(treasuryBundle("label.error.update") + de.getLocalizedMessage(), model);
        }
        return update(currency, model);
    }

    @Atomic
    public void updateCurrency(String code, LocalizedString name, String isoCode, String symbol, Model m) {
        getCurrency(m).edit(code, name, isoCode, symbol);
    }

}
