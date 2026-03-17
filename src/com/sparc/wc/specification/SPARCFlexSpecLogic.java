package com.sparc.wc.specification;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.client.ApplicationContext;
import com.lcs.wc.client.ClientContext;
import com.lcs.wc.client.web.pdf.PDFPageSize;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.document.LCSDocumentClientModel;
import com.lcs.wc.document.LCSDocumentLogic;
import com.lcs.wc.document.LCSDocumentQuery;
import com.lcs.wc.document.ZipGenerator;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.PDFProductSpecificationGenerator2;
import com.lcs.wc.report.FiltersList;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.specification.FlexSpecLogic;
import com.lcs.wc.specification.FlexSpecToSeasonLink;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.DeleteFileHelper;
import com.lcs.wc.util.FileLocation;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;
import com.ptc.core.common.util.Pair;
import com.sparc.wc.flexbom.gen.SPARCBOMMatColorExcelGenerator;

import wt.pom.Transaction;
import wt.util.WTException;

public class SPARCFlexSpecLogic extends FlexSpecLogic {

	private static final Logger LOGGER = LogManager.getLogger(SPARCFlexSpecLogic.class);

	@Override
	public String generateTechPackImpl(String timeToLive, String specId, String productId, String specPages,
			Map criteria, String availDocs) throws WTException {
		Pair<String, LCSDocumentClientModel> tpResult = this._generateTechPackWithExcelImpl(timeToLive, specId, productId,
				specPages, criteria, availDocs);

		return tpResult.first;
	}

	@Override
	public Pair<String, LCSDocumentClientModel> asyncGenerateTechPackImpl(String timeToLive, String specId,
			String productId, String specPages, Map criteria, String availDocs) throws WTException {
		return _generateTechPackWithExcelImpl(timeToLive, specId, productId, specPages, criteria, availDocs);
	}

	@SuppressWarnings("unchecked")
	private Pair<String, LCSDocumentClientModel> _generateTechPackWithExcelImpl(String timeToLive, String specId,
			String productId, String specPages, Map criteria, String availDocs) throws WTException {
		ClientContext cc = ClientContext.getContext();
		synchronized (cc) {
			Locale clientLocale = (Locale) criteria.get("clientLocale");
			Locale currentCCLocale = ClientContext.getContext().getLocale();

			try {
				if (clientLocale != null) {

					ClientContext.getContext().setLocale(clientLocale);
				}

				DeleteFileHelper dFH = new DeleteFileHelper();
				dFH.deleteOldFiles(FileLocation.PDFDownloadLocationImages, timeToLive);
				dFH.deleteOldFiles(FileLocation.PDFDownloadLocationFiles, timeToLive);

				PDFProductSpecificationGenerator2 ppsg = null;
				String sourceNumber = "";
				FlexSpecification spec = null;
				LCSProduct product = null;
				String specName = "";
				Object[] arguments;
				if (!FormatHelper.hasContent(specId)) {

					ApplicationContext appContext = new ApplicationContext();
					appContext.setProductContext(productId);
					product = appContext.getProductARev();
					arguments = new Object[] { product };
					ppsg = PDFProductSpecificationGenerator2.getOverrideInstance(arguments);
				} else {
					if (specId.indexOf("FlexSpecToSeasonLink") > -1) {
						FlexSpecToSeasonLink fstsl = (FlexSpecToSeasonLink) LCSQuery.findObjectById(specId);
						arguments = new Object[] { fstsl };
						ppsg = PDFProductSpecificationGenerator2.getOverrideInstance(arguments);
						spec = (FlexSpecification) VersionHelper.latestIterationOf(fstsl.getSpecificationMaster());
						sourceNumber = FormatHelper.getNumericObjectIdFromObject(spec.getSpecSource());
					} else {
						spec = (FlexSpecification) LCSQuery.findObjectById(specId);
						arguments = new Object[] { spec };
						ppsg = PDFProductSpecificationGenerator2.getOverrideInstance(arguments);

						sourceNumber = FormatHelper.getNumericObjectIdFromObject(spec.getSpecSource());
					}
					product = (LCSProduct) VersionHelper.getVersion(spec.getSpecOwner(), "A");
					specName = spec.getName();
				}

				if (FormatHelper.hasContent(specPages)) {
					Collection<String> pages = MOAHelper.getMOACollection(specPages);
					ppsg.setPages(pages);
				}


				String fileURL = "";
				if (FormatHelper.hasContent((String) criteria.get("paperSize"))) {
					ppsg.setPageSize(PDFPageSize.getPageSize((String) criteria.get("paperSize")));
				} else {
					ppsg.setPageSize(PDFPageSize.getPageSize("LETTER"));
				}

				ppsg.setLandscape("true".equals(criteria.get("useLandscape")));

				if (FormatHelper.hasContent((String) criteria.get("source"))) {
					ppsg.setSources((String) criteria.get("source"));
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("request.source was :  {}", criteria.get("source"));
					}
				} else {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("derived source was :  {}", sourceNumber);
					}
					ppsg.setSources(sourceNumber);
				}

				ppsg.setColorways((String) criteria.get("colorwaydata"));

				ppsg.setSize1Sizes((String) criteria.get("size1data"));

				ppsg.setSize2Sizes((String) criteria.get("size2data"));

				ppsg.setDestinations((String) criteria.get("destinationdata"));

				ppsg.setPageOptions((String) criteria.get("pageOptions"));

				ppsg.setSpecPageOptions((String) criteria.get("specPages"));

				ppsg.setBOMSectionViews((String) criteria.get("viewSelect"));

				ppsg.setColorwaysPerPage((String) criteria.get("numColorwaysPerPage"));

				ppsg.setSizesPerPage((String) criteria.get("numSizesPerPage"));

				ppsg.setShowChangeSince((String) criteria.get("showChangeSince"));

				ppsg.setAvailEPMDOCDocs((String) criteria.get("availEPMDOCDocs"));

				ppsg.setCadDocFilter((String) criteria.get("cadDocFilter"));

				ppsg.setAvailParts((String) criteria.get("availParts"));

				ppsg.setPartFilter((String) criteria.get("partFilter"));

				ppsg.setShowIndentedBOM((String) criteria.get("exportedIndentedBOM"));

				String sizeCatId = "";
				new SizingQuery();
				Vector results = SizingQuery.findProductSizeCategoriesForProduct(product).getResults();
				if (!results.isEmpty()) {
					FlexObject firstElement = (FlexObject) results.elementAt(0);

					sizeCatId = "OR:com.lcs.wc.sizing.ProductSizeCategory:"
							+ firstElement.getData("PRODUCTSIZECATEGORY.IDA2A2");

				}

				ppsg.setProductSizeCatId(sizeCatId);

				ppsg.setShowColorSwatch((String) criteria.get("showColorSwatches"));
				ppsg.setShowMatThumbnail((String) criteria.get("showMaterialThumbnail"));
				ppsg.setUseSize1Size2((String) criteria.get("useSize1Size2"));

				HashMap<String, Object> params = new HashMap<>();
				params.put("PRINT_BOM_HEADER", "true");
				params.put("PRINT_BOM_FOOTER", "true");
				params.put("PRINT_MEAS_HEADER", "true");
				params.put("PRINT_MEAS_FOOTER", "true");
				params.put("UOM", criteria.get("uom"));

				params.put("PRINT_CONST_HEADER", "true");
				params.put("PRINT_CONST_FOOTER", "true");
				params.put(PDFProductSpecificationGenerator2.INCLUDE_BOM_OWNER_VARIATIONS,
						criteria.get("includeBOMOwnerVariations"));

				params.put(PDFProductSpecificationGenerator2.INCLUDE_MEASUREMENTS_OWNER_SIZES,
						criteria.get("includeMeasurementsOwnerSizes"));

				params.put(PDFProductSpecificationGenerator2.INCLUDE_MARKEDUP_IMAGES_CONTENT,
						criteria.get("includeMarkedupImagesContent"));

				if (clientLocale != null) {
					params.put("clientLocale", clientLocale);
				}
				ppsg.setAddlParams(params);
				
				criteria.put("USE_MAT_THUMBNAIL", criteria.get("showMaterialThumbnail"));
				criteria.put(PDFProductSpecificationGenerator2.INCLUDE_BOM_OWNER_VARIATIONS,
						criteria.get("includeBOMOwnerVariations"));

				criteria.put(PDFProductSpecificationGenerator2.INCLUDE_MEASUREMENTS_OWNER_SIZES,
						criteria.get("includeMeasurementsOwnerSizes"));

				criteria.put(PDFProductSpecificationGenerator2.INCLUDE_MARKEDUP_IMAGES_CONTENT,
						criteria.get("includeMarkedupImagesContent"));
				criteria.put("PRODUCT_SIZE_CAT_ID", sizeCatId);
				SPARCBOMMatColorExcelGenerator eBom = new SPARCBOMMatColorExcelGenerator(criteria);
				ArrayList<String> view = (ArrayList<String>) eBom.generateBOMMatColorExcel(productId, specPages);
				Vector<FlexObject> docs = new Vector<FlexObject>();

				fileURL = ppsg.generateSpec();
				ZipGenerator gen = new ZipGenerator();
				String includeSecondary = (String) criteria.get("includeSecondaryContent");
				if ("true".equals(includeSecondary)) {
					gen.setIncludeSecondary(true);
				}

				fileURL = gen.getURL(gen.addToZip(docs, view, fileURL));

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("File URL {} exists {}", fileURL, (new File(fileURL)).exists());
				}
				String documentVault = (String) criteria.get("documentVault");
				String vaultDocumentTypeId = (String) criteria.get("vaultDocumentTypeId");

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("documentVault = {}", documentVault);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("vaultDocumentTypeId = {}", vaultDocumentTypeId);
				}
				if (!documentVault.equals("true") || !FormatHelper.hasContent(vaultDocumentTypeId)) {
					return new Pair<>(fileURL, null);
				}
				LCSDocumentClientModel documentModel = new LCSDocumentClientModel();
				FlexType vaultDocumentType = null;

				if (FormatHelper.hasContent(vaultDocumentTypeId)) {
					vaultDocumentType = FlexTypeCache.getFlexType(vaultDocumentTypeId);
				}

				String documentName = "Tech Pack for " + specName + " / " + product.getName() + " - ";
				Map<String, String> criteria2 = new HashMap();
				String colName = vaultDocumentType.getAttribute("name").getSearchCriteriaIndex();
				criteria2.put(colName, documentName + "*");
				criteria2.put("skipRanges", "true");

				Collection<LCSDocument> exisitDocuments = LCSQuery.getObjectsFromResults(
						(new LCSDocumentQuery()).findByCriteria(criteria2, vaultDocumentType, (Collection) null,
								(FiltersList) null, (Collection) null),
						"OR:com.lcs.wc.document.LCSDocument:", "LCSDocument.IDA2A2");

				LCSDocument exisitDocument = null;
				String exisitDocumentName = "";
				String exisitDocumentSequenceNumber = "";
				int documentSequenceNumber = 0;
				Pattern pattern = Pattern.compile("[0-9]*");
				Iterator<LCSDocument> exisitDocumentIter = exisitDocuments.iterator();

				while (exisitDocumentIter.hasNext()) {

					exisitDocument = exisitDocumentIter.next();
					exisitDocumentName = exisitDocument.getName();

					if (exisitDocumentName.startsWith(documentName)
							&& exisitDocumentName.length() >= exisitDocumentName.lastIndexOf(" - ") + 3) {

						exisitDocumentSequenceNumber = exisitDocumentName
								.substring(exisitDocumentName.lastIndexOf(" - ") + 3);

						if (pattern.matcher(exisitDocumentSequenceNumber).matches()
								&& Integer.valueOf(exisitDocumentSequenceNumber) > documentSequenceNumber) {

							documentSequenceNumber = Integer.valueOf(exisitDocumentSequenceNumber);

						}
					}
				}

				++documentSequenceNumber;
				documentModel.setFlexType(vaultDocumentType);
				documentModel.setName(documentName + documentSequenceNumber);
				documentModel.setValue("name", documentName + documentSequenceNumber);
				documentModel.save();

				String newDocRefIds = FormatHelper.getObjectId(documentModel.getBusinessObject().getMaster());
				Collection<String> ids = MOAHelper.getMOACollection(newDocRefIds);

				documentModel.associateContent(fileURL);

				if (spec != null) {
					(new LCSDocumentLogic()).associateDocuments(FormatHelper.getObjectId(spec), ids);
				}
				return new Pair<>(fileURL, documentModel);

			} catch (Exception e) {
				LOGGER.info("ERROR", e);
				if (e instanceof WTException) {
					throw (WTException) e;
				} else {
					throw new WTException(e);

				}
			} finally {
				ClientContext.getContext().setLocale(currentCCLocale);
			}
		}
	}

	public String generateTechPack(String timeToLive, String specId, String productId, String specPages,
			String availDocs, Map criteria) throws WTException {
		Transaction tr = null;
		String url = null;

		try {
			tr = new Transaction();
			tr.start();

			url = this.generateTechPackImpl(timeToLive, specId, productId, specPages, criteria, availDocs);
			tr.commit();
			tr = null;
		} finally {
			if (tr != null) {
				tr.rollback();
			}

		}

		return url;
	}

}
