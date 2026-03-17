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




public class GalaxyPrimaryMaterialUpdate implements RemoteAccess {


    static class BomPrimaryUpdateSummary {
        int totalFiles = 0;
        int totalRecords =0 ;
        int updatedPrimaryCount = 0;
        int exceptionPrimaryCount = 0;
        int validationPrimaryCount = 0;
        int noBomLinksCount = 0;

        Set<String> successllyUpdatedBoms = new HashSet<>();
        Set<String> validationFailedMasterIds = new HashSet<>();
        Set<String> exceptionBomMasterIds = new HashSet<>();
        Set<String> noBomLinks =new HashSet<>();

        void add(BomPrimaryUpdateSummary other){
            totalFiles++;
            totalRecords += other.totalRecords;
            updatedPrimaryCount += other.updatedPrimaryCount;
            exceptionPrimaryCount += other.exceptionPrimaryCount;
            validationPrimaryCount += other.validationPrimaryCount;
            noBomLinksCount += other.noBomLinksCount;
        }
    }



    public static void main(String[] args) {

        try {
            // Check if the argument (directory path) is provided
            //windchill com.galaxy.wc.migration.loader.GalaxyPrimaryMaterialUpdate C:\PTC\Windchill_12.0\Windchill\codebase\PRIMEBOMEXTRACT\
            if (args.length > 0) {
                RemoteMethodServer remoteMethodServer = RemoteMethodServer.getDefault();
                String username = "wcadmin";
                GatewayAuthenticator auth = new GatewayAuthenticator();
                auth.setRemoteUser(username);
                remoteMethodServer.setAuthenticator(auth);
                Object[] objArgs = {args[0]};
                Class[] classArr = {String.class};
                if (remoteMethodServer != null) {
                    remoteMethodServer.invoke("loadPrimaryData", "com.galaxy.wc.migration.loader.GalaxyPrimaryMaterialUpdate", null, classArr, objArgs);

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
     * @param isPrimaryMaterial
     */
    public static void loadPrimaryData(String directoryPath) {
        Path path = Paths.get(directoryPath);
        BomPrimaryUpdateSummary grandTotal = new BomPrimaryUpdateSummary();
        Path logsDirPath = Paths.get(directoryPath, "Primarylogs");

        try (Stream<Path> files = Files.list(path)) {
            files.filter(Files::isRegularFile)
                    .forEach(entry -> {
                        BomPrimaryUpdateSummary summary = processPrimaryMaterialUpdate(entry, directoryPath);
                        grandTotal.add(summary);
                    });
            writeFinalPrimarySummary(logsDirPath, grandTotal);

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
    private static void writeFinalPrimarySummary(Path logsDirPath, BomPrimaryUpdateSummary summary) {
        Path reportPath = logsDirPath.resolve("Summary_Report.txt");

        List<String> lines = List.of(
                "=========== BOM Processing Summary ===========",
                "Total Files Processed        : " + summary.totalFiles,
                "Total Records Processed      : " + summary.totalRecords,
                "Successful BOM Creations     : " + summary.updatedPrimaryCount,
                "Validation Failures          : " + summary.validationPrimaryCount,
                "No Bom Links Count           :" + summary.noBomLinksCount,
                "Exceptions During Processing : " + summary.exceptionPrimaryCount,
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


    private static BomPrimaryUpdateSummary processPrimaryMaterialUpdate(Path entry, String directoryPath) {
        System.out.println("processFile file: " + entry.getFileName());

        Set<String> processedIds = new HashSet<>();

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


                    if (processedIds.contains(bomData.get(GalaxyMigrationConstants.BOM_MASTER_ID))) {
                        lineNo++;
                        continue;
                    }
                    try {
                        BomPrimaryUpdateResult result = GalaxyBomPartUtil.updatePrimaryMaterial(bomData, logger, lineNo);
                        if(result.isBomUpdated()){
                            summary.updatedPrimaryCount++;
                            summary.successllyUpdatedBoms.add(bomData.get(GalaxyMigrationConstants.BOM_MASTER_ID));
                            processedIds.add(bomData.get(GalaxyMigrationConstants.BOM_MASTER_ID));
                        }

                        switch (result.getStatus()) {
                            case VALIDATION_FAILED:
                                summary.validationPrimaryCount++;
                                summary.validationFailedMasterIds.add(bomData.get(GalaxyMigrationConstants.BOM_MASTER_ID));
                                break;
                            case NO_BOM_LINKS:
                                summary.noBomLinksCount++;
                                summary.noBomLinks.add(bomData.get(GalaxyMigrationConstants.BOM_MASTER_ID));
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
                writeSetToFile(logsDirPath, "UpdatedBoms.txt", summary.successllyUpdatedBoms);
                writeSetToFile(logsDirPath, "ValidationFailedBoms.txt", summary.validationFailedMasterIds);
                writeSetToFile(logsDirPath, "ExceptionBoms.txt", summary.exceptionBomMasterIds);
                writeSetToFile(logsDirPath, "NoBomLinksBoms.txt", summary.noBomLinks);
                
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
                        logger.log("No BomLinks Count :"+summary.noBomLinksCount);
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

    private static void writeSetToFile(Path logsDirPath, String fileName, Set<String> dataSet) {
        Path filePath = logsDirPath.resolve(fileName);

        try {
            List<String> lines = new ArrayList<>(dataSet);
            Files.write(filePath, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Set data written to: " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + filePath);
            e.printStackTrace();
        }
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
