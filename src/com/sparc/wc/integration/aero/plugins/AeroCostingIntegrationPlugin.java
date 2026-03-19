package com.sparc.wc.integration.aero.plugins;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_STATUS_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COSTING_LOGGER_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_SEASON_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_STATUS_CONFIRMED_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_COST_SHEET_TYPE_PRODUCT;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_COUNTRY_OF_ORIGIN_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_PRICING_DATE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_INCOTERMS_ATTR;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Date;

import org.apache.logging.log4j.Logger;

import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSCostSheetLogic;
import com.lcs.wc.sourcing.LCSCostSheetQuery;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.VersionHelper;
import com.lcs.wc.country.LCSCountry;
import com.sparc.wc.integration.aero.domain.AeroCustomPluginException;
import com.sparc.wc.integration.aero.domain.AeroPayloadAttribute;
import com.sparc.wc.integration.aero.domain.AeroPayloadAttributeDefinitions;
import com.sparc.wc.integration.aero.repository.AeroCostingRepository;
import com.sparc.wc.integration.aero.util.AeroIntegrationPluginUtil;
import com.sparc.wc.integration.aero.util.AeroPayloadAttributeUtil;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.sparc.wc.util.SparcCostingConstants;

import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * Flex Plugin for use for Aero Costing Integration to:<br>
 * a. Update (reset) costing integration flags based on integration attributes changes made by users.<br>
 * b. Detect and prevent updates to flex objects if not allowed to. i.e. Check for no blank & locked attributes.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Task #10211 (Hypercare): Added logic to check for assigned colorways changes in cost sheet. New method added "isCostSheetColorwaysChanged".<br>
 * - Task #10387 (Hypercare): Switched method "isCostSheetConfirmed" to public, so to support cost sheet flag reset logic at AeroArticleIntegrationPlugin class.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9646 Aero - Costing Interface"
 */
public class AeroCostingIntegrationPlugin {
	
	private static final Logger LOGGER = LogR.getLogger(AERO_COSTING_LOGGER_NAME);
	
	private static List<AeroPayloadAttribute> SOURCING_CONFIG_ATTR_LIST = AeroPayloadAttributeDefinitions.
			getCostingPluginAttributesDefinitionsForFlexType(AERO_SOURCING_CONFIG_ATTR_GROUP_KEY);
	
	private static List<AeroPayloadAttribute> SOURCE_TO_SEASON_ATTR_LIST = AeroPayloadAttributeDefinitions.
			getCostingPluginAttributesDefinitionsForFlexType(AERO_SOURCING_SEASON_ATTR_GROUP_KEY);
	
	private static List<AeroPayloadAttribute> COST_SHEET_ATTR_LIST = AeroPayloadAttributeDefinitions.
			getCostingPluginAttributesDefinitionsForFlexType(AERO_COST_SHEET_ATTR_GROUP_KEY);
	
	private AeroCostingIntegrationPlugin() {
		
	}
	
	/**
	 * Main entry point for this plugin to trigger whenever an Aero Article related object is updated.<br> .
	 * @param obj The Flex object updated.
	 * @throws WTException If an error occurs while validating changed attributes from PLM or 
	 * while attempting to update the integration flag at the colorway-season link. 
	 */
	public static void validateChanges(WTObject obj) throws LCSException {
		
		LOGGER.info("[validateChanges] ==============STARTED Aero Costing Plugin @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " =========");
        LOGGER.debug("[validateChanges] WTObject type: " + obj.getDisplayIdentity());
		
        try {
        	
        	if (obj instanceof LCSSourcingConfig) {
            	validateSourcingConfig((LCSSourcingConfig)obj);
            } if (obj instanceof LCSSourceToSeasonLink) {
            	validateSourceToSeasonLink((LCSSourceToSeasonLink)obj);
            } else if (obj instanceof LCSProductCostSheet) {
            	validateProductCostSheet((LCSProductCostSheet)obj);
            } else {
            	LOGGER.debug("[validateChanges] Object type " + obj.getDisplayIdentity() + " ignored.");
            }
        	
        } catch(AeroCustomPluginException aeroEx) {
        	LOGGER.warn("[validateChanges] Skip/Exit condition found for Aero Costing Plugin validation: " + aeroEx.getMessage());
        } catch (LCSException lcsEx) {
        	LOGGER.warn("[validateChanges] Aero Costing Plugin validation alert: " + lcsEx.getMessage());
        	throw lcsEx;
        } catch (Exception e) {
        	LOGGER.error("[validateChanges] The Aero Costing Plugin failed to process the flex object.", e);
        }
        
        LOGGER.info("[validateChanges] ==============DONE Aero Costing Plugin @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " =========");
	}
	
	/**
     * Lookout for changes on the selected integration-eligible attribute values for sourcing config and take the following action(s):<br>
	 * - Confirm if this sourcing config is associated with an Aero product.<br>
	 * - Verifies Non-blanks for Sourcing Config if any of its associated cost sheets' status is confirmed.<br>
	 * - Reset all associated cost sheet integration flags if applicable.<br>
     * @param sourcingConfig The sourcing configuration instance.
     * @throws WTPropertyVetoException If an error occurs while resetting the integration flags.
     * @throws WTException If an error occurs while accessing sourcing configuration attributes.
     * @throws AeroCustomPluginException If a validation exit condition is found. i.e. If product Type is not Aero.
     */
    private static void validateSourcingConfig(LCSSourcingConfig sourcingConfig) throws WTPropertyVetoException, WTException, AeroCustomPluginException {
    	
    	if (sourcingConfig == null) {
    		return;
    	}
    	
    	Long sourcingConfigNumber = (Long)sourcingConfig.getValue(AERO_SOURCING_CONFIG_NUMBER_ATTR);
    	LOGGER.debug("[validateSourcingConfig] Sourcing Config # to check is: " + sourcingConfigNumber + " (" + sourcingConfig.getSourcingConfigName() + ").");
    	
    	if (AeroIntegrationPluginUtil.isTaggedAsIntegrationApiOrigin(sourcingConfig)) {
    		throw new AeroCustomPluginException("Sourcing Config #" + sourcingConfigNumber + " has been updated from the Aero Integration API.");
		}
    	
    	LCSProduct product = (LCSProduct)VersionHelper.latestIterationOf(sourcingConfig.getProductMaster());

		//Check if sourcing config is bound to an Aero product.
		AeroIntegrationPluginUtil.validateAeroProductType(product);
		
		LCSSourcingConfig prevSourcingConfig = (LCSSourcingConfig) VersionHelper.predecessorOf(sourcingConfig);
		
		if (prevSourcingConfig == null) {
    		throw new AeroCustomPluginException("No previous sourcing config was found for sourcing config #" + sourcingConfigNumber + ".");
        }
		
		List<AeroPayloadAttribute> changedAttrList = AeroIntegrationPluginUtil
        		.getChangedAttributes(sourcingConfig, prevSourcingConfig, SOURCING_CONFIG_ATTR_LIST, LOGGER);
		
		if (changedAttrList.isEmpty()) {
        	throw new AeroCustomPluginException("No changes found for sourcing config #" + sourcingConfigNumber + ".");
        }
		
		Collection<?> rawCostSheetList = LCSCostSheetQuery.getAllCostSheetsForSourcingConfig(prevSourcingConfig, false);
		
		if (rawCostSheetList == null || rawCostSheetList.isEmpty()) {
			throw new AeroCustomPluginException("No cost sheets found for sourcing config #" + sourcingConfigNumber + ".");
		}
		
		List<LCSProductCostSheet> costSheetList = new ArrayList<>();
		for (Object rawCS : rawCostSheetList) {
			if (rawCS instanceof LCSCostSheet && FLEX_COST_SHEET_TYPE_PRODUCT.equalsIgnoreCase(((LCSCostSheet)rawCS).getCostSheetType())) {
				costSheetList.add((LCSProductCostSheet)rawCS);
			}
		}//end for loop
		
		if (costSheetList == null || costSheetList.isEmpty()) {
			throw new AeroCustomPluginException("No product cost sheets found for sourcing config #" + sourcingConfigNumber + ".");
		}
		
		LOGGER.debug("[validateSourcingConfig] Found " + costSheetList.size() + " cost sheet for sourcing config #" + sourcingConfigNumber);
		
		List<LCSProductCostSheet> validatedCostSheetList = new ArrayList<>();
		
		//Validation of sourcing config against cost sheets.
		for (LCSProductCostSheet prodCS : costSheetList) {
			
			Long costSheetNumber = (Long)prodCS.getValue(AERO_COST_SHEET_NUMBER_ATTR);
			LOGGER.debug("[validateSourcingConfig] Cost Sheet # to check is: " + costSheetNumber + ".");
			
			LCSSeason season = (LCSSeason)VersionHelper.latestIterationOf(prodCS.getSeasonMaster());
	    	
	    	//Check if cost sheet is bound to a valid Aero season.
			try {
				AeroIntegrationPluginUtil.validateAeroSeasonType(season);
			} catch (AeroCustomPluginException cx) {
				LOGGER.debug("[validateSourcingConfig] Skipping cost sheet #" + costSheetNumber + " due to invalid season type.");
				continue;
			}
			
			validateAttrNonBlanks(sourcingConfig, prodCS, SOURCING_CONFIG_ATTR_LIST);
	    	
			validatedCostSheetList.add(prodCS);
		}//end for loop.
		
		//Reset integration flags on validated cost sheets.
		for (LCSProductCostSheet validatedCS : validatedCostSheetList) {
			AeroCostingRepository.resetIntegrationControlFlags(validatedCS);
		}//end for loop.
		
		LOGGER.debug("[validateSourcingConfig] Sourcing Config check is completed for sourcing config #" + sourcingConfigNumber + ".");
    }
	
    /**
     * Lookout for changes on selected integration-eligible attribute values for Source To Season Link object and takes the following action(s):<br>
     * - Confirm if this source to season link object is associated with an Aero product and season.<br>
	 * - Verifies Non-blanks for Sourcing Season link if any of its associated cost sheets' status is confirmed.<br>
	 * - Reset all associated cost sheet integration flags if applicable.<br>
     * @param sslink The Source To Season Link object that has been changed in the system.
     * @throws WTPropertyVetoException If an error occurs while resetting the integration flags.
     * @throws WTException If an error occurs while accessing source to season link attributes.
     * @throws AeroCustomPluginException If a validation exit condition is found. i.e. Source To Season link's product Type is not Aero.
     */
    private static void validateSourceToSeasonLink(LCSSourceToSeasonLink sslink) throws WTPropertyVetoException, WTException, AeroCustomPluginException {
    	
    	if (sslink == null || !sslink.isLatestIteration()) {
    		return;
    	}
    	
    	LOGGER.debug("[validateSourceToSeasonLink] Source To Season Link to check is: " + sslink);
    	
    	if (AeroIntegrationPluginUtil.isTaggedAsIntegrationApiOrigin(sslink)) {
    		throw new AeroCustomPluginException("Source To Season Link " + sslink + " has been updated from the Aero Integration API.");
		}
    	
    	LCSSourcingConfig sourcingConfig = (LCSSourcingConfig) VersionHelper.latestIterationOf(sslink.getSourcingConfigMaster());
    	Long sourcingConfigNumber = (Long)sourcingConfig.getValue(AERO_SOURCING_CONFIG_NUMBER_ATTR);
		LCSProduct product = (LCSProduct)VersionHelper.latestIterationOf(sourcingConfig.getProductMaster());
		
		//Check if source to season link is bound to an Aero product.
		AeroIntegrationPluginUtil.validateAeroProductType(product);
        
		LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(sslink.getSeasonMaster());
		
		//Check if cost sheet is bound to a valid Aero season.
    	AeroIntegrationPluginUtil.validateAeroSeasonType(season);
    	
    	Object preSSlink = VersionHelper.predecessorOf(sslink);
        
        if (preSSlink == null) {
        	throw new AeroCustomPluginException("No previous source to season link found for '" + sslink + "' (Sourcing #" + sourcingConfigNumber +").");
        }
        
        List<AeroPayloadAttribute> changedAttrList = AeroIntegrationPluginUtil
        		.getChangedAttributes(sslink, (LCSSourceToSeasonLink)preSSlink, SOURCE_TO_SEASON_ATTR_LIST, LOGGER);
        
        if (changedAttrList.isEmpty()) {
        	throw new AeroCustomPluginException("No changes found for Source To Season Link '" + sslink + "' (Sourcing #" + sourcingConfigNumber + ").");
        }
        
        Collection<?> rawCostSheetList = LCSCostSheetQuery.getCostSheetsForSourceToSeason(sslink, null);
        
        if (rawCostSheetList == null || rawCostSheetList.isEmpty()) {
			throw new AeroCustomPluginException("No cost sheets found for Source To Season Link '" + sslink + "' (Sourcing #" + sourcingConfigNumber + ").");
		}
        
        List<LCSProductCostSheet> costSheetList = new ArrayList<>();
		for (Object rawCS : rawCostSheetList) {
			if (rawCS instanceof LCSCostSheet && FLEX_COST_SHEET_TYPE_PRODUCT.equalsIgnoreCase(((LCSCostSheet)rawCS).getCostSheetType())) {
				costSheetList.add((LCSProductCostSheet)rawCS);
			}
		}//end for loop
		
		if (costSheetList == null || costSheetList.isEmpty()) {
			throw new AeroCustomPluginException("No product cost sheets found for Source To Season Link '" + sslink + "' (Sourcing #" + sourcingConfigNumber + ").");
		}
		
		LOGGER.debug("[validateSourceToSeasonLink] Found " + costSheetList.size() + " cost sheet for sourcing config #" + sourcingConfigNumber);
        
		List<LCSProductCostSheet> validatedCostSheetList = new ArrayList<>();
		
		//Validation of source to season link against cost sheets.
		for (LCSProductCostSheet prodCS : costSheetList) {
			Long costSheetNumber = (Long)prodCS.getValue(AERO_COST_SHEET_NUMBER_ATTR);
			LOGGER.debug("[validateSourceToSeasonLink] Cost Sheet # to check is: " + costSheetNumber + ".");
			
			validateAttrNonBlanks(sslink, prodCS, SOURCE_TO_SEASON_ATTR_LIST);
	    	
			validatedCostSheetList.add(prodCS);
		}//end for loop.

		//Reset integration flags on validated cost sheets.
		for (LCSProductCostSheet validatedCS : validatedCostSheetList) {
			AeroCostingRepository.resetIntegrationControlFlags(validatedCS);
		}//end for loop.
		
		LOGGER.debug("[validateSourceToSeasonLink] Source To Season Link check is completed for " + sslink + " (Sourcing #" + sourcingConfigNumber + ").");
    }
    
	/**
     * Lookout for changes on the selected integration-eligible attribute values for cost sheet and take the following action(s):<br>
	 * - Confirm this cost sheet is associated with an Aero product.<br>
	 * - Verifies Non-blanks for Cost Sheet, Sourcing Config and Sourcing Season if cost sheet status is confirmed.<br>
	 * - Reset cost sheet integration flags if applicable.<br>
     * @param costSheet The Product Cost Sheet instance.
     * @throws WTPropertyVetoException If an error occurs while resetting the integration flags.
     * @throws WTException If an error occurs while accessing product cost sheet attributes.
     * @throws AeroCustomPluginException If a validation exit condition is found. i.e. If product Type is not Aero.
     */
    private static void validateProductCostSheet(LCSProductCostSheet costSheet) throws WTPropertyVetoException, WTException, AeroCustomPluginException {
    	
    	if (costSheet == null) {
    		return;
    	}
    	
    	Long costSheetNumber = (Long)costSheet.getValue(AERO_COST_SHEET_NUMBER_ATTR);
    	LOGGER.debug("[validateProductCostSheet] Cost Sheet # to check is: " + costSheetNumber + " (" + costSheet.getName() + ").");
    	
    	if (AeroIntegrationPluginUtil.isTaggedAsIntegrationApiOrigin(costSheet)) {
    		throw new AeroCustomPluginException("Cost Sheet #" + costSheetNumber + " has been updated from the Aero Integration API.");
		}
    	
    	if (costSheet.isWhatIf()) {
    		throw new AeroCustomPluginException("Cost sheet #" + costSheetNumber + " is WHATIF.");
    	}
    	
    	LCSSeason season = (LCSSeason)VersionHelper.latestIterationOf(costSheet.getSeasonMaster());
    	
    	//Check if cost sheet is bound to a valid Aero season.
    	AeroIntegrationPluginUtil.validateAeroSeasonType(season);
    	
    	//Reminder: Cost sheets of any type can be created for an Aero product, so we need to ensure the product is of Aero type.
    	LCSProduct product = (LCSProduct)VersionHelper.latestIterationOf(costSheet.getProductMaster());
		
		//Check if cost sheet is bound to an Aero product.
		AeroIntegrationPluginUtil.validateAeroProductType(product);
    	
    	LCSProductCostSheet prevCostSheet = (LCSProductCostSheet)VersionHelper.predecessorOf(costSheet);
    	
		LCSSourcingConfig sourcingConfig = (LCSSourcingConfig)VersionHelper.latestIterationOf(costSheet.getSourcingConfigMaster());
        LCSSourceToSeasonLink sslink = new LCSSourcingConfigQuery().getSourceToSeasonLink(costSheet.getSourcingConfigMaster(), costSheet.getSeasonMaster());
        
        validateAttrNonBlanks(costSheet, COST_SHEET_ATTR_LIST);
        validateAttrNonBlanks(sourcingConfig, costSheet, SOURCING_CONFIG_ATTR_LIST);
        validateAttrNonBlanks(sslink, costSheet, SOURCE_TO_SEASON_ATTR_LIST);
		
    	if (prevCostSheet == null) {
    		throw new AeroCustomPluginException("No previous cost sheet was found for cost sheet #" + costSheetNumber + ".");
        }
    	
    	List<AeroPayloadAttribute> changedAttrList = AeroIntegrationPluginUtil
        		.getChangedAttributes(costSheet, prevCostSheet, COST_SHEET_ATTR_LIST, LOGGER);
        
        if (changedAttrList.isEmpty() && !isCostSheetColorwaysChanged(costSheet.getApplicableColorNames(), prevCostSheet.getApplicableColorNames())) {
        	throw new AeroCustomPluginException("No changes found for cost sheet #" + costSheetNumber + ".");
        }
    	
        boolean skipPersist= true;
        AeroCostingRepository.resetIntegrationControlFlags(costSheet, skipPersist);
        
        LOGGER.debug("[validateProductCostSheet] Cost sheet check is completed for cost sheet #" + costSheetNumber + ".");
    }
    
    /**
     * Conditionally checks for non-blanks attribute values for the given Product Cost Sheet object if the cost sheet status is 'Confirmed', 
     * and if the list of attributes given contain any attributes that require non-blank check.
     * @param costSheet The cost sheet object to check for non-blank attributes and to inspect its confirmed status.
     * @param attrList The list of attributes to inspect. Only those attributes marked for non-blank will be checked.
     * @throws LCSException If a mandatory non-blank value is found to be blank. 
     * @throws WTException If an error occurred while extracting attribute values from the flex objects given.
     */
    private static void validateAttrNonBlanks(LCSProductCostSheet costSheet,
    		List<AeroPayloadAttribute> attrList) throws LCSException, WTException {
    	
    	if (costSheet == null || attrList == null || attrList.isEmpty()) {
    		LOGGER.debug("[validateAttrNonBlanks] Skipping non blank validation check.");
    		return;
    	}
    	
    	if (isCostSheetConfirmed(costSheet) && costSheet.getApplicableColorNames() != null) {
    		LOGGER.debug("[validateAttrNonBlanks] No-blanks validation is required for this cost sheet.");
    		AeroPayloadAttributeUtil.validateNoBlanks(costSheet, attrList);
    	}
    	
    }
    
    /**
     * Conditionally checks for non-blanks attribute values for the given flex typed object if the cost sheet status is 'Confirmed', 
     * and if the list of attributes given contain any attributes that require non-blank check.
     * @param flexTyped The flex typed object to check for non-blank attributes.
     * @param costSheet The cost sheet object to check for its confirmed status.
     * @param attrList The list of attributes to inspect. Only those attributes marked for non-blank will be checked.
     * @throws LCSException If a mandatory non-blank value is found to be blank. 
     * @throws WTException If an error occurred while extracting attribute values from the flex objects given.
     */
    private static void validateAttrNonBlanks(FlexTyped flexTyped, 
    		LCSProductCostSheet costSheet,
    		List<AeroPayloadAttribute> attrList) throws LCSException, WTException {
    	
    	if (flexTyped == null || costSheet == null || attrList == null || attrList.isEmpty()) {
    		LOGGER.debug("[validateAttrNonBlanks] Skipping non blank validation check.");
    		return;
    	}
    	
    	if (isCostSheetConfirmed(costSheet) && costSheet.getApplicableColorNames() != null) {
    		LOGGER.debug("[validateAttrNonBlanks] No-blanks validation is required for this flex object.");
    		AeroPayloadAttributeUtil.validateNoBlanks(flexTyped, attrList);
    	}
    	
    }
    
    /**
     * Checks whether the cost sheet status attribute for the given cost sheet is set as 'confirmed'.
     * @param costSheet The cost sheet to check.
     * @return true if the cost sheet status is 'confirmed', false otherwise.
     * @throws WTException If an error occurred while extracting the cost sheet status value from the cost sheet object. 
     */
    public static boolean isCostSheetConfirmed(LCSProductCostSheet costSheet) throws WTException {
    	
    	if (costSheet == null || AERO_COST_SHEET_STATUS_CONFIRMED_VALUES.isEmpty()) {
    		return false;
    	}
    	
    	String costSheetStatus = (String)costSheet.getValue(AERO_COST_SHEET_STATUS_ATTR);
    	
    	return (costSheetStatus != null && AERO_COST_SHEET_STATUS_CONFIRMED_VALUES.contains(costSheetStatus));
    }
    
    /**
     * Checks whether the current and previous colorways selected for the cost sheet have changed or not.
     * @param currColorwayNames The current cost sheet colorways.
     * @param prevColorwayNames The previous cost sheet colorways.
     * @return trus if the cost sheet colorways assigned have changed or false otherwise.
     */
    private static boolean isCostSheetColorwaysChanged(String currColorwayNames, String prevColorwayNames) {
    	
    	if (currColorwayNames != null && currColorwayNames.toString().isEmpty()) {
    		currColorwayNames = null;
		}
		
		if (prevColorwayNames != null && prevColorwayNames.toString().isEmpty()) {
			prevColorwayNames = null;
		}
		
		LOGGER.debug("[isCostSheetColorwayChanged] currAttrValue = " + currColorwayNames + " <-> attrPrevValue = " + prevColorwayNames);
		
    	return SparcIntegrationUtil.divergenceCheck(prevColorwayNames, currColorwayNames);    	
    }

	/**
	 * Main entry point for this plugin to set the Tariff based on the Country Of Origin and CostSheet PricingDate.<br> .
	 * @param obj The Flex object updated.
	 * @throws WTException If an error occurs while setting attribute from PLM or
	 */
	public static void setTariff(WTObject obj) throws LCSException {
		if (!(obj instanceof LCSProductCostSheet)) {
			return;
		}
		LOGGER.debug("setTariff :" + obj);
		LCSProductCostSheet objCostSheet = null;
		LCSCountry objCountry = null;
		String countryName;
		double TariffByCountrypercentage = 0.00;
		FlexType boType = null;
		Date objCostDate = null;
		
		
        try {
            	objCostSheet = (LCSProductCostSheet) obj;
				LOGGER.debug("scCountryofOrigin >>>>>>"+objCostSheet.getValue(AERO_COST_SHEET_COUNTRY_OF_ORIGIN_ATTR));
				LOGGER.debug("scCotSheetPricingDate >>>>>>"+objCostSheet.getValue(AERO_COST_SHEET_PRICING_DATE_ATTR));
				System.out.println("scIncoterms >>>>>>"+objCostSheet.getValue(AERO_COST_SHEET_INCOTERMS_ATTR));
				
				if(null != objCostSheet && null != objCostSheet.getValue(AERO_COST_SHEET_COUNTRY_OF_ORIGIN_ATTR)){
					objCountry = (LCSCountry)objCostSheet.getValue(AERO_COST_SHEET_COUNTRY_OF_ORIGIN_ATTR);
					countryName = objCountry.getName();
					System.out.println("countryName >>>>>>"+countryName);
				}else{
					System.out.println("CountryofOrigin is Null at costsheet.");
					objCostSheet.setValue("scTariffByCountry",TariffByCountrypercentage);
					return;
				}
				
				if(null != countryName && null != objCostSheet.getValue(AERO_COST_SHEET_PRICING_DATE_ATTR)){
					if(null != objCostSheet.getValue(AERO_COST_SHEET_INCOTERMS_ATTR) && ("FOB".equalsIgnoreCase(objCostSheet.getValue(AERO_COST_SHEET_INCOTERMS_ATTR).toString()) || "scFca".equalsIgnoreCase(objCostSheet.getValue(AERO_COST_SHEET_INCOTERMS_ATTR).toString()))){
						boType = AeroIntegrationPluginUtil.getFlexType(SparcCostingConstants.TARIFF_BY_COUNTRY);
						objCostDate = (Date) objCostSheet.getValue(AERO_COST_SHEET_PRICING_DATE_ATTR);
						TariffByCountrypercentage = AeroIntegrationPluginUtil.populateTariffByCountryPercentage(objCostSheet, objCostDate, boType, objCountry);
						
						objCostSheet.setValue("scTariffByCountry",TariffByCountrypercentage);
					}else{
						objCostSheet.setValue("scTariffByCountry",TariffByCountrypercentage);
				}
 
        } 
		}catch (Exception e) {
        	LOGGER.error("The Aero Costing Plugin for Tariff failed to process the flex object.", e);
        }
        
        }
		
}
