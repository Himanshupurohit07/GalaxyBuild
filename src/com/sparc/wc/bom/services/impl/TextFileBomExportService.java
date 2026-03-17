package com.sparc.wc.bom.services.impl;

import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.bom.BomPart;
import com.sparc.wc.bom.constants.SparcMigrationConstants;
import com.sparc.wc.bom.services.BomExportService;
import wt.util.WTProperties;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static com.sparc.wc.bom.constants.SparcMigrationConstants.*;

import  com.lcs.wc.util.FormatHelper;

public class TextFileBomExportService implements BomExportService {

    public static final String COMMA_DELIMITER = ",";
    private static final int MAX_ROWS_PER_FILE = 5000;

    private int fileIndex = 1;
    private int currentRowCount = 0;
    private BufferedWriter writer;

    private static String wt_home = "";
    private static String baseDir = "";

    static {
        try {
            WTProperties wtProperties = WTProperties.getLocalProperties();
            wt_home = wtProperties.getProperty("wt.home");
            baseDir = wt_home + File.separator + "codebase" + File.separator + "BOMEXTRACT";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void export(List<BomPart> objBOMS,String seasonName,String seasTypePath){

        try{
            File seasonFolder = new File(baseDir + File.separator + seasonName.replaceAll("[ /]", "_"));
            if (!seasonFolder.exists()) {
                seasonFolder.mkdirs();  // Create the folder if it doesn't exist
            }
            String seasonFolderPath = seasonFolder.getAbsolutePath();
            if (writer == null) {
                openNewFile(seasonFolderPath, seasonName); // Open new file and write header
            }

            String strBOMid = "";
            for (BomPart bom : objBOMS) {
                if (currentRowCount >= MAX_ROWS_PER_FILE) {
                    rotateFile(seasonFolderPath, seasonName);  // Rotate files
                }
                strBOMid = FormatHelper.getNumericObjectIdFromObject(bom.getObjBomPart().getMaster());
                StringBuilder line = new StringBuilder();
                LCSProduct objProduct = (LCSProduct) VersionHelper.latestIterationOf((LCSPartMaster)bom.getObjBomPart().getOwnerMaster());


                line.append(seasonName)
                        .append(SPECIAL_DELIMITER)
                 .append(seasTypePath)
                        .append(SPECIAL_DELIMITER)
                 .append(bom.getStrProductNo()).
                        append(SPECIAL_DELIMITER)
                 .append(objProduct.getFlexType().getFullNameDisplay(true))
                 .append(SPECIAL_DELIMITER)
                 .append(bom.getStrSourcingNo())
                        .append(SPECIAL_DELIMITER)
				  .append(bom.getStrSourceName())
                        .append(SPECIAL_DELIMITER)
                 .append(bom.getStrSpecName())
                        .append(SPECIAL_DELIMITER)
                 .append(bom.getStrSpecId())
                        .append(SPECIAL_DELIMITER)
                 .append(bom.getObjBomPart()
                         .getValue(BOM_PART_NAME)).append(SPECIAL_DELIMITER)
                 .append(strBOMid)
                        .append(SPECIAL_DELIMITER)
                 .append(bom.getPartNumber())
                        .append(SPECIAL_DELIMITER)
                 .append(bom.getObjBomPart().getFlexType().getFullNameDisplay(true))
                 .append(SPECIAL_DELIMITER)
                 .append(bom.getStrBOMStatus())
                 .append(SPECIAL_DELIMITER)
                 .append(bom.getPrimaryOrNot())
                        .append(SPECIAL_DELIMITER)
                    .append(bom.getStrMaterialNo())
                    .append(SPECIAL_DELIMITER)
                    .append(bom.getStrMaterialDesc())
                    .append(SPECIAL_DELIMITER)
                    .append(bom.getStrSupplierNo());
                //FlexBOMPart^SeasonName^SeasonTypePath^ProductNo^ProductTypePath^SourcingNo^specName^SpecMasterID^BOMPARTNAME^BOMMASTERID^BOMPARTNUMBER^BOMTYPEPATH^BOMStatus^isPrimary^
                writer.write(line.toString().trim());
                writer.newLine();
                currentRowCount++;
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void setFileIndex() {
        this.fileIndex = 1;
        this.currentRowCount = 0;
        closeFile();  // Reset writer to ensure header will be added when reopened
    }

    private void rotateFile(String seasonFolderPath, String seasonName) throws IOException {
        closeFile();
        fileIndex++;
        openNewFile(seasonFolderPath, seasonName);
    }

    private void openNewFile(String seasonFolderPath, String seasonName) {
        try {
            String fileSuffix = (fileIndex > 0) ? "_" + fileIndex : "";
            String fileName = seasonFolderPath + File.separator + "BOM_export_" + seasonName.replaceAll("[ /]", "_") + fileSuffix + ".txt";
            writer = new BufferedWriter(new FileWriter(fileName, false));  // overwrite if rotating

            // Write header
            StringBuilder header = new StringBuilder();
            header.append(SEASON_NAME).
                    append(SPECIAL_DELIMITER)
                    .append("SeasonTypePath").append(SPECIAL_DELIMITER)
                    .append(PRODUCT_NO).append(SPECIAL_DELIMITER)
                    .append("ProductTypePath").append(SPECIAL_DELIMITER)
                    .append(SOURCING_NO).append(SPECIAL_DELIMITER)
                    .append("SOURCENAME").append(SPECIAL_DELIMITER)
                    .append(SPEC_NAME).append(SPECIAL_DELIMITER)
                    .append(SPEC_BRANCH_ID).append(SPECIAL_DELIMITER)
                    .append(BOM_NAME).append(SPECIAL_DELIMITER)
                    .append(BOM_BRANCH_ID).append(SPECIAL_DELIMITER)
                    .append(BOM_PART_NO_DISPLAY).append(SPECIAL_DELIMITER)
                    .append(BOM_TYPE_DISPLAY_NAME).append(SPECIAL_DELIMITER)
                    .append("BOMStatus").append(SPECIAL_DELIMITER)
                    .append(IS_PRIMARY_BOM)
                    .append(SPECIAL_DELIMITER)
                    .append("PRIMARY_MATERIAL_NO")
                    .append(SPECIAL_DELIMITER)
                    .append(PRIM_MATERIAL_DESCRIPTION)
                    .append(SPECIAL_DELIMITER)
                    .append("SUPPLIER_NO");
            writer.write(header.toString());
            writer.newLine();
            currentRowCount = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeFile() {
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
                writer = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        closeFile();
    }
}
