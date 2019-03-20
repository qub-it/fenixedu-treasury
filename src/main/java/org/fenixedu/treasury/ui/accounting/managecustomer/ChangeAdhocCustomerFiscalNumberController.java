package org.fenixedu.treasury.ui.accounting.managecustomer;

import static org.fenixedu.treasury.util.TreasuryConstants.treasuryBundle;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.AdhocCustomer;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.dto.AdhocCustomerBean;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.util.TreasuryConstants;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Strings;

@BennuSpringController(value = CustomerController.class)
@RequestMapping(ChangeAdhocCustomerFiscalNumberController.CONTROLLER_URI)
public class ChangeAdhocCustomerFiscalNumberController extends TreasuryBaseController {

    public static final String CONTROLLER_URI = "/treasury/accounting/managecustomer/changefiscalnumber";
    private static final String JSP_PATH = "/treasury/accounting/managecustomer/changefiscalnumber";

    private static final String CHANGE_FISCAL_NUMBER_ACTION_CONFIRM_URI = "/changefiscalnumberactionconfirm";
    public static final String CHANGE_FISCAL_NUMBER_ACTION_CONFIRM_URL = CONTROLLER_URI + CHANGE_FISCAL_NUMBER_ACTION_CONFIRM_URI;

    protected String getControllerURI() {
        return CONTROLLER_URI;
    }

    @RequestMapping(value = CHANGE_FISCAL_NUMBER_ACTION_CONFIRM_URI + "/{oid}", method = RequestMethod.GET)
    public String changefiscalnumberactionconfirm(@PathVariable("oid") final AdhocCustomer customer, final Model model) {
        assertUserIsBackOfficeMember(model);

        model.addAttribute("customer", customer);
        model.addAttribute("changeFiscalNumberActionFormURI", getControllerURI());

        if (customer.isFiscalValidated() && customer.isFiscalCodeValid()) {
            model.addAttribute("fiscalNumberValid", true);
        }

        return jspPage(CHANGE_FISCAL_NUMBER_ACTION_CONFIRM_URI);
    }

    private static final String CHANGE_FISCAL_NUMBER_FORM_URI = "/changefiscalnumberform";
    public static final String CHANGE_FISCAL_NUMBER_FORM_URL = CONTROLLER_URI + CHANGE_FISCAL_NUMBER_FORM_URI;

    @RequestMapping(value = CHANGE_FISCAL_NUMBER_FORM_URI + "/{oid}", method = RequestMethod.POST)
    public String changefiscalnumberform(@PathVariable("oid") final Customer customer, final Model model) {
        assertUserIsBackOfficeMember(model);

        final AdhocCustomerBean bean = new AdhocCustomerBean(customer);

        return _changefiscalnumberactionconfirm(customer, model, bean);
    }

    private String _changefiscalnumberactionconfirm(final Customer customer, final Model model, final AdhocCustomerBean bean) {
        model.addAttribute("customer", customer);
        model.addAttribute("customerBean", bean);
        model.addAttribute("customerBeanJson", getBeanJson(bean));

        return jspPage(CHANGE_FISCAL_NUMBER_FORM_URI);
    }

    private static final String CHANGE_POSTBACK_URI = "/changepostback";
    public static final String CHANGE_POSTBACK_URL = CONTROLLER_URI + CHANGE_POSTBACK_URI;
    
    @RequestMapping(value = CHANGE_POSTBACK_URI + "/{oid}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody String changepostback(@PathVariable("oid") final AdhocCustomer customer, @RequestParam("bean") final AdhocCustomerBean bean, final Model model) {
        bean.update();

        return getBeanJson(bean);
    }
    
    private static final String CHANGE_FISCAL_NUMBER_URI = "/change";
    public static final String CHANGE_FISCAL_NUMBER_URL = CONTROLLER_URI + CHANGE_FISCAL_NUMBER_URI;

    @RequestMapping(value = CHANGE_FISCAL_NUMBER_URI + "/{oid}", method = RequestMethod.POST)
    public String change(@PathVariable("oid") final AdhocCustomer adhocCustomer, @RequestParam("bean") final AdhocCustomerBean bean,
            final Model model) {
        assertUserIsBackOfficeMember(model);

        try {

            if (!bean.isChangeFiscalNumberConfirmed()) {
                throw new TreasuryDomainException("message.Customer.changeFiscalNumber.confirmation");
            }
         
            if(Strings.isNullOrEmpty(bean.getAddressCountryCode())) {
                addErrorMessage(treasuryBundle("error.Customer.addressCountryCode.required"), model);
                return _changefiscalnumberactionconfirm(adhocCustomer, model, bean);
            }
            
            if(!bean.isAddressValid()) {
                throw new TreasuryDomainException("error.AdhocCustomer.fill.required.address.fields");
            }
            
            adhocCustomer.changeFiscalNumber(bean);

            return "forward:" + CustomerController.READ_URL + adhocCustomer.getExternalId();
        } catch (final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);

            return _changefiscalnumberactionconfirm(adhocCustomer, model, bean);
        }
    }

    private String jspPage(final String page) {
        return JSP_PATH + page;
    }

}
