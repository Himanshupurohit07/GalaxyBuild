/*
 * PDFProductSpecPageHeaderGenerator.java
 *
 * Created on August 31, 2005, 9:20 AM
 */

package com.sparc.wc.product;

import com.lcs.wc.client.web.*;
import com.lcs.wc.client.web.pdf.*;
import com.lcs.wc.util.*;
import wt.util.WTMessage;

import java.awt.Color;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lcs.wc.product.*;

/**
 * Writes the title bar at the top of each PDF Spec page. Writes the title, the "status", and the page number
 */
public class PDFProductSpecPageHeaderGenerator extends PdfPageEventHelper {
    /** An Image that goes in the header. */
    public Image headerImage;

    /** The headertable. */
    public PdfPTable table;

    /** The Graphic state */
    public PdfGState gstate;

    /** A template that will hold the total number of pages. */
    public PdfTemplate tpl;

    /** The font that will be used. */
    public BaseFont font;

    public String headerTextLeft = "Test1";

    public String headerTextRight = " ";

    public String headerTextCenter = "TEST";

    public String fontClass = "TABLESECTIONHEADER";

    public String pageNumFontClass = "PAGE_NUMBERS";

    public PDFGeneratorHelper pgh = new PDFGeneratorHelper();

    public float cellHeight = 15.0f;

    /**
     * Generates a document with a header containing Page x of y and with a Watermark on every page.
     * 
     * @param args
     *            no arguments needed
     */
    public static void main(String args[]) {
        try {
            // step 1: creating the document
            Document doc = new Document(PageSize.A4, 50, 50, 100, 72);
            // step 2: creating the writer
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream("test.pdf"));
            // step 3: initialisations + opening the document
            writer.setPageEvent(new PDFProductSpecPageHeaderGenerator());
            doc.open();
            // step 4: adding content
            String text = "some padding text ";
            for (int k = 0; k < 10; ++k)
                text += text;
            Paragraph p = new Paragraph(text);
            p.setAlignment(Element.ALIGN_JUSTIFIED);
            doc.add(p);
            // step 5: closing the document
            doc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up the document for having the header written
     * 
     * @see com.lowagie.text.pdf.PdfPageEventHelper#onOpenDocument(com.lowagie.text.pdf.PdfWriter,
     *      com.lowagie.text.Document)
     * @param writer
     * @param document
     */

    public void onOpenDocument(PdfWriter writer, Document document) {
        try {

            // initialization of the template
            // CHUCK - Not sure what these numbers mean
            tpl = writer.getDirectContent().createTemplate(100, 100);

            // CHUCK - Not sure what a bounding box is
            tpl.setBoundingBox(new Rectangle(-20, -20, 100, 100));

            // initialization of the font
            font = BaseFont.createFont("Helvetica", BaseFont.WINANSI, false);

        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * on the end of the page the title bar is written to the page
     * 
     * @see com.lowagie.text.pdf.PdfPageEventHelper#onEndPage(com.lowagie.text.pdf.PdfWriter, com.lowagie.text.Document)
     * @param writer
     * @param document
     */
    public void onEndPage(PdfWriter writer, Document document) {
        try {

            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            cb.restoreState();
			PdfContentByte cb2 = writer.getDirectContent();
            cb2.saveState();
            cb2.restoreState();

            Font cellfont = pgh.getCellFont(fontClass, null, "8");

            // write the headertable
            PdfPTable table = new PdfPTable(3);
			PdfPTable table2 = new PdfPTable(4);
            table.setTotalWidth(document.right() - document.left());
			table2.setTotalWidth(document.right() - document.left());


            PdfPCell left = new PdfPCell(pgh.multiFontPara("	", cellfont));
            left.setHorizontalAlignment(Element.ALIGN_LEFT);
            left.setFixedHeight(cellHeight);
            left.setBorder(0);
            table.addCell(left);
			// HARSHAD -   Date Cell on Footer modification
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
			Date date  = new Date();
			String footerDate = df.format(date);
			
			PdfPCell leftDown = new PdfPCell(pgh.multiFontPara(footerDate, cellfont));
            leftDown.setHorizontalAlignment(Element.ALIGN_LEFT);
            leftDown.setFixedHeight(cellHeight);
            leftDown.setBorder(0);
            table2.addCell(leftDown);

            PdfPCell center = new PdfPCell(pgh.multiFontPara(headerTextLeft, cellfont));
            center.setHorizontalAlignment(Element.ALIGN_CENTER);
            center.setFixedHeight(cellHeight);
            center.setBorder(0);
            table.addCell(center);
			
			PdfPCell centerDown = new PdfPCell(pgh.multiFontPara(" "));
			centerDown.setFixedHeight(cellHeight);
            centerDown.setBorder(0);
            table2.addCell(centerDown);
			
			PdfPCell rightDown = new PdfPCell(pgh.multiFontPara(" "));
			rightDown.setFixedHeight(cellHeight);
            rightDown.setBorder(0);
            table2.addCell(rightDown);
			
			
			
		
            PdfPCell right = new PdfPCell(pgh.multiFontPara(headerTextRight, cellfont));
            right.setHorizontalAlignment(Element.ALIGN_LEFT);
            right.setFixedHeight(cellHeight);
            right.setBorder(0);
          //  table.addCell(right);
			
			PdfPCell rightestTop = new PdfPCell(pgh.multiFontPara(headerTextRight, cellfont));
            rightestTop.setHorizontalAlignment(Element.ALIGN_LEFT);
            rightestTop.setFixedHeight(cellHeight);
            rightestTop.setBorder(0);
            table.addCell(rightestTop);

            Object[] objB = { Integer.toString(writer.getPageNumber())};
            String text = WTMessage.getLocalizedMessage(RB.MAIN, "pageOf_LBL", objB);
            Font pageOfFont = pgh.getCellFont(pageNumFontClass, null, "8");
            font = pageOfFont.getCalculatedBaseFont(false);

            PdfPCell pageOf = new PdfPCell(pgh.multiFontPara(text, pageOfFont));
            pageOf.setPaddingRight(font.getWidthPoint("0000", 8));
            pageOf.setHorizontalAlignment(Element.ALIGN_RIGHT);
            pageOf.setBorder(0);
            table2.addCell(pageOf);

            table.writeSelectedRows(0, -1, document.left(), document.getPageSize().getHeight(), cb2);
			
			
			table2.writeSelectedRows(0, -1, document.left(), 20, cb);
            float textBase = document.getPageSize().getHeight() - 9;

            // for odd pagenumbers, show the footer at the left

            float adjust = font.getWidthPoint("000", 8);
          //  cb.addTemplate(tpl, document.right() - adjust, textBase);
			 cb.addTemplate(tpl, document.right() - adjust, 11);
            cb.saveState();

            cb.restoreState();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * goes back and fills in the page numbers for the title bar on each page
     * 
     * @see com.lowagie.text.pdf.PdfPageEventHelper#onCloseDocument(com.lowagie.text.pdf.PdfWriter,
     *      com.lowagie.text.Document)
     * @param writer
     * @param document
     */

    public void onCloseDocument(PdfWriter writer, Document document) {
        tpl.setColorFill(pgh.getColor(pageNumFontClass));
        tpl.setColorStroke(pgh.getColor(pageNumFontClass));
        tpl.beginText();
        tpl.setFontAndSize(font, 8);
        tpl.setTextMatrix(0, -1);
        tpl.showText(""+ (writer.getPageNumber() - 1));
        tpl.endText();
    }

}
