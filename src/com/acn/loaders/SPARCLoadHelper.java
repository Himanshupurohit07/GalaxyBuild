package com.acn.loaders;

import java.util.Hashtable;
import java.util.Iterator;

import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.load.LoadCommon;
import com.lcs.wc.load.LoadFlexTyped;
import com.lcs.wc.load.LoadHelper;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigLogic;
import com.lcs.wc.specification.FlexSpecLogic;
import com.lcs.wc.specification.FlexSpecification;

import wt.util.WTException;

public class SPARCLoadHelper extends LoadHelper{
	
	public static boolean save(FlexTyped flexTyped, Hashtable dataValues, String fileName) throws WTException {
		
		//--Setting attributes for the Object
		SPARCLoadHelper.setAtt(flexTyped, dataValues);
		
		String updateMode = LoadCommon.getValue(dataValues, "UPDATEMODE");
		LoadCommon.display("Saving "+flexTyped+" with Update Mode = "+updateMode);
		
		if(flexTyped instanceof LCSSourcingConfig) {
			flexTyped = new LCSSourcingConfigLogic().saveSourcingConfig((LCSSourcingConfig)flexTyped);
		}else if (flexTyped instanceof LCSSourceToSeasonLink){
			flexTyped = new LCSSourcingConfigLogic().saveSourceToSeasonLink((LCSSourceToSeasonLink)flexTyped);
		}else if(flexTyped instanceof FlexSpecification) {
			flexTyped = new FlexSpecLogic().saveSpec((FlexSpecification)flexTyped);
		}
		
		if(flexTyped==null) return false;
		//-- Set Lifecycle and State for the Object.
		if((flexTyped=LoadCommon.postLoadProcess(flexTyped, dataValues, fileName))==null)return false;
		
		return true;
		
	}

	public static boolean setAtt(FlexTyped flexTyped, Hashtable dataValues) {
		// TODO Auto-generated method stub
		LoadCommon.display("-- Setting Attributes for Object "+flexTyped.getClass().getName());
		String msg = "";
		Iterator messagesITR = LoadFlexTyped.setAttributes(flexTyped, dataValues).iterator();
		while(messagesITR!=null && messagesITR.hasNext()) {
			msg = (String)messagesITR.next();
			LoadCommon.display("-- SPARCLOADER: Setting attributes INFO : "+msg);
			if(msg.startsWith("#ERROR - The Required Attribute") || msg.startsWith("#ERROR - The Value")) {
				LoadCommon.display("-- SPARCLOADER: ERROR : OBJECT Failed to Load : "+msg);
				return false;
			}
		}
		LoadCommon.display("-- Setting Attributes OK.");
		return true;
	}

}
