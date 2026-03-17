package com.sparc.wc.exports.utils;

import com.sparc.wc.exports.constants.SparcExportConstants;
import com.sparc.wc.exports.domain.SparcLineSheetData;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class SparcLineSheetUtil {

    private static final String SKU_COST_SHEET_DELIMITER            = "\\|~\\*~\\|";
    private static final String COST_SHEET_ENTRIES_DELIMITER        = ",";
    private static final String COST_SHEET_MASTER_IDENTIFIER_STRING = "OR:com.lcs.wc.sourcing.LCSCostSheetMaster:";
    private static final String SKU_SEASON_LINK_IDENTIFIER_STRING   = "OR:com.lcs.wc.season.LCSSKUSeasonLink:";

    private SparcLineSheetUtil() {

    }

    public static SparcLineSheetData parse(final String seasonOid, final String division, final String exportType, final String selectedCostSheets) {

        if (!SparcStringUtils.hasContent(seasonOid) || !SparcStringUtils.hasContent(division) || !SparcStringUtils.hasContent(exportType) || !SparcStringUtils.hasContent(selectedCostSheets)) {
            return null;
        }
        final List<SparcLineSheetData.CostSheet> costSheets = Arrays.stream(selectedCostSheets.split(COST_SHEET_ENTRIES_DELIMITER, -1))
                .map(costSheetEntry -> {
                    if (!SparcStringUtils.hasContent(costSheetEntry)) {
                        return null;
                    }
                    final String[] components = costSheetEntry.split(SKU_COST_SHEET_DELIMITER, -1);
                    if (components.length != 2) {
                        return null;
                    }
                    return SparcLineSheetData.CostSheet.builder().skuSeasonOid(SKU_SEASON_LINK_IDENTIFIER_STRING + components[0].trim()).costSheetOid(COST_SHEET_MASTER_IDENTIFIER_STRING + components[1].trim()).build();
                })
                .filter(costSheet -> costSheet != null)
                .collect(Collectors.toList());
        return SparcLineSheetData.builder()
                .division(SparcStringUtils.stringToEnum(division.toUpperCase(), SparcLineSheetData.Division.class))
                .exportType(SparcStringUtils.stringToEnum(exportType.toUpperCase(), SparcLineSheetData.ExportType.class))
                .seasonOid(seasonOid)
                .costSheetList(costSheets)
                .build();
    }
}
