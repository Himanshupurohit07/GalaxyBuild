package com.sparc.wc.sample.gen;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lcs.wc.client.web.PDFGeneratorHelper;
import com.lcs.wc.util.FormatHelper;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import wt.util.WTException;
import wt.util.WTProperties;


/**
 * 
 * This class generates a single page PDF Document and will contain the Fit Photo/Corrctions image
 */
public class SPARCSampleFitPhotoCorrectionsPDFContent extends SPARCSamplePDFContentGenerator {
	
	private static final Logger LOGGER = LogManager.getLogger(SPARCSampleFitPhotoCorrectionsPDFContent.class.getName());
    public static String SPEC_ID = "SPEC_ID";
    public static String SEASONMASTER_ID = "SEASONMASTER_ID";
    static PDFGeneratorHelper pgh = new PDFGeneratorHelper();
    static float fixedHeight = 165.0f;
    public static final String PRODUCT_ID = "PRODUCT_ID";
	public static final String HEADER_HEIGHT = "HEADER_HEIGHT";
	public float pageHeight = 0;
	private static final String BORDERED_BLOCK = "BORDERED_BLOCK";
	private static final String FORMLABEL = "FORMLABEL";

    public SPARCSampleFitPhotoCorrectionsPDFContent() {
    }

    /**
     * @param imageURL
     * @return
     * @throws WTException
     */
    private PdfPCell createImageCell(String imageURL) throws WTException {
        try {
			Image img = null;
			String wthome = "";
			 if(imageURL!=null && FormatHelper.hasContent(imageURL)){
				 
				 wthome = WTProperties.getServerProperties().getProperty("wt.home");
				 imageURL =imageURL.split("Windchill")[1];
				 imageURL = wthome+File.separator+"codebase"+imageURL;
				img= Image.getInstance(imageURL);
				float imgHeight = img.getHeight();
				float imgWidth = img.getWidth();
				LOGGER.debug("imgHeight:"+imgHeight);
				LOGGER.debug("imgWidth:"+imgWidth);
				
					  img.scaleToFit(400.0f, 400.0f);
					  img.scaleToFit(400.0f, 400.0f);
				
			 }
            PdfPCell cell = new PdfPCell(new Phrase(" "));
              if(img != null){
                 cell = new PdfPCell(img);
            }
			  cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			  cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			  cell.setPadding(40.0f);
            return cell;
        } catch (Exception e) {
            throw new WTException(e);
        }
    }

    /**
     * This generates an image Cell
     * @param img
     * @return
     * @throws WTException
     */
    private PdfPCell createImageCell(Image img) throws WTException {
        try {
        	LOGGER.debug("*createImageCell:"+img);
            PdfPCell cell = new PdfPCell(new Phrase(" "));
              if(img != null){
            	  float imgHeight = img.getHeight();
   				 float imgWidth = img.getWidth();
   				  LOGGER.debug("imgHeight:"+imgHeight+":::"+"imgWidth:"+imgWidth);
   				  img.scaleToFit(500.0f, 400.0f);
                 cell = new PdfPCell(img);
                
            }
			  cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			  cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			  cell.setPadding(5.0f);
            return cell;
        } catch (Exception e) {
            throw new WTException(e);
        }
    }
	/**
	 * This method generates a Table that contains the Fit Photo/Corrections image
	 * @param imageURL
	 * @param docName
	 * @param img
	 * @param contentCellHeightOtherComponents
	 * @return
	 * @throws WTException
	 */
	public Collection<PdfPTable> getFitPhotoCorrectionImageContentCell(String imageURL, String docName, Image img,float contentCellHeightOtherComponents) throws WTException {
		LOGGER.debug("**SPARCSampleFitPhotoCorrectionsPDFContent start");
		try {


			Collection<PdfPTable> content = new ArrayList();
			PdfPTable fullTable = new PdfPTable(1);
			fullTable.setSplitLate(false);
			PdfPCell cell = null;
			
			cell = new PdfPCell(SPARCSampleDetailsPDFContent.pgh.multiFontPara(docName, SPARCSampleDetailsPDFContent.pgh
					.getCellFont(FORMLABEL, null, null)));
			cell.setColspan(0);
			cell.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			cell.setBorderWidth(1.0F);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_CENTER);
			fullTable.setWidthPercentage(100);
			fullTable.addCell(cell);
			
			PdfPTable imageTable = new PdfPTable(1);
			PdfPCell imageCell = createImageCell(img);
			LOGGER.debug("imageCell height:"+imageCell.getHeight());
			imageCell.setColspan(1);
			imageCell.setBorder(Rectangle.ALIGN_JUSTIFIED);
			imageCell.setFixedHeight(contentCellHeightOtherComponents);
			LOGGER.debug("imageCell height11:"+imageCell.getHeight());
			imageTable.addCell(imageCell);
			PdfPCell imageTableCell = new PdfPCell(imageTable);
			imageTableCell.setFixedHeight(contentCellHeightOtherComponents);
			LOGGER.debug("imageCell height44:"+imageTableCell.getHeight());
			fullTable.addCell(imageTableCell);
			content.add(fullTable);
			LOGGER.debug("**SPARCSampleFitPhotoCorrectionsPDFContent - end:"+content.size());
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WTException(e);
		}
		
	}

	 /**
	 * @param params
	 * @param doc
	 * @param pageType
	 * @return
	 * @throws WTException
	 */
	private float calcSPARCContentCellHeight(Map params, Document doc, String pageType) throws WTException {
	        float height = doc.top() - doc.bottom();

	        if (params.get(HEADER_HEIGHT) != null) {
				
	            Object hh = params.get("HEADER_HEIGHT");
				if("THUMBNAIL".equals(pageType)){
					hh = params.get("HEADER_HEIGHT1");
				}else if("COLORWAYS".equals(pageType)){
					hh = params.get("HEADER_HEIGHT2");
				}
	            if (hh instanceof Float) {
	                height = height - ((Float) hh).floatValue() -10;
	            }
	            if (hh instanceof String) {
	                height = height - (new Float((String) hh)).floatValue() -10;
	            }
	        }
	        LOGGER.debug("**returning height:"+height);
	        return height;
	    }
	@Override
	public Collection getPDFContentCollection(Map params, Document document) throws WTException {
		return null;
	}
	
	
	
	
}