package com.sparc.wc.exports.utils;

import com.sparc.wc.exports.domain.SparcLineSheetData;
import com.sparc.wc.exports.exceptions.SparcExportError;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import wt.log4j.LogR;

import java.io.File;

public final class SparcTemplateUtil {

    private static final Logger LOGGER           = LogR.getLogger(SparcTemplateUtil.class.getName());
    private static final String TEMPLATE_DIR_STR = "sparc_export_templates";
    private static final String TEMPLATE_DIR     = System.getenv("WT_HOME") + File.separator + "codebase" + File.separator + TEMPLATE_DIR_STR + File.separator;

    private SparcTemplateUtil() {

    }

    public static XSSFWorkbook readTemplate(final SparcLineSheetData.ExportType exportType, final SparcLineSheetData.Division division) throws SparcExportError {
        if (exportType == null || division == null) {
            throw new SparcExportError("Export Type and Division are required parameters for selecting an export template");
        }
        String templatePath = "";
        if(exportType == SparcLineSheetData.ExportType.HIGHLEVEL) {
        	templatePath = TEMPLATE_DIR + "HIGHLEVEL_APPAREL_template.xlsx";
        }
        else
         templatePath = TEMPLATE_DIR + exportType.toString() + "_" + division.toString() + "_template.xlsx";
        try {
            return new XSSFWorkbook(templatePath);
        } catch (Exception e) {
            throw new SparcExportError(e.getMessage());
        }
    }

    public static XSSFWorkbook readTemplate(final SparcLineSheetData.ExportType exportType, final SparcLineSheetData.Division division,Boolean isAppColorContext) throws SparcExportError {
        if (exportType == null || division == null) {
            throw new SparcExportError("Export Type and Division are required parameters for selecting an export template");
        }
        String templatePath = "";
		LOGGER.debug("isAppColorContext-------------------------"+isAppColorContext);
		
        if(exportType == SparcLineSheetData.ExportType.HIGHLEVEL && !isAppColorContext) {
            templatePath = TEMPLATE_DIR + "PRODUCT_HIGHLEVEL_APPAREL_template.xlsx";
        }
        else if(exportType == SparcLineSheetData.ExportType.HIGHLEVEL && isAppColorContext){
            templatePath = TEMPLATE_DIR + "HIGHLEVEL_APPAREL_template.xlsx";
        }
        else{
            templatePath = TEMPLATE_DIR + exportType.toString() + "_" + division.toString() + "_template.xlsx";
		}
		
		LOGGER.debug("templatePath-----------------------------"+templatePath);
        try {
            return new XSSFWorkbook(templatePath);
        } catch (Exception e) {
            throw new SparcExportError(e.getMessage());
        }
    }
}
