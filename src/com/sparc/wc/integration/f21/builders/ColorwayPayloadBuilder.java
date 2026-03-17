package com.sparc.wc.integration.f21.builders;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lcs.wc.color.LCSColor;
import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.LCSSKUSourcingLink;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.supplier.LCSSupplier;
import com.sparc.wc.integration.f21.domain.SCMColorwayPayload;
import com.sparc.wc.integration.f21.domain.SCMColorwayPayload.DataContent.ColorwayDataContent;
import com.sparc.wc.integration.f21.repository.SCMColorwayRepository;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.lcs.wc.db.FlexObject;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_MATCHING_SET_NONE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_MATCHING_SET_YES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_MATCHING_SET_NO;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_PRODUCT_UPDATE_FLAG_NEW;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_PRODUCT_UPDATE_FLAG_UPDATE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_YEAR_ROUND_FLAG;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_LAUNCH_DATE_FORMAT;
import static com.sparc.wc.integration.f21.repository.SCMColorwayRepository.lookupEnumDisplayValue;
import static com.sparc.wc.integration.f21.repository.SCMColorwayRepository.lookupEnumSecondaryKeyValue;

/**
 * Builds a Colorway payload.<br>
 * NOTE: As part of building the payload, this builder will also update the 
 * corresponding sourcing configuration flags for 'Sent To SCM' (to 'Yes') & 'Product Update Flag' (to false).<br>
 * FIXES/AMENDMENTS:<br>
 * - Deployment fixes: Merchant & Design Lead emails fixed.<br>
 * - Bug #8655: Wrong Category 1 & Category 2 values. Replaced use of util's method getValueFrom with new lookupSecondarykeyValue.<br>
 * - Bug #8660: Fix for Development Season value not updated according Year Round value.<br>
 * - Changed Style 1 & Style 2 to use Enum's Display Name instead of secondary key.<br>
 * - Minor update: Added description to method: lookupEnumDisplayValue.<br>
 * - Changed Product Update Flag logic per request from customer (Vinod):<br> 
 * --- If checkbox values is "Yes", then JSON output expedted is "UPDATE".<br>
 * --- If "No" or null (not set), then output expected is "NEW".<br>
 * - Moved attribute value lookup methods to repository class as part of UAT tasks #8846 & #8847.<br>
 * - Task #8845 (UAT): Pull Country Of Origin ISO Code from Finished Goods Factory @ Sourcing Config.<br>
 * - Task #8873 (UAT): Include colorways for all valid seasons the product is associated with.
 * - Task #8942 (UAT): Relocate product update flag to sourcing-colorway-season. Refactored build logic for colorway attributes.
 * - Task #9090 (UAT): Reworked and relocated the logic for season's year round evaluation/value override of the Development Season attribute, so that
 * it is no longer dependent to the presence of active colorways for a season. Added method buildProductSeasonAttributes.
 * - Task #9201 (UAT): Add outbound attribute 'Legacy SCM Item Number' to the payload.
 * 
 * @author Acnovate
 */
public class ColorwayPayloadBuilder {
	
	private Long sourcingConfigNumber;
	private SCMColorwayPayload colorway = null;
	
	/**
	 * Constructs a ColorwayPayloadBuilder instance.
	 * @param colorway The colorway payload to build.
	 */
	public ColorwayPayloadBuilder(SCMColorwayPayload colorway) {
		this.colorway = colorway;
	}
	
	public ColorwayPayloadBuilder setSourcingConfigNumber(Long sourcingConfigNumber) {
		this.sourcingConfigNumber = sourcingConfigNumber;
		return this;
	}
	
	/**
	 * Builds the season attributes of the payload.
	 * @param flexSeason The season instance to extract the required attribute values from.
	 * @throws WTException When an error occurs while fecthing season data at PLM.
	 */
	private void buildSeasonAttributes(LCSSeason flexSeason) throws WTException {
		
		if (flexSeason == null) {
			return;
		}
		
		//Set year round attribute into the payload ONLY if the attribute wasn't set it before (see buildProductSeasonAttributes for more info).
		if (!F21_SCM_YEAR_ROUND_FLAG.equalsIgnoreCase(colorway.getData().getDevelopmentSeason())) {
			colorway.getData().setDevelopmentSeason(lookupEnumSecondaryKeyValue(flexSeason, "seasonType"));
		}
		
	}
	
	/**
	 * Builds the product season related attributes.<br>
	 * @param flexProduct The product to link with the season.
	 * @param flexSeasonsList The product's seasons.
	 */
	private void buildProductSeasonAttributes(LCSProduct flexProduct, List<LCSSeason> flexSeasonsList) throws WTException {
		
		if (flexProduct == null || flexSeasonsList == null || flexSeasonsList.isEmpty()) {
			return;
		}
		
		//Check if year round season is enabled for any of the product-seasons, and if so update the Development Season attribute in the payload.
		//If no season has year round flag enabled, then the Development Season attribute in the payload won't be assigned to a value in this method. 
		//(see also buildSeasonAttributes for more info)
		for (LCSSeason flexSeason : flexSeasonsList) {
			
			LCSSeasonProductLink flexProdSeasonLink = LCSSeasonQuery.findSeasonProductLink(flexProduct, flexSeason);
			
			if (flexProdSeasonLink != null) {
				
				Object scYearRound = flexProdSeasonLink.getValue("scYearRound");
				
				if (scYearRound != null	&& (scYearRound instanceof Boolean)	&& (Boolean)scYearRound) {
					colorway.getData().setDevelopmentSeason(F21_SCM_YEAR_ROUND_FLAG);
					break;
				}
				
			}
			
		}//end for loop.	
	}
	
	/**
	 * Builds the product attributes of the payload.
	 * @param flexSrcConfig The sourcing configuration needed to fetch the associated product data.
	 * @return The product (if found), else <code>null</code>.
	 * @throws WTException When an error occurs while fecthing product data at PLM. 
	 * @throws Exception When validation error occurs, i.e. no sourcing configuration is provided.
	 */
	private LCSProduct buildProductAttributes(LCSSourcingConfig flexSrcConfig) throws WTException, Exception {
		
		LCSProduct flexProduct = SCMColorwayRepository.getProduct(flexSrcConfig);
		
		if (flexProduct == null) {
			throw new Exception("No product could be found in the system associated to the Sourcing Configuration with number '" + sourcingConfigNumber + "'.");
		}
		
		colorway.getData().setPlmProductNumber(Long.parseLong(String.valueOf(SparcIntegrationUtil.getValueFrom(flexProduct, "scPLMProductNo", false))));
		colorway.getData().setProductDescription((String)SparcIntegrationUtil.getValueFrom(flexProduct, "scProductDescription", false));
		colorway.getData().setProductShortDescription((String)SparcIntegrationUtil.getValueFrom(flexProduct, "scProductDescriptionShort", false));
		colorway.getData().setDivision(lookupEnumSecondaryKeyValue(flexProduct, "scF21Division"));
		colorway.getData().setDivisionDept(lookupEnumSecondaryKeyValue(flexProduct, "scF21DivisionDept"));
		colorway.getData().setCategory1(lookupEnumSecondaryKeyValue(flexProduct, "scF21Category1"));
		colorway.getData().setCategory2(lookupEnumSecondaryKeyValue(flexProduct, "scF21Category2"));
		colorway.getData().setStyle1(lookupEnumDisplayValue(flexProduct, "scF21Style1"));
		colorway.getData().setStyle2(lookupEnumDisplayValue(flexProduct, "scF21Style2"));
		colorway.getData().setPlacement(lookupEnumSecondaryKeyValue(flexProduct, "scPlacement"));
		colorway.getData().setTechnique(lookupEnumSecondaryKeyValue(flexProduct, "scTechnique"));
		colorway.getData().setDetail(lookupEnumSecondaryKeyValue(flexProduct, "scDetail"));
		colorway.getData().setNeckShape(lookupEnumSecondaryKeyValue(flexProduct, "scNeckShape"));
		
		Object scMatchingSet = SparcIntegrationUtil.getValueFrom(flexProduct, "scMatchingSet", false);
		
		if (scMatchingSet != null) {
			colorway.getData().setMatchingSet(((Boolean)scMatchingSet) ? F21_SCM_MATCHING_SET_YES : F21_SCM_MATCHING_SET_NO);
		} else {
			colorway.getData().setMatchingSet(F21_SCM_MATCHING_SET_NONE);
		}
		
		LCSLifecycleManaged flexVendorBrand = (LCSLifecycleManaged)SparcIntegrationUtil.getValueFrom(flexProduct, "scVendorBrand");
		
		if (flexVendorBrand != null) {
			colorway.getData().setVendorBrand((String)SparcIntegrationUtil.getValueFrom(flexVendorBrand, "scSecondaryKey", false));
		}
		
		colorway.getData().setTrendGroup(lookupEnumSecondaryKeyValue(flexProduct, "scTrendGroup"));
		colorway.getData().setSeasonalGroup(lookupEnumSecondaryKeyValue(flexProduct, "scSeasonalGroup"));
		colorway.getData().setStyleIntent(lookupEnumSecondaryKeyValue(flexProduct, "scStyleIntent"));
		colorway.getData().setLogoType(lookupEnumSecondaryKeyValue(flexProduct, "scLogoType"));
		colorway.getData().setLogoSubType(lookupEnumSecondaryKeyValue(flexProduct, "scLogoSubType"));
		colorway.getData().setFashion(lookupEnumSecondaryKeyValue(flexProduct, "scFashion"));
		colorway.getData().setDesignGroup(lookupEnumSecondaryKeyValue(flexProduct, "scDesignGroup"));
		
		LCSLifecycleManaged flexItemGroupCode = (LCSLifecycleManaged)SparcIntegrationUtil.getValueFrom(flexProduct, "scItemGroupCode");
		
		if (flexItemGroupCode != null) {
			colorway.getData().setItemGroupCode((String)SparcIntegrationUtil.getValueFrom(flexItemGroupCode, "scSecondaryKey", false));
		}
		
		colorway.getData().setFinish(lookupEnumSecondaryKeyValue(flexProduct, "scFinish"));
		colorway.getData().setPatternName(lookupEnumSecondaryKeyValue(flexProduct, "scPatternName"));
		colorway.getData().setDevelopmentPath(lookupEnumSecondaryKeyValue(flexProduct, "scDevelopmentPath"));
		
		LCSCountry flexFabricCountryOfOrigin = (LCSCountry)SparcIntegrationUtil.getValueFrom(flexProduct, "scFabricCOO");
		
		if (flexFabricCountryOfOrigin != null) {
			colorway.getData().setFabricCountryOfOriginISO((String)SparcIntegrationUtil.getValueFrom(flexFabricCountryOfOrigin, "scISOCode", false));
		}
		
		colorway.getData().setPriceGroup(lookupEnumSecondaryKeyValue(flexProduct, "scPriceGroup"));
		
		Object scLaunchDate = SparcIntegrationUtil.getValueFrom(flexProduct, "scLaunchDate", false);
		
		if (scLaunchDate != null) {
			colorway.getData().setLaunchDate(new SimpleDateFormat(F21_SCM_LAUNCH_DATE_FORMAT).format((Date)scLaunchDate));
		}
		
		Object merchObj = SparcIntegrationUtil.getValueFrom(flexProduct, "scMerchant");
		
		if (merchObj != null) {
			FlexObject merchFlexObject=(FlexObject)merchObj;
			colorway.getData().setMerchant((String)merchFlexObject.getData("EMAIL"));
		}
		
		Object designObj = SparcIntegrationUtil.getValueFrom(flexProduct, "scDesignLead");
		
		if (designObj != null) {
			FlexObject designFlexObject = (FlexObject)designObj;
			colorway.getData().setDesignLead((String)designFlexObject.getData("EMAIL"));
		}
		
		return flexProduct;
	}
	
	/**
	 * Builds the Sourcing Configuration related attributes.
	 * @return The source configuration instance related to the Sourcing Configuraton # used for this builder.
	 * @throws WTException If an error occurs while accessing/retrieving the attributes from PLM.
	 * @throws Exception If the Sourcing Configuration # doesn't exist within PLM.
	 */
	private LCSSourcingConfig buildSourcingConfigAttributes() throws WTException, Exception {
		
		LCSSourcingConfig flexSrcConfig = SCMColorwayRepository.getSourcingConfiguration(sourcingConfigNumber);
		
		if (flexSrcConfig == null) {
			throw new Exception("The Sourcing Configuration with number '" + sourcingConfigNumber + "' couldn't be found in the system.");
		}
		
		colorway.getData().setSourcingConfigNumber(sourcingConfigNumber);
		
		colorway.getData().setVendorStyleID((String)SparcIntegrationUtil.getValueFrom(flexSrcConfig, "scVendorStyleID", false));
		
		LCSSupplier flexVendor = (LCSSupplier)SparcIntegrationUtil.getValueFrom(flexSrcConfig, "vendor");
		
		if (flexVendor != null) {
			colorway.getData().setVendorSCMNumber((String)SparcIntegrationUtil.getValueFrom(flexVendor, "scSCMNumberF21", false));
		}
		
		LCSSupplier flexFGFactory = (LCSSupplier)SparcIntegrationUtil.getValueFrom(flexSrcConfig, "scFGFactory");
		
		if (flexFGFactory != null) {
			
			colorway.getData().setFinishedGoodsFactorySCMNumber((String)SparcIntegrationUtil.getValueFrom(flexFGFactory, "scSCMNumberF21", false));
			
			LCSCountry flexFGFactoryCountry = (LCSCountry)SparcIntegrationUtil.getValueFrom(flexFGFactory, "scCountry");
			
			if (flexFGFactoryCountry != null) {
				colorway.getData().setCountryOfOriginISO((String)SparcIntegrationUtil.getValueFrom(flexFGFactoryCountry, "scISOCode", false));
			}
			
		}
		
		colorway.getData().setLegacySCMItemNumber((String)SparcIntegrationUtil.getValueFrom(flexSrcConfig, "scLegacySCMItemNumber", false));
		
		return flexSrcConfig;
	}
	
	/**
	 * Builds the Source To Season Link related attributes.
	 * @return The Source To Season Link instance related to the Sourcing Configuraton # used for this builder.
	 * @throws WTException When an error occurs while accessing/retrieving the attributes from PLM.
	 */
	private LCSSourceToSeasonLink buildSourceToSeasonLinkAttributes() throws WTException {
		
		LCSSourceToSeasonLink flexSourceToSeason = SCMColorwayRepository.getSourceToSeasonLink(sourcingConfigNumber);
		
		if (flexSourceToSeason == null) {
			return flexSourceToSeason;
		}
		
		Object sourcingStatus = SparcIntegrationUtil.getValueFrom(flexSourceToSeason, "scSourcingStatus", false);
		
		if (sourcingStatus != null) {
			colorway.getData().setSourcingStatus((String)sourcingStatus);
		}
		
		return flexSourceToSeason;
	}
	
	/**
	 * Builds the colorway related attributes.
	 * @param flexSrcConfig The sourcing cofiguration needed to verify whether the colorway is actively used or not.
	 * @param flexProduct The product needed to get the list colorways associated to it.
	 * @param flexSeasonsList The list of seasons related to the product to use for the colorway extraction.
	 * @return The list of sourcing-colorways recently created (marked as NEW) if any.
	 * @throws WTException When an error occurs while accessing/retrieving the colorway attributes from PLM.
	 */
	private List<LCSSKUSourcingLink> buildColorwayAttributes(LCSSourcingConfig flexSrcConfig, LCSProduct flexProduct, List<LCSSeason> flexSeasonsList) throws WTException {
		
		List<LCSSKUSourcingLink> newSKUSourcingLinksList = new ArrayList<LCSSKUSourcingLink>();
		
		if (flexSrcConfig == null || flexProduct == null || flexSeasonsList == null || flexSeasonsList.isEmpty()) {
			return newSKUSourcingLinksList;
		}
				
		Map<LCSSeason, Set<LCSSKU>> activeSeasonColorwaysMap = SCMColorwayRepository.getActiveSourcingSeasonColorways(
				flexProduct, 
				flexSrcConfig, 
				flexSeasonsList);
		
		if (activeSeasonColorwaysMap.isEmpty()) {
			return newSKUSourcingLinksList;
		}
		
		for (LCSSeason flexColorwaySeason : activeSeasonColorwaysMap.keySet()) {
			
			Set<LCSSKU> colorwaysSet = activeSeasonColorwaysMap.get(flexColorwaySeason);
			
			for (LCSSKU flexColorway : colorwaysSet) {
				
				ColorwayDataContent colorwayPayload = colorway.getData().newColorwayDataContentInstance();
				colorway.getData().getColorways().add(colorwayPayload);
				
				colorwayPayload.setColorwayNumber(String.valueOf(SparcIntegrationUtil.getValueFrom(flexColorway, "scColorwayNo", false)));
				
				LCSColor color = (LCSColor)SparcIntegrationUtil.getValueFrom(flexColorway, "color");
				
				if (color != null) {
					colorwayPayload.setColorDetail((String)SparcIntegrationUtil.getValueFrom(color, "scColorDetail", false));
				}
				
				colorwayPayload.setColorwaySeason(flexColorwaySeason.getName());
				
				LCSSeasonProductLink flexSeasonProdLink = LCSSeasonQuery.findSeasonProductLink(flexColorway, flexColorwaySeason);
				
				if (flexSeasonProdLink != null) {
					colorwayPayload.setPlmColorwayStatus(lookupEnumDisplayValue(flexSeasonProdLink, "scPLMPolorwayStatus"));
				}
				
				LCSSKUSourcingLink skuSourceLink = new LCSSourcingConfigQuery().getSKUSourcingLink(flexSrcConfig, flexColorway, flexColorwaySeason);
				
				if (skuSourceLink != null) {
					
					Object productUpdateFlag = SparcIntegrationUtil.getValueFrom(skuSourceLink, "scProductUpdateFlag", false);
					Boolean isUpdate = (productUpdateFlag !=null && (Boolean)productUpdateFlag);
					
					colorwayPayload.setProductUpdateFlag(isUpdate ? F21_SCM_PRODUCT_UPDATE_FLAG_UPDATE : F21_SCM_PRODUCT_UPDATE_FLAG_NEW);
					
					if (!isUpdate) {
						newSKUSourcingLinksList.add(skuSourceLink);
					}
					
				}
				
			}//end flexColorway for loop.
			
		}//end flexColorwaySeason for loop.
		
		//Override year round flag 
		
		
		return newSKUSourcingLinksList;
	}
	
	/**
	 * Builds the Colorway Payload from PLM.
	 * @return The Colorway Payload.
	 * @throws WTException When an error occurs while accessing or retrieving the various data elements from PLM.
	 * @ throws WTPropertyVetoException When an error occurs while updating the SCM control flags at PLM.
	 * @throws Exception When a different error, i.e. validation fail, occurs.
	 */
	public SCMColorwayPayload build() throws WTException, WTPropertyVetoException, Exception {
		
		LCSSourcingConfig flexSrcConfig = buildSourcingConfigAttributes();
		buildSourceToSeasonLinkAttributes();
		
		LCSProduct flexProduct = buildProductAttributes(flexSrcConfig);
		
		List<LCSSeason> prodSeasonsList = SCMColorwayRepository.getValidSeasonsForProduct(flexProduct);
		
		buildProductSeasonAttributes(flexProduct, prodSeasonsList);
		
		LCSSeason latestSeason = SCMColorwayRepository.getLatestSeason(prodSeasonsList);
		buildSeasonAttributes(latestSeason);
		
		List<LCSSKUSourcingLink> flexSKUSourcingList = buildColorwayAttributes(flexSrcConfig, flexProduct, prodSeasonsList);
		
		SCMColorwayRepository.updateSCMControlFlags(flexSrcConfig, flexSKUSourcingList);
		
		return colorway;		
	}
	
}
