package com.sparc.wc.integration.aero.domain;

import java.util.Objects;

import com.sparc.wc.integration.aero.builders.AeroPayloadAttributeBuilder;

/**
 * Defines an integration attribute to be included as part of a payload within Aeropostale integrations.
 * 
 * FIXES/AMENDMENTS:<br>
 * - Added support for Enum's Aero Secondary Key.<br>
 * - Task #11142: Added compositeValueDelimiter attribute to support formatting of delimited values from composite attributes.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 */
public class AeroPayloadAttribute {
	
	private boolean noBlanks;
	private boolean lockAttrUpdate;
	private boolean placeholder;
	private boolean useEnumSecondaryKey;
	private boolean useEnumSecondaryKeyAero;
	private boolean useEnumDisplayName;
	
	private String jsonAttributeAlias;
	private String flexAttributeName;
	private String enumAttrKey;
	private String objectPath;
	private String objectRefAttrName;
	private String objectRefPath;
	private String format;
	private String formatClass;
	private String compositeValueDelimiter;
	
	/**
	 * Creates a builder instance to build a Aero Article Ids payload.
	 */
	public static AeroPayloadAttributeBuilder newBuilder() {
		return new AeroPayloadAttributeBuilder(new AeroPayloadAttribute());
	}
	
	private AeroPayloadAttribute() {
		
	}
	
	public boolean isNoBlanks() {
		return noBlanks;
	}

	public void setNoBlanks(boolean noBlanks) {
		this.noBlanks = noBlanks;
	}

	public boolean isLockAttrUpdate() {
		return lockAttrUpdate;
	}

	public void setLockAttrUpdate(boolean lockAttrUpdate) {
		this.lockAttrUpdate = lockAttrUpdate;
	}

	public boolean isPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(boolean placeholder) {
		this.placeholder = placeholder;
	}

	public boolean isUseEnumSecondaryKey() {
		return useEnumSecondaryKey;
	}

	public void setUseEnumSecondaryKey(boolean useEnumSecondaryKey) {
		this.useEnumSecondaryKey = useEnumSecondaryKey;
	}

	public boolean isUseEnumDisplayName() {
		return useEnumDisplayName;
	}

	public void setUseEnumDisplayName(boolean useEnumDisplayName) {
		this.useEnumDisplayName = useEnumDisplayName;
	}
	
	public boolean isUseEnumSecondaryKeyAero() {
		return useEnumSecondaryKeyAero;
	}

	public void setUseEnumSecondaryKeyAero(boolean useEnumSecondaryKeyAero) {
		this.useEnumSecondaryKeyAero = useEnumSecondaryKeyAero;
	}

	public String getJsonAttributeAlias() {
		return jsonAttributeAlias;
	}

	public void setJsonAttributeAlias(String jsonAttributeAlias) {
		this.jsonAttributeAlias = jsonAttributeAlias;
	}

	public String getFlexAttributeName() {
		return flexAttributeName;
	}

	public void setFlexAttributeName(String flexAttributeName) {
		this.flexAttributeName = flexAttributeName;
	}
	
	public String getEnumAttrKey() {
		return enumAttrKey;
	}

	public void setEnumAttrKey(String enumAttrKey) {
		this.enumAttrKey = enumAttrKey;
	}

	public String getObjectPath() {
		return objectPath;
	}

	public void setObjectPath(String objectPath) {
		this.objectPath = objectPath;
	}
	
	public String getObjectRefAttrName() {
		return objectRefAttrName;
	}
	
	public void setObjectRefAttrName(String objectRefAttrName) {
		this.objectRefAttrName = objectRefAttrName;
	}
	
	public String getObjectRefPath() {
		return objectRefPath;
	}
	
	public void setObjectRefPath(String objectRefPath) {
		this.objectRefPath = objectRefPath;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getFormatClass() {
		return formatClass;
	}

	public void setFormatClass(String formatClass) {
		this.formatClass = formatClass;
	}
	
	public String getCompositeValueDelimiter() {
		return compositeValueDelimiter;
	}

	public void setCompositeValueDelimiter(String compositeValueDelimiter) {
		this.compositeValueDelimiter = compositeValueDelimiter;
	}

	@Override
	public int hashCode() {
		return Objects.hash(compositeValueDelimiter, enumAttrKey, flexAttributeName, format, formatClass,
				jsonAttributeAlias, lockAttrUpdate, noBlanks, objectPath, objectRefAttrName, objectRefPath, placeholder,
				useEnumDisplayName, useEnumSecondaryKey, useEnumSecondaryKeyAero);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AeroPayloadAttribute other = (AeroPayloadAttribute) obj;
		return Objects.equals(compositeValueDelimiter, other.compositeValueDelimiter)
				&& Objects.equals(enumAttrKey, other.enumAttrKey)
				&& Objects.equals(flexAttributeName, other.flexAttributeName) && Objects.equals(format, other.format)
				&& Objects.equals(formatClass, other.formatClass)
				&& Objects.equals(jsonAttributeAlias, other.jsonAttributeAlias)
				&& lockAttrUpdate == other.lockAttrUpdate && noBlanks == other.noBlanks
				&& Objects.equals(objectPath, other.objectPath)
				&& Objects.equals(objectRefAttrName, other.objectRefAttrName)
				&& Objects.equals(objectRefPath, other.objectRefPath) && placeholder == other.placeholder
				&& useEnumDisplayName == other.useEnumDisplayName && useEnumSecondaryKey == other.useEnumSecondaryKey
				&& useEnumSecondaryKeyAero == other.useEnumSecondaryKeyAero;
	}

	@Override
	public String toString() {
		return "AeroPayloadAttribute [noBlanks=" + noBlanks + ", lockAttrUpdate=" + lockAttrUpdate + ", placeholder="
				+ placeholder + ", useEnumSecondaryKey=" + useEnumSecondaryKey + ", useEnumSecondaryKeyAero="
				+ useEnumSecondaryKeyAero + ", useEnumDisplayName=" + useEnumDisplayName + ", jsonAttributeAlias="
				+ jsonAttributeAlias + ", flexAttributeName=" + flexAttributeName + ", enumAttrKey=" + enumAttrKey
				+ ", objectPath=" + objectPath + ", objectRefAttrName=" + objectRefAttrName + ", objectRefPath="
				+ objectRefPath + ", format=" + format + ", formatClass=" + formatClass + ", compositeValueDelimiter="
				+ compositeValueDelimiter + "]";
	}
	
}
