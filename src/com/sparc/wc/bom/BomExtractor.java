package com.sparc.wc.bom;

import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.util.FormatHelper;
import com.sparc.wc.bom.helper.SparcBomHelper;
import com.sparc.wc.bom.processor.ProductBOMProcessor;
import com.sparc.wc.bom.services.impl.*;
import wt.httpgw.GatewayAuthenticator;

import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;

import java.util.Set;

public class BomExtractor implements RemoteAccess {




    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        try {

            //windchill com.sparc.wc.bom.BomExtractor "Season\\scSeasonReebok\\scFootwear" "Reebok Footwear Fall/Winter 2026"
            RemoteMethodServer remoteMethodServer = RemoteMethodServer.getDefault();
            String username ="wcadmin";
            GatewayAuthenticator auth = new GatewayAuthenticator();
            auth.setRemoteUser(username);
            remoteMethodServer.setAuthenticator(auth);
            Object[] objArgs = {args[0],args[1],args[2],args[3]};
            Class[] classArr = {String.class,String.class,String.class,String.class};
            if(remoteMethodServer != null) {
                remoteMethodServer.invoke("extractBOMData", "com.sparc.wc.bom.BomExtractor", null, classArr, objArgs);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param path
     * @param seasonName
     */
    public static void extractBOMData(String path,String seasonName,String userInputDate,String isPrimaryMaterial) {
        System.out.println("Extract BOM Data Start");


        Boolean isPrimary = Boolean.parseBoolean(isPrimaryMaterial);

        TextFileBomExportService exportService = new TextFileBomExportService();

        ProductBOMProcessor processor = new ProductBOMProcessor(
                new DefaultSeasonService(),
                new DefaultProductService(),
                new DefaultSourcingService(),
                new DefaultSpecificationService(),
                new DefaultBomService(),
                exportService
        );

        if(isPrimary){
            Set<FlexBOMPart> bomParts = SparcBomHelper.extractFlexBomParts(path,"");
            new PrimaryMaterialExportService().exportPrimaryMaterial(bomParts);
        }
        else {
            if (FormatHelper.hasContent(userInputDate)) {
                processor.extractDeltaData(path, seasonName, userInputDate);
            } else {
                processor.extractData(path, seasonName);
            }
        }

        exportService.close();
        System.out.println("Extract BOM Data End");
    }
}
