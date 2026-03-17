package com.sparc.wc.integration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.text.DecimalFormat; 

import org.json.simple.JSONObject;

import com.lcs.wc.color.LCSColor;
import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSLogEntry;
import com.lcs.wc.foundation.LCSLogEntryLogic;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.moa.LCSMOATable;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.LCSSeasonSKULinkClientModel;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSKUSourcingLink;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourceToSeasonLinkClientModel;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.integration.pojo.SparcArticlePOJO;
import com.sparc.wc.integration.pojo.SparcPIRPOJO;
import com.sparc.wc.util.SparcConstants;
import com.sparc.wc.util.SparcLogger;

import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class SparcRestIntegrationHelper {
	private static String prodSecAttributes;
	private static String prodSeasonSecAttributes;
	private static String skuSecAttributes;
	private static String seasonSecondaryAttributes;
	private static String sourceSecondaryAttributes;
	private static String sourceseasonSecondaryAttributes;
	private static String allowedSeasons;
	private static String allowedLifeCycle;
	private static String allowedCostSheetMilstones;
	public static String costMilestone;
	public static String skuSeasonSortingKeys;
	String methodName;
	SparcLogger logger = SparcLogger.getInstance();
	Map<String, String> sourceCScombo = new HashMap<String, String>();
	static {
		prodSecAttributes= LCSProperties.get("Integration.attributes.ProductSecondaryKeys");
		skuSecAttributes= LCSProperties.get("Integration.attributes.SKUSeasonSecondaryKeys");
		prodSeasonSecAttributes= LCSProperties.get("Integration.attributes.ProductSeasonSecondaryKeys");
		seasonSecondaryAttributes= LCSProperties.get("Integration.attributes.seasonSecondaryKeys");
		sourceSecondaryAttributes= LCSProperties.get("Integration.attributes.soureSecondaryKeys");
		sourceseasonSecondaryAttributes= LCSProperties.get("Integration.attributes.soureSeasonSecondaryKeys");
		allowedLifeCycle = LCSProperties.get("Integration.attributes.allowedLifeCycle");
		allowedSeasons = LCSProperties.get("Integration.attributes.seasonsAllowed");
		allowedCostSheetMilstones = LCSProperties.get("Integration.attributes.costSheetMileStones");
//HIT 294 Fix
		skuSeasonSortingKeys = LCSProperties.get("Integration.attributes.skuSeason.sortingMultiListKeys");
	}

	public SearchResults executeQuery(boolean isArticle, String  pageNo, String fetchNoOfRows, boolean isQueryForRowCount, String objectID) {
		methodName = "executeQuery";
		logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName, " Start");
		logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "product Secondary Attributes is "+ prodSecAttributes);
		logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Sku Secondary Attributes is " + skuSecAttributes);
		logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Product Season Secondary Attributes is " + prodSeasonSecAttributes);
		logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Season Secondary Attributes is " + seasonSecondaryAttributes);
		logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Source Secondary Attributes is " + sourceSecondaryAttributes);
		logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Source To Season Secondary Attributes is " + sourceseasonSecondaryAttributes);
		logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Allowed LifeCycles is " + allowedLifeCycle);
		logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Allowed Seasons is " + allowedSeasons);
		logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Allowed CostSheet MileStones  is " + allowedCostSheetMilstones);
		SearchResults searchResults = null;
		try {
			FlexType productFlexType = FlexTypeCache.getFlexTypeFromPath(SparcConstants.CONSTANT_PRODUCT_NAME);
			FlexType seasonFlexType = FlexTypeCache.getFlexTypeFromPath(SparcConstants.CONSTANT_SEASON_NAME);
			FlexType costSheetFlexType = FlexTypeCache.getFlexTypeFromPath(SparcConstants.SPARC_COSTSHEET);
			FlexType sourcingFlexType = FlexTypeCache.getFlexTypeFromPath(SparcConstants.CONSTANT_SOURCING_NAME);
			String integrationFlag = sourcingFlexType.getAttribute(SparcConstants.INTEGRATION_FLAG).getColumnName();
			//String sourcingStatus = sourcingFlexType.getAttribute(SparcConstants.SOURCING_SOURCING_STATUS).getColumnName();
			String highestMilestone = sourcingFlexType.getAttribute(SparcConstants.HIGHEST_MILESTONE).getColumnName();
			String colorwayLifecycleAttCol = productFlexType.getAttribute(SparcConstants.SKU_SEASON_LIFECYCLE).getColumnName();
			String skuSeasonSentToS4 = productFlexType.getAttribute(SparcConstants.SKU_SEASON_SENT_TO_S4).getColumnName();
			String seasonType = seasonFlexType.getAttribute(SparcConstants.SEASON_SEASON_TYPE).getColumnName();
			String seasonName = seasonFlexType.getAttribute(SparcConstants.SEASON_NAME).getColumnName();
			String productNme = productFlexType.getAttribute(SparcConstants.PRODUCT_NAME).getColumnName();
			String skuName = productFlexType.getAttribute(SparcConstants.SKU_NAME).getColumnName();
			String sourceName = sourcingFlexType.getAttribute(SparcConstants.SOURCE_NAME).getColumnName();
			String csSentToS4 = costSheetFlexType.getAttribute(SparcConstants.COSTSHEET_SENT_TO_S4).getColumnName();
			String costStatus = costSheetFlexType.getAttribute(SparcConstants.COSTSHEET_STATUS).getColumnName();
			costMilestone = costSheetFlexType.getAttribute(SparcConstants.COSTSHEET_MILESTONE).getColumnName();
			String costSheetName = costSheetFlexType.getAttribute("name").getColumnName();

			String sourcingStatus = sourcingFlexType.getAttribute(SparcConstants.SOURCING_SOURCING_STATUS).getColumnName();
			String sourcingSentToS4 = sourcingFlexType.getAttribute(SparcConstants.SOURCING_SENT_TO_S4).getColumnName();

			PreparedQueryStatement preparedQueryStatement = new PreparedQueryStatement();

			preparedQueryStatement.appendSelectColumn(SparcConstants.SEASON_OBJECT, SparcConstants.BRANCH_ITERATION);
			preparedQueryStatement.appendSelectColumn(SparcConstants.PRODUCT_OBJECT, SparcConstants.BRANCH_ITERATION);
			preparedQueryStatement.appendSelectColumn(SparcConstants.SKU_OBJECT, SparcConstants.BRANCH_ITERATION);
			preparedQueryStatement.appendSelectColumn(SparcConstants.PRODUCT_SEASON_OBJECT, SparcConstants.IDA2A2);
			preparedQueryStatement.appendSelectColumn(SparcConstants.SKU_SEASON_OBJECT, SparcConstants.IDA2A2);
			preparedQueryStatement.appendSelectColumn(SparcConstants.SOURCING_OBJECT, SparcConstants.BRANCH_ITERATION);
			preparedQueryStatement.appendSelectColumn(SparcConstants.PRODUCT_COSTSHEET_OBJECT, SparcConstants.BRANCH_ITERATION);
			preparedQueryStatement.appendSelectColumn(SparcConstants.SEASON_OBJECT, seasonName);
			preparedQueryStatement.appendSelectColumn(SparcConstants.PRODUCT_OBJECT, productNme);
			preparedQueryStatement.appendSelectColumn(SparcConstants.SKU_OBJECT, skuName);
			preparedQueryStatement.appendSelectColumn(SparcConstants.SOURCING_OBJECT, sourceName);
			preparedQueryStatement.appendSelectColumn(SparcConstants.SOURCING_OBJECT, SparcConstants.BRANCH_ITERATION);
			preparedQueryStatement.appendSelectColumn(SparcConstants.PRODUCT_COSTSHEET_OBJECT, SparcConstants.BRANCH_ITERATION);

			preparedQueryStatement.appendFromTable(SparcConstants.SEASON_OBJECT);
			preparedQueryStatement.appendFromTable(SparcConstants.PRODUCT_OBJECT);
			preparedQueryStatement.appendFromTable(SparcConstants.SKU_OBJECT);
			preparedQueryStatement.appendFromTable(SparcConstants.PRODUCT_SEASON_OBJECT);
			preparedQueryStatement.appendFromTable(SparcConstants.SKU_SEASON_OBJECT);
			preparedQueryStatement.appendFromTable(SparcConstants.SOURCING_OBJECT);
			preparedQueryStatement.appendFromTable(SparcConstants.PRODUCT_COSTSHEET_OBJECT);
			preparedQueryStatement.appendJoin(SparcConstants.SKU_OBJECT, SparcConstants.PROD_A_REV_ID,  SparcConstants.PRODUCT_OBJECT, SparcConstants.BRANCH_ITERATION);
			preparedQueryStatement.appendJoin(SparcConstants.PRODUCT_SEASON_OBJECT, SparcConstants.PROD_A_REV_ID, SparcConstants.PRODUCT_OBJECT, SparcConstants.BRANCH_ITERATION);
			preparedQueryStatement.appendJoin(SparcConstants.PRODUCT_SEASON_OBJECT, SparcConstants.SEASON_A_REV_ID,SparcConstants.SEASON_OBJECT, SparcConstants.BRANCH_ITERATION);
			preparedQueryStatement.appendJoin(SparcConstants.SKU_SEASON_OBJECT, SparcConstants.SKU_A_REV_ID,  SparcConstants.SKU_OBJECT, SparcConstants.BRANCH_ITERATION);
			preparedQueryStatement.appendJoin(SparcConstants.SKU_SEASON_OBJECT,  SparcConstants.SEASON_A_REV_ID, SparcConstants.SEASON_OBJECT, SparcConstants.BRANCH_ITERATION);
			preparedQueryStatement.appendJoin(SparcConstants.PRODUCT_COSTSHEET_OBJECT, SparcConstants.PROD_A_REV_ID,SparcConstants.SKU_OBJECT, SparcConstants.PROD_A_REV_ID);
			preparedQueryStatement.appendJoin(SparcConstants.PRODUCT_COSTSHEET_OBJECT, SparcConstants.SOURCE_MASTER_ID, SparcConstants.SOURCING_OBJECT,  SparcConstants.IDA3_MASTER_REFERENCE);
			preparedQueryStatement.appendJoin(SparcConstants.PRODUCT_COSTSHEET_OBJECT, SparcConstants.SEASON_A_REV_ID, SparcConstants.SEASON_OBJECT, SparcConstants.BRANCH_ITERATION);

			Collection<String> seasonsAllowed = prepareCollectionFromPropertyEntry(allowedSeasons);
			preparedQueryStatement.appendAndIfNeeded();
			preparedQueryStatement.appendInCriteria(new QueryColumn(SparcConstants.SEASON_OBJECT, seasonType), seasonsAllowed);

			preparedQueryStatement.appendAndIfNeeded();
			preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.SEASON_OBJECT, SparcConstants.LATEST_ITERATION_INFO, SparcConstants.ONE, Criteria.EQUALS));
			preparedQueryStatement.appendAndIfNeeded();
			preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.PRODUCT_OBJECT, SparcConstants.LATEST_ITERATION_INFO, SparcConstants.ONE, Criteria.EQUALS));
			preparedQueryStatement.appendAndIfNeeded();
			preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.SKU_OBJECT, SparcConstants.LATEST_ITERATION_INFO, SparcConstants.ONE, Criteria.EQUALS));
			preparedQueryStatement.appendAndIfNeeded();
			preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.PRODUCT_SEASON_OBJECT, SparcConstants.EFFECT_LATEST, SparcConstants.ONE, Criteria.EQUALS));
			preparedQueryStatement.appendAndIfNeeded();
			preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.SKU_SEASON_OBJECT, SparcConstants.EFFECT_LATEST, SparcConstants.ONE, Criteria.EQUALS));
			preparedQueryStatement.appendAndIfNeeded();
			preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.SOURCING_OBJECT, SparcConstants.LATEST_ITERATION_INFO, SparcConstants.ONE, Criteria.EQUALS));
			preparedQueryStatement.appendAndIfNeeded();
			preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.PRODUCT_COSTSHEET_OBJECT, SparcConstants.LATEST_ITERATION_INFO, SparcConstants.ONE, Criteria.EQUALS));
			preparedQueryStatement.appendAndIfNeeded();
			preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.PRODUCT_SEASON_OBJECT, SparcConstants.SEASON_REMOVED, SparcConstants.ONE, Criteria.NOT_EQUAL_TO));
			preparedQueryStatement.appendAndIfNeeded();
			preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.SKU_SEASON_OBJECT, SparcConstants.SEASON_REMOVED, SparcConstants.ONE, Criteria.NOT_EQUAL_TO));

			if(isArticle) {
				preparedQueryStatement.appendFromTable(SparcConstants.SOURCING_MASTER_OBJECT);
				preparedQueryStatement.appendFromTable(SparcConstants.SOURCE_TO_SEASONLINK_MASTER_OBJECT);
				preparedQueryStatement.appendJoin(SparcConstants.SOURCING_MASTER_OBJECT, SparcConstants.IDA2A2, SparcConstants.SOURCING_OBJECT, SparcConstants.IDA3_MASTER_REFERENCE);

				preparedQueryStatement.appendAndIfNeeded();
				preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.SKU_SEASON_OBJECT, skuSeasonSentToS4, SparcConstants.BOOLEAN_NO, Criteria.EQUALS));
				//preparedQueryStatement.appendAndIfNeeded();
				//preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.PRODUCT_SEASON_OBJECT, "", "scPlanned", Criteria.NOT_EQUAL_TO));
				preparedQueryStatement.appendAndIfNeeded();
				preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.SKU_SEASON_OBJECT, colorwayLifecycleAttCol, "scPlanned", Criteria.NOT_EQUAL_TO));
				
				//preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.SOURCING_MASTER_OBJECT, "primarysource", SparcConstants.ONE, Criteria.EQUALS));
				//Changing the logic from Primary Source(Product) to Primary Source (Season)
				//AND LCSSourceToSeasonLinkMaster.primarySTSL=1
				//AND LCSSourceToSeasonLinkMaster.idA3A6 = LCSSourcingConfigMaster.idA2A2
				//AND LCSSourceToSeasonLinkMaster.idA3B6 = LCSSeason.idA3masterReference
				
				preparedQueryStatement.appendAndIfNeeded();
				preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.SOURCE_TO_SEASONLINK_MASTER_OBJECT,"primarySTSL",SparcConstants.ONE, Criteria.EQUALS));
				
				preparedQueryStatement.appendAndIfNeeded();
				preparedQueryStatement.appendJoin(SparcConstants.SOURCE_TO_SEASONLINK_MASTER_OBJECT, 
				SparcConstants.IDA3A6, "LCSSourcingConfigMaster", SparcConstants.IDA2A2);
				
				preparedQueryStatement.appendAndIfNeeded();
				preparedQueryStatement.appendJoin(SparcConstants.SOURCE_TO_SEASONLINK_MASTER_OBJECT, 
				SparcConstants.IDA3B6, SparcConstants.SEASON_OBJECT, "idA3masterReference");
				
				preparedQueryStatement.appendAndIfNeeded();
				preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.PRODUCT_COSTSHEET_OBJECT, "primaryCostSheet", SparcConstants.ONE, Criteria.EQUALS));
			}else {
				Collection<String> milestonesAllowed = prepareCollectionFromPropertyEntry(allowedCostSheetMilstones);

				preparedQueryStatement.appendSelectColumn(SparcConstants.PRODUCT_COSTSHEET_OBJECT,SparcConstants.SEASON_A_REV_ID);
				preparedQueryStatement.appendSelectColumn(SparcConstants.PRODUCT_COSTSHEET_OBJECT, costSheetName);
				preparedQueryStatement.appendSelectColumn(SparcConstants.PRODUCT_COSTSHEET_OBJECT, costMilestone);
				preparedQueryStatement.appendSelectColumn(SparcConstants.PRODUCT_COSTSHEET_OBJECT, csSentToS4);
				preparedQueryStatement.appendSelectColumn(SparcConstants.SOURCE_TO_SEASON_OBJECT,sourcingStatus);
				preparedQueryStatement.appendSelectColumn(SparcConstants.SOURCE_TO_SEASON_OBJECT,sourcingSentToS4);
				preparedQueryStatement.appendFromTable(SparcConstants.SOURCE_TO_SEASON_OBJECT);
				preparedQueryStatement.appendFromTable(SparcConstants.SKU_SOURCING_LINK_OBJECT);
				preparedQueryStatement.appendAndIfNeeded();

				preparedQueryStatement.appendSelectColumn(SparcConstants.SOURCE_TO_SEASON_OBJECT, SparcConstants.BRANCH_ITERATION);
				preparedQueryStatement.appendJoin(SparcConstants.SOURCE_TO_SEASON_OBJECT, "sourcingConfigMasterId", SparcConstants.SOURCING_OBJECT, SparcConstants.IDA3_MASTER_REFERENCE);
				preparedQueryStatement.appendJoin(SparcConstants.SEASON_OBJECT,SparcConstants.IDA3_MASTER_REFERENCE,SparcConstants.SOURCE_TO_SEASON_OBJECT,"seasonMasterId");
				preparedQueryStatement.appendJoin(SparcConstants.PRODUCT_COSTSHEET_OBJECT, SparcConstants.BRANCH_ITERATION, SparcConstants.SKU_SOURCING_LINK_OBJECT, highestMilestone);
				preparedQueryStatement.appendJoin(SparcConstants.SKU_SOURCING_LINK_OBJECT, "idA3B5", SparcConstants.SOURCING_OBJECT, SparcConstants.IDA3_MASTER_REFERENCE);
				preparedQueryStatement.appendJoin(SparcConstants.SKU_SOURCING_LINK_OBJECT, "idA3A5", SparcConstants.SKU_OBJECT, SparcConstants.IDA3_MASTER_REFERENCE);
				Collection<String> allowedLifeCycles = prepareCollectionFromPropertyEntry(allowedLifeCycle);
				preparedQueryStatement.appendAndIfNeeded();

				preparedQueryStatement.appendInCriteria(new QueryColumn(SparcConstants.PRODUCT_COSTSHEET_OBJECT, costMilestone), milestonesAllowed);
				preparedQueryStatement.appendAndIfNeeded();
				preparedQueryStatement.appendInCriteria(new QueryColumn(SparcConstants.SKU_SEASON_OBJECT, colorwayLifecycleAttCol), allowedLifeCycles);
				preparedQueryStatement.appendAndIfNeeded();
				preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.SOURCE_TO_SEASON_OBJECT, SparcConstants.LATEST_ITERATION_INFO, SparcConstants.ONE, Criteria.EQUALS));
				preparedQueryStatement.appendAndIfNeeded();
				preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.SKU_SOURCING_LINK_OBJECT, "Active", SparcConstants.ONE, Criteria.EQUALS));
				preparedQueryStatement.appendAndIfNeeded();
				preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.SKU_SEASON_OBJECT, skuSeasonSentToS4, SparcConstants.BOOLEAN_YES, Criteria.EQUALS));
				preparedQueryStatement.appendAndIfNeeded();
				preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.PRODUCT_COSTSHEET_OBJECT, csSentToS4, SparcConstants.BOOLEAN_NO, Criteria.EQUALS));
				preparedQueryStatement.appendAndIfNeeded();
				preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.SKU_SOURCING_LINK_OBJECT, integrationFlag, SparcConstants.BOOLEAN_YES, Criteria.NOT_EQUAL_TO));
				preparedQueryStatement.appendAndIfNeeded();
				preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.PRODUCT_COSTSHEET_OBJECT, costStatus, SparcConstants.COSTSHEET_STATUS_CONFIRMED_CC, Criteria.EQUALS));
				preparedQueryStatement.appendAndIfNeeded();
				preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.PRODUCT_COSTSHEET_OBJECT, "whatIf", SparcConstants.ONE, Criteria.NOT_EQUAL_TO));
				
				if(FormatHelper.hasContent(objectID)){
					logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "STEP1 ****: objectID for single costsheet > " + objectID);
					
					preparedQueryStatement.appendAndIfNeeded();
					preparedQueryStatement.appendCriteria(new Criteria(SparcConstants.PRODUCT_COSTSHEET_OBJECT, SparcConstants.BRANCH_ITERATION, objectID, Criteria.EQUALS));
				}
			}
			
			
			logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName,"isQueryForRowCount  is "+isQueryForRowCount);
			if(!isQueryForRowCount) {
				preparedQueryStatement.setFromIndex(1); // setting the value always from 1 to fetch number of rows as on each call the data is updated  
					
					logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName,"fetchNoOfRows  is "+fetchNoOfRows);
				preparedQueryStatement.setToIndex(Integer.parseInt(fetchNoOfRows));

			}
			preparedQueryStatement.appendSortBy(new QueryColumn(SparcConstants.PRODUCT_OBJECT, SparcConstants.BRANCH_ITERATION));

			logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "QueryStatement " + preparedQueryStatement.toString());
			searchResults = LCSQuery.runDirectQuery(preparedQueryStatement);

		}catch(WTException e) {
			logger.logError(SparcRestIntegrationHelper.class.getName(), methodName, "Exception is  " + e);
		}
		logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName, " End");
		return searchResults;
	}

	/**
	 * @param propertyEntry
	 * @return
	 */
	public Collection<String> prepareCollectionFromPropertyEntry(String propertyEntry){
		Collection<String> returnCollection = new ArrayList<String>();
		if(FormatHelper.hasContent(propertyEntry)) {
			String splitEntry[] = propertyEntry.split(",");
			returnCollection = Arrays.asList(splitEntry);
		}else {
			logger.logError(SparcRestIntegrationHelper.class.getName(), "prepareCollectionFromPropertyEntry", propertyEntry+" is missing");
		}
		return returnCollection;
	}

	/**
	 * @param wtObject
	 * @param key
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public String getDisplayValue(WTObject wtObject, String key) throws Exception{
		methodName = "getDisplayValue";
		logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Start" +wtObject);
		String displayValue = "";
		
		if(wtObject instanceof LCSProduct) {
			LCSProduct prod = (LCSProduct)wtObject;
			if(key.equalsIgnoreCase(SparcConstants.PRODUCT_OUTSOLE)) {
				LCSMaterial material = (LCSMaterial)prod.getValue(key);
				if(material != null && material.getValue(SparcConstants.MATERIAL_NUMBER) != null) {
					displayValue = String.valueOf(material.getValue(SparcConstants.MATERIAL_NUMBER));
				}
			}else if(prodSecAttributes.contains(key)) {
				displayValue = prod.getFlexType().getAttribute(key).getAttValueList().getValueFromValueStore((String)prod.getValue(key), SparcConstants.CUSTOM_SECONDARY_KEY);
				logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Secondary Product Display for key "+ key + " and Value is " + displayValue);
			}else {
				displayValue = prod.getFlexType().getAttribute(key).getDisplayValue(prod);
				logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Product Display for key "+ key + " and Value is " + displayValue);
			}
		}else if(wtObject instanceof LCSProductSeasonLink) {
			LCSProductSeasonLink prodSeas = (LCSProductSeasonLink)wtObject; 
			if(prodSeasonSecAttributes.contains(key)) {
				String attValues = (String)prodSeas.getValue(key);
				if(FormatHelper.hasContent(attValues) && attValues.contains(MOAHelper.DELIM)) {
					StringBuffer buff = new StringBuffer();
					for(String singleValue : attValues.split("\\|\\~\\*\\~\\|")) {
						String tempValue1 = prodSeas.getFlexType().getAttribute(key).getAttValueList().getValueFromValueStore(singleValue, SparcConstants.CUSTOM_SECONDARY_KEY);
						buff.append(tempValue1).append(",");
					}
					displayValue = buff.toString();
				}else {
					displayValue = prodSeas.getFlexType().getAttribute(key).getAttValueList().getValueFromValueStore((String)prodSeas.getValue(key), SparcConstants.CUSTOM_SECONDARY_KEY);
				}
				logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Secondary Product Season Display for key "+ key + " and Value is " + displayValue);
			}else {
				displayValue = prodSeas.getFlexType().getAttribute(key).getDisplayValue(prodSeas);
				logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Product Season Display for key "+ key + " and Value is " + displayValue);
			}
		}else if(wtObject instanceof LCSColor) {
			LCSColor color = (LCSColor)wtObject;
			displayValue = color.getFlexType().getAttribute(key).getDisplayValue(color);
			logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Color Display for key "+ key + " and Value is " + displayValue);
		}else if(wtObject instanceof LCSSKUSeasonLink) {
			LCSSKUSeasonLink skuSeas = (LCSSKUSeasonLink)wtObject; 
			logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "skuSecAttributes -- "+skuSecAttributes);
			if(skuSecAttributes.contains(key) && skuSeas != null) {
				String attValues = (String)skuSeas.getValue(key);
				// HIT 294 Fix start
				if(FormatHelper.hasContent(attValues) && attValues.contains(MOAHelper.DELIM)) {
					StringBuffer buff = new StringBuffer();
					for(String singleValue : attValues.split("\\|\\~\\*\\~\\|")) {
						String tempValue1 = skuSeas.getFlexType().getAttribute(key).getAttValueList().getValueFromValueStore(singleValue, SparcConstants.CUSTOM_SECONDARY_KEY);
						buff.append(tempValue1).append(",");
					}
					if(skuSeasonSortingKeys != null && skuSeasonSortingKeys.contains(key)) {
						String[] tempArray = buff.toString().split(",");
						Arrays.sort(tempArray);
						displayValue = String.join(",", tempArray);
					}else {// HIT 294 Fix End
						displayValue = buff.toString();
					}
				}else {
					displayValue = skuSeas.getFlexType().getAttribute(key).getAttValueList().getValueFromValueStore((String)skuSeas.getValue(key), SparcConstants.CUSTOM_SECONDARY_KEY);
				}
				logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "If Secondary SKU Season Display for key "+ key + " and Value is " + displayValue);
			}else {
				String attributeType = skuSeas.getFlexType().getAttribute(key).getAttVariableType();
				if("Currency".equalsIgnoreCase(attributeType) || "Integer".equalsIgnoreCase(attributeType)){
					displayValue = String.valueOf(skuSeas.getValue(key));
					logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "SKU Season Value for key "+ key + " and Value is " + displayValue);
				}else {
					displayValue = skuSeas.getFlexType().getAttribute(key).getDisplayValue(skuSeas);
					logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "SKU Season Display for key "+ key + " and Value is " + displayValue);
				}
			}
		}else if(wtObject instanceof LCSSeason) {
			LCSSeason season = (LCSSeason)wtObject; 
			if(seasonSecondaryAttributes.contains(key)) {
				displayValue = season.getFlexType().getAttribute(key).getAttValueList().getValueFromValueStore((String)season.getValue(key), SparcConstants.CUSTOM_SECONDARY_KEY);
				logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Secondary Season Display for key "+ key + " and Value is " + displayValue);
			}else {
				displayValue = season.getFlexType().getAttribute(key).getDisplayValue(season);
				logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Season Display for key "+ key + " and Value is " + displayValue);
			}
		}else if(wtObject instanceof LCSProductCostSheet) {
			LCSProductCostSheet prodCS = (LCSProductCostSheet)wtObject; 
			if(key.equalsIgnoreCase(SparcConstants.COSTSHEET_HTS_CODE)) {
				LCSMOATable moa = (LCSMOATable) prodCS.getValue("scHTSClassificaiton");
				Collection moaColl = moa.getRows();
				Iterator itr = moaColl.iterator();
				String tempValue = null; 
				while(itr.hasNext()) {
					FlexObject obj = (FlexObject)itr.next();
					String htsCodeID = (String)obj.get(key);
					LCSLifecycleManaged managed = (LCSLifecycleManaged)LCSQuery.findObjectById("OR:com.lcs.wc.foundation.LCSLifecycleManaged:"+htsCodeID);
					if(FormatHelper.hasContent(tempValue)) {
						tempValue = tempValue + MOAHelper.DELIM + (String)managed.getValue(key);
					} else {
						tempValue = (String)managed.getValue(key);
					}
				}
				displayValue = tempValue;
			}else if(key.equalsIgnoreCase(SparcConstants.COSTSHEET_INCO_TERMS_LOCATION)) {
				LCSLifecycleManaged lcmanage = (LCSLifecycleManaged)prodCS.getValue(key);
				if(lcmanage != null) {
					displayValue = (String)lcmanage.getValue("sclocode");
					logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Secondary Inco term location or key "+ lcmanage.getValue("sclocode"));
				}
			}else {
				String attributeType = prodCS.getFlexType().getAttribute(key).getAttVariableType();
				if("currency".equalsIgnoreCase(attributeType)){
					displayValue = String.valueOf(prodCS.getValue(key));
					logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "CostSheet Value for key "+ key + " and Value is " + displayValue);
					
					if(FormatHelper.hasContent(displayValue)){
						double roundOff = (double) Math.round(Double.valueOf(displayValue) * 100.0) / 100.0;
						logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "roundOff: "+ roundOff);
						
						displayValue = Double.toString(roundOff);
					}
					
				}else {
					displayValue = prodCS.getFlexType().getAttribute(key).getDisplayValue(prodCS);
					logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Product CostSheet Display for key "+ key + " and Value is " + displayValue);
				}
			}		
		}else if(wtObject instanceof LCSSKU) {
			LCSSKU sku = (LCSSKU)wtObject; 
			if(skuSecAttributes.contains(key)) {
				displayValue = sku.getFlexType().getAttribute(key).getAttValueList().getValueFromValueStore((String)sku.getValue(key), SparcConstants.CUSTOM_SECONDARY_KEY);
				logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Secondary Colorway Display for key "+ key + " and Value is " + displayValue);
			}else {
				if(key.equalsIgnoreCase(SparcConstants.SKU_NUMBER)) {
					displayValue = String.format("%018d", Integer.valueOf(sku.getFlexType().getAttribute(key).getDisplayValue(sku)));
				} else if(key.equalsIgnoreCase(SparcConstants.SKU_NRF_CODE)) {
					displayValue = String.format("%03d", Integer.valueOf(sku.getFlexType().getAttribute(key).getDisplayValue(sku)));
				}else {
					displayValue = sku.getFlexType().getAttribute(key).getDisplayValue(sku);
					logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Colorway Display for key "+ key + " and Value is " + sku.getValue(key));
				}
			}	
		}else if(wtObject instanceof LCSSourceToSeasonLink) {
			LCSSourceToSeasonLink sourceSeasonLink = (LCSSourceToSeasonLink)wtObject;
			if(sourceseasonSecondaryAttributes.contains(key)) {
				displayValue = sourceSeasonLink.getFlexType().getAttribute(key).getAttValueList().getValueFromValueStore((String)sourceSeasonLink.getValue(key), SparcConstants.CUSTOM_SECONDARY_KEY);
				logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Secondary Colorway Display for key "+ key + " and Value is " + displayValue);
			}else{
				String attributeType = sourceSeasonLink.getFlexType().getAttribute(key).getAttVariableType();
				if("float".equalsIgnoreCase(attributeType)){
					displayValue = String.valueOf(sourceSeasonLink.getValue(key));
					logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Source to Season  Value for key "+ key + " and Value is " + displayValue);
				}else {
					displayValue = sourceSeasonLink.getFlexType().getAttribute(key).getDisplayValue(sourceSeasonLink);
					logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Source to Season  Display for key "+ key + " and Value is " + displayValue);
				}
			}
		}else if(wtObject instanceof LCSSourcingConfig) {
			LCSSourcingConfig sourcing = (LCSSourcingConfig)wtObject; 
			if(key.equalsIgnoreCase(SparcConstants.SOURCING_FACTORY)) {
				LCSSupplier supplier = (LCSSupplier)sourcing.getValue(key);
				if(supplier != null) {
					displayValue = 	(String)supplier.getValue(SparcConstants.SAP_VENDOR_NUMBER);
				}
			}else if(key.equalsIgnoreCase(SparcConstants.SOURCING_FACTORY_COUNTRY_ORIGIN)) {
				LCSCountry country = (LCSCountry)sourcing.getValue(key);
				if(country != null) {
					displayValue = (String)country.getValue(SparcConstants.COUNTRY_CODE);
				}
			} else if(sourceSecondaryAttributes.contains(key)) {
				displayValue = sourcing.getFlexType().getAttribute(key).getAttValueList().getValueFromValueStore((String)sourcing.getValue(key), SparcConstants.CUSTOM_SECONDARY_KEY);
				logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "SourcingSecondary Colorway Display for key "+ key + " and Value is " + displayValue);
			} else {
				displayValue = sourcing.getFlexType().getAttribute(key).getDisplayValue(sourcing);
				logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Sourcing Configuration Display for key "+ key + " and Value is " + displayValue);
			}
		}
		
		if(displayValue == null) {
			displayValue = "";
		}
		logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "End Display value returned is "+displayValue);
		return removeCommaIfItsInEnd(displayValue);	
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JSONObject sendFinalJSON(String integrationType, String pageNo, String fetchNoOfRows, String totalNoOfRows, String objectName, boolean executeArticle, String objectID) {
		methodName = "sendFinalJSON";
		logger.logDebug(SparcRestIntegrationArticleHelper.class.getName(), methodName, "objectID "+ integrationType +" : " + objectID);
		logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName, integrationType+"Start");
		String apiRequest = "pageNo=" +pageNo + ",fetchNoOfRows=" +fetchNoOfRows + ",totalNoOfRows=" +totalNoOfRows;
		logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName, "API Request is " + apiRequest);
		JSONObject masterPOJO = new JSONObject();
		Map<String, Object> queryCol = new HashMap<String, Object>();
		Map<String , Object> queriesList = new HashMap<String, Object>();
		String apiInput = null;
		int ignoreRecords = 0;
		List<?> pojoData = new ArrayList();
		List logEntry = new ArrayList();
		List successEntries = new ArrayList();
		List failedEntries = new ArrayList();
		SearchResults firstTimeCallCollection = executeQuery(executeArticle, pageNo, fetchNoOfRows, false, objectID);
		if(firstTimeCallCollection != null ) {
			if(!FormatHelper.hasContent(totalNoOfRows)) {
				totalNoOfRows = String.valueOf(firstTimeCallCollection.getResultsFound());
			}
			methodName = "sendFinalJSON";
			logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName, integrationType + "STEP2 ****: Query Result is "+totalNoOfRows);		
			queriesList = fetchIntegrationAttributes(firstTimeCallCollection.getResults(), integrationType, apiRequest);
			for (Map.Entry<String, Object> entry : queriesList.entrySet()) {
				if(entry.getKey().equalsIgnoreCase("pojo")) {
					pojoData = (List<?>) entry.getValue();
				}else if(entry.getKey().equalsIgnoreCase("SuccessEntries")) {
					successEntries = (ArrayList)entry.getValue();
				}else if (entry.getKey().equalsIgnoreCase("apiInput")){
					apiInput = (String)entry.getValue();
				}else if (entry.getKey().equalsIgnoreCase("ignoreRecords")){
					ignoreRecords = (int)entry.getValue();
				}else  {
					failedEntries = (ArrayList)entry.getValue();
				}
			}
		}
		queryCol.put("totalNoOfRecordsPerCall", pojoData.size());

		if(ignoreRecords > 0) {
			queryCol.put("totalNoOfRecord", Integer.parseInt(totalNoOfRows) - ignoreRecords);
			totalNoOfRows =String.valueOf(Integer.parseInt(totalNoOfRows) - ignoreRecords);
		}else {
			queryCol.put("totalNoOfRecord", totalNoOfRows);	
		}
		int noOfPages = 0;
		if(FormatHelper.hasContent(totalNoOfRows)) {
			noOfPages = Integer.valueOf(totalNoOfRows)/Integer.valueOf(fetchNoOfRows);
		}
		if(Integer.valueOf(fetchNoOfRows)*noOfPages == Integer.parseInt(totalNoOfRows)) {
			queryCol.put("totalNoOfPages", noOfPages);	
		}else {
			noOfPages = noOfPages + 1;
			queryCol.put("totalNoOfPages", noOfPages);
		}
		masterPOJO.putAll(queryCol);
		masterPOJO.put(objectName, pojoData);
		logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName, "Pojo Data is "+ pojoData);
		logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName, "Pojo Data Size is "+ pojoData.size());
		logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName, "Total No of Pages is "+ noOfPages);
		logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName, "Total No Row per Page is "+ totalNoOfRows);
		if(logEntry != null && logEntry.size() > 0) {
			logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName, "logged the Entry with data sent in payload");
		}else {
			if(logEntry != null) {
				logEntry.add("Response sent with no data to send in payload");
				logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName, "logged the Entry with Response sent with no data to send in payload");
			}
		}
		createLogEntry(integrationType, successEntries, failedEntries, Integer.parseInt(totalNoOfRows), Integer.valueOf(pageNo), pojoData.size(), apiInput);
		logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName, integrationType + " End");
		return masterPOJO;
	}	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> fetchIntegrationAttributes(Collection prodSeasonCol, String integrationType, String apiRequestInputs) {
		methodName = "fetchIntegrationAttributes ";

		List<String> logEntryValues = new ArrayList<String>();
		logger.logDebug(SparcRestIntegrationArticleHelper.class.getName(), methodName, integrationType +" Start");
		List<Object> listProductPojo = new ArrayList<Object>();
		Map<String, Object> integrationPojoList = new HashMap<String, Object>();
		List<String> successfulEntries = new ArrayList<String>();
		List<String> failedEntries = new ArrayList<String>();
		Iterator<FlexObject> prodSeasonColITR = prodSeasonCol.iterator();
		FlexObject fob = null;
		String seasonID = null;
		String prodARevID = null;
		String skuID = null;
		LCSProductCostSheet productCostSheet=null;
		//-- Fetching Data from Object
		seasonID = null;
		prodARevID = null;
		LCSProduct prodARev = null;
		LCSSeason season = null;
		LCSSKU skuARev = null;
		LCSProductSeasonLink prodSPL = null;
		LCSSKUSeasonLink skuSPL = null;
		LCSSourceToSeasonLink sourceToSeasonLink = null;
		String skusourceID = null;
		String sourceID = null;
		int shouldNotBePartOfQuery = 0;

		//-- Collecting all Product, Season and respective SKUs
		while (prodSeasonColITR.hasNext()) {
			fob = prodSeasonColITR.next();
			logger.logDebug(SparcRestIntegrationArticleHelper.class.getName(), methodName, "STEP3 ****: FlexObject for "+ integrationType +" : " + fob);
			seasonID = fob.getString(SparcConstants.SEASON_OBJECT + "." + SparcConstants.BRANCH_ITERATION);
			prodARevID = fob.getString(SparcConstants.PRODUCT_OBJECT + "." + SparcConstants.BRANCH_ITERATION);
			skuID = fob.getString(SparcConstants.SKU_OBJECT + "." + SparcConstants.BRANCH_ITERATION);
			if(integrationType.equalsIgnoreCase("Article")) {
				
				SparcRestIntegrationArticleHelper helper = new SparcRestIntegrationArticleHelper();
				try {
					prodARev = (LCSProduct)LCSQuery.findObjectById(SparcConstants.PRODUCT_VR + prodARevID);
					season = (LCSSeason)LCSQuery.findObjectById(SparcConstants.SEASON_VR + seasonID);
					prodSPL = (LCSProductSeasonLink)LCSSeasonQuery.findSeasonProductLink(prodARev, season);
					//-- Creating Product and prodSeason POJO.
					skuARev = (LCSSKU)LCSQuery.findObjectById(SparcConstants.SKU_VR + skuID);
					skuSPL = (LCSSKUSeasonLink)LCSSeasonQuery.findSeasonProductLink(skuARev, season);
					//-- Creating SKU and skuSeason POJO.						

					if(skuSPL != null) {
						try {
							
							String prodCS = fob.getString(SparcConstants.PRODUCT_COSTSHEET_OBJECT + "." + SparcConstants.BRANCH_ITERATION);
							productCostSheet = (LCSProductCostSheet)LCSQuery.findObjectById(SparcConstants.PRODUCT_COSTSHEET_VR + prodCS);
							SparcArticlePOJO productPOJO = helper.getProductSeasonData(prodARev, prodSPL, season, productCostSheet, skuARev, skuSPL);
							listProductPojo.add(productPOJO);
							successfulEntries.add(prodARev.getName()+"_"+skuARev.getMaster().getName());
							//moved lines below because:Integration failed with exception 
							LCSSeasonSKULinkClientModel skuClientModel = new LCSSeasonSKULinkClientModel();
							skuClientModel.load(skuSPL);
							skuClientModel.setValue(SparcConstants.SKU_SEASON_SENT_TO_S4, SparcConstants.BOOLEAN_YES);
							skuClientModel.save();
						}catch(WTException | WTPropertyVetoException e) {
							logger.logError(SparcRestIntegrationArticleHelper.class.getName(), methodName, " Save Data Exception is " + e);
							e.printStackTrace();
							failedEntries.add(prodARev.getName()+"_"+skuARev.getMaster().getName());		
						}
						catch(Exception e){
							logger.logError(SparcRestIntegrationArticleHelper.class.getName(), methodName, " Save Data Exception is " + e);
							e.printStackTrace();
							failedEntries.add(prodARev.getName()+"_"+skuARev.getMaster().getName());	
							
						}
					}
				}catch(WTException e) {
					logger.logError(SparcRestIntegrationArticleHelper.class.getName(), methodName, " WT Property Exception is " + e);
					e.printStackTrace();
				}

			}else {
				try {
					FlexType sourcingFlexType = FlexTypeCache.getFlexTypeFromPath(SparcConstants.CONSTANT_SOURCING_NAME);
					String sourcingStatus = sourcingFlexType.getAttribute(SparcConstants.SOURCING_SOURCING_STATUS).getColumnName();
					String sourcingSentToS4 = sourcingFlexType.getAttribute(SparcConstants.SOURCING_SENT_TO_S4).getColumnName();
					skuARev = (LCSSKU)LCSQuery.findObjectById(SparcConstants.SKU_VR + skuID);
					String prodCostSheetID = fob.getString(SparcConstants.PRODUCT_COSTSHEET_OBJECT + "." + SparcConstants.BRANCH_ITERATION);
					
					sourceID = fob.getString(SparcConstants.SOURCING_OBJECT + "." + SparcConstants.BRANCH_ITERATION);
					skusourceID = fob.getString(SparcConstants.SOURCE_TO_SEASON_OBJECT + "." + SparcConstants.BRANCH_ITERATION);
					productCostSheet = (LCSProductCostSheet)LCSQuery.findObjectById(SparcConstants.PRODUCT_COSTSHEET_VR + String.valueOf(prodCostSheetID));
					sourceToSeasonLink = (LCSSourceToSeasonLink)LCSQuery.findObjectById(SparcConstants.SOURCE_TO_SEASON_VR + skusourceID);
					SparcRestIntegrationPIRHelper helper = new SparcRestIntegrationPIRHelper();
					String sourcingStatusValue = fob.getString(SparcConstants.SOURCE_TO_SEASON_OBJECT + "." + sourcingStatus);
					logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "STEP4 ****: sourcingStatusValue "+ integrationType +" : " + sourcingStatusValue);
					String SourcingSentToS4Value = fob.getString(SparcConstants.SOURCE_TO_SEASON_OBJECT + "." + sourcingSentToS4);
					try {
						if(FormatHelper.hasContent(sourcingStatusValue) && sourcingStatusValue.equalsIgnoreCase("dropped")) {
							if(FormatHelper.hasContent(SourcingSentToS4Value) && SourcingSentToS4Value.equalsIgnoreCase(SparcConstants.BOOLEAN_YES)) {
								SparcPIRPOJO pirPOJO = helper.getCSAndSourceData(sourceToSeasonLink, productCostSheet, sourceID, seasonID, skuARev);
								
								String value = productCostSheet.toString()+"_"+skuARev.getMaster().getName();
								String key = sourceToSeasonLink.toString();
								logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "STEP5 ****: key"+ integrationType +" : " + key+": value: "+value);
								sourceCScombo = mapwithExistingValue(sourceCScombo, key, value);
								listProductPojo.add(pirPOJO);
								successfulEntries.add(skuARev.getName()+"_"+sourceToSeasonLink.getSourcingConfigMaster().getSourcingConfigName());
							}else {
								shouldNotBePartOfQuery++;
							}
						}else {
							logger.logDebug(SparcRestIntegrationArticleHelper.class.getName(), methodName, "STEP6 ****: sourcingStatusValue not Dropped"+ integrationType +" : ");
							SparcPIRPOJO pirPOJO = helper.getCSAndSourceData(sourceToSeasonLink, productCostSheet, sourceID, seasonID, skuARev);
							listProductPojo.add(pirPOJO);
							String value = productCostSheet.toString()+"_"+skuARev.getMaster().getName();
							String key = sourceToSeasonLink.toString();
							logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "STEP7 ****: key"+ integrationType +" : " + key+": value: "+value);
							sourceCScombo = mapwithExistingValue(sourceCScombo, key, value);
							successfulEntries.add(skuARev.getName()+"_"+sourceToSeasonLink.getSourcingConfigMaster().getSourcingConfigName());
						}
					}catch(WTException | WTPropertyVetoException e) {
						logger.logError(SparcRestIntegrationArticleHelper.class.getName(), methodName, "WTException occured is "+e.getStackTrace());
						failedEntries.add(skuARev.getName()+"_"+sourceToSeasonLink.getSourcingConfigMaster().getSourcingConfigName());
					}
				}catch(Exception e) {
					logger.logError(SparcRestIntegrationArticleHelper.class.getName(), methodName, "Exception occured is "+e.getStackTrace());
				}
			}
		}	
		integrationPojoList.put("pojo", listProductPojo);
		integrationPojoList.put("apiInput", apiRequestInputs);
		integrationPojoList.put("SuccessEntries", successfulEntries);
		integrationPojoList.put("FailedEntries", failedEntries);
		integrationPojoList.put("ignoreRecords", shouldNotBePartOfQuery);
		integrationPojoList.put(SparcConstants.CONSTANT_LOGENTRY, logEntryValues);
		logger.logDebug(SparcRestIntegrationArticleHelper.class.getName(), methodName, integrationType + " End");
		logger.logInfo(SparcRestIntegrationArticleHelper.class.getName(), methodName, "source and  costsheet map is "+sourceCScombo);
		for (Map.Entry<String, String> it : sourceCScombo.entrySet()) {
			logger.logDebug(SparcRestIntegrationArticleHelper.class.getName(), methodName," Key : " + it.getKey() + ", Value : " + it.getValue());
			try {
				List allIntergratedColorwaysForSourceToSeason = new ArrayList();
				List allNonIntergratedColorwaysForSourceToSeason = new ArrayList();
				LCSSourceToSeasonLink sourSeason = (LCSSourceToSeasonLink)LCSQuery.findObjectById("OR:"+it.getKey());
				//LCSSourcingConfig source = (LCSSourcingConfig)VersionHelper.latestIterationOf(sourSeason.getSourcingConfigMaster());
				//HIT 324 Fix
				Collection skuSourcingLinksCol = new LCSSourcingConfigQuery().getSkuSourcingLinks(sourSeason.getSourcingConfigMaster(), null, sourSeason.getSeasonMaster(), true);
				Iterator skuLinksCollITR = skuSourcingLinksCol.iterator();
				while(skuLinksCollITR.hasNext()) {
					LCSSKUSourcingLink eachSKUSourcingLink = (LCSSKUSourcingLink)skuLinksCollITR.next();
					if(eachSKUSourcingLink.isActive()){
						LCSSKU eachSKUARev = (LCSSKU)VersionHelper.getVersion(eachSKUSourcingLink.getSkuMaster(),"A");
						if(!eachSKUARev.isPlaceholder()) {
							String skuSourceColorName = eachSKUARev.getMaster().getName();
							if(SparcConstants.BOOLEAN_YES.equalsIgnoreCase((String)eachSKUSourcingLink.getValue(SparcConstants.INTEGRATION_FLAG))) {
								allIntergratedColorwaysForSourceToSeason.add(skuSourceColorName);
							}else {
								allNonIntergratedColorwaysForSourceToSeason.add(skuSourceColorName);
							}
						}
					}
				}
				logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName, "Integrated Colorways for the source to Season Link"+allIntergratedColorwaysForSourceToSeason);
				logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName, "Non Integrated Colorways for the source to Season Link"+allNonIntergratedColorwaysForSourceToSeason);
				Object value = (Object)it.getValue();
				String[] splitValue = value.toString().split(",");
				for(int i=0; i<splitValue.length; i++) {
					String[] splitCSValue = splitValue[i].toString().split("_");
					LCSProductCostSheet cs = (LCSProductCostSheet)LCSQuery.findObjectById("OR:"+splitCSValue[0]);
					int applicableColorwayCount = 0; //Fix FP 449
					if(allNonIntergratedColorwaysForSourceToSeason.size() != 0) {
						String applicableColorName = cs.getApplicableColorNames();
						if(FormatHelper.hasContent(applicableColorName)) {
							String split[] = applicableColorName.split("\\|\\~\\*\\~\\|");
							for(int j=0;j<split.length;j++) {
								if(allNonIntergratedColorwaysForSourceToSeason.contains(split[j])) {
									applicableColorwayCount++; //Fix FP 449
								}
							}
							//FP 449 Fix start
							if(applicableColorwayCount == 0) {
								persistCostSheet(cs, SparcConstants.BOOLEAN_YES);
							}else {
								persistCostSheet(cs, SparcConstants.BOOLEAN_NO);
							}
							//FP 449 Fix End
						}else {
							persistCostSheet(cs, SparcConstants.BOOLEAN_NO);	
						}
					}else {
						persistCostSheet(cs, SparcConstants.BOOLEAN_YES);
					}
				}
				persistsourcing(sourSeason);
			}catch(WTException e) {
				logger.logError(SparcRestIntegrationHelper.class.getName(), methodName, "Exception is"+ e);
			}			
		}

		return integrationPojoList;
	}

	@SuppressWarnings("rawtypes")
	public void createLogEntry(String integrationType, List successEntries, List failedEntries, int totalofRecords, int noOfPages, int noOfCallPerCall, String apiInput) {
		methodName = "createLogEntry";
		logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName, "Start");
		try {
			Date currDate = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy hh:mm:ss a");
			//sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
			String timeStamp = sdf.format(currDate);
			int safeRecordNumber = getLogEntrySafeRecordNumberForSuccessEntries(successEntries);
			logger.logInfo(SparcRestIntegrationHelper.class.getName(),"getLogEntrySafeRecordNumberForSuccessEntries", "safeRecordNumber is"+safeRecordNumber); 
			if(successEntries.size() != 0 && safeRecordNumber != 0 && successEntries.size() > safeRecordNumber) {
				int recordCount = 1;
				for(int i=0;i<successEntries.size();i+=safeRecordNumber) {
					int j=i+safeRecordNumber;
					if(j > successEntries.size()) {
						j = successEntries.size();
					}
					List tempSuccess = successEntries.subList(i, j);
					LCSLogEntry logEntryObj =  LCSLogEntry.newLCSLogEntry();
					LCSLogEntryLogic logEntryLogic = new LCSLogEntryLogic();
					FlexType logFlexType = FlexTypeCache.getFlexTypeFromPath(SparcConstants.LOGENTRY_INTEGRATION);
					logEntryObj.setFlexType(logFlexType);
					logEntryObj.setValue(SparcConstants.CONSTANT_LOGENTRY_NAME, integrationType+"_"+timeStamp+"_"+recordCount);
					logger.logInfo(SparcRestIntegrationHelper.class.getName(),"getLogEntrySafeRecordNumberForSuccessEntries", "tempSuccess length "+tempSuccess.toString().length()); 
					logEntryObj.setValue("scSuccessFullEntries", tempSuccess.toString());
					logEntryObj.setValue("scFailedEntries", failedEntries.toString());
					
					if(failedEntries.size() > 0) {
						logEntryObj.setValue("scFixNeeded", SparcConstants.BOOLEAN_YES);
					}else {
						logEntryObj.setValue("scFixNeeded", SparcConstants.BOOLEAN_NO);
					}
					logEntryObj.setValue("apiInput", apiInput);
					logEntryObj.setValue("scTotalNoOfRecords", totalofRecords);
					logEntryObj.setValue("scFetchNoOfRows", noOfCallPerCall);
					logEntryObj.setValue("scPageNo", noOfPages);

					logEntryLogic.saveLog(logEntryObj);

					recordCount++;
				}

			}else {

				LCSLogEntry logEntryObj =  LCSLogEntry.newLCSLogEntry();
				LCSLogEntryLogic logEntryLogic = new LCSLogEntryLogic();
				FlexType logFlexType = FlexTypeCache.getFlexTypeFromPath(SparcConstants.LOGENTRY_INTEGRATION);
				logEntryObj.setFlexType(logFlexType);
				//logEntryObj.get
				logEntryObj.setValue(SparcConstants.CONSTANT_LOGENTRY_NAME, integrationType+"_"+timeStamp);
				logEntryObj.setValue("scSuccessFullEntries", successEntries.toString());
				logEntryObj.setValue("scFailedEntries", failedEntries.toString());
				if(failedEntries.size() > 0) {
					logEntryObj.setValue("scFixNeeded", SparcConstants.BOOLEAN_YES);
				}else {
					logEntryObj.setValue("scFixNeeded", SparcConstants.BOOLEAN_NO);
				}
				logEntryObj.setValue("apiInput", apiInput);
				logEntryObj.setValue("scTotalNoOfRecords", totalofRecords);
				logEntryObj.setValue("scFetchNoOfRows", noOfCallPerCall);
				logEntryObj.setValue("scPageNo", noOfPages);

				logEntryLogic.saveLog(logEntryObj);
			}
			logger.logInfo(SparcRestIntegrationHelper.class.getName(), methodName, "End");
		}catch(WTException | WTPropertyVetoException e) {
			logger.logError(SparcRestIntegrationHelper.class.getName(), methodName, "Exception is "+ e);
		}
	}

	@SuppressWarnings("rawtypes")
	private int getLogEntrySafeRecordNumberForSuccessEntries(List successEntries) {
		logger.logInfo(SparcRestIntegrationHelper.class.getName(),"getLogEntrySafeRecordNumberForSuccessEntries","successEntries List size is "+successEntries.size());
		logger.logInfo(SparcRestIntegrationHelper.class.getName(),"getLogEntrySafeRecordNumberForSuccessEntries","successEntries List to String length is "+successEntries.toString().length());
		int value = successEntries.toString().length() / 1330; // 1330 is the max allowed character for text area on ptc cloud.
		logger.logInfo(SparcRestIntegrationHelper.class.getName(),"getLogEntrySafeRecordNumberForSuccessEntries", "value "+value); 
		int returnSize =0;
		if(value > 0) {
			returnSize = successEntries.size() / value;
		}
		logger.logInfo(SparcRestIntegrationHelper.class.getName(),"getLogEntrySafeRecordNumberForSuccessEntries", "returnSize "+returnSize); 
		return returnSize;
	}

	public String removeCommaIfItsInEnd(String value) {
		String LatestValue = value;
		if(FormatHelper.hasContent(LatestValue) && LatestValue.endsWith(",")) {
			LatestValue = LatestValue.substring(0, LatestValue.length()-1);
		}
		return LatestValue;
	}
	public SparcRestIntegrationHelper() {
	}

	public void persistsourcing(LCSSourceToSeasonLink sourceseasLink) {
		try {
			if(sourceseasLink != null) {
				LCSSourceToSeasonLinkClientModel scSeasonModel = new LCSSourceToSeasonLinkClientModel();
				scSeasonModel.load(FormatHelper.getObjectId(sourceseasLink));
				scSeasonModel.setValue(SparcConstants.SOURCING_SENT_TO_S4, SparcConstants.BOOLEAN_YES);
				scSeasonModel.save();
				logger.logDebug(SparcRestIntegrationHelper.class.getName(), "persistsourcing", " Sourcing Configuration Sent to S4 set to Yes");
			}
		}
		catch(WTException | WTPropertyVetoException e) {
			logger.logError(SparcRestIntegrationHelper.class.getName(), "persistsourcing", "Exception is "+e);
		}
	}

	public void persistCostSheet(LCSProductCostSheet costsheet, String sentToS4) {
		try {
			costsheet = (LCSProductCostSheet)VersionHelper.latestIterationOf(costsheet);
			String senttoS4 = (String)costsheet.getValue(SparcConstants.COSTSHEET_SENT_TO_S4);
			logger.logDebug(SparcRestIntegrationHelper.class.getName(), "persistCostSheet", "sent to S4 from current Costsheet is "+senttoS4);
			if(FormatHelper.hasContent(senttoS4) && "scNo".equalsIgnoreCase(senttoS4)) {
				LCSProductCostSheet CS = VersionHelper.checkout(costsheet);
				CS.setValue(SparcConstants.COSTSHEET_SENT_TO_S4, sentToS4);
				LCSLogic.persist(CS,true);
				CS = VersionHelper.checkin(CS);
				logger.logInfo(SparcRestIntegrationHelper.class.getName(), "persistCostSheet", "costsheet "+costsheet.getName()+" Saved");
			}
			logger.logInfo(SparcRestIntegrationHelper.class.getName(), "persistCostSheet", costsheet.getName()+ " Sent to S4 set to " + sentToS4);
		} catch (WTException e) {
			logger.logError(SparcRestIntegrationHelper.class.getName(), "persistCostSheet", "Exception is "+e);
		}
	}
	public Map<String, String> mapwithExistingValue(Map<String, String> map, String key, String value) {
		String methodName = "mapwithExistingValue";
		
		logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "STEP8 **** "+key+"-"+value);
		
		if(map.containsKey(key)) {
			logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "STEP9 ****map.get(key).toString() "+ map.get(key).toString());
			logger.logDebug(SparcRestIntegrationHelper.class.getName(), methodName, "Value "+ value);
			String latestValue = map.get(key) + "," + value;
			value = latestValue;
		}
		map.put(key, value);
		return map;
	}
}
