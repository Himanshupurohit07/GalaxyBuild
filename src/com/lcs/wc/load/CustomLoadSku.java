//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.lcs.wc.load;

import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.util.LCSProperties;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wt.fc.PersistenceHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class CustomLoadSku {
    private static final Logger LOGGER = LogManager.getLogger(CustomLoadSku.class.getName());
    protected static LCSSKUQuery SKU_QUERY = new LCSSKUQuery();
    public static final String IMAGE_URL = LCSProperties.get("com.lcs.wc.content.imageURL", "/Windchill/images");

    public CustomLoadSku() {
    }

    public static boolean createSku(Hashtable dataValues, Hashtable commandLine, Vector returnObjects) {
        return createSku(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
    }

    protected static boolean createSku(Hashtable dataValues, String fileName) {
        String key = null;
        int status = createLCSSku(dataValues, fileName);
        if (status < 0) {
            key = "LINEPLAN_SKUERRORCOUNT";
        } else if (status == 0) {
            key = "LINEPLAN_SKUSUCCESSCOUNT";
        } else if (status == 1) {
            key = "LINEPLAN_SKUDUPLICATECOUNT";
        } else if (status == 2) {
            key = "LINEPLAN_SKUWARNINGCOUNT";
        }

        if (LoadLinePlan.getLinePlanInfo(fileName, "LINEPLAN_MODE") != null) {
            LoadLinePlan.putLinePlanInfo(fileName, "LINEPLAN_SKUCOUNT", String.valueOf(Integer.parseInt(LoadLinePlan.getLinePlanInfo(fileName, "LINEPLAN_SKUCOUNT")) + 1));
            LoadLinePlan.putLinePlanInfo(fileName, key, String.valueOf(Integer.parseInt(LoadLinePlan.getLinePlanInfo(fileName, key)) + 1));
        }

        return status != -1;
    }

    protected static int createLCSSku(Hashtable dataValues, String fileName) {
        int status = -1;
        LCSProduct product = null;

        try {
            if (LoadLinePlan.getLinePlanInfo(fileName, "LINEPLAN_MODE") != null) {
                initSkuValues(dataValues, fileName);
            }

            product = (LCSProduct)dataValues.get("Product");
            if (product == null && (product = (LCSProduct)LoadCommon.getCache(fileName, "CURRENT_PRODUCT")) == null) {
                product = (LCSProduct)LoadCommon.searchForObject(fileName, "Product", dataValues);
            }
        } catch (WTException var18) {
            LoadCommon.display("\n#WTException : " + var18.getLocalizedMessage());
            var18.printStackTrace();
            LoadLinePlan.logLinePlan(fileName, var18.getLocalizedMessage());
        }

        if (product == null) {
            return -1;
        } else {
            LCSSeason season = null;

            try {
                season = (LCSSeason)dataValues.get("Season");
                if (season == null && (season = (LCSSeason)LoadCommon.getCache(fileName, "CURRENT_SEASON")) == null && LoadCommon.getValue(dataValues, "SeasonType") != null) {
                    season = (LCSSeason)LoadCommon.searchForObject(fileName, "Season", dataValues);
                }
            } catch (WTException var17) {
                LoadCommon.display("\n#WTException : " + var17.getLocalizedMessage());
                var17.printStackTrace();
                LoadLinePlan.logLinePlan(fileName, var17.getLocalizedMessage());
            }

            if (season == null) {
                return -1;
            } else {
                LoadCommon.display("Associating Season Is '" + season.getName() + "' ...");
                String value = LoadCommon.getValue(dataValues, "ProductType");
                if (value != null) {
                    dataValues.put("Type", value);
                }

                Enumeration keys = dataValues.keys();

                while(true) {
                    do {
                        if (!keys.hasMoreElements()) {
                            Hashtable dataValues2 = new Hashtable();
                            dataValues2.putAll(dataValues);
                            LCSSKU sku = null;

                            try {
                                sku = LCSSKU.newLCSSKU();
                                if (season != null) {
                                    dataValues.put("FlexTyped-seasonRevId:SearchCriteria", String.valueOf(season.getBranchIdentifier()));
                                }

                                if ((sku = getSkuFromProduct(product, sku, dataValues, fileName)) == null) {
                                    LoadCommon.display("\t Sku from product " + sku.getName());
                                    status = 1;
                                }
                            } catch (WTPropertyVetoException var15) {
                                LoadCommon.display("\n#WTPropertyVetoException : " + var15.getLocalizedMessage());
                                LoadLinePlan.logLinePlan(fileName, var15.getLocalizedMessage());
                                var15.printStackTrace();
                            } catch (WTException var16) {
                                LoadCommon.display("\n#WTException : " + var16.getLocalizedMessage());
                                LoadLinePlan.logLinePlan(fileName, var16.getLocalizedMessage());
                                var16.printStackTrace();
                            }

                            LoadCommon.deleteCache(fileName, "CURRENT_SEASONPRODUCTLINK");
                            if (status == 1) {
                                return status;
                            }

                            try {
                                if (PersistenceHelper.isPersistent(sku)) {
                                    LoadCommon.display("Found the seasonal sku object.");
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("sku version:  " + sku.getVersionIdentifier().getValue());
                                    }

                                    LoadCommon.putCache(fileName, sku);
                                    sku = (LCSSKU)LoadCommon.getCache(fileName, "CURRENT_FLEXTYPED");
                                } else {
                                    dataValues2.put("FlexTyped-versionIdA2versionInfo:SearchCriteria", "A");
                                    sku = getSkuFromProduct(product, sku, dataValues2, fileName);
                                    if (PersistenceHelper.isPersistent(sku)) {
                                        LoadCommon.display("Found the A rev sku object, but no seasonal version.  Will link sku to season on save.");
                                        LoadCommon.putCache(fileName, sku);
                                    } else {
                                        LoadCommon.display("Did not find an existing sku, creating new sku");
                                    }
                                }

                                sku.setProductMaster(product.getMaster());
                            } catch (WTPropertyVetoException var13) {
                                LoadCommon.display("\n#WTPropertyVetoException : " + var13.getLocalizedMessage());
                                LoadLinePlan.logLinePlan(fileName, var13.getLocalizedMessage());
                                var13.printStackTrace();
                            } catch (WTException var14) {
                                LoadCommon.display("\n#WTException : " + var14.getLocalizedMessage());
                                LoadLinePlan.logLinePlan(fileName, var14.getLocalizedMessage());
                                var14.printStackTrace();
                            }

                            status = validateSku(sku, dataValues, fileName);
                            if (status < 0) {
                                return -1;
                            }

                            try {
                                LoadCommon.loadThumbnail(sku, dataValues);
                                if (season != null) {
                                    dataValues.put("Season", season);
                                }

                                if (!LoadHelper.save(sku, dataValues, fileName)) {
                                    status = -1;
                                }else {
                                	
                                    if (season != null) {
                                    	//System.out.println("season--->"+season);
                                        LCSSeasonProductLink spl = LCSSeasonQuery.findSeasonProductLink(sku, season);
                                       // System.out.println("spl----->"+spl);
                                        if (spl == null) {
                                            sku = LoadSeason.SEASON_LOGIC.addSKU(sku, season);
                                        }
                                        if (sku.getVersionDisplayIdentifier().toString().equals("A")) {
                                            sku = LCSSeasonQuery.getSKUForSeason(sku, season.getMaster());
                                        }
                                    }
                                	
                                }
                            } catch (WTPropertyVetoException var10) {
                                LoadCommon.display("\n#WTPropertyVetoException : " + var10.getLocalizedMessage());
                                LoadLinePlan.logLinePlan(fileName, var10.getLocalizedMessage());
                                var10.printStackTrace();
                                status = -1;
                            } catch (WTException var11) {
                                LoadCommon.display("\n#WTException : " + var11.getLocalizedMessage());
                                LoadLinePlan.logLinePlan(fileName, var11.getLocalizedMessage());
                                var11.printStackTrace();
                                status = -1;
                            } catch (IOException var12) {
                                LoadCommon.display("\n#IOException : " + var12.getLocalizedMessage());
                                LoadLinePlan.logLinePlan(fileName, var12.getLocalizedMessage());
                                var12.printStackTrace();
                                status = -1;
                            }

                            return status;
                        }

                        value = (String)keys.nextElement();
                    } while(!value.startsWith("Product") && !value.startsWith("Season"));

                    dataValues.remove(value);
                }
            }
        }
    }

    protected static LCSSKU getSkuFromProduct(LCSProduct product, LCSSKU sku, Hashtable dataValues, String fileName) throws WTPropertyVetoException, WTException {
        String productId = String.valueOf(product.getMaster().getPersistInfo().getObjectIdentifier().getId());
        dataValues.put("FlexTyped-idA3A12:SearchCriteria", productId);
        return (LCSSKU)LoadCommon.getObjectByCriteria(fileName, sku, dataValues, "LCSSKU");
    }

    private static int validateSku(LCSSKU sku, Hashtable dataValues, String fileName) {
        int status = 0;
        String value = null;
        Vector messages = CustomLoadFlexTyped.setAttributes(sku, dataValues, fileName);

        for(int i = 0; i < messages.size(); ++i) {
            if ((value = (String)messages.elementAt(i)).startsWith("#ERROR")) {
                status = -1;
            }

            if (value.startsWith("#WARNING")) {
                status = 2;
            }

            LoadLinePlan.logLinePlan(fileName, value);
        }

        if (status < 0) {
            value = "ERROR SETTING VALUES FOR THE SKU !!!  FAILURE CREATING SKU !!!";
            LoadCommon.display(value);
            LoadLinePlan.logLinePlan(fileName, value);
        }

        return status;
    }

    private static void initSkuValues(Hashtable values, String fileName) {
        Hashtable dataValues = LoadCommon.copyHashtable(values);
        LoadCommon.getCriteria(dataValues, ":SearchCriteria");
        String lineplanDetails = LoadLinePlan.getLinePlanInfo(fileName, "LINEPLAN_DISPLAY");
        if (lineplanDetails == null) {
            lineplanDetails = "";
        }

        lineplanDetails = lineplanDetails.replace('\t', ' ') + " Sku Details -";

        for(int i = 0; i < LoadLinePlan.LINEPLAN_SKUDETAILS.length; ++i) {
            String var10001 = LoadLinePlan.LINEPLAN_SKUDETAILS[i];
            String var10003 = LoadLinePlan.LINEPLAN_SKUDETAILS[i];
            lineplanDetails = lineplanDetails + " " + var10001 + "='" + LoadCommon.getValue(dataValues, "flexAtt" + var10003) + "'";
        }

    }

    public static LCSSKU saveSku(LCSSKU sku, LCSSeason season) throws WTException {
        return saveSku(sku, season, true);
    }

    public static LCSSKU saveSku(LCSSKU sku, LCSSeason season, boolean deriveStrings) throws WTException {
        String seasonName = season == null ? "null" : season.getName();
        String skuNmae = PersistenceHelper.isPersistent(sku) ? sku.getName() : "";
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("LoadSku.saveSku(" + skuNmae + ", " + seasonName + ")");
        }

        sku = CustomLoadProduct.PRODUCT_LOGIC.saveSKU(sku, season, deriveStrings);
        if (season != null) {
            LCSSeasonProductLink spl = LCSSeasonQuery.findSeasonProductLink(sku, season);
            if (spl == null) {
                sku = LoadSeason.SEASON_LOGIC.addSKU(sku, season);
            }

            if (sku.getVersionDisplayIdentifier().toString().equals("A")) {
                sku = LCSSeasonQuery.getSKUForSeason(sku, season.getMaster());
            }
        }

        return sku;
    }
}

