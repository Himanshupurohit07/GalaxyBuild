package com.sparc.wc.migration.loader;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LineSheetQuery;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.specification.FlexSpecMaster;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecToSeasonLink;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;

import wt.httpgw.GatewayAuthenticator;
import wt.method.RemoteMethodServer;
import wt.util.WTException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.List;
import com.sparc.wc.util.SparcConstants;

public class SPARCFlexSpecExtract implements wt.method.RemoteAccess {

	public static String SEASON_BRANCHID = "LCSSEASON.BRANCHIDITERATIONINFO";
	public static String PRODUCT_BRANCHID = "LCSPRODUCT.BRANCHIDITERATIONINFO";
	public static String SPEC_BRANCHID = "FLEXSPECIFICATION.BRANCHIDITERATIONINFO";
	public static String SPEC_VR = "VR:com.lcs.wc.specification.FlexSpecification:";
	public static Logger logger = LogManager.getLogger(SPARCFlexSpecExtract.class.getName());

	/**
	 * 
	 * @param args0 user name
	 * @param args1 password
	 * @throws WTException
	 * @throws RemoteException
	 * @throws InvocationTargetException
	 * 
	 */
	public static void main(String[] args) {

		try {

			RemoteMethodServer rms = RemoteMethodServer.getDefault();
			String username = "wcadmin";
			GatewayAuthenticator auth = new GatewayAuthenticator();
			auth.setRemoteUser(username);
			rms.setAuthenticator(auth);
			String fileName = args[0];
			String mode = args[1].toLowerCase();
			String fromDate = null;
			if (mode.equalsIgnoreCase("delta"))
				fromDate = runDelta();
			@SuppressWarnings("rawtypes")
			Class[] argsTypes = { String.class, String.class };
			Object[] argsVal = { fileName, fromDate };
			rms.invoke("extractFlexSpec", SPARCFlexSpecExtract.class.getName(), null, argsTypes, argsVal);

		} catch (final Exception ex) {
			ex.getLocalizedMessage();
		}

	}

	public static String runDelta() {
		String fromDateStr = null;
		Scanner scanner = new Scanner(System.in);
		fromDateStr = promptAndValidateDate(scanner, "Please provide the from date (dd-MM-yyyy): ");

		System.out.println("Running in DELTA mode...");
		System.out.println("From Date: " + fromDateStr);

		return fromDateStr;
	}

	public static String promptAndValidateDate(Scanner scanner, String prompt) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		sdf.setLenient(false); // strict date validation

		while (true) {
			System.out.print(prompt);
			String input = scanner.nextLine();
			try {
				sdf.parse(input); // check if date is valid
				return input;
			} catch (ParseException e) {
				System.out.println("Invalid date format. Please enter the date in dd-MM-yyyy format.");
			}
		}
	}

	public static Timestamp convertToTimestamp(String dateStr) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		sdf.setLenient(false); // Strict validation
		Date date = sdf.parse(dateStr);
		return new Timestamp(date.getTime());
	}

	/**
	 * 
	 * @param args0 Date
	 * @throws WTException
	 * @implNote This Method is used to extract Specifications for Rebook and
	 *           generate the data file in text format
	 */
	@SuppressWarnings("unchecked")
	public static String extractFlexSpec(String fileName, String date) throws WTException {
		logger.debug("START EXTRACT FLEXSPEC >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		FlexType seasonRbkApparelType = FlexTypeCache.getFlexTypeFromPath("Season\\scSeasonReebok\\scRbkApparel");
		FlexType seasonRbkFootwearType = FlexTypeCache.getFlexTypeFromPath("Season\\scSeasonReebok\\scFootwear");
		List<List<String>> rows = new ArrayList<>();
		LCSSeason seasonObj = null;
		LCSProduct productObj = null;

		try {

			SearchResults seasonResults = getSeasons(seasonRbkApparelType, seasonRbkFootwearType);
			if (seasonResults.getResults() != null && seasonResults.getResults().size() > 0) {
				logger.debug("** No of seasons :" + seasonResults.getResults().size());
				for (FlexObject seasonFlexObj : (Collection<FlexObject>) seasonResults.getResults()) {
					seasonObj = (LCSSeason) LCSQuery
							.findObjectById(SparcConstants.SEASON_VR + seasonFlexObj.getString(SEASON_BRANCHID));
					if (seasonObj != null) {
						logger.debug("** Season name :" + seasonObj.getName());
						Collection<FlexObject> products = getProducts(null, seasonObj);
						if (products != null && products.size() > 0) {
							logger.debug(
									"** No of products in season - " + seasonObj.getName() + " :" + products.size());
							for (FlexObject productFlexObj : products) {
								productObj = (LCSProduct) LCSQuery.findObjectById(
										SparcConstants.PRODUCT_VR + productFlexObj.getString(PRODUCT_BRANCHID));
								if (productObj != null) {
									logger.debug("** Product name :" + productObj.getName());
									findSourceandSpec(productObj, seasonObj, rows, date);
								} else {
									logger.debug("** Unabale to find product using Branch ID, product Object is null ");
								}
							}
						} else {
							logger.debug("** No products found in this season ");
						}

					} else {
						logger.debug("** Unabale to find season using Branch ID, season Object is null ");
					}
				}
				logger.debug("** Extarcted Spec Data ---------- " + rows);

			} else {
				logger.debug("** No season is found ");
			}
			if (rows != null && rows.size() > 0) {
				createDataFile(rows, fileName);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug("END EXTRACT FLEXSPEC >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		return "";

	}

	/**
	 * 
	 * @throws WTException
	 * @implNote This Method is used to get Rebook seasons using prepared query
	 *           statement
	 */
	public static SearchResults getSeasons(FlexType seasonType1, FlexType seasonType2) throws WTException {
		logger.debug("START - Prepared Query for season >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		SearchResults results = null;
		try {

			PreparedQueryStatement pqs = new PreparedQueryStatement();
			pqs.appendFromTable("LCSSeason");
			pqs.appendSelectColumn("LCSSeason", "IDA2A2");
			pqs.appendSelectColumn("LCSSeason", "branchIditerationInfo");
			pqs.appendCriteria(new Criteria("LCSSeason", SparcConstants.LATEST_ITERATION_INFO, "1", Criteria.EQUALS));
			pqs.appendAndIfNeeded();
			pqs.appendOpenParen();
			pqs.appendCriteria(
					new Criteria("LCSSeason", "flexTypeIdPath", seasonType1.getTypeIdPath(), Criteria.EQUALS));
			pqs.appendOrIfNeeded();
			pqs.appendCriteria(
					new Criteria("LCSSeason", "flexTypeIdPath", seasonType2.getTypeIdPath(), Criteria.EQUALS));
			pqs.appendClosedParen();
			results = LCSQuery.runDirectQuery(pqs);
			logger.debug("**Query Result : " + results);

		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug("END - Prepared Query for season >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		return results;

	}

	/**
	 * @param args0 criteria Map
	 * @param args1 season object
	 * @throws WTException
	 * @implNote This Method is used to get all products in a season using season
	 *           object as parameter
	 */
	@SuppressWarnings("unchecked")
	public static Collection<FlexObject> getProducts(Map<String, String> criteria, LCSSeason seasonObj)
			throws WTException {
		logger.debug("START - Get products for season >>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		Collection<FlexObject> productData = new ArrayList<FlexObject>();
		LineSheetQuery lineSheetQuery = new LineSheetQuery();

		// Report Criteria
		boolean skus = false;
		boolean sourcing = false;
		boolean linePlanSourcing = false;
		boolean activeCostSheets = false;
		boolean whatIfCostSheets = false;
		boolean placeholders = false;
		@SuppressWarnings("rawtypes")
		Collection seasonGroupIds = null;

		try {

			productData = lineSheetQuery.runSeasonProductReport(null, // LCSProduct product
					seasonObj, // LCSSeason season
					null, // LCSSourcingConfig config
					skus, // boolean skus
					criteria, // Map criteria
					true, // boolean postProcessing
					null, // String materialGroupId
					sourcing, // boolean sourcing
					linePlanSourcing, // boolean secondarySourcing
					!activeCostSheets, // boolean primaryCostOnly
					whatIfCostSheets, // boolean whatifCosts
					null, // Collection usedAttKeys
					false, // boolean includeRemoved
					placeholders, // boolean includePlaceHolders
					seasonGroupIds, // Collection seasonGroupIds
					true, // Include cost spec (only applied if sourcing is true)
					null, // cost Spec which is not filtered out here
					null // PreparedStatement
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug("END - Get products for season >>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		return productData;

	}

	/**
	 * @param args0 product Object
	 * @param args1 season object
	 * @param args2 Array List
	 * @throws WTException
	 * @implNote This Method is used to get source and specification for product
	 *           season combination
	 */
	@SuppressWarnings("unchecked")
	public static List<List<String>> findSourceandSpec(LCSProduct product, LCSSeason season, List<List<String>> rows,
			String date) throws WTException {
		logger.debug("START - Find Source and Spec for Product Season >>>>>>>>>>>>>>>>>");

		LCSSourcingConfig sourceObj = null;
		FlexSpecification flexSpecObj = null;
		FlexSpecToSeasonLink fstsl = null;
		LCSSupplier vendorObj = null;

		try {

			Collection<LCSSourcingConfig> sources = LCSSourcingConfigQuery.getSourcingConfigForProductSeason(product,
					season);
			if (sources != null && sources.size() > 0) {
				logger.debug("** No of sources in product - " + product.getName() + " :" + sources.size());
				for (LCSSourcingConfig srcConfigTemp : sources) {
					sourceObj = srcConfigTemp;
					if (sourceObj != null) {
						logger.debug("** Source name :" + sourceObj.getName());
						// vendorObj = (LCSSupplier) sourceObj.getValue("vendor");
						// if (vendorObj != null) {
						SearchResults specResult = FlexSpecQuery.findExistingSpecs(product, season, sourceObj);
						if (specResult.getResults() != null && specResult.getResults().size() > 0) {
							logger.debug("** No of specs in source - " + sourceObj.getName() + " :"
									+ specResult.getResults().size());
							for (FlexObject specFlexObj : (Collection<FlexObject>) specResult.getResults()) {
								flexSpecObj = (FlexSpecification) LCSQuery
										.findObjectById(SPEC_VR + specFlexObj.getString(SPEC_BRANCHID));
								if (flexSpecObj != null) {
									if (date != null) {
										Timestamp fromDate = convertToTimestamp(date);
										Timestamp specmodifyDate = flexSpecObj.getModifyTimestamp();
										if (specmodifyDate.after(fromDate)) {
											logger.debug("** Spec name :" + flexSpecObj.getName());
											// Get spec to Season Link obj
											fstsl = FlexSpecQuery.findSpecToSeasonLink(
													(FlexSpecMaster) flexSpecObj.getMaster(),
													(LCSSeasonMaster) season.getMaster());
											if (fstsl != null) {
												extarctData(product, season, sourceObj, flexSpecObj, fstsl, rows);
											} else {
												logger.debug("** Unabale to find spec to Season Link obj ");
											}
										}
									} else {
										logger.debug("** Spec name :" + flexSpecObj.getName());
										// Get spec to Season Link obj
										fstsl = FlexSpecQuery.findSpecToSeasonLink(
												(FlexSpecMaster) flexSpecObj.getMaster(),
												(LCSSeasonMaster) season.getMaster());
										if (fstsl != null) {
											extarctData(product, season, sourceObj, flexSpecObj, fstsl, rows);
										} else {
											logger.debug("** Unabale to find spec to Season Link obj ");
										}

									}

								} else {
									logger.debug("** Unabale to find spec using BranchID, spec Object is null ");
								}
							}
						} else {
							logger.debug("** No spec found for product, season and source combination");
						}
						/*
						 * } else { logger.
						 * debug("** No vendor association in  source, source will bot be considered ");
						 * }
						 */

					} else {
						logger.debug("** Unabale to find source, source Object is null ");
					}
				}
			} else {
				logger.debug("** No sources found for product season");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug("END - Find Source and Spec for Product Season >>>>>>>>>>>>>>>>>>>");

		return rows;

	}

	/**
	 * @param args0 product Object
	 * @param args1 season object
	 * @param args2 source object
	 * @param args3 specification object
	 * @param args4 Array List
	 * @throws WTException
	 * @implNote This Method is used to Extract data for specification load
	 */
	public static List<List<String>> extarctData(LCSProduct product, LCSSeason season, LCSSourcingConfig source,
			FlexSpecification flexSpec, FlexSpecToSeasonLink fstsl, List<List<String>> rows) throws WTException {
		logger.debug("START - Extracting the Data >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		String FlexSpecflexAttscSpecStatus = "";
		String FlexSpecflexAttscDescription = "";
		String FlexSpecflexAttscComments = "";
		String FlexSpecflexAttspecName = "";
		String FlexSpecflexAttptcspecificationNumber = "";
		String FlexSpecflexAttsspecMasterID = "";
		String sourceMasterId = "";

		try {

			String SeasonflexAttseasonName = season.getName();
			String seasonType = season.getFlexType().getFullName(true);
			String ProductflexAttscPLMProductNo = Long.toString((long) product.getValue("scPLMProductNo"));
			String ProductType = product.getFlexType().getFullName(true);
			String sourceType = source.getFlexType().getFullName(true);
			String flexAttscSourcingNo = Long.toString((long) source.getValue("scSourcingNo"));
			// String flexAttscPLMVendorSupplierNo = getVendorNumber(source);
			if (flexSpec.getValue("ptcspecificationNumber") != null)
				FlexSpecflexAttptcspecificationNumber = (String) flexSpec.getValue("ptcspecificationNumber");
			if (flexSpec.getValue("specName") != null)
				FlexSpecflexAttspecName = (String) flexSpec.getValue("specName");
			if (flexSpec.getValue("scSpecStatus") != null)
				FlexSpecflexAttscSpecStatus = (String) flexSpec.getValue("scSpecStatus");
			if (flexSpec.getValue("scDescription") != null)
				FlexSpecflexAttscDescription = (String) flexSpec.getValue("scDescription");
			if (flexSpec.getValue("scComments") != null)
				FlexSpecflexAttscComments = (String) flexSpec.getValue("scComments");
			String isPrimarySpecStr = Boolean.toString(fstsl != null && fstsl.isPrimarySpec());
			// String FlexSpecflexAttsspecBranchIDSource = Long.toString((long)
			// flexSpec.getBranchIdentifier());
			if (flexSpec.getMaster() != null)
				FlexSpecflexAttsspecMasterID = FormatHelper.getNumericObjectIdFromObject(flexSpec.getMaster());
			if (source.getMaster() != null)
				sourceMasterId = FormatHelper.getNumericObjectIdFromObject(source.getMaster());
			/*
			 * String FlexSpecflexAttsscResendTechPack = "false";
			 * if(flexSpec.getValue("scResendTechPack") != null)
			 * FlexSpecflexAttsscResendTechPack = Boolean.toString((boolean)
			 * flexSpec.getValue("scResendTechPack"));
			 */

			rows.add(List.of("FlexSpecification", SeasonflexAttseasonName, seasonType, ProductflexAttscPLMProductNo,
					ProductType, sourceType, flexAttscSourcingNo, sourceMasterId, FlexSpecflexAttsspecMasterID,
					FlexSpecflexAttptcspecificationNumber, FlexSpecflexAttspecName, FlexSpecflexAttscSpecStatus,
					FlexSpecflexAttscDescription, FlexSpecflexAttscComments, isPrimarySpecStr));

		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.debug("END - Extracting the Data >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		return rows;

	}

	/**
	 * @param args1 source object
	 * @throws WTException
	 * @implNote This Method is used to get Vendor number from source
	 */
	public static String getVendorNumber(LCSSourcingConfig source) throws WTException {
		logger.debug("START - getVendorNumber >>>>>>>>>>>>>>>>>>>>>>>>");
		String vendorNo = "";
		try {

			LCSSupplier vendor = (LCSSupplier) source.getValue("vendor");
			vendorNo = Long.toString((long) vendor.getValue("scPLMVendorSupplierNo"));
			logger.debug("** vendorNo>>>>>>>>>" + vendorNo);

		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.debug("END - getVendorNumber >>>>>>>>>>>>>>>>>>>>>>>>");

		return vendorNo;

	}

	/**
	 * 
	 * @param args1 Array List
	 * @param args2 file path
	 * @throws WTException
	 * @implNote This Method is used to create data file for specification load
	 */
	public static void createDataFile(List<List<String>> rows, String filePath) throws WTException {
		logger.debug("START - Creating  the Data File >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			for (List<String> row : rows) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < row.size(); i++) {
					sb.append(row.get(i));
					if (i < row.size() - 1) {
						sb.append('\t'); // Add tab between values
					}
				}
				writer.write(sb.toString());
				writer.newLine(); // Move to next line
			}
			logger.debug("Data File written successfully at " + filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.debug("END - Creating  the Data File >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

	}

}
