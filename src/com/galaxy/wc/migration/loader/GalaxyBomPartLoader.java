package com.galaxy.wc.migration.loader;

import static com.galaxy.wc.bom.constants.GalaxyMigrationConstants.BOM_ATTR_LIST;
import static com.galaxy.wc.bom.constants.GalaxyMigrationConstants.PRIMARY_BOM_ATTR_LIST;

import com.galaxy.wc.bom.constants.GalaxyMigrationConstants;
import com.galaxy.wc.migration.loader.util.BomPartProcessingStatus;
import com.galaxy.wc.migration.loader.util.BomPrimaryUpdateResult;
import com.galaxy.wc.migration.loader.util.BomProcessingResult;
import com.galaxy.wc.migration.loader.util.GalaxyBomPartUtil;
import wt.httpgw.GatewayAuthenticator;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;




public class GalaxyBomPartLoader implements RemoteAccess {
    static class BomPrimaryUpdateSummary {
        int totalFiles = 0;
        int totalRecords =0 ;
        int updatedPrimaryCount = 0;
        int exceptionPrimaryCount = 0;
        int validationPrimaryCount = 0;

        Set<String> successllyUpdatedBoms = new HashSet<>();
        Set<String> validationFailedMasterIds = new HashSet<>();
        Set<String> exceptionBomMasterIds = new HashSet<>();

        void add(BomPrimaryUpdateSummary other){
            totalFiles++;
            totalRecords += other.totalRecords;
            updatedPrimaryCount += other.updatedPrimaryCount;
            exceptionPrimaryCount += other.exceptionPrimaryCount;
            validationPrimaryCount += other.validationPrimaryCount;
        }
    }


    static class BomProcessingSummary {
        int totalFiles = 0;
        int totalRecords = 0;
        int successCount = 0;
        int validationFailCount = 0;
        int duplicateCount = 0;
        int exceptionCount = 0;
        int addBomToSpecIssueCount = 0;

        Set<String> successfulBomMasterIds = new HashSet<>();
        Set<String> failedBomMasterIds = new HashSet<>();

        void add(BomProcessingSummary other) {
            totalFiles++;
            totalRecords += other.totalRecords;
            successCount += other.successCount;
            validationFailCount += other.validationFailCount;
            duplicateCount += other.duplicateCount;
            exceptionCount += other.exceptionCount;
            addBomToSpecIssueCount += other.addBomToSpecIssueCount;
            successfulBomMasterIds.addAll(other.successfulBomMasterIds);
            failedBomMasterIds.addAll(other.failedBomMasterIds);
        }
    }

    public static void main(String[] args) {

        try {
            // Check if the argument (directory path) is provided
            //windchill com.galaxy.wc.migration.loader.GalaxyBomPartLoader C:\PTC\Windchill_12.0\Windchill\codebase\BOMEXTRACT\
            if (args.length > 0) {
                RemoteMethodServer remoteMethodServer = RemoteMethodServer.getDefault();
                String username = "wcadmin";
                GatewayAuthenticator auth = new GatewayAuthenticator();
                auth.setRemoteUser(username);
                remoteMethodServer.setAuthenticator(auth);
                Object[] objArgs = {args[0]};
                Class[] classArr = {String.class};
                if (remoteMethodServer != null) {
                   
                     remoteMethodServer.invoke("loadBOMData", "com.galaxy.wc.migration.loader.GalaxyBomPartLoader", null, classArr, objArgs);
                    
                }
            } else {
                System.out.println("Please provide a directory path.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   
    /**
     *
     * @param directoryPath
     */
    public static void loadBOMData(String directoryPath) {
        Path path = Paths.get(directoryPath);
        BomProcessingSummary grandTotal = new BomProcessingSummary();
        Path logsDirPath = Paths.get(directoryPath, "logs");

        try (Stream<Path> files = Files.list(path)) {
            files.filter(Files::isRegularFile)
                    .forEach(entry -> {
                        BomProcessingSummary summary = processFile(entry, directoryPath);
                        grandTotal.add(summary);
                    });
            writeFinalSummary(logsDirPath, grandTotal);

        } catch (IOException e) {
            System.err.println("Error reading directory: " + path);
            e.printStackTrace();
        }
    }    


    /**
     *
     * @param logsDirPath
     * @param summary
     */
    private static void writeFinalSummary(Path logsDirPath, BomProcessingSummary summary) {
        Path reportPath = logsDirPath.resolve("Summary_Report.txt");

        List<String> lines = List.of(
                "=========== BOM Processing Summary ===========",
                "Total Files Processed        : " + summary.totalFiles,
                "Total Records Processed      : " + summary.totalRecords,
                "Successful BOM Creations     : " + summary.successCount,
                "Validation Failures          : " + summary.validationFailCount,
                "Duplicate BOMs Skipped       : " + summary.duplicateCount,
                "Exceptions During Processing : " + summary.exceptionCount,
                "Add BOM to Spec Failures     : " + summary.addBomToSpecIssueCount,
                "Failure BOM Count            : " +summary.failedBomMasterIds.size(),
                "Failure BOM Master IDs       :" +summary.failedBomMasterIds,
                "=============================================="
        );

        try {
            Files.write(reportPath, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Summary report written to: " + reportPath);
        } catch (IOException e) {
            System.err.println("Error writing final summary: " + e.getMessage());
        }
    }

    private static BomProcessingSummary processFile(Path entry, String directoryPath) {
        System.out.println("processFile file: " + entry.getFileName());

        // Initialize logging directory
        Path logsDirPath = Paths.get(directoryPath, "logs");
        if (!Files.exists(logsDirPath)) {
            try {
                Files.createDirectories(logsDirPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Log file path setup
        String logFileName = entry.getFileName().toString().replace(".txt", "_BOMLog.txt");
        Path logFilePath = logsDirPath.resolve(logFileName);

        int lineNo = 1;
        BomLogger logger = null;

        boolean fileProcessedSuccessfully = true;

        BomProcessingSummary summary = new BomProcessingSummary();
        summary.totalFiles = 1;

        try {
            // Initialize logger
            logger = BomLogger.getInstance(new FileLogStrategy(logFilePath.toString()));

            // Process each line of the file
            try (BufferedReader br = Files.newBufferedReader(entry, StandardCharsets.UTF_8)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("#")) {
                        continue; // Skip comment lines
                    }

                    Map<String, String> bomData = new HashMap<>();
                    List<String> sampleData = Arrays.asList(line.split("\\^"));

                    // Populate BOM data from the line
                    for (int i = 0; i < sampleData.size(); i++) {
                        if (i < BOM_ATTR_LIST.size()) {
                            bomData.put(BOM_ATTR_LIST.get(i), sampleData.get(i));
                        }
                    }

                    // Process the BOM data
                    try {
                        BomProcessingResult result = GalaxyBomPartUtil.processBOMData(bomData, logger, lineNo);
                        if(result.bomCreated){
                            summary.successCount++;
                            summary.successfulBomMasterIds.add(bomData.get(GalaxyMigrationConstants.BOM_MASTER_ID));
                        }
                        if(result.specLinkFailed){
                            summary.addBomToSpecIssueCount++;
                        }
                        if(result.bomAlreadyExists){
                            summary.duplicateCount++;
                        }
                        switch (result.status) {
                            case VALIDATION_FAILED:
                                summary.validationFailCount++;								
                                summary.failedBomMasterIds.add(bomData.get(GalaxyMigrationConstants.BOM_MASTER_ID));
                                break;
                          /*  case ALREADY_EXISTS:
                                summary.duplicateCount++;
                                break;*/
                            case EXCEPTION:
                                summary.exceptionCount++;
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        fileProcessedSuccessfully = false;  // Mark the file as failed
                        if (logger != null) {
                            logger.log("Error processing record: " + lineNo + " - Exception: " + e.getMessage());
                        }
                    }
                    lineNo++;
                }

                // After file processing, move it to the appropriate directory based on success
                if (fileProcessedSuccessfully) {
                    moveProcessedFile(entry, directoryPath);
                } else {
                    moveFailedFile(entry, directoryPath);  // Move to 'failed' folder if failed
                }
            } catch (IOException e) {
                System.err.println("Error reading file: " + entry.getFileName());
                e.printStackTrace();
                if (logger != null) {
                    logger.log("File read error: " + e.getMessage());
                }
                moveFailedFile(entry, directoryPath);  // Move file to 'failed' folder in case of read error
            }

        } catch (Exception e) {
            System.err.println("Logger initialization or processing error for file: " + entry.getFileName());
            e.printStackTrace();
            fileProcessedSuccessfully = false;
        } finally {
            // Close logger and ensure logs are flushed
            if (logger != null) {
                try {
                    if (fileProcessedSuccessfully) {
                        logger.log("Processing complete for file: " + entry.getFileName());
                        logger.log("File: " + entry.getFileName());
                        logger.log("SUCCESS: " + summary.successCount);
                        logger.log("VALIDATION_FAILED: " + summary.validationFailCount);
                        logger.log("ALREADY_EXISTS: " + summary.duplicateCount);
                        logger.log("EXCEPTION: " + summary.exceptionCount);
                        logger.log("ADD_BOM_TO_SPEC_ISSUE: " + summary.addBomToSpecIssueCount);
                        logger.log("Status: " + (fileProcessedSuccessfully ? "SUCCESS" : "FAILURE"));
                        logger.close();
                    } else {
                        logger.log("File processing failed for: " + entry.getFileName());
						logger.close(); 
                    }
                   // Ensure logs are written to file
                } catch (Exception e) {
                    System.err.println("Error closing logger: " + e.getMessage());
                }
            }
        }
        return summary;
    }

    private static BomPrimaryUpdateSummary processPrimaryMaterialUpdate(Path entry, String directoryPath) {
        System.out.println("processFile file: " + entry.getFileName());

        // Initialize logging directory
        Path logsDirPath = Paths.get(directoryPath, "Primarylogs");
        if (!Files.exists(logsDirPath)) {
            try {
                Files.createDirectories(logsDirPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Log file path setup
        String logFileName = entry.getFileName().toString().replace(".txt", "_BOMLog.txt");
        Path logFilePath = logsDirPath.resolve(logFileName);

        int lineNo = 1;
        BomLogger logger = null;

        boolean fileProcessedSuccessfully = true;

        BomPrimaryUpdateSummary summary = new BomPrimaryUpdateSummary();
        summary.totalFiles = 1;

        try {
            // Initialize logger
            logger = BomLogger.getInstance(new FileLogStrategy(logFilePath.toString()));

            // Process each line of the file
            try (BufferedReader br = Files.newBufferedReader(entry, StandardCharsets.UTF_8)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("#")) {
                        continue; // Skip comment lines
                    }

                    Map<String, String> bomData = new HashMap<>();
                    List<String> sampleData = Arrays.asList(line.split("\\^"));

                    // Populate BOM data from the line
                    for (int i = 0; i < sampleData.size(); i++) {
                        if (i < PRIMARY_BOM_ATTR_LIST.size()) {
                            bomData.put(PRIMARY_BOM_ATTR_LIST.get(i), sampleData.get(i));
                        }
                    }

                    // Process the BOM data
                    try {
                        BomPrimaryUpdateResult result = GalaxyBomPartUtil.updatePrimaryMaterial(bomData, logger, lineNo);
                        if(result.isBomUpdated()){
                            summary.updatedPrimaryCount++;
                            summary.successllyUpdatedBoms.add(bomData.get(GalaxyMigrationConstants.BOM_MASTER_ID));
                        }

                        switch (result.getStatus()) {
                            case VALIDATION_FAILED:
                                summary.validationPrimaryCount++;
                                summary.validationFailedMasterIds.add(bomData.get(GalaxyMigrationConstants.BOM_MASTER_ID));
                                break;
                            case EXCEPTION:
                                summary.exceptionPrimaryCount++;
                                summary.exceptionBomMasterIds.add(bomData.get(GalaxyMigrationConstants.BOM_MASTER_ID));
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        fileProcessedSuccessfully = false;  // Mark the file as failed
                        if (logger != null) {
                            logger.log("Error processing record: " + lineNo + " - Exception: " + e.getMessage());
                        }
                    }
                    lineNo++;
                }

                // After file processing, move it to the appropriate directory based on success
                if (fileProcessedSuccessfully) {
                    moveProcessedFile(entry, directoryPath);
                } else {
                    moveFailedFile(entry, directoryPath);  // Move to 'failed' folder if failed
                }
            } catch (IOException e) {
                System.err.println("Error reading file: " + entry.getFileName());
                e.printStackTrace();
                if (logger != null) {
                    logger.log("File read error: " + e.getMessage());
                }
                moveFailedFile(entry, directoryPath);  // Move file to 'failed' folder in case of read error
            }

        } catch (Exception e) {
            System.err.println("Logger initialization or processing error for file: " + entry.getFileName());
            e.printStackTrace();
            fileProcessedSuccessfully = false;
        } finally {
            // Close logger and ensure logs are flushed
            if (logger != null) {
                try {
                    if (fileProcessedSuccessfully) {
                        logger.log("Processing complete for file: " + entry.getFileName());
                        logger.log("File: " + entry.getFileName());
                        logger.log("SUCCESS: " + summary.updatedPrimaryCount);
                        logger.log("VALIDATION_FAILED: " + summary.validationPrimaryCount);
                        logger.log("EXCEPTION: " + summary.exceptionPrimaryCount);
                        logger.close();
                    } else {
                        logger.log("File processing failed for: " + entry.getFileName());
                    }
                    logger.close(); // Ensure logs are written to file
                } catch (Exception e) {
                    System.err.println("Error closing logger: " + e.getMessage());
                }
            }
        }
        return summary;
    }
    private static void moveProcessedFile(Path entry, String directoryPath) {
        Path processedDir = Paths.get(directoryPath, "processed");
        try {
            // Create processed directory if it doesn't exist
            Files.createDirectories(processedDir);
            Path targetPath = processedDir.resolve(entry.getFileName().toString() + "_" + System.currentTimeMillis()); // Append timestamp

            // Move the file to the processed directory
            Files.move(entry, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File moved to processed directory: " + targetPath);
        } catch (IOException e) {
            System.err.println("Error moving file to processed directory: " + entry.getFileName());
            e.printStackTrace();
        }
    }

    private static void moveFailedFile(Path entry, String directoryPath) {
        Path failedDir = Paths.get(directoryPath, "failed");
        try {
            // Create failed directory if it doesn't exist
            Files.createDirectories(failedDir);
            Path targetPath = failedDir.resolve(entry.getFileName().toString() + "_" + System.currentTimeMillis()); // Append timestamp

            // Move the file to the failed directory
            Files.move(entry, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File moved to failed directory: " + targetPath);
        } catch (IOException e) {
            System.err.println("Error moving file to failed directory: " + entry.getFileName());
            e.printStackTrace();
        }
    }
}
