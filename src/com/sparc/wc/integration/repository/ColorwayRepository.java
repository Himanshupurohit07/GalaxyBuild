package com.sparc.wc.integration.repository;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.sparc.wc.integration.domain.SparcColorwayUpdateRequest;
import com.sparc.wc.integration.aero.repository.LCSTables;
import com.sparc.wc.integration.constants.SparcIntegrationConstants;
import com.sparc.wc.integration.domain.SparcColorwayProcesses;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.util.SparcConstants;
import com.lcs.wc.season.*;
import wt.log4j.LogR;
import wt.util.WTException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.*;

/**
 * ColorwayRepository class<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Task #10314 (Hypercare): Updates to colorway index logic components and methods to optimize execution time:
 * Updated method "findColorwayIds", removed call to details api validation.
 * Optimized/fixed prepared statement to retrieve colorway ids: Added extra set of criteria (method "appendRevisionCriteria") to remove dups. Replaced some LCS tables with performance-optimized variants as per PTC's feedback.
 * Reuse of the main query to retrieve colorway season links ids for a given colorway (added methods "preparedQueryStatementForColorwayId" and "appendColorwayIdCriteria"). Added method "getSkuSeasonLinks(colorwayId, skuSeasonLinkIds)".
 * Original methods "getSkuSeasonLinks(colorwayId, process)" and "getSkuSeasonLinkIdsOfColorwayId" have been removed as no longer used (replaced by the above).
 * Added loggers to capture relevant activity and exceptions.
 * 
 * @author Acnovate
 */
public class ColorwayRepository {

    private static final Logger LOGGER = LogR.getLogger(ColorwayRepository.class.getName());
    
    //TODO: To replace the following with appropriate generic constants:
    private static final String PROD_A_REV_TABLE = "ProdARev";
    private static final String SKU_A_REV_TABLE = "SkuArev";
    private static final String SKU_SEASON_LINK_TABLE = "SkuSeasonLink";
    private static final String PRODUCT_SEASON_LINK_TABLE = "ProductSeasonLink";
    
    private ColorwayRepository() {

    }

    public static Set<String> findColorwayIds(final long from, final long to, final SparcColorwayProcesses process) throws WTException {
        final Set<String> colorwayIds = new HashSet<>();
        if (process == null) {
            return colorwayIds;
        }

        final PreparedQueryStatement pqs = preparedQueryStatementForColorwayIds(from == -1 ? null : Timestamp.from(Instant.ofEpochMilli(from)), to == -1 ? null : Timestamp.from(Instant.ofEpochMilli(to)), process);
        LOGGER.debug("[findColorwayIds for " + process + "] Query: " + pqs);

        final SearchResults sr = LCSQuery.runDirectQuery(pqs);
        if (sr == null) {
            return colorwayIds;
        }

        LOGGER.debug("[findColorwayIds for " + process + "] Query results: Found " + sr.getResultsFound() + " records.");

        @SuppressWarnings("unchecked")
        final Collection<FlexObject> resultSet = sr.getResults();
        if (resultSet == null) {
            return colorwayIds;
        }

        final FlexType clrFlexType = FlexTypeCache.getFlexTypeFromPath(SparcIntegrationConstants.LUCKY_PRODUCT_FLEX_PATH);
        final String clrNumClmName = clrFlexType.getAttribute(SparcIntegrationConstants.COLORWAY_INTERNAL_NAME).getColumnName();
        return resultSet.stream().map(fo -> fo.getString(SKU_A_REV_TABLE + "." + clrNumClmName)).collect(Collectors.toSet());
    }
    
    public static Set<String> findColorwaySeasonIds(final String colorwayId, final SparcColorwayProcesses process) throws WTException {
        final Set<String> colorwaySeasonIds = new HashSet<>();
        if (process == null || colorwayId == null) {
            return colorwaySeasonIds;
        }

        final PreparedQueryStatement pqs = preparedQueryStatementForColorwayId(colorwayId, process);
        LOGGER.debug("[findColorwaySeasonIds for " + process + " and colorway# " + colorwayId + "] Query: " + pqs);

        final SearchResults sr = LCSQuery.runDirectQuery(pqs);
        if (sr == null) {
            return colorwaySeasonIds;
        }
        
        LOGGER.debug("[findColorwaySeasonIds for " + process + " and colorway# " + colorwayId + "] Query results: Found " + sr.getResultsFound() + " records.");

        @SuppressWarnings("unchecked")
        final Collection<FlexObject> resultSet = sr.getResults();
        if (resultSet == null) {
            return colorwaySeasonIds;
        }
        
        for (FlexObject flexObj : resultSet) {
        	
        	colorwaySeasonIds.add(flexObj.getString(SKU_SEASON_LINK_TABLE + "." + SparcConstants.IDA2A2));
        	
        }//end for loop.
        
        return colorwaySeasonIds;
    }
    
    public static List<LCSSKUSeasonLink> getSkuSeasonLinks(final String colorwayId, final Collection<String> skuSeasonLinkIds) throws WTException {
    	
    	if (skuSeasonLinkIds == null) {
    		return null;
    	}
    	
        final String skuSeasonClass = "OR:com.lcs.wc.season.LCSSKUSeasonLink:";
        return skuSeasonLinkIds.stream().map(id -> {
            try {
                return (LCSSKUSeasonLink) LCSQuery.findObjectById(skuSeasonClass + id);
            } catch (Exception e) {
                LOGGER.error("[getSkuSeasonLinks] Failed to query colorway-season link id " + id + " for colorway #" + colorwayId + ".", e);
            }
            return null;
        }).filter(link -> link != null).collect(Collectors.toList());
    }

    public static List<LCSSKUSeasonLink> getSkuSeasonLinks(final String colorwayId, SparcColorwayUpdateRequest request) throws WTException {
        return getSkuSeasonLinks(colorwayId, getSkuSeasonLinkIdsOfColorwayIdWithoutProcess(colorwayId, request));
    }

    private static Set<String> getSkuSeasonLinkIdsOfColorwayIdWithoutProcess(final String colorwayId, SparcColorwayUpdateRequest request) throws WTException {
        final Set<String> ids = new HashSet<>();
		LCSSeason seasonObj = null;
        final FlexType clrFlexType = FlexTypeCache.getFlexTypeFromPath(SparcIntegrationConstants.LUCKY_PRODUCT_FLEX_PATH);
        final String clrNumClmName = clrFlexType.getAttribute(SparcIntegrationConstants.COLORWAY_INTERNAL_NAME).getColumnName();
        final PreparedQueryStatement pqs = new PreparedQueryStatement();
        pqs.appendSelectColumn(SKU_SEASON_LINK_TABLE, SparcConstants.IDA2A2);
        pqs.appendFromTable(SKU_A_REV_TABLE);
        pqs.appendFromTable(SKU_SEASON_LINK_TABLE);
        pqs.appendJoin(SKU_SEASON_LINK_TABLE, SparcConstants.SKU_A_REV_ID, SKU_A_REV_TABLE, SparcConstants.BRANCH_ITERATION);
        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(SKU_A_REV_TABLE, SparcConstants.LATEST_ITERATION_INFO, SparcConstants.ONE, Criteria.EQUALS));
        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(SKU_A_REV_TABLE, clrNumClmName, colorwayId, Criteria.EQUALS));
        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(SKU_SEASON_LINK_TABLE, SparcConstants.EFFECT_LATEST, SparcConstants.ONE, Criteria.EQUALS));
		
		LOGGER.debug("request >>>>>>>>>>>>>>>>>>>"+request.getCriteria().getYear() +"=Season Type= "+request.getCriteria().getSeasonType());
		
		final SearchResults sr = LCSQuery.runDirectQuery(pqs);
        if (sr == null) {
            return ids;
        }
        @SuppressWarnings("unchecked")
		final Collection<FlexObject> results = sr.getResults();
        if (results == null) {
            return ids;
        }
		
		Iterator resultsItr = results.iterator();
		String yearDispValue, devSeasSecondaryKeyValue = "";
		FlexType seasFlexType = FlexTypeCache.getFlexTypeFromPath(SparcIntegrationConstants.LUCKY_SEASON_FLEX_PATH);
		FlexTypeAttribute yearAtt = seasFlexType.getAttribute("year");
		FlexTypeAttribute deveSeasAttr = seasFlexType.getAttribute("seasonType");
		
		while(resultsItr.hasNext()){
			FlexObject fobj = (FlexObject)resultsItr.next();
					
			LCSSKUSeasonLink skuSeasLink = (LCSSKUSeasonLink)LCSQuery.findObjectById("OR:com.lcs.wc.season.LCSSKUSeasonLink:"+fobj.getString(SKU_SEASON_LINK_TABLE + "." + SparcConstants.IDA2A2));
			LCSSeason seasObj = (LCSSeason) VersionHelper.latestIterationOf(skuSeasLink.getSeasonMaster());

			LOGGER.debug("Seas year value ###############"+seasObj.getValue("year"));
			LOGGER.debug("seasonType ###############"+seasObj.getValue("seasonType"));
			
			yearDispValue = yearAtt.getAttValueList().getValue(seasObj.getValue("year").toString(), null);
			devSeasSecondaryKeyValue = deveSeasAttr.getAttValueList().getValueFromValueStore(seasObj.getValue("seasonType").toString(), "_CUSTOM_scSecondaryKey");
			
			LOGGER.debug("yearDispValue #############>>>##"+yearDispValue+"<<<devSeasSecondaryKeyValue>>>>"+devSeasSecondaryKeyValue);
			
			if(yearDispValue.equalsIgnoreCase(Integer.toString(request.getCriteria().getYear())) && devSeasSecondaryKeyValue.equalsIgnoreCase(request.getCriteria().getSeasonType())){
				 
				 ids.add(fobj.getString(SKU_SEASON_LINK_TABLE + "." + SparcConstants.IDA2A2));	
				 
				 return ids;
			}
		}
		
        return ids;
    }

    private static PreparedQueryStatement preparedQueryStatementForColorwayIds(final Timestamp from, final Timestamp to, final SparcColorwayProcesses process) throws WTException {
        final PreparedQueryStatement pqs = new PreparedQueryStatement();
        appendColorwayIdSelectColumn(pqs);
        appendFromTables(pqs, SparcConstants.SEASON_OBJECT, PROD_A_REV_TABLE, SKU_A_REV_TABLE, SKU_SEASON_LINK_TABLE, "LCSColor", PRODUCT_SEASON_LINK_TABLE);
        appendJoins(pqs);
        appendLatestIterationCriteria(pqs);
        appendRevisionCriteria(pqs);
        appendColorwayCriteria(pqs, process);
        appendSeasonCriteria(pqs);
        appendTimestampCriteria(pqs, from, to);
        return pqs;
    }
    
    private static PreparedQueryStatement preparedQueryStatementForColorwayId(final String colorwayId, final SparcColorwayProcesses process) throws WTException {
        final PreparedQueryStatement pqs = new PreparedQueryStatement();
        appendColorwaySeasonLinkIdSelectColumn(pqs);
        appendFromTables(pqs, SparcConstants.SEASON_OBJECT, PROD_A_REV_TABLE, SKU_A_REV_TABLE, SKU_SEASON_LINK_TABLE, "LCSColor", PRODUCT_SEASON_LINK_TABLE);
        appendJoins(pqs);
        appendLatestIterationCriteria(pqs);
        appendRevisionCriteria(pqs);
        appendColorwayCriteria(pqs, process);
        appendSeasonCriteria(pqs);
        appendColorwayIdCriteria(colorwayId, pqs);
        return pqs;
    }

    private static void appendTimestampCriteria(final PreparedQueryStatement pqs, final Timestamp from, final Timestamp to) {
        if (from != null) {
            pqs.appendAndIfNeeded();
            pqs.appendCriteria(new Criteria(SKU_SEASON_LINK_TABLE, "MODIFYSTAMPA2", from, Criteria.GREATER_THAN_EQUAL));
        }
        if (to != null) {
            pqs.appendAndIfNeeded();
            pqs.appendCriteria(new Criteria(SKU_SEASON_LINK_TABLE, "MODIFYSTAMPA2", to, Criteria.LESS_THAN_EQUAL));
        }

    }

    private static void appendColorwayIdSelectColumn(final PreparedQueryStatement pqs) throws WTException {
        final FlexType clrFlexType = FlexTypeCache.getFlexTypeFromPath(SparcIntegrationConstants.LUCKY_PRODUCT_FLEX_PATH);
        final String clrNumClmName = clrFlexType.getAttribute(SparcIntegrationConstants.COLORWAY_INTERNAL_NAME).getColumnName();
        pqs.appendSelectColumn(SKU_A_REV_TABLE, clrNumClmName);
    }
    
    private static void appendColorwaySeasonLinkIdSelectColumn(final PreparedQueryStatement pqs) throws WTException {
        pqs.appendSelectColumn(SKU_SEASON_LINK_TABLE, SparcConstants.IDA2A2);
    }

    private static void appendColorwayCriteria(final PreparedQueryStatement pqs, final SparcColorwayProcesses process) throws WTException {
        final FlexType clrFlexType = FlexTypeCache.getFlexTypeFromPath(SparcIntegrationConstants.LUCKY_PRODUCT_FLEX_PATH);
        final FlexType clFlexType = FlexTypeCache.getFlexTypeFromPath(SparcIntegrationConstants.LUCKY_COLOR_FLEX_TYPE_PATH);
        final String clrSeasonClrStatusColumnName = clrFlexType.getAttribute(SparcIntegrationConstants.COLORWAY_TO_SEASON_COLORWAY_STATUS_INTERNAL_NAME).getColumnName();
        final String clSentToFcFlag = clFlexType.getAttribute(SparcIntegrationConstants.LUCKY_COLOR_SENT_TO_FC_INTERNAL_NAME).getColumnName();
        final String capSharedColName = clrFlexType.getAttribute(AERO_COLORWAY_CAP_SHARED_ATTR).getColumnName();

        pqs.appendAndIfNeeded();
        if (SparcColorwayProcesses.FC == process) {
            pqs.appendInCriteria(new QueryColumn(SKU_SEASON_LINK_TABLE, clrSeasonClrStatusColumnName), SparcIntegrationConstants.LUCKY_FC_COLORWAY_STATUSES);
            pqs.appendAndIfNeeded();
            pqs.appendCriteria(new Criteria(SKU_SEASON_LINK_TABLE, clrFlexType.getAttribute(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME).getColumnName(), SparcIntegrationConstants.SENT_NO, Criteria.EQUALS));
            pqs.appendAndIfNeeded();
            pqs.appendCriteria(new Criteria("LCSColor", clSentToFcFlag, SparcIntegrationConstants.SENT_YES, Criteria.EQUALS));
        } else {
			pqs.appendOpenParen();
            pqs.appendOpenParen();
            pqs.appendCriteria(new Criteria(SKU_SEASON_LINK_TABLE,clrSeasonClrStatusColumnName,
                    SparcIntegrationConstants.AERO_PLM_COLORWAY_STATUS_DEVELOPMENT_VALUE,
                    Criteria.EQUALS));
            pqs.appendOr();
            pqs.appendCriteria(new Criteria(SKU_SEASON_LINK_TABLE,clrSeasonClrStatusColumnName,
                    SparcIntegrationConstants.LUCKY_PLM_COLORWAY_STATUS_PRODUCTION_VALUE,
                    Criteria.EQUALS));
            pqs.appendClosedParen();
            pqs.appendOr();
            pqs.appendOpenParen();
            pqs.appendCriteria(new Criteria(SKU_SEASON_LINK_TABLE, clrSeasonClrStatusColumnName, AERO_PLM_COLORWAY_STATUS_DROPPED_VALUE, Criteria.EQUALS));
            pqs.appendAnd();
            pqs.appendCriteria(new Criteria(SKU_SEASON_LINK_TABLE, capSharedColName, SparcIntegrationConstants.SENT_YES, Criteria.EQUALS));
            pqs.appendClosedParen();
            pqs.appendClosedParen();
            pqs.appendAndIfNeeded();
            pqs.appendCriteria(new Criteria(SKU_SEASON_LINK_TABLE, clrFlexType.getAttribute(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME).getColumnName(), SparcIntegrationConstants.SENT_NO, Criteria.EQUALS));
        }

    }

    private static void appendSeasonCriteria(final PreparedQueryStatement pqs) throws WTException {
        final FlexType seasonFlexType = FlexTypeCache.getFlexTypeFromPath(SparcIntegrationConstants.LUCKY_SEASON_FLEX_PATH);
        pqs.appendAndIfNeeded();
        pqs.appendInCriteria(new QueryColumn(SparcConstants.SEASON_OBJECT, seasonFlexType.getAttribute(SparcIntegrationConstants.DEVELOPMENT_SEASON_INTERNAL_NAME).getColumnName()), SparcIntegrationConstants.LUCKY_DEVELOPMENT_SEASONS);
        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(SparcConstants.SEASON_OBJECT, "FLEXTYPEIDPATH", seasonFlexType.getIdPath() + "%", Criteria.LIKE));
    }

    private static void appendLatestIterationCriteria(final PreparedQueryStatement pqs) {
        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(SparcConstants.SEASON_OBJECT, SparcConstants.LATEST_ITERATION_INFO, SparcConstants.ONE, Criteria.EQUALS));
        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(PROD_A_REV_TABLE, SparcConstants.LATEST_ITERATION_INFO, SparcConstants.ONE, Criteria.EQUALS));
        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(SKU_SEASON_LINK_TABLE, SparcConstants.EFFECT_LATEST, SparcConstants.ONE, Criteria.EQUALS));
        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(SKU_A_REV_TABLE, SparcConstants.LATEST_ITERATION_INFO, SparcConstants.ONE, Criteria.EQUALS));
        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(SKU_SEASON_LINK_TABLE, SparcConstants.SEASON_REMOVED, SparcConstants.ONE, Criteria.NOT_EQUAL_TO));
        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(PRODUCT_SEASON_LINK_TABLE, SparcConstants.EFFECT_LATEST, SparcConstants.ONE, Criteria.EQUALS));
        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(PRODUCT_SEASON_LINK_TABLE, SparcConstants.SEASON_REMOVED, SparcConstants.ONE, Criteria.NOT_EQUAL_TO));
    }
    
    private static void appendRevisionCriteria(final PreparedQueryStatement pqs) {
    	pqs.appendAndIfNeeded();
    	pqs.appendCriteria(new Criteria(SKU_A_REV_TABLE, "versionIdA2versionInfo", "A", Criteria.EQUALS));
    	pqs.appendAndIfNeeded();
    	pqs.appendCriteria(new Criteria(PROD_A_REV_TABLE, "versionIdA2versionInfo", "A", Criteria.EQUALS));
    	pqs.appendAndIfNeeded();
    	pqs.appendCriteria(new Criteria(SparcConstants.SEASON_OBJECT, "versionIdA2versionInfo", "A", Criteria.EQUALS));
    }

    private static void appendJoins(final PreparedQueryStatement pqs) {
        pqs.appendJoin(SKU_A_REV_TABLE, SparcConstants.PROD_A_REV_ID, PROD_A_REV_TABLE, SparcConstants.BRANCH_ITERATION);
        pqs.appendJoin(PRODUCT_SEASON_LINK_TABLE, SparcConstants.PROD_A_REV_ID, PROD_A_REV_TABLE, SparcConstants.BRANCH_ITERATION);
        pqs.appendJoin(PRODUCT_SEASON_LINK_TABLE, SparcConstants.SEASON_A_REV_ID, SparcConstants.SEASON_OBJECT, SparcConstants.BRANCH_ITERATION);
        pqs.appendJoin(SKU_SEASON_LINK_TABLE, SparcConstants.SKU_A_REV_ID, SKU_A_REV_TABLE, SparcConstants.BRANCH_ITERATION);
        pqs.appendJoin(SKU_SEASON_LINK_TABLE, SparcConstants.SEASON_A_REV_ID, SparcConstants.SEASON_OBJECT, SparcConstants.BRANCH_ITERATION);
        pqs.appendJoin(SKU_A_REV_TABLE, "IDA3A2TYPEINFOLCSSKU", "LCSColor", SparcConstants.IDA2A2);
    }
    
    private static void appendColorwayIdCriteria(final String colorwayId, final PreparedQueryStatement pqs) throws WTException {
    	final FlexType clrFlexType = FlexTypeCache.getFlexTypeFromPath(SparcIntegrationConstants.LUCKY_PRODUCT_FLEX_PATH);
        final String clrNumClmName = clrFlexType.getAttribute(SparcIntegrationConstants.COLORWAY_INTERNAL_NAME).getColumnName();
        
        pqs.appendAndIfNeeded();
    	pqs.appendCriteria(new Criteria(SKU_A_REV_TABLE, clrNumClmName, colorwayId, Criteria.EQUALS));
    }

    private static void appendFromTables(final PreparedQueryStatement pqs, final String... tables) {
        if (tables == null || tables.length < 1) {
            return;
        }
        Arrays.stream(tables)
                .filter(table -> table != null)
                .forEach(table -> pqs.appendFromTable(table));
    }

}
