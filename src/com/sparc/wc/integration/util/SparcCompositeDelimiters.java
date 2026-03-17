package com.sparc.wc.integration.util;

/**
 * Enumeration for various delimiters available to use when concatenating values from composite attributes.
 * 
 * @author Acnovate
 * @see "Task #11142 Aero Article Integration: Add Garment Content attribute to aero outbound colorway API"
 */
public enum SparcCompositeDelimiters {
	COMMA(","),
	SPACE(" "),
	EMPTY(""),
	COLON(":"),
	SEMICOLON(";"),
	UNDERSCORE("_"),
	HYPHEN("-"),
	FLEX("|~*~|");
	
	private String value;
	
	SparcCompositeDelimiters(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
