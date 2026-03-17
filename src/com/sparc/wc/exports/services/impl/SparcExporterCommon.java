package com.sparc.wc.exports.services.impl;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.ProductHeaderQuery;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.VersionHelper;
import com.sparc.tc.domain.InterpreterContext;
import com.sparc.tc.domain.TemplateBuilderContext;
import com.sparc.tc.exceptions.TCException;
import com.sparc.tc.util.StringUtils;
import com.sparc.wc.exports.constants.SparcExportConstants;
import com.sparc.wc.exports.repository.CostSheetRepository;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.sparc.wc.tc.domain.ObjectsCluster;
import org.apache.logging.log4j.Logger;
import wt.log4j.LogR;
import wt.org.WTUser;
import wt.util.WTException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SparcExporterCommon {

    private static final Logger LOGGER = LogR.getLogger(SparcExporterCommon.class.getName());

    protected void logInterpreterContext(final InterpreterContext interpreterContext) {
        if (interpreterContext.getInstructionErrors() != null && !interpreterContext.getInstructionErrors().isEmpty()) {
            LOGGER.error("Following errors are encountered while interpreting the template:\n" + interpreterContext.getInstructionErrors());
        }
        if (interpreterContext.getInstructionWarnings() != null && !interpreterContext.getInstructionWarnings().isEmpty()) {
            LOGGER.error("Following warnings are encountered while interpreting the template:\n" + interpreterContext.getInstructionWarnings());
        }
    }

    protected void logTemplateBuilderContext(final TemplateBuilderContext templateBuilderContext) {
        if (templateBuilderContext.getCopyErrors() != null && !templateBuilderContext.getCopyErrors().isEmpty()) {
            LOGGER.error("Following errors are encountered in template-builder:\n" + templateBuilderContext.getCopyErrors());
        }
        if (templateBuilderContext.getCopyWarnings() != null && !templateBuilderContext.getCopyWarnings().isEmpty()) {
            LOGGER.error("Following warning are encountered in template-builder:\n" + templateBuilderContext.getCopyWarnings());
        }
    }

    protected void addSeasonToCluster(final LCSSeason season, final ObjectsCluster objectsCluster) throws TCException {
        if (season == null) {
            return;
        }
        objectsCluster.getFlexObjectsCluster().addNonGroupedNode(season, LCSSeason.class.getName());
        objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.PLM_SEASON_NAME, season.getName());

    }

    protected void addCostSheetToCluster(final LCSCostSheet costSheet, final ObjectsCluster objectsCluster) throws TCException {
        if (costSheet == null) {
            return;
        }
        objectsCluster.getFlexObjectsCluster().addNonGroupedNode(costSheet, LCSCostSheet.class.getName());
        objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.REPRESENTATIVE_COSTING_SIZE, costSheet.getRepresentativeSize());
        objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.PLM_COST_SHEET_NAME, costSheet.getName());
        objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.PLM_COST_SHEET_NUMBER, SparcIntegrationUtil.getValueFrom(costSheet, SparcExportConstants.COST_SHEET_NUMBER_INTERNAL_NAME));
        final LCSSKU representativeColorway = CostSheetRepository.getRepresentativeColorwayOfCostSheet(costSheet);
        if (representativeColorway != null) {
            objectsCluster.getFlexObjectsCluster().addNonGroupedNode(representativeColorway, LCSSKU.class.getName());
            objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.REPRESENTATIVE_COLORWAY_NAME, SparcIntegrationUtil.getValueFrom(representativeColorway, "skuName"));
//            objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.REPRESENTATIVE_COLORWAY_NAME, representativeColorway.getName());
        } else {
            LOGGER.warn("No representative colorway was found for cost-sheet:" + costSheet.getName());
        }
    }

    protected void addSkuSeasonLinkToCluster(final LCSSKUSeasonLink link, final ObjectsCluster objectsCluster) throws TCException {
        if (link == null || objectsCluster == null) {
            return;
        }
        objectsCluster.getFlexObjectsCluster().addNonGroupedNode(link, LCSSKUSeasonLink.class.getName());
    }

    protected void addProductToCluster(final LCSProduct product, final ObjectsCluster objectsCluster) throws TCException {
        if (product == null) {
            return;
        }
        try {
            LOGGER.debug("Product version:" + VersionHelper.getFullVersionIdentifierValue(product));
            final LCSProduct productARev = (LCSProduct) VersionHelper.getVersion(product, "A");
            objectsCluster.getFlexObjectsCluster().addNonGroupedNode(productARev, LCSProduct.class.getName());
            objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.PLM_PRODUCT_NAME, productARev.getName());
            objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.PLM_PRODUCT_NUMBER, SparcIntegrationUtil.getValueFrom(productARev, SparcExportConstants.PRODUCT_NUMBER_INTERNAL_NAME));
        } catch (WTException e) {
            LOGGER.error("Encountered an error while fetching prod-a-rev, error:" + e.getMessage());
        }
    }

    protected void addSourcingConfigToCluster(final LCSSourcingConfig sourcingConfig, final ObjectsCluster objectsCluster) throws TCException {
        if (sourcingConfig == null) {
            return;
        }
        objectsCluster.getFlexObjectsCluster().addNonGroupedNode(sourcingConfig, LCSSourcingConfig.class.getName());
        objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.SOURCING_CONFIG_NAME, sourcingConfig.getSourcingConfigName());
        final LCSSupplier finishedGoodsFactorySupplier = (LCSSupplier) SparcIntegrationUtil.getValueFrom(sourcingConfig, SparcExportConstants.SOURCING_CONFIG_FINISHED_GOODS_FACTORY_INTERNAL_NAME);
        if (finishedGoodsFactorySupplier == null) {
            LOGGER.warn("Finished Goods Factory is found empty for the given sourcing configuration:" + sourcingConfig.getSourcingConfigName());
        } else {
            final LCSCountry countryOfOrigin = (LCSCountry) SparcIntegrationUtil.getValueFrom(finishedGoodsFactorySupplier, SparcExportConstants.FINISHED_GOODS_FACTORY_COUNTRY_INTERNAL_NAME);
            if (countryOfOrigin != null) {
                objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.COUNTRY_OF_ORIGIN, countryOfOrigin.getCountryName());
            } else {
                LOGGER.warn("Country of Origin is not defined for given Finished good Factory");
            }
        }
        final LCSSupplier vendor = (LCSSupplier) SparcIntegrationUtil.getValueFrom(sourcingConfig, SparcExportConstants.SOURCING_CONFIG_VENDOR_INTERNAL_NAME);
        if (vendor == null) {
            LOGGER.warn("No Vendor has been found for given sourcing configuration:" + sourcingConfig.getName());
        } else {
            objectsCluster.getFlexObjectsCluster().addNonGroupedNode(vendor, LCSSupplier.class.getName());
            objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.PLM_VENDOR_NAME, vendor.getName());
        }
        final LCSSupplier agentOffice = (LCSSupplier) SparcIntegrationUtil.getValueFrom(sourcingConfig, SparcExportConstants.SOURCING_CONFIG_AGENT_OFFICE_INTERNAL_NAME);
        if (agentOffice == null) {
            LOGGER.warn("No Agent/Office has been found for given sourcing configuration:" + sourcingConfig.getName());
        } else {
            final Object commissionPercent = SparcIntegrationUtil.getValueFrom(agentOffice, SparcExportConstants.SUPPLIER_AGENT_COMMISSION_US_INTERNAL_NAME);
            objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.PLM_AGENT_COMMISSION_PERCENT_US, commissionPercent);
        }
    }

    protected void addSeasonProductSourcingConfigurationCommonDataToCluster(final LCSSeason season, final LCSProduct product, final LCSSourcingConfig sourcingConfig, final LCSCostSheet costSheet, final ObjectsCluster objectsCluster) {
        if (season == null || product == null || costSheet == null) {
            return;
        }
        if (!StringUtils.hasContent(costSheet.getApplicableColorNames())) {
            LOGGER.warn("No Applicable colorways were found for the given cost-sheet:" + costSheet.getName());
            return;
        }
        final List<String> applicableColors = Arrays.stream(costSheet.getApplicableColorNames().split(SparcExportConstants.MOA_DELIMITER)).map(String::trim).collect(Collectors.toList());
        objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.COST_SHEET_SELECTED_COLORWAY_NAMES, applicableColors);
        try {
            final LCSSeasonProductLink seasonProductLink = LCSSeasonQuery.findSeasonProductLink(product, season);
            if (seasonProductLink != null) {
                objectsCluster.getFlexObjectsCluster().addNonGroupedNode(seasonProductLink, LCSSeasonProductLink.class.getName());
            } else {
                LOGGER.warn("No Product-Season link is found");
            }
            final Collection<LCSSKU> skUs = (new ProductHeaderQuery()).findSKUs(product, null, season, false);
            if (skUs == null || skUs.isEmpty()) {
                LOGGER.warn("Expecting a valid list of colorways for the given product, sourcing-config and season, but found none and there are selected colorways on cost-sheet, please check data");
                return;
            }
            final List<Object> colorwayNums = applicableColors.stream().map(colorName -> {
                LOGGER.debug("Iterating colorway:" + colorName);
                final LCSSKU lcssku = skUs.stream().filter(sku -> {
                    LOGGER.debug("Colorway:" + sku.getName() + " contains cost-sheet applicable color:" + sku.getName().contains(colorName));
                    return sku.getName().contains(colorName);
                }).findFirst().orElse(null);
                final Object colorwayNum = SparcIntegrationUtil.getValueFrom(lcssku, SparcExportConstants.COLORWAY_NUMBER_INTERNAL_NAME);
                LOGGER.debug("Found applicable colorway number:" + colorwayNum);
                return colorwayNum;
            }).collect(Collectors.toList());
            objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.COST_SHEET_SELECTED_COLORWAY_IDS, colorwayNums);
        } catch (Exception e) {
            LOGGER.warn("Encountered an error while fetching colorways from product, sourcing-config and season, error:" + e.getMessage());
        }
    }

    protected void addTemporalValuesToObjectsCluster(final ObjectsCluster objectsCluster) {
        objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.EXPORTED_TIME, new Date());
    }

    protected void addUserContextValuesToObjectsCluster(final ObjectsCluster objectsCluster) {
        try {
            final WTUser principle = ClientContext.getContext().getUser();
            if (principle == null) {
                return;
            }
            objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.EXPORTED_USER, principle.getName());
            objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.EXPORTED_USER_FULL_NAME, principle.getFullName());
            objectsCluster.getNonFlexObjectsCluster().addNonGroupedNode(SparcExportConstants.EXPORTED_USER_MAIL, principle.getEMail());
        } catch (WTException e) {
            LOGGER.error("Encountered an error while fetching current logged in user, this might be happening if a thread is started without a http request context");
        }
    }

}
