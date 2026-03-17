package com.sparc.wc.migration.loader;

import java.util.Hashtable;
import java.util.Vector;

import com.lcs.wc.load.LoadCommon;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.ReferencedTypeKeys;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.sparc.wc.migration.loader.util.SPARCLoadHelper;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class SPARCSourceLoad extends SPARCLoadBean {
	
	public static boolean loadSource(Hashtable dataValues, Hashtable commandLine, Vector returnobjects) {
		return loadSource(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
	}

	protected static boolean loadSource(Hashtable dataValues, String fileName) {
		// TODO Auto-generated method stub
		try {
			LoadCommon.display(" ********* CUSTOM SOURCE LOADER : START ******************");
			LCSSeason season = getSeason(dataValues, fileName);
			if(season!=null) {
				LoadCommon.display("-- INFO : Season found : "+season);
				LCSProduct prodARev = getProduct(dataValues, fileName);
				LoadCommon.display("-- INFO : Product found : "+prodARev);
				if(prodARev!=null) {
					String sourcingFlexTypePath = (String)dataValues.get("Type");
					String factorySAPNo = (String)dataValues.get("flexAttscFGFactory=scSAPVendorNo");
					//-- Fetch SourcingConfig Object
					LCSSourcingConfig srcConfig = null;
					LCSSourcingConfig existingSrcConfig = getExistingSourcingConfig(dataValues, prodARev, sourcingFlexTypePath, factorySAPNo);
					if(existingSrcConfig!=null) {
						LoadCommon.display("-- INFO : Sourcing Config Already Exists, using the same..");
						srcConfig = existingSrcConfig; 
					}else {//-- Create new Sourcing Config
						LoadCommon.display("-- INFO : Creating New Sourcing Config..");
						
						srcConfig = LCSSourcingConfig.newLCSSourcingConfig();
						LCSSourcingConfigMaster srcConfigMaster = new LCSSourcingConfigMaster();
						srcConfigMaster.setProductMaster(prodARev.getMaster());
						srcConfigMaster.setProductARevId(prodARev.getProductARevId());
						srcConfig.setFlexType(prodARev.getFlexType().getReferencedFlexType(ReferencedTypeKeys.SOURCING_CONFIG_TYPE));
						srcConfig.setMaster(srcConfigMaster);
						srcConfig.setProductMaster(prodARev.getMaster());
						srcConfig.setProductARevId(prodARev.getProductARevId());
						srcConfig.setPrimarySource(false);
						
						String sourcingName = "MMA Some Source Name";
						srcConfig.setSourcingConfigName(sourcingName);
						srcConfig.setValue("name",sourcingName);
						dataValues.remove("flexAttname");
						
					}
					//-- Saving Sourcing Config.
					LoadCommon.display("-- INFO : Saving Sourcing Config..");
					SPARCLoadHelper.save(srcConfig, dataValues, fileName);
					LoadCommon.display("-- INFO : Sourcing Config Saved!!");					

					//-- Adding Source TO Season.
					LoadCommon.display("-- INFO : Adding Sourcing Config TO Season..");
					createObjectToSeasonLinK(dataValues, srcConfig, season, fileName);
					
					//-- Creating new Specification.
					SPARCFlexSpecLoad.loadFlexSpec(dataValues, fileName, srcConfig, prodARev, season);
					
				}else {
					LoadCommon.display(" -- #ERROR : Product Cannot be Null ");
					return false;
				}
				
			}else {
				LoadCommon.display(" -- #ERROR : Season Cannot be Null ");
				return false;
			}
			
			LoadCommon.display(" ********* CUSTOM SOURCE LOADER : END ******************");
			return true;
		} catch (WTException | WTPropertyVetoException e) {
			// TODO Auto-generated catch block
			LoadCommon.display(" #WTException : "+e.getLocalizedMessage());
			e.printStackTrace();
			LoadCommon.display(" #ERROR : during creation Sourcing Config.");
			return false;
		}
	}

}
