package com.sparc.wc.bomlink.extractor;

import com.lcs.wc.flexbom.FlexBOMLink;

public interface BomLinkDataExtractor {
    /**
     *
     * @param row
     * @param section
     * @param strBomPartId
     * @return
     */
    String extractData(FlexBOMLink row,String section,String strBomPartId,Boolean isAppOrFtw);
}
