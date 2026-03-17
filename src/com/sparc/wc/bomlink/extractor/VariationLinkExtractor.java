package com.sparc.wc.bomlink.extractor;

import com.lcs.wc.flexbom.FlexBOMLink;
import com.lcs.wc.util.FormatHelper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class VariationLinkExtractor {

    /**
     *
     * @param links
     * @return
     */
    public List<FlexBOMLink> extract(Collection<FlexBOMLink> links) {
        return links.stream()
                .filter(link -> {
                    String dimName = link.getDimensionName();
                    return FormatHelper.hasContent(dimName);
                }).collect(Collectors.toList());
    }
}
