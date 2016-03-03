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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

import com.google.common.base.Strings;

public abstract class IntegrationOperation extends IntegrationOperation_Base {

    protected IntegrationOperation() {
        super();
    }

    protected void init(final org.joda.time.DateTime executionDate, final boolean processed, final boolean success,
            final java.lang.String errorLog) {
        setExecutionDate(executionDate);
        setProcessed(processed);
        setSuccess(success);
        setErrorLog(errorLog);
        checkRules();
    }

    private void checkRules() {

        if (Strings.isNullOrEmpty(this.getErrorLog()) == false) {
            this.setSuccess(false);
        }
        //
        // CHANGE_ME add more busines validations
        //

        // CHANGE_ME In order to validate UNIQUE restrictions
        // if (findByExecutionDate(getExecutionDate().count()>1)
        // {
        // throw new
        // TreasuryDomainException("error.IntegrationOperation.executionDate.duplicated");
        // }
        // if (findByProcessed(getProcessed().count()>1)
        // {
        // throw new
        // TreasuryDomainException("error.IntegrationOperation.processed.duplicated");
        // }
        // if (findBySuccess(getSuccess().count()>1)
        // {
        // throw new
        // TreasuryDomainException("error.IntegrationOperation.success.duplicated");
        // }
        // if (findByErrorLog(getErrorLog().count()>1)
        // {
        // throw new
        // TreasuryDomainException("error.IntegrationOperation.errorLog.duplicated");
        // }
    }

    @Atomic
    public void edit(final org.joda.time.DateTime executionDate, final boolean processed, final boolean success,
            final java.lang.String errorLog) {
        setExecutionDate(executionDate);
        setProcessed(processed);
        setSuccess(success);
        setErrorLog(errorLog);
        checkRules();
    }

    public boolean isDeletable() {
        return true;
    }

    @Atomic
    public void delete() {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.IntegrationOperation.cannot.delete");
        }
        this.setFinantialInstitution(null);
        if (this.getFile() != null) {
            this.getFile().delete();
        }
        
        if(getSoapInboundMessageOperationFile() != null) {
            getSoapInboundMessageOperationFile().delete();
        }
        
        if(getSoapOutboundMessageOperationFile() != null) {
            getSoapOutboundMessageOperationFile().delete();
        }
        
        if(getErrorLogFile() != null) {
            getErrorLogFile().delete();
        }
        
        if(getIntegrationLogFile() != null) {
            getIntegrationLogFile().delete();
        }
        
        this.setFile(null);
        deleteDomainObject();
    }

    @Atomic
    public void appendInfoLog(String message) {
        String infoLog = this.getIntegrationLog();
        if (infoLog == null) {
            setIntegrationLog("");
        }
        StringBuilder builder = new StringBuilder();
        builder.append(this.getIntegrationLog()).append("\n");
        builder.append(new DateTime().toString()).append(message);
        setIntegrationLog(builder.toString());
        checkRules();
    }

    @Atomic
    public void appendErrorLog(String message) {
        String errorLog = this.getErrorLog();
        if (errorLog == null) {
            setErrorLog("");
        }
        StringBuilder builder = new StringBuilder();
        builder.append(this.getErrorLog()).append("\n");
        builder.append(new DateTime().toString()).append(message);
        setErrorLog(builder.toString());
        checkRules();
    }

    @Atomic
    public void defineSoapInboundMessage(final String soapInboundMessage) {
        final String filename =
                String.format("soap_inbound_message_%s_%s.xml", this.getExternalId(), new DateTime().toString("%ddMMyyyyHHmmss"));

        if(getSoapInboundMessageOperationFile() != null) {
            getSoapInboundMessageOperationFile().delete(); 
        }
        
        OperationFile.createSoapInboundMessage(filename, zipValue(soapInboundMessage != null ? soapInboundMessage : "").getBytes(), this);
    }

    @Atomic
    public void defineSoapOutboutMessage(final String soapOutboundMessage) {
        final String filename =
                String.format("soap_outbound_message_%s_%s.xml", this.getExternalId(), new DateTime().toString("%ddMMyyyyHHmmss"));

        if(getSoapOutboundMessageOperationFile() != null) {
            getSoapOutboundMessageOperationFile().delete(); 
        }
        
        OperationFile.createSoapOutboundMessage(filename, zipValue(soapOutboundMessage != null ? soapOutboundMessage : "").getBytes(), this);
    }

    @Override
    public void setSoapInboundMessage(String soapInboundMessage) {
        String value = soapInboundMessage;
        if (value != null) {
            value = zipValue(value);
        }
        super.setSoapInboundMessage(value);
    }

    @Override
    public String getSoapInboundMessage() {
        if(getSoapInboundMessageOperationFile() != null) {
            byte[] content = getSoapInboundMessageOperationFile().getContent();
            
            try {
                return unzip(new String(content, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else {
            return unzip(super.getSoapInboundMessage());
        }
    }

    @Override
    public void setSoapOutboundMessage(String soapOutboundMessage) {
        String value = soapOutboundMessage;
        if (value != null) {
            value = zipValue(value);
        }
        super.setSoapOutboundMessage(value);
    }

    @Override
    public String getSoapOutboundMessage() {
        if(getSoapOutboundMessageOperationFile() != null) {
            byte[] content = getSoapOutboundMessageOperationFile().getContent();
            
            try {
                return unzip(new String(content, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else {
            return unzip(super.getSoapOutboundMessage());
        }
    }

    @Override
    public void setIntegrationLog(String integrationLog) {
        final String filename =
                String.format("integration_log_%s_%s.xml", this.getExternalId(), new DateTime().toString("%ddMMyyyyHHmmss"));
        
        if(getIntegrationLogFile() != null) {
            getIntegrationLogFile().delete(); 
        }
        
        OperationFile.createIntegrationLog(filename, zipValue(integrationLog != null ? integrationLog : "").getBytes(), this);
    }

    @Override
    public String getIntegrationLog() {
        if(getIntegrationLogFile() != null) {
            byte[] content = getIntegrationLogFile().getContent();
            
            try {
                return unzip(new String(content, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        
        return unzip(super.getIntegrationLog());
    }

    @Override
    public void setErrorLog(String errorLog) {
        final String filename =
                String.format("error_log_%s_%s.xml", this.getExternalId(), new DateTime().toString("%ddMMyyyyHHmmss"));
        
        if(getErrorLogFile() != null) {
            getErrorLogFile().delete();
        }
        
        OperationFile.createErrorLog(filename, zipValue(errorLog != null ? errorLog : "").getBytes(), this);
    }

    @Override
    public String getErrorLog() {
        if(getErrorLogFile() != null) {
            byte[] content = getErrorLogFile().getContent();
            
            try {
                return unzip(new String(content, "UTF-8"));
            } catch(UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        
        return unzip(super.getErrorLog());
    }

    // @formatter:off
    /* **************
     * ZIP OPERATIONS
     * **************
     */ 
    // @formatter:on
    
    private String zipValue(String value) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream;
        try {
            gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            gzipOutputStream.write(value.getBytes());
            gzipOutputStream.flush();
            gzipOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(Base64.getEncoder().encode(byteArrayOutputStream.toByteArray()));
    }

    private String unzip(String possibleZippedString) {
        String value = possibleZippedString;
        if (value != null) {
            try {
                GZIPInputStream gzipInputStream =
                        new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(possibleZippedString)));
                value = IOUtils.toString(gzipInputStream);
                gzipInputStream.close();
            } catch (Throwable t) {
                // mostprobably is not a zipped string so we just return the value
                value = possibleZippedString;
            }
        }
        return value;
    }

    
}
