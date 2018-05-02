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

import static org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory.treasuryPlatformServices;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class SibsInputFile extends SibsInputFile_Base implements IGenericFile {

    public static final String CONTENT_TYPE = "text/plain";

    protected SibsInputFile() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

    protected SibsInputFile(FinantialInstitution finantialInstitution, DateTime whenProcessedBySIBS, String displayName,
            String filename, byte[] content, User uploader) {
        this();
        init(finantialInstitution, whenProcessedBySIBS, displayName, filename, content, uploader);
    }

    protected void init(FinantialInstitution finantialInstitution, DateTime whenProcessedBySIBS, String displayName,
            String filename, byte[] content, User uploader) {

        setWhenProcessedBySibs(whenProcessedBySIBS);
        setUploader(uploader);
        setFinantialInstitution(finantialInstitution);
        
        treasuryPlatformServices().createFile(this, filename, CONTENT_TYPE, content);
        
        checkRules();
    }

    private void checkRules() {
        // Check that file is associated
        treasuryPlatformServices().getFileSize(this);
    }

    @Atomic
    public void edit() {
        checkRules();
    }

    public boolean isDeletable() {
        return true;
    }

    @Override
    @Atomic
    public void delete() {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.SibsInputFile.cannot.delete");
        }

        setFinantialInstitution(null);
        setUploader(null);
        
        super.deleteDomainObject();
    }

    /* FROM IGenericFile */
    
    @Override
    public byte[] getContent() {
        return treasuryPlatformServices().getFileContent(this);
    }

    @Override
    public Long getSize() {
        return treasuryPlatformServices().getFileSize(this);
    }

    @Override
    public DateTime getCreationDate() {
        return treasuryPlatformServices().getFileCreationDate(this);
    }

    @Override
    public String getFilename() {
        return treasuryPlatformServices().getFilename(this);
    }

    @Override
    public InputStream getStream() {
        return treasuryPlatformServices().getFileStream(this);
    }

    @Override
    public String getContentType() {
        return treasuryPlatformServices().getFileContentType(this);
    }

    @Override
    public boolean isAccessible(String username) {
        return User.findByUsername(username) != null;
    }

    /* SERVICES */
    
    @Atomic
    public static SibsInputFile create(FinantialInstitution finantialInstitution, DateTime whenProcessedBySIBS,
            String displayName, String filename, byte[] content, User uploader) {
        SibsInputFile result = new SibsInputFile(finantialInstitution, whenProcessedBySIBS, displayName, filename, content, uploader);
        
        return result;

    }

    public static Stream<SibsInputFile> findAll() {
        Set<SibsInputFile> result = new HashSet<SibsInputFile>();
        for (FinantialInstitution finantialInstitution : FinantialInstitution.findAll().collect(Collectors.toList())) {
            result.addAll(finantialInstitution.getSibsInputFilesSet());
        }

        return result.stream();
    }

    public static Stream<SibsInputFile> findByUploader(final User uploader) {
        return uploader.getSibsInputFilesSet().stream();
    }
    
}
