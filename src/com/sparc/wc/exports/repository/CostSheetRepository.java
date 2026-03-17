package com.sparc.wc.exports.repository;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSCostSheetMaster;
import com.lcs.wc.sourcing.LCSCostSheetQuery;
import com.sparc.wc.integration.util.SparcQueryUtil;
import org.apache.logging.log4j.Logger;
import wt.log4j.LogR;

import java.util.Collection;

public final class CostSheetRepository {

    private static final Logger LOGGER = LogR.getLogger(CostSheetRepository.class.getName());

    private CostSheetRepository() {

    }

    public static LCSSKU getRepresentativeColorwayOfCostSheet(final LCSCostSheet costSheet) {

        try {
            final Collection<FlexObject> colors = LCSCostSheetQuery.getRepresentativeColor((LCSCostSheetMaster) costSheet.getMaster());
            if (colors == null || colors.isEmpty()) {
                return null;
            }
            return colors.stream()
                    .map(fo -> {
                        return SparcQueryUtil.<LCSSKU>findObjectById("VR:com.lcs.wc.product.LCSSKU:" + fo.getString("LCSSKU.BRANCHIDITERATIONINFO"));
                    })
                    .filter(sku -> sku != null)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            LOGGER.error("Encountered an error while retrieving representative colorway from cost-sheet, error:" + e.getMessage());
            return null;
        }
    }
}
