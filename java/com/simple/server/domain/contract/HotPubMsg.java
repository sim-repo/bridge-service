package com.simple.server.domain.contract;

public class HotPubMsg extends AContract{
	
	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String getClazz() {
		return this.getClass().getName();
	}
	
	
	
}
