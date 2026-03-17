package com.sparc.wc.integration.services;

import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.VersionHelper;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SparcSourcingConfigWatchDogService class.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Task #10313: Reset Sent To FC and CAP flags upon change of HTS Code 1 and HTS Code Weight % attributes. Added method "checkHTSCode1Divergence" and updated method "check".
 * - Task #10705: Fix for Reebok Sent To S4 flag is not getting reset.<br>
 * Added method "isReebokHierarchy", and updated method "check" where specific code for reebok was placed based on the existing code available on Jan release.<br>
 * - Task #10752: Prevent reset of Sent To FC flag whenever HTS Code 1 attribute is updated for a non-primary sourcing config. Updated method "checkHierarchyAttrsDivergence".<br>
 * 
 * @author Acnovate
 */
public class SparcSourcingConfigWatchDogService {

    private static final Logger                      LOGGER                 = LogR.getLogger(SparcSourcingConfigWatchDogService.class.getName());
    private              SparcProductWatchDogService productWatchDogService = new SparcProductWatchDogService();

    public void check(final LCSSourceToSeasonLink deltaChangesLink) throws WTPropertyVetoException, WTException, LuckyCustomPluginException {
        LOGGER.info("[SparcSourcingConfigWatchDogService] Triggered Sourcing To Season watchdog service..");
        final LCSSourceToSeasonLink existingLink = SparcQueryUtil.findObjectById("VR:com.lcs.wc.sourcing.LCSSourceToSeasonLink:" + deltaChangesLink.getBranchIdentifier());

        if(deltaChangesLink == null){
            return;
        }


        if (deltaChangesLink.getSourcingConfigMaster() == null) {
            throw new LuckyCustomPluginException("[SparcSourcingConfigWatchDogService] Expecting a valid sourcing config master on Source to Season link");
        }

        LCSSourcingConfig sourcingConfig = (LCSSourcingConfig) VersionHelper.latestIterationOf(deltaChangesLink.getSourcingConfigMaster());
        LCSProduct product = (LCSProduct)VersionHelper.latestIterationOf(sourcingConfig.getProductMaster());
        LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(deltaChangesLink.getSeasonMaster());
        Object preSSlink = VersionHelper.predecessorOf(deltaChangesLink);

        if (preSSlink == null)
            throw new LuckyCustomPluginException("No previous source to season link found for '" + deltaChangesLink + "' (" + sourcingConfig.getSourcingConfigName() +").");

        Set<String> changedAttrList = SparcIntegrationUtil.getChangedAttributes(deltaChangesLink, (LCSSourceToSeasonLink)preSSlink,
                SparcIntegrationConstants.SOURCING_TO_SEASON_CONFIG_HTS_CODE1_CHAIN_INTERNAL_NAMES);
        
        LOGGER.debug("[SparcSourcingConfigWatchDogService] inside Sourcing config season");


        if (isLuckyHierarchy(product, season)) {
            LOGGER.info("[SparcSourcingConfigWatchDogService] Found Lucky Hierarchy, checking divergence");
			if (changedAttrList.isEmpty()) {
				throw new LuckyCustomPluginException("No changes found for Source To Season Link '" + deltaChangesLink + "' (" + sourcingConfig.getSourcingConfigName() + ").");
			}
			
            final boolean htsCode1Divergence = checkHTSCode1Divergence((LCSSourceToSeasonLink) preSSlink, deltaChangesLink);
            if (htsCode1Divergence) {
                LOGGER.debug("[SparcSourcingConfigWatchDogService] HTS Divergence is detected for Code 1, setting Sent to FC and CAP flags to NO");
                //productWatchDogService.setFlag(product, season, SparcPropertyDefinitions.Hierarchy.STYLE);
				List<LCSSKUSeasonLink> skuSeasonLinkList = LuckyArticleRepository.getProductColorwaysSeasonLinks(product, season);        
				for (LCSSKUSeasonLink skuSeasonLink : skuSeasonLinkList) {
					 LuckyArticleRepository.resetIntegrationControlFlags(skuSeasonLink);
				}
            }

        } else if (isReebokHierarchy(product, season)) {
            LOGGER.info("[SparcSourcingConfigWatchDogService] Found Reebok Hierarchy, checking divergence");

            final boolean htsDivergence = checkHTSDivergence(existingLink, deltaChangesLink);
            if (htsDivergence) {
                LOGGER.debug("[SparcSourcingConfigWatchDogService] HTS Divergence detected, setting Sent To S4 as No");
                productWatchDogService.setFlag(product, season, SparcPropertyDefinitions.Hierarchy.S4);
            }

        } else {
            LOGGER.debug("[SparcSourcingConfigWatchDogService] Skipping divergence check. LCSSourceToSeasonLink is not related to a Lucky or Reebok product.");
        }

        LOGGER.info("[SparcSourcingConfigWatchDogService] Divergence check completed");
    }

    private boolean isLuckyHierarchy(final LCSProduct product, LCSSeason season) {
        if (product == null || season == null) {
            LOGGER.error("[SparcSourcingConfigWatchDogService] Cannot check lucky hierarchy, requires valid product and season");
            return false;
        }
        final String prdFlexTypePath = product.getFlexType().getFullName(true);
        if (!prdFlexTypePath.startsWith(SparcIntegrationConstants.LUCKY_PRODUCT_FLEX_PATH)) {
            LOGGER.warn("[SparcSourcingConfigWatchDogService] The divergence check is configured for products in:" + SparcIntegrationConstants.LUCKY_PRODUCT_FLEX_PATH + ", but found:" + prdFlexTypePath);
            return false;
        }
        final String seasonFlexTypePath = season.getFlexType().getFullName(true);
        if (!seasonFlexTypePath.startsWith(SparcIntegrationConstants.LUCKY_SEASON_FLEX_PATH)) {
            LOGGER.warn("[SparcSourcingConfigWatchDogService] The divergence check is configured for seasons in:" + SparcIntegrationConstants.LUCKY_SEASON_FLEX_PATH + ", but found:" + prdFlexTypePath);
            return false;
        }
        return true;
    }
    
    private boolean isReebokHierarchy(final LCSProduct product, LCSSeason season) {
    	if (product == null || season == null) {
            LOGGER.error("[SparcSourcingConfigWatchDogService] Cannot check reebok hierarchy, requires valid product and season");
            return false;
        }
        final String prdFlexTypePath = product.getFlexType().getFullName(true);
        if (!prdFlexTypePath.startsWith(SparcIntegrationConstants.REEBOK_PRODUCT_APPAREL_FLEX_PATH) && !prdFlexTypePath.startsWith(SparcIntegrationConstants.REEBOK_PRODUCT_FOOTWEAR_FLEX_PATH)) {
            LOGGER.warn("[SparcSourcingConfigWatchDogService] The divergence check is configured for products in:" + SparcIntegrationConstants.REEBOK_PRODUCT_APPAREL_FLEX_PATH + " and " + SparcIntegrationConstants.REEBOK_PRODUCT_FOOTWEAR_FLEX_PATH + ", but found:" + prdFlexTypePath);
            return false;
        }
        final String seasonFlexTypePath = season.getFlexType().getFullName(true);
        if (!seasonFlexTypePath.startsWith(SparcIntegrationConstants.REEBOK_SEASON_FLEX_PATH)) {
            LOGGER.warn("[SparcSourcingConfigWatchDogService] The divergence check is configured for seasons in:" + SparcIntegrationConstants.REEBOK_SEASON_FLEX_PATH + ", but found:" + prdFlexTypePath);
            return false;
        }
        return true;
    }

    private boolean checkHTSCode1Divergence(final LCSSourceToSeasonLink existingLink, final LCSSourceToSeasonLink deltaChangesLink) {
        LOGGER.debug("[SparcSourcingConfigWatchDogService] Checking divergence of HTS Code1 Chain");
        if (!existingLink.isPrimarySTSL()) {
            LOGGER.info("[SparcSourcingConfigWatchDogService] Detected non-primary STSL, not checking divergence");
            return false;
        }
        return SparcIntegrationConstants.SOURCING_TO_SEASON_CONFIG_HTS_CODE1_CHAIN_INTERNAL_NAMES.stream().anyMatch(attr -> {
            final Object deltaChange = SparcIntegrationUtil.getValueFrom(deltaChangesLink, attr);
            final Object existing    = SparcIntegrationUtil.getValueFrom(existingLink, attr);
            LOGGER.debug("[SparcSourcingConfigWatchDogService] Checking (Source To Season Link) HTS divergence of attr:" + attr + ", delta change:" + deltaChange + ", existing:" + existing);
            return SparcIntegrationUtil.divergenceCheck(deltaChange, existing);
        });
    }
    
    private boolean checkHTSDivergence(final LCSSourceToSeasonLink existingLink, final LCSSourceToSeasonLink deltaChangesLink) {
        LOGGER.debug("[SparcSourcingConfigWatchDogService] Checking divergence of HTS Code Chain");
        if (!existingLink.isPrimarySTSL()) {
            LOGGER.info("[SparcSourcingConfigWatchDogService] Detected non-primary STSL, not checking divergence");
            return false;
        }
        return SparcIntegrationConstants.LUCKY_SOURCING_TO_SEASON_CONFIG_HTS_CODE_CHAIN_INTERNAL_NAMES.stream().anyMatch(attr -> {
            final Object deltaChange = SparcIntegrationUtil.getValueFrom(deltaChangesLink, attr);
            final Object existing    = SparcIntegrationUtil.getValueFrom(existingLink, attr);
            LOGGER.debug("[SparcSourcingConfigWatchDogService] Checking (Source To Season Link) HTS divergence of attr:" + attr + ", delta change:" + deltaChange + ", existing:" + existing);
            return SparcIntegrationUtil.divergenceCheck(deltaChange, existing);
        });
    }

    private boolean checkHierarchyAttrsDivergence(final LCSSourceToSeasonLink existingLink, final LCSSourceToSeasonLink deltaChangesLink, SparcPropertyDefinitions.Hierarchy hierarchy) {
        LOGGER.debug("[SparcSourcingConfigWatchDogService] Checking divergence of Source To Season Link attributes");
        final Set<String> attrs = SparcIntegrationConstants.COLORWAY_PAYLOAD_PROPERTY_DEFINITIONS.getAttrsFromHierarchy(hierarchy);
        final Set<String> sourceToSeasonAttrs = SparcIntegrationUtil.getAllAttributes(existingLink.getFlexType(), null);
        attrs.retainAll(sourceToSeasonAttrs);
        LOGGER.debug("[SparcSourcingConfigWatchDogService] Divergence list attributes:" + attrs);
        if (attrs.isEmpty()) {
            LOGGER.warn("[SparcSourcingConfigWatchDogService] No attributes were configured to check divergence");
            return false;
        }
        return attrs.stream().anyMatch(attr -> {
            final Object deltaChange = SparcIntegrationUtil.getValueFrom(deltaChangesLink, attr);
            final Object existing    = SparcIntegrationUtil.getValueFrom(existingLink, attr);
            LOGGER.debug("[SparcSourcingConfigWatchDogService] Checking (Source To Season Link) divergence of attr:" + attr + ", delta change:" + deltaChange + ", existing:" + existing + ", Hierarchy:" + hierarchy);
            return SparcIntegrationUtil.divergenceCheck(deltaChange, existing) == true;
        });
    }

    private LCSSeason getSeason(final LCSSourceToSeasonLink link) {
        final LCSSeasonMaster seasonMaster = link.getSeasonMaster();
        if (seasonMaster == null) {
            LOGGER.error("[SparcSourcingConfigWatchDogService] Expecting a valid season master on given source to season link");
            return null;
        }
        return SparcQueryUtil.findLatestIterationOf(seasonMaster);
    }

    private LCSProduct getProduct(final LCSSourceToSeasonLink link) {
        final LCSSourcingConfigMaster sourcingConfigMaster = link.getSourcingConfigMaster();
        final LCSSourcingConfig       sourcingConfig       = SparcQueryUtil.findLatestIterationOf(sourcingConfigMaster);
        if (sourcingConfig == null) {
            LOGGER.error("[SparcSourcingConfigWatchDogService] Couldn't find sourcing config from Sourcing Config master of given source to season link");
            return null;
        }
        final LCSPartMaster productMaster = sourcingConfig.getProductMaster();
        if (productMaster == null) {
            LOGGER.error("[SparcSourcingConfigWatchDogService] Expecting a valid Product Master on given Sourcing Config");
            return null;
        }
        return SparcQueryUtil.findLatestIterationOf(productMaster);
    }

}
