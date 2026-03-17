package com.sparc.wc.integration.aero.builders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_S4_SHARED_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_COST_SENT_TO_S4_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SENT_YES_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_SENT_TO_S4_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_STATUS_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_STATUS_CONFIRMED_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_STATUS_DEVELOPMENT_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_STATUS_DROPPED_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.SENT_YES;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.sparc.wc.integration.aero.repository.LCSTables;

import wt.util.WTException;

/**
 * Builds a query (prepared query statement) to obtain the list of product cost sheets from a given sourcing configuration number which are eligible for 
 * Aero S4 integration.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Task #9915 (UAT): Updated to use/check correct flags for S4.
 * - Task #9999 (UAT): Check for sourcing config 'dropped' status.
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9646 Aero - Costing Interface"
 */
public class AeroS4CostingDetailsQueryBuilder extends AeroCostingDetailsQueryBuilder {

	/**
	 * Constructs a AeroS4CostingDetailsQueryBuilder instance.
	 * @param sourcingConfigNumber The sourcing config number to get the list of related eligible cost sheets.
	 */
	public AeroS4CostingDetailsQueryBuilder(Long sourcingConfigNumber) {
		super(sourcingConfigNumber);
	}
	
	/**
	 * Builds the WHERE criteria related to requirement: S4 Shared is Yes at the seasonal colorway level.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereS4Shared(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType colorwaySeasonFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_PRODUCT_FLEX_TYPE_PATH);
		String s4SharedColName = colorwaySeasonFlexType.getAttribute(AERO_COLORWAY_S4_SHARED_ATTR).getColumnName();
		
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), s4SharedColName, SENT_YES, Criteria.EQUALS));
		
	}
	
	/**
	 * Builds the WHERE criteria related to requirement: Cost Sent to S4 is set to No.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereCostSentToS4(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType costSheetFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_COST_SHEET_FLEX_TYPE_PATH);
		String costSentToS4 = costSheetFlexType.getAttribute(AERO_COST_SHEET_COST_SENT_TO_S4_ATTR).getColumnName();
		
		pqs.appendAndIfNeeded();
		pqs.appendOpenParen();
		pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTCOSTSHEET.name(), costSentToS4, "", Criteria.IS_NULL));
		pqs.appendOr();
		pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTCOSTSHEET.name(), costSentToS4, AERO_SENT_YES_VALUE, Criteria.NOT_EQUAL_TO));
		pqs.appendClosedParen();
		
	}
	
	/**
	 * Builds the WHERE criteria related to requirement: Sourcing status is "Development" or "Confirmed" OR "Dropped" if Sourcing Config Sent to S4 is Yes.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereSourcingStatus(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType srcToSeasonFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_SOURCING_CONFIG_FLEX_TYPE_PATH);
		final String srcStatusColName = srcToSeasonFlexType.getAttribute(AERO_SOURCING_STATUS_ATTR).getColumnName();
		final String sourcingConfigSentToS4ColName = srcToSeasonFlexType.getAttribute(AERO_SOURCING_CONFIG_SENT_TO_S4_ATTR).getColumnName();
		
		pqs.appendAnd();
		
		pqs.appendCriteria(new Criteria() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public String getSqlExpression() {
				return "("
						+ LCSTables.LCSSOURCETOSEASONLINK.name() + "." +  srcStatusColName + " IN ('" + AERO_SOURCING_STATUS_CONFIRMED_VALUE + "','" + AERO_SOURCING_STATUS_DEVELOPMENT_VALUE + "')"
						+ " OR "
						+ "(" + new Criteria(LCSTables.LCSSOURCETOSEASONLINK.name(), srcStatusColName, AERO_SOURCING_STATUS_DROPPED_VALUE, Criteria.EQUALS).getSqlExpression()
						+ " AND " + new Criteria(LCSTables.LCSSOURCETOSEASONLINK.name(), sourcingConfigSentToS4ColName, AERO_SENT_YES_VALUE, Criteria.EQUALS).getSqlExpression() + ")"
						+ ")";
			}
			
		});
		
	}
	
	/**
	 * Builds the WHERE portion of the query into the prepared statement.<br>
	 * This includes specific condition criteria for CAP Costing:
	 * <ul>
	 * <li>S4 Shared is Yes at the seasonal colorway level.</li>
	 * <li>Cost Sent to S4 is set to No.</li>
	 * <li>Sourcing status is "Development" or "Confirmed".</li>
	 * </ul>
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	@Override
	protected void buildWhere(PreparedQueryStatement pqs) throws WTException {
		buildWhereS4Shared(pqs);
		buildWhereCostSentToS4(pqs);
		buildWhereSourcingStatus(pqs);
		super.buildWhere(pqs);
	}

}
