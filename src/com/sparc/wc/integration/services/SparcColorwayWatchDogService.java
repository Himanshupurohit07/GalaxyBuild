package com.sparc.wc.integration.services;

import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.*;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.integration.aero.domain.AeroCustomPluginException;
import com.sparc.wc.integration.aero.repository.AeroArticleRepository;
import com.sparc.wc.integration.constants.SparcIntegrationConstants;
import com.sparc.wc.integration.domain.SparcPropertyDefinitions;
import com.sparc.wc.integration.lucky.domain.LuckyCustomPluginException;
import com.sparc.wc.integration.lucky.repository.LuckyArticleRepository;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.sparc.wc.integration.util.SparcQueryUtil;
import org.apache.logging.log4j.Logger;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import com.lcs.wc.flextype.FlexTyped;


import java.util.*;
import java.util.stream.Collectors;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_CAP_ADOPTION_LINE;

/**
 * SparcColorwayWatchDogService class.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Updated method "setFlag": Replaced use of "LCSSeasonSKULinkClientModel" by "LCSLogic" approach to skip execution of other SSP.<br> 
 * 
 * @author Acnovate
 */
public class SparcColorwayWatchDogService {

    private final static Logger LOGGER = LogR.getLogger(SparcColorwayWatchDogService.class.getName());

    public void check(final LCSSKU deltaChangesSku) throws WTPropertyVetoException, WTException, LuckyCustomPluginException {
        LOGGER.info("Triggered Colorway watchdog service");
      
        if(deltaChangesSku == null)
            return;

        Long colorwayNumber = (Long)deltaChangesSku.getValue(AERO_COLORWAY_NUMBER_ATTR);
        LOGGER.debug("[validateColorway] Colorway # to check is: " + colorwayNumber + ".");
        if (deltaChangesSku.isPlaceholder()) {
            throw new LuckyCustomPluginException("Colorway #" + colorwayNumber + " is a placeholder.");
        }

        LCSSKU existingColorway  = (LCSSKU) VersionHelper.predecessorOf(deltaChangesSku);
        if (existingColorway == null) {
            throw new LuckyCustomPluginException("No previous version found for colorway #" + colorwayNumber + ".");
        }
       
		String version =  wt.vc.VersionControlHelper.getVersionIdentifier((wt.vc.Versioned) deltaChangesSku).getValue();
        String iteration = wt.vc.VersionControlHelper.getIterationIdentifier((wt.vc.Iterated) deltaChangesSku).getValue();
		
		LOGGER.debug("SparcColorwayWatchDogService---version----------"+version);
		LOGGER.debug("SparcColorwayWatchDogService---iteration---------"+iteration);
		
		
		if(deltaChangesSku.getCarriedOverFrom() != null){
		  LOGGER.debug("SparcColorwayWatchDogService Skipping the Plugins as this is a carry over");
		  return;
		}

        List<LCSSKUSeasonLink> colorwaySeasonLinkList = LuckyArticleRepository.getColorwaySeasonLink(colorwayNumber);
      
        if (colorwaySeasonLinkList == null || colorwaySeasonLinkList.isEmpty()) {
            throw new LuckyCustomPluginException("Unable to find colorway season links for colorway #" + colorwayNumber + ".");
        }
       
	    validateAttrNonBlanks(deltaChangesSku, colorwaySeasonLinkList, SparcIntegrationConstants.LUCKY_COLORWAY_PLUGIN_PROPERTY_DEFINITIONS.getNonBlankAttrs());
		
		Set<String> changedAttrList = SparcIntegrationUtil.getChangedAttributes(deltaChangesSku,existingColorway,SparcIntegrationConstants.LUCKY_COLORWAY_PLUGIN_PROPERTY_DEFINITIONS.getAttrsFromHierarchy());     

        if (changedAttrList.isEmpty()) {
            throw new LuckyCustomPluginException("No changes found for colorway #" + colorwayNumber + ".");
        }
		
		validateAttrLocked(deltaChangesSku, colorwaySeasonLinkList, changedAttrList);

        for (LCSSKUSeasonLink colorwaySeasonLink : colorwaySeasonLinkList) {
		    LuckyArticleRepository.resetIntegrationControlFlags(colorwaySeasonLink);
        }

    }
	
	
	   private static void validateAttrLocked(FlexTyped flexTyped,
                                           List<LCSSKUSeasonLink> skuSeasonLinkList,
                                           Set<String> attrList) throws LCSException,WTException {

        if (skuSeasonLinkList == null || skuSeasonLinkList.isEmpty() || attrList == null || attrList.isEmpty()) {
            LOGGER.debug("[validateAttrLocked] Skipping locked validation check.");
            return;
        }

		 boolean mustValidate = false;
		  for (LCSSKUSeasonLink skuSL : skuSeasonLinkList) {
            mustValidate = isProductionColorwayStatusValid(skuSL);

            if (mustValidate) {
                break;
            }
        }
		
        if (mustValidate) {
            Set<String> lockedAttrs = SparcIntegrationConstants.LUCKY_COLORWAY_PLUGIN_PROPERTY_DEFINITIONS.getLockedAttrs();
            StringBuilder lockAttributes = new StringBuilder();
            attrList.retainAll(lockedAttrs);
            for(String strAttr: attrList){
                if (lockAttributes.length() > 0) {
                    lockAttributes.append(", ");
                }
                lockAttributes.append(SparcIntegrationUtil.convertToDisplayName(strAttr,flexTyped));
            }
            if (lockAttributes.length() > 0) {
                throw new LCSException("Requested action is unable to complete.\n\r"
                        + "The following list of attributes are locked and cannot be modified: " + SparcIntegrationUtil.convertToDisplayNames(lockedAttrs, flexTyped) + ".");
            }
		}
      }	  
	  
	private static void validateAttrNonBlanks(FlexTyped flexTyped,
                                              List<LCSSKUSeasonLink> skuSeasonLinkList,
                                              Set<String> attrList) throws LCSException,WTException {

        if (skuSeasonLinkList == null || skuSeasonLinkList.isEmpty() || attrList == null || attrList.isEmpty()) {
            LOGGER.debug("[validateAttrNonBlanks] Skipping non blank validation check.");
            return;
        }  
		
		boolean mustValidate = false;
		  for (LCSSKUSeasonLink skuSL : skuSeasonLinkList) {
            mustValidate = isProductionColorwayStatusValid(skuSL);

            if (mustValidate) {
                break;
            }
        }

 // boolean emptyValueDetected = false;
          Set<String> blankAtt= new HashSet<>() ;

        if (mustValidate) {

         Iterator<String> listItr    = attrList.iterator();
            while(listItr.hasNext()) {
                String attrObj = listItr.next();
                final Object deltaChange = SparcIntegrationUtil.getValueFrom(flexTyped, attrObj);
                if (deltaChange instanceof Boolean && (deltaChange == null || !(Boolean) deltaChange)) {
                    blankAtt.add(attrObj);
                }
                else if (deltaChange == null || deltaChange.toString().trim().isEmpty()) {
                    blankAtt.add(attrObj);
                }

            }

        }
        if (!blankAtt.isEmpty()) {
            throw new LCSException("The attributes in the given list:" + SparcIntegrationUtil.convertToDisplayNames(blankAtt, flexTyped) + " cannot be null/empty when the Colorway status is in Production");
        }
    } 

	 private static boolean isProdColorwayStatusValidForBlankCheck(LCSSKUSeasonLink colorwaySeasonLink) throws WTException {
        final Object capAdoptionLine = colorwaySeasonLink.getValue(LUCKY_CAP_ADOPTION_LINE);
        final Object value = colorwaySeasonLink.getValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_COLORWAY_STATUS_INTERNAL_NAME);
        if ((value != null && value.toString().equals(SparcIntegrationConstants.COLORWAY_TO_SEASON_COLORWAY_STATUS_PRODUCTION))
                && (capAdoptionLine != null && capAdoptionLine instanceof Boolean && (Boolean) capAdoptionLine)) {
            return true;
        }
        return false;
    }
	
	
	private static boolean isProductionColorwayStatusValid(LCSSKUSeasonLink colorwaySeasonLink) throws WTException {
        final Object capAdoptionLine = colorwaySeasonLink.getValue(LUCKY_CAP_ADOPTION_LINE);
        final Object value = colorwaySeasonLink.getValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_COLORWAY_STATUS_INTERNAL_NAME);
        if (value != null && value.toString().equals(SparcIntegrationConstants.COLORWAY_TO_SEASON_COLORWAY_STATUS_PRODUCTION)) {
            return true;
        }
        return false;
    }
	
     private void checkNonBlankValues(final LCSSKU deltaChangesSku, final LCSSKU existingSku) throws LCSException {
        final Set<String> nonBlankAttrs         = SparcIntegrationConstants.COLORWAY_PAYLOAD_PROPERTY_DEFINITIONS.getNonBlankAttrs();
        final Set<String> explicitColorwayAttrs = SparcIntegrationConstants.COLORWAY_PAYLOAD_COLORWAY_PROPERTY_DEFINITIONS.getNonBlankAttrs();
        if (explicitColorwayAttrs != null) {
            nonBlankAttrs.addAll(explicitColorwayAttrs);
        }
        final Set<String> allAttributes = SparcIntegrationUtil.getAllAttributes(existingSku.getFlexType(), "PRODUCT", "SKU");
        allAttributes.retainAll(nonBlankAttrs);
        if (allAttributes.isEmpty()) {
            return;
        }
        final LCSProduct product = getProduct(existingSku);
        if (product == null) {
            LOGGER.error("Encountered an error while fetching product, the product is null for the given Colorway");
            return;
        }
        if (!verifyExistenceOfProductionStatusColorway(product)) {
            return;
        }
        LOGGER.debug("Checking non blank attrs:" + allAttributes);
        final boolean emptyValueDetected = allAttributes.stream().anyMatch(attr -> {
            final Object deltaChange = SparcIntegrationUtil.getValueFrom(deltaChangesSku, attr);
            LOGGER.debug("Checking non blanks, attr:" + attr + ", delta-change(is null):" + deltaChange == null + ", value:" + deltaChange);
            return deltaChange == null || deltaChange.toString().trim().isEmpty();
        });
        if (emptyValueDetected) {
            throw new LCSException("The attributes in the given list:" + SparcIntegrationUtil.convertToDisplayNames(allAttributes, existingSku) + " cannot be null/empty when the Colorway status is in Production");
        }
    }

    public void setFlag(final LCSSKU sku, final SparcPropertyDefinitions.Hierarchy hierarchy) {
        final LCSProduct product = getProduct(sku);
        LOGGER.info("Setting Sent To FC/Sent To CAP flags to NO for product:" + product.getName());
        if (product == null) {
            LOGGER.error("Cannot find product for given sku while setting the integration flags.");
            return;
        }
        final List<LCSSeason> luckySeasons = findLuckySeasons(product);
        if (luckySeasons == null || luckySeasons.isEmpty()) {
            LOGGER.error("Cannot find seasons for given sku while setting the integration flags.");
            return;
        }
        setFlag(sku, luckySeasons, hierarchy);
    }

    private void setFlag(final LCSSKU sku, final List<LCSSeason> seasons, final SparcPropertyDefinitions.Hierarchy hierarchy) {
        try {

            if (seasons == null || seasons.isEmpty()) {
                LOGGER.error("Can't set colorway status flag on the given colorway, as the season is not found");
                return;
            }
            seasons.stream().forEach(season -> {
                setFlag(sku, season, hierarchy);
            });

        } catch (Exception e) {
            LOGGER.error("Encountered an error while iterating the seasons of the given colorway, error:" + e);
        }
    }

    private void setFlag(final LCSSKU sku, final LCSSeason season, final SparcPropertyDefinitions.Hierarchy hierarchy) {
        try {
            final LCSSeasonProductLink spl = LCSSeasonQuery.findSeasonProductLink(sku, season);
            if (spl == null) {
                LOGGER.error("No season product link found while setting flag for FC/CAP");
                return;
            }
            
            if (SparcPropertyDefinitions.Hierarchy.STYLE == hierarchy) {
                LOGGER.info("Setting FC & CAP flag to NO");
                spl.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME, SparcIntegrationConstants.SENT_NO);
                spl.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME, SparcIntegrationConstants.SENT_NO);
            } else if (SparcPropertyDefinitions.Hierarchy.FC == hierarchy) {
                LOGGER.info("Setting FC flag to NO");
                spl.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME, SparcIntegrationConstants.SENT_NO);
            } else if (SparcPropertyDefinitions.Hierarchy.CAP == hierarchy) {
                LOGGER.info("Setting CAP flag to NO");
                spl.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME, SparcIntegrationConstants.SENT_NO);
            }
            
            LCSLogic.persist(spl, true);
            
        } catch (Exception e) {
            LOGGER.error("Encountered an error while setting the flags (Sent To FC/Sent To CAP) error:" + e);
        }

    }

    private boolean checkHierarchyAttrsDivergence(final LCSSKU existingSku, final LCSSKU deltaChangesSku, SparcPropertyDefinitions.Hierarchy hierarchy) {
        final Set<String> allAttrs      = SparcIntegrationConstants.COLORWAY_PAYLOAD_PROPERTY_DEFINITIONS.getAttrsFromHierarchy(hierarchy);
        final Set<String> colorwayAttrs = SparcIntegrationConstants.COLORWAY_PAYLOAD_COLORWAY_PROPERTY_DEFINITIONS.getAttrsFromHierarchy(hierarchy);
        allAttrs.addAll(colorwayAttrs);
        if (allAttrs.isEmpty()) {
            return false;
        }
        return allAttrs.stream().anyMatch(attr -> {
            final Object deltaChange = SparcIntegrationUtil.getValueFrom(deltaChangesSku, attr);
            final Object existing    = SparcIntegrationUtil.getValueFrom(existingSku, attr);
            LOGGER.debug("Checking (COLORWAY) divergence of attr:" + attr + ", delta change:" + deltaChange + ", existing:" + existing + ", Hierarchy:" + hierarchy);
            return SparcIntegrationUtil.divergenceCheck(deltaChange, existing) == true;
        });
    }

    private void enforceAttrsLock(final LCSSKU deltaChangesSku, final LCSSKU existingSku) throws LCSException {
        final Set<String> lockedAttrs           = SparcIntegrationConstants.COLORWAY_PAYLOAD_PROPERTY_DEFINITIONS.getLockedAttrs();
        final Set<String> colorwaySpecificAttrs = SparcIntegrationConstants.COLORWAY_PAYLOAD_COLORWAY_PROPERTY_DEFINITIONS.getLockedAttrs();
        lockedAttrs.addAll(colorwaySpecificAttrs);
        if (lockedAttrs.isEmpty()) {
            return;
        }
        final LCSProduct product = getProduct(existingSku);
        if (product == null) {
            LOGGER.error("Encountered an error while fetching product, the product is null for the given Colorway");
            return;
        }
        if (!verifyExistenceOfProductionStatusColorway(product)) {
            return;
        }
        final boolean changeDetected = lockedAttrs.stream().anyMatch(attr -> {
            final Object deltaChange = SparcIntegrationUtil.getValueFrom(deltaChangesSku, attr);
            final Object existing    = SparcIntegrationUtil.getValueFrom(existingSku, attr);
            LOGGER.debug("Checking (COLORWAY) divergence for locking of attr:" + attr + ", delta change:" + deltaChange + ", existing:" + existing);
            return SparcIntegrationUtil.divergenceCheck(deltaChange, existing) == true;
        });
        if (changeDetected) {
            throw new LCSException("Cannot modify attributes from list:" + SparcIntegrationUtil.convertToDisplayNames(lockedAttrs, existingSku) + ", when the colorway status is Production");
        }
    }

    private LCSProduct getProduct(final LCSSKU sku) {
        try {
            return sku.getProduct();
        } catch (Exception e) {
            //do nothing
        }
        return null;
    }

    private boolean verifyExistenceOfProductionStatusColorway(final LCSProduct prd) {
        final List<LCSSeason> luckySeasons = findLuckySeasons(prd);
        return luckySeasons.stream().anyMatch(season -> {
            try {
                final Collection<LCSPartMaster> skuMasters = LCSSeasonQuery.getSKUMastersForSeasonAndProduct(season, prd, false, false);
                if (skuMasters == null || skuMasters.isEmpty()) {
                    return false;
                }
                final List<LCSSKU> skus = SparcQueryUtil.<LCSSKU>findLatestIterationsOf(skuMasters);
                return verifyExistenceOfProductionStatusColorway(skus, season);
            } catch (Exception e) {
                LOGGER.error("Encountered an error while setting FC/CAP on the given product and season, error:" + e);
            }
            return false;
        });
    }

    private boolean verifyExistenceOfProductionStatusColorway(final List<LCSSKU> skus, final LCSSeason season) {
        return skus.stream().anyMatch(sku -> {
            try {
                final LCSSeasonProductLink spl = LCSSeasonQuery.findSeasonProductLink(sku, season);
                if (spl == null) {
                    return false;
                }
                final Object value = spl.getValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_COLORWAY_STATUS_INTERNAL_NAME);
                if (value != null && value.toString().equals(SparcIntegrationConstants.COLORWAY_TO_SEASON_COLORWAY_STATUS_PRODUCTION)) {
                    return true;
                }
            } catch (Exception e) {
                LOGGER.error("Encountered an error while fetching skulink from given sku and season, error:" + e);
            }
            return false;
        });
    }

    private List<LCSSeason> findLuckySeasons(final LCSProduct product) {
        try {
            final LCSSeasonQuery              lsq           = new LCSSeasonQuery();
            final Collection<LCSSeasonMaster> seasonMasters = lsq.findSeasons(product);
            if (seasonMasters == null || seasonMasters.isEmpty()) {
                return new ArrayList<>();
            }
            final List<LCSSeason> seasons = SparcQueryUtil.<LCSSeason>findLatestIterationsOf(seasonMasters);
            return seasons.stream()
                    .filter(season -> season.getFlexType().getFullName(true).startsWith(SparcIntegrationConstants.LUCKY_SEASON_FLEX_PATH))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Encountered an while searching for seasons for the given product, error:" + e);
        }
        return new ArrayList<>();
    }

}
