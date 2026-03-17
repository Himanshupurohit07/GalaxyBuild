package com.sparc.wc.integration.lucky.util;

import java.util.ArrayList;
import java.util.List;


import com.lcs.wc.sourcing.LCSCostSheet;
import com.sparc.wc.integration.lucky.domain.LuckyCustomPluginException;
import com.sparc.wc.integration.lucky.domain.LuckyPayloadAttribute;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;


import com.sparc.wc.integration.util.SparcIntegrationUtil;

import wt.log4j.LogR;
import wt.method.MethodContext;
import wt.util.WTException;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.*;


public class LuckyPluginUtil {

    private LuckyPluginUtil() {

    }

    private static final Logger LOGGER = LogR.getLogger(AERO_COMMON_LOGGER_NAME);

    /**
     * Checks whether current flex object is changed when compared to its previous version.<br>
     * Note: Uses default Aero common logger for printing debug messages.
     * @param current The current flex object to check.
     * @param previous The previous flex object to compare against the current one.
     * @param attributes List of flex object attributes to use for the comparison.
     * @return true if the object has changed, false otherwise or if no previous object was provided to compare with.
     * @throws WTException If an error occurs while verifying attribute values from the current and/or previous flex objects.
     */
    public static boolean isFlexObjectChanged(FlexTyped current, FlexTyped previous, List<LuckyPayloadAttribute> attributes) throws WTException {
        return isFlexObjectChanged(current, previous, attributes, LOGGER);
    }

    /**
     * Checks whether current flex object is changed when compared to its previous version.
     * @param current The current flex object to check.
     * @param previous The previous flex object to compare against the current one.
     * @param attributes List of flex object attributes to use for the comparison.
     * @param logger Use the logger provided
     * @return true if the object has changed, false otherwise or if no previous object was provided to compare with.
     * @throws WTException If an error occurs while verifying attribute values from the current and/or previous flex objects.
     */
    public static boolean isFlexObjectChanged(FlexTyped current, FlexTyped previous, List<LuckyPayloadAttribute> attributes, Logger logger) throws WTException {

        boolean isChanged = false;

        if (current == null || previous == null || attributes == null || attributes.isEmpty()) {
            if (logger != null) {
                LOGGER.debug("[isFlexObjectChanged] Skipping flex object attr change check due to missing arguments.");
            }
            return isChanged;
        }

        if (logger == null) {
            logger = LOGGER;
        }

        for (LuckyPayloadAttribute attr : attributes) {
            if (!FormatHelper.hasContent(attr.getFlexAttributeName())) {
                LOGGER.warn("[isFlexObjectChanged] No flex attribute name found for attribute definition: " + attr);
                continue;
            }
            Object currAttrValue = LuckyPayloadAttributeUtil.getAttributeValue(current, attr);
            Object attrPrevValue = LuckyPayloadAttributeUtil.getAttributeValue(previous, attr);
            LOGGER.debug("[isFlexObjectChanged] " + attr.getFlexAttributeName() + ": currAttrValue = " + currAttrValue + " <-> attrPrevValue = " + attrPrevValue);
            isChanged = SparcIntegrationUtil.divergenceCheck(attrPrevValue, currAttrValue);
            if (isChanged) {
                LOGGER.debug("[isFlexObjectChanged] Attribute '" + attr.getFlexAttributeName() + "' is changed.");
                break;
            }
        }//end for loop.

        return isChanged;
    }

    /**
     * Returns a list of attributes which their values got changed when comparing current vs previous flex object states.
     * @param current The current flex object to check.
     * @param previous The previous flex object to compare against the current one.
     * @param attributes List of flex object attributes to use for the comparison.
     * @param logger Use the logger provided
     * @return The list of attributes which values got changed.
     * @throws WTException If an error occurs while verifying attribute values from the current and/or previous flex objects.
     */
    public static List<LuckyPayloadAttribute> getChangedAttributes(FlexTyped current, FlexTyped previous, List<LuckyPayloadAttribute> attributes, Logger logger) throws WTException {

        List<LuckyPayloadAttribute> changedAttrs = new ArrayList<>();

        if (current == null || previous == null || attributes == null || attributes.isEmpty()) {
            if (logger != null) {
                LOGGER.debug("[getChangedAttributes] Skipping flex object attr change check due to missing arguments.");
            }
            return changedAttrs;
        }

        if (logger == null) {
            logger = LOGGER;
        }

        for (LuckyPayloadAttribute attr : attributes) {

            if (!FormatHelper.hasContent(attr.getFlexAttributeName())) {
                LOGGER.warn("[getChangedAttributes] No flex attribute name found for attribute definition: " + attr);
                continue;
            }

            Object currAttrValue = LuckyPayloadAttributeUtil.getAttributeValue(current, attr);
            Object attrPrevValue = LuckyPayloadAttributeUtil.getAttributeValue(previous, attr);

            LOGGER.debug("[getChangedAttributes] RAW: " + attr.getFlexAttributeName() + ": currAttrValue = " + currAttrValue + " <-> attrPrevValue = " + attrPrevValue);

            if (currAttrValue != null && currAttrValue.toString().isEmpty()) {
                currAttrValue = null;
            }

            if (attrPrevValue != null && attrPrevValue.toString().isEmpty()) {
                attrPrevValue = null;
            }

            LOGGER.debug("[getChangedAttributes] CURATED: " + attr.getFlexAttributeName() + ": currAttrValue = " + currAttrValue + " <-> attrPrevValue = " + attrPrevValue);

            if (SparcIntegrationUtil.divergenceCheck(attrPrevValue, currAttrValue)) {
                LOGGER.debug("[getChangedAttributes] Attribute '" + attr.getFlexAttributeName() + "' is changed.");
                changedAttrs.add(attr);
            }

        }//end for loop.

        return changedAttrs;
    }

    public static void validateLuckyCostsheetType(LCSCostSheet objCostSheet) throws WTException,LuckyCustomPluginException{
		System.out.println("validateLuckyCostsheetType------contains Lukcy------------------"+objCostSheet.getFlexType().getFullName(true));
        if(!(objCostSheet.getFlexType().getFullName(true).contains("Lucky"))){
           throw new LuckyCustomPluginException("Not a Valid Cost sheet type");
        }
    }

    /**
     * Checks whether the given season is a valid Lucky season type or not.
     * @param season The season to check.
     * @throws WTException If an error occurs when extracting the season type from the season object.
     * @throws LuckyCustomPluginException If the season is not a valid Lucky type.
     */
    public static void validateLuckySeasonType(LCSSeason season) throws WTException, LuckyCustomPluginException {

        if (season == null) {
            throw new LuckyCustomPluginException("Cannot validate Aero season type on a missing season.");
        }

        String developmentSeason = (String)season.getValue(AERO_SEASON_TYPE_ATTR);

        if (!LUCKY_DEVELOPMENT_SEASONS.contains(developmentSeason)) {
            throw new LuckyCustomPluginException("Not a valid Aero Season Type for integration: " + developmentSeason);
        }
    }

    /**
     * Checks whether the given product is an Lucky product type or not.
     * @param product The product to check.
     * @throws LuckyCustomPluginException If the product is not an Lucky type.
     */
    public static void validateLuckyProductType(LCSProduct product) throws LuckyCustomPluginException {

        if (product == null) {
            throw new LuckyCustomPluginException("Cannot validate Lucky product type on a missing product.");
        }

        String productType = product.getFlexType().getFullName(true);

        if(!productType.contains(LUCKY_PRODUCT_FLEX_PATH)) {
            throw new LuckyCustomPluginException("Not an Aero Product Type: " + productType);
        }
    }

    /**
     * Checks whether the given colorway is an Aero colorway type or not.
     * @param colorway The colorway to check.
     * @throws LuckyCustomPluginException If the colorway is not an Aero type.
     */
    public static void validateLuckyColorwayType(LCSSKU colorway) throws LuckyCustomPluginException {

        if (colorway == null) {
            throw new LuckyCustomPluginException("Cannot validate Lucky colorway type on a missing colorway.");
        }

        String colorwayType = colorway.getFlexType().getFullName(true);

        if (!colorwayType.contains(LUCKY_FLEX_TYPE_PATH_KEY)) {
            throw new LuckyCustomPluginException("Not an Aero Colorway Type: " + colorwayType);
        }
    }

    /**
     * Checks whether the given colorway name is available within the given cost sheet.
     * @param colorwayName The colorway name to check.
     * @param costSheet The cost sheet to check for the colorway name's presence.
     * @return true if the colorway name is found within the given cost sheet, false otherwise.
     */
    public static boolean isColorwayInCostSheet(String colorwayName, LCSProductCostSheet costSheet) {

        boolean found = false;

        if (colorwayName == null || costSheet == null) {
            return found;
        }

        String costSheetColorways = costSheet.getApplicableColorNames();

        if (costSheetColorways == null || costSheetColorways.isEmpty()) {
            return found;
        }

        String[] colorwaysInCostSheetArray = costSheetColorways.split("\\|~\\*~\\|");

        for (String colorwayCS : colorwaysInCostSheetArray) {

            found = colorwayName.equalsIgnoreCase(colorwayCS);

            if (found) {
                break;
            }

        }//end for loop.

        return found;
    }

    /**
     * Checks whether the given cost sheet belongs to the given season name.
     * @param costSheet The cost sheet to check.
     * @param seasonName The season name to check against the cost sheet.
     * @return true if the cost sheet is belongs to the given season, false otherwise.
     * @throws WTException If an error occurs while attempting to access the season details for the given cost sheet.
     */
    public static boolean isCostSheetInSeason(LCSProductCostSheet costSheet, String seasonName) throws WTException {

        if (costSheet == null || seasonName == null) {
            return false;
        }
        LCSSeason csSeason = (LCSSeason) VersionHelper.latestIterationOf(costSheet.getSeasonMaster());
        return (csSeason != null && seasonName.equalsIgnoreCase((String)csSeason.getValue("seasonName")));
    }
	 public static boolean isInterfaceAdmin() {
        try {
            return MethodContext.getContext().getUserName().equals(INTERFACE_ADMIN_USER_NAME);
        } catch (Exception e) {
            LOGGER.error("Encountered an error while fetching current user, error:" + e.getMessage());
        }
        return false;
    }


}
