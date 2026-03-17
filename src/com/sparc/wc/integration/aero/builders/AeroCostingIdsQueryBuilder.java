package com.sparc.wc.integration.aero.builders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_OBJECT_EFFECT_LATEST_YES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LATEST_FLEX_OBJECT_ITERATION;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LATEST_FLEX_OBJECT_VERSION;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_STATUS_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_STATUS_CONFIRMED_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_OBJECT_ACTIVE_YES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_COST_SHEET_TYPE_PRODUCT;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_COST_SHEET_WHATIF_NO;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_OBJECT_PLACEHOLDER_NO;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_SEASON_REMOVED_NO;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_SKU_SEASON_LINK_TYPE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_VALID_DEVELOPMENT_SEASONS_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_TYPE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_FLEX_TYPE_PATH;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.sparc.wc.integration.aero.repository.LCSTableColumns;
import com.sparc.wc.integration.aero.repository.LCSTables;

import wt.util.WTException;

/**
 * Builds a query (prepared query statement) to obtain the list of sourcing config numbers that contain eligible cost sheets for Aero S4 and CAP integration.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Enhanced query to link colorways associated to cost sheets by making use of table "COSTSHEETTOCOLORLINK" and removing potentially ambiguous LIKE condition. Updated methods "buildFrom", "buildJoins" and "buildWhereExcludeInvalidColorways".
 * - Enhanced query condition to prevent pulling season-removed sku season links.<br>
 * - Task #10565: Add missing join between lcsskusourcinglink and lcsseason tables to prevent bad dups to appear when same colorway is used in multiple season.<br>
 * - Removed sort-by method to improve query performance.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9646 Aero - Costing Interface"
 */
public abstract class AeroCostingIdsQueryBuilder {
	
	/**
	 * Constructs a AeroCostingIdsQueryBuilder instance. 
	 */
	public AeroCostingIdsQueryBuilder() {
		
	}
	
	/**
	 * Builds the SELECT portion of the query into the prepared statement.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	protected void buildSelect(PreparedQueryStatement pqs) throws WTException {
		
		if (pqs == null) {
			return;
		}
		
		pqs.setDistinct(true);
		
		//SELECT: scSourcingNo (Sourcing Config #)
		final FlexType srcConfigFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_SOURCING_CONFIG_FLEX_TYPE_PATH);
		final String srcConfigNumColName = srcConfigFlexType.getAttribute(AERO_SOURCING_CONFIG_NUMBER_ATTR).getColumnName();
		
		pqs.appendSelectColumn(LCSTables.LCSSOURCINGCONFIG.name(), srcConfigNumColName);
		
	}
	
	/**
	 * Builds the FROM portion of the query into the prepared statement.
	 * @param pqs The prepared statement to be updated.
	 */
	protected void buildFrom(PreparedQueryStatement pqs) {
		
		if (pqs == null) {
			return;
		}
		
		pqs.appendFromTable(LCSTables.LCSSOURCINGCONFIGMASTER.name());
        pqs.appendFromTable(LCSTables.LCSSOURCETOSEASONLINKMASTER.name());
        pqs.appendFromTable(LCSTables.LCSPRODUCT.name());
        pqs.appendFromTable(LCSTables.LCSSEASON.name()); 
        pqs.appendFromTable(LCSTables.LCSSOURCINGCONFIG.name());
        pqs.appendFromTable(LCSTables.LCSSOURCETOSEASONLINK.name());
        pqs.appendFromTable(LCSTables.LCSCOSTSHEETMASTER.name());
        pqs.appendFromTable(LCSTables.LCSPRODUCTCOSTSHEET.name());
        pqs.appendFromTable(LCSTables.LCSSKUSOURCINGLINK.name());
        pqs.appendFromTable(LCSTables.LCSSKU.name());
        pqs.appendFromTable(LCSTables.LCSSKUSEASONLINK.name());
        pqs.appendFromTable(LCSTables.COSTSHEETTOCOLORLINK.name());
        
	}
	
	/**
	 * Builds the table JOINS portion of the query into the prepared statement.
	 * @param pqs The prepared statement to be updated.
	 */
	protected void buildJoins(PreparedQueryStatement pqs) {
		
		if (pqs == null) {
			return;
		}
		
        //LCSSOURCETOSEASONLINKMASTER.IDA3A6 <-> LCSSOURCINGCONFIG.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.LCSSOURCETOSEASONLINKMASTER.name(), LCSTableColumns.IDA3A6.name(), 
        		LCSTables.LCSSOURCINGCONFIG.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
        //LCSSOURCETOSEASONLINKMASTER.IDA3B6 <-> LCSSEASON.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.LCSSOURCETOSEASONLINKMASTER.name(), LCSTableColumns.IDA3B6.name(),
        		LCSTables.LCSSEASON.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
        //LCSSOURCINGCONFIGMASTER.IDA3A6 <-> LCSPRODUCT.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.LCSSOURCINGCONFIGMASTER.name(), LCSTableColumns.IDA3A6.name(),
        		LCSTables.LCSPRODUCT.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
        //LCSSOURCINGCONFIG.IDA3MASTERREFERENCE <-> LCSSOURCINGCONFIGMASTER.IDA2A2
        pqs.appendJoin(LCSTables.LCSSOURCINGCONFIG.name(), LCSTableColumns.IDA3MASTERREFERENCE.name(),
        		LCSTables.LCSSOURCINGCONFIGMASTER.name(), LCSTableColumns.IDA2A2.name());
        //LCSSOURCETOSEASONLINK.IDA3MASTERREFERENCE <-> LCSSOURCETOSEASONLINKMASTER.IDA2A2
        pqs.appendJoin(LCSTables.LCSSOURCETOSEASONLINK.name(), LCSTableColumns.IDA3MASTERREFERENCE.name(),
        		LCSTables.LCSSOURCETOSEASONLINKMASTER.name(), LCSTableColumns.IDA2A2.name());
        //LCSCOSTSHEETMASTER.IDA3A6 <-> LCSSOURCINGCONFIGMASTER.IDA2A2
        pqs.appendJoin(LCSTables.LCSCOSTSHEETMASTER.name(), LCSTableColumns.IDA3A6.name(),
        		LCSTables.LCSSOURCINGCONFIGMASTER.name(), LCSTableColumns.IDA2A2.name());
        //LCSCOSTSHEETMASTER.IDA3C6 <-> LCSSOURCETOSEASONLINKMASTER.IDA3B6
        pqs.appendJoin(LCSTables.LCSCOSTSHEETMASTER.name(), LCSTableColumns.IDA3C6.name(),
        		LCSTables.LCSSOURCETOSEASONLINKMASTER.name(), LCSTableColumns.IDA3B6.name());
        //LCSPRODUCTCOSTSHEET.IDA3MASTERREFERENCE <-> LCSCOSTSHEETMASTER.IDA2A2
        pqs.appendJoin(LCSTables.LCSPRODUCTCOSTSHEET.name(), LCSTableColumns.IDA3MASTERREFERENCE.name(),
        		LCSTables.LCSCOSTSHEETMASTER.name(), LCSTableColumns.IDA2A2.name());
        //LCSSKUSOURCINGLINK.IDA3B5 <-> LCSSOURCINGCONFIGMASTER.IDA2A2
        pqs.appendJoin(LCSTables.LCSSKUSOURCINGLINK.name(), LCSTableColumns.IDA3B5.name(),
        		LCSTables.LCSSOURCINGCONFIGMASTER.name(), LCSTableColumns.IDA2A2.name());
        //LCSSKU.IDA3MASTERREFERENCE <-> LCSSKUSOURCINGLINK.IDA3A5
        pqs.appendJoin(LCSTables.LCSSKU.name(), LCSTableColumns.IDA3MASTERREFERENCE.name(),
        		LCSTables.LCSSKUSOURCINGLINK.name(), LCSTableColumns.IDA3A5.name());
        //LCSSEASON.IDA3MASTERREFERENCE = LCSSKUSOURCINGLINK.IDA3A7
        pqs.appendJoin(LCSTables.LCSSEASON.name(), LCSTableColumns.IDA3MASTERREFERENCE.name(),
        		LCSTables.LCSSKUSOURCINGLINK.name(), LCSTableColumns.IDA3A7.name());
        //LCSSKUSEASONLINK.PRODUCTMASTERID <-> LCSPRODUCT.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.LCSSKUSEASONLINK.name(), LCSTableColumns.PRODUCTMASTERID.name(),
        		LCSTables.LCSPRODUCT.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
        //LCSSKUSEASONLINK.IDA3B5 <-> LCSSEASON.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.LCSSKUSEASONLINK.name(), LCSTableColumns.IDA3B5.name(),
        		LCSTables.LCSSEASON.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
        //LCSSKUSEASONLINK.SKUMASTERID <-> LCSSKU.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.LCSSKUSEASONLINK.name(), LCSTableColumns.SKUMASTERID.name(),
        		LCSTables.LCSSKU.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
        //COSTSHEETTOCOLORLINK.IDA3A5 = LCSSKU.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.COSTSHEETTOCOLORLINK.name(), LCSTableColumns.IDA3A5.name(),
        		LCSTables.LCSSKU.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
        //COSTSHEETTOCOLORLINK.IDA3B5 = LCSPRODUCTCOSTSHEET.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.COSTSHEETTOCOLORLINK.name(), LCSTableColumns.IDA3B5.name(),
        		LCSTables.LCSPRODUCTCOSTSHEET.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
	}
	
	/**
	 * Builds the WHERE portion of the query into the prepared statement.<br>
	 * This includes condition criterias that address the following business requirements:<br>
	 * <ul>
	 * <li>1. Products in a Aeropostale Season</li>
	 * <li>2. Development Season is Fall, Holiday, Spring, Summer or BTS</li>
	 * <li>3. Internal Cost Sheet Status is Confirmed on the cost sheet</li>
	 * <li>4. Active Cost Sheet type (not What-if cost sheets)</li>
	 * <li>As well as other supporting criterias such as: Exclude placeholder or inactive colorways.</li>
	 * </ul>
	 * 
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	protected void buildWhere(PreparedQueryStatement pqs) throws WTException {
		
		if (pqs == null) {
			return;
		}
		
        buildWhereActiveCostSheet(pqs);
        buildWhereCostSheetStatus(pqs);
        buildWhereAeroSeason(pqs);
        buildWhereExcludeInvalidColorways(pqs);
        buildWhereLatestFlexObjectIteration(pqs);
        buildWhereLatestFlexObjectVersion(pqs);
        
	}
	
	/**
	 * Builds the WHERE criteria related to requirement: Active Cost Sheet type (not What-If Cost Sheets).
	 * @param pqs The prepared statement to be updated.
	 */
	private void buildWhereActiveCostSheet(PreparedQueryStatement pqs) {
		
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTCOSTSHEET.name(), 
        		LCSTableColumns.WHATIF.name(), 
        		FLEX_COST_SHEET_WHATIF_NO, 
        		Criteria.EQUALS));
		
		pqs.appendAnd();
        pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTCOSTSHEET.name(), 
        		LCSTableColumns.COSTSHEETTYPE.name(), 
        		FLEX_COST_SHEET_TYPE_PRODUCT, 
        		Criteria.EQUALS));
		
	}
	
	/**
	 * Builds the WHERE criteria related to requirement: "Internal Cost Sheet Status is Confirmed on the cost sheet".
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereCostSheetStatus(PreparedQueryStatement pqs) throws WTException {
		
		if (AERO_COST_SHEET_STATUS_CONFIRMED_VALUES.isEmpty()) {
			return;
		}
		
		final FlexType costSheetFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_COST_SHEET_FLEX_TYPE_PATH);
		final String costSheetStatusColName = costSheetFlexType.getAttribute(AERO_COST_SHEET_STATUS_ATTR).getColumnName();
		
		pqs.appendAndIfNeeded();
        pqs.appendInCriteria(LCSTables.LCSPRODUCTCOSTSHEET.name(), 
        		costSheetStatusColName, 
        		AERO_COST_SHEET_STATUS_CONFIRMED_VALUES);
	}
	
	/**
	 * Builds the WHERE criteria to include only valid Aero Seasons.<br>
	 * Valid Aero seasons are: Fall, Holiday, Spring, Summer or BTS.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereAeroSeason(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType seasonFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_SEASON_FLEX_TYPE_PATH);
		
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSEASON.name(), 
				LCSTableColumns.FLEXTYPEIDPATH.name(), 
				seasonFlexType.getIdPath() + "%", 
				Criteria.LIKE));
		
		final String developmentSeasonColName = seasonFlexType.getAttribute(AERO_SEASON_TYPE_ATTR).getColumnName();
		
		if (!AERO_VALID_DEVELOPMENT_SEASONS_VALUES.isEmpty()) {
			pqs.appendAndIfNeeded();
	        pqs.appendInCriteria(LCSTables.LCSSEASON.name(), developmentSeasonColName, AERO_VALID_DEVELOPMENT_SEASONS_VALUES);
		}
		
	}
	
	/**
	 * Builds the WHERE criteria to exclude placeholder, inactive or dropped colorways.
	 * and include only those colorways that exist (are selected) in the cost sheet.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereExcludeInvalidColorways(PreparedQueryStatement pqs) throws WTException {
		
		pqs.appendAndIfNeeded();
		
		//Colorway is not placeholder.
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKU.name(), 
        		LCSTableColumns.PLACEHOLDER.name(), 
        		FLEX_OBJECT_PLACEHOLDER_NO, 
        		Criteria.EQUALS));
		
		pqs.appendAnd();
		
		//Colorway is active in sourcing config-colorway-season.
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSOURCINGLINK.name(), 
        		LCSTableColumns.ACTIVE.name(), 
        		FLEX_OBJECT_ACTIVE_YES, 
        		Criteria.EQUALS));
		
		pqs.appendAnd();
		
		//Colorway Season-Link type is SKU.
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), 
        		LCSTableColumns.SEASONLINKTYPE.name(), 
        		FLEX_SKU_SEASON_LINK_TYPE, 
        		Criteria.EQUALS));
		
		pqs.appendAnd();
		
		//Colorway Season-Link is not season removed.
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), 
        		LCSTableColumns.SEASONREMOVED.name(), 
        		FLEX_SEASON_REMOVED_NO, 
        		Criteria.EQUALS));
		
	}
	
	/**
	 * Builds the WHERE criteria to include latest flex object iteration entries for: 
	 * Sourcing Config, Season, Product, Sourcing to Season Link, Cost Sheet and Colorway.
	 * @param pqs The prepared statement to be updated.
	 */
	private void buildWhereLatestFlexObjectIteration(PreparedQueryStatement pqs) {
		
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCINGCONFIG.name(), 
				LCSTableColumns.LATESTITERATIONINFO.name(), 
				LATEST_FLEX_OBJECT_ITERATION, 
				Criteria.EQUALS));
		pqs.appendAnd();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSEASON.name(), 
				LCSTableColumns.LATESTITERATIONINFO.name(), 
				LATEST_FLEX_OBJECT_ITERATION, 
				Criteria.EQUALS));
		pqs.appendAnd();
		pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCT.name(), 
				LCSTableColumns.LATESTITERATIONINFO.name(), 
				LATEST_FLEX_OBJECT_ITERATION, 
				Criteria.EQUALS));
		pqs.appendAnd();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCETOSEASONLINK.name(), 
				LCSTableColumns.LATESTITERATIONINFO.name(), 
				LATEST_FLEX_OBJECT_ITERATION, 
				Criteria.EQUALS));
		pqs.appendAnd();
		pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTCOSTSHEET.name(), 
				LCSTableColumns.LATESTITERATIONINFO.name(), 
				LATEST_FLEX_OBJECT_ITERATION, 
				Criteria.EQUALS));
		pqs.appendAnd();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKU.name(), 
				LCSTableColumns.LATESTITERATIONINFO.name(), 
				LATEST_FLEX_OBJECT_ITERATION, 
				Criteria.EQUALS));
		pqs.appendAnd();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), 
				LCSTableColumns.EFFECTLATEST.name(), 
				FLEX_OBJECT_EFFECT_LATEST_YES, 
				Criteria.EQUALS));
	}
	
	/**
	 * Builds the WHERE criteria to include latest flex object version entries for:
	 * Sourcing Config, Season, Product, Sourcing to Season Link, Cost Sheet, Colorway and Colorway-Season Link.
	 * @param pqs The prepared statement to be updated.
	 */
	private void buildWhereLatestFlexObjectVersion(PreparedQueryStatement pqs) {
		
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKU.name(), 
				LCSTableColumns.VERSIONIDA2VERSIONINFO.name(), 
				LATEST_FLEX_OBJECT_VERSION, 
				Criteria.EQUALS));
		pqs.appendAnd();
		pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCT.name(), 
				LCSTableColumns.VERSIONIDA2VERSIONINFO.name(), 
				LATEST_FLEX_OBJECT_VERSION, 
				Criteria.EQUALS));
		pqs.appendAnd();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSEASON.name(), 
				LCSTableColumns.VERSIONIDA2VERSIONINFO.name(), 
				LATEST_FLEX_OBJECT_VERSION, 
				Criteria.EQUALS));
		pqs.appendAnd();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCINGCONFIG.name(), 
				LCSTableColumns.VERSIONIDA2VERSIONINFO.name(), 
				LATEST_FLEX_OBJECT_VERSION, 
				Criteria.EQUALS));
		pqs.appendAnd();
		pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTCOSTSHEET.name(), 
				LCSTableColumns.VERSIONIDA2VERSIONINFO.name(), 
				LATEST_FLEX_OBJECT_VERSION, 
				Criteria.EQUALS));
		pqs.appendAnd();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCETOSEASONLINK.name(), 
				LCSTableColumns.VERSIONIDA2VERSIONINFO.name(), 
				LATEST_FLEX_OBJECT_VERSION, 
				Criteria.EQUALS));
		
	}
	
	/**
	 * Builds the prepared query statement to retrieve a list of sourcing config numbers that contain eligible cost sheets for Lucky FC & CAP integration.
	 * @return The prepared query statament built.
	 * @throws WTException If an error occurs while executing the flex api during the building of the various portions of the prepared query statement.
	 */
	public PreparedQueryStatement build() throws WTException {
				
		PreparedQueryStatement pqs = new PreparedQueryStatement();
		
		buildSelect(pqs);
		buildFrom(pqs);
		buildJoins(pqs);
		buildWhere(pqs);
		
		return pqs;
	}
	
}
