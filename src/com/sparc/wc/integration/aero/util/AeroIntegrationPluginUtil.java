package com.sparc.wc.integration.aero.util;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COMMON_LOGGER_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_TYPE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_VALID_DEVELOPMENT_SEASONS_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_INTEGRATION_CONTEXT_PREFIX;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_FLEX_TYPE_PATH_KEY;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.Collection;
import com.lcs.wc.db.*;
import com.lcs.wc.flextype.FlexTypeCache;
import java.sql.Timestamp;


import org.apache.logging.log4j.Logger;

import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.integration.aero.domain.AeroCustomPluginException;
import com.sparc.wc.integration.aero.domain.AeroPayloadAttribute;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.lcs.wc.db.SearchResults;
import java.text.SimpleDateFormat;

import wt.log4j.LogR;
import wt.method.MethodContext;
import wt.util.WTException;
/**
 * Provides utility functions intended to be used by Aero related Flex Plugins.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Renamed validateColorwayType to validateAeroColorwayType.<br>
 * - Added methos to handle object tagging as integration api origin: 
 * generateMethodContextKey, tagAsIntegrationApiOrigin, untagAsIntegrationApiOrigin, isTaggedAsIntegrationApiOrigin<br>
 * - Updated getChangedAttributes() to help properly evaluate changes when dealing with bogus empty Multi Entry type attributes.<br>
 * - Task #10387 (Hypercare): Added methods "isColorwayInCostSheet" and "isCostSheetInSeason".<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 */
public class AeroIntegrationPluginUtil {

	private AeroIntegrationPluginUtil() {
		
	}
	
	private static final Logger LOGGER = LogR.getLogger(AERO_COMMON_LOGGER_NAME);
	
	/**
     * Checks whether current flex object is changed when compared to its previous version.<br>
     * Note: Uses default Aero common logger for printing debug messages.
     * @param current The current flex object to check.
     * @param previous The previous flex object to compare against the current one.
     * @param attributes List of flex object attributes to use for the comparison.
     * @return true if the object has changed, false otherwise or if no previous object was provided to compare with.
     * @throws WTException If an error occurs while verifying attribute values from the current and/or previous flex objects.
     */
	public static boolean isFlexObjectChanged(FlexTyped current, FlexTyped previous, List<AeroPayloadAttribute> attributes) throws WTException {
		return isFlexObjectChanged(current, previous, attributes, LOGGER);
	}
	
	/**
     * Checks whether current flex object is changed when compared to its previous version.
     * @param current The current flex object to check.
     * @param previous The previous flex object to compare against the current one.
     * @param attributes List of flex object attributes to use for the comparison.
     * @param logger Use the logger provided
     * @return true if the object has changed, false otherwise or if no previous object was provided to compare with.
     * @throws WTException If an error occurs while verifying attribute values from the current and/or previous flex objects.
     */
    public static boolean isFlexObjectChanged(FlexTyped current, FlexTyped previous, List<AeroPayloadAttribute> attributes, Logger logger) throws WTException {
    	
    	boolean isChanged = false;
    	
    	if (current == null || previous == null || attributes == null || attributes.isEmpty()) {
    		if (logger != null) {
    			LOGGER.debug("[isFlexObjectChanged] Skipping flex object attr change check due to missing arguments.");
    		}
    		return isChanged;
    	}
    	
    	if (logger == null) {
    		logger = LOGGER;
    	}
    	
    	for (AeroPayloadAttribute attr : attributes) {
    		
    		if (!FormatHelper.hasContent(attr.getFlexAttributeName())) {
    			LOGGER.warn("[isFlexObjectChanged] No flex attribute name found for attribute definition: " + attr);
    			continue;
    		}
    		
    		Object currAttrValue = AeroPayloadAttributeUtil.getAttributeValue(current, attr);
    		Object attrPrevValue = AeroPayloadAttributeUtil.getAttributeValue(previous, attr);
    		LOGGER.debug("[isFlexObjectChanged] " + attr.getFlexAttributeName() + ": currAttrValue = " + currAttrValue + " <-> attrPrevValue = " + attrPrevValue);
    		
    		isChanged = SparcIntegrationUtil.divergenceCheck(attrPrevValue, currAttrValue);
    		
    		if (isChanged) {
            	LOGGER.debug("[isFlexObjectChanged] Attribute '" + attr.getFlexAttributeName() + "' is changed.");
            	break;
            }
    		
    	}//end for loop.
    	
        return isChanged;
    }
    
    /**
     * Returns a list of attributes which their values got changed when comparing current vs previous flex object states.
     * @param current The current flex object to check.
     * @param previous The previous flex object to compare against the current one.
     * @param attributes List of flex object attributes to use for the comparison.
     * @param logger Use the logger provided
     * @return The list of attributes which values got changed.
     * @throws WTException If an error occurs while verifying attribute values from the current and/or previous flex objects.
     */
    public static List<AeroPayloadAttribute> getChangedAttributes(FlexTyped current, FlexTyped previous, List<AeroPayloadAttribute> attributes, Logger logger) throws WTException {
    	
    	List<AeroPayloadAttribute> changedAttrs = new ArrayList<>();
    	
    	if (current == null || previous == null || attributes == null || attributes.isEmpty()) {
    		if (logger != null) {
    			LOGGER.debug("[getChangedAttributes] Skipping flex object attr change check due to missing arguments.");
    		}
    		return changedAttrs;
    	}
    	
    	if (logger == null) {
    		logger = LOGGER;
    	}
    	
    	for (AeroPayloadAttribute attr : attributes) {
    		
    		if (!FormatHelper.hasContent(attr.getFlexAttributeName())) {
    			LOGGER.warn("[getChangedAttributes] No flex attribute name found for attribute definition: " + attr);
    			continue;
    		}
    		
    		Object currAttrValue = AeroPayloadAttributeUtil.getAttributeValue(current, attr);
    		Object attrPrevValue = AeroPayloadAttributeUtil.getAttributeValue(previous, attr);
    		
    		LOGGER.debug("[getChangedAttributes] RAW: " + attr.getFlexAttributeName() + ": currAttrValue = " + currAttrValue + " <-> attrPrevValue = " + attrPrevValue);
    		
    		if (currAttrValue != null && currAttrValue.toString().isEmpty()) {
    			currAttrValue = null;
    		}
    		
    		if (attrPrevValue != null && attrPrevValue.toString().isEmpty()) {
    			attrPrevValue = null;
    		}
    		
    		LOGGER.debug("[getChangedAttributes] CURATED: " + attr.getFlexAttributeName() + ": currAttrValue = " + currAttrValue + " <-> attrPrevValue = " + attrPrevValue);
    		
    		if (SparcIntegrationUtil.divergenceCheck(attrPrevValue, currAttrValue)) {
            	LOGGER.debug("[getChangedAttributes] Attribute '" + attr.getFlexAttributeName() + "' is changed.");
            	changedAttrs.add(attr);
            }
    		
    	}//end for loop.
    	
        return changedAttrs;
    }
	//Start - This method is used in aero integration for locked attributes
	public static List<AeroPayloadAttribute> getChangedAttrList(FlexTyped current, FlexTyped previous, List<AeroPayloadAttribute> attributes, Logger logger) throws WTException {

		List<AeroPayloadAttribute> changedAttrs = new ArrayList<>();

		if (current == null || previous == null || attributes == null || attributes.isEmpty()) {
			if (logger != null) {
				LOGGER.debug("[getChangedAttributes] Skipping flex object attr change check due to missing arguments.");
			}
			return changedAttrs;
		}

		if (logger == null) {
			logger = LOGGER;
		}

		for (AeroPayloadAttribute attr : attributes) {

			if (!FormatHelper.hasContent(attr.getFlexAttributeName())) {
				LOGGER.warn("[getChangedAttributes] No flex attribute name found for attribute definition: " + attr);
				continue;
			}

			Object currAttrValue = AeroPayloadAttributeUtil.getAttributeValue(current, attr);
			Object attrPrevValue = AeroPayloadAttributeUtil.getAttributeValue(previous, attr);

			LOGGER.debug("[getChangedAttributes] RAW: " + attr.getFlexAttributeName() + ": currAttrValue = " + currAttrValue + " <-> attrPrevValue = " + attrPrevValue);

			if (currAttrValue != null && currAttrValue.toString().isEmpty()) {
				currAttrValue = null;
			}

			if (attrPrevValue != null && attrPrevValue.toString().isEmpty()) {
				attrPrevValue = null;
			}

			LOGGER.debug("[getChangedAttributes] CURATED: " + attr.getFlexAttributeName() + ": currAttrValue = " + currAttrValue + " <-> attrPrevValue = " + attrPrevValue);

			LOGGER.debug("ChangeCheck attrPrevValue----------------"+attrPrevValue);
			LOGGER.debug("ChangeCheck currAttrValue----------------"+currAttrValue);

			if (SparcIntegrationUtil.changeCheck(attrPrevValue, currAttrValue)) {
				LOGGER.debug("[getChangedAttributes] Attribute '" + attr.getFlexAttributeName() + "' is changed.");
				changedAttrs.add(attr);
			}

		}//end for loop.

		return changedAttrs;
	}
	//End - This method is used in aero integration for locked attributes
    /**
     * Checks whether the given season is a valid Aero season type or not.
     * @param season The season to check.
     * @throws WTException If an error occurs when extracting the season type from the season object.
     * @throws AeroCustomPluginException If the season is not a valid Aero type.
     */
    public static void validateAeroSeasonType(LCSSeason season) throws WTException, AeroCustomPluginException {
    	
    	if (season == null) {
    		throw new AeroCustomPluginException("Cannot validate Aero season type on a missing season.");
    	}
    	
    	String developmentSeason = (String)season.getValue(AERO_SEASON_TYPE_ATTR);
		
		if (!AERO_VALID_DEVELOPMENT_SEASONS_VALUES.contains(developmentSeason)) {
			throw new AeroCustomPluginException("Not a valid Aero Season Type for integration: " + developmentSeason);
		}
    }
    
    /**
     * Checks whether the given product is an Aero product type or not.
     * @param product The product to check.
     * @throws AeroCustomPluginException If the product is not an Aero type.
     */
    public static void validateAeroProductType(LCSProduct product) throws AeroCustomPluginException {
    	
    	if (product == null) {
    		throw new AeroCustomPluginException("Cannot validate Aero product type on a missing product.");
    	}
    	
    	String productType = product.getFlexType().getFullName(true);
		
    	if(!productType.contains(AERO_PRODUCT_FLEX_TYPE_PATH)) {
			throw new AeroCustomPluginException("Not an Aero Product Type: " + productType);
		}
    }
    
    /**
     * Checks whether the given colorway is an Aero colorway type or not.
     * @param colorway The colorway to check.
     * @throws AeroCustomPluginException If the colorway is not an Aero type.
     */
    public static void validateAeroColorwayType(LCSSKU colorway) throws AeroCustomPluginException {
    	
    	if (colorway == null) {
    		throw new AeroCustomPluginException("Cannot validate Aero colorway type on a missing colorway.");
    	}
    	
    	String colorwayType = colorway.getFlexType().getFullName(true);
    	
    	if (!colorwayType.contains(AERO_FLEX_TYPE_PATH_KEY)) {
    		throw new AeroCustomPluginException("Not an Aero Colorway Type: " + colorwayType);
    	}
    }
    
    /**
     * Checks whether the given colorway name is available within the given cost sheet.
     * @param colorwayName The colorway name to check.
     * @param costSheet The cost sheet to check for the colorway name's presence.
     * @return true if the colorway name is found within the given cost sheet, false otherwise.
     */
    public static boolean isColorwayInCostSheet(String colorwayName, LCSProductCostSheet costSheet) {
    	
    	boolean found = false;
    	
    	if (colorwayName == null || costSheet == null) {
    		return found;
    	}
    	
    	String costSheetColorways = costSheet.getApplicableColorNames();
    	
    	if (costSheetColorways == null || costSheetColorways.isEmpty()) {
    		return found;
    	}
    	
    	String[] colorwaysInCostSheetArray = costSheetColorways.split("\\|~\\*~\\|");
    	
    	for (String colorwayCS : colorwaysInCostSheetArray) {
    		
    		found = colorwayName.equalsIgnoreCase(colorwayCS);
    		
    		if (found) {
    			break;
    		}
    		
    	}//end for loop.
    	
    	return found;
    }
    
    /**
     * Checks whether the given cost sheet belongs to the given season name.
     * @param costSheet The cost sheet to check.
     * @param seasonName The season name to check against the cost sheet.
     * @return true if the cost sheet is belongs to the given season, false otherwise.
     * @throws WTException If an error occurs while attempting to access the season details for the given cost sheet.
     */
    public static boolean isCostSheetInSeason(LCSProductCostSheet costSheet, String seasonName) throws WTException {
    	
    	if (costSheet == null || seasonName == null) {
    		return false;
    	}
    	
    	LCSSeason csSeason = (LCSSeason) VersionHelper.latestIterationOf(costSheet.getSeasonMaster());
    	
    	return (csSeason != null && seasonName.equalsIgnoreCase((String)csSeason.getValue("seasonName")));
    }
    
    /**
	 * Generates an unique identifier from the given flex typed object to use when passing/retrieving info thru the MethodContext.
	 * @param flexTypedObj The FlexTyped object to generate a unique context id from.
	 * @return The generated context identifier.
	 */
	public static String generateMethodContextKey(FlexTyped flexTypedObj) {
		
		if (flexTypedObj == null) {
			return null;
		}
		
		return AERO_INTEGRATION_CONTEXT_PREFIX + "-" + flexTypedObj.toString();
	}
	
	/**
	 * Adds an entry into the Flex's MethodContext as reference to the given FlexTyped object, as a way to tag it as being updated from the integration api.
	 * Note: Uses generateMethodContextKey() to create the key identifier for the MethodContext map. 
	 * @param flexTypedObj The FlexTyped object to tag.
	 * @return The string reference representing the FlexTyped object tagged or <code>null</code> if no FlexTyped object was provided.
	 */
	public static String tagAsIntegrationApiOrigin(FlexTyped flexTypedObj) {
		
		String addedObjRef = null;
		
		if (flexTypedObj != null) {
			addedObjRef = flexTypedObj.toString();
			MethodContext.getContext().put(generateMethodContextKey(flexTypedObj), addedObjRef);
		}
		
		return addedObjRef;
	}
	
	/**
	 * Removes the entry reference to the given FlexTyped object from the Flex's MethodContext as a way to untag it as being updated from the integraton api.<br>
	 * Note: Uses generateMethodContextKey() to create the lookup key identifier for the MethodContext map.
	 * @param flexTypedObj The FlexTyped object to untag.
	 * @return The string reference representing the FlexTyped object untagged or <code>null</code> if no FlexTyped object was provided/found.
	 */
	public static String untagAsIntegrationApiOrigin(FlexTyped flexTypedObj) {
		
		String removedObjRef = null;
		
		if (flexTypedObj != null) {
			removedObjRef = (String)MethodContext.getContext().remove(generateMethodContextKey(flexTypedObj));
		}
		
		return removedObjRef;
	}
	
	/**
	 * Checks whether the given FlexTyped object has been tagged as updated from the integration api or not.
	 * @param flexTypedObj The FlexTyped object to check.
	 * @return true if the FlexTyped object has been tagged from the integration api, false otherwise.
	 */
	public static boolean isTaggedAsIntegrationApiOrigin(FlexTyped flexTypedObj) {
		return (flexTypedObj != null && MethodContext.getContext().containsKey(generateMethodContextKey(flexTypedObj)));
	}
	
	 /**
     * 
     * @param costsheet object, CostsheetPricingdate, BusinessObject Type, Country Object.
     */
	public static double populateTariffByCountryPercentage(final LCSProductCostSheet objCostSheet, Date ObjCostDate, FlexType boType,
	final LCSCountry countryObj) {
		double tariffByCountryPercentage = 0;
		try{
			 LCSLifecycleManaged tariffBO = AeroIntegrationPluginUtil.fetchTariffBO(ObjCostDate, countryObj, boType);
			 LOGGER.debug("tariffByCountryPercentage >>>>>>>>before null check"+tariffBO);
			 if(null != tariffBO){
				 LOGGER.debug("tariffByCountryPercentage >>>>>>>>"+tariffBO);
				 LOGGER.debug("tariffByCountryPercentage >>>>>>>>"+tariffBO.getValue("scTariffPerc"));
				 tariffByCountryPercentage = (Double)tariffBO.getValue("scTariffPerc");
				 LOGGER.debug("tariffByCountryPercentage >>>>>>>>"+tariffByCountryPercentage);
			 }
			 
		}catch (Exception e) {
            e.printStackTrace();
		}
		
		return tariffByCountryPercentage;
	}
								


	 public static LCSLifecycleManaged fetchTariffBO(Date ObjCostDate, LCSCountry objCountry, FlexType boType) {
		LCSLifecycleManaged tariffBO = null;
        PreparedQueryStatement query = new PreparedQueryStatement();
		PreparedQueryStatement subQuery = new PreparedQueryStatement();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
			SimpleDateFormat  sdf1 = new SimpleDateFormat("dd-MMM-yy");
			String subQueryDate = "";

        try { 
			String formatted = sdf.format(ObjCostDate);
					
			subQuery.appendFromTable("LCSLIFECYCLEMANAGED");
			QueryColumn startDateColumn = new QueryColumn("LCSLIFECYCLEMANAGED", boType.getAttribute("scStartDate").getColumnName());
			subQuery.appendSelectColumn(startDateColumn, "MAX");
			subQuery.appendCriteria(new Criteria("LCSLifecycleManaged", boType.getAttribute("scStartDate").getColumnName(), formatted, Criteria.LESS_THAN_EQUAL));
			subQuery.appendAndIfNeeded();
			subQuery.appendOpenParen();
			subQuery.appendCriteria(new Criteria("LCSLifecycleManaged", boType.getAttribute("scEndDate").getColumnName(), "", Criteria.IS_NULL));
			subQuery.appendOrIfNeeded();
			subQuery.appendOrIfNeeded();
			subQuery.appendCriteria(new Criteria("LCSLifecycleManaged", boType.getAttribute("scEndDate").getColumnName(),formatted, Criteria.GREATER_THAN_EQUAL));
			subQuery.appendClosedParen();
			subQuery.appendAndIfNeeded();
			subQuery.appendCriteria(new Criteria("LCSLifecycleManaged", boType.getAttribute("scCountry").getColumnName(), FormatHelper.getNumericVersionIdFromObject(objCountry), Criteria.EQUALS));
			
			LOGGER.debug("** subQuery:" + subQuery);
			SearchResults objSubQueryResults = LCSQuery.runDirectQuery(subQuery);
			LOGGER.debug("** objSubQueryResults :" + objSubQueryResults);
			
			if(objSubQueryResults.getResults() != null && objSubQueryResults.getResults().size() > 0){
				LOGGER.debug("** No of  :" + objSubQueryResults.getResults().size());
				for (FlexObject dateObj : (Collection<FlexObject>) objSubQueryResults.getResults()) {
					 LOGGER.debug("** dateObj:" + dateObj);
					 subQueryDate = dateObj.getString("MAX(LCSLIFECYCLEMANAGED.PTC_TMS_2TYPEINFOLCSLIFECYCL)");
					 SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); 
								
						if (subQueryDate != null && !subQueryDate.trim().isEmpty()) {
							LOGGER.debug("Raw subQueryDate: [" + subQueryDate + "]");
							LOGGER.debug("Length: " + subQueryDate.length());

								 Date parsedDate = inputFormat.parse(subQueryDate);
								 subQueryDate = sdf1.format(parsedDate);
								 LOGGER.debug("** subQueryDate:" + subQueryDate);
							} else {
								// Handle missing or invalid date string
								LOGGER.debug("Date string is empty or null");
							}
				}
				
			}
			
			query.appendFromTable("LCSLIFECYCLEMANAGED");
			query.appendSelectColumn("LCSLIFECYCLEMANAGED", "IDA2A2");
			
			query.appendCriteria(new Criteria("LCSLifecycleManaged", boType.getAttribute("scStartDate").getColumnName(), formatted, Criteria.LESS_THAN_EQUAL));
			query.appendAndIfNeeded();
			query.appendOpenParen();
			query.appendCriteria(new Criteria("LCSLifecycleManaged", boType.getAttribute("scEndDate").getColumnName(), "", Criteria.IS_NULL));
			query.appendOrIfNeeded();
			query.appendOrIfNeeded();
			query.appendCriteria(new Criteria("LCSLifecycleManaged", boType.getAttribute("scEndDate").getColumnName(),formatted, Criteria.GREATER_THAN_EQUAL));
			query.appendClosedParen();
			query.appendAndIfNeeded();
			query.appendCriteria(new Criteria("LCSLifecycleManaged", boType.getAttribute("scCountry").getColumnName(), FormatHelper.getNumericVersionIdFromObject(objCountry), Criteria.EQUALS));
			query.appendAndIfNeeded();
			

			query.appendCriteria(new Criteria("LCSLifecycleManaged", boType.getAttribute("scStartDate").getColumnName(),subQueryDate,  Criteria.EQUALS));

			LCSQuery.printStatement(query);
			
			LOGGER.debug("fetchTariffBO query is >>"+query);
			LOGGER.debug("FormatHelper is >>"+FormatHelper.getNumericVersionIdFromObject(objCountry));
			
			SearchResults objResults = LCSQuery.runDirectQuery(query);
			LOGGER.debug("** objResults :" + objResults);
			
			if(objResults.getResults() != null && objResults.getResults().size() > 0){
				LOGGER.debug("** No of  :" + objResults.getResults().size());
				for (FlexObject boObj : (Collection<FlexObject>) objResults.getResults()) {
					LOGGER.debug("** boObj:" + boObj);
					tariffBO =(LCSLifecycleManaged) LCSQuery.findObjectById("OR:com.lcs.wc.foundation.LCSLifecycleManaged:" + boObj.getString("LCSLIFECYCLEMANAGED.IDA2A2"));
				}
				
			}
			return tariffBO;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        LOGGER.debug("tariffBO-----------------------------_tariffBO");
        return tariffBO;
    }
	
								
	public static FlexType getFlexType(String typePath){
        FlexType boType = null;
        try {
             boType = FlexTypeCache.getFlexTypeFromPath(typePath);
        } catch (WTException e) {
            e.printStackTrace();
        }
        return boType;
    }											  
    
}
