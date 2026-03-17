<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- //////////////////////////////////////////////////////////////////////////////////////--%>

<%@page language="java"
       import="com.lcs.wc.db.SearchResults,
                com.lcs.wc.db.FlexObject,
                com.lcs.wc.client.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.season.*,
                com.lcs.wc.foundation.*,
                com.lcs.wc.util.*,
				org.apache.logging.log4j.Logger,
				org.apache.logging.log4j.LogManager,
                java.util.*,
				wt.util.*,
				java.text.*,
				java.io.*,
				com.lcs.wc.sourcing.*,
				com.sparc.wc.exports.services.SparcCostSheetExporterFactory,
				com.sparc.wc.exports.services.SparcLineSheetExporter,
				com.sparc.tc.domain.WorkbookFileSystem,
				com.sparc.tc.util.WorkBookFileSystemUtil"
%>

<%!
	public static final String JSPNAME="SPARCDownloadCostSheetData";
	private static final Logger logger = LogManager.getLogger("rfa.sparc.jsp.sourcing.SPARCDownloadCostSheetData");
	public static final String summaryCSDownloadZipFileName = LCSProperties.get("jsp.sparc.sourcing.SPARCDownloadCostSheetData.summaryCSDownloadZipFileName","CostSheet_SummaryDownload");
	public static final String detailedCSDownloadZipFileName = LCSProperties.get("jsp.sparc.sourcing.SPARCDownloadCostSheetData.detailedCSDownloadZipFileName","CostSheet_DetailedDownload");
	
%>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>

<%
	
	final String selectedIds = request.getParameter("selectedIds");
	final String seasonId = request.getParameter("seasonId");
	final String division = request.getParameter("division");
	final String csType = request.getParameter("csType");
	final Boolean isProdContext = Boolean.parseBoolean(request.getParameter("appColors"));
	
		try{
			System.out.println("isProdContext--------------------"+isProdContext);
		    final SparcLineSheetExporter exporter = SparcCostSheetExporterFactory.getExporter(csType, division, seasonId, selectedIds,isProdContext);
			final WorkbookFileSystem wfs = exporter.export();
			final String wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
			final String exportDir = wtHome+File.separator+File.separator+"codebase"+File.separator+"rfa"+File.separator+"temp"+"sparc_exports";
            WorkBookFileSystemUtil.flush(wfs, exportDir);
            final String exportedFile = exportDir+File.separator+wfs.getRootName();
            final long csSummaryTemplatedownloadFileKey = System.currentTimeMillis();
            lcsContext.requestedDownloadFile.put(String.valueOf(csSummaryTemplatedownloadFileKey),exportedFile);
			response.setContentType("text/xml");
			out.println("<downloadFileKey>" + String.valueOf(csSummaryTemplatedownloadFileKey) + "</downloadFileKey>");
            out.flush();
			logger.debug("**completed file download:");
		}
		catch(Exception e){
		    logger.error("Encountered an error while exporting detailed cost-sheet, error:"+e.getMessage());
			e.printStackTrace();
		}
		
 %>