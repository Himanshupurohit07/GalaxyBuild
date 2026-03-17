package com.galaxy.wc.migration.loader.util;

import com.lcs.wc.flexbom.*;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.material.LCSMaterialSupplier;
import com.lcs.wc.material.LCSMaterialSupplierQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.ProductHeaderQuery;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.specification.*;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import com.galaxy.wc.bom.constants.GalaxyMigrationConstants;
import com.galaxy.wc.bom.helper.GalaxyBomHelper;
import com.galaxy.wc.migration.loader.BomLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lcs.wc.foundation.LCSPluginManager;
import wt.util.WTException;
import wt.vc.wip.WorkInProgressHelper;

import java.util.*;

public class GalaxyBomPartUtil {





    private static final Logger cusLogger = LogManager.getLogger(GalaxyBomPartUtil.class.getName());
	
	
	
	
	  public static BomPrimaryUpdateResult updatePrimaryMaterial(Map<String, String> data, BomLogger logger, int lineNo) {

        String strProductNo = data.get(GalaxyMigrationConstants.PRODUCT_NO);
        String strBomMasterId = data.get(GalaxyMigrationConstants.BOM_MASTER_ID);
        String strPrimaryMaterialNo = data.get(GalaxyMigrationConstants.PRIM_MATERIAL_NO);
        String strPrimaryMatDesc = data.get(GalaxyMigrationConstants.PRIM_MATERIAL_DESC);
        String strPrimSupNo = data.get(GalaxyMigrationConstants.PRIM_SUPPLIER_NO);
        String strBOMName = data.get(GalaxyMigrationConstants.BOM_NAME);	
		
		
        LCSProduct objProduct = GalaxyBomHelper.getProduct(strProductNo, GalaxyMigrationConstants.PROD_A_VERSION);
        FlexBOMPart objBOMPart = isBomAvailable(objProduct, strBomMasterId);

        LCSMaterial objMaterial = null;
        LCSSupplier objSupplier = null;
        LCSMaterialSupplier matSupp = null;


        if (FormatHelper.equalsWithNull(objBOMPart, null)) {
            logger.log("BOM Not available for the BOM part-" + strBOMName + "-and the product no-" + strProductNo);
            return new BomPrimaryUpdateResult(BomPartProcessingStatus.VALIDATION_FAILED, false);
        }


        objMaterial = GalaxyBomHelper.findMaterialByID(strPrimaryMaterialNo);
        if (FormatHelper.equalsWithNull(objMaterial, null)) {
            logger.log("Material Not available-" + strPrimaryMaterialNo + "-and the LineNo-" + lineNo);
            return new BomPrimaryUpdateResult(BomPartProcessingStatus.VALIDATION_FAILED, false);
        }

        try {
            objSupplier = GalaxyBomHelper.findSupplierByNameType(strPrimSupNo);
        } catch (WTException ex) {

        }

        if (FormatHelper.equalsWithNull(objSupplier, null)) {
            logger.log("Supplier Not available-" + strPrimSupNo + "-and the LineNo-" + lineNo);
            return new BomPrimaryUpdateResult(BomPartProcessingStatus.VALIDATION_FAILED, false);
        }

        try {
            matSupp = LCSMaterialSupplierQuery.findMaterialSupplier(objMaterial.getMaster(), objSupplier.getMaster());
        } catch (WTException ex) {

        }

        if (FormatHelper.equalsWithNull(matSupp, null)) {
            logger.log("Material Supplier not available for the materialID-" + strPrimaryMaterialNo + "-and the supplier ID-" + strPrimSupNo);
            return new BomPrimaryUpdateResult(BomPartProcessingStatus.VALIDATION_FAILED, false);
        }

        Collection<FlexBOMLink> allLinks = new ArrayList<>();

        try {
            allLinks = LCSFlexBOMQuery.findFlexBOMLinks(
                    objBOMPart, null, null, null, null, null, null, null, false,
                    LCSFlexBOMQuery.ALL_DIMENSIONS, "", "", ""
            );
        } catch (Exception ex) {

        }

        if (allLinks.isEmpty()) {
            logger.log("No Bomlinks associated with this BOM-");
            return new BomPrimaryUpdateResult(BomPartProcessingStatus.NO_BOM_LINKS, false);
        }


        Boolean isBomUpdated = false;
        try {
            if (!VersionHelper.isCheckedOut(objBOMPart)) {
                objBOMPart = (FlexBOMPart) WorkInProgressHelper.service
                        .checkout(objBOMPart, WorkInProgressHelper.service.getCheckoutFolder(), "")
                        .getWorkingCopy();
            } else {
                objBOMPart = VersionHelper.getWorkingCopy(objBOMPart);
            }
            objBOMPart.setValue("primaryMaterial", matSupp);
            objBOMPart.setValue("pmDescription", strPrimaryMatDesc);

            LCSPluginManager.handleEvent(objBOMPart, "PRIMARY_MATERIAL_ROLL_UP");
            objBOMPart = (FlexBOMPart) LCSFlexBOMLogic.persist(objBOMPart);
            if (VersionHelper.isCheckedOut(objBOMPart)) {
                objBOMPart = (FlexBOMPart) WorkInProgressHelper.service.checkin(objBOMPart, null);
            }
            isBomUpdated = true;
        } catch (Exception ex) {
            logger.log("Primary Update of BOM Failed-" + strBOMName + "-and the BOM MasterID-" + strBomMasterId + "-and the Product is-" + strProductNo + "-and the exception is-" + ex.getMessage());
            ex.printStackTrace();
            isBomUpdated = false;
        } finally {
            try {
                if (!FormatHelper.equalsWithNull(objBOMPart, null)) {
                    if (VersionHelper.isCheckedOut(objBOMPart)) {
                        VersionHelper.undoCheckout(objBOMPart);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (isBomUpdated) {
            return new BomPrimaryUpdateResult(BomPartProcessingStatus.SUCCESS, isBomUpdated);
        } else {
            return new BomPrimaryUpdateResult(BomPartProcessingStatus.EXCEPTION, isBomUpdated);
        }
    }


    /**
     *
     * @param data
     * @param logger
     */
    public static BomProcessingResult processBOMData(Map<String, String> data, BomLogger logger, int lineNo) {
        String specName = data.get(GalaxyMigrationConstants.SPEC_NAME);
        String strSeasonName = data.get(GalaxyMigrationConstants.SEASON_NAME);
        String strProductNo = data.get(GalaxyMigrationConstants.PRODUCT_NO);
        String strSourcingNo = data.get(GalaxyMigrationConstants.SOURCE_MASTER_ID);
        String strSourceName = data.get(GalaxyMigrationConstants.SOURCE_NAME);
        String specNo = data.get(GalaxyMigrationConstants.SPEC_MASTER_ID);
        String strBomMasterId = data.get(GalaxyMigrationConstants.BOM_MASTER_ID);
        String strBomNo = data.get(GalaxyMigrationConstants.BOM_PART_NUMBER);
        String isPrimary = data.get(GalaxyMigrationConstants.IS_PRIMARY);
        String bomStatus = data.get(GalaxyMigrationConstants.BOM_HEAD_STATUS);
        String strBOMName = data.get(GalaxyMigrationConstants.BOM_NAME);
        String strBOMTypePath = data.get(GalaxyMigrationConstants.BOM_TYPE_PATH);

        // Get the season and product from the system
        LCSSeason objSeason = GalaxyBomHelper.getSeason(null, strSeasonName);


        LCSProduct objProduct = GalaxyBomHelper.getProduct(strProductNo, GalaxyMigrationConstants.PROD_A_VERSION);

        // Log if product is missing
        if (objProduct == null) {
            logger.log("Error:Product not found for ProductNo: " + strProductNo + " - Skipping this record. and the line No is "+lineNo);
            return new BomProcessingResult(BomPartProcessingStatus.VALIDATION_FAILED, false, false,false);// Skip this record and continue with the next one
        }

        // Check if season is missing
        if (objSeason == null) {
            logger.log("Error :Season not found for SeasonName: " + strSeasonName + " - Skipping this record. and the line No is "+lineNo);
            return new BomProcessingResult(BomPartProcessingStatus.VALIDATION_FAILED, false, false,false);
        }

        LCSMaterial objMaterial = null;

        // If specName is empty, handle it accordingly
        if (!FormatHelper.hasContent(specName)) {
            FlexBOMPart objBOMPart = isBomAvailable(objProduct, strBomMasterId);
            if (FormatHelper.equalsWithNull(objBOMPart, null)) {
                try {
                    createBOMPart(objProduct,strBOMName,strBomNo,objMaterial,null,strBOMTypePath,bomStatus,strBomMasterId);
                    // logger.log("BOM Created for the " + strProductNo + " and BomBranchId: " + strBomMasterId + " -Successfully.");
                    logger.log("BOM Created successfully for the Product "+strProductNo+" and the BOM name is----"+strBOMName+"and the lineno is"+lineNo);
                    return new BomProcessingResult(BomPartProcessingStatus.SUCCESS, true, false,false);

                }catch(Exception ex){
                    logger.log("Error while creating the BOM for the Product "+strProductNo+" and Bom Name "+strBOMName+"and the error message is"+ ex.getMessage() +"and the line No is "+lineNo);
                    return new BomProcessingResult(BomPartProcessingStatus.EXCEPTION, false, false,false);
                }
            } else {
                logger.log("BOM already exists for ProductNo: " + strProductNo + " and BomBranchId: " + strBomMasterId+" and the line No is "+lineNo);
                return new BomProcessingResult(BomPartProcessingStatus.ALREADY_EXISTS, false, false,true);
            }
        } else {
            // Handle the case where a specName is provided (BOM linked to specification)
            Optional<LCSSourcingConfig> sourcingConfig = findSourcingConfig(objProduct, strSourcingNo, objSeason);
            if (sourcingConfig.isEmpty()) {
                logger.log("Warning Sourcing configuration not found for ProductNo: " + strProductNo + " and SourcingNo: " + strSourcingNo + "and the source Name is "+strSourceName+" - Skipping this record. and the lineNo is "+lineNo);
                return new BomProcessingResult(BomPartProcessingStatus.VALIDATION_FAILED, false, false,false);
            }

            Optional<FlexSpecification> flexSpecification = findFlexSpecification(objProduct, sourcingConfig.get(), specNo, objSeason);
            if (flexSpecification.isEmpty()) {
                logger.log("Warning : Specification not found for ProductNo: " + strProductNo + ", SpecNo: " + specNo + "and the specName is "+specName+" and the corresponding source No is "+strSourcingNo+" Source Name is"
				+strSourceName+"and the BOM MASTER ID-"+strBomNo+"- Skipping this record. and the lineNo is "+lineNo);
                return new BomProcessingResult(BomPartProcessingStatus.VALIDATION_FAILED, false, false,false);
            }

            boolean bomCreated = false;
            boolean bomAlreadyExists = false;
            // Proceed with BOM creation or linking
            FlexBOMPart objBOMPart = isBomAvailable(objProduct, strBomMasterId);


            if (objBOMPart == null) {
                try {
                    objBOMPart = createBOMPart(objProduct,strBOMName,strBomNo,objMaterial,null,strBOMTypePath,bomStatus,strBomMasterId);
                    logger.log("BOM Created successfully for the Product "+strProductNo+" and the BOM name is----"+strBOMName+"and the lineno is"+lineNo);
                    bomCreated = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.log("Error creating BOM for ProductNo: " + strProductNo + " - and the error message is  " + e.getMessage() +" and the lineno is "+lineNo);
                    return new BomProcessingResult(BomPartProcessingStatus.EXCEPTION, false, false,false);
                }
            }
            else{
                bomAlreadyExists = true;
            }

            // Link BOM to Specification
            try {
                if(FlexSpecQuery.getSpecToComponentLink(flexSpecification.get(),objBOMPart) == null) {
                    try {
                        addBOMtoSpec(flexSpecification.get(), objBOMPart, Boolean.valueOf(isPrimary));
                        return new BomProcessingResult(BomPartProcessingStatus.SUCCESS, bomCreated, false,bomAlreadyExists);
                    }catch(Exception ex){
                        logger.log("Error while adding the BOM to the spec-and the BOM Name-"+strBomNo+"-and the spec Name is-"+specName+"and the product No is-"+strProductNo);
                        return new BomProcessingResult(BomPartProcessingStatus.ADD_BOM_TO_SPEC_ISSUE, bomCreated, true,bomAlreadyExists);
                    }
                }
                else{
                    FlexSpecToComponentLink objSpecToCompLink = FlexSpecQuery.getSpecToComponentLink(flexSpecification.get(),objBOMPart);
                    try {
                        objSpecToCompLink.setPrimaryComponent(Boolean.valueOf(isPrimary));
                        LCSLogic.persist(objSpecToCompLink,true);
                        logger.log("Spec to BOM Component updated successfully for the ProductNo --------"+strProductNo+" and the BOM Name is "+strBOMName);
                        return new BomProcessingResult(BomPartProcessingStatus.SUCCESS, bomCreated, false,bomAlreadyExists);
                    }catch(Exception ex){
                        logger.log("Error updating BOM spec link: " + ex.getMessage());
                        return new BomProcessingResult(BomPartProcessingStatus.EXCEPTION, bomCreated, false,bomAlreadyExists);
                    }
                }
            } catch (Exception e) {
                logger.log("Error checking or linking spec for Product: " + strProductNo + ", SpecNo: " + specNo + " - " + e.getMessage());
                return new BomProcessingResult(BomPartProcessingStatus.ADD_BOM_TO_SPEC_ISSUE, bomCreated, true,bomAlreadyExists);
            }
        }
    }


    /**
     *
     * @param objProduct
     * @return
     * @throws WTException
     * @throws Exception
     */
    public static FlexBOMPart createBOMPart(LCSProduct objProduct,String strBOMName,String strNum,LCSMaterial objMaterial,String strMatDesc,String strBOMTypePath,String strStatus,
                                            String strBOMMasterId) throws WTException,Exception{
        String bomTypeID = null;
        FlexBOMPart flexBOMPartObj = null;
        FlexBOMPartClientModel bomPartClientModelObj = new FlexBOMPartClientModel();
		 boolean isApparel = false;
        try {
            if(strBOMTypePath.contains("Footwear")){
                bomTypeID = FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath(GalaxyMigrationConstants.BOM_FOOTWEAR_TYPE));
            }
            else{
                bomTypeID = FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath(GalaxyMigrationConstants.BOM_APPAREL_TYPE));
				isApparel = true;
            }
            bomPartClientModelObj.setTypeId(bomTypeID);
            bomPartClientModelObj.initiateBOMPart(FormatHelper.getObjectId(objProduct), bomTypeID, "MAIN", null, false);
            flexBOMPartObj = bomPartClientModelObj.getBusinessObject();
            if(!VersionHelper.isCheckedOut(flexBOMPartObj)) {
                flexBOMPartObj = (FlexBOMPart) VersionHelper.checkout(flexBOMPartObj);
            }
            flexBOMPartObj.setValue(GalaxyMigrationConstants.BOM_PART_NAME, strBOMName);
            flexBOMPartObj.setValue(GalaxyMigrationConstants.NAME, strBOMName);
            flexBOMPartObj.setValue(GalaxyMigrationConstants.BOM_PART_NUMBER, strNum);
            flexBOMPartObj.setValue(LCSFlexBOMQuery.BOM_NUM_KEY, strNum);
            flexBOMPartObj.setValue(GalaxyMigrationConstants.SUB_ASSEMBLY_INSERTION_MODE, GalaxyMigrationConstants.PROMPT_USER_LINK_OR_COPY);
            flexBOMPartObj.setValue(GalaxyMigrationConstants.BOM_MASTER_ID, strBOMMasterId);
			if(isApparel){
                flexBOMPartObj.setValue(GalaxyMigrationConstants.BOM_STATUS,strStatus);
            }
            flexBOMPartObj = (FlexBOMPart) LCSLogic.persist(flexBOMPartObj, true);
            flexBOMPartObj = (FlexBOMPart) VersionHelper.checkin(flexBOMPartObj);
        } catch (WTException ex){
            ex.printStackTrace();
            throw new WTException("Bom was not created due to the issue-----------"+ex.getMessage());
        }catch(Exception ex){
            ex.printStackTrace();
            throw new Exception("Bom was not created due to the issue--------------"+ex.getMessage());
        }
        return flexBOMPartObj;

    }

    /**
     *
     * @param objSpec
     * @param objBomPart
     * @throws WTException
     */
    public static void addBOMtoSpec(FlexSpecification objSpec,FlexBOMPart objBomPart,Boolean isPrimary) throws WTException{
        try {

            FlexSpecHelper.service.addComponentToSpec(objSpec,null,objBomPart,isPrimary);
        } catch (WTException e) {
            throw new WTException(e.getMessage());
        }
    }

    /**
     *
     * @param objProduct
     * @param strSourceMasterID
     * @param objSeason
     * @return
     */
    // Step 2: Find Sourcing Configuration using Strategy pattern for filtering logic
    public static Optional<LCSSourcingConfig> findSourcingConfig(LCSProduct objProduct, String strSourceMasterID, LCSSeason objSeason) {
        try {
            Collection<LCSSourcingConfig> sourcingConfigs;
            sourcingConfigs = new ProductHeaderQuery().findSourcingConfigs(objProduct, objSeason);

            return sourcingConfigs.stream()
                    .filter(sourcing -> {
                        try {
                            return strSourceMasterID.equalsIgnoreCase((String)sourcing.getValue(GalaxyMigrationConstants.SOURCE_MASTER_ID));
                        } catch (WTException e) {
                            e.printStackTrace();
                        }
                        return false;
                    })
                    .findFirst();
        } catch (WTException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     *
     * @param objProduct
     * @param sourcingConfig
     * @param specNo
     * @param objSeason
     * @return
     */
    // Step 3: Find Flex Specification
    public static Optional<FlexSpecification> findFlexSpecification(LCSProduct objProduct, LCSSourcingConfig sourcingConfig, String specNo, LCSSeason objSeason) {
        try {
            // Retrieve specifications
            Collection<FlexSpecification> specifications = new ProductHeaderQuery().findSpecifications(objProduct, sourcingConfig, objSeason);

            // If no specifications are found, return an empty Optional immediately
            if (specifications.isEmpty()) {
                return Optional.empty();
            }

            // Find the specification matching the specNo, handle WTException outside stream
            return specifications.stream()
                    .filter(spec -> {
                        String specBranchId = null;
                        try {
                            specBranchId = (String) spec.getValue("glSpecMasterId");
                        } catch (WTException e) {
                            // Log the error instead of printing stack trace
                            System.err.println("Error getting specBranchId: " + e.getMessage());
                        }
                        return specNo.equalsIgnoreCase(specBranchId);
                    })
                    .findFirst();
        } catch (WTException e) {
            // Handle the exception gracefully
            System.err.println("Error fetching specifications: " + e.getMessage());
            return Optional.empty();
        }
    }


    /**
     *
     * @param objProduct
     * @param strBomBranchId
     * @param flexSpec
     */
    // Step 4: Process BOM Parts
    private static void processBOMParts(LCSProduct objProduct, String strBomBranchId, FlexSpecification flexSpec) {
        Collection<FlexBOMPart> boms = GalaxyBomHelper.getBOMs(objProduct, null, null);
        Optional<FlexBOMPart> bomPart = boms.stream()
                .filter(bom -> {
                    try {
                        return strBomBranchId.equalsIgnoreCase((String) bom.getValue(GalaxyMigrationConstants.BOM_MASTER_ID));
                    } catch (WTException e) {
                        e.printStackTrace();
                    }
                    return false;
                })
                .findFirst();

        if (bomPart.isPresent()) {
            FlexBOMPart objBomPart = bomPart.get();
            // Process the BOM part (Add your logic here)
        }
    }


    /**
     *
     * @param product
     * @param bomBranchId
     * @param flexSpec
     * @return
     */
    private static FlexBOMPart checkBomAvailable(LCSProduct product, String bomBranchId, FlexSpecification flexSpec) {
        // Fetch all BOM parts related to the product
        Collection<FlexBOMPart> bomParts = GalaxyBomHelper.getBOMs(product, null, null);

        // Loop through the BOM parts and find the one with the matching bomBranchId
        for (FlexBOMPart bom : bomParts) {
            try {
                // Compare the current BOM's branch ID with the provided bomBranchId (case-insensitive)
                String currentBomBranchId = (String) bom.getValue(GalaxyMigrationConstants.BOM_MASTER_ID);
                if (bomBranchId.equalsIgnoreCase(currentBomBranchId)) {
                    // If a match is found, return the corresponding BOM part
                    return bom;
                }
            } catch (WTException e) {
                // Log the exception if there is an error accessing the BOM part's value
                e.printStackTrace();
            }
        }
        // If no matching BOM part is found, return null
        return null;
    }

    /**
     *
     * @param product
     * @param bomBranchId
     * @return
     */
    public static FlexBOMPart isBomAvailable(LCSProduct product, String bomBranchId) {
        // Fetch all BOM parts related to the product
        Collection<FlexBOMPart> bomParts = GalaxyBomHelper.getBOMs(product, null, null);

        // Find the BOM part that matches the provided bomBranchId (case-insensitive comparison)
        Optional<FlexBOMPart> matchingBOMPart = bomParts.stream()
                .filter(bom -> {
                    try {
                        // Check if the bomBranchId matches the one in the current BOM part
                        String currentBomBranchId = (String) bom.getValue("glBomMasterId");
                        return bomBranchId.equalsIgnoreCase(currentBomBranchId);
                    } catch (WTException e) {
                        // Log the exception if there's an issue accessing the BOM part's value
                        e.printStackTrace();
                        return false; // Return false in case of an exception
                    }
                })
                .findFirst(); // We only need the first match
        // Return the matching BOM part if found, or null if not
        return matchingBOMPart.orElse(null);   }

}

