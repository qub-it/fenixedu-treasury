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
package org.fenixedu.treasury.domain.integration;

import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class OperationFileDomainObject extends OperationFileDomainObject_Base implements IGenericFile {

    public static final String CONTENT_TYPE = "application/octet-stream";

    public OperationFileDomainObject() {
        super();
        this.setDomainRoot(FenixFramework.getDomainRoot());
    }

//    public OperationFileDomainObject(final String fileName, final byte[] content) {
//        this();
//        
//        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
//        
//        services.createFile(this, fileName, CONTENT_TYPE, content);
//    }
    
    @Override
    public boolean isAccessible(final String username) {
        throw new RuntimeException("not implemented");
    }

    private void checkRules() {
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
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        if (!isDeletable()) {
            throw new TreasuryDomainException("error.OperationFile.cannot.delete");
        }

        this.setLogIntegrationOperation(null);
        this.setIntegrationOperation(null);
        
        // services.deleteFile(this);
        
        super.deleteDomainObject();
    }

//    @Atomic
//    public static OperationFileDomainObject create(String fileName, byte[] content, IntegrationOperation operation) {
//        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
//
//        final OperationFileDomainObject operationFile = new OperationFileDomainObject();
//        operationFile.setIntegrationOperation(operation);
//        
//        services.createFile(operationFile, fileName, CONTENT_TYPE, content);
//        
//        return operationFile;
//    }
//
//    @Atomic
//    public static OperationFileDomainObject createLog(final String fileName, final byte[] content, final IntegrationOperation operation) {
//        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
//
//        final OperationFileDomainObject operationFile = new OperationFileDomainObject();
//        operationFile.setLogIntegrationOperation(operation);
//        
//        services.createFile(operationFile, fileName, CONTENT_TYPE, content);
//
//        return operationFile;
//    }
    
    public static OperationFileDomainObject createFromOperationFile(final OperationFile file) {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        final OperationFileDomainObject result = new OperationFileDomainObject();
        
        result.setIntegrationOperation(file.getIntegrationOperation());
        result.setLogIntegrationOperation(file.getLogIntegrationOperation());
        result.setCreationDate(file.getCreationDate());
        result.setCreator(services.versioningCreatorUsername(file));
        result.setTreasuryFile(file);
        result.setFileId(file.getExternalId());
        
        return result;
    }
    
    public static Stream<OperationFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getOperationFileDomainObjectsSet().stream();
    }
    
    public static Optional<OperationFileDomainObject> findUniqueByOperationFile(final OperationFile file) {
        return findAll().filter(o -> o.getTreasuryFile() == file).findFirst();
    }
    
}
