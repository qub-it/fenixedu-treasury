package org.fenixedu.treasury.ui.document.forwardpayments;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Strings;

@SpringFunctionality(app = TreasuryController.class, title = "label.ManageForwardPayments.functionality",
        accessGroup = "treasuryBackOffice")
@RequestMapping(ManageForwardPaymentsController.CONTROLLER_URL)
public class ManageForwardPaymentsController extends TreasuryBaseController {

    private static final int MAX_SEARCH_SIZE = 3000;
    public static final String CONTROLLER_URL = "/treasury/document/forwardpayments/management";
    private static final String JSP_PATH = "/treasury/document/forwardpayments/management";

    @RequestMapping
    public String home() {
        return "redirect:" + SEARCH_URL;
    }

    public static final String SEARCH_URI = "/search";
    public static final String SEARCH_URL = CONTROLLER_URL + SEARCH_URI;

    @RequestMapping(value=SEARCH_URI, method=RequestMethod.GET)
    public String search(
            @RequestParam(value = "beginDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate beginDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate endDate,
            @RequestParam(value = "customerName", required = false) final String customerName,
            @RequestParam(value = "customerBusinessId", required = false) final String customerBusinessId, final Model model) {

        Stream<ForwardPayment> stream = Bennu.getInstance().getForwardPaymentsSet().stream();

        if (beginDate != null) {
            stream = stream.filter(i -> !i.getWhenOccured().isBefore(beginDate.toDateTimeAtStartOfDay()));
        }

        if (endDate != null) {
            stream = stream
                    .filter(i -> !i.getWhenOccured().isAfter(endDate.toDateTimeAtStartOfDay().plusDays(1).minusSeconds(1)));
        }

        if (!Strings.isNullOrEmpty(customerName)) {
            stream = stream
                    .filter(i -> Constants.stringNormalizedContains(i.getDebtAccount().getCustomer().getName(), customerName));
        }

        if (!Strings.isNullOrEmpty(customerBusinessId)) {
            stream = stream.filter(i -> i.getDebtAccount().getCustomer().getBusinessIdentification().equals(customerBusinessId));
        }

        List<ForwardPayment> forwardPayments =
                stream.sorted(java.util.Collections.reverseOrder(Comparator.comparing(ForwardPayment::getWhenOccured)))
                        .collect(Collectors.toList());
        
        if(forwardPayments.size() > MAX_SEARCH_SIZE) {
            forwardPayments = forwardPayments.subList(0, MAX_SEARCH_SIZE);
        }

        model.addAttribute("forwardPayments", forwardPayments);

        return jspPage(SEARCH_URI);
    }

    public static final String VIEW_URI = "/view";
    public static final String VIEW_URL = CONTROLLER_URL + VIEW_URI;

    @RequestMapping(VIEW_URI + "/{forwardPaymentId}")
    public String view(@PathVariable("forwardPaymentId") final ForwardPayment forwardPayment, final Model model) {
        model.addAttribute("forwardPayment", forwardPayment);
        
        return jspPage(VIEW_URI);
    }

    public static final String EXPORT_URI = "/export";
    public static final String EXPORT_URL = CONTROLLER_URL + EXPORT_URI;

    @RequestMapping(value = EXPORT_URI, method = RequestMethod.GET)
    @ResponseBody
    public void export() {

    }

    private String jspPage(final String mapping) {
        return JSP_PATH + mapping;
    }

}
