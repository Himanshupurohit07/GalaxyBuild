package com.sparc.wc.exports.constants;

import com.lcs.wc.util.LCSProperties;
import com.sparc.wc.exports.utils.SparcStringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class SparcExportConstants {

    private SparcExportConstants() {

    }

    private static final String      PATH_PREFIX                        = "com.sparc.exports";
    public static final  String      MOA_DELIMITER                      = "\\|~\\*~\\|";
    public static final  String      SOURCING_CONFIG_NAME               = "sourcingConfigName";
    public static final  String      REPRESENTATIVE_COLORWAY_NAME       = "representativeColorwayName";
    public static final  String      REPRESENTATIVE_COSTING_SIZE        = "representativeCostingSize";
    public static final  String      PLM_COST_SHEET_NAME                = "plmCostSheetName";
    public static final  String      PLM_COST_SHEET_NUMBER              = "plmCostSheetNumber";
    public static final  String      COST_SHEET_ID                      = "costSheetId";
    public static final  String      COST_SHEET_SELECTED_COLORWAY_NAMES = "costSheetSelectedColorwayNames";
    public static final  String      COST_SHEET_SELECTED_COLORWAY_IDS   = "costSheetSelectedColorwayIds";
    public static final  String      COUNTRY_OF_ORIGIN                  = "countryOfOrigin";
    public static final  String      BOM_LINK_ID                        = "bomLinkId";
    public static final  String      BOM_UOM                            = "bomUOM";
    public static final  String      SPECIFICATION_NAME                 = "specificationName";
    public static final  String      EXPORTED_TIME                      = "exportedTime";
    public static final  String      EXPORTED_USER                      = "exportedUser";
    public static final  String      EXPORTED_USER_FULL_NAME            = "exportedUserFullName";
    public static final  String      EXPORTED_USER_MAIL                 = "exportedUserMail";
    public static final  String      PLM_PRODUCT_NUMBER                 = "plmProductNumber";
    public static final  String      PLM_PRODUCT_NAME                   = "plmProductName";
    public static final  String      PLM_SEASON_NAME                    = "plmSeasonName";
    public static final  String      PLM_VENDOR_NAME                    = "plmVendorName";
    public static final  String      PLM_AGENT_COMMISSION_PERCENT_US    = "agentCommissionPercentUs";
    public static final  String      FLEX_ENUM_EXPORT_PARAM             = "ENUM_EXPORT";
    public static final  String      FLEX_OBJ_REF_LIST_EXPORT_PARAM     = "OBJ_REF_LIST_EXPORT";
    public static final  String      CUSTOM_FORMAT                      = "CUSTOM_FORMAT";
    public static final  String      FLEX_LEVEL                         = "LEVEL";
    public static final  String      FLEX_SCOPE                         = "SCOPE";
    public static final  List<Short> FOOTWEAR_DETAILED_BREAKDOWN_SHEETS = Arrays.stream(LCSProperties.get(PATH_PREFIX + ".footwear.detailed.breakdown.sheets", "0, 1").split(",", -1)).map(String::trim).map(SparcStringUtils::toShort).filter(index -> index >= 0).collect(Collectors.toList());
    public static final  List<Short> APPAREL_DETAILED_BREAKDOWN_SHEETS  = Arrays.stream(LCSProperties.get(PATH_PREFIX + ".apparel.detailed.breakdown.sheets", "0, 1").split(",", -1)).map(String::trim).map(SparcStringUtils::toShort).filter(index -> index >= 0).collect(Collectors.toList());

    public static final List<Short> APPAREL_HIGHLEVEL_SHEETS    = Arrays.stream(LCSProperties.get(PATH_PREFIX + ".apparel.highlevel.sheets", "0,1").split(",", -1)).map(SparcStringUtils::toShort).filter(index -> index >= 0).collect(Collectors.toList());
    public static final List<Short> NONAPPAREL_HIGHLEVEL_SHEETS = Arrays.stream(LCSProperties.get(PATH_PREFIX + ".nonapparel.highlevel.sheets", "0,1").split(",", -1)).map(SparcStringUtils::toShort).filter(index -> index >= 0).collect(Collectors.toList());
    public static final List<Short> LICENSED_HIGHLEVEL_SHEETS   = Arrays.stream(LCSProperties.get(PATH_PREFIX + ".licensed.highlevel.sheets", "0,1").split(",", -1)).map(SparcStringUtils::toShort).filter(index -> index >= 0).collect(Collectors.toList());
    public static final List<Short> HARDWARE_HIGHLEVEL_SHEETS   = Arrays.stream(LCSProperties.get(PATH_PREFIX + ".hardware.highlevel.sheets", "0,1").split(",", -1)).map(SparcStringUtils::toShort).filter(index -> index >= 0).collect(Collectors.toList());

    public static final String  SOURCING_CONFIG_FINISHED_GOODS_FACTORY_INTERNAL_NAME = LCSProperties.get(PATH_PREFIX + ".sourcing.config.finished.goods.factory.internal.name", "scFGFactory");
    public static final String  FINISHED_GOODS_FACTORY_COUNTRY_INTERNAL_NAME         = LCSProperties.get(PATH_PREFIX + ".finished.goods.factory.country.internal.name", "scCountry");
    public static final String  SOURCING_CONFIG_AGENT_OFFICE_INTERNAL_NAME           = LCSProperties.get(PATH_PREFIX + ".sourcing.config.agent.office.internal.name", "scAgentOffice");
    public static final String  SUPPLIER_AGENT_COMMISSION_US_INTERNAL_NAME           = LCSProperties.get(PATH_PREFIX + ".supplier.agent.commission.us.internal.name", "scAgentCommissionPercent");
    public static final String  COLORWAY_NUMBER_INTERNAL_NAME                        = LCSProperties.get(PATH_PREFIX + ".colorway.number.internal.name", "scColorwayNo");
    public static final String  COST_SHEET_SELECTED_BOM_INTERNAL_NAME                = LCSProperties.get(PATH_PREFIX + ".cost.sheet.selected.bom.internal.name", "scBOM");
    public static final String  COST_SHEET_NUMBER_INTERNAL_NAME                      = LCSProperties.get(PATH_PREFIX + ".cost.sheet.number.internal.name", "scCostSheetNumber");
    public static final String  PRODUCT_NUMBER_INTERNAL_NAME                         = LCSProperties.get(PATH_PREFIX + ".product.number.internal.name", "scPLMProductNo");
    public static final String  SOURCING_CONFIG_VENDOR_INTERNAL_NAME                 = LCSProperties.get(PATH_PREFIX + ".sourcing.config.vendor.internal.name", "vendor");
    public static final String  MATERIAL_SUPPLIER_BOM_UOM_INTERNAL_NAME              = LCSProperties.get(PATH_PREFIX + ".material.supplier.bom.uom.internal.name", "scBOMUOM");
    public static final boolean INCLUDE_MATERIAL_PLACEHOLDERS_IN_DETAILED_BOM_VIEW   = LCSProperties.getBoolean(PATH_PREFIX + ".include.material.placeholders.in.detailed.bom.view", true);

    public static final String FOOTWEAR_BREAKDOWN_BO  = "Business Object\\scFootWearBreakDownBO";
    public static final String FOOTWEAR_BREAKDOWN_DOC = "Document\\scFootWearBreakDown";
    public static final String BOM_FOOTWEAR_FLEXTYPE  = "BOM\\Materials\\scFootwear";
}
