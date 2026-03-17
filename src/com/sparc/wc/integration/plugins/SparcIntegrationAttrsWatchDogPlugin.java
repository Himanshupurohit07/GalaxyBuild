package com.sparc.wc.integration.plugins;

import com.lcs.wc.color.LCSColor;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.*;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.integration.aero.domain.AeroCustomPluginException;
import com.sparc.wc.integration.aero.domain.AeroPayloadAttribute;
import com.sparc.wc.integration.aero.repository.AeroArticleRepository;
import com.sparc.wc.integration.aero.util.AeroIntegrationPluginUtil;
import com.sparc.wc.integration.constants.SparcIntegrationConstants;
import com.sparc.wc.integration.lucky.domain.LuckyCustomPluginException;
import com.sparc.wc.integration.services.SparcBusinessObjectWatchDogService;
import com.sparc.wc.integration.services.SparcColorWatchDogService;
import com.sparc.wc.integration.services.SparcColorwayToSeasonWatchDogService;
import com.sparc.wc.integration.services.SparcColorwayWatchDogService;
import com.sparc.wc.integration.services.SparcProductWatchDogService;
import com.sparc.wc.integration.services.SparcSourcingConfigWatchDogService;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.sparc.wc.integration.lucky.repository.LuckyArticleRepository;
import com.lcs.wc.foundation.LCSLogic;
import org.apache.logging.log4j.Logger;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.method.MethodContext;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_NUMBER_ATTR;

public class SparcIntegrationAttrsWatchDogPlugin {

    private static final Logger LOGGER = LogR.getLogger(SparcIntegrationAttrsWatchDogPlugin.class.getName());

    public static void divergenceCheck(final WTObject wtObject) throws LCSException {
        LOGGER.info("Triggered watch dog for divergence check");
        LOGGER.info("divergenceCheck==============STARTED Lucky Article Plugin @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " =========");
		
		Boolean isInterfaceAdminCheck = false;
        isInterfaceAdminCheck = isInterfaceAdmin();
        try {
            if (wtObject instanceof LCSProduct) {
			    (new SparcProductWatchDogService()).check((LCSProduct) wtObject);
            }
			else{
			if(!isInterfaceAdminCheck){
			if (wtObject instanceof LCSSKU) {
                (new SparcColorwayWatchDogService()).check((LCSSKU) wtObject);
            } else if (wtObject instanceof LCSColor) {
                (new SparcColorWatchDogService()).check((LCSColor) wtObject);
            } else if (wtObject instanceof LCSLifecycleManaged) {
                (new SparcBusinessObjectWatchDogService()).check((LCSLifecycleManaged) wtObject);
            } else if (wtObject instanceof LCSSourceToSeasonLink) {
                (new SparcSourcingConfigWatchDogService()).check((LCSSourceToSeasonLink) wtObject);
            }
			}
			}
        }catch(LuckyCustomPluginException luckyEx){
            LOGGER.warn("[check] Skip/Exit condition found for Lucky Article Plugin validation: " + luckyEx.getMessage());
        }catch(LCSException lcsEx){
            LOGGER.warn("[check] Lucky Article Plugin validation alert: " + lcsEx.getMessage());
			throw lcsEx;
        }catch(Exception ex){
            LOGGER.error("[check] The Lucky Article Plugin failed to process the flex object.", ex);
        }
        LOGGER.info("divergenceCheck==============End Lucky Article Plugin @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " =========");

    }

    public static void nonBlankCheck(final WTObject wtObject) throws LCSException {
        LOGGER.info("Triggered watch dog for non blank check");
        if (isInterfaceAdmin()) {
            LOGGER.info("Detected interface-admin, not checking Non-Blanks");
            return;
        }
        try {
            if (wtObject instanceof LCSSeasonProductLink) {
                if ("SKU".equals(((LCSSeasonProductLink) wtObject).getSeasonLinkType())) {
                    (new SparcColorwayToSeasonWatchDogService()).check((LCSSeasonProductLink) wtObject);
                } else {
                    validateProductSeasonLink((LCSSeasonProductLink)wtObject);
                }
            }
        }catch(LuckyCustomPluginException luckyEx){
            LOGGER.warn("[check] Skip/Exit condition found for Aero Article Plugin validation: " + luckyEx.getMessage());
        }catch(LCSException ex){
            LOGGER.warn("[Check] Lucky Article Plugin validation alert: " + ex.getMessage());
            throw ex;
        }catch(Exception e){
            e.printStackTrace();
            LOGGER.error("[Check] The Lucky Article Plugin failed to process the flex object.", e);
        }
    }

    private static boolean isInterfaceAdmin() {
        try {
            return MethodContext.getContext().getUserName().equals(SparcIntegrationConstants.INTERFACE_ADMIN_USER_NAME);
        } catch (Exception e) {
            LOGGER.error("Encountered an error while fetching current user, error:" + e.getMessage());
        }
        return false;
    }

    private static void validateProductSeasonLink(LCSSeasonProductLink splink) throws WTPropertyVetoException, WTException, LuckyCustomPluginException {

        LOGGER.debug("[validateProductSeasonLink] Product Season Link to check is: " + splink);

        if (splink == null) {
            return;
        }

        if (!splink.isEffectLatest()) {
            throw new LuckyCustomPluginException("Product Season Link is not the latest in effect.");
        }
		
		LOGGER.debug("SparcIntegrationAttrsWatchDogPlugin splink.getCarriedOverFrom()-----------------------"+splink.getCarriedOverFrom());
		LOGGER.debug("SparcIntegrationAttrsWatchDogPlugin splink.getEffectSequence()-------------------------"+splink.getEffectSequence());
		
		
		if(splink.getCarriedOverFrom() != null &&  splink.getEffectSequence() == 0){			
			LOGGER.debug("SparcIntegrationAttrsWatchDogPlugin Skip the plugins on the CarryOver");
			return;
		}


        LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(splink.getSeasonMaster());   
        LCSProduct product = LCSProductQuery.getProductVersion("" + splink.getProductMasterId(), "A");
        Long productNumber = (Long)product.getValue(AERO_PRODUCT_NUMBER_ATTR);
		
        LOGGER.debug("[validateProductSeasonLink] Product Season Link product # is: " + productNumber);

        LCSSeasonProductLink preSPlink = LCSSeasonQuery.getPriorSeasonProductLink(splink);

        if (preSPlink == null) {
            throw new LuckyCustomPluginException("No previous version found for product season link " + splink + ".");
        }
        
		Set<String> changedAttrList =SparcIntegrationUtil.getChangedAttributes(splink,preSPlink,SparcIntegrationConstants.LUCKY_PRODUCT_SEASON_PLUGIN_PROPERTY_DEFINITIONS.getAttrsFromHierarchy());
        
        if (changedAttrList.isEmpty()) {
            throw new LuckyCustomPluginException("No changes found for product season link " + splink + ".");
        }
        LCSProductSeasonLink productSeasonLink = (LCSProductSeasonLink)splink;
        List<LCSSKUSeasonLink> allSkuSeasonLinkList = LuckyArticleRepository.getProductColorwaysSeasonLinks(product, season);

	    if (allSkuSeasonLinkList.isEmpty()) {
            throw new LuckyCustomPluginException("Unable to find colorway season links for product #" + productNumber + ".");
        }
		
        LOGGER.debug("[validateProductSeasonLink] Found " + allSkuSeasonLinkList.size() + " colorway-season links for product #" + productNumber + ".");

        for (LCSSKUSeasonLink skuSeasonLink : allSkuSeasonLinkList) {
            LuckyArticleRepository.resetIntegrationControlFlags(skuSeasonLink);
        }
        LOGGER.debug("[validateProductSeasonLink] Product Season Link check is completed for product #" + productNumber + ".");
    }
	
	public static void setLuckyFCColorID(final WTObject wtObject) throws LCSException {
        LOGGER.info("Set the value for Lucky FC Color ID attr at Colorway object");
        LOGGER.debug("setLuckyFCColorID==============STARTED Lucky setLuckyFCColorID Plugin @ "+wtObject);
		if (wtObject instanceof LCSSKU) {
			LCSSKU sku = (LCSSKU)wtObject; 
			LOGGER.debug("setLuckyFCColorID============== sku "+sku);
			
			if(null != sku){
			 try {
					LCSColor clrObj = (LCSColor)LCSQuery.findObjectById("OR:"+sku.getValue("color"));
					LOGGER.debug("Colorway associated sclCLRCDE ============== sclCLRCDE value "+clrObj.getValue("sclCLRCDE"));
					sku.setValue​("scFCColorID",clrObj.getValue("sclCLRCDE"));
			 }catch(Exception e) {
				 LOGGER.debug("setLuckyFCColorID =====" + e.getMessage());
				e.printStackTrace();
			 }
			}
		}
	}
}
