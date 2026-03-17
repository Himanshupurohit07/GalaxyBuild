package com.sparc.wc.integration.aero.repository;

/**
 * Enumeration for some out-of-the-box flex attribute variable types.<br>
 * 
 * FIXES/AMENDMENTS:
 * - Task #9928 (UAT): Added DRIVEN Attribute variable type to support the processing of related attribute values.
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 */
public enum FlexAttrVariableTypes {
	OBJECT_REF_LIST,
	BOOLEAN,
	DATE,
	COMPOSITE,
	MOALIST,
	MOAENTRY,
	CHOICE,
	DRIVEN
}
