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
package org.fenixedu.treasury.ui;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.treasury.adapters.CountryAdapter;
import org.fenixedu.treasury.adapters.DistrictAdapter;
import org.fenixedu.treasury.adapters.DomainObjectAdapter;
import org.fenixedu.treasury.adapters.LocalizedStringAdapter;
import org.fenixedu.treasury.adapters.MunicipalityAdapter;
import org.fenixedu.treasury.ui.converters.BeanConverterService;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.accesscontrol.TreasuryAccessControl;
import org.fenixedu.treasury.dto.ITreasuryBean;
import org.fenixedu.treasury.ui.converters.CountryConverterService;
import org.fenixedu.treasury.ui.converters.DistrictConverterService;
import org.fenixedu.treasury.ui.converters.MunicipalityConverterService;
import org.fenixedu.treasury.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import freemarker.template.LocalizedString;
import pt.ist.fenixframework.DomainObject;
import pt.ist.standards.geographic.Country;
import pt.ist.standards.geographic.District;
import pt.ist.standards.geographic.Municipality;

public class TreasuryBaseController {

    protected void assertUserIsManager(Model model) {
        if (TreasuryAccessControl.getInstance().isManager(Authenticate.getUser())) {
            return;
        } else {
            addErrorMessage(Constants.treasuryBundle("error.authorization.not.manager"), model);
            throw new SecurityException(Constants.treasuryBundle("error.authorization.not.manager"));
        }
    }

    protected void assertUserIsBackOfficeMember(Model model) {
        if (TreasuryAccessControl.getInstance().isBackOfficeMember(Authenticate.getUser())) {
            return;
        } else {
            addErrorMessage(Constants.treasuryBundle("error.authorization.not.backoffice"), model);
            throw new SecurityException(Constants.treasuryBundle("error.authorization.not.backoffice"));
        }
    }

    protected void assertUserIsFrontOfficeMember(Model model) {
        if (TreasuryAccessControl.getInstance().isFrontOfficeMember(Authenticate.getUser())) {
            return;
        } else {
            addErrorMessage(Constants.treasuryBundle("error.authorization.not.frontoffice"), model);
            throw new SecurityException(Constants.treasuryBundle("error.authorization.not.frontoffice"));
        }
    }

    protected void assertUserIsBackOfficeMember(FinantialInstitution finantialInstitution, Model model) {
        if (TreasuryAccessControl.getInstance().isBackOfficeMember(Authenticate.getUser(), finantialInstitution)) {
            return;
        } else {
            addErrorMessage(Constants.treasuryBundle("error.authorization.not.backoffice"), model);
            throw new SecurityException(Constants.treasuryBundle("error.authorization.not.backoffice"));
        }
    }

    protected void assertUserIsAllowToModifySettlements(FinantialInstitution finantialInstitution, Model model) {
        if (TreasuryAccessControl.getInstance().isAllowToModifySettlements(Authenticate.getUser(), finantialInstitution)) {
            return;
        } else {
            addErrorMessage(Constants.treasuryBundle("error.authorization.not.allow.to.modify.settlements"), model);
            throw new SecurityException(BundleUtil.getString(Constants.BUNDLE,
                    "error.authorization.not.allow.to.modify.settlements"));
        }
    }

    protected void assertUserIsAllowToModifyInvoices(FinantialInstitution finantialInstitution, Model model) {
        if (TreasuryAccessControl.getInstance().isAllowToModifyInvoices(Authenticate.getUser(), finantialInstitution)) {
            return;
        } else {
            addErrorMessage(Constants.treasuryBundle("error.authorization.not.allow.to.modify.invoices"), model);
            throw new SecurityException(
                    Constants.treasuryBundle("error.authorization.not.allow.to.modify.invoices"));
        }
    }

    protected void assertUserIsFrontOfficeMember(FinantialInstitution finantialInstitution, Model model) {
        if (TreasuryAccessControl.getInstance().isFrontOfficeMember(Authenticate.getUser(), finantialInstitution)) {
            return;
        } else {
            addErrorMessage(Constants.treasuryBundle("error.authorization.not.frontoffice"), model);
            throw new SecurityException(Constants.treasuryBundle("error.authorization.not.frontoffice"));
        }
    }

    /* From FenixEDUBaseController */
    
    protected static final String ERROR_MESSAGES = "errorMessages";
    protected static final String WARNING_MESSAGES = "warningMessages";
    protected static final String INFO_MESSAGES = "infoMessages";

    // Ricardo : This AutoWired is binded to Per-Request via SpringMVC proxy. The Binded HttpServletRequest is binded
    // to a proxy instead to the singleton
    // http://stackoverflow.com/questions/3320674/spring-how-do-i-inject-an-httpservletrequest-into-a-request-scoped-bean
    // http://stackoverflow.com/questions/28638962/autowiring-httpservletrequest-in-spring-controller
    @Autowired
    protected HttpServletRequest request;

    // The list of INFO messages that can be showed on View
    protected void addInfoMessage(final String message, final Model model) {
        ((List<String>) model.asMap().get(INFO_MESSAGES)).add(message);
    }

    // The list of WARNING messages that can be showed on View
    protected void addWarningMessage(final String message, final Model model) {
        ((List<String>) model.asMap().get(WARNING_MESSAGES)).add(message);
    }

    // The list of ERROR messages that can be showed on View
    protected void addErrorMessage(final String message, final Model model) {
        ((List<String>) model.asMap().get(ERROR_MESSAGES)).add(message);
    }

    protected void clearMessages(final Model model) {
        model.addAttribute(INFO_MESSAGES, new ArrayList<String>());
        model.addAttribute(WARNING_MESSAGES, new ArrayList<String>());
        model.addAttribute(ERROR_MESSAGES, new ArrayList<String>());
    }

    protected String redirect(final String destinationAction, final Model model, final RedirectAttributes redirectAttributes) {
        if (model.containsAttribute(INFO_MESSAGES)) {
            redirectAttributes.addFlashAttribute(INFO_MESSAGES, model.asMap().get(INFO_MESSAGES));
        }
        if (model.containsAttribute(WARNING_MESSAGES)) {
            redirectAttributes.addFlashAttribute(WARNING_MESSAGES, model.asMap().get(WARNING_MESSAGES));
        }
        if (model.containsAttribute(ERROR_MESSAGES)) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGES, model.asMap().get(ERROR_MESSAGES));
        }

        return "redirect:" + destinationAction;
    }

    protected String redirectToReferrer(final Model model, final RedirectAttributes redirectAttributes) {
        String previousURL = request.getHeader("referer");
        return redirect(previousURL, model, redirectAttributes);
    }

    @ModelAttribute
    protected void addModelProperties(final Model model, final HttpServletRequest request) {
        if (!model.containsAttribute(INFO_MESSAGES)) {
            model.addAttribute(INFO_MESSAGES, new ArrayList<String>());
        }
        if (!model.containsAttribute(WARNING_MESSAGES)) {
            model.addAttribute(WARNING_MESSAGES, new ArrayList<String>());
        }
        if (!model.containsAttribute(ERROR_MESSAGES)) {
            model.addAttribute(ERROR_MESSAGES, new ArrayList<String>());
        }

        //HACK: Forcing the "Messages AS DEFAULT CODE"
        WebApplicationContext webAppContext = RequestContextUtils.getWebApplicationContext(request);
        MessageSource messageSource = (MessageSource) webAppContext.getBean("messageSource");
        if (messageSource != null && messageSource instanceof ReloadableResourceBundleMessageSource) {
            ((ReloadableResourceBundleMessageSource) messageSource).setUseCodeAsDefaultMessage(true);
        }
        // Add here more attributes to the Model
        // model.addAttribute(<attr1Key>, <attr1Value>);
        // ....
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        GenericConversionService conversionService = (GenericConversionService) binder.getConversionService();
        if (!conversionService.canConvert(String.class, ITreasuryBean.class)) {
            GsonBuilder builder = new GsonBuilder();
            registerTypeAdapters(builder);
            conversionService.addConverter(new BeanConverterService(builder));
        }
        conversionService.addConverter(new CountryConverterService());
        conversionService.addConverter(new DistrictConverterService());
        conversionService.addConverter(new MunicipalityConverterService());

    }

    protected void registerTypeAdapters(final GsonBuilder builder) {
        builder.registerTypeAdapter(LocalizedString.class, new LocalizedStringAdapter());
        builder.registerTypeAdapter(Country.class, new CountryAdapter());
        builder.registerTypeAdapter(District.class, new DistrictAdapter());
        builder.registerTypeAdapter(Municipality.class, new MunicipalityAdapter());
        builder.registerTypeHierarchyAdapter(DomainObject.class, new DomainObjectAdapter());
    }

    protected String getBeanJson(final ITreasuryBean bean) {
        GsonBuilder builder = new GsonBuilder();
        registerTypeAdapters(builder);
        Gson gson = Converters.registerAll(builder).create();

        // CREATING JSON TREE TO ADD CLASSNAME ATTRIBUTE MUST DO THIS AUTOMAGICALLY
        JsonElement jsonTree = gson.toJsonTree(bean);
        jsonTree.getAsJsonObject().addProperty("classname", bean.getClass().getName());
        return jsonTree.toString();
    }
    
    
}
