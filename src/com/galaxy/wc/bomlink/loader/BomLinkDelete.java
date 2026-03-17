package com.galaxy.wc.bomlink.loader;


import com.lcs.wc.flexbom.FlexBOMLink;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.LCSFlexBOMLogic;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import com.galaxy.wc.bom.constants.GalaxyMigrationConstants;
import com.galaxy.wc.bom.helper.GalaxyBomHelper;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.httpgw.GatewayAuthenticator;
import wt.pom.Transaction;
import wt.util.WTProperties;
import wt.vc.wip.WorkInProgressHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;


public class BomLinkDelete implements RemoteAccess {

    private static final Logger logger = Logger.getLogger(BomLinkLoader.class.getName());

    private static final List<String> successfulBomMasterIds = new ArrayList<>();
    private static final List<String> failedBomMasterIds = new ArrayList<>();


    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            RemoteMethodServer remoteMethodServer = RemoteMethodServer.getDefault();
            GatewayAuthenticator auth = new GatewayAuthenticator();
            auth.setRemoteUser("wcadmin");
            remoteMethodServer.setAuthenticator(auth);
			//windchill 
            remoteMethodServer.invoke("processDeleteBomLinkData", "com.galaxy.wc.bomlink.loader.BomLinkDelete", null,
                    new Class[]{String.class}, new Object[]{args[0]});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param data
     */
    public static void processDeleteBomLinkData(String data) {
        String baseDir = getBaseDirectory(data);
        File dir = new File(baseDir);
        String strBrand = "";

        String processedDir = baseDir + File.separator + "deletionProcess";  // New directory for processed files
        File processedDirectory = new File(processedDir);

        if (!processedDirectory.exists()) {
            processedDirectory.mkdirs(); // Create processed directory if it doesn't exist
        }

        File[] files = dir.listFiles((d, name) -> name.startsWith("BOMLinkExtract"));
        if (files == null || files.length == 0) return;

       
        List<String> sectionOrder = new ArrayList<>();
        if (data.contains("Apparel")) {
            sectionOrder = Arrays.asList("mainmaterials", "sctrims", "sclining", "scartwork", "scprocess", "labels", "packaging");
            strBrand = GalaxyMigrationConstants.RBK_APP_BRAND;
        } else {
            sectionOrder = Arrays.asList("scFwUpper", "scSockliner", "scInsole", "scBottom", "scsundries", "scPackaging");
            strBrand = GalaxyMigrationConstants.RBK_FTW_BRAND;
        }

        BOMFileReader reader = new BOMFileReader();
        BOMDataManager manager = new BOMDataManager();

        for (File file : files) {
            processSingleFile(file, reader, manager, sectionOrder, strBrand);
        }
        // After processing all files, write results
        writeResultsToFile(processedDir);
    }

    /**
     *
     * @param processedDir
     */
    private static void writeResultsToFile(String processedDir) {
        File successFile = new File(processedDir + File.separator + "success.txt");
        File failureFile = new File(processedDir + File.separator + "failure.txt");

        try (BufferedWriter successWriter = new BufferedWriter(new FileWriter(successFile));
             BufferedWriter failureWriter = new BufferedWriter(new FileWriter(failureFile))) {

            for (String id : successfulBomMasterIds) {
                successWriter.write(id);
                successWriter.newLine();
            }
            for (String id : failedBomMasterIds) {
                failureWriter.write(id);
                failureWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param path
     * @return
     */
    private static String getBaseDirectory(String path) {
        String wt_home = "";
        try {
            WTProperties wtProperties = WTProperties.getLocalProperties();
            wt_home = wtProperties.getProperty("wt.home");
            wt_home = wt_home + File.separator + "codebase" + File.separator + "BOMLinkExtract" +
                    File.separator + path + File.separator;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wt_home;
    }



    /**
     * @param file
     * @param reader
     * @param manager
     * @param sectionAppOrder
     * @param strBrand
     */
    private static void processSingleFile(File file, BOMFileReader reader, BOMDataManager manager, List<String> sectionAppOrder, String strBrand) {
        try {
			System.out.println("prcoessSingleFile--------------------Start");			
           
			manager.clear(); // Clear all internal state
            List<BomLinkData> data = reader.readFile(file);
            manager.process(data);
            List<BomLinkData> secTopLinkData = new ArrayList<>();
            //FlexBOMPart objBOMPart = null;
            String strProdId = GalaxyMigrationConstants.EMPTY_SPACE;
            String strBOMName = GalaxyMigrationConstants.EMPTY_SPACE;
            StringBuilder branchIdsUpdated = new StringBuilder();
            Boolean isVariatedLink = false;
            Boolean isDeleted = false;


            List<FlexBOMLink> topLevelLinks = new ArrayList<>();
            List<FlexBOMLink> sortedLinks = new ArrayList<>();
            List<FlexBOMLink> varLinks = new ArrayList<>();
            List<String> bomMasterIds = new ArrayList<>();

            FlexBOMPart objBOMPart = null;


            //Start of the BomLinks Delete
            logger.info("Start of the BOM Link Delete");

            int count = 0;

            for (String strBOMMasterID : manager.getBomMasterData().keySet()) {
				
				System.out.println("strBOMMasterID----------------------"+strBOMMasterID);
                isDeleted = false;
                Transaction trdelete = null;
                try {
                    trdelete = new Transaction();
                    trdelete.start();

                    objBOMPart = GalaxyBomHelper.getBOMPart(strBOMMasterID);
                    if (FormatHelper.equalsWithNull(objBOMPart, null)) {
                        logger.info("There is no bom exists with the BOM Master Id  " + strBOMMasterID);
                        continue;
                    }

                    if (!VersionHelper.isCheckedOut(objBOMPart)) {
                        objBOMPart = (FlexBOMPart) WorkInProgressHelper.service
                                .checkout(objBOMPart, WorkInProgressHelper.service.getCheckoutFolder(), "")
                                .getWorkingCopy();
                    } else {
                        objBOMPart = VersionHelper.getWorkingCopy(objBOMPart);
                    }

                    Collection<FlexBOMLink> allLinks = LCSFlexBOMQuery.findFlexBOMLinks(
                            objBOMPart, null, null, null, null, null, null, null, true,
                            LCSFlexBOMQuery.ALL_DIMENSIONS, "", "", ""
                    );

                    if(allLinks.isEmpty()){
                        logger.info("No bomlinks associated with the BOM-------------"+strBOMMasterID);
                        continue;
                    }

                    topLevelLinks = filter(allLinks, sectionAppOrder);
                    sortedLinks = sort(topLevelLinks, sectionAppOrder);
                    varLinks = extract(allLinks);

                    //Start of variation link removal
                    if (!varLinks.isEmpty()) {
                        for (FlexBOMLink objVarLink : varLinks) {
                            new LCSFlexBOMLogic().delete(objVarLink);
                            count = count + 1;
                        }
                    }
                    //End of variation link removal

                    if (!sortedLinks.isEmpty()) {
                        for (FlexBOMLink bomLink : sortedLinks) {
                            new LCSFlexBOMLogic().delete(bomLink);
                            count = count + 1;
                        }
                        isDeleted= true;
                    }


                    if (isDeleted) {
                        objBOMPart = (FlexBOMPart) LCSFlexBOMLogic.persist(objBOMPart, true);
                        if (VersionHelper.isCheckedOut(objBOMPart)) {
                            objBOMPart = (FlexBOMPart) WorkInProgressHelper.service.checkin(objBOMPart, null);
                            logger.info("BOM Links successfully deleted for the BOM--------" + objBOMPart.getName());
                            successfulBomMasterIds.add(strBOMMasterID);
                        }
                    } else {
                        try {
                            objBOMPart = (FlexBOMPart) WorkInProgressHelper.service.undoCheckout(objBOMPart);
                            failedBomMasterIds.add(strBOMMasterID);
                        } catch (Exception ex) {
                            logger.severe("Exception while doing the undocheckout--------" + ex.getMessage());
                            failedBomMasterIds.add(strBOMMasterID);
                        }
                    }
                    trdelete.commit();
                    trdelete = null;

                } catch (Exception ex) {
                    logger.severe("Exception while processing the BOM----"+strBOMMasterID+" "+ex.getMessage());
                    failedBomMasterIds.add(strBOMMasterID);
                } finally {
                    if (trdelete != null) {
                        trdelete.rollback();
                    }
                }
                try {
                    if (!FormatHelper.equalsWithNull(objBOMPart, null)) {
                        if (VersionHelper.isCheckedOut(objBOMPart)) {
                            VersionHelper.undoCheckout(objBOMPart);
                        }
                    }
                }catch(Exception ex){
                    logger.severe("Delete Exception while doing the undocheckout------------------"+ex.getMessage());
                }
            }
            data.clear();
            System.out.println("Total No of Bom Links deleted---"+count);
        } catch (Exception e) {
            System.err.println("Error processing file: " + file.getName());
            e.printStackTrace();
        } finally {
            FlexTypeCache.refreshCache();
        }

    }

    /**
     * @param file
     * @return
     */
    private static long extractTimestamp(File file) {
        String name = file.getName();
        try {
            String timestampStr = name.substring(name.lastIndexOf('_') + 1);
            return Long.parseLong(timestampStr.replaceAll("[^\\d]", ""));
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * @param links
     * @param sectionOrder
     * @return
     */
    public static List<FlexBOMLink> filter(Collection<FlexBOMLink> links, List<String> sectionOrder) {
        List<FlexBOMLink> filteredLinks = links.stream()
                .filter(link -> {
                    try {
                        return !FormatHelper.hasContent(link.getDimensionName());
                    } catch (Exception e) {
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
                                        String sectionValue = (String) link.getValue("section");
                                        return section.equalsIgnoreCase(sectionValue);
                                    } catch (Exception e) {
                                        return false;
                                    }
                                })
                                .collect(Collectors.toList());
                    } catch (Exception e) {
                        return new ArrayList<FlexBOMLink>(); // Return empty list in case of error
                    }
                })
                .flatMap(List::stream) // Flatten the list while maintaining order
                .collect(Collectors.toList()); // Collect the final result into a single list
    }

    /**
     * @param links
     * @param sectionOrder
     * @return
     */
    public static List<FlexBOMLink> sort(List<FlexBOMLink> links, List<String> sectionOrder) {
        return links.stream().sorted(Comparator
                .comparing((FlexBOMLink link) -> {
                    try {
                        String section = (String) link.getValue("section");
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

    /**
     * @param links
     * @return
     */
    public static List<FlexBOMLink> extract(Collection<FlexBOMLink> links) {
        return links.stream()
                .filter(link -> {
                    String dimName = link.getDimensionName();
                    return FormatHelper.hasContent(dimName);
                }).collect(Collectors.toList());
    }

}
