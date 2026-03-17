package com.sparc.wc.integration.aero.domain;

import java.util.Objects;

/**
 * Represents the composite Key to use for the cost sheet eligibility requirement:<br>
 * PLM will send one Cost Sheet per unique combinations of Incoterms, Destination, Additional Buy, Vendor, Finished Goods Factory, Season, Year, and Colorway.<br>
 * In this case, the above statement is represented within this class' attributes as follows:<br>
 *  - Incoterms: costSheetIncoterms,<br>
 *  - Destination: costSheetDestination,<br>
 *  - Additional Buy: costSheetIncoterms,<br>
 *  - Vendor: vendorBranchId,<br>
 *  - Finished Goods Factory: fgFactoryBranchId,<br>
 *  - Season: seasonId (relevant key), seasonName, seasonType,<br>
 *  - Year: seasonYear,<br>
 *  - Colorway: colorwayId (relevant key), colorwayNumber, colorwayName.<br>
 *  Note that the above combination exists within a Sourcing Configuration, hence sourcingConfigId & sourcingConfigNumber are also part of the key.
 *  
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9646 Aero - Costing Interface"
 */
public class AeroCostingCostSheetKey {
	
	private Long colorwayId;
	private Long colorwayNumber;
	private Long fgFactoryBranchId;
	private Long seasonId;
	private Long sourcingConfigId;
	private Long sourcingConfigNumber;
	private Long vendorBranchId;
	
	private String colorwayName;
	private String costSheetAdditionalBuy;
	private String costSheetIncoterms;
	private String costSheetDestination;
	private String seasonName;
	private String seasonType;
	private String seasonYear;
	
	public AeroCostingCostSheetKey() {
		
	}

	public Long getColorwayId() {
		return colorwayId;
	}

	public void setColorwayId(Long colorwayId) {
		this.colorwayId = colorwayId;
	}

	public Long getColorwayNumber() {
		return colorwayNumber;
	}

	public void setColorwayNumber(Long colorwayNumber) {
		this.colorwayNumber = colorwayNumber;
	}

	public Long getFgFactoryBranchId() {
		return fgFactoryBranchId;
	}

	public void setFgFactoryBranchId(Long fgFactoryBranchId) {
		this.fgFactoryBranchId = fgFactoryBranchId;
	}

	public Long getSeasonId() {
		return seasonId;
	}

	public void setSeasonId(Long seasonId) {
		this.seasonId = seasonId;
	}

	public Long getSourcingConfigId() {
		return sourcingConfigId;
	}

	public void setSourcingConfigId(Long sourcingConfigId) {
		this.sourcingConfigId = sourcingConfigId;
	}

	public Long getSourcingConfigNumber() {
		return sourcingConfigNumber;
	}

	public void setSourcingConfigNumber(Long sourcingConfigNumber) {
		this.sourcingConfigNumber = sourcingConfigNumber;
	}

	public Long getVendorBranchId() {
		return vendorBranchId;
	}

	public void setVendorBranchId(Long vendorBranchId) {
		this.vendorBranchId = vendorBranchId;
	}

	public String getColorwayName() {
		return colorwayName;
	}

	public void setColorwayName(String colorwayName) {
		this.colorwayName = colorwayName;
	}

	public String getCostSheetAdditionalBuy() {
		return costSheetAdditionalBuy;
	}

	public void setCostSheetAdditionalBuy(String costSheetAdditionalBuy) {
		this.costSheetAdditionalBuy = costSheetAdditionalBuy;
	}

	public String getCostSheetIncoterms() {
		return costSheetIncoterms;
	}

	public void setCostSheetIncoterms(String costSheetIncoterms) {
		this.costSheetIncoterms = costSheetIncoterms;
	}

	public String getCostSheetDestination() {
		return costSheetDestination;
	}

	public void setCostSheetDestination(String costSheetDestination) {
		this.costSheetDestination = costSheetDestination;
	}

	public String getSeasonName() {
		return seasonName;
	}

	public void setSeasonName(String seasonName) {
		this.seasonName = seasonName;
	}

	public String getSeasonType() {
		return seasonType;
	}

	public void setSeasonType(String seasonType) {
		this.seasonType = seasonType;
	}

	public String getSeasonYear() {
		return seasonYear;
	}

	public void setSeasonYear(String seasonYear) {
		this.seasonYear = seasonYear;
	}

	@Override
	public int hashCode() {
		return Objects.hash(colorwayId, colorwayName, colorwayNumber, costSheetAdditionalBuy, costSheetDestination,
				costSheetIncoterms, fgFactoryBranchId, seasonId, seasonName, seasonType, seasonYear, sourcingConfigId,
				sourcingConfigNumber, vendorBranchId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AeroCostingCostSheetKey other = (AeroCostingCostSheetKey) obj;
		return Objects.equals(colorwayId, other.colorwayId) && Objects.equals(colorwayName, other.colorwayName)
				&& Objects.equals(colorwayNumber, other.colorwayNumber)
				&& Objects.equals(costSheetAdditionalBuy, other.costSheetAdditionalBuy)
				&& Objects.equals(costSheetDestination, other.costSheetDestination)
				&& Objects.equals(costSheetIncoterms, other.costSheetIncoterms)
				&& Objects.equals(fgFactoryBranchId, other.fgFactoryBranchId)
				&& Objects.equals(seasonId, other.seasonId) && Objects.equals(seasonName, other.seasonName)
				&& Objects.equals(seasonType, other.seasonType) && Objects.equals(seasonYear, other.seasonYear)
				&& Objects.equals(sourcingConfigId, other.sourcingConfigId)
				&& Objects.equals(sourcingConfigNumber, other.sourcingConfigNumber)
				&& Objects.equals(vendorBranchId, other.vendorBranchId);
	}
	
}
