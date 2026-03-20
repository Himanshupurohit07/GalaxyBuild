package com.sparc.wc.integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSKUSourcingLink;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.integration.pojo.SparcPIRPOJO;
import com.sparc.wc.util.SparcConstants;
import com.sparc.wc.util.SparcLogger;

import wt.util.WTException;

public class SparcRestIntegrationPIRHelper {
	SparcLogger logger = SparcLogger.getInstance();
	
	public SparcPIRPOJO getCSAndSourceData(LCSSourceToSeasonLink sourceseasLink, LCSProductCostSheet costsheet, String sourceId, String seasonId, LCSSKU skuARev) throws Exception{
		SparcRestIntegrationHelper restHelper = new SparcRestIntegrationHelper();
		logger.logInfo(SparcRestIntegrationPIRHelper.class.getName(), "getCSAndSourceData", "Start");
		SparcPIRPOJO pirPOJO = new SparcPIRPOJO();		
		
			LCSSourcingConfig source = (LCSSourcingConfig)LCSQuery.findObjectById(SparcConstants.SOURCING_CONFIG_VR + sourceId);
			LCSSeason season = (LCSSeason)LCSQuery.findObjectById(SparcConstants.SEASON_VR + FormatHelper.format(seasonId));

			pirPOJO.setScColorwayNo(restHelper.getDisplayValue(skuARev, SparcConstants.SKU_NUMBER));
			pirPOJO.setScFGFactory(restHelper.getDisplayValue(source, SparcConstants.SOURCING_FACTORY));
			pirPOJO.setScFactoryCountryOrigin(restHelper.getDisplayValue(source, SparcConstants.SOURCING_FACTORY_COUNTRY_ORIGIN));
			//pirPOJO.setScRegion(restHelper.getDisplayValue(source, SparcConstants.SOURCING_REGION));
			pirPOJO.setScprodleadtime(restHelper.getDisplayValue(sourceseasLink, SparcConstants.SOURCING_PRODUCTION_LEAD_TIME));
			//pirPOJO.setSctransleadtime(restHelper.getDisplayValue(sourceseasLink, SparcConstants.SOURCING_TRANSCATION_LEAD_TIME));
			pirPOJO.setScSourcingStatus(restHelper.getDisplayValue(sourceseasLink, SparcConstants.SOURCING_SOURCING_STATUS));
			pirPOJO.setSeasonType(restHelper.getDisplayValue(season, SparcConstants.SEASON_TYPE));
			pirPOJO.setYear(restHelper.getDisplayValue(season,SparcConstants.SEASON_YEAR));

			pirPOJO.setScIncoterms(restHelper.getDisplayValue(costsheet, SparcConstants.COSTSHEET_INCO_TERMS));
			pirPOJO.setScIncotermsLoc(restHelper.getDisplayValue(costsheet, SparcConstants.COSTSHEET_INCO_TERMS_LOCATION));
			pirPOJO.setScVendorCost(restHelper.getDisplayValue(costsheet, SparcConstants.COSTSHEET_VENDOR_COST));
			pirPOJO.setScTotalVendorCost(restHelper.getDisplayValue(costsheet, SparcConstants.COSTSHEET_TOTAL_VENDOR_COST));
			pirPOJO.setScMarketingroyalty(restHelper.getDisplayValue(costsheet, SparcConstants.COSTSHEET_MARKETING_ROYALTY));
			pirPOJO.setScOtherCost(restHelper.getDisplayValue(costsheet, SparcConstants.COSTSHEET_UNIT_OTHER_COST));	
			persistsourceAndCostSheet(sourceseasLink, costsheet, skuARev.getMaster().getName());
			
			try {
				String scFactoryPriority = (String) sourceseasLink
						.getValue(SparcConstants.SKU_SEASON_FACTORY_PRIORITY);
				// Set Factory Priority from LCSSourceToSeasonLink
				pirPOJO.setScFactoryPriority(scFactoryPriority);
			} catch (Exception e) {
				logger.logInfo(SparcRestIntegrationArticleHelper.class.getName(), "getCSAndSourceData",
						"Failed to set Factory Priority: " + e.getMessage());
			}
			logger.logInfo(SparcRestIntegrationPIRHelper.class.getName(), "getCSAndSourceData", "End");
		
		return pirPOJO;
	}
	@SuppressWarnings("rawtypes")
	public void persistsourceAndCostSheet(LCSSourceToSeasonLink sourceseasLink, LCSProductCostSheet costsheet, String colorwayName) {
		try {
			//HIT 324 Fix
			Collection skuSourcingLinksCol = new LCSSourcingConfigQuery().getSkuSourcingLinks(costsheet.getSourcingConfigMaster(), null, costsheet.getSeasonMaster(), true);
			Iterator skuLinksCollITR = skuSourcingLinksCol.iterator();
			List<String> sourceColorwayList =  new ArrayList<String>();
			while(skuLinksCollITR.hasNext()) {
				sourceColorwayList.add(colorwayName);
				LCSSKUSourcingLink eachSKUSourcingLink = (LCSSKUSourcingLink)skuLinksCollITR.next();
				if(eachSKUSourcingLink.isActive()){
					LCSSKU eachSKUARev = (LCSSKU)VersionHelper.getVersion(eachSKUSourcingLink.getSkuMaster(),"A");
					String skuSourceColorName = eachSKUARev.getMaster().getName();
					logger.logDebug(SparcRestIntegrationPIRHelper.class.getName(), "persistsourceAndCostSheet", "Each SKU Name is "+skuSourceColorName);
					logger.logDebug(SparcRestIntegrationPIRHelper.class.getName(), "persistsourceAndCostSheet", "SKU Name is "+colorwayName);
					if(FormatHelper.hasContent(colorwayName) && FormatHelper.hasContent(skuSourceColorName) && colorwayName.equalsIgnoreCase(skuSourceColorName)) {
						eachSKUSourcingLink.setValue(SparcConstants.INTEGRATION_FLAG , SparcConstants.BOOLEAN_YES);
						LCSLogic.persist(eachSKUSourcingLink);
					}
				}
			}		
		}catch(WTException e) {
			logger.logError(SparcRestIntegrationPIRHelper.class.getName(), "persistsourceAndCostSheet", "WTException is "+e);
		}
	}
}