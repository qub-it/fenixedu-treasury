package org.fenixedu.treasury.ui.administration.forwardpayments;

import java.util.stream.Collectors;

import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.PaymentMethod;
import org.fenixedu.treasury.domain.document.Series;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPaymentConfiguration;
import org.fenixedu.treasury.dto.forwardpayments.ForwardPaymentConfigurationBean;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.administration.managefinantialinstitution.FinantialInstitutionController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@BennuSpringController(FinantialInstitutionController.class)
@RequestMapping(ManageForwardPaymentConfigurationController.CONTROLLER_URL)
public class ManageForwardPaymentConfigurationController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/administration/manageforwardpaymentconfiguration";
    private static final String JSP_PATH = "/treasury/administration/manageforwardpaymentconfiguration";

    private static final String SEARCH_URI = "/search";
    public static final String SEARCH_URL = CONTROLLER_URL + SEARCH_URI;

    @RequestMapping(value = SEARCH_URI + "/{finantialInstitutionId}", method = RequestMethod.GET)
    public String search(@PathVariable("finantialInstitutionId") final FinantialInstitution finantialInstitution,
            final Model model) {

        model.addAttribute("finantialInstitution", finantialInstitution);
        model.addAttribute("forwardPaymentConfigurationsSet", finantialInstitution.getForwardPaymentConfigurationsSet());

        return jspPage(SEARCH_URI);
    }

    private static final String VIEW_URI = "/view";
    public static final String VIEW_URL = CONTROLLER_URL + VIEW_URI;

    @RequestMapping(value = VIEW_URI + "/{finantialInstitutionId}/{forwardPaymentConfigurationId}", method = RequestMethod.GET)
    public String view(@PathVariable("finantialInstitutionId") final FinantialInstitution finantialInstitution,
            @PathVariable("forwardPaymentConfigurationId") final ForwardPaymentConfiguration forwardPaymentConfiguration,
            final Model model) {

        model.addAttribute("finantialInstitution", finantialInstitution);
        model.addAttribute("forwardPaymentConfiguration", forwardPaymentConfiguration);

        return jspPage(VIEW_URI);
    }

    private static final String EDIT_URI = "/edit";
    public static final String EDIT_URL = CONTROLLER_URL + EDIT_URI;

    @RequestMapping(value = EDIT_URI + "/{finantialInstitutionId}/{forwardPaymentConfigurationId}", method = RequestMethod.GET)
    public String edit(@PathVariable("finantialInstitutionId") final FinantialInstitution finantialInstitution,
            @PathVariable("forwardPaymentConfigurationId") final ForwardPaymentConfiguration forwardPaymentConfiguration,
            final Model model) {
        ForwardPaymentConfigurationBean bean = new ForwardPaymentConfigurationBean(forwardPaymentConfiguration);
        return _edit(finantialInstitution, forwardPaymentConfiguration, model, bean);
    }

    private String _edit(final FinantialInstitution finantialInstitution,
            final ForwardPaymentConfiguration forwardPaymentConfiguration, final Model model,
            final ForwardPaymentConfigurationBean bean) {
        model.addAttribute("finantialInstitution", finantialInstitution);
        model.addAttribute("forwardPaymentConfiguration", forwardPaymentConfiguration);
        model.addAttribute("bean", bean);

        model.addAttribute("series_options", Series.findAll());
        model.addAttribute("paymentMethod_options", PaymentMethod.findAll().collect(Collectors.toSet()));

        return jspPage(EDIT_URI);
    }

    @RequestMapping(value = EDIT_URI + "/{finantialInstitutionId}/{forwardPaymentConfigurationId}", method = RequestMethod.POST)
    public String editpost(@PathVariable("finantialInstitutionId") final FinantialInstitution finantialInstitution,
            @PathVariable("forwardPaymentConfigurationId") final ForwardPaymentConfiguration forwardPaymentConfiguration,
            final ForwardPaymentConfigurationBean bean, final Model model, final RedirectAttributes redirectAttributes) {

        try {

            forwardPaymentConfiguration.edit(bean);

            return String.format("redirect:%s/%s/%s", VIEW_URL, finantialInstitution.getExternalId(),
                    forwardPaymentConfiguration.getExternalId());
        } catch (final Exception e) {
            addErrorMessage(e.getLocalizedMessage(), model);
        }

        return jspPage(EDIT_URI);
    }

    private static final String UPLOAD_VIRTUAL_TPA_CERTIFICATE_URI = "/uploadvirtualtpacertificate";
    public static final String UPLOAD_VIRTUAL_TPA_CERTIFICATE_URL = CONTROLLER_URL + UPLOAD_VIRTUAL_TPA_CERTIFICATE_URI;

    @RequestMapping(value = UPLOAD_VIRTUAL_TPA_CERTIFICATE_URI + "/{finantialInstitutionId}/{forwardPaymentConfigurationId}", method = RequestMethod.POST)
    public String uploadvirtualtpacertificatepost(
            @PathVariable("finantialInstitutionId") final FinantialInstitution finantialInstitution,
            @PathVariable("forwardPaymentConfigurationId") final ForwardPaymentConfiguration forwardPaymentConfiguration,
            @RequestParam(value = "certificateFile", required = true) MultipartFile certificateFile, final Model model,
            final RedirectAttributes redirectAttributes) {
        try {

            forwardPaymentConfiguration.saveVirtualTPACertificate(certificateFile.getOriginalFilename(),
                    certificateFile.getBytes());

            return String.format("redirect:%s/%s/%s", VIEW_URL, finantialInstitution.getExternalId(), forwardPaymentConfiguration.getExternalId());
        } catch (final Exception e) {
            addErrorMessage(e.getLocalizedMessage(), model);
        }

        return jspPage(VIEW_URI);
    }

    private static final String DOWNLOAD_VIRTUAL_TPA_CERTIFICATE_URI = "/downloadvirtualtpacertificate";
    public static final String DOWNLOAD_VIRTUAL_TPA_CERTIFICATE_URL = CONTROLLER_URL + DOWNLOAD_VIRTUAL_TPA_CERTIFICATE_URI;

    @RequestMapping(value = DOWNLOAD_VIRTUAL_TPA_CERTIFICATE_URI + "/{finantialInstitutionId}/{forwardPaymentConfigurationId}", method = RequestMethod.POST)
    @ResponseBody
    public Object downloadvirtualtpacertificate(
            @PathVariable("finantialInstitutionId") final FinantialInstitution finantialInstitution,
            @PathVariable("forwardPaymentConfigurationId") final ForwardPaymentConfiguration forwardPaymentConfiguration,
            final Model model) {
        assertUserIsManager(model);
        
        if (forwardPaymentConfiguration.getVirtualTPACertificate() != null) {
            return forwardPaymentConfiguration.getVirtualTPACertificate().getContent();
        }

        return null;
    }

    private String jspPage(final String page) {
        return JSP_PATH + page;
    }

}
