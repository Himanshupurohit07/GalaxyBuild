package com.sparc.wc.integration.aero.repository;

/**
 * Enumeration for out-of-the-box flex object references.
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 */
public enum LCSObjectReferences {
	LCSSOURCINGCONFIG("OR:com.lcs.wc.sourcing.LCSSourcingConfig:"),
	LCSPRODUCT("OR:com.lcs.wc.product.LCSProduct:"),
	LCSSEASON("OR:com.lcs.wc.season.LCSSeason:"),
	LCSPRODUCTCOSTSHEET("OR:com.lcs.wc.sourcing.LCSProductCostSheet:"),
	LCSSKU("OR:com.lcs.wc.product.LCSSKU:"),
	LCSSOURCETOSEASONLINK("OR:com.lcs.wc.sourcing.LCSSourceToSeasonLink:"),
	LCSSKUSOURCINGLINK("OR:com.lcs.wc.sourcing.LCSSKUSourcingLink:"),
	LCSSKUSEASONLINK("OR:com.lcs.wc.season.LCSSKUSeasonLink:"),
	LCSPRODUCTSEASONLINK("OR:com.lcs.wc.season.LCSProductSeasonLink:");
	
	private String objRef;
	
	LCSObjectReferences(String objRef) {
		this.objRef = objRef;
	}
	
	public String getObjRefString() {
		return objRef;
	}
}
