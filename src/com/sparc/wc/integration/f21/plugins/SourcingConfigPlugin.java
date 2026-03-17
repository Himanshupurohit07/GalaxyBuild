package com.sparc.wc.integration.f21.plugins;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Logger;

import wt.method.MethodContext;

import wt.fc.WTObject;
import wt.log4j.LogR;

import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.sourcing.SourcingConfigHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import com.lcs.wc.util.FormatHelper;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_SENT_TO_SCM21_SCNO;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_SC_SENT_TO_SCM21_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_INTEGRATION_CONTEXT_FLAG_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_INTEGRATION_CONTEXT_FLAG_UPDATED;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_FLEX_TYPE_PATH_KEY;

/**
 * Flex Plugin for use to update the sentToSCM flag based on the update of selected integration attributes and/or colorway additions.<br>
 * FIXES/AMENDMENTS:<br>
 * - Various deployment fixes to correct NullPointerException issues causing red line errors.
 * - Bug #8693: Added filtering criteria to LCSSourcingConfig, LCSSourceToSeasonLink and LCSSeasonProductLink to skip processing non-Forever21 types.
 * - Added constants to replace hardcoded values.
 * - Bug #8658: Fixed Source To Season Link lookup of previous version so attributes between versions can get compared appropriately.
 * - Bug #8694: Removed check for previous version that prevented tracking colorway additions.
 * - Fixed colorway logic: Set proper list of attributes to use. 
 * - Fixed isChanged logic: Added initial check for previous version to perform a clean exit.
 * - Replaced sysout calls with LogR logging.
 * - Code refactor into multiple methods, one for each flex type.
 * 
 * @author Acnovate
 */
public class SourcingConfigPlugin {


    private static final String SOURCINGATTR = LCSProperties.get("com.sparc.wc.sourcing.SourcingConfigPlugin.setSCMtoNo.SourcingAttributes", "");
    private static final String SEASONSOURCINGATTR = LCSProperties.get("com.sparc.wc.sourcing.SourcingConfigPlugin.setSCMtoNo.SeasonSourcingAttributes", "");
    private static final String PRODUCTATTR = LCSProperties.get("com.sparc.wc.sourcing.SourcingConfigPlugin.setSCMtoNo.ProductAttributes", "");
    private static final String SEASONPRODUCTATTR = LCSProperties.get("com.sparc.wc.sourcing.SourcingConfigPlugin.setSCMtoNo.SeasonProductAttributes", "");
    private static final String SEASONSKUATTR = LCSProperties.get("com.sparc.wc.sourcing.SourcingConfigPlugin.setSCMtoNo.SeasonSKUAttributes", "");
    private static final String SKUATTR = LCSProperties.get("com.sparc.wc.sourcing.SourcingConfigPlugin.setSCMtoNo.SKUAttributes", "");
    
    private static final Logger LOGGER = LogR.getLogger(SourcingConfigPlugin.class.getName());
    
    public static void setSCMtoNo(WTObject obj) throws WTException, WTPropertyVetoException {
    	LOGGER.info("==============in setSCMtoNo=========");
        LOGGER.debug("WTObject type: " + obj.getDisplayIdentity());

        if (obj instanceof LCSSourcingConfig) {
        	
        	validateSourcingConfig((LCSSourcingConfig)obj);
        	
        } else if (obj instanceof LCSSourceToSeasonLink) {
            
        	validateSourceToSeasonLink((LCSSourceToSeasonLink)obj);
            
        } else if (obj instanceof LCSProduct) {
        	
            validateProduct((LCSProduct)obj);
        	
        } else if (obj instanceof LCSSKU) {
        	
        	validateColorway((LCSSKU)obj);
        	
        } else if (obj instanceof LCSSeasonProductLink) {
        	
            validateSeasonProductLink((LCSSeasonProductLink)obj);
        	
        } else {
        	
        	LOGGER.debug("Object type " + obj.getDisplayIdentity() + " ignored.");
        	
        }
        
        LOGGER.info("=============DONE with setSCMtoNo=========");
    }
    
    /**
     * Validates the Sourcing Configuration instance if it meets the criteria for updating the Sent To SCM flag.
     * @param config The sourcing configuration instance.
     * @throws WTException If an error occurs while accessing sourcing configuration attributes or updating the Sent To SCM flag.
     */
    private static void validateSourcingConfig(LCSSourcingConfig config) throws WTException {
    	
    	LOGGER.debug("---sourcing config---------" + config);
		LCSProduct objProduct = (LCSProduct)VersionHelper.latestIterationOf(config.getProductMaster());
		String productType = objProduct.getFlexType().getFullName(true);
		
		LOGGER.debug("productType-----1111111----------"+productType);
		
		if(!(productType.contains(F21_SCM_FLEX_TYPE_PATH_KEY))) {
			return;
		}
        
		String sendToSCM = (String) config.getValue(F21_SCM_SC_SENT_TO_SCM21_INTERNAL_NAME);
        
		if (MethodContext.getContext().containsKey(F21_SCM_INTEGRATION_CONTEXT_FLAG_KEY)) {

            String isIntegrationContext = (String) MethodContext.getContext().get(F21_SCM_INTEGRATION_CONTEXT_FLAG_KEY);
            LOGGER.debug("isIntegrationContext---------------------" + isIntegrationContext);
            
            if (F21_SCM_INTEGRATION_CONTEXT_FLAG_UPDATED.equalsIgnoreCase(isIntegrationContext)) {
                MethodContext.getContext().remove(F21_SCM_INTEGRATION_CONTEXT_FLAG_KEY);
                return;
            }
        }
        
		if (F21_SCM_SENT_TO_SCM21_SCNO.equalsIgnoreCase(sendToSCM)) {
            return;
        }
        
		LCSSourcingConfig prevConfig = (LCSSourcingConfig) VersionHelper.predecessorOf(config);
        if (prevConfig == null) {
            return;
        }

		if (prevConfig != null && isChanged(config, prevConfig, SOURCINGATTR)) {
			config.setValue(F21_SCM_SC_SENT_TO_SCM21_INTERNAL_NAME, F21_SCM_SENT_TO_SCM21_SCNO);
		}
    	
    }
    
    /**
     * Validates the Source To Season Link instance if it meets the criteria for updating the Sent To SCM flag.
     * @param sslink The Source To Season Link instance.
     * @throws WTException If an error occurs while accessing source to season link attributes or updating the Sent To SCM flag.
     */
    private static void validateSourceToSeasonLink(LCSSourceToSeasonLink sslink) throws WTException {
    	
    	LOGGER.debug("---source to season link---------" + sslink);
    	
        LCSSourcingConfig config = (LCSSourcingConfig) VersionHelper.latestIterationOf(sslink.getSourcingConfigMaster());
		LCSProduct objProduct = (LCSProduct)VersionHelper.latestIterationOf(config.getProductMaster());
		String productType = objProduct.getFlexType().getFullName(true);
		LOGGER.debug("productType------222222---------"+productType);
		
		if(!(productType.contains(F21_SCM_FLEX_TYPE_PATH_KEY))) {
			return;
		}
        
		String sendToSCM = (String) config.getValue(F21_SCM_SC_SENT_TO_SCM21_INTERNAL_NAME);
        if (F21_SCM_SENT_TO_SCM21_SCNO.equalsIgnoreCase(sendToSCM)) {
            return;
        }
        
        Object preSSlink = VersionHelper.predecessorOf(sslink);
        LOGGER.debug("preSSlink-----------------------" + preSSlink);
        
        if (preSSlink == null) {
            return;
        }
        
        if (isChanged(sslink, (LCSSourceToSeasonLink)preSSlink, SEASONSOURCINGATTR)) {
            setSentToSCM(config);
        }
    	
    }
    
    /**
     * Validates the Product instance if it meets the criteria for updating the Sent To SCM flag.
     * @param prod The product instance.
     * @throws WTException If an error occurs while accessing product attributes or updating the Sent To SCM flag.
     */
    private static void validateProduct(LCSProduct prod) throws WTException {
    	
    	LOGGER.debug("---Product---------" + prod);
        LCSProduct prevProd = (LCSProduct) VersionHelper.predecessorOf(prod);
        LOGGER.debug("prevProd-------------" + prevProd);
        
        if (prevProd == null) {
            return;
        }

        if (isChanged(prod, prevProd, PRODUCTATTR)) {
        	
            LOGGER.debug("Get Version Identifier Value--------" + prod.getVersionIdentifier().getValue());
            
            @SuppressWarnings("unchecked")
			Collection<LCSSourcingConfig> sconfigs = LCSSourcingConfigQuery.getSourcingConfigsForProduct(prod);
            LOGGER.debug("sconfigs------------------" + sconfigs);
            
            Iterator<LCSSourcingConfig> it = sconfigs.iterator();
            
            while (it.hasNext()) {
                LCSSourcingConfig config = it.next();
                LOGGER.debug("config-111------------------" + config);
                LOGGER.debug("config 2222------------" + VersionHelper.getVersion(config, "A"));
                setSentToSCM(config);
            }
        }
    	
    }
    
    /**
     * Validates the Colorway (SKU) instance if it meets the criteria for updating the Sent To SCM flag.
     * @param skuObj The colorway (SKU) instance.
     * @throws WTException If an error occurs while accessing colorway attributes or updating the Sent To SCM flag.
     */
    private static void validateColorway(LCSSKU skuObj) throws WTException {
    	
    	LOGGER.debug("---Product---sku------" + skuObj);
        LOGGER.debug("is Placeholder-------------" + skuObj.isPlaceholder());
        
        if (skuObj.isPlaceholder()) {
            LOGGER.warn("SKU is a placeholder");
            return;
        }
        
        LCSSKU prevSku = (LCSSKU) VersionHelper.predecessorOf(skuObj);
        LOGGER.debug("skuObj-----------------" + skuObj);
        LOGGER.debug("prevSku-----------------" + prevSku);
        
        if (isChanged(skuObj, prevSku, SKUATTR)) {
        	
            @SuppressWarnings("unchecked")
			Collection<LCSSourcingConfig> sconfigs = LCSSourcingConfigQuery.getSourcingConfigsForProduct(skuObj.getProduct());
            Iterator<LCSSourcingConfig> it = sconfigs.iterator();
            while (it.hasNext()) {
                LCSSourcingConfig config = (LCSSourcingConfig) it.next();
                setSentToSCM(config);
            }
            
        }
    	
    }
    
    /**
     * Validates The Season Product Link instance (For both SKU and PRODUCT) if it meets the criteria for updating the Sent To SCM flag.
     * @param splink The Season Product Link instance.
     * @throws WTException If an error occurs while accessing season product link attributes or updating the Sent To SCM flag.
     */
    private static void validateSeasonProductLink(LCSSeasonProductLink splink) throws WTException {
    	
    	LOGGER.debug("---LCSSeasonProductLink---------" + splink);
        
        if (splink != null) {
        	
            LCSSeasonProductLink preSPlink = LCSSeasonQuery.getPriorSeasonProductLink(splink);

            if (preSPlink == null) {
                return;
            }
            
            String linkType = splink.getSeasonLinkType();
            LOGGER.debug("---LCSSeasonProductLink------linkType---" + linkType);
            
            if (linkType.equals("SKU")) {
                LCSSKU sku = (LCSSKU) VersionHelper.latestIterationOf(splink.getOwner().getMaster());
                
                String colorwayType = sku.getFlexType().getFullName(true);
    			LOGGER.debug("colorwayType------111111---------"+colorwayType);
    			
    			if(!(colorwayType.contains(F21_SCM_FLEX_TYPE_PATH_KEY))) {
    				return;
    			}
                
                if (isChanged(splink, preSPlink, SEASONSKUATTR)) {
                    @SuppressWarnings("unchecked")
					Collection<LCSSourcingConfig> sconfigs = LCSSourcingConfigQuery.getSourcingConfigsForProduct(sku.getProduct());
                    Iterator<LCSSourcingConfig> it = sconfigs.iterator();
                    while (it.hasNext()) {
                        LCSSourcingConfig config = (LCSSourcingConfig) it.next();
                        setSentToSCM(config);
                    }

                }
            } else {
                LCSProduct prod = (LCSProduct) VersionHelper.latestIterationOf(splink.getOwner().getMaster());
                
                String productType = prod.getFlexType().getFullName(true);
    			LOGGER.debug("productType-----333333----------"+productType);
    			
    			if(!(productType.contains(F21_SCM_FLEX_TYPE_PATH_KEY))) {
    				return;
    			}
                
                if (isChanged(splink, preSPlink, SEASONPRODUCTATTR)) {
                    @SuppressWarnings("unchecked")
					Collection<LCSSourcingConfig> sconfigs = LCSSourcingConfigQuery.getSourcingConfigsForProduct(prod);
                    Iterator<LCSSourcingConfig> it = sconfigs.iterator();
                    while (it.hasNext()) {
                        LCSSourcingConfig config = (LCSSourcingConfig) it.next();
                        setSentToSCM(config);
                    }

                }
            }
        }
    	
    }
    
    /**
     * Checks whether current flex object is changed when compared to its previous version.
     * @param current The current flex object to check.
     * @param previous The previous flex object to compare against the current one.
     * @param attributes List of flex object attributes to use for the comparison.
     * @return true if the object has changed or if no previous object was provided, false otherwise.
     * @throws WTException If an error occurs while verifying attribute values from the current and/or previous flex objects.
     */
    private static boolean isChanged(FlexTyped current, FlexTyped previous, String attributes) throws WTException {
    	
    	Boolean isChanged = false;
    	
    	if (previous == null) {
    		isChanged = true;
    		LOGGER.debug("=============DONE with isChanged (no previous flex version), returning " + isChanged + " === ");
    		return isChanged;
    	}
    	
        try {
        	
            List<String> removeAtts = Arrays.asList(attributes.split("\\s*,\\s*"));
            Iterator<String> listIter = removeAtts.iterator();
            
            while (listIter.hasNext()) {
                String attribute = listIter.next();
                if (FormatHelper.hasContent(attribute)) {
                	
                    Object currAttrValue = current.getValue(attribute);
                    LOGGER.debug("=============currAttrValue =========" + currAttrValue);
                    
                    Object attrPrev = null;
                    if (previous != null) {
                        attrPrev = previous.getValue(attribute);
                    }
                    
                    LOGGER.debug("=============attrPrev =========" + attrPrev);
                    
                    if (currAttrValue != null && attrPrev != null) {
                        if (!(attrPrev.toString().compareTo(currAttrValue.toString()) == 0)) {
                            isChanged = true;
                            break;
                        }
                    } else if ((currAttrValue != null && attrPrev == null) || (attrPrev != null && currAttrValue == null)) {
                        //There is a difference
                        isChanged = true;
                        break;

                    }
                }
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            isChanged = false;
        }
        
        LOGGER.debug("isChanged---------------" + isChanged);
        return isChanged;
    }
    
    /**
     * Updates the Sent To SCM flag for the given WTObject, provided it is LCSSourcingConfig instance.<br>
     * Note: Snt To SCM flag won't be updated if it's already set to "No" (scNo).
     * @param obj The WTObject object to update.
     * @throws WTException If an error occurs while updating the Sent To SCM flag at PLM.
     */
    private static void setSentToSCM(WTObject obj) throws WTException {
        
    	LOGGER.info("=============about to change SCM Flag=obj=");
        
        if (obj instanceof LCSSourcingConfig) {
        	
            LCSSourcingConfig config = (LCSSourcingConfig) obj;
            String sentToSCM = (String)config.getValue(F21_SCM_SC_SENT_TO_SCM21_INTERNAL_NAME);
            
            LOGGER.info("============= Sourcing Config = " + config.getName());
            LOGGER.info("============= SCM Flag (before update) = " + sentToSCM);
            
            if (!F21_SCM_SENT_TO_SCM21_SCNO.equalsIgnoreCase((String) config.getValue(F21_SCM_SC_SENT_TO_SCM21_INTERNAL_NAME))) {
            	
                config.setValue(F21_SCM_SC_SENT_TO_SCM21_INTERNAL_NAME, F21_SCM_SENT_TO_SCM21_SCNO);
                SourcingConfigHelper.service.saveSourcingConfig(config);
                LOGGER.info("===saved-------config");
                
            }
            
            LOGGER.info("============= SCM Flag (after update) = " + config.getValue(F21_SCM_SC_SENT_TO_SCM21_INTERNAL_NAME));
            
        }
        
    }

}
