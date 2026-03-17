package com.galaxy.wc.bomlink.loader;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class BOMDataManager {

    private final Map<String, List<BomLinkData>> bomMasterData = new LinkedHashMap<>(); // preserves order by BOMMASTERID
    private final Map<String, Map<Integer, BomLinkData>> branchIdToTopLink = new LinkedHashMap<>();// preserves TopLink order

    public void clear() {
        bomMasterData.clear();
        branchIdToTopLink.clear();
    }

    public void process(List<BomLinkData> dataList) {
        // Group all BOMData by BOMMASTERID, preserving order using LinkedHashMap

        System.out.println("Start time----process-------------"+ LocalDateTime.now());
        Map<String, List<BomLinkData>> groupedByBomMaster = dataList.stream()
                .collect(Collectors.groupingBy(
                        BomLinkData::getBomMasterId,
                        LinkedHashMap::new, // maintain order
                        Collectors.toList()
                ));

        for (Map.Entry<String, List<BomLinkData>> entry : groupedByBomMaster.entrySet()) {
            List<BomLinkData> bomDataList = entry.getValue();

            // Local map for this BOMMASTERID to map branchID to TopLink
            Map<Integer, BomLinkData> localBranchMap = new LinkedHashMap<>();

            for (BomLinkData data : bomDataList) {

                if (data.getDimensionName().isEmpty()) {
                    // It's a TopLink for a specific BOMMASTERID and branchId
                    branchIdToTopLink
                            .computeIfAbsent(entry.getKey(), k -> new LinkedHashMap<>())  // Get or create the map for the BOMMASTERID
                            .putIfAbsent(data.getBranchId(), data); // Store TopLink by branchId within this BOMMASTERID
                    localBranchMap.put(data.getBranchId(), data);
                }
            }

            for (BomLinkData data : bomDataList) {
                if (isVariated(data)) {
                    BomLinkData topLink = localBranchMap.get(data.getBranchId());
                    if (topLink != null) {
                        topLink.addVariatedLink(data); // Order of addition matches file
                    }
                }
            }

            bomMasterData.put(entry.getKey(), bomDataList);
        }
        System.out.println("Start time----process------End-------"+ LocalDateTime.now());

    }


    private boolean isVariated(BomLinkData data) {
        String dim = data.getDimensionName();
        return dim.equals(":SKU") || dim.equals(":SOURCE") || dim.equals(":SOURCE:SKU");
    }

    public BomLinkData getTopLinkByBomMasterIdAndBranchId(String bomMasterId, int branchId) {
        Map<Integer, BomLinkData> branchMap = branchIdToTopLink.get(bomMasterId);
        if (branchMap == null) {
            return null; // No such BOMMASTERID
        }
        return branchMap.get(branchId); // Returns the TopLink for the specified branchId
    }

    public Map<String, List<BomLinkData>> getBomMasterData() {
        return bomMasterData;
    }

    public List<BomLinkData> getTopLinksByBomMasterId(String bomMasterId) {
        Map<Integer, BomLinkData> branchMap = branchIdToTopLink.get(bomMasterId);
        if (branchMap == null) {
            return Collections.emptyList(); // No such BOMMASTERID
        }
        return new ArrayList<>(branchMap.values()); // Returns all TopLinks for the specified BOMMASTERID
    }

    public List<BomLinkData> getTopLinksBySectionName(String sectionName) {
        List<BomLinkData> result = new ArrayList<>();

        for (Map<Integer, BomLinkData> branchMap : branchIdToTopLink.values()) {
            for (BomLinkData topLink : branchMap.values()) {
                if (sectionName.equals(topLink.getSection())) {
                    result.add(topLink);
                }
            }
        }
        return result;
    }

    /**
     *
     * @param bomMasterId
     * @param sectionName
     * @return
     */
    public List<BomLinkData> getTopLinksByBomMasterIdAndSectionName(String bomMasterId, String sectionName) {
        Map<Integer, BomLinkData> branchMap = branchIdToTopLink.get(bomMasterId);
        if (branchMap == null) {
            return Collections.emptyList();
        }
        return branchMap.values().stream()
                .filter(topLink -> sectionName.equals(topLink.getSection()))
                .collect(Collectors.toList());
    }

    /**
     *
     * @param sectionName
     * @param bomDatas
     * @return
     */
    public List<BomLinkData> getTopLinksBySectionName(String sectionName,Collection<BomLinkData> bomDatas){
        List<BomLinkData> result = new ArrayList<>();
        for(BomLinkData linkData: bomDatas){
            if (sectionName.equals(linkData.getSection())) {
                result.add(linkData);
            }
        }
        return result;
    }

}
