<%-- Copyright (c) 2003 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// PAGE DOCUMENTATION//////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import=" com.lcs.wc.util.*,
                java.util.*,
				wt.util.*,com.sparc.wc.util.SparcUtil"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="flexg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<%
    flexg.setCreate(true);
%>
<%-- Initialize constants and messages --%>
<%!
    public static final String highLevelUploadStatusMessage = "The file upload has been started in the background. Please check the Business Object Apparel High Level after sometime";
%>

<%-- Initialize success message and division parameters --%>
<%
    String showSuccessMessageParam = request.getParameter("showSuccessMessage");
	
	System.out.println("showSuccessMessageParam--------"+showSuccessMessageParam);
	
	Collection<String> groups = lcsContext.getContext().getGroups();
	System.out.println("groups--------------------"+groups);
	String sparcBOMReport = "SPARC BOM REPORT";
	String catalystBOMReport = "CATALYST BOM REPORT";
	
	String reportToFetch = LCSProperties.get("com.sparc.wc.report.reportsName","AERO BOM REPORT,COLOR WHERE USED REPORT,LUCKY BOM REPORT,LUCKYBOMCOLORWAYSREPORT");
	System.out.println("============Reports========"+reportToFetch);
	String[] reportsArray = reportToFetch.split(",");
   
%>

<%-- Start HTML and content of the page --%>
<table width="100%">
    <tr>
        <td class="PAGEHEADING">
            <table width="100%">
                <tr>
                    <td class="PAGEHEADINGTITLE">
                        Sparc Reports
                    </td>
                </tr>              
            </table>
        </td>
    </tr>
	<br>
	 <tr>
        <td>

	<%for (int i = 0; i < reportsArray.length; i++){
		if(lcsContext.inGroup(reportsArray[i].toUpperCase())){%>
			&nbsp &nbsp  <b> <a style="color:blue;"href="javascript:runReport('<%= SparcUtil.fetchQMLURLForReport(reportsArray[i]) %>')"><%= reportsArray[i] %></a><br></b>
		<%}	
	}%>			

  </td>
    </tr>
</table>


<%-- Javascript Functionality for Running Reports --%>
<script language="Javascript">

    // Function to open report URL in a new window or tab
    function runReport(url) {
        // Open the URL in a new browser tab/window
        window.open(url, '_blank');
    }

</script>

<%-- End of page --%>
