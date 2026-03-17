package com.sparc.wc.bomlink.filter;

import com.lcs.wc.flexbom.FlexBOMLink;

import java.util.Collection;
import java.util.List;

public interface BOMLinkFilter {
    /**
     *
     * @param links
     * @param sectionOrder
     * @return
     */
    List<FlexBOMLink> filter(Collection<FlexBOMLink> links,List<String> sectionOrder);
}
