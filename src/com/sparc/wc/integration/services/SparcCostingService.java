package com.sparc.wc.integration.services;

import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSCostSheetQuery;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.sparc.wc.integration.constants.SparcIntegrationConstants;
import com.sparc.wc.integration.domain.SparcCostingResponse;
import com.sparc.wc.integration.domain.SparcTypeAttributeCollection;
import com.sparc.wc.integration.exceptions.SparcEntityNotFoundException;
import com.sparc.wc.integration.exceptions.SparcGenericException;
import com.sparc.wc.integration.repository.SourcingConfigRepository;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.sparc.wc.integration.util.SparcQueryUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import wt.log4j.LogR;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparcCostingService {

    private static final Logger LOGGER = LogR.getLogger(SparcCostingService.class.getName());

    public SparcCostingResponse getCostingResponse(final String sourcingConfigNumber) throws SparcGenericException {
        if (sourcingConfigNumber == null || sourcingConfigNumber.isEmpty()) {
            LOGGER.error("Expecting a valid sourcing config number");
            throw new SparcGenericException("Sourcing Config cannot be empty", 400);
        }
        final LCSSourcingConfig sourcingConfig = SourcingConfigRepository.getSourcingConfigFrom(sourcingConfigNumber);
        final List<LCSCostSheet> costSheets = getCostSheets(sourcingConfig);
        if (costSheets == null || costSheets.isEmpty()) {
            throw new SparcEntityNotFoundException("No cost sheets were found which are associate to given Sourcing config number");
        }
        final SparcCostingResponse costingResponse = new SparcCostingResponse();
        costingResponse.setSourcingConfigNumber(sourcingConfigNumber);
        costSheets.stream()
                .filter(costsheet -> costsheet != null)
                .filter(costSheet -> validateF21FlexTypes(costSheet))
                .filter(costSheet -> isCostSheetExportable(costSheet))
                .forEach(costsheet -> extractCostSheetData(costsheet, costingResponse));
        return costingResponse;
    }

    private boolean isCostSheetExportable(final LCSCostSheet costSheet) {

        final String vendorCostingStatus = (String) SparcIntegrationUtil.getValueFrom(costSheet, SparcIntegrationConstants.F21_COSTING_VENDOR_COSTING_STATUS_ATTRIBUTE_INTERNAL_NAME, false);
        if (vendorCostingStatus == null || vendorCostingStatus.isEmpty()) {
            return false;
        }
        if (!SparcIntegrationConstants.F21_COSTING_VENDOR_COSTING_STATUS_ALLOWED_VALUES.contains(vendorCostingStatus)) {
            return false;
        }
        final String costsheetInternalStatus = (String) SparcIntegrationUtil.getValueFrom(costSheet, SparcIntegrationConstants.F21_COSTING_INTERNAL_COSTSHEET_STATUS_ATTRIBUTE_INTERNAL_NAME, false);
        if (costsheetInternalStatus == null || costsheetInternalStatus.isEmpty()) {
            return false;
        }
        if (!SparcIntegrationConstants.F21_COSTING_INTERNAL_COSTSHEET_STATUS_ALLOWED_VALUES.contains(costsheetInternalStatus)) {
            return false;
        }
        return true;
    }

    private boolean validateF21FlexTypes(final LCSCostSheet costSheet) {
        final LCSPartMaster productMaster = costSheet.getProductMaster();
        if (productMaster != null) {
            final LCSProduct prd = SparcQueryUtil.findLatestIterationOf(productMaster);
            if (prd == null) {
                LOGGER.error("Product cannot be empty for the given costsheet");
                return false;
            }
            final boolean isF21Prd = prd.getFlexType().getFullName(true).startsWith(SparcIntegrationConstants.F21_COSTING_PRODUCT_FLEX_PATH);
            if (!isF21Prd) {
                return false;
            }
        }
        final LCSSeasonMaster seasonMaster = costSheet.getSeasonMaster();
        if (seasonMaster != null) {
            final LCSSeason season = SparcQueryUtil.findLatestIterationOf(seasonMaster);
            if (season != null) {
                final boolean isF21Season = season.getFlexType().getFullName(true).startsWith(SparcIntegrationConstants.F21_COSTING_SEASON_FLEX_PATH);
                if (!isF21Season) {
                    return false;
                }
            }
        }
        return true;
    }

    private void extractCostSheetData(final LCSCostSheet costSheet, final SparcCostingResponse response) {
        final SparcTypeAttributeCollection attributes = SparcIntegrationConstants.F21_COSTING_PAYLOAD_ATTRIBUTES;
        if (attributes == null || attributes.getAttributes() == null || attributes.getAttributes().isEmpty()) {
            LOGGER.error("No payload attributes (F21 Costing) were defined to include in response");
            return;
        }

        final Map<String, Object> data = new HashMap<>();
        attributes.getAttributes().stream()
                .filter(attr -> attr != null)
                .forEach(attr -> {
                    final Object value = SparcIntegrationUtil.getValueFrom(costSheet, attr.getInternalName(), true);
                    data.put(attr.getAlias(), value);
                });
        addSeasonData(costSheet, data);
        SparcIntegrationUtil.addTemporalFields(costSheet, "MMddyyyy", data);
        response.addCostsheet(data);
    }

    private void addSeasonData(final LCSCostSheet costSheet, final Map<String, Object> data) {
        final LCSSeasonMaster seasonMaster = costSheet.getSeasonMaster();
        final LCSSeason season = SparcQueryUtil.findLatestIterationOf(seasonMaster);
        if (season == null) {
            LOGGER.warn("No season is associated to given costsheet, ignoring season data");
            return;
        }
        data.put("season", season.getName());
    }

    private List<LCSCostSheet> getCostSheets(final LCSSourcingConfig sourcingConfig) throws SparcGenericException {
        try {
            return (List<LCSCostSheet>) LCSCostSheetQuery.getAllCostSheetsForSourcingConfig(sourcingConfig, SparcIntegrationConstants.F21_COSTING_INCLUDE_WHAT_IF_COST_SHEETS);
        } catch (Exception e) {
            LOGGER.error("Encountered an error while fetching cost sheets associated with the given sourcing config, error:" + e.getMessage());
            throw new SparcGenericException("Cannot fetch cost sheets at this moment, please contact system administrator if the problem persists", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
