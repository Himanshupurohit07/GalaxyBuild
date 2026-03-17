package com.sparc.wc.migration.loader;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.JoinCriteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.load.ExtractMeasurement;
import com.lcs.wc.load.ExtractDocument;
import com.lcs.wc.load.LoadCommon;
import com.lcs.wc.load.LoadFlexTyped;
import com.lcs.wc.measurements.LCSMeasurements;
import com.lcs.wc.measurements.LCSMeasurementsLogic;
import com.lcs.wc.measurements.LCSMeasurementsMaster;
import com.lcs.wc.measurements.LCSMeasurementsQuery;
import com.lcs.wc.measurements.LCSPointsOfMeasure;
import com.lcs.wc.measurements.LCSPointsOfMeasureLogic;
import com.lcs.wc.measurements.MeasurementValues;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sizing.FullSizeRange;
import com.lcs.wc.sizing.SizeCategory;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.MetricFormatter;
import com.lcs.wc.util.VersionHelper;
import com.ptc.core.meta.common.RemoteWorkerHandler;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wt.fc.WTObject;
import wt.httpgw.GatewayAuthenticator;
import wt.method.RemoteMethodServer;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import java.io.File;
import java.util.Properties;

public class SPARCMeasurementLibraryExtract implements wt.method.RemoteAccess {

	/**
	 * 
	 */
	private static String FILENAME = null;
	private static String DELIMITER;
	private static String TYPENAME = "Measurements";
	private static String REEBOK_BRAND = "reebok";
	private static String REEBOK = "Reebok";
	public static String[] ARGS;
	public static final int PRECISION = Integer.parseInt(LCSProperties.get("com.lcs.wc.measurements.PRECISION", "3"));
	public static final boolean USE_INCREMENTAL_GRADING = LCSProperties
			.getBoolean("com.lcs.wc.measurements.useIncrementalGrading");
	public static final String GRADING_METHOD_ATTRIBUTE = LCSProperties
			.get("com.lcs.wc.measurements.gradingMethodAttribute", "gradingMethod");
	//protected static LCSPointsOfMeasureLogic POM_LOGIC = new LCSPointsOfMeasureLogic();
    //protected static LCSMeasurementsLogic MEASUREMENTS_LOGIC = new LCSMeasurementsLogic();
	private static final Logger LOGGER = LogManager.getLogger(SPARCMeasurementLibraryExtract.class.getName());

	public static void main(String[] args) {
		for (int i = 0; i < args.length; ++i) {
			if (args[i].equalsIgnoreCase("-help")) {
				System.out.println(
						"Usage\t-\tjava com.lcs.wc.load.ExtractMeasurement -f FileName -d Delimiter -t TypeName");
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
				FILENAME = args[i];
			} else if (args[i].equalsIgnoreCase("-d")) {
				++i;
				DELIMITER = args[i];
			} else if (args[i].equalsIgnoreCase("-t")) {
				++i;
				TYPENAME = args[i].replace('_', ' ');
			} else {
				System.out.println("The Argument " + args[i] + " Is Not Supported !!!");
				System.exit(0);
			}
		}

		if (!FormatHelper.hasContent(FILENAME)) {
			System.out.println("Please specify a valid output file name");
			System.exit(0);
		}

		try {
			//RemoteWorkerHandler.handleRemoteWorker(new SPARCMeasurementLibraryExtractRemoteWorker(), args);
			RemoteMethodServer rms = RemoteMethodServer.getDefault();
			String username = "wcadmin";
			GatewayAuthenticator auth = new GatewayAuthenticator();
			auth.setRemoteUser(username);
			rms.setAuthenticator(auth);
			@SuppressWarnings("rawtypes")
			Class[] argsTypes = { Object.class};
			Object[] argsVal = { args };
			rms.invoke("process", SPARCMeasurementLibraryExtract.class.getName(), null, argsTypes, argsVal);

		} catch (Exception var5) {
			var5.printStackTrace();
		} finally {
			System.exit(0);
		}

	}

	public static void process(Object input) {
		FILENAME = null;
		TYPENAME = "Measurements";
		String[] args = (String[]) input;

		for (int i = 0; i < args.length; ++i) {
			if (args[i].equalsIgnoreCase("-f")) {
				++i;
				FILENAME = getOutputFileName(args[i], ".txt");
			} else if (args[i].equalsIgnoreCase("-d")) {
				++i;
				DELIMITER = args[i];
				if (DELIMITER.equalsIgnoreCase("TAB")) {
					DELIMITER = LoadCommon.DELIMITER;
				}
			} else {
				if (!args[i].equalsIgnoreCase("-t")) {
					System.out.println("The Argument " + args[i] + " Is Not Supported !!!");
					return;
				}

				++i;
				TYPENAME = args[i].replace('_', ' ');
			}
		}

		extractAll(FILENAME, DELIMITER, TYPENAME);
	}

	protected static String getOutputFileName(String fileName, String suffix) {
		Properties properties = System.getProperties();
		String userDir = properties.getProperty("user.dir");
		int indx = fileName.lastIndexOf(46);
		if (indx > -1) {
			fileName = fileName.substring(0, indx);
		}

		return userDir + File.separator + fileName.concat(suffix);
	}

	static void doSomething() {
	}

	public static void extractAll(String fileName, String delimiter, String type) {
		FILENAME = fileName;
		DELIMITER = delimiter;
		TYPENAME = type;
		extractAll();
	}

	protected static void extractAll() {
		LoadCommon.SERVER_MODE = false;
		LoadCommon.printToFile(FILENAME, "#Start Of Extract " + new Date(), false);
		extractMeasurements();
		String var10000 = FILENAME;
		Date var10001 = new Date();
		LoadCommon.printToFile(var10000, "#End Of Extract " + var10001);
		LoadCommon.SERVER_MODE = true;
	}

	protected static void extractMeasurements() {
		try {
			LoadCommon.printToFile(FILENAME, LoadCommon.LINEFEED + "#Extracting Measurement Data Of Type " + TYPENAME
					+ " ..." + LoadCommon.LINEFEED);
			LoadCommon.printToFile(FILENAME, "LCSInitMeasurements");
			LoadCommon.printToFile(FILENAME,
					LoadCommon.LINEFEED + "#Extracting Points Of Measure Library Items ..." + LoadCommon.LINEFEED);
			LCSPointsOfMeasure[] pomList = getPointsOfMeasures(TYPENAME, (Hashtable) null, (LCSMeasurements) null);

			for (int i = 0; i < pomList.length; ++i) {
				String var10001 = LoadCommon.LINEFEED;
				LoadCommon.printToFile(FILENAME, var10001 + getPOMDetails(pomList[i], false));
			}

			eM2();
			eG2();
			LoadCommon.printToFile(FILENAME, "LCSInitMeasurements");
		} catch (WTException var2) {
			LoadCommon.printToFile(FILENAME, "#WTException : " + var2.getLocalizedMessage());
		}

	}

	protected static void eM2() {
		try {
			LCSMeasurements[] measurementsList = null;
			Vector<?> pomList = null;
			String value = null;
			LoadCommon.printToFile(FILENAME, LoadCommon.LINEFEED
					+ "#Extracting Measurement Template Items, POMS And Values ..." + LoadCommon.LINEFEED);
			Hashtable<?, ?> table = getAllMeasurementData(TYPENAME, "TEMPLATE");
			Enumeration<?> keys = table.keys();
			measurementsList = new LCSMeasurements[table.size()];

			for (int var5 = 0; keys
					.hasMoreElements(); measurementsList[var5++] = (LCSMeasurements) LCSQuery.findObjectById(value)) {
				value = (String) keys.nextElement();
			}

			measurementsList = (LCSMeasurements[]) sortList(measurementsList, "measurementName");

			for (int i = 0; i < measurementsList.length; ++i) {
				LoadCommon.printToFile(FILENAME, getMeasurementDetails(measurementsList[i]));
				pomList = (Vector) table.get("OR:" + measurementsList[i].toString());

				for (int j = 0; j < pomList.size(); ++j) {
					LoadCommon.printToFile(FILENAME, getPOMDetails((LCSPointsOfMeasure) pomList.elementAt(j), true));
				}
			}
		} catch (WTException var8) {
			LoadCommon.printToFile(FILENAME, "#WTException : " + var8.getLocalizedMessage());
		} catch (IOException var9) {
			LoadCommon.printToFile(FILENAME, "#IOException : " + var9.getLocalizedMessage());
		}

	}

	protected static void eG2() {
		LOGGER.debug("eG2()");

		try {
			LCSMeasurements[] measurementsList = null;
			Vector<?> pomList = null;
			String value = null;
			LoadCommon.printToFile(FILENAME, LoadCommon.LINEFEED
					+ "#Extracting Measurement Gradings Items, POMS And Values ..." + LoadCommon.LINEFEED);
			Hashtable<?, ?> table = getAllMeasurementData(TYPENAME, "GRADINGS");
			Enumeration<?> keys = table.keys();
			measurementsList = new LCSMeasurements[table.size()];

			for (int var5 = 0; keys
					.hasMoreElements(); measurementsList[var5++] = (LCSMeasurements) LCSQuery.findObjectById(value)) {
				value = (String) keys.nextElement();
			}

			measurementsList = (LCSMeasurements[]) sortList(measurementsList, "measurementName");

			for (int i = 0; i < measurementsList.length; ++i) {
				LoadCommon.printToFile(FILENAME, getMeasurementDetails(measurementsList[i]));
				pomList = (Vector) table.get("OR:" + measurementsList[i].toString());

				for (int j = 0; j < pomList.size(); ++j) {
					LoadCommon.printToFile(FILENAME, getPOMDetails((LCSPointsOfMeasure) pomList.elementAt(j), true));
				}
			}
		} catch (WTException var8) {
			LoadCommon.printToFile(FILENAME, "#WTException : " + var8.getLocalizedMessage());
		} catch (IOException var9) {
			LoadCommon.printToFile(FILENAME, "#IOException : " + var9.getLocalizedMessage());
		}

	}

	public static String getPOMDetails(LCSPointsOfMeasure pom, boolean measurementValues) throws WTException {
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
		String line = "LCSPointsOfMeasure" + var10000 + LoadFlexTyped.getValue(pom, "measurementName") + DELIMITER
				+ LoadFlexTyped.getValue(pom, "number");
		line = line + DELIMITER + LoadCommon.getPathFromFlexType(pom.getFlexType()) + DELIMITER + pom.getId()
				+ DELIMITER + pom.getSortingNumber();
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
			var10000 = LoadFlexTyped.getValue(pomLibrary, "measurementName");
			value = var10000 + DELIMITER + LoadFlexTyped.getValue(pomLibrary, "number");
		} else {
			value = DELIMITER;
		}

		line = line + DELIMITER + value;
		if (pomMeasurementsMaster == null) {
			value = "";
		} else {
			measurements = (LCSMeasurements) VersionHelper.latestIterationOf(pomMeasurementsMaster);
			value = pomMeasurementsMaster.getMeasurementsName();
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

		line = line + DELIMITER + value + DELIMITER + LoadCommon.getObjectAttributes(pom, DELIMITER);
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

	public static String getMeasurementDetails(LCSMeasurements measurements) throws IOException {
		String value = null;
		String var10000 = DELIMITER;
		String line = "LCSMeasurements" + var10000 + LoadFlexTyped.getValue(measurements, "name") + DELIMITER
				+ LoadCommon.getPathFromFlexType(measurements.getFlexType());
		if ((value = measurements.getMeasurementsType()) == null) {
			value = "";
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
			value = measurementMaster.getMeasurementsName();
			value = value + DELIMITER + measurementMaster.getMeasurementsType();
		} else {
			value = DELIMITER;
			// DELIMITER.ma
		}

		line = line + DELIMITER + value;
		if ("INSTANCE".equals(measurements.getMeasurementsType())
				|| "INSTANCE_SPECIFIC".equals(measurements.getMeasurementsType())) {
			LOGGER.debug("--instance---");
			value = "NO PRODUCT";
			if (measurements.getProductMaster() != null) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(
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
					if (LOGGER.isDebugEnabled()) {
						var12 = LOGGER;
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
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(
							"measurements.getSeasonMaster().getName():  " + measurements.getSeasonMaster().getName());
				}

				value = measurements.getSeasonMaster().getName();
			}

			line = line + DELIMITER + value;
			value = "NO SEASON";
			if (measurements.getSeasonMaster() != null) {
				try {
					LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(measurements.getSeasonMaster());
					if (LOGGER.isDebugEnabled()) {
						var12 = LOGGER;
						var10001 = season.getFlexType();
						var12.debug("season.getTypeName():  " + var10001.getFullName(true));
					}

					value = season.getFlexType().getFullName(true);
				} catch (WTException var6) {
					var6.printStackTrace();
				}
			}

			line = line + DELIMITER + value;
			value = "";
			if (measurements.getSource() != null) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(
							"measurements.getSource().getName():  " + measurements.getSource().getSourcingConfigName());
				}

				value = measurements.getSource().getSourcingConfigName();
			}

			line = line + DELIMITER + value;
			value = "";
			if (measurements.getSource() != null) {
				try {
					LCSSourcingConfig sourceConfig = (LCSSourcingConfig) VersionHelper
							.latestIterationOf(measurements.getSource());
					if (LOGGER.isDebugEnabled()) {
						var12 = LOGGER;
						var10001 = sourceConfig.getFlexType();
						var12.debug("sourceConfig.getFlexType().getTypeName():  " + var10001.getFullName(true));
					}

					value = sourceConfig.getFlexType().getFullName(true);
				} catch (WTException var5) {
					var5.printStackTrace();
				}
			}

			line = line + DELIMITER + value;
		}

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
		line = line + LoadCommon.getObjectAttributes(measurements, DELIMITER);
		return line;
	}

	public static LCSPointsOfMeasure[] getPointsOfMeasures(LCSMeasurements measurement, Hashtable searchTable)
			throws WTException {
		return getPointsOfMeasures(LoadCommon.getPathFromFlexType(measurement.getFlexType()), searchTable, measurement);
	}

	public static LCSPointsOfMeasure getPointsOfMeasure(String flextype, String name, String number,
			LCSMeasurements measurement) throws WTException {
		Hashtable<String, String> searchTable = new Hashtable();
		if (name != null) {
			searchTable.put("measurementName", name);
		}

		if (number != null) {
			searchTable.put("number", number);
		}

		return getPointsOfMeasure(flextype, searchTable, measurement);
	}

	public static LCSPointsOfMeasure getPointsOfMeasure(String flextype, Hashtable searchTable,
			LCSMeasurements measurement) throws WTException {
		LOGGER.debug("-E-M-getPointsOfMeasure(String flextype, Hashtable searchTable, LCSMeasurements measurement)");
		LCSPointsOfMeasure pom = null;
		LCSPointsOfMeasure[] poms = getPointsOfMeasures(flextype, searchTable, measurement);
		if (poms.length > 0) {
			pom = poms[0];
		}

		return pom;
	}

	public static LCSPointsOfMeasure[] getPointsOfMeasures(String type, Hashtable<String, String> searchTable,
			LCSMeasurements measurement) throws WTException {
		LOGGER.debug("-E-M-getPointsOfMeasures(String type, Hashtable searchTable, LCSMeasurements measurement)");
		String pomType = "LIBRARY";
		FlexTypeAttribute brand = FlexTypeCache.getFlexTypeRoot("Measurements").getAttribute("scPOMbrand");
		if (searchTable == null) {
			searchTable = new Hashtable();
		}

		if (measurement != null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("measurement != null");
				LOGGER.debug("searchTable.keySet():  " + searchTable.keySet());
				LOGGER.debug("searchTable.get(FlexTyped-pointsOfMeasureType):  "
						+ (String) searchTable.get("FlexTyped-pointsOfMeasureType"));
			}

			if (searchTable.get("FlexTyped-pointsOfMeasureType") == null) {
				pomType = "TEMPLATE";
			} else {
				pomType = (String) searchTable.get("FlexTyped-pointsOfMeasureType");
			}

			searchTable.put("FlexTyped-idA3D5",
					String.valueOf(measurement.getMaster().getPersistInfo().getObjectIdentifier().getId()));
		}

		searchTable.put("FlexTyped-dropped", "0");
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("searchTable.get(FlexTyped-pointsOfMeasureType):  "
					+ (String) searchTable.get("FlexTyped-pointsOfMeasureType"));
		}

		searchTable.put("FlexTyped-pointsOfMeasureType", pomType);
		searchTable.put("FlexTyped-" + brand.getColumnName(), REEBOK_BRAND);
		WTObject[] pomObjects = LoadCommon.getObjectsByAttributes(type, searchTable, "LCSPointsOfMeasure", true);
		LCSPointsOfMeasure[] poms = new LCSPointsOfMeasure[pomObjects.length];

		for (int i = 0; i < pomObjects.length; ++i) {
			poms[i] = (LCSPointsOfMeasure) pomObjects[i];

		}
		return poms;
	}

	public static LCSMeasurements getMeasurement(String flexType, String measurementType, String measurementName)
			throws WTException {
		Hashtable searchTable = new Hashtable();
		searchTable.put("name", measurementName);
		return getMeasurement(flexType, measurementType, searchTable);
	}

	public static LCSMeasurements getMeasurement(String flexType, String measurementType, Hashtable searchTable)
			throws WTException {
		LCSMeasurements measurement = null;
		LCSMeasurements[] measurements = getMeasurements(flexType, measurementType, searchTable);
		if (measurements.length > 0) {
			measurement = measurements[0];
		}

		return measurement;
	}

	public static LCSMeasurements[] getMeasurements(String flexType, String measurementType,
			Hashtable<String, String> searchTable) throws WTException {
		if (searchTable == null) {
			searchTable = new Hashtable();
		}

		if (measurementType != null) {
			searchTable.put("FlexTyped-MASTER-measurementsType", measurementType);
		}

		FlexType flextype = LoadCommon.getCachedFlexTypeFromPath(flexType);
		WTObject[] measurementObjects = LoadCommon.getObjectsByAttributes(flextype, searchTable,
				"LCSMeasurements,MASTER=LCSMeasurementsMaster", true);
		LCSMeasurements[] measurements = new LCSMeasurements[measurementObjects.length];

		for (int i = 0; i < measurementObjects.length; ++i) {
			measurements[i] = (LCSMeasurements) measurementObjects[i];
		}

		return measurements;
	}

	public static Hashtable getAllMeasurementData(String type, String measurementsType) {
		LOGGER.debug("getAllMeasurementData(String type, String measurementsType)");
		Hashtable measurementObjects = new Hashtable();

		try {
			FlexTypeAttribute measurementName = FlexTypeCache.getFlexTypeRoot("Measurements").getAttribute("name");
			FlexType flextype = LoadCommon.getCachedFlexTypeFromPath(type);
			PreparedQueryStatement statement = new PreparedQueryStatement();
			statement.appendFromTable(LCSMeasurementsMaster.class);
			statement.appendFromTable(LCSMeasurements.class);
			statement.appendFromTable(LCSPointsOfMeasure.class);
			statement.appendSelectColumn(
					new QueryColumn(LCSMeasurements.class, "thePersistInfo.theObjectIdentifier.id"));
			statement.appendSelectColumn(
					new QueryColumn(LCSMeasurements.class, "thePersistInfo.theObjectIdentifier.classname"));
			statement.appendSelectColumn(
					new QueryColumn(LCSPointsOfMeasure.class, "thePersistInfo.theObjectIdentifier.id"));
			statement.appendSelectColumn(
					new QueryColumn(LCSPointsOfMeasure.class, "thePersistInfo.theObjectIdentifier.classname"));
			LoadCommon.display("Searching For All Measurement Data ...");
			statement.appendCriteria(new JoinCriteria(new QueryColumn(LCSMeasurements.class, "masterReference.key.id"),
					new QueryColumn(LCSMeasurementsMaster.class, "thePersistInfo.theObjectIdentifier.id")));
			statement.appendAndIfNeeded();
			statement.appendCriteria(new JoinCriteria(
					new QueryColumn(LCSMeasurementsMaster.class, "thePersistInfo.theObjectIdentifier.id"),
					new QueryColumn(LCSPointsOfMeasure.class, "measurementsMasterReference.key.id(+)")));
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria(new QueryColumn(LCSMeasurements.class, "flexTypeIdPath"), "?", "LIKE"),
					flextype.getTypeIdPath() + "%");
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria(new QueryColumn(LCSMeasurementsMaster.class, "measurementsType"),
					measurementsType, "="));
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria(new QueryColumn(LCSMeasurements.class, measurementName.getColumnDescriptorName()),
							"%" + REEBOK + "%", Criteria.LIKE));
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria(new QueryColumn(LCSMeasurements.class, "iterationInfo.latest"), "1", "="));
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria(new QueryColumn(LCSMeasurements.class, "checkoutInfo.state"), "wrk", "<>"));
			statement.appendSortBy(new QueryColumn(LCSMeasurementsMaster.class, "measurementsName"));
			statement.appendSortBy(new QueryColumn(LCSPointsOfMeasure.class,
					flextype.getAttribute("number").getColumnDescriptorName()));
			LoadCommon.display(statement.toString());
			Vector<?> results = LCSQuery.runDirectQuery(statement).getResults();
			String lastmeasurementString = "";
			String value = "";
			Vector poms = null;
			LCSPointsOfMeasure pom = null;
			FlexObject obj = null;

			for (int i = 0; i < results.size(); ++i) {
				obj = (FlexObject) results.elementAt(i);
				pom = null;
				String var10000 = obj.getString("LCSPOINTSOFMEASURE.CLASSNAMEA2A2");
				value = "OR:" + var10000 + ":" + obj.getString("LCSPOINTSOFMEASURE.IDA2A2");
				if (!value.equals("OR::")) {
					pom = (LCSPointsOfMeasure) LCSQuery.findObjectById(value);
				}

				var10000 = obj.getString("LCSMEASUREMENTS.CLASSNAMEA2A2");
				value = "OR:" + var10000 + ":" + obj.getString("LCSMEASUREMENTS.IDA2A2");
				if (!value.equals(lastmeasurementString)) {
					poms = new Vector();
				}

				if (pom != null && !pom.isDropped() && pom.getEffectOutDate() == null) {
					poms.add(pom);
				}

				measurementObjects.put(value, poms);
				lastmeasurementString = value;
			}
		} catch (WTException var12) {
			LoadCommon.display("\n#WTException : " + var12.getLocalizedMessage());
			var12.printStackTrace();
		}

		return measurementObjects;
	}

	public static FlexTyped[] sortList(FlexTyped[] objectList, String name) {
		FlexTyped temp = null;

		for (int i = 0; i < objectList.length - 1; ++i) {
			for (int j = i + 1; j < objectList.length; ++j) {
				if (LoadFlexTyped.getValue(objectList[i], name)
						.compareTo(LoadFlexTyped.getValue(objectList[j], name)) > 0) {
					temp = objectList[i];
					objectList[i] = objectList[j];
					objectList[j] = temp;
				}
			}
		}

		return objectList;
	}

	public static void deleteAllMeasurementObjects(String name) {
		LoadCommon.SERVER_MODE = false;

		try {
			LCSPointsOfMeasure[] pomObjects = null;
			LCSMeasurements[] measurementObjects = null;
			Hashtable<String, String> table = new Hashtable();
			table.put("measurementName", name);
			pomObjects = getPointsOfMeasures(TYPENAME, table, (LCSMeasurements) null);

			int i;
			for (i = 0; i < pomObjects.length; ++i) {
				LoadCommon.display("Deleting POM Library (" + (i + 1) + " Of " + pomObjects.length + ") ....");
				LoadCommon.deleteObject(pomObjects[i]);
			}

			measurementObjects = getMeasurements(TYPENAME, "TEMPLATE", (Hashtable) null);

			for (i = 0; i < measurementObjects.length; ++i) {
				pomObjects = getPointsOfMeasures(measurementObjects[i], (Hashtable) null);

				for (int j = 0; j < pomObjects.length; ++j) {
					LoadCommon.display("Deleting POM Library (" + (j + 1) + " Of " + pomObjects.length + ") ....");
					LoadCommon.deleteObject(pomObjects[i]);
				}

				LoadCommon.display(
						"Deleting Measurement Template (" + (i + 1) + " Of " + measurementObjects.length + ") ....");
				LoadCommon.deleteObject(measurementObjects[i]);
			}
		} catch (WTException var6) {
			LoadCommon.printToFile(FILENAME, "#WTException : " + var6.getLocalizedMessage());
		}

		LoadCommon.SERVER_MODE = true;
	}
}
