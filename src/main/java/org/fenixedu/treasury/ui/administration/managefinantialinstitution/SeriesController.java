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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.Series;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.util.Constants;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

@Component("org.fenixedu.treasury.ui.administration.manageFinantialInstitution.series")
@BennuSpringController(value = FinantialInstitutionController.class)
@RequestMapping(SeriesController.CONTROLLER_URL)
public class SeriesController extends TreasuryBaseController {
    public static final String CONTROLLER_URL = "/treasury/administration/managefinantialinstitution/series";
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
        if (model.containsAttribute("finantialInstitutionId")) {
            return "forward:" + FinantialInstitutionController.READ_URL + model.asMap().get("finantialInstitutionId");
        }
        return "forward:" + FinantialInstitutionController.SEARCH_URL;
    }

    private Series getSeries(Model model) {
        return (Series) model.asMap().get("series");
    }

    private void setSeries(Series series, Model model) {
        model.addAttribute("series", series);
    }

    @RequestMapping(value = "/search/view/{oid}")
    public String processSearchToViewAction(@PathVariable("oid") Series series, Model model, RedirectAttributes redirectAttributes) {
        return redirect(READ_URL + series.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") Series series, Model model, RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(series.getFinantialInstitution(), model);
            setSeries(series, model);
            return "treasury/administration/managefinantialinstitution/series/read";
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }
        return redirect(FinantialInstitutionController.READ_URL + series.getFinantialInstitution().getExternalId(), model,
                redirectAttributes);
    }

    @RequestMapping(value = DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") Series series, Model model, RedirectAttributes redirectAttributes) {
        setSeries(series, model);
        try {
            final FinantialInstitution finantialInstitution = series.getFinantialInstitution();
            super.assertUserIsBackOfficeMember(finantialInstitution, model);

            series.delete();
            
            addInfoMessage(Constants.treasuryBundle("label.success.delete"), model);
            return redirect(FinantialInstitutionController.READ_URL + finantialInstitution.getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(Constants.treasuryBundle("label.error.delete") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(Constants.treasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);
        }
        return redirect(READ_URL + series.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.GET)
    public String create(
            @RequestParam(value = "finantialInstitutionId", required = false) FinantialInstitution finantialInstitution,
            Model model) {
        model.addAttribute("finantialInstitutionId", finantialInstitution.getExternalId());
        return "treasury/administration/managefinantialinstitution/series/create";
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.POST)
    public String create(
            @RequestParam(value = "finantialInstitutionId", required = false) FinantialInstitution finantialInstitution,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) LocalizedString name, @RequestParam(value = "externseries",
                    required = false) boolean externSeries,
            @RequestParam(value = "certificated", required = false) boolean certificated, 
            @RequestParam(value = "legacy", required = false) boolean legacy,
            @RequestParam(value = "selectable", required = false) final boolean selectable,
            final Model model, final RedirectAttributes redirectAttributes) {
        try {
            super.assertUserIsBackOfficeMember(finantialInstitution, model);
            Series series = createSeries(finantialInstitution, code, name, externSeries, certificated, legacy, selectable);
            model.addAttribute("series", series);
            return redirect(READ_URL + getSeries(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(Constants.treasuryBundle("label.error.create") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(Constants.treasuryBundle("label.error.create") + ex.getLocalizedMessage(), model);
        }
        return create(finantialInstitution, model);
    }

    @Atomic
    public Series createSeries(FinantialInstitution finantialInstitution, String code, LocalizedString name,
            boolean externSeries, boolean certificated, boolean legacy, final boolean selectableForPayments) {
        //When creating the first series, it is the default series.
        boolean defaultSeries = finantialInstitution.getSeriesSet().size() == 0;
        return Series.create(finantialInstitution, code, name, externSeries, certificated, legacy, defaultSeries, selectableForPayments);
    }

    @RequestMapping(value = "/search/edit/{oid}")
    public String processSearchToEditAction(@PathVariable("oid") Series series, Model model, RedirectAttributes redirectAttributes) {
        return redirect(UPDATE_URL + series.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = "/search/editDefault/{oid}")
    public String processSearchToEditDefaultAction(@PathVariable("oid") Series series, Model model,
            RedirectAttributes redirectAttributes) {
        setSeries(series, model);
        setSeriesDefault(model);
        return redirect(FinantialInstitutionController.READ_URL + series.getFinantialInstitution().getExternalId(), model,
                redirectAttributes);
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") Series series, Model model, RedirectAttributes redirectAttributes) {
        setSeries(series, model);
        try {
            super.assertUserIsBackOfficeMember(series.getFinantialInstitution(), model);
            return "treasury/administration/managefinantialinstitution/series/update";
        } catch (Exception ex) {
            addErrorMessage(Constants.treasuryBundle("label.error.update") + ex.getLocalizedMessage(), model);
        }
        return redirect(READ_URL + series.getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(
                @PathVariable("oid") final Series series, 
                @RequestParam(value = "code", required = true) final String code,
                @RequestParam(value = "name", required = true) final LocalizedString name, 
                @RequestParam(value = "externseries", required = true) final boolean externSeries,
                @RequestParam(value = "certificated", required = false) final boolean certificated, 
                @RequestParam(value = "legacy", required = true) final boolean legacy, 
                @RequestParam(value = "active", required = true) final boolean active,
                @RequestParam(value = "selectable", required = false) final boolean selectable,
            Model model, RedirectAttributes redirectAttributes) {
        setSeries(series, model);
        try {
            super.assertUserIsBackOfficeMember(series.getFinantialInstitution(), model);
            updateSeries(code, name, externSeries, certificated, legacy, active, selectable, model);
            return redirect(READ_URL + getSeries(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(Constants.treasuryBundle("label.error.update") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(Constants.treasuryBundle("label.error.update") + ex.getLocalizedMessage(), model);
        }
        return update(series, model, redirectAttributes);
    }

    public void updateSeries(final String code, final LocalizedString name, final boolean externSeries, final boolean certificated, final boolean legacy,
            final boolean active, final boolean selectable, final Model model) {
        getSeries(model).edit(code, name, externSeries, certificated, legacy, active, selectable);
    }

    public void setSeriesDefault(Model model) {
        getSeries(model).getFinantialInstitution().markSeriesAsDefault(getSeries(model));
    }

    @RequestMapping(value = "/read/{oid}/createdebitnoteforpendingdebitentries", method = RequestMethod.POST)
    public String processReadToCreateDebitNoteForPendingDebitEntries(@PathVariable("oid") Series series, Model model,
            RedirectAttributes redirectAttributes) {

        FinantialInstitution fin = series.getFinantialInstitution();

        List<DebitEntry> allDebitEntries = new ArrayList<DebitEntry>();

        //Collect all Pending debitEntries
        for (DebtAccount debtAccount : fin.getDebtAccountsSet()) {
            allDebitEntries.addAll(debtAccount.getPendingInvoiceEntriesSet().stream().filter(x -> x.isDebitNoteEntry())
                    .filter(x -> x.getFinantialDocument() == null).map(DebitEntry.class::cast).collect(Collectors.toList()));
        }

        List<DebitEntry> sortedDebitEntries = allDebitEntries.stream().sorted((x, y) -> {
            if (x.getEntryDateTime().equals(y.getEntryDateTime())) {
                return x.getDueDate().compareTo(y.getDueDate());
            } else {
                return x.getEntryDateTime().compareTo(y.getEntryDateTime());
            }
        }).collect(Collectors.toList());

        for (DebitEntry debitEntry : sortedDebitEntries) {
            try {
                series.createDebitNoteForPendingEntry(debitEntry);
            } catch (Exception ex) {
                addErrorMessage(
                        "Error for debtAccount" + debitEntry.getDescription() + "-"
                                + debitEntry.getDebtAccount().getCustomer().getBusinessIdentification() + " : "
                                + ex.getLocalizedMessage(), model);
            }
        }
        addInfoMessage("SUCCESS", model);
        return redirect(READ_URL + series.getExternalId(), model, redirectAttributes);
    }

//    @RequestMapping(value = "/read/{oid}/createpaymentreferencecode", method = RequestMethod.GET)
//    public String processReadToCreatePaymentReferenceCode(@PathVariable("oid") Series series, Model model,
//            RedirectAttributes redirectAttributes) {
//
//        FinantialInstitution fin = series.getFinantialInstitution();
//
//        for (DebtAccount debtAccount : fin.getDebtAccountsSet()) {
//            try {
//                if (debtAccount.getPendingInvoiceEntriesSet().stream().anyMatch(x -> x.getFinantialDocument() == null)) {
//                    series.createDebitNoteForPendingEntries(debtAccount);
//                    break;
//                }
//            } catch (Exception ex) {
//                addErrorMessage("Error for debtAccount : " + ex.getLocalizedMessage(), model);
//            }
//        }
//
//        addInfoMessage("SUCCESS", model);
//        return redirect(READ_URL + series.getExternalId(), model, redirectAttributes);
//    }

}
