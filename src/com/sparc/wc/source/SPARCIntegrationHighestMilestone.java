package com.sparc.wc.source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.sourcing.*;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.util.SparcConstants;


import org.apache.logging.log4j.Logger;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.util.WTException;

/**
 * @author dsagar56
 *
 */
public class SPARCIntegrationHighestMilestone {

	private static final Logger LOGGER = LogR.getLogger(SPARCIntegrationHighestMilestone.class.getName());

	private static String COSTSHEET_MILESTONE_STATUS = "scMilestoneStatus";
	private static String ALLOWED_CSMILESTONE_ENTRY = LCSProperties.get("Integration.attributes.costSheetMileStones","scCrone,scPostcrone,scCrtwo,scPostcrtwo,scFinal");

	public static void setHighestMilestoneSKUSourcingCreate(WTObject wtObject) throws WTException {
		System.out.println("---- SSP for SET Highest Milestone COSTSHEET FROM CREATE ----");
		setHighestMilestone(wtObject, "CREATE");
	}
	
	public static void setHighestMilestoneSKUSourcingUpdate(WTObject wtObject) throws WTException {
		System.out.println("---- SSP for SET Highest Milestone COSTSHEET FROM UPDATE ----");
		setHighestMilestone(wtObject, "UPDATE");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void setHighestMilestone(WTObject wtObject, String mode) throws WTException {
		System.out.println("---- SSP for setHighestMilestone ----");
		String currentMileStone = null;
		String currentStatus = null;
		String setCSID = null;
		Map higestMilestoneCSIDMap = new HashMap();
		List<Integer> higestIntegerList = new ArrayList<Integer>();
		boolean validMilestone = false;
		boolean validStatus = false;
		String costSheetType ="";
		if(wtObject instanceof LCSProductCostSheet) {
			LCSProductCostSheet currentProdCS = (LCSProductCostSheet)wtObject;
			LCSCostSheetMaster currentProdCSMaster = (LCSCostSheetMaster) currentProdCS.getMaster();
			LCSSourcingConfigMaster scMaster = currentProdCS.getSourcingConfigMaster();
			LCSSeasonMaster seasonMaster = currentProdCS.getSeasonMaster();
			Boolean isApplicable  = false;
			if(seasonMaster!=null && scMaster != null){
				FlexTypeAttribute milestoneFlexTypeAtt = currentProdCS.getFlexType().getAttribute(COSTSHEET_MILESTONE_STATUS);
				currentMileStone = (String)currentProdCS.getValue(COSTSHEET_MILESTONE_STATUS);
				currentStatus = (String)currentProdCS.getValue(SparcConstants.COSTSHEET_STATUS);

				//9373 - Apply this functionality only for Reebok and Sparc Type - Start
				costSheetType=currentProdCS.getFlexType().getFullName(true);
				LOGGER.debug("costSheetType--------First---------------"+costSheetType);

				isApplicable= costSheetType.equalsIgnoreCase(SparcConstants.SPARC_COSTSHEET) ? true : costSheetType.contains("Reebok")? true : false;

				LOGGER.debug("isApplicable------First------------------"+isApplicable);

				if(!isApplicable){
					LOGGER.info(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "----Returning from Validation as Cost sheet type is not valid----:"+costSheetType);
					return;
				}

				if(ALLOWED_CSMILESTONE_ENTRY.contains(currentMileStone))validMilestone = true;
				if(SparcConstants.COSTSHEET_STATUS_CONFIRMED_CC.equalsIgnoreCase(currentStatus))validStatus = true;
				
				currentMileStone = milestoneFlexTypeAtt.getAttValueList().getValueFromValueStore(currentMileStone, "_CUSTOM_scSecondaryKey");
				Integer highestInteger = Integer.valueOf(0);
				if(FormatHelper.hasContent(currentMileStone)){
					highestInteger = Integer.valueOf(currentMileStone);
				}
				
				String applicableColorNames = currentProdCS.getApplicableColorNames();
			   // System.out.println("applicableColorNames"+applicableColorNames);
				List<String> costsheetSKUs = (List) MOAHelper.getMOACollection(applicableColorNames);
				//System.out.println("costsheetSKUs>>>"+costsheetSKUs);

				
				LCSSourcingConfig sourcingConfig = (LCSSourcingConfig)VersionHelper.latestIterationOf(scMaster);
				LCSProduct productARev = (LCSProduct)VersionHelper.getVersion(currentProdCS.getProductMaster(), "A");
				LCSSeason season = (LCSSeason)VersionHelper.latestIterationOf(seasonMaster);
				
				//-- Fetch any productCS if available(incl. highest milestone check)
				Collection prodCSCol = getCSForProduct(currentProdCS, productARev,
						season, sourcingConfig, mode, null);
				//System.out.println("-- Milestone Boolean Flag : "+validMilestone);
				//System.out.println("-- CSStaus Boolean Flag : "+validStatus);
				if(!FormatHelper.hasContent(applicableColorNames) && validMilestone && validStatus) {
					higestIntegerList.add(highestInteger);
					higestMilestoneCSIDMap.put(highestInteger, getCurrentCSBranchID(currentProdCS, mode));
				}
				//System.out.println("--- prodCSCol : "+prodCSCol);
				
				setCSID = fetchHighestMilestoneCS(prodCSCol, milestoneFlexTypeAtt,
							higestIntegerList, higestMilestoneCSIDMap);
				
				//System.out.println("--- setCSID : "+setCSID);
				
				//-- getSKUSourcingLink
				//-- Iterate through SKUSourcingLink and get SKU
				Collection skuSourcingLinksCol = new LCSSourcingConfigQuery().getSkuSourcingLinks(scMaster, null, seasonMaster, true);
				
				LCSSKUSourcingLink eachSKUSourcingLink = null;
				LCSSKU eachSKUARev = null;
				String skuName = null;
				Collection skuCSCol = null;
				
				LCSProductCostSheet csObject = null; //Added on 8/8/2023
				higestMilestoneCSIDMap = new HashMap();
				higestIntegerList = new ArrayList<Integer>();
				String tmpSetCSID = null;
				if(skuSourcingLinksCol!=null && skuSourcingLinksCol.size() > 0) {
					Iterator skuLinksCollITR = skuSourcingLinksCol.iterator();
					while(skuLinksCollITR.hasNext()) {
						tmpSetCSID = null;
						higestMilestoneCSIDMap = new HashMap();
						higestIntegerList = new ArrayList<Integer>();
						eachSKUSourcingLink = (LCSSKUSourcingLink)skuLinksCollITR.next();
						if(eachSKUSourcingLink.isActive()) {
							eachSKUARev = (LCSSKU)VersionHelper.getVersion(eachSKUSourcingLink.getSkuMaster(),"A");
							if(!eachSKUARev.isPlaceholder()) {
								skuName = (String)eachSKUARev.getValue("skuName");
								//System.out.println("skuName>>>>"+skuName + skuName.length());
								//-- Use SKU to fetch and respective SKUCS(incl. highest milestone check)
								skuCSCol = getCSForProduct(currentProdCS, productARev,
										season, sourcingConfig, mode, skuName);
								//System.out.println("skuCSCol"+skuCSCol.size());
								
								// Saurabh 8/8/2023 - Fetch only colorway specific costsheets
							    Collection skuCSColFinal = new ArrayList<>();  //Added on 8/8/2023

								Iterator skuCSColItr = skuCSCol.iterator();
								while(skuCSColItr.hasNext() ){
									isApplicable = false;
									csObject = (LCSProductCostSheet) skuCSColItr.next();
									//9373 - Apply this functionality only for Reebok and Sparc Type - Start
									costSheetType=csObject.getFlexType().getFullName(true);

									LOGGER.debug("costsheet type-------second-----------------"+costSheetType);

									isApplicable= costSheetType.equalsIgnoreCase(SparcConstants.SPARC_COSTSHEET) ? true : costSheetType.contains("Reebok")? true : false;

									LOGGER.debug("isApplicable-----second------------"+isApplicable);

									if(!isApplicable){
										LOGGER.info(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "iterating prodSheet  "+csObject);
										continue;
									}

									//9373 - Apply this functionality only for Reebok and Sparc Type - End
									String applicableColorNames2 = csObject.getApplicableColorNames();
									//System.out.println("applicableColorNames2" + applicableColorNames2);
									List<String> costsheetSKUs2 = (List) MOAHelper.getMOACollection(applicableColorNames2);
									//System.out.println("costsheetSKUs2>>>" + costsheetSKUs2);
									
									if (FormatHelper.hasContent(applicableColorNames2)) {
                                        
										Iterator costsheetSKUsItr2 = costsheetSKUs2.iterator();
										while (costsheetSKUsItr2.hasNext()) {
											String appColoName = (String) costsheetSKUsItr2.next();
											//System.out.println("appColoName>>" + appColoName + appColoName.length());
											if (skuName.equalsIgnoreCase(appColoName)) {
												//System.out.println("Colorway Name matches then add costsheet to collection");
												
												skuCSColFinal.add(csObject);
												break;

											}


										}
									}
							
									
								}
								//End
								
								
								
								String colNumberValue = (String) eachSKUARev.getValue("scColorwayNo").toString();
								//System.out.println("-- Milestone Boolean Flag(SKU) : "+ALLOWED_CSMILESTONE_ENTRY.contains(currentMileStone));
								if(validMilestone && validStatus && FormatHelper.hasContent(applicableColorNames)) {

									Iterator costsheetSKUsItr = costsheetSKUs.iterator();
									while(costsheetSKUsItr.hasNext()){
										String appColoName = (String) costsheetSKUsItr.next();
										//System.out.println("appColoName>>"+appColoName +appColoName.length());
										if(skuName.equalsIgnoreCase(appColoName)){
										//System.out.println("SKU NAMe is Matching");
										higestIntegerList.add(highestInteger);
										higestMilestoneCSIDMap.put(highestInteger, getCurrentCSBranchID(currentProdCS, mode));
									}
									}
								}
							//	System.out.println("--- skuCSCol : "+skuCSCol);
								if(skuCSColFinal != null){
								tmpSetCSID = fetchHighestMilestoneCS(skuCSColFinal, milestoneFlexTypeAtt,
											higestIntegerList, higestMilestoneCSIDMap);
							}
							//	System.out.println("--- skuName : "+skuName + tmpSetCSID);
								if(!FormatHelper.hasContent(tmpSetCSID)) {
									tmpSetCSID = setCSID;
 								}
                  
								//-- set SKUSourcingLink with either ProdCS or SKUCS
								eachSKUSourcingLink.setValue("scHighestMilestoneCS", tmpSetCSID);
							//	System.out.println("--- MMA setCSID for '"+skuName+"':"+tmpSetCSID);
								new LCSSourcingConfigLogic().saveSKUSourcingLink(eachSKUSourcingLink);                 
								
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static String fetchHighestMilestoneCS(Collection CSCol,
			FlexTypeAttribute milestoneFlexTypeAtt, List<Integer> higestIntegerList,
				Map higestMilestoneCSIDMap) throws WTException {
		String highestMilestoneCSID = "";
		LCSProductCostSheet eachProdCS = null;
		String milestone = null;
		Integer highestInteger = 0;
		
		Iterator prodCSColITR = CSCol.iterator();
		String costSheetType = "";
		Boolean isCheckApplicable = false;
		while(prodCSColITR.hasNext()) {
			eachProdCS = (LCSProductCostSheet)prodCSColITR.next();
			//9373 - Apply this functionality only for Reebok and Sparc Type - Start

			LOGGER.debug("costSheetType-------33333333----------------------"+costSheetType);
			costSheetType=eachProdCS.getFlexType().getFullName(true);

			isCheckApplicable= costSheetType.equalsIgnoreCase(SparcConstants.SPARC_COSTSHEET) ? true : costSheetType.contains("Reebok")? true : false;
			LOGGER.debug("isApplicable-----3333333333333------------"+isCheckApplicable);

			if(!isCheckApplicable){
				LOGGER.info(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "iterating prodSheet  "+eachProdCS);
				continue;
			}

			//9373 - Apply this functionality only for Reebok and Sparc Type - End

			milestone = (String)eachProdCS.getValue(SparcConstants.COSTSHEET_MILESTONE);
			if(FormatHelper.hasContent(milestone)) {
				milestone = milestoneFlexTypeAtt.getAttValueList().getValueFromValueStore(milestone, "_CUSTOM_scSecondaryKey");
				highestInteger = Integer.valueOf(milestone);
				if(!higestMilestoneCSIDMap.containsKey(highestInteger)) {
					higestIntegerList.add(highestInteger);
					higestMilestoneCSIDMap.put(highestInteger, FormatHelper.getNumericVersionIdFromObject(eachProdCS));
				}
			}
		}
		//System.out.println("--- higestMilestoneCSIDMap : "+higestMilestoneCSIDMap);
		if(higestIntegerList.size() > 0) {
			highestInteger = Collections.max(higestIntegerList);
			highestMilestoneCSID = (String)higestMilestoneCSIDMap.get(highestInteger);
		}
		
		return highestMilestoneCSID;
	}

	@SuppressWarnings("rawtypes")
	private static Collection getCSForProduct(LCSProductCostSheet prodCostSheet, LCSProduct productARev, LCSSeason season,
			LCSSourcingConfig sourcingConfig, String mode, String skuName) throws WTException {
		String milestoneAttColumn = prodCostSheet.getFlexType().getAttribute(COSTSHEET_MILESTONE_STATUS).getColumnName();
		String statusAttColumn = prodCostSheet.getFlexType().getAttribute(SparcConstants.COSTSHEET_STATUS).getColumnName();
		String prodARevId = FormatHelper.getNumericVersionIdFromObject(productARev);
		String seasonId = FormatHelper.getNumericVersionIdFromObject(season);
		String sourcingConfigId = FormatHelper.getNumericVersionIdFromObject(sourcingConfig);
		String likeCriteria = "%"+skuName+"|~*~|%";
		
		String splitEntry[] = ALLOWED_CSMILESTONE_ENTRY.split(",");
		Collection<String> allowedMilestoneCol = Arrays.asList(splitEntry);
		
		PreparedQueryStatement pqs = new PreparedQueryStatement();
		pqs.appendSelectColumn("LCSProductCostSheet", "branchIditerationInfo");
		pqs.appendSelectColumn("LCSProductCostSheet", "applicableColorNames");
		pqs.appendSelectColumn("LCSProductCostSheet", milestoneAttColumn);
		pqs.appendFromTable("LCSProductCostSheet");
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSProductCostSheet", "productARevId", prodARevId, Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSProductCostSheet", "seasonRevId", seasonId, Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSProductCostSheet", "sourcingConfigRevId", sourcingConfigId, Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSProductCostSheet", "latestiterationInfo", "1", Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSProductCostSheet", "statecheckoutInfo", "wrk", Criteria.NOT_EQUAL_TO));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSProductCostSheet", "whatif", "0", Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendInCriteria("LCSProductCostSheet", milestoneAttColumn, allowedMilestoneCol);
		if(FormatHelper.hasContent(skuName)) {
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria("LCSProductCostSheet", "applicableColorNames", likeCriteria, Criteria.LIKE));
		}else {
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria("LCSProductCostSheet", "applicableColorNames", "null", Criteria.IS_NULL));
		}
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSProductCostSheet", statusAttColumn, SparcConstants.COSTSHEET_STATUS_CONFIRMED_CC, Criteria.EQUALS));
		//-- exclusing Current Costsheet from Query
		String costsheetID = getCurrentCSBranchID(prodCostSheet, mode);
		
		//System.out.println("--- CostSheetID for "+mode+":"+costsheetID);
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSProductCostSheet", "branchIditerationInfo", costsheetID, Criteria.NOT_EQUAL_TO));
		
		//System.out.println("-- Costsheet Query : "+pqs.toString());
		SearchResults costSheetSearchResults = LCSQuery.runDirectQuery(pqs);
		Collection costSheetColl = LCSQuery.getObjectsFromResults(costSheetSearchResults, "VR:com.lcs.wc.sourcing.LCSProductCostSheet:", "LCSPRODUCTCOSTSHEET.BRANCHIDITERATIONINFO");
		
		return costSheetColl;
	}

	private static String getCurrentCSBranchID(LCSProductCostSheet prodCostSheet, String mode) throws WTException {
		String costsheetID = null;
		if("UPDATE".equalsIgnoreCase(mode)) {
			LCSProductCostSheet previousProdCS = (LCSProductCostSheet)VersionHelper.predecessorOf(prodCostSheet);
			costsheetID = FormatHelper.getNumericVersionIdFromObject(previousProdCS);
		}else if("CREATE".equalsIgnoreCase(mode)){
			costsheetID = FormatHelper.getNumericVersionIdFromObject(prodCostSheet);
		}
		return costsheetID;
	}
}
