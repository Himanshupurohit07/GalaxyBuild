package com.sparc.wc.bomlink.extractor;


import com.lcs.wc.color.LCSColor;
import com.lcs.wc.flexbom.FlexBOMLink;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.material.LCSMaterialColor;
import com.lcs.wc.material.LCSMaterialMaster;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.supplier.LCSSupplierMaster;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import wt.fc.WTObject;
import wt.util.WTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static com.sparc.wc.bom.constants.SparcMigrationConstants.*;

public class BomDataExtractorHelper {

    private static final Logger logger = LoggerFactory.getLogger(BomDataExtractorHelper.class);

    /**
     *
     * @param child
     * @return
     */
    public static String getMaterialValue(LCSMaterialMaster child) {
        try {
            Optional<LCSMaterial> objMaterial = Optional.ofNullable(child)
                    .map(c -> {
                        try {
                            return (LCSMaterial) VersionHelper.latestIterationOf(c);
                        } catch (WTException e) {
                            logger.error("Error fetching latest material iteration", e);
                        }
                        return null;
                    });
            return objMaterial.map(material -> {
                try {
                    return String.valueOf(material.getValue(MATERIAL_NO));
                } catch (WTException e) {
                    logger.error("Error fetching material number", e);
                }
                return EMPTY_SPACE;
            }).orElse(EMPTY_SPACE);
        } catch (Exception e) {
            logger.error("Error extracting material value", e);
            return EMPTY_SPACE;
        }
    }

    /**
     *
     * @param supMaster
     * @return
     */
    public static String getSupplierValue(LCSSupplierMaster supMaster) {
        try {
            Optional<LCSSupplier> objSupplier = Optional.ofNullable(supMaster)
                    .map(s -> {
                        try {
                            return (LCSSupplier) VersionHelper.latestIterationOf(s);
                        } catch (WTException e) {
                            logger.error("Error fetching latest supplier iteration", e);
                        }
                        return null;
                    });
            return objSupplier.map(s -> {
                try {
                    return String.valueOf(s.getValue(SUPPLIER_NO));
                } catch (WTException e) {
                    logger.error("Error fetching supplier number", e);
                }
                return EMPTY_SPACE;
            }).orElse("");
        } catch (Exception e) {
            logger.error("Error extracting supplier value", e);
            return EMPTY_SPACE;
        }
    }

    /**
     * This method get the sourcing Master ID
     * @param sourceDimension
     * @return
     */
    public static String getSourcingNo(WTObject sourceDimension) {
        if(FormatHelper.equalsWithNull(sourceDimension,null)){
            return EMPTY_SPACE;
        }
        else{
            try {
                LCSSourcingConfigMaster objSourcingConfigMaster = (LCSSourcingConfigMaster)sourceDimension;
                return FormatHelper.getNumericObjectIdFromObject(objSourcingConfigMaster);
            }catch(Exception ex){
                return EMPTY_SPACE;
            }
        }
    }

    /**
     *
     * @param cObject
     * @return
     */
    public static String getColorwayNo(WTObject cObject) {
        try {
            Optional<LCSSKU> objSKU = Optional.ofNullable(cObject)
                    .map(colorDim -> {
                        try {
                            return (LCSSKU) VersionHelper.latestIterationOf(colorDim);
                        } catch (WTException e) {
                            logger.error("Error fetching sourcing config", e);
                        }
                        return null;
                    });
            return objSKU.map(sku -> {
                try {
                    return String.valueOf(sku.getValue(COLORWAY_NO));
                } catch (WTException e) {
                    logger.error("Error fetching sourcing number", e);
                }
                return EMPTY_SPACE;
            }).orElse(EMPTY_SPACE);
        } catch (Exception e) {
            logger.error("Error extracting sourcing number", e);
            return EMPTY_SPACE;
        }
    }


    public static String getColorNo(LCSColor color){
        String strColor =EMPTY_SPACE;
        try {
            Optional<LCSColor> objColor = Optional.ofNullable(color);

            // Set strColor using the value from the color or return empty string in case of an error
            return objColor.map(c -> {
                try {
                    return String.valueOf(c.getValue(COLOR_NO));
                } catch (WTException e) {
                    logger.error("Error extracting color number", e);
                }
                return EMPTY_SPACE; // return empty space if exception occurs
            }).orElse(EMPTY_SPACE); // return empty space if no color found or it's null

        } catch (Exception e) {
            logger.error("Error extracting color", e);
            return EMPTY_SPACE; // return empty space in case of any exception
        }
    }

    /**
     *
     * @param materialColor
     * @return
     */
    public static String getMaterialColor(LCSMaterialColor materialColor) {
        try {
            return (materialColor != null) ? FormatHelper.getNumericObjectIdFromObject(materialColor) : EMPTY_SPACE;
        } catch (Exception e) {
            logger.error("Error extracting material color", e);
            return EMPTY_SPACE;
        }
    }

    /**
     *
     * @param row
     * @return
     */
    public static String getMaterialDesc(FlexBOMLink row) {
        String strMatDesc = "";
        try {
            // Safely handle row.getValue and convert it to String
            strMatDesc = Optional.ofNullable(row.getValue(MATERIAL_DESC))
                    .map(Object::toString) // Convert the object to a String
                    .orElse(EMPTY_SPACE); // Default to empty string if null
        } catch (Exception ex) {
            // Use logging instead of printStackTrace for better error tracking
            logger.error("Error extracting material description", ex);
        }
        return strMatDesc;
    }

    public void formTheBomLinkData(List<String> strAttributes,FlexBOMLink bomLink){
            for(String strAttribute : strAttributes){

            }
    }



}
