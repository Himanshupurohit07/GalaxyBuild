package com.sparc.wc.shareFile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.report.ColumnList;
import com.lcs.wc.report.ReportQuery;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.MultiCharDelimStringTokenizer;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.product.SPARCPDFMultiSpecGenerator;

import wt.org.WTUser;
import wt.util.WTException;

public class SPARCTechPackShareFileUtil {
	private static final Logger logger = LogManager.getLogger(SPARCTechPackShareFileUtil.class.getName());
	private static String specPagesStr = "jsp.specification.ChooseSingleSpecPage2.SpecPages";
	private static String pageOptionsStr = "jsp.specification.ChooseSingleSpecPage2.pageOptions";
	private static String numColorwaysPerPageStr = "jsp.specification.ChooseSingleSpecPage2.numColorwaysPerPage";
	private static String showColorSwatchStr = "jsp.specification.ChooseSingleSpecPage2.showColorSwatch";
	private static String showMaterialThumbStr = "jsp.specification.ChooseSingleSpecPage2.showMaterialThumb";
	private static String useSize1Size2Str = "jsp.specification.ChooseSingleSpecPage2.useSize1Size2";
	private static String bomViews_SingleBOMStr = "jsp.specification.ChooseSingleSpecPage2.BomViews.singleBOM";
	private static String measureUOMStr = "jsp.specification.ChooseSingleSpecPage2.MeasureUOM";
	private static String numSizesPerPageStr = "jsp.specification.ChooseSingleSpecPage2.numSizesPerPage";
	private static String childSpecsStr = "jsp.specification.ChooseSingleSpecPage2.includeChildSpecs";
	private static final String BRAND_KEY = LCSProperties
			.get("com.sparc.wc.product.SPARCExternalPartnerTechPackGenPlugin.BrandKey");
	private static String falseStr = "false";
	private static final Map<String, String> specPages;
	private static final Map<String, String> pageOptions;
	private static final Map<String, String> numColorwaysPerPage;
	private static final Map<String, String> showColorSwatch;
	private static final Map<String, String> showMaterialThumb;
	private static final Map<String, String> useSize1Size2;
	private static final Map<String, String> bomViews_SingleBOM;
	private static final Map<String, String> measureUOM;
	private static final Map<String, String> numSizesPerPage;
	private static final Map<String, String> childSpecs;
	private static String BIZOBJ_BRAND_KEY;
	private static String BIZOBJ_EXTPARTNER_KEY;
	private static String BIZOBJ_DIV_KEY;
	private static String BIZOBJ_EXTPARTNER_TYPE;
	private static String BIZOBJ_PATH_KEY;
	private static String DEVSEASON_KEY;
	private static String DEVYEAR__KEY;
	private static String DIVISION_KEY;
	static {
		specPages = LCSProperties.getPropertyEntriesStartWith(specPagesStr);
		pageOptions = LCSProperties.getPropertyEntriesStartWith(pageOptionsStr);
		numColorwaysPerPage = LCSProperties.getPropertyEntriesStartWith(numColorwaysPerPageStr);
		showColorSwatch = LCSProperties.getPropertyEntriesStartWith(showColorSwatchStr);
		showMaterialThumb = LCSProperties.getPropertyEntriesStartWith(showMaterialThumbStr);
		// checkAllColorways =
		// LCSProperties.getPropertyEntriesStartWith(checkAllColorwaysStr);
		// checkAllSizes = LCSProperties.getPropertyEntriesStartWith(checkAllSizesStr);
		// checkAllDestinations =
		// LCSProperties.getPropertyEntriesStartWith(checkAllDestinationsStr);
		useSize1Size2 = LCSProperties.getPropertyEntriesStartWith(useSize1Size2Str);
		bomViews_SingleBOM = LCSProperties.getPropertyEntriesStartWith(bomViews_SingleBOMStr);
		measureUOM = LCSProperties.getPropertyEntriesStartWith(measureUOMStr);
		numSizesPerPage = LCSProperties.getPropertyEntriesStartWith(numSizesPerPageStr);
		childSpecs = LCSProperties.getPropertyEntriesStartWith(childSpecsStr);
		BIZOBJ_BRAND_KEY = LCSProperties.get("com.sparc.wc.product.SPARCExternalPartnerTechPackGenPlugin.BrandKey",
				"scBrand");
		BIZOBJ_EXTPARTNER_KEY = LCSProperties.get(
				"com.sparc.wc.product.SPARCExternalPartnerTechPackGenPlugin.ExternalPartenersKey", "scExternalPartner");
		BIZOBJ_DIV_KEY = LCSProperties.get("com.sparc.wc.product.SPARCExternalPartnerTechPackGenPlugin.divisionKey",
				"scProductDivision");
		BIZOBJ_EXTPARTNER_TYPE = LCSProperties.get(
				"com.sparc.wc.businessObject.SPARCTechPackShareUtil.externalPArtnerType",
				"Business Object\\scBrandPartners");
		BIZOBJ_PATH_KEY = LCSProperties.get("com.sparc.wc.businessObject.SPARCTechPackShareUtil.pathKey",
				"scShareFilePath");
		DEVSEASON_KEY = LCSProperties.get("com.sparc.wc.season.SPARCTechPackShareUtil.devSeason", "seasonType");
		DEVYEAR__KEY = LCSProperties.get("com.sparc.wc.season.SPARCTechPackShareUtil.devYear", "year");
		DIVISION_KEY = LCSProperties.get("com.sparc.wc.season.SPARCTechPackShareUtil.divisionKey", "scProductDiv");
	}

	public static String getShareFilePath(String brand, String extPartner, LCSSeason season) throws WTException {
		String path = "";

		try {
			String division = (String) season.getValue(DIVISION_KEY);
			FlexType fType = FlexTypeCache.getFlexTypeFromPath(BIZOBJ_EXTPARTNER_TYPE);
			String brandCol = fType.getAttribute(BIZOBJ_BRAND_KEY).getColumnName();
			String partnerCol = fType.getAttribute(BIZOBJ_EXTPARTNER_KEY).getColumnName();
			String divCol = fType.getAttribute(BIZOBJ_DIV_KEY).getColumnName();
			PreparedQueryStatement stmt = new PreparedQueryStatement();
			stmt.appendFromTable(LCSLifecycleManaged.class);
			stmt.appendSelectColumn(
					new QueryColumn("LCSLifecycleManaged", fType.getAttribute(BIZOBJ_PATH_KEY).getColumnName()));
			stmt.appendCriteria(new Criteria("LCSLifecycleManaged", brandCol, brand, "="));
			stmt.appendAndIfNeeded();
			stmt.appendCriteria(new Criteria("LCSLifecycleManaged", divCol, division, "="));
			stmt.appendAndIfNeeded();
			stmt.appendCriteria(new Criteria("LCSLifecycleManaged", partnerCol, extPartner, "="));
			Vector<FlexObject> res = LCSQuery.runDirectQuery(stmt).getResults();
			if (!res.isEmpty()) {
				Iterator<FlexObject> itr = res.iterator();

				while (itr.hasNext()) {
					FlexObject fObj = itr.next();
					if (fObj != null) {
						path = fObj.getString(
								"LCSLIFECYCLEMANAGED." + fType.getAttribute(BIZOBJ_PATH_KEY).getColumnName());
						if (FormatHelper.hasContent(path)) {
							String devSeason = season.getFlexType().getAttribute(DEVSEASON_KEY)
									.getDisplayValue((String) season.getValue(DEVSEASON_KEY));
							if (FormatHelper.hasContent(devSeason))
								devSeason = devSeason.replace("/", " ");

							String devYear = season.getFlexType().getAttribute(DEVYEAR__KEY)
									.getDisplayValue((String) season.getValue(DEVYEAR__KEY));

							path = path + "/Tech Packs/" + devYear + "/" + devSeason;
							return path;
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return path;
	}

	public String getViewsSelect(LCSProduct product, FlexSpecification spec, String postFix) throws WTException {
		String bomViews_SingleBOMVal = bomViews_SingleBOM.get(bomViews_SingleBOMStr + postFix);
		StringTokenizer token = new MultiCharDelimStringTokenizer(bomViews_SingleBOMVal, MOAHelper.DELIM);
		String item = token.nextToken();
		String view = item.substring(item.indexOf(":") + 1).trim();
		StringBuilder selectViews = new StringBuilder();
		String Delim = "|~*~|";
		String delim2 = "-:-";
		ReportQuery q = new ReportQuery();

		WTUser principle = ClientContext.getContext().getUser();
		Collection<WTUser> userCol = new ArrayList<WTUser>();
		userCol.add(principle);

		Collection<FlexBOMPart> bomParts = (new LCSFlexBOMQuery()).findBOMPartsForOwner(product, null, null, spec);
		if (bomParts != null && bomParts.size() > 0) {
			Iterator<FlexBOMPart> i = bomParts.iterator();
			FlexType bomFType2 = null;
			while (i.hasNext()) {
				FlexBOMPart bom = (FlexBOMPart) i.next();
				FlexType bomFType = bom.getFlexType();
				if (bomFType2 != null && bomFType == bomFType2) {
					continue;
				}

				Collection<FlexObject> results = new ArrayList<FlexObject>();
				PreparedQueryStatement statement = new PreparedQueryStatement();
				statement.appendFromTable(ColumnList.class, "COLUMNLIST");
				statement.appendSelectColumn(
						new QueryColumn("COLUMNLIST", ColumnList.class, "thePersistInfo.theObjectIdentifier.id"));
				statement.appendSelectColumn(new QueryColumn("COLUMNLIST", ColumnList.class, "persistedType"));
				statement.appendSelectColumn(
						new QueryColumn("COLUMNLIST", ColumnList.class, "principalReference.key.id"));
				statement.appendSelectColumn(new QueryColumn("COLUMNLIST", ColumnList.class, "displayName"));
				statement.appendSelectColumn(new QueryColumn("COLUMNLIST", ColumnList.class, "relevantActivity"));
				statement.appendAndIfNeeded();// 319
				statement.appendCriteria(
						new Criteria(new QueryColumn("COLUMNLIST", ColumnList.class, "displayName"), "?", "="), view);
				statement.appendAndIfNeeded();// 311
				statement.appendCriteria(
						new Criteria(new QueryColumn("COLUMNLIST", ColumnList.class, "relevantActivity"), "?", "="),
						"EDIT_BOM");
				logger.debug("columnList query == {} ", statement.getSqlStatement());
				results.addAll(q.runDirectQuery(statement).getResults());
				Iterator<FlexObject> fItr = results.iterator();
				String columnList = "";
				String longConst = "";
				while (fItr.hasNext()) {
					FlexObject fObj = fItr.next();
					columnList = (String) fObj.get("COLUMNLIST.IDA2A2");
					// "COLUMNLIST.PERSISTEDTYPE - com.lcs.wc.flexbom.FlexBOMPart|343972"
					longConst = (String) fObj.get("COLUMNLIST.PERSISTEDTYPE");
					longConst = longConst.substring(longConst.indexOf('|') + 1, longConst.length());

				}
				if (FormatHelper.hasContent(columnList)) {

					Collection<String> sections = bomFType.getAttribute("section").getAttValueList()
							.getSelectableKeys(null, true);
					Iterator<String> sectionItr = sections.iterator();
					while (sectionItr.hasNext()) {

						{
							String section = (String) sectionItr.next();
							selectViews.append(Delim + "singleBOM" + delim2
									+ "VR:com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinition:" + longConst
									+ delim2 + section + delim2 + "ColumnList:" + columnList);
							selectViews.append(Delim + "sizingReport" + delim2
									+ "VR:com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinition:" + longConst
									+ delim2 + section + delim2 + "ColumnList:" + columnList);
							selectViews.append(Delim + "colorwayReport" + delim2
									+ "VR:com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinition:" + longConst
									+ delim2 + section + delim2 + "ColumnList:" + columnList);
						}
					}
				}
				bomFType2 = bomFType;
			}
		}
		return selectViews.toString();
	}

	public Map<String, String> createPDFParams(LCSProductSeasonLink psLink, FlexSpecification spec) {
		try {
			Map<String, String> params = new HashMap<>();
			LCSSeason season = VersionHelper.latestIterationOf(psLink.getSeasonMaster());
			LCSProduct product = LCSProductQuery.getProductVersion("" + psLink.getProductMasterId(), "A");
			String brand = (String) product.getValue(BRAND_KEY);
			String brandDisplay = getDisplayValue(product.getFlexType(), brand, BRAND_KEY);
			brandDisplay = brandDisplay.replaceAll(" ", "");
			String division = (String) season.getValue(DIVISION_KEY);
			division = getDisplayValue(season.getFlexType(), division, DIVISION_KEY);
			division = division.replaceAll(" ", "");
			String postFix = "." + brandDisplay + "_" + division;
			logger.debug("TechPack-property search for : {}", postFix);
			String specPagesVal = specPages.get(specPagesStr + postFix);
			String pageOptionsVal = pageOptions.get(pageOptionsStr + postFix);
			String measureUOMVal = measureUOM.get(measureUOMStr + postFix);
			String numColorwaysPerPageVal = numColorwaysPerPage.get(numColorwaysPerPageStr + postFix);
			String numSizesPerPageVal = numSizesPerPage.get(numSizesPerPageStr + postFix);
			String childSpecsVal = childSpecs.get(childSpecsStr + postFix);
			String showColorSwatchVal = showColorSwatch.get(showColorSwatchStr + postFix);
			String showMaterialThumbVal = showMaterialThumb.get(showMaterialThumbStr + postFix);
			// String checkAllColorwaysVal = checkAllColorways.get(checkAllColorwaysStr +
			// postFix);
			// String checkAllSizesVal = checkAllSizes.get(checkAllSizesStr + postFix);
			// String checkAllDestinationsVal =
			// checkAllDestinations.get(checkAllDestinationsStr + postFix);
			String useSize1Size2Val = useSize1Size2.get(useSize1Size2Str + postFix);
			SPARCPDFMultiSpecGenerator gen = new SPARCPDFMultiSpecGenerator();
			params.put("showThumbs", falseStr);
			params.put("showMaterialThumbnail", showMaterialThumbVal);
			params.put("includeMeasurementsOwnerSizes", "true");
			params.put("whatIfCostSheets", falseStr);
			params.put("placeholders", falseStr);
			FlexType productType = season.getProductType();
			String pTypeName = productType.getFullName(true);
			params.put("pTypeName", pTypeName);
			params.put("includeMarkedupImagesContent", "true");
			params.put("pageOptions", pageOptionsVal.replaceAll(",", "|~*~|"));
			params.put("uom", measureUOMVal);
			params.put("includeChildSpecs", childSpecsVal);
			FlexType flexTypeId = FlexTypeCache
					.getFlexTypeFromPath("Document\\Generated Tech Pack\\scGeneratedTechPack" + brandDisplay);
			String DocTypeId = FormatHelper.getObjectId(flexTypeId);
			params.put("vaultDocumentTypeId", DocTypeId);

			String viewSelectToBeSet = getViewsSelect(product, spec, postFix);

			params.put("viewSelect", viewSelectToBeSet);
			params.put("documentVault", "true");
			params.put("exportedIndentedBOM", falseStr);

			String specPagesToBeSet = getSpecPagesToBeSet(specPagesVal);

			params.put("specPages", specPagesToBeSet); ///////// to be created from components specPages=Header
														///////// Page|~*~|Colorways|~*~|IMAGES_PAGE:artwork|~*~|IMAGES_PAGE:scBOM|~*~|IMAGES_PAGE:backsketch|~*~|IMAGES_PAGE:colorwayimage|~*~|IMAGES_PAGE:constructiondetail|~*~|IMAGES_PAGE:coverpage|~*~|IMAGES_PAGE:croquissketch|~*~|IMAGES_PAGE:designdetail|~*~|IMAGES_PAGE:scdesignpack|~*~|IMAGES_PAGE:fitphotocorrections|~*~|IMAGES_PAGE:foldingpackingshipping|~*~|IMAGES_PAGE:frontsketch|~*~|IMAGES_PAGE:inspirationreferencephoto|~*~|IMAGES_PAGE:measurementdetail|~*~|IMAGES_PAGE:quickSpec|~*~|IMAGES_PAGE:trimlabelplacement|~*~|IMAGES_PAGE:washimage|~*~|BOM|~*~|MEASUREMENT
			params.put("partFilter", "System Default");
			long millisStart = Calendar.getInstance().getTimeInMillis();
			params.put("timestamp", String.valueOf(millisStart));
			if (useSize1Size2Val.contains("size2")) {
				String size2Data = gen.getSizes2(spec, product);
				params.put("size2data", size2Data);
			}
			params.put("useLandscape", "true");
			params.put("availEPMDOCDocs", "");
			params.put("includeCADDocuments", falseStr);
			params.put("showColorSwatches", showColorSwatchVal);
			String colorwayData = gen.getColorways(spec, product, season);
			params.put("colorwaydata", colorwayData);
			String sourceData = gen.getSources(spec, product);
			params.put("source", sourceData);
			params.put("useSize1Size2", useSize1Size2Val);
			params.put("activeCostSheets", falseStr);
			params.put("requestLocale", "en_US");
			params.put("numSizesPerPage", numSizesPerPageVal);
			params.put("includeAllParts", falseStr);
			String oid = FormatHelper.getObjectId(season);
			params.put("oid", oid);
			params.put("includeSecondaryContent", falseStr);
			params.put("updatingProductSizeCategory", falseStr);
			String oids = "VR:com.lcs.wc.specification.FlexSpecification:" + spec.getBranchIdentifier();
			params.put("oids", oids);
			params.put("layout", "freezepane");
			params.put("updatingRFQResponse", falseStr);
			params.put("pdf", falseStr);
			params.put("numColorwaysPerPage", numColorwaysPerPageVal);
			String size1Data = gen.getSizes1(spec, product);
			params.put("size1data", size1Data);
			params.put("availDocs", "");
			return params;
		} catch (Exception ex) {
			logger.error("Exception creating spec params for PDF {}", (Object) ex.getStackTrace());
			ex.printStackTrace();
			return null;
		}
	}

	private String getSpecPagesToBeSet(String specPagesVal) {
		String specPageStr = "";
		if (specPagesVal.contains("Header Page")) {
			specPageStr = specPageStr + "Header Page";
		}
		if (specPagesVal.contains("Colorways"))
			specPageStr = specPageStr + "|~*~|Colorways";

		if (specPagesVal.contains("Images Page"))
			specPageStr = specPageStr
					+ "|~*~|IMAGES_PAGE:artwork|~*~|IMAGES_PAGE:scBOM|~*~|IMAGES_PAGE:backsketch|~*~|IMAGES_PAGE:colorwayimage|~*~|IMAGES_PAGE:constructiondetail|~*~|IMAGES_PAGE:coverpage|~*~|IMAGES_PAGE:croquissketch|~*~|IMAGES_PAGE:designdetail|~*~|IMAGES_PAGE:scdesignpack|~*~|IMAGES_PAGE:fitphotocorrections|~*~|IMAGES_PAGE:foldingpackingshipping|~*~|IMAGES_PAGE:frontsketch|~*~|IMAGES_PAGE:inspirationreferencephoto|~*~|IMAGES_PAGE:measurementdetail|~*~|IMAGES_PAGE:quickSpec|~*~|IMAGES_PAGE:trimlabelplacement|~*~|IMAGES_PAGE:washimage";

		if (specPagesVal.contains("BOM"))
			specPageStr = specPageStr + "|~*~|BOM";

		if (specPagesVal.contains("Measurements"))
			specPageStr = specPageStr + "|~*~|MEASUREMENT";

		if (specPagesVal.contains("Sample"))
			specPageStr = specPageStr + "|~*~|SAMPLE";

		if (specPageStr.indexOf(0) == '|') {
			specPageStr = specPageStr.substring(5, specPageStr.length() - 1);

		}

		return specPageStr;

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
}
