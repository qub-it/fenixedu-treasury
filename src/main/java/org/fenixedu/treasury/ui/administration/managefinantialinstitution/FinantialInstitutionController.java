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
package org.fenixedu.treasury.ui.administration.managefinantialinstitution;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.StringNormalizer;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.integration.ERPConfiguration;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.domain.paymentcodes.SibsConfiguration;
import org.fenixedu.treasury.dto.FinantialInstitutionBean;
import org.fenixedu.treasury.services.integration.erp.ERPExporter;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.ui.administration.payments.sibs.managesibsconfiguration.SibsConfigurationController;
import org.fenixedu.treasury.ui.integration.erp.ERPConfigurationController;
import org.fenixedu.treasury.ui.integration.erp.ERPExportOperationController;
import org.fenixedu.treasury.util.Constants;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

//@Component("org.fenixedu.treasury.ui.administration.manageFinantialInstitution") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.administration.manageFinantialInstitution",
        accessGroup = "treasuryManagers")
@RequestMapping(FinantialInstitutionController.CONTROLLER_URL)
public class FinantialInstitutionController extends TreasuryBaseController {
    public static final String CONTROLLER_URL = "/treasury/administration/managefinantialinstitution/finantialinstitution";
    private static final String SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + SEARCH_URI;
    private static final String UPDATE_URI = "/update/";
    public static final String UPDATE_URL = CONTROLLER_URL + UPDATE_URI;
    private static final String CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + CREATE_URI;
    private static final String READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + READ_URI;
    private static final String DELETE_URI = "/delete/";
    public static final String DELETE_URL = CONTROLLER_URL + DELETE_URI;

    @RequestMapping
    public String home(Model model) {
        return "forward:" + SEARCH_URL;
    }

    private FinantialInstitutionBean getFinantialInstitutionBean(Model model) {
        return (FinantialInstitutionBean) model.asMap().get("finantialInstitutionBean");
    }

    private void setFinantialInstitutionBean(FinantialInstitutionBean bean, Model model) {
        bean.updateModelLists();
        model.addAttribute("finantialInstitutionBeanJson", getBeanJson(bean));
        model.addAttribute("finantialInstitutionBean", bean);
    }

    private FinantialInstitution getFinantialInstitution(Model m) {
        return (FinantialInstitution) m.asMap().get("finantialInstitution");
    }

    private void setFinantialInstitution(FinantialInstitution finantialInstitution, Model m) {
        m.addAttribute("finantialInstitution", finantialInstitution);
    }

    @Atomic
    public void deleteFinantialInstitution(FinantialInstitution finantialInstitution) {
        finantialInstitution.delete();
    }

    @RequestMapping(value = SEARCH_URI)
    public String search(Model model) {
        List<FinantialInstitution> searchfinantialinstitutionResultsDataSet =
                getSearchUniverseSearchFinantialInstitutionDataSet();

        //add the results dataSet to the model
        model.addAttribute("searchfinantialinstitutionResultsDataSet", searchfinantialinstitutionResultsDataSet);
        return "treasury/administration/managefinantialinstitution/finantialinstitution/search";
    }

    private List<FinantialInstitution> getSearchUniverseSearchFinantialInstitutionDataSet() {
        return new ArrayList<FinantialInstitution>(Bennu.getInstance().getFinantialInstitutionsSet());
    }

    private static final String SEARCH_TO_VIEW_ACTION_URI = "/search/view/";
    public static final String SEARCH_TO_VIEW_ACTION_URL = CONTROLLER_URL + SEARCH_TO_VIEW_ACTION_URI;

    @RequestMapping(value = SEARCH_TO_VIEW_ACTION_URI + "{oid}")
    public String processSearchToViewAction(@PathVariable("oid") FinantialInstitution finantialInstitution, Model model) {
        return "redirect:" + READ_URL + finantialInstitution.getExternalId();
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") FinantialInstitution finantialInstitution, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(finantialInstitution, model);
            setFinantialInstitution(finantialInstitution, model);
            model.addAttribute("finantialDocumentTypeSet", FinantialDocumentType.findAll().collect(Collectors.toList()));
            return "treasury/administration/managefinantialinstitution/finantialinstitution/read";
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }
        return redirect(FinantialInstitutionController.SEARCH_URL, model, redirectAttributes);
    }

    @RequestMapping(value = DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") FinantialInstitution finantialInstitution, Model model,
            RedirectAttributes redirectAttributes) {

        setFinantialInstitution(finantialInstitution, model);
        try {
            assertUserIsFrontOfficeMember(finantialInstitution, model);

            deleteFinantialInstitution(finantialInstitution);

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.delete"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (TreasuryDomainException tex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + tex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + ex.getLocalizedMessage(), model);
        }
        return redirect(READ_URL + getFinantialInstitution(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {
        if (getFinantialInstitutionBean(model) == null) {
            FinantialInstitutionBean bean = new FinantialInstitutionBean();
            setFinantialInstitutionBean(bean, model);
        }
        return "treasury/administration/managefinantialinstitution/finantialinstitution/create";
    }

    private static final String CREATEPOSTBACK_URI = "/createpostback";
    public static final String CREATEPOSTBACK_URL = CONTROLLER_URL + CREATEPOSTBACK_URI;

    @RequestMapping(value = CREATEPOSTBACK_URI, method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody String createpostback(@RequestParam(value = "bean", required = true) FinantialInstitutionBean bean,
            Model model) {
        assertUserIsManager(model);
        setFinantialInstitutionBean(bean, model);
        return getBeanJson(bean);
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "bean", required = true) FinantialInstitutionBean bean, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            assertUserIsManager(model);

            setFinantialInstitutionBean(bean, model);
            FinantialInstitution finantialInstitution = createFinantialInstitution(bean);
            setFinantialInstitution(finantialInstitution, model);
            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.create"), model);
            return redirect(READ_URL + getFinantialInstitution(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + tex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.create") + ex.getLocalizedMessage(), model);
        }
        return create(model);
    }

    @Atomic
    public FinantialInstitution createFinantialInstitution(FinantialInstitutionBean bean) {
        FinantialInstitution finantialInstitution =
                FinantialInstitution.create(bean.getFiscalcountryregion(), bean.getCurrency(), bean.getCode(),
                        bean.getFiscalNumber(), bean.getCompanyId(), bean.getName(), bean.getCompanyName(), bean.getAddress(),
                        bean.getCountry(), bean.getDistrict(), bean.getMunicipality(), bean.getLocality(), bean.getZipCode());
        return finantialInstitution;
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") FinantialInstitution finantialInstitution, Model model) {
        setFinantialInstitution(finantialInstitution, model);
        if (getFinantialInstitutionBean(model) == null) {
            setFinantialInstitutionBean(new FinantialInstitutionBean(finantialInstitution), model);
        }
        return "treasury/administration/managefinantialinstitution/finantialinstitution/update";
    }

    private static final String UPDATEPOSTBACK_URI = "/updatepostback/";
    public static final String UPDATEPOSTBACK_URL = CONTROLLER_URL + UPDATEPOSTBACK_URI;

    @RequestMapping(value = UPDATEPOSTBACK_URI + "{oid}", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public @ResponseBody String updatepostback(@PathVariable("oid") FinantialInstitution finantialInstitution, @RequestParam(
            value = "bean", required = true) FinantialInstitutionBean bean, Model model) {
        setFinantialInstitutionBean(bean, model);
        return getBeanJson(bean);
    }

    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") FinantialInstitution finantialInstitution, @RequestParam(value = "bean",
            required = true) FinantialInstitutionBean bean, Model model, RedirectAttributes redirectAttributes) {
        setFinantialInstitution(finantialInstitution, model);
        setFinantialInstitutionBean(bean, model);
        try {
            assertUserIsFrontOfficeMember(finantialInstitution, model);

            assertUserIsBackOfficeMember(finantialInstitution, model);
            updateFinantialInstitution(bean, model);

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.edit"), model);
            return redirect(READ_URL + getFinantialInstitution(model).getExternalId(), model, redirectAttributes);
        } catch (TreasuryDomainException tex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.edit") + tex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.edit") + ex.getLocalizedMessage(), model);
        }
        return update(finantialInstitution, model);
    }

    @Atomic
    public void updateFinantialInstitution(FinantialInstitutionBean bean, Model m) {
        getFinantialInstitution(m).edit(bean.getFiscalcountryregion(), bean.getCurrency(), bean.getCode(),
                bean.getFiscalNumber(), bean.getCompanyId(), bean.getName(), bean.getCompanyName(), bean.getAddress(),
                bean.getCountry(), bean.getDistrict(), bean.getMunicipality(), bean.getLocality(), bean.getZipCode());
    }

    @RequestMapping(value = "/read/{oid}/exportproductsintegrationfile", produces = "text/xml;charset=Windows-1252")
    public void processReadToExportProductIntegrationFile(@PathVariable("oid") FinantialInstitution finantialInstitution,
            Model model, RedirectAttributes redirectAttributes, HttpServletResponse response) {
        try {
            assertUserIsFrontOfficeMember(finantialInstitution, model);

            assertUserIsBackOfficeMember(finantialInstitution, model);
            String output = ERPExporter.exportsProductsToXML(finantialInstitution);
            response.setContentType("text/xml");
            response.setCharacterEncoding("Windows-1252");
            String filename =
                    URLEncoder.encode(
                            StringNormalizer
                                    .normalizePreservingCapitalizedLetters(
                                            "ERP_PRODUCTS_" + finantialInstitution.getFiscalNumber() + ".xml")
                                    .replaceAll("\\s", "_").replace(" ", "_"), "Windows-1252");
            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            response.getOutputStream().write(output.getBytes("Windows-1252"));
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.upload") + ex.getLocalizedMessage(), model);
            try {
                response.sendRedirect(redirect(READ_URL + finantialInstitution.getExternalId(), model, redirectAttributes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/read/{oid}/exportcustomersintegrationfile")
    public void processReadToExportCustomerIntegrationFile(@PathVariable("oid") FinantialInstitution finantialInstitution,
            Model model, RedirectAttributes redirectAttributes, HttpServletResponse response) {
        try {
            assertUserIsFrontOfficeMember(finantialInstitution, model);

            assertUserIsBackOfficeMember(finantialInstitution, model);
            String output = ERPExporter.exportsCustomersToXML(finantialInstitution);
            response.setContentType("text/xml");
            response.setCharacterEncoding("Windows-1252");
            String filename =
                    URLEncoder.encode(
                            StringNormalizer
                                    .normalizePreservingCapitalizedLetters(
                                            "ERP_CUSTOMERS_" + finantialInstitution.getFiscalNumber() + ".xml")
                                    .replaceAll("\\s", "_").replace(" ", "_"), "Windows-1252");
            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            response.getOutputStream().write(output.getBytes("Windows-1252"));
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.upload") + ex.getLocalizedMessage(), model);
            try {
                response.sendRedirect(redirect(READ_URL + finantialInstitution.getExternalId(), model, redirectAttributes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/read/{oid}/exportproductsintegrationonline")
    public String processReadToExportProductIntegrationOnline(@PathVariable("oid") FinantialInstitution finantialInstitution,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(finantialInstitution, model);

            assertUserIsBackOfficeMember(finantialInstitution, model);
            ERPExportOperation output = ERPExporter.exportProductsToIntegration(finantialInstitution);
            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.integration.erp.exportoperation.success"), model);
            return redirect(ERPExportOperationController.READ_URL + output.getExternalId(), model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(
                    BundleUtil.getString(Constants.BUNDLE, "label.integration.erp.exportoperation.error")
                            + ex.getLocalizedMessage(), model);
        }
        setFinantialInstitution(finantialInstitution, model);
        return "treasury/administration/managefinantialinstitution/finantialinstitution/read";
    }

    @RequestMapping(value = "/read/{oid}/exportcustomersintegrationonline")
    public String processReadToExportCustomersIntegrationOnline(@PathVariable("oid") FinantialInstitution finantialInstitution,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(finantialInstitution, model);

            assertUserIsBackOfficeMember(finantialInstitution, model);
            ERPExportOperation output = ERPExporter.exportCustomersToIntegration(finantialInstitution);
            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.integration.erp.exportoperation.success"), model);
            return redirect(ERPExportOperationController.READ_URL + output.getExternalId(), model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(
                    BundleUtil.getString(Constants.BUNDLE, "label.integration.erp.exportoperation.error")
                            + ex.getLocalizedMessage(), model);
        }
        setFinantialInstitution(finantialInstitution, model);
        return "treasury/administration/managefinantialinstitution/finantialinstitution/read";
    }

    @RequestMapping(value = "/read/{oid}/erpconfigurationupdate")
    public String processReadToERPConfigurationUpdate(@PathVariable("oid") FinantialInstitution finantialInstitution,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(finantialInstitution, model);

            assertUserIsBackOfficeMember(finantialInstitution, model);
            if (finantialInstitution.getErpIntegrationConfiguration() == null) {
                DocumentNumberSeries paymentsIntegrationSeries =
                        DocumentNumberSeries.find(FinantialDocumentType.findForSettlementNote(), finantialInstitution)
                                .filter(x -> x.getSeries().getExternSeries() == true).findFirst().orElse(null);
                if (paymentsIntegrationSeries == null) {
                    throw new TreasuryDomainException("error.ERPIntegrationConfiguration.invalid.external.payments.series");
                }
                ERPConfiguration erpIntegrationConfiguration =
                        ERPConfiguration.create(paymentsIntegrationSeries.getSeries(), finantialInstitution, "", "", "", "", "",
                                1024L);
                finantialInstitution.setErpIntegrationConfiguration(erpIntegrationConfiguration);
            }
            return redirect(ERPConfigurationController.READ_URL
                    + finantialInstitution.getErpIntegrationConfiguration().getExternalId(), model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            return read(finantialInstitution, model, redirectAttributes);
        }
    }

    @RequestMapping(value = "/read/{oid}/sibsconfigurationupdate")
    public String processReadToSibsConfigurationUpdate(@PathVariable("oid") FinantialInstitution finantialInstitution,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            assertUserIsFrontOfficeMember(finantialInstitution, model);

            assertUserIsBackOfficeMember(finantialInstitution, model);
            if (finantialInstitution.getSibsConfiguration() == null) {
                SibsConfiguration sibsConfiguration =
                        SibsConfiguration.create(finantialInstitution, "00000", "000000000", "000000000");
                finantialInstitution.setSibsConfiguration(sibsConfiguration);
            }
            return redirect(SibsConfigurationController.READ_URL + finantialInstitution.getSibsConfiguration().getExternalId(),
                    model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            return read(finantialInstitution, model, redirectAttributes);
        }
    }
}
