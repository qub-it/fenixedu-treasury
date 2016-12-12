package org.fenixedu.treasury.domain.integration;

import org.joda.time.DateTime;

public class IntegrationOperationLogBean {

    private StringBuilder integrationLogBuilder = new StringBuilder();
    private StringBuilder errorLogBuilder = new StringBuilder();
    
    private StringBuilder soapInboundMessageBuilder = new StringBuilder();
    private StringBuilder soapOutboundMessageBuilder = new StringBuilder();
    
    private String erpOperationId;
    
    public void appendIntegrationLog(String message) {
        integrationLogBuilder.append(new DateTime().toString()).append(message).append("\n");
    }
    
    public void appendErrorLog(String message) {
        errorLogBuilder.append(new DateTime().toString()).append(message).append("\n");
    }
    
    public void defineSoapInboundMessage(final String soapInboundMessage) {
        soapInboundMessageBuilder.append(soapInboundMessage);
    }
    
    public void defineSoapOutboundMessage(final String soapOutboundMessage) {
        soapOutboundMessageBuilder.append(soapOutboundMessage);
    }

    public String getIntegrationLog() {
        return integrationLogBuilder.toString();
    }
    
    public String getErrorLog() {
        return errorLogBuilder.toString();
    }
    
    public String getSoapInboundMessage() {
        return soapInboundMessageBuilder.toString();
    }
    
    public String getSoapOutboundMessage() {
        return soapOutboundMessageBuilder.toString();
    }
    
    public String getErpOperationId() {
        return erpOperationId;
    }
    
    public void setErpOperationId(String erpOperationId) {
        this.erpOperationId = erpOperationId;
    }
}
