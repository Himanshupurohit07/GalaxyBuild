package com.sparc.wc.material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Arrays;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTypeQuery;
import com.lcs.wc.flextype.FlexTyped;
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
import com.lcs.wc.material.MaterialPricingEntryClientModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import wt.fc.WTObject;
import wt.util.WTException;
import wt.vc.Iterated;
import wt.util.WTPropertyVetoException;
import java.lang.Exception;
import com.sparc.wc.util.*;

/**
 * SPARCBOMPriceCalcMatSup  is a Plugin class written to Calculate and set BOM Price as dictated by the combination of Purchase UOM and BOM UOM attributes present on the Material Supplier that holds the Material Pricing Entry. 
 * The method setBOMPriceOnMSAndMPE should be triggered on a Post Update Persist event on the Material ricing Entry
 * 
 * @author HARSHAD
 * @version 1.0 true.
 */
public final class SPARCBOMPriceCalcMatSup {
	/**Initializing PURCHASE_UOM_MATERIALSUPPLIER_KEY. */
	private static String PURCHASE_UOM_MATERIALSUPPLIER_KEY = LCSProperties.get("com.sparc.wc.util.SPARCFetchReusableTableData.PURCHASE_UOM_MATERIALSUPPLIER_KEY");	
	/**Initializing BOM_UOM_MATERIALSUPPLIER_KEY. */
	private static String BOM_UOM_MATERIALSUPPLIER_KEY = LCSProperties.get("com.sparc.wc.util.SPARCFetchReusableTableData.BOM_UOM_MATERIALSUPPLIER_KEY");	
	/**Initializing MOA_DELIMITER. */
	private static String MOA_DELIMITER = LCSProperties.get("com.sparc.wc.util.SPARCFetchReusableTableData.MOA_DELIMITER","~");	
	/**Initializing SPARC_MPE_BULKPRICEKEY. */
	private static String SPARC_MPE_BULKPRICEKEY = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.SPARC_MPE_BULKPRICEKEY","PTC_DBL_2TYPEINFOMATERIALPRI");
	/**Initializing SPARC_MPE_BOMPRICE. */
	private static String SPARC_MPE_BOMPRICE = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.SPARC_MPE_BOMPRICE");
	/**Initializing SPARC_MATSUPP_UNITPRICEKEY. */
	private static String SPARC_MATSUPP_UNITPRICEKEY = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.SPARC_MATSUPP_UNITPRICEKEY");
	/**Initializing SPARC_MATSUPP_BOMPRICE. */
	private static String SPARC_MATSUPP_BOMPRICE = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.SPARC_MATSUPP_BOMPRICE");					
	/**Initializing SPARC_MPE_FOB_UNITPRICEKEY. */
	private static String SPARC_MPE_FOB_UNITPRICEKEY = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.SPARC_MPE_FOB_UNITPRICEKEY","MATERIALPRICINGENTRY.PTC_DBL_2TYPEINFOMATERIALPRI");
	/**Initializing SPARC_MPE_FOB_BULKPRICEKEY. */
	private static String SPARC_MPE_FOB_BULKPRICEKEY = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.SPARC_MPE_FOB_BULKPRICEKEY","MATERIALPRICINGENTRY.PTC_DBL_3TYPEINFOMATERIALPRI");
	/**Initializing SPARC_MPE_FOB_IDA2A2. */
	private static String SPARC_MPE_FOB_IDA2A2 = LCSProperties.get("com.sparc.wc.material.BOMPriceCalculations.SPARC_MPE_FOB_IDA2A2","MATERIALPRICINGENTRY.IDA2A2");
	/**Initializing MATERIAL_PRICING_CLASSNAME_OR. */
	private static String MATERIAL_PRICING_CLASSNAME_OR = LCSProperties.get("com.sparc.wc.material.MATERIAL_PRICING_CLASSNAME_OR","OR:com.lcs.wc.material.MaterialPricingEntry:");
	/**Initializing logger. */
	private static Logger logger=LogManager.getLogger(SPARCBOMPriceCalcMatSup.class);
	
	/**
	*private constructor.
	*/
	private SPARCBOMPriceCalcMatSup(){
	}
	
	/**
	 * The method setBOMPriceOnMSAndMPE should be triggered on a Post Persist event on the Material Supplier.
	 * BOM Price is calculated based on Purchase UOM and BOM UOM of Material Supplier and Unit Price of Material Pricng Entries and Material Supplier.
	 * 
	 * @param obj WTObject.
	 * @throws WTException the WTException.
	 * @throws WTPropertyVetoException the WTPropertyVetoException.
	 */
	public static void setBOMPriceOnMSAndMPE(WTObject obj) throws WTException,WTPropertyVetoException {
		
		logger.debug("-----------------------Entering Method SPARCBOMPriceCalcMatSup.setBOMPriceOnMSAndMPE----------------------------");
		if(obj==null){
			return;
			
		}
		if(obj instanceof LCSMaterialSupplier){
			LCSMaterialSupplier matSupplierCurrentVersion = null;
			LCSMaterialSupplier matSupplierPreviousVersion = null;
			double conversionFactor = 0;
			LCSMaterialSupplier matSupplier = (LCSMaterialSupplier)obj;
			
			//Getting the cuurent version of Material Supplier Object.
			matSupplierCurrentVersion = (LCSMaterialSupplier) VersionHelper.latestIterationOf(matSupplier);
			String purchaseUOMValue  = (String)matSupplier.getValue(PURCHASE_UOM_MATERIALSUPPLIER_KEY);
			String bomUOMValue  = (String)matSupplier.getValue(BOM_UOM_MATERIALSUPPLIER_KEY);
			logger.debug("-----------------------SPARCBOMPriceCalcMatSup.setBOMPriceOnMSAndMPE-------matSupplier--------"+matSupplier);
			
			logger.debug("-----------------------SPARCBOMPriceCalcMatSup.setBOMPriceOnMSAndMPE-------purchaseUOMValue--------"+purchaseUOMValue);
			
			logger.debug("-----------------------SPARCBOMPriceCalcMatSup.setBOMPriceOnMSAndMPE-------bomUOMValue--------"+bomUOMValue);
			//Getting the Previous Version of Material Supplier Object.
			matSupplierPreviousVersion = (LCSMaterialSupplier) VersionHelper.predecessorOf((Iterated) matSupplier);
			logger.debug("-----------------------SPARCBOMPriceCalcMatSup.setBOMPriceOnMSAndMPE-------matSupplierPreviousVersion--------"+matSupplierPreviousVersion);
			// If there is difference in 1 or more Attributes between the 2 versions, this method returns true.
			if(!compareVersions(matSupplierCurrentVersion,matSupplierPreviousVersion,BOM_UOM_MATERIALSUPPLIER_KEY+MOA_DELIMITER+PURCHASE_UOM_MATERIALSUPPLIER_KEY+MOA_DELIMITER+SPARC_MATSUPP_UNITPRICEKEY)){
				logger.debug("-----------------------SPARCBOMPriceCalcMatSup.setBOMPriceOnMSAndMPE-------No Difference found between current and Previous versions--------");
				return;
			}
			if(FormatHelper.hasContent(purchaseUOMValue)&&FormatHelper.hasContent(bomUOMValue)){
				 conversionFactor = SPARCFetchReusableTableData.fetchBOMPriceConversionFactor(purchaseUOMValue,bomUOMValue);
				
				logger.debug("-----------------------SPARCBOMPriceCalcMatSup.setBOMPriceOnMSAndMPE-------conversionFactor--------"+conversionFactor);
					if(0.0!=conversionFactor){
						Collection matSupIdsCol = new ArrayList();
						matSupIdsCol.add(FormatHelper.getNumericFromOid(FormatHelper.getObjectId(matSupplier.getMaster())));
						SearchResults matPricingEntryResults = new MaterialPricingEntryQuery().findIndependentMaterialPricingEntryCollection(matSupIdsCol,matSupplier.getMaster()); 
						
						logger.debug("-----------------------SPARCBOMPriceCalcMatSup.setBOMPriceOnMSAndMPE-------matPricingEntryResults-Independent Pricing Entry SearchResults-------"+matPricingEntryResults);
							if(matPricingEntryResults!=null){
								Collection matPricingEntriesCollection = matPricingEntryResults.getResults();
								logger.debug("-----------------------SPARCBOMPriceCalcMatSup.setBOMPriceOnMSAndMPE-------matPricingEntriesCollection-Independent Pricing Entry Collection-------"+matPricingEntriesCollection);
								if(!matPricingEntriesCollection.isEmpty()){
									Iterator matPricingIterator = matPricingEntriesCollection.iterator();
									while(matPricingIterator.hasNext()){
										FlexObject matPricingEntryFOB = (FlexObject)matPricingIterator.next();
										String mpeIda2a2 = matPricingEntryFOB.getString(SPARC_MPE_FOB_IDA2A2);
										if(FormatHelper.hasContent(mpeIda2a2)){
											MaterialPricingEntry matPricingEntry = (MaterialPricingEntry)LCSQuery.findObjectById(MATERIAL_PRICING_CLASSNAME_OR+mpeIda2a2);
											double mpeUnitPrice = Double.parseDouble(matPricingEntryFOB.getString(SPARC_MPE_FOB_UNITPRICEKEY));
											//float mpeUnitPrice = (float)matPricingEntry.getValue(SPARC_MPE_UNITPRICEKEY);
											//float mpeBulkPrice = (float)matPricingEntry.getValue(SPARC_MPE_BULKPRICEKEY);
											
											
										//	float mpeBulkPrice = Float.parseFloat(matPricingEntryFOB.getString(SPARC_MPE_FOB_BULKPRICEKEY));
											double mpeBulkPrice = conversionFactor*mpeUnitPrice;
											logger.debug("-----------------------SPARCBOMPriceCalcMatSup.setBOMPriceOnMSAndMPE-------Calculated BOM Price for Material Pricing Entry-------"+mpeBulkPrice);
											//matPricingEntry.setValue(SPARC_MPE_BULKPRICEKEY,mpeBulkPrice);
											matPricingEntry.setValue(SPARC_MPE_BOMPRICE,mpeBulkPrice);
											matPricingEntry.setPrice(mpeBulkPrice);
											
											
											LCSLogic.persist(matPricingEntry,true);
											logger.debug("-----------------------SPARCBOMPriceCalcMatSup.setBOMPriceOnMSAndMPE-------Pricing Entry-------"+matPricingEntry+"----------has been updated---------");
											
										}
								}
							}
							
							
						}
					
					
					
					double matSuppBulkPrice = conversionFactor*(Double)matSupplierCurrentVersion.getValue(SPARC_MATSUPP_UNITPRICEKEY);
				
					logger.debug("-----------------------SPARCBOMPriceCalcMatSup.setBOMPriceOnMSAndMPE-----------------Calculated BOM Price for Material Supplier---------"+matSuppBulkPrice);
				
					matSupplierCurrentVersion.setValue(SPARC_MATSUPP_BOMPRICE,matSuppBulkPrice);
					LCSLogic.persist(matSupplierCurrentVersion,true);
					logger.debug("-----------------------SPARCBOMPriceCalcMatSup.setBOMPriceOnMSAndMPE-----------------Material Supplier has been updated---------");
					
					}else {
						
						matSupplierCurrentVersion.setValue(SPARC_MATSUPP_BOMPRICE,0.0);
						LCSLogic.persist(matSupplierCurrentVersion,true);
				}
			}else{
				return;
			}
			
		}
		
		
	
	}
	

	
	
	
	/**
	 * The method compareVersions is used to compare difference in Attribute values between two versions of the object.
	 * 
	 * 
	 * @param obj FlexTyped, prevObj FlexTyped, attributesToBeChecked String.
	 * @returns boolean
	 * @throws WTException the WTException.
	 */

	private static boolean compareVersions(FlexTyped obj, FlexTyped prevObj, String attributesToBeChecked) throws WTException {
		
		logger.debug("-----------------------SPARCBOMPriceCalcMatSup.compareVersions-----------------Entering the method---------");
		// If the previous version is null, this is a create case, Hence return true.
		if(null == prevObj) {
			logger.debug("-----------------------SPARCBOMPriceCalcMatSup.compareVersions-----------------New Material Supplier detected---------");
			return true;
		}		
		String currentValue = null;
		String prevValue = null;		
		
		String[] attributesToBeCheckedArray = attributesToBeChecked.split(MOA_DELIMITER);
		
		for (int i = 0; i < attributesToBeCheckedArray.length; i++) {
			if(!obj.getFlexType().isAttributeUsed(obj, attributesToBeCheckedArray[i])) {
				
				continue;
			}
			Object currentObjValue = obj.getValue(attributesToBeCheckedArray[i]);
			if(null != currentObjValue) {
				
				currentValue = String.valueOf(currentObjValue);
				
			}	
			Object prevObjValue = prevObj.getValue(attributesToBeCheckedArray[i]);
			if(null != prevObjValue) {
				prevValue = String.valueOf(prevObjValue);
			}
				
					logger.debug("-----------------------SPARCBOMPriceCalcMatSup.currentValue----------------"+currentValue);
					logger.debug("-----------------------SPARCBOMPriceCalcMatSup.prevValue----------------"+prevValue);
				if((FormatHelper.compareWithNulls((Comparable)currentValue, (Comparable)prevValue, true)) != 0){
						
					logger.debug("-----------------------SPARCBOMPriceCalcMatSup.compareVersions----------------"+attributesToBeCheckedArray[i]+"-Attribute value difference detected between current and previous versions --------");
					return true;
				}
			}
		
		logger.debug("-----------------------SPARCBOMPriceCalcMatSup.compareVersions----------------Both versions have the same values for attributes --------"+attributesToBeChecked);
		return false;
	}

}