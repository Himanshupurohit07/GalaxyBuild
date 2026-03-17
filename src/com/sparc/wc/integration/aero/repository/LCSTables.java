package com.sparc.wc.integration.aero.repository;

/**
 * Enumeration for out-of-the-box flex tables used in Aero queries.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Added table reference COSTSHEETTOCOLORLINK.
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development".
 */
public enum LCSTables {
	LCSSOURCINGCONFIGMASTER,
	LCSSOURCETOSEASONLINKMASTER,
	LCSPRODUCT,
	LCSSEASON,
	LCSSOURCINGCONFIG,
	LCSSOURCETOSEASONLINK,
	LCSCOSTSHEETMASTER,
	LCSPRODUCTCOSTSHEET,
	LCSSKUSOURCINGLINK,
	LCSSKU,
	LCSSKUSEASONLINK,
	LCSPRODUCTSEASONLINK,
	LCSCOLOR,
	COSTSHEETTOCOLORLINK
}
