<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java" session="false"
      errorPage="/rfa/jsp/flexbom/ViewBOMError.jsp"
       import="com.infoengine.object.factory.*,
                com.lcs.wc.flexbom.*,
                com.lcs.wc.client.Activities,
                com.lcs.wc.client.ClientContext,
                com.lcs.wc.client.web.*,
                com.lcs.wc.db.*,
                com.lcs.wc.document.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.infoengine.client.web.*,
                com.lcs.wc.flexbom.*,
                com.lcs.wc.foundation.*,
                com.lcs.wc.material.*,
                com.lcs.wc.part.*,
                com.lcs.wc.product.*,
                com.lcs.wc.report.*,
                com.lcs.wc.supplier.*,
                com.lcs.wc.color.*,
                com.lcs.wc.specification.*,
                com.lcs.wc.sourcing.*,
                com.lcs.wc.util.*,
                wt.org.*,
                wt.util.*,
                wt.locks.LockHelper,
                wt.ownership.*,
                wt.fc.WTObject,
                java.text.*,
                com.infoengine.object.factory.*,
                com.ptc.core.adapter.server.impl.*,
                wt.method.*,
                java.sql.Timestamp,
                org.apache.logging.log4j.Logger,
                org.apache.logging.log4j.LogManager,

                com.ptc.netmarkets.util.misc.NetmarketURL,
                com.ptc.netmarkets.util.beans.NmURLFactoryBean,
                com.ptc.netmarkets.util.misc.NmAction,
                java.math.*,
                java.util.*"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="productModel" scope="request" class="com.lcs.wc.product.LCSProductClientModel" />
<jsp:useBean id="type" scope="request" class="com.lcs.wc.flextype.FlexType" />
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="flexg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="columnList" scope="request" class="java.util.ArrayList" />
<jsp:useBean id="columns" scope="request" class="java.util.ArrayList" />
<jsp:useBean id="data" scope="request" class="java.util.ArrayList" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="request"/>
<% wt.util.WTContext.getContext().setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));
lcsContext.initContext();
lcsContext.setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%

String operationLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "operation_LBL", RB.objA ) ;
String materialLabel = WTMessage.getLocalizedMessage ( RB.MATERIAL, "material_LBL", RB.objA ) ;
String colorLabel = WTMessage.getLocalizedMessage ( RB.COLOR, "color_LBL", RB.objA ) ;
String supplierLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "supplier_LBL", RB.objA ) ;
String materialStatusLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "materialStatus_LBL", RB.objA ) ;
String totalWithEqualSignLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "totalWithEqualSign_LBL", RB.objA ) ;
String pricingDateLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "pricingDate_LBL",RB.objA ) ;
String runLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "run_LBL",RB.objA ) ;
String hideImagesLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "hideImages_LBL",RB.objA ) ;
String showImagesLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "showImages_LBL",RB.objA ) ;
String generatePDFOption = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "generatePDF_LBL", RB.objA ) ;//"Generate PDF";
String nameLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "name_LBL", RB.objA ) ;
String typeLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "type_LBL", RB.objA ) ;
String bomIdentificationGrpTitle = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "bomIdentification_GRP_TLE", RB.objA ) ;
String headerAttributesLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "headerAttributes_LBL", RB.objA ) ;

%>
<%!
   private static final Logger logger = LogManager.getLogger("rfa.jsp.flexbom.ViewBOM");
   public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");
/// VRD EXTENSION START: Totals for Vendor
	public static final boolean  SECTIONTOTAL_ENABLED_FORVENDOR = LCSProperties.getBoolean("ValueReady.VendorPortalUsers.BOM.SectionTotalEnabled");
/// VRD EXTENSION END
    public static final String JSPNAME = "ViewBOM";

    public static final boolean COSTING = LCSProperties.getBoolean("jsp.flexbom.costedBOM");

    //This property turns on using date based pricing effectivity for BOM
    public static final boolean USE_BOMDATEPRICING = LCSProperties.getBoolean("com.lcs.wc.material.useBOMDatePricing");

    //This property controls whether date based pricing should use the date widget or the iteration date for all non-latest iterations
    public static final boolean USE_BOMDATEPRICING_OLDITERATION = LCSProperties.getBoolean("com.lcs.wc.material.usePriceDateWidgetOldIterations");
    public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
    public static final String WT_IMAGE_LOCATION = LCSProperties.get("flexPLM.windchill.ImageLocation");
    public static final boolean USE_MULTILEVEL = LCSProperties.getBoolean("jsp.flexbom.ViewBOM.useMultilevel");
    public static final boolean NEW_PDF_PRINT_BOM = LCSProperties.getBoolean("com.lcs.wc.product.PDFProductSpecificationGenerator.newPrintBOM");

    public static final String UPDATE_BTNS = "UPDATE_BTNS";
    public static final String HEADER_PAGE = "HEADER_PAGE";
    public static final String DETAILS_PAGE = "DETAILS_PAGE";
    public static final String ADD_PAGE = "ADD_PAGE";

    public static final String VIEW_BOM_COLUMN_ORDER = PageManager.getPageURL("VIEW_BOM_COLUMN_ORDER", null);
    public static String EXCEL_GENERATOR_SUPPORT_PLUGIN = PageManager.getPageURL("EXCEL_GENERATOR_SUPPORT_PLUGIN", null);
    public static final String PDF_GENERATOR = PageManager.getPageURL("PDF_GENERATOR", null);
    public static final String WC_META_DATA= PageManager.getPageURL("WC_META_DATA", null);
    public static String MATERIAL_SUPPLIER_ROOT_TYPE = LCSProperties.get("com.lcs.wc.supplier.MaterialSupplierRootType", "Supplier");
    public static final boolean WCPART_ENABLED = LCSProperties.getBoolean("com.lcs.wc.specification.parts.Enabled");
    public static final String PRIMARY_MATERIAL_GROUP = LCSProperties.get("com.lcs.wc.flexbom.PrimaryMaterialGroup", "Primary Material");
/// VRD EXTENSION START: Configuration for pricing calculations
	private static final String COLORSPECIFICPRICE = LCSProperties.get("com.vrd.costsheet.CostSheetBOMPlugin.ColorSpecificPrice");
	private static final String PRICEOVERRIDE = LCSProperties.get("com.vrd.costsheet.CostSheetBOMPlugin.PriceOverride", "priceOverride");
	private static final String MATERIALQUANTITY = LCSProperties.get("com.vrd.costsheet.CostSheetBOMPlugin.MaterialQuantity", "quantity");
	private static final String LOSSADJUSTMENT = LCSProperties.get("com.vrd.costsheet.CostSheetBOMPlugin.LossAdjustment", "lossAdjustment");
	private static final String MARKUP = LCSProperties.get("com.vrd.costsheet.CostSheetBOMPlugin.MarkUp", "markUp");
/// VRD EXTENSION END

    public static String instance = "";
    static {
        try {
            instance = wt.util.WTProperties.getLocalProperties().getProperty ("wt.federation.ie.VMName");
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static final Collection sortTopLevel(Collection data, FlexType type) throws WTException{
       if(data == null || data.size() < 1){
            return new Vector();
       }

        Vector groupedVector = new Vector();

        Map table = TableDataUtil.groupIntoCollections(data, "section");
        Collection sections = type.getAttribute("section").getAttValueList().getSelectableKeys(null, true);
        String key = "";
        Iterator i = sections.iterator();
        Collection subSet = null;
        while(i.hasNext()){
            key = (String)i.next();
            subSet = (Collection)table.get(key);
            if(subSet != null){
                subSet = SortHelper.sortFlexObjectsByNumber(subSet,"sortingNumber");
                groupedVector.addAll(subSet);
            }
        }

        return groupedVector;
    }

    public static final Collection groupDataToBranchId(Collection data, FlexType type) throws WTException{
       if(data == null || data.size() < 1){
            return new Vector();
       }

       Map table = TableDataUtil.groupIntoCollections(data, "masterBranchId");
       Vector groupedVector = new Vector();
       List current = (List) table.get("0");
       if(current == null){
           return new Vector();
       }
       current = new Vector(sortTopLevel(current, type));
       table.remove("0");

       Set keys = table.keySet();

       int index = 0;

       for (int i=0;i<current.size();i++){
         TableData  td = (TableData)current.get(i);
         groupedVector.add(td);
         String value = td.getData("branchId");
         if(keys.contains(value)) {

            //LC: Sort the sub bom items by partName before adding to the vector
            Collection colSubBranches = (Collection)table.get(value);
            colSubBranches = SortHelper.sortFlexObjectsByNumber(colSubBranches,"sortingNumber");

            groupedVector.addAll( colSubBranches );
         }
       }


       return groupedVector;

    }
    //-----------------------------------------------------------------

    public static Collection getUsedAttList(Collection clist){
        Iterator i = clist.iterator();
        Set attList = new HashSet();
        String key = "";

        while(i.hasNext()){
            key = (String)i.next();
            if(key.indexOf(".") > -1){
                key = key.substring(key.indexOf(".") + 1);
            }

            attList.add(key);
        }

        attList.add("section");
        attList.add("materialDescription");
        attList.add("colorDescription");
        attList.add("highLight");

        return attList;
    }

    public static final Map extractComplexMaterialHeaderRecords(Collection data) throws WTException{
        HashMap map = new HashMap();

        FlexObject row = null;
        for(Iterator i = data.iterator(); i.hasNext();){
            row = (FlexObject)i.next();
            if(FormatHelper.hasContent(row.getData("MASTERBRANCH")) || FormatHelper.hasContent(row.getData("LINKEDBOM"))){
                map.put(row.getData("BRANCHID"), row);
            }
        }

        return map;
    }

    public static final boolean hasParentPrice(String branchId, Map complexMap){
        if(branchId != null && complexMap.containsKey(branchId)){
            FlexObject parent = (FlexObject)complexMap.get(branchId);
            if(FormatHelper.hasContent(parent.getData("materialPrice"))
                    || FormatHelper.hasContent(parent.getData("materialColorPrice"))
                    || FormatHelper.hasContent(parent.getData("priceOverride"))){

                return true;
            }
        }
        return false;
    }
%>
<%
    String showThumb = request.getParameter("showThumbs");
    String oid = request.getParameter("oid");
    String csv = request.getParameter("csv");
    String pdf = request.getParameter("pdf");
    String bomPartId = request.getParameter("bomPartId");

    boolean hideExportFunctions = FormatHelper.parseBoolean(request.getParameter("hideExportFunctions"));
    String hideViewFunctions = request.getParameter("hideViewFunctions");

    String isBOMOwner = request.getParameter("isBOMOwner");
    boolean isOwner = "true".equals(isBOMOwner) || null == isBOMOwner;

    String pricingDateDateString = "";
    FlexBOMPart part;
    if(FormatHelper.hasContent(bomPartId)){
        part = (FlexBOMPart)LCSPartQuery.findObjectById(bomPartId);
    }else{
        part = (FlexBOMPart) request.getAttribute("contextBOMPart");
    }

    boolean isLatestIteration = VersionHelper.isLatestIteration(part);

    if(!isLatestIteration){
        if(USE_BOMDATEPRICING_OLDITERATION){
            pricingDateDateString = request.getParameter("pricingDateDateString");
        }
        else{
            pricingDateDateString = FormatHelper.format(wt.fc.PersistenceHelper.getModifyStamp(part));
        }
    }
    else{
        if (USE_BOMDATEPRICING){
            pricingDateDateString = request.getParameter("pricingDateDateString");
        }
    }

    if(!FormatHelper.hasContent(pricingDateDateString)){
        pricingDateDateString = FormatHelper.format(new Date());
    }




    String partOid = "";
    if(part == null){
        partOid = request.getParameter("partOid");
        part = (FlexBOMPart) LCSPartQuery.findObjectById(partOid);
    } else {
        partOid = FormatHelper.getObjectId(part);
    }
    boolean billOfLabor = "LABOR".equals(part.getBomType());
    type = part.getFlexType();

    String currCode = type.getAttribute("rowTotal").getCurrencyCode();

    String ownerMode = "PRODUCT";
    BOMOwner ownerMaster = part.getOwnerMaster();
    if(!(ownerMaster instanceof LCSPartMaster)){
        ownerMode = "MATERIAL";
    }


    FlexType productType = FlexTypeCache.getFlexTypeFromPath("Product");

    Collection skuList = (Collection) request.getAttribute("contextSKUList");

    String specReport = "";
    if(part != null && "PRODUCT".equals(ownerMode)){
        specReport = (new FlexSpecUtil()).getComponentWhereUsedDetailsTable((WTObject)part.getMaster());
        if (part.getDesignDoc() != null) {
            LCSProduct productRevA = (LCSProduct) request.getAttribute("contextProductRevA");
            String prodMasterId = FormatHelper.getNumericFromReference(productRevA.getMasterReference());
            specReport += (new FlexSpecUtil()).getAssociatedImagePageDetail(part, prodMasterId);
        }
    }

    String synchReport = "";
    if(part != null && "PRODUCT".equals(ownerMode)){
        synchReport = (new BOMSynchUtil()).getBOMSynchReportTable(part);
    }

    ////////////////////////////////////////////////////////////
    // DETERMINE THE LEFT SIDE CONTEXT
    ////////////////////////////////////////////////////////////
    String size1 = FormatHelper.format(request.getParameter("size1"));
    String size2 = FormatHelper.format(request.getParameter("size2"));

    String skuMasterId = "";
    LCSSKU skuSeasonRev = (LCSSKU) request.getAttribute("contextSKUSeasonRev");
    String skuMode = "SINGLE";
    String sourceDimId = "";
    String destinationId = "";

    if(isOwner){
        if(skuSeasonRev == null && FormatHelper.hasContent(request.getParameter("contextSKUId"))){
            String skuId = (String)request.getParameter("contextSKUId");
            if (!skuId.equals("ALL_SKUS") && skuId.indexOf("SKU") > 0) {
                skuSeasonRev = (LCSSKU) LCSQuery.findObjectById(request.getParameter("contextSKUId"));
            }
        }

        if (skuSeasonRev != null) {
            skuMasterId = FormatHelper.getNumericFromOid(FormatHelper.getObjectId((WTObject)skuSeasonRev.getMaster()));
        } else {
            if(LCSFlexBOMQuery.ALL_SKUS.equals(request.getParameter("contextSKUId"))){
                skuMode = LCSFlexBOMQuery.ALL_SKUS;
            }
        }
        if(FormatHelper.hasContent(request.getParameter("materialColorId"))){
            if(LCSFlexBOMQuery.ALL_SKUS.equals(request.getParameter("materialColorId"))){
                skuMode = LCSFlexBOMQuery.ALL_SKUS;
            }
            else{
                skuMasterId = FormatHelper.getNumericFromOid(request.getParameter("materialColorId"));
            }
        }

        if("MATERIAL".equals(ownerMode) && FormatHelper.hasContent(request.getParameter("supplierLinkId"))){
            sourceDimId = FormatHelper.getObjectId((LCSMaterialSupplierMaster)((LCSMaterialSupplier)LCSQuery.findObjectById(request.getParameter("supplierLinkId"))).getMaster());
        } else if("PRODUCT".equals(ownerMode)){
            LCSSourcingConfig sourcingConfig = (LCSSourcingConfig) request.getAttribute("contextSourcingConfig");
            if (sourcingConfig != null){
                sourceDimId = FormatHelper.getNumericFromOid(FormatHelper.getObjectId((LCSSourcingConfigMaster)sourcingConfig.getMaster()));
            }
        }

        if("PRODUCT".equals(ownerMode)){
            destinationId = FormatHelper.format(request.getParameter("destinationId"));
        }
    }

    Collection atts = type.getAllAttributes(FlexBOMFlexTypeScopeDefinition.LINK_SCOPE, null, false);

    FlexType materialType = part.getFlexType().getReferencedFlexType(ReferencedTypeKeys.MATERIAL_TYPE);
    Collection materialAtts = materialType.getAllAttributes(MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE, null, false);
    Collection materialSupplierAtts = materialType.getAllAttributes(MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE, null, false);
    Collection supplierAtts = FlexTypeCache.getFlexTypeFromPath(MATERIAL_SUPPLIER_ROOT_TYPE).getAllAttributes();
    Collection matColorAtts = FlexTypeCache.getFlexTypeRoot("Material Color").getAllAttributes();
    Collection colorAtts = FlexTypeCache.getFlexTypeRoot("Color").getAllAttributes();

    String iWidth= request.getParameter("imageWidth");
    String iHeight= request.getParameter("imageHeight");
    int imageWidth = 0;
    int imageHeight = 0;
    if ( FormatHelper.hasContent(iWidth) ) {
        imageWidth = Integer.parseInt(iWidth);
    }
    if ( FormatHelper.hasContent(iHeight) ) {
        imageHeight = Integer.parseInt(iHeight);
    }
    if(imageWidth == 0 && imageHeight == 0){
        imageWidth = 75;
    }

    String timestamp;
    if(isLatestIteration){
        timestamp = FormatHelper.format(request.getParameter("bomEffectiveDate"));
    }else{
        timestamp = FormatHelper.formatWithDateTimeFormat(wt.fc.PersistenceHelper.getModifyStamp(part));
    }

    // PULL SECTIONS MOA STRING FROM THE REQUEST TO SEE WHAT SECTIONS
    // WE SHOULD BE DISPLAYING. IF IT IS NULL, THEN I IMPLIES ALL
    String sectionMOA = request.getParameter("sections");
    Collection sectionList = MOAHelper.getMOACollection(sectionMOA);

%>
<%

    TableColumn column;
    Map<String, TableColumn> columnMap = new HashMap<String, TableColumn>();
    FlexTypeAttribute att;
    columns = new ArrayList();

    Iterator attIter = atts.iterator();
    while(attIter.hasNext()){
        att = (FlexTypeAttribute) attIter.next();
        if (!ACLHelper.hasViewAccess(att)) {
            continue;
        }
        column = flexg.createTableColumn(att);
        column.setDisplayed(true);
        if( "object_ref".equals(att.getAttVariableType()) ||
        "object_ref_list".equals(att.getAttVariableType())){
            column.setLinkTableIndex(att.getAttKey());
            column.setTableIndex(att.getAttKey() + "Display");
            column.setUseQuickInfo(true);
        }
        else{
            column.setTableIndex(att.getAttKey());
        }
        column.setHeaderLink(null);
        column.setDecimalPrecision(att.getAttDecimalFigures());
        columnMap.put("BOM." + att.getAttKey(), column);
        if ("integer".equals(att.getAttVariableType())) {
            column.setFormat(FormatHelper.INT_FORMAT_WITH_PARENTHESES);
        }
    }

    attIter = materialAtts.iterator();
    while(attIter.hasNext()){
        att = (FlexTypeAttribute) attIter.next();
        if (!ACLHelper.hasViewAccess(att)) {
            continue;
        }
        column = flexg.createTableColumn(att);
        if( "object_ref".equals(att.getAttVariableType()) ||
        "object_ref_list".equals(att.getAttVariableType())){
            column.setLinkTableIndex(att.getAttKey());
            column.setTableIndex(att.getAttKey() + "Display");
            column.setUseQuickInfo(true);
        }
        else{
            column.setTableIndex(att.getAttKey());
        }
        column.setHeaderLink(null);
        column.setOwnerRefIndex("MATERIALBRANCHID");
        column.setOwnerIdPrefix("VR:com.lcs.wc.material.LCSMaterial:");

        columnMap.put("MATERIAL." + att.getAttKey(), column);
    }

    attIter = materialSupplierAtts.iterator();
    while(attIter.hasNext()){
        att = (FlexTypeAttribute) attIter.next();
        if (!ACLHelper.hasViewAccess(att)) {
            continue;
        }
        column = flexg.createTableColumn(att);
        if( "object_ref".equals(att.getAttVariableType()) ||
        "object_ref_list".equals(att.getAttVariableType())){
            column.setLinkTableIndex(att.getAttKey());
            column.setTableIndex(att.getAttKey() + "Display");
            column.setUseQuickInfo(true);
        }
        else{
            column.setTableIndex(att.getAttKey());
        }
        column.setHeaderLink(null);
        column.setOwnerRefIndex("MATERIALSUPPLIERBRANCHID");
        column.setOwnerIdPrefix("VR:com.lcs.wc.material.LCSMaterialSupplier:");
        columnMap.put("MATERIAL." + att.getAttKey(), column);
    }

    attIter = supplierAtts.iterator();
    while(attIter.hasNext()){
        att = (FlexTypeAttribute) attIter.next();
        if (!ACLHelper.hasViewAccess(att)) {
            continue;
        }
        column = flexg.createTableColumn(att);
        if( "object_ref".equals(att.getAttVariableType()) ||
        "object_ref_list".equals(att.getAttVariableType())){
            column.setLinkTableIndex(att.getAttKey());
            column.setTableIndex(att.getAttKey() + "Display");
            column.setUseQuickInfo(true);
        }
        else{
            column.setTableIndex(att.getAttKey());
        }
        column.setHeaderLink(null);
        column.setOwnerRefIndex("SUPPLIERBRANCHID");
        column.setOwnerIdPrefix("VR:com.lcs.wc.supplier.LCSSupplier:");

        columnMap.put("SUPPLIER." + att.getAttKey(), column);
    }

    attIter = matColorAtts.iterator();
    while(attIter.hasNext()){
        att = (FlexTypeAttribute) attIter.next();
        if (!ACLHelper.hasViewAccess(att)) {
            continue;
        }
        column = flexg.createTableColumn(att);
        if( "object_ref".equals(att.getAttVariableType()) ||
        "object_ref_list".equals(att.getAttVariableType())){
            column.setLinkTableIndex(att.getAttKey());
            column.setTableIndex(att.getAttKey() + "Display");
            column.setUseQuickInfo(true);
        }
        else{
            column.setTableIndex(att.getAttKey());
        }
        column.setHeaderLink(null);
        column.setOwnerRefIndex("MATERIALCOLORID");
        column.setOwnerIdPrefix("OR:com.lcs.wc.material.LCSMaterialColor:");


        columnMap.put("MATERIAL COLOR." + att.getAttKey(), column);
    }

    attIter = colorAtts.iterator();
    while(attIter.hasNext()){
        att = (FlexTypeAttribute) attIter.next();
        if (!ACLHelper.hasViewAccess(att)) {
            continue;
        }
        column = flexg.createTableColumn(att);
        if( "object_ref".equals(att.getAttVariableType()) ||
        "object_ref_list".equals(att.getAttVariableType())){
            column.setLinkTableIndex(att.getAttKey());
            column.setTableIndex(att.getAttKey() + "Display");
            column.setUseQuickInfo(true);
        }
        else{
            column.setTableIndex(att.getAttKey());
        }
        column.setHeaderLink(null);
        column.setOwnerRefIndex("COLORID");
        column.setOwnerIdPrefix("OR:com.lcs.wc.color.LCSColor:");

        columnMap.put("COLOR." + att.getAttKey(), column);
    }

    FlexTypeAttribute matDescAtt = part.getFlexType().getAttribute("materialDescription");
    column = new BOMMaterialTableColumn();
    column.setDisplayed(true);
    column.setTableIndex("MATERIALNAME");
    if("LABOR".equals(part.getBomType())){
        column.setHeaderLabel(operationLabel);
    } else {
        column.setHeaderLabel(materialLabel);
    }
    column.setLinkMethod("viewMaterial");
    column.setUseQuickInfo(true);
    column.setLinkTableIndex("childId");
    column.setLinkMethodPrefix("OR:com.lcs.wc.material.LCSMaterialMaster:");
    column.setFormatHTML(true);
    ((BOMMaterialTableColumn)column).setDescriptionIndex("MATERIALDESCRIPTION");
    column.setWrapping(matDescAtt.isAttTableWrapable());
    if(matDescAtt.isAttTableWrapable()){
        column.setMinCharWidth(AbstractFlexTypeGenerator.getEffectiveMinCharWidth(matDescAtt, column));
    }
    columnMap.put("MATERIAL.name", column);

    column = new TableColumn();
    column.setDisplayed(true);
    column.setTableIndex("materialColorId");
    column.setHeaderLabel("MatCol");
    column.setFormatHTML(true);
    columnMap.put("MATERIALCOLOR.ID", column);


    FlexTypeAttribute supDescAtt = part.getFlexType().getAttribute("supplierDescription");
    column = new TableColumn();
    column.setDisplayed(true);
    column.setTableIndex("supplierName");
    column.setHeaderLabel(supplierLabel);
    column.setLinkMethod("viewObject");
    column.setUseQuickInfo(true);
    column.setLinkTableIndex("supplierMasterId");
    column.setFormat(FormatHelper.STRING_FORMAT);
    column.setLinkMethodPrefix("OR:com.lcs.wc.supplier.LCSSupplierMaster:");
    column.setWrapping(supDescAtt.isAttTableWrapable());
    if(supDescAtt.isAttTableWrapable()){
        column.setMinCharWidth(AbstractFlexTypeGenerator.getEffectiveMinCharWidth(supDescAtt, column));
    }
   columnMap.put("SUPPLIER.name", column);


    FlexTypeAttribute colorAtt = part.getFlexType().getAttribute("colorDescription");

    if(LCSFlexBOMQuery.ALL_SKUS.equals(skuMode)){
        String skuKey = "LCSSKU.IDA3MASTERREFERENCE";
        String skuNameIndex = productType.getAttribute("skuName").getSearchResultIndex();
        if(!"PRODUCT".equals(ownerMode)){
            skuKey = "LCSMATERIALCOLOR.IDA2A2";
            skuNameIndex = "LCSCOLOR.COLORNAME";
        }
        Iterator skuIter = skuList.iterator();
        FlexObject skuObj;
        while(skuIter.hasNext()){
            skuObj = (FlexObject) skuIter.next();
            BOMColorTableColumn colorColumn = new BOMColorTableColumn();
            colorColumn.setDisplayed(true);
            colorColumn.setTableIndex("colorName$sku$" + skuObj.getString(skuKey));
            colorColumn.setSecondaryTableIndex("colorName");
            colorColumn.setDescriptionIndex("colorDescription$sku$" + skuObj.getString(skuKey));
            colorColumn.setSecondaryDescriptionIndex("colorDescription");
            colorColumn.setHeaderLabel(skuObj.getString(skuNameIndex));
            colorColumn.setLinkMethod("viewColor");
            colorColumn.setUseQuickInfo(true);
            colorColumn.setLinkTableIndex("colorId$sku$" + skuObj.getString(skuKey));
            colorColumn.setSecondaryLinkTableIndex("colorId");
            colorColumn.setLinkMethodPrefix("OR:com.lcs.wc.color.LCSColor:");
            colorColumn.setColumnWidth("1%");
            colorColumn.setWrapping(false);
            colorColumn.setBgColorIndex("colorHexColor$sku$" + skuObj.getString(skuKey));
            colorColumn.setSecondaryBgColorIndex("colorHexColor");
            colorColumn.setUseColorCell(true);
            colorColumn.setAlign("center");
            colorColumn.setImageIndex("colorThumbnail$sku$" + skuObj.getString(skuKey));
            colorColumn.setSecondaryImageIndex("colorThumbnail");
            colorColumn.setWrapping(colorAtt.isAttTableWrapable());
            if(colorAtt.isAttTableWrapable()){
                colorColumn.setMinCharWidth(AbstractFlexTypeGenerator.getEffectiveMinCharWidth(colorAtt, colorColumn));
            }

            columnMap.put("BOM.color$sku$" + skuObj.getString(skuKey), colorColumn);
        }

    } else {
        BOMColorTableColumn colorColumn = new BOMColorTableColumn();
        colorColumn.setDisplayed(true);
        colorColumn.setTableIndex("colorName");
        colorColumn.setDescriptionIndex("colorDescription");
        colorColumn.setHeaderLabel(colorLabel);
        colorColumn.setLinkMethod("viewColor");
        colorColumn.setUseQuickInfo(true);
        colorColumn.setLinkTableIndex("colorId");
        colorColumn.setLinkMethodPrefix("OR:com.lcs.wc.color.LCSColor:");
        colorColumn.setColumnWidth("1%");
        colorColumn.setWrapping(false);
        colorColumn.setBgColorIndex("colorHexColor");
        colorColumn.setUseColorCell(true);
        colorColumn.setImageIndex("colorThumbnail");
        colorColumn.setWrapping(colorAtt.isAttTableWrapable());
        if(colorAtt.isAttTableWrapable()){
            colorColumn.setMinCharWidth(AbstractFlexTypeGenerator.getEffectiveMinCharWidth(colorAtt, colorColumn));
        }
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
    if(imageWidth > 0){
        column.setImageWidth(imageWidth);
    }
    if(imageHeight > 0){
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
    column.setShowCriteriaNumericCompare(true);

    column = (TableColumn)columnMap.get("BOM.partName");

    FlexTypeAttribute partNameAtt = part.getFlexType().getAttribute("partName");
    BOMPartNameTableColumn columnt = new BOMPartNameTableColumn();
    columnt.setHeaderLabel(column.getHeaderLabel());
    columnt.setTableIndex(column.getTableIndex());
    columnt.setDisplayed(column.isDisplayed());
    columnt.setSubComponetIndex("masterBranchId");
    columnt.setComplexMaterialIndex("masterBranch");
    columnt.setLinkedBOMIndex("linkedBOM");
    columnt.setWrapping(false);
    if (column.getList() != null) {
        columnt.setList(column.getList());
        columnt.setAttributeType(column.getAttributeType());
    }
    columnt.setWrapping(partNameAtt.isAttTableWrapable());
    if(partNameAtt.isAttTableWrapable()){
        columnt.setMinCharWidth(AbstractFlexTypeGenerator.getEffectiveMinCharWidth(partNameAtt, columnt));
    }

    columnMap.put("BOM.partName", columnt);


    if(WCPART_ENABLED){
        column = new TableColumn();
        column.setDisplayed(true);
        column.setTableIndex("WCPARTNAME");
        column.setHeaderLabel(type.getAttribute("wcPartName").getAttDisplay());
        column.setLinkMethod("viewPart");
        column.setLinkTableIndex("WCPARTID");
        column.setLinkMethodPrefix("VR:wt.part.WTPart:");
        columnMap.put("BOM.wcPartName", column);
    }
%>
<jsp:include page="<%=subURLFolder+ VIEW_BOM_COLUMN_ORDER %>" flush="true">
    <jsp:param name="useBOMCosting" value="<%= COSTING %>" />
    <jsp:param name="ownerMode" value="<%= ownerMode %>" />
    <jsp:param name="viewBOMMode" value="VIEW" />
    <jsp:param name="skuMode" value="<%= skuMode %>" />
    <jsp:param name="oid" value="<%= partOid %>" />
</jsp:include>
<%




    Iterator columnKeys = columnList.iterator();
    String columnKey = "";
    ColumnList view = null;
    String stViewId = request.getParameter("viewId");
    String bomId = (String)request.getAttribute("BOMoid");
    FlexBOMPart bom = (FlexBOMPart)LCSQuery.findObjectById(bomId);
    FlexType bomType = bom.getFlexType();
    boolean viewPrimaryMaterialGroup = ACLHelper.hasViewAccess(bomType.getAttributeGroupObject(PRIMARY_MATERIAL_GROUP, false));

    if(request.getAttribute("viewIdOverride") != null){
        stViewId = (String) request.getAttribute("viewIdOverride");
    }

    view = lcsContext.getViewCache().getColumnList(stViewId, bomType, "EDIT_BOM");

    boolean showDiscreetCounts = false;
    boolean showCounts = false ;
    boolean showGrandTotals = false;
    if(view != null)
    {
    showCounts = view.isShowCounts();
    showGrandTotals = view.isShowGrandTotals();
    }
    while(columnKeys.hasNext()){
        columnKey = (String) columnKeys.next();

        if ("MATERIAL".equals(ownerMode) && columnKey.equals("BOM.wcPartName")){
        continue;
        }

        column = (TableColumn) columnMap.get(columnKey);
        if(column != null){
            if (columnKey.equals("BOM.partName")) {
                column.setFormatHTML(true);
                //column.setWrapping(true);
            }
            if (!columnKey.equals("BOM.color")) {
                column.setColumnClassIndex("highLight");
            }
            if(view != null)
            {
                //Set whether or not to total the colum
                if(view.isShowTotalColum(columnKey)){
                    column.setTotal(true);
                    column.setSubTotal(true);
                }

                //Set value in order for discreet counts for the column
                if(view.isShowCountColumn(columnKey)){
                    column.setDiscreteCount(true);
                    column.setIncludeEmptyInCount(false);
                    column.setDiscreteLabel(column.getHeaderLabel() + ": ");

                    showDiscreetCounts = true;
                }
            }
            columns.add(column);
        }

    }
    if (view!=null)
    {
    tg.setShowDiscreteRows(showDiscreetCounts && showCounts);
    tg.setShowTotals(showGrandTotals);
    }
    Collection usedAttKeys = getUsedAttList(columnList);
    if(!(usedAttKeys.contains("materialDescription"))){
        usedAttKeys.add("materialDescription");
    }
    if(!(usedAttKeys.contains("colorDescription"))){
        usedAttKeys.add("colorDescription");
    }


    // ================================================================
    // IMPORTANT- THESE ATTRIBUTES MUST BE INCLUDED IN THE QUERY RESULT
    // OR THE TOTAL CALCULATED ON THE PAGE WILL BE INCONSISTENT
    // ================================================================
    if(!(usedAttKeys.contains("materialPrice"))){
        usedAttKeys.add("materialPrice");
    }
/// VRD EXTENSION START: Configuration for pricing calculations //materialColorPrice
   /** if(!(usedAttKeys.contains(COLORSPECIFICPRICE))){
        usedAttKeys.add(COLORSPECIFICPRICE);
    }**/
    if(!(usedAttKeys.contains("materialColorPrice"))){
        usedAttKeys.add("materialColorPrice");
    }
    if(!(usedAttKeys.contains(MATERIALQUANTITY))){
        usedAttKeys.add(MATERIALQUANTITY);
    }
    if(!(usedAttKeys.contains(LOSSADJUSTMENT))){
        usedAttKeys.add(LOSSADJUSTMENT);
    }
    if(!(usedAttKeys.contains(PRICEOVERRIDE))){
        usedAttKeys.add(PRICEOVERRIDE);
/// VRD EXTENSION END
    }
    // ================================================================



    boolean multiLevel = USE_MULTILEVEL;


        if(logger.isDebugEnabled()){
        logger.debug("skuMasterId:" + skuMasterId);
        logger.debug("sourceDimId:" + sourceDimId);
        logger.debug("size1:" + size1);
        logger.debug("size2:" + size2);
        logger.debug("destinationId:" + destinationId);
        logger.debug("skuMode:" + skuMode);
        logger.debug("timestamp:" + timestamp);
        logger.debug("multiLevel:" + multiLevel);
        logger.debug("usedAttKeys:" + usedAttKeys);
        }


    Collection bomData = new ArrayList();
    try{
    bomData = LCSFindFlexBOMHelper.findBOM(part, skuMasterId, sourceDimId, size1, size2, destinationId, skuMode, timestamp, new Boolean(multiLevel), materialType, usedAttKeys);
    }catch(Throwable t){
        t.printStackTrace();
    }
    bomData = groupDataToBranchId(bomData, type);
    //logger.debug("--------------------------------------------------------");
    //logger.debug("bomData:  " + bomData);
    //logger.debug("--------------------------------------------------------");


    Map complexHeaderMap = extractComplexMaterialHeaderRecords(bomData);

    // IF OUR SECTIONS LIST HAS DATA, THEN WE FILTER THE RESULTS
    // SO THAT WE ONLY HAVE THOSE SECTIONS SPECIFIED.
    if(sectionList.size() > 0){
        Collection filteredResults = new ArrayList();
        Iterator sectionFilterIter = sectionList.iterator();
        while(sectionFilterIter.hasNext()){
            String section = (String) sectionFilterIter.next();
            filteredResults.addAll(TableDataUtil.filterBasedOnValue(bomData, "section", section));

        }
        bomData = filteredResults;
    }

    // CALCULATION OF TOTALS.
    Iterator bomIter = bomData.iterator();
    Iterator bomIter2 = bomData.iterator();
    TableData branch = null;
    double materialPrice;
    BigDecimal bomTotalSams = new BigDecimal("0.0");
    BigDecimal bomTotal = new BigDecimal("0.0");
    BigDecimal rtBd = null;
    BigDecimal mpbd = null;
    BigDecimal mcpbd = null;
    BigDecimal pobd =null;
    BigDecimal qbd = null;
    BigDecimal labd = null;
    BigDecimal samsbd = null;
    BigDecimal bdzero = new BigDecimal("0.0");
	//changes to calculate rowTotal with new formula - start
	BigDecimal cifbd = null;
	BigDecimal grossQtybd = null;
	BigDecimal calcPercent = null;
	BigDecimal qbdOrig = null;
	BigDecimal mpdOrig = null;
	//changes to calculate rowTotal with new formula - end
    String materialName;
    String flexBomLinkID;
    String materialSupplierMasterId ="";
    String materialColorId;
/// VRD EXTENSION START: Remove striked rows
    String markUp;
/// VRD EXTENSION END

    Collection matSups = new ArrayList();
    Collection matSupColors = new ArrayList();

    Map matSup= null;
    Map matSupColor= null;

//   Make sure all attributes used in row calculations and totaling are pulled back when retrieving bom data
//   add priceOverride value to the dataset being pulled back
    while(bomIter.hasNext()){
        branch = (TableData) bomIter.next();
        matSupColor = new HashMap();
        matSup = new HashMap();

        // get the materialPrice from the branch  (attribute on LCSMaterialSupplier)
        if(FormatHelper.hasContent(branch.getData("materialPrice"))){
            mpbd = new BigDecimal(branch.getData("materialPrice"));
        }
        else{
            mpbd = new BigDecimal("0.0");
        }

        // get the materialColorPrice from the branch  (attribute on LCSMaterialColor (legacy attribute)
        if(FormatHelper.hasContent(branch.getData("materialColorPrice"))){
            mcpbd = new BigDecimal(branch.getData("materialColorPrice"));
        }
        else{
            mcpbd = new BigDecimal("0.0");
        }

        // get the priceOverride from the branch  (attribute on LCSFlexBOMLink)
        if(FormatHelper.hasContent(branch.getData("priceOverride"))){
            pobd = new BigDecimal(branch.getData("priceOverride"));
        }
        else{
            pobd = new BigDecimal("0.0");
        }

        // get the quantity from the branch  (attribute on LCSFlexBOMLink)
        if(FormatHelper.hasContent(branch.getData("quantity"))){
            qbd = new BigDecimal(branch.getData("quantity"));
        }
        else{
            qbd = new BigDecimal("0.0");
        }

        // get the lossAdjustment from the branch  (attribute on LCSFlexBOMLink)
        if(FormatHelper.hasContent(branch.getData("lossAdjustment"))){
            labd = new BigDecimal(branch.getData("lossAdjustment"));
        }
        else{
            labd = new BigDecimal("0.0");
        }

        // get the sams from the branch (used for BOL, not sure what sams stands for)
        if(FormatHelper.hasContent(branch.getData("sams") )){
            samsbd = new BigDecimal(branch.getData("sams"));
        }
        else{
            samsbd = new BigDecimal("0.0");
        }


        flexBomLinkID = "OR:com.lcs.wc.flexbom.FlexBOMLink:" +FormatHelper.format(branch.getData("flexBomLinkId"));
        materialColorId = FormatHelper.format(branch.getData("materialColorId"));
        materialSupplierMasterId = FormatHelper.format(branch.getData("materialSupplierMasterId"));
/// VRD EXTENSION START: Remove striked rows
        markUp = FormatHelper.format(branch.getData(MARKUP));
/// VRD EXTENSION END

        matSup.put("materialSupplierMasterId", materialSupplierMasterId);

        matSupColor.put("materialSupplierMasterId", materialSupplierMasterId);
        matSupColor.put("materialColorId", materialColorId);

        matSups.add(matSup);
        matSupColors.add(matSupColor);


        // Override material price if materialColorPrice is available
	//changes to calculate rowTotal with new formula - start
       /* if(mcpbd.compareTo(bdzero) == 1){
            mpbd = mcpbd;
        }
		*/
	//changes to calculate rowTotal with new formula - end
        // Override again if user specified an Override Price in the BOM itself
        if(pobd.compareTo(bdzero) == 1){
            mpbd = pobd;
        }

        // Calculate the loss adjustment to increase quantity  q = q + ( q * la )
        if(labd.compareTo(bdzero) != 0){
            BigDecimal tempbd = qbd.multiply(labd);
            qbd = qbd.add(tempbd);
        }

        // The row total is then quantity * price
        rtBd = qbd.multiply(mpbd);
	//changes to calculate rowTotal with new formula - start	
		if(FormatHelper.hasContent(branch.getData("scCIF"))){
            cifbd = new BigDecimal(branch.getData("scCIF"));
        }
        else{
            cifbd = new BigDecimal("0.0");
        }
		
		if(FormatHelper.hasContent(branch.getData("scCalculationSizePercent"))){
            calcPercent = new BigDecimal(branch.getData("scCalculationSizePercent"));
        }
        else{
            calcPercent = new BigDecimal("0.0");
        }	
		if(FormatHelper.hasContent(branch.getData("scGrossQuantity"))){
            grossQtybd = new BigDecimal(branch.getData("scGrossQuantity"));
        }
        else{
            grossQtybd = new BigDecimal("0.0");
        }
		rtBd = mpbd.add(cifbd).multiply(grossQtybd);

        materialName = FormatHelper.format(branch.getData("name"));

/// VRD EXTENSION START: Remove striked rows
        if(materialName.indexOf("--") < 0 && (!hasParentPrice(branch.getData("masterBranchId"), complexHeaderMap) || branch.getData("branchId").indexOf("-") < 0) && !"delete".equals(markUp)){
/// VRD EXTENSION END
           /* if(FormatHelper.hasContent(branch.getData("masterBranchId"))){
                FlexObject parent = (FlexObject)complexHeaderMap.get(branch.getData("masterBranchId"));
                if(parent != null && FormatHelper.hasContentAllowZero(parent.getData("quantity"))){
                    BigDecimal pqbd = new BigDecimal(parent.getData("quantity"));
                    rtBd = pqbd.multiply(rtBd);
                }
            } */
//changes to calculate rowTotal with new formula - end
            bomTotal = bomTotal.add(rtBd);
            bomTotalSams = bomTotalSams.add(samsbd);
            branch.setData("rowTotal", rtBd.toString());
        } else {
            branch.setData("rowTotal", rtBd.toString());
        }
    }

    // To calculate pricing if USE_BOMDATEPRICING is true
    if (FormatHelper.hasContent(pricingDateDateString) && matSupColors.size() > 0){
        Date reqDate= null;
        bomTotal = new BigDecimal("0.0");
        bomTotalSams = new BigDecimal("0.0");

        try{
            if(pricingDateDateString.indexOf(" ") > 0){
                // MUST BE TIME FORMAT
                Locale locale = Locale.US;
                try {
                    locale = com.lcs.wc.client.ClientContext.getContext().getLocale();
                }
                catch (WTException wte) {
                    locale = Locale.US;
                }
                reqDate = FormatHelper.constructTimestamp(pricingDateDateString+" GMT",wt.util.WTMessage.getLocalizedMessage(RB.DATETIMEFORMAT, "LONG_STANDARD_DATETIME_FORMAT", RB.objA, locale),true);
            } else {
                reqDate = FormatHelper.parseDateIgnoreTZ(pricingDateDateString);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        MaterialPriceList mpl =  new MaterialPriceList(matSups, matSupColors, reqDate);


        while(bomIter2.hasNext()){
            branch = (TableData) bomIter2.next();

            // get the materialColorPrice from the branch  (attribute on LCSMaterialColor (legacy attribute)
/// VRD EXTENSION START: Configuration for pricing calculations
			/**if(FormatHelper.hasContent(branch.getData(COLORSPECIFICPRICE))){
				mcpbd = new BigDecimal(branch.getData(COLORSPECIFICPRICE));**/
			if(FormatHelper.hasContent(branch.getData("materialColorPrice"))){
            	mcpbd = new BigDecimal(branch.getData("materialColorPrice"));
        
				
/// VRD EXTENSION END
            }
            else{
                mcpbd = new BigDecimal("0.0");
            }

            // get the priceOverride from the branch  (attribute on LCSFlexBOMLink)
/// VRD EXTENSION START: Configuration for pricing calculations
			if(FormatHelper.hasContent(branch.getData(PRICEOVERRIDE))){
				pobd = new BigDecimal(branch.getData(PRICEOVERRIDE));
/// VRD EXTENSION END
            }
            else{
                pobd = new BigDecimal("0.0");
            }

            // get the quantity from the branch  (attribute on LCSFlexBOMLink)
/// VRD EXTENSION START: Configuration for pricing calculations
			if(FormatHelper.hasContent(branch.getData(MATERIALQUANTITY))){
				qbd = new BigDecimal(branch.getData(MATERIALQUANTITY));
/// VRD EXTENSION END
            }
            else{
                qbd = new BigDecimal("0.0");
            }

            // get the lossAdjustment from the branch  (attribute on LCSFlexBOMLink)
/// VRD EXTENSION START: Configuration for pricing calculations
			if(FormatHelper.hasContent(branch.getData(LOSSADJUSTMENT))){
				labd = new BigDecimal(branch.getData(LOSSADJUSTMENT));
/// VRD EXTENSION END
            }
            else{
                labd = new BigDecimal("0.0");
            }

            // get the sams from the branch (used for BOL, not sure what sams stands for)
            if(FormatHelper.hasContent(branch.getData("sams"))){
                samsbd = new BigDecimal(branch.getData("sams"));
            }
            else{
                samsbd = new BigDecimal("0.0");
            }

            materialColorId = FormatHelper.format(branch.getData("materialColorId"));
            materialSupplierMasterId = FormatHelper.format(branch.getData("materialSupplierMasterId"));
/// VRD EXTENSION START: Remove striked rows
        	markUp = FormatHelper.format(branch.getData(MARKUP));
/// VRD EXTENSION END

            // use the materialPriceList to get the price for a specific material/supplier/color
            materialPrice = mpl.getPrice(materialSupplierMasterId, materialColorId);
            // convert the materialPrice to a BigDecimal
            mpbd = new BigDecimal("" + materialPrice);


            // Override material pricelist price if materialColorPrice is available
          //changes to calculate rowTotal with new formula - start
	   /* if(mcpbd.compareTo(bdzero) == 1){
                mpbd = mcpbd;
            }
			*/
	//changes to calculate rowTotal with new formula - end
            // Calculate the loss adjustment to increase quantity  q = q + ( q * la )
            if(labd.compareTo(bdzero) != 0 ){
                BigDecimal tempbd = qbd.multiply(labd);
                qbd = qbd.add(tempbd);
            }

            branch.setData("materialPrice", "" + mpbd);

            // MOVED THIS CODE BELOW THE SETDATA CALL BECAUSE WHILE WE
            // WANT MATERIAL PRICE TO BE CHANGED FOR TOTAL ROLL UP,
            // WE DON"T WANT IT CHANGED IN THE TABLE's COLUMN.
            if(pobd.compareTo(bdzero) == 1){
               mpbd = pobd;
            }

            rtBd = qbd.multiply(mpbd);
			
			if(FormatHelper.hasContent(branch.getData("scCIF"))){
            cifbd = new BigDecimal(branch.getData("scCIF"));
        }
        else{
            cifbd = new BigDecimal("0.0");
        }
	
		
		
		if(FormatHelper.hasContent(branch.getData("scCalculationSizePercent"))){
            calcPercent = new BigDecimal(branch.getData("scCalculationSizePercent"));
        }
        else{
            calcPercent = new BigDecimal("0.0");
        }	
		if(FormatHelper.hasContent(branch.getData("scGrossQuantity"))){
            grossQtybd = new BigDecimal(branch.getData("scGrossQuantity"));
        }
        else{
            grossQtybd = new BigDecimal("0.0");
        }
		

		rtBd = mpbd.add(cifbd).multiply(grossQtybd);

/// VRD EXTENSION START: Remove striked rows
            materialName = FormatHelper.format(branch.getData("name"));
            if(materialName.indexOf("--") < 0 && (!hasParentPrice(branch.getData("masterBranchId"), complexHeaderMap) || branch.getData("branchId").indexOf("-") < 0) && !"delete".equals(markUp)){
              /*  if(FormatHelper.hasContent(branch.getData("masterBranchId"))){
                    FlexObject parent = (FlexObject)complexHeaderMap.get(branch.getData("masterBranchId"));
                    if(parent != null && FormatHelper.hasContentAllowZero(parent.getData("quantity"))){
                        BigDecimal pqbd = new BigDecimal(parent.getData("quantity"));
                        rtBd = pqbd.multiply(rtBd);
                    }
                } */
	    //changes to calculate rowTotal with new formula - end
                bomTotal = bomTotal.add(rtBd);
                bomTotalSams = bomTotalSams.add(samsbd);
                branch.setData("rowTotal", rtBd.toString());
            } else {
                branch.setData("rowTotal", "");
/// VRD EXTENSION END
            }
        }
    }

    NmURLFactoryBean urlFactoryBean = new NmURLFactoryBean();
    HashMap urlParam = new HashMap();
    String partLink = NetmarketURL.convertToShellURL(NetmarketURL.buildURL(urlFactoryBean, "tcomp", "infoPage", null, urlParam, true, new NmAction()));
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JSP METHODS /////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>


<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JAVSCRIPT ///////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<script>
    //Fix SPR#2047102 02/12/2011
    function runToView(val) {
        document.MAINFORM.oid.value = '<%= FormatHelper.encodeForJavascript(oid) %>';
        if (document.MAINFORM.oid.value.indexOf("MaterialSupplier") > -1 || document.MAINFORM.oid.value.indexOf("MaterialColor") > -1 ) {
            document.MAINFORM.activity.value = 'VIEW_MATERIAL_SUPPLIER';
        }else if (document.MAINFORM.oid.value.indexOf("Material") > -1 ) {
            document.MAINFORM.activity.value = 'VIEW_MATERIAL';
        } else {
            document.MAINFORM.activity.value = 'VIEW_SEASON_PRODUCT_LINK';
        }
        document.MAINFORM.action.value = 'INIT';
        if(document.MAINFORM.bomId){
            <% if(isLatestIteration){ %>
                document.getElementById('bomId').value = val;

            <% }else{ %>
                document.getElementById('bomId').options[document.getElementById('bomId').length] = new Option('', '');
                document.getElementById('bomId').value = '';
            <%} %>
        }
        if(document.MAINFORM.bomPartId){
            document.MAINFORM.bomPartId.value = val;
        }
        submitForm();
    }

    function toggleThumbs(val, bId) {
        //Fix SPR#2047102 02/12/2011
        document.MAINFORM.oid.value = '<%= FormatHelper.encodeForJavascript(oid) %>';
        if (document.MAINFORM.oid.value.indexOf("MaterialSupplier") > -1 || document.MAINFORM.oid.value.indexOf("MaterialColor") > -1 ) {
            document.MAINFORM.activity.value = 'VIEW_MATERIAL_SUPPLIER';
        }else if (document.MAINFORM.oid.value.indexOf("Material") > -1 ) {
            document.MAINFORM.activity.value = 'VIEW_MATERIAL';
        } else {
            document.MAINFORM.activity.value = 'VIEW_SEASON_PRODUCT_LINK';
        }
        document.MAINFORM.action.value = 'INIT';
        document.MAINFORM.showThumbs.value = val;
        if(document.MAINFORM.bomId){
            document.MAINFORM.bomId.value = bId;
        }
        if(document.MAINFORM.bomPartId){
            document.MAINFORM.bomPartId.value = bId;
        }
        submitForm();
    }

    function viewPart(oid){
        var partLink = "<%=partLink%>?oid=" + oid;
        openWindow(partLink);

    }
</script>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// HTML ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<input type="hidden" name="showThumb" value="<%= FormatHelper.encodeAndFormatForHTMLContent(showThumb) %>" >
    <%
        Collection groupByColumns = new ArrayList();
        groupByColumns.add(columnMap.get("BOM.section"));
        tg.setGroupByColumns(groupByColumns);
        tg.setSpaceBetweenGroups(false);
        if (columns != null)
        {
          request.setAttribute("columns", columns);
          request.setAttribute("groupByColumns", groupByColumns);
        }
/// VRD EXTENSION START: Totals for Vendor
		if(!ClientContext.getContext().isVendor ||(ClientContext.getContext().isVendor && SECTIONTOTAL_ENABLED_FORVENDOR)){
			column=(TableColumn)columnMap.get("BOM.rowTotal");
			column.setTotal(true);
			tg.setShowTotals(true);
			tg.setShowSubTotals(true);
		}
/// VRD EXTENSION END
      if (bomData != null)
        {
          request.setAttribute("data", bomData);
        }
    %>
    <jsp:include page="<%=subURLFolder+ EXCEL_GENERATOR_SUPPORT_PLUGIN %>" flush="true">
       <jsp:param name="csv" value="<%= csv %>" />
       <jsp:param name="reportDisplayName" value="ViewBom" />
       <jsp:param name="reportName" value="ViewBom" />
       <jsp:param name="skipIconRendering" value="true"/>
    </jsp:include>
    <%=
        tg.drawTable(bomData, columns, "", false)
    %>
       <% if(!billOfLabor) {
           column=(TableColumn)columnMap.get("BOM.rowTotal");%>
           <div class="grand-total">
               <%= totalWithEqualSignLabel + FormatHelper.formatCurrency(bomTotal.doubleValue(), column.getDecimalPrecision(), currCode) %>
           </div>

       <% } %>
<script>
    <%
    if(viewPrimaryMaterialGroup) {
        FlexTypeAttribute primaryMaterialAtt = bomType.getAttribute("primaryMaterial");
        if (primaryMaterialAtt != null) {
            LCSMaterialSupplier materialSupplier = (LCSMaterialSupplier) primaryMaterialAtt.getValue(bom);
            if (materialSupplier != null) {
                String primaryMaterialDisplay = flexg.drawDisplay(primaryMaterialAtt, bom, false, "", null);
                if (FormatHelper.hasContent(primaryMaterialDisplay)) {
                    primaryMaterialDisplay = primaryMaterialDisplay.replace("\"", "\\\"");
                    primaryMaterialDisplay = primaryMaterialDisplay.replace("\n", "");
                    primaryMaterialDisplay = primaryMaterialDisplay.replaceFirst("'\\)", "', '', '', 'tabPage=1')");
    %>
                    var pmDiv = document.getElementById('pmDescription');
                    if (pmDiv) {
                       pmDiv.innerHTML = "<%=primaryMaterialDisplay%>";
                    }
    <%          }
            }
        }
    }
    %>
</script>
<%-- START - FP-196 : Able to Generate Tech Pack from BOM page --%>
<script>
disableGenerateTechPack();



function disableGenerateTechPack(){
    var element = document.getElementById("genrateTechPack");
    if(element != null ){
        element.style.display = "none";
    }
}



</script>
<%-- END - FP-196 : Able to Generate Tech Pack from BOM page --%>