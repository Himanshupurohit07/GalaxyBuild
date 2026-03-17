<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.client.Activities,
                com.lcs.wc.client.web.*,
                com.lcs.wc.util.*,
                com.lcs.wc.db.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.season.*,
                com.lcs.wc.epmstruct.*,
                com.lcs.wc.changeAudit.ChangeTrackingPageHelper,
                com.lcs.wc.color.*,
                com.lcs.wc.product.*,
                com.lcs.wc.partstruct.*,      
                com.lcs.wc.specification.*,          
                wt.filter.NavigationCriteria,
                wt.filter.NavigationCriteriaHelper,
                wt.ownership.*,
                wt.util.*,
				org.apache.logging.log4j.Logger,
                org.apache.logging.log4j.LogManager,
                wt.locks.LockHelper,
                wt.org.*,
                java.text.*,
                java.util.*,
                wt.access.AccessPermission,
				java.io.File"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="flexg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>

<% lcsContext.setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%!
	private static final Logger logger = LogManager.getLogger("rfa.jsp.specification.ChooseSpecPages2");
	public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");
	public static final String BOM_IDENTIFIER = "BOM:  ";
	public static final String MEASUREMENTS_IDENTIFIER = "Measurements:  ";
    public static final String TRACKED_CHANGES_IDENTIFIER = "Tracked Changes:  ";
    public static final String CADDOC_VARIATIONS_IDENTIFIER = "CAD Document Variations:  ";
    public static final String PART_VARIATIONS_IDENTIFIER = "Part Variations:  ";
	public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override"); 
	public static final String WT_IMAGE_LOCATION = LCSProperties.get("flexPLM.windchill.ImageLocation");
    public static final String JS_REQUEST_VARIABLE = PageManager.getPageURL("JS_REQUEST_VARIABLE", null);

    public static final String SINGLE_BOM_PROP_ENTRY = "jsp.specification.specgen.ALL.useSingleBOM";
	public static boolean USE_SINGLEBOM = LCSProperties.getBoolean(SINGLE_BOM_PROP_ENTRY); 
    public static final String defaultUnitOfMeasure = LCSProperties.get(LCSProperties.DEFAULT_UNIT_OF_MEASURE, "si.Length.in");
	public static final String DEFAULT_CHILD_SPECS_CHECKED = LCSProperties.get("jsp.specification.ChooseSpecPage2.defaultChildSpecsChecked","false");
	public static final String DEFAULT_BOM_OWNER_VARIATIONS = LCSProperties.get("jsp.specification.TechPackGeneration.includeBOMOwnerVariations","false");
	public static final String DEFAULT_MEASUREMENTS_OWNER_VARIATIONS = LCSProperties.get("jsp.specification.TechPackGeneration.includeMeasurementsOwnerSizes","false");
    public static final String SPEC_REQUESTS = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.specRequests");
	
	public static String DEFAULT_DOCUMENT_VAULT_TYPE = 	LCSProperties.get("com.lcs.wc.document.documentVault.document.flexType");
	public static String DEFAULT_DOCUMENT_VAULT_REEBOKTYPE = LCSProperties.get("com.lcs.wc.document.documentVault.document.flexType");	
	public static String DEFAULT_DOCUMENT_VAULT_LUCKYTYPE = LCSProperties.get("com.lcs.wc.document.documentVault.document.luckyflexType");
	public static String DEFAULT_DOCUMENT_VAULT_F21TYPE = LCSProperties.get("com.lcs.wc.document.documentVault.document.f21flexType");
	
	public static String DEFAULT_DOCUMENT_VAULT_NAUTICATYPE = LCSProperties.get("com.lcs.wc.document.documentVault.document.nauticaflexType","Document\\Generated Tech Pack\\scGeneratedTechPackNautica");
	public static String DEFAULT_DOCUMENT_VAULT_AEROTYPE = LCSProperties.get("com.lcs.wc.document.documentVault.document.aeroflexType","Document\\Generated Tech Pack\\scGeneratedTechPackAeropostale");
	

	public static final boolean DEFAULT_ASYNC_MULTI_TPGENERATION = LCSProperties.getBoolean("com.lcs.wc.product.MultiTPGenAsyncService.defaultSelected");
	//add for story B-58112 03/30/2011
    public static final boolean INCLUDED_TRACKED_CHANGES = LCSProperties.getBoolean("jsp.specification.ChooseSingleSpecPage2.includedTrackedChanges");
    public static final boolean INCLUDED_CADDOC_VARIATIONS = LCSProperties.getBoolean("jsp.specification.ChooseSingleSpecPage2.includedCadDocVariations");
    public static final boolean REVISE_DOCUMENT = LCSProperties.getBoolean("com.lcs.wc.document.LCSDocument.revise");
    public static final String CLEAR_CELL_VALUE = LCSProperties.get("com.lcs.wc.LCSLogic.ClearCellValue", "!CLR");

    public static String baseUOM = "";
    public static String instance = "";
    public static final boolean CAD_DATA_ENABLED = LCSProperties.getBoolean("com.lcs.wc.specification.cadData.Enabled");
    public static final boolean PART_DATA_ENABLED = LCSProperties.getBoolean("com.lcs.wc.specification.parts.Enabled");
    public static final Collection<String> UOMs = new ArrayList<String>();
    static {
        try {
            instance = wt.util.WTProperties.getLocalProperties().getProperty ("wt.federation.ie.VMName");
            Map<?,?> allUOMS = UomConversionCache.getAllUomKeys();
            HashMap<?,?> inputUnit = (HashMap<?,?>)allUOMS.get(defaultUnitOfMeasure);
            baseUOM = (String)inputUnit.get("prompt");
        } catch(Exception e){
            e.printStackTrace();
        }
	UOMs.add("si.Length.in");
	UOMs.add("si.Length.cm");
	UOMs.add("si.Length.mm");
	UOMs.add("si.Length.ft");
	UOMs.add("si.Length.m");
    }
	public ArrayList<String> parseString(String str, String delim){
		ArrayList<String> list = new ArrayList<String>();
		StringTokenizer p = new StringTokenizer(str,delim);
		while(p.hasMoreTokens()){
			list.add(p.nextToken().trim());
		}
		
		return list;
	}

%>
<jsp:include page="<%=subURLFolder+ JS_REQUEST_VARIABLE %>" flush="true" >
	<jsp:param name="none" value="true" />
</jsp:include>

<%

   String chooserGroupTitle    = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "specComponents_LBL", RB.objA ) ;
   //String pageLabel            = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "page_LBL", RB.objA ) ;
   String availableComponentsLabel            = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "availableComponents_LBL", RB.objA ) ;
   String selectButton         = WTMessage.getLocalizedMessage ( RB.MAIN, "select_Btn", RB.objA ) ;
   String cancelButton         = WTMessage.getLocalizedMessage ( RB.MAIN, "cancel_Btn", RB.objA ) ;

   String pageTitle            = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "select_pages_LBL", RB.objA ) ;
   String variationOptionsGrpTle = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "variationOptions_GRP_TLE", RB.objA ) ;
   String colorwaysLabel	   = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "colorwaysLabel", RB.objA ) ;
   String sizesLabel		   = WTMessage.getLocalizedMessage ( RB.MEASUREMENTS, "sizes_LBL", RB.objA ) ;
   String destinationsLabel   = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "destinations_LBL", RB.objA ) ;
   String allLabel			   = WTMessage.getLocalizedMessage (RB.MAIN, "all_LBL", RB.objA ) ;
   String specComponentsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "specComponents_LBL", RB.objA ) ;
   String includeChildSpecifications = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "includeChildSpecifications_LBL", RB.objA ) ;
   String includeBOMOwnerVariations = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "includeBOMOwnerVariations_LBL", RB.objA ) ;
   String includeMeasurementsOwnerSizes = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "includeMeasurementsOwnerSizes_LBL", RB.objA ) ;
   String moveUpLabel		   = WTMessage.getLocalizedMessage (RB.MAIN, "moveUp_LBL", RB.objA ) ;
   String moveDownLabel	   = WTMessage.getLocalizedMessage (RB.MAIN, "moveDown_LBL", RB.objA ) ;
   String pageOptionsGrpTle	   = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "pageOptionsAndReports_GRP_TLE", RB.objA ) ;
   String pageOptionsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "pageOptions_LBL", RB.objA ) ;
   //String bomLabel			   = "BOM:  ";
   //String measurementsLabel   =  "Measurements:  ";
   String specRequestLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "specRequest", RB.objA ) ;
   String numberColorwaysPerPageLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "numColorwaysPerPage_LBL", RB.objA ) ;
   String numberSizesPerPageLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "numSizesPerPage_LBL", RB.objA ) ;
   //String reportsLabel		   = WTMessage.getLocalizedMessage (RB.SPECIFICATION, "pageOptionsAndReports_GRP_TLE", RB.objA ) ;
   String availableReportsLabel		   = WTMessage.getLocalizedMessage (RB.SPECIFICATION, "availableReports_LBL", RB.objA ) ;

   String viewOptionsLabel	   = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "viewOptions_LBL", RB.objA ) ;
   String showColorSwatchesLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "showColorSwatches_LBL", RB.objA ) ;
   String showMaterialThumbnailsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "showMaterialThumbnails_LBL", RB.objA ) ;
    //String useSize1Size2Label = "Use Size 1 or Size2 values";

   String headerLabel           = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "headers_LBL", RB.objA ) ;
   String bomComponentLabel     = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "bomComponent_LBL", RB.objA ) ;
   String measComponentLabel    = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "measComponent_LBL", RB.objA ) ;
   String consComponentLabel    = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "consComponent_LBL", RB.objA ) ;
   String impComponentLabel     = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "impComponent_LBL", RB.objA ) ;
   String fractionLabel = WTMessage.getLocalizedMessage ( RB.MEASUREMENTS, "fraction_LBL",RB.objA ) ;
   String measurementUOMOverrideLabel = WTMessage.getLocalizedMessage ( RB.MEASUREMENTS, "measurementUOMOverride_LBL",RB.objA ) ;
   String vaultDocumentsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "vaultDocuments_LBL", RB.objA ) ;
   String vaultDocumentTypeLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "vaultDocumentType_LBL", RB.objA ) ;
   String includeSecondaryLabel = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "includeSecondary_LBL", RB.objA );
   String allAvailableDocumentsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "allAvailDocs_LBL", RB.objA ) ;
   String includeMarkedupImagesLabel = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "includeMarkedupImagesLabel_LBL", RB.objA );
   String availableDocumentsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "availDocs_LBL", RB.objA ) ;
   String asynchGenLabel = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "asynchGen_LBL", RB.objA );
   FlexType defaultDocumentVaultType = FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE);
   //add for story B-58112 03/30/2011
   String trackedChangesLabel = WTMessage.getLocalizedMessage ( RB.CHANGE, "trackedChanges_LBL", RB.objA ) ;
   String showChangeSinceLabel = WTMessage.getLocalizedMessage( RB.EVENTS, "showChangeSince_lbl", RB.objA);
   String expandedTrackedChangesLabel = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "expandedTrackedChanges_LBL", RB.objA );
   String condensedTrackedChangesLabel = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "condensedTrackedChanges_LBL", RB.objA );
   String cadDocVariationsLabel = WTMessage.getLocalizedMessage( RB.EPMSTRUCT, "cadDocVariations_LBL", RB.objA );
   String partVariationsLabel = WTMessage.getLocalizedMessage( RB.PARTSTRUCT, "partVariations_LBL", RB.objA );
   String includeCADDocsLabel = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "includeCADDocs_LBL", RB.objA );
   String cadDocFilterLabel     = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "cadDocFilter_LBL", RB.objA );
   // story B-100680
   String includeAllPartsLabel = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "includeAllParts_LBL", RB.objA );
   String exportedIndentedBOMLabel = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "exportedIndentedBOM_LBL", RB.objA );
   String partFilterLabel = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "partFilter_LBL", RB.objA );
   String partDataOptionsLabel = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "partDataOptions_LBL", RB.objA );

   Vector<String> daysOrder = ChangeTrackingPageHelper.getChangeEventDaysDisplayOrders(lcsContext.getLocale()); 
   Map<String,String> daysDisplayMap = ChangeTrackingPageHelper.getChangeEventDaysDisplayMap(lcsContext.getLocale());
   String showChangeSinceDefault = ChangeTrackingPageHelper.getChangeEventDaysDefault();
   Map<String,String> daysMap = ChangeTrackingPageHelper.getChangeEventDaysMap(lcsContext.getLocale());
   String jsCalendarFormat = WTMessage.getLocalizedMessage( RB.DATETIMEFORMAT, "jsCalendarFormat", RB.objA);
   RequestParamHolder daysHolder = new RequestParamHolder(daysMap);
   
    boolean ajaxWindow = FormatHelper.parseBoolean(request.getParameter("ajaxWindow"));

    String selectFunction = "ChooseSpecPages2.selectSpecificationPages()";
    
    String ajaxReturnCall = request.getParameter("ajaxReturnCall");

    
    
    flexg.setModuleName("SEASON");
    String seasonId = request.getParameter("seasonId");
    //logger.debug("seasonId: " + seasonId);
    FlexType type = null;
    LCSSeason season = null;
    if(FormatHelper.hasContent(seasonId)){
    	season = (LCSSeason)LCSSeasonQuery.findObjectById(seasonId);
        type = season.getFlexType();
    }
	
	if(season.getProductType().getFullNameDisplay().contains("Lucky")){
		System.out.println("Inside LineSheet Lucky");
		DEFAULT_DOCUMENT_VAULT_TYPE = DEFAULT_DOCUMENT_VAULT_LUCKYTYPE;	
		defaultDocumentVaultType = FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE);
	}
	else if(season.getProductType().getFullNameDisplay().contains("Forever 21")){
		System.out.println("Inside LineSheet F21");
		DEFAULT_DOCUMENT_VAULT_TYPE = DEFAULT_DOCUMENT_VAULT_F21TYPE;
		defaultDocumentVaultType = FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE);
	}
	else if(season.getProductType().getFullNameDisplay().contains("Aeropostale")){
	  DEFAULT_DOCUMENT_VAULT_TYPE = DEFAULT_DOCUMENT_VAULT_AEROTYPE;
	  defaultDocumentVaultType  = FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE);
	} else if(season.getProductType().getFullNameDisplay().contains("Nautica")){
	  DEFAULT_DOCUMENT_VAULT_TYPE = DEFAULT_DOCUMENT_VAULT_NAUTICATYPE;
	  defaultDocumentVaultType  = FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE);
	}
	else{		
		DEFAULT_DOCUMENT_VAULT_TYPE = DEFAULT_DOCUMENT_VAULT_REEBOKTYPE;
		defaultDocumentVaultType = FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE);
	}
	
	


    String errorMessage = request.getParameter("errorMessage");


   ArrayList<String> specRequests = new ArrayList<String>();
   if(FormatHelper.hasContent(SPEC_REQUESTS)) {
	  specRequests = parseString(SPEC_REQUESTS, ",");
   }
   Map<?,?> specRequestsMap = FormatHelper.toMap(specRequests, RB.SPECIFICATION);
   System.out.println("-----specRequestsMap-----"+specRequestsMap);

%>
<%
	//START - Footwear Product Tech Pack Implementation 
	boolean isRBKProductFootwear = false;
	if(season.getProductType().getFullNameDisplay().toString().equals("Footwear\\Reebok")){
		isRBKProductFootwear = true;
	}
	//END - Footwear Product Tech Pack Implementation
    //Set up page options (which components will be printed)
    HashMap<String,String> compMap = new HashMap<String,String>();
    
    Vector<String> sortVec = new Vector<String>();

    //compMap.put("HEADER", headerLabel);
    //sortVec.add("HEADER");
	
	compMap.put("Header Page", "Header Page");
    sortVec.add("Header Page");
	compMap.put("Colorways", "Colorways");
    sortVec.add("Colorways");
    
    //compMap.put("CONSTRUCTION", consComponentLabel);
    //sortVec.add("CONSTRUCTION");
  	//START - Footwear Product Tech Pack Implementation 
  	if(!isRBKProductFootwear){
    	compMap.put("MEASUREMENT", measComponentLabel);
  	}

    FlexType docType = FlexTypeCache.getFlexTypeFromPath("Document\\Images Page");
    //AttributeValueList pageTypes = docType.getAttribute("scDocumentType").getAttValueList();
	AttributeValueList pageTypes = docType.getAttribute("pageType").getAttValueList();

    Iterator<?> selectableKeys = pageTypes.getSelectableKeys(lcsContext.getLocale(), true).iterator();
    String key = null;
    String display = null;
    while(selectableKeys.hasNext()){
        key = (String)selectableKeys.next();
        display = pageTypes.getValue(key, lcsContext.getLocale());

        compMap.put("IMAGES_PAGE:" + key, impComponentLabel + "-" + display);
        sortVec.add("IMAGES_PAGE:" + key);
    }

    HashMap<String,String> docMap = new HashMap<String,String>();
    FlexType flexType = FlexTypeCache.getFlexTypeFromPath("Document");
    Collection<String> flexTypes = FlexTypeCache.getConstantBasedFlexTypeCollection(flexType, com.lcs.wc.document.LCSDocumentQuery.TECH_PACKABLE);
    for (String fType : flexTypes) {
    	flexType = FlexTypeCache.getFlexType(fType);
    	if ( ! ACLHelper.hasAccess(flexType, AccessPermission.READ)){
    		continue;
    	}
        display = flexType.getFullNameDisplay(true);
        docMap.put(flexType.getFullName(true), display);
    }
	compMap.put("BOM", bomComponentLabel);
	compMap.put("SAMPLE", "Sample");
    sortVec.add("BOM");
    sortVec.add("MEASUREMENT");
	sortVec.add("SAMPLE");

    Collection<?> availablePages = new ClassLoadUtil(FileLocation.productSpecProperties).getKeyList();

       //Set up the report options
    Vector bomOptions = new Vector(new ClassLoadUtil(FileLocation.productSpecBOMProperties2).getKeyList());
	String pdfExportingFrom = request.getParameter("pdfExportingFrom");
	String exportingFromProp = LCSProperties.get(SINGLE_BOM_PROP_ENTRY.replace(".ALL.", "." + pdfExportingFrom + "."));
	if(FormatHelper.hasContent(exportingFromProp)){
		if(!FormatHelper.parseBoolean(exportingFromProp) && bomOptions.contains("singleBOM")){
            bomOptions.remove("singleBOM");
		}
			
	} else if(!USE_SINGLEBOM){
        if(bomOptions.contains("singleBOM")){
            bomOptions.remove("singleBOM");
        }
     }

     Collections.sort(bomOptions);
     Vector measurementOptions = new Vector(new ClassLoadUtil(FileLocation.productSpecMeasureProperties2).getKeyList());
     Collections.sort(measurementOptions);

     Map bomOptionsMap = FormatHelper.toMap(bomOptions, RB.FLEXBOM);
     Map measurementOptionsMap = FormatHelper.toMap(measurementOptions, RB.MEASUREMENTS);
measurementOptionsMap.put("gradeRulesIncrementReport","Measurement Set: Grades Rules Increment");
     Map<String,Object> allOptions = new HashMap<String,Object>();
     //Collection measurementKeys = measurementOptionsMap.keySet();
     Iterator<?> measKeysItr = measurementOptions.iterator();
     //Collection bomKeys = bomOptionsMap.keySet();
     Iterator<?> bomKeyItr = bomOptions.iterator();

     String dkey = null;
     while(measKeysItr.hasNext()) {
         dkey = (String)measKeysItr.next();
         allOptions.put(MEASUREMENTS_IDENTIFIER + dkey, measurementOptionsMap.get(dkey));
     }

     while(bomKeyItr.hasNext())
     {
         dkey = (String)bomKeyItr.next();
         allOptions.put(BOM_IDENTIFIER + dkey, bomOptionsMap.get(dkey));
    }
	
	
	   //Hypercare-150, TASK-9256 changes for Sample Details Report - start
		WTProperties wtproperties = null;
		String wthome = "";
		wtproperties = WTProperties.getLocalProperties();
		wthome = wtproperties.getProperty("wt.home");
		Vector sampleOptions = new Vector(new ClassLoadUtil(wthome+File.separator+"codebase"+File.separator+"ProductSpecificationSAMPLE2.properties").getKeyList());
		Collections.sort(sampleOptions);
		Map<String,String> sampleOptionsMap = FormatHelper.toMap(sampleOptions, RB.SAMPLES);
		//changes for RM Task# 10671 - start
		//sampleOptionsMap.put("sampleDetails","Sample: Sample Details");
		sampleOptionsMap.put("sampleRequestComments","Sample: Sample Request Comments");
		sampleOptionsMap.put("sampleFitPhotoCorrectionsImagePage","Sample: Sample Fit Photo/Corrections");
		sampleOptionsMap.put("sampleRequestFitDetails","Sample: Sample Request Fit Details");
		//changes for RM Task# 10671 - end
		Iterator<?> sampleKeyItr = sampleOptions.iterator();
	    while(sampleKeyItr.hasNext()){
	         dkey = (String)sampleKeyItr.next();
	         allOptions.put("Sample: " + dkey, sampleOptionsMap.get(dkey));
	    }
		//Hypercare-150, TASK-9256 changes for Sample Details Report - end
 
     //add for story B-58112 03/30/2011
     if (!ajaxWindow &&INCLUDED_TRACKED_CHANGES) {
     	  allOptions.put(TRACKED_CHANGES_IDENTIFIER + ChangeTrackingPDFGenerator.EXPANDED_REPORT, expandedTrackedChangesLabel);
      	  allOptions.put(TRACKED_CHANGES_IDENTIFIER + ChangeTrackingPDFGenerator.CONDENSED_REPORT, condensedTrackedChangesLabel);
     }
     if (CAD_DATA_ENABLED) {
          allOptions.put(CADDOC_VARIATIONS_IDENTIFIER + PDFCadDocVariationsGenerator.VARIATIONS_REPORT, cadDocVariationsLabel);
    }
     if (PART_DATA_ENABLED) {
     	  allOptions.put(PART_VARIATIONS_IDENTIFIER + PDFPartVariationsGenerator.VARIATIONS_REPORT, partVariationsLabel);
     }

	Map<String,String> formats = new HashMap<String,String>();
	formats.put("none", " ");
	formats.put(FormatHelper.FRACTION_FORMAT, fractionLabel + " " + baseUOM);
	formats.putAll(FormatHelper.toMap(UOMs, RB.UOMRENDERER));
	
	List<String> filterList = new ArrayList<String>();
	filterList.add("wt.part.WTPart");
	Map<String,String> savePartFilters = new FlexSpecUtil().getSaveFiltersForTechPack(filterList);
	String defaultFilterName = new FlexSpecUtil().getDefaultFilterName(filterList.size() > 0 ? filterList.get(0) : null);


%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JSP METHODS /////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%!
    public static final String JSPNAME = "ChooseSpecPages";
    
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JAVSCRIPT ///////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/specPrinter.js"></script>

<script>
var CLEAR_CELL_VALUE = '<%= CLEAR_CELL_VALUE%>';
var SOURCE = "SOURCE";
var SPEC_PAGES = "SPEC_PAGES";
var AVAIL_DOCS = 'AVAIL_DOCS';
var COLORWAYS_DATA = "COLORWAYS_DATA";
var SIZE1_DATA = "SIZE1_DATA";
var SIZE2_DATA = "SIZE2_DATA";
var DESTINATION_DATA = "DESTINATION_DATA";
var PAGE_OPTIONS = "PAGE_OPTIONS";
var SELECTED_VIEWS = "SELECTED_VIEWS";
var COLORWAYS_PER_PAGE = "COLORWAYS_PER_PAGE";
var SIZES_PER_PAGE = "SIZES_PER_PAGE";
var USE_SIZE1_SIZE2 = "USE_SIZE1_SIZE2";
var SHOW_COLOR_SWATCHES = "SHOW_COLOR_SWATCHES";
var SHOW_MATERIAL_THUMBNAIL = "SHOW_MATERIAL_THUMBNAIL";
var UOM = "UOM";
var INCLUDE_CHILD_SPECS = "INCLUDE_CHILD_SPECS";
var INCLUDE_BOM_OWNER_VARIATIONS = "INCLUDE_BOM_OWNER_VARIATIONS";
var INCLUDE_MEASUREMENTS_OWNER_SIZES = "INCLUDE_MEASUREMENTS_OWNER_SIZES";
var LANDSCAPE = "LANDSCAPE";
var DOCUMENTVAULT = "DOCUMENTVAULT";
var VAULTDOCUMENTTYPEID = "VAULTDOCUMENTTYPEID";
var ASYNCH_GENERATION = "ASYNCH_GENERATION";
var INCLUDE_CAD_DOCUMENTS = "INCLUDE_CAD_DOCUMENTS";
var SPEC_CAD_FILTER = "SPEC_CAD_FILTER"; 
var INCLUDE_PARTS = "INCLUDE_PARTS";

var title_Holder = document.title;
document.title = '<%=FormatHelper.encodeForJavascript(pageTitle)%>';
ChooseSpecPages2 = function(){
	return {

   selectSpecificationPages : function(){
		document.title = title_Holder;

        document.getElementById("oid").value = document.REQUEST.oid;
        document.getElementById("activity").value = 'FIND_SPECIFICATION';
        document.getElementById("action").value = 'INIT';
        
        var params = this.prepParams();
        //for(var n in params){
        //    alert(n + ':' + params[n]);
        //}
        if(document.getElementById("pageOptions").value.indexOf('<%=PDFCadDocVariationsGenerator.VARIATIONS_REPORT%>') > -1 
				&& !document.getElementById("includeCADDocumentsbox").checked){
		    alert("<%= FormatHelper.encodeForJavascript(WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "cadsNotSelected_LBL", RB.objA )) %>");
		    return;
		} 	
		if(document.getElementById("pageOptions").value.indexOf('<%=PDFPartVariationsGenerator.VARIATIONS_REPORT%>') > -1 
				 && !document.getElementById("includeAllPartsbox").checked){
		    alert("<%= FormatHelper.encodeForJavascript(WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "partsNotSelected_LBL", RB.objA )) %>");
		    return;
		}
        <%if(ajaxWindow){%>
		<%= FormatHelper.encodeForHTMLContent(ajaxReturnCall)%>
        <%}else{%>
        opener.updateSpecParams(params);
        submitForm();
        <%}%>
   },

    prepParams : function(){
        var params = {};

        //Spec Pages
        var specPagesVal = document.getElementById("specPages").value;
        params[SPEC_PAGES] = specPagesVal;

        //Available Docs
        var availDocsVal = document.getElementById("availDocs").value;
        params[AVAIL_DOCS] = availDocsVal;

        //Page Options
        var pageOptionsVal = document.getElementById("pageOptions").value;
        params[PAGE_OPTIONS] = pageOptionsVal;

        //Selected Views
        //document.MAINFORM.viewSelectedVal.value = createMOAViewSelect();
        //var viewSelectVal = document.MAINFORM.viewSelectedVal.value;
        //params[SELECTED_VIEWS] = viewSelectVal;

        //Colorways Per Page
        var numColorwaysPerPageVal = document.getElementById("numColorwaysPerPageInput").value
        params[COLORWAYS_PER_PAGE] = numColorwaysPerPageVal;

        //Sizes Per Page
        var numSizesPerPageVal = document.getElementById("numSizesPerPageInput").value;
        params[SIZES_PER_PAGE] = numSizesPerPageVal;


        //Use Size1/Size2
        params[USE_SIZE1_SIZE2] = document.getElementById("useSize1Size2").value;

        //Show Swatches
        params[SHOW_COLOR_SWATCHES] = document.getElementById("showColorSwatches").value;

        //Show Material Thumbnails
        params[SHOW_MATERIAL_THUMBNAIL] = document.getElementById("showMaterialThumbnail").value;

	    params[UOM] = document.getElementById("uom").value;

        params[INCLUDE_CHILD_SPECS] = document.getElementById("includeChildSpecs").value
        params[INCLUDE_BOM_OWNER_VARIATIONS] = document.getElementById("includeBOMOwnerVariations").value
        params[INCLUDE_MEASUREMENTS_OWNER_SIZES] = document.getElementById("includeMeasurementsOwnerSizes").value
        params[INCLUDE_MARKEDUP_IMAGES_CONTENT] = document.getElementById("includeMarkedupImagesContentbox").checked;
        params[INCLUDE_SECONDARY_CONTENT] = document.getElementById("includeSecondaryContentbox").checked;
        params[INCLUDE_CAD_DOCUMENTS] = document.getElementById("includeCADDocumentsbox").checked;
        params[LANDSCAPE] = document.getElementById("useLandscape").value
        //add for story B-58112 03/30/2011
    	var sinceDate = getSelectedDate();
        document.getElementById("showChangeSince").value = formatDateString(sinceDate, '%Y/%m/%d');
        params[SHOW_CHANGE_SINCE] = document.getElementById("showChangeSince").value;
        <%-- B-92545
        params[SPEC_CAD_FILTER]=document.getElementById("cadDocFilter").value;
        --%>
    	params[SPEC_PARTS]= "";
    	params[SPEC_PART_FILTER]=document.getElementById("partFilter").value;
    	params[SHOW_INDENTED_BOM]=document.getElementById("exportedIndentedBOM").value;

    	params[INCLUDE_ALL_PARTS]=document.getElementById("includeAllPartsbox").checked;
    	
		 <%if (ACLHelper.hasViewAccess(defaultDocumentVaultType)&&ACLHelper.hasCreateAccess(defaultDocumentVaultType)){ %>
	   	 	params[DOCUMENTVAULT] = document.getElementById("documentVault").value;

		   	params[VAULTDOCUMENTTYPEID] = document.getElementById("vaultDocumentTypeId").value;
		   	
		   	params[ASYNCH_GENERATION] = document.getElementById("asynchronousGeneration").value;
	 <%}%>

		return params;

    },
    //add for story B-58112 04/02/2011
    updatePage : function() {
    	var pageOptionsVar = document.getElementById("pageOptionsOptions");
    	var pageOptionsChosenVar = document.getElementById("pageOptionsChosen");
    	
    	// Check to make sure only the tracked changes selected have their views displayed
    	var includedTrackedChanges = false;
 		var includedPartData = false;
    	
    	for(var i = 0; i < pageOptionsVar.length; i++){
    		includedTrackedChanges = false;
    		if(pageOptionsChosenVar.length > 0){
    			for (var j = 0; j < pageOptionsChosenVar.length; j++){
    				if (pageOptionsChosenVar.options[j].value == '<%=TRACKED_CHANGES_IDENTIFIER + ChangeTrackingPDFGenerator.EXPANDED_REPORT%>' || 
    				        pageOptionsChosenVar.options[j].value == '<%=TRACKED_CHANGES_IDENTIFIER + ChangeTrackingPDFGenerator.CONDENSED_REPORT%>') {
    					includedTrackedChanges = true;
    					continue;
    				}
    				if(pageOptionsChosenVar.options[j].value == '<%=PART_VARIATIONS_IDENTIFIER + PDFPartVariationsGenerator.VARIATIONS_REPORT%>'){
    					   includedPartData =true;
    					   continue;
   					}
  					if(includedPartData&&includedTrackedChanges) break;
    			}
    		}
    		if (includedTrackedChanges) {
    			document.getElementById("divTrackedChanges").style.display='block';
    		} else {
    			document.getElementById("divTrackedChanges").style.display='none';
    		}
   		 	if (includedPartData) {
			 	document.getElementById("divPartData").style.display='block';
				document.getElementById("partFilter").disabled = true;
			} else {
				document.getElementById("divPartData").style.display='none';
			}
    	}
    },
    
    changeSinceDate : function() {
        var choice = $F("daysValueId").strip();
    	if(isNaN(choice)) {
    	    $('sinceDateCal').style.display = 'inline-block';
    			$("dateTimeDisplay").style.display='none';
    	} else {
    		$('sinceDateCal').style.display = 'none';
    		var sinceDate = new Date();
    		sinceDate.setDate(sinceDate.getDate()-parseInt(choice));
    		showDateTimeRightToWidget(sinceDate);
    	}
    },    
    
    

    chooseView : function(view){
//Clear values
    document.getElementById("specPages").value = ' ';
    document.getElementById("pageOptions").value = ' ';
    document.getElementById('availDocs').value = ' ';

    var specPagesChosen = document.getElementById("specPagesChosen");
    var pageOptionsChosen = document.getElementById("pageOptionsChosen");
    var availDocsChosen = document.getElementById("availDocsChosen");
    var availablePartsChosen = document.getElementById("availPartsChosen");

    for(var i = 0; i < specPagesChosen.options.length; i++){
        specPagesChosen.remove(i--);
    }

    for(var i = 0; i < pageOptionsChosen.options.length; i++) {
         pageOptionsChosen.remove(i--);
    }

    for(var i = 0; i < availDocsChosen.options.length; i++) {
         availDocsChosen.remove(i--);
    }
    
    if (availablePartsChosen && availablePartsChosen.options) {
        for(i = 0; i < availablePartsChosen.options.length; i++){
        	availablePartsChosen.remove(i--);
        }
    }

    document.getElementById('numColorwaysPerPageInput').value = '';

    document.getElementById('numSizesPerPageInput').value = '';

    document.getElementById('showColorSwatchesbox').checked = false;
    document.getElementById('showColorSwatches').value = 'false';

    document.getElementById('showMaterialThumbnailbox').checked = false;
    document.getElementById('showMaterialThumbnail').value = 'false';

    document.getElementById('includeChildSpecsbox').checked = <%=DEFAULT_CHILD_SPECS_CHECKED%>;
    
    document.getElementById('includeBOMOwnerVariations').checked = <%=DEFAULT_BOM_OWNER_VARIATIONS%>;
    
    document.getElementById('includeMeasurementsOwnerSizes').checked = <%=DEFAULT_MEASUREMENTS_OWNER_VARIATIONS%>;
    
    document.getElementById('includeSecondaryContentbox').checked = false;

    document.getElementById('includeMarkedupImagesContentbox').checked = false;

    document.getElementById('uom').value = 'none';
//End clearing of values
<% 
	String defView = null;
	
	String compPagesStr = null;
	Collection<?> compPages = null;
	Iterator<?> cpi = null;
	String compPage = "";
	
	
	
	Iterator<?> viewIter = specRequests.iterator();
	boolean first = true;
	while(viewIter.hasNext()){
		defView = (String)viewIter.next();
%>
	if (view == " ") {
		ChooseSpecPages2.updatePage();
	}
		<% if(first){ %>
			if(view == '<%=defView%>'){
		<%} else {%>
			} else if(view == '<%=defView%>'){
		<% } %>

<%
               String ncpp = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.numColorwaysPerPage." + defView, "");
			   if(!FormatHelper.hasContent(ncpp)){
				  ncpp = " ";
			   }
               String nspp = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.numSizesPerPage." + defView, "");
			   if(!FormatHelper.hasContent(nspp)){
				  nspp = " " ;
			   }
               String showColorSwatch = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.showColorSwatch." + defView, "");
               if(!FormatHelper.hasContent(showColorSwatch)){
                showColorSwatch = "false";
               }
               String showMaterialThumb = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.showMaterialThumb." + defView, "");
               if(!FormatHelper.hasContent(showMaterialThumb)){
                showMaterialThumb = "false";
               }
               String includeChildSpecs = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.includeChildSpecs." + defView, "");
               if(!FormatHelper.hasContent(includeChildSpecs)){
                includeChildSpecs = "false";
               }
               String includeMarkedupImages = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.includeMarkedupImagesContent." + defView, "");
               if(!FormatHelper.hasContent(includeMarkedupImages)){
                   includeMarkedupImages = "false";
               }
               String includeSecondary = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.includeSecondaryContent." + defView, "");
               if(!FormatHelper.hasContent(includeSecondary)){
                   includeSecondary = "false";
               }
               String MeasureUOM = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.MeasureUOM." + defView, "");
               if(!FormatHelper.hasContent(MeasureUOM)){
                MeasureUOM = "none";
               }
              
%>

               document.getElementById('numColorwaysPerPageInput').value = '<%=ncpp%>';

               document.getElementById('numSizesPerPageInput').value = '<%=nspp%>';

               document.getElementById('showColorSwatchesbox').checked = <%=showColorSwatch%>;
               document.getElementById('showColorSwatches').value = '<%=showColorSwatch%>';

               document.getElementById('showMaterialThumbnailbox').checked = <%=showMaterialThumb%>;
               document.getElementById('showMaterialThumbnail').value = '<%=showMaterialThumb%>';

               document.getElementById('includeChildSpecsbox').checked = <%=includeChildSpecs%>;
               document.getElementById('includeChildSpecs').value = '<%=includeChildSpecs%>';
               document.getElementById('includeMarkedupImagesContentbox').checked = <%=includeMarkedupImages%>;
               document.getElementById('includeSecondaryContentbox').checked = <%=includeSecondary%>;
               document.getElementById('uom').value = '<%=MeasureUOM%>';
<% 
		compPagesStr = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.SpecPages." + defView);
		if(FormatHelper.hasContent(compPagesStr)){
			compPages = parseString(compPagesStr, ",");
			cpi = compPages.iterator();
			while(cpi.hasNext()){
				compPage = (String)cpi.next();
%>
			
				var comps = compMap['<%= compPage %>'];
				addToChosenList(comps, document.getElementById("specPagesOptions"), document.getElementById("specPagesChosen"), document.getElementById("specPages"));
<%
			}// end while
		
		} // end if
				
		compPagesStr = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.availDocs." + defView);
		if(FormatHelper.hasContent(compPagesStr)){
			compPages = parseString(compPagesStr, ",");
			cpi = compPages.iterator();
			while(cpi.hasNext()){
				compPage = (String)cpi.next();
%>
                var comps = '<%= FormatHelper.encodeForJavascript(compPage) %>';
 				addToChosenList1(comps, document.getElementById("availDocsOptions"), document.getElementById("availDocsChosen"), document.getElementById("availDocs"));
<%
			}// end while

		} // end if

                   //Add BOM Reports section here
%>

        var reps = bomReports['<%=defView%>'];

        addToChosenList(reps, document.getElementById("pageOptionsOptions"), document.getElementById("pageOptionsChosen"), document.getElementById("pageOptions"));
<%
		if(!viewIter.hasNext()){
%>
		}
<%
		} // end if
        first = false;
    }//end while
%>

}, //end chooseView
chooseVaultDocumentType : function(){
    typeClass = 'com.lcs.wc.document.LCSDocument';
    rootTypeId = '<%=FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE))%>';
    launchChooser('<%=URL_CONTEXT%>/jsp/flextype/TypeChooser.jsp?typeclass=' + typeClass + '&rootTypeId='+ rootTypeId + '&accessType=CREATE_ACCESS', fkTypeDisplay, document.getElementById("vaultDocumentTypeId"));

}

}
}();

function addToChosenList1(comps, selectOptions, selectChosen, chosenElem){
    var sOptions = selectOptions.options;
    var sOption;
    for(var i = 0; i < sOptions.length; i++){
        sOption = sOptions[i];
        //alert(n + ' : ' + sOption.value);
        if(sOption.value == comps){
            addOption(selectChosen, sOption, true, false);
            ids = buildMOA(chosenElem.value, sOption.value);
            chosenElem.value = ids;
            break;
        }
    }
}

function addToChosenList(comps, selectOptions, selectChosen, chosenElem){
	for(var n in comps){
		//alert(n);
		var sOptions = selectOptions.options;
		var sOption;
		for(var i = 0; i < sOptions.length; i++){
			sOption = sOptions[i];
			//alert(n + ' : ' + sOption.value);
			if(sOption.value.toLowerCase() ==n.toLowerCase()){
				addOption(selectChosen, sOption, true, false);
				ids = buildMOA(chosenElem.value, sOption.value);
				chosenElem.value = ids;
                break;

			}
		}


	}
	ChooseSpecPages2.updatePage();
}


		var compMap = new Array();
        var docMap = new Array();
		var ctMap;
<%
	Collection<String> comptypes  = compMap.keySet();

	String compType = null;

    if(comptypes.contains("BOM")){
%>
        ctMap = new Array();
        ctMap['BOM'] = 'BOM';
        compMap['BOM'] = ctMap;
<% }
if(comptypes.contains("SAMPLE")){
%>
        ctMap = new Array();
        ctMap['SAMPLE'] = 'SAMPLE';
        compMap['SAMPLE'] = ctMap;
<% }

if(comptypes.contains("Header Page")){
%>
        ctMap = new Array();
        ctMap['Header Page'] = 'Header Page';
        compMap['Header Page'] = ctMap;
<% }

if(comptypes.contains("Colorways")){
%>
        ctMap = new Array();
        ctMap['Colorways'] = 'Colorways';
        compMap['Colorways'] = ctMap;
<% }

    if(comptypes.contains("CONSTRUCTION")){
%>
        ctMap = new Array();
        ctMap['CONSTRUCTION'] = 'CONSTRUCTION';
        compMap['Construction'] = ctMap;
<% }
    if(comptypes.contains("MEASUREMENT")){
%>
        ctMap = new Array();
        ctMap['MEASUREMENT'] = 'MEASUREMENT';
        compMap['Measurements'] = ctMap;
<% }
    //Image Pages
%>
        ctMap = new Array();
<%  //Sort the Image Pages types by display name.
    Map<String, String> iptypes = new TreeMap<String, String>();
    for(String cType : comptypes){
        if(cType.indexOf("IMAGES_PAGE") > -1){
            iptypes.put(compMap.get(cType), cType);
        }
        
    }
    
    Iterator<String> cti = iptypes.keySet().iterator();
    while(cti.hasNext()){
        compType = (String)cti.next();%>
        ctMap['<%=iptypes.get(compType)%>'] = ['<%=iptypes.get(compType)%>'];
  <%}%>
    compMap['Images Page'] = ctMap;


     var bomReports = new Array();
	 var defMap2 = new Array();
	 
<%
	String defView2 = null;
	Iterator<?> viewIter2 = specRequests.iterator();
	String repStr = null;
	Collection<?> repPages = null;
	Iterator<?> rpi = null;
	String repPage;
	while(viewIter2.hasNext()){
		defView2 = (String)viewIter2.next();
		repStr = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.pageOptions." + defView2);
%>
			defMap1 = new Array();
<%
		if(FormatHelper.hasContent(repStr)){
		
			repPages = parseString(repStr, ",");
			rpi = repPages.iterator();
			while(rpi.hasNext()){
				repPage = (String)rpi.next();
%>
			defMap1['<%=repPage%>'] = '<%=repPage%>';
<%
			}
%>
			bomReports['<%=defView2%>'] = defMap1;
		
<%
		}        
	}
%>
//add for story B-58112 03/30/2011

Event.observe(window,'load',function() {
	setDefaultSinceDateForCommon();
});


function showDateTimeRightToWidget(sinceDate) {
		sinceDate.setHours(0);
		sinceDate.setMinutes(1);
		$("dateTimeDisplay").innerHTML = formatDateString(sinceDate, "<%= WTMessage.getLocalizedMessage ( RB.DATETIMEFORMAT, "jsCalendarFormat", RB.objA ) %>") ;
		$("dateTimeDisplay").style.display='inline-block';				
}


function getSelectedDate() {

    var choice = parseInt($F("daysValueId").strip());
	var sinceDate = new Date();
	if(isNaN(choice)) {
		var tStr = $F('sinceDateInput').strip();
		if(tStr == ""){						//Date input is empty
			alert(dateSelectEmpty);
			return false;
		} else {
		    sinceDate = chkDateString(tStr, '', '<%=jsCalendarFormat%>', true);
		}
	} else {
		sinceDate.setDate(sinceDate.getDate() - choice);
	}
	return sinceDate;
}

function setDefaultSinceDateForCommon() {
	if(window.opener.parent && window.opener.parent.headerframe){
		if(window.opener.parent.headerframe.getGlobalChangeTrackingSinceDate) {
			dateStr = window.opener.parent.headerframe.getGlobalChangeTrackingSinceDate();
		}
	}		
	
	setDefaultSinceDate();	
}

function setDefaultSinceDateForAjax() {
	if(window.parent && window.parent.headerframe){
		if(window.parent.headerframe.getGlobalChangeTrackingSinceDate) {
			dateStr = window.parent.headerframe.getGlobalChangeTrackingSinceDate();
		}
	}else{
		dateStr = (new Date()).print('<%=jsCalendarFormat%>');
	}
	setDefaultSinceDate();

}


function setDefaultSinceDate() {

	$('sinceDateInput').value = dateStr;		
    var selectedDate = chkDateString(dateStr, '', '<%=jsCalendarFormat%>', true);
	var selectedDateStr = selectedDate.print('<%=jsCalendarFormat%>');
	var customizeElem = null;
	var specialDate = false;
	var daysMap = <%=daysHolder%>;
	var daysValueIdSelect = $('daysValueId');
	for(var i = 0 ; i < daysValueIdSelect.options.length; i++) {
		var opt = daysValueIdSelect.options[i];
		var choice = opt.value.strip();
		if(daysMap[choice]) {
			choice = daysMap[choice].strip();
			opt.value = choice;
			if(!isNaN(choice)) {
				var days = parseInt(choice);
				var date = new Date();
				date.setDate(date.getDate() - days);
				opt.dateVal = date.print('<%=jsCalendarFormat%>');
				if(selectedDateStr == opt.dateVal) {
						opt.selected=true;
						specialDate = true;
						$('sinceDateCal').style.display = 'none';
				}
			} else {
				customizeElem = opt;
			}
		}
	}
	if(! specialDate) {
		customizeElem.selected=true;
		$('sinceDateCal').style.display = 'inline-block';
	} else {
		$('sinceDateInput').value = new Date().print('<%=jsCalendarFormat%>');
		showDateTimeRightToWidget(selectedDate);
	}	
}

</script>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// HTML ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<% if( type != null){%>
<input type="hidden" name="typeId" value="<%= FormatHelper.getObjectId(type) %>">
<%}%>
<input type="hidden" name="vaultDocumentTypeId" id="vaultDocumentTypeId" value="">
<input type="hidden" name="specGen" value="true">
<input type="hidden" name="useLandscape" id="useLandscape" value="false">
<input type="hidden" name="returnMethod" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("returnMethod")) %>">
<input type="hidden" name="seasonId" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("seasonId")) %>"><!-- USED BY SEARCH -->
<input type="hidden" name="productId" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("productId")) %>"><!-- USED BY SEARCH -->

<table width="100%">
    <tr>
        <td class="button" align="left">
            <a class="button" href="javascript:<%=selectFunction %>"><%= selectButton %></a>&nbsp;&nbsp;&nbsp;
        </td>
    </tr>
   <tr>
      <td width="10%" valign="top">
         <%= tg.startGroupBorder() %>
         <%= tg.startTable() %>
         <%= tg.startGroupTitle() %><%= chooserGroupTitle %><%= tg.endTitle() %>
         <%= tg.startGroupContentTable() %>
         <tr>
            <td>
                <table>
                    <tr>
                    <% if(specRequestsMap != null && specRequestsMap.size() > 0) {%>
                    <%=  fg.createDropDownListWidget(specRequestLabel, specRequestsMap, "printLayout", "", "javascipt:ChooseSpecPages2.chooseView(this.value)", false)%>
                    <%}%>
                    </tr>
                    <tr>
           <%=fg.createStandardBooleanInput("includeChildSpecs", includeChildSpecifications, FormatHelper.parseBoolean(DEFAULT_CHILD_SPECS_CHECKED), false, false )%>
                    </tr>
                   <tr>
           <%=fg.createStandardBooleanInput("includeBOMOwnerVariations", includeBOMOwnerVariations, FormatHelper.parseBoolean(DEFAULT_BOM_OWNER_VARIATIONS), false, false )%>
                    </tr>
                    <tr>
           <%=fg.createStandardBooleanInput("includeMeasurementsOwnerSizes", includeMeasurementsOwnerSizes, FormatHelper.parseBoolean(DEFAULT_MEASUREMENTS_OWNER_VARIATIONS), false, false )%>
                    </tr>
                    <tr>
            <%= fg.createMultiChoice("specPages", availableComponentsLabel, compMap, null, false, sortVec, false) %>
                  <td>
                      <table>
                              <tr><td>
                              <a title='<%=FormatHelper.encodeForJavascript(moveUpLabel)%>' id='compmoveUpList' href='#' onclick='javascript:moveUpList(document.getElementById("specPagesChosen"), document.getElementById("specPages"));return false;'><img src='<%=WT_IMAGE_LOCATION%>/page_up.png' border='1' alt='<%=FormatHelper.encodeForJavascript(moveUpLabel)%>'></a>
                              </td></tr>
                              <tr><td>
                              <a title='<%=FormatHelper.encodeForJavascript(moveDownLabel)%>' id='compmoveDownList' href='#' onclick='javascript:moveDownList(document.getElementById("specPagesChosen"), document.getElementById("specPages"));return false;'><img src='<%=WT_IMAGE_LOCATION%>/page_down.png' border='1' alt='<%=FormatHelper.encodeForJavascript(moveDownLabel)%>'></a>
                              </td></tr>
                      </table>
               </td>
            </tr>
            <tr><td>&nbsp;&nbsp;&nbsp;</td></tr><tr>
            <tr><td>&nbsp;&nbsp;&nbsp;</td></tr>
            <tr>
            <%=fg.createStandardBooleanInput("includeMarkedupImagesContent", includeMarkedupImagesLabel, false, false, false )%>
            </tr>
            <tr>
           <%=fg.createStandardBooleanInput("includeSecondaryContent", includeSecondaryLabel, false, false, false )%>
                    </tr>
                    <tr>
                    </tr>
                    <tr>
                    <% if(REVISE_DOCUMENT)availableDocumentsLabel=allAvailableDocumentsLabel;%>
             <%= fg.createMultiChoice("availDocs", availableDocumentsLabel, docMap, null, false, sortVec, false) %>
                  <td>
                      <table>
                              <tr><td>
                              <a title='<%=FormatHelper.encodeForJavascript(moveUpLabel)%>' id='compmoveUpList' href='#' onclick='javascript:moveUpList(document.getElementById("availDocsChosen"), document.getElementById("availDocs"));return false;'><img src='<%=WT_IMAGE_LOCATION%>/page_up.png' border='1' alt='<%=FormatHelper.encodeForJavascript(moveUpLabel)%>'></a>
                              </td></tr>
                              <tr><td>
                              <a title='<%=FormatHelper.encodeForJavascript(moveDownLabel)%>' id='compmoveDownList' href='#' onclick='javascript:moveDownList(document.getElementById("availDocsChosen"), document.getElementById("availDocs"));return false;'><img src='<%=WT_IMAGE_LOCATION%>/page_down.png' border='1' alt='<%=FormatHelper.encodeForJavascript(moveDownLabel)%>'></a>
                              </td></tr>
                      </table>
               </td>
               </tr>
            <tr><td>&nbsp;&nbsp;&nbsp;</td></tr>
            <% if (CAD_DATA_ENABLED) { %>
            <tr></tr><tr>
           <%=fg.createStandardBooleanInput("includeCADDocuments", includeCADDocsLabel, false, false, false )%>
           </tr>
           <tr></tr>
           <tr>
           <% 
              // B-92545 Map<String,String> savedFilters = com.lcs.wc.specification.FlexSpecHelper.getSaveFiltersForTechPack(null);
              // B-92545 out.print(fg.createDropDownListWidget(cadDocFilterLabel, savedFilters, "cadDocFilter", "", null, false));
            } else {
           %>
                <input type="hidden" name="includeCADDocumentsbox" id="includeCADDocumentsbox" value="">
                <%-- B-92545
                <input type="hidden" name="cadDocFilter" id="cadDocFilter" value="">   
                --%>
           <%         	
            }
           %>
           </tr>
            <% if (PART_DATA_ENABLED) { %>
           <tr>
           <%=fg.createStandardBooleanInput("includeAllParts", includeAllPartsLabel, false, false, false )%>
           </tr>
           <tr>
           <% 
            } else {
           %>
                <input type="hidden" name="includeAllPartsbox" id="includeAllPartsbox" value="">
           <%         	
            }
           %>
           </tr>
           </table>
            </td>
         </tr>
         <%= tg.endContentTable() %>
         <%= tg.endTable() %>
         <%= tg.endBorder() %>
      </td>
   </tr>
   <tr>
        <td>
         <%= tg.startGroupBorder() %>
         <%= tg.startTable() %>
         <%= tg.startGroupTitle() %><%= pageOptionsGrpTle %><%= tg.endTitle() %>
         <%= tg.startGroupContentTable() %>
    
            <tr>
               <td class="SUBSECTION">
	  <div id='pageOptionsdiv_plus'>
		 <a href="javascript:changeDiv('pageOptionsdiv')"><img src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png" alt="" border="0" align="absmiddle"></a>&nbsp;&nbsp;
		 <a href="javascript:changeDiv('pageOptionsdiv')"><%= pageOptionsLabel%></a>
                  </div>
	  <div id='pageOptionsdiv_minus'>
		 <a href="javascript:changeDiv('pageOptionsdiv')"><img src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" alt="" border="0" align="absmiddle"></a>&nbsp;&nbsp;
		 <a href="javascript:changeDiv('pageOptionsdiv')"><%= pageOptionsLabel%></a>
                  </div>
               </td>
            </tr>
            <tr>
               <td>
	 <div id='pageOptionsdiv'>
                  <table>
                     <tr><td>
                     <%=fg.createIntegerInput("numColorwaysPerPage", numberColorwaysPerPageLabel, "5", 2, 0, 0, false, true, false )%>
                     </td></tr>
                     <tr><td>
                     <%=fg.createIntegerInput("numSizesPerPage", numberSizesPerPageLabel, "12", 2, 0, 0, false, true, false )%>
                     </td></tr>
                     <tr><td>
                     <%=fg.createStandardBooleanInput("showColorSwatches", showColorSwatchesLabel, true, false)%>
                     </td></tr>
                     <tr><td>
                     <%=fg.createStandardBooleanInput("showMaterialThumbnail", showMaterialThumbnailsLabel, true, false)%>
                     </td></tr>
                     <tr><td>
                     <input type="hidden" name="useSize1Size2" id="useSize1Size2" value="size1">
                     </td></tr>
		 <tr><td>
         <%=fg.createDropDownListWidget(measurementUOMOverrideLabel, formats, "uom", "si.Length.in", null, false, false) %>
		 </td></tr>
                  </table>
                 </div>
               </td>
            </tr>
            <script>
   toggleDiv('pageOptionsdiv');
   toggleDiv('pageOptionsdiv_minus');
            </script>
            <tr>
               <td><hr></td>
            </tr>

            <%-- Reports--%>
            <tr>
               <td>
                 <table>
                 <% 
					 //START - Footwear TechPack Implementation
					 if(isRBKProductFootwear){
						 allOptions.remove("Measurements:  gradeRulesIncrementReport");
						 allOptions.remove("Measurements:  gradingReport");
						 allOptions.remove("Measurements:  fitSpecReport");
					 }
					//END - Footwear TechPack Implementation
				 %>
                 <%= fg.createMultiChoice("pageOptions", availableReportsLabel, allOptions, null, false, null, false, null, "6", "javascript:ChooseSpecPages2.updatePage()") %>
                  </td>
                  <td>
                      <table>
                              <tr><td>
                              <a title='<%=FormatHelper.encodeForJavascript(moveUpLabel)%>' id='sizeRunmoveUpList' href='#' onclick='javascript:moveUpList(document.getElementById("pageOptionsChosen"), document.getElementById("pageOptions"));return false;'><img src='<%=WT_IMAGE_LOCATION%>/page_up.png' border='1' alt='<%=FormatHelper.encodeForJavascript(moveUpLabel)%>'></a>
                              </td></tr>
                              <tr><td>
                              <a title='<%=FormatHelper.encodeForJavascript(moveDownLabel)%>' id='sizeRunmoveDownList' href='#' onclick='javascript:moveDownList(document.getElementById("pageOptionsChosen"), document.getElementById("pageOptions"));return false;'><img src='<%=WT_IMAGE_LOCATION%>/page_down.png' border='1' alt='<%=FormatHelper.encodeForJavascript(moveDownLabel)%>'></a>
                              </td></tr>
                          </table>
                  </table>
               </td>
            </tr>
			<!--add for story B-58112 03/30/2011-->            
			<tr><td>
			<div id="divTrackedChanges" style="display:none">
			<table id="tblTrackedChanges" border='0' width='100%'>
			<tr>
				<td class="SUBSECTION" nowrap colspan='2'> 
				<div id='trackedChanges_div_plus'>
					<a href="javascript:changeDiv('trackedChanges_div')"><img src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png" alt="" border="0" align="absmiddle"></a>&nbsp;&nbsp;
					<a href="javascript:changeDiv('trackedChanges_div')"><%=trackedChangesLabel + " : "+ showChangeSinceLabel%></a>
				</div>
				<div id='trackedChanges_div_minus'>
					<a href="javascript:changeDiv('trackedChanges_div')"><img src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" alt="" border="0" align="absmiddle"></a>&nbsp;&nbsp;
					<a href="javascript:changeDiv('trackedChanges_div')"><%=trackedChangesLabel + " : "+ showChangeSinceLabel%></a>
			    </div>
			    </td>
			</tr>
			<tr>
				<td>
				<div id="trackedChanges_div">
					<!-- span class="FORMLABEL" style="display:inline-block;width:150px;font-size:11px;padding:0;"><%=showChangeSinceLabel %>:</span-->
					<%=fg.createHiddenInput("showChangeSince", "") %>
					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					<%= fg.createDropDownList(daysDisplayMap, "daysValueId", showChangeSinceDefault, "javascript:ChooseSpecPages2.changeSinceDate()", 1,false,null,daysOrder) %>
					<span id="sinceDateCal" style="display:none;">
						<%=FormGenerator.createDateInputField("sinceDate","","",10,10,false)%>
					</span> 
					<span id="dateTimeDisplay" class="FORMLABEL" style="display:none;"></span>
				</div>
				</td>
			</tr>
			<script>
			    toggleDiv('trackedChanges_div_plus');
			</script> 
			<tr><td><hr></td></tr>
			</table>
			</div>
  			<div id="divPartData" style="display:none">
			<table id="tblPartData" border='0' width='100%'>
			<tr>
				<td class="SUBSECTION"> 
	 			 <div id='partOptionsdiv_plus'>
					 <a href="javascript:changeDiv('partOptionsdiv')"><img src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png" alt="" border="0" align="absmiddle"></a>&nbsp;&nbsp;
					 <a href="javascript:changeDiv('partOptionsdiv')"><%= partDataOptionsLabel%></a>
                 </div>
				 <div id='partOptionsdiv_minus'>
					 <a href="javascript:changeDiv('partOptionsdiv')"><img src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" alt="" border="0" align="absmiddle"></a>&nbsp;&nbsp;
					 <a href="javascript:changeDiv('partOptionsdiv')"><%= partDataOptionsLabel%></a>
                 </div>
               </td>
            </tr>			
            <tr>
				<td>
					<div id="partOptionsdiv">
					<table id="tblexportedIndentedBOM" border='0' width='20%'>
						<tr>
							<td>
							<%=fg.createCustomActionBooleanInput("exportedIndentedBOM", exportedIndentedBOMLabel, "false", "javascript:RFASpecPrinter.changePartFilter();", false, true) %>
							</td>
						</tr>
						<tr>
							<td><%= fg.createDropDownListWidget(partFilterLabel, savePartFilters, "partFilter", defaultFilterName, null, false, false)%></td>
						</tr>
					</table>
					</div>
				</td>
			</tr>
			<script>
			    toggleDiv('partOptionsdiv_plus');
			</script> 
			<tr><td><hr></td></tr>
			</table>
			</div>
         <%= tg.endContentTable() %>
         <%= tg.endTable() %>
         <%= tg.endBorder() %>
        </td>
    </tr>
    
    
    <%if (ACLHelper.hasViewAccess(defaultDocumentVaultType)&&ACLHelper.hasCreateAccess(defaultDocumentVaultType)){ %>
    
    <tr>
    
    <td>
<div id="documentVaultDiv">
         <%= tg.startGroupBorder() %>
         <%= tg.startTable() %>
         <%= tg.startGroupTitle("DocumentVault") %>
         <%=vaultDocumentsLabel%>
         <%= tg.endTitle() %>
         <%= tg.startGroupContentTable("DocumentVault") %>
    </td></tr><table width="50%">
    	<tr><%=fg.createCustomActionBooleanInput("asynchronousGeneration", asynchGenLabel, "false", "javascript:RFASpecPrinter.setAsynchGen(this);", false, true) %>
    	</tr>
        <tr>
			<%=fg.createCustomActionBooleanInput("documentVault", vaultDocumentsLabel, "false","javascript:RFASpecPrinter.setDocumentVaultDiv(this);", false) %>
        </tr>
        <tr>
            <td class="FORMLABEL" align="left" nowrap width="150px"><div id="refTypeDiv" nowrap>&nbsp;&nbsp;<a href="javascript:ChooseSpecPages2.chooseVaultDocumentType()"><%= vaultDocumentTypeLabel %></a>&nbsp;</div></td>
            <td class="FORMELEMENT"  align="left">
               <b><div id="fkTypeDisplay" nowrap>------</div></b>
            </td>
        </tr>
         <%= tg.endContentTable() %>
         <%= tg.endTable() %>
         <%= tg.endBorder() %>

</div>
    
    
    </td>
    </tr>
    <%} %>
    <tr>
        <td class="button" align="left">
            <a class="button" href="javascript:<%=selectFunction %>"><%= selectButton %></a>&nbsp;&nbsp;&nbsp;
        </td>
    </tr>
</table>

<script>
<%if (ACLHelper.hasViewAccess(defaultDocumentVaultType)&& ACLHelper.hasCreateAccess(defaultDocumentVaultType)){ %>
    document.getElementById('fkTypeDisplay').innerHTML = '<%=FormatHelper.encodeForJavascript(FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE).getFullNameDisplay())%>';
if (null != document.getElementById("vaultDocumentTypeId")){
	document.getElementById("vaultDocumentTypeId").value = '<%=FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE))%>';
}else{
	document.getElementById('documentVaultDiv').style.display="none";
}
RFASpecPrinter.hideDocumentVaultDiv();
<%if(DEFAULT_ASYNC_MULTI_TPGENERATION||true){%>
   var dvCheckBox = document.getElementById('documentVaultbox');
   var dvInput = document.getElementById('documentVault');
   var agCheckBox = document.getElementById('asynchronousGenerationbox');
   var agInput = document.getElementById('asynchronousGeneration');
   dvCheckBox.disabled = 'true';
   dvCheckBox.checked = 'true';
   dvInput.value = true;
   agCheckBox.checked='true';
   agInput.value = true;
   RFASpecPrinter.showDocumentVaultDiv();
<%}%>
<%}%>
	
<%if (ajaxWindow) {%>
	setDefaultSinceDateForAjax();
<%}%>


//START -- Override OOtb Javascript function to default collapse DocumentVault Section
toggleExpandableDiv("DocumentVault", "DocumentVaultIcon");

function toggleExpandableDiv(divId, toggleIconId) {
    var div = document.getElementById(divId);
    var image = document.getElementById(toggleIconId);
	//commented ootb if else cond and written custom
    /*if (div.style.display == 'none') {
        div.style.display = 'block';
        image.src = wtImageLocation + '/expand_tree.png';
    } else {
        div.style.display = 'none';
        image.src = wtImageLocation + '/collapse_tree.png';
    }*/

if (div.style.display != 'none') {
        div.style.display = 'none';
        image.src = wtImageLocation + '/collapse_tree.png';
    } else {
        div.style.display = 'block';
        image.src = wtImageLocation + '/expand_tree.png';
    }
    closeActionsMenu();
}

//END -- Override OOtb Javascript function to default collapse DocumentVault Section
	
</script>