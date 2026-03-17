package com.sparc.wc.integration.aero.repository;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_ARTICLE_LOGGER_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_CAP_SHARED_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_S4_SHARED_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_SENT_TO_CAP_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_SENT_TO_S4_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SENT_YES_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_TYPE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_VALID_DEVELOPMENT_SEASONS_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SENT_NO_VALUE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.integration.aero.builders.AeroArticleColorwaySeasonDetailsQueryBuilder;
import com.sparc.wc.integration.aero.builders.AeroCAPArticleDetailsQueryBuilder;
import com.sparc.wc.integration.aero.builders.AeroCAPArticleIdsQueryBuilder;
import com.sparc.wc.integration.aero.builders.AeroColorwaySeasonLinkQuery;
import com.sparc.wc.integration.aero.builders.AeroProductColorwaySeasonLinkQueryBuilder;
import com.sparc.wc.integration.aero.builders.AeroS4ArticleDetailsQueryBuilder;
import com.sparc.wc.integration.aero.builders.AeroS4ArticleIdsQueryBuilder;
import com.sparc.wc.integration.aero.domain.AeroApiCallLogEntry;
import com.sparc.wc.integration.aero.domain.AeroApiCallLogException;
import com.sparc.wc.integration.aero.domain.AeroArticleFlexTypeMaster;
import com.sparc.wc.integration.aero.domain.AeroProcessesParam;

import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * Repository for accessing the various data elements from PLM related to Aero/Article for S4 & CAP integrations.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Fixed use of colorway-season client model at updateColorwaySeasonLink().<br>
 * - Restore use of LCSLogic.persist to save colorway season link due to odd save issue when trying to update the flag while updating product season (from the article plugin)
 * and having "Target Retail" attribute value as 0.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 */
public class AeroArticleRepository {
	
	private static final Logger LOGGER = LogR.getLogger(AERO_ARTICLE_LOGGER_NAME);

	private AeroArticleRepository() {
		
	}
	
	/**
	 * Lookup and retrieve from PLM Colorway Numbers that meet the criteria for Aero integration with S4 & CAP.
	 * @param process The identifier for the Aero target system, i.e. S4 or CAP.
	 * @return The list of colorway numbers found.
	 * @throws WTException If a flex api error occurs while retrieving/accessing the data from PLM.
	 */
	public static Set<String> findArticleColorwayNumbers(AeroProcessesParam process) throws WTException {
		
		SearchResults qryResults = null; 
		Set<String> colorwayNumbers = new HashSet<String>();
		
		PreparedQueryStatement pqs = null;
		
		if (process == AeroProcessesParam.CAP) {
			pqs = new AeroCAPArticleIdsQueryBuilder().build(); 
		}  else if (process == AeroProcessesParam.S4) {
			pqs = new AeroS4ArticleIdsQueryBuilder().build();
		}
		
		LOGGER.debug("[findArticleColorwayNumbers for " + process + "] Query: " + pqs);
        qryResults = LCSQuery.runDirectQuery(pqs);
        LOGGER.debug("[findArticleColorwayNumbers for " + process + "] Query results: Found " + qryResults.getResultsFound() + " records.");
		
        if (qryResults != null) {
        	
        	@SuppressWarnings("unchecked")
			Collection<FlexObject> resultSet = qryResults.getResults();
        	for (FlexObject fo : resultSet) {
        		
        		final FlexType colorwayFlexType = FlexTypeCache.getFlexTypeFromPath(AERO_PRODUCT_FLEX_TYPE_PATH);
        		final String colorwayNumColName = colorwayFlexType.getAttribute(AERO_COLORWAY_NUMBER_ATTR).getColumnName();
        		
        		String colorwayNum = (String)fo.get(LCSTables.LCSSKU.name() + "." + colorwayNumColName);
                
                try{
					Long.parseLong(colorwayNum);
				}catch(Exception ex){
					LOGGER.warn("Skipping bad Colorway # found '" + colorwayNum + "'.");
					continue;
				}
                
                LOGGER.debug("Colorway #" + colorwayNum + " is eligible for Aero to " + process + " Integration.");
                
                colorwayNumbers.add(colorwayNum);
                
        	}
        	
        }
        
		return colorwayNumbers;
	}
	
	/**
	 * Retrieves article details for the given colorway number and target process.<br>
	 * Note that multiple records will be produced if the same colorway meet the required criteria for more than one season.
	 * @param colorwayNumber The colorway number to use as key for finding article details.
	 * @param process The identifier for the aero target system, i.e. S4 or CAP.
	 * @return The list of article master records containing all relevant flex objects with the attributes relevant to the article details (colorway).
	 * @throws WTException If an error occurs while retrieving data from PLM.
	 * @throws AeroApiCallLogException If no colorway is found when querying the system or the colorway doesn't meet the expected criteria.
	 */
	public static List<AeroArticleFlexTypeMaster> getArticleDetails(Long colorwayNumber, 
			AeroProcessesParam process) throws WTException, AeroApiCallLogException {
		
		List<AeroArticleFlexTypeMaster> flexArticleMasterList = new ArrayList<AeroArticleFlexTypeMaster>();
		
		if (colorwayNumber == null || process == null) {
			return flexArticleMasterList;
		}
		
		SearchResults qryResults = null; 
		PreparedQueryStatement pqs = null;
		
		if (process == AeroProcessesParam.CAP) {
			pqs = new AeroCAPArticleDetailsQueryBuilder(Long.toString(colorwayNumber)).build(); 
		}  else if (process == AeroProcessesParam.S4) {
			pqs = new AeroS4ArticleDetailsQueryBuilder(Long.toString(colorwayNumber)).build();
		}
		
		LOGGER.debug("[getArticleDetails for Colorway Number " + colorwayNumber + " (" + process + ")] "
				+ "Query: " + pqs);
        qryResults = LCSQuery.runDirectQuery(pqs);
        LOGGER.debug("[getArticleDetails for Colorway Number " + colorwayNumber + " (" + process + ")] "
        		+ "Query results: Found " + qryResults.getResultsFound() + " records.");
		
        if (qryResults == null || qryResults.getResultsFound() == 0) {
        	throw new AeroApiCallLogException("Unable to locate colorway or colorway doesn't meet the expected selection criteria.", 
        			colorwayNumber.toString(), 
        			AeroApiCallLogEntry.ErrorTypes.NOT_FOUND);
        }
        
        @SuppressWarnings("unchecked")
		Collection<FlexObject> resultSet = qryResults.getResults();
    	
    	for (FlexObject fo : resultSet) {
    		
    		flexArticleMasterList.add(createArticleDetailsMaster(fo));
    		
    	}//end for loop.
        
        return flexArticleMasterList;        
	}
	
	/**
	 * Creates a AeroArticleFlexTypeMaster instance by loading it with the respective flex objects from PLM using 
	 * the flex object internal identifiers provided within the given flex object.
	 * @param flexObjectIdsMap The flex object containing the flex object internal identifiers to be loaded.
	 * @return The Article Details Master instance with all available Flex Objects loaded where applicable.
	 */
	private static AeroArticleFlexTypeMaster createArticleDetailsMaster(FlexObject flexObjectIdsMap) throws WTException {
		return loadArticleDetailsMaster(new AeroArticleFlexTypeMaster(), flexObjectIdsMap, false);
	}
	
	/**
	 * Populates an AeroArticleFlexTypeMaster instance by loading it with the respective flex objects from PLM using 
	 * the flex object internal identifiers provided within the given flex object.
	 * @param flexArticleMaster The AeroArticleFlexTypeMaster instance to load with Flex Objects.
	 * @param flexObjectIdsMap The flex object containing the flex object internal identifiers to be loaded.
	 * @param forceLoadIfExist If set to true, it forces the flex object to be populated into the given AeroArticleFlexTypeMaster instance even 
	 * if one is already assigned into it. 
	 * @return The Article Details Master instance with all available Flex Objects loaded where applicable.
	 * @throws WTException If an error occurs while retrieving flex objects from PLM.
	 */
	private static AeroArticleFlexTypeMaster loadArticleDetailsMaster(AeroArticleFlexTypeMaster flexArticleMaster, 
			FlexObject flexObjectIdsMap, boolean forceLoadIfExist) throws WTException {
		
		if (flexObjectIdsMap == null) {
			return flexArticleMaster;
		}
		
		//Prep Flex Object IDs
		String colorwayFlexId = (String)flexObjectIdsMap.get(LCSTables.LCSSKU.name() + "." + LCSTableColumns.IDA2A2.name());
		String productFlexId = (String)flexObjectIdsMap.get(LCSTables.LCSPRODUCT.name() + "." + LCSTableColumns.IDA2A2.name());
		String seasonFlexId = (String)flexObjectIdsMap.get(LCSTables.LCSSEASON.name() + "." + LCSTableColumns.IDA2A2.name());
		String colorwaySeasonLinkId = (String)flexObjectIdsMap.get(LCSTables.LCSSKUSEASONLINK.name() + "." + LCSTableColumns.IDA2A2.name());
		String productSeasonLinkId = (String)flexObjectIdsMap.get(LCSTables.LCSPRODUCTSEASONLINK.name() + "." + LCSTableColumns.IDA2A2.name());
		
		//Load Colorway (SKU).
		if (colorwayFlexId != null && (flexArticleMaster.getColorway() == null || forceLoadIfExist)) {
			flexArticleMaster.setColorway((LCSSKU)LCSQuery.findObjectById(LCSObjectReferences.LCSSKU.getObjRefString() + colorwayFlexId));
		}
		
		//Load Product.
		if (productFlexId != null && (flexArticleMaster.getProduct() == null || forceLoadIfExist)) {
			flexArticleMaster.setProduct(
					(LCSProduct)LCSQuery.findObjectById(LCSObjectReferences.LCSPRODUCT.getObjRefString() + productFlexId));
		}
		
		//Load Season
		if (seasonFlexId != null && (flexArticleMaster.getSeason() == null || forceLoadIfExist)) {
			flexArticleMaster.setSeason((LCSSeason)LCSQuery.findObjectById(LCSObjectReferences.LCSSEASON.getObjRefString() + seasonFlexId));
		}
				
		//Load Colorway Season Link.
		if (colorwaySeasonLinkId != null && (flexArticleMaster.getColorwaySeasonLink() == null || forceLoadIfExist)) {
			flexArticleMaster.setColorwaySeasonLink(
					(LCSSKUSeasonLink)LCSQuery.findObjectById(LCSObjectReferences.LCSSKUSEASONLINK.getObjRefString() + colorwaySeasonLinkId));
		}
		
		//Load Product Season Link.
		if (productSeasonLinkId != null && (flexArticleMaster.getProductSeasonLink() == null || forceLoadIfExist)) {
			flexArticleMaster.setProductSeasonLink(
					(LCSProductSeasonLink)LCSQuery.findObjectById(LCSObjectReferences.LCSPRODUCTSEASONLINK.getObjRefString() + productSeasonLinkId));
		}
		
		return flexArticleMaster;
	}
	
	/**
	 * Retrieves the primary source to season link for the given product & season.
	 * @param flexProduct The product to get the sourcing config from.
	 * @param flexSeason The season to get the sourcing config from.
	 * @return The primary source to season link object or <code>null</code> if not found or if either product or season objects were not provided.
	 * @throws WTException If an error occurs while looking up the source to season link at PLM.
	 */
	public static LCSSourceToSeasonLink getPrimarySourceToSeasonLink(LCSProduct flexProduct, LCSSeason flexSeason) throws WTException {
		
		if(flexProduct == null || flexSeason == null) {
			return null;
		}
		
		return LCSSourcingConfigQuery.getPrimarySourceToSeasonLink(flexProduct.getMaster(), flexSeason.getMaster());
		
	}
	
	/**
	 * Retrieves the colorway season details for the given colorway number.
	 * @param colorwayNumber The colorway number to get the details from.
	 * @return A list of flex master records, containing relevant flex objects for colorway season details. 
	 * These are: Season, Colorway, Colorway Season Link and Product.
	 * @throws WTException If an error occurs while retrieving data from PLM.
	 */
	public static List<AeroArticleFlexTypeMaster> getColorwaySeasonDetails(String colorwayNumber) throws WTException {
		
		List<AeroArticleFlexTypeMaster> flexArticleMasterList = new ArrayList<AeroArticleFlexTypeMaster>();
		
		if (colorwayNumber == null) {
			return flexArticleMasterList;
		}
		
		SearchResults qryResults = null; 
		PreparedQueryStatement pqs = new AeroArticleColorwaySeasonDetailsQueryBuilder(colorwayNumber).build();
		
		LOGGER.debug("[getArticleColorwaySeasonDetails for Colorway Number " + colorwayNumber + "] Query: " + pqs);
        qryResults = LCSQuery.runDirectQuery(pqs);
        LOGGER.debug("[getArticleColorwaySeasonDetails for Colorway Number " + colorwayNumber + "] "
        		+ "Query results: Found " + qryResults.getResultsFound() + " records.");
		
        if (qryResults != null && qryResults.getResultsFound() > 0) {
        	
        	@SuppressWarnings("unchecked")
			Collection<FlexObject> resultSet = qryResults.getResults();
        	
        	for (FlexObject fo : resultSet) {
        		
        		flexArticleMasterList.add(createArticleDetailsMaster(fo));
        		
        	}//end for loop.
        	
        }
        
		return flexArticleMasterList;
	}
	
	/**
	 * Resets the Aero Article integration control flags for the given Colorway-Season Link object unless they are already reset.<br>
	 * Flags to update for the given Colorway-Season Link are:<br>
	 * - Sent To S4: Sets 'scNo'.<br>
	 * - Sent To CAP: Sets 'scNo'.<br>
	 * @param flexColorwaySeasonLink The Colorway-Season Link where the flags to be updated are located.
	 * @return true if the flags have been reset (updated/saved), false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while attempting to update an invalid attribute within the flex object.
	 */
	public static boolean resetIntegrationControlFlags(LCSSKUSeasonLink flexColorwaySeasonLink) throws WTPropertyVetoException, WTException {
		return resetIntegrationControlFlags(flexColorwaySeasonLink, false);
	}
	
	/**
	 * Resets the Aero Article integration control flags for the given Colorway-Season Link object unless they are already reset.<br>
	 * Flags to update for the given Colorway-Season Link are:<br>
	 * - Sent To S4: Sets 'scNo'.<br>
	 * - Sent To CAP: Sets 'scNo'.<br>
	 * @param flexColorwaySeasonLink The Colorway-Season Link where the flags to be updated are located.
	 * @param skipPersist Skips executing a persist on the flex object.
	 * @return true if the flags have been reset (updated/saved), false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while attempting to update an invalid attribute within the flex object.
	 */
	public static boolean resetIntegrationControlFlags(LCSSKUSeasonLink flexColorwaySeasonLink, boolean skipPersist) throws WTPropertyVetoException, WTException {
		
		boolean updated = false;
		
		if (flexColorwaySeasonLink == null) {
			return updated;
		}
		
		Map<String, Object> colorwaySeasonFlags = new HashMap<String, Object>();
		
		if (AERO_SENT_YES_VALUE.equalsIgnoreCase((String)flexColorwaySeasonLink.getValue(AERO_COLORWAY_SENT_TO_CAP_ATTR))) {
			colorwaySeasonFlags.put(AERO_COLORWAY_SENT_TO_CAP_ATTR, AERO_SENT_NO_VALUE);
		}
		
		if (AERO_SENT_YES_VALUE.equalsIgnoreCase((String)flexColorwaySeasonLink.getValue(AERO_COLORWAY_SENT_TO_S4_ATTR))) {
			colorwaySeasonFlags.put(AERO_COLORWAY_SENT_TO_S4_ATTR, AERO_SENT_NO_VALUE);
		}
		
		if (!colorwaySeasonFlags.isEmpty()) {
			updated = updateColorwaySeasonLink(flexColorwaySeasonLink, colorwaySeasonFlags, skipPersist);
		} else {
			LOGGER.debug("No need to reset Aero Article integration flags for colorway season link " + flexColorwaySeasonLink + ".");
		}
		
		return updated;
	}
	
	/**
	 * Updates the Aero Article integration control flags for the given Colorway-Season Link and Process.<br>
	 * Flags to update according the specs are:<br>
	 * - Sent To S4: Sets 'scYes' (if process is S4).<br>
	 * - S4 Shared: Sets to 'scYes' unless already set to that value (if process is S4).<br>
	 * - Sent To CAP: Sets 'scYes' (if process is CAP).<br>
	 * - CAP Shared: Sets to 'scYes' unless already set to that value (if process is CAP).
	 * @param process The target system, either S4 or CAP.
	 * @param flexColorwaySeasonLink The Colorway-Season Link where the flags to be updated are located.
	 * @return true if the flags hasve been updated, false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while attempting to update an invalid attribute within the flex object.
	 */
	public static boolean updateIntegrationControlFlags(AeroProcessesParam process, 
			LCSSKUSeasonLink flexColorwaySeasonLink) throws WTPropertyVetoException, WTException {
		
		boolean updated = false;
		
		if (process == null || flexColorwaySeasonLink == null) {
			return updated;
		}
		
		Map<String, Object> colorwaySeasonFlags = new HashMap<String, Object>();
		
		if (process == AeroProcessesParam.CAP) {
			colorwaySeasonFlags.put(AERO_COLORWAY_SENT_TO_CAP_ATTR, AERO_SENT_YES_VALUE);
			
			if (!AERO_SENT_YES_VALUE.equalsIgnoreCase((String)flexColorwaySeasonLink.getValue(AERO_COLORWAY_CAP_SHARED_ATTR))) {
				colorwaySeasonFlags.put(AERO_COLORWAY_CAP_SHARED_ATTR, AERO_SENT_YES_VALUE);
			}
			
		} else if (process == AeroProcessesParam.S4) {
			colorwaySeasonFlags.put(AERO_COLORWAY_SENT_TO_S4_ATTR, AERO_SENT_YES_VALUE);
			
			if (!AERO_SENT_YES_VALUE.equalsIgnoreCase((String)flexColorwaySeasonLink.getValue(AERO_COLORWAY_S4_SHARED_ATTR))) {
				colorwaySeasonFlags.put(AERO_COLORWAY_S4_SHARED_ATTR, AERO_SENT_YES_VALUE);
			}
		}
		
		boolean skipPersist = false;
		updated = updateColorwaySeasonLink(flexColorwaySeasonLink, colorwaySeasonFlags, skipPersist);
		
		return updated;
	}
	
	/**
	 * Updates (save/commit) the values for the given attr_key-values map into the given colorway-season link instance.
	 * @param flexColorwaySeasonLink The Colorway-Season Link link to update.
	 * @param values The attr_key-value maps containing the data to update into the Colorway-Season Link.
	 * @param skipPersist Skips executing a persist on the flex object.
	 * @return true if the values have been updated, false otherwise.
	 * @throws WTException If an error occurs while processing the flag update at PLM.
	 * @throws WTPropertyVetoException If an error occurs while attempting to update an invalid attribute within the flex object.
	 */
	private static boolean updateColorwaySeasonLink(LCSSKUSeasonLink flexColorwaySeasonLink,
			Map<String, Object> values,
			boolean skipPersist) throws WTException, WTPropertyVetoException {
		
		boolean updated = false;
		boolean skipPlugins = true;
		
		if (flexColorwaySeasonLink == null || values == null || values.isEmpty()) {
			return updated;
		}
		
		Set<String> flagKeys = values.keySet();
		
		for (String key : flagKeys) {
			flexColorwaySeasonLink.setValue(key, values.get(key));
        }
		
		if (skipPersist) {
			LOGGER.debug("[updateColorwaySeasonLink] Colorway-Season Link " + flexColorwaySeasonLink + " persistance is skipped.");
		} else {
			LCSLogic.persist(flexColorwaySeasonLink, skipPlugins);
			LOGGER.debug("[updateColorwaySeasonLink] Colorway-Season Link " + flexColorwaySeasonLink + " is updated.");
		}
		
		return updated;
	}
	
	/**
	 * Retrieves a list of valid Aero seasons associated to the given product.
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
		
		seasons = new LCSSeasonQuery().findSeasons(flexProduct);
		
		for(LCSSeasonMaster master: seasons) {
			
			LCSSeason seasonObj = (LCSSeason) VersionHelper.latestIterationOf(master);
			
			if(seasonObj!=null) {
				
				String developmentSeason = (String)seasonObj.getValue(AERO_SEASON_TYPE_ATTR);
				
				if (!AERO_VALID_DEVELOPMENT_SEASONS_VALUES.contains(developmentSeason)) {
					LOGGER.debug("[getValidSeasonsForProduct] Skipping season " + seasonObj.getName() + " as it has an invalid Development Season Type value.");
					continue;
				}
				
				seasonsList.add(seasonObj);
				
			}
		}
		
		return seasonsList;		
	}
	
	/**
	 * Get all available colorway season links from the given product and season.<br>
	 * @param flexProduct The Product instance.
	 * @param flexSeason The Season instance to filter out those product colorways that do not belong to it.
	 * @param flexSrcConfig The Sourcing Configuration to check the colorways are active.
	 * @return The list of active colorways associated to the product and which are also available at the given season.
	 * @throws WTException If an error occurs while looking up colorways and other meta data at PLM.
	 */
	public static List<LCSSKUSeasonLink> getProductColorwaysSeasonLinks(LCSProduct flexProduct, 
			LCSSeason flexSeason) throws WTException {
		
		List<LCSSKUSeasonLink> flexSKUSeasonLinks = new ArrayList<>();
		
		if (flexProduct == null || flexSeason == null) {
			return flexSKUSeasonLinks;
		}
		
		boolean includePlaceholders = false;
		@SuppressWarnings("unchecked")
		Iterator<LCSSKU> skusItr = LCSSKUQuery.findSKUs(flexProduct, includePlaceholders).iterator();
		
        while(skusItr.hasNext()){
        	
            LCSSKU currentSku = (LCSSKU)skusItr.next();
            
            //Check colorway is available at the given season
            LCSSeasonProductLink flexSeasonProdLink = LCSSeasonQuery.findSeasonProductLink(currentSku, flexSeason); 
            
            if (flexSeasonProdLink != null && flexSeasonProdLink.isEffectLatest()) {
            	flexSKUSeasonLinks.add((LCSSKUSeasonLink)flexSeasonProdLink);
            }
            
        }
		
        return flexSKUSeasonLinks;
	}
	
	/**
	 * Get all available colorway season links from the given product and associated Aero valid seasons.<br>
	 * @param productNumber The product number to search for colorway season links.
	 * @return The list of colorway season links for the product.
	 * @throws WTException If an error occurs while looking up colorways and other meta data at PLM.
	 */
	public static List<LCSSKUSeasonLink> getProductColorwaysSeasonLinks(Long productNumber) throws WTException {
		
		List<LCSSKUSeasonLink> flexSKUSeasonLinks = new ArrayList<>();
		
		if (productNumber == null) {
			return flexSKUSeasonLinks;
		}
		
		SearchResults qryResults = null; 
		PreparedQueryStatement pqs = new AeroProductColorwaySeasonLinkQueryBuilder(productNumber).build();
		
		LOGGER.debug("[getProductColorwaysSeasonLinks for Product Number " + productNumber + "] Query: " + pqs);
        qryResults = LCSQuery.runDirectQuery(pqs);
        LOGGER.debug("[getProductColorwaysSeasonLinks for Product Number " + productNumber + "] "
        		+ "Query results: Found " + qryResults.getResultsFound() + " records.");
		
        if (qryResults != null && qryResults.getResultsFound() > 0) {
        	
        	@SuppressWarnings("unchecked")
			Collection<FlexObject> resultSet = qryResults.getResults();
        	
        	for (FlexObject fo : resultSet) {
        		
        		String colorwaySeasonLinkId = (String)fo.get(LCSTables.LCSSKUSEASONLINK.name() + "." + LCSTableColumns.IDA2A2.name());
        		LOGGER.debug("[getColorwaySeasonLink for Product Number " + productNumber + "] Colorway Season Link ID = " + colorwaySeasonLinkId);
        		
        		flexSKUSeasonLinks.add((LCSSKUSeasonLink)LCSQuery.findObjectById(LCSObjectReferences.LCSSKUSEASONLINK.getObjRefString() + colorwaySeasonLinkId));
        		
        	}//end for loop.
        	
        }
        
		return flexSKUSeasonLinks;
	}
	
	/**
	 * Get the list of colorway season links for the given colorway number.<br>
	 * @param colorwayNumber The colorway number to search for colorway season links.
	 * @return The list of colorway season links for the colorway number.
	 * @throws WTException If an error occurs while looking up colorways and other meta data at PLM.
	 */
	public static List<LCSSKUSeasonLink> getColorwaySeasonLink(Long colorwayNumber) throws WTException {
		
		List<LCSSKUSeasonLink> flexSKUSeasonLinks = new ArrayList<>();
		
		if (colorwayNumber == null) {
			return flexSKUSeasonLinks;
		}
		
		SearchResults qryResults = null; 
		PreparedQueryStatement pqs = new AeroColorwaySeasonLinkQuery(colorwayNumber).build();
		
		LOGGER.debug("[getColorwaySeasonLink for Colorway Number " + colorwayNumber + "] Query: " + pqs);
        qryResults = LCSQuery.runDirectQuery(pqs);
        LOGGER.debug("[getColorwaySeasonLink for Colorway Number " + colorwayNumber + "] "
        		+ "Query results: Found " + qryResults.getResultsFound() + " records.");
		
        if (qryResults != null && qryResults.getResultsFound() > 0) {
        	
        	@SuppressWarnings("unchecked")
			Collection<FlexObject> resultSet = qryResults.getResults();
        	
        	for (FlexObject fo : resultSet) {
        		
        		String colorwaySeasonLinkId = (String)fo.get(LCSTables.LCSSKUSEASONLINK.name() + "." + LCSTableColumns.IDA2A2.name());
        		LOGGER.debug("[getColorwaySeasonLink for Colorway Number " + colorwayNumber + "] Colorway Season Link ID = " + colorwaySeasonLinkId);
        		
        		flexSKUSeasonLinks.add((LCSSKUSeasonLink)LCSQuery.findObjectById(LCSObjectReferences.LCSSKUSEASONLINK.getObjRefString() + colorwaySeasonLinkId));
        		
        	}//end for loop.
        	
        }
        
		return flexSKUSeasonLinks;
	}
	
}
