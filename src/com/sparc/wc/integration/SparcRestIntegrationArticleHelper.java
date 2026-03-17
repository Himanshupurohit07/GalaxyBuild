package com.sparc.wc.integration;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.color.LCSColor;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.util.FormatHelper;
import com.sparc.wc.integration.constants.SparcIntegrationConstants;
import com.sparc.wc.integration.pojo.SparcArticlePOJO;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.sparc.wc.util.SparcConstants;
import com.sparc.wc.util.SparcLogger;

import wt.log4j.LogR;

public class SparcRestIntegrationArticleHelper {

	private static final Logger LOGGER = LogR.getLogger(SparcRestIntegrationArticleHelper.class.getName());
	SparcLogger logger = SparcLogger.getInstance();
	String methodName;

	/**
	 * @param prodARev
	 * @param prodSPL
	 * @param season
	 * @param productCostSheet
	 * @param skuARev
	 * @param skuSPL
	 * @return
	 */
	public SparcArticlePOJO getProductSeasonData(LCSProduct prodARev, LCSProductSeasonLink prodSPL, LCSSeason season,
			LCSProductCostSheet productCostSheet, LCSSKU skuARev, LCSSKUSeasonLink skuSPL) throws Exception {

		methodName = "getProductSeasonData";
		logger.logInfo(SparcRestIntegrationArticleHelper.class.getName(), methodName, " Start");
		SparcArticlePOJO articlePOJO = new SparcArticlePOJO();
		SparcRestIntegrationHelper restHelper = new SparcRestIntegrationHelper();
		String colorwayNumber = restHelper.getDisplayValue(skuARev, SparcConstants.SKU_NUMBER);
		logger.logInfo(SparcRestIntegrationArticleHelper.class.getName(), methodName,
				"****** PLM Processing Colorway Number is " + colorwayNumber + "******");
		articlePOJO.setScColorwayNo(colorwayNumber);
		articlePOJO.setScNRFcolorgroup(restHelper.getDisplayValue(skuARev, SparcConstants.SKU_NRF_GROUP));
		articlePOJO.setScNRFcolorcode(restHelper.getDisplayValue(skuARev, SparcConstants.SKU_NRF_CODE));
		articlePOJO.setSclegacyarticle(restHelper.getDisplayValue(skuARev, SparcConstants.SKU_LEGACY_ARTICLE));
		articlePOJO.setSclicensed(restHelper.getDisplayValue(skuARev, SparcConstants.SKU_LICENSED));

		LCSColor color = (LCSColor) LCSQuery.findObjectById("OR:" + skuARev.getValue("color"));
		articlePOJO.setScColorNo(restHelper.getDisplayValue(color, SparcConstants.COLOR_COLOR_NUMBER));
		articlePOJO.setScColorDesc(restHelper.getDisplayValue(color, SparcConstants.COLOR_DESC));
		articlePOJO.setScColorShortDesc(restHelper.getDisplayValue(color, SparcConstants.COLOR_SHORT_DESC));

		String plmProductNo = restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_PRODUCT_NUMBER);
		logger.logInfo(SparcRestIntegrationArticleHelper.class.getName(), methodName,
				"****** PLM Processing Product Number is " + plmProductNo + "******");
		articlePOJO.setScPLMProductNo(plmProductNo);
		articlePOJO.setScBrand(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_BRAND));
		articlePOJO.setScGender(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_GENDER));
		articlePOJO.setScAge(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_AGE));
		articlePOJO
				.setScProductDescriptionShort(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_SHORT_DESC));
		articlePOJO.setScProductDescription(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_DESC));

		if (!prodARev.getFlexType().getFullName().contains("scApparel")) {

			articlePOJO.setScOutsole(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_OUTSOLE));
			articlePOJO.setSclining(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_LINING));
			articlePOJO.setScinlaysole(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_INLAY_SOLE));
			articlePOJO.setScupper(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_UPPER_DESCRIPTION));
			articlePOJO
					.setScoutsoledescription(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_OUTSOLE_DESC));
			articlePOJO.setScproductwidth(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_PRODUCT_WIDTH));
			articlePOJO.setScworkingnumber("");
		} else {
			articlePOJO.setScworkingnumber(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_WORKING_NUMBER));
			articlePOJO.setScOutsole("");
			articlePOJO.setSclining("");
			articlePOJO.setScinlaysole("");
			articlePOJO.setScupper("");
			articlePOJO.setScoutsoledescription("");
			articlePOJO.setScproductwidth("");
		}

		articlePOJO.setScfitsilhoette(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_FIT_SILHOETTE));
		articlePOJO.setScbrandassetpartner(
				restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_BRAND_ASSET_PARTNER));
		articlePOJO.setSclegacymodelnumber(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_LEGACY_MODULE));
		articlePOJO.setSctechnology(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_TECHNOLOGY));
		articlePOJO.setScbenefits(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_BENEFITS));
		articlePOJO.setScfeatures(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_FEATURES));
		articlePOJO.setScSizeRange(restHelper.getDisplayValue(prodARev, SparcConstants.PRODUCT_SIZE_RANGE));

		articlePOJO.setScspecialusage(restHelper.getDisplayValue(skuSPL, SparcConstants.SKU_SEASON_SPECIAL_USAGE));
		articlePOJO.setSccreationtrack(restHelper.getDisplayValue(skuSPL, SparcConstants.SKU_SEASON_CREATION_TRACK));
		articlePOJO.setScTargetRetail(restHelper.getDisplayValue(skuSPL, SparcConstants.SKU_SEASON_TARGET_RETAIL));
		articlePOJO.setScInitialEstimatedUnits(
				restHelper.getDisplayValue(skuSPL, SparcConstants.SKU_SEASON_INITIAL_ESTIMATED_UNITS));
		articlePOJO.setSccolorwaylifecycle(restHelper.getDisplayValue(skuSPL, SparcConstants.SKU_SEASON_LIFECYCLE));
		articlePOJO
				.setScmatconstruction(restHelper.getDisplayValue(skuSPL, SparcConstants.SKU_SEASON_MAT_CONSTRUCTION));
		//////// hypercare 261 issue fix start ////////////
		articlePOJO.setScdistchannel(
				restHelper.getDisplayValue(skuSPL, SparcConstants.PRODUCT_SEASON_DISTRIBUTION_CHANNEL));
		articlePOJO.setScRetailexitdate(
				restHelper.getDisplayValue(skuSPL, SparcConstants.PRODUCT_SEASON_RETAIL_EXIT_DATE));
		articlePOJO.setScRetailintrodate(
				restHelper.getDisplayValue(skuSPL, SparcConstants.PRODUCT_SEASON_RETAIL_INTRO_DATE));
		articlePOJO.setScdevelopmentype(
				restHelper.getDisplayValue(skuSPL, SparcConstants.PRODUCT_SEASON_DEVELOPMENT_TYPE));
		//////// hypercare 261 issue fix End ////////////

		articlePOJO.setScDivision(restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_DIVISION));
		articlePOJO.setScProductGroup(restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_PRODUCT_GROUP));
		articlePOJO.setScProductType(restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_PRODUCT_TYPE));
		articlePOJO
				.setScsportcategory(restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_SPORTS_CATEGORY));
		articlePOJO.setSccorpmarketingline(
				restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_CORP_MARKETING_LINE));
		articlePOJO.setSccatmarketingline(
				restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_CAT_MARKETING_LINE));
		articlePOJO.setScsalesline(restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_SALES_LINE));
		articlePOJO.setSctechnologyconcept(
				restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_TECHNOLOGY_LINE));
		articlePOJO.setScsustainabilityethics(
				restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_SUSTAINABILITY_ETHICS));
		articlePOJO
				.setScsellingregion(restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_SELLING_REGION));
		articlePOJO.setScFranchise(restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_FRANCHISE));

		articlePOJO
				.setScprodmanager(restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_PRODUCT_MANAGER));
		// GM_Task #15085-start
		articlePOJO.setScprodmanager2(restHelper.getDisplayValue(skuSPL, SparcConstants.SKU_SEASON_PRODUCT_MANAGER));
		articlePOJO.setScInitialEstimatedUnits2(
				restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_INITIAL_ESTIMATED_UNITS));
		// GM_Task #15085-End

		// GM_Task #15080-Start
//		final boolean valueSetUsingSource = setHtsCode(productCostSheet, articlePOJO);
//		if (!valueSetUsingSource) {
//			String htsCode = restHelper.getDisplayValue(productCostSheet, SparcConstants.COSTSHEET_HTS_CODE);
//			logger.logDebug(SparcRestIntegrationArticleHelper.class.getName(), methodName, "HTS Code is " + htsCode);
//			if (FormatHelper.hasContent(htsCode) && htsCode.contains(MOAHelper.DELIM)) {
//				articlePOJO.setScHTSCode("999999");
//			} else {
//				articlePOJO
//						.setScHTSCode(restHelper.getDisplayValue(productCostSheet, SparcConstants.COSTSHEET_HTS_CODE));
//			}
//		}
		articlePOJO.setScHTSCode(getHtsCode(productCostSheet, "scHTSCode1"));
		articlePOJO.setScHTSCode2(getHtsCode(productCostSheet, "scHTSCode2"));
		articlePOJO.setScHTSCode3(getHtsCode(productCostSheet, "scHTSCode3"));
		articlePOJO.setScHTSCode4(getHtsCode(productCostSheet, "scHTSCode4"));
		// GM_Task #15080-End
		String seasonType = restHelper.getDisplayValue(season, SparcConstants.SEASON_SEASON_TYPE);
		String seasonYear = restHelper.getDisplayValue(season, SparcConstants.SEASON_YEAR);
		articlePOJO.setSeasonType(seasonType);
		articlePOJO.setYear(seasonYear);
		logger.logInfo(SparcRestIntegrationArticleHelper.class.getName(), methodName,
				"****** PLM Processing Season Type is " + seasonType + "******");
		logger.logInfo(SparcRestIntegrationArticleHelper.class.getName(), methodName,
				"****** PLM Processing Season Year is " + seasonYear + "******");

		setGalaxyAttributeChanges(articlePOJO, restHelper, prodARev, skuARev, skuSPL, prodSPL);
		setBusinessSegmentAttributeChanges(articlePOJO, restHelper, prodSPL);
		logger.logInfo(SparcRestIntegrationArticleHelper.class.getName(), methodName, "End and Pojo is " + articlePOJO);
		return articlePOJO;
	}

	/**
	 * Sets Galaxy-specific attributes on the provided article POJO using data from
	 * Product, SKU, and Cost Sheet revisions, along with helper methods for
	 * attribute extraction.
	 *
	 * @param productCostSheet the cost sheet object containing factory priority
	 * @param articlePOJO      the POJO where Galaxy attributes will be set
	 * @param restHelper       utility helper to extract display values from Flex
	 *                         objects
	 * @param prodARev         the latest revision of the product
	 * @param skuARev          the latest revision of the SKU
	 */
	private void setGalaxyAttributeChanges(final SparcArticlePOJO articlePOJO,
			final SparcRestIntegrationHelper restHelper, final LCSProduct prodARev, final LCSSKU skuARev,
			LCSSKUSeasonLink skuSPL, LCSProductSeasonLink prodSPL) {


		// Set Galaxy Color Code from SKU attributes
		try {
			String galaxyColorCode = restHelper.getDisplayValue(skuARev, SparcConstants.SKU_GALAXY_COLOR_CODE);
			articlePOJO.setGxGalaxyColorCode(galaxyColorCode);
		} catch (Exception e) {
			logAttributeError("SKU Galaxy Color Code", e);
		}

		// Set Global MSRP from SKU Season attributes
		try {
			articlePOJO.setGxGlobalMSRPSKUSeason(
					restHelper.getDisplayValue(skuSPL, SparcConstants.SKU_SEASON_GLOBAL_MSRP));
		} catch (Exception e) {
			logAttributeError("SKU Season Global MSRP", e);
		}

		// Set Global MSRP from Prod Season attributes
		try {
			articlePOJO.setGxGlobalMSRPProdSeason(
					restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_GLOBAL_MSRP));
		} catch (Exception e) {
			logAttributeError("Product Season Global MSRP", e);
		}

	}

	private void setBusinessSegmentAttributeChanges(final SparcArticlePOJO articlePOJO,
			final SparcRestIntegrationHelper restHelper, final LCSProductSeasonLink prodSPL) {

		try {
			articlePOJO.setScBusinessSegment(
					restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_BUSINESS_SEGMENT));
		} catch (Exception e) {
			logAttributeError("Product Season Business Segment", e);
		}

		try {
			articlePOJO.setScBusinessSegmentCode(
					restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_BUSINESS_SEGMENT_CODE));
		} catch (Exception e) {
			logAttributeError("Product Season Business Segment Code", e);
		}

		try {
			articlePOJO
					.setScKeyCategory(restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_KEY_CATEGORY));
		} catch (Exception e) {
			logAttributeError("Product Season Key Category", e);
		}

		try {
			articlePOJO.setScKeyCategoryCode(
					restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_KEY_CATEGORY_CODE));
		} catch (Exception e) {
			logAttributeError("Product Season Key Category Code", e);
		}

		try {
			articlePOJO.setScKeyCategoryCluster(
					restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_KEY_CATEGORY_CLUSTER));
		} catch (Exception e) {
			logAttributeError("Product Season Key Category Cluster", e);
		}

		try {
			articlePOJO.setScKeyCategoryClusterCode(
					restHelper.getDisplayValue(prodSPL, SparcConstants.PRODUCT_SEASON_KEY_CATEGORY_CLUSTER_CODE));
		} catch (Exception e) {
			logAttributeError("Product Season Key Category Cluster Code", e);
		}

	}

	private void logAttributeError(String attributeName, Exception e) {
		logger.logInfo(SparcRestIntegrationArticleHelper.class.getName(), methodName,
				"Failed to set " + attributeName + ": " + ExceptionUtils.getStackTrace(e));
	}

	private String getHtsCode(final LCSProductCostSheet costSheet, String attributeName) {
		String htsCode = "";
		if (costSheet != null) {

			try {

				final LCSSourceToSeasonLink primarySourceToSeasonLink = LCSSourcingConfigQuery
						.getPrimarySourceToSeasonLink(costSheet.getProductMaster(), costSheet.getSeasonMaster());

				final FlexTyped htsBO = (FlexTyped) SparcIntegrationUtil.getValueFrom(primarySourceToSeasonLink,
						attributeName);

				if (htsBO != null) {
					htsCode = (String) SparcIntegrationUtil.getValueFrom(htsBO, "scHTSCodeAssignment");

					if (!FormatHelper.hasContent(htsCode)) {
						LOGGER.error(
								"A valid HTS Business Object is found, but the HTS Code is not defined in the business object, please check the data");
						htsCode = "";
					}
				}
			} catch (Exception e) {
				LOGGER.error("Encountered an error while retrieving primary source to season link, error: "
						+ e.getMessage());
				e.printStackTrace();
			}

		} else {
			LOGGER.error("CostSheet is null, returning empty HTS code.");
		}

		return htsCode;
	}

//	private boolean setHtsCode(final LCSProductCostSheet costSheet, final SparcArticlePOJO articlePOJO) {
//		if (costSheet == null) {
//			return false;
//		}
//
//		try {
//			final LCSSourceToSeasonLink primarySourceToSeasonLink = LCSSourcingConfigQuery
//					.getPrimarySourceToSeasonLink(costSheet.getProductMaster(), costSheet.getSeasonMaster());
//			final FlexTyped htsBO = (FlexTyped) SparcIntegrationUtil.getValueFrom(primarySourceToSeasonLink,
//					"scHTSCode1");
//			if (htsBO == null) {
//				return false;
//			}
//			final Double weight = (double) SparcIntegrationUtil.getValueFrom(primarySourceToSeasonLink,
//					SparcIntegrationConstants.SOURCE_TO_SEASON_LINK_HTS_WEIGHT_INTERNAL_NAME, true);
//			if (weight == null || weight == 0) {
//				articlePOJO.setScHTSCode("NULL");
//				return true;
//			}
//			if (weight != SparcIntegrationConstants.SOURCE_TO_SEASON_LINK_HTS_WEIGHT_THRESHOLD) {
//				articlePOJO.setScHTSCode("999999");
//				return true;
//			}
//			final String htsCode = (String) SparcIntegrationUtil.getValueFrom(htsBO, "scHTSCodeAssignment");
//			if (FormatHelper.hasContent(htsCode)) {
//				articlePOJO.setScHTSCode(htsCode);
//			} else {
//				LOGGER.error(
//						"A valid HTS Business Object is found, but the HTS Code is not defined in the business object, please check the data");
//				articlePOJO.setScHTSCode("");
//			}
//			return true;
//		} catch (Exception e) {
//			LOGGER.error(
//					"Encountered an error while retrieving primary source to season link, error:" + e.getMessage());
//		}
//		return false;
//
//	}

}
