/*
 * PDFProductSpecificationMeasurements2.java
 *
 * Created on August 24, 2005, 2:52 PM
 */

package com.sparc.wc.measurements.gen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Hashtable;

import wt.fc.WTObject;
import wt.util.WTException;

import com.lcs.wc.infoengine.client.web.ElementTableData;
import com.lcs.wc.client.web.FlexTypeGenerator;
import com.lcs.wc.client.web.TableColumn;
import com.lcs.wc.client.web.pdf.PDFTableGenerator;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.foundation.LCSQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lcs.wc.measurements.LCSFindMeasurementsDelegate;
import com.lcs.wc.measurements.LCSMeasurements;
import com.lcs.wc.measurements.LCSMeasurementsQuery;
import com.lcs.wc.measurements.MeasurementsFlexTypeScopeDefinition;
import com.lcs.wc.measurements.*;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.product.PDFProductSpecificationGenerator2;
import com.lcs.wc.product.PDFProductSpecificationMeasurements2;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.measurements.gen.*;

import com.lcs.wc.db.*;
import com.lcs.wc.util.LCSMessage;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.RB;
import com.lcs.wc.util.SortHelper;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.*;

/**
 * Writes the Measurements section of a PDF Product Specification
 * 
 * @author Chuck
 */
public class SPARCGradeRulesIncrementPDF extends MeasurementsPDFContentGenerator {
	private static final Logger LOGGER = LogManager.getLogger(MeasurementsGradingPDF.class.getName());

	public static final String PRODUCT_ID = "PRODUCT_ID";

	public static final String SPEC_ID = "SPEC_ID";

	public static final String MEASUREMENTS_ID = "MEASUREMENTS_ID";

	public static final String HEADER_HEIGHT = "HEADER_HEIGHT";

	public static final String REPEAT = "REPEAT";

	public float pageHeight = 0;

	// public static final int MEASUREMENTS_SIZE_WRAP_LIMIT =
	// FormatHelper.parseInt(LCSProperties.get("ProductSpecification.measurementsSizeWrapLimit", "11"));
	public static final int MEASUREMENTS_DISTRIBUTION_THRESHOLD = FormatHelper.parseInt(LCSProperties.get(
			"ProductSpecification.measurementsDistributionThreshold", "5"));

	public int measurementsSizeWrapLimit = 0;

	private static final boolean INCLUDE_MEASUREMENT_SIZES_WITHOUT_USER_SELECTION = LCSProperties
			.getBoolean("com.lcs.wc.measurements.MeasurementsGradingPDF.includeMeasurementSizesWithoutUserSelection");

	private static final int PRECISION = FormatHelper
			.parseInt(LCSProperties.get("com.lcs.wc.measurements.PRECISION", "3"));

	private static final String defaultUnitOfMeasure = LCSProperties.get(LCSProperties.DEFAULT_UNIT_OF_MEASURE,
			"si.Length.in");

	private static final String MEASUREMENTS_GRADING_ATT_COLUMN_ORDER = LCSProperties
			.get("com.lcs.wc.measurements.gen.MeasurementsGradingPDF.AttributeColumnsOrder");

	/** Creates a new instance of PDFProductSpecificationMeasurements2 */
	public SPARCGradeRulesIncrementPDF() {
	}

	/**
	 * returns the Collection of PdfPTables containing the Measurements for the specification
	 * 
	 * @param params
	 * @param document
	 * @throws WTException
	 * @return
	 */
	@Override
	public Collection getPDFContentCollection(Map params, Document document) throws WTException {
		try {
			WTObject obj = (WTObject) LCSProductQuery.findObjectById((String) params.get(PRODUCT_ID));
			if (!(obj instanceof LCSProduct)) {
				throw new WTException("Can not use PDFProductSpecificationMeasurements on a non-LCSProduct - " + obj);
			}

			WTObject obj2 = (WTObject) LCSQuery
					.findObjectById((String) params.get(PDFProductSpecificationGenerator2.COMPONENT_ID));
			if (obj2 == null || !(obj2 instanceof LCSMeasurements)) {
				throw new WTException("Can not use PDFProductSpecificationFitSpec on without a Measurements - " + obj2);
			}

			measurementsSizeWrapLimit = ((Integer) params.get(PDFProductSpecificationGenerator2.SIZES_PER_PAGE))
					.intValue();

			this.pageHeight = this.calcPageHeight(params, document);

			LCSMeasurements measurements = (LCSMeasurements) obj2;

			PDFTableGenerator tg = null;
			Collection content = new ArrayList();
			Collection spcontent = new ArrayList();
			Collection sectionPageTitles = new ArrayList();
			String pageTitle = "Grade Rules"+ "- "
					+ measurements.getValue("name");

			Collection poms = getPOMs(measurements);
			Map columnMap = getColumns(measurements, params);
			Collection columns = null;
			Vector keys = new Vector(columnMap.keySet());
			if (columnMap.size() > 1) {
				keys.remove(REPEAT);
			}
			Collections.sort(keys);
			// columnMap.get(REPEAT);
			Iterator k = keys.iterator();
			String title = "-				Graded Rules"
					+ measurements.getValue("name") +
					"    " + LCSMessage.getLocalizedMessage(RB.MEASUREMENTS, "sampleSizeColon_LBL")
					+ measurements.getSampleSize();

			String uom = defaultUnitOfMeasure;
			uom = uom.substring(uom.lastIndexOf(".") + 1);

			if (measurements.getValue("uom") != null) {
				uom = (String) measurements.getValue("uom");
			}

			if (params.get(PDFProductSpecificationMeasurements2.UOM) != null) {
				String pdfUom = (String) params.get(PDFProductSpecificationMeasurements2.UOM);
				if (!(pdfUom).equals("none") && pdfUom.startsWith("si.Length")) {
					uom = pdfUom;
					uom = uom.substring(uom.lastIndexOf(".") + 1);
				} else if (pdfUom.equals(FormatHelper.FRACTION_FORMAT)) {
					String fractionLabel = LCSMessage.getLocalizedMessage(RB.MEASUREMENTS, "fraction_LBL");
					uom = fractionLabel + " "
							+ defaultUnitOfMeasure.substring(defaultUnitOfMeasure.lastIndexOf(".") + 1);
				}
			}

			title = title + "    " + measurements.getFlexType().getAttribute("uom").getAttDisplay() + ": " + uom;

			while (k.hasNext()) {
				String key = (String) k.next();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("key=" + key);
				}

				columns = new ArrayList();
				if (columnMap.size() > 1) {
					columns.addAll((Collection) columnMap.get(REPEAT));
				}
				columns.addAll((Collection) columnMap.get(key));

				tg = new PDFTableGenerator(document);
				tg.cellClassLight = "RPT_TBL";
				tg.cellClassDark = "RPT_TBD";
				tg.tableSubHeaderClass = "RPT_HEADER";
				tg.tableHeaderClass = "TABLE-HEADERTEXT";

				tg.setTitle(title);
				spcontent.addAll(tg.drawTables(poms, columns));
				sectionPageTitles.add(pageTitle);
			}
			PdfPTable fullTable = new PdfPTable(1);
			PdfPTable e = null;
			PdfPCell cell = null;

			// Header
			if (FormatHelper
					.parseBoolean((String) params.get(PDFProductSpecificationMeasurements2.PRINT_MEAS_HEADER))) {
				Collection MeasHeaderAtts = (Collection) params
						.get(PDFProductSpecificationMeasurements2.MEAS_HEADER_ATTS);
				if (MeasHeaderAtts != null && !MeasHeaderAtts.isEmpty()) {
					if (PDFProductSpecificationMeasurements2.MEAS_HEADER_SAME_PAGE) {
						for (Iterator HeaderI = MeasHeaderAtts.iterator(); HeaderI.hasNext();) {
							e = (PdfPTable) HeaderI.next();
							cell = new PdfPCell(e);
							fullTable.addCell(cell);
						}
					} else {
						content.addAll(MeasHeaderAtts);
						this.pageTitles.addAll(
								(Collection) params.get(PDFProductSpecificationMeasurements2.MEAS_HEADER_PAGE_TITLES));
					}
				}
			}

			// Add report sections
			Collection Footer = (Collection) params.get(PDFProductSpecificationMeasurements2.MEAS_FOOTER_ATTS);
			boolean usingFooter = (Footer != null && !Footer.isEmpty() && FormatHelper.parseBoolean((String) params
					.get(PDFProductSpecificationMeasurements2.PRINT_MEAS_FOOTER)));
			LOGGER.trace("MEAS_ON_SINGLE_PAGE");
			Iterator sci = spcontent.iterator();
			while (sci.hasNext()) {	
				e = (PdfPTable) sci.next();
				cell = new PdfPCell(e);
				fullTable.addCell(cell);
			}

			// Add Footer
			if (usingFooter) {
				LOGGER.trace("usingFooter");
				if (PDFProductSpecificationMeasurements2.MEAS_FOOTER_SAME_PAGE) {
					for (Iterator footI = Footer.iterator(); footI.hasNext();) {
						e = (PdfPTable) footI.next();
						cell = new PdfPCell(e);
						fullTable.addCell(cell);
					}
					// Add BOM to content
					content.add(fullTable);
					this.pageTitles.add(pageTitle);
				} else {
					// Add BOM to content
					content.add(fullTable);
					this.pageTitles.add(pageTitle);
					// Add Footer to content
					content.addAll(Footer);
					this.pageTitles.addAll(
							(Collection) params.get(PDFProductSpecificationMeasurements2.MEAS_FOOTER_PAGE_TITLES));
				}
			} else {
				content.add(fullTable);
				this.pageTitles.add(pageTitle);
			}

			return content;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WTException(e);
		}
	}

	private float calcPageHeight(Map params, Document doc) throws WTException {
		float height = doc.top() - doc.bottom();
		///  ----HARSHAD -  Adjusting height only for Measurement Header
		if (params.get("HEADER_HEIGHT") != null) {
			Object hh = params.get("HEADER_HEIGHT");
			///  ----HARSHAD -   REDUCING MEASUREMENT CONTENT SPACE by 50 points.
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
	 * Gets the Collection of measurements objects for the given Product
	 * 
	 * @param product
	 * @throws WTException
	 * @return
	 */
	public Collection getMeasurements(LCSProduct product, FlexSpecification spec) throws WTException {
		Collection results = LCSMeasurementsQuery.findMeasurmentsForProduct(product, spec).getResults();

		Collection ms = LCSQuery.getObjectsFromResults(results, "VR:com.lcs.wc.measurements.LCSMeasurements:",
				"LCSMEASUREMENTS.BRANCHIDITERATIONINFO");

		return ms;
	}

	/**
	 * Gets the Points of Measure for the given Measurements object
	 * 
	 * @param measurements
	 * @throws WTException
	 * @return
	 */
	public Collection getPOMs(LCSMeasurements measurements) throws WTException {
		try {
			ArrayList currentSizes = new ArrayList();
			currentSizes.addAll(MOAHelper.getMOACollection((measurements.getSizeRun())));
			Collection data = LCSFindMeasurementsDelegate.findPOM(FormatHelper.getObjectId(measurements), null);
			data = SortHelper.sortFlexObjectsByNumber(data, "sortingNumber");
			//System.out.println("----------POMdata------------"+data);
			String baseSampleSize = measurements.getSampleSize();
			int baseSizeIndex = -1;
			int gradeSizeIndex = -1;
			Vector vector = (Vector)MOAHelper.getMOACollection(measurements.getSizeRun());
			baseSizeIndex= vector.indexOf(baseSampleSize);

			if(data!=null){
				if(!data.isEmpty()){
					Iterator datIterator = data.iterator();
					while(datIterator.hasNext()){
						ElementTableData etd = (ElementTableData) datIterator.next();
						String pomId=(String)etd.getData("IDA2A2");
						LCSPointsOfMeasure pom = (LCSPointsOfMeasure)LCSQuery.findObjectById(pomId);
						Hashtable sizeMap;
						sizeMap = pom.getMeasurementValues();
						//System.out.println("000000000000000000000000000000------------pom.getMeasurementValues()--"+pom.getMeasurementValues()) ;
						String baseSizeMeasureValue= etd.getData("size_"+baseSampleSize);
						Iterator sizeIter = currentSizes.iterator();
						while (sizeIter.hasNext()) {
							String size = (String) sizeIter.next();
							//size_CRANE
							MeasurementValues measValue = (MeasurementValues) sizeMap.get(size);
							MeasurementValues baseMeasValue = (MeasurementValues)sizeMap.get(baseSampleSize);


							gradeSizeIndex= vector.indexOf(size);

							if(gradeSizeIndex>baseSizeIndex||true){
								float tempMeasVal = 0.0f;
								float tempBaseMeasVal = 0.0f;
								if(null == measValue && null == baseMeasValue) {
									String currentValue = String.valueOf(tempMeasVal - tempBaseMeasVal);	 
									etd.setData("size_"+size,currentValue);
								}else {
									String currentValue = String.valueOf(measValue.getValue() - baseMeasValue.getValue());	 
									etd.setData("size_"+size,currentValue);
								}

							}else if(baseSizeIndex>gradeSizeIndex){
								String currentValue = String.valueOf(baseMeasValue.getValue() - measValue.getValue());	 
								etd.setData("size_"+size,currentValue);
							}



						}


					}
				}
			}
			return data;
		} catch (Exception e) {
			throw new WTException(e);
		}
	}

	/**
	 * Gets the Collection of TableColumns for displaying the Measurements in the Product Specification
	 * 
	 * @param measurements
	 * @throws WTException
	 * @return
	 */
	public Map getColumns(LCSMeasurements measurements, Map params) throws WTException {
		HashMap columnsMap = new HashMap();

		TableColumn column;

		Collection sizeRun = (Collection) params.get(PDFProductSpecificationGenerator2.SIZES1);
		Collection selectedSizes = new Vector<String>();
		/*
		 * Conditionally enable inclusion of measurement sizes (SPR 5690448)
		 */
		if (INCLUDE_MEASUREMENT_SIZES_WITHOUT_USER_SELECTION) {
			if (sizeRun == null || sizeRun.size() == 0) {
				selectedSizes = MOAHelper.getMOACollection(measurements.getSizeRun());
			}
		}

		if (sizeRun != null && sizeRun.size() > 0) {
			Collection measurmentSizes = MOAHelper.getMOACollection(measurements.getSizeRun());
			Iterator<String> it = measurmentSizes.iterator();
			while (it.hasNext()) {
				String size = (String) it.next();
				if (sizeRun.contains(size)) {
					selectedSizes.add(size);
				}
			}
		}

		/*
		 * // comment or remove to fix SPR#2169798 if(sizeRun == null || sizeRun.size() == 0){ sizeRun =
		 * MOAHelper.getMOACollection(measurements.getSizeRun()); }
		 */
		String sampleSize = null;

		// String doubleFormat = FormatHelper.FLOAT_FORMAT_NO_PARENS;
		String doubleFormat = defaultUnitOfMeasure;

		if (measurements.getValue("uom") != null) {
			doubleFormat = (String) measurements.getValue("uom");
			doubleFormat = "si.Length." + doubleFormat;
		}

		if (params.get(PDFProductSpecificationMeasurements2.UOM) != null) {
			if (!((String) params.get(PDFProductSpecificationMeasurements2.UOM)).equals("none")) {
				doubleFormat = (String) params.get(PDFProductSpecificationMeasurements2.UOM);
			}
		}

		if (measurements.getSampleSize() != null) {
			sampleSize = measurements.getSampleSize();
		}

		/*
		 * if(sizeRun.size() > measurementsSizeWrapLimit && (measurementsSizeWrapLimit != 0 ) ){ //Need to break the
		 * table
		 * 
		 * }
		 */

		Collection repeatingColumns = new ArrayList();
		FlexTypeGenerator flexg = new FlexTypeGenerator();
		flexg.setScope(MeasurementsFlexTypeScopeDefinition.MEASUREMENT_SCOPE);

		// Populate Columns based on property entries

		StringTokenizer parser = new StringTokenizer(MEASUREMENTS_GRADING_ATT_COLUMN_ORDER, ",");
		FlexType measurementsType = measurements.getFlexType();

		String measurementNameAttKey = measurementsType.getAttKeyForAttribute("measurementName");
		String numberAttKey = measurementsType.getAttKeyForAttribute("number");
		String plusToleranceAttKey = measurementsType.getAttKeyForAttribute("plusTolerance");
		String minusToleranceAttKey = measurementsType.getAttKeyForAttribute("minusTolerance");

		FlexTypeAttribute placeholderRowAtt = measurementsType.getAttribute("placeholderRow");

		while (parser.hasMoreTokens()) {
			String attString = parser.nextToken();
			StringTokenizer flexTypeToken = new StringTokenizer(attString, "|");
			String attName = flexTypeToken.nextToken();
			float columnSize = (new Double(flexTypeToken.nextToken())).floatValue();

			if ("SIZES".equals(attName)) {
				Iterator runs = getSizeRun(selectedSizes, sampleSize).iterator();
				int sizeSet = 1;
				String sizePrefix = "SIZE";
				while (runs.hasNext()) {
					Collection run = (Collection) runs.next();
					Iterator sizeRunIter = run.iterator();
					String size;

					Collection columns = new ArrayList();

					while (sizeRunIter.hasNext()) {
						size = (String) sizeRunIter.next();
						// String tempSize = FormatHelper.formatJavascriptObjectName(size);
						column = new TableColumn();
						column.setPdfColumnWidthRatio(columnSize);

						if (size.equals(sampleSize)) {

							// NULL_VALUE_PLACEHOLDER Substitution handled here
							column.setShowCriteria(LCSMeasurementsQuery.NULL_VALUE_PLACEHOLDER);
							column.setShowCriteriaNot(true);
							column.setShowCriteriaTarget("size_" + size);
							column.setShowCriteriaNumericCompare(true);


							//column.setColumnClassIndex("highlight");

							column.setDisplayed(true);
							column.setHeaderLabel(size);
							column.setTableIndex("size_" + size);
							column.setColumnWidth("1%");
							column.setWrapping(false);
							column.setAlign("right");
							column.setHeaderAlign("right");
							column.setDecimalPrecision(3);
							// column.setFormat(FormatHelper.FLOAT_FORMAT_NO_PARENS);
							// column.setFormat(doubleFormat);

							column.setAttributeType("float");
							// Hi-lite the Sample Size Column
							column.setColumnClass("HIGHLIGHT_GRAY_TEXT_STRIKETHROUGH_RED");//HIGHLIGHT_GRAY_TEXT_STRIKETHROUGH_RED
							//column.setColumnClassIndex("highLight");
							column.setBgColorIndex("red");
							//colorColumn.setBgColorIndex("LCSCOLOR.COLORHEXIDECIMALVALUE");
							//colorColumn.setUseColorCell(true);
							//	column.setColorIndex("green");
							column.setSpecialClassIndex("highlight");
							column.setColumnHeaderClass("TABLESUBHEADER_SPECIAL");
							// Handle highlighting
							column.setColumnClass("HIGHLIGHT_GRAY_TEXT_STRIKETHROUGH_RED");//HIGHLIGHT_GRAY_TEXT_STRIKETHROUGH_RED

						} else {

							// NULL_VALUE_PLACEHOLDER Substitution handled here
							column.setShowCriteria(LCSMeasurementsQuery.NULL_VALUE_PLACEHOLDER);
							column.setShowCriteriaNot(true);
							column.setShowCriteriaTarget("size_" + size);
							column.setShowCriteriaNumericCompare(true);

							// Handle highlighting
							column.setColumnClassIndex("highLight");

							column.setDisplayed(true);
							column.setHeaderLabel(size);
							column.setTableIndex("size_" + size);
							column.setColumnWidth("1%");
							column.setWrapping(false);
							column.setAlign("right");
							column.setHeaderAlign("right");
							column.setDecimalPrecision(3);
							// column.setFormat(FormatHelper.FLOAT_FORMAT_NO_PARENS);
							// column.setFormat(doubleFormat);
							column.setAttributeType("float");
						}
						column.setDecimalPrecision(PRECISION);
						if (doubleFormat.startsWith("si.Length.")) {
							column.setOutputUom(doubleFormat);
							column.setFormat(FormatHelper.MEASUREMENT_UNIT_FORMAT);
						} else {
							column.setFormat(doubleFormat);
						}
						columns.add(column);
					}

					columnsMap.put(sizePrefix + sizeSet, columns);
					sizeSet++;
				}
			} else {
				FlexTypeAttribute att = measurementsType.getAttribute(attName);
				column = flexg.createTableColumn(att);
				if ("object_ref".equals(att.getAttVariableType())
						|| "object_ref_list".equals(att.getAttVariableType())) {
					column.setTableIndex(att.getAttKey() + "Display");
					column.setLinkTableIndex(att.getAttKey());
				} else {
					column.setTableIndex(att.getAttKey());
				}
				column.setColumnClassIndex("highLight"); // handle highlighting
				column.setPdfColumnWidthRatio(columnSize);
				column.setWrapping(true);

				if (measurementNameAttKey.equals(att.getAttKey())) {
					column.setColumnWidth("5%");
				} else {
					column.setShowCriteria("1.0");
					column.setShowCriteriaNot(true);
					column.setShowCriteriaTarget(placeholderRowAtt.getAttKey());
					column.setShowCriteriaNumericCompare(true);
				}

				if (numberAttKey.equals(att.getAttKey())) {
					column.setColumnWidth("1%");
					column.setWrapping(false);

				} else if (plusToleranceAttKey.equals(attName) || minusToleranceAttKey.equals(attName)) {
					// NULL_VALUE_PLACEHOLDER Substitution handled here
					column.setShowCriteria(LCSMeasurementsQuery.NULL_VALUE_PLACEHOLDER);
					column.setShowCriteriaNot(true);
					column.setShowCriteriaTarget(att.getAttKey());
					column.setShowCriteriaNumericCompare(true);
					column.setDecimalPrecision(PRECISION);
					if (doubleFormat.startsWith("si.Length.")) {
						column.setOutputUom(doubleFormat);
						column.setFormat(FormatHelper.MEASUREMENT_UNIT_FORMAT);
					} else {
						column.setFormat(doubleFormat);
					}
				}

				if ("image".equals(att.getAttVariableType())) {
					column.setShowFullImage(true);
				}

				repeatingColumns.add(column);
			}
		}

		columnsMap.put(REPEAT, repeatingColumns);

		return columnsMap;
	}

	/**
	 * gets the Collection of page titles for each of the Measurements pages returned
	 * 
	 * @return
	 */
	public Collection getPageHeaderCollection() {
		return pageTitles;
	}

	public Collection getSizeRun(Collection sizeRun, String sampleSize) {
		Collection runs = new ArrayList();
		Collection tempSizeRun = null;
		if (sizeRun.size() > measurementsSizeWrapLimit && (measurementsSizeWrapLimit != 0)) {
			boolean passedSampleSize = false;
			tempSizeRun = new ArrayList();
			Iterator sizeIter = sizeRun.iterator();
			String size;
			int counter = 0;
			while (sizeIter.hasNext()) {
				size = (String) sizeIter.next();
				if (counter == measurementsSizeWrapLimit) {
					runs.add(tempSizeRun);
					counter = 0;
					tempSizeRun = new ArrayList();
					tempSizeRun.add(sampleSize);
					counter++;
				}
				if (size.equals(sampleSize) && !passedSampleSize) {
					tempSizeRun.add(sampleSize);
					counter++;
					passedSampleSize = true;
					continue;
				}
				tempSizeRun.add(size);
				counter++;
			}
			if (counter <= measurementsSizeWrapLimit) {
				runs.add(tempSizeRun);
			}
		} else {
			runs.add(sizeRun);
		}

		return runs;
	}

	// ///////////////////////////////////////////////////////////////////////////

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
