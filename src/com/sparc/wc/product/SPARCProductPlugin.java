package com.sparc.wc.product;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import wt.fc.WTObject;
import wt.util.WTException;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Logger;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSLifecycleManagedLogic;
import com.lcs.wc.util.LCSProperties;
import com.sparc.wc.util.SparcConstants;
import wt.log4j.LogR;
import wt.method.MethodContext;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.INTERFACE_ADMIN_USER_NAME;
/**
 * This class contains the Server Side Plugins for LCSProducts
 */
public class SPARCProductPlugin {
	
	 private static final Logger LOGGER = LogR.getLogger(SPARCProductPlugin.class.getName());
	 
	// Department-level lock map to prevent concurrent updates for the same department
	private static final ConcurrentHashMap<String, Lock> departmentLocks = new ConcurrentHashMap<>();

	// Constants
	 private static final String FLEXTYPEPATH_AERO = LCSProperties.get("com.sparc.wc.integration.constants.SparcIntegrationConstants.aero.product.flex.type.path", "Product\\scApparel\\scAeropostale");
	 private static final String FLEXTYPEPATH_BO_AERO_STYLE_SEQUENCE = "Business Object\\scAeroStyleSequence";
	 private static final String ATTKEY_PRODUCT_AERO_DEPARTMENT = "scAeroDepartment";
	 private static final String ATTKEY_BO_AERO_DEPARTMENT = "scAeroDepartment";
	 private static final String ATTKEY_BO_AERO_SYLE_SEQUENCE_NEXTSTYLESEQUENCE = "scANextNumber";
	 private static final String ATTKEY_PRODUCT_AERO_LEGACY_MODEL_CODE = "sclegacymodelnumber";
	 private static final String BUSINESS_OBJECT_IDENTIFIER_LITERAL ="thePersistInfo.theObjectIdentifier.id";
	 
	 
	 /**
	  * This is starter method is to generate the Next Sequence Number from Aero Style Sequence Business Object and update onto Product for a given department 
	 * @param obj
	 * @throws WTException
	 */
	public static void setLegacyProductCode(WTObject obj) throws WTException {
    	 LOGGER.debug("**setLegacyProducCode start with:" + obj);
    	 if(!(obj instanceof LCSProduct))
    		 return;
    	 LCSProduct product = (LCSProduct)obj;
    	 FlexType productType = product.getFlexType();
    	 if (!FLEXTYPEPATH_AERO.equalsIgnoreCase(productType.getFullName(true))) 
			 return;
		 if(isInterfaceAdmin()) {
			 LOGGER.debug("Skipping the plugins from the interfaceadmin context");
			 return;
		 }
    	 
    	 String department = product.getValue(ATTKEY_PRODUCT_AERO_DEPARTMENT)!=null?(String)product.getValue(ATTKEY_PRODUCT_AERO_DEPARTMENT):"";
    	 String currentLegacyModelCode = "";
    	 if(department!=null && FormatHelper.hasContent(department)) {
    		 LCSProduct prevProduct = (LCSProduct)VersionHelper.predecessorOf(product);
    		 FlexTypeAttribute deptFlexAtt = productType.getAttribute(ATTKEY_PRODUCT_AERO_DEPARTMENT);
    		 String deptDisplayValue = deptFlexAtt.getAttValueList().getValue(department, null);
    		 if (prevProduct == null) {
    			 currentLegacyModelCode = product.getValue(ATTKEY_PRODUCT_AERO_LEGACY_MODEL_CODE)!=null?(String)product.getValue(ATTKEY_PRODUCT_AERO_LEGACY_MODEL_CODE):"";
    			 LOGGER.debug("**currentLegacyModelCode:"+currentLegacyModelCode);	
				if (currentLegacyModelCode == null || !FormatHelper.hasContent(currentLegacyModelCode)) {
					updateLegacyProductCode(product, department, deptDisplayValue);
				}
    		 	}
    	 	}
    	 LOGGER.debug("**setLegacyProducCode end");
    }
	 
	 /**
	  * 
	 * @param product
	 * @param departmentValue
	 * @param attDisplayValue
	 * @throws WTException
	 */
	public static void updateLegacyProductCode(LCSProduct product, String departmentValue, String attDisplayValue) throws WTException {
		Lock departmentLock = getDepartmentLock(departmentValue);

		try {

			// Acquire lock for the department to prevent concurrent updates
			departmentLock.lock();
			LCSLifecycleManaged businessObject = deriveAeroStyleSequenceBO(FLEXTYPEPATH_BO_AERO_STYLE_SEQUENCE, ATTKEY_BO_AERO_DEPARTMENT, departmentValue);

		if (businessObject != null) {
			String currentSequenceValue = (String) businessObject.getValue(ATTKEY_BO_AERO_SYLE_SEQUENCE_NEXTSTYLESEQUENCE);
			String newLegacyCode = currentSequenceValue;
			String strFlexTypeId = product.getFlexType().getIdNumber();
			Set<String> legacyNumbers = getLegacyNumbers(strFlexTypeId,product.getFlexType(),departmentValue);

			LOGGER.debug("legacyNumbers-------------------"+legacyNumbers);
			LOGGER.debug("newLegacyCode------------------"+newLegacyCode);
			if(!legacyNumbers.isEmpty()){
				while(legacyNumbers.contains(newLegacyCode)){
					LOGGER.debug("code exists before the system Legacy Code-----"+newLegacyCode);
					newLegacyCode = incrementSequence(businessObject, newLegacyCode);
				}
			}
			// Once we find an unused legacy code, set it on the product
			product.setValue(SparcConstants.PRODUCT_LEGACY_MODULE, newLegacyCode);
			// Increment the style sequence and persist the changes
			String updatedSequence = incrementSequence(businessObject, newLegacyCode);
			businessObject.setValue(ATTKEY_BO_AERO_SYLE_SEQUENCE_NEXTSTYLESEQUENCE, updatedSequence);
			LCSLifecycleManagedLogic.persist(businessObject, false);
			//LOGGER.debug("**BO is persisted with updated sequence: " + businessObject.getValue(ATTKEY_BO_AERO_SYLE_SEQUENCE_NEXTSTYLESEQUENCE));
		}
		} finally {
			LOGGER.debug("Getting unlocked from the System");
			departmentLock.unlock();  // Always release the lock
		}

	}

	/**
	 * This method we use to get the legacy numbers
	 * @param prodTypeId
	 * @param prodType
	 * @param dept
	 * @throws WTException
	 */
	public static Set<String> getLegacyNumbers(String prodTypeId, FlexType prodType, String dept) throws WTException {
		SearchResults results = new SearchResults();
		Set<String> legacyProdNumbers = new HashSet<String>();
		try {
			PreparedQueryStatement statement = new PreparedQueryStatement();
			statement.appendFromTable(LCSProduct.class);
			statement.appendSelectColumn(new QueryColumn(LCSProduct.class, "thePersistInfo.theObjectIdentifier.id"));
			statement.appendSelectColumn(new QueryColumn(LCSProduct.class, prodType.getAttribute(ATTKEY_PRODUCT_AERO_LEGACY_MODEL_CODE).getColumnDescriptorName()));
			statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, prodType.getAttribute(ATTKEY_PRODUCT_AERO_DEPARTMENT).getColumnDescriptorName()), dept, "="));
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, LCSQuery.TYPED_BRANCH_ID), prodTypeId, "="));
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria("LCSPRODUCT", "latestiterationinfo", "1", Criteria.EQUALS));
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria("LCSPRODUCT", "versionIdA2versionInfo", "A", Criteria.EQUALS));
			LOGGER.debug("Statement equals results---LCSPRODUCT------"+statement.toString());
			results = LCSQuery.runDirectQuery(statement);
			Vector resultsData = results.getResults();
			for (int i = 0; i < resultsData.size(); i++) {
				FlexObject flexObj = (FlexObject) resultsData.get(i);
				legacyProdNumbers.add(flexObj.getData("LCSPRODUCT.PTC_STR_18TYPEINFOLCSPRODUCT"));
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return legacyProdNumbers;
	}
	/**
	 * This method would increase the sequence by 1.
	 * @param bo The business object.
	 * @param currentSequenceNumber The current sequence number.
	 * @return The new incremented sequence number.
	 * @throws WTException If there are errors during execution.
	 */
	public static String incrementSequence(LCSLifecycleManaged bo, String currentSequenceNumber) throws WTException {
		String newSequenceNumber = "";

		LOGGER.debug("**check:" + Integer.valueOf(currentSequenceNumber).intValue() + ":" + Integer.valueOf("9999").intValue() + ":" + (Integer.valueOf(currentSequenceNumber).intValue() == Integer.valueOf("9999").intValue()));
		if (Integer.valueOf(currentSequenceNumber).intValue() == Integer.valueOf("9999").intValue()) {
			LOGGER.debug("**Recycling Aero Style Sequence:");
			return "1000";
		} else {
			int int_currentSequenceNumber = Integer.valueOf(currentSequenceNumber);
			int_currentSequenceNumber++;
			newSequenceNumber = String.format("%04d", int_currentSequenceNumber);
			LOGGER.debug("**currentSequenceNumber:" + currentSequenceNumber + " newSequenceNumber:" + newSequenceNumber);
			return newSequenceNumber;
		}
	}
	 
	 /**
	  * This method is to retrieve the appropriate Business object or the department
	 * @param FlexTypePath
	 * @param attKey
	 * @param attValue
	 * @return
	 * @throws WTException
	 */
	public static LCSLifecycleManaged deriveAeroStyleSequenceBO(String FlexTypePath, String attKey, String attValue) throws WTException {
		LCSLifecycleManaged businessObject = null;
		try {
			FlexType BOType = FlexTypeCache.getFlexTypeFromPath(FlexTypePath);

			PreparedQueryStatement statement = new PreparedQueryStatement();
			statement.appendFromTable(LCSLifecycleManaged.class);
			statement.appendSelectColumn(new QueryColumn(LCSLifecycleManaged.class, BUSINESS_OBJECT_IDENTIFIER_LITERAL));
			statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, BOType.getAttribute(attKey).getColumnDescriptorName()), attValue, "="));
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, LCSQuery.TYPED_BRANCH_ID), BOType.getIdNumber(), "="));
			LOGGER.debug("getBusinessObjectByDepatmentName-statement:" + statement.toString());
			SearchResults results = LCSQuery.runDirectQuery(statement);
			LOGGER.debug("**results.getResultsFound BO:" + results.getResultsFound());
			if (results.getResultsFound() > 0) {
				FlexObject flexBO = (FlexObject) results.getResults().elementAt(0);
				businessObject = (LCSLifecycleManaged) LCSQuery.findObjectById("OR:com.lcs.wc.foundation.LCSLifecycleManaged:" + flexBO.getString("LCSLIFECYCLEMANAGED.IDA2A2"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return businessObject;
	}
	/**
	 * This method retrieves the lock associated with a department. If it doesn't exist, create a new one.
	 * @param department The department name.
	 * @return The lock for the department.
	 */
	private static Lock getDepartmentLock(String department) {
		return departmentLocks.computeIfAbsent(department, k -> new ReentrantLock());
	}
	 private static boolean isInterfaceAdmin() {
        try {
            return MethodContext.getContext().getUserName().equals(INTERFACE_ADMIN_USER_NAME);
        } catch (Exception e) {
            LOGGER.error("Encountered an error while fetching current user, error:" + e.getMessage());
        }
        return false;
    }
}
