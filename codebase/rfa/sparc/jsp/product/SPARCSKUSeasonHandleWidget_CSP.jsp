<%-- Copyright (c) 2006 PTC, Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////   widget  PAGE DOCUMENTATION    //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%--
    JSP Type: ClientSidePlugin

    SPARCSKUSeasonHanldeWidget_CSP.jsp:   // Replace with actual filename //
  
--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.client.Activities,                
                com.lcs.wc.client.web.*,    
                com.lcs.wc.util.*,
                com.lcs.wc.db.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.color.*,
                com.lcs.wc.foundation.*,
                wt.ownership.*,
				com.lcs.wc.product.LCSProduct,
				com.lcs.wc.season.LCSProductSeasonLink,
				com.lcs.wc.season.LCSSeason,
				com.lcs.wc.product.LCSSKU,
				com.lcs.wc.season.LCSSKUSeasonLink,
				com.lcs.wc.season.LCSSeasonProductLink,
				com.lcs.wc.season.LCSSeasonQuery,
				com.sparc.wc.services.SparcSKUSeasonAttributeTransitionGuard,
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
    public static final String JSPNAME = "widgetUpdatePaletteColor_CSP";
    public static final boolean DEBUG = true;
    public static String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
	private static String BUY_READY = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.BUYREADY.Status");
	private static String SAMPLE_READY = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.SAMPLEREADY.Status");
	private static String DROPPED = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.DROPPED.Status");
	private static String PLANNED = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.PLANNED.Status");
	private static String RELEASED = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.RELEASED.Status");

	private static String COLORWAY_LIFECYCLE = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.Status");

    private static final String SKU_SEASON_PLM_COLORWAY_STATUS_INTERNAL_NAME  = LCSProperties.get("transition.guard.sku.season.plm.colorway.status.internal.name", "scPLMPolorwayStatus");

	public static final String COLORWAY_ERROR_MSG = LCSProperties.get("com.lcs.wc.product.ColorwaySeason.GenericErrorMessage");
	public static final String APPROVAL_ATT_ERROR_MSG = LCSProperties.get("com.lcs.wc.product.ColorwaySeason.ApprovalAtt.GenericErrorMessage");
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
    String ownerId = request.getParameter("ownerId");
    String contextSKUId = request.getParameter("contextSKUId");
    String seasonId = request.getParameter("seasonId");
	String type = request.getParameter("type"); 
	boolean checkuserGroups = true;
    //String attributeOid = request.getParameter("attributeOid"); // use for Multi-Object CSPs only
	
	String sparclifecycle = null;
	LCSSKUSeasonLink skuSeasonLink = null;
	if (FormatHelper.hasContent(adminGroup)) {
	StringTokenizer groupsTokenizer = new StringTokenizer(adminGroup, ",");
	while (groupsTokenizer.hasMoreTokens()) {
		String groupsName = (String) groupsTokenizer.nextToken();
		Collection<String> gropus = lcsContext.getGroups();
		System.out.println("----gropus----"+gropus);
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
	try{
    if(ownerId.contains("LCSSKU")){
		LCSSKU skuObj = (LCSSKU)LCSQuery.findObjectById(ownerId);
		LCSSeason season = (LCSSeason)LCSQuery.findObjectById(seasonId);
		
		skuSeasonLink = (LCSSKUSeasonLink) LCSSeasonQuery.findSeasonProductLink(skuObj, season);
		sparclifecycle=(String)skuSeasonLink.getValue(COLORWAY_LIFECYCLE);
		
	}
	}catch(Exception ex){
		System.out.println("---ownerId---"+ownerId);
	}
    String flexTypeLabel = "";
    String flexTypePath = "";

     
    boolean hasAdminAccess = (FormatHelper.hasContent(adminGroup) && lcsContext.inGroup(adminGroup.toUpperCase()));
    ClientSidePluginHelper csph = new ClientSidePluginHelper();
    csph.init(type);
    String flextypeName = csph.getFlexTypeName();
    
    ArrayList attKeyNamesToHandle = new ArrayList();
    //Add a line to add the attribute keyName to the attKeyNames Vector
    //attKeyNamesToHandle.add("keyName");
    attKeyNamesToHandle.add(COLORWAY_LIFECYCLE);
    attKeyNamesToHandle.add(SKU_SEASON_PLM_COLORWAY_STATUS_INTERNAL_NAME);
	System.out.println("sparclifecycle"+sparclifecycle);
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ///////////////////////////////// JAVSCRIPT PLUGIN LOGIC ////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

<script>
    <%-- ###########################################################################################/--%>
    <%-- Do not Alter this method. This is used to Automatically generate a handler routing function/--%>

 <%= csph.drawWidgetJsFunctionHandler(attKeyNamesToHandle)%>

    <%-- ###########################################################################################/--%>

    // ***** Begin function pattern *************************************************
	function <%=csph.getJsFunctionName(COLORWAY_LIFECYCLE) %>(widgetObj){ 
		var selectedValue = widgetObj.value;
		var planned = "<%= PLANNED %>";
		var buyReady = "<%= BUY_READY %>";
		var dropped = "<%= DROPPED %>";
		var sampleReady = "<%= SAMPLE_READY %>";
		var release = "<%= RELEASED %>";
		var sparclifecycle = "<%= DROPPED %>";
		var throwAlert = false;
		var throwEmptyOrNullCheck = false ;
		
<%-- 		<%if(skuSeasonLink.getFlexType().getFullNameDisplay().toString().contains("Reebok")){%> --%>
		
		if("<%= sparclifecycle %>" === dropped && <%= checkuserGroups%>){
			if(selectedValue == buyReady ||selectedValue == sampleReady || selectedValue == release || selectedValue == planned){
				throwAlert = true;
			}
		}
		else  if ("<%= sparclifecycle %>" === buyReady && <%= checkuserGroups%>){
				if(selectedValue == sampleReady || selectedValue == release || selectedValue == planned){
					throwAlert = true;
				}
			}else if("<%= sparclifecycle %>" === sampleReady && <%= checkuserGroups%>){
				if(selectedValue == release || selectedValue == planned){
					throwAlert = true;
				}
			}else if("<%= sparclifecycle %>" === release && <%= checkuserGroups%>){
				if(selectedValue == planned){
					throwAlert = true;
				}
			}

		if(selectedValue === buyReady ){
			<% 
			String testingSigningOff = null;
			String developmentSigningOff = null;
			String customSigningOff =  null;
			try{
				LCSProductSeasonLink productSeasonLinkObj = skuSeasonLink.getProductLink(); 
				 testingSigningOff = (String) skuSeasonLink.getValue("sctestsignoff");
				 developmentSigningOff = (String) skuSeasonLink.getValue("scdevsignoff");
				customSigningOff = (String) skuSeasonLink.getValue("sccustomssignoff");
				
			}catch(Exception ex){}
			%>
			
			var testingSigingOffVal =document.MAINFORM.<%= csph.getKeyElement("sctestsignoff")%>;
			var developmentSigingOffVal =document.MAINFORM.<%= csph.getKeyElement("scdevsignoff")%>;
			var customSigingOffVal =document.MAINFORM.<%= csph.getKeyElement("sccustomssignoff")%>;
			if( testingSigingOffVal.value.trim() === ""  || developmentSigingOffVal.value.trim() === "" || 
					customSigingOffVal.value.trim() === "" ){
				throwEmptyOrNullCheck = true ; 
			
			}else if( testingSigingOffVal.value === null  || developmentSigingOffVal.value === null || 
					customSigingOffVal.value === null ){
				throwEmptyOrNullCheck = true ; 
			
			}
			else if(testingSigingOffVal.value.trim() === "scNo" || developmentSigingOffVal.value.trim() === "scNo"
					||customSigingOffVal.value.trim() === "scNo"){
				throwEmptyOrNullCheck = true ; 
			}
	}
	
		if(throwAlert){
			
			var vals = document.MAINFORM.<%= csph.getKeyElement(COLORWAY_LIFECYCLE)%>;
			
			vals.value= "<%= sparclifecycle %>";//screleased
			var setOldSatusVal ;
			//START - FP-166 - Change from released to planned/ Sample ready to Released, alert is thrown but on 
			//clicking ok it is showing Previous state before Save
			if("<%=sparclifecycle%>" == dropped){setOldSatusVal = "Dropped";}
			else if("<%=sparclifecycle%>" == release){setOldSatusVal = "Released";}
			else if("<%=sparclifecycle%>" == sampleReady){setOldSatusVal = "Sample Ready";}
			else if("<%=sparclifecycle%>" == buyReady){setOldSatusVal = "Buy Ready";}
			else if("<%=sparclifecycle%>" == planned){setOldSatusVal = "Planned";}
			
			var elemID = "select2-" + "<%=csph.getKeyElement(COLORWAY_LIFECYCLE)%>" + "-container";
			var elem = document.getElementById(elemID);
			if(elem != null && elem != undefined && setOldSatusVal != null ){
				elem.textContent = setOldSatusVal;
			}
			alert("<%=COLORWAY_ERROR_MSG%>");
		}
		if(throwEmptyOrNullCheck){
			var vals = document.MAINFORM.<%= csph.getKeyElement(COLORWAY_LIFECYCLE)%>;
			
			vals.value= "<%= sparclifecycle %>";//screleased
			var setOldSatusVal ;
			//START - FP-166 - Change from released to planned/ Sample ready to Released, alert is thrown but on 
			//clicking ok it is showing Previous state before Save
			if("<%=sparclifecycle%>" == dropped){setOldSatusVal = "Dropped";}
			else if("<%=sparclifecycle%>" == release){setOldSatusVal = "Released";}
			else if("<%=sparclifecycle%>" == sampleReady){setOldSatusVal = "Sample Ready";}
			else if("<%=sparclifecycle%>" == buyReady){setOldSatusVal = "Buy Ready";}
			else if("<%=sparclifecycle%>" == planned){setOldSatusVal = "Planned";}
			
			var elemID = "select2-" + "<%=csph.getKeyElement(COLORWAY_LIFECYCLE)%>" + "-container";
			var elem = document.getElementById(elemID);
			if(elem != null && elem != undefined && setOldSatusVal != null ){
				elem.textContent = setOldSatusVal;
			}
			alert("<%= APPROVAL_ATT_ERROR_MSG %>");
		}
<%-- 	<%}%> --%>
	}
    // ***** End function pattern *************************************************

    	function <%=csph.getJsFunctionName(SKU_SEASON_PLM_COLORWAY_STATUS_INTERNAL_NAME) %>(widgetObj){
    		const selectedValue = widgetObj.value;
    		const restrictedValues = [];
    		<% final SparcSKUSeasonAttributeTransitionGuard transitionGuard = SparcSKUSeasonAttributeTransitionGuard.getInstance(); %>
            const prevSelectedvalue = "<%=transitionGuard.getCurrentColorwayStatus(skuSeasonLink)%>";
            <% final Set<String> restrictedTransitions = transitionGuard.getRestrictedColorwayStatusTransitions(skuSeasonLink); %>
            <%
               for(String transition: restrictedTransitions) {
             %>
             restrictedValues.push("<%=transition%>");
             <%
             }
            %>
            if(restrictedValues.includes(selectedValue)){
               alert("Cannot change PLM Colorway Status with current login");
            }

    	}

    <%-- ###########################################################################################/--%>
    <%-- Do not Remove this method. This is used to alert the user that a key is no longer available/--%>
    function keyNotValid(keyName){ 
	
       alert(" The flextype [ <%= flextypeName %> ] no longer Supports" + "\r" +
             "  the keyname [ " + keyName + " ]. " + "\r" + "\r" +
             "  Please call you PDM Admin group and inform them that " + "\r" +
             "  this message was displayed.")
             
    }



</script>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////// End of widget Client Side Plugin ////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
