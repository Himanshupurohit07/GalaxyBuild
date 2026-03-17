package com.sparc.wc.integration.lucky.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.logging.log4j.Logger;

import com.lcs.wc.color.LCSColor;
import com.lcs.wc.color.LCSColorLogic;
import com.lcs.wc.color.LCSColorQuery;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;

import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_COLOR_SENT_TO_FC_FLAG_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_NEW_COLOR_FLAG_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_COLOR_ID_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_COLOR_DESC_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_COLOR_NO_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_SENT_TO_FC_YES_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_NEW_COLOR_FLAG_UPDATE_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_COLOR_FLEX_TYPE_PATH;

/**
 * Repository for accessing the various data elements from PLM related to Lucky/FC Color integration.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Task #10753: Exclude colors where FC Color ID attribute value is blank. Updated method "findColorIds".<br>
 * - Replaced logic to save colors when updating flags to prevent execution of Server Side Plugins. Updated method "updateColorAttributeValues".<br> 
 * 
 * @author Acnovate
 */
public class FCColorRepository {
	
	private static final Logger LOGGER = LogR.getLogger(FCColorRepository.class.getName());

	private FCColorRepository() {
		
	}
	
	/**
	 * Lookup and retrieve all Color Ids that meet the criteria for Lucky/FC integration.
	 * @return The list of color ids (FC Color ID).
	 * @throws WTException If any error occurs during the data access/lookup at PLM.
	 */
	public static List<String> findColorIds() throws WTException {
		
		List<String> colorIds = new ArrayList<String>();
		
		FlexType luckyColorType = FlexTypeCache.getFlexTypeFromPath(LUCKY_FC_COLOR_FLEX_TYPE_PATH);
		LOGGER.debug("Lucky Colorway Colors Type to search for: " + luckyColorType.getFullName(true));
		
		Set<String> attrCols = new HashSet<String>();
		Map<String, String> criteria = new HashMap<String, String>();
		
		//Setup color attributes to pull from LCSColor.
		attrCols.add(LUCKY_FC_COLOR_ID_ATTR);
		attrCols.add(LUCKY_FC_COLOR_DESC_ATTR);
		attrCols.add(LUCKY_FC_COLOR_NO_ATTR);
		attrCols.add(LUCKY_FC_COLOR_SENT_TO_FC_FLAG_ATTR);
		
		SearchResults sr = new LCSColorQuery().findColorsByCriteria(criteria, luckyColorType, attrCols);
		int colorsCount = sr.getResultsFound();
		
		LOGGER.debug("Lucky Color IDs Query: " + sr.getQueryStatement().getSqlStatement());
		
		if (colorsCount > 0) {
			
			@SuppressWarnings("unchecked")
			Vector<FlexObject> results = sr.getResults();
			
			for (FlexObject colorInfo : results) {
				
				Object fcColorId = colorInfo.get("LCSCOLOR." + luckyColorType.getAttribute(LUCKY_FC_COLOR_ID_ATTR).getColumnName());
				Object colorDesc = colorInfo.get("LCSCOLOR." + luckyColorType.getAttribute(LUCKY_FC_COLOR_DESC_ATTR).getColumnName());
				Object colorNo = colorInfo.get("LCSCOLOR." + luckyColorType.getAttribute(LUCKY_FC_COLOR_NO_ATTR).getColumnName());
				Object sentToFC = colorInfo.get("LCSCOLOR." + luckyColorType.getAttribute(LUCKY_FC_COLOR_SENT_TO_FC_FLAG_ATTR).getColumnName());
				
				if (fcColorId != null && !((String)fcColorId).isBlank()) {
					
					if (sentToFC != null && !"scYes".equalsIgnoreCase((String)sentToFC)) {
						
						colorIds.add((String)fcColorId);
						LOGGER.debug("Found eligible FC Color ID '" + fcColorId + "' for color #" + colorNo + " (" + colorDesc + ").");
						
					} else {
						LOGGER.debug("FC Color ID '" + fcColorId + "' for color #" + colorNo + " (" + colorDesc + ") is not eligible to be sent to FC.");
					}
					
				} else {
					LOGGER.debug("FC Color ID for color #" + colorNo + " (" + colorDesc + ") is missing or blank.");
				}
				
			}
			
		}
		
		LOGGER.info("Found " + colorIds.size() + " eligible FC Color IDs out of " + colorsCount + " Lucky colorway colors retrieved.");
		
        return colorIds;
    }
	
	/**
	 * Retrieves the Lucky FC Colorway Color instance for the given FC Color ID value.
	 * @param fcColorId The FC Color ID (note this is not the same as plm color #).
	 * @return The Color instance or <code>null</code> if no color was found for the given id.
	 * @throws WTException If an error occurs while accessing PLM to query for the Color.
	 */
	public static LCSColor getColor(String fcColorId) throws WTException {
		
		LCSColor flexColor = null;
		PreparedQueryStatement pqs = new PreparedQueryStatement();
		
		final FlexType luckyColorType = FlexTypeCache.getFlexTypeFromPath(LUCKY_FC_COLOR_FLEX_TYPE_PATH);
        final String fcColorIdColName = luckyColorType.getAttribute(LUCKY_FC_COLOR_ID_ATTR).getColumnName();
        
        pqs.appendSelectColumn("LCSCOLOR", "IDA2A2");
        pqs.appendFromTable("LCSCOLOR");
        pqs.appendCriteria(new Criteria("LCSCOLOR", fcColorIdColName, "" + fcColorId, Criteria.EQUALS));
		
        LOGGER.debug("[getColor] Query: " + pqs);
        SearchResults qryResults = LCSQuery.runDirectQuery(pqs);
        LOGGER.info("[getColor] Query results: Found " + qryResults.getResultsFound() + " color records.");
        
        if (qryResults != null) {
        	
        	@SuppressWarnings("unchecked")
			Collection<FlexObject> resultSet = qryResults.getResults();
        	
        	for (FlexObject fo : resultSet) {
        		
        		flexColor = (LCSColor)LCSQuery.findObjectById("OR:com.lcs.wc.color.LCSColor:" + (String)fo.get("LCSCOLOR.IDA2A2"));
        		
        	}
        }
        
        return flexColor;
	}
	
	/**
	 * Updates the FC integration control flags for the given color instance.<br>
	 * Flags to update according the specs are:<br>
	 * - Sent To FC (scColorSentToFC): Set to 'Yes' (scYes).<br>
	 * - New Color Flag (sclNewcolorFlag): Set to 'Update' (scUpdate) if not done already.<br>
	 * @param flexColor The Color associated to the flag to update.
	 * @return true if the flags hasve been updated, false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while processing the flag update at PLM.
	 */
	public static boolean updateColorControlFlags(final LCSColor flexColor) throws WTException, WTPropertyVetoException {
		
		Map<String, Object> flagsMap = new HashMap<String, Object>();
		
		flagsMap.put(LUCKY_FC_COLOR_SENT_TO_FC_FLAG_ATTR, LUCKY_FC_SENT_TO_FC_YES_VALUE);
		flagsMap.put(LUCKY_FC_NEW_COLOR_FLAG_ATTR, LUCKY_FC_NEW_COLOR_FLAG_UPDATE_VALUE);
		
		return updateColorAttributeValues(flexColor, flagsMap);
	}
	
	/**
	 * Updates the values for the given attr_key-values map into the given Color type instance.
	 * @param flexColor The Color instance.
	 * @param values The attr_key-value maps containing the data to update into the Color.
	 * @return true if the values have been updated, false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while processing the flag update at PLM.
	 */
	private static boolean updateColorAttributeValues(final LCSColor flexColor, Map<String, 
			Object> values) throws WTException, WTPropertyVetoException {
		
		boolean skipPlugins = true;
		boolean updated = false;
		
		if (flexColor != null && !values.isEmpty()) {
			
			Set<String> flagKeys = values.keySet();
	        
	        for (String key : flagKeys) {
	        	flexColor.setValue(key, values.get(key));
	        }
	        
	        LCSColorLogic.persist(flexColor, skipPlugins);
	        LOGGER.debug("[updateColorAttributeValues] Color #" + flexColor.getValue(LUCKY_FC_COLOR_NO_ATTR) + 
	        		" (FC Color ID: " + flexColor.getValue(LUCKY_FC_COLOR_ID_ATTR) + ") is updated.");
			
	        updated = true;
			
		}
		
		return updated;
		
	}
	
	/**
	 * Generates a unique identifier from the given color to use when passing info thru the MethodContext.<br>
	 * The identifier is created by combining flex type path with color internal id: <flex type path>-<color id>
	 * @param flexColor The Color to generate a unique context id from.
	 * @return The generated context identifier.
	 */
	public static String getColorContextId(LCSColor flexColor) {
		
		String contextId = "";
		
		if (flexColor == null) {
			return contextId;
		}
		
		return flexColor.getFlexTypeIdPath() + "-" + flexColor.getPersistInfo().getObjectIdentifier().getId();
		
	}

}
