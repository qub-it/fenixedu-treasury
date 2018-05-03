package org.fenixedu.treasury.domain.document;

import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class TreasuryDocumentTemplateFileDomainObject extends TreasuryDocumentTemplateFileDomainObject_Base implements IGenericFile {
    
    public static final String CONTENT_TYPE = "application/vnd.oasis.opendocument.text";
    public static final String FILE_EXTENSION = ".odt";

    protected TreasuryDocumentTemplateFileDomainObject() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
        setCreationDate(new DateTime());
        setCreator(Authenticate.getUser().getUsername());
    }

    protected TreasuryDocumentTemplateFileDomainObject(final TreasuryDocumentTemplate documentTemplate, final boolean active,
            final String displayName, final String fileName, final byte[] content) {
        this();
        setTreasuryDocumentTemplate(documentTemplate);
        setActive(active);

        // treasuryPlatformServices().createFile(this, fileName, CONTENT_TYPE, content);
        
        // documentTemplate.activateFile(this);
        
        checkRules();
    }

    private void checkRules() {
        if (getDomainRoot() == null) {
            throw new TreasuryDomainException("error.TreasuryDocumentTemplateFile.bennu.required");
        }
        
        if (getTreasuryDocumentTemplate() == null) {
            throw new TreasuryDomainException("error.TreasuryDocumentTemplateFile.documentTemplate.required");
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
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.TreasuryDocumentTemplateFile.cannot.delete");
        }

        setDomainRoot(null);
        setTreasuryDocumentTemplate(null);
        
        deleteDomainObject();
    }

    public static TreasuryDocumentTemplateFileDomainObject copyAndAssociate(final TreasuryDocumentTemplateFile file) {

        if(file.getTreasuryDocumentTemplateFile() != null)  {
            throw new TreasuryDomainException("error.TreasuryDocumentTemplateFileDomainObject.already.with.copy");
        }
        
        TreasuryDocumentTemplateFileDomainObject domainObject = new TreasuryDocumentTemplateFileDomainObject();

        domainObject.setCreationDate(file.getVersioningCreationDate());
        domainObject.setCreator(file.getVersioningCreator());
        domainObject.setTreasuryDocumentTemplate(file.getTreasuryDocumentTemplate());
        domainObject.setActive(file.getActive());
        domainObject.setTreasuryFile(file);
        
        return domainObject;
    }
    
    public static Stream<TreasuryDocumentTemplateFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getTreasuryDocumentTemplateFilesDomainObjectSet().stream();
    }

    public static Stream<TreasuryDocumentTemplateFileDomainObject> findByDocumentTemplate(final TreasuryDocumentTemplate documentTemplate) {
        return documentTemplate.getTreasuryDocumentTemplateFilesDomainObjectSet().stream();
    }

    @Override
    public boolean isAccessible(String username) {
        return User.findByUsername(username) != null;
    }
    
}
