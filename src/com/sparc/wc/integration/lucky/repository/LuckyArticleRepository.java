package com.sparc.wc.integration.lucky.repository;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.sparc.wc.integration.aero.repository.LCSObjectReferences;
import com.sparc.wc.integration.aero.repository.LCSTableColumns;
import com.sparc.wc.integration.aero.repository.LCSTables;
import com.sparc.wc.integration.constants.SparcIntegrationConstants;

import com.sparc.wc.integration.lucky.builders.LuckyColorwaySeasonLinkQuery;
import com.sparc.wc.integration.lucky.builders.LuckyProductColorwaySeasonLinkQueryBuilder;


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




import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.*;


public class LuckyArticleRepository {

    private static final Logger LOGGER = LogR.getLogger("LuckyArticleRepository");

    private LuckyArticleRepository() {

    }



	
	public static boolean resetIntegrationControlFlags(LCSSKUSeasonLink flexColorwaySeasonLink,Boolean isLegacyStyleChanged,boolean skipPersist) throws WTPropertyVetoException, WTException {
       boolean updated = false;

        if (flexColorwaySeasonLink == null) {
            return updated;
        }       
        
        flexColorwaySeasonLink.setValue(COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME,SENT_NO);
        flexColorwaySeasonLink.setValue(COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME,SENT_NO);
		if(isLegacyStyleChanged){
		  flexColorwaySeasonLink.setValue(FC_SHARED_ATTR,SENT_NO);
		}

        updated = updateColorwaySeasonLink(flexColorwaySeasonLink,skipPersist);

        return updated;
    }
 
    /**
     * Resets the Lucky Article integration control flags for the given Colorway-Season Link object unless they are already reset.<br>
     * Flags to update for the given Colorway-Season Link are:<br>
     * - Sent To FC: Sets 'scNo'.<br>
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
     * Resets the Lucky Article integration control flags for the given Colorway-Season Link object unless they are already reset.<br>
     * Flags to update for the given Colorway-Season Link are:<br>
     * - Sent To FC: Sets 'scNo'.<br>
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
        System.out.println("(String)flexColorwaySeasonLink.getValue(COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME)"+(String)flexColorwaySeasonLink.getValue(COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME));

        flexColorwaySeasonLink.setValue(COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME,SENT_NO);
        flexColorwaySeasonLink.setValue(COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME,SENT_NO); 

        updated = updateColorwaySeasonLink(flexColorwaySeasonLink,skipPersist);

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
    private static boolean updateColorwaySeasonLink(LCSSKUSeasonLink flexColorwaySeasonLink,boolean skipPersist) throws WTException, WTPropertyVetoException {

        boolean updated = false;
        boolean skipPlugins = true;
      
        if (skipPersist) {
            LOGGER.debug("[updateColorwaySeasonLink] Colorway-Season Link " + flexColorwaySeasonLink + " persistance is skipped.");
        } else {
            LCSLogic.persist(flexColorwaySeasonLink, skipPlugins);
            LOGGER.debug("[updateColorwaySeasonLink] Colorway-Season Link " + flexColorwaySeasonLink + " is updated.");
        }

        return updated;
    }


    /**
     * Get all available colorway season links from the given product and season.<br>
     * @param flexProduct The Product instance.
     * @param flexSeason The Season instance to filter out those product colorways that do not belong to it.
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

            if (flexSeasonProdLink != null && flexSeasonProdLink.isEffectLatest() && !flexSeasonProdLink.isSeasonRemoved()) {
                flexSKUSeasonLinks.add((LCSSKUSeasonLink)flexSeasonProdLink);
            }

        }

        return flexSKUSeasonLinks;
    }

    /**
     * Get all available colorway season links from the given product and associated Lucky valid seasons.<br>
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
        PreparedQueryStatement pqs = new LuckyProductColorwaySeasonLinkQueryBuilder(productNumber).build();

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
        PreparedQueryStatement pqs = new LuckyColorwaySeasonLinkQuery(colorwayNumber).build();

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
