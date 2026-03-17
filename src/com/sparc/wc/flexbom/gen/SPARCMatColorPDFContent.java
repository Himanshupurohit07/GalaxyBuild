/*
 * MatColorPDFContent.java
 *
 * Created on Sept 13, 2007, 12:58 PM
 */

package com.sparc.wc.flexbom.gen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import wt.util.WTException;

import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.product.PDFProductSpecificationBOM2;
import com.lcs.wc.product.PDFProductSpecificationGenerator2;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.flexbom.gen.*;
/**
 *
 * @author Chuck
 */
public class SPARCMatColorPDFContent extends SPARCBOMPDFContentGenerator {

    private static final Logger LOGGER = LogManager.getLogger(SPARCMatColorPDFContent.class.getName());

    private static final String CLASSNAME = SPARCMatColorPDFContent.class.getName();

    public float tableWidthPercent = (new Float(
            LCSProperties.get("com.lcs.wc.flexbom.gen.MatColorPDFContent.tableWidthPercent", "95.0"))).floatValue();

    private static final boolean BOM_PDF_NO_EXTEND_LAST_ROW = LCSProperties
            .getBoolean("com.lcs.wc.product.PDFProductSpecificationGenerator.BOMnoExtendLastRow");

    /** Creates a new instance of MatColorPDFContent */
    public SPARCMatColorPDFContent() {
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
        LOGGER.debug("MatColorPDFContent.getPDFContentCollection");
        Collection content = new ArrayList();
        Collection spcontent = new ArrayList();
        Collection sectionPageTitles = new ArrayList();
        FlexBOMPart bomPart = (FlexBOMPart) params.get(SPARCBOMPDFContentGenerator.BOM_PART);
        Map tparams = new HashMap(params.size() + 2);
        tparams.putAll(params);

        SPARCMatColorGenerator bomDG = new SPARCMatColorGenerator();
        Collection allSkus = (Collection) params.get(SPARCBomDataGenerator.COLORWAYS);
        int maxPerPage = ((Integer) params.get(PDFProductSpecificationGenerator2.COLORWAYS_PER_PAGE)).intValue();

        // Create collection of arrayLists of skus
        Collection skusArray = splitItems(allSkus, maxPerPage);

        Iterator skusIt = null;

        Collection skusThisRun = new ArrayList();

        Collection sections = bomPart.getFlexType().getAttribute("section").getAttValueList()
                .getSelectableKeys(com.lcs.wc.client.ClientContext.getContext().getLocale(), true);
        String section = "";
        Iterator sectionIter = sections.iterator();

        while (sectionIter.hasNext()) {
            section = (String) sectionIter.next();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("-adding section :  " + section);
            }
            tparams.put(SPARCBomDataGenerator.SECTION, section);
            setSectionViewId(tparams);

            skusIt = skusArray.iterator();
            while (skusIt.hasNext()) {
                skusThisRun = (Collection) skusIt.next();
                // LOGGER.debug("skusThisRun: " + skusThisRun);
                tparams.put(SPARCBomDataGenerator.COLORWAYS, skusThisRun);
                bomDG = new SPARCMatColorGenerator();
                bomDG.init(tparams);
                Collection data = bomDG.getBOMData();
                Collection columns = bomDG.getTableColumns();

                spcontent.addAll(generatePDFPage(data, columns, document, tparams));
                if (!BOM_ON_SINGLE_PAGE) {
                    sectionPageTitles.add(getPageTitleText(tparams));
                }
            }

        }

        PdfPTable fullBOMTable = new PdfPTable(1);
        PdfPTable e = null;
        PdfPCell cell = null;
        fullBOMTable.setWidthPercentage(tableWidthPercent);
        // Add the BOM Header Attributes
        if (FormatHelper.parseBoolean((String) params.get(PDFProductSpecificationBOM2.PRINT_BOM_HEADER))) {
            LOGGER.trace(
                    "FormatHelper.parseBoolean((String)params.get(PDFProductSpecificationBOM2.PRINT_BOM_HEADER) )");
            Collection BomHeaderAtts = (Collection) params.get(PDFProductSpecificationBOM2.BOM_HEADER_ATTS);
            if (BomHeaderAtts != null && !BomHeaderAtts.isEmpty()) {
                LOGGER.trace("BomHeaderAtts != null && !BomHeaderAtts.isEmpty()");
                if (PDFProductSpecificationBOM2.BOM_HEADER_SAME_PAGE && BOM_ON_SINGLE_PAGE) {
                    LOGGER.trace("PDFProductSpecificationBOM2.BOM_HEADER_SAME_PAGE  && BOM_ON_SINGLE_PAGE");
                    for (Iterator HeaderI = BomHeaderAtts.iterator(); HeaderI.hasNext();) {
                        e = (PdfPTable) HeaderI.next();
                        cell = new PdfPCell(e);
                        fullBOMTable.addCell(cell);
                    }
                } else {
                    LOGGER.trace("NOT--PDFProductSpecificationBOM2.BOM_HEADER_SAME_PAGE  && BOM_ON_SINGLE_PAGE");
                    content.add(BomHeaderAtts);
                    this.pageTitles.addAll((Collection) params.get(PDFProductSpecificationBOM2.BOM_HEADER_PAGE_TITLES));
                }
            }
        }

        // Add BOM Sections
        Collection BOMFooter = (Collection) params.get(PDFProductSpecificationBOM2.BOM_FOOTER_ATTS);
        boolean usingFooter = (BOMFooter != null && !BOMFooter.isEmpty()
                && FormatHelper.parseBoolean((String) params.get(PDFProductSpecificationBOM2.PRINT_BOM_FOOTER)));
        if (BOM_ON_SINGLE_PAGE) {
            LOGGER.trace("BOM_ON_SINGLE_PAGE");
            Iterator sci = spcontent.iterator();
            while (sci.hasNext()) {
                e = (PdfPTable) sci.next();
                cell = new PdfPCell(e);
                fullBOMTable.addCell(cell);
            }

            if (BOM_PDF_NO_EXTEND_LAST_ROW) {
                fullBOMTable.setSplitLate(false);
            }
            // Add Footer
            if (usingFooter) {
                LOGGER.trace("usingFooter");
                if (PDFProductSpecificationBOM2.BOM_FOOTER_SAME_PAGE) {
                    for (Iterator footI = BOMFooter.iterator(); footI.hasNext();) {
                        e = (PdfPTable) footI.next();
                        cell = new PdfPCell(e);
                        fullBOMTable.addCell(cell);
                    }
                    // Add BOM to content
                    content.add(fullBOMTable);
                    this.pageTitles.add(getPageTitleText(tparams));
                } else {
                    // Add BOM to content
                    content.add(fullBOMTable);
                    this.pageTitles.add(getPageTitleText(tparams));
                    // Add Footer to content
                    content.addAll(BOMFooter);
                    this.pageTitles.addAll((Collection) params.get(PDFProductSpecificationBOM2.BOM_FOOTER_PAGE_TITLES));
                }
            } else {
                content.add(fullBOMTable);
                this.pageTitles.add(getPageTitleText(tparams));
            }
        } else { // BOM sections different pages
            this.pageTitles.addAll(sectionPageTitles);
            Iterator sci = spcontent.iterator();
            // Add the first section to the fullBOMTable in case we have a Header
            e = (PdfPTable) sci.next();
            cell = new PdfPCell(e);
            fullBOMTable.addCell(cell);
            content.add(fullBOMTable);
            while (sci.hasNext()) {
                e = (PdfPTable) sci.next();
                if (!sci.hasNext() && usingFooter) {
                    // Last element && using a footer
                    if (PDFProductSpecificationBOM2.BOM_FOOTER_SAME_PAGE) {
                        fullBOMTable = new PdfPTable(1);
                        cell = new PdfPCell(e);
                        fullBOMTable.addCell(cell);
                        for (Iterator footI = BOMFooter.iterator(); footI.hasNext();) {
                            e = (PdfPTable) footI.next();
                            cell = new PdfPCell(e);
                            fullBOMTable.addCell(cell);
                        }
                        content.add(fullBOMTable);
                    } else {
                        // Add last element
                        content.add(e);
                        // Add Footer
                        content.addAll(BOMFooter);
                        this.pageTitles
                                .addAll((Collection) params.get(PDFProductSpecificationBOM2.BOM_FOOTER_PAGE_TITLES));
                    }
                } else {
                    // Not the last element
                    content.add(e);
                }
            } // while
        }

        return content;
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
