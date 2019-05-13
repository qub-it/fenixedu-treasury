package org.fenixedu.treasury.services.integration;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.bennu.io.domain.GenericFile;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.bennu.scheduler.TaskRunner;
import org.fenixedu.bennu.scheduler.domain.SchedulerSystem;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.TreasuryFile;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.integration.ERPConfiguration;
import org.fenixedu.treasury.services.integration.erp.IERPExternalService;
import org.fenixedu.treasury.services.integration.erp.tasks.ERPExportSingleDocumentsTask;
import org.joda.time.DateTime;

import com.qubit.solution.fenixedu.bennu.webservices.domain.webservice.WebServiceClientConfiguration;
import com.qubit.solution.fenixedu.bennu.webservices.domain.webservice.WebServiceConfiguration;

import pt.ist.fenixframework.Atomic;

public class FenixEDUTreasuryPlatformDependentServices implements ITreasuryPlatformDependentServices {

    private static final int WAIT_TRANSACTION_TO_FINISH_MS = 500;

    public void scheduleSingleDocument(final FinantialDocument finantialDocument) {
        final List<FinantialDocument> documentsToExport = org.fenixedu.treasury.services.integration.erp.ERPExporterManager
                .filterDocumentsToExport(Collections.singletonList(finantialDocument).stream());

        if (documentsToExport.isEmpty()) {
            return;
        }

        final String externalId = documentsToExport.iterator().next().getExternalId();

        new Thread() {

            @Override
            @Atomic
            public void run() {
                try {
                    Thread.sleep(WAIT_TRANSACTION_TO_FINISH_MS);
                } catch (InterruptedException e) {
                }

                SchedulerSystem.queue(new TaskRunner(new ERPExportSingleDocumentsTask(externalId)));
            };

        }.start();
    }

    public IERPExternalService getERPExternalServiceImplementation(final ERPConfiguration erpConfiguration) {
        final String className = erpConfiguration.getImplementationClassName();

        try {

            //force the "invocation" of class name
            Class cl = Class.forName(className);
            WebServiceClientConfiguration clientConfiguration = WebServiceConfiguration.readByImplementationClass(className);

            IERPExternalService client = clientConfiguration.getClient();

            return client;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TreasuryDomainException("error.ERPConfiguration.invalid.external.service");
        }
    }

    /* File */

    @Override
    public byte[] getFileContent(final IGenericFile genericFile) {
        try {
            GenericFile file = (GenericFile) MethodUtils.invokeMethod(genericFile, "getTreasuryFile", null);

            return file.getContent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getFileSize(final IGenericFile genericFile) {
        try {
            GenericFile file = (GenericFile) MethodUtils.invokeMethod(genericFile, "getTreasuryFile", null);

            return file.getSize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DateTime getFileCreationDate(final IGenericFile genericFile) {
        try {
            GenericFile file = (GenericFile) MethodUtils.invokeMethod(genericFile, "getTreasuryFile", null);

            return file.getCreationDate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getFilename(final IGenericFile genericFile) {
        try {
            GenericFile file = (GenericFile) MethodUtils.invokeMethod(genericFile, "getTreasuryFile", null);

            return file.getFilename();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getFileStream(final IGenericFile genericFile) {
        try {
            GenericFile file = (GenericFile) MethodUtils.invokeMethod(genericFile, "getTreasuryFile", null);

            return file.getStream();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getFileContentType(final IGenericFile genericFile) {
        try {
            GenericFile file = (GenericFile) MethodUtils.invokeMethod(genericFile, "getTreasuryFile", null);

            return file.getContentType();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createFile(final IGenericFile genericFile, final String fileName, final String contentType,
            final byte[] content) {
        try {
            GenericFile file = TreasuryFile.create(fileName, contentType, content);

            MethodUtils.invokeMethod(genericFile, "setTreasuryFile", file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void deleteFile(final IGenericFile genericFile) {
        try {
            GenericFile file = (GenericFile) MethodUtils.invokeMethod(genericFile, "getTreasuryFile", null);

            file.delete();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /* User */

    @Override
    public String getLoggedUsername() {
        if(Authenticate.getUser() == null) {
            return null;
        }
        
        return Authenticate.getUser().getUsername();
    }

    /* Locales */

    @Override
    public Set<Locale> availableLocales() {
        return CoreConfiguration.supportedLocales();
    }

    /* Bundles */

    @Override
    public String bundle(final String bundleName, final String key, final String... args) {
        return BundleUtil.getString(bundleName, key, args);
    }

    @Override
    public String bundle(final Locale locale, final String bundleName, final String key, final String... args) {
        return BundleUtil.getString(bundleName, locale, key, args);
    }

    @Override
    public LocalizedString bundleI18N(final String bundleName, final String key, final String... args) {
        return BundleUtil.getLocalizedString(bundleName, key, args);
    }

    @Override
    public <T> String versioningCreatorUsername(T obj) {
        return readVersioningCreatorUsername(obj);
    }
    
    @Override
    public <T> DateTime versioningCreationDate(T obj) {
        return readVersioningCreationDate(obj);
    }
    
    @Override
    public <T> String versioningUpdatorUsername(T obj) {
        return readVersioningUpdatorUsername(obj);
    }
    
    @Override
    public <T> DateTime versioningUpdateDate(T obj) {
        return readVersioningUpdateDate(obj);
    }
    
    public static <T> String readVersioningCreatorUsername(T obj) {
        try {
            String username = (String) PropertyUtils.getProperty(obj, "versioningCreator");

            return username;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> DateTime readVersioningCreationDate(T obj) {
        try {
            DateTime creationDate = (DateTime) PropertyUtils.getProperty(obj, "versioningCreationDate");

            return creationDate;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String readVersioningUpdatorUsername(T obj) {
        try {
            Object versioningUpdatedBy = PropertyUtils.getProperty(obj, "versioningUpdatedBy");

            if (versioningUpdatedBy == null) {
                return null;
            }

            return (String) PropertyUtils.getProperty(versioningUpdatedBy, "username");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> DateTime readVersioningUpdateDate(T obj) {
        try {
            Object versioningUpdateDate = PropertyUtils.getProperty(obj, "versioningUpdateDate");

            if (versioningUpdateDate == null) {
                return null;
            }

            return (DateTime) PropertyUtils.getProperty(versioningUpdateDate, "date");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
