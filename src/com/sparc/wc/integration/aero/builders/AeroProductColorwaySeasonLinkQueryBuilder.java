package com.sparc.wc.integration.aero.builders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_TYPE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_VALID_DEVELOPMENT_SEASONS_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_OBJECT_EFFECT_LATEST_YES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_SEASON_REMOVED_NO;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_SKU_SEASON_LINK_TYPE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LATEST_FLEX_OBJECT_ITERATION;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LATEST_FLEX_OBJECT_VERSION;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.sparc.wc.integration.aero.repository.LCSTableColumns;
import com.sparc.wc.integration.aero.repository.LCSTables;

import wt.util.WTException;

/**
 * Builds a query (prepared query statement) to obtain the list of colorway season links for the given product number.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Added filter condition to ensure only SKU season link type is retrieved. Added method "buildWhereColorwaySeasonLinkType".
 * - Added filter condition to ensure only active SKU season links are retrieved. Added method "buildWhereActiveColorwaySeasonLinkType".
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9645 Aero - Article Interface"
 */
public class AeroProductColorwaySeasonLinkQueryBuilder {
	
	private Long productNumber;
	
	public AeroProductColorwaySeasonLinkQueryBuilder(Long productNumber) {
		this.productNumber = productNumber;
	}
	
	/**
	 * Builds the SELECT portion of the query into the prepared statement.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildSelect(PreparedQueryStatement pqs) throws WTException {
		pqs.setDistinct(true);
		pqs.appendSelectColumn(LCSTables.LCSSKUSEASONLINK.name(), LCSTableColumns.IDA2A2.name());
	}
	
	/**
	 * Builds the FROM portion of the query into the prepared statement.
	 * @param pqs The prepared statement to be updated.
	 */
	private void buildFrom(PreparedQueryStatement pqs) {
		pqs.appendFromTable(LCSTables.LCSPRODUCT.name());
        pqs.appendFromTable(LCSTables.LCSSEASON.name()); 
        pqs.appendFromTable(LCSTables.LCSSKUSEASONLINK.name());
	}
	
	/**
	 * Builds the table JOINS portion of the query into the prepared statement.
	 * @param pqs The prepared statement to be updated.
	 */
	private void buildJoins(PreparedQueryStatement pqs) {
		
		//LCSSKUSEASONLINK.PRODUCTMASTERID <-> LCSPRODUCT.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.LCSSKUSEASONLINK.name(), LCSTableColumns.PRODUCTMASTERID.name(),
        		LCSTables.LCSPRODUCT.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
        //LCSSKUSEASONLINK.IDA3B5 <-> LCSSEASON.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.LCSSKUSEASONLINK.name(), LCSTableColumns.IDA3B5.name(),
        		LCSTables.LCSSEASON.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
		
	}
	
	/**
	 * Builds the WHERE portion of the query into the prepared statement. 
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhere(PreparedQueryStatement pqs) throws WTException {
		buildWhereProductNumber(pqs);
		buildWhereAeroSeason(pqs);
		buildWhereColorwaySeasonLinkType(pqs);
		buildWhereActiveColorwaySeasonLinkType(pqs);
        buildWhereLatestFlexObjectIteration(pqs);
        buildWhereLatestFlexObjectVersion(pqs);
	}
	
	/**
	 * Builds the WHERE criteria for the Product Number.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereProductNumber(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType productFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_PRODUCT_FLEX_TYPE_PATH);
		final String productNumColName = productFlexType.getAttribute(AERO_PRODUCT_NUMBER_ATTR).getColumnName();
		
		pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCT.name(), 
        		productNumColName, 
        		((productNumber != null) ? productNumber.toString() : null), 
        		Criteria.EQUALS));
		
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
	 * Builds the WHERE criteria to include SKU type season links.<br>
	 * @param pqs The prepared statement to be updated.
	 */
	private void buildWhereColorwaySeasonLinkType(PreparedQueryStatement pqs) {
		
		pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), 
        		LCSTableColumns.SEASONLINKTYPE.name(), 
        		FLEX_SKU_SEASON_LINK_TYPE, 
        		Criteria.EQUALS));
		
	}
	
	/**
	 * Builds the WHERE criteria to include active colorway season links.<br>
	 * @param pqs The prepared statement to be updated.
	 */
	private void buildWhereActiveColorwaySeasonLinkType(PreparedQueryStatement pqs) {
		
		pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), 
        		LCSTableColumns.SEASONREMOVED.name(), 
        		FLEX_SEASON_REMOVED_NO, 
        		Criteria.EQUALS));
		
	}
	
	/**
	 * Builds the WHERE criteria to include latest flex object iteration entries. 
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
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), 
				LCSTableColumns.EFFECTLATEST.name(), 
				FLEX_OBJECT_EFFECT_LATEST_YES, 
				Criteria.EQUALS));
	}
	
	/**
	 * Builds the WHERE criteria to include latest flex object version entries.
	 * @param pqs The prepared statement to be updated.
	 */
	private void buildWhereLatestFlexObjectVersion(PreparedQueryStatement pqs) {
		
		pqs.appendAndIfNeeded();
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
	 * Builds the prepared query statement to retrieve a list of colorway season links identifiers for the.
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
