<%-- Copyright (c) 2006 PTC, Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////   onLoad  PAGE DOCUMENTATION    //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%--
    JSP Type: ClientSidePlugin

    SPARCProductSeasonOnLoad_CSP.jsp:    
  
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
				com.lcs.wc.season.SeasonProductLocator,
				com.lcs.wc.season.LCSSeasonProductLink,
				com.lcs.wc.product.LCSProduct,
				com.lcs.wc.season.LCSProductSeasonLink,
                java.util.*"%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext"
	scope="session" />

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

	private static String BUY_READY_SIGN_OFF_LOCKED_ATT = LCSProperties.get("com.sparc.jsp.product.disableSignOff.Attribute");
	private static String PROD_SEASON_RELEASED_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSProductSeasonLink.LockedAttribute.Released.StatusKeys");
	private static String PROD_SEASON_SAMPLE_READY_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSProductSeasonLink.LockedAttribute.SampleReady.Status.Keys");
	private static String PROD_SEASON_BUY_READY_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSProductSeasonLink.LockedAttribute.BuyReady.Status.Keys");
	
	public static final String adminGroup = LCSProperties.get("com.lcs.wc.client.adminGroups.name");%>

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
	String seasonId = request.getParameter("seasonId");
    String type = request.getParameter("type"); 
	String sparclifecycle = null; 
	boolean checkuserGroups = true;
	String oid = request.getParameter("oid");
	

    
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
	var alertThrow = false ;
    function disableAttributes(){ 
		var planned = "<%= PLANNED %>";
		var buyReady = "<%= BUY_READY %>";
		var dropped = "<%= DROPPED %>";
		var sampleReady = "<%= SAMPLE_READY %>";
		var release = "<%= RELEASED %>";
		
		<%
		
		if(ownerId.contains("LCSProduct")){
		LCSProduct lcsproduct = (LCSProduct)LCSQuery.findObjectById(ownerId);
		if(lcsproduct.getFlexType().getFullNameDisplay().toString().contains("Reebok")){
		LCSSeason season = (LCSSeason)LCSQuery.findObjectById(seasonId);
	   
		LCSProductSeasonLink prodSeasonLink = (LCSProductSeasonLink) LCSSeasonQuery.findSeasonProductLink(lcsproduct, season); 
		if(prodSeasonLink == null){return;}
		Collection colorways = LCSSKUQuery.findSKUs(lcsproduct);
		if(colorways == null){return;}
		Iterator colorwayItr = colorways.iterator();
		
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
			String buyReadyLockAtt = PROD_SEASON_BUY_READY_LOCKED_ATTRIBUTE ;
			StringTokenizer  buyTokenizer = new StringTokenizer(buyReadyLockAtt,",");
			while (buyTokenizer.hasMoreTokens()) {
				String releasedKeys = (String) buyTokenizer.nextToken();
				releasedKeys.trim();
				String cellType = (String) prodSeasonLink.getFlexType().getAttribute(releasedKeys).getAttVariableType();
				System.out.println("cellType = "+ cellType );
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
			
			
		}
		if( "<%=sparclifecycle%>" ==release || ("<%=oldColorwayLifeCycle%>" == release && "<%=sparclifecycle%>" == dropped)){
			<%
			String releasedLockAtt = PROD_SEASON_RELEASED_LOCKED_ATTRIBUTE ;
			StringTokenizer  tokenizer = new StringTokenizer(releasedLockAtt,",");
			while (tokenizer.hasMoreTokens()) {
				String releasedKeys = (String) tokenizer.nextToken();
				releasedKeys.trim();
				String cellType = (String) prodSeasonLink.getFlexType().getAttribute(releasedKeys).getAttVariableType();
				
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
			
			
		} else if("<%=sparclifecycle%>" ==sampleReady || ("<%=oldColorwayLifeCycle%>" == sampleReady && "<%=sparclifecycle%>" == dropped)){
			<%
			String sampleLockAtt = PROD_SEASON_SAMPLE_READY_LOCKED_ATTRIBUTE ;
			StringTokenizer  sampleTokenizer = new StringTokenizer(sampleLockAtt,",");
			while (sampleTokenizer.hasMoreTokens()) {
				String sampleKeys = (String) sampleTokenizer.nextToken();
				String cellType = (String) prodSeasonLink.getFlexType().getAttribute(sampleKeys).getAttVariableType();
			%>
			
			
			if("<%=csph.getKeyElement(sampleKeys)%>" != "cspKeyNotFound") {
			
				var elem = document.MAINFORM.<%=csph.getKeyElement(sampleKeys)%>;
				if(elem == null && elem == undefined){return;}
				if("<%= cellType %>" == "driven"){
					elem = document.MAINFORM.<%=csph.getKeyElement(sampleKeys)%>select;
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
		
		<%}
		}}
	 %>
	}

    // ***** End of function pattern *************************************************
    
    
	function lockedSignOffAtt(){
		var buyReady = "<%= BUY_READY %>";
    	<%
    	if(!ownerId.contains("LCSSKU")){
    		System.out.println("Called from Product Season");
    	}else {
    		if(ownerId.contains("LCSSKU")){
    			LCSSKU sku = (LCSSKU) LCSQuery.findObjectById(ownerId);
    			if(sku.getFlexType().getFullNameDisplay().toString().contains("Reebok")){
    			LCSSeason season = (LCSSeason)LCSQuery.findObjectById(seasonId);
    			
    			LCSSKUSeasonLink skuSeasonLink = (LCSSKUSeasonLink) LCSSeasonQuery.findSeasonProductLink(sku, season);
    			String sparclifecycleVal =(String)skuSeasonLink.getValue(COLORWAY_LIFECYCLE); %>
    			if( "<%=sparclifecycleVal%>" === buyReady ){
    				<%
    				//String buyReadyLockAtt = "sctestsignoff,scdevsignoff,sccustomssignoff" ;
    				String buyReadyLockAtt = BUY_READY_SIGN_OFF_LOCKED_ATT ;
    				StringTokenizer  buyReady = new StringTokenizer(buyReadyLockAtt,",");
    				while (buyReady.hasMoreTokens()) {
    					String buyReadydKeys = (String) buyReady.nextToken();
    					String cellType = (String) skuSeasonLink.getFlexType().getAttribute(buyReadydKeys.trim()).getAttVariableType();
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
    			
    			<% }		}} %>
    	
    }
    function runLoadFunctions(){ 
		disableAttributes();
		lockedSignOffAtt();
		
		
    }


</script>

<script>runLoadFunctions();</script>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////// End of onLoad Client Side Plugin ////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

