/*
 * PDFProductSpecificationHeader.java
 *
 * Created on August 22, 2005, 11:00 AM
 */

package com.sparc.wc.product;

import com.lcs.wc.product.*;
import com.lcs.wc.util.*;
import com.lcs.wc.document.ImageCellEvent;
import com.lcs.wc.document.*;
import com.lcs.wc.specification.*;
import com.lcs.wc.flextype.*;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.db.*;
import com.lcs.wc.client.web.*;
import com.lcs.wc.client.web.pdf.*;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.util.*;
import java.io.*;

import wt.util.*;

/**
 * Generates the Header for a ProductSpecification
 *
 * NOTE: This class is what is used for the OOB Header. In order use a different Header for the Product Specification a
 * new class needs to be created that implements com.lcs.wc.client.web.pdf.PDFHeader
 *
 * NOTE2: This implementation is an extension of PdfPTable, the getPDFHeader will return another instance of
 * PDFProductSpecificationHeader with the table filled, which can be added to a Document
 * 
 * @author Chuck
 */
public class SPARCPDFProductSpecificationProductThumbnail extends PdfPTable implements PDFHeader {

    // <img src="/LCSW/images/FLEXPDM_small.jpg">
    public static String PRODUCT_ID = "PRODUCT_ID";

    public static String SPEC_ID = "SPEC_ID";

    public static String SEASONMASTER_ID = "SEASONMASTER_ID";

    public static final String webHomeLocation = LCSProperties.get("flexPLM.webHome.location");

    static String wthome = "";

    static String codebase = "";

    static String imageFile = "";
	static String imageFileLeft = "";
	static String imageFileRight = "";

    static PDFGeneratorHelper pgh = new PDFGeneratorHelper();

    static float fixedHeight = 165.0f;

    static {
        try {
            imageFile = LCSProperties.get("com.lcs.wc.product.PDFProductSpecificationHeader.headerImage",
                    FormatHelper.formatOSFolderLocation(webHomeLocation) + "/images/FlexPLMLogo.png");
			imageFileLeft = LCSProperties.get("com.lcs.wc.product.Reebok.SPARCPDFProductSpecificationHeader.headerImageLeft");
                  
			imageFileRight = LCSProperties.get("com.lcs.wc.product.PDFProductSpecificationHeader.headerImage",
                    FormatHelper.formatOSFolderLocation(webHomeLocation) + "/images/FlexPLMLogo.png");
            wthome = WTProperties.getServerProperties().getProperty("wt.home");
            imageFile = wthome + File.separator + imageFile;
			imageFileLeft =  wthome + File.separator + imageFileLeft;
			imageFileRight =  wthome + File.separator + imageFileRight;
        } catch (Exception e) {
            System.out.println("Error initializing cache for ExcelGeneratorHelper");
            e.printStackTrace();
        }
    }

    /**
     * Creates a new instance of PDFProductSpecificationHeader
     */
    public SPARCPDFProductSpecificationProductThumbnail() {
    }

    /**
     * Constructor that specifies how many columns to have in the header
     *
     * Call the super(int) which initializes the header as a PdfPTable
     * 
     * @param cols
     *            number columns will be in the header
     */
    public SPARCPDFProductSpecificationProductThumbnail(int cols) {
        super(cols);
    }
	
	public Element getPDFHeader(Map params) throws WTException {
		return (Element)getThumbnailPageContentCell(params);
	}
	

    /**
     * returns another instance of PDFProductSpecificationHeader with the table filled, which can be added to a Document
     * 
     * @param params
     * @throws WTException
     * @return
     */
    public Element getThumbnailPageContentCell(Map params) throws WTException {
        try {
			
			
			
			
            SPARCPDFProductSpecificationProductThumbnail ppsh = new SPARCPDFProductSpecificationProductThumbnail(1);
            ppsh.setWidthPercentage(95.0f);
           // float[] widths = { 3.0f, 94.0f,3.0f };
            //ppsh.setWidths(widths);
			LCSProduct product = (LCSProduct) LCSProductQuery.findObjectById((String) params.get(PRODUCT_ID));
			
            /*PdfPCell marginCellLeftTop = new PdfPCell(pgh.multiFontPara(" "));
			marginCellLeftTop.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
            marginCellLeftTop.setBorderColor(pgh.getColor("HEXFFFFFF"));
			ppsh.addCell(marginCellLeftTop);
			
			
			PdfPCell marginCellMidTop = new PdfPCell(pgh.multiFontPara(" "));
			  marginCellMidTop.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
            marginCellMidTop.setBorderColor(pgh.getColor("HEXFFFFFF"));
			 ppsh.addCell(marginCellMidTop);
			 
			PdfPCell marginCellRightTop = new PdfPCell(pgh.multiFontPara(" "));
			  marginCellRightTop.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
            marginCellRightTop.setBorderColor(pgh.getColor("HEXFFFFFF"));
			ppsh.addCell(marginCellRightTop);
			
			 
			 
			PdfPCell marginCellLeftCenter = new PdfPCell(pgh.multiFontPara(" "));
			  marginCellLeftCenter.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
            marginCellLeftCenter.setBorderColor(pgh.getColor("HEXFFFFFF"));
			ppsh.addCell(marginCellLeftCenter);*/
			
			PdfPCell marginCellImageCenter = createImageCell("RIGHT",product);
			//PdfPCell marginCellImageCenter = new PdfPCell(pgh.multiFontPara((String)params.toString()));
			marginCellImageCenter.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
            marginCellImageCenter.setBorderColor(pgh.getColor("HEXFFFFFF"));			
			ppsh.addCell(marginCellImageCenter);
			
			/*PdfPCell marginCellRightCenter = new PdfPCell(pgh.multiFontPara(" "));
			  marginCellRightCenter.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
            marginCellRightCenter.setBorderColor(pgh.getColor("HEXFFFFFF"));
			 ppsh.addCell(marginCellRightCenter);
			 
			
			 PdfPCell marginCellLeftBottom = new PdfPCell(pgh.multiFontPara(" "));
			   marginCellLeftBottom.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
            marginCellLeftBottom.setBorderColor(pgh.getColor("HEXFFFFFF"));		
			ppsh.addCell(marginCellLeftBottom);
			
			
			
			PdfPCell marginCellMidBottom = new PdfPCell(pgh.multiFontPara(" "));
			marginCellMidBottom.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
            marginCellMidBottom.setBorderColor(pgh.getColor("HEXFFFFFF"));
			ppsh.addCell(marginCellMidBottom);
			
			
			PdfPCell marginCellRightBottom = new PdfPCell(pgh.multiFontPara(" "));
			 marginCellRightBottom.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
            marginCellRightBottom.setBorderColor(pgh.getColor("HEXFFFFFF"));
			 ppsh.addCell(marginCellRightBottom);*/
			 
			
           // PdfPCell tableDataLeft = createDataCell(params,"LEFT");
			//PdfPCell spacerCell = createSpacerCell();
			 //PdfPCell imageCellRight = createImageCell("RIGHT",product);
           // PdfPCell tableDataRight = createDataCell(params,"RIGHT");


            //ppsh.addCell(imageCellLeft);
            //ppsh.addCell(tableDataLeft);
			//ppsh.addCell(spacerCell);
			//ppsh.addCell(tableDataRight);
			//ppsh.addCell(imageCellRight);

            return ppsh;
        } catch (Exception e) {
            throw new WTException(e);
        }
    }

    /**
     * gets the Height for this header
     * 
     * @return
     */
    public float getHeight() {
        return fixedHeight;
    }

    private PdfPCell createImageCell(String imageAllignString, LCSProduct productObj) throws WTException {
        try {
			Image img = null;
			
			float hpadding = 0;
			float vpadding = 0;
			if("LEFT".equals(imageAllignString)){
             img = Image.getInstance(imageFileLeft);
			}
			else if("RIGHT".equals(imageAllignString)){
				 
				
            // img = Image.getInstance(imageFileRight);
			String wrongURL = productObj.getPartPrimaryImageURL();
			 if(productObj!=null && FormatHelper.hasContent(wrongURL)){
				 
				 
				 wrongURL =wrongURL.split("Windchill")[1];
				wrongURL = wthome+File.separator+"codebase"+wrongURL;

				
				img= Image.getInstance(wrongURL);
				float imgHeight = img.getHeight();
				float imgWidth = img.getWidth();
						
				if(imgWidth<1000){
					img.scaleToFit(300.0f, 300.0f);

				}
				if(imgHeight<1000){
					img.scaleToFit(300.0f, 300.0f);
				}

					//img.scaleAbsolute(imgWidth,imgHeight);
					
			//hpadding  =(546 - imgWidth);
			//vpadding = (380 - imgHeight);
			
			}
			}
			// Fix for INC0164446 - Image reverse issue
            PdfPCell cell = new PdfPCell(new Phrase(""));
              if(img != null){
                 cell = new PdfPCell(img);
            }
			  cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			  cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            return cell;
        } catch (Exception e) {
            throw new WTException(e);
        }
    }
	
	
	
	
	private PdfPCell createSpacerCell() throws WTException {
		
		 PdfPTable table = new PdfPTable(1);
		 PdfPCell cell = new PdfPCell(pgh.multiFontPara("        "));
            cell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
           // cell.setBorderColor(pgh.getCellBGColor("BORDERED_BLOCK", null));
            cell.setBorderWidth(0.0f);
			 cell.setBorderColorLeft(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
			  cell.setBorderColorLeft(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
			table.addCell(cell);
			table.addCell(cell);
			table.addCell(cell);
			table.addCell(cell);
			table.addCell(cell);
			
			 PdfPCell spacerTableCell = new PdfPCell(table);
           // spacerTableCell.setPadding(4.0f);
            spacerTableCell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            spacerTableCell.setBorderColor(pgh.getColor("HEX669999"));
          //  spacerTableCell.setBorderWidthLeft(0.0f);
          spacerTableCell.setBorderColorLeft(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
		   spacerTableCell.setBorderColorRight(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            spacerTableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            spacerTableCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            spacerTableCell.setFixedHeight(fixedHeight);
			
			
		
		return spacerTableCell;
	}
	
    private PdfPCell createDataCell(Map params,String cellAllignment ) throws WTException {
        try {
            if (!FormatHelper.hasContent((String) params.get(PRODUCT_ID))) {
                throw new WTException("Can not create PDFProductSpecificationHeader without PRODUCT_ID");
            }

            LCSProduct product = (LCSProduct) LCSProductQuery.findObjectById((String) params.get(PRODUCT_ID));
            String pNameAtt = product.getFlexType().getAttribute("productName").getAttDisplay() + ":";
            String pName = (String) product.getValue("productName");

            String specId = (String) params.get(SPEC_ID);
            String sNameAtt = "";
            String sName = "";
            if (FormatHelper.hasContent(specId)) {
                FlexSpecification spec = (FlexSpecification) LCSProductQuery.findObjectById(specId);
                sNameAtt = spec.getFlexType().getAttribute("specName").getAttDisplay() + ":";
                sName = (String) spec.getValue("specName");
            }
			if("LEFT".equals(cellAllignment)){
				pNameAtt = "LHS_ATT-"+pNameAtt;
				sNameAtt = "LHS_ATT-"+sNameAtt;
			}
			else if("RIGHT".equals(cellAllignment)){
				pNameAtt = "RHS_ATT-"+pNameAtt;
				sNameAtt = "RHS_ATT-"+sNameAtt;
			}
            PdfPTable table = new PdfPTable(2);
            PdfPCell cell = new PdfPCell(pgh.multiFontPara(pNameAtt, pgh.getCellFont("FORMLABEL", null, null)));
            cell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            cell.setBorderColor(pgh.getCellBGColor("BORDERED_BLOCK", null));
            cell.setBorderWidth(0.0f);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);

            cell = new PdfPCell(pgh.multiFontPara(pName, pgh.getCellFont("DISPLAYTEXT", null, null)));
            cell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            cell.setBorderColor(pgh.getCellBGColor("BORDERED_BLOCK", null));
            cell.setBorderWidth(0.0f);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
			
			cell = new PdfPCell(pgh.multiFontPara(pNameAtt, pgh.getCellFont("FORMLABEL", null, null)));
			cell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            cell.setBorderColor(pgh.getCellBGColor("BORDERED_BLOCK", null));
            cell.setBorderWidth(0.0f);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);

            cell = new PdfPCell(pgh.multiFontPara(pName, pgh.getCellFont("DISPLAYTEXT", null, null)));
            cell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            cell.setBorderColor(pgh.getCellBGColor("BORDERED_BLOCK", null));
            cell.setBorderWidth(0.0f);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
			
			cell = new PdfPCell(pgh.multiFontPara(pNameAtt, pgh.getCellFont("FORMLABEL", null, null)));
			cell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            cell.setBorderColor(pgh.getCellBGColor("BORDERED_BLOCK", null));
            cell.setBorderWidth(0.0f);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);

            cell = new PdfPCell(pgh.multiFontPara(pName, pgh.getCellFont("DISPLAYTEXT", null, null)));
            cell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            cell.setBorderColor(pgh.getCellBGColor("BORDERED_BLOCK", null));
            cell.setBorderWidth(0.0f);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
			

            cell = new PdfPCell(pgh.multiFontPara(sNameAtt, pgh.getCellFont("FORMLABEL", null, null)));
            cell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            cell.setBorderColor(pgh.getCellBGColor("BORDERED_BLOCK", null));
            cell.setBorderWidth(0.0f);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);

            cell = new PdfPCell(pgh.multiFontPara(sName, pgh.getCellFont("DISPLAYTEXT", null, null)));
            cell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            cell.setBorderColor(pgh.getCellBGColor("BORDERED_BLOCK", null));
            cell.setBorderWidth(0.0f);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
			
			 cell = new PdfPCell(pgh.multiFontPara(sNameAtt, pgh.getCellFont("FORMLABEL", null, null)));
            cell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            cell.setBorderColor(pgh.getCellBGColor("BORDERED_BLOCK", null));
            cell.setBorderWidth(0.0f);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);

            cell = new PdfPCell(pgh.multiFontPara(sName, pgh.getCellFont("DISPLAYTEXT", null, null)));
            cell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            cell.setBorderColor(pgh.getCellBGColor("BORDERED_BLOCK", null));
            cell.setBorderWidth(0.0f);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
			
			 cell = new PdfPCell(pgh.multiFontPara(sNameAtt, pgh.getCellFont("FORMLABEL", null, null)));
            cell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            cell.setBorderColor(pgh.getCellBGColor("BORDERED_BLOCK", null));
            cell.setBorderWidth(0.0f);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);

            cell = new PdfPCell(pgh.multiFontPara(sName, pgh.getCellFont("DISPLAYTEXT", null, null)));
            cell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            cell.setBorderColor(pgh.getCellBGColor("BORDERED_BLOCK", null));
            cell.setBorderWidth(0.0f);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);

            PdfPCell tableCell = new PdfPCell(table);
            tableCell.setPadding(4.0f);
            tableCell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            tableCell.setBorderColor(pgh.getColor("HEX669999"));
            tableCell.setBorderWidthLeft(0.0f);
            tableCell.setBorderColorLeft(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
			tableCell.setBorderColorRight(pgh.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableCell.setFixedHeight(fixedHeight);

            return tableCell;
        } catch (Exception e) {
            throw new WTException(e);
        }
    }
}