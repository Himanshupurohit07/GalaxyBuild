package com.sparc.wc.product;

import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import org.apache.logging.log4j.Logger;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import java.util.Collection;

public class SPARCAttributeManager {


    private static final Logger LOGGER = LogR.getLogger(SPARCAttributeManager.class.getName());
    private static String productCostSheetAtts = LCSProperties.get("com.lcs.wc.product.Cost.ProdAttributes","sccostcheetsenttoCAP,sccostcheetsenttos4");
    private static Collection<String> prodCostAttributes = FormatHelper.commaSeparatedListToCollection(productCostSheetAtts);


    /**
     *
     * @param obj
     */
    public static void setAttributeValueWithDefault(WTObject obj)  {
        try {
            if ((obj instanceof LCSProduct)) {
                setProdAttributeValueWithDefault(obj);
            } else if (obj instanceof LCSSKU) {
                setColorwayAttribValueWithDefault(obj);
            } else if(obj instanceof LCSProductCostSheet){
				setCostAttributeValueWithDefault(obj);
            }
        }
        catch(Exception ex){
            LOGGER.debug("Error while setting the value-------"+ex.getMessage());
        }

    }

    /**
     *
     * @param obj
     * @throws WTPropertyVetoException
     * @throws WTException
     */
    public static void setProdAttributeValueWithDefault(WTObject obj) throws WTPropertyVetoException,WTException {

        LCSProduct objProduct =(LCSProduct) obj;
        // Assuming objProduct is an instance of some class that has a getValue method
        Object valueObject = objProduct.getValue("sclNewColorwayFlag");

        // Check if the valueObject is not null and is of type String
        if (valueObject != null && valueObject instanceof String) {
            String newColorFlagValue = (String) valueObject;

            // Check if the string is not empty
            if (!FormatHelper.hasContent(newColorFlagValue)) {
                objProduct.setValue("sclNewColorwayFlag","scNew");
            }
        } else {
            objProduct.setValue("sclNewColorwayFlag","scNew");
        }
    }

    /**
     *
     * @param obj
     */
    public static void setCostAttributeValueWithDefault(WTObject obj)  {
        LCSProductCostSheet objProductCostsheet =(LCSProductCostSheet) obj;
        for(String strAttKey : prodCostAttributes) {
            try {
                Object resetObj = objProductCostsheet.getValue(strAttKey);
                if (resetObj != null && resetObj instanceof String) {
                    String newFlagValue = (String) resetObj;
                    if (!FormatHelper.hasContent(newFlagValue)) {
                        objProductCostsheet.setValue(strAttKey, "scNo");
                    }
                } else {
                    objProductCostsheet.setValue(strAttKey, "scNo");
                }
            }catch(WTException ex){
                ex.printStackTrace();
            }
        }
    }

    /**
     *
     * @param obj
     * @throws WTPropertyVetoException
     * @throws WTException
     */
    public static void setColorwayAttribValueWithDefault(WTObject obj) throws WTPropertyVetoException,WTException  {

        LCSSKU objSKU =(LCSSKU) obj;
        // Assuming objProduct is an instance of some class that has a getValue method
        Object valueObject = objSKU.getValue("sclNewColorwayFlag2");

        // Check if the valueObject is not null and is of type String
        if (valueObject != null && valueObject instanceof String) {
            String newColorFlagValue = (String) valueObject;

            // Check if the string is not empty
            if (!FormatHelper.hasContent(newColorFlagValue)) {
                objSKU.setValue("sclNewColorwayFlag2","scNew");
            }
        } else {
            objSKU.setValue("sclNewColorwayFlag2","scNew");
        }
    }

}
