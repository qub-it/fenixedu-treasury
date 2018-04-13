/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: xpto@qub-it.com
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
package org.fenixedu.treasury.ui.administration.payments.sibs.managesibsconfiguration;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.SibsConfiguration;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.administration.managefinantialinstitution.FinantialInstitutionController;
import org.fenixedu.treasury.util.Constants;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

//@Component("org.fenixedu.treasury.ui.administration.sibs.manageSibsConfiguration") <-- Use for duplicate controller name disambiguation
@BennuSpringController(value = FinantialInstitutionController.class)
@RequestMapping(SibsConfigurationController.CONTROLLER_URL)
public class SibsConfigurationController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/administration/sibs/managesibsconfiguration/sibsconfiguration";

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/";
    }

    private SibsConfiguration getSibsConfiguration(Model model) {
        return (SibsConfiguration) model.asMap().get("sibsConfiguration");
    }

    private void setSibsConfiguration(SibsConfiguration sibsConfiguration, Model model) {
        model.addAttribute("sibsConfiguration", sibsConfiguration);
    }

    private static final String _READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + _READ_URI;

    @RequestMapping(value = _READ_URI + "{oid}")
    public String read(@PathVariable("oid") SibsConfiguration sibsConfiguration, Model model) {
        assertUserIsBackOfficeMember(sibsConfiguration.getFinantialInstitution(), model);
        setSibsConfiguration(sibsConfiguration, model);
        return "treasury/administration/payments/sibs/managesibsconfiguration/sibsconfiguration/read";
    }

    private static final String _UPDATE_URI = "/update/";
    public static final String UPDATE_URL = CONTROLLER_URL + _UPDATE_URI;

    @RequestMapping(value = _UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") SibsConfiguration sibsConfiguration, Model model) {
        assertUserIsBackOfficeMember(sibsConfiguration.getFinantialInstitution(), model);
        setSibsConfiguration(sibsConfiguration, model);

        return "treasury/administration/payments/sibs/managesibsconfiguration/sibsconfiguration/update";
    }

    @RequestMapping(value = _UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") SibsConfiguration sibsConfiguration, @RequestParam(value = "entityreferencecode",
            required = false) String entityReferenceCode,
            @RequestParam(value = "sourceinstitutionid", required = false) String sourceInstitutionId, @RequestParam(
                    value = "destinationinstitutionid", required = false) String destinationInstitutionId, 
            @RequestParam(value = "sibsPaymentsBrokerUrl", required = false) final String sibsPaymentsBrokerUrl,
            @RequestParam(value = "sibsPaymentsBrokerSharedKey", required = false) final String sibsPaymentsBrokerSharedKey,
            Model model, RedirectAttributes redirectAttributes) {

        setSibsConfiguration(sibsConfiguration, model);

        try {
            assertUserIsFrontOfficeMember(sibsConfiguration.getFinantialInstitution(), model);

            assertUserIsBackOfficeMember(sibsConfiguration.getFinantialInstitution(), model);
            getSibsConfiguration(model).edit(entityReferenceCode, sourceInstitutionId, destinationInstitutionId, sibsPaymentsBrokerUrl,
                    sibsPaymentsBrokerSharedKey);

            return redirect(READ_URL + getSibsConfiguration(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(Constants.treasuryBundle("label.error.update") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(Constants.treasuryBundle("label.error.update") + ex.getLocalizedMessage(), model);
        }
        return update(sibsConfiguration, model);
    }
    
}
