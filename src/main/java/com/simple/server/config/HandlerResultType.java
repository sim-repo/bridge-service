package com.simple.server.config;

public enum HandlerResultType {
	flatXML("flatXML"), 
	flatJSON("flatJSON"), 
	complexJSON("complexJSON"), 
	firstFlatJSON("firstFlatJSON");
	
	private final String value;

	HandlerResultType(String value) {
		this.value = value;
	}

	public static HandlerResultType fromValue(String value) {
		if (value != null) {
			for (HandlerResultType ct : values()) {
				if (ct.value.equals(value)) {
					return ct;
				}
			}
		}
		return getDefault();
	}

	public String toValue() {
		return value;
	}

	public static HandlerResultType getDefault() {
		return flatXML;
	}
}
