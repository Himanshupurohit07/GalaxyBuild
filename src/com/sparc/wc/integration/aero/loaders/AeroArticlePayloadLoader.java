package com.sparc.wc.integration.aero.loaders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_SEASON_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_SEASON_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_TYPE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_YEAR_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SENT_YES_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_ARTICLE_CAP_UPDATE_API_CALL;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_ARTICLE_LOGGER_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_LIFECYCLE_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_S4_SHARED_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_VALID_COLORWAY_LIFECYCLE_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_ARTICLE_CAP_UPDATE_SUBDEPT_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_ARTICLE_CAP_UPDATE_CLASS_VALUES_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_SUB_DEPT_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_CLASS_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_LIFECYCLE_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.INTERFACE_ADMIN_USER_NAME;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Objects;

import org.apache.logging.log4j.Logger;

import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductClientModel;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUClientModel;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLinkClientModel;
import com.lcs.wc.season.LCSSeasonSKULinkClientModel;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.sparc.wc.integration.aero.domain.AeroApiCallLogEntry;
import com.sparc.wc.integration.aero.domain.AeroApiCallLogException;
import com.sparc.wc.integration.aero.domain.AeroArticleFlexTypeMaster;
import com.sparc.wc.integration.aero.domain.AeroCustomPluginException;
import com.sparc.wc.integration.aero.domain.AeroPayloadAttribute;
import com.sparc.wc.integration.aero.domain.AeroPayloadAttributeDefinitions;
import com.sparc.wc.integration.aero.repository.AeroArticleRepository;
import com.sparc.wc.integration.aero.util.AeroIntegrationPluginUtil;
import com.sparc.wc.integration.aero.util.AeroPayloadAttributeUtil;
import com.sparc.wc.integration.domain.SparcColorwayUpdateRequest;
import com.sparc.wc.integration.domain.SparcColorwayUpdateResponse;
import com.sparc.wc.integration.util.SparcIntegrationUtil;

import wt.log4j.LogR;
import wt.method.MethodContext;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * Loads the given list of Article/Colorway Payload values into their respective PLM objects.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Renamed method isColorwayLifecycleSet to isColorwayLifecycleValid in order to align to logic change.<br>
 * - Added comments and debug logging points; removed obsolete commented code.<br>
 * - Moved save logic for all flex objects to a single location to ensure all available validation steps are completed first.<br>
 * - Replaced LCSLogic.persist with appropriate object model logic and use of object tagging to mark as integration api origin for plugin bypass.<br>
 * - Enhanced validation logic for non-blanks and locked attributes.<br>
 * - Any non-exiting issues collected by the loader which will be displayed into the log entry will show up with "WARNING" status to avoid leaving the field empty.<br>
 * - Task #9899 (UAT): Aero - Article Inbound: Fix sub-dept/class validation (added methods: loadSubDeptClassConfigMap & isSubDeptClassComboValid)<br>
 * - Task #9989 (UAT): Reworked exception capture for invalid input values and log entry logging enhancements.
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9647 Aero - Colorway Inbound"
 */
public class AeroArticlePayloadLoader implements AeroInboundPayloadLoader<Collection<SparcColorwayUpdateRequest>, Map<SparcColorwayUpdateRequest, List<AeroApiCallLogException>>> {
	
	private static List<AeroPayloadAttribute> PRODUCT_ATTR_LIST = AeroPayloadAttributeDefinitions.
			getCAPArticleUpdateAttributesDefinitionsForFlexType(AERO_PRODUCT_ATTR_GROUP_KEY);
	
	private static List<AeroPayloadAttribute> COLORWAY_ATTR_LIST = AeroPayloadAttributeDefinitions.
			getCAPArticleUpdateAttributesDefinitionsForFlexType(AERO_COLORWAY_ATTR_GROUP_KEY);
	
	private static List<AeroPayloadAttribute> PRODUCT_SEASON_ATTR_LIST = AeroPayloadAttributeDefinitions.
			getCAPArticleUpdateAttributesDefinitionsForFlexType(AERO_PRODUCT_SEASON_ATTR_GROUP_KEY);
	
	private static List<AeroPayloadAttribute> COLORWAY_SEASON_ATTR_LIST = AeroPayloadAttributeDefinitions.
			getCAPArticleUpdateAttributesDefinitionsForFlexType(AERO_COLORWAY_SEASON_ATTR_GROUP_KEY);
	
	private static final Logger LOGGER = LogR.getLogger(AERO_ARTICLE_LOGGER_NAME);
	

	//Cache for storing Sub Department and Class mapping for validation. 
	private static Map<String, List<String>> SUBDEPT_CLASS_MAP = new ConcurrentHashMap<>();
	
	static {
		loadSubDeptClassConfigMap();
	}
	
	private Collection<SparcColorwayUpdateRequest> requestPayloadList;
	
	@Override
	public AeroInboundPayloadLoader<Collection<SparcColorwayUpdateRequest>, Map<SparcColorwayUpdateRequest, List<AeroApiCallLogException>>> setPayload(
			Collection<SparcColorwayUpdateRequest> requestPayloadList) {
		
		if (requestPayloadList != null) {
			this.requestPayloadList = requestPayloadList;
		} else {
			this.requestPayloadList = new ArrayList<SparcColorwayUpdateRequest>();
		}
		
		return this;
	}
	
	/**
	 * Updates the article's related product attributes.
	 * @param criteria The article criteria, i.e. colorway number, season type and year.
	 * @param flexProduct The product object to update.
	 * @param flexColorwaySeason The flex colorway season related to the current colorway being updated. Required for validation.
	 * @param requestPayload The payload containing the values to update into the product.
	 * @return A list of payload attributes which values were updated.
	 * @throws WTException If an error occurs while extracting/setting values from the product object.
	 * @throws AeroApiCallLogException If input criteria or flex product object are missing, 
	 * or if unable to lookup/resolve the internal flex value for the given payload value,
	 * or if unable to locate season-colorway links required to validate non-blanks/locked attributes.
	 * or if non-blanks/locked validation criteria is met. 
	 */
	private static List<AeroPayloadAttribute> updateProductAttributes(SparcColorwayUpdateRequest.Criteria criteria, 
			LCSProduct flexProduct, 
			LCSSKUSeasonLink flexColorwaySeason,
			Map<String, Object> requestPayload) throws WTException, AeroApiCallLogException {
		
		boolean subDeptFound = false;
		boolean classValueFound = false;
		String subDeptValue = null;
		String classValue = null;
		List<AeroPayloadAttribute> changedAttrList = new ArrayList<>();
		
		if (criteria == null) {
			throw new AeroApiCallLogException("Missing input criteria", AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
        }
		
		if (flexProduct == null || flexColorwaySeason == null) {
			throw new AeroApiCallLogException("No associated product or colorway season found for colorway # " + criteria.getScColorwayNo(), 
					AeroApiCallLogEntry.ErrorTypes.INVALID_ATTR_BLANK);
        }
		
		Long productNumber = (Long)flexProduct.getValue(AERO_PRODUCT_NUMBER_ATTR);
		LOGGER.debug("[updateProductAttributes] About to update Article Product attributes for colorway # " + criteria.getScColorwayNo()
				+ " and product #" + productNumber + ".");
		
		
		for (AeroPayloadAttribute attrDef : PRODUCT_ATTR_LIST) {
			
			if (!requestPayload.containsKey(attrDef.getJsonAttributeAlias())) {
				LOGGER.debug("[updateProductAttributes] Skipping product attribute " + attrDef.getFlexAttributeName() + " (" + attrDef.getJsonAttributeAlias() + ") "
						+ "as no input value is present.");
				continue;
			}
			
			LOGGER.debug("[updateProductAttributes] About to process update for product attribute: " + attrDef.getFlexAttributeName()
					+ " (" + attrDef.getJsonAttributeAlias() + ").");
			
			Object internalValue = null;
			Object requestValue = requestPayload.get(attrDef.getJsonAttributeAlias());
			
			if (AERO_PRODUCT_SUB_DEPT_ATTR.equalsIgnoreCase(attrDef.getFlexAttributeName())) {
				subDeptFound = true;
				subDeptValue = ((requestValue != null) ? requestValue.toString() : null); 
			} else if (AERO_PRODUCT_CLASS_ATTR.equalsIgnoreCase(attrDef.getFlexAttributeName())) {
				classValueFound = true;
				classValue = ((requestValue != null) ? requestValue.toString() : null); 
			}
			
			try {
				
				if (requestValue != null && !requestValue.toString().isBlank()) {
					internalValue = AeroPayloadAttributeUtil.lookupInternalValue(flexProduct, attrDef, requestValue);
					LOGGER.debug("[updateProductAttributes] Reverse lookup for product attribute " + attrDef.getFlexAttributeName() + " value " + requestValue
							+ " is: " + internalValue + ".");
					
					if (internalValue == null) {
						throw new Exception("No flex internal value found for '" + requestValue + "'.");
					}
				}
				
			} catch (Exception e) {
				LOGGER.error("[updateProductAttributes] Error mapping value " + requestValue + " to product attribute " + attrDef.getFlexAttributeName() + ". " + e.getMessage(), e);
				throw new AeroApiCallLogException(
						"Unable to map value received (" + requestValue + ") to a valid flex value for (product) payload attribute " + attrDef.getJsonAttributeAlias() + ".", 
						criteria.getScColorwayNo(), 
						criteria.getSeasonType(), 
						Integer.toString(criteria.getYear()),
						AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
			}
			
			try {
				
			LOGGER.debug("[updateProductAttributes] Attribute to update " + attrDef.getFlexAttributeName() + " (" + attrDef.getJsonAttributeAlias() + "), "
						+ "old value is: '" + flexProduct.getValue(attrDef.getFlexAttributeName()) + "', new value is: '" + internalValue + "'.");

				//Start of the IN-241231-0126
				Object oldValue = flexProduct.getValue(attrDef.getFlexAttributeName());

				LOGGER.debug("Product Attribute key------"+attrDef.getFlexAttributeName());
				LOGGER.debug("JSON Product Value------"+internalValue);
				LOGGER.debug("JSON Product Value------"+oldValue);

				if((internalValue != null  && !Objects.equals(internalValue,oldValue)))
				{
					LOGGER.debug("inside if of flexProduct Attributes--"+attrDef.getFlexAttributeName());
					changedAttrList.add(attrDef);
				}
				flexProduct.setValue(attrDef.getFlexAttributeName(), internalValue);
				//End of the IN-241231-0126

				System.out.println("changedAttrList---------------------"+changedAttrList);

				
			
				
			} catch (Exception ie) {
				LOGGER.error("[updateProductAttributes] Error updating product attribute " + attrDef.getFlexAttributeName() + " (" + internalValue + "). " + ie.getMessage(), ie);
				throw new AeroApiCallLogException("Failed to update value " + requestValue + " for (product) payload attribute " + attrDef.getJsonAttributeAlias() + ". " + ie.getMessage(), 
						criteria.getScColorwayNo(), 
						criteria.getSeasonType(), 
						Integer.toString(criteria.getYear()), 
						AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
			}
			
		}//end for loop.
		
		//Validate Sub-department and Class combination.
		if (subDeptFound && classValueFound && !isSubDeptClassComboValid(subDeptValue, classValue)) {
			throw new AeroApiCallLogException(
					"Invalid Sub-department (" + subDeptValue + ") and Class (" + classValue + ") combination found for product #" + productNumber + ".", 
					criteria.getScColorwayNo(), 
					criteria.getSeasonType(), 
					Integer.toString(criteria.getYear()), 
					AeroApiCallLogEntry.ErrorTypes.INVALID_ATTR_COMBINATION);
		}
		
		/*List<LCSSKUSeasonLink> skuSeasonLinkList = AeroArticleRepository.getProductColorwaysSeasonLinks(productNumber);
        
        if (skuSeasonLinkList.isEmpty()) {
        	throw new AeroApiCallLogException("Unable to find colorway season links for product #" + productNumber + ".", 
					criteria.getScColorwayNo(), 
					criteria.getSeasonType(), 
					Integer.toString(criteria.getYear()), 
					AeroApiCallLogEntry.ErrorTypes.ERROR);
        }
        
        try {
			
        	validateAttrLocked(flexProduct, skuSeasonLinkList, changedAttrList);
		
        } catch (LCSException e) {
        	throw new AeroApiCallLogException("Failed to update product #" + productNumber + "," + e.getMessage(), 
					criteria.getScColorwayNo(), 
					criteria.getSeasonType(), 
					Integer.toString(criteria.getYear()), 
					AeroApiCallLogEntry.ErrorTypes.INVALID_ATTR_LOCKED);
        }
        
        LOGGER.debug("[updateProductAttributes] skuSeasonLinkList contains colorway season link? " + skuSeasonLinkList.contains(flexColorwaySeason));
        addReplaceColorwaySeasonLink(flexColorwaySeason, skuSeasonLinkList);
        
        try {
		
        	validateAttrNonBlanks(flexProduct, skuSeasonLinkList, changedAttrList);
        } catch (LCSException e) {
        	throw new AeroApiCallLogException("Failed to update product #" + productNumber + "," + e.getMessage(), 
					criteria.getScColorwayNo(), 
					criteria.getSeasonType(), 
					Integer.toString(criteria.getYear()), 
					AeroApiCallLogEntry.ErrorTypes.INVALID_ATTR_BLANK);
        }*/
        
		return changedAttrList;
	}
	
	/**
	 * Updates the article's related Product Season attributes.
	 * @param criteria The article criteria, i.e. colorway number, season type and year.
	 * @param flexProductSeason The product season object to update.
	 * @param flexProduct The asociated product. This is required to pinpoint the colorway-seasons to validate blanks/locked attributes.
	 * @param flexSeason The associated season. This is required to pinpoint the colorway-seasons to validate blanks/locked attributes.
	 * @param flexColorwaySeason The flex colorway season related to the current colorway being updated. Required for validation.
	 * @param requestPayload The payload containing the values to update into the product-season.
	 * @return A list of payload attributes which values were updated.
	 * @throws WTException If an error occurs while extracting values from any of the given flex objects, or setting values into the product-season object.
	 * @throws AeroApiCallLogException If input criteria or any required flex object are missing, 
	 * or if unable to lookup/resolve the internal flex value for the given payload value,
	 * or if unable to locate season-colorway links required to validate non-blanks/locked attributes.
	 * or if non-blanks/locked validation criteria is met. 
	 */
	private static List<AeroPayloadAttribute> updateProductSeasonAttributes(SparcColorwayUpdateRequest.Criteria criteria, 
			LCSProductSeasonLink flexProductSeason,
			LCSProduct flexProduct, 
			LCSSeason flexSeason,
			LCSSKUSeasonLink flexColorwaySeason,
			Map<String, Object> requestPayload) throws WTException, AeroApiCallLogException {
		
		List<AeroPayloadAttribute> changedAttrList = new ArrayList<>();
		
		if (criteria == null) {
			throw new AeroApiCallLogException("Missing input criteria", AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
        }
		
		if (flexProductSeason == null || flexProduct == null || flexSeason == null || flexColorwaySeason == null) {
			throw new AeroApiCallLogException("No associated product season, product, season or colorway season found for colorway # " + criteria.getScColorwayNo(), 
					AeroApiCallLogEntry.ErrorTypes.INVALID_ATTR_BLANK);
        }
		
		try {
			AeroIntegrationPluginUtil.validateAeroProductType(flexProduct);
		} catch (AeroCustomPluginException pex) {
			throw new AeroApiCallLogException(pex.getMessage(), 
					criteria.getScColorwayNo(), 
					criteria.getSeasonType(), 
					Integer.toString(criteria.getYear()),
					AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
		}
		
		Long productNumber = (Long)flexProduct.getValue(AERO_PRODUCT_NUMBER_ATTR);
		LOGGER.debug("[updateProductSeasonAttributes] About to update Article Product Season attributes for colorway # " + criteria.getScColorwayNo() + ".");
		
		
		for (AeroPayloadAttribute attrDef : PRODUCT_SEASON_ATTR_LIST) {
			
			if (!requestPayload.containsKey(attrDef.getJsonAttributeAlias())) {
				LOGGER.debug("[updateProductSeasonAttributes] Skipping product season attribute " + attrDef.getFlexAttributeName() + " "
						+ "(" + attrDef.getJsonAttributeAlias() + ") as no input value is present.");
				continue;
			}
			
			LOGGER.debug("[updateProductSeasonAttributes] About to process update for product-season attribute: " + attrDef.getFlexAttributeName()
					+ " (" + attrDef.getJsonAttributeAlias() + ").");
			
			Object internalValue = null;
			Object requestValue = requestPayload.get(attrDef.getJsonAttributeAlias());
			
			try {
				
				if (requestValue != null && !requestValue.toString().isBlank()) {
					internalValue = AeroPayloadAttributeUtil.lookupInternalValue(flexProductSeason, attrDef, requestValue);
					LOGGER.debug("[updateProductSeasonAttributes] Reverse lookup for product-season attribute " + attrDef.getFlexAttributeName() + " value " + requestValue
							+ " is: " + internalValue + ".");
					
					if (internalValue == null) {
						throw new Exception("No flex internal value found for '" + requestValue + "'.");
					}
				}
				
			} catch (Exception e) {
				LOGGER.error("[updateProductSeasonAttributes] Error mapping value " + requestValue + " to product-season attribute " + attrDef.getFlexAttributeName() + ". " + e.getMessage(), e);
				throw new AeroApiCallLogException(
						"Unable to map value received (" + requestValue + ") to a valid flex value for (product-season) payload attribute " + attrDef.getJsonAttributeAlias() + ".", 
						criteria.getScColorwayNo(), 
						criteria.getSeasonType(), 
						Integer.toString(criteria.getYear()),
						AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
			}
			
			try {
				
				LOGGER.debug("[updateProductSeasonAttributes] Attribute to update " + attrDef.getFlexAttributeName() + " (" + attrDef.getJsonAttributeAlias() + "), "
						+ "old value is: '" + flexProductSeason.getValue(attrDef.getFlexAttributeName()) + "', new value is: '" + internalValue + "'.");
				
				flexProductSeason.setValue(attrDef.getFlexAttributeName(), internalValue);
				
				changedAttrList.add(attrDef);
				
			} catch (Exception ie) {
				LOGGER.error("[updateProductSeasonAttributes] Error updating product-season attribute " + attrDef.getFlexAttributeName() + " (" + internalValue + "). " + ie.getMessage(), ie);
				throw new AeroApiCallLogException("Failed to update value " + requestValue + " for (product-season) payload attribute " + attrDef.getJsonAttributeAlias() + ". " + ie.getMessage(), 
						criteria.getScColorwayNo(), 
						criteria.getSeasonType(), 
						Integer.toString(criteria.getYear()), 
						AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
			}
			
		}//end for loop.
		
		/*List<LCSSKUSeasonLink> skuSeasonLinkList = AeroArticleRepository.getProductColorwaysSeasonLinks(flexProduct, flexSeason);
		
		if (skuSeasonLinkList.isEmpty()) {
        	throw new AeroApiCallLogException("Unable to find colorway season links for product #" + productNumber + ".", 
					criteria.getScColorwayNo(), 
					criteria.getSeasonType(), 
					Integer.toString(criteria.getYear()), 
					AeroApiCallLogEntry.ErrorTypes.ERROR);
        }
        
		LOGGER.debug("[updateProductSeasonAttributes] skuSeasonLinkList contains colorway season link? " + skuSeasonLinkList.contains(flexColorwaySeason));
		addReplaceColorwaySeasonLink(flexColorwaySeason, skuSeasonLinkList);
		
        try {
        	validateAttrNonBlanks(flexProduct, skuSeasonLinkList, PRODUCT_SEASON_ATTR_LIST);
        } catch (LCSException e) {
        	throw new AeroApiCallLogException("Failed to update product season for product #" + productNumber + "," + e.getMessage(), 
					criteria.getScColorwayNo(), 
					criteria.getSeasonType(), 
					Integer.toString(criteria.getYear()), 
					AeroApiCallLogEntry.ErrorTypes.INVALID_ATTR_BLANK);
        }
        
        try {
        	validateAttrLocked(flexProduct, skuSeasonLinkList, changedAttrList);
        } catch (LCSException e) {
        	throw new AeroApiCallLogException("Failed to update product season for product #" + productNumber + "," + e.getMessage(), 
					criteria.getScColorwayNo(), 
					criteria.getSeasonType(), 
					Integer.toString(criteria.getYear()), 
					AeroApiCallLogEntry.ErrorTypes.INVALID_ATTR_LOCKED);
        }*/
		
		return changedAttrList;
	}
	
	/**
	 * Updates the article's related Colorway attributes.
	 * @param criteria The article criteria, i.e. colorway number, season type and year.
	 * @param flexColorway The colorway object to update.
	 * @param flexColorwaySeason The flex colorway season related to the current colorway being updated. Required for validation.
	 * @param requestPayload The payload containing the values to update into the colorway.
	 * @return A list of payload attributes which values were updated.
	 * @throws WTException If an error occurs while extracting/setting values to the given colorway object.
	 * @throws AeroApiCallLogException If input criteria or the flex colorway object are missing, 
	 * or if unable to lookup/resolve the internal flex value for the given payload value,
	 * or if unable to locate season-colorway links required to validate non-blanks/locked attributes.
	 * or if non-blanks/locked validation criteria is met. 
	 */
	private static List<AeroPayloadAttribute> updateColorwayAttributes(SparcColorwayUpdateRequest.Criteria criteria, 
			LCSSKU flexColorway,
			LCSSKUSeasonLink flexColorwaySeason,
			Map<String, Object> requestPayload) throws WTException, AeroApiCallLogException {
		
		List<AeroPayloadAttribute> changedAttrList = new ArrayList<>();
		
		if (criteria == null) {
			throw new AeroApiCallLogException("Missing input criteria", AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
        }
		
		if (flexColorway == null || flexColorwaySeason == null) {
			throw new AeroApiCallLogException("No associated colorway/colorway-season found for colorway # " + criteria.getScColorwayNo(), 
					AeroApiCallLogEntry.ErrorTypes.INVALID_ATTR_BLANK);
        }
		
		try {
			AeroIntegrationPluginUtil.validateAeroColorwayType(flexColorway);
		} catch (AeroCustomPluginException pex) {
			throw new AeroApiCallLogException(pex.getMessage(), 
					criteria.getScColorwayNo(), 
					criteria.getSeasonType(), 
					Integer.toString(criteria.getYear()),
					AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
		}
		
		LOGGER.debug("[updateColorwayAttributes] About to update Article colorway attributes for colorway # " + criteria.getScColorwayNo() + ".");
		
		for (AeroPayloadAttribute attrDef : COLORWAY_ATTR_LIST) {
			
			if (!requestPayload.containsKey(attrDef.getJsonAttributeAlias())) {
				LOGGER.debug("[updateColorwayAttributes] Skipping colorway season attribute " + attrDef.getFlexAttributeName() + " "
						+ "(" + attrDef.getJsonAttributeAlias() + ") as no input value is present.");
				continue;
			}
			
			LOGGER.debug("[updateColorwayAttributes] About to process update for colorway attribute: " + attrDef.getFlexAttributeName()
					+ " (" + attrDef.getJsonAttributeAlias() + ").");
			
			Object internalValue = null;
			Object requestValue = requestPayload.get(attrDef.getJsonAttributeAlias());
			
			try {
				
				if (requestValue != null && !requestValue.toString().isBlank()) {
					internalValue = AeroPayloadAttributeUtil.lookupInternalValue(flexColorway, attrDef, requestValue);
					LOGGER.debug("[updateColorwayAttributes] Reverse lookup for colorway attribute " + attrDef.getFlexAttributeName() + " value " + requestValue
							+ " is: " + internalValue + ".");
					
					if (internalValue == null) {
						throw new Exception("No flex internal value found for '" + requestValue + "'.");
					}
				}
				
			} catch (Exception e) {
				LOGGER.error("[updateColorwayAttributes] Error mapping value " + requestValue + " to colorway attribute " + attrDef.getFlexAttributeName() + ". " + e.getMessage(), e);
				throw new AeroApiCallLogException(
						"Unable to map value received (" + requestValue + ") to a valid flex value for (colorway) payload attribute " + attrDef.getJsonAttributeAlias() + ".", 
						criteria.getScColorwayNo(), 
						criteria.getSeasonType(), 
						Integer.toString(criteria.getYear()),
						AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
			}
			
			try {
				
				LOGGER.debug("[updateColorwayAttributes] Attribute to update " + attrDef.getFlexAttributeName() + " (" + attrDef.getJsonAttributeAlias() + "), "
						+ "old value is: '" + flexColorway.getValue(attrDef.getFlexAttributeName()) + "', new value is: '" + internalValue + "'.");
				
				flexColorway.setValue(attrDef.getFlexAttributeName(), internalValue);
				
				changedAttrList.add(attrDef);
				
			} catch (Exception ie) {
				LOGGER.error("[updateColorwayAttributes] Error updating colorway attribute " + attrDef.getFlexAttributeName() + " (" + internalValue + "). " + ie.getMessage(), ie);
				throw new AeroApiCallLogException("Failed to update value " + requestValue + " for (colorway) payload attribute " + attrDef.getJsonAttributeAlias() + ". " + ie.getMessage(), 
						criteria.getScColorwayNo(), 
						criteria.getSeasonType(), 
						Integer.toString(criteria.getYear()), 
						AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
			}
			
		}//end for loop.
		
		/*List<LCSSKUSeasonLink> skuSeasonLinkList = AeroArticleRepository.getColorwaySeasonLink(Long.parseLong(criteria.getScColorwayNo()));
		
		if (skuSeasonLinkList.isEmpty()) {
        	throw new AeroApiCallLogException("Unable to find colorway season links for colorway #" + criteria.getScColorwayNo() + ".", 
					criteria.getScColorwayNo(), 
					criteria.getSeasonType(), 
					Integer.toString(criteria.getYear()), 
					AeroApiCallLogEntry.ErrorTypes.ERROR);
        }
		
		try {
        	validateAttrLocked(flexColorway, skuSeasonLinkList, changedAttrList);
        } catch (LCSException e) {
        	throw new AeroApiCallLogException("Failed to update colorway #" + criteria.getScColorwayNo() + "," + e.getMessage(), 
					criteria.getScColorwayNo(), 
					criteria.getSeasonType(), 
					Integer.toString(criteria.getYear()), 
					AeroApiCallLogEntry.ErrorTypes.INVALID_ATTR_LOCKED);
        }
		
		LOGGER.debug("[updateColorwayAttributes] skuSeasonLinkList contains colorway season link? " + skuSeasonLinkList.contains(flexColorwaySeason));
		addReplaceColorwaySeasonLink(flexColorwaySeason, skuSeasonLinkList);
		
        try {
        	validateAttrNonBlanks(flexColorway, skuSeasonLinkList, COLORWAY_ATTR_LIST);
        } catch (LCSException e) {
        	throw new AeroApiCallLogException("Failed to update colorway #" + criteria.getScColorwayNo() + "," + e.getMessage(), 
					criteria.getScColorwayNo(), 
					criteria.getSeasonType(), 
					Integer.toString(criteria.getYear()), 
					AeroApiCallLogEntry.ErrorTypes.INVALID_ATTR_BLANK);
        }
      */  
		return changedAttrList;
	}
	
	/**
	 * Updates the article's related Colorway-Season attributes.
	 * @param criteria The article criteria, i.e. colorway number, season type and year.
	 * @param flexColorwaySeason The colorway-season object to update.
	 * @param requestPayload The payload containing the values to update into the colorway-season.
	 * @return A list of payload attributes which values were updated.
	 * @throws WTException If an error occurs while extracting/setting values to the given colorway-season object.
	 * @throws AeroApiCallLogException If input criteria or the flex colorway-season object are missing, 
	 * or if unable to lookup/resolve the internal flex value for the given payload value,
	 * or if non-blanks/locked validation criteria is met. 
	 */
	private static List<AeroPayloadAttribute> updateColorwaySeasonAttributes(SparcColorwayUpdateRequest.Criteria criteria, 
			LCSSKUSeasonLink flexColorwaySeason,
			Map<String, Object> requestPayload) throws WTException, AeroApiCallLogException {
		
		List<AeroPayloadAttribute> changedAttrList = new ArrayList<>();
		
		if (criteria == null) {
			throw new AeroApiCallLogException("Missing input criteria", AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
        }
		
		if (flexColorwaySeason == null) {
			throw new AeroApiCallLogException("No associated colorway season found for colorway # " + criteria.getScColorwayNo(), AeroApiCallLogEntry.ErrorTypes.INVALID_ATTR_BLANK);
        }
		
		//Since the colorway lifecycle attribute may be updated from the payload, we extract the current value at this step to to use it later on. 
	//	boolean requiresLockCheck = (isArticleColorwaySentToS4(flexColorwaySeason) && isColorwayLifecycleValid(flexColorwaySeason));
		
		LOGGER.debug("[updateColorwaySeasonAttributes] About to update Article Colorway Season attributes for colorway # " + criteria.getScColorwayNo() + ".");
		
		
		for (AeroPayloadAttribute attrDef : COLORWAY_SEASON_ATTR_LIST) {
			
			if (!requestPayload.containsKey(attrDef.getJsonAttributeAlias())) {
				LOGGER.debug("[updateColorwaySeasonAttributes] Skipping colorway-season attribute " + attrDef.getFlexAttributeName() + " "
						+ "(" + attrDef.getJsonAttributeAlias() + ") as no input value is present.");
				continue;
			}
			
			LOGGER.debug("[updateColorwaySeasonAttributes] About to process update for colorway-season attribute: " + attrDef.getFlexAttributeName()
					+ " (" + attrDef.getJsonAttributeAlias() + ").");
			
			Object internalValue = null;
			Object requestValue = requestPayload.get(attrDef.getJsonAttributeAlias());
			
			try {
				
				if (requestValue != null && !requestValue.toString().isBlank()) {
					internalValue = AeroPayloadAttributeUtil.lookupInternalValue(flexColorwaySeason, attrDef, requestValue);
					LOGGER.debug("[updateColorwaySeasonAttributes] Reverse lookup for colorway-season attribute " + attrDef.getFlexAttributeName() + " value " + requestValue
							+ " is: " + internalValue + ".");
					
					if (internalValue == null) {
						throw new Exception("No flex internal value found for '" + requestValue + "'.");
					}
				}
				
			} catch (Exception e) {
				LOGGER.error("[updateColorwaySeasonAttributes] Error mapping value " + requestValue + " to colorway-season attribute " + attrDef.getFlexAttributeName() + ". " + e.getMessage(), e);
				throw new AeroApiCallLogException(
						"Unable to map value received (" + requestValue + ") to a valid flex value for (colorway-season) payload attribute " + attrDef.getJsonAttributeAlias() + ".", 
						criteria.getScColorwayNo(), 
						criteria.getSeasonType(), 
						Integer.toString(criteria.getYear()),
						AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
			}
			
			try {
				
				LOGGER.debug("[updateColorwaySeasonAttributes] Attribute to update " + attrDef.getFlexAttributeName() + " (" + attrDef.getJsonAttributeAlias() + "), "
						+ "old value is: '" + flexColorwaySeason.getValue(attrDef.getFlexAttributeName()) + "', new value is: '" + internalValue + "'.");
				
				flexColorwaySeason.setValue(attrDef.getFlexAttributeName(), internalValue);
				
				changedAttrList.add(attrDef);
				
			} catch (Exception ie) {
				LOGGER.error("[updateColorwaySeasonAttributes] Error updating colorway-season attribute " + attrDef.getFlexAttributeName() + " (" + internalValue + "). " + ie.getMessage(), ie);
				throw new AeroApiCallLogException("Failed to update value " + requestValue + " for (colorway-season) payload attribute " + attrDef.getJsonAttributeAlias() + ". " + ie.getMessage(), 
						criteria.getScColorwayNo(), 
						criteria.getSeasonType(), 
						Integer.toString(criteria.getYear()), 
						AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
			}
			
		}//end for loop.
		
        
        /*try {
        	validateAttrNonBlanks(flexColorwaySeason, COLORWAY_SEASON_ATTR_LIST);
        } catch (LCSException e) {
        	throw new AeroApiCallLogException("Failed to update colorway season for colorway #" + criteria.getScColorwayNo() + "," + e.getMessage(), 
					criteria.getScColorwayNo(), 
					criteria.getSeasonType(), 
					Integer.toString(criteria.getYear()), 
					AeroApiCallLogEntry.ErrorTypes.INVALID_ATTR_BLANK);
        }
        
        try {
        	if (requiresLockCheck) {
        		//validateAttrLocked(flexColorwaySeason, changedAttrList);
        		LOGGER.debug("[updateColorwaySeasonAttributes] Update Lock validation is required for colorway season object.");
        		AeroPayloadAttributeUtil.validateLocked(flexColorwaySeason, changedAttrList);
        	} else {
        		LOGGER.debug("[updateColorwaySeasonAttributes] Skipping locked validation check for colorway season object.");
        	}
        } catch (LCSException e) {
        	throw new AeroApiCallLogException("Failed to update colorway season for colorway #" + criteria.getScColorwayNo() + "," + e.getMessage(), 
					criteria.getScColorwayNo(), 
					criteria.getSeasonType(), 
					Integer.toString(criteria.getYear()), 
					AeroApiCallLogEntry.ErrorTypes.INVALID_ATTR_LOCKED);
        }*/
		
		return changedAttrList;
	}
	
	@Override
	public Map<SparcColorwayUpdateRequest, List<AeroApiCallLogException>> load() throws WTException, WTPropertyVetoException, Exception {
		
		Map<SparcColorwayUpdateRequest, List<AeroApiCallLogException>> responseMap = new HashMap<>();
		
		if (requestPayloadList == null || requestPayloadList.isEmpty()) {
			return responseMap;
		}
		
		for (SparcColorwayUpdateRequest updRequest : requestPayloadList) {
			
			List<AeroApiCallLogException> errorList = new ArrayList<>();
			
			if (updRequest == null) {
				throw new AeroApiCallLogException("Unable to process update request, no input has been provided.", 
						AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
			}
			
			try {
				LOGGER.debug("[load] About to validate criteria for update request...");
				validateRequestCriteria(updRequest.getCriteria());
				
				String colorwayNumber = trimZeroPrefix(updRequest.getCriteria().getScColorwayNo());
				LOGGER.debug("[load] Update request criteria for colorway #" + colorwayNumber + " (" + updRequest.getCriteria().getScColorwayNo() + ") is validated OK.");
				
				
				if (updRequest.getAttrs() == null || updRequest.getAttrs().isEmpty()) {
					throw new AeroApiCallLogException("No attributes to update were found for colorway.",
							updRequest.getCriteria().getScColorwayNo(),
							updRequest.getCriteria().getSeasonType(),
							"" + updRequest.getCriteria().getYear(),
							AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
				}
				
				List<AeroArticleFlexTypeMaster> flexColorwayMasterList = AeroArticleRepository.getColorwaySeasonDetails(colorwayNumber);
				
				if (flexColorwayMasterList.isEmpty()) {
					throw new AeroApiCallLogException("Colorway was not found or colorway is not related to an Aeropostale Season type.", 
							updRequest.getCriteria().getScColorwayNo(),
							updRequest.getCriteria().getSeasonType(),
							"" + updRequest.getCriteria().getYear(),
							AeroApiCallLogEntry.ErrorTypes.NOT_FOUND);
				}
				
				AeroArticleFlexTypeMaster colorwayMaster = getMatchingColorwayMasterForCriteria(updRequest.getCriteria().getSeasonType(), 
						updRequest.getCriteria().getYear(), 
						flexColorwayMasterList);
				
				if (colorwayMaster == null) {
					throw new AeroApiCallLogException("No matching colorway found for the requested season criteria.",
							updRequest.getCriteria().getScColorwayNo(),
							updRequest.getCriteria().getSeasonType(),
							"" + updRequest.getCriteria().getYear(),
							AeroApiCallLogEntry.ErrorTypes.NOT_FOUND);
				}
				
				String colorwaySeasonName = colorwayMaster.getSeason().getName();
				LOGGER.debug("[load] Criteria for Colorway # " + colorwayNumber + " found a matching result: " + colorwaySeasonName);

				//Update product attributes.
				LCSProductClientModel prodModel = new LCSProductClientModel();
				prodModel.load(colorwayMaster.getProduct());
				List<AeroPayloadAttribute> updatedProdAttrList = updateProductAttributes(updRequest.getCriteria(),
						prodModel,
						colorwayMaster.getColorwaySeasonLink(),
						updRequest.getAttrs());

				if (!updatedProdAttrList.isEmpty()) {
					LOGGER.info("[load] Found " + updatedProdAttrList.size() + " attribute updates for product. Saving product...");
					try {
						prodModel.save();
					} catch (Exception ex) {
						throw ex;
					}
					LOGGER.info("[load] Product has been saved.");

				} else {
					LOGGER.debug("[load] No updates found for product. Skipping product...");
				}

				//Update colorway attributes.
				LCSSKUClientModel skuModel = new LCSSKUClientModel();
				skuModel.load(colorwayMaster.getColorway());
				List<AeroPayloadAttribute> updateSkuAttrList = updateColorwayAttributes(updRequest.getCriteria(),
						skuModel,
						colorwayMaster.getColorwaySeasonLink(),
						updRequest.getAttrs());

				if (!updateSkuAttrList.isEmpty()) {
					LOGGER.info("[load] Found " + updateSkuAttrList.size() + " attribute updates for colorway. Saving colorway...");

					try {
						skuModel.save();
					} catch (Exception ex) {
						throw ex;
					}

					LOGGER.info("[load] Colorway has been saved.");

				} else {
					LOGGER.debug("[load] No updates found for colorway. Skipping colorway...");
				}
				//Update product season link attributes


				LCSSeasonSKULinkClientModel skuSeasonModel = new LCSSeasonSKULinkClientModel();
				LCSSKUSeasonLink objSKUSeasonLink = (LCSSKUSeasonLink) LCSSeasonQuery.findSeasonProductLink(colorwayMaster.getColorwaySeasonLink().getSkuMaster(),colorwayMaster.getColorwaySeasonLink().getSeasonMaster());
				LOGGER.debug("objSKUSeasonLink isEffectLatest-----"+objSKUSeasonLink.isEffectLatest());
				LOGGER.debug("objSKUSeasonLink SeasonLinkType-----"+objSKUSeasonLink.getSeasonLinkType());

				skuSeasonModel.load(objSKUSeasonLink);
				List<AeroPayloadAttribute> updatedSkuSeasonAttrList = updateColorwaySeasonAttributes(updRequest.getCriteria(),
						skuSeasonModel,
						updRequest.getAttrs());

				if (!updatedSkuSeasonAttrList.isEmpty()) {
					LOGGER.info("[load] Found " + updatedSkuSeasonAttrList.size() + " attribute updates for colorway season. Saving colorway season...");

					try {
						skuSeasonModel.save();
					} catch (Exception ex) {
						throw ex;
					}

					LOGGER.info("[load] Colorway-Season Link has been saved.");

				} else {
					LOGGER.debug("[load] No updates found for colorway season. Skipping colorway season...");
				}

				LCSSeasonProductLinkClientModel prodSeasonModel = new LCSSeasonProductLinkClientModel();
				LCSSKUSeasonLink objSkuSeasLink = (LCSSKUSeasonLink) LCSSeasonQuery.findSeasonProductLink(colorwayMaster.getColorwaySeasonLink().getSkuMaster(),colorwayMaster.getColorwaySeasonLink().getSeasonMaster());
       			LOGGER.debug("objSkuSeaslink----------------"+objSkuSeasLink.isEffectLatest());
				LCSProductSeasonLink objProdSeasLink =(LCSProductSeasonLink) objSkuSeasLink.getProductLink();
				LOGGER.debug("ObjProdSeasonLink------effectlatest----------"+objProdSeasLink.isEffectLatest());
				
				prodSeasonModel.load(objProdSeasLink);
				
				List<AeroPayloadAttribute> updateProdSeasonAttrList = updateProductSeasonAttributes(updRequest.getCriteria(),
						prodSeasonModel,
						colorwayMaster.getProduct(),
						colorwayMaster.getSeason(),
						colorwayMaster.getColorwaySeasonLink(),
						updRequest.getAttrs());

				if (!updateProdSeasonAttrList.isEmpty()) {
					LOGGER.info("[load] Found " + updateProdSeasonAttrList.size() + " attribute updates for product season. Saving colorway product...");
					//LCSLogic.persist(colorwayMaster.getProductSeasonLink(), skipPlugins, bypassCheckoutOnUpdate);
					try {
						prodSeasonModel.save();
					} catch (Exception ex) {				
						throw ex;
					}
					
					LOGGER.info("[load] Product-Season Link has been saved.");
					
				} else {
					LOGGER.debug("[load] No updates found for product season. Skipping product season...");
				}
				
				responseMap.put(updRequest, errorList);
				
				untagArticleFlexObjects(colorwayMaster.getProduct(), 
						colorwayMaster.getColorwaySeasonLink(), 
						colorwayMaster.getProductSeasonLink(), 
						colorwayMaster.getColorway());
				
			} catch (AeroApiCallLogException aeroEx) {
				errorList.add(aeroEx);
				LOGGER.error("[load] Failed to process update request for colorway # " + ((updRequest.getCriteria() != null) ? updRequest.getCriteria().getScColorwayNo() : "") 
						+ ", " + aeroEx.getMessage() + ".", aeroEx);
				responseMap.put(updRequest, errorList);
			} catch (Exception e) {
				
				String colorwayNumber = ((updRequest.getCriteria() != null) ? updRequest.getCriteria().getScColorwayNo() : "");
				String seasonType = ((updRequest.getCriteria() != null) ? updRequest.getCriteria().getSeasonType() : "");
				String seasonYear = ((updRequest.getCriteria() != null) ? "" + updRequest.getCriteria().getYear() : "");
				
				errorList.add(new AeroApiCallLogException(e.getMessage(), colorwayNumber, seasonType, seasonYear, AeroApiCallLogEntry.ErrorTypes.ERROR));
				responseMap.put(updRequest, errorList);
				
				LOGGER.error("[load] Failed to process update request for colorway # " + colorwayNumber + ", " + e.getMessage() + ".", e);
				
			}
			
		}//end for loop.
		
		return responseMap;
	}
	
	/**
	 * Checks the request criteria is valid. 
	 * A valid request criteria is one that includes all mandatory attributes required to update an article/colorway.
	 * @param criteria The request criteria to check.
	 * @throws AeroApiCallLogException If no colorway criteria has been provided or if one or more mandatory attributes are missing.
	 */
	private void validateRequestCriteria(SparcColorwayUpdateRequest.Criteria criteria) throws AeroApiCallLogException {
		
		if (criteria == null) {
			throw new AeroApiCallLogException("Invalid request criteria: No colorway criteria has been provided.", 
					AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
		}
		
		if (criteria.getScColorwayNo() == null) {
			throw new AeroApiCallLogException("Invalid request criteria: Colorway number is missing.",
					AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
		}
		
		if (criteria.getSeasonType() == null) {
			throw new AeroApiCallLogException("Invalid request criteria: Colorway season type is missing.",
					criteria.getScColorwayNo(),
					AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
		}
		
		if (criteria.getYear() <= 0) {
			throw new AeroApiCallLogException("Invalid request criteria: Colorway season year is missing.",
					criteria.getScColorwayNo(),
					AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA);
		}
		
	}
	
	/**
	 * Returns the matching colorway master container for the given seasonType and year.
	 * @param seasonType The season type criteria.
	 * @param year The season year criteria.
	 * @param flexColorwayMasterList The list of flex master containers to match against.
	 * @return true, if the given season type and year criteria matches the flex object, or false otherwise.
	 * @throws WTException If an error occurs while retrieving attributes from the flex season.
	 */
	private static AeroArticleFlexTypeMaster getMatchingColorwayMasterForCriteria(String seasonType, 
			int year, 
			List<AeroArticleFlexTypeMaster> flexColorwayMasterList) throws WTException {
		
		AeroArticleFlexTypeMaster flexMaster = null;
		
		if (flexColorwayMasterList == null || flexColorwayMasterList.isEmpty() || seasonType == null) {
			return flexMaster;
		}
		
		for (AeroArticleFlexTypeMaster tempMaster : flexColorwayMasterList) {
			
			if (tempMaster.getSeason() == null) {
				continue;
			}
			
			String flexSeasonType = AeroPayloadAttributeUtil.lookupEnumSecondaryKeyAeroValue(tempMaster.getSeason(), AERO_SEASON_TYPE_ATTR);
			
			if (flexSeasonType == null) {
				flexSeasonType = AeroPayloadAttributeUtil.lookupEnumSecondaryKeyValue(tempMaster.getSeason(), AERO_SEASON_TYPE_ATTR);
			}
			
			String flexYear =  AeroPayloadAttributeUtil.lookupEnumSecondaryKeyValue(tempMaster.getSeason(), AERO_SEASON_YEAR_ATTR);
			
			if (seasonType.equalsIgnoreCase(flexSeasonType) && Integer.toString(year).equals(flexYear)) {
				flexMaster = tempMaster;
				break;
			}
			
		}//end for loop.
		
		return flexMaster;
	}
	
	/**
	 * Untags the given flex objects as integration api origin.
	 * @param product The product to untag.
	 * @param colorwaySeasonLink The colorway-season to untag.
	 * @param productSeasonLink The product-season to untag.
	 * @param colorway The colorway to untag.
	 */
	private static void untagArticleFlexObjects(LCSProduct product,
			LCSSKUSeasonLink colorwaySeasonLink,
			LCSProductSeasonLink productSeasonLink,
			LCSSKU colorway) {
		
		LOGGER.debug("[untagArticleFlexObjects] Product untagged as integration api origin: "
    			+ AeroIntegrationPluginUtil.untagAsIntegrationApiOrigin(product) + ".");
		LOGGER.debug("[untagArticleFlexObjects] Colorway-Season Link untagged as integration api origin: "
    			+ AeroIntegrationPluginUtil.untagAsIntegrationApiOrigin(colorwaySeasonLink) + ".");
		LOGGER.debug("[untagArticleFlexObjects] Product-Season Link untagged as integration api origin: "
    			+ AeroIntegrationPluginUtil.untagAsIntegrationApiOrigin(productSeasonLink) + ".");
		LOGGER.debug("[untagArticleFlexObjects] Colorway untagged as integration api origin: "
    			+ AeroIntegrationPluginUtil.untagAsIntegrationApiOrigin(colorway) + ".");
	}
	
	/**
	 * Add or Replaces the given colorway season link into the given list.
	 * If the colorway season is found in the list, then it'll be replaced, else it'll be added.
	 * @param colorwaySeasonLink The colorway season link to add/replace.
	 * @param colorwaySeasonLinkList The list of colorway season links where the given colorway season link is to be added or replaced.
	 */
	private static void addReplaceColorwaySeasonLink(LCSSKUSeasonLink colorwaySeasonLink, List<LCSSKUSeasonLink> colorwaySeasonLinkList) {
		
		if (colorwaySeasonLink == null || colorwaySeasonLinkList == null || !colorwaySeasonLinkList.contains(colorwaySeasonLink)) {
			return;
		}
		
		int foundPos = -1;
		
		for (foundPos = 0; foundPos < colorwaySeasonLinkList.size(); foundPos += 1) {
			if (colorwaySeasonLink.toString().equals(colorwaySeasonLinkList.get(foundPos).toString())) {
				break;
			}
		}
		
		if (foundPos >= 0) {
			LOGGER.debug("[addReplaceColorwaySeasonLink] Updated colorway season link " + colorwaySeasonLinkList + " is REPLACED.");
			colorwaySeasonLinkList.set(foundPos, colorwaySeasonLink);
		} else {
			LOGGER.debug("[addReplaceColorwaySeasonLink] Updated colorway season link " + colorwaySeasonLinkList + " is ADDED.");
			colorwaySeasonLinkList.add(colorwaySeasonLink);
		}
		
	}
	
	/**
     * Checks whether the colorway lifecycle attribute for the given colorway season link has a valid value set.
     * @param colorwaySeasonLink The colorway season link to check.
     * @return true if the colorway season link has a value set, false otherwise.
     * @throws WTException If an error occurred while extracting the colorway lifecycle value from the colorway season link object. 
     */
	private static boolean isColorwayLifecycleValid(LCSSKUSeasonLink colorwaySeasonLink,Boolean isBlank) throws WTException {
    	
    	if (colorwaySeasonLink == null || AERO_VALID_COLORWAY_LIFECYCLE_VALUES.isEmpty()) {
    		return false;
    	}
    	
    	String colorwayLifecycle = (String)colorwaySeasonLink.getValue(AERO_COLORWAY_LIFECYCLE_ATTR);
    	
		if(isInterfaceAdmin() && isBlank){
			return (colorwayLifecycle != null && AERO_COLORWAY_LIFECYCLE_VALUES.contains(colorwayLifecycle));
		}else {
			return (colorwayLifecycle != null && AERO_VALID_COLORWAY_LIFECYCLE_VALUES.contains(colorwayLifecycle));
		}
	}

	private static boolean isInterfaceAdmin() {
		try {
			return MethodContext.getContext().getUserName().equals(INTERFACE_ADMIN_USER_NAME);
		} catch (Exception e) {
			LOGGER.error("Encountered an error while fetching current user, error:" + e.getMessage());
		}
		return false;
	}
    /**
     * Checks within the given colorway season link whether the colorway has been sent to S4 or not by inspecting the S4 Shared attribute.
     * @param colorwaySeasonLink The colorway season link to check.
     * @return true if S4 Shared status is yes (colorway has been sent to S4 previously), false otherwise.
     * @throws WTException If an error occurred while extracting the colorway S4 Shared value from the colorway season link object.
     */
    private static boolean isArticleColorwaySentToS4(LCSSKUSeasonLink colorwaySeasonLink) throws WTException {
    	
    	if (colorwaySeasonLink == null) {
    		return false;
    	}
    	
    	return AERO_SENT_YES_VALUE.equalsIgnoreCase((String)colorwaySeasonLink.getValue(AERO_COLORWAY_S4_SHARED_ATTR));
    }
    
    /**
     * Conditionally checks for non-blanks attribute values for the given flex typed object if the colorway lifecycle value within any of the given 
     * colorway season links is not empty, and if the list of attributes given contain any attributes that require non-blank check.
     * @param flexTyped The flex typed object to check for non-blank attributes.
     * @param skuSeasonLinkList The list of colorway season links to inspect the status of the colorway cycle attribute.
     * @param attrList The list of attributes to inspect. Only those attributes marked for non-blank will be checked.
     * @throws LCSException If a mandatory non-blank value is found to be blank. 
     * @throws WTException If an error occurred while extracting attribute values from the flex objects given.
     */
    private static void validateAttrNonBlanks(FlexTyped flexTyped, 
    		List<LCSSKUSeasonLink> skuSeasonLinkList,
    		List<AeroPayloadAttribute> attrList) throws LCSException, WTException {
    	
    	if (skuSeasonLinkList == null || skuSeasonLinkList.isEmpty() || attrList == null || attrList.isEmpty()) {
    		LOGGER.debug("[validateAttrNonBlanks] Skipping non blank validation check.");
    		return;
    	}
		StringBuilder blankedAttrs = new StringBuilder();
		boolean mustValidate = false;
    	//Check if non-blank validation should occur or not.
    	for (LCSSKUSeasonLink skuSL : skuSeasonLinkList) {
			mustValidate = isColorwayLifecycleValid(skuSL,true);
    		
    		if (mustValidate) {
    			break;
    		}
    	}//end for loop.
    	
    	if (mustValidate) {
    		LOGGER.debug("[validateAttrNonBlanks] No-blanks validation is required for this flex object.");
    		
    		List<AeroPayloadAttribute> nonBlankAttrlist = AeroPayloadAttributeUtil.filterNonBlanks(attrList);
    		
    		for (AeroPayloadAttribute attr : nonBlankAttrlist) {
        		
        		Object value = AeroPayloadAttributeUtil.getAttributeValue(flexTyped, attr);
				//System.out.println("validateAttrNonBlanks ---"+attr + "----" + value);
        		
				if (value == null || value.toString().trim().isEmpty()) {
					if (blankedAttrs.length() > 0) {
						blankedAttrs.append(", ");
					}
					blankedAttrs.append(AeroPayloadAttributeUtil.getAttributeDisplayName(flexTyped, attr));
				}
			}

			if (blankedAttrs.length() > 0) {
				throw new LCSException("Requested action is unable to complete.\n\r"
						+ "The following list of attributes cannot be empty (blank):" + blankedAttrs.toString() + ".");
			}
		}
    }
    
    /**
     * Conditionally checks for non-blanks attribute values for the given Colorway Season Link object if the colorway lifecycle value within is not empty, 
     * and if the list of attributes given contain any attributes that require non-blank check.
     * @param skuSeasonLink The colorway season link to check for non-blank attributes and to inspect the status of the colorway cycle attribute.
     * @param attrList The list of attributes to inspect. Only those attributes marked for non-blank will be checked.
     * @throws LCSException If a mandatory non-blank value is found to be blank. 
     * @throws WTException If an error occurred while extracting attribute values from the flex objects given.
     */
    private static void validateAttrNonBlanks(LCSSKUSeasonLink skuSeasonLink,
    		List<AeroPayloadAttribute> attrList) throws LCSException, WTException {
    	
    	if (skuSeasonLink == null || attrList == null || attrList.isEmpty()) {
    		LOGGER.debug("[validateAttrNonBlanks] Skipping non blank validation check.");
    		return;
    	}
    	
		if (isColorwayLifecycleValid(skuSeasonLink,true)) {
    		LOGGER.debug("[validateAttrNonBlanks] No-blanks validation is required for this flex object.");
			
    		
    		List<AeroPayloadAttribute> nonBlankAttrlist = AeroPayloadAttributeUtil.filterNonBlanks(attrList);
    		
    		for (AeroPayloadAttribute attr : nonBlankAttrlist) {
        		
        		Object value = AeroPayloadAttributeUtil.getAttributeValue(skuSeasonLink, attr);
        		
        		if (value == null || value.toString().trim().isEmpty()) {
        			
        			throw new LCSException("Article update action aborted."
        					+ "The following list of attributes cannot be empty (blank): " + AeroPayloadAttributeUtil.getAttributeAliasNames(nonBlankAttrlist) + ".");
            	}
        		
        	}
    	}
    	
    }
    
    /**
     * Conditionally checks for update-locked attributes for the given Source To Season Link object if any of the colorways have been sent to S4,
     * and if the list of attributes given contain any attributes that require update-locked check.
     * @param flexTyped The flex typed object to check for locked attributes.
     * @param skuSeasonLinkList The list of colorway season links to inspect the status of the S4 Shared attribute status.
     * @param attrList The list of attributes to inspect. Only those attributes marked for update-locked will be checked.
     * @throws LCSException If a mandatory update locked attribute value is detected.
     * @throws WTException If an error occurred while extracting attribute values from the flex objects given.
     */
    private static void validateAttrLocked(FlexTyped flexTyped, 
    		List<LCSSKUSeasonLink> skuSeasonLinkList,
    		List<AeroPayloadAttribute> attrList) throws LCSException, WTException {
    	
    	if (skuSeasonLinkList == null || skuSeasonLinkList.isEmpty() || attrList == null || attrList.isEmpty()) {
    		LOGGER.debug("[validateAttrLocked] Skipping locked validation check.");
    		return;
    	}
    	
    	boolean mustValidate = false;
    	//Check if locked validation should occur or not.
    	for (LCSSKUSeasonLink skuSL : skuSeasonLinkList) {
			mustValidate = (isArticleColorwaySentToS4(skuSL) && isColorwayLifecycleValid(skuSL,false));
			LOGGER.debug("isArticleColorwaySentToS4(skuSL)---- " + isArticleColorwaySentToS4(skuSL) + "----isColorwayLifecycleValid(skuSL)-----" + isColorwayLifecycleValid(skuSL,false) +  "-----mustValidate---- Value" + mustValidate);
			if (mustValidate) {
    			break;
    		}
    	}//end for loop.
    	
    	if (mustValidate) {
    	LOGGER.debug("[validateAttrLocked] Update Lock validation is required for this flex object.");
    	AeroPayloadAttributeUtil.validateLocked(flexTyped, attrList);
    	}
    	
    }
	
	/**
	 * Identifies the overall status of the load process based on reviewing the status of each colorway loaded from the given map.<br>
	 * - If the status of any of the articles/colorways loaded is error, then the overall status is FAIL.<br>
	 * - If the status of any of the articles/colorways loaded is any other different from error, then the overall status is SUCCESS_WARNING.<br>
	 * - If no errors are given, then the status is SUCCESS.
	 * @param responseMap A map containing a list of colorways and any errors associated with each.
	 * @return The overall load status.
	 */
	public static AeroApiCallLogEntry.Status resolveOverallLoadStatus(final Map<SparcColorwayUpdateRequest, List<AeroApiCallLogException>> responseMap) {
		
		List<AeroApiCallLogException> allErrors = new ArrayList<>();
		
		responseMap.forEach((updRequest, errorList) -> {
			allErrors.addAll(errorList);
		});
		
        return resolveOverallLoadStatus(allErrors);
    }
	
	/**
	 * Identifies the overall status of the article load process based on reviewing the status of each article/colorway loaded.<br>
	 * - If the status of any of the articles/colorways loaded is error, then the overall status is FAIL.<br>
	 * - If the status of any of the articles/colorways loaded is any other different from error, then the overall status is SUCCESS_WARNING.<br>
	 * - If no errors are given, then the status is SUCCESS.
	 * @param errors A list of various types of errors.
	 * @return The overall status based on the analysis of the given list of error types.
	 */
	public static AeroApiCallLogEntry.Status resolveOverallLoadStatus(final List<AeroApiCallLogException> errors) {
        if (errors == null || errors.isEmpty()) {
            return AeroApiCallLogEntry.Status.SUCCESS;
        }
        final boolean errorsExist = errors.stream()
                .map(err -> err.getErrorType())
                .filter(type -> type != null)
                .anyMatch(type -> AeroApiCallLogEntry.ErrorTypes.ERROR == type);
        if (errorsExist) {
            return AeroApiCallLogEntry.Status.FAIL;
        }
        final boolean warningExists = errors.stream()
                .map(err -> err.getErrorType())
                .filter(type -> type != null)
                .anyMatch(type -> AeroApiCallLogEntry.ErrorTypes.ERROR != type);
        if (warningExists) {
            return AeroApiCallLogEntry.Status.SUCCESS_WARNINGS;
        }
        return AeroApiCallLogEntry.Status.SUCCESS;
    }
	
	/**
	 * Identifies the update response error type for a given article/colorway update error.<br>
	 * - If the api error type is 'error', then the resulting response error will also be 'error'.<br>
	 * - If the status of any of the colorways loaded is warning, then the overall status is SUCCESS_WARNING.<br>
	 * @param error A error to resolve its type from.
	 * @return The overall status based on the analysis of the given list of error types.
	 */
	private static SparcColorwayUpdateResponse.ErrorType resolveApiUpdateErrorType(final AeroApiCallLogException error) {
		
		SparcColorwayUpdateResponse.ErrorType updateErrorType = SparcColorwayUpdateResponse.ErrorType.ERROR; 
		
        if (error != null && 
        		AeroApiCallLogEntry.ErrorTypes.ERROR != error.getErrorType() && 
        		AeroApiCallLogEntry.ErrorTypes.INVALID_INPUT_CRITERIA != error.getErrorType()) {
        	updateErrorType = SparcColorwayUpdateResponse.ErrorType.WARNING;
        }
        
        return updateErrorType;
    }
	
	/**
	 * Creates the update response payload. for the given response map info.
	 * @param responseMap The map containing each colorway processed and any associated errors when available.
	 * @return The update response payload.
	 */
	public static SparcColorwayUpdateResponse createUpdateResponse(Map<SparcColorwayUpdateRequest, List<AeroApiCallLogException>> responseMap) {
		
		SparcColorwayUpdateResponse response = new SparcColorwayUpdateResponse();
		
		responseMap.forEach((colorwayRequest, errorList) -> {
			
			String colorwayNumber = ((colorwayRequest.getCriteria() != null && colorwayRequest.getCriteria().getScColorwayNo() != null) ? 
					colorwayRequest.getCriteria().getScColorwayNo() : "");
			
			if (errorList.isEmpty()) {
				response.getUpdated().add(colorwayNumber);
			} else {
				errorList.forEach(error -> {
					response.getErrors().add(
						SparcColorwayUpdateResponse.Error.builder().error(error.getMessage())
						.errorType(resolveApiUpdateErrorType(error))
						.id(colorwayNumber)
						.build());
				});
			}
			
		});
		
		return response;
	}
	
	/**
	 * Creates log entries based from the given list of article/colorway errors.
	 * @param responseMap The map containing article/colorways and associated errors.
	 * @return A list of log entries based on the article/colorway errors given, or an empty list if no errors were found.
	 */
	public static List<AeroApiCallLogEntry> createErrorLogEntries(Map<SparcColorwayUpdateRequest, List<AeroApiCallLogException>> responseMap) {
		
		Long entryTs = System.currentTimeMillis();
		List<AeroApiCallLogEntry> logEntryList = new ArrayList<>();
		
		if (responseMap == null || responseMap.isEmpty()) {
			return logEntryList;
		}
		
		responseMap.forEach((colorwayRequest, errorList) -> {
			
			String colorwayNumber = ((colorwayRequest.getCriteria() != null && colorwayRequest.getCriteria().getScColorwayNo() != null) ? 
					colorwayRequest.getCriteria().getScColorwayNo() : "");
			
			errorList.forEach(error -> {				
				logEntryList.add(AeroApiCallLogEntry.builder()
		                .apiCallType(AERO_ARTICLE_CAP_UPDATE_API_CALL)
		                .requestTime(entryTs)
		                .responseTime(entryTs)
		                .message(error.getMessage())
		                .request(SparcIntegrationUtil.deserialize(colorwayRequest))
		                .colorwayNumber(colorwayNumber)
		                .colorwaySeason(error.getSeasonType(), error.getSeasonYear())
		                .errorType(error.getErrorType())
		                .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
		                .status(AeroApiCallLogEntry.Status.WARNING)
		                .build());
			});
		});
		
		return logEntryList;
	}
	
	/**
	 * Removes the leading zeroes for the given colorway identifier. 
	 * @param colorwayId The colorway identifier.
	 * @return The colorway identifier as the number left after the removal of the leading zeroes.
	 */
	private String trimZeroPrefix(final String colorwayId) {
        if (colorwayId == null || !colorwayId.startsWith("0")) {
            return colorwayId;
        }
        final byte[] colorwayIdChars = colorwayId.getBytes();
        for (int iterator = 0; iterator < colorwayIdChars.length; iterator++) {
            if (colorwayIdChars[iterator] != '0') {
                return colorwayId.substring(iterator);
            }
        }
        return null;
    }
	
	/**
	 * Loads the Aero Sub Department<->Class Map based on the configuration found within sparc.integration.properties.
	 */
	private static void loadSubDeptClassConfigMap() {
		
		if (AERO_ARTICLE_CAP_UPDATE_SUBDEPT_VALUES.isEmpty()) {
			LOGGER.warn("[loadSubDeptClassMap] WARNING - No configured Aero sub department values for validation have been found.");
			return;
		}
		
		LOGGER.debug("[loadSubDeptClassMap] About to load Aero configuration class values for sub-departments.");
		
		for (String subDept : AERO_ARTICLE_CAP_UPDATE_SUBDEPT_VALUES) {
			
			if (subDept != null && !subDept.isEmpty()) {
				LOGGER.debug("Loading Aero configuration class values for sub-department: " + subDept);
				
				SUBDEPT_CLASS_MAP.put(subDept,
						Arrays.stream(LCSProperties.get(MessageFormat.format(AERO_ARTICLE_CAP_UPDATE_CLASS_VALUES_PATH, subDept), "")
								.split(","))
						        .map(classValue -> classValue.trim())
						        .collect(Collectors.toList()));
				
			}//end if
			
		}//end for loop.
		
		LOGGER.debug("[loadSubDeptClassMap] Aero configuration class values for " + SUBDEPT_CLASS_MAP.size() + " sub-departments have been loaded.");
		
	}
	
	/**
	 * Checks whether the given sub-department and class combination is valid.
	 * @param subDeptValue The sub-department value.
	 * @param classValue The Class value.
	 * @return true if the given sub-department/class combination is valid, false otherwise.
	 */
	private static boolean isSubDeptClassComboValid(String subDeptValue, String classValue) {
        List<String> classValuesList = SUBDEPT_CLASS_MAP.get(subDeptValue);
        return (classValuesList != null && classValuesList.contains(classValue));
    }
}
