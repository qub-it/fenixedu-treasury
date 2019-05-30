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
package org.fenixedu.treasury.ui.document.managepayments;

import static org.fenixedu.treasury.util.TreasuryConstants.treasuryBundle;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.TreasuryOperationLog;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.document.FinantialDocumentTypeEnum;
import org.fenixedu.treasury.domain.document.Series;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.FinantialDocumentPaymentCode;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCode;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;
import org.fenixedu.treasury.dto.document.managepayments.PaymentReferenceCodeBean;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.accounting.managecustomer.CustomerController;
import org.fenixedu.treasury.ui.administration.base.managelog.TreasuryOperationLogController;
import org.fenixedu.treasury.ui.document.manageinvoice.DebitNoteController;
import org.fenixedu.treasury.util.TreasuryConstants;
import org.joda.time.LocalDate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

//@Component("org.fenixedu.treasury.ui.document.managePayments") <-- Use for duplicate controller name disambiguation
@BennuSpringController(value = CustomerController.class)
@RequestMapping(PaymentReferenceCodeController.CONTROLLER_URL)
public class PaymentReferenceCodeController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/document/managepayments/paymentreferencecode";

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/";
    }

    private void setPaymentReferenceCodeBean(PaymentReferenceCodeBean bean, Model model) {
        model.addAttribute("paymentReferenceCodeBeanJson", getBeanJson(bean));
        model.addAttribute("paymentReferenceCodeBean", bean);
    }

    @Atomic
    public void deletePaymentReferenceCode(PaymentReferenceCode paymentReferenceCode) {
    }

    private static final String _CREATEPAYMENTCODEINDEBITNOTE_URI = "/createpaymentcodeindebitnote";
    public static final String CREATEPAYMENTCODEINDEBITNOTE_URL = CONTROLLER_URL + _CREATEPAYMENTCODEINDEBITNOTE_URI;

    @RequestMapping(value = _CREATEPAYMENTCODEINDEBITNOTE_URI, method = RequestMethod.GET)
    public String createpaymentcodeindebitnote(@RequestParam(value = "debitnote") DebitNote debitNote, Model model,
            RedirectAttributes redirectAttributes) {

        if (debitNote.getPaymentCodesSet().stream().anyMatch(pc -> pc.getPaymentReferenceCode().getState().isUsed())) {
            addErrorMessage(treasuryBundle("label.paymentreferencecode.already.has.one"), model);
            return redirect(DebitNoteController.READ_URL + debitNote.getExternalId(), model, redirectAttributes);
        }

        try {
            assertUserIsFrontOfficeMember(debitNote.getDocumentNumberSeries().getSeries().getFinantialInstitution(), model);

            PaymentReferenceCodeBean bean = new PaymentReferenceCodeBean();
            bean.setDebitNote(debitNote);
            bean.setPaymentAmount(debitNote.getOpenAmount());
            bean.setPaymentAmountWithInterst(debitNote.getOpenAmountWithInterests());
            bean.setUsePaymentAmountWithInterests(false);
            List<PaymentCodePool> activePools =
                    debitNote.getDebtAccount().getFinantialInstitution().getPaymentCodePoolsSet().stream()
                            .filter(x -> Boolean.TRUE.equals(x.getActive())).collect(Collectors.toList());
            bean.setPaymentCodePoolDataSource(activePools);
            bean.setBeginDate(new LocalDate());
            if (debitNote.getDocumentDueDate().isBefore(bean.getBeginDate())) {
                bean.setEndDate(new LocalDate());
            } else {
                bean.setEndDate(debitNote.getDocumentDueDate());
            }

            this.setPaymentReferenceCodeBean(bean, model);

        } catch (TreasuryDomainException tde) {
            addErrorMessage(treasuryBundle("label.error.delete") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(treasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);
        }

        return "treasury/document/managepayments/paymentreferencecode/createpaymentcodeindebitnote";
    }

    private static final String _CREATEPAYMENTCODEINDEBITNOTEPOSTBACK_URI = "/createpaymentcodeindebitnotepostback";
    public static final String CREATEPAYMENTCODEINDEBITNOTEPOSTBACK_URL = CONTROLLER_URL
            + _CREATEPAYMENTCODEINDEBITNOTEPOSTBACK_URI;

    @RequestMapping(value = _CREATEPAYMENTCODEINDEBITNOTEPOSTBACK_URI, method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public @ResponseBody String createpaymentcodeindebitnotepostback(
            @RequestParam(value = "bean", required = false) PaymentReferenceCodeBean bean, Model model) {
        this.setPaymentReferenceCodeBean(bean, model);
        bean.setPoolWithFixedAmount(bean.getPaymentCodePool().getIsFixedAmount());
        bean.setPoolVariableTimeWindow(bean.getPaymentCodePool().getIsVariableTimeWindow());
        return getBeanJson(bean);
    }

    @RequestMapping(value = _CREATEPAYMENTCODEINDEBITNOTE_URI, method = RequestMethod.POST)
    public String createpaymentcodeindebitnote(@RequestParam(value = "bean", required = false) PaymentReferenceCodeBean bean,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(bean.getDebitNote().getDocumentNumberSeries().getSeries().getFinantialInstitution(),
                    model);

            BigDecimal payableAmount = bean.getDebitNote().getOpenAmount();

            if (!TreasuryConstants.isEqual(bean.getPaymentAmount(), payableAmount)) {
                throw new TreasuryDomainException("error.PaymentReferenceCode.error.in.payment.amount.not.consistent");
            }
            if (bean.isUsePaymentAmountWithInterests()) {
                payableAmount = bean.getDebitNote().getOpenAmount();
                if (!TreasuryConstants.isEqual(bean.getPaymentAmountWithInterst(), payableAmount)) {
                    throw new TreasuryDomainException(
                            "error.PaymentReferenceCode.error.in.payment.amount.with.interests.not.consistent");
                }
            }

            final PaymentReferenceCode paymentReferenceCode = createPaymentReferenceCode(bean, payableAmount);
            addInfoMessage(treasuryBundle("label.document.managepayments.success.create.reference.code.debitnote"), model);

            model.addAttribute("paymentReferenceCode", paymentReferenceCode);
            return redirect(DebitNoteController.READ_URL + bean.getDebitNote().getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(treasuryBundle("label.error.create") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(treasuryBundle("label.error.create") + ex.getLocalizedMessage(), model);
        }
        this.setPaymentReferenceCodeBean(bean, model);
        return "treasury/document/managepayments/paymentreferencecode/createpaymentcodeindebitnote";
    }

    @Atomic
    @Deprecated
    // DELETE CODE
    private PaymentReferenceCode createPaymentReferenceCode(PaymentReferenceCodeBean bean, BigDecimal payableAmount) {
        throw new RuntimeException("deprecated");
    }

    //Create Payment reference code for DebitNote Series

    private static final String _CREATEPAYMENTCODEINSERIES_URI = "/createpaymentcodeinseries";
    public static final String CREATEPAYMENTCODEINSERIES_URL = CONTROLLER_URL + _CREATEPAYMENTCODEINSERIES_URI;

    @RequestMapping(value = _CREATEPAYMENTCODEINSERIES_URI, method = RequestMethod.GET)
    public String createpaymentcodeInSeries(@RequestParam(value = "series") Series series, Model model,
            RedirectAttributes redirectAttributes) {
        PaymentReferenceCodeBean paymentReferenceCodeBean = new PaymentReferenceCodeBean();
        paymentReferenceCodeBean.setMinAmount(BigDecimal.valueOf(5.0));
        paymentReferenceCodeBean.setPaymentCodePoolDataSource(series.getFinantialInstitution().getPaymentCodePoolsSet().stream()
                .filter(x -> x.getActive()).collect(Collectors.toList()));

        paymentReferenceCodeBean.setBeginDate(new LocalDate());
        paymentReferenceCodeBean.setEndDate(new LocalDate());

        this.setPaymentReferenceCodeBean(paymentReferenceCodeBean, model);
        model.addAttribute("series", series);
        return "treasury/document/managepayments/paymentreferencecode/createpaymentcodeinseries";
    }

    private static final String _CREATEPAYMENTCODEINSERIESPOSTBACK_URI = "/createpaymentcodeinseriespostback";
    public static final String CREATEPAYMENTCODEINSERIESPOSTBACK_URL = CONTROLLER_URL + _CREATEPAYMENTCODEINSERIESPOSTBACK_URI;

    @RequestMapping(value = _CREATEPAYMENTCODEINSERIESPOSTBACK_URI, method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public @ResponseBody String createpaymentcodeinseriespostback(
            @RequestParam(value = "bean", required = false) PaymentReferenceCodeBean bean, Model model) {
        this.setPaymentReferenceCodeBean(bean, model);
        bean.setPoolWithFixedAmount(bean.getPaymentCodePool().getIsFixedAmount());
        bean.setPoolVariableTimeWindow(bean.getPaymentCodePool().getIsVariableTimeWindow());
        return getBeanJson(bean);
    }

    @RequestMapping(value = _CREATEPAYMENTCODEINSERIES_URI, method = RequestMethod.POST)
    @Deprecated
    public String createpaymentcodeinseries(@RequestParam(value = "series") Series series, @RequestParam(value = "bean",
            required = false) PaymentReferenceCodeBean bean, Model model, RedirectAttributes redirectAttributes) {
        throw new RuntimeException("deprecated");
    }
    
}
