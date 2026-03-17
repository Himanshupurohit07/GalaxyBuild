<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>
 

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
        import="com.lcs.wc.client.web.*,
                java.util.*,
                com.lcs.wc.util.*,
                com.lcs.wc.specification.*,
                com.lcs.wc.db.*,
                com.lcs.wc.document.ZipGenerator,
                com.lcs.wc.measurements.*,
                com.lcs.wc.product.*,
                com.lcs.wc.client.web.pdf.*,
				com.lcs.wc.sizing.*,
				com.lcs.wc.sourcing.*,
                com.lcs.wc.util.*,
				wt.enterprise.*,
				com.lcs.wc.flextype.*,
				com.lcs.wc.document.*,
				wt.doc.*,
				org.apache.logging.log4j.Logger,
                org.apache.logging.log4j.LogManager,
				wt.content.*,
				java.net.*,
				com.lcs.wc.foundation.*,
				java.util.regex.*,
                wt.util.WTProperties"
%>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="appContext" class="com.lcs.wc.client.ApplicationContext" scope="session"/>

<% lcsContext.setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));%>

<%!
    private static final Logger logger = LogManager.getLogger("rfa.jsp.product.PDFProductSpecGenerator2");
	public static String timeToLive = LCSProperties.get("time.before.temp.files.are.deleted", "1");
	public static final String DEFAULT_ENCODING = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");
	
    public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");

%>
<%

logger.debug("Start----------PDFProductSpecGenerator2.jsp---------------------------------");
 
   boolean iconOnly = FormatHelper.parseBoolean(request.getParameter("iconOnly"));

    Map addlColors = PDFHelperMoreColors.getColors();
    PDFGeneratorHelper.loadAddlColorOptions(addlColors);

    String specId = request.getParameter("specId");
    String productId = request.getParameter("productId");
    if (!FormatHelper.hasContent(productId)) {
        productId = request.getParameter("splId");
    }
    String generateCSV = request.getParameter("pdf");
    String specPages = request.getParameter("specPages");
    String availDocs = request.getParameter("availDocs");
    String availEPMDOCDocs = request.getParameter("availEPMDOCDocs");
    String availParts = request.getParameter("availParts");
	String oid = request.getParameter("oid");
	
	boolean asynchronousGeneration = FormatHelper.parseBoolean(request.getParameter("asynchronousGeneration"));
	//logger.debug("asynchronousGeneration="+asynchronousGeneration);
	
	
	
	boolean ajaxRequest = FormatHelper.parseBoolean(request.getParameter("ajaxRequest"));

	if(!ajaxRequest){
%>
<SCRIPT language='JavaScript'>
        function generatePDF(){
            document.MAINFORM.pdf.value = 'true' ;
            submitForm();
        }
</SCRIPT>

<%}


if(!iconOnly && (FormatHelper.hasContent(specPages) || FormatHelper.hasContent(availDocs)|| FormatHelper.hasContent(availEPMDOCDocs))|| FormatHelper.hasContent(availParts)){ %>
    <% if ("true".equalsIgnoreCase(generateCSV)){
if(logger.isDebugEnabled()){ logger.debug("PPSG2.request:  " + RequestHelper.hashRequest(request));}
        
        appContext.setProductContext(oid);
		//LCSProduct product = appContext.getProductARev();
		
		String fileURL = "";
		Map params = RequestHelper.hashRequest(request);
		params.put("clientLocale",wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));
        if (specId == null){
            if(appContext.getFstslId() != null){
                specId = appContext.getFstslId();
            } else {
                specId = appContext.getSpecId();
            }
        }
		
		
		
		if (productId == null)
			productId = appContext.getProductARevId();
		if(!asynchronousGeneration){
			//fileURL = FlexSpecHelper.service.generateTechPack(timeToLive, specId, productId, specPages,
            //    availDocs, params);
			logger.debug("PPSG2.params:::  " + params);
			fileURL = new com.sparc.wc.specification.SPARCFlexSpecLogic().generateTechPack
					(timeToLive,specId,productId,specPages,availDocs, params);
					logger.debug("PPSG2.fileURL:  " + fileURL);
		}else{
			FlexSpecHelper.service.asyncGenerateTechPack(timeToLive,specId,productId,specPages,params,availDocs);
			logger.debug("Else asyncGenerateTechPack");
		}

		String downloadFileKey = new java.util.Date().getTime() + "";
		if(!asynchronousGeneration || ajaxRequest){
			lcsContext.requestedDownloadFile.put(downloadFileKey, fileURL);
		}

        if(ajaxRequest){
        	out.println(PageManager.getContextOverrideUrlAndMasterContorller() + "?forwardedFileForDownload=true&forwardedFileForDownloadKey=" + downloadFileKey);
        }else if(!asynchronousGeneration){
        %>
			 <SCRIPT language='JavaScript'>
				function openDownloadWindow() {
					var w=flexWindowOpenAsPost('Main.jsp?forwardedFileForDownload=true&forwardedFileForDownloadKey=<%=downloadFileKey%>');
					document.MAINFORM.pdf.value='false';
				}
				if (window.addEventListener){ 
					window.addEventListener('load', openDownloadWindow, false); 
				} else if (window.attachEvent){ 
					window.attachEvent('onload',openDownloadWindow);
				}

			</SCRIPT>
	<%} %>

   <% }%>

<% } %>
<% logger.debug("End------------PDFProductSpecGenerator2.jsp---------------------------------");
%>
