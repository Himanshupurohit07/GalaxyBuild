package com.sparc.wc.integration.services;

import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.*;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.integration.aero.domain.AeroPayloadAttribute;
import com.sparc.wc.integration.aero.repository.AeroArticleRepository;
import com.sparc.wc.integration.aero.util.AeroPayloadAttributeUtil;
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
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import wt.method.MethodContext;

import java.util.*;
import java.util.stream.Collectors;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.*;

/**
 * SparcProductWatchDogService class.<br>
 *
 * FIXES/AMENDMENTS:<br>
 * - Task #10313: Reset Sent To FC and CAP flags upon change of HTS Code 1 and HTS Code Wieght % attributes.<br>
 * - Added overloaded variants for method "setFlag".<br>
 * - Replaced use of "LCSSeasonSKULinkClientModel" by "LCSLogic" approach to skip execution of other SSP.
 *
 * @author Acnovate
 */
public class SparcProductWatchDogService {

    private final static Logger LOGGER = LogR.getLogger(SparcProductWatchDogService.class.getName());

    public void check(final LCSProduct deltaChangesProduct) throws WTPropertyVetoException,WTException,LuckyCustomPluginException {       

        LOGGER.debug("Delta Changes Product isRev-A:" + deltaChangesProduct.isRevA());
		
		
		LOGGER.debug("SparcProductWatchDogService CarriedOverFrom-------------"+deltaChangesProduct.getCarriedOverFrom());
		
		if(deltaChangesProduct.getCarriedOverFrom() != null){
		   LOGGER.debug("SparcProductWatchDogService Plugins skipped on Carryover");
		   return;
		}
        
        Long productNumber = (Long)deltaChangesProduct.getValue(AERO_PRODUCT_NUMBER_ATTR);

        LCSProduct existingProduct = (LCSProduct)VersionHelper.predecessorOf(deltaChangesProduct);
		
		
        if (existingProduct == null) {
            throw new LuckyCustomPluginException("No previous product version found for product #" + productNumber + ".");
        }
		
		Set<String> attrs = new HashSet<>();
		
		List<LCSSKUSeasonLink> allSkuSeasonLinkList = LuckyArticleRepository.getProductColorwaysSeasonLinks(productNumber);
		
		if (allSkuSeasonLinkList == null || allSkuSeasonLinkList.isEmpty()) {
            throw new LuckyCustomPluginException("Unable to find colorway season links for product #" + productNumber + ".");
        }
       
		//Start of July Build change to consider only the Size Range attribute when we receive the update from CAP
        Boolean isInterfaceAdminCheck = isInterfaceAdmin();
		LOGGER.debug("isInterfaceAdminCheck--------"+isInterfaceAdminCheck);
        if(isInterfaceAdminCheck) {
			attrs = SparcIntegrationConstants.LUCKY_PRODUCT_UPDATE_PROPERTY_DEFINITIONS.getAttrsFromHierarchy();
			LOGGER.debug("Interface Context Attributes---------------"+attrs);            
			
		//End of July Build change to consider only the Size Range attribute when we receive the update from CAP.        
        }
        else{
			attrs = SparcIntegrationConstants.LUCKY_PRODUCT_PLUGIN_PROPERTY_DEFINITIONS.getAttrsFromHierarchy();
			LOGGER.debug("Non Interface Context Attributes---------------"+attrs);
			validateAttrNonBlanks(deltaChangesProduct, allSkuSeasonLinkList, LUCKY_PRODUCT_PLUGIN_PROPERTY_DEFINITIONS.getNonBlankAttrs());	
		
        }

        Set<String> changedAttrList = SparcIntegrationUtil.getChangedAttributes(deltaChangesProduct,existingProduct,attrs);
        if(changedAttrList.isEmpty()){
			if(isInterfaceAdminCheck){
				LOGGER.debug("It is from interface admin context");
				return;
			}else{
               throw new LuckyCustomPluginException("No changes found for product #"+ productNumber + ".");
			}
        }
		
      //Start of July Build change to consider only the Size Range attribute when we receive the update from CAP.        
        if(isInterfaceAdminCheck) {
		 LOGGER.debug("Interface admin context");	
        validateAttrNonBlanks(deltaChangesProduct, allSkuSeasonLinkList, LUCKY_PRODUCT_UPDATE_PROPERTY_DEFINITIONS.getNonBlankAttrs());
		validateInoundAttrLocked(deltaChangesProduct, allSkuSeasonLinkList, changedAttrList);
		//End of July Build change to consider only the Size Range attribute when we receive the update from CAP.     
		}
        else{
		LOGGER.debug("Non Interface admin context");	       	
		validateAttrLocked(deltaChangesProduct, allSkuSeasonLinkList, changedAttrList);
        }
        
        boolean isLegStyleNumChanged =  isProductStyleLegacyChanged(existingProduct,deltaChangesProduct);
        //System.out.println("isLegStyleNumChanged---------------------------------"+isLegStyleNumChanged);
        for (LCSSKUSeasonLink colorwaySeasonLink : allSkuSeasonLinkList) {
            LuckyArticleRepository.resetIntegrationControlFlags(colorwaySeasonLink,isLegStyleNumChanged,false);
        }

        //Lucky Enhancement - Start
       String strValue = "";

        if(isLegStyleNumChanged){
            strValue = deltaChangesProduct.getValue(PRODUCT_NEW_VS_UPDATE_FLAG_INTERNAL_NAME) != null ? (String)deltaChangesProduct.getValue(PRODUCT_NEW_VS_UPDATE_FLAG_INTERNAL_NAME) : "";
            if(FormatHelper.hasContent(strValue) && PRODUCT_NEW_VS_UPDATE_FLAG_UPDATE_VALUE.equalsIgnoreCase(strValue)) {
                deltaChangesProduct.setValue(PRODUCT_NEW_VS_UPDATE_FLAG_INTERNAL_NAME, PRODUCT_NEW_VS_UPDATE_FLAG_NEW_VALUE);
            }
            for(LCSSKUSeasonLink objSSlink : allSkuSeasonLinkList){
                  markUpdatedOnColorwayOnUiContext(objSSlink);
            }
        }

        //Lucky Enhancement - End
    }


    /**
     * This method checks if the Legacy Style Number is changed or not and if it is changed it returns true
     * @param existingProduct
     * @param deltaChangesProduct
     * @return
     */
    private static boolean isProductStyleLegacyChanged(LCSProduct existingProduct,LCSProduct deltaChangesProduct){
        //Lucky Enhancement - Start
        final Object deltaChange = SparcIntegrationUtil.getValueFrom(existingProduct, LUCKY_COSTING_PRODUCT_LEGACY_STYLE_NUMBER_ATTR);
        final Object existing    = SparcIntegrationUtil.getValueFrom(deltaChangesProduct, LUCKY_COSTING_PRODUCT_LEGACY_STYLE_NUMBER_ATTR);
        LOGGER.debug("Checking (PRODUCT) divergence of attr:" + deltaChange + ", delta change:" + deltaChange + ", existing:" + existing );
        //Lucky Enhancement - End
        return SparcIntegrationUtil.divergenceCheck(deltaChange, existing);
    }

    private static void validateAttrNonBlanks(FlexTyped flexTyped,
                                              List<LCSSKUSeasonLink> skuSeasonLinkList,
                                              Set<String> attrList) throws LCSException,WTException {

        if (skuSeasonLinkList == null || skuSeasonLinkList.isEmpty() || attrList == null || attrList.isEmpty()) {
            LOGGER.debug("[validateAttrNonBlanks] Skipping non blank validation check.");
            return;
        }

        //Check if non-blank validation should occur or not.
        //boolean mustValidate = skuSeasonLinkList.stream().anyMatch(skuSL -> isProductionColorwayStatusValid(skuSL));
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
	

   
    private static void validateAttrLocked(FlexTyped flexTyped,
                                           List<LCSSKUSeasonLink> skuSeasonLinkList,
                                           Set<String> attrList) throws LCSException,WTException {

        if (skuSeasonLinkList == null || skuSeasonLinkList.isEmpty() || attrList == null || attrList.isEmpty()) {
            LOGGER.debug("[validateAttrLocked] Skipping locked validation check.");
            return;
        }

        boolean mustValidate = false;
        //Check if locked validation should occur or not.
        for (LCSSKUSeasonLink skuSL : skuSeasonLinkList) {
            mustValidate = isProductionColorwayStatusValid(skuSL);

            if (mustValidate) {
                break;
            }
        }//end for loop.

        if (mustValidate) {
            Set<String> lockedAttrs = LUCKY_PRODUCT_PLUGIN_PROPERTY_DEFINITIONS.getLockedAttrs();
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
    private static void validateInoundAttrLocked(FlexTyped flexTyped,
                                           List<LCSSKUSeasonLink> skuSeasonLinkList,
                                           Set<String> attrList) throws LCSException,WTException {

        if (skuSeasonLinkList == null || skuSeasonLinkList.isEmpty() || attrList == null || attrList.isEmpty()) {
            LOGGER.debug("[validateAttrLocked] Skipping locked validation check.");
            return;
        }

        boolean mustValidate = false;
        //Check if locked validation should occur or not.
        for (LCSSKUSeasonLink skuSL : skuSeasonLinkList) {
            mustValidate = isProductionColorwayStatusValid(skuSL);

            if (mustValidate) {
                break;
            }
        }//end for loop.
        FlexTyped sizeRangeObject = null;
		Object  strSizeRangeValue =  null;
        if (mustValidate) {
            Set<String> lockedAttrs = LUCKY_PRODUCT_UPDATE_PROPERTY_DEFINITIONS.getLockedAttrs();
			StringBuilder lockAttributes = new StringBuilder();
            attrList.retainAll(lockedAttrs);
            for(String strAttr: attrList){
                if (lockAttributes.length() > 0) {
                    lockAttributes.append(", ");
                }
                lockAttributes.append(SparcIntegrationUtil.convertToDisplayName(strAttr,flexTyped));
                sizeRangeObject = SparcIntegrationUtil.getValueFrom(flexTyped,strAttr) != null ? (FlexTyped)SparcIntegrationUtil.getValueFrom(flexTyped,strAttr) : null;
				if(sizeRangeObject != null){
				   strSizeRangeValue = SparcIntegrationUtil.getValueFrom(sizeRangeObject,"scISTYSIZR");			
				}
            }
            if (lockAttributes.length() > 0) {
                throw new LCSException("Requested action is unable to complete.\n\r"
                        + "The following list of attributes are locked and cannot be modified: " + SparcIntegrationUtil.convertToDisplayNames(lockedAttrs, flexTyped) + ". and " +
                        "Size Range value is "+strSizeRangeValue);
            }
        }
    }
		
		
    /**
     * This method updates the New vs Update flag to the new when the Legacy Style number is changed from the UI Perspective
     * @param link
     */
    public void markUpdatedOnColorwayOnUiContext(final LCSSKUSeasonLink link) {
        LCSSKU sku = null;
        try {
            LOGGER.debug("[markUpdatedOnColorway] Marking colorway New Vs Update flag..");
           
            final long skuARevId = (long) link.getSkuARevId();
            sku = (LCSSKU) SparcQueryUtil.findObjectById("VR:com.lcs.wc.product.LCSSKU:" + skuARevId);
            final String newVsUpdateValue = (String) SparcIntegrationUtil.getValueFrom(sku, SparcIntegrationConstants.COLORWAY_NEW_VS_UPDATE_FLAG_INTERNAL_NAME);

            if(SparcIntegrationConstants.PRODUCT_NEW_VS_UPDATE_FLAG_NEW_VALUE.equalsIgnoreCase(newVsUpdateValue)){
                System.out.println("It is a new Value only");
                return;
            }
            if(!VersionHelper.isCheckedOut(sku)) {
                sku = VersionHelper.checkout(sku);
                LOGGER.debug("[markUpdatedOnColorway] Colorway " + sku + " is checked-out.");
            }

            if (newVsUpdateValue == null || newVsUpdateValue.equals(PRODUCT_NEW_VS_UPDATE_FLAG_UPDATE_VALUE)) {
                sku.setValue(COLORWAY_NEW_VS_UPDATE_FLAG_INTERNAL_NAME, PRODUCT_NEW_VS_UPDATE_FLAG_NEW_VALUE);
            }

            LCSLogic.persist(sku, true);

            LOGGER.debug("[markUpdatedOnColorway] Marked colorway New Vs Update flag..");
        } catch (Exception e) {
            LOGGER.error("[markUpdatedOnColorway] Encountered an error while setting the New Vs Update flag on the Colorway, error:" + e, e);
        } finally {
            try {
                if(sku != null && VersionHelper.isCheckedOut(sku)){
                    VersionHelper.checkin(sku);
                    LOGGER.debug("[markUpdatedOnColorway] Product " + sku + " is checked-in.");
                }
            } catch (WTException e) {
                LOGGER.error("[markUpdatedOnColorway] failed to check in colorway " + sku + ". " + e.getMessage(), e);
            }
        }
    }

    private void checkNonBlankValues(final LCSProduct deltaChangesPrd, final LCSProduct existingPrd) throws LCSException {
        final Set<String> nonBlankAttrs = SparcIntegrationConstants.COLORWAY_PAYLOAD_PROPERTY_DEFINITIONS.getNonBlankAttrs();
        final Set<String> allAttributes = SparcIntegrationUtil.getAllAttributes(existingPrd.getFlexType(), "PRODUCT");
        allAttributes.retainAll(nonBlankAttrs);
        if (allAttributes.isEmpty()) {
            return;
        }
        if (!verifyExistenceOfProductionStatusColorway(existingPrd)) {
            return;
        }
        final boolean emptyValueDetected = allAttributes.stream().anyMatch(attr -> {
            final Object deltaChange = SparcIntegrationUtil.getValueFrom(deltaChangesPrd, attr);
            return deltaChange == null || deltaChange.toString().trim().isEmpty();
        });
        if (emptyValueDetected) {
            throw new LCSException("The attributes in the given list:" + SparcIntegrationUtil.convertToDisplayNames(allAttributes, existingPrd) + " cannot be null/empty when the Colorway status is in Production");
        }
    }

    private void enforceAttrsLock(final LCSProduct deltaChangesPrd, final LCSProduct existingPrd) throws LCSException {
        final Set<String> lockedAttrs       = SparcIntegrationConstants.COLORWAY_PAYLOAD_PROPERTY_DEFINITIONS.getLockedAttrs();
        final Set<String> objRefLockedAttrs = SparcIntegrationConstants.PRODUCT_OBJECT_REF_LOCKED_INTERNAL_NAMES;
        if (objRefLockedAttrs != null) {
            lockedAttrs.addAll(objRefLockedAttrs);
        }
        if (lockedAttrs.isEmpty()) {
            return;
        }
        if (!verifyExistenceOfProductionStatusColorway(existingPrd)) {
            return;
        }
        final boolean changeDetected = lockedAttrs.stream().anyMatch(attr -> {
            final Object deltaChange = SparcIntegrationUtil.getValueFrom(deltaChangesPrd, attr);
            final Object existing    = SparcIntegrationUtil.getValueFrom(existingPrd, attr);
            LOGGER.debug("Checking (PRODUCT) divergence for locking of attr:" + attr + ", delta change:" + deltaChange + ", existing:" + existing);
            return SparcIntegrationUtil.divergenceCheck(deltaChange, existing) == true;
        });
        if (changeDetected) {
            throw new LCSException("Cannot modify attributes from list:" + SparcIntegrationUtil.convertToDisplayNames(lockedAttrs, existingPrd) + ", when the colorway status is Production");
        }
    }

    public boolean verifyExistenceOfProductionStatusColorway(final LCSProduct prd) {
        final List<LCSSeason> luckySeasons = findLuckySeasons(prd);
        return luckySeasons.stream().anyMatch(season -> {
            try {
                @SuppressWarnings("unchecked")
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

    private boolean checkHierarchyAttrsDivergence(final LCSProduct existingPrd, final LCSProduct deltaChangesPrd, SparcPropertyDefinitions.Hierarchy hierarchy) {
        final Set<String> attrs       = SparcIntegrationConstants.COLORWAY_PAYLOAD_PROPERTY_DEFINITIONS.getAttrsFromHierarchy(hierarchy);
        final Set<String> objRefAttrs = SparcIntegrationConstants.PRODUCT_OBJECT_REF_LOCKED_INTERNAL_NAMES;
        if (objRefAttrs != null && hierarchy == SparcPropertyDefinitions.Hierarchy.FC) {
            attrs.addAll(objRefAttrs);
        }
        if (hierarchy == SparcPropertyDefinitions.Hierarchy.STYLE) {
            if (SparcIntegrationConstants.PRODUCT_EXPLICIT_DIVERGENCE_CHECK_INTERNAL_NAMES != null) {
                attrs.addAll(SparcIntegrationConstants.PRODUCT_EXPLICIT_DIVERGENCE_CHECK_INTERNAL_NAMES);
            }
        }
        if (attrs.isEmpty()) {
            return false;
        }
        return attrs.stream().anyMatch(attr -> {
            final Object deltaChange = SparcIntegrationUtil.getValueFrom(deltaChangesPrd, attr);
            final Object existing    = SparcIntegrationUtil.getValueFrom(existingPrd, attr);
            LOGGER.debug("Checking (PRODUCT) divergence of attr:" + attr + ", delta change:" + deltaChange + ", existing:" + existing + ", Hierarchy:" + hierarchy);
            return SparcIntegrationUtil.divergenceCheck(deltaChange, existing) == true;
        });
    }
    
    public void setFlag(final LCSProduct prd, final LCSSeason season, final Set<SparcPropertyDefinitions.Hierarchy> hierarchies) {
        if (prd == null || season == null || hierarchies == null) {
            LOGGER.error("Cannot set FC/CAP/S4 sent flag without valid product, season and hierarchy");
            return;
        }
        LOGGER.info("Setting Sent To FC/Sent To CAP/Sent To S4 flags to NO for Product:" + prd.getName());
        try {
            @SuppressWarnings("unchecked")
			final Collection<LCSPartMaster> skuMasters = LCSSeasonQuery.getSKUMastersForSeasonAndProduct(season, prd, false, false);
            if (skuMasters == null || skuMasters.isEmpty()) {
                return;
            }
            final List<LCSSKU> skus = SparcQueryUtil.<LCSSKU>findLatestIterationsOf(skuMasters);
            setFlag(skus, season, hierarchies);
        } catch (Exception e) {
            LOGGER.error("Encountered an error while setting FC/CAP on the given product and season, error:" + e);
        }
    }
    
    public void setFlag(final LCSProduct prd, final LCSSeason season, final SparcPropertyDefinitions.Hierarchy hierarchy) throws LCSException {
		System.out.println("inside Set Flag");
        if(prd == null||season == null||hierarchy == null){
            LOGGER.error("Cannot set FC/CAP/S4 sent flag without valid product, season and hierarchy");
            return;
        }
        LOGGER.info("Setting Sent To FC/Sent To CAP/Sent To S4 flags to NO for Product:" + prd.getName());
        try {
            @SuppressWarnings("unchecked")
			final Collection<LCSPartMaster> skuMasters = LCSSeasonQuery.getSKUMastersForSeasonAndProduct(season, prd, false, false);
            if (skuMasters == null || skuMasters.isEmpty()) {
                return;
            }
            final List<LCSSKU> skus = SparcQueryUtil.<LCSSKU>findLatestIterationsOf(skuMasters);
            setFlag(skus, season, hierarchy);
        }catch(LCSException ex){
            throw new LCSException(ex.getMessage());
        }
        catch (Exception e) {
            LOGGER.error("Encountered an error while setting FC/CAP on the given product and season, error:" + e);
        }
    }

    public void setFlag(final LCSProduct prd, final SparcPropertyDefinitions.Hierarchy hierarchy) {
        LOGGER.info("[setFlag] Setting Sent To FC/Sent To CAP flags to NO for Product:" + prd.getName());
        final List<LCSSeason> luckySeasons = findLuckySeasons(prd);
        luckySeasons.stream().forEach(season -> {
            try {
                @SuppressWarnings("unchecked")
				final Collection<LCSPartMaster> skuMasters = LCSSeasonQuery.getSKUMastersForSeasonAndProduct(season, prd, false, false);
                if (skuMasters == null || skuMasters.isEmpty()) {
                    return;
                }
                final List<LCSSKU> skus = SparcQueryUtil.<LCSSKU>findLatestIterationsOf(skuMasters);
                setFlag(skus, season, hierarchy);
            }catch (Exception e) {
                System.out.println("[setFlag] throws Excption---22222222222-"+e.getMessage());
                LOGGER.error("Encountered an error while setting FC/CAP on the given product and season, error:" + e);
            }
        });
    }
    
    private void setFlag(final Collection<LCSSKU> skus, final LCSSeason season, final Set<SparcPropertyDefinitions.Hierarchy> hierarchies) throws LCSException {
    	
    	if (skus == null || skus.isEmpty() || season == null || hierarchies == null || hierarchies.isEmpty()) {
    		return;
    	}
    	
    	for (LCSSKU sku : skus) {
    		
    		for (SparcPropertyDefinitions.Hierarchy hierarchy : hierarchies) {
    			
    			try {
    				final LCSSeasonProductLink spl = LCSSeasonQuery.findSeasonProductLink(sku, season);
        			setFlag(spl, hierarchy);
    			} catch(LCSException ex){
                    throw new LCSException(ex.getMessage());
                } catch (Exception e) {
    				LOGGER.error("[SparcProductWatchDogService] Encountered an error while fetching skulink from given sku and season, error: " + e.getMessage(), e);
    			}
    			
    		}//end hierarchies for loop.
    		
    	}//end skus for loop.
    }
    
    private void setFlag(final Collection<LCSSKU> skus, final LCSSeason season, final SparcPropertyDefinitions.Hierarchy hierarchy) throws LCSException {     
        for(LCSSKU sku :skus)
        {
            try {
                final LCSSeasonProductLink spl = LCSSeasonQuery.findSeasonProductLink(sku, season);
                setFlag(spl, hierarchy);                
            } catch(LCSException ex){
                throw new LCSException(ex.getMessage());
            } catch (Exception e) {
            	LOGGER.error("[SparcProductWatchDogService] Encountered an error while fetching skulink from given sku and season, error: " + e.getMessage(), e);
            }

        }
    }
    
    private void setFlag(final LCSSeasonProductLink spl, final SparcPropertyDefinitions.Hierarchy hierarchy) throws LCSException {
    	
    	if (spl == null || hierarchy == null) {
    		return;
    	}
    	
    	try {
    		
    		if (SparcPropertyDefinitions.Hierarchy.STYLE == hierarchy) {
    			spl.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME, SparcIntegrationConstants.SENT_NO);
    			spl.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME, SparcIntegrationConstants.SENT_NO);
            } else if (SparcPropertyDefinitions.Hierarchy.FC == hierarchy) {
            	spl.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME, SparcIntegrationConstants.SENT_NO);
            } else if (SparcPropertyDefinitions.Hierarchy.CAP == hierarchy) {
            	spl.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME, SparcIntegrationConstants.SENT_NO);
            }else if(SparcPropertyDefinitions.Hierarchy.S4 == hierarchy){
            	spl.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_S4_INTERNAL_NAME, SparcIntegrationConstants.SENT_NO);
            }
    		
    		LCSLogic.persist(spl, true);
    		LOGGER.error("[SparcProductWatchDogService] Integration flags for hierarchy " + hierarchy + " were updated.");
        } catch(LCSException ex){
            throw new LCSException(ex.getMessage());
        } catch (Exception e) {
            LOGGER.error("[SparcProductWatchDogService] Encountered an error while updating integration flags for hierarchy " + hierarchy + ", error: " + e.getMessage(), e);
        }
    }

    private List<LCSSeason> findLuckySeasons(final LCSProduct product) {
        try {
            final LCSSeasonQuery              lsq           = new LCSSeasonQuery();
            @SuppressWarnings("unchecked")
			final Collection<LCSSeasonMaster> seasonMasters = lsq.findSeasons(product);
            if (seasonMasters == null || seasonMasters.isEmpty()) {
                return new ArrayList<>();
            }
            final List<LCSSeason> seasons = SparcQueryUtil.<LCSSeason>findLatestIterationsOf(seasonMasters);
            return seasons.stream()
                    .filter(season -> season.getFlexType().getFullName(true).startsWith(SparcIntegrationConstants.LUCKY_SEASON_FLEX_PATH))
                    .filter(season -> {
                        final String seasonType = (String) SparcIntegrationUtil.getValueFrom(season, SparcIntegrationConstants.DEVELOPMENT_SEASON_INTERNAL_NAME);
                        if (seasonType == null || seasonType.isEmpty()) {
                            return false;
                        }
                        return SparcIntegrationConstants.LUCKY_DEVELOPMENT_SEASONS.contains(seasonType);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Encountered an while searching for seasons for the given product, error:" + e, e);
        }
        return new ArrayList<>();
    }

    private static boolean isInterfaceAdmin() {
        try {
            return MethodContext.getContext().getUserName().equals(SparcIntegrationConstants.INTERFACE_ADMIN_USER_NAME);
        } catch (Exception e) {
            LOGGER.error("Encountered an error while fetching current user, error:" + e.getMessage());
        }
        return false;
    }
}
