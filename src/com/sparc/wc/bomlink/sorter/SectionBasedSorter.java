package com.sparc.wc.bomlink.sorter;

import com.lcs.wc.flexbom.FlexBOMLink;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SectionBasedSorter implements BOMLinkSorter {


    /**
     *
     * @param links
     * @param sectionOrder
     * @return
     */
    @Override
    public List<FlexBOMLink> sort(List<FlexBOMLink> links,List<String> sectionOrder) {
        return links.stream().sorted(Comparator
                .comparing((FlexBOMLink link) -> {
                    try {
                        String section = (String)link.getValue("section");
                        return sectionOrder.indexOf(section);
                    } catch (Exception e) {
                        return Integer.MAX_VALUE;
                    }
                })
                .thenComparingInt(link -> {
                    try {
                        return (Integer) link.getSortingNumber();
                    } catch (Exception e) {
                        return Integer.MAX_VALUE;
                    }
                })).collect(Collectors.toList());
    }
}
