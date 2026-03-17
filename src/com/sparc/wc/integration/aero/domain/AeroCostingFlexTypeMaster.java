package com.sparc.wc.integration.aero.domain;

import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSKUSourcingLink;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;

/**
 * Container for the relevant flex type objects that provides attribute values required to populate the Costing payload for the Aero Integration to S4 & CAP.
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9646 Aero - Costing Interface"
 */
public class AeroCostingFlexTypeMaster {
	
	private LCSSKU colorway;
	private LCSProduct product;
	private LCSProductCostSheet productCostSheet;
	private LCSSeason season;
	private LCSSourcingConfig sourcingConfig;
	private LCSSourceToSeasonLink sourcingSeasonLink;
	private LCSSKUSourcingLink sourcingColorwayLink;
	private LCSSKUSeasonLink colorwaySeasonLink;
	
	public AeroCostingFlexTypeMaster() {
		
	}

	public LCSSKU getColorway() {
		return colorway;
	}

	public void setColorway(LCSSKU colorway) {
		this.colorway = colorway;
	}

	public LCSProduct getProduct() {
		return product;
	}

	public void setProduct(LCSProduct product) {
		this.product = product;
	}

	public LCSProductCostSheet getProductCostSheet() {
		return productCostSheet;
	}

	public void setProductCostSheet(LCSProductCostSheet productCostSheet) {
		this.productCostSheet = productCostSheet;
	}

	public LCSSeason getSeason() {
		return season;
	}

	public void setSeason(LCSSeason season) {
		this.season = season;
	}

	public LCSSourcingConfig getSourcingConfig() {
		return sourcingConfig;
	}

	public void setSourcingConfig(LCSSourcingConfig sourcingConfig) {
		this.sourcingConfig = sourcingConfig;
	}

	public LCSSourceToSeasonLink getSourcingSeasonLink() {
		return sourcingSeasonLink;
	}

	public void setSourcingSeasonLink(LCSSourceToSeasonLink sourcingSeasonLink) {
		this.sourcingSeasonLink = sourcingSeasonLink;
	}

	public LCSSKUSourcingLink getSourcingColorwayLink() {
		return sourcingColorwayLink;
	}

	public void setSourcingColorwayLink(LCSSKUSourcingLink sourcingColorwayLink) {
		this.sourcingColorwayLink = sourcingColorwayLink;
	}

	public LCSSKUSeasonLink getColorwaySeasonLink() {
		return colorwaySeasonLink;
	}

	public void setColorwaySeasonLink(LCSSKUSeasonLink colorwaySeasonLink) {
		this.colorwaySeasonLink = colorwaySeasonLink;
	}

	@Override
	public String toString() {
		return "AeroCostingFlexTypeMaster [colorway=" + colorway + ", product=" + product + ", productCostSheet="
				+ productCostSheet + ", season=" + season + ", sourcingConfig=" + sourcingConfig
				+ ", sourcingSeasonLink=" + sourcingSeasonLink + ", sourcingColorwayLink=" + sourcingColorwayLink
				+ ", colorwaySeasonLink=" + colorwaySeasonLink + "]";
	}
	
}
