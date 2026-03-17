package com.acn.loaders;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.document.LCSDocumentHelper;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.specification.FlexSpecLogic;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;

import wt.fc.PersistenceHelper;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTPrincipal;
import wt.session.SessionHelper;
import wt.util.WTException;

public class LinkImagePageToSpec implements RemoteAccess {

	public static Logger log = LogManager.getLogger(LinkImagePageToSpec.class.getName());

	public static String SEASON_VR = "VR:com.lcs.wc.season.LCSSeason:";
	public static String partMaster_OR = "OR:com.lcs.wc.part.LCSPartMaster:";
	public static String PRODUCT_VR = "VR:com.lcs.wc.product.LCSProduct:";
	public static String LATEST_ITERATION_INFO = "latestIterationInfo";
	public static String SEASON_BRANCHID = "LCSSEASON.BRANCHIDITERATIONINFO";
	public static String PRODUCT_BRANCHID = "LCSPRODUCT.BRANCHIDITERATIONINFO";
	public static String SPEC_BRANCHID = "FLEXSPECIFICATION.BRANCHIDITERATIONINFO";
	public static String SPEC_VR = "VR:com.lcs.wc.specification.FlexSpecification:";

	// local
	// private static String csv_file_path =
	// "C:\\PTC\\Extraction\\VisualAssets\\ImagePageCSVFile.csv";
	// galaxyurl
	// private static String csv_file_path =
	// "/appl/tmp/Migration/Document/csv/ImagePageCSVFile.csv";
	private static String csv_file_path = null;
	private static String USERNAME = null;
	private static String PASSWORD = null;

	public static void main(String[] args) {

		for (int i = 0; i < args.length; ++i) {
			if (args[i].equalsIgnoreCase("-help")) {
				System.out
						.println("Usage\t-\tjava com.lcs.wc.load.ExtractMaterial -f FileName -d Delimiter -t TypeName");
				System.out.println(
						"\t-f\tSpecifies The FileName To Extract To. If Not Specified, Prints On To The Consile.");
				System.out.println("\t-d\tSpecifies The Delimiter To Use. If Not Specified, Comma (',') Is Used.");
				System.out.println("\t-t\tSpecifies The Type To Use. If Not Specified, The Root Type Is Used.");
				System.exit(0);
			}

			if (i + 1 > args.length - 1) {
				System.out.println("Please Specify A Parameter For Argument " + args[i]);
				System.exit(0);
			}

			if (args[i].equalsIgnoreCase("-f")) {
				++i;
				csv_file_path = args[i];
			} else if (args[i].equalsIgnoreCase("-u")) {
				++i;
				USERNAME = args[i];
			} else if (args[i].equalsIgnoreCase("-p")) {
				++i;
				PASSWORD = args[i];
			} else {
				System.out.println("The Argument " + args[i] + " Is Not Supported !!!");
				System.exit(0);
			}
		}

		try {
			// Important: Authenticate the Windchill user session
			SessionHelper.manager.setAuthenticatedPrincipal(USERNAME);

			// Optional: Confirm
			WTPrincipal user = SessionHelper.manager.getPrincipal();
			System.out.println("Current Windchill user: " + user.getName());
			System.out.println("csv file path: " + csv_file_path);

			System.out.println("inside main method");
			RemoteMethodServer rms = RemoteMethodServer.getDefault();
			rms.setUserName(USERNAME);
			rms.setPassword(PASSWORD);
			@SuppressWarnings("rawtypes")
			Class[] argsTypes = { String.class };
			Object[] argsVal = { csv_file_path };
			rms.invoke("readCSVAndLinkImages", LinkImagePageToSpec.class.getName(), null, argsTypes, argsVal);
			System.out.println("exit main method");
		} catch (Exception ex) {
			ex.getLocalizedMessage();
		}

	}

	@SuppressWarnings("unchecked")
	public static String readCSVAndLinkImages(String csvFilePath) throws WTException {
		csv_file_path = csvFilePath;
		String logFilePath = createLogFilePath(csv_file_path);
		// comparing objects from csv

		try (Reader reader = new FileReader(csv_file_path, StandardCharsets.UTF_8);
				CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withDelimiter(',') // coma-delimited
						.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
			HashMap<String, LCSSeason> seasonMap = new HashMap<>();
			HashMap<String, LCSProduct> productMap = new HashMap<>();
			HashMap<String, LCSSourcingConfig> sourceMap = new HashMap<>();
			HashMap<String, SearchResults> specMap = new HashMap<>();

			for (CSVRecord record : csvParser) {
				// System.out.println("CSV Reading Started");
				log.info("CSV Reading Started");
				try {
					String csvSeasonName = record.get("Season Name");
					String csvProductRefId = record.get("PLM Product No");
					String csvProductName = record.get("Product Name");
					String csvColorwayId = record.get("Colorway No");
					// String csvColorwayName = record.get("Colorway Name");
					String csvSourceConfigNo = record.get("Sourcing Config No");
					// String csvSourceName = record.get("Source Name");
					String csvSpecificationId = record.get("Specification Id");
					// String csvSpecificationName = record.get("Specification Name");
					// String csvDocumentReferenceId = record.get("Document Reference");
					String csvDocumentMasterId = record.get("Document Master Id");
					// String csvDocumentName = record.get("Document Name");
					// String csvDocumentUrl = record.get("Document Url");

					LCSSeason season = null;
					LCSProduct product = null;
					LCSSourcingConfig source = null;

					// season
					if (!seasonMap.containsKey(csvSeasonName)) {

						season = new LCSSeasonQuery().findSeasonByNameType(csvSeasonName, null);
						seasonMap.put(csvSeasonName, season);
					} else {
						season = seasonMap.get(csvSeasonName);
					}

					// product
					if (!productMap.containsKey(csvProductName)) {

						product = new LCSProductQuery().findProductByNameType(csvProductName, null);
						productMap.put(csvProductName, product);
					} else {
						product = productMap.get(csvProductName);
					}

					// source
					if (!sourceMap.containsKey(csvSourceConfigNo)) {

						source = getSource(product, csvSourceConfigNo);
						sourceMap.put(csvSourceConfigNo, source);
					} else {
						source = sourceMap.get(csvSourceConfigNo);
					}

					// Specs

					SearchResults SpecFlexObjects = FlexSpecQuery.findExistingSpecs(product, season, source);

					Collection<FlexSpecification> specs = LCSQuery.getObjectsFromResults(SpecFlexObjects,
							"VR:com.lcs.wc.specification.FlexSpecification:",
							"FLEXSPECIFICATION.BRANCHIDITERATIONINFO");
					Iterator var37 = specs.iterator();

					while (var37.hasNext()) {
						FlexSpecification flexSpec = (FlexSpecification) var37.next();
						FlexSpecLogic specLogic = new FlexSpecLogic();

						if (flexSpec != null) {
							// System.out.println("Spec Found!");
							String specMasterId = String.valueOf(flexSpec.getValue("glSpecMasterId"));
							if (specMasterId.equals(csvSpecificationId)) {

								LCSDocument lcsDocument = findDocumentByMasterId(csvDocumentMasterId);

								if (lcsDocument != null) {
									// System.out.println("Document found!");
									log.info("Document Found!");

									lcsDocument = (LCSDocument) VersionHelper.latestIterationOf(lcsDocument);
									if (!VersionHelper.isCheckedOut(lcsDocument)) {
										lcsDocument = (LCSDocument) VersionHelper.checkout(lcsDocument);
									}

									String productLcsPartMasterId = getProductLCSPartMasterNumber(csvProductRefId);

									lcsDocument.setValue("ownerReference",
											(productLcsPartMasterId != null && !productLcsPartMasterId.isEmpty())
													? partMaster_OR + productLcsPartMasterId
													: null);

									lcsDocument.setValue("skuMasterReference",
											(csvColorwayId != null && !csvColorwayId.isEmpty())
													? partMaster_OR + getColorwayLCSPartMasterNumber(csvColorwayId)
													: null);

									LCSDocumentHelper.service.checkIn(lcsDocument);

									log.info("Adding document into spec- started!");
									specLogic.addComponentToSpec(flexSpec,
											(LCSDocument) PersistenceHelper.manager.refresh(lcsDocument));
									log.info("Document added into spec- ended!");

									// System.out.println("log file code started");
									writeLogToFile(logFilePath, "Linked Document (Image Page) " + csvDocumentMasterId
											+ " to Spec " + csvSpecificationId);
								}
							}
						}
					}

				}

				catch (Exception e) {
					// TODO: handle exception
					writeLogToFile(logFilePath,
							"Error processing record: " + record.toString() + " | " + e.getMessage());
					e.printStackTrace();
				}

			}

		} catch (Exception e) {
			writeLogToFile(createLogFilePath(csv_file_path), "Fatal Error: " + e.getMessage());
			e.printStackTrace();
		}

		return "";
	}

	private static String createLogFilePath(String csvFilePath) {
		File csvFile = new File(csvFilePath);
		String parentDir = csvFile.getParent();
		String fileName = csvFile.getName().replace(".csv", "_log.txt");
		return parentDir + File.separator + fileName;
	}

	private static void writeLogToFile(String logFilePath, String message) {
		try (FileWriter fw = new FileWriter(logFilePath, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {

			String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
			out.println(timestamp + " - " + message);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static LCSDocument findDocumentByMasterId(String csvDocumentMasterId) {

		LCSDocument document = null;

		try {
			FlexType flexType = FlexTypeCache
					.getFlexTypeFromPath("Document\\Images Page\\scImagePageReebok\\scVendorImages");

			String columnName = flexType.getAttribute("glDocumentMasterId").getColumnName();

			PreparedQueryStatement statement = new PreparedQueryStatement();
			statement.appendSelectColumn("LCSDocument", "IDA2A2");
			statement.appendFromTable("LCSDocument");
			// statement.appendSelectColumn(new QueryColumn("LCSDocument",
			// "thePersistInfo.theObjectIdentifier.id"));
//			statement.appendJoin(new QueryColumn(LCSDocument.class, "masterReference.key.id"),
//					new QueryColumn(WTDocumentMaster.class, "thePersistInfo.theObjectIdentifier.id"));
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria("LCSDocument", columnName, csvDocumentMasterId, Criteria.EQUALS));
			statement.appendAndIfNeeded();
//			statement.appendCriteria(new Criteria(new QueryColumn(LCSDocument.class, "checkoutInfo.state"), "wrk", "<>"));
//			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria("LCSDocument", "latestIterationInfo", "1", Criteria.EQUALS));

			SearchResults results = LCSQuery.runDirectQuery(statement);
			// System.out.println("Document query by glDocumentMasterId:
			// "+results.getResultsFound());
			if (results.getResultsFound() > 0) {
				FlexObject obj = (FlexObject) results.getResults().elementAt(0);
				document = (LCSDocument) LCSQuery
						.findObjectById("OR:com.lcs.wc.document.LCSDocument:" + obj.getString("LCSDOCUMENT.IDA2A2"));
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		return document;
	}

	private static LCSSourcingConfig getSource(LCSProduct prodARev, String sourceMasterID) throws WTException {

		// System.out.println("----getSource---");
		LCSSourcingConfig srcConfig = null;
		FlexType sourceType = prodARev.getFlexType().getReferencedFlexType("SOURCING_CONFIG_TYPE_ID");
		String sourceMasterAttCol = sourceType.getAttribute("glSourceMasterID").getColumnName();
		PreparedQueryStatement pqs = new PreparedQueryStatement();
		pqs.appendSelectColumn("LCSSourcingConfig", "branchIditerationInfo");
		pqs.appendFromTable("LCSSourcingConfig");
		pqs.appendCriteria(new Criteria("LCSSourcingConfig", "productARevId",
				FormatHelper.getNumericVersionIdFromObject(prodARev), Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSSourcingConfig", "latestIterationInfo", "1", Criteria.EQUALS));
		pqs.appendAndIfNeeded();
		pqs.appendCriteria(new Criteria("LCSSourcingConfig", sourceMasterAttCol, sourceMasterID, Criteria.EQUALS));

		SearchResults srcResults = LCSQuery.runDirectQuery(pqs);

		Collection srcColl = srcResults.getResults();

		if (srcColl != null && srcColl.size() == 1) {
			FlexObject fob = (FlexObject) srcColl.iterator().next();
			srcConfig = (LCSSourcingConfig) LCSQuery.findObjectById("VR:com.lcs.wc.sourcing.LCSSourcingConfig:"
					+ fob.getString("LCSSOURCINGCONFIG.BRANCHIDITERATIONINFO"));
		}

		return srcConfig;

	}

	@SuppressWarnings("unchecked")
	private static String getColorwayLCSPartMasterNumber(String csvColorwayId) {

		String masterRef = null;

		try {
			PreparedQueryStatement statement = new PreparedQueryStatement();
			statement.setDistinct(true);
			statement.appendFromTable("LCSSKU");

			// Select idA3masterReference column
			statement.appendSelectColumn("LCSSKU", "idA3masterReference");

			// WHERE clause
			statement.appendCriteria(new Criteria("LCSSKU", LATEST_ITERATION_INFO, "1", Criteria.EQUALS));
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria("LCSSKU", "ptc_lng_2typeInfoLCSSKU", csvColorwayId, Criteria.EQUALS));

			// LIMIT 1
			statement.setQueryLimit(1);

			SearchResults results = LCSQuery.runDirectQuery(statement);
			// System.out.println("query is: " + results);

			if (results != null && results.getResults().size() > 0) {
				for (FlexObject resultsObj : (Collection<FlexObject>) results.getResults()) {

					masterRef = (String) resultsObj.getString("LCSSKU.IDA3MASTERREFERENCE");
					System.out.println("masterReference for " + csvColorwayId + " = " + masterRef);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return masterRef;
	}

	@SuppressWarnings("unchecked")
	private static String getProductLCSPartMasterNumber(String plmProductNo) {

		String masterRef = null;

		try {
			PreparedQueryStatement statement = new PreparedQueryStatement();
			statement.setDistinct(true);
			statement.appendFromTable("LCSProduct");

			// Select idA3masterReference column
			statement.appendSelectColumn("LCSProduct", "idA3masterReference");

			// WHERE clause
			statement.appendCriteria(new Criteria("LCSProduct", LATEST_ITERATION_INFO, "1", Criteria.EQUALS));
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria("LCSProduct", "ptc_lng_2typeInfoLCSProduct", plmProductNo, Criteria.EQUALS));

			// LIMIT 1
			statement.setQueryLimit(1);

			SearchResults results = LCSQuery.runDirectQuery(statement);
			// System.out.println("query is: " + results);

			if (results != null && results.getResults().size() > 0) {
				for (FlexObject resultsObj : (Collection<FlexObject>) results.getResults()) {

					masterRef = (String) resultsObj.getString("LCSPRODUCT.IDA3MASTERREFERENCE");
					// System.out.println("masterReference for " + plmProductNo + " = " +
					// masterRef);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return masterRef;
	}

}
