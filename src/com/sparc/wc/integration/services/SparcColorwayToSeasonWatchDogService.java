package com.sparc.wc.integration.services;

import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.integration.constants.SparcIntegrationConstants;
import com.sparc.wc.integration.lucky.domain.LuckyCustomPluginException;
import com.sparc.wc.integration.domain.SparcPropertyDefinitions;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.sparc.wc.integration.util.SparcQueryUtil;
import org.apache.logging.log4j.Logger;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import java.util.*;
import java.util.stream.Collectors;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.*;

/**
 * SparcColorwayToSeasonWatchDogService class.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Updated method "setFlag": Replaced use of "LCSSeasonSKULinkClientModel" by "LCSLogic" approach to skip execution of other SSP.<br> 
 * 
 * @author Acnovate
 */
public class SparcColorwayToSeasonWatchDogService {

    private static final Logger LOGGER = LogR.getLogger(SparcColorwayToSeasonWatchDogService.class.getName());

    public void check(final LCSSeasonProductLink deltaChangesLink) throws WTPropertyVetoException,WTException,LuckyCustomPluginException {
        LOGGER.info("Triggered colorway-season link divergence and non blank check");
        if(deltaChangesLink == null){
            throw new LuckyCustomPluginException("No Colorway season link exists");
        }

        if(!deltaChangesLink.isEffectLatest()){
            return;
        }
		LOGGER.debug("deltaChangesLink.getCarriedOverFrom()-----------------------"+deltaChangesLink.getCarriedOverFrom());
		LOGGER.debug("deltaChangesLink.getEffectSequence()-------------------------"+deltaChangesLink.getEffectSequence());
		
		if(deltaChangesLink.getCarriedOverFrom() != null &&  deltaChangesLink.getEffectSequence() == 0){			
			LOGGER.debug("Skip the plugins on the CarryOver as the colorway status is reset to Initial");
			return;
		}

        final Object colorwayStatus = SparcIntegrationUtil.getValueFrom(deltaChangesLink, SparcIntegrationConstants.COLORWAY_TO_SEASON_COLORWAY_STATUS_INTERNAL_NAME);
        if (colorwayStatus == null) {
            throw new LuckyCustomPluginException("Colorway status is blank for the colorway season");
        }

        LCSSeasonProductLink preSPlink = LCSSeasonQuery.getPriorSeasonProductLink(deltaChangesLink);

        if(preSPlink == null){
             throw new LuckyCustomPluginException("No previous colorway season link exists");
        }
        final Set<String> attrs  = SparcIntegrationConstants.LUCKY_COLORWAY_SEASON_PLUGIN_PROPERTY_DEFINITIONS.getAttrsFromHierarchy();
		Set<String> changedAttrList = SparcIntegrationUtil.getChangedAttributes(deltaChangesLink,preSPlink,attrs);
		if (changedAttrList.isEmpty()) {
            throw new LuckyCustomPluginException("No Changes found for the Colorway Season link");
        }       
		checkNonBlank((String)colorwayStatus,(LCSSKUSeasonLink)deltaChangesLink);
        resetIntegrationControlFlags((LCSSKUSeasonLink)deltaChangesLink);
    }

    public void checkNonBlank(final String colorwayStatus, final LCSSKUSeasonLink colorwaySeasonLink) throws LCSException,WTException {
        if (colorwayStatus.isEmpty() || !colorwayStatus.equals(SparcIntegrationConstants.COLORWAY_TO_SEASON_COLORWAY_STATUS_PRODUCTION)) {
            return;
        }
        LCSSeason objSeason = (LCSSeason) VersionHelper.latestIterationOf(colorwaySeasonLink.getSeasonMaster());
        LCSSKU objColorway = (LCSSKU)VersionHelper.latestIterationOf(colorwaySeasonLink.getOwner().getMaster());
        LCSProduct objProduct = LCSProductQuery.getProductVersion("" + colorwaySeasonLink.getProductMasterId(), "A");
		objColorway =(LCSSKU)VersionHelper.getVersion(objColorway,"A");
		LOGGER.debug("objColorway--------------------------"+objColorway.getVersionIdentifier().getValue());
		LOGGER.debug("objColorway--------------------------------------"+objColorway);
		LOGGER.debug("Lucky Colorway Season Attributes--------------------------"+SparcIntegrationConstants.LUCKY_COLORWAY_SEASON_PLUGIN_PROPERTY_DEFINITIONS.getNonBlankAttrs());
		checkNonBlankValues(colorwaySeasonLink, SparcIntegrationConstants.LUCKY_COLORWAY_SEASON_PLUGIN_PROPERTY_DEFINITIONS.getNonBlankAttrs());
		LOGGER.debug("Lucky Product Attributes--------------------------"+SparcIntegrationConstants.LUCKY_PRODUCT_PLUGIN_PROPERTY_DEFINITIONS.getNonBlankAttrs());		
		checkNonBlankValues(objProduct,SparcIntegrationConstants.LUCKY_PRODUCT_PLUGIN_PROPERTY_DEFINITIONS.getNonBlankAttrs());
		LOGGER.debug("Lucky Colorway Attributes--------------------------"+SparcIntegrationConstants.LUCKY_COLORWAY_PLUGIN_PROPERTY_DEFINITIONS.getNonBlankAttrs());			
        checkNonBlankValues(objColorway,SparcIntegrationConstants.LUCKY_COLORWAY_PLUGIN_PROPERTY_DEFINITIONS.getNonBlankAttrs());
    }

    public static void resetIntegrationControlFlags(LCSSKUSeasonLink flexColorwaySeasonLink) throws WTPropertyVetoException, WTException {
        if (SENT_YES.equalsIgnoreCase((String)flexColorwaySeasonLink.getValue(COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME))) {
            flexColorwaySeasonLink.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME, SparcIntegrationConstants.SENT_NO);

        }
        if (SENT_YES.equalsIgnoreCase((String)flexColorwaySeasonLink.getValue(COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME))) {
            flexColorwaySeasonLink.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME, SparcIntegrationConstants.SENT_NO);
        }
    }


    /**
     *
     * @param deltaChangesPrd
     * @param allAttributes
     * @throws LCSException
     */
    private void checkNonBlankValues(final FlexTyped deltaChangesPrd, Set<String> allAttributes) throws LCSException,WTException {
        if (allAttributes.isEmpty()) {
            return;
        }
          Set<String> blankAtt= new HashSet<>() ;

         Iterator<String> listItr    = allAttributes.iterator();
            while(listItr.hasNext()) {
                String attrObj = listItr.next();
                final Object deltaChange = SparcIntegrationUtil.getValueFrom(deltaChangesPrd, attrObj);
                if (deltaChange instanceof Boolean && (deltaChange == null || !(Boolean) deltaChange)) {
                    blankAtt.add(attrObj);
                }
                else if (deltaChange == null || deltaChange.toString().trim().isEmpty()) {
                    blankAtt.add(attrObj);
                }

            }

        
        if (!blankAtt.isEmpty()) {
            throw new LCSException("The attributes in the given list:" + SparcIntegrationUtil.convertToDisplayNames(blankAtt, deltaChangesPrd) + " cannot be null/empty when the Colorway status is in Production");
        }
    }


    /**
     *
     * @param objSKUSeasonLink
     * @param changedAttrs
     * @throws LCSException
     */
    private void enforceAttrsLock(LCSSKUSeasonLink objSKUSeasonLink,Set<String> changedAttrs) throws LCSException {
        Set<String> lockedAttrs = SparcIntegrationConstants.LUCKY_COLORWAY_SEASON_PLUGIN_PROPERTY_DEFINITIONS.getLockedAttrs();
        StringBuilder lockAttributes = new StringBuilder();
        changedAttrs.retainAll(lockedAttrs);
        for(String strAttr: changedAttrs){
            if (lockAttributes.length() > 0) {
                lockAttributes.append(", ");
            }
            lockAttributes.append(SparcIntegrationUtil.convertToDisplayName(strAttr,objSKUSeasonLink));
        }
        if (lockAttributes.length() > 0) {
            throw new LCSException("Requested action is unable to complete.\n\r"
                    + "The following list of attributes are locked and cannot be modified: " + lockedAttrs.toString() + ".");
        }
    }

    private LCSSKU getColorway(final LCSSKUSeasonLink link) {
        final long skuARevId = (long) link.getSkuARevId();
        return SparcQueryUtil.findObjectById("VR:com.lcs.wc.product.LCSSKU:" + skuARevId);
    }

    private List<FlexType> convertToFlexTypes(final List<FlexTyped> sources) {
        return sources.stream()
                .map(flexTyped -> {
                    try {
                        return flexTyped.getFlexType();
                    } catch (Exception e) {
                        LOGGER.error("Encountered an error while converting flexTyped to flexType, error:" + e.getMessage());
                        return null;
                    }
                })
                .filter(ft -> ft != null)
                .collect(Collectors.toList());
    }

    private void addSizeRangeFlexTypeIfNull(final List<FlexType> flexTypes) {
        final boolean sizeRangeExists = flexTypes.stream().anyMatch(ft -> ft.getFullName(true).equals(SparcIntegrationConstants.SIZE_RANGE_BUSINESS_OBJECT_FLEX_PATH));
        if (!sizeRangeExists) {
            try {
                final FlexType sizeRangeFlexType = FlexTypeCache.getFlexTypeFromPath(SparcIntegrationConstants.SIZE_RANGE_BUSINESS_OBJECT_FLEX_PATH);
                if (sizeRangeFlexType != null) {
                    flexTypes.add(sizeRangeFlexType);
                }
            } catch (Exception e) {
                LOGGER.error("Encountered an error while fetching flexType, error:" + e.getMessage());
            }
        }
    }

    private LCSSKUSeasonLink findCurrentVersion(final LCSSKUSeasonLink link) {
        return null;//TODO add logic to get current version of link
    }

    private boolean checkHierarchyAttrsDivergence(final LCSSKUSeasonLink existingSku, final LCSSKUSeasonLink deltaChangesSku, SparcPropertyDefinitions.Hierarchy hierarchy) {
        final Set<String> allAttrs = SparcIntegrationConstants.LUCKY_COLORWAY_SEASON_PLUGIN_PROPERTY_DEFINITIONS.getAttrsFromHierarchy();
        if (allAttrs.isEmpty()) {
            return false;
        }
        return allAttrs.stream().anyMatch(attr -> {
            final Object deltaChange = SparcIntegrationUtil.getValueFrom(deltaChangesSku, attr);
            final Object existing    = SparcIntegrationUtil.getValueFrom(existingSku, attr);
            LOGGER.debug("Checking (Colorway-Season Link) divergence of attr:" + attr + ", delta change:" + deltaChange + ", existing:" + existing + ", Hierarchy:" + hierarchy);
            return SparcIntegrationUtil.divergenceCheck(deltaChange, existing) == true;
        });
    }

    private void setFlag(final LCSSKUSeasonLink link, final SparcPropertyDefinitions.Hierarchy hierarchy) {
        try {
            if (link == null) {
                LOGGER.error("No season product link found while setting flag for FC/CAP");
                return;
            }
            
            if (SparcPropertyDefinitions.Hierarchy.STYLE == hierarchy) {
                LOGGER.info("Setting FC & CAP flag to NO");
                link.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME, SparcIntegrationConstants.SENT_NO);
                link.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME, SparcIntegrationConstants.SENT_NO);
            } else if (SparcPropertyDefinitions.Hierarchy.FC == hierarchy) {
                LOGGER.info("Setting FC flag to NO");
                link.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME, SparcIntegrationConstants.SENT_NO);
            } else if (SparcPropertyDefinitions.Hierarchy.CAP == hierarchy) {
                LOGGER.info("Setting CAP flag to NO");
                link.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME, SparcIntegrationConstants.SENT_NO);
            }
            
            LCSLogic.persist(link, true);
            
        } catch (Exception e) {
            LOGGER.error("Encountered an error while setting the flags (Sent To FC/Sent To CAP) error:" + e, e);
        }

    }

    private void checkNonEmptyValue(final Object value, final String attr) throws LCSException {
        if (value == null || value.toString().trim().isEmpty()) {
            throw new LCSException("The attribute '" + attr + "' cannot be null/empty when the colorway status is changed to Production");
        }
    }

    private List<FlexTyped> getSources(final LCSSKUSeasonLink link) {
        final long            skuARevId     = (long) link.getSkuARevId();
        final LCSSKU          sku           = (LCSSKU) SparcQueryUtil.findObjectById("VR:com.lcs.wc.product.LCSSKU:" + skuARevId);
        final long            seasonRevId   = (long) link.getSeasonRevId();
        final LCSSeason       season        = (LCSSeason) SparcQueryUtil.findObjectById("VR:com.lcs.wc.season.LCSSeason:" + seasonRevId);
        final long            productARevId = (long) link.getProductARevId();
        final LCSProduct      productARev   = (LCSProduct) SparcQueryUtil.findObjectById("VR:com.lcs.wc.product.LCSProduct:" + productARevId);
        final List<FlexTyped> sources       = new ArrayList<>();
        sources.add(link);
        sources.add(sku);
        sources.add(season);
        sources.add(productARev);
        SparcIntegrationConstants.PRODUCT_OBJECT_REF_INTERNAL_NAMES.stream().forEach(objRef -> {
            try {
                final Object refValue = productARev.getValue(objRef);
                if (refValue != null) {
                    sources.add((FlexTyped) refValue);
                }
            } catch (Exception e) {
                //do nothing
            }
        });
        return sources;
    }

}
