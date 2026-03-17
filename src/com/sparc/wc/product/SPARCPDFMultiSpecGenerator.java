/*
 * PDFMultiSpecGenerator.java
 *
 * Created on December 1, 2006, 2:53 PM
 */

package com.sparc.wc.product;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.client.web.pdf.PDFPageSize;
import com.lcs.wc.construction.LCSConstructionQuery;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.document.FileRenamer;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.document.LCSDocumentClientModel;
import com.lcs.wc.document.LCSDocumentHelper;
import com.lcs.wc.document.LCSDocumentQuery;
import com.lcs.wc.document.ZipGenerator;
import com.lcs.wc.epmstruct.LCSEPMDocumentQuery;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTypeQueryStatement;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.measurements.LCSMeasurementsQuery;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.partstruct.FlexPartToSpecLinkQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.product.PDFProductSpecificationMeasurements2;
import com.lcs.wc.product.ProductDestinationQuery;
import com.lcs.wc.product.ProductHeaderQuery;
import com.lcs.wc.revise.ReviseLogic;
import com.lcs.wc.sample.LCSSample;
import com.lcs.wc.sample.LCSSampleQuery;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.sourcing.OrderConfirmation;
import com.lcs.wc.sourcing.OrderConfirmationMaster;
import com.lcs.wc.sourcing.RFQRequest;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.DeleteFileHelper;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.SortHelper;
import com.lcs.wc.util.VersionHelper;
import com.lcs.wc.util.ZipHelper;
import com.sparc.wc.flexbom.gen.SPARCBOMMatColorExcelGenerator;
import com.sparc.wc.shareFile.SPARCTechPackShareFileUtil;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.doc.WTDocumentMaster;
import wt.util.WTException;

/**
 *
 * @author Chuck
 */
public class SPARCPDFMultiSpecGenerator {
	private static final Logger LOGGER = LogManager.getLogger(SPARCPDFMultiSpecGenerator.class.getName());
	public static final String INCLUDETP_KEY = LCSProperties
			.get("com.lcs.wc.product.SPARCPDFMultiSpecGenerator.includeOnTPKey");
	private static final String BRAND_KEY = LCSProperties
			.get("com.sparc.wc.product.SPARCExternalPartnerTechPackGenPlugin.BrandKey");
	private static final String DIVISION_KEY = LCSProperties
			.get("com.sparc.wc.season.SPARCTechPackShareUtil.divisionKey", "scProductDiv");

	public static String DEFAULT_PAPER_SIZE = LCSProperties
			.get("com.lcs.wc.product.PDFMultiSpecGenerator.defaultPaperSize", "LETTER");

	public static String SPEC_PAGES = "specPages";

	public static String COLORWAYS_DATA = "colorwaydata";

	public static String SIZE1_DATA = "size1data";

	public static String SIZE2_DATA = "size2data";

	public static String DESTINATION_DATA = "destinationdata";

	public static String PAGE_OPTIONS = "pageOptions";

	public static String SELECTED_VIEWS = "viewSelect";

	public static String COLORWAYS_PER_PAGE = "numColorwaysPerPage";

	public static String SIZES_PER_PAGE = "numSizesPerPage";

	public static String USE_SIZE1_SIZE2 = "useSize1Size2";

	public static String SHOW_COLOR_SWATCHES = "showColorSwatches";

	public static String SHOW_MATERIAL_THUMBNAIL = "showMaterialThumbnail";

	public static String PAPER_SIZE = "paperSize";

	public static String USE_LANDSCAPE = "useLandscape";

	public static String UOM = "uom";

	final static String AVAIL_DOCS = "availDocs";

	final static String SECONDARY = "includeSecondaryContent";

	final static String SPECIFICATION = "OR:com.lcs.wc.specification.FlexSpecification:";

	final static String SPECIFICATIONMASTER = "OR:com.lcs.wc.specification.FlexSpecMaster:";

	public static String SEASONMASTER_ID = "SEASONMASTER_ID";

	final public static String NAME = "name";

	public static String CDELIM = SPARCPDFProductSpecificationGenerator2.TYPE_COMP_DELIM;

	/** Creates a new instance of PDFMultiSpecGenerator */
	public SPARCPDFMultiSpecGenerator() {
		LOGGER.debug("!!!!!instance of SPARCPDFMultiSpecGenerator");
	}

	ProductHeaderQuery phq = new ProductHeaderQuery();

	protected SPARCPDFProductSpecificationGenerator2 getGenerator(FlexSpecification spec, LCSSeasonMaster seasonMaster)
			throws WTException {
		Object[] arguments = { spec, seasonMaster };
		com.sparc.wc.product.SPARCPDFProductSpecificationGenerator2 ppsg = SPARCPDFProductSpecificationGenerator2
				.getOverrideInstance(arguments);
		return ppsg;
	}

	protected void setGeneratorParams(SPARCPDFProductSpecificationGenerator2 ppsg, FlexSpecification spec,
			Collection specPages, Map params, String outputFolder) throws WTException {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("ppsg: " + ppsg);
			LOGGER.debug("spec: " + spec);
			LOGGER.debug("specPages: " + specPages);
			LOGGER.debug("params: " + params);
		}
		LOGGER.debug("----------------------------------------------");

		String sourceNumber = "";
		String colorways = "";
		String sizes1 = "";
		String sizes2 = "";
		String destinations = "";

		LCSSeason season = null;
		String oid = (String) params.get("oid");

		LCSProduct product = (LCSProduct) VersionHelper.getVersion(spec.getSpecOwner(), "A");

		if (FormatHelper.hasContent((String) params.get("seasonId"))) {
			season = (LCSSeason) LCSQuery.findObjectById((String) params.get("seasonId"));
		} else if (FormatHelper.hasContent(oid) && oid.indexOf("LCSSeason") > -1) {
			season = (LCSSeason) LCSQuery.findObjectById(oid);
		}

		String brand = (String) product.getValue(BRAND_KEY);
		String brandDisplay = getDisplayValue(product.getFlexType(), brand, BRAND_KEY);
		brandDisplay = brandDisplay.replaceAll(" ", "");
		String division = (String) season.getValue(DIVISION_KEY);
		division = getDisplayValue(season.getFlexType(), division, DIVISION_KEY);
		division = division.replaceAll(" ", "");
		String postFix = "." + brandDisplay + "_" + division;
		String viewSelectStr = new SPARCTechPackShareFileUtil().getViewsSelect(product, spec, postFix);
		params.put("viewSelect", viewSelectStr);
		sourceNumber = FormatHelper.getNumericObjectIdFromObject(spec.getSpecSource());
		ppsg.setSources(sourceNumber);

		colorways = getColorways(spec, product, season);
		ppsg.setColorways(colorways);

		sizes1 = getSizes1(spec, product);
		ppsg.setSize1Sizes(sizes1);

		sizes2 = getSizes2(spec, product);
		ppsg.setSize2Sizes(sizes2);

		destinations = getDestinations(spec, product);
		ppsg.setDestinations(destinations);

		Collection<String> components = getComponentsForSpec(spec, product, specPages, params);
		ppsg.setPages(components);

		ppsg.setPageOptions((String) params.get(PAGE_OPTIONS));

		ppsg.setColorwaysPerPage((String) params.get(COLORWAYS_PER_PAGE));

		ppsg.setSizesPerPage((String) params.get(SIZES_PER_PAGE));

		Collection<?> scgs = SizingQuery.findProductSizeCategoriesForProduct(product).getResults();
		String sizeCatId;
		if (!scgs.isEmpty()) {
			FlexObject firstElement = (FlexObject) scgs.iterator().next(); // only concerned with the first sizing
																			// category
			sizeCatId = "OR:com.lcs.wc.sizing.ProductSizeCategory:"
					+ firstElement.getData("PRODUCTSIZECATEGORY.IDA2A2");
		} else {
			sizeCatId = "";
		}

		ppsg.setProductSizeCatId(sizeCatId);

		ppsg.setShowColorSwatch((String) params.get(SHOW_COLOR_SWATCHES));
		ppsg.setShowMatThumbnail((String) params.get(SHOW_MATERIAL_THUMBNAIL));
		ppsg.setUseSize1Size2((String) params.get(USE_SIZE1_SIZE2));

		if (FormatHelper.hasContent((String) params.get(PAPER_SIZE))) {
			ppsg.setPageSize(PDFPageSize.getPageSize((String) params.get(PAPER_SIZE)));
		} else {
			ppsg.setPageSize(PDFPageSize.getPageSize(DEFAULT_PAPER_SIZE));
		}

		if ("false".equals((String) params.get(USE_LANDSCAPE))) {
			ppsg.setLandscape(false);
		} else {
			ppsg.setLandscape(true);
		}

		HashMap params2 = new HashMap();
		params2.put(PDFProductSpecificationMeasurements2.UOM, params.get(UOM));
		if (season != null) {
			params2.put(SEASONMASTER_ID, FormatHelper.getObjectId((LCSSeasonMaster) season.getMaster()));
		}

		boolean includeCADDocuments = "true".equals((String) params.get("includeCADDocuments"));
		if (includeCADDocuments) {
			Collection<FlexObject> linkData = new LCSEPMDocumentQuery().findAssociatedEPMDocumentsByCriteria(spec,
					new HashMap<String, String>());
			List<String> specLinks = new ArrayList<String>();

			Iterator<FlexObject> linkIter = linkData.iterator();
			while (linkIter.hasNext()) {
				FlexObject flexObject = (FlexObject) linkIter.next();
				specLinks.add("OR:com.lcs.wc.epmstruct.FlexEPMDocToSpecLink:"
						+ flexObject.getString("FLEXEPMDOCTOSPECLINK.IDA2A2"));
			}
			params2.put("SPEC_CAD_DOCS", specLinks);
		}

		boolean includeParts = "true".equals((String) params.get("includeAllParts"));
		if (includeParts) {
			Collection<FlexObject> partLinkData = new FlexPartToSpecLinkQuery().findAssociatedPartsByCriteria(spec,
					new HashMap<String, String>());
			List<String> partToSpecLinks = new ArrayList<String>();

			Iterator<FlexObject> linkIter = partLinkData.iterator();
			while (linkIter.hasNext()) {
				FlexObject flexObject = (FlexObject) linkIter.next();
				partToSpecLinks.add("OR:com.lcs.wc.partstruct.FlexPartToSpecLink:"
						+ flexObject.getString("FlexPartToSpecLink.IDA2A2"));
			}
			params2.put("SPEC_PARTS", partToSpecLinks);
		}

		params2.put(SPARCPDFProductSpecificationGenerator2.INCLUDE_BOM_OWNER_VARIATIONS,
				(String) params.get("includeBOMOwnerVariations"));
		params2.put(SPARCPDFProductSpecificationGenerator2.INCLUDE_MEASUREMENTS_OWNER_SIZES,
				(String) params.get("includeMeasurementsOwnerSizes"));
		params2.put(SPARCPDFProductSpecificationGenerator2.INCLUDE_MARKEDUP_IMAGES_CONTENT,
				(String) params.get("includeMarkedupImagesContent"));

		ppsg.setShowIndentedBOM((String) params.get("exportedIndentedBOM"));

		ppsg.setPartFilter((String) params.get("partFilter"));

		params2.put("specPages", params.get("specPages"));

		ppsg.setAddlParams(params2);

		ppsg.setSpecPageOptions(components);

		ppsg.setShowChangeSince((String) params.get("showChangeSince"));

		ppsg.setBOMSectionViews((String) params.get("viewSelect"));
		ppsg.outputLocation = outputFolder;

	}

	public String createZipForSpecs(Collection specIds, Map params, String outputFolder) throws WTException {

		LOGGER.debug("----------------------------------------------");
		LOGGER.debug("PDFMultiSpecGenerator - createZipForSpecs");
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("params: " + params);
			LOGGER.debug("specIds: " + specIds);
		}
		LOGGER.debug("----------------------------------------------");

		String seasonId = (String) params.get("oid");
		LCSSeason season = null;
		if (FormatHelper.hasContent(seasonId)) {
			if (seasonId.indexOf("RFQRequest:") > -1) {
				if (params.get("currentSeasonId") != null
						&& FormatHelper.hasContent((String) params.get("currentSeasonId"))) {
					season = (LCSSeason) LCSQuery
							.findObjectById("VR:com.lcs.wc.season.LCSSeason:" + (String) params.get("currentSeasonId"));
				} else {
					RFQRequest rfq = (RFQRequest) LCSQuery.findObjectById(seasonId);
					if (rfq.getValue("seasonReference") != null) {
						season = (LCSSeason) rfq.getValue("seasonReference");
					}
				}
			} else if (seasonId.indexOf("OrderConfirmation:") > -1) {
				if (params.get("currentSeasonId") != null
						&& FormatHelper.hasContent((String) params.get("currentSeasonId"))) {
					season = (LCSSeason) LCSQuery
							.findObjectById("VR:com.lcs.wc.season.LCSSeason:" + (String) params.get("currentSeasonId"));
				} else {
					OrderConfirmation oc = (OrderConfirmation) LCSQuery.findObjectById(seasonId);
					OrderConfirmationMaster ocMaster = (OrderConfirmationMaster) oc.getMaster();
					if (ocMaster.getSeasonMaster() != null) {
						season = (LCSSeason) VersionHelper.latestIterationOf(ocMaster.getSeasonMaster());
					}
				}
			} else {
				season = (LCSSeason) LCSQuery.findObjectById(seasonId);
			}
		}

		LCSSeasonMaster sMaster = null;
		if (season != null) {
			sMaster = (LCSSeasonMaster) season.getMaster();
		}

		String specPagesStr = (String) params.get(SPEC_PAGES);
		Collection<String> specPages = null;
		if (!FormatHelper.hasContent(specPagesStr)) {
			specPages = new ArrayList<String>();
		} else {
			specPages = MOAHelper.getMOACollection(specPagesStr);
		}
		specPagesStr = (String) params.get(AVAIL_DOCS);
		ArrayList<String> flexTypes = null;
		if (FormatHelper.hasContent(specPagesStr)) {
			Collection<String> availDocs = MOAHelper.getMOACollection(specPagesStr);
			availDocs = LCSDocumentQuery.getAvailDocTypes(availDocs);
			flexTypes = new ArrayList<String>(availDocs.size());
			FlexType flexType = null;
			for (Iterator<?> it = availDocs.iterator(); it.hasNext();) {
				specPagesStr = (String) it.next();
				flexType = FlexTypeCache.getFlexTypeFromPath(specPagesStr);
				specPagesStr = FormatHelper.getNumericFromOid(FormatHelper.getObjectId(flexType));
				flexTypes.add(specPagesStr);
			}
		} else {
			flexTypes = new ArrayList<String>();
		}
		specPagesStr = (String) params.get(SECONDARY);
		boolean secondaryContent = "true".equals(specPagesStr);
		specPagesStr = (String) params.get("includeChildSpecs");
		boolean includeChildSpecs = "true".equals(specPagesStr);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("specPages: " + specPages);
		}

		Iterator<?> ids = LCSQuery.getObjectsFromCollection(specIds).iterator();
		LCSDocumentQuery query = new LCSDocumentQuery();
		SPARCPDFProductSpecificationGenerator2 ppsg = null;
		ZipGenerator zipGen = new ZipGenerator();
		zipGen.setIncludeSecondary(secondaryContent);
		ArrayList<String> allZips = new ArrayList<String>();
		String oneZip = null;
		String zipFile = null;
		String pdf = null;
		String childPdf = null;
		Collection<?> childSpecs = null;
		FlexSpecification spec = null;
		FlexSpecification childSpec = null;
		Vector<?> childDocs = null;
		Vector docs = null;
		Vector docs2 = null;
		while (ids.hasNext()) {
			try {
				spec = (FlexSpecification) ids.next();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("id: " + FormatHelper.getVersionId(spec));
				}
				if (includeChildSpecs) {
					childSpecs = FlexSpecQuery.findSpecToSpecLinks(spec);
				}

				ppsg = getGenerator(spec, sMaster);
				setGeneratorParams(ppsg, spec, specPages, params, outputFolder);

				LOGGER.debug("about to generate spec");
				pdf = ppsg.generateSpec();
				LOGGER.debug("finished generating spec");

				SPARCBOMMatColorExcelGenerator eBom;
				LCSProduct product = (LCSProduct) VersionHelper.getVersion(spec.getSpecOwner(), "A");
				eBom = new SPARCBOMMatColorExcelGenerator(ppsg.getUnmodifiableParamsExcel());

				docs = query.findPartDocAvailReferences(flexTypes, spec);
				if (ReviseLogic.REVISE_DOCUMENT) {
					Vector<?> descDocs = (Vector<?>) query.findPartDocDescribe(spec, flexTypes);
					docs.addAll(descDocs);
				}
				ArrayList<String> pdfChildren = new ArrayList<String>();
				if (includeChildSpecs) {
					FlexObject fo;
					for (Iterator<?> it = childSpecs.iterator(); it.hasNext();) {
						fo = (FlexObject) it.next();
						specPagesStr = fo.getData("FLEXSPECIFICATION.IDA2A2");
						childSpec = (FlexSpecification) LCSQuery.findObjectById(SPECIFICATION + specPagesStr);
						childDocs = query.findPartDocAvailReferences(flexTypes, childSpec);
						docs.addAll(childDocs);
						if (ReviseLogic.REVISE_DOCUMENT) {
							childDocs = (Vector) query.findPartDocDescribe(childSpec, flexTypes);
							docs.addAll(childDocs);
						}
						ppsg = getGenerator(childSpec, sMaster);
						setGeneratorParams(ppsg, childSpec, specPages, params, outputFolder);
						childPdf = ppsg.generateSpec();
						LOGGER.debug("finished generating child spec");
						pdfChildren.add(childPdf);
					}
				}
				addProductDocs(product, docs, query, flexTypes);
				if (sMaster != null) {
					product = LCSSeasonQuery.getProductForSeason(product, sMaster);
				}
				addProductDocs(product, docs, query, flexTypes);

				// temporarily hide while all the functions are ready.
				/*
				 * String epmFilterName = (String)params.get("cadDocFilter"); Collection<String>
				 * epmIds = null; //find all associated EPMDocuments if (includeCADDocuments) {
				 * epmIds = LCSEPMDocumentHelper.getEPMDocumentsIdBySpec(spec); } oneZip =
				 * zipGen.addToZipFile(docs, epmFilterName, pdfChildren, pdf, epmIds);
				 */

				ArrayList<String> view = (ArrayList<String>) eBom.generateBOMMatColorExcelFromMultiSpecGen(product,
						spec, sMaster);

				pdfChildren.addAll(view);
				oneZip = zipGen.addToZip(docs, pdfChildren, pdf);
				if (zipFile == null) {
					int indx = oneZip.lastIndexOf(java.io.File.separator);
					String seasonName = "";
					if (season != null) {
						seasonName = season.getName() + "-";
					}

					zipFile = oneZip.substring(0, indx + 1)
							+ FormatHelper.formatRemoveProblemFileNameChars(
									seasonName + "ProductSpecs-" + wt.session.SessionHelper.getPrincipal().getName())
							+ ".zip";
					zipFile = FileRenamer.rename(zipFile);
				}
				allZips.add(oneZip);
				pdfChildren.add(pdf);
				deletePDFs(pdfChildren);

				String documentVault = (String) params.get("documentVault");
				String vaultDocumentTypeId = (String) params.get("vaultDocumentTypeId");

				if ("true".equals(documentVault) && FormatHelper.hasContent(vaultDocumentTypeId)) {

					LCSDocumentClientModel documentModel = new LCSDocumentClientModel();
					FlexType vaultDocumentType = null;
					try {

						if (FormatHelper.hasContent(vaultDocumentTypeId)) {
							vaultDocumentType = FlexTypeCache.getFlexType(vaultDocumentTypeId);
						}

						Collection<LCSDocument> exisitDocuments = LCSQuery.getObjectsFromResults(
								new LCSDocumentQuery().findPartDocReferences(spec),
								"OR:com.lcs.wc.document.LCSDocument:", "LCSDocument.IDA2A2");
						LCSDocument exisitDocument = null;
						String exisitDocumentName = null;
						String exisitDocumentSequenceNumber = "";
						int documentSequenceNumber = 0;
						String documentName = "Tech Pack for " + spec.getName() + "-" + product.getName() + " - ";
						Pattern pattern = Pattern.compile("[0-9]*");
						Iterator<LCSDocument> exisitDocumentIter = exisitDocuments.iterator();

						while (exisitDocumentIter.hasNext()) {

							exisitDocument = exisitDocumentIter.next();
							exisitDocumentName = exisitDocument.getName();

							if (exisitDocumentName.startsWith(documentName)) {
								if (exisitDocumentName.length() >= exisitDocumentName.lastIndexOf(" - ") + 3) {
									exisitDocumentSequenceNumber = exisitDocumentName
											.substring(exisitDocumentName.lastIndexOf(" - ") + 3);
									if (pattern.matcher(exisitDocumentSequenceNumber).matches()) {
										if (Integer.valueOf(exisitDocumentSequenceNumber) > documentSequenceNumber) {
											documentSequenceNumber = Integer.valueOf(exisitDocumentSequenceNumber);
										}
									}
								}
							}
						}

						documentSequenceNumber = documentSequenceNumber + 1;
						documentModel.setFlexType(vaultDocumentType);
						documentModel.setName(documentName + documentSequenceNumber);
						documentModel.setValue(NAME, documentName + documentSequenceNumber);
						documentModel.save();
						String otherside = FormatHelper
								.getObjectId((WTDocumentMaster) (documentModel.getBusinessObject().getMaster()));
						String newDocRefIds = otherside;

						Collection<String> docIds = MOAHelper.getMOACollection(newDocRefIds);

						documentModel.associateContent(oneZip);
						// Bypass modify access check on the spec by calling the service directly
						// documentModel.associateDocuments(FormatHelper.getObjectId(spec), docIds);
						LCSDocumentHelper.service.associateDocuments(FormatHelper.getObjectId(spec), (Vector) docIds);

					} catch (LCSException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (zipFile != null) {
			ZipHelper zipHelper = new ZipHelper(zipFile, allZips);
			zipHelper.zip();
		}
		if (params.containsKey("pdfRFQ")) {
			DeleteFileHelper.deleteFile((String) params.get("pdfRFQ"));
		}
		for (String zip : allZips) {
			DeleteFileHelper.deleteFile(zip);
		}
		return zipFile;

	}

	private void deletePDFs(ArrayList<String> pdfFiles) {
		int size = pdfFiles.size();
		String pdf = null;
		java.io.File file = null;
		for (int i = 0; i < size; i++) {
			pdf = (String) pdfFiles.get(i);
			file = new java.io.File(pdf);
			if (file.exists()) {
				file.delete();
			}
		}
	}

	void addProductDocs(LCSProduct product, Vector docs, LCSDocumentQuery query, ArrayList<String> flexTypes)
			throws WTException, java.beans.PropertyVetoException {
		if (flexTypes.size() == 0) {
			return;
		}
		String objId = FormatHelper.getNumericFromOid(FormatHelper.getObjectId(product));
		Vector docs1 = query.findPartDocAvailReferences(flexTypes, product);
		docs.addAll(docs1);
		if (ReviseLogic.REVISE_DOCUMENT) {
			docs1 = (Vector) query.findPartDocDescribe(product, flexTypes);
			docs.addAll(docs1);
		}

		Iterator skus = LCSSKUQuery.findSKUs(product).iterator();
		LCSSKU colorway = null;
		while (skus.hasNext()) {
			colorway = (LCSSKU) skus.next();
			objId = FormatHelper.getNumericFromOid(FormatHelper.getObjectId(colorway));
			docs1 = query.findPartDocAvailReferences(flexTypes, colorway);
			docs.addAll(docs1);
			if (ReviseLogic.REVISE_DOCUMENT) {
				docs1 = (Vector) query.findPartDocDescribe(colorway, flexTypes);
				docs.addAll(docs1);
			}
		}
	}

	public void createPDFSpecifications(Collection specIds, Map params, String outputFolder) throws WTException {

		LOGGER.debug("SPARCPDFMultiSpecGenerator - createPDFSpecifications");

		String specPagesStr = (String) params.get(SPEC_PAGES);

		Collection specPages = null;
		if (!FormatHelper.hasContent(specPagesStr)) {
			specPages = new ArrayList();
		} else {
			specPages = MOAHelper.getMOACollection(specPagesStr);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("specPages: " + specPages);
		}
		Iterator ids = LCSQuery.getObjectsFromCollection(specIds).iterator();

		SPARCPDFProductSpecificationGenerator2 ppsg = null;
		LCSSeason seasonObj = (LCSSeason) LCSSeasonQuery.findObjectById((String) params.get("oid"));

		LCSSeasonMaster seasonMaster = (LCSSeasonMaster) seasonObj.getMaster();

		while (ids.hasNext()) {
			try {
				FlexSpecification spec = (FlexSpecification) ids.next();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("id: " + FormatHelper.getVersionId(spec));
				}

				ppsg = getGenerator(spec, seasonMaster);

				setGeneratorParams(ppsg, spec, specPages, params, outputFolder);

			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	public String getSizes1(FlexSpecification spec, LCSProduct product) throws WTException {
		Collection<String> measurementsResults = LCSMeasurementsQuery.findMeasurements(product, null, null);
		String sizeData = "";
		if (!measurementsResults.isEmpty()) {
			for (Object measurementObj : measurementsResults) {
				FlexObject measurement = (FlexObject) measurementObj;
				sizeData += measurement.getData("LCSMEASUREMENTS.SIZERUN");
			}
			return sizeData;
		}
		return null;
	}

	public String getSizes2(FlexSpecification spec, LCSProduct product) throws WTException {
		Collection resultVector = SizingQuery.findProductSizeCategoriesForProduct(product).getResults();

		if (!resultVector.isEmpty()) {
			FlexObject firstElement = (FlexObject) resultVector.iterator().next(); // only concerned with the first
																					// sizing category
			return firstElement.getData("PRODUCTSIZECATEGORY.SIZE2VALUES");
		}

		return null;
	}

	public String getDestinations(FlexSpecification spec, LCSProduct product) throws WTException {
		String destIds = "";
		Collection results = ProductDestinationQuery
				.findProductDestinationsforProduct((LCSPartMaster) product.getMaster()).getResults();
		String id = null;
		Iterator i = results.iterator();
		while (i.hasNext()) {
			id = ((FlexObject) i.next()).getString("PRODUCTDESTINATION.IDA2A2");
			destIds = destIds + id + MOAHelper.DELIM;
		}

		return destIds;
	}

	public String getColorways(FlexSpecification spec, LCSProduct product) throws WTException {
		return getColorways(spec, product, null);

	}

	public String getColorways(FlexSpecification spec, LCSProduct product, LCSSeason season) throws WTException {
		String colorways = "";
		LCSSourcingConfig config = null;
		if (spec != null) {
			config = (LCSSourcingConfig) VersionHelper
					.latestIterationOf((LCSSourcingConfigMaster) spec.getSpecSource());
		}
		Map skuTable = phq.findSKUsMap(product, config, season, true);
		Map reversed = new HashMap();
		Iterator keys = skuTable.keySet().iterator();
		String key = "";
		while (keys.hasNext()) {
			key = (String) keys.next();
			reversed.put(skuTable.get(key), key);
		}

		Collection sortedSkus = SortHelper.sortStrings(reversed.keySet());
		Iterator skuItr = sortedSkus.iterator();
		String temp = null;
		LCSSKU sku = null;
		String numeric = null;
		while (skuItr.hasNext()) {

			temp = (String) skuItr.next();

			sku = (LCSSKU) LCSQuery.findObjectById((String) reversed.get(temp));

			numeric = FormatHelper.getNumericFromReference(sku.getMasterReference());

			colorways = colorways + numeric + MOAHelper.DELIM;
		}

		return colorways;
	}

	public String getSources(FlexSpecification spec, LCSProduct product) throws WTException {
		String sources = "";

		Map sourceTable = phq.findSourcingConfigsMap(product, null);
		Map reversed = new HashMap();
		Iterator keys = sourceTable.keySet().iterator();
		String key = null;
		while (keys.hasNext()) {
			key = (String) keys.next();
			reversed.put(sourceTable.get(key), key);
		}

		Collection sortedSources = SortHelper.sortStrings(reversed.keySet());
		Iterator sourceItr = sortedSources.iterator();
		String temp = null;
		LCSSourcingConfig config = null;
		String numeric = null;
		while (sourceItr.hasNext()) {

			temp = (String) sourceItr.next();

			config = (LCSSourcingConfig) LCSQuery.findObjectById((String) reversed.get(temp));

			numeric = FormatHelper.getNumericFromReference(config.getMasterReference());

			sources = sources + numeric + MOAHelper.DELIM;
		}

		return sources;
	}

	public Collection getComponentsForSpec(FlexSpecification spec, LCSProduct product, Collection specPages)
			throws WTException {
		return getComponentsForSpec(spec, product, specPages, new HashMap());
	}

	public Collection<String> getComponentsForSpec(FlexSpecification spec, LCSProduct product, Collection specPages,
			Map params) throws WTException {

		Collection<String> compIds = new ArrayList<String>();
		Collection<FlexObject> specComponents = new ArrayList<FlexObject>();

		// Header page
		FlexObject fob = new FlexObject();

		fob.put("COMPONENT_TYPE", "Header Page");
		fob.put("NAME", "");
		fob.put("COMPONENTNAME", "");
		fob.put("COMPONENTID", "");
		fob.put("COMPONENTCLASS", "");
		fob.put("COMPONENT_TYPE_UNTRANSLATED", "Header Page");
		specComponents.add(fob);
		// Colorways
		fob = new FlexObject();
		fob.put("COMPONENT_TYPE", "Colorways");
		fob.put("NAME", "");
		fob.put("COMPONENTNAME", "");
		fob.put("COMPONENTID", "");
		fob.put("COMPONENTCLASS", "");
		fob.put("COMPONENT_TYPE_UNTRANSLATED", "Colorways");
		specComponents.add(fob);
		// Hypercare-150, TASK-9256 changes for Sample Details Report - start
		LCSSourcingConfig sampleSourcingConfig = (LCSSourcingConfig) VersionHelper
				.latestIterationOf((LCSSourcingConfigMaster) spec.getSpecSource());
		LOGGER.debug("**sampleSourcingConfig:" + sampleSourcingConfig);
		// Check to filter Samples for TechPack where "IncludeInTechPack is true
		Hashtable criteria1 = new Hashtable();
		criteria1.put(INCLUDETP_KEY, true);

		SearchResults sampleSearchResults1 = LCSSampleQuery.findSamplesForProduct(product, criteria1,
				sampleSourcingConfig.getFlexType(), sampleSourcingConfig);
		LOGGER.debug("**sampleSearchResults1: {}", sampleSearchResults1);
		Collection<?> sampleResults1 = sampleSearchResults1.getResults();
		if (sampleResults1 != null) {
			Iterator<?> sampleItr1 = sampleResults1.iterator();
			while (sampleItr1.hasNext()) {
				FlexObject samFob1 = (FlexObject) sampleItr1.next();

				LCSSample sample1 = (LCSSample) LCSQuery
						.findObjectById("OR:com.lcs.wc.sample.LCSSample:" + samFob1.getString("LCSSAMPLE.IDA2A2"));
				String includeAttColumn = sample1.getFlexType().getAttribute(INCLUDETP_KEY).getColumnName();
				String includeinTPStr = (String) samFob1.getString("LCSSAMPLE." + includeAttColumn);
				if (FormatHelper.hasContent(includeinTPStr) && "1".equals(includeinTPStr)) {
					samFob1.put("COMPONENT_TYPE", "Sample");
					samFob1.put("NAME", sample1.getName());
					samFob1.put("COMPONENTNAME", "");
					samFob1.put("OID", "OR:com.lcs.wc.sample.LCSSample:" + samFob1.getString("LCSSAMPLE.IDA2A2"));
					samFob1.put("COMPONENTID", "");
					samFob1.put("COMPONENTCLASS", "");
					samFob1.put("COMPONENT_TYPE_UNTRANSLATED", "Sample");
					specComponents.add(samFob1);
				}
			}
		}
		LOGGER.debug("**specComponents:" + specComponents);
		LOGGER.debug("**specPages:" + specPages);
		// Hypercare-150, TASK-9256 changes for Sample Details Report - end

		// Collection specComponents =
		// SPARCFlexSpecQuery.getSpecToComponentObjectsData(spec);
		Collection ootbspecComponents = FlexSpecQuery.getSpecToComponentObjectsData(spec);
		if (FormatHelper.parseBoolean((String) params.get("includeChildSpecs"))) {
			ootbspecComponents.addAll(FlexSpecQuery.getChildSpecComponents(spec, false));
		}
		specComponents.addAll(ootbspecComponents);

		Iterator<?> specTypeItr = specPages.iterator();
		for (specTypeItr = specPages.iterator(); specTypeItr.hasNext();) {

			String techPackComponentType = (String) specTypeItr.next();

			Iterator<?> specCompItr = specComponents.iterator();
			while (specCompItr.hasNext()) {
				FlexObject fo = (FlexObject) specCompItr.next();

				// We need more consistent on strings. Need a class with all constants to remove
				// cases of "Images pagee" vs
				// "IMAGE_PAGE" or "Measurements" vs "MEASUREMENT"
				String componentType = fo.getString("COMPONENT_TYPE_UNTRANSLATED");
				String componentType2;

				if (componentType.equalsIgnoreCase("Images Page")) {

					componentType2 = "IMAGES_PAGE:" + fo.getString("IMAGES_PAGE_TYPE");
				} else if (componentType.equalsIgnoreCase("Measurements")) {
					componentType2 = "MEASUREMENT";
				} else if (FormatHelper.hasContent(componentType) && componentType.equalsIgnoreCase("Sample")) {
					componentType2 = "SAMPLE";
				} else {
					componentType2 = componentType;
				}


                if (componentType2.equalsIgnoreCase(techPackComponentType)) {
                    compIds.add(componentType + CDELIM + fo.getString("OID"));
                    if (("true").equals(params.get("includeMarkedupImagesContent")) && ("Images Page").equalsIgnoreCase(componentType)) {
                        LCSDocument lcsDoc = (LCSDocument) LCSDocumentQuery.findObjectById((String)fo.get("OID"));
                        ApplicationData ad = null;
                        String desc = null;
                        LCSDocument document = null;
                        if(lcsDoc!=null) {
                            try {
                                document = (LCSDocument) ContentHelper.service.getContents(lcsDoc);
                               } catch (WTException e) {
                               // TODO Auto-generated catch block
                               e.printStackTrace();
                               } catch (PropertyVetoException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                               }
                            if(document!=null) {
                               Vector<?> ads = ContentHelper.getApplicationData(document);
                               for (int j = 0; j < ads.size(); j++) {
                                  ad = (ApplicationData) ads.elementAt(j);
                                  desc = ad.getDescription();
                                  desc = (desc == null) ? "" : desc;
                                  if (desc.startsWith("MARKUP")) {
                                   compIds.add(componentType + CDELIM + fo.getString("OID")+ CDELIM + ("MARKUPIMG") );
                                   break;
                                  }
                           	}
                          }
                        }
    
                    }
                }
            }
        }
       	LOGGER.debug("**compIds:" + compIds);
        return compIds;
    }

	public Collection getBOMParts(LCSProduct product, FlexSpecification spec) throws WTException {
		Collection bpIds = new ArrayList();
		Collection bomParts = (new LCSFlexBOMQuery()).findBOMPartsForOwner(product, null, null, spec);
		if (bomParts != null && bomParts.size() > 0) {
			Iterator i = bomParts.iterator();

			while (i.hasNext()) {
				bpIds.add("BOM" + CDELIM + FormatHelper.getVersionId((FlexBOMPart) i.next()));
			}
		}
		return bpIds;
	}

	public Collection getConstructionInfo(LCSProduct product, FlexSpecification spec) throws WTException {
		Collection results = LCSConstructionQuery.findConstructionForProduct(product, spec).getResults();
		Collection ms = new ArrayList();
		Iterator i = results.iterator();
		FlexObject obj = null;
		while (i.hasNext()) {
			obj = (FlexObject) i.next();
			ms.add("Construction" + CDELIM + "VR:com.lcs.wc.construction.LCSConstructionInfo:"
					+ (String) obj.get("LCSCONSTRUCTIONINFO.BRANCHIDITERATIONINFO"));
		}

		return ms;
	}

	public Collection getMeasurements(LCSProduct product, FlexSpecification spec) throws WTException {
		Collection results = LCSMeasurementsQuery.findMeasurmentsForProduct(product, spec).getResults();
		Collection ms = new ArrayList();
		Iterator i = results.iterator();
		FlexObject obj = null;
		while (i.hasNext()) {
			obj = (FlexObject) i.next();
			ms.add("Measurements" + CDELIM + "VR:com.lcs.wc.measurements.LCSMeasurements:"
					+ (String) obj.get("LCSMEASUREMENTS.BRANCHIDITERATIONINFO"));
		}

		return ms;
	}

	private Collection getImagePages(LCSProduct product, FlexSpecification spec, String pageType) throws WTException {
		try {

			LOGGER.debug("----------------------------------------------");
			LOGGER.debug("PDFMultiSpecGenerator - getImagePages");
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("product: " + product);
				LOGGER.debug("spec: " + spec);
				LOGGER.debug("pageType: " + pageType);
			}
			LOGGER.debug("----------------------------------------------");

			String productId = FormatHelper.getObjectId((LCSPartMaster) product.getMaster());
			String specId = null;
			if (spec != null) {
				specId = FormatHelper.getObjectId(spec.getMaster());
			}

			FlexTypeQueryStatement statement = (new LCSProductQuery()).getProductImagesQuery(productId, specId);
			if (FormatHelper.hasContent(pageType)) {
				statement.appendAndIfNeeded();
				statement.appendFlexCriteria("pageType", pageType, Criteria.EQUALS);
			}

			FlexType documentType = FlexTypeCache.getFlexTypeFromPath("Document");
			FlexTypeAttribute docNameAtt = documentType.getAttribute(NAME);

			statement.appendSortBy(new QueryColumn(LCSDocument.class, docNameAtt.getColumnDescriptorName()));

			Collection results = LCSQuery.runDirectQuery(statement).getResults();

			LOGGER.debug("----------------------------------------------");
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("statement: " + statement);
			}
			LOGGER.debug("----------------------------------------------");
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("results: " + results);
			}
			LOGGER.debug("----------------------------------------------");

			Iterator i = results.iterator();
			FlexObject obj = null;
			Collection ms = new ArrayList();
			while (i.hasNext()) {
				obj = (FlexObject) i.next();
				ms.add("Images Page" + CDELIM + "VR:com.lcs.wc.document.LCSDocument:"
						+ obj.get("LCSDOCUMENT.BRANCHIDITERATIONINFO"));
			}

			return ms;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WTException(e);
		}
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
