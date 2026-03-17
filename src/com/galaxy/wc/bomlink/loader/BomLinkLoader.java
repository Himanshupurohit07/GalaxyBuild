package com.galaxy.wc.bomlink.loader;

import com.lcs.wc.color.LCSColor;
import com.lcs.wc.flexbom.FlexBOMLink;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.LCSFlexBOMLogic;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.material.*;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.supplier.LCSSupplierQuery;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import com.galaxy.wc.bom.constants.GalaxyMigrationConstants;
import com.galaxy.wc.bom.helper.GalaxyBomHelper;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.httpgw.GatewayAuthenticator;
import wt.pom.Transaction;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.vc.wip.WorkInProgressHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import static com.galaxy.wc.bom.constants.GalaxyMigrationConstants.TILDE_DELIMITER;


public class BomLinkLoader implements RemoteAccess {

    private static final Logger cusLogger = Logger.getLogger(BomLinkLoader.class.getName());

    private static Set<String> missingColors = new HashSet<>();
    private static Set<String> missingMaterials = new HashSet<>();
    private static Set<String> missingSuppliers = new HashSet<>();
    private static Set<String> missingMaterialSuppliers = new HashSet<>();
    private static Set<String> missingMaterialColors = new HashSet<>();
    private static Set<String> missingColorways = new HashSet<>();
    private static Set<String> missingSourceIds = new HashSet<>();

    private static Set<String> onlyTopLinksSuccessList = new HashSet<>();
    private static Set<String> bothTopandVarSuccessList = new HashSet<>();
    private static Set<String> failureTopLinkList = new HashSet<>();
    private static Set<String> failureVarLinkList = new HashSet<>();

    private static List<String> failureTopLinks = new ArrayList<>();
    private static List<String> failureVariatedLinks = new ArrayList<>();

    private static int topLinkCount = 0;
    private static int varLinkCount = 0;
    private static int totalCount = 0;

    private static int topFailedCount = 0;
    private static int varFailedCount = 0;
    private static int totalVarCount = 0;


    private static String wt_home = "";
    private static String baseDir = "";

    static {
        try {
            WTProperties wtProperties = WTProperties.getLocalProperties();
            wt_home = wtProperties.getProperty("wt.home");
            baseDir = wt_home + File.separator + "codebase" + File.separator + "BOMLinkExtract" + File.separator + "Missing";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            RemoteMethodServer remoteMethodServer = RemoteMethodServer.getDefault();
            GatewayAuthenticator auth = new GatewayAuthenticator();
            auth.setRemoteUser("wcadmin");
            remoteMethodServer.setAuthenticator(auth);

            remoteMethodServer.invoke("processBomLinkData", "com.galaxy.wc.bomlink.loader.BomLinkLoader", null,
                    new Class[]{String.class,String.class}, new Object[]{args[0],args[1],args[2]});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param data
     * @param deltaLoader
     */
    public static void processBomLinkData(String data,String deltaLoader,String skipTopLink) {

        Boolean isDelta = Boolean.parseBoolean(deltaLoader);
		
		Boolean isSkipTopLink = Boolean.parseBoolean(skipTopLink);

        String baseDir = getBaseDirectory(data);
        File dir = new File(baseDir);

        String strBrand = "";

        List<String> deletedBOMMasters= new ArrayList<>();

        if(isDelta){
            deletedBOMMasters =readLinesFromFullPath(baseDir+File.separator+"deletionProcess"+File.separator+"failure.txt");
        }

        String processedDir = baseDir + File.separator + "processed";  // New directory for processed files
        File processedDirectory = new File(processedDir);
        if (!processedDirectory.exists()) {
            processedDirectory.mkdirs(); // Create processed directory if it doesn't exist
        }

        File[] files = dir.listFiles((d, name) -> name.startsWith("BOMLinkExtract"));
        if (files == null || files.length == 0) return;

        Arrays.sort(files, Comparator.comparingLong(BomLinkLoader::extractTimestamp));

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

        missingColorways.clear();
        missingMaterials.clear();
        missingSourceIds.clear();
        missingColors.clear();
        missingSuppliers.clear();
        missingMaterialColors.clear();
        missingMaterialSuppliers.clear();
        failureTopLinks.clear();
        failureVariatedLinks.clear();

        onlyTopLinksSuccessList.clear();
        bothTopandVarSuccessList.clear();
        failureTopLinkList.clear();
        failureVarLinkList.clear();

        topLinkCount =0;
        varLinkCount =0;
        totalCount =0;
        totalVarCount = 0;

        topFailedCount =0;
        varFailedCount = 0;


        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        String startTime = currentTime.format(formatter);

        for (File file : files) {
            Logger fileLogger = setupLogger(file);
            if (fileLogger == null) continue;
            processSingleFile(file, reader, manager, sectionOrder, strBrand, fileLogger,isDelta,deletedBOMMasters,isSkipTopLink);
            moveProcessedFile(file, processedDirectory);

            for (Handler handler : fileLogger.getHandlers()) {
                handler.flush();
                handler.close();
            }
        }
        currentTime = LocalTime.now();
        formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        String endTime = currentTime.format(formatter);
        writeMissingDataToFile(startTime,endTime,data);
    }


    /**
     *
     * @param fullPath
     * @return
     */
    public static List<String> readLinesFromFullPath(String fullPath) {
        Path filePath = Paths.get(fullPath);

        // Check if file exists and is readable
        if (!Files.exists(filePath)) {
            System.err.println("Error: File does not exist - " + fullPath);
            return List.of();
        }

        if (!Files.isRegularFile(filePath)) {
            System.err.println("Error: Path is not a regular file - " + fullPath);
            return List.of();
        }

        if (!Files.isReadable(filePath)) {
            System.err.println("Error: File is not readable - " + fullPath);
            return List.of();
        }

        try {
            List<String> collect = Files.lines(filePath)
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());
            return collect;

        } catch (IOException e) {
            System.err.println("Error reading file: " + fullPath);
            e.printStackTrace();
            return List.of(); // Return empty list if an I/O error occurs
        }
    }


    private static Logger setupLogger(File file) {
        try {
            File parentDir = file.getParentFile();
            File logsDir = new File(parentDir, "logs");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            File logFile = new File(logsDir, file.getName() + "_log.txt");

            Logger fileLogger = Logger.getLogger("Logger_" + file.getAbsolutePath());
            fileLogger.setUseParentHandlers(false);

            for (Handler handler : fileLogger.getHandlers()) {
                fileLogger.removeHandler(handler);
                handler.close();
            }

            FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath(), true);
            // Use custom formatter
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return String.format("[%1$tF %1$tT] [%2$-7s] %3$s%n",
                            record.getMillis(),
                            record.getLevel().getLocalizedName(),
                            record.getMessage());
                }
            });

            // fileHandler.setFormatter(new SimpleFormatter());
            fileLogger.addHandler(fileHandler);
            fileLogger.setLevel(Level.ALL);

            fileLogger.info("Logger initialized for file: " + file.getName());

            return fileLogger;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to set up logger: " + e.getMessage());
            return null;
        }
    }


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

    private static void moveProcessedFile(File file, File processedDirectory) {
        try {
            Path source = file.toPath();
            Path destination = processedDirectory.toPath().resolve(file.getName());

            // Move the file
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            cusLogger.info("File moved to: " + destination.toString());
        } catch (IOException e) {
            cusLogger.severe("Error moving file: " + file.getName() + " " + e.getMessage());
        }
    }

    /**
     * @param file
     * @param reader
     * @param manager
     * @param sectionAppOrder
     * @param strBrand
     * @param logger
     */
    private static void processSingleFile(File file, BOMFileReader reader, BOMDataManager manager, List<String> sectionAppOrder, String strBrand, Logger logger, Boolean deltaLoader,List<String> failureList,Boolean isSkipTopLink) {
        try {

            int successfulTopAndVariatedCount = 0;
            int topLinksUpdatedWithVariatedExceptionCount = 0;
            int onlyTopLinksPresentCount = 0; // New counter for only TopLinks

            manager.clear(); // Clear all internal state
            List<BomLinkData> data = reader.readFile(file);
            manager.process(data);

            List<BomLinkData> secTopLinkData = new ArrayList<>();
            FlexBOMPart objBOMPart = null;

            String strProdId = GalaxyMigrationConstants.EMPTY_SPACE;
            String strBOMName = GalaxyMigrationConstants.EMPTY_SPACE;
            StringBuilder branchIdsUpdated = new StringBuilder();

            Boolean isVariatedLink = true;

            //Start of the Bom Links creation.
            for (String bomMasterId : manager.getBomMasterData().keySet()) {

                //This code is for delta loader.
                if(deltaLoader){
                    if(failureList.contains(bomMasterId)){
                        logger.info("validation---BomLinks not deleted for this BOM Part----"+bomMasterId+"so recreating the bomlinks skipped");
                        continue;
                    }
                }
				
				 int topCount = 0;
                 int varCount = 0;
                 int exceptionTopCount = 0;
                 int exceptionVarCount = 0;

                Transaction trTop = null;
                Transaction trVar = null;
                try {
                    isVariatedLink = false;
                    objBOMPart = GalaxyBomHelper.getBOMPart(bomMasterId);
                    if (FormatHelper.equalsWithNull(objBOMPart, null)) {
                        logger.info("There is no bom exists with the BOM Master Id-" + bomMasterId);
                        continue;
                    }

					if(isSkipTopLink){
                    //Start of creating the top links
                    trTop = new Transaction();
                    trTop.start();
                    if (!VersionHelper.isCheckedOut(objBOMPart)) {
                        objBOMPart = (FlexBOMPart) WorkInProgressHelper.service
                                .checkout(objBOMPart, WorkInProgressHelper.service.getCheckoutFolder(), "")
                                .getWorkingCopy();
                    } else {
                        objBOMPart = VersionHelper.getWorkingCopy(objBOMPart);
                    }

                    Boolean allToplinks = true;
                    //Start of the Top Link Data and saving the objects.
                    topCount = 0;
                    varCount = 0;
                    exceptionTopCount = 0;
                    exceptionVarCount = 0;

                    for (String secName : sectionAppOrder) {
                        secTopLinkData = manager.getTopLinksByBomMasterIdAndSectionName(bomMasterId, secName);
                        int topLinkSortNumber = 0;
                        for (BomLinkData bomData : secTopLinkData) {
                            strBOMName = bomData.getBomName();
                            strProdId = bomData.getProdID();
                            topLinkSortNumber = topLinkSortNumber + 1;
                            try {
                                totalCount = totalCount +1;
                                processTopLinkData(objBOMPart, bomData, topLinkSortNumber, strBrand,file.getName());
                                topLinkCount = topLinkCount +1;
                                topCount = topCount +1;
                            }catch(Exception ex){
                                logger.severe(ex.getMessage());
                                allToplinks = false;
                                topFailedCount =topFailedCount + 1;
                                exceptionTopCount = exceptionTopCount + 1;
                            }
                            if (branchIdsUpdated.length() > 0) {
                                branchIdsUpdated.append(", ");
                            }
                            branchIdsUpdated.append(bomData.getBranchId());
                        }
                    }

                    //20 - Top Links
                    //15- success
                    //5 - Failures

                    //topLinkCount = 15
                    //totalCount= 20
                    //topFailedCount = 5
                    // topCOunt =15

                    objBOMPart = (FlexBOMPart) LCSFlexBOMLogic.persist(objBOMPart, true);
                    if (VersionHelper.isCheckedOut(objBOMPart)) {
                        objBOMPart = (FlexBOMPart) WorkInProgressHelper.service.checkin(objBOMPart, null);
                    }
                    if(!allToplinks){
                        totalCount = totalCount-topCount-exceptionTopCount;
                        topLinkCount = topLinkCount - topCount;
                        topFailedCount = topFailedCount + topCount;
                        failureTopLinkList.add(GalaxyMigrationConstants.BOM_STR+strBOMName+GalaxyMigrationConstants.PROD_STR+strProdId+
                                GalaxyMigrationConstants.FILE_STR+GalaxyMigrationConstants.VAR_NOT_STR+file.getName());
                        throw new Exception("No Top links created for the BOM");
                    }
                    trTop.commit();
                    trTop = null;
                    //End of the Top Link Data and Saving the Objects.
                    onlyTopLinksSuccessList.add(GalaxyMigrationConstants.BOM_CREATE_STR+strBOMName+
                            GalaxyMigrationConstants.PROD_STR+strProdId+GalaxyMigrationConstants.VAR_PROCESS+
                            file.getName());

                    if (branchIdsUpdated.length() > 0) {
                        logger.info("Top BomLinks with BranchIds updated successfully-" + branchIdsUpdated.toString() + "-for the BOM-" + bomMasterId + "-and the BOM Name is-" + strBOMName + "-and the product is" +
                                "-" + strProdId);
                    }
				}
                    branchIdsUpdated.setLength(0);

                    boolean hasNoVariatedLinks = true; // Renamed for clarity
                    List<BomLinkData> topLinks = manager.getTopLinksByBomMasterId(bomMasterId);

// Check if there are any variated links
                    for (BomLinkData topLinkData : topLinks) {
                        if (topLinkData != null && topLinkData.getVariatedLinks() != null && !topLinkData.getVariatedLinks().isEmpty()) {
                            hasNoVariatedLinks = false;
                            break; // Exit early as we found variated links
                        }
                    }

// Log and count if no variated links exist
                    if (hasNoVariatedLinks) {
                        logger.info("No Variated links found for BOM with Master ID-" + bomMasterId + "-and the BOMName-"+strBOMName+"-and Product ID: " + strProdId);
                        onlyTopLinksPresentCount++; // Increment count for only top links
                        continue; // Skip to the next BOM if only top links are found
                    }

                    trVar = new Transaction();
                    trVar.start();

                    if (!VersionHelper.isCheckedOut(objBOMPart)) {
                        objBOMPart = (FlexBOMPart) WorkInProgressHelper.service
                                .checkout(objBOMPart, WorkInProgressHelper.service.getCheckoutFolder(), "")
                                .getWorkingCopy();
                    } else {
                        objBOMPart = VersionHelper.getWorkingCopy(objBOMPart);
                    }
                    //Start of the Variated Links
                    isVariatedLink =true;
                    for (String secName : sectionAppOrder) {
                        secTopLinkData = manager.getTopLinksByBomMasterIdAndSectionName(bomMasterId, secName);
                        for (BomLinkData topLinkData : secTopLinkData) {
                            // Check if variated links exist before iterating
                            List<BomLinkData> variatedLinks = topLinkData.getVariatedLinks();
                            if (variatedLinks == null || variatedLinks.isEmpty()) {
                                // System.out.println("No variated links found for TopLinkData-" + topLinkData);
                                continue; // skip to next topLinkData
                            }
                            for (BomLinkData varLinkData : topLinkData.getVariatedLinks()) {
                                try {
                                    totalVarCount = totalVarCount + 1;
                                    processVariatedLinkData(objBOMPart, varLinkData, strBrand,file.getName());
									// System.out.println("Var link data after processing");
                                    varLinkCount = varLinkCount + 1;
                                    varCount = varCount + 1;
                                } catch (Exception ex) {
                                    logger.severe(ex.getMessage());
                                    isVariatedLink = false;
                                    varFailedCount = varFailedCount +1;
                                    exceptionVarCount = exceptionVarCount +1;
                                }
                            }
                        }
                    }

                    // if (isVariatedLink) {
                    objBOMPart = (FlexBOMPart) LCSFlexBOMLogic.persist(objBOMPart, true);
                    if (VersionHelper.isCheckedOut(objBOMPart)) {
                        objBOMPart = (FlexBOMPart) WorkInProgressHelper.service.checkin(objBOMPart, null);
                        successfulTopAndVariatedCount++;
                    }
                    //}
                    if(!isVariatedLink){
                        topLinksUpdatedWithVariatedExceptionCount++;
                        totalVarCount = totalVarCount -varCount-exceptionVarCount;
                        varLinkCount = varLinkCount - varCount;
                        varFailedCount = varFailedCount + varCount;
                        successfulTopAndVariatedCount--;
                        failureVarLinkList.add(GalaxyMigrationConstants.BOM_VAR_NOT_STR+strBOMName
                                +GalaxyMigrationConstants.PROD_STR+strProdId+GalaxyMigrationConstants.FILE_STR);
                        throw new Exception("No VAR links created for the BOM");
                    }
                    logger.info("Variated Links updated successfully for the BOM-" + bomMasterId + "-and the BOM name is-" + strBOMName + "-and the product is-" + strProdId);

                    //End of the variated Links
                    trVar.commit();
                    trVar = null;
                    bothTopandVarSuccessList.add(GalaxyMigrationConstants.BOM_VAR_CREATE_STR
                            +strBOMName+GalaxyMigrationConstants.PROD_STR+strProdId+GalaxyMigrationConstants.FILE_STR);
                    logger.info("Both Top and Variated links created successfully for the BOM-"+strBOMName+"-and the BOM Master ID-"+bomMasterId+"-and the ProductId-"+strProdId);
                } catch (Exception ex) {
                    logger.severe("Error in processing BOM Master ID~" + bomMasterId + "~" + ex.getMessage());
                } finally {
                    if (trTop != null) {
                        trTop.rollback();
                    }
                    if (trVar != null) {
                        trVar.rollback();
                    }
                    try {
                        if (!FormatHelper.equalsWithNull(objBOMPart, null)) {
                            if (VersionHelper.isCheckedOut(objBOMPart)) {
                                VersionHelper.undoCheckout(objBOMPart);
                            }
                        }
                    } catch (Exception ex) {
                        logger.info("Exception occured while undoing the checkout-for BOM-" + bomMasterId);
                    }
                }
            }
            //End of the BomLinks Creation.
            // Log the final counts for each scenario
            logger.info("Total Successful updates for both Top and Variated Links for the BOM-" + successfulTopAndVariatedCount);
            logger.info("Total Top Links updated with Variated Links exceptions for the BOM-" + topLinksUpdatedWithVariatedExceptionCount);
            logger.info("Total cases with only Top Links present (No Variated Links) for the BOM-" + onlyTopLinksPresentCount);
            data.clear();
        } catch (Exception e) {
            System.err.println("Error processing file-" + file.getName());
            e.printStackTrace();
        } finally {
            FlexTypeCache.refreshCache();
        }
    }

    /**
     * This method set the attributes for the top Link Object.
     *
     * @param objBOMPart
     * @param bomData
     * @param sortingNumber
     */
    private static void processTopLinkData(FlexBOMPart objBOMPart, BomLinkData bomData,
                                           int sortingNumber, String strBrand,String strFileName) throws Exception {
        String materialID = bomData.getMatId();
        String matDesc = bomData.getMatName();
        String section = bomData.getSection();
        String strSuppId = bomData.getSuppId();
        double priceOverride = bomData.getPriceOverride();
        double scCIF = bomData.getScCIF();
        double lossAdjustment = bomData.getLossAdjustment();
        double calcSizePercent = bomData.getScCalculationSizePercent();
        double quantity = bomData.getQuantity();
        String strComments = bomData.getComments();
        String compLocation = bomData.getScComponent();
        Boolean isApparel = GalaxyMigrationConstants.BOOL_FALSE;
        FlexBOMLink topLink = null;
        String matColorId = bomData.getMaterialColor();
        String strPlacement = bomData.getScAppPlacement();
        String strLineNo = bomData.getStrLineNo();
        String strBOMLinkIDA2A2 = bomData.getStrBOMLinkIDA2A2();
        String strColorDesc = bomData.getColorDesc();
		String strHighLight = bomData.getStrHighLight();        
        try {

            topLink = FlexBOMLink.newFlexBOMLink();
            topLink.setParent(objBOMPart.getMaster());
            topLink.setParentRev(objBOMPart.getVersionIdentifier().getValue());
            topLink.setParentReference(objBOMPart.getMasterReference());
            topLink.setBranchId(bomData.getBranchId());
            topLink.setFlexType(objBOMPart.getFlexType());

            topLink.setValue(GalaxyMigrationConstants.SECTION, section);
            LCSMaterial objMaterial = null;
            LCSSupplier objSupplier = null;

            isApparel = GalaxyMigrationConstants.RBK_APP_BRAND.equalsIgnoreCase(strBrand) ? GalaxyMigrationConstants.BOOL_TRUE : GalaxyMigrationConstants.BOOL_FALSE;

            if (GalaxyMigrationConstants.MATERIAL_PLACEHOLDER.equalsIgnoreCase(materialID)) {
                topLink.setChild(LCSMaterialQuery.PLACEHOLDER);
                topLink.setValue(GalaxyMigrationConstants.MATERIAL_DESC, matDesc);
            } else {
                objMaterial = GalaxyBomHelper.findMaterialByID(materialID);
                if (!FormatHelper.equalsWithNull(objMaterial, null)) {
                    topLink.setChild(objMaterial.getMaster());
                    topLink.setValue(GalaxyMigrationConstants.MATERIAL_DESC, objMaterial.getValue(GalaxyMigrationConstants.NAME));
                } else {
                    missingMaterials.add(materialID);
                    failureTopLinks.add(bomData.getProdID()+TILDE_DELIMITER+bomData.getBomName()+TILDE_DELIMITER+bomData.getBomMasterId()+TILDE_DELIMITER+bomData.getStrBOMLinkIDA2A2()+TILDE_DELIMITER
                            +bomData.getBranchId()+TILDE_DELIMITER+bomData.getDimensionName()+TILDE_DELIMITER+GalaxyMigrationConstants.MISSING_MATERIAL+TILDE_DELIMITER+materialID+
                            TILDE_DELIMITER+strFileName+TILDE_DELIMITER+bomData.getStrLineNo()+TILDE_DELIMITER+strFileName);
                    throw new WTException("Warning TopLink~Material doesn't exists-" + materialID +
                            "-BOMMASTERID-"+bomData.getBomMasterId()+"-BOMNAME-"+bomData.getBomName() + "-BRANCHID-" + bomData.getBranchId() +
                            "-BRANCHID-" + bomData.getProdID()+ "-BOMLINKIDA2A2"+strBOMLinkIDA2A2+"-LINENO-"+strLineNo);
                }
            }


            if (GalaxyMigrationConstants.SUPPLIER_PLACEHOLDER.equalsIgnoreCase(strSuppId)) {
                topLink.setSupplier(LCSSupplierQuery.PLACEHOLDER);
            } else {
                if (FormatHelper.hasContent(strSuppId)) {
                    objSupplier = GalaxyBomHelper.findSupplierByNameType(strSuppId);
                    if (FormatHelper.equalsWithNull(objSupplier, null)) {
                        missingSuppliers.add(strSuppId);
                        failureTopLinks.add(bomData.getProdID()+TILDE_DELIMITER+bomData.getBomName()+TILDE_DELIMITER+bomData.getBomMasterId()+TILDE_DELIMITER+bomData.getStrBOMLinkIDA2A2()+TILDE_DELIMITER
                                +bomData.getBranchId()+TILDE_DELIMITER+bomData.getDimensionName()+TILDE_DELIMITER+GalaxyMigrationConstants.MISSING_SUPPLIER+TILDE_DELIMITER+strSuppId+
                                TILDE_DELIMITER+bomData.getStrLineNo()+TILDE_DELIMITER+strFileName);
                        throw new WTException("Warning TopLink~Supplier doesn't exists-" +
                                strSuppId +"-BOMMASTERID-"+bomData.getBomMasterId()+"-BOMNAME-" + bomData.getBomName() + "-BRANCHID-" + bomData.getBranchId() +
                                "-PRODUCTID-" + bomData.getProdID()+"-BOMLINKIDA2A2-"+strBOMLinkIDA2A2+"-LINENO-"+strLineNo);
                    } else {
                        LCSMaterialSupplier matSupp = LCSMaterialSupplierQuery.findMaterialSupplier(objMaterial.getMaster(), objSupplier.getMaster());
                        if (FormatHelper.equalsWithNull(matSupp, null)) {
                            missingMaterialSuppliers.add(materialID+"~"+strSuppId);
                            failureTopLinks.add(bomData.getProdID()+TILDE_DELIMITER+bomData.getBomName()+TILDE_DELIMITER+bomData.getBomMasterId()+TILDE_DELIMITER+bomData.getStrBOMLinkIDA2A2()+TILDE_DELIMITER
                                    +bomData.getBranchId()+TILDE_DELIMITER+bomData.getDimensionName()+TILDE_DELIMITER+GalaxyMigrationConstants.MISSING_MATSUPP+TILDE_DELIMITER+materialID+
                                    TILDE_DELIMITER+strSuppId+TILDE_DELIMITER+bomData.getStrLineNo()+TILDE_DELIMITER+strFileName);
                            throw new WTException("Warning TopLink~Material Supplier Object doesn't exists-" + materialID +
                                    "-SupplierID-" + strSuppId +"-BOMMASTERID-"+bomData.getBomMasterId()+"-BOMNAME-"+
                                    bomData.getBomName() + "-BRANCHID-" + bomData.getBranchId() + "-PRODUCTID-" + bomData.getProdID()+
                                    "-BOMLINKIDA2A2-"+strBOMLinkIDA2A2+"-LINENO-"+strLineNo);
                        } else {
                            topLink.setSupplier(objSupplier.getMaster());
                            topLink.setValue(GalaxyMigrationConstants.SUPPLIER_DESC, objSupplier.getSupplierName());
                        }
                    }
                }
            }
			
			if(FormatHelper.hasContent(strHighLight)){
                topLink.setValue(GalaxyMigrationConstants.HIGH_LIGHT,strHighLight);
            }
            topLink.setValue(GalaxyMigrationConstants.LOSS_ADJUSTMENT, lossAdjustment);
            topLink.setValue(GalaxyMigrationConstants.PRICE_OVERRIDE, priceOverride);
            topLink.setValue(GalaxyMigrationConstants.CALC_SIZE_PERCENT, calcSizePercent);
            topLink.setValue(GalaxyMigrationConstants.CIF, scCIF);
            topLink.setValue(GalaxyMigrationConstants.QUANTITY, quantity);
            topLink.setValue(GalaxyMigrationConstants.COMMENTS, strComments);
			
            if (FormatHelper.hasContent(compLocation)) {
                topLink.setValue(GalaxyMigrationConstants.COM_ATTRIBUTE, GalaxyBomHelper.getComponentObject(compLocation, isApparel));
            }

            if(isApparel) {
                topLink.setValue(GalaxyMigrationConstants.SC_APP_PLACEMENT, strPlacement);
            }

            if (FormatHelper.hasContent(bomData.getColor())) {
                LCSColor objColor = GalaxyBomHelper.getColorObject(bomData.getColor());
                if (!FormatHelper.equalsWithNull(objColor, null)) {
                    topLink.setColor(objColor);
                    topLink.setValue(GalaxyMigrationConstants.COLOR_DESC, objColor.getName());
                } else {
                   /* throw new WTException("Warning TopLink~No Color available for the given-" + bomData.getColor() +
                            "-BOMMASTERID-" + bomData.getBomMasterId() + "-BOMNAME-" + bomData.getBomName() +
                            "-BRANCHID-" + bomData.getBranchId() + "-" +
                            "PRODUCTID-" + bomData.getProdID() + "-BOMLINKIDA2A2-" + bomData.getStrBOMLinkIDA2A2() + "-LINENO-" + bomData.getStrLineNo());

                    */
                }
            }else{
                topLink.setValue(GalaxyMigrationConstants.COLOR_DESC, strColorDesc);
            }

            if(FormatHelper.hasContent(matColorId)){
                topLink.setMaterialColor(GalaxyBomHelper.getMaterialColor(matColorId));
            }

            topLink.setValue(GalaxyMigrationConstants.BOM_DETAILED_CONTENT, bomData.getScBOMDetailedContent());
            topLink.setYield(quantity);
            topLink.calculateDimensionId();
            topLink.setWip(false);
            topLink.setInDate(new Timestamp((new Date()).getTime()));
            topLink.setOutDate((Timestamp) null);
            topLink.setDropped(false);
            topLink.setSortingNumber(sortingNumber);

            LCSLogic.deriveFlexTypeValues(topLink, true);
            topLink = (FlexBOMLink) LCSLogic.persist(topLink);
        } catch (WTException e) {
            throw new WTException(e.getMessage());
        } catch (WTPropertyVetoException e) {
            throw new WTException("Error TopLink-There is an issue while persisting the bomLink object for the product----" + bomData.getProdID() + " and " +
                    "the BOM Part Master-" + bomData.getBomMasterId() + "-and the BOM Branch Id is-" + bomData.getBranchId() + "and the BOMLink IDA2A2-"+strBOMLinkIDA2A2+"-and the lineno-"+strLineNo+"-"+e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error TopLink-There is an issue while persisting the bomLink object for the product-" + bomData.getProdID() + "-and-" +
                    "the BOM Part Master-" + bomData.getBomMasterId() + "-and the BOM Branch Id is-" + bomData.getBranchId() + "and the BOMLink IDA2A2-"+strBOMLinkIDA2A2+"-and the lineno-"+strLineNo+"-"+e.getMessage());
        }

    }


    /**
     * @param objBOMPart
     * @param bomData
     * @param strBrand
     * @throws Exception
     */
    private static void processVariatedLinkData(FlexBOMPart objBOMPart, BomLinkData bomData, String strBrand,String strFileName) throws
            Exception {

        String materialID = bomData.getMatId();
        String matDesc = bomData.getMatName();
        String strSuppId = bomData.getSuppId();
        double priceOverride = bomData.getPriceOverride();
        double scCIF = bomData.getScCIF();
        double lossAdjustment = bomData.getLossAdjustment();
        double calcSizePercent = bomData.getScCalculationSizePercent();
        double quantity = bomData.getQuantity();
        String strColorNo = bomData.getColor();
        String strColorwayNo = bomData.getColorway();
        String strProductNo = bomData.getProdID();
        Boolean isApparel = GalaxyMigrationConstants.BOOL_FALSE;
        String compLocation = bomData.getScComponent();
        String strComments = bomData.getComments();
        double grossQuantity = bomData.getScGrossQuantity();
        String matColorId = bomData.getMaterialColor();
        String strBOMLinkIDA2A2 = bomData.getStrBOMLinkIDA2A2();
		String strHighLight = bomData.getStrHighLight();        
        LCSProduct objProduct = null;
        LCSSourcingConfig objSourcingConfig = null;
        LCSSKU objSKU = null;
        LCSColor objColor = null;
        try {
            FlexBOMLink varLink = FlexBOMLink.newFlexBOMLink();
            varLink.setParent(objBOMPart.getMaster());
            varLink.setParentRev(objBOMPart.getVersionIdentifier().getValue());
            varLink.setParentReference(objBOMPart.getMasterReference());
            varLink.setBranchId(bomData.getBranchId());
            varLink.setFlexType(objBOMPart.getFlexType());

            LCSMaterial objMaterial = null;

            if (GalaxyMigrationConstants.MATERIAL_PLACEHOLDER.equalsIgnoreCase(materialID)) {
                varLink.setChild(LCSMaterialQuery.PLACEHOLDER);
                varLink.setValue(GalaxyMigrationConstants.MATERIAL_DESC, matDesc);
            } else {
                objMaterial = GalaxyBomHelper.findMaterialByID(materialID);
                if (!FormatHelper.equalsWithNull(objMaterial, null)) {
                    varLink.setChild(objMaterial.getMaster());
                    varLink.setValue(GalaxyMigrationConstants.MATERIAL_DESC, objMaterial.getValue(GalaxyMigrationConstants.NAME));
                } else {
                    missingMaterials.add(materialID);
                    failureVariatedLinks.add(bomData.getProdID()+TILDE_DELIMITER+bomData.getBomName()+TILDE_DELIMITER+bomData.getBomMasterId()+TILDE_DELIMITER+bomData.getStrBOMLinkIDA2A2()+TILDE_DELIMITER
                            +bomData.getBranchId()+TILDE_DELIMITER+bomData.getDimensionName()+TILDE_DELIMITER+GalaxyMigrationConstants.MISSING_MATERIAL+TILDE_DELIMITER+materialID+TILDE_DELIMITER
                            +bomData.getStrLineNo()+TILDE_DELIMITER+strFileName);

                    throw new WTException("Warning VarLink~Material doesn't exists-" + materialID
                            + "-BOMMASTERID-" + bomData.getBomMasterId()+"-BOMNAME-"+bomData.getBomName() + "-BRANCHID-"
                            + bomData.getBranchId() + "-PRODUCTID-" + bomData.getProdID()+"-BOMLINKIDA2A2-"+strBOMLinkIDA2A2+"-LINENO-"+bomData.getStrLineNo());
                }
            }

            LCSSupplier objSupplier = null;
            if (GalaxyMigrationConstants.SUPPLIER_PLACEHOLDER.equalsIgnoreCase(strSuppId)) {
                varLink.setSupplier(LCSSupplierQuery.PLACEHOLDER);
            } else {
                if (FormatHelper.hasContent(strSuppId)) {
                    objSupplier = GalaxyBomHelper.findSupplierByNameType(strSuppId);
                    if (FormatHelper.equalsWithNull(objSupplier, null)) {
                        missingSuppliers.add(strSuppId);
                        failureVariatedLinks.add(bomData.getProdID()+TILDE_DELIMITER+bomData.getBomName()+TILDE_DELIMITER+bomData.getBomMasterId()+TILDE_DELIMITER+bomData.getStrBOMLinkIDA2A2()+TILDE_DELIMITER
                                +bomData.getBranchId()+TILDE_DELIMITER+bomData.getDimensionName()+TILDE_DELIMITER+GalaxyMigrationConstants.MISSING_SUPPLIER+TILDE_DELIMITER+strSuppId+
                                TILDE_DELIMITER+
                                bomData.getStrLineNo()+TILDE_DELIMITER+strFileName);

                        throw new WTException("Warning VarLink~Supplier doesn't exists-" + strSuppId
                                + "-BOMMASTERID-" + bomData.getBomMasterId()+"-BOMNAME-"+bomData.getBomName() + "-BRANCHID-"
                                +bomData.getBranchId() + "-PRODUCTID-"+bomData.getProdID()+"-BOMLINKIDA2A2-"+strBOMLinkIDA2A2+"-LINENO-"+bomData.getStrLineNo());
                    } else {
                        LCSMaterialSupplier matSupp = LCSMaterialSupplierQuery.findMaterialSupplier(objMaterial.getMaster(), objSupplier.getMaster());
                        if (FormatHelper.equalsWithNull(matSupp, null)) {
                            missingMaterialSuppliers.add(materialID+"~"+strSuppId);
                            failureVariatedLinks.add(bomData.getProdID()+TILDE_DELIMITER+bomData.getBomName()+TILDE_DELIMITER+bomData.getBomMasterId()+TILDE_DELIMITER+bomData.getStrBOMLinkIDA2A2()+TILDE_DELIMITER
                                    +bomData.getBranchId()+TILDE_DELIMITER+bomData.getDimensionName()+TILDE_DELIMITER+GalaxyMigrationConstants.MISSING_SUPPLIER+TILDE_DELIMITER+
                                    materialID+TILDE_DELIMITER+strSuppId+TILDE_DELIMITER+
                                    bomData.getStrLineNo()+TILDE_DELIMITER+strFileName);
                            throw new WTException("Warning VarLink~Material Supplier Object doesn't exists-"
                                    + materialID + "-SupplierID-" + strSuppId +
                                    "-BOMMASTERID-" + bomData.getBomMasterId()+"-BOMNAME-"+bomData.getBomName()
                                    + "-BRANCHID-" +
                                    bomData.getBranchId() + "-PRODUCTID-" + bomData.getProdID()+"-BOMLINKIDA2A2-"+strBOMLinkIDA2A2+"-LINENO-"+bomData.getStrLineNo());
                        } else {
                            varLink.setSupplier(objSupplier.getMaster());
                            varLink.setValue(GalaxyMigrationConstants.SUPPLIER_DESC, objSupplier.getSupplierName());
                        }
                    }
                }
            }

            isApparel = GalaxyMigrationConstants.RBK_APP_BRAND.equalsIgnoreCase(strBrand) ? GalaxyMigrationConstants.BOOL_TRUE : GalaxyMigrationConstants.BOOL_FALSE;
            Boolean isContinueProccessing = handleVarBomData(bomData, isApparel, varLink,strFileName);
            
            if(!isContinueProccessing){
				System.out.println("Inside the Continue Processing");
                return;
            }
			if(FormatHelper.hasContent(strHighLight)){
				varLink.setValue(GalaxyMigrationConstants.HIGH_LIGHT,strHighLight);
            }

            varLink.setValue(GalaxyMigrationConstants.LOSS_ADJUSTMENT, lossAdjustment);
            varLink.setValue(GalaxyMigrationConstants.PRICE_OVERRIDE, priceOverride);
            varLink.setValue(GalaxyMigrationConstants.CALC_SIZE_PERCENT, calcSizePercent);
            varLink.setValue(GalaxyMigrationConstants.CIF, scCIF);
            varLink.setValue(GalaxyMigrationConstants.QUANTITY, quantity);
            varLink.setYield(quantity);
            varLink.setValue(GalaxyMigrationConstants.COMMENTS, strComments);
            varLink.setValue("scGrossQuantity", grossQuantity);
            if (FormatHelper.hasContent(compLocation)) {
                varLink.setValue(GalaxyMigrationConstants.COM_ATTRIBUTE, GalaxyBomHelper.getComponentObject(compLocation, isApparel));
            }
            varLink.setValue(GalaxyMigrationConstants.BOM_DETAILED_CONTENT, bomData.getScBOMDetailedContent());
            varLink.calculateDimensionId();
            varLink.setWip(false);
            varLink.setInDate(new Timestamp((new Date()).getTime()));
            varLink.setOutDate((Timestamp) null);
            varLink.setDropped(false);
            varLink.setSortingNumber(0);
            LCSFlexBOMLogic.persist(varLink);

        } catch (WTException e) {
            throw new WTException(e.getMessage());
        } catch (WTPropertyVetoException e) {
            e.printStackTrace();
            throw new WTException("Error VarLink-There is an issue while persisting the Variated BOM object-" +
                    bomData.getDimensionName() + "-for the product-" + bomData.getProdID() + "-and" +
                    "-the BOM Part Master-" + bomData.getBomMasterId() +
                    "-and the BOM Branch ID is-" + bomData.getBranchId()
                    +"-and the BOMLink IDA2A2-"+strBOMLinkIDA2A2+"-and the line no-"+bomData.getStrLineNo()+"-"+e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException("Error VarLink-There is an issue while persisting the Variated BOM object-" + bomData.getDimensionName() + "-for the product-" + bomData.getProdID() + "-and" +
                    "the BOM Part Master-" + bomData.getBomMasterId() + "-and the BOM Branch ID is-" + bomData.getBranchId() +"-"+
                    "-and the BOM Master ID is-"+bomData.getBranchId()+"-and the lineno-"+bomData.getStrLineNo()+"-"+e.getMessage());
        }
    }


    private static boolean handleVarBomData(BomLinkData bomData, boolean isApparel, FlexBOMLink varLink, String strFileName) throws Exception {
        if (GalaxyMigrationConstants.SKU_VARIATION.equalsIgnoreCase(bomData.getDimensionName())) {
            return handleSKUAndColor(bomData, isApparel, varLink, strFileName);
        } else if (GalaxyMigrationConstants.SOURCE_VARIATION.equalsIgnoreCase(bomData.getDimensionName())) {
            handleSourcing(bomData, varLink, strFileName);
            return true;
        } else {
            boolean proceed = handleSKUAndColor(bomData, isApparel, varLink, strFileName);
            if (!proceed) return false;
            handleSourcing(bomData, varLink, strFileName);
            return true;
        }
    }


    /**
     * @param bomData
     * @param isApparel
     * @param varLink
     * @throws WTException
     * @throws Exception
     */
    /*private static void handleVarBomData(BomLinkData bomData, boolean isApparel, FlexBOMLink varLink,String strFileName) throws
            WTException, Exception {
        if (GalaxyMigrationConstants.SKU_VARIATION.equalsIgnoreCase(bomData.getDimensionName())) {
            handleSKUAndColor(bomData, isApparel, varLink,strFileName);
        } else if (GalaxyMigrationConstants.SOURCE_VARIATION.equalsIgnoreCase(bomData.getDimensionName())) {
            handleSourcing(bomData, varLink,strFileName);
        } else {
            if(handleSKUAndColor(bomData, isApparel, varLink,strFileName)) {
                handleSourcing(bomData, varLink, strFileName);
            }
        }
    }*/

    /**
     * @param bomData
     * @param isApparel
     * @param varLink
     * @throws WTException
     */
    private static boolean handleSKUAndColor(BomLinkData bomData, boolean isApparel, FlexBOMLink varLink,String strFileName) throws
            Exception {
        LCSSKU objSKU = null;
        objSKU = isApparel
                ? GalaxyBomHelper.findSKU(bomData.getColorway(), bomData.getProdID(), GalaxyMigrationConstants.RBK_APPAREL_PATH)
                : GalaxyBomHelper.findSKU(bomData.getColorway(), bomData.getProdID(), GalaxyMigrationConstants.RBK_FOOTWEAR_PATH);

        if (FormatHelper.equalsWithNull(objSKU, null)) {
            missingColorways.add(bomData.getColorway());
            failureVariatedLinks.add(bomData.getProdID()+TILDE_DELIMITER+bomData.getBomName()+TILDE_DELIMITER+bomData.getBomMasterId()+TILDE_DELIMITER+bomData.getStrBOMLinkIDA2A2()+TILDE_DELIMITER
                    +bomData.getBranchId()+TILDE_DELIMITER+bomData.getDimensionName()+TILDE_DELIMITER+GalaxyMigrationConstants.MISSING_COLORWAY+TILDE_DELIMITER+
                    bomData.getColorway()+TILDE_DELIMITER+strFileName+TILDE_DELIMITER+bomData.getStrLineNo());
		  System.out.println("objSKU is not found");
          return false;
        }
        else {
            varLink.setColorDimension(objSKU.getMaster());
            if (FormatHelper.hasContent(bomData.getColor())) {
                LCSColor objColor = GalaxyBomHelper.getColorObject(bomData.getColor());
                if (!FormatHelper.equalsWithNull(objColor, null)) {
                    varLink.setColor(objColor);
                    varLink.setValue(GalaxyMigrationConstants.COLOR_DESC, objColor.getName());
                } else {
                    missingColors.add(bomData.getColor());
                    failureVariatedLinks.add(bomData.getProdID() + TILDE_DELIMITER + bomData.getBomName() + TILDE_DELIMITER + bomData.getBomMasterId() + TILDE_DELIMITER + bomData.getStrBOMLinkIDA2A2() + TILDE_DELIMITER
                            + bomData.getBranchId() + TILDE_DELIMITER + bomData.getDimensionName() + TILDE_DELIMITER + GalaxyMigrationConstants.MISSING_COLOR + TILDE_DELIMITER + bomData.getColor() +
                            TILDE_DELIMITER + strFileName + TILDE_DELIMITER + bomData.getStrLineNo());

                    throw new WTException("Warning VarLink~No Color available for the given-" + bomData.getColor() +
                            "-BOMMASTERID-" + bomData.getBomMasterId() + "-BOMNAME-" + bomData.getBomName() +
                            "-BRANCHID-" + bomData.getBranchId() + "-" +
                            "PRODUCTID-" + bomData.getProdID() + "-BOMLINKIDA2A2-" + bomData.getStrBOMLinkIDA2A2() + "-LINENO-" + bomData.getStrLineNo());
                }
            } else {
                varLink.setValue(GalaxyMigrationConstants.COLOR_DESC, bomData.getColorDesc());
            }
            LCSMaterialColor objMatColor = null;
            if (FormatHelper.hasContent(bomData.getMaterialColor()) && !"0".equalsIgnoreCase(bomData.getMaterialColor())) {
                objMatColor = GalaxyBomHelper.getMaterialColor(bomData.getMaterialColor());
                if (FormatHelper.equalsWithNull(objMatColor, null)) {
                    missingMaterialColors.add(bomData.getMaterialColor());
                    failureVariatedLinks.add(bomData.getProdID() + TILDE_DELIMITER + bomData.getBomName() + TILDE_DELIMITER + bomData.getBomMasterId() + TILDE_DELIMITER + bomData.getStrBOMLinkIDA2A2() + TILDE_DELIMITER
                            + bomData.getBranchId() + TILDE_DELIMITER + bomData.getDimensionName() + TILDE_DELIMITER + GalaxyMigrationConstants.MISSING_MAT_COLOR + TILDE_DELIMITER + bomData.getMaterialColor()
                            + TILDE_DELIMITER + strFileName + TILDE_DELIMITER + bomData.getStrLineNo());
                    throw new WTException("Warning VarLink~No Material color available for the Material-" + bomData.getMaterialColor() +
                            "-BOMMASTERID-" + bomData.getBomMasterId() + "-BOMNAME-" + bomData.getBomName() + "-BRANCHID-" +
                            bomData.getBranchId() + "-PRODUCTID-" + bomData.getProdID() + "-BOMLINKIDA2A2-" + bomData.getStrBOMLinkIDA2A2() + "-LINENO-" + bomData.getStrLineNo());
                } else {
                    varLink.setMaterialColor(objMatColor);
                }
            }
        }
        return true;
    }


    /**
     * @param bomData
     * @param varLink
     * @throws Exception
     */
    private static void handleSourcing(BomLinkData bomData, FlexBOMLink varLink,String strFileName) throws Exception {
        LCSProduct objProduct = null;
        LCSSourcingConfig objSourcingConfig = null;
        objProduct = GalaxyBomHelper.getProduct(bomData.getProdID(), "A");
        if (objProduct == null) {
            throw new WTException("No Product found for the product Id -------- " + bomData.getProdID());
        }
        objSourcingConfig = GalaxyBomHelper.getExistingSourcingConfig(bomData.getSourcingNo(), objProduct);
        if (objSourcingConfig == null) {
            missingSourceIds.add(bomData.getSourcingNo());
            failureVariatedLinks.add(bomData.getProdID()+TILDE_DELIMITER+bomData.getBomName()+TILDE_DELIMITER+bomData.getBomMasterId()+TILDE_DELIMITER+bomData.getStrBOMLinkIDA2A2()+TILDE_DELIMITER
                    +bomData.getBranchId()+TILDE_DELIMITER+bomData.getDimensionName()+TILDE_DELIMITER+GalaxyMigrationConstants.MISSING_SOURCING+TILDE_DELIMITER+bomData.getSourcingNo()
                    +TILDE_DELIMITER+strFileName+TILDE_DELIMITER+bomData.getStrLineNo());
            throw new WTException("Warning VarLink~No Source with sourcing No-" +bomData.getSourcingNo() +
                    "-BOMMASTERID-" + bomData.getBomMasterId()+"-BOMNAME-"+bomData.getBomName()+
                    "-BRANCHID-" + bomData.getBranchId() + "-PRODUCTID-" + bomData.getProdID()+"-BOMLINKIDA2A2-"+bomData.getStrBOMLinkIDA2A2()
                    +"-LINENO-"+bomData.getStrLineNo());
        }
        varLink.setSourceDimension(objSourcingConfig.getMaster());
    }

    private static void writeMissingDataToFile(String startTime,String endTime,String data) {
        try {

            System.out.println("writeMissingDataToFile - Start");

            Random rand = new Random();
            int randomPrefix = rand.nextInt(100000);

            File reportDir = new File(baseDir+File.separator+data+"_"+randomPrefix);
            if (!reportDir.exists()) {
                reportDir.mkdirs(); // This will create the directory if it doesn't exist
            }

            System.out.println("ReportDir Absolute Path-----------"+reportDir.getAbsolutePath());

            // Write missing colors to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportDir.getAbsolutePath() + File.separator+ "missing_colors"+"_"+randomPrefix+".txt"))) {
                for (String color : missingColors) {
                    writer.write(color);
                    writer.newLine();
                }
            }

            // Write missing materials to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportDir.getAbsolutePath() + File.separator+ "missing_materials"+"_"+randomPrefix+".txt"))) {
                for (String material : missingMaterials) {
                    writer.write(material);
                    writer.newLine();
                }
            }

            // Write missing suppliers to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportDir.getAbsolutePath() + File.separator+ "missing_suppliers.txt"+"_"+randomPrefix+".txt"))) {
                for (String supplier : missingSuppliers) {
                    writer.write(supplier);
                    writer.newLine();
                }
            }

            // Write missing material suppliers to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportDir.getAbsolutePath() + File.separator+ "missing_material_suppliers"+"_"+randomPrefix+".txt"))) {
                for (String materialSupplier : missingMaterialSuppliers) {
                    writer.write(materialSupplier);
                    writer.newLine();
                }
            }

            // Write missing material colors to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportDir.getAbsolutePath() + File.separator+ "missing_material_colors"+"_"+randomPrefix+".txt"))) {
                for (String materialColor : missingMaterialColors) {
                    writer.write(materialColor);
                    writer.newLine();
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportDir.getAbsolutePath() + File.separator+ "missing_colorways"+"_"+randomPrefix+".txt"))) {
                for (String strCway : missingColorways) {
                    writer.write(strCway);
                    writer.newLine();
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportDir.getAbsolutePath() + File.separator+ "missing_sourcings"+"_"+randomPrefix+".txt"))) {
                for (String missingSource : missingSourceIds) {
                    writer.write(missingSource);
                    writer.newLine();
                }
            }



            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportDir.getAbsolutePath() + File.separator+ "Summary_Report"+"_"+randomPrefix+".txt"))) {
                writer.write("Summary Report");
                writer.write("Top link count created successfully-"+topLinkCount);
                writer.newLine();
                writer.write("Var link count created successfully-"+varLinkCount);
                writer.newLine();
                writer.write("Total top links failed count-"+topFailedCount);
                writer.newLine();
                writer.write("Total Var links failed count-"+varFailedCount);
                writer.write("StartTime-"+startTime);
                writer.newLine();
                writer.write("EndTime-"+endTime);
            }


            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportDir.getAbsolutePath() + File.separator+ "VarLinkFailuresInfo"+"_"+randomPrefix+".txt"))){
                for (String failureVarLink : failureVariatedLinks) {
                    writer.write(failureVarLink);
                    writer.newLine();
                }
            }
            //End of the that
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportDir.getAbsolutePath() + File.separator+ "TopLinksSuccessFull"+"_"+randomPrefix+".txt"))){
                writer.write("Summary Report of the TopLinks created successfully for the BOM and the corresponding Product Info");
                writer.newLine();
                for (String onlyTopLink : onlyTopLinksSuccessList) {
                    writer.write(onlyTopLink);
                    writer.newLine();
                }
                writer.write("Total count of the BOM Parts for which Top Links created successfully-"+onlyTopLinksSuccessList.size());
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportDir.getAbsolutePath() + File.separator+ "TopLinksFailures"+"_"+randomPrefix+".txt"))){
                writer.write("Summary Report of the TopLinks not created for the BOM and the corresponding Product Info");
                for (String failTopLink : failureTopLinkList) {
                    writer.write(failTopLink);
                    writer.newLine();
                }
                writer.write("Total count of the BOM Parts for which Top Links not created successfully-"+failureTopLinkList.size());
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportDir.getAbsolutePath() + File.separator+ "VariatedLinkFailures"+"_"+randomPrefix+".txt"))){
                writer.write("Summary Report of the VariatedLinks not created for the BOM and the corresponding Product Info");
                for (String failVarLink : failureVarLinkList) {
                    writer.write(failVarLink);
                    writer.newLine();
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportDir.getAbsolutePath() + File.separator+ "BothTopAndVarSuccess"+"_"+randomPrefix+".txt"))){
                writer.write("Summary Report of the Both Top and Variated Links created for the BOM and the corresponding Product Info");
                for (String failVarLink : bothTopandVarSuccessList) {
                    writer.write(failVarLink);
                    writer.newLine();
                }
                writer.write("Total count of the BOM Parts for which Top Links and Var links created successfully-"+bothTopandVarSuccessList.size());
            }



            System.out.println("Missing data has been written to files.");
        } catch (IOException e) {
            e.printStackTrace();
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
