/*
 * SingleBOMPDFContent.java
 *
 * Created on November 17, 2006, 2:40 PM
 */

package com.sparc.wc.flexbom.gen;

import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lcs.wc.flexbom.*;
import com.lcs.wc.foundation.*;
import com.lcs.wc.sizing.*;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.*;
import com.lcs.wc.util.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import wt.util.*;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.flexbom.gen.*;

/**
 *
 * @author Chuck
 */
public class SPARCSingleBOMPDFContent extends SPARCBOMPDFContentGenerator {
    private static final Logger LOGGER = LogManager.getLogger(SPARCSingleBOMPDFContent.class.getName());

    public float tableWidthPercent = (new Float(
            LCSProperties.get("com.lcs.wc.flexbom.gen.SingleBOMPDFContent.tableWidthPercent", "95.0"))).floatValue();

    private static final boolean BOM_PDF_NO_EXTEND_LAST_ROW = LCSProperties
            .getBoolean("com.lcs.wc.product.PDFProductSpecificationGenerator.BOMnoExtendLastRow");

    private Map titleCache = new HashMap();

    private static final String SIZE1LABEL = "SIZE1LABEL";

    private static final String SIZE2LABEL = "SIZE2LABEL";

    /** Creates a new instance of SingleBOMPDFContent */
    public SPARCSingleBOMPDFContent() {
    }

    /**
     * gets an Element for insertion into a PDF Document
     * 
     * @param params
     *            A Map of parameters to pass to the Object. This provides the means for the calling class to have some
     *            "fore" knowledge of what implementations are being used and pass appropriate parameters.
     * @param document
     *            The PDF Document which the content is going to be added to. The document is passed in order to provide
     *            additional information related to the Document itself incase it is not provided in the params
     * @throws WTException
     *             For any error
     * @return an Element for insertion into a Document
     */
    public Collection getPDFContentCollection(Map params, Document document) throws WTException {
        ArrayList content = new ArrayList();
        ArrayList spcontent = new ArrayList();

        // Add the BOM Header Attributes
        if (FormatHelper.parseBoolean((String) params.get(PDFProductSpecificationBOM2.PRINT_BOM_HEADER))) {
            Collection BomHeaderAtts = (Collection) params.get(PDFProductSpecificationBOM2.BOM_HEADER_ATTS);
            if (BomHeaderAtts != null && !BomHeaderAtts.isEmpty()) {
                if (PDFProductSpecificationBOM2.BOM_HEADER_SAME_PAGE) {
                    if (BOM_ON_SINGLE_PAGE) {
                        spcontent.addAll(BomHeaderAtts);
                    } else {
                        content.addAll(BomHeaderAtts);
                        this.pageTitles
                                .addAll((Collection) params.get(PDFProductSpecificationBOM2.BOM_HEADER_PAGE_TITLES));
                    }
                } else {
                    content.add(BomHeaderAtts);
                    this.pageTitles.addAll((Collection) params.get(PDFProductSpecificationBOM2.BOM_HEADER_PAGE_TITLES));
                }
            }
        }

        Collection sources = (Collection) params.get(SPARCBomDataGenerator.SOURCES);
        Collection dests = (Collection) params.get(SPARCBomDataGenerator.DESTINATIONS);
        Collection colorways = (Collection) params.get(SPARCBomDataGenerator.COLORWAYS);
        Collection sizes1 = (Collection) params.get(SPARCBomDataGenerator.SIZES1);
        Collection sizes2 = (Collection) params.get(SPARCBomDataGenerator.SIZES2);
        FlexBOMPart bomPart = (FlexBOMPart) params.get(SPARCBOMPDFContentGenerator.BOM_PART);
        Collection sections = bomPart.getFlexType().getAttribute("section").getAttValueList()
                .getSelectableKeys(com.lcs.wc.client.ClientContext.getContext().getLocale(), true);

        HashMap tparams = new HashMap(params.size() + 6);
        tparams.putAll(params);

        // If there is no collection add a null value so the iterator works
        if (sources == null || sources.size() < 1) {
            sources = new ArrayList();
            sources.add(null);
        }

        if (dests == null || dests.size() < 1) {
            dests = new ArrayList();
            dests.add(null);
        }
        if (colorways == null || colorways.size() < 1) {
            colorways = new ArrayList();
            colorways.add(null);
        }
        if (sizes1 == null || sizes1.size() < 1) {
            sizes1 = new ArrayList();
            sizes1.add(null);
        }
        if (sizes2 == null || sizes2.size() < 1) {
            sizes2 = new ArrayList();
            sizes2.add(null);
        }
        Iterator sourcesIt = sources.iterator();
        Iterator destsIt;
        Iterator colorwayIt;
        Iterator sizes1It;
        Iterator sizes2It;
        Iterator sectionIter;

        String source = "";
        String dest = "";
        String colorway = "";
        String size1 = "";
        String size2 = "";
        String section = "";

        while (sourcesIt.hasNext()) {
            source = (String) sourcesIt.next();
            tparams.put(SPARCSingleBOMGenerator.SOURCE_ID, source);
            destsIt = dests.iterator();

            while (destsIt.hasNext()) {
                dest = (String) destsIt.next();
                tparams.put(SPARCSingleBOMGenerator.DESTINATION_ID, dest);
                colorwayIt = colorways.iterator();

                while (colorwayIt.hasNext()) {
                    colorway = (String) colorwayIt.next();
                    tparams.put(SPARCSingleBOMGenerator.SKU_ID, colorway);
                    sizes1It = sizes1.iterator();

                    while (sizes1It.hasNext()) {
                        size1 = (String) sizes1It.next();
                        // LOGGER.debug("size1: " + size1);
                        tparams.put(SPARCSingleBOMGenerator.SIZE1_VAL, size1);
                        sizes2It = sizes2.iterator();

                        while (sizes2It.hasNext()) {
                            size2 = (String) sizes2It.next();
                            // LOGGER.debug("size2: " + size2);
                            tparams.put(SPARCSingleBOMGenerator.SIZE2_VAL, size2);
                            sectionIter = sections.iterator();

                            while (sectionIter.hasNext()) {
                                section = (String) sectionIter.next();
                                tparams.put(SPARCBomDataGenerator.SECTION, section);
                                setSectionViewId(tparams);
                                if (BOM_ON_SINGLE_PAGE) {
                                    spcontent.addAll(generateSingleBOMPage(tparams, document));
                                } else {
                                    Collection sbp = generateSingleBOMPage(tparams, document);
                                    Iterator sbpi = sbp.iterator();
                                    PdfPTable stable = null;
                                    while (sbpi.hasNext()) {
                                        stable = (PdfPTable) sbpi.next();
                                        stable.setWidthPercentage(tableWidthPercent);
                                    }
                                    content.addAll(sbp);
                                    this.pageTitles.add(getPageTitleText(tparams));
                                }
                            } // End section iterator
                              // Footer
                            if (FormatHelper
                                    .parseBoolean((String) params.get(PDFProductSpecificationBOM2.PRINT_BOM_FOOTER))) {
                                Collection BOMFooter = (Collection) params
                                        .get(PDFProductSpecificationBOM2.BOM_FOOTER_ATTS);
                                if (BOMFooter != null && !BOMFooter.isEmpty()) {
                                    if (PDFProductSpecificationBOM2.BOM_FOOTER_SAME_PAGE) {
                                        if (BOM_ON_SINGLE_PAGE) {
                                            spcontent.addAll(BOMFooter);
                                        } else {
                                            content.addAll(BOMFooter);
                                            this.pageTitles.addAll((Collection) params
                                                    .get(PDFProductSpecificationBOM2.BOM_FOOTER_PAGE_TITLES));
                                        }
                                    } else {
                                        content.add(BOMFooter);
                                        this.pageTitles.addAll((Collection) params
                                                .get(PDFProductSpecificationBOM2.BOM_FOOTER_PAGE_TITLES));
                                    }

                                }
                            }

                            if (BOM_ON_SINGLE_PAGE) {
                                PdfPTable fullBOMTable = new PdfPTable(1);
                                fullBOMTable.setWidthPercentage(tableWidthPercent);

                                Iterator sci = spcontent.iterator();
                                PdfPTable e = null;
                                PdfPCell cell = null;
                                while (sci.hasNext()) {
                                    e = (PdfPTable) sci.next();
                                    cell = new PdfPCell(e);

                                    fullBOMTable.addCell(cell);
                                }

                                if (BOM_PDF_NO_EXTEND_LAST_ROW) {
                                    fullBOMTable.setSplitLate(false);
                                }

                                content.add(fullBOMTable);

                                this.pageTitles.add(getPageTitleText(tparams));
                                spcontent = new ArrayList();
                            }
                        } // End sizes2 iterator
                    } // End Sizes1 iterator
                } // End Colorway iterator
            } // End Dests iterator
        } // Emd Source iterator
        return content;
    }

    public Collection generateSingleBOMPage(Map params, Document document) throws WTException {

        // params.remove("RAW_DATA");
        // LOGGER.debug("generateSingleBOMPage(): params- " + params);

        SPARCSingleBOMGenerator bomDG = new SPARCSingleBOMGenerator();
        // LOGGER.debug("gsbp - size1: " + params.get(SPARCSingleBOMGenerator.SIZE1_VAL));
        // LOGGER.debug("gsbp - size2: " + params.get(SPARCSingleBOMGenerator.SIZE2_VAL));
        bomDG.init(params);
        // Collection data = new ArrayList();
        Collection data = bomDG.getBOMData();
        Collection columns = bomDG.getTableColumns();

        return generatePDFPage(data, columns, document, params);
    }

    /**
     * Returns the text to use as the page title
     */
    public String getPageTitleText(Map params) {
        String title = "";
        FlexBOMPart bomPart = (FlexBOMPart) params.get(SPARCBOMPDFContentGenerator.BOM_PART);

        title = bomPart.getName() + " -- " + params.get(PDFProductSpecificationGenerator2.REPORT_NAME);

        return title;
    }

    public String getTitleCellCenterText(Map params) throws WTException {
        String centerText = "";

        // Add destination label
        String destinationLabel = LCSMessage.getLocalizedMessage(RB.FLEXBOM, "destination_LBL");
        String destinationName = (String) this.titleCache.get(params.get(SPARCSingleBOMGenerator.DESTINATION_ID));
        String destId = (String) params.get(SPARCSingleBOMGenerator.DESTINATION_ID);
        if (!FormatHelper.hasContentAllowZero(destinationName) && FormatHelper.hasContent(destId)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("--Looking up destinationName:  " + destId);
            }
            if (destId.indexOf("ProductDestination") < 0) {
                destId = "OR:com.lcs.wc.product.ProductDestination:" + destId;
            }
            ProductDestination dest = (ProductDestination) LCSQuery.findObjectById(destId);
            destinationName = dest.getDestinationName();
            titleCache.put(params.get(SPARCSingleBOMGenerator.DESTINATION_ID), destinationName);
        }
        if (FormatHelper.hasContentAllowZero(destinationName)) {
            centerText = centerText + destinationLabel + destinationName;
        }

        // Add colorway Label
        String colorwayLabel = LCSMessage.getLocalizedMessage(RB.FLEXBOM, "colorwayColon_LBL");
        String colorwayName = (String) titleCache.get(params.get(SPARCSingleBOMGenerator.SKU_ID));
        String skuId = (String) params.get(SPARCSingleBOMGenerator.SKU_ID);
        if (!FormatHelper.hasContentAllowZero(colorwayName) && FormatHelper.hasContent(skuId)) {
            String skuMasterId = "";
            if (skuId.indexOf("LCSSKU:") > -1) {
                LCSSKU sku = (LCSSKU) LCSQuery.findObjectById(skuId);
                skuMasterId = FormatHelper.getNumericObjectIdFromObject((LCSPartMaster) sku.getMaster());
            } else if (skuId.indexOf("LCSPartMaster") > -1) {
                skuMasterId = FormatHelper.getNumericFromOid(skuId);
            } else {
                skuMasterId = skuId;
            }
            LCSSKU skuArev = LCSSKUQuery.getSKURevA(skuMasterId);
            colorwayName = skuArev.getName();

            titleCache.put(params.get(SPARCSingleBOMGenerator.SKU_ID), colorwayName);
        }
        if (FormatHelper.hasContentAllowZero(colorwayName)) {
            if (FormatHelper.hasContent(centerText)) {
                centerText = centerText + ", ";
            }
            centerText = centerText + colorwayLabel + colorwayName;
        }

        ProductSizeCategory productSizeCategory;
        // Add Size1 label
        String size1Value = (String) params.get(SPARCSingleBOMGenerator.SIZE1_VAL);
        if (FormatHelper.hasContentAllowZero(size1Value)) {
            String size1Label = (String) titleCache.get(SIZE1LABEL);
            if (!FormatHelper.hasContent(size1Label)) {
                if (FormatHelper
                        .hasContent((String) params.get(PDFProductSpecificationGenerator2.PRODUCT_SIZE_CAT_ID))) {
                    productSizeCategory = (ProductSizeCategory) LCSQuery
                            .findObjectById((String) params.get(PDFProductSpecificationGenerator2.PRODUCT_SIZE_CAT_ID));
                    size1Label = productSizeCategory.getSizeRange().getFullSizeRange().getSize1Label() + ":  ";
                } else {
                    size1Label = LCSMessage.getLocalizedMessage(RB.QUERYDEFINITION, "size1");
                }
                titleCache.put(SIZE1LABEL, size1Label);
            }
            if (FormatHelper.hasContent(centerText)) {
                centerText = centerText + ", ";
            }
            centerText = centerText + size1Label + size1Value;
        }

        // Add size 2 label
        String size2Value = (String) params.get(SPARCSingleBOMGenerator.SIZE2_VAL);
        if (FormatHelper.hasContentAllowZero(size2Value)) {
            String size2Label = (String) titleCache.get(SIZE2LABEL);
            if (!FormatHelper.hasContent(size2Label)) {
                if (FormatHelper
                        .hasContent((String) params.get(PDFProductSpecificationGenerator2.PRODUCT_SIZE_CAT_ID))) {
                    productSizeCategory = (ProductSizeCategory) LCSQuery
                            .findObjectById((String) params.get(PDFProductSpecificationGenerator2.PRODUCT_SIZE_CAT_ID));
                    size2Label = productSizeCategory.getSizeRange().getFullSizeRange().getSize2Label() + ":  ";
                } else {
                    size2Label = LCSMessage.getLocalizedMessage(RB.QUERYDEFINITION, "size1");
                }
                titleCache.put(SIZE2LABEL, size2Label);
            }
            if (FormatHelper.hasContent(centerText)) {
                centerText = centerText + ", ";
            }
            centerText = centerText + size2Label + size2Value;
        }

        if (!FormatHelper.hasContent(centerText)) {
            centerText = LCSMessage.getLocalizedMessage(RB.FLEXBOM, "topLevelBOM_LBL");
        }

        return centerText;
    }

}
