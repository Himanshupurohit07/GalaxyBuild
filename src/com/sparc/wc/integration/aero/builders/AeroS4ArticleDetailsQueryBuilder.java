package com.sparc.wc.integration.aero.builders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_FLEX_TYPE_PATH;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.sparc.wc.integration.aero.repository.LCSTableColumns;
import com.sparc.wc.integration.aero.repository.LCSTables;

import wt.util.WTException;

/**
 * Builds a query (prepared query statement) to obtain flex object keys relevant to extract article details for Aero S4 integration.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9645 Aero - Article Interface"
 */
public class AeroS4ArticleDetailsQueryBuilder extends AeroS4ArticleIdsQueryBuilder {
	
	private String colorwayNumber;
	
	/**
	 * Constructs an article details query instance with the given colorway number.
	 * @param colorwayNumber The colorway number to query.
	 */
	public AeroS4ArticleDetailsQueryBuilder(String colorwayNumber) {
		this.colorwayNumber = colorwayNumber;
	}
	
	@Override
	protected void buildSelect(PreparedQueryStatement pqs) throws WTException {

		if (pqs == null) {
			return;
		}
		
		//Flex Object internal IDs
		pqs.appendSelectColumn(LCSTables.LCSSKU.name(), LCSTableColumns.IDA2A2.name());
		pqs.appendSelectColumn(LCSTables.LCSPRODUCT.name(), LCSTableColumns.IDA2A2.name());
		pqs.appendSelectColumn(LCSTables.LCSSEASON.name(), LCSTableColumns.IDA2A2.name());
		pqs.appendSelectColumn(LCSTables.LCSSKUSEASONLINK.name(), LCSTableColumns.IDA2A2.name());
		pqs.appendSelectColumn(LCSTables.LCSPRODUCTSEASONLINK.name(), LCSTableColumns.IDA2A2.name());
		
	}
	
	/**
	 * Builds the WHERE criteria for the Colorway Number.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereColorwayNo(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType colorwayFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_PRODUCT_FLEX_TYPE_PATH);
		final String colorwayNumColName = colorwayFlexType.getAttribute(AERO_COLORWAY_NUMBER_ATTR).getColumnName();
		
		pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSKU.name(), 
        		colorwayNumColName, 
        		((colorwayNumber != null) ? colorwayNumber.toString() : null), 
        		Criteria.EQUALS));
		
	}

	@Override
	protected void buildWhere(PreparedQueryStatement pqs) throws WTException {
		buildWhereColorwayNo(pqs);
		super.buildWhere(pqs);
	}
	
}
