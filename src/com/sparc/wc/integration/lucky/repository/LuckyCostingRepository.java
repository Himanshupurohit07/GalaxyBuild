package com.sparc.wc.integration.lucky.repository;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SOURCING_NO_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_VENDOR_FC_VENDOR_NO_LUCKY_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_VENDOR_FC_VENDOR_NO_LUCKY2_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_VENDOR_FC_VENDOR_NO_LUCKY3_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_VENDOR_LUCKY_INCOTERMS_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_VENDOR_LUCKY_INCOTERMS2_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_VENDOR_LUCKY_INCOTERMS3_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SOURCING_HTS_CODE_1_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SOURCING_HTS_CODE_1_WEIGHT_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_HTS_ASSIGNMENT_TABLE_CODE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SENT_TO_FC_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SEASON_DEVELOPMENT_SEASON_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SKIP_DEVELOPMENT_SEASONS_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SENT_TO_CAP_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SENT_YES_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SENT_NO_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SOURCING_CONFIG_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SHEET_MILESTONE_FINAL_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_HTS_CODE_1_WEIGHT_THRESHOLD_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_HTS_CODE_1_DEFAULT;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COST_SENT_TO_FC_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COST_SENT_TO_CAP_ATTR;

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
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSKUSourcingLink;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.VersionHelper;
import com.lcs.wc.sourcing.LCSCostSheetLogic;
import com.sparc.wc.integration.lucky.builders.LuckyCostingCostSheetsQueryBuilder;
import com.sparc.wc.integration.lucky.builders.LuckyCostingCostSheetsQueryBuilder.ColorwayCostSheetKey;
import com.sparc.wc.integration.lucky.builders.LuckyCostingCostSheetsQueryBuilder.ColorwayCostSheetsResult;
import com.sparc.wc.integration.lucky.builders.LuckyCostingSourcingConfigIdsQueryBuilder;
import com.sparc.wc.integration.lucky.domain.LuckyCostingColorwayCostSheetMaster;
import com.sparc.wc.integration.lucky.domain.LuckyCostingProcessesParam;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.sparc.wc.util.SparcConstants;

import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * Repository for accessing the various data elements from PLM related to Lucky/Costing FC & CAP integration.<br>
 * FIXES/AMENDMENTS:<br>
 * - Task #9235 (UAT): Change to use HTS Code value instead of name from HTS Assignment Table object.<br>
 * - Task #10162 (Hypercare): Updated HTS code value resolution logic per new specs.<br>
 * 
 * @author Acnovate
 * @see "Task #8259 3.1 Costing API - FC & CAP (Outbound)"
 */
public class LuckyCostingRepository {
	
	private static final Logger LOGGER = LogR.getLogger(LuckyCostingRepository.class.getName());
	
	//Enumeration for out-of-the-box flex tables used in this respository.
	private static enum LCSTables {
		LCSSOURCINGCONFIG;
	}
	
	//Enumeration for out-of-the-box flex object references
	private static enum LCSObjectReferences {
		LCSSOURCINGCONFIG("OR:com.lcs.wc.sourcing.LCSSourcingConfig:"),
		LCSPRODUCT("OR:com.lcs.wc.product.LCSProduct:"),
		LCSSEASON("OR:com.lcs.wc.season.LCSSeason:"),
		LCSPRODUCTCOSTSHEET("OR:com.lcs.wc.sourcing.LCSProductCostSheet:"),
		LCSSKU("OR:com.lcs.wc.product.LCSSKU:"),
		LCSSOURCETOSEASONLINK("OR:com.lcs.wc.sourcing.LCSSourceToSeasonLink:"),
		LCSSKUSOURCINGLINK("OR:com.lcs.wc.sourcing.LCSSKUSourcingLink:");
		
		private String objRef;
		
		LCSObjectReferences(String objRef) {
			this.objRef = objRef;
		}
		
		public String getObjRefString() {
			return objRef;
		}
	}
	
	private LuckyCostingRepository() {
		
	}
	
	/**
	 * Lookup and retrieve from PLM all Sourcing Configuration Numbers that contain cost sheets 
	 * which meet the criteria for Lucky Costing integration for FC & CAP.
	 * @param process The identifier for the lucky target system, i.e. FC or CAP.
	 * @return The list of sourcing configuration numbers.
	 * @throws WTException If a flex api error occurs while retrieving/accessing the data from PLM.
	 */
	public static Set<String> findCostingSourcingConfigNumbers(LuckyCostingProcessesParam process) throws WTException {
		
		SearchResults qryResults = null; 
		Set<String> sourceConfigIds = new HashSet<String>();
		
		PreparedQueryStatement pqs = new LuckyCostingSourcingConfigIdsQueryBuilder(process).build();
		
		LOGGER.debug("[findCostingSourcingConfigNumbers for " + process + "] Query: " + pqs);
        qryResults = LCSQuery.runDirectQuery(pqs);
        LOGGER.debug("[findCostingSourcingConfigNumbers for " + process + "] Query results: Found " + qryResults.getResultsFound() + " records.");
		
        if (qryResults != null) {
        	
        	final FlexType srcConfigFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_SOURCING_CONFIG_FLEX_TYPE_PATH);
            final String srcConfigNumColName = srcConfigFlexType.getAttribute(LUCKY_COSTING_SOURCING_NO_ATTR).getColumnName();
        	
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
                
                LOGGER.debug("Sourcing Configuration #" + srcConfigNum + " contain eligible cost sheet(s) for Lucky Costing " + process + " Integration.");
                
                sourceConfigIds.add(srcConfigNum);
                
        	}
        	
        }
        
		return sourceConfigIds;
	}
	
	/**
	 * Search for integration-eligible season-product-sourcing-colorway cost sheets.<br>
	 * To fulfill the requirements and determine the right cost sheet for the season-product-sourcing-colorway, this method will perform the following:<br>
	 * - Query PLM for eligible product cost sheets linked to any active Lucky season-sourcing-colorway.<br>
	 * - As part of the query design, any Lucky seasonal products that don't have any sourcing-colorways will be ignored/excluded.<br>
	 * - Similarly, any product-sourcing-colorways which are inactive will be ignored by the query.<br>
	 * - Eligible (candidate) cost sheets retrieved by the query are those that meet the respective requirements defined in items #3 and #4 from 
	 * the integration document.<br>
	 * - To find the integration-eligible cost sheet for a given season-product-sourcing-colorway when multiple cost sheets are found for a colorway, 
	 * those (candidate) cost sheets retrieved earlier by the query will be further filtered according the requirements defined in item #5 from 
	 * the integration document.
	 * 
	 * @param sourcingConfigNumber The seasonal product's Sourcing Config Number linked to the cost sheets and colorways.
	 * @param process The identifier for the lucky target system, i.e. FC or CAP.
	 * @return
	 * @throws WTException
	 */
	public static List<LuckyCostingColorwayCostSheetMaster> findEligibleColorwayCostSheets(Long sourcingConfigNumber, 
			LuckyCostingProcessesParam process) throws WTException {
		
		SearchResults qryResults = null; 
		PreparedQueryStatement pqs = new LuckyCostingCostSheetsQueryBuilder(process, sourcingConfigNumber).buildQuery();
		List<LuckyCostingColorwayCostSheetMaster> colorwayCostSheetsList = new ArrayList<LuckyCostingColorwayCostSheetMaster>();
		
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
        	Map<ColorwayCostSheetKey, Set<ColorwayCostSheetsResult>> costSheetMap = 
        			new HashMap<ColorwayCostSheetKey, Set<ColorwayCostSheetsResult>>();
        	
        	@SuppressWarnings("unchecked")
			Collection<FlexObject> resultSet = qryResults.getResults();
        	
        	int debugCount = 1;
        	
        	for (FlexObject fo : resultSet) {
        		
        		ColorwayCostSheetsResult costSheetResult = LuckyCostingCostSheetsQueryBuilder.buildResult(fo);
    			
    			LOGGER.debug("costSheetResult [" + debugCount + "] for " + sourcingConfigNumber + " (" + process + ") is: " + costSheetResult);
    			debugCount += 1;
    			
    			Set<ColorwayCostSheetsResult> colorwayCostSheetsSet = costSheetMap.get(costSheetResult.getCostSheetKey());
    			
    			if (colorwayCostSheetsSet != null) {
    				colorwayCostSheetsSet.add(costSheetResult);
    			} else {
    				colorwayCostSheetsSet = new HashSet<ColorwayCostSheetsResult>();
    				colorwayCostSheetsSet.add(costSheetResult);
    				costSheetMap.put(costSheetResult.getCostSheetKey(), colorwayCostSheetsSet);
    			}
        		
        	}//end for loop.
        	
        	LOGGER.info("Found " + costSheetMap + " colorways for " + sourcingConfigNumber + " (" + process + ").");
        	
        	for (ColorwayCostSheetKey csKey : costSheetMap.keySet()) {
        		
        		ColorwayCostSheetsResult costSheet = null;
        		Set<ColorwayCostSheetsResult> csSet = costSheetMap.get(csKey);
        		
        		if (csSet.isEmpty()) {
        			//This scenario shouldn't ever occur in a normal execution where any lucky season-product-sourcing that 
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
        		
        		LuckyCostingColorwayCostSheetMaster csMaster = loadColorwayCostSheetMaster(costSheet);
        		LOGGER.debug("Colorway Cost Sheet Master loaded for Colorway #" + csKey.getColorwayNumber() 
        				+ " (" + csKey.getColorwayName() + "): " + csMaster);
        		
        		colorwayCostSheetsList.add(csMaster);
        		
        	}//end for loop.
        	
        }
        
        return colorwayCostSheetsList;        
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
	private static ColorwayCostSheetsResult lookupLatestEligibleCostSheet(Set<ColorwayCostSheetsResult> colorwayCostSheetsSet) {
		
		ColorwayCostSheetsResult costSheet = null;
		
		if (colorwayCostSheetsSet == null || colorwayCostSheetsSet.isEmpty()) {
			return costSheet;
		}
		
		ColorwayCostSheetsResult latestModifiedCostSheet = null;
		ColorwayCostSheetsResult latestModifiedFinalCostSheet = null;
		
		//Lookup cost sheets with Milestone "Final" and get the latest saved cost sheet.
		for (ColorwayCostSheetsResult cs : colorwayCostSheetsSet) {
			
			if (latestModifiedCostSheet == null) {
				latestModifiedCostSheet = cs;
			} else if (cs.getCostSheetModifiedDate() != null &&
					cs.getCostSheetModifiedDate().compareTo(latestModifiedCostSheet.getCostSheetModifiedDate()) > 0) {
				latestModifiedCostSheet = cs;
			}
			
			if (LUCKY_COSTING_COST_SHEET_MILESTONE_FINAL_VALUES.contains(cs.getCostSheetMilestone())) {
				
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
		
		return costSheet;
	}
	
	/**
	 * Creates a LuckyCostingColorwayCostSheetMaster instance by loading it with the respective flex objects from PLM using 
	 * the flex object internal identifiers avalable within the given colorway cost sheet result instance.
	 * @param colorwayCostSheetResult The colorway cost sheet result instance containing the required flex object internal identifiers.
	 * @return The Costing Colorway Cost Sheet Master instance with all available Flex Objects loaded where applicable.
	 */
	private static LuckyCostingColorwayCostSheetMaster loadColorwayCostSheetMaster(ColorwayCostSheetsResult colorwayCostSheetResult) throws WTException {
		
		LuckyCostingColorwayCostSheetMaster costSheetMaster = null;
		
		if (colorwayCostSheetResult == null) {
			return costSheetMaster;
		}
		
		Long sourcingConfigFlexId = colorwayCostSheetResult.getCostSheetKey().getSourcingConfigId();
		Long productFlexId = colorwayCostSheetResult.getProductId();
		Long seasonFlexId = colorwayCostSheetResult.getCostSheetKey().getSeasonId();
		Long colorwayFlexId = colorwayCostSheetResult.getCostSheetKey().getColorwayId();
		Long prodCostSheetFlexId = colorwayCostSheetResult.getCostSheetId();
		Long sourcingSeasonLinkFlexId = colorwayCostSheetResult.getSourcingSeasonLinkId();
		Long sourcingColorwayLinkFlexId = colorwayCostSheetResult.getSourcingColorwayLinkId();
		
		costSheetMaster = new LuckyCostingColorwayCostSheetMaster();
		
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
		
		return costSheetMaster;
	}
	
	/**
	 * Identifies the appropriate Full Circle Vendor Number within the given Flex Vendor object using the given Cost Sheet Incoterms.<br>
	 * This implementes item #6 of the integration functional requirements document, which describes how Full Circle Vendor Number is identified. 
	 * @param costSheetIncoterms The Cost Sheet Incoterms required to resolve the FC Vendor Number from the Vendor.
	 * @param flexVendor The Flex Vendor Object containing the FC Vendor Number to resolve.
	 * @return The Full Circle Vendor Number or <code>null</code> if either or both cost sheet incoterms or flex vendor object were not provided,
	 * or if no match could be found 
	 * @throws WTException If a flex api error occurs while retrieving/accessing the data from the Vendor at PLM.
	 */
	public static String resolveFullCircleVendorNumber(String costSheetIncoterms, LCSSupplier flexVendor) throws WTException {
		
		String fcVendorNoLucky = null;
		
		if (costSheetIncoterms == null || flexVendor == null) {
			return fcVendorNoLucky;
		}
		
		String vendorLuckyIncoterms = (String)flexVendor.getValue(LUCKY_COSTING_VENDOR_LUCKY_INCOTERMS_ATTR);
		String vendorLuckyIncoterms2 = (String)flexVendor.getValue(LUCKY_COSTING_VENDOR_LUCKY_INCOTERMS2_ATTR);
		String vendorLuckyIncoterms3 = (String)flexVendor.getValue(LUCKY_COSTING_VENDOR_LUCKY_INCOTERMS3_ATTR);
		String vendorNoLucky = (String)flexVendor.getValue(LUCKY_COSTING_VENDOR_FC_VENDOR_NO_LUCKY_ATTR);
		String vendorNoLucky2 = (String)flexVendor.getValue(LUCKY_COSTING_VENDOR_FC_VENDOR_NO_LUCKY2_ATTR);
		String vendorNoLucky3 = (String)flexVendor.getValue(LUCKY_COSTING_VENDOR_FC_VENDOR_NO_LUCKY3_ATTR);
		
		if (costSheetIncoterms.equalsIgnoreCase(vendorLuckyIncoterms)) {
			fcVendorNoLucky = vendorNoLucky;
		} else if (costSheetIncoterms.equalsIgnoreCase(vendorLuckyIncoterms2)) {
			fcVendorNoLucky = vendorNoLucky2;
		} else if (costSheetIncoterms.equalsIgnoreCase(vendorLuckyIncoterms3)) {
			fcVendorNoLucky = vendorNoLucky3;
		}
		
		LOGGER.debug("[resolveFullCircleVendorNumber] Cost Sheet Incoterms: " + costSheetIncoterms 
				+ ", Vendor Lucky Incoterms: " + vendorLuckyIncoterms + " - Vendor No Lucky: " + vendorNoLucky 
				+ ", Vendor Lucky Incoterms 2: " + vendorLuckyIncoterms2 + " - Vendor No Lucky 2: " + vendorNoLucky2
				+ ", Vendor Lucky Incoterms 3: " + vendorLuckyIncoterms3 + " - Vendor No Lucky 3: " + vendorNoLucky3
				+ ", Resolved Full Circle Vendor Number is: " + fcVendorNoLucky);
		
		return fcVendorNoLucky;
	}
	
	/**
	 * Identifies the appropriate value for HTS Code 1 from the given Sourcing Season Link object.
	 * @param flexSourcingSeasonLink The Sourcing Season Link object containing the required attributes to identify the appropriate HTS Code 1 value.
	 * @return The HTS Code 1 value identified or <code>null</code> if no Sourcing Season Link object was provided or if a number parsing error occurs.
	 * @throws WTException If a flex api error occurs while retrieving/accessing the data from the Sourcing Season Link object at PLM.
	 */
	public static String resolveHTSCode1(LCSSourceToSeasonLink flexSourcingSeasonLink) throws WTException {
		
		String htsCode1 = null;
		
		if (flexSourcingSeasonLink == null) {
			return htsCode1;
		}
		
		try {
			
			String htsCode1Name = null;
			Double htsCode1WeightPercentThreshold = Double.parseDouble(LUCKY_COSTING_HTS_CODE_1_WEIGHT_THRESHOLD_VALUE);
			Double htsCode1WeightPercent = (Double)flexSourcingSeasonLink.getValue(LUCKY_COSTING_SOURCING_HTS_CODE_1_WEIGHT_ATTR);
			
			if (htsCode1WeightPercentThreshold.equals(htsCode1WeightPercent)) {
				LCSLifecycleManaged htsAssignmentTableObj = (LCSLifecycleManaged)flexSourcingSeasonLink.getValue(LUCKY_COSTING_SOURCING_HTS_CODE_1_ATTR);
				
				if (htsAssignmentTableObj != null) {
					htsCode1Name = (String)htsAssignmentTableObj.getValue(LUCKY_COSTING_HTS_ASSIGNMENT_TABLE_CODE_ATTR);
					htsCode1 = htsCode1Name;
				}
				
			} else if (htsCode1WeightPercent != null && htsCode1WeightPercent.doubleValue() != 0d) {
				htsCode1 = LUCKY_COSTING_HTS_CODE_1_DEFAULT;
			}
			
			LOGGER.debug("[resolveHTSCode1] HTS Code 1 Weight %: " + htsCode1WeightPercent
					+ ", HTS Code 1 Weight % threshold: " + htsCode1WeightPercentThreshold
					+ ", HTS Code 1 (From HTS Assignment Table Name): " + htsCode1Name
					+ ", HTS Code 1 (Default value): " + LUCKY_COSTING_HTS_CODE_1_DEFAULT
					+ ", HTS Code 1 is: " + htsCode1);
			
		} catch(WTException wtx) {
			throw wtx;
	    } catch(Exception e) {
			LOGGER.error("Unable to resolve value for Sourcing Season's HTS Code 1 attribute: " + e.getMessage(), e);
		}
		
		return htsCode1;
	}
	
	/**
	 * Retrieves a list of valid seasons associated to the given product.
	 * @param flexProduct The product to retrieve the seasons for.
	 * @return The list of seasons found for the product, or an empty list if no seasons were found.
	 * @throws WTException If an error occurs while retrieving seasons data from PLM.
	 */
	@SuppressWarnings("unchecked")
	public static List<LCSSeason> getValidSeasonsForProduct(LCSProduct flexProduct) throws WTException {
		
		Collection<LCSSeasonMaster> seasons = null;
		List<LCSSeason> seasonsList = new ArrayList<LCSSeason>();
		
		if (flexProduct == null) {
			return seasonsList;
		}
		
		seasons = new LCSSeasonQuery().findSeasons(flexProduct);
		
		for(LCSSeasonMaster master: seasons) {
			
			LCSSeason seasonObj = (LCSSeason) VersionHelper.latestIterationOf(master);
			
			if(seasonObj!=null) {
				
				String developmentSeason = (String)SparcIntegrationUtil.getValueFrom(seasonObj, LUCKY_COSTING_SEASON_DEVELOPMENT_SEASON_ATTR);
				
				if (LUCKY_COSTING_SKIP_DEVELOPMENT_SEASONS_VALUES.contains(developmentSeason)) {
					LOGGER.debug("Latest Season lookup: Skipping season " + seasonObj.getName() + " as it has an invalid Development Season Type value.");
					continue;
				}
				
				seasonsList.add(seasonObj);
				
			}
		}
		
		return seasonsList;		
	}
	
	/**
	 * Updates the Lucky Costing integration control flags for the given sourcing-colorway and process.<br>
	 * Flags to update according the specs are:<br>
	 * - Sent To FC: Sets scYes for sourcing-colorways (if process is FC).<br>
	 * - Cost Sheet Sent To FC: Sets the number of the Cost Sheet for sourcing-colorways (if process is FC).<br>
	 * - Sent To CAP: Sets scYes for sourcing-colorways (if process is CAP).<br>
	 * - Cost Sheet Sent To CAP: Sets the number of the Cost Sheet for sourcing-colorways (if process is CAP).
	 * @param costSheetName The integration eligible cost sheet sent to FC or CAP.
	 * @param process The target system, either FC or CAP.
	 * @param flexSourcingColorwayLink The sourcing-colorway link where the flags to be updated are located.
	 * @return true if the flags hasve been updated, false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while processing the flag update at PLM.
	 */
	public static boolean updateIntegrationControlFlags(String costSheetNumber,
														LuckyCostingProcessesParam process,
														LCSProductCostSheet objProductCostSheet) throws WTPropertyVetoException, WTException {
		//Lucky Enhancement - Start
		boolean updated = false;

		if (process == null || objProductCostSheet == null) {
			return updated;
		}

		Map<String, Object> sourcingColorwayFlags = new HashMap<String, Object>();
		System.out.println("inside process----------------------"+process);
		System.out.println("LuckyCostingProcessesParam.FC------------------------------"+LuckyCostingProcessesParam.FC);
		if (process == LuckyCostingProcessesParam.FC) {
			System.out.println("LUCKY_COST_SENT_TO_FC_ATTR-----------"+LUCKY_COST_SENT_TO_FC_ATTR);
			System.out.println("LUCKY_COSTING_SENT_YES_VALUE-----------"+LUCKY_COSTING_SENT_YES_VALUE);
			sourcingColorwayFlags.put(LUCKY_COST_SENT_TO_FC_ATTR, LUCKY_COSTING_SENT_YES_VALUE);
		} else if (process == LuckyCostingProcessesParam.CAP) {
			sourcingColorwayFlags.put(LUCKY_COST_SENT_TO_CAP_ATTR, LUCKY_COSTING_SENT_YES_VALUE);
		}

		updated = updateSourcingColorwayValues(objProductCostSheet, sourcingColorwayFlags,false);
		//Lucky Enhancement - End
		return updated;
	}
	
	/**
	 * Checks whether the colorway associated to the given sourcing-colorway is a placeholder.
	 * @param flexSourcingColorwayLink
	 * @return true if the sourcing-colorway is bound to a colorway that is a placeholder, false otherwise.
	 * @throws WTException If an error occurs while retrieving the colorway info from PLM.
	 */
	public static boolean isSourcingColorwayPlaceholder(LCSSKUSourcingLink flexSourcingColorwayLink) throws WTException {
		
		boolean isPlaceholder = false;
		
		if (flexSourcingColorwayLink == null) {
			return isPlaceholder;
		}
		
		LCSSKU colorway = (LCSSKU)VersionHelper.getVersion(flexSourcingColorwayLink.getSkuMaster(),"A");
		
		return colorway.isPlaceholder();
	}
	
	/**
	 * Checks whether the Lucky Costing integration flags for the given sourcing-colorway are reset.
	 * The following attribute flags will be checked from the sourcing-colorway:<br>
	 * - Sent To FC value is scNo.<br>
	 * - Sent To CAP value is scNo.<br>
	 * @param flexSourcingColorwayLink The sourcing-colorway object containing the lucky costing integration flags to check.
	 * @return true if the Lucky Costing integrations flags are reset, false otherwise. 
	 */
	public static boolean isIntegrationFlagsReset(LCSSKUSourcingLink flexSourcingColorwayLink) {
		
		boolean isReset = false;
		
		if (flexSourcingColorwayLink == null) {
			return isReset;
		}
		
		String costSentToFC = (String)SparcIntegrationUtil.getValueFrom(flexSourcingColorwayLink, LUCKY_COSTING_COST_SENT_TO_FC_ATTR);
		String costSentToCAP = (String)SparcIntegrationUtil.getValueFrom(flexSourcingColorwayLink, LUCKY_COSTING_COST_SENT_TO_CAP_ATTR);
		
		isReset = (costSentToFC == null && costSentToCAP == null);
		
		if (!isReset) {
			isReset = LUCKY_COSTING_SENT_NO_VALUE.equalsIgnoreCase(costSentToFC)
					&& LUCKY_COSTING_SENT_NO_VALUE.equalsIgnoreCase(costSentToCAP);
		}
		
		return isReset;
		
	}


	public static boolean resetIntegrationControlFlags(LCSProductCostSheet flexCostSheet) throws WTPropertyVetoException, WTException {
		return resetIntegrationControlFlags(flexCostSheet, false);
	}

	/**
	 * Resets the Lucky Costing integration control flags for the given sourcing-colorway.<br>
	 * Flags to update for the given sourcing-colorway are:<br>
	 * - Sent To FC: Sets scNo.<br>
	 * - Sent To CAP: Sets scNo.<br>
	 * @param objProdCostSheet The Product Cost Sheet link where the flags to be updated are located.
	 * @return true if the flags hasve been reset (updated), false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while processing the flag update at PLM.
	 */
	public static boolean resetIntegrationControlFlags(LCSProductCostSheet objProdCostSheet,boolean skipPersist) throws WTPropertyVetoException, WTException {

		boolean updated = false;

		if (objProdCostSheet == null) {
			return updated;
		}

		Map<String, Object> sourcingColorwayFlags = new HashMap<String, Object>();

		sourcingColorwayFlags.put(LUCKY_COST_SENT_TO_FC_ATTR, LUCKY_COSTING_SENT_NO_VALUE);
		sourcingColorwayFlags.put(LUCKY_COST_SENT_TO_CAP_ATTR, LUCKY_COSTING_SENT_NO_VALUE);

		updated = updateSourcingColorwayValues(objProdCostSheet, sourcingColorwayFlags,skipPersist);

		return updated;
	}
	
	/**
	 * Updates the values for the given attr_key-values map into the given sourcing-colorway link.
	 * @param flexCostSheet The Product CostSheeet link to update.
	 * @param values The attr_key-value maps containing the data to update into the Sourcing-Colorway.
	 * @return true if the values have been updated, false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while processing the flag update at PLM.
	 */
	private static boolean updateSourcingColorwayValues(LCSProductCostSheet flexCostSheet,
														Map<String, Object> values,boolean skipPersist) throws WTPropertyVetoException, WTException {
		boolean updated = false;
		boolean skipPlugins = true;

		if (flexCostSheet == null || values == null || values.isEmpty()) {
			return updated;
		}
		Set<String> flagKeys = values.keySet();
		if(skipPersist){
			for (String key : flagKeys) {
				flexCostSheet.setValue(key, values.get(key));
			}

		}else {
			try {

				System.out.println("Is Latest iteration--------------------"+flexCostSheet.isLatestIteration());
				
				if (!VersionHelper.isCheckedOut(flexCostSheet)) {
					flexCostSheet = VersionHelper.checkout(flexCostSheet);
					LOGGER.debug("[updateProductCostSheet] Cost Sheet " + flexCostSheet + " is checked-out.");
				}
				for (String key : flagKeys) {
					flexCostSheet.setValue(key, values.get(key));
				}

				flexCostSheet = (LCSProductCostSheet) LCSCostSheetLogic.persist(flexCostSheet, skipPlugins);

				LOGGER.debug("[updateProductCostSheet] Cost Sheet " + flexCostSheet + " is updated.");
				updated = true;
			} finally {
				try {
					if (VersionHelper.isCheckedOut(flexCostSheet)) {
						VersionHelper.checkin(flexCostSheet);
						LOGGER.debug("[updateProductCostSheet] Cost Sheet " + flexCostSheet + " is checked-in.");
					}
				} catch (WTException e) {
					LOGGER.error("[updateProductCostSheet] failed to check in cost sheet " + flexCostSheet + ". " + e.getMessage(), e);
				}
			}
		}
		LOGGER.debug("Product Cost Sheet " + flexCostSheet + " is updated.");
		return updated;
	}
	
	/**
	 * Retrieve the enumeration's secondary key value for given flex type object and attribute name.
	 * @param flexObj The flex objext containing the attribute.
	 * @param attrName The name of the attribute to lookup for the secondary key value from.
	 * @return The enumeration's secondary key for the given attribute.
	 * @throws WTException If an error occurs while extracting the secondary key value from the flex object.
	 */
	public static String lookupEnumSecondaryKeyValue(FlexTyped flexObj, String attrName) throws WTException {
		
		return lookupAttrEnumKeyValue(flexObj, attrName, SparcConstants.CUSTOM_SECONDARY_KEY);
		
	}
	
	/**
	 * Retrieve the enumeration's display name for the given flex type object and attribute name.
	 * @param flexObj The flex objext containing the attribute.
	 * @param attrName The name of the attribute to lookup for the display name value from.
	 * @return The enumeration's displat name for the given attribute.
	 * @throws WTException If an error occurs while extracting the display name value from the flex object.
	 */
	public static String lookupEnumDisplayValue(FlexTyped flexObj, String attrName) throws WTException {
		
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
	private static String lookupAttrEnumKeyValue(FlexTyped flexObj, String attrName, String enumAttrName) throws WTException {
		
		String value = null;
		
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
