package com.sparc.wc.integration.aero.builders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_SENT_TO_CAP_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PLM_COLORWAY_STATUS_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PLM_COLORWAY_STATUS_DEVELOPMENT_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PLM_COLORWAY_STATUS_DROPPED_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_CAP_SHARED_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SENT_YES_VALUE;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.sparc.wc.integration.aero.repository.LCSTables;

import wt.util.WTException;

/**
 * Builds a query (prepared query statement) to obtain the list of colorway numbers eligible for Aero CAP integration.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9645 Aero - Article Interface"
 */
public class AeroCAPArticleIdsQueryBuilder extends AeroArticleIdsQueryBuilder {

	public AeroCAPArticleIdsQueryBuilder() {
		
	}
	
	/**
	 * Builds the WHERE criteria related to requirement:<br>
	 * PLM Colorway status is DEVELOPMENT OR DROPPED AND CAP Shared is Yes.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWherePLMColorwayStatus(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType colorwaySeasonFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_PRODUCT_FLEX_TYPE_PATH);
		String plmColorwayStatusColName = colorwaySeasonFlexType.getAttribute(AERO_PLM_COLORWAY_STATUS_ATTR).getColumnName();
		String capSharedColName = colorwaySeasonFlexType.getAttribute(AERO_COLORWAY_CAP_SHARED_ATTR).getColumnName();
		
		pqs.appendAndIfNeeded();
		pqs.appendOpenParen(); //outer parenthesis open.
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), 
				plmColorwayStatusColName, 
				AERO_PLM_COLORWAY_STATUS_DEVELOPMENT_VALUE, 
				Criteria.EQUALS));
		pqs.appendOr();
		pqs.appendOpenParen(); //inner parenthesis open.
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), 
				plmColorwayStatusColName, 
				AERO_PLM_COLORWAY_STATUS_DROPPED_VALUE, 
				Criteria.EQUALS));
		pqs.appendAnd();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), capSharedColName, AERO_SENT_YES_VALUE, Criteria.EQUALS));
		pqs.appendClosedParen(); //inner parenthesis close.
		pqs.appendClosedParen(); //outer parenthesis close.
	}
	
	/**
	 * Builds the WHERE criteria related to requirement:<br>
	 * The Colorway hasn't been sent to CAP: Sent to CAP is set to No.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereColorwaySentFlag(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType colorwaySeasonFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_PRODUCT_FLEX_TYPE_PATH);
		String sentToCAPColName = colorwaySeasonFlexType.getAttribute(AERO_COLORWAY_SENT_TO_CAP_ATTR).getColumnName();
		
		pqs.appendAndIfNeeded();
		pqs.appendOpenParen();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), sentToCAPColName, "", Criteria.IS_NULL));
		pqs.appendOr();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), sentToCAPColName, AERO_SENT_YES_VALUE, Criteria.NOT_EQUAL_TO));
		pqs.appendClosedParen();
		
	}
	
	/**
	 * Builds the WHERE portion of the query into the prepared statement.<br>
	 * This includes specific condition criterias for CAP:
	 * <ul>
	 * <li>PLM Colorway status is DEVELOPMENT OR DROPPED AND CAP Shared is Yes.</li>
	 * <li>Sent to CAP is set to No.</li>
	 * </ul>
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	@Override
	protected void buildWhere(PreparedQueryStatement pqs) throws WTException {
		buildWherePLMColorwayStatus(pqs);
		buildWhereColorwaySentFlag(pqs);
		super.buildWhere(pqs);
	}
	
}
