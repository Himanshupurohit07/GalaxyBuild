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
                com.lcs.wc.sourcing.*,
                com.lcs.wc.foundation.LCSQuery,
                com.lcs.wc.product.*,
	            com.lcs.wc.sizing.*,
                com.lcs.wc.specification.*,
                com.lcs.wc.season.LCSSeason,
                com.lcs.wc.client.web.html.*,
                com.lcs.wc.season.LCSSeasonMaster,
                com.lcs.wc.measurements.*,
                wt.part.*,
                wt.util.*,
                java.util.*,
				wt.method.*,
				org.apache.logging.log4j.LogManager,
				org.apache.logging.log4j.Logger"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="csheetModel" scope="request" class="com.lcs.wc.sourcing.LCSCostSheetClientModel" />
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="fg2" scope="request" class="com.lcs.wc.client.web.FormGenerator2" />
<jsp:useBean id="flexg2" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator2" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="appContext" class="com.lcs.wc.client.ApplicationContext" scope="session"/>

<% flexg2.setCreate(true); %>
<%!
    public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");
    public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");

    public static final boolean USE_SIZING = LCSProperties.getBoolean("com.lcs.wc.product.useSizing");
    public static final boolean USE_DESTINATION = LCSProperties.getBoolean("com.lcs.wc.product.useProductDestinations");
    public static final boolean USE_MULTIPLE_COST_SIZING = LCSProperties.getBoolean("com.lcs.wc.sourcing.costSheet.useMultiCostSizing");
    public static final boolean USE_MULTIPLE_COST_DESTINATION = LCSProperties.getBoolean("com.lcs.wc.sourcing.costSheet.useMultiCostDestination");

    public static boolean sizingOn = false;
    public static boolean destinationOn = false;

	static{
		if(USE_SIZING && USE_MULTIPLE_COST_SIZING){
			sizingOn = true;
		}
		if(USE_DESTINATION && USE_MULTIPLE_COST_DESTINATION){
			destinationOn = true;
		}
	}
%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%!
    public static final String CLIENT_SIDE_PLUGIN = PageManager.getPageURL("CLIENT_SIDE_PLUGINS", null);
    public static final String STANDARD_TEMPLATE_HEADER = PageManager.getPageURL("STANDARD_TEMPLATE_HEADER", null);
    public static final String STANDARD_TEMPLATE_FOOTER = PageManager.getPageURL("STANDARD_TEMPLATE_FOOTER", null);
	// added logger as part of Changes to add the BOM Name from source Cost Sheet into Method Context
	private static final Logger logger = LogManager.getLogger("rfa.sparc.jsp.sourcing.SPARCWhatIfMultiCostSheet.jsp");
%>
<input type="hidden" name="additionalParameters" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("additionalParameters")) %>">
<%
   String saveButton = WTMessage.getLocalizedMessage ( RB.MAIN, "save_Btn",RB.objA ) ;
   String cancelButton = WTMessage.getLocalizedMessage ( RB.MAIN, "cancel_Btn", RB.objA ) ;
   String closeButton = WTMessage.getLocalizedMessage ( RB.MAIN, "closeWindow_BTN",RB.objA ) ;
   String productIdentificationGrpTle = WTMessage.getLocalizedMessage ( RB.SOURCING,"productIdentification_GRP_TLE", RB.objA ) ;
   String costSheetIdentificationGrpTle = WTMessage.getLocalizedMessage ( RB.SOURCING, "costSheetIdentification_GRP_TLE", RB.objA ) ;
   String costingDimensionsGrpTle = WTMessage.getLocalizedMessage ( RB.SOURCING, "costingDimensions_GRP_TLE", RB.objA ) ;
   String sourcingConfigurationLabel = WTMessage.getLocalizedMessage ( RB.SOURCING, "sourcingConfiguration_LBL",RB.objA ) ;
   String specificationLabel = WTMessage.getLocalizedMessage ( RB.SOURCING, "specification_LBL", RB.objA ) ;
   String costSheetIdGrpTle = WTMessage.getLocalizedMessage ( RB.SOURCING, "costSheetId_GRP_TLE",RB.objA ) ;
   String youMustSupplyNonZeroNumberAlrt = WTMessage.getLocalizedMessage ( RB.MAIN, "youMustSupplyNonZeroNumber_ALRT" , RB.objA );
   String typeLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "type_LBL", RB.objA ) ;
   String seasonLabel = WTMessage.getLocalizedMessage ( RB.SOURCING, "season_LBL", RB.objA ) ;
   String productLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "product_LBL", RB.objA ) ;
   String skuLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "sku_LBL", RB.objA ) ;
   String whatIfCostSheetPgTle = WTMessage.getLocalizedMessage ( RB.SOURCING, "whatIfCostSheet_PG_TLE", RB.objA ) ;
   String sizeCategoryNameLabel = WTMessage.getLocalizedMessage ( RB.MEASUREMENTS, "sizeCategory_LBL",RB.objA ) ;
   String colorwayGroupLabel = WTMessage.getLocalizedMessage ( RB.SOURCING,"colorwayGroup_LBL", RB.objA ) ;
   String size1GroupLabel = WTMessage.getLocalizedMessage ( RB.SOURCING,"size1Group_LBL", RB.objA ) ;
   String size2GroupLabel = WTMessage.getLocalizedMessage ( RB.SOURCING,"size2Group_LBL", RB.objA ) ;
   String destinationGroupLabel = WTMessage.getLocalizedMessage ( RB.SOURCING,"destinationGroup_LBL", RB.objA ) ;
   String representativeColorwayLabel = WTMessage.getLocalizedMessage ( RB.SOURCING,"representativeColorway_LBL", RB.objA ) ;
   String representativeSizeLabel = WTMessage.getLocalizedMessage ( RB.SOURCING,"representativeSize_LBL", RB.objA ) ;
   String representativeDestinationLabel = WTMessage.getLocalizedMessage ( RB.SOURCING,"representativeDestination_LBL", RB.objA ) ;
   String noneSelectedLabel    = WTMessage.getLocalizedMessage ( RB.SEASON, "noneSelected_LBL", RB.objA ) ;//-- None Selected --
   String noneAvailableLabel   = WTMessage.getLocalizedMessage ( RB.SEASON, "noneAvailable_LBL", RB.objA ) ;//-- None Available --

   LCSCostSheet csheet = csheetModel.getBusinessObject();

   FlexType type = csheet.getFlexType();

   String number = String.valueOf(csheet.getNumber());
   if(FormatHelper.hasContent(number)){
    number = FormatHelper.applyFormat(number, FormatHelper.INT_FORMAT);
   }

   LCSSourcingConfigMaster sconfigMaster = csheet.getSourcingConfigMaster();
   String sConfigName = "";
   LCSSourcingConfig sconfig = null;
   if(sconfigMaster!=null){
	    sConfigName = sconfigMaster.getSourcingConfigName();
		sconfig = (LCSSourcingConfig)VersionHelper.latestIterationOf(sconfigMaster);
   }
   LCSSKU sku = (LCSSKU)VersionHelper.latestIterationOf(csheet.getSkuMaster());


   flexg2.setScope(CostSheetFlexTypeScopeDefinition.COST_SHEET_SCOPE);
   if(!sku.isPlaceholder()){
        flexg2.setLevel(CostSheetFlexTypeScopeDefinition.SKU_LEVEL);
   } else {
        flexg2.setLevel(CostSheetFlexTypeScopeDefinition.PRODUCT_LEVEL);
   }

   String copyFromOid = FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("copyFromOid"));

    String activity = request.getParameter("activity");
    String action = request.getParameter("action");
    String pluginType = request.getParameter("pluginType");

    String csName = (String)csheet.getValue("name");
    if (!FormatHelper.hasContent(csName)){
    csName = "Place Holder";
    }

    LCSSeason season = null;
	if(csheet.getSeasonRevId()>0){
		season = (LCSSeason) LCSQuery.findObjectById("VR:com.lcs.wc.season.LCSSeason:" + new Double("" + csheet.getSeasonRevId()).longValue());
	}

/**************************Colorways***************************************/
    HashMap colorwayNames = appContext.getSKUsMap();
    String selectedColorways = csheet.getApplicableColorNames();

    StringBuffer colorBuffer = new StringBuffer();

    if (colorwayNames != null){
        Collection selectedColorwaysColl = MOAHelper.getMOACollection(selectedColorways);
        Iterator selectedColorwaysCollIter = selectedColorwaysColl.iterator(); 

        while(selectedColorwaysCollIter.hasNext()){
            String colorChosen = (String)selectedColorwaysCollIter.next();

            Iterator colorKeyIter = colorwayNames.keySet().iterator();
            Iterator colorValueIter = colorwayNames.values().iterator();

            while(colorKeyIter.hasNext()) {
                String color_key = (String)colorKeyIter.next();
                String color = (String)colorValueIter.next();
                if(colorChosen.equals(color)){
                    colorBuffer = colorBuffer.append(color_key);
                    colorBuffer = colorBuffer.append(MOAHelper.DELIM);
                }
            }
        }
    }

    String selectedColorwayKeys = colorBuffer.toString();

    /**getRepresentativeColorQuery***/
    Collection representativeColorCol = LCSCostSheetQuery.getRepresentativeColor((LCSCostSheetMaster)csheet.getMaster());
    String representativeColor="";
    Iterator repColorColIter = representativeColorCol.iterator();
    FlexObject fo = null;

    while(repColorColIter.hasNext()) {
        fo = (FlexObject)repColorColIter.next();
        representativeColor = "VR:com.lcs.wc.product.LCSSKU:" + fo.getString("LCSSKU.BRANCHIDITERATIONINFO");
    }

/**********************Size Category*******************************************/
    String sizeCategoryNames = csheet.getApplicableSizeCategoryNames();
    String selectedCategory = "";

    Hashtable sizeNames = new Hashtable();

    LCSProduct productRevA = appContext.getProductARev();
    FlexType specType = productRevA.getFlexType().getReferencedFlexType(ReferencedTypeKeys.SPEC_TYPE);

    Collection productSizeCats = new SizingQuery().findPSDByProductAndSeason(null,productRevA, appContext.getSeason() ,null,null,null).getResults();

    HashMap sizeCategoryMap = new HashMap();
    Iterator productSizeCatsIter = productSizeCats.iterator();
    FlexObject fobj = null;
    String name = "";
    while(productSizeCatsIter.hasNext()){
        fobj = (FlexObject)productSizeCatsIter.next();
        sizeCategoryMap.put("OR:com.lcs.wc.sizing.ProductSizeCategory:" + fobj.get("PRODUCTSIZECATEGORY.IDA2A2"), fobj.get("PRODUCTSIZECATEGORYMASTER.NAME"));

        /*******************Set the selectedCategory to the one from the db***************/
        String categoryName = (String)fobj.get("PRODUCTSIZECATEGORYMASTER.NAME") + MOAHelper.DELIM;
        if((sizeCategoryNames != null) && (sizeCategoryNames.equals(categoryName))){
            selectedCategory = "OR:com.lcs.wc.sizing.ProductSizeCategory:" + fobj.get("PRODUCTSIZECATEGORY.IDA2A2");
        }
        %>
        <input id="sizeGroup<%= fobj.get("PRODUCTSIZECATEGORY.IDA2A2") %>Options" type="hidden" value='<%= fobj.get("PRODUCTSIZECATEGORY.SIZEVALUES") %>'>
        <input id="size2Group<%= fobj.get("PRODUCTSIZECATEGORY.IDA2A2") %>Options" type="hidden" value='<%= fobj.get("PRODUCTSIZECATEGORY.SIZE2VALUES") %>'>
        <%

    }
/*************************Size****************************************/
    String selectedSizes = csheet.getApplicableSizes();
    String selectedSizes2 = csheet.getApplicableSizes2();
    String representativeSize = csheet.getRepresentativeSize();
    String representativeSize2 = csheet.getRepresentativeSize2();
/*************************Destination****************************************/
    Map destinationMap = appContext.getProductDestinationsMap();
    String selectedDestinations = csheet.getApplicableDestinationNames();
    Collection selectedDestinationColl = MOAHelper.getMOACollection(selectedDestinations);
    Iterator selectedDestinationCollIter = selectedDestinationColl.iterator(); 
    StringBuffer destinationBuffer = new StringBuffer();

    while(selectedDestinationCollIter.hasNext()){
        String destinationChosen = (String)selectedDestinationCollIter.next();
        Iterator destinationKeyIter = destinationMap.keySet().iterator();
        Iterator destinationValueIter = destinationMap.values().iterator();
        while(destinationKeyIter.hasNext()) {
            String destination_key = (String)destinationKeyIter.next();
            String destination = (String)destinationValueIter.next();
            if(destinationChosen.equals(destination)){
                destinationBuffer = destinationBuffer.append(destination_key);
                destinationBuffer = destinationBuffer.append(MOAHelper.DELIM);
            }
        }
    }

    String selectedDestinationKeys = destinationBuffer.toString();

    /**getRepresentativeDestinationQuery***/
    Collection representativeDestCol = LCSCostSheetQuery.getRepresentativeDestination((LCSCostSheetMaster)csheet.getMaster());
    String representativeDest="";
    Iterator repDestColIter = representativeDestCol.iterator();

    while(repDestColIter.hasNext()) {
        fo = (FlexObject)repDestColIter.next();
        representativeDest = "OR:com.lcs.wc.product.ProductDestination:" + fo.getString("PRODUCTDESTINATION.IDA2A2");
    }
/*****************************************************************/

    //Hashtable skuTable = new Hashtable();
    HashMap specTable = (new ProductHeaderQuery()).findSpecificationsMap(productRevA, sconfig, season);
    if(!ACLHelper.hasViewAccess(specType)){
        specTable = new HashMap();
    }
    if(specTable.isEmpty()){
        specTable.put("", noneAvailableLabel);
    } 
    else{
       specTable.put(" ", noneSelectedLabel);
    }

    String specDisplay ="";
    if (appContext.getSpecification() != null) {
        specDisplay = (String)appContext.getSpecification().getValue("specName");
    } else {
        specDisplay = noneSelectedLabel;
    }
    String contextSpecId = null;
    FlexSpecMaster specMaster = csheetModel.getSpecificationMaster();
    if(specMaster != null){
        LCSSeasonMaster seasonMaster = null;
		if(season!=null){
			seasonMaster = (LCSSeasonMaster)season.getMaster();
		}
        FlexSpecToSeasonLink fstsl = FlexSpecQuery.findSpecToSeasonLink(specMaster, seasonMaster);
        if(fstsl != null){
            contextSpecId = FormatHelper.getObjectId(fstsl);
        }
    }

	String seasonId = "";
	String seasonIdFromRequest = request.getParameter("seasonId");
	if(FormatHelper.hasContent(seasonIdFromRequest) && seasonIdFromRequest.indexOf("LCSSeason")>-1){
		seasonId = FormatHelper.getObjectId((LCSSeasonMaster)season.getMaster());
	}
	
		// Changes to add the BOM Name from source Cost Sheet into Method Context - start
		String oldBOMValueFromCopyWhatIFCostSheet = (String)csheet.getValue("scBOM");
		logger.debug("***oldBOMValueFromCopyWhatIFCostSheet:"+oldBOMValueFromCopyWhatIFCostSheet);
		if(oldBOMValueFromCopyWhatIFCostSheet!=null && FormatHelper.hasContent(oldBOMValueFromCopyWhatIFCostSheet)){
			MethodContext.getContext().put("oldBOMValueFromCopyWhatIFCostSheet", oldBOMValueFromCopyWhatIFCostSheet);
		}
		// Changes to add the BOM Name from source Cost Sheet into Method Context - end
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JAVSCRIPT ///////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<input type="hidden" name="sCategory" value="<%=selectedCategory %>">
<input type="hidden" id="sCategorySizes" name="sCategorySizes" value="<%=selectedSizes %>">
<input type="hidden" id="sCategorySizes2" name="sCategorySizes2" value="<%=selectedSizes2 %>">
<input name="representativeSizeCategory"  type="hidden" value="">
<input type="hidden" name="whatif" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("whatif")) %>">
<input type="hidden" name="active" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("active")) %>">
<input type="hidden" name="includeActiveCSBox" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("active"))%>" >
<input type="hidden" name="includeWhatIfBox" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("whatif"))%>" >

<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/costSheet.js"></script>

<script language="Javascript">

    function update(){
        if(validate()){
            <% if(FormatHelper.hasContent(copyFromOid)) { %>
            document.MAINFORM.activity.value = 'WHATIF_MULTI_COSTSHEET';
            <% } else { %>
            document.MAINFORM.activity.value = 'UPDATE_WHATIF_COSTSHEET';
            <% } %>
            document.MAINFORM.action.value = 'SAVE';
			<%if(FormatHelper.hasContent(seasonId)){%>
			document.MAINFORM.seasonId.value = '<%= seasonId %>';
			<%}%>
         submitForm();
        }
    }
    function validate(){
      <%= flexg2.drawFormValidation(type.getAttribute("name")) %>
      <%= flexg2.generateFormValidation(csheet) %>
        
        return true;
    }
</script>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// HTML ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<input type="hidden" name="skuLinkId" value="<%=  FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("skuLinkId")) %>">

<div class="themedPage">
   <div id="costSheetIdenfication" class="card">
 	<input type="checkbox" id="costSheetIdenficationCard" class="cardCheckbox" checked disabled>
 	<div class="card-header">
 		<label class="card-label" for="costSheetIdenficationCard" >
   			<%= costSheetIdentificationGrpTle %>
   		</label>
 	</div>
 	 <div class="card-content">
 	 	 <div class="f-attribute-group">

					<%--display cost sheet name  --%>
					<%= flexg2.drawFormWidget(type.getAttribute("name"), csheetModel) %>
                    
                    <%--display type name  --%>
                	<%  DisplayElement typeDisplay = new DisplayElement(typeLabel, type.getFullNameDisplay()); %>
                	<%= DefaultCommonAttributeRenderer.createDisplay(typeDisplay) %>
                
                	<%--display source name  --%>
                	<%
                   	DisplayElement srcDisplay = new DisplayElement(sourcingConfigurationLabel, sConfigName); %>
                	<%= DefaultCommonAttributeRenderer.createDisplay(srcDisplay) %>
                    
                    <%--display product name  --%>
                	<%
                	DisplayElement productDisplay = new DisplayElement(productLabel, appContext.getProductName()); %>
                	<%= DefaultCommonAttributeRenderer.createDisplay(productDisplay) %>    
                   
                    <%--display specification name  --%>
               		<%
                		Vector<String> order = new Vector<String>();
                    	order.add(0," ");
               		%>
               		<div class="f-attribute-box">
               			<div class="input-title"><%=specificationLabel%></div>
               			<%=FormGenerator2.createDropDownListWidget(null, specTable, "specReference", contextSpecId, null, true, false, order, null, null, false, "", "", "", "", true) %>
               		</div>   
               
 	 	 	 		<%--display season name  --%>
 	 	 	 		<%
	            		DisplayElement seasonDisplay = new DisplayElement(seasonLabel, appContext.getSeasonName()); %>
	            	<%= DefaultCommonAttributeRenderer.createDisplay(seasonDisplay) %>
			      	</div>
 	 	</div>
 	</div>  

    <% if(ACLHelper.hasCreateAccess(type.getAttributeGroupObject(LCSProperties.get("jsp.sourcing.costsheetVariationsGroup"), false))){ %>
       <div id="costSheetVariations" class="card">
      	<input type="checkbox" id="costSheetVariationsCard" class="cardCheckbox" checked disabled>
	     <div class="card-header">
	      	<label class="card-label" for="costSheetVariationsCard" >
	          <%= costingDimensionsGrpTle %>
	        </label>
	     </div>
      	<div class="card-content">
      		 <div class="f-attribute-group">
       			<%--display colorways  --%>
       			 <%= FormGenerator2.createMultiChoice("colorwayGroup", colorwayGroupLabel, colorwayNames, selectedColorwayKeys, false, null, false, null, null, "setRepresentativeColorwaySel2();", true) %>
            	
            	<%--display representative colorway  --%>
				<div class="form-elem-container">
					<div class="input-title"><%=representativeColorwayLabel%></div>
	                <%= FormGenerator2.createDropDownList("representativeColorway", null, "", "", new HashMap(), null, "", "", "", "", false, true, false, false) %>
	            </div>
                
							<%if(destinationOn){%>
								<%= FormGenerator2.createMultiChoice("destinationGroup", destinationGroupLabel, destinationMap, selectedDestinationKeys, false, null, false, null, null, "setRepresentativeDestinationSel2();", true) %>

					<%--display representative destination  --%>
					<div class="form-elem-container">
					<div class="input-title"><%=representativeDestinationLabel%></div>
		                <%= FormGenerator2.createDropDownList("representativeDestination", null, "", "", new HashMap(), null, "", "", "", "", false, true, false, false) %>
		            </div>
                        
							<%}%>
                            
                            
							<%if(sizingOn){%>
							
								<%--display Product Size Definition  --%>
					<%= FormGenerator2.createDropDownListWidget(sizeCategoryNameLabel, sizeCategoryMap, "sizeCategoryGroup", selectedCategory, "setSizeOptionsSel2(this.value);setSize2OptionsSel2(this.value);", false, true, null, null, null, false, "", "", "", "", true) %>

					<%--display Sizes 1  --%>
					<%= FormGenerator2.createMultiChoice("sizeGroup", size1GroupLabel, sizeNames, selectedSizes, false, null, false, null, null, "setRepresentativeSizeSel2();", true) %>

					<%--display Representative Size  --%>
					<div class="form-elem-container">
					<div class="input-title"><%=representativeSizeLabel%></div>
		                <%= FormGenerator2.createDropDownList("representativeSize", null, "", "", new HashMap(), null, "", "", "", "", false, true, false, false) %>
		            </div>

					<%--display Sizes 2  --%>
					<%= FormGenerator2.createMultiChoice("size2Group", size2GroupLabel, sizeNames, selectedSizes2, false, null, false, null, null, "setRepresentativeSize2Sel2();", true) %>

					<%--display Representative Size  --%>
					<div class="form-elem-container">
					<div class="input-title"><%=representativeSizeLabel%></div>
		                <%= FormGenerator2.createDropDownList("representativeSize2", null, "", "", new HashMap(), null, "", "", "", "", false, true, false, false) %>
		            </div>
						
							<% } %>
                        </div>
      	
      		</div>
      	</div>
    <% } %>

  
         <%= flexg2.generateForm(csheet) %>

<script>
//------------------Set initial Representative colorway--------------
    if( ("<%=selectedColorwayKeys%>") != null){
    	setRepresentativeColorwaySel2();
        // set the representative color from the object
        setSelectedValueOfListFromValue(document.MAINFORM.representativeColorway, "<%= FormatHelper.encodeForJavascript(representativeColor) %>");

    }

//------------------Set the initial Representative Size Category---------------------
	<% if(sizingOn) { %>
    if((document.MAINFORM.sCategory.value) != null){
    	setSizeOptionsSel2(document.MAINFORM.sCategory.value);
        setSize2OptionsSel2(document.MAINFORM.sCategory.value);

            var sizeList = document.getElementById("sCategorySizes").value;
            if(hasContent(sizeList)) {
                var destChosenSizeList = document.MAINFORM.sizeGroupOptions;
                var sizes = moaToArray(sizeList);
                document.MAINFORM.sizeGroup.value = sizeList;

                for(var i = 0; i < sizes.length; i++){
                	setSelectedValueSel2(destChosenSizeList, sizes[i], sizes[i], true);
                }

                //------------------Set the initial Sizes ---------------------

                // Copy the sizeGroupChosen values to the representativeSize dropDownList
                copyOptionsSelect2(destChosenSizeList, document.MAINFORM.representativeSize);
                // set the set the representative size from the object
                setSelectedValueOfListFromDisplay(document.MAINFORM.representativeSize, "<%= FormatHelper.encodeForJavascript(representativeSize) %>");
            }
            
            //Size list 2
            var size2List = document.getElementById("sCategorySizes2").value;
            if(hasContent(size2List)) {
                var destChosenSize2List = document.MAINFORM.size2GroupOptions;
                var sizes = moaToArray(size2List);
                document.MAINFORM.size2Group.value = size2List;

                for(var i = 0; i < sizes.length; i++){
                	setSelectedValueSel2(destChosenSize2List, sizes[i], sizes[i], true);
                }

                //------------------Set the initial Sizes ---------------------

                // Copy the sizeGroupChosen values to the representativeSize dropDownList
                copyOptionsSelect2(destChosenSize2List, document.MAINFORM.representativeSize2);
                // set the set the representative size from the object
                setSelectedValueOfListFromDisplay(document.MAINFORM.representativeSize2, "<%= FormatHelper.encodeForJavascript(representativeSize2) %>");
            }

    }
	<% } %>

//------------------Set initial Representative destination-----------
	<% if(destinationOn){ %>
    if( ("<%=selectedDestinationKeys%>") != null){
    	setRepresentativeDestinationSel2();
        setSelectedValueOfListFromValue(document.MAINFORM.representativeDestination, "<%= FormatHelper.encodeForJavascript(representativeDest) %>");

    }
	<% } %>
</script>

</div>

<%-- //////////////////////////////////////////////////////////////////////////////////////// --%>
<%-- ///////////////////////////// Page Revision Info /////////////////////////////////////// --%>
<%-- //////////////////////////////////////////////////////////////////////////////////////// --%>