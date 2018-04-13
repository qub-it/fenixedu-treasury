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
package org.fenixedu.treasury.ui.administration.payments.sibs.managepaymentreferencecode;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCode;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCodeStateType;
import org.fenixedu.treasury.domain.paymentcodes.SibsTransactionDetail;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.ui.administration.payments.sibs.managepaymentcodepool.PaymentCodePoolController;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

@Component("org.fenixedu.treasury.ui.administration.payments.sibs.managePaymentReferenceCode")
@SpringFunctionality(app = TreasuryController.class,
        title = "label.title.administration.payments.sibs.managePaymentReferenceCode", accessGroup = "treasuryFrontOffice")
// @BennuSpringController(value = PaymentCodePoolController.class)
@RequestMapping(PaymentReferenceCodeController.CONTROLLER_URL)
public class PaymentReferenceCodeController extends TreasuryBaseController {

    public static final String CONTROLLER_URL =
            "/treasury/administration/payments/sibs/managepaymentreferencecode/paymentreferencecode";

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/";
    }

    private PaymentReferenceCode getPaymentReferenceCode(Model model) {
        return (PaymentReferenceCode) model.asMap().get("paymentReferenceCode");
    }

    private void setPaymentReferenceCode(PaymentReferenceCode paymentReferenceCode, Model model) {
        model.addAttribute("paymentReferenceCode", paymentReferenceCode);
    }

    @Atomic
    public void deletePaymentReferenceCode(PaymentReferenceCode paymentReferenceCode) {
        paymentReferenceCode.delete();
    }

    private static final String _SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI)
    public String search(@RequestParam(value = "referencecode", required = false) String referenceCode, @RequestParam(
            value = "begindate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime beginDate, @RequestParam(
            value = "enddate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime endDate, @RequestParam(
            value = "state", required = false) PaymentReferenceCodeStateType state, Model model) {

        List<PaymentReferenceCode> searchpaymentreferencecodeResultsDataSet =
                filterSearchPaymentReferenceCode(referenceCode, beginDate, endDate, state);
        
        if(searchpaymentreferencecodeResultsDataSet.size() > 500) {
            searchpaymentreferencecodeResultsDataSet = searchpaymentreferencecodeResultsDataSet.subList(0, 500 - 1);
        }

        model.addAttribute("searchpaymentreferencecodeResultsDataSet", searchpaymentreferencecodeResultsDataSet);
        model.addAttribute("stateValues", PaymentReferenceCodeStateType.values());
        return "treasury/administration/payments/sibs/managepaymentreferencecode/paymentreferencecode/search";
    }

    private Stream<PaymentReferenceCode> getSearchUniverseSearchPaymentReferenceCodeDataSet() {
        return PaymentReferenceCode.findAll();
    }

    private List<PaymentReferenceCode> filterSearchPaymentReferenceCode(String referenceCode, DateTime beginDate,
            DateTime endDate, PaymentReferenceCodeStateType state) {

        return getSearchUniverseSearchPaymentReferenceCodeDataSet()
                .filter(paymentReferenceCode -> referenceCode == null || referenceCode.length() == 0
                        || paymentReferenceCode.getReferenceCode() != null
                        && paymentReferenceCode.getReferenceCode().length() > 0
                        && paymentReferenceCode.getReferenceCode().toLowerCase().contains(referenceCode.toLowerCase()))
                .filter(paymentReferenceCode -> beginDate == null || beginDate.equals(paymentReferenceCode.getBeginDate()))
                .filter(paymentReferenceCode -> endDate == null || endDate.equals(paymentReferenceCode.getEndDate()))
                .filter(paymentReferenceCode -> state == null || state.equals(paymentReferenceCode.getState()))
                .collect(Collectors.toList());
    }

    private static final String _SEARCH_TO_VIEW_ACTION_URI = "/search/view/";
    public static final String SEARCH_TO_VIEW_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_VIEW_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_VIEW_ACTION_URI + "{oid}")
    public String processSearchToViewAction(@PathVariable("oid") PaymentReferenceCode paymentReferenceCode, Model model,
            RedirectAttributes redirectAttributes) {
        return redirect(READ_URL + paymentReferenceCode.getExternalId(), model, redirectAttributes);
    }

    private static final String _READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + _READ_URI;

    @RequestMapping(value = _READ_URI + "{oid}")
    public String read(@PathVariable("oid") final PaymentReferenceCode paymentReferenceCode, Model model) {
        setPaymentReferenceCode(paymentReferenceCode, model);

        model.addAttribute("sibsTransactionDetails", SibsTransactionDetail.findBySibsEntityAndReferenceCode(
                paymentReferenceCode.getPaymentCodePool().getEntityReferenceCode(), paymentReferenceCode.getReferenceCode()).collect(Collectors.toSet()));

        return "treasury/administration/payments/sibs/managepaymentreferencecode/paymentreferencecode/read";
    }

    private static final String _DELETE_URI = "/delete/";
    public static final String DELETE_URL = CONTROLLER_URL + _DELETE_URI;

    @RequestMapping(value = _DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") PaymentReferenceCode paymentReferenceCode, Model model,
            RedirectAttributes redirectAttributes) {

        setPaymentReferenceCode(paymentReferenceCode, model);
        try {
            assertUserIsFrontOfficeMember(paymentReferenceCode.getPaymentCodePool().getFinantialInstitution(), model);

            assertUserIsBackOfficeMember(paymentReferenceCode.getPaymentCodePool().getFinantialInstitution(), model);
            deletePaymentReferenceCode(paymentReferenceCode);

            addInfoMessage(Constants.treasuryBundle("label.success.delete"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (TreasuryDomainException tex) {
            addErrorMessage(Constants.treasuryBundle("label.error.delete") + tex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(Constants.treasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);
        }

        return redirect(READ_URL + getPaymentReferenceCode(model).getExternalId(), model, redirectAttributes);
    }

    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {
        model.addAttribute("stateValues", PaymentReferenceCodeStateType.values());

        return "treasury/administration/payments/sibs/managepaymentreferencecode/paymentreferencecode/create";
    }

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "referencecode", required = false) String referenceCode, @RequestParam(
            value = "begindate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beginDate, @RequestParam(
            value = "enddate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate, @RequestParam(
            value = "state", required = false) PaymentReferenceCodeStateType state, @RequestParam(value = "paymentcodepool",
            required = false) PaymentCodePool pool, Model model, RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(pool.getFinantialInstitution(), model);

            assertUserIsBackOfficeMember(pool.getFinantialInstitution(), model);

            PaymentReferenceCode paymentReferenceCode =
                    createPaymentReferenceCode(referenceCode, beginDate, endDate, state, pool);
            model.addAttribute("paymentReferenceCode", paymentReferenceCode);
            addInfoMessage(Constants.treasuryBundle("label.success.create"), model);

            return redirect(READ_URL + getPaymentReferenceCode(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tex) {
            addErrorMessage(Constants.treasuryBundle("label.error.create") + tex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(Constants.treasuryBundle("label.error.create") + ex.getLocalizedMessage(), model);
        }
        return create(model);
    }

    @Atomic
    public PaymentReferenceCode createPaymentReferenceCode(String referenceCode, LocalDate beginDate, LocalDate endDate,
            PaymentReferenceCodeStateType state, PaymentCodePool pool) {
        PaymentReferenceCode paymentReferenceCode =
                PaymentReferenceCode.create(referenceCode, beginDate, endDate, state, pool, BigDecimal.ZERO, BigDecimal.ZERO);

        return paymentReferenceCode;
    }

    private static final String _UPDATE_URI = "/update/";
    public static final String UPDATE_URL = CONTROLLER_URL + _UPDATE_URI;

    @RequestMapping(value = _UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") PaymentReferenceCode paymentReferenceCode, Model model) {
        model.addAttribute("stateValues", PaymentReferenceCodeStateType.values());
        setPaymentReferenceCode(paymentReferenceCode, model);
        return "treasury/administration/payments/sibs/managepaymentreferencecode/paymentreferencecode/update";
    }

    @RequestMapping(value = _UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") PaymentReferenceCode paymentReferenceCode, @RequestParam(value = "referencecode",
            required = false) String referenceCode, @RequestParam(value = "begindate", required = false) @DateTimeFormat(
            pattern = "yyyy-MM-dd") LocalDate beginDate, @RequestParam(value = "enddate", required = false) @DateTimeFormat(
            pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "state", required = false) PaymentReferenceCodeStateType state, Model model,
            RedirectAttributes redirectAttributes) {

        setPaymentReferenceCode(paymentReferenceCode, model);

        try {
            assertUserIsFrontOfficeMember(paymentReferenceCode.getPaymentCodePool().getFinantialInstitution(), model);

            assertUserIsBackOfficeMember(paymentReferenceCode.getPaymentCodePool().getFinantialInstitution(), model);

            updatePaymentReferenceCode(referenceCode, beginDate, endDate, state, model);
            addInfoMessage(Constants.treasuryBundle("label.success.update"), model);

            return redirect(READ_URL + getPaymentReferenceCode(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tex) {
            addErrorMessage(Constants.treasuryBundle("label.error.update") + tex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(Constants.treasuryBundle("label.error.update") + ex.getLocalizedMessage(), model);
        }
        return update(paymentReferenceCode, model);
    }

    @Atomic
    public void updatePaymentReferenceCode(String referenceCode, LocalDate beginDate, LocalDate endDate,
            PaymentReferenceCodeStateType state, Model model) {
        getPaymentReferenceCode(model).edit(referenceCode, beginDate, endDate, state);
    }

    @RequestMapping(value = "/read/{oid}/anull", method = RequestMethod.POST)
    public String processReadToAnull(@PathVariable("oid") PaymentReferenceCode paymentReferenceCode, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            paymentReferenceCode.anullPaymentReferenceCode();
            addInfoMessage(Constants.treasuryBundle("label.success.update"), model);

        } catch (TreasuryDomainException tex) {
            addErrorMessage(Constants.treasuryBundle("label.error.update") + tex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(Constants.treasuryBundle("label.error.update") + ex.getLocalizedMessage(), model);
        }
        return redirect(READ_URL + paymentReferenceCode.getExternalId(), model, redirectAttributes);

    }
}
