package com.sparc.wc.bom.processor;

import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.material.LCSMaterialSupplier;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.specification.FlexSpecLogic;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecToComponentLink;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.bom.BomPart;
import com.sparc.wc.bom.constants.SparcMigrationConstants;
import com.sparc.wc.bom.services.*;
import static com.sparc.wc.bom.constants.SparcMigrationConstants.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ProductBOMProcessor {

    private final SeasonService seasonService;
    private final ProductService productService;
    private final SourcingService sourcingService;
    private final SpecificationService specificationService;
    private final BomService bomService;
    private final BomExportService bomExportService;

    /**
     *
     * @param ss
     * @param ps
     * @param srcs
     * @param specs
     * @param bService
     * @param bES
     */
    public ProductBOMProcessor(SeasonService ss, ProductService ps, SourcingService srcs,
                               SpecificationService specs, BomService bService,BomExportService bES) {
        this.seasonService = ss;
        this.productService = ps;
        this.sourcingService = srcs;
        this.specificationService = specs;
        this.bomService = bService;
        this.bomExportService =bES;
    }


    /**
     *
     * @param typePath
     */
    public void extractData(String typePath,String seasonName) {
        System.out.println("Start of the Product BOM Data Processor");
        try {
            Set<LCSSeason> seasons = new HashSet<>();
            System.out.println("extractData-----seasonName------------"+seasonName);
            if(FormatHelper.hasContent(seasonName)){
                seasons.add(seasonService.getSeason(typePath,seasonName));
            }else {
                seasons = seasonService.getReebokSeasons(typePath);
            }
            Collection<FlexBOMPart> specBOMS = new ArrayList<>();
            Collection<FlexBOMPart> boms = new ArrayList<>();
            BomPart bomPart = null;
            List<BomPart> dataBoms = new ArrayList<>();
            int count = 0;
            Set<FlexBOMPart> allBoms = new HashSet<>();
            for(LCSSeason objSeason:seasons) {
                Collection<LCSProduct> products = productService.getProductsForSeason(objSeason);
                this.bomExportService.setFileIndex();
                specBOMS.clear();
                for (LCSProduct product : products) {
                    dataBoms.clear();
                    allBoms.clear();
                    Collection<LCSSourcingConfig> sources = sourcingService.getSourcingConfigs(product, objSeason);
                    for (LCSSourcingConfig source : sources) {
                        Collection<FlexSpecification> specs = specificationService.getSpecifications(product, source, objSeason);
                        for (FlexSpecification spec : specs) {
                            specBOMS.clear();
                            specBOMS = bomService.getBOMParts(product, source, spec);
                            for(FlexBOMPart objBOMPart : specBOMS){
                                allBoms.add(objBOMPart);
                                dataBoms.add(createBomPart(product, source, spec, objBOMPart,typePath));
                            }
                        }
                    }
                    boms.clear();
                    boms = bomService.getBOMParts(product, null, null);
                    boms.removeAll(allBoms);
                    for(FlexBOMPart objBOMPart:boms){
                        dataBoms.add(createBomPart(product, null, null, objBOMPart,typePath));
                    }
                    if(!dataBoms.isEmpty()) {
                        dataBoms.sort(Comparator.comparingInt(o -> {
                            String partNumber = o.getPartNumber();
                            if (partNumber == null || partNumber.isBlank()) {
                                return Integer.MAX_VALUE; // or 0, depending on where you want nulls to sort
                            }
                            return Integer.parseInt(partNumber);
                        }));
                        bomExportService.export(dataBoms, objSeason.getName(), objSeason.getFlexType().getFullNameDisplay(true));
                    }
                }
                count = count + dataBoms.size();            }
            System.out.println("BOM Total count is -------------------"+count);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("End of the Product BOM Data Processor");
    }


    /**
     *
     * @param typePath
     */
    public void extractDeltaData(String typePath,String seasonName,String userInputDate) {
        System.out.println("Start of the Product BOM Data Processor");
        try {
            Set<LCSSeason> seasons = new HashSet<>();
            if(FormatHelper.hasContent(seasonName)){
                seasons.add(seasonService.getSeason(typePath,seasonName));
            }else {
                seasons = seasonService.getReebokSeasons(typePath);
            }
            Collection<FlexBOMPart> specBOMS = new ArrayList<>();
            Collection<FlexBOMPart> boms = new ArrayList<>();
            Collection<FlexBOMPart> spBoms = new ArrayList<>();
            Collection<FlexBOMPart> specLessBoms = new ArrayList<>();
            BomPart bomPart = null;
            List<BomPart> dataBoms = new ArrayList<>();
            for(LCSSeason objSeason:seasons) {
                Collection<LCSProduct> products = productService.getProductsForSeason(objSeason);
                this.bomExportService.setFileIndex();
                specBOMS.clear();
                boms.clear();
                for (LCSProduct product : products) {
                    dataBoms.clear();
                    Collection<LCSSourcingConfig> sources = sourcingService.getSourcingConfigs(product, objSeason);
                    for (LCSSourcingConfig source : sources) {
                        Collection<FlexSpecification> specs = specificationService.getSpecifications(product, source, objSeason);
                        for (FlexSpecification spec : specs) {
                            specBOMS.clear();
                            spBoms.clear();
                            specBOMS = bomService.getBOMParts(product, source, spec);
                            System.out.println("Start of the Modified BOM");
                            spBoms =getModBoms(specBOMS,userInputDate);
                            System.out.println("End of the Modified BOM");
                            for(FlexBOMPart objBOMPart : spBoms){
                                dataBoms.add(createBomPart(product, source, spec, objBOMPart,typePath));
                            }
                            System.out.println("BOM Parts: " + specBOMS);
                        }
                    }
                    boms.clear();
                    boms = bomService.getBOMParts(product, null, null);
                    specLessBoms.clear();
                    specLessBoms =getModBoms(boms,userInputDate);
                    specLessBoms.removeAll(spBoms);

                    for(FlexBOMPart objBOMPart:specLessBoms){
                        dataBoms.add(createBomPart(product, null, null, objBOMPart,typePath));
                    }
                    if(!dataBoms.isEmpty()) {
                        dataBoms.sort(Comparator.comparingInt(o -> {
                            String partNumber = o.getPartNumber();
                            if (partNumber == null || partNumber.isBlank()) {
                                return Integer.MAX_VALUE; // or 0, depending on where you want nulls to sort
                            }
                            return Integer.parseInt(partNumber);
                        }));
                        bomExportService.export(dataBoms, objSeason.getName(), objSeason.getFlexType().getFullNameDisplay(true));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("End of the Product BOM Data Processor");
    }

    /**
     *
     * @param product
     * @param source
     * @param spec
     * @param bom
     * @return
     */
    private BomPart createBomPart(LCSProduct product, LCSSourcingConfig source,
                                  FlexSpecification spec, FlexBOMPart bom,String strTypePath) {

        BomPart bomPart = new BomPart();
        try {
            bomPart.setObjBomPart(bom);
            Object prodNoObj = product.getValue(PROD_NO_ATTRIBUTE);
            Long prodNo = (prodNoObj instanceof Long) ? (Long) prodNoObj : null;
            bomPart.setStrProductNo(prodNo != null ? String.valueOf(prodNo) : EMPTY_SPACE);
            if(FormatHelper.equalsWithNull(source,null)){
                bomPart.setStrSourcingNo(EMPTY_SPACE);
                bomPart.setStrSourceName(EMPTY_SPACE);
            }
            else {
                bomPart.setStrSourcingNo(FormatHelper.getNumericObjectIdFromObject(source.getMaster()));
                bomPart.setStrSourceName((String) source.getValue("name"));
            }
            if(FormatHelper.equalsWithNull(spec,null)){
                bomPart.setStrSpecId(EMPTY_SPACE);
                bomPart.setStrSpecName(EMPTY_SPACE);
            }
            else{
                bomPart.setStrSpecId(FormatHelper.getNumericObjectIdFromObject(spec.getMaster()));
                bomPart.setStrSpecName(spec.getName());
            }
            Object partNumberObj = bom.getValue(LCSFlexBOMQuery.BOM_NUM_KEY);
            String partNumber = (partNumberObj instanceof String) ? (String) partNumberObj : EMPTY_SPACE;
            bomPart.setPartNumber(partNumber);
            if (spec != null) {
                FlexSpecToComponentLink component = FlexSpecQuery.getSpecToComponentLink(spec, bom);
                bomPart.setPrimaryOrNot(component.isPrimaryComponent());
            } else {
                bomPart.setPrimaryOrNot(false);
            }
            if(strTypePath.contains(SC_FOOTWEAR)){
                bomPart.setStrBOMStatus(EMPTY_SPACE);
            }else{
                bomPart.setStrBOMStatus((String)bom.getValue(BOM_STATUS));
            }

            if(bom.getValue(PRIM_MATERIAL) != null){
                LCSMaterialSupplier objMatSupplier= (LCSMaterialSupplier) bom.getValue(PRIM_MATERIAL);
                LCSMaterial primMaterial= (LCSMaterial) VersionHelper.latestIterationOf(objMatSupplier.getMaterialMaster());
                bomPart.setStrMaterialNo((primMaterial.getValue(MATERIAL_NO).toString()));
                bomPart.setStrMaterialDesc((String)bom.getValue("pmDescription"));
                LCSSupplier objSupplier = (LCSSupplier) VersionHelper.latestIterationOf(objMatSupplier.getSupplierMaster());
                bomPart.setStrSupplierNo(objSupplier.getValue(PLM_VEND_NO).toString());
            }else{
                bomPart.setStrMaterialNo(EMPTY_SPACE);
                bomPart.setStrMaterialDesc(EMPTY_SPACE);
                bomPart.setStrSupplierNo(EMPTY_SPACE);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return bomPart;
    }

    /**
     *
     * @param flexBoms
     * @param userInputDate
     */
    public static Collection<FlexBOMPart> getModBoms(Collection<FlexBOMPart> flexBoms,String userInputDate){
        DateTimeFormatter formatter;
        Collection<FlexBOMPart> filteredBOMs = new ArrayList<>();

        // Determine the date format (with or without time)
        if (userInputDate.trim().contains(":")) {
            formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        } else {
            formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        }

        // Time zone in which the user provides the date (EST)
        ZoneId userTimeZone = ZoneId.of("America/New_York");

        try {
            LocalDateTime localDateTime;
            // Parse input date into LocalDateTime
            if (userInputDate.trim().contains(":")) {
                localDateTime = LocalDateTime.parse(userInputDate, formatter);
            } else {
                LocalDate localDate = LocalDate.parse(userInputDate, formatter);
                localDateTime = localDate.atStartOfDay(); // default time: 00:00
            }

            // Convert user input to ZonedDateTime in EST
            ZonedDateTime userInputInEST = ZonedDateTime.of(localDateTime, userTimeZone);
            // Normalize to UTC for consistent comparison
            Instant userInputUTC = userInputInEST.toInstant();
            System.out.println("User input (UTC): " + userInputUTC);

            // Iterate and filter BOMs
            for (FlexBOMPart objBomPart : flexBoms) {
                Timestamp modifyTimestamp = objBomPart.getModifyTimestamp();

                System.out.println("modifTimeStamp---------------------"+modifyTimestamp);
                if (modifyTimestamp == null) continue;

                // Convert DB timestamp to UTC Instant
                Instant bomModifiedUTC = modifyTimestamp.toInstant();

                System.out.println("BOM modified (UTC): " + bomModifiedUTC);

                // Compare and filter
                if (!bomModifiedUTC.isBefore(userInputUTC)) {
                    System.out.println("Included BOM: " + bomModifiedUTC);
                    filteredBOMs.add(objBomPart);
                }
            }
            System.out.println("Filtered BOMs count: " + filteredBOMs.size());
        } catch (Exception e) {
            System.err.println("Error parsing or comparing timestamps: " + e.getMessage());
        }
        return filteredBOMs;
    }

}




