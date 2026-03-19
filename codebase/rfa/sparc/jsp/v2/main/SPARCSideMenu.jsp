<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- ////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ///////////////////////////////////////--%>
<%-- ////////////////////////////////////////////////////////////////////////////////////--%>
<%@page import="wt.inf.container.WTContainerHelper"%>
<%@ page language="java"
    errorPage="../../exception/ErrorReport.jsp"
    import=" com.lcs.wc.util.*,
            com.lcs.wc.util.OSHelper,
            com.lcs.wc.util.aclvalidator.*,
            com.lcs.wc.client.Activities,
            com.lcs.wc.client.web.*,
            com.lcs.wc.flextype.*,
            com.lcs.wc.material.*,
            com.lcs.wc.season.*,
            com.lcs.wc.product.*,
            com.lcs.wc.calendar.*,
            com.lcs.wc.foundation.*,
            com.lcs.wc.document.*,
            com.lcs.wc.db.*,
            com.lcs.wc.sizing.*,
            com.ptc.netmarkets.util.beans.NmURLFactoryBean,
            com.ptc.netmarkets.util.misc.NmAction,
            com.ptc.netmarkets.model.NmOid,
            java.io.*,
            java.util.*,
            java.util.Map.*,
            wt.util.*,
            wt.httpgw.URLFactory,
        com.ptc.netmarkets.util.misc.NetmarketURL,
            wt.enterprise.*,
            wt.fc.*,
            wt.org.WTGroup,
            com.ptc.netmarkets.roleAccess.*,
            wt.facade.netmarkets.RoleHelper"
%>
<%-- ////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS //////////////////////////////--%>
<%-- ////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="appContext" class="com.lcs.wc.client.ApplicationContext" scope="session"/>
<jsp:useBean id="wtcontext" class="wt.httpgw.WTContextBean" scope="request"/>
<jsp:setProperty name="wtcontext" property="request" value="<%=request%>"/>


<%
response.setDateHeader("Expires", -1);
response.setHeader("Pragma", "No-cache");
response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

lcsContext.initContext();
lcsContext.setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));

response.setDateHeader("Expires",
   System.currentTimeMillis(  ) + 24*60*60*1000);
%>
<%-- ////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE /////////////////////////////--%>
<%-- ////////////////////////////////////////////////////////////////////////////////////--%>
<%!

    public static String getUrl = "";


    public static String WindchillContext = "/Windchill";

    static
    {
        try
        {
            WTProperties wtproperties = WTProperties.getLocalProperties();
            getUrl = wtproperties.getProperty("wt.server.codebase","");
            WindchillContext = "/" + wtproperties.getProperty("wt.webapp.name");

        }
        catch(Throwable throwable)
        {
            throw new ExceptionInInitializerError(throwable);
        }
    }
    public static final boolean BASELINE_INSTALLED = LCSProperties.getBoolean("com.lcs.baseline.Enabled");
    public static final boolean ERROR_TESTING = LCSProperties.getBoolean("jsp.exception.ErrorTesting.enabled");
    public static final boolean REMOTE_TC_MS_MANAGEMENT = LCSProperties.getBoolean("jsp.tcrestart-msrestart.enabled");

    public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");
    public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
    public static final String WT_IMAGE_LOCATION = LCSProperties.get("flexPLM.windchill.ImageLocation");
    public static final String STANDARD_TEMPLATE_HEADER = PageManager.getPageURL("STANDARD_TEMPLATE_HEADER", null);
    public static final String STANDARD_TEMPLATE_FOOTER = PageManager.getPageURL("STANDARD_TEMPLATE_FOOTER", null);

    public static final String JSPNAME = "SideMenu";
    public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");
    public static final boolean LOAD_SIDEBAR_ONLY_ON_PRODUCTLINK = LCSProperties.getBoolean("jsp.main.SideMenu.loadSidebarOnlyOnProductLink");

    public static final String PRODUCT_SKU_PAGE = "PRODUCT";
    public static final String CHANGE_ACTIVITIES_PAGE = "CHANGES";
    public static final String PROCESSES_PAGE = "PROCESSES";
    public static final String SOURCING_PAGE = "SOURCING";
    public static final String MEASUREMENTS_PAGE = "MEASUREMENTS";
    public static final String SYSTEM_PAGE = "SYSTEM";
    public static final String HISTORY_PAGE = "HISTORY";
    public static final String DOCUMENTS_PAGE = "DOCUMENTS";
    public static final String CONSTRUCTION_PAGE = "CONSTRUCTION";
    public static final String SAMPLES_PAGE = "SAMPLES";
    public static final String TESTING_PAGE = "TESTING";
    public static final String IMAGES_PAGE = "IMAGES";
    public static final String LABOR_PAGE = "LABOR";
    public static final String BOM_PAGE = "BOM";
    public static final String SPEC_SUMMARY_PAGE = "SPEC_SUMMARY";

    public static final String OVERVIEW_TAB = "1";
    public static final String COLOR_TAB = "3";
    public static final String IMAGES_TAB = "IAMGES";
    public static final String SOURCING_TAB = "7";
    public static final String MATERIALS_TAB = "BOM";
    public static final String LABOR_TAB = "LABOR";
    public static final String WHEREUSED_TAB = "WHEREUSED";
    public static final String PROCESSES_TAB = "6";
    public static final String DOCUMENTS_TAB = "5";
    public static final String SYSTEM_TAB = "4";
    public static final String PRICING = "PRICING";

    public static final String adminGroup = LCSProperties.get("jsp.main.administratorsGroup", "Administrators");
    public static final String typeAdminGroup = LCSProperties.get("jsp.flextype.adminGroup", "Type Administrators");
    public static final String userAdminGroup = LCSProperties.get("jsp.users.adminGroup","User Administrators");
    public static final String calendarAdminGroup = LCSProperties.get("jsp.calendar.adminGroup","Calendar Administrators");
    public static final String processAdminGroup = LCSProperties.get("jsp.process.adminGroup","Process Administrators");
    public static final String sizingDefinitionAdminGroup = LCSProperties.get("jsp.sizingDefinition.adminGroup","SizingDefinition Administrators");

    public static final boolean USE_BOM = LCSProperties.getBoolean("com.lcs.wc.bom.useBOM");
    public static final boolean USE_BOL = LCSProperties.getBoolean("com.lcs.wc.bom.useBOL");
    public static final boolean USE_CHANGEACTIVITIES = LCSProperties.getBoolean("com.lcs.wc.change.useChangeActivities");
    public static final boolean USE_MEASUREMENTS = LCSProperties.getBoolean("com.lcs.wc.measurements.useMeasurements");
    public static final boolean USE_CONSTRUCTION = LCSProperties.getBoolean("com.lcs.wc.construction.useConstruction");
    public static final boolean USE_SAMPLES = LCSProperties.getBoolean("com.lcs.wc.sample.useSamples");
    public static final boolean USE_TESTING = LCSProperties.getBoolean("com.lcs.wc.testing.useTesting");
    public static final boolean USE_SILHOUETTES = LCSProperties.getBoolean("com.lcs.wc.season.useSilhouettes");
    public static final boolean USE_INSPIRATIONS = LCSProperties.getBoolean("com.lcs.wc.season.useInspirations");
    public static final boolean ENABLE_RFQ = LCSProperties.getBoolean("com.lcs.wc.sourcing.useRFQ");

    public static final boolean USE_PRODUCT = LCSProperties.getBoolean("jsp.vendorportal.VendorPortalSideMenu.useProduct");
    public static final boolean USE_SAMPLE = LCSProperties.getBoolean("jsp.vendorportal.VendorPortalSideMenu.useSample");
    public static final boolean USE_MATERIAL = LCSProperties.getBoolean("jsp.vendorportal.VendorPortalSideMenu.useMaterial");
    public static final boolean USE_ORDERCONF = LCSProperties.getBoolean("jsp.vendorportal.VendorPortalSideMenu.orderConfirmation");
    public static final boolean USE_RFQ = LCSProperties.getBoolean("jsp.vendorportal.VendorPortalSideMenu.useRFQ");
    public static final boolean USE_ORDERCONFIRMATION = LCSProperties.getBoolean("com.lcs.wc.product.useOrderConfirmation");
    public static final boolean FORUMS_ENABLED= LCSProperties.getBoolean("jsp.discussionforum.discussionforum.enabled");
    public static final String SIDE_MENU_NAVIGATION_SET_PIN = LCSProperties.get("navigator.Pin","false");
    public static final String SIDE_MENU_NAVIGATION_CLICK = LCSProperties.get("sidenavigator.Click","true");
    public static final String SIDE_MENU_NAVIGATION_HOVER_DELAY = LCSProperties.get("sidenavigator.hoverDelay","250");
    public static final String SIDE_NAVIGATION_RESIZE_DISABLED_BROWSER_SPECIFIC = LCSProperties.get("sidenavigator.resize.disabledOnChromeAndSafar","true");


    public static final boolean USE_FLEXCOLLECTION = LCSProperties.getBoolean("jsp.util.SideMenu.enableFlexCollectionLibraries");
    public static final boolean CALENDAR_ENABLED = LCSProperties.getBoolean("jsp.calendar.calendar.enabled");

    public static final String TWX_DASHBOARD_OPTION = PageManager.getPageURL("TWX_DASHBOARD_OPTION", null);

    public static final String SIDE_WORKLIST_MENU = PageManager.getPageURL("SIDE_WORKLIST_MENU", null);
    public static final String WORKITEMS_LISTING = PageManager.getPageURL("WORKITEMS_LISTING", null);
	
	public static final String REPORTS_MASTER_GROUP = LCSProperties.get("jsp.main.reportsMasterGroup", "SPARC MASTER GROUPS");


    public static final String getClass(boolean active, String test, String value){

        if(active && FormatHelper.hasContent(value) && value.equals(test)){
            return "class='navigatorOptionSelected'";

        }
        return "";
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

%>

<%    response.setContentType("text/html; charset=" +defaultCharsetEncoding);%>
<jsp:include page="<%= subURLFolder + STANDARD_TEMPLATE_HEADER %>" flush="true">
    <jsp:param name="bodyClass" value="xnavigatorBody disable-scroll" />
</jsp:include>

<%

String siteTab_lbl = WTMessage.getLocalizedMessage ( RB.MAIN, "siteTab_LBL", RB.objA ) ;
String productTab_lbl = WTMessage.getLocalizedMessage ( RB.MAIN, "productTab_LBL", RB.objA ) ;
String materialTab_lbl = WTMessage.getLocalizedMessage ( RB.MATERIAL, "material_LBL", RB.objA );
String yourWorkListPgTle = WTMessage.getLocalizedMessage ( RB.WF, "yourWorkList_PG_TLE", RB.objA ) ;
String actions_lbl = WTMessage.getLocalizedMessage( RB.MAIN, "actions2_LBL", RB.objA ) ;
String clearUserGroupCacheLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "clearUserGroupCache_LBL_OPT", RB.objA ) ;
String skuLabel = WTMessage.getLocalizedMessage ( RB.SKU, "SKU_LBL", RB.objA ) ;
String materialSupplierLabel = WTMessage.getLocalizedMessage ( RB.MATERIALSUPPLIER, "materialSupplier_LBL", RB.objA ) ;
String pLMNavigatorLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "pLMNavigator_LBL", RB.objA ) ;
String myHomeButton  = WTMessage.getLocalizedMessage ( RB.MAIN, "myHome_Btn", RB.objA ) ;
String myWorkButton = WTMessage.getLocalizedMessage ( RB.MAIN, "myWork_Btn", RB.objA ) ;
String clipboardLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "clipboard_LBL", RB.objA ) ;
String mySeasonsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "mySeasons_LBL", RB.objA ) ;
String detailsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "details_LBL", RB.objA ) ;
String inspirationsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "inspirations_LBL", RB.objA ) ;
String paletteLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "palette_LBL", RB.objA ) ;
String conceptsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "concepts_LBL", RB.objA ) ;
String silhouettesLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "silhouettes_LBL", RB.objA ) ;
String developmentLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "development_LBL", RB.objA ) ;
String lineLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "line_LBL", RB.objA ) ;
String calendarLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "calendar_LBL", RB.objA ) ;
String dashboardsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "dashboards_LBL", RB.objA ) ;
String samplesConsoleLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "samplesConsole_LBL", RB.objA ) ;
String specificationLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "specification_LBL", RB.objA ) ;
String specificationConfig = WTMessage.getLocalizedMessage ( RB.MAIN, "specificationConfig_LBL", RB.objA ) ;
String imagesLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "images_LBL", RB.objA ) ;
String measurementsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "measurements_LBL", RB.objA ) ;
String materialsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "materials_LBL", RB.objA ) ;
String constructionLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "construction_LBL", RB.objA ) ;
String discussionLibrary_LBL = WTMessage.getLocalizedMessage ( RB.MAIN, "discussionLibrary_LBL", RB.objA ) ;
String documentsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "documents_LBL", RB.objA ) ;
String laborLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "labor_LBL", RB.objA ) ;
String specSummaryLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "specificationConfig_LBL", RB.objA ) ;
String processesLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "processes_LBL", RB.objA ) ;
String historyLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "history_LBL", RB.objA ) ;
String recentLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "recent_LBL", RB.objA ) ;
String overviewLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "overview_LBL", RB.objA ) ;
String librariesLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "libraries_LBL", RB.objA ) ;
String administrativeLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "administrative_LBL", RB.objA ) ;

String manageTypesLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "manageTypes_LBL", RB.objA ) ;
String manageExportsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "manageExports_LBL", RB.objA ) ;

String manageTypesAndEnumerationsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "manageTypesAndEnumerations_LBL", RB.objA ) ;

String manageCalendarsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "manageCalendars_LBL", RB.objA ) ;
String loadFileLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "loadFile_LBL", RB.objA ) ;

//String loadTranslationFileLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "loadTypeManagerTranslations_MENU", RB.objA ) ;

String sourcingLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "sourcing_LBL", RB.objA ) ;
String samplesLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "samples_LBL", RB.objA ) ;
String approvalsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "approvals_LBL", RB.objA ) ;
String changesLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "changes_LBL", RB.objA ) ;
String systemLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "system_LBL", RB.objA ) ;
String reportsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "reports_LBL", RB.objA ) ;
String listAttributesOpt = WTMessage.getLocalizedMessage ( RB.MAIN, "listAttributes_OPT", RB.objA ) ;
String manageProcessesLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "manageProcesses_OPT", RB.objA ) ;
String userAccessLogStatsOpt = WTMessage.getLocalizedMessage ( RB.MAIN, "userAccessLogStats_OPT", RB.objA ) ;

String customReports = "VIEW CATALYST REPORTS";

String manageUsersLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "manageUsers_OPT", RB.objA ) ;
String manageLocksLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "manageLocks_OPT", RB.objA ) ;
String manageAttributeValueListsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "manageAttributeValueLists_OPT", RB.objA ) ;
String workitemsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "workitems_LBL", RB.objA ) ;
String myCheckedOutItemsPgHead = WTMessage.getLocalizedMessage ( RB.MAIN, "myCheckedOutItems_PG_HEAD", RB.objA ) ;
String myWorkListItemsPgHead= WTMessage.getLocalizedMessage ( RB.MAIN, "myWorkListItems_PG_HEAD", RB.objA ) ;
//String myWorkListItemsPgHead="My Worklist";
String presentationBoardLabel = WTMessage.getLocalizedMessage ( RB.CONTEXTBARS, "presentationBoard_LBL", RB.objA ) ;
String loadWorkListPageTitle = WTMessage.getLocalizedMessage ( RB.WF, "loadWorkList_PG_TLE", RB.objA ) ;
String clearFlexTypeCacheLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "clearFlexTypeCache_LBL_OPT", RB.objA ) ;
String favoritesLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "favorites_LBL", RB.objA ) ;
String removeLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "remove_Btn", RB.objA ) ;
String navigatorLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "navigator_LBL", RB.objA ) ;
String worklistExceptionsOpt = WTMessage.getLocalizedMessage ( RB.MAIN, "worklistExceptions_OPT", RB.objA ) ;
String allCcalendarsOpt = WTMessage.getLocalizedMessage ( RB.MAIN, "allCalendars_OPT", RB.objA ) ;
String planningLabel = WTMessage.getLocalizedMessage ( RB.PLAN, "planning_LBL", RB.objA ) ;
//String productLbl = WTMessage.getLocalizedMessage ( RB.MAIN, "productOption_LBL",RB.objA ) ;
//String materialLbl = WTMessage.getLocalizedMessage ( RB.MAIN, "materialOption_LBL",RB.objA ) ;
//String documentLbl = WTMessage.getLocalizedMessage ( RB.MAIN, "documentOption_LBL",RB.objA ) ;
String myInfoLabel = WTMessage.getLocalizedMessage ( RB.VENDORPORTAL, "myInfo_LBL", RB.objA ) ;
String myInfoMultiLabel = WTMessage.getLocalizedMessage ( RB.VENDORPORTAL, "myInfoMulti_LBL", RB.objA ) ;
String rfqLabel = WTMessage.getLocalizedMessage ( RB.VENDORPORTAL, "rfq_LBL", RB.objA ) ;
String sampleLabel = WTMessage.getLocalizedMessage ( RB.VENDORPORTAL, "sample_LBL", RB.objA ) ;

String reports = WTMessage.getLocalizedMessage ( RB.MAIN, "reports_LBL", RB.objA ) ;
String productCount = WTMessage.getLocalizedMessage ( RB.MAIN, "productCount_LBL", RB.objA ) ;
String userAssignments = WTMessage.getLocalizedMessage ( RB.MAIN, "userAssignments_LBL", RB.objA ) ;
String materialUsage = WTMessage.getLocalizedMessage ( RB.MAIN, "materialUsage_LBL", RB.objA ) ;
String committments = WTMessage.getLocalizedMessage ( RB.MAIN, "committments_LBL", RB.objA ) ;
String seasonCosts = WTMessage.getLocalizedMessage ( RB.MAIN, "seasonCosts_LBL", RB.objA ) ;
String sampleTracking = WTMessage.getLocalizedMessage ( RB.MAIN, "sampleTracking_LBL", RB.objA ) ;
String labDipTracking = WTMessage.getLocalizedMessage ( RB.MAIN, "labDipTracking_LBL", RB.objA ) ;

String siteLevelLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "siteLevel_LBL", RB.objA ) ;
String orgLevelLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "orgLevel_LBL", RB.objA ) ;
String wcHomePage = WTMessage.getLocalizedMessage ( RB.MAIN, "wcHomePage_LBL", RB.objA ) ;
String toggleSideBarButton = WTMessage.getLocalizedMessage (RB.MAIN, "toggleSideBar_LBL", RB.objA ) ;
String pinNavigatorButton = WTMessage.getLocalizedMessage (RB.MAIN, "pinNavigator_LBL", RB.objA ) ;
String unpinNavigatorButton = WTMessage.getLocalizedMessage (RB.MAIN, "unpinNavigator_LBL", RB.objA ) ;
String managerUserGroupProfiles = WTMessage.getLocalizedMessage ( RB.MAIN, "manageUserGroupProfiles_LBL", RB.objA ) ;

String errorReport_forceSystemErrorLink = WTMessage.getLocalizedMessage ( RB.ERRORREPORT, "errorReport_forceSystemErrorLink", RB.objA ) ;

String FlexType_MAIN = "com.lcs.wc.resource.FlexTypeRB";
Object[] objA = new Object[0];

String exportAllFlexTypesOpt = WTMessage.getLocalizedMessage ( FlexType_MAIN, "exportAllFlexTypes_OPT", objA ) ;
String exportUsersandGroupsOpt = WTMessage.getLocalizedMessage ( FlexType_MAIN, "exportUsersandGroups_OPT", objA ) ;
String extractTeamsOpt = WTMessage.getLocalizedMessage ( FlexType_MAIN, "extractTeams_OPT", objA ) ;
String extractACLsOpt = WTMessage.getLocalizedMessage ( FlexType_MAIN, "extractACLs_OPT", objA ) ;
String exportCalendarsOpt = WTMessage.getLocalizedMessage ( FlexType_MAIN, "exportCalendars_OPT", objA ) ;
String extractAllFlitersViewsOpt = WTMessage.getLocalizedMessage (FlexType_MAIN, "extractAllFlitersViews_OPT", objA ) ;

String areYouSureExportAllTypesFromTypeManagerCnfrm = WTMessage.getLocalizedMessage (FlexType_MAIN, "areYouSureExportAllTypesFromTypeManager_CNFRM", objA ) ;
String areYouSureExportAllFlexTypesInstanceDataCnfrm = WTMessage.getLocalizedMessage (FlexType_MAIN, "areYouSureExportAllFlexTypesInstanceData_CNFRM", objA ) ;
String areYouSureExportAllFiltersViewsCnfrm = WTMessage.getLocalizedMessage (FlexType_MAIN, "areYouSureExportAllFiltersViews_CNFRM", objA ) ;
String areYouSureExportAllUserGroupsCnfrm = WTMessage.getLocalizedMessage (FlexType_MAIN, "areYouSureExportAllUserGroups_CNFRM", objA ) ;
String areYouSureExportAllCalendarCnfrm = WTMessage.getLocalizedMessage (FlexType_MAIN, "areYouSureExportAllCalendar_CNFRM", objA ) ;
String areYouSureExportAllTeamsCnfrm = WTMessage.getLocalizedMessage (FlexType_MAIN, "areYouSureExportAllTeams_CNFRM", objA ) ;
String areYouSureExportAllACLsCnfrm = WTMessage.getLocalizedMessage (FlexType_MAIN, "areYouSureExportAllACLs_CNFRM", objA ) ;
String areYouSureExportAllLifeCyclesCnfrm = WTMessage.getLocalizedMessage (FlexType_MAIN, "areYouSureExportAllLifeCycles_CNFRM", objA ) ;
String switchToLabel = WTMessage.getLocalizedMessage (RB.MAIN, "switch_to_LBL", objA );

String hostUrl = getUrl + "/..";


// START: VENDOR PORTAL PROFILE SETUP SECTION
String myProfileString = "";

  if(lcsContext.isVendor){
        myProfileString = (String)session.getAttribute("MY_PROFILE");
        if(!FormatHelper.hasContent(myProfileString)){
            myProfileString = "";
        }
        if(!FormatHelper.hasContent(myProfileString)){
            Collection vObjects = lcsContext.getVendorObjects();
            Iterator i = vObjects.iterator();
            FlexTyped tObj = null;
            while(i.hasNext()){
                tObj = (FlexTyped)i.next();
                String name = tObj.getFlexType().getRootType().getLocalizedDisplay(FlexType.TYPE_DISPLAY_NAME);
                name = name + ":" + tObj.getValue("name");
                String id = "";
                if(tObj instanceof wt.enterprise.RevisionControlled){
                    id = FormatHelper.getVersionId((wt.enterprise.RevisionControlled)tObj);
                }
                else{
                    id = FormatHelper.getObjectId((WTObject)tObj);
                }
                myProfileString += "<a href=\"javascript:viewVendorObject('" + id + "')\">&nbsp;" +  FormatHelper.encodeForHTMLContent(name) + "</a>";
                if(i.hasNext())myProfileString += "</br>";
            }
            session.setAttribute("MY_PROFILE", myProfileString);
        }
    }
// END: VENDOR PORTAL PROFILE SETUP SECTION


    appContext.loadSeasonList(true);

    String activity = request.getParameter("activity");
    String action = request.getParameter("action");
    String oid = request.getParameter("oid");
    String type = request.getParameter("type");
    String tabPage = request.getParameter("tabPage");
    String templateType = request.getParameter("templateType");
   
// START:  generate the Type and Attribute Mgmt URL
wt.inf.container.WTContainerRef containerRef = wt.inf.container.WTContainerHelper.service.getExchangeRef();
String siteOid = "OR:"+containerRef.toString();


wt.org.WTUser user = (wt.org.WTUser)wt.session.SessionHelper.getPrincipal();


// TODO: move to top and populate authorization vars.

// Default condiftion to false (fail closed)
boolean manageEnumerationEnabled = false;
manageEnumerationEnabled = lcsContext.isManageEnumerationEnabled(user);


String contextOid = siteOid;
NmURLFactoryBean urlFactoryBeanTM = new NmURLFactoryBean();
HashMap objectOid=new HashMap();
objectOid.put("containerOid",contextOid);
String tmURL = urlFactoryBeanTM.getFactory().getURL("/netmarkets/jsp/administration/shell.jsp",objectOid)+"&shellAdmin=Admin&helpCenter=wt.fhc.url#netmarkets/jsp/administration/typemgr.jsp";
// DONE: generate the Type and Attribute Mgmt URL

%>
<script>
// Launch script for WC Type and Attribute Management link
// Must be launched in new tab for TM UI to interact with frames properly.
//Default Side Bar State for product and non-product pages
function viewTM() {
   flexWindowOpenAsPost("<%=tmURL%>", "wcTM");
}

function chooseSuppliers(supplierTypeId, material){
    document.MAINFORM.materialId.value = material;
    launchModuleChooser('SUPPLIER','' ,'' ,'', true, 'addSuppliers', false, 'version', supplierTypeId,'');
}

function addSuppliers(ids){
    document.MAINFORM.newSupplierIds.value = ids;
    closeChooser();
    document.MAINFORM.action.value = 'INIT';
    document.MAINFORM.activity.value = 'ADD_MATERIAL_SUPPLIERS';
    document.MAINFORM.returnOid.value = document.MAINFORM.materialId.value;
    document.MAINFORM.returnActivity.value =  'VIEW_MATERIAL';
    document.MAINFORM.returnAction.value =  'INIT';
    submitForm();
}

</script>

<script type="text/javascript" src="<%= URL_CONTEXT %>/javascript/seasonProductPage.js"></script>
<script type="text/javascript" src="<%= URL_CONTEXT %>/javascript/v2/rfa-dragndrop.js"></script>
<input type="hidden" name="rootTypeId" value="">
<input type="hidden" name="linePlanLevel" value="">
<input type="hidden" name="contextSKUId" value="">
<input type="hidden" name="globalChangeTrackingSinceDate" value="">
<input type="hidden" name="newSupplierIds" value="">
<input type="hidden" name="materialId" value="">

<div id="allSideNav" style="height: 100%;">
  <div id='sideBarNavigator' class="sideBarNavigator">
    <div class='sideBarNavButtons'>
        <div class="x-tool x-tool-expand-east x-tool-expand-east-over collapseExpandAndPin" id="collapseExpandButton" onclick="toggleSideBar(event)" title='<%= FormatHelper.formatHTMLString(toggleSideBarButton) %>'>&nbsp;</div>
        <div class="x-tool x-tool-pin collapseExpandAndPin" id="pinbutton" onclick="setPin(event)" title="<%= pinNavigatorButton %>">&nbsp;</div>
    </div>
    <!-- side navigation functionality for mouse over or click -->
    <% if(Boolean.parseBoolean(SIDE_MENU_NAVIGATION_CLICK)){ %>
    <div onclick="toggleSideBar(event)" style="height: calc(100% - 70px);"></div>
    <% } else { %>
    <div onmouseenter="delaySideBarOnMouseOver()" onmouseleave="stopDelaySideBarOnMouseOut()" style="height: calc(100% - 70px);"></div>
    <% } %>
  </div>

<div id="tabnav" class="navBar">
    <div id='left'></div>
    <% if ("POPFRAME".equals(templateType)) { %> 
    <div id='siteNavLink' class="navTabHidden" onclick="javascript:changeNavigatorTab('site')"><%= siteTab_lbl %></div>
    <% } else { %>
    <div id='siteNavLink' class="navTabSelected" onclick="javascript:changeNavigatorTab('site')"><%= siteTab_lbl %></div>
    <% } %>

    <div id='productNavLink' class="navTabHidden" onclick="javascript:changeNavigatorTab('product')"><%= productTab_lbl %></div>
    <div id='materialNavLink' class="navTabHidden" onclick="javascript:changeNavigatorTab('material')"><%= materialTab_lbl %></div>
    <div id='left'></div>
</div>

<div id='siteNavigator' class="siteNavigator" onscroll='repositionActionMenu()'>
<!-- My Work -->
<div class='card'>
  <input type="checkbox" id="siteMyWork" class="cardCheckbox">
  <div class='card-header'>
    <label class='card-label card-collapsible' for="siteMyWork" title="<%= myWorkButton %>">
      <%= myWorkButton %>
    </label>
  </div>
  <div class='card-content' id='myWorkContent'>
   <a href="javascript:showMyWorkListItems();">
      <img id='myWorkListItemsContentIcon' valign="bottom" src='<%= WT_IMAGE_LOCATION %>/checkout.png' border='0'>
      <%=myWorkListItemsPgHead%>
    </a>
    <br>
    <a href="javascript:showCheckedOutItems();">
      <img id='myCheckedOutItemsContentIcon' valign="bottom" src='<%= WT_IMAGE_LOCATION %>/checkout.png' border='0'>
      <%=myCheckedOutItemsPgHead%>
    </a>
    <br>
    <div id="workListDiv"></div>
  </div>
</div>
<!-- End My Work -->

<!-- Vendor Profile -->
<%if(lcsContext.isVendor && FormatHelper.hasContent(myProfileString)){%>

<div class='card'>
  <input type="checkbox" id="siteVendorProfile" class="cardCheckbox">
  <div class='card-header'>
    <label class='card-label card-collapsible' for="siteVendorProfile" title="<%= myInfoLabel %>">
      <%= myInfoLabel %>
    </label>
  </div>
  <div class='card-content' id='vendorProfileContent'>
    <%= myProfileString%>
  </div>
</div>
<%}%>
<!-- End Vendor Profile -->

<!-- Favorites -->
<div class='card'>
  <input type="checkbox" id="siteFavorites" class="cardCheckbox">
  <div class='card-header'>
    <label class='card-label card-collapsible' for="siteFavorites" title="<%= favoritesLabel %>">
      <%= favoritesLabel %>
    </label>
  </div>
  <div class='card-content' id='favoritesContent'>
    <div id="favoritesListDiv" style='overflow:hidden'></div>
  </div>
</div>
<!-- End Favorites -->


<%
    String id = "";
    String name;
    String seasonVersionId;
    boolean activeSeason;


 %>
<!-- My Seasons -->
<div class='card'>
  <input type="checkbox" id="siteSeasons" class="cardCheckbox">
  <div class='card-header'>
    <label class='card-label card-collapsible' for="siteSeasons" title="<%= mySeasonsLabel %>">
      <%= mySeasonsLabel %>
    </label>
  </div>
  <div class='card-content' id='seasonsContent'>
  <%
      Map seasonMap = appContext.getSeasonMap();
      Iterator seasonIter = seasonMap.keySet().iterator();

      Collection seasons = new Vector();
      while(seasonIter.hasNext()) {
          FlexObject season = new FlexObject();
          season.put("oid",seasonIter.next());
          season.put("name",seasonMap.get(season.get("oid")));
          seasons.add(season);
      }
      seasonIter = SortHelper.sortFlexObjects(seasons,"name").iterator();

      %>
      <select name="seasonSelectList" id="seasonSelectList">
      <%
      while(seasonIter.hasNext()){
          FlexObject season = (FlexObject) seasonIter.next();
          id = "" + season.get("oid");
          name = "" + season.get("name");
          name = FormatHelper.encodeAndFormatForHTMLContent(name);
          %><option value="<%= id %>"><a href="javascript:void(0)"><%= name %></a><%
      }
      %>
      </select>
      <br>
      <div style="padding-bottom:10px;"></div>
      <a id="ac_seasonActions" href="javascript:getActiveSeasonActions()" class="actions_link"><%= actions_lbl %></a>
      <div class='card'>
        <input type="checkbox" id="seasonConcept" class="cardCheckbox" checked>
        <div class='card-header'>
          <label class='card-label card-collapsible' for="seasonConcept" title="<%= conceptsLabel %>">
            <%= conceptsLabel %>
          </label>
        </div>
        <div class='card-content' id='seasonConceptContent'>
          <a href="javascript:viewSelectedSeasonDetails()">
            <img src="<%=WT_IMAGE_LOCATION%>/flexdetails.png" alt="">
            <%= detailsLabel %>
          </a><br>
          <%if(USE_INSPIRATIONS){%>
            <a href="javascript:viewSelectedInspirations()">
              <img src="<%=WT_IMAGE_LOCATION%>/inspiration_s.png" alt="">
              <%= inspirationsLabel %>
            </a><br>
          <%}%>
          <a href="javascript:viewSelectedPalette()">
            <img src="<%=WT_IMAGE_LOCATION%>/palette_s_3.png" alt="">
            <%= paletteLabel %>
          </a><br>
          <%if(USE_SILHOUETTES){ %>
            <a href="javascript:viewSelectedSilhouettes()">
              <img src="<%=WT_IMAGE_LOCATION%>/silhouette_s.png" alt="">
              <%= silhouettesLabel %>
            </a><br>
          <% } %>
        </div>
      </div>

      <div class='card'>
        <input type="checkbox" id="seasonDevelopment" class="cardCheckbox" checked>
        <div class='card-header'>
          <label class='card-label card-collapsible' for="seasonDevelopment" title="<%= developmentLabel %>">
            <%= developmentLabel %>
          </label>
        </div>
        <div class='card-content' id='seasonDevelopmentContent'>
          <% if (!lcsContext.isVendor) { %>
            <a href="javascript:viewSelectedPlanning()">
              <img src="<%=WT_IMAGE_LOCATION%>/projectPlan.gif" alt="">
              <%= planningLabel %>
            </a><br>
          <% } %>
            <a href="javascript:viewSelectedLineSheet()">
              <img src="<%=WT_IMAGE_LOCATION%>/lineplan_s.png" alt="">
              <%= lineLabel %>
            </a><br>
          <% if (ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeFromPath("Season Group\\Line Board"))) { %>
            <a href="javascript:viewSelectedLineBoard()">
              <img src="<%=WT_IMAGE_LOCATION%>/lineboard_s.png" alt="">
              <%= presentationBoardLabel %>
            </a><br>
          <% } %>
          <% if (CALENDAR_ENABLED && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Calendar"))) { %>
            <a href="javascript:viewSelectedCalendar()">
              <img src="<%=WT_IMAGE_LOCATION%>/calendar2.png" alt="">
              <%= calendarLabel %>
            </a><br>
          <% } %>
          <a href="javascript:viewSelectedDashboard()">
            <img src="<%=WT_IMAGE_LOCATION%>/chart_pie.png" alt="">
            <%= dashboardsLabel %>
          </a><br>
          <% if( USE_SAMPLES && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Sample")) && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeFromPath("Sample\\Product")) &&   ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Product"))) { %>
            <a href="javascript:viewSelectedSamplesConsole()">
              <img src="<%=WT_IMAGE_LOCATION%>/samples_console.png" alt="">
              <%= samplesConsoleLabel %>
            </a><br>
          <% } %>
          </div>
        </div>

      <% if (!lcsContext.isVendor) { %>
        <div class='card'>
          <input type="checkbox" id="seasonReports" class="cardCheckbox">
          <div class='card-header'>
            <label class='card-label card-collapsible' for="seasonReports" title="<%= reportsLabel %>">
              <%= reportsLabel %>
            </label>
          </div>
          <div class='card-content' id='seasonReportsContent'>
            <% if (BASELINE_INSTALLED) { %>
              <a <%= getClass(true, "VIEW_PRODUCT_COUNT_REPORT", activity) %> href="javascript:viewReport('VIEW_PRODUCT_COUNT_REPORT')">
                <%= productCount%>
              </a><br>
              <a <%= getClass(true, "VIEW_USER_ASSIGNMENT_REPORT", activity) %> href="javascript:viewReport('VIEW_USER_ASSIGNMENT_REPORT')">
                <%= userAssignments%>
              </a><br>
              <a <%= getClass(true, "VIEW_MATERIAL_USAGE_REPORT", activity) %> href="javascript:viewReport('VIEW_MATERIAL_USAGE_REPORT')">
                <%= materialUsage%>
              </a><br>
              <a <%= getClass(true, "VIEW_MATERIAL_COMMITMENT_REPORT", activity) %> href="javascript:viewReport('VIEW_MATERIAL_COMMITMENT_REPORT')">
                <%= committments%>
              </a><br>
              <a <%= getClass(true, "VIEW_SEASON_COST_REPORT", activity) %> href="javascript:viewReport('VIEW_SEASON_COST_REPORT')">
                <%= seasonCosts%>
              </a><br>
              <a <%= getClass(true, "VIEW_SAMPLE_TRACKING_REPORT", activity) %> href="javascript:viewReport('VIEW_SAMPLE_TRACKING_REPORT')">
                <%= sampleTracking%>
              </a><br>
              <a <%= getClass(true, "VIEW_MATCOL_TRACKING_REPORT", activity) %> href="javascript:viewReport('VIEW_MATCOL_TRACKING_REPORT')">
                <%= labDipTracking%>
              </a><br>
              <!--- end of Baseline Report Additions -->
            <% } %>
            <% if (ThingworxDashboardHelper.hasActiveDashboardsForReports()) { %>
                  <jsp:include page="<%= subURLFolder + TWX_DASHBOARD_OPTION %>" flush="true">
                        <jsp:param name="ajaxMenu" value="true" />
                        <jsp:param name="fromSeasonReports" value="true" />
                  </jsp:include>
            <% } %>
          </div>
        </div>
      <% } %>
  </div>
</div>
<!-- End My Seasons -->



<!-- Libraries -->
<div class='card'>
<%if(lcsContext.isVendor){%>
  <input type="checkbox" id="siteLibraries" class="cardCheckbox" checked>
<% }else{%>
  <input type="checkbox" id="siteLibraries" class="cardCheckbox">
<%}%>
  <div class='card-header'>
    <label class='card-label card-collapsible' for="siteLibraries" title="<%= librariesLabel %>">
      <%= librariesLabel %>
    </label>
  </div>
  <div class='card-content' id='librariesContent'>
  <%
  Hashtable scripts = new Hashtable();

    // KEY is expected to be the ROOT type of the flex type tree
    if(!lcsContext.isVendor){
        if(ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeFromPath("BOM\\Materials"))){
            scripts.put("BOM","bomSearch()");
        }
        scripts.put("Color","findColor()");
        scripts.put("Business Object","findCBO()");
        scripts.put("Change Activity","findChangeActivity()");
        scripts.put("Country","findCountry()");
        scripts.put("Construction","manageConstruction()");
        scripts.put("Document","findDocument()");
        scripts.put("Document Collection","findDocumentCollection()");
        scripts.put("Effectivity Context","findEffectivityContext()");
        scripts.put("Image","findImage()");
        scripts.put("Last","findLast()");
        scripts.put("Log Entry","findLogEntry()");
        scripts.put("Material","findMaterial()");
        scripts.put("Measurements","manageMeasurements()");
        scripts.put("Media","findMedia()");
        scripts.put("Multi-Object","findMultiObject()");
        if (USE_ORDERCONFIRMATION){
            scripts.put("Order Confirmation","findOrderConfirmation()");
        }
        scripts.put("Palette","findPalette()");

        scripts.put("Plan","findPlan()");
        scripts.put("Placeholder","findPlaceholder()");
        scripts.put("Product","findProduct()");
        scripts.put("Size Definition","manageSizingDefinition()");
        if (ENABLE_RFQ){
            scripts.put("RFQ","findRFQ()");
        }
        scripts.put("Sample","findSample()");
        scripts.put("Season","findSeason()");
        scripts.put("SKU","findSKU()");
        scripts.put("Supplier","findSupplier()");
        scripts.put("Test Specification","manageTesting()");
        scripts.put("Calendar","manageCalendars()");
        scripts.put("Retail Component","findFlexComponent()");

    } else {
      scripts.put("Color","findColor()");
      scripts.put("Document","findDocument()");

      boolean hasSupplierLinks = false;

      for(Iterator vendorObjsItr = lcsContext.getVendorObjects().iterator();vendorObjsItr.hasNext(); ){
        if(hasSupplierLinks){
                break;
        }else if(vendorObjsItr.next().toString().indexOf("LCSSupplier:") > -1){
                hasSupplierLinks = true;
        }
      }
      if(USE_MATERIAL &&  ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Material")) && hasSupplierLinks) {
        scripts.put("Material","findMaterial()");
      }


      if(USE_ORDERCONFIRMATION && USE_ORDERCONF &&  ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Order Confirmation")) && hasSupplierLinks) {
        scripts.put("Order Confirmation","findOrderConfirmation()");
      }


      if(USE_PRODUCT && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Palette")) && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Season")) && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Product"))){
        scripts.put("Palette","findPalette()");
      }

      if(USE_PRODUCT &&  ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Product")) && hasSupplierLinks) {
        scripts.put("SKU","findSKU()");

        scripts.put("Product","findProduct()");
      }

      if(ENABLE_RFQ && USE_RFQ && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("RFQ")) && hasSupplierLinks) {
        scripts.put("RFQ","findRFQ()");
      }

      if(USE_SAMPLE && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Sample"))) {
        scripts.put("Sample","findSample()");
      }

      if(ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Season")) ){
        scripts.put("Season","findSeason()");
      }
      if(ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.component.FlexComponent")) ){
          scripts.put("Retail Component","findFlexComponent()");
      }
    }

    Object[] it = scripts.keySet().toArray();
    Arrays.sort(it);

    FlexType t;
    for (int i = 0; i < it.length; i++) {
      name = (String)it[i];
      if (name.equals("SKU") || name.equals("Placeholder")) {
        t = FlexTypeCache.getFlexTypeRoot("Product");
      } else if (name.equals("Material Supplier")) {
        t = FlexTypeCache.getFlexTypeRoot("Material");
      } else if (name.equals("Retail Component")) {
        t = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.component.FlexComponent");
      } else {
        t = FlexTypeCache.getFlexTypeRoot(name);
      }
      if (t != null && ACLHelper.hasViewAccess(t)) {
        String localizedName = "";
        if (name.equals("SKU")) {
          localizedName = skuLabel;
        } else if (name.equals("Material Supplier")) {
          localizedName = materialSupplierLabel;
        } else if (name.equals("Placeholder")) {
          localizedName = WTMessage.getLocalizedMessage ( RB.PLACEHOLDER, "placeholder_LBL", RB.objA ) ;
        } else {
          localizedName = t.getTypeDisplayName();
        }
        String script = "" + scripts.get(name);
        scripts.remove(name);
        scripts.put(localizedName, script);
      } else {
          scripts.remove(name);
      }
    }

    Iterator revisableEntityChildren = FlexTypeCache.getFlexTypeFromPath("Revisable Entity").getChildren().iterator();
    while (revisableEntityChildren.hasNext()) {
      t = (FlexType)revisableEntityChildren.next();
      if (ACLHelper.hasViewAccess(t) && !lcsContext.isVendor) {
        scripts.put(t.getTypeDisplayName(), "findRBO('" + FormatHelper.getObjectId(t) + "')");
      }
    }

    if(USE_FLEXCOLLECTION){
      Iterator flexCollectionChildren = FlexTypeCache.getFlexTypeFromPath("Collection").getChildren().iterator();
      while(flexCollectionChildren.hasNext()){
        t = (FlexType)flexCollectionChildren.next();
        if(ACLHelper.hasViewAccess(t)){
          scripts.put(t.getTypeDisplayName(), "findFlexCollection('" + FormatHelper.getObjectId(t) + "')");
        }
      }
    }

    if(FORUMS_ENABLED){
      String libOid = new wt.fc.ReferenceFactory().getReferenceString(com.lcs.wc.util.FlexContainerHelper.getFlexContainer());
      String parameters = "components$loadWizardStep$" + libOid + "$|forum$discuss$" + libOid + "&oid=" + libOid;
      HashMap urlParam = new HashMap();
      urlParam.put("oid", libOid);
      String discussionLink = FormatHelper.convertToShellURL(NetmarketURL.buildURL(new NmURLFactoryBean(), "FlexPLM/project", "view_forum", null, urlParam, true, new NmAction()));
      scripts.put(discussionLibrary_LBL,"createDialogWindow('"+ discussionLink +"', 'Discussions', '900', '600', '1')");
    }

    Iterator keys = SortHelper.sortStrings(scripts.keySet(), lcsContext.getLocale()).iterator();
    while(keys.hasNext()){
      name = (String) keys.next();
  %>
      <a href="javascript:<%= scripts.get(name) %>"><%= FormatHelper.encodeForHTMLContent(name) %></a><br>
  <%
    }
  %>
  </div>
</div>
<!-- End Libraries -->


<!-- Reports -->
<%if(!lcsContext.isVendor){%>
<div class='card'>
  <input type="checkbox" id="siteLibraries" class="cardCheckbox">
  <input type="checkbox" id="siteReports" class="cardCheckbox">

  <div class='card-header'>
    <label class='card-label card-collapsible' for="siteReports" title="<%= reportsLabel %>">
      <%= reportsLabel %>
    </label>
  </div>
  <div class='card-content' id='reportsContent'>

        <a href="javascript:viewExceptionReport()" <%= getClass(true, "WORKLIST_EXCEPTION_REPORT", activity)%>><%= worklistExceptionsOpt %></a><br>
        <% if(adminGroup.equals("ALL") || lcsContext.inGroup(adminGroup.toUpperCase() )){ %>
            <a href="javascript:viewAllAttributes()"><%= listAttributesOpt %></a><br>
            <a href="javascript:viewLCSAccessLogStats()"><%= userAccessLogStatsOpt %></a><br>
        <% } %>
		<% if(lcsContext.inGroup(REPORTS_MASTER_GROUP.toUpperCase())){%>
		<a href="javascript:viewAllSparcReports()"><%= customReports %></a><br>
		<% } %>
  </div>
</div>
<%}%>
<!-- End Reports -->

<!-- Administrative -->
<%
if(!lcsContext.isVendor &&
      (
        adminGroup.equals("ALL") ||
        lcsContext.inGroup(adminGroup.toUpperCase()) ||
        lcsContext.inGroup(typeAdminGroup.toUpperCase()) ||
        lcsContext.inGroup(userAdminGroup.toUpperCase()) ||
		lcsContext.inGroup("PRODUCT_SKU_SEASON_LOAD_GROUP") ||
        lcsContext.inGroup(processAdminGroup.toUpperCase()) ||
        manageEnumerationEnabled
       )
) {
%>

<div class='card'>
  <input type="checkbox" id="siteAdministrative" class="cardCheckbox">
  <div class='card-header'>
    <label class='card-label card-collapsible' for="siteAdministrative" title="<%= administrativeLabel %>">
      <%= administrativeLabel %>
    </label>
  </div>
  <div class='card-content' id='administrativeContent'>
    <%
    /*** Get the classic container reference to be used for some clients ***/

    String orgContainerRef = com.lcs.wc.util.FlexContainerHelper.getFlexContainer().getParentRef().toString();
    String siteContainerRef = (wt.inf.container.WTContainerHelper.getExchangeRef()).toString();

    String WCShellContext = WindchillContext + "/" + NetmarketURL.convertToShellURL(NetmarketURL.getMVCURL(""));

    NmURLFactoryBean urlFactoryBean = new NmURLFactoryBean();
    HashMap urlParam = new HashMap();
    urlParam.put("oid", java.net.URLEncoder.encode("OR:" +orgContainerRef));

    String orgUserGroupLink  = FormatHelper.convertToShellURL(NetmarketURL.buildURL(urlFactoryBean, "FlexPLM/administration", "participantAdministration", null, urlParam, true, new NmAction()));

    urlParam.clear();
    urlParam.put("oid", java.net.URLEncoder.encode("OR:" +siteContainerRef));
    String siteUserGroupLink  = FormatHelper.convertToShellURL(NetmarketURL.buildURL(urlFactoryBean, "FlexPLM/administration", "participantAdministration", null, urlParam, true, new NmAction()));

    String wcHomePageLink  = WCShellContext + "homepage";

    String wcProfilesLinks  = NetmarketURL.buildURL(urlFactoryBean, "FlexPLM/org", "listProfiles", NmOid.newNmOid("OR:" +orgContainerRef));

    if(adminGroup.equals("ALL") || lcsContext.inGroup(adminGroup.toUpperCase()) || lcsContext.inGroup("PRODUCT_SKU_SEASON_LOAD_GROUP")){ %>
	
	<%if(!lcsContext.inGroup("PRODUCT_SKU_SEASON_LOAD_GROUP")){%>

        <%if(ERROR_TESTING){%>
            <a href="javascript:throwErrorPage()"><%= errorReport_forceSystemErrorLink %></a><br>
        <%}%>
        <a href="javascript:viewTM()"><%= manageTypesAndEnumerationsLabel %></a><br>

    <div class='card'>
      <input type="checkbox" id="manageExports" class="cardCheckbox">
      <div class='card-header'>
        <label class='card-label card-collapsible' for="manageExports" title="<%= manageExportsLabel %>">
          <%= manageExportsLabel %>
        </label>
      </div>
      <div class='card-content' id='manageExportsContent'>
        <a href="javascript:go('exportCalendars');"><%= exportCalendarsOpt %></a><br>
        <a href="javascript:go('exportAllFiltersViews');"><%= extractAllFlitersViewsOpt %></a><br>
        <a href="javascript:go('extractTeams');"><%= extractTeamsOpt %></a><br>
        <a href="javascript:go('exportAllUserAndGroups');"><%= exportUsersandGroupsOpt %></a><br>
      </div>
    </div>
    <a href="javascript:manageMultiObjectLocks()"><%= manageLocksLabel%></a><br>
    <a href="javascript:processAdmin()"><%= manageProcessesLabel %></a><br>
    <div class='card'>
      <input type="checkbox" id="manageUsers" class="cardCheckbox">
      <div class='card-header'>
        <label class='card-label card-collapsible' for="manageUsers" title="<%= manageUsersLabel %>">
          <%= manageUsersLabel %>
        </label>
      </div>
      <div class='card-content' id='manageUsersContent'>
      <% if (lcsContext.inGroup("ADMINISTRATORS") || (lcsContext.getUser().getOrganization() != null && lcsContext.getUser().getOrganization().getAdministrator() != null && lcsContext.inGroup(lcsContext.getUser().getOrganization().getAdministrator().getName().toUpperCase()))) { %>
        <a href="javascript:manageUsers('<%=orgUserGroupLink%>')">
          <img width='16' align='middle' src='<%=WT_IMAGE_LOCATION%>/organization_context.gif' alt='' border='0' align='bottom'>
          <%=FormatHelper.formatJavascriptString(orgLevelLabel)%>
        </a><br>
      <% } %>
      <% if (lcsContext.inGroup("ADMINISTRATORS")) { %>
        <a href="javascript:manageUsers('<%=siteUserGroupLink%>')">
          <img width='16' align='middle' src='<%=WT_IMAGE_LOCATION%>/user.gif' alt='' border='0' align='bottom'>
          <%=FormatHelper.formatJavascriptString(siteLevelLabel)%>
        </a><br>
      <% } %>
      </div>
    </div>
    <a href="javascript:createToolbarWindow('<%=wcProfilesLinks%>','Profiles')"><%= managerUserGroupProfiles %></a><br>
	<%}%>
    <a href="javascript:loadFile()"><%= loadFileLabel %></a><br>
	<%if(!lcsContext.inGroup("PRODUCT_SKU_SEASON_LOAD_GROUP")){%>
		<a href="javascript:clearUserGroupCache()"><%= clearUserGroupCacheLabel %></a><br>
		<a href="javascript:clearFlexTypeCache()"><%= clearFlexTypeCacheLabel %></a><br>
		<a href="javascript:wcHomePage('<%=wcHomePageLink%>')"><%= wcHomePage %></a><br>
	<%}%>
    <% } else if (lcsContext.inGroup(typeAdminGroup.toUpperCase()) ||
                  lcsContext.inGroup(userAdminGroup.toUpperCase()) ||
                  lcsContext.inGroup(processAdminGroup.toUpperCase()) ||
                  manageEnumerationEnabled
                 ) {
          if (lcsContext.inGroup(typeAdminGroup.toUpperCase())) { %>
            <a href="javascript:viewTM()"><%= manageTypesAndEnumerationsLabel %></a><br>
       <% }
          if (lcsContext.inGroup(processAdminGroup.toUpperCase())) { %>
            <a href="javascript:processAdmin()"><%= manageProcessesLabel %></a><br>
       <% }
          if (lcsContext.inGroup(userAdminGroup.toUpperCase())) { %>
            <div class='card'>
              <input type="checkbox" id="manageUsers" class="cardCheckbox">
              <div class='card-header'>
                <label class='card-label card-collapsible' for="manageUsers" title="<%= manageUsersLabel %>">
                  <%= manageUsersLabel %>
                </label>
              </div>
              <div class='card-content' id='manageUsersContent'>
              <% if (lcsContext.inGroup("ADMINISTRATORS") || (lcsContext.getUser().getOrganization() != null && lcsContext.getUser().getOrganization().getAdministrator() != null && lcsContext.inGroup(lcsContext.getUser().getOrganization().getAdministrator().getName().toUpperCase()))) { %>
                <a href="javascript:manageUsers('<%=orgUserGroupLink%>')">
                  <img width='16' align='middle' src='<%=WT_IMAGE_LOCATION%>/organization_context.gif' alt='' border='0' align='bottom'>
                  <%=FormatHelper.formatJavascriptString(orgLevelLabel)%>
                </a><br>
              <% } %>
              <% if (lcsContext.inGroup("ADMINISTRATORS")) { %>
                <a href="javascript:manageUsers('<%=siteUserGroupLink%>')">
                  <img width='16' align='middle' src='<%=WT_IMAGE_LOCATION%>/user.gif' alt='' border='0' align='bottom'>
                  <%=FormatHelper.formatJavascriptString(siteLevelLabel)%>
                </a><br>
              <% } %>
              </div>
            </div>
       <% }
          if (manageEnumerationEnabled) { %>
            <a href="javascript:viewTM()"><%= manageAttributeValueListsLabel %></a><br>
       <% }
        } else {
          if(manageEnumerationEnabled){ %>
            <a href="javascript:viewTM()"><%= manageAttributeValueListsLabel %></a><br>
     <% }
     } %>
  </div>
</div>
<%}%>
<!-- End Administrative -->

</div> <!-- END SITE NAVIGATION -->

<div id='productNavigator' class="productNavigator" onscroll='repositionActionMenu()'>
    PRODUCT NAV
</div>
    
<div id='materialNavigator' class="materialNavigator" onscroll='repositionActionMenu()'>
    Material NAV
</div>

<script>
    var hasProductTab = false;
    var hasMaterialTab = false;
    var colorwaySlideIndex = 0;
    var matSupplierSlideIndex = 0;
    
    function plusSlides(n) {
      showSlides(colorwaySlideIndex += n);
    }
    
    function showSlides(n) {
      var colorwaySlides = document.getElementsByClassName("colorwayContent-slide");
      var colorwayViewport = document.getElementsByClassName("colorwayContent-viewport")[0];
      var max = colorwaySlides.length - 1;
      if (n > max) {colorwaySlideIndex = 0}
      if (n < 0) {colorwaySlideIndex = max}
      if(colorwayViewport) {
          colorwayViewport.scroll({
                top: 0,
                left: colorwaySlideIndex*194, //This is the width of the viewport
                behavior: 'smooth'
              });
      }
    }
    
    // TODO: need to make this a common reusable function/functionality
    function plusMatSupplierSlides(n) {
      showMatSupplierSlides(matSupplierSlideIndex += n);
    }
    
    function showMatSupplierSlides(n) {
      var matSupplierSlides = document.getElementsByClassName("matSupplierContent-slide");
      var matSupplierViewport = document.getElementsByClassName("matSupplierContent-viewport")[0];
      var max = matSupplierSlides.length - 1;
      if (n > max) {matSupplierSlideIndex = 0}
      if (n < 0) {matSupplierSlideIndex = max}
      if(matSupplierViewport) {
    	  matSupplierViewport.scroll({
                top: 0,
                left: matSupplierSlideIndex*194, //This is the width of the viewport
                behavior: 'smooth'
              });
      }
    }

    //override function for obj ref quick info popup in sidebarframe
    function showDelayDiv(id, url, event) {
      var windowType = document.getElementsByName('windowType');
      if(windowType && windowType.length > 0 && windowType[0].value == 'READ_ONLY_POPUP') {
          return;
      }
      if (currentDelaySource != id) {
          currentDelaySource = id;
          var timeout = setTimeout('showDelayDivNow(\'' + id + '\', \'' + url + '\', \'completeShowColorwayQuickInfo\')', QUICKINFO_TIMEOUT);
          event.target.onmouseout = function() {
              clearTimeout(timeout);
              currentDelaySource = '';
          }
      }
    }

    var activeProductId;
    var activeSKUId;
    var activeOid;
    function getActionsMenuWithActiveProductId(Id){
        getActionsMenu(Id, '', '&activeProductId='+activeOid+'&contextSKUId='+activeSKUId);
    }
    ///////////////////////////////////////////////////////////////////////////
    function loadProductSideBar(productId,skuId,quickNavTab){
        var tempQuickNavTab='PRODUCT';
        if(hasContent(quickNavTab)){
            tempQuickNavTab=quickNavTab;
        }
        //setActiveProduct(productId, productId, '', '', true);
        if(!<%= LOAD_SIDEBAR_ONLY_ON_PRODUCTLINK %>){
            window.parent.contentframe.loadProductNavigator(productId,tempQuickNavTab,'',skuId);
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    function setActiveProductFromSeasonList(productId){
        setActiveProduct(productId, activeOid, '', '', true);
        if(window.parent.contentframe.changeProductSeason){
            window.parent.contentframe.changeProductSeason(productId);
        }else{
            window.parent.contentframe.loadProductNavigator(productId);
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    function setActiveProduct(productId, activeId, productName, skuId, forceReload){
        // CHECK TO SEE IF ACTIVE PRODUCT ID IS THE SAME
        //console.log("setActiveProduct");
        hasProductTab = true;
        if(!(forceReload == true) && activeProductId == productId && activeSKUId == skuId){
            changeNavigatorTab('product');
            if (activeId == activeProductId)
                return;
        }
        if(activeProductId != productId || forceReload){
            if(forceReload){
                console.log("setActiveProduct: forceReload");
            }else{
                console.log("setActiveProduct: loading with new product id");
            }
            if(!forceReload && activeProductId != productId && !sessionStorage.getItem("isSwitchSPL")) {
                sessionStorage.setItem("productPageSideBarState", "sideBarExpand");
            }
            sessionStorage.setItem("isSwitchSPL", "false");
            runPostAjaxRequest(location.protocol + '//' + location.host + '<%=PageManager.getContextOverrideUrlAndMasterContorller()%>', 'activity=VIEW_SIDE_BAR_PRODUCT_NAVIGATOR&timecode=' + new Date().getTime() + '&oid=' + activeId +'&globalChangeTrackingSinceDate='+window.parent.headerframe.getGlobalChangeTrackingSinceDate(), 'completeLoadProductNavigator');
        } else {
            changeNavigatorTab('product');
        }

        activeProductId = productId;
        activeSKUId = skuId;
        if(hasContent(activeId)){
            activeOid = activeId;
        } else {
            activeOid = productId;
        }
        console.log("setActiveProduct: activeProductId = " + activeProductId + " activeSKUId " + activeSKUId + " activeId = " + activeId);
        //The following 2 parameters are set in here so that the quick actions from the side menu (Update Measurement Set, Update Constuction Set)will return to the view season product link page upon completion.
        document.REQUEST.oid = activeOid;
        document.REQUEST.activity = "VIEW_SEASON_PRODUCT_LINK";
        //Clear the old activity/action info to prevent going to the wrong place
        document.MAINFORM.activity.value="VIEW_SEASON_PRODUCT_LINK";
        document.MAINFORM.action.value="";
        document.MAINFORM.oid.value=productId;
        document.MAINFORM.returnActivity.value="VIEW_SEASON_PRODUCT_LINK";
        document.MAINFORM.returnAction.value="";
        document.MAINFORM.returnOid.value=productId;
        document.MAINFORM.copyFromOid.value = '';
        document.MAINFORM.productId.value='';
        document.MAINFORM.seasonId.value = '';

    }

    function reloadMaterialSideBar(materialId, activeId){
    	setActiveMaterial(materialId, activeId, "", true);
    }
    
    function setActiveMaterial(materialId, activeId, materialName, forceReload){
        // CHECK TO SEE IF ACTIVE MATERIAL ID IS THE SAME
        hasMaterialTab = true;
        if(activeId != materialId || forceReload){
            if(forceReload){
                console.log("setActiveMaterial: forceReload");
            }else{
                console.log("setActive: loading with new product id");
            }
	        sessionStorage.setItem("nonProductPageSideBarState", "sideBarExpand");
            runPostAjaxRequest(location.protocol + '//' + location.host + '<%=PageManager.getContextOverrideUrlAndMasterContorller()%>', 'activity=VIEW_SIDE_BAR_MATERIAL_NAVIGATOR&timecode=' + new Date().getTime() + '&oid=' + activeId +'&globalChangeTrackingSinceDate='+window.parent.headerframe.getGlobalChangeTrackingSinceDate(), 'completeLoadMaterialNavigator');
        } else {
            changeNavigatorTab('material');
        }

        activeMaterialId = materialId;
        if(hasContent(activeId)){
            activeOid = activeId;
        } else {
            activeOid = materialId;
        }
        console.log("setActiveMaterial: activeMaterialId = " + activeMaterialId + " activeId = " + activeId);
        //The following 2 parameters are set in here so that the quick actions from the side menu (Update Measurement Set, Update Constuction Set)will return to the view season product link page upon completion.
        document.REQUEST.oid = activeOid;
        document.REQUEST.activity = "VIEW_MATERIAL";
        //Clear the old activity/action info to prevent going to the wrong place
        document.MAINFORM.activity.value="VIEW_MATERIAL";
        document.MAINFORM.action.value="";
        document.MAINFORM.oid.value=materialId;
        document.MAINFORM.returnActivity.value="VIEW_MATERIAL";
        document.MAINFORM.returnAction.value="";
        document.MAINFORM.returnOid.value=materialId;
        document.MAINFORM.copyFromOid.value = '';
    }

    function completeLoadProductNavigator(xml, text){
        changeNavigatorTab('product');
        //console.log(text);
        var div = document.getElementById('productNavigator');
        parseScripts(text);
        div.innerHTML = text;
        colorwaySlideIndex = 0;
        showSlides(colorwaySlideIndex);
        initializeDragDrop(true);
    }

    function completeLoadMaterialNavigator(xml, text){
        changeNavigatorTab('material');
        var div = document.getElementById('materialNavigator');
        parseScripts(text);
        div.innerHTML = text;
        colorwaySlideIndex = 0;
        //for material Color Chips
        showSlides(colorwaySlideIndex);
        initializeDragDrop(true);
        // Hide plus icons for supplier and colorway in case of Material Popup
        if(window.parent.name == "popup"){
            document.getElementById('matSupplierNewSupplier').style.display = "none";
            document.getElementById('colorwayPanelNewColorways').style.display = "none";
        }
    }

    function reloadSideBarOnSuccess(){
        setActiveProduct(activeProductId, activeOid, "", activeSKUId, true);
         }
    ////////////////////////////////////////////////////////////////////////////
    function addToFavorites(ids){
        //displayFavorites();
        if(hasContent(ids)){
           runPostAjaxRequest(location.protocol + '//' + location.host + '<%=PageManager.getContextOverrideUrlAndMasterContorller()%>','activity=ADD_TO_FAVORITES&oids=' + ids + '&timecode=' + new Date().getTime(), 'completeAddFavorites');
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    function completeAddFavorites(){
        loadFavorites();
    }
    function loadFavorites(){
        runPostAjaxRequest(location.protocol + '//' + location.host + '<%=PageManager.getContextOverrideUrlAndMasterContorller()%>','activity=VIEW_FAVORITES' + '&timecode=' + new Date().getTime(), 'completeViewFavorites');
    }
    ////////////////////////////////////////////////////////////////////////////
    function completeViewFavorites(xml, text){
        var div = document.getElementById('favoritesListDiv');
        div.innerHTML = text;
    }
    ////////////////////////////////////////////////////////////////////////////
    function removeFromFavorites(){
        var ids = getCheckBoxIds(document.MAINFORM.favoritesCheckBox);
        runPostAjaxRequest(location.protocol + '//' + location.host + '<%=PageManager.getContextOverrideUrlAndMasterContorller()%>','activity=REMOVE_FROM_FAVORITES&oids=' + ids + '&timecode=' + new Date().getTime(), 'completeViewFavorites');
        //alert(ids);
    }
    ////////////////////////////////////////////////////////////////////////////
    function viewSelectedLineSheet(){
        var seasonList = document.MAINFORM.seasonSelectList;
        viewLinePlan(seasonList.options[seasonList.selectedIndex].value);
    }
    function viewSelectedLineBoard(){
        var seasonList = document.MAINFORM.seasonSelectList;
        viewLineBoard(seasonList.options[seasonList.selectedIndex].value);
    }
    function viewSelectedPalette(){
        var seasonList = document.MAINFORM.seasonSelectList;
        clear_returnToSearchOID(); //Added for SPR 8078368
        viewSeasonPalette(seasonList.options[seasonList.selectedIndex].value);
    }
    function viewSelectedPlanning(){
        var seasonList = document.MAINFORM.seasonSelectList;
        viewSeasonPlanning(seasonList.options[seasonList.selectedIndex].value);
    }
    function viewSelectedDashboard(){
        var seasonList = document.MAINFORM.seasonSelectList;
        viewSeasonDashboards(seasonList.options[seasonList.selectedIndex].value);
    }
    function viewSelectedSamplesConsole(){
        var seasonList = document.MAINFORM.seasonSelectList;
        viewSamplesConsole(seasonList.options[seasonList.selectedIndex].value);
    }
    function viewSelectedCalendar(){
        var seasonList = document.MAINFORM.seasonSelectList;
        viewSeasonCalendar(seasonList.options[seasonList.selectedIndex].value);
    }
    function viewSelectedSeasonDetails(){
        var seasonList = document.MAINFORM.seasonSelectList;
        if(window.sessionStorage){
             sessionStorage.setItem("RETURN_TO_SEARCH_view","false");
        }else{
            CookieManager.setCookie({name:'RETURN_TO_SEARCH_view',value:"false"});
        }
        clear_returnToSearchOID();  //Added for SPR 8078368
        viewObject(seasonList.options[seasonList.selectedIndex].value);
    }
    function viewSelectedSilhouettes(){
        var seasonList = document.MAINFORM.seasonSelectList;
        clear_returnToSearchOID();  //Added for SPR 8078368
        viewSeasonSilhouette(seasonList.options[seasonList.selectedIndex].value);
    }
    function viewSelectedInspirations(){
        var seasonList = document.MAINFORM.seasonSelectList;
        clear_returnToSearchOID();  //Added for SPR 8078368
        viewSeasonInspiration(seasonList.options[seasonList.selectedIndex].value);
    }

    var activeSeasonMenu = document.getElementById('ac_seasonActions');
    function getActiveSeasonActions(){
        console.log("getActiveSeasonActions!");

        var seasonList = document.MAINFORM.seasonSelectList;
        activeSeasonMenu.id = ('ac_' + seasonList.options[seasonList.selectedIndex].value);
        console.log(activeSeasonMenu.id);
        getActionsMenu(seasonList.options[seasonList.selectedIndex].value);
    }
    ////////////////////////////////////////////////////////////////////////////
    loadFavorites();
     
    ////////////////////////////////////////////////////////////////////////////
    function changeNavigatorTab(tab, openSideNav){
        var productNavLink = document.getElementById('productNavLink');
        var siteNavLink = document.getElementById('siteNavLink');
        var materialNavLink = document.getElementById('materialNavLink');
        var frm = window.parent.document.getElementById('sidebarframe');
       
        closeActionsMenu();
        if(frm.scrollWidth === 20 && openSideNav){
            // open side navigation if user click on site or product link
            toggleSideBar();
        }

        if(tab == 'site'){
            showDiv('siteNavigator');
            hideDiv('productNavigator');
            hideDiv('materialNavigator');
            if(productNavLink.className == 'navTabSelected')
                productNavLink.className = 'navTab';
            siteNavLink.className = 'navTabSelected';
            if(materialNavLink.className == 'navTabSelected' || materialNavLink.className != 'navTabHidden')
            materialNavLink.className = 'navTab';
        } else if (tab == 'product'){
            showDiv('productNavigator');
            hideDiv('siteNavigator');
            hideDiv('materialNavigator');
            productNavLink.className = 'navTabSelected';
            siteNavLink.className = 'navTab';
            if(materialNavLink.className == 'navTabSelected' || materialNavLink.className != 'navTabHidden')
            materialNavLink.className = 'navTab';
        } else if (tab == 'material'){
            showDiv('materialNavigator');
            hideDiv('productNavigator');
            hideDiv('siteNavigator');
            materialNavLink.className = 'navTabSelected';
            siteNavLink.className = 'navTab';
            if(productNavLink.className == 'navTabSelected' || productNavLink.className != 'navTabHidden')
            	productNavLink.className = 'navTab';
            if(window.parent.name == "popup"){
            	 siteNavLink.className = 'navTabHidden';
                 productNavLink.className = 'navTabHidden';
            }
        }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    function checkInitialLoadWorkList(){
        var workListDiv = document.getElementById('workListDiv');
        if(!hasContent(workListDiv.innerHTML)){
            console.log("checkInitialLoadWorkList: LOADING");
            loadWorkList();
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    checkInitialLoadWorkList();

    ////////////////////////////////////////////////////////////////////////////
    function loadWorkList(){
        console.log("loadWorkList: start");
        var workListDiv = document.getElementById('workListDiv');
        workListDiv.innerHTML = "<center><br><img border='0' src='<%= URL_CONTEXT %>/images/blue-loading.gif'><br></center>";

        runPostAjaxRequest(location.protocol + '//' + location.host + '<%=URL_CONTEXT%><%=WORKITEMS_LISTING%>', 'timecode=' + new Date().getTime(), 'completeLoadWorkList');
    }
    ////////////////////////////////////////////////////////////////////////////
    function completeLoadWorkList(xml, text){
        //console.log(text);
        var workListDiv = document.getElementById('workListDiv');
        workListDiv.innerHTML = text;
    }
    ////////////////////////////////////////////////////////////////////////////
    function removeFromFavorites(){
        var ids = getCheckBoxIds(document.MAINFORM.favoritesCheckBox);
        runPostAjaxRequest(location.protocol + '//' + location.host + '<%=PageManager.getContextOverrideUrlAndMasterContorller()%>','activity=REMOVE_FROM_FAVORITES&oids=' + ids + '&timecode=' + new Date().getTime(), 'completeViewFavorites');
    }
    ////////////////////////////////////////////////////////////////////////////
    // Baseline Report additions start //

    function viewReport(activity){
        document.MAINFORM.activity.value = activity;
        document.MAINFORM.oid.value = getSelectedSeasonId();
        submitForm();
    }

    function viewVendorObject(oid){
        if(oid.indexOf('LCSSupplier') > -1){
            viewSupplier(oid);
        }
    }
    function launchRFQ(){
        //alert("RFQ");
        document.MAINFORM.activity.value="VIEW_RFQ_PAGE";
        document.MAINFORM.action.value="INIT";
        submitForm();
    }

    function launchSampleList(){
        //alert("RFQ");
        document.MAINFORM.activity.value="VIEW_VENDOR_SAMPLES_PAGE";
        document.MAINFORM.action.value="INIT";
        submitForm();
    }

    function launchVendorInfo(){
        //alert("RFQ");
        document.MAINFORM.activity.value="VIEW_VENDOR_INFO";
        document.MAINFORM.action.value="INIT";
        submitForm();
    }
    ///////////////////////////////////////////////////////////////////////////
    function viewSPL(tabPage){
        if(hasContent(activeSKUId)){
            document.MAINFORM.contextSKUId.value = activeSKUId;
        }
        else{
            document.MAINFORM.contextSKUId.value = '';
        }

        viewProductSeasonLink(activeOid, tabPage);
    }

    //Added this to keep from having to rewrrite grabbing season id stuff each time. CG
    function getSelectedSeasonId(){
        var seasonList = document.MAINFORM.seasonSelectList;
        var seasonId = (seasonList.options[seasonList.selectedIndex].value);
        return seasonId;
    }

function changeSelectedSeason(id){
    var seasonList = document.MAINFORM.seasonSelectList;
    setSelectedValueOfListFromValue(seasonList, id);
}

function viewAllSparcReports()
{
		document.MAINFORM.activity.value = 'QML_CUSTOM_REPORTS';
		document.MAINFORM.action.value = 'INIT';
        submitForm();
}

    function go(goAct){

         if(goAct == 'exportAllFiltersViews'){
                if(confirm('<%= FormatHelper.formatJavascriptString(areYouSureExportAllFiltersViewsCnfrm, false)%>')){
                    document.MAINFORM.activity.value = '<%= "EXPORT_ALL_FILTERS_VIEWS" %>';
                    document.MAINFORM.action.value = 'ExportAllFiltersViews';
                    document.MAINFORM.goAct = "";
                    displayWorkingMessage();
                    submitForm();
                }else{
                    document.MAINFORM.activity.value = '<%= "EXPORT_CANCELED" %>';
                    document.MAINFORM.action.value = 'ExportCalendars';
                    document.MAINFORM.goAct = "";
                    submitForm();
                }
            }else if(goAct == 'exportAllUserAndGroups'){
                if(confirm('<%= FormatHelper.formatJavascriptString(areYouSureExportAllUserGroupsCnfrm, false) %>')){
                    document.MAINFORM.activity.value = '<%= "EXPORT_ALL_USERS_AND_GROUPS" %>';
                    document.MAINFORM.action.value = 'ExportAllUserAndGroups';
                    document.MAINFORM.goAct = "";
                    displayWorkingMessage();
                    submitForm();
                }else{
                    document.MAINFORM.activity.value = '<%= "EXPORT_CANCELED" %>';
                    document.MAINFORM.action.value = 'ExportCalendars';
                    document.MAINFORM.goAct = "";
                    submitForm();
                }
            }else if(goAct == 'exportCalendars'){
                    if(confirm('<%= FormatHelper.formatJavascriptString(areYouSureExportAllCalendarCnfrm, false) %>')){
                        document.MAINFORM.activity.value = '<%= "EXPORT_CALENDARS" %>';
                        document.MAINFORM.action.value = 'ExportCalendars';
                        document.MAINFORM.goAct = "";
                        displayWorkingMessage();
                        submitForm();
                    }else{
                        document.MAINFORM.activity.value = '<%= "EXPORT_CANCELED" %>';
                        document.MAINFORM.action.value = 'ExportCalendars';
                        document.MAINFORM.goAct = "";
                        submitForm();
                    }
            }else if(goAct == 'extractTeams'){
                    if(confirm('<%= FormatHelper.formatJavascriptString(areYouSureExportAllTeamsCnfrm, false) %>')){
                        document.MAINFORM.activity.value = '<%= "EXTRACT_TEAMS" %>';
                        document.MAINFORM.action.value = 'ExtractTeams';
                        document.MAINFORM.goAct = "";
                        displayWorkingMessage();
                        submitForm();
                    }else{
                        document.MAINFORM.activity.value = '<%= "EXPORT_CANCELED" %>';
                        document.MAINFORM.action.value = 'ExportCalendars';
                        document.MAINFORM.goAct = "";
                        submitForm();
                    }
            }
    }

    function showAppSwitcher(){
        var divWindow = new jsWindow(120, 150, -8, 5, 30, '<%=FormatHelper.encodeAndFormatForHTMLContent(switchToLabel)%>', 20, false, false, true);
        runPostAjaxRequest(urlContext + '<%=PageManager.getMasterControllerUrl()%>', '&activity=APP_SWITCHER', 'ajaxDefaultResponse');
        setTimeout(function(){ closeDivWindow(); }, 5000);
    }
    function showMyWorkListItems(){
    	document.MAINFORM.activity.value = 'VIEW_WORK_LIST_TABLE';
        document.MAINFORM.action.value = '';
        submitForm();
    }
    //side navigator default behavior
    var SIDE_MENU_NAVIGATION_SET_PIN = <%= SIDE_MENU_NAVIGATION_SET_PIN %>;
    if(SIDE_MENU_NAVIGATION_SET_PIN) {
        sessionStorage.setItem("nonProductPageSideBarState", "sideBarExpand");
    } else {
        sessionStorage.setItem("nonProductPageSideBarState", "sideBarCollape");
    }
    var pinNavigatorButton = '<%= FormatHelper.formatHTMLString(pinNavigatorButton) %>';
    var unpinNavigatorButton = '<%= FormatHelper.formatHTMLString(unpinNavigatorButton) %>';
    var SIDE_MENU_NAVIGATION_CLICK = <%= SIDE_MENU_NAVIGATION_CLICK %>;
    var SIDE_MENU_NAVIGATION_HOVER_DELAY = <%= SIDE_MENU_NAVIGATION_HOVER_DELAY %>;
    var SIDE_NAVIGATION_RESIZE_DISABLED_BROWSER_SPECIFIC = <%= SIDE_NAVIGATION_RESIZE_DISABLED_BROWSER_SPECIFIC %>;
    var lastSize = 20;
    if(!hasProductTab && !hasMaterialTab) {
        defaultPin();
    }
</script>

<jsp:include page="<%= subURLFolder + STANDARD_TEMPLATE_FOOTER %>" flush="true">
    <jsp:param name="bodyClass" value="xnavigatorBody" />
</jsp:include>
