package com.sparc.wc.material;

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
import com.lcs.wc.material.LCSMaterialSupplierMaster;
import com.lcs.wc.material.LCSMaterialSupplier;
import com.lcs.wc.material.MaterialPricingEntryClientModel;
import com.lcs.wc.material.MaterialPricingEntry;
import com.lcs.wc.material.MaterialPricingEntryQuery;
import com.lcs.wc.moa.LCSMOATable;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wt.fc.WTObject;
import wt.util.WTException;
import wt.vc.Iterated;
import wt.util.WTPropertyVetoException;


import com.sparc.wc.util.*;
/**
 * SPARCBOMPriceCalcMatPricing  is a Plugin class written to Calculate BOM Price as dictated by the combination of Purchase UOM and BOM UOM attributes present on the Material Supplier that holds the Material Pricing Entry. 
 * The method setBOMPriceOnMPE should be triggered on a Post Update Persist event on the Material {ricing Entry
 * 
 * @author HARSHAD
 * @version 1.0 true.
 */
public final class SPARCBOMPriceCalcMatPricing {
	
	/**Initializing PURCHASE_UOM_MATERIALSUPPLIER_KEY. */
	private static String PURCHASE_UOM_MATERIALSUPPLIER_KEY = LCSProperties.get("com.sparc.wc.util.SPARCFetchReusableTableData.PURCHASE_UOM_MATERIALSUPPLIER_KEY");	
	/**Initializing BOM_UOM_MATERIALSUPPLIER_KEY. */
	private static String BOM_UOM_MATERIALSUPPLIER_KEY = LCSProperties.get("com.sparc.wc.util.SPARCFetchReusableTableData.BOM_UOM_MATERIALSUPPLIER_KEY");	
	/**Initializing SPARC_MPE_BULKPRICEKEY. */
	private static String SPARC_MPE_BULKPRICEKEY = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.SPARC_MPE_BULKPRICEKEY");
	/**Initializing SPARC_MPE_BOMPRICE. */
	private static String SPARC_MPE_BOMPRICE = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.SPARC_MPE_BOMPRICE");
	/**Initializing SPARC_MPE_FOB_UNITPRICEKEY. */
	private static String SPARC_MPE_FOB_UNITPRICEKEY = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.SPARC_MPE_FOB_UNITPRICEKEY","MATERIALPRICINGENTRY.PTC_DBL_2TYPEINFOMATERIALPRI");
	/**Initializing SPARC_MPE_FOB_BULKPRICEKEY. */
	private static String SPARC_MPE_FOB_BULKPRICEKEY = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.SPARC_MPE_FOB_BULKPRICEKEY","MATERIALPRICINGENTRY.PTC_DBL_3TYPEINFOMATERIALPRI");
	/**Initializing SPARC_MPE_FOB_IDA2A2. */
	private static String SPARC_MPE_FOB_IDA2A2 = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.SPARC_MPE_FOB_IDA2A2","MATERIALPRICINGENTRY.IDA2A2");
	/**Initializing Logger. */
	private static Logger logger=LogManager.getLogger(SPARCBOMPriceCalcMatPricing.class);					
	
	
	/**
	*private constructor.
	*/
	private SPARCBOMPriceCalcMatPricing(){
	}
	
	/**
	 * The method setBOMPriceOnMPE should be triggered on a Post Update Persist event on the Material Pricing Entry.
	 * BOM Price is calculated based on Purchase UOM and BOM UOM of Material Supplier and Unit Price of Material Pricng Entry.
	 * 
	 * @param obj WTObject.
	 * @throws WTException the WTException.
	 * @throws WTPropertyVetoException the WTPropertyVetoException.
	 */
	public static void setBOMPriceOnMPE (WTObject obj) throws WTException,WTPropertyVetoException{
		logger.debug("-----------------------Entering Method SPARCBOMPriceCalcMatPricing.setBOMPriceOnMPE----------------------------");
		if(obj==null){
			logger.debug("-----------------------Exiting Method SPARCBOMPriceCalcMatPricing.setBOMPriceOnMPE as Instance is not found----------------------------");
			return;
			
		}
		if(obj instanceof MaterialPricingEntry){
		double conversionFactor = 0;
		
		MaterialPricingEntry matPricingEntry = (MaterialPricingEntry)obj;
		
		/*
		Developer's explanation: Tried obtaining attribute values using the getValue API. Doesnt work for Material Pricing Entry.
		//float mpeUnitPrice = (float)matPricingEntry.getValue(SPARC_MPE_UNITPRICEKEY);
		//float mpeBulkPrice = (float)matPricingEntry.getValue(SPARC_MPE_BULKPRICEKEY);
		We will fetch these values by using a predefined query that returns these values in the form of FLexObjects
		*/
			
			logger.debug("------------SPARCBOMPriceCalcMatPricing.setBOMPriceOnMPE-----------Material Pricing Entry under process is------"+matPricingEntry);	
			LCSMaterialSupplierMaster materialSupplierMaster = (LCSMaterialSupplierMaster)matPricingEntry.getMaterialSupplier();
			logger.debug("------------SPARCBOMPriceCalcMatPricing.setBOMPriceOnMPE-----------Material Supplier Master------"+materialSupplierMaster);
			LCSMaterialSupplier materialSupplier = (LCSMaterialSupplier)VersionHelper.latestIterationOf(materialSupplierMaster);
			logger.debug("------------SPARCBOMPriceCalcMatPricing.setBOMPriceOnMPE-----------Material Supplier------"+materialSupplier);
			String purchaseUOMValue  = (String)materialSupplier.getValue(PURCHASE_UOM_MATERIALSUPPLIER_KEY);
			String bomUOMValue  = (String)materialSupplier.getValue(BOM_UOM_MATERIALSUPPLIER_KEY);
			logger.debug("------------SPARCBOMPriceCalcMatPricing.setBOMPriceOnMPE-----------Material Supplier purchaseUOMValue------"+purchaseUOMValue);
			logger.debug("------------SPARCBOMPriceCalcMatPricing.setBOMPriceOnMPE-----------Material Supplier bomUOMValue------"+bomUOMValue);
			Collection matSupIdsCol = new ArrayList();
			
			matSupIdsCol.add(FormatHelper.getNumericFromOid(FormatHelper.getObjectId(materialSupplier.getMaster())));
			
			//Ideally, this query should return only 1 Material Pricing Entry, but in the offchance that this returns multiple Material Pricing entries, we will iterate the whole result set and filter out the current Material Pricing entry by matching the IdA2A2.
			// Assumption - Only Independent Material Pricing Entry is being used.
			SearchResults matPricingEntryResults = new MaterialPricingEntryQuery().findDuplicatePricingEntriesForIndependantDates(matPricingEntry);
			logger.debug("------------SPARCBOMPriceCalcMatPricing.setBOMPriceOnMPE-----------matPricingEntryResults------"+matPricingEntryResults);	
				if(matPricingEntryResults!=null){
					Collection matPricingEntriesCollection = matPricingEntryResults.getResults();
					if(!matPricingEntriesCollection.isEmpty()){
						Iterator matPricingIterator = matPricingEntriesCollection.iterator();
						while(matPricingIterator.hasNext()){
							FlexObject matPricingEntryFOB = (FlexObject)matPricingIterator.next();
							String mpeIda2a2 = matPricingEntryFOB.getString(SPARC_MPE_FOB_IDA2A2);
							if(FormatHelper.hasContent(mpeIda2a2)){
								// Here we match IdA2A2s. When these Ids match, we ensure that we are obtaining the correct Material Pricing Entry  values.
								
								if(matPricingEntry.toString().contains(mpeIda2a2)){
									logger.debug("------------SPARCBOMPriceCalcMatPricing.setBOMPriceOnMPE-----------material Pricing Entry FlexObject------"+matPricingEntryFOB);
									double mpeUnitPrice = Double.parseDouble(matPricingEntryFOB.getString(SPARC_MPE_FOB_UNITPRICEKEY));
									double mpeBulkPrice = Double.parseDouble(matPricingEntryFOB.getString(SPARC_MPE_FOB_BULKPRICEKEY));
									
									if(FormatHelper.hasContent(purchaseUOMValue)&&FormatHelper.hasContent(bomUOMValue)){
										
										// Fetching Conversion factor from the Reusable table based on Purchase UOM and BOM UOM values from Material Supplier.
										
										 conversionFactor = SPARCFetchReusableTableData.fetchBOMPriceConversionFactor(purchaseUOMValue,bomUOMValue);
										logger.debug("------------SPARCBOMPriceCalcMatPricing.setBOMPriceOnMPE-----------conversionFactor fetched------"+matPricingEntryFOB);
										if(0.0f!=conversionFactor){
											mpeBulkPrice = conversionFactor*mpeUnitPrice;
											logger.debug("------------SPARCBOMPriceCalcMatPricing.setBOMPriceOnMPE-----------calculated BOM Price on Material Pricing Entry------"+mpeBulkPrice);
											//matPricingEntry.setValue(SPARC_MPE_BULKPRICEKEY,mpeBulkPrice);
											matPricingEntry.setValue(SPARC_MPE_BOMPRICE,mpeBulkPrice);
																				
											// This API 'setPrice' is explicitly used to push the updated value on BOM. setValue API doesnt work correctly. 

											matPricingEntry.setPrice(mpeBulkPrice);
											
											LCSLogic.persist(matPricingEntry,true);
											logger.debug("------------SPARCBOMPriceCalcMatPricing.setBOMPriceOnMPE-----------MPE BOM Price set");
										}
									}
									
								}
							}
						}
					}
				}
			
		}else{
			
		logger.debug("------------SPARCBOMPriceCalcMatPricing.setBOMPriceOnMPE-----------Method invoked with incompatible object instance");	
		}
		logger.debug("------------SPARCBOMPriceCalcMatPricing.setBOMPriceOnMPE-----------Exiting Plugin");
	}
	
}