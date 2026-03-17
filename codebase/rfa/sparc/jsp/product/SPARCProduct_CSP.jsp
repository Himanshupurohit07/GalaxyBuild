<%-- Copyright (c) 2006 PTC, Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////   onLoad  PAGE DOCUMENTATION    //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%--
    JSP Type: ClientSidePlugin

   SPARCProduct_CSP.jsp:    
  
--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////  Add to the import package/class list as required  ////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.client.Activities,                
                com.lcs.wc.client.web.*,    
                com.lcs.wc.util.*,
                com.lcs.wc.db.*,
                com.lcs.wc.product.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.foundation.*,
				com.lcs.wc.season.LCSSeasonQuery,
				com.lcs.wc.season.LCSSKUSeasonLink,
				com.lcs.wc.season.LCSSeason,
				com.lcs.wc.season.LCSSeasonMaster,
                java.util.*"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JSP METHODS /////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%! 
    public static final String JSPNAME = "SparcProduct_CSP";
    public static final boolean DEBUG = true;
    public static String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
	private static String COLORWAY_LIFECYCLE = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.Status");
	private static String PRODUCT_LIFECYCLE = LCSProperties.get("com.lcs.wc.product.LCSProduct.ProductLifecycle.Status");
	
	private static String BUY_READY = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.BUYREADY.Status");
	private static String SAMPLE_READY = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.SAMPLEREADY.Status");
	private static String DROPPED = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.DROPPED.Status");
	private static String PLANNED = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.PLANNED.Status");
	private static String RELEASED = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.RELEASED.Status");

	
	private static String PRODUCT_PLANNED_STATUS_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSProduct.LockedAttribute.Planned.Status.Keys");
	private static String PRODUCT_RELEASED_STATUS_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSProduct.LockedAttribute.Released.Status.Keys");
	private static String PRODUCT_SAMPLE_READY_STATUS_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSProduct.LockedAttribute.SampleReady.Status.Keys");
	private static String PRODUCT_BUY_READY_STATUS_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSProduct.LockedAttribute.BuyReady.Status.Keys");
	
	private static String APP_RBK_FLEXTYPE = LCSProperties.get("com.lcs.wc.product.LCSProduct.FlexType.Reebok");
	public static final String adminGroup = LCSProperties.get("com.lcs.wc.client.adminGroups.name");
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ///////////////////// INITIALIZATION JSP CODE and CSP environment ///////////////////--%>
<%-- ///// This section is generic and is included in all the Client Side Plugins ////////--%>
<%-- ////// Do not alter this section unless instructed to do so via the comments  ///////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%     

    String activity = request.getParameter("activity");
    String action = request.getParameter("action");
    String oid = request.getParameter("oid");
    String type = request.getParameter("type"); 
    boolean checkuserGroups = true;
    LCSSeasonQuery sQuery = new LCSSeasonQuery();
    
	LCSProduct product = (LCSProduct)LCSQuery.findObjectById(oid);
	if( !(product.getFlexType().getFullNameDisplay().toString().equals(APP_RBK_FLEXTYPE)) && 
			!(product.getFlexType().getFullNameDisplay().toString().contains("Reebok"))) {
		return;
	}

	Collection seasonsUsed = sQuery.findSeasons(product);
	Collection colorways = LCSSKUQuery.findSKUs(product);
	Iterator colorwayItr = colorways.iterator();
	System.out.println("seasonsUsed in "+seasonsUsed.size());
	Iterator seasonItr = seasonsUsed.iterator();
	String sparclifecycle = null; 
    
    ClientSidePluginHelper csph = new ClientSidePluginHelper();
    csph.init(type);
    String flextypeName = csph.getFlexTypeName();
    
    // if required add java code after this line   


    
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ///////////////////////////////// JAVSCRIPT PLUGIN LOGIC ////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<style type="text/css">
.pointer-events-disable-color{
	pointer-events : none;
}
</style>
<script>

    // ***** Begining of function pattern *************************************************

    function disableAttributes(){ 
	var planned = "<%= PLANNED %>";
	var buyReady = "<%= BUY_READY %>";
	var dropped = "<%= DROPPED %>";
	var sampleReady = "<%= SAMPLE_READY %>";
	var release = "<%= RELEASED %>";
	<%
	while(seasonItr.hasNext()){
		LCSSeasonMaster seasonmaster = (LCSSeasonMaster)seasonItr.next();
		LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(seasonmaster);
		while(colorwayItr.hasNext()){
			LCSSKU skuObj = (LCSSKU) colorwayItr.next();
			LCSSKUSeasonLink skuSeasonLink = (LCSSKUSeasonLink) LCSSeasonQuery.findSeasonProductLink(skuObj, season);
			sparclifecycle=(String)skuSeasonLink.getValue(COLORWAY_LIFECYCLE);
			LCSSKUSeasonLink prevSeasonLink = null;
			String oldColorwayLifeCycle = null;
			if (skuSeasonLink != null  ) {
				prevSeasonLink=(LCSSKUSeasonLink)LCSSeasonQuery.getPriorSeasonProductLink(skuSeasonLink);

			}
			if (prevSeasonLink != null) {
				// get old version of colorway lifecycle value
				oldColorwayLifeCycle = (String) prevSeasonLink.getValue(COLORWAY_LIFECYCLE);
			}
			
			
	%>
		
			
		
		if( "<%=sparclifecycle%>" == buyReady || ("<%=oldColorwayLifeCycle%>" == buyReady && "<%=sparclifecycle%>" == dropped)){
			
			<%
			String buyReadyLockAtt = PRODUCT_BUY_READY_STATUS_LOCKED_ATTRIBUTE ;
			StringTokenizer  buyReady = new StringTokenizer(buyReadyLockAtt,",");
			while (buyReady.hasMoreTokens()) {
				String buyReadydKeys = (String) buyReady.nextToken();
				buyReadydKeys.trim();
				String cellType = (String) product.getFlexType().getAttribute(buyReadydKeys.trim()).getAttVariableType();
			%>
			
			
			if("<%=csph.getKeyElement(buyReadydKeys)%>" != "cspKeyNotFound") {
				var elem = document.MAINFORM.<%=csph.getKeyElement(buyReadydKeys)%>;
				if(elem == null && elem == undefined){return;}
				if("<%= cellType %>" == "driven"){
					elem = document.MAINFORM.<%=csph.getKeyElement(buyReadydKeys)%>select;
				}else if("<%= cellType %>" == "object_ref"){
					if(elem.parentElement != null && elem.parentElement.parentElement != null && 
							elem.parentElement != undefined		&& elem.parentElement.parentElement != undefined ){
						elem.parentElement.parentElement.classList.add('pointer-events-disable-color');
					}
				}
				
				if(hasContent(elem)){
					elem.disabled=true;
				}
			}
			<%}%>
			
			
		}
		if( "<%=sparclifecycle%>" ==release || ("<%=oldColorwayLifeCycle%>" == release && "<%=sparclifecycle%>" == dropped)){
			
			<%
			String releasedLockAtt = PRODUCT_RELEASED_STATUS_LOCKED_ATTRIBUTE ;
			StringTokenizer  releaseTokenizer = new StringTokenizer(releasedLockAtt,",");
			while (releaseTokenizer.hasMoreTokens()) {
				String releasedKeys = (String) releaseTokenizer.nextToken();
				releasedKeys.trim();
				String cellType = (String) product.getFlexType().getAttribute(releasedKeys.trim()).getAttVariableType();
			%>
			
			
			if("<%=csph.getKeyElement(releasedKeys)%>" != "cspKeyNotFound") {
				var elem = document.MAINFORM.<%=csph.getKeyElement(releasedKeys)%>;
				if(elem == null && elem == undefined){return;}
				if("<%= cellType %>" == "driven"){
					elem = document.MAINFORM.<%=csph.getKeyElement(releasedKeys)%>select;
				}else if("<%= cellType %>" == "object_ref"){
					if(elem.parentElement != null && elem.parentElement.parentElement != null && 
							elem.parentElement != undefined		&& elem.parentElement.parentElement != undefined ){
						elem.parentElement.parentElement.classList.add('pointer-events-disable-color');
					}
				}
				
				if(hasContent(elem)){
					elem.disabled=true;
				}
			}
			<%}%>
			
			
		}else if( "<%=sparclifecycle%>" ==sampleReady || ("<%=oldColorwayLifeCycle%>" == sampleReady && "<%=sparclifecycle%>" == dropped)){
			
			<%
			String releasedLockAtt1 = PRODUCT_SAMPLE_READY_STATUS_LOCKED_ATTRIBUTE ;
			StringTokenizer  itrs1 = new StringTokenizer(releasedLockAtt1,",");
			while (itrs1.hasMoreTokens()) {
				String sampleReadyKeys = (String) itrs1.nextToken();
				sampleReadyKeys.trim();
				String cellType = (String) product.getFlexType().getAttribute(sampleReadyKeys.trim()).getAttVariableType();
			%>
			//alert("<%=csph.getKeyElement(sampleReadyKeys)%>");
			
			if("<%=csph.getKeyElement(sampleReadyKeys)%>" != "cspKeyNotFound") {
				var elem = document.MAINFORM.<%=csph.getKeyElement(sampleReadyKeys)%>;
				if(elem == null && elem == undefined){return;}
				if("<%= cellType %>" == "driven"){
					elem = document.MAINFORM.<%=csph.getKeyElement(sampleReadyKeys)%>select;
				}else if("<%= cellType %>" == "object_ref"){
					if(elem.parentElement != null && elem.parentElement.parentElement != null && 
							elem.parentElement != undefined		&& elem.parentElement.parentElement != undefined ){
						elem.parentElement.parentElement.classList.add('pointer-events-disable-color');
					}
				}
				
				if(hasContent(elem)){
					elem.disabled=true;
				}
			}
			<%}%>
			
			
		}
		else if( "<%=sparclifecycle%>" ==planned || ("<%=oldColorwayLifeCycle%>" == planned && "<%=sparclifecycle%>" == dropped)){
			
			<%
			String plannedLocked = PRODUCT_PLANNED_STATUS_LOCKED_ATTRIBUTE ;
			StringTokenizer  plannedItrs = new StringTokenizer(plannedLocked,",");
			while (plannedItrs.hasMoreTokens()) {
				String plannedKeys = (String) plannedItrs.nextToken();
				plannedKeys.trim();
				String cellType = (String) product.getFlexType().getAttribute(plannedKeys.trim()).getAttVariableType();
			%>
			//alert("<%=csph.getKeyElement(plannedKeys)%>");
			
			if("<%=csph.getKeyElement(plannedKeys)%>" != "cspKeyNotFound") {
				var elem = document.MAINFORM.<%=csph.getKeyElement(plannedKeys)%>;
				if(elem == null && elem == undefined){return;}
				if("<%= cellType %>" == "driven"){
					elem = document.MAINFORM.<%=csph.getKeyElement(plannedKeys)%>select;
				}else if("<%= cellType %>" == "object_ref"){
					if(elem.parentElement != null && elem.parentElement.parentElement != null && 
							elem.parentElement != undefined		&& elem.parentElement.parentElement != undefined ){
						elem.parentElement.parentElement.classList.add('pointer-events-disable-color');
					}
				}
				
				if(hasContent(elem)){
					elem.disabled=true;
				}
			}
			<%}%>
			
			
		}
	<%	}
	}
	
	%>
	}

    // ***** End of function pattern *************************************************
// 	function lockAttGenrics(status){
		
		
// 	}
    function runLoadFunctions(){ 
        disableAttributes();
		//alert("few of the Attributes are Locked");
    }


</script>

<script>runLoadFunctions();</script>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////// End of onLoad Client Side Plugin ////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

