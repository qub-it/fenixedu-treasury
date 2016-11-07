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
package org.fenixedu.treasury.ui.managetreasuryexemption;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.event.TreasuryEvent;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.exemption.TreasuryExemption;
import org.fenixedu.treasury.dto.TreasuryExemptionBean;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.accounting.managecustomer.CustomerController;
import org.fenixedu.treasury.ui.accounting.managecustomer.TreasuryEventController;
import org.fenixedu.treasury.util.Constants;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//@Component("org.fenixedu.treasury.ui.manageTreasuryExemption") <-- Use for duplicate controller name disambiguation
@BennuSpringController(value = CustomerController.class)
@RequestMapping(TreasuryExemptionController.CONTROLLER_URL)
public class TreasuryExemptionController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/managetreasuryexemption/treasuryexemption";
    private static final String JSP_PATH = "treasury/managetreasuryexemption/treasuryexemption";

    private TreasuryExemptionBean getTreasuryExemptionBean(Model model) {
        return (TreasuryExemptionBean) model.asMap().get("treasuryExemptionBean");
    }

    private void setTreasuryExemptionBean(TreasuryExemptionBean bean, Model model) {
        model.addAttribute("treasuryExemptionBeanJson", getBeanJson(bean));
        model.addAttribute("treasuryExemptionBean", bean);
    }

    private TreasuryExemption getTreasuryExemption(Model m) {
        return (TreasuryExemption) m.asMap().get("treasuryExemption");
    }

    private void setTreasuryExemption(TreasuryExemption treasuryExemption, Model m) {
        m.addAttribute("treasuryExemption", treasuryExemption);
    }

    private TreasuryEvent getTreasuryEvent(Model m) {
        return (TreasuryEvent) m.asMap().get("treasuryEvent");
    }

    private void setTreasuryEvent(TreasuryEvent treasuryEvent, Model m) {
        m.addAttribute("treasuryEvent", treasuryEvent);
    }

    private static final String _SEARCH_TO_DELETE_ACTION_URI = "/search/delete/";
    public static final String SEARCH_TO_DELETE_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_DELETE_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_DELETE_ACTION_URI + "{oid}", method = RequestMethod.POST)
    public String processSearchToDeleteAction(@PathVariable("oid") TreasuryExemption treasuryExemption, Model model,
            RedirectAttributes redirectAttributes) {

        final TreasuryEvent treasuryEvent = treasuryExemption.getTreasuryEvent();

        try {
            assertUserIsFrontOfficeMember(model);

            treasuryExemption.delete();

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.delete"), model);
        } catch (DomainException ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }

        return redirect(TreasuryEventController.READ_URL + treasuryEvent.getExternalId(), model, redirectAttributes);
    }

    private static final String _CREATE_URI = "/create/";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value = _CREATE_URI + "{treasuryEventId}", method = RequestMethod.GET)
    public String create(@PathVariable("treasuryEventId") final TreasuryEvent treasuryEvent, final Model model) {
        setTreasuryEvent(treasuryEvent, model);
        setTreasuryExemptionBean(new TreasuryExemptionBean(treasuryEvent), model);
        return jspPage("create");
    }

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "bean", required = true) TreasuryExemptionBean bean, final Model model,
            final RedirectAttributes redirectAttributes) {
        setTreasuryExemptionBean(bean, model);
        try {
            assertUserIsFrontOfficeMember(model);

            TreasuryExemption.create(bean.getTreasuryExemptionType(), bean.getTreasuryEvent(), bean.getReason(),
                    bean.getValuetoexempt(), bean.getDebitEntry());

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.create"), model);

            return redirect(TreasuryEventController.READ_URL + bean.getTreasuryEvent().getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + tex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + ex.getLocalizedMessage(), model);
        }
        return create(bean.getTreasuryEvent(), model);
    }

    private static final String _CREATEPOSTBACK_URI = "/createPostBack/";
    public static final String CREATEPOSTBACK_URL = CONTROLLER_URL + _CREATEPOSTBACK_URI;

    @RequestMapping(value = _CREATEPOSTBACK_URI, method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody String createpostback(@RequestParam(value = "bean", required = true) TreasuryExemptionBean bean,
            Model model) {
        if (bean.getDebitEntry() != null && bean.getTreasuryExemptionType() != null) {
            BigDecimal amount =
                    bean.getDebitEntry().getAmountWithVat().multiply(
                            bean.getTreasuryExemptionType().getDefaultExemptionPercentage()
                            .divide(BigDecimal.valueOf(100)));
            amount =
                    bean.getTreasuryEvent().getDebtAccount().getFinantialInstitution().getCurrency()
                    .getValueWithScale(amount);
            bean.setValuetoexempt(amount);
        }
        
        setTreasuryExemptionBean(bean, model);
        return getBeanJson(bean);
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }

}
