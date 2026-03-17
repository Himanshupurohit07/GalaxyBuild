package com.sparc.wc.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTypeQuery;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.material.LCSMaterialSupplier;
import com.lcs.wc.material.MaterialPricingEntry;
import com.lcs.wc.material.MaterialPricingEntryQuery;
import com.lcs.wc.moa.LCSMOATable;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import org.apache.commons.collections.CollectionUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wt.fc.WTObject;
import wt.util.WTException;
import wt.vc.Iterated;
import wt.util.WTPropertyVetoException;

/**
 * SPARCFetchReusableTableData  is a Utility Class written to find Reusable Table Related Data 
 * Eg: The correct Row data required for BOM Price Conversion according to the combination of Purchase UOM and BOM UOM selected values
 * This class can be modified to fetch other Reusable Table Instances also
 * 
 * @author HARSHAD
 * @version 1.0 true.
 */
public final class SPARCFetchReusableTableData {

	/**Initializing SPARC_BOM_PRICE_CONVERSION_MOATABLE_ATTKEY. */
	private static String SPARC_BOM_PRICE_CONVERSION_MOATABLE_ATTKEY = LCSProperties.get("com.sparc.wc.util.SPARCFetchReusableTableData.SPARC_BOM_PRICE_CONVERSION_MOATABLE_ATTKEY");	
	/**Initializing SPARC_BOM_PRICE_CONVERSION_BUSINESSOBJECT_FULL_PATH. */
	private static String SPARC_BOM_PRICE_CONVERSION_BUSINESSOBJECT_FULL_PATH = LCSProperties.get("com.sparc.wc.util.SPARCFetchReusableTableData.SPARC_BOM_PRICE_CONVERSION_BUSINESSOBJECT_FULL_PATH");	
	/**Initializing SPARC_MOA_PURCHASEUOM_KEY. */
	private static String SPARC_MOA_PURCHASEUOM_KEY = LCSProperties.get("com.sparc.wc.util.SPARCFetchReusableTableData.SPARC_MOA_PURCHASEUOM_KEY");	
	/**Initializing SPARC_MOA_BOMUOM_KEY. */
	private static String SPARC_MOA_BOMUOM_KEY = LCSProperties.get("com.sparc.wc.util.SPARCFetchReusableTableData.SPARC_MOA_BOMUOM_KEY");	
	/**Initializing SPARC_MOA_FOBSTRING_CONVERSIONFACTOR. */
	private static String SPARC_MOA_FOBSTRING_CONVERSIONFACTOR = LCSProperties.get("com.sparc.wc.util.SPARCFetchReusableTableData.SPARC_MOA_FOBSTRING_CONVERSIONFACTOR");
	/**Initializing SCPURCHASEUOMMS. */
	private static String SCPURCHASEUOMMS = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.SCPURCHASEUOMMS","SCPURCHASEUOMMS");
	/**Initializing SCBOMUOMMS. */
	private static String SCBOMUOMMS = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.SCBOMUOMMS","SCBOMUOMMS");
	/**Initializing SCBOMPRICECONVERSIONFACTOR. */
	private static String SCBOMPRICECONVERSIONFACTOR = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.SCBOMPRICECONVERSIONFACTOR","SCBOMPRICECONVERSIONFACTOR");
	/**Initializing BUSINESSOBJECTIDA2A2. */
	private static String BUSINESSOBJECTIDA2A2 = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.BUSINESSOBJECTIDA2A2","LCSLIFECYCLEMANAGED.IDA2A2");
	/**Initializing BUSINESSOBJECTIDA2A2. */
	private static String BUSINESSOBJECT_CLASSNAME_OR = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.BUSINESSOBJECT_CLASSNAME_OR","OR:com.lcs.wc.foundation.LCSLifecycleManaged:");
	
	/**Initializing logger. */
	private static Logger logger=LogManager.getLogger(SPARCFetchReusableTableData.class);	
	
	
	/**
	*private constructor.
	*/
	private SPARCFetchReusableTableData(){
	}
	
	
	/**
	 * The method fetchReusableTable is used to fetch MOA Table based on Businessobject Hierarchy and MOATable key
	 * 
	 * 
	 * @param businessObjectFlexTypePath String, reusableTableKey String.
	 * @returns LCSMOATable
	 * @throws WTException the WTException.
	 */
	public static  LCSMOATable fetchReusableTable(String businessObjectFlexTypePath,String reusableTableKey) throws WTException {

logger.debug("-----------------------Entering Method SPARCFetchReusableTableData.fetchReusableTable----------------------------");

		LCSMOATable requiredMOATable = null;

		//Fetching the Business Object Instance that holds the MOA Table
		FlexType businessObjectType = FlexTypeCache.getFlexTypeFromPath(businessObjectFlexTypePath);	
		FlexTypeQuery ftq = new FlexTypeQuery();
		Collection boColl  = new ArrayList();
		boColl = ftq.findAllObjectsTypedBy(businessObjectType).getResults();	
		
		
		logger.debug("-----------------------SPARCFetchReusableTableData.fetchReusableTable---------------------boColl-------"+boColl);
		// There should only be a unique instance of the Business object that holds the MOATable
			if (boColl != null && boColl.size() == 1) {
				FlexObject fob = (FlexObject) CollectionUtils.get(boColl, 0);
				String boIdA2A2 = fob.getString(BUSINESSOBJECTIDA2A2);
				if(FormatHelper.hasContent(boIdA2A2)){
				LCSLifecycleManaged	sparcBOMPriceConversionBO = (LCSLifecycleManaged) LCSQuery.findObjectById(BUSINESSOBJECT_CLASSNAME_OR+ boIdA2A2);
					if(sparcBOMPriceConversionBO!=null){
					LCSMOATable	bomPriceConversionReusableTable = (LCSMOATable) sparcBOMPriceConversionBO.getValue(reusableTableKey);
						if(bomPriceConversionReusableTable!=null){
						requiredMOATable = 	bomPriceConversionReusableTable;
						}
					}
				}
			}

													
			return requiredMOATable;
	}
	
	/**
	 * The method fetchBOMPriceConversionFactor is used to fetch ConversionFactor Table based on Purchase UOM and BOM UOM value
	 * 
	 * 
	 * @param scPurchaseUOMValue String, msUnitOfMeasureValue String.
	 * @returns float.
	 * @throws WTException the WTException.
	 */	
	public static double fetchBOMPriceConversionFactor(String scPurchaseUOMValue, String msUnitOfMeasureValue) throws WTException{
		logger.debug("-----------------------Entering SPARCFetchReusableTableData.fetchBOMPriceConversionFactor----------------------------");
		double conversionFactor = 0;
		// Creating Filter Criteria Map to fetch only specific rows from the MOA Table
		Map filterCriteriaMap = new HashMap();
		filterCriteriaMap.put(SPARC_MOA_PURCHASEUOM_KEY,scPurchaseUOMValue);	
		filterCriteriaMap.put(SPARC_MOA_BOMUOM_KEY,msUnitOfMeasureValue);
		logger.debug("-----------------------SPARCFetchReusableTableData.fetchBOMPriceConversionFactor-----------------------filterCriteriaMap-----"+filterCriteriaMap);

		LCSMOATable bomPriceConversionReusableTable = null;
		Collection bomPriceMOARowsCollection= new ArrayList();	
		// Using the method fetchReusableTable to get the MOATable used for BOM Price Conversion
		bomPriceConversionReusableTable = fetchReusableTable(SPARC_BOM_PRICE_CONVERSION_BUSINESSOBJECT_FULL_PATH,SPARC_BOM_PRICE_CONVERSION_MOATABLE_ATTKEY);
		logger.debug("-----------------------SPARCFetchReusableTableData.fetchBOMPriceConversionFactor-----------------------bomPriceConversionReusableTable-----"+bomPriceConversionReusableTable);

			if(bomPriceConversionReusableTable!=null){
				// The Filter Criteria set should ideally return only 1 row.
				bomPriceMOARowsCollection = bomPriceConversionReusableTable.getRows(filterCriteriaMap, true);
				//bomPriceMOARowsCollection = bomPriceConversionReusableTable.getRows();
			}
			logger.debug("-----------------------SPARCFetchReusableTableData.fetchBOMPriceConversionFactor-----------------------bomPriceMOARowsCollection-----"+bomPriceMOARowsCollection);
		
			if(bomPriceMOARowsCollection !=null && !bomPriceMOARowsCollection.isEmpty() && bomPriceMOARowsCollection.size()==1){
				
				FlexObject correctMOARowFOB = (FlexObject)bomPriceMOARowsCollection.iterator().next();
				
				 String conversionFactorString = correctMOARowFOB.getString(SPARC_MOA_FOBSTRING_CONVERSIONFACTOR);
				 conversionFactor =  Double.parseDouble(conversionFactorString);
			}else if(bomPriceMOARowsCollection.size()>1){
				Iterator bomPriceIterator = bomPriceMOARowsCollection.iterator();
				while(bomPriceIterator.hasNext()){
					FlexObject fob =(FlexObject)bomPriceIterator.next();
					String purchaseUOMMOA = fob.getString(SCPURCHASEUOMMS);
					String BOMUOMMOA = fob.getString(SCBOMUOMMS);
					
					if(purchaseUOMMOA.equals(scPurchaseUOMValue)&&BOMUOMMOA.equals(msUnitOfMeasureValue)){
						
						conversionFactor = Double.parseDouble(fob.getString(SCBOMPRICECONVERSIONFACTOR));
					}
				}
				
				
				
			}
			logger.debug("-----------------------SPARCFetchReusableTableData.fetchBOMPriceConversionFactor-----------------------conversionFactor-----"+conversionFactor);
			return conversionFactor;
			
	}
	


}