package com.sparc.wc.costsheet;

import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.*;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.util.SparcConstants;
import com.sparc.wc.util.SparcCostingConstants;
import com.sparc.wc.util.SparcCostingUtil;
import org.apache.logging.log4j.Logger;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import java.util.*;
import com.lcs.wc.product.LCSProduct;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_FLEX_TYPE_PATH;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;



public class SPARCCalculatedCostingPlugin {

    private static final Logger LOGGER = LogR.getLogger(SPARCCalculatedCostingPlugin.class.getName());
	private static String HTSSumKey = LCSProperties.get("com.lcs.wc.source.SourceToSeason.Aero.HTSWeightSum");
    private static String HTSKey = LCSProperties.get("com.lcs.wc.source.SourceToSeason.Aero.HTSCode");

    public static void calculateAndPopulateCostAttributesFromCostSheet(WTObject obj) {
        System.out.println("Start of populateCalcFieldsFromBusObject -----------------------------------");
        if (!(obj instanceof LCSProductCostSheet)) {
            return;
        }
        LCSProductCostSheet objCostSheet = null;
        Map<String, Object> calculatedAttributes = new LinkedHashMap<>();
        try {
            LOGGER.info("Start of populateCalcFieldsFromBusObject");
            objCostSheet = (LCSProductCostSheet) obj;
            LCSSourcingConfig objSourcingConfig = SparcCostingUtil.getSourcingConfig(objCostSheet);
            if (objSourcingConfig == null) {
                LOGGER.info("Sourcing config is null");
                return;
            }
            if (objCostSheet.getFlexType().getFullName(true).equalsIgnoreCase(SparcCostingConstants.SPARC_COSTSHEET))
                return;
            Collection<LCSCostSheet> costSheets = SparcCostingUtil.getCostSheetsForSourcingConfig(objSourcingConfig);
            if (costSheets.isEmpty()) {
                return;
            }
            LCSSeason objSeason = SparcCostingUtil.getLatestSeason(objCostSheet);
            if (!(LCSSourcingConfigQuery.sourceToSeasonExists(objSourcingConfig.getMaster(), objSeason.getMaster()))) {
                LOGGER.info("Source to Season link doesn't exists");
                return;
            }
            LCSSourceToSeasonLink sourceSeasonLink = SparcCostingUtil.getSourceSeasonLink(objSourcingConfig, objCostSheet);
            List<String> strHTSCodeKeys = SparcCostingUtil.convertCommaSepValuesintoList(SparcCostingConstants.HTS_CODES_KEY);
            LCSCountry originCountry = SparcCostingUtil.getCountry(objSourcingConfig, SparcCostingConstants.FINISHED_GOODS_FACTORY_KEY,SparcCostingConstants.COUNTRY_KEY);
            calculatedAttributes = calculateAttributeValues(objCostSheet, strHTSCodeKeys, calculatedAttributes, originCountry, sourceSeasonLink);
           /* if(calculatedAttributes.isEmpty()){
                for(String strKey : SparcCostingConstants.COSTSHEET_KEYS){
                    objCostSheet.setValue(strKey,0.00);
                }
            }*/
            //else{
                for(String strKey : SparcCostingConstants.COSTSHEET_KEYS){
                    if(calculatedAttributes.containsKey(strKey)){
						if(calculatedAttributes.get(strKey) instanceof Double){
                           objCostSheet.setValue(strKey,(Double)calculatedAttributes.get(strKey));
						}
						else{
						 objCostSheet.setValue(strKey,(String)calculatedAttributes.get(strKey));	
						}
                    }else
					{
						//System.out.println("strKey---------------"+strKey);
						if("scHTSMatchingRow".equalsIgnoreCase(strKey)||"scFreightMatchingRow".equalsIgnoreCase(strKey)||"scInternalMatchingRow".equalsIgnoreCase(strKey)||"scOtherMatchingRow".equalsIgnoreCase(strKey))
						{
							System.out.println("Do Nothing");
						}
						else{
                           objCostSheet.setValue(strKey,0.00);
						}
                    }
                }
           // }

        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.error("There is an error associated with costsheet " + ex.getMessage());
        }
        System.out.println("End of populateCalcFieldsFromBusObject");
        //return objCostSheet;
    }


    public static void calculateAndPopulateCostAttributesFromSourceSeason(WTObject obj){
        System.out.println("obj-----populateCalculatedAttributes-----------------"+obj);
        if(!(obj instanceof LCSSourceToSeasonLink)){
            return;
        }
        LCSSourceToSeasonLink objSourceSeasLink = (LCSSourceToSeasonLink) obj;
        Collection<WTObject> costSheetsForSourceToSeason = new ArrayList();
        Map<String,Object> mappedAttributes = new LinkedHashMap<>();
        List<String> strHTSCodeKeys = new ArrayList<>();
        LCSCountry originCountry = null;
        LCSCountry destCountry = null;
        LCSSourcingConfig objSourcingConfig = null;
        try {

            if(FormatHelper.equalsWithNull(objSourceSeasLink.getSourcingConfigMaster(),null)){
                System.out.println("Sourcing Config Master is null");
                return;
            }

            objSourcingConfig = (LCSSourcingConfig) VersionHelper.latestIterationOf(objSourceSeasLink.getSourcingConfigMaster());
            costSheetsForSourceToSeason = LCSCostSheetQuery.getCostSheetsForSourceToSeason(objSourceSeasLink.getSeasonMaster(), objSourceSeasLink.getSourcingConfigMaster(), null, false, false);
            for (WTObject objCostSheet : costSheetsForSourceToSeason) {
                if (!(objCostSheet instanceof LCSProductCostSheet)) {
                    System.out.println("It is not a Product Costsheet");
                    continue;
                }
				
				
                LCSProductCostSheet objProductCostSheet = (LCSProductCostSheet) objCostSheet;
				if (objProductCostSheet.getFlexType().getFullName(true).equalsIgnoreCase(SparcCostingConstants.SPARC_COSTSHEET))
						return;
                mappedAttributes = new LinkedHashMap<>();
                strHTSCodeKeys = SparcCostingUtil.convertCommaSepValuesintoList(SparcCostingConstants.HTS_CODES_KEY);
                originCountry = SparcCostingUtil.getCountry(objSourcingConfig, SparcCostingConstants.FINISHED_GOODS_FACTORY_KEY,SparcCostingConstants.COUNTRY_KEY);


                mappedAttributes = calculateAttributeValues(objProductCostSheet, strHTSCodeKeys, mappedAttributes, originCountry, objSourceSeasLink);
                objProductCostSheet = (LCSProductCostSheet) VersionHelper.latestIterationOf(objProductCostSheet);
                objProductCostSheet = VersionHelper.checkout(objProductCostSheet);
                for (Map.Entry<String, Object> entry : mappedAttributes.entrySet()) {
                    objProductCostSheet.setValue(entry.getKey(), entry.getValue());
                }
                objProductCostSheet=(LCSProductCostSheet)new LCSCostSheetLogic().saveCostSheet(objProductCostSheet,true,true);
                if(VersionHelper.isCheckedOut(objProductCostSheet)){
                    VersionHelper.checkin(objProductCostSheet);
                }

            }
        } catch (WTException e) {
            e.printStackTrace();
        } catch (WTPropertyVetoException e) {
            e.printStackTrace();
        }
    }

    public static Map<String,Object> calculateAttributeValues(LCSProductCostSheet objCostSheet,List<String> strHTSCodeKeys,Map<String,Object> mappedAttributes,
                                                              LCSCountry originCountry,LCSSourceToSeasonLink objSourceSeasLink){
        LCSCountry destCountry = null;
        String originCountryId = SparcConstants.EMPTY_STRING;
        String desCountryId = SparcConstants.EMPTY_STRING;
        Date objCostDate = null;
        FlexType boType = null;
        try {
            destCountry = SparcCostingUtil.getCountry(objCostSheet, SparcCostingConstants.DEST_COUNTRY_KEY);
            if (FormatHelper.equalsWithNull(destCountry, null)) {
                destCountry = SparcCostingConstants.countryQuery.findCountryByNameType(SparcCostingConstants.DEST_COUNTRY_VALUE, null);
            }
            objCostDate = (Date) objCostSheet.getValue(SparcCostingConstants.COSTING_PRICING_DATE);
            desCountryId = FormatHelper.getNumericVersionIdFromObject(destCountry);
            boType = SparcCostingUtil.getFlexType(SparcCostingConstants.HTS_LOOKUP_PATH);
            if (!FormatHelper.equalsWithNull(originCountry, null)) {
                originCountryId = FormatHelper.getNumericVersionIdFromObject(originCountry);
                mappedAttributes = SparcCostingUtil.calculateCostForHTSCode(strHTSCodeKeys, objSourceSeasLink, originCountryId, desCountryId, objCostDate,
                        objCostSheet, boType, mappedAttributes);

            }
            boType = SparcCostingUtil.getFlexType(SparcCostingConstants.FREIGHT_LOOKUP_PATH);
            mappedAttributes = SparcCostingUtil.populateFreightRate(objCostSheet, objCostDate, boType, mappedAttributes);
            boType = SparcCostingUtil.getFlexType(SparcCostingConstants.OTHER_LOOKUP_TYPE);
            mappedAttributes = SparcCostingUtil.populateInternalLoadRate(objCostSheet, objCostDate, boType, SparcCostingConstants.OTHER_LOOKUP_ATTRIBUTES, false, mappedAttributes);
            boType = SparcCostingUtil.getFlexType(SparcCostingConstants.INTERNAL_LOOKUP_TYPE);
            mappedAttributes = SparcCostingUtil.populateInternalLoadRate(objCostSheet, objCostDate, boType, SparcCostingConstants.INTERNAL_LOOKUP_ATTRIBUTES, true, mappedAttributes);

        }catch(WTException ex){
            ex.printStackTrace();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return mappedAttributes;
    }
	
	
	public static void ValidateHTSWightSum(WTObject obj) throws WTException, WTPropertyVetoException {
        System.out.println("Inside validateHTSSum");
        if(!(obj instanceof LCSSourceToSeasonLink)){
            return;
        }
        LCSSourceToSeasonLink objSourceSeasLink = (LCSSourceToSeasonLink) obj;
		LCSSourcingConfig sourcingConfig = (LCSSourcingConfig) VersionHelper.latestIterationOf(objSourceSeasLink.getSourcingConfigMaster());
		LCSProduct product = (LCSProduct)VersionHelper.latestIterationOf(sourcingConfig.getProductMaster());
		
		//Check if source to season link is bound to an Aero product.
		if(product != null){
		String productType = product.getFlexType().getFullName(true);
		
    	if(productType.contains(AERO_PRODUCT_FLEX_TYPE_PATH)) 
		{
		
		
		if (objSourceSeasLink.getValue(HTSKey)!= null && objSourceSeasLink.getValue(HTSSumKey)!= null && !objSourceSeasLink.getValue(HTSKey).toString().isEmpty()){
			//System.out.println("HTSSum value ----"+objSourceSeasLink.getValue(HTSSumKey) + "HTS value----"+ objSourceSeasLink.getValue(HTSKey).toString() );
			Double HTSSumValue = (Double) objSourceSeasLink.getValue(HTSSumKey);
			if (HTSSumValue<100)
			{
				throw new LCSException("HTS Weight Sum Attribute should be equal to 100");
			}
				
			
		}
			
			
		}
		}
		
        
		
		
}

}
