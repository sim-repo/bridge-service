package com.simple.server.domain.contract;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


@JsonAutoDetect
@JsonDeserialize(as = ErrSubMsg.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrSubMsg extends AContract{
	
	private Integer id;
	private String storeClass;	
	
	@Override
	public String getClazz() {
		return this.getClass().getName();
	}	
	public Integer getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getStoreClass() {
		return storeClass;
	}
	public void setStoreClass(String storeClass) {
		this.storeClass = storeClass;
	}
	@Override
	public void copyFrom(IContract _msg) throws Exception {
		if(_msg == null)
			return;	
	}	
}
