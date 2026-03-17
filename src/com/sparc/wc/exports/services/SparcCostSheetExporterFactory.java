package com.sparc.wc.exports.services;

import com.sparc.tc.abstractions.Interpreter;
import com.sparc.tc.abstractions.TemplateBuilder;
import com.sparc.tc.domain.Template;
import com.sparc.tc.impl.InterpreterImpl;
import com.sparc.tc.impl.PlaceholderParserImpl;
import com.sparc.tc.impl.TemplateBuilderImpl;
import com.sparc.wc.exports.constants.SparcExportConstants;
import com.sparc.wc.exports.domain.SparcLineSheetData;
import com.sparc.wc.exports.exceptions.SparcExportError;
import com.sparc.wc.exports.services.impl.SparcDetailedCostSheetExporterImpl;
import com.sparc.wc.exports.services.impl.SparcHighLevelCostSheetExporterImpl;
import com.sparc.wc.exports.utils.SparcLineSheetUtil;
import com.sparc.wc.exports.utils.SparcStringUtils;
import com.sparc.wc.exports.utils.SparcTemplateUtil;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import wt.log4j.LogR;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class SparcCostSheetExporterFactory {

    private static final Logger LOGGER = LogR.getLogger(SparcCostSheetExporterFactory.class.getName());

    private SparcCostSheetExporterFactory() {

    }

    public static SparcLineSheetExporter getExporter(final String exportType, final String division, final String seasonOid, final String costSheetOids,final Boolean isAppColorContext) throws SparcExportError, IOException {
        if (!SparcStringUtils.hasContent(exportType) || !SparcStringUtils.hasContent(division)) {
            throw new SparcExportError("Cannot get cost sheet exporter without valid export type and division");
        }
        final SparcLineSheetData.ExportType type = SparcStringUtils.stringToEnum(exportType.toUpperCase(), SparcLineSheetData.ExportType.class);
        if (type == null) {
            throw new SparcExportError(exportType + " is not a valid export type");
        }
        final SparcLineSheetData.Division div = SparcStringUtils.stringToEnum(division.toUpperCase(), SparcLineSheetData.Division.class);
        if (div == null) {
            throw new SparcExportError(division + " is not a valid division");
        }
		LOGGER.debug("SparcCostSheetExporterFactory------type--------------"+type);
		LOGGER.debug("SparcCostSheetExporterFactory------div--------------"+div);
		LOGGER.debug("SparcCostSheetExporterFactory------isAppColorContext--------------"+isAppColorContext);
        final XSSFWorkbook templateFile = SparcTemplateUtil.readTemplate(type, div,isAppColorContext);
        if (templateFile == null) {
            throw new SparcExportError("Cannot generate an export, as the templateFile is not found");
        }
        final Template           template        = new Template(templateFile);
        final SparcLineSheetData lineSheetData   = SparcLineSheetUtil.parse(seasonOid, division, exportType, costSheetOids);
        final Interpreter        interpreter     = new InterpreterImpl(new PlaceholderParserImpl());
        final TemplateBuilder    templateBuilder = new TemplateBuilderImpl(interpreter);
        final List<Short>        sheetIndices    = getSheetIndices(type, div);
        LOGGER.debug("Assembling exporter with sheet-indices:" + sheetIndices);
        switch (type) {
            case HIGHLEVEL:
                return new SparcHighLevelCostSheetExporterImpl(interpreter, templateBuilder, sheetIndices, lineSheetData, template,isAppColorContext);
             case DETAILED:
                return new SparcDetailedCostSheetExporterImpl(interpreter, templateBuilder, sheetIndices, lineSheetData, template);
            default:
                return null;
        }
    }

    private static List<Short> getSheetIndices(final SparcLineSheetData.ExportType exportType, final SparcLineSheetData.Division division) {
        if (exportType == SparcLineSheetData.ExportType.DETAILED && division == SparcLineSheetData.Division.FOOTWEAR) {
            return SparcExportConstants.FOOTWEAR_DETAILED_BREAKDOWN_SHEETS;
        } else if (exportType == SparcLineSheetData.ExportType.DETAILED && division == SparcLineSheetData.Division.APPAREL) {
            return SparcExportConstants.APPAREL_DETAILED_BREAKDOWN_SHEETS;
        }
        else if (exportType == SparcLineSheetData.ExportType.HIGHLEVEL) {
        	if(division == SparcLineSheetData.Division.NONAPPAREL) {
                return SparcExportConstants.NONAPPAREL_HIGHLEVEL_SHEETS;
        }else if(division == SparcLineSheetData.Division.APPAREL) {
                return SparcExportConstants.APPAREL_HIGHLEVEL_SHEETS;
        }else if(division == SparcLineSheetData.Division.LICENSED) {
                return SparcExportConstants.LICENSED_HIGHLEVEL_SHEETS;
        }else if(division == SparcLineSheetData.Division.HARDWARE) {
            return SparcExportConstants.HARDWARE_HIGHLEVEL_SHEETS;
        	} 
			
		else if(division == SparcLineSheetData.Division.FOOTWEAR) {
                return SparcExportConstants.HARDWARE_HIGHLEVEL_SHEETS;
            }
        }
        return null;
    }

}
