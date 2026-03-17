package com.sparc.wc.sample.gen;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lcs.wc.client.web.PDFGeneratorHelper;
import com.lcs.wc.client.web.pdf.PDFTableGenerator;
import com.lcs.wc.client.web.pdf.PDFUtils;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.measurements.LCSFitTest;
import com.lcs.wc.measurements.LCSMeasurements;
import com.lcs.wc.measurements.LCSMeasurementsQuery;
import com.lcs.wc.measurements.LCSPointsOfMeasure;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.PDFProductSpecificationGenerator2;
import com.lcs.wc.sample.LCSSample;
import com.lcs.wc.sample.LCSSampleRequest;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MetricFormatter;
import com.lcs.wc.util.SortHelper;
import com.lcs.wc.util.VersionHelper;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lcs.wc.flextype.UomConversionCache;
import com.lcs.wc.util.LCSMessage;
import com.lcs.wc.util.RB;

import com.lcs.wc.product.PDFProductSpecificationGenerator2;
import com.lcs.wc.product.PDFProductSpecificationMeasurements2;

import wt.fc.WTObject;
import wt.util.WTException;

/**
 * This class produces The Fit Information Table along with a sub Header for Fit Details Page in Tech Pack
 * 
 */
public class SPARCSampleRequestFitDetailsPDFContent extends SPARCSamplePDFContentGenerator {
	private static final Logger LOGGER = LogManager.getLogger(SPARCSampleRequestFitDetailsPDFContent.class.getName());
	public static final String PRODUCT_ID = "PRODUCT_ID";
	public static final String HEADER_HEIGHT = "HEADER_HEIGHT";
	public float pageHeight = 0;
	public static PDFGeneratorHelper pgh = new PDFGeneratorHelper();
	private static final String BORDERED_BLOCK = "BORDERED_BLOCK";
	private static final String FORMLABEL = "FORMLABEL";
	private static final String DISPLAYTEXT = "DISPLAYTEXT";
	public static final double NULL_VALUE_PLACEHOLDER = Double.parseDouble(LCSMeasurementsQuery.NULL_VALUE_PLACEHOLDER);
	
	private static final String defaultUnitOfMeasure = LCSProperties.get(LCSProperties.DEFAULT_UNIT_OF_MEASURE,
            "si.Length.in");

	public SPARCSampleRequestFitDetailsPDFContent() {
	}

	/**
	 * This method Produces both ethe sub Header and the Fit Information Tab
	 */
	@Override
	public Collection<PdfPTable> getPDFContentCollection(Map params, Document document) throws WTException {
		LOGGER.debug("**SPARCSampleRequestFitDetailsPDFContent getPDFContentCollection - start");
		String requestName = "";
		String sampleType = "";
		String sampleColorways = "";
		String sampleSize = "";
		String evaulationComments = "";
		String evaluationStatus = "";
		LCSMeasurements measurements = null;
		try {
			WTObject obj = (WTObject) LCSQuery.findObjectById((String) params.get(PRODUCT_ID));
			if (!(obj instanceof LCSProduct)) {
				throw new WTException("Can not use PDFProductSpecificationMeasurements on a non-LCSProduct - " + obj);
			}

			WTObject obj2 = (WTObject) LCSQuery
					.findObjectById((String) params.get(PDFProductSpecificationGenerator2.COMPONENT_ID));
			if ( !(obj2 instanceof LCSSample)) {
				throw new WTException("Can not use PDFProductSpecificationFitSpec on without a Sample - " + obj2);
			}
			this.pageHeight = this.calcPageHeight(params, document);
			LCSSample sample = (LCSSample) obj2;

			LCSSampleRequest sampleRequest = sample.getSampleRequest();
			LCSFitTest fitTest = LCSMeasurementsQuery.findFitTest(sample);
			
		String customUOM = "";
			
       

			if(fitTest != null) {
				measurements = (LCSMeasurements) VersionHelper.latestIterationOf(fitTest.getMeasurementsMaster());
				
				sampleSize = measurements.getSampleSize();
				LOGGER.debug("**sampleSize222:"+sampleSize);
				LOGGER.debug("**sampleType-fit222:"+fitTest.getSampleSize());
				sampleSize = fitTest.getSampleSize();
				 if (measurements.getValue("uom") != null) {
                    customUOM = (String) measurements.getValue("uom");                
                                 
               }
			}
			requestName = sampleRequest.getName();
			sampleType = sample.getFlexType().getTypeDisplayName();
			LOGGER.debug("**requestName:"+requestName);
			LOGGER.debug("**sampleType:"+sampleType);
			if(sample.getColor() != null) {
				sampleColorways = (String)(SeasonProductLocator.getSKUARev((LCSPartMaster)sample.getColor()).getValue("skuName"));
			}
			evaulationComments = (String) sample.getValue("scEvaluationComments");
			LOGGER.debug("**evaulationComments1:"+evaulationComments);
			evaluationStatus = getAttListValue(sample.getFlexType().getAttribute("scSampleEvaluationstatus"), sample);
			LOGGER.debug("**evaluationStatus:"+evaluationStatus);

			PDFTableGenerator tg = null;
			Collection<PdfPTable> content = new ArrayList();
			Collection<Element> spcontent = new ArrayList<>();
			PdfPCell emptyCell = new PdfPCell();
			emptyCell.setMinimumHeight(20.0F);
			emptyCell.setBorder(0);

			PdfPTable fullTable = new PdfPTable(1);
			fullTable.setSplitLate(false);
			PdfPTable e = null;
			PdfPCell cell = null;

			tg = new PDFTableGenerator(document);
			tg.cellClassLight = "RPT_TBL";
			tg.cellClassDark = "RPT_TBD";
			tg.tableSubHeaderClass = "RPT_HEADER";
			tg.tableHeaderClass = "TABLE-HEADERTEXT";
			spcontent.add(tg.drawHeader());
			
			LOGGER.debug("**requestName:"+requestName);
			cell = new PdfPCell(SPARCSampleRequestFitDetailsPDFContent.pgh.multiFontPara(requestName, SPARCSampleRequestFitDetailsPDFContent.pgh
					.getCellFont(FORMLABEL, null, null)));
			cell.setColspan(0);
			cell.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			cell.setBorderWidth(1.0F);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_CENTER);
			fullTable.setWidthPercentage(100);
			fullTable.addCell(cell);
			
			PdfPTable attributeTable = new PdfPTable(6);
			
			LOGGER.debug("**sampleType:"+sampleType);
			
			cell = new PdfPCell(SPARCSampleRequestFitDetailsPDFContent.pgh.multiFontPara("Request Type : " + sampleType, SPARCSampleRequestFitDetailsPDFContent.pgh
					.getCellFont(DISPLAYTEXT, null, null)));
			cell.setColspan(6);
			cell.setBorder(Rectangle.NO_BORDER);
			//cell.setBorderWidthBottom(1f);
			cell.setHorizontalAlignment(0);
			cell.setVerticalAlignment(5);
			attributeTable.addCell(cell);

			LOGGER.debug("**sampleColorways:"+sampleColorways);
			
			cell = new PdfPCell(SPARCSampleRequestFitDetailsPDFContent.pgh.multiFontPara("Colorway : " + sampleColorways, SPARCSampleRequestFitDetailsPDFContent.pgh
					.getCellFont(DISPLAYTEXT, null, null)));
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(0);
			cell.setVerticalAlignment(5);
			attributeTable.addCell(cell);
			
			LOGGER.debug("**sampleSize:"+sampleSize);
			
			cell = new PdfPCell(SPARCSampleRequestFitDetailsPDFContent.pgh.multiFontPara("Sample Size : " + sampleSize, SPARCSampleRequestFitDetailsPDFContent.pgh
					.getCellFont(DISPLAYTEXT, null, null)));
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(0);
			cell.setVerticalAlignment(5);
			attributeTable.addCell(cell);
			
			
			String reqDate = null;

			Date requestDate = (Date) sampleRequest.getValue("sampleRequestRequestDate");
			LOGGER.debug("**requestDate:"+requestDate);

			if(requestDate!=null) {
				String pattern = "MM/dd/yyyy";
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
				reqDate = simpleDateFormat.format(requestDate);
				LOGGER.debug("**reqDate:"+reqDate);
				cell = new PdfPCell(SPARCSampleRequestFitDetailsPDFContent.pgh.multiFontPara("Request Date: " + reqDate, SPARCSampleRequestFitDetailsPDFContent.pgh
						.getCellFont(DISPLAYTEXT, null, null)));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(0);
				cell.setVerticalAlignment(5);
				attributeTable.addCell(cell);
			}else {
				cell = new PdfPCell(SPARCSampleRequestFitDetailsPDFContent.pgh.multiFontPara("Request Date: " , SPARCSampleRequestFitDetailsPDFContent.pgh
						.getCellFont(DISPLAYTEXT, null, null)));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(0);
				cell.setVerticalAlignment(5);
				attributeTable.addCell(cell);
			}
			
			String recDate = null;
			Date receivedDate = (Date) sample.getValue("ptcReceivedDate");
			LOGGER.debug("**receivedDate:"+receivedDate);

			if(receivedDate!=null) {
				String pattern = "MM/dd/yyyy";
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
				recDate = simpleDateFormat.format(receivedDate);
				LOGGER.debug("**recDate:"+recDate);
				cell = new PdfPCell(SPARCSampleRequestFitDetailsPDFContent.pgh.multiFontPara("Received Date: " + recDate, SPARCSampleRequestFitDetailsPDFContent.pgh
						.getCellFont(DISPLAYTEXT, null, null)));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(0);
				cell.setVerticalAlignment(5);
				attributeTable.addCell(cell);
			}else {
				cell = new PdfPCell(SPARCSampleRequestFitDetailsPDFContent.pgh.multiFontPara("Received Date: " , SPARCSampleRequestFitDetailsPDFContent.pgh
						.getCellFont(DISPLAYTEXT, null, null)));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(0);
				cell.setVerticalAlignment(5);
				attributeTable.addCell(cell);
			}
			
			cell = new PdfPCell(SPARCSampleRequestFitDetailsPDFContent.pgh.multiFontPara("Evaluation Status: " + evaluationStatus, SPARCSampleRequestFitDetailsPDFContent.pgh
					.getCellFont(DISPLAYTEXT, null, null)));
			cell.setBorder(Rectangle.NO_BORDER);
			//cell.setNoWrap(true);
			cell.setHorizontalAlignment(0);
			cell.setVerticalAlignment(5);
			attributeTable.addCell(cell);
			
		
			
			if (params.get(PDFProductSpecificationMeasurements2.UOM) != null) {
                String pdfUom = (String) params.get(PDFProductSpecificationMeasurements2.UOM);
                if (!(pdfUom).equals("none") && pdfUom.startsWith("si.Length")) {
                    customUOM = pdfUom;
                    customUOM = customUOM.substring(customUOM.lastIndexOf(".") + 1);
                } else if (pdfUom.equals(FormatHelper.FRACTION_FORMAT)) {
                    String fractionLabel = LCSMessage.getLocalizedMessage(RB.MEASUREMENTS, "fraction_LBL");
                    customUOM = fractionLabel + " "
                            + defaultUnitOfMeasure.substring(defaultUnitOfMeasure.lastIndexOf(".") + 1);
                }
            }
			
			
				cell = new PdfPCell(SPARCSampleRequestFitDetailsPDFContent.pgh.multiFontPara("UOM: "+customUOM , SPARCSampleRequestFitDetailsPDFContent.pgh
					.getCellFont(DISPLAYTEXT, null, null)));
			
			
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(0);
			cell.setVerticalAlignment(5);
			attributeTable.addCell(cell);
			
			attributeTable.setWidthPercentage(100);
			
			PdfPCell attributeTableCell = new PdfPCell(attributeTable);
			fullTable.addCell(attributeTableCell);
			
			
			PdfPTable fitInformationTable = new PdfPTable(1);
			//fitInformationTable.setWidthPercentage(100.0f);
			PdfPCell colorwaysContentCell = new PdfPCell(PDFUtils.prepareElement(new SPARCSampleRequestFitDetailsPDFContent().getFitDetailsPDFTable(params)));
            colorwaysContentCell.setBorder(0);
            fitInformationTable.addCell(colorwaysContentCell);
            fitInformationTable.setSplitLate(false);
			fitInformationTable.setWidthPercentage(95);
			fitInformationTable.getTotalHeight();
			
			PdfPCell fitInformationTableCell = new PdfPCell(fitInformationTable);
			fullTable.addCell(fitInformationTableCell);


			for (Element element : spcontent) {
				if (element instanceof PdfPTable) {
					e = (PdfPTable)element;
					cell = new PdfPCell(e);
					cell.setBorder(0);
				} else if (element instanceof PdfPCell) {
					cell = (PdfPCell)element;
				} 

			} 
			
			content.add(fullTable);
			LOGGER.debug("**SPARCSampleDetailsPDFContent getPDFContentCollection - end");
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WTException(e);
		}
		
		
	}

	private float calcPageHeight(Map params, Document doc) throws WTException {
		float height = doc.top() - doc.bottom();
		if (params.get(HEADER_HEIGHT) != null) {
			Object hh = params.get(HEADER_HEIGHT);
			if (hh instanceof Float) {
				height = height - ((Float) hh).floatValue() - 50.0f;
			}
			if (hh instanceof String) {
				height = height - (new Float((String) hh)).floatValue() - 50.0f;
			}
		}

		return height;
	}


	/**
	 * This method is to return the Display value of the drop down attribute values
	 * @param typeAttribute
	 * @param typed
	 * @return
	 * @throws WTException
	 */
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
			return value!=null?value:"";
		} catch (Exception e) {
			throw new WTException(e);
		}

	}

	
	/**
	 * This method will be caled to get the generated Table contents from SPARCPDFProductSpecificationGenerator2
	 * @param params
	 * @return
	 * @throws WTException
	 */
	public Element getFitDetailsPDFTable(Map params) throws WTException {
	        try {
	            PdfPTable ppsh = new PdfPTable(10);
	           // ppsh.setWidthPercentage(95.0f);
	            ppsh.setWidthPercentage(90.0f);
	            float[] widths = {25.0f, 5.0f, 5.0f,7.50f, 7.50f, 7.50f,7.50f,7.50f, 7.50f,25.0f };
	            ppsh.setWidths(widths);
				ppsh =getFitInformationTableHeader(ppsh);
				Map fitInfoDataMap = getFitInformationTableData(params);
			//	LOGGER.debug("**fitInfoDataMapSize:"+fitInfoDataMagetFitInformationTableHeaderp.size());
				ppsh = drawFitInformationTable(ppsh,fitInfoDataMap);
				if(!(fitInfoDataMap.size()>0))
					ppsh = drawEmptyRow(ppsh);
				
	            return ppsh;
	        } catch (Exception e) {
	            throw new WTException(e);
	        }
	    }
	  
		/**
		 * Empty Row insertion
		 * @param ppsh
		 * @return
		 * @throws WTException
		 */
		public PdfPTable drawEmptyRow(PdfPTable ppsh) throws WTException {
			
			 /* PdfPCell bottomCell1 = new PdfPCell(pgh.multiFontPara(" "));
			  bottomCell1.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
	           bottomCell1.setBorderColor(pgh.getColor("HEXFFFFFF"));
			   ppsh.addCell(bottomCell1);
			   
			  PdfPCell bottomCell2 = new PdfPCell(pgh.multiFontPara(" "));
			   bottomCell2.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
	           bottomCell2.setBorderColor(pgh.getColor("HEXFFFFFF"));
			   ppsh.addCell(bottomCell2);*/
			   
			  PdfPCell bottomCell3 = new PdfPCell(pgh.multiFontPara(" "));
			   bottomCell3.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
	           bottomCell3.setBorderColor(pgh.getColor("HEXFFFFFF"));
			   ppsh.addCell(bottomCell3);
			   
			/*  PdfPCell bottomCell4 = new PdfPCell(pgh.multiFontPara(" "));
			   bottomCell4.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
	           bottomCell4.setBorderColor(pgh.getColor("HEXFFFFFF"));
			   ppsh.addCell(bottomCell4);*/
			   
			   PdfPCell bottomCell5 = new PdfPCell(pgh.multiFontPara(" "));
			   bottomCell5.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
			   bottomCell5.setBorderColor(pgh.getColor("HEXFFFFFF"));
		       ppsh.addCell(bottomCell5);
		       
		       PdfPCell bottomCell6 = new PdfPCell(pgh.multiFontPara(" "));
		       bottomCell6.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
		       bottomCell6.setBorderColor(pgh.getColor("HEXFFFFFF"));
		       ppsh.addCell(bottomCell6);
				  
		       PdfPCell bottomCell7 = new PdfPCell(pgh.multiFontPara(" "));
		       bottomCell7.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
		       bottomCell7.setBorderColor(pgh.getColor("HEXFFFFFF"));
		       ppsh.addCell(bottomCell7);
				  
		       PdfPCell bottomCell8 = new PdfPCell(pgh.multiFontPara(" "));
		       bottomCell8.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
		       bottomCell8.setBorderColor(pgh.getColor("HEXFFFFFF"));
		       ppsh.addCell(bottomCell8);
				   
		       PdfPCell bottomCell9 = new PdfPCell(pgh.multiFontPara(" "));
		       bottomCell9.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
		       bottomCell9.setBorderColor(pgh.getColor("HEXFFFFFF"));
		       ppsh.addCell(bottomCell9);
					  
		       PdfPCell bottomCell10 = new PdfPCell(pgh.multiFontPara(" "));
		       bottomCell10.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
		       bottomCell10.setBorderColor(pgh.getColor("HEXFFFFFF"));
		       ppsh.addCell(bottomCell10);
					  
		       PdfPCell bottomCell11 = new PdfPCell(pgh.multiFontPara(" "));
		       bottomCell11.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
		       bottomCell11.setBorderColor(pgh.getColor("HEXFFFFFF"));
		       ppsh.addCell(bottomCell11);
					  
		       PdfPCell bottomCell12 = new PdfPCell(pgh.multiFontPara(" "));
		       bottomCell12.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
		       bottomCell12.setBorderColor(pgh.getColor("HEXFFFFFF"));
		       ppsh.addCell(bottomCell12);
		       
		       PdfPCell bottomCell13 = new PdfPCell(pgh.multiFontPara(" "));
		       bottomCell13.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
		       bottomCell13.setBorderColor(pgh.getColor("HEXFFFFFF"));
		       ppsh.addCell(bottomCell13);
			 
			 return ppsh;
		}
	   
	 /**
	  * This method produces a map of values required to build the Fit Information table
	 * @param params
	 * @return
	 * @throws WTException
	 */
	public Map getFitInformationTableData(Map params) throws WTException {
			
		   LinkedHashMap allRows = new LinkedHashMap();
			String sampleID = (String)params.get("COMPONENT_ID");
			LOGGER.debug("**getFitInformationTableData sampleID:"+sampleID);
			 LCSSample sample = (LCSSample) LCSQuery.findObjectById(sampleID);
			 LOGGER.debug("** sample:"+sample);
			 LCSFitTest fitTest = LCSMeasurementsQuery.findFitTest(sample);
			 LOGGER.debug("** fitTest:"+fitTest);
			 LCSMeasurements measurements = null;
			 LCSPointsOfMeasure pom;
			 if(fitTest!=null) {
				 
				 measurements = (LCSMeasurements) VersionHelper.latestIterationOf(fitTest.getMeasurementsMaster());
				 LOGGER.debug("** measurements:"+measurements);
				
				Collection<FlexObject> fitTestData = LCSMeasurementsQuery.findFitTestData(fitTest).getResults();
				LOGGER.debug("** fitTestData1:"+fitTestData);
				fitTestData  = SortHelper.sortFlexObjectsByNumber(fitTestData, "LCSPOINTSOFMEASURE.SORTINGNUMBER");
				LOGGER.debug("** fitTestData2:"+fitTestData);
				 Iterator<FlexObject> fitTestDataIterator = fitTestData.iterator();
				 
				FlexType measurementType =  FlexTypeCache.getFlexTypeFromPath("Measurements");
				FlexTypeAttribute sectionAtt = measurementType.getAttribute("section");
				FlexTypeAttribute criticalPomAtt = measurementType.getAttribute("criticalPom");
				
				
				FlexTypeAttribute plusToleranceAtt = measurementType.getAttribute("plusTolerance");
				FlexTypeAttribute minusToleranceAtt = measurementType.getAttribute("minusTolerance");
				FlexTypeAttribute requestedMeasurementAtt = measurementType.getAttribute("requestedMeasurement");
				FlexTypeAttribute quotedMeasurementAtt = measurementType.getAttribute("quotedMeasurement");
				FlexTypeAttribute quotedMeasurementDeltaAtt = measurementType.getAttribute("quotedMeasurementDelta");
				FlexTypeAttribute actualMeasurementAtt = measurementType.getAttribute("actualMeasurement");
				FlexTypeAttribute actualMeasurementDeltaAtt = measurementType.getAttribute("actualMeasurementDelta");
				FlexTypeAttribute newMeasurementAtt = measurementType.getAttribute("newMeasurement");
				int precision = 0;
				
				
				//String doubleFormat = "si.Length.in";
				String doubleFormat = LCSProperties.get(LCSProperties.DEFAULT_UNIT_OF_MEASURE, "si.Length.in");
				LOGGER.debug("** doubleFormat:"+doubleFormat);	

				 if (measurements.getValue("uom") != null) {
					doubleFormat = (String) measurements.getValue("uom");
					doubleFormat = "si.Length." + doubleFormat;
				}
				
				//System.out.println("double Format First-----------------------"+doubleFormat);
					
						
				Object object = null;
				 while(fitTestDataIterator.hasNext()) {
					 LinkedHashMap rowData  = new LinkedHashMap();
					 FlexObject flitTestFlexObj = fitTestDataIterator.next();
						 LOGGER.debug("** flitTestFlexObj:"+flitTestFlexObj);
						  pom = (LCSPointsOfMeasure)LCSQuery.findObjectById("com.lcs.wc.measurements.LCSPointsOfMeasure:"+flitTestFlexObj.getString("LCSPOINTSOFMEASURE.IDA2A2"));
						  LOGGER.debug("** pom:"+pom);
						  LOGGER.debug("** pom.isDropped():"+pom.isDropped()); 
						  
						 if(!pom.isDropped()) {
						 String isPlaceholder = pom.getValue("placeholderRow")!=null?String.valueOf(pom.getValue("placeholderRow")):"";
						 LOGGER.debug("** isPlaceholder:"+isPlaceholder);
						 
						 String sectionValue = "";
						 String criticalityValue = "";
						 
						 if((String)pom.getValue("section")!=null && FormatHelper.hasContent((String)pom.getValue("section"))) {
							 sectionValue = sectionAtt.getAttValueList().getValue((String)pom.getValue("section"), null);
						 LOGGER.debug("** sectionValue:"+sectionValue);
						 }
						 
						 String pomNo =  pom.getValue("number")!=null?(String)pom.getValue("number"):"";
						 LOGGER.debug("**pomNo:"+pomNo);
						 
						 String pomName =  pom.getValue("measurementName")!=null?(String)pom.getValue("measurementName"):"";
						 LOGGER.debug("**pomName:"+pomName);
						 
						 if((String)pom.getValue("criticalPom")!=null && FormatHelper.hasContent((String)pom.getValue("criticalPom")))
							 criticalityValue =  criticalPomAtt.getAttValueList().getValue((String)pom.getValue("criticalPom"), null);
						 LOGGER.debug("** criticalityValue:"+criticalityValue);
						 
						 String plusTolerance = "";
						 object = pom.getValue("plusTolerance");
						 LOGGER.debug("** NULL_VALUE_PLACEHOLDER:"+NULL_VALUE_PLACEHOLDER);
						 
						 Boolean isConversion = true;
						 Boolean isFractionInchConversion = false;	


						 //System.out.println("params.get(PDFProductSpecificationMeasurements2.UOM)--------"+params.get(PDFProductSpecificationMeasurements2.UOM));
						 
						 if (params.get(PDFProductSpecificationMeasurements2.UOM) != null) {
							if (!((String) params.get(PDFProductSpecificationMeasurements2.UOM)).equals("none")) {
							       doubleFormat = (String) params.get(PDFProductSpecificationMeasurements2.UOM);
								   if("FRACTION_FORMAT".equalsIgnoreCase(doubleFormat)){
									   isFractionInchConversion = true;
									   isConversion = false;									   
								   }
								   else{
								     isConversion = true;
									 isFractionInchConversion = false;
								   }
							}
						 }
						// if (object!=null && !isPlaceholder.equalsIgnoreCase("1") && !(String.valueOf(pom.getValue("plusTolerance"))).equalsIgnoreCase("999999")) {
						 if (object!=null && !isPlaceholder.equalsIgnoreCase("1") && FormatHelper.parseFloat(String.valueOf(pom.getValue("plusTolerance")))!=NULL_VALUE_PLACEHOLDER) {
							plusTolerance =  String.valueOf(pom.getValue("plusTolerance"));
							 LOGGER.debug("**plusTolerance-before:"+plusTolerance);
							 precision=plusToleranceAtt.getAttDecimalFigures();
							 if(isConversion){
							//	System.out.println("inside if conversion"); 
								plusTolerance = FormatHelper.applyFormat(plusTolerance, FormatHelper.MEASUREMENT_UNIT_FORMAT, precision, doubleFormat);
							 }
							 else if(isFractionInchConversion){
								 plusTolerance = FormatHelper.applyFormat(plusTolerance, doubleFormat, precision);								 
							 }							 
						 }
						 
						 String minusTolerance = "";
						 object = pom.getValue("minusTolerance");
						 if (object!=null && !isPlaceholder.equalsIgnoreCase("1") && FormatHelper.parseFloat(String.valueOf(pom.getValue("minusTolerance")))!=NULL_VALUE_PLACEHOLDER) { 
							 minusTolerance =  String.valueOf(pom.getValue("minusTolerance"));
                             LOGGER.debug("**minusTolerance-before:"+minusTolerance);
							 precision=minusToleranceAtt.getAttDecimalFigures();
							 if(isConversion){
								 minusTolerance = FormatHelper.applyFormat(minusTolerance, FormatHelper.MEASUREMENT_UNIT_FORMAT, precision, doubleFormat);
								 minusTolerance = String.format("%.4f",Float.valueOf(minusTolerance));
						
							 }else if(isFractionInchConversion){
								 minusTolerance = FormatHelper.applyFormat(minusTolerance, doubleFormat, precision);								 
							 }						
								
						 }
						 
						 String requestedMeasurement = "";
						 object = pom.getValue("requestedMeasurement");
						 if (object!=null && !isPlaceholder.equalsIgnoreCase("1") && FormatHelper.parseFloat(String.valueOf(pom.getValue("requestedMeasurement")))!=NULL_VALUE_PLACEHOLDER) {
							 requestedMeasurement =  String.valueOf(pom.getValue("requestedMeasurement"));
							 LOGGER.debug("**requestedMeasurement-before::::::::::::::::"+requestedMeasurement);
							
							 precision=requestedMeasurementAtt.getAttDecimalFigures();
							 
							 if(isConversion){
								requestedMeasurement = FormatHelper.applyFormat(requestedMeasurement, FormatHelper.MEASUREMENT_UNIT_FORMAT, precision, doubleFormat);
							    requestedMeasurement = String.format("%.4f",Float.valueOf(requestedMeasurement));						
						
							 }else if(isFractionInchConversion){
								 requestedMeasurement = FormatHelper.applyFormat(requestedMeasurement, doubleFormat, precision);								 
							 }
							
							 
						 }
						 
						 String quotedMeasurement = "";
						 object = pom.getValue("quotedMeasurement");
						 if (object!=null && !isPlaceholder.equalsIgnoreCase("1")  && FormatHelper.parseFloat(String.valueOf(pom.getValue("quotedMeasurement")))!=NULL_VALUE_PLACEHOLDER) {
							 quotedMeasurement =  String.valueOf(pom.getValue("quotedMeasurement"));
							 LOGGER.debug("**quotedMeasurement-before:"+quotedMeasurement);			
							 
							 precision=quotedMeasurementAtt.getAttDecimalFigures();
							 
							 if(isConversion){
								quotedMeasurement = FormatHelper.applyFormat(quotedMeasurement, FormatHelper.MEASUREMENT_UNIT_FORMAT, precision, doubleFormat);
								quotedMeasurement = String.format("%.4f",Float.valueOf(quotedMeasurement));		
							 }else if(isFractionInchConversion){
								quotedMeasurement = FormatHelper.applyFormat(quotedMeasurement, doubleFormat, precision);								 
							 }						 				
							 
						 }
						 
						 String quotedMeasurementDelta = "";
						 object=pom.getValue("quotedMeasurementDelta");
						 if (object!=null & !isPlaceholder.equalsIgnoreCase("1") && FormatHelper.parseFloat(String.valueOf(pom.getValue("quotedMeasurementDelta")))!=NULL_VALUE_PLACEHOLDER){ 
							 quotedMeasurementDelta =  String.valueOf(pom.getValue("quotedMeasurementDelta"));
							 LOGGER.debug("**quotedMeasurementDelta-before:"+quotedMeasurementDelta);							 
							
							 precision=quotedMeasurementDeltaAtt.getAttDecimalFigures();
							 if(isConversion){
								quotedMeasurementDelta = FormatHelper.applyFormat(quotedMeasurementDelta, FormatHelper.MEASUREMENT_UNIT_FORMAT, precision, doubleFormat);
								quotedMeasurementDelta = String.format("%.4f",Float.valueOf(quotedMeasurementDelta));
					
							 }else if(isFractionInchConversion){
								 quotedMeasurementDelta = FormatHelper.applyFormat(quotedMeasurementDelta, doubleFormat, precision);								 
							 }							 
					
						 }
						 
						 String actualMeasurement = "";
						 object = pom.getValue("actualMeasurement");
						 LOGGER.debug("** FormatHelper.parseFloatactualMeasurement:"+FormatHelper.parseFloat(String.valueOf(pom.getValue("actualMeasurement"))));
						 if (object!=null && !isPlaceholder.equalsIgnoreCase("1") && FormatHelper.parseFloat(String.valueOf(pom.getValue("actualMeasurement")))!=NULL_VALUE_PLACEHOLDER){
							 actualMeasurement =  String.valueOf(pom.getValue("actualMeasurement"));
							 LOGGER.debug("**actualMeasurement-before:"+actualMeasurement);
							 
							 precision=actualMeasurementAtt.getAttDecimalFigures();
							  if(isConversion){
								actualMeasurement = FormatHelper.applyFormat(actualMeasurement, FormatHelper.MEASUREMENT_UNIT_FORMAT, precision, doubleFormat);
								actualMeasurement = String.format("%.4f",Float.valueOf(actualMeasurement));						
					
							 }else if(isFractionInchConversion){
								 actualMeasurement = FormatHelper.applyFormat(actualMeasurement, doubleFormat, precision);								 
							 }
						 
						 }
						 
						 String actualMeasurementDelta = "";
						 object = pom.getValue("actualMeasurementDelta");
						 LOGGER.debug("** FormatHelper.parseFloatactualMeasurementDelta:"+FormatHelper.parseFloat(String.valueOf(pom.getValue("actualMeasurementDelta"))));
						 if (object!=null && !isPlaceholder.equalsIgnoreCase("1") && FormatHelper.parseFloat(String.valueOf(pom.getValue("actualMeasurementDelta")))!=NULL_VALUE_PLACEHOLDER){
							 actualMeasurementDelta =  String.valueOf(pom.getValue("actualMeasurementDelta"));
							 LOGGER.debug("**actualMeasurementDelta-before:"+actualMeasurementDelta);
							
							 precision=actualMeasurementDeltaAtt.getAttDecimalFigures();
							 
							 if(isConversion){
								actualMeasurementDelta = FormatHelper.applyFormat(actualMeasurementDelta, FormatHelper.MEASUREMENT_UNIT_FORMAT, precision, doubleFormat);
								actualMeasurementDelta = String.format("%.4f",Float.valueOf(actualMeasurementDelta));						
					
							 }else if(isFractionInchConversion){
								 actualMeasurementDelta = FormatHelper.applyFormat(actualMeasurementDelta, doubleFormat, precision);								 
							 }
							
						 }
						 
						 String newMeasurement = "";
						 object =  pom.getValue("newMeasurement");
						 if (object!=null && !isPlaceholder.equalsIgnoreCase("1") && FormatHelper.parseFloat(String.valueOf(pom.getValue("newMeasurement")))!=NULL_VALUE_PLACEHOLDER){
							 newMeasurement =  String.valueOf(pom.getValue("newMeasurement"));
							 LOGGER.debug("**newMeasurement-before:"+newMeasurement);
							 precision=newMeasurementAtt.getAttDecimalFigures();
							  if(isConversion){
								newMeasurement = FormatHelper.applyFormat(newMeasurement, FormatHelper.MEASUREMENT_UNIT_FORMAT, precision, doubleFormat);
							    newMeasurement = String.format("%.4f",Float.valueOf(newMeasurement));						
					
							  }else if(isFractionInchConversion){
								 newMeasurement = FormatHelper.applyFormat(newMeasurement, doubleFormat, precision);								 
							 }
						
						 }
						 
						 
						 String sampleMeasurementComments =  pom.getValue("sampleMeasurementComments")!=null?String.valueOf(pom.getValue("sampleMeasurementComments")):"";
						 LOGGER.debug("**sampleMeasurementComments:"+sampleMeasurementComments);
						 
						 
						//  rowData.put("1", sectionValue);
						//  rowData.put("2", pomNo);
						  rowData.put("1", pomName);
						//  rowData.put("4", criticalityValue);
						  rowData.put("2", plusTolerance);
						  rowData.put("3", minusTolerance);
						  rowData.put("4", requestedMeasurement);
						  rowData.put("5", quotedMeasurement);
						  rowData.put("6", quotedMeasurementDelta);
						  rowData.put("7", actualMeasurement);
						  rowData.put("8", actualMeasurementDelta);
						  rowData.put("9", newMeasurement);
						  rowData.put("10", sampleMeasurementComments);
						  allRows.put((String)flitTestFlexObj.getString("LCSPOINTSOFMEASURE.SORTINGNUMBER"),rowData);
				 	}
				 }
			 }
			 LOGGER.debug("**allRows:"+allRows);
					return allRows;
	 }
	   /**
	    * Draws the actualtable and cells
	 * @param ppsh
	 * @param tableData
	 * @return
	 * @throws WTException
	 */
	public PdfPTable drawFitInformationTable(PdfPTable ppsh,Map tableData) throws WTException {

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
		   							
		   					}
		   				}
		   			}
		   		}
		   	}
		   }


		   return ppsh;
		   }   
	   
	@SuppressWarnings("unchecked")
	public Collection<String> getPageHeaderCollection() {
		return pageTitles;
	}
	
	
	 /**
	  * This method is to generate the TableHeaders
	 * @param ppsh
	 * @return
	 * @throws WTException
	 */
	public PdfPTable getFitInformationTableHeader(PdfPTable ppsh) throws WTException {
		 
	    	
          	/*PdfPCell cell_Section = new PdfPCell(pgh.multiFontPara("Section"));
          	cell_Section.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
          	cell_Section.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			ppsh.addCell(cell_Section);
			
			PdfPCell cell_POMNo = new PdfPCell(pgh.multiFontPara("POM #"));
			cell_POMNo.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
			cell_POMNo.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			ppsh.addCell(cell_POMNo);*/
			 
			PdfPCell cell_POMName = new PdfPCell(pgh.multiFontPara("POM Name"));
			cell_POMName.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
			cell_POMName.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			ppsh.addCell(cell_POMName);
			
			/*PdfPCell cell_Criticality = new PdfPCell(pgh.multiFontPara("Criticality"));
			cell_Criticality.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
			cell_Criticality.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			ppsh.addCell(cell_Criticality);*/
		
			PdfPCell cell_TolPlus = new PdfPCell(pgh.multiFontPara("Tol (+)"));
			cell_TolPlus.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
			cell_TolPlus.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			ppsh.addCell(cell_TolPlus);
			
			PdfPCell cell_TolMinus = new PdfPCell(pgh.multiFontPara("Tol (-)"));
			cell_TolMinus.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
			cell_TolMinus.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			ppsh.addCell(cell_TolMinus);
			 
			PdfPCell cell_ReqMeas = new PdfPCell(pgh.multiFontPara("Requested Meas."));
			cell_ReqMeas.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
			cell_ReqMeas.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			ppsh.addCell(cell_ReqMeas);
			
			PdfPCell cell_VendMeas = new PdfPCell(pgh.multiFontPara("Vendor Meas."));
			cell_VendMeas.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
			cell_VendMeas.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			ppsh.addCell(cell_VendMeas);
			
			PdfPCell cell_VendorDelta = new PdfPCell(pgh.multiFontPara("Vendor Meas. Delta"));
			cell_VendorDelta.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
			cell_VendorDelta.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			ppsh.addCell(cell_VendorDelta);
			
			PdfPCell cell_InternalMeas = new PdfPCell(pgh.multiFontPara("Internal Meas."));
			cell_InternalMeas.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
			cell_InternalMeas.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			ppsh.addCell(cell_InternalMeas);
			
			PdfPCell cell_InternalDelta = new PdfPCell(pgh.multiFontPara("Internal Meas. Delta"));
			cell_InternalDelta.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
			cell_InternalDelta.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			ppsh.addCell(cell_InternalDelta);
			 
			PdfPCell cell_RevisedMeas = new PdfPCell(pgh.multiFontPara("Revised Meas."));
			cell_RevisedMeas.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
			cell_RevisedMeas.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			ppsh.addCell(cell_RevisedMeas);
			
			PdfPCell cell_Comments = new PdfPCell(pgh.multiFontPara("Comments"));
			cell_Comments.setBackgroundColor(pgh.getCellBGColor("BORDERED_BLOCK", "HEXFFFFFF"));
			cell_Comments.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			ppsh.addCell(cell_Comments);
		 
		 return ppsh;
		 
	 }


}
