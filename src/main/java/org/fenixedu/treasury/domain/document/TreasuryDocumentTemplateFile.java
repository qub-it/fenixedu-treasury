/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and ServiÃ§os Partilhados da
 * Universidade de Lisboa:
 *  - Copyright Â© 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright Â© 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: ricardo.pedro@qub-it.com
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

package org.fenixedu.treasury.domain.document;

import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class TreasuryDocumentTemplateFile extends TreasuryDocumentTemplateFile_Base {

    public static final String CONTENT_TYPE = "application/vnd.oasis.opendocument.text";
    public static final String FILE_EXTENSION = ".odt";

    protected TreasuryDocumentTemplateFile() {
        super();
        setBennu(Bennu.getInstance());
    }

    protected TreasuryDocumentTemplateFile(final TreasuryDocumentTemplate documentTemplate, final boolean active,
            final String displayName, final String fileName, final byte[] content) {
        this();
        this.init(displayName, fileName, content);
        setTreasuryDocumentTemplate(documentTemplate);
        setActive(active);

        documentTemplate.activateFile(this);

        checkRules();
        
        TreasuryDocumentTemplateFileDomainObject.createFromTreasuryDocumentTemplateFile(this);
    }

    private void checkRules() {
        if (getTreasuryDocumentTemplate() == null) {
            throw new TreasuryDomainException("error.TreasuryDocumentTemplateFile.documentTemplate.required");
        }
    }

    @Atomic
    public void edit(final TreasuryDocumentTemplate documentTemplate, final boolean active) {
        setTreasuryDocumentTemplate(documentTemplate);
        setActive(active);

        checkRules();
        
        TreasuryDocumentTemplateFileDomainObject.findAll().filter(o -> TreasuryDocumentTemplateFile.this == o.getTreasuryFile()).findFirst().get().edit(documentTemplate, active);
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

        setBennu(null);
        setTreasuryDocumentTemplate(null);
        super.delete();
        
        TreasuryDocumentTemplateFileDomainObject.findAll().filter(o -> TreasuryDocumentTemplateFile.this == o.getTreasuryFile()).findFirst().get().delete();
    }

    @Atomic
    static TreasuryDocumentTemplateFile create(final TreasuryDocumentTemplate documentTemplate, final String displayName,
            final String fileName, final byte[] content) {
        TreasuryDocumentTemplateFile documentTemplateFile =
                new TreasuryDocumentTemplateFile(documentTemplate, false, displayName, fileName, content);
        return documentTemplateFile;
    }

    public static Stream<TreasuryDocumentTemplateFile> findAll() {
        return Bennu.getInstance().getTreasuryDocumentTemplateFilesSet().stream();
    }

    public static Stream<TreasuryDocumentTemplateFile> findByDocumentTemplate(final TreasuryDocumentTemplate documentTemplate) {
        return documentTemplate.getTreasuryDocumentTemplateFilesSet().stream();
    }

    @Override
    public boolean isAccessible(User user) {
        return user != null;
    }
}
