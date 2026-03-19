<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// PAGE DOCUMENTATION//////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%--
    JSP Type: Controller
--%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@ page language="java"
    errorPage="../../../../jsp/exception/ControlException.jsp"
        import="com.lcs.wc.client.web.PageManager,
                org.apache.logging.log4j.Logger,
                org.apache.logging.log4j.LogManager,
				com.lcs.wc.util.*,
				wt.util.*,wt.queue.*,com.sparc.wc.costsheet.uploader.SPARCCostSheetQueueManager"
			

%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INCLUDED FILES  //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>


<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

<% wt.util.WTContext.getContext().setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// STATIS JSP CODE //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

<%!
    private static final Logger logger = LogManager.getLogger("rfa.sparc.jsp.sourcing.SPARCReportController");
    public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");
	public static final String JSPNAME = "SPARCReportController";
    public static final String MAINTEMPLATE = PageManager.getPageURL("MAINTEMPLATE", null);
    public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");


%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%
    logger.debug("**Request Params:"+RequestHelper.hashRequest(request));
	String activity = request.getParameter("activity");
    String action = request.getParameter("action");
    String seasonVersionId = request.getParameter("seasonVersionId");
	String title = "SPARC REPORTS";
	String view = "";
	String showSuccessMessage ="";
	String strDivision = "";
	String seasonId = "";
	String uploadedFileName = "";
	
	
	if("QML_CUSTOM_REPORTS".equals(activity)){		
         if("INIT".equals(action)){
            view = "VIEW_QML_REPORTS";			
        }		
	}
	
	String contentPage = null;
    if(view != null){
        contentPage = PageManager.getPageURL(view, null);
    } else {
        contentPage = "";
    }
 %>

<jsp:forward page="<%=subURLFolder+ MAINTEMPLATE %>">
    <jsp:param name="title" value="<%= java.net.URLEncoder.encode(title, defaultCharsetEncoding)%>" />
   <jsp:param name="requestedPage" value="<%= contentPage %>" />
   <jsp:param name="contentPage" value="<%= contentPage %>" />
   <jsp:param name="showSuccessMessage" value="<%= showSuccessMessage %>" />
</jsp:forward>