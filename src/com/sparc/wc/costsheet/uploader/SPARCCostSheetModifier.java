package com.sparc.wc.costsheet.uploader;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flexbom.*;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.material.LCSMaterialMaster;
import com.lcs.wc.material.LCSMaterialQuery;
import com.lcs.wc.sourcing.*;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.util.VersionHelper;
import com.sparc.tc.exceptions.TCExceptionRuntime;

import com.sparc.tc.abstractions.CorrespondenceMaker;
import com.sparc.tc.abstractions.Interpreter;
import com.sparc.tc.domain.*;
import com.sparc.tc.domain.Table.Record;
import com.sparc.tc.impl.CorrespondenceMakerImpl;
import com.sparc.tc.impl.InterpreterImpl;
import com.sparc.tc.impl.PlaceholderParserImpl;
import com.sparc.wc.source.SparcProductCostSheetPlugin;
import com.sparc.wc.util.SparcCostingConstants;
import com.sparc.wc.util.SparcCostingUploadUtil;
import org.apache.logging.log4j.Logger;
import wt.fc.WTObject;
import wt.locks.LockHelper;
import wt.log4j.LogR;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import org.apache.poi.ss.util.CellReference;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class SPARCCostSheetModifier {

    private static final String BOM_LINK_ATTRIBUTES = LCSProperties.get("com.sparc.wc.upload.bomlink.attributes", "priceOverride,scCIF,quantity,lossAdjustment,scCalculationSizePercent");

    private static final String CS_ATTRIBUTES = LCSProperties.get("com.sparc.wc.upload.costsheet.attributes","scMilestoneStatus,scIncoterms,scFirstSaleMarkup,scVendorCostComments");


    private static final String NAME_ATTRIBUTES = LCSProperties.get("com.sparc.wc.docBoName.attributes","plmSeasonName,plmProductNumber,costSheetSelectedColorwayIds,scCostSheetNumber");
	
	private static final String FTW_OVERRIDE_ATTRIBUTES = LCSProperties.get("com.sparc.wc.ftw.costsheet.overrideattributes");
	
	private static final String APP_OVERRIDE_ATTRIBUTES = LCSProperties.get("com.sparc.wc.app.costsheet.overrideattributes");
	
	private static final String FTW_ADDITIONAL_ATTRIBUTES = LCSProperties.get("com.sparc.wc.ftw.costsheet.additionalattributes");
	
	private static final String APP_ADDITIONAL_ATTRIBUTES = LCSProperties.get("com.sparc.wc.app.costsheet.additionalattributes");
	
	private static Collection<String> ftwOverrideAttributes = FormatHelper.commaSeparatedListToCollection(FTW_OVERRIDE_ATTRIBUTES);
	
	private static Collection<String> appOverrideAttributes = FormatHelper.commaSeparatedListToCollection(APP_OVERRIDE_ATTRIBUTES);
	
	private static Collection<String> ftwAdditionalAttributes = FormatHelper.commaSeparatedListToCollection(FTW_ADDITIONAL_ATTRIBUTES);
	
	private static Collection<String> appAdditionalAttributes = FormatHelper.commaSeparatedListToCollection(APP_ADDITIONAL_ATTRIBUTES);


    private static Collection<String> bomLinkAttributes = FormatHelper.commaSeparatedListToCollection(BOM_LINK_ATTRIBUTES);

    private static Collection<String> costSheetAttributes = FormatHelper.commaSeparatedListToCollection(CS_ATTRIBUTES);

    private static Collection<String> nameAttributes = FormatHelper.commaSeparatedListToCollection(NAME_ATTRIBUTES);

    private static final Logger LOGGER = LogR.getLogger(SPARCCostSheetModifier.class.getName());

    private CorrespondenceData data;

    private CorrespondenceMakerContext context;

    private List<String> errorsAndWarnings;

    private String boName;

    private int countWarnings;
    private int countErrors;

    private List<String> summaryFiles;

    private static Map<String,String> sectionMap;
    private static final String sectionPrefix = "com.sparc.wc.bomlink.section.";
    private Map<String,Page.Element> pageElementsMap;
	
	private Collection<String> overrideAttributes = new ArrayList<>();
	private Collection<String> additionalAttributes = new ArrayList<>();

    static{
        sectionMap = LCSProperties.getPropertyEntriesStartWith("com.sparc.wc.bomlink.section.");
    }
    /**
     *
     * @param file
     * @param strDivision
     */
    public void processZipFile(String file, String strDivision,String businessObjectId,String seasonName) {
        LOGGER.info("Start of the Process File");
        String workingDir = SparcCostingUploadUtil.initFolderForUnZipping(System.currentTimeMillis() + "");
        String archivedFile;
        try {
            List<String> unzippedFiles = SparcCostingUploadUtil.unzipContentsTo(file, workingDir);
            this.summaryFiles = new LinkedList<>();
            unzippedFiles.stream().forEach(unzippedFile -> {
                upload(unzippedFile, strDivision,businessObjectId,true);
            });

            archivedFile = SparcCostingUploadUtil.archiveTheFiles(this.summaryFiles);
            SparcCostingUploadUtil.uploadDataAndSummaryFiles(file, archivedFile, businessObjectId, seasonName+"DETAILBREAKDOWN"+ClientContext.getContext().getUser().getName(),strDivision
            );
            SparcCostingUploadUtil.deleteTheInPutFiles(workingDir);
        } catch (Exception e) {
            LOGGER.error("Encountered an exception while processing the zip file, error:" + e.getMessage());
            if (LOGGER.isDebugEnabled())
                e.printStackTrace();
        }
        LOGGER.info("Start of the Process File");
    }



    /**
     * @param dataFileName
     * @param strDivision
     */
    public void upload(String dataFileName, String strDivision,String businessObjectId,Boolean isZipFile) {
        LOGGER.info("Start of the upload method");
        String templateFile = SparcCostingUploadUtil.getTemplateFile(strDivision);
        if (!FormatHelper.hasContent(templateFile)) {
            LOGGER.error("No template file found for Upload, please configure one");
            return;
        }
        final Interpreter interpreter = new InterpreterImpl(new PlaceholderParserImpl());
        final CorrespondenceMaker correspondenceMaker = new CorrespondenceMakerImpl(interpreter);
        this.context = new CorrespondenceMakerContext();
        this.boName = "";
        this.countErrors=0;
        this.countWarnings=0;
        this.pageElementsMap=null;
        String uploadSummaryFile = "";
        try {
            XSSFWorkbook templateWorkbook = new XSSFWorkbook(templateFile);
            XSSFWorkbook dataWorkbook = new XSSFWorkbook(dataFileName);
            final CorrespondenceData correspondenceData = correspondenceMaker.correspond(templateWorkbook, dataWorkbook, this.context,true);
            this.data = correspondenceData;
            this.setFirstMapData();
            this.boName= this.boAndDocumentName();
            this.updateCSAndBomLinks(strDivision);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if(isZipFile) {
            uploadSummaryFile = SparcCostingUploadUtil.writeErrorsAndWarningsToExcel(this.context.getCorrespondenceErrors(), this.context.getCorrespondenceWarnings(), this.boName, this.countErrors, this.countWarnings);
            this.summaryFiles.add(uploadSummaryFile);
        }
        else {
            uploadSummaryFile = SparcCostingUploadUtil.writeErrorsAndWarningsToExcel(this.context.getCorrespondenceErrors(), this.context.getCorrespondenceWarnings(), this.boName, this.countErrors, this.countWarnings);
            SparcCostingUploadUtil.uploadDataAndSummaryFiles(dataFileName, uploadSummaryFile, businessObjectId, this.boName,strDivision);
        }
        LOGGER.info("End of the upload method");
    }

    /**
     *
     */
    private void updateCSAndBomLinks(String strDivision) {
        LOGGER.info("Start of the method updateCSAndBomLinks");
        LCSProductCostSheet objProductCostSheet = null;
        Boolean isReqAttsHasValues = true;
        Boolean isRequired = true;
        try {
            LOGGER.info("Start of the method updateCSAndBomLinks");
            Variable costSheetVariableData =this.data.getPage().getVariable(SparcCostingConstants.COST_SHEET_NUMBER,(short)0);
            String strCostSheetNumber = costSheetVariableData.getSingleData();
            objProductCostSheet = this.getCostSheetObject(strCostSheetNumber)!=null? (LCSProductCostSheet) this.getCostSheetObject(strCostSheetNumber):null;
            if(FormatHelper.equalsWithNull(objProductCostSheet,null)){
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("No Product CostSheet exists with this CostSheetNumber "+strCostSheetNumber,
                        TCExceptionRuntime.Type.ERROR));
            }
            Variable mileStoneVarData =this.data.getPage().getVariable("scMilestoneStatus",(short)0);
            String strMileStoneVarData = mileStoneVarData.getSingleData();
            objProductCostSheet = (LCSProductCostSheet) VersionHelper.latestIterationOf((LCSCostSheetMaster)objProductCostSheet.getMaster());
            isReqAttsHasValues = this.checkForRequiredAttributes(objProductCostSheet);
            if(!isReqAttsHasValues) {
                this.countErrors++;
				this.context.addToCorrespondenceLog(new TCExceptionRuntime("BOM and CostSheet is not updated", TCExceptionRuntime.Type.ERROR));
				return;
            }
            if(!VersionHelper.isCheckedOut(objProductCostSheet)) {
                objProductCostSheet = VersionHelper.checkout(objProductCostSheet);
            }
            try{
                SparcProductCostSheetPlugin.checkforMileStoneMatch(objProductCostSheet,"UPDATE",strMileStoneVarData);
            }catch(LCSException ex){
                ex.printStackTrace();
                this.countErrors++;
                this.context.addToCorrespondenceLog(new TCExceptionRuntime(ex.getMessage() +"," + strCostSheetNumber, TCExceptionRuntime.Type.ERROR));
				this.context.addToCorrespondenceLog(new TCExceptionRuntime("BOM and CostSheet is not updated", TCExceptionRuntime.Type.ERROR));           
                isRequired = false;
				return;
            }
            String isBOMUpdated = updateBOMLinks();
            if(SparcCostingConstants.ERROR.equalsIgnoreCase(isBOMUpdated)){
                LOGGER.info("BOM is checked out by the user");
                isRequired = false;
				return;
            }
            objProductCostSheet = updateCostSheet(objProductCostSheet);
            objProductCostSheet = updateAdditionalBOMItems(objProductCostSheet,strDivision);
            objProductCostSheet = resetOverrideAttributesToZero(objProductCostSheet,strDivision);
            objProductCostSheet.setValue(SparcCostingConstants.RETRIEVE_BOM_KEY, true);
            LCSCostSheetLogic.deriveFlexTypeValues(objProductCostSheet,true);
            objProductCostSheet =(LCSProductCostSheet) LCSCostSheetLogic.persist(objProductCostSheet);
            this.context.addToCorrespondenceLog(new TCExceptionRuntime("Cost Sheet updated Successfully", TCExceptionRuntime.Type.WARNING));
            LOGGER.info("End of the method updateCSAndBomLinks");
        } catch (Exception ex) {
            ex.printStackTrace();
            this.context.addToCorrespondenceLog(new TCExceptionRuntime("Error while updating the costsheet" +ex.getMessage(),
                    TCExceptionRuntime.Type.ERROR));
        }
        finally {
            try {
                if(VersionHelper.isCheckedOut(objProductCostSheet)){
                    VersionHelper.checkin(objProductCostSheet);
                }
            } catch (WTException e) {
               LOGGER.error("Error while checking in the object-----"+e.getMessage());
            }
        }
    }
	
	
	/**
	*  This method reset the attributes to Zero
	*/
	private LCSProductCostSheet resetOverrideAttributesToZero(LCSProductCostSheet objProductCostSheet,String strDivision){
		overrideAttributes = "Apparel".equalsIgnoreCase(strDivision) ? appOverrideAttributes : ftwOverrideAttributes;		
		overrideAttributes.forEach(strAttribute -> {
            try {
                objProductCostSheet.setValue(strAttribute, 0.00);
            } catch (Exception e) {
                LOGGER.error("Error while checking in the object-----"+e.getMessage());
            }
        });		
		return objProductCostSheet;		
	}	

    /**
     *
     * @param strCostSheetNumber
     * @return
     */
    public LCSCostSheet getCostSheetObject(String strCostSheetNumber){
        LOGGER.info("Start of the method getCostSheetObject CostSheetNumber "+strCostSheetNumber);
        SearchResults costObjResults = SparcCostingUploadUtil.getCostSheetObjectResult(strCostSheetNumber);
        LCSCostSheet objProductCostSheet = null;
        if(costObjResults != null){
            objProductCostSheet =SparcCostingUploadUtil.getCostSheetObject(costObjResults);
        }
        LOGGER.info("End of the method getCostSheetObject CostSheetNumber "+strCostSheetNumber);
        return objProductCostSheet;
    }


    /**
     * This method updates the BOM Link objects
     */
    private String updateBOMLinks() {
        LOGGER.info("Start of the method updateBOMLinks");
        List<Record> bomLinkRecords = getBomLinkRecords();
        if (bomLinkRecords.isEmpty()) {
            this.countWarnings++;
            this.context.addToCorrespondenceLog(new TCExceptionRuntime("There are no records in the input file",TCExceptionRuntime.Type.WARNING));
            return SparcCostingConstants.WARNING;
        }
        List<FlexBOMLink> bomLinks = new ArrayList<>();
        Map<String, Record> bomRecordsMap = this.formBomLinkRecordsOnBOMId(bomLinkRecords);
        LOGGER.debug("BOM Records Map----------------"+bomRecordsMap);
		Boolean isBOMLinkLatest = true;
        for (Map.Entry<String, Table.Record> bomRecordMap : bomRecordsMap.entrySet()) {
            String bomLinkId = bomRecordMap.getKey();
            if (!FormatHelper.hasContent(bomLinkId)) {
                this.countWarnings++;
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("This is not ID present in the input file",
                        TCExceptionRuntime.Type.WARNING));
                continue;
            }
            bomLinkId = SparcCostingUploadUtil.getID(bomLinkId);
            bomLinkId = SparcCostingConstants.OR_BOM_LINK + bomLinkId;
            try {
                FlexBOMLink flexBOMLink = (FlexBOMLink) LCSQuery.findObjectById(bomLinkId);
                if (flexBOMLink.getOutDate() != null) {
                    this.countErrors++;
                    this.context.addToCorrespondenceLog(new TCExceptionRuntime("This is not the latest data so Bom records not updated, so please download the latest data and " +
                            "modify the data and upload it back",
                            TCExceptionRuntime.Type.ERROR));
		    isBOMLinkLatest = false;		
                    break;
                }
                if (!FormatHelper.equalsWithNull(flexBOMLink, null)) {
                    setBOMLinkAttributes(flexBOMLink, bomRecordMap.getValue());
                }
                else{
                    this.context.addToCorrespondenceLog(new TCExceptionRuntime("there is NO BOM Link Object available for this ID",
                            TCExceptionRuntime.Type.WARNING));
                    continue;
                }
               bomLinks.add(flexBOMLink);
            } catch (WTException ex) {
                ex.printStackTrace();
            }
        }
		
        LOGGER.info("bomLinks-----------------"+bomLinks);
		if(!isBOMLinkLatest)
		{
			 return SparcCostingConstants.ERROR;
        }		
		LOGGER.info("End of the method updateBOMLinks");		
        return saveFlexBomLinks(bomLinks);
    }

    /**
     * This method updates the FlexBOMLink object     *
     * @param flexBOMLinks
     */
    private String saveFlexBomLinks(List<FlexBOMLink> flexBOMLinks)  {
        LCSFlexBOMLogic flexBOMLogic = new LCSFlexBOMLogic();
        FlexBOMPart bomPart = null;
        FlexBOMLink bomLink = null;
        FlexBOMPartMaster bomPartMaster = null;
		Boolean isCheck = false;
        try {
            if (!flexBOMLinks.isEmpty()) {
                bomLink = flexBOMLinks.get(0);
                bomPartMaster = (FlexBOMPartMaster) bomLink.getParent();
                bomPart = (FlexBOMPart) VersionHelper.latestIterationOf(bomPartMaster);
                Boolean isCheckedOut = VersionHelper.isCheckedOut(bomPart);
                Boolean isWorkingCopy = VersionHelper.isWorkingCopy(bomPart);
                Boolean isCheckedOutByUser = VersionHelper.isCheckedOutByUser(bomPart);
                Boolean isLocked = LockHelper.isLocked(bomPart);
                Boolean editable = ((!isCheckedOut && !isLocked )|| isCheckedOutByUser);                

                if(!editable){
					this.countErrors++;                   
                    WTUser user = (WTUser) LockHelper.getLocker(bomPart);
					this.context.addToCorrespondenceLog(new TCExceptionRuntime("The Upload failed for Costsheet and BOM as the BOM is checked out by the user "+user.getFullName(),
                            TCExceptionRuntime.Type.ERROR));
					isCheck = true;		
                    return SparcCostingConstants.ERROR;
                }
				if(isCheckedOut && isCheckedOutByUser && isWorkingCopy)
				{                    
					this.countErrors++;                   
                    this.context.addToCorrespondenceLog(new TCExceptionRuntime("The Upload failed for Costsheet and BOM as the BOM is checked out by the user "+ClientContext.getContext().getUser().getName(),
                            TCExceptionRuntime.Type.ERROR));
					isCheck = true;			
                    return SparcCostingConstants.ERROR;
				}
				else{
                bomPart = (FlexBOMPart) VersionHelper.checkout(bomPart);
				}
            }
            flexBOMLinks.stream()
                    .filter(flexBOMLink -> !FormatHelper.equalsWithNull(flexBOMLink.getChild(), null))
                    .forEach(flexBOMLink -> {
                        String masterId = "";
                        LCSMaterialMaster materialMaster = null;
                        try {
                            materialMaster = flexBOMLink.getChild();
                            masterId = FormatHelper.getObjectId(materialMaster);
                            if (masterId.equals(LCSMaterialQuery.PLACEHOLDERID)) {
                                String materialName = (String) flexBOMLink.getValue("materialDescription");
                                System.out.println("materialName-------------------"+materialName);
                                if (!FormatHelper.hasContent(materialName)) {
                                    return; // Skip to the next iteration
                                }
                            }
                            System.out.println("saving the object");
                            flexBOMLogic.deriveFlexTypeValues(flexBOMLink, true);
                            flexBOMLink = (FlexBOMLink) flexBOMLogic.persist(flexBOMLink);
                            LOGGER.info("Saved FlexBOMLink with OID:" + FormatHelper.getObjectId(flexBOMLink));
                        } catch (WTException e) {
                            LOGGER.error("Encountered an error saving FlexBOMLink, error:" + e.getMessage());
                            if (LOGGER.isDebugEnabled()) {
                                e.printStackTrace();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });


            if(!FormatHelper.equalsWithNull(bomPart,null)){
             flexBOMLogic.persist(bomPart,true);
          }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
            try {
			    if (!FormatHelper.equalsWithNull(bomPart, null) && VersionHelper.isCheckedOut(bomPart) && !isCheck) {
				    VersionHelper.checkin(bomPart);
                }
            }catch(WTException ex){
                LOGGER.error("Error while checking the object   "+ex.getMessage());
            }
        }
        this.context.addToCorrespondenceLog(new TCExceptionRuntime("BOM Link updated successfully", TCExceptionRuntime.Type.WARNING));
        return SparcCostingConstants.SUCCESS;
    }



    /**
     * This method get the BOM Link records.
     * @return
     */
    private List<Record> getBomLinkRecords(){
        LOGGER.info("Start of the method getBomLinkRecords");
        List<Record> filteredRecords = new ArrayList<>();
        try {
			 filteredRecords = this.data.getGroupedPage().getTable((short) 0).getRecords().stream()
                    .filter(dataRecord ->
                            dataRecord.getElements().containsKey("bomLinkId")
                    ).collect(Collectors.toList());
           LOGGER.debug("filteredRecords--------------------------"+filteredRecords);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        LOGGER.info("End of the method getBomLinkRecords");
        return filteredRecords;
    }

    /**
     * This method sets the BOM link attributes for the FlexBOM Link Object
     * @param flexBOMLink
     * @param bomLinkRecord
     * @return
     */
    private FlexBOMLink setBOMLinkAttributes(FlexBOMLink flexBOMLink, Record bomLinkRecord) {
        LOGGER.info("Start of the method setBOMLinkAttributes");
        Map<String, Record.Element> elementsMap = bomLinkRecord.getElements();
        String strKey = "";
        for (Map.Entry<String, Record.Element> elementMap : elementsMap.entrySet()) {
            strKey = elementMap.getKey();
            try {
                if(SparcCostingConstants.BOM_LINK_ID.equalsIgnoreCase(strKey)|| SparcCostingConstants.SECTION_KEY.equalsIgnoreCase(strKey)){
                    continue;
                }
                flexBOMLink.setValue(strKey, elementMap.getValue().getData());
            } catch (Exception ex) {
                ex.printStackTrace();
                this.countWarnings++;
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Encountered an error while reading value of field:" +
                        strKey + "and coordinates are Row Position "+ (elementMap.getValue().getCoordinate().getRow() + 1) +
               " and column position "+ CellReference.convertNumToColString(elementMap.getValue().getCoordinate().getColumn()) +", error:" + ex.getMessage(), TCExceptionRuntime.Type.ERROR));
            }
        }
        LOGGER.info("End of the method setBOMLinkAttributes");
        return flexBOMLink;
    }


    /**
     * @param bomLinkRecords
     * @return
     */
    private Map<String, Table.Record> formBomLinkRecordsOnBOMId(List<Record> bomLinkRecords) {
        LOGGER.info("Start of the method formBomLinkRecordsOnBOMId");
        Map<String, Record> bomLinkMap = new HashMap<>();
        for (Record bomLinkRecord : bomLinkRecords) {
            Map<String, Record.Element> recordElements = bomLinkRecord.getElements();
            for (Map.Entry<String, Record.Element> recordElement : recordElements.entrySet()) {
                if (SparcCostingConstants.BOM_LINK_ID.equalsIgnoreCase(recordElement.getKey())) {
                    if (FormatHelper.hasContent(recordElement.getValue().getData())) {
                        bomLinkMap.put(SparcCostingUploadUtil.getLongNumber(recordElement.getValue().getData()), bomLinkRecord);
                    }
                }
            }
        }
        LOGGER.info("End of the method formBomLinkRecordsOnBOMId");
        return bomLinkMap;
    }


    /**
     * This method update the CostSheet object
     */
    private LCSProductCostSheet updateCostSheet(LCSProductCostSheet csModel) {
        LOGGER.info("Start of the method updateCostSheet");
        String key = "";
        String data = "";
        Page.Element value = null;
        Map<String, Page.Element> map = this.pageElementsMap;
        nameAttributes.forEach(map::remove);
        for (Map.Entry<String, Page.Element> entry : map.entrySet()) {
            try {
                key = entry.getKey();
                value = entry.getValue();
                data = value.getData();
                if (SparcCostingUploadUtil.isListType(csModel, key) && data != null) {
                    LOGGER.debug(" is List type");
                    data = SparcCostingUploadUtil.getInternalNameForEnum(csModel.getFlexType(), key, data);
                }
                else if(SparcCostingUploadUtil.isDateType(csModel,key) && data != null){
                    LOGGER.debug(" is Date  type");
                    data = SparcCostingUploadUtil.formattedDate(data);
                }
                else if(SparcCostingConstants.OTHER_PROCESS_KEY.equalsIgnoreCase(key) && data != null){
                   if( ((String)data).startsWith(SparcCostingConstants.SEMICOLON_DELIMITER)){
                            csModel.setValue(key,((String)data).substring(1).trim());
                    }
                }
                else if(SparcCostingUploadUtil.isReference(csModel,key) && data != null){
                    WTObject obj = SparcCostingUploadUtil.getObjectData(csModel,key,data);
                    if(obj!=null){
                        csModel.setValue(key,obj);
                    }
                    continue;
                }
                else if(SparcCostingUploadUtil.isNumberType(csModel,key) && data != null && SparcCostingUploadUtil.containsLetterOrSpecialChar(data)){
                    this.countWarnings++;
                    this.context.addToCorrespondenceLog(new TCExceptionRuntime("Invalid data error for the key "+key+ " " +
                            "and coordinates are row position "+(value.getCoordinate().getRow()+ 1 ) + " and Column position is "
                            +CellReference.convertNumToColString(value.getCoordinate().getColumn())+" value is "+data, TCExceptionRuntime.Type.WARNING));
                    continue;
                }
                else if(SparcCostingUploadUtil.isTextType(csModel,key) && data != null){
                  if(data.length() > csModel.getFlexType().getAttribute(key).getAttUpperLimit()){
                        this.countWarnings++;
						this.context.addToCorrespondenceLog(new TCExceptionRuntime("input data provided exceeded the upper limit for the attribute "+key+ " " +
                            "and coordinates are row position "+(value.getCoordinate().getRow() + 1) + " and Column position is "
                            +CellReference.convertNumToColString(value.getCoordinate().getColumn())+" value is "+data, TCExceptionRuntime.Type.WARNING));
                    continue;						
					}
                }

                csModel.setValue(key, data);

            } catch(NumberFormatException ex){
                this.countWarnings++;
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Invalid data error for the key "+key+ " " +
                        "and coordinates are row position "+(value.getCoordinate().getRow() + 1)+ " and Column position is "
                        +CellReference.convertNumToColString(value.getCoordinate().getColumn())+" and error message is"+ex.getMessage(), TCExceptionRuntime.Type.ERROR));
            }
            catch (Exception ex) {
                this.countWarnings++;
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Invalid data error for the key "+key+ " " +
                        "and coordinates are row position "+(value.getCoordinate().getRow() + 1)+ " and Column position is "
                        +CellReference.convertNumToColString(value.getCoordinate().getColumn())+" and error message is "+ex.getMessage(), TCExceptionRuntime.Type.ERROR));

            }
        }
        LOGGER.info("End of the method updateCostSheet");
        return csModel;
    }

    private void setFirstMapData(){
        Collection<Map<String, Page.Element>> pageData = this.data.getPage().getVariableMap().values();
        Map<String, Page.Element> firstMap = new HashMap<>();
        if(!pageData.isEmpty()){
            firstMap = pageData.stream().findFirst().orElse(null);
        }
        this.pageElementsMap = firstMap;
    }


    private boolean checkForRequiredAttributes(LCSProductCostSheet csModel){
        List<String> requiredAttributes = SparcCostingUploadUtil.getRequiredAttributes(csModel);
        String strValue = "";
        Boolean isRequiredHasValue = true;
        for(String strAttKey : requiredAttributes){
            if(this.pageElementsMap.containsKey(strAttKey)){
                strValue = this.pageElementsMap.get(strAttKey).getData();
                if(!FormatHelper.hasContent(strValue)){
                    this.countErrors++;
                    this.context.addToCorrespondenceLog(new TCExceptionRuntime("Required Field" +
                            strAttKey + "and coordinates are Row Position"+ (this.pageElementsMap.get(strAttKey).getCoordinate().getRow() + 1) +
                            "and column position"+ CellReference.convertNumToColString(this.pageElementsMap.get(strAttKey).getCoordinate().getColumn())+", error:Missing the data", TCExceptionRuntime.Type.ERROR));
                    isRequiredHasValue = false;
                }
            }
        }
        return isRequiredHasValue;
    }

    /**
     *
     * @return
     */
    private String boAndDocumentName(){
        StringBuffer sb = new StringBuffer();
        for(String strAttribute : nameAttributes){
            sb.append(this.pageElementsMap.get(strAttribute).getData());
            sb.append("_");
        }
        return sb.toString();
    }

    /**
     *
     */
    private LCSProductCostSheet updateAdditionalBOMItems(LCSProductCostSheet csModel,String strDivision) {
		
		Map<String, Page.Element> map = this.pageElementsMap;
	    LOGGER.debug("map--------------------------"+map);
	
		additionalAttributes = "Apparel".equalsIgnoreCase(strDivision) ? appAdditionalAttributes : ftwAdditionalAttributes;
		LOGGER.debug("additionalAttributes--------------------------"+additionalAttributes);
		Page.Element value = null;
		String data = "";
		for(String additionalAttribute : additionalAttributes){
			try{
				value = map.get(additionalAttribute);
				data = value.getData();
				csModel.setValue(additionalAttribute, data);
			}catch(Exception ex){
				//System.out.println("updateAdditionalBOMItems------------"+ex.getMessage());
	            LOGGER.error("Error while setting the data------"+ex.getMessage());
			}
		}
       return csModel;
    }


    /**
     * This method set the Cost Sheet additional attribute values from the excel
     * and if there is no inputs for the additional sectional attributes in the excel then corresponding costsheet attributes are reset to zero.
     * @param additionalSectionMap
     * @param csModel
     * @return
     */
    private LCSProductCostSheet setValuesForCSAdditionalAttributes(Map<String, Double> additionalSectionMap, LCSProductCostSheet csModel,String strDivision) {
        LOGGER.info("Start of the method setValuesForCSAdditionalAttributes");
        String enumKey = "";
        FlexType bomFlexType = null;
        String strKey = "";
        bomFlexType= SparcCostingUploadUtil.getBOMFlexType(strDivision);
        List<String> enumKeys = new ArrayList<>();
        for (Map.Entry<String, Double> additionalSectionEntry : additionalSectionMap.entrySet()) {
            try {
                enumKey = SparcCostingUploadUtil.getInternalNameForEnum(bomFlexType,SparcCostingConstants.SECTION_KEY, additionalSectionEntry.getKey());
                strKey = sectionMap.get(sectionPrefix + enumKey);
                if(FormatHelper.hasContent(strKey)) {
                    enumKeys.add(enumKey);
                    csModel.setValue(strKey, additionalSectionEntry.getValue());
                }
            }catch (WTException ex) {
                LOGGER.error("Error while setting the attribute value of CostSheet "+ex.getMessage());
            } catch (WTPropertyVetoException e) {
                LOGGER.error("Error while setting the attribute value of CostSheet "+e.getMessage());
            }
        }

        //Start of the code where we need to reset the Cost Sheet attributes to Zero
        Collection<String> sectionEnumKeys = SparcCostingUploadUtil.getEnumKeys(bomFlexType, SparcCostingConstants.SECTION_KEY);
		System.out.println("sectionEnumKeys--------------------"+sectionEnumKeys);
        sectionEnumKeys.stream()
                .filter(sectionEnumKey -> enumKeys.isEmpty() || !enumKeys.contains(sectionEnumKey))
                .forEach(sectionEnumKey -> {
                    try {
                        csModel.setValue(sectionMap.get(sectionPrefix + sectionEnumKey), 0.00);
                    } catch (WTException | WTPropertyVetoException ex) {
                        LOGGER.error("Error while resetting the Additional Attribute to zero: " + ex.getMessage());
                    }
                });

        //End of the code where we need to reset the Cost Sheet attributes to Zero


        LOGGER.info("End of the method setValuesForCSAdditionalAttributes");
        return csModel;
    }

    /**
     * @param additionalItems
     * @return
     */
    private Map<String, Double> calculateAdditionalSectionTotal(List<Table.Record> additionalItems) {
        LOGGER.info("Start of the method calculateAdditionalSectionTotal");
        Map<String, Double> additionalItemsMap = new HashMap<>();
        for (Record bomRecord : additionalItems) {
            Map<String, Record.Element> recordElement = bomRecord.getElements();
            if (!recordElement.containsKey(SparcCostingConstants.SECTION_KEY)) {
                LOGGER.info("Section key not found in the record, skipping this record");
                continue;
            }
            for (Map.Entry<String, Record.Element> recordEntry : recordElement.entrySet()) {
                String sectionName = recordEntry.getValue().getData();
                try {
                    if(!FormatHelper.hasContent(sectionName)){
                        System.out.println("Section Name is blank");
                        continue;
                    }
                    String quantityData = bomRecord.getElements().get("totalMatCost").getData();
                    double quantity = Double.valueOf(quantityData);
                    if (additionalItemsMap.containsKey(sectionName)) {
                        additionalItemsMap.put(sectionName, additionalItemsMap.get(sectionName) + quantity);
                    } else {
                        additionalItemsMap.put(sectionName, quantity);
                    }
                } catch (NumberFormatException e) {
                    this.countErrors++;
                    this.context.addToCorrespondenceLog(new TCExceptionRuntime("Invalid data error for the keyTotal Mat Cost" +e.getMessage(), TCExceptionRuntime.Type.ERROR));
                }catch(Exception ex){
                    this.countErrors++;
                    this.context.addToCorrespondenceLog(new TCExceptionRuntime("Issue with totalMatCost " +ex.getMessage(), TCExceptionRuntime.Type.ERROR));
                }
            }
        }
        LOGGER.info("End of the method calculateAdditionalSectionTotal");
        return additionalItemsMap;
    }
}
