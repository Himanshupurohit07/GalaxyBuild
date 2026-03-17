package com.sparc.wc.bomlink.sorter;

import com.lcs.wc.flexbom.FlexBOMLink;

import java.util.List;

public interface BOMLinkSorter {

    /**
     *
     * @param links
     * @param sectionOrder
     * @return
     */
    List<FlexBOMLink> sort(List<FlexBOMLink> links,List<String> sectionOrder);
}
