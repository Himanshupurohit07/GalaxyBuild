package com.sparc.wc.integration.lucky.plugins;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.lcs.wc.color.LCSColor;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.sparc.wc.integration.lucky.repository.FCColorRepository;

import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.method.MethodContext;
import wt.util.WTException;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_INTEGRATION_CONTEXT_FLAG_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_COLOR_SENT_TO_FC_FLAG_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_SENT_TO_FC_NO_VALUE;

/**
 * Flex Plugin for use to update the scColorSentToFC flag based on the update of selected integration attributes related to Lucky Colorways Colors.
 * 
 * @author Acnovate
 */
public class FCColorPlugin {

	private static final String COLORATTR = LCSProperties.get(
			"com.sparc.wc.integration.lucky.plugins.SourcingConfigPlugin.FCColorPlugin.updateFCColorFlag.COLORAttributes", "");
	
	private static final Logger LOGGER = LogR.getLogger(FCColorPlugin.class.getName());
	
	/**
	 * Main entry point for this plugin to trigger whenever a Lucky Colorway Color is modified.<br> 
	 * It'll look for changes to selected attributes of the color (those applicable to the FC Color api integration) and 
	 * update the color integration flag "scColorSentToFC" accordingly.
	 * @param obj The windchill object updated, which in this case should be a color (LCSColor).
	 * @throws WTException If an error occurs while validating changed attributes from PLM or 
	 * while attempting to update the integration flag for the color. 
	 */
	public static void updateFCColorFlag(WTObject obj) throws WTException {
		LOGGER.info("==============in updateFCColorFlag=========");
        LOGGER.debug("WTObject type: " + obj.getDisplayIdentity());
        
        if (obj instanceof LCSColor) {
        	
        	validateColor((LCSColor)obj);
        	
        } else {
        	LOGGER.debug("Object type " + obj.getDisplayIdentity() + " ignored.");
        }
        
        LOGGER.info("=============DONE with updateFCColorFlag=========");
	}
	
	/**
	 * Checks whether the color being processed by this plugin is eligible for updating the Sent To FC flag to "No".
	 * @param color The color to validate.
	 * @throws WTException If an error occurs while accessing or updating color attributes at PLM.
	 */
	private static void validateColor(LCSColor color) throws WTException {
		
		LOGGER.debug("---color---------" + color);
		
		if (color.getPersistInfo() == null || color.getPersistInfo().getObjectIdentifier() == null) {
            LOGGER.info("Detected new color, skipping plugin check.");
            return;
        }
		
		if (isFromIntegrationOrigin(color)) {
			LOGGER.debug("FC Color updated from the integration, skipping plugin check.");
			return;
		}
		
		String sendToFC = (String)color.getValue(LUCKY_FC_COLOR_SENT_TO_FC_FLAG_ATTR);
		
		if(LUCKY_FC_SENT_TO_FC_NO_VALUE.equalsIgnoreCase(sendToFC)) {
			LOGGER.debug("Sent To FC flag is already set to No, skipping plugin check.");
			return;
		}
		
		LCSColor prevColor = (LCSColor)LCSQuery.findObjectById(
				"OR:com.lcs.wc.color.LCSColor:" + color.getPersistInfo().getObjectIdentifier().getId(), false);
		
		if (isChanged(color, prevColor, COLORATTR)) {
			color.setValue(LUCKY_FC_COLOR_SENT_TO_FC_FLAG_ATTR, LUCKY_FC_SENT_TO_FC_NO_VALUE);
			LOGGER.debug("Integration attribute(s) for color " + color.getName() + " have been changed. "
					+ "Flag " + LUCKY_FC_COLOR_SENT_TO_FC_FLAG_ATTR + " value is updated.");
		} else {
			LOGGER.debug("No integration attributes changes found for color " + color.getName() + ". "
					+ "Flag " + LUCKY_FC_COLOR_SENT_TO_FC_FLAG_ATTR + " value not updated.");
		}
		
	}
	
	/**
	 * Inspect the PLM's MethodContext to confirm whether the update to the given color has been triggered from the FC Color integration or not.
	 * @param color The color instance being updated.
	 * @return true if the color update has been triggered from the FC Color integration, false otherwise.
	 */
	private static boolean isFromIntegrationOrigin(LCSColor color) {
		
		boolean isIntegration = false;
		
		if (MethodContext.getContext().containsKey(LUCKY_FC_INTEGRATION_CONTEXT_FLAG_KEY)) {
			
			String updatedColorKey = FCColorRepository.getColorContextId(color);
			LOGGER.debug("updatedColorKey---------------------" + updatedColorKey);
			
            String updatedColorKeyCtx = (String) MethodContext.getContext().get(LUCKY_FC_INTEGRATION_CONTEXT_FLAG_KEY);
            LOGGER.debug("updatedColorKeyCtx---------------------" + updatedColorKeyCtx);
            
            if (updatedColorKey.equalsIgnoreCase(updatedColorKeyCtx)) {
                MethodContext.getContext().remove(LUCKY_FC_INTEGRATION_CONTEXT_FLAG_KEY);
                isIntegration = true;
            }
            
        }
		
		return isIntegration;
	}
	
	/**
     * Checks whether current flex object is changed when compared to its previous version.
     * @param current The current flex object to check.
     * @param previous The previous flex object to compare against the current one.
     * @param attributes List of flex object attributes to use for the comparison.
     * @return true if the object has changed or if no previous object was provided, false otherwise.
     * @throws WTException If an error occurs while verifying attribute values from the current and/or previous flex objects.
     */
    private static boolean isChanged(FlexTyped current, FlexTyped previous, String attributes) throws WTException {
    	
    	Boolean isChanged = false;
    	
    	if (previous == null) {
    		isChanged = true;
    		LOGGER.debug("=============DONE with isChanged (no previous flex version), returning " + isChanged + " === ");
    		return isChanged;
    	}
    	
        try {
        	
            List<String> removeAtts = Arrays.asList(attributes.split("\\s*,\\s*"));
            Iterator<String> listIter = removeAtts.iterator();
            
            while (listIter.hasNext()) {
                String attribute = listIter.next();
                if (FormatHelper.hasContent(attribute)) {
                	
                    Object currAttrValue = current.getValue(attribute);
                    LOGGER.debug("=============currAttrValue =========" + currAttrValue);
                    
                    Object attrPrev = null;
                    if (previous != null) {
                        attrPrev = previous.getValue(attribute);
                    }
                    
                    LOGGER.debug("=============attrPrev =========" + attrPrev);
                    
                    if (currAttrValue != null && attrPrev != null) {
                        if (!(attrPrev.toString().compareTo(currAttrValue.toString()) == 0)) {
                            isChanged = true;
                            break;
                        }
                    } else if ((currAttrValue != null && attrPrev == null) || (attrPrev != null && currAttrValue == null)) {
                        //There is a difference
                        isChanged = true;
                        break;

                    }
                }
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            isChanged = false;
        }
        
        LOGGER.debug("isChanged---------------" + isChanged);
        return isChanged;
    }

}
