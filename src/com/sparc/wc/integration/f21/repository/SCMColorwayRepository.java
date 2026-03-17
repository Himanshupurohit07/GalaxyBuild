package com.sparc.wc.integration.f21.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.LCSSKUSourcingLink;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigClientModel;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.sparc.wc.util.SparcConstants;

import wt.log4j.LogR;
import wt.method.MethodContext;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_SENT_TO_SCM21_SCYES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_SOURCING_STATUS_CONFIRMED;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_SOURCING_STATUS_DROPPED;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_SC_SENT_TO_SCM21_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_SC_PRODUCT_UPDATE_FLAG_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_SEASON_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_SC_SOURCING_NO_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_SC_SOURCING_STATUS_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_SOURCING_CONFIGURATION_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_INTEGRATION_CONTEXT_FLAG_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_INTEGRATION_CONTEXT_FLAG_UPDATED;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_OVERRIDE_ALT_SECONDARY_KEY_LIST;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_ALT_SECONDARY_KEY_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_DEVELOPMENT_SEASON_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_SKIP_DEVELOPMENT_SEASONS;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LATEST_FLEX_OBJECT_ITERATION;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LATEST_FLEX_OBJECT_VERSION;

/**
 * Repository for accessing the various data elements from PLM related to F21/SCM integration.<br>
 * FIXES/ADMENDMENTS:<br>
 * - Added integration context usage to skip plugin processing when the Sent To SCM flag is updated.<br>
 * - Bug #8659: Fixed Sourcing Config's Dropped status eligibility for sending to SCM.<br>
 * - Replaced custom logging with PLM's LogR.<br> 
 * - Changed Product Update Flag logic per request from customer (Vinod): When updating the Product Update Flag set it to true (Yes).
 * - Moved attribute value lookup methods from payload builder class as part of UAT tasks #8846 & #8847.<br> 
 * - Task #8846 (UAT): Use new custom F21 secondary key (_CUSTOM_scF21SecondaryKey) enumeration attribute to pull values from 
 * pre-configured attributes (i.e. Development Season / seasonType)<br>
 * - Task #8847 (UAT): Exclude invalid Development Season types when querying for sourcing configs and when retrieving the latest season.<br>
 * - Minor update: Added conditional check to sourcing config ids query build to look for invalid development season types if applicable.<br>   
 * - Minor update: Added explicit warning message during sourcing config ids lookup when encountering a bad sourcing config #.<br>
 * - Task #8873 (UAT): Include colorways for all valid seasons the product is associated with.
 * As part of this change, new methods were added: getValidSeasonsForProduct and overload version of getLatestSeason.<br>
 * - Task #8942 (UAT): Added methods getActiveProductColorwaysForSeason & updateSourcingColorwayValues to support relocation of 
 * product update flag to sourcing-season.
 * - Minor enhancements to queries for retrieving sourcing config ids, sourcing config details and source-to-season link data: 
 * Added explicit version/iteration criteria. Removed direct join between product-season from the sourcing config query.
 * - Minor code cleanup: Added enum constants for system tables and table columns to remove hardcoded references.
 * 
 * @author Acnovate
 */
public class SCMColorwayRepository {

	//Enumeration for out-of-the-box flex tables used in this respository.
	private static enum LCSTables {
		LCSSOURCINGCONFIG,
		LCSSEASON,
		LCSSOURCETOSEASONLINK,
		LCSPRODUCT,
		LCSSOURCINGCONFIGMASTER,
		LCSSOURCETOSEASONLINKMASTER;
	}
	
	//Enumeration for out-of-the-box flex table columns used in the query.
	private static enum LCSTableColumns {
		IDA3A6,
		IDA3MASTERREFERENCE,
		IDA3B6,
		IDA2A2,
		IDA3B12,
		FLEXTYPEIDPATH,
		MODIFYSTAMPA2,
		BRANCHIDITERATIONINFO,
		VERSIONIDA2VERSIONINFO,
		LATESTITERATIONINFO;
	}
	
	//Enumeration for out-of-the-box flex object references
	private static enum LCSObjectReferences {
		LCSSOURCINGCONFIG("OR:com.lcs.wc.sourcing.LCSSourcingConfig:"),
		LCSSOURCETOSEASONLINK("OR:com.lcs.wc.sourcing.LCSSourceToSeasonLink:");
		
		private String objRef;
		
		LCSObjectReferences(String objRef) {
			this.objRef = objRef;
		}
		
		public String getObjRefString() {
			return objRef;
		}
	}
	

	//Enumeration for out-of-the-box flex version references
	private static enum LCSVersionReferences {
		LCSSOURCINGCONFIG("VR:com.lcs.wc.sourcing.LCSSourcingConfig:");
		
		private String objRef;
		
		LCSVersionReferences(String objRef) {
			this.objRef = objRef;
		}
		
		public String getVerRefString() {
			return objRef;
		}
	}
	
	private static final Logger LOGGER = LogR.getLogger(SCMColorwayRepository.class.getName());
	
	private SCMColorwayRepository() {
		
	}
	
	/**
	 * Lookup and retrieve all Sourcing Configuration Ids that meet the criteria for F21/SCM integration.
	 * @param from Beginning part of the modified range. Restricts the retrieval of sourcing configuration ids 
	 * to those sourcing configurations that were modified after this date onwards. 
	 * @param to Ending part of the modified date range. Restricts the retrieval of sourcing configuration ids
	 * to those sourcing configurations that were modified before this date.
	 * @return The list of sourcing configuration ids (or sourcing configuration numbers according to PLM).
	 * @throws WTException If any error occurs during the data access/lookup at PLM.
	 */
	public static List<Long> findSourcingConfigIds(final Timestamp from, final Timestamp to) throws WTException {
		
		SearchResults qryResults = null; 
		List<Long> sourceConfigIds = new ArrayList<Long>();
        
		PreparedQueryStatement pqs = buildSourcingConfigIdsQuery(from, to);
        
		LOGGER.info("[findSourcingConfigIds] Query: " + pqs);
        qryResults = LCSQuery.runDirectQuery(pqs);
        LOGGER.debug("[findSourcingConfigIds] Query results: Found " + qryResults.getResultsFound() + " records.");
        
        if (qryResults != null) {
        	
        	@SuppressWarnings("unchecked")
			Collection<FlexObject> resultSet = qryResults.getResults();
        	for (FlexObject fo : resultSet) {
        		
        		final FlexType srcConfigFlexType = FlexTypeCache.getFlexTypeFromPath(F21_SCM_SOURCING_CONFIGURATION_FLEX_TYPE_PATH);
                final String srcConfigNumColName = srcConfigFlexType.getAttribute(F21_SCM_SC_SOURCING_NO_INTERNAL_NAME).getColumnName();
        		long srcConfigNum=0L;
        		
				try{
					srcConfigNum = Long.parseLong((String)fo.get(LCSTables.LCSSOURCINGCONFIG.name() + "." + srcConfigNumColName));
				}catch(Exception ex){
					LOGGER.warn("Skipping bad sourcing configuration # found '"
							+ fo.get(LCSTables.LCSSOURCINGCONFIG.name() + "." + srcConfigNumColName) + "'.");
					continue;
				}
        		
        		final FlexType srcToSeasonFlexType = FlexTypeCache.getFlexTypeFromPath(F21_SCM_SOURCING_CONFIGURATION_FLEX_TYPE_PATH);
        		final String srcStatusColName = srcToSeasonFlexType.getAttribute(F21_SCM_SC_SOURCING_STATUS_INTERNAL_NAME).getColumnName();
        		
        		LOGGER.debug("srcStatusColName = " + srcStatusColName);
        		
        		boolean isDropped = F21_SCM_SOURCING_STATUS_DROPPED.equalsIgnoreCase(
        				(String)fo.get(LCSTables.LCSSOURCETOSEASONLINK.name() + "." + srcStatusColName));
        		
        		String srcBranchId = (String)fo.get(LCSTables.LCSSOURCINGCONFIG.name() + "." + LCSTableColumns.BRANCHIDITERATIONINFO.name());
        				
        		if (!isDropped) {
        			sourceConfigIds.add(srcConfigNum);
        		} else if (isDropped && isDroppedSourcingConfigIdEligible(srcBranchId)) {
        			LOGGER.debug("Sourcing Configuration #" + srcConfigNum + " with status 'Dropped' is eligible for SCM");
        			sourceConfigIds.add(srcConfigNum);
        		} else {
        			LOGGER.debug("Sourcing Configuration #" + srcConfigNum + " with status 'Dropped' is not eligible for SCM as it hasn't been sent to SCM previously.");
        		}
        		
        	}
        }
        
        return sourceConfigIds;
    }
	
	/**
	 * Builds the prepared stetement with the PLM SQL to lookup for Sourcing Configuration numbers.
	 * @param from Beginning part of the modified range. Restricts the retrieval of sourcing configuration ids 
	 * to those sourcing configurations that were modified after this date onwards. 
	 * @param to Ending part of the modified date range. Restricts the retrieval of sourcing configuration ids
	 * to those sourcing configurations that were modified before this date.
	 * @return The compiled Prepared Statament (query) to retrieve the list of sourcing configuration numbers.
	 * @throws WTException If an error occurs while constructing the prepared statement or while accessing flex meta data at PLM.
	 */
	private static PreparedQueryStatement buildSourcingConfigIdsQuery(final Timestamp from, final Timestamp to) throws WTException {
		
		PreparedQueryStatement pqs = new PreparedQueryStatement();
		
		//SELECT: scSourcingNo (Sourcing Config #)
		final FlexType srcConfigFlexType = FlexTypeCache.getFlexTypeFromPath(F21_SCM_SOURCING_CONFIGURATION_FLEX_TYPE_PATH);
        final String srcConfigNumColName = srcConfigFlexType.getAttribute(F21_SCM_SC_SOURCING_NO_INTERNAL_NAME).getColumnName();
        pqs.setDistinct(true);
        pqs.appendSelectColumn(LCSTables.LCSSOURCINGCONFIG.name(), srcConfigNumColName);
        
        //SELECT: branchIditerationInfo
        pqs.appendSelectColumn(LCSTables.LCSSOURCINGCONFIG.name(), LCSTableColumns.BRANCHIDITERATIONINFO.name());
        
        //SELECT: scSourcingStatus (Sourcing Status)
        final FlexType srcToSeasonFlexType = FlexTypeCache.getFlexTypeFromPath(F21_SCM_SOURCING_CONFIGURATION_FLEX_TYPE_PATH);
		final String srcStatusColName = srcToSeasonFlexType.getAttribute(F21_SCM_SC_SOURCING_STATUS_INTERNAL_NAME).getColumnName();
		pqs.appendSelectColumn(LCSTables.LCSSOURCETOSEASONLINK.name(), srcStatusColName);
		
		//FROM:
        pqs.appendFromTable(LCSTables.LCSSOURCINGCONFIGMASTER.name());
        pqs.appendFromTable(LCSTables.LCSSOURCETOSEASONLINKMASTER.name());
        pqs.appendFromTable(LCSTables.LCSPRODUCT.name());
        pqs.appendFromTable(LCSTables.LCSSEASON.name()); 
        pqs.appendFromTable(LCSTables.LCSSOURCINGCONFIG.name());
        pqs.appendFromTable(LCSTables.LCSSOURCETOSEASONLINK.name());
        
		//JOINS:
        pqs.appendJoin(LCSTables.LCSSOURCETOSEASONLINKMASTER.name(),LCSTableColumns.IDA3A6.name(),LCSTables.LCSSOURCINGCONFIG.name(),LCSTableColumns.IDA3MASTERREFERENCE.name());
		pqs.appendJoin(LCSTables.LCSSOURCETOSEASONLINKMASTER.name(),LCSTableColumns.IDA3B6.name(),LCSTables.LCSSEASON.name(),LCSTableColumns.IDA3MASTERREFERENCE.name());
		pqs.appendJoin(LCSTables.LCSSOURCINGCONFIGMASTER.name(),LCSTableColumns.IDA3A6.name(),LCSTables.LCSPRODUCT.name(),LCSTableColumns.IDA3MASTERREFERENCE.name());
		pqs.appendJoin(LCSTables.LCSSOURCINGCONFIG.name(),LCSTableColumns.IDA3MASTERREFERENCE.name(),LCSTables.LCSSOURCINGCONFIGMASTER.name(),LCSTableColumns.IDA2A2.name());
		pqs.appendJoin(LCSTables.LCSSOURCETOSEASONLINK.name(),LCSTableColumns.IDA3MASTERREFERENCE.name(),LCSTables.LCSSOURCETOSEASONLINKMASTER.name(),LCSTableColumns.IDA2A2.name());
		
		//WHERE: Sent to SCM21 is 'No' or blank (not 'Yes')
		final String sentToSCMColName = srcConfigFlexType.getAttribute(F21_SCM_SC_SENT_TO_SCM21_INTERNAL_NAME).getColumnName();
		pqs.appendAndIfNeeded();
		pqs.appendOpenParen();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCINGCONFIG.name(), sentToSCMColName, "", Criteria.IS_NULL));
		pqs.appendOr();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCINGCONFIG.name(), sentToSCMColName, F21_SCM_SENT_TO_SCM21_SCYES, Criteria.NOT_EQUAL_TO));
		pqs.appendClosedParen();
		
		//WHERE: Sourcing Status is 'Confirmed' or 'Dropped'
		pqs.appendAndIfNeeded();
		pqs.appendInCriteria(LCSTables.LCSSOURCETOSEASONLINK.name(), 
				srcStatusColName, 
				Arrays.asList(new String[] {F21_SCM_SOURCING_STATUS_CONFIRMED, F21_SCM_SOURCING_STATUS_DROPPED}));
		
		//WHERE: Season belongs to 'F21' AND is valid Development Season type.
		final FlexType seasonFlexType = FlexTypeCache.getFlexTypeFromPath(F21_SCM_SEASON_FLEX_TYPE_PATH);
		final String developmentSeasonColName = seasonFlexType.getAttribute(F21_SCM_DEVELOPMENT_SEASON_INTERNAL_NAME).getColumnName(); 		
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSEASON.name(), LCSTableColumns.FLEXTYPEIDPATH.name(), seasonFlexType.getIdPath() + "%", Criteria.LIKE));
		
		if (!F21_SCM_SKIP_DEVELOPMENT_SEASONS.isEmpty()) {
			pqs.appendAndIfNeeded();
	        pqs.appendNotInCriteria(LCSTables.LCSSEASON.name(), developmentSeasonColName, F21_SCM_SKIP_DEVELOPMENT_SEASONS);
		}
        
        //WHERE: Flex Objects latest iteration
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCINGCONFIG.name(), LCSTableColumns.LATESTITERATIONINFO.name(), LATEST_FLEX_OBJECT_ITERATION, Criteria.EQUALS));
		pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCETOSEASONLINK.name(), LCSTableColumns.LATESTITERATIONINFO.name(), LATEST_FLEX_OBJECT_ITERATION, Criteria.EQUALS));
		pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSEASON.name(), LCSTableColumns.LATESTITERATIONINFO.name(), LATEST_FLEX_OBJECT_ITERATION, Criteria.EQUALS));
        pqs.appendAndIfNeeded();
      	pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCT.name(), LCSTableColumns.LATESTITERATIONINFO.name(), LATEST_FLEX_OBJECT_ITERATION, Criteria.EQUALS));
        
      	//WHERE: Flex Objects latest version
      	pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCINGCONFIG.name(), 
				LCSTableColumns.VERSIONIDA2VERSIONINFO.name(), 
				LATEST_FLEX_OBJECT_VERSION, 
				Criteria.EQUALS));
		
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCETOSEASONLINK.name(), 
				LCSTableColumns.VERSIONIDA2VERSIONINFO.name(), 
				LATEST_FLEX_OBJECT_VERSION, 
				Criteria.EQUALS));
		
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSEASON.name(), 
				LCSTableColumns.VERSIONIDA2VERSIONINFO.name(), 
				LATEST_FLEX_OBJECT_VERSION, 
				Criteria.EQUALS));
		
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSPRODUCT.name(), 
				LCSTableColumns.VERSIONIDA2VERSIONINFO.name(), 
				LATEST_FLEX_OBJECT_VERSION, 
				Criteria.EQUALS));
		
      	if (from != null || to != null) {
      		
          	//WHERE: Modified date range for various types
          	pqs.appendAndIfNeeded();
          	pqs.appendOpenParen(); //Level 0 Paren
          	
          	//Modified date range for Sourcing Configuration
          	pqs.appendOpenParen(); 
          	if (from != null) {
                pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCINGCONFIG.name(), LCSTableColumns.MODIFYSTAMPA2.name(), from, Criteria.GREATER_THAN_EQUAL));
            }
          	
            if (to != null) {
                pqs.appendAndIfNeeded();
                pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCINGCONFIG.name(), LCSTableColumns.MODIFYSTAMPA2.name(), to, Criteria.LESS_THAN_EQUAL));
            }
            
            pqs.appendClosedParen();
            pqs.appendClosedParen(); //Level 0 Paren
      	}
      	
        return pqs;
	}
	
	/**
	 * Verifies whether the given sourcing configuration branch identifier related to a 'Dropped' source configuration is eligible to be sent to SCM.<br>
	 * A source configuration with configuration status 'Dropped' is eligible to be sent to SCM only if it was sent to SCM previously.<br>
	 * @param srcConfigBranchId - The source configuration's Branch Identifier of a presumed dropped source configuration.
	 * @return true if the source configuration # is eligible to be sent to SCM, or false otherwise.
	 */
	private static boolean isDroppedSourcingConfigIdEligible(String srcConfigBranchId) throws WTException {
		
		boolean isEligible = false;
		
		LCSSourcingConfig sconfig = (LCSSourcingConfig)LCSQuery
				.findObjectById(LCSVersionReferences.LCSSOURCINGCONFIG.getVerRefString() + srcConfigBranchId);
		Collection<?> versionsColl = VersionHelper.allIterationsFrom(sconfig);
		Iterator<?> versions = versionsColl.iterator();
		
		while (versions.hasNext()) {

			LCSSourcingConfig source1 = (LCSSourcingConfig)versions.next();
			String sentToSCM = (String)source1.getValue(F21_SCM_SC_SENT_TO_SCM21_INTERNAL_NAME);

			if(F21_SCM_SENT_TO_SCM21_SCYES.equalsIgnoreCase(sentToSCM)){
				isEligible = true; 
				break;
			}
		}
		
		return isEligible;
	}
	
	/**
	 * Retrieves the Sourcing Configuration instance for the given Sourcing Configuration Number.
	 * @param srcConfigNum The Sourcing Configuration Number
	 * @return The Sourcing Configuration instance or <code>null</code> if no Sourcing Configuration instance 
	 * could be found for the Sourcing Configuration Number provided.
	 * @throws WTException If an error occurs while accessing PLM to query for the Sourcing Configuration.
	 */
	public static LCSSourcingConfig getSourcingConfiguration(long srcConfigNum) throws WTException {
		
		LCSSourcingConfig flexSrcConfig = null;
		PreparedQueryStatement pqs = new PreparedQueryStatement();
		
		final FlexType srcConfigFlexType = FlexTypeCache.getFlexTypeFromPath(F21_SCM_SOURCING_CONFIGURATION_FLEX_TYPE_PATH);
        final String srcConfigNumColName = srcConfigFlexType.getAttribute(F21_SCM_SC_SOURCING_NO_INTERNAL_NAME).getColumnName();
        
        pqs.appendSelectColumn(LCSTables.LCSSOURCINGCONFIG.name(), LCSTableColumns.IDA2A2.name());
        pqs.appendFromTable(LCSTables.LCSSOURCINGCONFIG.name());
        pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCINGCONFIG.name(), LCSTableColumns.LATESTITERATIONINFO.name(), LATEST_FLEX_OBJECT_ITERATION, Criteria.EQUALS));
        pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCINGCONFIG.name(),LCSTableColumns.VERSIONIDA2VERSIONINFO.name(),LATEST_FLEX_OBJECT_VERSION,Criteria.EQUALS));
        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCINGCONFIG.name(), srcConfigNumColName, "" + srcConfigNum, Criteria.EQUALS));
		
        LOGGER.info("[getSourcingConfiguration] Query: " + pqs);
        SearchResults qryResults = LCSQuery.runDirectQuery(pqs);
        LOGGER.info("[getSourcingConfiguration] Query results: Found " + qryResults.getResultsFound() + " records.");
        
        if (qryResults != null) {
        	
        	@SuppressWarnings("unchecked")
			Collection<FlexObject> resultSet = qryResults.getResults();
        	
        	for (FlexObject fo : resultSet) {
        		
        		flexSrcConfig = (LCSSourcingConfig)LCSQuery.findObjectById(
        				LCSObjectReferences.LCSSOURCINGCONFIG.getObjRefString()
        						+ (String)fo.get(LCSTables.LCSSOURCINGCONFIG.name() + "." + LCSTableColumns.IDA2A2.name()));
        		
        	}
        }
        
        return flexSrcConfig;
	}
	
	/**
	 * Retrieves the Source To Season link for the Sourcing Configuration Number.
	 * @param srcConfigNum The Sourcing Configuration Number
	 * @return The Source To Season Link instance or <code>null</code> if no Source To Season data 
	 * could be found for the Sourcing Configuration Number provided.
	 * @throws WTException  If an error occurs while accessing PLM to query for the Source To Season Link.
	 */
	public static LCSSourceToSeasonLink getSourceToSeasonLink(long srcConfigNum) throws WTException {
		
		LCSSourceToSeasonLink flexSourceToSeasonLink = null;
		
		PreparedQueryStatement pqs = buildSourcingToSeasonLinkQuery(srcConfigNum);
		
		LOGGER.info("[getSourceToSeasonLink] Query: " + pqs);
        SearchResults qryResults = LCSQuery.runDirectQuery(pqs);
        LOGGER.info("[getSourceToSeasonLink] Query results: Found " + qryResults.getResultsFound() + " records.");
        
        if (qryResults != null) {
        	
        	@SuppressWarnings("unchecked")
			Collection<FlexObject> resultSet = qryResults.getResults();
        	
        	for (FlexObject fo : resultSet) {
        		
        		flexSourceToSeasonLink = (LCSSourceToSeasonLink)LCSQuery.findObjectById(
        				LCSObjectReferences.LCSSOURCETOSEASONLINK.getObjRefString()
        						+ (String)fo.get(LCSTables.LCSSOURCETOSEASONLINK.name() + "." + LCSTableColumns.IDA2A2.name()));
        		
        		
        	}
        }
		
		return flexSourceToSeasonLink;
	}
	
	/**
	 * Builds the prepared stetement with the PLM SQL to lookup Sourcing To Season details for the given sourcing configuration number.
	 * @param srcConfigNum The sourcing configuration number to use as key.
	 * @return The compiled Prepared Statament (query) to retrieve the Sourcing To Season details.
	 * @throws WTException If an error occurs while constructing the prepared statement or while accessing flex meta data at PLM.
	 */
	private static PreparedQueryStatement buildSourcingToSeasonLinkQuery(long srcConfigNum) throws WTException {
		
		final FlexType srcConfigFlexType = FlexTypeCache.getFlexTypeFromPath(F21_SCM_SOURCING_CONFIGURATION_FLEX_TYPE_PATH);
        final String srcConfigNumColName = srcConfigFlexType.getAttribute(F21_SCM_SC_SOURCING_NO_INTERNAL_NAME).getColumnName();
		
		PreparedQueryStatement pqs = new PreparedQueryStatement();
		
        //SELECT: IDA2A2 (Source To Season Link ID)
		pqs.appendSelectColumn(LCSTables.LCSSOURCETOSEASONLINK.name(), LCSTableColumns.IDA2A2.name());
		
		//FROM:
        pqs.appendFromTable(LCSTables.LCSSOURCETOSEASONLINKMASTER.name());
        pqs.appendFromTable(LCSTables.LCSSOURCINGCONFIG.name());
        pqs.appendFromTable(LCSTables.LCSSOURCETOSEASONLINK.name());
        
		//JOINS:
        pqs.appendJoin(LCSTables.LCSSOURCETOSEASONLINKMASTER.name(),
        		LCSTableColumns.IDA3A6.name(),
        		LCSTables.LCSSOURCINGCONFIG.name(),
        		LCSTableColumns.IDA3MASTERREFERENCE.name());
        
		pqs.appendJoin(LCSTables.LCSSOURCETOSEASONLINK.name(),
				LCSTableColumns.IDA3MASTERREFERENCE.name(),
				LCSTables.LCSSOURCETOSEASONLINKMASTER.name(),
				LCSTableColumns.IDA2A2.name());
		
		//WHERE: Source Configuration number
		pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCINGCONFIG.name(), srcConfigNumColName, "" + srcConfigNum, Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCINGCONFIG.name(), LCSTableColumns.LATESTITERATIONINFO.name(), LATEST_FLEX_OBJECT_ITERATION, Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCINGCONFIG.name(),LCSTableColumns.VERSIONIDA2VERSIONINFO.name(),LATEST_FLEX_OBJECT_VERSION,Criteria.EQUALS));
		
		//WHERE: Source to Season Link latest iteration.
		pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCETOSEASONLINK.name(), LCSTableColumns.LATESTITERATIONINFO.name(), LATEST_FLEX_OBJECT_ITERATION, Criteria.EQUALS));
        pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria(LCSTables.LCSSOURCETOSEASONLINK.name(),LCSTableColumns.VERSIONIDA2VERSIONINFO.name(),LATEST_FLEX_OBJECT_VERSION,Criteria.EQUALS));
        
        return pqs;
	}
	
	/**
	 * Retrieves the Product associated to the given Sourcing Configuration instance.
	 * @param flexSrcConfig The Sourcing Configuration instance.
	 * @return The product instance or <code>null</code> if no sourcing configuration is provided. 
	 * @throws WTException If an error occurs while retrieving the product details from PLM.
	 */
	public static LCSProduct getProduct(LCSSourcingConfig flexSrcConfig) throws WTException {
		
		LCSProduct flexProduct = null;
		
		if (flexSrcConfig != null) {
			 flexProduct = (LCSProduct)VersionHelper.getVersion(flexSrcConfig.getProductMaster(), LATEST_FLEX_OBJECT_VERSION);
		}
		
		return flexProduct;
	}
	
	/**
	 * Retrieves a list of SCM integration valid seasons associated to the given product.
	 * @param flexProduct The product to retrieve the seasons for.
	 * @return The list of seasons found for the product, or an empty list if no seasons were found.
	 * @throws WTException If an error occurs while retrieving seasons data from PLM.
	 */
	@SuppressWarnings("unchecked")
	public static List<LCSSeason> getValidSeasonsForProduct(LCSProduct flexProduct) throws WTException {
		
		Collection<LCSSeasonMaster> seasons = null;
		List<LCSSeason> seasonsList = new ArrayList<LCSSeason>();
		
		if (flexProduct == null) {
			return seasonsList;
		}
		
		try {
			
			seasons = new LCSSeasonQuery().findSeasons(flexProduct);
			
			for(LCSSeasonMaster master: seasons) {
				
				LCSSeason seasonObj = (LCSSeason) VersionHelper.latestIterationOf(master);
				
				if(seasonObj!=null) {
					
					String developmentSeason = (String)SparcIntegrationUtil.getValueFrom(seasonObj, F21_SCM_DEVELOPMENT_SEASON_INTERNAL_NAME, false);
					
					if (F21_SCM_SKIP_DEVELOPMENT_SEASONS.contains(developmentSeason)) {
						LOGGER.debug("Latest Season lookup: Skipping season " + seasonObj.getName() + " as it has an invalid Development Season Type value.");
						continue;
					}
					
					seasonsList.add(seasonObj);
					
				}
			}
			
		} catch (RuntimeException e) {
			LOGGER.error("Failed to get the list of valid seasons for product " + flexProduct.getName() + ".", e);
		}
		
		return seasonsList;		
	}
	
	/**
	 * Retrieves the latest season based on creation time found within the list of seasons provided.
	 * @param seasonsList The list of seasons.
	 * @return The latest season found within the list of seasons given or <code>null</code> if the list given is empty.
	 * @throws WTException If an error occurs while inspecting any of the seasons attributes.
	 */
	public static LCSSeason getLatestSeason(List<LCSSeason> seasonsList) throws WTException {
		
		Timestamp time1 = null;
		LCSSeason seasonLatest = null;
		
		if (seasonsList == null || seasonsList.isEmpty()) {
			return seasonLatest;
		}
		
		try {
			
			for(LCSSeason seasonObj : seasonsList) {
				
				Timestamp time2 = seasonObj.getCreateTimestamp();
				
				if(time1 == null || time2.compareTo(time1)>0) {
					time1 = time2;
					seasonLatest = seasonObj;
				}
				
			}
			
		} catch (RuntimeException e) {
			LOGGER.error("Failed to determine the latest season from the list given.", e);
		}
		
		return seasonLatest;
	}
	
	/**
	 * Retrieves the latest season associated to the given product.
	 * @param flexProduct The product to lookup the latest season from.
	 * @return The latest season for the product or <code>null</code> if no product was provided or the product is not tied to any season.
	 * @throws WTException If an error occurs while retrieving seasons data from PLM.
	 */
	@SuppressWarnings("unchecked")
	public static LCSSeason getLatestSeason(LCSProduct flexProduct) throws WTException {
		
		Timestamp time1 = null;
		LCSSeason seasonLatest = null;
		Collection<LCSSeasonMaster> seasons = null;
		
		if (flexProduct == null) {
			return seasonLatest;
		}
		
		try {
			
			seasons = new LCSSeasonQuery().findSeasons(flexProduct);
			
			for(LCSSeasonMaster master: seasons) {
				
				LCSSeason seasonObj = (LCSSeason) VersionHelper.latestIterationOf(master);
				
				if(seasonObj!=null) {
					
					String developmentSeason = (String)SparcIntegrationUtil.getValueFrom(seasonObj, F21_SCM_DEVELOPMENT_SEASON_INTERNAL_NAME, false);
					
					if (F21_SCM_SKIP_DEVELOPMENT_SEASONS.contains(developmentSeason)) {
						LOGGER.debug("Latest Season lookup: Skipping season " + seasonObj.getName() + " as it has an invalid Development Season Type value.");
						continue;
					}
					
					Timestamp time2 = seasonObj.getCreateTimestamp();
					
					if(time1 == null || time2.compareTo(time1)>0) {
						time1 = time2;
						seasonLatest = seasonObj;
					}
				}
			}
			
			LOGGER.debug("Latest season for product " + flexProduct.getName() + " is "
					+ ((seasonLatest != null) ? seasonLatest.getName() : " not found") 
					+ ".");
			
		} catch (RuntimeException e) {
			LOGGER.error("Failed to get the latest season for product " + flexProduct.getName() + ".", e);
		}
		
		return seasonLatest;
	}
	
	/**
	 * Updates the SCM integration control flags for the given sourcing configuration and list of sourcing-colorways.<br>
	 * Flags to update according the specs are:<br>
	 * - Product Update Flag: Set to boolean true for sourcing-colorways.<br>
	 * - Send To SCM: Set to 'Yes' (scYes) for sourcing configuration.
	 * @param flexSrcConfig The Sourcing Configuration associated to the flag to update.
	 * @param flexSourcingColorwayList The list of sourcing-colorway links to update.
	 * @return true if the flags hasve been updated, false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while processing the flag update at PLM.
	 */
	public static boolean updateSCMControlFlags(final LCSSourcingConfig flexSrcConfig,
			List<LCSSKUSourcingLink> flexSourcingColorwayList) throws WTException, WTPropertyVetoException {
		
		Map<String, Object> srcConfigFlagsMap = new HashMap<String, Object>();
		srcConfigFlagsMap.put(F21_SCM_SC_SENT_TO_SCM21_INTERNAL_NAME, F21_SCM_SENT_TO_SCM21_SCYES);
		
		Map<String, Object> skuSourcingFlagsMap = new HashMap<String, Object>();
		skuSourcingFlagsMap.put(F21_SCM_SC_PRODUCT_UPDATE_FLAG_INTERNAL_NAME, true);
		
		return updateSourcingConfigurationValues(flexSrcConfig, srcConfigFlagsMap)
				&& updateSourcingColorwayValues(flexSourcingColorwayList, skuSourcingFlagsMap);
	}
	
	/**
	 * Updates the values for the given attr_key-values map into the given sourcing configuration type instance.
	 * @param flexSrcConfig TheSourcing Configuration instance.
	 * @param values The attr_key-value maps containing the data to update into the Sourcing Configuration.
	 * @return true if the values have been updated, false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while processing the flag update at PLM.
	 */
	private static boolean updateSourcingConfigurationValues(final LCSSourcingConfig flexSrcConfig, Map<String, 
			Object> values) throws WTException, WTPropertyVetoException {
		
		boolean updated = false;
		
		if (flexSrcConfig != null && !values.isEmpty()) {
			
			MethodContext.getContext().put(F21_SCM_INTEGRATION_CONTEXT_FLAG_KEY, F21_SCM_INTEGRATION_CONTEXT_FLAG_UPDATED);
			
			LCSSourcingConfigClientModel srcModel = new LCSSourcingConfigClientModel();
	        srcModel.load(FormatHelper.getObjectId(flexSrcConfig));
	        
	        Set<String> flagKeys = values.keySet();
	        
	        for (String key : flagKeys) {
	        	srcModel.setValue(key, values.get(key));
	        }
	        
	        srcModel.save();
			
	        updated = true;
			
	        LOGGER.debug("Sourcing Configuration " + flexSrcConfig.getName() + " is updated.");
	        
		}
		
		return updated;
		
	}
	
	/**
	 * Updates the values for the given attr_key-values map into the given list of sourcing-colorway links.
	 * @param flexSourcingColorwayList The list of sourcing-colorway links to update.
	 * @param values The attr_key-value maps containing the data to update into each Sourcing-Colorway in the list.
	 * @return true if the values have been updated, false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while processing the flag update at PLM.
	 */
	private static boolean updateSourcingColorwayValues(List<LCSSKUSourcingLink> flexSourcingColorwayList, 
			Map<String, Object> values) throws WTException, WTPropertyVetoException {
		
		boolean updated = false;
		
		if (flexSourcingColorwayList != null && !flexSourcingColorwayList.isEmpty() && values != null && !values.isEmpty()) {
			
			for (LCSSKUSourcingLink flexSKUSourcing : flexSourcingColorwayList) {
				
				Set<String> flagKeys = values.keySet();
				
				for (String key : flagKeys) {
					flexSKUSourcing.setValue(key, values.get(key));
		        }
				
				LCSLogic.persist(flexSKUSourcing);
				
				updated = true;
				
				LOGGER.debug("Sourcing-Colorway " + flexSKUSourcing + " is updated.");
			}
			
		}
		
		return updated;
	}
	
	/**
	 * Get all active colorways from the given product which are also available on the given season the product is linked to.<br>
	 * Note that 'active' colorways refer to those that are selected (checked) at Source-Colorway level.
	 * @param flexProduct The Product instance to get the Colorways associated to.
	 * @param flexSeason The Season instance to filter out those product colorways that do not belong to it.
	 * @param flexSrcConfig The Sourcing Configuration to check the colorways are active.
	 * @return The list of active colorways associated to the product and which are also available at the given season.
	 * @throws WTException If an error occurs while looking up colorways and other meta data at PLM.
	 */
	public static List<LCSSKU> getActiveProductColorwaysForSeason(LCSProduct flexProduct, 
			LCSSeason flexSeason,
			LCSSourcingConfig flexSrcConfig) throws WTException {
		
		List<LCSSKU> flexColorways = new ArrayList<>();
		
		if (flexSrcConfig == null || flexProduct == null || flexSeason == null) {
			return flexColorways;
		}
		
		@SuppressWarnings("unchecked")
		Iterator<LCSSKU> skusItr = LCSSKUQuery.findSKUs(flexProduct).iterator();
		
        while(skusItr.hasNext()){
        	
            LCSSKU currentSku = (LCSSKU)skusItr.next();
            
            //Check colorway is available at the given season (should return an instance)
            LCSSeasonProductLink flexSeasonProdLink = LCSSeasonQuery.findSeasonProductLink(currentSku, flexSeason); 
            
            //Get the sourcing link to check if the colorway is selected at the Source-Colorway level.
            LCSSKUSourcingLink skuSourceLink = new LCSSourcingConfigQuery().getSKUSourcingLink(flexSrcConfig, currentSku, flexSeason);
            
            if (flexSeasonProdLink != null && skuSourceLink != null && skuSourceLink.isActive()) {
            	flexColorways.add(currentSku);
            }
            
        }
		
        return flexColorways;
	}
	
	/**
	 * Get all colorways per season for the given product which are active within the list of given seasons.<br>
	 * Note that 'active' colorways refer to those that are selected (checked) at Sourcing-Colorway level.
	 * @param flexProduct The Product instance to get the Colorways associated to.
	 * @param flexSrcConfig The Sourcing Configuration to check whether the product linked colorways are active or not (ignores inactive colorways).
	 * @param flexSeasonsList The list of seasons where the colorways should be linked to.
	 * @throws WTException If an error occurs while looking up colorways and other meta data at PLM.
	 */
	public static Map<LCSSeason, Set<LCSSKU>> getActiveSourcingSeasonColorways(LCSProduct flexProduct, 
			LCSSourcingConfig flexSrcConfig,
			List<LCSSeason> flexSeasonsList) throws WTException {
		
		Map<LCSSeason, Set<LCSSKU>> flexSeasonColorwaysMap = new HashMap<LCSSeason, Set<LCSSKU>>();
		
		if (flexSrcConfig == null || flexProduct == null || flexSeasonsList == null || flexSeasonsList.isEmpty()) {
			return flexSeasonColorwaysMap;
		}
		
		@SuppressWarnings("unchecked")
		Iterator<LCSSKU> skusItr = LCSSKUQuery.findSKUs(flexProduct).iterator();
		
        while(skusItr.hasNext()){
        	
            LCSSKU currentSku = (LCSSKU)skusItr.next();
            
            for (LCSSeason flexSeason : flexSeasonsList) {
            	
            	//Check colorway is available at the given season (should return an instance)
                LCSSeasonProductLink flexSeasonProdLink = LCSSeasonQuery.findSeasonProductLink(currentSku, flexSeason); 
                
                //Get the sourcing link to check if the colorway is selected at the Source-Colorway level.
                LCSSKUSourcingLink skuSourceLink = new LCSSourcingConfigQuery().getSKUSourcingLink(flexSrcConfig, currentSku, flexSeason);
                
                if (flexSeasonProdLink != null && skuSourceLink != null && skuSourceLink.isActive()) {
                	
                	Set<LCSSKU> colorwaysSet = flexSeasonColorwaysMap.get(flexSeason);
                	
                	if (colorwaysSet != null) {
                		colorwaysSet.add(currentSku);
                	} else {
                		colorwaysSet = new HashSet<LCSSKU>();
                		colorwaysSet.add(currentSku);
                		flexSeasonColorwaysMap.put(flexSeason, colorwaysSet);
                	}
                	
                }
            	
            }
            
        }
        
        if (LOGGER.isDebugEnabled()) {
        	
        	int colorwayCount = 0;
        	
        	for (Set<LCSSKU> seasonColorways : flexSeasonColorwaysMap.values()) {
        		colorwayCount += seasonColorways.size();
        	}
        	
        	LOGGER.debug("Found " + colorwayCount + " active colorways for product " + flexProduct.getName() + " across " + flexSeasonsList.size() + " seasons.");
        	
        }
		
		return flexSeasonColorwaysMap;
	}
	
	/**
	 * Retrieve the enumeration's secondary key value for given flex type object and attribute name.
	 * @param flexObj The flex objext containing the attribute.
	 * @param attrName The name of the attribute to lookup for the secondary key value from.
	 * @return The enumeration's secondary key for the given attribute.
	 * @throws WTException If an error occurs while extracting the secondary key value from the flex object.
	 */
	public static String lookupEnumSecondaryKeyValue(FlexTyped flexObj, String attrName) throws WTException {
		
		return lookupAttrEnumKeyValue(flexObj, 
				attrName, 
				((useAltEnumF21SecondaryKey(attrName)) ? F21_SCM_ALT_SECONDARY_KEY_INTERNAL_NAME : SparcConstants.CUSTOM_SECONDARY_KEY));
		
	}
	
	/**
	 * Checks if the given attribute must get its value from the alternate Enum's F21 secondary key value instead of the default one.
	 * @param attrName The attribute to check.
	 * @return true if the Enum's F21 secondary key should be used instead of the default key to extract the value from the given attribute.
	 */
	private static boolean useAltEnumF21SecondaryKey(String attrName) {
		return (attrName != null && F21_SCM_OVERRIDE_ALT_SECONDARY_KEY_LIST.contains(attrName));
	}
	
	/**
	 * Retrieve the enumeration's display name for the given flex type object and attribute name.
	 * @param flexObj The flex objext containing the attribute.
	 * @param attrName The name of the attribute to lookup for the display name value from.
	 * @return The enumeration's displat name for the given attribute.
	 * @throws WTException If an error occurs while extracting the display name value from the flex object.
	 */
	public static String lookupEnumDisplayValue(FlexTyped flexObj, String attrName) throws WTException {
		
		return lookupAttrEnumKeyValue(flexObj, attrName, "displayName");
		
	}
	
	/**
	 * Retrieve the enumeration's value for given flex type object, attribute name and Enum attribute name.
	 * @param flexObj The flex objext containing the attribute to get the value & enum from.
	 * @param attrName The name of the attribute to lookup for the Enum key value from.
	 * @param enumAttrName The Enum attribute to get the value from, i.e. displayName or _CUSTOM_scSecondaryKey
	 * @return The enumeration's value for the given attribute & enum attribute.
	 * @throws WTException If an error occurs while extracting the enumeration value from the flex object.
	 */
	private static String lookupAttrEnumKeyValue(FlexTyped flexObj, String attrName, String enumAttrName) throws WTException {
		
		String value = null;
		
		if (flexObj == null || attrName == null || enumAttrName == null) {
			return value;
		}
		
		final Object flexObjValue = flexObj.getValue(attrName);
		
		if (flexObjValue == null || !(flexObjValue instanceof String)) {
			return value;
		}
		
		final FlexTypeAttribute flexAttr = flexObj.getFlexType().getAttribute(attrName);
		
		if (flexAttr != null && flexAttr.getAttValueList() != null) {
			value = flexAttr.getAttValueList().get((String)flexObjValue, enumAttrName);
		}
		
		return value;
		
	}
	
}
