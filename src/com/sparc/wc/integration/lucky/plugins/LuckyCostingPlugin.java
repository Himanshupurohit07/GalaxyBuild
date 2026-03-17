package com.sparc.wc.integration.lucky.plugins;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;

import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSCostSheetQuery;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.sourcing.LCSCostSheetLogic;

import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.integration.lucky.domain.LuckyCustomPluginException;
import com.sparc.wc.integration.lucky.domain.LuckyPayloadAttribute;
import com.sparc.wc.integration.lucky.domain.LuckyPayloadAttributeDefinitions;
import com.sparc.wc.integration.lucky.repository.LuckyCostingRepository;
import com.sparc.wc.integration.lucky.util.LuckyPayloadAttributeUtil;
import com.sparc.wc.integration.lucky.util.LuckyPluginUtil;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import org.apache.logging.log4j.Logger;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.Mastered;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import wt.method.MethodContext;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_LOGGER_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_SOURCING_CONFIG_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_SOURCING_SEASON_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COST_SHEET_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SOURCING_CONFIG_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_COST_SHEET_TYPE_PRODUCT;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_NUMBER_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_STATUS_CONFIRMED_VALUES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COST_SHEET_STATUS_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COST_SHEET_INCO_ATTR_GROUP_KEY;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.INTERFACE_ADMIN_USER_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.PRODUCT_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COST_SENT_TO_FC_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COST_SENT_TO_CAP_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_SENT_NO_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.COST_BRANCH_ID;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.COST_VR_STR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.APP_COLOR_NAMES;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.COST_TYPE_STR;

public class LuckyCostingPlugin {

    private static final Logger LOGGER = LogR.getLogger(LUCKY_COSTING_LOGGER_NAME);

    private static List<LuckyPayloadAttribute> SOURCING_ATTR_LIST = LuckyPayloadAttributeDefinitions.getCostingAttributesDefinitionsForFlexType(LUCKY_SOURCING_CONFIG_ATTR_GROUP_KEY);

    private static List<LuckyPayloadAttribute> SOURCING_TO_SEASON_ATTR_LIST = LuckyPayloadAttributeDefinitions.getCostingAttributesDefinitionsForFlexType(LUCKY_SOURCING_SEASON_ATTR_GROUP_KEY);

    private static List<LuckyPayloadAttribute> COST_SHEET_ATTR_LIST = LuckyPayloadAttributeDefinitions.getCostingAttributesDefinitionsForFlexType(LUCKY_COST_SHEET_ATTR_GROUP_KEY);

    private static List<LuckyPayloadAttribute> SOURCING_INCO_ATTR_LIST = LuckyPayloadAttributeDefinitions.getCostingAttributesDefinitionsForFlexType(LUCKY_COST_SHEET_INCO_ATTR_GROUP_KEY);


    /**
     * @param obj
     * @throws LCSException
     */
    public static void updateIntegrationFlags(WTObject obj) throws LCSException {


        LOGGER.info("[updateIntegrationFlags] ==============STARTED Lucky Costing Plugin @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " =========");
        LOGGER.debug("[updateIntegrationFlags] WTObject type: " + obj.getDisplayIdentity());
        if (isInterfaceAdmin()) {
            LOGGER.info("Detected interface-admin, so skipping the plugins");
            return;
        }
        try {
            if (obj instanceof LCSSourcingConfig) {
                validateSourcingConfig((LCSSourcingConfig) obj);
            }
            if (obj instanceof LCSSourceToSeasonLink) {
                validateSourceToSeasonLink((LCSSourceToSeasonLink) obj);
            } else if (obj instanceof LCSProductCostSheet) {
                validateProductCostSheet((LCSProductCostSheet) obj);
            } else if (obj instanceof LCSProduct) {
                validateProduct((LCSProduct) obj);
            } else {
                LOGGER.debug("[validateChanges] Object type " + obj.getDisplayIdentity() + " ignored.");
            }
        } catch (LuckyCustomPluginException luckyEx) {
            luckyEx.printStackTrace();
            LOGGER.warn("[updateIntegrationFlags] Skip/Exit condition found for Lucky Costing Plugin validation: " + luckyEx.getMessage());
        } catch (LCSException lcsEx) {
            lcsEx.printStackTrace();
            LOGGER.warn("[updateIntegrationFlags] Lucky Costing Plugin validation alert: " + lcsEx.getMessage());
            throw lcsEx;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("[updateIntegrationFlags] The Lucky Costing Plugin failed to process the flex object.", e);
        }
        LOGGER.info("[updateIntegrationFlags] ==============DONE Lucky Costing Plugin @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " =========");
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
     *
     * @param prod
     * @throws WTPropertyVetoException
     * @throws WTException
     * @throws LuckyCustomPluginException
     */
    private static void validateProduct(LCSProduct prod) throws WTPropertyVetoException, WTException, LuckyCustomPluginException {
        LOGGER.debug("[Lucky FC and CAP Costing Plugin]---validate product--------- " + prod.getName());
        LCSProduct prevProd = (LCSProduct)VersionHelper.predecessorOf(prod);
        if (prevProd == null) {
            LOGGER.debug("No previous product version found for '" + prod.getName() + "', skipping plugin...");
            return;
        }

        List<LCSSeason> prdSeasonsList = LuckyCostingRepository.getValidSeasonsForProduct(prod);
        System.out.println("prdSeasonsList----from Product Context----------------------"+prdSeasonsList);
        if (prdSeasonsList == null || prdSeasonsList.isEmpty()) {
            LOGGER.debug("seasonless product Product '" + prod.getName() + "so skipping it");
            return;
        }

        @SuppressWarnings("unchecked")
        Collection<LCSSourcingConfig> sconfigs = LCSSourcingConfigQuery.getSourcingConfigsForProduct(prod);

        if (sconfigs == null || sconfigs.isEmpty()) {
            LOGGER.debug("Product '" + prod.getName() + "' doesn't have any sourcing configurations, skipping plugin...");
            return;
        }

        //Here LuckyPayloadAttribute is not used as there is only attribute at Product level
        if (!isChanged(prod, prevProd, PRODUCT_ATTR)) {
            LOGGER.debug("No changes found for product '" + prod.getName() + "', skipping plugin...");
            return;
        }


        Collection emptyColl = new ArrayList<>();

        for (LCSSeason objSeason : prdSeasonsList) {
            Collection<LCSSourcingConfig> objSources = LCSSourcingConfigQuery.getSourcingConfigForProductSeason(prod.getMaster(), objSeason.getMaster());
            if (objSources.isEmpty()) {
                LOGGER.debug("Product Context ----- No Sources Available");
                continue;
            }
            for (LCSSourcingConfig objSourceConfig : objSources) {
                Collection<FlexObject> objCostSheets = LCSCostSheetQuery.getCostSheetsForProduct(new HashMap<>(), prevProd, objSourceConfig, objSeason, emptyColl, false, false);
                if (objCostSheets.isEmpty()) {
                    LOGGER.debug("Product Context ----- No CostSheets Available");
                    continue;
                }
                for (FlexObject obj : objCostSheets) {
                    if (FormatHelper.hasContent(obj.getString(APP_COLOR_NAMES)) && FLEX_COST_SHEET_TYPE_PRODUCT.equalsIgnoreCase(obj.getString(COST_TYPE_STR))) {
                        LCSProductCostSheet objProdCostSheet = (LCSProductCostSheet) LCSQuery.findObjectById(COST_VR_STR + obj.getString(COST_BRANCH_ID));
                        if(!objProdCostSheet.isLatestIteration()){
                            objProdCostSheet = (LCSProductCostSheet)VersionHelper.latestIterationOf(objProdCostSheet);
                        }
                        objProdCostSheet.setValue(LUCKY_COST_SENT_TO_FC_ATTR, LUCKY_COSTING_SENT_NO_VALUE);
                        objProdCostSheet.setValue(LUCKY_COST_SENT_TO_CAP_ATTR, LUCKY_COSTING_SENT_NO_VALUE);
                        new LCSCostSheetLogic().saveCostSheet(objProdCostSheet, false, true);
                    }
                }
            }
        }
    }



    /**
     * Lookout for changes on the selected integration-eligible attribute values for sourcing config and take the following action(s):<br>
     * - Confirm if this sourcing config is associated with a Lucky product.<br>
     * - Verifies Non-blanks for Sourcing Config if any of its associated cost sheets' status is confirmed.<br>
     * - Reset all associated cost sheet integration flags if applicable.<br>
     * @param sourcingConfig The sourcing configuration instance.
     * @throws WTPropertyVetoException If an error occurs while resetting the integration flags.
     * @throws WTException If an error occurs while accessing sourcing configuration attributes.
     * @throws LuckyCustomPluginException If a validation exit condition is found. i.e. If product Type is not Aero.
     */
    private static void validateSourcingConfig(LCSSourcingConfig sourcingConfig) throws WTPropertyVetoException, WTException, LuckyCustomPluginException {

        if (sourcingConfig == null) {
            return;
        }

        Long sourcingConfigNumber = (Long)sourcingConfig.getValue(AERO_SOURCING_CONFIG_NUMBER_ATTR);
        LOGGER.debug("[validateSourcingConfig] Sourcing Config # to check is: " + sourcingConfigNumber + " (" + sourcingConfig.getSourcingConfigName() + ").");

        LCSProduct product = (LCSProduct) VersionHelper.latestIterationOf(sourcingConfig.getProductMaster());
        //Check if sourcing config is bound to an Aero product.
        LuckyPluginUtil.validateLuckyProductType(product);

        LCSSourcingConfig prevSourcingConfig = (LCSSourcingConfig) VersionHelper.predecessorOf(sourcingConfig);

        if (prevSourcingConfig == null) {
            throw new LuckyCustomPluginException("No previous sourcing config was found for sourcing config #" + sourcingConfigNumber + ".");
        }


        Collection<LCSSourceToSeasonLink> stsl = new LCSSourcingConfigQuery().getSourceToSeasonLinks(sourcingConfig);
        if(stsl == null) {
            //System.out.println("No Source to season links------1111111-------");
            return;
        }

        stsl = new LCSSourcingConfigQuery().getSourceToSeasonLinks(prevSourcingConfig);
        if(stsl == null) {
           // System.out.println("No Source to season links----22222222---------");
            return;
        }

        List<LuckyPayloadAttribute> changedAttrList = LuckyPluginUtil.getChangedAttributes(sourcingConfig, prevSourcingConfig, SOURCING_ATTR_LIST, LOGGER);

        if (changedAttrList.isEmpty()) {
            throw new LuckyCustomPluginException("No changes found for sourcing config #" + sourcingConfigNumber + ".");
        }

        Collection<?> rawCostSheetList = LCSCostSheetQuery.getAllCostSheetsForSourcingConfig(prevSourcingConfig, false);

        if (rawCostSheetList == null || rawCostSheetList.isEmpty()) {
            throw new LuckyCustomPluginException("No cost sheets found for sourcing config #" + sourcingConfigNumber + ".");
        }

        List<LCSProductCostSheet> costSheetList = new ArrayList<>();
        for (Object rawCS : rawCostSheetList) {
            if (rawCS instanceof LCSCostSheet && FLEX_COST_SHEET_TYPE_PRODUCT.equalsIgnoreCase(((LCSCostSheet)rawCS).getCostSheetType()) ) {
                if(!FormatHelper.equalsWithNull((((LCSProductCostSheet)rawCS).getSeasonMaster()),null)) {
                    costSheetList.add((LCSProductCostSheet) rawCS);
                }
            }
        }//end for loop

        if (costSheetList == null || costSheetList.isEmpty()) {
            throw new LuckyCustomPluginException("No product cost sheets found for sourcing config #" + sourcingConfigNumber + ".");
        }

        LOGGER.debug("[validateSourcingConfig] Found " + costSheetList.size() + " cost sheet for sourcing config #" + sourcingConfigNumber);

        List<LCSProductCostSheet> validatedCostSheetList = new ArrayList<>();

        //Validation of sourcing config against cost sheets.
        for (LCSProductCostSheet prodCS : costSheetList) {
            Long costSheetNumber = (Long)prodCS.getValue(AERO_COST_SHEET_NUMBER_ATTR);
            LOGGER.debug("[validateSourcingConfig] Cost Sheet # to check is: " + costSheetNumber + ".");


            try {
                LuckyPluginUtil.validateLuckyCostsheetType(prodCS);
            } catch (LuckyCustomPluginException cx) {
                LOGGER.debug("Not a valid costsheet type");
                continue;
            }

            LCSSeason season = (LCSSeason)VersionHelper.latestIterationOf(prodCS.getSeasonMaster());
            //Check if cost sheet is bound to a valid Aero season.
            try {
                LuckyPluginUtil.validateLuckySeasonType(season);
            } catch (LuckyCustomPluginException cx) {
                LOGGER.debug("[validateSourcingConfig] Skipping cost sheet #" + costSheetNumber + " due to invalid season type.");
                continue;
            }
            validateSourceAttrNonBlanks(sourcingConfig, prodCS, SOURCING_ATTR_LIST);
            validateAttrNonBlanks(sourcingConfig,prodCS,SOURCING_INCO_ATTR_LIST,prodCS.getValue("scIncoterms"));
            validatedCostSheetList.add(prodCS);
        }//end for loop.

        //Reset integration flags on validated cost sheets.
        for (LCSProductCostSheet validatedCS : validatedCostSheetList) {
            LuckyCostingRepository.resetIntegrationControlFlags(validatedCS);
        }//end for loop.

        LOGGER.debug("[validateSourcingConfig] Sourcing Config check is completed for sourcing config #" + sourcingConfigNumber + ".");
    }

    private static void validateAttrNonBlanks(FlexTyped flexTyped, LCSProductCostSheet costSheet, List<LuckyPayloadAttribute> attrList) throws LCSException, WTException {

        if (flexTyped == null || costSheet == null || attrList == null || attrList.isEmpty()) {
            LOGGER.debug("[validateAttrNonBlanks] Skipping non blank validation check.");
            return;
        }

        if (isCostSheetConfirmed(costSheet) && costSheet.getApplicableColorNames() != null) {
            LOGGER.debug("[validateAttrNonBlanks] No-blanks validation is required for this flex object.");
            LuckyPayloadAttributeUtil.validateNoBlanks(flexTyped, attrList);
        }
    }

    /**
     *
     * @param flexTyped
     * @param costSheet
     * @param attrList
     * @throws LCSException
     * @throws WTException
     */
    private static void validateSourceAttrNonBlanks(FlexTyped flexTyped, LCSProductCostSheet costSheet, List<LuckyPayloadAttribute> attrList) throws LCSException, WTException {

        if (flexTyped == null || costSheet == null || attrList == null || attrList.isEmpty()) {
            LOGGER.debug("[validateAttrNonBlanks] Skipping non blank validation check.");
            return;
        }

        if (isCostSheetConfirmed(costSheet) && costSheet.getApplicableColorNames() != null) {
            LOGGER.debug("[validateAttrNonBlanks] No-blanks validation is required for this flex object.");
            LuckyPayloadAttributeUtil.validateSourcingBlanks(flexTyped, attrList);
        }
    }

    private static void validateAttrNonBlanks(FlexTyped flexTyped, LCSProductCostSheet costSheet, List<LuckyPayloadAttribute> attrList, Object incoTerms) throws LCSException, WTException {

        if (flexTyped == null || costSheet == null || attrList == null || attrList.isEmpty()) {
            LOGGER.debug("[validateAttrNonBlanks] Skipping non blank validation check.");
            return;
        }

        if (isCostSheetConfirmed(costSheet) && costSheet.getApplicableColorNames() != null) {
            LOGGER.debug("[validateAttrNonBlanks] No-blanks validation is required for this flex object.");
            LuckyPayloadAttributeUtil.validateFullCircleBlanks(flexTyped, attrList,incoTerms);
        }
    }

    /**
     *
     * @param costSheet
     * @return
     * @throws WTException
     */
    public static boolean isCostSheetConfirmed(LCSProductCostSheet costSheet) throws WTException {
        if (costSheet == null || AERO_COST_SHEET_STATUS_CONFIRMED_VALUES.isEmpty()) {
            return false;
        }
        String costSheetStatus = (String)costSheet.getValue(AERO_COST_SHEET_STATUS_ATTR);
        return (costSheetStatus != null && AERO_COST_SHEET_STATUS_CONFIRMED_VALUES.contains(costSheetStatus));
    }

    /**
     *
     * @param sslink
     * @throws WTPropertyVetoException
     * @throws WTException
     * @throws LuckyCustomPluginException
     */
    private static void validateSourceToSeasonLink(LCSSourceToSeasonLink sslink) throws WTPropertyVetoException, WTException, LuckyCustomPluginException {

        if (sslink == null || !sslink.isLatestIteration()) {
            return;
        }

        LOGGER.debug("[validateSourceToSeasonLink] Source To Season Link to check is: " + sslink);

        LCSSourcingConfig sourcingConfig = (LCSSourcingConfig) VersionHelper.latestIterationOf(sslink.getSourcingConfigMaster());
        Long sourcingConfigNumber = (Long)sourcingConfig.getValue(AERO_SOURCING_CONFIG_NUMBER_ATTR);
        LCSProduct product = (LCSProduct)VersionHelper.latestIterationOf(sourcingConfig.getProductMaster());

        //Check if source to season link is bound to an Aero product.
        LuckyPluginUtil.validateLuckyProductType(product);

        LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(sslink.getSeasonMaster());

        //Check if cost sheet is bound to a valid Aero season.
        LuckyPluginUtil.validateLuckySeasonType(season);

        Object preSSlink = VersionHelper.predecessorOf(sslink);

        if (preSSlink == null) {
            throw new LuckyCustomPluginException("No previous source to season link found for '" + sslink + "' (Sourcing #" + sourcingConfigNumber +").");
        }

        List<LuckyPayloadAttribute> changedAttrList = LuckyPluginUtil.getChangedAttributes(sslink, (LCSSourceToSeasonLink) preSSlink, SOURCING_TO_SEASON_ATTR_LIST, LOGGER);

        if (changedAttrList.isEmpty()) {
            throw new LuckyCustomPluginException("No changes found for Source To Season Link '" + sslink + "' (Sourcing #" + sourcingConfigNumber + ").");
        }

        Collection<?> rawCostSheetList = LCSCostSheetQuery.getCostSheetsForSourceToSeason(sslink, null);

        if (rawCostSheetList == null || rawCostSheetList.isEmpty()) {
            throw new LuckyCustomPluginException("No cost sheets found for Source To Season Link '" + sslink + "' (Sourcing #" + sourcingConfigNumber + ").");
        }

        List<LCSProductCostSheet> costSheetList = new ArrayList<>();
        for (Object rawCS : rawCostSheetList) {
            if (rawCS instanceof LCSCostSheet && FLEX_COST_SHEET_TYPE_PRODUCT.equalsIgnoreCase(((LCSCostSheet)rawCS).getCostSheetType()) && !(((LCSCostSheet)rawCS).isWhatIf())) {
                costSheetList.add((LCSProductCostSheet)rawCS);
            }
        }//end for loop

        if (costSheetList == null || costSheetList.isEmpty()) {
            throw new LuckyCustomPluginException("No product cost sheets found for Source To Season Link '" + sslink + "' (Sourcing #" + sourcingConfigNumber + ").");
        }

        LOGGER.debug("[validateSourceToSeasonLink] Found " + costSheetList.size() + " cost sheet for sourcing config #" + sourcingConfigNumber);

        List<LCSProductCostSheet> validatedCostSheetList = new ArrayList<>();

        //Validation of source to season link against cost sheets.
        for (LCSProductCostSheet prodCS : costSheetList) {
            Long costSheetNumber = (Long)prodCS.getValue(AERO_COST_SHEET_NUMBER_ATTR);
            LOGGER.debug("[validateSourceToSeasonLink] Cost Sheet # to check is: " + costSheetNumber + ".");
            validateAttrNonBlanks(sslink, prodCS, SOURCING_TO_SEASON_ATTR_LIST);
            validatedCostSheetList.add(prodCS);
        }//end for loop.

        //Reset integration flags on validated cost sheets.
        for (LCSProductCostSheet validatedCS : validatedCostSheetList) {
            LuckyCostingRepository.resetIntegrationControlFlags(validatedCS);
        }//end for loop.

        LOGGER.debug("[validateSourceToSeasonLink] Source To Season Link check is completed for " + sslink + " (Sourcing #" + sourcingConfigNumber + ").");
    }

    /**
     *
     * @param costSheet
     * @throws WTPropertyVetoException
     * @throws WTException
     * @throws LuckyCustomPluginException
     */
    private static void validateProductCostSheet(LCSProductCostSheet costSheet) throws WTPropertyVetoException, WTException, LuckyCustomPluginException {

        if (costSheet == null) {
            return;
        }

        Long costSheetNumber = (Long)costSheet.getValue(AERO_COST_SHEET_NUMBER_ATTR);
        LOGGER.debug("[validateProductCostSheet] Cost Sheet # to check is: " + costSheetNumber + " (" + costSheet.getName() + ").");


        if (costSheet.isWhatIf()) {
            throw new LuckyCustomPluginException("Cost sheet #" + costSheetNumber + " is WHATIF.");
        }

        try {
            LuckyPluginUtil.validateLuckyCostsheetType(costSheet);
        }catch(LuckyCustomPluginException ex){
            LOGGER.debug("Invalid Cost Sheet type");
            return;
        }

        try {
            LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(costSheet.getSeasonMaster());

        //Check if cost sheet is bound to a valid Aero season.
        LuckyPluginUtil.validateLuckySeasonType(season);

        LCSProduct product = (LCSProduct)VersionHelper.latestIterationOf(costSheet.getProductMaster());

        LuckyPluginUtil.validateLuckyProductType(product);

            if (!(LuckyPluginUtil.isInterfaceAdmin()) && MethodContext.getContext().containsKey("SPARC_NO_COLORWAY_PRESENT")) {
                Boolean isSparcColorway = Boolean.parseBoolean((String)MethodContext.getContext().get("SPARC_NO_COLORWAY_PRESENT"));
							
                if (isSparcColorway) {
                    throw new LCSException("Cannot save the CostSheet as there are no colorways associated with CostSheet");
                }
            }
        LCSProductCostSheet prevCostSheet = (LCSProductCostSheet)VersionHelper.predecessorOf(costSheet);


        LCSSourcingConfigMaster objSourcingConfigMaster = costSheet.getSourcingConfigMaster();
        LCSSourcingConfig sourcingConfig = (LCSSourcingConfig)VersionHelper.latestIterationOf((Mastered)objSourcingConfigMaster);

        LCSSourceToSeasonLink sslink = new LCSSourcingConfigQuery().getSourceToSeasonLink(costSheet.getSourcingConfigMaster(), costSheet.getSeasonMaster());
        validateAttrNonBlanks(costSheet, COST_SHEET_ATTR_LIST);
        validateSourceAttrNonBlanks(sourcingConfig, costSheet, SOURCING_ATTR_LIST);
        validateAttrNonBlanks(sourcingConfig,costSheet,SOURCING_INCO_ATTR_LIST,costSheet.getValue("scIncoterms"));
        validateAttrNonBlanks(sslink, costSheet, SOURCING_TO_SEASON_ATTR_LIST);

        if (prevCostSheet == null) {
            throw new LuckyCustomPluginException("No previous cost sheet was found for cost sheet #" + costSheetNumber + ".");
        }

        List<LuckyPayloadAttribute> changedAttrList = LuckyPluginUtil.getChangedAttributes(costSheet, prevCostSheet, COST_SHEET_ATTR_LIST, LOGGER);

        if (changedAttrList.isEmpty() && !isCostSheetColorwaysChanged(costSheet.getApplicableColorNames(), prevCostSheet.getApplicableColorNames())) {
            throw new LuckyCustomPluginException("No changes found for cost sheet #" + costSheetNumber + ".");
        }

        boolean skipPersist= true;
        LuckyCostingRepository.resetIntegrationControlFlags(costSheet,skipPersist);

        LOGGER.debug("[validateProductCostSheet] Cost sheet check is completed for cost sheet #" + costSheetNumber + ".");
        }
        finally{
            if (MethodContext.getContext().containsKey("SPARC_NO_COLORWAY_PRESENT")) {
                MethodContext.getContext().remove("SPARC_NO_COLORWAY_PRESENT");
            }
        }
    }

    /**
     *
     * @param costSheet
     * @param attrList
     * @throws LCSException
     * @throws WTException
     */
    private static void validateAttrNonBlanks(LCSProductCostSheet costSheet, List<LuckyPayloadAttribute> attrList) throws LCSException, WTException {

        if (costSheet == null || attrList == null || attrList.isEmpty()) {
            LOGGER.debug("[validateAttrNonBlanks] Skipping non blank validation check.");
            return;
        }

        if (isCostSheetConfirmed(costSheet) && costSheet.getApplicableColorNames() != null) {
            LOGGER.debug("[validateAttrNonBlanks] No-blanks validation is required for this cost sheet.");
            LuckyPayloadAttributeUtil.validateNoBlanks(costSheet, attrList);
        }
    }


    /**
     * Checks whether the current and previous colorways selected for the cost sheet have changed or not.
     * @param currColorwayNames The current cost sheet colorways.
     * @param prevColorwayNames The previous cost sheet colorways.
     * @return trus if the cost sheet colorways assigned have changed or false otherwise.
     */
    private static boolean isCostSheetColorwaysChanged(String currColorwayNames, String prevColorwayNames) {

        if (currColorwayNames != null && currColorwayNames.toString().isEmpty()) {
            currColorwayNames = null;
        }
        if (prevColorwayNames != null && prevColorwayNames.toString().isEmpty()) {
            prevColorwayNames = null;
        }
        LOGGER.debug("[isCostSheetColorwayChanged] currAttrValue = " + currColorwayNames + " <-> attrPrevValue = " + prevColorwayNames);
        return SparcIntegrationUtil.divergenceCheck(prevColorwayNames, currColorwayNames);
    }
    
	/**
     * Checks whether current flex object is changed when compared to its previous version.
     * @param current The current flex object to check.
     * @param previous The previous flex object to compare against the current one.
     * @param attributes List of flex object attributes to use for the comparison.
     * @return true if the object has changed or if no previous object was provided, false otherwise.
     * @throws WTException If an error occurs while verifying attribute values from the current and/or previous flex objects.
     */
    private static boolean isChanged(FlexTyped current, FlexTyped previous, String attributes) throws WTException {

        Boolean isChanged = false;

        if (previous == null) {
            isChanged = false;
            LOGGER.debug("=============DONE with isChanged (no previous flex version), returning " + isChanged + " === ");
            return isChanged;
        }
        List<String> removeAtts = Arrays.asList(attributes.split("\\s*,\\s*"));
        for(String strAttribute: removeAtts){
            if (FormatHelper.hasContent(strAttribute)) {
                Object currAttrValue = current.getValue(strAttribute);
                LOGGER.debug("=============currAttrValue =========" + currAttrValue);
                Object attrPrev = null;
                if (previous != null) {
                    attrPrev = previous.getValue(strAttribute);
                }
                LOGGER.debug("=============attrPrev =========" + attrPrev);
                if (currAttrValue != null && attrPrev != null) {
                    if (!(attrPrev.toString().compareTo(currAttrValue.toString()) == 0)) {
                        isChanged = true;
                        break;
                    }
                } else if ((currAttrValue != null && attrPrev == null) || (attrPrev != null && currAttrValue == null)) {
                    isChanged = true;
                    break;
                }
            }
        }
        return isChanged;
    }
	
}
