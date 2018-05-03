package org.fenixedu.treasury.domain.paymentcodes;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class SibsInputFileDomainObject extends SibsInputFileDomainObject_Base {
    
    public static final String CONTENT_TYPE = "text/plain";

    protected SibsInputFileDomainObject() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
        setCreationDate(new DateTime());
        setCreator(Authenticate.getUser().getUsername());
    }

    protected SibsInputFileDomainObject(FinantialInstitution finantialInstitution, DateTime whenProcessedBySIBS, String displayName,
            String filename, byte[] content, User uploader) {
        this();
        init(finantialInstitution, whenProcessedBySIBS, displayName, filename, content, uploader);
    }

    protected void init(FinantialInstitution finantialInstitution, DateTime whenProcessedBySIBS, String displayName,
            String filename, byte[] content, User uploader) {

        setWhenProcessedBySibs(whenProcessedBySIBS);
        setUploader(uploader);
        setFinantialInstitution(finantialInstitution);
        
        checkRules();
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

    @Atomic
    public void delete() {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.SibsInputFile.cannot.delete");
        }

        setFinantialInstitution(null);
        setUploader(null);
        setDomainRoot(null);
        
        deleteDomainObject();
    }

    public static Stream<SibsInputFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getSibsInputFilesDomainObjectSet().stream();
    }
    
    @Atomic
    public static SibsInputFileDomainObject copyAndAssociate(final SibsInputFile sibsInputFile) {
        
        if(sibsInputFile.getSibsInputFile() != null)  {
            throw new TreasuryDomainException("error.SibsInputFileDomainObject.already.with.copy");
        }
        
        final FinantialInstitution finantialInstitution = sibsInputFile.getFinantialInstitution();
        final DateTime whenProcessedBySIBS = sibsInputFile.getWhenProcessedBySibs();
        final String displayName = sibsInputFile.getDisplayName();
        final String filename = sibsInputFile.getFilename();
        final User uploader = sibsInputFile.getUploader();
        
        final SibsInputFileDomainObject domainObject = new SibsInputFileDomainObject(finantialInstitution, whenProcessedBySIBS, displayName, filename, null, uploader);
        
        domainObject.setCreationDate(sibsInputFile.getCreationDate());
        domainObject.setCreator(sibsInputFile.getVersioningCreator());
        domainObject.setTreasuryFile(sibsInputFile);
        
        return domainObject;
    }

    public boolean isAccessible(User arg0) {
        return true;
    }    
}
