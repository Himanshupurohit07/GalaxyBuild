package com.sparc.wc.exports.services.impl;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSCostSheetMaster;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.specification.FlexSpecMaster;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.VersionHelper;
import com.sparc.tc.abstractions.Interpreter;
import com.sparc.tc.abstractions.TemplateBuilder;
import com.sparc.tc.abstractions.ValueSupplier;
import com.sparc.tc.domain.AttributeValue;
import com.sparc.tc.domain.Coordinate;
import com.sparc.tc.domain.InterpreterContext;
import com.sparc.tc.domain.PlaceHolder;
import com.sparc.tc.domain.Template;
import com.sparc.tc.domain.TemplateBuilderContext;
import com.sparc.tc.domain.WorkbookFileSystem;
import com.sparc.tc.exceptions.TCException;
import com.sparc.tc.util.StringUtils;
import com.sparc.wc.exports.constants.SparcExportConstants;
import com.sparc.wc.exports.domain.SparcLineSheetData;
import com.sparc.wc.exports.domain.SparcLineSheetData.CostSheet;
import com.sparc.wc.exports.repository.CostSheetRepository;
import com.sparc.wc.exports.services.SparcLineSheetExporter;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.sparc.wc.integration.util.SparcQueryUtil;
import com.sparc.wc.tc.domain.ObjectsCluster;

import wt.log4j.LogR;
import wt.util.WTException;


public class SparcHighLevelCostSheetExporterImpl implements SparcLineSheetExporter {

    private static final Logger LOGGER = LogR.getLogger(SparcHighLevelCostSheetExporterImpl.class.getName());
    private static final String             ROOT_DIRECTORY          = "HighLevel_Cost_Sheets_download.zip";
    private static final String             FILE_DATE_FORMAT        = "MMddyyyy_HHmmss";
    public static final  String             EMPTY_FIELD_PLACEHOLDER = "_";
    private final        Interpreter        interpreter;
    private final        TemplateBuilder    templateBuilder;
    private final        List<Short>        sheets;
    private final        SparcLineSheetData lineSheetData;
    private final        Template           template;
    private Boolean isAppColorContext = true;

    @SuppressWarnings("all")
    public SparcHighLevelCostSheetExporterImpl(final Interpreter interpreter, final TemplateBuilder templateBuilder, final List<Short> sheets, final SparcLineSheetData lineSheetData, final Template template, final Boolean isAppColorContext) {
        this.interpreter = interpreter;
        this.templateBuilder = templateBuilder;
        this.sheets = sheets;
        this.lineSheetData = lineSheetData;
        this.template = template;
        this.isAppColorContext = isAppColorContext;
    }
    @Override
    public WorkbookFileSystem export() throws TCException {
        LOGGER.debug("Exporting cost-sheet summary download start");
        final WorkbookFileSystem wfs = new WorkbookFileSystem();
        wfs.touchDirectory(ROOT_DIRECTORY, true, null);
        LOGGER.debug("lineSheetData.getCostSheetList()---------------"+lineSheetData.getCostSheetList());
        if (lineSheetData.getCostSheetList() == null || lineSheetData.getCostSheetList().isEmpty()) {
            throw new TCException("No cost sheet has been selected for exporting");
        }
        List<CostSheet> allCostSheets = lineSheetData.getCostSheetList();
        
        HashMap<LCSSupplier,HashSet<LCSProductCostSheet>> vendorCSMap = new HashMap<LCSSupplier,HashSet<LCSProductCostSheet>>();
        HashSet<LCSProductCostSheet> tempList = null;
        	for(CostSheet costSheet : lineSheetData.getCostSheetList()) {
        		LOGGER.debug("**proessing started:"+costSheet);
        		LCSCostSheetMaster costSheetMaster = SparcQueryUtil.findObjectById(costSheet.getCostSheetOid(), false);
        		LCSProductCostSheet csObject = (LCSProductCostSheet)SparcQueryUtil.findLatestIterationOf(costSheetMaster);
				if(!csObject.getFlexType().getFullName(true).contains("scApparelHighLevel")){
                    System.out.println("This is the Break down Cost sheet so we are skipping it");
                    continue;
                }				
            if(!this.isAppColorContext && csObject.getApplicableColorNames() != null){
                System.out.println("We are skipping the costsheets where applicable colorways is associated from product context");
                continue;
            }
          
        		LOGGER.debug("**csObject:"+csObject);
        		LCSSourcingConfig srcConfig = (LCSSourcingConfig)SparcQueryUtil.findLatestIterationOf(csObject.getSourcingConfigMaster());
        		LCSSupplier vendor = (LCSSupplier) SparcIntegrationUtil.getValueFrom(srcConfig, SparcExportConstants.SOURCING_CONFIG_VENDOR_INTERNAL_NAME);
        		if(vendor!=null) {
        			tempList = null;
        			if(vendorCSMap.get(vendor) == null || vendorCSMap.get(vendor).size() < 1) {
        				tempList = new HashSet<LCSProductCostSheet>();
        				tempList.add(csObject);
        				vendorCSMap.put(vendor, tempList);
        			}
        		else {
        			tempList = vendorCSMap.get(vendor);
        			tempList.add(csObject);
        			vendorCSMap.put(vendor, tempList);
        		}
        	} else {
        		LOGGER.error("Supplier is not selected on the sourcing for cost sheet:"+srcConfig);
        	} 
        }
        	LOGGER.debug("**vendorCSMap:"+vendorCSMap);
        	 Iterator<LCSSupplier> vendorIDIterator = vendorCSMap.keySet().iterator();
        	 while(vendorIDIterator.hasNext()) {
        		 LCSSupplier vendor = vendorIDIterator.next();
        		 LOGGER.debug("**Started Processing Batch:"+vendor);
        		 processCostSheet(vendorCSMap.get(vendor), vendor, wfs);
        		 LOGGER.debug("**Completed Processing Batch:"+vendor);
        	 }
        	
        return wfs;
    }
    
    protected void addSourcingConfigToCluster(final LCSSourcingConfig sourcingConfig, final ObjectsCluster objectsCluster) throws TCException {
        if (sourcingConfig == null) {
            return;
        }
        objectsCluster.getFlexObjectsCluster().addNonGroupedNode(sourcingConfig, LCSSourcingConfig.class.getName());
        objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.SOURCING_CONFIG_NAME, sourcingConfig.getSourcingConfigName());
        final LCSSupplier finishedGoodsFactorySupplier = (LCSSupplier) SparcIntegrationUtil.getValueFrom(sourcingConfig, SparcExportConstants.SOURCING_CONFIG_FINISHED_GOODS_FACTORY_INTERNAL_NAME);
        if (finishedGoodsFactorySupplier == null) {
            LOGGER.warn("Finished Goods Factory is found empty for the given sourcing configuration:" + sourcingConfig.getSourcingConfigName());
        } else {
            final LCSCountry countryOfOrigin = (LCSCountry) SparcIntegrationUtil.getValueFrom(finishedGoodsFactorySupplier, SparcExportConstants.FINISHED_GOODS_FACTORY_COUNTRY_INTERNAL_NAME);
            if (countryOfOrigin != null) {
                objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.COUNTRY_OF_ORIGIN, countryOfOrigin.getCountryName());
            } else {
                LOGGER.warn("Country of Origin is not defined for given Finished good Factory");
            }
        }
        final LCSSupplier vendor = (LCSSupplier) SparcIntegrationUtil.getValueFrom(sourcingConfig, SparcExportConstants.SOURCING_CONFIG_VENDOR_INTERNAL_NAME);
        if (vendor == null) {
            LOGGER.warn("No Vendor has been found for given sourcing configuration:" + sourcingConfig.getName());
        } else {
            objectsCluster.getFlexObjectsCluster().addNonGroupedNode(vendor, LCSSupplier.class.getName());
            objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.PLM_VENDOR_NAME, vendor.getName());
        }
    }
   
    private void processCostSheet(HashSet<LCSProductCostSheet> costSheetList, LCSSupplier vendor, final WorkbookFileSystem wfs) {
        try {
        	LOGGER.debug("**inside processCostSheet:"+costSheetList+":::"+wfs);
            final ObjectsCluster objectsCluster = createObjectsCluster(costSheetList,vendor);
            LOGGER.debug("Objects-Cluster:" + objectsCluster);
            final ValueSupplier valueSupplier = new ValueSupplier() {
                @Override
                public AttributeValue getValue(PlaceHolder placeHolder) {
                    if (placeHolder == null || placeHolder.getVar() == null || placeHolder.getVar().isEmpty() || objectsCluster == null) {
                        return null;
                    }
                    if (placeHolder.getFlexType() != null && !placeHolder.getFlexType().isEmpty()) {
                        return objectsCluster.getFlexObjectsCluster().getValue(placeHolder);
                    }
                    return objectsCluster.getNonFlexObjectsCluster().getValue(placeHolder);
                }
            };
            final InterpreterContext interpreterContext = new InterpreterContext();
            final List<Coordinate>   coordinates        = interpreter.interpret(template.getTemplate(), sheets, interpreterContext);
            LOGGER.debug("Coordinates:" + coordinates);
            System.out.println("***coordinates:"+coordinates);
            logInterpreterContext(interpreterContext);
            LOGGER.debug("***logInterpreterContext completed:");
            final TemplateBuilderContext templateBuilderContext = new TemplateBuilderContext();
            final XSSFWorkbook           builtWorkbook          = templateBuilder.build(template.getTemplate(), coordinates, valueSupplier, templateBuilderContext);
            logTemplateBuilderContext(templateBuilderContext);
            final String directory = getDirectory(objectsCluster);
            LOGGER.debug("**directory:"+directory);
            wfs.touchDirectory(directory, true, ROOT_DIRECTORY);
            wfs.touchFile(getWorkBookName(objectsCluster), builtWorkbook, ROOT_DIRECTORY, directory);
            LOGGER.debug("**Sheet Creation completed!!!");
        } catch (Exception e) {
            LOGGER.error("Encountered an error while processing vendorBranch:" + vendor + ", error:" + e.getMessage());
        }
    }
    
    private ObjectsCluster createObjectsCluster(HashSet<LCSProductCostSheet> costSheetSet, LCSSupplier vendor) throws TCException, WTException {
        LOGGER.debug("Creating Objects-Cluster");
        final ObjectsCluster objectsCluster = new ObjectsCluster();
        
        if (costSheetSet.isEmpty() || costSheetSet.size()<1) {
            LOGGER.error("CostSheet list is empty, skipping:" + costSheetSet);
            return null;
        }
        if (!StringUtils.hasContent(lineSheetData.getSeasonOid())) {
            LOGGER.error("Season-oid is a required field while exporting data, skipping:" + vendor);
            return null;
        }
        final LCSSeason season = SparcQueryUtil.findObjectById(lineSheetData.getSeasonOid(), false);
        if (season == null) {
            LOGGER.error("Cannot find season with supplied season-oid:" + lineSheetData.getSeasonOid() + ", skipping export of vendor Branch:" + vendor);
            return null;
        }
        
        
        
        LOGGER.debug("before season:"+objectsCluster);
        addSeasonToCluster(season, objectsCluster);
        LOGGER.debug("after season:"+objectsCluster);
        addCostSheetsToCluster(objectsCluster, costSheetSet,season);
        LOGGER.debug("after addCostSheetsToCluster:"+objectsCluster);
       // LCSSupplier vendor = SparcQueryUtil.findObjectById("VR:com.lcs.wc.supplier.LCSSupplier:"+supplier, false);
        
        objectsCluster.getFlexObjectsCluster().addNonGroupedNode(vendor, LCSSupplier.class.getName());
        objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.PLM_VENDOR_NAME, vendor.getName());
        LOGGER.debug("after vendor info:"+objectsCluster);
        addTemporalValuesToObjectsCluster(objectsCluster);
        LOGGER.debug("*after all:"+objectsCluster);
        return objectsCluster;
    }
    
    protected void addTemporalValuesToObjectsCluster(final ObjectsCluster objectsCluster) {
        objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.EXPORTED_TIME, new Date());
    }
    
    protected void addSeasonToCluster(final LCSSeason season, final ObjectsCluster objectsCluster) throws TCException {
        if (season == null) {
            return;
        }
        objectsCluster.getFlexObjectsCluster().addNonGroupedNode(season, LCSSeason.class.getName());
        objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.PLM_SEASON_NAME, season.getName());

    }
    private void addCostSheetsToCluster(ObjectsCluster objectsCluster, HashSet<LCSProductCostSheet> costSheetSet, LCSSeason season) throws TCException, WTException {
    	
    	ArrayList<FlexTyped> csFlexTypedList = new ArrayList<FlexTyped>();
    	ArrayList<FlexTyped> productFlexTypedList = new ArrayList<FlexTyped>();
    	ArrayList<FlexTyped> sourcingConfigsFlexTypedList = new ArrayList<FlexTyped>();
    	ArrayList<FlexTyped> SpecificationFlexTypedList = new ArrayList<FlexTyped>();
    	ArrayList<FlexTyped> seasonFlexTypedList = new ArrayList<FlexTyped>();
    	
    	LCSProduct product = null;
    	FlexSpecMaster specMaster = null;
    	FlexSpecification spec = null;
    	
    	HashMap<String, Object> cosySheetTypeMap = null;
    	HashMap<String, Object> sourcingConfigMap = null;
    	HashMap<String, Object> applicableColorsMap = null;
    	HashMap<String, Object> representativeColorwayMap = null;
    	HashMap<String, Object> lastUpdatedOnMap = null;
    	HashMap<String, Object> lastUpdatedByMap = null;
    	HashMap<String, Object> modeMap = null;
        HashMap<String,Object> primaryMap = null;
    	HashMap<String, Object> countMap = null;
    	LCSSourcingConfig sourcingConfig = null;
    	LCSSKU representativeColorway = null;
        List<String> applicableColors = new ArrayList<String>();
        int i = 0;
    	for(LCSProductCostSheet cs : costSheetSet) {
    		csFlexTypedList.add((FlexTyped)cs);
    		i++;
    		
    		objectsCluster.getFlexObjectsCluster().addNonGroupedNode(cs, LCSProductCostSheet.class.getName());
    		
    		countMap = new HashMap<String, Object>();
    		countMap.put("count", i);
            objectsCluster.getNonFlexObjectsCluster().addGroupedNode("count",countMap);
            
    		modeMap = new HashMap<String, Object>();
    		modeMap.put("mode", "Update");
            objectsCluster.getNonFlexObjectsCluster().addGroupedNode("mode",modeMap);
    		
            
    		product = SparcQueryUtil.findLatestIterationOf(cs.getProductMaster());
            LOGGER.debug("Product version:" + VersionHelper.getFullVersionIdentifierValue(product));
            product = (LCSProduct) VersionHelper.getVersion(product, "A");
            productFlexTypedList.add(product);
            
            sourcingConfig = SparcQueryUtil.findLatestIterationOf(cs.getSourcingConfigMaster());
            sourcingConfigsFlexTypedList.add(sourcingConfig);
            sourcingConfigMap = new HashMap<String, Object>();
            sourcingConfigMap.put("plmSourceName", sourcingConfig.getSourcingConfigName());
            objectsCluster.getNonFlexObjectsCluster().addGroupedNode("plmSourceName",sourcingConfigMap);
            
            specMaster = cs.getSpecificationMaster();
            spec = SparcQueryUtil.findLatestIterationOf(specMaster);
            SpecificationFlexTypedList.add(spec);
            
            seasonFlexTypedList.add(season);
            
            cosySheetTypeMap = new HashMap<String, Object>();
            cosySheetTypeMap.put("costSheetType", cs.getFlexType().getFullNameDisplay(true));
            objectsCluster.getNonFlexObjectsCluster().addGroupedNode("costSheetType",cosySheetTypeMap);
            
            lastUpdatedOnMap = new HashMap<String, Object>();
            Date date = new Date(cs.getPersistInfo().getModifyStamp().getTime());
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            lastUpdatedOnMap.put("lastUpdatedOn",sdf.format(date)+" GMT" );
            objectsCluster.getNonFlexObjectsCluster().addGroupedNode("lastUpdatedOn",lastUpdatedOnMap);
            
            lastUpdatedByMap = new HashMap<String, Object>();
            lastUpdatedByMap.put("lastUpdatedBy", cs.getModifierFullName());
            objectsCluster.getNonFlexObjectsCluster().addGroupedNode("lastUpdatedBy",lastUpdatedByMap);
            
            applicableColors = Optional.ofNullable(cs.getApplicableColorNames())
                    .filter(s -> !s.trim().isEmpty())
                    .map(s -> Arrays.stream(s.split(SparcExportConstants.MOA_DELIMITER))
                            .map(String::trim)
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
            LOGGER.debug("**applicableColors:"+applicableColors);
            applicableColorsMap = new HashMap<String, Object>();
            applicableColorsMap.put("costSheetSelectedColorwayNames", applicableColors);
            objectsCluster.getNonFlexObjectsCluster().addGroupedNode(SparcExportConstants.COST_SHEET_SELECTED_COLORWAY_NAMES, applicableColorsMap);
           
            representativeColorway = CostSheetRepository.getRepresentativeColorwayOfCostSheet(cs);
            LOGGER.debug("**representativeColorway:"+representativeColorway);
            representativeColorwayMap = new HashMap<String, Object>();
            if(representativeColorway!=null) {
            	representativeColorwayMap.put("representativeColorwayName",  SparcIntegrationUtil.getValueFrom(representativeColorway, "skuName"));
            }else {
            	representativeColorwayMap.put("representativeColorwayName",  "");
            }
            
                objectsCluster.getNonFlexObjectsCluster().addGroupedNode(SparcExportConstants.REPRESENTATIVE_COLORWAY_NAME,representativeColorwayMap);
             
            primaryMap = new HashMap<String, Object>();
            if(cs.isPrimaryCostSheet()) {
			    primaryMap.put("isPrimary", "Yes");
            }
            else{
			    primaryMap.put("isPrimary", "No");
            }
			
		    objectsCluster.getNonFlexObjectsCluster().addGroupedNode("isPrimary",primaryMap);


        }
    	 objectsCluster.getFlexObjectsCluster().addGroupedNode("cost_sheets", csFlexTypedList, LCSProductCostSheet.class.getName());
    	 objectsCluster.getFlexObjectsCluster().addGroupedNode("products", productFlexTypedList, LCSProduct.class.getName());
    	 objectsCluster.getFlexObjectsCluster().addGroupedNode("sourcingConfigs", sourcingConfigsFlexTypedList, LCSSourcingConfig.class.getName());
    	 objectsCluster.getFlexObjectsCluster().addGroupedNode("seasons", seasonFlexTypedList, LCSSeason.class.getName());
    	 objectsCluster.getFlexObjectsCluster().addGroupedNode("specs", SpecificationFlexTypedList, FlexSpecification.class.getName());
    	 
    	 
    }
    protected void logInterpreterContext(final InterpreterContext interpreterContext) {
        if (interpreterContext.getInstructionErrors() != null && !interpreterContext.getInstructionErrors().isEmpty()) {
            LOGGER.error("Following errors are encountered while interpreting the template:\n" + interpreterContext.getInstructionErrors());
        }
        if (interpreterContext.getInstructionWarnings() != null && !interpreterContext.getInstructionWarnings().isEmpty()) {
            LOGGER.error("Following warnings are encountered while interpreting the template:\n" + interpreterContext.getInstructionWarnings());
        }
    }

    protected void logTemplateBuilderContext(final TemplateBuilderContext templateBuilderContext) {
        if (templateBuilderContext.getCopyErrors() != null && !templateBuilderContext.getCopyErrors().isEmpty()) {
            LOGGER.error("Following errors are encountered in template-builder:\n" + templateBuilderContext.getCopyErrors());
        }
        if (templateBuilderContext.getCopyWarnings() != null && !templateBuilderContext.getCopyWarnings().isEmpty()) {
            LOGGER.error("Following warning are encountered in template-builder:\n" + templateBuilderContext.getCopyWarnings());
        }
    }
    private String getDirectory(final ObjectsCluster objectsCluster) {
        final String vendorDirectory = getVendorDirectory(objectsCluster);
        return vendorDirectory + ".zip";
    }
    private String getVendorDirectory(final ObjectsCluster objectsCluster) {
        final StringBuilder sb = new StringBuilder();
        appendField(SparcExportConstants.PLM_VENDOR_NAME, sb, objectsCluster);
        if (sb.toString().endsWith(EMPTY_FIELD_PLACEHOLDER)) {
            sb.delete(sb.length() - EMPTY_FIELD_PLACEHOLDER.length(), sb.length());
        }
        return sb.toString().replace("/", "_").replace(" ", "_");
    }
    private void appendField(final String field, final StringBuilder sb, final ObjectsCluster objectsCluster) {
        final String fieldValue = getValue(field, objectsCluster);
        if (!StringUtils.hasContent(fieldValue)) {
            LOGGER.warn("Expecting a valid " + field + " to form the cost-sheet file name, but found empty");
            sb.append(EMPTY_FIELD_PLACEHOLDER);
        } else {
            sb.append(fieldValue + EMPTY_FIELD_PLACEHOLDER);
        }
    }
    private String getValue(final String key, final ObjectsCluster objectsCluster) {
        final PlaceHolder placeHolder = new PlaceHolder();
        placeHolder.setVar(key);
        final AttributeValue value = objectsCluster.getNonFlexObjectsCluster().getValue(placeHolder);
        if (value == null || value.getData() == null) {
            return null;
        }
        return value.getData().toString();
    }
    private String getWorkBookName(final ObjectsCluster objectsCluster) {
        final String        costSheetFullQualifiedName = getCostSheetFullQualifiedName(objectsCluster);
        final StringBuilder sb                         = new StringBuilder();
        sb.append(costSheetFullQualifiedName);
        appendField(SparcExportConstants.PLM_COST_SHEET_NAME, sb, objectsCluster);
        appendField(SparcExportConstants.PLM_COST_SHEET_NUMBER, sb, objectsCluster);
        return sb.toString() + getFormattedDate(objectsCluster) + ".xlsx";
    }
    private String getCostSheetFullQualifiedName(final ObjectsCluster objectsCluster) {
        final StringBuilder sb = new StringBuilder();
        appendField(SparcExportConstants.PLM_SEASON_NAME, sb, objectsCluster);
        appendField(SparcExportConstants.PLM_VENDOR_NAME, sb, objectsCluster);
        sb.append("CostSheet_HighLevel_");
        return sb.toString().replace("/", "_").replace(" ", "_");
    }
    private String getFormattedDate(final ObjectsCluster objectsCluster) {
        final SimpleDateFormat sdf         = new SimpleDateFormat(FILE_DATE_FORMAT);
        final PlaceHolder      placeHolder = new PlaceHolder();
        placeHolder.setVar(SparcExportConstants.EXPORTED_TIME);
        final AttributeValue value = objectsCluster.getNonFlexObjectsCluster().getValue(placeHolder);
        if (value == null || value.getData() == null) {
            LOGGER.error("Please add export time while creating objects cluster");
            return sdf.format(new Date());
        } else {
            return sdf.format(value.getData());
        }
    }
}
