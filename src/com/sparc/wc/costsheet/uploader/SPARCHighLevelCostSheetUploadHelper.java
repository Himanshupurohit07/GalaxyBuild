package com.sparc.wc.costsheet.uploader;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.lcs.wc.sourcing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.document.LCSDocumentClientModel;
import com.lcs.wc.document.LCSDocumentLogic;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSLifecycleManagedLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.material.LCSMaterialSupplierQuery;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.DeleteFileHelper;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import com.sparc.tc.abstractions.CorrespondenceMaker;
import com.sparc.tc.abstractions.Interpreter;
import com.sparc.tc.domain.CorrespondenceData;
import com.sparc.tc.domain.CorrespondenceMakerContext;
import com.sparc.tc.domain.Table;
import com.sparc.tc.domain.Variable;
import com.sparc.tc.exceptions.TCExceptionRuntime;
import com.sparc.tc.impl.CorrespondenceMakerImpl;
import com.sparc.tc.impl.InterpreterImpl;
import com.sparc.tc.impl.PlaceholderParserImpl;
import com.sparc.wc.util.SparcCostingConstants;
import com.sparc.wc.util.SparcCostingUploadUtil;
import com.lcs.wc.util.FormatHelper;
import wt.util.WTProperties;


import wt.fc.WTObject;
import wt.pom.Transaction;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import static com.sparc.wc.util.SparcCostingConstants.*;


public class SPARCHighLevelCostSheetUploadHelper {

    private CorrespondenceMakerContext context;
    private CorrespondenceData data;
    private int countErrors;
    private int countWarnings;

    private static final Logger LOGGER = LogManager.getLogger(SPARCHighLevelCostSheetUploadHelper.class.getName());

    private static final String CS_HIGHLEVEL_DOCUMENT_TYPE_PREFX = "com.sparc.wc.costsheet.uploader.SPARCHighLevelCostSheetUploadHelper.docType.";

    private static final String CS_HIGHLEVEL_SPECNAME_IDENTIFIER = "FLEXSPECIFICATION.PTC_STR_2TYPEINFOFLEXSPECIFI";

    private static final String CS_HIGHLEVEL_SPEC_IDA2A2 = "FLEXSPECIFICATION.IDA2A2";

    private static final String CS_HIGHLEVEL_BO_IDA2A2 = "LCSLIFECYCLEMANAGED.IDA2A2";

    private static final String CS_HIGHLEVEL_BO_OBJECT_IDENTIFIER_LITERAL = "thePersistInfo.theObjectIdentifier.id";

    private static final String CS_HIGHLEVEL_SPEC_OBJECTID_PREFIX = "OR:com.lcs.wc.specification.FlexSpecification:";

    private static final String CS_HIGHLEVEL_SKUNAME_ATTR = "skuName";

    private static final String CS_HIGHLEVEL_PRODUCT_NAME_IDENTIFIER = "productName";

    private static final String CS_HIGHLEVEL_SEASON_NAME_IDENTIFIER = "seasonName";

    private static final String CS_HIGHLEVEL_COSTSHEET_TYPE_IDENTIFIER = "costSheetType";

    private static final String CS_HIGHLEVEL_PLMSOURCE_NAME_IDENTIFIER = "plmSourceName";

    private static final String CS_HIGHLEVEL_SPEC_NAME_IDENTIFIER = "specName";

    private static final String CS_HIGHLEVEL_SELECTED_COLORWAYS_IDENTIFIER = "costSheetSelectedColorwayNames";

    private static final String CS_HIGHLEVEL_REPRESENTATIVE_COLORWAY_IDENTIFIER = "representativeColorwayName";

    private static final String CS_HIGHLEVEL_MOA_DELIMITER = "|~*~|";

    private static final String CS_HIGHLEVEL_SPARC_COSTSHEET_TYPE = "Cost Sheet\\scSparc";

    private static final String CS_HIGHLEVEL_DEFAULT_DOC_TYPE = "Document\\scApparelHighLevel";

    private static final String CS_HIGHLEVEL_PRIMARY_CONTENT_COMMENT = "Uploaded By Admin";

    private static final String CS_HIGHLEVEL_SECONDARY_CONTENT_COMMENT = "Upload Summary Log by Admin";


    private static String csHighLevelEditableAttributes = LCSProperties.get("com.sparc.wc.costsheet.uploader.SPARCHighLevelCostSheetUploadHelper.csHighLevelEditableAttributes", "name,scMilestoneStatus,scVendorCostSheetStatus,scIncoterms,scIncotermlocation,scDestination,scAdditionalBuy,scQuoteGoodUntil,scQuoteUnits,scMainMaterialUnitPrice,scMainMaterialYield,scVendorCost,scHangerCost,scFirstSaleMarkup,scStyleMOQ,scColorMOQ,scMOQComments,scLeadTimePOtoShip,scVendorCostComments");
    @SuppressWarnings("unchecked")
    private static Collection<String> csHighLevelEditableAttCol = FormatHelper.commaSeparatedListToCollection(csHighLevelEditableAttributes);

    private static String csHighLevelCreatabeAttributes = LCSProperties.get("com.sparc.wc.costsheet.uploader.SPARCHighLevelCostSheetUploadHelper.csHighLevelCreateableAttributes", "name,scMilestoneStatus,scVendorCostSheetStatus,scIncoterms,scIncotermlocation,scDestination,scAdditionalBuy,scQuoteGoodUntil,scQuoteUnits,scMainMaterialUnitPrice,scMainMaterialYield,scVendorCost,scHangerCost,scFirstSaleMarkup,scStyleMOQ,scColorMOQ,scMOQComments,scLeadTimePOtoShip,scVendorCostComments");
    @SuppressWarnings("unchecked")
    private static Collection<String> csHighLevelCreateableAttCol = FormatHelper.commaSeparatedListToCollection(csHighLevelCreatabeAttributes);

    private static String csHighLevelAttrKeyValuePair = LCSProperties.get("com.sparc.wc.costsheet.uploader.SPARCHighLevelCostSheetUploadHelper.csHighLevelAttrKeyValuePair", "name~Cost Sheet Name,scMilestoneStatus~Cost Sheet Milestone,scVendorCostSheetStatus~Vendor Cost Sheet Status,scIncoterms~Incoterms,scIncotermlocation~Incoterms Location,scDestination~Destination,scAdditionalBuy~Additional Buy,scQuoteGoodUntil~Quote Good Until,scQuoteUnits~Quote Units,scMainMaterialUnitPrice~Main Material Unit Price,scMainMaterialYield~Main Material Yield,scVendorCost~Vendor Product Cost Override,scHangerCost~Hanger Cost,scFirstSaleMarkup~First Sale Markup,scStyleMOQ~Style MOQ,scColorMOQ~Color MOQ,scMOQComments~MOQ Comments,scLeadTimePOtoShip~Lead Time (PO to Ship),scVendorCostComments~Vendor Cost Comments");

    private static Collection<String> csHighLevelAttrKeyValueMap = FormatHelper.commaSeparatedListToCollection(csHighLevelAttrKeyValuePair);


    private static String csHighLevelRequiredAttributes = LCSProperties.get("com.sparc.wc.costsheet.uploader.SPARCHighLevelCostSheetUploadHelper.csHighLevelRequiredAttributes", "Incoterms~scIncoterms,");
    @SuppressWarnings("unchecked")
    private static Collection<String> csHighLevelRequiredAttCol = FormatHelper.commaSeparatedListToCollection(csHighLevelRequiredAttributes);

    private static String csHighLevelObjectRefAttributes = LCSProperties.get("com.sparc.wc.costsheet.uploader.SPARCHighLevelCostSheetUploadHelper.csHighLevelObjectRefAttributes", "scIncotermlocation,");
    @SuppressWarnings("unchecked")
    private static Collection<String> csHighLevelObjectRefAttributesCol = FormatHelper.commaSeparatedListToCollection(csHighLevelObjectRefAttributes);

    private static String csHighLevelMandatortAttsforCreate = LCSProperties.get("com.sparc.wc.costsheet.uploader.SPARCHighLevelCostSheetUploadHelper.csHighLevelMandatortAttsforCreate", "productName,costSheetType,seasonName,plmSourceName,specName,costSheetSelectedColorwayNames,representativeColorwayName,scIncoterms");
    @SuppressWarnings("unchecked")
    private static Collection<String> csHighLevelMandatortAttsforCreateCol = FormatHelper.commaSeparatedListToCollection(csHighLevelMandatortAttsforCreate);

    private static String costSheetTypeInternalDisplayNameMapping = LCSProperties.get("com.sparc.wc.costsheet.uploader.SPARCHighLevelCostSheetUploadHelper.costSheetTypeInternalDisplayNameMapping", "Cost Sheet Product\\Sparc\\High Level\\Aeropostale~Cost Sheet\\scSparc\\scApparelHighLevel\\scApparelHighLevelAeropostale,Cost Sheet Product\\Sparc\\High Level\\Forever 21~Cost Sheet\\scSparc\\scApparelHighLevel\\scApparelHighLevelForever21,Cost Sheet Product\\Sparc\\High Level\\Lucky~Cost Sheet\\scSparc\\scApparelHighLevel\\scApparelHighLevelLucky,Cost Sheet Product\\Sparc\\High Level\\Reebok~Cost Sheet\\scSparc\\scApparelHighLevel\\scApparelHighLevelReebok");
    @SuppressWarnings("unchecked")
    private static Collection<String> costSheetTypeInternalDisplayNameMappingCol = FormatHelper.commaSeparatedListToCollection(costSheetTypeInternalDisplayNameMapping);

    private static String costSheetTypeDisplayNames = LCSProperties.get("com.sparc.wc.costsheet.uploader.SPARCHighLevelCostSheetUploadHelper.costSheetTypeDisplayNames", "Cost Sheet Product\\Sparc\\High Level\\Aeropostale,Cost Sheet Product\\Sparc\\High Level\\Forever 21,Cost Sheet Product\\Sparc\\High Level\\Lucky,Cost Sheet Product\\Sparc\\High Level\\Reebok");
    @SuppressWarnings("unchecked")
    private static Collection<String> costSheetTypeDisplayNamesCol = FormatHelper.commaSeparatedListToCollection(costSheetTypeDisplayNames);

    public static String wt_home = "";

    static {
        try {
            WTProperties wtproperties = WTProperties.getLocalProperties();
            wt_home = wtproperties.getProperty("wt.home");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is to Helper method to perform the upload
     *
     * @param templateFile
     * @param dataFileName
     * @param businessObjectId
     * @param isZipFile
     * @param strDivision
     * @param strBO_Name
     * @return
     */
    public String performUpload(String templateFile, String dataFileName, String businessObjectId, boolean isZipFile, String strDivision, String strBO_Name) {
        String summaryFileName = "";
        LOGGER.info("**performUpload - start:" + templateFile + ":" + dataFileName + ":" + businessObjectId + ":" + String.valueOf(isZipFile) + ":" + strBO_Name);
        Interpreter interpreter = new InterpreterImpl(new PlaceholderParserImpl());
        CorrespondenceMaker correspondenceMaker = new CorrespondenceMakerImpl(interpreter);
        this.context = new CorrespondenceMakerContext();
        String docFlexTypePath = LCSProperties.get(CS_HIGHLEVEL_DOCUMENT_TYPE_PREFX + strDivision, CS_HIGHLEVEL_DEFAULT_DOC_TYPE);
        LCSProductCostSheet csObject = null;
        String mode = "";
        Iterator<String> stringIterator = null;
        String[] pair;
        String costSheetNumber = "";
        XSSFWorkbook templateWorkbook = null;
        XSSFWorkbook dataWorkbook = null;
        try {
            boolean addSelectedColorwaysOnUpdate = false;
            boolean addRepSKUOnUpdate = false;
            templateWorkbook = new XSSFWorkbook(templateFile);
            dataWorkbook = new XSSFWorkbook(dataFileName);
            final CorrespondenceData correspondenceData = correspondenceMaker.correspond(templateWorkbook, dataWorkbook, this.context, true);
            this.data = correspondenceData;
            LOGGER.debug("***correspondenceData:" + correspondenceData);
            LOGGER.debug("***correspondenceData1.getGroupedPage:" + this.data.getGroupedPage());
            LOGGER.debug("***correspondenceData:3" + this.data.getGroupedPage().getTable((short) 0));
            Map<Integer, Table.Record> rowRecordMap = this.data.getGroupedPage().getTable((short) 0).getRecordMap();
            Set<Integer> rowSet = rowRecordMap.keySet();
			boolean isUpdated=false;
			boolean isProdContext = false;

            rowProcessing:
            for (Integer rowNum : rowSet) {
                LCSSKU objSKU = null;
                Table.Record record = rowRecordMap.get(rowNum);
                rowNum++;
                LOGGER.debug("record elements details:" + record.getElements().size() + ":" + record.getElements());
				isUpdated=false;
				addSelectedColorwaysOnUpdate = false;
				addRepSKUOnUpdate = false;
                //mode = String.valueOf(record.getVariable("mode").getData());
                if (record.getVariable("mode") != null) {
                    LOGGER.debug("***mode:" + mode);
                    mode = String.valueOf(record.getVariable("mode").getData());
                    if (!FormatHelper.hasContent(mode)) {
                        LOGGER.debug("Mode data is blank so skip processing this");
                        continue;
                    }
                    //update logic start
                    if (UPLOAD_UPDATE.equalsIgnoreCase(mode)) {
                        LOGGER.debug("**Update Started:");
                     	 for(String strReqAtt : csHighLevelRequiredAttCol){
                            pair = strReqAtt.split("~");
                            if (!(record.getVariable(pair[1]) != null && record.getVariable(pair[1]).getData() != null && FormatHelper.hasContent(String.valueOf(record.getVariable(pair[1]).getData())))) {
                                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Skipping the Update of Cost Sheet since the Value is not filled in for Required attribute " + pair[0] + " at Row:" + rowNum, TCExceptionRuntime.Type.ERROR));
                                LOGGER.debug("Skipping the Update of Cost Sheet since the Value is not filled in for Required attribute " + pair[0] + " at Row:" + rowNum);
                                this.countErrors++;
                                continue rowProcessing;
                            }
                        }

                        if (!(record.getVariable(SparcCostingConstants.COST_SHEET_NUMBER) != null && record.getVariable(SparcCostingConstants.COST_SHEET_NUMBER).getData() != null)) {
                            this.context.addToCorrespondenceLog(new TCExceptionRuntime("Value for Cost Sheet Number is invalid and hence can't proceed with upload at Row:" + record.getVariable(SparcCostingConstants.COST_SHEET_NUMBER).getCoordinate().getRow() + ", Column:" + record.getVariable(SparcCostingConstants.COST_SHEET_NUMBER).getCoordinate().getRow(), TCExceptionRuntime.Type.ERROR));
                            this.countErrors++;
                            LOGGER.debug("Value for Cost Sheet Number is invalid and hence can't proceed with upload at Row:" + record.getVariable(SparcCostingConstants.COST_SHEET_NUMBER).getCoordinate().getRow() + ", Column:" + record.getVariable(SparcCostingConstants.COST_SHEET_NUMBER).getCoordinate().getRow());
                        } else {
                            costSheetNumber = (String) record.getVariable(SparcCostingConstants.COST_SHEET_NUMBER).getData();
                            csObject = (LCSProductCostSheet) getCostSheetObject(costSheetNumber, record.getVariable(SparcCostingConstants.COST_SHEET_NUMBER));
                            LOGGER.debug("**csObject:" + csObject + ":" + csObject.getName());
                        }
                        if (!VersionHelper.isCheckedOut(csObject)) {
                            csObject = VersionHelper.checkout(csObject);
                            LOGGER.debug("**Initial Checkedout Success!!!");
                        }
						Variable varData = record.getVariable(COST_SELECTED_COLORWAY_NAMES);
                        String appColors= varData.getData() != null? (String)varData.getData() : "";
						if(!FormatHelper.hasContent(appColors)){
							 this.context.addToCorrespondenceLog(new TCExceptionRuntime("Skipping the Update of Cost Sheet since the colorways value is not filled  at Row:" + rowNum, TCExceptionRuntime.Type.ERROR));
                             this.countErrors++;
							 LOGGER.debug("No Input for colorways provided");
							 continue;
						}
                        appColors = convertCommaSeparatedString(appColors);
                        varData = record.getVariable(REP_COLORWAY);
                        String repColorway = varData.getData() != null? (String)varData.getData() : "";
						
						if(!FormatHelper.hasContent(repColorway)){
							 this.context.addToCorrespondenceLog(new TCExceptionRuntime("Skipping the Update of Cost Sheet since the RepColorway value is not filled  at Row:" + rowNum, TCExceptionRuntime.Type.ERROR));
                             this.countErrors++;
							 LOGGER.debug("No Input for Rep colorway provided");
							 continue;
						}						
						String isPrimary = (String)record.getVariable("isPrimary").getData();
				          
                        List<String> selectedColorwayIds= new ArrayList<String>();

                        if(csObject.getApplicableColorNames() == null){
							isProdContext = true;
							isUpdated = this.setAttributeValuesOnUpdate(csObject, record);							
                            LCSProduct	objProduct = validateProductInput(CS_HIGHLEVEL_PRODUCT_NAME_IDENTIFIER, record, rowNum);
                            LCSSeason	objSeason = validateSeasonInput(CS_HIGHLEVEL_SEASON_NAME_IDENTIFIER, record, rowNum);
                            String selectedColorwaysStatus = validateSelectedColorwayInput(CS_HIGHLEVEL_SELECTED_COLORWAYS_IDENTIFIER, record, objProduct, objSeason, rowNum);
                            Map<String, LCSSKU>  repColorways = validateRepresentativeColorwayNameInput(CS_HIGHLEVEL_REPRESENTATIVE_COLORWAY_IDENTIFIER, record, objProduct, objSeason, selectedColorwaysStatus, rowNum);
							if (selectedColorwaysStatus != null && !selectedColorwaysStatus.equalsIgnoreCase(VALUE_ERROR)) {
                                if (!selectedColorwaysStatus.equalsIgnoreCase(NO_VALUE_STATUS)) {
                                    try {
                                        selectedColorwayIds = getSelectedColorwayIds(selectedColorwaysStatus, objProduct);
                                        addSelectedColorwaysOnUpdate = true;
                                    } catch (WTException | WTPropertyVetoException e) {
                                        this.context.addToCorrespondenceLog(new TCExceptionRuntime
                                                ("Error occurred when trying to get the Applicable Colors, Costsheet Creation not done, "
                                                        + "Contact Administrator!!! Selected Colorways shouldn't be null or invalid"
                                                        + " for the Representative Colorway to be added, Data entered in Row: "
                                                        + record.getVariable(CS_HIGHLEVEL_SELECTED_COLORWAYS_IDENTIFIER).
                                                        getCoordinate().getRow(),
                                                        TCExceptionRuntime.Type.ERROR));
                                        this.countErrors++;
                                        addSelectedColorwaysOnUpdate = false;
                                    }
                                } else {
                                    addSelectedColorwaysOnUpdate = false;
                                }
                            }
                            if (repColorways != null) {
                                for (String repSKUStatusKey : repColorways.keySet()) {
                                    if (!repSKUStatusKey.equalsIgnoreCase(VALUE_ERROR)) {
                                        addRepSKUOnUpdate = true;
                                        objSKU = repColorways.get(repSKUStatusKey);
                                    }
                                }
                            }	
							isUpdated = addSelectedColorwaysOnUpdate && addRepSKUOnUpdate;							
                        }
						else{							
							isUpdated = this.setAttributeValuesOnUpdate(csObject, record);	
						}						 					
                        LOGGER.debug("isUpdated-------------------------"+isUpdated);
						
                        if(!isUpdated && VersionHelper.isCheckedOut(csObject)) {
                            LOGGER.debug("**No Updates made hence Undo Checkout");
						    VersionHelper.undoCheckout(csObject);
                        }else if (isUpdated) {
                            Transaction tr = null;
                            try {
                                LOGGER.debug("**Transaction started");
								System.out.println("**Transaction started");
                            
                                tr = new Transaction();
                                tr.start();
                                LOGGER.debug("**Update Has been Made and CS will be persisted");
								LOGGER.debug("isPrimary------------------"+isPrimary);
                            	if("Yes".equalsIgnoreCase(isPrimary)){
								   csObject.setApplicableColorNames(appColors);
                                   csObject =(LCSProductCostSheet) new LCSCostSheetLogic().setAsPrimary(csObject);
							    }
								else{
								   LCSCostSheetLogic.deriveFlexTypeValues(csObject, true);
								   csObject = (LCSProductCostSheet) LCSCostSheetLogic.persist(csObject);
								   csObject.setApplicableColorNames(appColors);
   								}							
                                if(!selectedColorwayIds.isEmpty()) //Logic to save applicable colorways
                                {
                                    LCSCostSheetMaster costSheetMaster = (LCSCostSheetMaster)csObject.getMaster();
                                    for(String objSKUID : selectedColorwayIds)
                                    {
                                        LCSSKU skuObj = (LCSSKU)LCSQuery.findObjectById(objSKUID);
                                        if(skuObj != null) {
                                            if (objSKUID.equals(FormatHelper.getVersionId(objSKU))) {
                                                createColorLink(costSheetMaster, skuObj, true);
                                            }
                                            else{
                                                createColorLink(costSheetMaster, skuObj, false);
                                            }
                                        }
                                    }
                                }
                                if(VersionHelper.isCheckedOut(csObject)) {
                                    VersionHelper.checkin(csObject);
                                }
                                LOGGER.debug("Checked in Success!!!");
                                tr.commit();
                                tr = null;
                            } catch (Exception e) {
                                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Following error occurred when trying to save the details for Row:" + (record.getVariable(SparcCostingConstants.COST_SHEET_NUMBER).getCoordinate().getRow() + 1) + "\n" + e.getLocalizedMessage(), TCExceptionRuntime.Type.ERROR));
                                this.countErrors++;
                                LOGGER.debug("Following error occurred when trying to save the details for Row:" + (record.getVariable(SparcCostingConstants.COST_SHEET_NUMBER).getCoordinate().getRow() + 1) + "\n" + e.getLocalizedMessage());
                                e.printStackTrace();
                            } finally {
                                if (tr != null) {
                                    tr.rollback();
                                    LOGGER.debug("**Transaction RolledBack");
                                }
                                if (csObject != null) {
                                    csObject = (LCSProductCostSheet) VersionHelper.latestIterationOf(csObject);
                                    if (VersionHelper.isCheckedOut(csObject)) {
                                        VersionHelper.undoCheckout(csObject);
                                        LOGGER.debug("Undo Checkout Success!!!");
                                    }
                                }
                            }
                        }
                        LOGGER.debug("**Update Ended:");
                    }
                    //create logic start
                    else if (UPLOAD_CREATE.equalsIgnoreCase(mode)) {
                        LOGGER.debug("***Create Logic start");
                        ValidateInputObjectsandCreateCS(record, rowNum);
                        LOGGER.debug("***Create Logic end");
                    }
                }
            }
            String uploadSummaryFile = "";
            if (isZipFile) {
                LOGGER.debug("***zip was uploaded");
                uploadSummaryFile = SparcCostingUploadUtil.writeErrorsAndWarningsToExcel(this.context.getCorrespondenceErrors(), this.context.getCorrespondenceWarnings(), strBO_Name, this.countErrors, this.countWarnings);
                summaryFileName = uploadSummaryFile;
            } else {
                LOGGER.debug("***xlsx was uploaded");
                uploadSummaryFile = SparcCostingUploadUtil.writeErrorsAndWarningsToExcel(this.context.getCorrespondenceErrors(), this.context.getCorrespondenceWarnings(), strBO_Name, this.countErrors, this.countWarnings);
                LOGGER.debug("**uploadSummaryFile:" + uploadSummaryFile);
                uploadDataAndSummaryFiles(dataFileName, uploadSummaryFile, businessObjectId, strBO_Name, docFlexTypePath);
            }

        } catch (IOException | WTException e) {
            LOGGER.debug("PerformUpload WTException-----" + e.getLocalizedMessage());
            e.printStackTrace();
        } catch (Exception ex) {
            LOGGER.debug("PerformUpload Exception-----" + ex.getLocalizedMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (csObject != null) {
                    csObject = (LCSProductCostSheet) VersionHelper.latestIterationOf(csObject);
                    if (VersionHelper.isCheckedOut(csObject)) {
                        LOGGER.debug("Undo Checkout Success!!!");
                        VersionHelper.undoCheckout(csObject);
                    }
                }
            } catch (WTException e) {
                e.printStackTrace();
            }
            try {
                if (templateWorkbook != null) {
                    templateWorkbook.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                if (dataWorkbook != null) {
                    dataWorkbook.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        LOGGER.info("**performUpload - end:");
        return summaryFileName;
    }

    /**
     * This method creates the CostSheet to Color Link Object for each colorway.
     * @param csMaster
     * @param sku
     * @param isRepColor
     */
    public static void createColorLink(LCSCostSheetMaster csMaster, LCSSKU sku, boolean isRepColor)
    {
        try {
            CostSheetToColorLink costSheetToColorLink = CostSheetToColorLink.newCostSheetToColorLink(sku.getMaster(), csMaster);
            costSheetToColorLink.setRepresentative(isRepColor);
            LCSCostSheetLogic.persist(costSheetToColorLink);
            LOGGER.debug("Saved CostSheetToColorLink with OID:"+FormatHelper.getObjectId(costSheetToColorLink));
        } catch (Exception e) {
            LOGGER.error("Error creating Cost sheet to Color Link for cost Sheet:"+csMaster+", with sku:"+sku);
        }
    }

    /**
     *
     * @param input
     * @return
     */
    public static String convertCommaSeparatedString(String input) {
        // Split the input string by commas
        String[] skuNames = input.split(",");

        // Join the parts with the separator |~*~| using Java 8 streams
        String result = Arrays.stream(skuNames)
                .collect(Collectors.joining(SKU_DELIMITER));

        // Add the trailing separator if the input is not empty
        if (!input.isEmpty()) {
            result += SKU_DELIMITER;
        }
        return result;
    }

    /**
     * This validates if the uploaded Product Name is valid for create
     *
     * @param productName
     * @param record
     * @return
     */
    public LCSProduct validateProductInput(String productName, Table.Record record, int rowNum) {
        String userInput = "";
        Variable variable = null;
        LCSProduct prd = null;
        variable = record.getVariable(productName);
        if (variable != null && variable.getData() != null) {
            userInput = String.valueOf(variable.getData());
            LOGGER.debug("Product Name userInput:" + userInput);
            try {
                prd = new LCSProductQuery().findProductByNameType(userInput, null);
                if (prd != null) {
                    prd = (LCSProduct) VersionHelper.getVersion(prd, "A");
                    LOGGER.debug("Product Name:" + prd.getName());
                } else {
                    LOGGER.debug("Invalid Product Name Entered");
                    this.context.addToCorrespondenceLog(new TCExceptionRuntime("Unable to Find the Product from the values entered at Row: " + variable.getCoordinate().getRow(), TCExceptionRuntime.Type.ERROR));
                    this.countErrors++;
                }
            } catch (WTException e) {
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Unable to Find the Product from the values entered at Row: " + variable.getCoordinate().getRow(), TCExceptionRuntime.Type.ERROR));
                this.countErrors++;
                e.printStackTrace();
            }
        } else {
            LOGGER.debug("** PLM Name was not entered ");
            this.context.addToCorrespondenceLog(new TCExceptionRuntime("Value not entered in the Field PLM Name at Row: " + rowNum, TCExceptionRuntime.Type.ERROR));
            this.countErrors++;
        }
        return prd;
    }

    /**
     * This validates if the uploaded Season Name is valid for create
     *
     * @param seasonName
     * @param record
     * @return
     */
    public LCSSeason validateSeasonInput(String seasonName, Table.Record record, int rowNum) {
        String userInput = "";
        Variable variable = null;
        LCSSeason season = null;
        variable = record.getVariable(seasonName);
        if (variable != null && variable.getData() != null) {
            userInput = String.valueOf(variable.getData());
            LOGGER.debug("Season Name userInput:" + userInput);
            try {
                season = new LCSSeasonQuery().findSeasonByNameType(userInput, null);
                if (season != null) {
                    season = (LCSSeason) VersionHelper.latestIterationOf(season);
                    LOGGER.debug("season Name:" + season.getName());
                } else {
                    LOGGER.debug("**Season Not Found");
                    this.context.addToCorrespondenceLog(new TCExceptionRuntime("Unable to Find the Season from the values Entered at Row: " + variable.getCoordinate().getRow(), TCExceptionRuntime.Type.ERROR));
                    this.countErrors++;
                }
            } catch (WTException e) {
                LOGGER.debug("Exception when getting Season Name");
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Unable to Find the Season from the Values Entered in Row: " + variable.getCoordinate().getRow(), TCExceptionRuntime.Type.ERROR));
                this.countErrors++;
                e.printStackTrace();
            }
        } else {
            LOGGER.debug("Season name was not entered");
            this.context.addToCorrespondenceLog(new TCExceptionRuntime("Value not entered in the Field Season Name at Row: " + rowNum, TCExceptionRuntime.Type.ERROR));
            this.countErrors++;
        }
        return season;
    }

    /**
     * This validates if the uploaded Cost Sheet type is valid for create
     *
     * @param costSheetType
     * @param record
     * @return
     */
    public FlexType validateCostSheetTypeInput(String costSheetType, Table.Record record, int rowNum) {
        String userInput = "";
        Variable variable = null;
        FlexType csflexType = null;

        variable = record.getVariable(costSheetType);
        if (variable != null && variable.getData() != null) {
            userInput = String.valueOf(variable.getData());
            LOGGER.debug("**costSheetType userInput:" + userInput);
            try {
                LOGGER.debug("**costSheetTypeDisplayNamesCol:" + costSheetTypeDisplayNamesCol);
                LOGGER.debug("**costSheetTypeInternalDisplayNameMappingCol:" + costSheetTypeInternalDisplayNameMappingCol);
                if (costSheetTypeDisplayNamesCol.contains(userInput)) {
                    for (String pairCol : costSheetTypeInternalDisplayNameMappingCol) {
                        String[] pair = pairCol.split("~");
                        if (pair[0].equalsIgnoreCase(userInput)) {
                            csflexType = FlexTypeCache.getFlexTypeFromPath(pair[1]);
                        }
                    }
                } else {
                    LOGGER.debug("Invalid Cost Sheet Type Entered at Row: " + variable.getCoordinate().getRow());
                    this.context.addToCorrespondenceLog(new TCExceptionRuntime("Invalid Cost Sheet Type Entered at Row: " + variable.getCoordinate().getRow(), TCExceptionRuntime.Type.ERROR));
                    this.countErrors++;
                }
            } catch (WTException e) {
                LOGGER.debug("Error-Invalid Cost Sheet Type Entered at Row: " + rowNum);
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Invalid Cost Sheet Type Entered at Row: " + rowNum, TCExceptionRuntime.Type.ERROR));
                this.countErrors++;
                e.printStackTrace();
            }
        } else {
            LOGGER.debug("Value not entered in the Field Cost Sheet Type at Row: " + variable.getCoordinate().getRow());
            this.context.addToCorrespondenceLog(new TCExceptionRuntime("Value not entered in the Field Cost Sheet Type at Row: " + rowNum, TCExceptionRuntime.Type.ERROR));
            this.countErrors++;
        }

        return csflexType;
    }

    /**
     * This validates if the uploaded Source Name is valid for create
     *
     * @param plmSourceName
     * @param record
     * @param prd
     * @param season
     * @return
     */
    @SuppressWarnings("unchecked")
    public LCSSourcingConfig validateSourceNameInput(String plmSourceName, Table.Record record, LCSProduct prd, LCSSeason season, int rowNum) {
        String userInput = "";
        Variable variable = null;
        LCSSourcingConfig srcConfig = null;
        variable = record.getVariable(plmSourceName);
        if (variable != null && variable.getData() != null) {
            userInput = String.valueOf(variable.getData()).trim();
            LOGGER.debug("**plmSourceName userInput:" + userInput);
            try {
                Collection<LCSSourcingConfig> allSources = LCSSourcingConfigQuery.getSourcingConfigForProductSeason(prd, season);
                LOGGER.debug("**allSources:" + allSources);
                for (LCSSourcingConfig srcConfigTemp : allSources) {
                    LOGGER.debug("**Processing:" + srcConfigTemp + ":::" + srcConfigTemp.getSourcingConfigName());
                    if (userInput.equals(srcConfigTemp.getSourcingConfigName())) {
                        srcConfig = srcConfigTemp;
                    }
                }
                if (srcConfig == null) {
                    this.context.addToCorrespondenceLog(new TCExceptionRuntime("Invalid Sourcing Config Entered at Row: " + variable.getCoordinate().getRow(), TCExceptionRuntime.Type.ERROR));
                    this.countErrors++;
                }
            } catch (WTException e) {
                this.countErrors++;
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Invalid Sourcing Config Entered in Row: " + variable.getCoordinate().getRow(), TCExceptionRuntime.Type.ERROR));
                e.printStackTrace();
            }
        } else {
            this.context.addToCorrespondenceLog(new TCExceptionRuntime("Value not entered in the Field Invalid Sourcing Config  in Row: " + rowNum, TCExceptionRuntime.Type.ERROR));
            this.countErrors++;
        }
        return srcConfig;
    }

    /**
     * This validates if the uploaded Spec Name is valid for create
     *
     * @param specName
     * @param record
     * @param prd
     * @param season
     * @param srcConfig
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, FlexSpecification> validateSpecNameInput(String specName, Table.Record record, LCSProduct prd, LCSSeason season, LCSSourcingConfig srcConfig, int rowNum) {
        Map<String, FlexSpecification> flexSpecStatusMap = new HashMap<String, FlexSpecification>();
        FlexSpecification flexSpec = null;
        String userInput = "";
        Variable variable = null;
        variable = record.getVariable(specName);
        if (variable != null && variable.getData() != null) {
            userInput = String.valueOf(variable.getData()).trim();
            if (!FormatHelper.hasContent(userInput)) {
                //flexSpecStatusMap.put("novalue", null);
                flexSpecStatusMap.put(NO_VALUE_STATUS, null);
                LOGGER.debug("Value not entered in Specification");
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Value not entered in the Field Specification in Row: " + rowNum, TCExceptionRuntime.Type.WARNING));
                this.countWarnings++;
                return flexSpecStatusMap;
            }
            LOGGER.debug("**specName userInput:" + userInput);
            try {
                SearchResults specResult = FlexSpecQuery.findExistingSpecs(prd, season, srcConfig);
                LOGGER.debug("**specResult.getResult:" + specResult.getResults());
                for (FlexObject specFlexObj : (Collection<FlexObject>) specResult.getResults()) {
                    String tempSpecName = specFlexObj.getString(CS_HIGHLEVEL_SPECNAME_IDENTIFIER);
                    if (userInput.equals(tempSpecName)) {
                        flexSpec = (FlexSpecification) LCSQuery.findObjectById(CS_HIGHLEVEL_SPEC_OBJECTID_PREFIX + specFlexObj.getString(CS_HIGHLEVEL_SPEC_IDA2A2));
                        // flexSpecStatusMap.put("found", flexSpec);
                        flexSpecStatusMap.put(VALUE_FOUND, flexSpec);
                        LOGGER.debug("**Returning flexSpec:" + flexSpec);
                        break;
                    }
                }
                if (flexSpec == null) {
                    LOGGER.debug("**Spec Not Found");
                    //flexSpecStatusMap.put("error", null);
                    flexSpecStatusMap.put(VALUE_ERROR, null);
                    this.context.addToCorrespondenceLog(new TCExceptionRuntime("Invalid Specification Entered at Row: " + variable.getCoordinate().getRow(), TCExceptionRuntime.Type.ERROR));
                    this.countErrors++;
                }
            } catch (WTException e) {
                //flexSpecStatusMap.put("error", null);
                flexSpecStatusMap.put(VALUE_ERROR, null);
                LOGGER.debug("**Exception when retrieving Spec Not Found");
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Invalid Specification Entered in Row: " + variable.getCoordinate().getRow(), TCExceptionRuntime.Type.ERROR));
                this.countErrors++;
                e.printStackTrace();
            }
        } else {
            flexSpecStatusMap.put(NO_VALUE_STATUS, null);
            LOGGER.debug("Value not entered in Specification");
            this.context.addToCorrespondenceLog(new TCExceptionRuntime("Value not entered in the Field Specification in Row: " + rowNum, TCExceptionRuntime.Type.ERROR));
            this.countWarnings++;
        }

        return flexSpecStatusMap;
    }

    /**
     * This validates if the Selected Colorways  is valid for create
     *
     * @param costSheetSelectedColorwayNames
     * @param record
     * @param prd
     * @param season
     * @return
     */
    @SuppressWarnings("unchecked")
    public String validateSelectedColorwayInput(String costSheetSelectedColorwayNames, Table.Record record, LCSProduct prd, LCSSeason season, int rowNum) {

        String userInput = "";
        String costSheetSelectedColorwayNamesStatus = "";
        Variable variable = null;
        variable = record.getVariable(costSheetSelectedColorwayNames);
        Collection<String> tempSelectedColorwayCol = null;
        if (variable != null && variable.getData() != null) {
            userInput = String.valueOf(variable.getData()).trim();
            LOGGER.debug("**costSheetSelectedColorwayNames userInput:" + userInput);
            try {
                if (!FormatHelper.hasContent(userInput)) {
                    this.context.addToCorrespondenceLog(new TCExceptionRuntime("No input for colorway is provided in the excel so costsheet will not be created " + variable.getCoordinate().getRow(), TCExceptionRuntime.Type.ERROR));
                    costSheetSelectedColorwayNamesStatus = VALUE_ERROR;
                    this.countErrors++;
                    return costSheetSelectedColorwayNamesStatus;
                }
                tempSelectedColorwayCol = FormatHelper.commaSeparatedListToCollection(userInput);
                for (String tempSKU : tempSelectedColorwayCol) {
                    Collection<LCSSKU> allSKUs = LCSSKUQuery.findSKUs(prd);
                    boolean isSKUExist = false;
                    for (LCSSKU skuObj : allSKUs) {
                        LOGGER.debug("**Checking Against:" + skuObj.getValue(CS_HIGHLEVEL_SKUNAME_ATTR));
                        if (tempSKU.equals(skuObj.getValue(CS_HIGHLEVEL_SKUNAME_ATTR))) {
                            LCSSKUSeasonLink skuSeasonLink = (LCSSKUSeasonLink) LCSSeasonQuery.findSeasonProductLink(skuObj, season);
                            LOGGER.debug("**skuSeasonLink:" + skuSeasonLink);
                            if (!skuSeasonLink.isSeasonRemoved()) {
                                isSKUExist = true;
                            }
                        }
                    }
                    if (!isSKUExist) {
                        LOGGER.debug("Either Colorway:" + tempSKU + " Not Present in Product or not added to Season Entered at Row: " + variable.getCoordinate().getRow());
                        this.context.addToCorrespondenceLog(new TCExceptionRuntime("Either Colorway:" + tempSKU + " Not Present in Product or not added to Season Entered in Row: " + variable.getCoordinate().getRow(), TCExceptionRuntime.Type.ERROR));
                        costSheetSelectedColorwayNamesStatus = VALUE_ERROR;
                    } else
                        costSheetSelectedColorwayNamesStatus = userInput;
                }
            } catch (WTException e) {
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Either Colorway Not Present in Product or not added to Season Entered in Row: " + variable.getCoordinate().getRow(), TCExceptionRuntime.Type.ERROR));
                costSheetSelectedColorwayNamesStatus = VALUE_ERROR;
                this.countErrors++;
                e.printStackTrace();
            }
        } else {
            LOGGER.debug("No Colorway selected for creation");
            costSheetSelectedColorwayNamesStatus = NO_VALUE_STATUS;
        }
        return costSheetSelectedColorwayNamesStatus;
    }


    /***
     *
     * @param csSkuFromInput
     * @param productAttKey
     * @param productAttValue
     * @return
     * @throws WTException
     * @throws WTPropertyVetoException
     */
    public static LCSSKU findSKU(String csSkuFromInput, String productAttKey, String productAttValue) throws WTException, WTPropertyVetoException {
        LOGGER.debug("**findSKU:" + csSkuFromInput + "::" + productAttKey + "::" + productAttValue);
        LCSSKU csSku = null;
        FlexType skuType = FlexTypeCache.getFlexTypeFromPath("Product");
        skuType.setTypeScopeDefinition("SKU");
        FlexType productType = FlexTypeCache.getFlexTypeFromPath("Product");
        skuType.setTypeScopeDefinition("PRODUCT");
        try {
            PreparedQueryStatement statement = new PreparedQueryStatement();
            statement.appendFromTable(LCSSKU.class);
            statement.appendFromTable(LCSProduct.class);
            statement.appendSelectColumn(new QueryColumn(LCSSKU.class, "thePersistInfo.theObjectIdentifier.id"));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSSKU.class, "flexTypeIdPath"), skuType.getTypeIdPath() + "%", Criteria.LIKE));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "flexTypeIdPath"), skuType.getTypeIdPath() + "%", Criteria.LIKE));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSSKU.class, skuType.getAttribute("skuName").getColumnDescriptorName()), csSkuFromInput.trim(), Criteria.EQUALS));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, productType.getAttribute(productAttKey).getColumnDescriptorName()), productAttValue, Criteria.EQUALS));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria("LCSPRODUCT", "latestiterationinfo", "1", Criteria.EQUALS));
            statement.appendAndIfNeeded();
            statement.appendJoin("LCSSKU", "productARevId", "LCSPRODUCT", "branchIditerationInfo");
            //statement.appendCriteria(new Criteria(new QueryColumn(LCSSKU.class, "productARevId"), new QueryColumn(LCSProduct.class, "branchIditerationInfo"), Criteria.EQUALS));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria("LCSSKU", "placeholder", "1", Criteria.NOT_EQUAL_TO));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria("LCSSKU", "latestiterationinfo", "1", Criteria.EQUALS));

            LOGGER.debug("SKU Query: " + statement);
            SearchResults results = LCSQuery.runDirectQuery(statement);
            if (results != null) {
                LOGGER.debug("Sku results :" + results.toString());
                if (results.getResultsFound() > 0) {
                    LOGGER.debug("SKU Found: " + csSkuFromInput);
                    FlexObject obj = (FlexObject) results.getResults().elementAt(0);
                    if (obj != null) {
                        csSku = (LCSSKU) LCSQuery.findObjectById("OR:com.lcs.wc.product.LCSSKU:" + obj.getString("LCSSKU.idA2A2"));
                        csSku = (LCSSKU) VersionHelper.getVersion(csSku.getMaster(), "A");
                        csSku = (LCSSKU) VersionHelper.latestIterationOf(csSku);

                        if (csSku != null)
                            LOGGER.debug("SKU Match found : " + csSku.toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.debug("Error in findSKU" + e.getLocalizedMessage());
        }
        return csSku;
    }

    /**
     * This validates if the Representative Colorway is valid for create
     *
     * @param representativeColorwayName
     * @param record
     * @param prd
     * @param season
     * @param costSheetSelectedColorwayNamesStatus
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, LCSSKU> validateRepresentativeColorwayNameInput(String representativeColorwayName, Table.Record record, LCSProduct prd, LCSSeason season, String costSheetSelectedColorwayNamesStatus, int rowNum) {
        Map<String, LCSSKU> representativeColorwayObjStatus = new HashMap<String, LCSSKU>();
        String userInput = "";
        Variable variable = null;
        variable = record.getVariable(representativeColorwayName);
        if (variable != null && variable.getData() != null) {
            userInput = String.valueOf(variable.getData()).trim();
            LOGGER.debug("**representativeColorwayName userInput:" + userInput);
            if (!FormatHelper.hasContent(userInput)) {
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("No input provided for Representative colorway in the excel so costsheet will not be created " + variable.getCoordinate().getRow(), TCExceptionRuntime.Type.ERROR));
                representativeColorwayObjStatus.put(VALUE_ERROR, null);
                this.countErrors++;
                return representativeColorwayObjStatus;
            }
            try {
                if ((!costSheetSelectedColorwayNamesStatus.equalsIgnoreCase(VALUE_ERROR)
                        && !costSheetSelectedColorwayNamesStatus.equalsIgnoreCase(NO_VALUE_STATUS))) {
                    Collection<LCSSKU> allSKUs = LCSSKUQuery.findSKUs(prd);
                    for (LCSSKU skuObj : allSKUs) {
                        LOGGER.debug("**skuObj.getName():" + skuObj.getValue(CS_HIGHLEVEL_SKUNAME_ATTR));
                        if (userInput.equals(skuObj.getValue(CS_HIGHLEVEL_SKUNAME_ATTR))) {
                            LCSSKUSeasonLink skuSeasonLink = (LCSSKUSeasonLink) LCSSeasonQuery.findSeasonProductLink(skuObj, season);
                            LOGGER.debug("**skuSeasonLink:" + skuSeasonLink);
                            if (!skuSeasonLink.isSeasonRemoved()) {
                                representativeColorwayObjStatus.put(VALUE_FOUND, skuObj);
                            }
                        }
                    }
                } else {
                    LOGGER.debug("Either Selected Colorway is blank or invalid but Rep Colorway is filled-in");
                    this.context.addToCorrespondenceLog(new TCExceptionRuntime("Selected Colorways shouldn't be null or invalid for the Representative Colorway to be added, Data entered in Row: " + variable.getCoordinate().getRow(),
                            TCExceptionRuntime.Type.ERROR));
                    this.countErrors++;
                    representativeColorwayObjStatus.put(VALUE_ERROR, null);
                }
            } catch (WTException e) {
                representativeColorwayObjStatus.put(VALUE_ERROR, null);
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Selected Colorways shouldn't be null or invalid for the Representative Colorway to be added, Data entered in Row: " + variable.getCoordinate().getRow(),
                        TCExceptionRuntime.Type.ERROR));
                this.countErrors++;
                e.printStackTrace();
            }
        } else {
            representativeColorwayObjStatus.put(NO_VALUE_STATUS, null);
            LOGGER.debug("No Represent Colorway selected for creation");
        }

        return representativeColorwayObjStatus;
    }


    /**
     * Started Method to Validate the input and perform Creation of cost sheet
     *
     * @param record
     */
    public void ValidateInputObjectsandCreateCS(Table.Record record, int rowNum) {
        Iterator<String> itr = csHighLevelMandatortAttsforCreateCol.iterator();
        String objectKey = "";
        LCSProduct product = null;
        LCSSeason season = null;
        LCSSourcingConfig srcConfig = null;
        FlexType csflexType = null;
        Map<String, FlexSpecification> flexSpecStatus = null;
        String costSheetSelectedColorwayNamesStatus = "";
        Map<String, LCSSKU> representativeColorwayObjStatus = null;
        boolean addSpec = false;
        boolean addSelectedColorways = false;
        boolean addRepSKU = false;
        FlexSpecification spec = null;
        ArrayList<String> selectedColorwayIDCol = null;
        LCSSKU sku = null;
        boolean mandatorAttValidationPass = false;
        while (itr.hasNext()) {
            objectKey = itr.next();
            LOGGER.debug("**Processing objectKey:" + objectKey);
            if (objectKey.equalsIgnoreCase(CS_HIGHLEVEL_PRODUCT_NAME_IDENTIFIER)) {
                product = validateProductInput(CS_HIGHLEVEL_PRODUCT_NAME_IDENTIFIER, record, rowNum);
            } else if (objectKey.equalsIgnoreCase(CS_HIGHLEVEL_SEASON_NAME_IDENTIFIER)) {
                season = validateSeasonInput(CS_HIGHLEVEL_SEASON_NAME_IDENTIFIER, record, rowNum);
            } else if (objectKey.equalsIgnoreCase(CS_HIGHLEVEL_COSTSHEET_TYPE_IDENTIFIER)) {
                csflexType = validateCostSheetTypeInput(CS_HIGHLEVEL_COSTSHEET_TYPE_IDENTIFIER, record, rowNum);
            } else if (objectKey.equalsIgnoreCase(CS_HIGHLEVEL_PLMSOURCE_NAME_IDENTIFIER) && product != null && season != null) {
                srcConfig = validateSourceNameInput(CS_HIGHLEVEL_PLMSOURCE_NAME_IDENTIFIER, record, product, season, rowNum);
            } else if (objectKey.equalsIgnoreCase(CS_HIGHLEVEL_SPEC_NAME_IDENTIFIER) && product != null && season != null && srcConfig != null) {
                flexSpecStatus = validateSpecNameInput(CS_HIGHLEVEL_SPEC_NAME_IDENTIFIER, record, product, season, srcConfig, rowNum);
            } else if (objectKey.equalsIgnoreCase(CS_HIGHLEVEL_SELECTED_COLORWAYS_IDENTIFIER) && product != null && season != null) {
                costSheetSelectedColorwayNamesStatus = validateSelectedColorwayInput(CS_HIGHLEVEL_SELECTED_COLORWAYS_IDENTIFIER, record, product, season, rowNum);
            } else if (objectKey.equalsIgnoreCase(CS_HIGHLEVEL_REPRESENTATIVE_COLORWAY_IDENTIFIER)) {
                representativeColorwayObjStatus = validateRepresentativeColorwayNameInput(CS_HIGHLEVEL_REPRESENTATIVE_COLORWAY_IDENTIFIER, record, product, season, costSheetSelectedColorwayNamesStatus, rowNum);
            }
        }

        mandatorAttValidationPass = validateMandatoryAttributes(record, rowNum);

        if (flexSpecStatus != null) {
            for (String specStatusKey : flexSpecStatus.keySet()) {
                if (!specStatusKey.equalsIgnoreCase("error")) {
                    addSpec = true;
                    spec = flexSpecStatus.get(specStatusKey);
                }
            }
        }
        if (costSheetSelectedColorwayNamesStatus != null && !costSheetSelectedColorwayNamesStatus.equalsIgnoreCase(VALUE_ERROR)) {
            selectedColorwayIDCol = new ArrayList<String>();
            if (!costSheetSelectedColorwayNamesStatus.equalsIgnoreCase(NO_VALUE_STATUS)) {
                try {
                    selectedColorwayIDCol = getSelectedColorwayIds(costSheetSelectedColorwayNamesStatus, product);
                    addSelectedColorways = true;
                } catch (WTException | WTPropertyVetoException e) {
                    this.context.addToCorrespondenceLog(new TCExceptionRuntime
                            ("Error occured when trying to get the Applicable Colors, Costsheet Creation not done, "
                                    + "Contact Administrator!!!Selected Colorways shouln't be null or invalid"
                                    + " for the Representative Colorway to be added, Data entered in Row: "
                                    + record.getVariable(CS_HIGHLEVEL_SELECTED_COLORWAYS_IDENTIFIER).
                                    getCoordinate().getRow(),
                                    TCExceptionRuntime.Type.ERROR));
                    this.countErrors++;
                    addSelectedColorways = false;
                }
            } else {
                addSelectedColorways = false;
            }
        }

        if (representativeColorwayObjStatus != null) {
            for (String repSKUStatusKey : representativeColorwayObjStatus.keySet()) {
                if (!repSKUStatusKey.equalsIgnoreCase(VALUE_ERROR)) {
                    addRepSKU = true;
                    sku = representativeColorwayObjStatus.get(repSKUStatusKey);
                }
            }
        }

        if (product != null && season != null && csflexType != null && srcConfig != null && addSpec && addSelectedColorways && addRepSKU && mandatorAttValidationPass) {

            createCostSheet(record, product, season, csflexType, srcConfig, spec, selectedColorwayIDCol, sku);
        }

    }

    public boolean validateMandatoryAttributes(Table.Record record, int rowNum) {
        Iterator<String> stringIterator = null;
        boolean mandatorAttValidationPass = true;
        String[] pair = null;
        stringIterator = csHighLevelRequiredAttCol.iterator();
        while (stringIterator.hasNext()) {
            pair = stringIterator.next().split("~");
            LOGGER.debug("pair:" + pair);
            if (!(record.getVariable(pair[1]) != null && record.getVariable(pair[1]).getData() != null && FormatHelper.hasContent(String.valueOf(record.getVariable(pair[1]).getData())))) {
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Skipping the Creation of Cost Sheet since the Value is not filled in for Required attribute " + pair[0] + " at Row:" + rowNum, TCExceptionRuntime.Type.ERROR));
                LOGGER.debug("Skipping the Creation of Cost Sheet since the Value is not filled in for Required attribute " + pair[0] + " at Row:" + rowNum);
                this.countErrors++;
                mandatorAttValidationPass = false;
            }
        }
        return mandatorAttValidationPass;
    }

    public ArrayList<String> getSelectedColorwayIds(String applicableColorNames, LCSProduct prod) throws WTException, WTPropertyVetoException {
        String[] applicableColorNameArr = applicableColorNames.split(",");
        String productNumber = String.valueOf(prod.getValue(PLM_PROD_NO));
        ArrayList<String> skuIDCol = new ArrayList<String>();
        for (String Color : applicableColorNameArr) {
            skuIDCol.add(SKU_STR_VR + String.valueOf(findSKU(Color, PLM_PROD_NO, productNumber).getBranchIdentifier()));
        }
        LOGGER.debug("**skuCol:" + skuIDCol);
        return skuIDCol;

    }

    /**
     * This method captures the logic to create cost sheet post validation
     *
     * @param record
     * @param product
     * @param season
     * @param csflexType
     * @param srcConfig
     * @param spec
     * @param applicableColorways
     * @param representativeColorwayObj
     */
    public void createCostSheet(Table.Record record, LCSProduct product, LCSSeason season, FlexType csflexType, LCSSourcingConfig srcConfig, FlexSpecification spec, ArrayList<String> applicableColors, LCSSKU representativeColorwayObj) {

        LOGGER.debug("**createCostSheet:" + applicableColors + "::" + representativeColorwayObj);
        Collection<String> tempSelectedColorways = null;

        // Csst sheet Create logic

        LCSCostSheetClientModel costSheetClientModel = new LCSCostSheetClientModel();
        try {
			String isPrimary = (String)record.getVariable("isPrimary").getData();				        
            LCSPartMaster placeholderMaster = product.getPlaceholderMaster();
            if (placeholderMaster != null) {
                costSheetClientModel.setSkuMaster(placeholderMaster);
            }
            costSheetClientModel.setCostSheetType("PRODUCT");
            costSheetClientModel.setFlexType(csflexType);

            if (product != null && product.getMaster() != null) {
                costSheetClientModel.setProductMaster(product.getMaster());
            }

            if (srcConfig != null && srcConfig.getMaster() != null) {
                LOGGER.debug("Setting SourcingConfigMaster on Cost Sheet:" + srcConfig.getMaster());
                costSheetClientModel.setSourcingConfigMaster(srcConfig.getMaster());
                LOGGER.debug("Setting setSourcingConfigRevId on Cost Sheet:" + srcConfig.getBranchIdentifier());
                //setting sourcingConfig RevID explicitly
                costSheetClientModel.setSourcingConfigRevId(srcConfig.getBranchIdentifier());
            }

            if (season != null && season.getMaster() != null) {
                LOGGER.debug("Setting SeasonMaster on Cost Sheet");
                costSheetClientModel.setSeasonMaster(season.getMaster());
            }

            boolean attributeSetStatus = setAttributeValuesOnCreate(csflexType, record, costSheetClientModel, product, season, srcConfig, spec, tempSelectedColorways, representativeColorwayObj);
            LOGGER.debug(attributeSetStatus);
            if (attributeSetStatus) {
                LOGGER.debug("**applicableColors:" + applicableColors);
                if (applicableColors != null && applicableColors.size() > 0)
                    setApplicableColors(costSheetClientModel, applicableColors, record);

                LOGGER.debug("**Setting rep.Colorway:" + representativeColorwayObj);
                if (representativeColorwayObj != null)
                    setRepresentativeColorway(costSheetClientModel, representativeColorwayObj);

                LOGGER.debug("**spec:" + spec);
                if (spec != null)
                    attachSpecification(costSheetClientModel, spec);

                costSheetClientModel.save();
				LCSProductCostSheet objCostSheet =(LCSProductCostSheet) costSheetClientModel.getBusinessObject();
             	if("Yes".equalsIgnoreCase(isPrimary)){
					if(VersionHelper.isLatestVersion(objCostSheet)){
						System.out.println("Latest Costsheet--------------");
						SourcingConfigHelper.service.setAsPrimary(objCostSheet);
					}
					else{						
						System.out.println("Not the latest CS object");
						objCostSheet=(LCSProductCostSheet)VersionHelper.latestIterationOf(objCostSheet);
						SourcingConfigHelper.service.setAsPrimary(objCostSheet);
					}
				}
                LOGGER.debug("**COST SHEET CREATION COMPLETED!!!");
            }
        } catch (WTPropertyVetoException | WTException e) {
            this.context.addToCorrespondenceLog(new TCExceptionRuntime("Following error occurred when trying to save the details for Row:" + (record.getVariable("mode").getCoordinate().getRow() + 1) + "\n" + e.getLocalizedMessage(), TCExceptionRuntime.Type.ERROR));
            this.countErrors++;
            LOGGER.debug("create-Following error occured when trying to save the details for Row:" + (record.getVariable("mode").getCoordinate().getRow() + 1) + "\n" + e.getLocalizedMessage());
            e.printStackTrace();
        } catch (Exception ex) {
            this.context.addToCorrespondenceLog(new TCExceptionRuntime("Following error occurred while saving the object " + ex.getLocalizedMessage(), TCExceptionRuntime.Type.WARNING));
            this.countWarnings++;
            LOGGER.debug("Error occurred while creating the CostSheet Object");
            ex.printStackTrace();
        }

    }

    /**
     * This method is used to attach Specification on the new cost sheet
     *
     * @param costSheetClientModel
     * @param spec
     * @return
     */
    private static LCSCostSheetClientModel attachSpecification(LCSCostSheetClientModel costSheetClientModel, FlexSpecification spec) {

        try {
            costSheetClientModel.setSpecificationMaster(spec.getMaster());
        } catch (Exception e) {
            LOGGER.debug("Encountered an exception while trying to set Specification, error:" + e.getMessage());
        }
        LOGGER.debug("**Specification atatched**");
        return costSheetClientModel;
    }

    /**
     * This method is used to set Applicable colors on the new cost sheet
     *
     * @param costSheetClientModel
     * @param applicableColorways
     * @param record
     * @return
     * @throws WTException
     */
    private static LCSCostSheetClientModel setApplicableColors(LCSCostSheetClientModel costSheetClientModel, ArrayList<String> applicableColors, Table.Record record) throws WTException {

        LOGGER.debug("*setApplicableColors:" + costSheetClientModel + "::" + applicableColors);
        Iterator<LCSSKU> colorIter = LCSQuery.getObjectsFromCollection(applicableColors, 2).iterator();
        StringBuffer skuBuffer = new StringBuffer();
        StringBuffer skuIDBuffer = new StringBuffer();
        while (colorIter.hasNext()) {
            LCSSKU sku = colorIter.next();
            skuBuffer.append("" + sku.getValue(SKU_NAME));
            skuBuffer.append(SKU_DELIMITER);
        }
        Iterator<String> irt = applicableColors.iterator();
        while (irt.hasNext()) {
            String id = irt.next();
            skuIDBuffer.append("" + id);
            skuIDBuffer.append(SKU_DELIMITER);
        }
        try {
            LOGGER.debug("*skuBuffer:" + skuBuffer.toString());
            LOGGER.debug("*skuIDBuffer:" + skuIDBuffer.toString());
            costSheetClientModel.setApplicableColorNames(skuBuffer.toString());
            costSheetClientModel.setColorwayGroup(skuIDBuffer.toString());
        } catch (Exception e) {
            LOGGER.debug("Encountered exception while setting applicable color names:" + applicableColors);
            e.printStackTrace();
        }

        return costSheetClientModel;
    }

    /**
     * This method is used to set Representative Colorway on the new cost sheet
     *
     * @param costSheetClientModel
     * @param representativeColorway
     * @return
     */
    public static LCSCostSheetClientModel setRepresentativeColorway(LCSCostSheetClientModel costSheetClientModel, LCSSKU representativeColorway) {
        try {
            LOGGER.debug("**Setting SKU Master for CS");
            // costSheetClientModel.setSkuMaster(representativeColorway.getMaster());
            String oid = FormatHelper.getVersionId(representativeColorway);
            if (FormatHelper.hasContent(oid))
                costSheetClientModel.setRepresentativeColorway(oid);
        } catch (Exception e) {
            LOGGER.debug("Encountered an error setting Representative Colorway, error:" + e.getMessage());
            e.printStackTrace();
        }

        return costSheetClientModel;
    }

    /**
     * This method is used to set other attributes on the new cost sheet
     *
     * @param csflexType
     * @param record
     * @param costSheetClientModel
     * @param prd
     * @param season
     * @param srcConfig
     * @param spec
     * @param tempSelectedColorways
     * @param representativeColorwayObj
     */
    public boolean setAttributeValuesOnCreate(FlexType csflexType, Table.Record record, LCSCostSheetClientModel costSheetClientModel, LCSProduct prd, LCSSeason season, LCSSourcingConfig srcConfig, FlexSpecification spec, Collection<String> tempSelectedColorways, LCSSKU representativeColorwayObj) {

        LOGGER.debug("**setAttributeValuesOnCreate - start");
        boolean status = setAttributeValuesOnCreate(costSheetClientModel, record, csflexType);
        LOGGER.debug("**setAttributeValuesOnCreate - end" + status);
        return status;
    }

    /**
     * This method is used to set other attributes on update cost sheet
     *
     * @param csObject
     * @param record
     * @return
     */
    public boolean setAttributeValuesOnUpdate(LCSProductCostSheet csObject, Table.Record record) {
        boolean isCostSheetUpdated = false;
        Iterator<String> stringIterator = null;
        String att = "";
        String currVal = "";
        Variable variable = null;
        String objectInputted = null;
        stringIterator = csHighLevelEditableAttCol.iterator();
        while (stringIterator.hasNext()) {
            try {
                att = (String) stringIterator.next();
                LOGGER.debug("Processing attribute:" + att);
                variable = record.getVariable(att);
                objectInputted = getValueToCompare(variable, csObject.getFlexType());
                if (objectInputted != null && FormatHelper.hasContent(String.valueOf(objectInputted))) {
                    currVal = String.valueOf(csObject.getValue(att));
                    LOGGER.debug("**currVal1:" + currVal);
                    if (!objectInputted.equalsIgnoreCase(currVal)) {
                        if (csHighLevelObjectRefAttributesCol.contains(att))
                            csObject.setValue(att, LCSQuery.findObjectById(objectInputted));
                        else
                            csObject.setValue(att, objectInputted);
                        isCostSheetUpdated = true;
                    }
                } else {
                    currVal = String.valueOf(csObject.getValue(att));
                    LOGGER.debug("**currVal2:" + currVal);
                    if (currVal != null && FormatHelper.hasContent(currVal.toString())) {
                        csObject.setValue(att, "");
                        isCostSheetUpdated = true;
                    }
                }
            } catch (Exception e) {

                String attDisplay = "";
                for (String pair : csHighLevelAttrKeyValueMap) {
                    String[] keyandValue = pair.split("~");

                    if (att.equalsIgnoreCase(keyandValue[0]))
                        attDisplay = keyandValue[1];
                }
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Error when trying to set value for field :" + attDisplay + " in the Row:" + (variable.getCoordinate().getRow() + 1) + ". Please Check the Value Entered!!!", TCExceptionRuntime.Type.ERROR));
                this.countErrors++;
                LOGGER.debug("Error when trying to set value for field :" + attDisplay + " in the Row:" + (variable.getCoordinate().getRow() + 1) + ". Please Check the Value Entered!!!");
                e.printStackTrace();
                isCostSheetUpdated = false;
                return isCostSheetUpdated;
            }
        }
        return isCostSheetUpdated;

    }

    public boolean setAttributeValuesOnCreate(LCSCostSheetClientModel costSheetClientModel, Table.Record record, FlexType csflexType) {
        Iterator<String> stringIterator = null;
        boolean attributeSetStatus = true;
        String att = "";
        Variable variable = null;
        String objectInputted = null;
        stringIterator = csHighLevelCreateableAttCol.iterator();
        LOGGER.debug("**setAttributeValuesOnCreate start");
        while (stringIterator.hasNext()) {
            try {
                att = (String) stringIterator.next();
                LOGGER.debug("*Processing att:" + att);
                variable = record.getVariable(att);
                objectInputted = getValueToCompare(variable, csflexType);
                if (objectInputted != null && FormatHelper.hasContent(String.valueOf(objectInputted))) {
                    if (csHighLevelObjectRefAttributesCol.contains(att))
                        costSheetClientModel.setValue(att, LCSQuery.findObjectById(objectInputted));
                    else
                        costSheetClientModel.setValue(att, objectInputted);
                } else {
                    costSheetClientModel.setValue(att, "");
                }

            } catch (Exception e) {
                String attDisplay = "";
                for (String pair : csHighLevelAttrKeyValueMap) {
                    String[] keyandValue = pair.split("~");

                    if (att.equalsIgnoreCase(keyandValue[0]))
                        attDisplay = keyandValue[1];
                }
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Error when trying to set value for field :" + attDisplay + " in the Row:" + (variable.getCoordinate().getRow() + 1) + ". Please Check the Value Entered!!!", TCExceptionRuntime.Type.ERROR));
                this.countErrors++;
                LOGGER.debug("Error when trying to set value for field :" + attDisplay + " in the Row:" + (variable.getCoordinate().getRow() + 1) + ". Please Check the Value Entered!!!");
                e.printStackTrace();
                attributeSetStatus = false;
                return attributeSetStatus;
            }
        }
        return attributeSetStatus;
    }

    /**
     * This method supplies the internal value to compare
     *
     * @param variable
     * @param csflexType
     * @return
     * @throws WTException
     * @throws ParseException
     */
    public String getValueToCompare(Variable variable, FlexType csflexType) throws WTException, ParseException {
        LOGGER.debug("**getValueToCompare:" + variable + ":" + csflexType.getTypeName());
        String userInput = null;

        if (variable != null) {
            String var = variable.getCoordinate().getPlaceHolder().getVar();
            FlexTypeAttribute flexAttribute = null;
            AttributeValueList attList = null;
            String variabeType = csflexType.getAttribute(var).getAttVariableType();

            if (variabeType.equalsIgnoreCase(CUSTOM_OBJECT_REF)) {
                if (var.equalsIgnoreCase(INCO_TERM_LOCATION)) {
                    userInput = String.valueOf(getBusinessObjectByName(csflexType, var, String.valueOf(variable.getData())));
                }
            } else if (variabeType.equalsIgnoreCase(CHOICE_TYPE) || variabeType.equalsIgnoreCase(DRIVEN_TYPE)) {
                flexAttribute = csflexType.getAttribute(var);
                attList = flexAttribute.getAttValueList();
                Collection<String> keys = attList.getSelectableKeys(Locale.getDefault(), true);
                if (keys != null) {
                    for (String enumKey : keys) {
                        if (attList.getValue(enumKey, Locale.getDefault()).equals(String.valueOf(variable.getData())))
                            userInput = enumKey;
                    }
                }
            } else if (variabeType.equalsIgnoreCase(DATE_TYPE)) {
                SimpleDateFormat inputFormat = new SimpleDateFormat(DATE_FORMAT_TYPE);
                if (variable.getData() != null && FormatHelper.hasContent((String) variable.getData())) {
                    Date date = inputFormat.parse((String) variable.getData());
                    userInput = String.valueOf(new Timestamp(date.getTime()));
                }
            } else if (variabeType.equalsIgnoreCase(INTEGER_TYPE)) {
                //userInput = String.valueOf((Double.valueOf((String)variable.getData()).intValue()));
                userInput = Optional.ofNullable(variable.getData())
                        .map(Object::toString)  // Convert Object to String
                        .filter(data -> !data.isEmpty())  // Filter out empty strings
                        .map(data -> {
                            return String.valueOf(Double.valueOf(data).intValue());  // Convert to int and then String
                        })
                        .orElse("0");
            } else {
                userInput = String.valueOf(variable.getData());
            }
        }

        LOGGER.debug("**getValueToCompare-returning:" + userInput);
        return userInput;

    }

    /**
     * This method is to retrieve the Business Object selected on the Cost Sheet
     *
     * @param csflexType
     * @param attKey
     * @param data
     * @return
     */
    public static WTObject getBusinessObjectByName(FlexType csflexType, String attKey, String data) {
        LOGGER.debug("**getBusinessObjectByName:" + csflexType.getTypeName() + ":" + attKey + ":" + data);
        LCSLifecycleManaged businessObject = null;
        try {
            PreparedQueryStatement statement = new PreparedQueryStatement();
            FlexType BOType = csflexType.getAttribute(attKey).getRefType();
            statement.appendFromTable(LCSLifecycleManaged.class);
            statement.appendSelectColumn(new QueryColumn(LCSLifecycleManaged.class, CS_HIGHLEVEL_BO_OBJECT_IDENTIFIER_LITERAL));
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, BOType.getAttribute(BO_NAME).getColumnDescriptorName()), data, COND_ASSIGN_OP));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, LCSQuery.TYPED_BRANCH_ID), BOType.getIdNumber(), COND_ASSIGN_OP));
            SearchResults results = LCSQuery.runDirectQuery(statement);
            if (results.getResultsFound() > 0) {
                FlexObject flexBO = (FlexObject) results.getResults().elementAt(0);
                businessObject = (LCSLifecycleManaged) LCSQuery.findObjectById(csflexType.getAttribute(attKey).getRefDefinition().getIdPrefix() + flexBO.getString(CS_HIGHLEVEL_BO_IDA2A2));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        LOGGER.debug("**Returning businessObject:" + businessObject);
        return businessObject;
    }

    /**
     * @param strCostSheetNumber
     * @param variable
     * @return
     */
    public LCSCostSheet getCostSheetObject(String strCostSheetNumber, Variable variable) {

        LCSProductCostSheet objProductCostSheet = null;
        try {
            LOGGER.debug("**getCostSheetObject:" + strCostSheetNumber);
            SearchResults costObjResults = getCostSheetObjectResult(strCostSheetNumber);
            LOGGER.debug("**costObjResults:" + costObjResults.getResults());
            if (costObjResults.getResults() != null && costObjResults.getResults().size() > 0) {
                for (FlexObject csFlexObj : (Collection<FlexObject>) costObjResults.getResults()) {
                    objProductCostSheet = (LCSProductCostSheet) LCSQuery.findObjectById(SparcCostingConstants.VR_COST_SHEET_STR + csFlexObj.getString(SparcCostingConstants.COST_BRANCH_ID));
                }
            } else {
                this.context.addToCorrespondenceLog(new TCExceptionRuntime("Cost Sheet doesn't exist with the number provided in CostSheet Number field at Row:" + variable.getCoordinate().getRow(),
                        TCExceptionRuntime.Type.ERROR));
                this.countErrors++;
            }
        } catch (WTException e) {
            this.context.addToCorrespondenceLog(new TCExceptionRuntime("Cost Sheet doesn't exist with the number provided in CostSheet Number field at Row:" + variable.getCoordinate().getRow(),
                    TCExceptionRuntime.Type.ERROR));
            this.countErrors++;
            e.printStackTrace();

        }
        LOGGER.debug("**Returning getCostSheetObject:" + objProductCostSheet);
        return objProductCostSheet;
    }

    /**
     * @param costSheetNumber
     * @return
     * @throws WTException
     */
    public static SearchResults getCostSheetObjectResult(String costSheetNumber) throws WTException {
        costSheetNumber = getLongNumber(costSheetNumber);
        LOGGER.debug("**getCostSheetObjectResult:" + costSheetNumber);
        PreparedQueryStatement statement = new PreparedQueryStatement();
        SearchResults results = null;
        FlexType costSheetFlexType = FlexTypeCache.getFlexTypeFromPath(CS_HIGHLEVEL_SPARC_COSTSHEET_TYPE);
        statement.appendFromTable(SparcCostingConstants.PRODUCT_COST_SHEET, SparcCostingConstants.LCS_COST_SHEET);
        statement.appendSelectColumn(new QueryColumn(SparcCostingConstants.LCS_COST_SHEET, LCSProductCostSheet.class, SparcCostingConstants.ITERATION_INFO_BRANCH_ID));
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(SparcCostingConstants.LCS_COST_SHEET, LCSProductCostSheet.class, SparcCostingConstants.ITERATION_INFO_LATEST), "1", "="));
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(SparcCostingConstants.LCS_COST_SHEET, LCSProductCostSheet.class,
                costSheetFlexType.getAttribute(SparcCostingConstants.COST_SHEET_NUMBER).getColumnDescriptorName()), costSheetNumber, "="));
        LOGGER.debug("**getCostSheetObjectResult Query Statement:" + statement);
        results = LCSQuery.runDirectQuery(statement);

        return results;

    }

    /**
     * @param costSheetNumber
     * @return
     */
    public static String getLongNumber(final String costSheetNumber) {
        double parsedValue = Double.parseDouble(costSheetNumber);
        long exactValue = (long) parsedValue;
        return String.valueOf(exactValue);
    }

    /**
     * @return
     */
    public static String setDateForBusinessObject() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CUSTOM_DATE_TYPE);
        String formattedDate = currentDate.format(formatter);
        return formattedDate;
    }

    /**
     * @param dataSheet
     * @param uploadSummaryFile
     * @param strBusOid
     * @param documentName
     * @param docFlexTypePath
     */
    public static void uploadDataAndSummaryFiles(String dataSheet, String uploadSummaryFile, String strBusOid, String documentName, String docFlexTypePath) {
        LCSDocumentClientModel documentModel = new LCSDocumentClientModel();
        final LCSDocumentLogic documentLogic = new LCSDocumentLogic();
        Collection<String> docIds = new ArrayList<>();
        String uploadedFilesPath = "";
        Transaction trx = new Transaction();
        try {
            trx.start();
            FlexType objFlexType = FlexTypeCache.getFlexTypeFromPath(docFlexTypePath);
            documentModel.setFlexType(objFlexType);
            documentModel.setValue(DOC_NAME, documentName);
            documentModel.setName(documentName);
            documentModel.setContentComment(CS_HIGHLEVEL_PRIMARY_CONTENT_COMMENT);
            int numberOfFiles = 10;
            String[] secondaryContentFiles = new String[numberOfFiles];
            String[] secondaryContentComments = new String[numberOfFiles];
            documentModel.setSecondaryContentFiles(secondaryContentFiles);
            documentModel.setSecondaryContentComments(secondaryContentComments);
            documentModel.save();

            String oid = FormatHelper.getVersionId(documentModel.getBusinessObject());
            LCSDocument objectDOc = (LCSDocument) LCSQuery.findObjectById(oid);
            documentLogic.associateContent(objectDOc, dataSheet);
            if (FormatHelper.hasContent(uploadSummaryFile)) {
                secondaryContentFiles[0] = uploadSummaryFile;
                secondaryContentComments[0] = CS_HIGHLEVEL_SECONDARY_CONTENT_COMMENT;
                documentLogic.associateSecondaryContents(objectDOc, secondaryContentFiles, secondaryContentComments);
            }
            objectDOc = (LCSDocument) VersionHelper.latestIterationOf(objectDOc);

            docIds.add(FormatHelper.getObjectId(objectDOc.getMaster()));
            LCSLifecycleManaged detailBusinessObject = (LCSLifecycleManaged) LCSQuery.findObjectById(strBusOid);
            detailBusinessObject.setValue(SC_STATUS_KEY, SC_STATUS_COMPLETED);
            detailBusinessObject.setValue(END_DATE_KEY, setDateForBusinessObject());
            LCSLifecycleManagedLogic.persist(detailBusinessObject, true);
            documentLogic.associateDocuments(strBusOid, docIds);
            DeleteFileHelper dFH = new DeleteFileHelper();
            uploadedFilesPath = wt_home + UPLOADED_PATH;
            LOGGER.debug("SPARCHighLevelCostSheetUploadHelper Uploaded file getting removed-----" + dataSheet);
            LOGGER.debug("SPARCHighLevelCostSheetUploadHelper Uploaded Summary File-------------------------" + uploadSummaryFile);
            LOGGER.debug("SPARCHighLevelCostSheetUploadHelper Uploaded files Path------------------" + uploadedFilesPath);
            //Files uploaded will not be deleted immediately and it will delete the older files available in the lcsDocuments location on each upload
            dFH.deleteOldFiles(uploadedFilesPath, TIME_TO_LIVE);
            DeleteFileHelper.deleteFile(dataSheet);
            DeleteFileHelper.deleteFileArray(secondaryContentFiles);
            trx.commit();
            trx = null;
        } catch (Exception ex) {
            LOGGER.debug("SPARCHighLevelCostSheetUploadHelper Exception while getting the CostSheets---" + ex.getMessage());
            ex.printStackTrace();
        } finally {
            if (trx != null) {
                trx.rollback();
            }
        }
    }


}
