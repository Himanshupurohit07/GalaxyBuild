
package com.lcs.wc.load;

import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductLogic;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.LCSProperties;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class CustomLoadProduct {
    protected static LCSProductQuery PRODUCT_QUERY = new LCSProductQuery();
    protected static LCSProductLogic PRODUCT_LOGIC = new LCSProductLogic();
    public static final String IMAGE_URL = LCSProperties.get("com.lcs.wc.content.imageURL", "/Windchill/images");

    public CustomLoadProduct() {
    }

    public static boolean createProduct(Hashtable<String, Object> dataValues, Hashtable<String, Object> commandLine, Vector returnObjects) {
        return createProduct(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
    }

    protected static boolean createProduct(Hashtable<String, Object> dataValues, String fileName) {
        String key = null;
        int status = createLCSProduct(dataValues, fileName);
        if (status == -1) {
            key = "LINEPLAN_PRODUCTERRORCOUNT";
        } else if (status == 0) {
        
            key = "LINEPLAN_PRODUCTSUCCESSCOUNT";
        } else if (status == 1) {
        	

            key = "LINEPLAN_PRODUCTDUPLICATECOUNT";
            
        } else if (status == 2) {
            key = "LINEPLAN_PRODUCTWARNINGCOUNT";
        }

        if (LoadLinePlan.getLinePlanInfo(fileName, "LINEPLAN_MODE") != null) {
            LoadLinePlan.putLinePlanInfo(fileName, "LINEPLAN_PRODUCTCOUNT", String.valueOf(Integer.parseInt(LoadLinePlan.getLinePlanInfo(fileName, "LINEPLAN_PRODUCTCOUNT")) + 1));
            LoadLinePlan.putLinePlanInfo(fileName, key, String.valueOf(Integer.parseInt(LoadLinePlan.getLinePlanInfo(fileName, key)) + 1));
        }

        return status != -1;
    }

    protected static int createLCSProduct(Hashtable<String, Object> dataValues, String fileName) {
        if (LoadLinePlan.getLinePlanInfo(fileName, "LINEPLAN_MODE") != null) {
            initProductValues(dataValues, fileName);
        }

        String value = null;
        Hashtable<String, Object> seasonValues = new Hashtable();
        Enumeration<?> keys = dataValues.keys();

        while(keys.hasMoreElements()) {
            value = (String)keys.nextElement();
            if (value.startsWith("Season")) {
                seasonValues.put(value, dataValues.get(value));
                dataValues.remove(value);
            }
        }

        LCSProduct product = null;

        try {
            product = LCSProduct.newLCSProduct();
            product = (LCSProduct)LoadCommon.getObjectByCriteria(fileName, product, dataValues);
        } catch (WTException var11) {
            if (LoadCommon.DEBUG) {
                var11.printStackTrace();
            }

            LoadCommon.display("\n#WTException : " + var11.getLocalizedMessage());
            LoadLinePlan.logLinePlan(fileName, var11.getLocalizedMessage());
        }

        if (product == null) {
            return 1;
        } else {
            dataValues.putAll(seasonValues);
            int status = validateProduct(product, dataValues, fileName);
            if (status < 0) {
                return -1;
            } else {
                try {
                    LoadCommon.loadThumbnail(product, dataValues);
                  

                    LCSSeason season = (LCSSeason)dataValues.get("Season");
                                                           
                    if (season == null && LoadCommon.getValue(dataValues, "SeasonType") != null) {
                    	if ((season = (LCSSeason)LoadCommon.searchForObject(fileName, "Season", dataValues)) == null) {
                            return -1;
                        }

                        dataValues.put("Season", season);
                        LoadCommon.display("Associating Product To Season '" + season.getName() + "' ...");
                    }

                    LoadCommon.deleteCache(fileName, "CURRENT_SEASONPRODUCTLINK");
                    
                                   
                    
                    if (!LoadHelper.save(product, dataValues, fileName)) {
                    	
                        status = -1;
                    }else {
                    	product = LoadSeason.SEASON_LOGIC.addProduct(product, season);           
                        if (product.getVersionDisplayIdentifier().toString().equals("A")) {
                        	     product = LCSSeasonQuery.getProductForSeason(product, season.getMaster());
                        }
                    	
                    }
                   
                    
                    
                    
                } catch (WTPropertyVetoException var8) {
                    if (LoadCommon.DEBUG) {
                        var8.printStackTrace();
                    }

                    LoadCommon.display("\n#WTPropertyVetoException : " + var8.getLocalizedMessage());
                    LoadLinePlan.logLinePlan(fileName, var8.getLocalizedMessage());
                } catch (WTException var9) {
                    if (LoadCommon.DEBUG) {
                        var9.printStackTrace();
                    }

                    LoadCommon.display("\n#WTException : " + var9.getLocalizedMessage());
                    LoadLinePlan.logLinePlan(fileName, var9.getLocalizedMessage());
                } catch (UnsupportedEncodingException var10) {
                    if (LoadCommon.DEBUG) {
                        var10.printStackTrace();
                    }

                    LoadCommon.display("\n#UnsupportedEncodingException : " + var10.getLocalizedMessage());
                    LoadLinePlan.logLinePlan(fileName, var10.getLocalizedMessage());
                }

                return status;
            }
        }
    }

    private static int validateProduct(LCSProduct product, Hashtable<String, Object> dataValues, String fileName) {
        int status = 0;
        String value = null;
        Vector<String> messages = CustomLoadFlexTyped.setAttributes(product, dataValues, fileName);

        for(int i = 0; i < messages.size(); ++i) {
            if ((value = (String)messages.elementAt(i)).startsWith("#ERROR")) {
                status = -1;
            }

            if (value.startsWith("#WARNING") && status == 0) {
                status = 2;
            }

            LoadLinePlan.logLinePlan(fileName, value);
        }

        if (status < 0) {
            value = "ERROR SETTING VALUES FOR THE PRODUCT !!!  FAILURE CREATING PRODUCT !!!";
            LoadCommon.display(value);
            LoadLinePlan.logLinePlan(fileName, value);
        }

        return status;
    }

    private static void initProductValues(Hashtable<String, Object> values, String fileName) {
        Hashtable<String, Object> dataValues = LoadCommon.copyHashtable(values);
        LoadCommon.getCriteria(dataValues, ":SearchCriteria");
        String lineplanDetails = "Product Details -";

        for(int i = 0; i < LoadLinePlan.LINEPLAN_PRODUCTDETAILS.length; ++i) {
            String var10001 = LoadLinePlan.LINEPLAN_PRODUCTDETAILS[i];
            String var10003 = LoadLinePlan.LINEPLAN_PRODUCTDETAILS[i];
            lineplanDetails = lineplanDetails + " " + var10001 + "='" + LoadCommon.getValue(dataValues, "flexAtt" + var10003) + "'";
        }

        LoadLinePlan.putLinePlanInfo(fileName, "LINEPLAN_DISPLAY", lineplanDetails);
    }

    public static LCSProduct saveProduct(LCSProduct product, LCSSeason season) throws WTException {
        return saveProduct(product, season, true);
    }

    public static LCSProduct saveProduct(LCSProduct product, LCSSeason season, boolean deriveStrings) throws WTException {
        LoadCommon.display("Saving Product ...");
        product = PRODUCT_LOGIC.saveProduct(product, deriveStrings);     
        if (season != null) {
            LoadCommon.display("Adding Product To Season ...");
            product = LoadSeason.SEASON_LOGIC.addProduct(product, season);           
            if (product.getVersionDisplayIdentifier().toString().equals("A")) {
            	     product = LCSSeasonQuery.getProductForSeason(product, season.getMaster());
            }
        }

        return product;
    }
}

