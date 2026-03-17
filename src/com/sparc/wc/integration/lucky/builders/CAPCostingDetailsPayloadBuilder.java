package com.sparc.wc.integration.lucky.builders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.*;
import static com.sparc.wc.integration.lucky.repository.LuckyCostingRepository.lookupEnumDisplayValue;
import static com.sparc.wc.integration.lucky.repository.LuckyCostingRepository.lookupEnumSecondaryKeyValue;

import java.util.ArrayList;
import java.util.List;

import com.lcs.wc.color.LCSColor;
import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.supplier.LCSSupplier;
import com.sparc.wc.integration.aero.domain.AeroProcessesParam;
import com.sparc.wc.integration.aero.repository.AeroCostingRepository;
import com.sparc.wc.integration.lucky.domain.CAPCostingDetailsPayload;
import com.sparc.wc.integration.lucky.domain.CAPCostingDetailsPayload.DataContent.CostSheetDataContent;
import com.sparc.wc.integration.lucky.domain.LuckyCostingColorwayCostSheetMaster;
import com.sparc.wc.integration.lucky.domain.LuckyCostingProcessesParam;
import com.sparc.wc.integration.lucky.repository.LuckyCostingRepository;
import com.sparc.wc.integration.util.SparcIntegrationUtil;

import org.apache.logging.log4j.Logger;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * Builds a Lucky/CAP Costing Details payload.<br>
 * FIXES/AMENDMENTS:<br>
 * - Task #9233 (UAT): Fixed colorway's color number value in payload. <br>
 * - Task #9234 (UAT): change scSAPVendorNameLucky to scFinishedGoodsFactoryName.<br>
 * - Task #9238 (UAT): Remove sclegacyarticle from JSON.<br>
 * - Task #9388 (UAT): Change the source of the JSON scFullCircleVendorName to the Vendor Library OOTB 'name' PLM attribute.
 *
 * @author Acnovate
 * @see "Task #8259 3.1 Costing API - FC & CAP (Outbound)"
 */
public class CAPCostingDetailsPayloadBuilder {


	private static final Logger LOGGER = LogR.getLogger(LUCKY_COSTING_LOGGER_NAME);

	private Long sourcingConfigNumber;
	private CAPCostingDetailsPayload capCostingDetails;

	/**
	 * Constructs a CAPCostingDetailsPayloadBuilder instance.
	 * @param capCostingDetails The CAP Costing Details to build by this builder.
	 */
	public CAPCostingDetailsPayloadBuilder(CAPCostingDetailsPayload capCostingDetails) {
		this.capCostingDetails = capCostingDetails;
	}

	public CAPCostingDetailsPayloadBuilder setSourcingConfigNumber(Long sourcingConfigNumber) {
		this.sourcingConfigNumber = sourcingConfigNumber;
		return this;
	}

	/**
	 * Builds the Sourcing Configuration related attributes for the payload.
	 * @param costSheetDataPayload The cost sheet data payload to load the attribute values into.
	 * @param flexSourcingConfig The Sourcing Configuration to extract the attribute values from.
	 * @param flexCostSheet The Product Cost Sheet to extract the value for incoterms, which is then needed to identify the CAP Vendor Number.
	 * @throws WTException If an error occurs while accessing/retrieving the Sourcing Config, Product Cost Sheet or Vendor attributes from PLM.
	 */
	private void buildSourcingConfigAttributes(CostSheetDataContent costSheetDataPayload,
											   LCSSourcingConfig flexSourcingConfig,
											   LCSProductCostSheet flexCostSheet) throws WTException {

		if (costSheetDataPayload == null || flexSourcingConfig == null) {
			return;
		}

		String costSheetIncoterms = (String)SparcIntegrationUtil.getValueFrom(flexCostSheet, LUCKY_COSTING_COST_SHEET_INCOTERMS_ATTR);
		LCSSupplier flexVendor = (LCSSupplier)SparcIntegrationUtil.getValueFrom(flexSourcingConfig, LUCKY_COSTING_SOURCING_VENDOR_ATTR);

		if (flexVendor != null) {
			costSheetDataPayload.setScFullCircleVendorNo(LuckyCostingRepository.resolveFullCircleVendorNumber(costSheetIncoterms, flexVendor));
			costSheetDataPayload.setScFullCircleVendorName((String)SparcIntegrationUtil.getValueFrom(flexVendor, FULL_CIRCLE_VENDOR_NAME));
		}

		LCSSupplier flexFGFactory = (LCSSupplier)SparcIntegrationUtil.getValueFrom(flexSourcingConfig, LUCKY_COSTING_SOURCING_FG_FACTORY_ATTR);

		if (flexFGFactory != null) {
			costSheetDataPayload.setScLuckyFactoryID(
					(String)SparcIntegrationUtil.getValueFrom(flexFGFactory, LUCKY_COSTING_VENDOR_FC_VENDOR_NO_LUCKY_ATTR));
			costSheetDataPayload.setScFinishedGoodsFactoryName(
					(String)SparcIntegrationUtil.getValueFrom(flexFGFactory, LUCKY_COSTING_VENDOR_FG_FACTORY_NAME_ATTR));
		}

	}

	/**
	 * Builds the Sourcing To Season Link related attributes for the payload.
	 * @param costSheetDataPayload The cost sheet data payload to load the attribute values into.
	 * @param flexSourcingSeasonLink The Source To Season Link to extract the attribute values from.
	 * @throws WTException If an error occurs while accessing/retrieving the Source To Season Link or HTS Assignment Table attributes from PLM.
	 */
	private void buildSourcingSeasonAttributes(CostSheetDataContent costSheetDataPayload,
											   LCSSourceToSeasonLink flexSourcingSeasonLink) throws WTException {

		if (costSheetDataPayload == null || flexSourcingSeasonLink == null) {
			return;
		}

		costSheetDataPayload.setScHTSCode(LuckyCostingRepository.resolveHTSCode1(flexSourcingSeasonLink));

	}

	/**
	 * Builds the Season related attributes for the payload.
	 * @param costSheetDataPayload The cost sheet data payload to load the attribute values into.
	 * @param flexSeason The Season to extract the attribute values from.
	 * @throws WTException If an error occurs while accessing/retrieving the Season attributes from PLM.
	 */
	private void buildSeasonAttributes(CostSheetDataContent costSheetDataPayload, LCSSeason flexSeason) throws WTException {

		if (costSheetDataPayload == null || flexSeason == null) {
			return;
		}

		costSheetDataPayload.setSeasonType(lookupEnumSecondaryKeyValue(flexSeason, LUCKY_COSTING_SEASON_DEVELOPMENT_SEASON_ATTR));
		costSheetDataPayload.setYear(lookupEnumSecondaryKeyValue(flexSeason, LUCKY_COSTING_SEASON_DEVELOPMENT_YEAR_ATTR));
	}

	/**
	 * Builds the Product related attributes for the payload.
	 * @param costSheetDataPayload The cost sheet data payload to load the attribute values into.
	 * @param flexProduct The Product to extract the attribute values from.
	 * @throws WTException If an error occurs while accessing/retrieving the Product attributes from PLM.
	 */
	private void buildProductAttributes(CostSheetDataContent costSheetDataPayload, LCSProduct flexProduct) throws WTException {

		if (costSheetDataPayload == null || flexProduct == null) {
			return;
		}

		Object productNo = SparcIntegrationUtil.getValueFrom(flexProduct, LUCKY_COSTING_PRODUCT_NO_ATTR);

		if (productNo != null) {
			costSheetDataPayload.setScPLMProductNo(String.valueOf((Long)productNo));
		}
		costSheetDataPayload.setSclSTY_NUM((String)SparcIntegrationUtil.getValueFrom(flexProduct, LUCKY_COSTING_PRODUCT_LEGACY_STYLE_NUMBER_ATTR));
	}

	/**
	 * Builds the Colorway related attributes for the payload.
	 * @param costSheetDataPayload The cost sheet data payload to load the attribute values into.
	 * @param flexColorway The Colorway to extract the attribute values from.
	 * @throws WTException If an error occurs while accessing/retrieving the Colorway or Color attributes from PLM.
	 */
	private void buildColorwayAttributes(CostSheetDataContent costSheetDataPayload, LCSSKU flexColorway) throws WTException {

		if (costSheetDataPayload == null || flexColorway == null) {
			return;
		}

		LCSColor flexColor = (LCSColor)SparcIntegrationUtil.getValueFrom(flexColorway, LUCKY_COSTING_COLORWAY_COLOR_ATTR);

		if (flexColor != null) {

			Object colorNo = SparcIntegrationUtil.getValueFrom(flexColor, LUCKY_COSTING_COLOR_NO_ATTR);

			if (colorNo != null) {
				costSheetDataPayload.setScColorNo(Long.toString((Long)colorNo));
			}

		}

		Object colorwayNo = SparcIntegrationUtil.getValueFrom(flexColorway, LUCKY_COSTING_COLORWAY_NUMBER_ATTR);

		if (colorwayNo != null) {
			costSheetDataPayload.setScColorwayNo(SparcIntegrationUtil.addPadding(String.valueOf((Long)colorwayNo)));
		}

	}

	/**
	 * Builds the Cost Sheet related attributes for the payload.
	 * @param costSheetDataPayload The cost sheet data payload to load the attribute values into.
	 * @param flexCostSheet The Product Cost Sheet to extract the attribute values from.
	 * @throws WTException If an error occurs while accessing/retrieving the Product Cost Sheet attributes from PLM.
	 */
	private void buildCostSheetAttributes(CostSheetDataContent costSheetDataPayload, LCSProductCostSheet flexCostSheet) throws WTException {

		if (costSheetDataPayload == null || flexCostSheet == null) {
			return;
		}

		costSheetDataPayload.setScCostSheetNumber(String.valueOf(SparcIntegrationUtil.getValueFrom(flexCostSheet, LUCKY_COSTING_COST_SHEET_NUMBER_ATTR)));
		costSheetDataPayload.setScCostSheetName((String)SparcIntegrationUtil.getValueFrom(flexCostSheet, LUCKY_COSTING_COST_SHEET_NAME_ATTR));
		costSheetDataPayload.setScIncoterms(lookupEnumDisplayValue(flexCostSheet, LUCKY_COSTING_COST_SHEET_INCOTERMS_ATTR));
		costSheetDataPayload.setScCurrencyCode(LUCKY_COSTING_COST_SHEET_CURRENCY_CODE_VALUE);


		LCSCountry flexCountryOfOrigin = (LCSCountry)SparcIntegrationUtil.getValueFrom(flexCostSheet, LUCKY_COSTING_COST_SHEET_COUNTRY_OF_ORIGIN_ATTR);

		if (flexCountryOfOrigin != null) {
			costSheetDataPayload.setScCountryofOrigin(
					(String)SparcIntegrationUtil.getValueFrom(flexCountryOfOrigin, LUCKY_COSTING_COUNTRY_ISO_CODE_ATTR));
		}

		Double vendorTotalCost = (Double)SparcIntegrationUtil.getValueFrom(flexCostSheet, LUCKY_COSTING_COST_SHEET_VENDOR_TOTAL_COST_ATTR);

		if (vendorTotalCost != null) {
			costSheetDataPayload.setScTotalVendorCost(Double.toString(vendorTotalCost));
		}

		Double landedCostUS = (Double)SparcIntegrationUtil.getValueFrom(flexCostSheet, LUCKY_COSTING_COST_SHEET_LANDED_COST_ATTR);

		if (landedCostUS != null) {
			costSheetDataPayload.setScLandedCostUSELC(Double.toString(landedCostUS));
			costSheetDataPayload.setScLandedCostUSWCCost(Double.toString(landedCostUS));
		}

		costSheetDataPayload.setScShipMode(lookupEnumDisplayValue(flexCostSheet, LUCKY_COSTING_COST_SHEET_SHIP_MODE_ATTR));
		costSheetDataPayload.setScMilestoneStatus(lookupEnumDisplayValue(flexCostSheet, LUCKY_COSTING_COST_SHEET_MILESTONE_ATTR));
		costSheetDataPayload.setScCostSheetStatus(lookupEnumDisplayValue(flexCostSheet, LUCKY_COSTING_COST_SHEET_STATUS_ATTR));
		//Lucky Enhancement - Start
		Double vendorProdCost = (Double)SparcIntegrationUtil.getValueFrom(flexCostSheet, LUCKY_COSTING_COST_SHEET_VENDOR_PROD_COST_ATTR);
		if(vendorProdCost != null) {
			costSheetDataPayload.setScVendorProductCost(Double.toString(vendorProdCost));
		}
		Double otherVendorCosts = (Double)SparcIntegrationUtil.getValueFrom(flexCostSheet, LUCKY_COSTING_COST_SHEET_OTHER_VENDOR_COSTS_ATTR);

		if (otherVendorCosts != null) {
			costSheetDataPayload.setScUnitsOtherCosts(Double.toString(otherVendorCosts));
		}
		Double agentCommissionPercentUS = (Double)SparcIntegrationUtil.getValueFrom(flexCostSheet, LUCKY_COSTING_COST_SHEET_AGENT_COMM_PERCENT_ATTR);

		if (agentCommissionPercentUS != null) {
			costSheetDataPayload.setScAgentCommissionPercentUS(Double.toString(agentCommissionPercentUS));
		}

		Double agentCommissionDollarsUS = (Double)SparcIntegrationUtil.getValueFrom(flexCostSheet, LUCKY_COSTING_COST_SHEET_AGENT_COMM_DOLLARS_ATTR);

		if (agentCommissionDollarsUS != null) {
			costSheetDataPayload.setScAgentCommissionDollarsUS(Double.toString(agentCommissionDollarsUS));
		}

		Double freightDollarsUS = (Double)SparcIntegrationUtil.getValueFrom(flexCostSheet, LUCKY_COSTING_COST_SHEET_FREIGHT_DOLLARS_ATTR);

		if (freightDollarsUS != null) {
			costSheetDataPayload.setScFreightDollarsUS(Double.toString(freightDollarsUS));
		}

		Double dutyPercentUS = (Double)SparcIntegrationUtil.getValueFrom(flexCostSheet, LUCKY_COSTING_COST_SHEET_DUTY_PERCENT_ATTR);

		if (dutyPercentUS != null) {
			costSheetDataPayload.setScDutyPercentUS(Double.toString(dutyPercentUS));
		}

		Double totalDutyDollarsUS = (Double)SparcIntegrationUtil.getValueFrom(flexCostSheet, LUCKY_COSTING_COST_SHEET_TOTAL_DUTY_DOLLARS_ATTR);

		if (totalDutyDollarsUS != null) {
			costSheetDataPayload.setScTotalDutyDollarsUS(Double.toString(totalDutyDollarsUS));
		}
		//Lucky Enhancement - End
	}

	/**
	 *
	 * @param costSheetDataPayload
	 * @param flexSourcingConfig
	 * @throws WTException
	 */
	private void buildSourcingConfigAttributes(CostSheetDataContent costSheetDataPayload,LCSSourcingConfig flexSourcingConfig) throws WTException {

		if (costSheetDataPayload == null || flexSourcingConfig == null) {
			return;
		}
		LCSSupplier flexVendor = (LCSSupplier)SparcIntegrationUtil.getValueFrom(flexSourcingConfig, LUCKY_COSTING_SOURCING_VENDOR_ATTR);

		if (flexVendor != null) {
			costSheetDataPayload.setScCostSheetNumber(String.valueOf(SparcIntegrationUtil.getValueFrom(flexVendor, LUCKY_COST_VENDOR_SUPPLIER_NO)));
			costSheetDataPayload.setScVendorName((String)SparcIntegrationUtil.getValueFrom(flexVendor, LUCKY_VENDOR_NAME));
			costSheetDataPayload.setScVendorNo(String.valueOf((Long)SparcIntegrationUtil.getValueFrom(flexVendor, LUCKY_COST_VENDOR_SUPPLIER_NO)));
		}
	}

	/**
	 *
	 * @param costSheetDataPayload
	 * @param flexSourcingSeasonLink
	 * @throws WTException
	 */
	private void buildSourceSeasonAttributes(CostSheetDataContent costSheetDataPayload,
											 LCSSourceToSeasonLink flexSourcingSeasonLink) throws WTException {

		if (costSheetDataPayload == null || flexSourcingSeasonLink == null) {
			return;
		}

		costSheetDataPayload.setScSourcingStatus(lookupEnumSecondaryKeyValue(flexSourcingSeasonLink,LUCKY_COSTING_SOURCING_STATUS_ATTR));

	}

	/**
	 *
	 * @param costSheetDataPayload
	 * @param flexColorway
	 * @throws WTException
	 */
	private void buildColorAttributes(CostSheetDataContent costSheetDataPayload, LCSSKU flexColorway) throws WTException {

		if (costSheetDataPayload == null || flexColorway == null) {
			return;
		}
		LCSColor flexColor = (LCSColor)SparcIntegrationUtil.getValueFrom(flexColorway, LUCKY_COSTING_COLORWAY_COLOR_ATTR);

		if (flexColor != null) {
			costSheetDataPayload.setSclCLR_CDE((String)SparcIntegrationUtil.getValueFrom(flexColor, LUCKY_FC_COLOR_ID_ATTR));
		}
	}


	/**
	 * Builds the CAP Costing Details payload.
	 * @return The CAP Costing Details payload built.
	 * @throws WTException When an error occurs while accessing or retrieving the various data elements from PLM.
	 * @throws WTPropertyVetoException When an error occurs while updating the Sourcing-Colorway-Season costing control flags at PLM.
	 * @throws Exception When a different error, i.e. validation fail, occurs.
	 */
	public CAPCostingDetailsPayload build() throws WTException, WTPropertyVetoException, Exception {

		List<LuckyCostingColorwayCostSheetMaster> colorwayCostSheets = LuckyCostingRepository.findEligibleColorwayCostSheets(sourcingConfigNumber,
				LuckyCostingProcessesParam.CAP);

		List<String> updatedCostSheets = new ArrayList<String>();


		if (sourcingConfigNumber != null) {
			capCostingDetails.getData().setSourcingConfigNumber(sourcingConfigNumber.toString());
		}

		for (LuckyCostingColorwayCostSheetMaster costSheetMaster : colorwayCostSheets) {
			CostSheetDataContent costSheetDataPayload = capCostingDetails.getData().newCostSheetDataContentInstance();
			capCostingDetails.getData().getCostsheets().add(costSheetDataPayload);
			String costSheetNumber = "" + (costSheetMaster.getProductCostSheet() != null ? costSheetMaster.getProductCostSheet().getValue(LUCKY_COSTING_COST_SHEET_NUMBER_ATTR): "");
			String productNumber = "" + (costSheetMaster.getProduct() != null ? costSheetMaster.getProduct().getValue(LUCKY_COSTING_PRODUCT_NO_ATTR): "");
			String seasonName = "" + (costSheetMaster.getSeason() != null ? costSheetMaster.getSeason().getValue(LUCKY_COSTING_SEASON_NAME_ATTR) : "");
			String colorwayNumber = "" + (costSheetMaster.getColorway() != null ? costSheetMaster.getColorway().getValue(COLORWAY_INTERNAL_NAME): "");

			LOGGER.debug("About to build Aero CAP Costing payload for cost sheet #" + costSheetNumber +
					", colorway #" + colorwayNumber +
					", sourcing config #" + sourcingConfigNumber +
					", product #" + productNumber +
					", season '" + seasonName + "'.");


			buildSourcingConfigAttributes(costSheetDataPayload, costSheetMaster.getSourcingConfig(), costSheetMaster.getProductCostSheet());
			buildSourcingSeasonAttributes(costSheetDataPayload, costSheetMaster.getSourcingSeasonLink());
			buildSeasonAttributes(costSheetDataPayload, costSheetMaster.getSeason());
			buildProductAttributes(costSheetDataPayload, costSheetMaster.getProduct());
			buildColorwayAttributes(costSheetDataPayload, costSheetMaster.getColorway());
			buildCostSheetAttributes(costSheetDataPayload, costSheetMaster.getProductCostSheet());
			//Lucky Enhancement - Start
			buildColorAttributes(costSheetDataPayload,costSheetMaster.getColorway());
			buildSourcingConfigAttributes(costSheetDataPayload,costSheetMaster.getSourcingConfig());
			buildSourceSeasonAttributes(costSheetDataPayload,costSheetMaster.getSourcingSeasonLink());
			//Lucky Enhancement - End

			if (costSheetMaster.getProductCostSheet() != null && !updatedCostSheets.contains(costSheetNumber)) {
				LOGGER.debug("About to update Lucky CAP Costing integration flags for cost sheet #" + costSheetNumber + "(colorway #" + colorwayNumber + ").");
				try {
					if (LuckyCostingRepository.updateIntegrationControlFlags(costSheetDataPayload.getScCostSheetNumber(),
							LuckyCostingProcessesParam.CAP,
							costSheetMaster.getProductCostSheet())) {
						updatedCostSheets.add(costSheetNumber);
						LOGGER.debug("Lucky CAP Costing integration flags for for cost sheet #" + costSheetNumber + "(colorway #" + colorwayNumber + ") have been updated.");
					}

				} catch(Exception csEx) {
					throw new Exception("Failed to update cost sheet CAP integration control flags for cost sheet #" + costSheetNumber + " under sourcing config #" + sourcingConfigNumber + " and product #" + productNumber + ".", csEx);
				}
			} else {
				LOGGER.debug("Lucky CAP Costing integration flags update for for cost sheet #" + costSheetNumber + "(colorway #" + colorwayNumber + "). has been skipped (flags already updated).");
			}
		}
		return capCostingDetails;
	}

}
