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
package org.fenixedu.treasury.ui.administration.payments.sibs.managepaymentcodepool;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.PaymentMethod;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

//@Component("org.fenixedu.treasury.ui.administration.payments.sibs.managePaymentCodePool") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.administration.payments.sibs.managePaymentCodePool",
        accessGroup = "treasuryManagers")
@RequestMapping(PaymentCodePoolController.CONTROLLER_URL)
public class PaymentCodePoolController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/administration/payments/sibs/managepaymentcodepool/paymentcodepool";

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/";
    }

    private PaymentCodePool getPaymentCodePool(Model model) {
        return (PaymentCodePool) model.asMap().get("paymentCodePool");
    }

    private void setPaymentCodePool(PaymentCodePool paymentCodePool, Model model) {
        model.addAttribute("paymentCodePool", paymentCodePool);
    }

    @Atomic
    public void deletePaymentCodePool(PaymentCodePool paymentCodePool) {
        // paymentCodePool.delete();
    }

//				
    private static final String _SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI)
    public String search(
            @RequestParam(value = "finantialinstitution", required = false) org.fenixedu.treasury.domain.FinantialInstitution finantialInstitution,
            Model model) {
        List<PaymentCodePool> searchpaymentcodepoolResultsDataSet = filterSearchPaymentCodePool(finantialInstitution);

        //add the results dataSet to the model
        model.addAttribute("searchpaymentcodepoolResultsDataSet", searchpaymentcodepoolResultsDataSet);
        model.addAttribute("PaymentCodePool_finantialInstitution_options",
                FinantialInstitution.findAll().collect(Collectors.toList()));

        return "treasury/administration/payments/sibs/managepaymentcodepool/paymentcodepool/search";
    }

    private Stream<PaymentCodePool> getSearchUniverseSearchPaymentCodePoolDataSet() {
        return PaymentCodePool.findAll();
    }

    private List<PaymentCodePool> filterSearchPaymentCodePool(
            org.fenixedu.treasury.domain.FinantialInstitution finantialInstitution) {

        return getSearchUniverseSearchPaymentCodePoolDataSet().filter(
                paymentCodePool -> finantialInstitution == null
                        || finantialInstitution == paymentCodePool.getFinantialInstitution()).collect(Collectors.toList());
    }

    private static final String _SEARCH_TO_VIEW_ACTION_URI = "/search/view/";
    public static final String SEARCH_TO_VIEW_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_VIEW_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_VIEW_ACTION_URI + "{oid}")
    public String processSearchToViewAction(@PathVariable("oid") PaymentCodePool paymentCodePool, Model model,
            RedirectAttributes redirectAttributes) {
        return redirect(READ_URL + paymentCodePool.getExternalId(), model, redirectAttributes);
    }

    private static final String _READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + _READ_URI;

    @RequestMapping(value = _READ_URI + "{oid}")
    public String read(@PathVariable("oid") PaymentCodePool paymentCodePool, Model model) {
        setPaymentCodePool(paymentCodePool, model);
        return "treasury/administration/payments/sibs/managepaymentcodepool/paymentcodepool/read";
    }

//
    private static final String _DELETE_URI = "/delete/";
    public static final String DELETE_URL = CONTROLLER_URL + _DELETE_URI;

    @RequestMapping(value = _DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") PaymentCodePool paymentCodePool, Model model, RedirectAttributes redirectAttributes) {

        setPaymentCodePool(paymentCodePool, model);
        try {
            assertUserIsFrontOfficeMember(paymentCodePool.getFinantialInstitution(), model);

            deletePaymentCodePool(paymentCodePool);

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.delete"), model);
            return redirect("/treasury/administration/payments/sibs/managepaymentcodepool/paymentcodepool/", model,
                    redirectAttributes);
        } catch (TreasuryDomainException tex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + tex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + ex.getLocalizedMessage(), model);
        }

        return "treasury/administration/payments/sibs/managepaymentcodepool/paymentcodepool/read/"
                + getPaymentCodePool(model).getExternalId();
    }

    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {
        model.addAttribute("finantialInstitutionList", FinantialInstitution.findAll().collect(Collectors.toList()));

        model.addAttribute(
                "PaymentCodePool_documentSeriesForPayments_options",
                DocumentNumberSeries.findAll()
                        .filter(x -> x.getFinantialDocumentType().equals(FinantialDocumentType.findForSettlementNote()))
                        .filter(x -> x.getSeries().getActive()).collect(Collectors.toList()));

        model.addAttribute("PaymentCodePool_paymentMethod_options", PaymentMethod.findAll().collect(Collectors.toList()));
        return "treasury/administration/payments/sibs/managepaymentcodepool/paymentcodepool/create";
    }

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "finantialinstitution") FinantialInstitution finantialInstitution, @RequestParam(
            value = "name") String name,
            @RequestParam(value = "entityreferencecode", required = false) String entityReferenceCode, @RequestParam(
                    value = "minreferencecode", required = false) Long minReferenceCode, @RequestParam(
                    value = "maxreferencecode", required = false) Long maxReferenceCode, @RequestParam(value = "minamount",
                    required = false) BigDecimal minAmount,
            @RequestParam(value = "maxamount", required = false) BigDecimal maxAmount, @RequestParam(value = "validfrom",
                    required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate validFrom, @RequestParam(
                    value = "validto", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate validTo,
            @RequestParam(value = "active", required = false) Boolean active, @RequestParam(value = "usecheckdigit",
                    required = false) Boolean useCheckDigit,
            @RequestParam(value = "documentnumberseries") DocumentNumberSeries documentNumberSeries, @RequestParam(
                    value = "paymentmethod") PaymentMethod paymentMethod, Model model, RedirectAttributes redirectAttributes) {

        try {
            assertUserIsFrontOfficeMember(finantialInstitution, model);

            PaymentCodePool paymentCodePool =
                    createPaymentCodePool(finantialInstitution, name, entityReferenceCode, minReferenceCode, maxReferenceCode,
                            minAmount, maxAmount, validFrom, validTo, active, useCheckDigit, documentNumberSeries, paymentMethod);

            model.addAttribute("paymentCodePool", paymentCodePool);
            return redirect(READ_URL + getPaymentCodePool(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + tex.getLocalizedMessage(), model);
        } catch (Exception de) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + de.getLocalizedMessage(), model);
        }

        return create(model);
    }

    @Atomic
    public PaymentCodePool createPaymentCodePool(org.fenixedu.treasury.domain.FinantialInstitution finantialInstitution,
            String name, String entityReferenceCode, Long minReferenceCode, Long maxReferenceCode, BigDecimal minAmount,
            BigDecimal maxAmount, LocalDate validFrom, LocalDate validTo, Boolean active, Boolean useCheckDigit,
            DocumentNumberSeries documentNumberSeries, PaymentMethod paymentMethod) {

        PaymentCodePool paymentCodePool =
                PaymentCodePool.create(name, entityReferenceCode, minReferenceCode, maxReferenceCode, minAmount, maxAmount,
                        validFrom, validTo, active, useCheckDigit, finantialInstitution, documentNumberSeries, paymentMethod);

        return paymentCodePool;
    }

    private static final String _UPDATE_URI = "/update/";
    public static final String UPDATE_URL = CONTROLLER_URL + _UPDATE_URI;

    @RequestMapping(value = _UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") PaymentCodePool paymentCodePool, Model model) {
        model.addAttribute("finantialInstitutionList", FinantialInstitution.findAll().collect(Collectors.toList()));

        model.addAttribute(
                "PaymentCodePool_documentSeriesForPayments_options",
                DocumentNumberSeries.findAll()
                        .filter(x -> x.getFinantialDocumentType().equals(FinantialDocumentType.findForSettlementNote()))
                        .collect(Collectors.toList()));

        model.addAttribute("PaymentCodePool_paymentMethod_options", PaymentMethod.findAll().collect(Collectors.toList()));

        setPaymentCodePool(paymentCodePool, model);

        return "treasury/administration/payments/sibs/managepaymentcodepool/paymentcodepool/update";

    }

    @RequestMapping(value = _UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") PaymentCodePool paymentCodePool, @RequestParam(value = "finantialinstitution",
            required = false) FinantialInstitution finantialInstitution,
            @RequestParam(value = "name", required = false) String name, @RequestParam(value = "entityreferencecode",
                    required = false) String entityReferenceCode,
            @RequestParam(value = "minreferencecode", required = false) Long minReferenceCode, @RequestParam(
                    value = "maxreferencecode", required = false) Long maxReferenceCode, @RequestParam(value = "minamount",
                    required = false) BigDecimal minAmount,
            @RequestParam(value = "maxamount", required = false) BigDecimal maxAmount, @RequestParam(value = "validfrom",
                    required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate validFrom, @RequestParam(
                    value = "validto", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate validTo,
            @RequestParam(value = "active", required = false) Boolean active, @RequestParam(value = "usecheckdigit",
                    required = false) Boolean useCheckDigit,
            @RequestParam(value = "documentnumberseries") DocumentNumberSeries documentNumberSeries, @RequestParam(
                    value = "paymentmethod") PaymentMethod paymentMethod,

            Model model, RedirectAttributes redirectAttributes) {

        setPaymentCodePool(paymentCodePool, model);

        try {
            assertUserIsFrontOfficeMember(finantialInstitution, model);

            updatePaymentCodePool(finantialInstitution, name, entityReferenceCode, minReferenceCode, maxReferenceCode, minAmount,
                    maxAmount, validFrom, validTo, active, useCheckDigit, documentNumberSeries, paymentMethod, model);

            return redirect(READ_URL + getPaymentCodePool(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + tex.getLocalizedMessage(), model);
        } catch (Exception de) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + de.getLocalizedMessage(), model);
        }
        return update(paymentCodePool, model);
    }

    @Atomic
    public void updatePaymentCodePool(FinantialInstitution finantialInstitution, String name, String entityReferenceCode,
            Long minReferenceCode, Long maxReferenceCode, BigDecimal minAmount, BigDecimal maxAmount, LocalDate validFrom,
            LocalDate validTo, Boolean active, Boolean useCheckDigit, DocumentNumberSeries seriesToUseInPayments,
            PaymentMethod paymentMethod, Model model) {

        getPaymentCodePool(model).edit(name, active, seriesToUseInPayments, paymentMethod);
        getPaymentCodePool(model).setNewValidPeriod(validFrom, validTo);
        getPaymentCodePool(model).changeFinantialInstitution(finantialInstitution);
        getPaymentCodePool(model).changePooltype(useCheckDigit);
        getPaymentCodePool(model).changeReferenceCode(entityReferenceCode, minReferenceCode, maxReferenceCode);
        getPaymentCodePool(model).changeAmount(minAmount, maxAmount);
    }
}
