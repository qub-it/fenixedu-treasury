package org.fenixedu.treasury.ui.document.managepayments;

import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.document.reimbursement.ReimbursementProcessStateLog;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@BennuSpringController(value = org.fenixedu.treasury.ui.document.managepayments.SettlementNoteController.class)
@RequestMapping(ReimbursementProcessStateLogController.CONTROLLER_URL)
public class ReimbursementProcessStateLogController {
    public static final String CONTROLLER_URL = "/treasury/document/managepayments/reimbursementprocessstatelog";
    private static final String JSP_PATH = "/treasury/document/managepayments/reimbursementprocessstatelog";

    private static final String _SEARCH_URI = "/search";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI + "/{oid}", method = RequestMethod.GET)
    public String search(@PathVariable("oid") final SettlementNote settlementNote, final Model model) {
        final List<ReimbursementProcessStateLog> logs = settlementNote.getReimbursementProcessStateLogsSet().stream()
                .sorted(ReimbursementProcessStateLog.COMPARE_BY_VERSIONING_DATE.reversed()).collect(Collectors.toList());

        model.addAttribute("settlementNote", settlementNote);
        model.addAttribute("logs", logs);

        return jspPage(_SEARCH_URI);
    }

    private String jspPage(final String page) {
        return JSP_PATH + page;
    }
}
