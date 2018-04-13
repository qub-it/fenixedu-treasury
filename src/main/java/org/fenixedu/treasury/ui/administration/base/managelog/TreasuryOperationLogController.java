/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and ServiÃ§os Partilhados da
 * Universidade de Lisboa:
 *  - Copyright Â© 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright Â© 2015 Universidade de Lisboa (after any Go-Live phase)
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
package org.fenixedu.treasury.ui.administration.base.managelog;

import static org.fenixedu.treasury.util.Constants.treasuryBundle;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.TreasuryOperationLog;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.administration.managefinantialinstitution.FinantialInstitutionController;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;

//@Component("org.fenixedu.treasury.ui.administration.base.managelog") <-- Use for duplicate controller name disambiguation
//@SpringFunctionality(app = TreasuryController.class, title = "label.title.administration.base.manageTreasuryOperationLog",
//        accessGroup = "treasuryBackOffice")
@BennuSpringController(value = FinantialInstitutionController.class)
@RequestMapping(TreasuryOperationLogController.CONTROLLER_URL)
public class TreasuryOperationLogController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/administration/base/managetreasuryoperationlog/treasuryoperationlog";

    public static final long SEARCH_LIMIT_SIZE = 75;

    private List<TreasuryOperationLog> getTreasuryOperationLogSet(Model model) {
        return (List<TreasuryOperationLog>) model.asMap().get("treasuryOperationLogSet");
    }

    private void setTreasuryOperationLogSet(List<TreasuryOperationLog> treasuryOperationLogSet, Model model) {
        model.addAttribute("treasuryOperationLogSet", treasuryOperationLogSet);
    }

    private DomainObject getDomainObject(Model model) {
        return (DomainObject) model.asMap().get("domainObject");
    }

    private void setDomainObject(DomainObject domainObject, Model model) {
        model.addAttribute("domainObject", domainObject);
    }

    private static final String _READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + _READ_URI;

    @RequestMapping(value = _READ_URI + "{oid}")
    public String read(@PathVariable("oid") DomainObject domainObject,
            @RequestParam(value = "logdatefrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate logDateFrom,
            @RequestParam(value = "logdateto", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate logDateTo,
            Model model, RedirectAttributes redirectAttributes) {
        if (!FenixFramework.isDomainObjectValid(domainObject)) {
            addErrorMessage(treasuryBundle("error.read.object.oid.not.valid"), model);
        } else {
            setDomainObject(domainObject, model);
            setTreasuryOperationLogSet(filterSearch(domainObject.getExternalId(), null, logDateFrom, logDateTo), model);
        }
        return "treasury/administration/base/managetreasuryoperationlog/treasuryoperationlog/read";
    }

    private Stream<TreasuryOperationLog> getSearchUniverse() {
        return TreasuryOperationLog.findAll();
    }

    private List<TreasuryOperationLog> filterSearch(String oid, String type, LocalDate from, LocalDate to) {
        return getSearchUniverse().filter(log -> oid == null || log.getDomainOid().equals(oid))
                .filter(log -> type == null || log.getType().equals(type))
                .filter(log -> from == null || log.getVersioningCreationDate().toLocalDate().isAfter(from)
                        || log.getVersioningCreationDate().toLocalDate().isEqual(from))
                .filter(log -> to == null || log.getVersioningCreationDate().toLocalDate().isBefore(to)
                        || log.getVersioningCreationDate().toLocalDate().isEqual(to))
                .sorted(TreasuryOperationLog.COMPARATOR_BY_CREATION_DATE).limit(SEARCH_LIMIT_SIZE).collect(Collectors.toList());
    }
}
