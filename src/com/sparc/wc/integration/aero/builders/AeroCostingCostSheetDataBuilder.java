package com.sparc.wc.integration.aero.builders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_FG_FACTORY_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_VENDOR_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_NAME_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_ADDITIONAL_BUY_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_DESTINATION_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_INCOTERMS_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_MILESTONE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_NAME_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_YEAR_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_NAME_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_TYPE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_TIMESTAMP_FORMAT;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_NAME_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_NUMBER_ATTR;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.sparc.wc.integration.aero.domain.AeroCostingCostSheetData;
import com.sparc.wc.integration.aero.repository.LCSTableColumns;
import com.sparc.wc.integration.aero.repository.LCSTables;

import wt.util.WTException;

/**
 * Builds a Colorway Cost Sheet Data containing relevant details for aero costing integration.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9646 Aero - Costing Interface"
 */
public class AeroCostingCostSheetDataBuilder {
	
	private FlexObject rawDataMap;
	private AeroCostingCostSheetData csData;

	public AeroCostingCostSheetDataBuilder(AeroCostingCostSheetData csData) {
		this.csData = csData;
	}
	
	public AeroCostingCostSheetDataBuilder setRawDataMap(FlexObject rawDataMap) {
		this.rawDataMap = rawDataMap;
		return this;
	}
	
	/**
	 * Extracts the selected attribute values from the data map and loads them into the cost sheet data instance.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildDataContent() throws WTException {
		
		final FlexType productFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_PRODUCT_FLEX_TYPE_PATH);
		final FlexType costSheetFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_COST_SHEET_FLEX_TYPE_PATH);
		
        final String productNumColName = productFlexType.getAttribute(AERO_PRODUCT_NUMBER_ATTR).getColumnName();
        final String productNameColName = productFlexType.getAttribute(AERO_PRODUCT_NAME_ATTR).getColumnName();
        final String costSheetNameColName = costSheetFlexType.getAttribute(AERO_COST_SHEET_NAME_ATTR).getColumnName();
        final String costSheetNumberColName = costSheetFlexType.getAttribute(AERO_COST_SHEET_NUMBER_ATTR).getColumnName();
        final String costSheetMilestoneColName = costSheetFlexType.getAttribute(AERO_COST_SHEET_MILESTONE_ATTR).getColumnName();
        
        //Product Attributes.
        csData.setProductId(Long.parseLong((String)rawDataMap.get(LCSTables.LCSPRODUCT.name() + "." + LCSTableColumns.IDA2A2.name())));
        csData.setProductNumber(Long.parseLong((String)rawDataMap.get(LCSTables.LCSPRODUCT.name() + "." + productNumColName)));
		csData.setProductName((String)rawDataMap.get(LCSTables.LCSPRODUCT.name() + "." + productNameColName));
        
		//Cost Sheet Attributes.
		csData.setCostSheetId(Long.parseLong((String)rawDataMap.get(LCSTables.LCSPRODUCTCOSTSHEET.name() + "." + LCSTableColumns.IDA2A2.name())));
		csData.setCostSheetName((String)rawDataMap.get(LCSTables.LCSPRODUCTCOSTSHEET.name() + "." + costSheetNameColName));
		csData.setCostSheetNumber(
				Long.parseLong((String)rawDataMap.get(LCSTables.LCSPRODUCTCOSTSHEET.name() + "." + costSheetNumberColName)));
		csData.setCostSheetMilestone((String)rawDataMap.get(LCSTables.LCSPRODUCTCOSTSHEET.name() + "." + costSheetMilestoneColName));
		
		try {
			
			final Date modifiedDate = new SimpleDateFormat(AERO_TIMESTAMP_FORMAT).parse(
					(String)rawDataMap.get(LCSTables.LCSPRODUCTCOSTSHEET.name() + "." + LCSTableColumns.MODIFYSTAMPA2.name()));
			
			csData.setCostSheetModifiedDate(new Timestamp(modifiedDate.getTime()));
			
		} catch(ParseException ex) {
			throw new WTException(ex, "Unable to extract last saved timestamp from cost sheet # "
					+ csData.getCostSheetNumber() + "(" + csData.getCostSheetName() + ").");
		}
		
		//Other Link object IDs.
		csData.setSourcingSeasonLinkId(Long.parseLong((String)rawDataMap.get(LCSTables.LCSSOURCETOSEASONLINK.name() + "." + LCSTableColumns.IDA2A2.name())));
		csData.setSourcingColorwayLinkId(Long.parseLong((String)rawDataMap.get(LCSTables.LCSSKUSOURCINGLINK.name() + "." + LCSTableColumns.IDA2A2.name())));
		csData.setColorwaySeasonLinkId(Long.parseLong((String)rawDataMap.get(LCSTables.LCSSKUSEASONLINK.name() + "." + LCSTableColumns.IDA2A2.name())));
		
	}
	
	/**
	 * Extracts the selected attribute values from the data map and loads them into the cost sheet data's keys instance.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildDataKeys() throws WTException {
		
		final FlexType srcConfigFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_SOURCING_CONFIG_FLEX_TYPE_PATH);
		final FlexType seasonFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_SEASON_FLEX_TYPE_PATH);
		final FlexType colorwayFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_PRODUCT_FLEX_TYPE_PATH);
		final FlexType costSheetFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_COST_SHEET_FLEX_TYPE_PATH);
		
		final String srcConfigNumColName = srcConfigFlexType.getAttribute(AERO_SOURCING_CONFIG_NUMBER_ATTR).getColumnName();
		final String seasonNameColName = seasonFlexType.getAttribute(AERO_SEASON_NAME_ATTR).getColumnName();
		final String seasonTypeColName = seasonFlexType.getAttribute(AERO_SEASON_TYPE_ATTR).getColumnName();
		final String seasonYearColName = seasonFlexType.getAttribute(AERO_SEASON_YEAR_ATTR).getColumnName();
		final String colorwayNameColName = colorwayFlexType.getAttribute(AERO_COLORWAY_NAME_ATTR).getColumnName();
		final String colorwayNumberColName = colorwayFlexType.getAttribute(AERO_COLORWAY_NUMBER_ATTR).getColumnName();
		final String srcConfigFGFactoryColName = srcConfigFlexType.getAttribute(AERO_SOURCING_FG_FACTORY_ATTR).getColumnName();
		final String srcConfigVendorColName = srcConfigFlexType.getAttribute(AERO_SOURCING_VENDOR_ATTR).getColumnName();
		final String costSheetIncotermsColName = costSheetFlexType.getAttribute(AERO_COST_SHEET_INCOTERMS_ATTR).getColumnName();
		final String costSheetAdditionalBuyColName = costSheetFlexType.getAttribute(AERO_COST_SHEET_ADDITIONAL_BUY_ATTR).getColumnName();
		final String costSheetDestinationColName = costSheetFlexType.getAttribute(AERO_COST_SHEET_DESTINATION_ATTR).getColumnName();
		
		//Sourcing Config Attributes.
		csData.getCostSheetKey().setSourcingConfigId(
				Long.parseLong((String)rawDataMap.get(LCSTables.LCSSOURCINGCONFIG.name() + "." + LCSTableColumns.IDA2A2.name())));
		csData.getCostSheetKey().setSourcingConfigNumber(
				Long.parseLong((String)rawDataMap.get(LCSTables.LCSSOURCINGCONFIG.name() + "." + srcConfigNumColName)));
		csData.getCostSheetKey().setFgFactoryBranchId(
				Long.parseLong((String)rawDataMap.get(LCSTables.LCSSOURCINGCONFIG.name() + "." + srcConfigFGFactoryColName)));
		csData.getCostSheetKey().setVendorBranchId(
				Long.parseLong((String)rawDataMap.get(LCSTables.LCSSOURCINGCONFIG.name() + "." + srcConfigVendorColName)));
		
        //Season Attributes.
        csData.getCostSheetKey().setSeasonId(
        		Long.parseLong((String)rawDataMap.get(LCSTables.LCSSEASON.name() + "." + LCSTableColumns.IDA2A2.name())));
        csData.getCostSheetKey().setSeasonName((String)rawDataMap.get(LCSTables.LCSSEASON.name() + "." + seasonNameColName));
        csData.getCostSheetKey().setSeasonType((String)rawDataMap.get(LCSTables.LCSSEASON.name() + "." + seasonTypeColName));
        csData.getCostSheetKey().setSeasonYear((String)rawDataMap.get(LCSTables.LCSSEASON.name() + "." + seasonYearColName));
        
        //Colorway Attributes.
        csData.getCostSheetKey().setColorwayId(
				Long.parseLong((String)rawDataMap.get(LCSTables.LCSSKU.name() + "." + LCSTableColumns.IDA2A2.name())));
        csData.getCostSheetKey().setColorwayName((String)rawDataMap.get(LCSTables.LCSSKU.name() + "." + colorwayNameColName));
        csData.getCostSheetKey().setColorwayNumber(
        		Long.parseLong((String)rawDataMap.get(LCSTables.LCSSKU.name() + "." + colorwayNumberColName)));
        
        //Cost Sheet Attributes.
        csData.getCostSheetKey().setCostSheetIncoterms((String)rawDataMap.get(LCSTables.LCSPRODUCTCOSTSHEET.name() + "." + costSheetIncotermsColName));
        csData.getCostSheetKey().setCostSheetAdditionalBuy((String)rawDataMap.get(LCSTables.LCSPRODUCTCOSTSHEET.name() + "." + costSheetAdditionalBuyColName));
        csData.getCostSheetKey().setCostSheetDestination((String)rawDataMap.get(LCSTables.LCSPRODUCTCOSTSHEET.name() + "." + costSheetDestinationColName));
        
	}
	
	/**
	 * Builds the costing cost sheet data from the flex object result set map.
	 * @return The costing cost sheet data built.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	public AeroCostingCostSheetData build() throws WTException {
		
		if (rawDataMap == null || rawDataMap.isEmpty() || csData == null) {
			return csData;
		}
		
		buildDataKeys();
		buildDataContent();
		
		return csData;
	}

}
