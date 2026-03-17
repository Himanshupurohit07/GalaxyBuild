package com.sparc.wc.integration.aero.builders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_OBJECT_EFFECT_LATEST_YES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LATEST_FLEX_OBJECT_ITERATION;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LATEST_FLEX_OBJECT_VERSION;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_TYPE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLOR_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_VALID_DEVELOPMENT_SEASONS_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_OBJECT_PLACEHOLDER_NO;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_PRODUCT_SEASON_LINK_TYPE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_SEASON_REMOVED_NO;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_SKU_SEASON_LINK_TYPE;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.sparc.wc.integration.aero.repository.LCSTableColumns;
import com.sparc.wc.integration.aero.repository.LCSTables;

import wt.util.WTException;

/**
 * Builds a query (prepared query statement) to obtain the list of colorway numbers eligible for Aero S4 or CAP integrations.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Changed Aero color type criteria to use EQUALS instead of LIKE at buildWhereAeroColorwayColor method to improve query performance.<br>
 * - Removed sort-by method to improve query performance.<br>
 * - Enhanced query condition to prevent pulling season-removed sku and product season links.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9645 Aero - Article Interface"
 */
public abstract class AeroArticleIdsQueryBuilder {
	
	/**
	 * Constructs a AeroArticleIdsQueryBuilder instance.
	 */
	public AeroArticleIdsQueryBuilder() {
		
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
		
		//SELECT: scColorwayNo (Colorway #)
		final FlexType colorwayFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_PRODUCT_FLEX_TYPE_PATH);
		final String colorwayNumColName = colorwayFlexType.getAttribute(AERO_COLORWAY_NUMBER_ATTR).getColumnName();
		
		pqs.appendSelectColumn(LCSTables.LCSSKU.name(), colorwayNumColName);
		
	}
	
	/**
	 * Builds the FROM portion of the query into the prepared statement.
	 * @param pqs The prepared statement to be updated.
	 */
	protected void buildFrom(PreparedQueryStatement pqs) {
		
		if (pqs == null) {
			return;
		}
		
        pqs.appendFromTable(LCSTables.LCSPRODUCT.name());
        pqs.appendFromTable(LCSTables.LCSSEASON.name()); 
        pqs.appendFromTable(LCSTables.LCSSKU.name());
        pqs.appendFromTable(LCSTables.LCSSKUSEASONLINK.name());
        pqs.appendFromTable(LCSTables.LCSPRODUCTSEASONLINK.name());
        pqs.appendFromTable(LCSTables.LCSCOLOR.name());
        
	}
	
	/**
	 * Builds the table JOINS portion of the query into the prepared statement.
	 * @param pqs The prepared statement to be updated.
	 */
	protected void buildJoins(PreparedQueryStatement pqs) {
		
		if (pqs == null) {
			return;
		}
		
        //LCSSKUSEASONLINK.PRODUCTMASTERID <-> LCSPRODUCT.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.LCSSKUSEASONLINK.name(), LCSTableColumns.PRODUCTMASTERID.name(),
        		LCSTables.LCSPRODUCT.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
        //LCSSKUSEASONLINK.IDA3B5 <-> LCSSEASON.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.LCSSKUSEASONLINK.name(), LCSTableColumns.IDA3B5.name(),
        		LCSTables.LCSSEASON.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
        //LCSSKUSEASONLINK.SKUMASTERID <-> LCSSKU.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.LCSSKUSEASONLINK.name(), LCSTableColumns.SKUMASTERID.name(),
        		LCSTables.LCSSKU.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
        //LCSPRODUCTSEASONLINK.PRODUCTMASTERID <-> LCSPRODUCT.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.LCSPRODUCTSEASONLINK.name(), LCSTableColumns.PRODUCTMASTERID.name(),
        		LCSTables.LCSPRODUCT.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
        //LCSPRODUCTSEASONLINK.IDA3B5 = LCSSEASON.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.LCSPRODUCTSEASONLINK.name(), LCSTableColumns.IDA3B5.name(),
        		LCSTables.LCSSEASON.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
        //LCSSKU.IDA3A2TYPEINFOLCSSKU = LCSCOLOR.IDA2A2
        pqs.appendJoin(LCSTables.LCSSKU.name(), LCSTableColumns.IDA3A2TYPEINFOLCSSKU.name(),
        		LCSTables.LCSCOLOR.name(), LCSTableColumns.IDA2A2.name());
        
	}
	
	/**
	 * Builds the WHERE portion of the query into the prepared statement.<br>
	 * This includes common condition criterias that address the following business requirements for both CAP & S4:<br>
	 * <ul>
	 * <li>1. Products in a Aeropostale Season.</li>
	 * <li>2. Development Season is Fall, Holiday, Spring, Summer or BTS.</li>
	 * </ul>
	 * See relevant sub-classes related to CAP or S4 to view specific query criterias applicable to each. 
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	protected void buildWhere(PreparedQueryStatement pqs) throws WTException {
		
		if (pqs == null) {
			return;
		}
		
        buildWhereAeroSeason(pqs);
        buildWhereAeroColorwayColor(pqs);
        buildWhereExcludeInvalidColorways(pqs);
        buildWhereLatestFlexObjectIteration(pqs);
        buildWhereLatestFlexObjectVersion(pqs);
        
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
	 * Builds the WHERE criteria to include only valid Aero Colorway Colors.<br>
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereAeroColorwayColor(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType colorFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_COLOR_FLEX_TYPE_PATH);
		
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSCOLOR.name(), 
				LCSTableColumns.FLEXTYPEIDPATH.name(), 
				colorFlexType.getIdPath(), 
				Criteria.EQUALS));
		
	}
	
	/**
	 * Builds the WHERE criteria to exclude placeholder, inactive or dropped colorways.
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
		
		pqs.appendAnd();
		
		//Product Season-Link type is PRODUCT
		pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTSEASONLINK.name(), 
        		LCSTableColumns.SEASONLINKTYPE.name(), 
        		FLEX_PRODUCT_SEASON_LINK_TYPE, 
        		Criteria.EQUALS));
		
		pqs.appendAnd();
		
		//Product Season-Link type is not season removed.
		pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTSEASONLINK.name(), 
        		LCSTableColumns.SEASONREMOVED.name(), 
        		FLEX_SEASON_REMOVED_NO, 
        		Criteria.EQUALS));
		
	}
	
	/**
	 * Builds the WHERE criteria to include latest flex object iteration entries for: 
	 * Sourcing Config, Season, Product, Sourcing to Season Link, Cost Sheet, Colorway, Colorway-Season Link, Product-Season Link and 
	 * Source-to-Season-Link-Master.
	 * @param pqs The prepared statement to be updated.
	 */
	private void buildWhereLatestFlexObjectIteration(PreparedQueryStatement pqs) {
		
		pqs.appendAndIfNeeded();
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
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKU.name(), 
				LCSTableColumns.LATESTITERATIONINFO.name(), 
				LATEST_FLEX_OBJECT_ITERATION, 
				Criteria.EQUALS));
		pqs.appendAnd();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), 
				LCSTableColumns.EFFECTLATEST.name(), 
				FLEX_OBJECT_EFFECT_LATEST_YES, 
				Criteria.EQUALS));
		pqs.appendAnd();
		pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTSEASONLINK.name(), 
				LCSTableColumns.EFFECTLATEST.name(), 
				FLEX_OBJECT_EFFECT_LATEST_YES, 
				Criteria.EQUALS));
	}
	
	/**
	 * Builds the WHERE criteria to include latest flex object version entries for:
	 * Sourcing Config, Season, Product, Sourcing to Season Link, Cost Sheet and Colorway.
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
		
	}
	
	/**
	 * Builds the prepared query statement to retrieve a list of colorway numbers that contain eligible cost sheets for Lucky FC & CAP integration.
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
