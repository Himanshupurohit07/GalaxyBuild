package com.sparc.wc.bom.services.impl;

import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.material.LCSMaterialSupplier;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.bom.BomPart;
import wt.util.WTProperties;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.sparc.wc.bom.constants.SparcMigrationConstants.*;
import static com.sparc.wc.bom.constants.SparcMigrationConstants.SPECIAL_DELIMITER;

public class PrimaryMaterialExportService {


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
            baseDir = wt_home + File.separator + "codebase" + File.separator + "PRIMEBOMEXTRACT";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void exportPrimaryMaterial(Set<FlexBOMPart> objBomParts){

        try{
            File seasonFolder = new File(baseDir);
            if (!seasonFolder.exists()) {
                seasonFolder.mkdirs();  // Create the folder if it doesn't exist
            }
            String seasonFolderPath = seasonFolder.getAbsolutePath();
            if (writer == null) {
                openNewFile(seasonFolderPath); // Open new file and write header
            }

            String strBOMid = "";
            String strMatNo = "";
            String strDesc = "";
            String strSuppNo = "";
            for (FlexBOMPart bom : objBomParts) {
                if (currentRowCount >= MAX_ROWS_PER_FILE) {
                    rotateFile(seasonFolderPath);  // Rotate files
                }
                strBOMid = FormatHelper.getNumericObjectIdFromObject(bom.getMaster());
                StringBuilder line = new StringBuilder();
                LCSProduct objProduct = (LCSProduct) VersionHelper.latestIterationOf((LCSPartMaster)bom.getOwnerMaster());

                if(bom.getValue(PRIM_MATERIAL) != null){
                    LCSMaterialSupplier objMatSupplier= (LCSMaterialSupplier) bom.getValue(PRIM_MATERIAL);
                    LCSMaterial primMaterial= (LCSMaterial) VersionHelper.latestIterationOf(objMatSupplier.getMaterialMaster());
                    strMatNo = primMaterial.getValue(MATERIAL_NO).toString();
                    strDesc = (String)bom.getValue("pmDescription");
                    LCSSupplier objSupplier = (LCSSupplier) VersionHelper.latestIterationOf(objMatSupplier.getSupplierMaster());
                    strSuppNo = objSupplier.getValue(PLM_VEND_NO).toString();
                }else{
                    continue;
                }
                line.append(objProduct.getValue("scPLMProductNo").toString())
                                .append(SPECIAL_DELIMITER)
                                        .append(bom.getName())
                                                .append(SPECIAL_DELIMITER)
                                                        .append(strBOMid)
                                                                .append(SPECIAL_DELIMITER)
                                                                        .append(strMatNo).append(SPECIAL_DELIMITER)
                                .append(strDesc).append(SPECIAL_DELIMITER)
                                .append(strSuppNo);

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


    public void setFileIndex() {
        this.fileIndex = 1;
        this.currentRowCount = 0;
        closeFile();  // Reset writer to ensure header will be added when reopened
    }

    private void rotateFile(String seasonFolderPath) throws IOException {
        closeFile();
        fileIndex++;
        openNewFile(seasonFolderPath);
    }

    private void openNewFile(String seasonFolderPath) {
        try {
            String fileSuffix = (fileIndex > 0) ? "_" + fileIndex : "";
            Random rand = new Random();
            int randomInt = rand.nextInt();

            String fileName = seasonFolderPath + File.separator + "PRIME_BOM_EXPORT_"+randomInt+".txt";
            writer = new BufferedWriter(new FileWriter(fileName, false));  // overwrite if rotating

            // Write header
            StringBuilder header = new StringBuilder();
            header.append("#PRODUCT_NO").
                    append(SPECIAL_DELIMITER)
                            .append("BOM_NAME")
                                    .append(SPECIAL_DELIMITER)
                                            .append("BOM_MASTER_ID")
                                                    .append(SPECIAL_DELIMITER)
                                                            .append("PRIMARY_MAT_NO")
                                                                    .append(SPECIAL_DELIMITER)
                                                                            .append("PRIM_SUPP_NO");

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

