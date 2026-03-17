package com.sparc.wc.bomlink.processor;

import com.lcs.wc.flexbom.FlexBOMPart;

import java.util.List;
import java.util.Set;

public interface BOMProcessor {

    /**
     *
     * @param bomParts
     * @param sectionOrder
     */
    void process(Set<FlexBOMPart> bomParts, List<String> sectionOrder);
}
