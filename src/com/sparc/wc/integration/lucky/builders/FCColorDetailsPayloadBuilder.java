package com.sparc.wc.integration.lucky.builders;

import com.lcs.wc.color.LCSColor;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTyped;
import com.sparc.wc.integration.lucky.domain.FCColorDetailsPayload;
import com.sparc.wc.integration.lucky.repository.FCColorRepository;
import com.sparc.wc.integration.util.SparcIntegrationUtil;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_COLOR_DESC_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_NEW_COLOR_FLAG_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_NEW_COLOR_FLAG_NEW_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_NEW_COLOR_FLAG_UPDATE_VALUE;

/**
 * Builds a Lucky/FC Color Details payload.<br>
 * 
 * @author Acnovate
 */
public class FCColorDetailsPayloadBuilder {
	
	private String colorId;
	private FCColorDetailsPayload colorDetails;

	/**
	 * Constructs a ColorwayPayloadBuilder instance.
	 * @param colorDetails The color detail payload to build.
	 */
	public FCColorDetailsPayloadBuilder(FCColorDetailsPayload colorDetails) {
		this.colorDetails = colorDetails;
	}

	public FCColorDetailsPayloadBuilder setColorId(String colorId) {
		this.colorId = colorId;
		return this;
	}
	
	/**
	 * Builds the attributes for the payload from the Lucky Colorways Color retrieved from PLM.  
	 * @return The flex Color instance used to extract the color attributes for the payload.
	 * @throws WTException If an error occurs while accessing/retrieving thecolor and/or its attributes from PLM.
	 * @throws Exception If there's no color found for the FC Color ID used to create this builder.
	 */
	private LCSColor buildColorAttributes() throws WTException, Exception {
		
		LCSColor flexColor = FCColorRepository.getColor(colorId);
		
		if (flexColor == null) {
			throw new Exception("The Lucky Colorways Color with FC Color ID '" + colorId + "' couldn't be found in the system.");
		}
		
		colorDetails.getData().setSclCLR_CDE(colorId);
		
		Object colorDesc = SparcIntegrationUtil.getValueFrom(flexColor, LUCKY_FC_COLOR_DESC_ATTR, false);
		
		if (colorDesc != null) {
			colorDetails.getData().setSclCLR_DESC((String)colorDesc);
			colorDetails.getData().setSclCLR_CATCLR((String)colorDesc);
		}
		
		Object newColorFlag = SparcIntegrationUtil.getValueFrom(flexColor, LUCKY_FC_NEW_COLOR_FLAG_ATTR, false);
		
		//Force set New Color Flag to "New" if unassigned.
		if (newColorFlag == null || 
				(!LUCKY_FC_NEW_COLOR_FLAG_UPDATE_VALUE.equalsIgnoreCase((String)newColorFlag) &&
						!LUCKY_FC_NEW_COLOR_FLAG_NEW_VALUE.equalsIgnoreCase((String)newColorFlag))) {
			flexColor.setValue(LUCKY_FC_NEW_COLOR_FLAG_ATTR, LUCKY_FC_NEW_COLOR_FLAG_NEW_VALUE);
		}
		
		colorDetails.getData().setSclNewcolorFlag(lookupEnumDisplayValue(flexColor, LUCKY_FC_NEW_COLOR_FLAG_ATTR));
		
		return flexColor;
	}
	
	/**
	 * Builds the FC Color Payload from PLM.
	 * @return The FC Color Payload.
	 * @throws WTException When an error occurs while accessing or retrieving the various data elements from PLM.
	 * @ throws WTPropertyVetoException When an error occurs while updating the Color control flags at PLM.
	 * @throws Exception When a different error, i.e. validation fail, occurs.
	 */
	public FCColorDetailsPayload build() throws WTException, WTPropertyVetoException, Exception {
		
		LCSColor color = buildColorAttributes();
		FCColorRepository.updateColorControlFlags(color);	
		
		return colorDetails;
	}
	
	/**
	 * Retrieve the enumeration's display name for the given flex type object and attribute name.
	 * @param flexObj The flex objext containing the attribute.
	 * @param attrName The name of the attribute to lookup for the display name value from.
	 * @return The enumeration's displat name for the given attribute.
	 * @throws WTException If an error occurs while extracting the display name value from the flex object.
	 */
	private String lookupEnumDisplayValue(FlexTyped flexObj, String attrName) throws WTException {
		
		return lookupAttrEnumKeyValue(flexObj, attrName, "displayName");
		
	}
	
	/**
	 * Retrieve the enumeration's value for given flex type object, attribute name and Enum attribute name.
	 * @param flexObj The flex objext containing the attribute to get the value & enum from.
	 * @param attrName The name of the attribute to lookup for the Enum key value from.
	 * @param enumAttrName The Enum attribute to get the value from, i.e. displayName or _CUSTOM_scSecondaryKey
	 * @return The enumeration's value for the given attribute & enum attribute.
	 * @throws WTException If an error occurs while extracting the enumeration value from the flex object.
	 */
	private String lookupAttrEnumKeyValue(FlexTyped flexObj, String attrName, String enumAttrName) throws WTException {
		
		String value = "";
		
		if (flexObj == null || attrName == null || enumAttrName == null) {
			return value;
		}
		
		final Object flexObjValue = flexObj.getValue(attrName);
		
		if (flexObjValue == null || !(flexObjValue instanceof String)) {
			return value;
		}
		
		final FlexTypeAttribute flexAttr = flexObj.getFlexType().getAttribute(attrName);
		
		if (flexAttr != null && flexAttr.getAttValueList() != null) {
			value = flexAttr.getAttValueList().get((String)flexObjValue, enumAttrName);
		}
		
		return value;
		
	}

}
