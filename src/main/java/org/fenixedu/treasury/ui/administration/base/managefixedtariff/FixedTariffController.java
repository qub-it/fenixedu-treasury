/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
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
package org.fenixedu.treasury.ui.administration.base.managefixedtariff;

import static org.fenixedu.treasury.util.Constants.treasuryBundle;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.tariff.DueDateCalculationType;
import org.fenixedu.treasury.domain.tariff.FixedTariff;
import org.fenixedu.treasury.domain.tariff.InterestRate;
import org.fenixedu.treasury.dto.FixedTariffBean;
import org.fenixedu.treasury.dto.FixedTariffInterestRateBean;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.administration.base.manageproduct.ProductController;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

//@Component("org.fenixedu.treasury.ui.administration.base.manageFixedTariff") <-- Use for duplicate controller name disambiguation
@BennuSpringController(value = ProductController.class)
@RequestMapping(FixedTariffController.CONTROLLER_URL)
public class FixedTariffController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/administration/base/managefixedtariff/fixedtariff";
    protected static final String _DELETE_URI = "/delete/";
    public static final String DELETE_URL = CONTROLLER_URL + _DELETE_URI;

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/";
    }

    private FixedTariffBean getFixedTariffBean(Model model) {
        return (FixedTariffBean) model.asMap().get("fixedTariffBean");
    }

    private void setFixedTariffBean(FixedTariffBean bean, Model model) {
        model.addAttribute("fixedTariffBeanJson", getBeanJson(bean));
        model.addAttribute("fixedTariffBean", bean);
    }

    private FixedTariff getFixedTariff(Model model) {
        return (FixedTariff) model.asMap().get("fixedTariff");
    }

    private void setFixedTariff(FixedTariff fixedTariff, Model model) {
        model.addAttribute("fixedTariff", fixedTariff);
    }

    @Atomic
    public void deleteFixedTariff(FixedTariff fixedTariff) {

        fixedTariff.delete();
    }

    @RequestMapping(value = _DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") FixedTariff fixedTariff, Model model, RedirectAttributes redirectAttributes) {
        String productId = fixedTariff.getProduct().getExternalId();
        setFixedTariff(fixedTariff, model);
        try {
            assertUserIsBackOfficeMember(fixedTariff.getFinantialEntity().getFinantialInstitution(), model);

            deleteFixedTariff(fixedTariff);

            addInfoMessage(treasuryBundle("label.success.delete"), model);
            return redirect(ProductController.READ_URL + productId, model, redirectAttributes);
        } catch (DomainException ex) {
            addErrorMessage(treasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);

        } catch (Exception ex) {
            addErrorMessage(treasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);
        }
        return redirect(ProductController.READ_URL + productId, model, redirectAttributes);
    }

    private static final String _READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + _READ_URI;

    @RequestMapping(value = _READ_URI + "{oid}")
    public String read(@PathVariable("oid") FixedTariff fixedTariff, Model model) {
        setFixedTariff(fixedTariff, model);
        return "treasury/administration/base/managefixedtariff/fixedtariff/read";
    }

    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.GET)
    public String create(@RequestParam(value = "product", required = true) Product product, @RequestParam(
            value = "finantialInstitution", required = true) FinantialInstitution finantialInstitution, Model model) {

        FixedTariffBean bean = new FixedTariffBean();
        bean.setProduct(product);
        bean.setFinantialEntityDataSource(finantialInstitution.getFinantialEntitiesSet().stream().collect(Collectors.toList()));
        bean.setFinantialInstitution(finantialInstitution);
        this.setFixedTariffBean(bean, model);
        return "treasury/administration/base/managefixedtariff/fixedtariff/create";

    }

    private static final String _CREATEPOSTBACK_URI = "/createpostback";
    public static final String CREATEPOSTBACK_URL = CONTROLLER_URL + _CREATEPOSTBACK_URI;

    @RequestMapping(value = _CREATEPOSTBACK_URI, method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody String createpostback(@RequestParam(value = "bean", required = false) FixedTariffBean bean, Model model) {
        this.setFixedTariffBean(bean, model);
        return getBeanJson(bean);
    }

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "bean", required = false) FixedTariffBean bean, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            assertUserIsBackOfficeMember(bean.getFinantialInstitution(), model);

            FixedTariff fixedTariff =
                    createFixedTariff(bean.getAmount(), bean.getApplyInterests(), bean.getBeginDate(),
                            bean.getDueDateCalculationType(), bean.getEndDate(), bean.getFinantialEntity(),
                            bean.getFixedDueDate(), bean.getNumberOfDaysAfterCreationForDueDate(), bean.getInterestRate(),
                            bean.getProduct());

            setFixedTariff(fixedTariff, model);
            return redirect(FixedTariffController.READ_URL + getFixedTariff(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(treasuryBundle("label.error.create") + tde.getLocalizedMessage(), model);
            this.setFixedTariffBean(bean, model);
        }
        return "treasury/administration/base/managefixedtariff/fixedtariff/create";
    }

    @Atomic
    public FixedTariff createFixedTariff(java.math.BigDecimal amount, boolean applyInterests, org.joda.time.LocalDate beginDate,
            org.fenixedu.treasury.domain.tariff.DueDateCalculationType dueDateCalculationType, org.joda.time.LocalDate endDate,
            org.fenixedu.treasury.domain.FinantialEntity finantialEntity, org.joda.time.LocalDate fixedDueDate,
            int numberOfDaysAfterCreationForDueDate, FixedTariffInterestRateBean interestRateBean, Product product) {
        InterestRate interestRate = null;

        FixedTariff fixedTariff =
                FixedTariff.create(product, interestRate, finantialEntity, amount, beginDate.toDateTimeAtStartOfDay(),
                        endDate.toDateTimeAtStartOfDay(), dueDateCalculationType, fixedDueDate,
                        numberOfDaysAfterCreationForDueDate, false);
        if (applyInterests) {
            interestRate =
                    InterestRate.createForTariff(fixedTariff, interestRateBean.getInterestType(),
                            interestRateBean.getNumberOfDaysAfterDueDate(), interestRateBean.getApplyInFirstWorkday(),
                            interestRateBean.getMaximumDaysToApplyPenalty(), interestRateBean.getMaximumMonthsToApplyPenalty(),
                            interestRateBean.getInterestFixedAmount(), interestRateBean.getRate());
            fixedTariff.setInterestRate(interestRate);
            fixedTariff.setApplyInterests(true);
        }
        fixedTariff.checkRules();
        return fixedTariff;
    }

    private static final String _UPDATE_URI = "/update/";
    public static final String UPDATE_URL = CONTROLLER_URL + _UPDATE_URI;

    @RequestMapping(value = _UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") FixedTariff fixedTariff, Model model) {
        setFixedTariff(fixedTariff, model);
        FixedTariffBean bean = new FixedTariffBean(fixedTariff);
        setFixedTariffBean(bean, model);
        return "treasury/administration/base/managefixedtariff/fixedtariff/update";
    }

    private static final String _UPDATEPOSTBACK_URI = "/updatepostback/";
    public static final String UPDATEPOSTBACK_URL = CONTROLLER_URL + _UPDATEPOSTBACK_URI;

    @RequestMapping(value = _UPDATEPOSTBACK_URI + "{oid}", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public @ResponseBody String updatepostback(@PathVariable("oid") FixedTariff fixedTariff, @RequestParam(value = "bean",
            required = false) FixedTariffBean bean, Model model) {
        this.setFixedTariffBean(bean, model);
        return getBeanJson(bean);
    }

    @RequestMapping(value = _UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") FixedTariff fixedTariff,
            @RequestParam(value = "bean", required = false) FixedTariffBean bean, Model model,
            RedirectAttributes redirectAttributes) {
        setFixedTariff(fixedTariff, model);
        try {
            assertUserIsBackOfficeMember(bean.getFinantialInstitution(), model);

            updateFixedTariff(bean.getAmount(), bean.getApplyInterests(), bean.getBeginDate().toDateTimeAtStartOfDay(),
                    bean.getDueDateCalculationType(), bean.getEndDate().toDateTimeAtStartOfDay(), bean.getFinantialEntity(),
                    bean.getFixedDueDate(), bean.getNumberOfDaysAfterCreationForDueDate(), bean.getInterestRate(),
                    bean.getProduct(), model);
            return redirect(FixedTariffController.READ_URL + getFixedTariff(model).getExternalId(), model, redirectAttributes);
        } catch (Exception tde) {
            setFixedTariffBean(bean, model);
            addErrorMessage(treasuryBundle("label.error.update") + tde.getLocalizedMessage(), model);
        }
        return "treasury/administration/base/managefixedtariff/fixedtariff/update";
    }

    @Atomic
    public void updateFixedTariff(BigDecimal amount, boolean applyInterests, DateTime beginDate,
            DueDateCalculationType dueDateCalculationType, DateTime endDate, FinantialEntity finantialEntity,
            LocalDate fixedDueDate, int numberOfDaysAfterCreationForDueDate, FixedTariffInterestRateBean rateBean,
            Product product, Model model) {
        getFixedTariff(model).edit(product, finantialEntity, amount, beginDate, endDate, dueDateCalculationType, fixedDueDate,
                numberOfDaysAfterCreationForDueDate, applyInterests, rateBean);
    }
}
