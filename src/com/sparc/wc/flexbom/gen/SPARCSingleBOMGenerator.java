/*
 * SingleBOMGenerator.java
 *
 * Created on November 17, 2006, 2:41 PM
 */

package com.sparc.wc.flexbom.gen;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wt.util.WTException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.client.web.FlexTypeGenerator;
import com.lcs.wc.client.web.LifecycleStateTableColumn;
import com.lcs.wc.client.web.TableColumn;
import com.lcs.wc.client.web.TableData;
import com.lcs.wc.client.web.TableDataUtil;
import com.lcs.wc.flexbom.BOMColorTableColumn;
import com.lcs.wc.flexbom.BOMMaterialTableColumn;
import com.lcs.wc.flexbom.BOMPartNameTableColumn;
import com.lcs.wc.flexbom.FlexBOMFlexTypeScopeDefinition;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.LCSFindFlexBOMDelegate;
import com.lcs.wc.flexbom.LCSFindFlexBOMHelper;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.material.MaterialPriceList;
import com.lcs.wc.material.MaterialSupplierFlexTypeScopeDefinition;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.report.ColumnList;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSMessage;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.RB;
import com.lcs.wc.util.SortHelper;

import com.lcs.wc.flexbom.gen.*;
/**
 *
 * @author Chuck
 */
public class SPARCSingleBOMGenerator extends SPARCBomDataGenerator {
    private static final Logger LOGGER = LogManager.getLogger(SPARCSingleBOMGenerator.class.getName());

    public int imageWidth = (new Integer(
            LCSProperties.get("com.lcs.wc.flexbom.gen.SingleBOMGenerator.matThumbWidth", "75"))).intValue();

    public int imageHeight = (new Integer(
            LCSProperties.get("com.lcs.wc.flexbom.gen.SingleBOMGenerator.matThumbHeight", "0"))).intValue();

    public static String MATERIAL_SUPPLIER_ROOT_TYPE = LCSProperties.get("com.lcs.wc.supplier.MaterialSupplierRootType",
            "Supplier");

    public static String MATERIAL_COLOR_ROOT_TYPE = LCSProperties.get("com.lcs.wc.supplier.MaterialColorRootType",
            "Material Color");

    public static final boolean USE_MULTILEVEL = LCSProperties.getBoolean("jsp.flexbom.ViewBOM.useMultilevel");

    public static final String MATERIAL_TYPE_PATH = "MATERIAL_TYPE_PATH";

    /** Creates a new instance of SingleBOMGenerator */
    public SPARCSingleBOMGenerator() {
    }

    String operationLabel = LCSMessage.getLocalizedMessage(RB.FLEXBOM, "operation_LBL");

    String materialLabel = LCSMessage.getLocalizedMessage(RB.MATERIAL, "material_LBL");

    String colorLabel = LCSMessage.getLocalizedMessage(RB.COLOR, "color_LBL");

    String supplierLabel = LCSMessage.getLocalizedMessage(RB.FLEXBOM, "supplier_LBL");

    String materialStatusLabel = LCSMessage.getLocalizedMessage(RB.FLEXBOM, "materialStatus_LBL");

    static String BOM_PART_ID = "BOM_PART_ID";

    FlexBOMPart part = null;

    FlexType materialType = null;

    static String SKU_ID = "SKU_ID";

    String skuMasterId = "";

    static String SOURCE_ID = "SOURCE_ID";

    String sourceDimId = "";

    static String SIZE1_VAL = "SIZE1_VAL";

    String size1 = "";

    static String SIZE2_VAL = "SIZE2_VAL";

    String size2 = "";

    static String DESTINATION_ID = "DESTINATION_ID";

    String destinationId = "";

    static String SKU_MODE = "SKU_MODE";

    String skuMode = "SINGLE";

    String TIME_STAMP = "";

    String timestamp = "";

    boolean multiLevel = USE_MULTILEVEL;

    boolean useBOMCosting = false;

    public void init(Map<String, Object> params) throws WTException {
        super.init(params);
        if (params != null) {

            part = (FlexBOMPart) params.get(SPARCBOMPDFContentGenerator.BOM_PART);

            if ((part == null) && FormatHelper.hasContent((String) params.get(BOM_PART_ID))) {
                part = (FlexBOMPart) LCSQuery.findObjectById((String) params.get(BOM_PART_ID));
            }

            if (params.get(MATERIAL_TYPE_PATH) != null) {
                materialType = FlexTypeCache.getFlexTypeFromPath((String) params.get(MATERIAL_TYPE_PATH));
            } else {
                materialType = part.getFlexType()
                        .getReferencedFlexType(com.lcs.wc.product.ReferencedTypeKeys.MATERIAL_TYPE);
            }

            if (FormatHelper.hasContent((String) params.get(SKU_ID))) {
                String skuId = (String) params.get(SKU_ID);
                if (skuId.indexOf("LCSSKU:") > -1) {
                    LCSSKU sku = (LCSSKU) LCSQuery.findObjectById(skuId);
                    skuMasterId = FormatHelper.getObjectId((LCSPartMaster) sku.getMaster());
                } else if (skuId.indexOf("LCSPartMaster") > -1) {
                    skuMasterId = skuId;
                } else {
                    skuMasterId = "OR:com.lcs.wc.part.LCSPartMaster:" + skuId;
                }

            }

            if (FormatHelper.hasContent((String) params.get(SOURCE_ID))) {
                String sourceId = (String) params.get(SOURCE_ID);

                if (sourceId.indexOf("LCSSourcingConfigMaster") > -1) {
                    sourceDimId = sourceId;
                } else if (sourceId.indexOf("LCSSourcingConfig") > -1) {
                    LCSSourcingConfig config = (LCSSourcingConfig) LCSQuery.findObjectById(sourceId);
                    sourceDimId = FormatHelper.getObjectId((LCSSourcingConfigMaster) config.getMaster());
                } else {
                    sourceDimId = "OR:com.lcs.wc.sourcing.LCSSourcingConfigMaster:" + sourceId;
                }
            }

            if (FormatHelper.hasContentAllowZero((String) params.get(SIZE1_VAL))) {
                size1 = (String) params.get(SIZE1_VAL);
            }

            if (FormatHelper.hasContentAllowZero((String) params.get(SIZE2_VAL))) {
                size2 = (String) params.get(SIZE2_VAL);
            }

            if (FormatHelper.hasContent((String) params.get(DESTINATION_ID))) {
                String destId = (String) params.get(DESTINATION_ID);

                if (destId.indexOf("Destination") > -1) {
                    destinationId = destId;
                } else {
                    destinationId = "OR:com.lcs.wc.product.ProductDestination:" + destId;
                }
            }

            if (FormatHelper.hasContent((String) params.get(SKU_MODE))) {
                skuMode = (String) params.get(SKU_MODE);
            }

            if (FormatHelper.hasContent((String) params.get(TIME_STAMP))) {
                timestamp = (String) params.get(TIME_STAMP);
            }

        }
    }

    /**
     * private void printParams(){ LOGGER.debug("part: " + part); LOGGER.debug("skuMasterId: " + skuMasterId);
     * LOGGER.debug("sourceDimId: " + sourceDimId); LOGGER.debug("size1: " + size1); LOGGER.debug("size2: " + size2);
     * LOGGER.debug("destinationId: " + destinationId); LOGGER.debug("skuMode: " + skuMode); LOGGER.debug("timestamp: "
     * + timestamp); LOGGER.debug("multiLevel: " + multiLevel); LOGGER.debug("materialType: " + materialType); }
     **/
    public Collection getBOMData() throws WTException {
        // return this.dataSet;
        try {
            Collection<String> keys = getColumnList(view);
            Collection usedAttKeys = getUsedAttList(keys);
            Collection<TableData> bomData = LCSFindFlexBOMHelper.findBOM(part, skuMasterId, sourceDimId, size1, size2,
                    destinationId, skuMode, timestamp, new Boolean(multiLevel), materialType, usedAttKeys,
                    this.section);
            // changed order of the next two lines to minimize the number of pricing lookups to the BOM section of
            // interest
            // Below post processing is no more required since findBOM API will be taking care of it
            // bomData = postProcess(bomData, this.section);
            mergeMaterialPricing(bomData);

            bomData = SortHelper.sortFlexObjectsByNumber(bomData, "sortingNumber");
            bomData = groupDataToBranchId(bomData, "branchId", "masterBranchId", null);
            return bomData;

        } catch (Exception e) {
            throw new WTException(e);
        }
    }

    public static Collection<TableData> groupDataToBranchId(Collection<TableData> data, String branchKey,
            String masterKey, String sortingKey) {
        if (data == null || data.size() < 1) {
            return new ArrayList<TableData>();
        }

        Map<String, List<TableData>> table = TableDataUtil.groupIntoCollections(data, masterKey);

        Collection<TableData> current;
        List<TableData> groupedVector = new ArrayList<TableData>();
        current = table.get("0");
        if (current == null) {
            return new ArrayList<TableData>();
        }
        table.remove("0");

        Set<String> keys = table.keySet();

        String value = "";
        for (TableData td : current) {

            value = td.getData(branchKey);
            groupedVector.add(td);
            if (!keys.isEmpty()) {
                if (keys.contains(value)) {
                    Collection<TableData> tval = table.get(value);
                    groupedVector.addAll(tval);
                }
            }
        }

        return groupedVector;

    }

    public Collection<TableData> postProcess(Collection<TableData> bomData, String sectionVal) throws WTException {
        Collection<TableData> fData = new ArrayList<TableData>();

        for (TableData row : bomData) {
            if (sectionVal.equals(row.getData("section"))) {
                // calculateTotal(row);
                fData.add(row);
            }
        }

        return fData;
    }

    private void mergeMaterialPricing(Collection<TableData> bomData) throws WTException {
        // CALCULATION OF TOTALS.
        double materialPrice;
        BigDecimal bomTotalSams = new BigDecimal("0.0");
        BigDecimal bomTotal = new BigDecimal("0.0");
        BigDecimal bdzero = new BigDecimal("0.0");

        Collection<Map> matSups = new ArrayList<Map>();
        Collection<Map> matSupColors = new ArrayList<Map>();

        // Make sure all attributes used in row calculations and totaling are pulled back when retrieving bom data
        // add priceOverride value to the dataset being pulled back
        for (TableData branch : bomData) {
            Map<String, String> matSupColor = new HashMap<String, String>();
            Map<String, String> matSup = new HashMap<String, String>();

            BigDecimal mpbd = new BigDecimal("0.0");
            // get the materialPrice from the branch (attribute on LCSMaterialSupplier)
            if (FormatHelper.hasContent(branch.getData("materialPrice"))) {
                mpbd = new BigDecimal(branch.getData("materialPrice"));
            }

            BigDecimal mcpbd = new BigDecimal("0.0");
            // get the materialColorPrice from the branch (attribute on LCSMaterialColor (legacy attribute)
            if (FormatHelper.hasContent(branch.getData("materialColorPrice"))) {
                mcpbd = new BigDecimal(branch.getData("materialColorPrice"));
            }

            BigDecimal pobd = new BigDecimal("0.0");
            // get the priceOverride from the branch (attribute on LCSFlexBOMLink)
            if (FormatHelper.hasContent(branch.getData("priceOverride"))) {
                pobd = new BigDecimal(branch.getData("priceOverride"));
            }

            BigDecimal qbd = new BigDecimal("0.0");
            // get the quantity from the branch (attribute on LCSFlexBOMLink)
            if (FormatHelper.hasContent(branch.getData("quantity"))) {
                qbd = new BigDecimal(branch.getData("quantity"));
            }

            BigDecimal labd = new BigDecimal("0.0");
            // get the lossAdjustment from the branch (attribute on LCSFlexBOMLink)
            if (FormatHelper.hasContent(branch.getData("lossAdjustment"))) {
                labd = new BigDecimal(branch.getData("lossAdjustment"));
            }

            BigDecimal samsbd = new BigDecimal("0.0");
            // get the sams from the branch (used for BOL, not sure what sams stands for)
            if (FormatHelper.hasContent(branch.getData("sams"))) {
                samsbd = new BigDecimal(branch.getData("sams"));
            }
            //changes to calculate rowTotal with new formula - start
            BigDecimal grossQtybd = new BigDecimal("0.0");
            if(FormatHelper.hasContent(branch.getData("scGrossQuantity"))){
            	grossQtybd = new BigDecimal(branch.getData("scGrossQuantity"));
            }
            else{
            	grossQtybd = new BigDecimal("0.0");
            }
            
            BigDecimal cifbd = new BigDecimal("0.0");
        	if(FormatHelper.hasContent(branch.getData("scCIF"))){
                cifbd = new BigDecimal(branch.getData("scCIF"));
            }
            else{
            	cifbd = new BigDecimal("0.0");
            }
            //changes to calculate rowTotal with new formula - end
            String materialColorId = FormatHelper.format(branch.getData("materialColorId"));
            String materialSupplierMasterId = FormatHelper.format(branch.getData("materialSupplierMasterId"));

            matSup.put("materialSupplierMasterId", materialSupplierMasterId);

            matSupColor.put("materialSupplierMasterId", materialSupplierMasterId);
            matSupColor.put("materialColorId", materialColorId);

            matSups.add(matSup);
            matSupColors.add(matSupColor);
			//changes to calculate rowTotal with new formula - start
            // Override material price if materialColorPrice is available
          /*  if (mcpbd.compareTo(bdzero) == 1) {
                mpbd = mcpbd;
            }
            */
			//changes to calculate rowTotal with new formula - end
            // Override again if user specified an Override Price in the BOM itself
            if (pobd.compareTo(bdzero) == 1) {
                mpbd = pobd;
            }

            // Calculate the loss adjustment to increase quantity q = q + ( q * la )
            if (labd.compareTo(bdzero) != 0) {
                BigDecimal tempbd = qbd.multiply(labd);
                qbd = qbd.add(tempbd);
            }

            // The row total is then quantity * price
			//changes to calculate rowTotal with new formula - start
           //BigDecimal rtBd = qbd.multiply(mpbd);
            BigDecimal rtBd = mpbd.add(cifbd).multiply(grossQtybd);
            //changes to calculate rowTotal with new formula - end
            String materialName = FormatHelper.format(branch.getData("name"));

            if (materialName.indexOf("--") < 0) {
                bomTotal = bomTotal.add(rtBd);
                bomTotalSams = bomTotalSams.add(samsbd);
                branch.setData("rowTotal", rtBd.toString());
            } else {
                branch.setData("rowTotal", rtBd.toString());
            }
        }

        // To calculate pricing if USE_BOMDATEPRICING is true
        if (matSupColors.size() > 0) {
            Date reqDate = new Date();
            bomTotal = new BigDecimal("0.0");
            bomTotalSams = new BigDecimal("0.0");

            try {
                if (FormatHelper.hasContent(timestamp)) {
                    reqDate = new Timestamp(FormatHelper.parseDate(timestamp).getTime());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            MaterialPriceList mpl = new MaterialPriceList(matSups, matSupColors, reqDate);

            for (TableData branch : bomData) {

                BigDecimal mcpbd = new BigDecimal("0.0");
                // get the materialColorPrice from the branch (attribute on LCSMaterialColor (legacy attribute)
                if (FormatHelper.hasContent(branch.getData("materialColorPrice"))) {
                    mcpbd = new BigDecimal(branch.getData("materialColorPrice"));
                }
                BigDecimal pobd = new BigDecimal("0.0");
                // get the priceOverride from the branch (attribute on LCSFlexBOMLink)
                if (FormatHelper.hasContent(branch.getData("priceOverride"))) {
                    pobd = new BigDecimal(branch.getData("priceOverride"));
                }

                BigDecimal qbd = new BigDecimal("0.0");
                // get the quantity from the branch (attribute on LCSFlexBOMLink)
                if (FormatHelper.hasContent(branch.getData("quantity"))) {
                    qbd = new BigDecimal(branch.getData("quantity"));
                }

                BigDecimal labd = new BigDecimal("0.0");
                // get the lossAdjustment from the branch (attribute on LCSFlexBOMLink)
                if (FormatHelper.hasContent(branch.getData("lossAdjustment"))) {
                    labd = new BigDecimal(branch.getData("lossAdjustment"));
                }

                BigDecimal samsbd = new BigDecimal("0.0");
                // get the sams from the branch (used for BOL, not sure what sams stands for)
                if (FormatHelper.hasContent(branch.getData("sams"))) {
                    samsbd = new BigDecimal(branch.getData("sams"));
                }
                //changes to calculate rowTotal with new formula - start
                BigDecimal grossQtybd = new BigDecimal("0.0");
                if(FormatHelper.hasContent(branch.getData("scGrossQuantity"))){
                	grossQtybd = new BigDecimal(branch.getData("scGrossQuantity"));
                }
                else{
                	grossQtybd = new BigDecimal("0.0");
                }
                
                BigDecimal cifbd = new BigDecimal("0.0");
            	if(FormatHelper.hasContent(branch.getData("scCIF"))){
                    cifbd = new BigDecimal(branch.getData("scCIF"));
                }
                else{
                	cifbd = new BigDecimal("0.0");
                }
                //changes to calculate rowTotal with new formula - end
                String materialColorId = FormatHelper.format(branch.getData("materialColorId"));
                String materialSupplierMasterId = FormatHelper.format(branch.getData("materialSupplierMasterId"));

                // use the materialPriceList to get the price for a specific material/supplier/color
                materialPrice = mpl.getPrice(materialSupplierMasterId, materialColorId);
                // convert the materialPrice to a BigDecimal
                BigDecimal mpbd = new BigDecimal("" + materialPrice);
				//changes to calculate rowTotal with new formula - start
               /* 
			   // Override material pricelist price if materialColorPrice is available
                if (mcpbd.compareTo(bdzero) == 1) {
                    mpbd = mcpbd;
                }
				*/
				//changes to calculate rowTotal with new formula - end
                // Calculate the loss adjustment to increase quantity q = q + ( q * la )
                if (labd.compareTo(bdzero) != 0) {
                    BigDecimal tempbd = qbd.multiply(labd);
                    qbd = qbd.add(tempbd);
                }

                branch.setData("materialPrice", "" + mpbd);

                // MOVED THIS CODE BELOW THE SETDATA CALL BECAUSE WHILE WE
                // WANT MATERIAL PRICE TO BE CHANGED FOR TOTAL ROLL UP,
                // WE DON"T WANT IT CHANGED IN THE TABLE's COLUMN.
                if (pobd.compareTo(bdzero) == 1) {
                    mpbd = pobd;
                }
				//changes to calculate rowTotal with new formula - start
               // BigDecimal rtBd = qbd.multiply(mpbd);
                BigDecimal rtBd = mpbd.add(cifbd).multiply(grossQtybd);
                //changes to calculate rowTotal with new formula - end
                String materialName = FormatHelper.format(branch.getData("name"));
                if (materialName.indexOf("--") < 0) {
                    bomTotal = bomTotal.add(rtBd);
                    bomTotalSams = bomTotalSams.add(samsbd);
                    branch.setData("rowTotal", rtBd.toString());
                } else {
                    branch.setData("rowTotal", rtBd.toString());
                }
            }
        }

    }

    protected void calculateTotal(TableData row) {
        double materialPrice = FormatHelper.parseDouble(row.getData("materialPrice"));
        double materialColorPrice = FormatHelper.parseDouble(row.getData("materialColorPrice"));
        double priceOverride = FormatHelper.parseDouble(row.getData("priceOverride"));
        double quantity = FormatHelper.parseDouble(row.getData("quantity"));
        double lossAdjustment = FormatHelper.parseDouble(row.getData("lossAdjustment"));

        if (materialColorPrice > 0) {
            materialPrice = materialColorPrice;
        }
        if (priceOverride > 0) {
            materialPrice = priceOverride;
        }

        if (lossAdjustment != 0) {
            quantity = quantity + (quantity * lossAdjustment);
        }

        double rowTotal = quantity * materialPrice;

        row.setData("rowTotal", "" + rowTotal);
    }

    public Collection<TableData> joinInLinkedAssemblies(Collection<TableData> data) throws WTException {
        // return this.dataSet;
        try {
            Collection<TableData> bomData = (Collection<TableData>) LCSFindFlexBOMDelegate.joinInLinkedBOMs(data);
            return bomData;

        } catch (Exception e) {
            throw new WTException(e);
        }
    }

    public Collection<TableColumn> getTableColumns() throws WTException {
        return getTableColumns(part.getFlexType(), view);
    }

    public Collection<TableColumn> getTableColumns(FlexType type, ColumnList view) throws WTException {
        Map<String, TableColumn> columnMap = getColumnMap(type);
        Collection<TableColumn> columns = new ArrayList<TableColumn>();

        for (String columnKey : getColumnList(view)) {
            TableColumn column = columnMap.get(columnKey);
            if (column != null) {
                column.setColumnClassIndex("highLight");
                columns.add(column);
            }
        }

        return columns;
    }

    public Collection<String> getColumnList(ColumnList view) {
        if (view == null) {
            return getDefaultColumnList();
        }

        Collection<String> columnList = new ArrayList<String>();
        // THIS IS THE COLUMN LAYOUT FOR A STANDARD BILL OF MATERIALS
        if (this.useMatThumbnail) {
            columnList.add("MATERIAL.thumbnail");
        }
        columnList.add("BOM.rowNumber");
		// Harshad -  Add Custpm PartName Key here
        columnList.add("BOM.scComponent");
       // columnList.add("BOM.materialDescription");

        Collection<String> viewAtts = view.getAttributes();

        if (viewAtts == null) {
            return columnList;
        }

        for (String attKey : viewAtts) {
            // LOGGER.debug("attKey: " + attKey);
            if ("materialDescription".equals(attKey) || "BOM.materialDescription".equals(attKey)) {
                if (!columnList.contains("MATERIAL.name"))
                    columnList.add("MATERIAL.name");
            } else if ("supplierName".equals(attKey)) {
                columnList.add("SUPPLIER.name");
            } else if ("ColorDescription".equals(attKey)) {
                if (LCSFlexBOMQuery.ALL_SKUS.equals(skuMode)) {
                    for (String skuObj : this.getColorways()) {
                        columnList.add("BOM.color$sku$" + skuObj);
                    }

                } else {
                    columnList.add("BOM.color");
                }
            } else if ("Colorways".equals(attKey)) {
                continue;
            } else if ("sourceDim".equals(attKey)) {
                continue;
            } else if ("destinationDim".equals(attKey)) {
                continue;
            }

            else if ("colorDim".equals(attKey)) {
                continue;
            } else if ("size1Dim".equals(attKey)) {
                continue;
            } else if ("size2Dim".equals(attKey)) {
                continue;
            }
            // CHUCK - Not sure if Size gets displayed at all or what the column would be in View mode
            else if ("SIZE1".equals(attKey)) {
                continue;
            } else if ("SIZE2".equals(attKey)) {
                continue;
            } else if (!columnList.contains(attKey)) {
                if (attKey.indexOf(".") > -1) {
                    attKey = attKey.substring(0, attKey.indexOf(".")).toUpperCase()
                            + attKey.substring(attKey.indexOf("."));
                }
                columnList.add(attKey);
            }
        }

        if (!WCPART_ENABLED) {
            if (columnList.contains("BOM.wcPartName")) {
                columnList.remove("BOM.wcPartName");
            }
        }

        return columnList;
    }

    private Collection<String> getDefaultColumnList() {
        Collection<String> columnList = new ArrayList<String>();
        if (this.useMatThumbnail) {
            columnList.add("MATERIAL.thumbnail");
        }
		columnList.add("BOM.rowNumber");
		// Harshad -  Add Custpm PartName Key here
		 columnList.add("BOM.scComponent");
        //columnList.add("BOM.materialDescription");

        columnList.add("MATERIAL.name");
        columnList.add("SUPPLIER.name");

        if (WCPART_ENABLED) {
            columnList.add("BOM.wcPartName");
        }

        if (LCSFlexBOMQuery.ALL_SKUS.equals(skuMode)) {
            for (String skuObj : this.getColorways()) {
                columnList.add("BOM.color$sku$" + skuObj);
            }

        } else {
            columnList.add("BOM.color");
            columnList.add("MATERIAL.unitOfMeasure");
            columnList.add("BOM.quantity");
        }

        if (useBOMCosting) {
            columnList.add("BOM.lossAdjustment");
            columnList.add("MATERIAL.materialPrice");
            columnList.add("BOM.priceOverride");
            columnList.add("BOM.rowTotal");

        }
        return columnList;
    }

    private static final String DISPLAY = "Display";

    private static final String OBJECT_REF = "object_ref";

    private static final String OBJECT_REF_LIST = "object_ref_list";

    private Map<String, TableColumn> getTableColumnMapFromAttrsList(Collection<FlexTypeAttribute> attsList,
            String prefix, FlexTypeGenerator flexg) throws WTException {
        Map<String, TableColumn> columnMap = new HashMap<String, TableColumn>();
        for (FlexTypeAttribute att : attsList) {
            TableColumn column = flexg.createTableColumn(att);
            column.setDisplayed(true);
            if (att.getAttScope() != null
                    && MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE.equals(att.getAttScope())) {
                column.setOwnerRefIndex("MATERIALSUPPLIERBRANCHID");
                column.setOwnerIdPrefix("VR:com.lcs.wc.material.LCSMaterialSupplier:");
            } else if ("MATERIAL COLOR.".equals(prefix)) {
                column.setOwnerRefIndex("MATERIALCOLORID");
                column.setOwnerIdPrefix("OR:com.lcs.wc.material.LCSMaterialColor:");
            } else if ("SUPPLIER.".equals(prefix)) {
                column.setOwnerRefIndex("SUPPLIERBRANCHID");
                column.setOwnerIdPrefix("VR:com.lcs.wc.supplier.LCSSupplier:");
            }
            if (OBJECT_REF.equals(att.getAttVariableType()) || OBJECT_REF_LIST.equals(att.getAttVariableType())) {
                column.setLinkTableIndex(att.getAttKey());
                column.setTableIndex(att.getAttKey() + DISPLAY);
            } else {
                column.setTableIndex(att.getAttKey());
            }
            columnMap.put(prefix + att.getAttKey(), column);
        }
        return columnMap;
    }

    public Map<String, TableColumn> getColumnMap() throws WTException {
        return getColumnMap(part.getFlexType());
    }

    public Map<String, TableColumn> getColumnMap(FlexType type) throws WTException {
        FlexTypeGenerator flexg = new FlexTypeGenerator();

        Collection<FlexTypeAttribute> atts = type.getAllAttributes(FlexBOMFlexTypeScopeDefinition.LINK_SCOPE, null,
                false);
        Collection<FlexTypeAttribute> materialAtts = materialType
                .getAllAttributes(MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE, null, false);
        Collection<FlexTypeAttribute> materialSupplierAtts = materialType
                .getAllAttributes(MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE, null, false);
        Collection<FlexTypeAttribute> materialColorAtts = FlexTypeCache.getFlexTypeFromPath(MATERIAL_COLOR_ROOT_TYPE)
                .getAllAttributes();
        Collection<FlexTypeAttribute> supplierAtts = FlexTypeCache.getFlexTypeFromPath(MATERIAL_SUPPLIER_ROOT_TYPE)
                .getAllAttributes();

        Map<String, TableColumn> columnMap = new HashMap<String, TableColumn>();

        columnMap.putAll(getTableColumnMapFromAttrsList(atts, "BOM.", flexg));
        columnMap.putAll(getTableColumnMapFromAttrsList(materialAtts, "MATERIAL.", flexg));
        columnMap.putAll(getTableColumnMapFromAttrsList(materialSupplierAtts, "MATERIAL.", flexg));
        columnMap.putAll(getTableColumnMapFromAttrsList(materialColorAtts, "MATERIAL COLOR.", flexg));
        columnMap.putAll(getTableColumnMapFromAttrsList(supplierAtts, "SUPPLIER.", flexg));

        TableColumn column = new BOMMaterialTableColumn();
        column.setDisplayed(true);
        column.setTableIndex("materialName");
        column.setHeaderLabel(materialLabel);
        column.setLinkMethod("viewMaterial");
        column.setLinkTableIndex("childId");
        column.setLinkMethodPrefix("OR:com.lcs.wc.material.LCSMaterialMaster:");
        column.setFormatHTML(true);
        ((BOMMaterialTableColumn) column).setDescriptionIndex("materialDescription");
        columnMap.put("MATERIAL.name", column);

        column = new TableColumn();
        column.setDisplayed(true);
        column.setTableIndex("materialColorId");
        column.setHeaderLabel("MatCol");
        column.setFormatHTML(true);
        columnMap.put("MATERIALCOLOR.ID", column);

        column = new TableColumn();
        column.setDisplayed(true);
        column.setTableIndex("supplierName");
        column.setHeaderLabel(supplierLabel);
        column.setLinkMethod("viewSupplier");
        column.setLinkTableIndex("supplierMasterId");
        column.setFormat(FormatHelper.STRING_FORMAT);
        column.setLinkMethodPrefix("OR:com.lcs.wc.supplier.LCSSupplierMaster:");
        columnMap.put("SUPPLIER.name", column);

        if (LCSFlexBOMQuery.ALL_SKUS.equals(skuMode)) {
            for (String skuId : this.getColorways()) {
                LCSSKU sku = LCSSKUQuery.getSKURevA(skuId);
                BOMColorTableColumn colorColumn = new BOMColorTableColumn();
                colorColumn.setDisplayed(true);
                colorColumn.setTableIndex("colorName$sku$" + skuId);
                colorColumn.setSecondaryTableIndex("colorName");
                colorColumn.setDescriptionIndex("colorDescription$sku$" + skuId);
                colorColumn.setSecondaryDescriptionIndex("colorDescription");
                colorColumn.setHeaderLabel((String) sku.getValue("skuName"));
                colorColumn.setLinkMethod("viewColor");
                colorColumn.setLinkTableIndex("colorId$sku$" + skuId);
                colorColumn.setSecondaryLinkTableIndex("colorId");
                colorColumn.setLinkMethodPrefix("OR:com.lcs.wc.color.LCSColor:");
                colorColumn.setColumnWidth("1%");
                colorColumn.setWrapping(false);
                colorColumn.setBgColorIndex("colorHexColor$sku$" + skuId);
                colorColumn.setSecondaryBgColorIndex("colorHexColor");
                colorColumn.setUseColorCell(true);
                colorColumn.setAlign("center");
                colorColumn.setImageIndex("colorThumbnail$sku$" + skuId);
                colorColumn.setSecondaryImageIndex("colorThumbnail");
                colorColumn.setUseColorCell(this.useColorSwatch);
                colorColumn.setFormatHTML(false);

                columnMap.put("BOM.color$sku$" + skuId, colorColumn);
            }

        } else {
            BOMColorTableColumn colorColumn = new BOMColorTableColumn();
            colorColumn.setDisplayed(true);
            colorColumn.setTableIndex("colorName");
            colorColumn.setDescriptionIndex("colorDescription");
            colorColumn.setHeaderLabel(colorLabel);
            colorColumn.setLinkMethod("viewColor");
            colorColumn.setLinkTableIndex("colorId");
            colorColumn.setLinkMethodPrefix("OR:com.lcs.wc.color.LCSColor:");
            colorColumn.setColumnWidth("1%");
            colorColumn.setWrapping(false);
            colorColumn.setBgColorIndex("colorHexColor");
            colorColumn.setUseColorCell(true);
            colorColumn.setImageIndex("colorThumbnail");
            colorColumn.setUseColorCell(this.useColorSwatch);
            colorColumn.setFormatHTML(false);

            columnMap.put("BOM.color", colorColumn);
        }

        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel("");
        column.setHeaderAlign("left");
        column.setLinkMethod("launchImageViewer");
        column.setLinkTableIndex("childThumbnail");
        column.setTableIndex("childThumbnail");
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
        columnMap.put("MATERIAL.thumbnail", column);

        column = new LifecycleStateTableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(materialStatusLabel);
        column.setHeaderAlign("left");
        column.setTableIndex("materialSupplierStatus");
        columnMap.put("MATERIALSUPPLIER.status", column);

        column = (TableColumn) columnMap.get("BOM.section");
        column.setDisplayed(false);

        column = (TableColumn) columnMap.get("BOM.quantity");
        column.setShowCriteria("0");
        column.setShowCriteriaNot(true);
        column.setShowCriteriaTarget(column.getTableIndex());
	// Harshad -  Add Custpm PartName Key here
        column = (TableColumn) columnMap.get("BOM.scComponent");

        BOMPartNameTableColumn columnt = new BOMPartNameTableColumn();
        columnt.setHeaderLabel(column.getHeaderLabel());
        columnt.setTableIndex(column.getTableIndex());
        columnt.setDisplayed(column.isDisplayed());
        columnt.setSubComponetIndex("masterBranchId");
        columnt.setComplexMaterialIndex("masterBranch");
        columnt.setLinkedBOMIndex("linkedBOM");
        columnt.setFormatHTML(false);
        columnt.setWrapping(true);
		// Harshad -  Add Custpm PartName Key here
        columnMap.put("BOM.materialDescription", columnt);

        if (this.useMatThumbnail) {
            Collection<TableColumn> cols = columnMap.values();
            for (TableColumn tc : cols) {
                if ("image".equals(tc.getAttributeType())) {
                    tc.setShowFullImage(true);
                }
            }
        }

        return columnMap;
    }

    private Collection getUsedAttList(Collection clist) {
        Iterator i = clist.iterator();
        Set attList = new HashSet();
        String key = "";
        while (i.hasNext()) {
            key = (String) i.next();
            if (key.indexOf(".") > -1) {
                key = key.substring(key.indexOf(".") + 1);
            }
            attList.add(key);
        }
        attList.add("section");
        attList.add("materialDescription");
        attList.add("colorDescription");
        attList.add("highLight");
        attList.add("unitOfMeasure");
        attList.add("materialPrice");
        attList.add("materialColorPrice");
        attList.add("materialColorSamsIndex");
        attList.add("materialSAMsTotal");
        return attList;
    }
}
