package com.sparc.wc.integration.repository;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.sparc.wc.integration.constants.SparcIntegrationConstants;
import com.sparc.wc.integration.exceptions.SparcEntityNotFoundException;
import com.sparc.wc.integration.util.SparcQueryUtil;
import com.sparc.wc.util.SparcConstants;
import org.apache.logging.log4j.Logger;
import wt.log4j.LogR;
import wt.util.WTException;

import java.util.Collection;

public class SourcingConfigRepository {

    private static final Logger LOGGER = LogR.getLogger(SourcingConfigRepository.class.getName());

    private SourcingConfigRepository() {

    }

    public static LCSSourcingConfig getSourcingConfigFrom(final String sourcingConfigNumber) throws SparcEntityNotFoundException {
        if (sourcingConfigNumber == null || sourcingConfigNumber.isEmpty()) {
            LOGGER.error("Expecting a valid sourcing config number to fetch Sourcing config");
            return null;
        }
        final PreparedQueryStatement query = createQuery(sourcingConfigNumber);
        return getSourcingConfig(query);
    }

    private static LCSSourcingConfig getSourcingConfig(final PreparedQueryStatement query) throws SparcEntityNotFoundException {
        final SearchResults searchResults = SparcQueryUtil.runQuery(query);
        if (searchResults == null) {
            throw new SparcEntityNotFoundException("No Sourcing Config found with the given souring config number");
        }
        final Collection<FlexObject> results = searchResults.getResults();
        if (results == null || results.isEmpty()) {
            throw new SparcEntityNotFoundException("No sourcing config has been found with the given Sourcing Config Number");
        }
        final String branchId = results.stream()
                .filter(fo -> fo != null)
                .map(fo -> fo.get(SparcConstants.SOURCING_OBJECT + "." + SparcConstants.BRANCH_ITERATION))
                .filter(bId -> bId != null)
                .map(bid -> (String) bid)
                .findFirst()
                .orElse(null);
        if (branchId == null || branchId.isEmpty()) {
            throw new SparcEntityNotFoundException("No sourcing config has been found with the given Sourcing Config Number");
        }
        return SparcQueryUtil.findObjectById("VR:com.lcs.wc.sourcing.LCSSourcingConfig:" + branchId);
    }

    private static PreparedQueryStatement createQuery(final String sourcingConfigNumber) {
        try {
            final FlexType sourcingConfigFlexType = FlexTypeCache.getFlexTypeFromPath(SparcIntegrationConstants.F21_SOURCING_CONFIG_FLEX_PATH);
            if (sourcingConfigFlexType == null) {
                LOGGER.error("Expecting a valid sourcing config flexType, but found null, cannot create query");
                return null;
            }
            final FlexTypeAttribute sourcingConfigNumAttr = sourcingConfigFlexType.getAttribute(SparcIntegrationConstants.F21_COSTING_SOURCING_CONFIG_NUMBER_INTERNAL_NAME);
            if (sourcingConfigNumAttr == null) {
                LOGGER.error("Expecting a valid sourcing config number attribute, but found null, cannot create query");
                return null;
            }
            final PreparedQueryStatement query = new PreparedQueryStatement();
            query.appendSelectColumn(new QueryColumn(SparcConstants.SOURCING_OBJECT, SparcConstants.BRANCH_ITERATION));
            query.appendFromTable(LCSSourcingConfig.class);
            query.appendCriteria(new Criteria(SparcConstants.SOURCING_OBJECT, sourcingConfigNumAttr.getColumnName(), sourcingConfigNumber, Criteria.EQUALS));
            query.appendAndIfNeeded();
            query.appendCriteria(new Criteria(SparcConstants.SOURCING_OBJECT, SparcConstants.LATEST_ITERATION_INFO, SparcConstants.ONE, Criteria.EQUALS));
            return query;
        } catch (WTException e) {
            LOGGER.error("Encountered an error while creating query for fetching sourcing config from sourcing config #, error:" + e.getMessage());
        }

        return null;
    }
}
