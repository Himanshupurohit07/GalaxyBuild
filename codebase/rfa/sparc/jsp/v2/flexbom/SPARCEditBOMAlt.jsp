<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page import="java.util.stream.Collectors"%>
<%@page import="com.lcs.wc.client.web.html.ActionLink"%>
<%@page import="com.lcs.wc.client.web.html.DropdownMenuIcon"%>
<%@page import="com.lcs.wc.client.web.html.DefaultIconButtonRenderer"%>
<%@page import="com.lcs.wc.client.web.html.IconButtonRenderer"%>
<%@page language="java"
       import=" com.lcs.wc.client.*,
                com.lcs.wc.client.web.*,
                com.lcs.wc.db.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.flexbom.*,
                com.lcs.wc.foundation.*,
                com.lcs.wc.material.*,
                com.lcs.wc.specification.*,
                com.lcs.wc.part.*,
                com.lcs.wc.product.*,
                com.lcs.wc.sizing.ProductSizeCategory,
                com.lcs.wc.sizing.FullSizeRange,
                com.lcs.wc.sizing.SizingQuery,
                com.lcs.wc.material.*,
                com.lcs.wc.report.*,
                com.lcs.wc.season.*,
                com.lcs.wc.sourcing.*,
                com.lcs.wc.supplier.*,
                com.lcs.wc.util.*,
                org.apache.logging.log4j.Logger,
                org.apache.logging.log4j.LogManager,
                com.lcs.wc.color.LCSPalette,
                com.lcs.wc.color.LCSPaletteQuery,
                wt.util.*,
                wt.enterprise.*,
                wt.fc.*,
                wt.session.SessionServerHelper,
                java.text.*,
                com.lcs.wc.client.web.html.JavascriptFunctionCall,
                com.lcs.wc.client.web.html.JavascriptFunctionCall.Argument,
                com.infoengine.object.factory.*,
                java.util.*"
%>
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca"%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="type" scope="request" class="com.lcs.wc.flextype.FlexType" />
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="flexg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="columnList" scope="request" class="java.util.ArrayList" />
<jsp:useBean id="appContext" class="com.lcs.wc.client.ApplicationContext" scope="session"/>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="wtcontext" class="wt.httpgw.WTContextBean" scope="request"/>
<jsp:setProperty name="wtcontext" property="request" value="<%=request%>"/>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%!
    private static final Logger logger = LogManager.getLogger("rfa.jsp.flexbom.EditBOMAlt");
    public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");
    public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
    public static final String WT_IMAGE_LOCATION = LCSProperties.get("flexPLM.windchill.ImageLocation");
    public static final String WT_CLIENT_IMAGE_LOCATION = LCSProperties.get("flexPLM.windchill.ClientImageLocation");
    public static final String JSPNAME = "BOMEditor";
    public static final boolean COSTING = LCSProperties.getBoolean("jsp.flexbom.costedBOM");
    public static final int TIMEOUT_THRESHHOLD = LCSProperties.get("jsp.flexbom.timeoutThreshhold", 40000);
    public static final int DIM_ROW_THRESHHOLD = LCSProperties.get("jsp.flexbom.dimensionExpandCountThreshhold", 30);
    public static final boolean USE_AUTOMATIC_BOM_HIGHLIGHT = LCSProperties.getBoolean("jsp.flexbom.automaticBOMHighlight");
    public static final boolean USE_AUTOMATIC_BOL_HIGHLIGHT = LCSProperties.getBoolean("jsp.flexbom.automaticBOLHighlight");
    public static final int SPEC_REPORT_WINDOW_SIZE = LCSProperties.get("jsp.component.SpecReportWindowSize", 600);
    public static final String SPEC_REPORT_IMG = LCSProperties.get("jsp.component.SpecReportImage", "hotlist.gif");
    public static final String SYNCH_REPORT_IMG = LCSProperties.get("jsp.component.SyncReportImage", "server_connected_and_synhronized.png");
    public static final String MATERIAL_QUICK_SEARCH_ATTRIBUTE_MAP = LCSProperties.get("jsp.flexbom.EditBOM.materialQuickSearchAttributePrefixMapping", "DEFAULT:name");
    public static final String COLOR_QUICK_SEARCH_ATTRIBUTE_MAP = LCSProperties.get("jsp.flexbom.EditBOM.colorQuickSearchAttributePrefixMapping", "DEFAULT:name");
    public static final String VIEW_BOM_COLUMN_ORDER = PageManager.getPageURL("VIEW_BOM_COLUMN_ORDER", null);
    public static final String EDIT_BOM_HEADER = PageManager.getPageURL("EDIT_BOM_HEADER", null);
    public static final String BOM_VIEW_PLUGIN = PageManager.getPageURL("PRODUCT_BOM_VIEW_PLUGIN", null);
    public static final String STANDARD_TEMPLATE_HEADER = PageManager.getPageURL("STANDARD_TEMPLATE_HEADER", null);
    public static final String STANDARD_TEMPLATE_FOOTER = PageManager.getPageURL("STANDARD_TEMPLATE_FOOTER", null);
    public static final boolean USE_PRODUCTDESTINATIONS = LCSProperties.getBoolean("com.lcs.wc.product.useProductDestinations");
    public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");
    public static final String AJAX_SEARCH_PREFIX = LCSProperties.get("jsp.flexbom.EditBOM.materialQuickSearchPrefix", ":");
    public static final boolean USE_COLLAPSE_BUTTON = LCSProperties.getBoolean("jsp.flexbom.useCollapseButton");
    public static final String CLIENT_SIDE_PLUGIN = PageManager.getPageURL("CLIENT_SIDE_PLUGINS", null);
    public static final String OBJECT_THUMBNAIL_PLUGIN= PageManager.getPageURL("OBJECT_THUMBNAIL_PLUGIN", null);
    public static final String BOM_AJAX_UTILITY = PageManager.getPageURL("BOM_AJAX_UTILITY", null);
    public static final boolean USE_AJAX_SAVE = LCSProperties.getBoolean("jsp.flexbom.useAjaxSave");
    public static String MATERIAL_SUPPLIER_ROOT_TYPE = LCSProperties.get("com.lcs.wc.supplier.MaterialSupplierRootType", "Supplier");
    public static final boolean WCPART_ENABLED = LCSProperties.getBoolean("com.lcs.wc.specification.parts.Enabled");
    public static final String PRIMARY_MATERIAL_GROUP = LCSProperties.get("com.lcs.wc.flexbom.PrimaryMaterialGroup", "Primary Material");
    public static final String stickyProductHeaderImageWidth = LCSProperties.get("jsp.image.StickyProductHeader.imageWidth", "300");
    public static final boolean BOM_HEADER_DEFAULT_COLLAPSE = LCSProperties.getBoolean("jsp.flexbom.bomHeaderDefaultCollapse");
    public static final boolean CASE_INSENSITIVE = LCSProperties.getBoolean("jsp.flexbom.caseInsensitive");
    public static final boolean USE_SIZING = LCSProperties.getBoolean("com.lcs.wc.product.useSizing");
    public static final String VIEW_DELIM = "~";
    public static String instance = "";
    public static String hostUrl = "";
    public static String codebaseUrl = "";
    public static final String RANGE_DELIM = "|---|";
    String noneSelectedLabel = WTMessage.getLocalizedMessage(RB.SEASON, "noneSelected_LBL", RB.objA);
    public static final boolean ENABLE_DEBUG_SECTION = LCSProperties.getBoolean("jsp.flexbom.EditBOMAlt.enableDebugSection");

    public static final String MORE_ACTIONS_ICON_URL = URL_CONTEXT + "/images/icon_group.svg";

    static {
        try {

            instance = wt.util.WTProperties.getLocalProperties().getProperty ("wt.federation.ie.VMName");
            WTProperties wtproperties = WTProperties.getLocalProperties();
            codebaseUrl = wtproperties.getProperty("wt.server.codebase","");
            hostUrl = wtproperties.getProperty("wt.server.codebase","") + "/..";
        } catch(Exception e){
            e.printStackTrace();
        }
    }
%>


<%
String specReportSize = "" + SPEC_REPORT_WINDOW_SIZE;
boolean USE_AUTO_SET_PRIMARY_MATERIAL = LCSProperties.getBoolean("com.lcs.wc.flexbom.autosetPrimaryMaterial", false);
wt.util.WTContext.getContext().setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));
lcsContext.initContext();
lcsContext.setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));
lcsContext.setRequest(request);
lcsContext.setCacheSafe(true);
response.setContentType("text/html; charset=" +defaultCharsetEncoding);

String chooseaMaterialFromThePaletteOrMaterialLibraryOnMouseOverTooltip = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "chooseaMaterialFromThePaletteOrMaterialLibraryOnMouseOver_TOOLTIP", RB.objA ) ;
String chooseaColorFromThePaletteorColorLibraryOnMouseOverTooltip = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "chooseaColorFromThePaletteorColorLibraryOnMouseOver_TOOLTIP", RB.objA ) ;
String bomIdentificationLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "bomIdentification_GRP_TLE", RB.objA ) ;
String headerAttributesToolTip = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "headerAttributes_TOOLTIP", RB.objA ) ;
String expandBOMheaderToolTip = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "expandBOMheader_TOOLTIP", RB.objA ) ;
String collapseBOMheaderToolTip = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "collapseBOMheader_TOOLTIP", RB.objA ) ;
String headerAttributesLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "headerAttributes_LBL", RB.objA ) ;
String sectionLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "section_LBL", RB.objA ) ;
String totalLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "total_LBL", RB.objA ) ;
String modelButton = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "model_Btn", RB.objA ) ;
String rowButton = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "row_Btn", RB.objA ) ;
String saveRecentChangesContinueEdittingOnMouseOverTooltip =  WTMessage.getLocalizedMessage ( RB.FLEXBOM, "saveRecentChangesContinueEdittingOnMouseOver_TOOLTIP", RB.objA ) ;
String saveRecentChangesExitEditorOnMouseOverTooltip = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "saveRecentChangesExitEditorOnMouseOver_TOOLTIP", RB.objA ) ;
String ignoreRecentChangesExitEditorOnMouseOverTooltip = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "ignoreRecentChangesExitEditorOnMouseOver_TOOLTIP", RB.objA ) ;
String clearPrimaryMaterialOnMouseOverTooltip = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "clearPrimaryMaterialBtnOnMouseOver_TOOLTIP", RB.objA ) ;
String colorsLabel = WTMessage.getLocalizedMessage ( RB.COLOR, "colors_LBL", RB.objA ) ;
String saveButton = WTMessage.getLocalizedMessage ( RB.MAIN, "save_Btn", RB.objA ) ;
String saveCheckInButton = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "saveCheckIn_Btn", RB.objA ) ;
String closeEditorButton = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "closeEditor_Btn", RB.objA ) ;
String clearPrimaryMaterialButton = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "clearPrimaryMaterial_Btn", RB.objA ) ;
String closeLabel = WTMessage.getLocalizedMessage (RB.MAIN, "closeWithParenths", RB.objA ) ;
String setAsPrimaryLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "setAsPrimaryMaterial_LBL", RB.objA ) ;
String insertAfterLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "insertAfter_LBL", RB.objA ) ;
String insertBeforeLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "insertBefore_LBL", RB.objA ) ;
String deleteLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "delete_Btn", RB.objA ) ;
String cutLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "cut_LBL", RB.objA ) ;
String copyLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "copy_LBL", RB.objA ) ;
String pasteLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "paste_LBL", RB.objA ) ;
String materialsLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "materials_LBL",RB.objA ) ;
String libraryLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "library_LBL",RB.objA ) ;
String youMayOnlySelectFromPaletteForThisMaterialAlrt = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "youMayOnlySelectFromPaletteForThisMaterial_ALRT", RB.objA );
String youMayNotUseLibraryForThisMaterialAlrt = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "youMayNotUseLibraryForThisMaterial_ALRT", RB.objA );
String noLineItemSelectInBOMAlrt = WTMessage.getLocalizedMessage (RB.FLEXBOM, "noLineItemSelectInBOM_ALRT", RB.objA ) ;
String eitherSelectALineItemOrAddANewLineItemAlrt = WTMessage.getLocalizedMessage (RB.FLEXBOM, "eitherSelectALineItemOrAddANewLineItem_ALRT", RB.objA ) ;
String toAddANewLineItemInstructionAlrt = WTMessage.getLocalizedMessage (RB.FLEXBOM, "toAddANewLineItemInstruction_ALRT", RB.objA ) ;
String thenTryInsertingAMaterialAlrt = WTMessage.getLocalizedMessage (RB.FLEXBOM, "thenTryInsertingAMaterial_ALRT", RB.objA ) ;
String thisColorIsNotInThePaletteAlrt = WTMessage.getLocalizedMessage (RB.FLEXBOM, "thisColorIsNotInThePalette_ALRT", RB.objA ) ;
String youMayNotUseThisMaterialWithColorsNotInThePaletteAlrt = WTMessage.getLocalizedMessage (RB.FLEXBOM, "youMayNotUseThisMaterialWithColorsNotInThePalette_ALRT", RB.objA ) ;
String noRowSelectedMSG = WTMessage.getLocalizedMessage (RB.FLEXBOM, "noRowSelected_MSG", RB.objA ) ;
String clearAll = WTMessage.getLocalizedMessage ( RB.MAIN, "clearAll_ALTS",RB.objA ) ;
String selectAll = WTMessage.getLocalizedMessage ( RB.MAIN, "selectAll_ALTS",RB.objA ) ;
String deleteSelectedRows = WTMessage.getLocalizedMessage ( RB.MAIN, "deleteSelectedRows_ALTS",RB.objA ) ;
String moveSelectedRowsUp = WTMessage.getLocalizedMessage ( RB.MAIN, "moveSelectedRowsUp_ALTS",RB.objA ) ;
String moveSelectedRowsDown = WTMessage.getLocalizedMessage ( RB.MAIN, "moveSelectedRowsDown_ALTS",RB.objA ) ;
String insertBeforeSelectRows = WTMessage.getLocalizedMessage ( RB.MAIN, "insertBeforeSelectRows_ALTS",RB.objA ) ;
String insertAfterSelectRows = WTMessage.getLocalizedMessage ( RB.MAIN, "insertAfterSelectRows_ALTS",RB.objA ) ;

String defaultLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "default_LBL",RB.objA ) ;
String colorwayColorsLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "colorwayColors_LBL",RB.objA ) ;
String colorwayMaterialsLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "colorwayMaterials_LBL",RB.objA ) ;
String fiveLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "five_LBL",RB.objA ) ;
String fourLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "four_LBL",RB.objA ) ;
String editorModeLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "editorMode_LBL",RB.objA ) ;
String yieldsLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "yields_LBL",RB.objA ) ;
String colorLabel = WTMessage.getLocalizedMessage ( RB.COLOR, "color_LBL",RB.objA ) ;
String clearWithParenthLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "clearWithParenth_LBL",RB.objA ) ;
String editorHelp = WTMessage.getLocalizedMessage ( RB.MAIN, "editorHelp_TOOLTIP",RB.objA ) ;
String destinationLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "destinations_LBL",RB.objA ) ;
String destinationDimLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "destinationDim", RB.objA ) ;

String seasonLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "season_LBL", RB.objA ) ;
String productLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "product_LBL", RB.objA ) ;
String prodSeasonLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "prodSeason_OPT", RB.objA ) ;
String skuLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "sku_LBL", RB.objA ) ;
String editBOMPgTle = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "editBOM_PG_TLE",RB.objA ) ;
String allWithDashLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "allWithDash_LBL",RB.objA ) ;
String noneWithDashLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "noneWithDash_LBL",RB.objA ) ;


String costsLabel = WTMessage.getLocalizedMessage ( RB.SOURCING, "costs_LBL",RB.objA ) ;

String hideAllBtn = WTMessage.getLocalizedMessage ( RB.TABLEGENERATOR, "hideAll_BTN", RB.objA ) ;
String showAllBtn = WTMessage.getLocalizedMessage ( RB.TABLEGENERATOR, "showAll_BTN", RB.objA ) ;
String hiddenBtn = WTMessage.getLocalizedMessage ( RB.TABLEGENERATOR, "hidden_BTN", RB.objA ) ;
String columnBtn = WTMessage.getLocalizedMessage ( RB.TABLEGENERATOR, "column_BTN", RB.objA ) ;

String hideShowButton = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "hideShow_LBL", RB.objA );
String selectLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "select_LBL", RB.objA );
String insertLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "insert_LBL", RB.objA );

String toggleImagesToolTip  = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "toggleImagesToolTip", RB.objA );
String imagesLabel          = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "images_LBL", RB.objA );
String selectViewToolTip    = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "selectViewToolTip", RB.objA );
String expandLabel          = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "expandLabel", RB.objA );
String loadingWaitLabel     = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "loadingWaitLabel", RB.objA );
String bomLoadLabel         = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "bomLoadLabel", RB.objA );
String showDimensionsToolTip= WTMessage.getLocalizedMessage ( RB.FLEXBOM, "showDimensionsToolTip", RB.objA );
String dimensionsLabel      = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "dimensionsLabel", RB.objA );
String seasonIconToolTip      = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "seasonIconToolTip", RB.objA );
String sizeIconToolTip      = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "sizeIconToolTip", RB.objA );
String cancelLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "cancel_Btn",RB.objA ) ;
String sourceLabel           = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "sourceLabel", RB.objA );
String sourcesLabel           = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "sources_LBL", RB.objA );
String branchLabel          = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "branchLabel", RB.objA );
String colorwayLabel        = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "colorwayLabel", RB.objA );
String supplierLabel        = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "supplierLabel", RB.objA );
String colorDescriptionLabel= WTMessage.getLocalizedMessage ( RB.FLEXBOM, "colorDescriptionLabel", RB.objA );
String colorwaysLabel       = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "colorwaysLabel", RB.objA );

String viewLabel            = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "viewLabel", RB.objA );

String sizeCategoryNameLabel = WTMessage.getLocalizedMessage ( RB.SIZING, "sizeDefinition_LBL",RB.objA ) ;
String cssRuleNotFoundMsg = WTMessage.getLocalizedMessage ( RB.MEASUREMENTS, "cssRuleNotFound_MSG", RB.objA );

String invalidDimensionsMSG = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "invalidDimensions_MSG",RB.objA ) ;
String dimThreshholdMSG = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "dimThreshhold_MSG",RB.objA ) ;

String collapseBOMTooltip = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "hideAllExpandedRowsinBOM_TOOLTIP", RB.objA ) ;
String collapseBOMLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "collapse_LBL", RB.objA ) ;

String retrieveBOMWarningMessage = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "retrieveBOMWarningMessage",RB.objA ) ;
String confirmSaveChangesMessage = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "confirmSaveChangesMessage",RB.objA ) ;
String confirmSaveAndCheckInMessage = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "confirmSaveAndCheckInMessage",RB.objA ) ;
String confirmCloseEditorMessage = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "confirmCloseEditorMessage",RB.objA ) ;

String savingBOMPleaseBePatientMsg = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "savingBOMPleaseBePatient_MSG", RB.objA );
String errorMsg = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "errorColon_MSG", RB.objA ) ;
String searchingMaterialsPleaseBePatientMsg = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "searchingMaterialsPleaseBePatient_MSG", RB.objA ) ;
String searchingColorsPleaseBePatientMsg = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "searchingColorsPleaseBePatient_MSG", RB.objA ) ;
String multipleResultsMsg = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "multipleResults_MSG", RB.objA ) ;
String bomIsSavedMessage = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "bomIsSaved_MSG",RB.objA ) ;
String materialisSubassemblyCopyOrLinkMsg = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "materialisSubassemblyCopyOrLink_MSG", RB.objBR ) ;
String insertCopyLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "insertCopy_LBL", RB.objA ) ;
String linkLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "link_LBL", RB.objA ) ;
String operationLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "operation_LBL", RB.objA ) ;
String multiProductLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "multiProduct_LBL", RB.objA ) ;
String complexCheckedOutMsg = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "complexCheckedOut_MSG", RB.objA );
String notFoundMSG = WTMessage.getLocalizedMessage( RB.MAIN, "notFound_MSG", RB.objA);
/* - -------------------------- - */
/* - BOM HighLighting resources - */
/* - -------------------------- - */
String highLightRows = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "highLightRows_LBL", RB.objA ) ;
String toolBarWarningMsg = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "toolBarWarning_MSG", RB.objA ) ;
String noneLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "none_LBL",RB.objA ) ;
String reservedColorLabel = WTMessage.getLocalizedMessage ( RB.MEASUREMENTS, "reservedColor_Lbl", RB.objA );
String highLightBOMRowColorMsg = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "highlighBOMRowColor_MSG", RB.objA );

String expandCollapseSection = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "expandCollapseSection_MSG", RB.objA );//"Expand Section";
String expandCollapseBOM = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "expandCollapseBOM_MSG", RB.objA );//"Expand BOM";
String noVariationsSelectedMSG = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "noVariationsSelected_MSG", RB.objA );//"You have not selected any Variations.";
String expandingBranchMSG = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "expandingBranch_MSG", RB.objA );
String derivedFromOtherAttributeStringLbl = WTMessage.getLocalizedMessage( RB.FLEXGENERATOR, "derivedFromOtherAttributes_LBL" );
String derivedStringLbl = WTMessage.getLocalizedMessage(RB.FLEXGENERATOR, "derived_LBL");
String noneSelectedLabel = WTMessage.getLocalizedMessage(RB.SEASON, "noneSelected_LBL", RB.objA);

String URI_CONTEXT = request.getRequestURI();
int lastSlash = URI_CONTEXT.lastIndexOf("/");
String CURRENT_LOCATION = URI_CONTEXT.substring(0,lastSlash+1);
String AJAX_PAGE = URL_CONTEXT + BOM_AJAX_UTILITY;

%>

<%// try{ %>

<% if ( WCPART_ENABLED ){%>
<script type="text/javascript" src="/<%=wt.util.WTProperties.getLocalProperties().getProperty("wt.webapp.name")%>/netmarkets/javascript/util/windchill-libs.js"></script>
<script type="text/javascript" src="/<%=wt.util.WTProperties.getLocalProperties().getProperty("wt.webapp.name")%>/netmarkets/javascript/ext/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="/<%=wt.util.WTProperties.getLocalProperties().getProperty("wt.webapp.name")%>/netmarkets/javascript/util/ext-and-extensions.js"></script>
<script type="text/javascript" src="/<%=wt.util.WTProperties.getLocalProperties().getProperty("wt.webapp.name")%>/netmarkets/javascript/util/windchill-all.js"></script>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/flexPickerChooser.js"></script>
<%}%>
<%request.setAttribute("layoutType", "DIV_LAYOUT"); %>

<jsp:include page="<%=subURLFolder+ STANDARD_TEMPLATE_HEADER %>" flush="true">
    <jsp:param name="none" value="" />
</jsp:include>

<%
    boolean useBOMCosting = COSTING;
    String partOid = request.getParameter("oid");
    String skuMasterId = request.getParameter("bomColorDim");
    String size1 = FormatHelper.format(request.getParameter("bomSize1Dim"));
    String size2 = FormatHelper.format(request.getParameter("bomSize2Dim"));
    String destinationId = FormatHelper.format(request.getParameter("bomDestinationDim"));
    String skuMode = "SINGLE";
    String sourceMode = "SINGLE";
    String sizeMode = "SINGLE";

    //////Check modify access on BOM parent////////
    FlexBOMPart bomPart = (FlexBOMPart) LCSQuery.findObjectById(partOid);
    boolean modifyAccess = ACLHelper.hasModifyAccess(bomPart);
    if (!modifyAccess)
        throw new Exception("No access allowed.");

    boolean useSize1 = false;
    boolean useSize2 = false;

    String selectedSizeCategory = request.getParameter("sizeCategoryId");
    String splId = request.getParameter("splId");
    String contextSKUId = request.getParameter("contextSKUId");

    String bomEditorMode = "SKU_COLOR";
    String multiAttribute = "";
    String multiDimension = "";
    boolean allColorMode = false;

    String sourceDimId = "";
    boolean topLevelMode = true;

    if("SKU_COLOR".equals(bomEditorMode)){
        multiAttribute = "colorDescription";
        multiDimension = "SKU";
        skuMode = LCSFlexBOMQuery.ALL_SKUS;
        allColorMode = true;
        sourceMode = "";
        size1 = "";
        size2 = "";
    }

    // DIFFERENTIATE BETWEEN MATERIAL AND PRODUCT BOM
    String productId = "";
    String materialId = "";
    String ownerId = "";
    LCSProduct product = null;
    LCSMaterial material = null;
    FlexType productType = null;
    FlexType materialType = null;
    String ownerMode = "";
    RevisionControlled bomOwner = null;
    String primaryImageURL = "";
    String defaultImageURL = "";

    Map sizeCategoryMap = new HashMap();

    request.setAttribute("contextBOMPart", bomPart);
    boolean productBOM = false;
    boolean materialBOM = false;
    BOMOwner ownerMaster = bomPart.getOwnerMaster();
    LCSSeason season = null;
    String seasonId = null;
    LCSPalette palette = null;

    if(ownerMaster instanceof LCSPartMaster){
        productBOM = true;
        product = (LCSProduct) VersionHelper.getVersion(ownerMaster, "A");
        productType = product.getFlexType();
        ownerMode = "PRODUCT";
        bomOwner = product;
        productId = FormatHelper.getVersionId(product);
        String productLinkId = splId;
        if(!FormatHelper.hasContent(appContext.getProductARevId())
                || !(productId.equals(appContext.getProductARevId()))
                || (FormatHelper.hasContent(productLinkId) && !(productLinkId.equals(appContext.getProductLinkId())))
                || !FormatHelper.hasContent(productLinkId)){

            if(!FormatHelper.hasContent(appContext.getActiveProductId()) || (FormatHelper.hasContent(appContext.getActiveProductId()) && appContext.getActiveProductId().indexOf("LCSSourcingConfig")==-1 && appContext.getActiveProductId().indexOf("FlexSpecification")==-1)){
                if(FormatHelper.hasContent(productLinkId)){
                    appContext.setProductContext(productLinkId);
                } else {
                    appContext.setProductContext(productId);

                }
            }
        }
        ownerId = productId;
        primaryImageURL = product.getPartPrimaryImageURL();
        defaultImageURL = LCSProduct.newLCSProduct().getPartPrimaryImageURL();

        if(FormatHelper.hasContent(splId)){
            season = appContext.getSeason();
            if(season != null){
                try{
                   palette = season.getPalette();
                }catch(Exception e){
                    logger.debug(e.getMessage());
                }
                LCSSeasonProductLink link = LCSSeasonQuery.findSeasonProductLink(product, season);
                if(link != null){
                    splId = FormatHelper.getObjectId(link);
                } else {
                    season = null;
                }
            }
        }
        sizeCategoryMap  = BOMEditorUtility.getSizeCategoryMap(product, season);

        if(sizeCategoryMap != null && sizeCategoryMap.keySet().size() > 0){
            if(!(sizeCategoryMap.keySet().contains(selectedSizeCategory))){
                selectedSizeCategory = null;
            }
        }

        if ( ! FormatHelper.hasContent(selectedSizeCategory) ) {
            //CHUCK - Need to select a default size category
            if ( sizeCategoryMap.keySet().size() > 0 ) {
               Collection firstSize = BOMEditorUtility.getSizeCategoryList(product, season);
               FlexType sizingType = product.getFlexType().getReferencedFlexType(ReferencedTypeKeys.SIZING_TYPE);
               String sizeNameKey = sizingType.getAttribute("name").getColumnName().toUpperCase();
               firstSize = SortHelper.sortFlexObjects(firstSize, "PRODUCTSIZECATEGORY." + sizeNameKey);
               Iterator firstSizeIt = firstSize.iterator();
               selectedSizeCategory = "VR:com.lcs.wc.sizing.ProductSizeCategory:" + ((FlexObject)firstSizeIt.next()).get("PRODUCTSIZECATEGORY.BRANCHIDITERATIONINFO");
            }
        }
        materialType = bomPart.getFlexType().getReferencedFlexType(ReferencedTypeKeys.MATERIAL_TYPE);
    } else {
        materialBOM = true;
        material = (LCSMaterial) VersionHelper.latestIterationOf(ownerMaster);
        //materialType = material.getFlexType();
        materialType = bomPart.getFlexType().getReferencedFlexType(ReferencedTypeKeys.MATERIAL_TYPE);
        ownerMode = "MATERIAL";
        bomOwner = material;
        materialId = FormatHelper.getVersionId(material);
        ownerId = materialId;
        appContext.setMaterialContext(materialId);
        primaryImageURL = material.getPrimaryImageURL();
    }
    if(USE_AUTO_SET_PRIMARY_MATERIAL && !productBOM) {
        USE_AUTO_SET_PRIMARY_MATERIAL = false;
    }
    Map seasonMap = new HashMap();
    Vector<String> order = new Vector<String>();
    if(productBOM){
        seasonMap = (new ProductHeaderQuery()).findSeasonsForProductMap(product, true);
        seasonMap.put(" ", noneSelectedLabel);
        order.add(0, " ");
        if(seasonMap != null && !seasonMap.containsKey(splId)) {
            splId = " ";
        }
    }

    boolean billOfLabor = "LABOR".equals(bomPart.getBomType());
    String shortLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "BOM_short" , RB.objA );
    String mediumLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "BOM_medium" , RB.objA );
    String materialLabel = materialsLabel;


    if(billOfLabor){
        materialLabel = libraryLabel;
        useBOMCosting = false;
    }
    Object[] objB = {shortLabel};
    String retrieveButton = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "retrieve_Btn", objB ) ;
    String forTheCurrentColorAndSourceTooltip = WTMessage.getLocalizedMessage (RB.FLEXBOM, "forTheCurrentColorAndSource_TOOLTIP", objB ) ;
    String size1Label = "";
    String size2Label = "";
    String size1DimLabel = "";
    String size2DimLabel = "";
    boolean sizedBOM = false;
    Collection size1Collection = null;
    Collection size2Collection = null;

    Vector size1Order = null;
    Vector size2Order = null;

    Map size1Map = new HashMap();
    Map size2Map = new HashMap();
    if(FormatHelper.hasContent(selectedSizeCategory)){
        String numId = FormatHelper.getNumericFromOid(selectedSizeCategory);

        ProductSizeCategory sizeCategory = (ProductSizeCategory)LCSQuery.findObjectById(selectedSizeCategory);

        if(FormatHelper.getObjectId((WTObject)ownerMaster).equals(FormatHelper.getObjectId(sizeCategory.getProductMaster()))){

            FullSizeRange fullSizeRange = sizeCategory.getSizeRange().getFullSizeRange();
            size1Label = fullSizeRange.getSize1Label();
            size2Label = fullSizeRange.getSize2Label();
            Object[] objC = {size1Label };
            size1DimLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "sizeDimLabel", objC ) ;
            objC[0] = size2Label;
            size2DimLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "sizeDimLabel", objC ) ;

            size1Collection = MOAHelper.getMOACollection(sizeCategory.getSizeValues());
            size1Order = new Vector();
            Iterator sizeRange = size1Collection.iterator();
            String sizeVal = "";
            if(sizeRange.hasNext()){
                useSize1 = true;
            }
            while(sizeRange.hasNext()){
                sizeVal = (String)sizeRange.next();
                size1Map.put(sizeVal, sizeVal);
                size1Order.add(sizeVal);

            }
            size2Collection = MOAHelper.getMOACollection(sizeCategory.getSize2Values());
            size2Order = new Vector();
            sizeRange = size2Collection.iterator();
            if(sizeRange.hasNext()){
                useSize2 = true;
            }
            while(sizeRange.hasNext()){
                sizeVal = (String)sizeRange.next();
                //size1Map.put(numId + "_" + sizeVal, sizeVal);
                //size1Order.add(numId + "_" + sizeVal);
                size2Map.put(sizeVal, sizeVal);
                size2Order.add(sizeVal);

            }
        }
        //currently there is nothing for size2
    }
    request.setAttribute("bomOwner", bomOwner);
    type = bomPart.getFlexType();

    Collection sections = type.getAttribute("section").getAttValueList().getSelectableKeys(lcsContext.getLocale(), true);

    boolean includeSetAsPrimary = ACLHelper.hasEditAccess(type.getAttributeGroupObject(PRIMARY_MATERIAL_GROUP, false)) && !billOfLabor;

    boolean updateName =ACLHelper.hasEditAccess(type.getAttributeGroupObject(LCSProperties.get("com.lcs.wc.flexbom.FlexBOMPartNameAttributeGroup", "General Attributes"), false));

    FlexTypeAttribute att;

    //Set up views
    String activity = "EDIT_BOM";
    String selectedView = "";
    String defaultViewId = lcsContext.viewCache.getDefaultViewId(FormatHelper.getObjectId(type), activity);
    String defViewName = "";
    if(FormatHelper.hasContent(defaultViewId)){
        defViewName = lcsContext.viewCache.getView(defaultViewId).getDisplayName();
    }

    // LIST OF VIEWS
    Collection reportColumns = lcsContext.viewCache.getViews(FormatHelper.getObjectId(type), activity);
    Collection<String> permanentColumns = new ArrayList<String>() {
        {
            add("Insert");
            add("Select");
            add("Expand");
            add("scComponent");
            add("materialDescription");
        }
    };

    // BUILD A LISTING OF VIEWS FOR BUILDING VIEW DROP DOWNS
    HashMap<String, String> columnOptions = new HashMap<>();
    Vector<String> viewOrder = new Vector<String>();
    viewOrder.add(0,"1");
    if(reportColumns != null && reportColumns.size() > 0){
        Iterator ci = reportColumns.iterator();
        columnOptions.put("1",noneSelectedLabel);
        while(ci.hasNext()){
            FlexObject fobj = (FlexObject)ci.next();
            columnOptions.put("OR:com.lcs.wc.report.ColumnList:" + fobj.getString("COLUMNLIST.IDA2A2"),fobj.getString("COLUMNLIST.DISPLAYNAME"));
        }
    }

    // ROLL THOUGH THE ALL VIEWS AND FOR EACH VIEW ADD ALL OF
    // ITS COLUMNS TO A MASTER LISTING OF ALL COLUMNS USED FOR THIS
    // BOM
    Set viewColumns = new TreeSet();
    Iterator rci = LCSQuery.getObjectsFromResults(reportColumns, "OR:com.lcs.wc.report.ColumnList:", "COLUMNLIST.IDA2A2").iterator();

    while ( rci.hasNext() ) {
        ColumnList list = (ColumnList) rci.next();
        if ( list.getAttributes() != null ) {
            Iterator subList = list.getAttributes().iterator();
            while ( subList.hasNext() ) {
                String aid = (String)subList.next();
                if(aid.indexOf(".") > -1){
                    aid = aid.substring(0, aid.indexOf(".")).toUpperCase() + aid.substring(aid.indexOf("."));
                }
                while(aid.indexOf(" ") > -1){
                    aid = aid.substring(0, aid.indexOf(" ")) + aid.substring(aid.indexOf(" ") + 1);
                }
				logger.debug("adding column from view : "+aid);
                viewColumns.add(aid);
            }
        }
    }
    if(billOfLabor) {
        viewColumns.add("BOM.process");
        viewColumns.add("SUPPLIER.name");
        viewColumns.add("BOM.quantity");
        viewColumns.add("BOM.lossAdjustment");
        viewColumns.add("MATERIALSUPPLIER.materialPrice");
        viewColumns.add("BOM.priceOverride");
        viewColumns.add("BOM.rowTotal");
    } else {
        viewColumns.add("MATERIAL.unitOfMeasure");
        viewColumns.add("BOM.quantity");
    }
    // REMOVE CERTAIN VALUES FROM THE LIST OF COLUMNS
    // (WHY?)(ANSWER: Remove columns the columns that should not be diplayed in the view, unless specified as part of another action...i.e. expand variation)
    Iterator cleanup = viewColumns.iterator();
    %>
    <script>
    var variationAttInView = [];
    var autoSetPrimaryMaterial = <%=USE_AUTO_SET_PRIMARY_MATERIAL%>;
    </script>
    <%
    while(cleanup.hasNext()){
        String tv = (String)cleanup.next();
        if("Colorways".equals(tv) ||
            "sourceDim".equals(tv) ||
            "destinationDim".equals(tv) ||
            "colorDim".equals(tv) ||
            "size1Dim".equals(tv) ||
            "size2Dim".equals(tv) ||
            "supplierName".equals(tv) ||
            "materialDescription".equals(tv)){

    %>
    <script>
        variationAttInView.push('<%= tv %>');
    </script>
    <%
            cleanup.remove();
        }
    }
    //Get all attributes that user has access to.
    Collection atts = type.getAllAttributes(FlexBOMFlexTypeScopeDefinition.LINK_SCOPE, null, false);
    Map attMap = flexg.hashAttributesByKey(atts, "BOM.");

    Collection bomLinkAttsTemp = atts;
    Collection bomAttsTemp = type.getAllAttributes(FlexBOMFlexTypeScopeDefinition.BOM_SCOPE, null, false);
    bomLinkAttsTemp.addAll(bomAttsTemp);
    Iterator bomHeaderAttsIterTemp = bomLinkAttsTemp.iterator();

    //get the required attributes
    Map requiredAttMap = new HashMap();
    FlexTypeAttribute requiredAtt = null;
    for (Iterator i = attMap.keySet().iterator(); i.hasNext();){

        String columnKey = (String) i.next();
        requiredAtt = (FlexTypeAttribute)attMap.get(columnKey);
        if (requiredAtt.isAttRequired()){
            requiredAttMap.put(columnKey, requiredAtt);
        }
    }

    FlexType materialRoot = bomPart.getFlexType().getReferencedFlexType(ReferencedTypeKeys.MATERIAL_TYPE);

    FlexType ftColorRoot = FlexTypeCache.getFlexTypeFromPath("Color");
    Set<String> excludeEncodeSet = new HashSet<String>();
    Collection<FlexTypeAttribute> materialAtts = materialRoot.getAllAttributes(MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE, null, false);
    excludeEncodeSet.addAll(materialAtts.stream().filter(mat -> "image".equals(mat.getAttVariableType()) || "url".equals(mat.getAttVariableType())).map(FlexTypeAttribute::getAttKey).map(s -> "MATERIAL_" + s).collect(Collectors.toList()));
    attMap.putAll(flexg.hashAttributesByKey(materialAtts, "MATERIAL."));

    Collection<FlexTypeAttribute> materialSupplierAtts = materialRoot.getAllAttributes(MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE, null, false);
    excludeEncodeSet.addAll(materialSupplierAtts.stream().filter(mat -> "image".equals(mat.getAttVariableType()) || "url".equals(mat.getAttVariableType())).map(FlexTypeAttribute::getColumnName).map(s -> "MATERIAL_" + s).collect(Collectors.toList()));
    attMap.putAll(flexg.hashAttributesByKey(materialSupplierAtts, "MATERIAL."));

    FlexType supplierRoot = FlexTypeCache.getFlexTypeFromPath(MATERIAL_SUPPLIER_ROOT_TYPE);
    Collection<FlexTypeAttribute> supplierAtts = supplierRoot.getAllAttributes();
    excludeEncodeSet.addAll(supplierAtts.stream().filter(mat -> "image".equals(mat.getAttVariableType()) || "url".equals(mat.getAttVariableType())).map(FlexTypeAttribute::getColumnName).map(s -> "SUPPLIER_" + s).collect(Collectors.toList()));
    attMap.putAll(flexg.hashAttributesByKey(supplierAtts, "SUPPLIER."));

    FlexType colorRoot = FlexTypeCache.getFlexTypeFromPath("Color");
    Collection<FlexTypeAttribute> colorAtts = colorRoot.getAllAttributes();
    excludeEncodeSet.addAll(colorAtts.stream().filter(mat -> "image".equals(mat.getAttVariableType()) || "url".equals(mat.getAttVariableType())).map(FlexTypeAttribute::getColumnName).map(s -> "COLOR_" + s).collect(Collectors.toList()));
    attMap.putAll(flexg.hashAttributesByKey(colorAtts, "COLOR."));

    FlexType matColorRoot = FlexTypeCache.getFlexTypeFromPath("Material Color");
    Collection<FlexTypeAttribute> matcolorAtts = matColorRoot.getAllAttributes();
    excludeEncodeSet.addAll(matcolorAtts.stream().filter(mat -> "image".equals(mat.getAttVariableType()) || "url".equals(mat.getAttVariableType())).map(FlexTypeAttribute::getColumnName).map(s -> "MATERIALCOLOR_" + s).collect(Collectors.toList()));
    attMap.putAll(flexg.hashAttributesByKey(matcolorAtts, "MATERIALCOLOR."));

    Iterator columnIter;
    String columnKey;

    String section = "";
    Iterator sectionIter = null;


%>
<jsp:include page="<%=subURLFolder+ VIEW_BOM_COLUMN_ORDER %>" flush="true">
    <jsp:param name="useBOMCosting" value="<%= useBOMCosting %>" />
    <jsp:param name="ownerMode" value="<%= ownerMode %>" />
</jsp:include>
<%
    // THIS CODE HAS TO BE BELOW THE HEADER INCLUDE
    Collection skuList = new ArrayList();
    String skuIDIndex = "";
    String skUDescriptionIndex = "";
    String skuNameIndex = "";

    if ( productBOM ) {
        skuIDIndex = "LCSSKU.IDA3MASTERREFERENCE";
        skUDescriptionIndex = "";
        skuNameIndex = productType.getAttribute("skuName").getSearchResultIndex();
    } else {
        skuIDIndex = "LCSMATERIALCOLOR.IDA2A2";
        skUDescriptionIndex = "";
        skuNameIndex = "MATCOLOR.DISPLAY";
    }
    Map tSkuMap = new HashMap();
    Map skuMasterMap = new HashMap();

    if ( "SKU".equals(multiDimension) ) {
        if ( productBOM ) {
            ArrayList attributeRequest = new ArrayList();
            attributeRequest.add("skuName");
            skuList = new ArrayList();
            if(ownerId.equals(appContext.getProductARevId())){
                tSkuMap = appContext.getSKUsMap();
            } else {
                tSkuMap = (new ProductHeaderQuery()).findSKUsMap(product, null, season, false);
            }

            Map ttSkuMap = new HashMap();
            if(FormatHelper.hasContent(request.getParameter("colorwayIds"))){
                Iterator cids = MOAHelper.getMOACollection(request.getParameter("colorwayIds")).iterator();
                String cid = null;
                while(cids.hasNext()){
                    cid = "VR:com.lcs.wc.product.LCSSKU:" + (String)cids.next();
                    if(tSkuMap.containsKey(cid)){
                        ttSkuMap.put(cid, tSkuMap.get(cid));
                    }
                }
                tSkuMap = new HashMap(ttSkuMap);
            }


            Iterator i = LCSQuery.getObjectsFromCollection(tSkuMap.keySet()).iterator();
            while(i.hasNext()){
                LCSSKU sku = (LCSSKU) i.next();//LCSQuery.findObjectById(fullId);
                String fullId = FormatHelper.getVersionId(sku);
                FlexObject skufo = new FlexObject();
                skufo.put(skuIDIndex, FormatHelper.getNumericFromReference(sku.getMasterReference()));
                skufo.put(skuNameIndex, tSkuMap.get(fullId));
                skuList.add(skufo);
                skuMasterMap.put(FormatHelper.getObjectId((WTObject)sku.getMaster()), tSkuMap.get(fullId));
            }
        } else {
            // BUILD THE SKULIST FROM THE MATERIAL COLORS FOR
            // THIS MATERIAL.

            LCSMaterialQuery matQuery = new LCSMaterialQuery();
            LCSMaterialColorQuery matcolQuery = new LCSMaterialColorQuery();
            if (ClientContext.getContext().isVendor) {
                skuList=matcolQuery.findMaterialColorDataForVendor(material,true).getResults();
            }
            else{
                skuList = matQuery.findMaterialColorData(material).getResults();
            }

            if(FormatHelper.hasContent(request.getParameter("colorwayIds")) ){
                Collection cids = MOAHelper.getMOACollection(request.getParameter("colorwayIds"));
                Iterator skui = skuList.iterator();
                while(skui.hasNext()){
                    FlexObject skufo = (FlexObject)skui.next();
                    if(!cids.contains(skufo.getString("LCSMATERIALCOLOR.IDA2A2"))){
                        skui.remove();
                    }
                }
            }
            BOMEditorUtility.getMaterialColorDisplay(skuList);
        }
        skuList = SortHelper.sortFlexObjects(skuList, skuNameIndex);
    }
    Map destinationMap = new HashMap();
    if(productBOM){
        SearchResults destinationResults = new ProductDestinationQuery().findProductDestinationsforProduct((LCSPartMaster)product.getMaster());

        destinationMap = LCSQuery.createTableList(destinationResults, "PRODUCTDESTINATION.IDA2A2", "PRODUCTDESTINATION.DESTINATIONNAME", "");
    }

    String tabPage = request.getParameter("tabPage");

    String specReport = "";
    boolean multiProd = false;

    if(bomPart != null && "PRODUCT".equals(ownerMode)){
        Collection whereUsed = FlexSpecQuery.componentWhereUsed((WTObject)bomPart.getMaster());
        multiProd = BOMEditorUtility.multipleProdCheck(whereUsed);
        specReport = (new FlexSpecUtil()).getComponentWhereUsedDetailsTable(whereUsed);
        if (bomPart.getDesignDoc() != null) {
            String prodMasterId = FormatHelper.getNumericFromReference(appContext.getProductARev().getMasterReference());
            specReport += (new FlexSpecUtil()).getAssociatedImagePageDetail(bomPart, prodMasterId);
        }
    }

    String synchReport = "";
    if(bomPart != null && "PRODUCT".equals(ownerMode)){
        synchReport = (new BOMSynchUtil()).getBOMSynchReportTable(bomPart);
    }


    // AUTOMATIC HIGHLIGHTING //
    // There are 3 pieces to AUTOMATIC HIGHLIGHTING
    // 1.  The code immediately following this comment block
    //      It extracts the configured colors to use for automatic highlighting based on a SingleList attribute defined in the BOM Type
    //      NO MATTER HOW MANY ARE DEFINED, currently only the first 3 will be used
    // 2.  FlexBomLogic.js function handleChange()
    //      Performs the actual automatic highlight when a user changes the value of the singlelist attribute
    // 3.  Code at bottom of this file to populate the "key" of the palette identifiying which colors are reserved for automatic highlight usage
    //
    Map autoHighlightColors = new HashMap();
    Map autoHighlightCodes = new HashMap();
    Map autoHighlightKeys = new HashMap();

    if ((billOfLabor && USE_AUTOMATIC_BOL_HIGHLIGHT) || ((!billOfLabor) && USE_AUTOMATIC_BOM_HIGHLIGHT)) {
        FlexTypeAttribute markUpAtt = type.getAttribute("ptcbomPartMarkUp");
        if (markUpAtt != null) {
            Collection colorDriverKeys = markUpAtt.getAttValueList().getKeys();
            Iterator iter = colorDriverKeys.iterator();
            while (iter.hasNext()) {
                String valueListKey = (String)iter.next();
                if (FormatHelper.hasContent( (String)markUpAtt.getAttValueList().getValueFromValueStore(valueListKey, "highlightColor"))) {
                    // get the actual HIGHLIGHT_ORANGE color CSS string from the valuestore
                    String autoColor = (String)markUpAtt.getAttValueList().getValueFromValueStore(valueListKey, "highlightColor");
                    // get the locale specific display for this attributeValueList Key
                    String codeLabel= (String)markUpAtt.getAttValueList().getValue(valueListKey, lcsContext.getLocale());
                    autoHighlightKeys.put(valueListKey, autoColor);
                    autoHighlightColors.put(autoColor, valueListKey);
                    autoHighlightCodes.put(autoColor, codeLabel);
                }
            }
        }
    }
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// HTML ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

<script>
function hideSelectorDiv() {
    if(document.getElementById('selectorDiv')){
        hideDiv('selectorDiv');
    }
}

function executeDriveAutomaticHighlightOnChange(attKey, currentRow, branch) {
    <% if ((billOfLabor && USE_AUTOMATIC_BOL_HIGHLIGHT) || ((!billOfLabor) && USE_AUTOMATIC_BOM_HIGHLIGHT)) { %>
        // AUTOMATIC HIGHLIGHTING //
        // if the attribute modified by the user is the markUp attribute
        // get the highlight color to use
        // and set the highlight based on the value selected by the user
        //
        if (attKey.indexOf('MarkUp') > -1) {
            var keyColors = {};
            var colorKeys = {};

            // set default highlight color
            <% Collection aCollection = autoHighlightKeys.keySet();
            Iterator markUpKeys = aCollection.iterator();
            while (markUpKeys.hasNext()) {
                String markUpKey1 = (String)markUpKeys.next();
            %>
                keyColors['<%=markUpKey1%>'] = '<%=autoHighlightKeys.get(markUpKey1)%>'
                colorKeys['<%=autoHighlightKeys.get(markUpKey1)%>'] = '<%=markUpKey1%>';
            <% } %>


            if (hasContent(branch.ptcbomPartMarkUp)) {

                if (hasContent(keyColors[branch.ptcbomPartMarkUp])) {
                    var color = keyColors[branch.ptcbomPartMarkUp];
                    if (getCSSRule("." + color, false)) { // check to ensure Admin has created Style class in Style Sheet
                        setHighLightForRow(currentRow, branch, color);
                        branch.highLight = color;
                    } else {
                        alert('<%= FormatHelper.encodeForJavascript(cssRuleNotFoundMsg) %>');
                    }
                }
            }else{ // if markUp has been set to null && branch had a previous highlight then clear the highlight
                if (hasContent(colorKeys[branch.highLight])) {
                    setHighLightForRow(currentRow, branch, 'HIGHLIGHT_WHITE');
                    branch.highLight = '';
                }
            }
       }
   <% } %>
}
var currencyAttDisplayVariables = [];
var bomLinkDerivedAttributes = {};
var derivedStringLabel = " (" + '<%= FormatHelper.encodeForJavascriptInSingleQuote(derivedStringLbl) %>' + ")";
<%
if(bomPart != null) {
    Collection<FlexTypeAttribute> bomAtts = bomPart.getFlexType().getAllAttributes();
    for (FlexTypeAttribute bomAtt : bomAtts){
        if(FormatHelper.hasContent(bomAtt.getAttDerivedFrom())) {
            %>
            bomLinkDerivedAttributes['<%= bomAtt.getAttKey() %>'] = '<%= FormatHelper.encodeForJavascript(bomAtt.getAttDefaultValue()) %>';
            bomLinkDerivedAttributes['<%= bomAtt.getAttKey() %>' + "Display"] = '<%= FormatHelper.encodeForJavascript(derivedFromOtherAttributeStringLbl) %>';
            <%
        }
        if("currency".equals(bomAtt.getAttVariableType())) {%>
            currencyAttDisplayVariables.push('<%= bomAtt.getAttKey() %>' + "Display");
<%      }
    }
}
%>
</script>
<base id='basehref' href="<%=codebaseUrl%>/"/>


<link href="<%=URL_CONTEXT%>/css/domtableeditor.css" rel="stylesheet">
<link href="<%=URL_CONTEXT%>/css/bomHeader.css" rel="stylesheet">
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/ajax.js"></script>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/util.js"></script>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/flexbommodel.js"></script>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/jsdebugger.js"></script>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/domtableeditor.js"></script>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/datamodel.js"></script>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/bomdatamodel.js"></script>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/flexbomlogic.js"></script>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/flexbomhighlight.js"></script>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/flexbomrestyle.js"></script>
<style>
.subassemblypartname {
    padding-left: 30px !important;
    background-image: url("<%=WT_IMAGE_LOCATION%>/button_next.png");
    background-repeat: no-repeat;
    background-position: 5px 5px;
    font-style: italic;
    overflow:hidden;
}
</style>
<script type="text/javascript">
    //Populate the ovrMap
<%
    Collection ovrOptions = MOAHelper.getMOACollection(LCSFlexBOMQuery.BOM_OVR_ORDER);
    Iterator ovrIt = ovrOptions.iterator();
    String ovr = "";
    while(ovrIt.hasNext()){
        ovr = (String)ovrIt.next();
        if(materialBOM && ":SOURCE:SKU".equals(ovr)){
            continue;
        }
%>
    ovrMap[ovrMap.length] = '<%= ovr %>';
<% } %>

    var viewMode = false;

    // DECLARATION OF JS VARIABLES THAT WILL BE USED IN FLEXBOMLOGIC JAVASCRIPT
    var partOid = '<%= FormatHelper.encodeForJavascript(partOid) %>';
    var productBOM = <%= productBOM %>;
    var URL_CONTEXT = '<%= URL_CONTEXT %>';
    var WT_IMAGE_LOCATION = '<%= WT_IMAGE_LOCATION %>';
    var urlContext_masterController_url = '<%=PageManager.getContextOverrideUrlAndMasterContorller(true)%>';
    var materialPlaceHolderId = '<%= FormatHelper.getNumericObjectIdFromObject(LCSMaterialQuery.PLACEHOLDER) %>';
    var materialSupplierPlaceholderId = '<%= FormatHelper.getNumericFromOid(LCSMaterialSupplierQuery.PLACEHOLDERID) %>';
    var noRowSelectedMSG = '<%= FormatHelper.encodeForJavascript(noRowSelectedMSG) %>';
    var youMayOnlySelectFromPaletteForThisMaterialAlrt = '<%= FormatHelper.encodeForJavascript(youMayOnlySelectFromPaletteForThisMaterialAlrt)%>';
    var youMayNotUseLibraryForThisMaterialAlrt = '<%= FormatHelper.encodeForJavascript(youMayNotUseLibraryForThisMaterialAlrt)%>';
    var useBOMCosting = <%= useBOMCosting %>;
    var AJAX_SEARCH_PREFIX = "<%= AJAX_SEARCH_PREFIX %>";
    var retrieveBOMWarningMessage = '<%= FormatHelper.encodeForJavascript(retrieveBOMWarningMessage) %>';
    disableDOMTableMenu = true;
    invalidDimensionCombinationMSG = '<%= FormatHelper.encodeForJavascript(invalidDimensionsMSG) %>';
    sourcesDimLBL='<%= FormatHelper.encodeForJavascript(sourcesLabel) %>';
    skuDimLBL='<%= FormatHelper.encodeForJavascript(colorwaysLabel) %>';
    size1DimLBL='<%= FormatHelper.encodeForJavascript(size1Label) %>';
    size2DimLBL='<%= FormatHelper.encodeForJavascript(size2Label) %>';
    destinationDimLBL='<%= FormatHelper.encodeForJavascript(destinationLabel) %>';
    dimRowsThreshhold = <%= "" + DIM_ROW_THRESHHOLD %>;
    dimThresholdExceedMSG = '<%= FormatHelper.encodeForJavascript(dimThreshholdMSG) %>';

    noVariationsSelectedMSG = '<%= FormatHelper.encodeForJavascript(noVariationsSelectedMSG) %>';

    expandingBranchMSG = '<%= FormatHelper.encodeForJavascript(expandingBranchMSG) %>';

    branchTotalDecimalPlaces = <%= "" + bomPart.getFlexType().getAttribute("rowTotal").getAttDecimalFigures() %>;

    var sectionTypeMap = {};
        <%
        sectionIter = sections.iterator();
        while(sectionIter.hasNext()){
            section = (String) sectionIter.next();
            Collection typeIdPaths = BOMEditorUtility.getTypeListForSection(section, materialType);
            if(typeIdPaths != null && typeIdPaths.size() > 0){
            %>
    var <%= section %>TypeMap = new Array();
            <%
                Iterator tpI = typeIdPaths.iterator();
                while(tpI.hasNext()){
            %>
     <%= section %>TypeMap[<%= section %>TypeMap.length] = '<%= tpI.next() %>';
                <% } %>
     sectionTypeMap['<%= section %>'] = <%= section %>TypeMap;
            <% } %>
        <% } %>

    var sectionModeMap = {};
        <%
        sectionIter = sections.iterator();
        String modePrefix = "com.lcs.wc.material.BOMSection.constraintMode.";
        String sectionModeKey = "";
        String sectionMode = "";
        while(sectionIter.hasNext()){
            section = (String) sectionIter.next();
            sectionModeKey = modePrefix + section;
            sectionMode = LCSProperties.get(sectionModeKey);
            if(FormatHelper.hasContent(sectionMode)){
            %>
            sectionModeMap['<%= section %>'] = '<%= sectionMode %>';
            <% } else { %>
            sectionModeMap['<%= section %>'] = 'PREVENT';
            <% } %>

        <% } %>

    <%
    //////////////////////////////////////////////////////////////////////
    // JC - THIS IS REQUIRED:  GUARANTEES THE MAXBRANCHID, IF BELONGS TO ROW THAT HAS BEEN DROPPED, IS NOT REUSED.
    // CANNOT RELY ON LOADING OF bomDataModel, OR LOADING OF TOP LEVEL ROWS TO TABLE DATA MODEL
    // [createTDMTopLevelEntries()] TO DETERMINE THE NEXT AVAILABLE BRANCH ID
    // BECAUSE ONLY NON-DROPPED WIP RECORDS ARE INCLUDED IN THE ACTIVE DATA SET.
    // SO USING NEW FUNCTION setNextBranchId(int maxbranchid) to initialize
    //////////////////////////////////////////////////////////////////////
    LCSFlexBOMLogic logic = new LCSFlexBOMLogic();
    int maxBranchId = logic.getMaxBranchId(bomPart);
    %>
    //////////////////////////////////////////////////////////////////////
    // BUILD THE BOM DATA MODEL...THIS IS BUILT USING THE LCSFLEXBOMQUERY,
    // AND DUMPING THE DATA
    //////////////////////////////////////////////////////////////////////
    var bomDataOrder = new Array();
    var orderIndex = 0;
    var bomDataModel = new BOMDataModel();
    var bB;
    bomDataModel.setNextBranchId('<%= maxBranchId %>');

    <%
    logger.debug("viewColumn: " + viewColumns);
    BomEditorUtil bmu = new BomEditorUtil(bomPart.getFlexType());

    Collection usedAttsKeys = BOMEditorUtility.getUsedAttList(viewColumns);
    bmu.setUsedAttKeys(usedAttsKeys);
    String usedAttKeysStr = MOAHelper.toMOAString(usedAttsKeys);
	logger.debug("usedAttKeysStr: " + usedAttKeysStr);
    String viewColumnsStr = MOAHelper.toMOAString(viewColumns);
	logger.debug("viewColumnsStr: " + viewColumnsStr);
    Collection bomDataCol = bmu.getBDMData(bomPart, viewColumns);
    bomDataCol = SortHelper.sortMapsNumeric(bomDataCol, "sortingNumber");
    Iterator bomData = bomDataCol.iterator();

    Collection numericKeys = bmu.getNumericKeys();
    Map souMap = new HashMap();

    if(productBOM){
        souMap = BOMEditorUtility.getSourceMasters((new ProductHeaderQuery()).findSourcingConfigsMap(product, season));
    }

    while(bomData.hasNext()){
        Map dataRow = (Map)bomData.next();
        if(!productBOM || (BOMEditorUtility.validProductRow(dataRow, skuMasterMap, souMap, size1Map, size2Map, destinationMap))){
            out.println("\tbB = new BOMBranch();");
            Iterator keys = dataRow.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String)keys.next();
                String val = (String) ("" + dataRow.get(key));

                if (((
                        (numericKeys.contains(key) && FormatHelper.hasContent(val))
                        || (!numericKeys.contains(key) && FormatHelper.hasContentAllowZero(val))
                    ) && !val.equals("false")) ||
                    (( ("size1DimensionId".equals(key) ||
                        "size2DimensionId".equals(key))) &&
                        FormatHelper.hasContentAllowZero(val))) {

                    out.println("\tbB." + key + " = '" + FormatHelper.encodeForJavascript(val) + "';");
                }

                // this key could have a false value and if so we need it set to 'false'
                if ("skuOverridesExist".equals(key) && val.equals("false")) {
                    out.println("\tbB." + key + " = false;");
                }

                 if ("hasMaterialAccess".equals(key) && val.equals("false")) {
                    out.println("\tbB." + key + " = false;");
                }
            }
            out.println("\tbB.persisted = true;");
            //out.println("\tbB.dropped = false;");
            //out.println("\tbB.touched = false;");
            //out.println("\tbB.added = false;");
            out.println("\tbB.colorControlled = '" + dataRow.get("colorControlled") + "';");
            out.println("\tif(hasContent(bB.materialName)){ bB.materialDescription = bB.materialName; }");
            out.println("\tif(hasContent(bB.supplierName)){ bB.supplierDescription = bB.materialName; }");
            out.println("\tbomDataModel.loadBranch(bB," + !FormatHelper.hasContent((String)dataRow.get("dimensionName")) + ");");

            out.println();
            %>
            //the dimensionName is null, it means is toplevel row
            if (!hasContent(bB.dimensionName)) {
                bomDataOrder[orderIndex] = bB.branchId;
                orderIndex++;
            }
            <%
        }
    }
    %>
    columnInfo = <%= bmu.getColumnInfo(viewColumns) %>
    var dataModel = new FlexBOMModel();
    dataModel.placeholderChildId = '<%= FormatHelper.getNumericObjectIdFromObject(LCSMaterialSupplierQuery.getPlaceholder()) %>'

    //////////////////////////////////////////////////////////////////////
    // JC - THIS IS REQUIRED:  GUARANTEES THE MAXBRANCHID, IF BELONGS TO ROW THAT HAS BEEN DROPPED, IS NOT REUSED.
    // CANNOT RELY ON LOADING OF bomDataModel ABOVE, OR LOADING OF TOP LEVEL ROWS TO TABLE DATA MODEL
    // BELOW (createTDMTopLevelEntries()) TO DETERMINE THE NEXT AVAILABLE BRANCH ID
    // BECAUSE ONLY NON-DROPPED WIP RECORDS ARE INCLUDED IN THE ACTIVE DATA SET.
    // SO USING NEW FUNCTION setNextBranchId(int maxbranchid) to initialize
    //////////////////////////////////////////////////////////////////////
    dataModel.setNextBranchId('<%= maxBranchId %>');

    //////////////////////////////////////////////////////////////////////////////////////
    function isEditable(cell, skipColorMode, skipAlert){
        //debug(cell.id);
        if(cell.style.display == 'none'){
            return false;
        }

        var id = cell.id;


        <jsp:include page="<%=subURLFolder+ CLIENT_SIDE_PLUGIN %>" flush="true" >
             <jsp:param name="type" value="<%= FormatHelper.getObjectId(type) %>"/>
             <jsp:param name="pluginType" value="BOMEditor_isEditable"/>
        </jsp:include>


        if(cell.className == 'disabled'){
            return false;
        }

        var skuMode = (id.indexOf('$sku$') > 0)

        var attKey;
        var branchId;
        var branch;
        var skuId;
        if(skuMode){
            attKey = id.substring(id.indexOf('_') + 1, id.indexOf('$'));
            skuId = id.substring(id.lastIndexOf('$') + 1, id.length);
            branchId = id.substring(1, id.indexOf('_'));
            branch = dataModel.getBranch(branchId);

        } else {
            attKey = id.substring(id.indexOf('_') + 1, id.length);
            branchId = id.substring(1, id.indexOf('_'));
            branch = dataModel.getBranch(branchId);
        }

        if(bomLinkDerivedAttributes.hasOwnProperty(attKey)) {
            return false;
        }


        if( attKey == 'name' ||
            attKey == 'materialPrice' ||
            attKey == 'rowTotal' ||
            attKey == 'supplierName' ||
            attKey == 'colorDim' ||
            attKey == 'sourceDim' ||
            attKey == 'destinationDim' ||
            attKey == 'size1Dim' ||
            attKey == 'size2Dim' ||
            attKey == 'brchId' ||
            attKey == 'dimExpander' ||

        <%
               Iterator matAtts = materialRoot.getAllAttributes(null, null, false).iterator();
                    while(matAtts.hasNext()){
                            att = (FlexTypeAttribute) matAtts.next();
                            if(BOMEditorUtility.isInViewColumns("MATERIAL", att.getAttKey(), viewColumns)){
                                out.print("attKey == 'MATERIAL_" + att.getAttKey() + "' ||");
                            }
                    }

            %>
        <%
               Iterator supAtts = supplierRoot.getAllAttributes(null, null, false).iterator();
                    while(supAtts.hasNext()){
                            att = (FlexTypeAttribute) supAtts.next();
                            if(BOMEditorUtility.isInViewColumns("SUPPLIER", att.getAttKey(), viewColumns)){
                                out.print("attKey == 'SUPPLIER_" + att.getAttKey() + "' ||");
                            }
                    }

            %>
        <%
               Iterator matColAtts = matColorRoot.getAllAttributes(null, null, false).iterator();
                    while(matColAtts.hasNext()){
                            att = (FlexTypeAttribute) matColAtts.next();
                            if(BOMEditorUtility.isInViewColumns("MATERIALCOLOR", att.getAttKey(), viewColumns)){
                                out.print("attKey == 'MATERIALCOLOR_" + att.getAttKey() + "' ||");
                            }
                    }

            %>
        <%
               Iterator colAtts = colorRoot.getAllAttributes(null, null, false).iterator();
                    while(colAtts.hasNext()){
                            att = (FlexTypeAttribute) colAtts.next();
                            if(BOMEditorUtility.isInViewColumns("COLOR", att.getAttKey(), viewColumns)){
                                out.print("attKey == 'COLOR_" + att.getAttKey() + "' ||");
                            }
                    }

            %>
            attKey == 'unitOfMeasure' ||
            attKey == 'refNumber'){
            return false;
        }

        if(attKey == 'partName' && hasContent(branch.parentBranchId)){
            return false;

        }

        if(attKey == 'materialDescription'){
            //debug("isEditable: materialDescription: materialLink = " + branch.materialLinked);
            if(skuMode){
                if(branch["materialLinked$sku$" + skuId]){
                    return false;
                }

            } else {
                if(branch.materialLinked){
                    return false;
                }
            }
        }
        //ALLOW EDIT OF UOM IF MATERIAL IS NOT LINKED
        if(attKey == 'unitOfMeasure' && branch.materialLinked){
            return false;
        }


        <%  Iterator attsIter = atts.iterator();
        while(attsIter.hasNext()){
            att = (FlexTypeAttribute) attsIter.next();
            if(!att.isAttUpdateable() || !ACLHelper.hasEditAccess(att)){
                %>if(branch.persisted && attKey == '<%= att.getAttKey() %>'){ return false; }
                <%
            }
            if(!att.isAttSettable() || !ACLHelper.hasCreateAccess(att)){
                %>if(!branch.persisted && attKey == '<%= att.getAttKey() %>'){ return false; }
                <%
            }
        }
        %>

        if(attKey == 'colorDescription' && !skipColorMode){

            // QUICK FIX TO PREVENT EDITING OF NON RELEVANT SKU DATA.
            if(cell.innerHTML.indexOf("disabled") > -1){
                return false;
            }
            // dmf nike below
            if(getColorMode(branch) == MATCOL_SELECT_COLORMODE || branch.colorControlled == '4'|| branch.colorControlled == '4.0'){
                return true;

            } else if(getColorMode(branch) == FORCE_PALETTE_COLORMODE && <%= palette == null %>){
                // TEMP ENABLE TEXT EDIT ON PALETTE MODE when palette is not null.
                if(!skipAlert){
                    alert(youMayOnlySelectFromPaletteForThisMaterialAlrt);
                }
                return false;
            }
            // For Colorway Variation
            if(skuMode){
                if(branch["colorLinked$sku$" + skuId]){
                    return true;
                }

            } else {
                if(branch.colorLinked){
                    //For Color Variation
                    return true;
                }
            }
        }
        return true;
    }


    ////////////////////////////////////////////////////////////////////////////
    // MATERIAL AJAX QUICK SEARCH IMPLEMENTATION
    ////////////////////////////////////////////////////////////////////////////
    var materialPrefixAttributeMap = {};
    var attributeKeyToTypeMap ={};
    materialPrefixAttributeMap['DEFAULT'] = 'name';
    var materialColorMode = "";
    <%
    /** LOAD PREFIX / ATTRIBUTE MAP FOR MATERIAL QUICK SEARCH
     */
    String prefixMapString = MATERIAL_QUICK_SEARCH_ATTRIBUTE_MAP;
    StringTokenizer tokenizer = new StringTokenizer(prefixMapString, ",");
    while(tokenizer.hasMoreTokens()){
        String mapping = tokenizer.nextToken();

        String prefix = mapping.substring(0, mapping.indexOf(":"));
        String attribute = mapping.substring(mapping.indexOf(":") + 1, mapping.length());
        if(materialRoot.getAttributeKeyList().contains(attribute.toUpperCase())){
            %>
            materialPrefixAttributeMap['<%= prefix %>'] = '<%= materialRoot.getAttribute(attribute).getSearchCriteriaIndex() %>';
            attributeKeyToTypeMap['<%= materialRoot.getAttribute(attribute).getSearchCriteriaIndex() %>'] ='<%= materialRoot.getAttribute(attribute).getAttVariableType() %>';
            <%
        }
    }
    %>
    var viewColumnKeys = {};
    var colorColumnKeys = new Array();
    <%
        Iterator it = viewColumns.iterator();
        while ( it.hasNext() ) {
           String tattkey = (String)it.next();
           //Can't bring in Color/Material color right now
           if ( tattkey.startsWith("MATERIAL") && tattkey.indexOf("COLOR") < 0 ) {
            String attkey = tattkey.substring(tattkey.indexOf(".") + 1);
            String searchidx = FlexTypeQuery.getDisplayResultIndex(materialRoot.getAttribute(attkey), null, null).toUpperCase();

            %>
            viewColumnKeys['MATERIAL_<%= attkey %>'] = '<%= searchidx %>';
            <%
           } else if ( tattkey.startsWith("SUPPLIER") ) {
            String attkey = tattkey.substring(tattkey.indexOf(".") + 1);
            String searchidx = FlexTypeQuery.getDisplayResultIndex(supplierRoot.getAttribute(attkey), null, null).toUpperCase();
            %>
            viewColumnKeys['SUPPLIER_<%= attkey %>'] = '<%= searchidx %>';
            <%
           } else if ( tattkey.startsWith("MATERIALCOLOR") ) {
            %>
            colorColumnKeys[colorColumnKeys.length] = '<%= tattkey.substring(0, tattkey.indexOf(".")) + "_" + tattkey.substring(tattkey.indexOf(".") + 1) %>';
            <%
           } else if(tattkey.startsWith("COLOR")) {
            %>
            colorColumnKeys[colorColumnKeys.length] = '<%= tattkey.substring(0, tattkey.indexOf(".")) + "_" + tattkey.substring(tattkey.indexOf(".") + 1) %>';
            <%
           }
        }
    %>

    materialPrefixAttributeMap['VERSIONID'] = 'LCSMATERIALSUPPLIER_BRANCHIDITERATIONINFO';
    materialPrefixAttributeMap['MASTERID'] = 'LCSMATERIALSUPPLIER_IDA3MASTERREFERENCE';
    var queryBranchId = "";
    var bMatWorking = false;
    var matCellId = "";
    var caseInsensitive = '<%= CASE_INSENSITIVE %>';
    ////////////////////////////////////////////////////////////////////////////
    function findMaterial(criteria, branch, sourceCell,sectionCheck){
        var selectOnly = true;

<%
    ArrayList attCols = new ArrayList();
    if(viewColumns.size() > 0){
        Iterator i = viewColumns.iterator();
        while(i.hasNext()){
            String tempattkey = (String)i.next();
            if(tempattkey.indexOf(".") > 0){
                tempattkey = tempattkey.substring(tempattkey.indexOf(".") + 1);
            }
            attCols.add(tempattkey);
        }
    }
%>

        var queryColumns = '<%= MOAHelper.toMOAString(attCols) %>';
        if(branch){
            queryBranchId = branch.branchId;
            selectOnly = false;
        }

        //save matCellId, so we can clear it in the case user selects "Close" on the chooser dialog
        if (!bMatWorking && sourceCell) {
            matCellId = sourceCell.id;
            bMatWorking = true;
        }

        var attribute = materialPrefixAttributeMap['DEFAULT'];
        if(criteria.indexOf(AJAX_SEARCH_PREFIX) > -1){
            var prefix = "";

            if(criteria.indexOf(AJAX_SEARCH_PREFIX) == 0){
                criteria = criteria.substring(criteria.indexOf(AJAX_SEARCH_PREFIX) + 1, criteria.length);
                attribute = materialPrefixAttributeMap["DEFAULT"];
            } else {
                prefix = criteria.substring(0, criteria.indexOf(AJAX_SEARCH_PREFIX));
                if(hasContent(materialPrefixAttributeMap[prefix])){
                    criteria = criteria.substring(criteria.indexOf(AJAX_SEARCH_PREFIX) + 1, criteria.length);
                    attribute = materialPrefixAttributeMap[prefix];
                    if (hasContent(attributeKeyToTypeMap[attribute])){
                        var attributeType = attributeKeyToTypeMap[attribute];
                        if (attributeType == "integer" || attributeType == "float" || attributeType =="sequence" || attributeType == "currency"){
                            criteria = criteria + '<%= RANGE_DELIM %>' + criteria;
                        }
                    }
                }else{
                    bMatWorking = false;
                    return;
                }
            }
        }

        criteria = replaceAll(criteria,'%','%');
        var materialTypeId = '<%= FormatHelper.getObjectId(materialType) %>';
        createPopUp('<%= FormatHelper.encodeForJavascript(searchingMaterialsPleaseBePatientMsg) %>',"AjaxStatus");
        //////////To fix SPR#1972026, change to use post to call Ajax request, because of the length limitation of URL
        var destUrl = location.protocol + '//' + location.host + urlContext_masterController_url;
        var headerAttParams = getHeaderAttParams();
        if(sectionCheck){
            var parameters = 'activity=FIND_MATERIAL_SUPPLIER&skipRanges=true&&action=AJAXSEARCH&' + attribute + '=' + encodeURIComponent(criteria) + '&sectionConstraint=' + branch.section + '&materialTypeId=' + materialTypeId + '&attCols=' + queryColumns + '&' + new Date() + '&bomType=<%= bomPart.getBomType() %>' + '&caseInsensitive=' + caseInsensitive;
            parameters = parameters + headerAttParams;
            runPostAjaxRequest(destUrl,parameters,'handleMaterialQuery');
        }
        else{
            var parameters = 'activity=FIND_MATERIAL_SUPPLIER&skipRanges=true&action=AJAXSEARCH&' + attribute + '=' + encodeURIComponent(criteria) + '&materialTypeId=' + materialTypeId + '&attCols=' + queryColumns + '&' + new Date() + '&bomType=<%= bomPart.getBomType() %>'+ '&caseInsensitive=' + caseInsensitive;
            parameters = parameters + headerAttParams;
            runPostAjaxRequest(destUrl,parameters,'handleMaterialQuery');
        }
    }

    function handleMaterialQuery(xml, text){
        closeDiv("AjaxStatus");
        endCellEdit();
        var count = 0;
        var countText = text.substring(text.indexOf("resultsCount:") + 13, text.indexOf("resultsCount:") + 23);
        countText = trim(countText);
        if(hasContent(countText)){
            count = parseFloat(countText);
        }

        var branch = dataModel.getBranch(queryBranchId);
        if(!hasContent(branch.dimensionName)){
            //is topLevel...collapse if expanded
            var topLevelBranch = dataModel.getBranch(branch.branchId);
            if(topLevelBranch.expanded){
                expandBranch(queryBranchId);
            }
        }

        var cell = document.getElementById("r" + queryBranchId + "_materialDescription");

        if(count > 0){
            // MULTIPLE RESULTS (HTML RESULTS)
            var content = cell.innerHTML;
            content = content + ' :  ' + '<%= FormatHelper.encodeForJavascript(multipleResultsMsg) %>';
            cell.innerHTML = content;
            branch.materialDescription = content;
            createPopUp_for_Material_Color("matMat", text);
            return;
        }

        var rows;
        if(xml){
            rows = xml.getElementsByTagName('row');
            bMatWorking = false;
        }
        if((!rows || rows.length == 0) && count == 0){
            // NO RESULTS FOUND
            var content = cell.innerHTML;
            content = content + ': '+ '<%= FormatHelper.encodeForJavascript(notFoundMSG) %>';
            cell.innerHTML = content;
            branch.materialDescription = content;
            bMatWorking = false;
            return;
        }

        // Save and restore currentRow on handleMaterialQuery as we are inserting a new material at the current row and deleteSubAssembly messes with it internally
        savedCurrentRow = currentRow;
        deleteSubAssembly(branch);
        currentRow = savedCurrentRow;

        ////////////////////////////////////////////////////////////////////////
        // THE CODE BELOW THIS POINT ASSIGNS A MATERIAL TO A ROW IN THE EDITOR.
        ///////////////////////////////////////////////////////////////////////

        count = 1;

        var materialName = getValueFromXMLRow(rows[0], '<%= materialRoot.getAttribute("name").getSearchResultIndex().toUpperCase() %>');
        var materialUOM = getValueFromXMLRow(rows[0], '<%= materialRoot.getAttribute("unitOfMeasure").getSearchResultIndex().toUpperCase() %>');
        var materialSupplierPrice = getValueFromXMLRow(rows[0], '<%= materialRoot.getAttribute("materialPrice").getSearchResultIndex().toUpperCase() %>');
        var supplierName = getValueFromXMLRow(rows[0], 'LCSSUPPLIERMASTER.SUPPLIERNAME');
        var materialTypePath = getValueFromXMLRow(rows[0], 'LCSMATERIAL.FLEXTYPEIDPATH');
        var bomWIP = getValueFromXMLRow(rows[0], 'BOM_WIP');
        var saInsertMode = getValueFromXMLRow(rows[0], 'BOM_INSERTIONMODE');
        var hasMaterialAccess = getValueFromXMLRow(rows[0], 'hasMaterialAccess');
        branch.hasMaterialAccess=hasMaterialAccess;
        branch.materialTypePath=materialTypePath;

        if('ALWAYS_INSERT_COPY' == saInsertMode && '1' == bomWIP){
            alert('<%= FormatHelper.encodeForJavascript(complexCheckedOutMsg) %>');
            return;
        }
        var branchSection = branch.section;
        var secMap = sectionTypeMap[branchSection];

        if(!isBOMSectionConstraintValid(branch,false,cell)){
           return;
        }

        if(!hasContent(materialSupplierPrice)){
            materialSupplierPrice = '0';
        }
        var id = "";
        var materialColorControlled = "";


        branch.childId = getValueFromXMLRow(rows[0], 'LCSMATERIALSUPPLIERMASTER.IDA2A2');
        branch.supplierVersionId = getValueFromXMLRow(rows[0], 'LCSSUPPLIER.BRANCHIDITERATIONINFO');
        branch.materialSupplierVersionId = getValueFromXMLRow(rows[0], 'LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO');
        branch.materialVersionId = getValueFromXMLRow(rows[0], 'LCSMATERIAL.BRANCHIDITERATIONINFO');
        branch.colorControlled = getValueFromXMLRow(rows[0], 'COLORCONTROLLED');
        branch.materialName = htmlDecode(materialName);
        branch.touched = true;
        branch.materialLinked = true;
        branch.materialDescription = htmlDecode(materialName);
        branch.materialSupplierMasterId = branch.childId;
        branch.supplierName = htmlDecode(supplierName);
        branch.matSupplierPrice = materialSupplierPrice;

        for(var n in viewColumnKeys){
            if(hasContent(getValueFromXMLRow(rows[0], viewColumnKeys[n]))){
                branch[n] = htmlDecode(getValueFromXMLRow(rows[0], viewColumnKeys[n]));
            }else{
                branch[n] = "";
            }
        }
        //CLEAR COLORS AS APPLICABLE
        clearColorInfo(branch);

        // THIS LINE UPDATES THE BOM DATA MODEL WITH THE CHANGES.
        handleNewMaterialAssignment(branch);

        var colorMode = "";

        document.MAINFORM.currentMaterialId.value = branch.childId;
        document.MAINFORM.currentMaterialName.value = materialName;
        document.MAINFORM.currentSupplierName.value = supplierName;
        document.MAINFORM.currentMaterialSupplierPrice.value = materialSupplierPrice;
        document.MAINFORM.currentMaterialUOM.value = materialUOM;
        document.MAINFORM.currentColorControlled.value = colorMode;

        var currentMaterialNameDiv = document.getElementById("currentMaterialNameDiv");
        currentMaterialNameDiv.innerHTML = materialName + " / " + supplierName;
        currentMaterialNameDiv.title = htmlDecode(materialName + " / " + supplierName);

        cell.innerHTML = materialName;
        if(branch.override){
            cell.className = 'BOMEDITOROVERRIDE';
        } else {
            cell.className = 'materialLinked pincol';
        }

        var cell = document.getElementById("r" + queryBranchId + "_materialPrice");
        if(cell){
            cell.innerHTML = materialSupplierPrice;
        }

        var cell = document.getElementById("r" + queryBranchId + "_unitOfMeasure");
        if(cell){
            cell.innerHTML = materialUOM;
        }

        var cell = document.getElementById("r" + queryBranchId + "_supplierName");
        if(cell){
            cell.innerHTML = supplierName;
        }

        var excludeEncodeArr = new Array();
        excludeEncodeArr = '<%=excludeEncodeSet%>';
        for(var n in viewColumnKeys){
            var cell = document.getElementById("r" + queryBranchId + "_" + n);
            if(cell){
                if(hasContent(branch[n])){
                    if(excludeEncodeArr.includes(n) == true || excludeEncodeArr.includes(n) == 'true') {
                        cell.innerHTML = branch[n];
                    } else {
                        cell.innerHTML = HTMLEncoder.encodeForHTMLContent(branch[n]);
                    }
                }
                else{
                    cell.innerHTML = "&nbsp";
                }
            }
        }

        ////////////////////////////////////////////
        // HANDLE COLOR CONTROL MODES
        ////////////////////////////////////////////
        if(colorMode == FORCE_PALETTE_COLORMODE &&
           !checkForNonPaletteColors(branch)){
        } else {
            if(hasContent(colorMode)){
                branch.colorControlled = colorMode;
            }

            // INSERT A TEMPORARY MATERIAL COLOR LIST
            var materialColorList = materialColorMap['M' + branch.childId];

            var tKey = 'M' + branch.childId;

            if(!materialColorList){

                materialColorList = new Array();
                var materialColorData = getValueFromXMLRow(rows[0], 'MATERIALCOLORDATA');
                var matColArray = moaToArray(materialColorData);

                var colorName;
                var colorId;
                var materialColorId;
                var matColString;
                var matColPrice;

                for(var i = 0; i < matColArray.length; i++){
                    matColString = matColArray[i];
                    //alert(matColString);
                    matColPrice = matColString.substring(matColString.indexOf("###") + 3, matColString.length);
                    colorName = matColString.substring(matColString.indexOf("---") + 3, matColString.indexOf("###"));
                    colorId = matColString.substring( matColString.indexOf("***") + 3, matColString.indexOf("---"));
                    materialColorId = matColString.substring(0, matColString.indexOf("*"));
                    //debug(" materialColorId = " + materialColorId + " colorId = " + colorId +  " colorName = " + colorName);

                    materialColorData = {};
                    materialColorData.colorName = colorName;
                    materialColorData.colorId = colorId;
                    materialColorData.materialColorId = materialColorId;
                    materialColorData.price = matColPrice;
                    //CMS - Assuming the material color list returned will only be those MC that the user has access to
                    materialColorData.noAccess = false;

                    materialColorList[materialColorList.length] = materialColorData;
                }
                materialColorMap['M' + branch.childId] = materialColorList;
            }
        }


        ////////////////////////////////////////////
        // HANDLE SUB ASSEMBLIES
        ////////////////////////////////////////////
        var bomId = getValueFromXMLRow(rows[0], 'BOM_ID');

        if(!("NONE" == bomId)){
            if('ALWAYS_INSERT_COPY' == saInsertMode){
                if("1" == bomWIP){
                    alert('<%= FormatHelper.encodeForJavascript(complexCheckedOutMsg) %>');
                } else {
                    insertSubAssemblyCopy(bomId, branch.branchId);
                }
            } else if('ALWAYS_LINK' == saInsertMode){
                insertLinkedComplexMaterialIcon(branch.branchId);
            } else if('IGNORE' == saInsertMode){
                // DO NOTHING
            } else if('PROMPT_USER_LINK_OR_COPY' == saInsertMode){
                promptSubAssemblyOption(bomId, branch.branchId, bomWIP);
            } else {
                promptSubAssemblyOption(bomId, branch.branchId, bomWIP);
            }
        }
    }
    
    function createMatPopUp(text, id){
        if(!hasContent(id)){
            id = 'ajaxSearchResultPopup';
        }
        var button = "<a href=\"javascript:closeAjaxSearchResultPopup(\'" + id + "\'),clearMatCell(\'"+matCellId +"\')\"><%= FormatHelper.encodeForJavascript(closeLabel) %></a><br><br>";
        text = button + text;
        displayAjaxSearchResultPopup(id, text);
    }
    
    //---------------------------------------------------
    function clearMatCell(id) {
        if (id == null || id == undefined)
            return;
        var cell = document.getElementById(id);
        cell.innerHTML = '';

        var attKey = id.substring(id.indexOf('_') + 1, id.length);
        var branchId = id.substring(1, id.indexOf('_'));
        var branch = dataModel.getBranch(branchId);

        branch[attKey + "Display"] = '';
        branch[attKey] = '';
        bMatWorking = false;
    }
    //-------------------------------------------------
    ////////////////////////////////////////////////////////////////////////////
    function insertSubAssemblyCopy(bomId, masterBranchId){
        closeDiv('subAssemblyOptions');

        var viewColumnsStr = document.MAINFORM.viewColumnsStr.value;
        var usedAttKeysStr = document.MAINFORM.usedAttKeysStr.value;

        var loc = location.protocol + '//' + location.host + urlContext_masterController_url;
        var sOptions = 'activity=FIND_SUBASSEMBLY_BOM';
        sOptions = sOptions + '&oid=' + bomId;
        sOptions = sOptions + '&masterBranchId=' + masterBranchId;
        sOptions = sOptions + '&usedAttKeysStr=' + usedAttKeysStr;
        sOptions = sOptions + '&viewColumnsStr=' + viewColumnsStr;


        runPostAjaxRequest(loc, sOptions, 'insertSubAssembly');

        //change menu cell
        if(!viewMode){
            //debug('updating menu');
            var menuCell = getCell(currentRow, 1);
            menuCell.innerHTML =  '<div class=\"table-action-bar\">' + menuArray['subAsMenu'] + '</div>';
        }

    }
    ////////////////////////////////////////////////////////////////////////////
    function promptSubAssemblyOption(bomId, masterBranchId, bomWIP){

        var display = '<%= FormatHelper.encodeForJavascript(materialisSubassemblyCopyOrLinkMsg)%>';

        if("1" == bomWIP){
            //display += "<br><br><a class='button' onmouseover=\"return overlib('<%= FormatHelper.encodeForJavascript(complexCheckedOutMsg) %>');\" onmouseout=\"return nd();\" ><%= FormatHelper.encodeForJavascript(insertCopyLabel) %></a>";
            display += "<br><br>";
            display += "<a class='button' onmouseover=\"return overlib("
                display += "'";
                    display += "<%= FormatHelper.encodeForJavascript(complexCheckedOutMsg).replace("'","\\\\&#39;") %>";
                display += "'";
                display += ')" ';
                display += " onmouseout=\"return nd();\" href=\"javascript:doNothing()\">";
                display += "<%= FormatHelper.encodeForJavascript(insertCopyLabel) %>";
            display += "</a>";
        }
        else{
            display += "<br><br><a class='button' href=\"javascript:insertSubAssemblyCopy('" + bomId + "', '" + masterBranchId + "')\"><%= FormatHelper.encodeForJavascript(insertCopyLabel) %></a>";
        }
        display += "<br><br><a class='button' href=\"javascript:insertLinkedComplexMaterialIcon('" + masterBranchId + "')\"><%= FormatHelper.encodeForJavascript(linkLabel) %></a>";
        createPopUp(display, 'subAssemblyOptions');

    }

    // dmf nike
    function insertLinkedComplexMaterialIcon(masterBranchId)    {
        var expanderCell = document.getElementById("r" + masterBranchId + "_dimExpander");
        //expanderCell.innerHTML = '<img onclick="selectRowForCellImage(this);createPopUp(\'Expanding Branch\', \'expandingMessage\');expandBranch(' + masterBranchId + ')" border="0" src="' + URL_CONTEXT + '/images/pim_launch.gif">';
        expanderCell.innerHTML = '<img id="expanderImage' + masterBranchId + '" onclick="selectRowForCellImage(this);createPopUp(expandingBranchMSG, \'expandingMessage\');expandBranch(' + masterBranchId + ')" border="0" src="' + WT_IMAGE_LOCATION + '/pim_launch.png">';
        var branch = dataModel.getBranch(masterBranchId);
        branch.linkedBOM = '1';

        closeDiv('subAssemblyOptions');
    }
   
    function popUpChooseMaterial(id){
        //debug("popUpChooseMaterial: " + id);
        closeAjaxSearchResultPopup('ajaxSearchResultPopup');
        assignMaterial(id, queryBranchId);
        enableClear("clearMaterialDiv");

    }
    //SPR task :6784496 Clear Links visible even if no  material or color is copied to clipboard.
    function enableClear(clearDivId){
        document.getElementById(clearDivId).show();
        document.getElementById(clearDivId).style.display="inline";
    }
    ////////////////////////////////////////////////////////////////////////////
    function assignMaterial(id, branchId){
        //alert('assignMaterial : id = ' + id);
        var branch;
        if(branchId){
            queryBranchId = branchId;
        }
        branch = dataModel.getBranch(queryBranchId);

        var cell = document.getElementById("r" + queryBranchId + "_materialDescription");
        cell.innerHTML = 'Loading...';

        var criteria = id;
        if(id.indexOf('MASTERID') < 0 &&
           id.indexOf('VERSIONID') < 0){
            criteria = "VERSIONID" + AJAX_SEARCH_PREFIX + id;
        } else {
            critera = id;
        }

        findMaterial(criteria, branch);
    }
    ////////////////////////////////////////////////////////////////////////////
    // END AJAX MATERIAL QUICK SEARCH
    ////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////
    // AJAX COLOR QUICK SEARCH
    ////////////////////////////////////////////////////////////////////////////
    var arColorPrefixAttributeMap = {};
    arColorPrefixAttributeMap['DEFAULT'] = 'colorName';
    var colorAttributeKeyToTypeMap ={};
    <%
    /** LOAD PREFIX / ATTRIBUTE MAP FOR COLOR QUICK SEARCH
     */
    String stPrefixMap = COLOR_QUICK_SEARCH_ATTRIBUTE_MAP;
    tokenizer = new StringTokenizer(stPrefixMap, ",");
    while(tokenizer.hasMoreTokens()){
        String stMapping = tokenizer.nextToken();

        String stPrefix = stMapping.substring(0, stMapping.indexOf(":"));
        String stAttribute = stMapping.substring(stMapping.indexOf(":") + 1);
        if(ftColorRoot.getAttributeKeyList().contains(stAttribute.toUpperCase())){
            %>
            arColorPrefixAttributeMap['<%= stPrefix %>'] = '<%= ftColorRoot.getAttribute(stAttribute).getSearchCriteriaIndex() %>';
            colorAttributeKeyToTypeMap['<%= ftColorRoot.getAttribute(stAttribute).getSearchCriteriaIndex() %>'] ='<%= ftColorRoot.getAttribute(stAttribute).getAttVariableType() %>';
            <%
        }
    }
    %>

    arColorPrefixAttributeMap['OBJECTID'] =  'LCSCOLOR_IDA2A2';
    arColorPrefixAttributeMap['PALETTEID'] = 'LCSPALETTE_IDA2A2';
    var cellId = "";
    var bWorking = false;
    //-----------------------------------------------------------------
    function findColor(criteria, branch, sourceCell,sectionCheck){
        //debug("Find Color... criteria = " + criteria + ",sourceCell's id: " + sourceCell.id);
        //debug("section check...." + sectionCheck);
        var selectOnly = true;
        if (!bWorking) {
            cellId = sourceCell.id;
            //set the bWorking to true to prevent the change of Cell Id if Ajax still working
            bWorking = true;
        }

        <%
        ArrayList arlAttCols = new ArrayList();
        if(viewColumns.size() > 0){
            Iterator i = viewColumns.iterator();
            String tempattkey = "";
            while(i.hasNext()){
                tempattkey = (String)i.next();
                if(tempattkey.indexOf(".") > 0){
                    tempattkey = tempattkey.substring(tempattkey.indexOf(".") + 1);
                }
                arlAttCols.add(tempattkey);
            }
        }

        %>

        var queryColumns = '<%= MOAHelper.toMOAString(arlAttCols) %>';
        if(branch){
            queryBranchId = branch.branchId;
            selectOnly = false;
        }
        var attribute = arColorPrefixAttributeMap['DEFAULT'];
        if(criteria.indexOf(AJAX_SEARCH_PREFIX) > -1){
            var prefix = "";

            if(criteria.indexOf(AJAX_SEARCH_PREFIX) == 0){
                criteria = criteria.substring(criteria.indexOf(AJAX_SEARCH_PREFIX) + 1, criteria.length);
                attribute = arColorPrefixAttributeMap["DEFAULT"];
            }
            else {
                prefix = criteria.substring(0, criteria.indexOf(AJAX_SEARCH_PREFIX));
                if(hasContent(arColorPrefixAttributeMap[prefix])){
                    criteria = criteria.substring(criteria.indexOf(AJAX_SEARCH_PREFIX) + 1, criteria.length);
                    attribute = arColorPrefixAttributeMap[prefix];
                    if (hasContent(colorAttributeKeyToTypeMap[attribute])){
                        var attributeType = colorAttributeKeyToTypeMap[attribute];
                        if (attributeType == "integer" || attributeType == "float" || attributeType =="sequence" || attributeType == "currency"){
                            criteria = criteria + '<%= RANGE_DELIM %>' + criteria;
                        }
                    }
                }
                else {
                    bWorking = false;
                    return;
                }
            }

            //this criteria is for palette
            //it seems that this one never get call, because if Color Control mode = 3 Palette Only - Create Material-Color
            //a drop down box is displayed instead of a text box.
            // IF COLOR MODE == FORCE_PALETTE_COLORMODE THEN THE COLOR CAN ONLY COME FROM THE PALETTE


            var paletteCriteria = "";
            var dimId;
            dimId = cellId.substring(cellId.lastIndexOf('$') + 1, cellId.length);
            branchId = cellId.substring(1, cellId.indexOf('_'));
            var multiMode = (cellId.indexOf('$sku$') > 0)
            var colorMode = getColorControlledMode(branch,dimId,branchId,multiMode); 
            if(colorMode == FORCE_PALETTE_COLORMODE && <%= palette !=null %>){
                //alert(youMayOnlySelectFromPaletteForThisMaterialAlrt);
                paletteCriteria = "&" + arColorPrefixAttributeMap['PALETTEID'] + "=" +
                "<%= palette!=null?palette.getPersistInfo().getObjectIdentifier().getId():0 %>";
                //alert("paletteCriteria: " + paletteCriteria);
            }
            //end for palette

        }//if AJAX_SEARCH_PREFIX

        var colorTypeId = '<%= FormatHelper.getObjectId(ftColorRoot) %>';
        createPopUp('<%= FormatHelper.encodeForJavascript(searchingColorsPleaseBePatientMsg) %>',"AjaxStatus");
        //debug(branch.section);

        if(sectionCheck){
            runPostAjaxRequest(location.protocol + '//' + location.host + urlContext_masterController_url, 'activity=FIND_COLOR&action=AJAXSEARCH&' + attribute + "=" + criteria + '&sectionConstraint=' + branch.section + '&colorTypeId=' + colorTypeId + '&caseInsensitive=' + caseInsensitive + '&attCols=' + queryColumns + paletteCriteria, 'handleColorQuery');
        }
        else{
            runPostAjaxRequest(location.protocol + '//' + location.host + urlContext_masterController_url, 'activity=FIND_COLOR&action=AJAXSEARCH&' + attribute + "=" + criteria + '&colorTypeId=' + colorTypeId + '&caseInsensitive=' + caseInsensitive + '&attCols=' + queryColumns + paletteCriteria, 'handleColorQuery');
        }
    }

    //-----------------------------------------------------------------
    function handleColorQuery(xml, text){
        closeDiv("AjaxStatus");
        var count = 0;
        var countText = text.substring(text.indexOf("resultsCount:") + 13, text.indexOf("resultsCount:") + 23);
        countText = trim(countText);
        if(hasContent(countText)){
            count = parseFloat(countText);
        }
        var branch = dataModel.getBranch(queryBranchId);
        //need to find the sku master id to put in here if it is the case for SKU, use global javascript var
        var cell = document.getElementById(cellId);
        //alert('handleColorQuery: count = ' + count + ",cell id: " + cell.id);

        //CHUCK -08/21/08 Without this line, an exact match color search will cause the editor to break (js error clicking on any cell)
        //Likhit -26/11/20 This is no more the case, there is no error thrown with exact match color search when clicking on any other cell
        //setTimeout('endCellEdit()',500);
        // MULTIPLE RESULTS FOUND...
        if(count > 0){
            // MULTIPLE RESULTS (HTML RESULTS)
            var content = '';
            cell.innerHTML = content;
            var skuMode = (cellId.indexOf('$sku$') > 0)
            var skuId;
            if(skuMode){
                attKey = cellId.substring(cellId.indexOf('_') + 1, cellId.indexOf('$'));
                skuId  = cellId.substring(cellId.lastIndexOf('$') + 1, cellId.length);
                branch['colorDescription$sku$' + skuId] = content;
            } else {
                branch.colorDescription = content;
            }
            createPopUp_for_Material_Color("colorMat", text);
            return;
        }
        var rows;
        if(xml){
            //set the bWorking to false to allow the change of Cell Id
            bWorking = false;
            rows = xml.getElementsByTagName('row');
        }
        if((!rows || rows.length == 0) && count == 0){
            // NO RESULTS FOUND
            var content = cell.innerHTML;
            content = content + ': '+ '<%= FormatHelper.encodeForJavascript(notFoundMSG)%>';
            cell.innerHTML = content;
            var skuMode = (cellId.indexOf('$sku$') > 0)
            var skuId;
            if(skuMode){
                attKey = cellId.substring(cellId.indexOf('_') + 1, cellId.indexOf('$'));
                skuId  = cellId.substring(cellId.lastIndexOf('$') + 1, cellId.length);
                branch['colorDescription$sku$' + skuId] = content;
            } else {
                branch.colorDescription = content;
            }
            //set the bWorking to false to allow the change of Cell Id
            bWorking = false;
            return;
        }
        //put data to HTML display
        var colorName = getValueFromXMLRow(rows[0], 'LCSCOLOR.COLORNAME');
        cell.innerHTML = colorName;

        //put data into bom model
        document.MAINFORM.currentColorId.value = getValueFromXMLRow(rows[0], 'LCSCOLOR.IDA2A2');
        document.MAINFORM.currentColorName.value = colorName;
        var currentColorNameDiv = document.getElementById("currentColorNameDiv");
        currentColorNameDiv.innerHTML = colorName;
        currentColorNameDiv.title = colorName;

        if(!branch.persisted && !<%= ACLHelper.hasCreateAccess(type.getAttribute("colorDescription")) %>){
            return;
        }
        if(branch.persisted && !<%= ACLHelper.hasEditAccess(type.getAttribute("colorDescription")) %>){
            return;
        }
        if (!bWorking){
            // INSERT COLOR
            var skuMode = (cellId.indexOf('$sku$') > 0)
            var skuId;
            if(skuMode){
                attKey = cellId.substring(cellId.indexOf('_') + 1, cellId.indexOf('$'));
                skuId  = cellId.substring(cellId.lastIndexOf('$') + 1, cellId.length);
            } else {
                attKey = cellId.substring(cellId.indexOf('_') + 1, cellId.length);
            }
            // CHECK IS IN PALETTE
            var inPalette = false;
            var paletteName = colorPaletteIds["NAME_" + document.MAINFORM.currentColorId.value];
            var paletteRGB = colorPaletteIds["RGB_" + document.MAINFORM.currentColorId.value];
            if(hasContent(paletteName)){
                inPalette = true;
            }

            // IF COLOR MODE == FORCE_PALETTE_COLORMODE THEN
            // THE COLOR CAN ONLY COME FROM THE PALETTE
            // CHECK IT HERE AND EXIT IF NEEDED.
            if(getColorMode(branch) == FORCE_PALETTE_COLORMODE && !inPalette){
                alert(youMayOnlySelectFromPaletteForThisMaterialAlrt);
                return;
            }
            if(skuMode){
                branch['colorLinked$sku$' + skuId] = true;
                branch['colorId$sku$' + skuId] = document.MAINFORM.currentColorId.value;
                branch['colorDescription$sku$' + skuId] = htmlDecode(document.MAINFORM.currentColorName.value);
                branch['colorDescriptionDisplay$sku$' + skuId] = htmlDecode(document.MAINFORM.currentColorName.value);
                branch['colorName$sku$' + skuId] = htmlDecode(document.MAINFORM.currentColorName.value);
                branch.touched = true;
                setColorInformationIntoBOMModel(branch, skuId);
            }
            else {
                branch.colorId = document.MAINFORM.currentColorId.value;
                branch.colorName = htmlDecode(document.MAINFORM.currentColorName.value);
                branch.touched = true;
                branch.colorLinked = true;
                branch.colorDescription = htmlDecode(document.MAINFORM.currentColorName.value);
                branch.colorDescriptionDisplay = htmlDecode(document.MAINFORM.currentColorName.value);
                if(branch.override){
                    setColorInformationIntoBOMModel(branch);
                } else {
                    setColorInformationIntoBOMModelTopLevel(branch);
                }
            }
            cell.innerHTML = document.MAINFORM.currentColorName.value;
            cell.className = 'colorLinked';

            // WILL PROPOGATE THE CHANGE TO RELEVANT
            // CELLS IN THE TABLE DATA MODEL AND THE DOM.
            propogateColorChangeInTable(branch.branchId, cell.id);
            // fix for SPR#6773699 - the colorName was getting double encoded down the line.
            propagateTopColorChange(cell,branch,htmlDecode(colorName));
        }
        //end put data into bom model
    }

    function createColorPopUp(text, id){
        if ( !hasContent(id) ) {
            id = 'ajaxSearchResultPopup';
        }
        var button = "<a href=\"javascript:closeAjaxSearchResultPopup(\'" + id + "\'),clearCell(\'"+cellId +"\')\"><%= FormatHelper.encodeForJavascript(closeLabel) %></a><br><br>";
        text = button + text;

        displayAjaxSearchResultPopup(id, text);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // END AJAX COLOR QUICK SEARCH
    ////////////////////////////////////////////////////////////////////////////
    
    function createPopUp_for_Material_Color(whoCalled, textForMat, idForMat) {
    	if(whoCalled.localeCompare("colorMat") == 0) {
    		if(bMatWorking == true) {
        		clearMatCell(matCellId);
        	}
    		createColorPopUp(textForMat, idForMat);
    	}else
    	if(whoCalled.localeCompare("matMat") == 0) {
    		if(bWorking == true) {
        		clearCell(cellId);
        	}
    		createMatPopUp(textForMat, idForMat);
    	}
    }
    function initNewRow(newRow, newBranch){

        <jsp:include page="<%=subURLFolder+ CLIENT_SIDE_PLUGIN %>" flush="true" >
             <jsp:param name="type" value="<%= FormatHelper.getObjectId(type) %>"/>
             <jsp:param name="pluginType" value="BOMEditor_initNewRow"/>
        </jsp:include>
    }

//changes to calculate rowTotal with new formula - start
function calculateRowTotal(branch) {


    if (useBOMCosting) {
        var rts = branch.rowTotal;
        if (!hasContent(rts)) {
            rts = 0;
        }

        var qs = branch.quantity;
        if (!hasContent(qs)) {
            qs = 0;
        }

        var las = branch.lossAdjustment;
        if (!hasContent(las)) {
            las = 0;
        }
				
		  var cif = branch.scCIF;
        if (!hasContent(cif)) {
            cif = 0;
        }	
		
		 var calcPercentage = branch.scCalculationSizePercent;
        if (!hasContent(calcPercentage)) {
            calcPercentage = 0;
        }

		var tmplas = 1 + las;
		var tmpcalc= 1 + calcPercentage;

        var rowTotal = parseFloat(rts);
        var quantity = parseFloat(qs);
        var lossAdjustment = parseFloat(las);
		var cifValue = parseFloat(cif);
		var calcPercentageValue = parseFloat(calcPercentage);
		var grossQuantity = quantity * (1+lossAdjustment) * (1+calcPercentageValue);
        var grossQuantityValue = parseFloat(grossQuantity);
        var materialPriceCell = document.getElementById("r" + branch.branchId + "_MATERIAL_materialPrice");

        if (!materialPriceCell) {
            return;
        }
        var mps = materialPriceCell.innerHTML;
        //var mps = branch.MATERIAL_materialPrice;
        if (!hasContent(mps)) {
            mps = 0;
        } else {
            if (mps.indexOf('$') > -1) {
                mps = mps.substring(1);
            }
        }

        var pos = branch.priceOverride;
        if (!hasContent(pos)) {
            pos = 0;
        }

        var materialPrice = parseFloat(mps);
        var priceOverride = parseFloat(pos);

        if (lossAdjustment > 0) {
            quantity = quantity + (quantity * lossAdjustment);
        }

        if (priceOverride && priceOverride > 0) {
            materialPrice = priceOverride;
        }

        if (isNaN(materialPrice)) {
            materialPrice = 0;
        }

        rowTotal = quantity * materialPrice;
		rowTotal = (materialPrice + cifValue) * grossQuantityValue;
        branch.rowTotal = rowTotal;


        var cell = document.getElementById("r" + branch.branchId + "_rowTotal");
        if (cell) {
            //if(hasContent(branch.rowTotal) && branch.rowTotal > 0){
            if (hasContentAllowZero('' + branch.rowTotal)) {
                var val = format_locale(formatFloat("" + rndStrOrNumFloat(formatFloat(branch.rowTotal), branchTotalDecimalPlaces), branchTotalDecimalPlaces));
                //cell.innerHTML = val;

                //Remove current value
                while (cell.childNodes.length > 0) {
                    cell.removeChild(cell.childNodes[0]);
                }
                //add new value
                var txtNode = document.createTextNode(val);
                cell.appendChild(txtNode);
            }
        }

    }
}
//changes to calculate rowTotal with new formula - end

    function handleCtrlClick(cell){
        var id = cell.id;
        var skuMode = (id.indexOf('$sku$') > 0)
        var attKey;
        var branchId;
        var branch;
        var skuId;
        if(skuMode){
            attKey = id.substring(id.indexOf('_') + 1, id.indexOf('$'));
            skuId = id.substring(id.lastIndexOf('$') + 1, id.length);
            branchId = id.substring(1, id.indexOf('_'));
            branch = dataModel.getBranch(branchId);
        } else {
            attKey = id.substring(id.indexOf('_') + 1, id.length);
            branchId = id.substring(1, id.indexOf('_'));
            branch = dataModel.getBranch(branchId);
        }

        if(attKey == 'materialDescription'){
            if(!branch.persisted && !<%= ACLHelper.hasCreateAccess(type.getAttribute("materialDescription")) %>){
                return;
            }
            if(branch.persisted && !<%= ACLHelper.hasEditAccess(type.getAttribute("materialDescription")) %>){
                return;
            }

            if(!hasContent(branch.dimensionName)){
                //is topLevel...collapse if expanded
                var topLevelBranch = dataModel.getBranch(branch.branchId);
                if(topLevelBranch.expanded){
                    expandBranch(branchId);
                }
            }

            // INSERT MATERIAL
            if(hasContent(document.MAINFORM.currentMaterialId.value)){
                if(document.MAINFORM.currentColorControlled.value == FORCE_PALETTE_COLORMODE &&
                    !checkForNonPaletteColors(branch)){
                    return false;
                }
                // BRIAN ADDED TO CORRECT ISSUES WHERE CURRENT CELL IS CLICKED...
                endCellEdit();
                assignMaterial('MASTERID' + AJAX_SEARCH_PREFIX + document.MAINFORM.currentMaterialId.value, branch.branchId, false);
            }
            return true;
        } else if (attKey == 'wcPartName'){

            if(!branch.persisted && !<%= ACLHelper.hasCreateAccess(type.getAttribute("wcPartName")) %>){
                return;
            }
            if(branch.persisted && !<%= ACLHelper.hasEditAccess(type.getAttribute("wcPartName")) %>){
                return;
            }

            if(!hasContent(branch.dimensionName)){
                //is topLevel...collapse if expanded
                var topLevelBranch = dataModel.getBranch(branch.branchId);
                if(topLevelBranch.expanded){
                    expandBranch(branchId);
                }
            }

            branch.wcPartName = '';
            branch.wcPartNameDisplay= '';
            branch.wcPartId = '0';
            branch.touched = true;

            // handle WHERE CURRENT CELL IS CLICKED...
            endCellEdit();

            var cell = document.getElementById("r" + branchId + "_wcPartName");
            if(cell != null){
                cell.innerHTML = '';
            }

            handleWCPartAssignment(branch);


        }else if(attKey == 'colorDescription'){
            //console.log("handleCtrlClick: attKey = " + attKey);

            if(!branch.persisted && !<%= ACLHelper.hasCreateAccess(type.getAttribute("colorDescription")) %>){
                return;
            }
            if(branch.persisted && !<%= ACLHelper.hasEditAccess(type.getAttribute("colorDescription")) %>){
                return;
            }

            var msId = branch.materialSupplierMasterId;
            var materialColorList = materialColorMap['M' + msId];
            var matColObj;
            for(var i = 0; materialColorList && i < materialColorList.length; i++){
                if(materialColorList[i].colorId == document.MAINFORM.currentColorId.value){
                    matColObj = materialColorList[i];
                }
            }
            // INSERT COLOR
            if(hasContent(document.MAINFORM.currentColorId.value)){
                //dmf nike below
                if(getColorMode(branch) == MATCOL_SELECT_COLORMODE || branch.colorControlled == '4'){
                     var value = document.MAINFORM.currentColorId.value
                     var display = document.MAINFORM.currentColorName.value;
                    // RETURNING FALSE MEANS THAT THE CTRL-CLICK
                    // HANDLER IS RETURNING CONTROL BACK TO
                    // THE STANDARD CELL EDITING LOGIC. THIS
                    // IS WHAT WE WANT HERE BECAUSE SELECTION
                    // WILL OCCUR VIA A DROP DOWN LIST
                    // EDIT WAS MADE TO A COLOWAY-COLOR COLUMN
                    if(skuMode){
                        if(matColObj != null){
                            branch['colorId$sku$' + skuId] = value;
                            branch['colorName$sku$' + skuId] = display;
                            branch.touched = true;
                            branch['colorLinked$sku$' + skuId] = true;
                            branch['colorDescription$sku$' + skuId] = display;
                            branch['colorDescriptionDisplay$sku$' + skuId] = display;
                            cell.className = 'colorLinked';
                            cell.innerHTML = HTMLEncoder.encodeForHTMLContent(display);
                            branch['materialColorId$sku$' + skuId] = matColObj.materialColorId;
                            branch['MATERIAL_materialPriceDisplay'] = matColObj.price;
                            branch['MATERIAL_materialPrice'] = matColObj.price;
                            if(branch.override){
                                var priceCell = document.getElementById('r' + branch.branchId + "_MATERIAL_materialPrice");
                                if(priceCell){
                                    priceCell.innerHTML = matColObj.price;
                                }
                                calculateRowTotal(branch);
                            }
                            setColorInformationIntoBOMModel(branch, skuId);
                            propogateColorChangeInTable(branchId, id);
                        } else {
                            return false;
                        }
                    // EDIT WAS MADE TO THE COLOR COLUMN
                    } else {
                        if(matColObj != null){
                            branch.colorId = value;
                            branch.colorName = display;
                            branch.touched = true;
                            branch.colorLinked = true;
                            branch.colorDescription = display;
                            branch.colorDescriptionDisplay = display;
                            cell.className = 'colorLinked';
                            cell.innerHTML = HTMLEncoder.encodeForHTMLContent(display);
                            branch.materialColorId = matColObj.materialColorId;
                            matPrice = matColObj.price;
                            branch['MATERIAL_materialPriceDisplay'] = matPrice;
                            branch['MATERIAL_materialPrice'] = matPrice;
                            var priceCell = document.getElementById('r' + branch.branchId + "_MATERIAL_materialPrice");
                            if(priceCell){
                                priceCell.innerHTML = matPrice;
                            }

                            calculateRowTotal(branch);
                            if ( branch.override ) {
                                //alert("override");
                                setColorInformationIntoBOMModel(branch);
                            } else {
                                setColorInformationIntoBOMModelTopLevel(branch);
                                // ADD TO PROPAGATE COLORWAY CELLS
                                propagateTopColorChange(cell,branch,display);
                            }
                            // PROPOGATE CHANGES TO OTHER TABLE CELLS
                            propogateColorChangeInTable(branchId, id);
                        } else {
                           return false;
                        }
                    }
                    return true;
                }// END DROP DOWN COLOR CONTROL CHECK

                // CHECK IS IN PALETTE
                var inPalette = false;
                var paletteName = colorPaletteIds["NAME_" + document.MAINFORM.currentColorId.value];
                var paletteRGB = colorPaletteIds["RGB_" + document.MAINFORM.currentColorId.value];
                if(hasContent(paletteName)){
                    inPalette = true;
                }

                // IF COLOR MODE == FORCE_PALETTE_COLORMODE THEN
                // THE COLOR CAN ONLY COME FROM THE PALETTE
                // CHECK IT HERE AND EXIT IF NEEDED.
                if(getColorMode(branch) == FORCE_PALETTE_COLORMODE && !inPalette){
                    alert(youMayOnlySelectFromPaletteForThisMaterialAlrt);
                    return true;
                }


                if(skuMode){
                    branch['colorLinked$sku$' + skuId] = true;
                    branch['colorId$sku$' + skuId] = document.MAINFORM.currentColorId.value;
                    branch['colorDescription$sku$' + skuId] = htmlDecode(document.MAINFORM.currentColorName.value);
                    branch['colorDescriptionDisplay$sku$' + skuId] = htmlDecode(document.MAINFORM.currentColorName.value);
                    branch['colorName$sku$' + skuId] = htmlDecode(document.MAINFORM.currentColorName.value);
                    branch.touched = true;
                    if(matColObj != null){
                        branch['materialColorId$sku$' + skuId] = matColObj.materialColorId;
                    } else {
                        branch['materialColorId$sku$' + skuId] = 0;
                    }
                    setColorInformationIntoBOMModel(branch, skuId);
                } else {
                    branch.colorId = document.MAINFORM.currentColorId.value;
                    branch.colorName = htmlDecode(document.MAINFORM.currentColorName.value);
                    branch.touched = true;
                    branch.colorLinked = true;
                    branch.colorDescription = htmlDecode(document.MAINFORM.currentColorName.value);
                    branch.colorDescriptionDisplay = htmlDecode(document.MAINFORM.currentColorName.value);
                    if(matColObj != null){
                        branch.materialColorId = matColObj.materialColorId;
                    } else {
                        branch.materialColorId = 0;
                    }
                    if(branch.override){
                        setColorInformationIntoBOMModel(branch);
                    } else {
                        setColorInformationIntoBOMModelTopLevel(branch);
                    }
                }
                endCellEdit();
                cell.innerHTML = HTMLEncoder.encodeForHTMLContent(htmlDecode(document.MAINFORM.currentColorName.value));
                cell.className = 'colorLinked';
            }

            // dmf nike below
            if(getColorMode(branch) == MATCOL_SELECT_COLORMODE  ||  branch.colorControlled == '4'){
                //console.log('returning false from ctrl click');
                return false;
            }
            propogateColorChangeInTable(branchId, id);
            propagateTopColorChange(cell,branch,htmlDecode(cell.innerHTML));
            return true;
        }
        return false;
    }

function handleCtrlShiftClick(cell){
        console.log("handleCtrlShiftClick");
        var id = cell.id;
        var skuMode = (id.indexOf('$sku$') > 0)

        var attKey;
        var branchId;
        var branch;
        var skuId;
        if(skuMode){
            attKey = id.substring(id.indexOf('_') + 1, id.indexOf('$'));
            skuId = id.substring(id.lastIndexOf('$') + 1, id.length);
            branchId = id.substring(1, id.indexOf('_'));
            branch = dataModel.getBranch(branchId);
        } else {
            attKey = id.substring(id.indexOf('_') + 1, id.length);
            branchId = id.substring(1, id.indexOf('_'));
            branch = dataModel.getBranch(branchId);
        }

        if(attKey == 'materialDescription'){
            console.log("ctrl click on material");
            if(!branch.persisted && !<%= ACLHelper.hasCreateAccess(type.getAttribute("materialDescription")) %>){
                return;
            }
            if(branch.persisted && !<%= ACLHelper.hasEditAccess(type.getAttribute("materialDescription")) %>){
                return;
            }

            if(!hasContent(branch.dimensionName)){
                //is topLevel...collapse if expanded
                var topLevelBranch = dataModel.getBranch(branch.branchId);
                if(topLevelBranch.expanded){
                    expandBranch(branchId);
                }
            }
                //cell.className = "";

                if(skuMode){
                    clearColorInfo(branch);
                    branch['childId$sku$' + skuId] = dataModel.placeholderChildId;
                    branch['materialSupplierMasterId$sku$' + skuId] = dataModel.placeholderChildId;
                    branch['materialName$sku$' + skuId] = '';
                    branch['materialLinked$sku$' + skuId] = false;
                    branch['materialDescription$sku$' + skuId] = '';
                    branch['colorControlled$sku$' + skuId] = '';
                    branch['supplierDescription$sku$' + skuId] = '';

                } else {
                    clearColorInfo(branch);
                    branch.childId = dataModel.placeholderChildId;
                    branch.materialName = '';
                    branch.materialLinked = false;
                    branch.materialDescription = '';
                    branch.materialSupplierMasterId = '';
                    branch.materialSupplierVersionId = '';
                    branch.supplierName = '';
                    branch.colorControlled = '';
                    branch.supplierDescription = '';
                }
                branch.touched = true;

                // BRIAN ADDED TO CORRECT ISSUES WHERE CURRENT CELL IS CLICKED...
                endCellEdit();

                cell.innerHTML = '';
                //Chuck added
                var cell = document.getElementById("r" + branchId + "_materialPrice");
                if(cell != null)cell.innerHTML = '';

                var cell = document.getElementById("r" + branchId + "_unitOfMeasure");
                if(cell != null)cell.innerHTML = '';

                var cell = document.getElementById("r" + branchId + "_supplierName");
                if(cell != null)cell.innerHTML = '';

                //CHUCK - NEED TO CLEAR OUT ALL CELLS FOR THE MATERIAL/SUPPLIER
                for(var n in viewColumnKeys){
                    if(hasContent(branch[n])){
                        var cell = document.getElementById("r" + branchId + "_" + n);
                        if(cell){
                            cell.innerHTML = '';
                        }
                    }
                }

                //if(branch.masterBranch){
                    // CLEARS A SUB ASSEMBLY
                    deleteSubAssembly(branch);
                    //currentRow = document.getElementById("r" + branch.branchId);
                //}
                handleNewMaterialAssignment(branch);
                //end chuck added

            return true;

        }else if(attKey == 'colorDescription'){
            endCellEdit();
            //console.log("handleCtrlClick: attKey = " + attKey);

            if(!branch.persisted && !<%= ACLHelper.hasCreateAccess(type.getAttribute("colorDescription")) %>){
                return;
            }
            if(branch.persisted && !<%= ACLHelper.hasEditAccess(type.getAttribute("colorDescription")) %>){
                return;
            }

            var msId = branch.materialSupplierMasterId;
            var materialColorList = materialColorMap['M' + msId];
            var matColObj;
            var value = document.MAINFORM.currentColorId.value;
            for(var i = 0; materialColorList && i < materialColorList.length; i++){
                if(materialColorList[i].colorId == value){
                    matColObj = materialColorList[i];
                }
            }

            // INSERT COLOR
            if(skuMode){
                branch['colorLinked$sku$' + skuId] = false;
                branch['colorId$sku$' + skuId] = '0';
                branch['colorDescription$sku$' + skuId] = '';
                branch['colorDescriptionDisplay$sku$' + skuId] = '';
                branch['colorName$sku$' + skuId] = '';
                branch['materialColorId$sku$' + skuId] = '0';
                branch.touched = true;
                setColorInformationIntoBOMModel(branch, skuId);
            } else if(!skuMode || getColorMode(branch) == MATCOL_SELECT_COLORMODE  ||  branch.colorControlled == '4') {
                branch.colorId = '0';
                branch.materialColorId = '0';
                branch.colorName = '';
                branch.touched = true;
                branch.colorLinked = false;
                branch.colorDescription = '';
                branch.colorDescriptionDisplay = '';
                setColorInformationIntoBOMModel(branch);
                if(branch.override){
                    setColorInformationIntoBOMModel(branch);
                } else {
                    setColorInformationIntoBOMModelTopLevel(branch);
                }

            }

            currentCell = '';   // ADDED THIS LINE TO FIX ISSUE WITH NULL CURRENT CELL
            cell.innerHTML = '';

            propogateColorChangeInTable(branchId, id);
            propagateTopColorChange(cell,branch,cell.innerHTML);

            return true;
        }

        return false;
    }


    /////////////////////////////////////////////////////////////////////////////////////
    function insertMaterial(id, name, colorMode){
        if(currentRow && !<%= "materialDescription".equals(multiAttribute) %>){
            //materialColorMode = colorMode;
            var branch = getBranchFromRow(currentRow);
            var criteria = "VERSIONID" + AJAX_SEARCH_PREFIX + id;
            //alert("criteria = " + criteria);
            findMaterial(criteria, branch);
            enableClear('clearMaterialDiv');
            return;
        } else {
            //materialColorMode = colorMode;
            var branch = null;
            var criteria = "VERSIONID" + AJAX_SEARCH_PREFIX + id;
            //alert("criteria = " + criteria);
            findMaterial(criteria, branch);

            //alert( '<%= FormatHelper.encodeForJavascript(noLineItemSelectInBOMAlrt) %>'+ "\n" +
            //      '<%= FormatHelper.encodeForJavascript(eitherSelectALineItemOrAddANewLineItemAlrt) %>' + "\n\n" +
            //      '<%= FormatHelper.encodeForJavascript(toAddANewLineItemInstructionAlrt) %>' + "\n" +
            //      '<%= FormatHelper.encodeForJavascript(thenTryInsertingAMaterialAlrt) %>');
        }
       enableClear('clearMaterialDiv');
    }
    /////////////////////////////////////////////////////////////////////////////////////
    function checkForNonPaletteColors(branch){

        if(<%= "colorDescription".equals(multiAttribute) %>){
            // MULTIPLE COLOR MODE
            <%
            Iterator skuIter = skuList.iterator();
            FlexObject skuObj;
            while(skuIter.hasNext()){
                skuObj = (FlexObject) skuIter.next();
                String tempKey = "colorId$sku$" + skuObj.getString(skuIDIndex);
                %>
                if(hasContent(branch.colorName$sku$<%= skuObj.getString(skuIDIndex) %>) && !hasContent(colorPaletteIds["NAME_" + branch.<%= tempKey %>])){
                    <%// CDB-fix compound message %>
                    alert('<%= FormatHelper.encodeForJavascript(thisColorIsNotInThePaletteAlrt) %>' + branch.colorName$sku$<%= skuObj.getString(skuIDIndex) %> + '<%= FormatHelper.encodeForJavascript(youMayNotUseThisMaterialWithColorsNotInThePaletteAlrt) %>');
                    return false;
                }
                <%
            }
            %>


        } else {
            // SINGLE COLOR MODE
            if(hasContent(branch.colorName) && !hasContent(colorPaletteIds["NAME_" + branch.colorId])){
                alert('<%= FormatHelper.encodeForJavascript(thisColorIsNotInThePaletteAlrt) %>' + HTMLEncoder.encodeForHTMLContent(branch.colorName) + '<%= FormatHelper.encodeForJavascript(youMayNotUseThisMaterialWithColorsNotInThePaletteAlrt) %>');
                return false;
            }
        }
        return true;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //--------------------------------------------------------------------------

var divWindow;
var jswWidth = 300;
var jswHeight = 200;

    function save(){
        if(getLastCheckedValidationStatus() === false){
            return;
        }
        //Story: B-104545
        if(<%=includeSetAsPrimary%> && needClearOut()){
            if(!autoSetPrimaryMaterial) {
                if(confirm(clearConfirmMSG)){
                    RFABOM.clearPrimaryMaterial();
                }else{
                    return;
                }
            } else {
                RFABOM.clearPrimaryMaterial();
            }
        }

        var confirmation = true;
        if(!<%= USE_AJAX_SAVE %>){
            confirmation = confirm('<%= FormatHelper.encodeForJavascript(confirmSaveChangesMessage) %>');
        }

        if(validate() && confirmation){
            endCellEdit();
            var dataString = getDataString();
            //debug(dataString); //return;
            if(dataString != null){
                if (<%= USE_AJAX_SAVE %>) {
                    //grayOut(true, {'zindex':'2', 'opacity':'25'});
                    var cenPos = getCenteredPosition(jswWidth, jswHeight);
                    var cenx = cenPos["x"];
                    var ceny = cenPos["y"];

                    divWindow = new jsWindow(jswWidth, jswHeight, ceny, cenx, 30, "", 20, true, false, false, true, true);
                    divWindow.showProcessingMessage();


                    //return;

                    var parameters = 'action=SAVE_ALT'
                                      + '&oid=<%= FormatHelper.encodeForJavascript(partOid) %>'
                                      + '&seasonId=<%= seasonId %>'
                                      + '&ownerId=<%= ownerId %>'
                                      + '&dataString='+ encodeURIComponent(dataString)
                                      + '&typesString='+ document.MAINFORM.typesString.value;
    
                                      var formDataParam = '';
                                      <%
                                      FlexTypeAttribute bomatt = null;
                                      while(bomHeaderAttsIterTemp.hasNext()){
                                        bomatt = (FlexTypeAttribute)bomHeaderAttsIterTemp.next();
                                        if(!"multiobject".equals(bomatt.getAttVariableType()) && !"iteratedmultiobject".equals(bomatt.getAttVariableType()) && !"derivedString".equals(bomatt.getAttVariableType()) && !"image".equals(bomatt.getAttVariableType()) && !"sequence".equals(bomatt.getAttVariableType()) && !"discussion".equals(bomatt.getAttVariableType()) && !bomatt.isAttHidden()  &&  !FormatHelper.hasContent(bomatt.getAttDerivedFrom())){
                                          %>
                                        if(document.MAINFORM.<%=bomatt.getAttributeName()%> != null || document.MAINFORM.<%=bomatt.getAttributeName()%>DateString != null){

                                        <%if("date".equals(bomatt.getAttVariableType())){%>
                                            formDataParam +='&<%=bomatt.getAttributeName()%>DateString=' + document.MAINFORM.<%=bomatt.getAttributeName()%>DateString.value;
                                        <%}else{%>
                                            formDataParam +='&<%=bomatt.getAttributeName()%>=' + encodeURIComponent(document.MAINFORM.<%=bomatt.getAttributeName()%>.value);
                                        <%} %>
                                        }
                                      <%}%>
                                      <%}%>
                                      parameters +=formDataParam;
                    var headerAttParams = getHeaderAttParams();
                    //debug('headerAttParams = ' + headerAttParams);
                    parameters = parameters + headerAttParams;

                    runPostAjaxRequest('<%=AJAX_PAGE%>',parameters ,'ajaxSaveFinish');
                }
                else {
                    document.MAINFORM.activity.value = 'EDIT_BOM';
                    document.MAINFORM.action.value = 'SAVE_ALT';
                    document.MAINFORM.oid.value = '<%= FormatHelper.encodeForJavascript(partOid) %>';
                    document.MAINFORM.seasonId.value = '<%= seasonId %>';
                    document.MAINFORM.ownerId.value = '<%= ownerId %>';
                    document.MAINFORM.dataString.value = dataString;
                    submitForm('contentframe');
                }
                //document.getElementById('debugText').value = dataString;
            }
        }
    }


 

    //--------------------------------------------------------------------------
    /*
    function ajaxSave(){
        var confirmation = true;
        if(!<%= USE_AJAX_SAVE %>){
            confirmation = confirm('<%= FormatHelper.encodeForJavascript(confirmSaveChangesMessage) %>');
        }

        if(confirmation){
            endCellEdit();
            var dataString = getDataString();
            if(dataString != null  && validate()){
                createPopUp('<%=FormatHelper.encodeForJavascript(savingBOMPleaseBePatientMsg) %>',"AjaxStatus");
                runPostAjaxRequest('<%=AJAX_PAGE%>','action=SAVE_ALT'
                                            + '&oid=<%= FormatHelper.encodeForJavascript(partOid) %>'
                                            + '&seasonId=<%= seasonId %>'
                                            + '&ownerId=<%= ownerId %>'
                                            + '&dataString='+ encodeURIComponent(dataString)
                                            + '&typesString='+ document.MAINFORM.typesString.value,'ajaxSaveFinish');
            }
        }
    }
    */
    //--------------------------------------------------------------------------
    function ajaxSaveFinish(xml, text){
        //grayOut(false);
        var cenPos = getCenteredPosition(jswWidth, jswHeight);
        var cenx = cenPos["x"];
        var ceny = cenPos["y"];

        divWindow = new jsWindow(jswWidth, jswHeight, ceny, cenx, 30, "", 20, true, false, true, false, true);
        closeDiv("AjaxStatus");

        var status ='Data String is too long. No XML returned.';
        if (xml != null && xml != undefined)
            status = xml.getElementsByTagName("status")[0].firstChild.data;
        if ("SUCCESS" == status ) {
            //createPopUp ('<p><font color="#0000FF"><%=FormatHelper.encodeForJavascript(bomIsSavedMessage)%></font></p>');
            divWindow.addHTML('<div class="LABEL" style="text-align:center"><br><br><br><%=FormatHelper.encodeForJavascript(bomIsSavedMessage)%></div>');
            // NO CONFIRMATION NEEDED.... JUST CLOSE WINDOW
            setTimeout('closeDivWindow(true)', 1000);
        }
        else {
            status = HTMLEncoder.encodeForHTMLContent(status);
            //createPopUp ('<%= FormatHelper.encodeForJavascript(errorMsg)%><p><font color="#FF0000">' + status + '</font></p>');
            divWindow.addHTML('<%= FormatHelper.encodeForHTMLContent(errorMsg)%><p><font color="#FF0000">' + status + '</font></p>');
        }
    }



    //--------------------------------------------------------------------------
    function doNothing(xml,text){
     }
    //--------------------------------------------------------------------------
    function refreshBOM(xml, text) {
        var bomFresh = xml.getElementsByTagName("bomFresh")[0].firstChild.data;
        //if bomFresh = null, it will be assigned true from the server side
        if (bomFresh != 'true') {
            //set it to refreshing
            runPostAjaxRequest('<%=AJAX_PAGE%>','action=SET_BOM_FRESH&bom=<%= FormatHelper.encodeForJavascript(partOid) %>&fresh=refreshing','doNothing');

            //alert ("Bom is not fresh.");
            document.MAINFORM.activity.value = 'EDIT_BOM';
            document.MAINFORM.action.value = 'INIT';
            document.MAINFORM.returnActivity.value = 'VIEW_SEASON_PRODUCT_LINK';
            document.MAINFORM.returnAction.value = 'INIT';
            document.MAINFORM.tabPage.value = '<%= FormatHelper.encodeForJavascript(tabPage) %>';
            setReturnOid();
            submitForm();
        }


    }
    //--------------------------------------------------------------------------
    window.onunload = function (){
        runPostAjaxRequest('<%=AJAX_PAGE%>','action=SET_BOM_FRESH&bom=<%= FormatHelper.encodeForJavascript(partOid) %>&fresh=false','doNothing');
    }
    //--------------------------------------------------------------------------
    //////////////////////////////////////////////////////////////////////////////////////
    function saveAndDone(){
        if(getLastCheckedValidationStatus() === false){
            return;
        }
        //Story: B-104545
        if(<%=includeSetAsPrimary%> && needClearOut()){
            if(!autoSetPrimaryMaterial) {
                if(confirm(clearConfirmMSG)){
                    RFABOM.clearPrimaryMaterial();
                }else{
                    return;
                }
            } else {
                RFABOM.clearPrimaryMaterial();
            }
        }
        if(validate() && confirm('<%= FormatHelper.encodeForJavascript(confirmSaveAndCheckInMessage) %>')){
            //debug('1');
            endCellEdit();
            //debug('2');
            //debug('3');
            var dataString = getDataString();
            if(dataString != null){
            //debug('4');
                document.MAINFORM.activity.value = 'EDIT_BOM';
                document.MAINFORM.action.value = 'CHECK_IN_ALT';
                document.MAINFORM.oid.value = '<%= FormatHelper.encodeForJavascript(partOid) %>';
                document.MAINFORM.seasonId.value = '<%= seasonId %>';
                document.MAINFORM.ownerId.value = '<%= ownerId %>';
                document.MAINFORM.dataString.value = dataString;
            //debug('5');

                   setReturnOid();
            //debug('6');
                submitForm('contentframe');

                var cenPos = getCenteredPosition(jswWidth, jswHeight);
                var cenx = cenPos["x"];
                var ceny = cenPos["y"];

                divWindow = new jsWindow(jswWidth, jswHeight, ceny, cenx, 30, "", 20, true, false, false, true, true);
                //divWindow = new jsWindow(jswWidth, jswHeight, ceny, cenx, 30, "", 20, true, false, false);
                divWindow.showProcessingMessage();
               //debug('7');
            }
            //displayWorkingMessage();
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////
    function cancel(){
        if(confirm('<%= FormatHelper.encodeForJavascript(confirmCloseEditorMessage) %>')){
            document.MAINFORM.activity.value = 'EDIT_BOM';
            document.MAINFORM.action.value = 'CLOSE';
            document.MAINFORM.oid.value = '<%= FormatHelper.encodeForJavascript(partOid) %>';
            setReturnOid();
            submitForm('contentframe');

            var cenPos = getCenteredPosition(jswWidth, jswHeight);
            var cenx = cenPos["x"];
            var ceny = cenPos["y"];

            divWindow = new jsWindow(jswWidth, jswHeight, ceny, cenx, 30, "", 20, true, false, false, true, true);
            divWindow.showProcessingMessage();

        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    function setReturnOid() {
      <% if(ownerMaster instanceof LCSPartMaster ) { %>
         document.MAINFORM.returnOid.value = '<%= FormatHelper.encodeForJavascript(appContext.getActiveProductId()) %>';
         document.MAINFORM.returnActivity.value = "VIEW_PRODUCT";
         document.MAINFORM.tabPage.value = "<%= FormatHelper.encodeForJavascript(tabPage) %>";
      <% } else { %>
         //document.MAINFORM.returnOid.value = '<%= FormatHelper.getVersionId((RevisionControlled)VersionHelper.getVersion(ownerMaster, "A")  )%>';
         document.MAINFORM.returnOid.value = '<%= FormatHelper.getVersionId( (RevisionControlled) VersionHelper.latestIterationOf(ownerMaster) )%>';
         document.MAINFORM.returnActivity.value = "VIEW_MATERIAL";
         document.MAINFORM.tabPage.value = "<%= FormatHelper.encodeForJavascript(tabPage) %>";
      <% } %>
    }

    //////////////////////////////////////////////////////////////////////////////////////
    function validate(){
        <%
            flexg.setScope(com.lcs.wc.flexbom.FlexBOMFlexTypeScopeDefinition.BOM_SCOPE);
            flexg.setUpdate(true);
            if(updateName){
        %>
        <%= flexg.drawFormValidation(bomPart.getFlexType().getAttribute("name")) %>
        <%}%>
        <%= flexg.generateFormValidation(bomPart) %>
        <%
            flexg.setScope(null);
        %>
        return true;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    function getDataString( keepState ){

        var dataString = new quickDoc();
        var branch;
        var branches = bomDataModel.getData();
        var bomModelLength = branches.length;
        var changeMade = false;
        var changedAttributesKey = new Array();
        for (n in branches){
            branch = branches[n];
            if(branch.dropped && !branch.persisted){
                continue;
            }
            changeMade = false;
            if(branch.touched){

                //debug("branch touched = " + branch.dimensionId);
                var tempString = new quickDoc();
                tempString.write('<%= MultiObjectHelper.DROPPED %><%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + branch.dropped + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                tempString.write('<%= MultiObjectHelper.ID %><%= MultiObjectHelper.NAME_VALUE_DELIMITER %>-PARENT:com.lcs.wc.flexbom.FlexBOMPartMaster:<%= FormatHelper.getNumericObjectIdFromObject((WTObject)bomPart.getMaster()) %>-REV:<%= bomPart.getVersionIdentifier().getValue() %>-' + branch.dimensionId + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                tempString.write('branchId<%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + branch.branchId + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                if(hasContent(branch.dimensionName)){
                    tempString.write('dimensionName<%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + branch.dimensionName + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                }
                if(hasContent(branch.sourceDimensionId)){
                    tempString.write('sourceDimensionId<%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + branch.sourceDimensionId + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                }
                if(hasContent(branch.colorDimensionId)){
                    tempString.write('colorDimensionId<%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + branch.colorDimensionId + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                }
                if(hasContent(branch.size1DimensionId)){
                    tempString.write('size1<%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + branch.size1DimensionId + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                }
                if(hasContent(branch.size2DimensionId)){
                    tempString.write('size2<%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + branch.size2DimensionId + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                }
                if(hasContent(branch.destinationDimensionId)){
                    tempString.write('destinationDimensionId<%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + branch.destinationDimensionId + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                }
                if(bomDataModel.hasValueChanged(branch.dimensionId, 'materialSupplierMasterId') || branch.persisted == false){
                    tempString.write('materialSupplierMasterId<%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + branch.materialSupplierMasterId + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                    tempString.write('childId<%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + branch.materialSupplierMasterId + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                    changeMade = true;
                    changedAttributesKey[changedAttributesKey.length]='materialSupplierMasterId';
                }
                if((bomDataModel.hasValueChanged(branch.dimensionId, 'sortingNumber')  || branch.persisted == false )&& (isNumber(branch.branchId) || branch.branchId.indexOf('-') < 0)){
                    tempString.write('sortingNumber<%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + format(branch.sortingNumber) + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                    changeMade = true;
                    changedAttributesKey[changedAttributesKey.length]='sortingNumber';
                }

                if( bomDataModel.hasValueChanged(branch.dimensionId, 'colorId') || branch.persisted == false){

                    tempString.write('colorId<%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + format(branch.colorId) + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                    changeMade = true;
                    changedAttributesKey[changedAttributesKey.length]='colorId';

                }
                else{//colorId is not changed -- but for non-material override branch, if parent material is changed, the material color needs update.

                    var branchDimId = branch.dimensionId;
                    if ( (branchDimId.indexOf('SKU'))>-1 && format(branch['materialSupplierMasterId']) == format(dataModel.placeholderChildId)){

                        var parentBranchId = branchDimId.substring(0, branchDimId.indexOf('-'));
                        var parentBranch = bomDataModel.getBranch(parentBranchId);
                        if (hasContent(parentBranchId) && bomDataModel.hasValueChanged(parentBranch.dimensionId, 'materialSupplierMasterId')){
                            tempString.write('colorId<%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + format(branch.colorId) + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                    changeMade = true;
                    changedAttributesKey[changedAttributesKey.length]='colorId';
                        }
                    }
                }

                if(bomDataModel.hasValueChanged(branch.dimensionId, 'materialColorId') || branch.persisted == false){
                    tempString.write('materialColorId<%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + format(branch.materialColorId) + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                    changeMade = true;
                    changedAttributesKey[changedAttributesKey.length]='materialColorId';
                }
                if(bomDataModel.hasValueChanged(branch.dimensionId, 'masterBranchId') || branch.persisted == false){
                    tempString.write('masterBranchId<%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + format(branch.masterBranchId) + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                    changeMade = true;
                    changedAttributesKey[changedAttributesKey.length]='masterBranchId';
                }
                if(bomDataModel.hasValueChanged(branch.dimensionId, 'masterBranch') || branch.persisted == false){
                    tempString.write('masterBranch<%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + format(branch.masterBranch) + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                    changeMade = true;
                    changedAttributesKey[changedAttributesKey.length]='masterBranch';
                }
                if(bomDataModel.hasValueChanged(branch.dimensionId, 'wcPartId') || branch.persisted == false){
                    tempString.write('wcPartId<%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + format(branch.wcPartId) + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                    changeMade = true;
                    changedAttributesKey[changedAttributesKey.length]='wcPartId';
                }

                <%
                Iterator allAtts = atts.iterator();
                String tempKey = "";
                // LOOP THROUGH FLEX ATTRIBUTES
                while(allAtts.hasNext()){
                    att = (FlexTypeAttribute) allAtts.next();

                    if ( BOMEditorUtility.isInViewColumns("BOM", att.getAttKey(), columnList) && att.isAttRequired() && ACLHelper.hasModifyAccess(att)){
                %>
                         //check if the required attribute is not null
                        if (!branch.dropped && !hasContent(branch.dimensionName) && !bomDataModel.hasValueForAtt(branch.dimensionId, '<%=att.getAttKey()%>')){

                            var attName = '<%=att.getAttDisplay()%>';
                            alert (mustEnterValueForMSG + attName);
                            return null;
                        }
                   <%}
                    if("composite".equals(att.getAttVariableType())){
                   %>
                        var total = tallyCompositMOA(branch.<%= att.getAttKey() %>);
                        if(total > 0 && total != 100){
                          alert(inputErrorMSG + "\n" +
                          fieldLabelMSG + name + "\n\n" +
                                currentCompositeIsMSG + total +"\n" +
                                totalMustBe100PercentMSG + "\n \n");
                          return null;
                       }

                    <%}%>
                    // ONLY VALUES THAT HAVE CHANGED ARE PASSED.
                    if(bomDataModel.hasValueChanged(branch.dimensionId, '<%= att.getAttKey() %>') || branch.persisted == false){
                        changeMade = true;
                        tempString.write('<%= att.getAttKey() %><%= MultiObjectHelper.NAME_VALUE_DELIMITER %>' + format(branch['<%= att.getAttKey() %>']) + '<%= MultiObjectHelper.ATTRIBUTE_DELIMITER %>');
                        changedAttributesKey[changedAttributesKey.length]='<%= att.getAttKey() %>';
                    }
                    <%
                }
                %>
                if(changeMade || branch.dropped){
                    dataString.write(tempString.toString());
                    dataString.write('<%= MultiObjectHelper.ROW_DELIMITER %>'); // ENTRY DELIMETER

                    //put the changes into the originalBranch for the next saving behavior
                    for(changedAtt in changedAttributesKey){
                        bomDataModel.updateOriginalDataModel(branch.dimensionId, changedAttributesKey[changedAtt]);
                    }

                }
            }
            //////////////////////////////////////////
            // ADDITIONS FOR AJAX SAVING
            //////////////////////////////////////////
            if(!keepState){
                branch.touched = false;
                branch.persisted = true;
                if(branch.dropped && branch.persistent){

                    // REMOVE BRANCH FROM BOM DM, AND TABLE DM
                    // MAKE SURE ALL OVERRIDES ARE GONE
                    // RECALCULATE MAX BRANCH ID: IF A WIP BRANCH IS DROPPED, IT IS GONE FROM THE DB, SO THE SERVER SIDE LOGIC WILL REUSE THE BRANCH NUMBER
                }
            }
            changedAttributesKey = new Array();
        }
        return dataString.toString();
    }


    //////////////////////////////////////////////////////////////////////////////////////
    /** Calculates the bom total. Will also handle section roll ups real time as well.
     */
    function calculateBOMTotal(hardRecalc){

    <% if(useBOMCosting){ %>
        var bomTotal = 0.0;
        var branches = dataModel.getData();
        var sectionTotals = {};
        for(var n in branches){
            var branch = branches[n];
            if(hardRecalc){
                calculateRowTotal(branch);
            }
            var rowTotal = parseFloat(branch.rowTotal);
            if(rowTotal){
                bomTotal = bomTotal + rowTotal;

                var section = branch.section;
                var sectionTotal = sectionTotals[section];
                if(!sectionTotal){
                    sectionTotal = 0.0;
                }
                sectionTotal = sectionTotal + rowTotal;
                sectionTotals[section] = sectionTotal;
                //alert(sectionTotals[section]);
            }

        }
        var cell = document.getElementById("grandtotal");
        if(cell){
            cell.innerHTML = "$" + formatFloat("" + rndStrOrNumFloat(bomTotal, 2), 2);
        }

        <%
        sectionIter = sections.iterator();
        while(sectionIter.hasNext()){
            section = (String) sectionIter.next();
            %>
            cell = document.getElementById("total_<%= section %>");
            if(cell){

                var sectionTotal = sectionTotals['<%= section %>'];
                if(!sectionTotal){
                    sectionTotal = 0.0;
                }
                cell.innerHTML = "$" + formatFloat("" + rndStrOrNumFloat(sectionTotal, 2), 2);
            }
            <%
        }
        %>
    <% } %>
    }
    //////////////////////////////////////////////////////////////////////////////////////
    function executeBOMHandleChangeCSP(oldValue, newValue, branch, sourceCell, attKey){

        <jsp:include page="<%=subURLFolder+ CLIENT_SIDE_PLUGIN %>" flush="true" >
             <jsp:param name="type" value="<%= FormatHelper.getObjectId(type) %>"/>
             <jsp:param name="pluginType" value="BOMEditor_handleChange"/>
        </jsp:include>
    }

   //----------------------------------------------
   function freshTest(){
        runPostAjaxRequest('<%=AJAX_PAGE%>','action=GET_BOM_FRESH&bom=<%= FormatHelper.encodeForJavascript(partOid) %>' ,'refreshBOM');
    }
    //---------------------------------------------
   // dmf nike
    var specReport = '';
    var synchReport = '';

    function getSpecReportHTML(){
        return specReport;
    }

    function getSyncReportHTML(){
        return synchReport;
    }
   
    function colorwayCheck(){

        var loc = location.protocol + '//' + location.host + urlContext_masterController_url;
        var sOptions = 'activity=EDIT_BOM_COLORWAYS_CHECK';
        sOptions = sOptions + '&owner=<%= ownerId %>';

        <% if(productBOM){ %>

        var splId = document.MAINFORM.splId.value;
        sOptions = sOptions + '&seasonId=' + splId +'&splId='+splId;

        <% } %>

        runPostAjaxRequest(loc, sOptions, 'postColorwayCheck');
    }

    function getSKUsForEdit(ownerId, seasonid){
        divWindow = new jsWindow(600, 400, 100, 50, 30, "", 20, true, true, true);
        divWindow.showProcessingMessage();

        var ajParams = '&ajaxWindow=true';
        ajParams = ajParams + '&detailRequest=false';
        ajParams = ajParams + '&module=BOM_COLORWAYS';
        ajParams = ajParams + '&action=SEARCH_ONLY';
        ajParams = ajParams + '&multiple=true';
        ajParams = ajParams + '&selectParameter=completeGetSKUsForEdit';
        ajParams = ajParams + '&idOnly=false';
        ajParams = ajParams + '&objectIdType=version';
        ajParams = ajParams + '&owner=' + ownerId;
        if(seasonid){
            ajParams = ajParams + '&seasonId=' + seasonid;
        }
        runPostAjaxRequest('<%=URL_CONTEXT%>/jsp/main/Chooser.jsp', ajParams, 'ajaxDefaultResponse');
    }




<%if ( WCPART_ENABLED ){%>
    window.onload=function(){
      var pickerID = "partPicker";
      var updateHiddenField = $(pickerID);
      //remove input text
      var updateDisplayField = $(pickerID +"$label$");
      updateDisplayField.style.visibility = 'hidden';
      $(pickerID+"_FIND_BTN").style.visibility = 'hidden';
    }
<%}%>

  function launchPartPicker(cell){
     $("partPicker_FIND_BTN").click();
  }



</script>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// BODY ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>


<div id="loadingMessageSpace">
    <center>
        <span class="HEADING1">
        <br><br><br><br><br><br><br><br><br><br>
        <%= FormatHelper.encodeForHTMLContent(loadingWaitLabel) %>
        <br>
        <br>
        <%= FormatHelper.encodeForHTMLContent(bomLoadLabel) %>
        <br>
        <br>
        <img src="<%= URL_CONTEXT %>/images/blue-loading.gif">
        </span>

    </center>
</div>
<div id="bomEditorSpace" style="display:none">
<input type="hidden" name="dataString" value="">
<input type="hidden" name="altEditMode" value="true">
<input name="currentMaterialId" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("currentMaterialId")) %>" type="hidden">
<input name="currentMaterialName" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("currentMaterialName")) %>" type="hidden">
<input name="currentSupplierName" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("currentSupplierName")) %>" type="hidden">
<input name="currentMaterialSupplierPrice" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("currentMaterialSupplierPrice")) %>" type="hidden">
<input name="currentMaterialUOM" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("currentMaterialUOM")) %>" type="hidden">
<input name="currentColorControlled" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("currentColorControlled")) %>" type="hidden">
<input name="currentMaterialPrice" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("currentMaterialPrice")) %>" type="hidden">
<input name="currentMaterialSupplier" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("currentMaterialSupplier")) %>" type="hidden">
<input name="currentColorId" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("currentColorId")) %>" type="hidden">
<input name="currentColorName" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("currentColorName")) %>" type="hidden">
<input name="bomEditorMode" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("bomEditorMode")) %>" type="hidden">
<input name="currentsplId" value="<%= FormatHelper.encodeForHTMLAttribute(splId) %>" type="hidden">
<input name="preventEnter" value="true" type="hidden">
<input name="bomIterId" value="<%= FormatHelper.getObjectId(bomPart) %>" type="hidden">
<input name="currentTime" value="" type="hidden">
<input name="contextSKUId" value="<%= FormatHelper.encodeAndFormatForHTMLContent(contextSKUId) %>" type="hidden">
<input name="destinationId" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("destinationId")) %>" type="hidden">

<input name="usedAttKeysStr" value="<%= usedAttKeysStr %>" type="hidden">
<input name="viewColumnsStr" value="<%= viewColumnsStr %>" type="hidden">
<input type="hidden" name="colorwayIds" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("colorwayIds")) %>">

<%
    String sizeCat = request.getParameter("sizeCategoryId");
    String contextSizeCat = request.getParameter("contextSizeCatId");
    String errorMessage = request.getParameter("errorMessage");

    if(!FormatHelper.hasContent(contextSizeCat) && FormatHelper.hasContent(sizeCat)){
        contextSizeCat = sizeCat;
    }
    else if(!FormatHelper.hasContent(contextSizeCat)){
        contextSizeCat = "blank";
    }
%>
<input name="contextSizeCatId" value="<%= FormatHelper.encodeForHTMLAttribute(contextSizeCat) %>" type="hidden">
<input name="size1" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("size1")) %>" type="hidden">
<input name="size2" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("size2")) %>" type="hidden">

<div class='tableDebuggerCls'>+++++++ **** EditBOMAlt.jspf -> domtableeditor + Table Generator(startTable)  ******+++++++++++++</div>

<!-- Color Vairations -->
<script type="text/javascript">
    var colorDimNameMap = {};
    <%
        Map fullSKUIdColors = tSkuMap;
        Iterator skuIdList = fullSKUIdColors.keySet().iterator();

        Map newColors = new HashMap();
        while (skuIdList.hasNext())     {
            String skuId = "" + skuIdList.next();
            LCSSKU sku = (LCSSKU) LCSQuery.findObjectById(skuId);

            String numeric = FormatHelper.getNumericFromReference(sku.getMasterReference());
            newColors.put(numeric, fullSKUIdColors.get(skuId));
    %>
    colorDimNameMap['<%=numeric%>'] = '<%=FormatHelper.encodeForJavascript("" + sku.getValue("skuName"))%>';
    <%  }

    %>
</script>
<!-- End Color Variations -->

<!-- Start Destination Variations -->
    <%
        if ("PRODUCT".equals(ownerMode) && USE_PRODUCTDESTINATIONS) {
    %>
    <script type="text/javascript">
        var destinationDimNameMap = {};
        <%
            Iterator destList = destinationMap.keySet().iterator();
            while (destList.hasNext()) {
                String numeric = "" + destList.next();
                String name = "" + destinationMap.get(numeric);
        %>
        destinationDimNameMap['<%=numeric%>'] = '<%=FormatHelper.encodeForJavascript(name)%>';
        <%
            }

        %>
    </script>
    <%
        }
    %>
<!-- End Destination Variations -->

	<div class="bom-header">
		<div class="title-bar  bom-header-first-row">
			<div class="title-bar-label bom-header-first-row-col1">
				<%
				    if (productBOM) {
				%>
				<%=FormatHelper.encodeAndFormatForHTMLContent((String) product.getValue("productName"))%>
				-
				<%=FormatHelper.encodeAndFormatForHTMLContent((String) bomPart.getValue("name"))%>
				<%
				    } else {
				%>
				<%=FormatHelper.encodeAndFormatForHTMLContent((String) material.getValue("name"))%>
				<%
				    }
				%>
			</div>
			<div class="title-bar-tool-bar bom-header-first-row-col2">
				<div class="title-bar-tool-bar-item">
					<!-- Start View includes -->
					<%request.setAttribute("inlineLabel", "inline-label"); %>
					<jsp:include page="<%=subURLFolder + BOM_VIEW_PLUGIN%>"
						flush="true">
						<jsp:param name="oid" value="<%=partOid%>" />
						<jsp:param name="returnOid" value="<%=partOid%>" />
					</jsp:include>
					<!-- End View Icludes -->
				</div>
				<div class="title-bar-tool-bar-item">
					<!-- Header Attributes -->
					<img
						title="<%=FormatHelper.encodeAndFormatForHTMLContent(headerAttributesToolTip)%>"
						src="<%=URL_CONTEXT%>/images/attributes.gif"
						onclick="javascript:toggleDiv('bomHeaderAttributesDiv')"></img>
				</div>
					<!-- Remove Primary -->
					<%
					    if (includeSetAsPrimary && !USE_AUTO_SET_PRIMARY_MATERIAL) {
					%>
					<div class="title-bar-tool-bar-item">
					<img
						title="<%=FormatHelper.encodeAndFormatForHTMLContent(clearPrimaryMaterialOnMouseOverTooltip)%>"
						src="<%=URL_CONTEXT%>/images/clearPrmMat.png"
						onclick="javascript:RFABOM.clearPrimaryMaterial()"></img>
						</div>
					<%
					    }
					%>
				<%
				    if (productBOM) {
				%>
				<div class="title-bar-tool-bar-item"
					onmouseover="return overlib(getSpecReportHTML(), WIDTH, <%=specReportSize%>);"
					onmouseout="return nd();">
					<!-- BOM Linking Info -->
					<img border="0" src="<%=WT_IMAGE_LOCATION%>/<%=SPEC_REPORT_IMG%>"
						onclick="javascript:doNothing()">
				</div>
				<div class="title-bar-tool-bar-item"
					onmouseover="return overlib(getSyncReportHTML(), WIDTH, <%=specReportSize%>);"
					onmouseout="return nd();">
					<img border="0" src="<%=WT_IMAGE_LOCATION%>/<%=SYNCH_REPORT_IMG%>"
						onclick="javascript:doNothing()">
				</div>
				<%
				    }
				%>
				<!-- Linked to multiple-Products -->
				<%
				    if (multiProd) {
				%>
				<div class="title-bar-tool-bar-item">
					<img
						title="<%=FormatHelper.encodeAndFormatForHTMLContent(multiProductLabel)%>"
						src="<%=URL_CONTEXT%>/images/linkedProducts.png"
					></img>
				</div>
				<%
				    }
				%>
				<div class="title-bar-tool-bar-item">
					<img border="0" width="16px"
						title="<%=FormatHelper.encodeAndFormatForHTMLContent(collapseBOMTooltip)%>"
						src="<%=URL_CONTEXT%>/images/close-variants-white.svg"
					  onclick="javascript:collapseBranches()"></img>
				</div>
			</div>
			<div class="title-bar-tool-bar bom-header-first-row-col3">
				<div class="title-bar-tool-bar-item">
					<a class="restyled-button"
						title="<%=FormatHelper.encodeAndFormatForHTMLContent(saveRecentChangesContinueEdittingOnMouseOverTooltip)%>"
						href="javascript:save()"><span><%=saveButton%></span></a>
				</div>
				<div class="title-bar-tool-bar-item">
					<a class="restyled-button"
						title="<%=FormatHelper.encodeAndFormatForHTMLContent(saveRecentChangesExitEditorOnMouseOverTooltip)%>"
						href="javascript:saveAndDone()"><span><%=saveCheckInButton%></span></a>
				</div>
				<div class="title-bar-tool-bar-item">
					<a class="restyled-button"
						title="<%=FormatHelper.encodeAndFormatForHTMLContent(ignoreRecentChangesExitEditorOnMouseOverTooltip)%>"
						href="javascript:cancel()"><span><%=cancelLabel%></span></a>
				</div>
			</div>
		</div>
		<!-- End of first row -->
		<div class="bom-header-second-row">
			<%
			    if (productBOM) {
			%>
			<div
				title="<%=FormatHelper.encodeAndFormatForHTMLContent(seasonIconToolTip)%>"
				class="bom-header-second-row-icon">
				<img src="<%=URL_CONTEXT%>/images/season_s.png" />
			</div>
			<div class="f-attribute-box season  bom-header-second-row-dropdown">
				<%=FormGenerator2.createDropDownListWidget(null, seasonMap, "splId", splId, null, false, false,
						order, null, null, false, "", "", "", noneSelectedLabel, true)%>
			</div>
			<%
			    if (USE_SIZING) {
			%>
			<div class="bom-header-second-row-icon"
				title="<%=FormatHelper.encodeAndFormatForHTMLContent(sizeIconToolTip)%>">
				<img src="<%=URL_CONTEXT%>/images/units_of_measure.png" />
			</div>
			<div class="f-attribute-box bom-header-second-row-dropdown">
				<%=FormGenerator2.createDropDownListWidget(null, sizeCategoryMap, "sizeCategoryId",
						selectedSizeCategory, null, false, false, null, null, null, false, "", "", "", "", true)%>
			</div>
			<%
			    }
			}
			%>
			<div
				title="<%=FormatHelper.encodeAndFormatForHTMLContent(forTheCurrentColorAndSourceTooltip)%>"
				class="bom-header-second-row-icon retrieve-bom">
				<img src="<%=WT_IMAGE_LOCATION%>/refresh.gif"
					onclick="javascript:colorwayCheck()"></img>
			</div>
			<!-- Materials Button -->
			<div
				title="<%=FormatHelper
					.encodeAndFormatForHTMLContent(chooseaMaterialFromThePaletteOrMaterialLibraryOnMouseOverTooltip)%>"
				class="bom-header-second-row-icon">
				<img src="<%=WT_IMAGE_LOCATION%>/material.png"
					onclick="javascript:toggleMaterialFrame()" />
			</div>
			<div class="clipboard-data">
				<!-- Materials Clipboard -->
				<div id="currentMaterialNameDiv">
					<%
					    String materialSupplierNameDisplay = "";
								if (FormatHelper.hasContent(FormatHelper.format(request.getParameter("currentMaterialName")))) {
									materialSupplierNameDisplay = FormatHelper
											.encodeForHTMLContent(request.getParameter("currentMaterialName")) + " / "
											+ FormatHelper.encodeForHTMLContent(request.getParameter("currentSupplierName"));
								}
					%>
					<%=FormatHelper.encodeAndFormatForHTMLContent(materialSupplierNameDisplay)%>
				</div>
				<!-- End materialClipboard -->
				<div id="clearMaterialDiv">
					<img src="<%=URL_CONTEXT%>/images/icon_close.svg"
						onclick="javascript:clearSelectedMaterial()" />
				</div>
				<!-- End clearMaterialDIv -->
			</div>
			<%
			    if (!billOfLabor) {
			%>
			<div
				title="<%=FormatHelper
						.encodeAndFormatForHTMLContent(chooseaColorFromThePaletteorColorLibraryOnMouseOverTooltip)%>"
				class="bom-header-second-row-icon">
				<img src="<%=URL_CONTEXT%>/images/paletteColors.gif"
					onclick="javascript:toggleColorFrame()">
				<!-- Color Clipboard -->
			</div>
			<div class="clipboard-data">
				<div id="currentColorNameDiv">
					<%=FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("currentMaterialColor"))%>
				</div>
				<div id="clearColorDiv">
					<img src="<%=URL_CONTEXT%>/images/icon_close.svg"
						onclick="javascript:clearSelectedColor()">
				</div>
				<!-- End colorClipboard div -->
			</div>
			<%
			    }
			%>
		</div>
		<div class="variation">
			<%
			    if (productBOM) {
			%>

			<script type="text/javascript">
    var sourceDimNameMap = {};

    <%//Map fullSourceIds = appContext.getSourcesMap();
				Map allSourceIds = (new ProductHeaderQuery()).findSourcingConfigsMap(product, season);

				Iterator newSourceIdList = allSourceIds.keySet().iterator();

				Map newSources = new HashMap();
				while (newSourceIdList.hasNext()) {

					String sourceId = "" + newSourceIdList.next();
					WTObject obj = (WTObject) LCSQuery.findObjectById(sourceId);
					LCSSourcingConfigMaster scMaster = null;
					if (obj instanceof LCSSourcingConfig) {
						scMaster = (LCSSourcingConfigMaster) ((LCSSourcingConfig) obj).getMaster();
					} else if (obj instanceof LCSSourceToSeasonLink) {
						scMaster = (LCSSourcingConfigMaster) ((LCSSourceToSeasonLink) obj).getSourcingConfigMaster();
					}

					String numeric = FormatHelper.getNumericObjectIdFromObject(scMaster);
					newSources.put(numeric, allSourceIds.get(sourceId));%>
            sourceDimNameMap['<%=numeric%>'] = '<%=FormatHelper.encodeForJavascript("" + scMaster.getSourcingConfigName())%>';
    <%}%>
</script>
			<!-- End Source Variations -->
			<div class="f-attribute-box" id="sourcesDD">
				<%=FormGenerator2.createMultiChoice("selectedSources", sourcesLabel, newSources,
						request.getParameter("selectedSources"), false, null, true, null, null, null, false)%>
			</div>
			<div class="f-attribute-box" id="colowaysDD">
				<%=FormGenerator2.createMultiChoice("selectedColorways",
						colorwaysLabel, newColors,
						request.getParameter("selectedColorways"), false, null, true, null, null, null, false)%>
			</div>
			<%
			    if (FormatHelper.hasContent(size1Label) && size1Map.keySet().size() > 0 && USE_SIZING) {
			%>
			<div class="f-attribute-box" id="sizes1DD">
				<%=FormGenerator2.createMultiChoice("selectedSizes1", size1Label, size1Map,
							request.getParameter("selectedSizes1"), false, null, true, null, null, null, false)%>
			</div>
			<%
			    }
			%>

			<%
			    if (FormatHelper.hasContent(size2Label) && size2Map.keySet().size() > 0 && USE_SIZING) {
			%>
			<div class="f-attribute-box" id="sizes2DD">
				<%=FormGenerator2.createMultiChoice("selectedSizes2", size2Label, size2Map,
							request.getParameter("selectedSizes2"), false, null, true, null, null, null, false)%>
			</div>
			<%
			    }
			%>
			<%
			    if ("PRODUCT".equals(ownerMode) && USE_PRODUCTDESTINATIONS) {
			%>
			<div class="f-attribute-box" id="destinationsDD">
				<%=FormGenerator2.createMultiChoice("selectedDestinations", destinationLabel, destinationMap,
							request.getParameter("selectedDestinations"), false, null, true, null, null, null, false)%>
			</div>

			<%
			    }
			%>
			<div id="refreshDD" class="f-attribute-box">
				<div class="form-element-container">
					<div class="input-title">&nbsp;</div>
					<div class="image-body">
						<span> <a
							onmouseover="return overlib('<%=FormatHelper.encodeForJavascript(expandCollapseBOM)%>');"
							onmouseout="return nd();" href="javascript:expandAllForBOM()">
								<img class="buttonImg" id='bomExpandSectionImg'
								src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png">
						</a>
						</span>
					</div>
				</div>
			</div>
			<%
			    } else {
			%>
			<script type="text/javascript">
                var sourceDimNameMap = {};
                <%//CHUCK - HERE
				Collection materialSuppliersCollection = LCSMaterialSupplierQuery.findMaterialSuppliers(material)
						.getResults();
				Map fullSourceIds = FlexObjectUtil.hashCollection(materialSuppliersCollection,
						"LCSMATERIALSUPPLIERMASTER.IDA2A2");
				Iterator sourceIdList = fullSourceIds.keySet().iterator();
				FlexObject obj = null;
				Map sources = new HashMap();
				String sourceId = "";
				while (sourceIdList.hasNext()) {
					sourceId = (String) sourceIdList.next();
					obj = (FlexObject) fullSourceIds.get(sourceId);%>
                            sourceDimNameMap['<%=sourceId%>'] = '<%=FormatHelper.encodeForJavascript("" + obj.get("LCSSUPPLIERMASTER.SUPPLIERNAME"))%>';
                            <%//sourceId = "OR:com.lcs.wc.material.LCSMaterialSupplierMaster:" + sourceId;
					sources.put(sourceId, obj.get("LCSSUPPLIERMASTER.SUPPLIERNAME"));
				}%>
                </script>
			<div class="f-attribute-box" id="sourcesDD">
				<%=FormGenerator2.createMultiChoice("selectedSources", sourcesLabel, sources,
						request.getParameter("selectedSources"), false, null, true, null, null, null, false)%>
			</div>

			<script type="text/javascript">
                    var colorDimNameMap = {};
                    <%//Collection matCols = new LCSMaterialColorQuery().findMaterialColorData((LCSMaterialMaster)material.getMaster(), null).getResults();
				Collection matCols = skuList;
				Iterator matColIter = matCols.iterator();
				Map colors = new HashMap();
				FlexObject fo;
				while (matColIter.hasNext()) {
					fo = (FlexObject) matColIter.next();
					String numeric = fo.getString("LCSMATERIALCOLOR.IDA2A2");
					colors.put(numeric, fo.getString("MATCOLOR.DISPLAY"));%>
                            colorDimNameMap['<%=numeric%>'] = '<%=FormatHelper.encodeForJavascript(fo.getString("MATCOLOR.DISPLAY"))%>';
                            <%}%>
                    </script>
			<div class="f-attribute-box" id="colowaysDD">
				<%=FormGenerator2.createMultiChoice("selectedColorways",
						FormatHelper.encodeAndFormatForHTMLContent(colorwaysLabel), colors,
						request.getParameter("selectedColorways"), false, null, true, null, null, null, false)%>
			</div>
			<div id="refreshDD" class="f-attribute-box">
				<div class="form-element-container">
					<div class="input-title">&nbsp;</div>
					<div class="image-body">
						<span style="float: left; vertical-align: middle;"> <a
							onmouseover="return overlib('<%=FormatHelper.encodeForJavascript(expandCollapseBOM)%>');"
							onmouseout="return nd();" href="javascript:expandAllForBOM()">
								<img class="buttonImg" id='bomExpandSectionImg'
								src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png">
						</a>
						</span>
					</div>
				</div>
			</div>

			<%
			    }
			%>
		</div>
	</div>

<!-- Start of BOM Content -->
<div id="pageContents" class="themedPage" onscroll="hideSelectorDiv();">
	<div>
		<%
  		  if (FormatHelper.hasContent(errorMessage)) {
		%>
			<div class="ERROR">
				<%=FormatHelper.encodeAndFormatForHTMLContent(
						java.net.URLDecoder.decode(errorMessage, defaultCharsetEncoding))%>
			</div>
		<%}%>
		
			<div id="bomHeaderAttributesDiv">
              <table>
                <tr>
                    <td>
                        <%=tg.startGroupBorder()%>
                        <%=tg.startTable()%>
                        <%=tg.startGroupTitle()%>
                            &nbsp;<%=bomIdentificationLabel%>:
                        <%=tg.endTitle()%>
                        <%=tg.startGroupContentTable()%>
                        <col width="15%"></col><col width="35%"></col>
                        <col width="15%"></col><col width="35%"></col>
                        </td></tr>
                        <tr>
                        <%
                            if (updateName) {
                        %>
                            <%=flexg.drawFormWidget(type.getAttribute("name"), bomPart, true)%>
                        <%
                            } else {
                        %>
                            <%=FormGenerator.createDisplay(type.getAttribute("name").getAttDisplay(true),
						(String) bomPart.getValue("name"), FormatHelper.STRING_FORMAT)%>
                        <%
                            }
                        %>
                            <%=fg.createPlaceholder()%>
                        </tr>
                        <%=tg.endContentTable()%>
                        <%=tg.endTable()%>
                        <%=tg.endBorder()%>
                <tr>
                    <td>
                        <%
                            flexg.setScope(com.lcs.wc.flexbom.FlexBOMFlexTypeScopeDefinition.BOM_SCOPE);
                        			flexg.setUpdate(true);
                        %>
                        <%=flexg.generateForm(bomPart)%>
                        <%
                            flexg.setScope(null);
                        %>
                         <script type="text/javascript">
                            toggleDiv('bomHeaderAttributesDiv');
                            <!-- toggling header to show collapsed by default -->
                            <% if (BOM_HEADER_DEFAULT_COLLAPSE) {%>
                            changeDiv('lowerHdr','minus');
                            <%}%>
                            </script>
                    </td>
                </tr>
            </table>
            </div>
            
            
            <div id="ajaxSearchResultPopup" style="display:none">
            </div>
            
			<div id="bomWorkSpaceDiv" >
            <%!
                public HashMap getAttWrappable(FlexTypeAttribute att, HashMap map, String id) {
                    int wrapValue = 0;
                    if(att.isAttTableWrapable()){
                        wrapValue = att.getAttTableMinWidth() * 8;
                        wrapValue = (wrapValue < 62) ? 62 : wrapValue;
                        map.put(id,wrapValue);
                    }
                    return map;
                }
            %>
				<%

                                if (materialBOM){
                                    columnList.remove("BOM.wcPartName");
                                }
								logger.debug("columnList :\n"+columnList);
                                columnIter = columnList.iterator();
                                ArrayList visColumns = new ArrayList();
                                HashMap nameToLogicalMap = new HashMap();
                                HashMap positionToNameMap = new HashMap();
                                HashMap positionToCountMap = new HashMap();
                                HashMap keyToDisplayMap = new HashMap();
                                HashMap displayToKeyMap = new HashMap();
                                HashMap attKeyToWrapValueMap = new HashMap();

                                while(columnIter.hasNext()){
                                    columnKey = (String) columnIter.next();

									boolean isRequiredAtt = false;
                                    if (requiredAttMap.containsKey(columnKey)){
                                        isRequiredAtt = true;
                                    }
                                    boolean isBOMAtt = false;
                    				if (columnKey.indexOf("BOM.") == 0){
                                       isBOMAtt = true;
                                    }

                                    if(columnKey.equals("BOM.branchId")){

                                        if (isRequiredAtt){ //Add * to required column for BOM atts
                                            branchLabel = "*" + branchLabel;
                                        }

                                        visColumns.add(branchLabel);
                                        Integer i = BOMEditorUtility.getNextInteger(nameToLogicalMap.values());
                                        nameToLogicalMap.put("brnchId", i);
                                        positionToCountMap.put("brnchId", new Integer(1));
                                        positionToNameMap.put(i, "brnchId");
                                        displayToKeyMap.put(branchLabel, "brnchId");
                                        keyToDisplayMap.put("brnchId", branchLabel);
                                        if(attMap.containsKey(columnKey)) {
                                            attKeyToWrapValueMap = getAttWrappable((FlexTypeAttribute) attMap.get(columnKey), attKeyToWrapValueMap, "brnchId");
                                        }
                                        continue;
                                    }

                                    if(columnKey.equals("colorDim")){
                                        visColumns.add(colorwayLabel);
                                        Integer i = BOMEditorUtility.getNextInteger(nameToLogicalMap.values());
                                        nameToLogicalMap.put("colorDim", i);
                                        positionToCountMap.put("colorDim", new Integer(1));
                                        positionToNameMap.put(i, "colorDim");
                                        displayToKeyMap.put(colorwayLabel, "colorDim");
                                        keyToDisplayMap.put("colorDim", colorwayLabel);
                                        if(attMap.containsKey(columnKey)) {
                                            attKeyToWrapValueMap = getAttWrappable((FlexTypeAttribute) attMap.get(columnKey), attKeyToWrapValueMap, "colorDim");
                                        }
                                        continue;

                                    }

                                    if(columnKey.equals("sourceDim")){
                                        visColumns.add(sourceLabel);
                                        Integer i = BOMEditorUtility.getNextInteger(nameToLogicalMap.values());
                                        nameToLogicalMap.put("sourceDim", i);
                                        positionToCountMap.put("sourceDim", new Integer(1));
                                        positionToNameMap.put(i, "sourceDim");
                                        displayToKeyMap.put(sourceLabel, "sourceDim");
                                        keyToDisplayMap.put("sourceDim", sourceLabel);
                                        if(attMap.containsKey(columnKey)) {
                                            attKeyToWrapValueMap = getAttWrappable((FlexTypeAttribute) attMap.get(columnKey), attKeyToWrapValueMap, "sourceDim");
                                        }
                                        continue;

                                    }



                                    if(columnKey.equals("destinationDim")){
                                        visColumns.add(destinationDimLabel);
                                        Integer i = BOMEditorUtility.getNextInteger(nameToLogicalMap.values());
                                        nameToLogicalMap.put("destinationDim", i);
                                        positionToCountMap.put("destinationDim", new Integer(1));
                                        positionToNameMap.put(i, "destinationDim");
                                        displayToKeyMap.put(destinationDimLabel, "destinationDim");
                                        keyToDisplayMap.put("destinationDim", destinationDimLabel);
                                        if(attMap.containsKey(columnKey)) {
                                            attKeyToWrapValueMap = getAttWrappable((FlexTypeAttribute) attMap.get(columnKey), attKeyToWrapValueMap, "destinationDim");
                                        }
                                        continue;

                                    }

                                    if(columnKey.equals("size1Dim") && FormatHelper.hasContent(size1Label)){
                                        visColumns.add(size1DimLabel);
                                        Integer i = BOMEditorUtility.getNextInteger(nameToLogicalMap.values());
                                        nameToLogicalMap.put("size1Dim", i);
                                        positionToCountMap.put("size1Dim", new Integer(1));
                                        positionToNameMap.put(i, "size1Dim");
                                        displayToKeyMap.put(size1DimLabel, "size1Dim");
                                        keyToDisplayMap.put("size1Dim", size1DimLabel);
                                        if(attMap.containsKey(columnKey)) {
                                            attKeyToWrapValueMap = getAttWrappable((FlexTypeAttribute) attMap.get(columnKey), attKeyToWrapValueMap, "size1Dim");
                                        }
                                        continue;

                                    }

                                    if(columnKey.equals("size2Dim") && FormatHelper.hasContent(size2Label)){
                                        visColumns.add(size2DimLabel);
                                        Integer i = BOMEditorUtility.getNextInteger(nameToLogicalMap.values());
                                        nameToLogicalMap.put("size2Dim", i);
                                        positionToCountMap.put("size2Dim", new Integer(1));
                                        positionToNameMap.put(i, "size2Dim");
                                        displayToKeyMap.put(size2DimLabel, "size2Dim");
                                        keyToDisplayMap.put("size2Dim", size2DimLabel);
                                        if(attMap.containsKey(columnKey)) {
                                            attKeyToWrapValueMap = getAttWrappable((FlexTypeAttribute) attMap.get(columnKey), attKeyToWrapValueMap, "size2Dim");
                                        }
                                        continue;

                                    }


                                    if(columnKey.equals("SUPPLIER.name")){
                                        visColumns.add(supplierLabel);
                                        Integer i = BOMEditorUtility.getNextInteger(nameToLogicalMap.values());
                                        nameToLogicalMap.put("supplierName", i);
                                        positionToCountMap.put("supplierName", new Integer(1));
                                        positionToNameMap.put(i, "supplierName");
                                        displayToKeyMap.put(supplierLabel, "supplierName");
                                        keyToDisplayMap.put("supplierName", supplierLabel);
                                        if(attMap.containsKey(columnKey)) {
                                            attKeyToWrapValueMap = getAttWrappable((FlexTypeAttribute) attMap.get(columnKey), attKeyToWrapValueMap, "supplierName");
                                        }
                                        continue;
                                    }


                                    att = (FlexTypeAttribute) attMap.get(columnKey);
                                    if(att == null || !ACLHelper.hasViewAccess(att)){
                                        continue;
                                    }

                                    if( ("colorDescription".equals(att.getAttKey()) && "colorDescription".equals(multiAttribute)) ||
                                        ("materialDescription".equals(att.getAttKey()) && "materialDescription".equals(multiAttribute))){

                                        if (isRequiredAtt && isBOMAtt){ //required column for BOM atts
                                            colorDescriptionLabel = "*"+ colorDescriptionLabel;
                                        }

                                        //Also need to add in Product level color column
                                        visColumns.add(colorDescriptionLabel);
                                        Integer i = BOMEditorUtility.getNextInteger(nameToLogicalMap.values());
                                        nameToLogicalMap.put("colorDescription", i);
                                        positionToCountMap.put("colorDescription", new Integer(1));
                                        positionToNameMap.put(i, "colorDescription");
                                        displayToKeyMap.put(colorDescriptionLabel, "colorDescription");
                                        keyToDisplayMap.put("colorDescription", colorDescriptionLabel);
                                        attKeyToWrapValueMap = getAttWrappable(att, attKeyToWrapValueMap, "colorDescription");

                                        skuIter = skuList.iterator();
                                        boolean first = true;
                                        Integer position = new Integer(0);
                                        int count = 0;
                                        while(skuIter.hasNext()){
                                            skuObj = (FlexObject) skuIter.next();
                                            visColumns.add(skuObj.getString(skuNameIndex));
                                            if(first){
                                                position = BOMEditorUtility.getNextInteger(nameToLogicalMap.values());
                                                first = false;
                                                positionToNameMap.put(position, "Colorways");
                                                positionToCountMap.put("Colorways", new Integer(skuList.size()));

                                            }
                                            nameToLogicalMap.put(skuObj.getString(skuNameIndex), position);
                                            count++;

                                            String skuDisplay = skuObj.getString(skuNameIndex);
                                            if (isRequiredAtt && isBOMAtt){ //required column for BOM atts
                                                skuDisplay = "*" + skuDisplay;
                                                colorwaysLabel = "*" + colorwaysLabel;
                                            }

                                            //displayToKeyMap.put(skuObj.getString(skuNameIndex), skuObj.getString(skuNameIndex));
                                            displayToKeyMap.put(skuDisplay, skuObj.getString(skuNameIndex));
                                            keyToDisplayMap.put("Colorways", colorwaysLabel);
                                        }


                                    } else if("quantity".equals(att.getAttKey()) && "quantity".equals(multiAttribute)){
                                        Iterator sizeIter = null;
                                        int sizeLength = 0;
                                        int count = 0;
                                        if("SIZE1".equals(multiDimension)){
                                            sizeIter = size1Collection.iterator();
                                            sizeLength = size1Collection.size();
                                        } else {
                                            sizeIter = size2Collection.iterator();
                                            sizeLength = size2Collection.size();
                                        }
                                        String size;
                                        boolean first = true;
                                        Integer position = new Integer(0);
                                        while(sizeIter.hasNext()){
                                            size = (String) sizeIter.next();


                                            if (isRequiredAtt && isBOMAtt ){ //required column for BOM atts
                                                size = "*"+ size;
                                            }

                                            visColumns.add(size);
                                            if(first){
                                                position = BOMEditorUtility.getNextInteger(nameToLogicalMap.values());
                                                first = false;
                                                positionToNameMap.put(position, multiDimension);
                                                positionToCountMap.put(multiDimension, new Integer(sizeLength));

                                            }
                                            nameToLogicalMap.put(size, position);
                                            count++;
                                            displayToKeyMap.put(size, size);
                                            keyToDisplayMap.put(multiDimension, multiDimension);
                                            attKeyToWrapValueMap = getAttWrappable(att, attKeyToWrapValueMap, "quantity");
                                        }

                                    } else {
                                        String attKey = FormatHelper.replaceCharacter(columnKey, ".", "_");
                                        if(columnKey.indexOf("BOM.") == 0){
                                            attKey = att.getAttKey();
                                        }
                                        String tattDisplay = att.getAttDisplay();

                                        if(attKey.equals("materialDescription") && billOfLabor){
                                            tattDisplay = operationLabel;
                                        }

                                        if (isRequiredAtt && isBOMAtt){ //required column for BOM atts
                                            tattDisplay = "*" + tattDisplay;
                                        }

                                        visColumns.add(tattDisplay);
                                        Integer i = BOMEditorUtility.getNextInteger(nameToLogicalMap.values());
                                        nameToLogicalMap.put(attKey, i);
                                        positionToNameMap.put(i, attKey);
                                        positionToCountMap.put(attKey, new Integer(1));
                                        displayToKeyMap.put(tattDisplay, attKey);
                                        keyToDisplayMap.put(attKey, tattDisplay);
                                        attKeyToWrapValueMap = getAttWrappable(att, attKeyToWrapValueMap, attKey);
                                    }

                                }
								
								logger.debug("nameToLogicalMap :\n"+nameToLogicalMap);
								logger.debug("positionToNameMap :\n"+positionToNameMap);
								logger.debug("positionToCountMap :\n"+positionToCountMap);
								logger.debug("displayToKeyMap :\n"+displayToKeyMap);
								logger.debug("keyToDisplayMap :\n"+keyToDisplayMap);
								logger.debug("attKeyToWrapValueMap :\n"+attKeyToWrapValueMap);
                                %>



				<script type="text/javascript">
				    //Build view support
				orderAssign[1] = 'Insert';
				orderAssign[2] = 'Select';
				orderAssign[3] = 'Expand';
				
				columnAssign['Insert']   = 1;
				columnAssign['Select']   = 2;
				columnAssign['Expand']   = 3;
				
				logicAssign['Insert']    = 'Insert';
				logicAssign['Select']    = 'Select';
				logicAssign['Expand']    = 'Expand';
				
				columnCounts['Insert']   = 1;
				columnCounts['Select']   = 1;
				columnCounts['Expand']   = 1;
				
				tAssign[0] = 'Insert';
				tAssign[1] = 'Select';
				tAssign[2] = 'Expand';
				
				
				
				<%

				    Vector positions = new Vector(positionToNameMap.keySet());
				    Collections.sort(positions);
				
				    int hardColumnCount = 3;
				
				    int tally = 3;
				
				    Iterator oa = positions.iterator();
				    Integer pos = null;
				    int lacount = 0;
				    int iPos = 0;
				    String name = "";
				    while(oa.hasNext()){
				        pos = (Integer)oa.next();
				        iPos = pos.intValue() + hardColumnCount;
				        name = (String)positionToNameMap.get(pos);
				        lacount = ((Integer)positionToCountMap.get(name)).intValue();
				%>
				orderAssign[<%= iPos %>] = '<%= name %>';
				columnAssign['<%= name %>']  = <%= iPos %>;
				<%
				        if(lacount <= 1){
				%>
				tAssign[<%= tally %>] = '<%= name %>';
				<%
				            tally++;
				        }  else {
				            for(int x = 1; x <= lacount; x++) {
				        %>
				tAssign[<%= tally %>] = '<%= name %><%= x %>';
				        <%
				            tally++;
				            }
				        } %>
				<%  }  %>
				
				<%
				    Iterator la = positionToCountMap.keySet().iterator();
				    while(la.hasNext()){
				        name = (String)la.next();
				        lacount = ((Integer)positionToCountMap.get(name)).intValue();
				        if(lacount <= 1){
				%>
				logicAssign['<%= name %>'] = '<%= name %>';
				columnCounts['<%= name %>'] = <%= lacount %>;
				<%      } else {
				            for(int x = 1; x <= lacount; x++) {
				%>
				logicAssign['<%= name %><%= x %>'] = '<%= name %>';
				<%
				            }
				%>
				columnCounts['<%= name %>'] = <%= lacount %>;
				<%
				        }
				    }
				%>
				
				//Build the views
				<%= BOMEditorUtility.buildViewsArrays(reportColumns, permanentColumns, (skuList != null && skuList.size() > 0), useSize1, useSize2, materialBOM, billOfLabor, attMap) %>
				
				
				var viewTables = {};
				var tableDiv;
				
				function setSectionState(sectionName, sectionState) {
				    //changeDiv(sectionName);
				    //alert ("Section " + sectionName +  " : " + sectionState);
				    runAjaxRequest('<%=AJAX_PAGE%>?action=SET_ACTION_STATE&sectionName=<%= FormatHelper.encodeForJavascript(partOid) %>' + sectionName + '&sectionState='+ sectionState,'doNothing');
				}
				</script>



					<div>
                            <div class="bomPage">
                             <%
                            String cellName;


                            sectionIter = sections.iterator();
                            int sectionCount=0;
                            while(sectionIter.hasNext()){
                                section = (String) sectionIter.next();
                                %>
                                    <%
                                    int visCount = positionToNameMap.keySet().size() + hardColumnCount;
                                    int sectionHashCode = section.hashCode();
                                    %>
                                    
                                    <div class="card card__overflow">
                                    	<input type="checkbox" id='<%= section %>TabEditorTableDiv_plus' class="cardCheckbox" checked/>
                                    	<div class="card-header">
										    <label class="card-label card-collapsible" for='<%= section %>TabEditorTableDiv_plus'>
										      <%= FormatHelper.encodeForHTMLContent(type.getAttribute("section").getAttValueList().getValue(section, lcsContext.getLocale())) %>
										    </label>
										  </div>
										  <div class="card-content">
										    <div id="<%= section %>TabEditorTableDiv">
                                            	<!-- Icons row -->
                                            	<div class="bom-table-header-menu">
                                                            <div class="bom-table-header-menu-item">
                                                                <a title='<%= FormatHelper.encodeForHTMLAttribute(deleteSelectedRows) %>' href="javascript:selectDomTable('<%= section %>TabEditorTable');validateBeforeTopLevelAction('removeRows()', '<%= FormatHelper.encodeForJavascript(toolBarWarningMsg) %>')">
                                                                    <img  src="<%=URL_CONTEXT%>/images/icon_delete.svg">
                                                                </a>
                                                            </div>
                                                            <div class="bom-table-header-menu-item">
                                                               <a title='<%= FormatHelper.encodeForHTMLAttribute(moveSelectedRowsUp) %>' href="javascript:selectDomTable('<%= section %>TabEditorTable');validateBeforeTopLevelAction('moveRowsUp()', '<%= FormatHelper.encodeForJavascript(toolBarWarningMsg) %>');toggleBomExpand('<%= section %>')">
                                                                	<img  src="<%=URL_CONTEXT%>/images/arrowUp.svg">
                                                            	</a>
                                                            </div>
                                                            <div class="bom-table-header-menu-item">
                                                                <a title='<%= FormatHelper.encodeForHTMLAttribute(moveSelectedRowsDown) %>' href="javascript:selectDomTable('<%= section %>TabEditorTable');validateBeforeTopLevelAction('moveRowsDown()', '<%= FormatHelper.encodeForJavascript(toolBarWarningMsg) %>');toggleBomExpand('<%= section %>')">
                                                                	<img  src="<%=URL_CONTEXT%>/images/arrowDown.svg">
                                                                </a>
                                                            </div>
                                                            <div class="bom-table-header-menu-item">
                                                                <a title='<%= FormatHelper.encodeForHTMLAttribute(insertBeforeSelectRows) %>'  href="javascript:selectDomTable('<%= section %>TabEditorTable');validateBeforeTopLevelAction('insertRows(true)', '<%= FormatHelper.encodeForJavascript(toolBarWarningMsg) %>')">
                                                                	<img src="<%=URL_CONTEXT%>/images/insert_before.svg">
                                                                </a>
                                                            </div>
                                                            <div class="bom-table-header-menu-item">
                                                                <a title='<%= FormatHelper.encodeForHTMLAttribute(insertAfterSelectRows) %>' href="javascript:selectDomTable('<%= section %>TabEditorTable');validateBeforeTopLevelAction('insertRows(false)', '<%= FormatHelper.encodeForJavascript(toolBarWarningMsg) %>')">
                                                                	<img  src="<%=URL_CONTEXT%>/images/insert_after.svg">
                                                                </a>
                                                            </div>
                                                            <div class="bom-table-header-menu-item">
                                                                <a title='<%= FormatHelper.encodeForHTMLAttribute(expandCollapseSection) %>' href="javascript:selectDomTable('<%= section %>TabEditorTable');javascript:expandAllForSection('<%= section %>')">
                                                                	<img id='<%=section%>ExpandSectionImg'  src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png">
                                                                </a>
                                                            </div>
                                                            <div class="bom-table-header-menu-item">
                                                            	<a onmouseout="return nd();" title='<%= FormatHelper.encodeForHTMLAttribute(highLightRows) %>' href="javascript:void()">
                                                                	<img  src="<%=URL_CONTEXT%>/images/highlight.svg" onclick="javascript:selectDomTable('<%= section%>TabEditorTable');return overlib(menuArray['highLightMenu'], STICKY, MOUSEOFF);">
                                                            	</a>
                                                            </div>
                                                            
                                                            <div class="table-header-buttons-item" id="hideColButton<%= section %>">
           														 <a class="restyled-button-rectangle" id="hideColButton<%= section %>" href="javascript:showHidableTableColumns('<%= section %>', 'hideColButton<%= section %>')"> <span><%= hideShowButton %></span></a>
																	<!-- Starting hide show action button implementation -->
																	<div id="columnHide<%= section %>" class="hideTableColumnDiv" style='width: 150px; z-index: 1000; position: absolute; display:none;'>
																		<div class='column-name'><input type=checkbox name="HCeditorTable_0" value="true" onclick="javascript:showHideAllColumnsView(this,'<%= section %>TabEditorTable','<%= visCount %>', '<%= section %>TabEditorTable_')" checked>
																		&nbsp;&nbsp;<span class='column-name'><%= hideAllBtn%>/<%= showAllBtn %></span></div>
                            											<div class='column-name'></div>
																	
																		<%-- Removing these next three rows completely causes a problem for the insertRow feature, setting display=none solves that problem--%>
		                                                               <div class='column-name' style="display:none;">
                                                                           <input type=checkbox id="HC<%= section %>TabEditorTable_1" name="HC<%= section %>TabEditorTable_1" value="true" onclick="toggleViewColumnCB(this, '<%= section %>TabEditorTable'); fixWidthForLastWrapColumn();" checked >
		                                                                 </div>
		                                                                 <div class='column-name' style="display:none;">
                                                                           <input type=checkbox id="HC<%= section %>TabEditorTable_2" name="HC<%= section %>TabEditorTable_2" value="true" onclick="toggleViewColumnCB(this, '<%= section %>TabEditorTable'); fixWidthForLastWrapColumn();" checked >
		                                                                 </div>
		                                                                 <div class='column-name' style="display:none;">
                                                                           <input type=checkbox id="HC<%= section %>TabEditorTable_3" name="HC<%= section %>TabEditorTable_3" value="true" onclick="toggleViewColumnCB(this, '<%= section %>TabEditorTable'); fixWidthForLastWrapColumn();" checked >
		                                                                 </div>
																			<%
		 
		                                                                 int count = 3;
		                                                                 Iterator vis = positions.iterator();
		                                                                 Integer position = null;
		                                                                 int actualPosition = 0;
		                                                                 while (vis.hasNext()){
		                                                                     position = (Integer)vis.next();
		                                                                     String columnDisplay = (String) keyToDisplayMap.get(positionToNameMap.get(position));
		                                                                     actualPosition = position.intValue() + count;
		 
		                                                                 %>
		                                                                 
		                                                                 <div class='column-name' style="display:none;">
                                                                           <input type=checkbox id="HC<%= section %>TabEditorTable_<%= actualPosition %>" name="HC<%= section %>TabEditorTable_<%= actualPosition %>" value="true" onclick="toggleViewColumnCB(this, '<%= section %>TabEditorTable'); fixWidthForLastWrapColumn();" checked>
																				&nbsp;&nbsp;<span><%= FormatHelper.encodeAndFormatForHTMLContent(columnDisplay) %></span>
		                                                               </div>
																		
																		<% 
		                                                                    // break after partName and material are written
		                                                                    if (actualPosition == 5) {
		                                                                       break;
		                                                                    }
		                                                                 } %>
																	
																	
																	
																    		<!-- Need to loop over the remaining columns to create the checkboxes -->
				                                                                <%
				                                                                while(vis.hasNext()){
				                                                                    position = (Integer)vis.next();
				                                                                    String columnDisplay = (String) keyToDisplayMap.get(positionToNameMap.get(position));
				                                                                    actualPosition = position.intValue() + count;
				
				                                                                %>
				                                                               <div class='column-name'>
                                                                                  <input type=checkbox id="HC<%= section %>TabEditorTable_<%= actualPosition %>" name="HC<%= section %>TabEditorTable_<%= actualPosition %>" value="true" onclick="toggleViewColumnCB(this, '<%= section %>TabEditorTable'); fixWidthForLastWrapColumn();" checked>
				                                                                  &nbsp;&nbsp; <span class='column-name'><%= FormatHelper.encodeAndFormatForHTMLContent(columnDisplay) %></span>
				                                                                </div>
				                                                                <% } %>
				                                                             
																</div>
															</div>
                                                            
                                                            <div class="select-view bom-table-header-menu-item inline-label">
                                                                <div class="input-title inline-label input-title-std"><%=viewLabel%></div>
                                                                <%
                                                                if(FormatHelper.hasContent(request.getParameter("viewId"))){
                                                                    selectedView = request.getParameter("viewId");
                                                                }
                                                                else if(FormatHelper.hasContent(defaultViewId)){
                                                                    selectedView = defaultViewId; ;
                                                                } else {
                                                                    selectedView = "1"; 
                                                                }%>

                                                             <%=FormGenerator2.createDropDownListWidget(null, columnOptions, "bomViewList"+sectionCount, selectedView,
                                                                    "setViewDisplay(this.options[this.selectedIndex].value,'" + section+ "TabEditorTable'," + visCount + ");",
                                                                    true, false, viewOrder, null, null, false, "", "","", noneSelectedLabel, false)%> 
                                                            </div>
                                                            <div class="bom-table-header-menu-item">
                                                            	<a title='<%= FormatHelper.encodeForHTMLAttribute(editorHelp) %>' href="javascript:createDialogWindow('<%=URL_CONTEXT%>/html/measurements/EditorHelp.jsp?Editor=BOM', 'EditorHelp', '450', '575')">
                                                                	<img  src="<%=WT_CLIENT_IMAGE_LOCATION%>/help.png">
                                                            	</a>
                                                            </div>
                                                        </div>
                                                  
                                                
                                                  
                                                  <!-- card internal table row -->
                                                  <script type="text/javascript">
	                                                toggleDiv('hideShowBoxes<%= section %>');
	
	                                                tca = {};
	                                                for(var n in columnAssign){
	                                                    tca[n] = columnAssign[n];
	                                                }
	                                                taa = new Array();
	                                                for(var n = 0; n < tAssign.length; n++){
	                                                    taa[n] = tAssign[n];
	                                                }
	                                                columnAssignMap['<%= section %>TabEditorTable'] = tca;
	                                                tAssignMap['<%= section %>TabEditorTable'] = taa;
	
	                                               </script>
                                                  
                                                  <div class="table-wrapper" onscroll="hideSelectorDiv();">
                                                    <table id="<%= section %>TabEditorTable" align=left width="100%" class="editor TABLE_OUTLINE BOM-internal-EditorTable" border="0" cellspacing="1" cellpadding="0" onClick="handleTableClick()">
                                                           <tr id="columns">
                                                               <td scope="row" class="TABLESUBHEADER" align="left" style="padding-left: 4px;  width: 27px; min-width: 27px; max-width: 27px; left: 0; z-index: 16 !important;"><div class="table-header-wrapper"> 
                                                                    <div class="checkbox-body-table"> 
                                                                        <label class="checkbox-label-table">
                                                                            <input type="checkbox" id="<%= section %>selectAllCheckBox" value="false" onclick="javascript:selectDomTable('<%= section %>TabEditorTable');toggleAllSectionItems(event);">
                                                                            <span class="checkbox-custom rectangular">
                                                                            </span>
                                                                        </label>
                                                                    </div>
                                                               </td>

                                                               <td class="TABLESUBHEADER" align="left" style="padding-left: 0px;  width: 27px; min-width: 27px; max-width: 27px; left: 37px; z-index: 16 !important;" > 
                                                                   <div class="table-header-wrapper"> 
                                                                   </div>
                                                               </td>


                                                               <td class="TABLESUBHEADER" align="left" style="padding-left: 0px;  width: 100px; min-width: 100px; max-width: 100px; left: 74px; z-index: 16 !important;">
                                                                   <div class="table-header-wrapper"> 
                                                                   </div>
                                                               </td>
                                                               <%-- Removing icon_close.svg image from these three columns 
                                                               <td class="TABLESUBHEADER"><div class="table-header-wrapper"><a href="javascript:toggleViewColumn('HC<%= section %>TabEditorTable_1','<%= section %>TabEditorTable');"><img class="hide-table-column" src="<%=URL_CONTEXT%>/images/icon_close.svg" border="0" onmouseover="return overlib('<%= "<b>" + FormatHelper.encodeForHTMLThenJavascript(insertLabel) + "</b>" %>');" onmouseout="return nd();"></a></div></td>
                                                               <td class="TABLESUBHEADER"><div class="table-header-wrapper"><a href="javascript:toggleViewColumn('HC<%= section %>TabEditorTable_2','<%= section %>TabEditorTable');"><img class="hide-table-column" src="<%=URL_CONTEXT%>/images/icon_close.svg" border="0" onmouseover="return overlib('<%= "<b>" + FormatHelper.encodeForHTMLThenJavascript(selectLabel) + "</b>" %>');" onmouseout="return nd();"></a></div></td>
                                                               <td class="TABLESUBHEADER"><div class="table-header-wrapper"><a href="javascript:toggleViewColumn('HC<%= section %>TabEditorTable_3','<%= section %>TabEditorTable');"><img class="hide-table-column" src="<%=URL_CONTEXT%>/images/icon_close.svg" border="0" onmouseover="return overlib('<%= "<b>" + FormatHelper.encodeForHTMLThenJavascript(expandLabel) + "</b>" %>');" onmouseout="return nd();"></a></div></td>
                                                               --%>

                                                               <%
                                                               // BOM.partName column (no ability to hide column using "X" icon)
                                                                  vis = visColumns.iterator();
                                                                  if (vis.hasNext()) {
                                                                    String columnDisplay = (String) vis.next();
                                                                    String ck = (String)displayToKeyMap.get(columnDisplay);

                                                                    actualPosition = ((Integer)nameToLogicalMap.get(ck)).intValue() + count;
                                                               %>
                                                               <td class="TABLESUBHEADER" align="left" style="padding-left: 0px; width: 254px; min-width: 254px; max-width: 254px; left: 176px; z-index: 16 !important;"><div class="table-header-wrapper"><%=FormatHelper.encodeAndFormatForHTMLContent(columnDisplay)%></div></td>
                                                               <% } %>

                                                               <%
                                                               // BOM.materialDescription column (no ability to hide column using "X" icon)
                                                                  if (vis.hasNext()) {
                                                                    String columnDisplay = (String) vis.next();
                                                                    String ck = (String)displayToKeyMap.get(columnDisplay);

                                                                    actualPosition = ((Integer)nameToLogicalMap.get(ck)).intValue() + count;
                                                               %>
                                                               <td class="TABLESUBHEADER PIN" align="left" style="padding-left: 0px;  min-width: 178px; left: 432px !important; z-index: 16 !important; border-right: 1px solid #A0A0A0;"><div class="table-header-wrapper"><%=FormatHelper.encodeAndFormatForHTMLContent(columnDisplay)%></div></td>
                                                               <% } %>

                                                               <%
                                                                  while(vis.hasNext()){
                                                                    String columnDisplay = (String) vis.next();
                                                                    String ck = (String)displayToKeyMap.get(columnDisplay);

                                                                    actualPosition = ((Integer)nameToLogicalMap.get(ck)).intValue() + count;
																	String attWrappableStyle = "nowrap";
																	String textWrapStyle = "";

                                                                    if(attKeyToWrapValueMap.containsKey(ck)){
                                                                        attWrappableStyle = "table-layout:fixed;min-width:"+ (Integer)attKeyToWrapValueMap.get(ck) +"px;max-width:"+ (Integer)attKeyToWrapValueMap.get(ck) +"px; padding-right: 5px";
                                                                    }
                                                               %>
                                                              <td  style="<%= attWrappableStyle %>" class="TABLESUBHEADER" align="left"><div class="table-header-wrapper"><a href="javascript:;" onclick="javascript:toggleViewColumn('HC<%= section %>TabEditorTable_<%= actualPosition %>','<%= section %>TabEditorTable'); javascript:fixWidthForLastWrapColumn();"><img class="hide-table-column" src="<%=URL_CONTEXT%>/images/icon_close.svg" border="0" onmouseover="return overlib('<%= "<b>" + FormatHelper.encodeForHTMLThenJavascript(columnDisplay) + "</b>" %>');" onmouseout="return nd();"></a>&nbsp;<%=FormatHelper.encodeAndFormatForHTMLContent(columnDisplay) %></div></td>
                                                               <% } %>
                                                            </tr>


                                                           <tr id="columns" style="display:none;">
                                                               <td class="TABLESUBHEADER" align="left"><a href="javascript:toggleViewColumn('HC<%= section %>TabEditorTable_2','<%= section %>TabEditorTable');"><img src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" border="0" onmouseover="return overlib('<%= "<b>" + FormatHelper.encodeForHTMLThenJavascript(selectLabel) + "</b>" %>');" onmouseout="return nd();"></a></td>
                                                               <td class="TABLESUBHEADER" align="left"><a href="javascript:toggleViewColumn('HC<%= section %>TabEditorTable_1','<%= section %>TabEditorTable');"><img src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" border="0" onmouseover="return overlib('<%= "<b>" + FormatHelper.encodeForHTMLThenJavascript(insertLabel) + "</b>" %>');" onmouseout="return nd();"></a></td>
                                                               <td class="TABLESUBHEADER" align="left"><a href="javascript:toggleViewColumn('HC<%= section %>TabEditorTable_3','<%= section %>TabEditorTable');"><img src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" border="0" onmouseover="return overlib('<%= "<b>" + FormatHelper.encodeForHTMLThenJavascript(expandLabel) + "</b>" %>');" onmouseout="return nd();"></a></td>
                                                               <%
                                                                  vis = visColumns.iterator();
                                                                  while(vis.hasNext()){
                                                                    String columnDisplay = (String) vis.next();
                                                                    String ck = (String)displayToKeyMap.get(columnDisplay);

                                                                    actualPosition = ((Integer)nameToLogicalMap.get(ck)).intValue() + count;
                                                               %>
                                                                <td class="TABLESUBHEADER" align="left"><a href="javascript:;" onclick="javascript:toggleViewColumn('HC<%= section %>TabEditorTable_<%= actualPosition %>','<%= section %>TabEditorTable');javascript:fixWidthForLastWrapColumn();"><img src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" border="0" onmouseover="return overlib('<%= "<b>" + FormatHelper.encodeForHTMLThenJavascript(columnDisplay) + "</b>" %>');" onmouseout="return nd();"></a>&nbsp;<%=FormatHelper.encodeAndFormatForHTMLContent(columnDisplay) %></td>
                                                               <% } %>
                                                            </tr>
                                                        </table>
                                                        </div>
                                                  
                                            </div>
										  </div>
                                    </div>
                                    <!-- End of section -->

                                <%  sectionCount++;
                            } // end while sections has next
                            %>
                            </div> <!-- End of wrapped table-wrappers -->
                            <% if(ENABLE_DEBUG_SECTION){ %>
                          <div id="debugDiv" class="BoxWrapper">
                                <div class="BoXContent">
                                      <table>
                                        <tr>
                                            <td>
                                            <a class="button" onClick="document.getElementById('debugText').value = dataModel.dumpModel()"><%= modelButton %></a>
                                            <a class="button"  onClick="document.getElementById('debugText').value = dumpSelectedRow()"><%= rowButton %></a>
                                            <a class="button"  onClick="document.getElementById('debugText').value = bomDataModel.dumpModel()">BOM DM</a>
                                            <a class="button"  onClick="dumpSelectedBOMDMRow()">BOM DM ROW</a>
                                            <a  class="button" onClick="document.getElementById('debugText').value = ''">Clear</a>
                                            <a  class="button"  onClick="debug(getDataString(true))">Save Test</a>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><textarea id="debugText" style="size:7pt" cols="150" rows="20"></textarea></td>
                                        </tr>
                                    </table>
                                 </div>
                            </div>
                          <% } %>
                            </div>
		</div>
		
		
	</div>
</div>
<!-- End of BOM Content -->  
            <!-- SOURCE CELLS FOR DEFINING WIDGETS -->
               <table><tr>
                <%
                columnIter = columnList.iterator();
                while(columnIter.hasNext()){
                    columnKey = (String) columnIter.next();
                    if(columnKey.endsWith("BOM.partName")){
                        %>
                        
                        <td id="partNameSource"><input type="text" name="partNameSource" size="30"></td>
                        <%
                        continue;
                    }
                    if(columnKey.equals("BOM.branchId")){
                        %>
                        <td id="brchIdSource"><input type="text" name="brchId"></td>
                        <%
                        continue;
                    }
                    if(columnKey.equals("colorDim")){
                        %>
                        <td id="colorDimSource"><input type="text" name="colorDim"></td>
                        <%
                        continue;
                    }
                    if(columnKey.equals("sourceDim")){
                        %>
                        <td id="sourceDimSource"><input type="text" name="sourceDim"></td>
                        <%
                        continue;
                    }
                    if(columnKey.equals("destinationDim")){
                        %>
                        <td id="destinationDimSource"><input type="text" name="destinationDim"></td>
                        <%
                        continue;
                    }
                    if(columnKey.equals("size1Dim")){
                        %>
                        <td id="size1DimSource"><input type="text" name="size1Dim"></td>
                        <%
                        continue;
                    }
                    if(columnKey.equals("size2Dim")){
                        %>
                        <td id="size2DimSource"><input type="text" name="size2Dim"></td>
                        <%
                        continue;
                    }

                    if(columnKey.equals("SUPPLIER.name")){

                        %>
                        <td id="supplierNameSource"><input type="text" name="supplierName"></td>
                        <%
                        continue;
                    }

                    if(columnKey.equals("BOM.wcPartName")){

                        att = (FlexTypeAttribute) attMap.get(columnKey);
                        if(WCPART_ENABLED) {
                        %>
                        <td id="wcPartNameSource"><div id="wcPartNameSourceDiv"><%= fg.createItemPickerWidget("wcPartName", "", att) %></div></td>
                        <% } else {%>
                        <td id="wcPartNameSource"><div id="wcPartNameSourceDiv"><a href="javascript:doNothing()">Part:</a>&nbsp;<input type="hidden" value="" name="wcPartName" id="wcPartName"><div id="wcPartNameDisplay" tabindex="0"><a href="javascript:doNothing()">------</a></div></div></td>
                   <%   }
                        continue;
                    }

                    att = (FlexTypeAttribute) attMap.get(columnKey);

                    if(att == null){
                        continue;
                    }
                    String attKey = FormatHelper.replaceCharacter(columnKey, ".", "_");
                    if(columnKey.indexOf("BOM.") == 0){
                        attKey = att.getAttKey();
                    }
                    if(att.isAttUpdateable() && columnKey.indexOf("BOM.") > -1){
                        flexg.setScope(FlexBOMFlexTypeScopeDefinition.LINK_SCOPE);
                        flexg.setMultiClassForm(true);
                    %>
                    <td id="<%= attKey %>Source"><div id="<%= attKey %>SourceDiv" style="padding:1.5px" ><%= flexg.drawFormWidget(att, null, null, false, null, true) %></div></td>
                    <%
                        flexg.setScope(null);
                    } else { %>
                    <td id="<%= attKey %>Source"><div id="<%= attKey %>SourceDiv"></div></td>
                    <% } %>
                <% } %>
                <td id="materialColorListSource"><select name="materialColorListSelect"></select></td>
</tr>
            </table>
 <% if("PRODUCT".equals(ownerMode)){ %>
    <tr>
        <td>
        <div id='specReportDiv'>
        	<div class="bom-overlib-addOn">
            	<%= specReport %>
            </div>
        </div>
        </td>
    </tr>
    <tr>
        <td>
        <div id='synchReportDiv'>
        	<div class="bom-overlib-addOn">
            	<%= synchReport %>
            </div>
        </div>
        </td>
    </tr>
    
<script>

    var reportDiv = document.getElementById('specReportDiv');
    specReport = reportDiv.innerHTML;
    reportDiv = document.getElementById('synchReportDiv');
    synchReport = reportDiv.innerHTML;
    hideDiv('specReportDiv');
    hideDiv('synchReportDiv');
</script>
<% } %>

<script type="text/javascript"> //toggleDiv("debugDiv"); </script>
</table>






<%  //  The HIGHLIGHT_color must be defined as a CSS class in the main.css file
    String [] highLightColors = { "HIGHLIGHT_YELLOW",
                                  "HIGHLIGHT_ORANGE",
                                  "HIGHLIGHT_GREEN",
                                  "HIGHLIGHT_BLUE",
                                  "HIGHLIGHT_PINK",
                                  "HIGHLIGHT_ROSE",
                                  "HIGHLIGHT_PALEBLUE",
                                  "HIGHLIGHT_PURPLE",
                                  "HIGHLIGHT_GRAY_TEXT_STRIKETHROUGH_RED",
                                  "HIGHLIGHT_GRAY_TEXT_STRIKETHROUGH_BLUE",
                                  "HIGHLIGHT_GRAY_TEXT_STRIKETHROUGH_GREEN",
                                  "HIGHLIGHT_GRAY_TEXT_STRIKETHROUGH_ORANGE"
                                 };
%>
<div id='highLightMenu'>
            <table border="0">
                <tr>
                    <td align="middle" colspan="4" id="HIGHLIGHT_WHITE" class="HIGHLIGHT_WHITE PALETTEBORDER" onclick="javascript:highLightBOMRows('HIGHLIGHT_WHITE');" onMouseOver="this.className='HIGHLIGHT_WHITE PALETTEBORDERACTIVE';" onMouseOut="this.className='HIGHLIGHT_WHITE PALETTEBORDER';">
                        <%= noneLabel %>
                    </td>
                    <td rowspan="4" id="white" bgcolor="#FFFFFF">
                        <%= highLightBOMRowColorMsg %>
                    </td>
                </tr>

                <tr>
                    <%
                    int j = 1;
                    String code = "&nbsp;&nbsp;&nbsp;"; // single letter code to indicate which color in the highlight palette is linked to the automatic highlighting attribute
                    Map reservedLabel =new HashMap();
                    boolean reservedColorsConfigured = false;
                    for (int i=0; i<4; i++) { // First Four highLight colors
                        // if the palette color is currently configured as an auto-highlight color tag it as such
                        if (FormatHelper.hasContent((String)autoHighlightColors.get(highLightColors[i]))) {
                            code = "&nbsp;"+j+"&nbsp;";
                            reservedLabel.put("" + j, (String)autoHighlightCodes.get(highLightColors[i]));
                            reservedColorsConfigured = true;
                            j++;
                        } else {
                            code = "&nbsp;&nbsp;&nbsp;";
                        }
                     %>
                        <td id="<%= highLightColors[i] %>" class="<%= highLightColors[i] %> PALETTEBORDER" onclick="javascript:highLightBOMRows('<%= highLightColors[i] %>');" onMouseOver="this.className='<%= highLightColors[i]%> PALETTEBORDERACTIVE';" onMouseOut="this.className = '<%= highLightColors[i] %> PALETTEBORDER';">
                            <%= code %>
                            </td>
                    <% } %>
                </tr>
                <tr>
                    <% for (int i=4; i<8; i++) { // last Four highLight colors%>
                    <%
                        // if the palette color is currently configured as an auto-highlight color tag it as such
                        if (FormatHelper.hasContent((String)autoHighlightColors.get(highLightColors[i]))) {
                            code = "&nbsp;"+j+"&nbsp;";
                            reservedLabel.put("" + j, (String)autoHighlightCodes.get(highLightColors[i]));
                            reservedColorsConfigured = true;
                            j++;
                        } else {
                            code = "&nbsp;&nbsp;&nbsp;";
                        }
                     %>
                        <td id="<%= highLightColors[i] %>" class="<%= highLightColors[i] %> PALETTEBORDER" onclick="javascript:highLightBOMRows('<%= highLightColors[i] %>');" onMouseOver="this.className='<%= highLightColors[i]%> PALETTEBORDERACTIVE';" onMouseOut="this.className = '<%= highLightColors[i] %> PALETTEBORDER';">
                            <%= code %>
                        </td>
                    <% } %>
                </tr>
                <tr>
                    <% //RED STRIKE THROUGH
                        if (FormatHelper.hasContent((String)autoHighlightColors.get(highLightColors[8]))) {
                            code = "&nbsp;"+j+"&nbsp;";
                            reservedLabel.put("" + j, (String)autoHighlightCodes.get(highLightColors[8]));
                            reservedColorsConfigured = true;
                            j++;
                        } else {
                            code = "&nbsp;&nbsp;R&nbsp;";

                        }
                    %>
                    <td id="<%= highLightColors[8] %>" class="<%= highLightColors[8] %> PALETTEBORDER" onclick="javascript:highLightBOMRows('<%= highLightColors[8] %>');" onMouseOver="this.className='<%= highLightColors[8]%> PALETTEBORDERACTIVE';" onMouseOut="this.className = '<%= highLightColors[8] %> PALETTEBORDER';">
                        <%= code %>
                    </td>

                    <%  // BLUE STRIKE THROUGH
                        if (FormatHelper.hasContent((String)autoHighlightColors.get(highLightColors[9]))) {
                            code = "&nbsp;"+j+"&nbsp;";
                            reservedLabel.put("" + j, (String)autoHighlightCodes.get(highLightColors[9]));
                            reservedColorsConfigured = true;
                            j++;
                        } else {
                            code = "&nbsp;&nbsp;B&nbsp;";
                        }
                    %>
                    <td id="<%= highLightColors[9] %>" class="<%= highLightColors[9] %> PALETTEBORDER" onclick="javascript:highLightBOMRows('<%= highLightColors[9] %>');" onMouseOver="this.className='<%= highLightColors[9]%> PALETTEBORDERACTIVE';" onMouseOut="this.className = '<%= highLightColors[9] %> PALETTEBORDER';">
                        <%= code %>
                    </td>

                    <% // GREEN STRIKE THROUGH
                        if (FormatHelper.hasContent((String)autoHighlightColors.get(highLightColors[10]))) {
                            code = "&nbsp;"+j+"&nbsp;";
                            reservedLabel.put("" + j, (String)autoHighlightCodes.get(highLightColors[10]));
                            reservedColorsConfigured = true;
                            j++;
                        } else {
                            code = "&nbsp;&nbsp;G&nbsp;";
                        }
                    %>
                    <td id="<%= highLightColors[10] %>" class="<%= highLightColors[10] %> PALETTEBORDER" onclick="javascript:highLightBOMRows('<%= highLightColors[10] %>');" onMouseOver="this.className='<%= highLightColors[10]%> PALETTEBORDERACTIVE';" onMouseOut="this.className = '<%= highLightColors[10] %> PALETTEBORDER';">
                        <%= code %>
                    </td>

                    <%  // ORANGE STRIKE THROUGH
                        if (FormatHelper.hasContent((String)autoHighlightColors.get(highLightColors[11]))) {
                            code = "&nbsp;"+j+"&nbsp;";
                            reservedLabel.put("" + j, (String)autoHighlightCodes.get(highLightColors[11]));
                            reservedColorsConfigured = true;
                            j++;
                        } else {
                            code = "&nbsp;&nbsp;O&nbsp;";
                        }
                    %>
                    <td id="<%= highLightColors[11] %>" class="<%= highLightColors[11] %> PALETTEBORDER" onclick="javascript:highLightBOMRows('<%= highLightColors[11] %>');" onMouseOver="this.className='<%= highLightColors[11]%> PALETTEBORDERACTIVE';" onMouseOut="this.className = '<%= highLightColors[11] %> PALETTEBORDER';">
                        <%= code %>
                    </td>
                </tr>
                <% if (reservedColorsConfigured) { %>
                    <tr>
                        <td colspan="4" bgcolor="white">
                            <%=reservedColorLabel%><br>
                            <%
                            Collection rcKeys = reservedLabel.keySet();
                            Iterator rcKeyIter = SortHelper.sortStringsAsDoubles(rcKeys, true).iterator();
                            while (rcKeyIter.hasNext()) {
                               String keyLabel = (String)rcKeyIter.next();
                            %>
                               &nbsp;<%=keyLabel%> = <%=reservedLabel.get(keyLabel)%><br>
                            <% } %>
                        </td>
                    </tr>
                <% } %>
            </table>
</div>


<script type="text/javascript">
    var menuArray = {};

    var hm = document.getElementById('highLightMenu');
    menuArray['highLightMenu'] = hm.innerHTML;
    hm.innerHTML = '';

    function selectRowAndShowMenu(me, event) {
        endCellEdit();
        selectRowForCellImage(me);
        openDropdownMenuForStickyColumn(event);
    }
    
    <% 
    	IconButtonRenderer iconButtonRenderer = new DefaultIconButtonRenderer();
        DropdownMenuIcon tmdropdownMenuIcon = new DropdownMenuIcon();
        tmdropdownMenuIcon.setPopupOnClick(true);
        tmdropdownMenuIcon.setImageUrl(MORE_ACTIONS_ICON_URL);
        tmdropdownMenuIcon.setOnClick(JavascriptFunctionCall.create("selectRowAndShowMenu").addArgument(Argument.asJsVariable("this"))
            .addArgument(Argument.asJsVariable("event")));
        tmdropdownMenuIcon.setMenuContentId("bomTable-menuImage-action");
        
        tmdropdownMenuIcon.addSubAction(new ActionLink("insertAfter", insertAfterLabel, 
                JavascriptFunctionCall.create("insertRow").addArgument(Argument.asBoolean(false))));                                    	
        tmdropdownMenuIcon.addSubAction(new ActionLink("insertBefore", insertBeforeLabel,
                JavascriptFunctionCall.create("insertRow").addArgument(Argument.asBoolean(true))));    
        tmdropdownMenuIcon.addSubAction(new ActionLink("delete", deleteLabel, JavascriptFunctionCall.create("removeRow").addArgument(Argument.asBoolean(true))));
        tmdropdownMenuIcon.addSubAction(new ActionLink("cut", cutLabel, JavascriptFunctionCall.create("cutRow")));   
        tmdropdownMenuIcon.addSubAction(new ActionLink("copy", copyLabel, JavascriptFunctionCall.create("copyRow")));   
        tmdropdownMenuIcon.addSubAction(new ActionLink("paste", pasteLabel, JavascriptFunctionCall.create("pasteRow").addArgument(Argument.asBoolean(false))));
        if(includeSetAsPrimary && !USE_AUTO_SET_PRIMARY_MATERIAL){ 
            tmdropdownMenuIcon.addSubAction(new ActionLink("setAsPrimary", setAsPrimaryLabel, JavascriptFunctionCall.create("RFABOM.setAsPrimary").addArgument(Argument.asBoolean(true))));
        }
     %>
     var tableMenuWidget = '<%= iconButtonRenderer.renderDropdownMenuIconButton(tmdropdownMenuIcon) %>';
     menuArray['tableMenu'] = tableMenuWidget;

    <% 
        DropdownMenuIcon cmdropdownMenuIcon = new DropdownMenuIcon();
        cmdropdownMenuIcon.setPopupOnClick(true);
        cmdropdownMenuIcon.setImageUrl(MORE_ACTIONS_ICON_URL);
        cmdropdownMenuIcon.setOnClick(JavascriptFunctionCall.create("selectRowAndShowMenu").addArgument(Argument.asJsVariable("this"))
            .addArgument(Argument.asJsVariable("event")));
        cmdropdownMenuIcon.setMenuContentId("bomTable-menuImage-action");
        
        cmdropdownMenuIcon.addSubAction(new ActionLink("delete", deleteLabel, JavascriptFunctionCall.create("removeRow").addArgument(Argument.asBoolean(true))));
     %>
     var childMenuWidget = '<%= iconButtonRenderer.renderDropdownMenuIconButton(cmdropdownMenuIcon) %>';
     menuArray['childMenu'] = childMenuWidget;

    <% 
        DropdownMenuIcon samdropdownMenuIcon = new DropdownMenuIcon();
        samdropdownMenuIcon.setPopupOnClick(true);
        samdropdownMenuIcon.setImageUrl(MORE_ACTIONS_ICON_URL);
        samdropdownMenuIcon.setOnClick(JavascriptFunctionCall.create("selectRowAndShowMenu").addArgument(Argument.asJsVariable("this"))
            .addArgument(Argument.asJsVariable("event")));
        samdropdownMenuIcon.setMenuContentId("bomTableTable-menuImage-action");
        
        samdropdownMenuIcon.addSubAction(new ActionLink("insertAfter", insertAfterLabel, 
                JavascriptFunctionCall.create("insertRow").addArgument(Argument.asBoolean(false))));                                    	
        samdropdownMenuIcon.addSubAction(new ActionLink("insertBefore", insertBeforeLabel,
                JavascriptFunctionCall.create("insertRow").addArgument(Argument.asBoolean(true))));    
        samdropdownMenuIcon.addSubAction(new ActionLink("delete", deleteLabel, JavascriptFunctionCall.create("removeRow").addArgument(Argument.asBoolean(true))));
        if(includeSetAsPrimary && !USE_AUTO_SET_PRIMARY_MATERIAL){ 
            samdropdownMenuIcon.addSubAction(new ActionLink("setAsPrimary", setAsPrimaryLabel, JavascriptFunctionCall.create("RFABOM.setAsPrimary").addArgument(Argument.asBoolean(true))));
        }
     %>
     var subAsMenuWidget = '<%= iconButtonRenderer.renderDropdownMenuIconButton(samdropdownMenuIcon) %>';
     menuArray['subAsMenu'] = subAsMenuWidget;

</script>
<script type="text/javascript">
    //freshTest();
</script>
</body>
<script type="text/javascript">
    var cellTypes = {};
    var columnList = new Array();
    var alignments = {};
    var defaultValues = {};
    var defaultValuesDisplay = {};
    var classNames = {};
    var precisions = {};
    var sourceCell;
    var widget;
    <%
    columnIter = columnList.iterator();

    while(columnIter.hasNext()){

        columnKey = (String) columnIter.next();
        if(columnKey.equals("BOM.branchId")){

            %>
            sourceCell = document.getElementById('brchIdSource');
            widget = sourceCell.childNodes[0];
            while(sourceCell.childNodes.length > 0){
                var child = sourceCell.removeChild(sourceCell.childNodes[0]);
                if(!("TEXT" == child.tagName)){
                    widget = child;
                }
            }
            widgets["brchId"] = widget;
            cellTypes["brchId"] = "text";
            columnList[columnList.length] = "brchId";
            <%
            continue;
        }
        if(columnKey.equals("colorDim")){

            %>
            sourceCell = document.getElementById('colorDimSource');
            widget = sourceCell.childNodes[0];
            while(sourceCell.childNodes.length > 0){
                var child = sourceCell.removeChild(sourceCell.childNodes[0]);
                if(!("TEXT" == child.tagName)){
                    widget = child;
                }
            }
            widgets["colorDim"] = widget;
            cellTypes["colorDim"] = "text";
            columnList[columnList.length] = "colorDim";
            <%
            continue;
        }
        if(columnKey.equals("sourceDim")){

            %>
            sourceCell = document.getElementById('sourceDimSource');
            widget = sourceCell.childNodes[0];
            while(sourceCell.childNodes.length > 0){
                var child = sourceCell.removeChild(sourceCell.childNodes[0]);
                if(!("TEXT" == child.tagName)){
                    widget = child;
                }
            }
            widgets["sourceDim"] = widget;
            cellTypes["sourceDim"] = "text";
            columnList[columnList.length] = "sourceDim";
            <%
            continue;
        }
        if(columnKey.equals("destinationDim")){

            %>
            sourceCell = document.getElementById('destinationDimSource');
            widget = sourceCell.childNodes[0];
            while(sourceCell.childNodes.length > 0){
                var child = sourceCell.removeChild(sourceCell.childNodes[0]);
                if(!("TEXT" == child.tagName)){
                    widget = child;
                }
            }
            widgets["destinationDim"] = widget;
            cellTypes["destinationDim"] = "text";
            columnList[columnList.length] = "destinationDim";
            <%
            continue;
        }
        if(columnKey.equals("size1Dim")){

            %>
            sourceCell = document.getElementById('size1DimSource');
            widget = sourceCell.childNodes[0];
            while(sourceCell.childNodes.length > 0){
                var child = sourceCell.removeChild(sourceCell.childNodes[0]);
                if(!("TEXT" == child.tagName)){
                    widget = child;
                }
            }
            widgets["size1Dim"] = widget;
            cellTypes["size1Dim"] = "text";
            columnList[columnList.length] = "size1Dim";
            <%
            continue;
        }
        if(columnKey.equals("size2Dim")){

            %>
            sourceCell = document.getElementById('size2DimSource');
            widget = sourceCell.childNodes[0];
            while(sourceCell.childNodes.length > 0){
                var child = sourceCell.removeChild(sourceCell.childNodes[0]);
                if(!("TEXT" == child.tagName)){
                    widget = child;
                }
            }
            widgets["size2Dim"] = widget;
            cellTypes["size2Dim"] = "text";
            columnList[columnList.length] = "size2Dim";
            <%
            continue;
        }

        if(columnKey.equals("SUPPLIER.name")){

            %>
            sourceCell = document.getElementById('supplierNameSource');
            widget = sourceCell.childNodes[0];
            while(sourceCell.childNodes.length > 0){
                var child = sourceCell.removeChild(sourceCell.childNodes[0]);
                if(!("TEXT" == child.tagName)){
                    widget = child;
                }
            }
            widgets["supplierName"] = widget;
            cellTypes["supplierName"] = "text";
            columnList[columnList.length] = "supplierName";
            <%
            continue;
        }

        att = (FlexTypeAttribute) attMap.get(columnKey);
        if(att == null){
            continue;
        }
        String attKey = FormatHelper.replaceCharacter(columnKey, ".", "_");
        if(columnKey.indexOf("BOM.") == 0){
            attKey = att.getAttKey();
        }

        %>
        sourceCell = document.getElementById('<%= attKey %>Source');
        widget = sourceCell.childNodes[0];
        //alert(sourceCell.id + " child count = " + sourceCell.childNodes.length);
        while(sourceCell.childNodes.length > 0){
            //alert(sourceCell.id + " child = " + sourceCell.childNodes[0].tagName);
            var child = sourceCell.removeChild(sourceCell.childNodes[0]);
            if(!("TEXT" == child.tagName)){
                widget = child;
            }
        }



        <%
        ///////////////////////////////////////////////////////////////////////////////////////////
        // HANDLE THE COLOR WIDGET. NEED TO DRAW MULTIPLE TIMES FOR ALL COLOR MODES
        ///////////////////////////////////////////////////////////////////////////////////////////
        if( ("colorDescription".equals(multiAttribute) && "colorDescription".equals(att.getAttKey())) ||
            ("materialDescription".equals(multiAttribute) && "materialDescription".equals(att.getAttKey()))
           ){
            skuIter = skuList.iterator();
            while(skuIter.hasNext()){
                skuObj = (FlexObject) skuIter.next();
                %>
                widgets["<%= att.getAttKey() + "$sku$" + skuObj.getString(skuIDIndex) %>"] = widget;
                cellTypes["<%= att.getAttKey() + "$sku$" + skuObj.getString(skuIDIndex) %>"] = "<%= att.getAttVariableType() %>";
                precisions["<%= att.getAttKey() + "$sku$" + skuObj.getString(skuIDIndex) %>"] = "<%= att.getAttDecimalFigures() %>";
                columnList[columnList.length] = "<%= att.getAttKey() + "$sku$" + skuObj.getString(skuIDIndex) %>";
                <%
            }
            %>
                widgets["<%= att.getAttKey() %>"] = widget;
                cellTypes["<%= att.getAttKey() %>"] = "<%= att.getAttVariableType() %>";
                precisions["<%= att.getAttKey() %>"] = "<%= att.getAttDecimalFigures() %>";
                columnList[columnList.length] = "<%= att.getAttKey() %>";
        <%
         ///////////////////////////////////////////////////////////////////////////////////////////
        // HANDLE THE QUANTITY WIDGET. NEED TO DRAW MULTIPLE TIMES FOR ALL SIZE MODES
        ///////////////////////////////////////////////////////////////////////////////////////////
        } else if("quantity".equals(multiAttribute) && "quantity".equals(att.getAttKey())){

            Iterator sizeIter = null;
            String sizeDim = "";
            if("SIZE1".equals(multiDimension)){
                sizeIter = size1Collection.iterator();
                sizeDim = "size1";
            } else {
                sizeIter = size2Collection.iterator();
                sizeDim = "size2";
            }
            String size;
            while(sizeIter.hasNext()){
                size = (String) sizeIter.next();
                %>
                widgets["<%= att.getAttKey() + "$" + sizeDim + "$" + size %>"] = widget;
                cellTypes["<%= att.getAttKey() + "$" + sizeDim + "$" + size %>"] = "<%= att.getAttVariableType() %>";
                precisions["<%= att.getAttKey() + "$" + sizeDim + "$" + size %>"] = "<%= att.getAttDecimalFigures() %>";
                columnList[columnList.length] = "<%= att.getAttKey() + "$" + sizeDim + "$" + size %>";
                <%
            }

        } else {
            %>
            widgets["<%= attKey %>"] = widget;
            cellTypes["<%= attKey %>"] = "<%= att.getAttVariableType() %>";
            precisions["<%= attKey %>"] = "<%= att.getAttDecimalFigures() %>";
            columnList[columnList.length] = "<%= attKey %>";
            <%if(att.getAttFlexType().getRootType().getFullName().equals("BOM")){
                if(FormatHelper.hasContent(att.getAttDerivedFrom())) {
                    if(FormatHelper.hasContent(att.getAttDefaultValue())) { %>
                        defaultValues["<%= attKey %>"] = "<%= FormatHelper.encodeForJavascript(att.getAttDefaultValue()) %>";
                    <%} else {
                        if(att.isStringDatatype()) { %>
                            defaultValues["<%= attKey %>"] = "";
                    <%  } else if(att.isNumericDatatype()) { %>
                            defaultValues["<%= attKey %>"] = 0;
                    <%  }
                    } %>
                    defaultValuesDisplay["<%= attKey %>"] = "<%= FormatHelper.encodeForJavascript(derivedFromOtherAttributeStringLbl) %>";
                    <%
                } else if(FormatHelper.hasContent(att.getAttDefaultValue())){ %>
                    defaultValues["<%= attKey %>"] = "<%= FormatHelper.encodeForJavascript(att.getAttDefaultValue()) %>";
                    defaultValuesDisplay["<%= attKey %>"] = "<%= FormatHelper.encodeForJavascript(att.getDisplayValue(att.getAttDefaultValue())) %>";
            <%
                } else if("date".equals( att.getAttVariableType() ) && "true".equalsIgnoreCase( att.getAddtlParam( FlexTypeAttribute.DATE_DEFAULT_TO_CURRENT ) ) ){
                    Date currentDate = new Date(System.currentTimeMillis()) ;
                    String defaultToCurrentDateString = FormatHelper.format(currentDate);
                    SimpleDateFormat sdf = new SimpleDateFormat(FormatHelper.DATE_ONLY_YMD);
                    String defaultToCurrentDateValueString = sdf.format(currentDate);

                %>
                        defaultValues["<%= attKey %>"] = "<%= defaultToCurrentDateValueString%>";
                        defaultValuesDisplay["<%= attKey %>"] = "<%= defaultToCurrentDateString %>";
               <% }
           }
       }
    }
    %>


    sourceCell = document.getElementById('materialColorListSource');
    var materialColorListWidget = sourceCell.childNodes[0];
    sourceCell.removeChild(sourceCell.childNodes[0]);

    cellTypes["wcPartName"] = "wc_part_ref";

    var table = document.getElementById("editorTable");
    //var tableMenu = document.getElementById("tableMenu");
    //var childMenu = document.getElementById("childMenu");
   if(is.nav){
        <%
        sectionIter = sections.iterator();
        String tableName;
        while(sectionIter.hasNext()){
            section = (String) sectionIter.next();
            tableName = section + "TabEditorTableDiv";
            %>
            table = document.getElementById("<%= tableName %>");
            table.addEventListener('click',handleTableClick,false);
            <%
        }
        %>
        /*
        tableMenu.addEventListener('onmouseover',highlightMenuOption,false);
        tableMenu.addEventListener('onmouseout',checkHide,false);
        tableMenu.addEventListener('click',invokeMenuAction,false);
        childMenu.addEventListener('onmouseover',highlightMenuOption,false);
        childMenu.addEventListener('onmouseout',checkHide,false);
        childMenu.addEventListener('click',invokeMenuAction,false);
        */
    }

    if(dataModel.dataModel.length == 0){
        //insertRow(null, 1);
    }

    <%

    if(true){
        sectionIter = sections.iterator();
        boolean bHasSessionState = false;
        while(sectionIter.hasNext()){
            section = (String) sectionIter.next();
            if (session.getAttribute(partOid + section + "TabEditorTableDiv") != null) {
                bHasSessionState = true;
            }
            %>
            <%-- toggleDiv('<%= section %>TabEditorTableDiv');
            toggleDiv('<%= section %>TabEditorTableDiv_minus'); --%>
            <%
        }
    %>

    <%
    //only expanse the first section if there is no section data
    //CHUCK - Modified so all sections are expanded if no section data is found

        if (!bHasSessionState) {
            String firstSection = (String) sections.iterator().next();
            sectionIter = sections.iterator();
            while(sectionIter.hasNext()){
                section = (String) sectionIter.next();%>
                setSectionState("<%= section %>TabEditorTableDiv",'plus');
            <% } %>
            currentSectionValue = '<%= firstSection %>';
    <%
        }
    }//if true - why? %>



    <% if(useBOMCosting){ %>
    // FORCE BOM ROLL UP CALCULATION IF COSTING IS ENABLED.
    //calculateBOMTotal(true);
    //toggleDiv('bomCostsDiv');
    <% } %>



    <% if(!topLevelMode){ %>
    // FOR NOW, IF WE ARE NOT IN PRODUCT MODE,
    // THEN DISALLOW ROW ADDITION
    //DOM_TE_ALLOW_ROW_INSERT = false;
    //DOM_TE_ALLOW_ROW_DELETE = false;

    <% } %>
    // CONFIGURABLE MESSAGE
    DOM_TE_ALLOW_ROW_INSERT_MESSAGE = "You may only insert rows while working on the product BOM.";


//DOM_TE_CONFIRM_ROW_DELETE_MESSAGE = "Are you sure you want to remove this row?";
//DOM_TE_MULTI_CONFIRM_ROW_DELETE_MESSAGE = "Are you sure you want to remove these rows?"

//CHUCK - HARDCODED TO TRUE TO ALLOW FOR PAGE LOAD
DOM_TE_ALLOW_ROW_INSERT = true;


</script>

<script type="text/javascript">
<%

    // THE FOLLOWING CODE LOCATES MATERIAL COLOR DATA FOR ALL
    // MATERIALS CURRENTLY BEING USED IN THE BOM AND CREATES A
    // JAVASCRITP MAP OF THE DATA. THIS DATA IS USED TO ASSIGN
    // COLORS TO MATERIALS THAT HAVE BEEN ASSIGNED AND ARE COLOR
    // CONTROLLED.

    Map materialColorMap = new LCSFlexBOMQuery().findAvailableMaterialColorMapForBOM(bomPart);
    Map usedMaterialColorMap = new LCSFlexBOMQuery().findAvailableMaterialColorMapForBOM(bomPart, false, true);


    logger.debug("materialColorMap: " + materialColorMap);
    logger.debug("usedMaterialColorMap: " + usedMaterialColorMap);

    //This will pull in any colors that the user doesn't have access to, but have been used.
    //This is required so that a user doesn't accidentally remove the selected color with no way
    //to put it back.
    //The code that builds the drop down list needs to be updated to check for the NOACCESS = true
    //If that is the case, the value should only be included in the drop down if it is the currently
    //selected value.  This way we prevent the color being selected for other records that have the
    //same material, but a different color.
    BOMEditorUtility.mergeMCMaps(materialColorMap, usedMaterialColorMap);


    logger.debug("materialColorMap: " + materialColorMap);

    //Need to merge any MCs that are used into the list of those available to the user
    //This is to ensure any that are used within the BOM will still be selectable if the user clicks
    //on a cell that that MC is used in.

    %>
        var materialColorMap = {};
        var materialColorData;
        var materialColorList;
        var msId;

        <%
        if(materialColorMap.keySet().size() > 0){
            MaterialPriceList mpl = BOMEditorUtility.getMaterialPriceList(materialColorMap);
            Iterator materialIter = materialColorMap.keySet().iterator();
            Collection matColList;
            Iterator materialColorIter;
            FlexObject materialColorFlexObj;
            String price = "";
            boolean noAccess = false;
            while(materialIter.hasNext()){

                materialId = (String) materialIter.next();
                matColList = (Collection) materialColorMap.get(materialId);
            matColList = SortHelper.sortFlexObjects(matColList, "LCSCOLOR.COLORNAME");
                materialColorIter = matColList.iterator();
                %>
                materialColorList = new Array();
                <%
                while(materialColorIter.hasNext()){
                    materialColorFlexObj = (FlexObject) materialColorIter.next();
                    price = "" + mpl.getPrice(materialId, materialColorFlexObj.getString("LCSMATERIALCOLOR.IDA2A2"));
                    //CMS - adding noAccess flag to allow js checking of user access to the MC while building the color dropdown
                    noAccess = "true".equals(materialColorFlexObj.getString("NOACCESS"));
                    %>
                    materialColorData = {};
                    materialColorData.colorName = '<%= FormatHelper.encodeForJavascript(materialColorFlexObj.getString("LCSCOLOR.COLORNAME")) %>';
                    materialColorData.colorId = '<%= materialColorFlexObj.getString("LCSCOLOR.IDA2A2") %>';
                    materialColorData.materialColorId = '<%= materialColorFlexObj.getString("LCSMATERIALCOLOR.IDA2A2") %>';
                    materialColorData.price = '<%= price %>';
                    materialColorData.noAccess = <%= noAccess %>;
                    materialColorList[materialColorList.length] = materialColorData;
                    <%

                }
                %>
                materialColorMap.M<%= materialId %> = materialColorList;
                <%
            }
        }
    %>


/* define event handler to scroll active row into view when either browser window is 
    a. resized by user 
    b. Material/Color frames trigger content frame resize 
*/
window.onresize = function(event) {
        setTimeout("currentRow.scrollIntoView({behavior: 'auto', block: 'center'})",250);

        // Try centering current "table" first, then scroll row into view, slightly better experience
        //setTimeout("domTableEditor.currentTable({behavior: 'smooth', block: 'center'})",500);
        //setTimeout("currentRow.scrollIntoView({behavior: 'smooth', block: 'nearest'})",1000);
};
    

</script>
<script type="text/javascript">
var sectionArr = new Array();
        <%
        sectionIter = sections.iterator();

        while(sectionIter.hasNext()){
            section = (String) sectionIter.next();
        %>
sectionArr[sectionArr.length] = '<%= section %>';
        <%    }  %>
//Load the top level DOM Table

createTDMTopLevelEntries();
createTopLevelDOMRows(sectionArr);

toggleDiv('loadingMessageSpace');
toggleDiv('bomEditorSpace');


      <%
        sectionIter = sections.iterator();
        while(sectionIter.hasNext()){
            section = (String) sectionIter.next();
        %>
            <% if("1".equals(selectedView)){ %>
                setViewDisplay('1', '<%= section %>TabEditorTable', <%=positionToNameMap.keySet().size() + hardColumnCount%>);
            <% } else{%>
                setViewDisplay('<%= FormatHelper.encodeForJavascriptInSingleQuote(selectedView) %>', '<%= section %>TabEditorTable', <%=positionToNameMap.keySet().size() + hardColumnCount%>);
            <%}%>
            hideDimensionColumns('<%= section %>');
        <%    }  %>


<% if(palette != null){%>
//Load paletteColor info
<%
    Collection colors = new LCSPaletteQuery().findColorsForPalette(palette, null);
    Iterator paletteIter = colors.iterator();
    FlexObject obj;
    String id;
    while(paletteIter.hasNext()){
        obj = (FlexObject) paletteIter.next();
        id = "" + obj.getData("LCSCOLOR.IDA2A2");
        %>
        colorPaletteIds['<%= "NAME_" + id %>'] = '<%= FormatHelper.encodeForJavascript(obj.getString("LCSCOLOR.COLORNAME")) %>';
        colorPaletteIds['<%= "RGB_" + id %>'] = '<%= obj.getString("LCSCOLOR.ColorHexidecimalValue") %>';
        <%
    }
    %>

<% } %>

<%if (WCPART_ENABLED ){%>
    PTC.getMainWindow().PTC.performance= function() {
    };

    PTC.getMainWindow().PTC.performance.restartTimer= function() {
    };

    PTC.getMainWindow().PTC.performance.startComponentTimer= function() {
    };
<%}%>

    <%
    boolean previousEnforcement = SessionServerHelper.manager.setAccessEnforced(false); 
    try{
     if(includeSetAsPrimary){
        FlexType bomType = bomPart.getFlexType();
        String primaryMaterialWidgetId =bomType.getAttribute("pmDescription").getAttributeName();
        String primaryMaterialNumericWidgetId= bomType.getAttribute("primaryMaterial").getAttributeName();
        RevisionControlled primaryMaterial = (RevisionControlled)bomPart.getValue("primaryMaterial");
        String primaryMaterialSupplierObjectId = (primaryMaterial == null)? "": FormatHelper.getNumericVersionIdFromObject(primaryMaterial);

    %>
     RFABOM.configurePrimaryMaterialWidgets('<%=primaryMaterialWidgetId%>', '<%=primaryMaterialNumericWidgetId%>', '<%= primaryMaterialSupplierObjectId%>');
   <%}
    }catch(WTException e){
        logger.error(e.getMessage(),e);
    }finally {
        SessionServerHelper.manager.setAccessEnforced(previousEnforcement);
    }%>

    function completeShowSelector(xml, text) {
        var element = currentSelectWidget;
        selectorDiv = document.getElementById('selectorDiv');
        selectorDiv.innerHTML = '';
        selectorDiv.innerHTML = text;
        activeSelectorRow = false;
        var fromTop;
        var fromLeft;
        var posOffset = 0;
        if (element != null) {
            var ft = findVertPosOfObject(element);
            if (hasContent(ft)) {
                fromTop = ft - posOffset + element.offsetHeight;
            }
            var fl = findHorzPosOfObject(element);
            if (hasContent(fl)) {
                fromLeft = fl - posOffset;
            }
        }
        selectorDiv.style.display = 'block';
        selectorDiv.style.zIndex = 10000;
        selectorDiv.style.position = 'absolute';
        selectorDiv.style.left = fromLeft;
        selectorDiv.style.top = fromTop;
        if (is.ie) {
            hideElem(selectorDiv);
        }
    }

    function findHorzPosOfObject(obj) {
        var curleft = 0;
        if (obj) {
            var rect = obj.getBoundingClientRect();
            curleft = rect.left;
        }
        return curleft;
    }

</script>
<jsp:include page="<%=subURLFolder+ STANDARD_TEMPLATE_FOOTER %>" flush="true" >
<jsp:param name="type" value="<%= FormatHelper.getObjectId(bomPart.getFlexType()) %>"/>
<jsp:param name="none" value="" />
</jsp:include>
<%// } catch(Throwable t){ t.printStackTrace(); } %>

<% if ( WCPART_ENABLED ){%>
    <wctags:itemPicker id="partPicker"
       objectType="wt.part.WTPart"
       typeComponentId="Foundation.partpicker"
       showAllObjectTypes="false"
       componentId='partpicker'
       typePickerFormat="dropdown"
       multiSelect="false"
       pickerCallback="partPickerCallback"
       excludeSubTypes="com.lcs.wc.part.LCSPart"
       pickedAttributes="name,number,versionInfo.identifier.versionId,iterationInfo.branchId,iterationInfo.identifier.iterationId,view"
       displayAttribute="name,number,versionInfo.identifier.versionId,iterationInfo.branchId,iterationInfo.identifier.iterationId,view"/>

<%}%>

<script>
addScrollListnerOnTable(".table-wrapper",true);
addScrollListnerOnContentDiv("pageContents");

function fixWidthForLastWrapColumn(){
	jQuery('div[id*=TabEditorTableDiv]').each(function(index, tableDiv) {
		var visibleHeaders = jQuery('#columns:first-child td:not([style*="display: none;"])', tableDiv);
		var lastVisibleHeader = visibleHeaders.length > 0 ? visibleHeaders[visibleHeaders.length - 1] : undefined;  
		
		if(lastVisibleHeader){
			console.log(lastVisibleHeader);
			if(lastVisibleHeader.style.removeAttribute){
				lastVisibleHeader.style.removeAttribute('max-width');
			} else{
				lastVisibleHeader.style.removeProperty('max-width');
			}
		}
	});
}

(function($) {
	$(document).ready(function() {
		fixWidthForLastWrapColumn();
	});
})(jQuery);

</script>

<style type="text/css">
#ajaxSearchResultPopup > div > div {
  overflow-x: unset;
}
</style>
