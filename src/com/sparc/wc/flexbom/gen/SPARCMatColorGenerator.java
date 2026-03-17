/*
 * CWMatColorGenerator.java
 *
 * Created on Sept 13, 2007, 11:03 AM
 */

package com.sparc.wc.flexbom.gen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wt.fc.ReferenceFactory;
import wt.util.WTException;

import com.lcs.wc.client.web.FlexTypeGenerator;
import com.lcs.wc.flexbom.gen.*;
import com.lcs.wc.client.web.TableColumn;
import com.lcs.wc.client.web.TableData;
import com.lcs.wc.color.LCSColor;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flexbom.BOMColorTableColumn;
import com.lcs.wc.flexbom.BOMMaterialTableColumn;
import com.lcs.wc.flexbom.BOMPartNameTableColumn;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.FlexBOMPartMaster;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.flexbom.MCIDimensionHelper;
import com.lcs.wc.flexbom.MaterialColorInfo;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.material.ComplexMaterialColorQuery;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.material.LCSMaterialColor;
import com.lcs.wc.material.LCSMaterialMaster;
import com.lcs.wc.material.LCSMaterialQuery;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.product.PDFProductSpecificationGenerator2;
import com.lcs.wc.product.ProductDestination;
import com.lcs.wc.report.ColumnList;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.db.*;

import wt.fc.ReferenceFactory;
import wt.part.*;

import com.lcs.wc.color.LCSColor;
import com.lcs.wc.product.*;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.util.FlexObjectUtil;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSMessage;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.RB;
import com.lcs.wc.util.VersionHelper;

/**
 *
 * @author Chuck Modified by Quoc Pham. Added dimension column to the report
 */
public class SPARCMatColorGenerator extends SPARCBomDataGenerator {
   // private static final Logger LOGGER = LogManager.getLogger(MatColorGenerator.class.getName());

    public static final String MATERIAL_TYPE_PATH = "MATERIAL_TYPE_PATH";

    private static final String MATERIALREFID = "IDA3B5";

    private static final String SUPPLIERREFID = "IDA3C5";

    private static final String COLORS = "COLORS";

    private static final String SIZE1 = "SIZE1";

    private static final String SIZE2 = "SIZE2";

    private static final String DESTINATION = "DESTINATION";

    private static final String COLORIDS = "COLORIDS";

    private static final String COLORNAME = "COLORNAME";

    private static final String COLORDESC = "COLORDESC";

    private static final String COLORID = "COLORID";

    private static final String COLORHEX = "COLORHEX";

    private static final String COLORTHUMB = "COLORTHUMB";

    private static final String MATERIALCOLORID = "MATERIALCOLORID";

    private String materialNameDbColName;

    private String HIGH_LIGHT_ATT = "ATT1";

    private String materialDescDbColName;
    
    private String componentOrLocationDbColName;
    
    private String componentOrLocationDisplay;

    private String componentNameDbColName;

    private String supplierNameDbColName;

    private String colorNameDbColName;

    private String priceDbColName;

    private String componentNameDisplay;

    private String priceOverrideDbColName;

    private String quantityDbColName;

    private String lossAdjustmentDbColName;

    private String rowTotalDbColName;
    
	//changes to calculate rowTotal with new formula - start
    private String grossQuantityDbColName;
    private String cifDbColName;
	//changes to calculate rowTotal with new formula - end
    private String WCPARTNAME_DISPLAY = "ATT9";

    private String priceKey;

    private String overrideKey;

    private String quantityKey;

    private String lossAdjustmentKey;
    
	//changes to calculate rowTotal with new formula - start
    private String cifKey;
    private String grossQuantityKey;
	//changes to calculate rowTotal with new formula - end
    private String rowTotalKey;

    protected String colorwaysLabel = LCSMessage.getLocalizedMessage(RB.FLEXBOM, "colorwaysLabel");

    protected String colorLabel = LCSMessage.getLocalizedMessage(RB.COLOR, "color_LBL");

    protected String size1Label = LCSMessage.getLocalizedMessage(RB.QUERYDEFINITION, "size1");

    protected String size2Label = LCSMessage.getLocalizedMessage(RB.QUERYDEFINITION, "size2");

    public float partNameWidth = (new Float(
            LCSProperties.get("com.lcs.wc.flexbom.gen.MatColorGenerator.partNameWidth", "1.5"))).floatValue();

    public float materialNameWidth = (new Float(
            LCSProperties.get("com.lcs.wc.flexbom.gen.MatColorGenerator.materialNameWidth", "1.25"))).floatValue();

    public float supplierNameWidth = (new Float(
            LCSProperties.get("com.lcs.wc.flexbom.gen.MatColorGenerator.supplierNameWidth", "1.25"))).floatValue();

    public float colorwayWidth = (new Float(
            LCSProperties.get("com.lcs.wc.flexbom.gen.MatColorGenerator.colorwayWidth", "0.75"))).floatValue();

    public int imageWidth = (new Integer(
            LCSProperties.get("com.lcs.wc.flexbom.gen.MatColorGenerator.matThumbWidth", "75"))).intValue();

    public int imageHeight = (new Integer(
            LCSProperties.get("com.lcs.wc.flexbom.gen.MatColorGenerator.matThumbHeight", "0"))).intValue();

    public boolean USE_DEFAULT_COLUMNS = LCSProperties
            .getBoolean("com.lcs.wc.flexbom.gen.MatColorGenerator.useDefaultColumns");

    private static final String DISPLAY_VAL = "DISPLAY_VAL";

    private Map<String, Collection<String>> dimensionMap;

    /** Creates a new instance of MatColorGenerator */
    private FlexType materialType = null;

    private FlexType supplierType = null;

    private FlexType bomType = null;

    public SPARCMatColorGenerator() {
    }

    public Collection getBOMData() throws WTException {
        return this.dataSet;
    }

    private void debug(String msg) {
	/*
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(msg);
        }
	*/	
    }

    public Collection<String> getViewAttributes(ColumnList view) {
        ArrayList<String> viewAtts = new ArrayList<String>();
        if (view != null) {
            debug("view attributes: " + view.getAttributes());
            if (view.getAttributes() != null) {
                viewAtts.addAll(view.getAttributes());
            }

            viewAtts.removeAll(getViewAttributesToRemoved());

            if (!viewAtts.contains("Colorways")) {
                viewAtts.add("Colorways");
            }
        }

        if (WCPART_ENABLED && view == null) {
            viewAtts.add(0, "BOM.wcPartName");
        }

        viewAtts.add(0, "supplierName");
        viewAtts.add(0, "materialDescription");

        if (this.getDestinations() != null && this.getDestinations().size() > 0) {
            viewAtts.add(0, DESTINATION);
        }

        // Always dispaly Source Column. If it is null, display all sources
        viewAtts.add(0, SOURCES);

        if (this.getSizes2() != null && this.getSizes2().size() > 0) {
            viewAtts.add(0, SIZE2);
        }
        if (this.getSizes1() != null && this.getSizes1().size() > 0) {
            viewAtts.add(0, SIZE1);
        }
        if (this.getColorways() != null && this.getColorways().size() > 0) {
            viewAtts.add(0, COLORS);
        }
// HARSHAD- Component Part Attribute Replacement
      //  viewAtts.add(0, "materialDescription");
		viewAtts.add(0, "scComponent");

        if (this.useMatThumbnail) {
            viewAtts.add(0, "MATERIAL.thumbnail");
        }

        if (view == null) {
            if (USE_DEFAULT_COLUMNS) {
                viewAtts.add("Material.price");
                viewAtts.add("Material.unitOfMeasure");
                viewAtts.add("BOM.quantity");
            }
            viewAtts.add("Colorways");
        }
        return viewAtts;
    }

    private Collection<String> getViewAttributesToRemoved() {
        Collection<String> viewAttributesToRemoved = new ArrayList<String>();
		// HARSHAD- Component Part Attribute Replacement

        viewAttributesToRemoved.add("BOM.scComponent");
        viewAttributesToRemoved.add("BOM.materialDescription");
        viewAttributesToRemoved.add("supplierName");
        viewAttributesToRemoved.add("materialDescription");
        if (!WCPART_ENABLED) {
            viewAttributesToRemoved.add("BOM.wcPartName");
        }
        return viewAttributesToRemoved;
    }

    public Map<String, TableColumn> getViewColumns(ColumnList view) throws WTException {
        Map<String, TableColumn> viewColumns = new HashMap<String, TableColumn>();
        if (view != null) {
            viewColumns.putAll(getViewColumns());
            debug("viewColumn keys: " + viewColumns.keySet());
        }
        if (view == null) {
            if (USE_DEFAULT_COLUMNS) {
                TableColumn column = null;
                FlexTypeGenerator flexg = new FlexTypeGenerator();
                FlexTypeAttribute att = null;

                att = materialType.getAttribute("materialPrice");
                column = flexg.createTableColumn(null, att, materialType, false, "LCSMATERIALSUPPLIER");
                viewColumns.put("Material.price", column);

                att = materialType.getAttribute("unitOfMeasure");
                column = flexg.createTableColumn(null, att, materialType, false, "LCSMATERIAL");
                viewColumns.put("Material.unitOfMeasure", column);

                att = bomType.getAttribute("quantity");
                column = flexg.createTableColumn(null, att, bomType, false, "FLEXBOMLINK");
                viewColumns.put("BOM.quantity", column);
            }
        }
        TableColumn column = new TableColumn();

        column = new BOMPartNameTableColumn();
        column.setHeaderLabel(componentNameDisplay);
        column.setTableIndex("FLEXBOMLINK." + componentNameDbColName);
        column.setDisplayed(true);
        ((BOMPartNameTableColumn) column).setSubComponetIndex("FLEXBOMLINK.MASTERBRANCHID");
        ((BOMPartNameTableColumn) column).setComplexMaterialIndex("FLEXBOMLINK.MASTERBRANCH");
        ((BOMPartNameTableColumn) column).setLinkedBOMIndex("FLEXBOMLINK.LINKEDBOM");
		// HARSHAD- Component Part Attribute Replacement
        column.setSpecialClassIndex("CLASS_OVERRIDE");
        column.setPdfColumnWidthRatio(partNameWidth);
        //Commented Code By DS
        //viewColumns.put("materialDescription", column);
		viewColumns.put("scComponent", column);

        if (WCPART_ENABLED) {
            column = new TableColumn();
            column.setHeaderLabel(WCPARTNAME_DISPLAY);
            column.setTableIndex(WCPARTNAME);
            column.setDisplayed(true);
            column.setFormat(FormatHelper.MOA_FORMAT);
            viewColumns.put("BOM.wcPartName", column);
        }

        column = new TableColumn();
        column.setHeaderLabel(LCSMessage.getLocalizedMessage(RB.SOURCING, "sourceColumn_LBL"));
        column.setTableIndex(SOURCES);
        column.setDisplayed(true);
        column.setFormat(FormatHelper.MOA_FORMAT);
        viewColumns.put(SOURCES, column);

        column = new TableColumn();
        column.setHeaderLabel(colorwaysLabel);
        column.setTableIndex(COLORS);
        column.setDisplayed(true);
        column.setFormat(FormatHelper.MOA_FORMAT);
        viewColumns.put(COLORS, column);

        column = new TableColumn();
        column.setHeaderLabel(size1Label);
        column.setTableIndex(SIZE1);
        column.setDisplayed(true);
        column.setFormat(FormatHelper.MOA_FORMAT);
        viewColumns.put(SIZE1, column);

        column = new TableColumn();
        column.setHeaderLabel(size2Label);
        column.setTableIndex(SIZE2);
        column.setDisplayed(true);
        column.setFormat(FormatHelper.MOA_FORMAT);
        viewColumns.put(SIZE2, column);

        column = new TableColumn();
        column.setHeaderLabel(LCSMessage.getLocalizedMessage(RB.FLEXBOM, "destination_noColon_LBL"));
        column.setTableIndex(DESTINATION);
        column.setDisplayed(true);
        column.setFormat(FormatHelper.MOA_FORMAT);
        viewColumns.put(DESTINATION, column);

        column = new BOMMaterialTableColumn();
        column.setHeaderLabel(this.materialLabel);
        column.setTableIndex("LCSMATERIAL." + materialNameDbColName);
        column.setDisplayed(true);
        column.setPdfColumnWidthRatio(materialNameWidth);
        column.setLinkMethod("viewMaterial");
        column.setLinkTableIndex("childId");
        column.setLinkMethodPrefix("OR:com.lcs.wc.material.LCSMaterialMaster:");
        ((BOMMaterialTableColumn) column).setDescriptionIndex("FLEXBOMLINK." + materialDescDbColName);
        viewColumns.put("materialDescription", column);

        column = new TableColumn();
        column.setHeaderLabel(this.supplierLabel);
        column.setTableIndex("LCSSUPPLIERMASTER.SUPPLIERNAME");
        column.setDisplayed(true);
        column.setPdfColumnWidthRatio(supplierNameWidth);
        column.setFormat(FormatHelper.STRING_FORMAT);
        viewColumns.put("supplierName", column);

        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel("");
        column.setHeaderAlign("left");
        column.setLinkMethod("launchImageViewer");
        column.setLinkTableIndex("LCSMATERIAL.PRIMARYIMAGEURL");
        column.setTableIndex("LCSMATERIAL.PRIMARYIMAGEURL");
        column.setColumnWidth("1%");
        column.setLinkMethodPrefix("");
        column.setImage(true);
        column.setShowFullImage(this.useMatThumbnail);

        if (imageWidth > 0) {
            column.setImageWidth(imageWidth);
        }
        if (imageHeight > 0) {
            column.setImageHeight(imageHeight);
        }
        viewColumns.put("MATERIAL.thumbnail", column);

        debug("Getting columns...colorways: " + getColorways());
        if (this.getColorways() != null && this.getColorways().size() > 0) {
            ReferenceFactory rf = new ReferenceFactory();
            Map<String, LCSSKU> cwId_SKU_map = new HashMap<String, LCSSKU>();
            if (!this.getColorways().isEmpty()) {
                Collection<LCSSKU> skus = LCSSKUQuery.getSKURevA(this.getColorways());
                for (LCSSKU sku : skus) {
                    String refString = rf.getReferenceString(sku.getMasterReference());
                    String idString = refString.substring(refString.lastIndexOf(":") + 1);
                    cwId_SKU_map.put(idString, sku);
                }
            }
            for (String cwId : this.getColorways()) {
                BOMColorTableColumn colorColumn = new BOMColorTableColumn();
                colorColumn.setDisplayed(true);
                colorColumn.setTableIndex(cwId + "." + COLORNAME);
                colorColumn.setDescriptionIndex(cwId + "." + COLORDESC);
                colorColumn.setHeaderLabel((String) cwId_SKU_map.get(cwId).getValue("skuName"));
                colorColumn.setLinkMethod("viewColor");
                colorColumn.setLinkTableIndex(cwId + "." + COLORID);
                colorColumn.setLinkMethodPrefix("OR:com.lcs.wc.color.LCSColor:");
                colorColumn.setColumnWidth("1%");
                colorColumn.setWrapping(false);
                colorColumn.setBgColorIndex(cwId + "." + COLORHEX);
                colorColumn.setUseColorCell(true);
                colorColumn.setAlign("center");
                colorColumn.setImageIndex(cwId + "." + COLORTHUMB);
                colorColumn.setSpecialClassIndex(cwId + "_CLASS_OVERRIDE");
                colorColumn.setPdfColumnWidthRatio(colorwayWidth);
                colorColumn.setUseColorCell(this.useColorSwatch);
                colorColumn.setFormatHTML(false);
                viewColumns.put(cwId + "." + DISPLAY_VAL, colorColumn);
            }
        } else {
            BOMColorTableColumn colorColumn = new BOMColorTableColumn();
            colorColumn.setDisplayed(true);
            colorColumn.setTableIndex("LCSCOLOR.COLORNAME");
            colorColumn.setDescriptionIndex("FLEXBOMLINK." + colorNameDbColName);
            colorColumn.setHeaderLabel(colorLabel);
            colorColumn.setLinkMethod("viewColor");
            colorColumn.setLinkTableIndex("LCSCOLOR.IDA2A2");
            colorColumn.setLinkMethodPrefix("OR:com.lcs.wc.color.LCSColor:");
            colorColumn.setColumnWidth("1%");
            colorColumn.setWrapping(false);
            colorColumn.setBgColorIndex("LCSCOLOR.COLORHEXIDECIMALVALUE");
            colorColumn.setUseColorCell(true);
            colorColumn.setAlign("center");
            colorColumn.setImageIndex("LCSCOLOR.THUMBNAIL");
            colorColumn.setUseColorCell(this.useColorSwatch);
            colorColumn.setFormatHTML(false);
            viewColumns.put(DISPLAY_VAL, colorColumn);
        }
        return viewColumns;
    }

    public Collection<TableColumn> getTableColumns() throws WTException {
        Collection<String> viewAtts = getViewAttributes(this.view);
        Map<String, TableColumn> viewColumns = getViewColumns(this.view);
        Collection<TableColumn> columns = new ArrayList<TableColumn>();
        for (String att : viewAtts) {
            if ("Colorways".equals(att)) {
                if (this.getColorways() != null && this.getColorways().size() > 0) {
                    for (String cwId : this.getColorways()) {
                        if (viewColumns.get(cwId + "." + DISPLAY_VAL) != null) {
                            columns.add(viewColumns.get(cwId + "." + DISPLAY_VAL));
                        }
                    }
                } else {
                    columns.add(viewColumns.get(DISPLAY_VAL));
                }
            } else {
                if (viewColumns.get(att) != null) {
                    columns.add(viewColumns.get(att));
                }
            }
        }

        Iterator columnGroups = columns.iterator();
        TableColumn singleColumn;
        Collection<TableColumn> columnResults = new ArrayList<TableColumn>(columns.size());
        while (columnGroups.hasNext()) {
            singleColumn = (TableColumn) columnGroups.next();
            if (singleColumn != null && !(singleColumn instanceof BOMColorTableColumn)) {
                singleColumn.setColumnClassIndex("FLEXBOMLINK." + HIGH_LIGHT_ATT);
                columnResults.add(singleColumn);
            } else {
                columnResults.add(singleColumn);
            }
        }
        return columnResults;
    }

    public void init(Map<String, Object> params) throws WTException {

        super.init(params);

        if (true) {
            HashMap<String, Object> tparams = new HashMap<String, Object>(params);
            tparams.remove(SPARCBomDataGenerator.RAW_BOM_DATA);
          //  LOGGER.debug("tparams: " + tparams);
        }

        if (params != null) {
            if (params.get(MATERIAL_TYPE_PATH) != null) {
                materialType = FlexTypeCache.getFlexTypeFromPath((String) params.get(MATERIAL_TYPE_PATH));
            } else {
                materialType = FlexTypeCache.getFlexTypeRoot("Material");
            }
            supplierType = FlexTypeCache.getFlexTypeRoot("Supplier");
            //DS - Added Path
            bomType = FlexTypeCache.getFlexTypeFromPath("BOM\\Materials");
            // HARSHAD- Component Part Attribute Replacement
            materialNameDbColName = materialType.getAttribute("name").getColumnName();
            supplierNameDbColName = supplierType.getAttribute("name").getColumnName();
            componentNameDbColName = bomType.getAttribute("scComponent").getColumnName();
            colorNameDbColName = bomType.getAttribute("colorDescription").getColumnName();
			// HARSHAD- Component Part Attribute Replacement
			//componentOrLocationDisplay = bomType.getAttribute("scComponent").getAttDisplay();
            priceDbColName = materialType.getAttribute("materialPrice").getColumnName();
            materialDescDbColName = bomType.getAttribute("materialDescription").getColumnName();
            componentNameDisplay = bomType.getAttribute("scComponent").getAttDisplay();

            priceOverrideDbColName = bomType.getAttribute("priceOverride").getColumnName();
            quantityDbColName = bomType.getAttribute("quantity").getColumnName();
            lossAdjustmentDbColName = bomType.getAttribute("lossAdjustment").getColumnName();
            rowTotalDbColName = bomType.getAttribute("rowTotal").getColumnName();
            HIGH_LIGHT_ATT = bomType.getAttribute("highLight").getColumnName();
            
			//changes to calculate rowTotal with new formula - start
            cifDbColName = bomType.getAttribute("scCIF").getColumnName();
            grossQuantityDbColName = bomType.getAttribute("scGrossQuantity").getColumnName();
            //changes to calculate rowTotal with new formula - end
            if (WCPART_ENABLED) {
                WCPARTNAME_DISPLAY = bomType.getAttribute("wcPartName").getAttDisplay();
            }
            priceKey = "LCSMATERIALSUPPLIER." + priceDbColName;
            overrideKey = "FLEXBOMLINK." + priceOverrideDbColName;
            quantityKey = "FLEXBOMLINK." + quantityDbColName;
            lossAdjustmentKey = "FLEXBOMLINK." + lossAdjustmentDbColName;
            rowTotalKey = "FLEXBOMLINK." + rowTotalDbColName;
            //changes to calculate rowTotal with new formula - start
            cifKey = "FLEXBOMLINK." + cifDbColName;
            grossQuantityKey = "FLEXBOMLINK." + grossQuantityDbColName;
			//changes to calculate rowTotal with new formula - end
            if (this.dataSet != null) {
                this.dataSet = groupDataToBranchId(this.dataSet, "FLEXBOMLINK.BRANCHID", "FLEXBOMLINK.MASTERBRANCHID",
                        "FLEXBOMLINK.SORTINGNUMBER");
                Collection<TableData> processedData = new ArrayList<TableData>();
                Collection<FlexObject> topLevelBranches = this.getTLBranches();

                for (FlexObject topLevelBranch : topLevelBranches) {

                    // CMS - This code has been simplified. Since the complex materials records are included, it isn't
                    // necessary to process them differently/seperately. They can be processed as a regular top level
                    // branch.
                    /*
                     * boolean isSubComponent =
                     * FormatHelper.hasContent(topLevelBranch.getString("FLEXBOMLINK.MASTERBRANCHID")); boolean
                     * isComplexMaterial = FormatHelper.hasContent(topLevelBranch.getString("FLEXBOMLINK.MASTERBRANCH"))
                     * || FormatHelper.hasContent(topLevelBranch.getString("FLEXBOMLINK.LINKEDBOM"));
                     * 
                     * if (!isSubComponent) { if (isComplexMaterial) processedData.addAll(processComplexBranch
                     * (topLevelBranch)); else processedData.addAll(processBranch (topLevelBranch)); }
                     */

                    processedData.addAll(processBranch(topLevelBranch));
                }
                this.dataSet = processedData;
            }

            if (FormatHelper.hasContent((String) params.get(PDFProductSpecificationGenerator2.PRODUCT_SIZE_CAT_ID))) {
                ProductSizeCategory productSizeCategory = (ProductSizeCategory) LCSQuery
                        .findObjectById((String) params.get(PDFProductSpecificationGenerator2.PRODUCT_SIZE_CAT_ID));
                size1Label = productSizeCategory.getSizeRange().getFullSizeRange().getSize1Label();
                size2Label = productSizeCategory.getSizeRange().getFullSizeRange().getSize2Label();

            }

        }
    }

    /**
     * Process a BOM component record. We use the MCIdimensionHelper method to calculate the materials and colors data
     * for each dimension set
     */
    private Collection<FlexObject> processBranch(FlexObject topLevel) throws WTException {

        calculatePrice(topLevel);

        // Initialize all the variables and data structures needed
        ArrayList<FlexObject> data = new ArrayList<FlexObject>();
		// HARSHAD- Component Part Attribute Replacement
        String partNameIndex = FlexTypeCache.getFlexTypeFromPath("BOM\\Materials").getAttribute("scComponent").getSearchResultIndex();
        String partName = topLevel.getString(partNameIndex);
		
		if(FormatHelper.hasContent(partName)){
			LCSLifecycleManaged compLocObj = (LCSLifecycleManaged) LCSQuery.findObjectById("OR:com.lcs.wc.foundation.LCSLifecycleManaged:"+partName);
			
			if(null != compLocObj){
				partName = compLocObj.getValue("name").toString();
			}
		}
		
		//String partNameIndx = FlexTypeCache.getFlexTypeRoot("BOM").getAttribute("scComponent").getSearchResultIndex();
        //String partName = topLevel.getString(partNameIndx);

        // If the dimension map hasn't been initialized, build the map
        if (dimensionMap == null)
            initDimensionMap(topLevel);

        MaterialColorInfo topLevelMci = MCIDimensionHelper.getMaterialColorInfo(topLevel);
        Map ovrMap = getOverRideMap();
        String branchKey = topLevel.getString(DIMID_COL);
        Collection ovrForBranch = (Collection) ovrMap.get(branchKey);

        if (ovrForBranch.size() > 0) {
            // If the branch has any overrides, initialize a MCIDimensionHelper Object
            MCIDimensionHelper mciHelper = new MCIDimensionHelper(dimensionMap, topLevel, ovrForBranch);
            Map<MaterialColorInfo, Collection> materialMciMap = mciHelper
                    .groupMcisByMaterialSupplier(mciHelper.getAllUniqueMaterialColorInfo());
            // LOGGER.debug("materialMciMap: " + materialMciMap);
            debug("\n processBranch-materialMciMap: " + materialMciMap);
            for (MaterialColorInfo materialMci : materialMciMap.keySet()) {
                // Get material/Supplier info
                Collection matColorMciList = (Collection) materialMciMap.get(materialMci);
                Map<Map, Collection> dimensionMciMap = mciHelper.groupMcisByUniqueDimensionSet(matColorMciList);
                debug("\n processBranch-dimensionMciMap: " + dimensionMciMap);

                for (Map dimensions : dimensionMciMap.keySet()) {
                    Collection<MaterialColorInfo> colorMciList = dimensionMciMap.get(dimensions);
                    debug("\n processBranch-colorMciList: " + colorMciList);
                    // For each unique dimensions set, create a new row and populate material and dimensions data
                    FlexObject newRow = topLevel.dup();
                    newRow.put(partNameIndex, "");
                    debug("\n processBranch-newRow: " + newRow);

                    // Added/Modified to account for need for additional info
                    FlexObject ovr = getOverRideRecord(ovrForBranch, materialMci);
                    debug("\n processBranch-ovr: " + ovr);

                    // SPR 2130492 do not copy the highlight from the toplevel ,we need set children's highlight or
                    // strikethrough if it has
//               if(ovr!=null){
//            	  String highlight = ovr.getString("FLEXBOMLINK."+HIGH_LIGHT_ATT);
//            	  if(FormatHelper.hasContent(highlight))
//            		 newRow.put("FLEXBOMLINK."+HIGH_LIGHT_ATT, highlight);
//               }

                    addMaterialData(newRow, materialMci, ovr);
                    debug("\n processBranch-newRow + addMaterialData: " + newRow);

                    addDimensionsData(newRow, mciHelper.getMergedDimensions(colorMciList));
                    debug("\n processBranch-newRow + addDimensionsData: " + newRow);
                    blankAllColorData(newRow);
                    debug("\n processBranch-newRow + blankAllColorData: " + newRow);

                    // For this component row, populate color data for every applicable colorway dimension
                    for (MaterialColorInfo colorMci : colorMciList) {
                        Collection<String> skus = mciHelper.getColorwayDimension(colorMci);

                        for (String skuId : skus) {
                            // Given the processing of the colorMcis, it is unclear under what circumstance the
                            // color data would not be applicable to this record.
                            // if(isTopLevelDimension(dimensions, skus)){
                            // addColorData(newRow, skuId, topLevelMci);
                            // debug("\n processBranch-newRow + addColorData, isTopLevel: " + newRow);
                            // }else{
                            addColorData(newRow, skuId, colorMci);
                            debug("\n processBranch-newRow + addColorData, not top level: " + newRow);
                            // }
                        }

                        if (skus == null || skus.size() < 1) {
                            Collection sources = mciHelper.getSourceDimension(colorMci);
                            for (Object source : sources) {
                                if (isTopLevelDimension(dimensions)) {
                                    newRow.put("LCSCOLOR.COLORNAME", topLevelMci.colorName);
                                    newRow.put("FLEXBOMLINK." + colorNameDbColName, topLevelMci.colorDescription);
                                    newRow.put("LCSCOLOR.IDA2A2", topLevelMci.colorId);
                                    newRow.put("LCSCOLOR.COLORHEXIDECIMALVALUE", topLevelMci.colorHexValue);
                                } else {
                                    newRow.put("LCSCOLOR.COLORNAME", colorMci.colorName);
                                    newRow.put("FLEXBOMLINK." + colorNameDbColName, colorMci.colorDescription);
                                    newRow.put("LCSCOLOR.IDA2A2", colorMci.colorId);
                                    newRow.put("LCSCOLOR.COLORHEXIDECIMALVALUE", colorMci.colorHexValue);
                                    // addColorData(newRow, sourceId, colorMci);
                                }
                            }
                        }
                    }
                    calculatePrice(newRow);
                    debug("\n processBranch-newRow + before add to data: " + newRow);
                    data.add(newRow);
                }
            }
        }
        if (!(data.size() > 0)) {// If the data is empty, it means there's no related ovr branches were processed.
            if (topLevelMci.hasMaterialInfo()) {
                // If the branch doesn't have any overrides, populate top level material and color
                FlexObject newRow = topLevel.dup();
                addMaterialData(newRow, topLevelMci, null);
                addAllColorData(newRow, topLevelMci);
                addDimensionsData(newRow, dimensionMap);
                calculatePrice(newRow);
                data.add(newRow);
            } else if (topLevelMci.hasColorInfo()) {
                // If the branch doesn't have any overrides, populate top level material and color
                FlexObject newRow = topLevel.dup();
                addAllColorData(newRow, topLevelMci);
                addDimensionsData(newRow, dimensionMap);
                calculatePrice(newRow);
                data.add(newRow);
            } else {
                // For undefined component, add a blank row
                FlexObject newRow = topLevel.dup();
                calculatePrice(newRow);
                data.add(newRow);
            }
        }

        // Sort all rows and populate the partName only for the first Row
        Collection<FlexObject> sortedData = sortBranchDataSet(data);
        FlexObject firstRow = (FlexObject) sortedData.iterator().next();
        firstRow.put(partNameIndex, partName);

        return sortedData;
    }

    private boolean isTopLevelDimension(Map dimension) {
        return isTopLevelDimension(dimension, null);
    }

    private boolean isTopLevelDimension(Map dimension, Collection skus) {
        debug("isTopLevelDimension - dimension: " + dimension);
        debug("isTopLevelDimension - skus: " + skus);
        Map topLevelDimension = this.dimensionMap;
        debug("topLevelDimension: " + topLevelDimension);
        if (((Collection) topLevelDimension.get("SOURCE")).size() != ((Collection) dimension.get("SOURCE")).size())
            return false;

        if (topLevelDimension.containsKey("SKU") && skus != null) {
            if (((Collection) topLevelDimension.get("SKU")).size() != skus.size())
                return false;
        }
        if (((Collection) topLevelDimension.get("DESTINATION")).size() != ((Collection) dimension.get("DESTINATION"))
                .size())
            return false;

        if (((Collection) topLevelDimension.get("SIZE1")).size() != ((Collection) dimension.get("SIZE1")).size())
            return false;

        if (((Collection) topLevelDimension.get("SIZE2")).size() != ((Collection) dimension.get("SIZE2")).size())
            return false;

        return true;
    }

    private void calculatePrice(FlexObject row) throws WTException {

        double materialPrice = FormatHelper.parseDouble(row.getData(priceKey));
        double priceOverride = FormatHelper.parseDouble(row.getData(overrideKey));
        double quantity = FormatHelper.parseDouble(row.getData(quantityKey));
        double lossAdjustment = FormatHelper.parseDouble(row.getData(lossAdjustmentKey));
        
		//changes to calculate rowTotal with new formula - start
        double cif = FormatHelper.parseDouble(row.getData(cifKey));
        double grossQuantity = FormatHelper.parseDouble(row.getData(grossQuantityKey));
        if (priceOverride > 0) {
            materialPrice = priceOverride;
        }
       /* if (lossAdjustment != 0) {
            quantity = quantity + (quantity * lossAdjustment);
        }

        double rowTotal = quantity * materialPrice;
        
        */
        double rowTotal  = ( materialPrice + cif ) * grossQuantity;
		//changes to calculate rowTotal with new formula - end
        row.put(rowTotalKey, "" + rowTotal);

    }

    private FlexObject getOverRideRecord(Collection<FlexObject> records, MaterialColorInfo mci) {

        for (FlexObject record : records) {
            if (mci.materialSupplierId.equals(record.getData("LCSMATERIALSUPPLIERMASTER.IDA2A2"))) {
                return record;
            } else if (!FormatHelper.hasContent(mci.materialSupplierId)
                    && FormatHelper.hasContent(mci.materialName)
                    && mci.materialName.equals(record.getData("FLEXBOMLINK." + materialDescDbColName))) {

                return record;
            }
        }

        return null;
    }

    /**
     * Process a complex material row. We first find all the combinations for the top level using the processBranch
     * method We then loop through each resulting rows and find the related Material BOM. We populate the applicable
     * subcomponents from the Bom and determine the color data from the SKU overrides
     */
    private Collection processComplexBranch(FlexObject topLevel) throws WTException {

        // calculatePrice(topLevel);

        Collection<FlexObject> processedData = new ArrayList<FlexObject>();
        List<String> sortKeys = new ArrayList<String>();
        sortKeys.add("FLEXBOMLINK.SORTINGNUMBER");

        // Get all the top Level overrides
        Collection<FlexObject> topComponents = processBranch(topLevel);

        for (FlexObject topComponent : topComponents) {
            // For each complex Material Override, we find the material BOM
            String materialId = "OR:com.lcs.wc.material.LCSMaterialMaster:"
                    + topComponent.getString("FLEXBOMLINK." + MATERIALREFID);

            if (materialId.equals(LCSMaterialQuery.PLACEHOLDERID)) {
                materialId = "OR:com.lcs.wc.material.LCSMaterialMaster:"
                        + topComponent.getString("LCSMATERIAL.IDA3MASTERREFERENCE");
            }

            LCSMaterialMaster materialMaster = (LCSMaterialMaster) LCSQuery.findObjectById(materialId);
            LCSMaterial material = (LCSMaterial) VersionHelper.latestIterationOf(materialMaster);

            FlexBOMPart subBomPart;
            Collection bomParts = new LCSFlexBOMQuery().findBOMPartsForOwner(material);

            if (!bomParts.isEmpty()) {
                // If material Bom is found, get all the subcomponents top level branches
                subBomPart = (FlexBOMPart) bomParts.iterator().next();
                Map overrideMap = MCIDimensionHelper.getOverrideMap(subBomPart, materialType);
                Collection<FlexObject> subComponentsList = new ArrayList<FlexObject>();
                Collection<FlexObject> topLevelBranches = overrideMap.keySet();

                for (FlexObject topLevelComplexBranch : topLevelBranches) {

                    if (!FormatHelper.hasContent(topLevelComplexBranch.getString(DIM_COL)) &&
                            MCIDimensionHelper.getMaterialColorInfo(topLevelComplexBranch).hasMaterialInfo()/*
                                                                                                             * &&
                                                                                                             * FormatHelper
                                                                                                             * .
                                                                                                             * hasContent
                                                                                                             * (
                                                                                                             * partName)
                                                                                                             */) {
                        // If the sub component top level is defined, create a new row and populate color data using
                        // only SKU overrides
                        FlexObject newRow = topLevelComplexBranch.dup();
                        newRow.put("FLEXBOMLINK.MASTERBRANCHID", topComponent.getString("FLEXBOMLINK.BRANCHID"));
                        blankAllColorData(newRow);
                        addAllSubComponentsColorData(newRow, topComponent, subBomPart);
                        calculatePrice(newRow);
                        subComponentsList.add(newRow);
                    }
                }
                // Sort the subcomponents,
                subComponentsList = FlexObjectUtil.sortFlexObjects(subComponentsList, sortKeys);
                // add the topcomponent row and all its sub components
                processedData.add(topComponent);
                processedData.addAll(subComponentsList);
            } else {
                // If material Bom cannot be found, only add top component
                processedData.add(topComponent);
            }
        }
        return processedData;
    }

    /**
     * This is the sort order for the data on the report
     */
    private Collection<FlexObject> sortBranchDataSet(Collection<FlexObject> dataSet) {

        List<String> sortKeys = new ArrayList<String>();
        sortKeys.add(SOURCES);
        sortKeys.add(COLORS);
        sortKeys.add(SIZE1);
        sortKeys.add(SIZE2);
        sortKeys.add(DESTINATION);
        return FlexObjectUtil.sortFlexObjects(dataSet, sortKeys);
    }

    /**
     * Initialize the dimension map used to initialize the MCIDimensionHelper Class
     */
    private void initDimensionMap(FlexObject TopLevel) throws WTException {

        dimensionMap = new HashMap<String, Collection<String>>();
        dimensionMap.put(MCIDimensionHelper.SKU, this.getColorways());
        dimensionMap.put(MCIDimensionHelper.SIZE1, this.getSizes1());
        dimensionMap.put(MCIDimensionHelper.SIZE2, this.getSizes2());

        // If no sources have been selected on the User Interface, get all the sources for the product to display in the
        // report
        if (this.getSources().size() == 0) {
          //  LOGGER.debug("Source is NULL");
            String bomOid = "OR:com.lcs.wc.flexbom.FlexBOMPartMaster:" + TopLevel.getString("FLEXBOMLINK.IDA3A5");
            FlexBOMPartMaster bomMaster = (FlexBOMPartMaster) LCSQuery.findObjectById(bomOid);
            FlexBOMPart bomPart = (FlexBOMPart) VersionHelper.latestIterationOf(bomMaster);
            LCSPartMaster productMaster = (LCSPartMaster) bomPart.getOwnerMaster();
            Collection<LCSSourcingConfig> allSources = LCSSourcingConfigQuery
                    .getSourcingConfigForProduct(productMaster);
            Collection<String> allSourcesId = new ArrayList<String>();

            for (LCSSourcingConfig source : allSources) {
                String sourceId = FormatHelper
                        .getNumericObjectIdFromObject((LCSSourcingConfigMaster) source.getMaster());
                allSourcesId.add(sourceId);
            }
            dimensionMap.put(MCIDimensionHelper.SOURCE, allSourcesId);
        } else {
            dimensionMap.put(MCIDimensionHelper.SOURCE, this.getSources());
        }

        // We need to check if the format of the destination Id is correct before initializing the MCIdimenstionHelper.
        // This is needed because when we run the report from the linesheet, it seems like the destination Ids are
        // passed in differently
        Collection<String> destinationList = new ArrayList<String>();

        for (String destinationId : this.getDestinations()) {

            if (destinationId.startsWith("OR:")) {
                destinationId = FormatHelper.getNumericFromOid(destinationId);
            }

            destinationList.add(destinationId);
        }
        dimensionMap.put(MCIDimensionHelper.DESTINATION, destinationList);
    }

    /**
     * Populates material and supplier data for a row
     */
    private void addMaterialData(FlexObject row, MaterialColorInfo mci, FlexObject orRow) throws WTException {
        debug("\nrow: " + row);
        debug("\nmci: " + mci);
        debug("\norRow: " + orRow);

        if (orRow != null) {
            Collection<TableColumn> columns = getTableColumns();
            List needColumns = new ArrayList();
            String attType = "";
            // Fix SPR#2180666, add the attribute value from override row if this attribute has been included on custom
            // view, for example, the BOM view contained a color object reference attribute on Material
            for (TableColumn column : columns) {
                attType = column.getAttributeType();
                if (FormatHelper.hasContent(attType)
                        && (attType.equalsIgnoreCase("object_ref") || attType.equalsIgnoreCase("object_ref_list"))) {
                    needColumns.add(column.getTableIndex().toUpperCase());
                    debug("\n\nThe potential attributes need to copy: TABLEINDEX: " + column.getTableIndex()
                            + " HEADLABEL: " + column.getHeaderLabel());
                }
            }

            for (String key : orRow.keySet()) {
                if (key.startsWith("LCSMATERIAL") || key.startsWith("LCSSUPPLIER")) {
                    row.put(key, orRow.get(key));
                } else if (needColumns.contains(key) && !FormatHelper.hasContent(row.getString(key))) {
                    row.put(key, orRow.get(key));
                    debug("\n\nAdding view attributes, KEY: " + key + " VALUE: " + orRow.get(key));
                }
            }
            if (!FormatHelper.hasContent(row.getString("FLEXBOMLINK." + componentNameDbColName))
                    && FormatHelper.hasContent(orRow.getString("FLEXBOMLINK." + componentNameDbColName))) {
                row.put("FLEXBOMLINK." + componentNameDbColName, orRow.get("FLEXBOMLINK." + componentNameDbColName));
            }
        }

        row.put("FLEXBOMLINK." + MATERIALREFID, mci.materialId);
        row.put("LCSMATERIAL." + materialNameDbColName, mci.materialName);
        row.put("LCSSUPPLIERMASTER.SUPPLIERNAME", mci.supplierName);
        row.put("LCSSUPPLER." + supplierNameDbColName, mci.supplierName);
        row.put("LCSMATERIALSUPPLIERMASTER.IDA2A2", mci.materialSupplierId);
        row.put("FLEXBOMLINK." + SUPPLIERREFID, mci.supplierId);
        row.put("LCSMATERIALCOLOR.IDA2A2", mci.materialColorId);

    }

    /**
     * Populates color data for all colorway dimensions in a row
     */
    private void addAllColorData(FlexObject row, MaterialColorInfo colorMci) throws WTException {
        for (String skuId : this.getColorways()) {
            addColorData(row, skuId, colorMci);
        }
    }

    /**
     * Populates "X" color data for all colorway dimensions in a row
     */
    private void blankAllColorData(FlexObject row) throws WTException {
        debug("blankAllColorData this.getColorways().size(): " + this.getColorways().size());
        debug("blankAllColorData this.getColorways(): " + this.getColorways());
        for (String skuId : this.getColorways()) {
            blankColorData(skuId, row);
        }
    }

    /**
     * Populates color data for all colorway dimensions in a complex material sub component row
     */
    private void addAllSubComponentsColorData(FlexObject subRow, FlexObject topRow, FlexBOMPart bomPart)
            throws WTException {
        for (String skuId : this.getColorways()) {
            String colorName = topRow.getString(skuId + "." + COLORNAME);
            String matColorId = topRow.getString(skuId + "." + MATERIALCOLORID);

            if (FormatHelper.hasContent(matColorId) && (!"X".equals(colorName))) {
                addComplexColorData(bomPart, subRow, skuId, matColorId);
            } else if (!FormatHelper.hasContent(matColorId) && (!"X".equals(colorName))) {
                addUnavailableData(skuId, subRow);
            } else {
                blankColorData(skuId, subRow);
            }
        }
    }

    /**
     * Populates "X" color data for a row given a colorway dimension
     */
    private void blankColorData(String skuId, FlexObject row) throws WTException {
        debug("blankColorData skuId: " + skuId);
        row.put(skuId + "." + COLORNAME, "X");
        row.put(skuId + "." + COLORID, "");
        row.put(skuId + "." + COLORHEX, "");
        row.put(skuId + "." + MATERIALCOLORID, "");
        row.put(skuId + "_CLASS_OVERRIDE", "BOM_OVERRIDE");
    }

    /**
     * Populates "N/A" color data for a row given a colorway dimension
     */
    private void addUnavailableData(String skuId, FlexObject row) throws WTException {
        row.put(skuId + "." + COLORNAME, "N/A");
        row.put(skuId + "." + COLORID, "");
        row.put(skuId + "." + COLORHEX, "");
        row.put(skuId + "." + MATERIALCOLORID, "");
        row.put(skuId + "_CLASS_OVERRIDE", "BOM_OVERRIDE");
    }

    /**
     * Populates color data for a row given a colorway dimension
     */
    private void addColorData(FlexObject row, String skuId, MaterialColorInfo colorMci) throws WTException {
        String colorName = colorMci.colorName;
        String colorId = colorMci.colorId;
        String colorDescription = colorMci.colorDescription;
        String materialColorId = colorMci.materialColorId;

        // LCSSKU sku = LCSSKUQuery.getSKURevA(skuId);
        String coloridstring = row.getString(COLORIDS);

        if (!FormatHelper.hasContent(coloridstring)) {
            coloridstring = skuId;
        } else {
            coloridstring = coloridstring + MOAHelper.DELIM + skuId;
        }
        row.put(COLORIDS, coloridstring);
        row.put(skuId + "." + COLORNAME, colorName);
        row.put(skuId + "." + COLORDESC, colorDescription);
        row.put(skuId + "." + COLORID, colorId);
        row.put(skuId + "." + MATERIALCOLORID, materialColorId);
        // row.put(skuId + "." + COLORHEX, colorMci.colorHexValue);

        if (FormatHelper.hasContent(colorId)) {
            LCSColor color = (LCSColor) LCSQuery.findObjectById("OR:com.lcs.wc.color.LCSColor:" + colorId);
            if (color.getColorHexidecimalValue() != null) {
                row.put(skuId + "." + COLORHEX, color.getColorHexidecimalValue());
            }
            if (color.getThumbnail() != null) {
                row.put(skuId + "." + COLORTHUMB, color.getThumbnail());
            }
        }
    }

    /**
     * Populates color data for a complex material sub Component row given a materialColor dimension
     */
    private void addComplexColorData(FlexBOMPart bomPart, FlexObject row, String skuId, String matColorId)
            throws WTException {

        FlexType bomType = FlexTypeCache.getFlexTypeRoot("BOM");
        String matColorOid = "OR:com.lcs.wc.material.LCSMaterialColor:" + matColorId;
        String branchId = row.getString("FLEXBOMLINK.BRANCHID");
        LCSMaterialColor matColor = (LCSMaterialColor) LCSQuery.findObjectById(matColorOid);
        HashMap SubComponentsColorMap = (HashMap) (new ComplexMaterialColorQuery()).findSubComponentsColorMap(bomPart,
                matColor, bomType);

        if (SubComponentsColorMap.containsKey(branchId)) {
            FlexObject colorData = (FlexObject) SubComponentsColorMap.get(branchId);
            MaterialColorInfo subComponentColorMci = MCIDimensionHelper.getMaterialColorInfo(colorData);

            row.put(skuId + "." + COLORNAME, subComponentColorMci.colorName);
            row.put(skuId + "." + COLORDESC, subComponentColorMci.colorDescription);
            row.put(skuId + "." + COLORID, subComponentColorMci.colorId);
            row.put(skuId + "." + MATERIALCOLORID, subComponentColorMci.materialColorId);

            if (FormatHelper.hasContent(subComponentColorMci.colorId)) {
                LCSColor color = (LCSColor) LCSQuery
                        .findObjectById("OR:com.lcs.wc.color.LCSColor:" + subComponentColorMci.colorId);

                if (color.getColorHexidecimalValue() != null) {
                    row.put(skuId + "." + COLORHEX, color.getColorHexidecimalValue());
                }

                if (color.getThumbnail() != null) {
                    row.put(skuId + "." + COLORTHUMB, color.getThumbnail());
                }
            }
        } else {
            addUnavailableData(skuId, row);
        }
    }

    /**
     * Populates dimensions data for a row
     */
    private void addDimensionsData(FlexObject row, Map<String, Collection<String>> dimensionSubSet) throws WTException {

        if (dimensionSubSet.get(MCIDimensionHelper.SKU) != null) {
            Collection<String> skuList = (Collection<String>) dimensionSubSet.get(MCIDimensionHelper.SKU);
            if (!skuList.isEmpty()) {
                Collection<LCSSKU> skus = LCSSKUQuery.getSKURevA(skuList);

                String colorstring = "";
                for (LCSSKU sku : skus) {
                    String skuName = (String) sku.getValue("skuName");
                    colorstring = addString(colorstring, skuName);
                }
                row.put(COLORS, colorstring);
            }
        }

        if (dimensionSubSet.get(MCIDimensionHelper.SIZE1) != null) {
            Collection<String> size1List = dimensionSubSet.get(MCIDimensionHelper.SIZE1);
            String size1string = "";
            for (String size1 : size1List) {
                // FOR SPR 2076070 we do not need size definition to be sorted, so keep its previous order.
                size1string = MOAHelper.addValue(size1string, size1);// addString(size1string, size1);
            }
            row.put(SIZE1, size1string);

        }

        if (dimensionSubSet.get(MCIDimensionHelper.SIZE2) != null) {
            Collection<String> size2List = dimensionSubSet.get(MCIDimensionHelper.SIZE2);
            String size2string = "";
            for (String size2 : size2List) {
                // FOR SPR 2076070 we do not need size definition to be sorted, so keep its previous order.
                size2string = MOAHelper.addValue(size2string, size2);// addString(size2string, size2);//
            }
            row.put(SIZE2, size2string);
        }

        if (dimensionSubSet.get(MCIDimensionHelper.SOURCE) != null) {
            Collection<String> sourceList = dimensionSubSet.get(MCIDimensionHelper.SOURCE);
            String sourcestring = "";
            for (String sourceId : sourceList) {
                LCSSourcingConfigMaster source = (LCSSourcingConfigMaster) LCSQuery
                        .findObjectById("OR:com.lcs.wc.sourcing.LCSSourcingConfigMaster:" + sourceId);
                String sourceName = (String) source.getSourcingConfigName();
                sourcestring = addString(sourcestring, sourceName);
            }
            row.put(SOURCES, sourcestring);

        }

        if (dimensionSubSet.get(MCIDimensionHelper.DESTINATION) != null) {
            Collection<String> destinationList = dimensionSubSet.get(MCIDimensionHelper.DESTINATION);
            String destinationstring = "";
            for (String destinationId : destinationList) {
                destinationId = "OR:com.lcs.wc.product.ProductDestination:" + destinationId;
                ProductDestination destination = (ProductDestination) LCSQuery.findObjectById(destinationId);
                String destinationName = (String) destination.getDestinationName();
                destinationstring = addString(destinationstring, destinationName);
            }
            row.put(DESTINATION, destinationstring);
        }
    }
}
