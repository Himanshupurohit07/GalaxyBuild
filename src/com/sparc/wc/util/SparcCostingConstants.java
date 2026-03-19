package com.sparc.wc.util;

import com.lcs.wc.country.LCSCountryQuery;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;

import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.FormatHelper;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class SparcCostingConstants {

    public static final String HTS_CODES_KEY = LCSProperties.get("com.sparc.wc.costing.HTSCodes", "scHTSCode1~scHTSCode1WeightPercent,scHTSCode2~scHTSCode2WeightPercent,scHTSCode3~scHTSCode3WeightPercent,scHTSCode4~scHTSCode4WeightPercent");

    public static final String HTS_LOOKUP_PATH = LCSProperties.get("com.sparc.wc.costing.HTSBOFlexType");

    public static final String OBJECT_IDENTIFIER = "thePersistInfo.theObjectIdentifier.id";

    public static final String TYPE_IDENTIFIER = "typeDefinitionReference.key.branchId";

    public static final String HTS_CODE_KEY = "scHTSCode";

    public static final String START_DATE_KEY = "scStartDate";


    public static final String END_DATE_KEY = "scEndDate";

    public static final String COUNTRY_ORIGIN_KEY = "scCountryOfOrigin";

    public static final String DESTINATION_COUNTRY_KEY = "scDestinationCountry";
	
	public static final String DESTINATION_KEY = "scDestination";

    public static final LCSSourcingConfigQuery SOURCING_QUERY = new LCSSourcingConfigQuery();

    public static final String EMPTY_STRING = "";

    public static final String DEST_COUNTRY_KEY = "scDestinationCountryUSA";

    public static final String DEST_COUNTRY_VALUE = "USA";


    public static final String COSTING_PRICING_DATE = "scCotSheetPricingDate";

    public static final String DUTY_PERCENT = "scDutyPercent";

    public static final String DUTY_PERCENT_USA = "scDutyPercentUS";

    public static final String COST_TARRIF_PERCENT_KEY = "scTariffPercent";

    public static final String BO_TARIFF_PER_KEY = "scTariffPercent";

    public static final LCSCountryQuery countryQuery = new LCSCountryQuery();

    public static final String FREIGHT_INPUT_ATTRIBUTES = LCSProperties.get("com.sparc.wc.costing.FreightAttributes");

    public static final String FREIGHT_LOOKUP_PATH = LCSProperties.get("com.sparc.wc.costing.FreightBOFlexType");

    public static final String TILDE_DELIMITER = "~";

    public static final String FREIGHT_DOLLARS_US = "scFreightDollarsUS";

    public static final String FREIGHT_RATE = "scFreightRate";

    public static final String INTERNAL_LOOKUP_ATTRIBUTES = LCSProperties.get("com.sparc.wc.costing.InternalLoadAttributes");

    public static final String OTHER_LOOKUP_ATTRIBUTES = LCSProperties.get("com.sparc.wc.costing.OtherLoadAttributes");

    public static final String INTERNAL_LOOKUP_TYPE = LCSProperties.get("com.sparc.wc.costing.InternalLoadFlexType");
	
	public static final String TARIFF_BY_COUNTRY = LCSProperties.get("com.sparc.wc.costing.TariffByCountry");

    public static final String OTHER_LOOKUP_TYPE = LCSProperties.get("com.sparc.wc.costing.OtherBOFlexType");

    public static final String INTERNAL_COST_PERCENT_US = "scInternalLoadPercentUS";

    public static final String INTERNAL_BO_PERCENT_US = "scInternalLoadPercent";

    public static final String BRAND_TEXT = "scBrand~text~nonBlank";

    public static final String REEBOK = "reebok";

    public static final String LUCKY = "Lucky";

    public static final String OTHER_BO_PERCENT = "scOtherPercent";

    public static final String OTHER_COST_PERCENT = "scOtherPercentUS";

    public static final String FREIGHT_COST_DOLLARS_OVERRIDE_KEY = "scFreightDollarsUSOverride";

    public static final String INTERNAL_COST_OVERRIDE = "scInternalLoadUSOverride";

    public static final String DUTY_PERCENT_OVERRIDE = "scDutyPercentUSOverride";

    public static String SPARC_COSTSHEET = "Cost Sheet\\scSparc";

    public static final String FINISHED_GOODS_FACTORY_KEY = "scFGFactory";

    public static final String COUNTRY_KEY = "scCountry";

    public static final String FREIGHT_DOLLARS1_KEY ="scFreight1DollarsUS";

    public static final String FLAT_DUTY_DOLLAR_KEY ="scFlatDutyDollar";

    public static final String COMMA_DELIMITER = ",";
	
	public static final Set<String> COSTSHEET_KEYS = Arrays.stream(LCSProperties.get("com.sparc.wc.costing.attributes").split(",",-1)).map(strKey -> strKey.trim())
            .collect(Collectors.toSet());
			
	
	public static final String  CATEGORY_KEY ="scCategory2";


    public static final String RETRIEVE_BOM_KEY = "scRetrievefromBOM";

    public static final String OR_BOM_LINK ="OR:com.lcs.wc.flexbom.FlexBOMLink:";

    public static final String DOC_FOOTWEAR_BREAKDOWN = "Document\\scFootWearBreakDown";

    public static final String SC_STATUS_KEY = "scStatus";

    public static final String SC_STATUS_COMPLETED = "scCompleted";

    public static final String SC_STATUS_INPROGRESS = "scInProgress";

    public static final String BO_NAME = "name";

    public static final String FOOTWEAR_BOM = "BOM\\Materials\\scFootwear";

    public static final String APPAREL_BOM = "BOM\\Materials\\scApparel";

    public static final String SECTION_KEY = "section";
	
	public static final String OTHER_PROCESS_KEY = "scOtherProcessesCost";

    public static final String SEMICOLON_DELIMITER = ";";

    public static final String PRODUCT_COST_SHEET = "LCSProductCostSheet";

    public static final String LCS_COST_SHEET = "LCSCostSheet";

    public static final String ITERATION_INFO_BRANCH_ID = "iterationInfo.branchId";

    public static final String ITERATION_INFO_LATEST = "iterationInfo.latest";

    public static final String APPAREL = "Apparel";

    public static final String FOOTWEAR = "Footwear";

    public static final String IS_ADDITIONAL ="isAdditional";

    public static final String COST_SHEET_NUMBER = "scCostSheetNumber";

    public static final String VR_COST_SHEET_STR = "VR:com.lcs.wc.sourcing.LCSProductCostSheet:";

    public static final String COST_BRANCH_ID = "LCSCOSTSHEET.BRANCHIDITERATIONINFO";

    public static final String BOM_LINK_ID = "bomLinkId";

    public static final String S_CURRENCY= "currency";

    public static final String S_FLOAT ="float";

    public static final String S_INTEGER ="integer";

    public static final String S_DATE ="date";

    public static final String S_CHOICE ="choice";

    public static final String S_DRIVEN = "driven";

    public static final String S_OBJECT_REF ="object_ref";

    public static final String S_TEXT_AREA = "textArea";

    public static final String S_TEXT = "text";

    public static final String SUCCESS = "SUCCESS";

    public static final String ERROR = "ERROR";

    public static final String WARNING = "WARNING";

    public static final String UPLOAD_UPDATE = "update";

    public static final String UPLOAD_CREATE = "create";

    public static final String NO_VALUE_STATUS = "novalue";

    public static final String VALUE_FOUND = "found";

    public static final String VALUE_ERROR = "error";

    public static final String SKU_NAME = "skuName";

    public static final String SKU_DELIMITER = "|~*~|";

    public static final String CUSTOM_OBJECT_REF =  "object_ref";

    public static final String INCO_TERM_LOCATION= "scIncotermlocation";

    public static final String CHOICE_TYPE = "choice";

    public static final String DRIVEN_TYPE = "driven";

    public static final String DATE_TYPE = "date";

    public static final String DATE_FORMAT_TYPE = "dd-MMM-yyyy";

    public static final String INTEGER_TYPE = "Integer";

    public static final String CUSTOM_DATE_TYPE = "MM/dd/yyyy";

    public static final String TIME_TO_LIVE = "4800000";

    public static final String UPLOADED_PATH = FormatHelper.formatOSFolderLocation(LCSProperties.get("com.lcs.wc.content.documentfilePath", "/lcsdocuments"));

    public static final String DOC_NAME = "Name";

    public static final String COND_ASSIGN_OP = "=";

    public static final String PLM_PROD_NO = "scPLMProductNo";

    public static final String SKU_STR_VR = "VR:com.lcs.wc.product.LCSSKU:";


    public static final String PROD_STR = "PRODUCT";

    public static final String PROD_OBJECT_IDENTIFIER = "thePersistInfo.theObjectIdentifier.id";

    public static final String FLEX_ID_PATH = "flexTypeIdPath";

    public static final String LCS_PROD_STR = "LCSPRODUCT";
	
	public static final String COST_SELECTED_COLORWAY_NAMES = "costSheetSelectedColorwayNames";
	
	public static final String REP_COLORWAY = "representativeColorwayName";
}
