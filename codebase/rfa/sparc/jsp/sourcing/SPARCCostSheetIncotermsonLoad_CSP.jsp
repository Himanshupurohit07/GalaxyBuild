<%-- Copyright (c) 2006 PTC, Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////   widget  PAGE DOCUMENTATION    //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%--
    JSP Type: ClientSidePlugin

    SPARCCostSheetIncotermsonLoad_CSP.jsp:   // Replace with actual filename //
  
--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page import="org.apache.logging.log4j.LogManager"%>
<%@page import="org.apache.logging.log4j.Logger"%>
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
	private static final Logger logger = LogManager.getLogger("rfa.sparc.jsp.sourcing.SPARCCostSheetIncotermsonLoad_CSP");
    public static final String JSPNAME = "SPARCCostSheetIncotermsonLoad_CSP";
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
	String incoterms = null;
	LCSLifecycleManaged incotermsLocation = null;
	String objectId = null;
	LCSProductCostSheet costSheet = null;
	LCSSourcingConfigMaster scMaster = null;
	LCSSourcingConfig sourcingConfig = null;
	LCSSupplier supplier = null;
	try{

		 costSheet = (LCSProductCostSheet) LCSQuery.findObjectById(oid);
	
		 scMaster = costSheet.getSourcingConfigMaster();
	
		 sourcingConfig = (LCSSourcingConfig) VersionHelper.latestIterationOf(scMaster);
	
		 supplier = (LCSSupplier) sourcingConfig.getValue("scFGFactory");
			if(supplier == null){
				if(logger.isDebugEnabled()){
					logger.debug("\t supplier \t" + supplier);
				}
				return;
			}
		String flextypeDisplayName = supplier.getFlexType().getFullNameDisplay().toString();
		if(supplier != null && flextypeDisplayName.contains("Finished Goods Factory")){
			 incoterms = (String) supplier.getValue("scIncoterms");
			 incotermsLocation = (LCSLifecycleManaged) supplier.getValue("scIncotermlocation");
			 objectId = FormatHelper.getNumericObjectIdFromObject(incotermsLocation);
		
		}
	}catch(Exception ex){
		if(logger.isDebugEnabled()){
			logger.debug("\t costSheet \t" + costSheet +
						 "\t scMaster \t" + scMaster +
						 "\t sourcingConfig \t" + sourcingConfig + 
						 "\t supplier \t" + supplier);
		}
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
		if((incotermsElement !=null  && incotermsElement.value.trim() =="" && "<%= incoterms %>" != null )){
			incotermsElement.value = "<%= incoterms %>";
		}
		if((incotermsLocElement !=null && incotermsLocElement.value.trim() == "" && "<%= incotermsLocation %>" != null && "<%= objectId %>" != null )){
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



