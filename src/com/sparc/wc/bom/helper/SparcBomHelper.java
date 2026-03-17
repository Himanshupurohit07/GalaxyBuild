package com.sparc.wc.bom.helper;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.lcs.wc.client.ApplicationContext;
import com.lcs.wc.color.LCSColor;
import com.lcs.wc.db.*;
import com.lcs.wc.flexbom.FlexBOMLink;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.material.LCSMaterialColor;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.ProductHeaderQuery;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.query.LSQModule;
import com.lcs.wc.season.query.LineSheetQuery;
import com.lcs.wc.season.query.LineSheetQueryOptions;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecToComponentLink;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.supplier.LCSSupplierMaster;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.bom.constants.SparcMigrationConstants;
import wt.util.WTException;
import com.lcs.wc.product.LCSProduct;
import wt.util.WTPropertyVetoException;
import com.lcs.wc.material.LCSMaterialColor;

import java.util.*;
import java.util.stream.Collectors;

public class SparcBomHelper {

    private static FlexType BO_APP_FLEXTYPE;
    private static FlexType BO_FTW_FLEXTYPE;


    static{
        try {
            BO_APP_FLEXTYPE =FlexTypeCache.getFlexTypeFromPath(SparcMigrationConstants.BO_APP_PATH);
            BO_FTW_FLEXTYPE = FlexTypeCache.getFlexTypeFromPath(SparcMigrationConstants.BO_FTW_PATH);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * @param strPath
     * @return
     */
    public static LCSSeason getSeason(String strPath, String seasonName) {
        FlexType flextype = null;
        LCSSeason objSeason = null;
        try {
            if (FormatHelper.hasContent(strPath)) {
                flextype = FlexTypeCache.getFlexTypeFromPath(strPath);
            }
            objSeason = new LCSSeasonQuery().findSeasonByNameType(seasonName, flextype);
        } catch (WTException e) {
            e.printStackTrace();
        }
        return objSeason;
    }

    /**
     * @return
     */
    public static Set<LCSSeason> getReebokSeasons(String typePath) {
        SearchResults results = new SearchResults();
        PreparedQueryStatement statement = new PreparedQueryStatement();
        Set<LCSSeason> seasons = new HashSet<>();
        try {
            //Season\scSeasonReebok\scFootwear
            statement.appendFromTable(LCSSeason.class);
            statement.appendSelectColumn(new QueryColumn(LCSSeason.class, "thePersistInfo.theObjectIdentifier.id"));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSSeason.class, "checkoutInfo.state"), "wrk", Criteria.NOT_EQUAL_TO));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSSeason.class, "iterationInfo.latest"), "1", Criteria.EQUALS));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSSeason.class, LCSQuery.TYPED_BRANCH_ID), FlexTypeCache.getFlexTypeFromPath(typePath).getIdNumber(), Criteria.EQUALS));
            results = LCSQuery.runDirectQuery(statement);
            Vector resultsData = results.getResults();
            for (int i = 0; i < resultsData.size(); i++) {
                FlexObject flexObj = (FlexObject) resultsData.get(i);
                LCSSeason objSeason = (LCSSeason) LCSSeasonQuery.findObjectById("OR:com.lcs.wc.season.LCSSeason:" + FormatHelper.applyFormat((String) flexObj.get("LCSSEASON.IDA2A2"), FormatHelper.LONG_FORMAT));
                seasons.add(objSeason);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return seasons;
    }

    /**
     * @param objSeason
     */
    public static Collection<FlexObject> getProductFlexObjects(LCSSeason objSeason) {
        com.lcs.wc.season.query.LineSheetQuery lsqn = null;
        Collection<FlexObject> productFlexObjects = new ArrayList<>();
        try {
            LineSheetQueryOptions options = new LineSheetQueryOptions();
            options.setSeason(objSeason);
            Injector injector = Guice.createInjector(new LSQModule());
            lsqn = injector.getInstance(com.lcs.wc.season.query.LineSheetQuery.class);
            LineSheetQuery lineSheetQuery = new LineSheetQuery();
            productFlexObjects = lsqn.getLineSheetResults(options);
        } catch (WTException ex) {
            ex.printStackTrace();
        }
        return productFlexObjects;
    }


    /**
     * This method gets the product objects based on the flexobjects.
     *
     * @return
     */
    public static List<LCSProduct> getProductObjects(Collection<FlexObject> prodFlexObjects) {
        LCSProduct objProduct = null;
        List<LCSProduct> prodObjects = new ArrayList<LCSProduct>();
        for (FlexObject prodFlexObject : prodFlexObjects) {
            try {
                String oid = "VR:com.lcs.wc.product.LCSProduct:" + FormatHelper.applyFormat((String) prodFlexObject.get("LCSPRODUCT.BRANCHIDITERATIONINFO"), FormatHelper.LONG_FORMAT);
                objProduct = (LCSProduct) LCSProductQuery.findObjectById(oid);
                Long strProdNo = (Long) objProduct.getValue("scPLMProductNo");
                //if("100153".equalsIgnoreCase(strProdNo.toString())) {
                    objProduct = (LCSProduct) VersionHelper.latestIterationOf(objProduct);
                    objProduct = (LCSProduct) VersionHelper.getVersion(objProduct, "A");
					prodObjects.add(objProduct);
                //}               

            } catch (WTException e) {
                e.printStackTrace();
            }
        }
        return prodObjects;
    }

    /**
     * This method gets the Collection of sources for the season and product.
     *
     * @param objProduct
     * @param objSeason
     * @return
     */
    public static Collection<LCSSourcingConfig> getSources(LCSProduct objProduct, LCSSeason objSeason) {
        Collection<LCSSourcingConfig> sources = new ArrayList<>();
        try {
            sources = (Collection<LCSSourcingConfig>) new ProductHeaderQuery().findSourcingConfigs(objProduct, objSeason);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sources;
    }

    /**
     * @param objProduct
     * @param objSeason
     * @param source
     * @return
     */
    public static Collection<FlexSpecification> getSpecs(LCSProduct objProduct, LCSSeason objSeason, LCSSourcingConfig source) {
        Collection<FlexSpecification> specs = new ArrayList<>();
        try {
            specs = (Collection<FlexSpecification>) new ProductHeaderQuery().findSpecifications(objProduct, source, objSeason);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return specs;
    }

    /**
     * @param objProduct
     * @param objSource
     * @param spec
     * @return
     */
    public static Collection<FlexBOMPart> getBOMs(LCSProduct objProduct, LCSSourcingConfig objSource, FlexSpecification spec) {
        Collection<FlexBOMPart> objBOMs = new ArrayList<>();
        try {
            objBOMs = new LCSFlexBOMQuery().findBOMObjects(objProduct, objSource, spec, "MAIN", true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return objBOMs;
    }

    /**
     * @return
     */
    public static boolean isPrimaryBOM(FlexSpecification objSpec, FlexBOMPart objBOM, Boolean isComponentCheck) {
        boolean isPrimary = false;
        FlexSpecToComponentLink specToComponentLink = null;
        if (!FormatHelper.equalsWithNull(objSpec, null) && isComponentCheck) {
            try {
                specToComponentLink = (FlexSpecToComponentLink) FlexSpecQuery.getSpecToComponentLink(objSpec, objBOM);
                if (!FormatHelper.equalsWithNull(specToComponentLink, null)) {
                    isPrimary = specToComponentLink.isPrimaryComponent();
                }
            } catch (WTException e) {
                e.printStackTrace();
            }
        }
        return isPrimary;
    }

    /**
     *
     * @param strProductNo
     * @param version
     * @return
     */
    public static LCSProduct getProduct(String strProductNo, String version) {
        LCSProduct product = null;
        try {
            PreparedQueryStatement statement = new PreparedQueryStatement();
            statement.appendFromTable(LCSProduct.class);
            String plmProductAtt = FlexTypeCache.getFlexTypeRoot(SparcMigrationConstants.PRODUCT).getAttribute(SparcMigrationConstants.PROD_NO_ATTRIBUTE).getColumnDescriptorName();
            statement.appendSelectColumn(new QueryColumn(LCSProduct.class, SparcMigrationConstants.ITERATION_BRANCHID));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, plmProductAtt), "?", "="), strProductNo);
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, SparcMigrationConstants.CHECKOUT_STATE), "wrk", "<>"));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, SparcMigrationConstants.ITERATION_LATEST), "1", "="));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, SparcMigrationConstants.VERSION_ID), "?", "="), version);
            SearchResults results = LCSQuery.runDirectQuery(statement);
            if (results.getResultsFound() > 0) {
                FlexObject obj = (FlexObject) results.getResults().elementAt(0);
                product = (LCSProduct) LCSQuery.findObjectById(SparcMigrationConstants.PRODUCT_VR_STR + obj.getString(SparcMigrationConstants.PROD_BRANCH_STR));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return product;
    }


    /**
     * This method return the collection of Bom Parts for all the season Products.
     *
     * @param typePath
     * @return
     */
    public static Set<FlexBOMPart> extractBOMParts(String typePath,String strSeasonName) {
        Set<LCSSeason> seasons = new HashSet<>();
        if(FormatHelper.hasContent(strSeasonName)){
            seasons.add(SparcBomHelper.getSeason(typePath,strSeasonName));
        }
        else {
            seasons = SparcBomHelper.getReebokSeasons(typePath);
        }

        Set<FlexBOMPart> flexBomParts = new LinkedHashSet<>();
        for (LCSSeason objSeason : seasons) {
            Collection<FlexObject> objFlexObjects = getProductFlexObjects(objSeason);
            List<LCSProduct> objProducts = getProductObjects(objFlexObjects);
            for (LCSProduct objProduct : objProducts) {
                try {
                    Long productNo = (Long) objProduct.getValue("scPLMProductNo");
                   //if ("103126".equalsIgnoreCase(productNo.toString()) ||"146441".equalsIgnoreCase(productNo.toString())) {
                        Collection<FlexBOMPart> boMs = getBOMs(objProduct, null, null);
                        flexBomParts.addAll(boMs);
                    //}
                } catch (WTException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return flexBomParts;
    }
    /**
     *
     * @param typePath
     * @param strSeasonName
     * @return
     */
    public static Set<FlexBOMPart> extractFlexBomParts(String typePath, String strSeasonName) {
        List<LCSSeason> seasonsList = new ArrayList<>();

        if (FormatHelper.hasContent(strSeasonName)) {
            seasonsList.add(SparcBomHelper.getSeason(typePath, strSeasonName));
        } else {
            Set<LCSSeason> seasons = SparcBomHelper.getReebokSeasons(typePath);

            System.out.println("seasons--------------------"+seasons);
            // Filter out invalid seasons and collect with year
            List<LCSSeason> validSeasons = new ArrayList<>();
            Map<LCSSeason, Integer> seasonYearMap = new HashMap<>();

            for (LCSSeason season : seasons) {
                Integer year = extractSeasonYear(season);
                if (year != null) {
                    validSeasons.add(season);
                    seasonYearMap.put(season, year);
                }
            }

            // Sort the validSeasons list based on the extracted year
            Collections.sort(validSeasons, new Comparator<LCSSeason>() {
                @Override
                public int compare(LCSSeason s1, LCSSeason s2) {
                    return seasonYearMap.get(s1).compareTo(seasonYearMap.get(s2));
                }
            });

			System.out.println("validSeasons----------------------"+validSeasons);
			seasonsList = validSeasons;
        }
		
		

        Set<FlexBOMPart> flexBomParts = new LinkedHashSet<>();

        for (LCSSeason objSeason : seasonsList) {
            Collection<FlexObject> objFlexObjects = getProductFlexObjects(objSeason);
            List<LCSProduct> objProducts = getProductObjects(objFlexObjects);

            for (LCSProduct objProduct : objProducts) {
                Collection<FlexBOMPart> boMs = getBOMs(objProduct, null, null);
                flexBomParts.addAll(boMs);
            }
        }
		
		System.out.println("flexBomParts---------------------"+flexBomParts);
        return flexBomParts;
    }

    private static Integer extractSeasonYear(LCSSeason season) {

        Object yearValueObj = null;
        try {
            yearValueObj = season.getValue("year");
            if (yearValueObj == null) return null;

            String yearValue = yearValueObj.toString().toLowerCase(); // e.g., "sc2024"
            if (yearValue.startsWith("sc") && yearValue.length() == 6) {
                try {
                    return Integer.parseInt(yearValue.substring(2)); // Extract "2024"
                } catch (NumberFormatException e) {
                    System.out.println("Failed to parse year from: " + yearValue);
                    return null;
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        
        return null;
    }

    /**
     *
     * @param typePath
     * @param strSeasonName
     * @return
     */
    public static Set<FlexBOMPart> extBOMParts(String typePath,String strSeasonName,String strDate){
        Set<LCSSeason> seasons = new HashSet<>();

        if(FormatHelper.hasContent(strSeasonName)){
            seasons.add(SparcBomHelper.getSeason(typePath,strSeasonName));
        }
        else {
            seasons = SparcBomHelper.getReebokSeasons(typePath);
        }

        Set<FlexBOMPart> flexBomParts = new LinkedHashSet<>();
        for (LCSSeason objSeason : seasons) {
            Collection<FlexObject> objFlexObjects = getProductFlexObjects(objSeason);
            List<LCSProduct> objProducts = getProductObjects(objFlexObjects);
            for (LCSProduct objProduct : objProducts) {
                try {
                    Long productNo = (Long) objProduct.getValue("scPLMProductNo");
                    if ("157583".equalsIgnoreCase(productNo.toString()) ||"115825".equalsIgnoreCase(productNo.toString())) {
                        Collection<FlexBOMPart> boMs = getBOMs(objProduct, null, null);
                        for(FlexBOMPart objBomPart : boMs){

                        }
                        flexBomParts.addAll(boMs);
                    }
                } catch (WTException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return flexBomParts;

    }


    /**
     * @param strTypePath
     * @return
     */
    public static Set<FlexBOMPart> getBomParts(String strTypePath) {
        Set<LCSProduct> products = new HashSet<>();
        Set<FlexBOMPart> flexBomParts = new HashSet<>();
        try {
            PreparedQueryStatement statement = new PreparedQueryStatement();
            statement.appendFromTable(LCSProduct.class);
            statement.appendSelectColumn(new QueryColumn(LCSProduct.class, "iterationInfo.branchId"));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "checkoutInfo.state"), "wrk", "<>"));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "iterationInfo.latest"), "1", "="));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, LCSQuery.TYPED_BRANCH_ID), FlexTypeCache.getFlexTypeFromPath(strTypePath).getIdNumber(), Criteria.EQUALS));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "versionInfo.identifier.versionId"), "?", "="), "A");
            SearchResults results = LCSQuery.runDirectQuery(statement);
            Iterator i = results.getResults().iterator();
            FlexObject fobj = null;

            while (i.hasNext()) {
                fobj = (FlexObject) i.next();
                products.add((LCSProduct) LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSProduct:" + fobj.getString("LCSPRODUCT.BRANCHIDITERATIONINFO")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        for (LCSProduct objProduct : products) {
            Collection<FlexBOMPart> boMs = getBOMs(objProduct, null, null);
            flexBomParts.addAll(boMs);
        }
        return flexBomParts;
    }


    /**
     * @return
     * @throws WTException
     */
    public static LCSMaterial findMaterialByID(String strMaterialID)  {
        LCSMaterial material = null;
        try {
            PreparedQueryStatement statement = new PreparedQueryStatement();
            String strMaterialNo = FlexTypeCache.getFlexTypeRoot("Material").getAttribute("scMaterialNo").getColumnDescriptorName();
            statement.appendFromTable(LCSMaterial.class);
            statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "thePersistInfo.theObjectIdentifier.id"));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, strMaterialNo), "?", "="), Long.parseLong(strMaterialID));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "checkoutInfo.state"), "wrk", "<>"));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "iterationInfo.latest"), "1", "="));
            SearchResults results = LCSQuery.runDirectQuery(statement);

            if (results.getResultsFound() > 0) {
                FlexObject obj = (FlexObject) results.getResults().elementAt(0);
                material = (LCSMaterial) LCSQuery.findObjectById("OR:com.lcs.wc.material.LCSMaterial:" + obj.getString("LCSMATERIAL.IDA2A2"));
            }
        }catch(Exception ex){
            ex.printStackTrace();;
        }
        return material;
    }


    /**
     * @return
     * @throws WTException
     */
    public static LCSSupplier findSupplierByNameType(String strSupplierId) throws WTException {
        LCSSupplier supplier = null;
        String strSupplierNo = FlexTypeCache.getFlexTypeRoot("Supplier").getAttribute("scPLMVendorSupplierNo").getColumnDescriptorName();

        PreparedQueryStatement statement = new PreparedQueryStatement();
        statement.appendFromTable(LCSSupplier.class);
        statement.appendFromTable(LCSSupplierMaster.class);
        statement.appendSelectColumn(new QueryColumn(LCSSupplier.class, "thePersistInfo.theObjectIdentifier.id"));
        statement.appendCriteria(new Criteria(new QueryColumn(LCSSupplier.class, strSupplierNo), "?", "="), Long.parseLong(strSupplierId));
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(LCSSupplier.class, "checkoutInfo.state"), "wrk", "<>"));
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(LCSSupplier.class, "iterationInfo.latest"), "1", "="));

        SearchResults results = LCSQuery.runDirectQuery(statement);
        if (results.getResultsFound() > 0) {
            FlexObject obj = (FlexObject) results.getResults().elementAt(0);
            supplier = (LCSSupplier) LCSQuery.findObjectById("OR:com.lcs.wc.supplier.LCSSupplier:" + obj.getString("LCSSUPPLIER.IDA2A2"));
        }
        return supplier;
    }

    /**
     * @param fullParts
     * @param typed
     */
    public static List<String> formBomLinkData(List<String> fullParts, FlexTyped typed,Boolean isAppOrFtw) {
        String attVarType = "";
        for (String strAttKey : SparcMigrationConstants.linkAttrList) {
            try {
                attVarType = typed.getFlexType().getAttribute(strAttKey).getAttVariableType();
                Object value = typed.getValue(strAttKey);
                String safeValue = SparcMigrationConstants.EMPTY_SPACE;
                if(SparcMigrationConstants.APP_PLACEMENT.equalsIgnoreCase(strAttKey) && !isAppOrFtw){
                    fullParts.add(SparcMigrationConstants.EMPTY_SPACE);
                }
                switch (attVarType) {
                    case SparcMigrationConstants.CURRENCY_TYPE:
                    case SparcMigrationConstants.FLOAT_TYPE:
                        if (value instanceof Number) {
                            double d = ((Number) value).doubleValue();
                            safeValue = (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
                        } else {
                            safeValue = safe(value);
                        }
                        break;

                    case SparcMigrationConstants.OBJECT_REF:
                        if (value != null) {
                            safeValue = ((LCSLifecycleManaged) value).getName();
                        }
                        break;
                    case SparcMigrationConstants.CHOICE_TYPE:
                        if (value != null) {
                            safeValue = value.toString(); // or getDisplayValue() if available
                        }
                        break;

                    default:
                        safeValue = safe(value);
                        break;
                }
                fullParts.add(safeValue);
            } catch (Exception ex) {
                // Log or handle exception, add empty string as fallback
                fullParts.add(SparcMigrationConstants.EMPTY_SPACE);
            }
        }

        return fullParts;
    }

    /**
     *
     * @param value
     * @return
     */
    private static String safe(Object value) {
        if (value == null) return SparcMigrationConstants.EMPTY_SPACE;
        if (value instanceof Double) {
            double d = (Double) value;
            return (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
        } else if (value instanceof Float) {
            float f = (Float) value;
            return (f == Math.floor(f)) ? String.valueOf((long) f) : String.valueOf(f);
        } else if (value instanceof Number || value instanceof String) {
            return value.toString();
        } else {
            return value.toString(); // fallback for object_ref or other types
        }

    }

    /**
     *
     * @param strBOMPartMasterId
     * @return
     */
    public static FlexBOMPart getBOMPart(String strBOMPartMasterId){
        PreparedQueryStatement statement = new PreparedQueryStatement();
        FlexBOMPart objBOMPart = null;
        try {
            FlexType objBOMFlexType = FlexTypeCache.getFlexTypeFromPath("BOM\\Materials");
            String BOM_MASTER_COLUMN = objBOMFlexType.getAttribute("scBOMMasterID").getColumnDescriptorName();
            statement.appendFromTable(FlexBOMPart.class);
            statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "iterationInfo.branchId"));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "iterationInfo.latest"), "1", "="));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "checkoutInfo.state"), "wrk", "<>"));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "versionInfo.identifier.versionId"), "A", "="));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, BOM_MASTER_COLUMN), strBOMPartMasterId, "="));
            SearchResults results = LCSQuery.runDirectQuery(statement);
            if (results.getResultsFound() > 0) {
                FlexObject obj = (FlexObject) results.getResults().elementAt(0);
                objBOMPart = (FlexBOMPart) LCSQuery.findObjectById("VR:com.lcs.wc.flexbom.FlexBOMPart:" + obj.getString("FlexBOMPart.BRANCHIDITERATIONINFO"));
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
        return objBOMPart;
    }


    /**
     *
     * @param colorNo
     * @return
     */
    public static LCSColor getColorObject(String colorNo){
        LCSColor color = null;
        FlexType colorFlexType = null;
        try {
            PreparedQueryStatement statement = new PreparedQueryStatement();
            colorFlexType = FlexTypeCache.getFlexTypeFromPath("Color");
            String colorNo_Col = colorFlexType.getAttribute("scColorNo").getColumnDescriptorName();
            statement.appendFromTable(LCSColor.class);
            statement.appendSelectColumn(new QueryColumn(LCSColor.class, "thePersistInfo.theObjectIdentifier.id"));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSColor.class, colorNo_Col), "?", "="), Long.parseLong(colorNo));
            SearchResults results = LCSQuery.runDirectQuery(statement);
            if (results.getResultsFound() > 0) {
                FlexObject obj = (FlexObject) results.getResults().elementAt(0);
                color = (LCSColor) LCSQuery.findObjectById("OR:com.lcs.wc.color.LCSColor:" + obj.getString("LCSCOLOR.IDA2A2"));
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return color;
    }


    public static LCSSKU findSKU(String csSkuFromInput, String productAttValue,String typePath) throws WTException, WTPropertyVetoException {
        LCSSKU csSku = null;
        FlexType skuType = FlexTypeCache.getFlexTypeFromPath(typePath);
        skuType.setTypeScopeDefinition("SKU");
        FlexType productType = FlexTypeCache.getFlexTypeFromPath(typePath);
        productType.setTypeScopeDefinition("PRODUCT");
        try {
            PreparedQueryStatement statement = new PreparedQueryStatement();
            statement.appendFromTable(LCSSKU.class);
            statement.appendFromTable(LCSProduct.class);
            statement.appendSelectColumn(new QueryColumn(LCSSKU.class, "thePersistInfo.theObjectIdentifier.id"));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSSKU.class, LCSQuery.TYPED_BRANCH_ID), skuType.getIdNumber(LCSSKU.class), Criteria.EQUALS));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, LCSQuery.TYPED_BRANCH_ID), productType.getIdNumber(), Criteria.EQUALS));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSSKU.class, skuType.getAttribute("scColorwayNo").getColumnDescriptorName()), "?", "="), Long.parseLong(csSkuFromInput));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, productType.getAttribute("scPLMProductNo").getColumnDescriptorName()), productAttValue, Criteria.EQUALS));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "iterationInfo.latest"), "1", "="));
            statement.appendAndIfNeeded();
            statement.appendJoin("LCSSKU", "productARevId", "LCSPRODUCT", "branchIditerationInfo");
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria("LCSSKU", "placeholder", "1", Criteria.NOT_EQUAL_TO));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSSKU.class, "iterationInfo.latest"), "1", "="));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, SparcMigrationConstants.VERSION_ID), "?", "="), "A");
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSSKU.class, SparcMigrationConstants.VERSION_ID), "?", "="), "A");
            SearchResults results = LCSQuery.runDirectQuery(statement);
            if (results != null) {
                if (results.getResultsFound() > 0) {
                    FlexObject obj = (FlexObject) results.getResults().elementAt(0);
                    if (obj != null) {
                        csSku = (LCSSKU) LCSQuery.findObjectById("OR:com.lcs.wc.product.LCSSKU:" + obj.getString("LCSSKU.idA2A2"));
                        csSku = (LCSSKU) VersionHelper.getVersion(csSku.getMaster(), "A");
                        csSku = (LCSSKU) VersionHelper.latestIterationOf(csSku);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return csSku;
    }

    /**
     *
     * @param name
     * @param isApparel
     * @return
     */
    public static LCSLifecycleManaged getComponentObject(String name,Boolean isApparel) {
        PreparedQueryStatement statement = new PreparedQueryStatement();
        String strColumnName = SparcMigrationConstants.EMPTY_SPACE;
        LCSLifecycleManaged objManaged = null;
        try {
            statement.appendFromTable(LCSLifecycleManaged.class);
            statement.appendSelectColumn(new QueryColumn(LCSLifecycleManaged.class, SparcMigrationConstants.COLUMN_IDA2A2_STR));
            statement.appendAndIfNeeded();
            if (isApparel) {
                strColumnName = BO_APP_FLEXTYPE.getAttribute(SparcMigrationConstants.COM_NAME_ATTRIBUTE).getColumnDescriptorName();
                statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, strColumnName), "?", "="), name.trim());
                statement.appendAndIfNeeded();
                statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, LCSQuery.TYPED_BRANCH_ID), BO_APP_FLEXTYPE.getIdNumber(), Criteria.EQUALS));
            } else {
                strColumnName = BO_FTW_FLEXTYPE.getAttribute(SparcMigrationConstants.COM_NAME_ATTRIBUTE).getColumnDescriptorName();
                statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, strColumnName), "?", "="), name.trim());
                statement.appendAndIfNeeded();
                statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, LCSQuery.TYPED_BRANCH_ID), BO_FTW_FLEXTYPE.getIdNumber(), Criteria.EQUALS));
            }
            SearchResults results = LCSQuery.runDirectQuery(statement);
            if (results != null) {
                if (results.getResultsFound() > 0) {
                    FlexObject obj = (FlexObject) results.getResults().elementAt(0);
                    if (obj != null) {
                        objManaged =(LCSLifecycleManaged) LCSQuery.findObjectById(SparcMigrationConstants.BO_OR_STR+obj.getString(SparcMigrationConstants.BO_IDA2A2));
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return objManaged;
    }


    /**
     *
     * @param strSourcingNo
     * @param prodARev
     * @return
     * @throws WTException
     */
    public static LCSSourcingConfig getExistingSourcingConfig(String strSourcingNo,LCSProduct prodARev) throws WTException{
        LCSSourcingConfig srcConfig = null;
        PreparedQueryStatement pqs = new PreparedQueryStatement();
        pqs.appendFromTable("LCSSourcingConfig");
        pqs.appendSelectColumn("LCSSourcingConfig", "branchIditerationInfo");
        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria("LCSSourcingConfig", "productARevId", FormatHelper.getNumericVersionIdFromObject(prodARev), Criteria.EQUALS));
        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria("LCSSourcingConfig", "latestIterationInfo", "1", Criteria.EQUALS));
        SearchResults srcResults = LCSQuery.runDirectQuery(pqs);
        Collection srcColl = srcResults.getResults();
        if(srcColl!=null && srcColl.size()==1) {
            FlexObject fob = (FlexObject)srcColl.iterator().next();
            srcConfig = (LCSSourcingConfig) LCSQuery.findObjectById("VR:com.lcs.wc.sourcing.LCSSourcingConfig:"
                    +fob.getString("LCSSOURCINGCONFIG.BRANCHIDITERATIONINFO"));
        }
        return srcConfig;
    }

    /**
     *
     * @param matColorId
     * @return
     */
    public static LCSMaterialColor getMaterialColor(String matColorId) throws WTException{
        LCSMaterialColor objMaterialColor = null;
        objMaterialColor= (LCSMaterialColor)LCSQuery.findObjectById("OR:com.lcs.wc.material.LCSMaterialColor:"+ FormatHelper.applyFormat(matColorId, FormatHelper.LONG_FORMAT));
        return objMaterialColor;
    }

}
