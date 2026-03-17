package com.acn.loaders;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.load.LoadCommon;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.ReferencedTypeKeys;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.specification.FlexSpecMaster;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.FormatHelper;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class SPARCFlexSpecLoad extends SPARCLoadBean {
	
	public static boolean loadSpec(Hashtable dataValues, Hashtable commandLine, Vector returnobjects) {
		return loadSpec(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
	}

	@SuppressWarnings("rawtypes")
	protected static boolean loadSpec(Hashtable dataValues, String fileName) {
		// TODO Auto-generated method stub
		try {
			LoadCommon.display(" ********* CUSTOM SPEC LOADER : START ******************");
			LCSSeason season = getSeason(dataValues, fileName);
			if (season != null) {
				LoadCommon.display("-- INFO : Season found : " + season);
				LCSProduct prodARev = getProduct(dataValues, fileName);
				LoadCommon.display("-- INFO : Product found : " + prodARev);
				if (prodARev != null) {
					String sourcingFlexTypePath = (String) dataValues.get("Type");
					String sourceMasterID = (String) dataValues.get("sourceMasterId");
					// -- Fetch SourcingConfig Object
					LCSSourcingConfig srcConfig = null;
					LCSSourcingConfig existingSrcConfig = getSource(dataValues, prodARev,
							sourcingFlexTypePath, sourceMasterID);
					if (existingSrcConfig != null) {
						LoadCommon.display("-- INFO : Source Found");
						srcConfig = existingSrcConfig;
						//-- Creating new Specification.
						loadFlexSpec(dataValues, fileName, srcConfig, prodARev, season);
					} else {
						LoadCommon.display(" -- #ERROR : Source Cannot be Null ");
						return false;
					}
				} else {
					LoadCommon.display(" -- #ERROR : Product Cannot be Null ");
					return false;
				}

			} else {
				LoadCommon.display(" -- #ERROR : Season Cannot be Null ");
				return false;
			}
			LoadCommon.display(" ********* CUSTOM SPEC LOADER : END ******************");
			return true;
		} catch (WTException | WTPropertyVetoException e) {
			// TODO Auto-generated catch block
			LoadCommon.display(" #WTException : "+e.getLocalizedMessage());
			e.printStackTrace();
			LoadCommon.display(" #ERROR : during creation  FlexSpec.");
			return false;
		}
	}

	protected static boolean loadFlexSpec(Hashtable dataValues, String fileName, LCSSourcingConfig srcConfig,
			LCSProduct prodARev, LCSSeason season) throws WTException, WTPropertyVetoException {

		Hashtable specValues = LoadCommon.parseObjectAttributes(dataValues, null, "FlexSpec", false, true);
		FlexType specifcationFlexType = prodARev.getFlexType().getReferencedFlexType(ReferencedTypeKeys.SPEC_TYPE);
		FlexSpecification existingFlexSpec = null;
		FlexSpecification flexSpec = null;
		existingFlexSpec = getExistingSpecForSource(specifcationFlexType, srcConfig, specValues);
		if (existingFlexSpec != null) {
			LoadCommon.display("-- INFO : FlexSpecification Exists, using the same..");
			flexSpec = existingFlexSpec;
		} else {
			LoadCommon.display("-- INFO : Creating New Flex Specification..");

			flexSpec = FlexSpecification.newFlexSpecification();
			flexSpec.setFlexType(specifcationFlexType);
			flexSpec.setSpecOwner(prodARev.getMaster());
			flexSpec.setSpecSource(srcConfig.getMaster());
			flexSpec.setMaster(new FlexSpecMaster());
			
		}

		// -- Saving FlexSpecification
		LoadCommon.display("-- INFO : Saving FlexSpecification..");
		SPARCLoadHelper.save(flexSpec, specValues, fileName);
		LoadCommon.display("-- INFO : FlexSpecification Saved!!");

		// -- Adding FlexSpec To Season
		LoadCommon.display("-- INFO : Associating Flex Specification to Season..");
		Boolean isPrimary = Boolean.valueOf((String) dataValues.get("isPrimary"));
		LoadCommon.display("-- INFO : isPrimary Spec ---"+isPrimary);
		createObjectToSeasonLinK(dataValues, flexSpec, season, fileName, isPrimary);

		return true;

	}
	
	protected static LCSSourcingConfig getSource(Hashtable dataValues, LCSProduct prodARev,
			String sourcingFlexTypePath, String sourceMasterID) throws WTException {
		// TODO Auto-generated method stub
		LCSSourcingConfig srcConfig = null;
		FlexType sourcingFlexType = FlexTypeCache.getFlexTypeFromPath(sourcingFlexTypePath);
		String sourceMasterAttCol = sourcingFlexType.getAttribute("glSourceMasterID").getColumnName();
		
		PreparedQueryStatement pqs = new PreparedQueryStatement();
		pqs.appendSelectColumn("LCSSourcingConfig", "branchIditerationInfo");
		pqs.appendFromTable("LCSSourcingConfig");
		pqs.appendCriteria(new Criteria("LCSSourcingConfig", "productARevId", FormatHelper.getNumericVersionIdFromObject(prodARev), Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSSourcingConfig", "latestIterationInfo", "1", Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSSourcingConfig", sourceMasterAttCol, sourceMasterID, Criteria.EQUALS));
		
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

}
