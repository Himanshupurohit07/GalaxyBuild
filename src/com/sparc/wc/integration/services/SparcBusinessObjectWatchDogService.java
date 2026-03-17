package com.sparc.wc.integration.services;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
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

public class SparcBusinessObjectWatchDogService {

    private static final Logger LOGGER = LogR.getLogger(SparcBusinessObjectWatchDogService.class.getName());

    private SparcProductWatchDogService productWatchDogService;

    public SparcBusinessObjectWatchDogService() {
        this.productWatchDogService = new SparcProductWatchDogService();
    }

    public void check(final LCSLifecycleManaged deltaChangesBO) {
        if (deltaChangesBO.getPersistInfo() == null || deltaChangesBO.getPersistInfo().getObjectIdentifier() == null) {
            LOGGER.info("Detected new Business Object, not checking for divergence");
            return;
        }
        try {
            final LCSLifecycleManaged existingBO = SparcQueryUtil.<LCSLifecycleManaged>findObjectById("OR:com.lcs.wc.foundation.LCSLifecycleManaged:" + deltaChangesBO.getPersistInfo().getObjectIdentifier().getId(), false);
            if (existingBO == null) {
                LOGGER.info("No existing Business Object has been found for given delta changes, not checking for divergence");
                return;
            }
            final boolean styleDivergence = checkHierarchyAttrsDivergence(existingBO, deltaChangesBO, SparcPropertyDefinitions.Hierarchy.STYLE);
          //  System.out.println("styleDivergence-------SparcBusinessObjectWatchDogService------------------------"+styleDivergence);
            if (styleDivergence) {
                getProductsUsedByBusinessObject(existingBO).stream().forEach(product -> productWatchDogService.setFlag(product, SparcPropertyDefinitions.Hierarchy.STYLE));
            }

        } catch (Exception e) {
            LOGGER.error("Encountered an error while checking divergence on Business Object, error:" + e);
        }
    }

    private boolean checkHierarchyAttrsDivergence(final LCSLifecycleManaged existingBO, final LCSLifecycleManaged deltaChangesBO, SparcPropertyDefinitions.Hierarchy hierarchy) {
        final Set<String> attrs = SparcIntegrationConstants.LUCKY_BO_PLUGIN_PROPERTY_DEFINITIONS.getAttrsFromHierarchy();
        if (attrs.isEmpty()) {
            return false;
        }
        return attrs.stream().anyMatch(attr -> {
            final Object deltaChange = SparcIntegrationUtil.getValueFrom(deltaChangesBO, attr);
            final Object existing = SparcIntegrationUtil.getValueFrom(existingBO, attr);
            return SparcIntegrationUtil.divergenceCheck(deltaChange, existing) == true;
        });
    }

    private List<LCSProduct> getProductsUsedByBusinessObject(final LCSLifecycleManaged businessObject) {
        final PreparedQueryStatement pqs = new PreparedQueryStatement();
        try {
            pqs.appendSelectColumn(SparcConstants.PRODUCT_OBJECT, SparcConstants.BRANCH_ITERATION);
            pqs.appendFromTable(LCSProduct.class);
            appendProductCriteria(pqs, businessObject);
            pqs.appendAndIfNeeded();
            pqs.appendCriteria(new Criteria(SparcConstants.PRODUCT_OBJECT, SparcConstants.LATEST_ITERATION_INFO, SparcConstants.ONE, Criteria.EQUALS));
            final SearchResults sr = LCSQuery.runDirectQuery(pqs);
            if (sr == null) {
                return new ArrayList<>();
            }
            final Collection<FlexObject> results = sr.getResults();
            if (results == null) {
                return new ArrayList<>();
            }
            return results.stream()
                    .map(fo -> fo.getString(SparcConstants.PRODUCT_OBJECT + "." + SparcConstants.BRANCH_ITERATION))
                    .map(id -> SparcQueryUtil.<LCSProduct>findObjectById("VR:com.lcs.wc.product.LCSProduct:" + id))
                    .filter(product -> product != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Encountered an error while querying for Products using the given businessObject, error:" + e);
            return new ArrayList<>();
        }
    }

    private void appendProductCriteria(final PreparedQueryStatement pqs, final LCSLifecycleManaged businessObject) throws Exception {
        final long boId = businessObject.getPersistInfo().getObjectIdentifier().getId();
        final String flexPath = businessObject.getFlexType().getFullName(true);
        if (flexPath.startsWith(SparcIntegrationConstants.HTS_BUSINESS_OBJECT_FLEX_PATH)) {
            final String htsColumn = getProductColumn(SparcIntegrationConstants.PRODUCT_HTS_CODE_INTERNAL_NAME);
            if (htsColumn == null) {
                throw new Exception("HTS Column is not found in Product with internal name:" + SparcIntegrationConstants.PRODUCT_HTS_CODE_INTERNAL_NAME);
            }
            pqs.appendAndIfNeeded();
            pqs.appendCriteria(new Criteria(SparcConstants.PRODUCT_OBJECT, htsColumn, boId + "", Criteria.EQUALS));
        } else if (flexPath.startsWith(SparcIntegrationConstants.SIZE_RANGE_BUSINESS_OBJECT_FLEX_PATH)) {
            final String sizeRangeColumn = getProductColumn(SparcIntegrationConstants.PRODUCT_SIZE_RANGE_INTERNAL_NAME);
            if (sizeRangeColumn == null) {
                throw new Exception("Size Range is not found in Product with internal name:" + SparcIntegrationConstants.PRODUCT_SIZE_RANGE_INTERNAL_NAME);
            }
            pqs.appendAndIfNeeded();
            pqs.appendCriteria(new Criteria(SparcConstants.PRODUCT_OBJECT, sizeRangeColumn, boId + "", Criteria.EQUALS));
        } else {
            throw new Exception("This plugin is only applied on Size Range and HTS Lookup Table Business Objects");
        }
    }

    private String getProductColumn(final String attr) {
        try {
            final FlexTypeAttribute flexTypeAttribute = FlexTypeCache.getFlexTypeFromPath(SparcIntegrationConstants.LUCKY_PRODUCT_FLEX_PATH).getAttribute(attr);
            if (flexTypeAttribute == null) {
                return null;
            }
            return flexTypeAttribute.getColumnName();
        } catch (Exception e) {
            return null;
        }
    }

}
