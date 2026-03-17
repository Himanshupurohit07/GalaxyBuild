<%-- Copyright (c) 2006 PTC, Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////   onLoad  PAGE DOCUMENTATION    //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%--
    JSP Type: ClientSidePlugin

    SPARCColorOnLoad_CSP.jsp:  
  
--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////  Add to the import package/class list as required  ////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page import="com.lcs.wc.whereused.FAWhereUsedQuery"%>
<%@page import="com.lcs.wc.color.LCSColor"%>
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
    public static final String JSPNAME = "SPARCColorOnLoad_CSP";
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
	
	private static String COLOR_RELEASED_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.color.LCSColor.LockedAttribute.Released.Status.Keys");
	private static String COLOR_BUYREADY_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.color.LCSColor.LockedAttribute.BuyReady.Status.Keys");
	private static String COLOR_SAMPLEREADY_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.color.LCSColor.LockedAttribute.SampleReady.Status.Keys");
	
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
    if (FormatHelper.hasContent(adminGroup)) {
    	StringTokenizer groupsTokenizer = new StringTokenizer(adminGroup, ",");
    	while (groupsTokenizer.hasMoreTokens()) {
    		String groupsName = (String) groupsTokenizer.nextToken();
    		if(groupsName == null){return;}
    		Enumeration<?> enumeration = UserGroupHelper.getWTUsers();
    		boolean orgAdmin = UserGroupHelper.isOrgAdmin();
    		boolean siteAdmin = UserGroupHelper.isSiteAdmin();
    		Collection<String> gropus = lcsContext.getGroups();
    		if(gropus == null){return;}
    		String userName = lcsContext.getUserName();
    		Iterator<String> groupsItr = gropus.iterator();
    		while (groupsItr.hasNext()) {
    			String grpName = groupsItr.next();
    			if (grpName != null && grpName.equals(groupsName)) {
    					checkuserGroups = false;
    					break;
    			}
    		}

    	}

    }
    LCSSeasonQuery sQuery = new LCSSeasonQuery();
    
	LCSColor color = (LCSColor)LCSQuery.findObjectById(oid);
	
	
    
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
		if(checkuserGroups){
		FAWhereUsedQuery whereUsedQuery = new FAWhereUsedQuery();
		Collection<FlexObject> collection =  whereUsedQuery.checkForObjectReferences(color);
		String sparclifecycle="";
		if(collection == null) {return;}
		Iterator<FlexObject> whereUsedIter = collection.iterator();
		while (whereUsedIter.hasNext()) {
			FlexObject flexObject = (FlexObject) whereUsedIter.next();
			String objectClass = flexObject.getString("CLASS");
			String objectOID = flexObject.getString("OID");
			String whereUsedClass = flexObject.getString("WHEREUSEDCLASS");
			if(objectClass.equals("com.lcs.wc.product.LCSSKU")) {
				LCSSKU lcssku = (LCSSKU) LCSQuery.findObjectById(whereUsedClass+":"+objectOID);
				LCSSeasonMaster lcsSeasonMaster = null;
				@SuppressWarnings({ "unchecked" })
				Collection<LCSSeasonMaster> masters = new LCSSeasonQuery().findSeasons(lcssku.getMaster());
				if (masters != null) {
					Iterator<LCSSeasonMaster> masterIter = masters.iterator();
					while (masterIter.hasNext()) {
						lcsSeasonMaster = (LCSSeasonMaster) masterIter.next();
						LCSSeason lcsSeason = (LCSSeason) VersionHelper.latestIterationOf(lcsSeasonMaster);
						if (lcsSeason != null) {
							LCSSKUSeasonLink lcsSeasonProductLink = (LCSSKUSeasonLink) LCSSeasonQuery
									.findSeasonProductLink(lcssku, lcsSeason);
							 sparclifecycle = (String) lcsSeasonProductLink.getValue(COLORWAY_LIFECYCLE);
							LCSSKUSeasonLink prevSeasonLink = null;
							String oldColorwayLifeCycle = null;
							if (lcsSeasonProductLink != null  ) {
								prevSeasonLink=(LCSSKUSeasonLink)LCSSeasonQuery.getPriorSeasonProductLink(lcsSeasonProductLink);

							}
							if (prevSeasonLink != null) {
								// get old version of colorway lifecycle value
								oldColorwayLifeCycle = (String) prevSeasonLink.getValue(COLORWAY_LIFECYCLE);
								System.out.println("--------oldColorwayLifeCycle--------"+oldColorwayLifeCycle + "----sparclifecycle----"+sparclifecycle);
							}
						
					
		
		
	%>
	
	if("<%=sparclifecycle%>" == buyReady || ("<%=oldColorwayLifeCycle%>" == buyReady && "<%=sparclifecycle%>" == dropped)){
		<%
			
		String buyReadyLockAtt = COLOR_BUYREADY_LOCKED_ATTRIBUTE ;
		StringTokenizer  buyReadyItr = new StringTokenizer(buyReadyLockAtt,",");
		while (buyReadyItr.hasMoreTokens()) {
			String buyReadykey = (String) buyReadyItr.nextToken();
			String cellType = (String) color.getFlexType().getAttribute(buyReadykey).getAttVariableType();
		
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
				
			String releasedLockAtt = COLOR_RELEASED_LOCKED_ATTRIBUTE ;
			StringTokenizer  itrs = new StringTokenizer(releasedLockAtt,",");
			while (itrs.hasMoreTokens()) {
				String key = (String) itrs.nextToken();
				String cellType = (String) color.getFlexType().getAttribute(key).getAttVariableType();
			
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
		}
		 else if("<%=sparclifecycle%>" == sampleReady || ("<%=oldColorwayLifeCycle%>" == sampleReady && "<%=sparclifecycle%>" == dropped)){
			<%
				
			String samplereleasedLockAtt = COLOR_RELEASED_LOCKED_ATTRIBUTE ;
			StringTokenizer  sampleitrs = new StringTokenizer(samplereleasedLockAtt,",");
			while (sampleitrs.hasMoreTokens()) {
				String sampleKeys = (String) sampleitrs.nextToken();
				String cellType = (String) color.getFlexType().getAttribute(sampleKeys).getAttVariableType();
			
			%>
		
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
	
					<%	
						}
						}
				}
			}
		}
		}	%>
						
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

