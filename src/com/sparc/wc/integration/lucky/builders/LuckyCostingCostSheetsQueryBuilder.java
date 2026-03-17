package com.sparc.wc.integration.lucky.builders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_OBJECT_EFFECT_LATEST_YES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LATEST_FLEX_OBJECT_ITERATION;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LATEST_FLEX_OBJECT_VERSION;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_ACTIVE_YES_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SENT_TO_CAP_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SENT_TO_FC_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SHEET_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SHEET_STATUS_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SEASON_NAME_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SEASON_DEVELOPMENT_YEAR_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SHEET_NAME_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SHEET_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SHEET_MILESTONE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SHEET_INCOTERMS_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COLORWAY_NAME_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COLORWAY_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COLORWAY_SEASON_SENT_TO_CAP_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COLORWAY_SEASON_SENT_TO_FC_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_PRODUCT_NO_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SEASON_DEVELOPMENT_SEASON_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_PRODUCT_NAME_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SHEET_STATUS_CONFIRMED_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SHEET_TYPE_PRODUCT_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_COST_SHEET_WHATIF_NO_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_PLACEHOLDER_NO_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_PRODUCT_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SEASON_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SENT_YES_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SKIP_DEVELOPMENT_SEASONS_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SOURCING_CONFIG_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SOURCING_NO_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SOURCING_STATUS_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SOURCING_FG_FACTORY_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SOURCING_VENDOR_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SOURCING_STATUS_CONFIRMED_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SOURCING_STATUS_DEVELOPMENT_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_TIMESTAMP_FORMAT;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COST_SENT_TO_FC_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COST_SENT_TO_CAP_ATTR;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import org.apache.logging.log4j.Logger;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.sparc.wc.integration.lucky.domain.LuckyCostingProcessesParam;
import com.sparc.wc.integration.lucky.repository.LuckyCostingRepository;

import wt.log4j.LogR;
import wt.util.WTException;

/**
 * Builds a query (prepared query statement) to obtain the list of product cost sheets from a given sourcing configuration number which 
 * are eligible for Lucky FC & CAP integration.<br>
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
public class LuckyCostingCostSheetsQueryBuilder {

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
		MODIFYSTAMPA2,
		PRODUCTMASTERID,
		SKUMASTERID,
		EFFECTLATEST,
		APPLICABLECOLORNAMES,
		SEASONREMOVED,
		IDA3A7
	}
	
	private static final Logger LOGGER = LogR.getLogger(LuckyCostingRepository.class.getName());
	
	private Long sourcingConfigNumber;
	private LuckyCostingProcessesParam process;
	
	/**
	 * Constructs a LuckyCostingCostSheetQueryBuilder instance.
	 * @param sourcingConfigNumber The sourcing config number to get the list of related eligible cost sheets.
	 * @param process The identifier for the lucky target system, i.e. FC or CAP. This will be used to determine part of the SQL condition criteria.
	 */
	public LuckyCostingCostSheetsQueryBuilder(LuckyCostingProcessesParam process, Long sourcingConfigNumber) {
		this.sourcingConfigNumber = sourcingConfigNumber;
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
		
		buildSelectSourcingConfigAttributeValues(pqs);
		buildSelectProductAttributeValues(pqs);
		buildSelectSeasonAttributeValues(pqs);
		buildSelectCostSheetAttributeValues(pqs);
		buildSelectColorwayAttributeValues(pqs);
		buildSelectFlexObjectIds(pqs);
		
	}
	
	/**
	 * Builds the SELECT to get the Sourcing Config related attribute values, such as:<br>
	 * - Sourcing Configuration Number,<br>
	 * - Vendor,<br> 
	 * - Finished Goods Factory. 
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildSelectSourcingConfigAttributeValues(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType srcConfigFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_SOURCING_CONFIG_FLEX_TYPE_PATH);
		
		final String srcConfigNumColName = srcConfigFlexType.getAttribute(LUCKY_COSTING_SOURCING_NO_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSSOURCINGCONFIG.name(), srcConfigNumColName);
		
		final String srcConfigFGFactoryColName = srcConfigFlexType.getAttribute(LUCKY_COSTING_SOURCING_FG_FACTORY_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSSOURCINGCONFIG.name(), srcConfigFGFactoryColName);
		
		final String srcConfigVendorColName = srcConfigFlexType.getAttribute(LUCKY_COSTING_SOURCING_VENDOR_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSSOURCINGCONFIG.name(), srcConfigVendorColName);
		
	}
	
	/**
	 * Builds the SELECT to get the Product related attribute values, such as:<br>
	 * - Product Number,<br>
	 * - Product Name.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildSelectProductAttributeValues(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType productFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_PRODUCT_FLEX_TYPE_PATH);
		
		final String productNumColName = productFlexType.getAttribute(LUCKY_COSTING_PRODUCT_NO_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSPRODUCT.name(), productNumColName);
		
		final String productNameColName = productFlexType.getAttribute(LUCKY_COSTING_PRODUCT_NAME_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSPRODUCT.name(), productNameColName);
		
	}
	
	/**
	 * Builds the SELECT to get the Season related attribute values, such as: Season Name and Season Year.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildSelectSeasonAttributeValues(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType seasonFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_SEASON_FLEX_TYPE_PATH);
		
		final String seasonNameColName = seasonFlexType.getAttribute(LUCKY_COSTING_SEASON_NAME_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSSEASON.name(), seasonNameColName);
		
		final String yearColName = seasonFlexType.getAttribute(LUCKY_COSTING_SEASON_DEVELOPMENT_YEAR_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSSEASON.name(), yearColName);
	}
	
	/**
	 * Builds the SELECT to get the Cost Sheet related attribute values, such as:<br>
	 * - Cost Sheet Name,<br>
	 * - Cost Sheet Number,<br>
	 * - Cost Sheet Milestone,<br>
	 * - Cost Sheet Incoterms,<br>
	 * - Cost Sheet Modified Date.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildSelectCostSheetAttributeValues(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType costSheetFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_COST_SHEET_FLEX_TYPE_PATH);
		
		final String costSheetNameColName = costSheetFlexType.getAttribute(LUCKY_COSTING_COST_SHEET_NAME_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSPRODUCTCOSTSHEET.name(), costSheetNameColName);
		
		final String costSheetNumberColName = costSheetFlexType.getAttribute(LUCKY_COSTING_COST_SHEET_NUMBER_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSPRODUCTCOSTSHEET.name(), costSheetNumberColName);
		
		final String costSheetMilestoneColName = costSheetFlexType.getAttribute(LUCKY_COSTING_COST_SHEET_MILESTONE_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSPRODUCTCOSTSHEET.name(), costSheetMilestoneColName);
		
		final String costSheetIncotermsColName = costSheetFlexType.getAttribute(LUCKY_COSTING_COST_SHEET_INCOTERMS_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSPRODUCTCOSTSHEET.name(), costSheetIncotermsColName);
		
		pqs.appendSelectColumn(LCSTables.LCSPRODUCTCOSTSHEET.name(), LCSTableColumns.MODIFYSTAMPA2.name());
		
	}
	
	/**
	 * Builds the SELECT to get the Colorway related attribute values, such as: Colorway Name and Colorway Number.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildSelectColorwayAttributeValues(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType colorwayFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_PRODUCT_FLEX_TYPE_PATH);
		
		final String colorwayNameColName = colorwayFlexType.getAttribute(LUCKY_COSTING_COLORWAY_NAME_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSSKU.name(), colorwayNameColName);
		
		final String colorwayNumberColName = colorwayFlexType.getAttribute(LUCKY_COSTING_COLORWAY_NUMBER_ATTR).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSSKU.name(), colorwayNumberColName);
	}
	
	/**
	 * Builds the SELECT to get the Internal ID values for the following Flex Objects:<br>
	 * Sourcing Configuration, Product, Season, Product Cost Sheet and Colorway.
	 * @param pqs The prepared statement to be updated.
	 */
	private void buildSelectFlexObjectIds(PreparedQueryStatement pqs) {	
		
		pqs.appendSelectColumn(LCSTables.LCSSOURCINGCONFIG.name(), LCSTableColumns.IDA2A2.name());
		pqs.appendSelectColumn(LCSTables.LCSPRODUCT.name(), LCSTableColumns.IDA2A2.name());
		pqs.appendSelectColumn(LCSTables.LCSSEASON.name(), LCSTableColumns.IDA2A2.name());
		pqs.appendSelectColumn(LCSTables.LCSPRODUCTCOSTSHEET.name(), LCSTableColumns.IDA2A2.name());
		pqs.appendSelectColumn(LCSTables.LCSSKU.name(), LCSTableColumns.IDA2A2.name());
		pqs.appendSelectColumn(LCSTables.LCSSOURCETOSEASONLINK.name(), LCSTableColumns.IDA2A2.name());
		pqs.appendSelectColumn(LCSTables.LCSSKUSOURCINGLINK.name(), LCSTableColumns.IDA2A2.name());
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
		
		buildWhereSourcingConfigNo(pqs);
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
	 * Builds the WHERE criteria for the Sourcing Configuration Number.
	 * @param pqs The prepared statement to be updated.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private void buildWhereSourcingConfigNo(PreparedQueryStatement pqs) throws WTException {
		
		final FlexType srcConfigFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_SOURCING_CONFIG_FLEX_TYPE_PATH);
		final String srcConfigNumColName = srcConfigFlexType.getAttribute(LUCKY_COSTING_SOURCING_NO_ATTR).getColumnName();
		
		pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCINGCONFIG.name(), 
        		srcConfigNumColName, 
        		((sourcingConfigNumber != null) ? sourcingConfigNumber.toString() : null), 
        		Criteria.EQUALS));
		
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
	 * Builds the WHERE criteria to include only Lucky Seasons.
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
	 * Sourcing Config, Season, Product, Sourcing to Season Link, Cost Sheet, Colorway and Colorway-Season Link.
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
	 * Builds the WHERE criteria to include latest flex object version entries for: Product and Colorway.
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
		
	}
	
	/**
	 * Builds the prepared query statement to retrieve a list of sourcing config numbers that contain eligible cost sheets for Lucky FC & CAP integration.
	 * @return The prepared query statament built.
	 * @throws WTException If an error occurs while executing the flex api during the building of the various portions of the prepared query statement.
	 */
	public PreparedQueryStatement buildQuery() throws WTException {
		
		PreparedQueryStatement pqs = new PreparedQueryStatement();
		
		buildSelect(pqs);
		buildFrom(pqs);
		buildJoins(pqs);
		buildWhere(pqs);
		
		return pqs;
	}
	
	/**
	 * Creates an instance of ColorwayCostSheetsResult which contains the extracted cost sheet values for the given Flex Object.<br>
	 * It is assumed the Flex Object provided has been generated from the query built thru this builder, 
	 * hence it is expected all required data columns are present.<br>
	 * @param flexRecordObj The Flext Object containing the data values related to a lucky colorway cost sheet.
	 * @return The lucky colorway cost sheet data values contained within a ColorwayCostSheetsResult or 
	 * <code>null</code> if no flex record object has been provided.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	public static ColorwayCostSheetsResult buildResult(FlexObject flexRecordObj) throws WTException {
		
		if (flexRecordObj == null) {
			return null;
		}
		
		ColorwayCostSheetsResult ccsResult = new ColorwayCostSheetsResult();
		
		buildResultForSourcingConfig(flexRecordObj, ccsResult);
		buildResultForProduct(flexRecordObj, ccsResult);
		buildResultForSeason(flexRecordObj, ccsResult);
		buildResultForCostSheet(flexRecordObj, ccsResult);
		buildResultForColorway(flexRecordObj, ccsResult);
		buildResultForFlexObjectIds(flexRecordObj, ccsResult);
        
		return ccsResult;
	}
	
	/**
	 * Extracts the sourcing configuration related attribute values from the given flex object, 
	 * and adds them into the given ColorwayCostSheetsResult instance.<br>
	 * The Sourcing Configuration values to extract are:<br>
	 * - Sourcing Config Number<br>, 
	 * - Finished Goods Factory Branch Id,<br>
	 * - Vendor Branch Id.
	 * @param flexRecordObj The Flext Object containing the sourcing config data values.
	 * @param costSheetResult The ColorwayCostSheetsResult instance where sourcing configuration related values will be loaded to.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private static void buildResultForSourcingConfig(FlexObject flexRecordObj, 
			ColorwayCostSheetsResult costSheetResult) throws WTException {
		
		if (flexRecordObj == null || costSheetResult == null) {
			return;
		}
		
		final FlexType srcConfigFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_SOURCING_CONFIG_FLEX_TYPE_PATH);
		
        final String srcConfigNumColName = srcConfigFlexType.getAttribute(LUCKY_COSTING_SOURCING_NO_ATTR).getColumnName();
		
        costSheetResult.getCostSheetKey().setSourcingConfigNumber(
				Long.parseLong((String)flexRecordObj.get(LCSTables.LCSSOURCINGCONFIG.name() + "." + srcConfigNumColName)));
        
        final String srcConfigFGFactoryColName = srcConfigFlexType.getAttribute(LUCKY_COSTING_SOURCING_FG_FACTORY_ATTR).getColumnName();
		costSheetResult.getCostSheetKey().setFgFactoryBranchId(
				Long.parseLong((String)flexRecordObj.get(LCSTables.LCSSOURCINGCONFIG.name() + "." + srcConfigFGFactoryColName)));
		
		final String srcConfigVendorColName = srcConfigFlexType.getAttribute(LUCKY_COSTING_SOURCING_VENDOR_ATTR).getColumnName();
		costSheetResult.getCostSheetKey().setVendorBranchId(
				Long.parseLong((String)flexRecordObj.get(LCSTables.LCSSOURCINGCONFIG.name() + "." + srcConfigVendorColName)));
		
	}
	
	/**
	 * Extracts the product related attribute values from the given flex object, and adds them into the given ColorwayCostSheetsResult instance.<br>
	 * The Product values to extract are: Product Number, Product Name.
	 * @param flexRecordObj The Flext Object containing the product data values.
	 * @param costSheetResult The ColorwayCostSheetsResult instance where product related values will be loaded to.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private static void buildResultForProduct(FlexObject flexRecordObj,	ColorwayCostSheetsResult costSheetResult) throws WTException {
		
		final FlexType productFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_PRODUCT_FLEX_TYPE_PATH);
		
		final String productNumColName = productFlexType.getAttribute(LUCKY_COSTING_PRODUCT_NO_ATTR).getColumnName();
		costSheetResult.setProductNumber(Long.parseLong((String)flexRecordObj.get(LCSTables.LCSPRODUCT.name() + "." + productNumColName)));
		
		final String productNameColName = productFlexType.getAttribute(LUCKY_COSTING_PRODUCT_NAME_ATTR).getColumnName();
		costSheetResult.setProductName((String)flexRecordObj.get(LCSTables.LCSPRODUCT.name() + "." + productNameColName));
		
	}
	
	/**
	 * Extracts the season related attribute values from the given flex object, and adds them into the given ColorwayCostSheetsResult instance.<br>
	 * The Season values to extract are: Season Name, Season Year.
	 * @param flexRecordObj The Flext Object containing the season data values.
	 * @param costSheetResult The ColorwayCostSheetsResult instance where season related values will be loaded to.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private static void buildResultForSeason(FlexObject flexRecordObj,	ColorwayCostSheetsResult costSheetResult) throws WTException {
		
		final FlexType seasonFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_SEASON_FLEX_TYPE_PATH);
		
		final String seasonNameColName = seasonFlexType.getAttribute(LUCKY_COSTING_SEASON_NAME_ATTR).getColumnName();
		costSheetResult.getCostSheetKey().setSeasonName((String)flexRecordObj.get(LCSTables.LCSSEASON.name() + "." + seasonNameColName));
		
		final String yearColName = seasonFlexType.getAttribute(LUCKY_COSTING_SEASON_DEVELOPMENT_YEAR_ATTR).getColumnName();
		costSheetResult.getCostSheetKey().setSeasonYear((String)flexRecordObj.get(LCSTables.LCSSEASON.name() + "." + yearColName));
		
	}
	
	/**
	 * Extracts the cost sheet related attribute values from the given flex object, and adds them into the given ColorwayCostSheetsResult instance.<br>
	 * - Cost Sheet Name,<br>
	 * - Cost Sheet Number,<br>
	 * - Cost Sheet Milestone,<br>
	 * - Cost Sheet Incoterms,<br>
	 * - Cost Sheet Modified Date.
	 * @param flexRecordObj The Flext Object containing the cost sheet data values.
	 * @param costSheetResult The ColorwayCostSheetsResult instance where cost sheet related values will be loaded to.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private static void buildResultForCostSheet(FlexObject flexRecordObj, ColorwayCostSheetsResult costSheetResult) throws WTException {
		
		final FlexType costSheetFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_COST_SHEET_FLEX_TYPE_PATH);
		
		final String costSheetNameColName = costSheetFlexType.getAttribute(LUCKY_COSTING_COST_SHEET_NAME_ATTR).getColumnName();
		costSheetResult.setCostSheetName((String)flexRecordObj.get(LCSTables.LCSPRODUCTCOSTSHEET.name() + "." + costSheetNameColName));
		
		final String costSheetNumberColName = costSheetFlexType.getAttribute(LUCKY_COSTING_COST_SHEET_NUMBER_ATTR).getColumnName();
		costSheetResult.setCostSheetNumber(
				Long.parseLong((String)flexRecordObj.get(LCSTables.LCSPRODUCTCOSTSHEET.name() + "." + costSheetNumberColName)));
		
		final String costSheetMilestoneColName = costSheetFlexType.getAttribute(LUCKY_COSTING_COST_SHEET_MILESTONE_ATTR).getColumnName();
		costSheetResult.setCostSheetMilestone((String)flexRecordObj.get(LCSTables.LCSPRODUCTCOSTSHEET.name() + "." + costSheetMilestoneColName));
		
		final String costSheetIncotermsColName = costSheetFlexType.getAttribute(LUCKY_COSTING_COST_SHEET_INCOTERMS_ATTR).getColumnName();
		costSheetResult.getCostSheetKey().setCostSheetIncoterms(
				(String)flexRecordObj.get(LCSTables.LCSPRODUCTCOSTSHEET.name() + "." + costSheetIncotermsColName));
		
		try {
			
			final Date modifiedDate = new SimpleDateFormat(LUCKY_COSTING_TIMESTAMP_FORMAT).parse(
					(String)flexRecordObj.get(LCSTables.LCSPRODUCTCOSTSHEET.name() + "." + LCSTableColumns.MODIFYSTAMPA2.name()));
			
			costSheetResult.setCostSheetModifiedDate(new Timestamp(modifiedDate.getTime()));
			
		} catch(ParseException ex) {
			LOGGER.warn("Unable to extract last saved timestamp from cost sheet # "
					+ costSheetResult.getCostSheetNumber() + "(" + costSheetResult.getCostSheetName() + "): " + ex.getMessage());
		}
		
	}
	
	/**
	 * Extracts the cost sheet related attribute values from the given flex object, and adds them into the given ColorwayCostSheetsResult instance.<br>
	 * The colorway attribute values to extract are: Colorway Name and Colorway Number.
	 * @param flexRecordObj The Flext Object containing the colorway data values.
	 * @param costSheetResult The ColorwayCostSheetsResult instance where colorway related values will be loaded to.
	 * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
	 */
	private static void buildResultForColorway(FlexObject flexRecordObj, ColorwayCostSheetsResult costSheetResult) throws WTException {
		
		final FlexType colorwayFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_COSTING_PRODUCT_FLEX_TYPE_PATH);
		
		final String colorwayNameColName = colorwayFlexType.getAttribute(LUCKY_COSTING_COLORWAY_NAME_ATTR).getColumnName();
		costSheetResult.getCostSheetKey().setColorwayName((String)flexRecordObj.get(LCSTables.LCSSKU.name() + "." + colorwayNameColName));
		
		final String colorwayNumberColName = colorwayFlexType.getAttribute(LUCKY_COSTING_COLORWAY_NUMBER_ATTR).getColumnName();
		costSheetResult.getCostSheetKey().setColorwayNumber(
				Long.parseLong((String)flexRecordObj.get(LCSTables.LCSSKU.name() + "." + colorwayNumberColName)));
		
	}
	
	/**
	 * Extracts selected internal flex identifier values from the given flex object, and adds them into the given ColorwayCostSheetsResult instance.<br>
	 * The internal identifier values reference the following flex objects:<br>
	 * - Sourcing Configuration,<br>
	 * - Product,<br>
	 * - Season,<br>
	 * - Product Cost Sheet,<br>
	 * - Colorway,<br>
	 * - Source To Season Link,<br>
	 * - Source Colorway Link.
	 * @param flexRecordObj The Flext Object containing the flex object internal flex identifier values.
	 * @param costSheetResult The ColorwayCostSheetsResult instance where flex object related internal identifiers will be loaded to.
	 */
	private static void buildResultForFlexObjectIds(FlexObject flexRecordObj, ColorwayCostSheetsResult costSheetResult) {
		
		costSheetResult.getCostSheetKey().setSourcingConfigId(
        		Long.parseLong((String)flexRecordObj.get(LCSTables.LCSSOURCINGCONFIG.name() + "." + LCSTableColumns.IDA2A2.name())));
		
		costSheetResult.setProductId(
				Long.parseLong((String)flexRecordObj.get(LCSTables.LCSPRODUCT.name() + "." + LCSTableColumns.IDA2A2.name())));
		
		costSheetResult.getCostSheetKey().setSeasonId(
				Long.parseLong((String)flexRecordObj.get(LCSTables.LCSSEASON.name() + "." + LCSTableColumns.IDA2A2.name())));
		
		costSheetResult.setCostSheetId(
				Long.parseLong((String)flexRecordObj.get(LCSTables.LCSPRODUCTCOSTSHEET.name() + "." + LCSTableColumns.IDA2A2.name())));
		
		costSheetResult.getCostSheetKey().setColorwayId(
				Long.parseLong((String)flexRecordObj.get(LCSTables.LCSSKU.name() + "." + LCSTableColumns.IDA2A2.name())));
		
		costSheetResult.setSourcingSeasonLinkId(
				Long.parseLong((String)flexRecordObj.get(LCSTables.LCSSOURCETOSEASONLINK.name() + "." + LCSTableColumns.IDA2A2.name())));
		
		costSheetResult.setSourcingColorwayLinkId(
				Long.parseLong((String)flexRecordObj.get(LCSTables.LCSSKUSOURCINGLINK.name() + "." + LCSTableColumns.IDA2A2.name())));
		
	}
	
	/**
	 * Represents the composite Key to use for the cost sheet eligibility requirement:<br>
	 * 5. PLM will send one Cost Sheet per unique combinations of Incoterms, Vendor, Finished Goods Factory, Season, Year, and Colorway.<br>
	 * In this case, the above statement is represented within this class' attributes as follows:<br>
	 *  - Incoterms: costSheetIncoterms,<br>
	 *  - Vendor: vendorBranchId,<br>
	 *  - Finished Goods Factory: fgFactoryBranchId,<br>
	 *  - Season: seasonId (relevant key), seasonName,<br>
	 *  - Year: seasonYear.<br>
	 *  - Colorway: colorwayId (relevant key), colorwayNumber, colorwayName.<br>
	 *  Note that the above combination exists within a Sourcing Configuration, hence sourcingConfigId & sourcingConfigNumber are also part of the key.
	 */
	public static class ColorwayCostSheetKey {
		
		private Long colorwayId;
		private Long colorwayNumber;
		private Long fgFactoryBranchId;
		private Long seasonId;
		private Long sourcingConfigId;
		private Long sourcingConfigNumber;
		private Long vendorBranchId;
		
		private String colorwayName;
		private String costSheetIncoterms;
		private String seasonName;
		private String seasonYear;
		
		private ColorwayCostSheetKey() {
			
		}
		
		public Long getColorwayId() {
			return colorwayId;
		}
		
		public void setColorwayId(Long colorwayId) {
			this.colorwayId = colorwayId;
		}
		
		public Long getColorwayNumber() {
			return colorwayNumber;
		}
		
		public void setColorwayNumber(Long colorwayNumber) {
			this.colorwayNumber = colorwayNumber;
		}
		
		public Long getFgFactoryBranchId() {
			return fgFactoryBranchId;
		}
		
		public void setFgFactoryBranchId(Long fgFactoryBranchId) {
			this.fgFactoryBranchId = fgFactoryBranchId;
		}
		
		public Long getSeasonId() {
			return seasonId;
		}
		
		public void setSeasonId(Long seasonId) {
			this.seasonId = seasonId;
		}
		
		public Long getSourcingConfigId() {
			return sourcingConfigId;
		}
		
		public void setSourcingConfigId(Long sourcingConfigId) {
			this.sourcingConfigId = sourcingConfigId;
		}
		
		public Long getSourcingConfigNumber() {
			return sourcingConfigNumber;
		}
		
		public void setSourcingConfigNumber(Long sourcingConfigNumber) {
			this.sourcingConfigNumber = sourcingConfigNumber;
		}
		
		public Long getVendorBranchId() {
			return vendorBranchId;
		}
		
		public void setVendorBranchId(Long vendorBranchId) {
			this.vendorBranchId = vendorBranchId;
		}
		
		public String getColorwayName() {
			return colorwayName;
		}
		
		public void setColorwayName(String colorwayName) {
			this.colorwayName = colorwayName;
		}
		
		public String getCostSheetIncoterms() {
			return costSheetIncoterms;
		}
		
		public void setCostSheetIncoterms(String costSheetIncoterms) {
			this.costSheetIncoterms = costSheetIncoterms;
		}
		
		public String getSeasonName() {
			return seasonName;
		}
		
		public void setSeasonName(String seasonName) {
			this.seasonName = seasonName;
		}
		
		public String getSeasonYear() {
			return seasonYear;
		}
		
		public void setSeasonYear(String seasonYear) {
			this.seasonYear = seasonYear;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ Objects.hash(colorwayId, colorwayName, colorwayNumber, costSheetIncoterms, fgFactoryBranchId,
							seasonId, seasonName, seasonYear, sourcingConfigId, sourcingConfigNumber, vendorBranchId);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ColorwayCostSheetKey other = (ColorwayCostSheetKey) obj;
			
			return Objects.equals(colorwayId, other.colorwayId) && Objects.equals(colorwayName, other.colorwayName)
					&& Objects.equals(colorwayNumber, other.colorwayNumber)
					&& Objects.equals(costSheetIncoterms, other.costSheetIncoterms)
					&& Objects.equals(fgFactoryBranchId, other.fgFactoryBranchId)
					&& Objects.equals(seasonId, other.seasonId) && Objects.equals(seasonName, other.seasonName)
					&& Objects.equals(seasonYear, other.seasonYear)
					&& Objects.equals(sourcingConfigId, other.sourcingConfigId)
					&& Objects.equals(sourcingConfigNumber, other.sourcingConfigNumber)
					&& Objects.equals(vendorBranchId, other.vendorBranchId);
		}

		@Override
		public String toString() {
			return "ColorwayCostSheetKey [colorwayId=" + colorwayId + ", colorwayNumber=" + colorwayNumber
					+ ", fgFactoryBranchId=" + fgFactoryBranchId + ", seasonId=" + seasonId + ", sourcingConfigId="
					+ sourcingConfigId + ", sourcingConfigNumber=" + sourcingConfigNumber + ", vendorBranchId="
					+ vendorBranchId + ", colorwayName=" + colorwayName + ", costSheetIncoterms=" + costSheetIncoterms
					+ ", seasonName=" + seasonName + ", seasonYear=" + seasonYear + "]";
		}
		
	}
	
	/**
	 * Defines the container for the relevant data elements of a lucky colorway cost sheet within PLM.<br>
	 * Note that in this context, relavant data elements refer to those key attributes required to later determine which is the right cost sheet
	 * to be eligible to be sent thru the lucky costing integration (one cost sheet per sourcing-season-colorway). 
	 * @see LuckyCostingRepository.findEligibleColorwayCostSheets for further details on how this container/data is used.
	 */
	public static class ColorwayCostSheetsResult {
		
		private Long costSheetId;
		private Long costSheetNumber;
		private Long productId;
		private Long productNumber;
		private Long sourcingSeasonLinkId;
		private Long sourcingColorwayLinkId;
		
		private String costSheetName;
		private String costSheetMilestone;
		private String productName;
		
		private ColorwayCostSheetKey costSheetKey;
		
		private Timestamp costSheetModifiedDate;
		
		private ColorwayCostSheetsResult() {
			costSheetKey = new ColorwayCostSheetKey();
		}

		public Long getCostSheetId() {
			return costSheetId;
		}

		public void setCostSheetId(Long costSheetId) {
			this.costSheetId = costSheetId;
		}

		public Long getCostSheetNumber() {
			return costSheetNumber;
		}

		public void setCostSheetNumber(Long costSheetNumber) {
			this.costSheetNumber = costSheetNumber;
		}

		public Long getProductId() {
			return productId;
		}

		public void setProductId(Long productId) {
			this.productId = productId;
		}

		public Long getProductNumber() {
			return productNumber;
		}

		public void setProductNumber(Long productNumber) {
			this.productNumber = productNumber;
		}

		public String getCostSheetName() {
			return costSheetName;
		}

		public void setCostSheetName(String costSheetName) {
			this.costSheetName = costSheetName;
		}

		public String getCostSheetMilestone() {
			return costSheetMilestone;
		}

		public void setCostSheetMilestone(String costSheetMilestone) {
			this.costSheetMilestone = costSheetMilestone;
		}

		public String getProductName() {
			return productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
		}

		public ColorwayCostSheetKey getCostSheetKey() {
			return costSheetKey;
		}

		public void setCostSheetKey(ColorwayCostSheetKey costSheetKey) {
			this.costSheetKey = costSheetKey;
		}

		public Timestamp getCostSheetModifiedDate() {
			return costSheetModifiedDate;
		}

		public void setCostSheetModifiedDate(Timestamp costSheetModifiedDate) {
			this.costSheetModifiedDate = costSheetModifiedDate;
		}

		public Long getSourcingSeasonLinkId() {
			return sourcingSeasonLinkId;
		}

		public void setSourcingSeasonLinkId(Long sourcingSeasonLinkId) {
			this.sourcingSeasonLinkId = sourcingSeasonLinkId;
		}

		public Long getSourcingColorwayLinkId() {
			return sourcingColorwayLinkId;
		}

		public void setSourcingColorwayLinkId(Long sourcingColorwayLinkId) {
			this.sourcingColorwayLinkId = sourcingColorwayLinkId;
		}

		@Override
		public int hashCode() {
			return Objects.hash(costSheetId, costSheetKey, costSheetMilestone, costSheetModifiedDate, costSheetName,
					costSheetNumber, productId, productName, productNumber, sourcingColorwayLinkId,
					sourcingSeasonLinkId);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ColorwayCostSheetsResult other = (ColorwayCostSheetsResult) obj;
			return Objects.equals(costSheetId, other.costSheetId) && Objects.equals(costSheetKey, other.costSheetKey)
					&& Objects.equals(costSheetMilestone, other.costSheetMilestone)
					&& Objects.equals(costSheetModifiedDate, other.costSheetModifiedDate)
					&& Objects.equals(costSheetName, other.costSheetName)
					&& Objects.equals(costSheetNumber, other.costSheetNumber)
					&& Objects.equals(productId, other.productId) && Objects.equals(productName, other.productName)
					&& Objects.equals(productNumber, other.productNumber)
					&& Objects.equals(sourcingColorwayLinkId, other.sourcingColorwayLinkId)
					&& Objects.equals(sourcingSeasonLinkId, other.sourcingSeasonLinkId);
		}

		@Override
		public String toString() {
			return "ColorwayCostSheetsResult [costSheetId=" + costSheetId + ", costSheetNumber=" + costSheetNumber
					+ ", productId=" + productId + ", productNumber=" + productNumber + ", sourcingSeasonLinkId="
					+ sourcingSeasonLinkId + ", sourcingColorwayLinkId=" + sourcingColorwayLinkId + ", costSheetName="
					+ costSheetName + ", costSheetMilestone=" + costSheetMilestone + ", productName=" + productName
					+ ", costSheetKey=" + costSheetKey + ", costSheetModifiedDate=" + costSheetModifiedDate + "]";
		}
		
	}
	
}
