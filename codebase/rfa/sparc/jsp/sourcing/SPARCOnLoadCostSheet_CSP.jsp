<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////   onLoad  PAGE DOCUMENTATION    //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%--
    JSP Type: ClientSidePlugin

    SPARConLoadCostSheet_CSP.jsp:
  
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
                com.lcs.wc.foundation.*,
                java.util.*,
				wt.fc.WTObject,
				org.apache.logging.log4j.LogManager,
				org.apache.logging.log4j.Logger,
				com.lcs.wc.specification.*,
				com.lcs.wc.season.LCSSeason,
				com.lcs.wc.sourcing.*,
				com.lcs.wc.flexbom.*,
				com.lcs.wc.season.*,
				com.lcs.wc.supplier.*,
				java.util.stream.*,
				wt.method.*"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ///////////////////// INITIALIZATION JSP CODE and CSP environment ///////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%     

    String activity = request.getParameter("activity");
    String action = request.getParameter("action");
    String oid = request.getParameter("oid");
    String type = request.getParameter("type");
	String oldBOMValue = "";
	String currentPortValue = "";
	String oldPortValue = "";

    logger.debug("onLoad..type = " + type);   
    logger.debug("onLoad...activity = " + activity);   
    logger.debug("onLoad...action = " + action);
	logger.debug("onLoad...oid = " + oid);	
	
    if(MethodContext.getContext().get("oldBOMValueFromCopyWhatIFCostSheet")!=null){
		oldBOMValue = (String)MethodContext.getContext().get("oldBOMValueFromCopyWhatIFCostSheet");
		logger.debug("oldBOMValue from CopyWhatIFCostSheet:"+oldBOMValue);
		MethodContext.getContext().remove("oldBOMValueFromCopyWhatIFCostSheet");
	}
	else if(MethodContext.getContext().get("oldBOMValueFromCopyCostSheet")!=null){
		oldBOMValue = (String)MethodContext.getContext().get("oldBOMValueFromCopyCostSheet");
		logger.debug("oldBOMValue from oldBOMValueFromCopyCostSheet:"+oldBOMValue);
		MethodContext.getContext().remove("oldBOMValueFromCopyCostSheet");
	}
	logger.debug("oldBOMValue - CSP - AFTER REMOVAL:"+(String)MethodContext.getContext().get("oldBOMValue"));
    ClientSidePluginHelper csph = new ClientSidePluginHelper();
    csph.init(type);
    String flextypeName = csph.getFlexTypeName();
    FlexType flextype = csph.getFlexType();
    logger.debug("***1:"+!oid.contains("FlexSpecToSeasonLink"));
	logger.debug("***2:"+!oid.contains("LCSProductCostSheet"));
	logger.debug("***3:"+flextype.getFullName());
	logger.debug("***4:"+flextype.getFullName().indexOf("scApparelBreakdown"));
	logger.debug("***flextype:"+flextype.getFullName());
	String bomNameAttrKey = "scBOM";
	
	/*if(((flextype.getFullName().indexOf("scApparelBreakdown") < 1) && (flextype.getFullName().indexOf("scFootwearBreakdown") < 1))&& (!(activity.equalsIgnoreCase("WHATIF_MULTI_COSTSHEET") && action.equalsIgnoreCase("INIT")))){
			logger.debug("***Returnig from SPARCOnLoadCostSheet_CSP.jsp as its not Apparel BreakDown Costsheet");
			return;
		}
		*/
		
	logger.debug("***Proceeding from SPARCOnLoadCostSheet_CSP.jsp as Season Spec is  selected or It's a Product Create/Update page or Copy as WhatIf or Copy As Active is performed that has BOM Name populated");
	
	LCSSourcingConfigMaster scMaster = null;
	LCSSourcingConfig sourcingConfig = null;
	LCSProductCostSheet costsheet= null;
	ArrayList<String> listOfBOMs = null;
	LCSSeasonMaster seasonMaster = null;
	FlexSpecMaster specMaster = null;
	FlexSpecification spec = null;
	String currentBOMValue="";
	Map<String, String> listOfPorts = null;
	//Map<String, String> listOfCSPorts = null;
	String portAttKey = "scPortOfOrigin";
	
	FlexType csFlextype = FlexTypeCache.getFlexTypeFromPath("Cost Sheet");
	logger.debug("**flextype:"+csFlextype);
	
	if(oid.contains("FlexSpecToSeasonLink")){
		FlexSpecToSeasonLink flexSpecToSeasonLink = (FlexSpecToSeasonLink) LCSQuery.findObjectById(oid);
		logger.debug("**flexSpecToSeasonLink:"+flexSpecToSeasonLink);
		if(flexSpecToSeasonLink!=null){
		LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(flexSpecToSeasonLink.getSeasonMaster());
		logger.debug("**season:"+season);
		spec = (FlexSpecification)  VersionHelper.latestIterationOf(flexSpecToSeasonLink.getSpecificationMaster());
		logger.debug("**spec:"+spec);
		scMaster = spec.getSpecSource();
		
		listOfBOMs= getBOMs(spec);
		listOfPorts = getPorts(getSourcingConfig(scMaster));
		logger.debug("**listOfPorts ::"+listOfPorts);
		
		//listOfCSPorts = getAllAvailablePorts(csFlextype);
		//logger.debug("**listOfCSPorts:::::"+listOfCSPorts);	
		}
	}
	else if (oid.contains("LCSProductCostSheet")){
		logger.debug("**LCSProductCostSheet:");
			costsheet = (LCSProductCostSheet) LCSCostSheetQuery.findObjectById(oid);
			currentBOMValue	=(String)costsheet.getValue(bomNameAttrKey);
			logger.debug("**currentBOMValue:"+currentBOMValue);	
			currentPortValue = getDisplayValues(costsheet, null ,portAttKey, (String)costsheet.getValue(portAttKey));	
			//currentPortValue = (String)costsheet.getValue(portAttKey);
			logger.debug("**currentPortValue::::"+currentPortValue);
			specMaster = costsheet.getSpecificationMaster();
			seasonMaster = costsheet.getSeasonMaster();

			if(specMaster!=null && seasonMaster!=null){
				spec = (FlexSpecification)  VersionHelper.latestIterationOf(specMaster);
				logger.debug("**spec111:"+spec);
				listOfBOMs= getBOMs(spec);
			}
			scMaster = costsheet.getSourcingConfigMaster();

			listOfPorts = getPorts(getSourcingConfig(scMaster));
						
			//listOfCSPorts = getAllAvailablePorts(costsheet.getFlexType());
			//logger.debug("**listOfCSPorts:::::"+listOfCSPorts);	
		}else if(oid.contains("LCSSourceToSeasonLink")){
			LCSSourceToSeasonLink ssLink = (LCSSourceToSeasonLink) LCSQuery.findObjectById(oid);
			scMaster = ssLink.getSourcingConfigMaster();
	
			listOfPorts = getPorts(getSourcingConfig(scMaster));
			logger.debug("* LCSSourceToSeasonLink listOfPorts ::"+listOfPorts);
			
			//listOfCSPorts = getAllAvailablePorts(csFlextype);
			//logger.debug("*LCSSourceToSeasonLink listOfCSPorts:::::"+listOfCSPorts);	
		}

%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JSP METHODS /////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%! 
	private static final Logger logger = LogManager.getLogger("rfa.sparc.jsp.sourcing.SPARCOnLoadCostSheet_CSP.jsp");
    public static final String JSPNAME = "ClientSidePlugin";
    public static final boolean DEBUG = true;
	
	public static ArrayList<String> getBOMs(FlexSpecification spec) throws Exception {
		Collection<FlexBOMPart> boms = FlexSpecQuery.getSpecComponents(spec,"BOM");
		logger.debug("**boms1:"+boms);
		Collection boms1 = SortHelper.sortedPersistablesByCreateStamp(boms);
		logger.debug("**boms2:"+boms1);
		FlexBOMPart bomPart= null;
		ArrayList<String> listOfBOMValues = new ArrayList<String>();
		for (Object bom: boms1)		{
			bomPart = (FlexBOMPart)bom;
			if (!"LABOR".equals(bomPart.getBomType())) {
				listOfBOMValues.add(bomPart.getName());
			}
		}
		logger.debug("lstOfBOMValues:"+listOfBOMValues);
		
		
		return listOfBOMValues;
	}
	//logger.debug("MethodContext.getContext():"+MethodContext.getContext(Thread.currentThread()));
	
	public static LCSSourcingConfig getSourcingConfig(LCSSourcingConfigMaster scMaster) throws Exception{
		LCSSourcingConfig scConfig = null;
		if(null != scMaster){
			scConfig = (LCSSourcingConfig) VersionHelper.latestIterationOf(scMaster);
		}
		return scConfig;
	}
	
	public static Map<String, String> getPorts(LCSSourcingConfig sConfig) throws Exception{
		LCSSupplier supplierObj = null;
		HashMap<String, String> listOfPortValues = new HashMap<String, String>();
		Map<String, String> sortedMap = new LinkedHashMap<String, String>();
		
		try{
		logger.info("**scFGFactory :"+sConfig.getValue("scFGFactory"));
		supplierObj = (LCSSupplier)sConfig.getValue("scFGFactory");
		
		if(null != supplierObj){
			logger.info("**supplierObj scShippingPort values:"+supplierObj.getValue("scShippingPort"));
			
			if(null != supplierObj.getValue("scShippingPort")){
				StringTokenizer shippingPort = new StringTokenizer(supplierObj.getValue("scShippingPort").toString(), "|~*~|");
				logger.info("**shippingPort :"+shippingPort);
							
				while (shippingPort.hasMoreTokens()){
					 String strToken = shippingPort.nextToken();
					 listOfPortValues.put(strToken, getDisplayValues(supplierObj,null, "scShippingPort",strToken));
				 }
			}
		}
		 
		sortedMap = listOfPortValues.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,(e1,e2)->e1,LinkedHashMap::new));
		
		}catch(Exception e){
			logger.debug("Inside catch :"+e.getMessage());
			System.out.println("Inside catch :"+e.getMessage());
		}
		return sortedMap;
	}
	
	public static String getDisplayValues(WTObject Obj,FlexType type,  String AttKey, String tokenValueKey) throws Exception{
		String displayValue = "";
		AttributeValueList attrValueList = null;
		
		if(Obj == null && null != type){
			attrValueList = type.getAttribute(AttKey).getAttValueList();
		}
		
		if(Obj instanceof LCSSupplier){
			LCSSupplier supObj = (LCSSupplier)Obj;
			attrValueList = supObj.getFlexType().getAttribute(AttKey).getAttValueList();
		}else if(Obj instanceof LCSProductCostSheet){
			LCSProductCostSheet csObj = (LCSProductCostSheet)Obj;
			attrValueList = csObj.getFlexType().getAttribute(AttKey).getAttValueList();
		}
				
		if(null != attrValueList){
			displayValue = attrValueList.getValue(tokenValueKey,Locale.getDefault());
			logger.info("**displayValue :"+displayValue);
		}
		return displayValue;
	}
	
	public static Map<String, String> getAllAvailablePorts(FlexType type) throws Exception{
		logger.info("*Inside getAllAvailablePorts ");
		HashMap<String, String> listOfCSPortValues = new HashMap<String, String>();
		
		AttributeValueList portValueList = type.getAttribute("scPortofOrigin").getAttValueList();
		logger.info("*portValueList :"+portValueList);
			Collection<String> portsList = portValueList.getKeys();
			logger.info("**portsList :"+portsList);
			Iterator portsItr = portsList.iterator();
			while(portsItr.hasNext()){
				String key = portsItr.next().toString();
				listOfCSPortValues.put(key, getDisplayValues(null, type,"scPortofOrigin",key));
			}
			
			Map<String, String> sortedMap = listOfCSPortValues.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,(e1,e2)->e1,LinkedHashMap::new));
			
			return sortedMap;
	}
	
	
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ///////////////////////////////// JAVSCRIPT PLUGIN LOGIC ////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

<script>

    function populateBOMName(){
	
	if("<%= csph.getKeyElement("scBOM") %>" != "cspKeyNotFound") {
		
		var bomNameElement = document.MAINFORM.<%= csph.getKeyElement("scBOM")%>;
		if(bomNameElement != null){
			<% 
			if (listOfBOMs!=null){
			for (String bom:listOfBOMs){
				logger.debug("**Processing :"+bom);
			%>
				var option = document.createElement("option");
				option.value = '<%= bom %>';
				option.text = '<%= bom %>';
					if('<%= bom %>' =='<%= currentBOMValue %>' || ('<%= oldBOMValue %>'!=null && '<%= oldBOMValue %>'!=''  && '<%= bom %>' == '<%= oldBOMValue%>' )){						
					option.selected=true;
				}
				bomNameElement.appendChild(option);
			<% } } %>
			}
		}
	}
	
	 function populatePortOfOrigin(){
		 if("<%= csph.getKeyElement("scPortOfOrigin") %>" != "cspKeyNotFound") {
			 var portElement = document.MAINFORM.<%= csph.getKeyElement("scPortOfOrigin")%>;
			 
			 if(portElement != null){
				   <% 
					if (listOfPorts!=null){
						logger.debug("*listOfPorts size.... "+listOfPorts.size());
						if(listOfPorts.size() > 1){
							for (String portKey:listOfPorts.keySet()){
								String portValue = listOfPorts.get(portKey);
								logger.debug("If there are multiple ports selected on the Finished Goods Factory");
								logger.debug("portKey : "+portKey+" portValue : "+listOfPorts.get(portKey)+" =currentPortValue = "+currentPortValue);
								%>
								var portOptions = document.createElement("option");
								portOptions.title = '<%= portValue %>';
								portOptions.value = '<%= portKey %>';
								portOptions.text = '<%= portValue %>';
								
								if('<%= portValue %>' =='<%= currentPortValue %>' || ('<%= oldPortValue %>'!=null && '<%= oldPortValue %>'!=''  && '<%= portValue %>' == '<%= oldPortValue%>' )){
									portOptions.selected=true;
								}
								portElement.appendChild(portOptions);
								<%
							}
						}
						else if (listOfPorts.size() == 1){
							//If there is only one port then populating that port on the Costsheet POO
							logger.debug("If there is only one port then populating that port on the Costsheet POO");
							for (String portKey:listOfPorts.keySet()){
								String portValue = listOfPorts.get(portKey);
							%>
							var portOptions = document.createElement("option");
							portOptions.title = '<%= portValue %>';
							portOptions.value = '<%= portKey %>';
							portOptions.text = '<%= portValue %>';
							portElement.appendChild(portOptions);
							
							portOptions.selected=true;
							<%
							}
						}
					}
						%>
			 }
		 }
	 }
	 
	function runLoadFunctions(){
		<%
		if(flextype.getFullName().contains("scApparelBreakdownAeropostale") || flextype.getFullName().contains("scApparelHighLevelAeropostale") ){	
		%>
			populatePortOfOrigin();
		<%
		}
		%>
		
		<%
		if(!(flextype.getFullName().contains("scApparelHighLevelAeropostale")) && ((oid.contains("FlexSpecToSeasonLink")) || (oid.contains("LCSProductCostSheet")))){
			%>
			populateBOMName();
		<%
		}
		%>
			
		}
</script>

<script>runLoadFunctions();</script>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////// End of onLoad Client Side Plugin ////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

