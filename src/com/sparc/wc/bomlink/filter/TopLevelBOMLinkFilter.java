package com.sparc.wc.bomlink.filter;

import com.lcs.wc.flexbom.FlexBOMLink;
import com.lcs.wc.util.FormatHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TopLevelBOMLinkFilter implements BOMLinkFilter {


    /**
     *
     * @param links
     * @param sectionOrder
     * @return
     */
    @Override
    public List<FlexBOMLink> filter(Collection<FlexBOMLink> links,List<String> sectionOrder) {
        List<FlexBOMLink> filteredLinks = links.stream()
                .filter(link -> {
                    try {
                        return !FormatHelper.hasContent(link.getDimensionName());
                    } catch (Exception e) {
                        logError("Error checking dimension name", e);
                        return false;
                    }
                })
                .collect(Collectors.toList());

        // Process sections and collect matching links, while maintaining section order
        return sectionOrder.stream()
                .map(section -> {
                    // For each section, filter the matching links
                    try {
                        return filteredLinks.stream()
                                .filter(link -> {
                                    try {
                                        String sectionValue = (String)link.getValue("section");
                                        return section.equalsIgnoreCase(sectionValue);
                                    } catch (Exception e) {
                                        logError("Error getting section from link", e);
                                        return false;
                                    }
                                })
                                .collect(Collectors.toList());
                    } catch (Exception e) {
                        logError("Error processing section: " + section, e);
                        return new ArrayList<FlexBOMLink>(); // Return empty list in case of error
                    }
                })
                .flatMap(List::stream) // Flatten the list while maintaining order
                .collect(Collectors.toList()); // Collect the final result into a single list
    }

    private void logError(String message, Exception e) {
        // Centralized error logging (could be replaced with a logging framework)
        System.err.println(message + ": " + e.getMessage());
    }





}
