package com.sparc.wc.bomlink.extractor;

import com.lcs.wc.flexbom.FlexBOMLink;
import com.sparc.wc.bom.constants.SparcMigrationConstants;

import java.util.Optional;

public class BomDataExtractorFactory {


    private static final DefaultBomLinkDataExtractor DEFAULT_EXTRACTOR = new DefaultBomLinkDataExtractor();
    private static final SkuBomDataExtractor SKU_EXTRACTOR = new SkuBomDataExtractor();
    private static final SourceBomDataExtractor SOURCE_EXTRACTOR = new SourceBomDataExtractor();
    private static final SourceSkuBomDataExtractor SOURCE_SKU_EXTRACTOR = new SourceSkuBomDataExtractor();


    /**
     *
     * @param row
     * @return
     */
    public static BomLinkDataExtractor getExtractor(FlexBOMLink row) {
        String dimensionName = Optional.ofNullable(row.getDimensionName()).orElse("");
        switch (dimensionName) {
            case SparcMigrationConstants.SKU_VARIATION:
                return SKU_EXTRACTOR;
            case SparcMigrationConstants.SOURCE_VARIATION:
                return SOURCE_EXTRACTOR;
            case SparcMigrationConstants.SOURCE_SKU_VARIATION:
                return SOURCE_SKU_EXTRACTOR;
            default:
                return DEFAULT_EXTRACTOR;
        }
    }

}

