/*
 * CWMatColorGenerator.java
 *
 * Created on October 31, 2006, 11:03 AM
 */

package com.sparc.wc.flexbom.gen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wt.part.WTPart;
import wt.util.WTException;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.client.web.FlexTypeGenerator;
import com.lcs.wc.client.web.TableColumn;
import com.lcs.wc.client.web.TableData;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flexbom.BOMColorTableColumn;
import com.lcs.wc.flexbom.BOMMaterialTableColumn;
import com.lcs.wc.flexbom.BOMPartNameTableColumn;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.flexbom.MaterialColorInfo;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.material.LCSMaterialQuery;
import com.lcs.wc.report.ColumnList;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSMessage;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.RB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.lcs.wc.flexbom.gen.*;
/**
 *
 * @author Chuck
 */
public class SPARCSizeGenerator extends SPARCBomDataGenerator {
    private static final Logger LOGGER = LogManager.getLogger(SPARCSizeGenerator.class.getName());

    public static final String MATERIAL_TYPE_PATH = "MATERIAL_TYPE_PATH";

    private static final String MATERIALREFID = "IDA3B5";

    private static final String SUPPLIERREFID = "IDA3C5";
//    private static final String COLORREFID = "IDA3D5";

//    private static final String COLORDIMCOLUMN = "IDA3E5";
//    private static final String SOURCEDIMCOLUMN = "IDA3F5";
//    private static final String DESTINATIONDIMCOLUMN = "IDA3H5";

    protected static String SIZE_DISPLAY_COL = "";

    private static final String DISPLAY_VAL = "DISPLAY_VAL";

    public String SIZES = "SIZES";

    public String sizeLabel = LCSMessage.getLocalizedMessage(RB.SOURCING, "sizesColumn_LBL");

    private Collection<String> sizes = new ArrayList<String>();

    private String source = "";

    private String materialNameDbColName;

    private String materialDescDbColName;

    private String componentNameDbColName;

    private String HIGH_LIGHT_ATT = "ATT1";

    private String supplierNameDbColName;

    private String colorNameDbColName;

    private String priceDbColName;

    private String componentNameDisplay;

    private String priceOverrideDbColName;

    private String quantityDbColName;

    private String lossAdjustmentDbColName;
    
	//changes to calculate rowTotal with new formula - start
    private String grossQuantityDbColName;
    private String cifDbColName;
	//changes to calculate rowTotal with new formula - end
    private String rowTotalDbColName;

    private String WCPARTNAME_DISPLAY = "ATT9";

    private String priceKey;

    private String overrideKey;

    private String quantityKey;

    private String lossAdjustmentKey;

    private String rowTotalKey;
    //changes to calculate rowTotal with new formula - start
    private String cifKey;   
    private String grossQuantityKey;
	//changes to calculate rowTotal with new formula - end
    /** Creates a new instance of CWMatColorGenerator */
    private FlexType materialType = null;

    private FlexType supplierType = null;

    private FlexType bomType = null;

    public boolean USE_DEFAULT_COLUMNS = LCSProperties
            .getBoolean("com.lcs.wc.flexbom.gen.SizeGenerator.useDefaultColumns");

    public float partNameWidth = (new Float(
            LCSProperties.get("com.lcs.wc.flexbom.gen.SizeGenerator.partNameWidth", "1.5"))).floatValue();

    public float materialNameWidth = (new Float(
            LCSProperties.get("com.lcs.wc.flexbom.gen.SizeGenerator.materialNameWidth", "1.25"))).floatValue();

    public float supplierNameWidth = (new Float(
            LCSProperties.get("com.lcs.wc.flexbom.gen.SizeGenerator.supplierNameWidth", "1.25"))).floatValue();

    public float sizeWidth = (new Float(LCSProperties.get("com.lcs.wc.flexbom.gen.SizeGenerator.sizeWidth", "0.75")))
            .floatValue();

    public int imageWidth = Integer
            .parseInt(LCSProperties.get("com.lcs.wc.flexbom.gen.SizeGenerator.matThumbWidth", "75"));

    public int imageHeight = Integer
            .parseInt(LCSProperties.get("com.lcs.wc.flexbom.gen.SizeGenerator.matThumbHeight", "0"));

    public SPARCSizeGenerator() {
    }

    public Collection getBOMData() throws WTException {
        return this.dataSet;
    }

    public Collection<String> getViewAttributes(ColumnList view) {
        ArrayList<String> viewAtts = new ArrayList<String>();

        if (view != null) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("view attributes: " + view.getAttributes());
            if (view.getAttributes() != null) {
                viewAtts.addAll(view.getAttributes());
            }
            viewAtts.removeAll(getViewAttributesToRemoved());

            String colName = "";
            boolean hasSizeColumn = false;
            for (int i = 0; i < viewAtts.size(); i++) {
                colName = (String) viewAtts.get(i);
                if ("size1Dim".equals(colName) || "size2Dim".equals(colName)) {
                    viewAtts.remove(i);
                    viewAtts.add(i, "SIZE");
                    hasSizeColumn = true;
                    break;
                }
            }

            if (!hasSizeColumn) {
                viewAtts.add("SIZE");

            }
        }
        if (!viewAtts.contains("ColorDescription")) {
            viewAtts.add("ColorDescription");
        }

        if (WCPART_ENABLED && view == null) {
            viewAtts.add(0, "BOM.wcPartName");
        }

        viewAtts.add(0, "supplierName");
        if (!viewAtts.contains("materialDescription")) {
        	viewAtts.add(0, "materialDescription");
        }
        if (this.sizes != null && this.sizes.size() > 0) {
            viewAtts.add(0, SIZES);
        }
		// - Harshad - Custom PartName Attribute here.
        viewAtts.add(0, "scComponent");
        if (this.useMatThumbnail) {
            viewAtts.add(0, "MATERIAL.thumbnail");
        }

        if (this.view == null) {
            if (USE_DEFAULT_COLUMNS) {
                viewAtts.add("Material.price");
                viewAtts.add("Material.unitOfMeasure");
            }

            viewAtts.add("SIZE");
        }
        return viewAtts;
    }

    private Collection<String> getViewAttributesToRemoved() {
        Collection<String> viewAttributesToRemoved = new ArrayList<String>();
		// - Harshad - Custom PartName Attribute here.
        viewAttributesToRemoved.add("BOM.scComponent");
		// - Harshad - Custom PartName Attribute here.
        viewAttributesToRemoved.add("BOM.partName");
        viewAttributesToRemoved.add("supplierName");
        viewAttributesToRemoved.add("BOM.materialDescription");
        viewAttributesToRemoved.add("BOM.materialDescription");
        if (!WCPART_ENABLED) {
            viewAttributesToRemoved.add("BOM.wcPartName");
        }
        return viewAttributesToRemoved;
    }

    public Map<String, TableColumn> getViewColumns(ColumnList view) throws WTException {
        Map<String, TableColumn> viewColumns = new HashMap<String, TableColumn>();

        if (view != null) {
            viewColumns.putAll(getViewColumns());
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("viewColumn keys: " + viewColumns.keySet());
        }

        if (this.view == null) {
            if (USE_DEFAULT_COLUMNS) {
                FlexTypeGenerator flexg = new FlexTypeGenerator();

                FlexTypeAttribute att = materialType.getAttribute("materialPrice");
                TableColumn column = flexg.createTableColumn(null, att, materialType, false, "LCSMATERIALSUPPLIER");
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
        ((BOMPartNameTableColumn) column).setSubComponetIndex("FLEXBOMLINK.MASTERBRANCHID");
        ((BOMPartNameTableColumn) column).setComplexMaterialIndex("FLEXBOMLINK.MASTERBRANCH");
        ((BOMPartNameTableColumn) column).setLinkedBOMIndex("FLEXBOMLINK.LINKEDBOM");

        column.setDisplayed(true);
        column.setSpecialClassIndex("CLASS_OVERRIDE");
        column.setPdfColumnWidthRatio(partNameWidth);
		// - Harshad - Custom PartName Attribute here.
        viewColumns.put("scComponent", column);

        if (WCPART_ENABLED) {
            column = new TableColumn();
            WCPARTNAME_DISPLAY = bomType.getAttribute("wcPartName").getAttDisplay();
            column.setHeaderLabel(WCPARTNAME_DISPLAY);
            column.setTableIndex(WCPARTNAME);
            column.setDisplayed(true);
            column.setFormat(FormatHelper.MOA_FORMAT);
            viewColumns.put("BOM.wcPartName", column);

        }
        
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
        column.setHeaderLabel(sizeLabel);
        column.setTableIndex(SIZES);
        column.setDisplayed(true);
        column.setFormat(FormatHelper.MOA_FORMAT);
        viewColumns.put(SIZES, column);

      

        column = new TableColumn();
        column.setHeaderLabel(this.supplierLabel);
        column.setTableIndex("LCSSUPPLIERMASTER.SUPPLIERNAME");
        column.setFormat(FormatHelper.STRING_FORMAT);
        column.setPdfColumnWidthRatio(supplierNameWidth);
        column.setDisplayed(true);
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

        BOMColorTableColumn colorColumn = new BOMColorTableColumn();
        colorColumn.setDisplayed(true);
        colorColumn.setTableIndex("LCSCOLOR.COLORNAME");
        colorColumn.setDescriptionIndex("FLEXBOMLINK." + colorNameDbColName);
        colorColumn.setHeaderLabel(LCSMessage.getLocalizedMessage(RB.COLOR, "color_LBL"));
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
        viewColumns.put("ColorDescription", colorColumn);

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Getting columns...sizes: " + sizes);
        if (this.sizes != null && this.sizes.size() > 0) {
            for (String size : this.sizes) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("creating column for size: " + size);
                column = new TableColumn();
                column.setHeaderLabel(size);
                column.setTableIndex(size + "." + DISPLAY_VAL);
                column.setDisplayed(true);
                column.setSpecialClassIndex(size + "_CLASS_OVERRIDE");
                column.setPdfColumnWidthRatio(sizeWidth);
                column.setAlign("center");
                viewColumns.put(size + "." + DISPLAY_VAL, column);
            }
        }

        return viewColumns;
    }

    public Collection<TableColumn> getTableColumns() throws WTException {
        Collection<String> viewAtts = getViewAttributes(this.view);
        Map<String, TableColumn> viewColumns = getViewColumns(this.view);

        Collection<TableColumn> columns = new ArrayList<TableColumn>();

        for (String att : viewAtts) {
            if ("SIZE".equals(att)) {
                if (this.sizes != null && this.sizes.size() > 0) {
                    for (String size : this.sizes) {
                        if (viewColumns.get(size + "." + DISPLAY_VAL) != null) {
                            columns.add(viewColumns.get(size + "." + DISPLAY_VAL));
                        }
                    }
                }
            } else {
                if (viewColumns.get(att) != null) {
                    viewColumns.get(att).setColumnClassIndex("FLEXBOMLINK." + HIGH_LIGHT_ATT);
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

    private void printOverrides() {
        Map<String, Collection<FlexObject>> orMap = getOverRideMap();
        for (String tlId : orMap.keySet()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("tlid: " + tlId);
            }
            for (FlexObject obj : orMap.get(tlId)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\torId: " + obj.getString(DIMID_COL));
                }
            }
        }
    }

    public void init(Map<String, Object> params) throws WTException {
        super.init(params);
//if(LOGGER.isDebugEnabled()) LOGGER.debug("----params" + params);        
        if (params != null) {
            if (params.get(MATERIAL_TYPE_PATH) != null) {
                materialType = FlexTypeCache.getFlexTypeFromPath((String) params.get(MATERIAL_TYPE_PATH));
            } else {
                materialType = FlexTypeCache.getFlexTypeRoot("Material");
            }
            supplierType = FlexTypeCache.getFlexTypeRoot("Supplier");
            bomType = FlexTypeCache.getFlexTypeFromPath("BOM\\Materials");

            materialNameDbColName = materialType.getAttribute("name").getColumnName();
            supplierNameDbColName = supplierType.getAttribute("name").getColumnName();
			// - Harshad - Custom PartName Attribute here.
            componentNameDbColName = bomType.getAttribute("scComponent").getColumnName();
            priceDbColName = materialType.getAttribute("materialPrice").getColumnName();
            materialDescDbColName = bomType.getAttribute("materialDescription").getColumnName();
            colorNameDbColName = bomType.getAttribute("colorDescription").getColumnName();
			// - Harshad - Custom PartName Attribute here.
            componentNameDisplay = bomType.getAttribute("scComponent").getAttDisplay();

            priceOverrideDbColName = bomType.getAttribute("priceOverride").getColumnName();
            quantityDbColName = bomType.getAttribute("quantity").getColumnName();
            lossAdjustmentDbColName = bomType.getAttribute("lossAdjustment").getColumnName();
            rowTotalDbColName = bomType.getAttribute("rowTotal").getColumnName();
            
            
            
            
            priceKey = "LCSMATERIALSUPPLIER." + priceDbColName;
            overrideKey = "FLEXBOMLINK." + priceOverrideDbColName;
            quantityKey = "FLEXBOMLINK." + quantityDbColName;
            lossAdjustmentKey = "FLEXBOMLINK." + lossAdjustmentDbColName;
            rowTotalKey = "FLEXBOMLINK." + rowTotalDbColName;
            HIGH_LIGHT_ATT = bomType.getAttribute("highLight").getColumnName();
			
			//changes to calculate rowTotal with new formula - start
            cifDbColName = bomType.getAttribute("scCIF").getColumnName();
            grossQuantityDbColName = bomType.getAttribute("scGrossQuantity").getColumnName();
            cifKey = "FLEXBOMLINK." + cifDbColName;
            grossQuantityKey = "FLEXBOMLINK." + grossQuantityDbColName;
            //changes to calculate rowTotal with new formula - end
			
            if (this.getSources() != null && this.getSources().size() > 0) {
                this.source = (String) this.getSources().toArray()[0];
            }

            this.sizes = new ArrayList<String>();
            if ("SIZE1".equalsIgnoreCase(this.sizeAtt)) {
                sizes = this.getSizes1();
            } else if ("SIZE2".equalsIgnoreCase(this.sizeAtt)) {
                sizes = this.getSizes2();
            }

            if (this.dataSet != null) {
                // printOverrides();
                this.dataSet = filterDataSet(this.dataSet, this.sizeAtt);
                this.dataSet = groupDataToBranchId(this.dataSet, "FLEXBOMLINK.BRANCHID", "FLEXBOMLINK.MASTERBRANCHID",
                        "FLEXBOMLINK.SORTINGNUMBER");

                printOverrides();
                LOGGER.debug("\n\n");

                Map<String, Map<String, Collection<MaterialColorInfo>>> materialSizeMap = getMaterialSizeMapping();
                SIZE_DISPLAY_COL = this.displayAttCol;

                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("materialSizeMap:\n " + materialSizeMap);

                LOGGER.debug("\n\n");

                Collection<TableData> processedData = new ArrayList<TableData>();

                Collection<FlexObject> tls = getTLBranches();
                for (FlexObject tlb : tls) {
                    // LOGGER.debug("\n\n");
                    // if(LOGGER.isDebugEnabled()) LOGGER.debug("tlb: " + tlb);
                    // if(LOGGER.isDebugEnabled()) LOGGER.debug("materialSizeMap: " + materialSizeMap);
                    processedData.addAll(processBranch(tlb, materialSizeMap));
                }

                this.dataSet = processedData;

            }

        }
    }

    private void calculatePrice(FlexObject row) throws WTException {

        double materialPrice = FormatHelper.parseDouble(row.getData(priceKey));
        double priceOverride = FormatHelper.parseDouble(row.getData(overrideKey));
        double quantity = FormatHelper.parseDouble(row.getData(quantityKey));
        double lossAdjustment = FormatHelper.parseDouble(row.getData(lossAdjustmentKey));
        
		//changes and updated to calculate rowTotal with new formula - start
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

    private Collection<FlexObject> processBranch(FlexObject topLevel,
            Map<String, Map<String, Collection<MaterialColorInfo>>> materialSizeMap) throws WTException {
        ArrayList<FlexObject> data = new ArrayList<FlexObject>();

        FlexObject tlDataRow = topLevel.dup();

        calculatePrice(tlDataRow);

        String tlId = topLevel.getString(DIMID_COL);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("\ntopLevelId: " + tlId);

        String tlMatId = topLevel.getString("FLEXBOMLINK." + MATERIALREFID);
        String tlMatSupId = topLevel.getString("LCSMATERIALSUPPLIERMASTER.IDA2A2");

        if (!FormatHelper.hasContent(tlMatId)
                || tlMatId.equals(FormatHelper.getNumericFromOid(LCSMaterialQuery.PLACEHOLDERID))) {
            tlMatId = "";
        }

        Map<String, Collection<MaterialColorInfo>> sizeMap = materialSizeMap.get(tlId);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("MCIs: " + sizeMap);
        boolean tlMatch = false;

        // IF COLOR APPLIES TO ROW THEN ADD TO COLORS COLUMN, AND ADD DATA TO THE APPROPRIATE
        // SKU COLUMN....IF IT DOES NOT APPLY TO THE ROW, THEN MARK THE SKU COLUMN AS NOT APPLICABLE
        String allLabel = LCSMessage.getLocalizedMessage(RB.MAIN, "all_LBL");
        for (String size : sizeMap.keySet()) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Processing size: " + size);
            Collection<MaterialColorInfo> sizeMaterials = sizeMap.get(size);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("\t # of material colors for size: " + sizeMaterials.size());
            if (sizeMaterials != null && sizeMaterials.size() > 0) {
                LOGGER.debug("\tprocessing found materials");
                for (MaterialColorInfo mci : sizeMaterials) {
                    if (mci != null) {
                        if (LOGGER.isDebugEnabled())
                            LOGGER.debug("\t\tMCI not null...tlMatSupId: " + tlMatSupId
                                    + "      mci.materialSupplierId:" + mci.materialSupplierId);
                        if (tlMatSupId.equals(mci.materialSupplierId)) {
                            LOGGER.debug("\t\tAre equal");
                            // matches top level...add to top level info
                            tlMatch = true;
                            FlexObject or = getOverrideRow(tlId, this.sizeAtt, size, mci.materialSupplierId);
                            if (or != null && FormatHelper.hasContentAllowZero(or.getString(SIZE_DISPLAY_COL))) {
                                addSizeData(tlDataRow, size, or.getString(SIZE_DISPLAY_COL));
                            } else {
                                // No override for size/material...look for override independant of material..
                                or = getOverrideRow(tlId, this.sizeAtt, size, null);
                                if (or != null && FormatHelper.hasContentAllowZero(or.getString(SIZE_DISPLAY_COL))) {
                                    addSizeData(tlDataRow, size, or.getString(SIZE_DISPLAY_COL));
                                } else {
                                    addSizeData(tlDataRow, size, tlDataRow.getString(SIZE_DISPLAY_COL));
                                }
                            }
                        } else {
                            LOGGER.debug("\t\tAre NOT equal");

                            // NEED TO LOOP OVER ALL EXISTING MATERIAL ROWS...
                            if (data.size() > 0) {
                                // There is already existing data...check to see if this material matches existing
                                boolean foundMat = false;
                                for (FlexObject tRow : data) {
                                    String tRowMat = tRow.getString("FLEXBOMLINK." + MATERIALREFID);
                                    if (tRowMat.equals(mci.materialId)) {
                                        // If this is a placeholder material match, need to see if the
                                        // materialDescription matches as well
                                        if (tRowMat
                                                .equals(FormatHelper.getNumericFromOid(LCSMaterialQuery.PLACEHOLDERID))
                                                && FormatHelper.hasContent(
                                                        tRow.getString("FLEXBOMLINK." + materialDescDbColName))
                                                && !(tRow.getString("FLEXBOMLINK." + materialDescDbColName)
                                                        .equals(mci.materialName))) {

                                            // This is a placeholder record that has a material description set, but
                                            // doesn't match the description of the mci
                                            continue;
                                        }
                                        // Found an existing row that matches the material...update for the color
                                        LOGGER.debug("\t\tfound matching material...adding size to it");
                                        foundMat = true;
                                        FlexObject or = getOverrideRow(tlId, this.sizeAtt, size,
                                                mci.materialSupplierId);
                                        if (or != null
                                                && FormatHelper.hasContentAllowZero(or.getString(SIZE_DISPLAY_COL))) {
                                            if (LOGGER.isDebugEnabled())
                                                LOGGER.debug("\t\tFound or:" + or);
                                            addSizeData(tRow, size, or.getString(SIZE_DISPLAY_COL));
                                        } else {
                                            // No override for size...use Top Level value..
                                            LOGGER.debug("\t\tDid not find override record...");
                                            addSizeData(tRow, size, tlDataRow.getString(SIZE_DISPLAY_COL));
                                        }
                                    }
                                }
                                // Didn't find a row for the material already...create a new one
                                if (!foundMat) {
                                    LOGGER.debug(
                                            "\t\tDid not find matching material/size...getting override row as starting point.");

                                    FlexObject newRow = topLevel.dup();
                                    FlexObject orRow = getOverrideRow(tlId, this.sizeAtt, size, mci.materialSupplierId);

                                    if (LOGGER.isDebugEnabled())
                                        LOGGER.debug("\t\tFound or for new row:" + orRow);

                                    addMaterialData(newRow, mci, orRow);

                                    if (FormatHelper.hasContentAllowZero(orRow.getString(SIZE_DISPLAY_COL))) {
                                        addSizeData(newRow, size, orRow.getString(SIZE_DISPLAY_COL));
                                    } else {
                                        // No override for size...use Top Level value..
                                        addSizeData(newRow, size, tlDataRow.getString(SIZE_DISPLAY_COL));
                                    }
                                    calculatePrice(newRow);
                                    data.add(newRow);
                                }
                            } else {
                                // There isn't any data yet...need new row
                                LOGGER.debug("\t\tThere is no data...using new override row.");
                                if (LOGGER.isDebugEnabled())
                                    LOGGER.debug("MaterialName: " + mci.materialName);
                                if (LOGGER.isDebugEnabled())
                                    LOGGER.debug("MaterialName: " + mci.supplierName);
                                FlexObject newRow = topLevel.dup();
                                FlexObject orRow = getOverrideRow(tlId, this.sizeAtt, size, mci.materialSupplierId);

                                if (LOGGER.isDebugEnabled())
                                    LOGGER.debug("\t\tFound or for new row:" + orRow);

                                addMaterialData(newRow, mci, orRow);
                                if (FormatHelper.hasContentAllowZero(orRow.getString(SIZE_DISPLAY_COL))) {
                                    addSizeData(newRow, size, orRow.getString(SIZE_DISPLAY_COL));
                                } else {
                                    // No override for size...use Top Level value..
                                    addSizeData(newRow, size, tlDataRow.getString(SIZE_DISPLAY_COL));
                                }
                                calculatePrice(newRow);
                                data.add(newRow);
                            }
                        }
                    } else {
                        // no material assigned...top level must have no material assigned as well...add to top level
                        LOGGER.debug("\tNo mci found");
                        tlMatch = true;
                        FlexObject or = getOverrideRow(tlId, this.sizeAtt, size, null);

                        if (or == null) {
                            LOGGER.debug("\n\nDIDN'T FIND OVERRIDE RECORD");
                        } else {
                            if (LOGGER.isDebugEnabled())
                                LOGGER.debug("\tfound or record...size - " + SIZE_DISPLAY_COL + ": "
                                        + or.getString(SIZE_DISPLAY_COL));
                        }

                        if (or != null && FormatHelper.hasContentAllowZero(or.getString(SIZE_DISPLAY_COL))) {
                            addSizeData(tlDataRow, size, or.getString(SIZE_DISPLAY_COL));
                        } else {
                            addSizeData(tlDataRow, size, tlDataRow.getString(SIZE_DISPLAY_COL));
                        }
                    }
                }
            } else {
                LOGGER.debug("\tdidn't find colors...adding to top level");

                // no material assigned...top level must have no material assigned as well...add to top level
                LOGGER.debug("\touter No mci found");
                tlMatch = true;
                FlexObject or = getOverrideRow(tlId, this.sizeAtt, size, null);

                if (or == null) {
                    LOGGER.debug("\n\nDIDN'T FIND OVERRIDE RECORD");
                } else {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("\tfound or record...size: " + or.getString(SIZE_DISPLAY_COL));
                }
                if (or != null && FormatHelper.hasContentAllowZero(or.getString(SIZE_DISPLAY_COL))) {
                    addSizeData(tlDataRow, size, or.getString(SIZE_DISPLAY_COL));
                } else {
                    addSizeData(tlDataRow, size, tlDataRow.getString(SIZE_DISPLAY_COL));
                }
            }

        }
        if (tlMatch) {
            // calculatePrice(tlDataRow);
            data.add(0, tlDataRow);
        }

        if (data.size() > 0) {

            if (data.size() == 1 && this.sizes != null && this.sizes.size() > 0) {
                FlexObject row = data.get(0);
                row.put(SIZES, allLabel);
            } else {
                // need to format first column...
                // For now putting dashes...want blank but with different color formatting
                for (int i = 1; i < data.size(); i++) {
                    FlexObject row = data.get(i);
                    row.put("FLEXBOMLINK." + componentNameDbColName, " ");
                    row.put("CLASS_OVERRIDE", "BOM_OVERRIDE");
                }
            }

            // Format Color Columns that don't apply for the rows
            for (String sid : this.sizes) {
                for (FlexObject row : data) {
                    if (!FormatHelper.hasContent(row.getString(sid + "." + DISPLAY_VAL))) {
                        blankSizeData(row, sid);
                    }
                }
            }
        }

        return data;
    }

    public void addMaterialData(FlexObject row, MaterialColorInfo mci, FlexObject orRow) throws WTException {
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
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("\n\nThe potential attributes need to copy: TABLEINDEX: " + column.getTableIndex()
                                + " HEADLABEL: " + column.getHeaderLabel());
                }
            }

            for (String key : orRow.keySet()) {
                if (key.startsWith("LCSMATERIAL") || key.startsWith("LCSSUPPLIER")) {
                    row.put(key, orRow.get(key));
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("\n\nAdding LCSMaterial, LCSSupplier relevant attribute, KEY: " + key + " VALUE: "
                                + orRow.get(key));
                } else if (needColumns.contains(key) && !FormatHelper.hasContent(row.getString(key))) {
                    row.put(key, orRow.get(key));
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("\n\nAdding view attributes, KEY: " + key + " VALUE: " + orRow.get(key));
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

    public void addSizeData(FlexObject row, String size, String value) throws WTException {
        String sizeString = row.getString(SIZES);
        // FOR SPR 2076070 we do not need size definition to be sorted, so keep its previous order.
        sizeString = MOAHelper.addValue(sizeString, size);// addString(sizeString, size);
        row.put(SIZES, sizeString);

        value = getDisplayVal(value);
        row.put(size + "." + DISPLAY_VAL, value);
    }

    public void blankSizeData(FlexObject row, String size) throws WTException {
        row.put(size + "." + DISPLAY_VAL, "X");
        row.put(size + "_CLASS_OVERRIDE", "BOM_OVERRIDE");
    }

    /*
     * private Map geDatatRow(Collection data, String key, String value){ if(data != null &&
     * FormatHelper.hasContent(key) && FormatHelper.hasContent(value)){ Iterator i = data.iterator(); Map dm = null;
     * while(i.hasNext()){ dm = (Map)i.next(); if(dm.get(key).equals(value)){ return dm; } } } return null; }
     */

    private Map<String, Map<String, Collection<MaterialColorInfo>>> getMaterialSizeMapping() throws WTException {
        Map<String, Map<String, Collection<MaterialColorInfo>>> smMap = new HashMap<String, Map<String, Collection<MaterialColorInfo>>>();

        Collection<FlexObject> tls = this.getTLBranches();
        Map<String, Collection<FlexObject>> ovrRdMap = this.getOverRideMap();

        // Loop over all top level branches
        for (FlexObject topLevel : tls) {
            Collection<MaterialColorInfo> retMat = null;

            // Prepare the collectio nfor all overrides
            Collection<FlexObject> fullBranch = new ArrayList<FlexObject>();

            // add top level to collection of branches to process
            fullBranch.add(topLevel);

            // get the top level branch id
            String branchId = topLevel.getString(DIMID_COL);

            // Get the branch overrides for the given top level branch
            Collection<FlexObject> branchOrs = ovrRdMap.get(branchId);
            if (branchOrs != null && branchOrs.size() > 0) {
                // if any overrides are found include them in the collection to process
                fullBranch.addAll(branchOrs);
            }
            Map<String, Collection<MaterialColorInfo>> sizeMaterialMap = new HashMap<String, Collection<MaterialColorInfo>>();

            Map<String, MaterialColorInfo> materialColorMap = getMaterialColorsForBranch(fullBranch, this.sizeAtt);

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("materialColorMap: " + materialColorMap);

            // If there are colorways to consider
            if (this.sizes != null && this.sizes.size() > 0) {
                // Loop over the list of colorways for the BOM
                for (String size : this.sizes) {
                    // get each color id and add process it individually...this requires putting each element
                    // into a collection, as the following call requires collections
                    /*
                     * Collection sizeIdCol = new ArrayList(); sizeIdCol.add(size);
                     * 
                     * //Get the material(s) used for the given color way based on the top level and source and
                     * available overrides if("SIZE1".equalsIgnoreCase(this.sizeAtt)){ retMat =
                     * bomQuery.getUniqueMaterialColorCombinationsForBranch(fullBranch, null, this.sources, sizeIdCol,
                     * null); } else if("SIZE2".equalsIgnoreCase(this.sizeAtt)){ retMat =
                     * bomQuery.getUniqueMaterialColorCombinationsForBranch(fullBranch, null, this.sources, null,
                     * sizeIdCol); }
                     **/
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("size: " + size);

                    retMat = getMaterialColorForSize(materialColorMap, branchId, this.sizeAtt, size);
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("retMat: " + retMat);

                    // add the returned material to a map keyed by color id
                    sizeMaterialMap.put(size, retMat);
                }
            }
            // If there are no colorways to consider
            else {
                // material for toplevel? is this required? needed for case where no colorways are passed in?
                // Get the material(s) used for the given top level based on the and source if one was passed
                // retMat = bomQuery.getUniqueMaterialColorCombinationsForBranch(fullBranch, null, this.sources, null,
                // null);
                // retMat = bomQuery.getUniqueMaterialColorCombinationForBOM(fullBranch, null, this.sources, null,
                // null).values();
                retMat = getMaterialColorForSize(materialColorMap, branchId, this.sizeAtt, "-");

                // add the returned material to a map keyed by color id...since no color is used, empty string is the
                // key
                sizeMaterialMap.put("", retMat);
            }

            // Add the list of color/materials to a map keyed by top level branch id
            smMap.put(branchId, sizeMaterialMap);
        }

        return smMap;
    }

    protected FlexObject getOverrideRow(String tlId, String sAtt, String sVal, String matSupId) {
        FlexObject fobj = null;
        sAtt = sAtt.toUpperCase();
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("looking for override: tlid: " + tlId + "   satt:sVal: " + sAtt + ":" + sVal + "    matSupId: "
                    + matSupId);

        if (this.getSources() != null && this.getSources().size() > 0) {
            // LOOK FOR SOURCE:SKU
            fobj = getOverrideRowForDim(tlId, sAtt + ":" + sVal, matSupId, ":SOURCE:" + sAtt);
            if (fobj != null)
                return fobj;
        }

        // LOOK FOR SKU
        fobj = getOverrideRowForDim(tlId, sAtt + ":" + sVal, matSupId, ":" + sAtt);
        if (fobj != null)
            return fobj;

        if (this.getSources() != null && this.getSources().size() > 0) {
            // LOOK FOR SOURCE
            // fobj = getOverrideRowForDim(tlId, this.source, matSupId, ":SOURCE:SKU");
            // if(fobj != null) return fobj;

            // LOOK FOR SOURCE
            fobj = getOverrideRowForDim(tlId, this.source, matSupId, ":SOURCE");
            if (fobj != null)
                return fobj;
        }
        return null;
    }

    protected Collection<TableData> filterDataSet(Collection<TableData> data, String sAtt) {
        Collection<TableData> filteredData = new ArrayList<TableData>();

        boolean useSource = this.getSources() != null && this.getSources().size() > 0;
        sAtt = sAtt.toUpperCase();

        for (TableData td : data) {
            FlexObject fobj = (FlexObject) td;
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("sAtt: " + sAtt + "     dimCol: " + fobj.getString(DIM_COL));
            if (!FormatHelper.hasContent(fobj.getString(DIM_COL))
                    || ((useSource && ((":SOURCE:" + sAtt).equals(fobj.getString(DIM_COL))
                            || ":SOURCE".equals(fobj.getString(DIM_COL))))
                            || (":" + sAtt).equals(fobj.getString(DIM_COL)))) {
                filteredData.add(fobj);
            }
        }

        return filteredData;
    }

    protected Collection<MaterialColorInfo> getMaterialColorForSize(Map<String, MaterialColorInfo> mciMap, String tlId,
            String sizeAtt, String sizeVal) {
        sizeAtt = sizeAtt.toUpperCase();
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("tlId: " + tlId + "   sizeAtt:" + sizeAtt + "    sizeVal:" + sizeVal + "     source: "
                    + this.source);
        Collection<MaterialColorInfo> matCol = new ArrayList<MaterialColorInfo>();
        MaterialColorInfo mci = null;

        MaterialColorInfo topLevel = null;
        if (mciMap.keySet().contains(tlId)) {
            topLevel = (MaterialColorInfo) mciMap.get(mciMap);
        }
//SIZE1:33
        String dimKey = "";

        // Find the lowest level dimension override (if source:size and
        // using source, then take source:size rather than source)

        // Looking for Source:Size overrides if using source
        for (String tKey : mciMap.keySet()) {
            if (FormatHelper.hasContent(this.source)) {
                if (tKey.indexOf(this.source) > -1 && tKey.endsWith(sizeAtt + ":" + sizeVal)) {
                    LOGGER.debug("in source size");
                    dimKey = tKey;
                    break;
                }
            }

        }
        // If didn't find source level, look for sizeatt + size val
        if (!FormatHelper.hasContent(dimKey)) {
            for (String tKey : mciMap.keySet()) {
                if (tKey.endsWith(sizeAtt + ":" + sizeVal)) {
                    LOGGER.debug("in size");
                    dimKey = tKey;
                    break;
                }

            }
        }
        // If didn't find source:size or size and using source, get source override
        if (!FormatHelper.hasContent(dimKey)) {
            for (String tKey : mciMap.keySet()) {
                if (FormatHelper.hasContent(this.source)) {
                    if (tKey.indexOf(this.source) > -1) {
                        LOGGER.debug("in source");
                        dimKey = tKey;
                        break;
                    }
                }

            }
        }
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("dimKey: " + dimKey);

        if (FormatHelper.hasContent(dimKey)) {
            mci = (MaterialColorInfo) mciMap.get(dimKey);
        }
        if (mci == null && topLevel != null) {
            mci = topLevel;
        }
        if (mci != null) {
            matCol.add(mci);
        }

        return matCol;
    }

    protected Map<String, MaterialColorInfo> getMaterialColorsForBranch(Collection<FlexObject> rows, String sizeAtt)
            throws WTException {
        LOGGER.debug("\n\ngetMaterialColorsForBranch");
        sizeAtt = sizeAtt.toUpperCase();

        Map<String, MaterialColorInfo> materialColors = new HashMap<String, MaterialColorInfo>();
        MaterialColorInfo mci = null;

        LCSFlexBOMQuery bq = new LCSFlexBOMQuery();

        for (FlexObject row : rows) {
            String materialId = row.getString("FLEXBOMLINK." + MATERIALREFID);
            String dimensionId = row.getString(DIMID_COL);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("dimensionId: " + dimensionId + "     materialId: " + materialId);
                LOGGER.debug("\tFormatHelper.hasContent(materialId): " + FormatHelper.hasContent(materialId));
                LOGGER.debug("\tmaterialDescDbColName: " + materialDescDbColName);
                LOGGER.debug("\trow.getString(\"FLEXBOMLINK.\" + materialDescDbColName): "
                        + row.getString("FLEXBOMLINK." + materialDescDbColName));
                LOGGER.debug("\tFormatHelper.getNumericFromOid(LCSMaterialQuery.PLACEHOLDERID).equals(materialId): "
                        + FormatHelper.getNumericFromOid(LCSMaterialQuery.PLACEHOLDERID).equals(materialId));
                LOGGER.debug("\tmaterialColors.get(dimensionId) == null: " + (materialColors.get(dimensionId) == null));
            }

            // row has material set && the material is not the placeholder && I haven't found the dimension already
            if (FormatHelper.hasContent(materialId)
                    && (!FormatHelper.getNumericFromOid(LCSMaterialQuery.PLACEHOLDERID).equals(materialId)
                            || (FormatHelper.getNumericFromOid(LCSMaterialQuery.PLACEHOLDERID).equals(materialId)
                                    && FormatHelper.hasContent(row.getString("FLEXBOMLINK." + materialDescDbColName))))
                    && materialColors.get(dimensionId) == null) {
                String dimName = row.getString("FLEXBOMLINK.DIMENSIONNAME");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\t\tdimName: " + dimName);
                    LOGGER.debug("\t\tsizeAtt: " + sizeAtt);
                    LOGGER.debug("\t\tthis.sources: " + this.getSources());
                }
                if (!FormatHelper.hasContent(dimName) || dimName.indexOf(":" + sizeAtt) > -1
                        || (this.getSources() != null && this.getSources().size() > 0 && dimName.equals(":SOURCE"))) {
                    mci = bq.bomInfoToMaterialColorInfo(row);
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("\t\tnew MCI: " + mci);
                    materialColors.put(dimensionId, mci);
                }

            }
        }
        LOGGER.debug("end getMaterialColorsForBranch\n\n");
        return materialColors;
    }

    /**
     * debug method is no longer supported, please use log4j logger of the class.
     */
    @Deprecated
    private void debug(String msg) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(msg);
        }
    }
}
