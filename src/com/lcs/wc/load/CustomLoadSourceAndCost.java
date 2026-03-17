package com.lcs.wc.load;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.product.ReferencedTypeKeys;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.*;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

import static com.sparc.wc.util.SparcCostingConstants.PLM_PROD_NO;
import static com.sparc.wc.util.SparcCostingConstants.SKU_STR_VR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wt.fc.PersistenceHelper;
import wt.method.MethodContext;
import wt.part.WTPartMaster;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class CustomLoadSourceAndCost {
	private static final Logger LOGGER = LogManager.getLogger(LoadSourceAndCost.class.getName());
	static final String TRUE = "true";
	public static final String FOLDERLOCATION = LCSProperties.get("com.lcs.wc.sourcing.LCSSourcingConfig.rootFolder",
			"/SourcingConfig");

	public static final String SPEC_TYPE = "SPEC_TYPE_ID";
	public CustomLoadSourceAndCost() {
	}

	
	protected static boolean createSourcingConfig(LCSProduct product, LCSSeason season,
			Hashtable<String, Object> dataValues, String fileName, String sourceid) {
		boolean status = false;
		LCSSourcingConfig sc = null;
		LCSSourcingConfig sc2 = null;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("\t data values \n" + dataValues + "\n");
		}
		

		try {
			sc = getLCSSourcingConfig(product, dataValues, sourceid);
			if (sc != null) {
				System.out.println("sc Name--->" + sc.getName());
			}
		} catch (WTException var18) {
			var18.printStackTrace();
			LoadCommon.display("\n#WTException : " + var18.getLocalizedMessage());
		} catch (WTPropertyVetoException e) {
		
			e.printStackTrace();
		}

		sc = (LCSSourcingConfig) LoadCommon.postLoadProcess(sc, dataValues, fileName);
		String value = (String) dataValues.get("STSLPrimary");
		boolean seasonPrimary = "true".equals(value);
		if (sc != null) {
			System.out.println("-----if condition--- because same source is already present----");
			sc.setValue("glSourceMasterID", sourceid);
			LoadCommon.putCache(fileName, "CURRENT_SOURCING_CONFIG", sc);
			LoadCommon.putCache(fileName, "CURRENT_FLEXTYPED", sc);
           try {
				LCSSourceToSeasonLink link = (new LCSSourcingConfigQuery()).getSourceToSeasonLink(sc, season);
				if (link == null) {
					System.out.println("----Creating new source to season Link----");
					link = LoadLinePlan.SOURCINGCONFIG_LOGIC.createSourceToSeasonLink(sc, season);
				     System.out.println("link--->"+link.isPrimarySTSL());
				}
				
//				if (seasonPrimary&&!link.isPrimarySTSL()) {
//					System.out.println("----if condition--season Primary-----");
//					System.out.println("link-->"+link.isPrimarySTSL());
//					LoadLinePlan.SOURCINGCONFIG_LOGIC.setAsPrimary(link);
				    System.out.println("link--->"+link.isPrimarySTSL());
					link = (LCSSourceToSeasonLink) PersistenceHelper.manager.refresh(link);
				//}
				LoadCommon.putCache(fileName, "CURRENT_SOURCETOSEASONLINK", link);
				return true;
			} catch (Exception var15) {
				var15.printStackTrace();
				return false;
			}
		} else {
			
			System.out.println("---else -------- creating a new one---");
			try {
				sc = LCSSourcingConfig.newLCSSourcingConfig();
				LCSSourcingConfigMaster scMaster = new LCSSourcingConfigMaster();
				scMaster.setProductMasterReference(product.getMasterReference());
				sc.setMaster(scMaster);
				value = (String) dataValues.get("Primary");
				boolean primary = "true".equals(value);
				value = (String) dataValues.get("Type");
			  FlexType flexType = FlexTypeCache.getFlexTypeFromPath(value);
				sc.setFlexType(flexType);
				String name = (String) dataValues.get("flexAttname");
				String number = (String) dataValues.get("flexAttnumber");
				sc.setSourcingConfigName(name);
				sc.setValue("name", name);
				sc.setValue("number", number);
				sc.setValue("glSourceMasterID", sourceid);
				sc.setProductARevId(product.getProductARevId());
				sc.setProductSeasonRevId(Double.parseDouble(FormatHelper.getNumericVersionIdFromObject(product)));
				sc.setSeasonRevId(product.getSeasonRevId());
				scMaster.setProductARevId(product.getProductARevId());
				scMaster.setProductSeasonRevId(Double.parseDouble(FormatHelper.getNumericVersionIdFromObject(product)));
				scMaster.setSeasonRevId(product.getSeasonRevId());
				MethodContext methodContext = MethodContext.getContext();
				methodContext.put("LOAD_IN_PROGRESS", "true");
				status = LoadHelper.save(sc, dataValues, fileName);
				methodContext.remove("LOAD_IN_PROGRESS");
				System.out.println("sc----->" + sc.getName());
				

				if (primary && !sc.isPrimarySource()) {
					// System.out.println("-----SET Primary S True ----");
					sc2 = getLCSSourcingConfig2(product, dataValues);
					sc2.setPrimarySource(false);
					sc.setPrimarySource(true);
					LoadLinePlan.SOURCINGCONFIG_LOGIC.setAsPrimary(sc);
				}

				if (!primary && sc.isPrimarySource()) {
					// System.out.println("--SET Primary S False ----");
					sc.setPrimarySource(false);
				}
				// System.out.println("Check Source Primary Status--->"+sc.isPrimarySource());

				LoadCommon.putCache(fileName, "CURRENT_SOURCING_CONFIG", sc);

				LCSSourceToSeasonLink link = LoadLinePlan.SOURCINGCONFIG_LOGIC.createSourceToSeasonLink(sc, season);
				if (seasonPrimary && !link.isPrimarySTSL()) {
					//LoadLinePlan.SOURCINGCONFIG_LOGIC.setAsPrimary(link);
					link.setPrimarySTSL(true);
					link = (LCSSourceToSeasonLink) PersistenceHelper.manager.refresh(link);
				}
				if (!seasonPrimary && link.isPrimarySTSL()) {
					link.setPrimarySTSL(false);
				}

				LoadCommon.putCache(fileName, "CURRENT_SOURCING_CONFIG", sc);
				LoadCommon.putCache(fileName, "CURRENT_SOURCETOSEASONLINK", link);
			} catch (WTException var16) {
				var16.printStackTrace();
				LoadCommon.display("\n#WTException : " + var16.getLocalizedMessage());
			} catch (WTPropertyVetoException var17) {
				var17.printStackTrace();
				LoadCommon.display("\n#WTPropertyVetoException : " + var17.getLocalizedMessage());
			}
			

			return status;
		}
	}

	protected static LCSSourcingConfig getExistingSourcingConfig(LCSProduct prod, String sourceMasterid)
			throws WTException {
		LCSSourcingConfig srcConfig = null;
		FlexType sourcingFlexType = prod.getFlexType().getReferencedFlexType("SOURCING_CONFIG_TYPE_ID");
		String scSourceMasteCol = sourcingFlexType.getAttribute("scMasterId").getColumnName();

		PreparedQueryStatement pqs = new PreparedQueryStatement();
		pqs.appendFromTable("LCSSourcingConfig");
		pqs.appendSelectColumn("LCSSourcingConfig", "branchIditerationInfo");
		pqs.appendAndIfNeeded();

		pqs.appendCriteria(new Criteria("LCSSourcingConfig", scSourceMasteCol, sourceMasterid, Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSSourcingConfig", "productARevId",
				FormatHelper.getNumericVersionIdFromObject(prod), Criteria.EQUALS));

		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSSourcingConfig", "latestIterationInfo", "1", Criteria.EQUALS));

		LoadCommon.display("-- INFO: Query to Search existing Source : " + pqs.toString());
		SearchResults srcResults = LCSQuery.runDirectQuery(pqs);
		Collection srcColl = srcResults.getResults();
		if (srcColl != null && srcColl.size() == 1) {
			FlexObject fob = (FlexObject) srcColl.iterator().next();
			srcConfig = (LCSSourcingConfig) LCSQuery.findObjectById("VR:com.lcs.wc.sourcing.LCSSourcingConfig:"
					+ fob.getString("LCSSOURCINGCONFIG.BRANCHIDITERATIONINFO"));
		}

		return srcConfig;
	}

	protected static LCSSourcingConfig getLCSSourcingConfig2(LCSProduct product, Hashtable<String, Object> dataValues)
			throws WTPropertyVetoException, WTException {
		FlexType sourceType = product.getFlexType().getReferencedFlexType("SOURCING_CONFIG_TYPE_ID");
		Hashtable<String, Object> searchTable = LoadCommon.getCriteria(dataValues, ":SearchCriteria");
		LCSPartMaster productMaster = product.getMaster();
		String productMasterId = LCSQuery.getNumericFromOid(FormatHelper.getObjectId(productMaster));

		String primary = "true";

		Long prmry = null;
		if (primary != null) {
			prmry = primary.equals("true") ? new Long(1L) : new Long(0L);

		}

		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.setDistinct(true);
		statement.appendFromTable(LCSSourcingConfigMaster.class);
		statement.appendFromTable(LCSSourcingConfig.class);
		statement.appendFromTable(LCSSourceToSeasonLinkMaster.class);
		statement.appendSelectColumn(new QueryColumn(LCSSourcingConfig.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSSourcingConfigMaster.class, "productMasterReference.key.id"), "?", "="),
				new Long(productMasterId));
		if (prmry != null) {
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria(new QueryColumn(LCSSourcingConfigMaster.class, "primarySource"), "?", "="), prmry);
		}

		statement.appendAndIfNeeded();
		statement.appendJoin(new QueryColumn(LCSSourceToSeasonLinkMaster.class, "sourcingConfigMasterReference.key.id"),
				new QueryColumn(LCSSourcingConfigMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSSourcingConfig.class, "iterationInfo.latest"), "1", "="));
		Set<String> set = searchTable.keySet();
		Iterator var11 = set.iterator();

		while (var11.hasNext()) {
			String key = (String) var11.next();
			statement.appendAndIfNeeded();
			String value = (String) searchTable.get(key);
			key = sourceType.getAttribute(key).getColumnDescriptorName();
			statement.appendCriteria(new Criteria(new QueryColumn(LCSSourcingConfig.class, key), "?", "="), value);
		}

		statement.appendJoin(new QueryColumn(LCSSourcingConfig.class, "masterReference.key.id"),
				new QueryColumn(LCSSourcingConfigMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		System.out.println(statement);
		Collection<?> results = LCSQuery.runDirectQuery(statement).getResults();
		System.out.println("we have " + results.size() + " source configs");
		Iterator<?> it = results.iterator();
		FlexObject fo = null;
		LCSSourcingConfig sc = null;
		if (it != null && it.hasNext()) {
			fo = (FlexObject) it.next();
		}

		if (fo != null) {
			sc = (LCSSourcingConfig) LCSQuery.findObjectById(
					"OR:com.lcs.wc.sourcing.LCSSourcingConfig:" + fo.getString("LCSSOURCINGCONFIG.IDA2A2"), false);
		}

		return sc;
	}

	
	protected static LCSSourcingConfig getLCSSourcingConfig3(LCSProduct product, Hashtable<String, Object> dataValues)throws WTPropertyVetoException, WTException {
		FlexType sourceType = product.getFlexType().getReferencedFlexType("SOURCING_CONFIG_TYPE_ID");
		Hashtable<String, Object> searchTable = LoadCommon.getCriteria(dataValues, ":SearchCriteria");
		LCSPartMaster productMaster = product.getMaster();
		String productMasterId = LCSQuery.getNumericFromOid(FormatHelper.getObjectId(productMaster));

	    PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.setDistinct(true);
		statement.appendFromTable(LCSSourcingConfigMaster.class);
		statement.appendFromTable(LCSSourcingConfig.class);
		statement.appendFromTable(LCSSourceToSeasonLinkMaster.class);
		statement.appendSelectColumn(new QueryColumn(LCSSourcingConfig.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendCriteria(new Criteria(new QueryColumn(LCSSourcingConfigMaster.class, "productMasterReference.key.id"), "?", "="),new Long(productMasterId));
		statement.appendAndIfNeeded();
		statement.appendJoin(new QueryColumn(LCSSourceToSeasonLinkMaster.class, "sourcingConfigMasterReference.key.id"),new QueryColumn(LCSSourcingConfigMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSSourcingConfig.class, "iterationInfo.latest"), "1", "="));
		Set<String> set = searchTable.keySet();
		Iterator var11 = set.iterator();
        while (var11.hasNext()) {
			String key = (String) var11.next();
			statement.appendAndIfNeeded();
			String value = (String) searchTable.get(key);
			key = sourceType.getAttribute(key).getColumnDescriptorName();
			statement.appendCriteria(new Criteria(new QueryColumn(LCSSourcingConfig.class, key), "?", "="), value);
		}

		statement.appendJoin(new QueryColumn(LCSSourcingConfig.class, "masterReference.key.id"),new QueryColumn(LCSSourcingConfigMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		System.out.println(statement);
		Collection<?> results = LCSQuery.runDirectQuery(statement).getResults();
		System.out.println("we have " + results.size() + " source configs");
		Iterator<?> it = results.iterator();
		FlexObject fo = null;
		LCSSourcingConfig sc = null;
		if (it != null && it.hasNext()) {
			fo = (FlexObject) it.next();
		}

		if (fo != null) {
			sc = (LCSSourcingConfig) LCSQuery.findObjectById("OR:com.lcs.wc.sourcing.LCSSourcingConfig:" + fo.getString("LCSSOURCINGCONFIG.IDA2A2"), false);
		}

		return sc;
	}
	
	
	protected static LCSSourcingConfig getLCSSourcingConfig(LCSProduct product, Hashtable<String, Object> dataValues,String MasterID) throws WTPropertyVetoException, WTException {
		System.out.println("MasterID-->"+MasterID);
		FlexType sourceType = product.getFlexType().getReferencedFlexType("SOURCING_CONFIG_TYPE_ID");
		Hashtable<String, Object> searchTable = LoadCommon.getCriteria(dataValues, ":SearchCriteria");
		LCSPartMaster productMaster = product.getMaster();
		String productMasterId = LCSQuery.getNumericFromOid(FormatHelper.getObjectId(productMaster));
		String scSourceMasteCol = sourceType.getAttribute("glSourceMasterID").getColumnName();
		String primary = (String) dataValues.get("Primary");
	    PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.setDistinct(true);
		statement.appendFromTable(LCSSourcingConfigMaster.class);
		statement.appendFromTable(LCSSourcingConfig.class);
		statement.appendFromTable(LCSSourceToSeasonLinkMaster.class);
		statement.appendSelectColumn(new QueryColumn(LCSSourcingConfig.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendCriteria(new Criteria(new QueryColumn(LCSSourcingConfigMaster.class, "productMasterReference.key.id"), "?", "=")	,	new Long(productMasterId));

	
		if(MasterID!=null) {
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria("LCSSourcingConfig", scSourceMasteCol, MasterID, Criteria.EQUALS));
		}
		statement.appendAndIfNeeded();
		statement.appendJoin(new QueryColumn(LCSSourceToSeasonLinkMaster.class, "sourcingConfigMasterReference.key.id"),new QueryColumn(LCSSourcingConfigMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSSourcingConfig.class, "iterationInfo.latest"), "1", "="));
		Set<String> set = searchTable.keySet();
	    Iterator var11 = set.iterator();        while(var11.hasNext()) {
        String key = (String)var11.next();
      //  statement.appendAndIfNeeded();
        String value = (String)searchTable.get(key);
        key = sourceType.getAttribute(key).getColumnDescriptorName();
    //    statement.appendCriteria(new Criteria(new QueryColumn(LCSSourcingConfig.class, key), "?", "="), value);
         }

	    statement.appendJoin(new QueryColumn(LCSSourcingConfig.class, "masterReference.key.id"	),new QueryColumn(LCSSourcingConfigMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		System.out.println(statement);
		Collection<?> results = LCSQuery.runDirectQuery(statement).getResults();
		System.out.println("we have " + results.size() + " source configs");
		Iterator<?> it = results.iterator();
		FlexObject fo = null;
		LCSSourcingConfig sc = null;
		if (it != null && it.hasNext()) {
			fo = (FlexObject) it.next();
		}

		if (fo != null) {                
			sc = (LCSSourcingConfig) LCSQuery.findObjectById("OR:com.lcs.wc.sourcing.LCSSourcingConfig:" + fo.getString("LCSSOURCINGCONFIG.IDA2A2"), false);
		}                                                     

		return sc;
	}

	protected static boolean createCostSheet(LCSProduct product, LCSSeason season, LCSSKU sku, LCSSourcingConfig sc,Hashtable<String, Object> dataValues, String fileName, String colorNames, String representiveColorway,boolean isPrimary,String costsheetMasterid,boolean whatIf,String SpecMasterId)throws WTException, WTPropertyVetoException {
		boolean status = false;
		LCSCostSheet cs = null;
		LCSPartMaster skuMaster = null;
		LCSCostSheet cs2 = null;
		if (sku == null) {
			skuMaster = product.getPlaceholderMaster();
		} else {
			skuMaster = sku.getMaster();
		}
	 	cs = getLCSCostSheet1(product, season, skuMaster, sc, dataValues,costsheetMasterid);
	 	
		 if(cs== null) {
		 LCSSeasonProductLink link2 = LCSSeasonQuery.findSeasonProductLink(product, season);
		 cs = (new LCSCostSheetLogic()).createNewCostSheet(sc, skuMaster, link2, season, isPrimary);
		 cs.setWhatIf(whatIf);
		 String 	value = (String) dataValues.get("Type");
		 FlexType flexType = FlexTypeCache.getFlexTypeFromPath(value);
		 cs.setFlexType(flexType);
		 if(SpecMasterId!=null&&!SpecMasterId.equalsIgnoreCase("")){
			 
			 FlexSpecification specification = getSpecification(product,SpecMasterId);
			 if(specification!=null) {
						     cs.setSpecificationMaster(specification.getMaster());
                }
			 
		 }
		 
	     new LCSCostSheetLogic().saveCostSheet(cs);
		System.out.println("creating  a new costsheet --->"+cs.getName()+"-"+cs.isPrimaryCostSheet());
		cs =(LCSCostSheet) VersionHelper.latestIterationOf(cs);
		
		ArrayList<String> selectedColorwayIds = getSelectedColorwayIds(colorNames, product);
	    LCSCostSheetMaster costSheetMaster = (LCSCostSheetMaster) cs.getMaster();
        if (!VersionHelper.isCheckedOut(cs)) {
			cs = VersionHelper.checkout(cs);
			System.out.println("**Initial Checkedout Success!!!");
			cs.setApplicableColorNames(colorNames); // Set Coloways in CostShe
			cs.setValue("glCostSheetMasterID", costsheetMasterid);
			
			for (String objSKUID : selectedColorwayIds) {//   Creating CostSheet Colorways links
                   LCSSKU skuObj = (LCSSKU) LCSQuery.findObjectById(objSKUID);
				if (skuObj != null) {
					  representiveColorway = representiveColorway.replace("|~*~|", "").trim();
					if (skuObj.getValue("skuName").equals(representiveColorway)) {
						createColorLink(costSheetMaster, skuObj, true);
					} else {
						createColorLink(costSheetMaster, skuObj, false);
                     }
				}
            }

			cs = (LCSCostSheet) PersistenceHelper.manager.save(cs);
			cs = VersionHelper.checkin(cs);
			System.out.println("again check IN");
			if (cs != null) {
				LoadCommon.putCache(fileName, "CURRENT_PRODUCTCOSTSHEET", cs);
				LoadCommon.putCache(fileName, "CURRENT_FLEXTYPED", cs);
				status = true;
				return true;
			}

		}
	}else {
			if (cs != null) {
					System.out.println("---Already present----"+cs.getName());
				    String value = (String) dataValues.get("Type");
					FlexType flexType = FlexTypeCache.getFlexTypeFromPath(value);
					cs.setFlexType(flexType);
					 if(SpecMasterId!=null&&!SpecMasterId.equalsIgnoreCase("")){
					 FlexSpecification specification = getSpecification(product,SpecMasterId);
					 if(specification!=null) {
					 cs.setSpecificationMaster(specification.getMaster());
					 new LCSCostSheetLogic().saveCostSheet(cs);
					     }
					 }
					LoadCommon.putCache(fileName, "CURRENT_PRODUCTCOSTSHEET", cs);
					LoadCommon.putCache(fileName, "CURRENT_FLEXTYPED", cs);
					status = true;
					return true;
				}
			}
//		if (cs != null) {
//			LoadCommon.putCache(fileName, "CURRENT_PRODUCTCOSTSHEET", cs);
//			LoadCommon.putCache(fileName, "CURRENT_FLEXTYPED", cs);
//			return true;
//		} else {
//			try {
//
//				LCSSeasonProductLink link = LCSSeasonQuery.findSeasonProductLink(product, season);
//				cs = (new LCSCostSheetLogic()).createNewCostSheet(sc, skuMaster, link, season, false);
//				new LCSCostSheetLogic().saveCostSheet(cs);
//
//				if (cs != null) {
//					LoadCommon.putCache(fileName, "CURRENT_COSTSHEET", cs);
//					LoadCommon.putCache(fileName, "CURRENT_FLEXTYPED", cs);
//					status = true;
//				}
//			} catch (WTException var10) {
//				var10.printStackTrace();
//				LoadCommon.display("\n#WTException : " + var10.getLocalizedMessage());
//			}
//
//			return status;
//		}
			return status;
	}

	public static void createColorLink(LCSCostSheetMaster csMaster, LCSSKU sku, boolean isRepColor) {
		try {
			CostSheetToColorLink costSheetToColorLink = CostSheetToColorLink.newCostSheetToColorLink(sku.getMaster(),
					csMaster);
			costSheetToColorLink.setRepresentative(isRepColor);
			LCSCostSheetLogic.persist(costSheetToColorLink);
			System.out.println("Saved CostSheetToColorLink with OID:" + FormatHelper.getObjectId(costSheetToColorLink));
		} catch (Exception e) {
			System.out.println("Error creating Cost sheet to Color Link for cost Sheet:" + csMaster + ", with sku:" + sku);
		}

	}

	public static LCSCostSheet getLCSCostSheet(LCSProduct product, LCSSeason season, LCSPartMaster skuMaster,
			LCSSourcingConfig sc, Hashtable<String, Object> dataValues) throws WTPropertyVetoException, WTException {
		LCSSeasonMaster seasonMaster = season.getMaster();
		LCSSourcingConfigMaster scMaster = sc.getMaster();
		Collection<?> results = LCSCostSheetQuery.getCostSheetForConfig(skuMaster, scMaster, seasonMaster, false);
		LCSCostSheet cs = null;
		System.out.println("\t we have " + results.size() + " cost sheets");
		if (results != null && results.size() > 0) {
			Iterator<?> it = results.iterator();
			if (it.hasNext()) {
				cs = (LCSCostSheet) it.next();
			}

		}

		return cs;
	}


	
	  /** @deprecated */
	public static LCSCostSheet getLCSCostSheet1(LCSProduct product, LCSSeason season, LCSPartMaster sku, LCSSourcingConfig sc,Hashtable dataValues,String  costsheetMasterid ) throws WTPropertyVetoException, WTException {
		System.out.println("-----getLCSCostSheet1--------");
		  String scMasterId =null;
		String seasonMasterId = LCSQuery.getNumericFromOid(FormatHelper.getObjectId(season.getMaster()));
		String skuMasterId = LCSQuery.getNumericFromOid(FormatHelper.getObjectId(sku));
	    if(sc!=null) {
        LCSSourcingConfigMaster scMaster = sc.getMaster();
	    scMasterId = LCSQuery.getNumericFromOid(FormatHelper.getObjectId(scMaster));
	     }
		FlexType costsheetType = FlexTypeCache.getFlexTypeFromPath("Cost Sheet\\scSparc");//Cost Sheet\scSparc
		String sccostsheetCol = costsheetType.getAttribute("glCostSheetMasterID").getColumnName();
        System.out.println(dataValues);
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable(LCSCostSheetMaster.class);
	    if (sku.getName().startsWith("PLACEHOLDER")) {
            statement.appendFromTable("LCSProductCostSheet", "LCSCostSheet");
        } else {
            statement.appendFromTable("LCSSKUCostSheet", "LCSCostSheet");
        }
		
		 statement.appendSelectColumn(new QueryColumn("LCSCostSheet", LCSProductCostSheet.class, "iterationInfo.branchId"));
	     statement.appendSelectColumn(new QueryColumn("LCSCostSheet", LCSProductCostSheet.class, "thePersistInfo.theObjectIdentifier.classname"));
	      if (FormatHelper.hasContent(scMasterId)) {
	          statement.appendAndIfNeeded();
	          statement.appendCriteria(new Criteria(new QueryColumn(LCSCostSheetMaster.class, "sourcingConfigMasterReference.key.id"), "?", "="), new Long(scMasterId));
	        }

		statement.appendAndIfNeeded();
     	statement.appendCriteria(new Criteria("LCSCostSheet", sccostsheetCol, costsheetMasterid, Criteria.EQUALS));
		statement.appendAndIfNeeded();
		
		statement.appendCriteria(new Criteria(new QueryColumn(LCSCostSheetMaster.class, "skuMasterReference.key.id"), "?", "="), new Long(skuMasterId));
		if (FormatHelper.hasContent(seasonMasterId)) {
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSCostSheetMaster.class, "seasonMasterReference.key.id"), "?", "="), new Long(seasonMasterId));
        }
		statement.appendAndIfNeeded();
		statement.appendJoin(new QueryColumn("LCSCostSheet",LCSProductCostSheet.class, "masterReference.key.id"),new QueryColumn(LCSCostSheetMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn("LCSCostSheet",LCSProductCostSheet.class, "iterationInfo.latest"), "1", "="));
		System.out.println(statement);
	    Collection objectsFromResults = LCSCostSheetQuery.getObjectsFromResults(statement, "VR:", "LCSCOSTSHEET.CLASSNAMEA2A2", "LCSCOSTSHEET.BRANCHIDITERATIONINFO");
	    LCSCostSheet cs = null;
	    if (objectsFromResults != null && objectsFromResults.size() > 0) {
        Iterator<?> it = objectsFromResults.iterator();
        if (it.hasNext()) {
           cs = (LCSCostSheet)it.next();
          }
         }

		return cs;
	}

	public static ArrayList<String> getSelectedColorwayIds(String applicableColorNames, LCSProduct prod)throws WTException, WTPropertyVetoException {
		String[] applicableColorNameArr = applicableColorNames.split("\\|~\\*~\\|");
		String productNumber = String.valueOf(prod.getValue(PLM_PROD_NO));

		ArrayList<String> skuIDCol = new ArrayList<String>();

		for (String Color : applicableColorNameArr) {
	  Collection	productSKUList = LCSSKUQuery.findSKUs(prod, false);
		if(productSKUList.size() > 0){
			Iterator iter = productSKUList.iterator();
		
			LCSSKU tempSKU;
				while (iter.hasNext()) {
					tempSKU = (LCSSKU) iter.next();
					if(Color.equals(tempSKU.getValue("skuName"))){
						skuIDCol.add(SKU_STR_VR +String.valueOf(tempSKU.getBranchIdentifier()));
					}
			}
			}
			
		}

		//System.out.println("**skuCol:" + skuIDCol);
		return skuIDCol;

	}

	public static LCSSKU findSKU(String csSkuFromInput, String productAttKey, String productAttValue)
			throws WTException, WTPropertyVetoException {

		System.out.println("**findSKU:" + csSkuFromInput + "::" + productAttKey + "::" + productAttValue);

		LCSSKU csSku = null;

		FlexType skuType = FlexTypeCache.getFlexTypeFromPath("Product");

		skuType.setTypeScopeDefinition("SKU");
		

		FlexType productType = FlexTypeCache.getFlexTypeFromPath("Product");

		skuType.setTypeScopeDefinition("PRODUCT");

		try {

			PreparedQueryStatement statement = new PreparedQueryStatement();

			statement.appendFromTable(LCSSKU.class);

			statement.appendFromTable(LCSProduct.class);

			statement.appendSelectColumn(new QueryColumn(LCSSKU.class, "thePersistInfo.theObjectIdentifier.id"));

			statement.appendAndIfNeeded();

			statement.appendCriteria(new Criteria(new QueryColumn(LCSSKU.class, "flexTypeIdPath"),
					skuType.getTypeIdPath() + "%", Criteria.LIKE));

			statement.appendAndIfNeeded();

			statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "flexTypeIdPath"),
					skuType.getTypeIdPath() + "%", Criteria.LIKE));

			statement.appendAndIfNeeded();

			statement.appendCriteria(new Criteria(
					new QueryColumn(LCSSKU.class, skuType.getAttribute("skuName").getColumnDescriptorName()),
					csSkuFromInput.trim(), Criteria.EQUALS));

			statement.appendAndIfNeeded();

			statement.appendCriteria(new Criteria(
					new QueryColumn(LCSProduct.class,
							productType.getAttribute(productAttKey).getColumnDescriptorName()),
					productAttValue, Criteria.EQUALS));

			statement.appendAndIfNeeded();

			statement.appendCriteria(new Criteria("LCSPRODUCT", "latestiterationinfo", "1", Criteria.EQUALS));

			statement.appendAndIfNeeded();

			statement.appendJoin("LCSSKU", "productARevId", "LCSPRODUCT", "branchIditerationInfo");

			// statement.appendCriteria(new Criteria(new QueryColumn(LCSSKU.class,
			// "productARevId"), new QueryColumn(LCSProduct.class, "branchIditerationInfo"),
			// Criteria.EQUALS));

			statement.appendAndIfNeeded();

			statement.appendCriteria(new Criteria("LCSSKU", "placeholder", "1", Criteria.NOT_EQUAL_TO));

			statement.appendAndIfNeeded();

			statement.appendCriteria(new Criteria("LCSSKU", "latestiterationinfo", "1", Criteria.EQUALS));

			System.out.println("SKU Query: " + statement);

			SearchResults results = LCSQuery.runDirectQuery(statement);

			if (results != null) {

				System.out.println("Sku results :" + results.toString());

				if (results.getResultsFound() > 0) {

					System.out.println("SKU Found: " + csSkuFromInput);

					FlexObject obj = (FlexObject) results.getResults().elementAt(0);

					if (obj != null) {

						csSku = (LCSSKU) LCSQuery
								.findObjectById("OR:com.lcs.wc.product.LCSSKU:" + obj.getString("LCSSKU.idA2A2"));

						csSku = (LCSSKU) VersionHelper.getVersion(csSku.getMaster(), "A");

						csSku = (LCSSKU) VersionHelper.latestIterationOf(csSku);

						if (csSku != null)

							System.out.println("SKU Match found : " + csSku.toString());

					}

				}

			}

		} catch (Exception e) {

			e.printStackTrace();

			System.out.println("Error in findSKU" + e.getLocalizedMessage());

		}

		return csSku;

	}
	
	
	
	public static FlexSpecification getSpecification(LCSProduct prod,String specMasterId ) {
		FlexSpecification flexSpec= null;
	     try {
			FlexType specifcationFlexType = prod.getFlexType().getReferencedFlexType(ReferencedTypeKeys.SPEC_TYPE);
	
		String specMasterAttCol = specifcationFlexType.getAttribute("glSpecMasterId").getColumnName();
			
	
        PreparedQueryStatement pqs = new PreparedQueryStatement();
		pqs.appendFromTable("FlexSpecification");
		pqs.appendSelectColumn("FlexSpecification", "branchIditerationInfo");
	    pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("FlexSpecification", "latestIterationInfo", "1", Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("FlexSpecification", specMasterAttCol, specMasterId, Criteria.EQUALS));
		LoadCommon.display("-- INFO: Query to Search existing Spec : "+pqs.toString());
		SearchResults srcResults = LCSQuery.runDirectQuery(pqs);
		Collection srcColl = srcResults.getResults();
		if(srcColl!=null && srcColl.size()==1) {
			FlexObject fob = (FlexObject)srcColl.iterator().next();
			flexSpec = (FlexSpecification) LCSQuery.findObjectById("VR:com.lcs.wc.specification.FlexSpecification:"+fob.getString("FLEXSPECIFICATION.BRANCHIDITERATIONINFO"));
		}
		return flexSpec;
			
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flexSpec;
		
		
	}
	
	
	
	

}
