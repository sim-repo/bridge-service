package com.simple.server.domain.contract;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonAutoDetect
@JsonDeserialize(as = RedirectRouting.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedirectRouting extends AContract{

	private static final long serialVersionUID = 1L;
	
	@JsonProperty("id")
	protected int id;
	
	protected String methodName;	
	protected String url;	
	protected Boolean useAuth;
	protected Boolean isPost;
	protected String contentType;
	
	@Override
	public String getClazz() {
		return RedirectRouting.class.getName();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Boolean getUseAuth() {
		return useAuth;
	}

	public void setUseAuth(Boolean useAuth) {
		this.useAuth = useAuth;
	}

	public Boolean getIsPost() {
		return isPost;
	}

	public void setIsPost(Boolean isPost) {
		this.isPost = isPost;
	}
	
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
