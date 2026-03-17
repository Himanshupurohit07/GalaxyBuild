<%-- Copyright (c) 2006 PTC, Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////   widget  PAGE DOCUMENTATION    //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%--
    JSP Type: ClientSidePlugin

    SPARCCreateCostSheetIncoterms_CSP.jsp:  
  
--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page import="com.lcs.wc.season.LCSSeason"%>
<%@page import="com.lcs.wc.specification.FlexSpecification"%>
<%@page import="com.lcs.wc.specification.FlexSpecToSeasonLink"%>
<%@page import="com.lcs.wc.sourcing.LCSSourceToSeasonLink"%>
<%@page import="com.lcs.wc.supplier.LCSSupplier"%>
<%@page import="com.lcs.wc.sourcing.LCSSourcingConfig"%>
<%@page import="com.lcs.wc.sourcing.LCSSourcingConfigMaster"%>
<%@page import="com.lcs.wc.sourcing.LCSProductCostSheet"%>
<%@page language="java"
       import="com.lcs.wc.client.Activities,                
                com.lcs.wc.client.web.*,    
                com.lcs.wc.util.*,
                com.lcs.wc.db.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.color.*,
                com.lcs.wc.foundation.*,
                wt.ownership.*,
                java.util.*"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JSP METHODS /////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>


<%! 
    public static final String JSPNAME = "SPARCCreateCostSheetIncoterms_CSP";
    public static final boolean DEBUG = true;
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ///////////////////// INITIALIZATION JSP CODE and CSP environment ///////////////////--%>
<%-- ///// This section is generic and is included in all the Client Side Plugins ////////--%>
<%-- ////// Do not alter this section unless instructed to do so via the comments  ///////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%     

    String activity = request.getParameter("activity");
    String action = request.getParameter("action");
    String ownerId = request.getParameter("ownerId");
    String contextSKUId = request.getParameter("contextSKUId");
    String seasonId = request.getParameter("seasonId");
	String type = request.getParameter("type"); 
	String oid = request.getParameter("oid");
	LCSSourceToSeasonLink sourceToSeasonLink = null ;
	if(oid.contains("LCSProduct")){
		return;
	}
	LCSSourcingConfigMaster scMaster = null;
	LCSSourcingConfig sourcingConfig = null;
	if(oid.contains("LCSSourceToSeasonLink")){
	 	sourceToSeasonLink = (LCSSourceToSeasonLink) LCSQuery.findObjectById(oid);
	 	scMaster = sourceToSeasonLink.getSourcingConfigMaster();
	 	sourcingConfig = (LCSSourcingConfig) VersionHelper.latestIterationOf(scMaster);
	}else if(oid.contains("FlexSpecToSeasonLink")){
		FlexSpecToSeasonLink flexSpecToSeasonLink = (FlexSpecToSeasonLink) LCSQuery.findObjectById(oid);
		LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(flexSpecToSeasonLink.getSeasonMaster());
		FlexSpecification spec = (FlexSpecification)  VersionHelper.latestIterationOf(flexSpecToSeasonLink.getSpecificationMaster());
		scMaster = spec.getSpecSource();
		sourcingConfig = (LCSSourcingConfig) VersionHelper.latestIterationOf(scMaster);
	}
	//START : Fixed - When no season is selected
	else if(oid.contains("FlexSpecification")){
		FlexSpecification flexSpec = (FlexSpecification) LCSQuery.findObjectById(oid);
		scMaster = flexSpec.getSpecSource();
		sourcingConfig = (LCSSourcingConfig) VersionHelper.latestIterationOf(scMaster);
	}else if(oid.contains("LCSSourcingConfig")){
		sourcingConfig = (LCSSourcingConfig) LCSQuery.findObjectById(oid);
		
	}
	//END : Fixed - When no season is selected
	if(sourcingConfig == null){
		return;
	}
	
	LCSSupplier supplier = (LCSSupplier) sourcingConfig.getValue("scFGFactory");
	if(supplier == null){
		return;
	}
	String incoterms = null;
	LCSLifecycleManaged incotermsLocation = null;

	String objectId = null;
	String flextypeDisplayName = supplier.getFlexType().getFullNameDisplay().toString();
	if(supplier != null && flextypeDisplayName.contains("Finished Goods Factory")){
		 incoterms = (String) supplier.getValue("scIncoterms");
		 incotermsLocation = (LCSLifecycleManaged) supplier.getValue("scIncotermlocation");
		 //START : FP-392
		 if(null != incotermsLocation){
			objectId = FormatHelper.getNumericObjectIdFromObject(incotermsLocation);
		 }
		 //END : FP-392
	
	}
	if(objectId == null || incoterms == null || incotermsLocation == null){
		return;
	}
	ClientSidePluginHelper csph = new ClientSidePluginHelper();
    csph.init(type);
    String flextypeName = csph.getFlexTypeName();
	%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ///////////////////////////////// JAVSCRIPT PLUGIN LOGIC ////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<script>
	function populateIncoterms(){
		var incotermsElement = document.MAINFORM.<%= csph.getKeyElement("scIncoterms")%>;
		var incotermsLocElement = document.MAINFORM.<%= csph.getKeyElement("scIncotermlocation")%>;
		var elemId ="quickSearchInput_"+ "<%=csph.getKeyElement("scIncotermlocation")%>" ;
		var divElem = document.getElementById(elemId);
		if(incotermsElement !=null   ){
			incotermsElement.value = "<%= incoterms %>";
		}
		if(incotermsLocElement !=null &&  divElem != null && "<%= incotermsLocation %>" != null  && "<%= objectId %>" != null  ){
			divElem.value = "<%= incotermsLocation.getName().toString()  %>"
			incotermsLocElement.value = "<%= objectId %>";
		}
	}
	
	function runLoadFunctions(){
		populateIncoterms();
	}
	
	

</script>



<script>runLoadFunctions();</script>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////// End of onLoad Client Side Plugin ////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>	



