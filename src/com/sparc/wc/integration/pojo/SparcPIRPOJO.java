package com.sparc.wc.integration.pojo;

public class SparcPIRPOJO {
	
	//Integration CostSheet Attributes Start
	private String scIncoterms;
	private String scIncotermsLoc;
	private String scVendorCost;
	private String scTotalVendorCost;
	private String scHTSCode;
	private String scMarketingroyalty;
	private String scOtherCost;
	//Integration CostSheet Attributes End
	
	//Integration Sourcing Attributes Start
	private String scFGFactory;
	private String scFactoryCountryOrigin;
	private String scRegion;
	private String scprodleadtime;
	private String scSourcingStatus;
	private String scColorwayNo;
	private String seasonType;
	private String year;
	private String sctransleadtime;
	//Integration Sourcing Attributes End
	
	//Integration SKU Season Attributes Start
	private String scFactoryPriority;
	//Integration SKU Season Attributes End
	
	public SparcPIRPOJO() {
	}

	public String getScIncoterms() {
		return scIncoterms;
	}

	public void setScIncoterms(String scIncoterms) {
		this.scIncoterms = scIncoterms;
	}

	public String getScIncotermsLoc() {
		return scIncotermsLoc;
	}

	public void setScIncotermsLoc(String scIncotermsLoc) {
		this.scIncotermsLoc = scIncotermsLoc;
	}

	public String getScVendorCost() {
		return scVendorCost;
	}

	public void setScVendorCost(String scVendorCost) {
		this.scVendorCost = scVendorCost;
	}

	public String getScTotalVendorCost() {
		return scTotalVendorCost;
	}

	public void setScTotalVendorCost(String scTotalVendorCost) {
		this.scTotalVendorCost = scTotalVendorCost;
	}

	public String getScHTSCode() {
		return scHTSCode;
	}

	public void setScHTSCode(String scHTSCode) {
		this.scHTSCode = scHTSCode;
	}

	public String getScMarketingroyalty() {
		return scMarketingroyalty;
	}

	public void setScMarketingroyalty(String scMarketingroyalty) {
		this.scMarketingroyalty = scMarketingroyalty;
	}

	public String getScOtherCost() {
		return scOtherCost;
	}

	public void setScOtherCost(String scOtherCost) {
		this.scOtherCost = scOtherCost;
	}

	public String getScFGFactory() {
		return scFGFactory;
	}

	public void setScFGFactory(String scFGFactory) {
		this.scFGFactory = scFGFactory;
	}

	public String getScFactoryCountryOrigin() {
		return scFactoryCountryOrigin;
	}

	public void setScFactoryCountryOrigin(String scFactoryCountryOrigin) {
		this.scFactoryCountryOrigin = scFactoryCountryOrigin;
	}

	public String getScRegion() {
		return scRegion;
	}

	public void setScRegion(String scRegion) {
		this.scRegion = scRegion;
	}

	public String getScprodleadtime() {
		return scprodleadtime;
	}

	public void setScprodleadtime(String scprodleadtime) {
		this.scprodleadtime = scprodleadtime;
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

	public String getSctransleadtime() {
		return sctransleadtime;
	}

	public void setSctransleadtime(String sctransleadtime) {
		this.sctransleadtime = sctransleadtime;
	}

	@Override
	public String toString() {
		return "SparcPIRPOJO [scIncoterms=" + scIncoterms + ", scIncotermsLoc=" + scIncotermsLoc + ", scVendorCost="
				+ scVendorCost + ", scTotalVendorCost=" + scTotalVendorCost + ", scHTSCode=" + scHTSCode
				+ ", scMarketingroyalty=" + scMarketingroyalty + ", scOtherCost=" + scOtherCost + ", scFGFactory="
				+ scFGFactory + ", scFactoryCountryOrigin=" + scFactoryCountryOrigin + ", scRegion=" + scRegion
				+ ", scprodleadtime=" + scprodleadtime + ", scSourcingStatus=" + scSourcingStatus + ", scColorwayNo="
				+ scColorwayNo + ", seasonType=" + seasonType + ", year=" + year + ", sctransleadtime="
				+ sctransleadtime + "]";
	}

	public SparcPIRPOJO(String scIncoterms, String scIncotermsLoc, String scVendorCost, String scTotalVendorCost,
			String scHTSCode, String scMarketingroyalty, String scOtherCost, String scFGFactory,
			String scFactoryCountryOrigin, String scRegion, String scprodleadtime, String scSourcingStatus,
			String scColorwayNo, String seasonType, String year, String sctransleadtime) {
		super();
		this.scIncoterms = scIncoterms;
		this.scIncotermsLoc = scIncotermsLoc;
		this.scVendorCost = scVendorCost;
		this.scTotalVendorCost = scTotalVendorCost;
		this.scHTSCode = scHTSCode;
		this.scMarketingroyalty = scMarketingroyalty;
		this.scOtherCost = scOtherCost;
		this.scFGFactory = scFGFactory;
		this.scFactoryCountryOrigin = scFactoryCountryOrigin;
		this.scRegion = scRegion;
		this.scprodleadtime = scprodleadtime;
		this.scSourcingStatus = scSourcingStatus;
		this.scColorwayNo = scColorwayNo;
		this.seasonType = seasonType;
		this.year = year;
		this.sctransleadtime = sctransleadtime;
	}
	public void SparcPIRPOJO() {
		
	}

	public String getScFactoryPriority() {
		return scFactoryPriority;
	}

	public void setScFactoryPriority(String scFactoryPriority) {
		this.scFactoryPriority = scFactoryPriority;
	}
}