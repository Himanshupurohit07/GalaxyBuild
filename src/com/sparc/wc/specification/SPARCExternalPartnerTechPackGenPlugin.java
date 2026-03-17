package com.sparc.wc.specification;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.document.FileRenamer;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.FileLocation;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.product.SPARCProductLogic;
import com.sparc.wc.shareFile.SPARCShareFileAPICallLogEntry;
import com.sparc.wc.shareFile.SPARCShareFileAPILogEntryService;
import com.sparc.wc.shareFile.SPARCShareFileUtil;
import com.sparc.wc.shareFile.SPARCTechPackShareFileUtil;
import com.sparc.wc.util.SPARCQueueHelper;
import com.sparc.wc.util.SparcCostingUploadUtil;

import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;
import wt.util.WTException;

public class SPARCExternalPartnerTechPackGenPlugin {
	private static final Logger logger = LogManager.getLogger(SPARCExternalPartnerTechPackGenPlugin.class.getName());
	private static final String PDFEXPORTQUEUENAME = LCSProperties.get("com.sparc.wc.specification.pdfExportQueueName");
	private static final String BRAND_KEY = LCSProperties
			.get("com.sparc.wc.product.SPARCExternalPartnerTechPackGenPlugin.BrandKey");
	private static final String EXTERNALPARTENER_KEY = LCSProperties
			.get("com.sparc.wc.product.SPARCExternalPartnerTechPackGenPlugin.ExternalPartenersKey");
	private static final String SPECSTATUS_KEY = LCSProperties
			.get("com.sparc.wc.product.SPARCExternalPartnerTechPackGenPlugin.SpecStatusKey");
	private static final String SPECSTATUS_VAL = LCSProperties
			.get("com.sparc.wc.product.SPARCExternalPartnerTechPackGenPlugin.SpecStatusVal");
	private static final String LASTSENTKEY = LCSProperties
			.get("com.sparc.wc.product.SPARCExternalPartnerTechPackGenPlugin.lastSentKey");
	private static final String RESENDTP_KEY = LCSProperties
			.get("com.sparc.wc.product.SPARCExternalPartnerTechPackGenPlugin.resendTPKey");
	private static final String APICALL_STRING = LCSProperties
			.get("com.sparc.wc.product.SPARCExternalPartnerTechPackGenPlugin.apiCallString");
	private static final String PRODUCTNUM_KEY = LCSProperties
			.get("com.sparc.wc.product.SPARCExternalPartnerTechPackGenPlugin.PLMProductNumKey");

	public static void createQueueEntryToGenerateTechPackForExternalPartners(WTObject wtObject) {
		logger.debug("Plugin method called createQueueEntryToGenerateTechPackForExternalPartners.");
		try {
			if (wtObject instanceof FlexSpecification) {

				FlexSpecification spec = (FlexSpecification) wtObject;

				Boolean valid = validateSpecForExport(spec);
				if (Boolean.FALSE.equals(valid)) {
					return;
				}

			} else if (wtObject instanceof LCSProductSeasonLink) {

				LCSProductSeasonLink psLink = (LCSProductSeasonLink) wtObject;
				if (!(psLink.isEffectLatest())) {
					return;
				}
				List<String> newPartners = newPartnersAddedToProductSeason(psLink);
				if (Boolean.TRUE.equals(newPartners.isEmpty())) {
					return;
				}
			}
			Class<?>[] argTypes = (new Class[] { String.class });
			Object[] args = (new Object[] { wtObject.toString() });
			new SPARCQueueHelper().addQueueEntryForPDFExport(argTypes, args, PDFEXPORTQUEUENAME,
					"com.sparc.wc.specification.SPARCExternalPartnerTechPackGenPlugin", "exportPDF");
		} catch (Exception ex) {
			logger.error("Error procssing plugin method {}", (Object) ex.getStackTrace());
			ex.printStackTrace();
		}
	}

	private static List<String> newPartnersAddedToProductSeason(LCSProductSeasonLink psLink) {
		List<String> newPartnersList = Collections.emptyList();
		try {
			LCSProductSeasonLink psLinkPrior = (LCSProductSeasonLink) LCSSeasonQuery.getPriorSeasonProductLink(psLink);
			String prePartners = psLinkPrior != null ? (String) psLinkPrior.getValue(EXTERNALPARTENER_KEY) : null;
			String newPartners = (String) psLink.getValue(EXTERNALPARTENER_KEY);
			Collection<String> prePartnerColl = FormatHelper.hasContent(prePartners)
					? MOAHelper.getMOACollection(prePartners)
					: Collections.emptyList();
			Collection<String> newPartnerColl = FormatHelper.hasContent(newPartners)
					? MOAHelper.getMOACollection(newPartners)
					: Collections.emptyList();

			newPartnersList = newPartnerColl.stream()
					.filter(partner -> prePartnerColl.stream().noneMatch(oldPartner -> oldPartner.equals(partner)))
					.collect(Collectors.toList());

		} catch (WTException e) {
			logger.error("Unable to check new partners added to the list due to exception!", e);
			e.printStackTrace();
		}
		return newPartnersList;
	}

	private static Boolean validateSpecForExport(FlexSpecification spec) throws WTException {
		Boolean valid = false;
		String specStatus = (String) spec.getValue(SPECSTATUS_KEY);
		if (SPECSTATUS_VAL.equals(specStatus)) {
			FlexSpecification priorSpec = (FlexSpecification) VersionHelper.predecessorOf(spec);
			String preSpecStatus = null;
			if (priorSpec != null) {
				preSpecStatus = (String) priorSpec.getValue(SPECSTATUS_KEY);
			}
			Boolean resend = (Boolean) spec.getValue(RESENDTP_KEY);
			if (!specStatus.equals(preSpecStatus) || specStatus.equals(preSpecStatus) && resend)
				valid = true;
		}
		return valid;
	}

	public static void exportPDF(String wtobjectId) {
		try {
			WTObject wtObject = (WTObject) LCSQuery.findObjectById(wtobjectId);
			if (wtObject instanceof FlexSpecification) {
				logger.debug("wtObject object of specification!!");
				FlexSpecification spec = (FlexSpecification) wtObject;
				LCSProduct prod = (LCSProduct) VersionHelper.getVersion(spec.getSpecOwner(), "A");
				if (prod != null) {
					processSpecForExport(spec, prod);
				}

			} else if (wtObject instanceof LCSProductSeasonLink) {
				logger.debug("wtObject object of LCSProductSeasonLink!!");
				LCSProductSeasonLink psLink = (LCSProductSeasonLink) wtObject;
				LCSProduct product = LCSProductQuery.getProductVersion("" + psLink.getProductMasterId(), "A");
				List<String> newPartners = newPartnersAddedToProductSeason(psLink);
				logger.debug("newPartners ::;:: {}", newPartners);
				processPSLinkForExport(product, psLink, newPartners);
			}
		} catch (WTException e) {
			logger.error("WTException Processing Queue Method ExportPDF {}", (Object) e.getStackTrace());
			e.printStackTrace();
		}
	}

	private static void processPSLinkForExport(LCSProduct product, LCSProductSeasonLink psLink,
			List<String> newPartners) {
		boolean success = false;
		try {
			LCSSeasonMaster seasonMaster = psLink.getSeasonMaster();
			LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(seasonMaster);
			SearchResults specResults = FlexSpecQuery.findExistingSpecs(product, season, (WTObject) null);
			Vector<?> specs = specResults.getResults();
			logger.debug("Number of specs this product and season : {}", specs.size());
			FlexSpecification specObject = null;
			Iterator<?> specIter = specs.iterator();
			while (specIter.hasNext()) {
				FlexObject specFO = (FlexObject) specIter.next();
				String specID = "com.lcs.wc.specification.FlexSpecification:"
						+ specFO.getString("FLEXSPECIFICATION.IDA2A2");
				specObject = (FlexSpecification) LCSQuery.findObjectById(specID);
				logger.info("processing spec : {}", specObject.getName());
				String specStatus = (String) specObject.getValue(SPECSTATUS_KEY);
				if (FormatHelper.hasContent(specStatus) && SPECSTATUS_VAL.equals(specStatus)) {
					success = createAndShareTP(psLink, specObject, newPartners);
				}
			}
		} catch (Exception ex) {
			logger.error("Exception in processPDLinkForExport {}", (Object) ex.getStackTrace());
			success = false;
		}
		if (success) {
			try {
				LocalDate currentDate = LocalDate.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
				String formattedDate = currentDate.format(formatter);
				psLink.setValue(LASTSENTKEY, formattedDate);
				// LCSLogic.persist(psLink, true);
				PersistenceServerHelper.manager.update(psLink);
			} catch (Exception ex) {
				logger.error("error while updating lastSent date after successfule execution {}",
						(Object) ex.getStackTrace());
			}
		}
	}

	private static void processSpecForExport(FlexSpecification spec, LCSProduct prod) {
		boolean success = false;
		try {
			Collection<?> seasonsLinked = FlexSpecQuery.specSeasonUsed(spec.getMaster());
			Iterator<?> seasonItr = seasonsLinked.iterator();
			while (seasonItr.hasNext()) {

				FlexObject sObj = (FlexObject) seasonItr.next();
				LCSSeason season = (LCSSeason) LCSQuery.findObjectById(
						"VR:com.lcs.wc.season.LCSSeason:" + sObj.getString("LCSSEASON.BRANCHIDITERATIONINFO"));
				if (season != null) {
					LCSProductSeasonLink psLink = (LCSProductSeasonLink) LCSSeasonQuery.findSeasonProductLink(prod,
							season);
					if (psLink != null) {
						String partners = (String) psLink.getValue(EXTERNALPARTENER_KEY);
						logger.debug("Partners available : {}", partners);
						List<String> partnerDel = (List<String>) MOAHelper.getMOACollection(partners);
						if ((partnerDel != null && !partnerDel.isEmpty())) {
							success = createAndShareTP(psLink, spec, partnerDel);
							if (success) {
								try {
									LocalDate currentDate = LocalDate.now();
									DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
									String formattedDate = currentDate.format(formatter);
									psLink.setValue(LASTSENTKEY, formattedDate);
									// LCSLogic.persist(psLink, true);
									PersistenceServerHelper.manager.update(psLink);
								} catch (Exception ex) {
									logger.error(
											"error while updating lastSent date after successfule execution of spec {} execption as : ",
											spec.getName(), (Object) ex.getStackTrace());
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Exception processing PSLink.. {}", (Object) ex.getStackTrace());
			success = false;
		}

		if (success) {
			try {
				spec.setValue(RESENDTP_KEY, false);
				PersistenceServerHelper.manager.update(spec);
				// FlexSpecLogic.persist(spec, true);
			} catch (Exception ex) {
				logger.error("error while resetting RESEND TECHPACK after successful execution {}",
						(Object) ex.getStackTrace());
			}
		}
	}

	private static boolean createAndShareTP(LCSProductSeasonLink psLink, FlexSpecification specObject,
			List<String> newPartners) {
		Boolean success = false;
		try {
			Map<String, String> newParams = new SPARCTechPackShareFileUtil().createPDFParams(psLink, specObject);
			logger.debug("params fetched for TP creation ::: {}", newParams);
			success = uploadOnShareFile(newParams, specObject, psLink, newPartners);
		} catch (WTException e) {
			logger.error("error in uploadOnShareFile {}", (Object) e.getStackTrace());
			e.printStackTrace();
			return false;
		}
		return success;
	}

	private static boolean uploadOnShareFile(Map<String, String> params, FlexSpecification spec,
			LCSProductSeasonLink psLink, List<String> partnerDel) throws WTException {
		Boolean success = false;
		LCSSeason season = null;
		Map<Integer, String> response = new HashMap<>();
		Long plmProductNo = null;
		String partner = null;
		SPARCShareFileAPICallLogEntry shareFileLogEntry = null;

		SPARCProductLogic logic = new SPARCProductLogic();
		String folder = getTempFolderPath();
		LCSProduct product = LCSProductQuery.getProductVersion("" + psLink.getProductMasterId(), "A");
		String brand = (String) product.getValue(BRAND_KEY);
		plmProductNo = (Long) product.getValue(PRODUCTNUM_KEY);
		LCSSeasonMaster seasonMaster = psLink.getSeasonMaster();
		season = (LCSSeason) VersionHelper.latestIterationOf(seasonMaster);

		String specId = "VR:com.lcs.wc.specification.FlexSpecification:" + spec.getBranchIdentifier();
		Collection<String> specIds = new ArrayList<>();
		specIds.add(specId);
		String url = logic.createPDFSpecifications(specIds, params, folder);
		logger.debug("Tech Pack created at local path : {}", url);
		if (url != null) {
			String outFilePath = getUnZipPath(url);
			Iterator<String> partneritr = partnerDel.iterator();
			while (partneritr.hasNext()) {
				partner = partneritr.next();
				response = new HashMap<>();
				String shareFilePath = SPARCTechPackShareFileUtil.getShareFilePath(brand, partner, season);
				logger.debug("shareFilePath : {} for partner : {}", url, partner);
				shareFileLogEntry = SPARCShareFileAPICallLogEntry.builder()
						.flexTypePath("Log Entry\\scShareFileIntegrationLogEntry").apiCallType(APICALL_STRING)
						.season(season).plmProductNo(plmProductNo).specification(spec).partner(partner).build();
				shareFileLogEntry.setRequestTime(System.currentTimeMillis());
				if (FormatHelper.hasContent(shareFilePath)) {
					response = (new SPARCShareFileUtil()).initiateFileShare(outFilePath, shareFilePath,
							shareFileLogEntry);
				} else {
					response.put(404,
							"Error sending tech pack to the partner due to missing configuration in Business Object. Please resend the tech pack once ShareFile folder is configured in the business object");
				}

				shareFileLogEntry.setResponseTime(System.currentTimeMillis());
				mapResponseObjectForLogEntry(shareFileLogEntry, response);
				success = SPARCShareFileAPICallLogEntry.Status.SUCCESS.getResponseCode()
						.equals(shareFileLogEntry.getStatus().getResponseCode());
				new SPARCShareFileAPILogEntryService().log(shareFileLogEntry);

			}
		}

		return success;

	}

	private static void mapResponseObjectForLogEntry(SPARCShareFileAPICallLogEntry shareFileLogEntry,
			Map<Integer, String> response) {
		Integer responseCode = 500;
		String responseMessage = null;

		for (Map.Entry<Integer, String> entry : response.entrySet()) {
			responseCode = entry.getKey();
			responseMessage = entry.getValue();
		}

		switch (responseCode) {
		case 200:
			shareFileLogEntry.setStatus(SPARCShareFileAPICallLogEntry.Status.SUCCESS);
			break;
		case 400:
			shareFileLogEntry.setStatus(SPARCShareFileAPICallLogEntry.Status.BAD_REQUEST);
			break;
		case 401:
			shareFileLogEntry.setStatus(SPARCShareFileAPICallLogEntry.Status.UNAUTHORIZED);
			break;
		case 404:
			shareFileLogEntry.setStatus(SPARCShareFileAPICallLogEntry.Status.RESOURCE_NOT_FOUND);
			break;
		case 500:
		default:
			shareFileLogEntry.setStatus(SPARCShareFileAPICallLogEntry.Status.INTERNAL_ERROR);
			break;
		}

		shareFileLogEntry.setResponse(responseMessage);
	}

	private static String getUnZipPath(String url) {
		String folder = url.substring(0, url.lastIndexOf(File.separator)) + File.separator + "content";
		File outFile = new File(folder);
		outFile.mkdir();
		folder = outFile.getAbsolutePath();
		String outFilePath = url;
		try {
			SparcCostingUploadUtil.unzipContentsTo(url, folder);
			outFile = new File(folder);
			File[] files = outFile.listFiles();
			outFilePath = files[0].getAbsolutePath();
			logger.debug("Tech Pack created at local path: {}", outFilePath);
		} catch (IOException e) {
			logger.error("Unable to unzip contents of {} to the folder {} due to exception !", url, folder, e);
			e.printStackTrace();
		}

		return outFilePath;
	}

	private static String getTempFolderPath() {
		String folder = FileLocation.PDFDownloadLocationFiles + "temp";
		File outFile = new File(folder);
		outFile = FileRenamer.rename(outFile);
		outFile.mkdir();
		folder = outFile.getAbsolutePath();
		return folder;
	}
}
