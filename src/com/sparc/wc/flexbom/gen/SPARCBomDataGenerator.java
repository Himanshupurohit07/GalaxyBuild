/*
 * BomDataGenerator.java
 *
 * Created on October 31, 2006, 10:58 AM
 */

package com.sparc.wc.flexbom.gen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wt.util.WTException;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.client.web.TableColumn;
import com.lcs.wc.client.web.TableData;
import com.lcs.wc.client.web.TableDataUtil;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.report.ColumnList;
import com.lcs.wc.report.ReportQuery;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSMessage;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.RB;
import com.lcs.wc.util.SortHelper;
import com.lcs.wc.flexbom.gen.*;

/**
 *
 * @author Chuck
 */
public abstract class SPARCBomDataGenerator {
  //  private static final Logger LOGGER = LogManager.getLogger(BomDataGenerator.class.getName());

    public static final boolean WCPART_ENABLED = LCSProperties.getBoolean("com.lcs.wc.specification.parts.Enabled");

    public static final String RAW_BOM_DATA = "RAW_DATA";

    public static final String SOURCES = "SOURCES";

    public static final String COLORWAYS = "COLORWAYS";

    public static final String SIZES1 = "SIZES1";

    public static final String SIZES2 = "SIZES2";

    public static final String DESTINATIONS = "DESTINATIONS";

    public static final String SECTION = "SECTION";

    public static final String VIEW_ID = "VIEW_ID";

    public static final String SIZE_ATT = "SIZE_ATT";

    public static final String SIZE_DISPLAY = "SIZE_DISPLAY";

    public static final String USE_COLOR_SWATCH = "USE_COLOR_SWATCH";

    public static final String USE_MAT_THUMBNAIL = "USE_MAT_THUMBNAIL";

    public static final String USE_SIZE1_SIZE2 = "USE_SIZE1_SIZE2";

    protected static final String DIM_COL = "FLEXBOMLINK.DIMENSIONNAME";

    protected static final String CPLX_COL = "FLEXBOMLINK.MASTERBRANCHID";

    protected static final String DIMID_COL = "FLEXBOMLINK.DIMENSIONID";

    protected static final String WCPARTNAME = "WTPART.DISPLAYIDENTIFIER";

    protected Collection<FlexObject> topLevelBranches = null;

    protected Map<String, Collection<FlexObject>> orMap = null;

    protected Collection<TableData> dataSet = new ArrayList<TableData>();

    private Collection<String> sources = new ArrayList<String>();

    private Collection<String> colorways = new ArrayList<String>();

    private Collection<String> sizes1 = new ArrayList<String>();

    private Collection<String> sizes2 = new ArrayList<String>();

    private Collection<String> destinations = new ArrayList<String>();

    protected String section = "";

    protected String viewId = "";

    protected ColumnList view = null;

    protected Map<String, TableColumn> columnMap = null;

    protected String sizeAtt = "SIZE1";

    protected FlexTypeAttribute displayAtt = null;

    protected String displayAttKey = "quantity";

    protected String displayAttCol = "";

    protected boolean isAttList = false;

    protected boolean useColorSwatch = false;

    protected boolean useMatThumbnail = false;

    private String sectionAtt = "";

    private FlexType bomType = null;

    protected String materialLabel = LCSMessage.getLocalizedMessage(RB.MATERIAL, "material_LBL");

    protected String supplierLabel = LCSMessage.getLocalizedMessage(RB.FLEXBOM, "supplier_LBL");

    public void init(Map<String, Object> params) throws WTException {
        bomType = FlexTypeCache.getFlexTypeRoot("BOM");
        sectionAtt = bomType.getAttribute("section").getColumnName();
        topLevelBranches = null;
        orMap = null;
        columnMap = null;

        if (params != null) {
            if (params.get(SPARCBomDataGenerator.RAW_BOM_DATA) != null) {

                this.dataSet = new ArrayList<TableData>();
                this.dataSet.addAll((Collection<TableData>) params.get(SPARCBomDataGenerator.RAW_BOM_DATA));

            }

            if (params.get(SPARCBomDataGenerator.SOURCES) != null) {
                this.sources = (Collection) params.get(SPARCBomDataGenerator.SOURCES);
            }
            if (params.get(SPARCBomDataGenerator.COLORWAYS) != null) {
                this.colorways = (Collection<String>) params.get(SPARCBomDataGenerator.COLORWAYS);
            }
            if (params.get(SPARCBomDataGenerator.SIZES1) != null) {
                this.sizes1 = (Collection<String>) params.get(SPARCBomDataGenerator.SIZES1);
            }
            if (params.get(SPARCBomDataGenerator.SIZES2) != null) {
                this.sizes2 = (Collection<String>) params.get(SPARCBomDataGenerator.SIZES2);
            }
            if (params.get(SPARCBomDataGenerator.DESTINATIONS) != null) {
                this.destinations = (Collection) params.get(SPARCBomDataGenerator.DESTINATIONS);
            }

            if (params.get(SPARCBomDataGenerator.SECTION) != null) {
                this.section = (String) params.get(SPARCBomDataGenerator.SECTION);
            }

            if ("true".equals(params.get(SPARCBomDataGenerator.USE_COLOR_SWATCH))) {
                this.useColorSwatch = true;
            }

            if ("true".equals(params.get(SPARCBomDataGenerator.USE_MAT_THUMBNAIL))) {
                this.useMatThumbnail = true;
            }

            if (params.get(SPARCBomDataGenerator.VIEW_ID) != null) {
                this.viewId = (String) params.get(SPARCBomDataGenerator.VIEW_ID);
                if (FormatHelper.hasContent(this.viewId)) {
                    view = (ColumnList) LCSQuery.findObjectById(this.viewId);
                } else {
                    view = null;
                }
            }

            if (params.get(SPARCBomDataGenerator.SIZE_ATT) != null) {
                this.sizeAtt = (String) params.get(SPARCBomDataGenerator.SIZE_ATT);
            }
            if (params.get(SPARCBomDataGenerator.SIZE_DISPLAY) != null) {
                this.displayAttKey = (String) params.get(SPARCBomDataGenerator.SIZE_DISPLAY);
            }

        }
        displayAtt = bomType.getAttribute(displayAttKey);
        displayAttCol = displayAtt.getSearchResultIndex();
        isAttList = (displayAtt.getAttValueList() != null);

    }

    public abstract Collection getBOMData() throws WTException;

    public abstract Collection getTableColumns() throws WTException;

    public Collection<String> getSources() {
        return sources;
    }

    public void setSources(Collection<String> sources) {
        this.sources = sources;
    }

    public Collection<String> getColorways() {
        return colorways;
    }

    public void setColorways(Collection<String> colorways) {
        this.colorways = colorways;
    }

    public Collection<String> getSizes1() {
        return sizes1;
    }

    public void setSizes1(Collection<String> sizes1) {
        this.sizes1 = sizes1;
    }

    public Collection<String> getSizes2() {
        return sizes2;
    }

    public void setSizes2(Collection<String> sizes2) {
        this.sizes2 = sizes2;
    }

    public Collection<String> getDestinations() {
        return destinations;
    }

    public void setDestinations(Collection<String> destinations) {
        this.destinations = destinations;
    }

    protected String getDisplayVal(String val) throws WTException {
        if (!isAttList)
            return val;

        return displayAtt.getDisplayForLocale(val, ClientContext.getContext().getLocale());
    }

    protected Map<String, Collection<FlexObject>> getOverRideMap() {
        if (this.orMap != null) {
            return this.orMap;
        }

        Map<String, Collection<FlexObject>> dataMap = new HashMap<String, Collection<FlexObject>>();

        // Collect list of top level branches
        Collection<FlexObject> tlBranches = getTLBranches();

        for (FlexObject tlbranch : tlBranches) {
            Collection<FlexObject> overrides = new ArrayList<FlexObject>();
            String tlDimId = tlbranch.getString(DIMID_COL);
            String tlBranch = getNameStubFromDimId(tlDimId);

            for (TableData branch : dataSet) {
                FlexObject obj = (FlexObject) branch;
                if (FormatHelper.hasContent(obj.getString(DIM_COL))) {
                    String orDimId = obj.getString(DIMID_COL);
                    // orDimId = orDimId.substring(0, orDimId.indexOf("-"))

                    if (FormatHelper.hasContent(orDimId)) {
                        String orBranch = getNameStubFromDimId(orDimId);
                        if (tlBranch.equals(orBranch)) {
                            overrides.add(obj);
                        }
                    }
                }
            }

            dataMap.put(tlDimId, overrides);
        }

        this.orMap = dataMap;
        return dataMap;
    }

    public static String getBranchFromDimId(String dimId) {
        // tlDimId: -PARENT:com.lcs.wc.flexbom.FlexBOMPartMaster:165797-REV:A-BRANCH:1
        // orDimId:
        // -PARENT:com.lcs.wc.flexbom.FlexBOMPartMaster:165797-REV:A-BRANCH:15-SKU:com.lcs.wc.part.LCSPartMaster:165714
        String BC = "BRANCH:";

        if (dimId.indexOf(BC) < 0)
            return "";

        String stub = dimId.substring(dimId.indexOf(BC) + BC.length());
        if (stub.indexOf("-") > -1) {
            stub = stub.substring(0, stub.indexOf("-"));
        }

        return stub.trim();

    }

    public static String getNameStubFromDimId(String dimId) {
        String nameStub = dimId.substring(0, dimId.indexOf("BRANCH:") + "BRANCH:".length()) + getBranchFromDimId(dimId);
        return nameStub.trim();
    }

    protected Collection<FlexObject> getTLBranches() {
        if (topLevelBranches != null) {
            return topLevelBranches;
        }

        // Collect list of top level branches
        Collection<FlexObject> topLevelBranches = new ArrayList<FlexObject>();
        for (TableData branch : dataSet) {
            FlexObject obj = (FlexObject) branch;
            if (!FormatHelper.hasContent(obj.getString(DIM_COL)) /*
                                                                  * &&
                                                                  * !FormatHelper.hasContent(branch.getString(CPLX_COL))
                                                                  */) {
                if (FormatHelper.hasContent(this.section)) {
                    if (this.section.equals(obj.getString("FLEXBOMLINK." + sectionAtt))) {
                        topLevelBranches.add(obj);
                    }
                } else {
                    topLevelBranches.add(obj);
                }
            }
        }
        return topLevelBranches;
    }

    protected FlexObject getOverrideRowForDim(String tlId, String dimVal, String matSupId, String dim) {
        Map<String, Collection<FlexObject>> ors = getOverRideMap();
        Collection<FlexObject> cors = ors.get(tlId);

        // LOOK FOR SOURCE:SKU
        for (FlexObject fobj : cors) {
            if (!dim.equals(fobj.getString("FLEXBOMLINK.DIMENSIONNAME"))) {
                continue;
            }

            if (FormatHelper.hasContent(matSupId)) {
                if (FormatHelper.hasContent(fobj.getString(DIMID_COL)) && fobj.getString(DIMID_COL).endsWith(dimVal)
                        && matSupId.equals(fobj.getString("LCSMATERIALSUPPLIERMASTER.IDA2A2"))) {
                    return fobj;
                }
            } else {
                if (FormatHelper.hasContent(fobj.getString(DIMID_COL)) && fobj.getString(DIMID_COL).endsWith(dimVal)) {
                    return fobj;
                }
            }
        }

        return null;
    }

    public static Collection<TableData> groupDataToBranchId(Collection<TableData> data, String branchKey,
            String masterKey, String sortingKey) {
        if (data == null || data.size() < 1) {
            return new ArrayList<TableData>();
        }
        Map<String, List<TableData>> table = TableDataUtil.groupIntoCollections(data, masterKey);
        List<TableData> collectionOfTableData = new ArrayList<TableData>();
        Collection<TableData> current;
        List<TableData> groupedVector = new ArrayList<TableData>();
        current = table.get("0");
        if (current == null) {
            return new ArrayList<TableData>();
        }
        groupedVector.addAll(current);
        table.remove("0");

        Set<String> keys = table.keySet();

        String value = "";
        for (TableData td : current) {

            // CMS - Check added to prevent override records causing linked records to get
            // added more than once.
            if (FormatHelper.hasContent(td.getData("FLEXBOMLINK.DIMENSIONNAME"))) {
                if (!keys.isEmpty()) {
                    collectionOfTableData.add(td);
                }
                continue;
            }
            value = td.getData(branchKey);
            if (!keys.isEmpty()) {

/*
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("keys are not empty and the size is " + keys.size());
                }
*/
                collectionOfTableData.add(td);
                if (keys.contains(value)) {

        //            LOGGER.debug("keys set contains value about to add parts of the complex material");

                    Collection<TableData> tval = table.get(value);
                    if (FormatHelper.hasContent(sortingKey)) {
                        tval = SortHelper.sortMapsNumeric(tval, "FLEXBOMLINK.SORTINGNUMBER");
                        collectionOfTableData.addAll(tval);
                    }
                }
            }
        }

        if (!collectionOfTableData.isEmpty()) {
            groupedVector.clear();
            groupedVector.addAll(collectionOfTableData);
        }
        return groupedVector;

    }

    protected String addString(String collection, String newStr) {
        String newString = "";
        if (!FormatHelper.hasContentAllowZero(collection)) {
            newString = newStr;
        } else {
            List<String> strs = new ArrayList<String>(MOAHelper.getMOACollection(collection));
            strs.add(newStr);
            Collections.sort(strs);
            newString = MOAHelper.toMOAString(strs);
        }

        return newString;
    }

    protected Map<String, TableColumn> getViewColumns() throws WTException {

        if (columnMap != null)
            return columnMap;
        if (this.view == null)
            return new HashMap<String, TableColumn>();

        columnMap = new HashMap<String, TableColumn>();

        if (!("EDIT_BOM".equals(this.view.getRelevantActivity()))) {
            // THIS VIEW CAN NOT CURRENTLY APPLY TO BOM REPORT
            return columnMap;
        }

        ReportQuery.getTableColumns(this.view, columnMap, false, false, null, null);

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
}
