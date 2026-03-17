package com.sparc.wc.integration.aero.builders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_CAP_SHARED_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COSTING_VALID_SOURCING_STATUS_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_COST_SENT_TO_CAP_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SENT_YES_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_STATUS_ATTR;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.sparc.wc.integration.aero.repository.LCSTables;

import wt.util.WTException;

/**
 * Builds a query (prepared query statement) to obtain the list of product cost sheets from a given sourcing configuration number which are eligible for 
 * Aero CAP integration.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9646 Aero - Costing Interface"
 */
public class AeroCAPCostingDetailsQueryBuilder extends AeroCostingDetailsQueryBuilder {
	
	/**
	 * Constructs a AeroCAPCostingDetailsQueryBuilder instance.
	 * @param sourcingConfigNumber The sourcing config number to get the list of related eligible cost sheets.
	 */
	public AeroCAPCostingDetailsQueryBuilder(Long sourcingConfigNumber) {
		super(sourcingConfigNumber);
	}
	
	/**
	 * Builds the WHERE criteria related to requirement: CAP Shared is Yes at the seasonal colorway level.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereCAPShared(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType colorwaySeasonFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_PRODUCT_FLEX_TYPE_PATH);
		String capSharedColName = colorwaySeasonFlexType.getAttribute(AERO_COLORWAY_CAP_SHARED_ATTR).getColumnName();
		
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), capSharedColName, AERO_SENT_YES_VALUE, Criteria.EQUALS));
		
	}
	
	/**
	 * Builds the WHERE criteria related to requirement: Cost Sent to CAP is set to No.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereCostSentToCAP(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType costSheetFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_COST_SHEET_FLEX_TYPE_PATH);
		String costSentToCAP = costSheetFlexType.getAttribute(AERO_COST_SHEET_COST_SENT_TO_CAP_ATTR).getColumnName();
		
		pqs.appendAndIfNeeded();
		pqs.appendOpenParen();
		pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTCOSTSHEET.name(), costSentToCAP, "", Criteria.IS_NULL));
		pqs.appendOr();
		pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTCOSTSHEET.name(), costSentToCAP, AERO_SENT_YES_VALUE, Criteria.NOT_EQUAL_TO));
		pqs.appendClosedParen();
		
	}
	
	/**
	 * Builds the WHERE criteria related to requirement: Sourcing status is "Development" or "Confirmed".
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereSourcingStatus(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType srcToSeasonFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_SOURCING_CONFIG_FLEX_TYPE_PATH);
		final String srcStatusColName = srcToSeasonFlexType.getAttribute(AERO_SOURCING_STATUS_ATTR).getColumnName();
		
		pqs.appendAndIfNeeded();
		pqs.appendInCriteria(LCSTables.LCSSOURCETOSEASONLINK.name(), 
				srcStatusColName, 
				AERO_COSTING_VALID_SOURCING_STATUS_VALUES);
		
	}
	
	/**
	 * Builds the WHERE portion of the query into the prepared statement.<br>
	 * This includes specific condition criterias for CAP Costing:
	 * <ul>
	 * <li>CAP Shared is Yes at the seasonal colorway level.</li>
	 * <li>Cost Sent to CAP is set to No.</li>
	 * <li>Sourcing status is "Development" or "Confirmed".</li>
	 * </ul>
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	@Override
	protected void buildWhere(PreparedQueryStatement pqs) throws WTException {
		buildWhereCAPShared(pqs);
		buildWhereCostSentToCAP(pqs);
		buildWhereSourcingStatus(pqs);
		super.buildWhere(pqs);
	}

}
