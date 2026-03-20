package com.sparc.wc.source;

import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.supplier.LCSSupplier;

import wt.fc.PersistenceHelper;
import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class GalaxyCountryFOO {
	static LCSSupplier vendorObj = null;
	static LCSCountry countryObj = null;
   
	public static void FillCOO(WTObject wtObject) throws WTException, WTPropertyVetoException {
		LCSSourcingConfig source = null;
		System.out.println("wtobject---------------"+wtObject.getDisplayType());
		vendorObj = (LCSSupplier) ((LCSSourcingConfig) wtObject).getValue("scFGFactory");
		if (vendorObj != null && vendorObj.getName() != null) {
		System.out.println("vendor name-------------------------"+vendorObj.getName());
		countryObj =  (LCSCountry) vendorObj.getValue("scCountry");
		System.out.println("country---------------------"+countryObj.getName());
		//String country = countryObj.getName();
		source = (LCSSourcingConfig) wtObject;
		source.setValue("scFactoryCountryOrigin", countryObj);
		source = (LCSSourcingConfig) PersistenceHelper.manager.save(source);
		}
	}
}
