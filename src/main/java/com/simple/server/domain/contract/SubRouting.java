package com.simple.server.domain.contract;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.simple.server.config.ContentType;
import com.simple.server.domain.IRec;
import com.simple.server.domain.log.LogEventSetting;

@JsonAutoDetect
@JsonDeserialize(as = SubRouting.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubRouting extends AContract{
	
	@JsonProperty("id")
	protected int id;
	protected String bodyContentType = "";
	protected Boolean useAuth = false;
	protected String bodyFldSeparator = "";	
	protected Boolean removeXmlAttributes = false;	
	protected Boolean useCharsetBase64 = false;
	protected Boolean useXmlDeclaration = false;
	protected Boolean disableRoutingPubErrSuccess = false;
	
	@Override
	public String getClazz() {
		return SubRouting.class.getName();
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}		
	public ContentType getBodyContentType() {
		return ContentType.fromValue(bodyContentType);
	}
	public void setBodyContentType(ContentType bodyContentType) {				
		this.bodyContentType = bodyContentType.toValue();
	}		
	public Boolean getUseAuth() {		
		return useAuth == null ? false : useAuth ;
	}
	public void setUseAuth(Boolean useAuth) {
		if (useAuth != null) {
			this.useAuth = useAuth;
		}
	}		
	public String getBodyFldSeparator() {
		return bodyFldSeparator;
	}
	public void setBodyFldSeparator(String bodyFldSeparator) {
		if (bodyFldSeparator != null) {
			this.bodyFldSeparator = bodyFldSeparator;
		}
	}			
	public Boolean getRemoveXmlAttributes() {
		return removeXmlAttributes == null ? false : removeXmlAttributes;
	}
	public void setRemoveXmlAttributes(Boolean removeXmlAttributes) {
		if (removeXmlAttributes != null) {
			this.removeXmlAttributes = removeXmlAttributes;
		}
	}
	public Boolean getUseCharsetBase64() {
		return useCharsetBase64 == null ? false : useCharsetBase64;
	}
	public void setUseCharsetBase64(Boolean useCharsetBase64) {
		if (useCharsetBase64 != null) {
			this.useCharsetBase64 = useCharsetBase64;
		}
	}
	public void setBodyContentType(String bodyContentType) {
		if (bodyContentType != null) {
			this.bodyContentType = bodyContentType;
		}
	}	
	public Boolean getUseXmlDeclaration() {
		return useXmlDeclaration == null ? false : useXmlDeclaration;
	}
	public void setUseXmlDeclaration(Boolean useXmlDeclaration) {
		if (useXmlDeclaration != null) {
			this.useXmlDeclaration = useXmlDeclaration;
		}
	}
	public Boolean getDisableRoutingPubErrSuccess() {
		return disableRoutingPubErrSuccess == null ? false : disableRoutingPubErrSuccess;
	}
	public void setDisableRoutingPubErrSuccess(Boolean disableRoutingPubErrSuccess) {
		if (disableRoutingPubErrSuccess != null) {
			this.disableRoutingPubErrSuccess = disableRoutingPubErrSuccess;
		}
	}
	@Override
	public void copyFrom(IRec rec) throws Exception{
		if (rec == null)
			return;
		if (rec instanceof LogEventSetting) {
			LogEventSetting les = (LogEventSetting) rec;
			this.setEventId(les.getEventId());
			this.setSubscriberId(les.getSubscriberId());
			this.setSubscriberHandler(les.getSubscriberHandler());			
		}		
	}
}
