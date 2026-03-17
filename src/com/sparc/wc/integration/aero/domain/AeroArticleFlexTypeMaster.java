package com.sparc.wc.integration.aero.domain;

import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;

/**
 * Container for the relevant flex type objects that provides attribute values required to populate the Article payload for the Aero Integration to S4 & CAP.
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 */
public class AeroArticleFlexTypeMaster {
	
	private LCSSKU colorway;
	private LCSProduct product;
	private LCSSeason season;
	private LCSProductSeasonLink productSeasonLink;
	private LCSSKUSeasonLink colorwaySeasonLink;

	public AeroArticleFlexTypeMaster() {
		
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
	
	public LCSSeason getSeason() {
		return season;
	}

	public void setSeason(LCSSeason season) {
		this.season = season;
	}

	public LCSProductSeasonLink getProductSeasonLink() {
		return productSeasonLink;
	}

	public void setProductSeasonLink(LCSProductSeasonLink productSeasonLink) {
		this.productSeasonLink = productSeasonLink;
	}

	public LCSSKUSeasonLink getColorwaySeasonLink() {
		return colorwaySeasonLink;
	}

	public void setColorwaySeasonLink(LCSSKUSeasonLink colorwaySeasonLink) {
		this.colorwaySeasonLink = colorwaySeasonLink;
	}

	@Override
	public String toString() {
		return "AeroArticleFlexTypeMaster [colorway=" + colorway + ", product=" + product + ", season=" + season
				+ ", productSeasonLink=" + productSeasonLink + ", colorwaySeasonLink=" + colorwaySeasonLink + "]";
	}
	
}
