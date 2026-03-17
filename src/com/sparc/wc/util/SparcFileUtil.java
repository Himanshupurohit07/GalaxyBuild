package com.sparc.wc.util;
import java.io.File;

import org.apache.logging.log4j.Logger;
import wt.log4j.LogR;

public class SparcFileUtil {

    private static final Logger LOGGER = LogR.getLogger(SparcFileUtil.class.getName());

    public static String extractExtension(String filePath) {
        LOGGER.debug("Extracting extension:" + filePath);
        if (filePath == null || filePath.equals("")) {
            return "";
        }
        if (!filePath.contains(".")) {
            return "";
        }
        return filePath.substring(filePath.lastIndexOf(".") + 1);
    }

    public static String getAbsoluteFileName(String baseDir, String fileName) {
        String absPath = baseDir;
        if (baseDir.endsWith(File.separator)) {
            return baseDir + fileName;
        }
        if (fileName.startsWith(File.separator)) {
            return baseDir + fileName;
        }
        return baseDir + File.separator + fileName;
    }
}
