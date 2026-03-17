package com.sparc.wc.costsheet;

import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.country.LCSCountryQuery;
import com.lcs.wc.db.*;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.flextype.RetypeLogic;
import com.lcs.wc.sourcing.*;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.util.SparcCostingConstants;
import org.apache.logging.log4j.Logger;
import wt.fc.WTObject;
import wt.log4j.LogR;
import com.lcs.wc.product.LCSProduct;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import java.util.Collection;



public class SparcCostSheetPlugins {

    private static final Logger LOGGER = LogR.getLogger(SparcCostSheetPlugins.class.getName());


    /**
     * This plugin creates the first CostSheet based on the Product Type when the product is created.
     *
     * @param wtObject
     * @return
     * @throws WTException
     */
    public static final WTObject setTypeForDefaultCostSheet(WTObject wtObject) {
        System.out.println("Start of PRE DERIVE EVENT");
        System.out.println("Start of setTypeForDefaultCostSheet-----" + wtObject);
        LOGGER.info("Start of setTypeForDefaultCostSheet");
        LCSProductCostSheet objCostSheet = null;
        if (!(wtObject instanceof LCSProductCostSheet)) {
            return wtObject;
        }
        try {
            objCostSheet = (LCSProductCostSheet) wtObject;

            if (FormatHelper.equalsWithNull(objCostSheet.getSourcingConfigMaster(), null)) {
                LOGGER.info("Sourcing Config is null");
                return objCostSheet;
            }
            LCSSourcingConfig objSourcingConfig = (LCSSourcingConfig) VersionHelper.latestIterationOf(objCostSheet.getSourcingConfigMaster());
            Collection<LCSCostSheet> costSheets = LCSCostSheetQuery.getAllCostSheetsForSourcingConfig(objSourcingConfig);
			
			System.out.println("Flextype------------"+objCostSheet.getFlexType().getFullName(true));
			System.out.println("Costing sparc---------------"+SparcCostingConstants.SPARC_COSTSHEET);
			System.out.println("value--------------"+objCostSheet.getFlexType().getFullName(true).equalsIgnoreCase(SparcCostingConstants.SPARC_COSTSHEET));
			
            if (costSheets.isEmpty() && objCostSheet.getFlexType().getFullName(true).equalsIgnoreCase(SparcCostingConstants.SPARC_COSTSHEET)) {
				System.out.println("Costhseet inside");
                objCostSheet = updateCostSheetFlexType(objCostSheet, objSourcingConfig);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("End of setTypeForDefaultCostSheet-----" + wtObject);
        LOGGER.info("End of setTypeForDefaultCostSheet");
        System.out.println("End of PRE DERIVE EVENT");
        return objCostSheet;
    }


    /**
     * This method change the costshe
     *
     * @param objCostSheet
     * @param objSourcingConfig
     * @return
     * @throws WTException
     */
    private static LCSProductCostSheet updateCostSheetFlexType(LCSProductCostSheet objCostSheet, LCSSourcingConfig objSourcingConfig) throws WTException {
        LOGGER.info("Start of createNewCostSheet");
        LCSProduct objProduct = getLatestProduct(objSourcingConfig.getProductMaster());


        String costSheetType = getCostSheetType(objProduct);
        if (!FormatHelper.hasContent(costSheetType)) {
            return objCostSheet;
        }
        FlexType flexTypeFromPath = FlexTypeCache.getFlexTypeFromPath(costSheetType);
        FlexTyped flexTyped = RetypeLogic.changeType((FlexTyped) objCostSheet, FormatHelper.getObjectId(flexTypeFromPath), false);
        LOGGER.info("End of createNewCostSheet");
        return (LCSProductCostSheet) flexTyped;
    }

    /**
     * @param productMaster
     * @return
     * @throws WTException
     */
    private static LCSProduct getLatestProduct(WTObject productMaster) throws WTException {
        LOGGER.info("Start of getLatestProduct");
        LCSProduct objProduct = (LCSProduct) VersionHelper.latestIterationOf(productMaster);
        objProduct = (LCSProduct) VersionHelper.getVersion(objProduct, "A");
        LOGGER.info("End of getLatestProduct");
        return objProduct;
    }

    /**
     * @param objProduct
     * @return
     * @throws WTException
     */
    private static String getCostSheetType(LCSProduct objProduct) throws WTException {
        LOGGER.info("Start of getCostSheetType");
        String productType = objProduct.getFlexType().getFullName(true);
		//As part of Costsheet downloader, High level costsheet should be defaulted on create of product so commenting this code - Start
        /*if (productType.contains("scFootwearReebok")) {
            return "";
        }*/
		//As part of Costsheet downloader, High level costsheet should be defaulted on create of product so commenting this code - End
    
        LOGGER.info("productType  is    " + productType);
        System.out.println("product type is   from syso   " + productType);
        productType = productType.replaceAll("\\\\", ".");

        LOGGER.info("Updated ProductType  is    " + productType);
        LOGGER.info("End of getCostSheetType");
        return LCSProperties.get(productType);
    }

   
}
