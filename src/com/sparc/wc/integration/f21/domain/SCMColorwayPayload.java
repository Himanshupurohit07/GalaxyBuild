package com.sparc.wc.integration.f21.domain;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.sparc.wc.integration.f21.builders.ColorwayPayloadBuilder;

/**
 * Payload for a Colorway data as per defined for SCM integration.
 * - Task #8942 (UAT): Relocate product update flag to colorway data.
 * - Task #9201 (UAT): Add outbound attribute 'Legacy SCM Item Number' to the payload.
 * 
 * @author Acnovate
 */
public class SCMColorwayPayload {
	
	private MetaContent meta;
	private DataContent data;
	
	/**
	 * Creates a builder instance to build a SCM ColorwayIds payload.
	 */
	public static ColorwayPayloadBuilder newBuilder() {
		return new ColorwayPayloadBuilder(new SCMColorwayPayload());
	}
	
	private SCMColorwayPayload() {
		meta = new MetaContent();
		data = new DataContent();
	}
	
	public class MetaContent {
		
	}
	
	public class DataContent {
		
		private long sourcingConfigNumber;
		private String sourcingStatus;
		private long plmProductNumber;
		private String vendorSCMNumber;
		private String finishedGoodsFactorySCMNumber;
		private String countryOfOriginISO;
		private String vendorStyleID;
		private String developmentSeason;
		private String productDescription;
		private String productShortDescription;
		private String division;
		private String divisionDept;
		private String category1;
		private String category2;
		private String style1;
		private String style2;
		private String placement;
		private String technique;
		private String detail;
		private String neckShape;
		private String matchingSet;
		private String vendorBrand;
		private String trendGroup;
		private String seasonalGroup;
		private String styleIntent;
		private String logoType;
		private String logoSubType;
		private String fashion;
		private String designGroup;
		private String itemGroupCode;
		private String finish;
		private String patternName;
		private String developmentPath;
		private String fabricCountryOfOriginISO;
		private String priceGroup;
		private String launchDate;
		private String merchant;
		private String designLead;
		private String legacySCMItemNumber;
		
		private List<ColorwayDataContent> colorways;
		
		public DataContent() {
			colorways = new ArrayList<ColorwayDataContent>();
		}
		
		public long getSourcingConfigNumber() {
			return sourcingConfigNumber;
		}

		public void setSourcingConfigNumber(long sourcingConfigNumber) {
			this.sourcingConfigNumber = sourcingConfigNumber;
		}

		public String getSourcingStatus() {
			return sourcingStatus;
		}

		public void setSourcingStatus(String sourcingStatus) {
			this.sourcingStatus = sourcingStatus;
		}

		public long getPlmProductNumber() {
			return plmProductNumber;
		}

		public void setPlmProductNumber(long plmProductNumber) {
			this.plmProductNumber = plmProductNumber;
		}

		public String getVendorSCMNumber() {
			return vendorSCMNumber;
		}

		public void setVendorSCMNumber(String vendorSCMNumber) {
			this.vendorSCMNumber = vendorSCMNumber;
		}

		public String getFinishedGoodsFactorySCMNumber() {
			return finishedGoodsFactorySCMNumber;
		}

		public void setFinishedGoodsFactorySCMNumber(String finishedGoodsFactorySCMNumber) {
			this.finishedGoodsFactorySCMNumber = finishedGoodsFactorySCMNumber;
		}

		public String getCountryOfOriginISO() {
			return countryOfOriginISO;
		}

		public void setCountryOfOriginISO(String countryOfOriginISO) {
			this.countryOfOriginISO = countryOfOriginISO;
		}

		public String getVendorStyleID() {
			return vendorStyleID;
		}

		public void setVendorStyleID(String vendorStyleID) {
			this.vendorStyleID = vendorStyleID;
		}

		public String getDevelopmentSeason() {
			return developmentSeason;
		}

		public void setDevelopmentSeason(String developmentSeason) {
			this.developmentSeason = developmentSeason;
		}
		
		public String getProductDescription() {
			return productDescription;
		}

		public void setProductDescription(String productDescription) {
			this.productDescription = productDescription;
		}

		public String getProductShortDescription() {
			return productShortDescription;
		}

		public void setProductShortDescription(String productShortDescription) {
			this.productShortDescription = productShortDescription;
		}

		public String getDivision() {
			return division;
		}

		public void setDivision(String division) {
			this.division = division;
		}

		public String getDivisionDept() {
			return divisionDept;
		}

		public void setDivisionDept(String divisionDept) {
			this.divisionDept = divisionDept;
		}

		public String getCategory1() {
			return category1;
		}

		public void setCategory1(String category1) {
			this.category1 = category1;
		}

		public String getCategory2() {
			return category2;
		}

		public void setCategory2(String category2) {
			this.category2 = category2;
		}

		public String getStyle1() {
			return style1;
		}

		public void setStyle1(String style1) {
			this.style1 = style1;
		}

		public String getStyle2() {
			return style2;
		}

		public void setStyle2(String style2) {
			this.style2 = style2;
		}

		public String getPlacement() {
			return placement;
		}

		public void setPlacement(String placement) {
			this.placement = placement;
		}

		public String getTechnique() {
			return technique;
		}

		public void setTechnique(String technique) {
			this.technique = technique;
		}

		public String getDetail() {
			return detail;
		}

		public void setDetail(String detail) {
			this.detail = detail;
		}

		public String getNeckShape() {
			return neckShape;
		}

		public void setNeckShape(String neckShape) {
			this.neckShape = neckShape;
		}

		public String getMatchingSet() {
			return matchingSet;
		}

		public void setMatchingSet(String matchingSet) {
			this.matchingSet = matchingSet;
		}

		public String getVendorBrand() {
			return vendorBrand;
		}

		public void setVendorBrand(String vendorBrand) {
			this.vendorBrand = vendorBrand;
		}

		public String getTrendGroup() {
			return trendGroup;
		}

		public void setTrendGroup(String trendGroup) {
			this.trendGroup = trendGroup;
		}

		public String getSeasonalGroup() {
			return seasonalGroup;
		}

		public void setSeasonalGroup(String seasonalGroup) {
			this.seasonalGroup = seasonalGroup;
		}

		public String getStyleIntent() {
			return styleIntent;
		}

		public void setStyleIntent(String styleIntent) {
			this.styleIntent = styleIntent;
		}

		public String getLogoType() {
			return logoType;
		}

		public void setLogoType(String logoType) {
			this.logoType = logoType;
		}

		public String getLogoSubType() {
			return logoSubType;
		}

		public void setLogoSubType(String logoSubType) {
			this.logoSubType = logoSubType;
		}

		public String getFashion() {
			return fashion;
		}

		public void setFashion(String fashion) {
			this.fashion = fashion;
		}

		public String getDesignGroup() {
			return designGroup;
		}

		public void setDesignGroup(String designGroup) {
			this.designGroup = designGroup;
		}

		public String getItemGroupCode() {
			return itemGroupCode;
		}

		public void setItemGroupCode(String itemGroupCode) {
			this.itemGroupCode = itemGroupCode;
		}

		public String getFinish() {
			return finish;
		}

		public void setFinish(String finish) {
			this.finish = finish;
		}

		public String getPatternName() {
			return patternName;
		}

		public void setPatternName(String patternName) {
			this.patternName = patternName;
		}

		public String getDevelopmentPath() {
			return developmentPath;
		}

		public void setDevelopmentPath(String developmentPath) {
			this.developmentPath = developmentPath;
		}

		public String getFabricCountryOfOriginISO() {
			return fabricCountryOfOriginISO;
		}

		public void setFabricCountryOfOriginISO(String fabricCountryOfOriginISO) {
			this.fabricCountryOfOriginISO = fabricCountryOfOriginISO;
		}

		public String getPriceGroup() {
			return priceGroup;
		}

		public void setPriceGroup(String priceGroup) {
			this.priceGroup = priceGroup;
		}

		public String getLaunchDate() {
			return launchDate;
		}

		public void setLaunchDate(String launchDate) {
			this.launchDate = launchDate;
		}

		public String getMerchant() {
			return merchant;
		}

		public void setMerchant(String merchant) {
			this.merchant = merchant;
		}

		public String getDesignLead() {
			return designLead;
		}

		public void setDesignLead(String designLead) {
			this.designLead = designLead;
		}
		
		public List<ColorwayDataContent> getColorways() {
			return colorways;
		}

		public void setColorways(List<ColorwayDataContent> colorways) {
			this.colorways = colorways;
		}
		
		public String getLegacySCMItemNumber() {
			return legacySCMItemNumber;
		}

		public void setLegacySCMItemNumber(String legacySCMItemNumber) {
			this.legacySCMItemNumber = legacySCMItemNumber;
		}
		
		@Override
		public String toString() {
			return "DataContent [sourcingConfigNumber=" + sourcingConfigNumber + ", sourcingStatus=" + sourcingStatus
					+ ", plmProductNumber=" + plmProductNumber + ", vendorSCMNumber=" + vendorSCMNumber
					+ ", finishedGoodsFactorySCMNumber=" + finishedGoodsFactorySCMNumber + ", countryOfOriginISO="
					+ countryOfOriginISO + ", vendorStyleID=" + vendorStyleID + ", developmentSeason="
					+ developmentSeason + ", productDescription=" + productDescription + ", productShortDescription="
					+ productShortDescription + ", division=" + division + ", divisionDept=" + divisionDept
					+ ", category1=" + category1 + ", category2=" + category2 + ", style1=" + style1 + ", style2="
					+ style2 + ", placement=" + placement + ", technique=" + technique + ", detail=" + detail
					+ ", neckShape=" + neckShape + ", matchingSet=" + matchingSet + ", vendorBrand=" + vendorBrand
					+ ", trendGroup=" + trendGroup + ", seasonalGroup=" + seasonalGroup + ", styleIntent=" + styleIntent
					+ ", logoType=" + logoType + ", logoSubType=" + logoSubType + ", fashion=" + fashion
					+ ", designGroup=" + designGroup + ", itemGroupCode=" + itemGroupCode + ", finish=" + finish
					+ ", patternName=" + patternName + ", developmentPath=" + developmentPath
					+ ", fabricCountryOfOriginISO=" + fabricCountryOfOriginISO + ", priceGroup=" + priceGroup
					+ ", launchDate=" + launchDate + ", merchant=" + merchant + ", designLead=" + designLead
					+ ", legacySCMItemNumber=" + legacySCMItemNumber + ", colorways=" + colorways + "]";
		}

		public ColorwayDataContent newColorwayDataContentInstance() {
			return new ColorwayDataContent();
		}
		
		public class ColorwayDataContent {
			
			private String colorwayNumber;
			private String colorDetail;
			private String colorwaySeason;
			private String plmColorwayStatus;
			private String productUpdateFlag;
			
			public String getColorwayNumber() {
				return colorwayNumber;
			}
			public void setColorwayNumber(String colorwayNumber) {
				this.colorwayNumber = colorwayNumber;
			}
			public String getColorDetail() {
				return colorDetail;
			}
			public void setColorDetail(String colorDetail) {
				this.colorDetail = colorDetail;
			}
			public String getColorwaySeason() {
				return colorwaySeason;
			}
			public void setColorwaySeason(String colorwaySeason) {
				this.colorwaySeason = colorwaySeason;
			}
			public String getPlmColorwayStatus() {
				return plmColorwayStatus;
			}
			public void setPlmColorwayStatus(String plmColorwayStatus) {
				this.plmColorwayStatus = plmColorwayStatus;
			}
			
			public String getProductUpdateFlag() {
				return productUpdateFlag;
			}

			public void setProductUpdateFlag(String productUpdateFlag) {
				this.productUpdateFlag = productUpdateFlag;
			}
			
			@Override
			public String toString() {
				return "ColorwayDataContent [colorwayNumber=" + colorwayNumber + ", colorDetail=" + colorDetail
						+ ", colorwaySeason=" + colorwaySeason + ", plmColorwayStatus=" + plmColorwayStatus
						+ ", productUpdateFlag=" + productUpdateFlag + "]";
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
		//return new Gson().toJson(this);
	}

	@Override
	public String toString() {
		return "SCMColorwayPayload [meta=" + meta + ", data=" + data + "]";
	}
	
}
