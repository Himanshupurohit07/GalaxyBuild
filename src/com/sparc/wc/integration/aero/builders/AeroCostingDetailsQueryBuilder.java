package com.sparc.wc.integration.aero.builders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_NAME_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_ADDITIONAL_BUY_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_DESTINATION_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_INCOTERMS_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_MILESTONE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_NAME_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_NAME_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_TYPE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_YEAR_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_NAME_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_FG_FACTORY_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_VENDOR_ATTR;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.sparc.wc.integration.aero.repository.LCSTableColumns;
import com.sparc.wc.integration.aero.repository.LCSTables;

import wt.util.WTException;

/**
 * Builds a query (prepared query statement) to obtain the list of product cost sheets from a given sourcing configuration number which 
 * are eligible for Aero S4 & CAP integration.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9646 Aero - Costing Interface"
 */
public abstract class AeroCostingDetailsQueryBuilder extends AeroCostingIdsQueryBuilder {
	
	private Long sourcingConfigNumber;

	/**
	 * Constructs a AeroCostingDetailsQueryBuilder instance.
	 * @param sourcingConfigNumber The sourcing config number to get the list of related eligible cost sheets.
	 */
	public AeroCostingDetailsQueryBuilder(Long sourcingConfigNumber) {
		this.sourcingConfigNumber = sourcingConfigNumber;
	}
	
	/**
	 * Builds the SELECT portion of the query into the prepared statement.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	@Override
	protected void buildSelect(PreparedQueryStatement pqs) throws WTException {
		
		if (pqs == null) {
			return;
		}
		
		buildSelectSourcingConfigAttributeValues(pqs);
		buildSelectProductAttributeValues(pqs);
		buildSelectSeasonAttributeValues(pqs);
		buildSelectCostSheetAttributeValues(pqs);
		buildSelectColorwayAttributeValues(pqs);
		
		pqs.appendSelectColumn(LCSTables.LCSSOURCETOSEASONLINK.name(), LCSTableColumns.IDA2A2.name());
		pqs.appendSelectColumn(LCSTables.LCSSKUSOURCINGLINK.name(), LCSTableColumns.IDA2A2.name());
		pqs.appendSelectColumn(LCSTables.LCSSKUSEASONLINK.name(), LCSTableColumns.IDA2A2.name());
		
	}
	
	/**
	 * Builds the SELECT to get the Sourcing Config related attribute values<br>
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildSelectSourcingConfigAttributeValues(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType srcConfigFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_SOURCING_CONFIG_FLEX_TYPE_PATH);
		
		final String srcConfigNumColName = srcConfigFlexType.getAttribute(AERO_SOURCING_CONFIG_NUMBER_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSSOURCINGCONFIG.name(), srcConfigNumColName);
		
		final String srcConfigFGFactoryColName = srcConfigFlexType.getAttribute(AERO_SOURCING_FG_FACTORY_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSSOURCINGCONFIG.name(), srcConfigFGFactoryColName);
		
		final String srcConfigVendorColName = srcConfigFlexType.getAttribute(AERO_SOURCING_VENDOR_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSSOURCINGCONFIG.name(), srcConfigVendorColName);
		
		pqs.appendSelectColumn(LCSTables.LCSSOURCINGCONFIG.name(), LCSTableColumns.IDA2A2.name());
	}
	
	/**
	 * Builds the SELECT to get the Product related attribute values.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildSelectProductAttributeValues(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType productFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_PRODUCT_FLEX_TYPE_PATH);
		
		final String productNumColName = productFlexType.getAttribute(AERO_PRODUCT_NUMBER_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSPRODUCT.name(), productNumColName);
		
		final String productNameColName = productFlexType.getAttribute(AERO_PRODUCT_NAME_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSPRODUCT.name(), productNameColName);
		
		pqs.appendSelectColumn(LCSTables.LCSPRODUCT.name(), LCSTableColumns.IDA2A2.name());
		
	}
	
	/**
	 * Builds the SELECT to get the Season related attribute values.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildSelectSeasonAttributeValues(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType seasonFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_SEASON_FLEX_TYPE_PATH);
		
		final String seasonNameColName = seasonFlexType.getAttribute(AERO_SEASON_NAME_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSSEASON.name(), seasonNameColName);
		
		final String seasonYearColName = seasonFlexType.getAttribute(AERO_SEASON_YEAR_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSSEASON.name(), seasonYearColName);
		
		final String seasonTypeColName = seasonFlexType.getAttribute(AERO_SEASON_TYPE_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSSEASON.name(), seasonTypeColName);
		
		pqs.appendSelectColumn(LCSTables.LCSSEASON.name(), LCSTableColumns.IDA2A2.name());
	}
	
	/**
	 * Builds the SELECT to get the Cost Sheet related attribute values.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildSelectCostSheetAttributeValues(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType costSheetFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_COST_SHEET_FLEX_TYPE_PATH);
		
		pqs.appendSelectColumn(LCSTables.LCSPRODUCTCOSTSHEET.name(), LCSTableColumns.MODIFYSTAMPA2.name());
		pqs.appendSelectColumn(LCSTables.LCSPRODUCTCOSTSHEET.name(), LCSTableColumns.IDA2A2.name());
		
		final String costSheetNameColName = costSheetFlexType.getAttribute(AERO_COST_SHEET_NAME_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSPRODUCTCOSTSHEET.name(), costSheetNameColName);
		
		final String costSheetNumberColName = costSheetFlexType.getAttribute(AERO_COST_SHEET_NUMBER_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSPRODUCTCOSTSHEET.name(), costSheetNumberColName);
		
		final String costSheetMilestoneColName = costSheetFlexType.getAttribute(AERO_COST_SHEET_MILESTONE_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSPRODUCTCOSTSHEET.name(), costSheetMilestoneColName);
		
		final String costSheetIncotermsColName = costSheetFlexType.getAttribute(AERO_COST_SHEET_INCOTERMS_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSPRODUCTCOSTSHEET.name(), costSheetIncotermsColName);
		
		final String costSheetAdditionalBuyColName = costSheetFlexType.getAttribute(AERO_COST_SHEET_ADDITIONAL_BUY_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSPRODUCTCOSTSHEET.name(), costSheetAdditionalBuyColName);
		
		final String costSheetDestinationColName = costSheetFlexType.getAttribute(AERO_COST_SHEET_DESTINATION_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSPRODUCTCOSTSHEET.name(), costSheetDestinationColName);
		
	}
	
	/**
	 * Builds the SELECT to get the Colorway related attribute values.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildSelectColorwayAttributeValues(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType colorwayFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_PRODUCT_FLEX_TYPE_PATH);
		
		final String colorwayNameColName = colorwayFlexType.getAttribute(AERO_COLORWAY_NAME_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSSKU.name(), colorwayNameColName);
		
		final String colorwayNumberColName = colorwayFlexType.getAttribute(AERO_COLORWAY_NUMBER_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSSKU.name(), colorwayNumberColName);
		
		pqs.appendSelectColumn(LCSTables.LCSSKU.name(), LCSTableColumns.IDA2A2.name());
	}
	
	/**
	 * Builds the WHERE portion of the query into the prepared statement.<br>
	 * This includes condition criterias that address the following business requirements:<br>
	 * TBD.
	 * 
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	@Override
	protected void buildWhere(PreparedQueryStatement pqs) throws WTException {
		
		if (pqs == null) {
			return;
		}
		
		super.buildWhere(pqs);
		buildWhereSourcingConfigNo(pqs);
        
	}
	
	/**
	 * Builds the WHERE criteria for the Sourcing Configuration Number.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereSourcingConfigNo(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType srcConfigFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_SOURCING_CONFIG_FLEX_TYPE_PATH);
		final String srcConfigNumColName = srcConfigFlexType.getAttribute(AERO_SOURCING_CONFIG_NUMBER_ATTR).getColumnName();
		
		pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCINGCONFIG.name(), 
        		srcConfigNumColName, 
        		((sourcingConfigNumber != null) ? sourcingConfigNumber.toString() : null), 
        		Criteria.EQUALS));
		
	}
	
}
