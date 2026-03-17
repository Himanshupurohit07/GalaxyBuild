package com.galaxy.wc.product;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductLogic;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * This class contains logic to populate Business Segments details at Product Season level
 */
public class GalaxyProductPlugin {
	
	private static final Logger LOGGER = LogManager.getLogger(GalaxyProductPlugin.class);
	private static final String CRITERIA_PROD_ATTS = LCSProperties.get("com.galaxy.wc.product.BusinessSegmentMapping.Criteria.ProdAtts");
	@SuppressWarnings("unchecked")
	static Set<String> AllProdCriteriaAtts = FormatHelper.commaSeparatedListToSet(CRITERIA_PROD_ATTS);
	private static final String CRITERIA_PRODSEASON_ATTS = LCSProperties.get("com.galaxy.wc.product.BusinessSegmentMapping.Criteria.ProdSeasonAtts");
	@SuppressWarnings("unchecked")
	static Set<String> AllProdSeasonCriteriaAtts = FormatHelper.commaSeparatedListToSet(CRITERIA_PRODSEASON_ATTS);
	private static final String BUSINESS_SEGMENT_MAPPING_TYPE = LCSProperties.get("com.galaxy.wc.product.BusinessSegmentMapping.BOType");
	private static final String BUSINESS_SEGMENT_PRODSEASON_ATTS = LCSProperties.get("com.galaxy.wc.product.BusinessSegmentMapping.Target.ProdSeasonAtts");
	@SuppressWarnings("unchecked")
	static Set<String> TargetProductSeasonAtts = FormatHelper.commaSeparatedListToSet(BUSINESS_SEGMENT_PRODSEASON_ATTS);
	
	/**
	 * @param obj
	 * @param prd
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static void setBusinessSegmentAttValFromProdLink(WTObject obj, LCSProduct prd) throws WTException, WTPropertyVetoException {
		
		HashMap<String, String> prodMap = new HashMap<String, String>();
		HashMap<String, String> prodSeasonMap = new HashMap<String, String>();
		String attVal= "";
		if (obj instanceof LCSProductSeasonLink) {
		final LCSProductSeasonLink seasonProdLink = (LCSProductSeasonLink) obj;
		LOGGER.debug("**seasonProdLink-effectSeq:"+seasonProdLink.getEffectSequence());
		LOGGER.debug("**seasonProdLink-effectlatest:"+seasonProdLink.isEffectLatest());
		if (seasonProdLink != null && "PRODUCT".equalsIgnoreCase(seasonProdLink.getSeasonLinkType().trim()) && seasonProdLink.isEffectLatest() && seasonProdLink.getEffectSequence()>0)	{
			LCSProduct prod = SeasonProductLocator.getProductSeasonRev(seasonProdLink);
			final LCSSeason season = SeasonProductLocator.getSeasonRev(prod);
			prod = (LCSProduct) VersionHelper.getFirstVersion(prod);
			prod = (LCSProduct) VersionHelper.latestIterationOf(prod);
			if(prd!=null)
				prod=prd;
			LOGGER.debug("**AllProdCriteriaAtts:"+AllProdCriteriaAtts);
			LOGGER.debug("**AllProdSeasonCriteriaAtts:"+AllProdSeasonCriteriaAtts);
			
			for (String attKey: AllProdCriteriaAtts) {
				attVal = prod.getValue(attKey)!=null?String.valueOf(prod.getValue(attKey)):"";
				prodMap.put(attKey, attVal);
			}
			for (String attKey: AllProdSeasonCriteriaAtts) {
				attVal = seasonProdLink.getValue(attKey)!=null?String.valueOf(seasonProdLink.getValue(attKey)):"";
				prodSeasonMap.put(attKey, attVal);
			}
			
			LOGGER.debug("**prodMap:"+prodMap);
			LOGGER.debug("**prodSeasonMap:"+prodSeasonMap);
			LOGGER.debug("**TargetProductSeasonAtts:"+TargetProductSeasonAtts);
			
			LCSLifecycleManaged businessSegmentMappingBO = findBusinessSegmentData(prodMap,prodSeasonMap);
			
			if(businessSegmentMappingBO!=null) {
			for (String attKey: TargetProductSeasonAtts) {
				attVal = businessSegmentMappingBO.getValue(attKey)!=null?String.valueOf(businessSegmentMappingBO.getValue(attKey)):"";
				seasonProdLink.setValue(attKey, attVal);
				LOGGER.debug("**setting value::"+attKey+":::"+attVal);
				}
			}
			else {
				for (String attKey: TargetProductSeasonAtts) {
					LOGGER.debug("**Processing for blank:"+attKey);
					seasonProdLink.setValue(attKey, "");
					}
			}
			
		}
	}
		
	}
	
	/**
	 * @param obj
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static void setBusinessSegmentAttValFromProd(WTObject obj) throws WTException, WTPropertyVetoException{
		
		LCSProduct prd = (LCSProduct)obj;
		LOGGER.debug("RECEIVED Product:"+prd);
		
		final LCSProduct previousItProduct = (LCSProduct) VersionHelper.predecessorOf(prd);
		LOGGER.debug("**previousItProduct:"+ previousItProduct);
		
		String currentAgeVal="";
		String previousAgeVal="";
		
		String currentGenderVal="";
		String previousGenderVal="";
		boolean changed=false;
		if (previousItProduct != null) {
			previousAgeVal= previousItProduct.getValue("scAge")!=null?String.valueOf(previousItProduct.getValue("scAge")):"";
			currentAgeVal= prd.getValue("scAge")!=null?String.valueOf(prd.getValue("scAge")):"";
			LOGGER.debug("**previousAgeVal:"+previousAgeVal+":::"+"currentAgeVal:"+ currentAgeVal);
			if(!previousAgeVal.equalsIgnoreCase(currentAgeVal)) {
				changed=true;
				}
			
			previousGenderVal= previousItProduct.getValue("scGender")!=null?String.valueOf(previousItProduct.getValue("scGender")):"";
			currentGenderVal= prd.getValue("scGender")!=null?String.valueOf(prd.getValue("scGender")):"";
			LOGGER.debug("**previousGenderVal:"+previousGenderVal+":::"+"currentGenderVal:"+ currentGenderVal);
			if(!previousGenderVal.equalsIgnoreCase(currentGenderVal)) {
				changed=true;
				}
			}
		if(changed) {
			final LCSSeasonQuery seasonQuery = new LCSSeasonQuery();
			final Collection<LCSSeasonProductLink> seasonProductLinks = seasonQuery.findSeasonProductLinks(prd);
			LOGGER.debug("seasonProductLinks:"+seasonProductLinks.size()+":::"+seasonProductLinks);
			for(LCSSeasonProductLink seasonProductLink : seasonProductLinks) {
				if(seasonProductLink!=null && seasonProductLink.getSeasonLinkType().equalsIgnoreCase("PRODUCT")) {
					LCSProductSeasonLink prodSeasonLink = (LCSProductSeasonLink)seasonProductLink;
					if(prodSeasonLink.isEffectLatest() && prodSeasonLink.getEffectSequence()>0) {
						setBusinessSegmentAttValFromProdLink(prodSeasonLink, prd);
						LCSProductLogic.persist(prodSeasonLink, true);
						LOGGER.debug("**Persisted prodSeasonLink:"+prodSeasonLink);
					}
				}
			}
		}
		
	
		
		
	}
	/**
	 * @param obj
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 * This is  starter method to perform the valu lookup, the same is configured in SSP properties
	 */
	public static void setBusinessSegmentAttVal(WTObject obj) throws WTException, WTPropertyVetoException {
		
		LOGGER.debug("**setBusinessSegmentAttVal:"+obj +"::"+((FlexTyped)obj).getFlexType().getFullNameDisplay(true));
		
	if(((FlexTyped)obj).getFlexType().getFullNameDisplay(true).contains("Reebok")){
		
		if (obj instanceof LCSProductSeasonLink) {
			setBusinessSegmentAttValFromProdLink(obj,null);
		}
		else if (obj instanceof LCSProduct) {
			setBusinessSegmentAttValFromProd(obj);
		}
	}
	}
	
	
	/**
	 * @param prodMap
	 * @param prodSeasonMap
	 * @return
	 * @throws WTException
	 */
	public static LCSLifecycleManaged findBusinessSegmentData(HashMap prodMap,HashMap prodSeasonMap) throws WTException {
		LOGGER.debug("**prodMap1:"+prodMap);
		LOGGER.debug("**prodSeasonMap1:"+prodSeasonMap);
		LCSLifecycleManaged businessSegmentMappingBO = null;
		FlexType businessSegmentMappingType = FlexTypeCache.getFlexTypeFromPath(BUSINESS_SEGMENT_MAPPING_TYPE);
		String typeIdPath = businessSegmentMappingType.getIdPath();
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable(LCSLifecycleManaged.class);
		statement.appendSelectColumn(new QueryColumn(LCSLifecycleManaged.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendCriteria(new Criteria("LCSLIFECYCLEMANAGED", "FLEXTYPEIDPATH", "%"+typeIdPath+"%", Criteria.LIKE));
		statement.setDistinct(true);
		Set<Entry> set1 = prodMap.entrySet();
		Set<Entry> set2 = prodSeasonMap.entrySet();
		String attColumnName="";
		for (Entry entry: set1) {
			attColumnName = businessSegmentMappingType.getAttribute(String.valueOf(entry.getKey())).getColumnName();
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria("LCSLIFECYCLEMANAGED", attColumnName, String.valueOf(entry.getValue()), Criteria.EQUALS));
		
		}
		for (Entry entry: set2) {
			attColumnName = businessSegmentMappingType.getAttribute(String.valueOf(entry.getKey())).getColumnName();
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria("LCSLIFECYCLEMANAGED", attColumnName, String.valueOf(entry.getValue()), Criteria.EQUALS));
		}
		
		LOGGER.debug("***statement=" + statement);
		final SearchResults searchResults = LCSQuery.runDirectQuery(statement);
		if (searchResults.getResultsFound() == 1 ) {
			final FlexObject fob = (FlexObject) searchResults.getResults().iterator().next();
			LOGGER.debug("***fob:" + fob);
			final String boIDA2A2 = (String) fob.getString("LCSLIFECYCLEMANAGED.IDA2A2");
			if (FormatHelper.hasContent(boIDA2A2)) {
				 businessSegmentMappingBO = (LCSLifecycleManaged) LCSQuery.findObjectById("OR:com.lcs.wc.foundation.LCSLifecycleManaged:" + boIDA2A2);	
			}
		}else if(searchResults.getResultsFound() > 1){
			throw new LCSException("There are duplicate Entries in Business Segment Lookup for the implied Criteria, Please contact System Administrator!!!");
			
		}else {
			LOGGER.debug("The Business Segment Lookup Values are not present for the implied Criteria, Please contact System Administrator!!!");
		}
		
		return businessSegmentMappingBO;
	}
}