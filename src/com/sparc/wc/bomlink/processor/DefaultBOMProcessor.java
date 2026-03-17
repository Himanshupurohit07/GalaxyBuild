package com.sparc.wc.bomlink.processor;

import com.lcs.wc.flexbom.FlexBOMLink;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.bomlink.extractor.BomDataExtractorFactory;
import com.sparc.wc.bomlink.extractor.BomLinkDataExtractor;
import com.sparc.wc.bomlink.extractor.VariationLinkExtractor;
import com.sparc.wc.bomlink.filter.BOMLinkFilter;
import com.sparc.wc.bomlink.filter.TopLevelBOMLinkFilter;
import com.sparc.wc.bomlink.sorter.BOMLinkSorter;
import com.sparc.wc.bomlink.sorter.SectionBasedSorter;
import com.sparc.wc.bomlink.util.FileWriterUtility;
import wt.locks.LockHelper;
import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WTProperties;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.sparc.wc.bom.constants.SparcMigrationConstants.*;


/**
 *
 */
public class DefaultBOMProcessor implements BOMProcessor {

    private final BOMLinkFilter filter = new TopLevelBOMLinkFilter();
    private final BOMLinkSorter sorter = new SectionBasedSorter();
    private final VariationLinkExtractor variationExtractor = new VariationLinkExtractor();

    private final FileWriterUtility fileWriter;

    private final Boolean isAppOrFtw;

    private static String wt_home = "";
    private static String baseDir = "";

    static {
        try {
            WTProperties wtProperties = WTProperties.getLocalProperties();
            wt_home = wtProperties.getProperty("wt.home");
            baseDir = wt_home + File.separator + "codebase" + File.separator + "BOMLinkExtract" + File.separator;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public DefaultBOMProcessor(String outputDirectory,String subDirectory,Boolean isAppOrFtw) {
        this.fileWriter = new FileWriterUtility(outputDirectory,subDirectory);
        this.isAppOrFtw = isAppOrFtw;
    }

    /**
     *
     * @param bomParts
     * @param sectionOrder
     */
    @Override
    public void process(Set<FlexBOMPart> bomParts,List<String> sectionOrder) {
        List<FlexBOMLink> topLevelLinks = new ArrayList<>();
        List<FlexBOMLink> sortedLinks = new ArrayList<>();
        List<FlexBOMLink> variatedLinks = new ArrayList<>();
        List<String> sortedVariatedRows = new ArrayList<>();

        int count =0;
        String strProductNo = "";
        String strBomPartName = "";
        Set<String> checkOutBoms = new HashSet<>();

            for (FlexBOMPart bomPart : bomParts) {
                try {
                    strProductNo = "";
                    strBomPartName = "";
                    LCSProduct objProduct = (LCSProduct) VersionHelper.latestIterationOf((LCSPartMaster) bomPart.getOwnerMaster());
                    strProductNo = String.valueOf((Long) objProduct.getValue(PLM_PRODUCT_NO));
                    strBomPartName = (String) bomPart.getValue(BOM_PART_NAME);
                    if (VersionHelper.isCheckedOut(bomPart)) {
                        checkOutBoms.add(strProductNo + "~" + strBomPartName + "~" + bomPart.getLockerEMail() + "~" + bomPart.getLockerFullName());
                        //continue;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                topLevelLinks.clear();
                sortedLinks.clear();
                variatedLinks.clear();
                sortedVariatedRows.clear();

                try {
                    Collection<FlexBOMLink> allLinks = LCSFlexBOMQuery.findFlexBOMLinks(
                            bomPart, null, null, null, null, null, null, null, false,
                            LCSFlexBOMQuery.ALL_DIMENSIONS, "", "", ""
                    );

                if(allLinks.isEmpty()){
                    System.out.println("Bom Links are empty----------");
                    continue;
                }

                allLinks = allLinks.stream()
                        .filter(link -> !link.isDropped())
                        .collect(Collectors.toList());

               String bomMasterId =FormatHelper.getNumericObjectIdFromObject(bomPart.getMaster());

                topLevelLinks = filter.filter(allLinks,sectionOrder);
                sortedLinks = sorter.sort(topLevelLinks,sectionOrder);
                variatedLinks = variationExtractor.extract(allLinks);

                    if (!variatedLinks.isEmpty()) {
                        sortedVariatedRows = getCorrespondingVariatedRows(sortedLinks, variatedLinks, bomMasterId, isAppOrFtw);
                    } else {
                        sortedVariatedRows = extractTopBomData(sortedLinks, bomMasterId, isAppOrFtw);
                    }

                    String bomPartHeader = strProductNo + SPECIAL_DELIMITER + strBomPartName;
                    fileWriter.writeBomPartRows(bomPartHeader, sortedVariatedRows);
                    count = count + sortedVariatedRows.size();
                    fileWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        String summarReport = "";

        if(isAppOrFtw)  {
            summarReport = baseDir + "Footwear" + File.separator+"Summary_Report_FootWear.txt";
        }
        else{
            summarReport = baseDir + "Apparel" + File.separator + "Summary_Report_Apparel.txt";
        }

        try (BufferedWriter summaryWriter = new BufferedWriter(new FileWriter(summarReport, true))) {
            summaryWriter.write("Checked Out BOMs:\n");
            for (String checkedOutBom : checkOutBoms) {
                summaryWriter.write(checkedOutBom + "\n");
            }
            // Write the BOMLink count to the Summary Report
            summaryWriter.write("\nTotal BOM Links Processed: " + count + "\n");

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    /**
     *
     * @param sortedLinks
     * @param variatedLinks
     * @return
     */
    public static List<String> getCorrespondingVariatedRows(List<FlexBOMLink> sortedLinks, List<FlexBOMLink> variatedLinks,String strBomPartId,Boolean isAppOrFtw) {
        // Pre-group variatedLinks by branchId for faster access
        List<String> resultRows = new ArrayList<>();
        Map<Integer, List<FlexBOMLink>> variatedLinksByBranchId = new HashMap<>();
        for (FlexBOMLink link : variatedLinks) {
            Integer branchId = (Integer) link.getBranchId();
            variatedLinksByBranchId.computeIfAbsent(branchId, k -> new ArrayList<>()).add(link);
        }
        String strSection = EMPTY_SPACE;

        // Process each topLink and its associated variatedLinks

        for (FlexBOMLink topLink : sortedLinks) {
            Integer branchId = (Integer) topLink.getBranchId();
            BomLinkDataExtractor extractor = BomDataExtractorFactory.getExtractor(topLink);
            try {
                strSection =(String)topLink.getValue(SECTION);
            } catch (WTException e) {
               e.printStackTrace();
            }

            resultRows.add(extractor.extractData(topLink,strSection,strBomPartId,isAppOrFtw));  // Add top-level link data

            // Get all variated links for this branchId
            List<FlexBOMLink> relatedLinks = variatedLinksByBranchId.getOrDefault(branchId, Collections.emptyList());

            // Categorize them by dimensionName
            List<FlexBOMLink> skuRows = new ArrayList<>();
            List<FlexBOMLink> sourceRows = new ArrayList<>();
            List<FlexBOMLink> sourceSkuRows = new ArrayList<>();

            for (FlexBOMLink link : relatedLinks) {
                String dimensionName = link.getDimensionName();
                if (SKU_VARIATION.equals(dimensionName)) {
                    skuRows.add(link);
                } else if (SOURCE_VARIATION.equals(dimensionName)) {
                    sourceRows.add(link);
                } else if (SOURCE_SKU_VARIATION.equals(dimensionName)) {
                    sourceSkuRows.add(link);
                }
            }
            // Add the rows immediately after the topLink, in the specified order
            resultRows.addAll(extractVariatedBomData(skuRows,strSection,strBomPartId,isAppOrFtw));        // First :SKU rows
            resultRows.addAll(extractVariatedBomData(sourceRows,strSection,strBomPartId,isAppOrFtw));     // Then :SOURCE rows
            resultRows.addAll(extractVariatedBomData(sourceSkuRows,strSection,strBomPartId,isAppOrFtw));
        }
        return resultRows;
    }


    /**
     *
     * @param links
     * @return
     */
    private static List<String> extractVariatedBomData(List<FlexBOMLink> links,String section,String strBomPartId,Boolean isAppOrFtw) {
        List<String> bomDataList = new ArrayList<>();
        for (FlexBOMLink link : links) {
            BomLinkDataExtractor extractor = BomDataExtractorFactory.getExtractor(link);
            bomDataList.add(extractor.extractData(link,section,strBomPartId,isAppOrFtw));
        }
        return bomDataList;
    }

    /**
     *
     * @param links
     * @return
     */
    private static List<String> extractTopBomData(List<FlexBOMLink> links,String strBomPartId,Boolean isAppOrFtw) {
        List<String> bomDataList = new ArrayList<>();
        int targetBranchId = 0;
        for (FlexBOMLink link : links) {
            BomLinkDataExtractor extractor = BomDataExtractorFactory.getExtractor(link);
            try {
                targetBranchId = targetBranchId + 1;
                bomDataList.add(extractor.extractData(link,(String)link.getValue(SECTION),strBomPartId,isAppOrFtw));

            } catch (WTException e) {
              e.printStackTrace();
            }
        }
        return bomDataList;
    }
}
