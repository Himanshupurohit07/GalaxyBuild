package com.sparc.wc.costsheet;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSCostSheetLogic;
import com.lcs.wc.sourcing.LCSCostSheetMaster;
import com.lcs.wc.sourcing.LCSCostSheetQuery;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.util.SparcConstants;
import com.lcs.wc.country.LCSCountry;
import com.sparc.wc.integration.aero.plugins.AeroCostingIntegrationPlugin;

import wt.fc.ObjectIdentifier;
import wt.fc.PersistInfo;
import wt.fc.WTObject;
import wt.method.MethodContext;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;



public class SPARCNonCalculatedCostingPlugin {
	
	private static final Logger LOGGER = LogManager.getLogger(SPARCNonCalculatedCostingPlugin.class.getName());
	private static String productORColorwayAtts = LCSProperties.get("com.lcs.wc.product.CostSheet.productORColorwayLevelAttributes");
	private static String productLevelAtts = LCSProperties.get("com.lcs.wc.product.CostSheet.productLevelAttributes");
	private static String sourcing2ndLevelAtts = LCSProperties.get("com.lcs.wc.product.CostSheet.sourcing2ndLevelAttributes");
	private static Collection<String> productORColorwayAttsCol = FormatHelper.commaSeparatedListToCollection(productORColorwayAtts);
	private static Collection<String> productLevelAttsCol = FormatHelper.commaSeparatedListToCollection(productLevelAtts);
	private static Collection<String> sourcing2ndLevelAttsCol = FormatHelper.commaSeparatedListToCollection(sourcing2ndLevelAtts);
	private static final String SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT = LCSProperties.get("com.lcs.wc.product.CostSheet.SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT", "SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT");
	private static final String SPARC_REFRESH_MULTI_COSTSHEET = LCSProperties.get("com.lcs.wc.product.CostSheet.SPARC_REFRESH_MULTI_COSTSHEET", "SPARC_REFRESH_MULTI_COSTSHEET");
	private static final String SPARC_CUSTOMREP_COLORWAY = LCSProperties.get("com.lcs.wc.product.CostSheet.SPARC_CUSTOMREP_COLORWAY", "SPARC_CUSTOMREP_COLORWAY");
	
	
	
	/**
	 * This method is registered for SSP to derive the attribute values from other sources and set it in Costsheet
	 * @param obj
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static void updateNonCalculatedCSAttrsFromOtherSources(WTObject obj) throws WTException, WTPropertyVetoException {
		
	
		LOGGER.debug("**updateNonCalculatedCSAttrsFromOtherSources - start:"+obj);
		if(!(obj instanceof LCSProductCostSheet))
			return;
		LCSProductCostSheet costSheetObj = (LCSProductCostSheet)obj;
		LOGGER.debug("**updateNonCalculatedCSAttrsFromOtherSources:"+costSheetObj+":::"+costSheetObj.getFlexType().getFullName(true)+":::"+costSheetObj.getName());
		if(costSheetObj.getFlexType().getFullName(true).equalsIgnoreCase(SparcConstants.SPARC_COSTSHEET))
				return;
		
		String sourcingConfigRevID ="";
		if(costSheetObj.getSourcingConfigRevId()>0)
			sourcingConfigRevID = String.valueOf(new BigDecimal(costSheetObj.getSourcingConfigRevId()).longValue());
			LCSSeasonMaster seasonMaster = costSheetObj.getSeasonMaster();
		
		if(sourcingConfigRevID!=null && FormatHelper.hasContent(sourcingConfigRevID) && seasonMaster!=null) {
			
			LOGGER.debug("costSheetObj.getCopiedFromReference():"+costSheetObj.getCopiedFromReference());
			LOGGER.debug("costSheetObj.getCopiedFrom():"+costSheetObj.getCopiedFrom());
			LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(seasonMaster);
			LCSSourcingConfig sourcingConfig = (LCSSourcingConfig) LCSQuery.findObjectById(SparcConstants.SOURCING_CONFIG_VR+sourcingConfigRevID);
		
			LCSProduct product = (LCSProduct) VersionHelper.latestIterationOf(sourcingConfig.getProductMaster());
			product = (LCSProduct) VersionHelper.getVersion(product, "A");

			LCSSeasonProductLink seasonProdLink = null;
			seasonProdLink = getSeasonProductLink(product,season,costSheetObj);
		
			//Set Product level attributes into CS
			setCSAttributesFromProduct(product,costSheetObj);
		
			//Set Product OR Colorway level attributes into CS
			if(seasonProdLink != null){
			  setCSAttributesFromProductORColorway(seasonProdLink, costSheetObj);
			}
		
			//Set Sourcing level attributes into CS
			setCSAttributesFromSourcing(sourcingConfig,costSheetObj);
		
			LOGGER.debug("**updateNonCalculatedCSAttrsFromOtherSources - Completed Setting up all non calculated attribute Values ont Cost Sheet***");
		
	}
	
}
	
	/**
	 * 	This method is to set the Cost sheet attributes either from ProductSeasonLink or ColorwaySeasonLink if Representative Colorway is selected
	 * @param seasonProdLink
	 * @param costSheetObj
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static void setCSAttributesFromProductORColorway(LCSSeasonProductLink seasonProdLink, LCSProductCostSheet costSheetObj) throws WTException, WTPropertyVetoException {
		
		Iterator<String> stringIterator = null;
		String tempString="";
		String sourceAttKey="";
		String targetAttKey="";
		Object sourceValue =null;
		if(productORColorwayAtts!=null && productORColorwayAttsCol.size()>0) {
			stringIterator = productORColorwayAttsCol.iterator();
			while(stringIterator.hasNext()) {
				tempString = stringIterator.next();
				sourceAttKey = MOAHelper.firstValue(tempString);
				targetAttKey = MOAHelper.lastValue(tempString);
				sourceValue = seasonProdLink.getValue(sourceAttKey);
				if(sourceValue!=null && FormatHelper.hasContentAllowZero(String.valueOf(sourceValue))) {
					sourceValue = getValue(sourceAttKey,seasonProdLink,targetAttKey,costSheetObj);
					costSheetObj.setValue(targetAttKey, sourceValue);
				}
				else
					costSheetObj.setValue(targetAttKey, "");
			}
		}
	}
	/**
	 * This method is to get the Representative Color String value from Cost sheet
	 * @param costSheet
	 * @return
	 * @throws WTException
	 */
public static Collection<FlexObject> getRepresentativeColor(LCSProductCostSheet costSheet) throws WTException {
		
		Collection<FlexObject> representativeColorCol = null;
		PersistInfo  costSheetMasterPersisitInfo= costSheet.getMaster().getPersistInfo();
		LOGGER.debug("**NONCalculated Attribute Derivation:costSheetMasterPersisitInfo:"+costSheetMasterPersisitInfo);
		ObjectIdentifier costSheetMasterObjectIdentifier = null;
		long costSheetMasterID = 0;
		if(costSheetMasterPersisitInfo!=null) {
		costSheetMasterObjectIdentifier = costSheet.getMaster().getPersistInfo().getObjectIdentifier();
		LOGGER.debug("**NONCalculated Attribute Derivation:costSheetMasterObjectIdentifier:"+costSheetMasterObjectIdentifier);
			}
		if(costSheetMasterObjectIdentifier!=null) {
			costSheetMasterID = costSheetMasterObjectIdentifier.getId();
			LOGGER.debug("**NONCalculated Attribute Derivation:costSheetMasterID:"+costSheetMasterID);
			}
		if(costSheetMasterID>0) {
			representativeColorCol = LCSCostSheetQuery.getRepresentativeColor((LCSCostSheetMaster)costSheet.getMaster());
			}
		
		LOGGER.debug("**NONCalculated Attribute Derivation:returning representativeColorCol:"+representativeColorCol);
		return representativeColorCol;
	}
/**
 * This will return the Colorway Object based on the Colurway String passed 
 * @param representativeColorCol
 * @return
 * @throws WTException
 */
public static LCSSKU getSKUFromRepresentativeColor(Collection<FlexObject> representativeColorCol) throws WTException {
	LCSSKU sku = null;
	String repColorId ="";
	Iterator<FlexObject> repColorColIter = representativeColorCol.iterator();
    FlexObject fo = null;
    while(repColorColIter.hasNext()) {
    	fo = (FlexObject)repColorColIter.next();
    	repColorId = SparcConstants.SKU_VR + fo.getString("LCSSKU.BRANCHIDITERATIONINFO");
        sku = (LCSSKU) LCSQuery.findObjectById(repColorId);
    }
    LOGGER.debug("**NONCalculated Attribute Derivation:returning getSKUFromRepresentativeColor:"+sku);
	return sku;
	
}
/**
 * This method is to get the Rep. Colourway which is part of the Form Parameter
 * @param costSheet
 * @param sku
 * @return
 * @throws WTException
 */
public static LCSSKU overrideSKUFromMethodContext(LCSProductCostSheet costSheet, LCSSKU sku) throws WTException {
	
	LCSSKU representativeColorway = null;
	String representativeColorwayStr="";
	
	LOGGER.debug("**NONCalculated Attribute Derivation:"+MethodContext.getContext(Thread.currentThread()));
	if(MethodContext.getContext().get(SPARC_CUSTOMREP_COLORWAY)!=null && FormatHelper.hasContent(String.valueOf(SPARC_CUSTOMREP_COLORWAY))){
			representativeColorwayStr = (String) MethodContext.getContext().get("SPARC_CUSTOMREP_COLORWAY");
			representativeColorway = (LCSSKU) LCSQuery.findObjectById(representativeColorwayStr);
			LOGGER.debug("**NONCalculated Attribute Derivation - Colorway selected or Changed"+representativeColorway+":::"+representativeColorway.getName());
			MethodContext.getContext().remove(SPARC_CUSTOMREP_COLORWAY);
			return representativeColorway;
	}
	
	LOGGER.debug("**NONCalculated Attribute Derivation:Returning Rep Colorway as it is a special scenario:"+sku);
	return sku;
}
	/**
	 * This method takes product, season, Coshsheet as parameters and returns ProductSeasonLink, If the Representative Colorway is selected then it will return SKUSeasonLink
	 * @param product
	 * @param season
	 * @param costSheetObj
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("unchecked")
	public static LCSSeasonProductLink getSeasonProductLink(LCSProduct product, LCSSeason season, LCSProductCostSheet costSheetObj) throws WTException {
		
		boolean isWhatIforActiveCopy =false;
		Collection<FlexObject> representativeColorCol = null;
		LCSSKU originalSKU = null;
		LCSSKU overriddenSKU = null;
		LCSSKU sku = null;
		LCSSeasonProductLink seasonProdLink = null;
		representativeColorCol = getRepresentativeColor(costSheetObj);
		
		if(representativeColorCol != null) {
			originalSKU = getSKUFromRepresentativeColor(representativeColorCol);
			sku = originalSKU;
		}
		LOGGER.debug("**NONCalculated Attribute Derivation:MethodContext Variables:"+MethodContext.getContext(Thread.currentThread()));
		
		if((MethodContext.getContext().get(SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT)==null 
				|| !FormatHelper.hasContent(String.valueOf(SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT)))){
				 
			if(MethodContext.getContext().get(SPARC_REFRESH_MULTI_COSTSHEET)==null 
					|| !FormatHelper.hasContent(String.valueOf(SPARC_REFRESH_MULTI_COSTSHEET))){
				overriddenSKU = overrideSKUFromMethodContext(costSheetObj, sku);
				sku = overriddenSKU;
				if(overriddenSKU !=originalSKU) {
					isWhatIforActiveCopy = true;
				}
			}
			else {
				LOGGER.debug("****NONCalculated Attribute Derivation:Its a Refresh Scenario so continuing with already associated Colorways:");
				MethodContext.getContext().remove(SPARC_REFRESH_MULTI_COSTSHEET);
			}
		}
		else {
			LOGGER.debug("***NONCalculated Attribute Derivation:Either No Colorway Present or Previously adde Colourway removed");
			MethodContext.getContext().remove(SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT);
			sku = null;
			isWhatIforActiveCopy=true;
		}
		
		LOGGER.debug("***NONCalculated Attribute Derivation copiedFrom1:"+costSheetObj.getCopiedFrom()+"::isWhatIforActiveCopy"+isWhatIforActiveCopy);
		LOGGER.debug("***NONCalculated Attribute Derivation getIterationDisplayIdentifier:"+costSheetObj.getIterationDisplayIdentifier());
		Collection<FlexObject> representativeColorColForSRcCS = null;
		if(!isWhatIforActiveCopy && costSheetObj.getCopiedFrom() !=null && (costSheetObj.getIterationDisplayIdentifier()==null || !FormatHelper.hasContent(costSheetObj.getIterationDisplayIdentifier().toString()))) {
			
			LCSProductCostSheet copiedFrom = (LCSProductCostSheet)costSheetObj.getCopiedFrom();
			LOGGER.debug("***NONCalculated Attribute Derivation copiedFrom2:"+copiedFrom);
			representativeColorColForSRcCS = getRepresentativeColor(copiedFrom);
			
			if(representativeColorColForSRcCS != null) {
				overriddenSKU = getSKUFromRepresentativeColor(representativeColorColForSRcCS);
				sku = overriddenSKU;
				LOGGER.debug("***NONCalculated Attribute Derivation SKU from Source CS:"+sku);
			}
			
		}
		
		if(sku == null) {
			seasonProdLink = LCSSeasonQuery.findSeasonProductLink(product, season);
		}
		else {
			seasonProdLink = LCSSeasonQuery.findSeasonProductLink(sku, season);
		}
		if(seasonProdLink != null){
		  LOGGER.debug("***NONCalculated Attribute Derivation:Final seasonProduct Link:"+seasonProdLink+"**TYPE -"+seasonProdLink.getSeasonLinkType());
		}
		return seasonProdLink;
	}

	

	/**
	 * This method sets the Costsheet attributes from Product
	 * @param product
	 * @param costSheetObj
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static void setCSAttributesFromProduct(LCSProduct product, LCSProductCostSheet costSheetObj) throws WTException, WTPropertyVetoException {
		
		Iterator<String> stringIterator = null;
		String tempString="";
		String sourceAttKey="";
		String targetAttKey="";
		Object sourceValue =null;
		
		if(productLevelAttsCol!=null && productLevelAttsCol.size()>0) {
			stringIterator = productLevelAttsCol.iterator();
			while(stringIterator.hasNext()) {
				tempString = stringIterator.next();
				sourceAttKey = MOAHelper.firstValue(tempString);
				targetAttKey = MOAHelper.lastValue(tempString);
				sourceValue = product.getValue(sourceAttKey);
				if(sourceValue!=null && FormatHelper.hasContent(String.valueOf(sourceValue))) {
						sourceValue = getValue(sourceAttKey,product,targetAttKey,costSheetObj);
						costSheetObj.setValue(targetAttKey, sourceValue);
						
						if(sourceAttKey.equalsIgnoreCase(SparcConstants.PRODUCT_BRAND))
							setCSCategoryAttributes(sourceAttKey, product, costSheetObj);
						
				}
				else
					costSheetObj.setValue(targetAttKey, "");
			}
		}
	}
	
	
	/**
	 * 
	 * This method sets the Costsheet attributes from Sourcing Config
	 * @param sourcingConfig
	 * @param costSheetObj
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static void setCSAttributesFromSourcing(LCSSourcingConfig sourcingConfig, LCSProductCostSheet costSheetObj) throws WTException, WTPropertyVetoException {
		
		Iterator<String> stringIterator = null;
		String tempString="";
		String sourceAttKey="";
		String targetAttKey="";
		LCSSupplier tempSupplier= null;
		LCSCountry countryOfOrigin = null;
		
		if(sourcing2ndLevelAttsCol!=null && sourcing2ndLevelAttsCol.size()>0) {
			stringIterator = sourcing2ndLevelAttsCol.iterator();
			while(stringIterator.hasNext()) {
				tempString = stringIterator.next();
				sourceAttKey = MOAHelper.firstValue(tempString);
				targetAttKey = MOAHelper.lastValue(tempString);
				String tempArray[] = sourceAttKey.split("~");
						tempSupplier = (LCSSupplier) sourcingConfig.getValue(tempArray[1]);
						if(tempSupplier!=null) {
							LOGGER.debug("***Source - SourcingConfig 2nd Level - sourceAttValue:"+tempSupplier.getValue(tempArray[2]));
							if(tempSupplier.getValue(tempArray[2])!=null){
								costSheetObj.setValue(targetAttKey, tempSupplier.getValue(tempArray[2]));
								countryOfOrigin = (LCSCountry)tempSupplier.getValue(tempArray[2]);
							}
							else
								costSheetObj.setValue(targetAttKey, "");
							}
						else
							costSheetObj.setValue(targetAttKey, "");
			}
			
			if(null != countryOfOrigin){
				 countryOfOrigin = (LCSCountry)VersionHelper.latestIterationOf(countryOfOrigin);
						LOGGER.debug("countryOfOrigin in setCSAttributesFromSourcing>>>"+countryOfOrigin);	
						AeroCostingIntegrationPlugin.setTariff(costSheetObj);
						LOGGER.debug("scTariffByCountry = "+costSheetObj.getValue("scTariffByCountry"));
						LOGGER.debug("scTariffByCountryDollarUS = "+costSheetObj.getValue("scTariffByCountryDollarUS"));
			}
			
		}
	}
	
	/**
	 * This method sets the Costsheet Category attributes from Product
	 * @param sourceAttKey
	 * @param product
	 * @param costSheetObj
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static void setCSCategoryAttributes(String sourceAttKey, LCSProduct product, LCSProductCostSheet costSheetObj) throws WTException, WTPropertyVetoException {
	
		Object sourceValue = product.getValue(sourceAttKey);
		String productType=product.getFlexType().getFullName(true);
			String strSourceValue = String.valueOf(sourceValue);
			if(strSourceValue.equalsIgnoreCase(SparcConstants.BRAND_ENUM_INTERNALKEY_REEBOK)) {
				if(productType.equalsIgnoreCase(SparcConstants.PRODUCT_APPAREL_REEBOK_PATH)) {
					costSheetObj.setValue(SparcConstants.COSTSHEET_CATEGORY1, SparcConstants.APPAREL_DISPLAYVALUE);
					}
				else if(productType.equalsIgnoreCase(SparcConstants.PRODUCT_FOOTWEAR_REEBOK_PATH)) {
					costSheetObj.setValue(SparcConstants.COSTSHEET_CATEGORY1, SparcConstants.FOOTWEAR_DISPLAYVALUE);
					}
			}
			else if(strSourceValue.equalsIgnoreCase(SparcConstants.BRAND_ENUM_INTERNALKEY_LUCKY)) {
				if(product.getValue(SparcConstants.PRODUCT_CATEGORY)!=null && FormatHelper.hasContent(String.valueOf(product.getValue(SparcConstants.PRODUCT_CATEGORY)))) {
					sourceValue = getValue(SparcConstants.PRODUCT_CATEGORY,product,SparcConstants.COSTSHEET_CATEGORY1,costSheetObj);
					costSheetObj.setValue(SparcConstants.COSTSHEET_CATEGORY1, sourceValue);
				}
				else
					costSheetObj.setValue(SparcConstants.COSTSHEET_CATEGORY1, "");
			}
			else if(strSourceValue.equalsIgnoreCase(SparcConstants.BRAND_ENUM_INTERNALKEY_FOREVER21)) {
				if(product.getValue(SparcConstants.PRODUCT_CATEGORY1)!=null && FormatHelper.hasContent(String.valueOf(product.getValue(SparcConstants.PRODUCT_CATEGORY1)))){
					sourceValue = getValue(SparcConstants.PRODUCT_CATEGORY1,product,SparcConstants.COSTSHEET_CATEGORY1,costSheetObj);
					costSheetObj.setValue(SparcConstants.COSTSHEET_CATEGORY1, sourceValue);
					
				}
				else
					costSheetObj.setValue(SparcConstants.COSTSHEET_CATEGORY1, "");
				
				if(product.getValue(SparcConstants.PRODUCT_CATEGORY2)!=null && FormatHelper.hasContent(String.valueOf(product.getValue(SparcConstants.PRODUCT_CATEGORY2)))) {
					sourceValue = getValue(SparcConstants.PRODUCT_CATEGORY2,product,SparcConstants.COSTSHEET_CATEGORY2,costSheetObj);
					costSheetObj.setValue(SparcConstants.COSTSHEET_CATEGORY2, sourceValue);
				}
				else
					costSheetObj.setValue(SparcConstants.COSTSHEET_CATEGORY2, "");
			}
			//Changes for Task 9896 - start
			else if(strSourceValue.equalsIgnoreCase(SparcConstants.BRAND_ENUM_INTERNALKEY_AEROPOSTALE)) {
				if(product.getValue(SparcConstants.PRODUCT_CATEGORY)!=null && FormatHelper.hasContent(String.valueOf(product.getValue(SparcConstants.PRODUCT_CATEGORY)))) {
					sourceValue = getValue(SparcConstants.PRODUCT_CATEGORY,product,SparcConstants.COSTSHEET_CATEGORY1,costSheetObj);
					costSheetObj.setValue(SparcConstants.COSTSHEET_CATEGORY1, sourceValue);
				}
				else {
					costSheetObj.setValue(SparcConstants.COSTSHEET_CATEGORY1, "");
					}
			//Changes for Task 9896 - end
			}
			//changes for RM Task 10435 - start 
			else if(SparcConstants.BRAND_ENUM_INTERNAL_KEY_NAUTICA.equalsIgnoreCase(strSourceValue)){
				if(product.getValue(SparcConstants.PROD_NAUT_CATEGORY1)!=null && FormatHelper.hasContent(String.valueOf(product.getValue(SparcConstants.PROD_NAUT_CATEGORY1)))) {
					sourceValue = getValue(SparcConstants.PROD_NAUT_CATEGORY1,product,SparcConstants.COSTSHEET_CATEGORY1,costSheetObj);
					costSheetObj.setValue(SparcConstants.COSTSHEET_CATEGORY1, sourceValue);
				}
				else {
					costSheetObj.setValue(SparcConstants.COSTSHEET_CATEGORY1, "");
				}
				if(product.getValue(SparcConstants.PROD_NAUT_DIVISION)!=null && FormatHelper.hasContent(String.valueOf(product.getValue(SparcConstants.PROD_NAUT_DIVISION)))) {
					sourceValue = getValue(SparcConstants.PROD_NAUT_DIVISION,product,SparcConstants.COSTSHEET_CATEGORY2,costSheetObj);
					costSheetObj.setValue(SparcConstants.COSTSHEET_CATEGORY2, sourceValue);
				}
				else
					costSheetObj.setValue(SparcConstants.COSTSHEET_CATEGORY2, "");
			}
			//changes for RM Task 10435 - end 
	}
	
	
	/**
	 * This method returns the appropriate Value to be set on the Target Attribute
	 * @param sorceAttKey
	 * @param sourceType
	 * @param taretAttKey
	 * @param targetType
	 * @return
	 * @throws WTException
	 */
	public static Object getValue(String sorceAttKey,FlexTyped sourceType,String taretAttKey,FlexTyped targetType) throws WTException {
		
		LOGGER.debug("**getValue:"+sorceAttKey+"::"+sourceType+"**"+taretAttKey+"::"+targetType);
		Object sourceValue = "";
		FlexTypeAttribute sourceFlexTypeAttr = sourceType.getFlexType().getAttribute(sorceAttKey);
		LOGGER.debug("**sourceFlexTypeAttr:"+sourceFlexTypeAttr);
		String sourceAttributeVariableType = sourceFlexTypeAttr.getAttVariableType();
		
		FlexTypeAttribute targetFlexTypeAttr = targetType.getFlexType().getAttribute(taretAttKey);
		LOGGER.debug("**targetFlexTypeAttr:"+targetFlexTypeAttr);
		String targetAttributeVariableType = targetFlexTypeAttr.getAttVariableType();
		
		if((sourceAttributeVariableType.equalsIgnoreCase(SparcConstants.ATTRIBUTE_VARIABLETYPE_CHOICE) || 
				sourceAttributeVariableType.equalsIgnoreCase(SparcConstants.ATTRIBUTE_VARIABLETYPE_DRIVEN)) 
				&& (!(targetAttributeVariableType.equalsIgnoreCase(SparcConstants.ATTRIBUTE_VARIABLETYPE_CHOICE) || targetAttributeVariableType.equalsIgnoreCase(SparcConstants.ATTRIBUTE_VARIABLETYPE_DRIVEN)))) {
			sourceValue  = sourceFlexTypeAttr.getDisplayValue(sourceType);
		}
		else if(sourceAttributeVariableType.equalsIgnoreCase(SparcConstants.ATTRIBUTE_VARIABLETYPE_CURRENCY)) {
			sourceValue = sourceType.getValue(sorceAttKey);
		}
		else {
			sourceValue = String.valueOf(sourceType.getValue(sorceAttKey));
		}
		LOGGER.debug("Returning - Value:"+sourceValue);
		return sourceValue;
	}
	
	/**
	 * This method is to set the Default cost sheet attribute values from Product Season during creation of product
	 * @param obj
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	@SuppressWarnings("unchecked")
	public static void updateDefaultCSAttributes(WTObject obj) throws WTException, WTPropertyVetoException {
		
		LOGGER.debug("**updateDefaultCSAttributes:"+obj);
		
		if(!(obj instanceof LCSProductSeasonLink)) {
			return;
		}
		
		LCSProductSeasonLink prodSeasonLink = (LCSProductSeasonLink) obj;
		String prodSeasonRevID="";
		if(prodSeasonLink.getProductSeasonRevId()>0) {
			prodSeasonRevID = String.valueOf(new BigDecimal(prodSeasonLink.getProductSeasonRevId()).longValue());
			}
		LOGGER.debug("**prodSeasonRevID:"+prodSeasonRevID);
		LCSProduct product = (LCSProduct) LCSQuery.findObjectById(SparcConstants.PRODUCT_VR+prodSeasonRevID);
		LOGGER.debug("**product:"+product);
		
		String vesionDisplayIdentifier = product.getVersionDisplayIdentifier().toString();
		int effectSequence = prodSeasonLink.getEffectSequence();
		boolean isEffectLatest = prodSeasonLink.isEffectLatest();
		LOGGER.debug("vesionDisplayIdentifier:"+vesionDisplayIdentifier);
		LOGGER.debug("effectSequence:"+effectSequence);
		LOGGER.debug("isEffectLatest:"+isEffectLatest);
		LCSSourcingConfig srcConfig = LCSSourcingConfigQuery.getPrimarySourceForProduct(product);
		LOGGER.debug("srcConfig:"+srcConfig);
		
		if(vesionDisplayIdentifier.equalsIgnoreCase("B") && effectSequence == 1 && isEffectLatest && srcConfig!=null)  {
			
			LOGGER.debug("***New Product Creation, will be updating the Default cost sheet values***:");

			Collection allCostSheets = LCSCostSheetQuery.getAllCostSheetsForSourcingConfig(srcConfig, false);
			LOGGER.debug("allCostSheets:"+allCostSheets);
			LOGGER.debug("Number of Default Costshees:"+allCostSheets.size());
			Iterator costSheetIterator = allCostSheets.iterator();
			String applicableColorNames="";
			LCSProductCostSheet costSheet = null;
			while (costSheetIterator.hasNext()) {
				LCSCostSheet cs = (LCSCostSheet) costSheetIterator.next();
				LOGGER.debug("cs:"+cs);
				if(cs instanceof LCSProductCostSheet ) {
					costSheet = (LCSProductCostSheet) cs;
				LOGGER.debug("costSheet:"+costSheet);
				applicableColorNames = costSheet.getApplicableColorNames();
				LOGGER.debug("applicableColorNames:"+applicableColorNames);
				if(applicableColorNames == null || !FormatHelper.hasContent(applicableColorNames)) {
					performCostSheetAttrsUpdate(prodSeasonLink,costSheet);
						}
					}
				}
			}
		}
	
	/**
	 * This is helper method to check-out the cost sheet and update the values during roduct creation
	 * @param prodSeasonLink
	 * @param costSheet
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static void performCostSheetAttrsUpdate(LCSProductSeasonLink prodSeasonLink,LCSProductCostSheet costSheet) throws WTException, WTPropertyVetoException{
		
		LOGGER.debug("performCostSheetAttrsUpdate start");
		costSheet = VersionHelper.checkout(costSheet);
		LOGGER.debug("costSheet-checkedout:"+costSheet);
		setCSAttributesFromProductORColorway((LCSSeasonProductLink)prodSeasonLink,costSheet);
		LOGGER.debug("persist started:");
		LCSCostSheetLogic.persist(costSheet, true);
		LOGGER.debug("persist Ended:");
		VersionHelper.checkin(costSheet);
		LOGGER.debug("costSheet-checkedin:"+costSheet);
		LOGGER.debug("performCostSheetAttrsUpdate end");
		
	}
}
