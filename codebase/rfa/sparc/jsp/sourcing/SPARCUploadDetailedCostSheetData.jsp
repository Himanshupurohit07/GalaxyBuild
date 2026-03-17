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
				wt.util.*"
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
<%!
	public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
	public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");
	public static final String supportedFileFormats = LCSProperties.get("jsp.sparc.sourcing.UploadDetailCSTemplate.allowedFileTypes","xls,zip,xlsx");
	public static final String costSheetDetailUploadHeading = LCSProperties.get("jsp.sparc.sourcing.UploadDetailCSTemplate.costSheetSummarUploadHeading","CostSheet Detail Upload");
	public static final String uploadFileLabel = LCSProperties.get("jsp.sparc.sourcing.UploadDetailCSTemplate.uploadFileLabel","Upload File");
	public static final String noLoadFileSelected = LCSProperties.get("jsp.sparc.sourcing.UploadDetailCSTemplate.noFileSelectedMessage","No file selected for upload");
	public static final String invalidFileFormat = LCSProperties.get("jsp.sparc.sourcing.UploadDetailCSTemplate.invalidFieFormatMessage","File selected with invalid format, Allowed formats are "+supportedFileFormats);
	public static final String detailUploadFWStatusMessage = LCSProperties.get("jsp.sparc.sourcing.UploadDetailCSFWTemplate.detailUploadStatusMessage","The file upload has been started in the background. Please check the Business Object Footwear Detailed Breakdown Upload Status after sometime");
	public static final String detailUploadAPPStatusMessage = LCSProperties.get("jsp.sparc.sourcing.UploadDetailCSAPPTemplate.detailUploadStatusMessage","The file upload has been started in the background. Please check the Business Object Apparel Detailed Breakdown Upload Status after sometime");
	
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%
String loadButton = WTMessage.getLocalizedMessage ( RB.MAIN, "load_Btn",RB.objA ) ;
String fileLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "file_LBL",RB.objA ) ;

%>
<%
	String showSuccessMessageParam = request.getParameter("showSuccessMessage");
	System.out.println("**showSuccessMessageParam:"+showSuccessMessageParam);
	String strDivision = request.getParameter("division");
	String strStatusMessage = "";
	
	if("Apparel".equalsIgnoreCase(strDivision)){
	 	strStatusMessage = detailUploadAPPStatusMessage;
	}else{
		strStatusMessage = detailUploadFWStatusMessage;
	}

%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JAVSCRIPT ///////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

<% if(showSuccessMessageParam!=null && FormatHelper.hasContent(showSuccessMessageParam)){ %>

<script>
alert('<%= strStatusMessage %>');
window.close();
</script>
<%} else { %>
<script language="Javascript">

    function load(){
        if(validate()){
            document.MAINFORM.activity.value = 'COSTSHEET_BREAKDOWN_UPLOAD_TEMPLATE';
            document.MAINFORM.action.value = 'PERFORM_UPLOAD';
            submitForm();	
        }
    }

   function validate(){
     if(document.MAINFORM.loadFile.value =="null" || document.MAINFORM.loadFile.value==""){
         alert('<%= FormatHelper.formatJavascriptString(noLoadFileSelected ,false)%>');
     return false;
     }else{
    	 var fileName = document.MAINFORM.loadFile.value;
    	 var fileExt = fileName.substring(fileName.lastIndexOf(".")+1);
    	 var supportedExtensions = '<%=supportedFileFormats%>';
    	 
    	 if(supportedExtensions.indexOf(fileExt) !== -1){
    		 return true;
    	 }
    	 else{
    		alert('<%=invalidFileFormat%>');
    		return false;
	 	}
       }
    }

</script>



<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// HTML ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<input name="division" type="hidden" value='<%= strDivision %>'>
<input name="typeId" type="hidden" value='<%= FormatHelper.format(request.getParameter("typeId")) %>'>
<input name="seasonId" type="hidden" value='<%= FormatHelper.format(request.getParameter("oid")) %>'>
<table width="100%">
    <tr>
        <td class="PAGEHEADING">
            <table width="100%">
                <tr>
                    <td class="PAGEHEADINGTITLE">
                        <%= costSheetDetailUploadHeading %>
                    </td>
                    <td class='button' class="PAGEHEADINGTEXT" align="right">
                        <a class='button' href="javascript:load()"><%= loadButton %></a>&nbsp;&nbsp;&nbsp;&nbsp;
                    </td>
               </tr>
           </table>
       </td>
    </tr>
      <td>
         <%= tg.startGroupBorder() %>
         <%= tg.startTable() %>
         <%= tg.startGroupTitle() %><%= uploadFileLabel %><%= tg.endTitle() %>
         <%= tg.startGroupContentTable() %>
         <col width="15%"></col><col width="35%"></col>
         <col width="15%"></col><col width="35%"></col>
         <tr>
                 <td class="FORMLABEL" nowrap valign=middle>
                  &nbsp;&nbsp;<%= fileLabel %>&nbsp;
                 </td>
                 <td class="FORMELEMENT" nowrap valign=middle>
                  <input  size = "50" name="loadFile" type="FILE" style="height: 21px" accept=".xls, .xlsx, .zip">
                 </td>
         </tr>

         <%= tg.endContentTable() %>
         <%= tg.endTable() %>
         <%= tg.endBorder() %>
      </td>
    </tr>


</table>
<% } %>
