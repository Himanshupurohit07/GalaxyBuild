<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@ page language="java"
    errorPage="/../../exception/ControlException.jsp"
        import="com.lcs.wc.db.SearchResults,
      com.lcs.wc.client.web.PageManager,
      com.lcs.wc.client.web.WebControllers,
      com.lcs.wc.client.Activities,
      com.lcs.wc.material.*,
      com.lcs.wc.foundation.*,
      com.lcs.wc.flextype.*, 
      com.lcs.wc.document.FileRenamer,
      com.lcs.wc.db.*,
	  java.util.regex.Pattern,
      wt.doc.*,
      java.util.*,
	  org.apache.commons.io.FileUtils,
      com.lcs.wc.util.*,
      com.lcs.wc.load.*,
      java.util.Collection,
      com.lcs.wc.util.OSHelper,
      wt.util.WTProperties,
      wt.util.WTMessage,
    com.lcs.wc.flextype.FlexTypeCache,
      java.io.*"

%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INCLUDED FILES  //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>


<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="wtcontext" class="wt.httpgw.WTContextBean" scope="request"/>
<jsp:setProperty name="wtcontext" property="request" value="<%=request%>"/>
<% wt.util.WTContext.getContext().setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// STATIS JSP CODE //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%!
	public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");

    public static final String JSPNAME = "SPARCCsvLoadFileController";
    public static final String MAINTEMPLATE = PageManager.getPageURL("MAINTEMPLATE", null);
    public static final String adminGroup = LCSProperties.get("jsp.main.administratorsGroup", "Administrators");
	public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");
	public static String getUrl = "";
    public static String WindchillContext = "/Windchill";
    public static String wt_home = "";
    public static String wTCodebase = "";
	public static final String lcsPropertiesCsvLoadFileLogPathURL = FormatHelper.formatOSFolderLocation(LCSProperties.get("com.lcs.wc.content.CsvLoadFileLogURL", "none"));

    static 
    {
        try
        {
            WTProperties wtproperties = WTProperties.getLocalProperties();
			getUrl = wtproperties.getProperty("wt.server.codebase","");
			WindchillContext = "/" + wtproperties.getProperty("wt.webapp.name");
            wt_home =  wtproperties.getProperty("wt.home");
			wTCodebase = wtproperties.getProperty("wt.codebase.location","");

        } catch(Exception e){
            e.printStackTrace();
        }
    }
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%
String activity = request.getParameter("activity");
String action = request.getParameter("action");
String oid = request.getParameter("oid");
String returnActivity = request.getParameter("returnActivity");
String returnAction = request.getParameter("returnAction");
String returnOid = request.getParameter("returnOid");
boolean checkReturn = false;
String FILE_SEPARATOR = File.separator;
String title = "";
String errorMessage = (FormatHelper.hasContent(request.getParameter("errorMessage"))) ?
		java.net.URLDecoder.decode(request.getParameter("errorMessage"), defaultCharsetEncoding) : "";
String infoMessage = request.getParameter("infoMessage");
String view = null;
String formType = "standard";

String type = request.getParameter("type");
String fileToRemove = request.getParameter("fileToRemove");
boolean saveAs = false;
String importCSVFileName = null;
String importCSVFile = "false";
String additionalParameters = "";



   ///////////////////////////////////////////////////////////////////

boolean hasAdminAccess = (FormatHelper.hasContent(adminGroup) && lcsContext.inGroup(adminGroup.toUpperCase()) || lcsContext.inGroup("PRODUCT_SKU_SEASON_LOAD_GROUP"));

if(!hasAdminAccess){
    throw new LCSAccessException(RB.EXCEPTION, "administrativePermissionToPerformAction_LBL",RB.objA );
}


/////////////////////////////////////////////////////////////////////////////////////////

if("LOAD_A_FILE".equals(activity)){

    view = PageManager.LOAD_FILE_PAGE;

    title = WTMessage.getLocalizedMessage (RB.MAIN, "loadAFile_PG_TLE", RB.objA ) ;
    formType = "csvLoadFile";


  if("SAVE".equals(action)){
    //This is run after the load button is pressed.
    try {

        String filePath = FormatHelper.formatOSFolderLocation(LCSProperties.get("com.lcs.wc.content.CsvLoadFilePath", "none"));
        String logFilePath = FormatHelper.formatOSFolderLocation(LCSProperties.get("com.lcs.wc.content.CsvLoadFileLogPath", "none"));
        String timeToLive = LCSProperties.get("time.before.temp.files.are.deleted", "4800000");

        filePath = wt_home + filePath;
        String loadFile = (String)request.getAttribute("loadFile");
		
		System.out.println("loadFile-----------------------------"+loadFile);
		
        String mapFileName = "";
		if(request.getAttribute("mapFile") != null) {
			mapFileName = (String)request.getAttribute("mapFile");
		}

        title = "Load File";
        view = PageManager.SAFE_PAGE;
        // view = PageManager.LOAD_FILE_PAGE;
        ///////////////////////////////////////
        String dataFileName = (String)request.getAttribute("loadFile");
		
		System.out.println("dataFileName-----CSVLoadController---------------------------"+dataFileName);
		
        String token = request.getParameter("token");

        if(token == null || token.equalsIgnoreCase("")){
            token = "TAB";
        }
        if(token.equalsIgnoreCase("TAB")){
            token = "TAB";
        }


        String mapFileDropDown = request.getParameter("mapFileDropDown");
        String logFile = "";
        String savedLogFileName = "";
        //Sets up the map file

        try{
            if(mapFileName == null || mapFileName.equalsIgnoreCase("")) {
                if(mapFileDropDown.equalsIgnoreCase("lcsMapFile")){
                      mapFileName = wt_home + FILE_SEPARATOR + "loadFiles" + FILE_SEPARATOR + "lcsLoadFiles" + FILE_SEPARATOR + "lcsmapfile.txt";
                      savedLogFileName = wt_home + logFilePath + "import.txt";
                      File savedLogFile = new File(savedLogFileName);
                      savedLogFile = FileRenamer.rename(savedLogFile);
                      savedLogFileName = savedLogFile.getName();
                      logFile = wt_home + logFilePath + savedLogFileName;
                }
                if(mapFileDropDown.equalsIgnoreCase("csvmapfile")) {

                    mapFileName = wt_home + FILE_SEPARATOR + "loadFiles" + FILE_SEPARATOR + "csvmapfile.txt";
                    savedLogFileName = wt_home + logFilePath + "import.txt";
                    File savedLogFile = new File(savedLogFileName);
                    savedLogFile = FileRenamer.rename(savedLogFile);
                    savedLogFileName = savedLogFile.getName();
                    logFile = wt_home + logFilePath + savedLogFileName;
                }
            } else {
                savedLogFileName = wt_home + logFilePath + "import.txt";
                File savedLogFile = new File(savedLogFileName);
                savedLogFile = FileRenamer.rename(savedLogFile);
                savedLogFileName = savedLogFile.getName();
                logFile = wt_home + logFilePath + savedLogFileName;
            }
          }catch(Exception e){}

        ////////////////////////////////////////////

          Hashtable<?,?> hashtable = new Hashtable<Object,Object>(10);

          boolean isSeason = LoadFile.isLoadingSeason(dataFileName, token);
          LoadFile.performLoad(dataFileName,mapFileName,token,logFile);

          importCSVFileName = wTCodebase + lcsPropertiesCsvLoadFileLogPathURL + savedLogFileName;
          importCSVFile="true";



			try {//this just ensures not leaking data back to client per file paths
			 String content = FileUtils.readFileToString(new File(importCSVFileName), "UTF-8");

			String secureDataFileString = dataFileName.substring(dataFileName.lastIndexOf(FormatHelper.formatOSFolderLocation("/")) + 1);
			String secureMapFileString = mapFileName.substring(mapFileName.lastIndexOf(FormatHelper.formatOSFolderLocation("/")) + 1);

			 //mod slashes if exist (if windows) in content so can use regex in replaceall
			 content = content.replaceAll("\\\\", "/");
			 content = content.replaceAll("\\\\", "/");
			 String dataFilePath=dataFileName.replaceAll("\\\\", "/");
			 content = content.replaceAll(Pattern.quote(dataFilePath), secureDataFileString);
			 content = content.replaceAll(mapFileName.replaceAll("\\\\", "/"), secureMapFileString);

			 File tempFile = new File(importCSVFileName);
			 FileUtils.writeStringToFile(tempFile, content, "UTF-8");
			} catch (IOException e) {
			 //Simple exception handling, replace with what's necessary for your use case!
			 throw new RuntimeException("Generating file failed", e);
			}



          DeleteFileHelper dFH = new DeleteFileHelper();
          dFH.deleteOldFiles(filePath,timeToLive);
          dFH.deleteOldFiles(wt_home + logFilePath,timeToLive);
          DeleteFileHelper.deleteFile(dataFileName);
          view = PageManager.SAFE_PAGE;
          if (isSeason) {
    	  	 additionalParameters = "&forceReloadSeasonDropdown=true";
          }
    	
      } catch(Exception e){
        e.printStackTrace();
      }
      action = "";
      activity = "LOAD_A_FILE";

    } 


}

    String contentPage = null;
    if(view != null){
        contentPage = PageManager.getPageURL(view, null);
    } else {
        contentPage = "";
    }

    lcsContext.viewCache.clearCache();
    lcsContext.filterCache.clearCache();


	String downloadFileKey = new java.util.Date().getTime() + "";
	lcsContext.requestedDownloadFile.put(downloadFileKey, importCSVFileName);

%>
<jsp:forward page="<%=subURLFolder+ MAINTEMPLATE %>">
    <jsp:param name="title" value="<%= java.net.URLEncoder.encode(title, defaultCharsetEncoding) %>" />
    <jsp:param name="infoMessage" value="<%= infoMessage %>" />
    <jsp:param name="errorMessage" value="<%= java.net.URLEncoder.encode(errorMessage, defaultCharsetEncoding) %>" />
    <jsp:param name="requestedPage" value="<%= contentPage %>" />
    <jsp:param name="contentPage" value="<%= contentPage %>" />
    <jsp:param name="oid" value="<%= oid %>" />
    <jsp:param name="action" value="<%= action %>" />
    <jsp:param name="formType" value="<%= formType %>" />
    <jsp:param name="additionalParameters" value="<%= additionalParameters %>" />
    <jsp:param name="activity" value="<%= activity %>" />
    <jsp:param name="objectType" value="Document" />
    <jsp:param name="type" value="<%= type %>" />
    <jsp:param name="saveAs" value="<%= saveAs %>" />
   <jsp:param name="importCSVFile" value="<%= importCSVFile %>" />
   <jsp:param name="forwardedFileForDownloadKey" value="<%= downloadFileKey%>" />

</jsp:forward>
