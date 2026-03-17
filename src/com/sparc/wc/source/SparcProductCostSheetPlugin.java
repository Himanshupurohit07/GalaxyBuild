package com.sparc.wc.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.sourcing.LCSCostSheetLogic;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSKUSourcingLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.util.ACLHelper;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.util.SparcConstants;
import com.sparc.wc.util.SparcCostingUploadUtil;
import com.sparc.wc.util.SparcLogger;

import wt.fc.WTObject;
import wt.util.WTException;

public class SparcProductCostSheetPlugin {
	static SparcLogger logger = SparcLogger.getInstance();
	static {
		logger.getDebugValue(SparcConstants.INTEGRATION_DEBUG_CALL);
	}

	public static void checkforMileStoneMatchCreate(WTObject wtObject) throws LCSException {
		logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatchCreate", "---- SSP for COSTSHEET MILESTONE CHECK FROM CREATE ----");
		checkforMileStoneMatch(wtObject, "CREATE");
		logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatchCreate", "---- End of SSP for COSTSHEET MILESTONE CHECK FROM CREATE ----");
	}

	public static void checkforMileStoneMatchUpdate(WTObject wtObject) throws LCSException {
		logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatchUpdate", "---- SSP for COSTSHEET MILESTONE CHECK FROM UPDATE ----");
		checkforMileStoneMatch(wtObject, "UPDATE");
		logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatchUpdate", "---- End of SSP for COSTSHEET MILESTONE CHECK FROM UPDATE ----");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void checkforMileStoneMatch(WTObject wtObject, String mode) throws LCSException {
		logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "---- SSP for COSTSHEET MILESTONE CHECK ----");
		String currentMileStone = null;
		boolean throwError = false;
		Collection tempCurrentApplicableNames;
		Collection<String> currentApplicableNames = new ArrayList<String>();
		String currentMileStoneDisplay = null;
		String errorColorNames = null;
		String errorColors = null;
		String costSheetType ="";
		if(wtObject instanceof LCSProductCostSheet) {
			Set<String> milestoneSet = new HashSet<String> ();
		
			try {
				FlexType costSheetFlexType = FlexTypeCache.getFlexTypeFromPath(SparcConstants.SPARC_COSTSHEET);
				String milestoneAttColumn = costSheetFlexType.getAttribute(SparcConstants.COSTSHEET_MILESTONE).getColumnName();
				LCSProductCostSheet prodCostSheet = (LCSProductCostSheet)wtObject;
				if(!prodCostSheet.isWhatIf()) {
					currentMileStone = (String)prodCostSheet.getValue(SparcConstants.COSTSHEET_MILESTONE);
					//Changes for UAT Log 9076 - start
					costSheetType=prodCostSheet.getFlexType().getFullName(true);
					logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "----costSheetType----:"+costSheetType);
					if(!costSheetType.equalsIgnoreCase(SparcConstants.SPARC_COSTSHEET) && !costSheetType.contains("Reebok")) {
						logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "----Returning from Validation as Cost sheet type is not valid----:"+costSheetType);
						return;
					}
					//Changes for UAT Log 9076 - end
					if(FormatHelper.hasContent(currentMileStone)) {
						LCSSourcingConfigMaster scMaster = prodCostSheet.getSourcingConfigMaster();
						if(scMaster != null) {
							LCSSourcingConfig sourcingConfig = (LCSSourcingConfig)VersionHelper.latestIterationOf(scMaster);
							LCSProduct productARev = (LCSProduct)VersionHelper.getVersion(prodCostSheet.getProductMaster(), "A");
							LCSSeasonMaster seasonMaster = (LCSSeasonMaster)prodCostSheet.getSeasonMaster();
							tempCurrentApplicableNames = MOAHelper.getMOACollection(prodCostSheet.getApplicableColorNames());
							currentApplicableNames = getCollectionFromCurrenAndIterateCostsheets(tempCurrentApplicableNames, currentMileStone, scMaster, seasonMaster);
							logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "currentApplicableNames is ==>"+currentApplicableNames);
							currentMileStoneDisplay = prodCostSheet.getFlexType().getAttribute(SparcConstants.COSTSHEET_MILESTONE).getDisplayValue(prodCostSheet);
							logger.logDebug(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "currentMileStone ==>"+currentMileStone);
							
							if(seasonMaster != null) {
								LCSSeason season = (LCSSeason)VersionHelper.latestIterationOf(seasonMaster);
							
								
								Collection existingCostSheetsCol = getCostSheetsForProductCheckMilestone(prodCostSheet, productARev, season, sourcingConfig, mode, currentMileStone);
								logger.logDebug(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "--- existingCostSheetsCol : "+existingCostSheetsCol.size());
								Iterator itr = existingCostSheetsCol.iterator();
								
								while(itr.hasNext()) {
									FlexObject obj = (FlexObject)itr.next();
									LCSProductCostSheet prodSheet = (LCSProductCostSheet)LCSQuery.findObjectById(SparcConstants.PRODUCT_COSTSHEET_VR+obj.getString(SparcConstants.PRODUCT_COSTSHEET_OBJECT+"."+ SparcConstants.BRANCH_ITERATION));
									//Changes for UAT Log 9076 - start
									costSheetType=prodSheet.getFlexType().getFullName(true);
									logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "iterating prodSheet  "+prodSheet);
									if(!prodSheet.toString().equalsIgnoreCase(prodCostSheet.toString()) && (costSheetType.equalsIgnoreCase(SparcConstants.SPARC_COSTSHEET) || costSheetType.contains("Reebok"))){
										//Changes for UAT Log 9076 - end
										Collection colorColl = MOAHelper.getMOACollection(prodSheet.getApplicableColorNames());
										Set milestoneSettemp = getCollectionFromCurrenAndIterateCostsheets(colorColl, obj.getString(SparcConstants.PRODUCT_COSTSHEET_OBJECT+"."+milestoneAttColumn), prodSheet.getSourcingConfigMaster(), prodSheet.getSeasonMaster());
										milestoneSet.addAll(milestoneSettemp);
									}
								}
							}
						}	
					}
				}
			}catch(WTException e) {
				logger.logError(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "Error in SparcProductCostSheetPlugin"+e);				
			}
			logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "milestoneSet is "+milestoneSet);
			logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "currentApplicableNames is "+currentApplicableNames);
			Iterator itr3 = currentApplicableNames.iterator();
			while(itr3.hasNext()) {
				String tempColorName = (String)itr3.next();
				if(milestoneSet.contains(tempColorName)) {
					if(FormatHelper.hasContent(errorColorNames)) {
						errorColorNames = errorColorNames+","+tempColorName;
						String split[] = tempColorName.split("_");
						errorColors = errorColors + ","+split[0];
					}else {
						errorColorNames = tempColorName;
						String split[] = tempColorName.split("_");
						errorColors = split[0];
					}
					throwError = true;
				}
			}
			
			if(throwError) {
				throw new LCSException(errorColors+" Colorway with milestone status:'"+currentMileStoneDisplay +"' already exists for this Colorway. Choose different milestone...");
			}
		}
	}

	public static void checkforMileStoneMatch(WTObject wtObject, String mode,String currentMileStone) throws LCSException {
		logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "---- SSP for COSTSHEET MILESTONE CHECK ----");
		boolean throwError = false;
		Collection tempCurrentApplicableNames;
		Collection<String> currentApplicableNames = new ArrayList<String>();
		String errorColorNames = null;
		String errorColors = null;
		String costSheetType ="";
		String milStoneDisplay = currentMileStone;
		if(wtObject instanceof LCSProductCostSheet) {
			Set<String> milestoneSet = new HashSet<String> ();

			try {
				FlexType costSheetFlexType = FlexTypeCache.getFlexTypeFromPath(SparcConstants.SPARC_COSTSHEET);
				String milestoneAttColumn = costSheetFlexType.getAttribute(SparcConstants.COSTSHEET_MILESTONE).getColumnName();
				LCSProductCostSheet prodCostSheet = (LCSProductCostSheet)wtObject;
				if(!prodCostSheet.isWhatIf()) {
					//Changes for UAT Log 9076 - start
					currentMileStone =SparcCostingUploadUtil.getInternalNameForEnum(prodCostSheet.getFlexType(),"scMilestoneStatus",currentMileStone);

					costSheetType=prodCostSheet.getFlexType().getFullName(true);
					logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "----costSheetType----:"+costSheetType);
					if(!costSheetType.equalsIgnoreCase(SparcConstants.SPARC_COSTSHEET) && !costSheetType.contains("Reebok")) {
						logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "----Returning from Validation as Cost sheet type is not valid----:"+costSheetType);
						return;
					}
					//Changes for UAT Log 9076 - end
					if(FormatHelper.hasContent(currentMileStone)) {
						LCSSourcingConfigMaster scMaster = prodCostSheet.getSourcingConfigMaster();
						if(scMaster != null) {
							LCSSourcingConfig sourcingConfig = (LCSSourcingConfig)VersionHelper.latestIterationOf(scMaster);
							LCSProduct productARev = (LCSProduct)VersionHelper.getVersion(prodCostSheet.getProductMaster(), "A");
							LCSSeasonMaster seasonMaster = (LCSSeasonMaster)prodCostSheet.getSeasonMaster();
							tempCurrentApplicableNames = MOAHelper.getMOACollection(prodCostSheet.getApplicableColorNames());
							currentApplicableNames = getCollectionFromCurrenAndIterateCostsheets(tempCurrentApplicableNames, currentMileStone, scMaster, seasonMaster);
							logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "currentApplicableNames is ==>"+currentApplicableNames);
							//currentMileStoneDisplay = prodCostSheet.getFlexType().getAttribute(SparcConstants.COSTSHEET_MILESTONE).getDisplayValue(prodCostSheet);
							logger.logDebug(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "currentMileStone ==>"+currentMileStone);

							if(seasonMaster != null) {
								LCSSeason season = (LCSSeason)VersionHelper.latestIterationOf(seasonMaster);


								Collection existingCostSheetsCol = getCostSheetsForProductCheckMilestone(prodCostSheet, productARev, season, sourcingConfig, mode, currentMileStone);
								logger.logDebug(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "--- existingCostSheetsCol : "+existingCostSheetsCol.size());
								Iterator itr = existingCostSheetsCol.iterator();

								while(itr.hasNext()) {
									FlexObject obj = (FlexObject)itr.next();
									LCSProductCostSheet prodSheet = (LCSProductCostSheet)LCSQuery.findObjectById(SparcConstants.PRODUCT_COSTSHEET_VR+obj.getString(SparcConstants.PRODUCT_COSTSHEET_OBJECT+"."+ SparcConstants.BRANCH_ITERATION));
									//Changes for UAT Log 9076 - start
									costSheetType=prodSheet.getFlexType().getFullName(true);
									logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "iterating prodSheet  "+prodSheet);
									if(!prodSheet.toString().equalsIgnoreCase(prodCostSheet.toString()) && (costSheetType.equalsIgnoreCase(SparcConstants.SPARC_COSTSHEET) || costSheetType.contains("Reebok"))){
										//Changes for UAT Log 9076 - end
										Collection colorColl = MOAHelper.getMOACollection(prodSheet.getApplicableColorNames());
										Set milestoneSettemp = getCollectionFromCurrenAndIterateCostsheets(colorColl, obj.getString(SparcConstants.PRODUCT_COSTSHEET_OBJECT+"."+milestoneAttColumn), prodSheet.getSourcingConfigMaster(), prodSheet.getSeasonMaster());
										milestoneSet.addAll(milestoneSettemp);
									}
								}
							}
						}
					}
				}
			}catch(WTException e) {
				logger.logError(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "Error in SparcProductCostSheetPlugin"+e);
			}
			logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "milestoneSet is "+milestoneSet);
			logger.logInfo(SparcProductCostSheetPlugin.class.getName(), "checkforMileStoneMatch", "currentApplicableNames is "+currentApplicableNames);
			Iterator itr3 = currentApplicableNames.iterator();
			while(itr3.hasNext()) {
				String tempColorName = (String)itr3.next();
				if(milestoneSet.contains(tempColorName)) {
					if(FormatHelper.hasContent(errorColorNames)) {
						errorColorNames = errorColorNames+","+tempColorName;
						String split[] = tempColorName.split("_");
						errorColors = errorColors + ","+split[0];
					}else {
						errorColorNames = tempColorName;
						String split[] = tempColorName.split("_");
						errorColors = split[0];
					}
					throwError = true;
				}
			}

			if(throwError) {
				throw new LCSException(errorColors+" Colorway with milestone status:'"+milStoneDisplay +"' already exists for this Colorway. Choose different milestone...");
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static Set<String> getCollectionFromCurrenAndIterateCostsheets(Collection coll, String mileStone, LCSSourcingConfigMaster scMaster, LCSSeasonMaster seasonMaster) {
		Set<String> tempCol = new HashSet<String> ();
		try {
			if(coll.size() != 0) {
				Iterator itr1 = coll.iterator();
				while(itr1.hasNext()) {
					tempCol.add(itr1.next()+"_"+mileStone);
				}
			}else{
				//HIT324 fix

				Collection skuSourcingLinksCol = new LCSSourcingConfigQuery().getSkuSourcingLinks(scMaster, null, seasonMaster, true);
				Iterator skuLinksCollITR = skuSourcingLinksCol.iterator();
				while(skuLinksCollITR.hasNext()) {
					LCSSKUSourcingLink eachSKUSourcingLink = (LCSSKUSourcingLink)skuLinksCollITR.next();
					LCSSKU eachSKUARev = (LCSSKU)VersionHelper.getVersion(eachSKUSourcingLink.getSkuMaster(),"A");
					if(!eachSKUARev.isPlaceholder()) {
						tempCol.add(eachSKUSourcingLink.getSkuMaster().getName()+"_P_"+mileStone);
						//_P_ is used to different between the Colorways in Colorway CostSheet and Product Costsheet. 
					}
				}
			}
		}catch(WTException e) {
			logger.logError(SparcProductCostSheetPlugin.class.getName(), "getCollectionFromCurrenAndIterateCostsheets", "Error in SparcProductCostSheetPlugin"+e);				
		}
		return tempCol;
	}

	@SuppressWarnings("rawtypes")
	private static Collection getCostSheetsForProductCheckMilestone(LCSProductCostSheet prodCostSheet, LCSProduct productARev, LCSSeason season,
			LCSSourcingConfig sourcingConfig, String mode, String currentMileStone) throws WTException {
		FlexType sourcingFlexType = FlexTypeCache.getFlexTypeFromPath(SparcConstants.CONSTANT_SOURCING_NAME);
		
		String highestMilestone = sourcingFlexType.getAttribute(SparcConstants.HIGHEST_MILESTONE).getColumnName();
		
		logger.logDebug(SparcProductCostSheetPlugin.class.getName(), "getCostSheetsForProductCheckMilestone", "higestMilestone "+highestMilestone);
		// TODO Auto-generated method stub
		String milestoneAttColumn = prodCostSheet.getFlexType().getAttribute(SparcConstants.COSTSHEET_MILESTONE).getColumnName();
		String prodARevId = FormatHelper.getNumericVersionIdFromObject(productARev);
		String seasonId = FormatHelper.getNumericVersionIdFromObject(season);
		String sourcingConfigId = FormatHelper.getNumericVersionIdFromObject(sourcingConfig);

		PreparedQueryStatement pqs = new PreparedQueryStatement();
		pqs.appendSelectColumn(SparcConstants.PRODUCT_COSTSHEET_OBJECT, SparcConstants.BRANCH_ITERATION);
		pqs.appendSelectColumn(SparcConstants.PRODUCT_COSTSHEET_OBJECT, milestoneAttColumn);
		pqs.appendSelectColumn(SparcConstants.PRODUCT_COSTSHEET_OBJECT, "statecheckoutInfo");
		pqs.appendFromTable("LCSProductCostSheet");
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(SparcConstants.PRODUCT_COSTSHEET_OBJECT, SparcConstants.PROD_A_REV_ID, prodARevId, Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(SparcConstants.PRODUCT_COSTSHEET_OBJECT, SparcConstants.SEASON_A_REV_ID, seasonId, Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(SparcConstants.PRODUCT_COSTSHEET_OBJECT, "sourcingConfigRevId", sourcingConfigId, Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(SparcConstants.PRODUCT_COSTSHEET_OBJECT,  SparcConstants.LATEST_ITERATION_INFO, SparcConstants.ONE, Criteria.EQUALS));
		//pqs.appendAndIfNeeded();
		//pqs.appendCriteria(new Criteria("LCSProductCostSheet", milestoneAttColumn, currentMileStone, Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(SparcConstants.PRODUCT_COSTSHEET_OBJECT, "statecheckoutInfo","wrk", Criteria.NOT_EQUAL_TO));
		
		logger.logDebug(SparcProductCostSheetPlugin.class.getName(), "getCostSheetsForProductCheckMilestone", "prodCostSheet is "+prodCostSheet);
		if("UPDATE".equalsIgnoreCase(mode)) {
			LCSProductCostSheet previousProdCS = (LCSProductCostSheet)VersionHelper.predecessorOf(prodCostSheet);
			String prodCSBranchId = FormatHelper.getNumericVersionIdFromObject(previousProdCS);
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(SparcConstants.PRODUCT_COSTSHEET_OBJECT, SparcConstants.BRANCH_ITERATION, prodCSBranchId, Criteria.NOT_EQUAL_TO));
		}
		
		//	pqs.appendAndIfNeeded();
		//	pqs.appendCriteria(new Criteria(SparcConstants.PRODUCT_COSTSHEET_OBJECT, costsheetStatustAttColumn, SparcConstants.COSTSHEET_STATUS_CONFIRMED_CC, Criteria.EQUALS));
		
		logger.logDebug(SparcProductCostSheetPlugin.class.getName(), "getCostSheetsForProductCheckMilestone", "-- Costsheet Query : "+pqs.toString());
		Collection existingCostSheetsCol = (Collection) LCSQuery.runDirectQuery(pqs).getResults();
		return existingCostSheetsCol;
	}
	
	/**
	 * Defaulting Sent to S4 value while copy cost sheet on PRE_CREATE_PERSIST
	 * 
	 * @param wtObject
	 * @throws WTException
	 */
	public static void setCreateSentToS4ForNonAdmin(WTObject wtObject) throws WTException {
		setCostSheetValue(wtObject, "CREATE");
	}
	
	/**

	
	/**
	 * Defaulting Sent to S4 value while copy cost sheet on PRE_CREATE_PERSIST
	 * 
	 * @param wtObject
	 * @throws WTException
	 */
	public static void setCostSheetValue(WTObject wtObject, String mode) throws WTException {
		if(wtObject instanceof LCSProductCostSheet) {
			LCSProductCostSheet productCostSheet = (LCSProductCostSheet) wtObject;
			logger.logDebug(SparcProductCostSheetPlugin.class.getName(), "setCreateSentToS4ForNonAdmin", "-- Costsheet  : "+productCostSheet);
			if(productCostSheet.getCopiedFrom()!=null && ACLHelper.hasAdminAccess(productCostSheet)) {
				productCostSheet.setValue(SparcConstants.COSTSHEET_SENT_TO_S4, SparcConstants.BOOLEAN_NO);
				LCSCostSheetLogic.persist(productCostSheet, true);
			}
		}
			
	}

}
