package org.fenixedu.bennu.io.domain;

import static org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory.treasuryPlatformServices;

import java.io.InputStream;

import org.joda.time.DateTime;

import pt.ist.fenixframework.DomainObject;

public interface IGenericFile extends DomainObject {

	public boolean isAccessible(final String username);

	public void delete();
	
	public String getFileId();
	
	public void setFileId(final String id);
	
	default public byte[] getContent() {
		return treasuryPlatformServices().getFileContent(this);
	}

	default public Long getSize() {
		return treasuryPlatformServices().getFileSize(this);
	}

	default public DateTime getCreationDate() {
		return treasuryPlatformServices().getFileCreationDate(this);
	}

	default public String getFilename() {
		return treasuryPlatformServices().getFilename(this);
	}

	default public InputStream getStream() {
		return treasuryPlatformServices().getFileStream(this);
	}

	default public String getContentType() {
		return treasuryPlatformServices().getFileContentType(this);
	}
	
}