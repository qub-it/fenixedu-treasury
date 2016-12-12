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
package org.fenixedu.treasury.services.integration.erp.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class DocumentsInformationOutput {
    private String requestId;
    private List<DocumentStatusWS> documentStatus;
    
    private String soapOutboundMessage;
    private String soapInboundMessage;

    private List<String> otherMessages = Lists.newArrayList();
    
    public DocumentsInformationOutput() {
        documentStatus = new ArrayList<DocumentStatusWS>();
    }

    public List<DocumentStatusWS> getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(List<DocumentStatusWS> documentStatus) {
        this.documentStatus = documentStatus;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getSoapInboundMessage() {
        return soapInboundMessage;
    }
    
    public void setSoapInboundMessage(String soapInboundMessage) {
        this.soapInboundMessage = soapInboundMessage;
    }
    
    public String getSoapOutboundMessage() {
        return soapOutboundMessage;
    }
    
    public void setSoapOutboundMessage(String soapOutboundMessage) {
        this.soapOutboundMessage = soapOutboundMessage;
    }
    
    public List<String> getOtherMessages() {
        return otherMessages;
    }
    
    public void setOtherMessages(List<String> otherMessages) {
        this.otherMessages = otherMessages;
    }

}
