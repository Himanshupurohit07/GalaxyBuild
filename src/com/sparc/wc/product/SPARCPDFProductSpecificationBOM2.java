/*
 * PDFProductSpecificationBOM2.java
 *
 * Created on August 24, 2005, 2:52 PM
 */

package com.sparc.wc.product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wt.util.WTException;

import com.lcs.wc.client.web.pdf.PDFContentCollection;
import com.lcs.wc.client.web.pdf.PDFFlexTypeGenerator;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.flexbom.gen.BOMPDFContentGenerator;
import com.lcs.wc.flexbom.gen.BomDataGenerator;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.material.MaterialPriceList;
import com.lcs.wc.util.ClassLoadUtil;
import com.lcs.wc.util.FileLocation;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSMessage;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.RB;
import com.lowagie.text.Document;
import com.lcs.wc.product.*;

import com.sparc.wc.flexbom.gen.*;
/**
 * Writes the BOM section of a PDF Product Specification
 * 
 * @author Chuck
 */
public class SPARCPDFProductSpecificationBOM2 implements PDFContentCollection, SpecPageSet {
    private static final Logger LOGGER = LogManager.getLogger(SPARCPDFProductSpecificationBOM2.class.getName());

    String operationLabel = LCSMessage.getLocalizedMessage(RB.FLEXBOM, "operation_LBL");

    String materialLabel = LCSMessage.getLocalizedMessage(RB.MATERIAL, "material_LBL");

    String colorLabel = LCSMessage.getLocalizedMessage(RB.COLOR, "color_LBL");

    String supplierLabel = LCSMessage.getLocalizedMessage(RB.FLEXBOM, "supplier_LBL");

    String materialStatusLabel = LCSMessage.getLocalizedMessage(RB.FLEXBOM, "materialStatus_LBL");

    public static final boolean BOM_HEADER_SAME_PAGE = LCSProperties
            .getBoolean("com.lcs.wc.product.PDFProductSpecificationGenerator.BOMHeaderSamePage");

    public static final boolean BOM_HEADER_EVERY_REPORT = LCSProperties
            .getBoolean("com.lcs.wc.product.PDFProductSpecificationGenerator.BOMHeaderEveryReport");

    public static final boolean BOM_FOOTER_SAME_PAGE = LCSProperties
            .getBoolean("com.lcs.wc.product.PDFProductSpecificationGenerator.BOMFooterSamePage");

    public static final boolean BOM_FOOTER_EVERY_REPORT = LCSProperties
            .getBoolean("com.lcs.wc.product.PDFProductSpecificationGenerator.BOMFooterEveryReport");

    protected ClassLoadUtil clu = null;

    public static String PRODUCT_ID = "PRODUCT_ID";

    public static String SPEC_ID = "SPEC_ID";

    public static String HEADER_HEIGHT = "HEADER_HEIGHT";

    public static final String BOM_HEADER_CLASS = "BOM_HEADER_CLASS";

    public static final String BOM_FOOTER_CLASS = "BOM_FOOTER_CLASS";

    public static final String BOL_HEADER_CLASS = "BOL_HEADER_CLASS";

    public static final String BOL_FOOTER_CLASS = "BOL_FOOTER_CLASS";

    // public static String BOM_SECTIONS = "BOM_SECTIONS";
    public static String COLORWAYS = "COLORWAYS";

    public static String SOURCES = "SOURCES";

    public static String DESTINATIONS = "DESTINATIONS";

    public static final boolean COSTING = LCSProperties.getBoolean("jsp.flexbom.costedBOM");

    public static final String viewBOMMode = "VIEW";

    public static final String skuMode = "SINGLE";

    // Header
    public static final String BOM_HEADER_ATTS = "BOM_HEADER_ATTS";

    public static final String BOM_HEADER_PAGE_TITLES = "BOM_HEADER_PAGE_TITLES";

    public static final String PRINT_BOM_HEADER = "PRINT_BOM_HEADER";

    public static final String PRINT_BOM_HEADER_SAME_PAGE = "PRINT_BOM_HEADER_SAME_PAGE";

    // Footer
    public static final String BOM_FOOTER_ATTS = "BOM_FOOTER_ATTS";

    public static final String BOM_FOOTER_PAGE_TITLES = "BOM_FOOTER_PAGE_TITLES";

    public static final String PRINT_BOM_FOOTER = "PRINT_BOM_FOOTER";

    public static final String PRINT_BOM_FOOTER_SAME_PAGE = "PRINT_BOM_FOOTER_SAME_PAGE";

    private static final String MAT_COLOR_COLUMN = "LCSMATERIALCOLOR.IDA2A2";

    private static final String MAT_SUPPLIER_COLUMN = "LCSMATERIALSUPPLIERMASTER.IDA2A2";

    private static String PRICE_COLUMN = "";

    static {
        try {
            PRICE_COLUMN = "LCSMATERIALSUPPLIER."
                    + FlexTypeCache.getFlexTypeRoot("Material").getAttribute("materialPrice").getColumnName();
        } catch (Exception e) {
            System.out.println("ERROR: Can not determing the matrialPrice attribute for Material type");
            e.printStackTrace();
        }
    }

    public float pageHeight = 0;

    Collection pageTitles = new ArrayList();

    String propertyFile = null;

    /** Creates a new instance of PDFProdudctSpecificationMeasurements */
    public SPARCPDFProductSpecificationBOM2() {
    }

    /**
     * returns the Collection of PdfPTables containing the BOMs for the specification
     * 
     * @param params
     * @param document
     * @throws WTException
     * @return
     */
    public Collection getPDFContentCollection(Map params, Document document) throws WTException {
        LOGGER.debug("SPARCPDFProductSpecificationBOM2.getPDFContentCollection()");
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("params.keySet():  " + params.keySet());
        try {
            String Id = (String) params.get(PDFProductSpecificationGenerator2.COMPONENT_ID);
            // System.out.println("Id : " + Id);
            this.pageTitles = new ArrayList();
            FlexBOMPart bomPart = (FlexBOMPart) LCSQuery.findObjectById(Id);
            params.put(SPARCBOMPDFContentGenerator.BOM_PART, bomPart);
            params.put(PDFFlexTypeGenerator.FLEXTYPED, bomPart);
            params.put(PRINT_BOM_HEADER_SAME_PAGE, String.valueOf(BOM_HEADER_SAME_PAGE));
            params.put(PRINT_BOM_FOOTER_SAME_PAGE, String.valueOf(BOM_FOOTER_SAME_PAGE));

            // Add the PDF of the BOM Flex Header Attributes to the params map
            getBOMHeaderAndFooter(params, document);
            // If we are only printing the footer once, remove print_footer until last report
            boolean printFooter = FormatHelper.parseBoolean((String) params.get(PRINT_BOM_FOOTER));
            if (printFooter && !BOM_FOOTER_EVERY_REPORT) {
                params.remove(PRINT_BOM_FOOTER);
            }

            if (FormatHelper.hasContent(this.propertyFile)) {
                clu = new ClassLoadUtil(this.propertyFile);
            } else {
                clu = new ClassLoadUtil(FileLocation.productSpecBOMProperties2);
            }

            Collection bomOptions = (Collection) params.get(PDFProductSpecificationGenerator2.COMPONENT_PAGE_OPTIONS);

            this.pageHeight = this.calcPageHeight(params, document);

            // PDFTableGenerator tg = null;
            Collection tables = new ArrayList();
            String option = "";

            if (bomOptions != null && bomOptions.size() > 0) {
                Iterator bomOptionsIter = bomOptions.iterator();

                // Add the BOM Data
                params.put(SPARCBomDataGenerator.RAW_BOM_DATA, getBOMData(bomPart));
                while (bomOptionsIter.hasNext()) {
                    option = (String) bomOptionsIter.next();
                    // If last report & only print footer on last report
                    if (!bomOptionsIter.hasNext() && printFooter && !BOM_FOOTER_EVERY_REPORT) {
                        params.put(PRINT_BOM_FOOTER, "true");
                    }

                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("Creating PDF BOM Option :  " + option);
                    SPARCBOMPDFContentGenerator bomCG = (SPARCBOMPDFContentGenerator) clu.getClass(option);
                    if (clu.getParams(option) != null) {
                        params.putAll(clu.getParams(option));
                    }

                    String display = LCSMessage.getLocalizedMessage(RB.FLEXBOM, option);
                    params.put(PDFProductSpecificationGenerator2.REPORT_NAME, display);
                    params.put(PDFProductSpecificationGenerator2.REPORT_KEY, option);

                    if (bomCG == null) {
                        System.out.println("#Could not find a class for option : " + option + ".  Skipping BOM Option");
                    }
                    tables.addAll(bomCG.getPDFContentCollection(params, document));
                    pageTitles.addAll(bomCG.getPageTitles());

                    if (!BOM_HEADER_EVERY_REPORT && params.containsKey(PRINT_BOM_HEADER)) {
                        params.remove(PRINT_BOM_HEADER);
                    }
                }
            }

            return tables;

        } catch (Exception e) {
            throw new WTException(e);
        }
    }

    private float calcPageHeight(Map params, Document doc) throws WTException {
        float height = doc.top() - doc.bottom();

        if (params.get(HEADER_HEIGHT) != null) {
            Object hh = params.get("HEADER_HEIGHT");
            if (hh instanceof Float) {
                height = height - ((Float) hh).floatValue();
            }
            if (hh instanceof String) {
                height = height - (new Float((String) hh)).floatValue();
            }
        }

        return height;
    }

    /**
     * gets the Collection of page titles for each of the BOMs returned
     * 
     * @return
     */
    public Collection getPageHeaderCollection() {
        return this.pageTitles;
    }

    public Collection getBOMData(FlexBOMPart bomPart) throws WTException {
        Collection qresults = new ArrayList();

        FlexType bomType = bomPart.getFlexType();
        FlexType materialType = bomType.getReferencedFlexType(ReferencedTypeKeys.MATERIAL_TYPE);

        // USing LCSFlexBOMQuery.EFFECTIVE_ONLY, so we print out the effective copy, not the working copy
        qresults = LCSFlexBOMQuery
                .findFlexBOMData(bomPart, null, null, null, null, null, LCSFlexBOMQuery.EFFECTIVE_ONLY, null, false,
                        false, LCSFlexBOMQuery.ALL_DIMENSIONS, null, null, null, materialType)
                .getResults();

        qresults = LCSFlexBOMQuery.bomSort(qresults, bomPart.getFlexType());

        qresults = LCSFlexBOMQuery.joinInLinkedBOMs(qresults);

        mergeMaterialColorPricing(qresults);

        return qresults;
    }

    private void mergeMaterialColorPricing(Collection data) throws WTException {

        Iterator i = data.iterator();
        FlexObject record = null;

        Collection matSups = new ArrayList();
        Collection matSupColors = new ArrayList();

        Map matSup = null;
        Map matSupColor = null;

        String materialColorId = "";
        String materialSupplierMasterId = "";

        double materialPrice;

        while (i.hasNext()) {
            record = (FlexObject) i.next();
            matSupColor = new HashMap();
            matSup = new HashMap();

            // Get material supplier and material color for each record
            materialColorId = FormatHelper.format(record.getData(MAT_COLOR_COLUMN));
            materialSupplierMasterId = FormatHelper.format(record.getData(MAT_SUPPLIER_COLUMN));

            matSup.put("materialSupplierMasterId", materialSupplierMasterId);

            matSupColor.put("materialSupplierMasterId", materialSupplierMasterId);
            matSupColor.put("materialColorId", materialColorId);

            matSups.add(matSup);
            matSupColors.add(matSupColor);
        }

        if (matSupColors.size() > 0) {
            // This may need to be updated to come from a parameter to allow for effectivity based pricing
            Date reqDate = new Date();

            MaterialPriceList mpl = new MaterialPriceList(matSups, matSupColors, reqDate);

            i = data.iterator();
            while (i.hasNext()) {
                record = (FlexObject) i.next();

                materialColorId = FormatHelper.format(record.getData(MAT_COLOR_COLUMN));
                materialSupplierMasterId = FormatHelper.format(record.getData(MAT_SUPPLIER_COLUMN));

                // use the materialPriceList to get the price for a specific material/supplier/color
                materialPrice = mpl.getPrice(materialSupplierMasterId, materialColorId);

                record.put(PRICE_COLUMN, materialPrice);
            }

        }
    }

    public void getBOMHeaderAndFooter(Map params, Document document) throws WTException {
        try {
            // Header
            String getAtts = (String) params.get(PRINT_BOM_HEADER);
            BOMPDFContentGenerator bomAttCG = (BOMPDFContentGenerator) params.get(BOM_HEADER_CLASS);
            if (FormatHelper.parseBoolean(getAtts) && bomAttCG != null) {
                LOGGER.debug("getBOMHeaderAndFooter():--Generating header attributes");

                bomAttCG.init();
                params.put(BOM_HEADER_ATTS, bomAttCG.getPDFContentCollection(params, document));
                params.put(BOM_HEADER_PAGE_TITLES, bomAttCG.getPageTitles());
            } else {
                LOGGER.debug("getBOMHeaderAndFooter():Not generating header attributes");
            }

            // Footer
            String useFooter = (String) params.get(PRINT_BOM_FOOTER);
            bomAttCG = (BOMPDFContentGenerator) params.get(BOM_FOOTER_CLASS);
            if (FormatHelper.parseBoolean(useFooter) && bomAttCG != null) {
                LOGGER.debug("getBOMHeaderAndFooter():--Generating footer");

                bomAttCG.init();
                params.put(BOM_FOOTER_ATTS, bomAttCG.getPDFContentCollection(params, document));
                params.put(BOM_FOOTER_PAGE_TITLES, bomAttCG.getPageTitles());
            } else {
                LOGGER.debug("getBOMHeaderAndFooter():Not generating footer");
            }

        } catch (Exception e) {
            throw new WTException(e);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    /**
     * debug method is no longer supported, please use log4j logger of the class.
     */
    @Deprecated
    public static void debug(String msg) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(msg);
        }
    }

}
