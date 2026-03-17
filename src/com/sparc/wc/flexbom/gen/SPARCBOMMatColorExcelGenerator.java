package com.sparc.wc.flexbom.gen;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.client.web.ExcelGenerator;
import com.lcs.wc.client.web.FlexTypeGenerator;
import com.lcs.wc.client.web.TableColumn;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flexbom.BOMColorTableColumn;
import com.lcs.wc.flexbom.BOMMaterialTableColumn;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.report.ReportQuery;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecToSeasonLink;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.product.SPARCPDFProductSpecificationBOM2;

import wt.util.WTException;

public class SPARCBOMMatColorExcelGenerator {
	private static final Logger LOGGER = LogManager.getLogger(SPARCBOMMatColorExcelGenerator.class);
	public float materialNameWidth = (new Float(
			LCSProperties.get("com.lcs.wc.flexbom.gen.MatColorGenerator.materialNameWidth", "1.25"))).floatValue();
	private static final String BOM_VIEW = "jsp.specification.ChooseSingleSpecPage2.BomViews";
	private static final String VIEW_ID = "VIEW_ID";
	private static final String EDIT_BOM = "EDIT_BOM";
	private static final String SCBRAND = "scBrand";
	private static final String SCPRODUCTDIV = "scProductDiv";
	private static final String DELIM = "\\|~\\*~\\|";
	private static final String LINK_SCOPE = "LINK_SCOPE";
	private FlexType bomType = null;
	private String materialDescDbColName;
	private FlexType matType = null;
	String fileName;
	ArrayList<String> fileList = new ArrayList<>();

	private Map<String, Object> param = new HashMap<>();

	public SPARCBOMMatColorExcelGenerator(Map<String, Object> param) {
		if (param != null) {
			this.param.putAll(param);
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> generateBOMMatColorExcelFromMultiSpecGen(LCSProduct product, FlexSpecification spec,
			LCSSeasonMaster sMaster) {

		String bomName = null;
		FlexBOMPart bomPart = null;
		try {
			LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(sMaster);
			String division = (String) season.getValue(SCPRODUCTDIV);
			division = getDisplayValue(season.getFlexType(), division, SCPRODUCTDIV);
			String displayName = null;
			String brand = "";
			brand = (String) product.getValue(SCBRAND);
			brand = getDisplayValue(product.getFlexType(), brand, SCBRAND);
			brand = brand.replace(" ", "");
			Collection<?> bomParts;
			bomParts = getBOMParts(spec);
			Iterator<?> bomItr = bomParts.iterator();
			while (bomItr.hasNext()) {
				bomPart = (FlexBOMPart) bomItr.next();

				bomName = bomPart.getName();

				String bomViews2 = LCSProperties.get(BOM_VIEW + "." + "singleBOM" + "." + brand + "_" + division);
				if (bomViews2 != null) {
					StringTokenizer token = new StringTokenizer(bomViews2, "|~*~|");
					String item = token.nextToken();
					displayName = item.substring(item.indexOf(":") + 1).trim();
					LOGGER.debug("The BOM View as per select the Tech Pack Request Type : {}", displayName);
				} else {
					String defViewName = getDefaultViewNameForUser(bomPart);
					displayName = defViewName;
					LOGGER.debug("The Default view set as : {}", displayName);
				}

				ReportQuery reportQuery = new ReportQuery();
				Collection<FlexObject> col;
				col = reportQuery.findReportViews(bomPart.getFlexType(), null, EDIT_BOM, displayName);

				Optional<Object> columnListIdOptional = col.stream().findFirst()
						.map(column -> column.getData("COLUMNLIST.IDA2A2"));
				if (columnListIdOptional.isPresent()) {
					param.put(VIEW_ID, "OR:com.lcs.wc.report.ColumnList:" + columnListIdOptional.get());
				}

				SPARCPDFProductSpecificationBOM2 productSpecbom = new SPARCPDFProductSpecificationBOM2();

				param.put(SPARCBomDataGenerator.RAW_BOM_DATA, productSpecbom.getBOMData(bomPart));
				SPARCMatColorGenerator matcol = new SPARCMatColorGenerator();
				matcol.init(param);

				Collection<FlexObject> data = matcol.getBOMData();
			//	System.out.println("data :: " + data);

				Collection<TableColumn> tablecolumns = matcol.getTableColumns();
				LOGGER.debug("Table Columns : {}", tablecolumns);
				tablecolumns.stream().filter(tc -> tc instanceof BOMColorTableColumn).forEach(tc -> {
					tc.setLinkMethod(null);
					tc.setLinkTableIndex(null);
					tc.setLinkMethodPrefix(null);
					tc.setBgColorIndex(null);
					((BOMColorTableColumn) tc).setUseColorCell(false);
					((BOMColorTableColumn) tc).setImageIndex(null);
				});
				bomType = FlexTypeCache.getFlexTypeFromPath("BOM\\Materials");
				materialDescDbColName = bomType.getAttribute("materialDescription").getColumnName();
				matType = FlexTypeCache.getFlexTypeFromPath("Material");
				String materialDescDbColName2 = matType.getAttribute("ptcmaterialName").getColumnName();
				tablecolumns.stream().filter(tc -> tc instanceof BOMMaterialTableColumn).forEach(tc -> {
					tc.setHeaderLabel("Material");
					tc.setTableIndex("FLEXBOMLINK." + materialDescDbColName);
					tc.setDisplayed(true);
					tc.setPdfColumnWidthRatio(materialNameWidth);
					tc.setLinkMethod(null);
					tc.setLinkTableIndex(null);
					tc.setLinkMethodPrefix(null);
					((BOMMaterialTableColumn) tc).setDescriptionIndex("FLEXBOMLINK." + materialDescDbColName);

				});
				FlexTypeGenerator ftg = new FlexTypeGenerator();
				ftg.setScope(LINK_SCOPE);
				ftg.setLevel(LINK_SCOPE);
				TableColumn column3 = ftg.createTableColumn(bomPart.getFlexType().getAttribute("section"));
				SPARCExcelGenerator excelGen = new SPARCExcelGenerator();
				excelGen.setGroupColumn(column3);
				excelGen.setSpaceBetweenGroups(false);
				ClientContext context = ClientContext.getContext();
				String headerContent = "PLM Product # - " + product.getValue("scPLMProductNo")
						+ "                             Product Name - " + product.getValue("productName");
			//	System.out.println("single : data" + data);
				for (FlexObject fObj : data) {
					if(!fObj.getData("LCSMATERIAL." + materialDescDbColName2).contains("material_placeholder")) {
						fObj.setData("FLEXBOMLINK." + materialDescDbColName,
								fObj.getData("LCSMATERIAL." + materialDescDbColName2));

					}
				}

				fileName = excelGen.drawTable(data, tablecolumns, context, (bomName + "_excel report BOM"),
						headerContent);
				LOGGER.debug("headerContent == {}", headerContent);
				fileList.add(URLDecoder.decode(fileName, ExcelGenerator.defaultCharsetEncoding));
				LOGGER.debug("BOM excel file : {}", fileList);
			}
			if (!fileList.isEmpty()) {
				LOGGER.debug("BOMExcel generated successfully");
			}

		} catch (WTException | UnsupportedEncodingException e) {
			LOGGER.error("Unable to generate material color excel for BOM Part: {} due to exception : {}", bomName, e);
			e.printStackTrace();
		}

		return fileList;

	}

	@SuppressWarnings("unchecked")
	public List<String> generateBOMMatColorExcel(String productId, String specPages) {
		String bomName = null;
		try {
			String size2data = (String) param.get("size2data");
			String size1data = (String) param.get("size1data");
			String colorwaydata = (String) param.get("colorwaydata");

			String[] sizes2 = size2data.split(DELIM);
			String[] sizes1 = size1data.split(DELIM);
			String[] colorways = colorwaydata.split(DELIM);

			List<String> size1 = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(sizes1, 1, sizes1.length)));
			List<String> size2 = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(sizes2, 1, sizes2.length)));
			List<String> colorwaydt = new ArrayList<>(
					Arrays.asList(Arrays.copyOfRange(colorways, 1, colorways.length)));
			param.put("SIZES2", size2);
			param.put("SIZES1", size1);
			param.put("COLORWAYS", colorwaydt);
			param.put("USE_MAT_THUMBNAIL", "false");
			LCSProduct product = (LCSProduct) LCSQuery.findObjectById(productId);

			String displayName = null;

			String brandDivisionSuffix = getBrandDivisionSuffix((String) param.get("oid"), product);
			String[] segments = specPages.split(DELIM);

			for (String segment : segments) {
				if (segment.startsWith("BOM-:-")) {

					String bomEntry = segment.substring("BOM-:-".length());

					String id1 = bomEntry.substring("VR:com.lcs.wc.flexbom.FlexBOMPart:".length());
					FlexBOMPart bomPart = (FlexBOMPart) LCSQuery
							.findObjectById("VR:com.lcs.wc.flexbom.FlexBOMPart:" + id1);
					bomName = bomPart.getName();

					String bomViews2 = LCSProperties.get(BOM_VIEW + "." + "singleBOM" + "." + brandDivisionSuffix);
					if (bomViews2 != null) {
						StringTokenizer token = new StringTokenizer(bomViews2, "|~*~|");
						String item = token.nextToken();
						displayName = item.substring(item.indexOf(":") + 1).trim();
						LOGGER.debug("The BOM View as per select the Tech Pack Request Type : {}", displayName);
					} else {
						String defViewName = getDefaultViewNameForUser(bomPart);
						displayName = defViewName;
						LOGGER.debug("The Default view set as : {}", displayName);
					}

					ReportQuery reportQuery = new ReportQuery();
					Collection<FlexObject> col;

					col = reportQuery.findReportViews(bomPart.getFlexType(), null, EDIT_BOM, displayName);
					Optional<Object> columnListIdOptional = col.stream().findFirst()
							.map(column -> column.getData("COLUMNLIST.IDA2A2"));
					if (columnListIdOptional.isPresent()) {
						param.put(VIEW_ID, "OR:com.lcs.wc.report.ColumnList:" + columnListIdOptional.get());
					}

					SPARCPDFProductSpecificationBOM2 productSpecbom = new SPARCPDFProductSpecificationBOM2();

					param.put(SPARCBomDataGenerator.RAW_BOM_DATA, productSpecbom.getBOMData(bomPart));

					List<String> sourcesToBeSet = new ArrayList<>();
					sourcesToBeSet.add((String) param.get("source"));
					param.put(SPARCBomDataGenerator.SOURCES, sourcesToBeSet);
					SPARCMatColorGenerator matcol = new SPARCMatColorGenerator();
					matcol.init(param);

					Collection<FlexObject> data = matcol.getBOMData();

					Collection<TableColumn> tablecolumns = matcol.getTableColumns();
					LOGGER.debug("Table Columns : {} ", tablecolumns);
					bomType = FlexTypeCache.getFlexTypeFromPath("BOM\\Materials");
					materialDescDbColName = bomType.getAttribute("materialDescription").getColumnName();

					matType = FlexTypeCache.getFlexTypeFromPath("Material");
					String materialDescDbColName2 = matType.getAttribute("ptcmaterialName").getColumnName();
					tablecolumns.stream().filter(tc -> tc instanceof BOMMaterialTableColumn).forEach(tc -> {
						tc.setHeaderLabel("Material");
						tc.setTableIndex("FLEXBOMLINK." + materialDescDbColName);
						tc.setDisplayed(true);
						tc.setPdfColumnWidthRatio(materialNameWidth);
						tc.setLinkMethod(null);
						tc.setLinkTableIndex(null);
						tc.setLinkMethodPrefix(null);
						((BOMMaterialTableColumn) tc).setDescriptionIndex("FLEXBOMLINK." + materialDescDbColName);

					});

					tablecolumns.stream().filter(tc -> tc instanceof BOMColorTableColumn).forEach(tc -> {
						tc.setLinkMethod(null);
						tc.setLinkTableIndex(null);
						tc.setLinkMethodPrefix(null);
						tc.setBgColorIndex(null);
						((BOMColorTableColumn) tc).setUseColorCell(false);
						((BOMColorTableColumn) tc).setImageIndex(null);
					});

					FlexTypeGenerator ftg = new FlexTypeGenerator();
					ftg.setScope(LINK_SCOPE);
					ftg.setLevel(LINK_SCOPE);
					TableColumn column3 = ftg.createTableColumn(bomPart.getFlexType().getAttribute("section"));

					SPARCExcelGenerator excelGen = new SPARCExcelGenerator();

					excelGen.setGroupColumn(column3);
					excelGen.setSpaceBetweenGroups(false);

					ClientContext context = ClientContext.getContext();

					String headerContent = "PLM Product # - " + product.getValue("scPLMProductNo")
							+ "                             Product Name - " + product.getValue("productName");


					for (FlexObject fObj : data) {
						//System.out.println("fObj.getData LCSMATERIAL materialDescDbColName2) : data" + fObj.getData("LCSMATERIAL." + materialDescDbColName2));
						if(!fObj.getData("LCSMATERIAL." + materialDescDbColName2).contains("material_placeholder")) {
							fObj.setData("FLEXBOMLINK." + materialDescDbColName,
									fObj.getData("LCSMATERIAL." + materialDescDbColName2));
						}
					}

					fileName = excelGen.drawTable(data, tablecolumns, context, (bomName + "_excel report BOM"),
							headerContent);
					LOGGER.debug("headerContent == {}", headerContent);
					fileList.add(URLDecoder.decode(fileName, ExcelGenerator.defaultCharsetEncoding));
					LOGGER.debug("BOM excel file : {}", fileList);
				}

			}
			if (!fileList.isEmpty()) {
				LOGGER.debug("BOMExcel generated successfully");
			}
		} catch (WTException | UnsupportedEncodingException e) {
			LOGGER.error("Unable to generate material color excel for BOM Part: {} due to exception : {}", bomName, e);
			e.printStackTrace();
		}

		return fileList;

	}

	private String getBrandDivisionSuffix(String oid, LCSProduct product) throws WTException {
		String division = "Apparel";
		String brand = (String) product.getValue(SCBRAND);
		brand = getDisplayValue(product.getFlexType(), brand, SCBRAND);
		brand = brand.replaceAll("\\s+", "");
		if (FormatHelper.hasContent(oid) && oid.indexOf("com.lcs.wc.specification.FlexSpecification") > -1) {

			division = product.getFlexType().getFullNameDisplay(true).contains(division) ? division : "Footwear";
		} else {
			FlexSpecToSeasonLink fstsl = (FlexSpecToSeasonLink) LCSQuery.findObjectById(oid);
			LCSSeason season = VersionHelper.latestIterationOf(fstsl.getSeasonMaster());
			division = (String) season.getValue(SCPRODUCTDIV);
			division = getDisplayValue(season.getFlexType(), division, SCPRODUCTDIV);
			division = division.replaceAll("\\s+", "");
		}
		return brand.concat("_").concat(division);
	}

	private String getDefaultViewNameForUser(FlexBOMPart bomPart) {
		String defViewName = "";
		FlexType type = null;
		String activity = EDIT_BOM;
		type = bomPart.getFlexType();
		String defaultViewId;
		try {
			ClientContext lcsContext = ClientContext.getContext();
			defaultViewId = lcsContext.viewCache.getDefaultViewId(FormatHelper.getObjectId(type), activity);

			if (FormatHelper.hasContent(defaultViewId)) {
				defViewName = lcsContext.viewCache.getView(defaultViewId).getDisplayName();
			}
		} catch (WTException e) {
			e.printStackTrace();
		}

		return defViewName;
	}

	public static String getDisplayValue(FlexType type, String keyValue, String attKey) throws WTException {
		String attValue = "";
		if (FormatHelper.hasContentAllowZero(keyValue)) {
			FlexTypeAttribute flexAtt = type.getAttribute(attKey);
			AttributeValueList attList = flexAtt.getAttValueList();
			attValue = attList.getValue(keyValue, Locale.getDefault());
		}
		return attValue;
	}

	private Collection<?> getBOMParts(FlexSpecification spec) throws WTException {

		return (Collection<?>) (FlexSpecQuery.getSpecComponents(spec, "BOM") != null
				? FlexSpecQuery.getSpecComponents(spec, "BOM")
				: "");
	}

}
