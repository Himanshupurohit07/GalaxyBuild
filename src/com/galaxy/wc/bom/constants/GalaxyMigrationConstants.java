package com.galaxy.wc.bom.constants;

import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

import java.util.List;

public class GalaxyMigrationConstants {



    public static final String PRODUCT_BRANCH_ID = "ProdBranchId";

    public static final String SOURCING_NO = "SourcingNo";

    public static final String SOURCING_BRANCH_ID ="SourcingBranchId";


    public static final String BOM_NAME ="bomName";

    public static final String BOM_TYPE_DISPLAY_NAME = "BOM Type DisplayName";

    public static final String IS_PRIMARY_BOM = "Is Primary BOM";

    public static final String PROD_NO_ATTRIBUTE = "scPLMProductNo";

    public static final String SOURCING_NO_ATTRIBUTE = "scSourcingNo";

    public static final String SPEC_BRANCH_ID = "SpecBranchId";

    public static final String BOM_BRANCH_ID = "BomBranchId";

    public static final String BOM_LINK_BRANCH_ID ="FLEXBOMLINK.branchId";

    public static final String DIMENISION_NAME = "FLEXBOMLINK.dimensionname";

    public static final String BRANCH_DIM = "BRANCH_DIM";

    public static final String DIMENSION_ID ="FLEXBOMLINK.DIMENSIONID";

    public static final String OR_BOM_LINK_STR = "OR:com.lcs.wc.flexbom.FlexBOMLink:";

    public static final String BOM_LINK_IDA2A2 ="FLEXBOMLINK.IDA2A2";

    public static final String BOM_LINK_BRAN_ID = "FLEXBOMLINK.BRANCHID";

    public static final String SORTING_NUMBER = "FLEXBOMLINK.SORTINGNUMBER";

    public static final String SECTION ="section";

    public static final String MATERIAL_PLACEHOLDER ="material_placeholder";

    public static final String SUPPLIER_PLACEHOLDER ="placeholder";

    public static final String MATERIAL_NO = "scMaterialNo";

    public static final String SUPPLIER_NO= "scPLMVendorSupplierNo";

    public static final String MATERIAL_DESC ="materialDescription";

    public static final String LOSS_ADJUSTMENT = "lossAdjustment";

    public static final String PRICE_OVERRIDE ="priceOverride";

    public static final String COMMA_DELIMITER = ",";

    public static final String EMPTY_SPACE = "";

    public static final String COLORWAY_NO ="scColorwayNo";

    public static final String COLOR_NO = "scColorNo";

    public static final String SKU_VARIATION = ":SKU";

    public static final String SOURCE_VARIATION =":SOURCE";

    public static final String SOURCE_SKU_VARIATION =":SOURCE:SKU";

    public static final String PLM_SOURCE_NO = "scSourcingNo";


    public static final String PLM_PRODUCT_NO = "scPLMProductNo";

    public static final String CALC_SIZE_PERCENT = "scCalculationSizePercent";

    public static final String QUANTITY = "quantity";

    public static final String bomLinkAttributes = LCSProperties.get("bomlink.attributes","scComponent,scAppPlacement,scBOMDetailedContent,scCalculationSizePercent,scCIF,quantity,lossAdjustment,priceOverride,scComments,scGrossQuantity");

    public static final List<String> linkAttrList = FormatHelper.commaSeparatedListToList(bomLinkAttributes);
	
	

    public static final String CURRENCY_TYPE = "currency";

    public static final String FLOAT_TYPE = "float";

    public static final String CHOICE_TYPE ="choice";

    public static final String OBJECT_REF= "object_ref";

    public static final String SUPPLIER_DESC= "supplierDescription";

    public static final String CIF ="scCIF";

    public static final String YIELD = "yield";

    public static final String RBK_APPAREL_PATH = "Product\\scApparel\\scReebok";

    public static final String RBK_FOOTWEAR_PATH = "Product\\scFootwear\\scFootwearReebok";

    public static final String COLOR_DESC ="colorDescription";

    public static final String RBK_APP_BRAND ="RBKApparel";

    public static final String RBK_FTW_BRAND = "RBKFootwear";

    public static final String SPECIAL_DELIMITER = "^";

    public static final String BOM_STATUS = "bomstatus";

    public static final String BOM_PART_NAME = "ptcbomPartName";

    public static final String BOM_ATTRIBUTES = LCSProperties.get("BOMPART.ATTRIBUTES","seasonName,seasonTypePath,productNo,prodTypePath,glSourceMasterId,sourceName,specName,glSpecMasterID,bomName,glBomMasterId,ptcbomPartNumber,bomTypePath,bomStatus,isPrimary");

    public static final List<String> BOM_ATTR_LIST  = FormatHelper.commaSeparatedListToList(BOM_ATTRIBUTES);

    public static final String PRIMARY_BOM_ATTRIBUTES = LCSProperties.get("BOMPART.ATTRIBUTES","productNo,bomName,glBomMasterId,strMaterialNo,strMaterialDesc,strSupplierNo");

    public static final List<String> PRIMARY_BOM_ATTR_LIST  = FormatHelper.commaSeparatedListToList(PRIMARY_BOM_ATTRIBUTES);


    

    public static final String PRIM_MATERIAL_NO = "strMaterialNo";

    public static final String PRIM_MATERIAL_DESC = "strMaterialDesc";

    public static final String PRIM_SUPPLIER_NO = "strSupplierNo";
    public static final String SPEC_NAME ="specName";

    public static final String SOURCE_NAME = "sourceName";

    public static final String SEASON_NAME = "seasonName";

    public static final String PRODUCT_NO ="productNo";

    public static final String SOURCE_MASTER_ID ="glSourceMasterId";

    public static final String SPEC_MASTER_ID= "glSpecMasterID";

    public static final String IS_PRIMARY= "isPrimary";

    public static final String BOM_HEAD_STATUS= "bomStatus";

    public static final String PROD_A_VERSION = "A";

    public static final String NAME = "name";

    public static final String BOM_PART_NUMBER = "ptcbomPartNumber";

    public static final String SUB_ASSEMBLY_INSERTION_MODE = "subassemblyInsertionMode";

    public static final String PROMPT_USER_LINK_OR_COPY= "PROMPT_USER_LINK_OR_COPY";

    public static final String BOM_MASTER_ID ="glBomMasterId";

    public static final String FLEX_BOM_PART ="FlexBOMPart";

    public static final String ITERATION_BRANCHID = "iterationInfo.branchId";

    public static final String CHECKOUT_STATE = "checkoutInfo.state";

    public static final String ITERATION_LATEST = "iterationInfo.latest";

    public static final String VERSION_ID = "versionInfo.identifier.versionId";

    public static final String PRODUCT_VR_STR = "VR:com.lcs.wc.product.LCSProduct:";

    public static final String PROD_BRANCH_STR ="LCSPRODUCT.BRANCHIDITERATIONINFO";

    public static final String PRODUCT = "Product";

    public static final String COMMENTS = "scComments";

    public static final String BO_APP_PATH = "Business Object\\scComponentListApparel";

    public static final String BO_FTW_PATH = "Business Object\\scComponentListFootwear";

    public static final String COM_NAME_ATTRIBUTE = "sfComponentName";

    public static final String BO_OR_STR= "OR:com.lcs.wc.foundation.LCSLifecycleManaged:";

    public static final String BO_IDA2A2 = "LCSLifecycleManaged.IDA2A2";

    public static final String COLUMN_IDA2A2_STR ="thePersistInfo.theObjectIdentifier.id";

    public static final Boolean BOOL_TRUE = true;

    public static final Boolean BOOL_FALSE = false;

    public static final String COM_ATTRIBUTE ="scComponent";

    public static final String BOM_DETAILED_CONTENT ="scBOMDetailedContent";

    public static final String APP_PLACEMENT="scAppPlacement";

    public static final String APP_BRAND ="Apparel";

    public static final String FTW_BRAND = "Footwear";

    public static final String PRIM_MATERIAL = "primaryMaterial";

    public static final String PRIM_MAT_DESC = "pmDescription";

    public static final String PRIM_MATERIAL_DISPLAY = "PRIMARYMATERIAL";

    public static final String PRIM_MATERIAL_DESCRIPTION = "PRIMARYMATERIALDESC";

    public static final String BOM_PART_NO_DISPLAY = "BOM_PART_NUMBER";

    public static final String BOM_TYPE_PATH="bomTypePath";

    public static final String SC_FOOTWEAR = "scFootwear";
	
	public static final String SC_APP_PLACEMENT = "scAppPlacement";
	
	
	public static final String TILDE_DELIMITER = "~";

    public static final String MISSING_MATERIAL ="MissingMaterial";

    public static final String MISSING_SUPPLIER ="MissingSupplier";

    public static final String MISSING_MATSUPP ="MissingMATSUPP";

    public static final String MISSING_COLOR = "MissingColor";

    public static final String MISSING_COLORWAY = "MissingColorway";

    public static final String  MISSING_MAT_COLOR ="MissingMatColor";
    
    public static final String MISSING_SOURCING = "MissingSourcing";
	
	 public static final String BOM_STR = "Top BOM  links not created successfully~BOMNAME~";

    public static final String PROD_STR = "~for the product~";

    public static final String FILE_STR= "~and the fileName is~";

    public static final String VAR_NOT_STR = "so VariatedLinks for this BOM Not Processed~";

    public static final String VAR_PROCESS = "So variated links will be processed if available for the corresponding toplinks~";

    public static final String BOM_CREATE_STR = "Top BOM links created successfully~BOMNAME~";

    public static final String BOM_VAR_CREATE_STR = "Var BOM Links created successfully~BOMNAME~";

    public static final String BOM_VAR_NOT_STR = "Var BOM Links not created successfully~BOMNAME~";
	
	
	public static final String BOM_FOOTWEAR_TYPE = "BOM\\Materials\\scFootwear";

    public static final String BOM_APPAREL_TYPE = "BOM\\Materials\\scApparel";
	
	public static final String HIGH_LIGHT = "highLight";

	



}
