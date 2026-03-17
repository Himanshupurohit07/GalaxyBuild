package com.sparc.wc.costsheet.uploader;

import com.lcs.wc.client.ClientContext;
import com.sparc.wc.exports.constants.SparcExportConstants;
import com.sparc.wc.util.SparcCostingUploadUtil;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.season.LCSSeason;
import com.sparc.wc.util.SparcFileUtil;
import org.apache.logging.log4j.Logger;
import wt.log4j.LogR;
import wt.util.WTException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class SPARCCostSheetUploader {

    public static void processFile(String fileName,String strDivision,String seasonId){
        System.out.println("Start of the Process File");
        SPARCCostSheetModifier sparcCostSheetModifier = new SPARCCostSheetModifier();
        String extension = SparcFileUtil.extractExtension(fileName);
        if (extension.equals("")) {
            return;
        }
        String userName = "";
        LCSSeason objSeason = null;
        String seasonName = "";
        try {
            userName = ClientContext.getContext().getUser().getName();
            objSeason = (LCSSeason)LCSQuery.findObjectById(seasonId);
            seasonName = objSeason.getName();
        }catch(WTException ex){
            ex.printStackTrace();
        }
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
        String timestamp = now.format(formatter);
        String file_name = String.format("data_%s", now.format(DateTimeFormatter.ofPattern("MMM_yyyy-dd_HH.mm.ss")), timestamp);       
        String strBO_Name= seasonName +"_" + "DETAILBREAKDOWN" + "_" +userName+ "_" + file_name;

        String businessObjectId = SparcCostingUploadUtil.createDetailedBusinessObject(strBO_Name,strDivision);

        switch (extension) {
            case "zip":
                sparcCostSheetModifier.processZipFile(fileName,strDivision,businessObjectId,seasonName);
                break;
            case "xlsx":
                sparcCostSheetModifier.upload(fileName,strDivision,businessObjectId,false);
                break;
            case "xls":
        }
        System.out.println("End of the Process File");
    }
}
