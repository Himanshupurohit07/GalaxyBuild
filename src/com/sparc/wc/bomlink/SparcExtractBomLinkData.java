package com.sparc.wc.bomlink;

import com.lcs.wc.flexbom.FlexBOMPart;
import com.sparc.wc.bomlink.processor.BOMProcessor;
import com.sparc.wc.bomlink.processor.DefaultBOMProcessor;
import com.sparc.wc.bom.helper.SparcBomHelper;
import wt.method.RemoteMethodServer;
import wt.method.RemoteAccess;
import wt.httpgw.GatewayAuthenticator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SparcExtractBomLinkData implements RemoteAccess {

    public static void main(String[] args) {
        try {

            //windchill com.sparc.wc.bomlink.SparcExtractBomLinkData "Season\\scSeasonReebok\\scRbkApparel"
            //windchill com.sparc.wc.bomlink.SparcExtractBomLinkData "Season\\scSeasonReebok\\scFootwear"
            RemoteMethodServer remoteMethodServer = RemoteMethodServer.getDefault();
            GatewayAuthenticator auth = new GatewayAuthenticator();
            auth.setRemoteUser("wcadmin");
            remoteMethodServer.setAuthenticator(auth);
            remoteMethodServer.invoke("processFlexBOM", "com.sparc.wc.bomlink.SparcExtractBomLinkData", null,
                    new Class[]{String.class,String.class}, new Object[]{args[0],args[1]});

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param typePath
     */
    public static void processFlexBOM(String typePath,String seasonName) {
        List<String> sectionApparelOrder = Arrays.asList("mainmaterials", "sctrims", "sclining", "scartwork", "scprocess", "labels", "packaging");
        List<String> sectionFootwearOrder = Arrays.asList("scFwUpper", "scSockliner", "scInsole", "scBottom", "scsundries", "scPackaging");

        // Determine which section order to pass based on typePath
        List<String> sectionOrder = null;
        String directoryPath="";
        String strProductType = "";
        Boolean isApparel = false;
        if (typePath.contains("scRbkApparel")) {
            sectionOrder = sectionApparelOrder;
            directoryPath = "Apparel";
            strProductType = "Product\\scApparel\\scReebok";
            isApparel = true;
        } else if (typePath.contains("scFootwear")) {
            sectionOrder = sectionFootwearOrder;
            directoryPath = "Footwear";
            strProductType = "Product\\scFootwear\\scFootwearReebok";
            isApparel = false;
        }

        Set<FlexBOMPart> bomParts = SparcBomHelper.extractFlexBomParts(typePath,seasonName);

        System.out.println("BomParts---Count-----"+bomParts.size());
        BOMProcessor processor = new DefaultBOMProcessor("BOMLinkExtract",directoryPath,isApparel);
        ZonedDateTime estTime = ZonedDateTime.now(ZoneId.of("America/New_York"));
        // Define the format pattern (hh:mm:ss.SSS)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        // Format the EST time
        String formattedTime = estTime.format(formatter);
        System.out.println("Start time----------------------"+formattedTime);
        //bomParts.add(objBOMPart);
        processor.process(bomParts,sectionOrder);
        estTime = ZonedDateTime.now(ZoneId.of("America/New_York"));
        System.out.println("End time----------------------"+estTime.format(formatter));
    }
}

