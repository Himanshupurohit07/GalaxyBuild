package com.acn.loaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import com.lcs.wc.color.LCSColor;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.document.LCSDocumentHelper;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.material.LCSMaterialMaster;
import com.lcs.wc.material.LCSMaterialSupplier;
import com.lcs.wc.material.LCSMaterialSupplierQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.sample.LCSSample;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.supplier.LCSSupplierMaster;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import wt.fc.WTObject;
import wt.httpgw.GatewayAuthenticator;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.util.WTException;

public class FlexDocumentLinkUtility implements RemoteAccess {


	public static void main(String[] args) {

		try {

			System.out.println(" ------ FlexDocumentLinkUtility Called ------");

			RemoteMethodServer rms = RemoteMethodServer.getDefault();
			GatewayAuthenticator auth = new GatewayAuthenticator();
			String username = args[0];
			String password = args[1];
			auth.setRemoteUser(username);
			auth.setRemoteUser(password);
			String outputFolderPath = args[2];
			System.out.println(" OutputFolderPath: " + outputFolderPath);
			Class[] argsTypes = { String.class };
			Object[] argsVal = { outputFolderPath };

			System.out.println(" ------- Start FlexDocumentLinkUtility -------");
			rms.invoke("associateDocumentsWithSuppliersFromCSV", FlexDocumentLinkUtility.class.getName(), null,
					argsTypes, argsVal);
			System.out.println("------- Linking is done -------");
		} catch (final Exception ex) {
			ex.getLocalizedMessage();
		}
	}


	public static void associateDocumentsWithSuppliersFromCSV(String folderPath) {
		System.out.println("Reading CSV files from before: " + folderPath);

		// Normalize folder path
		if (!folderPath.endsWith("/") && !folderPath.endsWith("\\")) {
			folderPath = folderPath + File.separator;
		}

		System.out.println("Reading CSV files from after: " + folderPath);

		// File names and types
		Map<String, String[]> csvFileInfo = new LinkedHashMap<>();
		csvFileInfo.put("Supplier_references.csv",
				new String[] { "scDocumentReferenceNumber", "scPLMVendorSupplierNo", "Supplier Name", "supplier" });
		csvFileInfo.put("Material_references.csv",
				new String[] { "scDocumentReferenceNumber", "scMaterialNo", "Material Name", "material" });
		csvFileInfo.put("Color_references.csv",
				new String[] { "scDocumentReferenceNumber", "scColorNo", "Color Name", "color" });
		csvFileInfo.put("Sample_references.csv",
				new String[] { "scDocumentReferenceNumber", "scSampleNo", "Sample Name", "sample" });

		// Process standard CSVs
		for (Map.Entry<String, String[]> entry : csvFileInfo.entrySet()) {
			String filename = entry.getKey();
			String[] args = entry.getValue();
			String fullPath = folderPath + filename;

			File file = new File(fullPath);
			if (file.exists()) {
				processCSV(fullPath, args[0], args[1], args[2], args[3]);
			} else {
				System.out.println("Skipping missing file: " + fullPath);
			}
		}

		// Process custom/complex CSVs
		handleMaterialSupplierCSV(folderPath);
		handleProductSeasonCSV(folderPath);
		handleBusinessObjectCSV(folderPath);
		handleColorwayObjectCSV(folderPath);
		handleFlexSpecificationObjectCSV(folderPath);
	}

	private static void handleMaterialSupplierCSV(String folderPath) {
		File file = new File(folderPath, "MaterialSupplier_references.csv");
		if (file.exists()) {
			processMaterialSupplierCSV(file.getAbsolutePath());
		} else {
			System.out.println("Skipping missing file: " + file.getAbsolutePath());
		}
	}

	private static void handleProductSeasonCSV(String folderPath) {

		File file = new File(folderPath, "Product_references.csv");
		if (file.exists()) {
			processProductSeasonCSV(file.getAbsolutePath());
		} else {
			System.out.println("Skipping missing file: " + file.getAbsolutePath());
		}

	}

	private static void handleBusinessObjectCSV(String folderPath) {
		File file = new File(folderPath, "BusinessObject_references.csv");
		if (file.exists()) {
			processBusinessObjectCSV(file.getAbsolutePath());
		} else {
			System.out.println("Skipping missing file: " + file.getAbsolutePath());
		}

	}

	private static void handleColorwayObjectCSV(String folderPath) {

		File file = new File(folderPath, "Colorway_references.csv");
		if (file.exists()) {
			processColorwayObjectCSV(file.getAbsolutePath());
		} else {
			System.out.println("Skipping missing file: " + file.getAbsolutePath());
		}
	}

	private static void handleFlexSpecificationObjectCSV(String folderPath) {

		File file = new File(folderPath, "FlexSpecification_references.csv");
		if (file.exists()) {
			processFlexSpecificationObjectCSV(file.getAbsolutePath());
		} else {
			System.out.println("Skipping missing file: " + file.getAbsolutePath());
		}
	}
	private static void processCSV(String csvPath, String docCol, String objectCol, String nameCol, String type) {
		try (Reader reader = new InputStreamReader(new FileInputStream(csvPath), StandardCharsets.UTF_8);
				CSVParser csvParser = new CSVParser(reader,
						CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim().withQuote('"'))) {
			for (CSVRecord record : csvParser) {
				String docRef = record.get(docCol).trim();
				String objectCode = record.get(objectCol).trim();
				String objectName = record.get(nameCol).trim();
				System.out.println(
						"docRef :: " + docRef + " objectCode :: " + objectCode + " objectName :: " + objectName);
				try {
					LCSDocument document = findDocumentByReferenceNumber(docRef);
					WTObject targetObject = null;

					if ("supplier".equalsIgnoreCase(type)) {
						targetObject = (LCSSupplier) findSupplierByNumber(objectCode);
					} else if ("material".equalsIgnoreCase(type)) {
						targetObject = (LCSMaterial) findMaterialByNumber(objectCode);
					} else if ("color".equalsIgnoreCase(type)) {
						targetObject = (LCSColor) findColorByNumber(objectCode);
					} else if ("sample".equalsIgnoreCase(type)) {
						targetObject = (LCSSample) findSampleByNumber(objectCode);
					}

					if (document != null && targetObject != null) {
						String targetOid = FormatHelper.getObjectId(targetObject);
						Vector<String> docIds = new Vector<>();
						docIds.add(FormatHelper.getObjectId(document.getMaster()));
						LCSDocumentHelper.service.associateDocuments(targetOid, docIds);

						System.out.printf("Linked Doc [%s] with %s [%s | %s]%n", docRef, capitalize(type), objectName,
								objectCode);
					} else {
						System.err.printf("Not Found -> Doc: %s, %s: %s%n", docRef, capitalize(type), objectCode);
					}
				} catch (Exception e) {
					System.err.printf("Error processing docRef %s for %s: %s%n", docRef, type, e.getMessage());
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.err.println("Failed to read CSV: " + csvPath);
			e.printStackTrace();
		}
	}

	public static void processBusinessObjectCSV(String csvPath) {
		System.out.println(" ----- Inside processBusinessObjectCSV ----- ");

		try (Reader reader = new InputStreamReader(new FileInputStream(csvPath), StandardCharsets.UTF_8);
				CSVParser csvParser = new CSVParser(reader,
						CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim().withQuote('"'))) {

			for (CSVRecord record : csvParser) {
				String docRef = record.get("scDocumentReferenceNumber").trim();
				String businessObjectName = record.get("BusinessObjectName").trim();
				String flexType = record.get("FlexTypeName").trim();

				try {
					// Find document
					LCSDocument document = findDocumentByReferenceNumber(docRef);
					if (document == null) {
						System.err.printf("Document not found for reference: %s%n", docRef);
						continue;
					}

					// Find business object
					LCSLifecycleManaged businessObj = findBusinessObjectByName(businessObjectName);
					if (businessObj == null) {
						System.err.printf("BusinessObject not found: %s%n", businessObjectName);
						continue;
					}

					String businessObjOid = FormatHelper.getObjectId(businessObj);
					Vector<String> docIds = new Vector<>();
					docIds.add(FormatHelper.getObjectId(document.getMaster()));

					LCSDocumentHelper.service.associateDocuments(businessObjOid, docIds);
					System.out.printf("Linked Document [%s] to Business Object [%s] (Type: %s)%n", docRef,
							businessObjectName, flexType);

				} catch (Exception e) {
					System.err.printf("Error linking docRef %s to BusinessObject %s: %s%n", docRef, businessObjectName,
							e.getMessage());
					e.printStackTrace();
				}
			}

		} catch (IOException e) {
			System.err.println("Failed to read CSV: " + csvPath);
			e.printStackTrace();
		}
	}

	public static LCSLifecycleManaged findBusinessObjectByName(String businessObjectName) throws WTException {
		PreparedQueryStatement stmt = new PreparedQueryStatement();
		stmt.appendFromTable("LCSLifecycleManaged");
		stmt.appendSelectColumn(new QueryColumn("LCSLifecycleManaged", "IDA2A2"));
//		stmt.appendCriteria(new Criteria("LCSLifecycleManaged", "ptc_str_1typeInfoLCSLifecycl", businessObjectName,
//		Criteria.EQUALS));
		String attColumn = FlexTypeCache.getFlexTypeFromPath("Business Object").getAttribute("name").getColumnName();
		System.out.println(" expected : ptc_str_1typeInfoLCSLifecycl : "+attColumn);
		stmt.appendCriteria(new Criteria("LCSLifecycleManaged", attColumn, businessObjectName, Criteria.EQUALS));
		LCSQuery.printStatement(stmt);
		Collection<?> results = LCSQuery.runDirectQuery(stmt).getResults();
		if (!results.isEmpty()) {
			FlexObject fo = (FlexObject) results.iterator().next();
			String objId = fo.getString("LCSLIFECYCLEMANAGED.IDA2A2");
			LCSLifecycleManaged lifeCycle = (LCSLifecycleManaged) LCSQuery
					.findObjectById("com.lcs.wc.foundation.LCSLifecycleManaged:" + objId);
			return lifeCycle;
		}
		return null;
	}

	public static void processMaterialSupplierCSV(String csvPath) {
		System.out.println("inside the processMaterialSupplierCSV ");
		try (Reader reader = new InputStreamReader(new FileInputStream(csvPath), StandardCharsets.UTF_8);
				CSVParser csvParser = new CSVParser(reader,
						CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim().withQuote('"'))) {
			for (CSVRecord record : csvParser) {
				String docRef = record.get("scDocumentReferenceNumber").trim();
				String materialNo = record.get("Material No").trim();
				String supplierNo = record.get("Supplier No").trim();
				String linkName = record.get("MaterialSupplier Name").trim(); // Optional for log

				try {
					LCSDocument document = findDocumentByReferenceNumber(docRef);
					LCSMaterialSupplier materialSupplier = findMaterialSupplierByNumber(materialNo, supplierNo);

					if (document == null) {
						System.err.printf("Document not found for reference: %s%n", docRef);
						continue;
					}

					if (materialSupplier == null) {
						System.err.printf("MaterialSupplier not found for material: %s and supplier: %s%n", materialNo,
								supplierNo);
						continue;
					}

					String materialSupplierOid = FormatHelper.getObjectId(materialSupplier);
					Vector<String> docIds = new Vector<>();
					docIds.add(FormatHelper.getObjectId(document.getMaster()));

					LCSDocumentHelper.service.associateDocuments(materialSupplierOid, docIds);

					System.out.printf("Linked Document [%s] to MaterialSupplier [%s]%n", docRef, linkName);

				} catch (Exception e) {
					System.err.printf("Error linking docRef %s to MaterialSupplier: %s%n", docRef, e.getMessage());
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.err.println("Failed to read CSV: " + csvPath);
			e.printStackTrace();
		}

	}

	public static void processColorwayObjectCSV(String csvPath) {
		System.out.println(" ----- inside the processColorwayObjectCSV ----- ");
		try (Reader reader = new InputStreamReader(new FileInputStream(csvPath), StandardCharsets.UTF_8);
				CSVParser csvParser = new CSVParser(reader,
						CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim().withQuote('"'))) {
			for (CSVRecord record : csvParser) {
				String docRef = record.get("scDocumentReferenceNumber").trim();
				String colorwayNo = record.get("scColorwayNo").trim();
				String seasonName = record.isSet("Season Name") ? record.get("Season Name").trim() : "";
				String skuName = record.isSet("Sku Name") ? record.get("Sku Name").trim() : "";
				try {
					LCSDocument document = findDocumentByReferenceNumber(docRef);
					if (seasonName != null && !seasonName.isEmpty()) {
						// Link to product-season
						LCSSKUSeasonLink skuSeasonLink = findSkuSeasonLinkByNumber(colorwayNo, seasonName);
						if (skuSeasonLink != null) {
							Vector<String> docIds = new Vector<>();
							docIds.add(FormatHelper.getObjectId(document.getMaster()));
							LCSDocumentHelper.service.associateDocuments(
									FormatHelper.getObjectId(SeasonProductLocator.getSKUSeasonRev(skuSeasonLink)),
									(Vector) docIds);
							System.out.printf("Linked Document [%s] to Sku-Season [%s - %s]%n", docRef, colorwayNo,
									seasonName);
						} else {
							System.err.printf("Sku-Season link not found for Sku [%s] and season [%s]%n", colorwayNo,
									seasonName);
						}
					} else {
						LCSSKU sku = findColorWayByNumber(colorwayNo);
						String skuOid = FormatHelper.getObjectId(sku);
						Vector<String> docIds = new Vector<>();
						docIds.add(FormatHelper.getObjectId(document.getMaster()));
						LCSDocumentHelper.service.associateDocuments(skuOid, docIds);
						System.out.printf("Linked Document [%s] to SKU [%s]%n", docRef, skuName);

					}

				} catch (Exception e) {
					System.err.printf("Error linking docRef %s to Sku: %s%n", docRef, e.getMessage());
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.err.println("Failed to read CSV: " + csvPath);
			e.printStackTrace();
		}

	}

	public static void processFlexSpecificationObjectCSV(String csvPath) {
		System.out.println(" ----- inside the processFlexSpecificationObjectCSV ----- ");
		try (Reader reader = new InputStreamReader(new FileInputStream(csvPath), StandardCharsets.UTF_8);
				CSVParser csvParser = new CSVParser(reader,
						CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim().withQuote('"'))) {
			for (CSVRecord record : csvParser) {
				String docRef = record.get("scDocumentReferenceNumber").trim();
				String productNo = record.get("scPLMProductNo").trim();
				String flexSpecificationName = record.isSet("FlexSpecification Name")
						? record.get("FlexSpecification Name").trim()
						: "";
				try {
					LCSDocument document = findDocumentByReferenceNumber(docRef);
					LCSProduct product = findProductByNumber(productNo);
					SearchResults searchResults = FlexSpecQuery.findExistingSpecs(product, null, null);
					Collection<?> resultList = searchResults.getResults();

					FlexSpecification matchingSpec = null;
					for (Object obj : resultList) {
						if (obj instanceof FlexObject) {
							FlexObject fo = (FlexObject) obj;
							//String specName = fo.getString("FLEXSPECIFICATION.ptc_str_2typeInfoFlexSpecifi");
							String attColumn = FlexTypeCache.getFlexTypeFromPath("Specification").getAttribute("specName").getColumnName();
							String specName = fo.getString("FLEXSPECIFICATION."+attColumn);
							System.out.println(" expected : ptc_str_2typeInfoFlexSpecifi : "+attColumn);
							if (flexSpecificationName.equalsIgnoreCase(specName)) {
								String objId = fo.getString("FLEXSPECIFICATION.IDA2A2");
								matchingSpec = (FlexSpecification) LCSQuery.findObjectById(
										"com.lcs.wc.specification.FlexSpecification:" + objId);
								break;
							}
						}
					}
					
					if (matchingSpec == null) {
						System.err.println("Specification not found for Product [" + productNo + "] and Name [" + flexSpecificationName + "]");
						continue;
					}
					
					String flexSpecID = FormatHelper.getObjectId(matchingSpec);
					Vector<String> docIds = new Vector<>();
					docIds.add(FormatHelper.getObjectId(document.getMaster()));
					LCSDocumentHelper.service.associateDocuments(flexSpecID, docIds);

					System.out.printf("Linked Document [%s] to FlexSpecification [%s] with ProductNumber[%s]%n", docRef, flexSpecificationName, productNo);
						
								} catch (Exception e) {
					System.err.printf("Error linking docRef %s to Specification: %s%n", docRef, e.getMessage());
					e.printStackTrace();                 
				}
			}
		} catch (IOException e) {
			System.err.println("Failed to read CSV: " + csvPath);
			e.printStackTrace();
		}

	}

	public static LCSSKUSeasonLink findSkuSeasonLinkByNumber(String colorwayNo, String seasonName) throws WTException {
		System.out.println("colorwayNo :: " + colorwayNo);
		System.out.println("seasonName :: " + seasonName);

		LCSSKU colorway = findColorWayByNumber(colorwayNo);
		PreparedQueryStatement stmt = new PreparedQueryStatement();
		stmt.appendFromTable("LCSSEASON");
		stmt.appendSelectColumn(new QueryColumn("LCSSEASON", "IDA2A2"));
		//stmt.appendCriteria(new Criteria("LCSSEASON", "ptc_str_4typeInfoLCSSeason", seasonName, Criteria.EQUALS));
		String attColumn = FlexTypeCache.getFlexTypeFromPath("Season").getAttribute("seasonName").getColumnName();
		System.out.println(" expected : ptc_str_4typeInfoLCSSeason : "+attColumn);
		stmt.appendCriteria(new Criteria("LCSSEASON",attColumn, seasonName, Criteria.EQUALS));
		stmt.appendAnd();
		stmt.appendCriteria(new Criteria("LCSSEASON", "LATESTITERATIONINFO", "1", "="));
		LCSQuery.printStatement(stmt);
		System.out.println("stmt :: " + stmt);
		Collection<?> results = LCSQuery.runDirectQuery(stmt).getResults();
		if (!results.isEmpty()) {
			FlexObject fo = (FlexObject) results.iterator().next();
			String objId = fo.getString("LCSSEASON.IDA2A2");
			LCSSeason sea = (LCSSeason) LCSQuery.findObjectById("com.lcs.wc.season.LCSSeason:" + objId);
			LCSSKUSeasonLink seasonSKULink = (LCSSKUSeasonLink) LCSSeasonQuery.findSeasonProductLink(colorway, sea);
			return seasonSKULink;
		}

		return null;

	}

	public static void processProductSeasonCSV(String csvPath) {
		System.out.println("inside the processProductSeasonCSV ");
		try (Reader reader = new InputStreamReader(new FileInputStream(csvPath), StandardCharsets.UTF_8);
				CSVParser csvParser = new CSVParser(reader,
						CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim().withQuote('"'))) {
			for (CSVRecord record : csvParser) {
				String docRef = record.get("scDocumentReferenceNumber").trim();
				String productNo = record.get("scPLMProductNo").trim();
				String seasonName = record.isSet("Season Name") ? record.get("Season Name").trim() : "";
				String productName = record.get("Product Name").trim();

				try {
					LCSDocument document = findDocumentByReferenceNumber(docRef);

					if (seasonName != null && !seasonName.isEmpty()) {
						// Link to product-season
						LCSSeasonProductLink productSeasonLink = findProductSeasonLinkByNumber(productNo, seasonName);
						if (productSeasonLink != null) {
							Vector<String> docIds = new Vector<>();
							docIds.add(FormatHelper.getObjectId(document.getMaster()));
							LCSDocumentHelper.service.associateDocuments(
									FormatHelper
											.getObjectId(SeasonProductLocator.getProductSeasonRev(productSeasonLink)),
									(Vector) docIds);

							System.out.printf("Linked Document [%s] to Product-Season [%s - %s]%n", docRef, productNo,
									seasonName);
						} else {
							System.err.printf("Product-Season link not found for product [%s] and season [%s]%n",
									productNo, seasonName);
						}
					} else {
						LCSProduct product = findProductByNumber(productNo);
						String productOid = FormatHelper.getObjectId(product);
						Vector<String> docIds = new Vector<>();
						docIds.add(FormatHelper.getObjectId(document.getMaster()));
						LCSDocumentHelper.service.associateDocuments(productOid, docIds);
						System.out.printf("Linked Document [%s] to Product [%s]%n", docRef, productName);

					}

				} catch (Exception e) {
					System.err.printf("Error linking docRef %s to Product: %s%n", docRef, e.getMessage());
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.err.println("Failed to read CSV: " + csvPath);
			e.printStackTrace();
		}

	}

	private static String capitalize(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	public static LCSDocument findDocumentByReferenceNumber(String docRef) throws WTException {
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable("LCSDocument");
		statement.appendSelectColumn(new QueryColumn("LCSDocument", "IDA2A2"));
	//	statement.appendCriteria(new Criteria("LCSDocument", "ptc_lng_2typeInfoLCSDocument", docRef, Criteria.EQUALS));
		String attColumn = FlexTypeCache.getFlexTypeFromPath("Document").getAttribute("scDocumentReferenceNumber").getColumnName();
		System.out.println(" expected : ptc_lng_2typeInfoLCSDocument : " + attColumn);
		statement.appendCriteria(new Criteria("LCSDocument", attColumn, docRef, Criteria.EQUALS));
		LCSQuery.printStatement(statement);
		Collection<?> results = LCSQuery.runDirectQuery(statement).getResults();
		if (!results.isEmpty()) {
			FlexObject fo = (FlexObject) results.iterator().next();
			String objId = fo.getString("LCSDOCUMENT.IDA2A2");
			LCSDocument doc = (LCSDocument) LCSQuery.findObjectById("com.lcs.wc.document.LCSDocument:" + objId);
			return (LCSDocument) VersionHelper.latestIterationOf(doc);
		}
		return null;
	}

	public static LCSProduct findProductByNumber(String productNo) throws WTException {
		PreparedQueryStatement stmt = new PreparedQueryStatement();
		stmt.appendFromTable("LCSProduct");
		stmt.appendSelectColumn(new QueryColumn("LCSProduct", "IDA2A2"));
		//stmt.appendCriteria(new Criteria("LCSProduct", "ptc_lng_2typeInfoLCSProduct", productNo, Criteria.EQUALS));
		String attColumn = FlexTypeCache.getFlexTypeFromPath("Product").getAttribute("scPLMProductNo").getColumnName();
		System.out.println( "expected : ptc_lng_2typeInfoLCSProduct : "+ attColumn);
		stmt.appendCriteria(new Criteria("LCSProduct", attColumn , productNo, Criteria.EQUALS));

		LCSQuery.printStatement(stmt);

		Collection<?> results = LCSQuery.runDirectQuery(stmt).getResults();
		if (!results.isEmpty()) {
			FlexObject fo = (FlexObject) results.iterator().next();
			String objId = fo.getString("LCSPRODUCT.IDA2A2");
			LCSProduct prod = (LCSProduct) LCSQuery.findObjectById("com.lcs.wc.product.LCSProduct:" + objId);
			return (LCSProduct) VersionHelper.latestIterationOf(prod);
		}
		return null;
	}

	public static LCSSeasonProductLink findProductSeasonLinkByNumber(String productNo, String seasonName)
			throws WTException {
		System.out.println("productNo :: " + productNo);
		System.out.println("seasonName :: " + seasonName);
		LCSProduct product = findProductByNumber(productNo);
		PreparedQueryStatement stmt = new PreparedQueryStatement();
		stmt.appendFromTable("LCSSEASON");
		stmt.appendSelectColumn(new QueryColumn("LCSSEASON", "IDA2A2"));
		//stmt.appendCriteria(new Criteria("LCSSEASON", "ptc_str_4typeInfoLCSSeason", seasonName, Criteria.EQUALS));
		String attColumn = FlexTypeCache.getFlexTypeFromPath("Season").getAttribute("seasonName").getColumnName();
		stmt.appendCriteria(new Criteria("LCSSEASON", attColumn , seasonName, Criteria.EQUALS));
		System.out.println(" expected : ptc_str_4typeInfoLCSSeason : " + attColumn);
		stmt.appendAnd();
		stmt.appendCriteria(new Criteria("LCSSEASON", "LATESTITERATIONINFO", "1", "="));
		LCSQuery.printStatement(stmt);
		System.out.println("stmt :: " + stmt);
		Collection<?> results = LCSQuery.runDirectQuery(stmt).getResults();
		if (!results.isEmpty()) {
			FlexObject fo = (FlexObject) results.iterator().next();
			String objId = fo.getString("LCSSEASON.IDA2A2");
			LCSSeason sea = (LCSSeason) LCSQuery.findObjectById("com.lcs.wc.season.LCSSeason:" + objId);
			LCSSeasonProductLink seasonProdLink = LCSSeasonQuery.findSeasonProductLink(product, sea);
			return seasonProdLink;
		}

		return null;

	}

	public static LCSSupplier findSupplierByNumber(String supplierNo) throws WTException {
		PreparedQueryStatement stmt = new PreparedQueryStatement();
		stmt.appendFromTable("LCSSupplier");
		stmt.appendSelectColumn(new QueryColumn("LCSSupplier", "IDA2A2"));
		//stmt.appendCriteria(new Criteria("LCSSupplier", "ptc_lng_6typeInfoLCSSupplier", supplierNo, Criteria.EQUALS));
		String attColumn = FlexTypeCache.getFlexTypeFromPath("Supplier").getAttribute("scPLMVendorSupplierNo").getColumnName();
		System.out.println(" expected : ptc_lng_6typeInfoLCSSupplier " + attColumn);
		stmt.appendCriteria(new Criteria("LCSSupplier", attColumn , supplierNo, Criteria.EQUALS));
		LCSQuery.printStatement(stmt);

		Collection<?> results = LCSQuery.runDirectQuery(stmt).getResults();
		if (!results.isEmpty()) {
			FlexObject fo = (FlexObject) results.iterator().next();
			String objId = fo.getString("LCSSUPPLIER.IDA2A2");
			LCSSupplier supp = (LCSSupplier) LCSQuery.findObjectById("com.lcs.wc.supplier.LCSSupplier:" + objId);
			return (LCSSupplier) VersionHelper.latestIterationOf(supp);
		}
		return null;
	}

	public static LCSSample findSampleByNumber(String sampleNo) throws WTException {
		PreparedQueryStatement stmt = new PreparedQueryStatement();
		stmt.appendFromTable("LCSSample");
		stmt.appendSelectColumn(new QueryColumn("LCSSample", "IDA2A2"));
		//stmt.appendCriteria(new Criteria("LCSSample", "ptc_lng_3typeInfoLCSSample", sampleNo, Criteria.EQUALS));
		String attColumn = FlexTypeCache.getFlexTypeFromPath("Sample").getAttribute("scNewSampleNo").getColumnName();
		System.out.println(" expected : ptc_lng_3typeInfoLCSSample " + attColumn);
		stmt.appendCriteria(new Criteria("LCSSample", attColumn , sampleNo, Criteria.EQUALS));
		LCSQuery.printStatement(stmt);

		Collection<?> results = LCSQuery.runDirectQuery(stmt).getResults();
		if (!results.isEmpty()) {
			FlexObject fo = (FlexObject) results.iterator().next();
			String objId = fo.getString("LCSSAMPLE.IDA2A2");
			LCSSample samp = (LCSSample) LCSQuery.findObjectById("com.lcs.wc.sample.LCSSample:" + objId);
			return samp;
		}
		return null;
	}

	public static LCSMaterial findMaterialByNumber(String materialNo) throws WTException {
		PreparedQueryStatement stmt = new PreparedQueryStatement();
		stmt.appendFromTable("LCSMaterial");
		stmt.appendSelectColumn(new QueryColumn("LCSMaterial", "IDA2A2"));
		//stmt.appendCriteria(new Criteria("LCSMaterial", "ptc_lng_10typeInfoLCSMateria", materialNo, Criteria.EQUALS));
		String attColumn = FlexTypeCache.getFlexTypeFromPath("Material").getAttribute("scMaterialNo").getColumnName();
		System.out.println(" expected : ptc_lng_10typeInfoLCSMateria : " + attColumn);
		stmt.appendCriteria(new Criteria("LCSMaterial", attColumn , materialNo, Criteria.EQUALS));

		LCSQuery.printStatement(stmt);
		Collection<?> results = LCSQuery.runDirectQuery(stmt).getResults();
		if (!results.isEmpty()) {
			FlexObject fo = (FlexObject) results.iterator().next();
			String objId = fo.getString("LCSMATERIAL.IDA2A2");
			LCSMaterial mat = (LCSMaterial) LCSQuery.findObjectById("com.lcs.wc.material.LCSMaterial:" + objId);
			return (LCSMaterial) VersionHelper.latestIterationOf(mat);
		}
		return null;
	}

	public static LCSColor findColorByNumber(String colorNo) throws WTException {
		PreparedQueryStatement stmt = new PreparedQueryStatement();
		stmt.appendFromTable("LCSColor");
		stmt.appendSelectColumn(new QueryColumn("LCSColor", "IDA2A2"));
	//	stmt.appendCriteria(new Criteria("LCSColor", "ptc_lng_1typeInfoLCSColor", colorNo, Criteria.EQUALS));
		String attColumn = FlexTypeCache.getFlexTypeFromPath("Color").getAttribute("scColorNo").getColumnName();
		System.out.println(" expected : ptc_lng_1typeInfoLCSColor : " + attColumn);
		stmt.appendCriteria(new Criteria("LCSColor", attColumn, colorNo, Criteria.EQUALS));

		LCSQuery.printStatement(stmt);
		Collection<?> results = LCSQuery.runDirectQuery(stmt).getResults();
		if (!results.isEmpty()) {
			FlexObject fo = (FlexObject) results.iterator().next();
			String objId = fo.getString("LCSCOLOR.IDA2A2");
			LCSColor col = (LCSColor) LCSQuery.findObjectById("com.lcs.wc.color.LCSColor:" + objId);
			return col;
		}
		return null;
	}

	public static LCSSKU findColorWayByNumber(String colorwayNo) throws WTException {
		PreparedQueryStatement stmt = new PreparedQueryStatement();
		stmt.appendFromTable("LCSSKU");
		stmt.appendSelectColumn(new QueryColumn("LCSSKU", "IDA2A2"));
	// 	stmt.appendCriteria(new Criteria("LCSSKU", "ptc_lng_2typeInfoLCSSKU", colorwayNo, Criteria.EQUALS));
		String attColumn = FlexTypeCache.getFlexTypeFromPath("Product").getAttribute("scColorwayNo").getColumnName();
		System.out.println(" expected : ptc_lng_2typeInfoLCSSKU : " + attColumn);
		stmt.appendCriteria(new Criteria("LCSSKU", attColumn , colorwayNo, Criteria.EQUALS));

		LCSQuery.printStatement(stmt);
		Collection<?> results = LCSQuery.runDirectQuery(stmt).getResults();
		if (!results.isEmpty()) {
			FlexObject fo = (FlexObject) results.iterator().next();
			String objId = fo.getString("LCSSKU.IDA2A2");
			LCSSKU colorway = (LCSSKU) LCSQuery.findObjectById("com.lcs.wc.product.LCSSKU:" + objId);
			return (LCSSKU) VersionHelper.latestIterationOf(colorway);

		}
		return null;
	}

	public static FlexSpecification findFlexSpecificationByNumber(String flexSpecificationName) throws WTException {
		PreparedQueryStatement stmt = new PreparedQueryStatement();
		stmt.appendFromTable("FLEXSPECIFICATION");
		stmt.appendSelectColumn(new QueryColumn("FLEXSPECIFICATION", "IDA2A2"));
//		stmt.appendCriteria(new Criteria("FLEXSPECIFICATION", "ptc_str_2typeInfoFlexSpecifi", flexSpecificationName,
//				Criteria.EQUALS));
		String attColumn = FlexTypeCache.getFlexTypeFromPath("Specification").getAttribute("specName").getColumnName();
		System.out.println(" expected : ptc_str_2typeInfoFlexSpecifi : " + attColumn);
		stmt.appendCriteria(new Criteria("FLEXSPECIFICATION", attColumn, flexSpecificationName,
				Criteria.EQUALS));
		LCSQuery.printStatement(stmt);
		System.out.println("stmt ::" + stmt);
		Collection<?> results = LCSQuery.runDirectQuery(stmt).getResults();
		System.out.println("results ::" + results);
		if (!results.isEmpty()) {
			FlexObject fo = (FlexObject) results.iterator().next();
			String objId = fo.getString("FLEXSPECIFICATION.IDA2A2");
			FlexSpecification flexSpec = (FlexSpecification) LCSQuery
					.findObjectById("com.lcs.wc.specification.FlexSpecification:" + objId);
			return (FlexSpecification) VersionHelper.latestIterationOf(flexSpec);

		}
		return null;
	}

	public static LCSMaterialSupplier findMaterialSupplierByNumber(String materialNo, String supplierNo)
			throws WTException {

		System.out.println("materialNo :: " + materialNo);
		System.out.println("supplierNo :: " + supplierNo);

		// --- Step 1: Get Material Master ---
		PreparedQueryStatement stmt = new PreparedQueryStatement();
		stmt.appendFromTable("LCSMaterial");
		stmt.appendSelectColumn(new QueryColumn("LCSMaterial", "IDA2A2"));
		//stmt.appendCriteria(new Criteria("LCSMaterial", "ptc_lng_10typeInfoLCSMateria", materialNo, Criteria.EQUALS));
		String attColumn = FlexTypeCache.getFlexTypeFromPath("Material").getAttribute("scMaterialNo").getColumnName();
		System.out.println(" expected : ptc_lng_10typeInfoLCSMateria : " + attColumn);
		stmt.appendCriteria(new Criteria("LCSMaterial", attColumn , materialNo, Criteria.EQUALS));

		LCSQuery.printStatement(stmt);

		Collection<?> results = LCSQuery.runDirectQuery(stmt).getResults();
		LCSMaterialMaster materialMaster = null;

		if (!results.isEmpty()) {
			FlexObject fo = (FlexObject) results.iterator().next();
			String objId = fo.getString("LCSMaterial.IDA2A2");
			LCSMaterial material = (LCSMaterial) LCSQuery.findObjectById("com.lcs.wc.material.LCSMaterial:" + objId);
			LCSMaterial materialLI = (LCSMaterial) VersionHelper.latestIterationOf(material);
			materialMaster = materialLI.getMaster();
		} else {
			System.out.println("Material not found for number: " + materialNo);
			return null;
		}

		// --- Step 2: Get Supplier Master ---
		PreparedQueryStatement stmt1 = new PreparedQueryStatement();
		stmt1.appendFromTable("LCSSupplier");
		stmt1.appendSelectColumn(new QueryColumn("LCSSupplier", "IDA2A2"));
	//	stmt1.appendCriteria(new Criteria("LCSSupplier", "ptc_lng_6typeInfoLCSSupplier", supplierNo, Criteria.EQUALS));
		
		String attColumn1 = FlexTypeCache.getFlexTypeFromPath("Supplier").getAttribute("scPLMVendorSupplierNo").getColumnName();
		System.out.println("expected : ptc_lng_6typeInfoLCSSupplier : " + attColumn1 );
		stmt1.appendCriteria(new Criteria("LCSSupplier", attColumn1, supplierNo, Criteria.EQUALS));
		LCSQuery.printStatement(stmt1);

		Collection<?> results1 = LCSQuery.runDirectQuery(stmt1).getResults();
		LCSSupplierMaster supplierMaster = null;

		if (!results1.isEmpty()) {
			FlexObject fo1 = (FlexObject) results1.iterator().next();
			String objId1 = fo1.getString("LCSSupplier.IDA2A2");
			LCSSupplier supplier = (LCSSupplier) LCSQuery.findObjectById("com.lcs.wc.supplier.LCSSupplier:" + objId1);
			LCSSupplier supplierLI = (LCSSupplier) VersionHelper.latestIterationOf(supplier);
			supplierMaster = supplierLI.getMaster();
		} else {
			System.out.println("Supplier not found for number: " + supplierNo);
			return null;
		}
		if (materialMaster != null && supplierMaster != null) {
			LCSMaterialSupplier matSupp = LCSMaterialSupplierQuery.findMaterialSupplier(materialMaster, supplierMaster);
			if (matSupp != null) {
				System.out.println("MaterialSupplier found for given material and supplier.");
			} else {
				System.out.println("No MaterialSupplier link found for the given combination.");
			}
			return matSupp;
		}

		System.out.println("Either materialMaster or supplierMaster is null.");
		return null;
	}

}
