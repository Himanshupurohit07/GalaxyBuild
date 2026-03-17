
<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.db.SearchResults,
                com.lcs.wc.db.FlexObject,
                com.lcs.wc.client.*,
                com.lcs.wc.client.web.*,
                com.lcs.wc.util.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.supplier.*,
                com.lcs.wc.report.*,
                wt.indexsearch.*,
                wt.util.*,
                org.apache.logging.log4j.Logger,
                org.apache.logging.log4j.LogManager,
                java.util.*"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="flexg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="flexg2" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator2" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />


<%!
    public static final String JSPNAME = "FindSupplierResults";
	private static final Logger logger = LogManager.getLogger("rfa.jsp.supplier.FindSupplierResults");
	//Initializing the order of attributes 
	public static final String attributesOrder = LCSProperties.get("com.lcs.wc.vendor.attributes");
	//Initializing Vendor FlexType
	public static final String flex = LCSProperties.get("com.lcs.wc.vendor.flextype");

    public static final String getCellClass(boolean b){
       if(b){
            return "TABLEBODYDARK";
        } else {
            return "TABLEBODYLIGHT";
        }
    }
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%

String nameLabel = WTMessage.getLocalizedMessage(RB.QUERYDEFINITION, "supplierName", RB.objA);
nameLabel = nameLabel.substring(nameLabel.indexOf('\\') +1);
String allLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "all_LBL", RB.objA ) ;
String chooseLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "choose_LBL",RB.objA ) ;
String imageLabel =  WTMessage.getLocalizedMessage ( RB.MAIN, "image_LBL", RB.objA ) ;

String errorQuickSearchMessage = "";
boolean cqsError = false;
%>
<%
    FlexType flexType = null;
    String type = request.getParameter("type");
    if(FormatHelper.hasContent(type)){
      flexType = FlexTypeCache.getFlexType(type);
    }
    String types = "Supplier|" + type;

    String ajaxWindow = "false";

    if(FormatHelper.hasContent(request.getParameter("ajaxWindow"))) {
        ajaxWindow = FormatHelper.format(request.getParameter("ajaxWindow"));
    }
    String tableLayout = FormatHelper.format(request.getParameter("layout"));
%>

<%@ include file="../../../jsp/reports/ViewLookup.jspf" %>

<%
    boolean darkRow = false;

    String returnAction = request.getParameter("returnAction");
    boolean chooser = FormatHelper.parseBoolean(request.getParameter("chooser"));
    boolean multiple = FormatHelper.parseBoolean(request.getParameter("multiple"));
    boolean idOnly = FormatHelper.parseBoolean(request.getParameter("idOnly"));
    String objectIdType = FormatHelper.format(request.getParameter("objectIdType"));

    String  SEARCH_CLASS_DISPLAY = flexType.getFullNameDisplay(true);

    String idColumn = "LCSSUPPLIER.BRANCHIDITERATIONINFO";
    String bulkActivity = "FIND_SUPPLIER";
    String bulkAction = "SEARCH";

    FlexTypeAttribute nameAttr = FlexTypeCache.getFlexTypeRoot("Supplier").getAttribute("name");
    boolean updateMode = "true".equals(request.getParameter("updateMode"));
    //boolean updateMode = true;

    if(updateMode){
        FTSLHolder hldr = new FTSLHolder();
        ArrayList keys = new ArrayList();
        String nameCriteriaKey = nameAttr.getSearchCriteriaIndex();
        keys.add(nameCriteriaKey);

        hldr.add(type, null, null, keys);

        session.setAttribute("BU_FTSLHOLDER", hldr);
    }
%>
<%
    HashMap columnMap = new HashMap();

    flexg.setScope(null);
    flexg.setLevel(null);
    flexg.createTableColumns(flexType, columnMap, flexType.getAllAttributes(null, null, false), updateMode, false, "Supplier.", null, true, null);

   //Build potentially required "Hard attribute" columns
   columnMap.putAll(flexg.createHardColumns(flexType, "LCSSupplier", "Supplier"));


    TableColumn column = null;
    if(updateMode){

        UpdateFileTableColumn fcolumn = new UpdateFileTableColumn();
        fcolumn.setHeaderLabel(imageLabel);
        fcolumn.setDisplayed(true);
        fcolumn.setTableIndex("LCSSUPPLIER.PRIMARYIMAGEURL");
        fcolumn.setColumnWidth("10%");
        fcolumn.setClassname("LCSSUPPLIER");
        fcolumn.setWorkingIdIndex("LCSSUPPLIER.IDA2A2");
        fcolumn.setFormElementName("primaryImageURL");
        fcolumn.setFormatHTML(false);
        fcolumn.setColumnWidth("10%");
        fcolumn.setWrapping(true);
        fcolumn.setImage(true);
        columnMap.put("Supplier.primaryImageURL", fcolumn);
    }  else {
        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(imageLabel);
        column.setHeaderAlign("left");
        column.setLinkMethod("launchImageViewer");
        column.setLinkTableIndex("LCSSUPPLIER.PRIMARYIMAGEURL");
        column.setTableIndex("LCSSUPPLIER.PRIMARYIMAGEURL");
        column.setColumnWidth("1%");
        column.setLinkMethodPrefix("");
        column.setImage(true);
        column.setShowFullImage(FormatHelper.parseBoolean(request.getParameter("showThumbs")));
        column.setImageWidth(75);
        columnMap.put("Supplier.primaryImageURL", column);

    }

    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(nameLabel);
    column.setHeaderAlign("left");
    column.setLinkMethod("viewObject");
    column.setUseQuickInfo(true);
    column.setLinkTableIndex("LCSSUPPLIER.BRANCHIDITERATIONINFO");
    column.setTableIndex(nameAttr.getSearchResultIndex());
    column.setLinkMethodPrefix("VR:com.lcs.wc.supplier.LCSSupplier:");
    column.setHeaderLink("javascript:resort('" + nameAttr.getSearchResultIndex() + "')");
    column.setWrapping(nameAttr.isAttTableWrapable());
    column.setHeaderWrapping(nameAttr.isAttTableWrapable());
    column.setMinCharWidth(AbstractFlexTypeGenerator.getEffectiveMinCharWidth(nameAttr, column));
    columnMap.put("Supplier.name", column);

    //Build the collection of Columns to use in the report
    Collection columnList = new ArrayList();

    if(chooser){
        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel("");
        column.setHeaderAlign("left");

        column.setTableIndex("LCSSUPPLIERMASTER.NAME");

        String prefix = "VR:com.lcs.wc.supplier.LCSSupplier:";
        if("master".equals(objectIdType)){
            prefix = "OR:com.lcs.wc.supplier.LCSSupplierMaster:";
            idColumn = "LCSSUPPLIERMASTER.IDA2A2";
        }

        if(multiple){
            if(ajaxWindow.equals("true")){
                column.setHeaderLabel("<input type=\"checkbox\" id=\"selectAllChooserCheckBox\" value=\"false\" onClick=\"javascript:toggleAllChooserItems()\">" + allLabel);
            }else{
                column.setHeaderLabel(flexg2.drawSelectAllCheckbox(allLabel));
            }
            column.setFormat("UNFORMATTED_HTML");
            TableFormElement fe = new CheckBoxTableFormElement();
            fe.setValueIndex(idColumn);
            fe.setValuePrefix(prefix);
            if(ajaxWindow.equals("true")){
                fe.setName("selectedChooserIds");
            }else{
                fe.setName("selectedIds");
            }
            column.setFormElement(fe);
        } else {

            column.setLinkMethod("opener.choose");
            column.setLinkTableIndex(idColumn);
            if(!idOnly){
                column.setLinkMethodPrefix(prefix);
            }
            column.setLinkTableIndex2("LCSSUPPLIERMASTER.SUPPLIERNAME");
            column.setConstantDisplay(true);
            column.setWrapping(false);
            column.setConstantValue(chooseLabel);
        }

        column.setColumnWidth("1%");
        columnList.add(column);
    }

    if("thumbnails".equals(tableLayout) || "filmstrip".equals(tableLayout)) {
        DefaultTileCellRenderer cellRenderer = new DefaultTileCellRenderer();
        cellRenderer.setImageIndex("LCSSUPPLIER.PrimaryImageURL");
        cellRenderer.setIdIndex(idColumn);
        cellRenderer.setNameIndex(flexType.getAttribute("name").getSearchResultIndex());
        cellRenderer.setIdPrefix("VR:com.lcs.wc.supplier.LCSSupplier:");
        request.setAttribute("tileCellRenderer", cellRenderer);
    }

    //Setup the attlist/columns
    //attList is the list of attribute keys...required for the query to get the attributes
    Collection attList = new ArrayList();
    //columns is the list of Columns in the order they should be displayed from the column map
    Collection columns = new ArrayList(columnList);

    TableDataModel tdm = null;
    if(FormatHelper.hasContent(viewId)){
        tdm = new TableDataModel(columnMap, new ArrayList(), columns, viewId);
        attList = tdm.attList;
        //logger.debug(tdm.toString());
    }
    else{

        flexg.setScope(null);
        flexg.setLevel(null);
        flexg.setSingleLevel(false);
        Collection searchColumns = flexg.createSearchResultColumnKeys(flexType, "Supplier.");

        columns.add(columnMap.get("Supplier.primaryImageURL"));
        columns.add(columnMap.get("Supplier.name"));
        columns.add(columnMap.get("Supplier.creator"));
		
		//Code to arrange the attributes
		if(flex.equals (SEARCH_CLASS_DISPLAY) || flex.contains(SEARCH_CLASS_DISPLAY)){
		searchColumns = new ArrayList();
		if (FormatHelper.hasContent(attributesOrder)) {
				StringTokenizer groupsTokenizer = new StringTokenizer(attributesOrder, ",");
				while (groupsTokenizer.hasMoreTokens()) {
					String groupsName = (String) groupsTokenizer.nextToken();
					String concat = "Supplier."+groupsName;
					System.out.println("Concatenated string:"+concat);
					searchColumns.add(concat);
				}
		}
		}
		System.out.println("After :"+searchColumns);
        flexg.extractColumns(searchColumns, columnMap, attList, columns);
    }
%>
<%

    // BEGIN INDEX SEARCH - Set Up and Run the query via this plugin
    Collection oidList = null;
    String searchterm = null;
    if (FormatHelper.hasContent(request.getParameter("indexSearchKeyword"))) {
        searchterm = (request.getParameter("indexSearchKeyword")).trim();
    }
    String typeclass = null;
    if (FormatHelper.hasContent(request.getParameter("typeClass"))) {
        typeclass =request.getParameter("typeClass");
    }
%>
    <%@ include file="../../../jsp/indexsearch/IndexSearchLibraryPlugin.jspf" %>
<%
    // oidList should be populated now if user specified a keyword to search against, otherwise size=0
    // END INDEX SEARCH filter


    //Run the FlexPLM query
    Map criteria = RequestHelper.hashRequest(request);
    LCSSupplierQuery query = new LCSSupplierQuery();
    SearchResults results = SearchResults.emptySearchResults();
    if(null != tdm && null != tdm.groupByColumns){
        criteria.put("groupByColumns", tdm.getGroupByProperties());
    }
    try{
        if ( FormatHelper.hasContent(searchterm)) {
            if (oidList != null) {
                results = query.findSuppliersByCriteria(criteria, flexType, attList, filter, oidList);
            } // no else - no findby criteria needed if a keyword search was done and no hits occured
        } else {
            results = query.findSuppliersByCriteria(criteria, flexType, attList, filter, oidList);
        }
    }catch(LCSException ex){
        cqsError =true;
        errorQuickSearchMessage = ex.getLocalizedMessage();
    }
    if(cqsError){%>
        <table>
            <tr>
                <td class="ERROR">
                    <%= errorQuickSearchMessage %>
                </td>
            </tr>
        </table>
    <%}
    boolean RTS = FormatHelper.parseBoolean(request.getParameter("RETURN_TO_SEARCH_FROM"));
    if(results.getResultsFound() == 1 && !chooser && !RTS){
        // IF ONLY ONE PRODUCT IS FOUND, TAKE THE USER TO THE DETAILS PAGE
        FlexObject data = (FlexObject) results.getResults().elementAt(0);

        %>
        <script>
            viewObject('VR:com.lcs.wc.supplier.LCSSupplier:<%= data.getString("LCSSUPPLIER.BRANCHIDITERATIONINFO") %>');
        </script>
        <%
    }
    //Iterator suppliers = results.getResults().iterator();
    //FlexObject supplier = null;

 %>
<%@ include file="../../../jsp/main/SearchResultsTable.jspf" %>
