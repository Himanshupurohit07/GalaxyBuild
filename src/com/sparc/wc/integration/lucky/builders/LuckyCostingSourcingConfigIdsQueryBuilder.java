package com.sparc.wc.integration.lucky.builders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SOURCING_NO_ATTR;

import java.util.Arrays;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SHEET_STATUS_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SOURCING_STATUS_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SENT_TO_FC_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SEASON_DEVELOPMENT_SEASON_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SENT_TO_CAP_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COLORWAY_SEASON_SENT_TO_FC_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COLORWAY_SEASON_SENT_TO_CAP_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SHEET_STATUS_CONFIRMED_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SHEET_TYPE_PRODUCT_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SHEET_WHATIF_NO_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SOURCING_STATUS_CONFIRMED_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SOURCING_STATUS_DEVELOPMENT_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SENT_YES_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_PLACEHOLDER_NO_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_ACTIVE_YES_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SKIP_DEVELOPMENT_SEASONS_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SOURCING_CONFIG_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SHEET_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_PRODUCT_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SEASON_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LATEST_FLEX_OBJECT_VERSION;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_OBJECT_EFFECT_LATEST_YES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LATEST_FLEX_OBJECT_ITERATION;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COST_SENT_TO_FC_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COST_SENT_TO_CAP_ATTR;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.sparc.wc.integration.lucky.domain.LuckyCostingProcessesParam;

import wt.util.WTException;

/**
 * Builds a query (prepared query statement) to obtain the list of sourcing config numbers that contain eligible cost sheets for Lucky FC & CAP integration.<br>
 * FIXES/AMENDMENTS:<br>
 * - Task #9232 (UAT): Use colorway-season flags to check for sent to FC or CAP instead of using "New vs Update" flag on colorway/product.<br>
 * - Task #9265 (UAT): Ensure only those colorways selected within the cost sheet are eligible to be sent to the integration.<br>
 * - Task #10565: Add missing join between lcsskusourcinglink and lcsseason tables to prevent bad dups to appear when same colorway is used in multiple season.<br>
 * - Added column IDA3A7 to LCSTableColumns enum.<br>
 * - Removed sort-by method to improve query performance.<br>
 * - Enhanced query to link colorways associated to cost sheets by making use of table "COSTSHEETTOCOLORLINK" and removing potentially ambiguous LIKE condition.<br>
 * - Updated methods "buildFrom", "buildJoins" and "buildWhereExcludeInvalidColorways".<br>
 * - Added table COSTSHEETTOCOLORLINK to LCSTables enum.<br>
 * 
 * @author Acnovate
 * @see "Task #8259 3.1 Costing API - FC & CAP (Outbound)"
 */
public class LuckyCostingSourcingConfigIdsQueryBuilder {
	
	//Enumeration for out-of-the-box flex tables used in the query.
	private static enum LCSTables {
		LCSSOURCINGCONFIGMASTER,
		LCSSOURCETOSEASONLINKMASTER,
		LCSPRODUCT,
		LCSSEASON,
		LCSSOURCINGCONFIG,
		LCSSOURCETOSEASONLINK,
		LCSCOSTSHEETMASTER,
		LCSPRODUCTCOSTSHEET,
		LCSSKUSOURCINGLINK,
		LCSSKU,
		LCSSKUSEASONLINK,
		COSTSHEETTOCOLORLINK
	}
	
	//Enumeration for out-of-the-box flex table columns used in the query.
	private static enum LCSTableColumns {
		IDA3A6,
		IDA3MASTERREFERENCE,
		IDA3B6,
		IDA2A2,
		IDA3C6,
		IDA3B5,
		IDA3A5,
		COSTSHEETTYPE,
		WHATIF,
		FLEXTYPEIDPATH,
		PLACEHOLDER,
		ACTIVE,
		LATESTITERATIONINFO,
		VERSIONIDA2VERSIONINFO,
		PRODUCTMASTERID,
		SKUMASTERID,
		EFFECTLATEST,
		APPLICABLECOLORNAMES,
		SEASONREMOVED,
		IDA3A7
	}
	
	private LuckyCostingProcessesParam process;
	
	/**
	 * Constructs a LuckyCostingSourcingConfigIdsQueryBuilder instance.
	 * @param process The identifier for the lucky target system, i.e. FC or CAP. This will be used to determine part of the SQL condition criteria. 
	 */
	public LuckyCostingSourcingConfigIdsQueryBuilder(LuckyCostingProcessesParam process) {
		this.process = process;
	}
	
	/**
	 * Builds the SELECT portion of the query into the prepared statement.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildSelect(PreparedQueryStatement pqs) throws WTException {
		
		if (pqs == null) {
			return;
		}
		
		pqs.setDistinct(true);
		
		//SELECT: scSourcingNo (Sourcing Config #)
		final FlexType srcConfigFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_SOURCING_CONFIG_FLEX_TYPE_PATH);
		final String srcConfigNumColName = srcConfigFlexType.getAttribute(LUCKY_COSTING_SOURCING_NO_ATTR).getColumnName();
		
		pqs.appendSelectColumn(LCSTables.LCSSOURCINGCONFIG.name(), srcConfigNumColName);
		
	}
	
	/**
	 * Builds the FROM portion of the query into the prepared statement.
	 * @param pqs The prepared statement to be updated.
	 */
	private void buildFrom(PreparedQueryStatement pqs) {
		
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
	private void buildJoins(PreparedQueryStatement pqs) {
		
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
	 * <li>b. The Colorway has previously been sent to Full Circle (if process is FC) or CAP (if process is CAP).</li>
	 * <li>c. "Internal Luck Cost Sheet Status" attribute is "Confirmed".</li>
	 * <li>d. Active Cost Sheet type (not What-If Cost Sheets)</li>
	 * <li>e. Sourcing Status is "Development" or "Confirmed"</li>
	 * <li>f. "Cost Sent to FC" flag is "No" (if process is FC) OR "Cost Sent to CAP" flag is "No" (if process is CAP)</li>
	 * <li>As well as other supporting criterias such as: Include only Lucky Seasons and exclude placeholder or inactive colorways.</li>
	 * </ul>
	 * Note: The item "a." originally meant to check for product has been removed as condition per the latest requirements.
	 * 
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhere(PreparedQueryStatement pqs) throws WTException {
		
		if (pqs == null) {
			return;
		}
		
        buildWhereColorwaySentFlag(pqs);
        buildWhereCostSheetStatus(pqs);
        buildWhereActiveCostSheet(pqs);
        buildWhereSourcingStatus(pqs);
        buildWhereCostSentFlag(pqs);
        buildWhereLuckySeason(pqs);
        buildWhereExcludeInvalidColorways(pqs);
        buildWhereLatestFlexObjectIteration(pqs);
        buildWhereLatestFlexObjectVersion(pqs);
        
	}
	
	/**
	 * Builds the WHERE criteria related to requirement:<br>
	 * The Colorway has previously been sent to either FC (if process is FC) or CAP (if process is CAP).
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereColorwaySentFlag(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType colorwaySeasonFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_PRODUCT_FLEX_TYPE_PATH);
		
		if (process == LuckyCostingProcessesParam.FC) {
			
			String sentToFCColName = colorwaySeasonFlexType.getAttribute(LUCKY_COSTING_COLORWAY_SEASON_SENT_TO_FC_ATTR).getColumnName();
			
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), sentToFCColName, LUCKY_COSTING_SENT_YES_VALUE, Criteria.EQUALS));
			
		} else if (process == LuckyCostingProcessesParam.CAP) {
			
			String sentToCAPColName = colorwaySeasonFlexType.getAttribute(LUCKY_COSTING_COLORWAY_SEASON_SENT_TO_CAP_ATTR).getColumnName();
			
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), sentToCAPColName, LUCKY_COSTING_SENT_YES_VALUE, Criteria.EQUALS));
			
		}
		
	}
	
	/**
	 * Builds the WHERE criteria related to requirement: "Internal Luck Cost Sheet Status" attribute is "Confirmed".
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereCostSheetStatus(PreparedQueryStatement pqs) throws WTException {
		
		if (LUCKY_COSTING_COST_SHEET_STATUS_CONFIRMED_VALUES.isEmpty()) {
			return;
		}
		
		final FlexType costSheetFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_COST_SHEET_FLEX_TYPE_PATH);
		final String costSheetStatusColName = costSheetFlexType.getAttribute(LUCKY_COSTING_COST_SHEET_STATUS_ATTR).getColumnName();
		
		pqs.appendAndIfNeeded();
        pqs.appendInCriteria(LCSTables.LCSPRODUCTCOSTSHEET.name(), 
        		costSheetStatusColName, 
        		LUCKY_COSTING_COST_SHEET_STATUS_CONFIRMED_VALUES);
	}
	
	/**
	 * Builds the WHERE criteria related to requirement: Active Cost Sheet type (not What-If Cost Sheets).
	 * @param pqs The prepared statement to be updated.
	 */
	private void buildWhereActiveCostSheet(PreparedQueryStatement pqs) {
		
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTCOSTSHEET.name(), 
        		LCSTableColumns.WHATIF.name(), 
        		LUCKY_COSTING_COST_SHEET_WHATIF_NO_VALUE, 
        		Criteria.EQUALS));
		
		pqs.appendAnd();
        pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTCOSTSHEET.name(), 
        		LCSTableColumns.COSTSHEETTYPE.name(), 
        		LUCKY_COSTING_COST_SHEET_TYPE_PRODUCT_VALUE, 
        		Criteria.EQUALS));
		
	}
	
	/**
	 * Builds the WHERE criteria related to requirement: Active Cost Sheet type (not What-If Cost Sheets).
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereSourcingStatus(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType srcToSeasonFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_SOURCING_CONFIG_FLEX_TYPE_PATH);
		final String srcStatusColName = srcToSeasonFlexType.getAttribute(LUCKY_COSTING_SOURCING_STATUS_ATTR).getColumnName();
		
		pqs.appendAndIfNeeded();
		pqs.appendInCriteria(LCSTables.LCSSOURCETOSEASONLINK.name(), 
				srcStatusColName, 
				Arrays.asList(new String[] {LUCKY_COSTING_SOURCING_STATUS_CONFIRMED_VALUE, LUCKY_COSTING_SOURCING_STATUS_DEVELOPMENT_VALUE}));
		
	}
	
	/**
	 * Builds the WHERE criteria related to requirement: 
	 * "Cost Sent to FC" flag is "No" (if process is FC) OR "Cost Sent to CAP" flag is "No" (if process is CAP).
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereCostSentFlag(PreparedQueryStatement pqs) throws WTException {

		//Lucky Enhancement - Start
		final FlexType srcConfigFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_COST_SHEET_FLEX_TYPE_PATH);

		if (process == LuckyCostingProcessesParam.FC) {

			String sentToFCColName = srcConfigFlexType.getAttribute(LUCKY_COST_SENT_TO_FC_ATTR).getColumnName();

			pqs.appendAndIfNeeded();
			pqs.appendOpenParen();
			pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTCOSTSHEET.name(), sentToFCColName, "", Criteria.IS_NULL));
			pqs.appendOr();
			pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTCOSTSHEET.name(), sentToFCColName, LUCKY_COSTING_SENT_YES_VALUE, Criteria.NOT_EQUAL_TO));
			pqs.appendClosedParen();

		} else if (process == LuckyCostingProcessesParam.CAP) {

			String sentToCAPColName = srcConfigFlexType.getAttribute(LUCKY_COST_SENT_TO_CAP_ATTR).getColumnName();
			pqs.appendAndIfNeeded();
			pqs.appendOpenParen();
			pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTCOSTSHEET.name(), sentToCAPColName, "", Criteria.IS_NULL));
			pqs.appendOr();
			pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCTCOSTSHEET.name(), sentToCAPColName, LUCKY_COSTING_SENT_YES_VALUE, Criteria.NOT_EQUAL_TO));
			pqs.appendClosedParen();

		}
		//Lucky Enhancement - End
		
	}
	
	/**
	 * Builds the WHERE criteria to include only valid Lucky Seasons.<br>
	 * Any valid season doesn't include Blocks & Patterns or Advanced Concepts types.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereLuckySeason(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType seasonFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_SEASON_FLEX_TYPE_PATH);
		
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSEASON.name(), 
				LCSTableColumns.FLEXTYPEIDPATH.name(), 
				seasonFlexType.getIdPath() + "%", 
				Criteria.LIKE));
		
		final String developmentSeasonColName = seasonFlexType.getAttribute(LUCKY_COSTING_SEASON_DEVELOPMENT_SEASON_ATTR).getColumnName();
		
		if (!LUCKY_COSTING_SKIP_DEVELOPMENT_SEASONS_VALUES.isEmpty()) {
			pqs.appendAndIfNeeded();
	        pqs.appendNotInCriteria(LCSTables.LCSSEASON.name(), developmentSeasonColName, LUCKY_COSTING_SKIP_DEVELOPMENT_SEASONS_VALUES);
		}
		
	}
	
	/**
	 * Builds the WHERE criteria to exclude placeholder or inactive colorways,
	 * and include only those colorways that exist (are selected) in the cost sheet.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereExcludeInvalidColorways(PreparedQueryStatement pqs) throws WTException {
		
		pqs.appendAndIfNeeded();
		
		//Colorway is not placeholder.
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKU.name(), 
        		LCSTableColumns.PLACEHOLDER.name(), 
        		LUCKY_COSTING_PLACEHOLDER_NO_VALUE, 
        		Criteria.EQUALS));
		
		pqs.appendAnd();
		
		//Colorway is active in sourcing config-colorway-season.
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSOURCINGLINK.name(), 
        		LCSTableColumns.ACTIVE.name(), 
        		LUCKY_COSTING_ACTIVE_YES_VALUE, 
        		Criteria.EQUALS));
		
		//Colorways is not removed:
		pqs.appendAnd();
		
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), LCSTableColumns.SEASONREMOVED.name(), "0", Criteria.EQUALS));
		
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
		pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCETOSEASONLINK.name(), 
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
		pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(), 
				LCSTableColumns.EFFECTLATEST.name(), 
				FLEX_OBJECT_EFFECT_LATEST_YES, 
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
