package com.sparc.wc.migration.loader;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.calendar.LCSCalendar;
import com.lcs.wc.color.LCSColor;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSObjectToObjectLink;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.infoengine.client.web.ElementTableData;
import com.lcs.wc.load.ExtractCalendar;
import com.lcs.wc.load.ExtractDocument;
import com.lcs.wc.load.LoadCommon;
import com.lcs.wc.load.LoadFlexTyped;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.material.LCSMaterialColor;
import com.lcs.wc.material.LCSMaterialSupplier;
import com.lcs.wc.measurements.LCSFindMeasurementsDelegate;
import com.lcs.wc.measurements.LCSMeasurements;
import com.lcs.wc.measurements.LCSMeasurementsMaster;
import com.lcs.wc.measurements.LCSMeasurementsQuery;
import com.lcs.wc.measurements.LCSPointsOfMeasure;
import com.lcs.wc.measurements.MeasurementValues;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sizing.FullSizeRange;
import com.lcs.wc.sizing.SizeCategory;
import com.lcs.wc.sourcing.LCSSKUSourcingLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.MetricFormatter;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.util.SparcConstants;

import wt.fc.WTObject;
import wt.httpgw.GatewayAuthenticator;
import wt.lifecycle.LifeCycleManaged;
import wt.method.RemoteMethodServer;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class SPARCProductMeasurementsExtract implements wt.method.RemoteAccess {

	public static Vector<String> FLEXTYPED_OBJECT_REFERENCES = new Vector();
	public static String DELIMITER = "TAB";
	public static String NEW_LINE = "\n";
	public static String MEASUREMENT_TYPE = "Measurements\\scApparel";
	public static String MEASUREMENT_BRANCHID = "LCSMEASUREMENTS.BRANCHIDITERATIONINFO";
	public static String MEASUREMENT_VR = "VR:com.lcs.wc.measurements.LCSMeasurements:";
	public static final int PRECISION = Integer.parseInt(LCSProperties.get("com.lcs.wc.measurements.PRECISION", "3"));
	public static final boolean USE_INCREMENTAL_GRADING = LCSProperties
			.getBoolean("com.lcs.wc.measurements.useIncrementalGrading");
	public static final String GRADING_METHOD_ATTRIBUTE = LCSProperties
			.get("com.lcs.wc.measurements.gradingMethodAttribute", "gradingMethod");
	public static Logger logger = LogManager.getLogger(SPARCProductMeasurementsExtract.class.getName());

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
				fromDate = SPARCFlexSpecExtract.runDelta();
			@SuppressWarnings("rawtypes")
			Class[] argsTypes = { String.class, String.class };
			Object[] argsVal = { fileName, fromDate };
			System.out.println("Extraction Started!");
			rms.invoke("extractMeasurement", SPARCProductMeasurementsExtract.class.getName(), null, argsTypes, argsVal);
			System.out.println("Extraction Ended!");
		} catch (final Exception ex) {
			System.out.println("Exception Catched!");
			ex.getLocalizedMessage();
		}

	}

	/**
	 * 
	 * @param args0 Date
	 * @throws WTException
	 * @implNote This Method is used to extract Measurement for Rebook and generate
	 *           the data file in text format
	 */
	@SuppressWarnings("unchecked")
	public static String extractMeasurement(String fileName, String date) throws WTException {
		logger.debug("START EXTRACT Measurement >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		FlexType seasonRbkApparelType = FlexTypeCache.getFlexTypeFromPath("Season\\scSeasonReebok\\scRbkApparel");
		//FlexType seasonRbkFootwearType = FlexTypeCache.getFlexTypeFromPath("Season\\scSeasonReebok\\scFootwear");
		List<List<String>> rows = new ArrayList<>();
		LCSSeason seasonObj = null;
		LCSProduct productObj = null;

		try {
			fileName = SPARCMeasurementLibraryExtract.getOutputFileName(fileName, ".txt");
			LoadCommon.printToFile(fileName, "#Start Of Extract " + new Date(), false);
			//SearchResults seasonResults = SPARCFlexSpecExtract.getSeasons(seasonRbkApparelType, seasonRbkFootwearType);
			SearchResults seasonResults = getSeasons(seasonRbkApparelType);
			if (seasonResults.getResults() != null && seasonResults.getResults().size() > 0) {
				logger.debug("** No of seasons :" + seasonResults.getResults().size());
				for (FlexObject seasonFlexObj : (Collection<FlexObject>) seasonResults.getResults()) {
					seasonObj = (LCSSeason) LCSQuery.findObjectById(
							SparcConstants.SEASON_VR + seasonFlexObj.getString(SPARCFlexSpecExtract.SEASON_BRANCHID));
					if (seasonObj != null) {
						logger.debug("** Season name :" + seasonObj.getName());
						Collection<FlexObject> products = SPARCFlexSpecExtract.getProducts(null, seasonObj);
						if (products != null && products.size() > 0) {
							logger.debug(
									"** No of products in season - " + seasonObj.getName() + " :" + products.size());
							for (FlexObject productFlexObj : products) {
								productObj = (LCSProduct) LCSQuery.findObjectById(SparcConstants.PRODUCT_VR
										+ productFlexObj.getString(SPARCFlexSpecExtract.PRODUCT_BRANCHID));
								if (productObj != null) {
									logger.debug("** Product name :" + productObj.getName());
									findSourceandSpec(productObj, seasonObj, fileName, date);
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
				// logger.debug("** Extarcted Measurement Data ---------- " + rows);

			} else {
				logger.debug("** No season is found ");
			}
			String var10000 = fileName;
			Date var10001 = new Date();
			LoadCommon.printToFile(var10000, LoadCommon.LINEFEED + "#End Of Extract " + var10001);
			LoadCommon.SERVER_MODE = true;
			if (rows != null && rows.size() > 0) {
				// createDataFile(rows, FILE_PATH);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static SearchResults getSeasons(FlexType seasonType) throws WTException {
		logger.debug("START - Prepared Query for season >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		SearchResults results = null;
		try {

			PreparedQueryStatement pqs = new PreparedQueryStatement();
			pqs.appendFromTable("LCSSeason");
			pqs.appendSelectColumn("LCSSeason", "IDA2A2");
			pqs.appendSelectColumn("LCSSeason", "branchIditerationInfo");
			pqs.appendCriteria(new Criteria("LCSSeason", SparcConstants.LATEST_ITERATION_INFO, "1", Criteria.EQUALS));
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(
					new Criteria("LCSSeason", "flexTypeIdPath", seasonType.getTypeIdPath(), Criteria.EQUALS));
			results = LCSQuery.runDirectQuery(pqs);
			logger.debug("**Query Result : " + results);

		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug("END - Prepared Query for season >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		return results;

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
	public static List<List<String>> findSourceandSpec(LCSProduct product, LCSSeason season, String fileName,
			String date) throws WTException {
		logger.debug("START - Find Source and Spec for Product Season >>>>>>>>>>>>>>>>>");

		LCSSourcingConfig sourceObj = null;
		FlexSpecification flexSpecObj = null;
		LCSSupplier vendorObj = null;
		Iterator msItr;
		FlexObject measurement;
		LCSMeasurements mesurementObj = null;

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
								flexSpecObj = (FlexSpecification) LCSQuery.findObjectById(SPARCFlexSpecExtract.SPEC_VR
										+ specFlexObj.getString(SPARCFlexSpecExtract.SPEC_BRANCHID));
								if (flexSpecObj != null) {
									logger.debug("** Spec name :" + sourceObj.getName());
									Collection<String> measurementsResults = LCSMeasurementsQuery
											.findMeasurements(product, sourceObj, flexSpecObj, (Date) null);
									if (!measurementsResults.isEmpty()) {
										logger.debug("** No of Measurements in spec - " + flexSpecObj.getName() + " :"
												+ measurementsResults.size());
										msItr = measurementsResults.iterator();
										while (msItr.hasNext()) {
											measurement = (FlexObject) msItr.next();
											mesurementObj = (LCSMeasurements) LCSQuery.findObjectById(
													MEASUREMENT_VR + measurement.getString(MEASUREMENT_BRANCHID));
											if (mesurementObj != null) {
												logger.debug("** Measurement name :" + mesurementObj.getName());
												if (date != null) {
													Timestamp fromDate = SPARCFlexSpecExtract.convertToTimestamp(date);
													Timestamp measurementModifyDate = mesurementObj
															.getModifyTimestamp();
													if (measurementModifyDate.after(fromDate)) {
														extractMeasurements(mesurementObj, fileName, season, sourceObj,
																flexSpecObj);
													}
												} else {
													extractMeasurements(mesurementObj, fileName, season, sourceObj,
															flexSpecObj);
												}

											} else {
												logger.debug(
														"** Unabale to find Measurement using BranchID, Measurement Object is null ");
											}
										}

									} else {
										logger.debug(
												"** No Measurement found for product, season , source and spec combination");
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

		return null;

	}

	@SuppressWarnings("unchecked")
	public static void extractMeasurements(LCSMeasurements measurements, String fileName, LCSSeason season,
			LCSSourcingConfig sourceObj, FlexSpecification flexSpecObj) {
		try {
			LoadCommon.printToFile(fileName,
					LoadCommon.LINEFEED + "#Extracting Measurements from Products " + " ..." + LoadCommon.LINEFEED);
			LoadCommon.printToFile(fileName, "LCSInitMeasurements");
			LoadCommon.printToFile(fileName,
					LoadCommon.LINEFEED + "#Extracting Product Measurement, POMS And Values ..." + LoadCommon.LINEFEED);
			LoadCommon.printToFile(fileName, getMeasurementDetails(measurements, season, sourceObj, flexSpecObj));
			Collection<ElementTableData> poms = LCSFindMeasurementsDelegate
					.findPOM(FormatHelper.getObjectId(measurements), null);
			if (poms != null) {
				// LoadCommon.printToFile(fileName, LoadCommon.LINEFEED);
				Iterator<ElementTableData> pomsItr = poms.iterator();
				while (pomsItr.hasNext()) {
					ElementTableData pom = pomsItr.next();
					LCSPointsOfMeasure lcsPom = (LCSPointsOfMeasure) LCSQuery.findObjectById(pom.getData("IDA2A2"));
					LoadCommon.printToFile(fileName, getPOMDetails(lcsPom, true));
				}
			}
			if (poms.isEmpty() || poms == null)
				LoadCommon.printToFile(fileName, LoadCommon.LINEFEED + "LCSInitMeasurements");
			else {
				LoadCommon.printToFile(fileName, "LCSInitMeasurements");
			}
		} catch (IOException var3) {
			LoadCommon.printToFile(fileName, "#IOException : " + var3.getLocalizedMessage());

		} catch (Exception var4) {
			LoadCommon.printToFile(fileName, "#IOException : " + var4.getLocalizedMessage());
		}

	}

	public static String getMeasurementDetails(LCSMeasurements measurements, LCSSeason seasonObj,
			LCSSourcingConfig sourceObj, FlexSpecification flexSpecObj) throws IOException {

		if (DELIMITER.equalsIgnoreCase("TAB")) {
			DELIMITER = LoadCommon.DELIMITER;
		}
		String value = null;
		String var10000 = DELIMITER;
		String line = "LCSMeasurementsProduct" + var10000 + LoadFlexTyped.getValue(measurements, "name").replace("\t", "") + DELIMITER
				+ LoadCommon.getPathFromFlexType(measurements.getFlexType());
		if ((value = measurements.getMeasurementsType()) == null) {
			value = "";
		}
		line = line + DELIMITER + value;
		if (measurements.getProductSizeCategory() == null) {
			value = "";
		}
		else {
			value=measurements.getProductSizeCategory().getName();
		}

		line = line + DELIMITER + value;
		if ((value = measurements.getSizeRun()) == null) {
			value = "";
		}

		line = line + DELIMITER + value;
		if ((value = measurements.getSampleSize()) == null) {
			value = "";
		}

		line = line + DELIMITER + value;
		LCSMeasurementsMaster measurementMaster = measurements.getSourceTemplate();
		if (measurementMaster != null) {
			value = measurementMaster.getMeasurementsName().replace("\t", "");
			value = value + DELIMITER + MEASUREMENT_TYPE;
		} else {
			value = DELIMITER;
			// DELIMITER.ma
		}
		line = line + DELIMITER + value;
		LCSMeasurementsMaster measurementMasterGrade = measurements.getGradings();
		if (measurementMasterGrade != null) {
			value = measurementMasterGrade.getMeasurementsName().replace("\t", "");
			value = value + DELIMITER + MEASUREMENT_TYPE;
		} else {
			value = DELIMITER;
			// DELIMITER.ma
		}

		line = line + DELIMITER + value;
		if ("INSTANCE".equals(measurements.getMeasurementsType())
				|| "INSTANCE_SPECIFIC".equals(measurements.getMeasurementsType())) {
			logger.debug("--instance---");
			value = "NO PRODUCT";
			if (measurements.getProductMaster() != null) {
				if (logger.isDebugEnabled()) {
					logger.debug(
							"measurements.getProductMaster().getName():  " + measurements.getProductMaster().getName());
				}

				value = measurements.getProductMaster().getName();
			}

			line = line + DELIMITER + value;
			value = "NO PRODUCT";
			Logger var12;
			FlexType var10001;
			if (measurements.getProductMaster() != null) {
				try {
					LCSProduct product = (LCSProduct) VersionHelper.latestIterationOf(measurements.getProductMaster());
					if (logger.isDebugEnabled()) {
						var12 = logger;
						var10001 = product.getFlexType();
						var12.debug("product.getTypeName():  " + var10001.getFullName(true));
					}

					value = product.getFlexType().getFullName(true);
				} catch (WTException var7) {
					var7.printStackTrace();
				}
			}

			line = line + DELIMITER + value;
			value = "NO SEASON";
			if (measurements.getSeasonMaster() != null) {
				if (logger.isDebugEnabled()) {
					logger.debug(
							"measurements.getSeasonMaster().getName():  " + measurements.getSeasonMaster().getName());
				}

				value = measurements.getSeasonMaster().getName();
			} else {
				value = seasonObj.getMaster().getName();
			}

			line = line + DELIMITER + value;
			value = "NO SEASON";
			if (measurements.getSeasonMaster() != null) {
				try {
					LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(measurements.getSeasonMaster());
					if (logger.isDebugEnabled()) {
						var12 = logger;
						var10001 = season.getFlexType();
						var12.debug("season.getTypeName():  " + var10001.getFullName(true));
					}

					value = season.getFlexType().getFullName(true);
				} catch (WTException var6) {
					var6.printStackTrace();
				}
			} else {
				value = seasonObj.getFlexType().getFullName(true);
			}

			line = line + DELIMITER + value;
			value = "";
			if (measurements.getSource() != null) {
				if (logger.isDebugEnabled()) {
					logger.debug(
							"measurements.getSource().getName():  " + measurements.getSource().getSourcingConfigName());
				}

				value = measurements.getSource().getSourcingConfigName();
			} else {
				value = sourceObj.getSourcingConfigName();
			}

			line = line + DELIMITER + value;
			value = "";
			if (measurements.getSource() != null) {
				try {
					LCSSourcingConfig sourceConfig = (LCSSourcingConfig) VersionHelper
							.latestIterationOf(measurements.getSource());
					if (logger.isDebugEnabled()) {
						var12 = logger;
						var10001 = sourceConfig.getFlexType();
						var12.debug("sourceConfig.getFlexType().getTypeName():  " + var10001.getFullName(true));
					}

					value = sourceConfig.getFlexType().getFullName(true);
				} catch (WTException var5) {
					var5.printStackTrace();
				}
			} else {
				value = sourceObj.getFlexType().getFullName(true);
			}

			line = line + DELIMITER + value;
		}

		value = "";
		if (flexSpecObj != null) {
			value = flexSpecObj.getName();
		}

		line = line + DELIMITER + value;

		value = "";
		if (measurements.getSizeCategory() != null) {
			SizeCategory sizeCat = measurements.getSizeCategory();
			value = sizeCat.getName();
		}

		line = line + DELIMITER + value;
		value = "";
		if (measurements.getFullSizeRange() != null) {
			FullSizeRange fullSizeRange = measurements.getFullSizeRange();
			value = fullSizeRange.getName();
		}

		line = line + DELIMITER + value + DELIMITER;
		value = measurements.getPrimaryImageURL();
		value = FormatHelper.decodeImageUrl(value);
		value = ExtractDocument.downloadImage(value);
		line = line + value + DELIMITER;

		value = FormatHelper.getNumericObjectIdFromObject(measurements.getMaster());
		// line = line + NEW_LINE + "LCSObjectAttribute" + DELIMITER +
		// "glMeasurementMasterID" + DELIMITER + value;
		
		line = line + getObjectAttributes(measurements, DELIMITER);
		line = addMeasurementMasterID(line, measurements);
		return line;
	}

	private static String getObjectAttributes(WTObject measurements, String delimiter) {
		LCSCalendar calendar = ExtractCalendar.getCalendar(measurements);
		String line = calendar == null ? "" : calendar.getName();
		StringBuffer sBuff = new StringBuffer(line);
		if (measurements instanceof LifeCycleManaged) {
			sBuff = sBuff.append(delimiter).append(((LifeCycleManaged) measurements).getLifeCycleName());
			sBuff = sBuff.append(delimiter).append(((LifeCycleManaged) measurements).getLifeCycleState());
		}

		sBuff = sBuff.append(delimiter).append("IGNORE");
		sBuff = sBuff.append(LoadCommon.LINEFEED).append(getAttributes((FlexTyped) measurements, delimiter, LoadCommon.USE_MIGRATIONID));
		return sBuff.toString();
	}

	private static String getAttributes(FlexTyped measurements, String delimiter, boolean MigrationId) {
		String value = "";

		try {
			FlexType flextype = measurements.getFlexType();
			if (flextype == null) {
				LoadCommon.display(
						"#The FlexTyped Object '" + measurements.toString() + "' Has No FlexType Associated To It !!!");
				return value;
			}

			String attValue = "";
			String moaValue = "";
			Collection<FlexTypeAttribute> attributes = flextype.getAllAttributesUsedBy(measurements);
			String attributeType = null;
			Iterator var9 = attributes.iterator();

			while (true) {
				while (true) {
					while (var9.hasNext()) {
						FlexTypeAttribute attribute = (FlexTypeAttribute) var9.next();
						attributeType = attribute.getAttVariableType();
						if (!attributeType.equals("object_ref") && !attributeType.equals("object_ref_list")) {
							if (!attributeType.endsWith("multiobject") && !attributeType.endsWith("discussion")) {
								if (!attributeType.endsWith("derivedString")) {
									if (attributeType.equals("userList")) {
										FlexObject obj = (FlexObject) measurements.getValue(attribute.getAttKey());
										if (obj != null) {
											value = value + "LCSObjectAttribute" + delimiter + attribute.getAttKey()
													+ delimiter + obj.getData("NAME").replace("\t", "") + LoadCommon.LINEFEED;
										}
									} else if (!(attValue = LoadFlexTyped.getValue(measurements, attribute.getAttKey()))
											.trim().equals("")) {
										if (attributeType.equals("image")) {
											attValue = ExtractDocument.downloadImage(attValue);
										}

										value = value + "LCSObjectAttribute" + delimiter + attribute.getAttKey()
												+ delimiter + attValue.replace("\t", "") + LoadCommon.LINEFEED;
									}
								}
							} 
						} else if (!skipObjRefAttr(attribute, measurements)) {
							if ((!(measurements instanceof LCSSKU)
									|| !attribute.getAttKey().equals(flextype.getAttKeyForAttribute("color")))
									&& !(measurements instanceof LCSSourcingConfig)) {
								if (!(measurements instanceof LCSMaterialSupplier) && !(measurements instanceof LCSMaterialColor)) {
									FLEXTYPED_OBJECT_REFERENCES.add(getObjectReferences(measurements, attribute, delimiter));
								} else {
									FLEXTYPED_OBJECT_REFERENCES
											.add(getObjectReferenceForLinkAttributes(measurements, attribute, delimiter));
								}
							} else {
								value = value + getObjectReferences(measurements, attribute, delimiter) + LoadCommon.LINEFEED;
							}
						}
					}

					if (MigrationId) {
						value = value + "LCSObjectAttribute" + delimiter + "migrationId" + delimiter
								+ ((WTObject) measurements).getPersistInfo().getObjectIdentifier().getId()
								+ LoadCommon.LINEFEED;
					}

					if (!value.equals("") || measurements instanceof LCSSKUSourcingLink) {
						value = value + "LCSObjectAttribute" + delimiter + "End";
					}

					if (!moaValue.equals("")) {
						value = moaValue + value;
					}

					return value;
				}
			}
		} catch (WTException var12) {
			var12.printStackTrace();
			LoadCommon.display("\n#WTException : " + var12.getLocalizedMessage());
			value = "";
		} catch (IOException var13) {
			var13.printStackTrace();
			LoadCommon.display("\n#IOException : " + var13.getLocalizedMessage());
			value = "";
		}

		return value;
	}

	private static String getObjectReferenceForLinkAttributes(FlexTyped flexObj, FlexTypeAttribute attribute,
			String delimiter) {
		String value = "";
		StringBuffer buffer = new StringBuffer("LCSObjectReferenceToLink");
		String linkIdentifier = "";

		try {
			WTObject refObj = (WTObject) flexObj.getValue(attribute.getAttKey());
			if (refObj == null) {
				value = "";
			} else {
				LCSMaterial material;
				LCSSupplier supplier;
				if (flexObj instanceof LCSMaterialSupplier) {
					linkIdentifier = "MaterialSupplier";
					buffer.append(delimiter).append(linkIdentifier);
					buffer.append(delimiter).append(((LCSMaterialSupplier) flexObj).getName()).append(delimiter);
					material = (LCSMaterial) VersionHelper
							.latestIterationOf(((LCSMaterialSupplier) flexObj).getMaterialMaster());
					buffer.append(LoadCommon.getPathFromFlexType(material.getFlexType())).append(delimiter)
							.append(material.getName()).append(delimiter);
					buffer.append(LoadCommon.getName(material)).append(delimiter);
					supplier = (LCSSupplier) VersionHelper
							.latestIterationOf(((LCSMaterialSupplier) flexObj).getSupplierMaster());
					buffer.append(LoadCommon.getPathFromFlexType(supplier.getFlexType())).append(delimiter)
							.append(supplier.getSupplierName()).append(delimiter);
					buffer.append(LoadCommon.getName(supplier)).append(delimiter);
					buffer.append("").append(delimiter).append("").append(delimiter).append("").append(delimiter);
				} else if (flexObj instanceof LCSMaterialColor) {
					linkIdentifier = "MaterialColor";
					buffer.append(delimiter).append(linkIdentifier);
					buffer.append(delimiter).append(((LCSMaterialColor) flexObj).getName()).append(delimiter);
					material = (LCSMaterial) VersionHelper
							.latestIterationOf(((LCSMaterialColor) flexObj).getMaterialMaster());
					buffer.append(LoadCommon.getPathFromFlexType(material.getFlexType())).append(delimiter)
							.append(material.getName()).append(delimiter);
					buffer.append(LoadCommon.getName(material)).append(delimiter);
					supplier = (LCSSupplier) VersionHelper
							.latestIterationOf(((LCSMaterialColor) flexObj).getSupplierMaster());
					buffer.append(LoadCommon.getPathFromFlexType(supplier.getFlexType())).append(delimiter)
							.append(supplier.getSupplierName()).append(delimiter);
					buffer.append(LoadCommon.getName(supplier)).append(delimiter);
					LCSColor color = ((LCSMaterialColor) flexObj).getColor();
					buffer.append(LoadCommon.getPathFromFlexType(color.getFlexType())).append(delimiter)
							.append(color.getColorName()).append(delimiter);
					buffer.append(LoadCommon.getName(color)).append(delimiter);
				}

				buffer.append(attribute.getAttKey()).append(delimiter);
				buffer.append(LoadCommon.getIdentification(refObj, LoadCommon.getName(refObj), delimiter));
				value = buffer.toString();
			}
		} catch (WTException var10) {
			LoadCommon.display("\n#WTException : " + var10.getLocalizedMessage());
			value = "";
		}

		return value;
	}

	private static String getObjectReferences(FlexTyped flexObj, FlexTypeAttribute attribute, String delimiter) {
		String value = null;
		StringBuffer buffer = new StringBuffer("LCSObjectReference");

		try {
			WTObject refObj = (WTObject) flexObj.getValue(attribute.getAttKey());
			if (refObj == null) {
				value = "";
			} else {
				buffer.append(delimiter);
				buffer.append(LoadCommon.getIdentification((WTObject) flexObj, LoadCommon.getName((WTObject) flexObj),
						delimiter));
				buffer.append(delimiter).append(attribute.getAttKey()).append(delimiter);
				buffer.append(LoadCommon.getIdentification(refObj, LoadCommon.getName(refObj), delimiter));
				value = buffer.toString();
			}
		} catch (WTException var6) {
			LoadCommon.display("\n#WTException : " + var6.getLocalizedMessage());
			value = "";
		}

		return value;
	}

	private static boolean skipObjRefAttr(FlexTypeAttribute attribute, FlexTyped object) {
		if (attribute.getRefDefinition() == null) {
			return false;
		} else if (object instanceof LCSObjectToObjectLink) {
			return true;
		} else {
			String name = attribute.getRefDefinition().getName();
			return "MATERIALSUPPLIER".equals(name) || "MATERIALCOLOR".equals(name);
		}
	}

	public static String addMeasurementMasterID(String line, LCSMeasurements measurements) {
		StringBuilder modified = new StringBuilder();
		try {
			String masterID = FormatHelper.getNumericObjectIdFromObject(measurements.getMaster());
			String newLineToInsert = "LCSObjectAttribute" + DELIMITER + "glMeasurementMasterID" + DELIMITER + masterID;
			String[] lines = line.split(NEW_LINE);

			for (int i = 0; i < lines.length; i++) {
				if (i == lines.length - 1) {
					modified.append(newLineToInsert).append(NEW_LINE);
				}
				modified.append(lines[i]);
				if (i < lines.length - 1) {
					modified.append(NEW_LINE);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(modified.toString());
		return modified.toString();
	}

	public static String getPOMDetails(LCSPointsOfMeasure pom, boolean measurementValues) throws WTException {
		if (DELIMITER.equalsIgnoreCase("TAB")) {
			DELIMITER = LoadCommon.DELIMITER;
		}
		double NULL_VALUE_PLACEHOLDER = Double.parseDouble(LCSMeasurementsQuery.NULL_VALUE_PLACEHOLDER);
		LCSMeasurementsMaster pomMeasurementsMaster = pom.getMeasurementsMaster();
		LCSMeasurements measurements = null;
		if (pomMeasurementsMaster != null) {
			measurements = (LCSMeasurements) VersionHelper.latestIterationOf(pomMeasurementsMaster);
		}

		String value = null;
		String uom = null;
		if (measurements != null && measurements.getValue("uom") != null
				&& FormatHelper.hasContent(((String) measurements.getValue("uom")).toString())) {
			uom = "si.Length." + measurements.getValue("uom").toString();
		}

		String sampleSizeValue = Double.toString(pom.getSampleValue());

		if (uom != null) {
			sampleSizeValue = MetricFormatter.uomConvertor(sampleSizeValue.toString(), uom, PRECISION, false, false);
		}

		String var10000 = DELIMITER;
		String line = "LCSPointsOfMeasure" + var10000 + LoadFlexTyped.getValue(pom, "measurementName").replace("\t", "") + DELIMITER
				+ LoadFlexTyped.getValue(pom, "number");
		line =line +DELIMITER +pom.getValue("scnumber");
		line = line + DELIMITER + LoadCommon.getPathFromFlexType(pom.getFlexType()) + DELIMITER + pom.getId()
				+ DELIMITER + pom.getSortingNumber();
		System.out.println("line :: "+line);
		line = line + DELIMITER + sampleSizeValue + DELIMITER + pom.getPlusTol() + DELIMITER + pom.getMinusTol();
		if ((value = pom.getPoint1()) == null) {
			value = "";
		}

		line = line + DELIMITER + value;
		if ((value = pom.getPoint2()) == null) {
			value = "";
		}

		line = line + DELIMITER + value;
		if ((value = pom.getPointsOfMeasureType()) == null) {
			value = "";
		}

		line = line + DELIMITER + value;
		LCSPointsOfMeasure pomLibrary = pom.getLibraryItem();
		if (pomLibrary != null && !pomLibrary.isDropped()) {
			var10000 = LoadFlexTyped.getValue(pomLibrary, "measurementName").replace("\t", "");
			value = var10000 + DELIMITER + LoadFlexTyped.getValue(pomLibrary, "number");
		} else {
			value = DELIMITER;
		}

		line = line + DELIMITER + value;
		if (pomMeasurementsMaster == null) {
			value = "";
		} else {
			measurements = (LCSMeasurements) VersionHelper.latestIterationOf(pomMeasurementsMaster);
			value = pomMeasurementsMaster.getMeasurementsName().replace("\t", "");
		}

		FlexTypeAttribute plusToleranceAtt;
		if (uom != null) {
			uom = "si.Length." + measurements.getValue("uom").toString();
			plusToleranceAtt = measurements.getFlexType().getAttribute("plusTolerance");

			try {
				if (pom.getValue("plusTolerance") != null
						&& Double.parseDouble(pom.getValue("plusTolerance").toString()) != NULL_VALUE_PLACEHOLDER) {
					pom.setValue("plusTolerance",
							new Double(MetricFormatter.uomConvertor(pom.getValue("plusTolerance").toString(), uom,
									plusToleranceAtt.getAttDecimalFigures(), false, false)));
				}

				if (pom.getValue("minusTolerance") != null
						&& Double.parseDouble(pom.getValue("minusTolerance").toString()) != NULL_VALUE_PLACEHOLDER) {
					pom.setValue("minusTolerance",
							new Double(MetricFormatter.uomConvertor(pom.getValue("minusTolerance").toString(), uom,
									plusToleranceAtt.getAttDecimalFigures(), false, false)));
				}
			} catch (WTPropertyVetoException var26) {
				var26.printStackTrace();
			}
		}

		line = line + DELIMITER + value + DELIMITER + getObjectAttributes(pom, DELIMITER);
		plusToleranceAtt = null;
		String header = "LCSMeasurementValues";
		if (measurements != null && "GRADINGS".equals(measurements.getMeasurementsType())) {
			header = "LCSGradingValues";
		}

		String gradingMethod = "";
		if (measurements != null && measurements.getValue(GRADING_METHOD_ATTRIBUTE) != null) {
			gradingMethod = measurements.getValue(GRADING_METHOD_ATTRIBUTE).toString();
		}

		Hashtable measurementValuesTable;
		if (measurementValues && (measurementValuesTable = pom.getMeasurementValues()) != null) {
			MeasurementValues mValue = null;
			Enumeration<?> keys = measurementValuesTable.keys();
			line = line + LoadCommon.LINEFEED + header + DELIMITER + "Start Of MeasurementValues";
			String currentValue;
			if ("GRADINGS".equals(measurements.getMeasurementsType())) {
				currentValue = null;
				MeasurementValues mv2 = null;
				String sampleSize = "";
				Iterator<?> sizeRunIter = MOAHelper.getMOACollection(measurements.getSizeRun()).iterator();
				String size;
				MeasurementValues mv;
				if (USE_INCREMENTAL_GRADING && "incremental".equals(gradingMethod)) {
					Vector<?> vector = (Vector) MOAHelper.getMOACollection(measurements.getSizeRun());
					int baseSizeIndex = vector.indexOf(measurements.getSampleSize());
					boolean var23 = true;

					label139: while (true) {
						while (true) {
							int gradeSizeIndex;
							do {
								if (!sizeRunIter.hasNext()) {
									break label139;
								}

								size = (String) sizeRunIter.next();
								gradeSizeIndex = vector.indexOf(size);
							} while (pom.getMeasurementValues() == null);

							mv = (MeasurementValues) pom.getMeasurementValues().get(size);
							if (gradeSizeIndex > baseSizeIndex) {
								mv2 = (MeasurementValues) pom.getMeasurementValues()
										.get(vector.get(gradeSizeIndex - 1));
							} else if (baseSizeIndex > gradeSizeIndex) {
								mv2 = (MeasurementValues) pom.getMeasurementValues()
										.get(vector.get(gradeSizeIndex + 1));
							}

							if (mv != null && (double) mv.getValue() == NULL_VALUE_PLACEHOLDER) {
								line = line + LoadCommon.LINEFEED + header + DELIMITER + size + DELIMITER
										+ mv.getValue() + DELIMITER + mv.getGrading() + DELIMITER
										+ mv.getOverrideValue() + DELIMITER + mv.getActualValue();
							} else {
								String currentValue1;
								String newValue;
								if (mv != null && mv2 != null) {
									currentValue1 = String.valueOf(mv.getValue() - mv2.getValue());
									if (uom != null && uom.indexOf("FRACTION") < 0) {
										newValue = MetricFormatter.uomConvertor(currentValue1, uom, PRECISION, false,
												false);
										line = line + LoadCommon.LINEFEED + header + DELIMITER + size + DELIMITER
												+ newValue + DELIMITER + mv.getGrading() + DELIMITER
												+ mv.getOverrideValue() + DELIMITER + mv.getActualValue();
									} else {
										line = line + LoadCommon.LINEFEED + header + DELIMITER + size + DELIMITER
												+ currentValue1 + DELIMITER + mv.getGrading() + DELIMITER
												+ mv.getOverrideValue() + DELIMITER + mv.getActualValue();
									}
								} else if (mv != null) {
									currentValue1 = String.valueOf(mv.getValue());
									if (uom != null) {
										newValue = MetricFormatter.uomConvertor(currentValue1, uom, PRECISION, false,
												false);
										line = line + LoadCommon.LINEFEED + header + DELIMITER + size + DELIMITER
												+ newValue + DELIMITER + mv.getGrading() + DELIMITER
												+ mv.getOverrideValue() + DELIMITER + mv.getActualValue();
									} else {
										line = line + LoadCommon.LINEFEED + header + DELIMITER + size + DELIMITER
												+ currentValue1 + DELIMITER + mv.getGrading() + DELIMITER
												+ mv.getOverrideValue() + DELIMITER + mv.getActualValue();
									}
								}
							}
						}
					}
				} else {
					sampleSize = measurements.getSampleSize();

					label155: while (true) {
						while (true) {
							do {
								if (!sizeRunIter.hasNext()) {
									break label155;
								}

								size = (String) sizeRunIter.next();
							} while (pom.getMeasurementValues() == null);

							mv = (MeasurementValues) pom.getMeasurementValues().get(sampleSize);
							mv2 = (MeasurementValues) pom.getMeasurementValues().get(size);
							if (mv2 != null && (double) mv2.getValue() == NULL_VALUE_PLACEHOLDER) {
								line = line + LoadCommon.LINEFEED + header + DELIMITER + size + DELIMITER
										+ mv2.getValue() + DELIMITER + mv2.getGrading() + DELIMITER
										+ mv2.getOverrideValue() + DELIMITER + mv2.getActualValue();
							} else if (mv != null && mv2 != null) {
								String currentValue1 = String.valueOf(mv2.getValue() - mv.getValue());
								if (uom != null) {
									String newValue = MetricFormatter.uomConvertor(currentValue1, uom, PRECISION, false,
											false);
									line = line + LoadCommon.LINEFEED + header + DELIMITER + size + DELIMITER + newValue
											+ DELIMITER + mv2.getGrading() + DELIMITER + mv2.getOverrideValue()
											+ DELIMITER + mv2.getActualValue();
								} else {
									line = line + LoadCommon.LINEFEED + header + DELIMITER + size + DELIMITER
											+ currentValue1 + DELIMITER + mv2.getGrading() + DELIMITER
											+ mv2.getOverrideValue() + DELIMITER + mv2.getActualValue();
								}
							}
						}
					}
				}
			} else {
				label165: while (true) {
					while (true) {
						if (!keys.hasMoreElements()) {
							break label165;
						}

						value = (String) keys.nextElement();
						mValue = (MeasurementValues) measurementValuesTable.get(value);
						currentValue = String.valueOf(mValue.getValue());
						if (uom != null && (double) mValue.getValue() != NULL_VALUE_PLACEHOLDER) {
							String newValue = MetricFormatter.uomConvertor(currentValue, uom, PRECISION, false, false);
							line = line + LoadCommon.LINEFEED + header + DELIMITER + value + DELIMITER + newValue
									+ DELIMITER + mValue.getGrading() + DELIMITER + mValue.getOverrideValue()
									+ DELIMITER + mValue.getActualValue();
						} else {
							line = line + LoadCommon.LINEFEED + header + DELIMITER + value + DELIMITER
									+ mValue.getValue() + DELIMITER + mValue.getGrading() + DELIMITER
									+ mValue.getOverrideValue() + DELIMITER + mValue.getActualValue();
						}
					}
				}
			}

			line = line + LoadCommon.LINEFEED + header + DELIMITER + "End Of MeasurementValues" + LoadCommon.LINEFEED;
		}

		return line;
	}

}
