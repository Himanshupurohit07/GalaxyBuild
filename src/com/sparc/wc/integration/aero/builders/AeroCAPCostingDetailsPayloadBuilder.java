package com.sparc.wc.integration.aero.builders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COSTING_LOGGER_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_HTS_CODE_PLACEHOLDER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_NAME_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_SEASON_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_NUMBER_ATTR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.lcs.wc.util.LCSProperties;

import org.apache.logging.log4j.Logger;

import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.sparc.wc.integration.aero.domain.AeroCostingDetailsPayload;
import com.sparc.wc.integration.aero.domain.AeroCostingFlexTypeMaster;
import com.sparc.wc.integration.aero.domain.AeroPayloadAttribute;
import com.sparc.wc.integration.aero.domain.AeroPayloadAttributeDefinitions;
import com.sparc.wc.integration.aero.domain.AeroProcessesParam;
import com.sparc.wc.integration.aero.repository.AeroCostingRepository;
import com.sparc.wc.integration.aero.util.AeroPayloadAttributeUtil;

import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * Builds an Costing Details payload for Aero PLM-to-CAP integration.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Task #9973 (UAT): Reworked call to cost sheet integration flags update to prevent checkout/wrong iteration related errors.<br>
 * - Task #10212 (Hypercare): Updated build() method by adding custom message whenever integration flag updates fails, as well as updated related logging messages.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9646 Aero - Costing Interface"
 */
public class AeroCAPCostingDetailsPayloadBuilder implements AeroCostingDetailsPayloadBuilder {
	
	private static final Logger LOGGER = LogR.getLogger(AERO_COSTING_LOGGER_NAME);
	
	private Long sourcingConfigNumber;
	private AeroCostingDetailsPayload costingPayload;
	private static Map<String,String> costChannelMap = new HashMap<String,String>();
	private static final String CHANNEL_PREFIX = "com.sparc.wc.costchannel.";
	private static final String COST_CHANNEL = "scCostChannel";
	private static final String DESTINATION_KEY = "scDestinationFlexKey";

	static{
		costChannelMap =  LCSProperties.getPropertyEntriesStartWith(CHANNEL_PREFIX);
	}
	public AeroCAPCostingDetailsPayloadBuilder(AeroCostingDetailsPayload costingPayload) {
		this.costingPayload = costingPayload;
	}

	@Override
	public AeroCostingDetailsPayloadBuilder setSourcingConfigNumber(Long sourcingConfigNumber) {
		this.sourcingConfigNumber = sourcingConfigNumber;
		return this;
	}
	
	/**
	 * Builds the Product Cost Sheet related attributes for the payload.
	 * @param costsheetPayload The cost sheet payload container where product cost sheet attributes will be stored into.
	 * @param flexCostSheet The Product Cost Sheet object to extract the attribute values from.
	 * @throws WTException If an error occurs while accessing/retrieving the Cost Sheet attributes values from the flex object.
	 */
	private void buildCostSheetAttributes(Map<String, Object> costsheetPayload, LCSProductCostSheet flexCostSheet) throws WTException {
		
		if (costsheetPayload == null || flexCostSheet == null) {
			return;
		}
		
		for (AeroPayloadAttribute attrDef : AeroPayloadAttributeDefinitions.
				getCAPCostingAttributesDefinitionsForFlexType(AERO_COST_SHEET_ATTR_GROUP_KEY)) {
			LOGGER.debug("[buildCostSheetAttributes] Cost Sheet attrDef to load: " + attrDef);
			AeroPayloadAttributeUtil.loadAttributeValue(flexCostSheet, attrDef, costsheetPayload);
		}
		
	}
	
	/**
	 * Builds the Sourcing Season related attributes for the payload.
	 * @param costsheetPayload The cost sheet payload container where sourcing season attributes will be stored into.
	 * @param flexSourcingSeasonLink The Source To Season object to extract the attribute values from.
	 * @throws WTException If an error occurs while accessing/retrieving the Sourcing Season attributes values from the flex object.
	 */
	private void buildSourcingSeasonAttributes(Map<String, Object> costsheetPayload, LCSSourceToSeasonLink flexSourcingSeasonLink) throws WTException {
		
		if (costsheetPayload == null || flexSourcingSeasonLink == null) {
			return;
		}
		
		for (AeroPayloadAttribute attrDef : AeroPayloadAttributeDefinitions.
				getCAPCostingAttributesDefinitionsForFlexType(AERO_SOURCING_SEASON_ATTR_GROUP_KEY)) {
			LOGGER.debug("[buildSourcingSeasonAttributes] Sourcing Season attrDef to load: " + attrDef);
			if (AERO_HTS_CODE_PLACEHOLDER_ATTR.equalsIgnoreCase(attrDef.getFlexAttributeName())) {
				costsheetPayload.put(attrDef.getJsonAttributeAlias(), AeroCostingRepository.resolveHTSCode1(flexSourcingSeasonLink));
			} else {
				AeroPayloadAttributeUtil.loadAttributeValue(flexSourcingSeasonLink, attrDef, costsheetPayload);
			}
			
		}
		
	}
	
	/**
	 * Builds the Sourcing Config related attributes for the payload.
	 * @param costsheetPayload The cost sheet payload container where sourcing config attributes will be stored into.
	 * @param flexSourcingConfig The Sourcing Config to extract the attribute values from.
	 * @throws WTException If an error occurs while accessing/retrieving the Sourcing Config attributes values from the flex object.
	 */
	private void buildSourcingConfigAttributes(Map<String, Object> costsheetPayload, LCSSourcingConfig flexSourcingConfig) throws WTException {
		
		if (costsheetPayload == null || flexSourcingConfig == null) {
			return;
		}
		
		for (AeroPayloadAttribute attrDef : AeroPayloadAttributeDefinitions.
				getCAPCostingAttributesDefinitionsForFlexType(AERO_SOURCING_CONFIG_ATTR_GROUP_KEY)) {
			LOGGER.debug("[buildSourcingConfigAttributes] Sourcing Config attrDef to load: " + attrDef);
			AeroPayloadAttributeUtil.loadAttributeValue(flexSourcingConfig, attrDef, costsheetPayload);
		}
		
	}
	
	/**
	 * Builds the Colorway related attributes for the payload.
	 * @param costsheetPayload The cost sheet payload container where colorway attributes will be stored into.
	 * @param flexColorway The Colorway to extract the attribute values from.
	 * @throws WTException If an error occurs while accessing/retrieving the Colorway attributes values from the flex object.
	 */
	private void buildColorwayAttributes(Map<String, Object> costsheetPayload, LCSSKU flexColorway) throws WTException {
		
		if (costsheetPayload == null || flexColorway == null) {
			return;
		}
		
		for (AeroPayloadAttribute attrDef : AeroPayloadAttributeDefinitions.getCAPCostingAttributesDefinitionsForFlexType(AERO_COLORWAY_ATTR_GROUP_KEY)) {
			System.out.println("[buildColorwayAttributes] Colorway attrDef to load: " + attrDef);
			AeroPayloadAttributeUtil.loadAttributeValue(flexColorway, attrDef, costsheetPayload);
		}
		
	}
	
	/**
	 * Builds the Season related attributes for the payload.
	 * @param costsheetPayload The cost sheet payload container where season attributes will be stored into.
	 * @param flexSeason The Season to extract the attribute values from.
	 * @throws WTException If an error occurs while accessing/retrieving the Season attributes values from the flex object.
	 */
	private void buildSeasonAttributes(Map<String, Object> costsheetPayload, LCSSeason flexSeason) throws WTException {
		
		if (costsheetPayload == null || flexSeason == null) {
			return;
		}
		
		for (AeroPayloadAttribute attrDef : AeroPayloadAttributeDefinitions.getCAPCostingAttributesDefinitionsForFlexType(AERO_SEASON_ATTR_GROUP_KEY)) {
			LOGGER.debug("[buildSeasonAttributes] Season attrDef to load: " + attrDef);
			AeroPayloadAttributeUtil.loadAttributeValue(flexSeason, attrDef, costsheetPayload);
		}
		
	}
	
	@Override
	public AeroCostingDetailsPayload build() throws WTException, WTPropertyVetoException, Exception {
		
		if (sourcingConfigNumber == null || costingPayload == null) {
			return costingPayload;
		}
		
		List<String> updatedCostSheets = new ArrayList<String>();
		List<AeroCostingFlexTypeMaster> flexCostingMasterList = AeroCostingRepository.findEligibleColorwayCostSheets(
				sourcingConfigNumber, 
				AeroProcessesParam.CAP);
		
		costingPayload.setSourcingConfigNumber(sourcingConfigNumber.toString());
		
		for (AeroCostingFlexTypeMaster costingMaster : flexCostingMasterList) {
			
			if (costingMaster != null) {
				
				Map<String, Object> costsheetPayload = new HashMap<>();
				
				costingPayload.getCostsheets().add(costsheetPayload);
				
				String costSheetNumber = "" + (costingMaster.getProductCostSheet() != null ? costingMaster.getProductCostSheet().getValue(AERO_COST_SHEET_NUMBER_ATTR): "");
				String productNumber = "" + (costingMaster.getProduct() != null ? costingMaster.getProduct().getValue(AERO_PRODUCT_NUMBER_ATTR): "");
				String seasonName = "" + (costingMaster.getSeason() != null ? costingMaster.getSeason().getValue(AERO_SEASON_NAME_ATTR) : "");
				String colorwayNumber = "" + (costingMaster.getColorway() != null ? costingMaster.getColorway().getValue(AERO_COLORWAY_NUMBER_ATTR): "");
				
				LOGGER.debug("About to build Aero CAP Costing payload for cost sheet #" + costSheetNumber +
						", colorway #" + colorwayNumber +
						", sourcing config #" + sourcingConfigNumber + 
						", product #" + productNumber +
						", season '" + seasonName + "'.");
				
				buildColorwayAttributes(costsheetPayload, costingMaster.getColorway());
				buildSeasonAttributes(costsheetPayload, costingMaster.getSeason());
				buildSourcingConfigAttributes(costsheetPayload, costingMaster.getSourcingConfig());
				buildSourcingSeasonAttributes(costsheetPayload, costingMaster.getSourcingSeasonLink());
				buildCostSheetAttributes(costsheetPayload, costingMaster.getProductCostSheet());

				if(costsheetPayload.get(DESTINATION_KEY) != null){
					costsheetPayload.put(COST_CHANNEL,costChannelMap.get(CHANNEL_PREFIX + (String)costsheetPayload.get(DESTINATION_KEY)));
				}
				
				AeroPayloadAttributeUtil.logPayload(costsheetPayload, LOGGER);
				
				if (costingMaster.getProductCostSheet() != null && !updatedCostSheets.contains(costSheetNumber)) {
					
					LOGGER.debug("About to update Aero CAP Costing integration flags for cost sheet #" + costSheetNumber + "(colorway #" + colorwayNumber + ").");
					
					try {
						
						if (AeroCostingRepository.updateIntegrationControlFlags(AeroProcessesParam.CAP, costingMaster.getProductCostSheet())) {
							updatedCostSheets.add(costSheetNumber);
							LOGGER.debug("Aero CAP Costing integration flags for for cost sheet #" + costSheetNumber + "(colorway #" + colorwayNumber + ") have been updated.");
						}
						
					} catch(Exception csEx) {
						throw new Exception("Failed to update cost sheet CAP integration control flags for cost sheet #" + costSheetNumber + " under sourcing config #" + sourcingConfigNumber + " and product #" + productNumber + ".", csEx);
					}
					
				} else {
					LOGGER.debug("Aero CAP Costing integration flags update for for cost sheet #" + costSheetNumber + "(colorway #" + colorwayNumber + "). has been skipped (flags already updated).");
				}
				
			}
			
		}//end for loop.
		
		return costingPayload;
	}

}
