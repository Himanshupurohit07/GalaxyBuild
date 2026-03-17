<%-- Copyright (c) 2006 PTC, Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////   onLoad  PAGE DOCUMENTATION    //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%--
    JSP Type: ClientSidePlugin

    SPARCSKU_CSP.jsp:  
  
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
	
	private static String BUY_READY = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.BUYREADY.Status");
	private static String SAMPLE_READY = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.SAMPLEREADY.Status");
	private static String DROPPED = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.DROPPED.Status");
	private static String PLANNED = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.PLANNED.Status");
	private static String RELEASED = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.RELEASED.Status");

	
	private static String COLORWAY_RELEASED_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSSKU.LockedAttribute.Released.Status.Keys");
	private static String COLORWAY_SAMPLE_READYLOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSSKU.LockedAttribute.Released.Status.Keys");
	private static String COLORWAY_BUY_READY_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSSKU.LockedAttribute.BuyReady.Status.Keys");
	
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
    
	LCSSKU sku = (LCSSKU)LCSQuery.findObjectById(oid);
	if( !(sku.getFlexType().getFullNameDisplay().toString().equals(APP_RBK_FLEXTYPE)) && 
			!(sku.getFlexType().getFullNameDisplay().toString().contains("Reebok")) ) {
		return;
	}
	Collection seasonsUsed = sQuery.findSeasons(sku.getMaster());
	Iterator seasonItr = seasonsUsed.iterator();
	String sparclifecycle = null;  
    
    ClientSidePluginHelper csph = new ClientSidePluginHelper();
    csph.init(type);
    String flextypeName = csph.getFlexTypeName(); 


    
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
			LCSSKUSeasonLink skuSeasonLink = (LCSSKUSeasonLink) LCSSeasonQuery.findSeasonProductLink(sku, season);
			sparclifecycle = (String) skuSeasonLink.getValue(COLORWAY_LIFECYCLE);
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
	
	
	if("<%=sparclifecycle%>" == buyReady || ("<%=oldColorwayLifeCycle%>" == buyReady && "<%=sparclifecycle%>" == dropped)){
		<%
			
		String buyReadyLockAtt = COLORWAY_BUY_READY_LOCKED_ATTRIBUTE ;
		StringTokenizer  buyReadyItr = new StringTokenizer(buyReadyLockAtt,",");
		while (buyReadyItr.hasMoreTokens()) {
			String buyReadykey = (String) buyReadyItr.nextToken();
			String cellType = (String) sku.getFlexType().getAttribute(buyReadykey).getAttVariableType();
		
		%>
		
		if("<%=csph.getKeyElement(buyReadykey)%>" != "cspKeyNotFound" ) {
			var elem = document.MAINFORM.<%=csph.getKeyElement(buyReadykey)%>;
			if(elem == null && elem == undefined){return;}
			if(elem == null){return;}
			if("<%= cellType%>" == "object_ref"){
				if(elem.parentElement != null && elem.parentElement.parentElement != null && 
						elem.parentElement != undefined		&& elem.parentElement.parentElement != undefined ){
					elem.parentElement.parentElement.classList.add('pointer-events-disable-color');
				}
			} else if("<%= cellType%>" == "driven"){
				elem = document.MAINFORM.<%=csph.getKeyElement(buyReadykey)%>select;
			}
			elem.disabled = true;
			if(navigator.appName == "Microsoft Internet Explorer"){
				elem.className = "disabled";
				elem.onfocus = function(){this.blur();};
			}else {
				elem.setAttribute("class","disabled");
				elem.setAttribute("onfocus","this.blur()");
			}
			
		}
		<%}%>
	}
		if("<%=sparclifecycle%>" == release || ("<%=oldColorwayLifeCycle%>" == release && "<%=sparclifecycle%>" == dropped)){
			<%
				
			String releasedLockAtt = COLORWAY_RELEASED_LOCKED_ATTRIBUTE ;
			StringTokenizer  itrs = new StringTokenizer(releasedLockAtt,",");
			while (itrs.hasMoreTokens()) {
				String key = (String) itrs.nextToken();
				String cellType = (String) sku.getFlexType().getAttribute(key).getAttVariableType();
			
			%>
			
			if("<%=csph.getKeyElement(key)%>" != "cspKeyNotFound" ) {
				var elem = document.MAINFORM.<%=csph.getKeyElement(key)%>;
				if(elem == null && elem == undefined){return;}
				if(elem == null){return;}
				if("<%= cellType%>" == "object_ref"){
					if(elem.parentElement != null && elem.parentElement.parentElement != null && 
							elem.parentElement != undefined		&& elem.parentElement.parentElement != undefined ){
						elem.parentElement.parentElement.classList.add('pointer-events-disable-color');
					}
				} else if("<%= cellType%>" == "driven"){
					elem = document.MAINFORM.<%=csph.getKeyElement(key)%>select;
				}
				elem.disabled = true;
				if(navigator.appName == "Microsoft Internet Explorer"){
					elem.className = "disabled";
					elem.onfocus = function(){this.blur();};
				}else {
					elem.setAttribute("class","disabled");
					elem.setAttribute("onfocus","this.blur()");
				}
				
			}
			<%}%>
		} else if("<%=sparclifecycle%>" == sampleReady || ("<%=oldColorwayLifeCycle%>" == sampleReady && "<%=sparclifecycle%>" == dropped)){
			<%
				
			String samplereleasedLockAtt = COLORWAY_SAMPLE_READYLOCKED_ATTRIBUTE ;
			StringTokenizer  sampleitrs = new StringTokenizer(samplereleasedLockAtt,",");
			while (sampleitrs.hasMoreTokens()) {
				String sampleKeys = (String) sampleitrs.nextToken();
				String cellType = (String) sku.getFlexType().getAttribute(sampleKeys).getAttVariableType();
			
			%>
			//alert("2");
			if("<%=csph.getKeyElement(sampleKeys)%>" != "cspKeyNotFound" ) {
				var elem = document.MAINFORM.<%=csph.getKeyElement(sampleKeys)%>;
				if(elem == null && elem == undefined){return;}
				if(elem == null){return;}
				if("<%= cellType%>" == "object_ref"){
					if(elem.parentElement != null && elem.parentElement.parentElement != null && 
							elem.parentElement != undefined		&& elem.parentElement.parentElement != undefined ){
						elem.parentElement.parentElement.classList.add('pointer-events-disable-color');
					}
				} else if("<%= cellType%>" == "driven"){
					elem = document.MAINFORM.<%=csph.getKeyElement(sampleKeys)%>select;
				}
				elem.disabled = true;
				if(navigator.appName == "Microsoft Internet Explorer"){
					elem.className = "disabled";
					elem.onfocus = function(){this.blur();};
				}else {
					elem.setAttribute("class","disabled");
					elem.setAttribute("onfocus","this.blur()");
				}
			}
			<%}%>
		}
		<%} 		
%>
	}

    // ***** End of function pattern *************************************************

    function runLoadFunctions(){ 
		disableAttributes();
		//alert("few of the Attributes are Locked");
    }


</script>

<script>runLoadFunctions();</script>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////// End of onLoad Client Side Plugin ////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

