package com.simple.server.domain.contract;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.simple.server.domain.IRec;


@JsonAutoDetect
@JsonDeserialize(as = UniMsg.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UniMsg extends AContract{
	
	private static final long serialVersionUID = 1L;
	private int id;
	private String body;	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	@Override
	public String getClazz() {
		return UniMsg.class.getName();
	}
}
