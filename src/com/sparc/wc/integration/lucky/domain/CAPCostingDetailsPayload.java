package com.sparc.wc.integration.lucky.domain;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.sparc.wc.integration.lucky.builders.CAPCostingDetailsPayloadBuilder;

/**
 * Payload for a Costing detail data for Lucky/CAP.<br
 * FIXES/AMENDMENTS:<br>
 * - Task #9234 (UAT): change scSAPVendorNameLucky to scFinishedGoodsFactoryName.<br>
 * - Task #9238 (UAT): Remove sclegacyarticle from JSON.<br>
 * 
 * @author Acnovate
 * @see "Task #8259 3.1 Costing API - FC & CAP (Outbound)"
 */
public class CAPCostingDetailsPayload {

	private MetaContent meta;
	private DataContent data;
	
	/**
	 * Creates a builder instance to build a CAP Costing details payload.
	 */
	public static CAPCostingDetailsPayloadBuilder newBuilder() {
		return new CAPCostingDetailsPayloadBuilder(new CAPCostingDetailsPayload());
	}
	
	private CAPCostingDetailsPayload() {
		meta = new MetaContent();
		data = new DataContent();
	}
	
	/**
	 * Payload for meta data content.
	 */
	public class MetaContent {
		
	}
	
	/**
	 * Payload for data content.
	 */
	public class DataContent {
		
		private String sourcingConfigNumber;
		private List<CostSheetDataContent> costsheets;
		
		public DataContent() {
			costsheets = new ArrayList<CostSheetDataContent>();
		}
		
		public String getSourcingConfigNumber() {
			return sourcingConfigNumber;
		}
		
		public void setSourcingConfigNumber(String sourcingConfigNumber) {
			this.sourcingConfigNumber = sourcingConfigNumber;
		}
		
		public List<CostSheetDataContent> getCostsheets() {
			return costsheets;
		}
		
		public CostSheetDataContent newCostSheetDataContentInstance() {
			return new CostSheetDataContent();
		}
		
		public class CostSheetDataContent {
			
			private String scColorwayNo;
			private String scColorNo;
			private String seasonType;
			private String year;
			private String scPLMProductNo;
			private String scCostSheetNumber;
			private String scCostSheetName;
			private String scFullCircleVendorNo;
			private String scFullCircleVendorName;
			private String scLuckyFactoryID;
			private String scFinishedGoodsFactoryName;
			private String scIncoterms;
			private String scTotalVendorCost;
			private String scLandedCostUSELC;
			private String scLandedCostUSWCCost;
			private String scShipMode;
			private String scCountryofOrigin;
			private String scMilestoneStatus;
			private String scCostSheetStatus;
			private String scHTSCode;
			private String scVendorProductCost;
			private String sclSTY_NUM;
			private String sclCLR_CDE;
			private String scUnitsOtherCosts;
			private String scAgentCommissionPercentUS;
			private String scAgentCommissionDollarsUS;
			private String scFreightDollarsUS;
			private String scDutyPercentUS;
			private String scTotalDutyDollarsUS;
			private String scVendorNo;
			private String scVendorName;
			private String scSourcingStatus;
			private String scCurrencyCode;

			public String getScCurrencyCode() {
				return scCurrencyCode;
			}

			public void setScCurrencyCode(String scCurrencyCode) {
				this.scCurrencyCode = scCurrencyCode;
			}



			public String getScVendorProductCost() {
				return scVendorProductCost;
			}

			public void setScVendorProductCost(String scVendorProductCost) {
				this.scVendorProductCost = scVendorProductCost;
			}

			public String getSclSTY_NUM() {
				return sclSTY_NUM;
			}

			public void setSclSTY_NUM(String sclSTY_NUM) {
				this.sclSTY_NUM = sclSTY_NUM;
			}

			public String getSclCLR_CDE() {
				return sclCLR_CDE;
			}

			public void setSclCLR_CDE(String sclCLR_CDE) {
				this.sclCLR_CDE = sclCLR_CDE;
			}

			public String getScUnitsOtherCosts() {
				return scUnitsOtherCosts;
			}

			public void setScUnitsOtherCosts(String scUnitsOtherCosts) {
				this.scUnitsOtherCosts = scUnitsOtherCosts;
			}

			public String getScAgentCommissionPercentUS() {
				return scAgentCommissionPercentUS;
			}

			public void setScAgentCommissionPercentUS(String scAgentCommissionPercentUS) {
				this.scAgentCommissionPercentUS = scAgentCommissionPercentUS;
			}

			public String getScAgentCommissionDollarsUS() {
				return scAgentCommissionDollarsUS;
			}

			public void setScAgentCommissionDollarsUS(String scAgentCommissionDollarsUS) {
				this.scAgentCommissionDollarsUS = scAgentCommissionDollarsUS;
			}

			public String getScFreightDollarsUS() {
				return scFreightDollarsUS;
			}

			public void setScFreightDollarsUS(String scFreightDollarsUS) {
				this.scFreightDollarsUS = scFreightDollarsUS;
			}

			public String getScDutyPercentUS() {
				return scDutyPercentUS;
			}

			public void setScDutyPercentUS(String scDutyPercentUS) {
				this.scDutyPercentUS = scDutyPercentUS;
			}

			public String getScTotalDutyDollarsUS() {
				return scTotalDutyDollarsUS;
			}

			public void setScTotalDutyDollarsUS(String scTotalDutyDollarsUS) {
				this.scTotalDutyDollarsUS = scTotalDutyDollarsUS;
			}

			public String getScVendorNo() {
				return scVendorNo;
			}

			public void setScVendorNo(String scVendorNo) {
				this.scVendorNo = scVendorNo;
			}

			public String getScVendorName() {
				return scVendorName;
			}

			public void setScVendorName(String scVendorName) {
				this.scVendorName = scVendorName;
			}

			public String getScSourcingStatus() {
				return scSourcingStatus;
			}

			public void setScSourcingStatus(String scSourcingStatus) {
				this.scSourcingStatus = scSourcingStatus;
			}
			
			public String getScColorwayNo() {
				return scColorwayNo;
			}

			public void setScColorwayNo(String scColorwayNo) {
				this.scColorwayNo = scColorwayNo;
			}

			public String getScColorNo() {
				return scColorNo;
			}

			public void setScColorNo(String scColorNo) {
				this.scColorNo = scColorNo;
			}

			public String getSeasonType() {
				return seasonType;
			}

			public void setSeasonType(String seasonType) {
				this.seasonType = seasonType;
			}

			public String getYear() {
				return year;
			}

			public void setYear(String year) {
				this.year = year;
			}

			public String getScPLMProductNo() {
				return scPLMProductNo;
			}

			public void setScPLMProductNo(String scPLMProductNo) {
				this.scPLMProductNo = scPLMProductNo;
			}

			public String getScCostSheetNumber() {
				return scCostSheetNumber;
			}

			public void setScCostSheetNumber(String scCostSheetNumber) {
				this.scCostSheetNumber = scCostSheetNumber;
			}

			public String getScCostSheetName() {
				return scCostSheetName;
			}

			public void setScCostSheetName(String scCostSheetName) {
				this.scCostSheetName = scCostSheetName;
			}

			public String getScFullCircleVendorNo() {
				return scFullCircleVendorNo;
			}

			public void setScFullCircleVendorNo(String scFullCircleVendorNo) {
				this.scFullCircleVendorNo = scFullCircleVendorNo;
			}

			public String getScFullCircleVendorName() {
				return scFullCircleVendorName;
			}

			public void setScFullCircleVendorName(String scFullCircleVendorName) {
				this.scFullCircleVendorName = scFullCircleVendorName;
			}

			public String getScLuckyFactoryID() {
				return scLuckyFactoryID;
			}

			public void setScLuckyFactoryID(String scLuckyFactoryID) {
				this.scLuckyFactoryID = scLuckyFactoryID;
			}
			
			public String getScFinishedGoodsFactoryName() {
				return scFinishedGoodsFactoryName;
			}

			public void setScFinishedGoodsFactoryName(String scFinishedGoodsFactoryName) {
				this.scFinishedGoodsFactoryName = scFinishedGoodsFactoryName;
			}

			public String getScIncoterms() {
				return scIncoterms;
			}

			public void setScIncoterms(String scIncoterms) {
				this.scIncoterms = scIncoterms;
			}

			public String getScTotalVendorCost() {
				return scTotalVendorCost;
			}

			public void setScTotalVendorCost(String scTotalVendorCost) {
				this.scTotalVendorCost = scTotalVendorCost;
			}

			public String getScLandedCostUSELC() {
				return scLandedCostUSELC;
			}

			public void setScLandedCostUSELC(String scLandedCostUSELC) {
				this.scLandedCostUSELC = scLandedCostUSELC;
			}

			public String getScLandedCostUSWCCost() {
				return scLandedCostUSWCCost;
			}

			public void setScLandedCostUSWCCost(String scLandedCostUSWCCost) {
				this.scLandedCostUSWCCost = scLandedCostUSWCCost;
			}

			public String getScShipMode() {
				return scShipMode;
			}

			public void setScShipMode(String scShipMode) {
				this.scShipMode = scShipMode;
			}

			public String getScCountryofOrigin() {
				return scCountryofOrigin;
			}

			public void setScCountryofOrigin(String scCountryofOrigin) {
				this.scCountryofOrigin = scCountryofOrigin;
			}

			public String getScMilestoneStatus() {
				return scMilestoneStatus;
			}

			public void setScMilestoneStatus(String scMilestoneStatus) {
				this.scMilestoneStatus = scMilestoneStatus;
			}

			public String getScCostSheetStatus() {
				return scCostSheetStatus;
			}

			public void setScCostSheetStatus(String scCostSheetStatus) {
				this.scCostSheetStatus = scCostSheetStatus;
			}

			public String getScHTSCode() {
				return scHTSCode;
			}

			public void setScHTSCode(String scHTSCode) {
				this.scHTSCode = scHTSCode;
			}
			
			@Override
			public String toString() {
				return "CostSheetDataContent [scColorwayNo=" + scColorwayNo + ", scColorNo=" + scColorNo
						+ ", seasonType=" + seasonType + ", year=" + year + ", scPLMProductNo=" + scPLMProductNo
						+ ", scCostSheetNumber=" + scCostSheetNumber + ", scCostSheetName=" + scCostSheetName
						+ ", scFullCircleVendorNo=" + scFullCircleVendorNo + ", scFullCircleVendorName="
						+ scFullCircleVendorName + ", scLuckyFactoryID=" + scLuckyFactoryID
						+ ", scFinishedGoodsFactoryName=" + scFinishedGoodsFactoryName + ", scIncoterms=" + scIncoterms
						+ ", scTotalVendorCost=" + scTotalVendorCost + ", scLandedCostUSELC=" + scLandedCostUSELC
						+ ", scLandedCostUSWCCost=" + scLandedCostUSWCCost + ", scShipMode=" + scShipMode
						+ ", scCountryofOrigin=" + scCountryofOrigin + ", scMilestoneStatus=" + scMilestoneStatus
						+ ", scCostSheetStatus=" + scCostSheetStatus + ", scHTSCode=" + scHTSCode + "]";
			}
			
		}
		
	}
	
	public MetaContent getMeta() {
		return meta;
	}

	public void setMeta(MetaContent meta) {
		this.meta = meta;
	}

	public DataContent getData() {
		return data;
	}

	public void setData(DataContent data) {
		this.data = data;
	}
	
	public String toJSON() {
		return new GsonBuilder().serializeNulls().create().toJson(this);
	}

	@Override
	public String toString() {
		return "CAPCostingDetailsPayload [meta=" + meta + ", data=" + data + "]";
	}
	
}
