<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// PAGE DOCUMENTATION//////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%--

--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.db.*,
            com.lcs.wc.calendar.*,
            com.lcs.wc.client.*,
            com.lcs.wc.client.web.*,
            com.lcs.wc.season.*,
            com.lcs.wc.season.query.*,
            com.lcs.wc.color.*,
            com.lcs.wc.util.*,
            com.lcs.wc.foundation.*,
            com.lcs.wc.product.*,
            com.lcs.wc.flextype.*,
            com.lcs.wc.sourcing.*,
            com.lcs.wc.db.*,
            com.google.inject.Guice,
            com.google.inject.Inject,
            com.google.inject.Injector,
            wt.fc.*,
            wt.util.*,
            wt.org.*,
            com.lcs.wc.infoengine.client.web.*,
            java.util.*"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="ftg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="seasonModel" scope="request" class="com.lcs.wc.season.LCSSeasonClientModel" />
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JAVA CODE ///////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%
//setting up which RBs to use
Object[] objA = new Object[0];
String Season_MAIN = "com.lcs.wc.resource.SeasonRB";
String RB_MAIN = "com.lcs.wc.resource.MainRB";
String Reports_MAIN = "com.lcs.wc.resource.ReportsRB";

String lifecycleStatus = WTMessage.getLocalizedMessage ( Season_MAIN, "lifecycleStatus_LBL",objA ) ;
String timeOfChange = WTMessage.getLocalizedMessage ( Season_MAIN, "timeOfChange_LBL",objA ) ;
String newString = WTMessage.getLocalizedMessage ( Season_MAIN, "newString_LBL",objA ) ;
String oldString = WTMessage.getLocalizedMessage ( Season_MAIN, "oldString_LBL",objA ) ;
String newNumeric = WTMessage.getLocalizedMessage ( Season_MAIN, "newNumeric_LBL",objA ) ;
String oldNumeric = WTMessage.getLocalizedMessage ( Season_MAIN, "oldNumeric_LBL",objA ) ;
String newCurrency = WTMessage.getLocalizedMessage ( Season_MAIN, "newCurrency_LBL",objA ) ;
String oldCurrency = WTMessage.getLocalizedMessage ( Season_MAIN, "oldCurrency_LBL",objA ) ;
String changedAttribute = WTMessage.getLocalizedMessage ( Season_MAIN, "changedAttribute_LBL",objA ) ;
String userID = WTMessage.getLocalizedMessage ( Season_MAIN, "userID_LBL",objA ) ;
String changedTo = WTMessage.getLocalizedMessage ( Season_MAIN, "changedTo_LBL",objA ) ;
String changedFrom = WTMessage.getLocalizedMessage ( Season_MAIN, "changedFrom_LBL",objA ) ;
String changeReportPgHead = WTMessage.getLocalizedMessage ( Season_MAIN, "changeReport_OPT",objA ) ;
String reportParametersPgHead = WTMessage.getLocalizedMessage ( Season_MAIN, "reportParameters_PG_HEAD",objA ) ;
String runLabel = WTMessage.getLocalizedMessage ( RB_MAIN, "run_LBL",objA ) ;
String seasonLabel = WTMessage.getLocalizedMessage ( RB_MAIN, "season_LBL",objA ) ;
String changedOnEndDate = WTMessage.getLocalizedMessage ( RB_MAIN, "changedOnEndDate_LBL",objA ) ;
String changedOnBeginDate = WTMessage.getLocalizedMessage ( RB_MAIN, "changedOnBeginDate_LBL",objA ) ;
String attributesLabel = WTMessage.getLocalizedMessage ( RB_MAIN, "attributes_LBL",objA ) ;
String fontSizeLabel = WTMessage.getLocalizedMessage ( Reports_MAIN, "fontSize_LBL",objA ) ;
String rowsPerPageLabel = WTMessage.getLocalizedMessage ( Reports_MAIN, "rowsPerPage_LBL",objA ) ;
String runInPrintModeLabel = WTMessage.getLocalizedMessage ( Reports_MAIN, "runInPrintMode_LBL",objA ) ;
String landscapeLegalLabel = WTMessage.getLocalizedMessage ( Reports_MAIN, "landscapeLegal_LBL",objA ) ;
String printerSetupUpRecommendation = WTMessage.getLocalizedMessage ( Reports_MAIN, "printerSetupUpRecommendation_LBL",objA ) ;
String changesExistingOn = WTMessage.getLocalizedMessage ( Season_MAIN, "changesExistingOn_LBL",objA ) ;
%>
<%!
	public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");
    public static String instance = "";
    public static String systemName = "";
    public static String GROUPBY_INPUT_DELIMITER = ", ";

    public static final String SEASON_HEADER = PageManager.getPageURL("SEASON_HEADER", null);
    public static final String EXCEL_GENERATOR_SUPPORT_PLUGIN = PageManager.getPageURL("EXCEL_GENERATOR_SUPPORT_PLUGIN", null);
    public static final String DEFAULT_ROWS_PER_PAGE = "30";

    static {
        try {
            instance = wt.util.WTProperties.getLocalProperties().getProperty ("wt.federation.ie.VMName");
            systemName = wt.util.WTProperties.getLocalProperties().getProperty ("wt.rmi.server.hostname");
        } catch(Exception e){
            e.printStackTrace();
        }

    }
%>
<%
    String seasonName = null;
    String oid = request.getParameter("oid");


    tg.setSpaceBetweenGroups(false);
    tg.setDrawTotalLabel(false);

    String userId = request.getParameter("userId");

    LCSSeason season = seasonModel.getBusinessObject();
    FlexType productType = season.getProductType();
    FlexType costSheetType = productType.getReferencedFlexType(ReferencedTypeKeys.COST_SHEET_TYPE);
    FlexType sourcingType = productType.getReferencedFlexType(ReferencedTypeKeys.SOURCING_CONFIG_TYPE);
     String seasonMasterId = "";

    seasonName = season.getName();
    seasonMasterId = FormatHelper.getNumericFromReference(season.getMasterReference());

    boolean runReport = (season != null && FormatHelper.parseBoolean( request.getParameter("runReport")));

    String rowsPerPage = "";
    int rowsToBreak = 0;
    if(FormatHelper.hasContent(request.getParameter("printMode")) && "true".equals(request.getParameter("printMode"))){
        if(FormatHelper.hasContent(request.getParameter("rowsPerPage"))){
            rowsPerPage = request.getParameter("rowsPerPage");
            rowsToBreak = (new Integer(rowsPerPage)).intValue();
        }

        if(rowsToBreak > 0){
            tg.setUsePageBreaks(true);
            tg.setPageBreakRows(rowsToBreak);
        }
    }
    else{
        tg.setUsePageBreaks(false);
    }

    Hashtable columnMap = new Hashtable();
    Collection columns = new Vector();

    Collection attList = new Vector();
    Collection skuData = null;
    Collection productData = null;

    Collection changeData = null;
    Collection changeColumns = new Vector();

    Collection filteredChangeData = new Vector();

    Collection filterList = new Vector();
    Hashtable attributeMap = new Hashtable();

    String action = request.getParameter("action");
    String business = "";
    String reportType = "";

    boolean includeRemoved = false;
    Hashtable groupColumnsKey = new Hashtable();





    ////////////////////////////////////////////////////////////////////////////
    // CREATE THE TABLE COLUMN DEFINITIONS
    ////////////////////////////////////////////////////////////////////////////

        ftg.setScope(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SCOPE);
        ftg.setLevel(FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL);
        ftg.createTableColumns(productType, columnMap, productType.getAllAttributes(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SCOPE, FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL, false), false, "PRODUCT.");

        ftg.setScope(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SCOPE);
        ftg.setLevel(FootwearApparelFlexTypeScopeDefinition.SKU_LEVEL);
        ftg.createTableColumns(productType, columnMap, productType.getAllAttributes(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SCOPE, FootwearApparelFlexTypeScopeDefinition.SKU_LEVEL, false), false, "PRODUCT.");

        ftg.setScope(FootwearApparelFlexTypeScopeDefinition.PRODUCTSEASON_SCOPE);
        ftg.setLevel(FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL);
        ftg.createTableColumns(productType, columnMap, productType.getAllAttributes(FootwearApparelFlexTypeScopeDefinition.PRODUCTSEASON_SCOPE, FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL, true), false, "PRODUCT.");

        ftg.setScope(FootwearApparelFlexTypeScopeDefinition.PRODUCTSEASON_SCOPE);
        ftg.setLevel(FootwearApparelFlexTypeScopeDefinition.SKU_LEVEL);
        ftg.createTableColumns(productType, columnMap, productType.getAllAttributes(FootwearApparelFlexTypeScopeDefinition.PRODUCTSEASON_SCOPE, FootwearApparelFlexTypeScopeDefinition.SKU_LEVEL, false), false, "PRODUCT.");

        ftg.setScope(null);
        ftg.setLevel(null);

        ftg.createTableColumns(sourcingType, columnMap, sourcingType.getAllAttributes(), false, "SOURCING.");


        ftg.setScope(CostSheetFlexTypeScopeDefinition.COST_SHEET_SCOPE);
        ftg.setLevel(CostSheetFlexTypeScopeDefinition.PRODUCT_LEVEL);
        ftg.createTableColumns(costSheetType, columnMap, costSheetType.getAllAttributes(CostSheetFlexTypeScopeDefinition.COST_SHEET_SCOPE, CostSheetFlexTypeScopeDefinition.PRODUCT_LEVEL, false), false, "COSTSHEET.");

        ftg.setScope(CostSheetFlexTypeScopeDefinition.COST_SHEET_SCOPE);
        ftg.setLevel(CostSheetFlexTypeScopeDefinition.SKU_LEVEL);
        ftg.createTableColumns(costSheetType, columnMap, costSheetType.getAllAttributes(CostSheetFlexTypeScopeDefinition.COST_SHEET_SCOPE, CostSheetFlexTypeScopeDefinition.SKU_LEVEL, false), false, "COSTSHEET.");

        // CREATES THE PRODUCT NAME COLUMN WHICH IS NOT MADE BY THE ABOVE CODE
        com.lcs.wc.client.web.TableColumn column = new com.lcs.wc.client.web.TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(productType.getAttribute("productName").getAttDisplay());
        column.setHeaderAlign("left");
        column.setLinkMethod("iterationHistory");
        column.setLinkTableIndex("LCSPRODUCT.BRANCHIDITERATIONINFO");
        column.setTableIndex(productType.getAttribute("productName").getSearchResultIndex());
        column.setLinkMethodPrefix("VR:com.lcs.wc.product.LCSProduct:");
        column.setWrapping(false);
        column.setShowGroupByHeader(false);
        column.setShowGroupSubTotal(false);
        columnMap.put("PRODUCT.productName", column);

        // CREATES THE SKU NAME COLUMN WHICH IS NOT MADE BY THE ABOVE CODE
        column = new com.lcs.wc.client.web.TableColumn();
        if("SKU".equals(request.getParameter("rptLevel"))){
            column.setDisplayed(true);
        }
        column.setHeaderLabel(productType.getAttribute("skuName").getAttDisplay());
        column.setHeaderAlign("left");
        column.setLinkMethod("iterationHistory");
        column.setLinkTableIndex("LCSSKU.BRANCHIDITERATIONINFO");
        column.setTableIndex(productType.getAttribute("skuName").getSearchResultIndex());
        column.setLinkMethodPrefix("VR:com.lcs.wc.product.LCSSKU:");
        column.setWrapping(false);
        columnMap.put("PRODUCT.skuName", column);


        ////////////////////////////////////////////////////////////////////////
        // COLUMNS SPECIFIC TO THE CHANGE REPORT
        ////////////////////////////////////////////////////////////////////////
        column = new com.lcs.wc.client.web.TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(timeOfChange);
        column.setHeaderAlign("left");
        column.setTableIndex("LCSSEASONALCHANGELOG.CREATESTAMPA2");
        column.setWrapping(false);
        column.setFormat(FormatHelper.GMT_TO_LOCAL_FORMAT);
        columnMap.put("CHANGELOG.CREATESTAMP", column);

        column = new com.lcs.wc.client.web.TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(newString);
        column.setTableIndex("LCSSEASONALCHANGELOG.NEWSTRINGVALUE");
        column.setWrapping(false);
        column.setFormat(FormatHelper.STRING_FORMAT);
        columnMap.put("CHANGELOG.NEWSTRINGVALUE", column);

        column = new com.lcs.wc.client.web.TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(oldString);
        column.setTableIndex("LCSSEASONALCHANGELOG.OLDSTRINGVALUE");
        column.setWrapping(false);
        column.setFormat(FormatHelper.STRING_FORMAT);
        columnMap.put("CHANGELOG.OLDSTRINGVALUE", column);

        column = new com.lcs.wc.client.web.TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(newNumeric);
        column.setTableIndex("LCSSEASONALCHANGELOG.NEWDOUBLEVALUE");
        column.setWrapping(false);
        column.setFormat(FormatHelper.DOUBLE_FORMAT);
        columnMap.put("CHANGELOG.NEWNUMERICVALUE", column);

        column = new com.lcs.wc.client.web.TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(oldNumeric);
        column.setTableIndex("LCSSEASONALCHANGELOG.OLDDOUBLEVALUE");
        column.setWrapping(false);
        column.setFormat(FormatHelper.DOUBLE_FORMAT);
        columnMap.put("CHANGELOG.OLDNUMERICVALUE", column);

        column = new com.lcs.wc.client.web.TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(newCurrency);
        column.setTableIndex("LCSSEASONALCHANGELOG.NEWDOUBLEVALUE");
        column.setWrapping(false);
        column.setFormat(FormatHelper.CURRENCY_FORMAT);
        columnMap.put("CHANGELOG.NEWCURRENCYVALUE", column);

        column = new com.lcs.wc.client.web.TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(oldCurrency);
        column.setTableIndex("LCSSEASONALCHANGELOG.OLDDOUBLEVALUE");
        column.setWrapping(false);
        column.setFormat(FormatHelper.CURRENCY_FORMAT);
        columnMap.put("CHANGELOG.OLDCURRENCYVALUE", column);


        column = new com.lcs.wc.client.web.TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(changedAttribute);
        column.setTableIndex("FLEXTYPEATTRIBUTE.ATTDISPLAY");
        column.setWrapping(false);
        column.setFormat(FormatHelper.STRING_FORMAT);
        columnMap.put("CHANGELOG.ATTRIBUTE", column);


        column = new com.lcs.wc.client.web.TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(userID);
        column.setTableIndex("WTUSER.NAME");
        column.setWrapping(false);
        column.setFormat(FormatHelper.STRING_FORMAT);
        columnMap.put("CHANGELOG.USER", column);

        column = new com.lcs.wc.client.web.TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(changedTo);
        column.setTableIndex("CHANGELOG.NEWVAL");
        column.setWrapping(false);
        column.setFormat(FormatHelper.STRING_FORMAT);
        columnMap.put("CHANGELOG.NEWVAL", column);

        column = new com.lcs.wc.client.web.TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(changedFrom);
        column.setTableIndex("CHANGELOG.OLDVAL");
        column.setWrapping(false);
        column.setFormat(FormatHelper.STRING_FORMAT);
        columnMap.put("CHANGELOG.OLDVAL", column);

        column = new com.lcs.wc.client.web.TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(lifecycleStatus);
        column.setHeaderAlign("left");
        column.setTableIndex("LCSPRODUCT.STATESTATE");
        column.setWrapping(false);
        columnMap.put("PRODUCT.LCSTATE", column);

        ////////////////////////////////////////////////////////////////////////
        // CREATES THE LIST OF COLUMNS WHICH WILL ACTUALLY
        // BE USED BUT THE REPORT
        ////////////////////////////////////////////////////////////////////////
        Collection columnList = new Vector();

        // THIS LIST NEEDS TO BE HOLD ALL VALUES WHICH MIGHT BE
        // DISPLAYED IN THE FINAL TABLE.. (SPECIAL TO THIS REPORT)
        columnList.add("PRODUCT.productName");


        // COLUMNS USED FOR CHANGE REPORT
        changeColumns.add(columnMap.get("PRODUCT.productName"));
        if("SKU".equals(request.getParameter("rptLevel"))){
            changeColumns.add(columnMap.get("PRODUCT.skuName"));
        }

        changeColumns.add(columnMap.get("CHANGELOG.CREATESTAMP"));
        changeColumns.add(columnMap.get("CHANGELOG.USER"));
        changeColumns.add(columnMap.get("CHANGELOG.ATTRIBUTE"));
        changeColumns.add(columnMap.get("CHANGELOG.OLDVAL"));
        changeColumns.add(columnMap.get("CHANGELOG.NEWVAL"));


        com.lcs.wc.client.web.TableColumn potentialColumn;
        Iterator it = columnList.iterator();
        String key = "";

        while(it.hasNext()){
            key = (String)it.next();
            potentialColumn = (com.lcs.wc.client.web.TableColumn) columnMap.get(key);
            if(potentialColumn != null){
                columns.add(potentialColumn);
                if(key.indexOf(".") > 0){
                    key = key.substring(key.indexOf(".") + 1, key.length());
                }
                attList.add(key);
            }
        }


        ////////////////////////////////////////////////////////////////////////
        // RUNS THE QUERY
        ////////////////////////////////////////////////////////////////////////

        if(runReport || "true".equals(request.getParameter("csv"))){
            
            Hashtable criteria = new Hashtable();
            criteria.putAll(RequestHelper.hashRequest(request));
            String rptLevel = FormatHelper.format(request.getParameter("rptLevel"));

            Injector injector = Guice.createInjector(new LSQModule());
            com.lcs.wc.season.query.LineSheetQuery lsqn = injector.getInstance(com.lcs.wc.season.query.LineSheetQuery.class);
            LineSheetQueryOptions options = new LineSheetQueryOptions();
            options.setSeason(season);
            options.criteria = criteria;
            options.setIncludeSourcing(true);
            options.secondarySourcing = true;
            options.costing = true;
            options.usedAttKeys = attList;

            if("SKU".equals(rptLevel)){
                options.skus = true;
                options.skuCosting = true;
            }

            Collection data = lsqn.getLineSheetResults(options);
            skuData = new Vector(data);
            productData = new Vector(data);

            Date startDate = FormatHelper.parseDate(request.getParameter("startDateString"));

            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            startDate = cal.getTime();

            Date endDate = FormatHelper.parseDate(request.getParameter("endDateString"));
            cal = Calendar.getInstance();
            cal.setTime(endDate);
            cal.set(Calendar.HOUR_OF_DAY, 24);
            endDate = cal.getTime();

            Collection attributeChangeList = null;
            if(FormatHelper.hasContent(request.getParameter("attChangeList"))){
                 attributeChangeList = MOAHelper.getMOACollection(request.getParameter("attChangeList"));
            }

            //RETRIEVE USER-SELECTED LEVEL TO DETERMINE IF PRODUCT, SKU, OR BOTH
            Vector rptLevels = new Vector();
            rptLevels.add(rptLevel);
            rptLevels.add("PRODUCT-SKU");
            rptLevels.add("");

            boolean returnProductsOnly = false;
    if (rptLevel.equalsIgnoreCase("PRODUCT")) {
                returnProductsOnly = true;
            }

            if (attributeChangeList!=null) { 
                changeData = new LCSSeasonChangeLogQuery().findChangeLogEntriesMultiLevel(startDate, endDate, attributeChangeList, userId, rptLevels, seasonMasterId, returnProductsOnly).getResults();
            }


            ////////////////////////////////////////////////////////////////////////
            // POST PROCESSING OF DATA
            ////////////////////////////////////////////////////////////////////////
            Map productHash = TableDataUtil.hashCollection(productData, "LCSPRODUCT.IDA3MASTERREFERENCE");
            Map placeholderHash = TableDataUtil.hashCollection(productData, "LCSPRODUCT.IDA3A12");
            Map skuHash = TableDataUtil.hashCollection(skuData, "LCSSKU.IDA3MASTERREFERENCE");
            Map costSheetHash = TableDataUtil.hashCollection(skuData, "LCSCOSTSHEET.IDA3MASTERREFERENCE");
  //vv
			System.out.println("-----LCSSeasonalChangeLog.CHANGED_OBJECT_REFERENCE ----"+LCSSeasonalChangeLog.CHANGED_OBJECT_REFERENCE );
			System.out.println("-----LCSSeasonalChangeLog.SEASON_MASTER_REFERENCE ----"+LCSSeasonalChangeLog.SEASON_MASTER_REFERENCE );
			System.out.println("-----LCSSeasonalChangeLog.SKU_MASTER_REFERENCE ----"+LCSSeasonalChangeLog.SKU_MASTER_REFERENCE );
			System.out.println("-----LCSSeasonalChangeLog.PRODUCT_MASTER_REFERENCE ----"+LCSSeasonalChangeLog.PRODUCT_MASTER_REFERENCE );
			System.out.println("-----LCSSeasonalChangeLog.COST_SHEET_MASTER_REFERENCE ----"+LCSSeasonalChangeLog.COST_SHEET_MASTER_REFERENCE );

            // LOOP THROUGH THE CHANGES AND LOCATE CORRECT ROW TO JOIN TO FROM
            // ROW HASHES... IF NO ROW IS FOUND, THEN THE ROW CHANGE IS
            // DISCARDED AS IT MUST NOT HAVE MET THE FILTER.

            if (changeData!=null && !changeData.isEmpty()) {
                Iterator changesIter = changeData.iterator();

        FlexObjectHelper foHelper = new FlexObjectHelper(LCSSeasonalChangeLog.class);
		
		
        while(changesIter.hasNext()){
            FlexObject dataRow = null;
            FlexObject changeRow = (FlexObject) changesIter.next();
            
            //Retrieve the persusted string attribute get the FlexType & FlexTypeAttribute and add data for the LFexTypeAttribute in the FlexObject.
            String persistedAttribute = changeRow.getString(foHelper.getFlexObjectKey(LCSSeasonalChangeLog.PERSISTED_ATTRIBUTE));
            AttributeDefinitionPersistedString adps = new AttributeDefinitionPersistedString(persistedAttribute);
            FlexType changeType = adps.getFlexType();
            FlexTypeAttribute changeAtt = adps.getFlexTypeAttribute();
            changeRow.setData("FLEXTYPEATTRIBUTE.ATTKEY", changeAtt.getAttKey());
            changeRow.setData("FLEXTYPEATTRIBUTE.ATTDISPLAY", changeAtt.getAttDisplay());
            changeRow.setData("FLEXTYPEATTRIBUTE.ATTOBJECTLEVEL", changeAtt.getAttObjectLevel());
            changeRow.setData("FLEXTYPEATTRIBUTE.ATTSCOPE", changeAtt.getAttScope());
          
            //filtering on the selected levels
            if(!rptLevels.contains(changeAtt.getAttObjectLevel())){
                continue;
            }
                    boolean passedFilter = false;

            String changeObjectClassName = foHelper.getFlexObjectKey(LCSSeasonalChangeLog.CHANGED_OBJECT_REFERENCE + "." + WTAttributeNameIfc.REF_CLASSNAME);
            
                    // PRODUCT CHANGE
		    //The LCSProductCostSheet will be stored to column LCSSeasonalChangeLog.classnamekeyH4 since the LCSCostSheet split to LCSProductCostSheet and LCSSKUCostSheet
            if(FormatHelper.format(changeRow.getString(changeObjectClassName)).indexOf("LCSProduct") > 0){
                String dataRowKey = foHelper.getFlexObjectKey(LCSSeasonalChangeLog.PRODUCT_MASTER_REFERENCE + "." + WTAttributeNameIfc.REF_OBJECT_ID);
                dataRow = (FlexObject)productHash.get(changeRow.getString(dataRowKey));
                    }
                    // SKU CHANGE
            if("SKU".equals(rptLevel)){
            	// clear PRODUCT level change if rptLevel is SKU
            	dataRow = null;
		    ////The LCSSKUCostSheet will be stored to column LCSSeasonalChangeLog.classnamekeyH4 since the LCSCostSheet split to LCSProductCostSheet and LCSSKUCostSheet
                if(FormatHelper.format(changeRow.getString(changeObjectClassName)).indexOf("LCSSKU") > 0){
                    String dataRowKey = foHelper.getFlexObjectKey(LCSSeasonalChangeLog.SKU_MASTER_REFERENCE + "." + WTAttributeNameIfc.REF_OBJECT_ID);
                    dataRow = (FlexObject)skuHash.get(changeRow.getString(dataRowKey));
                        }
                    }

                    // PRODCT SEASON CHANGE
            if(FormatHelper.format(changeRow.getString(changeObjectClassName)).indexOf("LCSSeasonProductLink") > 0){
                String dataRowKey = foHelper.getFlexObjectKey(LCSSeasonalChangeLog.SKU_MASTER_REFERENCE + "." + WTAttributeNameIfc.REF_OBJECT_ID);
                dataRow = (FlexObject)placeholderHash.get(changeRow.getString(dataRowKey));
                if(dataRow == null){
                    dataRow = (FlexObject)skuHash.get(changeRow.getString(dataRowKey));
                        }
                    }

                    // COST SHEET CHANGE
                    //TODO address with story about seasonal change log - suggestion changing to 'CostSheet'
            if(FormatHelper.format(changeRow.getString(changeObjectClassName)).indexOf("LCSCostSheet") > 0){
                String seasonMasterIdKey = foHelper.getFlexObjectKey(LCSSeasonalChangeLog.SEASON_MASTER_REFERENCE + "." + WTAttributeNameIfc.REF_OBJECT_ID);
                if(seasonMasterId.equals(changeRow.getString(seasonMasterIdKey))){
                    String dataRowKey = foHelper.getFlexObjectKey(LCSSeasonalChangeLog.COST_SHEET_MASTER_REFERENCE + "." + WTAttributeNameIfc.REF_OBJECT_ID);
                    dataRow = (FlexObject)costSheetHash.get(changeRow.getString(dataRowKey));
                        }
                    }

                    // SOURCING CONFIG CHANGE
            if(FormatHelper.format(changeRow.getString(changeObjectClassName)).indexOf("LCSSourcingConfig") > 0){
                        // will never be set for sourcing config 
                        //  if(seasonMasterId.equals(changeRow.getString("LCSSEASONALCHANGELOG.IDA3B4"))){
                String dataRowKey = foHelper.getFlexObjectKey(LCSSeasonalChangeLog.PRODUCT_MASTER_REFERENCE + "." + WTAttributeNameIfc.REF_OBJECT_ID);

                    dataRow = (FlexObject)productHash.get(changeRow.getString(dataRowKey));
                        //  }
                    }

            if(dataRow != null){
                filteredChangeData.add(TableDataUtil.joinObjects(changeRow, dataRow));
                passedFilter = true;
            }

                    if(passedFilter){
                        if(changeAtt.isStringDatatype()){
                           TableDataUtil.formatIntoColumn(changeRow, "LCSSEASONALCHANGELOG.NEWSTRINGVALUE", "CHANGELOG.NEWVAL", FormatHelper.STRING_FORMAT, changeAtt);
                           TableDataUtil.formatIntoColumn(changeRow, "LCSSEASONALCHANGELOG.OLDSTRINGVALUE", "CHANGELOG.OLDVAL", FormatHelper.STRING_FORMAT, changeAtt);
                        }
                        else if(changeAtt.isNumericDatatype()){
                            if(changeAtt.getAttVariableType().equals("currency")){
                                TableDataUtil.formatIntoColumn(changeRow, "LCSSEASONALCHANGELOG.NEWDOUBLEVALUE", "CHANGELOG.NEWVAL", FormatHelper.CURRENCY_FORMAT, changeAtt);
                                TableDataUtil.formatIntoColumn(changeRow, "LCSSEASONALCHANGELOG.OLDDOUBLEVALUE", "CHANGELOG.OLDVAL", FormatHelper.CURRENCY_FORMAT, changeAtt);
                            }
                            else if(changeAtt.getAttVariableType().equals("object_ref")|| changeAtt.getAttVariableType().equals("object_ref_list")){
                               TableDataUtil.formatIntoColumn(changeRow, "LCSSEASONALCHANGELOG.NEWSTRINGVALUE", "CHANGELOG.NEWVAL", FormatHelper.STRING_FORMAT);
                               TableDataUtil.formatIntoColumn(changeRow, "LCSSEASONALCHANGELOG.OLDSTRINGVALUE", "CHANGELOG.OLDVAL", FormatHelper.STRING_FORMAT);
                            }
                            else {
                                TableDataUtil.formatIntoColumn(changeRow, "LCSSEASONALCHANGELOG.NEWDOUBLEVALUE", "CHANGELOG.NEWVAL", FormatHelper.DOUBLE_FORMAT, changeAtt);
                                TableDataUtil.formatIntoColumn(changeRow, "LCSSEASONALCHANGELOG.OLDDOUBLEVALUE", "CHANGELOG.OLDVAL", FormatHelper.DOUBLE_FORMAT, changeAtt);
                            }
                        }
                        else if(changeAtt.isDateDatatype()){
                           TableDataUtil.formatIntoColumn(changeRow, "LCSSEASONALCHANGELOG.NEWDATE", "CHANGELOG.NEWVAL", FormatHelper.DATE_STRING_FORMAT);
                           TableDataUtil.formatIntoColumn(changeRow, "LCSSEASONALCHANGELOG.OLDDATE", "CHANGELOG.OLDVAL", FormatHelper.DATE_STRING_FORMAT);
                        }
                    }
                }
            }


            // SET CONDITIONAL DRAWING OF CELLS BASED ON
            // WETHER THE GIVEN SKU IS A PLACEHOLDER. THIS
            // REMOVES UNWANTED DATA FOR SKU LEVEL REPORTS WHERE
            // THE PRODUCT DOES NOT HAVE A REAL SKU
            column = (TableColumn) columnMap.get("PRODUCT.skuName");
            column.setShowCriteriaTarget("LCSSKU.PLACEHOLDER");
            column.setShowCriteria("0");
            column.setShowCriteriaNumericCompare(true);
        ////////////////////////////////////////////////////////////////////////
        // INDICATE WHICH COLUMNS SHOULD BE TOTALED
        ////////////////////////////////////////////////////////////////////////
        /////////////////////////////
        //Filters init
        /////////////////////////////

    }//end if run report

    attributeMap = FlexTypeUtil.getAttributeMap(productType.getAllAttributes(), "PRODUCT.");
    attributeMap.putAll(FlexTypeUtil.getAttributeMap(sourcingType.getAllAttributes(), "SOURCING."));
    attributeMap.putAll(FlexTypeUtil.getAttributeMap(costSheetType.getAllAttributes(), "COSTSHEET."));


%>

<%

        filterList.add("PRODUCT.productName");

 %>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JAVASCRIPT //////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<script>
    function runReport(){
        if(!validate()){
            return;
        }
        document.MAINFORM.activity.value = 'VIEW_SEASON_CHANGE_REPORT';
        document.MAINFORM.action.value = '<%=FormatHelper.encodeForJavascript(action)%>';
        document.MAINFORM.runReport.value = 'true'
        document.MAINFORM.oid.value = document.MAINFORM.reportSeasonOid.options[document.MAINFORM.reportSeasonOid.selectedIndex].value;
        submitForm();
    }

    function validate(){
        if(!valRequiredSingleSelect(document.MAINFORM.reportSeasonOid, "Season")){ return false; }
        if(!valRequiredStandardInput(document.MAINFORM.startDateString, "Start Date")){ return false; }
        if(!valRequiredStandardInput(document.MAINFORM.endDateString, "End Date")){ return false; }
		convertSpaceToZero(document.MAINFORM.rowsPerPage);
        if(!isValidIntegerRange(document.MAINFORM.rowsPerPage, "Rows Per Page", -1.0E9, 1.0E9, false, false)){ return false; }
        <% if(reportType.equals("Change")){ %>
            if(!valRequiredMOAInput(document.MAINFORM.attChangeListChosen, document.MAINFORM.attChangeListOptions, "Attributes")){ return false; }
        <% } %>
        return true;
    }

    function chooseSeason(seasonId){
        document.MAINFORM.activity.value = 'VIEW_SEASON_CHANGE_REPORT';
        document.MAINFORM.action.value = '<%=FormatHelper.encodeForJavascript(action)%>';
        document.MAINFORM.runReport.value = 'true'
        document.MAINFORM.oid.value = document.MAINFORM.reportSeasonOid.options[document.MAINFORM.reportSeasonOid.selectedIndex].value;
        // clear the AttChangeListChosen selector
        document.MAINFORM.attChangeList.value='';
        submitForm();
    }
    
    function changeAttOptions(clearChosen){


    }
</script>


<%
    String fontSize = FormatHelper.format(request.getParameter("reportFontSize"));
    if(!FormatHelper.hasContent(fontSize)){
        fontSize = "8pt";
    }
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////////////// REPORT CSS  //////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<style type="text/css">

    .RPT_TBL {
        font-family: Arial, Helvetica, sans-serif;
        text-decoration: normal;
        font-size: <%= FormatHelper.encodeForHTMLContent(fontSize) %>;
        font-weight: normal;
        font-style: normal;
        color: black;
        background-color: #FFFFFF;
        padding-left : 6;
        padding-right : 6;
        padding-top: 4;
        vertical-align: text-top;
    }

    .RPT_TBD {
        font-family: Arial, Helvetica, sans-serif;
        text-decoration: normal;
        font-size: <%= FormatHelper.encodeForHTMLContent(fontSize) %>;
        font-weight: normal;
        font-style: normal;
        color: black;
        background-color: #EEEEEE;
        padding-left : 6;
        padding-right : 6;
        padding-top: 4;
        vertical-align: text-top;

    }

    .RPT_BACKGROUND{
        vertical-align: top;
    }

    .RPT_HEADER {
        font-family: Arial, Helvetica, sans-serif;
        text-decoration: underline;
        font-size: <%= FormatHelper.encodeForHTMLContent(fontSize) %>;
        font-weight: bold;
        font-style: normal;
        color: black;
        background-color: #FFFFFF;
        padding-left : 4;
        padding-right : 4;
        vertical-align: text-bottom;
    }

    .RPT_GROUPSEPARATOR {
        font-size: <%= FormatHelper.encodeForHTMLContent(fontSize) %>;
    }

    .RPT_TOTALS {
        font-family: Arial, Helvetica, sans-serif;
        font-weight: bold;
        font-size: <%= FormatHelper.encodeForHTMLContent(fontSize) %>;
        padding-left : 6;
        padding-right : 6;
        padding-top: 4;

    }

</style>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////////// HTML ////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<input type="hidden" name="runReport" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("runReport")) %>">
<table width="100%" >


<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////////////// REPORT TABLE  ////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>


<%
    Collection groupBy = new Vector();
    if(runReport || "true".equals(request.getParameter("csv"))){

        String reportGroupBy = FormatHelper.format(request.getParameter("reportGroupBy"));

        groupBy = GroupBySortByHelper.getGroupBySortByColumns(reportGroupBy, columnMap, groupColumnsKey, GROUPBY_INPUT_DELIMITER);

        if(groupBy == null)groupBy = new Vector();

        Iterator gbit = groupBy.iterator();
        while(gbit.hasNext()){
            column = (com.lcs.wc.client.web.TableColumn)gbit.next();
            column.setShowGroupByHeader(false);
            column.setShowGroupSubTotal(false);
        }
    }
%>
<tr>
  <td>
    <jsp:include page="<%=subURLFolder+ SEASON_HEADER %>" flush="true" >
	                        		<jsp:param name="none" value="" />
						</jsp:include>
  </td>
</tr>
<tr>
        <td>
            <table cellspacing=0>
                <td class="PAGEHEADINGTITLE">
                    <%= FormatHelper.encodeAndFormatForHTMLContent(season.getValue("seasonName").toString()) %> : <%= changeReportPgHead %>:
                </td>
            </table>
        </td>
    </tr>
<% if(runReport){ %>

<tr>
    <td>
        <%
            ////////////////////////////////////////////////////////////////////////
            // OVERRIDE THE OOB TABLE CSS CLASSES
            ////////////////////////////////////////////////////////////////////////
            //tg.borderClass = "";
            //tg.cellClassLight = "RPT_TBL";
            //tg.cellClassDark = "RPT_TBD";
            //tg.tableBackgroundClass = "RPT_BACKGROUND";
            //tg.tableSubHeaderClass = "RPT_HEADER";
            //tg.groupSeparatorClass = "RPT_GROUPSEPARATOR";
            //tg.totalsClass = "RPT_TOTALS";
            //tg.subTotalsClass = "RPT_TOTALS";

            ////////////////////////////////////////////////////////////////////////
            // TELL THE TABLE GENERATOR TO SHOW TOTALS
            ////////////////////////////////////////////////////////////////////////
            tg.setShowTotals(false);
            tg.setShowSubTotals(false);
            tg.setShowDiscreteRows(false);

            ////////////////////////////////////////////////////////////////////////
            // HANDLE GROUP BY SETTINGS
            ////////////////////////////////////////////////////////////////////////

            //tg.setGroupByColumns(groupBy);

            out.print(tg.drawTable(filteredChangeData, changeColumns, null, false, true));

        %>
    </td>
</tr>
<% } %>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////////////// REPORT INPUTS ////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

<tr><td style="page-break-before: always"></td></tr>
<tr>
    <td>
        <table>
            <td class="HEADING1">
                <%= reportParametersPgHead %>&nbsp;
            </td>
            <td class="button">
                <a class="button" href="javascript:runReport()"><%= runLabel %></a>
            </td>
        </table>
    </td>
</tr>
<tr>
    <td>
       <table>
            <tr>
                <td  class="BORDERED_BLOCK">
                    <table border="0">
                    <col width="10%"><col width="40%">
                    <col width="10%"><col width="40%">
                        <tr>
                        <%
                            SearchResults seasons = new LCSSeasonQuery().findActiveSeasons();
                            FlexType seasonType = season.getFlexType();
                            String seasonNameColumn = seasonType.getAttribute("seasonName").getSearchResultIndex();
                            Hashtable table = LCSQuery.createTableList(seasons, "LCSSEASON.BRANCHIDITERATIONINFO", seasonNameColumn, "VR:com.lcs.wc.season.LCSSeason:");
                            Hashtable table2 = new Hashtable(table);

                            Set keys = table.keySet();
                            Iterator iter = keys.iterator();
                            String tablekey = null;
                            String entry = null;
                            while(iter.hasNext()){
                                 tablekey = (String)iter.next();
                                 entry = (String)table.get(tablekey);
                                 if(entry.indexOf(business) < 0 ){
                                    table2.remove(tablekey);
                                 }
                            }


                        %>
                        <%= fg.createDropDownListWidget(seasonLabel, table2, "reportSeasonOid", FormatHelper.getVersionId(season), "chooseSeason(this.value)", false, false) %>

                        </tr>

                        <tr>
                        <%  Hashtable levelHash = new Hashtable();
                                            levelHash.put("PRODUCT", "Product");
                                            levelHash.put("SKU", "Sku");
                        %>
                         <%= fg.createDropDownListWidget(changesExistingOn, levelHash, "rptLevel", request.getParameter("rptLevel"), "changeAttOptions(true)", false, false) %>
                        </tr>
                        <tr>
                            <%
                                String sDate = request.getParameter("startDateString");
                                String eDate = request.getParameter("endDateString");
                                if(!FormatHelper.hasContent(sDate)){
                                    java.util.Date date = new java.util.Date(System.currentTimeMillis());
                                    sDate = FormatHelper.applyFormat(date, FormatHelper.DATE_STRING_FORMAT);
                                }
                                if(!FormatHelper.hasContent(eDate)){
                                    java.util.Date date = new java.util.Date(System.currentTimeMillis());
                                    eDate = FormatHelper.applyFormat(date, FormatHelper.DATE_STRING_FORMAT);
                                }
                            %>
                            <%= fg.createDateInput("start", changedOnBeginDate, sDate, 10, 10, false) %>

                            <%= fg.createDateInput("end", changedOnEndDate, eDate, 10, 10, false) %>

                        </tr>
                        <%
					        Hashtable attChangeListTable = new Hashtable();						 
							LCSSeasonChangeLogLogic lcsSeasonChangeLogLogic = new LCSSeasonChangeLogLogic();
							
							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "productName", attChangeListTable);
							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "skuName", attChangeListTable);
								
							//VV
							System.out.println("-----product type--"+productType.getFullName(true));
							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "scProductDescription", attChangeListTable);

							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "scSizeRange", attChangeListTable);
							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "scProductDescriptionShort", attChangeListTable);



							
							//Product Season attributes
							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "scproductlifecycle", attChangeListTable);

							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "sccatmarketingline", attChangeListTable);

							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "scsalesline", attChangeListTable);

							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "scdevelopmentype", attChangeListTable);
							
							
							
							//Colorway attributes
							
							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "scNRFcolorcode", attChangeListTable);
							

							//Colorway Season attributes 

							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "sccustomssignoff", attChangeListTable);

							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "scBuyReadyDate", attChangeListTable);

							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "scearlybuyready", attChangeListTable);

							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "sccolorwaylifecycle", attChangeListTable);
							
							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "scRetailintrodate", attChangeListTable);
							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "sccreationtrack", attChangeListTable);

							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "scspecialusage", attChangeListTable);

							lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "scdevelopmentype", attChangeListTable);
							
							
							 	System.out.println("-----product tyupe-22-"+productType.getFullName(true));
							//vv
							
                            /* 
                            // Example of how to add an attribute that is in a subtype:
                            if (productType.getFullName(true).equals("Product\\com.ptc.PatSubtypeProduct")){
								lcsSeasonChangeLogLogic.addTrackingAttribute(productType, "A_string", attChangeListTable);
                            } 
                           */
                        %>
                        <tr>
                            <%= fg.createMultiChoice("attChangeList", attributesLabel, attChangeListTable, FormatHelper.format(request.getParameter("attChangeList")), false) %>
                        </tr>
                   </table>
              </td>
            </tr>

            <tr>
                <td class="BORDERED_BLOCK">
                    <table>
                        <%  Iterator list = filterList.iterator();
                            String thisKey = "";
                            FlexTypeAttribute filterAtt = null;

                            ftg.setScope(null);
                            ftg.setLevel(null);

                            while(list.hasNext()){
                                thisKey = (String) list.next();
                                filterAtt = (FlexTypeAttribute) attributeMap.get(thisKey);
                                if(filterAtt != null && filterAtt.isAttEnabled()){
                                    %>
                                    <tr>
                                    <%= ftg.createSearchCriteriaWidget(filterAtt, productType, request) %>
                                    </tr>
                                    <%
                                }
                            }
                        %>

                   </table>
              </td>
            </tr>
            <tr>
                <td class="BORDERED_BLOCK">
                    <table>
                        <%  Map groupByTable = GroupBySortByHelper.makeDropDownList(groupColumnsKey, GROUPBY_INPUT_DELIMITER); %>

                        <tr>
                         <!--   <%= fg.createDropDownListWidget("Group By/Sort By", groupByTable, "reportGroupBy", request.getParameter("reportGroupBy"), "", false, false) %> -->
                        </tr>
                        <tr>
                            <%  Hashtable fontTable = new Hashtable();
                                fontTable.put("8", "08pt");
                                fontTable.put("6", "06pt");
                                fontTable.put("4", "04pt");
                                fontTable.put("10", "10pt");
                            %>
                             <%= fg.createDropDownListWidget(fontSizeLabel, fontTable, "reportFontSize", request.getParameter("reportFontSize"), "", false) %>
                        </tr>
                       <tr>

                            <%

                            if(season == null){
                                rowsPerPage = DEFAULT_ROWS_PER_PAGE;
                            }%>

                            <%= fg.createIntegerInput("rowsPerPage", rowsPerPageLabel, rowsPerPage, 3, 0, false) %>

                            <%
                            String printM = request.getParameter("printMode");
                            if(season == null){
                                printM = "true";
                            }

                            %>
                            <%=fg.createStandardBooleanInput("printMode", runInPrintModeLabel, printM, false) %>



                        </tr>
                        <tr>
                            <%= fg.createDisplay(printerSetupUpRecommendation, landscapeLegalLabel, FormatHelper.STRING_FORMAT) %>
                        </tr>
                        <tr>
                            <td>
                                <table>
                                    <tr>
                                        <td class="button">
                                            <%
                                            request.setAttribute("columns", changeColumns);
                                            request.setAttribute("data", filteredChangeData);
                                            request.setAttribute("groupByColumns", groupBy);

                                            String csv = request.getParameter("csv");
                                            %>
   
                                           <jsp:include page="<%=subURLFolder+ EXCEL_GENERATOR_SUPPORT_PLUGIN %>" flush="true">
                                                <jsp:param name="csv" value="<%= csv %>" />
                                                <jsp:param name="reportDisplayName" value="<%= business + reportType %>" />
                                               <jsp:param name="reportName" value="<%= business + reportType %>"/>
                                                <jsp:param name="discreteRows" value="false"/>
                                                <jsp:param name="showTotals" value="false"/>
                                                <jsp:param name="showSubTotals" value="false"/>
                                             </jsp:include>

                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                   </table>
              </td>
            </tr>

        </table>
    </td>
</tr>

<tr>
    <td class="button">
        &nbsp;<a class="button" href="javascript:runReport()"><%= runLabel %></a><br><br>
    </td>
</tr>
</table>
