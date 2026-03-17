package com.sparc.wc.integration.aero.builders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_ARTICLE_LOGGER_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_SEASON_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_HTS_CODE_PLACEHOLDER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_PRODUCT_SEASON_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_SEASON_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SEASON_NAME_ATTR;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.sparc.wc.integration.aero.domain.AeroArticleDetailsPayload;
import com.sparc.wc.integration.aero.domain.AeroArticleFlexTypeMaster;
import com.sparc.wc.integration.aero.domain.AeroPayloadAttribute;
import com.sparc.wc.integration.aero.domain.AeroPayloadAttributeDefinitions;
import com.sparc.wc.integration.aero.domain.AeroProcessesParam;
import com.sparc.wc.integration.aero.repository.AeroArticleRepository;
import com.sparc.wc.integration.aero.repository.AeroCostingRepository;
import com.sparc.wc.integration.aero.util.AeroPayloadAttributeUtil;

import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * Builds an Artile Details payload for Aero PLM-to-CAP integration.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Task #10212 (Hypercare): Updated build() method by adding custom message whenever integration flag updates fails, as well as updated related logging messages.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9645 Aero - Article Interface"
 */
public class AeroCAPArticleDetailsPayloadBuilder implements AeroArticleDetailsPayloadBuilder {
	
	private static final Logger LOGGER = LogR.getLogger(AERO_ARTICLE_LOGGER_NAME);
	
	private Long colorwayNumber;
	private AeroArticleDetailsPayload articlePayload;
	
	public AeroCAPArticleDetailsPayloadBuilder(AeroArticleDetailsPayload articlePayload) {
		this.articlePayload = articlePayload;
	}
	
	public AeroCAPArticleDetailsPayloadBuilder setColorwayNumber(Long colorwayNumber) {
		this.colorwayNumber = colorwayNumber;
		return this;
	}
	
	/**
	 * Builds the Product Season related attributes for the payload.
	 * @param articleDetailsMap The article details container where product season attributes will be stored into.
	 * @param flexProductSeasonLink The Product Season Link object to extract the attribute values from.
	 * @throws WTException If an error occurs while accessing/retrieving the Product Season attributes values from the flex object.
	 */
	private void buildProductSeasonAttributes(Map<String, Object> articleDetailsMap, LCSProductSeasonLink flexProductSeasonLink) throws WTException {
		
		if (articleDetailsMap == null || flexProductSeasonLink == null) {
			return;
		}
		
		for (AeroPayloadAttribute attrDef : AeroPayloadAttributeDefinitions.
				getCAPArticleAttributesDefinitionsForFlexType(AERO_PRODUCT_SEASON_ATTR_GROUP_KEY)) {
			LOGGER.debug("[buildProductSeasonAttributes] Product Season attrDef to load: " + attrDef);
			AeroPayloadAttributeUtil.loadAttributeValue(flexProductSeasonLink, attrDef, articleDetailsMap);
		}
		
	}
	
	/**
	 * Builds the Colorway (SKU) Season related attributes for the payload.
	 * @param articleDetailsMap The article details container where colorway season attributes will be stored into.
	 * @param flexColorwaySeasonLink The Colorway (SKU) Season Link object to extract the attribute values from.
	 * @throws WTException If an error occurs while accessing/retrieving the Colorway Season attributes values from the flex object.
	 */
	private void buildColorwaySeasonAttributes(Map<String, Object> articleDetailsMap, LCSSKUSeasonLink flexColorwaySeasonLink) throws WTException {
		
		if (articleDetailsMap == null || flexColorwaySeasonLink == null) {
			return;
		}
		
		for (AeroPayloadAttribute attrDef : AeroPayloadAttributeDefinitions.
				getCAPArticleAttributesDefinitionsForFlexType(AERO_COLORWAY_SEASON_ATTR_GROUP_KEY)) {
			LOGGER.debug("[buildColorwaySeasonAttributes] Colorway Season attrDef to load: " + attrDef);
			AeroPayloadAttributeUtil.loadAttributeValue(flexColorwaySeasonLink, attrDef, articleDetailsMap);
		}
		
	}
	
	/**
	 * Builds the Sourcing Season related attributes for the payload.
	 * @param articleDetailsMap The article details container where sourcing season attributes will be stored into.
	 * @param flexSourcingSeasonLink The Source To Season object to extract the attribute values from.
	 * @throws WTException If an error occurs while accessing/retrieving the Sourcing Season attributes values from the flex object.
	 */
	private void buildSourcingSeasonAttributes(Map<String, Object> articleDetailsMap, LCSSourceToSeasonLink flexSourcingSeasonLink) throws WTException {
		
		if (articleDetailsMap == null || flexSourcingSeasonLink == null) {
			return;
		}
		
		for (AeroPayloadAttribute attrDef : AeroPayloadAttributeDefinitions.
				getCAPArticleAttributesDefinitionsForFlexType(AERO_SOURCING_SEASON_ATTR_GROUP_KEY)) {
			LOGGER.debug("[buildSourcingSeasonAttributes] Sourcing Season attrDef to load: " + attrDef);
			if (AERO_HTS_CODE_PLACEHOLDER_ATTR.equalsIgnoreCase(attrDef.getFlexAttributeName())) {
				articleDetailsMap.put(attrDef.getJsonAttributeAlias(), AeroCostingRepository.resolveHTSCode1(flexSourcingSeasonLink));
			} else {
				AeroPayloadAttributeUtil.loadAttributeValue(flexSourcingSeasonLink, attrDef, articleDetailsMap);
			}
		}
		
	}
	
	/**
	 * Builds the Product related attributes for the payload.
	 * @param articleDetailsMap The article details container where product attributes will be stored into.
	 * @param flexProduct The Product to extract the attribute values from.
	 * @throws WTException If an error occurs while accessing/retrieving the Product attributes values from the flex object.
	 */
	private void buildProductAttributes(Map<String, Object> articleDetailsMap, LCSProduct flexProduct) throws WTException {
		
		if (articleDetailsMap == null || flexProduct == null) {
			return;
		}
		
		for (AeroPayloadAttribute attrDef : AeroPayloadAttributeDefinitions.getCAPArticleAttributesDefinitionsForFlexType(AERO_PRODUCT_ATTR_GROUP_KEY)) {
			LOGGER.debug("[buildProductAttributes] Product attrDef to load: " + attrDef);
			AeroPayloadAttributeUtil.loadAttributeValue(flexProduct, attrDef, articleDetailsMap);
		}
		
	}
	
	/**
	 * Builds the Colorway related attributes for the payload.
	 * @param articleDetailsMap The article details container where colorway attributes will be stored into.
	 * @param flexColorway The Colorway to extract the attribute values from.
	 * @throws WTException If an error occurs while accessing/retrieving the Colorway attributes values from the flex object.
	 */
	private void buildColorwayAttributes(Map<String, Object> articleDetailsMap, LCSSKU flexColorway) throws WTException {
		
		if (articleDetailsMap == null || flexColorway == null) {
			return;
		}
		
		for (AeroPayloadAttribute attrDef : AeroPayloadAttributeDefinitions.getCAPArticleAttributesDefinitionsForFlexType(AERO_COLORWAY_ATTR_GROUP_KEY)) {
			System.out.println("[buildColorwayAttributes] Colorway attrDef to load: " + attrDef);
			AeroPayloadAttributeUtil.loadAttributeValue(flexColorway, attrDef, articleDetailsMap);
		}
		
	}
	
	/**
	 * Builds the Season related attributes for the payload.
	 * @param articleDetailsMap The article details container where season attributes will be stored into.
	 * @param flexSeason The Season to extract the attribute values from.
	 * @throws WTException If an error occurs while accessing/retrieving the Season attributes values from the flex object.
	 */
	private void buildSeasonAttributes(Map<String, Object> articleDetailsMap, LCSSeason flexSeason) throws WTException {
		
		if (articleDetailsMap == null || flexSeason == null) {
			return;
		}
		
		for (AeroPayloadAttribute attrDef : AeroPayloadAttributeDefinitions.getCAPArticleAttributesDefinitionsForFlexType(AERO_SEASON_ATTR_GROUP_KEY)) {
			LOGGER.debug("[buildSeasonAttributes] Season attrDef to load: " + attrDef);
			AeroPayloadAttributeUtil.loadAttributeValue(flexSeason, attrDef, articleDetailsMap);
		}
		
	}
	
	/**
	 * Builds the Article Details payload for CAP.
	 * @return The Article Details payload built.
	 * @throws WTException When an error occurs while accessing or retrieving the various data elements from PLM.
	 * @throws WTPropertyVetoException When an error occurs while updating the control flags at PLM.
	 * @throws Exception When a different error, i.e. validation fail, occurs.
	 */
	public AeroArticleDetailsPayload build() throws WTException, WTPropertyVetoException, Exception {
		
		if (articlePayload == null || colorwayNumber == null) {
			return articlePayload;
		}
		
		List<AeroArticleFlexTypeMaster> flexArticleMasterList = AeroArticleRepository.getArticleDetails(colorwayNumber, 
				AeroProcessesParam.CAP);
		
		for (AeroArticleFlexTypeMaster articleMaster : flexArticleMasterList) {
			
			if (articleMaster == null) {
				continue;
			}
			
			String seasonName = "" +  (articleMaster.getSeason() != null ? articleMaster.getSeason().getValue(AERO_SEASON_NAME_ATTR) : "");
			Map<String, Object> articleDetailsMap = new HashMap<String, Object>();
			
			articlePayload.getData().add(articleDetailsMap);
			
			LOGGER.debug("About to build Aero CAP Article payload for colorway " + colorwayNumber + " at season " + seasonName + ".");
			
			buildColorwayAttributes(articleDetailsMap, articleMaster.getColorway());
			buildSeasonAttributes(articleDetailsMap, articleMaster.getSeason());
			buildProductAttributes(articleDetailsMap, articleMaster.getProduct());
			buildSourcingSeasonAttributes(articleDetailsMap, 
					AeroArticleRepository.getPrimarySourceToSeasonLink(articleMaster.getProduct(), articleMaster.getSeason()));
			buildColorwaySeasonAttributes(articleDetailsMap, articleMaster.getColorwaySeasonLink());
			buildProductSeasonAttributes(articleDetailsMap, articleMaster.getProductSeasonLink());
			
			AeroPayloadAttributeUtil.logPayload(articleDetailsMap, LOGGER);
			
			LOGGER.debug("About to update Aero CAP Article integration flags for colorway " + colorwayNumber + " at season " + seasonName + ".");
			try {
				AeroArticleRepository.updateIntegrationControlFlags(AeroProcessesParam.CAP, articleMaster.getColorwaySeasonLink());
			} catch(Exception cslinkEx) {
				throw new Exception("Failed to update colorway-season CAP integration control flag for colorway #" + colorwayNumber + " and season " + seasonName + ".", cslinkEx);	
			}
			
			LOGGER.debug("Aero CAP Article integration flags for colorway " + colorwayNumber + " at season " + seasonName + " have been updated.");
			
		}//end for loop.
		
		return articlePayload;
	}
	
}
