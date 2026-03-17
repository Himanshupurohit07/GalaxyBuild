<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page import="com.lcs.wc.sample.LCSSample"%>
<%@page import="com.lcs.wc.sample.LCSSampleQuery"%>
<%@page import="java.io.File"%>
<%@page import="java.util.Map.Entry"%>
<%@page language="java"
       import="com.lcs.wc.client.Activities,
                com.lcs.wc.client.web.*,
                com.lcs.wc.client.ClientContext,
                com.lcs.wc.util.*,
                com.lcs.wc.db.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.season.*,
                com.lcs.wc.changeAudit.ChangeTrackingPageHelper,
                com.lcs.wc.color.*,
                com.lcs.wc.construction.*,
                com.lcs.wc.document.*,
                com.lcs.wc.measurements.*,
                com.lcs.wc.epmstruct.*,
                com.lcs.wc.partstruct.*,
                com.lcs.wc.part.*,
                com.lcs.wc.product.*,
                com.lcs.wc.sizing.*,
                com.lcs.wc.specification.*,
                com.lcs.wc.sourcing.*,
                com.lcs.wc.flexbom.*,
                com.lcs.wc.report.*,
                com.lcs.wc.foundation.LCSQuery,
                com.lcs.wc.specification.*,
                com.lcs.wc.sample.LCSSample,
                com.lcs.wc.sample.LCSSampleRequest,
                java.sql.Timestamp,
		com.lcs.wc.sample.LCSSampleQuery,
                wt.content.ApplicationData,
                wt.content.ContentHelper,
                wt.part.WTPart,
                wt.ownership.*,
                wt.fc.*,
                wt.util.*,
                org.apache.logging.log4j.Logger,
                org.apache.logging.log4j.LogManager,
                wt.epm.*,
                wt.identity.IdentityFactory,
                wt.locks.LockHelper,
                wt.org.*,
                java.text.*,
                com.lcs.wc.measurements.*,
                java.util.*,
                java.util.stream.*"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="type" scope="request" class="com.lcs.wc.flextype.FlexType" />
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="flexg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="appContext" class="com.lcs.wc.client.ApplicationContext" scope="session"/>
<% lcsContext.setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%!
    private static final Logger logger = LogManager.getLogger("rfa.jsp.specification.ChooseSingleSpecPage2");
    public static final String JSPNAME = "ChooseSingleSpecPage";
    public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
    public static final String WT_IMAGE_LOCATION = LCSProperties.get("flexPLM.windchill.ImageLocation");
    boolean size2exists = false;
    public static final String TYPE_COMP_DELIM = PDFProductSpecificationGenerator2.TYPE_COMP_DELIM;
    public static final String MARKUPIMG = "MARKUPIMG";
    public static final String BOM_IDENTIFIER = "BOM:  ";
    public static final String SAMPLE_IDENTIFIER = "Sample: ";
    public static final String MEASUREMENTS_IDENTIFIER = "Measurements:  ";
    public static final String TRACKED_CHANGES_IDENTIFIER = "Tracked Changes:  ";
    public static final String CADDOC_VARIATIONS_IDENTIFIER = "CAD Document Variations:  ";
    public static final String PART_VARIATIONS_IDENTIFIER = "Part Variations:  ";
    public static final String SPEC_REQUESTS = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.specRequests");
    public static final String DEFAULT_ALL_COLORWAYS_CHECKED = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.defaultAllColorwaysChecked","true");
    public static final String DEFAULT_ALL_SIZES_CHECKED = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.defaultAllSizesChecked","false");
    public static final String DEFAULT_ALL_DESTINATIONS_CHECKED = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.defaultAllDestinationsChecked","false");
    public static final String DEFAULT_CHILD_SPECS_CHECKED = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.defaultChildSpecsChecked","false");
    public static final String DEFAULT_BOM_OWNER_VARIATIONS = LCSProperties.get("jsp.specification.TechPackGeneration.includeBOMOwnerVariations","true");
    public static final String DEFAULT_MEASUREMENTS_OWNER_SIZES = LCSProperties.get("jsp.specification.TechPackGeneration.includeMeasurementsOwnerSizes","true");
    public static final String DEFAULT_CHILD_DOCS_CHECKED = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.defaultChildDocsChecked","false");
    public static String DEFAULT_DOCUMENT_VAULT_TYPE = LCSProperties.get("com.lcs.wc.document.documentVault.document.flexType");
	public static String DEFAULT_DOCUMENT_VAULT_REEBOKTYPE = LCSProperties.get("com.lcs.wc.document.documentVault.document.flexType");	
	public static String DEFAULT_DOCUMENT_VAULT_LUCKYTYPE = LCSProperties.get("com.lcs.wc.document.documentVault.document.luckyflexType");
	public static String DEFAULT_DOCUMENT_VAULT_F21TYPE = LCSProperties.get("com.lcs.wc.document.documentVault.document.f21flexType");
	public static String DEFAULT_DOCUMENT_VAULT_NAUTICATYPE = LCSProperties.get("com.lcs.wc.document.documentVault.document.nauticaflexType","Document\\Generated Tech Pack\\scGeneratedTechPackNautica");
	public static String DEFAULT_DOCUMENT_VAULT_AEROTYPE = LCSProperties.get("com.lcs.wc.document.documentVault.document.aeroflexType","Document\\Generated Tech Pack\\scGeneratedTechPackAeropostale");	
    public static final boolean DEFAULT_ASYNC_SINGLE_TPGENERATION = LCSProperties.getBoolean("com.lcs.wc.specification.SingleTPGenAsyncService.defaultSelected");
    public static final boolean CADDOC_ENABLED = LCSProperties.getBoolean("com.lcs.wc.specification.cadData.Enabled");
    public static final boolean PART_ENABLED = LCSProperties.getBoolean("com.lcs.wc.specification.parts.Enabled");
    static final String ENCODING = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");
    static final String defaultUnitOfMeasure = LCSProperties.get(LCSProperties.DEFAULT_UNIT_OF_MEASURE, "si.Length.in");
    public static String instance = "";
    public static String cadDoc = "";
    public  String measurementSets;
    public  String measurementSetsName;
    public String msetDataString="";

    public static final Collection<String> UOMs = new ArrayList<String>();
    //add for story B-58112 03/28/2011
    public static final boolean INCLUDED_TRACKED_CHANGES = LCSProperties.getBoolean("jsp.specification.ChooseSingleSpecPage2.includedTrackedChanges");
    public static final String CLEAR_CELL_VALUE = LCSProperties.get("com.lcs.wc.LCSLogic.ClearCellValue", "!CLR");
    public static final boolean USE_PRODUCTDESTINATIONS = LCSProperties.getBoolean("com.lcs.wc.product.useProductDestinations");
    public static final String FIT_SAMPLE_ROOT_TYPE = LCSProperties.get("com.lcs.wc.sample.LCSSample.Product.Fit.Root","Sample\\Product");
    static {
        UOMs.addAll(getMeasurementSetUOMOverride());
    }

    public static List<String> getMeasurementSetUOMOverride() {
        final String uoms = LCSProperties.get("jsp.specification.ChooseSingleSpecPage2.measurementSetUOMOverride", "si.Length.in, si.Length.cm, si.Length.mm, si.Length.ft, si.Length.m");
        return FormatHelper.commaSeparatedListToList(uoms);
    }

    public static String drawCheckBox(String name, String value, String label, boolean checked){
        StringBuffer buffer = new StringBuffer();
        if(FormatHelper.hasContentAllowZero(label)){
            buffer.append("<td class=\"FORMLABEL\">" + FormatHelper.encodeAndFormatForHTMLContent(label) + "&nbsp;&nbsp; </td>");
        }
        buffer.append("<td>");
        buffer.append("<input class=\"TABLEFORMELEMENT\" ");
        buffer.append("type=\"checkbox\" ");
        buffer.append("name=\"" + name + "\" ");
        buffer.append("value=\"" + FormatHelper.encodeAndFormatForHTMLContent(value) + "\" ");
        if(checked){
             buffer.append(" checked ");
        }
        buffer.append("onclick=\"selectSize(this)\"");
        buffer.append(">");
        buffer.append("<br>");
        buffer.append("</td>");
        return buffer.toString();
    }
//checkbox for sizing in tech pack
    public static String drawCheckBox(String name, String value,  boolean checked, String id){
        StringBuffer buffer = new StringBuffer();

        buffer.append("<td class=\"LABEL\"   align=\"center\" >");
        buffer.append("<input class=\"TABLEFORMELEMENT\" ");
        buffer.append("type=\"checkbox\" ");
        buffer.append("id=\"" + id + "\" ");
        buffer.append("name=\"" + name + "\" ");
        buffer.append("value=\"" + FormatHelper.encodeAndFormatForHTMLContent(value) + "\" ");
        if(checked){
             buffer.append(" checked ");
        }
        buffer.append("onclick=\"selectSize(this)\"");
        buffer.append(">");
        buffer.append("<br>");
        buffer.append("</td>");
        return buffer.toString();
    }
    public static void addComponentToMaps(Map<Object,Object> cmap, Map<Object,String> nMap, FlexObject component){
        String ctype = component.getString("COMPONENT_TYPE_UNTRANSLATED");
        nMap.put(component.get("OID"), component.getString("NAME"));

        Collection<String> objs = (Collection<String>)cmap.get(ctype);

        if(objs == null){
            objs = new ArrayList<String>();
        }

        objs.add(component.getString("OID"));
        cmap.put(ctype, objs);
		
		
    }


    public static Collection sortComponents(Collection components, java.util.Locale locale) throws WTException{
        FlexObject fo = null;
        String compName;
        List<String> keyList = new ArrayList<String>();
        Map<String, FlexObject> foMap = new HashMap<String, FlexObject>();
        Iterator<?> componentsIter = components.iterator();
        int i= 0;
		while(componentsIter.hasNext()){
			fo = (FlexObject) componentsIter.next();
			String compOid = fo.getString("OID");
			Object component=null;
			if(FormatHelper.hasContent(compOid)){
			component =LCSQuery.findObjectById(compOid);
			}else{
				keyList.add(fo.getString("COMPONENT_TYPE"));
				foMap.put(fo.getString("COMPONENT_TYPE")+"0", fo);
			}

			LCSDocument document = null;
			boolean isTurntable = false;
			if(component!=null&&component instanceof LCSDocument) {
				document =  (LCSDocument)component;
				if(document != null)
					isTurntable = LCSDocumentLogic.isTurntable(document);
			}

            if( !isTurntable) {
				compName = fo.getData("NAME");
				String compType = fo.getData("COMPONENT_TYPE");
				compName =  compType + " " + compName;
				if(foMap.get(compName) != null) {
					i++;
					compName = compName + "-"+String .valueOf(i);
				}
				keyList.add(compName);
				foMap.put(compName, fo);
			}
		}
		Collection<String> sortedKeys = SortHelper.sortStrings(keyList, locale);
        Iterator<String> keysIter = sortedKeys.iterator();
        Collection<FlexObject> sortedComponents = new ArrayList<FlexObject>();
		
        while(keysIter.hasNext()){
            sortedComponents.add(foMap.get(keysIter.next()));
        }

        return sortedComponents;
    }

%>
<%
   String errorMessage = request.getParameter("errorMessage");



   String returnMethod = request.getParameter("returnMethod");
   String returnId = request.getParameter("returnId");
   appContext.setProductContext(returnId);
   String componentId = request.getParameter("componentId");

   //If we have passed a single component, we will not display all options
   boolean allComponents = !FormatHelper.hasContent(componentId);
   logger.debug("***ChooseSingleSpecPae - allComponents:"+allComponents);
   boolean isBOMComponent = !allComponents && (componentId.indexOf("com.lcs.wc.flexbom.FlexBOMPart:")> -1);
   boolean isMeasureComponent = !allComponents && (componentId.indexOf("com.lcs.wc.measurements.LCSMeasurements:")> -1);

   //If we don't have a source selected, display a dropdown list of sources
   boolean needSource = !allComponents;




   String availableComponentsLabel            = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "availableComponents_LBL", RB.objA ) ;
   String selectButton         = WTMessage.getLocalizedMessage ( RB.MAIN, "select_Btn", RB.objA ) ;
   String pageTitle            = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "createPDFSpec_PG_TLE", RB.objA ) ;
   String variationOptionsGrpTle = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "variationOptions_GRP_TLE", RB.objA ) ;
   String sourceLabel          = WTMessage.getLocalizedMessage ( RB.SEASON, "source_LBL", RB.objA ) ;//Source
   String colorwaysLabel       = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "colorwaysLabel", RB.objA ) ;
   String sizesLabel           = WTMessage.getLocalizedMessage ( RB.MEASUREMENTS, "sizes_LBL", RB.objA ) ;
   String destinationsLabel   = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "destinations_LBL", RB.objA ) ;
   String allLabel             = WTMessage.getLocalizedMessage (RB.MAIN, "all_LBL", RB.objA ) ;
   String specComponentsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "specComponents_LBL", RB.objA ) ;
   String includeChildSpecifications = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "includeChildSpecifications_LBL", RB.objA ) ;
   String includeBOMOwnerVariations = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "includeBOMOwnerVariations_LBL", RB.objA ) ;
   String includeMeasurementsOwnerSizes = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "includeMeasurementsOwnerSizes_LBL", RB.objA ) ;
   String moveUpLabel          = WTMessage.getLocalizedMessage (RB.MAIN, "moveUp_LBL", RB.objA ) ;
   String moveDownLabel    = WTMessage.getLocalizedMessage (RB.MAIN, "moveDown_LBL", RB.objA ) ;
   String pageOptionsGrpTle    = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "pageOptionsAndReports_GRP_TLE", RB.objA ) ;
   String pageOptionsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "pageOptions_LBL", RB.objA ) ;
   String specRequestLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "specRequest", RB.objA ) ;
   String numberColorwaysPerPageLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "numColorwaysPerPage_LBL", RB.objA ) ;
   String numberSizesPerPageLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "numSizesPerPage_LBL", RB.objA ) ;
   String availableReportsLabel  = WTMessage.getLocalizedMessage (RB.SPECIFICATION, "availableReports_LBL", RB.objA ) ;
   String viewOptionsLabel     = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "viewOptions_LBL", RB.objA ) ;
   String showColorSwatchesLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "showColorSwatches_LBL", RB.objA ) ;
   String showMaterialThumbnailsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "showMaterialThumbnails_LBL", RB.objA ) ;
   String fractionLabel = WTMessage.getLocalizedMessage ( RB.MEASUREMENTS, "fraction_LBL",RB.objA ) ;
   String measurementUOMOverrideLabel = WTMessage.getLocalizedMessage ( RB.MEASUREMENTS, "measurementUOMOverride_LBL",RB.objA ) ;
   String availableDocumentsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "availDocs_LBL", RB.objA ) ;
   String availDescDocsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "availDescDocs_LBL", RB.objA ) ;
   String vaultDocumentsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "vaultDocuments_LBL", RB.objA ) ;
   String vaultDocumentTypeLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "vaultDocumentType_LBL", RB.objA ) ;
   String includeSecondaryLabel = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "includeSecondary_LBL", RB.objA );

   String asynchGenLabel = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "asynchGen_LBL", RB.objA );
   String trackedChangesLabel = WTMessage.getLocalizedMessage ( RB.CHANGE, "trackedChanges_LBL", RB.objA ) ;
   String showChangeSinceLabel = WTMessage.getLocalizedMessage( RB.EVENTS, "showChangeSince_lbl", RB.objA);
   String expandedTrackedChangesLabel = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "expandedTrackedChanges_LBL", RB.objA );
   String condensedTrackedChangesLabel = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "condensedTrackedChanges_LBL", RB.objA );
   String cadDocVariationsLabel = WTMessage.getLocalizedMessage( RB.EPMSTRUCT, "cadDocVariations_LBL", RB.objA );
   String partVariationsLabel = WTMessage.getLocalizedMessage( RB.PARTSTRUCT, "partVariations_LBL", RB.objA );
   String availableCADDocGrpHdr = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "availableCADDoc_GRPHDR", RB.objA );
   String availableCADDocLbl    = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "availableCADDoc_LBL", RB.objA );
   String cadDocFilterLabel     = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "cadDocFilter_LBL", RB.objA );
   String availablePartGrpHdr = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "availablePart_GRPHDR", RB.objA );
   String availablePartLbl    = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "availablePart_LBL", RB.objA );
   String partFilterLabel     = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "partFilter_LBL", RB.objA );
   String showIndentedBOMLabel = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "exportedIndentedBOM_LBL", RB.objA );
   String partDataOptionsLabel = WTMessage.getLocalizedMessage( RB.SPECIFICATION, "partDataOptions_LBL", RB.objA );
   String useValuesLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "use_LBL", RB.objA ) ;
   String baseUOM = WTMessage.getLocalizedMessage ( RB.UOMRENDERER, defaultUnitOfMeasure + "_prompt",RB.objA ) ;
   String markedUpLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "markedUp_LBL", RB.objA );

   String tabPage = request.getParameter("tabPage");
   FlexType defaultDocumentVaultType = FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE);
   //add for story B-58112 03/28/2011

   Vector<String> daysOrder = ChangeTrackingPageHelper.getChangeEventDaysDisplayOrders(lcsContext.getLocale());
   Map<String,String> daysDisplayMap = ChangeTrackingPageHelper.getChangeEventDaysDisplayMap(lcsContext.getLocale());
   Map<String,String> daysMap = ChangeTrackingPageHelper.getChangeEventDaysMap(lcsContext.getLocale());
   String showChangeSinceDefault = ChangeTrackingPageHelper.getChangeEventDaysDefault();
   String jsCalendarFormat = WTMessage.getLocalizedMessage( RB.DATETIMEFORMAT, "jsCalendarFormat", RB.objA);
   RequestParamHolder daysHolder = new RequestParamHolder(daysMap);

   //Taking the values from the properties file and create map
   // of the report types to select from
    //START - Footwear Product tech Pack implementation 
   boolean isRBKProductFootwear = false ; 
   LCSProduct ftwProduct = appContext.getProductARev();
   if(ftwProduct.getFlexType().getFullNameDisplay().toString().equals("Footwear\\Reebok")){
   		isRBKProductFootwear = true ; 
   }
   if(ftwProduct.getFlexType().getFullNameDisplay().contains("Lucky")){
	  System.out.println("inside techpack Lucky");
	  DEFAULT_DOCUMENT_VAULT_TYPE = DEFAULT_DOCUMENT_VAULT_LUCKYTYPE;
	  defaultDocumentVaultType  = FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE);
	  
   }
   else if(ftwProduct.getFlexType().getFullNameDisplay().contains("Forever 21")){
	  DEFAULT_DOCUMENT_VAULT_TYPE = DEFAULT_DOCUMENT_VAULT_F21TYPE;
	  defaultDocumentVaultType  = FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE);
   }else if(ftwProduct.getFlexType().getFullNameDisplay().contains("Aeropostale")){
	  DEFAULT_DOCUMENT_VAULT_TYPE = DEFAULT_DOCUMENT_VAULT_AEROTYPE;
	  defaultDocumentVaultType  = FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE);
   } else if(ftwProduct.getFlexType().getFullNameDisplay().contains("Nautica")){
	  DEFAULT_DOCUMENT_VAULT_TYPE = DEFAULT_DOCUMENT_VAULT_NAUTICATYPE;
	  defaultDocumentVaultType  = FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE);
   }
   else {
	  DEFAULT_DOCUMENT_VAULT_TYPE = DEFAULT_DOCUMENT_VAULT_REEBOKTYPE;
	  defaultDocumentVaultType  = FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE); 
   }
   
   //System.out.println("defaultDocumentVaultType---------------"+defaultDocumentVaultType);
   
	//END - Footwear Product tech Pack implementation 

   Vector bomOptions = new Vector(new ClassLoadUtil(FileLocation.productSpecBOMProperties2).getKeyList());
   Collections.sort(bomOptions);
   Vector measurementOptions = new Vector(new ClassLoadUtil(FileLocation.productSpecMeasureProperties2).getKeyList());
   Collections.sort(measurementOptions);
  
   //Hypercare-150, TASK-9256 changes for Sample Details Report - start
   	WTProperties wtproperties = null;
	String wthome = "";
	wtproperties = WTProperties.getLocalProperties();
	wthome = wtproperties.getProperty("wt.home");
   Vector sampleOptions = new Vector(new ClassLoadUtil(wthome+File.separator+"codebase"+File.separator+"ProductSpecificationSAMPLE2.properties").getKeyList());
   Collections.sort(sampleOptions);
 //Hypercare-150, TASK-9256 changes for Sample Details Report - end
 
   Map allOptions = new HashMap();
	
 //START-SAMPLE TECH PACK IMPLEMENTATION
   Map<String,String> sampleOptionsMap = FormatHelper.toMap(sampleOptions, RB.SAMPLES);
 //END-SAMPLE TECH PACK IMPLEMENTATION
   Map<?,?> bomOptionsMap = FormatHelper.toMap(bomOptions, RB.FLEXBOM);
   Map<String,String> measurementOptionsMap = FormatHelper.toMap(measurementOptions, RB.MEASUREMENTS);
 
    measurementOptionsMap.put("gradeRulesIncrementReport","Measurement Set: Grades Rules Increment");
	 //changes for RM Task# 10671 - start
	 sampleOptionsMap.put("sampleRequestComments","Sample: Sample Request Comments");
	 sampleOptionsMap.put("sampleFitPhotoCorrectionsImagePage","Sample: Sample Fit Photo/Corrections");
	 sampleOptionsMap.put("sampleRequestFitDetails","Sample: Sample Request Fit Details");
	 //changes for RM Task# 10671 - end

   Iterator<?> measKeysItr = measurementOptions.iterator();
   Iterator<?> bomKeyItr = bomOptions.iterator();
   Iterator<?> sampleKeyItr = sampleOptions.iterator();
   if(allComponents || isMeasureComponent) {
     String dkey = "";
     while(measKeysItr.hasNext())
     {
         dkey = (String)measKeysItr.next();
         allOptions.put(MEASUREMENTS_IDENTIFIER + dkey, measurementOptionsMap.get(dkey));
     }
   }
   if(allComponents || isBOMComponent) {
     String dkey = "";
     while(bomKeyItr.hasNext())
     {
         dkey = (String)bomKeyItr.next();
         allOptions.put(BOM_IDENTIFIER + dkey, bomOptionsMap.get(dkey));
    }
  }
 //Hypercare-150, TASK-9256 changes for Sample Details Report - start
   if(allComponents ) {
	     String dkey = "";
	     while(sampleKeyItr.hasNext())
	     {
	         dkey = (String)sampleKeyItr.next();
	         
	         allOptions.put(SAMPLE_IDENTIFIER + dkey, sampleOptionsMap.get(dkey));
	    }
	  }
 //Hypercare-150, TASK-9256 changes for Sample Details Report - end
   
   //add for story B-58112 03/28/2011
   if (allComponents && INCLUDED_TRACKED_CHANGES) {
       allOptions.put(TRACKED_CHANGES_IDENTIFIER + ChangeTrackingPDFGenerator.EXPANDED_REPORT, expandedTrackedChangesLabel);
       allOptions.put(TRACKED_CHANGES_IDENTIFIER + ChangeTrackingPDFGenerator.CONDENSED_REPORT, condensedTrackedChangesLabel);
   }

   if (allComponents && CADDOC_ENABLED) {
       allOptions.put(CADDOC_VARIATIONS_IDENTIFIER + PDFCadDocVariationsGenerator.VARIATIONS_REPORT, cadDocVariationsLabel);
   }


   if (allComponents && PART_ENABLED) {
       allOptions.put(PART_VARIATIONS_IDENTIFIER + PDFPartVariationsGenerator.VARIATIONS_REPORT, partVariationsLabel);
   }


   Collection<String> specRequests = SingleSpecPageUtil.getSpecRequests();

   Map specRequestsMap = FormatHelper.toMap(specRequests, RB.SPECIFICATION);

   FlexSpecification currentSpec;
   LCSProduct product = appContext.getProductARev();
   if(FormatHelper.hasContent(returnId) && (returnId.indexOf("com.lcs.wc.specification.FlexSpecification") > -1)){
      currentSpec = (FlexSpecification)LCSQuery.findObjectById(returnId);
   } else {
      currentSpec = (FlexSpecification)appContext.getSpecification();
   }



   /*
      use the method above to get all the associated CAD documents with spec, currently , I only hard coded for
      demo, the save filters map returned as the <name,name>  since the name is unique for filters. we do not use filter id in here.

   */

   // get filter to pass into helper method
   Map<String,String> specLinks = new HashMap<String,String>();
   List<String> cadDocsList = new ArrayList<String>();
   // Find associated caddocs and build MOA string to pass into helper method
   try {
    if (currentSpec != null) {
          Collection<FlexObject> epmDocs = new LCSEPMDocumentQuery().findAssociatedEPMDocumentsByCriteria(currentSpec, new HashMap<String,String>());
          for (FlexObject flexObject : epmDocs) {
            cadDocsList.add("VR:wt.epm.EPMDocument:"+flexObject.getString("FLEXEPMDOCTOSPECLINK.BRANCHIDA3B5"));
            specLinks.put("OR:com.lcs.wc.epmstruct.FlexEPMDocToSpecLink:"+flexObject.getString("FLEXEPMDOCTOSPECLINK.IDA2A2"), flexObject.getString("EPMDOCUMENTMASTER.NAME"));
      }
    }
   } catch(Exception e){
       e.printStackTrace();
   }
   /* B-92545
   Map<String,String> saveFilters=FlexSpecHelper.getSaveFiltersForTechPack(cadDocsList);
   */
   // This is only for parts , to retrieve the parts info
   Map<String,String> partTOSpecLinks = new HashMap<String,String>();
   List<String> partsList = new ArrayList<String>();
   // Find associated parts and build MOA string to pass into helper method
   try {
    if (currentSpec != null) {

        Collection<FlexObject> parts = new FlexPartToSpecLinkQuery().findAssociatedPartsByCriteria(currentSpec, new HashMap<String,String>());
        Collection<WTPart> wtParts = LCSQuery.getObjectsFromResults(parts, "VR:wt.part.WTPart:",  "WTPART.BRANCHIDITERATIONINFO");
        //putting iteration display indentifier of WtPart into HashMap by key branch identifier of WtPart
        HashMap  tempMap = new HashMap();
        for(WTPart wtPart:wtParts) {
            tempMap.put(wtPart.getBranchIdentifier(),  IdentityFactory.getDisplayIdentifier(wtPart).getLocalizedMessage(WTContext.getContext().getLocale()).toString());
        }
        for (FlexObject flexObject : parts) {
            partsList.add("VR:wt.part.WTPart:"+flexObject.getString("FLEXPARTTOSPECLINK.BRANCHIDA3B5"));
            partTOSpecLinks.put("OR:com.lcs.wc.partstruct.FlexPartToSpecLink:"+flexObject.getString("FLEXPARTTOSPECLINK.IDA2A2"), ""+tempMap.get(Long.valueOf(flexObject.get("WTPART.BRANCHIDITERATIONINFO").toString())));
      }
    }
   } catch(Exception e){
       e.printStackTrace();
   }

   Map<String,String> savePartFilters=new FlexSpecUtil().getSaveFiltersForTechPack(partsList);
   String defaultFilterName = new FlexSpecUtil().getDefaultFilterName(partsList.size() > 0 ? partsList.get(0) : null);

   FlexSpecQuery q;
   Collection<FlexObject> components = new ArrayList<FlexObject>();
   Collection<FlexObject> componentsCustom = new ArrayList<FlexObject>();
   Map<Object,Object> compMap = new HashMap<Object,Object>();
   Map<Object,String> nameMap = new HashMap<Object,String>();
   Map<String,String> listOfComp = new HashMap<String,String>();
   Map<String,String> listOfDocs = new HashMap<String,String>();
   Map<String,String> listOfDescDocs = new HashMap<String,String>();
   Map<String,String> childDocs = new HashMap<String,String>();
   Map<String,String> childDescDocs = new HashMap<String,String>();
   Collection<String> parentComponents = new ArrayList<String>();
   Collection<String> linkedComponents = new ArrayList<String>();
   Map<String,String> childListOfComp = new HashMap<String,String>();
   String componentSelectedValues ="";
   String docContentSelectValue = "";
   String docContentSelectValue1 = "";
   Collection<FlexType> bomFlexTypes = new ArrayList<FlexType>();
   FlexObject compfo = null;
   FlexBOMPart tbom;
   String compName;
   String compOid;
   String compType1;
   String compType2;
   String compId;
   String hasComponentParent;
   LCSProduct prodB = appContext.getProductSeasonRev();
   ZipGenerator gen = new ZipGenerator();
   gen.addDocumentsToMap(product, listOfDocs, listOfDescDocs);
   gen.addDocumentsToMap(prodB, listOfDocs, listOfDescDocs);
   Iterator<?> skus = LCSSKUQuery.findSKUs(product).iterator();
   LCSSKU colorway = null;
   while (skus.hasNext()) {
       colorway = (LCSSKU)skus.next();
       gen.addDocumentsToMap(colorway, listOfDocs, listOfDescDocs);
   }
   if (prodB != null) {
       skus = LCSSKUQuery.findSKUs(prodB).iterator();
       while (skus.hasNext()) {
           colorway = (LCSSKU)skus.next();
           gen.addDocumentsToMap(colorway, listOfDocs, listOfDescDocs);
       }
   }
   if (currentSpec != null)
   {
        gen.addDocumentsToMap(currentSpec, listOfDocs, listOfDescDocs);
   }

   if(allComponents) {
	   //Header Page
	    FlexObject fob=new FlexObject();
	   
	  fob.put("COMPONENT_TYPE","Header Page");
	  fob.put("NAME","");
	  fob.put("COMPONENTNAME","");	 
	  fob.put("COMPONENTID","");
	  fob.put("COMPONENTCLASS","");
	  fob.put("COMPONENT_TYPE_UNTRANSLATED","Header Page");
	  //fob.put("LAST_MODIFIED_ON",new Date().toString());
		components.add(fob);
	   
	   //Colorways
	   fob=new FlexObject();
	  fob.put("COMPONENT_TYPE","Colorways");
	  fob.put("NAME","");
	  fob.put("COMPONENTNAME","");	 
	  fob.put("COMPONENTID","");
	  fob.put("COMPONENTCLASS","");
	  fob.put("COMPONENT_TYPE_UNTRANSLATED","Colorways");
	  components.add(fob);
	   
	  //Hypercare-150, TASK-9256 changes for Sample Details Report - start
	  LCSSourcingConfig sampleSourcingConfig = appContext.getSourcingConfig();
	   SearchResults sampleSearchResults1 =  LCSSampleQuery.findSamplesForProduct(product, new Hashtable<>(), sampleSourcingConfig.getFlexType(), sampleSourcingConfig);
	   Collection<?> sampleResults1 = sampleSearchResults1.getResults();
	  
	  if(sampleResults1 != null ){
		  Iterator<?> sampleItr1 = sampleResults1.iterator();
		  while(sampleItr1.hasNext()){
			  FlexObject samFob1 = (FlexObject) sampleItr1.next();
			  LCSSample sample1 = (LCSSample) LCSQuery.findObjectById("OR:com.lcs.wc.sample.LCSSample:"+samFob1.getString("LCSSAMPLE.IDA2A2"));
              samFob1.put("COMPONENT_TYPE","Sample");
			  samFob1.put("NAME",sample1.getName());
			  samFob1.put("COMPONENTNAME","");	 
			  samFob1.put("OID","OR:com.lcs.wc.sample.LCSSample:"+samFob1.getString("LCSSAMPLE.IDA2A2"));
			  samFob1.put("COMPONENTID","");
			  samFob1.put("COMPONENTCLASS","");
			  samFob1.put("COMPONENT_TYPE_UNTRANSLATED","Sample");
			  components.add(samFob1);
		  }
	  }
	  
	//Hypercare-150, TASK-9256 changes for Sample Details Report - end
	
      if(currentSpec != null) {
         componentsCustom = FlexSpecQuery.getSpecToComponentObjectsData(currentSpec, false);
		 
      } else {
         componentsCustom= FlexSpecQuery.getComponentReport(product, appContext.getSourcingConfig(), appContext.getSpecification(), null);
      }

      Hashtable criteria = new Hashtable();  
      Collection sampleResults= LCSSampleQuery.findSamplesForProduct(product, criteria, appContext.getSourcingConfig().getFlexType(), 
    		  appContext.getSourcingConfig(), null, appContext.getSpecification()).getResults();      
      Collection compIds = new ArrayList();
	  LCSSampleQuery sampQuery = new LCSSampleQuery();	   
	  FlexType sampleFlexType = FlexTypeCache.getFlexTypeFromPath(FIT_SAMPLE_ROOT_TYPE);  	   
	  FlexObject flexObject = null;
	  LCSFitTest fitTest = null;
	  LCSMeasurements measurements = null;
	  // Store value of measurement set name
	  String measurementDesc = "";
      String sampleSize = "";
	  String attKey = "sampleType";
	  String sampleType = "";
      String attValueKey = "";
	  String sampleIda2a2 = null;
	  LCSSample sample = null;	   
	  LCSSampleRequest sampleReq = null; 
	  String colon = ":";
	  String sampleReqName = null;
	  String oidSample = null;	   
	  String sampleRootType = "Sample";
	  String sampleName = null;
	  String sampId= null;

      if(sampleResults !=null && sampleResults.size() > 0 ){

        Iterator itr = sampleResults.iterator();
        Timestamp modifyTimestamp  = null;

        while(itr.hasNext()){
            flexObject = (FlexObject) itr.next();
            sampleIda2a2 = (String) flexObject.getString("LCSSAMPLE.IDA2A2");
            LCSSample sampleObj = (LCSSample) LCSQuery.findObjectById("OR:com.lcs.wc.sample.LCSSample:"+sampleIda2a2);

            if(modifyTimestamp==null){
                modifyTimestamp = sampleObj.getModifyTimestamp();
                sample = sampleObj;
            }else{
                if(!modifyTimestamp.after(sampleObj.getModifyTimestamp())){
                    modifyTimestamp = sampleObj.getModifyTimestamp();
                    sample = sampleObj;
                }
            }
        }

        if( sample!=null && ((sample.getFlexType()).getFullName(true)).contains("Fit")){

            sampleName = (String) sample.getValue("name");
            oidSample = "OR:"+sample;
            sampleRootType = "FitSample";
            sampId = sampleRootType + TYPE_COMP_DELIM + oidSample;
            //listOfComp.put(sampId, sampleName);

        }

      }

      components.addAll(compIds);

      //3D View and 3D Model should not be visible under available components for Tech Pack Generation
      componentsCustom=componentsCustom.stream().filter(comp -> !FlexSpecLogic.DOCUMENT_3DMODEL_COMPONENT_TYPE.equals(((FlexObject)comp).getData("COMPONENT_TYPE_UNTRANSLATED"))).collect(Collectors.toList());
      componentsCustom=componentsCustom.stream().filter(comp -> !FlexSpecLogic.DOCUMENT_3DVIEW_COMPONENT_TYPE.equals(((FlexObject)comp).getData("COMPONENT_TYPE_UNTRANSLATED"))).collect(Collectors.toList());
      componentsCustom = sortComponents(componentsCustom, lcsContext.getLocale());
      components.addAll(componentsCustom);
	     

      Iterator<?> compI = components.iterator();
      while(compI.hasNext()){
         compfo = (FlexObject)compI.next();
         //START - Footwear Tech Pack Implementation
         if(isRBKProductFootwear){
	         if(compfo.getData("COMPONENT_TYPE_UNTRANSLATED").toString().trim().equals("Measurements")){
	        	continue;
	         }
         }
         //END - Footwear Tech Pack Implementation
         compOid = compfo.getString("OID");
         if(!FormatHelper.hasContent(compOid)){
            compOid = compfo.getString("COMPONENTCLASS") +compfo.getString("COMPONENTID");
            compfo.put("OID",compOid);
         }
         //Create component map for javascript map
         compType1 = compfo.getData("COMPONENT_TYPE");
         if(!FormatHelper.hasContent(compType1)) {
            compType1 = compfo.getData("COMPONENTTYPE");
         }
         compType2 = compfo.getData("COMPONENT_TYPE_UNTRANSLATED");
		 
         if(!FormatHelper.hasContent(compType2)) {
            compType2 = compfo.getData("COMPONENTTYPE");
            compfo.put("COMPONENT_TYPE_UNTRANSLATED",compType2);
         }
         compName =  compfo.getData("NAME");
         if(!FormatHelper.hasContent(compName)){
            compName =  compfo.getData("COMPONENTNAME");
            compfo.put("NAME",compName);
         }
	if(FormatHelper.hasContent(compType2)&&(compType2.contains("Header Page")||compType2.contains("Colorways"))){
			  compfo.put("OID",compType2);
			  compfo.put("NAME",compType2);
			  compfo.put("COMPONENT_TYPE_UNTRANSLATED",compType2);
	}
	//Hypercare-150, TASK-9256 changes for Sample Details Report - start
	if(FormatHelper.hasContent(compType2)&&(compType2.contains("Sample"))){
		  compfo.put("OID",compType2);
		  compfo.put("NAME",compType2);
		  compfo.put("COMPONENT_TYPE_UNTRANSLATED",compType2);
}
//END  //Hypercare-150, TASK-9256 changes for Sample Details Report - end
         compId = compType2 + TYPE_COMP_DELIM + compOid ;
         parentComponents.add(compId);
         //Create map for component multi choice widget
            compName =  compType1 + " " + compName;



         addComponentToMaps(compMap, nameMap, compfo);
         //If component is a BOM, add the bom.getType() to a Collection for the BOM view options
         if("BOM".equalsIgnoreCase(compType2) || "BOL".equalsIgnoreCase(compType2)) {
            tbom = (FlexBOMPart)LCSQuery.findObjectById(compOid );
            if(!bomFlexTypes.contains(tbom.getFlexType())) {
               bomFlexTypes.add(tbom.getFlexType() );
            }
         }
         listOfComp.put(compId, compName);
		 Object component=null;
		 //System.out.println("----compOid--"+compOid);
if(FormatHelper.hasContent(compOid)&&compOid!=null&&!"nullnull".equals(compOid)){
 component = LCSQuery.findObjectById(compOid);}
        LCSDocument document = null;
        boolean isTurntable = false;
        if(component instanceof LCSDocument) {
            document = (LCSDocument) component;
            if(document != null) {
                document = (LCSDocument) ContentHelper.service.getContents(document);
                Vector<?> ads = ContentHelper.getApplicationData(document);
                ApplicationData ad = null;
                String desc = null;
                for (int i = 0; i < ads.size(); i++) {
                    ad = (ApplicationData) ads.elementAt(i);
                    desc = ad.getDescription();
                    desc = (desc == null) ? "" : desc;
                    if (desc.startsWith(MARKUPIMG) && Character.isDigit(desc.charAt(desc.length() - 1))) {
                        compId = compType2 + TYPE_COMP_DELIM + compOid + TYPE_COMP_DELIM + MARKUPIMG;
                        listOfComp.put(compId, compName + markedUpLabel);
                    }
                }
            }
        }

         hasComponentParent = compfo.getString("HAS_COMPONENT_PARENT");
         if (FormatHelper.hasContent(hasComponentParent) && "true".equals(hasComponentParent)) {
             linkedComponents.add(compId);
         }
      }

      if(currentSpec == null){
            currentSpec = appContext.getSpecification();
      }
      Collection<?> childSpecComps = FlexSpecQuery.getChildSpecComponents(currentSpec, false);
      Collection<?> children = FlexSpecQuery.findSpecToSpecLinks(currentSpec);
      gen.addLinkedProdDocs(product, childDocs, childDescDocs);
      if(childSpecComps == null || childSpecComps.isEmpty()){
        //logger.debug("\n\n\nDidn't find any child spec components");
      } else{
        Iterator<?> it = childSpecComps.iterator();
        //logger.debug("\n\n--------------\nChild Spec components");
        while(it.hasNext()){
             compfo = (FlexObject)it.next();
             //logger.debug("compfo:  " + compfo);
             compOid = compfo.getString("OID");

             //Create component map for javascript map
             compType1 = compfo.getData("COMPONENT_TYPE");

             compType2 = compfo.getData("COMPONENT_TYPE_UNTRANSLATED");

             compName =  compfo.getData("NAME");

             //Create map for component multi choice widget
             compName = compType1 + " (" + compfo.getString("PRODUCT_NAME") + ") - " + compName;

             compId = compType2 + TYPE_COMP_DELIM + compOid ;

             if(parentComponents.contains(compId)){
                //If the parent spec has this component, do nothing
                //logger.debug("//If the parent spec has this component, do nothing");
                continue;
             }
             //logger.debug("compOid:  " + compOid);
             if(nameMap.containsKey(compOid)){
                String tempName = (String)nameMap.get(compOid);
                //logger.debug("if(nameMap.containsKey(compOid))" + tempName);
                if(FormatHelper.hasContent(tempName) && tempName.indexOf(compfo.getString("PRODUCT_NAME")) == -1){
                    //logger.debug("if(FormatHelper.hasContent(tempName) && tempName.indexOf(compfo.getString(PRODUCT_NAME)) == -1)");
                    compName = tempName.substring(0, tempName.indexOf(") -") ) + ", " + compfo.getString("PRODUCT_NAME") +  tempName.substring(tempName.indexOf(") -"));
                }
             } else {

                 //If component is a BOM, add the bom.getType() to a Collection for the BOM view options
                 if("BOM".equalsIgnoreCase(compType2) ||"BOL".equalsIgnoreCase(compType2)) {
                    tbom = (FlexBOMPart)LCSQuery.findObjectById(compOid );
                    if(!bomFlexTypes.contains(tbom.getFlexType())) {
                       bomFlexTypes.add(tbom.getFlexType() );
                    }
                 }
             }
             compfo.put("NAME", compName);
             addComponentToMaps(compMap, nameMap, compfo);
             childListOfComp.put(compId,compName);

        }
      }

      FlexObject fo;
      for (Iterator<?> it = children.iterator(); it.hasNext();) {
          fo = (FlexObject)it.next();
          compOid = fo.getData("FLEXSPECIFICATION.IDA3MASTERREFERENCE");
          gen.addDocumentsToMap(compOid, childDocs, false);
          gen.addSpecDescDocsToMap(compOid, childDescDocs);
      }
  }else {
      compType1 = "";
      compType2 = "";
      compName = "";
      compOid = "";
      boolean isOwner = true;
   //If we only have one component do everything manually
      WTObject wo = (WTObject)LCSQuery.findObjectById(componentId);
      compOid = componentId;
      if(wo instanceof FlexBOMPart){
         tbom = (FlexBOMPart)wo;
         compName = tbom.getName();
         if(!("LABOR".equals(tbom.getFlexType())) ){
            compType1 = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "BOM_short", RB.objA ) ;
            compType2= "BOM";
         }else {
            compType1 = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "BOL_short", RB.objA ) ;
            compType2= "BOL";
         }
         isOwner = FormatHelper.getObjectId((LCSPartMaster)product.getMaster()).equals(FormatHelper.getObjectId(tbom.getOwnerMaster()));
         bomFlexTypes.add(tbom.getFlexType() );

      }else if(wo instanceof LCSMeasurements){
    	//START - Footwear TechPack Implementation
		  if(!isRBKProductFootwear){
			 LCSMeasurements me = (LCSMeasurements)wo;
			 compName = me.getMeasurementsName();
			 compType1 = me.getFlexType().getRootType().getTypeDisplayName();
			 compType2 = "Measurements";
		  }
		//END - Footwear TechPack Implementation

      }else if(wo instanceof LCSConstructionInfo){
         LCSConstructionInfo ci = (LCSConstructionInfo)wo;
         compName = ci.getConstructionInfoName();
         compType1 = ci.getFlexType().getRootType().getTypeDisplayName();
         compType2 ="Construction";

      }else if(wo instanceof LCSDocument){
         LCSDocument doc = (LCSDocument)wo;
         compName = doc.getName();
         compType1 = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "impComponent_LBL", RB.objA ) ;
         compType2 = "Images Page";
      }
      //Hypercare-150, TASK-9256 changes for Sample Details Report - start
      else if(wo instanceof LCSSample){
  			 LCSSample sample = (LCSSample)wo;
  			 compName = sample.getSampleName();
  			 compType1 = sample.getFlexType().getRootType().getTypeDisplayName();
  			 compType2 = "Sample";

        }
      //Hypercare-150, TASK-9256 changes for Sample Details Report - end
      //Since there is only one component, select it by default.
      compName =  compType1 + " " + compName;
      compId = compType2 + TYPE_COMP_DELIM + compOid ;
      componentSelectedValues = compId;
      listOfComp.put(compId,compName);
      if (!isOwner) {
        linkedComponents.add(compId);
      }

  }


    Map<String,String> formats = new HashMap<String,String>();
    formats.put("none", " ");
    formats.put(FormatHelper.FRACTION_FORMAT, fractionLabel +" "+ baseUOM);
    formats.putAll(FormatHelper.toMap(UOMs, RB.UOMRENDERER));


logger.debug("***compMap: " + compMap);
logger.debug("**listOfComp: " + listOfComp);
logger.debug("**nameMap: " + nameMap);
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JAVSCRIPT ///////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<script language="JavaScript" src="<%=URL_CONTEXT%>/javascript/moa.js"></script>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/specPrinter.js"></script>
<script>
var CLEAR_CELL_VALUE = '<%= CLEAR_CELL_VALUE%>';


document.CHOOSE_SINGLE_SPEC = new Object();
document.CHOOSE_SINGLE_SPEC.childSpecComponentIds = new Array();
document.CHOOSE_SINGLE_SPEC.childSpecComponentNames = new Array();
document.CHOOSE_SINGLE_SPEC.childSpecDocIds = new Array();
document.CHOOSE_SINGLE_SPEC.childSpecDocNames = new Array();
document.CHOOSE_SINGLE_SPEC.childSpecDescDocIds = new Array();
document.CHOOSE_SINGLE_SPEC.childSpecDescDocNames = new Array();
var linkedComponents = new Array();
<% String tempid = null;
    int incre = 0;
for(Iterator it = childListOfComp.keySet().iterator();it.hasNext(); ){
        tempid = (String)it.next();
        incre++;
%>
document.CHOOSE_SINGLE_SPEC.childSpecComponentIds[<%= incre %>] = "<%=tempid%>";
document.CHOOSE_SINGLE_SPEC.childSpecComponentNames[<%= incre %>] = '<%= FormatHelper.encodeForJavascript((String)childListOfComp.get(tempid))%>';
<% }
    incre = 0;
    for (Iterator it = childDocs.keySet().iterator(); it.hasNext();) {
        tempid = (String)it.next();
        incre++;
%>
document.CHOOSE_SINGLE_SPEC.childSpecDocIds[<%= incre %>] = "<%=tempid%>";
document.CHOOSE_SINGLE_SPEC.childSpecDocNames[<%= incre %>] = '<%= FormatHelper.encodeForJavascript((String)childDocs.get(tempid))%>';
<% }

    incre = 0;
    for (Iterator it = childDescDocs.keySet().iterator(); it.hasNext();) {
        tempid = (String)it.next();
        incre++;
%>
document.CHOOSE_SINGLE_SPEC.childSpecDescDocIds[<%= incre %>] = "<%=tempid%>";
document.CHOOSE_SINGLE_SPEC.childSpecDescDocNames[<%= incre %>] = '<%= FormatHelper.encodeForJavascript((String)childDescDocs.get(tempid))%>';
<% } %>

BOM_IDENTIFIER = "<%= BOM_IDENTIFIER%>";
<%
    int ii =0;
    for (Iterator it = linkedComponents.iterator(); it.hasNext();) {
%>

linkedComponents[<%=ii%>]="<%=(String)it.next()%>";
<%
ii++;
}
%>

function createMOA(group)
{
    var group = document.getElementsByName(group);
    var ids = '';
            // GROUP THE SELECED IDS INTO A MOA STRING
            for (var k =0; k < group.length; k++)  {
                if (group[k].checked)   {
                       ids = buildMOA(ids, group[k].value);
                }
            }
            return ids;

}
function createMOAViewSelect()
{
    var group = document.getElementsByName('viewSelect');
    var ids = ' ';
            // GROUP THE SELECED IDS INTO A MOA STRING
            for (var k =0; k < group.length; k++)  {
                if (group[k].value!=' ')   {
                       ids = buildMOA(ids, group[k].value);
                }
            }
            return ids;
}


    var compMap = new Array();
    var docMap = new Array();
    var descDocMap = new Array();
    var ctMap;
    var dMap;
<%
    Collection comptypes  = compMap.keySet();
    Iterator cti = comptypes.iterator();
    Collection ids = null;
    Iterator idi = null;
    String id = "";
    String compType = "";
    while(cti.hasNext()){
        compType = (String)cti.next();
%>
         ctMap = new Array();
<%

        ids = (Collection)compMap.get(compType);
        if(ids != null){
            idi = ids.iterator();
            while(idi.hasNext()){
                id = (String)idi.next();
%>
        ctMap['<%= id %>'] = '<%= FormatHelper.encodeForJavascript((String)nameMap.get(id)) %>';

<%
            }
        }
%>
        compMap['<%= compType %>'] = ctMap;

<%
    }
%>

     var bomReports = new Array();
     var defMap2 = new Array();

<%
    String defView2 = null;
    Iterator viewIter2 = specRequests.iterator();
    while(viewIter2.hasNext()){
        defView2 = (String)viewIter2.next();
        Collection<String> repPages = SingleSpecPageUtil.getPageOptions(defView2);
%>
            defMap1 = new Array();
            dMap = new Array();
<%
        if(!repPages.isEmpty()){

            for(String repPage: repPages){
%>
            defMap1['<%=repPage%>'] = '<%=repPage%>';
<%          }%>
            bomReports['<%=defView2%>'] = defMap1;

<%
        }
        Collection<String> requestDocs = gen.getRequestDocs(defView2);
        for (Iterator it = listOfDocs.keySet().iterator(); it.hasNext(); ) {
            id = (String)it.next();
            if (requestDocs.contains(id)) {
%>
                dMap['<%= id%>'] = '<%= FormatHelper.encodeForJavascript((String)listOfDocs.get(id)) %>';
<%
            }
        }
%>
        docMap['<%= defView2%>'] = dMap;
        dMap = new Array();
<%
        for (Iterator it = listOfDescDocs.keySet().iterator(); it.hasNext(); ) {
            id = (String)it.next();
            if (requestDocs.contains(id)) {
%>
               dMap['<%= id%>'] = '<%= FormatHelper.encodeForJavascript((String)listOfDescDocs.get(id)) %>';
<%
            }
        }
%>
        descDocMap['<%= defView2%>'] = dMap;
        dMap = new Array();
<%
        for (Iterator it = childDocs.keySet().iterator(); it.hasNext(); ) {
            id = (String)it.next();
            if (requestDocs.contains(id)) {
%>
                dMap['<%= id%>'] = '<%= FormatHelper.encodeForJavascript((String)childDocs.get(id)) %>';
<%
            }
         }
%>
        docMap['child' + '<%= defView2%>'] = dMap;
        dMap = new Array();
<%
        for (Iterator it = childDescDocs.keySet().iterator(); it.hasNext(); ) {
            id = (String)it.next();
            if (requestDocs.contains(id)) {
%>
                dMap['<%= id%>'] = '<%= FormatHelper.encodeForJavascript((String)childDescDocs.get(id)) %>';
<%
            }
         }
%>
        docMap['childDesc' + '<%= defView2%>'] = dMap;
<%
    }
%>

 function changePartFilter(){

     if(document.MAINFORM.exportedIndentedBOMbox.checked==true){
        document.MAINFORM.partFilter.disabled = false;
        document.MAINFORM.exportedIndentedBOM.value = "true";
     }else{
        document.MAINFORM.partFilter.disabled = true;
        document.MAINFORM.exportedIndentedBOM.value = "false";
     }
 }


 function selectSpecificationPages(size2exists){
      params = new Array();
      var chosenMset=new Array();
	  var chosenSamples=new Array();

     //Sizes
     //Size 1
     document.MAINFORM.size1dataVal.value = createMOA('size1data');
     var size1Val =  document.MAINFORM.size1dataVal.value;
     params[SIZE1_DATA] = size1Val;

     //Size2
    document.MAINFORM.size2dataVal.value = createMOA('size2data');
    var size2Val = document.MAINFORM.size2dataVal.value;
    params[SIZE2_DATA] = size2Val;


      //Source
      var sourceVal = document.MAINFORM.source.value;
      params[SOURCE] = sourceVal;

     //Spec Pages
     var specPagesVal = document.MAINFORM.specPages.value;
	 
     var availDocsValue = document.MAINFORM.availDocs.value;
     var availDescDocsValue = document.MAINFORM.availDescDocs.value;
     var availEPMDocsValue = document.MAINFORM.availEPMDOCDocs.value;
     var availPartsValue = document.MAINFORM.availParts.value;

     //Report pages
	 var sampleRequestCommentsFlag;
	 var sapleFitPhotoCorrection;
	 var sampleRequestFitDetails;
     var gradeFlag;
     var fitSpecFlag;
     var docs = document.MAINFORM.pageOptions.value;
     var newDocsArray = docs.split("|~*~|");
     for(var i=0; i<newDocsArray.length; i++){
         if(newDocsArray[i] == "Measurements:  gradingReport"){
             gradeFlag = true;
             break;
         }
         else if(newDocsArray[i] == "Measurements:  fitSpecReport")
         {
             fitSpecFlag = true;
             break;
         }
	 //changes for RM Task# 10671 - start
		  else if(newDocsArray[i] == "Sample: sampleRequestComments"){
             sampleRequestCommentsFlag = true;
             break;
         }
		  else if( newDocsArray[i] == "Sample: sampleFitPhotoCorrectionsImagePage")
         {
             sapleFitPhotoCorrection = true;
             break;
         }
		  else if(newDocsArray[i] == "Sample: sampleRequestFitDetails")
         {
             sampleRequestFitDetails = true;
             break;
         }
	 //changes for RM Task# 10671 - end
         else{
             gradeFlag = false;
             fitSpecFlag = false;
         }

      }





     //alert("caddoc? "+ document.MAINFORM.pageOptions.value.indexOf('<%=PDFCadDocVariationsGenerator.VARIATIONS_REPORT%>'));
     if(document.MAINFORM.pageOptions.value.indexOf('<%=PDFCadDocVariationsGenerator.VARIATIONS_REPORT%>') > -1
             && !hasContent(availEPMDocsValue)){
            alert("<%= FormatHelper.encodeForJavascript(WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "noCadDocsSelected_LBL", RB.objA )) %>");
            return;
     }

     if(document.MAINFORM.pageOptions.value.indexOf('<%=PDFPartVariationsGenerator.VARIATIONS_REPORT%>') > -1
             && !hasContent(availPartsValue)){
            alert("<%= FormatHelper.encodeForJavascript(WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "noPartsSelected_LBL", RB.objA )) %>");
            return;
     }

     else if(!hasContent(specPagesVal) && !hasContent(availDocsValue + availDescDocsValue+availEPMDocsValue+availPartsValue)){
        alert("<%= FormatHelper.encodeForJavascript(WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "noComponentsSelected_LBL", RB.objA )) %>");
        return;
     }

     //validation to check if sizes doesn't exist and BOM is selected as component or any document is selected as reference document
     else if(!hasContent(size1Val)&&!hasContent(size2Val)){
         var compSelected;
         var flagValue= false;
         compSelected=specPagesVal.split("|~*~|");
         for (var i = 0; i < compSelected.length; i++){
                if(compSelected[i].trim()!=""&&compSelected[i]!=null){
                	//Hypercare-150, TASK-9256 changes for Sample Details Report
                    if(compSelected[i].substring(0, 3)=="BOM" ||compSelected[i].substring(0, 3)=="Ima" ||compSelected[i].substring(0, 3)=="Con"||compSelected[i].substring(0, 3)=="Col"||compSelected[i].substring(0, 3)=="Hea" || compSelected[i].substring(0, 3)=="SAM"){
                    	flagValue=true;
                    }else{
                        flagValue=false;
                        break;
                    }
                }else if(hasContent(availDocsValue + availDescDocsValue+availEPMDocsValue+availPartsValue)){
                    flagValue = true;
                }
         }
		<%
		//START - Footwear TechPack Implementation
		if(!isRBKProductFootwear) {%>
         if(!fitSpecFlag){
            if(!flagValue){
             alert("<%= FormatHelper.encodeForJavascript(WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "noSizesSelected_LBL", RB.objA )) %>");
             return;
         }
        }
		<%}
		//END - Footwear TechPack Implementation
		
		%>
    }

     //validation to check if measurement sets exist and correct sizes exist for respective selected measurement sets
     else if(getSelectedmeasurmentSetData){
         var sizeArray= new Array();
         var selectedsize;
         var flag= false;
         var flagValue=false;
         var msetName="";
         var msetValues= new Array();
         msetValues=specPagesVal.split("|~*~|");
         for (var i = 0; i < msetValues.length; i++){
            if(msetValues[i].length!=1){
                var length = "Measurements-:-VR:".length;
                if(msetValues[i].indexOf("Measurements-:-VR:") > -1){
                chosenMset[i] = msetValues[i].substring(length,msetValues[i].length);
                }
            }
         }

         var myApp = new Map();
         myApp = getMsetData(specPagesVal);


        var finalMap = new Map();
        var selectedSizes = new Array();
        selectedSizes = size1Val.split("|~*~|");
        var flag = false;
        myApp.forEach(function (key, value){

                 for(var i=0; i<value.length;i++){
                       for(j=0;j<selectedSizes.length;j++){
                             if(value[i] == selectedSizes[j]){
                                   flag = true;
                                   j = selectedSizes.length;
                             }else{
                               flag = false;
                             }
                        }
                        if(flag == true){
                         i = value.length;
                        }
                      }

                      if(key!=""){
                       finalMap.set(key, flag);
                      }
            });


        finalMap.forEach(function(key, value){
            if(!key){
                msetName=value;
                flagValue=true;
            }
        });

        if(flagValue){
         var val=getMeasurmentsetname(msetName);
         alert("<%= FormatHelper.encodeForJavascript(WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "noMeasurmentSizesSelected_LBL", RB.objA )) %>"+" "+val);
         return;
        }

     }

     //validation to check if grade report is selected and no measurement set is selected as component
     if(gradeFlag && chosenMset.length<=0){
         alert("<%= FormatHelper.encodeForJavascript(WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "noMeasurmentComponentsSelected_LBL", RB.objA )) %>");
         return;
     }

	// sample  start
         var selectedsize;
         var flag= false;
         var flagValue=false;
         var msetName="";
         var sampleValues= new Array();
         sampleValues=specPagesVal.split("|~*~|");
         for (var i = 0; i < sampleValues.length; i++){
            if(sampleValues[i].length!=1){
                var slength = "Sample-:-VR:".length;
                if(sampleValues[i].indexOf("Sample-:-OR:") > -1){
                chosenSamples[i] = sampleValues[i].substring(slength,sampleValues[i].length);
				}
				
			}
		 }
	 //changes for RM Task# 10671 - start
	if(sampleRequestCommentsFlag && chosenSamples.length<=0){
         alert(" You must add a sample component when the Sample: Sample Request Comments Report is selected ");
         return;
     }
	 
	 else if(sapleFitPhotoCorrection && chosenSamples.length<=0){
         alert(" You must add a sample component when the Sample: Sample Fit Photo/Corrections Report is selected ");
         return;
     }
	 
	 else if(sampleRequestFitDetails && chosenSamples.length<=0){
         alert(" You must add a sample component when the Sample: Sample Request Fit Details Report is selected ");
         return;
     }
     //changes for RM Task# 10671 - end

     params[SPEC_PAGES] = specPagesVal;
	
     params[AVAIL_DOCS] = availDocsValue + "|~*~|" + availDescDocsValue;

     //Colorways
     document.MAINFORM.colorwaydataVal.value = createMOA('colorwaydata');
     var colorwayVal = document.MAINFORM.colorwaydataVal.value;
     params[COLORWAYS_DATA] = colorwayVal;



     //Destination
     if(document.MAINFORM.destinationdata){
        document.MAINFORM.destinationdataVal.value = createMOA('destinationdata');
        var destinationsVal = document.MAINFORM.destinationdataVal.value;
        params[DESTINATION_DATA] = destinationsVal;
     }

     //Page Options
     var pageOptionsVal = document.MAINFORM.pageOptions.value;
     params[PAGE_OPTIONS] = pageOptionsVal;

     //Selected Views
     document.MAINFORM.viewSelectedVal.value = createMOAViewSelect();
     var viewSelectVal = document.MAINFORM.viewSelectedVal.value;
     params[SELECTED_VIEWS] = viewSelectVal;

     //Colorways Per Page
     var numColorwaysPerPageVal = document.MAINFORM.numColorwaysPerPage.value
     params[COLORWAYS_PER_PAGE] = numColorwaysPerPageVal;

     //Sizes Per Page
     var numSizesPerPageVal = document.MAINFORM.numSizesPerPage.value;
     params[SIZES_PER_PAGE] = numSizesPerPageVal;


     //Use Size1/Size2
     params[USE_SIZE1_SIZE2] = document.MAINFORM.useSize1Size2.value;

     //Show Swatches
     params[SHOW_COLOR_SWATCHES] = document.MAINFORM.showColorSwatches.value;

     //includeBOMOwnerVariations
     params[INCLUDE_BOM_OWNER_VARIATIONS] = document.MAINFORM.includeBOMOwnerVariations.value;

     //includeMeasurementsOwnerSizes
     //Setting the flag to false because the related checkbox has been removed.
     params[INCLUDE_MEASUREMENTS_OWNER_SIZES] = false; //document.MAINFORM.includeMeasurementsOwnerSizes.value;

     //Show Material Thumbnails
     params[SHOW_MATERIAL_THUMBNAIL] = document.MAINFORM.showMaterialThumbnail.value;

     params[UOM] = document.MAINFORM.uom.value;

     <%if (ACLHelper.hasViewAccess(defaultDocumentVaultType)&&ACLHelper.hasCreateAccess(defaultDocumentVaultType)){ %>
         params[DOCUMENTVAULT] = document.MAINFORM.documentVault.value;
         params[VAULTDOCUMENTTYPEID] = document.MAINFORM.vaultDocumentTypeId.value;
         params[ASYNCH_GENERATION] = document.MAINFORM.asynchronousGeneration.value;
         params[BACKGROUND_TECHPACK]=document.MAINFORM.asynchronousGeneration.value;
     <%}%>
     params[INCLUDE_SECONDARY_CONTENT] = document.MAINFORM.includeSecondaryContent.value;
     //add for story B-58112 03/28/2011
     var sinceDate = getSelectedDate();
     document.MAINFORM.showChangeSince.value = formatDateString(sinceDate, '%Y/%m/%d');
     params[SHOW_CHANGE_SINCE]= document.MAINFORM.showChangeSince.value;
    <%-- B-92545
    params[SPEC_CAD_FILTER]=document.MAINFORM.cadDocFilter.value;
    --%>
    params[SPEC_CAD_DOCS]=document.MAINFORM.availEPMDOCDocs.value;
    //spec to parts
    params[SPEC_PARTS]= availPartsValue;
    params[SPEC_PART_FILTER]=document.MAINFORM.partFilter.value;

    params[SHOW_INDENTED_BOM]=document.MAINFORM.exportedIndentedBOM.value;

        // EVALUATE THE RETURN CALL
        var returnMethod = '<%= FormatHelper.encodeForJavascript(returnMethod) %>';

        var returnId = '<%= FormatHelper.encodeForJavascript(returnId) %>';

        var methodCall = 'opener.' + returnMethod + '(returnId, params)';
        //var methodCall = 'opener.' + returnMethod + '(returnId, specPagesVal, skusVal, size1Val, size2Val, destinationsVal, bomOptionsVal, viewSelectVal, numColorwaysPerPageVal, numSizesPerPageVal)';

        eval(methodCall);
   }

////////////////////////////////////////////////////////////////////////////////////////////////////////////
function select(){
      var group = document.MAINFORM.selectedIds;
      var checked = false;
      var ids = 'noneSelected';

      // GROUP THE SELECED IDS INTO A MOA STRING
      for (var k =0; k < group.length; k++)  {
         if (group[k].checked)   {
            if (ids=='noneSelected') {
              ids='';
            }
            ids = buildMOA(ids, group[k].value);

         }
      }
   }
////////////////////////////////////////////////////////////////////////////////////////////////////////////
function toggleSelect(allcheck, namecheck){
        var selectAllCheckbox = document.getElementsByName(allcheck);
        var checkboxcheck = document.getElementsByName(namecheck);


        for (i=0; i<checkboxcheck.length; i++) {

            if (selectAllCheckbox[0].checked && !checkboxcheck[i].disabled) {
                checkboxcheck[i].checked = true;

            } else {
                checkboxcheck[i].checked = false;
            }


        }
    }



function chooseView(view) {

// Clear existing Data if a NEW view is chosen


//if (document.MAINFORM.printLayout.value!=' ')
//{
    if(<%= allComponents%>){
        document.MAINFORM.specPages.value = ' ';
    }
    document.MAINFORM.pageOptions.value = ' ';

    var specPagesChosen = document.MAINFORM.specPagesChosen;
    var pageOptionsChosen = document.MAINFORM.pageOptionsChosen;
    var availDocsChosen = document.MAINFORM.availDocsChosen;
    var availDescDocsChosen = document.MAINFORM.availDescDocsChosen;
    var availEPMDocsChosen = document.MAINFORM.availEPMDOCDocsChosen;
    var availablePartsChosen = document.MAINFORM.availPartsChosen;


    if(<%= allComponents%>){
       for(i = 0; i < specPagesChosen.options.length; i++){
            specPagesChosen.remove(i--);
       }
    }

    if (availDescDocsChosen && availDescDocsChosen.options) {
        for(i = 0; i < availDescDocsChosen.options.length; i++){
            availDescDocsChosen.remove(i--);
        }
    }

    if (availDocsChosen && availDocsChosen.options) {
        for(i = 0; i < availDocsChosen.options.length; i++){
            availDocsChosen.remove(i--);
        }
    }

    if (availEPMDocsChosen && availEPMDocsChosen.options) {
        for(i = 0; i < availEPMDocsChosen.options.length; i++){
            availEPMDocsChosen.remove(i--);
        }
    }

    if (availablePartsChosen && availablePartsChosen.options) {
        for(i = 0; i < availablePartsChosen.options.length; i++){
            availablePartsChosen.remove(i--);
        }
    }

    for(i=0; i < pageOptionsChosen.options.length; i++) {
         pageOptionsChosen.remove(i--);
    }

    document.getElementById('numColorwaysPerPageInput').value = '';

    document.getElementById('numSizesPerPageInput').value = '';

    document.getElementById('showColorSwatchesbox').checked = false;
    document.getElementById('showColorSwatches').value = 'false';

    document.getElementById('showMaterialThumbnailbox').checked = false;
    document.getElementById('showMaterialThumbnail').value = 'false';

    var icsb = document.getElementById('includeChildSpecsbox');
    if(icsb != null){
       icsb.checked = <%=DEFAULT_CHILD_SPECS_CHECKED%>;
       includeChildSpecsfunction();
       includeChildDocsFunction();
       includeChildDescDocsFunction();
    }

    var iscb = document.getElementById('includeSecondaryContentbox');
    if(iscb != null) {
        iscb.checked = false;
    }

    document.getElementById('uom').value = 'none';

         var acb = document.getElementById('allColorwaysbox')
         if(acb != null){
            acb.checked = <%= DEFAULT_ALL_COLORWAYS_CHECKED%>;
            toggleSelect('allColorwaysbox', 'colorwaydata');
         }

         var asb = document.getElementById('allSizesbox');
         if(asb != null){
            asb.checked = <%= DEFAULT_ALL_SIZES_CHECKED%>;
            selectAllSizes();
         }

         var asb2 = document.getElementById('allSizes2box');
         if(asb2 != null){
            asb2.checked = <%= DEFAULT_ALL_SIZES_CHECKED%>;
            selectAllSizes2();
         }


         var adb = document.getElementById('allDestinationsbox');
         if(adb != null){
            adb.checked = <%= DEFAULT_ALL_DESTINATIONS_CHECKED%>;
            toggleSelect('allDestinationsbox', 'destinationdata');
         }

         var uss = document.getElementById('useSize1Size2');
         if(uss != null){
            uss.value = 'size1';
         }
        <% Iterator bomOptionsIt1 = bomOptions.iterator();
   Iterator bomFlexTypesIt1;
   AttributeValueList sectionAttList1;
   Collection section1 = new ArrayList();
   Iterator sectionItr1;
   String editBomActivity1 = "EDIT_BOM";
   Collection reportColumns1 = new ArrayList();
   Map reportOptions1 = new HashMap();
   String defaultViewId1 = "";
   String bomOption1 = "";
   FlexType bomFlexType1;
   FlexObject fobj1;
   String bomFlexTypeId1 = "";
   if( (bomOptions != null && bomOptions.size() >0) && (bomFlexTypes != null && bomFlexTypes.size() > 0)) {
   while(bomOptionsIt1.hasNext()) {
      bomOption1 = (String)bomOptionsIt1.next();
      bomFlexTypesIt1 = bomFlexTypes.iterator();
      while(bomFlexTypesIt1.hasNext()) {
         bomFlexType1 = (FlexType)bomFlexTypesIt1.next();
         bomFlexTypeId1 = FormatHelper.getNumericObjectIdFromObject(bomFlexType1);
          defaultViewId1 = lcsContext.viewCache.getDefaultViewId(FormatHelper.getObjectId(bomFlexType1), editBomActivity1);

               sectionAttList1 = bomFlexType1.getAttribute("section").getAttValueList();
               section1 = sectionAttList1.getSelectableKeys(lcsContext.getContext().getLocale(), true);

               sectionItr1 = section1.iterator();


               while (sectionItr1.hasNext()){
                  String secID1 = (String)sectionItr1.next();
                    String selectedViewId1 = null;
                    if(FormatHelper.hasContent(defaultViewId1)){
                     selectedViewId1 = bomOption1 + TYPE_COMP_DELIM + FormatHelper.getObjectId(bomFlexType1)+ TYPE_COMP_DELIM + secID1 + TYPE_COMP_DELIM+"ColumnList"+ defaultViewId1.substring(defaultViewId1.lastIndexOf(":"));
                    }
                    %>
         var bomviews=document.getElementsByName('viewSelect');
         for(var i=0;i<bomviews.length;i++){
             var  bomsection=bomviews[i];
             for(var j=0;j<bomsection.options.length;j++){
                                var bomoption=bomsection.options[j].value;
                                  if((hasContent(bomoption))&&(bomoption.indexOf('<%=secID1%>')>-1 )&& (bomoption.indexOf('<%=bomOption1%>')>-1)){

                                    bomsection.value='<%=selectedViewId1%>';
                                }
                            }

         }
         <%

               }
         }
        }
   }

   %>

//}

//End clearing of values



<%
    String defView = "";

    Iterator viewIter = specRequests.iterator();
    boolean first = true;
    while(viewIter.hasNext()){
        defView = (String)viewIter.next();
%>
        //add for SPR# 2063313 03/31/2011
        if (view == " ") {
            updatePage();
        }

        <% if(first){ %>
            if(view == '<%=defView%>'){
        <%} else {%>
            } else if(view == '<%=defView%>'){
        <% } %>

                //for(var n in compMap){
                    //alert(n);
                //}

        document.getElementById('numColorwaysPerPageInput').value = '<%=SingleSpecPageUtil.getNumColorwaysPerPage(defView)%>';

        document.getElementById('numSizesPerPageInput').value = '<%=SingleSpecPageUtil.getNumSizesPerPage(defView)%>';

        document.getElementById('showColorSwatchesbox').checked = <%=SingleSpecPageUtil.getShowColorSwatch(defView)%>;
        document.getElementById('showColorSwatches').value = '<%=SingleSpecPageUtil.getShowColorSwatch(defView)%>';

        document.getElementById('showMaterialThumbnailbox').checked = <%=SingleSpecPageUtil.getShowMaterialThumb(defView)%>;
        document.getElementById('showMaterialThumbnail').value = '<%=SingleSpecPageUtil.getShowMaterialThumb(defView)%>';


        var icsb = document.getElementById('includeChildSpecsbox');
        if(icsb != null){
            icsb.checked = <%=SingleSpecPageUtil.getIncludeChildSpecs(defView)%>;
            includeChildSpecsfunction();
            if (document.MAINFORM.availDocsOptions) {
               var docD = docMap['child' + '<%= defView %>'];
               addToChosenList(docD, document.MAINFORM.availDocsOptions, document.MAINFORM.availDocsChosen, document.MAINFORM.availDocs);
            }
            if (document.MAINFORM.availDescDocsOptions) {
                var docD = descDocMap['child' + '<%= defView %>'];
                addToChosenList(docD, document.MAINFORM.availDescDocsOptions, document.MAINFORM.availDescDocsChosen, document.MAINFORM.availDescDocs);
             }
        }

        var iscb = document.getElementById('includeSecondaryContentbox');
        if(iscb != null){
            iscb.checked = <%=SingleSpecPageUtil.getIncludeSecondaryContent(defView)%>;
            setIncludeSecondary(iscb);
        }
        includeChildDocsFunction();
        includeChildDescDocsFunction()

         document.getElementById('uom').value = '<%=SingleSpecPageUtil.getMeasureUOM(defView)%>';

         var acb = document.getElementById('allColorwaysbox');
         if(acb != null){
             acb.checked = <%= SingleSpecPageUtil.getCheckAllColorways(defView)%>;
             toggleSelect('allColorwaysbox', 'colorwaydata');
         }

         var asb = document.getElementById('allSizesbox');
         if(asb != null){
             asb.checked = <%= SingleSpecPageUtil.getCheckAllSizes(defView)%>;
             selectAllSizes();
         }

         var asb2 = document.getElementById('allSizes2box');
         if(asb2 != null){
             asb2.checked = <%= SingleSpecPageUtil.getCheckAllSizes(defView)%>;
             selectAllSizes2();
         }

         var adb = document.getElementById('allDestinationsbox');
         if(adb != null){
             adb.checked = <%= SingleSpecPageUtil.getCheckAllDestinations(defView)%>;
             toggleSelect('allDestinationsbox', 'destinationdata');
         }

         var uss = document.getElementById('useSize1Size2');
         if(uss != null && uss.type!='hidden'){
             uss.value = '<%=SingleSpecPageUtil.getUseSize1Size2(defView)%>';
         }

         if (document.MAINFORM.availDocsOptions) {
            var docD = docMap['<%= defView %>'];
            addToChosenList(docD, document.MAINFORM.availDocsOptions,
                document.MAINFORM.availDocsChosen, document.MAINFORM.availDocs);
        }

        if (document.MAINFORM.availDescDocsOptions) {
            var docD = descDocMap['<%= defView %>'];
            addToChosenList(docD, document.MAINFORM.availDescDocsOptions,
                document.MAINFORM.availDescDocsChosen, document.MAINFORM.availDescDocs);
        }
    <%  Iterator bomOptionsIts = bomOptions.iterator();
   Iterator bomFlexTypesIts;
   AttributeValueList sectionAttLists;
   Collection sections = new ArrayList();
   Iterator sectionsItr;
   String editBomActivitys = "EDIT_BOM";
   Collection reportColumn = new ArrayList();
   Map reportOption = new HashMap();
   String defaultView = "";
   String bomOption2 = "";
   FlexType bomFlexType;
   FlexObject fobjs;
   String bomFlexTypeId = "";
   if( (bomOptions != null && bomOptions.size() >0) && (bomFlexTypes != null && bomFlexTypes.size() > 0)) {
   while(bomOptionsIts.hasNext()) {
      bomOption2 = (String)bomOptionsIts.next();
      bomFlexTypesIts = bomFlexTypes.iterator();
     while(bomFlexTypesIts.hasNext()) {
         bomFlexType = (FlexType)bomFlexTypesIts.next();
         bomFlexTypeId = FormatHelper.getNumericObjectIdFromObject(bomFlexType);
         lcsContext.viewCache.clearCache(FormatHelper.getObjectId(bomFlexType), editBomActivitys );
         reportColumn = lcsContext.viewCache.getViews(FormatHelper.getObjectId(bomFlexType), editBomActivitys);
         if(reportColumn != null && reportColumn.size() > 0) {//If we don't have any views, don't draw the div
            Object[] objC = {bomFlexType.getFullNameDisplay(true)};
               sectionAttLists = bomFlexType.getAttribute("section").getAttValueList();
               sections = sectionAttLists.getSelectableKeys(lcsContext.getContext().getLocale(), true);
               sectionsItr = sections.iterator();
               while (sectionsItr.hasNext()){
                  String secID = (String)sectionsItr.next();
                  String bomViewProperty=SingleSpecPageUtil.getBOMView(bomOption2,defView);
                   StringTokenizer sectionToken = new StringTokenizer(
                bomViewProperty, "|~*~|");
                String BomView=null;
                    // validate if values are filled for given attribute keys
                while(sectionToken.hasMoreTokens()) {
                     String token = sectionToken.nextToken();
                     StringTokenizer typeToken = new StringTokenizer(token, ":");
                    if (typeToken.hasMoreTokens()) {
                         String sectionName = typeToken.nextToken();
                          if(secID.equals(sectionName)){
                             BomView= typeToken.nextToken();
                             break;
                        }
                    }
                }
                  reportOption = new HashMap();
                    Iterator ri = reportColumn.iterator();
                    while(ri.hasNext()) {
                     FlexObject fobj = (FlexObject)ri.next();

                    String bomviewselect=bomOption2 + TYPE_COMP_DELIM + FormatHelper.getObjectId(bomFlexType)+ TYPE_COMP_DELIM + secID + TYPE_COMP_DELIM+"ColumnList:"+fobj.getString("COLUMNLIST.IDA2A2");
                    if(fobj.getString("COLUMNLIST.DISPLAYNAME").equals(BomView)){
                    %>
                        var length=document.getElementsByName("viewSelect").length;
                         for(var i=0;i<length;i++){
                            var bomsection=document.getElementsByName("viewSelect")[i];
                             for(var j=0;j<bomsection.options.length;j++){
                                var bomoption=bomsection.options[j].value;
                                  if((hasContent(bomoption))&&(bomoption.indexOf('<%=secID%>')>-1 )&& (bomoption.indexOf('<%=bomOption2%>')>-1)){

                                    bomsection.value='<%=bomviewselect%>';
                                }
                            }
                        }
                    <%
                    }
                }
           }
        }
     }
   }
 }
%>

<%
        boolean includeMarkedupImages = LCSProperties.getBoolean("jsp.specification.ChooseSingleSpecPage2.includeMarkedupImagesContent." + defView, false);
		
        for(String compPage : SingleSpecPageUtil.getSpecPages(defView)){
%>	
            var comps = compMap['<%= compPage %>'];
		
            var includeMarkedupImages = '<%= includeMarkedupImages %>';
		
          
			addCompsToChosenList(comps, document.MAINFORM.specPagesOptions, document.MAINFORM.specPagesChosen, document.MAINFORM.specPages, includeMarkedupImages);
		
            //addToChosenList(comps, document.MAINFORM.specPagesOptions, document.MAINFORM.specPagesChosen, document.MAINFORM.specPages);
<%
        }// end while
 
        //Add BOM Reports section here%>
        var reps = bomReports['<%=defView%>'];

        addToChosenList(reps, document.MAINFORM.pageOptionsOptions, document.MAINFORM.pageOptionsChosen, document.MAINFORM.pageOptions);

<%
        if(!viewIter.hasNext()){
%>
        }
<%
        } // end if
        first=false;

    }// end while
%>
} // end javascript function

/**
 * Logic to handle auto preselection of components to select, original + (markedup version instead of original image when present) for
 * the configured specPages when includeMarkedupImages property is set
 */
function addCompsToChosenList(comps, selectOptions, selectChosen, chosenElem, includeMarkedupImages){
	var cmap = new Map();
	
	for(var n in comps){
		var sOptions = selectOptions.options;
		
        for(var i = 0; i < sOptions.length; i++){
            var sOption = sOptions[i];
		//alert("--soption0--"+sOption.value);
            if(sOption.value.indexOf(n) > -1){
				//alert("--soption0--"+sOption.value);
                const sArr = sOption.value.split("-:-");
                if(includeMarkedupImages == "true"){
                    if(cmap.has(sArr[1])){
                        if(sArr.length>2 && sArr[2].startsWith("MARKUPIMG")){
                            cmap.set(sArr[1], sOption);
                        }
                    }else{
                        cmap.set(sArr[1], sOption);
                    }
                }else{//alert("--sArr--"+sArr[0]);
                    if(sArr.length == 2){
						//alert("--sArr--"+sArr[0]);
						//alert("--sArr--"+sArr[1]);
                        addOption(selectChosen, sOption, false, false);
                        ids = buildMOA(chosenElem.value, sOption.value);
						
                        chosenElem.value = ids;
                    }
                }
            }
        }
    }
    if(cmap.size > 0){
		
        var cvals = cmap.values();
        for (var cval of cvals){
            addOption(selectChosen, cval, true, false);
            ids = buildMOA(chosenElem.value, cval.value);
            chosenElem.value = ids;
        }
    }
    updatePage();
    updateDoc();
}

function addToChosenList(comps, selectOptions, selectChosen, chosenElem){
    for(var n in comps){
        //alert(n);
        var sOptions = selectOptions.options;
        var sOption;
        for(var i = 0; i < sOptions.length; i++){
            sOption = sOptions[i];
            //alert(n + ' : ' + sOption.value);
            if(sOption.value.indexOf(n) > -1){
                addOption(selectChosen, sOption, true, false);
                ids = buildMOA(chosenElem.value, sOption.value);
                chosenElem.value = ids;
            }
        }
    }
    updatePage();
    updateDoc();
}

function addResults(){
    add(document.MAINFORM.resultsMap, document.MAINFORM.specPagesChosen, document.MAINFORM.specPages,
    document.MAINFORM.availDocs, false);
    javascript:updatePage()
}

function selectAllSizes(){
    toggleSelect('allSizesbox', 'size1data');
}

function selectAllSizes2(){
    toggleSelect('allSizes2box', 'size2data');
}

//remove all check on deselect of any of the checkbox
function selectSize(e){
     if(e.id=='size1dataid'){
         var selectAllCheckbox = document.getElementsByName('allSizesbox');
         var checkboxcheck = document.getElementsByName('size1data');
     }
     else if(e.id=='size2dataid'){
         var selectAllCheckbox = document.getElementsByName('allSizes2box');
         var checkboxcheck = document.getElementsByName('size2data');
     }
     else if(e.name=='colorwaydata') {
         var selectAllCheckbox = document.getElementsByName('allColorwaysbox');
         var checkboxcheck = document.getElementsByName('colorwaydata');
     }
     else if(e.name=='destinationdata') {
         var selectAllCheckbox = document.getElementsByName('allDestinationsbox');
         var checkboxcheck = document.getElementsByName('destinationdata');
     }

     if(selectAllCheckbox != undefined && checkboxcheck != undefined) {
         selectAllCheckbox[0].checked =true;
         for (i=0; i<checkboxcheck.length; i++) {
             if(!checkboxcheck[i].checked && checkboxcheck[i].type == "checkbox") {
                selectAllCheckbox[0].checked =false;
                break;
             }
         }
     }
}


function chooseSize()
{
    var size1 = document.getElementsByName('size1databox');
    var size2 = document.getElementsByName('size2databox');
    var allbox = document.getElementsByName('allSizesbox');
    var allbox2 = document.getElementsByName('allSizesbox2');
    var size1Table = document.getElementById('size1');
    var size2Table = document.getElementById('size2');
    var sizeSelectValue = document.MAINFORM.sizeCat.value;

    if(sizeSelectValue=="size1")
    {
        allbox[0].checked = false;
        size1Table.style.display ='block'
        size2Table.style.display ='none';

        for (i=0; i<size1.length; i++)
        {
            size1[i].disabled = false;
        }


        for (i=0; i<size2.length; i++)
        {
            size2[i].checked = false;
            size2[i].disabled = true;
        }
    }

    else if(sizeSelectValue=="size2")
    {
        allbox[0].checked = false;

        for (i=0; i<size2.length; i++)
        {
            size2[i].disabled = false;
        }

        size1Table.style.display ='none'
        size2Table.style.display ='block';

        for (i=0; i<size1.length; i++)
        {
            size1[i].checked = false;
            size1[i].disabled = true;
        }

    }

    else
    {
        allbox[0].checked = false;

        for (i=0; i<size2.length; i++)
        {
            size2[i].disabled = false;
        }

        for (i=0; i<size1.length; i++)
        {
            size1[i].disabled = false;
        }

        size1Table.style.display ='block'
        size2Table.style.display ='block';
    }
}

function updatePage() {
   var specCompOptions = document.MAINFORM.specPagesOptions;
   var specCompSelected = document.MAINFORM.specPagesChosen;
 
   var optionsIndex = specCompOptions.selectedIndex;
   var pageOptionsVar = document.MAINFORM.pageOptionsOptions;
   var pageOptionsChosenVar = document.MAINFORM.pageOptionsChosen;

   var includeBOMOwnerVariationsLbl = document.getElementById("includeBOMOwnerVariationsLbl");
   var includeMeasurementsOwnerSizesLbl = document.getElementById("includeMeasurementsOwnerSizesLbl");


   var division;
   var currentBom;

   // Check to make sure only the boms selected have their views displayed
   var found = false;
   var includedTrackedChanges = false;
   var includedPartData = false;
   var includeBOMOwnerVariations = false;
   //var includeMeasurementsOwnerSizes = false;

    for (var i = 0; i < specCompSelected.length; i++) {
        compVal = specCompSelected.options[i].value;
        for (var j = 0; j < linkedComponents.length; j++) {
            if (compVal == linkedComponents[j]) {
                if (compVal.indexOf("BOM-:-") > -1) {
                    includeBOMOwnerVariations = true;
                }
                /* The related checkbox has been removed
                if (compVal.indexOf("Measurements-:-") > -1) {
                    includeMeasurementsOwnerSizes = true;
                }*/
            }
        }
    }

	//Adding code for removing disbale class by DS
	includeBOMOwnerVariations = true;
    if (includeBOMOwnerVariations) {
        includeBOMOwnerVariationsLbl.getElementsByTagName("font")[0].removeAttribute('class');
        document.getElementById("includeBOMOwnerVariationsbox").disabled = false;

    } else {
        includeBOMOwnerVariationsLbl.getElementsByTagName("font")[0].setAttribute('class','LIGHTER_BAR');
        document.getElementById("includeBOMOwnerVariationsbox").disabled = true;
    }
    /*The related checkbox has been removed
    if (includeMeasurementsOwnerSizes) {
        includeMeasurementsOwnerSizesLbl.getElementsByTagName("font")[0].removeAttribute('class');
        document.getElementById("includeMeasurementsOwnerSizesbox").disabled = false;
    } else {
        includeMeasurementsOwnerSizesLbl.getElementsByTagName("font")[0].setAttribute('class','LIGHTER_BAR');
        document.getElementById("includeMeasurementsOwnerSizesbox").disabled = true;
    }*/


   for(i=0; i<pageOptionsVar.length; i++){
      found = false;
      includedTrackedChanges = false;
      var testVal = pageOptionsVar.options[i].value;
      if  (testVal.indexOf(BOM_IDENTIFIER) == 0){
         if(pageOptionsChosenVar.length > 0){
            for (j=0; j<pageOptionsChosenVar.length; j++){
               if (pageOptionsChosenVar.options[j].value==pageOptionsVar.options[i].value){
                  found = true;
                  break;
               }
            }
            for (j=0; j<pageOptionsChosenVar.length; j++){
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
         var divIdVar = pageOptionsVar.options[i].value;
         divIdVar = divIdVar.substring(BOM_IDENTIFIER.length);
         division = document.getElementById(divIdVar +"div");
         if (found && division){ // if it's not found in the chosen list, remove the views
            division.style.display ='block';
         } else {
            if(division != null){
                division.style.display ='none';
            }
         }

         if (includedTrackedChanges) {
             document.getElementById("divTrackedChanges").style.display='block';
         } else {
             document.getElementById("divTrackedChanges").style.display='none';
         }
         if (includedPartData) {
             document.getElementById("divSpecPart").style.display='block';
             document.MAINFORM.partFilter.disabled = true;
         } else {
             document.getElementById("divSpecPart").style.display='none';
         }
      }
   }
}

function updateDoc() {
}

function includeChildDescDocsFunction()
{
    //alert("includeChildDescDocsFunction");
    //var checkbox = document.MAINFORM.includeChildDocsbox;
    var checkbox = document.MAINFORM.includeChildSpecsbox;
    var docs = document.CHOOSE_SINGLE_SPEC.childSpecDescDocIds;
    var docNames = document.CHOOSE_SINGLE_SPEC.childSpecDescDocNames;
    var docOptions = document.getElementById('availDescDocsOptions');
    var chosenDocs = document.getElementById('availDescDocsChosen');
    if(checkbox != null && checkbox.checked){
        //alert("checked");
        for (i = 1; i < docs.length; i++) {
            addOptionValue(docOptions, docNames[i], docs[i], "true", "true");
        }
    } else {
        //alert("unchecked");
        var removeOptions = "";
        for (i = 1; i < docs.length; i++) {
            //removeOptions = buildMOA(removoeOptions, docs[i]);
            removeOptions = removeOptions + DELIMITER + docs[i];
        }

        removeSelectOptions(docOptions, removeOptions)
        removeSelectOptions(chosenDocs, removeOptions)
    }
}

function includeChildDocsFunction()
{
    //alert("includeChildDocsFunction");
    //var checkbox = document.MAINFORM.includeChildDocsbox;
    var checkbox = document.MAINFORM.includeChildSpecsbox;
    var docs = document.CHOOSE_SINGLE_SPEC.childSpecDocIds;
    var docNames = document.CHOOSE_SINGLE_SPEC.childSpecDocNames;
    var docOptions = document.getElementById('availDocsOptions');
    var chosenDocs = document.getElementById('availDocsChosen');
    if(checkbox != null && checkbox.checked){
        //alert("checked");
        for (i = 1; i < docs.length; i++) {
            addOptionValue(docOptions, docNames[i], docs[i], "true", "true");
        }
    } else {
        //alert("unchecked");
        var removeOptions = "";
        for (i = 1; i < docs.length; i++) {
            //removeOptions = buildMOA(removoeOptions, docs[i]);
            removeOptions = removeOptions + DELIMITER + docs[i];
        }

        removeSelectOptions(docOptions, removeOptions)
        removeSelectOptions(chosenDocs, removeOptions)
    }
}

function includeChildSpecsfunction(){
    //alert("includeChildSpecsfunction");
    var checkbox = document.MAINFORM.includeChildSpecsbox;
    var ids =document.CHOOSE_SINGLE_SPEC.childSpecComponentIds;
    var names = document.CHOOSE_SINGLE_SPEC.childSpecComponentNames;
    var options = document.getElementById('specPagesOptions');
    var chosen = document.getElementById('specPagesChosen');
    if(checkbox.checked){
        //alert("checked");
        for(i = 1;i <ids.length;i++){
            //alert("ids[" + i + " --" + ids[i]);
            addOptionValue(options, names[i], ids[i], "true", "true");
        }
    } else {
        //alert("unchecked");
        var removeOptions = "";
        for(i = 1;i <ids.length;i++){
            removeOptions = buildMOA(removeOptions, ids[i]);
        }
        removeSelectOptions(options, removeOptions)
        removeSelectOptions(chosen, removeOptions)
    }
//chooseView(document.MAINFORM.printLayout.value);
}

function includeChildSpecsAndDocs() {
   includeChildSpecsfunction();
   includeChildDocsFunction();
   includeChildDescDocsFunction();
}

function setIncludeSecondary(includeSecondaryCheckbox){
    if(includeSecondaryCheckbox.checked){
        document.getElementById('includeSecondaryContent').value = 'true';
    } else {
        document.getElementById('includeSecondaryContent').value = 'false';
    }
}

function chooseVaultDocumentType(){
    var typeClass = 'com.lcs.wc.document.LCSDocument';
    var rootTypeId = '<%=FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE))%>';
    launchChooser('<%=URL_CONTEXT%>/jsp/flextype/TypeChooser.jsp?typeclass=' + typeClass + '&rootTypeId='+ rootTypeId + '&accessType=CREATE_ACCESS', fkTypeDisplay, document.MAINFORM.vaultDocumentTypeId);
}

function setDocumentVaultDiv(documentVaultCheckbox){
    if(documentVaultCheckbox.checked){
        document.getElementById('documentVault').value = 'true';
    } else {
        document.getElementById('documentVault').value = 'false';
    }
    if (documentVaultCheckbox.checked){
        showDocumentVaultDiv();
    }else{
        hideDocumentVaultDiv();
    }
}

function showDocumentVaultDiv(){
    document.getElementById('refTypeDiv').style.display ='block';
    document.getElementById('fkTypeDisplay').style.display ='block';
}

function hideDocumentVaultDiv(){
    document.getElementById('refTypeDiv').style.display ='none';
    document.getElementById('fkTypeDisplay').style.display ='none';
}

function setAsynchGen(checkBox){
    var checkBox2 = document.getElementById('documentVaultbox');
    var gen = document.getElementById('asynchronousGeneration');
    if (checkBox.checked) {
        checkBox2.checked = 'true';
        checkBox2.disabled = 'true';
        gen.value = true;
        document.MAINFORM.documentVault.value = true;
        showDocumentVaultDiv();
    } else {
        checkBox2.checked = false;
        checkBox2.disabled = false;
        gen.value = false;
        document.MAINFORM.documentVault.value=false;
        hideDocumentVaultDiv();
    }
}
//add for story B-58112 03/28/2011
function changeSinceDate() {
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
}

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
        if(tStr == ""){                     //Date input is empty
            alert(dateSelectEmpty);
            return false;
        } else  {
            sinceDate = chkDateString(tStr, '', '<%=jsCalendarFormat%>', true);
        }
    } else {
        sinceDate.setDate(sinceDate.getDate() - choice);
    }
    return sinceDate;
}


Event.observe(window,'load',function() {

    if(window.opener.parent && window.opener.parent.headerframe){
        if(window.opener.parent.headerframe.getGlobalChangeTrackingSinceDate) {
            dateStr = window.opener.parent.headerframe.getGlobalChangeTrackingSinceDate();
        }
    } else {
        var today = new Date();
        today.setDate(today.getDate() - 1);
        dateStr = today.print('<%=jsCalendarFormat%>');
    }
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
    updatePage();
});


</script>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// HTML ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<input type="hidden" name="returnMethod" value="<%= FormatHelper.encodeAndFormatForHTMLContent(returnMethod) %>">
<input name="vaultDocumentTypeId" type="hidden" value="">
<input type="hidden" name="colorwaydataVal" value="">
<input type="hidden" name="size1dataVal" value="">
<input type="hidden" name="size2dataVal" value="">
<input type="hidden" name="destinationdataVal" value="">
<input type="hidden" name="viewSelectedVal" value="">


<%-- /////////////////////////////////////////HEADER CODE ////////////////////////////////////////////--%>

<table>
<tr>
    <td class="PAGEHEADINGTITLE">
   <%= pageTitle %>
   <% if (currentSpec != null) { %>
      <%= FormatHelper.encodeAndFormatForHTMLContent(currentSpec.getName()) %>
   <% } else {%>
      <%= FormatHelper.encodeAndFormatForHTMLContent(product.getName())%>
   <% } %>
    </td>
</tr>
</table>
   <% if(FormatHelper.hasContent(errorMessage)) { %>
   <tr>
      <td class = "MAINLIGHTCOLOR" width="100%" border="0">
         <table>
            <tr>
               <td class="ERROR">
                  <%= java.net.URLDecoder.decode(errorMessage, ENCODING) %>
               </td>
            </tr>
         </table>
        </td>
   </tr>
   <% } %>
<%-- /////////////////////////////////////////VARIATION OPTIONS CODE ////////////////////////////////////////////--%>

<%=tg.startGroupBorder()%>
<%=tg.startTable()%>
<%=tg.startGroupTitle()%><%=variationOptionsGrpTle%>
<%=tg.endTitle()%>
<%=tg.startGroupContentTable()%>
<table border='0' halign='center' width='100%'>


	<tr halign='center'>
		<%
		if (needSource) {
		%>
		<td class="LABEL" align='center' nowrap><%=sourceLabel%></td>
		<%
		}
		%>
		<td class="LABEL" align='center' nowrap><%=colorwaysLabel%></td>
		<!--  <td class="LABEL" align='center' nowrap></td>-->
		<%
		if (USE_PRODUCTDESTINATIONS) {
		%>
		<td class="LABEL" align='center' nowrap><%=destinationsLabel%></td>
		<%
		}
		%>
	</tr>
	<tr>
		<%-- //////////////////Source selection/////////////////////////////////--%>
		<%
		String sourceMasterId = "";
		LCSSourcingConfig selectedSource = appContext.getSourcingConfig();
		if (selectedSource != null) {
			selectedSource.getMaster();
			sourceMasterId = FormatHelper.getNumericObjectIdFromObject((WTObject) selectedSource.getMaster());
		}

		if (needSource) {
			Map appContextSources = appContext.getSourcesMap();
			Map sources = new HashMap();

			Iterator i = appContextSources.keySet().iterator();

			String source;
			LCSSourcingConfig sc;
			String sourceId;
			while (i.hasNext()) {
				source = (String) i.next();
				if (FormatHelper.hasContent(source) && source.indexOf("LCSSourceToSeasonLink") > -1) {
					LCSSourceToSeasonLink stsl = (LCSSourceToSeasonLink) LCSQuery.findObjectById(source);
					sourceId = FormatHelper.getNumericObjectIdFromObject((wt.fc.WTObject) stsl.getSourcingConfigMaster());
				} else {
					sc = (LCSSourcingConfig) LCSQuery.findObjectById(source);
					sourceId = FormatHelper.getNumericObjectIdFromObject((wt.fc.WTObject) sc.getMaster());
				}
				sources.put(sourceId, appContextSources.get(source));
			}
		%>
		<td nowrap valign='top' align='center'>
			<table border='0' cellpadding="1" cellspacing="1">
				<col width='15%'>
				<col width="35%">
				<tr>
					<td>&nbsp;</td>
				</tr>
			</table>
			<table>
				<tr>
					<td class="LABEL">&nbsp;</td>
				</tr>
			</table>
			<table cellpadding="1" cellspacing="1" border='0' align='center'>
				<tr><%=fg.createDropDownListWidget(sourceLabel, sources, "source", sourceMasterId, null, false)%></tr>
			</table>
		</td>
		<%
		} else {
		%>
		<input type="hidden" name="source" value="<%=sourceMasterId%>">
		<%
		}
		%>


		<%-- //////////////////Colorway selection/////////////////////////////////--%>
		<%
		Map skuTable = appContext.getSKUsMap();
		
		Map reversed = new HashMap();
		Iterator keys = skuTable.keySet().iterator();
		String key = "";
		System.out.println("-----------skuTable-------------"+skuTable);
		while (keys.hasNext()) {
			key = (String) keys.next();
			reversed.put(skuTable.get(key), key);
		}
		Collection sortedSkus = SortHelper.sortStrings(reversed.keySet());
		//If we have more than 0 skus, draw the section/table
		if (skuTable.size() > 0) {
		%>
		<td nowrap valign='top' align='center'>
			<table border='0' cellpadding="1" cellspacing="1">
				<col width='15%'>
				<col width="35%">
				<tr valign='bottom'>
					<td valign='bottom'><%=fg.createCustomActionBooleanInput("allColorways", allLabel, DEFAULT_ALL_COLORWAYS_CHECKED, "javascipt:toggleSelect(\'allColorwaysbox\', \'colorwaydata\')", false)%></td>
				</tr>
			</table>
			<table>
				<tr>
					<td class="LABEL">&nbsp;</td>
				</tr>
			</table>
			<table cellpadding="1" cellspacing="1" border='0' valign='baseline'
				align='center'>
				<%
				Iterator skuItr = sortedSkus.iterator();
				String temp = "";
				LCSSKU sku = null;
				String numeric = "";
				String label = "";
				while (skuItr.hasNext()) {
					temp = (String) skuItr.next();
					System.out.println("-------temp-----"+(String)reversed.get(temp));
					sku = (LCSSKU) LCSQuery.findObjectById((String) reversed.get(temp));
					numeric = FormatHelper.getNumericFromReference(sku.getMasterReference());
					System.out.println("-------numeric-----"+numeric);
					label = sku.getName();
				%>
				<tr valign='baseline'><%=drawCheckBox("colorwaydata", numeric, label, false)%></tr>
				<%
				}
				%>
			</table>
		</td>
		<script>
			toggleSelect('allColorwaysbox', 'colorwaydata');
		</script>
		<%
		} else {
		%>
		<input type="hidden" name="colorwaydata" value="">
		<td></td>
		<%
		}
		%>


		<%-- //////////////////Destination selection/////////////////////////////////--%>
		<%
		if (USE_PRODUCTDESTINATIONS) {
			Map hash1 = appContext.getProductDestinationsMap();
			Map reversedDest = new HashMap();
			Iterator keysDest = hash1.keySet().iterator();
			String keyDest = "";
			while (keysDest.hasNext()) {
				keyDest = (String) keysDest.next();
				reversedDest.put(hash1.get(keyDest), keyDest);
			}
			Collection sortedDest = SortHelper.sortStrings(reversedDest.keySet());
			Iterator destItr = sortedDest.iterator();
			if (hash1.size() > 0) {
		%>
		<td nowrap valign='top' align='center'>
			<table nowrap cellpadding="1" cellspacing="1" border='0'>
				<col width='15%'>
				<col width="35%">
				<tr>
					<td><%=fg.createCustomActionBooleanInput("allDestinations", allLabel, DEFAULT_ALL_DESTINATIONS_CHECKED, "javascipt:toggleSelect(\'allDestinationsbox\', \'destinationdata\')", false)%></td>
				</tr>
			</table>
			<table>
				<tr>
					<td class="LABEL">&nbsp;</td>
				</tr>
			</table>
			<table cellpadding="1" cellspacing="1" border='0' valign='baseline'
				align='center'>
				<%
				String numeric = "";
				String label = "";
				while (destItr.hasNext()) {
					label = (String) destItr.next();
					numeric = (String) reversedDest.get(label);
				%>
				<tr valign='baseline'><%=drawCheckBox("destinationdata", numeric, label, false)%></tr>
				<%
				}
				%>
			</table>
		</td>
		<%
		} else {
		%>
		<input type="hidden" name="colorwaydata" value="">
		<td></td>
		<% } %>
		<% } %>
		<script>
			toggleSelect('allDestinationsbox', 'destinationdata');
		</script>
	</tr>
	<%= tg.endContentTable()%>
	<%= tg.endTable() %>
	<%= tg.endBorder() %>



	<%-- //////////////////////Specification Components///////////////////////////--%>

	<%=tg.startGroupBorder()%>
	<%=tg.startTable()%>
	<%=tg.startGroupTitle()%><%=specComponentsLabel%><%=tg.endTitle()%>
	<%=tg.startGroupContentTable()%>

	<table>
		<tr>
			<td></td>
			<% if (allComponents && specRequestsMap != null && specRequestsMap.size() > 0) { %>
			<%=fg.createDropDownListWidget(specRequestLabel, specRequestsMap, "printLayout", "", "javascipt:chooseView(document.MAINFORM.printLayout.value)", false)%>
			<% } %>
		<tr>
		</tr>
		<tr>
			<td>
				<% if (logger.isDebugEnabled()) {
					logger.debug("\t child list of comp " + childListOfComp.size() + " child docs " + childDocs.size());
				} %>
				<% if (!childListOfComp.isEmpty() || !childDocs.isEmpty() || !childDescDocs.isEmpty()) { %>
				<%=fg.createCustomActionBooleanInput("includeChildSpecs", includeChildSpecifications, DEFAULT_CHILD_SPECS_CHECKED, "javascript:includeChildSpecsAndDocs()", false, false)%>
				<% } else { %>
					<input type="hidden" name="includeChildSpecsbox" value="">
				<% } %>
			</td>
		</tr>
		<tr id="includeBOMOwnerVariationsLbl">
			<td><%=fg.createLabel(includeBOMOwnerVariations, false, "", "LIGHTER_BAR")%>
			</td>
			<td><%=fg.createCustomActionBooleanWidgetBody("includeBOMOwnerVariations", DEFAULT_BOM_OWNER_VARIATIONS, "handleCheckBox(this, includeBOMOwnerVariations);handleWidgetEvent(this);", false, false, true)%>
			</td>
		</tr>

		<!-- <tr id="includeMeasurementsOwnerSizesLbl">
   <td> -->
		<%--    <%=fg.createLabel(includeMeasurementsOwnerSizes, false, "", "LIGHTER_BAR") %> --%>
		<!-- </td>
   <td> -->
		<%--<%=fg.createCustomActionBooleanWidgetBody("includeMeasurementsOwnerSizes", DEFAULT_MEASUREMENTS_OWNER_SIZES, "handleCheckBox(this, includeMeasurementsOwnerSizes);handleWidgetEvent(this);", false, false, true) --%>
		<!-- </td>
   </tr> -->

		<tr>
			<td><%=fg.createLabel(availableComponentsLabel, false)%></td>
		</tr>

		<tr>
			<td><%=fg.createMultiChoice("specPages", null, listOfComp, componentSelectedValues, false, null, false, null, "10", "javascript:updatePage()", false, true)%>
			</td>
			<% if (!allComponents) { %>
			<SCRIPT>
			    document.MAINFORM.specPagesOptions.disabled=true;
			    document.MAINFORM.specPagesChosen.disabled=true;
		    </SCRIPT>
			<% } %>
			<td>
				<table>
					<tr>
						<td><a
							title='<%=LCSMessage.getHTMLMessage(RB.MAIN, "moveUp_LBL", RB.objA, false)%>'
							id='sizeRunmoveUpList' href='#'
							onclick='javascript:moveUpList(document.MAINFORM.specPagesChosen, document.MAINFORM.specPages);return false;'><img
								src='<%=WT_IMAGE_LOCATION%>/page_up.png' border='1'
								alt='<%=FormatHelper.formatHTMLString(moveUpLabel, false)%>'></a>
						</td>
					</tr>
					<tr>
						<td><a
							title='<%=FormatHelper.formatHTMLString(moveDownLabel, false)%>'
							id='sizeRunmoveDownList' href='#'
							onclick='javascript:moveDownList(document.MAINFORM.specPagesChosen, document.MAINFORM.specPages);return false;'><img
								src='<%=WT_IMAGE_LOCATION%>/page_down.png' border='1'
								alt='<%=FormatHelper.formatHTMLString(moveDownLabel, false)%>'></a>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		<%=tg.endContentTable()%>
		<%=tg.endTable()%>
		<%=tg.endBorder()%>

		<% if (listOfDocs.isEmpty() && listOfDescDocs.isEmpty()) { %>
		<input type="hidden" name="includeSecondaryContent" id="includeSecondaryContent" value="">
		<% } else { %>
			<%=tg.startGroupBorder()%>
			<table>
				<tr>
					<td><%=fg.createCustomActionBooleanInput("includeSecondaryContent", includeSecondaryLabel, "false", "javascript:setIncludeSecondary(this);", false)%>
					</td>
				</tr>
			</table>
			<%=tg.endBorder()%>
		<% } %>
	</table>

	<% if (listOfDocs.isEmpty()) { %>
	<input type="hidden" name="availDocs" id="availDocs" value="">
	<input type="hidden" name="docOptionsOptions" id="docOptionsOptions" value="">
	<% } else { %>

	<%=tg.startGroupBorder()%>
	<%=tg.startTable()%>
	<%=tg.startGroupTitle()%><%=availableDocumentsLabel%><%=tg.endTitle()%>
	<%=tg.startGroupContentTable()%>

	<table>
		<tr>
			<td><%=fg.createMultiChoice("availDocs", availableDocumentsLabel, listOfDocs, docContentSelectValue, false, null, false, null, "10", "javascript:updateDoc()")%>
			</td>
			<td>
				<table>
					<tr>
						<td><a
							title='<%=LCSMessage.getHTMLMessage(RB.MAIN, "moveUp_LBL", RB.objA, false)%>'
							id='sizeRunmoveUpList' href='#'
							onclick='javascript:moveUpList(document.MAINFORM.availDocsChosen, document.MAINFORM.availDocs);return false;'><img
								src='<%=WT_IMAGE_LOCATION%>/page_up.png' border='1'
								alt='<%=FormatHelper.formatJavascriptString(moveUpLabel, false)%>'></a>
						</td>
					</tr>
					<tr>
						<td><a
							title='<%=FormatHelper.formatHTMLString(moveDownLabel, false)%>'
							id='sizeRunmoveDownList' href='#'
							onclick='javascript:moveDownList(document.MAINFORM.availDocsChosen, document.MAINFORM.availDocs);return false;'><img
								src='<%=WT_IMAGE_LOCATION%>/page_down.png' border='1'
								alt='<%=FormatHelper.formatJavascriptString(moveDownLabel, false)%>'></a>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
	<%=tg.endContentTable()%>
	<%=tg.endTable()%>
	<%=tg.endBorder()%>
	<% } %>

	<% if (listOfDescDocs.isEmpty()) { %>
	<input type="hidden" name="availDescDocs" id="availDescDocs" value="">
	<input type="hidden" name="docOptionsOptions" id="docOptionsOptions" value="">
	<% } else { %>
	<%=tg.startGroupBorder()%>
	<%=tg.startTable()%>
	<%=tg.startGroupTitle()%><%=availDescDocsLabel%><%=tg.endTitle()%>
	<%=tg.startGroupContentTable()%>

	</table>
	<table>
		<tr>
			<td><%=fg.createMultiChoice("availDescDocs", availDescDocsLabel, listOfDescDocs, docContentSelectValue1, false, null, false, null, "10", "javascript:updateDoc()")%>
			</td>
			<td>
				<table>
					<tr>
						<td><a
							title='<%=LCSMessage.getHTMLMessage(RB.MAIN, "moveUp_LBL", RB.objA, false)%>'
							id='sizeRunmoveUpList' href='#'
							onclick='javascript:moveUpList(document.MAINFORM.availDescDocsChosen, document.MAINFORM.availDescDocs);return false;'><img
								src='<%=WT_IMAGE_LOCATION%>/page_up.png' border='1'
								alt='<%=FormatHelper.formatJavascriptString(moveUpLabel, false)%>'></a>
						</td>
					</tr>
					<tr>
						<td><a
							title='<%=FormatHelper.formatHTMLString(moveDownLabel, false)%>'
							id='sizeRunmoveDownList' href='#'
							onclick='javascript:moveDownList(document.MAINFORM.availDescDocsChosen, document.MAINFORM.availDescDocs);return false;'><img
								src='<%=WT_IMAGE_LOCATION%>/page_down.png' border='1'
								alt='<%= FormatHelper.formatJavascriptString(moveDownLabel, false)%>'></a>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>

	<%= tg.endContentTable() %>
	<%= tg.endTable() %>
	<%= tg.endBorder() %>
	<% } %>


<%-- //////////////////////////Sizing Definitions--%>
<%-- //START - Footwear TechPack Implementation--%>
<%if(!isRBKProductFootwear){ %>
<%=tg.startGroupBorder()%>
<%=tg.startTable()%>
<%=tg.startGroupTitle()%><%=sizesLabel%><%=tg.endTitle()%>
<%=tg.startGroupContentTable()%>

<%-- //////////////////Sizing selection/////////////////////////////////--%>

<%}%>
<%-- //END - Footwear TechPack Implementation--%>
<%
    List<FlexObject> sizeDefinitionListPSD = null;
    List<FlexObject> sizeDefinitionListMset = null;
    Map<String, Object> sizingDataMap = null;
    sizingDataMap = TechPackSizingHelper.getAllRelevantSizes(product, appContext.getSourcingConfig(),
            currentSpec);

    String sizeDataLabel = "";
    String sizeDataLabel2 = "";
    String sizeLabel = "";
    String sizeLabel2 = "";
    String sizeData = "";
    String sizeData2 = "";
    String category = "";
    String styleCss[] = {"TBLD", "TBLL"};
    String cssValue = "";
    String category2 = "";
    String size1lLabel="";
    String size2lLabel="";
    int msetSize=0;
    ArrayList<String> msetsizeList = null;
    int count = 0;
    LinkedHashSet<String> sortedset_size1 = (LinkedHashSet<String>) sizingDataMap.get("SIZE1_HEADERS");
    LinkedHashSet<String> sortedset_size2 = (LinkedHashSet<String>) sizingDataMap.get("SIZE2_HEADERS");
    sizeDefinitionListPSD = (ArrayList<FlexObject>) sizingDataMap.get("SIZE_DEFINITIONS");
    sizeDefinitionListMset = (ArrayList<FlexObject>) sizingDataMap.get("MEASUREMENT_SETS");
     if(sizingDataMap.get("SIZE1_LABEL")!=null){
     size1lLabel = sizingDataMap.get("SIZE1_LABEL").toString();
     }
     if(sizingDataMap.get("SIZE2_LABEL")!=null){
     size2lLabel = sizingDataMap.get("SIZE2_LABEL").toString();
     }
     if(sizeDefinitionListMset!=null){
           msetSize=sizeDefinitionListMset.size();
       }
    if (sortedset_size1!=null &&sortedset_size1.size() > 0){
%>
<%-- //START - Footwear TechPack Implementation--%>
<%if(!isRBKProductFootwear){ %>

<tr>
    <td>
        <div>
            <table cellpadding="1" cellspacing="1" width="60%" class="TABLE_OUTLINE">

                <tr>
                    <td colspan="<%=sortedset_size1.size() + 1%>">
                        <table style="float: right;">
                            <tr>
								<%if(!isRBKProductFootwear){ %>
                                <td><%=fg.createCustomActionBooleanInput("allSizes", allLabel, DEFAULT_ALL_SIZES_CHECKED, "javascipt:selectAllSizes()", false)%></td>
                           		<%} %>
                            </tr>
                        </table>

                    </td>
                </tr>
<% } %>
<%-- //END - Footwear TechPack Implementation--%>
<%-- //START - Footwear TechPack Implementation--%>
<%if(!isRBKProductFootwear){ %>
                <tr class="TBLL">
                    <td class="LABEL" align='CENTER'><%=sourceLabel%></td>
                    <td class="LABEL" align='CENTER' style="word-break: break-all;" colspan="<%=sortedset_size1.size()%>"><%=FormatHelper.encodeAndFormatForHTMLContent(size1lLabel)%></td>
                    <!--sortedset_size1.size() -->
                </tr>
                <tr class="TBLD">
                    <td align="LEFT" nowrap></td>
                    <%
                        String temp1 = "";
                            for (String size : sortedset_size1) {
                                temp1 = size;
                    %>
                    <%=drawCheckBox("size1data", temp1, false, "size1dataid")%>
                    <%
                        }
                    %>

                </tr>
				<% } %>
				<%-- //END - Footwear TechPack Implementation--%>
				<%-- //START - Footwear TechPack Implementation--%>
				<%if(!isRBKProductFootwear){ %>
                <tr>
                    <td class="TBLL" align='CENTER' nowrap></td>
                    <%
                        for (String size : sortedset_size1) {
                    %>
                    <td class="TBLL" align="center" nowrap><%=FormatHelper.encodeAndFormatForHTMLContent(size)%></td>
                    <%
                        }
                    %>
                </tr>
				<%}%>
				<%-- //END - Footwear TechPack Implementation--%>
				<%-- //START - Footwear TechPack Implementation--%>
                <%if(sizeDefinitionListPSD!=null && !isRBKProductFootwear){
                    for (int i = 0; i < sizeDefinitionListPSD.size(); i++) {
                            FlexObject firstElement = sizeDefinitionListPSD.get(i); // only concerned with the first sizing category
                            if (count % 2 == 0) {
                                cssValue = styleCss[0];
                            } else {
                                cssValue = styleCss[1];
                            }

                            // sizeDataLabel=firstElement.get("ProductSizeCategory.sizeValues").toString();
                            sizeData = firstElement.getData("PRODUCTSIZECATEGORY.SIZEVALUES").toString();
                            sizeLabel = firstElement.get("ProductSizeCategoryMaster.name").toString();

                            sizeDataLabel = size1lLabel;
                            int index = sizeLabel.indexOf("|");
                            if (index > 0) {
                                category = sizeLabel.substring(0, index);
                            } else {
                                category = sizeLabel;
                            }

                            Collection sizes = MOAHelper.getMOACollection(sizeData);
                %>
                <tr class="<%=cssValue%>">
                    <td class="LABEL" align='LEFT' nowrap><%=FormatHelper.encodeAndFormatForHTMLContent(category)%></td>
                    <%
                        Iterator it1 = sizes.iterator();
                                while (it1.hasNext()) {
                                    if (!it1.next().toString().equals("SIZE_NOT_PRESENT")) {
                    %>

                    <td class="LABEL" align='CENTER' nowrap>X</td>

                    <%
                        } else {
                    %>
                    <td class="LABEL" align='CENTER' nowrap></td>
                    <%
                        }
                                }
                    %>
                </tr>
                <%
                    count++;
                        }
                }
                 //END - Footwear TechPack Implementation
                        measurementSets = new String();
                        measurementSetsName = new String();
                      //START - Footwear TechPack Implementation
                          if(sizeDefinitionListMset!=null && !isRBKProductFootwear){
                        for (int i = 0; i < sizeDefinitionListMset.size(); i++) {
                            FlexObject firstElement = sizeDefinitionListMset.get(i);

                            //only concerned with the first sizing category
                            if (count % 2 == 0) {
                                cssValue = styleCss[0];
                            } else {
                                cssValue = styleCss[1];
                            }
                            msetsizeList = new ArrayList<String>();

                            //sizeDataLabel=firstElement.get("ProductSizeCategory.sizeValues").toString();
                            sizeData = firstElement.getData("LCSMEASUREMENTS.SIZERUN").toString();

                            if(firstElement.containsKey("LCSMEASUREMENTSMASTER.MEASUREMENTSNAME")) {
                                sizeLabel = firstElement.get("LCSMEASUREMENTSMASTER.MEASUREMENTSNAME").toString();
                            }
                            else if(firstElement.containsKey("SPECLESSMEASUREMENTS.NAME")) {
                                sizeLabel = firstElement.get("SPECLESSMEASUREMENTS.NAME").toString();
                            }
                            measurementSets += firstElement.getData("LCSMEASUREMENTS.CLASSNAMEA2A2").toString() + ":"
                                    + firstElement.getData("LCSMEASUREMENTS.BRANCHIDITERATIONINFO").toString() + "|~*~||~*~|"
                                    + firstElement.getData("LCSMEASUREMENTS.SIZERUN").toString() + ",";
                            measurementSetsName += sizeLabel + ",";
                            Collection sizes = MOAHelper.getMOACollection(sizeData);
                %>
                <tr class="<%=cssValue%>">
                    <td class="LABEL" align='LEFT' nowrap><%=FormatHelper.encodeAndFormatForHTMLContent(sizeLabel)%></td>
                    <%
                        Iterator it2 = sizes.iterator();
                                while (it2.hasNext()) {
                                    String value = it2.next().toString();
                                    if (!value.equals("SIZE_NOT_PRESENT")) {
                                        msetsizeList.add(value);
                    %>

                    <td class="LABEL" align='CENTER' nowrap>X</td>

                    <%
                        } else {
                    %>
                    <td class="LABEL" align='CENTER' nowrap></td>
                    <%
                        }
                                }
                    %>

                <tr>
                    <%
                        count++;
                            }
                          }
                        //END - Footwear TechPack Implementation
                    %>

            </table>
        </div>
    </td>
</tr>
<%
    }
%>
<tr/><tr/><tr/><tr/><tr/><tr/><tr/><tr/><tr/><tr/>
<tr/><tr/><tr/><tr/><tr/><tr/><tr/><tr/><tr/><tr/>

<%-- //START - Footwear TechPack Implementation --%>

 <%if(sortedset_size2!=null&&sortedset_size2.size()>0 && !isRBKProductFootwear){ %>


<tr>
    <td>
        <div>

            <!-- second table for size 2 -->
    <table cellpadding="1" width="60%" cellspacing="1"  class="TABLE_OUTLINE" >
                <tr>
                    <td colspan="<%=sortedset_size2.size() + 1%>">
                        <table style="float: right;">
                            <tr>
                      <td><%=fg.createCustomActionBooleanInput("allSizes2", allLabel, DEFAULT_ALL_SIZES_CHECKED, "javascipt:selectAllSizes2()", false) %></td>
                            </tr>
                        </table>
                    </td>
                </tr>


                <tr class="TBLL" rowspan="3">
                    <td class="LABEL" align='CENTER'><%=sourceLabel%></td>
                    <td class="LABEL" align='CENTER' style="word-break: break-all;" colspan="<%=sortedset_size2.size()%>"><%=FormatHelper.encodeAndFormatForHTMLContent(size2lLabel)%></td>
                    <!--sortedset_size1.size() -->
                </tr>
                <tr class="TBLD">
                    <td class="LABEL" align="CENTER" nowrap></td>
                    <%
                        String temp2 = "";
                            for (String size : sortedset_size2) {
                                temp2 = size;
                    %>
                    <%=drawCheckBox("size2data", temp2, false, "size2dataid")%>
                    <%
                        }
                    %>

                </tr>
                <tr class="TBLL">
                    <td class="LABEL" align='LEFT' nowrap></td>
                    <%
                        for (String size : sortedset_size2) {
                    %>
                    <td class="LABEL" align="CENTER" nowrap><%=FormatHelper.encodeAndFormatForHTMLContent(size)%></td>
                    <%
                        }
                    %>
                </tr>

                <% if(sizeDefinitionListPSD!=null){
                        for (int i = 0; i < sizeDefinitionListPSD.size(); i++) {
                            FlexObject firstElement = sizeDefinitionListPSD.get(i); // only concerned with the first sizing category

                            if (count % 2 == 0) {
                                cssValue = styleCss[0];
                            } else {
                                cssValue = styleCss[1];
                            }

                            category2 = "";
                            //sizeDataLabel2=firstElement.get("ProductSizeCategory.size2Values").toString();
                            sizeData2 = firstElement.getData("PRODUCTSIZECATEGORY.SIZE2VALUES").toString();

                            sizeLabel2 = firstElement.get("ProductSizeCategoryMaster.name").toString();

                            sizeDataLabel2 = size2lLabel;
                            int index = sizeLabel2.indexOf("|");
                            if (index > 0) {
                                category2 = sizeLabel2.substring(index + 1, sizeLabel2.length());
                            } else {
                                count++;
                            }
                            Collection sizes2 = MOAHelper.getMOACollection(sizeData2);
                            if (category2.length() > 0 && !category2.toString().isEmpty()) {
                %>
                <tr class="<%=cssValue%>">
                    <td class="LABEL" align='LEFT' nowrap><%=FormatHelper.encodeAndFormatForHTMLContent(category2)%></td>
                    <%
                        Iterator it3 = sizes2.iterator();
                                    while (it3.hasNext()) {
                                        if (!it3.next().toString().equals("SIZE_NOT_PRESENT")) {
                    %>

                    <td class="LABEL" align='CENTER' nowrap>X</td>

                    <%
                        } else {
                    %>
                    <td></td>
                    <%
                        }
                                    }
                    %>
                </tr>
                <%
                    }

                            count++;
                        }
                }%>
            </table>
            <%
                }
		//END - Footwear TechPack Implementation
            %>

        </div>
    </td>
</tr>


<script>
//to check if measurements sets exist as component
function getSelectedmeasurmentSetData(){
    var value=<%= msetSize%>;
    if(value>0){
        return true;
    }else{
        return false;
    }
}

//to get selected measurement sizes
function getMsetData(selectedComps){
    var msetSelectedComponents;
    var selectedMeasurments=new Array();
    var count  = 0;
    var measurementset;
    if(<%=measurementSets!=null && measurementSets.length() > 0%>){
    measurementset= "<%=FormatHelper.encodeForJavascript(measurementSets)%>";
    }else{
        measurementset="";
    }
    var newArr= new Array();
    var msetWithSizelist=new Array();
    msetSelectedComponents=selectedComps.split("|~*~|");
    var availableSizes="";
    var sizeMap = new Map();
    for (var i = 0; i < msetSelectedComponents.length; i++){
        if(msetSelectedComponents[i].length!=1){
            var length = "Measurements-:-VR:".length;

            if(msetSelectedComponents[i].indexOf("Measurements-:-")>-1){
            newArr[count] = msetSelectedComponents[i].substring(length,msetSelectedComponents[i].length);
            count++;
            }
        }
    }

    //msetSelectedComponents data is stored in newArr in array format
    if(measurementset.length>0){
    selectedMeasurments=measurementset.split(",");
    }
    for (var i = 0; i < selectedMeasurments.length; i++){
        if(selectedMeasurments[i].length>1){
            for (var j = 0; j < newArr.length; j++){

                var value=selectedMeasurments[i].split("|~*~||~*~|");

                if(newArr[j]==value[0]){
                    availableSizes+=value[0]+"|~*~||~*~|"+value[1]+",";

                }
            }
        }
    }

    //available sizes contains measurement set code and respectives sizes seperated with comma
    if(availableSizes.length>0){
        msetWithSizelist=availableSizes.split(",");
        if(msetWithSizelist.length>0){
            for (var j = 0; j < msetWithSizelist.length; j++){
                var availableSizelist= new Array();
                var msetWiseSizes= new Array();
                var key='';
                availableSizelist=msetWithSizelist[j].split("|~*~||~*~||~*~|");
                if(availableSizelist.length>0){
                    if(availableSizelist[0]!=''){
                        key=availableSizelist[0];
                        msetWiseSizes=availableSizelist[1].split("|~*~|");
                    }
                    var counter=0;
                    var finalSizeList= new Array();
                    for (var i = 0; i < msetWiseSizes.length; i++){
                        if(msetWiseSizes[i].length>0 && msetWiseSizes[i]!='SIZE_NOT_PRESENT' && msetWiseSizes[i]!='undefined'){
                            finalSizeList[counter]=msetWiseSizes[i];
                            counter++;
                        }

                    }
                }
            sizeMap.set(finalSizeList, key);
            //sizeMap contains measurement set code as key and respective sizes array as value
            }
        }
    }
    return sizeMap;
}

//to get selected measurement name with respect to measurement id
function getMeasurmentsetname(value){
    var msetName="";
    var result="";
    var resultArray= new Array();
    var msetResultArray= new Array();
    <%
    msetDataString = "";
    for (Map.Entry<String, String> entry : listOfComp.entrySet()) {
        msetDataString += entry.getKey() + "/" + FormatHelper.encodeForJavascript(entry.getValue()) + ",";
            }
    msetDataString = msetDataString.length() > 0 ? msetDataString.substring(0, msetDataString.length() - 1) : "";
     %>
    result="<%=msetDataString%>";
    if(result!=""){
        resultArray=result.split(",");
        for (var i = 0; i < resultArray.length; i++){
            msetResultArray=resultArray[i].split("/");
            if(msetResultArray[0]=='Measurements-:-VR:'+value){
                msetName=   msetResultArray[1];
            }

        }
    }
    return msetName;
}


selectAllSizes();
selectAllSizes2();
</script>
<%-- //START - Footwear TechPack Implementation --%>
<% if(!isRBKProductFootwear){%>
<%=tg.endContentTable()%>
<%=tg.endTable()%>
<%=tg.endBorder()%>
<%} %>
<%-- //END - Footwear TechPack Implementation --%>

<% ///////////////////////////////////////////Doc Options/////////////////////%>

<div id="CADDocumentDiv">
<% if (CADDOC_ENABLED&& !specLinks.isEmpty()) { %>
    <%= tg.startGroupBorder() %>

    <%= tg.startTable() %>
    <%= tg.startGroupTitle() %><%=availableCADDocGrpHdr%><%= tg.endTitle() %>
    <%= tg.startGroupContentTable() %>

     <table width="45%">
         <tr>
         <td>
                <%= fg.createMultiChoice("availEPMDOCDocs", availableCADDocLbl, specLinks, "", false, null, false, null, "10", "") %>
         </td><td>
            <table>
                <tr><td>
                <a title='<%= LCSMessage.getHTMLMessage(RB.MAIN, "moveUp_LBL", RB.objA, false)%>' id='sizeRunmoveUpList' href='#' onclick='javascript:moveUpList(document.MAINFORM.availEPMDOCDocsChosen, document.MAINFORM.availEPMDOCDocs);return false;'><img src='<%=WT_IMAGE_LOCATION%>/page_up.png' border='1' alt='<%= FormatHelper.formatJavascriptString(moveUpLabel, false)%>'></a>
                </td></tr>
                <tr><td>
                <a title='<%= FormatHelper.formatHTMLString(moveDownLabel,false)%>' id='sizeRunmoveDownList' href='#' onclick='javascript:moveDownList(document.MAINFORM.availEPMDOCDocsChosen, document.MAINFORM.availEPMDOCDocs);return false;'><img src='<%=WT_IMAGE_LOCATION%>/page_down.png' border='1' alt='<%= FormatHelper.formatJavascriptString(moveDownLabel, false)%>'></a>
                </td></tr>
            </table>
         </td></tr>
         <%-- B-92545
      <tr><td><%= fg.createDropDownListWidget(cadDocFilterLabel, saveFilters, "cadDocFilter", "", null, false)%></td></tr>
     --%>
   </table>
   <%= tg.endTable() %>
   <%= tg.endTableContentTable() %>
    <%= tg.endBorder() %>
    <div style ="border-left:770px solid #dfdfdf;height:1px;overflow:hidden;margin:0;margin-top:-17px;margin-left:8px;margin-right:8px;"> </div>

<%}else{%>
         <input type="hidden" name="availEPMDOCDocs" id="availEPMDOCDocs" value="">
         <%-- B-92545
         <input type="hidden" name="cadDocFilter" id="cadDocFilter" value="">
         --%>

<% } %>
</div>

<div id="PartsDiv" style ="margin-left:8px;margin-right:8px;">
<% if (PART_ENABLED&& !partTOSpecLinks.isEmpty()) { %>
    <%= tg.startGroupBorder() %>
    <%= tg.startTable() %>
    <%= tg.startGroupTitle() %>
    <%=availablePartGrpHdr%>
    <%= tg.endTitle() %>
    <%= tg.startGroupContentTable() %>
     <table >
         <tr>
         <td>
                <%= fg.createMultiChoice("availParts", availablePartLbl, partTOSpecLinks, "", false, null, false, null, "10", "") %>
         </td><td>
            <table>
                <tr><td>
                <a title='<%= LCSMessage.getHTMLMessage(RB.MAIN, "moveUp_LBL", RB.objA, false)%>' id='sizeRunmoveUpList' href='#' onclick='javascript:moveUpList(document.MAINFORM.availPartsChosen, document.MAINFORM.availParts);return false;'><img src='<%=WT_IMAGE_LOCATION%>/page_up.png' border='1' alt='<%= FormatHelper.formatJavascriptString(moveUpLabel, false)%>'></a>
                </td></tr>
                <tr><td>
                <a title='<%= FormatHelper.formatHTMLString(moveDownLabel,false)%>' id='sizeRunmoveDownList' href='#' onclick='javascript:moveDownList(document.MAINFORM.availPartsChosen, document.MAINFORM.availParts);return false;'><img src='<%=WT_IMAGE_LOCATION%>/page_down.png' border='1' alt='<%= FormatHelper.formatJavascriptString(moveDownLabel, false)%>'></a>
                </td></tr>
            </table>
         </td></tr>

   </table>
   <%= tg.endTable() %>
   <%= tg.endTableContentTable() %>
    <div style = "border-left:770px solid #dfdfdf;height:1px;overflow:hidden;margin:0;margin-top:-10px;margin-left:8px;margin-right:8px;"> </div>
<%}else{%>
         <input type="hidden" name="availParts" id="availParts" value="">
         <input type="hidden" name="partFilter" id="partFilter" value="">

<% } %>
</div>

<%-- //////////////////////////////Page Options///////////////////////////////////--%>

<%= tg.startGroupBorder() %>
<%= tg.startTable() %>
<%= tg.startGroupTitle()%><%= pageOptionsGrpTle%><%= tg.endTitle() %>
<%= tg.startGroupContentTable() %>
<%-- Other Options--%>
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
         <%=fg.createDropDownListWidget(measurementUOMOverrideLabel, formats, "uom", "si.Length.in", null, false, false) %>
         </td></tr>
         <tr><td>
         <% if(FormatHelper.hasContent(sizeDataLabel2)) {
         Map sizeCats = new HashMap();
         sizeCats.put("size1", sizeDataLabel);
         sizeCats.put("size2", sizeDataLabel2);
         Vector order = new Vector();
         order.add("size1");
         order.add("size2");
         Object[] objB = {sizeDataLabel, sizeDataLabel2};
         String useSize1Size2Label = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "useSize1OrSize2_LBL", objB) ;

         %><%= fg.createDropDownListWidget(useValuesLabel, sizeCats, "useSize1Size2", "size1", "", false, false, order) %>
         <%} else { %>
         <input type="hidden" name="useSize1Size2" id="useSize1Size2" value="size1">
         <% } %>
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
     <%= fg.createMultiChoice("pageOptions", availableReportsLabel, allOptions, null, false, null, false, null, "6", "javascript:updatePage()") %>
      </td>
      <td>
          <table>
                  <tr><td>
                  <a title='<%= FormatHelper.formatHTMLString(moveUpLabel,false)%>' id='sizeRunmoveUpList' href='#' onclick='javascript:moveUpList(document.MAINFORM.pageOptionsChosen, document.MAINFORM.pageOptions);return false;'><img src='<%=WT_IMAGE_LOCATION%>/page_up.png' border='1' alt='<%=FormatHelper.formatJavascriptString(moveUpLabel, false)%>'></a>
                  </td></tr>
                  <tr><td>
                  <a title='<%= FormatHelper.formatHTMLString(moveDownLabel,false)%>' id='sizeRunmoveDownList' href='#' onclick='javascript:moveDownList(document.MAINFORM.pageOptionsChosen, document.MAINFORM.pageOptions);return false;'><img src='<%=WT_IMAGE_LOCATION%>/page_down.png' border='1' alt='<%=FormatHelper.formatJavascriptString(moveDownLabel, false)%>'></a>
                  </td></tr>
              </table>
      </table>
   </td>
</tr>
<%-- Section Views--%>
<tr><td>
   <% Iterator bomOptionsIt = bomOptions.iterator();
   Iterator bomFlexTypesIt;
   AttributeValueList sectionAttList;
   Collection section = new ArrayList();
   Iterator sectionItr;
   String editBomActivity = "EDIT_BOM";
   Collection reportColumns = new ArrayList();
   Map reportOptions = new HashMap();
   String defaultViewId = "";
   String bomOption = "";
   FlexType bomFlexType;
   FlexObject fobj;
   String bomFlexTypeId = "";
   if( (bomOptions != null && bomOptions.size() >0) && (bomFlexTypes != null && bomFlexTypes.size() > 0)) {
   while(bomOptionsIt.hasNext()) {
      bomOption = (String)bomOptionsIt.next();
      bomFlexTypesIt = bomFlexTypes.iterator();%>
      <div nowrap id="<%=bomOption+"div"%>">
      <table id="<%=bomOption%>" border='0' width='100%'>
        <%while(bomFlexTypesIt.hasNext()) {
         bomFlexType = (FlexType)bomFlexTypesIt.next();
         bomFlexTypeId = FormatHelper.getNumericObjectIdFromObject(bomFlexType);
         lcsContext.viewCache.clearCache(FormatHelper.getObjectId(bomFlexType), editBomActivity );
         reportColumns = lcsContext.viewCache.getViews(FormatHelper.getObjectId(bomFlexType), editBomActivity);
         defaultViewId = lcsContext.viewCache.getDefaultViewId(FormatHelper.getObjectId(bomFlexType), editBomActivity);
         if(reportColumns != null && reportColumns.size() > 0) {//If we don't have any views, don't draw the div
            Object[] objC = {bomFlexType.getFullNameDisplay(true)};%>
            <tr>
                <td class="SUBSECTION" nowrap colspan='2'>
                  <div id='<%= bomOption %>_<%= bomFlexTypeId %>_div_plus'>
                     <a href="javascript:changeDiv('<%= bomOption %>_<%= bomFlexTypeId %>_div')"><img src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png" alt="" border="0" align="absmiddle"></a>&nbsp;&nbsp;
                     <a href="javascript:changeDiv('<%= bomOption %>_<%= bomFlexTypeId %>_div')"><%=bomOptionsMap.get(bomOption) + ":  " + WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "type_LBL", objC )+ "  "+ viewOptionsLabel%></a>
                  </div>
                  <div id='<%= bomOption %>_<%= bomFlexTypeId %>_div_minus'>
                     <a href="javascript:changeDiv('<%= bomOption %>_<%= bomFlexTypeId %>_div')"><img src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" alt="" border="0" align="absmiddle"></a>&nbsp;&nbsp;
                     <a href="javascript:changeDiv('<%= bomOption %>_<%= bomFlexTypeId %>_div')"><%=bomOptionsMap.get(bomOption) + ":  " + WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "type_LBL", objC ) + "  "+ viewOptionsLabel%></a>
                  </div>
                </td>
            </tr>
<%
               sectionAttList = bomFlexType.getAttribute("section").getAttValueList();
               section = sectionAttList.getSelectableKeys(lcsContext.getContext().getLocale(), true);

               sectionItr = section.iterator();
%>
            <tr>
                <td>
                    <div id='<%= bomOption %>_<%= bomFlexTypeId %>_div'>
                    <table>
<%

               while (sectionItr.hasNext()){
                  String secID = (String)sectionItr.next();
                  %>
               <tr>
                  <%reportOptions = new HashMap();
                    Iterator ri = reportColumns.iterator();
                    while(ri.hasNext()) {
                     fobj = (FlexObject)ri.next();

                     reportOptions.put(bomOption + TYPE_COMP_DELIM + FormatHelper.getObjectId(bomFlexType)+ TYPE_COMP_DELIM + secID + TYPE_COMP_DELIM+"ColumnList:"+fobj.getString("COLUMNLIST.IDA2A2"),fobj.getString("COLUMNLIST.DISPLAYNAME"));

                    }
                    String selectedViewId = null;
                    if(FormatHelper.hasContent(defaultViewId)){
                     selectedViewId = bomOption + TYPE_COMP_DELIM + FormatHelper.getObjectId(bomFlexType)+ TYPE_COMP_DELIM + secID + TYPE_COMP_DELIM+"ColumnList"+ defaultViewId.substring(defaultViewId.lastIndexOf(":"));
                    }
                    %><%=fg.createDropDownListWidget(sectionAttList.getValue(secID, lcsContext.getContext().getLocale()), reportOptions, "viewSelect", selectedViewId, "", false) %>

               </tr>
            <%}//End section loop%>
                    </div>
                    </table>
                </td>
            </tr>
<script>
    toggleDiv('<%= bomOption %>_<%= bomFlexTypeId %>_div_plus');
</script>
<tr><td><hr></td><tr>
         <%}%>
      <%}//End bomFlexType loop
%>
      </table></div>
      <script>
      document.getElementById('<%=bomOption +"div"%>').style.display ='none';
      </script>
<%
   }//End bom options loop
   } else {%>
   <input type="hidden" name="viewSelect" value="">
   <%}%>

</td></tr>
<!--add for story B-58112 03/28/2011-->
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
        <%= fg.createDropDownList(daysDisplayMap, "daysValueId", showChangeSinceDefault, "changeSinceDate()", 1,false,null,daysOrder) %>
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
</td></tr>

<tr>
    <td>
<div id="divSpecPart" style="display:none">
<table id="tblSpecPart" border='0' width='100%'>
<tr>
    <td class="SUBSECTION" nowrap colspan='2'>
    <div id='partData_div_plus'>
        <a href="javascript:changeDiv('divExportedIndentedBOM')"><img src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png" alt="" border="0" align="absmiddle"></a>&nbsp;&nbsp;
        <a href="javascript:changeDiv('divExportedIndentedBOM')"><%=partDataOptionsLabel%></a>
    </div>
    <div id='partData_div_minus'>
        <a href="javascript:changeDiv('divExportedIndentedBOM')"><img src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" alt="" border="0" align="absmiddle"></a>&nbsp;&nbsp;
        <a href="javascript:changeDiv('divExportedIndentedBOM')"><%=partDataOptionsLabel%></a>
    </div>
    </td>
</tr>
<tr><td>
<div id="divExportedIndentedBOM">
<table id="tblexportedIndentedBOM" border='0' width='20%'>
<tr><td>
<%=fg.createCustomActionBooleanInput("exportedIndentedBOM", showIndentedBOMLabel, "false", "javascript:changePartFilter();", false, true) %>
</td>
</tr>
 <tr><td><%= fg.createDropDownListWidget(partFilterLabel, savePartFilters, "partFilter", defaultFilterName, null,false,false)%></td></tr>
</table>
</div>
</td></tr>
<script>
    toggleDiv('partData_div_plus');
</script>
<tr><td><hr></td></tr>
</table>
</div>
</td></tr>

<%= tg.endContentTable() %>
<%= tg.endTable() %>
<%= tg.endBorder() %>
<%-- ////////////////////////////// End Page Options///////////////////////////////////--%>
<%if (ACLHelper.hasViewAccess(defaultDocumentVaultType)&&ACLHelper.hasCreateAccess(defaultDocumentVaultType)) { %>

<div id="documentVaultDiv">
             <%= tg.startGroupBorder() %>
             <%= tg.startTable() %>
             <%= tg.startGroupTitle("DocumentVaultGroup") %>
             <%=vaultDocumentsLabel%>
             <%= tg.endTitle() %>
             <%= tg.startGroupContentTable("DocumentVaultGroup") %>

         <table width="45%"><tr>
                 <%=fg.createCustomActionBooleanInput("asynchronousGeneration", asynchGenLabel, "false", "javascript:setAsynchGen(this);", false, true) %>
            </tr><tr>
                <%=fg.createCustomActionBooleanInput("documentVault", vaultDocumentsLabel, "false", "javascript:setDocumentVaultDiv(this);", false, false) %>
            </tr><tr>
             <td class="FORMLABEL" align="left" nowrap width="150px"><div id="refTypeDiv" nowrap>&nbsp;&nbsp;<a href="javascript:chooseVaultDocumentType()"><%= vaultDocumentTypeLabel %></a>&nbsp;</div></td>
             <td class="FORMELEMENT"  align="left" nowrap>
                <b><div id="fkTypeDisplay" nowrap>------</div></b>
             </td>
      </tr>
         <%= tg.endContentTable() %>
         <%= tg.endTable() %>
         <%= tg.endBorder() %>
</div>
<%} %>
<table> <tr>
    <td class="button" align="right">
    <a class="button" href="javascript:selectSpecificationPages(<%=size2exists%>)"><%= selectButton %></a>&nbsp;&nbsp;&nbsp;
   </td>
</tr> </table>
<script>
<%

if (ACLHelper.hasViewAccess(defaultDocumentVaultType) && ACLHelper.hasCreateAccess(defaultDocumentVaultType)){
    String showDocVault = request.getParameter("showDocVault");
    if (!"true".equals(showDocVault)) { %>
        document.getElementById('documentVaultDiv').style.display ='none';
  <%}%>

    document.getElementById('fkTypeDisplay').innerHTML = '<%=FormatHelper.encodeForJavascript(FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE).getFullNameDisplay())%>';
    document.MAINFORM.vaultDocumentTypeId.value = '<%=FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath(DEFAULT_DOCUMENT_VAULT_TYPE))%>';
    hideDocumentVaultDiv();
    <%if(DEFAULT_ASYNC_SINGLE_TPGENERATION){%>
       var dvCheckBox = document.getElementById('documentVaultbox');
       var dvInput = document.getElementById('documentVault');
       var agCheckBox = document.getElementById('asynchronousGenerationbox');
       var agInput = document.getElementById('asynchronousGeneration');
       dvCheckBox.disabled = 'true';
       dvCheckBox.checked = 'true';
       dvInput.value = true;
       agCheckBox.checked='true';
       agInput.value = true;
       showDocumentVaultDiv();
    <%}%>
<%}%>
includeChildSpecsfunction();
includeChildDocsFunction();
includeChildDescDocsFunction();

//START -- Override OOtb Javascript function to default collapse DocumentVault Section
toggleExpandableDiv("DocumentVaultGroup", "DocumentVaultGroupIcon");

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