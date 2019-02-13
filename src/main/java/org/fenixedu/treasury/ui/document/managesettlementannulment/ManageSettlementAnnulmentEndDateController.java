package org.fenixedu.treasury.ui.document.managesettlementannulment;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.FiscalYear;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Lists;

@SpringFunctionality(app = TreasuryController.class, title = "label.ManageSettlementAnnulmentEndDateController.title", accessGroup = "treasuryManagers")
@RequestMapping(ManageSettlementAnnulmentEndDateController.CONTROLLER_URL)
public class ManageSettlementAnnulmentEndDateController extends TreasuryBaseController {
    
    public static final String CONTROLLER_URL = "/treasury/document/managesettlementannulmentenddate";
    private static final String JSP_PATH = "treasury/document/managesettlementannulmentenddate";

    @RequestMapping
    public String home(Model model, final RedirectAttributes redirectAttributes) {
        if(FinantialInstitution.findAll().count() == 1) {
            final FinantialInstitution institution = FinantialInstitution.findAll().iterator().next();
            
            return redirect(SEARCH_URL + "/" + institution.getExternalId(), model, redirectAttributes);
        }
        
        return redirect(SEARCH_URL, model, redirectAttributes);
    }
    
    private static final String _SEARCH_URI = "/search";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI)
    public String search(final Model model, final RedirectAttributes redirectAttributes) {
        if(FinantialInstitution.findAll().count() == 1) {
            final FinantialInstitution institution = FinantialInstitution.findAll().iterator().next();
            
            return redirect(SEARCH_URL + "/" + institution.getExternalId(), model, redirectAttributes);
        }
        
        return  jspPage(_SEARCH_URI);
    }
    
    @RequestMapping(value = _SEARCH_URI + "/{finantialInstitutionId}")
    public String search(@PathVariable("finantialInstitutionId") final FinantialInstitution finantialInstitution, final Model model, final RedirectAttributes redirectAttributes) {
        model.addAttribute("finantialInstitutionOptions", FinantialInstitution.findAll().collect(Collectors.toSet()));
        model.addAttribute("finantialInstitution", finantialInstitution);
        
        List<FiscalYear> result = Lists.newArrayList(finantialInstitution.getFiscalYearsSet());
        Collections.sort(result, FiscalYear.COMPARE_BY_YEAR.reversed());
        
        model.addAttribute("result", result);
        return  jspPage(_SEARCH_URI);
    }
    
    private static final String _UPDATE_URI = "/update";
    public static final String UPDATE_URL = CONTROLLER_URL + _UPDATE_URI;
    
    @RequestMapping(value = _UPDATE_URI + "/{finantialInstitutionId}/{fiscalYearId}")
    public String update(@PathVariable("finantialInstitutionId") final FinantialInstitution finantialInstitution, 
            @PathVariable("fiscalYearId") final FiscalYear fiscalYear, final Model model) {
        
        model.addAttribute("finantialInstitution", finantialInstitution);
        model.addAttribute("fiscalYear", fiscalYear);
        
        return jspPage(_UPDATE_URI);
    }
    
    @RequestMapping(value = _UPDATE_URI + "/{finantialInstitutionId}/{fiscalYearId}", method=RequestMethod.POST)
    public String updatepost(@PathVariable("finantialInstitutionId") final FinantialInstitution finantialInstitution, @PathVariable("fiscalYearId") final FiscalYear fiscalYear, 
            @RequestParam("settlementAnnulmentEndDate") @DateTimeFormat(iso = ISO.DATE) final LocalDate settlementAnnulmentEndDate, final Model model, final RedirectAttributes redirectAttributes) {

        try {
            fiscalYear.editSettlementAnnulmentEndDate(settlementAnnulmentEndDate);
            
            return redirect(SEARCH_URL + "/" + finantialInstitution.getExternalId(), model, redirectAttributes);
        } catch(final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
            
            return jspPage(_UPDATE_URI);
        }
        
    }
    
    private String jspPage(final String mapping) {
        return JSP_PATH + mapping;
    }


}
