package org.fenixedu.treasury.domain.document;

import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class TreasuryDocumentTemplateFileDomainObject extends TreasuryDocumentTemplateFileDomainObject_Base
        implements IGenericFile {

    public static final String CONTENT_TYPE = "application/vnd.oasis.opendocument.text";
    public static final String FILE_EXTENSION = ".odt";

    protected TreasuryDocumentTemplateFileDomainObject() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

//    protected TreasuryDocumentTemplateFileDomainObject(final TreasuryDocumentTemplate documentTemplate, final boolean active,
//            final String displayName, final String fileName, final byte[] content) {
//        this();
//
//        TreasuryPlataformDependentServicesFactory.implementation().createFile(this, fileName, CONTENT_TYPE, content);
//        setTreasuryDocumentTemplate(documentTemplate);
//        setActive(active);
//
//        // documentTemplate.activateFile(this);
//
//        checkRules();
//    }

    private void checkRules() {
        if (getTreasuryDocumentTemplate() == null) {
            throw new TreasuryDomainException("error.TreasuryDocumentTemplateFileDomainObject.documentTemplate.required");
        }
    }

    @Atomic
    public void edit(final TreasuryDocumentTemplate documentTemplate, final boolean active) {
        setTreasuryDocumentTemplate(documentTemplate);
        setActive(active);

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
            throw new TreasuryDomainException("error.TreasuryDocumentTemplateFileDomainObject.cannot.delete");
        }

        setDomainRoot(null);
        setTreasuryDocumentTemplate(null);
        
        // services.deleteFile(this);

        super.deleteDomainObject();
    }

//    @Atomic
//    public static TreasuryDocumentTemplateFileDomainObject create(final TreasuryDocumentTemplate documentTemplate,
//            final String displayName, final String fileName, final byte[] content) {
//        TreasuryDocumentTemplateFileDomainObject result =
//                new TreasuryDocumentTemplateFileDomainObject(documentTemplate, false, displayName, fileName, content);
//        return result;
//    }
    
    @Atomic
    public static TreasuryDocumentTemplateFileDomainObject createFromTreasuryDocumentTemplateFile(final TreasuryDocumentTemplateFile file) {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
        
        final TreasuryDocumentTemplateFileDomainObject result = new TreasuryDocumentTemplateFileDomainObject();
        
        result.setActive(file.getActive());
        result.setTreasuryDocumentTemplate(file.getTreasuryDocumentTemplate());
        result.setCreationDate(file.getCreationDate());
        result.setCreator(services.versioningCreatorUsername(file));
        result.setTreasuryFile(file);
        result.setFileId(file.getExternalId());
        
        result.checkRules();
        
        return result;
    }

    public static Stream<TreasuryDocumentTemplateFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getTreasuryDocumentTemplateFileDomainObjectsSet().stream();
    }

    public static Stream<TreasuryDocumentTemplateFile> findByDocumentTemplate(final TreasuryDocumentTemplate documentTemplate) {
        return documentTemplate.getTreasuryDocumentTemplateFilesSet().stream();
    }
    
    public static Optional<TreasuryDocumentTemplateFileDomainObject> findUniqueByTreasuryDocumentTemplateFile(TreasuryDocumentTemplateFile file) {
        return findAll().filter(o -> o.getTreasuryFile() == file).findFirst();
    }

    @Override
    public boolean isAccessible(final String username) {
        return TreasuryAccessControlAPI.isBackOfficeMember(username, getTreasuryDocumentTemplate().getFinantialEntity());
    }

}
