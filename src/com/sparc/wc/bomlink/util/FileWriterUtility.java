package com.sparc.wc.bomlink.util;

import com.sparc.wc.bom.constants.SparcMigrationConstants;
import wt.util.WTProperties;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileWriterUtility {

    private static final int MAX_ROWS = 3000;
    private BufferedWriter writer;
    private int currentRowCount;
    private String currentFilePath;
    private final File directory;
    private static String wt_home = "";
    private static String baseDir = "";

    static {
        try {
            WTProperties wtProperties = WTProperties.getLocalProperties();
            wt_home = wtProperties.getProperty("wt.home");
            baseDir = wt_home + File.separator + "codebase" + File.separator;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public FileWriterUtility(String directoryPath, String subDirectory) {
        this.directory = new File(baseDir + directoryPath + File.separator + subDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        this.currentFilePath = generateNewFileName();
        this.writer = openFile(this.currentFilePath);
    }

    private BufferedWriter openFile(String filePath) {
        try {
            return new BufferedWriter(new FileWriter(filePath, true));
        } catch (IOException e) {
            throw new RuntimeException("Unable to open file for writing: " + filePath, e);
        }
    }

    private String generateNewFileName() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return directory.getPath() + File.separator + "BOMLinkExtract_" + timestamp + ".txt";
    }

    public boolean canFitRows(int numberOfRows) {
        return (currentRowCount + numberOfRows) <= MAX_ROWS;
    }

    private void rotateFile() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentFilePath = generateNewFileName();
        writer = openFile(currentFilePath);
        currentRowCount = 0;
    }

    public void writeBomPartRows(String header, List<String> rows) {
        try {
            if (!canFitRows(rows.size())) {
                rotateFile();
            }
            for (String row : rows) {
                writer.write(header + SparcMigrationConstants.SPECIAL_DELIMITER + row+SparcMigrationConstants.SPECIAL_DELIMITER+currentRowCount);
                writer.newLine();
                currentRowCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void flush() {
        try {
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void closeFile() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
