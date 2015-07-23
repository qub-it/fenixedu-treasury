package org.fenixedu.treasury.services.integration.erp.dto;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.util.Constants;

public class DocumentStatusWS {
    private String documentNumber;
    private StatusType integrationStatus;
    private String errorDescription;

    public enum StatusType {
        PENDING, ERROR, SUCCESS;

        public LocalizedString getDescriptionI18N() {
            return BundleUtil.getLocalizedString(Constants.BUNDLE, getClass().getSimpleName() + "." + name());
        }
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public StatusType getIntegrationStatus() {
        return integrationStatus;
    }

    public void setIntegrationStatus(StatusType integrationStatus) {
        this.integrationStatus = integrationStatus;
    }

    public boolean isIntegratedWithSuccess() {
        return this.getIntegrationStatus() != null && this.getIntegrationStatus().equals(StatusType.SUCCESS);
    }
}