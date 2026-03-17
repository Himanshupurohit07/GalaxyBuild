package com.sparc.wc.integration.aero.domain;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_S4_ARTICLE_PAYLOAD_ATTRIBUTES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_CAP_ARTICLE_PAYLOAD_ATTRIBUTES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_ARTICLE_PLUGIN_ATTRIBUTES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_CAP_ARTICLE_UPDATE_ATTRIBUTES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_S4_COSTING_PAYLOAD_ATTRIBUTES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_CAP_COSTING_PAYLOAD_ATTRIBUTES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COSTING_PLUGIN_ATTRIBUTES;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a collection of payload attributes used throughout the various Aeropostale integrations.<br>
 * The payload attribute definitions are loaded from the integration properties, then parsed and stored into 
 * appropriate map caches relevant to a specificic Aero integration, so they can be accessed when generating the outbound payloads.
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 */
public class AeroPayloadAttributeDefinitions {
	
	/**
	 * Cache for Article attributes for S4 integration.
	 */
	private static final Map<String, List<AeroPayloadAttribute>> S4_ARTICLE_PAYLOAD_ATTR_MAP = new ConcurrentHashMap<>(10);
	/**
	 * Cache for Article attributes for CAP integration.
	 */
	private static final Map<String, List<AeroPayloadAttribute>> CAP_ARTICLE_PAYLOAD_ATTR_MAP = new ConcurrentHashMap<>(10);
	/**
	 * Cache for Article attributes for Plugin validation.
	 */
	private static final Map<String, List<AeroPayloadAttribute>> ARTICLE_PLUGIN_VALIDATION_ATTR_MAP = new ConcurrentHashMap<>(10);
	/**
	 * Cache for Article attributes for CAP (Update) integration.
	 */
	private static final Map<String, List<AeroPayloadAttribute>> CAP_ARTICLE_UPDATE_ATTR_MAP = new ConcurrentHashMap<>(10);
	/**
	 * Cache for Costing (PIR) attributes for S4 integration.
	 */
	private static final Map<String, List<AeroPayloadAttribute>> S4_COSTING_PAYLOAD_ATTR_MAP = new ConcurrentHashMap<>(10);
	/**
	 * Cache for Costing attributes for CAP integration.
	 */
	private static final Map<String, List<AeroPayloadAttribute>> CAP_COSTING_PAYLOAD_ATTR_MAP = new ConcurrentHashMap<>(10);
	/**
	 * Cache for Costing attributes for Plugin validation.
	 */
	private static final Map<String, List<AeroPayloadAttribute>> COSTING_PLUGIN_VALIDATION_ATTR_MAP = new ConcurrentHashMap<>(10);
	
	static {
		loadAttributeDefinitions(AERO_S4_ARTICLE_PAYLOAD_ATTRIBUTES, S4_ARTICLE_PAYLOAD_ATTR_MAP);
		loadAttributeDefinitions(AERO_CAP_ARTICLE_PAYLOAD_ATTRIBUTES, CAP_ARTICLE_PAYLOAD_ATTR_MAP);
		loadAttributeDefinitions(AERO_ARTICLE_PLUGIN_ATTRIBUTES, ARTICLE_PLUGIN_VALIDATION_ATTR_MAP);
		loadAttributeDefinitions(AERO_CAP_ARTICLE_UPDATE_ATTRIBUTES, CAP_ARTICLE_UPDATE_ATTR_MAP);
		loadAttributeDefinitions(AERO_S4_COSTING_PAYLOAD_ATTRIBUTES, S4_COSTING_PAYLOAD_ATTR_MAP);
		loadAttributeDefinitions(AERO_CAP_COSTING_PAYLOAD_ATTRIBUTES, CAP_COSTING_PAYLOAD_ATTR_MAP);
		loadAttributeDefinitions(AERO_COSTING_PLUGIN_ATTRIBUTES, COSTING_PLUGIN_VALIDATION_ATTR_MAP);
	}
	
	/**
	 * Parses and loads the attributes definitions from the given raw definitions map into the given target one used as cache.
	 * @param aeroAttrSourceDefsMap The source Map containing raw attribute mapping definitions.
	 * @param aeroAttrTargetMap The target Map to store the parsed attribute mapping definitions.
	 */
	private static void loadAttributeDefinitions(Map<String, Set<String>> aeroAttrSourceDefsMap, 
			Map<String, List<AeroPayloadAttribute>> aeroAttrTargetMap) {
		
		if (aeroAttrSourceDefsMap == null || aeroAttrSourceDefsMap.isEmpty() || aeroAttrTargetMap == null) {
			return;
		}
		
		aeroAttrSourceDefsMap.forEach((key, attrDefinitions) -> {
			
			List<AeroPayloadAttribute> payloadAttrs = new CopyOnWriteArrayList<>();
			List<AeroPayloadAttribute> noBlanksAttrs = new CopyOnWriteArrayList<>();
			List<AeroPayloadAttribute> lockedAttrs = new CopyOnWriteArrayList<>();
			
			attrDefinitions.forEach((attrDef) -> {
				if (attrDef != null && !attrDef.isBlank()) {
					AeroPayloadAttribute attr = AeroPayloadAttribute.newBuilder().build(attrDef);
					payloadAttrs.add(attr);
					
					if (attr.isNoBlanks()) {
						noBlanksAttrs.add(attr);
					}
					
					if (attr.isLockAttrUpdate()) {
						lockedAttrs.add(attr);
					}
				}
			});
			
			aeroAttrTargetMap.put(key, payloadAttrs);
			
		});
		
	}
	
	/**
	 * Returns the list of S4 Article payload attributes definitions for the given flex type group key.<br>
	 * The flex type group key refers to one of the flex types, for example "PRODUCT" or "COLORWAY".<br>
	 * Please refer to the various flex type group key constants available at the SparcIntegrationConstants.
	 * These can be recognize as they have the following naming format: AERO_<flex type ref>_ATTR_GROUP_KEY. 
	 * @param flexTypeGroupKey The flex type group key that identifies the list of attribute definitions.
	 * @return The list of payload attributes or <code>null</code> if no attributes were found for the given flex type group key.
	 */
	public static List<AeroPayloadAttribute> getS4ArticleAttributesDefinitionsForFlexType(String flexTypeGroupKey) {
		return S4_ARTICLE_PAYLOAD_ATTR_MAP.get(flexTypeGroupKey);
	}
	
	/**
	 * Returns the list of CAP Article payload attributes definitions for the given flex type group key.<br>
	 * The flex type group key refers to one of the flex types, for example "PRODUCT" or "COLORWAY".<br>
	 * Please refer to the various flex type group key constants available at the SparcIntegrationConstants.
	 * These can be recognize as they have the following naming format: AERO_<flex type ref>_ATTR_GROUP_KEY. 
	 * @param flexTypeGroupKey The flex type group key that identifies the list of attribute definitions.
	 * @return The list of payload attributes or <code>null</code> if no attributes were found for the given flex type group key.
	 */
	public static List<AeroPayloadAttribute> getCAPArticleAttributesDefinitionsForFlexType(String flexTypeGroupKey) {
		return CAP_ARTICLE_PAYLOAD_ATTR_MAP.get(flexTypeGroupKey);
	}
	
	/**
	 * Returns the list of Article Plugin attributes definitions for the given flex type group key.<br>
	 * The flex type group key refers to one of the flex types, for example "PRODUCT" or "COLORWAY".<br>
	 * Please refer to the various flex type group key constants available at the SparcIntegrationConstants.
	 * These can be recognize as they have the following naming format: AERO_<flex type ref>_ATTR_GROUP_KEY. 
	 * @param flexTypeGroupKey The flex type group key that identifies the list of attribute definitions.
	 * @return The list of payload attributes or <code>null</code> if no attributes were found for the given flex type group key.
	 */
	public static List<AeroPayloadAttribute> getArticlePluginAttributesDefinitionsForFlexType(String flexTypeGroupKey) {
		return ARTICLE_PLUGIN_VALIDATION_ATTR_MAP.get(flexTypeGroupKey);
	}
	
	/**
	 * Returns the list of CAP Article payload attributes definitions for the given flex type group key.<br>
	 * The flex type group key refers to one of the flex types, for example "PRODUCT" or "COLORWAY".<br>
	 * Please refer to the various flex type group key constants available at the SparcIntegrationConstants.
	 * These can be recognize as they have the following naming format: AERO_<flex type ref>_ATTR_GROUP_KEY. 
	 * @param flexTypeGroupKey The flex type group key that identifies the list of attribute definitions.
	 * @return The list of payload attributes or <code>null</code> if no attributes were found for the given flex type group key.
	 */
	public static List<AeroPayloadAttribute> getCAPArticleUpdateAttributesDefinitionsForFlexType(String flexTypeGroupKey) {
		return CAP_ARTICLE_UPDATE_ATTR_MAP.get(flexTypeGroupKey);
	}
	
	/**
	 * Returns the list of S4 Costing (PIR) payload attributes definitions for the given flex type group key.<br>
	 * The flex type group key refers to one of the flex types, for example "PRODUCT" or "COLORWAY".<br>
	 * Please refer to the various flex type group key constants available at the SparcIntegrationConstants.
	 * These can be recognize as they have the following naming format: AERO_<flex type ref>_ATTR_GROUP_KEY. 
	 * @param flexTypeGroupKey The flex type group key that identifies the list of attribute definitions.
	 * @return The list of payload attributes or <code>null</code> if no attributes were found for the given flex type group key.
	 */
	public static List<AeroPayloadAttribute> getS4CostingAttributesDefinitionsForFlexType(String flexTypeGroupKey) {
		return S4_COSTING_PAYLOAD_ATTR_MAP.get(flexTypeGroupKey);
	}
	
	/**
	 * Returns the list of CAP Costing payload attributes definitions for the given flex type group key.<br>
	 * The flex type group key refers to one of the flex types, for example "PRODUCT" or "COLORWAY".<br>
	 * Please refer to the various flex type group key constants available at the SparcIntegrationConstants.
	 * These can be recognize as they have the following naming format: AERO_<flex type ref>_ATTR_GROUP_KEY. 
	 * @param flexTypeGroupKey The flex type group key that identifies the list of attribute definitions.
	 * @return The list of payload attributes or <code>null</code> if no attributes were found for the given flex type group key.
	 */
	public static List<AeroPayloadAttribute> getCAPCostingAttributesDefinitionsForFlexType(String flexTypeGroupKey) {
		return CAP_COSTING_PAYLOAD_ATTR_MAP.get(flexTypeGroupKey);
	}
	
	/**
	 * Returns the list of Costing Plugin attributes definitions for the given flex type group key.<br>
	 * The flex type group key refers to one of the flex types, for example "PRODUCT" or "COLORWAY".<br>
	 * Please refer to the various flex type group key constants available at the SparcIntegrationConstants.
	 * These can be recognize as they have the following naming format: AERO_<flex type ref>_ATTR_GROUP_KEY. 
	 * @param flexTypeGroupKey The flex type group key that identifies the list of attribute definitions.
	 * @return The list of payload attributes or <code>null</code> if no attributes were found for the given flex type group key.
	 */
	public static List<AeroPayloadAttribute> getCostingPluginAttributesDefinitionsForFlexType(String flexTypeGroupKey) {
		return COSTING_PLUGIN_VALIDATION_ATTR_MAP.get(flexTypeGroupKey);
	}
	
	private AeroPayloadAttributeDefinitions() {
		
	}
	
}
