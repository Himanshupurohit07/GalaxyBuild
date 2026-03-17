package com.sparc.wc.integration.services;

import com.lcs.wc.color.LCSColor;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.util.LCSException;
import com.sparc.wc.integration.constants.SparcIntegrationConstants;
import com.sparc.wc.integration.domain.SparcPropertyDefinitions;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.sparc.wc.integration.util.SparcQueryUtil;
import com.sparc.wc.util.SparcConstants;
import org.apache.logging.log4j.Logger;
import wt.log4j.LogR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SparcColorWatchDogService {

    private final static Logger LOGGER = LogR.getLogger(SparcProductWatchDogService.class.getName());

    private final SparcColorwayWatchDogService colorwayWatchDogService;

    public SparcColorWatchDogService() {
        this.colorwayWatchDogService = new SparcColorwayWatchDogService();
    }

    public void check(final LCSColor deltaChangesColor) {
        if (deltaChangesColor.getPersistInfo() == null || deltaChangesColor.getPersistInfo().getObjectIdentifier() == null) {
            LOGGER.info("Detected new color, ignoring divergence check");
            return;
        }
        try {
            final LCSColor existingColor = SparcQueryUtil.<LCSColor>findObjectById("OR:com.lcs.wc.color.LCSColor:" + deltaChangesColor.getPersistInfo().getObjectIdentifier().getId());
            final boolean styleAttrsDivergence = checkHierarchyAttrsDivergence(existingColor, deltaChangesColor, SparcPropertyDefinitions.Hierarchy.STYLE);
            if (styleAttrsDivergence) {
                setFlag(existingColor, SparcPropertyDefinitions.Hierarchy.STYLE);              
            }          
        } catch (Exception e) {
            LOGGER.error("Encountered an error while checking divergence on Color, error:" + e);
        }

    }

    private void setFlag(final LCSColor color, SparcPropertyDefinitions.Hierarchy hierarchy) {
        final List<LCSSKU> colorwaysUsedByColor = getColorwaysUsedByColor(color);
        LOGGER.debug("Colorways used by color - size:" + colorwaysUsedByColor.size());
        colorwaysUsedByColor.stream().forEach(sku -> {
            colorwayWatchDogService.setFlag(sku, hierarchy);
        });
    }

    private boolean checkHierarchyAttrsDivergence(final LCSColor existingColor, final LCSColor deltaChangesColor, SparcPropertyDefinitions.Hierarchy hierarchy) {
        final Set<String> attrs = SparcIntegrationConstants.LUCKY_COLOR_PLUGIN_PROPERTY_DEFINITIONS.getAttrsFromHierarchy();
        if (attrs.isEmpty()) {
            return false;
        }
        return attrs.stream().anyMatch(attr -> {
            final Object deltaChange = SparcIntegrationUtil.getValueFrom(deltaChangesColor, attr);
            final Object existing = SparcIntegrationUtil.getValueFrom(existingColor, attr);
            return SparcIntegrationUtil.divergenceCheck(deltaChange, existing) == true;
        });
    }

    private List<LCSSKU> getColorwaysUsedByColor(final LCSColor color) {
        final PreparedQueryStatement pqs = new PreparedQueryStatement();
        try {
            pqs.appendSelectColumn(SparcConstants.SKU_OBJECT, SparcConstants.BRANCH_ITERATION);
            pqs.appendFromTable(LCSSKU.class);
            pqs.appendFromTable(LCSColor.class);
            pqs.appendJoin(SparcConstants.SKU_OBJECT, "idA3A2typeInfoLCSSKU", "LCSColor", "ida2a2");
            pqs.appendAndIfNeeded();
            pqs.appendCriteria(new Criteria("LCSColor", "ida2a2", color.getPersistInfo().getObjectIdentifier().getId() + "", Criteria.EQUALS));
            pqs.appendAndIfNeeded();
            pqs.appendCriteria(new Criteria(SparcConstants.SKU_OBJECT, SparcConstants.LATEST_ITERATION_INFO, SparcConstants.ONE, Criteria.EQUALS));
            pqs.appendAndIfNeeded();
            pqs.appendCriteria(new Criteria(SparcConstants.SKU_OBJECT, SparcConstants.PRODUCT_SEASON_REVID, "0", Criteria.NOT_EQUAL_TO));
            final SearchResults sr = LCSQuery.runDirectQuery(pqs);
            if (sr == null) {
                return new ArrayList<>();
            }
            final Collection<FlexObject> results = sr.getResults();
            if (results == null) {
                return new ArrayList<>();
            }
            return results.stream()
                    .map(fo -> fo.getString(SparcConstants.SKU_OBJECT + "." + SparcConstants.BRANCH_ITERATION))
                    .map(id -> SparcQueryUtil.<LCSSKU>findObjectById("VR:com.lcs.wc.product.LCSSKU:" + id))
                    .filter(sku -> sku != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Encountered an error while querying for colorways using the given color, error:" + e);
            return new ArrayList<>();
        }
    }

}
