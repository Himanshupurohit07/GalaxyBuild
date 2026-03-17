package com.lcs.wc.load;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSEffectiveLink;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductLogic;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.ReferencedTypeKeys;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.*;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;

import java.util.*;

import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;
import wt.method.MethodContext;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.wip.WorkInProgressHelper;

import static com.lcs.wc.foundation.LCSLogic.loadMethodContextCache;
import static com.lcs.wc.foundation.LCSLogic.persist;

public class CustomLoadLinePlan {
 protected static String[] LINEPLAN_PRODUCTDETAILS = new String[0];
 protected static String[] LINEPLAN_SKUDETAILS = new String[0];
 protected static LCSSourcingConfigLogic SOURCINGCONFIG_LOGIC = new LCSSourcingConfigLogic();
 protected static LCSSourcingConfigQuery SOURCINGCONFIG_QUERY = new LCSSourcingConfigQuery();
 protected static LCSCostSheetQuery COSTSHEET_QUERY = new LCSCostSheetQuery();
 protected static LCSCostSheetLogic COSTSHEET_LOGIC = new LCSCostSheetLogic();

 public CustomLoadLinePlan() {
 }
 public static boolean initLinePlan(Hashtable<?, ?> dataValues, Hashtable<?, ?> commandLine, Vector<?> returnObjects) {
     return initLinePlan(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
 }

 public static boolean createLinePlan(Hashtable<?, ?> dataValues, Hashtable<?, ?> commandLine, Vector<?> returnObjects) {
     return createLinePlan((Hashtable<String, Object>) dataValues, LoadCommon.getValue(commandLine, "FileName", false));
 }

 public static boolean createLinePlanLinks(Hashtable<?, ?> dataValues, Hashtable<?, ?> commandLine, Vector<?> returnObjects) {
     return createLinePlanLinks((Hashtable<String, Object>) dataValues, LoadCommon.getValue(commandLine, "FileName", false));
 }

 public static boolean createLinePlanAttribute(Hashtable<?, ?> dataValues, Hashtable<?, ?> commandLine, Vector<?> returnObjects) throws WTPropertyVetoException, WTException {
     return createLinePlanAttribute((Hashtable<String, Object>) dataValues, LoadCommon.getValue(commandLine, "FileName", false));
 }

 public static boolean createLinePlanMOAAttribute(Hashtable<?, ?> dataValues, Hashtable<?, ?> commandLine, Vector<?> returnObjects) {
     return createLinePlanMOAAttribute((Hashtable<String, Object>) dataValues, LoadCommon.getValue(commandLine, "FileName", false));
 }

 protected static boolean initLinePlan(Hashtable<?, ?> dataValues, String fileName) {
     String value = LoadCommon.getValue(dataValues, "Key", true);
     if (value == null) {
         return false;
     } else {
         putLinePlanInfo(fileName, "LINEPLAN_DISPLAY", "");
         if (value.equals("Start Of Line Plan")) {
             LoadCommon.deleteSessionCache(fileName);
             putLinePlanInfo(fileName, "LINEPLAN_MODE", "TRUE");
             putLinePlanInfo(fileName, "LINEPLAN_REPORTFILE", fileName + ".rpt");
             putLinePlanInfo(fileName, "LINEPLAN_PRODUCTCOUNT", "0");
             putLinePlanInfo(fileName, "LINEPLAN_PRODUCTSUCCESSCOUNT", "0");
             putLinePlanInfo(fileName, "LINEPLAN_PRODUCTWARNINGCOUNT", "0");
             putLinePlanInfo(fileName, "LINEPLAN_PRODUCTDUPLICATECOUNT", "0");
             putLinePlanInfo(fileName, "LINEPLAN_PRODUCTERRORCOUNT", "0");
             putLinePlanInfo(fileName, "LINEPLAN_SKUCOUNT", "0");
             putLinePlanInfo(fileName, "LINEPLAN_SKUSUCCESSCOUNT", "0");
             putLinePlanInfo(fileName, "LINEPLAN_SKUWARNINGCOUNT", "0");
             putLinePlanInfo(fileName, "LINEPLAN_SKUDUPLICATECOUNT", "0");
             putLinePlanInfo(fileName, "LINEPLAN_SKUERRORCOUNT", "0");
             LoadCommon.putCache(fileName, "LINEPLAN_PRODUCTS", new Hashtable());
             LoadCommon.putCache(fileName, "Start Date", new Date());
             logLinePlan(fileName, "Start Of Line Plan Report ........" + LoadCommon.LINEFEED, false);
         } else if (value.equals("End Of Line Plan")) {
             String var10001 = LoadCommon.LINEFEED;
             logLinePlan(fileName, var10001 + "TOTAL # OF RECORDS PROCESSED\t" + getLinePlanInfo(fileName, "LINEPLAN_PRODUCTCOUNT"));
             logLinePlan(fileName, "TOTAL # OF PRODUCTS CREATED\t" + getLinePlanInfo(fileName, "LINEPLAN_PRODUCTSUCCESSCOUNT"));
             logLinePlan(fileName, "TOTAL # OF PRODUCTS CREATED WITH WARNINGS\t" + getLinePlanInfo(fileName, "LINEPLAN_PRODUCTWARNINGCOUNT"));
             logLinePlan(fileName, "TOTAL # OF PRODUCTS NOT CREATED (EXISTING PRODUCTS)\t" + getLinePlanInfo(fileName, "LINEPLAN_PRODUCTDUPLICATECOUNT"));
             logLinePlan(fileName, "TOTAL # OF PRODUCTS FAILED\t" + getLinePlanInfo(fileName, "LINEPLAN_PRODUCTERRORCOUNT"));
             logLinePlan(fileName, "TOTAL # OF SKUS\t" + getLinePlanInfo(fileName, "LINEPLAN_SKUCOUNT"));
             logLinePlan(fileName, "TOTAL # OF SKUS CREATED\t" + getLinePlanInfo(fileName, "LINEPLAN_SKUSUCCESSCOUNT"));
             logLinePlan(fileName, "TOTAL # OF SKUS CREATED WITH WARNINGS\t" + getLinePlanInfo(fileName, "LINEPLAN_SKUWARNINGCOUNT"));
             logLinePlan(fileName, "TOTAL # OF SKU NOT CREATED (EXISTING SKUS)\t" + getLinePlanInfo(fileName, "LINEPLAN_SKUDUPLICATECOUNT"));
             logLinePlan(fileName, "TOTAL # OF SKUS FAILED\t" + getLinePlanInfo(fileName, "LINEPLAN_SKUERRORCOUNT"));
             logLinePlan(fileName, LoadCommon.LINEFEED);
             logLinePlan(fileName, "Line Plan Loading Started\t" + LoadCommon.getCache(fileName, "Start Date"));
             Date var3 = new Date();
             logLinePlan(fileName, "Line Plan Loading Finished\t" + var3);
             logLinePlan(fileName, LoadCommon.LINEFEED + "End Of Line Plan Report ........");
             LoadCommon.deleteSessionCache(fileName);
             putLinePlanInfo(fileName, "LINEPLAN_REPORTFILE", "");
         }

         LoadCommon.deleteSessionCache("FlexTypePaths");
         return true;
     }
 }

 protected static boolean createLinePlan(Hashtable<String, Object> dataValues, String fileName) {
     System.out.println(dataValues);
     if (!preLoadProcess(dataValues)) {
         return false;
     } else {
         LCSSeason season = getSeason(dataValues, fileName);
         return season != null;
     }
 }

 protected static boolean createLinePlanLinks(Hashtable<String, Object> dataValues, String fileName) {
     try {
         String value = null;
         WTObject[] var10000 = new WTObject[]{null, null};
         LCSSeason season = null;
         LoadCommon.parseConstantValues(dataValues);
         Hashtable<String, Object> table = LoadCommon.parseObjectAttributes(dataValues, (Hashtable)null, "Season", false, true);
         if ((value = LoadCommon.getValue(dataValues, "ProductType", false)) != null) {
             table.put("SeasonProductType", value);
         }

         WTObject[] obj = LoadCommon.searchForObject(table, (String)null, false, true);
         if ((season = (LCSSeason)obj[0]) == null) {
             return false;
         } else {
             table = LoadCommon.parseObjectAttributes(dataValues, (Hashtable)null, "Product", false, true);
             obj = LoadCommon.searchForObject(table, (String)null, false, true);
             if (obj[0] == null) {
                 return false;
             } else {
                 setSeasonProductValues(season, obj[0], dataValues, fileName);
                 setSourcingConfigValues(season, (LCSProduct)obj[0], dataValues, fileName);
                 setCostSheetValues(season, obj[0], dataValues, fileName);
                 table = LoadCommon.parseObjectAttributes(dataValues, (Hashtable)null, "Sku", false, true);
                 if (!LoadCommon.containsData(table)) {
                     return true;
                 } else {
                     if ((value = LoadCommon.getValue(dataValues, "ProductType", false)) != null) {
                         table.put("Type", value);
                     }

                     table.put("FlexTyped-idA3A12:SearchCriteria", String.valueOf(((LCSProduct)obj[0]).getMaster().getPersistInfo().getObjectIdentifier().getId()));
                     obj = LoadCommon.searchForObject(table, "LCSSKU", false, true);
                     if (obj[0] == null) {
                         return false;
                     } else {
                         setSeasonProductValues(season, obj[0], dataValues, fileName);
                         setSkuSourcingConfigValues(season, (LCSSKU)obj[0], dataValues, fileName);
                         setCostSheetValues(season, obj[0], dataValues, fileName);
                         return true;
                     }
                 }
             }
         }
     } catch (WTException var6) {
         var6.printStackTrace();
         LoadCommon.display("\n#WTException : " + var6.getLocalizedMessage());
         return false;
     }
 }

 protected static LCSSeasonProductLink setSeasonProductValues(LCSSeason season, WTObject obj, Hashtable<String, Object> dataValues, String fileName) {
     LCSSeasonProductLink seasonProduct = null;
     String objectKey = "Product";
     if (obj instanceof LCSSKU) {
         objectKey = "Sku";
     }

     String[] lineplanAttributes = new String[]{"SeasProductProdSku", "SeasProduct" + objectKey};
     Hashtable<String, Object> table = getLinkAttributes(dataValues, lineplanAttributes);
     if (!LoadCommon.containsData(table)) {
         return seasonProduct;
     } else {
         LoadCommon.display("Setting Season Product " + objectKey + " Attributes ...");
         if (objectKey.equals("Product")) {
             seasonProduct = ExtractLinePlan.getSeasonProductLink(season, (LCSProduct)obj);
         } else {
             seasonProduct = ExtractLinePlan.getSeasonProductLink(season, (LCSSKU)obj);
         }

         if (seasonProduct == null) {
             LoadCommon.display("#Error Retrieving Season Product Link For Product/Sku " + obj + " on season " + season.getName() + "!!!");
         } else {
             setLinkAttributes(seasonProduct, table, fileName);
         }

         return seasonProduct;
     }
 }

 protected static void setSourcingConfigValues(LCSSeason season, LCSProduct product, Hashtable<String, Object> dataValues, String fileName) throws WTException {
     String[] lineplanAttributes = new String[]{"SourcingConfig", ""};
     Hashtable<String, Object> table = getLinkAttributes(dataValues, lineplanAttributes);
     if (LoadCommon.containsData(table)) {
         LoadCommon.display("Setting Sourcing Config Attributes ...");
         Collection<?> sourcingConfigs = LCSSourcingConfigQuery.getSourcingConfigForProductSeason(product, season);
         Iterator<?> it = sourcingConfigs.iterator();

         while(it.hasNext()) {
             setLinkAttributes((LCSSourcingConfig)it.next(), table, fileName);
         }

     }
 }

 protected static LCSSKUSourcingLink[] setSkuSourcingConfigValues(LCSSeason season, LCSSKU sku, Hashtable<String, Object> dataValues, String fileName) throws WTException {
     LCSSKUSourcingLink[] skuSourcings = new LCSSKUSourcingLink[0];
     String[] lineplanAttributes = new String[]{"SkuSourcingConfig", ""};
     Hashtable<String, Object> table = getLinkAttributes(dataValues, lineplanAttributes);
     if (!LoadCommon.containsData(table)) {
         return skuSourcings;
     } else {
         LoadCommon.display("Setting Sku Sourcing Attributes ...");
         String skuMasterId = String.valueOf(sku.getMaster().getPersistInfo().getObjectIdentifier().getId());
         String seasonMasterId = String.valueOf(season.getMaster().getPersistInfo().getObjectIdentifier().getId());
         Vector<?> links = (Vector)SOURCINGCONFIG_QUERY.getSkuSourcingLinks((String)null, skuMasterId, seasonMasterId, false);
         Object[] skuSourcings1 = links.toArray();
         skuSourcings = new LCSSKUSourcingLink[skuSourcings1.length];

         for(int i = 0; i < skuSourcings1.length; ++i) {
             skuSourcings[i] = (LCSSKUSourcingLink)skuSourcings1[i];
             setLinkAttributes(skuSourcings[i], table, fileName);
         }

         return skuSourcings;
     }
 }

 protected static Collection<Object> setCostSheetValues(LCSSeason season, WTObject obj, Hashtable<String, Object> dataValues, String fileName) {
     Collection<Object> costSheets = new ArrayList();
     LCSProduct product = null;
     LCSSKU sku = null;
     String objectKey = "Product";

     try {
         if (obj instanceof LCSSKU) {
             objectKey = "Sku";
             sku = (LCSSKU)obj;
             product = sku.getProduct();
         } else {
             product = (LCSProduct)obj;
         }

         String[] lineplanAttributes = new String[]{"CostSheetProdSku", "CostSheet" + objectKey};
         Hashtable<String, Object> table = getLinkAttributes(dataValues, lineplanAttributes);
         if (!LoadCommon.containsData(table)) {
             return (Collection)costSheets;
         }

         LoadCommon.display("Setting Cost Sheet " + objectKey + " Attributes ...");
         Collection<?> sourcingConfigs2 = LCSSourcingConfigQuery.getSourcingConfigForProductSeason(product, season);
         LCSSourcingConfig source = null;
         Iterator<?> it = sourcingConfigs2.iterator();

         while(it.hasNext()) {
             source = (LCSSourcingConfig)it.next();
             LCSCostSheetQuery var10000;
             if (objectKey.equals("Product")) {
                 var10000 = COSTSHEET_QUERY;
                 costSheets = LCSCostSheetQuery.getCostSheetForConfig(product.getPlaceholderMaster(), source.getMaster());
             } else {
                 var10000 = COSTSHEET_QUERY;
                 costSheets = LCSCostSheetQuery.getCostSheetForConfig(sku.getMaster(), source.getMaster());
             }
         }
     } catch (WTException var13) {
         var13.printStackTrace();
         LoadCommon.display("\n#WTException : " + var13.getLocalizedMessage());
     }

     return (Collection)costSheets;
 }

 protected static boolean createLinePlanAttribute(Hashtable<String, Object> dataValues, String fileName) throws WTPropertyVetoException, WTException {
    
     String lineplanObjectKey = LoadCommon.getValue(dataValues, "ObjectKey", true);
     if (lineplanObjectKey == null) {
         return false;
     } else {
         String attributeKey = LoadCommon.getValue(dataValues, "AttributeKey", true);
         if (attributeKey == null) {
             return false;
         } else {
             FlexTyped lineplanObject = (FlexTyped)LoadCommon.getCache(fileName, lineplanObjectKey);
                if (lineplanObject == null) {
                 LoadCommon.display("#No FlexTyped Object Available In Cache For Key '" + lineplanObjectKey + "' !!!");
                 return false;
             } else {
                 LoadCommon.display("Cached Object = " + lineplanObject.toString());
                 LoadCommon.putCache(fileName, "CURRENT_FLEXTYPED", lineplanObject);
                 String attributeRefType = LoadCommon.getValue(dataValues, "AttributeRefType", false);
                 if (attributeRefType == null) {
                     return CustomLoadFlexTyped.createFlexTypeAttribute(dataValues, fileName);
                 } else {
                     dataValues.put("AttributeKey", attributeKey + "=name");
                     dataValues.put("AttributeRefValue", LoadCommon.getValue(dataValues, "AttributeValue", false));
//                     System.out.println("dataValues 313--->"+dataValues);
                     return CustomLoadFlexTyped.createFlexTypeObjectReferences(dataValues, fileName);
                 }
             }
         }
     }
 }

 protected static boolean createLinePlanMOAAttribute(Hashtable<String, Object> dataValues, String fileName) {
     LoadCommon.parseConstantValues(dataValues);
     String lineplanObjectKey = LoadCommon.getValue(dataValues, "ObjectKey", true);
     if (lineplanObjectKey == null) {
         return false;
     } else {
         String attributeKey = LoadCommon.getValue(dataValues, "AttributeKey", true);
         if (attributeKey == null) {
             return false;
         } else {
             FlexTyped lineplanObject = (FlexTyped)LoadCommon.getCache(fileName, lineplanObjectKey);
             if (lineplanObject == null) {
                 LoadCommon.display("#No FlexTyped Object Available In Cache For Key '" + lineplanObjectKey + "' !!!");
                 return false;
             } else {
                 LoadCommon.display("Cached Object = " + lineplanObject.toString());
                 LoadCommon.putCache(fileName, "CURRENT_FLEXTYPED", lineplanObject);
                 return LoadMOA.createMOAEntry(dataValues, fileName);
             }
         }
     }
 }

 protected static LCSSeason getSeason(Hashtable<String, Object> dataValues, String fileName) {
     Hashtable<String, Object> seasonValues = LoadCommon.parseObjectAttributes(dataValues, (Hashtable)null, "Season", false, true);
     LoadCommon.display("Season Values = " + seasonValues.toString());
     return LoadSeason.createSeason(seasonValues, fileName) ? (LCSSeason)LoadCommon.getCache(fileName, "CURRENT_SEASON") : null;
 }

 public static boolean addProduct(Hashtable<String, Object> dataValues, Hashtable<String, Object> commandLine, Vector<?> returnObjects) throws WTException, WTPropertyVetoException {
     String fileName = LoadCommon.getValue(commandLine, "FileName", false);
     LCSSeason season = (LCSSeason)LoadCommon.getCache(fileName, "CURRENT_SEASON");
     LCSProduct product = addProduct(season, dataValues, fileName);    
     return product != null;
 }

 protected static LCSProduct addProduct(LCSSeason season, Hashtable<String, Object> dataValues, String fileName) throws WTException {
     Hashtable<String, Object> productValues = LoadCommon.parseObjectAttributes(dataValues, (Hashtable)null, "Product", false, true);
     if (season != null) {
         productValues.put("Season", season);
     }
     LoadCommon.display("Product Values = " + productValues.toString());
     LCSProduct product = null;
     MethodContext.getContext().put("LOAD_IN_PROGRESS", "true");
     MethodContext methodContext = MethodContext.getContext();
     methodContext.put("LOAD_IN_PROGRESS", "true");
     if (CustomLoadProduct.createProduct(productValues, fileName)) {
         product = (LCSProduct)LoadCommon.getCache(fileName, "CURRENT_PRODUCT");
     }
        methodContext.remove("LOAD_IN_PROGRESS");
		
     return product;
 }

 public static boolean addSku(Hashtable<String, Object> dataValues, Hashtable<String, Object> commandLine, Vector<?> returnObjects) {
     String fileName = LoadCommon.getValue(commandLine, "FileName", false);
     LCSSeason season = (LCSSeason)LoadCommon.getCache(fileName, "CURRENT_SEASON");
     LCSProduct product = (LCSProduct)LoadCommon.getCache(fileName, "CURRENT_PRODUCT");
     LCSSKU sku = addSku(season, product, dataValues, fileName);
     return sku != null;
 }

 protected static LCSSKU addSku(LCSSeason season, LCSProduct product, Hashtable<String, Object> dataValues, String fileName) {
     Hashtable<String, Object> criteriaValues = LoadCommon.parseObjectAttributes(dataValues, (Hashtable)null, "Sku", false, false);
     criteriaValues = LoadCommon.getCriteria(criteriaValues, ":SearchCriteria");
     if (!LoadCommon.containsData(criteriaValues)) {
         LoadCommon.display("No Sku Specified ....");
         return null;
     } else {
         Hashtable<String, Object> skuValues = LoadCommon.parseObjectAttributes(dataValues, (Hashtable)null, "Sku", false, false);
         skuValues.put("Product", product);
         if (season != null) {
             skuValues.put("Season", season);
         }

         LoadCommon.display("Sku Values = " + skuValues.toString());
         return CustomLoadSku.createSku(skuValues, fileName) ? (LCSSKU)LoadCommon.getCache(fileName, "CURRENT_SKU") : null;
     }
 }

 public static boolean addSourcingConfig(Hashtable<String, Object> dataValues, Hashtable<?, ?> commandLine, Vector<?> returnObjects) throws WTException {
    System.out.println("----customLoadLineplan----");
	 LoadCommon.display(" S C Values: " + dataValues);
     String fileName = LoadCommon.getValue(commandLine, "FileName", false);
     LCSSeason season = (LCSSeason)LoadCommon.getCache(fileName, "CURRENT_SEASON");
     LCSProduct product = (LCSProduct)LoadCommon.getCache(fileName, "CURRENT_PRODUCT");
      Hashtable<String, Object> values = LoadCommon.parseObjectAttributes(dataValues, (Hashtable)null, "SourcingConfig", false, true);
      String sourceMasterID =(String)dataValues.get("sourceMasterID");
    //  System.out.println("sourceMasterID-->"+sourceMasterID);
      boolean ans = CustomLoadSourceAndCost.createSourcingConfig(product, season, values, fileName,sourceMasterID);
     if (ans) {
         LCSSourcingConfig sc = (LCSSourcingConfig)LoadCommon.getCache(fileName, "CURRENT_SOURCING_CONFIG");
         Collection<?> links = SOURCINGCONFIG_QUERY.getSkuSourcingLinks(sc.getMaster(), (LCSPartMaster)null, season.getMaster(), false);
         HashMap<String, LCSSKUSourcingLink> map = new HashMap(links.size() - 1);
         LCSSKUSourcingLink link = null;
         String name = null;
         Iterator<?> it = links.iterator();

         while(it.hasNext()) {
             link = (LCSSKUSourcingLink)it.next();
             LCSSKU var10000 = (LCSSKU)VersionHelper.getVersion(link.getSkuMaster(), "A");
             name = "" + var10000.getValue("skuName");
             if (!FormatHelper.hasContent(name)) {
                 name = link.getSkuMaster().getName();
             }

             if (name.indexOf("PLACEHOLDER:PRODUCT") != 0) {
                 map.put(name, link);
             }
         }

         LoadCommon.putCache(fileName, "CURRENT_SOURCE_TO_SKUS_LINKS", map);
         return true;
     } else {
         return false;
     }
 }
 public static boolean addCostSheet(Hashtable<String, Object> dataValues, Hashtable<?, ?> commandLine, Vector<?> returnObjects) throws WTPropertyVetoException, WTException, InterruptedException {
     System.out.println("===========================addCostSHeet Custom");
     LoadCommon.display("Cost Sheet Values: " + dataValues);
     String fileName = LoadCommon.getValue(commandLine, "FileName", false);
  //  LCSSeason season = (LCSSeason)LoadCommon.getCache(fileName, "CURRENT_SEASON");
     LCSProduct product = getProduct(dataValues,fileName);
    LCSSeason season =  getSeason1(dataValues,fileName);
// LCSProduct product = (LCSProduct)LoadCommon.getCache(fileName, "CURRENT_PRODUCT");
    // LCSSKU sku = (LCSSKU)LoadCommon.getCache(fileName, "CURRENT_SKU");
     LCSSKU sku = null;
    boolean primary= false;
    boolean whatIf= false;
    String colorNames =(String) dataValues.get("flexAttApplicableColorNames");
    String representiveColorway =(String) dataValues.get("flexAttRepresentiveColorway");
    String isPrimary =(String) dataValues.get("flexAttIsPrimary");
    String costsheetid =(String) dataValues.get("CostSheetMasterId");
    String sourceMasterid =(String) dataValues.get("SourceMasterId");//flexAttIsWhatif
    String isWhatif =(String) dataValues.get("flexAttIsWhatif");
    String SpecMasterId =(String) dataValues.get("SpecMasterId");
   // System.out.println("SpecMasterId--->"+SpecMasterId.isBlank());
    //System.out.println("SpecMasterId--->"+SpecMasterId.equalsIgnoreCase(""));
   if(isPrimary.equals("true")) {
	   primary=true;
	 }
   if(isWhatif.equals("true")) {
	   whatIf=true;
	 }
     if(FormatHelper.hasContent(colorNames)) { 
    	 colorNames=colorNames.replace("^","~");
    }
     
   //  LCSSourcingConfig sc = (LCSSourcingConfig)LoadCommon.getCache(fileName, "CURRENT_SOURCING_CONFIG");
  // sc = verifySourcingConfig(product, sc, dataValues);
     //FlexType sourceType = product.getFlexType().getReferencedFlexType("SOURCING_CONFIG_TYPE_ID");
     LCSSourcingConfig sc =  getSource(dataValues,product,sourceMasterid);
      LCSCostSheet cs = addCostSheet(season, product, sku, sc, dataValues, fileName,colorNames,representiveColorway,primary,costsheetid,whatIf,SpecMasterId);
     return cs != null;
 }

 protected static LCSCostSheet addCostSheet(LCSSeason season, LCSProduct product, LCSSKU sku, LCSSourcingConfig sc, Hashtable<String, Object> dataValues, String fileName, String colorNames,String representiveColorway,boolean isPrimary,String costsheetid,boolean whatIf,String SpecMasterId) throws WTPropertyVetoException, WTException {
     Hashtable<String, Object> values = LoadCommon.parseObjectAttributes(dataValues, (Hashtable)null, "CostSheet", false, true);
     LCSCostSheet cs = null;
     if (CustomLoadSourceAndCost.createCostSheet(product, season, sku, sc, values, fileName,colorNames,representiveColorway,isPrimary,costsheetid,whatIf,SpecMasterId)) {
    	  cs = (LCSCostSheet)LoadCommon.getCache(fileName, "CURRENT_PRODUCTCOSTSHEET");
   
       }
   return cs;
 }
 
 
 
 

 protected static LCSSourcingConfig verifySourcingConfig(LCSProduct product, LCSSourcingConfig sc, Hashtable<String, Object> dataValues) {
     Hashtable<String, Object> scValues = LoadCommon.parseObjectAttributes(dataValues, (Hashtable)null, "SourcingConfig", false, true);
     System.out.println(scValues);
     String scName = (String)scValues.get("flexAttname:SearchCriteria");

     try {
         StringBuffer sb = new StringBuffer((String)product.getValue("productName"));
         sb.append(">").append(scName);
         scName = sb.toString();
     } catch (WTException var8) {
         var8.printStackTrace();
         LoadCommon.display("\n#WTException : " + var8.getLocalizedMessage());
     }

     if (sc != null && scName.equals(sc.getName())) {
         return sc;
     } else {
         try {
        	 
             sc = CustomLoadSourceAndCost.getLCSSourcingConfig3(product, scValues);
         } catch (WTException var6) {
             var6.printStackTrace();
             LoadCommon.display("\n#WTException : " + var6.getLocalizedMessage());
         } catch (WTPropertyVetoException var7) {
             var7.printStackTrace();
             LoadCommon.display("\n#WTException : " + var7.getLocalizedMessage());
         }

         return sc;
     }
 }

 public static boolean addSKUSourcingLink(Hashtable<String, Object> dataValues, Hashtable<String, Object> commandLine, Vector<?> returnObjects) {
     LoadCommon.display("Setting Colorway Sourcing Attributes ...");
     String fileName = LoadCommon.getValue(commandLine, "FileName", false);
     HashMap<String, LCSSKUSourcingLink> links = (HashMap)LoadCommon.getCache(fileName, "CURRENT_SOURCE_TO_SKUS_LINKS");
     if (links != null && links.size() != 0) {
         boolean active = Boolean.valueOf((String)dataValues.get("active"));
         String name = (String)dataValues.get("skuMasterName");
         LCSSKUSourcingLink link = (LCSSKUSourcingLink)links.get(name);
         if (link == null) {
             System.out.println("There is no source to colorway link for the colorway " + name);
             return true;
         } else {
             if (active && !link.isActive() || !active && link.isActive()) {
                 try {
                     link.setActive(active);
                 } catch (WTPropertyVetoException var9) {
                     var9.printStackTrace();
                     return false;
                 }
             }

             LoadCommon.putCache(fileName, "CURRENT_SKUSOURCINGLINK", link);
             LoadCommon.putCache(fileName, "CURRENT_FLEXTYPED", link);
             return true;
         }
     } else {
         return true;
     }
 }

 protected static Hashtable<String, Object> getLinkAttributes(Hashtable<String, Object> dataValues, String[] attributeKeys) {
     Hashtable<String, Object> table = null;

     for(int i = 0; i < attributeKeys.length; ++i) {
         if (!attributeKeys[i].equals("")) {
             table = LoadCommon.parseObjectAttributes(dataValues, table, attributeKeys[i], false, true);
         }
     }

     table = table == null ? new Hashtable() : table;
     return table;
 }

 protected static void setLinkAttributes(FlexTyped link, Hashtable<String, Object> table, String fileName) {
     try {
         LoadCommon.display("Saving " + link + " With Values " + table + " ...");
         boolean status = true;
         String value = null;
         FlexTyped tempLink = null;
         if (link instanceof LCSEffectiveLink) {
             tempLink = (FlexTyped)((LCSEffectiveLink)link).duplicate();
         } else {
             tempLink = link;
         }

         Vector<String> messages = CustomLoadFlexTyped.setAttributes(tempLink, table, fileName);
         if ("TRUE".equalsIgnoreCase(LoadCommon.getValue(table, "VALIDATE_MANDATORYATTRIBUTES"))) {
             for(int i = 0; i < messages.size(); ++i) {
                 if ((value = (String)messages.elementAt(i)).startsWith("#ERROR")) {
                     status = false;
                 }

                 String var10001 = link.getClass().getName();
                 logLinePlan(fileName, var10001 + " : " + value);
             }

             if (!status) {
                 value = "ERROR SETTING VALUES FOR THE LINK !!!  FAILURE UPDATING LINK !!!";
                 logLinePlan(fileName, value);
                 return;
             }
         }

         long time = (new Date()).getTime();
         LoadHelper.save(tempLink, table, fileName);
         time = (new Date()).getTime() - time;
         LoadCommon.display("Saving Took\t" + time + " ms.");
     } catch (WTException var9) {
         var9.printStackTrace();
         LoadCommon.display("\n#WTException : " + var9.getLocalizedMessage());
     }

 }

 protected static boolean preLoadProcess(Hashtable<String, Object> dataValues) {
     LoadCommon.parseConstantValues(dataValues);
     return true;
 }

 protected static void logLinePlan(String fileName, String line) {
     logLinePlan(fileName, line, true);
 }

 protected static void logLinePlan(String fileName, String line, boolean append) {
     String reportFile = getLinePlanInfo(fileName, "LINEPLAN_REPORTFILE");
     String lineplanDisplay = getLinePlanInfo(fileName, "LINEPLAN_DISPLAY");
     if (reportFile != null) {
         if (lineplanDisplay != null) {
             line = lineplanDisplay + "\t" + line;
         }

         LoadCommon.printToFile(reportFile, line, append);
     }

     LoadCommon.display(line);
 }

 protected static String getLinePlanInfo(String fileName, String key) {
     String value = null;
     Hashtable<String, Object> lineplanInfo = (Hashtable)LoadCommon.getCache(fileName, "LINEPLAN_INFORMATION");
     if (lineplanInfo != null) {
         value = LoadCommon.getValue(lineplanInfo, key);
     }

     return value;
 }

 protected static void putLinePlanInfo(String fileName, String key, Object value) {
     Hashtable<String, Object> lineplanInfo = (Hashtable)LoadCommon.getCache(fileName, "LINEPLAN_INFORMATION");
     if (lineplanInfo == null) {
         lineplanInfo = new Hashtable();
     }

     lineplanInfo.put(key, value);
     LoadCommon.putCache(fileName, "LINEPLAN_INFORMATION", lineplanInfo);
 }

 
 protected static LCSSeason getSeason1(Hashtable dataValues, String fileName) {
		LCSSeason season = null;
		try {
			String object = (String)dataValues.get("SkuflexAttseasonName:SearchCriteria");
			String seasontype = (String)dataValues.get("SeasonType:SearchCriteria");
		    FlexType seasonFlextype = FlexTypeCache.getFlexTypeFromPath(seasontype);
		    season=new LCSSeasonQuery().findSeasonByNameType(object, seasonFlextype);
			//Hashtable seasonValues = LoadCommon.parseObjectAttributes(dataValues, null, "Season", false, true);
			//season = (LCSSeason)LoadCommon.searchForObject(fileName, "Season", dataValues);
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
 
 
 protected static LCSSourcingConfig getSource(Hashtable dataValues, LCSProduct prodARev, String sourceMasterID) throws WTException {
	 System.out.println("----getSource---");
		LCSSourcingConfig srcConfig = null;
	    FlexType sourceType = prodARev.getFlexType().getReferencedFlexType("SOURCING_CONFIG_TYPE_ID");
        String sourceMasterAttCol = sourceType.getAttribute("glSourceMasterID").getColumnName();
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
			srcConfig = (LCSSourcingConfig) LCSQuery.findObjectById("VR:com.lcs.wc.sourcing.LCSSourcingConfig:"+fob.getString("LCSSOURCINGCONFIG.BRANCHIDITERATIONINFO"));
		}
		return srcConfig;
	}
 
 
 
 
 
 
 public static void main(String[] argv) {
     LoadCommon.SERVER_MODE = false;
     if (argv[0].equals("-DELETE")) {
         WTObject[] objects = null;

         try {
             objects = LoadCommon.getObjectsByAttributes("Product", (Hashtable)null, "LCSSKU", true);

             int i;
             for(i = 0; i < objects.length; ++i) {
                 LoadCommon.deleteObject(objects[i], false);
             }

             objects = LoadCommon.getObjectsByAttributes("Product", (Hashtable)null, (String)null, true);

             for(i = 0; i < objects.length; ++i) {
                 LoadCommon.deleteObject(objects[i], false);
             }
         } catch (WTException var3) {
             var3.printStackTrace();
             LoadCommon.display("\n#WTException : " + var3.getLocalizedMessage());
         }

         LoadCommon.SERVER_MODE = true;
     }
 }
}

