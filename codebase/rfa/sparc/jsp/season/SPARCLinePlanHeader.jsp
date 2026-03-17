<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// PAGE DOCUMENTATION//////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%--
    JSP Type: View

--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.db.*,
            com.lcs.wc.client.web.*,
            com.lcs.wc.client.*,
            com.lcs.wc.util.*,
            com.lcs.wc.product.*,
            com.lcs.wc.season.*,
            com.lcs.wc.flextype.*,
            com.lcs.wc.report.*,
            com.lcs.wc.sourcing.*,
            wt.util.*,
            wt.org.*,
            org.apache.logging.log4j.Logger,
            org.apache.logging.log4j.LogManager,
            com.lcs.wc.foundation.*,
            java.util.*"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="seasonModel" scope="request" class="com.lcs.wc.season.LCSSeasonClientModel" />
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="lineSheetGridGenerator" scope="session" class="com.lcs.wc.client.web.ExtJSGridGenerator" />
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="ftg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="columns" scope="request" class="java.util.Vector" />
<jsp:useBean id="columnMap" scope="request" class="java.util.Hashtable" />
<jsp:useBean id="linePlanData" scope="request" class="java.util.Vector" />
<jsp:useBean id="linePlanConfig" scope="session" class="com.lcs.wc.season.LinePlanConfig" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="linePlanDataCache" class="com.lcs.wc.season.LCSSeasonReportCache" scope="session"/>
<jsp:useBean id="placeholderMode" scope="session" class="java.lang.StringBuffer" />
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>


<%!
    private static final Logger logger = LogManager.getLogger("rfa.jsp.season.LinePlanHeader");
    public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");
    public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
    public static final String WT_IMAGE_LOCATION = LCSProperties.get("flexPLM.windchill.ImageLocation");
    public static String EXCEL_GENERATOR_SUPPORT_PLUGIN = PageManager.getPageURL("EXCEL_GENERATOR_SUPPORT_PLUGIN", null);
    public static final String PDF_GENERATOR = PageManager.getPageURL("PDF_GENERATOR", null);
    public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");

    public static final String SEASON_HEADER = PageManager.getPageURL("SEASON_HEADER", null);
    public static final boolean USE_DELIVERIES = LCSProperties.getBoolean("com.lcs.wc.season.useDeliveries");
    public static final boolean USE_PDF_PRINT_SPEC = LCSProperties.getBoolean("jsp.specification.PrintSpec.PDF");
    public static final String CREATE_UPDATE_ACL_FOR_CARRYOVER;
    public static final Collection refreshCostsGroups;
    public static final String DISCUSSION_FORM_POSTINGS = PageManager.getPageURL("DISCUSSION_FORM_POSTINGS", null);
    public static final boolean FORUMS_ENABLED= LCSProperties.getBoolean("jsp.discussionforum.discussionforum.enabled");
    public static final String DISCUSSION_FORM = PageManager.getPageURL("DISCUSSION_FORM", null);
    public static final String SPEC_CHOOSER_HIDDEN_INPUTS = PageManager.getPageURL("SPEC_CHOOSER_HIDDEN_INPUTS", null);
    public static final String PRODUCT_ROOT_TYPE = LCSProperties.get("com.lcs.wc.sample.LCSSample.Product.Root");
    public static final boolean USE_MULTI_COSTING = LCSProperties.getBoolean("com.lcs.wc.sourcing.useLCSMultiCosting");
    public static final boolean USE_SAMPLES = LCSProperties.getBoolean("com.lcs.wc.sample.useSamples");
    public static final boolean USE_ORDERCONFIRMATION = LCSProperties.getBoolean("com.lcs.wc.product.useOrderConfirmation");
    public static final boolean ENABLE_RFQ = LCSProperties.getBoolean("com.lcs.wc.sourcing.useRFQ");
    
    public static final boolean SUBSCRIPTION_ENABLED= LCSProperties.getBoolean("jsp.subscriptions.enabled");
    public static final String SUBSCRIPTION_FORM = PageManager.getPageURL("SUBSCRIPTION_FORM", null);


    public static boolean newBOM = LCSProperties.getBoolean("com.lcs.wc.product.PDFProductSpecificationGenerator.newPrintBOM");
    public static final boolean USE_PLACEHOLDERS = LCSProperties.getBoolean("com.lcs.wc.placeholder.usePlaceholders");

    public static final boolean SHOW_CHANGEDPRODINFO_DEFAULT = LCSProperties.getBoolean("jsp.season.LinePlan.showChangedProdInfo");

    public static final String TWX_DASHBOARD_OPTION = PageManager.getPageURL("TWX_DASHBOARD_OPTION", null);

    public static final boolean ENABLE_COLORWAY_MATRIX  = LCSProperties.getBoolean("jsp.season.LinePlan.enableColorwayManager");
    public static final String CONTEXT_PATH = LCSProperties.get("flexPLM.urlContext.override");
    boolean highlightOptions=false;
	
	// changes for costing uploader - start
	public static final String csDetailedBreakdownDownloadLabel = LCSProperties.get("jsp.sparc.season.SPARCLinePlanHeader.breakdownDownloadAction","Costing - Detailed Breakdown Download");
    public static final String csHighLevelDownloadLabel = LCSProperties.get("jsp.sparc.season.SPARCLinePlanHeader.highLevelDownloadAction","Costing – High Level Download");
	public static final String productDivisionKey= LCSProperties.get("jsp.sparc.season.SPARCLinePlanHeader.productDivisionKey","scProductDiv");
	public static final String csDetailedBreakdownUploadLabel = LCSProperties.get("jsp.sparc.season.SPARCLinePlanHeader.breakdownUploadAction","Costing - Detailed Breakdown Upload");
    public static final String csHighLevelUploadLabel = LCSProperties.get("jsp.sparc.season.SPARCLinePlanHeader.highLevelUploadAction","Costing – High Level Upload");
	public static Collection<String> applicableViews = FormatHelper.commaSeparatedListToCollection(LCSProperties.get("jsp.sparc.season.SPARCLinePlanHeader.applicableViews","Costing - Upload/Download View,Product Costing View"));
	
	public static String PROD_COST_VIEW = "Product Costing View";
	
	public static final String maxSelectionLimit = LCSProperties.get("jsp.sparc.season.SPARCLinePlanHeader.maxSelectionLimit","500");
	public static Collection<String> productToCostsheetTypeMapping = FormatHelper.commaSeparatedListToCollection(LCSProperties.get("jsp.sparc.season.SPARCLinePlanHeader.ProductToCostsheetTypeMapping","Product\\scApparel\\scAeropostale~Cost Sheet\\scSparc\\scApparelHighLevel\\scApparelHighLevelAeropostale:Cost Sheet\\scSparc\\scApparelBreakdown\\scApparelBreakdownAeropostale,Product\\scApparel\\scForever21~Cost Sheet\\scSparc\\scApparelHighLevel\\scApparelHighLevelForever21:Cost Sheet\\scSparc\\scApparelBreakdown\\scApparelBreakdownForever21,Product\\scApparel\\scLucky~Cost Sheet\\scSparc\\scApparelHighLevel\\scApparelHighLevelLucky:Cost Sheet\\scSparc\\scApparelBreakdown\\scApparelBreakdownLucky,Product\\scApparel\\scReebok~Cost Sheet\\scSparc\\scApparelHighLevel\\scApparelHighLevelReebok:Cost Sheet\\scSparc\\scApparelBreakdown\\scApparelBreakdownReebok,Product\\scFootwear\\scFootwearReebok~Cost Sheet\\scSparc\\scFootwearBreakdown\\scFootwearBreakdownReebok:Cost Sheet\\scSparc\\scApparelHighLevel\\scApparelHighLevelReebok,Product\\scApparel\\scNautica~Cost Sheet\\scSparc\\scApparelHighLevel\\scApparelHighLevelNautica:Cost Sheet\\scSparc\\scApparelBreakdown\\scApparelBreakdownNautica"));
	public static Collection<String> DivisionMapping = FormatHelper.commaSeparatedListToCollection(LCSProperties.get("jsp.sparc.season.SPARCLinePlanHeader.DivisionMapping","scNonApparel~NONAPPAREL,sclicensed~LICENSEDAPP,sclicensedftw~LICENSEDFTW,schardware~HARDWARE,apparel~APPAREL,footwear~FOOTWEAR"));
	// changes for costing uploader - end

    static {
        String refreshGroups = LCSProperties.get("com.lcs.wc.sourcing.refreshCostSheetGroups");
        StringTokenizer tokenizer = new StringTokenizer(refreshGroups, ",");
        refreshCostsGroups = new Vector();
        while(tokenizer.hasMoreTokens()){
            refreshCostsGroups.add(tokenizer.nextToken().toUpperCase());
        }

        if ( FormatHelper.hasContent( LCSProperties.get("com.jsp.season.LinePlanHeader.carryOverMoveDependentOn")) ) {
            CREATE_UPDATE_ACL_FOR_CARRYOVER = LCSProperties.get("com.jsp.season.LinePlanHeader.carryOverMoveDependentOn");
        }
        else {
            CREATE_UPDATE_ACL_FOR_CARRYOVER = "create";
        }

    }

%>
<%
String resultsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "results_LBL", RB.objA ) ;
String hideImagesLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "hideImages_LBL",RB.objA ) ;
String showImagesLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "showImages_LBL",RB.objA ) ;

String createNewOpt = WTMessage.getLocalizedMessage ( RB.SEASON, "createNew_OPT", RB.objA ) ;
String linePlanEditorlabel = WTMessage.getLocalizedMessage ( RB.SEASON, "linePlanEditor_LBL", RB.objA ) ;
String linePlanWorkListLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "linePlanWorkList_LBL", RB.objA ) ;
String linePlanViewerLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "linePlanViewer_LBL", RB.objA ) ;
String copyProductFromSeason = WTMessage.getLocalizedMessage ( RB.SEASON, "copyProductFromSeason_OPT", RB.objA ) ;
String carryOverOpt = WTMessage.getLocalizedMessage ( RB.SEASON, "carryOver_OPT", RB.objA ) ;
String moveOpt = WTMessage.getLocalizedMessage ( RB.SEASON, "move_OPT", RB.objA ) ;
String removeOpt = WTMessage.getLocalizedMessage ( RB.SEASON, "remove_OPT", RB.objA ) ;
String editLinePlanOpt = WTMessage.getLocalizedMessage ( RB.SEASON, "editLinePlan_OPT", RB.objA ) ;
String recalculateCostsOpt = WTMessage.getLocalizedMessage ( RB.SEASON, "recalculateCosts_OPT", RB.objA ) ;
String changeReportOpt = WTMessage.getLocalizedMessage ( RB.SEASON, "changeReport_OPT", RB.objA ) ;
String viewLineBoardOpt = WTMessage.getLocalizedMessage ( RB.SEASON, "lineBoard_OPT", RB.objA ) ;
String createLineBoardOpt = WTMessage.getLocalizedMessage ( RB.SEASON, "createLineBoard_OPT", RB.objA ) ;
String filtersButton = WTMessage.getLocalizedMessage ( RB.SEASON, "filters_Btn", RB.objA ) ;
String optionsButton = WTMessage.getLocalizedMessage ( RB.SEASON, "options_Btn", RB.objA ) ;
String saveChangesButton = WTMessage.getLocalizedMessage ( RB.SEASON, "saveChanges_Btn", RB.objA ) ;
String saveChangesButtonTooltip = WTMessage.getLocalizedMessage(RB.SEASON, "LINESHEET_SAVE_BUTTON_TOOLTIP");
String groupByLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "groupBy_LBL", RB.objA ) ;
String cancelButton = WTMessage.getLocalizedMessage ( RB.MAIN, "cancel_Btn", RB.objA ) ;
String cancelButtonTooltip = WTMessage.getLocalizedMessage(RB.SEASON, "LINESHEET_CANCEL_BUTTON_TOOLTIP");
String runButton = WTMessage.getLocalizedMessage ( RB.SEASON, "run_Btn", RB.objA ) ;
String lineViewerOptionsPgHead = WTMessage.getLocalizedMessage ( RB.SEASON, "lineViewerOptions_PG_HEAD", RB.objA ) ;
String showThumbnailsLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "showThumbnails_LBL", RB.objA ) ;
String allSourcesLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "allSources_LBL", RB.objA ) ;
String includePlaceHoldersLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "includePlaceHolders_LBL", RB.objA ) ;
String includeActiveCostSheetsLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "includeActiveCostSheets_LBL", RB.objA ) ;
String includeWhatIfCostSheetsLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "includeWhatIfCostSheets_LBL", RB.objA ) ;
String showChangedProdInfoLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "showChangedProdInfo_LBL", RB.objA ) ;

String actionsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "actions_LBL", RB.objA ) ;
String carryOverProducts = WTMessage.getLocalizedMessage ( RB.SEASON, "carryOverProducts_PG_TLE",RB.objA ) ;
String carryOverSKUs = WTMessage.getLocalizedMessage ( RB.SEASON, "carryOverSKUs_PG_TLE",RB.objA ) ;
String moveProducts = WTMessage.getLocalizedMessage ( RB.SEASON, "moveProducts_PG_TLE",RB.objA ) ;
String moveSKUs = WTMessage.getLocalizedMessage ( RB.SEASON, "moveSKUs_PG_TLE",RB.objA ) ;
String selectOptionFromDropDownList = WTMessage.getLocalizedMessage ( RB.MAIN, "selectOptionFromDropDownList_ALRT",RB.objA ) ;
String addProdTo = WTMessage.getLocalizedMessage ( RB.SEASON, "addProdTo_LBL",RB.objA ) ;
String addSKUsToSeason = WTMessage.getLocalizedMessage ( RB.SEASON, "addSKUsToSeason_LBL",RB.objA ) ;
String generateProductSpecifications = WTMessage.getLocalizedMessage ( RB.SEASON, "generateProductSpecifications_LBL",RB.objA ) ;
String recalculateAllCostsVisableMayTakeSomeTime = WTMessage.getLocalizedMessage ( RB.SEASON, "recalculateAllCostsVisableMayTakeSomeTime_CNFRM",RB.objA ) ;
String documentLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "document_LBL",RB.objA ) ;
String linePlan = WTMessage.getLocalizedMessage ( RB.SEASON, "linePlan_LBL",RB.objA ) ;
String addExisting = WTMessage.getLocalizedMessage ( RB.SEASON, "addExisting_LBL",RB.objA ) ;
String fromLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "from_LBL",RB.objA ) ;
String lineSheetCachedUseRunButtonToReloadDataMouseOver = WTMessage.getLocalizedMessage ( RB.SEASON, "lineSheetCachedUseRunButtonToReloadDataMouseOver_TOOLTIP",RB.objA ) ;
String levelLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "level_LBL",RB.objA ) ;
String linePlanActionsDisabledInThisMode = WTMessage.getLocalizedMessage ( RB.SEASON, "linePlanActionsDisabledInThisMode_PG_TLE",RB.objA ) ;
String viewSelectionDisabledInThisMode = WTMessage.getLocalizedMessage ( RB.SEASON, "viewSelectionDisabledInThisMode_PG_TLE",RB.objA ) ;
String sourcingSelectionDisabledInThisMode = WTMessage.getLocalizedMessage ( RB.SEASON, "sourcingSelectionDisabledInThisMode_PG_TLE",RB.objA ) ;
String levelSelectionDisabledInThisMode = WTMessage.getLocalizedMessage ( RB.SEASON, "levelSelectionDisabledInThisMode_PG_TLE",RB.objA ) ;
String cachedAsOfLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "cachedAsOf_LBL",RB.objA ) ;
String youMustSelectAFilterToUpdate = WTMessage.getLocalizedMessage ( RB.MAIN, "youMustSelectAFilterToUpdate_ALRT",RB.objA ) ;

String seasonLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "season_LBL", RB.objA ) ;
String placeholderLabel = WTMessage.getLocalizedMessage(RB.PLACEHOLDER, "placeholder_LBL", RB.objA);
String productLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "product_LBL", RB.objA ) ;
String productsLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "products_LBL", RB.objA ) ;
String skuLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "sku_LBL", RB.objA ) ;
String skusLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "skus_LBL", RB.objA ) ;
String addToFavoritesLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "addToFavorites", RB.objA ) ;
String addLineSheetToFavoritesLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "addLineSheetToFavorites", RB.objA ) ;
String sourceLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "source_LBL", RB.objA ) ;
String sourceToSeasonLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "sourceToSeason_LBL", RB.objA ) ;
String costSheetLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "costSheet_LBL", RB.objA ) ;
String costingLabel = WTMessage.getLocalizedMessage ( RB.SOURCING, "costing_LBL", RB.objA ) ;

String massCopyProducts = WTMessage.getLocalizedMessage ( RB.SEASON, "massCopyProduct_PG_TLE",RB.objA ) ;

String sourcingConfigsLabel = WTMessage.getLocalizedMessage ( RB.SOURCING, "sourcingConfigs_LBL", RB.objA);
String specificationsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "specifications_LBL", RB.objA);
String samplesLabel = WTMessage.getLocalizedMessage ( RB.SAMPLES, "samples_LBL", RB.objA);
String pleaseSelectSpecificationMSG = WTMessage.getLocalizedMessage ( RB.SAMPLES, "pleaseSelectSpecification_MSG", RB.objA );
String pleaseSelectMeasurementSetMSG = WTMessage.getLocalizedMessage ( RB.SAMPLES, "pleaseSelectMeasurementSet_MSG", RB.objA );
String loadingMSG = WTMessage.getLocalizedMessage ( RB.MAIN, "loading_MSG", RB.objA );
String noneAvailableLabel = WTMessage.getLocalizedMessage ( RB.MEASUREMENTS, "noneAvailable_LBL", RB.objA );
String newLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "new_LBL",RB.objA ) ;
String notApplicableLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "notApplicable_LBL",RB.objA ) ;
String listLabel = LCSMessage.getJavascriptMessage(RB.MAIN, "list_LBL", RB.objA);
String filmstripLabel = LCSMessage.getJavascriptMessage(RB.MAIN, "filmStrip_LBL", RB.objA);
String thumbnailLabel = LCSMessage.getJavascriptMessage(RB.MAIN, "thumbnail_LBL", RB.objA);
String freezepaneLabel = LCSMessage.getJavascriptMessage(RB.MAIN, "freezepane_LBL", RB.objA);
String samplesConsoleLabel = LCSMessage.getJavascriptMessage(RB.MAIN, "samplesConsole_LBL", RB.objA);
String orderConfirmationLabel = WTMessage.getLocalizedMessage ( RB.ORDERCONFIRMATION, "orderConfirmation_LBL", RB.objA ) ;


String maxSelectedProducts = LCSProperties.get("jsp.season.colorwaymanager.maxproducts");
String colorwaySelectionMessage = WTMessage.getLocalizedMessage ( RB.MAIN, "colorwaySelectionMessage", RB.objA ) ;

String rfqLabel = WTMessage.getLocalizedMessage ( RB.RFQ, "rfq_LBL", RB.objA ) ;
String zipTooltip = WTMessage.getLocalizedMessage ( RB.SEASON, "exportLinePlanToZip_LBL", RB.objA ) ;


////////////////////////////////////////////////////////////
//Initialize the access setting
////////////////////////////////////////////////////////////

boolean hasSPLCreateAccess = false, hasSPLUpdateAccess = false;
boolean hasSKUCreateAccess = false, hasSKULinkUpdateAccess = false;
hasSPLCreateAccess     = ACLHelper.hasCreateAccess(FlexTypeCache.getFlexTypeFromPath("Product"));
hasSKUCreateAccess     = ACLHelper.hasCreateAccess(FlexTypeCache.getFlexTypeFromPath("Product"));
hasSPLUpdateAccess     = ACLHelper.hasEditAccess(FlexTypeCache.getFlexTypeFromPath("Product"));
hasSKULinkUpdateAccess = ACLHelper.hasEditAccess(FlexTypeCache.getFlexTypeFromPath("Product"));
/////////////////////////////////////////////////////////////

    FlexType productType = seasonModel.getProductType();
    String pTypeName = LinePlanConfig.getLinePlanConfigKey(request, productType);
    boolean linePlanChooser = FormatHelper.parseBoolean(request.getParameter("linePlanChooser"));
    String tabPage = request.getParameter("tabPage");
    String action = request.getParameter("action");
    String oid = request.getParameter("oid");

    LCSSeason season = seasonModel.getBusinessObject();

    String workItemName = request.getParameter("workItemName");
    boolean workMode = FormatHelper.hasContent(workItemName);

    boolean highlightFilter = "true".equals(request.getParameter("highlightFilter"));
    boolean usePDF = LCSProperties.getBoolean("com.lcs.wc.client.web.PDFGenerator.usePDF");
    boolean enableFreezePane = LCSProperties.getBoolean("jsp.season.linesheet.enableFreezePane");
    boolean enableClassicMode = LCSProperties.getBoolean("jsp.season.linesheet.enableClassicMode");
    boolean usedCache = FormatHelper.parseBoolean(request.getParameter("usedCache"));
    boolean sourcing = FormatHelper.parseBoolean(request.getParameter("sourcing"));
    boolean runLinePlan = true;//"true".equals(request.getParameter("runLinePlan"));
    boolean run="true".equals(request.getParameter("runLinePlan"));
    boolean editLinePlan = "true".equals(request.getParameter("editLinePlan"));
    String linePlanLevel =  (String) linePlanConfig.get(pTypeName,"linePlanLevel");

    ColumnList selectedView =null;
    String groupByAttributeHolder= " ";
    if(FormatHelper.hasContent((String )linePlanConfig.get(pTypeName, "viewId"))){
        selectedView=(ColumnList)LCSQuery.findObjectById((String)linePlanConfig.get(pTypeName,"viewId"));
    }
	// changes for costing uploader - start
	String viewDisplayName = "";
	HashMap<String, ArrayList<String>> productCostSheetTypeMap = new HashMap<String, ArrayList<String>>();
	HashMap<String, String> divisionMap = new HashMap<String, String>();
	if(selectedView!=null)
		viewDisplayName = selectedView.getDisplayName();
	logger.debug("**viewDisplayName:"+viewDisplayName);
	ArrayList<String> list = null;
	for (String temp: productToCostsheetTypeMapping){
		String[] tempArray = temp.split("~");
		String[] types;
		if(tempArray[1].contains(":")){
			types = tempArray[1].split(":");
			
			list = new ArrayList<String>();
			for (String temp1: types){
				list.add(temp1);
				}
			}
			else{
			list = new ArrayList<String>();
			list.add(tempArray[1]);
			}
		productCostSheetTypeMap.put(tempArray[0],list);
	}
	for (String temp: DivisionMapping){
		String[] tempArray = temp.split("~");
		divisionMap.put(tempArray[0],tempArray[1]);
		
	}
	logger.debug("**productCostSheetTypeMap:"+productCostSheetTypeMap);
	logger.debug("**divisionMap:"+divisionMap);
	// changes for costing uploader - end

    if(FormatHelper.hasContent((String )linePlanConfig.get(pTypeName, "groupByAttribute"))){
        groupByAttributeHolder=(String)linePlanConfig.get(pTypeName,"groupByAttribute");
    }

    // If we don't have the values for setting the lineplan level, let's look in the
    // linePlanLevelHold parameter.
     if (!FormatHelper.hasContent(linePlanLevel))
     {
        if (request.getParameter("linePlanLevelHold") != null)
        {
            linePlanLevel = request.getParameter("linePlanLevelHold");
        }
       else
       {
            linePlanLevel = FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL;
        }

     }


    String linePlanView = (String)linePlanConfig.get(pTypeName,"linePlanView");
    if(!FormatHelper.hasContent(linePlanView)){
        linePlanView = "marketing";
        linePlanConfig.put(pTypeName, "linePlanView", "marketing");
    }

    boolean skus = (FootwearApparelFlexTypeScopeDefinition.PRODUCT_SKU_LEVEL.equals(linePlanLevel));
    boolean placeholdersOnly = (LineSheetQuery.PLACEHOLDER_MODE_PLACEHOLDER.equals(placeholderMode.toString()));
    boolean linePlanSourcing = "true".equals((String)linePlanConfig.get(pTypeName, "linePlanSourcing"));
    String linePlanChooserTitle = request.getParameter("linePlanChooserTitle");
    String linePlanChooserType = request.getParameter("linePlanChooserType");
    boolean skuChooser = "SKU".equals(linePlanChooserType);
    boolean sourcingChooser = "SOURCING".equals(linePlanChooserType);

    boolean placeholders = "true".equals((String)linePlanConfig.get(pTypeName, "placeholders"));

    boolean activeCostSheets = "true".equals((String)linePlanConfig.get(pTypeName, "activeCostSheets"));
    activeCostSheets = (activeCostSheets && !linePlanChooser);

    boolean whatIfCostSheets = "true".equals((String)linePlanConfig.get(pTypeName, "whatIfCostSheets"));
    whatIfCostSheets = (whatIfCostSheets && !linePlanChooser);

    boolean showChangedProdInfo=false;
    if(!FormatHelper.hasContent((String)linePlanConfig.get(pTypeName, "showChangedProdInfo"))){
        showChangedProdInfo = SHOW_CHANGEDPRODINFO_DEFAULT;
    }else{
        showChangedProdInfo = "true".equals((String)linePlanConfig.get(pTypeName, "showChangedProdInfo"));
        showChangedProdInfo = (showChangedProdInfo && !linePlanChooser);
    }



    if(skuChooser){
        linePlanLevel = FootwearApparelFlexTypeScopeDefinition.PRODUCT_SKU_LEVEL;
    } else if(linePlanChooser){
        linePlanLevel = FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL;
    }
    String infoMessage = request.getParameter("infoMessage");
    linePlanSourcing = (linePlanSourcing && !linePlanChooser);

    if(sourcingChooser){
        linePlanSourcing = true;
    }

    FlexType sampleType = FlexTypeCache.getFlexTypeFromPath(PRODUCT_ROOT_TYPE);

    String seasonId = FormatHelper.getVersionId(seasonModel.getBusinessObject());
    String addParam = "&pdfExportingFrom=lineplan&action=ADDITIONAL&seasonId=" + seasonId;


    //Set up the types for views
    String typesString = "";
    typesString = MOAHelper.addValue(typesString, "Product|" + FormatHelper.getObjectId(season.getProductType()));
    typesString = MOAHelper.addValue(typesString, "Cost Sheet|" + FormatHelper.getObjectId(season.getProductType().getReferencedFlexType(ReferencedTypeKeys.COST_SHEET_TYPE)));
    typesString = MOAHelper.addValue(typesString, "Sourcing Configuration|" + FormatHelper.getObjectId(season.getProductType().getReferencedFlexType(ReferencedTypeKeys.SOURCING_CONFIG_TYPE)));
    typesString = MOAHelper.addValue(typesString, "Material|" + FormatHelper.getObjectId(FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.material.LCSMaterial")));



    FlexType flexType = season.getProductType();

    boolean showThumbs = "true".equals((String)linePlanConfig.get(pTypeName, "showThumbs"));
    String layout = (String) linePlanConfig.get(pTypeName,"layout");
    if (layout == null) {
        String layoutProperty = LCSProperties.get("jsp.season.linesheet.defaultMode");
        if ("classic".equals(layoutProperty)) {
            linePlanConfig.put(pTypeName, "layout", "list");
        } else if ("freezePane".equals(layoutProperty)) {
            linePlanConfig.put(pTypeName, "layout", "freezepane");
        }
        layout = (String) linePlanConfig.get(pTypeName,"layout");
    }

    // ESTABLISH THE TILE CELL RENDERED BEFORE THE PDF INCLUSION.
    DefaultTileCellRenderer tileCellRenderer = new DefaultTileCellRenderer();
    tileCellRenderer.setActionsMenuType("LINESHEET");
    if (skus){
        tileCellRenderer.setNameIndex(productType.getAttribute("skuName").getSearchResultIndex());
        tileCellRenderer.setImageIndex("LCSSKU.PartPrimaryImageURL");
        //SPR11157638
        tileCellRenderer.setImageIndex2("LCSPRODUCT.PartPrimaryImageURL");
        tileCellRenderer.setIdIndex("LCSSKUSEASONLINK.SKUSEASONREVID");
        tileCellRenderer.setIdPrefix("VR:com.lcs.wc.product.LCSSKU:");
    } else {
    tileCellRenderer.setNameIndex(productType.getAttribute("productName").getSearchResultIndex());
        tileCellRenderer.setImageIndex("LCSPRODUCT.PartPrimaryImageURL");
        tileCellRenderer.setIdIndex("LCSPRODUCTSEASONLINK.PRODUCTSEASONREVID");
    tileCellRenderer.setIdPrefix("VR:com.lcs.wc.product.LCSProduct:");
    }
    request.setAttribute("tileCellRenderer", tileCellRenderer);

    FlexType orderConfirmationType = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.sourcing.OrderConfirmation");
    FlexType rfqRootType = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.sourcing.RFQRequest");

    Date date = new Date();
    String reportDate = FormatHelper.formatWithTime(date);
    String userName=lcsContext.getUserName();
    String reportTitle = FormatHelper.format(linePlan+" - " + season.getValue("seasonName"));
    reportTitle = reportTitle.replace("  ", " ");

    FlexType seasonGroupType = FlexTypeCache.getFlexTypeFromPath("Season Group");
    FlexTypeAttribute seasonGroupNameAtt = seasonGroupType.getAttribute("name");
    if((linePlanChooser && request.getParameter("groupByAttribute") ==null)
         || (groupByAttributeHolder==null || !FormatHelper.hasContent(groupByAttributeHolder))){
        groupByAttributeHolder=" ";
    }

    if(!linePlanChooser){
        highlightOptions=LineSheetUtils.optionsDiffer(selectedView,linePlanConfig,pTypeName,groupByAttributeHolder);
        if(!highlightOptions && !placeholderMode.toString().equals(LineSheetQuery.PLACEHOLDER_MODE_PRODUCT)){
            highlightOptions=true;
        }
    }else{
        highlightOptions=false;
    }
%>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/ajax.js"></script>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/season/lineplan.js"></script>

<//%@ include file="../../jsp/reports/ViewLookup.jspf" %>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////   JAVASCRIPT //////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>


<script>
document.LINEPLAN_HEADER = new Object();
document.LINEPLAN_HEADER.seasonId ='<%= FormatHelper.getVersionId(season) %>';
document.LINEPLAN_HEADER.carryOverProducts = '<%= java.net.URLEncoder.encode(carryOverProducts, defaultCharsetEncoding) %>';
document.LINEPLAN_HEADER.carryOverSKUs = '<%= java.net.URLEncoder.encode(carryOverSKUs, defaultCharsetEncoding) %>';
document.LINEPLAN_HEADER.moveProducts = '<%= java.net.URLEncoder.encode(moveProducts, defaultCharsetEncoding) %>';
document.LINEPLAN_HEADER.copyProductFromSeason = '<%= java.net.URLEncoder.encode(copyProductFromSeason, defaultCharsetEncoding) %>';
document.LINEPLAN_HEADER.moveSKUS = '<%= java.net.URLEncoder.encode(moveSKUs, defaultCharsetEncoding) %>';
document.LINEPLAN_HEADER.actionWillMoveProducts1_CNFRM ='<%Object[] objB =  {season.getName()};out.print(FormatHelper.encodeForJavascript(WTMessage.getLocalizedMessage(RB.SEASON, "actionWillMoveProducts1_CNFRM", objB))); %>';
document.LINEPLAN_HEADER.selectOptionFromDropDownList = '<%= FormatHelper.formatJavascriptString(selectOptionFromDropDownList ,false)%>';
document.LINEPLAN_HEADER.addProdTo = '<%= java.net.URLEncoder.encode(addProdTo, defaultCharsetEncoding) %>' + ' ';
document.LINEPLAN_HEADER.addSKUsToSeason = '<%= java.net.URLEncoder.encode(addSKUsToSeason, defaultCharsetEncoding) %>';
document.LINEPLAN_HEADER.recalculateAllCostsVisableMayTakeSomeTime ='<%= FormatHelper.formatJavascriptString(recalculateAllCostsVisableMayTakeSomeTime ,false)%>';
document.LINEPLAN_HEADER.seasonModelProductType = '<%= FormatHelper.getObjectId(seasonModel.getProductType()) %>';
document.LINEPLAN_HEADER.flexTypeObjectId = '<%= FormatHelper.getObjectId(flexType) %>';
document.LINEPLAN_HEADER.youMustSelectAFilterToUpdate = '<%= FormatHelper.formatJavascriptString(youMustSelectAFilterToUpdate ,false)%>';
document.LINEPLAN_HEADER.lineBoardFlexTypeObjectId = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Season Group\\Line Board")) %>';
document.LINEPLAN_HEADER.massCopyProducts = '<%= java.net.URLEncoder.encode(massCopyProducts, defaultCharsetEncoding) %>';
document.LINEPLAN_HEADER.addProductsToPlaceholder = '<%=java.net.URLEncoder.encode(WTMessage.getLocalizedMessage(RB.PLACEHOLDER, "addProductsToPlaceholder_TLE", RB.objA), defaultCharsetEncoding)%>';
document.LINEPLAN_HEADER.showChangedProdInfo = '<%= showChangedProdInfo%>';
document.MAINFORM.showThumbs.value='<%=showThumbs%>';
document.LINEPLAN_HEADER.groupByAttribute = '<%= ( FormatHelper.encodeForJavascript(groupByAttributeHolder)==null) ? " " : FormatHelper.encodeForJavascript(groupByAttributeHolder)%>';
if (document.MAINFORM.layout) {
    document.MAINFORM.layout.value = '<%= FormatHelper.encodeForJavascript(layout)%>';
}

    var divHash = new Array();

    var linePlanChooser;

    var maxSelectedProducts = <%= maxSelectedProducts %> ;
    var colorwaySelectionMessage = '<%= FormatHelper.formatJavascriptString(colorwaySelectionMessage ,false) %>' ;

</script>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ///////////////////////////////////////// HTML //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<input type='hidden' name="relevantActivity" value='VIEW_LINE_PLAN'>
<input type='hidden' name="typesString" value='<%= typesString %>'>
<input type="hidden" name="currentDiv"  value="">
<input type='hidden' name="layout" value='<%=FormatHelper.encodeAndFormatForHTMLContent( layout )%>'>
<input type="hidden" name="seasonGroupIds" value="">
<input type="hidden" name="selectedPages" value="">
<input type="hidden" name="copyFromSeason" value="">
<input type="hidden" name="viewCostSheets" value="">


<jsp:include page = "<%= subURLFolder+ SPEC_CHOOSER_HIDDEN_INPUTS%>" flush="true">
       <jsp:param name="none" value="" />
</jsp:include>

<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/linePlanMenu.js"></script>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/specPrinter.js"></script>

<%
String resultsLabelWithNumbers = resultsLabel + " ";
int resultsFound = linePlanData.size();
int fromIndex = 1;
int toIndex = resultsFound;
String queryTime = "" + request.getAttribute("lineSheetQueryTime");
if(!FormatHelper.hasContent(queryTime)){
    queryTime = "0.000";
}
if(resultsFound < 1){
    Object[] objC = {"0-" + Integer.toString(toIndex),  Integer.toString(resultsFound), queryTime};
    resultsLabelWithNumbers = WTMessage.getLocalizedMessage(RB.MAIN, "resultsOfInSeconds_LBL", objC);
} else {
    Object[] objC = {Integer.toString(fromIndex) +"-" + Integer.toString(toIndex),  Integer.toString(resultsFound), queryTime};
    resultsLabelWithNumbers = WTMessage.getLocalizedMessage(RB.MAIN, "resultsOfInSeconds_LBL", objC);
}

%>

<% if(FormatHelper.hasContent(linePlanChooserTitle)){ %>
	<%
	String[] arr = null;
	String seasonGroupTypeId = request.getParameter("seasonGroupTypeIdForChooserTitle");
	if(FormatHelper.hasContent(seasonGroupTypeId)) {
	    arr = new String[]{seasonGroupTypeId, season.getValue("seasonName").toString()};
	} else {
	    arr = new String[]{season.getValue("seasonName").toString()};
	}
	String addProductsToFrom_LBL = WTMessage.getLocalizedMessage (RB.SEASON, linePlanChooserTitle, arr);
	%>
    <tr>
        <td class="PAGEHEADING">
            <table>
                <tr>
                    <td nowrap class="PAGEHEADINGTITLE">
                        <%= FormatHelper.encodeAndFormatForHTMLContent(addProductsToFrom_LBL) %>
                    </td>
                    <%
                    //boolean useViews = true;
                    boolean chooser = false;
                    boolean useUpdate = false;
                    %>
                    <%@ include file="../../../jsp/season/LPViewButtons.jspf" %>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td class="SEARCH_RESULTS_BAR">
            <%= resultsLabelWithNumbers %></b>
            &nbsp;&nbsp;&nbsp;&nbsp;
            <span>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <a id="thumbsButton" href="javascript:toggleThumbs(<%= !showThumbs %>)"><%= showThumbs ? hideImagesLabel : showImagesLabel %></a>


            &nbsp;&nbsp;
<%     if(enableFreezePane) {%>
            <a onmouseover="return overlib('<%= freezepaneLabel%>', LEFT);" onmouseout="return nd();" href="javascript:setLayout('freezepane')"><img border="0" src="<%=URL_CONTEXT%>/images/table_freeze_panes.png"></a>
<%     }%>
            <a onmouseover="return overlib('<%= thumbnailLabel%>', LEFT);" onmouseout="return nd();" href="javascript:setLayout('thumbnails')"><img border="0" src="<%=WT_IMAGE_LOCATION%>/thumbnail.png"></a>
            <a onmouseover="return overlib('<%= filmstripLabel%>', LEFT);" onmouseout="return nd();" href="javascript:setLayout('filmstrip')"><img border="0" src="<%= WT_IMAGE_LOCATION %>/filmstrip.png"></a>
<%     if(enableClassicMode) {%>
            <a onmouseover="return overlib('<%= listLabel%>', LEFT);" onmouseout="return nd();" href="javascript:setLayout('list')"><img border="0" src="<%=WT_IMAGE_LOCATION%>/flexdetails.png"></a>
<%} %>
       </td>
    </tr>

<% } else if ("freezepane".equals(layout) && editLinePlan) { %>
    <%
        /////////////////////////////////////////////////////////////////////////////////////////
        //  Render SeasonGroup widgets next to the run button, one action menu/one picklist for each type
        //  of seasongroup configured for this season
        /////////////////////////////////////////////////////////////////////////////////////////
        Iterator seasonGroupsTypeIds = MOAHelper.getMOASet(season.getSeasonGroupIds()).iterator();
        while(seasonGroupsTypeIds.hasNext()){
            String seasonGroupTypeId = (String) seasonGroupsTypeIds.next();
            FlexType sgType = FlexTypeCache.getFlexType(seasonGroupTypeId);
            if (ACLHelper.hasViewAccess(sgType)){
                String formElementId = "SG_" + FormatHelper.getNumericObjectIdFromObject(sgType);
                String selectedSeasonGroupId = (String) linePlanConfig.get(pTypeName, formElementId);
                out.println(FormGenerator.createHiddenInput(formElementId, selectedSeasonGroupId));
            }
        }
    %>
    <tr>
        <table width='100%' style='border-top: solid 1px grey' border='0' cellspacing='5' cellpadding='0'>
        <tr>
            <td colspan=2 class='HEADING1'>
                <%=FormatHelper.encodeAndFormatForHTMLContent( FormatHelper.format("" + season.getValue("seasonName")) )%>
            </td>
            <td>
                <table border='0' cellspacing='0' cellpadding='0'>
                <%
                    //boolean useViews = true;
                    boolean chooser = false;
                    boolean useUpdate = false;
                %>
                <%@ include file="../../../jsp/season/LPViewButtons.jspf" %>
                </table>
            </td>
             <td></td>
             <td valign="top" width="40%">
                <%@ include file="../../../jsp/reports/PinnedViewsInclude.jspf" %>
            </td>
        </tr>
        <tr>
            <td>
                <div class="button-set">
                    <a class="button" id="saveLinePlanButton" href="javascript:saveInCellEditingLineSheet()" title="<%= saveChangesButtonTooltip %>"><%= saveChangesButton %></a>
                    <a class="button" href="javascript:cancelInCellEditingLineSheet()" title="<%= cancelButtonTooltip %>"><%= cancelButton %></a>
                </div>
            </td>
            <td style='text-align:right;padding-right:20px;'>
                <span id="exportingSpan">
                    <img style="vertical-align:top" src="<%=URL_CONTEXT%>/images/blue-loading.gif" height="16" width="16">
                    <%=FormatHelper.formatJavascriptString(loadingMSG, false)%>
                </span>
            </td>
            <td style='vertical-align:center;padding-left: 2px;'>
                <a id="thumbsButton" style='vertical-align: top;' href="javascript:toggleThumbs(<%= !showThumbs %>)"><%= showThumbs ? hideImagesLabel : showImagesLabel %></a>
                &nbsp;&nbsp;
                <%
                if (columns != null) request.setAttribute("columns", columns);
                if (linePlanData != null){
                    request.setAttribute("data", linePlanData);
                }
                String csv = request.getParameter("csv");
                String pdf = request.getParameter("pdf");
                %>
                <% if(runLinePlan && !linePlanChooser & !workMode){ %>
                    <jsp:include page="<%=subURLFolder+ EXCEL_GENERATOR_SUPPORT_PLUGIN %>" flush="true">
                        <jsp:param name="csv" value="<%= csv %>" />
                        <jsp:param name="reportDisplayName" value="<%= linePlan %>" />
                        <jsp:param name="reportName" value="<%= linePlan %>" />
                        <jsp:param name="layout" value="<%= layout %>" />
                    </jsp:include>
                    <% if(usePDF){ %>
                        <jsp:include page="<%=subURLFolder+ PDF_GENERATOR %>" flush="true">
                            <jsp:param name="pdf" value="<%= pdf %>"/>
                            <jsp:param name="reportName" value="<%= documentLabel %>"/>
                            <jsp:param name="cellClassDark" value="RPT_TBD"/>
                            <jsp:param name="cellClassLight" value="RPT_TBL"/>
                            <jsp:param name="tableHeaderClass" value="RPT_HEADER"/>
                            <jsp:param name="tableTotalsClass" value="RPT_TOTALS"/>
                            <jsp:param name="reportTitle" value="<%=reportTitle%>"/>
                            <jsp:param name="userName" value="<%=userName%>"/>
                            <jsp:param name="reportDate" value="<%=reportDate%>"/>
                            <jsp:param name="layout" value="<%= layout %>" />
                        </jsp:include>
                    <% }  %>
                <% } %>
            </td>
            <td>
            </td>
            <td align='right'>
                <%= resultsLabelWithNumbers %>
                &nbsp;&nbsp;
            </td>
        </tr>
        </table>
    </tr>
<% } else { %>
    <tr>
        <td onwrap>
            <jsp:include page="<%=subURLFolder+ SEASON_HEADER %>" flush="true" >
                <jsp:param name="none" value="" />
            </jsp:include>
            <div id="popitmenu" onMouseover="clearhidemenu();" onMouseout="dynamichide(event)"></div>
        </td>
    </tr>
    <tr>
        <td>
            <% if(!("freezepane").equals(layout) && !("filmstrip").equals(layout)) { %>
                <div style='width:100vw;'>
            <% } %>
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                           <td nowrap rowspan=2>
                                <span class="HEADING1">
                                    <%=FormatHelper.encodeAndFormatForHTMLContent(season.getValue("seasonName").toString()) %> :
                                    <% if(editLinePlan){
                                        String editLinePlanScope = request.getParameter("editLinePlanScope");
                                        boolean productSKUEdit = "productSku".equals(editLinePlanScope);
                                        boolean seasonEdit = "season".equals(editLinePlanScope);

                                        boolean placeholderEdit = "PLACEHOLDER".equals(editLinePlanScope);

                                        boolean sourcingEdit = "SOURCING_CONFIG_SCOPE".equals(editLinePlanScope);
                                        boolean srcToSeasonEdit = "SOURCE_TO_SEASON_SCOPE".equals(editLinePlanScope);
                                        boolean costSheetEdit = "COST_SHEET".equals(editLinePlanScope);
                                    %>
                                        <%= linePlanEditorlabel %>
                                        <% if(workMode) { %>
                                            <br><font class="HEADING2"><%= FormatHelper.encodeForHTMLContent(workItemName) %></font>
                                        <% } %>

                                    <% } else if(workMode) { %>
                                        <%= linePlanWorkListLabel %><br><font class="HEADING2"><%= FormatHelper.encodeForHTMLContent(workItemName) %></font>
                                    <% } else { %>
                                        <%= linePlanViewerLabel %>
                                    <% } %>
                                </span>
                            </td>
                            <% if(!workMode) { %>
                            <td rowspan=2 style='padding-bottom: 16px;padding-left: 10px;'>
                                    &nbsp;&nbsp;  <a onmouseover="return overlib('<%= FormatHelper.formatJavascriptString(addLineSheetToFavoritesLabel) %>', LEFT);" onmouseout="return nd();" href="javascript:addObjectToFavorites('<%= FormatHelper.getVersionId(season) %>')"><img   border="0" src="<%=WT_IMAGE_LOCATION%>/favorites_add.gif"></a>
                            </td>
                            <% } %>
                            <td align="left" nowrap>
                                <table border="0" cellspacing="0" cellpadding="0">
                                <%
                                    //boolean useViews = true;
                                    boolean chooser = false;
                                    boolean useUpdate = false;
                                %>
                                    <%@ include file="../../../jsp/season/LPViewButtons.jspf" %>
                                </table>
                            </td>
                            <td valign="top" rowspan="2" width="40%">
                              <%@ include file="../../../jsp/reports/PinnedViewsInclude.jspf" %>
                            </td>
                        </tr>
                        <tr>
                           <% if(!workMode){ %>
                            <td style='padding-bottom: 6px;' nowrap valign="left">
                                <span class="LABEL"><%= actionsLabel %>&nbsp;</span>
                                <select name="linePlanActions" onChange="evalList(this)">
                                    <option>
                                    <% if(ACLHelper.hasCreateAccess(productType)){ %>
                                        <%if(!lcsContext.isVendor && USE_PLACEHOLDERS){%>
                                            <option value="addNewPlaceholder()"><%= createNewOpt %> <%= placeholderLabel%>
                                        <%}%>
                                        <option value="addNewProduct()"><%= createNewOpt %> <%= productLabel %>
                                    <% } %>
                                    <%if(!placeholdersOnly && ACLHelper.hasCreateAccess(productType.getReferencedFlexType(ReferencedTypeKeys.SOURCING_CONFIG_TYPE))){%>
                                        <option value="addMultipleSourcingConfigs()"><%= createNewOpt %> <%= sourcingConfigsLabel %>
                                    <%}%>
                                     <%if((FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL.equals(linePlanLevel) || !FormatHelper.hasContent(linePlanLevel)) && LineSheetQuery.PLACEHOLDER_MODE_PRODUCT.equals(placeholderMode.toString()) && !activeCostSheets && !whatIfCostSheets && LineSheetUtils.useSourcing(false, columnMap, columns, linePlanConfig.get(pTypeName)) && ACLHelper.hasCreateAccess(productType.getReferencedFlexType(ReferencedTypeKeys.SPEC_TYPE))){%>
                                        <option value="addMultipleSpecifications()"><%= createNewOpt %> <%= specificationsLabel %>
                                    <%}%>
                                    <%if(USE_MULTI_COSTING && !placeholdersOnly&& ACLHelper.hasCreateAccess(productType.getReferencedFlexType(ReferencedTypeKeys.COST_SHEET_TYPE))){%>
                                        <option value="addMultipleCostSheets()"><%= createNewOpt %> <%= costingLabel %>
                                    <%}%>
                                    <%if(ACLHelper.hasCreateAccess(sampleType)&& !placeholdersOnly){%>
                                        <option value="addMultipleSamples()"><%= createNewOpt %> <%= samplesLabel %>
                                    <%}%>
                                    <%if(USE_ORDERCONFIRMATION && !lcsContext.isVendor && ACLHelper.hasCreateAccess(orderConfirmationType)&& !placeholdersOnly){%>
                                        <option>-----------------------------------
                                        <option value="createOrderConfirmation()"><%= createNewOpt %> <%= orderConfirmationLabel %>
                                    <%}%>
                                    <%if(ENABLE_RFQ && !lcsContext.isVendor && ACLHelper.hasCreateAccess(rfqRootType)&& !placeholdersOnly){%>
                                        <option>-----------------------------------
                                        <option value="createRFQ()"><%= createNewOpt %> <%= rfqLabel %>
                                    <%}%>
                                    <option>-----------------------------------
                                     <% //Check here against a properties file to see if Colorway Matrix is enabled. Then check ACL + options.

                                        if (!lcsContext.isVendor && ENABLE_COLORWAY_MATRIX && LineSheetUtils.hasManageColorwaysAccess(productType, linePlanConfig.get(pTypeName)))
                                        { %>
                                                <option value="manageColorWayMatrix();"><%= WTMessage.getLocalizedMessage ( RB.MAIN, "showColorwayMatrix_LBL", RB.objA ) %>
                                                <option>-----------------------------------
                                        <% } %>
                                    <% //Check here against a properties file to see if Carry Over/Move
                                    //should be dependent on create or update ACL permissions
                                    if ( !lcsContext.isVendor && ((CREATE_UPDATE_ACL_FOR_CARRYOVER.equalsIgnoreCase("create") && (ACLHelper.hasCreateAccess(productType))) ||
                                        (CREATE_UPDATE_ACL_FOR_CARRYOVER.equalsIgnoreCase("update") && (ACLHelper.hasEditAccess(productType))))) {
                                    %>

                                        <% if(ACLHelper.hasCreateAccess(productType)){ %>
                                            <option value="copyProductFromSeason();"><%= copyProductFromSeason %>
                                            <option>-----------------------------------

                                            <option value="massCopyProductsFromSeason();"><%= massCopyProducts %>
                                            <option>-----------------------------------
                                        <% } %>

                                        <option value="carryOverProducts();"><%= carryOverOpt %> <%= productsLabel %>
                                        <option value="carryOverSKUs();"><%= carryOverOpt %> <%= skusLabel %>
                                        <option value="moveProducts();"><%= moveOpt %> <%= productsLabel %>
                                        <option value="moveSKUs();"><%= moveOpt %> <%= skusLabel %>
                                        <option>-----------------------------------


                                    <% } %>
										<!-- changes for costing uploader start -->
										<%
										String division = (String)season.getValue(productDivisionKey);
										division = divisionMap.get(division);
										List<String> tempList = new ArrayList<String>();
										boolean hasCreateAccess_nonFootwear_HighLevel = false;
										boolean hasReadAccess_nonFootwear_HighLevel = false;
										boolean hasEditAccess_nonFootwear_HighLevel = false;
										
										boolean hasCreateAccess_nonFootwear_Breakdown = false;
										boolean hasReadAccess_nonFootwear_Breakdown = false;
										boolean hasEditAccess_nonFootwear_Breakdown = false;
										
										boolean hasCreateAccess_Footwear_Breakdown = false;
										boolean hasReadAccess_Footwear_Breakdown = false;
										boolean hasEditAccess_Footwear_Breakdown = false;
										
										if (productCostSheetTypeMap.containsKey(productType.getFullName(true))) {
										 	tempList = productCostSheetTypeMap.get(productType.getFullName(true));
										}
										
										for(String tempTypeName: tempList){
											
											if(tempTypeName.contains("HighLevel")){
												
												hasCreateAccess_nonFootwear_HighLevel = ACLHelper.hasCreateAccess(FlexTypeCache.getFlexTypeFromPath(tempTypeName));
												hasReadAccess_nonFootwear_HighLevel = ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeFromPath(tempTypeName));
												hasEditAccess_nonFootwear_HighLevel = ACLHelper.hasEditAccess(FlexTypeCache.getFlexTypeFromPath(tempTypeName));
											}else if(tempTypeName.contains("Breakdown") && !division.equalsIgnoreCase("footwear")){
												
												hasCreateAccess_nonFootwear_Breakdown = ACLHelper.hasCreateAccess(FlexTypeCache.getFlexTypeFromPath(tempTypeName));
												hasReadAccess_nonFootwear_Breakdown = ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeFromPath(tempTypeName));
												hasEditAccess_nonFootwear_Breakdown = ACLHelper.hasEditAccess(FlexTypeCache.getFlexTypeFromPath(tempTypeName));
											}else if(tempTypeName.contains("Breakdown") && division.equalsIgnoreCase("footwear")){
												
												hasCreateAccess_Footwear_Breakdown = ACLHelper.hasCreateAccess(FlexTypeCache.getFlexTypeFromPath(tempTypeName));
												hasReadAccess_Footwear_Breakdown = ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeFromPath(tempTypeName));
												hasEditAccess_Footwear_Breakdown = ACLHelper.hasEditAccess(FlexTypeCache.getFlexTypeFromPath(tempTypeName));
											}
											
										}
										if(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SKU_LEVEL.equals(linePlanLevel) && applicableViews.contains(viewDisplayName)) {
											if(hasReadAccess_Footwear_Breakdown || hasReadAccess_nonFootwear_Breakdown ){	%>
											<option value="costSheetDataDownload('<%= division %>','detailed');"><%= csDetailedBreakdownDownloadLabel %>
												<% if(division !=null && hasReadAccess_nonFootwear_HighLevel){ %>
											<option value="costSheetDataDownload('<%= division %>', 'highLevel');"><%= csHighLevelDownloadLabel %>
										<%} } if((hasCreateAccess_nonFootwear_Breakdown && hasEditAccess_nonFootwear_Breakdown) || (hasCreateAccess_Footwear_Breakdown && hasEditAccess_Footwear_Breakdown)){%>
												<% if(division != null && ("footwear".equalsIgnoreCase(division) || "apparel".equalsIgnoreCase(division))){%> 
												 <option value="csDetailedBreakdownUpload('<%= division %>');"><%= csDetailedBreakdownUploadLabel %> 
												<%} if(division !=null && (hasCreateAccess_nonFootwear_HighLevel && hasEditAccess_nonFootwear_HighLevel)){ %>
												 <option value="csHighLevelTemplateUpload('<%= division %>');"><%= csHighLevelUploadLabel %>
										<%} 
										}%>
											<option>-----------------------------------
										<%}%>
											<%											
											
											if(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SCOPE.equals(linePlanLevel) && PROD_COST_VIEW.equals(viewDisplayName)) {
											%>		
											<%							
													
													if(division !=null && hasReadAccess_nonFootwear_HighLevel){ %>
										              <option value="prodCostSheetDataDownload('<%= division %>', 'highLevel');"><%= csHighLevelDownloadLabel %>
													<%} if(division !=null && (hasCreateAccess_nonFootwear_HighLevel && hasEditAccess_nonFootwear_HighLevel)){ %>
													  <option value="prodCSHighLevelTemplateUpload('<%= division %>');"><%= csHighLevelUploadLabel %>
													  <%  }
													  }
											%>
										<!-- changes for costing uploader end -->

                                    <% if(ACLHelper.hasEditAccess(productType)){
                                         if(!lcsContext.isVendor){%>
                                            <option value="launchModuleChooser('PRODUCT','' ,'' ,'', true, 'addProducts', false, 'master', document.LINEPLAN_HEADER.seasonModelProductType);"><%= addExisting %> <%= productsLabel %>
                                            <option value="launchModuleChooser('SKU','' ,'' ,'', true, 'addSKUs', false, 'master', document.LINEPLAN_HEADER.seasonModelProductType);"><%= addExisting %> <%= skusLabel %>

                                            <option>-----------------------------------
                                        <% } %>
                                    <% if(!linePlanSourcing && !activeCostSheets && !whatIfCostSheets && LineSheetQuery.PLACEHOLDER_MODE_PRODUCT.equals(placeholderMode.toString()) && !placeholders){ %>
                                       <% if(( (CREATE_UPDATE_ACL_FOR_CARRYOVER.equalsIgnoreCase("create") && hasSPLCreateAccess ) || (CREATE_UPDATE_ACL_FOR_CARRYOVER.equalsIgnoreCase("update") && hasSPLUpdateAccess))
                                                 && (FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL.equals(linePlanLevel) || !FormatHelper.hasContent(linePlanLevel)) && (!lcsContext.isVendor)) { %>
                                       <option value="removeProducts()"><%= removeOpt %> <%= productsLabel %>
                                       <% } else if(((CREATE_UPDATE_ACL_FOR_CARRYOVER.equalsIgnoreCase("create") && hasSKUCreateAccess) || (CREATE_UPDATE_ACL_FOR_CARRYOVER.equalsIgnoreCase("update") && hasSKULinkUpdateAccess) ) && (FootwearApparelFlexTypeScopeDefinition.PRODUCT_SKU_LEVEL.equals(linePlanLevel))) { %>
                                       <option value="removeSKUs()"><%= removeOpt %> <%= skusLabel %>
                                       <% } %>
                                       <option>-----------------------------------
                                     <% } %>

                                        <% if((CollectionUtil.findIntersection(refreshCostsGroups, lcsContext.getGroups()).size() > 0) && (!lcsContext.isVendor)){ %>
                                            <option value="refreshCosts()"><%= recalculateCostsOpt %>
                                            <option>-----------------------------------
                                        <% } %>
                                        <% if(!lcsContext.isVendor){ %>
                                            <option value="viewSeasonChangeReport()"><%= changeReportOpt %>
                                            <option>-----------------------------------
                                        <% } %>
                                    <% } %>
                                    <%if( ACLHelper.hasCreateAccess(FlexTypeCache.getFlexTypeFromPath("Season Group\\Line Board"))){ %>
                                    <option value="viewLineBoard(document.LINEPLAN_HEADER.seasonId)"><%= viewLineBoardOpt %>
                                    <%} %>
                                    <%if( ACLHelper.hasCreateAccess(FlexTypeCache.getFlexTypeFromPath("Season Group\\Line Board"))){ %>
                                    <option value="createLineBoard()"><%= createLineBoardOpt %>
                                    <%} %>
                                    <%if(USE_PDF_PRINT_SPEC){%>
                                        <option value="generateProductSpecs('<%= addParam %>', <%= newBOM %>, '<%=FormatHelper.getObjectId(FlexTypeCache.getFlexTypeRoot("Specification")) %>')"><%= generateProductSpecifications %>
                                        <option>-----------------------------------
                                    <%}%>
                                    <%

                                    String typeName = flexType.getFullName(true);
                                    if(ThingworxDashboardHelper.hasActiveDashboards(typeName, request.getParameter("activity"), tabPage)) { %>
                                        <jsp:include page="<%=subURLFolder+ TWX_DASHBOARD_OPTION %>" flush="true">
                                        <jsp:param name="type" value="<%= typeName %>" />
                                        <jsp:param name="returnActivity" value="<%= request.getParameter(\"activity\") %>" />
                                        <jsp:param name="returnAction" value="<%= action %>" />
                                        <jsp:param name="returnOid" value="<%=  oid %>" />
                                        <jsp:param name="tabPage" value="<%= tabPage %>" />
                                     </jsp:include>
                                     <option>-----------------------------------
                                 <% } %>

            <%-- Added for story : B-109259 --%>
            <%-- Configuration of event subscription for email notification --%>
            <%-- Start --%>
            <% if(FORUMS_ENABLED && SUBSCRIPTION_ENABLED){ %>
            &nbsp;
            <jsp:include page="<%=subURLFolder+ DISCUSSION_FORM %>" flush="true">
              <jsp:param name="type" value="<%= flexType.getType() %>" />
              <jsp:param name="targetOid" value="<%= FormatHelper.getVersionId(season) %>" />
            </jsp:include>
            &nbsp;
            <jsp:include page="<%=subURLFolder+ SUBSCRIPTION_FORM %>" flush="true">
              <jsp:param name="type" value="<%= flexType.getType() %>" />
              <jsp:param name="targetOid" value="<%= FormatHelper.getVersionId(season) %>" />
            </jsp:include>
            <% } else if(FORUMS_ENABLED){ %>
            <jsp:include page="<%=subURLFolder+ DISCUSSION_FORM %>" flush="true">
               <jsp:param name="type" value="<%= flexType.getType() %>" />
              <jsp:param name="targetOid" value="<%= FormatHelper.getVersionId(season) %>" />
            </jsp:include>
            <% } else if(SUBSCRIPTION_ENABLED){ %>
            <jsp:include page="<%=subURLFolder+ SUBSCRIPTION_FORM %>" flush="true">
               <jsp:param name="type" value="<%= flexType.getType() %>" />
              <jsp:param name="targetOid" value="<%= FormatHelper.getVersionId(season) %>" />
           </jsp:include>
            <% } %>
            <%-- End --%>
            &nbsp;

                                </select>
                            </td>
                           <% }%>
                        </tr>
                        <tr class="header-strip">
                            <td colspan=2 style='padding-right:20px;text-align:right'>
                                <span id="exportingSpan">
                                    <%-- <a> tag for layout like other images in the header-strip --%>
                                    <a><img border="0" src="<%=URL_CONTEXT%>/images/blue-loading.gif" height="16" width="16"></a>
                                    <%=FormatHelper.formatJavascriptString(loadingMSG, false)%>
                                </span>
                            </td>
                            <td>
                                <a id="thumbsButton" style='vertical-align: top;' href="javascript:toggleThumbs(<%= !showThumbs %>)"><%= showThumbs ? hideImagesLabel : showImagesLabel %></a>
                                &nbsp;&nbsp;
                                <%
                                if (columns != null) request.setAttribute("columns", columns);
                                if (linePlanData != null){
                                    request.setAttribute("data", linePlanData);
                                }
                                String csv = request.getParameter("csv");
                                String pdf = request.getParameter("pdf");
                                %>
                                <% if(runLinePlan && !linePlanChooser & !workMode){ %>
                                    <jsp:include page="<%=subURLFolder+ EXCEL_GENERATOR_SUPPORT_PLUGIN %>" flush="true">
                                        <jsp:param name="csv" value="<%= csv %>" />
                                        <jsp:param name="reportDisplayName" value="<%= linePlan %>" />
                                        <jsp:param name="reportName" value="<%= linePlan %>" />
                                        <jsp:param name="layout" value="<%= layout %>" />
                                    </jsp:include>
                                    <% if(usePDF){ %>
                                        <jsp:include page="<%=subURLFolder+ PDF_GENERATOR %>" flush="true">
                                            <jsp:param name="pdf" value="<%= pdf %>"/>
                                            <jsp:param name="reportName" value="<%= documentLabel %>"/>
                                            <jsp:param name="cellClassDark" value="RPT_TBD"/>
                                            <jsp:param name="cellClassLight" value="RPT_TBL"/>
                                            <jsp:param name="tableHeaderClass" value="RPT_HEADER"/>
                                            <jsp:param name="tableTotalsClass" value="RPT_TOTALS"/>
                                            <jsp:param name="reportTitle" value="<%=reportTitle%>"/>
                                            <jsp:param name="userName" value="<%=userName%>"/>
                                            <jsp:param name="reportDate" value="<%=reportDate%>"/>
                                            <jsp:param name="layout" value="<%= layout %>" />
                                        </jsp:include>
                                    <% }  %>

                                    <%if(USE_PDF_PRINT_SPEC){%>
                                        <a onmouseover="return overlib('<%= FormatHelper.formatJavascriptString(zipTooltip) %>');" onmouseout="return nd();" class="" href="javascript:generateProductSpecs('<%= addParam %>', <%= newBOM %>, '<%=FormatHelper.getObjectId(FlexTypeCache.getFlexTypeRoot("Specification")) %>')"><img border="0" src="<%= WT_IMAGE_LOCATION %>/managed_collection.gif"></a>&nbsp;&nbsp;
                                    <% } %>

                                <% } %>

                                <% if(!workMode) {%>
                                <a onmouseover="return overlib('<%= FormatHelper.formatJavascriptString(addToFavoritesLabel) %>', LEFT);" onmouseout="return nd();" href="javascript:addToFavorites()"><img border="0" src="<%=WT_IMAGE_LOCATION%>/favorites_add.gif"></a>
                                <% } %>
                                &nbsp;
                    <%     if(enableFreezePane) {%>
                                <a onmouseover="return overlib('<%= freezepaneLabel%>', LEFT);" onmouseout="return nd();" href="javascript:setLayout('freezepane')"><img border="0" src="<%=URL_CONTEXT%>/images/table_freeze_panes.png"></a>
                    <%     }%>
                                <a onmouseover="return overlib('<%= thumbnailLabel%>', LEFT);" onmouseout="return nd();" href="javascript:setLayout('thumbnails')"><img border="0" src="<%=WT_IMAGE_LOCATION%>/thumbnail.png"></a>
                                <a onmouseover="return overlib('<%= filmstripLabel%>', LEFT);" onmouseout="return nd();" href="javascript:setLayout('filmstrip')"><img border="0" src="<%= WT_IMAGE_LOCATION %>/filmstrip.png"></a>
                    <%     if(enableClassicMode) {%>
                                <a onmouseover="return overlib('<%= listLabel%>', LEFT);" onmouseout="return nd();" href="javascript:setLayout('list')"><img border="0" src="<%=WT_IMAGE_LOCATION%>/flexdetails.png"></a>
                    <%} %>
                    <% if( USE_SAMPLES && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Sample")) && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeFromPath("Sample\\Product")) &&  ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Product"))) {%>
                            <a onmouseover="return overlib('<%= samplesConsoleLabel%>', LEFT);" onmouseout="return nd();" href="javascript:viewSamplesConsole('<%= FormatHelper.encodeForJavascript(oid) %>')"><img width='16' border="0" src="<%= WT_IMAGE_LOCATION %>/samples_console.png"></a>
                    <%} %>
                            </td>
                            <td style='border-top: solid 1px grey;white-space: nowrap' align="right" rowspan="2">
                                <%= resultsLabelWithNumbers %>
                                &nbsp;&nbsp;
                           </td>
                        </tr>
                    </table>
            <% if(!("freezepane").equals(layout) && !("filmstrip").equals(layout)) { %>
                </div>
            <% } %>
        </td>
    </tr>
<% } %>

<%if (!("freezepane".equals(layout) && editLinePlan)) { %>
    <tr>
        <td class="SEARCH_RESULTS_BAR">
            <table>
                <tr>
                    <td>
                        <div class="button-set">
                            <% if(!editLinePlan){ %>
                                <a class="button" id="filterButton" href="javascript:toggleFilters();resizeFreezePane();"><%= filtersButton %></a>
                                <a class="button" id="optionsButton" href="javascript:toggleDiv('linePlanOptionsDiv');resizeFreezePane();"><%= optionsButton %></a>
                            <%  } %>
                            <% if (editLinePlan) { %>
                                <a class="button" id="saveLinePlanButton" href="javascript:saveLinePlanValidation()" ><%= saveChangesButton %></a>
                                <a class="button" href="javascript:cancelEditLinePlan()"><%= cancelButton %></a>
                                &nbsp;&nbsp;<span id="loadingSpan"><img style="vertical-align:middle" src="<%=URL_CONTEXT%>/images/blue-loading.gif" height="18" width="18">&nbsp;<span><%=FormatHelper.formatJavascriptString(loadingMSG, false)%></span></span>
                            <% } else { %>
                                <a class="button" href="javascript:refresh()"><%= runButton %></a>
                               <% if(ACLHelper.hasEditAccess(productType) && !linePlanChooser){ %>
                                <a class="button" href="javascript:runEditLinePlan('multipleScopes', 'multipleLevels')"><%= editLinePlanOpt %></a>
                               <% } %>
                            <% } %>
                        </div>
                    </td>
                    <%
                    /////////////////////////////////////////////////////////////////////////////////////////
                    //  Render SeasonGroup widgets next to the run button, one action menu/one picklist for each type
                    //  of seasongroup configured for this season
                    /////////////////////////////////////////////////////////////////////////////////////////
                    Iterator seasonGroupsTypeIds = MOAHelper.getMOASet(season.getSeasonGroupIds()).iterator();
                    SearchResults seasonGroupResults = new SeasonGroupQuery().findSeasonGroupsForSeason(season);
                    Map seasonGroups;
                    Collection filteredList;
                    String formElementId;
                    String seasonGroupTypeId;
                    while(seasonGroupsTypeIds.hasNext()){
                        seasonGroupTypeId = (String) seasonGroupsTypeIds.next();
                        FlexType sgType = FlexTypeCache.getFlexType(seasonGroupTypeId);
                        if (ACLHelper.hasViewAccess(sgType)){
                            if(editLinePlan){
                                formElementId = "SG_" + FormatHelper.getNumericObjectIdFromObject(sgType);
                                String selectedSeasonGroupId = (String) linePlanConfig.get(pTypeName, formElementId);
                                out.println(FormGenerator.createHiddenInput(formElementId, selectedSeasonGroupId));
                            }else{
                    %>
                    <td>
                        <div class="BoxWrapper">
                            <div class="Box">
                                <div class="BoxHeader">
                                    <div>
                                    </div>
                                </div>
                                <div class="BoxContent">
                                <%
                                    formElementId = "SG_" + FormatHelper.getNumericObjectIdFromObject(sgType);

                                    filteredList = TableDataUtil.filterBasedOnValue(seasonGroupResults.getResults(), "SEASONGROUP.BRANCHIDA2TYPEDEFINITIONREFE", FormatHelper.getNumericObjectIdFromObject(sgType));
                                    SearchResults tempResults = new SearchResults();
                                    tempResults.setResults(new Vector(filteredList));
                                    seasonGroups = LCSQuery.createTableList(tempResults, "SEASONGROUP.BRANCHIDITERATIONINFO", seasonGroupNameAtt.getSearchResultIndex().toUpperCase(),  "VR:com.lcs.wc.season.SeasonGroup:");
                                    String selectedSeasonGroupId = (String) linePlanConfig.get(pTypeName, formElementId);
                                    if (FormatHelper.hasContent(request.getParameter("seasonGroupId"))&&seasonGroups.containsKey(request.getParameter("seasonGroupId"))) {
                                        selectedSeasonGroupId = request.getParameter("seasonGroupId");
                                    }

                                    if(FormatHelper.hasContent((String) linePlanConfig.get(pTypeName, formElementId))){
                                        highlightFilter = true;
                                    }
                                    boolean drawSkusLink=false;
                                    if (skus && !linePlanChooser) {
                                        drawSkusLink = true;
                                    }

                                %>
                                    <div class="LABEL">
                                        <table><tr><td>

                                            &nbsp;&nbsp;<a id="ac_SG_<%= FormatHelper.getObjectId(sgType) %>" href="javascript:getActionsMenu('<%=FormatHelper.getObjectId(sgType)%>', 'SEASONGROUP', '&skusLink=<%=drawSkusLink%>', 'ac_SG_')" class="actions_link"><%=FormatHelper.encodeAndFormatForHTMLContent(sgType.getTypeDisplayName())%></a>
                                        </td>
                                        <%= fg.createDropDownListWidget("", seasonGroups, formElementId, selectedSeasonGroupId, "", false) %>
                                        </tr></table>
                                    </div>
                                </div>
                                <div class="BoxFooter">
                                    <div></div>
                                </div>
                            </div>
                        </div>
                    </td>
                        <% }//!EditLinePlan %>
                    <% } //  if (ACLHelper.hasViewAccess(sgType)){ %>
                <% } //while(seasonGroupsTypeIds.hasNext()){ %>
            </tr>
        </table>
<% if (highlightFilter) {%>
    <script>
        var filterButton = document.getElementById("filterButton");
        highlightButton(filterButton);
    </script>
<% } %>

<% if (highlightOptions) {%>
    <script>
        var optionsButton = document.getElementById("optionsButton");
        highlightButton(optionsButton);
    </script>
<% } %>
                    <jsp:include page="<%=subURLFolder+ DISCUSSION_FORM_POSTINGS %>" flush="true">
                        <jsp:param name="oid" value="<%= FormatHelper.getObjectId(season) %>" />
                    </jsp:include>
        <% if(usedCache){ %>
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <span class="LABEL">
        <a onmouseover="return overlib('<%= FormatHelper.formatJavascriptString(lineSheetCachedUseRunButtonToReloadDataMouseOver) %>');" onmouseout="return nd();" href="javascript:void(0)">(<%= cachedAsOfLabel %>&nbsp;<%= FormatHelper.formatWithTime(linePlanDataCache.getDate()) %>)</a>
        </span>
        <% } %>
        </td>
    </tr>
<% } %>
    <tr>
        <td>
            <div class="BORDERED_BLOCK" id="linePlanOptionsDiv">
                <table>
                    <tr>
                        <td class="HEADING3" colspan="2"><%= lineViewerOptionsPgHead %></td>
                    </tr>
                    <tr>
                    <%  Hashtable levels = new Hashtable();
                        levels.put(FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL, productLabel);
                        levels.put(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SKU_LEVEL, skuLabel);
                        Vector order = new Vector();
                        order.add(FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL);
                        order.add(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SKU_LEVEL);
                    %>
                        <%//= fg.createDropDownListWidget("Level", levels, "linePlanLevel", linePlanLevel, "", false) %>
                        <%= fg.createDropDownListWidget(levelLabel, levels, "linePlanLevel", linePlanLevel, "syncShowPlaceholderBox();", false, false, order, null, null) %>
                    </tr>
                    <tr>
                    <%if(USE_PLACEHOLDERS){
                        String productPlaceholderLabel = WTMessage.getLocalizedMessage(RB.PLACEHOLDER, "productOrPlaceholderMenu", RB.objA);
                        Hashtable prodOrPlaceList = new Hashtable();
                        prodOrPlaceList.put(LineSheetQuery.PLACEHOLDER_MODE_PRODUCT, WTMessage.getLocalizedMessage(RB.PLACEHOLDER, "productsOnly_OPTION", RB.objA));
                        if(!lcsContext.isVendor){
                            prodOrPlaceList.put(LineSheetQuery.PLACEHOLDER_MODE_PLACEHOLDER, WTMessage.getLocalizedMessage(RB.PLACEHOLDER, "placeholdersOnly_OPTION", RB.objA));
                            prodOrPlaceList.put(LineSheetQuery.PLACEHOLDER_MODE_BOTH, WTMessage.getLocalizedMessage(RB.PLACEHOLDER, "productsAndPlaceholders_OPTION", RB.objA));
                       }
                       out.println(fg.createDropDownListWidget(productPlaceholderLabel, prodOrPlaceList, "placeholderMode", placeholderMode.toString(), "javascript:changePlaceholdersOptions(this,'" + LineSheetQuery.PLACEHOLDER_MODE_PLACEHOLDER + "','" + FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL + "','" + workMode + "')", false, false));
                    %>
                    <script>
                        changePlaceholdersOptions(document.MAINFORM.placeholderMode,'<%=LineSheetQuery.PLACEHOLDER_MODE_PLACEHOLDER%>','<%=FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL%>');
                    </script>
                    <%
                    }%>
                    </tr>
                    <tr>
                    <%
                        Hashtable groupList = new Hashtable();
                        Iterator columnIter = columns.iterator();
                        while(columnIter.hasNext()) {
                            TableColumn column = (TableColumn) columnIter.next();
                            if(column!=null) {
                                if(column.getTableIndex()!=null && column.getHeaderLabel() != null && !column.getHeaderLabel().equals("")) {
                                   if (column.getAttributeType() != null && column.getAttributeType().equalsIgnoreCase("constant")){
                                        // do nothing, constant attributes cannot be grouped by. Note: we might have an attribute with null attributetype that needs to be added on the group by.
                                        if(logger.isDebugEnabled()) {logger.debug("########## Skipping constant attribute: " + column.getHeaderLabel());}
                                    }
                                    else{
                                        groupList.put(column.getTableIndex(),column.getHeaderLabel());
                                    }
                                }
                            }
                        }

                        // SPR 2169665
                        groupList.remove("COLORCHIPINFO");
                        groupList.remove("COLORCHIPINFO_CHIP");

                    %>
                        <%= fg.createDropDownListWidget(groupByLabel, groupList, "groupByAttribute", groupByAttributeHolder, "", false) %>
                    </tr>
                    <tr>
                        <td class="FORMLABEL">&nbsp;&nbsp;&nbsp;<%= showThumbnailsLabel %></td>
                        <td><input name="showThumbsBox"  type=checkbox onClick="handleCheckBox(this, document.MAINFORM.showThumbs)" <%= showThumbs ? "checked" : "" %>></td>
                    </tr>
                    <tr>
                        <td class="FORMLABEL">&nbsp;&nbsp;&nbsp;<%= includePlaceHoldersLabel %></td>
                        <td><input type="hidden" name="placeholders" value="<%=  placeholders %>"><input name="placeHoldersBox" id="placeHoldersBox" type=checkbox onClick="handleCheckBox(this, document.MAINFORM.placeholders);" <% if(placeholders){ out.print("checked"); } if(!skus){out.println(" disabled='true' ");}%>></td>
                    </tr>

                    <tr>
                        <td class="FORMLABEL">&nbsp;&nbsp;&nbsp;<%= allSourcesLabel %></td>
                        <td><input type="hidden" name="linePlanSourcing" value="<%=  linePlanSourcing %>"><input name="linePlanSourcingBox"  type=checkbox onClick="handleCheckBox(this, document.MAINFORM.linePlanSourcing)" <% if(linePlanSourcing){ out.print("checked"); } %>></td>
                    </tr>
                    <% if (USE_MULTI_COSTING) { %>
                    <tr>
                        <td class="FORMLABEL">&nbsp;&nbsp;&nbsp;<%= includeActiveCostSheetsLabel %></td>
                        <td><input type="hidden" name="activeCostSheets" value="<%=  activeCostSheets %>"><input name="activeCostSheetsBox"  type=checkbox onClick="handleCheckBox(this, document.MAINFORM.activeCostSheets);syncActiveBox(this, document.MAINFORM.whatIfCostSheets, document.MAINFORM.activeCostSheets);" <% if(activeCostSheets){ out.print("checked"); } %>></td>
                    </tr>
                    <% } %>
                    <tr>
                        <td class="FORMLABEL">&nbsp;&nbsp;&nbsp;<%= includeWhatIfCostSheetsLabel %></td>
                        <td><input type="hidden" name="whatIfCostSheets" value="<%=  whatIfCostSheets %>"><input name="whatIfCostSheetsBox"  type=checkbox onClick="handleCheckBox(this, document.MAINFORM.whatIfCostSheets);syncWhatIfBox(this, document.MAINFORM.whatIfCostSheets, document.MAINFORM.activeCostSheets);" <% if(whatIfCostSheets){ out.print("checked"); } %>></td>
                    </tr>
                    <tr>
                        <td class="FORMLABEL">&nbsp;&nbsp;&nbsp;<%= showChangedProdInfoLabel %></td>
                        <td>
                        <input type="hidden" name="showChangedProdInfo" value="<%=  showChangedProdInfo %>">
                        <input name="showChangedProdInfoBox"  type=checkbox onClick="handleCheckBox(this, document.MAINFORM.showChangedProdInfo)" <% if(showChangedProdInfo){ out.print("checked"); } %>>
                        </td>
                    </tr>
                </table>
            </div>
        </td>
    </tr>

    <script>

    var placeholderModeDropdown = document.getElementById('placeholderMode');
    if (placeholderModeDropdown && placeholderModeDropdown.options[placeholderModeDropdown.selectedIndex].value == '<%= FormatHelper.formatJavascriptString(LineSheetQuery.PLACEHOLDER_MODE_PLACEHOLDER, false)%>'){
        document.MAINFORM.showChangedProdInfoBox.checked=false;
        document.MAINFORM.showChangedProdInfoBox.disabled=true;
        document.MAINFORM.showChangedProdInfo.value=false;
    }

    <% if(workMode || linePlanChooser || editLinePlan) { %>

        document.MAINFORM.linePlanLevel.disabled = true;
        document.MAINFORM.linePlanLevel.title = '<%= FormatHelper.formatJavascriptString(levelSelectionDisabledInThisMode, false) %>';
        document.MAINFORM.linePlanSourcingBox.disabled = true;
        document.MAINFORM.linePlanSourcingBox.title = '<%= FormatHelper.formatJavascriptString(sourcingSelectionDisabledInThisMode, false) %>';
    <% } %>
    <% if(editLinePlan) { %>

        document.MAINFORM.linePlanView.disabled = true;
        document.MAINFORM.linePlanView.title = '<%= FormatHelper.formatJavascriptString(viewSelectionDisabledInThisMode) %>';
        if(document.MAINFORM.linePlanActions){
            document.MAINFORM.linePlanActions.disabled = true;
            document.MAINFORM.linePlanActions.title = '<%= FormatHelper.formatJavascriptString(linePlanActionsDisabledInThisMode) %>';
        }

    <% } %>
    toggleDiv('linePlanOptionsDiv');

    var pleaseSelectSpecificationMSG = '<%=FormatHelper.formatJavascriptString(pleaseSelectSpecificationMSG, false)%>';
    var pleaseSelectMeasurementSetMSG = '<%=FormatHelper.formatJavascriptString(pleaseSelectMeasurementSetMSG, false)%>';
    var loadingMSG = '<%=FormatHelper.formatJavascriptString(loadingMSG, false)%>';
    var noneAvailableLabel = '<%=FormatHelper.formatJavascriptString(noneAvailableLabel, false)%>';
    var newLabel = '<%=FormatHelper.formatJavascriptString(newLabel, false)%>';
    var notApplicableLabel = '<%=FormatHelper.formatJavascriptString(notApplicableLabel, false)%>';

    function linesheetDOMLoaded() {
        var saveLinePlanButton = document.getElementById('saveLinePlanButton');
        saveLinePlanButton.classList.remove('buttonDisabled');
        var loadingSpan = document.getElementById('loadingSpan');
        loadingSpan.parentElement.removeChild(loadingSpan);
    }

    var saveLinePlanButton = document.getElementById('saveLinePlanButton');
    if (saveLinePlanButton) {
        saveLinePlanButton.classList.add('buttonDisabled');
        if (document.readyState === "loading") {
            document.addEventListener("DOMContentLoaded", linesheetDOMLoaded);
        }
        else {
            linesheetDOMLoaded();
        }
    }

    showExporting(false);

    </script>
<!-- changes for costing uploader start -->
<script>

function costSheetDataDownload(division, csType){

  var selectedCostSheets = [];
		
		if (typeof Ext !== 'undefined'){
			var flexExtGrid = Ext.ComponentMgr.get('flexExtGrid');
			if (flexExtGrid === undefined) {
				flexExtGrid = Ext.ComponentMgr.get('lineSheetGrid');
			}
			if(flexExtGrid.getStore().data.items.length == 0){
				alert("There are no products for this filter criteria");
				return;
			}
			var selectionModel = flexExtGrid.getSelectionModel();
			var selectedItems = selectionModel.getSelection();	
			if (selectedItems.length == 0) {
				alert(youNeedToCheckAtLeastOneCheckboxMSG);
				return;
			}	
			for (var k = 0; k < selectedItems.length; k++) {
					if (flexExtGrid.getStore().data.contains(selectedItems[k])) {
						if((selectedItems[k].get('LCSSKUSeasonLink_REPEATED_ID') != null || selectedItems[k].get('LCSSKUSeasonLink_REPEATED_ID') != 'undefined') && (selectedItems[k].get('LCSProductCostSheet_REPEATED_ID') != null && selectedItems[k].get('LCSProductCostSheet_REPEATED_ID') != 'undefined')){
							selectedCostSheets.push(selectedItems[k].get('LCSSKUSeasonLink_REPEATED_ID').replace("LCSSKUSEASONLINK:","")+"|~*~|"+selectedItems[k].get('LCSProductCostSheet_REPEATED_ID').replace("LCSPRODUCTCOSTSHEET:",""));
						}
					}
			}
			if (selectedCostSheets.length > 0) {			
			var maxCountLimit='<%=maxSelectionLimit%>';
            if(selectedCostSheets.length>maxCountLimit){
				alert("You can select maximum of "+maxCountLimit+" rows only");
				return;
			}else{
			var seasonId = "";
			var params = "";
			var url = '<%=URL_CONTEXT%>/sparc/jsp/sourcing/SPARCDownloadCostSheetData.jsp'; 
			if(document.LINE_PLAN.seasonVersionId){
                seasonId = document.LINE_PLAN.seasonVersionId;
            }			
			var isAppColorways = true;			
			params = 'selectedIds=' + selectedCostSheets + '&seasonId=' +seasonId+'&division='+division+'&csType='+csType+'&appColors='+isAppColorways;
			runPostAjaxRequest(url, params, 'downloadCostSheetData');	
				}
			}
		}
}

function prodCostSheetDataDownload(division, csType){

  var selectedCostSheets = [];
		
		if (typeof Ext !== 'undefined'){
			var flexExtGrid = Ext.ComponentMgr.get('flexExtGrid');
			if (flexExtGrid === undefined) {
				flexExtGrid = Ext.ComponentMgr.get('lineSheetGrid');
			}
			if(flexExtGrid.getStore().data.items.length == 0){
				alert("There are no products for this filter criteria");
				return;
			}
			var selectionModel = flexExtGrid.getSelectionModel();
			var selectedItems = selectionModel.getSelection();	
			if (selectedItems.length == 0) {
				alert(youNeedToCheckAtLeastOneCheckboxMSG);
				return;
			}	
			for (var k = 0; k < selectedItems.length; k++) {
					if (flexExtGrid.getStore().data.contains(selectedItems[k])) {
						if((selectedItems[k].get('LCSProductSeasonLink_REPEATED_ID') != null || 
						selectedItems[k].get('LCSProductSeasonLink_REPEATED_ID') != 'undefined') 
						&& (selectedItems[k].get('LCSProductCostSheet_REPEATED_ID') != null && selectedItems[k].get('LCSProductCostSheet_REPEATED_ID') != 'undefined')){
						
							selectedCostSheets.push(selectedItems[k].get('LCSProductSeasonLink_REPEATED_ID').replace("LCSPRODUCTSEASONLINK:","")+"|~*~|"+selectedItems[k].get('LCSProductCostSheet_REPEATED_ID').replace("LCSPRODUCTCOSTSHEET:",""));
						}
						
			}
			}
			
			if (selectedCostSheets.length > 0) {			
			var maxCountLimit='<%=maxSelectionLimit%>';
            if(selectedCostSheets.length>maxCountLimit){
				alert("You can select maximum of "+maxCountLimit+" rows only");
				return;
			}else{
			var seasonId = "";
			var params = "";
			var url = '<%=URL_CONTEXT%>/sparc/jsp/sourcing/SPARCDownloadCostSheetData.jsp'; 
			if(document.LINE_PLAN.seasonVersionId){
                seasonId = document.LINE_PLAN.seasonVersionId;
            }
			var isAppColorways = false;
			params = 'selectedIds=' + selectedCostSheets + '&seasonId=' +seasonId+'&division='+division+'&csType='+csType+'&appColors='+isAppColorways;
			runPostAjaxRequest(url, params, 'downloadCostSheetData');	
				}
			}
		}
}
function downloadCostSheetData(xml, text){
	var downloadFileKey = "";
	downloadFileKey = xml.getElementsByTagName("downloadFileKey")[0].firstChild.data;
	window.open('Main.jsp?forwardedFileForDownload=true&forwardedFileForDownloadKey='+downloadFileKey);	
	}
	
function csDetailedBreakdownUpload(division){
	//Based on the division we will add the action later
	flexWindowOpenAsPost(urlContext + masterControllerURL + '?action=VIEW_UPLOAD_PAGE&oid=' + document.LINEPLAN_HEADER.seasonId +'&division='+division +'&activity=COSTSHEET_BREAKDOWN_UPLOAD_TEMPLATE&templateType=EMPTY&formType=csvLoadFile&importCSVFile=false', 'chooser', 'dependent=yes,width=600,height=400,top=50,left=300,titlebar=yes,scrollbars=1,resizable=yes');

	
}
function csHighLevelTemplateUpload(division){
	
	flexWindowOpenAsPost(urlContext + masterControllerURL + '?action=VIEW_UPLOAD_PAGE&oid=' + document.LINEPLAN_HEADER.seasonId + '&division='+division +'&activity=COSTSHEET_HIGHLEVEL_UPLOAD_TEMPLATE&templateType=EMPTY&formType=csvLoadFile&importCSVFile=false', 'chooser', 'dependent=yes,width=600,height=400,top=50,left=300,titlebar=yes,scrollbars=1,resizable=yes');
	
}

function prodCSHighLevelTemplateUpload(division){
	flexWindowOpenAsPost(urlContext + masterControllerURL + '?action=VIEW_UPLOAD_PAGE&oid=' + document.LINEPLAN_HEADER.seasonId + '&division='+division +'&activity=COSTSHEET_HIGHLEVEL_UPLOAD_TEMPLATE&templateType=EMPTY&formType=csvLoadFile&importCSVFile=false', 'chooser', 'dependent=yes,width=600,height=400,top=50,left=300,titlebar=yes,scrollbars=1,resizable=yes');	
}
	
</script>
<!-- changes for costing uploader end -->
<% if(FormatHelper.hasContent(infoMessage)){ %>
<tr>
    <td class="INFO">
        <%= FormatHelper.encodeForHTMLContent(java.net.URLDecoder.decode(infoMessage, defaultCharsetEncoding)) %>
    </td>
</tr>
<% } %>
