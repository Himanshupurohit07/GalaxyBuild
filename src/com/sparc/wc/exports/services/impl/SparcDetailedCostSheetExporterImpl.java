package com.sparc.wc.exports.services.impl;

import com.ibm.icu.text.SimpleDateFormat;
import com.lcs.wc.flexbom.FlexBOMLink;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.flexbom._FlexBOMLink;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.material.LCSMaterialMaster;
import com.lcs.wc.material.LCSMaterialQuery;
import com.lcs.wc.material.LCSMaterialSupplier;
import com.lcs.wc.material.LCSMaterialSupplierQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSCostSheetMaster;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.specification.FlexSpecMaster;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.supplier.LCSSupplierMaster;
import com.lcs.wc.util.FormatHelper;
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
import com.sparc.wc.exports.services.SparcLineSheetExporter;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.sparc.wc.integration.util.SparcQueryUtil;
import com.sparc.wc.tc.domain.ObjectsCluster;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import wt.log4j.LogR;
import wt.util.WTException;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SparcDetailedCostSheetExporterImpl extends SparcExporterCommon implements SparcLineSheetExporter {
    private static final Logger             LOGGER                  = LogR.getLogger(SparcDetailedCostSheetExporterImpl.class.getName());
    //<editor-fold defaultstate="collapsed" desc="delombok">
    private static final String             BOM_GROUP               = "BOM_GROUP";
    private static final String             ROOT_DIRECTORY          = "Detailed_Cost_Sheets_download.zip";
    private static final String             FILE_DATE_FORMAT        = "MMddyyyy_HHmmss";
    public static final  String             EMPTY_FIELD_PLACEHOLDER = "_";
    public static final  String             NAME                    = "name";
    private final        Interpreter        interpreter;
    private final        TemplateBuilder    templateBuilder;
    private final        List<Short>        sheets;
    private final        SparcLineSheetData lineSheetData;
    private final        Template           template;
    //</editor-fold>

    @Override
    public WorkbookFileSystem export() throws TCException {
        LOGGER.debug("Exporting cost-sheet detailed download");
        final WorkbookFileSystem wfs = new WorkbookFileSystem();
        wfs.touchDirectory(ROOT_DIRECTORY, true, null);
        if (lineSheetData.getCostSheetList() == null || lineSheetData.getCostSheetList().isEmpty()) {
            throw new TCException("No cost sheet has been selected for exporting");
        }
        lineSheetData.getCostSheetList().stream().forEach(costSheet -> {
            processCostSheet(costSheet, wfs);
        });
        return wfs;
    }

    private void processCostSheet(final SparcLineSheetData.CostSheet costSheet, final WorkbookFileSystem wfs) {
        try {
            LOGGER.debug("Processing cost-sheet:" + costSheet.getCostSheetOid());
            final ObjectsCluster objectsCluster = createObjectsCluster(costSheet);
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
            logInterpreterContext(interpreterContext);
            final TemplateBuilderContext templateBuilderContext = new TemplateBuilderContext();
            final XSSFWorkbook           builtWorkbook          = templateBuilder.build(template.getTemplate(), coordinates, valueSupplier, templateBuilderContext);
            logTemplateBuilderContext(templateBuilderContext);
            final String directory = getDirectory(objectsCluster);
            LOGGER.debug("Export directory for cost-sheet:" + costSheet.getCostSheetOid() + ", dir:" + directory);
            wfs.touchDirectory(directory, true, ROOT_DIRECTORY);
            wfs.touchFile(getWorkBookName(objectsCluster), builtWorkbook, ROOT_DIRECTORY, directory);
        } catch (Exception e) {
            LOGGER.error("Encountered an error while processing cost-sheet:" + costSheet + ", error:" + e.getMessage());
        }
    }

    private String getWorkBookName(final ObjectsCluster objectsCluster) {
        final String        costSheetFullQualifiedName = getCostSheetFullQualifiedName(objectsCluster);
        final StringBuilder sb                         = new StringBuilder();
        sb.append(costSheetFullQualifiedName);
        appendField(SparcExportConstants.PLM_COST_SHEET_NAME, sb, objectsCluster);
        appendField(SparcExportConstants.PLM_COST_SHEET_NUMBER, sb, objectsCluster);
        return sb.toString() + getFormattedDate(objectsCluster) + ".xlsx";
    }

    private String getDirectory(final ObjectsCluster objectsCluster) {
        final String vendorDirectory = getVendorDirectory(objectsCluster);
        return vendorDirectory + ".zip";
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

    private String getVendorDirectory(final ObjectsCluster objectsCluster) {
        final StringBuilder sb = new StringBuilder();
        appendField(SparcExportConstants.PLM_VENDOR_NAME, sb, objectsCluster);
        if (sb.toString().endsWith(EMPTY_FIELD_PLACEHOLDER)) {
            sb.delete(sb.length() - EMPTY_FIELD_PLACEHOLDER.length(), sb.length());
        }
        return sb.toString().replace("/", "_").replace(" ", "_");
    }

    private String getCostSheetFullQualifiedName(final ObjectsCluster objectsCluster) {
        final StringBuilder sb = new StringBuilder();
        appendField(SparcExportConstants.PLM_PRODUCT_NUMBER, sb, objectsCluster);
        appendField(SparcExportConstants.PLM_PRODUCT_NAME, sb, objectsCluster);
        appendField(SparcExportConstants.PLM_SEASON_NAME, sb, objectsCluster);
        appendField(SparcExportConstants.PLM_VENDOR_NAME, sb, objectsCluster);
        sb.append("Cost_Breakdown_");
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

    private ObjectsCluster createObjectsCluster(final SparcLineSheetData.CostSheet costSheet) throws TCException {
        LOGGER.debug("Creating Objects-Cluster");
        final ObjectsCluster objectsCluster = new ObjectsCluster();
        if (!StringUtils.hasContent(lineSheetData.getSeasonOid())) {
            LOGGER.error("Season-oid is a required field while exporting data, skipping:" + costSheet);
            return null;
        }
        final LCSSeason season = SparcQueryUtil.findObjectById(lineSheetData.getSeasonOid(), false);
        if (season == null) {
            LOGGER.error("Cannot find season with supplied season-oid:" + lineSheetData.getSeasonOid() + ", skipping export of cost-sheet:" + costSheet);
            return null;
        }
        addSeasonToCluster(season, objectsCluster);
        if (!StringUtils.hasContent(costSheet.getCostSheetOid())) {
            LOGGER.error("Cannot export detailed breakdown without a valid cost-sheet-oid, skipping:" + costSheet);
            return null;
        }
        objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.COST_SHEET_ID, costSheet.getCostSheetOid());
        final LCSCostSheetMaster costSheetMaster = SparcQueryUtil.findObjectById(costSheet.getCostSheetOid(), false);
        if (costSheetMaster == null) {
            LOGGER.error("Cannot find cost-sheet-master with the given oid:" + costSheet.getCostSheetOid());
        }
        final LCSCostSheet cs = SparcQueryUtil.findLatestIterationOf(costSheetMaster);
        if (cs == null) {
            LOGGER.error("Cannot export detailed breakdown without a valid cost-sheet object, supplied cost-sheet-oid:" + costSheet.getCostSheetOid());
            return null;
        }
        addCostSheetToCluster(cs, objectsCluster);
        final LCSSKUSeasonLink skuSeasonLink = SparcQueryUtil.findObjectById(costSheet.getSkuSeasonOid(), false);
        if (skuSeasonLink == null) {
            LOGGER.warn("No SKU-Season link is specified");
        } else {
            addSkuSeasonLinkToCluster(skuSeasonLink, objectsCluster);
        }
        final LCSProduct product = SparcQueryUtil.findLatestIterationOf(cs.getProductMaster());
        if (product == null) {
            LOGGER.warn("Product was found empty for the cost-sheet with oid:" + costSheet.getCostSheetOid());
        } else {
            addProductToCluster(product, objectsCluster);
        }
        final LCSSourcingConfig sourcingConfig = SparcQueryUtil.findLatestIterationOf(cs.getSourcingConfigMaster());
        if (sourcingConfig == null) {
            LOGGER.warn("Sourcing-Config was found empty for the cost-sheet with oid:" + costSheet.getCostSheetOid());
        } else {
            addSourcingConfigToCluster(sourcingConfig, objectsCluster);
        }
        addSeasonProductSourcingConfigurationCommonDataToCluster(season, product, sourcingConfig, cs, objectsCluster);
        addBomLinksToCluster(cs, objectsCluster);
        addTemporalValuesToObjectsCluster(objectsCluster);
        addUserContextValuesToObjectsCluster(objectsCluster);
        return objectsCluster;
    }

    private void addBomLinksToCluster(final LCSCostSheet costSheet, final ObjectsCluster objectsCluster) throws TCException {
        final FlexSpecMaster specMaster = costSheet.getSpecificationMaster();
        if (specMaster == null) {
            LOGGER.error("Expecting a valid spec-master attached to cost-sheet");
            return;
        }
        final FlexSpecification spec = SparcQueryUtil.findLatestIterationOf(specMaster);
        if (spec == null) {
            LOGGER.error("Cannot fetch a versioned reference of Specification from it\'s master, please check data integrity");
            return;
        }
        objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.SPECIFICATION_NAME, spec.getName());
        final String selectedBom = (String) SparcIntegrationUtil.getValueFrom(costSheet, SparcExportConstants.COST_SHEET_SELECTED_BOM_INTERNAL_NAME);
        if (specMaster == null || !StringUtils.hasContent(selectedBom)) {
            LOGGER.error("Expecting a valid specifications attached to given cost-sheet:" + costSheet.getName() + "/ valid bom selected in BOM-Costs");
            return;
        }
        final FlexBOMPart selectedBomPart = getSelectedBomPart(spec, selectedBom);
        if (selectedBomPart == null) {
            LOGGER.error("Expecting a valid bom-part, but found none");
            return;
        }
        final List<FlexBOMLink> bomLinks = getBomLinks(selectedBomPart);
        if (bomLinks == null || bomLinks.isEmpty()) {
            LOGGER.error("No bomlinks were found associated to the cost-sheet:" + costSheet.getName());
            return;
        }
        addBomLinksMaterialsSuppliersToCluster(objectsCluster, bomLinks);
    }

    private void addBomLinksMaterialsSuppliersToCluster(ObjectsCluster objectsCluster, List<FlexBOMLink> bomLinks) throws TCException {
        final List<FlexTyped> sortedBomLinks = sortBomLinks(bomLinks);
        final List<Object> bomLinkIds = sortedBomLinks.stream().map(bomLink -> {
            return FormatHelper.getNumericObjectIdFromObject((FlexBOMLink) bomLink);
        }).collect(Collectors.toList());
        final List<FlexTyped> materialsOfBomLinks         = getMaterialsOfBomLinks(sortedBomLinks);
        final List<FlexTyped> suppliersOfBomLinks         = getSuppliersOfBomLinks(sortedBomLinks);
        final List<FlexTyped> materialSuppliersOfBomLinks = getMaterialSuppliersOfBomLinks(sortedBomLinks);
        final List<Object>    bomUOMValues                = getBomUOMValues(materialSuppliersOfBomLinks);
        objectsCluster.getFlexObjectsCluster().addGroupedNode(BOM_GROUP, sortedBomLinks, FlexBOMLink.class.getName());
        objectsCluster.getNonFlexObjectsCluster().addGroupedNodeSingleKeyValue(BOM_GROUP, SparcExportConstants.BOM_LINK_ID, bomLinkIds);
        objectsCluster.getNonFlexObjectsCluster().addGroupedNodeSingleKeyValue("bom-uom-group", SparcExportConstants.BOM_UOM, bomUOMValues);
        objectsCluster.getFlexObjectsCluster().addGroupedNode("materials-group", materialsOfBomLinks, LCSMaterial.class.getName());
        objectsCluster.getFlexObjectsCluster().addGroupedNode("suppliers-group", suppliersOfBomLinks, LCSSupplier.class.getName());
        objectsCluster.getFlexObjectsCluster().addGroupedNode("material-suppliers-group", materialSuppliersOfBomLinks, LCSMaterialSupplier.class.getName());
    }

    private List<Object> getBomUOMValues(final List<FlexTyped> materialSuppliers) {
        if (materialSuppliers == null || materialSuppliers.isEmpty()) {
            return new LinkedList<>();
        }
        return materialSuppliers.stream()
                .map(matSupp -> {
                    return SparcIntegrationUtil.getChoiceDisplayValue(matSupp, SparcExportConstants.MATERIAL_SUPPLIER_BOM_UOM_INTERNAL_NAME);
                }).collect(Collectors.toList());
    }

    private List<FlexTyped> getMaterialSuppliersOfBomLinks(final List<FlexTyped> bomLinks) {
        return bomLinks.stream().map(link -> {
            if (link == null) {
                return null;
            }
            final LCSSupplierMaster supplierMaster = ((FlexBOMLink) link).getSupplier();
            if (supplierMaster == null) {
                return null;
            }
            final LCSMaterialMaster materialMaster = ((FlexBOMLink) link).getChild();
            if (materialMaster == null) {
                return null;
            }
            try {
                return LCSMaterialSupplierQuery.findMaterialSupplier(materialMaster, supplierMaster);
            } catch (WTException e) {
                LOGGER.error("Encountered an error while querying for material supplier using material-master and supplier-master, error:" + e.getMessage());
            }
            return null;
        }).collect(Collectors.toList());
    }

    private List<FlexTyped> getSuppliersOfBomLinks(final List<FlexTyped> bomLinks) {
        return bomLinks.stream().map(link -> {
            if (link == null) {
                return null;
            }
            final LCSSupplierMaster supplierMaster = ((FlexBOMLink) link).getSupplier();
            if (supplierMaster == null) {
                return null;
            }
            final LCSSupplier supplier = SparcQueryUtil.findLatestIterationOf(supplierMaster);
            if (supplier == null) {
                return null;
            }
            final String supplierName = (String) SparcIntegrationUtil.getValueFrom(supplier, NAME);
            if (supplierName != null && supplierName.equalsIgnoreCase("placeholder")) {
                removeSupplierPlaceholder(supplier);
            }
            return supplier;
        }).collect(Collectors.toList());
    }

    private void removeSupplierPlaceholder(final LCSSupplier supplier) {
        try {
            supplier.setValue(NAME, "");
        } catch (Exception e) {
            LOGGER.error("Encountered an error while removing placeholder name of the supplier, error:" + e.getMessage());
        }
    }

    private List<FlexTyped> getMaterialsOfBomLinks(final List<FlexTyped> bomLinks) {
        return bomLinks.stream().map(link -> {
            if (link == null) {
                return null;
            }
            final LCSMaterialMaster materialMaster = ((FlexBOMLink) link).getChild();
            if (materialMaster == null) {
                return null;
            }
            return SparcQueryUtil.<LCSMaterial>findLatestIterationOf(materialMaster);
        }).collect(Collectors.toList());
    }

    private List<FlexBOMLink> getBomLinks(final FlexBOMPart bomPart) {
        try {
            final Collection<FlexBOMLink> flexBOMLinks = LCSFlexBOMQuery.findFlexBOMLinks(bomPart, null, null, null, null, null, LCSFlexBOMQuery.EFFECTIVE_ONLY, null, false, null, null, null, null);
            if (flexBOMLinks == null || flexBOMLinks.isEmpty()) {
                LOGGER.warn("Found no bomlinks for the given bom-part:" + bomPart.getName());
                return null;
            }
            return flexBOMLinks.stream().filter(bomLink -> {
                final String objectId = FormatHelper.getObjectId(bomLink.getChild());
                if (SparcExportConstants.INCLUDE_MATERIAL_PLACEHOLDERS_IN_DETAILED_BOM_VIEW) {
                    return true;
                } else {
                    return !objectId.equalsIgnoreCase(LCSMaterialQuery.PLACEHOLDERID);
                }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Encountered an error while fetching BOM-Links from given bom-part, error:" + e.getMessage());
        }
        return null;
    }

    private List<FlexTyped> sortBomLinks(final List<FlexBOMLink> links) {
        final List<FlexTyped> sortedLinks = new LinkedList<>();
        final List<String>    sections    = SparcIntegrationUtil.exportEnumOfAttribute(links.stream().findFirst().orElse(null), "section");
        if (sections == null || sections.isEmpty()) {
            LOGGER.error("Expecting a valid set of sections defined, but found empty, cannot sort bom-links, please check the logic");
            return links.stream().map(link -> (FlexTyped) link).collect(Collectors.toList());
        }
        sections.stream().map(section -> links.stream().filter(link -> {
            final String sectionName = SparcIntegrationUtil.getChoiceDisplayValue(link, "section");
            return sectionName != null && sectionName.equals(section);
        }).collect(Collectors.toList())).filter(unsortedSectionLinks -> !unsortedSectionLinks.isEmpty()).forEach(unsortedSectionLinks -> {
            Collections.sort(unsortedSectionLinks, Comparator.comparingInt(_FlexBOMLink::getSortingNumber));
            sortedLinks.addAll(unsortedSectionLinks);
        });
        return sortedLinks;
    }

    private FlexBOMPart getSelectedBomPart(final FlexSpecification spec, final String selectedBomPart) {
        try {
            final Collection<FlexBOMPart> bomParts = FlexSpecQuery.getSpecComponents(spec, "BOM");
            if (bomParts == null || bomParts.isEmpty()) {
                return null;
            }
            return bomParts.stream().filter(bomPart -> {
                return bomPart != null && bomPart.getName().equals(selectedBomPart);
            }).findFirst().orElse(null);
        } catch (WTException e) {
            LOGGER.error("Encountered an error while fetching bom-parts from given spec, error:" + e.getMessage());
        }
        return null;
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public SparcDetailedCostSheetExporterImpl(final Interpreter interpreter, final TemplateBuilder templateBuilder, final List<Short> sheets, final SparcLineSheetData lineSheetData, final Template template) {
        this.interpreter = interpreter;
        this.templateBuilder = templateBuilder;
        this.sheets = sheets;
        this.lineSheetData = lineSheetData;
        this.template = template;
    }
    //</editor-fold>
}
