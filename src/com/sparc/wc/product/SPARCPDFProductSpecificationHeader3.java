/*
 * PDFProductSpecificationHeader.java
 *
 * Created on August 22, 2005, 11:00 AM
 */

package com.sparc.wc.product;

import com.lcs.wc.product.*;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.util.*;
import com.lcs.wc.document.ImageCellEvent;
import com.lcs.wc.document.*;
import com.lcs.wc.specification.*;
import com.lcs.wc.flextype.*;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.measurements.LCSMeasurements;
import com.lcs.wc.db.*;
import com.lcs.wc.client.web.*;
import com.lcs.wc.client.web.pdf.*;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.sparc.wc.util.SparcConstants;
import com.sparc.wc.util.SparcUtil;

import java.util.*;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

import wt.log4j.LogR;
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
public class SPARCPDFProductSpecificationHeader3 extends PdfPTable implements PDFHeader {
	
	 private static final Logger LOGGER = LogManager.getLogger(SPARCPDFProductSpecificationHeader3.class.getName());


    // <img src="/LCSW/images/FLEXPDM_small.jpg">
    public static String PRODUCT_ID = "PRODUCT_ID";

    public static String SPEC_ID = "SPEC_ID";

    public static String SEASONMASTER_ID = "SEASONMASTER_ID";

    public static final String webHomeLocation = LCSProperties.get("flexPLM.webHome.location");

    static String wthome = "";

    static String codebase = "";

    static String imageFile = "";
	//static String imageFileLeft = "";
	static String imageFileRight = "";
	//static String luckyImageFileLeft = "";

    static PDFGeneratorHelper pgh = new PDFGeneratorHelper();

    static float fixedHeight = 130.0f;
    
    private static final String LUCKY_LEFT_HEADER3_KEYNAMES = LCSProperties.get("com.sparc.lucky.leftHeader3KeyDisplayNames");
	private static final String LUCKY_RIGHT_HEADER3_KEYNAMES = LCSProperties.get("com.sparc.lucky.rightHeader3KeyDisplayNames");
	//private static String f21ImageLeft = "";

	private static final String F21_LEFT_HEADER3_KEYNAMES = LCSProperties.get("com.sparc.f21.leftHeader3KeyDisplayNames");
	private static final String F21_RIGHT_HEADER3_KEYNAMES = LCSProperties.get("com.sparc.f21.rightHeader3KeyDisplayNames");
	
	private static final String NAUTICA_LEFT_HEADER3_KEYNAMES = LCSProperties.get("com.sparc.nautica.leftHeader3KeyDisplayNames");
	private static final String NAUTICA_RIGHT_HEADER3_KEYNAMES = LCSProperties.get("com.sparc.nautica.rightHeader3KeyDisplayNames");
	
	
	private static final String AEROPOSTALE_LEFT_HEADER3_KEYNAMES = LCSProperties.get("com.sparc.aeropostale.leftHeader3KeyDisplayNames");
	private static final String AEROPOSTALE_RIGHT_HEADER3_KEYNAMES = LCSProperties.get("com.sparc.aeropostale.rightHeader3KeyDisplayNames");
	
    static {
        try {
            imageFile = LCSProperties.get("com.lcs.wc.product.PDFProductSpecificationHeader.headerImage",
                    FormatHelper.formatOSFolderLocation(webHomeLocation) + "/images/FlexPLMLogo.png");
			//imageFileLeft = LCSProperties.get("com.lcs.wc.product.Reebok.SPARCPDFProductSpecificationHeader.headerImageLeft");
                  
			imageFileRight = LCSProperties.get("com.lcs.wc.product.PDFProductSpecificationHeader.headerImage",
                    FormatHelper.formatOSFolderLocation(webHomeLocation) + "/images/FlexPLMLogo.png");
            wthome = WTProperties.getServerProperties().getProperty("wt.home");
            imageFile = wthome + File.separator + imageFile;
           // luckyImageFileLeft = wthome + File.separator + SparcConstants.LUCKY_LOGO;

			//f21ImageLeft = wthome + File.separator + SparcConstants.F21_LOGO;
			//imageFileLeft =  wthome + File.separator + imageFileLeft;
			imageFileRight =  wthome + File.separator + imageFileRight;
        } catch (Exception e) {
            System.out.println("Error initializing cache for ExcelGeneratorHelper");
            e.printStackTrace();
        }
    }

    /**
     * Creates a new instance of PDFProductSpecificationHeader
     */
    public SPARCPDFProductSpecificationHeader3() {
    }

    /**
     * Constructor that specifies how many columns to have in the header
     *
     * Call the super(int) which initializes the header as a PdfPTable
     * 
     * @param cols
     *            number columns will be in the header
     */
    public SPARCPDFProductSpecificationHeader3(int cols) {
        super(cols);
    }

    /**
     * returns another instance of PDFProductSpecificationHeader with the table filled, which can be added to a Document
     * 
     * @param params
     * @throws WTException
     * @return
     */
    public Element getPDFHeader(Map params)  {
		  SPARCPDFProductSpecificationHeader3 ppsh = new SPARCPDFProductSpecificationHeader3(4);
        try {
			
        	PdfPCell tableDataLeft = null;
			PdfPCell imageCellRight = null;
			PdfPCell tableDataRight = null;
			PdfPCell imageCellLeft = null;
			
          
            ppsh.setWidthPercentage(95.0f);
            float[] widths = { 30.0f, 20.0f, 20.0f,30.0f };
            ppsh.setWidths(widths);
			LCSProduct product = (LCSProduct) LCSProductQuery.findObjectById((String) params.get(PRODUCT_ID));
			String strBrand = SparcUtil.getAttListValue("scBrand",product);
			
			String productFlexType = product.getFlexType().getFullNameDisplay(false);
			
           /* PdfPCell imageCellLeft = createImageCell("LEFT",null);
            PdfPCell tableDataLeft = createDataCell(params,"LEFT");
			//PdfPCell spacerCell = createSpacerCell();
			 PdfPCell imageCellRight = createImageCell("RIGHT",product);
            PdfPCell tableDataRight = createDataCell(params,"RIGHT");*/
			
		/*	if(productFlexType.contains("Lucky")) {
				imageCellLeft = createImageCell(SparcConstants.LEFT,null,"Lucky");
				tableDataLeft = createLuckyDataCell(params,SparcConstants.LEFT,"Lucky");
				imageCellRight = createImageCell(SparcConstants.RIGHT,product,"Lucky");
				tableDataRight = createLuckyDataCell(params,SparcConstants.RIGHT,"Lucky");
			}
			else if (productFlexType.contains("F21")) {
				imageCellLeft = createImageCell(SparcConstants.LEFT,null,"F21");
				tableDataLeft = createLuckyDataCell(params,SparcConstants.LEFT,"F21");
				imageCellRight = createImageCell(SparcConstants.RIGHT,product,"F21");
				tableDataRight = createLuckyDataCell(params,SparcConstants.RIGHT,"F21");
			}
			else {
				imageCellLeft = createImageCell(SparcConstants.LEFT,null,"Reebok");
				tableDataLeft = createDataCell(params,SparcConstants.LEFT);
				//PdfPCell spacerCell = createSpacerCell();
				imageCellRight = createImageCell(SparcConstants.RIGHT,product,"Reebok");
				tableDataRight = createDataCell(params,SparcConstants.RIGHT);
			}*/

			String flexType = "";

			if (productFlexType.contains(SparcConstants.LUCKY_BRAND) || productFlexType.contains(SparcConstants.F21_BRAND)) {
				flexType = productFlexType.contains(SparcConstants.LUCKY_BRAND) ? SparcConstants.LUCKY_BRAND : SparcConstants.F21_BRAND;
			} else if(productFlexType.contains(SparcConstants.AEROPOSTALE_BRAND) || productFlexType.contains(SparcConstants.NAUTICA_BRAND)) {
				flexType = productFlexType.contains(SparcConstants.AEROPOSTALE_BRAND) ? SparcConstants.AEROPOSTALE_BRAND : SparcConstants.NAUTICA_BRAND;
			} else {
				flexType = SparcConstants.REEBOK_BRAND;
			}

			imageCellLeft = createImageCell(SparcConstants.LEFT, null, flexType);
			tableDataLeft = flexType.equalsIgnoreCase("Lucky") || flexType.equalsIgnoreCase("Forever 21") || flexType.equalsIgnoreCase("Aeropostale") || flexType.equalsIgnoreCase("Nautica")? createLuckyDataCell(params, SparcConstants.LEFT, flexType) : createDataCell(params, SparcConstants.LEFT);
			imageCellRight = createImageCell(SparcConstants.RIGHT, product, flexType);
			tableDataRight = flexType.equalsIgnoreCase("Lucky") || flexType.equalsIgnoreCase("Forever 21") || flexType.equalsIgnoreCase("Aeropostale") || flexType.equalsIgnoreCase("Nautica")? createLuckyDataCell(params, SparcConstants.RIGHT, flexType) : createDataCell(params, SparcConstants.RIGHT);



            ppsh.addCell(imageCellLeft);
            ppsh.addCell(tableDataLeft);
			//ppsh.addCell(spacerCell);
			ppsh.addCell(tableDataRight);
			ppsh.addCell(imageCellRight);

            return ppsh;
        } catch (Exception e) {
		
			e.printStackTrace();
           // throw new WTException(e);
        }
		finally{
			  return ppsh;
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

    private PdfPCell createImageCell(String imageAllignString, LCSProduct productObj,String brand) throws WTException {
        try {
			Image img = null;
			String imageFileName="";
			
			if("LEFT".equals(imageAllignString)){
             //img = "Lucky".equalsIgnoreCase(brand)?Image.getInstance(luckyImageFileLeft) :"Forever 21".equalsIgnoreCase(brand)?Image.getInstance(f21ImageLeft) :Image.getInstance(imageFileLeft);
			
             imageFileName = LCSProperties.get("com.lcs.wc.product."+brand+".SPARCPDFProductSpecificationHeader.headerImageLeft","codebase//rfa/sparc//images//Forever21Logo.png");
				LOGGER.debug("Image to be added on the Left:"+wthome + File.separator + imageFileName);
				img = Image.getInstance(wthome + File.separator + imageFileName);
			
			
			}
			else if("RIGHT".equals(imageAllignString)){
				 
            // img = Image.getInstance(imageFileRight);
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

            cell.setBackgroundColor(PDFGeneratorHelper.getCellBGColor("BORDERED_BLOCK", "HEXDDEBF7"));

            cell.setBorderColor(PDFGeneratorHelper.getColor("HEX669999"));
            cell.setBorderWidthRight(0.0f);
			if("LEFT".equals(imageAllignString)){
            cell.setBorderColorRight(PDFGeneratorHelper.getCellBGColor("BORDERED_BLOCK", "HEXDDEBF7"));
			cell.setBorderWidthRight(0.0f);
			}
			if("RIGHT".equals(imageAllignString)){
            cell.setBorderColorLeft(PDFGeneratorHelper.getCellBGColor("BORDERED_BLOCK", "HEXDDEBF7"));
			cell.setBorderWidthLeft(0.0f);
			}
            cell.setFixedHeight(fixedHeight);
			if(img!=null){
            ImageCellEvent event = new ImageCellEvent(img, 4.0f, 4.0f);
			
            
			if("RIGHT".equals(imageAllignString)){
			event.setAlignLeft(false);
			}
			if("LEFT".equals(imageAllignString)){
			event.setAlignLeft(true);
			}
            cell.setCellEvent(event);
			}
            return cell;
        } catch (Exception e) {
            throw new WTException(e);
        }
    }
	
	
	
	
	private PdfPCell createSpacerCell() throws WTException {
		
		 PdfPTable table = new PdfPTable(1);
		 PdfPCell cell = new PdfPCell(pgh.multiFontPara("        "));
            cell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXDDEBF7"));
           // cell.setBorderColor(pgh.getCellBGColor("BORDERED_BLOCK", null));
            cell.setBorderWidth(0.0f);
			 cell.setBorderColorLeft(pgh.getCellBGColor("BORDERED_BLOCK", "HEXDDEBF7"));
			  cell.setBorderColorLeft(pgh.getCellBGColor("BORDERED_BLOCK", "HEXDDEBF7"));
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
            spacerTableCell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXDDEBF7"));
            spacerTableCell.setBorderColor(pgh.getColor("HEX669999"));
          //  spacerTableCell.setBorderWidthLeft(0.0f);
          spacerTableCell.setBorderColorLeft(pgh.getCellBGColor("BORDERED_BLOCK", "HEXDDEBF7"));
		   spacerTableCell.setBorderColorRight(pgh.getCellBGColor("BORDERED_BLOCK", "HEXDDEBF7"));
            spacerTableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            spacerTableCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            spacerTableCell.setFixedHeight(fixedHeight);
			
			
		
		return spacerTableCell;
	}
	
	private PdfPCell createLuckyDataCell(Map params,String cellAllignment,String strType) throws WTException {
		try {
			Map<String,FlexTyped> mapObjects = new HashMap<>();
			validateParams(params);
			mapObjects = createFlexTypedMap(params);
			List<Map.Entry<String, String>> luckyHeaderkeys = new ArrayList<>();
			luckyHeaderkeys = getLuckyHeaderKeys(cellAllignment,strType);
			PdfPTable dataTable = createDataTable();
			for (Map.Entry<String, String> entry : luckyHeaderkeys) 
			 {
				   System.out.println("Header3 Key: " + SparcUtil.getObjectFrom(mapObjects,entry.getKey()) + ",Header3 Value: " + entry.getValue());
		           dataTable.addCell(createCell(entry.getValue()+ SparcConstants.COLON_DELIMITER,"FORMLABEL"));
		           dataTable.addCell(createCell(SparcUtil.getObjectFrom(mapObjects,entry.getKey()),"DISPLAYTEXT"));     
		            
		     }
			 
			 return createPdfPCell(dataTable);

						
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e);
		}	
		
	}
	
	private PdfPCell createPdfPCell(PdfPTable datatable) {
	    PdfPCell datatableCell = new PdfPCell(datatable);
	    datatableCell.setPadding(4.0f);
	    datatableCell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXDDEBF7"));
	    datatableCell.setBorderColor(pgh.getColor("HEX669999"));
	    datatableCell.setBorderWidthLeft(0.0f);
	    datatableCell.setBorderColorLeft(pgh.getCellBGColor("BORDERED_BLOCK", "HEXDDEBF7"));
	    datatableCell.setBorderColorRight(pgh.getCellBGColor("BORDERED_BLOCK", "HEXDDEBF7"));
	    datatableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
	    datatableCell.setVerticalAlignment(Element.ALIGN_TOP);
		datatableCell.setPaddingTop(30f);
	    datatableCell.setFixedHeight(fixedHeight);
	    return datatableCell;
	}
	
	private PdfPTable createDataTable() {
	    PdfPTable datatable = new PdfPTable(2);	    
	    return datatable;
	}
	
	/**
	 * 
	 * @param cellAlignment
	 * @return
	 */
	private List<Map.Entry<String, String>> getLuckyHeaderKeys(String cellAlignment,String strType) {
	    List<Map.Entry<String, String>> luckyHeaderKeys;
	    
	    /*
	    if (SparcConstants.LEFT.equalsIgnoreCase(cellAlignment)) {
	        luckyHeaderKeys = "Lucky".equalsIgnoreCase(strType)?SparcUtil.getListFromBrandContext(LUCKY_LEFT_HEADER3_KEYNAMES):SparcUtil.getListFromBrandContext(F21_LEFT_HEADER3_KEYNAMES);
	    } else {
	        luckyHeaderKeys = "Lucky".equalsIgnoreCase(strType)?SparcUtil.getListFromBrandContext(LUCKY_RIGHT_HEADER3_KEYNAMES):SparcUtil.getListFromBrandContext(F21_RIGHT_HEADER3_KEYNAMES);
	    }
	    */
	    if ("LEFT".equalsIgnoreCase(cellAlignment)) {
			if ("Lucky".equalsIgnoreCase(strType)) {
				luckyHeaderKeys = SparcUtil.getListFromBrandContext(LUCKY_LEFT_HEADER3_KEYNAMES);
			} 
			else if ("Aeropostale".equalsIgnoreCase(strType)) {
				luckyHeaderKeys = SparcUtil.getListFromBrandContext(AEROPOSTALE_LEFT_HEADER3_KEYNAMES);
			}
			else if ("Nautica".equalsIgnoreCase(strType)) {
				luckyHeaderKeys = SparcUtil.getListFromBrandContext(NAUTICA_LEFT_HEADER3_KEYNAMES);
			}else {
				luckyHeaderKeys = SparcUtil.getListFromBrandContext(F21_LEFT_HEADER3_KEYNAMES);
			}
		} else {
			if ("Lucky".equalsIgnoreCase(strType)) {
				luckyHeaderKeys = SparcUtil.getListFromBrandContext(LUCKY_RIGHT_HEADER3_KEYNAMES);
			} 
			else if ("Aeropostale".equalsIgnoreCase(strType)) {
				luckyHeaderKeys = SparcUtil.getListFromBrandContext(AEROPOSTALE_RIGHT_HEADER3_KEYNAMES);
			}
			else if ("Nautica".equalsIgnoreCase(strType)) {
				luckyHeaderKeys = SparcUtil.getListFromBrandContext(NAUTICA_RIGHT_HEADER3_KEYNAMES);
			}else {
				luckyHeaderKeys = SparcUtil.getListFromBrandContext(F21_RIGHT_HEADER3_KEYNAMES);
			}
		}
	    return luckyHeaderKeys;
	}
	
	/**
	 * 
	 * @param params
	 * @throws WTException
	 */
	private void validateParams(Map params) throws WTException {
	    if (!FormatHelper.hasContent((String) params.get(PRODUCT_ID))) {
	        throw new WTException("Can not create SPARCPDFProductSpecificationHeader without PRODUCT_ID");
	    }
	   
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 */
	private Map<String, FlexTyped> createFlexTypedMap(Map params) throws WTException{
		LCSProduct product = null;
		LCSSeason season = null;
		LCSSeasonProductLink seasonProductLink = null;
		Map<String, FlexTyped> mapObjects = new HashMap<>();
		try {
		product = (LCSProduct) LCSProductQuery.findObjectById((String) params.get(PRODUCT_ID));
		String oid = (String) params.get("oid");

		String specId = (String) params.get(SPEC_ID);
		FlexSpecification spec = null;
		LCSSourcingConfigMaster sourcingConfigMaster = null;
		LCSSourcingConfig objSourcingConfig = null;
		if (FormatHelper.hasContent(specId)) {
			spec = (FlexSpecification) LCSProductQuery.findObjectById(specId);
			sourcingConfigMaster = spec.getSpecSource();
			objSourcingConfig = (LCSSourcingConfig) VersionHelper.latestIterationOf(sourcingConfigMaster);
		}

		LCSSeasonMaster seasonMaster = null;
		if (FormatHelper.hasContent((String) params.get(SEASONMASTER_ID))) {
			seasonMaster = (LCSSeasonMaster) LCSSeasonQuery.findObjectById((String) params.get(SEASONMASTER_ID));
			season = (LCSSeason) VersionHelper.latestIterationOf(seasonMaster);
		} else if (spec != null && sourcingConfigMaster != null) {
			@SuppressWarnings("unchecked")
			Collection<LCSSourceToSeasonLink> stsl = new LCSSourcingConfigQuery()
					.getSourceToSeasonLinks(sourcingConfigMaster);
			Iterator<LCSSourceToSeasonLink> iterators = stsl.iterator();
			while (iterators.hasNext()) {
				LCSSourceToSeasonLink sourceToSeasonLink = iterators.next();
				seasonMaster = (LCSSeasonMaster) sourceToSeasonLink.getSeasonMaster();
			}
			if (seasonMaster != null) {
				season = (LCSSeason) VersionHelper.latestIterationOf(seasonMaster);
			}

		} else if (FormatHelper.hasContent(oid) && oid.indexOf("LCSSeason") > -1) {
			season = (LCSSeason) LCSQuery.findObjectById(oid);
		}
		if (season != null) {
			seasonProductLink = LCSSeasonQuery.findSeasonProductLink(product, season);

		}
		mapObjects.put("Product", product);
		mapObjects.put("Season", season);
		mapObjects.put("SeasProdLink", seasonProductLink);
		mapObjects.put("Sourcing", objSourcingConfig);

		return mapObjects;
		}catch(WTException ex) {
			throw new WTException(ex);
		}
	}
	

	
    private PdfPCell createDataCell(Map params,String cellAllignment )  {
    	String seasonNameLbl = "";
		String seasonName = "";
		String plmProductNameLbl = "";
		String plmProductName = "";
		String plmProductStatusLbl = "";
		String plmProductStatus = "";
		String sourcingConfigurationLbl = "";
		String sourcingConfiguration = "";
		LCSSeasonMaster seasonMaster = null;
		PdfPTable datatable = new PdfPTable(2);
		PdfPCell datatableCell = new PdfPCell(datatable);
		try {
			LCSProduct product = null;
			LCSSeason season = null;
			LCSSeasonProductLink seasonProductLink = null;
			if(!FormatHelper.hasContent((String) params.get(PRODUCT_ID))) {
				throw new WTException("Can not create SPARCPDFProductSpecificationHeader without PRODUCT_ID");
			}
			product = (LCSProduct) LCSProductQuery.findObjectById((String) params.get(PRODUCT_ID));
			String oid = (String) params.get("oid");

			String specId = (String) params.get(SPEC_ID);
			FlexSpecification spec = null ;
			LCSSourcingConfigMaster sourcingConfigMaster = null ;
			if (FormatHelper.hasContent(specId)) {
				spec = (FlexSpecification) LCSProductQuery.findObjectById(specId);
				sourcingConfigMaster = spec.getSpecSource();

			}

			if(FormatHelper.hasContent((String) params.get(SEASONMASTER_ID))) {
				seasonMaster = (LCSSeasonMaster) LCSSeasonQuery.findObjectById((String) params.get(SEASONMASTER_ID));
				season = (LCSSeason) VersionHelper.latestIterationOf(seasonMaster);		
			}else if (spec != null && sourcingConfigMaster != null) {
				@SuppressWarnings("unchecked")
				Collection<LCSSourceToSeasonLink> stsl = new LCSSourcingConfigQuery().getSourceToSeasonLinks(sourcingConfigMaster);
				Iterator<LCSSourceToSeasonLink> iterators = stsl.iterator();
				while(iterators.hasNext()) {
					LCSSourceToSeasonLink sourceToSeasonLink = iterators.next();
					seasonMaster = (LCSSeasonMaster)sourceToSeasonLink.getSeasonMaster();
				}
					if(seasonMaster!=null){
					season = (LCSSeason) VersionHelper.latestIterationOf(seasonMaster);
					}
			
			} else if (FormatHelper.hasContent(oid) && oid.indexOf("LCSSeason") > -1) {
				season = (LCSSeason) LCSQuery.findObjectById(oid);
			} 
			if(season != null) {
				
				seasonProductLink = LCSSeasonQuery.findSeasonProductLink(product, season);
			
			}
		
        	if("LEFT".equals(cellAllignment)){
				
				plmProductNameLbl =  "PLM Product Name:";
				plmProductName = getAttListValue(product.getFlexType().getAttribute("productName"), product);
				
				seasonNameLbl = "Season" + " : ";
				if(season != null) {
				seasonName = getAttListValue(season.getFlexType().getAttribute("seasonName"), season);
				}
				
				
				datatable.addCell(createCell(plmProductNameLbl,"FORMLABEL"));
				datatable.addCell(createCell(plmProductName,"DISPLAYTEXT"));
				
				datatable.addCell(createCell(seasonNameLbl,"FORMLABEL"));
				datatable.addCell(createCell(seasonName,"DISPLAYTEXT"));
				
        	}else if("RIGHT".equals(cellAllignment)){
        		plmProductStatusLbl="PLM Product Status :";
        		sourcingConfigurationLbl = "Sourcing Configuration" + ":";
				sourcingConfiguration =  sourcingConfigMaster.getSourcingConfigName();
				if(seasonProductLink!=null){
				
				plmProductStatus = getAttListValue(seasonProductLink.getFlexType().getAttribute("scPLMProductStatus"), seasonProductLink);
				}
					datatable.addCell(createCell(plmProductStatusLbl,"FORMLABEL"));
					datatable.addCell(createCell(plmProductStatus,"DISPLAYTEXT"));
						
					datatable.addCell(createCell(sourcingConfigurationLbl,"FORMLABEL"));
					datatable.addCell(createCell(sourcingConfiguration,"DISPLAYTEXT"));
			
        	}
            
        
			
			datatableCell.setPadding(4.0f);
			datatableCell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXDDEBF7"));
			datatableCell.setBorderColor(pgh.getColor("HEX669999"));
			datatableCell.setBorderWidthLeft(0.0f);
			datatableCell.setBorderColorLeft(pgh.getCellBGColor("BORDERED_BLOCK", "HEXDDEBF7"));
			datatableCell.setBorderColorRight(pgh.getCellBGColor("BORDERED_BLOCK", "HEXDDEBF7"));
			datatableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
			datatableCell.setVerticalAlignment(Element.ALIGN_TOP);
			datatableCell.setPaddingTop(30.0f);
			datatableCell.setFixedHeight(fixedHeight);			
			
			return datatableCell;
            
		}catch(Exception e) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("\t ERROR \t" + e + "\t");
			}
			
			e.printStackTrace();
           // throw new WTException(e);
        }
		finally{
			 return datatableCell;
		}
    }
    
    private PdfPCell createCell(String text,String textType) {
		
		PdfPCell cell = new PdfPCell(pgh.multiFontPhrase(text, pgh.getCellFont(textType, null, null)));
		cell.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXDDEBF7"));
		cell.setBorderColor(pgh.getCellBGColor("BORDERED_BLOCK", null));
		cell.setBorderWidth(0.0f);
		//cell.setBorder(0);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		cell.setVerticalAlignment(Element.ALIGN_TOP);
		
		return cell;
		
	}
    
    public static String getAttListValue(FlexTypeAttribute typeAttribute,FlexTyped typed) throws WTException {
		String key = "";
		String value = "";
		try {
			key = typeAttribute.getAttKey();
			value = (String) typed.getValue(key);
			AttributeValueList valueList = typeAttribute.getAttValueList();
			if(valueList != null) {
				value = valueList.getValue(value, null);
			}
			return value;
		} catch (Exception e) {
			// TODO: handle exception
			throw new WTException(e);
		}
		
	}
}
