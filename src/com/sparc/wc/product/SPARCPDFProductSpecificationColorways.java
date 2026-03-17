/*
 * PDFProductSpecificationHeader.java
 *
 * Created on August 22, 2005, 11:00 AM
 */

package com.sparc.wc.product;


import wt.part.*;
import com.lcs.wc.part.*;
import com.lcs.wc.product.*;
import com.lcs.wc.season.*;
import com.lcs.wc.foundation.*;
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
public class SPARCPDFProductSpecificationColorways extends PdfPTable {

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

    static float fixedHeight = 300.0f;

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
    public SPARCPDFProductSpecificationColorways() {
    }

    /**
     * Constructor that specifies how many columns to have in the header
     *
     * Call the super(int) which initializes the header as a PdfPTable
     * 
     * @param cols
     *            number columns will be in the header
     */
    public SPARCPDFProductSpecificationColorways(int cols) {
        super(cols);
    }

    /**
     * returns another instance of PDFProductSpecificationHeader with the table filled, which can be added to a Document
     * 
     * @param params
     * @throws WTException
     * @return
     */
    public Element getColorwaysPDFTable(Map params) throws WTException {
        try {
			
			
			//PdfPCell tableCell = new PdfPCell();
            PdfPTable ppsh = new PdfPTable(4);
            ppsh.setWidthPercentage(95.0f);
            float[] widths = { 25.0f, 25.0f, 25.0f,25.0f };
            ppsh.setWidths(widths);
			LCSProduct product = (LCSProduct) LCSProductQuery.findObjectById((String) params.get(PRODUCT_ID));
		 	 
			ppsh =getColorwaysTableHeader(ppsh);
			Map colorwaysDataMap = getColorwaysTableData(params);
			ppsh = drawColorwaysDataTable(ppsh,colorwaysDataMap);
			 ppsh = drawEmptyRow(ppsh);
			
            return ppsh;
        } catch (Exception e) {
            throw new WTException(e);
        }
    }
    // changes for RM Task 10480 - start
    public Element getReferenceCommentsPDFTable(Map params) throws WTException {
        try {
			
			
             PdfPTable ppsh = new PdfPTable(1);
            ppsh.setWidthPercentage(95.0f);
            float[] widths = { 90.0f };
            ppsh.setWidths(widths);
			LCSProduct product = (LCSProduct) LCSProductQuery.findObjectById((String) params.get(PRODUCT_ID));
			String referenceComments = (String)product.getValue("scReferenceStyleComments");
			System.out.println("**referenceComments:"+referenceComments);
			ppsh = getReferenceComments(ppsh,referenceComments);
			//Map colorwaysDataMap = getColorwaysTableData(params);
			//ppsh = drawColorwaysDataTable(ppsh,colorwaysDataMap);
			 ppsh = drawEmptyRow(ppsh);
			
            return ppsh;
        } catch (Exception e) {
            throw new WTException(e);
        }
    }
    // changes for RM Task 10480 - end
    
	
	public PdfPTable drawEmptyRow(PdfPTable ppsh) throws WTException {
		  PdfPCell bottomCell1 = new PdfPCell(pgh.multiFontPara(" "));
		  bottomCell1.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
           bottomCell1.setBorderColor(pgh.getColor("HEXFFFFFF"));
		   ppsh.addCell(bottomCell1);
		  PdfPCell bottomCell2 = new PdfPCell(pgh.multiFontPara(" "));
		   bottomCell2.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
           bottomCell2.setBorderColor(pgh.getColor("HEXFFFFFF"));
		   ppsh.addCell(bottomCell2);
		  PdfPCell bottomCell3 = new PdfPCell(pgh.multiFontPara(" "));
		   bottomCell3.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
           bottomCell3.setBorderColor(pgh.getColor("HEXFFFFFF"));
		   ppsh.addCell(bottomCell3);
		  PdfPCell bottomCell4 = new PdfPCell(pgh.multiFontPara(" "));
		   bottomCell4.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
           bottomCell4.setBorderColor(pgh.getColor("HEXFFFFFF"));
		   ppsh.addCell(bottomCell4);
		 
		 return ppsh;
	}
	
	 public PdfPTable getColorwaysTableHeader(PdfPTable ppsh) throws WTException {
		 
		    	
            PdfPCell marginCellLeftTop = new PdfPCell(pgh.multiFontPara("Colorway"));
			marginCellLeftTop.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
          //  marginCellLeftTop.setBorderColor(pgh.getColor("HEXFFFFFF"));
			ppsh.addCell(marginCellLeftTop);
			
			
			PdfPCell marginCellLeftMidTop = new PdfPCell(pgh.multiFontPara("Colorway #"));
			  marginCellLeftMidTop.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
           // marginCellMidTop.setBorderColor(pgh.getColor("HEXFFFFFF"));
			 ppsh.addCell(marginCellLeftMidTop);
			 
			 
			PdfPCell marginCellRightMidTop = new PdfPCell(pgh.multiFontPara("PLM Colorway Status"));
			  marginCellRightMidTop.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
            //marginCellRightTop.setBorderColor(pgh.getColor("HEXFFFFFF"));
			ppsh.addCell(marginCellRightMidTop);
			
			 PdfPCell marginCellRightTop = new PdfPCell(pgh.multiFontPara("Colorway Carry Over Y/N"));
			  marginCellRightTop.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
            //marginCellRightTop.setBorderColor(pgh.getColor("HEXFFFFFF"));
			ppsh.addCell(marginCellRightTop);
		
		 
		 return ppsh;
		 
	 }
	 // changes for RM Task 10480 - start
	 public PdfPTable getReferenceComments(PdfPTable ppsh, String referenceComments) throws WTException {
		 
         	PdfPCell marginCellLeftTop = new PdfPCell(pgh.multiFontPara("Reference Comments"));
			marginCellLeftTop.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
			ppsh.addCell(marginCellLeftTop);
			
			PdfPCell marginCellLeftTop1 = new PdfPCell(pgh.multiFontPara(referenceComments));
			marginCellLeftTop1.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));	
			ppsh.addCell(marginCellLeftTop1);
		 return ppsh;
		 
	 }
	 
	 
 public Map getColorwaysTableData(Map params) throws WTException {
		
		Map allRows = new HashMap();
		Collection colorwayMap = (Collection)params.get("COLORWAYS");
		String seasonMasterId = (String)params.get("SEASONMASTER_ID");
		boolean isSeasonChosen = false;
		LCSSeason chosenSeason = null;
		if(FormatHelper.hasContent(seasonMasterId)){
			Object genericSeasonObject = LCSQuery.findObjectById(seasonMasterId);
			if(genericSeasonObject!=null){
				if(genericSeasonObject instanceof LCSSeasonMaster){
					LCSSeason season = (LCSSeason)VersionHelper.latestIterationOf((LCSSeasonMaster)genericSeasonObject);
					if(season!= null){
						chosenSeason = season;
						isSeasonChosen = true;
					}
				}
			}
			
		}
		
		
			if(colorwayMap!=null){
				if(!colorwayMap.isEmpty() && colorwayMap.size()>0){
					Iterator colorwayIter = colorwayMap.iterator();
					while(colorwayIter.hasNext()){
						Map rowData  = new HashMap ();
						rowData.put("1","	");
						rowData.put("2","	");
						rowData.put("3","	");
						rowData.put("4","	");
						String colorwayId = (String)colorwayIter.next();
						
						
						if(FormatHelper.hasContent(colorwayId)){
							
							//VR:com.lcs.wc.product.LCSSKU:
							/*
							colorwayId = "VR:com.lcs.wc.product.LCSSKU:"+colorwayId;
							LCSSKU sku = null;
							 sku =(LCSSKU) LCSSKUQuery.findObjectById(colorwayId,false);
							*/
							LCSSKU sku = null;
							String partMasterId  = "OR:com.lcs.wc.part.LCSPartMaster:"+colorwayId;
							LCSPartMaster skuMaster = (LCSPartMaster)LCSQuery.findObjectById(partMasterId);
							 if(skuMaster!=null){
								sku = (LCSSKU)VersionHelper.latestIterationOf(skuMaster); 
							  LCSSKU skuArev = null;
							   skuArev = SeasonProductLocator.getSKUARev(sku);
							   rowData.put("1",(String)skuArev.getValue("skuName"));
							   rowData.put("2",String.valueOf(skuArev.getValue("scColorwayNo")));
							   if(isSeasonChosen){
								  LCSSeasonProductLink skuSeasonLink =(LCSSeasonProductLink) LCSSeasonQuery.findSeasonProductLink(sku,chosenSeason);
								   if(skuSeasonLink.getOwner()instanceof LCSSKU){
									String  colorwayStatus = (String)skuSeasonLink.getValue("scPLMPolorwayStatus");
									colorwayStatus=SPARCPDFProductSpecificationHeader1.getAttListValue(skuSeasonLink.getFlexType().getAttribute("scPLMPolorwayStatus"), skuSeasonLink);
									
									rowData.put("3",colorwayStatus);
									String skuCarryOverFlag=(String)skuSeasonLink.getValue("scPLMColorwayCarryOver");
									skuCarryOverFlag=SPARCPDFProductSpecificationHeader1.getAttListValue(skuSeasonLink.getFlexType().getAttribute("scPLMColorwayCarryOver"), skuSeasonLink);
									
									if(skuCarryOverFlag!=null&&FormatHelper.hasContent(skuCarryOverFlag)){
										if("yes".equalsIgnoreCase(skuCarryOverFlag)) {
									rowData.put("4","Yes");	
										}
										else{
											rowData.put("4","No");	
											}
									}else{
									rowData.put("4","No");	
									}
									
								   }
							   }
							   
							   
							 }
						}
					
						allRows.put(colorwayId,rowData);

					
					}
					
				}
			}
        
	
	
 return allRows;
 }

public PdfPTable drawColorwaysDataTable(PdfPTable ppsh,Map tableData) throws WTException {

if(tableData!=null){
	if(!tableData.isEmpty()&& tableData.size()>0){
		Iterator rowIter = tableData.keySet().iterator();
		while(rowIter.hasNext()){
			Map rowDataMap = (Map)tableData.get((String)rowIter.next());
			if(rowDataMap!=null){
				if(!rowDataMap.isEmpty()&& rowDataMap.size()>0){
					Iterator cellNoIter = rowDataMap.keySet().iterator();
					while(cellNoIter.hasNext()){
						String cellNoString  =(String)cellNoIter.next();
						String cellData = (String)rowDataMap.get(cellNoString);
						
						PdfPCell marginCellLeftTop = new PdfPCell(pgh.multiFontPara(cellData));
						marginCellLeftTop.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));	
						ppsh.addCell(marginCellLeftTop);		
							
          //  marginCellLeftTop.setBorderColor(pgh.getColor("HEXFFFFFF"));
			
					}
			
			}
			}
		}
		
		
	}
	
	
	
}


return ppsh;
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
			
			
			if("LEFT".equals(imageAllignString)){
             img = Image.getInstance(imageFileLeft);
			}
			else if("RIGHT".equals(imageAllignString)){
				 
             img = Image.getInstance(imageFileRight);
			String wrongURL = productObj.getPartPrimaryImageURL();
			 if(productObj!=null && FormatHelper.hasContent(wrongURL)){
				 
				 
				 wrongURL =wrongURL.split("Windchill")[1];
				wrongURL = wthome+File.separator+"codebase"+wrongURL;
				 
				
				
				
				//  
				 
				img = Image.getInstance(wrongURL);
			}
			}
            PdfPCell cell = new PdfPCell(new Paragraph());
            cell.setUseBorderPadding(true);
            cell.setPadding(2.0f);

            cell.setBackgroundColor(PDFGeneratorHelper.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));

            cell.setBorderColor(PDFGeneratorHelper.getColor("HEX669999"));
            cell.setBorderWidthRight(0.0f);
			if("LEFT".equals(imageAllignString)){
            cell.setBorderColorRight(PDFGeneratorHelper.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
			cell.setBorderWidthRight(0.0f);
			}
			if("RIGHT".equals(imageAllignString)){
            cell.setBorderColorLeft(PDFGeneratorHelper.getCellBGColor("BORDERED_BLOCK", "HEXC8C9C7"));
			cell.setBorderWidthLeft(0.0f);
			}
            cell.setFixedHeight(fixedHeight);
            ImageCellEvent event = new ImageCellEvent(img, 0f, 4.5f);
            event.setAlignLeft(true);
            cell.setCellEvent(event);
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
