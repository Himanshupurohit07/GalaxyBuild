<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>


<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
        import="com.lcs.wc.client.web.*,
                java.util.*,
                com.lcs.wc.util.*,
                com.lcs.wc.specification.*,
                com.lcs.wc.product.*,
                com.lcs.wc.client.web.pdf.*,
                com.lcs.wc.util.*,
                wt.util.WTProperties"
%>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<%!
    public static String timeToLive = LCSProperties.get("time.before.temp.files.are.deleted", "1");

%>
<%
    //String pdfTooltip = WTMessage.getLocalizedMessage ( RB_MAIN, "pdfTooltip", objA ) ;
    String pdfTooltip = "Export to PDF" ;
    boolean iconOnly = FormatHelper.parseBoolean(request.getParameter("iconOnly"));

    Map addlColors = PDFHelperMoreColors.getColors();
    PDFGeneratorHelper.loadAddlColorOptions(addlColors);

    String specId = request.getParameter("specId");
    String generateCSV = request.getParameter("pdf");
    String specPages = request.getParameter("specPages");

%>
<SCRIPT language='JavaScript'>
        function generatePDF(){
            document.MAINFORM.pdf.value = 'true' ;
            submitForm();
        }
</SCRIPT>

<% if(!iconOnly && FormatHelper.hasContent(specId)){ %>
    <% if ("true".equalsIgnoreCase(generateCSV)){
        
        DeleteFileHelper dFH = new DeleteFileHelper();
        dFH.deleteOldFiles(FileLocation.PDFDownloadLocationImages,timeToLive);
        dFH.deleteOldFiles(FileLocation.PDFDownloadLocationFiles,timeToLive);

        PDFProductSpecificationGenerator ppsg = null;

        
        if(specId.indexOf("FlexSpecToSeasonLink") > -1){
            FlexSpecToSeasonLink fstsl = (FlexSpecToSeasonLink)(new LCSProductQuery()).findObjectById(specId);
            ppsg = new PDFProductSpecificationGenerator(fstsl);

        }
        else{
            FlexSpecification spec = (FlexSpecification)(new LCSProductQuery()).findObjectById(specId);
            ppsg = new PDFProductSpecificationGenerator(spec);
            
        }

        if(FormatHelper.hasContent(specPages)){
            Collection pages = MOAHelper.getMOACollection(specPages);
			System.out.println("------------specPages---------------"+specPages);
            ppsg.setPages(pages);
        }

        String fileURL = "";

        if(FormatHelper.hasContent(request.getParameter("paperSize"))){
            ppsg.setPageSize(PDFPageSize.getPageSize(request.getParameter("paperSize")));
        }
        else{
            ppsg.setPageSize(PDFPageSize.getPageSize("LETTER"));
        }

        if("false".equals(request.getParameter("useLandscape"))){
            ppsg.setLandscape(false);
        }
        else{
            ppsg.setLandscape(true);
        }

        fileURL = ppsg.generateSpec();
		String downloadFileKey = new java.util.Date().getTime() + "";
		lcsContext.requestedDownloadFile.put(downloadFileKey, fileURL);


    %>
    <SCRIPT language='JavaScript'>
		var w=flexWindowOpenAsPost('Main.jsp?forwardedFileForDownload=true&forwardedFileForDownloadKey=<%=downloadFileKey%>');
        document.MAINFORM.pdf.value='false';
    </SCRIPT>

   <% }%>

<% } %>
