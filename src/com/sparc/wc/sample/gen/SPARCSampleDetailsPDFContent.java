package com.sparc.wc.sample.gen;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.client.web.PDFGeneratorHelper;
import com.lcs.wc.client.web.pdf.PDFTableGenerator;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.measurements.LCSFitTest;
import com.lcs.wc.measurements.LCSMeasurements;
import com.lcs.wc.measurements.LCSMeasurementsQuery;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.PDFProductSpecificationGenerator2;
import com.lcs.wc.sample.LCSSample;
import com.lcs.wc.sample.LCSSampleRequest;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.VersionHelper;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import wt.fc.WTObject;
import wt.util.WTException;

public class SPARCSampleDetailsPDFContent extends SPARCSamplePDFContentGenerator {
	private static final Logger LOGGER = LogManager.getLogger(SPARCSampleDetailsPDFContent.class.getName());
	public static final String PRODUCT_ID = "PRODUCT_ID";
	public static final String HEADER_HEIGHT = "HEADER_HEIGHT";
	public float pageHeight = 0;
	public static PDFGeneratorHelper pgh = new PDFGeneratorHelper();
	private static final String BORDERED_BLOCK = "BORDERED_BLOCK";
	private static final String FORMLABEL = "FORMLABEL";
	private static final String DISPLAYTEXT = "DISPLAYTEXT";


	public SPARCSampleDetailsPDFContent() {
	}

	@Override
	public Collection<PdfPTable> getPDFContentCollection(Map params, Document document) throws WTException {
		LOGGER.debug("**SPARCSampleDetailsPDFContent getPDFContentCollection - start");
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

			if(fitTest != null) {
				measurements = (LCSMeasurements) VersionHelper.latestIterationOf(fitTest.getMeasurementsMaster());
				sampleSize = measurements.getSampleSize();
				LOGGER.debug("**sampleSize222:"+sampleSize);
				LOGGER.debug("**sampleType-fit222:"+fitTest.getSampleSize());
				sampleSize = fitTest.getSampleSize();
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

			String pageTitle = "Sample Details";
			this.pageTitles.add(pageTitle);
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
			
			//PdfPTable headerTable = new PdfPTable(1);
			//headerTable.setHeaderRows(0);
			//headerTable.setWidthPercentage(100.0f);
			LOGGER.debug("**requestName:"+requestName);
			cell = new PdfPCell(SPARCSampleDetailsPDFContent.pgh.multiFontPara(requestName, SPARCSampleDetailsPDFContent.pgh
					.getCellFont(FORMLABEL, null, null)));
			cell.setColspan(0);
			cell.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			cell.setBorderWidth(1.0F);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_CENTER);
			//PdfPCell headerTableCell = new PdfPCell(headerTable);
			fullTable.setWidthPercentage(100);
			fullTable.addCell(cell);
			
			PdfPTable attributeTable = new PdfPTable(6);
			
			LOGGER.debug("**sampleType:"+sampleType);
			
			cell = new PdfPCell(SPARCSampleDetailsPDFContent.pgh.multiFontPara("Request Type : " + sampleType, SPARCSampleDetailsPDFContent.pgh
					.getCellFont(DISPLAYTEXT, null, null)));
			cell.setColspan(6);
			cell.setBorder(Rectangle.NO_BORDER);
			//cell.setBorderWidthBottom(1f);
			cell.setHorizontalAlignment(0);
			cell.setVerticalAlignment(5);
			attributeTable.addCell(cell);

			LOGGER.debug("**sampleColorways:"+sampleColorways);
			
			cell = new PdfPCell(SPARCSampleDetailsPDFContent.pgh.multiFontPara("Colorway : " + sampleColorways, SPARCSampleDetailsPDFContent.pgh
					.getCellFont(DISPLAYTEXT, null, null)));
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(0);
			cell.setVerticalAlignment(5);
			attributeTable.addCell(cell);
			
			LOGGER.debug("**sampleSize:"+sampleSize);
			
			cell = new PdfPCell(SPARCSampleDetailsPDFContent.pgh.multiFontPara("Sample Size : " + sampleSize, SPARCSampleDetailsPDFContent.pgh
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
				cell = new PdfPCell(SPARCSampleDetailsPDFContent.pgh.multiFontPara("Request Date: " + reqDate, SPARCSampleDetailsPDFContent.pgh
						.getCellFont(DISPLAYTEXT, null, null)));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(0);
				cell.setVerticalAlignment(5);
				attributeTable.addCell(cell);
			}else {
				cell = new PdfPCell(SPARCSampleDetailsPDFContent.pgh.multiFontPara("Request Date: " , SPARCSampleDetailsPDFContent.pgh
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
				cell = new PdfPCell(SPARCSampleDetailsPDFContent.pgh.multiFontPara("Received Date: " + recDate, SPARCSampleDetailsPDFContent.pgh
						.getCellFont(DISPLAYTEXT, null, null)));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(0);
				cell.setVerticalAlignment(5);
				attributeTable.addCell(cell);
			}else {
				cell = new PdfPCell(SPARCSampleDetailsPDFContent.pgh.multiFontPara("Received Date: " , SPARCSampleDetailsPDFContent.pgh
						.getCellFont(DISPLAYTEXT, null, null)));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(0);
				cell.setVerticalAlignment(5);
				attributeTable.addCell(cell);
			}
			
			cell = new PdfPCell(SPARCSampleDetailsPDFContent.pgh.multiFontPara("Evaluation Status : " + evaluationStatus, SPARCSampleDetailsPDFContent.pgh
					.getCellFont(DISPLAYTEXT, null, null)));
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setNoWrap(true);
			cell.setHorizontalAlignment(0);
			cell.setVerticalAlignment(5);
			attributeTable.addCell(cell);
			attributeTable.setWidthPercentage(100);
			
			cell = new PdfPCell(SPARCSampleDetailsPDFContent.pgh.multiFontPara("", SPARCSampleDetailsPDFContent.pgh
					.getCellFont(DISPLAYTEXT, null, null)));
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(0);
			cell.setVerticalAlignment(5);
			attributeTable.addCell(cell);
			
			PdfPCell attributeTableCell = new PdfPCell(attributeTable);
			
			fullTable.addCell(attributeTableCell);


			PdfPTable evaluationCommentsTable = new PdfPTable(1);
			evaluationCommentsTable.setWidthPercentage(100.0f);
			cell = new PdfPCell(SPARCSampleDetailsPDFContent.pgh.multiFontPara("Evaluation Comments", SPARCSampleDetailsPDFContent.pgh
					.getCellFont(FORMLABEL, null, null)));
			cell.setColspan(0);
			cell.setBackgroundColor(PDFGeneratorHelper.getCellBGColor(BORDERED_BLOCK,null));
			cell.setBorderWidth(1.0F);
			cell.setHorizontalAlignment(0);
			cell.setVerticalAlignment(5);
			evaluationCommentsTable.addCell(cell);
			cell = new PdfPCell(SPARCSampleDetailsPDFContent.pgh.multiFontPara(evaulationComments, SPARCSampleDetailsPDFContent.pgh
					.getCellFont(DISPLAYTEXT, null, null)));
			cell.setHorizontalAlignment(0);
			cell.setVerticalAlignment(5);
			//cell.setFixedHeight(20.0f);
			evaluationCommentsTable.addCell(cell);
			
			evaluationCommentsTable.setWidthPercentage(100);
			
			PdfPCell evaluationCommentsTableCell = new PdfPCell(evaluationCommentsTable);
			fullTable.addCell(evaluationCommentsTableCell);


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

	@SuppressWarnings("deprecation")
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

	@SuppressWarnings("unchecked")
	public Collection<String> getPageHeaderCollection() {
		return pageTitles;
	}



}
