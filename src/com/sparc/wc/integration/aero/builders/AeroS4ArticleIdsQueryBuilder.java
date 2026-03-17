package com.sparc.wc.integration.aero.builders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_CHANNEL_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_SENT_TO_S4_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_LIFECYCLE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_VALID_DEVELOPMENT_CHANNEL_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_VALID_COLORWAY_LIFECYCLE_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SENT_YES_VALUE;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.sparc.wc.integration.aero.repository.LCSTables;

import wt.util.WTException;

/**
 * Builds a query (prepared query statement) to obtain the list of colorway numbers eligible for Aero S4 integration.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Updated logic within buildWhereValidColorwayLifecycle, to look for valid colorway lifecycles values.<br>
 * - Task #9917 (UAT): 191 - New attribute "Channel" needed in product ADM and needs to be updated in CAP to Flex interface. 
 * Also, this attribute should trigger the flex to S4 interface instead of the "Dev Channel" attribute.<br> 
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9645 Aero - Article Interface"
 */
public class AeroS4ArticleIdsQueryBuilder extends AeroArticleIdsQueryBuilder {

	public AeroS4ArticleIdsQueryBuilder() {
		
	}
	
	/**
	 * Builds the WHERE criteria related to requirement:
	 * Development Channel has at least one of these values in the multi-list: Club, Discounters, or Wholesale Full Price.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereValidColorwayDevelopmentChannel(PreparedQueryStatement pqs) throws WTException {
		
		if (AERO_VALID_DEVELOPMENT_CHANNEL_VALUES.isEmpty()) {
			return;
		}
		
		final FlexType colorwaySeasonFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_PRODUCT_FLEX_TYPE_PATH);
		String colorwayChannelColName = colorwaySeasonFlexType.getAttribute(AERO_COLORWAY_CHANNEL_ATTR).getColumnName();
		boolean isFirst = true;
		
		pqs.appendAndIfNeeded();
		pqs.appendOpenParen();
		
		for (String devChannel : AERO_VALID_DEVELOPMENT_CHANNEL_VALUES) {
			
			if (!isFirst) {
				pqs.appendOr();
			} else {
				isFirst = false;
			}
			
			pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), colorwayChannelColName, "%" + devChannel + "%", Criteria.LIKE));
			
		}
		
		pqs.appendClosedParen();
		
	}
	
	/**
	 * Builds the WHERE criteria related to requirement: Colorway Lifecycle status is not null – (Released, Sample Ready, Buy Ready or Dropped).
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereValidColorwayLifecycle(PreparedQueryStatement pqs) throws WTException {
		
		if (AERO_VALID_COLORWAY_LIFECYCLE_VALUES.isEmpty()) {
			return;
		}
		
		final FlexType colorwaySeasonFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_PRODUCT_FLEX_TYPE_PATH);
		String colorwayLifecycleColName = colorwaySeasonFlexType.getAttribute(AERO_COLORWAY_LIFECYCLE_ATTR).getColumnName();
		
		pqs.appendAndIfNeeded();
        pqs.appendInCriteria(LCSTables.LCSSKUSEASONLINK.name(), colorwayLifecycleColName, AERO_VALID_COLORWAY_LIFECYCLE_VALUES);
		
	}
	
	/**
	 * Builds the WHERE criteria related to requirement: The Colorway hasn't been sent to S4: Sent to S4 is set to No.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereColorwaySentFlag(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType colorwaySeasonFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_PRODUCT_FLEX_TYPE_PATH);
		String sentToS4ColName = colorwaySeasonFlexType.getAttribute(AERO_COLORWAY_SENT_TO_S4_ATTR).getColumnName();
		
		pqs.appendAndIfNeeded();
		pqs.appendOpenParen();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), sentToS4ColName, "", Criteria.IS_NULL));
		pqs.appendOr();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), sentToS4ColName, AERO_SENT_YES_VALUE, Criteria.NOT_EQUAL_TO));
		pqs.appendClosedParen();
		
	}
	
	/**
	 * Builds the WHERE portion of the query into the prepared statement.<br>
	 * This includes specific condition criterias for S4:
	 * <ul>
	 * <li>Colorway Lifecycle status is not null – (Released, Sample Ready, Buy Ready or Dropped).</li>
	 * <li>Development Channel has at least one of these values in the multi-list: Club, Discounters, or Wholesale Full Price.</li>
	 * <li>Sent to S4 is set to No.</li>
	 * </ul>
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	@Override
	protected void buildWhere(PreparedQueryStatement pqs) throws WTException {
		buildWhereValidColorwayLifecycle(pqs);
		buildWhereValidColorwayDevelopmentChannel(pqs);
		buildWhereColorwaySentFlag(pqs);
		super.buildWhere(pqs);
	}

}
