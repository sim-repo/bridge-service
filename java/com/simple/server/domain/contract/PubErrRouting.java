package com.simple.server.domain.contract;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.simple.server.config.EndpointType;

@JsonAutoDetect
@JsonDeserialize(as = PubErrRouting.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PubErrRouting extends AContract{

	@JsonProperty("id")
	int id;
	
	@Override
	public String getClazz() {
		return PubErrRouting.class.getName();
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

}
