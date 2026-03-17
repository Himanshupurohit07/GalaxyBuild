package com.sparc.wc.util;


import com.lcs.wc.db.*;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.document.LCSDocumentClientModel;
import com.lcs.wc.document.LCSDocumentLogic;
import com.lcs.wc.flextype.*;
import com.lcs.wc.foundation.*;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import wt.fc.WTObject;
import wt.introspection.WTIntrospector;
import wt.log4j.LogR;
import wt.pds.DatabaseInfoUtilities;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.pom.Transaction;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 */
public class SparcCostingUploadUtil {

    private static final String TEMPLATE_MAPPING_PREFIX = "com.sparc.wc.costsheet.detailedtemplateMappings.";

    private static String baseDir = "";

    private static final Logger LOGGER = LogR.getLogger(SparcCostingUploadUtil.class.getName());


    private static final String EXPORT_LOCATION = FormatHelper.formatOSFolderLocation(LCSProperties.get("com.lcs.wc.client.web.ExcelGenerator.exportLocation", "costSheetExport"));

    private static String wt_home = "";

    private static final String BO_FLEX_TYPE = "com.sparc.wc.bo.flextype.";

    private static final String DOC_FLEX_TYPE = "com.sparc.wc.document.flextype.";
    static {
        try {
            WTProperties wtProperties = WTProperties.getLocalProperties();
            wt_home = wtProperties.getProperty("wt.home");
            baseDir = wt_home + File.separator + "codebase" + File.separator + "CostSheetTemplate" + File.separator;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * This method gets the template file
     * @param strDivision
     * @return
     */
    public static String getTemplateFile(final String strDivision){
        String templateFileName = LCSProperties.get(TEMPLATE_MAPPING_PREFIX+strDivision);
        return SparcFileUtil.getAbsoluteFileName(baseDir,templateFileName);
    }


    public static SearchResults getCostSheetObjectResult(String costSheetNumber) {
        costSheetNumber = getLongNumber(costSheetNumber);
        LOGGER.debug("Start of the getCostSheetObjectResult-----costSheetNumber----------"+costSheetNumber);
        PreparedQueryStatement statement = new PreparedQueryStatement();
        SearchResults results = null;
        try {
            FlexType costSheetFlexType = FlexTypeCache.getFlexTypeFromPath("Cost Sheet\\scSparc");
            statement.appendFromTable(SparcCostingConstants.PRODUCT_COST_SHEET,SparcCostingConstants.LCS_COST_SHEET);
            statement.appendSelectColumn(new QueryColumn(SparcCostingConstants.LCS_COST_SHEET, LCSProductCostSheet.class,SparcCostingConstants.ITERATION_INFO_BRANCH_ID));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(SparcCostingConstants.LCS_COST_SHEET, LCSProductCostSheet.class, SparcCostingConstants.ITERATION_INFO_LATEST), "1", "="));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(SparcCostingConstants.LCS_COST_SHEET, LCSProductCostSheet.class,
                    costSheetFlexType.getAttribute(SparcCostingConstants.COST_SHEET_NUMBER).getColumnDescriptorName()), costSheetNumber, "="));
            LOGGER.debug("Query Statement---------------" + costSheetNumber);
            results = LCSQuery.runDirectQuery(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return results;

    }

    public static String getLongNumber(final String costSheetNumber){
        double parsedValue = Double.parseDouble(costSheetNumber);
        long exactValue = (long) parsedValue;
        return String.valueOf(exactValue);
    }

    /**
     *
     * @param objResults
     * @return
     */
    public static LCSCostSheet getCostSheetObject(SearchResults objResults) {
        Vector objVector = objResults.getResults();
        FlexObject costIDObject = (FlexObject) objVector.elementAt(0);
        LCSCostSheet objCostSheet = null;
        try {
            objCostSheet = (LCSCostSheet) LCSQuery.findObjectById(SparcCostingConstants.VR_COST_SHEET_STR + costIDObject.getString(SparcCostingConstants.COST_BRANCH_ID));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return objCostSheet;
    }

    public static String getInternalNameForEnum(FlexType flexType, String key, String value) {
        try {

            FlexTypeAttribute flexAtt = flexType.getAttribute(key);
            AttributeValueList attList = flexAtt.getAttValueList();
            Collection<String> keys = attList.getSelectableKeys(Locale.getDefault(),true);
            if (keys != null) {
                for (String enumKey : keys) {
                    if (attList.getValue(enumKey, Locale.getDefault()).equals(value))
                        return enumKey;
                }
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    public static Collection<String> getEnumKeys(FlexType flexType, String key) {
        Collection<String> enumKeys = new LinkedHashSet<>();
        try {
            FlexTypeAttribute flexAtt = flexType.getAttribute(key);
            AttributeValueList attList = flexAtt.getAttValueList();
            enumKeys =  attList.getSelectableKeys(Locale.getDefault(),true);
        } catch (Exception e) {
            LOGGER.error("Error while getting the keys------" + e.getMessage());
        }
        return enumKeys;
    }

    public static FlexType getBOMFlexType(String strDivision){
        FlexType bomFlexType = null;
        try {
            if (SparcCostingConstants.FOOTWEAR.equalsIgnoreCase(strDivision)) {
                bomFlexType = FlexTypeCache.getFlexTypeFromPath(SparcCostingConstants.FOOTWEAR_BOM);
            } else if (SparcCostingConstants.APPAREL.equalsIgnoreCase(strDivision)) {
                bomFlexType = FlexTypeCache.getFlexTypeFromPath(SparcCostingConstants.APPAREL_BOM);
            }

        }catch(Exception e){
            LOGGER.error("Error occured while fetching the BOM FlexType-----"+e.getMessage());
        }
        return bomFlexType;
    }

    public static boolean isListType(FlexTyped flexTyped, String key) {
        try {
            if (flexTyped.getFlexType().getAttribute(key).getAttVariableType().equalsIgnoreCase(SparcCostingConstants.S_CHOICE) || flexTyped.getFlexType().getAttribute(key).getAttVariableType().equalsIgnoreCase(SparcCostingConstants.S_DRIVEN))
                return true;
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDateType(FlexTyped flexTyped, String key) {
        try {
            if (flexTyped.getFlexType().getAttribute(key).getAttVariableType().equalsIgnoreCase(SparcCostingConstants.S_DATE))
                return true;
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isNumberType(FlexTyped flexTyped,String key){
        try {
            String strDateType = flexTyped.getFlexType().getAttribute(key).getAttVariableType();
            if(SparcCostingConstants.S_CURRENCY.equalsIgnoreCase(strDateType) || SparcCostingConstants.S_FLOAT.equalsIgnoreCase(strDateType) || SparcCostingConstants.S_INTEGER.equalsIgnoreCase(strDateType)){
                return true;
            }
        }catch(Exception ex){
            return false;
        }
        return false;
    }

    public static boolean isTextType(FlexTyped flexTyped,String key){
        try {
            String strDateType = flexTyped.getFlexType().getAttribute(key).getAttVariableType();
            if(SparcCostingConstants.S_TEXT.equalsIgnoreCase(strDateType) ||SparcCostingConstants.S_TEXT_AREA.equalsIgnoreCase(strDateType) ){
                return true;
            }
        }catch(Exception ex){
            return false;
        }
        return false;
    }



    public static boolean containsLetterOrSpecialChar(String data) {
        Pattern pattern = Pattern.compile("[a-zA-Z!@#$%^&*()_+{}|:\"<>?`~\\[\\]\\;',/\\\\]");
        Matcher matcher = pattern.matcher(data);
        return matcher.find();
    }

    public static List<String> getRequiredAttributes(LCSProductCostSheet csModel){
        List<String> requiredAttributes = new ArrayList<>();
        try {
            for(FlexTypeAttribute flexTypeAttribute:csModel.getFlexType().getAllAttributes()){
                 if(flexTypeAttribute.isAttRequired()){
                        requiredAttributes.add(flexTypeAttribute.getAttKey());
                 }
            }
        } catch (WTException e) {
           LOGGER.error("Error while iterating through the attributes------"+e.getMessage());
        }
        return requiredAttributes;
    }

    public static boolean isReference(FlexTyped flexTyped, String key) {
        try {
            if (flexTyped.getFlexType().getAttribute(key).getAttVariableType().equalsIgnoreCase(SparcCostingConstants.S_OBJECT_REF))
                return true;
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static WTObject getObjectData(FlexTyped objCostSheet, String attKey, String data){
        LCSLifecycleManaged objCycleManaged = null;
        try {
            objCostSheet.getFlexType().getAttribute(attKey).getRefDefinition().getFlexTypeClass();
            FlexType objFlexType =objCostSheet.getFlexType().getAttribute(attKey).getRefType();
            PreparedQueryStatement statement = new PreparedQueryStatement();
            statement.appendFromTable(LCSLifecycleManaged.class);
            statement.appendSelectColumn(new QueryColumn(LCSLifecycleManaged.class, "thePersistInfo.theObjectIdentifier.id"));
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, objFlexType.getAttribute("name").getColumnDescriptorName()), data, "="));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, LCSQuery.TYPED_BRANCH_ID), objFlexType.getIdNumber(), "="));
            System.out.println("statement toString-----getObjectData-------------"+statement.toString());
            SearchResults  results = LCSQuery.runDirectQuery(statement);
            if(results.getResultsFound() > 0){
                FlexObject flexBO= (FlexObject) results.getResults().elementAt(0);
                objCycleManaged =(LCSLifecycleManaged) LCSQuery.findObjectById(objCostSheet.getFlexType().getAttribute(attKey).getRefDefinition().getIdPrefix() + flexBO.getString("LCSLIFECYCLEMANAGED.IDA2A2"));
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return objCycleManaged;
    }

    /**
     *
     * @param zipFilePath
     * @param targetDir
     * @return
     * @throws IOException
     */
    public static List<String> unzipContentsTo(String zipFilePath, String targetDir) throws IOException {
        ZipFile zipFile = new ZipFile(zipFilePath);

        if (zipFile == null) {
            return null;
        }

        List<String> paths = new ArrayList<>();
        byte[] buffer = new byte[1024];
        try {
            zipFile.stream()
                    .filter(entry -> entry != null && !entry.isDirectory())
                    .forEach(entry -> {
                        try (InputStream inputStream = zipFile.getInputStream(entry)) {
                            String extractPath = SparcFileUtil.getAbsoluteFileName(targetDir, entry.getName());
                            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(extractPath))) {
                                int readLen;
                                while ((readLen = inputStream.read(buffer)) != -1) {
                                    bos.write(buffer, 0, readLen);
                                }
                                paths.add(extractPath);
                            }
                        } catch (IOException e) {
                            LOGGER.error("Encountered an error while reading the entry in zip file, error: " + e.getMessage());
                            if (LOGGER.isDebugEnabled())
                                e.printStackTrace();
                        }
                    });
        } finally {
            zipFile.close();
        }

        return paths;
    }


    /**
     * This method creates the excel file and writes the errors and warnings to the newly created excel file
     * @param errors
     * @return
     */
    public static String writeErrorsAndWarningsToExcel(List<String> errors,List<String> warnings,String strFileName,int errorCount,int warningCount) {
        long currentTimeMillis = System.currentTimeMillis();
        strFileName = strFileName.replaceAll("[ /,]", "");
        String logsDir = baseDir + "logs";

        File logsFolder = new File(logsDir);
        if (!logsFolder.exists()) {
            logsFolder.mkdirs(); // Create the logs folder if it doesn't exist
        }

        String fileName = logsDir + File.separator + strFileName + "_" + "UploadSummary" + ".xlsx";
        Boolean isSuccessfull = true;
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet summarySheet = workbook.createSheet("Summary");
            Row row = null;
            Cell cell = null;

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row with Summary Description
            Row headerRow = summarySheet.createRow(0);
            Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("Summary Description");
            //headerCell.setCellStyle(headerStyle);

            Row errorCountRow = summarySheet.createRow(1);
            CellStyle countStyle = workbook.createCellStyle();
            Cell errorCountCell = errorCountRow.createCell(0);
            errorCountCell.setCellValue("Number of Errors: " + errorCount);

            int errorRowNum = 2;
            if(errors != null) {
                isSuccessfull = false;
                for (String error : errors) {
                    row = summarySheet.createRow(errorRowNum++);
                    cell = row.createCell(0);
                    cell.setCellValue(error);
					applyWrapTextStyle(workbook, cell);
                }
            }
            Row warningCountRow = summarySheet.createRow(errorRowNum++);
            Cell  warningCountCell = warningCountRow.createCell(0);
            warningCountCell.setCellValue("Number of Warnings: " + warningCount);
            // Writing warnings to the Warnings sheet
            if(warnings != null) {
                isSuccessfull = false;
                for (String warning : warnings) {
                    row = summarySheet.createRow(errorRowNum++);
                    cell = row.createCell(0);
                    cell.setCellValue(warning);
					applyWrapTextStyle(workbook, cell);
                }
            }
			int lastRowNum = summarySheet.getLastRowNum();
			for (int i = 0; i <= lastRowNum; i++) {
               Row currentRow = summarySheet.getRow(i);
				if (currentRow != null) {
					cell = currentRow.getCell(0);
					if (cell != null) {
						applyWrapTextStyle(workbook, cell);
					}				
                    if (i == 0) {
                        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
                        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(headerStyle);
                        summarySheet.autoSizeColumn(0);
                        continue;
                    }
					summarySheet.addMergedRegion(new CellRangeAddress(i, i, 0, 20));
				}
			}

            // Writing the workbook to the specified file path
            try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
                workbook.write(outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }
	
	private static void applyWrapTextStyle(Workbook workbook, Cell cell) {
        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        cell.setCellStyle(wrapStyle);
    }

    public static String archiveTheFiles(List<String> filesToArchive){
        String zipFilePath = "";
        try {
            zipFilePath = baseDir+File.separator+ "logs" + File.separator+"archive.zip";
            FileOutputStream fos = new FileOutputStream(zipFilePath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for (String filePath : filesToArchive) {
                File file = new File(filePath);
                if (file.exists()) {
                    addToZipFile(file, zos);
                    file.delete(); // Delete the file after zipping
                } else {
                    System.out.println("File not found: " + filePath);
                }
            }
            zos.close();
            fos.close();

            System.out.println("Zipping and deletion successful.");
        } catch (IOException e) {
            System.err.println("Error occurred while zipping or deleting files: " + e.getMessage());
        }
        return zipFilePath;
    }

    private static void addToZipFile(File file, ZipOutputStream zos) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }
    /**
     *
     * @param name
     * @param strDivision
     */
    public static String createDetailedBusinessObject(final String name, final String strDivision){
        LCSLifecycleManagedClientModel businessObj = new LCSLifecycleManagedClientModel();
        String businessObjectId = "";
        try {
            businessObj.setFlexType(FlexTypeCache.getFlexTypeFromPath(LCSProperties.get(BO_FLEX_TYPE+strDivision)));
            businessObj.setLifecycleManagedName(name);
            businessObj.setValue(SparcCostingConstants.BO_NAME,name);
            businessObj.setValue(SparcCostingConstants.SC_STATUS_KEY,SparcCostingConstants.SC_STATUS_INPROGRESS);
            businessObj.setValue(SparcCostingConstants.START_DATE_KEY,setDateForBusinessObject());
            businessObj.save();
            businessObjectId = FormatHelper.getObjectId(businessObj.getBusinessObject());
        } catch (WTPropertyVetoException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        }
        return businessObjectId;
    }

    public static String setDateForBusinessObject(){
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String formattedDate = currentDate.format(formatter);
        return formattedDate;
    }

    public static String createHighLevelBusinessObject(final String name, final String flexTypePath){
        LCSLifecycleManagedClientModel businessObj = new LCSLifecycleManagedClientModel();
        String businessObjectId = "";
        try {
        	businessObj.setFlexType(FlexTypeCache.getFlexTypeFromPath(flexTypePath));
        	businessObj.setLifecycleManagedName(name);
        	businessObj.setValue(SparcCostingConstants.BO_NAME,name);
        	businessObj.setValue(SparcCostingConstants.SC_STATUS_KEY,SparcCostingConstants.SC_STATUS_INPROGRESS);
            businessObj.setValue(SparcCostingConstants.START_DATE_KEY,setDateForBusinessObject());
            businessObj.save();
            businessObjectId = FormatHelper.getObjectId(businessObj.getBusinessObject());
        } catch (WTPropertyVetoException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        }
        return businessObjectId;
    }
    /**
     * This method creates a Document object and associates the DataSheet and uploadSummary File to the  Document.
     * It also associates the created document object to the Business Object.
     * @param dataSheet
     * @param uploadSummaryFile
     */
    public static void uploadDataAndSummaryFiles(String dataSheet, String uploadSummaryFile,String strBusOid,String documentName,String strDivision){
        LCSDocumentClientModel documentModel = new LCSDocumentClientModel();
        final LCSDocumentLogic documentLogic = new LCSDocumentLogic();
        Collection<String> docIds =  new ArrayList<>();
        Transaction trx = new Transaction();
        documentName = documentName + System.currentTimeMillis();
        try{
            trx.start();
			FlexType objFlexType =FlexTypeCache.getFlexTypeFromPath(LCSProperties.get(DOC_FLEX_TYPE+strDivision));
            documentModel.setFlexType(objFlexType);
            documentModel.setValue("Name",documentName);
            documentModel.setName(documentName);
            documentModel.setContentComment("TEST COMMENT");
            int numberOfFiles = 10;
            String[] secondaryContentFiles = new String[numberOfFiles];
            String[] secondaryContentComments = new String[numberOfFiles];
            documentModel.setSecondaryContentFiles(secondaryContentFiles);
            documentModel.setSecondaryContentComments(secondaryContentComments);
            documentModel.save();

            String oid = FormatHelper.getVersionId(documentModel.getBusinessObject());
            LCSDocument objectDOc =(LCSDocument) LCSQuery.findObjectById(oid);
            documentLogic.associateContent(objectDOc,dataSheet);
            if(FormatHelper.hasContent(uploadSummaryFile)) {
                secondaryContentFiles[0] = uploadSummaryFile;
                secondaryContentComments[0] = "Upload Summary";
                documentLogic.associateSecondaryContents(objectDOc, secondaryContentFiles, secondaryContentComments);
            }
            objectDOc =(LCSDocument) VersionHelper.latestIterationOf(objectDOc);

            docIds.add(FormatHelper.getObjectId(objectDOc.getMaster()));
            LCSLifecycleManaged detailBusinessObject = (LCSLifecycleManaged) LCSQuery.findObjectById(strBusOid);
            detailBusinessObject.setValue(SparcCostingConstants.SC_STATUS_KEY,SparcCostingConstants.SC_STATUS_COMPLETED);
            detailBusinessObject.setValue(SparcCostingConstants.END_DATE_KEY,setDateForBusinessObject());
            LCSLifecycleManagedLogic.persist(detailBusinessObject,true);
            documentLogic.associateDocuments(strBusOid,docIds);
            DeleteFileHelper.deleteFile(dataSheet);
            DeleteFileHelper.deleteFileArray(secondaryContentFiles);
            trx.commit();
            trx = null;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        finally{
            if(trx != null)
            {
                trx.rollback();
            }
        }
    }

    public static String initFolderForUnZipping(String timestamp){
        String folderPath = SparcFileUtil.getAbsoluteFileName(baseDir, "ZipInputFiles");
        folderPath = SparcFileUtil.getAbsoluteFileName(folderPath, timestamp);
        File workingDir = new File(folderPath);
        if (!workingDir.exists()) {
            LOGGER.info("Creating temporary folder for export:" + folderPath);
            workingDir.mkdirs();

        }
        return folderPath;
    }

    /**
     * This method deletes the files from the specific directory
     * @param workingDir
     */
    public static void deleteTheInPutFiles(String workingDir){
        File directory = new File(workingDir);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    /**
     * This method return the BOM ID and
     * @param strBOMLinkId
     * @return
     */
    public static String getID(String strBOMLinkId){
        int lastIndex = strBOMLinkId.lastIndexOf('.');

        if (lastIndex != -1) {
            strBOMLinkId  = strBOMLinkId.substring(0, lastIndex + 1);
            System.out.println("Substring: " + strBOMLinkId);
        } else {
            System.out.println("Period not found in the string.");
        }
        return strBOMLinkId;
    }

    public static String formattedDate(String srcDate) {
        String outputDate = "";
        if (srcDate == null || "".equals(srcDate)) {
            return "";
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MMM-yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy");
            Date date = inputFormat.parse(srcDate);
            outputDate = outputFormat.format(date);
        } catch (Exception e) {
            LOGGER.error("Encountered an exception while trying to parse the date, error:" + e.getMessage());
            if (LOGGER.isDebugEnabled())
                e.printStackTrace();
        }
        return outputDate;
    }


}
