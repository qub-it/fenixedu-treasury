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
package org.fenixedu.treasury.domain.paymentcodes;

import static org.fenixedu.treasury.util.TreasuryConstants.treasuryBundle;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.FenixEDUTreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.fenixedu.treasury.services.payments.sibs.SIBSImportationFileDTO;
import org.fenixedu.treasury.services.payments.sibs.SIBSPaymentsImporter.ProcessResult;
import org.fenixedu.treasury.util.TreasuryConstants;
import org.fenixedu.treasury.util.streaming.spreadsheet.ExcelSheet;
import org.fenixedu.treasury.util.streaming.spreadsheet.Spreadsheet;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class SibsReportFileDomainObject extends SibsReportFileDomainObject_Base implements IGenericFile {

    public static final String CONTENT_TYPE = "text/plain";
    public static final String FILE_EXTENSION = ".idm";

    protected SibsReportFileDomainObject() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

//    protected SibsReportFileDomainObject(final DateTime whenProcessedBySibs, final BigDecimal transactionsTotalAmount,
//            final BigDecimal totalCost, final String displayName, final String fileName, final byte[] content) {
//        this();
//
//        this.init(whenProcessedBySibs, transactionsTotalAmount, totalCost, displayName, fileName, content);
//
//        checkRules();
//    }
//
    protected void init(final DateTime whenProcessedBySibs, final BigDecimal transactionsTotalAmount, final BigDecimal totalCost,
            final String displayName, final String fileName, final byte[] content) {

        TreasuryPlataformDependentServicesFactory.implementation().createFile(this, fileName, CONTENT_TYPE, content);

        setWhenProcessedBySibs(whenProcessedBySibs);
        setTransactionsTotalAmount(transactionsTotalAmount);
        setTotalCost(totalCost);
        checkRules();
    }

    private void checkRules() {
    }

    @Atomic
    public void edit(final DateTime whenProcessedBySibs, final BigDecimal transactionsTotalAmount, final BigDecimal totalCost) {
        setWhenProcessedBySibs(whenProcessedBySibs);
        setTransactionsTotalAmount(transactionsTotalAmount);
        setTotalCost(totalCost);
        
        checkRules();
    }

    public boolean isDeletable() {
        return getReferenceCodesSet().isEmpty() && getSibsTransactionsSet().isEmpty();
    }

    @Override
    @Atomic
    public void delete() {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        if (!isDeletable()) {
            throw new TreasuryDomainException("error.SibsReportFile.cannot.delete");
        }

        setDomainRoot(null);
        
        // services.deleteFile(this);
        
        super.deleteDomainObject();
    }

    public static Stream<SibsReportFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getSibsReportFileDomainObjectsSet().stream();
    }

    public static Stream<SibsReportFileDomainObject> findByTransactionsTotalAmount(final BigDecimal transactionsTotalAmount) {
        return findAll().filter(i -> transactionsTotalAmount.equals(i.getTransactionsTotalAmount()));
    }

    public static Stream<SibsReportFileDomainObject> findByTotalCost(final BigDecimal totalCost) {
        return findAll().filter(i -> totalCost.equals(i.getTotalCost()));
    }
    
    public static Optional<SibsReportFileDomainObject> findUniqueBySibsReportFile(final SibsReportFile file) {
        return findAll().filter(o -> o.getTreasuryFile() == file).findFirst();
    }

    @Override
    public boolean isAccessible(final String username) {
        return TreasuryAccessControlAPI.isBackOfficeMember(username);
    }

//    @Atomic
//    public static SibsReportFileDomainObject create(final DateTime whenProcessedBySibs, final BigDecimal transactionsTotalAmount,
//            final BigDecimal totalCost, final String displayName, final String fileName, final byte[] content) {
//        return new SibsReportFileDomainObject(whenProcessedBySibs, transactionsTotalAmount, totalCost, displayName, fileName, content);
//
//    }

    @Atomic
    public static SibsReportFileDomainObject createFromSibsReportFile(final SibsReportFile file) {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        SibsReportFileDomainObject result = new SibsReportFileDomainObject();
        
        result.setWhenProcessedBySibs(file.getWhenProcessedBySibs());
        result.setTransactionsTotalAmount(file.getTransactionsTotalAmount());
        result.setTotalCost(file.getTotalCost());
        result.setErrorLog(file.getErrorLog());
        result.setInfoLog(file.getInfoLog());
        result.setCreationDate(file.getCreationDate());
        result.setCreator(services.versioningCreatorUsername(file));
        result.setTreasuryFile(file);
        result.setFileId(file.getExternalId());
        
        result.getReferenceCodesSet().addAll(file.getReferenceCodesSet());
        result.getSibsTransactionsSet().addAll(file.getSibsTransactionsSet());
        
        result.checkRules();
        
        return result;
    }

    protected static byte[] buildContentFor(final SIBSImportationFileDTO reportFileDTO) {

        Stream<SibsSpreadsheetRowReportBean> lines =
                reportFileDTO.getLines().stream().map(l -> new SibsSpreadsheetRowReportBean(l));

        return Spreadsheet.buildSpreadsheetContent(new Spreadsheet() {

            @Override
            public ExcelSheet[] getSheets() {
                return new ExcelSheet[] { ExcelSheet.create(treasuryBundle("label.SibsReportFile.spreadsheet.name"),
                        SibsSpreadsheetRowReportBean.SPREADSHEET_HEADERS, lines) };
            }
        }, null);

    }

    protected static String filenameFor(final SIBSImportationFileDTO reportFileDTO) {
        final String date = new DateTime().toString("yyyyMMddHHmm");
        return "Relatorio-SIBS-" + date + ".xlsx";
    }

    protected static String displayNameFor(final SIBSImportationFileDTO reportFileDTO) {
        final String date = new DateTime().toString("yyyyMMddHHmm");
        return "Relatorio-SIBS-" + date;
    }

//    @Atomic
//    public static SibsReportFileDomainObject processSIBSIncommingFile(final SIBSImportationFileDTO reportDTO) {
//        byte[] content = buildContentFor(reportDTO);
//        SibsReportFile result = SibsReportFileDomainObject.create(reportDTO.getWhenProcessedBySibs(), reportDTO.getTransactionsTotalAmount(),
//                reportDTO.getTotalCost(), displayNameFor(reportDTO), filenameFor(reportDTO), content);
//
//        return result;
//    }

    
    public Integer getNumberOfTransactions() {
        return this.getSibsTransactionsSet().size();
    }

    public String getTransactionDescription(Integer index) {
        if (this.getSibsTransactionsSet().size() > index) {
            if (index > 0) {
                return this.getSibsTransactionsSet().stream().skip(index - 1).findFirst().get().toString();
            } else if (index == 0) {
                return this.getSibsTransactionsSet().iterator().next().toString();
            }
        }
        return "";
    }

    public BigDecimal getTransactionAmount(Integer index) {
        if (this.getSibsTransactionsSet().size() > index) {
            if (index > 0) {
                return this.getSibsTransactionsSet().stream().skip(index - 1).findFirst().get().getAmountPayed();
            } else if (index == 0) {
                return this.getSibsTransactionsSet().iterator().next().getAmountPayed();
            }
        }
        return BigDecimal.ZERO;
    }

    @Atomic
    public void updateLogMessages(ProcessResult result) {
        StringBuilder build = new StringBuilder();
        for (String s : result.getErrorMessages()) {
            build.append(s + "\n");
        }
        this.setErrorLog(build.toString());
        build = new StringBuilder();
        for (String s : result.getActionMessages()) {
            build.append(s + "\n");
        }
        this.setInfoLog(build.toString());
    }

}
