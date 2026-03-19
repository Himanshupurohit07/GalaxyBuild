package com.sparc.wc.integration.aero.plugins;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_ARTICLE_LOGGER_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SENT_YES_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PLM_COLORWAY_STATUS_DROPPED_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PLM_COLORWAY_LIFECYCLE_DROPPED_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_SEASON_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_VALID_COLORWAY_LIFECYCLE_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_COST_SHEET_TYPE_PRODUCT;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_SEASON_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_LIFECYCLE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_NAME_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_S4_SHARED_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PLM_COLORWAY_STATUS_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_SEASON_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_NAME_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_CAP_SHARED_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_CAP_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.INTERFACE_ADMIN_USER_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_LIFECYCLE_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PLM_COLORWAY_STATUS_INITIAL_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.COLORWAY_TO_SEASON_COLORWAY_STATUS_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PLM_COLORWAY_STATUS_DEVELOPMENT_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME;

import wt.vc.VersionIdentifier;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.lcs.wc.foundation.LCSLogic;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSCostSheetQuery;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.VersionHelper;
import com.lcs.wc.foundation.LCSQuery;
import com.sparc.wc.integration.aero.domain.AeroCustomPluginException;
import com.sparc.wc.integration.aero.domain.AeroPayloadAttribute;
import com.sparc.wc.integration.aero.domain.AeroPayloadAttributeDefinitions;
import com.sparc.wc.integration.aero.repository.AeroArticleRepository;
import com.sparc.wc.integration.aero.repository.AeroCostingRepository;
import com.sparc.wc.integration.aero.util.AeroIntegrationPluginUtil;
import com.sparc.wc.integration.aero.util.AeroPayloadAttributeUtil;

import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.method.MethodContext;

/**
 * Flex Plugin for use for Aero Article Integration to:<br>
 * a. Update (reset) article/colorway integration flags based on integration attributes changes made by users.<br>
 * b. Detect and prevent updates to flex objects if not allowed to. i.e. Check for no blank & locked attributes.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Renamed method isColorwayLifecycleSet to isColorwayLifecycleValid in order to align to logic change.<br>
 * - Added further non-blank validations on product and colorway when processing saves for colorway-season link.<br>
 * - Added check to exclude processing objects being updated from the integration api.<br>
 * - Task #10143 (Hypercare): Added logic to detect updates for product-season eligible attributes.
 * Removed method "validateSeasonProductLink" and replaced it with respective methods: "validateColorwaySeasonLink" and "validateProductSeasonLink".<br>
 * - Task #10387 (Hypercare): Added logic to reset integration flags on cost sheets whenever colorways integration flags get reset. 
 * This includes the addition of the following method: "resetCostSheetIntegrationFlags".<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9645 Aero - Article Interface"
 */
public class AeroArticleIntegrationPlugin {
	
	private static final Logger LOGGER = LogR.getLogger(AERO_ARTICLE_LOGGER_NAME);
	
	private static List<AeroPayloadAttribute> SOURCE_TO_SEASON_ATTR_LIST = AeroPayloadAttributeDefinitions.
			getArticlePluginAttributesDefinitionsForFlexType(AERO_SOURCING_SEASON_ATTR_GROUP_KEY);
	
	private static List<AeroPayloadAttribute> PRODUCT_ATTR_LIST = AeroPayloadAttributeDefinitions.
			getArticlePluginAttributesDefinitionsForFlexType(AERO_PRODUCT_ATTR_GROUP_KEY);
	
	private static List<AeroPayloadAttribute> COLORWAY_ATTR_LIST = AeroPayloadAttributeDefinitions.
			getArticlePluginAttributesDefinitionsForFlexType(AERO_COLORWAY_ATTR_GROUP_KEY);
	
	private static List<AeroPayloadAttribute> COLORWAY_SEASON_ATTR_LIST = AeroPayloadAttributeDefinitions.
			getArticlePluginAttributesDefinitionsForFlexType(AERO_COLORWAY_SEASON_ATTR_GROUP_KEY);
	
	private static List<AeroPayloadAttribute> PRODUCT_SEASON_ATTR_LIST = AeroPayloadAttributeDefinitions.
			getArticlePluginAttributesDefinitionsForFlexType(AERO_PRODUCT_SEASON_ATTR_GROUP_KEY);
	
	private static List<AeroPayloadAttribute> COLORWAY_SEASON_CAP_ATTR_LIST = AeroPayloadAttributeDefinitions.
			getArticlePluginAttributesDefinitionsForFlexType(AERO_COLORWAY_CAP_ATTR_GROUP_KEY);
			
	private AeroArticleIntegrationPlugin() {
		
	}
	
	/**
	 * Main entry point for this plugin to trigger whenever an Aero Article related object is updated.<br> .
	 * @param obj The Flex object updated.
	 * @throws WTException If an error occurs while validating changed attributes from PLM or 
	 * while attempting to update the integration flag at the colorway-season link. 
	 */
	public static void validateChanges(WTObject obj) throws LCSException {
		
		LOGGER.info("[validateChanges] ==============STARTED Aero Article Plugin @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " =========");
        LOGGER.debug("[validateChanges] WTObject type: " + obj.getDisplayIdentity());
		
        try {
        	
        	if (obj instanceof LCSSourceToSeasonLink) {
            	validateSourceToSeasonLink((LCSSourceToSeasonLink)obj);
            } else if (obj instanceof LCSProduct) {
				LCSProduct prodObj = (LCSProduct)obj;
				VersionIdentifier productVersion = prodObj.getVersionIdentifier();
				System.out.println("productVersion>>>> "+productVersion);
				if(!(productVersion.getValue().equals("A"))){
					long productARevId = (long)prodObj.getProductARevId();
					System.out.println("productARevId>>>> "+productARevId);
					prodObj = (LCSProduct)LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSProduct:"+productARevId);
				}

				validateProduct(prodObj);
		
            } else if (obj instanceof LCSSKU) {
            	validateColorway((LCSSKU)obj);
            } else if (obj instanceof LCSSeasonProductLink) {
            	if ("SKU".equals(((LCSSeasonProductLink)obj).getSeasonLinkType())) {
            		validateColorwaySeasonLink((LCSSeasonProductLink)obj);
            	} else {
            		validateProductSeasonLink((LCSSeasonProductLink)obj);
            	}
            } else {
            	LOGGER.debug("[validateChanges] Object type " + obj.getDisplayIdentity() + " ignored.");
            }
        	
        } catch(AeroCustomPluginException aeroEx) {
        	LOGGER.warn("[validateChanges] Skip/Exit condition found for Aero Article Plugin validation: " + aeroEx.getMessage());
        } catch (LCSException lcsEx) {
        	LOGGER.warn("[validateChanges] Aero Article Plugin validation alert: " + lcsEx.getMessage());
        	throw lcsEx;
        } catch (Exception e) {
        	LOGGER.error("[validateChanges] The Aero Article Plugin failed to process the flex object.", e);
        }
        
        LOGGER.info("[validateChanges] ==============DONE Aero Article Plugin @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " =========");
	}
	
	/**
	 * Lookout for changes on the selected integration-eligible attribute values for product season and takes the following action(s):<br>
	 * - Verifies Non-blanks and locked attributes where required.<br>
     * - Reset of Article integration flags at the colorway's colorway season link.<br>
	 * @param splink The season product link instance (Product-Season).
	 * @throws WTPropertyVetoException If an error occurs while resetting the integration flags.
     * @throws WTException If an error occurs while accessing product season attributes.
     * @throws AeroCustomPluginException If a validation exit condition is found. i.e. If product type is not Aero.
	 */
	private static void validateProductSeasonLink(LCSSeasonProductLink splink) throws WTPropertyVetoException, WTException, AeroCustomPluginException {
		
		LOGGER.debug("[validateProductSeasonLink] Product Season Link to check is: " + splink);
		
		if (splink == null) {
    		return;
    	}
		
		if (!splink.isEffectLatest()) {
			throw new AeroCustomPluginException("Product Season Link is not the latest in effect.");
		}
		
		if (AeroIntegrationPluginUtil.isTaggedAsIntegrationApiOrigin(splink)) {
			throw new AeroCustomPluginException("Product Season Link " + splink + " has been updated from the Aero Integration API.");
		}
		
		LCSSeason season = (LCSSeason)VersionHelper.latestIterationOf(splink.getSeasonMaster());
    	
    	//Check if product season link is bound to a valid Aero season.
    	AeroIntegrationPluginUtil.validateAeroSeasonType(season);
		
    	LCSProduct product = LCSProductQuery.getProductVersion("" + splink.getProductMasterId(), "A");
		
		AeroIntegrationPluginUtil.validateAeroProductType(product);
		Long productNumber = (Long)product.getValue(AERO_PRODUCT_NUMBER_ATTR);
		LOGGER.debug("[validateProductSeasonLink] Product Season Link product # is: " + productNumber);
		
		LCSSeasonProductLink preSPlink = LCSSeasonQuery.getPriorSeasonProductLink(splink);
		
        if (preSPlink == null) {
        	throw new AeroCustomPluginException("No previous version found for product season link " + splink + ".");
        }
        
        List<AeroPayloadAttribute> changedAttrList = AeroIntegrationPluginUtil
        		.getChangedAttributes(splink, preSPlink, PRODUCT_SEASON_ATTR_LIST, LOGGER);
        
        if (changedAttrList.isEmpty()) {
        	throw new AeroCustomPluginException("No changes found for product season link " + splink + ".");
        }
		
        LCSProductSeasonLink productSeasonLink = (LCSProductSeasonLink)splink;
        List<LCSSKUSeasonLink> allSkuSeasonLinkList = AeroArticleRepository.getProductColorwaysSeasonLinks(product, season);
        
        if (allSkuSeasonLinkList.isEmpty()) {
        	throw new AeroCustomPluginException("Unable to find colorway season links for product #" + productNumber + ".");
        }
        
        LOGGER.debug("[validateProductSeasonLink] Found " + allSkuSeasonLinkList.size() + " colorway-season links for product #" + productNumber + ".");
        
        validateAttrNonBlanks(productSeasonLink, allSkuSeasonLinkList, PRODUCT_SEASON_ATTR_LIST);
		validateAttrLocked(productSeasonLink, allSkuSeasonLinkList, changedAttrList);
        
        for (LCSSKUSeasonLink skuSeasonLink : allSkuSeasonLinkList) {
        	AeroArticleRepository.resetIntegrationControlFlags(skuSeasonLink);
        }//end for loop.
		
		LOGGER.debug("[validateProductSeasonLink] Product Season Link check is completed for product #" + productNumber + ".");
		
	}
	
	/**
	 * Lookout for changes on the selected integration-eligible attribute values for colorway season and takes the following action(s):<br>
	 * - Confirm this is a Colorway Season Link and not Product Season Link.
	 * - Verifies Non-blanks and locked attributes where required.<br>
	 * - If PLM colorway status is DROPPED, then set colorway lifecycle to DROPPED.
     * - Reset of Article integration flags at the colorway's colorway season link.<br>
	 * @param splink The season product link instance (Colorway-Season).
	 * @throws WTPropertyVetoException If an error occurs while resetting the integration flags.
     * @throws WTException If an error occurs while accessing colorway season attributes.
     * @throws AeroCustomPluginException If a validation exit condition is found. i.e. If colorway type is not Aero.
	 */
	private static void validateColorwaySeasonLink(LCSSeasonProductLink splink) throws WTPropertyVetoException, WTException, AeroCustomPluginException {
		
		LOGGER.debug("[validateColorwaySeasonLink] Colorway Season Link to check is: " + splink);
		
		if (splink == null) {
    		return;
    	}
		
		if (!splink.isEffectLatest()) {
			throw new AeroCustomPluginException("Colorway Season Link is not the latest in effect.");
		}
		
		if (AeroIntegrationPluginUtil.isTaggedAsIntegrationApiOrigin(splink)) {
			throw new AeroCustomPluginException("Colorway Season Link " + splink + " has been updated from the Aero Integration API.");
		}
		
		LCSSeason season = (LCSSeason)VersionHelper.latestIterationOf(splink.getSeasonMaster());
    	
    	//Check if colorway season link is bound to a valid Aero season.
    	AeroIntegrationPluginUtil.validateAeroSeasonType(season);
		
		LCSSKU colorway = (LCSSKU)VersionHelper.latestIterationOf(splink.getOwner().getMaster());
		
		AeroIntegrationPluginUtil.validateAeroColorwayType(colorway);
		Long colorwayNumber = (Long)colorway.getValue(AERO_COLORWAY_NUMBER_ATTR);
		LOGGER.debug("[validateColorwaySeasonLink] Colorway Season Link colorway # is: " + colorwayNumber);
		
		LCSSeasonProductLink preSPlink = LCSSeasonQuery.getPriorSeasonProductLink(splink);
		
        if (preSPlink == null) {
        	throw new AeroCustomPluginException("No previous version found for colorway season link " + splink + ".");
        }


        if(!isInterfaceAdmin()  && AERO_SENT_YES_VALUE.equalsIgnoreCase((String)splink.getValue(AERO_COLORWAY_CAP_SHARED_ATTR)) &&
                AERO_SENT_YES_VALUE.equalsIgnoreCase((String)splink.getValue(COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME)) &&
                AERO_PLM_COLORWAY_STATUS_DEVELOPMENT_VALUE.equalsIgnoreCase((String)preSPlink.getValue(COLORWAY_TO_SEASON_COLORWAY_STATUS_INTERNAL_NAME)) &&
                AERO_PLM_COLORWAY_STATUS_INITIAL_VALUE.equalsIgnoreCase((String)splink.getValue(COLORWAY_TO_SEASON_COLORWAY_STATUS_INTERNAL_NAME)))
        {
			splink.setValue(COLORWAY_TO_SEASON_COLORWAY_STATUS_INTERNAL_NAME,(String)preSPlink.getValue(COLORWAY_TO_SEASON_COLORWAY_STATUS_INTERNAL_NAME));
            throw new LCSException("Status cannot be changed from Development to Initial once the data is shared with CAP and sent to CAP is YES");
        }
        //End of the

        //Start of the changes for the  IN-250225-0084
        if(!isInterfaceAdmin() && AERO_SENT_YES_VALUE.equalsIgnoreCase((String)splink.getValue(AERO_COLORWAY_CAP_SHARED_ATTR)) &&
                AERO_PLM_COLORWAY_STATUS_DROPPED_VALUE.equalsIgnoreCase((String)preSPlink.getValue(COLORWAY_TO_SEASON_COLORWAY_STATUS_INTERNAL_NAME)) &&
                (AERO_PLM_COLORWAY_STATUS_DEVELOPMENT_VALUE.equalsIgnoreCase((String)splink.getValue(COLORWAY_TO_SEASON_COLORWAY_STATUS_INTERNAL_NAME)) ||
                        AERO_PLM_COLORWAY_STATUS_INITIAL_VALUE.equalsIgnoreCase((String)splink.getValue(COLORWAY_TO_SEASON_COLORWAY_STATUS_INTERNAL_NAME))))
        {
            splink.setValue(COLORWAY_TO_SEASON_COLORWAY_STATUS_INTERNAL_NAME,(String)preSPlink.getValue(COLORWAY_TO_SEASON_COLORWAY_STATUS_INTERNAL_NAME));
            throw new LCSException("Colorway already shared with CAP so changing from dropped to Development/Initial not allowed");
        }
        //End of the changes for the  IN-250225-0084
        List<AeroPayloadAttribute> changedAttrList = AeroIntegrationPluginUtil
        		.getChangedAttributes(splink, preSPlink, COLORWAY_SEASON_ATTR_LIST, LOGGER);
        
        if (changedAttrList.isEmpty()) {
        	throw new AeroCustomPluginException("No changes found for colorway season link " + splink + ".");
        }
		
        LCSSKUSeasonLink colorwaySeasonLink = (LCSSKUSeasonLink)splink;
        LCSProduct product = LCSProductQuery.getProductVersion("" + splink.getProductMasterId(), "A");
        
        validateAttrNonBlanks(colorwaySeasonLink, COLORWAY_SEASON_ATTR_LIST);
        validateAttrNonBlanks(colorway, colorwaySeasonLink, COLORWAY_ATTR_LIST);
        validateAttrNonBlanks(product, colorwaySeasonLink, PRODUCT_ATTR_LIST);
		validateAttrLocked(colorwaySeasonLink, changedAttrList);
		
		if (isPLMColorwayStatusDropped(colorwaySeasonLink) && isArticleColorwaySentToS4(colorwaySeasonLink)) {
			LOGGER.debug("[validateColorwaySeasonLink] PLM colorway status is DROPPED for this colorway season link (" + splink + "), updating colorway lifecycle to DROPPED.");
			dropColorwayLifeCycle(colorwaySeasonLink);
		}
		
		boolean skipPersist= true;
		AeroArticleRepository.resetIntegrationControlFlags(colorwaySeasonLink, skipPersist);
		resetCostSheetIntegrationFlags(colorwaySeasonLink, product);
		
		LOGGER.debug("[validateColorwaySeasonLink] Colorway Season Link check is completed for colorway #" + colorwayNumber + ".");
		
	}
	
	/**
     * Lookout for changes on the selected integration-eligible attribute values for colorway (SKU) and takes the following action(s):<br>
     * - Verifies Non-blanks and locked attributes where required.<br>
     * - Reset of Article integration flags at the colorway's colorway season link.<br>
     * @param skuObj The colorway (SKU) instance.
     * @throws WTPropertyVetoException If an error occurs while resetting the integration flags.
     * @throws WTException If an error occurs while accessing colorway attributes.
     * @throws AeroCustomPluginException If a validation exit condition is found. i.e. If product Type is not Aero.
     */
    private static void validateColorway(LCSSKU skuObj) throws WTPropertyVetoException, WTException, AeroCustomPluginException {
    	
    	if (skuObj == null) {
    		return;
    	}
    	
    	Long colorwayNumber = (Long)skuObj.getValue(AERO_COLORWAY_NUMBER_ATTR);
    	LOGGER.debug("[validateColorway] Colorway # to check is: " + colorwayNumber + ".");
    	
    	if (AeroIntegrationPluginUtil.isTaggedAsIntegrationApiOrigin(skuObj)) {
    		throw new AeroCustomPluginException("Colorway # " + colorwayNumber + " has been updated from the Aero Integration API.");
		}
    	
    	if (skuObj.isPlaceholder()) {
    		throw new AeroCustomPluginException("Colorway #" + colorwayNumber + " is a placeholder.");
        }
    	
    	LCSSKU prevSkuObj = (LCSSKU) VersionHelper.predecessorOf(skuObj);
        
        if (prevSkuObj == null) {
        	throw new AeroCustomPluginException("No previous version found for colorway #" + colorwayNumber + ".");
        }
    	
        List<AeroPayloadAttribute> changedAttrList = AeroIntegrationPluginUtil
        		.getChangedAttributes(skuObj, prevSkuObj, COLORWAY_ATTR_LIST, LOGGER);
        
		List<AeroPayloadAttribute> changedCapAttrList = AeroIntegrationPluginUtil
				.getChangedAttributes(skuObj, prevSkuObj, COLORWAY_SEASON_CAP_ATTR_LIST, LOGGER);
				
        if (changedAttrList.isEmpty()) {
        	throw new AeroCustomPluginException("No changes found for colorway #" + colorwayNumber + ".");
        }
        
        List<LCSSKUSeasonLink> colorwaySeasonLinkList = AeroArticleRepository.getColorwaySeasonLink(colorwayNumber);
        
        if (colorwaySeasonLinkList == null || colorwaySeasonLinkList.isEmpty()) {
        	throw new AeroCustomPluginException("Unable to find colorway season links for colorway #" + colorwayNumber + ".");
        }
        
        validateAttrNonBlanks(skuObj, colorwaySeasonLinkList, COLORWAY_ATTR_LIST);
		if(!isInterfaceAdmin()) {
			validateAttrLockOnColorWayStatus(skuObj, colorwaySeasonLinkList, changedCapAttrList);
		}
		validateAttrLocked(skuObj, colorwaySeasonLinkList, changedAttrList);
		
        for (LCSSKUSeasonLink colorwaySeasonLink : colorwaySeasonLinkList) {
			AeroArticleRepository.resetIntegrationControlFlags(colorwaySeasonLink);
		}//end for loop
		
		LOGGER.debug("[validateColorway] Colorway check is completed for colorway #" + colorwayNumber + ".");
    }
	
	/**
     * Lookout for changes on selected integration-eligible attribute values for Product object and takes the following action(s):<br>
     * - Verifies Non-blanks and locked attributes where required.<br>
     * - Reset of Article integration flags at colorway season link for all the valid seasons the product is associated with.<br>
     * @param product The product instance.
     * @throws WTPropertyVetoException If an error occurs while resetting the integration flags.
     * @throws WTException If an error occurs while accessing product attributes.
     * @throws AeroCustomPluginException If a validation exit condition is found. i.e. If product Type is not Aero.
     */
    private static void validateProduct(LCSProduct product) throws WTPropertyVetoException, WTException, AeroCustomPluginException {
    	
    	if (product == null) {
    		return;
    	}
    	
    	Long productNumber = (Long)product.getValue(AERO_PRODUCT_NUMBER_ATTR);
    	LOGGER.debug("[validateProduct] Product # to check is: " + productNumber + " (" + product.getName() + ").");
    	
    	if (AeroIntegrationPluginUtil.isTaggedAsIntegrationApiOrigin(product)) {
    		throw new AeroCustomPluginException("Product # " + productNumber + " has been updated from the Aero Integration API.");
		}
    	
    	LCSProduct prevProduct = (LCSProduct)VersionHelper.predecessorOf(product);
        
        if (prevProduct == null) {
        	throw new AeroCustomPluginException("No previous product version found for product #" + productNumber + ".");
        }
    	
        List<AeroPayloadAttribute> changedAttrList = AeroIntegrationPluginUtil
        		.getChangedAttributes(product, prevProduct, PRODUCT_ATTR_LIST, LOGGER);
        
        if (changedAttrList.isEmpty()) {
        	throw new AeroCustomPluginException("No changes found for product #" + productNumber + ".");
        }
        
        List<LCSSKUSeasonLink> allSkuSeasonLinkList = AeroArticleRepository.getProductColorwaysSeasonLinks(productNumber);
        
        if (allSkuSeasonLinkList.isEmpty()) {
        	throw new AeroCustomPluginException("Unable to find colorway season links for product #" + productNumber + ".");
        }
        
        validateAttrNonBlanks(product, allSkuSeasonLinkList, PRODUCT_ATTR_LIST);
		List<AeroPayloadAttribute> lockedAttrList = AeroIntegrationPluginUtil
				.getChangedAttrList(product, prevProduct, PRODUCT_ATTR_LIST.stream().filter(a -> a.isLockAttrUpdate()).collect(Collectors.toList()), LOGGER);

		LOGGER.debug("ValidateProduct lockedAttrList---------------"+lockedAttrList);

		validateAttrLocked(product, allSkuSeasonLinkList, lockedAttrList);

		for (LCSSKUSeasonLink skuSeasonLink : allSkuSeasonLinkList) {
			LOGGER.debug("ValidateProduct skuSeasonLink-----plugin context---before------------"+skuSeasonLink.isEffectLatest());
			skuSeasonLink =(LCSSKUSeasonLink)LCSSeasonQuery.findSeasonProductLink(skuSeasonLink.getSkuMaster(),skuSeasonLink.getSeasonMaster());
			LOGGER.debug("ValidateProduct-----plugin context---after------------"+skuSeasonLink.isEffectLatest());
			AeroArticleRepository.resetIntegrationControlFlags(skuSeasonLink);
		}//end for loop.

		LOGGER.debug("[validateProduct] Product check is completed for product #" + productNumber + ".");
	}

	/**
     * Lookout for changes on selected integration-eligible attribute values for Source To Season Link object and takes the following action(s):<br>
     * - Checks the source to season link object is the primary (season) one.<br>
     * - Verifies Non-blanks and locked attributes where required.<br>
     * - Reset of Article integration flags at colorway season link within the season.<br>
     * @param sslink The Source To Season Link object that has been changed in the system.
     * @throws WTPropertyVetoException If an error occurs while resetting the integration flags.
     * @throws WTException If an error occurs while accessing source to season link attributes.
     * @throws AeroCustomPluginException If a validation exit condition is found. i.e. Source To Season link's product Type is not Aero.
     */
    private static void validateSourceToSeasonLink(LCSSourceToSeasonLink sslink) throws WTPropertyVetoException, WTException, AeroCustomPluginException {
    	
    	if (sslink == null) {
    		return;
    	}
    	
    	LOGGER.debug("[validateSourceToSeasonLink] Source To Season Link to check is: " + sslink);
    	
    	if (AeroIntegrationPluginUtil.isTaggedAsIntegrationApiOrigin(sslink)) {
    		throw new AeroCustomPluginException("Source To Season Link " + sslink + " has been updated from the Aero Integration API.");
		}
    	
        LCSSourcingConfig sourcingConfig = (LCSSourcingConfig) VersionHelper.latestIterationOf(sslink.getSourcingConfigMaster());
		LCSProduct product = (LCSProduct)VersionHelper.latestIterationOf(sourcingConfig.getProductMaster());
		
		//Check if source to season link is bound to an Aero product.
		AeroIntegrationPluginUtil.validateAeroProductType(product);
        
		LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(sslink.getSeasonMaster());
		
		//Check if cost sheet is bound to a valid Aero season.
    	AeroIntegrationPluginUtil.validateAeroSeasonType(season);
		
		//Check if primary (season).
		if (!sslink.isPrimarySTSL()) {
			throw new AeroCustomPluginException("Source to Season Link '" + sslink + "' (" + sourcingConfig.getSourcingConfigName() +") updated is not the primary (season) one.");
		}
		
        Object preSSlink = VersionHelper.predecessorOf(sslink);
        
        if (preSSlink == null) {
        	throw new AeroCustomPluginException("No previous source to season link found for '" + sslink + "' (" + sourcingConfig.getSourcingConfigName() +").");
        }
        
        List<AeroPayloadAttribute> changedAttrList = AeroIntegrationPluginUtil
        		.getChangedAttributes(sslink, (LCSSourceToSeasonLink)preSSlink, SOURCE_TO_SEASON_ATTR_LIST, LOGGER);
        
        if (changedAttrList.isEmpty()) {
        	throw new AeroCustomPluginException("No changes found for Source To Season Link '" + sslink + "' (" + sourcingConfig.getSourcingConfigName() + ").");
        }
        
        List<LCSSKUSeasonLink> skuSeasonLinkList = AeroArticleRepository.getProductColorwaysSeasonLinks(product, season);
        
		LOGGER.debug("[validateSourceToSeasonLink] Found '" + skuSeasonLinkList.size() + "' colorway season links for product #" + product.getNumber()
				+ " and season '" + sslink.getSeasonMaster().getName() + "'.");
		
		validateAttrNonBlanks(sslink, skuSeasonLinkList, SOURCE_TO_SEASON_ATTR_LIST);
		validateAttrLocked(sslink, skuSeasonLinkList, changedAttrList);
		
		for (LCSSKUSeasonLink skuSeasonLink : skuSeasonLinkList) {
			AeroArticleRepository.resetIntegrationControlFlags(skuSeasonLink);
		}//end for loop
    	
		LOGGER.debug("[validateSourceToSeasonLink] Source To Season Link check is completed.");
		
    }
    
    /**
     * Checks whether the colorway lifecycle attribute for the given colorway season link has a valid value set.
     * @param colorwaySeasonLink The season coloreay link to check.
     * @return true if the colorway season link has a value set, false otherwise.
     * @throws WTException If an error occurred while extracting the colorway lifecycle value from the colorway season link object. 
     */
	private static boolean isColorwayLifecycleValid(LCSSKUSeasonLink colorwaySeasonLink,Boolean isBlank) throws WTException {

		if (colorwaySeasonLink == null || AERO_VALID_COLORWAY_LIFECYCLE_VALUES.isEmpty()) {
			return false;
		}

		String colorwayLifecycle = (String)colorwaySeasonLink.getValue(AERO_COLORWAY_LIFECYCLE_ATTR);

		//No Blank validation check for the Sub Department,class,Size Category and Size Range when the lifecyle is dropped from
		//the integration perspective
		if(isInterfaceAdmin() && isBlank){
			System.out.println("inside the intefaceadmin");
			return (colorwayLifecycle != null && AERO_COLORWAY_LIFECYCLE_VALUES.contains(colorwayLifecycle));
		}else {
			System.out.println("inside the non interface admin");
			return (colorwayLifecycle != null && AERO_VALID_COLORWAY_LIFECYCLE_VALUES.contains(colorwayLifecycle));
		}

	}
    private static boolean isArticleColorwaySentToCAP(LCSSKUSeasonLink colorwaySeasonLink) throws WTException {
    	
    	if (colorwaySeasonLink == null) {
    		return false;
    	}
    	
    	return AERO_SENT_YES_VALUE.equalsIgnoreCase((String)colorwaySeasonLink.getValue(AERO_COLORWAY_CAP_SHARED_ATTR));
    }
    
    /**
     * Checks within the given colorway season link whether the colorway has been sent to S4 or not by inspecting the S4 Shared attribute.
     * @param colorwaySeasonLink The colorway season link to check.
     * @return true if S4 Shared status is yes (colorway has been sent to S4 previously), false otherwise.
     * @throws WTException If an error occurred while extracting the colorway S4 Shared value from the colorway season link object.
     */
    private static boolean isArticleColorwaySentToS4(LCSSKUSeasonLink colorwaySeasonLink) throws WTException {
    	
    	if (colorwaySeasonLink == null) {
    		return false;
    	}
    	
    	return AERO_SENT_YES_VALUE.equalsIgnoreCase((String)colorwaySeasonLink.getValue(AERO_COLORWAY_S4_SHARED_ATTR));
    }
    
    /**
     * Checks within the given colorway season link whether the PLM colorway status has been set to Dropped or not.
     * @param colorwaySeasonLink The colorway season link to check.
     * @return true if the PLM colorway status has been set to dropped, false otherwise.
     * @throws WTException If an error occurred while extracting the PLM colorway status value from the colorway season link object.
     */
    private static boolean isPLMColorwayStatusDropped(LCSSKUSeasonLink colorwaySeasonLink) throws WTException {
    	
    	if (colorwaySeasonLink == null) {
    		return false;
    	}
    	
    	return AERO_PLM_COLORWAY_STATUS_DROPPED_VALUE.equalsIgnoreCase((String)colorwaySeasonLink.getValue(AERO_PLM_COLORWAY_STATUS_ATTR));
    }
    
    /**
     * Updates the colorway lifecycle attribute within the given colorway season link to DROPPED.
     * @param colorwaySeasonLink The colorway season link to update.
     */
    private static void dropColorwayLifeCycle(LCSSKUSeasonLink colorwaySeasonLink) {
    	
    	if (colorwaySeasonLink == null) {
    		return;
    	}
    	
    	colorwaySeasonLink.setValue(AERO_COLORWAY_LIFECYCLE_ATTR, AERO_PLM_COLORWAY_LIFECYCLE_DROPPED_VALUE);
    }
    
    /**
     * Conditionally checks for non-blanks attribute values for the given flex typed object if the colorway lifecycle value within any of the given 
     * colorway season links is not empty, and if the list of attributes given contain any attributes that require non-blank check.
     * @param flexTyped The flex typed object to check for non-blank attributes.
     * @param skuSeasonLinkList The list of colorway season links to inspect the status of the colorway cycle attribute.
     * @param attrList The list of attributes to inspect. Only those attributes marked for non-blank will be checked.
     * @throws LCSException If a mandatory non-blank value is found to be blank. 
     * @throws WTException If an error occurred while extracting attribute values from the flex objects given.
     */
    private static void validateAttrNonBlanks(FlexTyped flexTyped, 
    		List<LCSSKUSeasonLink> skuSeasonLinkList,
    		List<AeroPayloadAttribute> attrList) throws LCSException, WTException {
    	
    	if (skuSeasonLinkList == null || skuSeasonLinkList.isEmpty() || attrList == null || attrList.isEmpty()) {
    		LOGGER.debug("[validateAttrNonBlanks] Skipping non blank validation check.");
    		return;
    	}
    	
    	boolean mustValidate = false;
    	//Check if non-blank validation should occur or not.
    	for (LCSSKUSeasonLink skuSL : skuSeasonLinkList) {
			mustValidate = isColorwayLifecycleValid(skuSL,true);
    		
    		if (mustValidate) {
    			break;
    		}
    	}//end for loop.
    	
    	if (mustValidate) {
    		LOGGER.debug("[validateAttrNonBlanks] No-blanks validation is required for this flex object.");
    		AeroPayloadAttributeUtil.validateNoBlanks(flexTyped, attrList);
    	}
    	
    }
    
    /**
     * Conditionally checks for non-blanks attribute values for the given flex typed object if the colorway lifecycle value within the given 
     * colorway season link is not empty, and if the list of attributes given contain any attributes that require non-blank check.
     * @param flexTyped The flex typed object to check for non-blank attributes.
     * @param skuSeasonLink The colorway season link to inspect the status of the colorway cycle attribute.
     * @param attrList The list of attributes to inspect. Only those attributes marked for non-blank will be checked.
     * @throws LCSException If a mandatory non-blank value is found to be blank. 
     * @throws WTException If an error occurred while extracting attribute values from the flex objects given.
     */
    private static void validateAttrNonBlanks(FlexTyped flexTyped, 
    		LCSSKUSeasonLink skuSeasonLink,
    		List<AeroPayloadAttribute> attrList) throws LCSException, WTException {
    	
    	if (flexTyped == null || skuSeasonLink == null || attrList == null || attrList.isEmpty()) {
    		LOGGER.debug("[validateAttrNonBlanks] Skipping non blank validation check.");
    		return;
    	}
    	
		if (isColorwayLifecycleValid(skuSeasonLink,true)) {
			LOGGER.debug("[validateAttrNonBlanks] No-blanks validation is required for this flex object.");
    		AeroPayloadAttributeUtil.validateNoBlanks(flexTyped, attrList);
    	}
    	
    }
    
    /**
     * Conditionally checks for non-blanks attribute values for the given Colorway Season Link object if the colorway lifecycle value within is not empty, 
     * and if the list of attributes given contain any attributes that require non-blank check.
     * @param skuSeasonLink The colorway season link to check for non-blank attributes and to inspect the status of the colorway cycle attribute.
     * @param attrList The list of attributes to inspect. Only those attributes marked for non-blank will be checked.
     * @throws LCSException If a mandatory non-blank value is found to be blank. 
     * @throws WTException If an error occurred while extracting attribute values from the flex objects given.
     */
    private static void validateAttrNonBlanks(LCSSKUSeasonLink skuSeasonLink,
    		List<AeroPayloadAttribute> attrList) throws LCSException, WTException {
    	
    	if (skuSeasonLink == null || attrList == null || attrList.isEmpty()) {
    		LOGGER.debug("[validateAttrNonBlanks] Skipping non blank validation check.");
    		return;
    	}
    	
		if (isColorwayLifecycleValid(skuSeasonLink,true)) {
			LOGGER.debug("[validateAttrNonBlanks] No-blanks validation is required for this flex object.");
    		AeroPayloadAttributeUtil.validateNoBlanks(skuSeasonLink, attrList);
    	}
    	
    }
    
    /**
     * Conditionally checks for update-locked attributes for the given Source To Season Link object if any of the colorways have been sent to S4,
     * and if the list of attributes given contain any attributes that require update-locked check.
     * @param flexTyped The flex typed object to check for locked attributes.
     * @param skuSeasonLinkList The list of colorway season links to inspect the status of the S4 Shared attribute status.
     * @param attrList The list of attributes to inspect. Only those attributes marked for lupdate locked will be checked.
     * @throws LCSException If a mandatory update locked attribute value is detected.
     * @throws WTException If an error occurred while extracting attribute values from the flex objects given.
     */
    private static void validateAttrLocked(FlexTyped flexTyped, 
    		List<LCSSKUSeasonLink> skuSeasonLinkList,
    		List<AeroPayloadAttribute> attrList) throws LCSException, WTException {
    	
    	if (skuSeasonLinkList == null || skuSeasonLinkList.isEmpty() || attrList == null || attrList.isEmpty()) {
    		LOGGER.debug("[validateAttrLocked] Skipping locked validation check.");
    		return;
    	}
    	
    	boolean mustValidate = false;
    	//Check if locked validation should occur or not.
    	for (LCSSKUSeasonLink skuSL : skuSeasonLinkList) {
    		mustValidate = isArticleColorwaySentToS4(skuSL);
    		
    		if (mustValidate) {
    			break;
    		}
    	}//end for loop.
    	
    	if (mustValidate) {
    		LOGGER.debug("[validateAttrLocked] Update Lock validation is required for this flex object.");
    		AeroPayloadAttributeUtil.validateLocked(flexTyped, attrList);
    	}
	}

	/**
	 *
	 * @param flexTyped
	 * @param skuSeasonLinkList
	 * @return
	 * @throws LCSException
	 * @throws WTException
	 */
	private static void validateAttrLockOnColorWayStatus(FlexTyped flexTyped,
														 List<LCSSKUSeasonLink> skuSeasonLinkList,List<AeroPayloadAttribute> attrList) throws LCSException, WTException {

		if (skuSeasonLinkList == null || skuSeasonLinkList.isEmpty() || attrList == null || attrList.isEmpty()) {
			LOGGER.debug("[validateAttrLocked] Skipping locked validation check.");
			return;
		}
		boolean mustValidate = false;

		//Check if locked validation should occur or not.
		for (LCSSKUSeasonLink skuSL : skuSeasonLinkList) {
			mustValidate = isArticleColorwaySentToCAP(skuSL);
			if(mustValidate){
				break;
			}
		}//end for loop.
		if (mustValidate) {
			LOGGER.debug("[validateAttrLocked] Update Lock validation is required for this flex object.");
			AeroPayloadAttributeUtil.validateCAPLocked(flexTyped, attrList);
		}

	}
    /**
     * Conditionally checks for update-locked attributes for the given Colorway Season Link object if its related colorway have been sent to S4,
     * and if the list of attributes given contain any attributes that require update-locked check.
     * @param skuSeasonLink The colorway season link to check for locked attributes and to inspect the status of the S4 Shared attribute status.
     * @param attrList The list of attributes to inspect. Only those attributes marked for lupdate locked will be checked.
     * @throws LCSException If a mandatory update locked attribute value is detected.
     * @throws WTException If an error occurred while extracting attribute values from the flex objects given.
     */
    private static void validateAttrLocked(LCSSKUSeasonLink skuSeasonLink,
    		List<AeroPayloadAttribute> attrList) throws LCSException, WTException {
    	
    	if (skuSeasonLink == null || attrList == null || attrList.isEmpty()) {
    		LOGGER.debug("[validateAttrLocked] Skipping locked validation check.");
    		return;
    	}
    	
    	if (isArticleColorwaySentToS4(skuSeasonLink)) {
    		LOGGER.debug("[validateAttrLocked] Update Lock validation is required for this flex object.");
    		AeroPayloadAttributeUtil.validateLocked(skuSeasonLink, attrList);
    	}
    	
    }
    
    /**
     * Extracts all cost sheets associated to the given colorway season link and product and resets their Aero integration control flags.
     * @param skuslink The colorway season link instance required to extract associated cost sheets.
     * @param product The product instance required to obtain sourcing configs required to extract associated cost sheets.
     * @throws WTPropertyVetoException If an error occurs while attempting to update an invalid attribute within the flex object.
     * @throws WTException If an error occurs while processing the flag update at PLM.
     */
private static void resetCostSheetIntegrationFlags(LCSSKUSeasonLink skuslink, LCSProduct product) throws WTPropertyVetoException, WTException {
    	
    	if (skuslink == null || product == null) {
    		return;
    	}
    	
    	boolean includeWhatIfCostSheet = false;
    	LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(skuslink.getSeasonMaster());
		LCSSKU sku = (LCSSKU)VersionHelper.latestIterationOf(skuslink.getSkuMaster());
		
		Long colorwayNumber = (Long)sku.getValue(AERO_COLORWAY_NUMBER_ATTR);
		Long productNumber = (Long)product.getValue(AERO_PRODUCT_NUMBER_ATTR);
		String colorwayName = (String)sku.getValue(AERO_COLORWAY_NAME_ATTR);
		String seasonName = (String)season.getValue(AERO_SEASON_NAME_ATTR);
		
		
		@SuppressWarnings("unchecked")
		Collection<LCSSourcingConfig> sconfigList = LCSSourcingConfigQuery.getSourcingConfigsForProduct(sku.getProduct());		
		LOGGER.debug("[resetCostSheetIntegrationFlags] Found " + sconfigList.size() + " sourcing configurations associated to colorway #" + colorwayNumber + " (" + colorwayName + ").");
		
		for (LCSSourcingConfig sourcingConfig : sconfigList) {
			
			Long sourcingConfigNum = (Long)sourcingConfig.getValue(AERO_SOURCING_CONFIG_NUMBER_ATTR);
			
			Collection<?> rawCostSheetList = LCSCostSheetQuery.getAllCostSheetsForSourcingConfig(sourcingConfig, includeWhatIfCostSheet);
			
			if (rawCostSheetList == null || rawCostSheetList.isEmpty()) {
				LOGGER.debug("[resetCostSheetIntegrationFlags] No eligible cost sheets were found associated to colorway #" + colorwayNumber + " (" + colorwayName + "), "
						+ "sourcing config #" + sourcingConfigNum + " (product #" + productNumber + " ).");
				continue;
			}
	        
			LOGGER.debug("[resetCostSheetIntegrationFlags] Found " + rawCostSheetList.size() + " RAW cost sheets associated to "
        			+ "sourcing config #" + sourcingConfigNum + " (product #" + productNumber + ").");
			
			for (Object rawCS : rawCostSheetList) {
				
				if (rawCS instanceof LCSCostSheet && FLEX_COST_SHEET_TYPE_PRODUCT.equalsIgnoreCase(((LCSCostSheet)rawCS).getCostSheetType())) {
					
					LCSProductCostSheet costSheet = (LCSProductCostSheet)rawCS;
					Long costSheetNumber = (Long)costSheet.getValue(AERO_COST_SHEET_NUMBER_ATTR);
					
					LOGGER.debug("[resetCostSheetIntegrationFlags] Inspecting cost sheet #" + costSheetNumber + ". List of colorways found: " + costSheet.getApplicableColorNames() + ".");
					
					if (!AeroIntegrationPluginUtil.isColorwayInCostSheet(colorwayName, costSheet)) {
						LOGGER.debug("[resetCostSheetIntegrationFlags] Skipping integration flag updates for cost sheet #" + costSheetNumber + ". "
								+ "Colorway '" + colorwayName + "' was not found within.");
					} else if (!AeroCostingIntegrationPlugin.isCostSheetConfirmed(costSheet)) {
						LOGGER.debug("[resetCostSheetIntegrationFlags] Skipping integration flag updates for cost sheet #" + costSheetNumber + ". "
								+ "Cost sheet satus is not eligible for Aero integration.");
					} else if (!AeroIntegrationPluginUtil.isCostSheetInSeason(costSheet, seasonName)) {
						LOGGER.debug("[resetCostSheetIntegrationFlags] Skipping integration flag updates for cost sheet #" + costSheetNumber + ". "
								+ "Cost sheet is not in the same season as the colorway (" + seasonName + ").");
					} else {
						
						LOGGER.debug("[resetCostSheetIntegrationFlags] Colorway '" + colorwayName + "' was found on cost sheet #" + costSheetNumber + ".");
						
						if (AeroCostingRepository.resetIntegrationControlFlags(costSheet)) {
							LOGGER.debug("[resetCostSheetIntegrationFlags] Updated Aero integration flags for cost sheet #" + costSheetNumber + ".");
						} else {
							LOGGER.debug("[resetCostSheetIntegrationFlags] Skipped Aero integration flags reset for cost sheet #" + costSheetNumber + ".");
						}
					}
					
				} else {
					LOGGER.debug("[resetCostSheetIntegrationFlags] A cost sheet record was found which is not of type PRODUCT. Cost Sheet is ignored.");
				}
			}//end cost sheet loop.
			
		}//end sourcing config for loop.
		
    }
	 private static boolean isInterfaceAdmin() {
        try {
            return MethodContext.getContext().getUserName().equals(INTERFACE_ADMIN_USER_NAME);
        } catch (Exception e) {
            LOGGER.error("Encountered an error while fetching current user, error:" + e.getMessage());
        }
        return false;
    }
    
}
