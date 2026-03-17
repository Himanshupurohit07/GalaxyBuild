package com.sparc.wc.migration.loader;

import java.util.Hashtable;

import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.load.LoadCommon;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.ReferencedTypeKeys;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.specification.FlexSpecMaster;
import com.lcs.wc.specification.FlexSpecification;
import com.sparc.wc.migration.loader.util.SPARCLoadHelper;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class SPARCFlexSpecLoad extends SPARCLoadBean {
	
	protected static boolean loadFlexSpec(Hashtable dataValues, String fileName,
			LCSSourcingConfig srcConfig, LCSProduct prodARev, LCSSeason season) throws WTException, WTPropertyVetoException {
		
		Hashtable specValues = LoadCommon.parseObjectAttributes(dataValues, null, "FlexSpec", false, true);
		FlexType specifcationFlexType = prodARev.getFlexType().getReferencedFlexType(ReferencedTypeKeys.SPEC_TYPE);
		FlexSpecification existingFlexSpec = null;
		FlexSpecification flexSpec = null;
		existingFlexSpec = getExistingSpecForSource(specifcationFlexType, srcConfig, specValues);
		if(existingFlexSpec!=null) {
			LoadCommon.display("-- INFO : FlexSpecification Exists, using the same..");
			flexSpec = existingFlexSpec;
		}else {
			LoadCommon.display("-- INFO : Creating New Flex Specification..");
			
			flexSpec = FlexSpecification.newFlexSpecification();
			flexSpec.setFlexType(specifcationFlexType);
			flexSpec.setSpecOwner(prodARev.getMaster());
			flexSpec.setSpecSource(srcConfig.getMaster());
			flexSpec.setMaster(new FlexSpecMaster());
		}	
	
		//-- Saving FlexSpecification
		LoadCommon.display("-- INFO : Saving FlexSpecification..");
		SPARCLoadHelper.save(flexSpec, specValues, fileName);
		LoadCommon.display("-- INFO : FlexSpecification Saved!!");			
		
		//-- Adding FlexSpec To Season
		LoadCommon.display("-- INFO : Associating Flex Specification to Season..");
		createObjectToSeasonLinK(dataValues, flexSpec, season, fileName);
		
		return true;
		
	}

}
