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
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sizing.SizingQuery;
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
import com.lcs.wc.flextype.FlexTyped;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import wt.org.WTUser;

import org.apache.jena.sparql.function.library.leviathan.log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sparc.wc.util.SparcConstants;
import com.sparc.wc.util.SparcUtil;

import java.io.*;

import wt.fc.WTObject;
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
public class SPARCPDFProductSpecificationHeader1 extends PdfPTable implements PDFHeader {

	private static final Logger LOGGER = LogManager.getLogger(SPARCPDFProductSpecificationHeader1.class.getName());

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
	//static String f21ImageFileLeft = "";

	static PDFGeneratorHelper pgh = new PDFGeneratorHelper();

	static float fixedHeight = 225.0f;
	private static  String LUCKY_LEFT_HEADERKEYNAMES = "";
	private static  String LUCKY_RIGHT_HEADERKEYNAMES = "";
	
	
	private static  String AEROPOSTALE_LEFT_HEADERKEYNAMES = "";
	private static  String AEROPOSTALE_RIGHT_HEADERKEYNAMES = "";
	
	private static  String NAUTICA_LEFT_HEADERKEYNAMES = "";
	private static  String NAUTICA_RIGHT_HEADERKEYNAMES = "";
	
	private static String F21_LEFT_HEADERKEYNAMES = "";
	private static String F21_RIGHT_HEADERKEYNAMES = "";

	static {
		try {
			imageFile = LCSProperties.get("com.lcs.wc.product.PDFProductSpecificationHeader.headerImage",
					FormatHelper.formatOSFolderLocation(webHomeLocation) + "/images/FlexPLMLogo.png");
			//Commented out imagesFileLeft variables to generalize as part of RM Task 10480
			//imageFileLeft = LCSProperties.get("com.lcs.wc.product.SPARCPDFProductSpecificationHeader.headerImageLeft");
			//luckyImageFileLeft = LCSProperties.get("com.lcs.wc.product.Lucky.SPARCPDFProductSpecificationHeader.headerImageLeft");

			imageFileRight = LCSProperties.get("com.lcs.wc.product.PDFProductSpecificationHeader.headerImage",
					FormatHelper.formatOSFolderLocation(webHomeLocation) + "/images/FlexPLMLogo.png");
			//f21ImageFileLeft = LCSProperties.get("com.lcs.wc.product.F21.SPARCPDFProductSpecificationHeader.headerImageLeft");
			wthome = WTProperties.getServerProperties().getProperty("wt.home");
			imageFile = wthome + File.separator + imageFile;
			//imageFileLeft =  wthome + File.separator + imageFileLeft;
			imageFileRight =  wthome + File.separator + imageFileRight;
			//luckyImageFileLeft = wthome + File.separator + luckyImageFileLeft;
			//f21ImageFileLeft = wthome + File.separator + f21ImageFileLeft;
			LUCKY_LEFT_HEADERKEYNAMES = LCSProperties.get("com.sparc.lucky.leftHeaderKeyDisplayNames");
			LUCKY_RIGHT_HEADERKEYNAMES = LCSProperties.get("com.sparc.lucky.rightHeaderKeyDisplayNames");
			
			F21_LEFT_HEADERKEYNAMES = LCSProperties.get("com.sparc.f21.leftHeaderKeyDisplayNames");
			F21_RIGHT_HEADERKEYNAMES = LCSProperties.get("com.sparc.f21.rightHeaderKeyDisplayNames");
			
			AEROPOSTALE_LEFT_HEADERKEYNAMES = LCSProperties.get("com.sparc.aeropostale.leftHeaderKeyDisplayNames");
			AEROPOSTALE_RIGHT_HEADERKEYNAMES = LCSProperties.get("com.sparc.aeropostale.rightHeaderKeyDisplayNames");
			
			NAUTICA_LEFT_HEADERKEYNAMES = LCSProperties.get("com.sparc.nautica.leftHeaderKeyDisplayNames");
			NAUTICA_RIGHT_HEADERKEYNAMES = LCSProperties.get("com.sparc.nautica.rightHeaderKeyDisplayNames");

		} catch (Exception e) {
			System.out.println("Error initializing cache for ExcelGeneratorHelper");
			e.printStackTrace();
		}
	}

	/**
	 * Creates a new instance of PDFProductSpecificationHeader
	 */
	public SPARCPDFProductSpecificationHeader1() {
	}

	/**
	 * Constructor that specifies how many columns to have in the header
	 *
	 * Call the super(int) which initializes the header as a PdfPTable
	 * 
	 * @param cols
	 *            number columns will be in the header
	 */
	public SPARCPDFProductSpecificationHeader1(int cols) {
		super(cols);
	}

	/**
	 * returns another instance of PDFProductSpecificationHeader with the table filled, which can be added to a Document
	 * 
	 * @param params
	 * @throws WTException
	 * @return
	 */
	public Element getPDFHeader(Map params) throws WTException {
		try {

			PdfPCell tableDataLeft = null;
			PdfPCell imageCellRight = null;
			PdfPCell tableDataRight = null;
			PdfPCell imageCellLeft = null;

			SPARCPDFProductSpecificationHeader1 ppsh = new SPARCPDFProductSpecificationHeader1(4);
			ppsh.setWidthPercentage(95.0f);
			float[] widths = { 30.0f, 20.0f, 20.0f,30.0f };
			ppsh.setWidths(widths);
			LCSProduct product = (LCSProduct) LCSProductQuery.findObjectById((String) params.get(PRODUCT_ID));
			String strBrand = SparcUtil.getAttListValue("scBrand",product);
			
			String productFlexType = product.getFlexType().getFullNameDisplay(false);

			/*if(productFlexType.contains("Lucky")) {
				imageCellLeft = createImageCell("LEFT",null,"Lucky");
				tableDataLeft = createLuckyDataCell(params,"LEFT","Lucky");
				//PdfPCell spacerCell = createSpacerCell();
				imageCellRight = createImageCell("RIGHT",product,"Lucky");
				tableDataRight = createLuckyDataCell(params,"RIGHT","Lucky");
			}
			else if(productFlexType.contains("Forever 21")){
				imageCellLeft = createImageCell("LEFT",null,"F21");
				tableDataLeft = createLuckyDataCell(params,"LEFT","F21");
				//PdfPCell spacerCell = createSpacerCell();
				imageCellRight = createImageCell("RIGHT",product,"F21");
				tableDataRight = createLuckyDataCell(params,"RIGHT","F21");

			}
			else {
				imageCellLeft = createImageCell("LEFT",null,"Reebok");
				tableDataLeft = createDataCell(params,"LEFT");
				//PdfPCell spacerCell = createSpacerCell();
				imageCellRight = createImageCell("RIGHT",product,"Reebok");
				tableDataRight = createDataCell(params,"RIGHT");
			}*/

			String flexType = "";

			System.out.println("productFlexType-------------"+productFlexType);
			if (productFlexType.contains(SparcConstants.LUCKY_BRAND) || productFlexType.contains(SparcConstants.F21_BRAND)) {
				flexType = productFlexType.contains(SparcConstants.LUCKY_BRAND) ? SparcConstants.LUCKY_BRAND : SparcConstants.F21_BRAND;
			} else if(productFlexType.contains(SparcConstants.AEROPOSTALE_BRAND) || productFlexType.contains(SparcConstants.NAUTICA_BRAND)) {
				flexType = productFlexType.contains(SparcConstants.AEROPOSTALE_BRAND) ? SparcConstants.AEROPOSTALE_BRAND : SparcConstants.NAUTICA_BRAND;
			}else {
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

	private PdfPCell createImageCell(String imageAllignString, LCSProduct productObj,String brand) throws WTException {
		try {
			Image img = null;
			String imageFileName ="";
			
			LOGGER.debug("**Propert Name:com.lcs.wc.product."+brand+".SPARCPDFProductSpecificationHeader.headerImageLeft");
			if("LEFT".equals(imageAllignString)){
				//img = "Lucky".equalsIgnoreCase(brand)?Image.getInstance(luckyImageFileLeft) : "Forever 21".equalsIgnoreCase(brand)?Image.getInstance(f21ImageFileLeft) :Image.getInstance(imageFileLeft);
				imageFileName = LCSProperties.get("com.lcs.wc.product."+brand+".SPARCPDFProductSpecificationHeader.headerImageLeft","codebase//rfa/sparc//images//Forever21Logo.png");
				LOGGER.debug("Image to be added on the Left:"+wthome + File.separator + imageFileName);
				img = Image.getInstance(wthome + File.separator + imageFileName);
				if(brand.equalsIgnoreCase("Nautica") || brand.equalsIgnoreCase("Aeropostale")) {
					fixedHeight=175.0f;
					LOGGER.debug("**Setting the Header Hieght as 175");
					}
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
				ImageCellEvent event = new ImageCellEvent(img, 0f, 4.5f);
				event.setAlignLeft(true);
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
		           System.out.println("Key: " + SparcUtil.getObjectFrom(mapObjects,entry.getKey()) + ", Value: " + entry.getValue());
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
	/*private List<Map.Entry<String, String>> getLuckyHeaderKeys(String cellAlignment,String strType) {
		List<Map.Entry<String, String>> luckyHeaderKeys;
		if ("LEFT".equalsIgnoreCase(cellAlignment)) {
			luckyHeaderKeys = "Lucky".equalsIgnoreCase(strType)?SparcUtil.getListFromBrandContext(LUCKY_LEFT_HEADERKEYNAMES): SparcUtil.getListFromBrandContext(F21_LEFT_HEADERKEYNAMES);
		} else {
			luckyHeaderKeys = "Lucky".equalsIgnoreCase(strType)?SparcUtil.getListFromBrandContext(LUCKY_RIGHT_HEADERKEYNAMES):SparcUtil.getListFromBrandContext(F21_RIGHT_HEADERKEYNAMES);
		}
		return luckyHeaderKeys;
	}*/

	private List<Map.Entry<String, String>> getLuckyHeaderKeys(String cellAlignment, String strType) {
		HeaderKeyProvider headerKeyProvider;

		if ("LEFT".equalsIgnoreCase(cellAlignment)) {
			if ("Lucky".equalsIgnoreCase(strType)) {
				headerKeyProvider = new LuckyHeaderKeyProvider(LUCKY_LEFT_HEADERKEYNAMES);
				
			} 
			else if ("Aeropostale".equalsIgnoreCase(strType)) {
				headerKeyProvider = new AeropostaleHeaderKeyProvider(AEROPOSTALE_LEFT_HEADERKEYNAMES);
			}
			else if ("Nautica".equalsIgnoreCase(strType)) {
				headerKeyProvider = new NauticaHeaderKeyProvider(NAUTICA_LEFT_HEADERKEYNAMES);
			}else {
				headerKeyProvider = new F21HeaderKeyProvider(F21_LEFT_HEADERKEYNAMES);
			}
		} else {
			if ("Lucky".equalsIgnoreCase(strType)) {
				headerKeyProvider = new LuckyHeaderKeyProvider(LUCKY_RIGHT_HEADERKEYNAMES);
			} else if ("Aeropostale".equalsIgnoreCase(strType)) {
				headerKeyProvider = new AeropostaleHeaderKeyProvider(AEROPOSTALE_RIGHT_HEADERKEYNAMES);
			}
			else if ("Nautica".equalsIgnoreCase(strType)) {
				headerKeyProvider = new NauticaHeaderKeyProvider(NAUTICA_RIGHT_HEADERKEYNAMES);
			}else {
				headerKeyProvider = new F21HeaderKeyProvider(F21_RIGHT_HEADERKEYNAMES);
			}
		}

		return headerKeyProvider.getHeaderKeys();
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

			SearchResults sizeResults = SizingQuery.findProductSizeCategoriesForProduct(product);
			Vector objResults = sizeResults.getResults();
			ProductSizeCategory objProductSizeCategory = (ProductSizeCategory) objResults.stream()
					.map(test -> {
						String strProdSizeId;
						strProdSizeId = ((FlexObject) test).getData("PRODUCTSIZECATEGORY.BRANCHIDITERATIONINFO");
						try {
							return (ProductSizeCategory) LCSQuery.findObjectById("VR:com.lcs.wc.sizing.ProductSizeCategory:" + strProdSizeId);
						} catch (WTException e) {
							throw new RuntimeException(e);
						}
					})
					.filter(Objects::nonNull)
					.findFirst()
					.orElse(null);

			mapObjects.put("ProdSizeDef", objProductSizeCategory);
			mapObjects.put("Product", product);
			mapObjects.put("Season", season);
			mapObjects.put("SeasProdLink", seasonProductLink);
			mapObjects.put("Sourcing", objSourcingConfig);

		return mapObjects;
		}catch(WTException ex) {
			throw new WTException(ex);
		}
	}
	

	
	private PdfPCell createDataCell(Map params,String cellAllignment ) throws WTException {
		String seasonNameLbl = "";
		String seasonName = "";
		String plmProductNameLbl = "";
		String plmProductName = "";
		String plmProductStatusLbl = "";
		String plmProductStatus = "";
		String sourcingConfigurationLbl = "";
		String sourcingConfiguration = "";

		String productDivisionLbl = "";
		String productDivision = "";

		String sizeRangeLbl = "";
		String sizeRange = "";

		String productTypeLbl = "";
		String productType = "";

		String developerLbl = "";
		String developer = "";

		String genderLbl = "";
		String gender = "";

		String designerLbl = "";
		String designer = "";

		String ageLbl = "";
		String age = "";

		String patternDeveloperLbl = "";
		String patternDeveloper = "";

		String lastLbl = "";
		String last = "";
		LCSMaterial lastMatObj = null;


		String outsoleLbl = "";
		String outsole = "";
		LCSMaterial outsoleMatObj = null;


		LCSSeasonMaster seasonMaster = null;
		PdfPTable datatable = new PdfPTable(2);
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
				//seasonName = FormatHelper.format((String) season.getValue("seasonName"));
			
				seasonProductLink = LCSSeasonQuery.findSeasonProductLink(product, season);
				
			
			}


			if("LEFT".equals(cellAllignment)){
				//plmProductNameLbl = product.getFlexType().getAttribute("productName").getAttDisplay() + ":";
				plmProductNameLbl = "PLM Product Name :";
				plmProductName = getAttListValue(product.getFlexType().getAttribute("productName"), product);
				seasonNameLbl = "Season" + " : ";
				if(season != null) {
				
				seasonName = getAttListValue(season.getFlexType().getAttribute("seasonName"), season);
				}
				try {
				
				
				productDivisionLbl="Product   Division :";
				productTypeLbl="Product Type :";
				if(seasonProductLink!=null){
				//productDivisionLbl = seasonProductLink.getFlexType().getAttribute("scDivision").getAttDisplay() + ":";
				productDivision = getAttListValue(seasonProductLink.getFlexType().getAttribute("scDivision"), seasonProductLink);
				
				//productTypeLbl = seasonProductLink.getFlexType().getAttribute("scProductType").getAttDisplay() + ":";
				productType = getAttListValue(seasonProductLink.getFlexType().getAttribute("scProductType"), seasonProductLink);
				}
				genderLbl = product.getFlexType().getAttribute("scGender").getAttDisplay() + ":";
				gender = getAttListValue(product.getFlexType().getAttribute("scGender"), product);
				
				ageLbl = product.getFlexType().getAttribute("scAge").getAttDisplay() + ":";
				age = getAttListValue(product.getFlexType().getAttribute("scAge"), product);
				}catch(Exception ex) {
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug("\t Seaosn is not found error" + season + "\t");
					}
				}

				datatable.addCell(createCell(plmProductNameLbl,"FORMLABEL"));
				datatable.addCell(createCell(plmProductName,"DISPLAYTEXT"));
				
				
				datatable.addCell(createCell(seasonNameLbl,"FORMLABEL"));
				datatable.addCell(createCell(seasonName,"DISPLAYTEXT"));
				
				datatable.addCell(createCell(productDivisionLbl,"FORMLABEL"));
				datatable.addCell(createCell(productDivision,"DISPLAYTEXT"));
				
				datatable.addCell(createCell(productTypeLbl,"FORMLABEL"));
				datatable.addCell(createCell(productType,"DISPLAYTEXT"));
				
				datatable.addCell(createCell(genderLbl,"FORMLABEL"));
				datatable.addCell(createCell(gender,"DISPLAYTEXT"));
				
				datatable.addCell(createCell(ageLbl,"FORMLABEL"));
				datatable.addCell(createCell(age,"DISPLAYTEXT"));

			}
			else if("RIGHT".equals(cellAllignment)){
				try {
					plmProductStatusLbl="PLM Product Status :";
					developerLbl="Developer :";
					designerLbl="Designer :";
					patternDeveloperLbl="Pattern Developer:";
					sourcingConfigurationLbl = "Sourcing Configuration" + ":";
					sourcingConfiguration =  sourcingConfigMaster.getSourcingConfigName();
					
					
					
					sizeRangeLbl = product.getFlexType().getAttribute("scSizeRange").getAttDisplay() + ":";
					sizeRange = getAttListValue(product.getFlexType().getAttribute("scSizeRange"), product);
				System.out.println("----typepath--"+product.getFlexType().getFullNameDisplay().toString());
				if(product.getFlexType().getFullNameDisplay().toString().equals("Footwear\\Reebok")){
					lastLbl = product.getFlexType().getAttribute("scLast").getAttDisplay() + ":";
					lastMatObj = (LCSMaterial) product.getValue("scLast");
					if(lastMatObj != null) {
						last = lastMatObj.getName().toString();
					}

					outsoleLbl = product.getFlexType().getAttribute("scOutsole").getAttDisplay() + ":";
					outsoleMatObj = (LCSMaterial) product.getValue("scOutsole");
					if(outsoleMatObj != null) {
						outsole = outsoleMatObj.getName().toString();
					}
				}
						if(seasonProductLink!=null){
				//plmProductStatusLbl = seasonProductLink.getFlexType().getAttribute("scPLMProductStatus").getAttDisplay() + ":";
				plmProductStatus = getAttListValue(seasonProductLink.getFlexType().getAttribute("scPLMProductStatus"), seasonProductLink);
				
				//getAttListValue(product.getFlexType().getAttribute("productName"), product);
				
				
				
				//developerLbl = seasonProductLink.getFlexType().getAttribute("scdeveloper").getAttDisplay() + ":";
				
				FlexObject devloperUser =(FlexObject) seasonProductLink.getValue("scdeveloper");
				developer=(String)devloperUser.getData("FULLNAME");
				
				
				
				//designerLbl = seasonProductLink.getFlexType().getAttribute("scDesigner").getAttDisplay() + ":";
				
				FlexObject designerUser =(FlexObject) seasonProductLink.getValue("scDesigner");
				
				designer=(String)designerUser.getData("FULLNAME");
				
						}
						
						
						
					if(!product.getFlexType().getFullNameDisplay().toString().equals("Footwear\\Reebok")&&seasonProductLink!=null) {
					
						try {
							FlexObject patternDeveloperUser = (FlexObject)seasonProductLink.getValue("scpatterndeveloper");

							patternDeveloper=(String)patternDeveloperUser.getData("FULLNAME");
						}catch(Exception ex) {

						}
					} 
					
			
				}
				catch(Exception ex) {
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug("\t Seaosn is not found error" + season + "\t");
					}
				}
				
				datatable.addCell(createCell(plmProductStatusLbl,"FORMLABEL"));
				datatable.addCell(createCell(plmProductStatus,"DISPLAYTEXT"));
				datatable.addCell(createCell(sourcingConfigurationLbl,"FORMLABEL"));
				datatable.addCell(createCell(sourcingConfiguration,"DISPLAYTEXT"));

				

				datatable.addCell(createCell(sizeRangeLbl,"FORMLABEL"));
				datatable.addCell(createCell(sizeRange,"DISPLAYTEXT"));

				

				datatable.addCell(createCell(developerLbl,"FORMLABEL"));
				datatable.addCell(createCell(developer,"DISPLAYTEXT"));

				

				datatable.addCell(createCell(designerLbl,"FORMLABEL"));
				datatable.addCell(createCell(designer,"DISPLAYTEXT"));
				
				
				if(!product.getFlexType().getFullNameDisplay().toString().equals("Footwear\\Reebok")) {
					datatable.addCell(createCell(patternDeveloperLbl,"FORMLABEL"));
					datatable.addCell(createCell(patternDeveloper,"DISPLAYTEXT"));
				}else {
					datatable.addCell(createCell(lastLbl,"FORMLABEL"));
					datatable.addCell(createCell(last,"DISPLAYTEXT"));

					datatable.addCell(createCell(outsoleLbl,"FORMLABEL"));
					datatable.addCell(createCell(outsole,"DISPLAYTEXT"));
				}

			}

			String COMPONENT_ID = (String) params.get("COMPONENT_ID");
			LCSMeasurements measurements =  null;
			WTObject wtObject = null;
			if(FormatHelper.hasContent(COMPONENT_ID)) {
				wtObject = (WTObject) LCSQuery.findObjectById((String) params.get(COMPONENT_ID));
				if(wtObject instanceof LCSMeasurements) {
					measurements = (LCSMeasurements) wtObject;
				}
			}
			
			

			PdfPCell datatableCell = new PdfPCell(datatable);
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
		} catch (Exception e) {
			// TODO: handle exception
			throw new WTException(e);
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
