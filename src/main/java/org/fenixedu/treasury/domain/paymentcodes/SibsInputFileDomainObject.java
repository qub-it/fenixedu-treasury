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

import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class SibsInputFileDomainObject extends SibsInputFileDomainObject_Base implements IGenericFile {

    public static final String CONTENT_TYPE = "text/plain";

    protected SibsInputFileDomainObject() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

//    protected SibsInputFileDomainObject(FinantialInstitution finantialInstitution, DateTime whenProcessedBySIBS, String displayName,
//            String filename, byte[] content, final String uploader) {
//        this();
//        init(finantialInstitution, whenProcessedBySIBS, displayName, filename, content, uploader);
//    }
//
    protected void init(FinantialInstitution finantialInstitution, DateTime whenProcessedBySIBS, String displayName,
            String filename, byte[] content, final String uploader) {
        
        TreasuryPlataformDependentServicesFactory.implementation().createFile(this, filename, CONTENT_TYPE, content);

        setWhenProcessedBySibs(whenProcessedBySIBS);
        setUploaderUsername(uploader);
        setFinantialInstitution(finantialInstitution);
        
        checkRules();
    }

    private void checkRules() {
    }

    public boolean isDeletable() {
        return true;
    }

    @Override
    @Atomic
    public void delete() {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        if (!isDeletable()) {
            throw new TreasuryDomainException("error.SibsInputFile.cannot.delete");
        }

        setFinantialInstitution(null);
        setDomainRoot(null);

        // services.deleteFile(this);
        
        super.deleteDomainObject();
    }

//    @Atomic
//    public static SibsInputFileDomainObject create(FinantialInstitution finantialInstitution, DateTime whenProcessedBySIBS,
//            String displayName, String filename, byte[] content, final String uploader) {
//        return new SibsInputFileDomainObject(finantialInstitution, whenProcessedBySIBS, displayName, filename, content, uploader);
//    }

    public static SibsInputFileDomainObject createFromSibsInputFile(final SibsInputFile file) {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        final SibsInputFileDomainObject result = new SibsInputFileDomainObject();
        
        result.setWhenProcessedBySibs(file.getWhenProcessedBySibs());
        result.setUploaderUsername(file.getUploaderUsername());
        result.setFinantialInstitution(file.getFinantialInstitution());
        result.setTreasuryFile(file);
        result.setCreationDate(file.getCreationDate());
        result.setCreator(services.versioningCreatorUsername(file));
        result.setFileId(file.getExternalId());
        
        return result;
    }
    
    public static Stream<SibsInputFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getSibsInputFileDomainObjectsSet().stream();
    }
    
    public static Optional<SibsInputFileDomainObject> findUniqueBySibsInputFile(final SibsInputFile file) {
        return findAll().filter(o -> o.getTreasuryFile() == file).findFirst();
    }

    @Override
    public boolean isAccessible(final String username) {
        return TreasuryAccessControlAPI.isBackOfficeMember(username, getFinantialInstitution());
    }

}
