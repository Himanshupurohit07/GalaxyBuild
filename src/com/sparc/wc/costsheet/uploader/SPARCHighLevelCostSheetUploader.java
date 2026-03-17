package com.sparc.wc.costsheet.uploader;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.sparc.wc.util.SparcCostingUploadUtil;
import com.sparc.wc.util.SparcFileUtil;

import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTProperties;


/**
 * Main Class to perform High Level Upload/Create
 */
/**
 * 
 */
public class SPARCHighLevelCostSheetUploader {

	private static final Logger LOGGER = LogR.getLogger(SPARCHighLevelCostSheetUploader.class.getName());
	private static final String CS_HIGHLEVEL_TEMPLATE = LCSProperties.get("com.sparc.wc.costsheet.uploader.SPARCHighLevelCostSheetUploader.Template","HIGHLEVEL_APPAREL_template.xlsx");
	private static final String CS_HIGHLEVEL_DOCUMENT_TYPE_PREFX = "com.sparc.wc.costsheet.uploader.SPARCHighLevelCostSheetUploadHelper.docType.";
	private static final String CS_HIGHLEVEL_BO_TYPE = "Business Object\\scApparelHighLevelBO";
	private static final String CS_HIGHLEVEL_DEFAULT_DOC_TYPE = "Document\\scApparelHighLevel";
	private static String baseDir = "";
    private static String wt_home = "";
    static {
	        try {
	            WTProperties wtProperties = WTProperties.getLocalProperties();
	            wt_home = wtProperties.getProperty("wt.home");
	            baseDir = wt_home + File.separator + "codebase" + File.separator + "sparc_export_templates" + File.separator;
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    }
    
    
	/**
	 * This is the Starter Method to process the user input for update/create
	 * @param fileName
	 * @param strDivision
	 * @param seasonId
	 */
	public static void processFile(String fileName,String strDivision, String seasonId){
		LOGGER.debug("**start processFile:"+fileName+"::"+strDivision+"::"+seasonId);
        String extension = SparcFileUtil.extractExtension(fileName);
        if (extension.equals("")) {
            return;
        }
        String userName = "";
        LCSSeason objSeason = null;
        String seasonName = "";
        try {
            userName = ClientContext.getContext().getUser().getName();
            objSeason = (LCSSeason)LCSQuery.findObjectById(seasonId);
            seasonName = objSeason.getName();
        }catch(WTException ex){
            ex.printStackTrace();
        }
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
        String timestamp = now.format(formatter);
        String file_name = String.format("data_%s", timestamp);
        String strBO_Name= seasonName +"_" + "HIGHLEVEL" + "_" +userName+ "_" + file_name;
        LOGGER.debug("**strBO_Name:"+strBO_Name);
        String businessObjectId = SparcCostingUploadUtil.createHighLevelBusinessObject(strBO_Name, CS_HIGHLEVEL_BO_TYPE);
        switch (extension) {
            case "zip":
                processZipFile(fileName,strDivision,businessObjectId,seasonName,strBO_Name);
                break;
            case "xlsx":
                upload(fileName,strDivision,businessObjectId,false,strBO_Name);
                break;
        }
        LOGGER.debug("** End processFile**");
    }
    
    /**
     * This method is triggered if the File uploaded is .zip
     * @param file
     * @param strDivision
     * @param businessObjectId
     * @param seasonName
     * @param strBO_Name
     */
    public static void processZipFile(String file, String strDivision, String businessObjectId,String seasonName, String strBO_Name) {
        LOGGER.info("**processZipFile start with:"+file+":::"+strDivision+":::"+businessObjectId+":::"+seasonName);
		
		  String workingDir =SparcCostingUploadUtil.initFolderForUnZipping(System.currentTimeMillis() +""); 
		  String archivedFile;
		  String docFlexTypePath = "";
		  try { 
			  List<String> unzippedFiles = SparcCostingUploadUtil.unzipContentsTo(file, workingDir); 
			  LOGGER.debug("**unzippedFiles:"+unzippedFiles);
			  LinkedList<String> summaryFiles= new LinkedList<>(); 
			  
			  for(String unzippedFile :unzippedFiles) {
				  LOGGER.debug("**Processing UnzipFile:"+unzippedFile);
				  String uploadSummaryFile = upload(unzippedFile,strDivision,businessObjectId,true,strBO_Name);
				  if(uploadSummaryFile!=null && FormatHelper.hasContent(uploadSummaryFile))
					  summaryFiles.add(uploadSummaryFile);
			  }
		  archivedFile = SparcCostingUploadUtil.archiveTheFiles(summaryFiles);
		  docFlexTypePath =  LCSProperties.get(CS_HIGHLEVEL_DOCUMENT_TYPE_PREFX+strDivision,CS_HIGHLEVEL_DEFAULT_DOC_TYPE);
		  SPARCHighLevelCostSheetUploadHelper.uploadDataAndSummaryFiles(file, archivedFile, businessObjectId, strBO_Name,docFlexTypePath);
		  SparcCostingUploadUtil.deleteTheInPutFiles(workingDir); 
		  } 
		  catch (Exception e){
			  LOGGER.error("Encountered an exception while processing the zip file, error:" + e.getMessage()); 
			  if (LOGGER.isDebugEnabled()) 
				  e.printStackTrace(); 
			  }
		 
        LOGGER.info("**processZipFile end with:"+file+":::"+strDivision+":::"+businessObjectId+":::"+seasonName);
		
    }
    
    /**
     * 
     * This method is triggered to process each .xlsx
     * @param dataFileName
     * @param strDivision
     * @param businessObjectId
     * @param isZipFile
     * @param strBO_Name
     * @return
     */
    public static String upload(String dataFileName,String strDivision, String businessObjectId,Boolean isZipFile, String  strBO_Name) {
       String summaryFileName = "";
    	LOGGER.info("**upload start with:"+dataFileName+":::"+strDivision+":::"+businessObjectId+":::"+isZipFile);
        String templateFile = SparcFileUtil.getAbsoluteFileName(baseDir,CS_HIGHLEVEL_TEMPLATE);
        
        if(!FormatHelper.hasContent(templateFile)) {
   		  LOGGER.error("No template file found for Upload, please configure one");
   		  return "";
   		  }
        SPARCHighLevelCostSheetUploadHelper HLCSHelper = new SPARCHighLevelCostSheetUploadHelper();
        summaryFileName = HLCSHelper.performUpload(templateFile, dataFileName, businessObjectId, isZipFile, strDivision, strBO_Name);
		
        LOGGER.info("**upload end with:"+dataFileName+":::"+strDivision+":::"+businessObjectId+":::"+isZipFile);
		return summaryFileName;
    }
    
}
