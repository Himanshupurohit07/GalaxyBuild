package com.acn.loaders;

import java.util.Collection;
import java.util.Hashtable;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.load.LoadCommon;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigLogic;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.specification.FlexSpecHelper;
import com.lcs.wc.specification.FlexSpecLogic;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecToSeasonLink;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;

import wt.util.WTException;

public abstract class SPARCLoadBean extends LoadCommon{
	
	protected static LCSSeason getSeason(Hashtable dataValues, String fileName) {
		LCSSeason season = null;
		try {
			Hashtable seasonValues = LoadCommon.parseObjectAttributes(dataValues, null, "Season", false, true);
			season = (LCSSeason)LoadCommon.searchForObject(fileName, "Season", dataValues);
			if(season!=null) {
				return season;
			}else {
				return null;
			}
		} catch (WTException wtException) {
			// TODO Auto-generated catch block
			LoadCommon.display("#WTException : "+wtException.getLocalizedMessage());
			wtException.printStackTrace();
			LoadCommon.display("#ERROR while fetching Season Object");
			return null;
		}
	}
	
	protected static LCSProduct getProduct(Hashtable dataValues, String fileName) {
		LCSProduct prodARev = null;
		try {
			Hashtable productValues = LoadCommon.parseObjectAttributes(dataValues, null, "Product", false, true);
			prodARev = (LCSProduct)LoadCommon.searchForObject(fileName, "Product", dataValues);
			if(prodARev!=null) {
				prodARev = (LCSProduct)VersionHelper.getVersion(prodARev, "A");
				return prodARev;
			}else {
				return null;
			}
		} catch (WTException wtException) {
			// TODO Auto-generated catch block
			LoadCommon.display("#WTException : "+wtException.getLocalizedMessage());
			wtException.printStackTrace();
			LoadCommon.display("#ERROR while fetching Product Object");
			return null;
		}
	}
	
	protected static LCSSourcingConfig getExistingSourcingConfig(Hashtable dataValues, LCSProduct prodARev,
			String sourcingFlexTypePath, String factorySAPNo) throws WTException {
		// TODO Auto-generated method stub
		LCSSourcingConfig srcConfig = null;
		FlexType sourcingFlexType = FlexTypeCache.getFlexTypeFromPath(sourcingFlexTypePath);
		FlexType supplierFlexType = FlexTypeCache.getFlexTypeFromPath("Supplier\\Factory");
		String factoryAttCol = sourcingFlexType.getAttribute("scFGFactory").getColumnName();
		String sapVendorNoAttCol = supplierFlexType.getAttribute("scSAPVendorNo").getColumnName();
		
		PreparedQueryStatement pqs = new PreparedQueryStatement();
		pqs.appendFromTable("LCSSourcingConfig");
		pqs.appendFromTable("LCSSupplier");
		pqs.appendSelectColumn("LCSSourcingConfig", "branchIditerationInfo");
		pqs.appendJoin("LCSSourcingConfig", factoryAttCol, "LCSSupplier", "branchIditerationInfo");
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSSupplier", sapVendorNoAttCol, factorySAPNo, Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSSourcingConfig", "productARevId", FormatHelper.getNumericVersionIdFromObject(prodARev), Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSSupplier", "latestIterationInfo", "1", Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSSourcingConfig", "latestIterationInfo", "1", Criteria.EQUALS));
		
		LoadCommon.display("-- INFO: Query to Search existing Source : "+pqs.toString());
		SearchResults srcResults = LCSQuery.runDirectQuery(pqs);
		Collection srcColl = srcResults.getResults();
		if(srcColl!=null && srcColl.size()==1) {
			FlexObject fob = (FlexObject)srcColl.iterator().next();
			srcConfig = (LCSSourcingConfig) LCSQuery.findObjectById("VR:com.lcs.wc.sourcing.LCSSourcingConfig:"
							+fob.getString("LCSSOURCINGCONFIG.BRANCHIDITERATIONINFO"));
		}
		
		return srcConfig;
	}
	
	protected static FlexSpecification getExistingSpecForSource(FlexType specifcationFlexType,
			LCSSourcingConfig srcConfig, Hashtable specValue) throws WTException {
		// TODO Auto-generated method stub
		FlexSpecification flexSpec = null;
		String srcConfigMasterId = FormatHelper.getNumericObjectIdFromObject((LCSSourcingConfigMaster)srcConfig.getMaster());
		String specMasterAttCol = specifcationFlexType.getAttribute("glSpecMasterId").getColumnName();
		String specMasterValue1 = (String)specValue.get("FlexSpecflexAttglSpecMasterId");
		String specMasterValue2 = (String)specValue.get("flexAttglSpecMasterId");
		
		System.out.println("specMasterValue1: "+specMasterValue1);
		System.out.println("specMasterValue2: "+specMasterValue2);
		
		
		PreparedQueryStatement pqs = new PreparedQueryStatement();
		pqs.appendFromTable("FlexSpecification");
		pqs.appendSelectColumn("FlexSpecification", "branchIditerationInfo");
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("FlexSpecification", "idA3B12", srcConfigMasterId, Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("FlexSpecification", "latestIterationInfo", "1", Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("FlexSpecification", specMasterAttCol, specMasterValue2, Criteria.EQUALS));
		
		LoadCommon.display("-- INFO: Query to Search existing Spec : "+pqs.toString());
		SearchResults srcResults = LCSQuery.runDirectQuery(pqs);
		Collection srcColl = srcResults.getResults();
		if(srcColl!=null && srcColl.size()==1) {
			FlexObject fob = (FlexObject)srcColl.iterator().next();
			flexSpec = (FlexSpecification) LCSQuery.findObjectById("VR:com.lcs.wc.specification.FlexSpecification:"
							+fob.getString("FLEXSPECIFICATION.BRANCHIDITERATIONINFO"));
		}
		
		return flexSpec;
	}
	
	protected static void createObjectToSeasonLinK(Hashtable dataValues, Object obj,
			LCSSeason season, String fileName, boolean isPrimary) throws WTException {
		// TODO Auto-generated method stub
		if(obj instanceof LCSSourcingConfig) {
			LCSSourcingConfig srcConfig = (LCSSourcingConfig)obj;
			LCSSourcingConfigLogic srcConfigLogic = new LCSSourcingConfigLogic();
			Hashtable stslValues = LoadCommon.parseObjectAttributes(dataValues, null, "STSL", false, true);
			PreparedQueryStatement pqs = LCSSourcingConfigQuery.getSourceToSeasonQuery(srcConfig.getMaster(), season.getMaster());
			SearchResults srcResults = LCSQuery.runDirectQuery(pqs);
			Collection srcColl = srcResults.getResults();
			if(srcColl==null || (srcColl!=null && srcColl.size()==0)){
				LCSSourceToSeasonLink stsl = srcConfigLogic.createSourceToSeasonLink(srcConfig, season);
				SPARCLoadHelper.save(stsl, stslValues, fileName);
				srcConfigLogic.createNewSKUSourcingLinks(stsl);
				LoadCommon.display("-- INFO: Associated Sourcing Config to Season and created New SKUSourcingLinks!!");
			}else if(srcColl!=null && srcColl.size()==1) {
				LoadCommon.display("-- INFO: Association to Season already exists, updating STSL attributes");
				FlexObject fob = (FlexObject)srcColl.iterator().next();
				//LoadCommon.display("-- INFO: STSL FOB : "+fob);
				LCSSourceToSeasonLink existingSTSL = (LCSSourceToSeasonLink)LCSQuery
						.findObjectById("VR:com.lcs.wc.sourcing.LCSSourceToSeasonLink:"+fob.getString("LCSSOURCETOSEASONLINK.BRANCHIDITERATIONINFO"));
				SPARCLoadHelper.save(existingSTSL, stslValues, fileName);
				srcConfigLogic.createNewSKUSourcingLinks(existingSTSL);
			}
		}else if(obj instanceof FlexSpecification) {
			FlexSpecification flexSpec = (FlexSpecification)obj;
			FlexSpecToSeasonLink flexSpecToSeasonLink = FlexSpecQuery.findSpecToSeasonLink(flexSpec.getMaster(), season.getMaster());
			System.out.println("flexSpecToSeasonLink: "+flexSpecToSeasonLink);
			if (flexSpecToSeasonLink == null) {
				FlexSpecToSeasonLink fstsl = FlexSpecHelper.service.addSpecToSeason(flexSpec.getMaster(),
						(LCSSeasonMaster) season.getMaster(), isPrimary);
				LoadCommon.display("-- INFO: Associated FlexSpec to Season!!");
				/*
				 * FlexSpecLogic specLogic = new FlexSpecLogic(); FlexSpecToSeasonLink fstl=
				 * specLogic.addSpecToSeason(flexSpec.getMaster(), season.getMaster());
				 * fstl.setPrimarySpec(isPrimary);
				 * LoadCommon.display("-- INFO: Associated FlexSpec to Season!!"); if(isPrimary)
				 * FlexSpecHelper.service.setAsPrimarySpec(fstl);
				 */
				// specLogic.setAsPrimarySpec(fstl);
			}else {
				
				if(isPrimary) {
					FlexSpecHelper.service.setAsPrimarySpec(flexSpecToSeasonLink);
				}
				
				
			}
		}
	}

}
