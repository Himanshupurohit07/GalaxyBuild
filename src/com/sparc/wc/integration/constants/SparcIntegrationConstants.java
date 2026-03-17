package com.sparc.wc.integration.constants;

import com.lcs.wc.util.LCSProperties;
import com.sparc.wc.integration.domain.SparcPropertyDefinitions;
import com.sparc.wc.integration.domain.SparcTypeAttributeCollection;

import wt.dataservice.DataServiceFactory;
import wt.dataservice.Oracle;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SparcIntegrationConstants {

    public final static int    COLORWAY_NUMBER_LENGTH = 18;
    public final static String PADDING_CHAR           = "0";

    private static final String PROPERTY_PATH_PREFIX = "com.sparc.wc.integration.constants.SparcIntegrationConstants";

    public static final String COLORWAY_INTERNAL_NAME                           = LCSProperties.get(PROPERTY_PATH_PREFIX + ".colorwayId.internal.name", "scColorwayNo");
    public static final String COLORWAY_TO_SEASON_COLORWAY_STATUS_INTERNAL_NAME = LCSProperties.get(PROPERTY_PATH_PREFIX + ".colorwayStatus.internal.name", "scPLMPolorwayStatus");
    public static final String COLORWAY_TO_SEASON_COLORWAY_STATUS_PRODUCTION    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".colorwayStatus.production", "scProduction");
    public static final String COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME      = LCSProperties.get(PROPERTY_PATH_PREFIX + ".sent.to.fc.internal.name", "scSentToFC");
    public static final String COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME     = LCSProperties.get(PROPERTY_PATH_PREFIX + ".sent.to.cap.internal.name", "scSentToCAP");
    public static final String COLORWAY_TO_SEASON_SENT_TO_S4_INTERNAL_NAME      = LCSProperties.get(PROPERTY_PATH_PREFIX + ".sent.to.s4.internal.name", "scSentToS4");

    public static final String      SENT_YES                                         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".sent.yes.internal.name", "scYes");
    public static final String      SENT_NO                                          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".sent.no.internal.name", "scNo");
    public static final Set<String> LUCKY_FC_COLORWAY_STATUSES                       = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".colorway.fc.statuses", "scProduction").split(",", -1)).map(status -> status.trim()).collect(Collectors.toSet());
    public static final Set<String> LUCKY_CAP_COLORWAY_STATUSES                      = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".colorway.cap.statuses", "scProduction, scDevelopment").split(",", -1)).map(status -> status.trim()).collect(Collectors.toSet());
    public static final String      LUCKY_PRODUCT_FLEX_PATH                          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.product.flex.path", "Product\\scApparel\\scLucky");
    public static final String      LUCKY_SEASON_FLEX_PATH                           = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.season.flex.path", "Season\\scLuckySeason");
    public static final String      DEVELOPMENT_SEASON_INTERNAL_NAME                 = LCSProperties.get(PROPERTY_PATH_PREFIX + ".development.season.internal.name", "seasonType");
    public static final String      SEASON_YEAR_INTERNAL_NAME                        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".season.year.internal.name", "year");
    public static final Set<String> PRODUCT_OBJECT_REF_INTERNAL_NAMES                = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".product.object.reference.internal.names", "scLsizerange, scHTSCode1, color").split(",", -1)).map(objRef -> objRef.trim()).collect(Collectors.toSet());
    public static final Set<String> PRODUCT_OBJECT_REF_LOCKED_INTERNAL_NAMES         = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".product.object.reference.locked.internal.names", "scLsizerange").split(",", -1)).map(objRef -> objRef.trim()).collect(Collectors.toSet());
    public static final Set<String> PRODUCT_EXPLICIT_DIVERGENCE_CHECK_INTERNAL_NAMES = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".product.explicit.divergence.check.internal.names", "sclSTYCNT").split(",", -1)).map(objRef -> objRef.trim()).collect(Collectors.toSet());
    public static final String      LUCKY_COLOR_FLEX_TYPE_PATH                       = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.color.flex.type.path", "Color\\scColorwayColors\\scLucky");
    public static final String      LUCKY_COLOR_SENT_TO_FC_INTERNAL_NAME             = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.color.sent.to.fc.internal.name", "scColorSentToFC");
    public static final Set<String> LUCKY_DEVELOPMENT_SEASONS                        = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.development.seasons", "scFall, scHoliday, scSpring, scSummer").split(",", -1))
            .map(season -> season.trim())
            .collect(Collectors.toSet());

    public static final SparcPropertyDefinitions COLORWAY_PAYLOAD_PROPERTY_DEFINITIONS                 = SparcPropertyDefinitions.load(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.colorway.payload.properties.definitions", "seasonName|seasonName|CAP|true|false, seasonType|seasonType|CAP|true|false,  sclSTYDIV|sclSTY_DIV|STYLE|false|true, sclSTYLAB|sclSTY_LAB|FC|false|true, sclSTYCLS|sclSTY_CLS|STYLE|false|true"));
    public static final SparcPropertyDefinitions COLORWAY_PAYLOAD_COLORWAY_PROPERTY_DEFINITIONS        = SparcPropertyDefinitions.load(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.colorway.payload.colorway.properties.definitions", "sclSTYDISflag|sclSTY_DISflagC|FC|true|false, sclSTYCNT|sclSTY_CNT|STYLE|false|false"));
    public static final SparcPropertyDefinitions COLORWAY_PAYLOAD_PRODUCT_SEASON_DEFINITIONS           = SparcPropertyDefinitions.load(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.colorway.payload.product_season.properties.definitions", "scldevstatus|scldevstatus|true|false"));
    public static final SparcPropertyDefinitions SOURCE_TO_SEASON_LINK_HTS_CODE_DEFINITION             = SparcPropertyDefinitions.load(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.colorway.payload.source_season.properties.definitions", "scHTSCodeAssignment|sclSTY_HTS|FC|true|false"));
    public static final SparcPropertyDefinitions COLORWAY_PAYLOAD_COLORWAY_SEASON_DEFINITIONS          = SparcPropertyDefinitions.load(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.colorway.payload.colorway_season.properties.definitions"));

    public static final String                   SOURCE_TO_SEASON_LINK_HTS_WEIGHT_INTERNAL_NAME        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".source.to.season.link.hts.weight.internal.name", "scHTSCode1WeightPercent");
    public static final String                   BUSINESS_OBJECT_HTS_ASSIGNMENT_HTS_CODE_INTERNAL_NAME = LCSProperties.get(PROPERTY_PATH_PREFIX + ".business.object.hts.assignment.hts.code.internal.name", "scHTSCodeAssignment");
    public static final double                   SOURCE_TO_SEASON_LINK_HTS_WEIGHT_THRESHOLD            = LCSProperties.get(PROPERTY_PATH_PREFIX + ".source.to.season.link.hts.weight.threshold", 100);
    public static final String                   COMPOSITION_CONTENT_DELIMITER                         = LCSProperties.get("sparc.composition.delimiter", " ");
    //Plugin related entries for Lucky.
    public static final SparcPropertyDefinitions LUCKY_PRODUCT_PLUGIN_PROPERTY_DEFINITIONS                 = SparcPropertyDefinitions.load(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.product.plugins.properties.definitions"));

    public static final SparcPropertyDefinitions LUCKY_PRODUCT_UPDATE_PROPERTY_DEFINITIONS                 = SparcPropertyDefinitions.load(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.product.update.properties.definitions"));


    public static final SparcPropertyDefinitions LUCKY_COLORWAY_PLUGIN_PROPERTY_DEFINITIONS                = SparcPropertyDefinitions.load(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.colorway.plugins.properties.definitions"));

    public static final SparcPropertyDefinitions LUCKY_PRODUCT_SEASON_PLUGIN_PROPERTY_DEFINITIONS         = SparcPropertyDefinitions.load(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.colorway.plugins.product_season.properties.definitions"));
    public static final SparcPropertyDefinitions LUCKY_COLOR_PLUGIN_PROPERTY_DEFINITIONS                  = SparcPropertyDefinitions.load(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.colors.plugins.properties.definitions"));
    public static final SparcPropertyDefinitions LUCKY_BO_PLUGIN_PROPERTY_DEFINITIONS                     = SparcPropertyDefinitions.load(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.bo.plugins.properties.definitions"));

    public static final SparcPropertyDefinitions LUCKY_COLORWAY_SEASON_PLUGIN_PROPERTY_DEFINITIONS        = SparcPropertyDefinitions.load(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.colorway.colorway_season.plugins.properties.definitions"));

    public static final String LUCKY_COLORWAY_INDEX_API_CALL             = LCSProperties.get(PROPERTY_PATH_PREFIX + ".colorway.index.api.call", "Lucky Colorway Index API Call");
    public static final String LUCKY_COLORWAY_DETAIL_API_CALL            = LCSProperties.get(PROPERTY_PATH_PREFIX + ".colorway.detail.api.call", "Lucky Colorway Detail API Call");
    public static final String LUCKY_COLORWAY_UPDATE_API_CALL            = LCSProperties.get(PROPERTY_PATH_PREFIX + ".colorway.update.api.call", "Lucky Colorway Update API Call");
    public static final String F21_COSTING_API_CALL                      = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.costing.detail.api.call", "F21 Costing API Call");
    public static final String API_CALL_REQUEST_TIMESTAMP_INTERNAL_NAME  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".api.call.request.timestamp.internal.name", "scReqTimestamp");
    public static final String API_CALL_RESPONSE_TIMESTAMP_INTERNAL_NAME = LCSProperties.get(PROPERTY_PATH_PREFIX + ".api.call.response.timestamp.internal.name", "scResTimestamp");
    public static final String API_CALL_RESPONSE_INTERNAL_NAME           = LCSProperties.get(PROPERTY_PATH_PREFIX + ".api.call.response.internal.name", "scResponse");
    public static final String API_CALL_REQUEST_INTERNAL_NAME            = LCSProperties.get(PROPERTY_PATH_PREFIX + ".api.call.request.internal.name", "scRequest");
    public static final String API_CALL_TYPE_INTERNAL_NAME               = LCSProperties.get(PROPERTY_PATH_PREFIX + ".api.call.type.internal.name", "scApiCallType");
    public static final String API_CALL_PRINCIPAL_INTERNAL_NAME          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".api.call.principal.internal.name", "scApiCallPrincipal");
    public static final String API_CALL_STATUS_INTERNAL_NAME             = LCSProperties.get(PROPERTY_PATH_PREFIX + ".api.call.status.internal.name", "scApiCallStatus");
    public static final String API_CALL_MESSAGE_INTERNAL_NAME            = LCSProperties.get(PROPERTY_PATH_PREFIX + ".api.call.message.internal.name", "scApiCallMessage");
    public static final String API_CALL_ERROR_TYPE_INTERNAL_NAME         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".api.call.error_type.internal.name", "scErrorType");
    public static final String API_CALL_COLORWAY_NUMBER_INTERNAL_NAME    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".api.call.colorway_number.internal.name", "scColorwayNumber");
    public static final String API_CALL_COLORWAY_SEASON_INTERNAL_NAME    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".api.call.colorway_season.internal.name", "scColorwaySeason");
    public static final String API_CALL_LOG_ENTRY_ORDER_INTERNAL_NAME    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".api.call.log.entry.order.internal.name", "scLogEntryOrder");
    public static final String LUCKY_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH   = LCSProperties.get(PROPERTY_PATH_PREFIX + ".api.call.log.entry.flex.type.path", "Log Entry\\scLuckyIntegration");

    public static final String PRODUCT_NEW_VS_UPDATE_FLAG_INTERNAL_NAME  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".product.new.vs.update.flag.internal.name", "sclNewColorwayFlag");
    public static final String COLORWAY_NEW_VS_UPDATE_FLAG_INTERNAL_NAME = LCSProperties.get(PROPERTY_PATH_PREFIX + ".product.new.vs.update.flag.internal.name", "sclNewColorwayFlag2");
    public static final String PRODUCT_NEW_VS_UPDATE_FLAG_NEW_VALUE      = LCSProperties.get(PROPERTY_PATH_PREFIX + ".product.new.vs.update.flag.new.value", "scNew");
    public static final String PRODUCT_NEW_VS_UPDATE_FLAG_UPDATE_VALUE   = LCSProperties.get(PROPERTY_PATH_PREFIX + ".product.new.vs.update.flag.update.value", "scupdate");

    public static final String HTS_BUSINESS_OBJECT_FLEX_PATH        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".hts.business.object.flex.path", "Business Object\\scHTSLookUpTable");
    public static final String SIZE_RANGE_BUSINESS_OBJECT_FLEX_PATH = LCSProperties.get(PROPERTY_PATH_PREFIX + ".hts.business.object.flex.path", "Business Object\\scSizeRange");

    public static final String      PRODUCT_HTS_CODE_INTERNAL_NAME                                = LCSProperties.get(PROPERTY_PATH_PREFIX + ".product.hts.code.internal.name", "sclSTYHTS");
    public static final String      PRODUCT_SIZE_RANGE_INTERNAL_NAME                              = LCSProperties.get(PROPERTY_PATH_PREFIX + ".product.hts.code.internal.name", "scLsizerange");
    public static final String      LUCKY_SOURCING_TO_SEASON_CONFIG_HTS_CODE_INTERNAL_NAME        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.sourcing.to.season.config.hts.code.internal.name", "scHTSCode1");
    public static final Set<String> SOURCING_TO_SEASON_CONFIG_HTS_CODE1_CHAIN_INTERNAL_NAMES      = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.colorway.plugins.sourcing_season.hierarchy.properties.definitions", "scHTSCode1, scHTSCode1WeightPercent").split(",", -1)).map(String::trim).collect(Collectors.toSet());
    public static final Set<String> LUCKY_SOURCING_TO_SEASON_CONFIG_HTS_CODE_CHAIN_INTERNAL_NAMES = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.sourcing.to.season.config.hts.code.chain.internal.names", "scHTSCode1, scHTSCode1WeightPercent, scHTSCode2, scHTSCode2WeightPercent, scHTSCode3, scHTSCode3WeightPercent, scHTSCode4, scHTSCode4WeightPercent").split(",", -1)).map(String::trim).collect(Collectors.toSet());
    //Forever 21 Constants

    public static final String F21_SCM_COLORWAY_INDEX_API_CALL          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.colorway.index.api.call", "F21/SCM Colorway Index API Call");
    public static final String F21_SCM_COLORWAY_DETAIL_API_CALL         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.colorway.detail.api.call", "F21/SCM Colorway Detail API Call");
    public static final String F21_SCM_LOGGER_DEBUG_ENABLED             = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.logger.debug.enabled", "true");
    public static final String F21_SCM_INTEGRATION_CONTEXT_FLAG_UPDATED = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.integration_context.flag.updated", "Yes");
    public static final String F21_SCM_INTEGRATION_CONTEXT_FLAG_KEY     = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.integration_context.flag.key", "SCM_SOURCEINTEGRATION_CONTEXT");

    //F21 constants for type paths
    public static final String F21_SCM_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH     = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.api.call.log.entry.flex.type.path", "Log Entry\\scScmIntegration");
    public static final String F21_SCM_SEASON_FLEX_TYPE_PATH                 = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.season.flex.type.path", "Season\\scF21");
    public static final String F21_SCM_SOURCING_CONFIGURATION_FLEX_TYPE_PATH = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.sourcing_configuration.flex.type.path", "Sourcing Configuration");
    public static final String F21_SCM_FLEX_TYPE_PATH_KEY                    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.flex.type.path.key", "scForever21");

    //F21 constants for attribute or Query values
    public static final String F21_SCM_SENT_TO_SCM21_YES          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.sourcing_config.senttoscm.flag.query.yes.value", "Yes");
    public static final String F21_SCM_SENT_TO_SCM21_NO           = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.sourcing_config.senttoscm.flag.query.no.value", "No");
    public static final String F21_SCM_SENT_TO_SCM21_SCYES        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.sourcing_config.senttoscm.flag.yes.value", "scYes");
    public static final String F21_SCM_SENT_TO_SCM21_SCNO         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.sourcing_config.senttoscm.flag.yes.value", "scNo");
    public static final String F21_SCM_SOURCING_STATUS_CONFIRMED  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.sourcing_config.sourcing_status.query.confirmed.value", "confirmed");
    public static final String F21_SCM_SOURCING_STATUS_DROPPED    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.sourcing_config.sourcing_status.query.dropped.value", "dropped");
    public static final String F21_SCM_PRODUCT_UPDATE_FLAG_NEW    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.sourcing_config.product_update_flag.new.value", "NEW");
    public static final String F21_SCM_PRODUCT_UPDATE_FLAG_UPDATE = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.sourcing_config.product_update_flag.update.value", "UPDATE");
    public static final String F21_SCM_MATCHING_SET_NONE          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.product.matching_set.none.value", "0000");
    public static final String F21_SCM_MATCHING_SET_YES           = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.product.matching_set.yes.value", "0010");
    public static final String F21_SCM_MATCHING_SET_NO            = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.product.matching_set.no.value", "0020");
    public static final String F21_SCM_YEAR_ROUND_FLAG            = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.product_season.year_round.value", "0022");
    public static final String F21_SCM_LAUNCH_DATE_FORMAT         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.product.launch_date.format", "MM/dd/yyyy");

    public static final Set<String> F21_SCM_SKIP_DEVELOPMENT_SEASONS = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.skip_development_seasons.value", "scblocks, scadvancedconcept").split(",", -1))
            .map(season -> season.trim())
            .collect(Collectors.toSet());

    public static final Set<String> F21_SCM_OVERRIDE_ALT_SECONDARY_KEY_LIST = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.override_alt_secondary_key_list.value", "seasonType").split(",", -1))
            .map(attr -> attr.trim())
            .collect(Collectors.toSet());

    //F21 constants for attributes Names
    public static final String F21_SCM_SC_SENT_TO_SCM21_INTERNAL_NAME       = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.scSenttoSCM21.internal.name", "scSenttoSCM21");
    public static final String F21_SCM_SC_PRODUCT_UPDATE_FLAG_INTERNAL_NAME = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.scProductUpdateFlag.internal.name", "scProductUpdateFlag");
    public static final String F21_SCM_SC_SOURCING_NO_INTERNAL_NAME         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.scSourcingNo.internal.name", "scSourcingNo");
    public static final String F21_SCM_SC_SOURCING_STATUS_INTERNAL_NAME     = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.scSourcingStatus.internal.name", "scSourcingStatus");
    public static final String F21_SCM_DEVELOPMENT_SEASON_INTERNAL_NAME     = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.seasonType.internal.name", "seasonType");
    public static final String F21_SCM_ALT_SECONDARY_KEY_INTERNAL_NAME      = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.scm.alt_secondary_key.internal.name", "_CUSTOM_scF21SecondaryKey");

    //---
    //Lucky FC Color
    public static final String LUCKY_FC_COLOR_INDEX_API_CALL         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.fc.color.index.api.call", "Lucky FC Color Index API Call");
    public static final String LUCKY_FC_COLOR_DETAIL_API_CALL        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.fc.color.detail.api.call", "Lucky FC Color Detail API Call");
    public static final String LUCKY_FC_COLOR_FLEX_TYPE_PATH         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.fc.color.flex.type.path", "Color\\scColorwayColors\\scLucky");
    public static final String LUCKY_FC_INTEGRATION_CONTEXT_FLAG_KEY = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.fc.color.integration_context.flag.key", "FC_COLOR_INTEGRATION_SAVED");

    //Lucky FC Color FLAG values
    public static final String LUCKY_FC_SENT_TO_FC_NO_VALUE         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.fc.color.sent_to_fc.no.value", "scNo");
    public static final String LUCKY_FC_SENT_TO_FC_YES_VALUE        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.fc.color.sent_to_fc.yes.value", "scYes");
    public static final String LUCKY_FC_NEW_COLOR_FLAG_UPDATE_VALUE = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.fc.color.new_color_flag.update.value", "scupdate");
    public static final String LUCKY_FC_NEW_COLOR_FLAG_NEW_VALUE    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.fc.color.new_color_flag.update.value", "scNew");

    //Lucky FC Color Internal Attributes Names
    public static final String LUCKY_FC_COLOR_DESC_ATTR            = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.fc.color.color_description.attr.name", "scColorDescription");
    public static final String LUCKY_FC_NEW_COLOR_FLAG_ATTR        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.fc.color.new_color_flag.attr.name", "sclNewcolorFlag");
    public static final String LUCKY_FC_COLOR_SENT_TO_FC_FLAG_ATTR = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.fc.color.color_sent_to_fc.attr.name", "scColorSentToFC");
    public static final String LUCKY_FC_COLOR_ID_ATTR              = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.fc.color.fc_color_id.attr.name", "sclCLRCDE");
    public static final String LUCKY_FC_COLOR_NO_ATTR              = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.fc.color.color_no.attr.name", "scColorNo");

    public static final SparcTypeAttributeCollection COLORWAY_UPDATE_ATTRIBUTES = SparcTypeAttributeCollection.load(LCSProperties.get(PROPERTY_PATH_PREFIX + ".colorway.update.attributes", "scMerchDivision|scMerchDivision|PRODUCT|PRODUCT, scMerchDepartment|scMerchDepartment|PRODUCT|PRODUCT"));

    //F21 Costing
    public static final String                       F21_COSTING_SOURCING_CONFIG_NUMBER_INTERNAL_NAME = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.costing.sourcing.config.number.internal.name", "scSourcingNo");
    public static final String                       F21_SOURCING_CONFIG_FLEX_PATH                    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.sourcing.config.flex.path", "Sourcing Configuration");
    public static final SparcTypeAttributeCollection F21_COSTING_PAYLOAD_ATTRIBUTES                   = SparcTypeAttributeCollection.load(LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.costing.payload.attributes", "costSheetName|name||COSTSHEET, costSheetNumber|scCostSheetNumber||COSTSHEET"));
    public static final boolean                      F21_COSTING_INCLUDE_WHAT_IF_COST_SHEETS          = LCSProperties.getBoolean(PROPERTY_PATH_PREFIX + ".f21.costing.include.what.if.cost.sheets", false);

    public static final String      F21_COSTING_VENDOR_COSTING_STATUS_ATTRIBUTE_INTERNAL_NAME     = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.costing.vendor.costing.status.attribute.internal.name", "scVendorCostSheetStatus");
    public static final Set<String> F21_COSTING_VENDOR_COSTING_STATUS_ALLOWED_VALUES              = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.costing.vendor.costing.status.allowed.values", "scVendorSubmitted, scInNegotiation, scNegotiated").split(",", -1))
            .map(token -> token.trim())
            .collect(Collectors.toSet());
    public static final String      F21_COSTING_INTERNAL_COSTSHEET_STATUS_ATTRIBUTE_INTERNAL_NAME = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.costing.internal.costsheet.status.attribute.internal.name", "scCostSheetStatus");
    public static final Set<String> F21_COSTING_INTERNAL_COSTSHEET_STATUS_ALLOWED_VALUES          = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.costing.vendor.costing.status.allowed.values", "scF21InWork, scF21Confirmed, scInwork, scConfirmedbytone, scConfirmedbycc, scLuckyInWork, scLuckyConfirmed").split(",", -1))
            .map(token -> token.trim())
            .collect(Collectors.toSet());

    public static final String F21_COSTING_SEASON_FLEX_PATH  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.costing.season.flex.path", "Season\\scF21");
    public static final String F21_COSTING_PRODUCT_FLEX_PATH = LCSProperties.get(PROPERTY_PATH_PREFIX + ".f21.costing.product.flex.path", "Product\\scApparel\\scForever21");

    //Lucky Costing for FC & CAP
    public static final String LUCKY_COSTING_INDEX_API_CALL      = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.index.api.call", "Lucky Costing Index API Call");
    public static final String LUCKY_COSTING_FC_DETAIL_API_CALL  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.fc.detail.api.call", "Lucky FC Costing Detail API Call");
    public static final String LUCKY_COSTING_CAP_DETAIL_API_CALL = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cap.detail.api.call", "Lucky CAP Costing Detail API Call");
    public static final String LUCKY_COSTING_TIMESTAMP_FORMAT    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.timestamp_format.value", "yyyy-MM-dd hh:mm:ss.SSS");

    //Lucky Costing constants for type paths
    public static final String LUCKY_COSTING_SOURCING_CONFIG_FLEX_TYPE_PATH    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_configuration.flex.type.path", "Sourcing Configuration");
    public static final String LUCKY_COSTING_PRODUCT_FLEX_TYPE_PATH            = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.product.flex.type.path", LUCKY_PRODUCT_FLEX_PATH);
    public static final String LUCKY_COSTING_SEASON_FLEX_TYPE_PATH             = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.season.flex.type.path", LUCKY_SEASON_FLEX_PATH);
    public static final String LUCKY_COSTING_COST_SHEET_FLEX_TYPE_PATH         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.flex.type.path", "Cost Sheet\\scSparc");
    public static final String LUCKY_COSTING_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.api.log_entry.flex.type.path", LUCKY_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
    public static final String LUCKY_COSTING_LUCKY_FLEX_TYPE_KEY               = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.lucky.flex.type.path.key", "scLucky");

    //Lucky Costing constants for attributes Names
    public static final String LUCKY_COSTING_SOURCING_NO_ATTR                   = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.sourcing_no.attr.name", "scSourcingNo");
    public static final String LUCKY_COSTING_SOURCING_STATUS_ATTR               = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.sourcing_status.attr.name", "scSourcingStatus");
    public static final String LUCKY_COSTING_SOURCING_FG_FACTORY_ATTR           = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.finished_goods_factory.attr.name", "scFGFactory");
    public static final String LUCKY_COSTING_SOURCING_VENDOR_ATTR               = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.vendor.attr.name", "vendor");
    public static final String LUCKY_COSTING_SOURCING_HTS_CODE_1_ATTR           = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.hts_code1.attr.name", "scHTSCode1");
    public static final String LUCKY_COSTING_SOURCING_HTS_CODE_1_WEIGHT_ATTR    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.hts_code1_weight.attr.name", "scHTSCode1WeightPercent");
    public static final String LUCKY_COSTING_HTS_ASSIGNMENT_TABLE_CODE_ATTR     = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.business_object.hts_assignment_table.code.attr.name", "scHTSCodeAssignment");
    public static final String LUCKY_COSTING_VENDOR_FC_VENDOR_NO_LUCKY_ATTR     = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.vendor.fc_vendor_no_lucky.attr.name", "scFullCircleVendorNoLucky");
    public static final String LUCKY_COSTING_VENDOR_FC_VENDOR_NO_LUCKY2_ATTR    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.vendor.fc_vendor_no_lucky2.attr.name", "scFullCircleVendorNoLucky2");
    public static final String LUCKY_COSTING_VENDOR_FC_VENDOR_NO_LUCKY3_ATTR    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.vendor.fc_vendor_no_lucky3.attr.name", "scFullCircleVendorNoLucky3");
    public static final String LUCKY_COSTING_VENDOR_LUCKY_INCOTERMS_ATTR        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.vendor.lucky_incoterms.attr.name", "scLIncoterms");
    public static final String LUCKY_COSTING_VENDOR_LUCKY_INCOTERMS2_ATTR       = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.vendor.lucky_incoterms2.attr.name", "scLIncoterms2");
    public static final String LUCKY_COSTING_VENDOR_LUCKY_INCOTERMS3_ATTR       = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.vendor.lucky_incoterms3.attr.name", "scLIncoterms3");
    public static final String LUCKY_COSTING_VENDOR_FG_FACTORY_NAME_ATTR        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.vendor.finished_goods_factory_name.attr.name", "scFinishedGoodsFactoryName");
    public static final String LUCKY_COSTING_PRODUCT_NO_ATTR                    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.product.product_no.attr.name", "scPLMProductNo");
    public static final String LUCKY_COSTING_PRODUCT_NAME_ATTR                  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.product.product_name.attr.name", "productName");
    public static final String LUCKY_COSTING_PRODUCT_LEGACY_STYLE_NUMBER_ATTR   = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.product.legacy_style_number.attr.name", "sclegacymodelnumber");
    public static final String LUCKY_COSTING_COLORWAY_COLOR_ATTR                = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.colorway.color.attr.name", "color");
    public static final String LUCKY_COSTING_COST_SHEET_STATUS_ATTR             = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.cost_sheet_status.attr.name", "scCostSheetStatus");
    public static final String LUCKY_COSTING_COST_SHEET_NAME_ATTR               = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.cost_sheet_name.attr.name", "name");
    public static final String LUCKY_COSTING_COST_SHEET_NUMBER_ATTR             = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.cost_sheet_number.attr.name", "scCostSheetNumber");
    public static final String LUCKY_COSTING_COST_SHEET_MILESTONE_ATTR          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.cost_sheet_milestone.attr.name", "scMilestoneStatus");
    public static final String LUCKY_COSTING_COST_SHEET_INCOTERMS_ATTR          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.cost_sheet_incoterms.attr.name", "scIncoterms");
    public static final String LUCKY_COSTING_COST_SHEET_COUNTRY_OF_ORIGIN_ATTR  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.country_of_origin.attr.name", "scCountryofOrigin");
    public static final String LUCKY_COSTING_COST_SHEET_VENDOR_TOTAL_COST_ATTR  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.vendor_total_cost.attr.name", "scTotalVendorCost");
    public static final String LUCKY_COSTING_COST_SHEET_VENDOR_PROD_COST_ATTR   = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.vendor_product_cost.attr.name", "scVendorProductCost");
    public static final String LUCKY_COSTING_COST_SHEET_OTHER_VENDOR_COSTS_ATTR = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.other_vendor_costs.attr.name", "scUnitsOtherCosts");
    public static final String LUCKY_COSTING_COST_SHEET_AGENT_COMM_PERCENT_ATTR = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.agent_commission_percent.attr.name", "scAgentCommissionPercentUS");
    public static final String LUCKY_COSTING_COST_SHEET_AGENT_COMM_DOLLARS_ATTR = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.agent_commission_dollars.attr.name", "scAgentCommissionDollarsUS");
    public static final String LUCKY_COSTING_COST_SHEET_FREIGHT_DOLLARS_ATTR    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.freight_dollars.attr.name", "scFreightDollarsUS");
    public static final String LUCKY_COSTING_COST_SHEET_DUTY_PERCENT_ATTR       = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.duty_percent.attr.name", "scDutyPercentUS");
    public static final String LUCKY_COSTING_COST_SHEET_TOTAL_DUTY_DOLLARS_ATTR = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.total_duty_dollars.attr.name", "scTotalDutyDollarsUS");
	public static final String LUCKY_COSTING_COST_SHEET_INTERNAL_LOAD_PERCENT_US_ATTR = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.internal_load_percent_us.attr.name", "scInternalLoadPercentUS");
    public static final String LUCKY_COSTING_COST_SHEET_LANDED_COST_ATTR        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.landed_cost.attr.name", "scLandedCostUS");
    public static final String LUCKY_COSTING_COST_SHEET_SHIP_MODE_ATTR          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.ship_mode.attr.name", "scShipMode");
    public static final String LUCKY_COSTING_COST_SENT_TO_FC_ATTR               = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.colorway_season.cost_sent_to_fc.attr.name", "scCostSentToFC");
    public static final String LUCKY_COSTING_COST_SENT_TO_CAP_ATTR              = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.colorway_season.cost_sent_to_cap.attr.name", "scCostSentToCAP");
    public static final String LUCKY_COSTING_COST_SHEET_SENT_TO_FC_ATTR         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.colorway_season.cost_sheet_name_sent_to_fc.attr.name", "scCostSheetFC");
    public static final String LUCKY_COSTING_COST_SHEET_SENT_TO_CAP_ATTR        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.colorway_season.cost_sheet_name_sent_to_cap.attr.name", "scCostSheetCAP");
    public static final String LUCKY_COSTING_SEASON_NAME_ATTR                   = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.season.season_name.attr.name", "seasonName");
    public static final String LUCKY_COSTING_SEASON_DEVELOPMENT_YEAR_ATTR       = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.season.development_year.attr.name", "year");
    public static final String LUCKY_COSTING_SEASON_DEVELOPMENT_SEASON_ATTR     = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.season.development_season.attr.name", "seasonType");
    public static final String LUCKY_COSTING_COLORWAY_NAME_ATTR                 = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.colorway.colorway_name.attr.name", "skuName");
    public static final String LUCKY_COSTING_COLORWAY_NUMBER_ATTR               = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.colorway.colorway_number.attr.name", "scColorwayNo");
    public static final String LUCKY_COSTING_FC_COLOR_ID_ATTR                   = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.color.fc_color_id.attr.name", LUCKY_FC_COLOR_ID_ATTR);
    public static final String LUCKY_COSTING_COLOR_NO_ATTR                      = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.color.color_no.attr.name", "scColorNo");
    public static final String LUCKY_COSTING_COUNTRY_ISO_CODE_ATTR              = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.country.iso_code.attr.name", "scISOCode");
    public static final String LUCKY_COSTING_COLORWAY_SEASON_SENT_TO_FC_ATTR    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.colorway.season.sent_to_fc.attr.name", COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME);
    public static final String LUCKY_COSTING_COLORWAY_SEASON_SENT_TO_CAP_ATTR   = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.colorway.season.sent_to_cap.attr.name", COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME);

    //Lucky Costing FLAG/Attr values
    public static final String LUCKY_COSTING_COST_SHEET_TYPE_PRODUCT_VALUE     = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.cost_sheet_type.product.value", "PRODUCT");
    public static final String LUCKY_COSTING_COST_SHEET_WHATIF_NO_VALUE        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.whatif.no.value", "0");
    public static final String LUCKY_COSTING_COST_SHEET_CURRENCY_CODE_VALUE    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.currency_code.value", "USD");
    public static final String LUCKY_COSTING_SOURCING_STATUS_CONFIRMED_VALUE   = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.sourcing_status.confirmed.value", "confirmed");
    public static final String LUCKY_COSTING_SOURCING_STATUS_DEVELOPMENT_VALUE = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sourcing_config.sourcing_status.development.value", "development");
    public static final String LUCKY_COSTING_SENT_YES_VALUE                    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sent.yes.value", SENT_YES);
    public static final String LUCKY_COSTING_SENT_NO_VALUE                     = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.sent.no.value", SENT_NO);
    public static final String LUCKY_COSTING_PLACEHOLDER_YES_VALUE             = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.placeholder.yes.value", "1");
    public static final String LUCKY_COSTING_PLACEHOLDER_NO_VALUE              = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.placeholder.no.value", "0");
    public static final String LUCKY_COSTING_ACTIVE_YES_VALUE                  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.active.yes.value", "1");
    public static final String LUCKY_COSTING_ACTIVE_NO_VALUE                   = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.active.no.value", "0");
    public static final String LUCKY_COSTING_HTS_CODE_1_WEIGHT_THRESHOLD_VALUE = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.hts_code_1_threshold.value", "100.0");
    public static final String LUCKY_COSTING_HTS_CODE_1_DEFAULT                = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.hts_code_1_default.value", "999999");

    public static final Set<String> LUCKY_COSTING_COST_SHEET_STATUS_CONFIRMED_VALUES = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.cost_sheet_status.confirmed.values", "scConfirmedbytone,scConfirmedbycc,scF21Confirmed,scLuckyConfirmed")
                    .split(",", -1))
            .map(season -> season.trim())
            .collect(Collectors.toSet());

    public static final Set<String> LUCKY_COSTING_COST_SHEET_MILESTONE_FINAL_VALUES = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.cost_sheet.cost_sheet_milestone.final.values", "scFinal,scLuckyFinal,scF21Final")
                    .split(",", -1))
            .map(season -> season.trim())
            .collect(Collectors.toSet());

    public static final Set<String> LUCKY_COSTING_SKIP_DEVELOPMENT_SEASONS_VALUES = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.season.skip_development_seasons.values", "scblocks, scadvancedconcept").split(",", -1))
            .map(season -> season.trim())
            .collect(Collectors.toSet());

    //Lucky Costing Plugin values
    public static final String LUCKY_COSTING_PLUGIN_ATTR_PATH_PREFIX = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.plugin.attributes_path", "com.sparc.wc.integration.lucky.plugins.LuckyCostingPlugin.updateIntegrationFlags");

    //Flex Generic
    public static final String LATEST_FLEX_OBJECT_VERSION         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex_object.latest_version", "A");
    public static final String LATEST_FLEX_OBJECT_ITERATION       = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex_object.latest_iteration", "1");
    public static final String FLEX_OBJECT_EFFECT_LATEST_YES      = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex_object.effect_latest.yes", "1");
    public static final String FLEX_OBJECT_EFFECT_LATEST_NO       = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex_object.effect_latest.no", "0");
    public static final String FLEX_OBJECT_PLACEHOLDER_YES        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex_object.placeholder.yes", "1");
    public static final String FLEX_OBJECT_PLACEHOLDER_NO         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex_object.placeholder.no", "0");
    public static final String FLEX_OBJECT_ACTIVE_YES             = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex_object.active.yes", "1");
    public static final String FLEX_OBJECT_ACTIVE_NO              = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex_object.active.no", "0");
    public static final String FLEX_COST_SHEET_WHATIF_YES         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex.cost_sheet.whatif.yes", "1");
    public static final String FLEX_COST_SHEET_WHATIF_NO          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex.cost_sheet.whatif.no", "0");
    public static final String FLEX_COST_SHEET_TYPE_PRODUCT       = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex.cost_sheet.type.product", "PRODUCT");
    public static final String FLEX_COST_SHEET_TYPE_SKU           = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex.cost_sheet.type.sku", "SKU");
    public static final String FLEX_LOG_ENTRY_DEFAULT_STRING_SIZE = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex.log_entry.default_string_size", "2000");
    public static final String FLEX_PRODUCT_SEASON_LINK_TYPE      = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex.product.season_link.type", "PRODUCT");
    public static final String FLEX_SKU_SEASON_LINK_TYPE          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex.colorway.season_link.type", "SKU");
    public static final String FLEX_SEASON_REMOVED_YES            = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex_object.season_removed.yes", "1");
    public static final String FLEX_SEASON_REMOVED_NO             = LCSProperties.get(PROPERTY_PATH_PREFIX + ".flex_object.season_removed.no", "0");

    //Other
    public static final String SQL_CONCAT_OPERATOR = LCSProperties.get(PROPERTY_PATH_PREFIX + ".sql.overrides.concat_operator", ((DataServiceFactory.getDefault().getDatastore() instanceof Oracle) ? "||" : "+"));

    public static final String INTERFACE_ADMIN_USER_NAME = LCSProperties.get(PROPERTY_PATH_PREFIX + ".interface.admin.user.name", "interfaceadmin");

    //---
    //Aero Constants
    public static final String AERO_ARTICLE_INDEX_API_CALL      = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.article.index.api.call", "Aero Article Index API Call");
    public static final String AERO_ARTICLE_S4_DETAIL_API_CALL  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.article.s4.detail.api.call", "Aero S4 Article Detail API Call");
    public static final String AERO_ARTICLE_CAP_DETAIL_API_CALL = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.article.cap.detail.api.call", "Aero CAP Article Detail API Call");
    public static final String AERO_ARTICLE_CAP_UPDATE_API_CALL = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.article.cap.update.api.call", "Aero CAP Article Update API Call");
    public static final String AERO_COSTING_INDEX_API_CALL      = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.costing.index.api.call", "Aero Costing Index API Call");
    public static final String AERO_COSTING_S4_DETAIL_API_CALL  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.costing.s4.detail.api.call", "Aero S4 Costing Detail API Call");
    public static final String AERO_COSTING_CAP_DETAIL_API_CALL = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.costing.cap.detail.api.call", "Aero CAP Costing Detail API Call");
    public static final String AERO_COMMON_LOGGER_NAME          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.common.logger.name", "AERO_INTEGRATION_COMMON_LOGGER");
    public static final String AERO_ARTICLE_LOGGER_NAME         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.article.logger.name", "AERO_INTEGRATION_ARTICLE_LOGGER");
    public static final String AERO_COSTING_LOGGER_NAME         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.costing.logger.name", "AERO_INTEGRATION_COSTING_LOGGER");
    public static final String AERO_TIMESTAMP_FORMAT            = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.timestamp_format.value", "yyyy-MM-dd hh:mm:ss.SSS");
    public static final String AERO_INTEGRATION_CONTEXT_PREFIX  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.integration_context.prefix", "AERO_INTEGRATION_CALL");

    //Aero constants for type paths
    public static final String AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.api.log_entry.flex.type.path", "Log Entry\\scAeroIntegration");
    public static final String AERO_PRODUCT_FLEX_TYPE_PATH            = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.product.flex.type.path", "Product\\scApparel\\scAeropostale");
    public static final String AERO_SOURCING_CONFIG_FLEX_TYPE_PATH    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.sourcing_configuration.flex.type.path", "Sourcing Configuration");
    public static final String AERO_SEASON_FLEX_TYPE_PATH             = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.season.flex.type.path", "Season\\scAeropostaleSeason");
    public static final String AERO_COLOR_FLEX_TYPE_PATH              = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.color.flex.type.path", "Color\\scColorwayColors\\scAeropostale");
    public static final String AERO_COST_SHEET_FLEX_TYPE_PATH         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.costing.cost_sheet.flex.type.path", "Cost Sheet\\scSparc");
    public static final String AERO_FLEX_TYPE_PATH_KEY                = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.flex.type.path", "scAeropostale");

    //Aero constants for attribute mappings.
    public static final String AERO_OBJECT_PATH_SEPARATOR            = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.object_path_separator", "->");
    public static final String AERO_SEASON_ATTR_GROUP_KEY            = "SEASON";
    public static final String AERO_PRODUCT_ATTR_GROUP_KEY           = "PRODUCT";
    public static final String AERO_COLORWAY_ATTR_GROUP_KEY          = "COLORWAY";
    public static final String AERO_COLORWAY_CAP_ATTR_GROUP_KEY      = "COLORWAY_CAP";
    public static final String AERO_COLORWAY_SEASON_ATTR_GROUP_KEY   = "COLORWAY_SEASON";
    public static final String AERO_PRODUCT_SEASON_ATTR_GROUP_KEY    = "PRODUCT_SEASON";
    public static final String AERO_SOURCING_CONFIG_ATTR_GROUP_KEY   = "SOURCING_CONFIG";
    public static final String AERO_SOURCING_SEASON_ATTR_GROUP_KEY   = "SOURCING_SEASON";
    public static final String AERO_SOURCING_COLORWAY_ATTR_GROUP_KEY = "SOURCING_COLORWAY";
    public static final String AERO_COST_SHEET_ATTR_GROUP_KEY        = "COST_SHEET";
    public static final String AERO_ATTR_KEY_PATH_SEPARATOR          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.attributes_path_separator", ":");

    public static final Map<String, Set<String>> AERO_S4_ARTICLE_PAYLOAD_ATTRIBUTES = new ConcurrentHashMap<>(6);

    static {
        AERO_S4_ARTICLE_PAYLOAD_ATTRIBUTES.put(AERO_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.s4.article.payload.season.attributes", "").split(","))
                .map(seasonAttr -> seasonAttr.trim())
                .collect(Collectors.toSet()));

        AERO_S4_ARTICLE_PAYLOAD_ATTRIBUTES.put(AERO_PRODUCT_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.s4.article.payload.product.attributes", "").split(","))
                .map(productAttr -> productAttr.trim())
                .collect(Collectors.toSet()));

        AERO_S4_ARTICLE_PAYLOAD_ATTRIBUTES.put(AERO_COLORWAY_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.s4.article.payload.colorway.attributes", "").split(","))
                .map(skuAttr -> skuAttr.trim())
                .collect(Collectors.toSet()));

        AERO_S4_ARTICLE_PAYLOAD_ATTRIBUTES.put(AERO_COLORWAY_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.s4.article.payload.colorway_season.attributes", "").split(","))
                .map(skuSeasonAttr -> skuSeasonAttr.trim())
                .collect(Collectors.toSet()));

        AERO_S4_ARTICLE_PAYLOAD_ATTRIBUTES.put(AERO_PRODUCT_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.s4.article.payload.product_season.attributes", "").split(","))
                .map(prodSeasonAttr -> prodSeasonAttr.trim())
                .collect(Collectors.toSet()));

        AERO_S4_ARTICLE_PAYLOAD_ATTRIBUTES.put(AERO_SOURCING_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.s4.article.payload.sourcing_season.attributes", "").split(","))
                .map(srcSeasonAttr -> srcSeasonAttr.trim())
                .collect(Collectors.toSet()));

    }

    public static final Map<String, Set<String>> AERO_CAP_ARTICLE_PAYLOAD_ATTRIBUTES = new ConcurrentHashMap<>(6);

    static {
        AERO_CAP_ARTICLE_PAYLOAD_ATTRIBUTES.put(AERO_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cap.article.payload.season.attributes", "").split(","))
                .map(seasonAttr -> seasonAttr.trim())
                .collect(Collectors.toSet()));

        AERO_CAP_ARTICLE_PAYLOAD_ATTRIBUTES.put(AERO_PRODUCT_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cap.article.payload.product.attributes", "").split(","))
                .map(productAttr -> productAttr.trim())
                .collect(Collectors.toSet()));

        AERO_CAP_ARTICLE_PAYLOAD_ATTRIBUTES.put(AERO_COLORWAY_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cap.article.payload.colorway.attributes", "").split(","))
                .map(skuAttr -> skuAttr.trim())
                .collect(Collectors.toSet()));

        AERO_CAP_ARTICLE_PAYLOAD_ATTRIBUTES.put(AERO_COLORWAY_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cap.article.payload.colorway_season.attributes", "").split(","))
                .map(skuSeasonAttr -> skuSeasonAttr.trim())
                .collect(Collectors.toSet()));

        AERO_CAP_ARTICLE_PAYLOAD_ATTRIBUTES.put(AERO_PRODUCT_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cap.article.payload.product_season.attributes", "").split(","))
                .map(prodSeasonAttr -> prodSeasonAttr.trim())
                .collect(Collectors.toSet()));

        AERO_CAP_ARTICLE_PAYLOAD_ATTRIBUTES.put(AERO_SOURCING_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cap.article.payload.sourcing_season.attributes", "").split(","))
                .map(srcSeasonAttr -> srcSeasonAttr.trim())
                .collect(Collectors.toSet()));

    }

    public static final Map<String, Set<String>> AERO_ARTICLE_PLUGIN_ATTRIBUTES = new ConcurrentHashMap<>(4);

    static {

        AERO_ARTICLE_PLUGIN_ATTRIBUTES.put(AERO_PRODUCT_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.article.plugin.product.attributes", "").split(","))
                .map(productAttr -> productAttr.trim())
                .collect(Collectors.toSet()));

        AERO_ARTICLE_PLUGIN_ATTRIBUTES.put(AERO_COLORWAY_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.article.plugin.colorway.attributes", "").split(","))
                .map(skuAttr -> skuAttr.trim())
                .collect(Collectors.toSet()));

        AERO_ARTICLE_PLUGIN_ATTRIBUTES.put(AERO_COLORWAY_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.article.plugin.colorway_season.attributes", "").split(","))
                .map(skuSeasonAttr -> skuSeasonAttr.trim())
                .collect(Collectors.toSet()));

        AERO_ARTICLE_PLUGIN_ATTRIBUTES.put(AERO_PRODUCT_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.article.plugin.product_season.attributes", "").split(","))
                .map(prodSeasonAttr -> prodSeasonAttr.trim())
                .collect(Collectors.toSet()));

        AERO_ARTICLE_PLUGIN_ATTRIBUTES.put(AERO_SOURCING_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.article.plugin.sourcing_season.attributes", "").split(","))
                .map(srcSeasonAttr -> srcSeasonAttr.trim())
                .collect(Collectors.toSet()));
        AERO_ARTICLE_PLUGIN_ATTRIBUTES.put(AERO_COLORWAY_CAP_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.article.plugin.colorway.cap_season.attributes", "").split(","))
                .map(srcSeasonAttr -> srcSeasonAttr.trim())
                .collect(Collectors.toSet()));

    }

    public static final Map<String, Set<String>> AERO_CAP_ARTICLE_UPDATE_ATTRIBUTES = new ConcurrentHashMap<>(4);

    static {

        AERO_CAP_ARTICLE_UPDATE_ATTRIBUTES.put(AERO_PRODUCT_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cap.article.update.product.attributes", "").split(","))
                .map(productAttr -> productAttr.trim())
                .collect(Collectors.toSet()));

        AERO_CAP_ARTICLE_UPDATE_ATTRIBUTES.put(AERO_COLORWAY_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cap.article.update.colorway.attributes", "").split(","))
                .map(skuAttr -> skuAttr.trim())
                .collect(Collectors.toSet()));

        AERO_CAP_ARTICLE_UPDATE_ATTRIBUTES.put(AERO_COLORWAY_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cap.article.update.colorway_season.attributes", "").split(","))
                .map(skuSeasonAttr -> skuSeasonAttr.trim())
                .collect(Collectors.toSet()));

        AERO_CAP_ARTICLE_UPDATE_ATTRIBUTES.put(AERO_PRODUCT_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cap.article.update.product_season.attributes", "").split(","))
                .map(prodSeasonAttr -> prodSeasonAttr.trim())
                .collect(Collectors.toSet()));

    }

    public static final Map<String, Set<String>> AERO_S4_COSTING_PAYLOAD_ATTRIBUTES = new ConcurrentHashMap<>(9);

    static {
        AERO_S4_COSTING_PAYLOAD_ATTRIBUTES.put(AERO_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.s4.costing.payload.season.attributes", "").split(","))
                .map(seasonAttr -> seasonAttr.trim())
                .collect(Collectors.toSet()));

        AERO_S4_COSTING_PAYLOAD_ATTRIBUTES.put(AERO_COLORWAY_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.s4.costing.payload.colorway.attributes", "").split(","))
                .map(skuAttr -> skuAttr.trim())
                .collect(Collectors.toSet()));

        AERO_S4_COSTING_PAYLOAD_ATTRIBUTES.put(AERO_SOURCING_CONFIG_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.s4.costing.payload.sourcing_config.attributes", "").split(","))
                .map(srcConfigAttr -> srcConfigAttr.trim())
                .collect(Collectors.toSet()));

        AERO_S4_COSTING_PAYLOAD_ATTRIBUTES.put(AERO_SOURCING_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.s4.costing.payload.sourcing_season.attributes", "").split(","))
                .map(srcSeasonAttr -> srcSeasonAttr.trim())
                .collect(Collectors.toSet()));

        AERO_S4_COSTING_PAYLOAD_ATTRIBUTES.put(AERO_COST_SHEET_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.s4.costing.payload.cost_sheet.attributes", "").split(","))
                .map(costSheetAttr -> costSheetAttr.trim())
                .collect(Collectors.toSet()));
    }

    public static final Map<String, Set<String>> AERO_CAP_COSTING_PAYLOAD_ATTRIBUTES = new ConcurrentHashMap<>(9);

    static {
        AERO_CAP_COSTING_PAYLOAD_ATTRIBUTES.put(AERO_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cap.costing.payload.season.attributes", "").split(","))
                .map(seasonAttr -> seasonAttr.trim())
                .collect(Collectors.toSet()));

        AERO_CAP_COSTING_PAYLOAD_ATTRIBUTES.put(AERO_COLORWAY_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cap.costing.payload.colorway.attributes", "").split(","))
                .map(skuAttr -> skuAttr.trim())
                .collect(Collectors.toSet()));

        AERO_CAP_COSTING_PAYLOAD_ATTRIBUTES.put(AERO_SOURCING_CONFIG_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cap.costing.payload.sourcing_config.attributes", "").split(","))
                .map(srcConfigAttr -> srcConfigAttr.trim())
                .collect(Collectors.toSet()));

        AERO_CAP_COSTING_PAYLOAD_ATTRIBUTES.put(AERO_SOURCING_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cap.costing.payload.sourcing_season.attributes", "").split(","))
                .map(srcSeasonAttr -> srcSeasonAttr.trim())
                .collect(Collectors.toSet()));

        AERO_CAP_COSTING_PAYLOAD_ATTRIBUTES.put(AERO_COST_SHEET_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cap.costing.payload.cost_sheet.attributes", "").split(","))
                .map(costSheetAttr -> costSheetAttr.trim())
                .collect(Collectors.toSet()));
    }

    public static final Map<String, Set<String>> AERO_COSTING_PLUGIN_ATTRIBUTES = new ConcurrentHashMap<>(4);

    static {

        AERO_COSTING_PLUGIN_ATTRIBUTES.put(AERO_SOURCING_CONFIG_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.costing.plugin.sourcing_config.attributes", "").split(","))
                .map(productAttr -> productAttr.trim())
                .collect(Collectors.toSet()));

        AERO_COSTING_PLUGIN_ATTRIBUTES.put(AERO_SOURCING_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.costing.plugin.sourcing_season.attributes", "").split(","))
                .map(srcSeasonAttr -> srcSeasonAttr.trim())
                .collect(Collectors.toSet()));

        AERO_COSTING_PLUGIN_ATTRIBUTES.put(AERO_COST_SHEET_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.costing.plugin.cost_sheet.attributes", "").split(","))
                .map(skuAttr -> skuAttr.trim())
                .collect(Collectors.toSet()));

    }

    //Aero constants for attributes Names
    public static final String AERO_COLORWAY_NUMBER_ATTR             = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.colorway.colorway_number.attr.name", COLORWAY_INTERNAL_NAME);
    public static final String AERO_COLORWAY_NAME_ATTR               = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.colorway.colorway_name.attr.name", "skuName");
    public static final String AERO_SEASON_NAME_ATTR                 = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.season.season_name.attr.name", "seasonName");
    public static final String AERO_SEASON_TYPE_ATTR                 = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.season.development_season.attr.name", DEVELOPMENT_SEASON_INTERNAL_NAME);
    public static final String AERO_SEASON_YEAR_ATTR                 = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.season.development_year.attr.name", SEASON_YEAR_INTERNAL_NAME);
    public static final String AERO_SOURCING_CONFIG_NUMBER_ATTR      = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.sourcing_config.sourcing_no.attr.name", "scSourcingNo");
    public static final String AERO_COST_SHEET_NAME_ATTR             = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cost_sheet.cost_sheet_name.attr.name", "name");
    public static final String AERO_COST_SHEET_NUMBER_ATTR           = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cost_sheet.cost_sheet_number.attr.name", "scCostSheetNumber");
    public static final String AERO_PRODUCT_NUMBER_ATTR              = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.product.product_no.attr.name", "scPLMProductNo");
    public static final String AERO_PRODUCT_NAME_ATTR                = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.product.product_name.attr.name", "productName");
    public static final String AERO_PRODUCT_SUB_DEPT_ATTR            = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.product.sub_department.attr.name", "scAeroSubDepartment");
    public static final String AERO_PRODUCT_CLASS_ATTR               = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.product.class.attr.name", "scAeroClass");
    public static final String AERO_SOURCING_HTS_CODE_1_ATTR         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.sourcing_config.hts_code1.attr.name", "scHTSCode1");
    public static final String AERO_SOURCING_HTS_CODE_1_WEIGHT_ATTR  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.sourcing_config.hts_code1_weight.attr.name", "scHTSCode1WeightPercent");
    public static final String AERO_HTS_ASSIGNMENT_TABLE_CODE_ATTR   = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.business_object.hts_assignment_table.code.attr.name", "scHTSCodeAssignment");
    public static final String AERO_HTS_CODE_PLACEHOLDER_ATTR        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.placeholder.hts_code.attr.name", "scHTSCode");
    public static final String AERO_PLM_COLORWAY_STATUS_ATTR         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.colorway.season.plm_colorway_status.attr.name", "scPLMPolorwayStatus");
    public static final String AERO_COLORWAY_CAP_SHARED_ATTR         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.colorway.season.cap_shared.attr.name", "scCAPshared");

    public static final String FC_SHARED_ATTR   = LCSProperties.get(PROPERTY_PATH_PREFIX + ".fc.colorway.season.fc_shared.attr.name", "scFCshared");

    public static final String AERO_COLORWAY_S4_SHARED_ATTR          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.colorway.season.s4_shared.attr.name", "scS4Shared");
    public static final String AERO_COLORWAY_SENT_TO_CAP_ATTR        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.colorway.season.sent_to_cap.attr.name", "scSentToCAP");
    public static final String AERO_COLORWAY_SENT_TO_S4_ATTR         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.colorway.season.sent_to_s4.attr.name", "scSentToS4");
    public static final String AERO_COLORWAY_LIFECYCLE_ATTR          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.colorway.season.colorway_lifecycle.attr.name", "sccolorwaylifecycle");
    public static final String AERO_COLORWAY_CHANNEL_ATTR            = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.colorway.season.colorway_channel.attr.name", "scColorwayChannel");
    public static final String AERO_COST_SHEET_STATUS_ATTR           = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cost_sheet.cost_sheet_status.attr.name", "scCostSheetStatus");
    public static final String AERO_SOURCING_STATUS_ATTR             = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.sourcing_config.sourcing_status.attr.name", "scSourcingStatus");
    public static final String AERO_SOURCING_CONFIG_SENT_TO_S4_ATTR  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.sourcing_config.sourcing_config_sent_to_s4.attr.name", "scsourcingconfigsent");
    public static final String AERO_SOURCING_FG_FACTORY_ATTR         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.sourcing_config.finished_goods_factory.attr.name", "scFGFactory");
    public static final String AERO_SOURCING_VENDOR_ATTR             = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.sourcing_config.vendor.attr.name", "vendor");
    public static final String AERO_COST_SHEET_COST_SENT_TO_CAP_ATTR = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cost_sheet.cost_sent_to_cap.attr.name", "sccostcheetsenttoCAP");
    public static final String AERO_COST_SHEET_COST_SENT_TO_S4_ATTR  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cost_sheet.cost_sent_to_s4.attr.name", "sccostcheetsenttos4");
    public static final String AERO_COST_SHEET_MILESTONE_ATTR        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cost_sheet.cost_sheet_milestone.attr.name", "scMilestoneStatus");
    public static final String AERO_COST_SHEET_INCOTERMS_ATTR        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cost_sheet.incoterms.attr.name", "scIncoterms");
    public static final String AERO_COST_SHEET_ADDITIONAL_BUY_ATTR   = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cost_sheet.additional_buy.attr.name", "scAdditionalBuy");
    public static final String AERO_COST_SHEET_DESTINATION_ATTR      = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cost_sheet.destination.attr.name", "scDestination");
    public static final String AERO_ENUM_SECONDARY_KEY_ATTR          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.enumeration.secondary_key.attr.name", "_CUSTOM_scAeroSecondaryKey");

    //Aero constants for FLAG/Attr values
    public static final String AERO_HTS_CODE_1_WEIGHT_THRESHOLD_VALUE     = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.hts_code_1_threshold.value", "100.0");
    public static final String AERO_HTS_CODE_1_DEFAULT                    = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.hts_code_1_default.value", "999999");
    public static final String AERO_PLM_COLORWAY_STATUS_DEVELOPMENT_VALUE = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.colorway.season.plm_colorway_status.development.value", "scDevelopment");
    public static final String AERO_PLM_COLORWAY_STATUS_DROPPED_VALUE     = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.colorway.season.plm_colorway_status.dropped.value", "scDropped");
    public static final String AERO_PLM_COLORWAY_LIFECYCLE_DROPPED_VALUE  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.colorway.season.colorway_lifecycle.dropped.value", "scdropped");
    public static final String AERO_SENT_YES_VALUE                        = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.sent.yes.value", SENT_YES);
    public static final String AERO_SENT_NO_VALUE                         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.sent.no.value", SENT_NO);
    public static final String AERO_SOURCING_STATUS_DROPPED_VALUE         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.sourcing_config.sourcing_status.dropped.value", "dropped");
    public static final String AERO_SOURCING_STATUS_CONFIRMED_VALUE       = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.sourcing_config.sourcing_status.confirmed.value", "confirmed");
    public static final String AERO_SOURCING_STATUS_DEVELOPMENT_VALUE     = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.sourcing_config.sourcing_status.development.value", "development");
    public static final String AERO_ARTICLE_CAP_UPDATE_CLASS_VALUES_PATH  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.article.update.validation.class_path_prefix.value", PROPERTY_PATH_PREFIX + ".aero.article.update.validation.subdepartment.{0}.class.values");

    public static final Set<String> AERO_VALID_DEVELOPMENT_SEASONS_VALUES = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.article.season.valid_development_seasons.values", "scFall, scHoliday, scSpring, scSummer, scBTS").split(",", -1))
            .map(season -> season.trim())
            .collect(Collectors.toSet());

    public static final Set<String> AERO_VALID_DEVELOPMENT_CHANNEL_VALUES = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.article.colorway.season.valid_development_channel.values", "scclub, scdiscounters, scWholesaleFullPrice").split(",", -1))
            .map(season -> season.trim())
            .collect(Collectors.toSet());

    public static final Set<String> AERO_VALID_COLORWAY_LIFECYCLE_VALUES = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.colorway.valid_colorway_lifecycle.values", "screleased, scsampleready, scbuyready, scdropped").split(",", -1))
            .map(season -> season.trim())
            .collect(Collectors.toSet());

    public static final Set<String> AERO_COLORWAY_LIFECYCLE_VALUES = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.colorway.valid_colorway_lifecycle.values", "screleased, scsampleready, scbuyready").split(",", -1))
            .map(season -> season.trim())
            .collect(Collectors.toSet());
    public static final Set<String> AERO_COST_SHEET_STATUS_CONFIRMED_VALUES = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.cost_sheet.cost_sheet_status.confirmed.values", "scConfirmedbytone,scConfirmedbycc,scF21Confirmed,scLuckyConfirmed,scAeroConfirmed")
                    .split(",", -1))
            .map(season -> season.trim())
            .collect(Collectors.toSet());

    public static final Set<String> AERO_COSTING_VALID_SOURCING_STATUS_VALUES = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.costing.sourcing_config.valid_sourcing_status.values", "confirmed,development")
                    .split(",", -1))
            .map(season -> season.trim())
            .collect(Collectors.toSet());

    public static final Set<String> AERO_COSTING_COST_SHEET_MILESTONE_FINAL_VALUES = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.costing.cost_sheet.cost_sheet_milestone.final.values", "scFinal,scLuckyFinal,scF21Final,scAeroFinal")
                    .split(",", -1))
            .map(season -> season.trim())
            .collect(Collectors.toSet());

    public static final Set<String> AERO_ARTICLE_CAP_UPDATE_SUBDEPT_VALUES = Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.article.update.validation.subdepartments.values", "")
                    .split(",", -1))
            .map(season -> season.trim())
            .collect(Collectors.toSet());

    //---
    //Reebok constants
    public static final String REEBOK_PRODUCT_APPAREL_FLEX_PATH = LCSProperties.get(PROPERTY_PATH_PREFIX + ".reebok.product.apparel.flex.path", "Product\\scApparel\\scReebok");
    public static final String REEBOK_PRODUCT_FOOTWEAR_FLEX_PATH = LCSProperties.get(PROPERTY_PATH_PREFIX + ".reebok.product.footwear.flex.path", "Product\\scFootwear\\scFootwearReebok");
    public static final String REEBOK_SEASON_FLEX_PATH  = LCSProperties.get(PROPERTY_PATH_PREFIX + ".reebok.season.flex.path", "Season\\scSeasonReebok");

    public static final String LUCKY_CAP_ADOPTION_LINE = "scCAPlineadoption";


    public static final String LUCKY_PLM_COLORWAY_STATUS_PRODUCTION_VALUE = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.colorway.season.plm_colorway_status.production.value", "scProduction");

    public static final String LUCKY_COST_SENT_TO_FC_ATTR               = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.cost.sourcing_config.colorway_season.cost_sent_to_fc.attr.name", "sccostsheetsenttoFC");
    public static final String LUCKY_COST_SENT_TO_CAP_ATTR              = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.cost.sourcing_config.colorway_season.cost_sent_to_cap.attr.name", "sccostcheetsenttoCAP");

    public static final String LUCKY_COST_VENDOR_SUPPLIER_NO = "scPLMVendorSupplierNo";

    public static final String LUCKY_VENDOR_NAME ="name";

    public static final String LUCKY_COSTING_LOGGER_NAME         = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.logger.name", "LUCKY_INTEGRATION_COSTING_LOGGER");

    public static final String LUCKY_FLEX_TYPE_PATH_KEY          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.flex.type.path", "scLucky");


    public static final String LUCKY_SEASON_ATTR_GROUP_KEY            = "LUCKY_SEASON";
    public static final String LUCKY_PRODUCT_ATTR_GROUP_KEY           = "LUCKY_PRODUCT";
    public static final String LUCKY_COLORWAY_ATTR_GROUP_KEY          = "LUCKY_COLORWAY";
    public static final String LUCKY_COLORWAY_SEASON_ATTR_GROUP_KEY   = "LUCKY_COLORWAY_SEASON";
    public static final String LUCKY_PRODUCT_SEASON_ATTR_GROUP_KEY    = "LUCKY_PRODUCT_SEASON";
    public static final String LUCKY_SOURCING_CONFIG_ATTR_GROUP_KEY   = "LUCKY_SOURCING_CONFIG";
    public static final String LUCKY_SOURCING_SEASON_ATTR_GROUP_KEY   = "LUCKY_SOURCING_SEASON";
    public static final String LUCKY_SOURCING_COLORWAY_ATTR_GROUP_KEY = "LUCKY_SOURCING_COLORWAY";
    public static final String LUCKY_COST_SHEET_ATTR_GROUP_KEY        = "LUCKY_COST_SHEET";

    public static final String LUCKY_COST_SHEET_INCO_ATTR_GROUP_KEY        = "LUCKY_INCO_COST_SHEET";

    public static final String FULL_CIRCLE_VENDOR_NAME = "scSAPVendorNameLucky";

    public static final String PRODUCT_ATTR          = LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.plugin.product.attributes", "");

    public static final String COST_BRANCH_ID ="LCSPRODUCTCOSTSHEET.BRANCHIDITERATIONINFO";
    public static final String COST_VR_STR = "VR:com.lcs.wc.sourcing.LCSProductCostSheet:";
    public static final String APP_COLOR_NAMES = "LCSCOSTSHEET.APPLICABLECOLORNAMES";
    public static final String COST_TYPE_STR = "LCSCOSTSHEET.COSTSHEETTYPE";
    public static final Map<String, Set<String>> LUCKY_COSTING_PLUGIN_ATTRIBUTES = new HashMap<>();
	
	
	public static final String AERO_PLM_COLORWAY_STATUS_INITIAL_VALUE     = LCSProperties.get(PROPERTY_PATH_PREFIX + ".aero.colorway.season.plm_colorway_status.initial.value", "scInitial");


    static {

        LUCKY_COSTING_PLUGIN_ATTRIBUTES.put(LUCKY_SOURCING_CONFIG_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.plugin.sourcing_config.attributes", "").split(","))
                .map(productAttr -> productAttr.trim())
                .collect(Collectors.toSet()));

        LUCKY_COSTING_PLUGIN_ATTRIBUTES.put(LUCKY_SOURCING_SEASON_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.plugin.sourcing_season.attributes", "").split(","))
                .map(srcSeasonAttr -> srcSeasonAttr.trim())
                .collect(Collectors.toSet()));

        LUCKY_COSTING_PLUGIN_ATTRIBUTES.put(LUCKY_COST_SHEET_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.plugin.cost_sheet.attributes", "").split(","))
                .map(skuAttr -> skuAttr.trim())
                .collect(Collectors.toSet()));

        LUCKY_COSTING_PLUGIN_ATTRIBUTES.put(LUCKY_COST_SHEET_INCO_ATTR_GROUP_KEY, Arrays.stream(LCSProperties.get(PROPERTY_PATH_PREFIX + ".lucky.costing.plugin.inco_terms.attributes", "").split(","))
                .map(skuAttr -> skuAttr.trim())
                .collect(Collectors.toSet()));


    }
	
	//Lucky Enhancement - End
    private SparcIntegrationConstants() {

    }
	
	
}
