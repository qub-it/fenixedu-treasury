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
package org.fenixedu.treasury.ui.integration.erp;

import java.util.stream.Collectors;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.document.Series;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.integration.ERPConfiguration;
import org.fenixedu.treasury.services.integration.erp.IERPExporter;
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

//@Component("org.fenixedu.treasury.ui.integration.erp") <-- Use for duplicate controller name disambiguation
@BennuSpringController(value = FinantialInstitutionController.class)
@RequestMapping(ERPConfigurationController.CONTROLLER_URL)
public class ERPConfigurationController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/integration/erp/erpconfiguration";

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/";
    }

    private ERPConfiguration getERPConfiguration(Model model) {
        return (ERPConfiguration) model.asMap().get("eRPConfiguration");
    }

    private void setERPConfiguration(ERPConfiguration eRPConfiguration, Model model) {
        model.addAttribute("eRPConfiguration", eRPConfiguration);
    }

    private static final String _READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + _READ_URI;

    @RequestMapping(value = _READ_URI + "{oid}")
    public String read(@PathVariable("oid") ERPConfiguration eRPConfiguration, Model model) {
        setERPConfiguration(eRPConfiguration, model);
        return "treasury/integration/erp/erpconfiguration/read";
    }

    private static final String _UPDATE_URI = "/update/";
    public static final String UPDATE_URL = CONTROLLER_URL + _UPDATE_URI;

    @RequestMapping(value = _UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") ERPConfiguration eRPConfiguration, Model model) {
        model.addAttribute("ERPConfiguration_paymentsIntegrationSeries_options",
                Series.find(eRPConfiguration.getFinantialInstitution()).stream().filter(x -> x.getExternSeries() == true)
                        .filter(x -> x.getActive()).collect(Collectors.toList()));
        setERPConfiguration(eRPConfiguration, model);

        return "treasury/integration/erp/erpconfiguration/update";

    }

    @RequestMapping(value = _UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") ERPConfiguration eRPConfiguration,
            @RequestParam(value = "active", required = false) boolean active,
            @RequestParam(value = "exportannulledrelateddocuments", required = false) boolean exportAnnulledRelatedDocuments,
            @RequestParam(value = "exportonlyrelateddocumentsperexport",
                    required = false) boolean exportOnlyRelatedDocumentsPerExport,
            @RequestParam(value = "externalurl", required = false) String externalURL,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "paymentsintegrationseries", required = false) Series paymentsIntegrationSeries,
            @RequestParam(value = "implementationclassname", required = false) String implementationClassName,
            @RequestParam(value = "maxsizebytestoexportonlineModel", required = false) Long maxSizeBytesToExportOnline,
            Model model, RedirectAttributes redirectAttributes) {

        setERPConfiguration(eRPConfiguration, model);

        try {
            assertUserIsBackOfficeMember(eRPConfiguration.getFinantialInstitution(), model);

            updateERPConfiguration(active, exportOnlyRelatedDocumentsPerExport, exportAnnulledRelatedDocuments, externalURL,
                    username, password, paymentsIntegrationSeries, implementationClassName, maxSizeBytesToExportOnline, model);

            return redirect(READ_URL + getERPConfiguration(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tde) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + tde.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.update") + ex.getLocalizedMessage(), model);
        }
        return update(eRPConfiguration, model);
    }

    @Atomic
    public void updateERPConfiguration(boolean active, boolean exportOnlyRelatedDocumentsPerExport,
            boolean exportAnnulledRelatedDocuments, String externalURL, String username, String password,
            Series paymentsIntegrationSeries, String implementationClassName, Long maxSizeBytesToExportOnline, Model model) {
        getERPConfiguration(model).edit(active, paymentsIntegrationSeries, externalURL, username, password,
                exportAnnulledRelatedDocuments, exportOnlyRelatedDocumentsPerExport, implementationClassName,
                maxSizeBytesToExportOnline);
    }

    @RequestMapping(value = "/update/{oid}/test")
    public String processUpdateToTest(@PathVariable("oid") ERPConfiguration eRPConfiguration, Model model,
            RedirectAttributes redirectAttributes) {
        setERPConfiguration(eRPConfiguration, model);
        try {
            final IERPExporter erpExporter = eRPConfiguration.getERPExternalServiceImplementation().getERPExporter();

            erpExporter.testExportToIntegration(eRPConfiguration.getFinantialInstitution());
            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.sucess.erpconfiguration.test"), model);
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }
        return redirect(READ_URL + getERPConfiguration(model).getExternalId(), model, redirectAttributes);
    }

}
