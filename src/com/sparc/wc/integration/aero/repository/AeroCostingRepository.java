package com.sparc.wc.integration.aero.repository;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COSTING_LOGGER_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_HTS_ASSIGNMENT_TABLE_CODE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_HTS_CODE_1_DEFAULT;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_HTS_CODE_1_WEIGHT_THRESHOLD_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SENT_NO_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SENT_YES_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_SENT_TO_S4_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_HTS_CODE_1_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_HTS_CODE_1_WEIGHT_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_COST_SENT_TO_CAP_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_COST_SENT_TO_S4_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COSTING_COST_SHEET_MILESTONE_FINAL_VALUES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSCostSheetLogic;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSKUSourcingLink;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.integration.aero.builders.AeroCAPCostingDetailsQueryBuilder;
import com.sparc.wc.integration.aero.builders.AeroCAPCostingIdsQueryBuilder;
import com.sparc.wc.integration.aero.builders.AeroS4CostingDetailsQueryBuilder;
import com.sparc.wc.integration.aero.builders.AeroS4CostingIdsQueryBuilder;
import com.sparc.wc.integration.aero.domain.AeroCostingCostSheetKey;
import com.sparc.wc.integration.aero.domain.AeroCostingCostSheetData;
import com.sparc.wc.integration.aero.domain.AeroCostingFlexTypeMaster;
import com.sparc.wc.integration.aero.domain.AeroProcessesParam;

import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * Repository for accessing the various data elements from PLM related to Aero/Costing for CAP integration.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Fixed use of source to season client model at updateSourcingToSeasonLink().<br>
 * - Task #9973 (UAT): Reworked cost sheet & Source To Season Link save action to prevent checkout/wrong iteration related errors.<br>
 * - Task #10160 (Hypercare): Updated HTS code value resolution logic per new specs.<br>
 * - Task #10212 (Hypercare): Replaced use of source to season client model object to LCSLogic.persist for saving the source to season object and skipping conflicting plugin.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9646 Aero - Costing Interface"
 */
public class AeroCostingRepository {
	
	private static final Logger LOGGER = LogR.getLogger(AERO_COSTING_LOGGER_NAME);
	
	/**
	 * Lookup and retrieve from PLM all Sourcing Configuration Numbers that contain cost sheets 
	 * which meet the criteria for Lucky Costing integration for FC & CAP.
	 * @param process The identifier for the lucky target system, i.e. FC or CAP.
	 * @return The list of sourcing configuration numbers.
	 * @throws WTException If a flex api error occurs while retrieving/accessing the data from PLM.
	 */
	public static Set<String> findCostingSourcingConfigNumbers(AeroProcessesParam process) throws WTException {
		
		SearchResults qryResults = null; 
		Set<String> sourceConfigIds = new HashSet<String>();
		
		PreparedQueryStatement pqs = null;
		
		if (process == AeroProcessesParam.CAP) {
			pqs = new AeroCAPCostingIdsQueryBuilder().build(); 
		}  else if (process == AeroProcessesParam.S4) {
			pqs = new AeroS4CostingIdsQueryBuilder().build();
		}
		
		LOGGER.debug("[findCostingSourcingConfigNumbers for " + process + "] Query: " + pqs);
        qryResults = LCSQuery.runDirectQuery(pqs);
        LOGGER.debug("[findCostingSourcingConfigNumbers for " + process + "] Query results: Found " + qryResults.getResultsFound() + " records.");
		
        if (qryResults != null) {
        	
        	final FlexType srcConfigFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_SOURCING_CONFIG_FLEX_TYPE_PATH);
            final String srcConfigNumColName = srcConfigFlexType.getAttribute(AERO_SOURCING_CONFIG_NUMBER_ATTR).getColumnName();
        	
        	@SuppressWarnings("unchecked")
			Collection<FlexObject> resultSet = qryResults.getResults();
        	for (FlexObject fo : resultSet) {
        		
        		String srcConfigNum = (String)fo.get(LCSTables.LCSSOURCINGCONFIG.name() + "." + srcConfigNumColName);
                
                try{
					Long.parseLong(srcConfigNum);
				}catch(Exception ex){
					LOGGER.warn("Skipping bad Sourcing Configuration # found '" + srcConfigNum + "'.");
					continue;
				}
                
                LOGGER.debug("Sourcing Configuration #" + srcConfigNum + " contain eligible cost sheet(s) for Aero Costing " + process + " Integration.");
                
                sourceConfigIds.add(srcConfigNum);
                
        	}
        	
        }
        
		return sourceConfigIds;
	}
	
	/**
	 * Search for integration-eligible season-product-sourcing-colorway cost sheets.<br>
	 * To fulfill the requirements and determine the right cost sheet for the season-product-sourcing-colorway, this method will perform the following:<br>
	 * - Query PLM for eligible product cost sheets linked to any active Lucky season-sourcing-colorway.<br>
	 * - As part of the query design, any Aero seasonal products that don't have any sourcing-colorways will be ignored/excluded.<br>
	 * - Similarly, any product-sourcing-colorways which are inactive will be ignored by the query.<br>
	 * - Eligible (candidate) cost sheets retrieved by the query are those that meet the respective requirements defined in the integration document.<br>
	 * - To find the integration-eligible cost sheet for a given season-product-sourcing-colorway when multiple cost sheets are found for a colorway, 
	 * those (candidate) cost sheets retrieved earlier by the query will be further filtered according the requirements defined in the integration document.
	 * 
	 * @param sourcingConfigNumber The seasonal product's Sourcing Config Number linked to the cost sheets and colorways.
	 * @param process The identifier for the Aero target system, i.e. S4 or CAP.
	 * @return The list of flex objects contained within a master container where each represent an eligible cost sheet.
	 * @throws WTException If a flex api error occurs while retrieving/accessing the data from PLM.
	 */
	public static List<AeroCostingFlexTypeMaster> findEligibleColorwayCostSheets(Long sourcingConfigNumber, 
			AeroProcessesParam process) throws WTException {
		
		SearchResults qryResults = null; 
		PreparedQueryStatement pqs = null;
		
		if (process == AeroProcessesParam.CAP) {
			pqs = new AeroCAPCostingDetailsQueryBuilder(sourcingConfigNumber).build(); 
		}  else if (process == AeroProcessesParam.S4) {
			pqs = new AeroS4CostingDetailsQueryBuilder(sourcingConfigNumber).build();
		}
		
		List<AeroCostingFlexTypeMaster> colorwayCostSheetsList = new ArrayList<AeroCostingFlexTypeMaster>();
		
		LOGGER.debug("[findEligibleColorwayCostSheets for Sourcing Config Number " + sourcingConfigNumber + " (" + process + ")] "
				+ "Query: " + pqs);
        qryResults = LCSQuery.runDirectQuery(pqs);
        LOGGER.debug("[findEligibleColorwayCostSheets for Sourcing Config Number " + sourcingConfigNumber + " (" + process + ")] "
        		+ "Query results: Found " + qryResults.getResultsFound() + " records.");
		
        if (qryResults != null && qryResults.getResultsFound() > 0) {
        	
        	//This map helps model the data resulting from running the query into the relationship of: colorway<->costsheet(s), 
        	//where one colorway (the map key) is linked to one or more cost sheets (the map values).
        	//Its purpose is to make it easier to later isolate the one cost sheet that is eligible for integration for the colorway.
        	//It is important to note that the "colorway" being referred to as the key for this map is a composition of 
        	//multiple attributes (see ColorwayCostSheetKey), which they all adhere to item #5 of the lucky costing requirements document.
        	Map<AeroCostingCostSheetKey, Set<AeroCostingCostSheetData>> costSheetMap = 
        			new HashMap<AeroCostingCostSheetKey, Set<AeroCostingCostSheetData>>();
        	
        	@SuppressWarnings("unchecked")
			Collection<FlexObject> resultSet = qryResults.getResults();
        	
        	int debugCount = 1;
        	
        	for (FlexObject fo : resultSet) {
        		
        		AeroCostingCostSheetData costSheetData = AeroCostingCostSheetData.newBuilder().setRawDataMap(fo).build();
    			
    			LOGGER.debug("costSheetResult [" + debugCount + "] for " + sourcingConfigNumber + " (" + process + ") is: " + costSheetData);
    			debugCount += 1;
    			
    			Set<AeroCostingCostSheetData> colorwayCostSheetsSet = costSheetMap.get(costSheetData.getCostSheetKey());
    			
    			if (colorwayCostSheetsSet != null) {
    				colorwayCostSheetsSet.add(costSheetData);
    			} else {
    				colorwayCostSheetsSet = new HashSet<AeroCostingCostSheetData>();
    				colorwayCostSheetsSet.add(costSheetData);
    				costSheetMap.put(costSheetData.getCostSheetKey(), colorwayCostSheetsSet);
    			}
        		
        	}//end for loop.
        	
        	LOGGER.info("Found " + costSheetMap + " colorways for " + sourcingConfigNumber + " (" + process + ").");
        	
        	for (AeroCostingCostSheetKey csKey : costSheetMap.keySet()) {
        		
        		AeroCostingCostSheetData costSheet = null;
        		Set<AeroCostingCostSheetData> csSet = costSheetMap.get(csKey);
        		
        		if (csSet.isEmpty()) {
        			//This scenario shouldn't ever occur in a normal execution where any aero season-product-sourcing that 
        			//doesn't have active colorways and/or eligible cost sheets will get excluded by the PLM query.
        			LOGGER.warn("Colorway #" + csKey.getColorwayNumber() 
        			+ " (" + csKey.getColorwayName() + ")"
    				+ " for Sourcing Config #" + sourcingConfigNumber 
    				+ " (" + process + ") "
    				+ " doesn't have any cost sheet that may be eligible for integration.");
        			
        			continue;
        		}
        		
        		LOGGER.info("Colorway #" + csKey.getColorwayNumber() 
        		        + " (" + csKey.getColorwayName() + ")"
        				+ " for Sourcing Config #" + sourcingConfigNumber 
        				+ " (" + process + ") "
        				+ " has " + csSet.size() + " cost sheet(s) that may be eligible for integration.");
        		
        		costSheet = lookupLatestEligibleCostSheet(csSet);
        		
        		LOGGER.info("Eligible cost sheet for integration found for Colorway #"
        				+ csKey.getColorwayNumber() + " (" + csKey.getColorwayName() + "): " + costSheet);
        		
        		AeroCostingFlexTypeMaster csMaster = loadColorwayCostSheetMaster(costSheet);
        		LOGGER.debug("Colorway Cost Sheet Master loaded for Colorway #" + csKey.getColorwayNumber() 
        				+ " (" + csKey.getColorwayName() + "): " + csMaster);
        		
        		colorwayCostSheetsList.add(csMaster);
        		
        	}//end for loop.
        	
        }
        
        return colorwayCostSheetsList;        
	}
	
	/**
	 * Identifies the appropriate value for HTS Code 1 from the given Flex object.
	 * @param flexTypedObj The Flex object containing the required attributes to identify the appropriate HTS Code 1 value.
	 * @return The HTS Code 1 value identified or <code>null</code> if no Sourcing Season Link object was provided or if a number parsing error occurs.
	 * @throws WTException If a flex api error occurs while retrieving/accessing the data from the Flex object at PLM.
	 */
	public static String resolveHTSCode1(FlexTyped flexTypedObj) throws WTException {
		
		String htsCode1 = null;
		
		if (flexTypedObj == null) {
			return htsCode1;
		}
		
		try {
			
			String htsCode1Name = null;
			Double htsCode1WeightPercentThreshold = Double.parseDouble(AERO_HTS_CODE_1_WEIGHT_THRESHOLD_VALUE);
			Double htsCode1WeightPercent = (Double)flexTypedObj.getValue(AERO_SOURCING_HTS_CODE_1_WEIGHT_ATTR);
			
			if (htsCode1WeightPercentThreshold.equals(htsCode1WeightPercent)) {
				LCSLifecycleManaged htsAssignmentTableObj = (LCSLifecycleManaged)flexTypedObj.getValue(AERO_SOURCING_HTS_CODE_1_ATTR);
				
				if (htsAssignmentTableObj != null) {
					htsCode1Name = (String)htsAssignmentTableObj.getValue(AERO_HTS_ASSIGNMENT_TABLE_CODE_ATTR);
					htsCode1 = htsCode1Name;
				}
				
			} else if (htsCode1WeightPercent != null && htsCode1WeightPercent.doubleValue() != 0d) {
				htsCode1 = AERO_HTS_CODE_1_DEFAULT;
			}
			
			LOGGER.debug("[resolveHTSCode1] HTS Code 1 Weight %: " + htsCode1WeightPercent
					+ ", HTS Code 1 Weight % threshold: " + htsCode1WeightPercentThreshold
					+ ", HTS Code 1 (From HTS Assignment Table Name): " + htsCode1Name
					+ ", HTS Code 1 (Default value): " + AERO_HTS_CODE_1_DEFAULT
					+ ", HTS Code 1 is: " + htsCode1);
			
		} catch(WTException wtx) {
			throw wtx;
	    } catch(Exception e) {
			LOGGER.error("[resolveHTSCode1] Unable to resolve value for HTS Code 1 attribute: " + e.getMessage(), e);
		}
		
		return htsCode1;
	}
	
	/**
	 * Searches the given Set of colorway cost sheet candidates for the latest eligible one.<br>
	 * This search is part of item #5 of the integration document: If the season-sourcing-colorway is linked to multiple candidate cost sheets,
	 * then a. find the one with Milestone status as Final and b. If multiple cost sheets with milestone status as Final are found, then select
	 * the one that was last saved (last modified timestamp).<br>
	 * Note that if the given Set constains just one cost sheet, then that will be considreed the latest eligible.
	 * @param colorwayCostSheetsSet The set of colorway cost sheets assumed to be related to a single season-sourcing-colorway.
	 * @return The latest eligible colorway cost sheet identified or <code>null</code> if the cost sheet set given is empty or not provided.
	 */
	private static AeroCostingCostSheetData lookupLatestEligibleCostSheet(Set<AeroCostingCostSheetData> colorwayCostSheetsSet) {
		
		AeroCostingCostSheetData costSheet = null;
		
		if (colorwayCostSheetsSet == null || colorwayCostSheetsSet.isEmpty()) {
			return costSheet;
		}
		
		AeroCostingCostSheetData latestModifiedCostSheet = null;
		AeroCostingCostSheetData latestModifiedFinalCostSheet = null;
		
		//Lookup cost sheets with Milestone "Final" and get the latest saved cost sheet.
		for (AeroCostingCostSheetData cs : colorwayCostSheetsSet) {
			
			if (latestModifiedCostSheet == null) {
				latestModifiedCostSheet = cs;
			} else if (cs.getCostSheetModifiedDate() != null &&
					cs.getCostSheetModifiedDate().compareTo(latestModifiedCostSheet.getCostSheetModifiedDate()) > 0) {
				latestModifiedCostSheet = cs;
			}
			
			if (AERO_COSTING_COST_SHEET_MILESTONE_FINAL_VALUES.contains(cs.getCostSheetMilestone())) {
				
				LOGGER.debug("Cost Sheet with Milestone Final found: " + cs.getCostSheetNumber() + " (" + cs.getCostSheetName() + ").");
				
				if (latestModifiedFinalCostSheet == null) {
					latestModifiedFinalCostSheet = cs;
				} else if (cs.getCostSheetModifiedDate() != null &&
						cs.getCostSheetModifiedDate().compareTo(latestModifiedFinalCostSheet.getCostSheetModifiedDate()) > 0) {
					latestModifiedFinalCostSheet = cs;
				}
				
			}
			
		}
		
		//Prioritize any latest saved cost sheet with Final milestone status should one exist.
		if (latestModifiedFinalCostSheet != null) {
			costSheet = latestModifiedFinalCostSheet;
		} else {
			LOGGER.debug("There are no Cost Sheets with Milestone Final for "
					+ "Colorway #" + latestModifiedCostSheet.getCostSheetKey().getColorwayNumber() 
					+ " (" + latestModifiedCostSheet.getCostSheetKey().getColorwayName() + ").");
			costSheet = latestModifiedCostSheet;
		}
		costSheet = latestModifiedCostSheet;
		
		return costSheet;
	}
	
	/**
	 * Creates a AeroColorwayCostSheetFlexTypeMaster instance (container for relevant flex objects associated toa colorway cost sheet) by loading it with 
	 * the respective flex objects from PLM using the flex object internal identifiers extracted from PLM.
	 * @param colorwayCostSheetData The colorway cost sheet data containing the required flex object internal identifiers.
	 * @return The Costing Colorway Cost Sheet Master instance with all available Flex Objects loaded where applicable.
	 */
	private static AeroCostingFlexTypeMaster loadColorwayCostSheetMaster(AeroCostingCostSheetData colorwayCostSheetData) throws WTException {
		
		AeroCostingFlexTypeMaster costSheetMaster = null;
		
		if (colorwayCostSheetData == null) {
			return costSheetMaster;
		}
		
		Long sourcingConfigFlexId = colorwayCostSheetData.getCostSheetKey().getSourcingConfigId();
		Long productFlexId = colorwayCostSheetData.getProductId();
		Long seasonFlexId = colorwayCostSheetData.getCostSheetKey().getSeasonId();
		Long colorwayFlexId = colorwayCostSheetData.getCostSheetKey().getColorwayId();
		Long prodCostSheetFlexId = colorwayCostSheetData.getCostSheetId();
		Long sourcingSeasonLinkFlexId = colorwayCostSheetData.getSourcingSeasonLinkId();
		Long sourcingColorwayLinkFlexId = colorwayCostSheetData.getSourcingColorwayLinkId();
		Long colorwaySeasonLinkFlexId = colorwayCostSheetData.getColorwaySeasonLinkId();
		
		costSheetMaster = new AeroCostingFlexTypeMaster();
		
		if (sourcingConfigFlexId != null) {
			costSheetMaster.setSourcingConfig(
					(LCSSourcingConfig)LCSQuery.findObjectById(LCSObjectReferences.LCSSOURCINGCONFIG.getObjRefString() + sourcingConfigFlexId));
		}
		
		if (productFlexId != null) {
			costSheetMaster.setProduct(
					(LCSProduct)LCSQuery.findObjectById(LCSObjectReferences.LCSPRODUCT.getObjRefString() + productFlexId));
		}
		
		if (seasonFlexId != null) {
			costSheetMaster.setSeason((LCSSeason)LCSQuery.findObjectById(LCSObjectReferences.LCSSEASON.getObjRefString() + seasonFlexId));
		}
		
		if (prodCostSheetFlexId != null) {
			costSheetMaster.setProductCostSheet(
					(LCSProductCostSheet)LCSQuery.findObjectById(LCSObjectReferences.LCSPRODUCTCOSTSHEET.getObjRefString() + prodCostSheetFlexId));
		}
		
		if (colorwayFlexId != null) {
			costSheetMaster.setColorway((LCSSKU)LCSQuery.findObjectById(LCSObjectReferences.LCSSKU.getObjRefString() + colorwayFlexId));
		}
		
		if (sourcingSeasonLinkFlexId != null) {
			costSheetMaster.setSourcingSeasonLink(
					(LCSSourceToSeasonLink)LCSQuery.findObjectById(LCSObjectReferences.LCSSOURCETOSEASONLINK.getObjRefString() + sourcingSeasonLinkFlexId));
		}
		
		if (sourcingColorwayLinkFlexId != null) {
			costSheetMaster.setSourcingColorwayLink(
					(LCSSKUSourcingLink)LCSQuery.findObjectById(LCSObjectReferences.LCSSKUSOURCINGLINK.getObjRefString() + sourcingColorwayLinkFlexId));
		}
		
		if (colorwaySeasonLinkFlexId != null) {
			costSheetMaster.setColorwaySeasonLink(
					(LCSSKUSeasonLink)LCSQuery.findObjectById(LCSObjectReferences.LCSSKUSEASONLINK.getObjRefString() + colorwaySeasonLinkFlexId));
		}
		
		return costSheetMaster;
	}
	
	/**
	 * Resets the Aero Article integration control flags for the given Prouct Cost Sheet object unless they are already reset.<br>
	 * Flags to update for the given Colorway-Season Link are:<br>
	 * - Cost Sent To S4: Sets 'scNo'.<br>
	 * - Cost Sent To CAP: Sets 'scNo'.<br>
	 * @param flexCostSheet The Product Cost Sheet where the flags to be updated are located.
	 * @return true if the flags have been reset (updated/saved), false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while attempting to update an invalid attribute within the flex object.
	 */
	public static boolean resetIntegrationControlFlags(LCSProductCostSheet flexCostSheet) throws WTPropertyVetoException, WTException {
		return resetIntegrationControlFlags(flexCostSheet, false);
	}
	
	/**
	 * Resets the Aero Article integration control flags for the given Prouct Cost Sheet object unless they are already reset.<br>
	 * Flags to update for the given Colorway-Season Link are:<br>
	 * - Cost Sent To S4: Sets 'scNo'.<br>
	 * - Cost Sent To CAP: Sets 'scNo'.<br>
	 * @param flexCostSheet The Product Cost Sheet where the flags to be updated are located.
	 * @param skipPersist Skips executing a persist on the flex object.
	 * @return true if the flags have been reset (updated/saved), false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while attempting to update an invalid attribute within the flex object.
	 */
	public static boolean resetIntegrationControlFlags(LCSProductCostSheet flexCostSheet, boolean skipPersist) throws WTPropertyVetoException, WTException {
		
		boolean updated = false;
		
		if (flexCostSheet == null) {
			return updated;
		}
		
		Map<String, Object> costSheetFlags = new HashMap<String, Object>();
		
		if (AERO_SENT_YES_VALUE.equalsIgnoreCase((String)flexCostSheet.getValue(AERO_COST_SHEET_COST_SENT_TO_CAP_ATTR))) {
			costSheetFlags.put(AERO_COST_SHEET_COST_SENT_TO_CAP_ATTR, AERO_SENT_NO_VALUE);
		}
		
		if (AERO_SENT_YES_VALUE.equalsIgnoreCase((String)flexCostSheet.getValue(AERO_COST_SHEET_COST_SENT_TO_S4_ATTR))) {
			costSheetFlags.put(AERO_COST_SHEET_COST_SENT_TO_S4_ATTR, AERO_SENT_NO_VALUE);
		}
		
		if (!costSheetFlags.isEmpty()) {
			updated = updateProductCostSheet(flexCostSheet, costSheetFlags, skipPersist);
		} else {
			LOGGER.debug("No need to reset Aero Costing integration flags for cost sheet " + flexCostSheet + ".");
		}
		
		return updated;
	}
	
	/**
	 * Updates the Aero Costing integration control flags for the given Cost Sheet and Process.<br>
	 * Flags to update according the specs are:<br>
	 * - Cost Sent To S4: Sets 'scYes' (if process is S4).<br>
	 * - Cost Sent To CAP: Sets 'scYes' (if process is CAP).<br>
	 * @param process The target system, either S4 or CAP.
	 * @param flexCostSheet The Product Cost Sheet where the flags to be updated are located.
	 * @return true if the flags have been updated, false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while attempting to update an invalid attribute within the flex object.
	 */
	public static boolean updateIntegrationControlFlags(AeroProcessesParam process, 
			LCSProductCostSheet flexCostSheet) throws WTPropertyVetoException, WTException {
		
		boolean updated = false;
		
		if (process == null || flexCostSheet == null) {
			return updated;
		}
		
		Map<String, Object> costSheetFlags = new HashMap<String, Object>();
		
		if (process == AeroProcessesParam.CAP) {
			costSheetFlags.put(AERO_COST_SHEET_COST_SENT_TO_CAP_ATTR, AERO_SENT_YES_VALUE);
		} else if (process == AeroProcessesParam.S4) {
			costSheetFlags.put(AERO_COST_SHEET_COST_SENT_TO_S4_ATTR, AERO_SENT_YES_VALUE);
		}
		
		boolean skipPersist = false;
		updated = updateProductCostSheet(flexCostSheet, costSheetFlags, skipPersist);
		
		return updated;
	}
	
	/**
	 * Updates the Aero Costing integration control flags for the given Source To Season Link.<br>
	 * Flags to update according the specs are:<br>
	 * - Sourcing Config Sent To S4: Sets 'scYes' unless already set to that value (if process is S4).<br>
	 * @param flexSourceToSeasonLink The Source To Season Link where the flags to be updated are located.
	 * @return true if the flags have been updated, false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while attempting to update an invalid attribute within the flex object.
	 */
	public static boolean updateIntegrationControlFlags(LCSSourceToSeasonLink flexSourceToSeasonLink) throws WTPropertyVetoException, WTException {
		
		boolean updated = false;
		
		if (flexSourceToSeasonLink == null) {
			return updated;
		}
		
		Map<String, Object> sourcingSeasonFlags = new HashMap<String, Object>();
		
		if (!AERO_SENT_YES_VALUE.equalsIgnoreCase((String)flexSourceToSeasonLink.getValue(AERO_SOURCING_CONFIG_SENT_TO_S4_ATTR))) {
			sourcingSeasonFlags.put(AERO_SOURCING_CONFIG_SENT_TO_S4_ATTR, AERO_SENT_YES_VALUE);
		}
		
		boolean skipPersist = false;
		updated = updateSourcingToSeasonLink(flexSourceToSeasonLink, sourcingSeasonFlags, skipPersist);
		
		return updated;
	}
	
	/**
	 * Updates (save/commit) the values for the given attr_key-values map into the given product cost sheet instance.
	 * @param flexCostSheet The Product Cost Sheet to update.
	 * @param values The attr_key-value maps containing the data to update into the product cost sheet.
	 * @param skipPersist Skips executing a persist on the flex object.
	 * @return true if the values have been updated, false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while attempting to update an invalid attribute within the flex object.
	 */
	private static boolean updateProductCostSheet(LCSProductCostSheet flexCostSheet,
			Map<String, Object> values,
			boolean skipPersist) throws WTException, WTPropertyVetoException {
		
		boolean skipPlugins = true;
		boolean updated = false;
		
		if (flexCostSheet == null || values == null || values.isEmpty()) {
			return updated;
		}
		
		Set<String> flagKeys = values.keySet();
		
		if (skipPersist) {
			for (String key : flagKeys) {
				flexCostSheet.setValue(key, values.get(key));
	        }
			LOGGER.debug("[updateProductCostSheet] Cost Sheet " + flexCostSheet + " persistance is skipped.");
		} else {
			
			try {
				
				if(!VersionHelper.isCheckedOut(flexCostSheet)) {
					flexCostSheet = VersionHelper.checkout(flexCostSheet);
					LOGGER.debug("[updateProductCostSheet] Cost Sheet " + flexCostSheet + " is checked-out.");
	            }
				
				for (String key : flagKeys) {
					flexCostSheet.setValue(key, values.get(key));
		        }
				
				flexCostSheet = (LCSProductCostSheet)LCSCostSheetLogic.persist(flexCostSheet, skipPlugins);
				
            	LOGGER.debug("[updateProductCostSheet] Cost Sheet " + flexCostSheet + " is updated.");
    			updated = true;
            } finally {
            	try {
                    if(VersionHelper.isCheckedOut(flexCostSheet)){
                        VersionHelper.checkin(flexCostSheet);
                        LOGGER.debug("[updateProductCostSheet] Cost Sheet " + flexCostSheet + " is checked-in.");
                    }
                } catch (WTException e) {
                   LOGGER.error("[updateProductCostSheet] failed to check in cost sheet " + flexCostSheet + ". " + e.getMessage(), e);
                }
            }
			
		}
		
		return updated;
	}
	
	/**
	 * Updates (save/commit) the values for the given attr_key-values map into the given Sourcing To Season Link instance.
	 * @param flexSourceToSeasonLink The Sourcing To Season Link to update.
	 * @param values The attr_key-value maps containing the data to update into the Sourcing To Season Link.
	 * @param skipPersist Skips executing a persist on the flex object.
	 * @return true if the values have been updated, false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while attempting to update an invalid attribute within the flex object.
	 */
	private static boolean updateSourcingToSeasonLink(LCSSourceToSeasonLink flexSourceToSeasonLink,
			Map<String, Object> values,
			boolean skipPersist) throws WTException, WTPropertyVetoException {
		
		boolean skipPlugins = true;
		boolean updated = false;
		
		if (flexSourceToSeasonLink == null || values == null || values.isEmpty()) {
			return updated;
		}
		
		Set<String> flagKeys = values.keySet();
		
		if (skipPersist) {
			
			for (String key : flagKeys) {
				flexSourceToSeasonLink.setValue(key, values.get(key));
	        }
			LOGGER.debug("[updateSourcingToSeasonLink] Sourcing To Season Link " + flexSourceToSeasonLink + " persistance is skipped.");
			
		} else {
			
			try {
				
				if(!VersionHelper.isCheckedOut(flexSourceToSeasonLink)) {
					flexSourceToSeasonLink = VersionHelper.checkout(flexSourceToSeasonLink);
					LOGGER.debug("[updateSourcingToSeasonLink]  Sourcing To Season Link " + flexSourceToSeasonLink + " is checked-out.");
	            }
				
				for (String key : flagKeys) {
					flexSourceToSeasonLink.setValue(key, values.get(key));
		        }
				
				flexSourceToSeasonLink = (LCSSourceToSeasonLink)LCSLogic.persist(flexSourceToSeasonLink, skipPlugins);
				
				LOGGER.debug("[updateSourcingToSeasonLink] Sourcing To Season Link " + flexSourceToSeasonLink + " is updated.");
    			updated = true;
            } finally {
            	try {
                    if(VersionHelper.isCheckedOut(flexSourceToSeasonLink)){
                        VersionHelper.checkin(flexSourceToSeasonLink);
                        LOGGER.debug("[updateSourcingToSeasonLink]  Sourcing To Season Link " + flexSourceToSeasonLink + " is checked-in.");
                    }
                } catch (WTException e) {
                   LOGGER.error("[updateSourcingToSeasonLink] failed to check in cost sheet " + flexSourceToSeasonLink + ". " + e.getMessage(), e);
                }
            }
			
		}
		
		return updated;
	}
	
	private AeroCostingRepository() {
		
	}
	
}
