package com.sparc.wc.integration.aero.domain;

import java.sql.Timestamp;
import java.util.Objects;

import com.sparc.wc.integration.aero.builders.AeroCostingCostSheetDataBuilder;

/**
 * Defines the container for the relevant data elements of an Aero colorway cost sheet within PLM.<br>
 * Note that in this context, relavant data elements refer to those key attributes required to later determine which is the right cost sheet
 * to be eligible to be sent thru the Aero costing integration (one cost sheet per sourcing-season-colorway). 
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9646 Aero - Costing Interface"
 * @see "AeroCostingRepository.findEligibleColorwayCostSheets for further details on how this container/data is used."
 */
public class AeroCostingCostSheetData {

	private Long costSheetId;
	private Long costSheetNumber;
	private Long productId;
	private Long productNumber;
	private Long sourcingSeasonLinkId;
	private Long sourcingColorwayLinkId;
	private Long colorwaySeasonLinkId;
	
	private String costSheetName;
	private String costSheetMilestone;
	private String productName;
	
	private AeroCostingCostSheetKey costSheetKey;
	
	private Timestamp costSheetModifiedDate;
	
	/**
	 * Creates a builder instance to build a Aero Costing Cost Sheet data.
	 */
	public static AeroCostingCostSheetDataBuilder newBuilder() {
		return new AeroCostingCostSheetDataBuilder(new AeroCostingCostSheetData());
	}
	
	private AeroCostingCostSheetData() {
		costSheetKey = new AeroCostingCostSheetKey();
	}

	public Long getCostSheetId() {
		return costSheetId;
	}

	public void setCostSheetId(Long costSheetId) {
		this.costSheetId = costSheetId;
	}

	public Long getCostSheetNumber() {
		return costSheetNumber;
	}

	public void setCostSheetNumber(Long costSheetNumber) {
		this.costSheetNumber = costSheetNumber;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Long getProductNumber() {
		return productNumber;
	}

	public void setProductNumber(Long productNumber) {
		this.productNumber = productNumber;
	}

	public Long getSourcingSeasonLinkId() {
		return sourcingSeasonLinkId;
	}

	public void setSourcingSeasonLinkId(Long sourcingSeasonLinkId) {
		this.sourcingSeasonLinkId = sourcingSeasonLinkId;
	}

	public Long getSourcingColorwayLinkId() {
		return sourcingColorwayLinkId;
	}

	public void setSourcingColorwayLinkId(Long sourcingColorwayLinkId) {
		this.sourcingColorwayLinkId = sourcingColorwayLinkId;
	}

	public Long getColorwaySeasonLinkId() {
		return colorwaySeasonLinkId;
	}

	public void setColorwaySeasonLinkId(Long colorwaySeasonLinkId) {
		this.colorwaySeasonLinkId = colorwaySeasonLinkId;
	}

	public String getCostSheetName() {
		return costSheetName;
	}

	public void setCostSheetName(String costSheetName) {
		this.costSheetName = costSheetName;
	}

	public String getCostSheetMilestone() {
		return costSheetMilestone;
	}

	public void setCostSheetMilestone(String costSheetMilestone) {
		this.costSheetMilestone = costSheetMilestone;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public AeroCostingCostSheetKey getCostSheetKey() {
		return costSheetKey;
	}

	public void setCostSheetKey(AeroCostingCostSheetKey costSheetKey) {
		this.costSheetKey = costSheetKey;
	}

	public Timestamp getCostSheetModifiedDate() {
		return costSheetModifiedDate;
	}

	public void setCostSheetModifiedDate(Timestamp costSheetModifiedDate) {
		this.costSheetModifiedDate = costSheetModifiedDate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(colorwaySeasonLinkId, costSheetId, costSheetKey, costSheetMilestone, costSheetModifiedDate,
				costSheetName, costSheetNumber, productId, productName, productNumber, sourcingColorwayLinkId,
				sourcingSeasonLinkId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AeroCostingCostSheetData other = (AeroCostingCostSheetData) obj;
		return Objects.equals(colorwaySeasonLinkId, other.colorwaySeasonLinkId)
				&& Objects.equals(costSheetId, other.costSheetId) && Objects.equals(costSheetKey, other.costSheetKey)
				&& Objects.equals(costSheetMilestone, other.costSheetMilestone)
				&& Objects.equals(costSheetModifiedDate, other.costSheetModifiedDate)
				&& Objects.equals(costSheetName, other.costSheetName)
				&& Objects.equals(costSheetNumber, other.costSheetNumber) && Objects.equals(productId, other.productId)
				&& Objects.equals(productName, other.productName) && Objects.equals(productNumber, other.productNumber)
				&& Objects.equals(sourcingColorwayLinkId, other.sourcingColorwayLinkId)
				&& Objects.equals(sourcingSeasonLinkId, other.sourcingSeasonLinkId);
	}

	@Override
	public String toString() {
		return "AeroCostingCostSheetData [costSheetId=" + costSheetId + ", costSheetNumber=" + costSheetNumber
				+ ", productId=" + productId + ", productNumber=" + productNumber + ", sourcingSeasonLinkId="
				+ sourcingSeasonLinkId + ", sourcingColorwayLinkId=" + sourcingColorwayLinkId
				+ ", colorwaySeasonLinkId=" + colorwaySeasonLinkId + ", costSheetName=" + costSheetName
				+ ", costSheetMilestone=" + costSheetMilestone + ", productName=" + productName + ", costSheetKey="
				+ costSheetKey + ", costSheetModifiedDate=" + costSheetModifiedDate + "]";
	}
	
}
