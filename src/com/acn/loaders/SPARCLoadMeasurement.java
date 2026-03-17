package com.acn.loaders;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.foundation.LCSRevisionControlled;
import com.lcs.wc.load.ExtractMeasurement;
import com.lcs.wc.load.LoadCommon;
import com.lcs.wc.load.LoadFlexTyped;
import com.lcs.wc.load.LoadSku;
import com.lcs.wc.measurements.LCSMeasurements;
import com.lcs.wc.measurements.LCSMeasurementsLogic;
import com.lcs.wc.measurements.LCSMeasurementsMaster;
import com.lcs.wc.measurements.LCSMeasurementsQuery;
import com.lcs.wc.measurements.LCSPointsOfMeasure;
import com.lcs.wc.measurements.LCSPointsOfMeasureLogic;
import com.lcs.wc.measurements.MeasurementValues;
import com.lcs.wc.part.LCSPart;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sizing.FullSizeRange;
import com.lcs.wc.sizing.SizeCategory;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.specification.FlexSpecLogic;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.MetricFormatter;
import com.lcs.wc.util.VersionHelper;

import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;
import wt.method.MethodContext;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class SPARCLoadMeasurement {
	private static final Logger LOGGER = LogManager.getLogger(SPARCLoadMeasurement.class.getName());
	public static boolean DEBUG = true;

	private static final String defaultUnitOfMeasure = LCSProperties.get("com.lcs.wc.measurements.defaultUnitOfMeasure",
			"si.Length.in");
	public static final boolean USE_INCREMENTAL_GRADING = LCSProperties
			.getBoolean("com.lcs.wc.measurements.useIncrementalGrading");
	public static final String GRADING_METHOD_ATTRIBUTE = LCSProperties
			.get("com.lcs.wc.measurements.gradingMethodAttribute", "gradingMethod");
	protected static LCSPointsOfMeasureLogic POM_LOGIC = new LCSPointsOfMeasureLogic();
	protected static LCSMeasurementsLogic MEASUREMENTS_LOGIC = new LCSMeasurementsLogic();

	public static boolean createPointsOfMeasurement(Hashtable<String, Object> dataValues,
			Hashtable<String, Object> commandLine, Vector<?> returnObjects) {
		return createPointsOfMeasurement(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
	}

	public static boolean createMeasurements(Hashtable<String, Object> dataValues,
			Hashtable<String, Object> commandLine, Vector<?> returnObjects) {
		return createMeasurements(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
	}

	public static boolean createMeasurementValues(Hashtable<String, Object> dataValues,
			Hashtable<String, Object> commandLine, Vector<?> returnObjects) {
		return createMeasurementValues(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
	}

	public static boolean createGradingValues(Hashtable<String, Object> dataValues,
			Hashtable<String, Object> commandLine, Vector<?> returnObjects) {
		return createGradingValues(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
	}

	public static boolean initMeasurements(Hashtable<String, Object> dataValues, Hashtable<String, Object> commandLine,
			Vector<?> returnObjects) {
		return initMeasurements(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
	}

	protected static boolean createPointsOfMeasurement(Hashtable<String, Object> dataValues, String fileName) {
		try {
			LCSPointsOfMeasure pom = LCSPointsOfMeasure.newLCSPointsOfMeasure();
			String value = LoadCommon.getValue(dataValues, "FlexTyped-pointsOfMeasureType:SearchCriteria", false);
			if (value != null && !"LIBRARY".equalsIgnoreCase(value)) {
				if ((pom = getPOMTemplate(dataValues, fileName)) == null) {
					LoadCommon.deleteFromCache(fileName, LCSPointsOfMeasure.class);
					return false;
				}
			} else if ((pom = (LCSPointsOfMeasure) LoadCommon.getObjectByCriteria(fileName, pom, dataValues,
					"LCSPointsOfMeasure")) == null) {
				return true;
			}

			pom.setPointsOfMeasureType(value);
			if ((value = LoadCommon.getValue(dataValues, "FlexTyped-Id", false)) != null) {
				pom.setId(LoadCommon.validateInt("Id", value));
			}

			if ((value = LoadCommon.getValue(dataValues, "SortingNumber", false)) != null) {
				pom.setSortingNumber(LoadCommon.validateInt("SortingNumber", value));
			}

			if ((value = LoadCommon.getValue(dataValues, "SampleValue", false)) != null) {
				pom.setSampleValue(LoadCommon.validateFloat("SampleValue", value));
			}

			if ((value = LoadCommon.getValue(dataValues, "PlusTol", false)) != null) {
				pom.setPlusTol(LoadCommon.validateFloat("PlusTol", value));
			}

			if ((value = LoadCommon.getValue(dataValues, "MinusTol", false)) != null) {
				pom.setMinusTol(LoadCommon.validateFloat("MinusTol", value));
			}

			if ((value = LoadCommon.getValue(dataValues, "Point1", false)) != null) {
				pom.setPoint1(value);
			}

			if ((value = LoadCommon.getValue(dataValues, "Point2", false)) != null) {
				pom.setPoint2(value);
			}

			return save(pom, dataValues, fileName);
		} catch (WTPropertyVetoException var4) {
			LoadCommon.display("\n#WTPropertyVetoException : " + var4.getLocalizedMessage());
		} catch (WTException var5) {
			LoadCommon.display("\n#WTException : " + var5.getLocalizedMessage());
		}

		return false;
	}

	protected static LCSPointsOfMeasure getPOMTemplate(Hashtable<String, Object> dataValues, String fileName) {
		try {
			LCSPointsOfMeasure pom = null;
			LCSPointsOfMeasure pomLibrary = null;
			LCSMeasurements pomMeasurements = null;
			String type = LoadCommon.getValue(dataValues, "Type", true);
			if (type == null) {
				return null;
			}

			String[] values = new String[] { LoadCommon.getValue(dataValues, "LibraryName"),
					LoadCommon.getValue(dataValues, "LibraryNumber"), null };
			if ((values[2] = LoadCommon.getValue(dataValues, "MeasurementName", true)) == null) {
				return null;
			}

			if ((pomMeasurements = (LCSMeasurements) LoadCommon.getCache(fileName, "CURRENT_MEASUREMENTS")) == null) {
				LoadCommon.display("#ERROR - For This POM Template, No Measurement Exists With Name '" + values[2]
						+ "' And Type '" + type + "' !!!");
				return null;
			}

			if ((values[0] != null || values[1] != null) && (pomLibrary = getPointsOfMeasure(type, values[0], values[1],
					(LCSMeasurements) null, fileName)) == null) {
				LoadCommon.display("#WARNING - No POM Exists In the Library With Name '" + values[0] + "' Number '"
						+ values[1] + "' And Type '" + type + "' Library Ref will not be set. !!!");
			}

			Hashtable<?, ?> searchTable = LoadCommon.getCriteria(dataValues, ":SearchCriteria");
			String measurementName = "";
			String number = "";
			if (searchTable.get("measurementName") != null) {
				measurementName = (String) searchTable.get("measurementName");
			}

			if (searchTable.get("number") != null) {
				number = (String) searchTable.get("number");
			}

			if (!number.equals("") && !measurementName.equals("")
					&& (pom = ExtractMeasurement.getPointsOfMeasure(type, searchTable, pomMeasurements)) != null) {
				LoadCommon.display("Updating POM Template With Values " + searchTable + " And Type '" + type + "' ...");
			} else {
				LoadCommon.display("Creating POM Template With Values " + searchTable + " And Type '" + type + "' ...");
				pom = LCSPointsOfMeasure.newLCSPointsOfMeasure();
				pom.setFlexType(LoadCommon.getCachedFlexTypeFromPath(type));
				pom.setMeasurements(pomMeasurements);
				pom.setMeasurementsMaster(pomMeasurements.getMaster());
				pom.setLibraryItem(pomLibrary);
				pom.setMeasurementValues(new Hashtable());
			}

			return pom;
		} catch (WTPropertyVetoException var10) {
			LoadCommon.display("\n#WTPropertyVetoException : " + var10.getLocalizedMessage());
		} catch (WTException var11) {
			LoadCommon.display("\n#WTException : " + var11.getLocalizedMessage());
		}

		return null;
	}

	protected static boolean createMeasurements(Hashtable<String, Object> dataValues, String fileName) {
		try {
			String value = null;
			if (DEBUG) {
				LoadCommon.display(dataValues.toString());
			}

			LCSMeasurements measurements = LCSMeasurements.newLCSMeasurements();
			FlexSpecification spec = null;
			if ((measurements = (LCSMeasurements) LoadCommon.getObjectByCriteria(fileName, measurements, dataValues,
					"LCSMeasurements,MASTER=LCSMeasurementsMaster")) == null) {
				return true;
			}

			if ((value = LoadCommon.getValue(dataValues, "FlexTyped-MASTER-measurementsType", false)) != null) {
				measurements.setMeasurementsType(value);
			}

			String productName;
			String fObj;
			if (measurements.getMeasurementsType().equals("INSTANCE")
					|| "INSTANCE_SPECIFIC".equals(measurements.getMeasurementsType())) {
				productName = LoadCommon.getValue(dataValues, "ProductName");
				fObj = LoadCommon.getValue(dataValues, "ProductType");
				if (!FormatHelper.hasContent(fObj)) {
					fObj = "Product";
				}

				String seasonName = LoadCommon.getValue(dataValues, "SeasonName");
				String seasonType = LoadCommon.getValue(dataValues, "SeasonType");
				if (!FormatHelper.hasContent(seasonType)) {
					seasonType = "Season";
				}

				String sourceName = LoadCommon.getValue(dataValues, "SourceName", false);
				String sourcingType = LoadCommon.getValue(dataValues, "SourceType");
				if (!FormatHelper.hasContent(sourcingType)) {
					sourcingType = "Sourcing Configuration";
				}

				String specName = LoadCommon.getValue(dataValues, "SpecificationName", false);
				LOGGER.debug("----------------------");
				LOGGER.debug("isInstance && save");
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("productName = " + productName);
					LOGGER.debug("productType = " + fObj);
					LOGGER.debug("seasonName = " + seasonName);
					LOGGER.debug("seasonType= " + seasonType);
					LOGGER.debug("sourceName= " + sourceName);
					LOGGER.debug("sourcingType= " + sourcingType);
				}

				FlexType productFlextype = FlexTypeCache.getFlexTypeFromPath(fObj);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("productFlextype.getFullName(true)" + productFlextype.getFullName(true));
				}

				LCSProduct product = null;
				if (productFlextype != null) {
					product = (new LCSProductQuery()).findProductByNameType(productName, productFlextype);
				}

				if (product != null) {
					LOGGER.debug("product.getName() = " + product.getName());
				}

				if (product == null) {
					System.out.println("---Could not find a product--returning");
					return false;
				}

				FlexType seasonFlextype = FlexTypeCache.getFlexTypeFromPath(seasonType);
				LCSSeason season = null;
				LCSSeasonQuery seasonQuery = new LCSSeasonQuery();
				if (seasonFlextype != null) {
					season = seasonQuery.findSeasonByNameType(seasonName, seasonFlextype);
				}

				if (season != null) {
					LOGGER.debug("---season.getName():  " + season.getName());
				}

				if (season == null) {
					System.out.println("---Could not find a season--returning");
					return false;
				}

				product = LCSSeasonQuery.getProductForSeason(product, season.getMaster());
				LCSPartMaster productMaster = product.getMaster();
				LCSSeasonMaster seasonMaster = season.getMaster();
				LCSSourcingConfigMaster sourcingMaster = null;
				LOGGER.debug("----set productMaster && seasonMaster");
				SearchResults sourcing;
				if (FormatHelper.hasContent(sourceName)) {
					sourcing = LCSSourcingConfigQuery.getSourcingConfigDataForProductSeason(productMaster, seasonMaster,
							false);
					Collection<LCSSourcingConfig> configs = LCSSourcingConfigQuery.getObjectsFromResults(sourcing,
							"VR:com.lcs.wc.sourcing.LCSSourcingConfig:", "LCSSOURCINGCONFIG.BRANCHIDITERATIONINFO");
					Iterator var22 = configs.iterator();

					while (var22.hasNext()) {
						LCSSourcingConfig sourcing1 = (LCSSourcingConfig) var22.next();
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("sourcing.getSourcingConfigName()>" + sourcing1.getSourcingConfigName() + "<");
						}

						if (sourcing1.getSourcingConfigName().equalsIgnoreCase(sourceName)) {
							LOGGER.debug("--matched--");
							sourcingMaster = sourcing1.getMaster();
							break;
						}
					}
				}

				if (FormatHelper.hasContent(specName)) {
					sourcing = null;
					SearchResults SpecFlexObjects = FlexSpecQuery.findExistingSpecs(product, season, sourcingMaster);
					Collection<FlexSpecification> specs = LCSQuery.getObjectsFromResults(SpecFlexObjects,
							"VR:com.lcs.wc.specification.FlexSpecification:",
							"FLEXSPECIFICATION.BRANCHIDITERATIONINFO");
					Iterator var37 = specs.iterator();

					while (var37.hasNext()) {
						FlexSpecification flexSpec = (FlexSpecification) var37.next();
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("flexSpec.getValue(\"specName\")>" + flexSpec.getValue("specName") + "<");
						}

						if (flexSpec.getValue("specName").toString().equalsIgnoreCase(specName)) {
							LOGGER.debug("--matched--");
							spec = flexSpec;
							break;
						}
					}
				}

				measurements.setSeasonMaster(seasonMaster);
				measurements.setProductMaster(productMaster);
				if (sourcingMaster != null) {
					LOGGER.debug("-!-setting sourcing master");
					measurements.setSource(sourcingMaster);
				} else {
					LOGGER.debug("--didn't set sourcing master");
				}

				LOGGER.debug("-----done-----");
				LOGGER.debug("---------------");
			}

			if ((value = LoadCommon.getValue(dataValues, "SizeRun", false)) != null) {
				measurements.setSizeRun(value);
			}

			if ((value = LoadCommon.getValue(dataValues, "SampleSize", false)) != null) {
				measurements.setSampleSize(value);
			}

			if ((measurements.getMeasurementsType().equals("INSTANCE")
					|| "INSTANCE_SPECIFIC".equals(measurements.getMeasurementsType()))
					&& (value = LoadCommon.getValue(dataValues, "SourceTemplate", false)) != null) {
				productName = LoadCommon.getValue(dataValues, "SourceTemplateType", true);
				if (productName == null) {
					return false;
				}

				LCSMeasurements sourceTemplate = getMeasurement(productName, value, "TEMPLATE", fileName);
				if (sourceTemplate == null) {
					LoadCommon.display(
							"No Measurements Exists With Name '" + value + "' And Type '" + productName + " !!!");
					return false;
				}

				LoadCommon.display("Setting Measurements Object To '" + sourceTemplate + "' ...");
				measurements.setSourceTemplate(sourceTemplate.getMaster());
			}
			
			if ((measurements.getMeasurementsType().equals("INSTANCE")
					|| "INSTANCE_SPECIFIC".equals(measurements.getMeasurementsType()))
					&& (value = LoadCommon.getValue(dataValues, "GradeTemplate", false)) != null) {
				productName = LoadCommon.getValue(dataValues, "GradeTemplateType", true);
				if (productName == null) {
					return false;
				}

				LCSMeasurements gradeTemplate = getMeasurement(productName, value, "GRADINGS", fileName);
				if (gradeTemplate == null) {
					LoadCommon.display(
							"No Measurements Exists With Name '" + value + "' And Type '" + productName + " !!!");
					return false;
				}

				LoadCommon.display("Setting Garde Object To '" + gradeTemplate + "' ...");
				measurements.setGradings(gradeTemplate.getMaster());
			}

			Vector results;
			FlexObject fObj1;
			if ((value = LoadCommon.getValue(dataValues, "SizeCategory", false)) != null) {
				results = SizingQuery.findSizeCategoriesByName(value).getResults();
				fObj1 = null;
				if (results.size() > 0) {
					fObj1 = (FlexObject) results.firstElement();
					SizeCategory sizeCat = (SizeCategory) LCSQuery
							.findObjectById("com.lcs.wc.sizing.SizeCategory:" + fObj1.getString("SIZECATEGORY.IDA2A2"));
					measurements.setSizeCategory(sizeCat);
				} else {
					LoadCommon.display("#Warning - Could not find a Size Category with name '" + value
							+ "'.  Not setting a Size Category");
				}
			}

			if ((value = LoadCommon.getValue(dataValues, "FullSizeRange", false)) != null) {
				results = SizingQuery.findFullSizeRangesByName(value).getResults();
				fObj1 = null;
				if (results.size() > 0) {
					fObj1 = (FlexObject) results.firstElement();
					FullSizeRange fullSizeRange = (FullSizeRange) LCSQuery.findObjectById(
							"com.lcs.wc.sizing.FullSizeRange:" + fObj1.getString("FULLSIZERANGE.IDA2A2"));
					measurements.setFullSizeRange(fullSizeRange);
				} else {
					LoadCommon.display("#Warning - Could not find a Full Size Range with name '" + value
							+ "'.  Not setting a Full Size Range");
				}
			}

			loadThumbnail(measurements, dataValues);
			boolean success = save(measurements, dataValues, fileName);
			if (spec != null) {
				FlexSpecLogic specLogic = new FlexSpecLogic();
				specLogic.addComponentToSpec(spec, (LCSMeasurements) PersistenceHelper.manager.refresh(measurements));
			}

			return success;
		} catch (WTPropertyVetoException var25) {
			if (DEBUG) {
				var25.printStackTrace();
			}

			LoadCommon.display("\n#WTPropertyVetoException : " + var25.getLocalizedMessage());
		} catch (WTException var26) {
			if (DEBUG) {
				var26.printStackTrace();
			}

			LoadCommon.display("\n#WTException : " + var26.getLocalizedMessage());
		} catch (IOException var27) {
			if (DEBUG) {
				var27.printStackTrace();
			}

			LoadCommon.display("\n#IOException : " + var27.getLocalizedMessage());
		}

		return false;
	}

	protected static boolean createMeasurementValues(Hashtable<String, Object> dataValues, String fileName) {
		try {
			LOGGER.debug(dataValues);
			LCSPointsOfMeasure pom = (LCSPointsOfMeasure) LoadCommon.getCache(fileName, "CURRENT_POINTSOFMEASURE");
			if (pom == null) {
				LoadCommon.display("No Points Of Measure Object In Cache !!!");
				return false;
			}

			String value = null;
			String key = LoadCommon.getValue(dataValues, "Key", true);
			if (key == null) {
				return false;
			}

			if (key.equalsIgnoreCase("Start Of MeasurementValues")) {
				LoadCommon.display("Initializing MeasurementValues ...");
				pom.setMeasurementValues(new Hashtable());
			} else if (key.equalsIgnoreCase("End Of MeasurementValues")) {
				LoadCommon.display("Saving MeasurementValues ...");
				updateGradingValues(pom);
				PersistenceServerHelper.manager.update(pom);
				pom = (LCSPointsOfMeasure) PersistenceHelper.manager.refresh(pom);
			} else {
				MeasurementValues mValue = new MeasurementValues();
				if ((value = LoadCommon.getValue(dataValues, "Value", false)) != null) {
					mValue.setValue(LoadCommon.validateFloat("Value", value));
				}

				if ((value = LoadCommon.getValue(dataValues, "Grading", false)) != null) {
					mValue.setGrading(LoadCommon.validateFloat("Grading", value));
				}

				if ((value = LoadCommon.getValue(dataValues, "OverrideValue", false)) != null) {
					mValue.setOverrideValue(LoadCommon.validateFloat("OverrideValue", value));
				}

				if ((value = LoadCommon.getValue(dataValues, "ActualValue", false)) != null) {
					mValue.setActualValue(LoadCommon.validateFloat("ActualValue", value));
				}

				Hashtable<String, MeasurementValues> table = pom.getMeasurementValues();
				table.put(key, mValue);
			}

			LoadCommon.putCache(fileName, pom);
			putPOMInCache(fileName, pom);
			return true;
		} catch (WTPropertyVetoException var7) {
			if (DEBUG) {
				var7.printStackTrace();
			}

			LoadCommon.display("\n#WTPropertyVetoException : " + var7.getLocalizedMessage());
		} catch (WTException var8) {
			if (DEBUG) {
				var8.printStackTrace();
			}

			LoadCommon.display("\n#WTException : " + var8.getLocalizedMessage());
		}

		return false;
	}

	protected static boolean createGradingValues(Hashtable<String, Object> dataValues, String fileName) {
		try {
			System.out.println(dataValues);
			LCSPointsOfMeasure pom = (LCSPointsOfMeasure) LoadCommon.getCache(fileName, "CURRENT_POINTSOFMEASURE");
			if (pom == null) {
				LoadCommon.display("No Points Of Measure Object In Cache !!!");
				return false;
			}

			String value = null;
			String key = LoadCommon.getValue(dataValues, "Key", true);
			if (key == null) {
				return false;
			}

			if (key.equalsIgnoreCase("Start Of MeasurementValues")) {
				LoadCommon.display("Initializing MeasurementValues ...");
				pom.setMeasurementValues(new Hashtable());
			} else if (key.equalsIgnoreCase("End Of MeasurementValues")) {
				LoadCommon.display("Saving MeasurementValues ...");
				pom = updateGradingValues(pom);
				PersistenceServerHelper.manager.update(pom);
				pom = (LCSPointsOfMeasure) PersistenceHelper.manager.refresh(pom);
			} else {
				MeasurementValues mValue = new MeasurementValues();
				if ((value = LoadCommon.getValue(dataValues, "Value", false)) != null) {
					mValue.setValue(LoadCommon.validateFloat("Value", value));
				}

				if ((value = LoadCommon.getValue(dataValues, "Grading", false)) != null) {
					mValue.setGrading(LoadCommon.validateFloat("Grading", value));
				}

				if ((value = LoadCommon.getValue(dataValues, "OverrideValue", false)) != null) {
					mValue.setOverrideValue(LoadCommon.validateFloat("OverrideValue", value));
				}

				if ((value = LoadCommon.getValue(dataValues, "ActualValue", false)) != null) {
					mValue.setActualValue(LoadCommon.validateFloat("ActualValue", value));
				}

				Hashtable<String, MeasurementValues> table = pom.getMeasurementValues();
				table.put(key, mValue);
			}

			LoadCommon.putCache(fileName, pom);
			putPOMInCache(fileName, pom);
			return true;
		} catch (WTPropertyVetoException var7) {
			if (DEBUG) {
				var7.printStackTrace();
			}

			LoadCommon.display("\n#WTPropertyVetoException : " + var7.getLocalizedMessage());
		} catch (WTException var8) {
			if (DEBUG) {
				var8.printStackTrace();
			}

			LoadCommon.display("\n#WTException : " + var8.getLocalizedMessage());
		}

		return false;
	}

	private static LCSPointsOfMeasure updateGradingValues(LCSPointsOfMeasure pom)
			throws WTException, WTPropertyVetoException {
		double NULL_VALUE_PLACEHOLDER = Double.parseDouble(LCSMeasurementsQuery.NULL_VALUE_PLACEHOLDER);
		MeasurementValues mv = null;
		MeasurementValues mv2 = null;
		LCSMeasurementsMaster measurementsMaster = pom.getMeasurementsMaster();
		LCSMeasurements measurements = (LCSMeasurements) VersionHelper.latestIterationOf(measurementsMaster);
		String uom = null;
		String currentValue;
		if (measurements.getValue("uom") != null) {
			uom = measurements.getValue("uom").toString();
			uom = "si.Length." + uom;
			String currentValue1;
			if (pom.getValue("plusTolerance") != null
					&& Double.parseDouble(pom.getValue("plusTolerance").toString()) != NULL_VALUE_PLACEHOLDER) {
				currentValue1 = pom.getValue("plusTolerance").toString();
				currentValue1 = MetricFormatter.parseNumberFromInputUomToOutputUom(currentValue1, -1, uom,
						defaultUnitOfMeasure);
				pom.setValue("plusTolerance", Float.valueOf(currentValue1));
			}

			if (pom.getValue("minusTolerance") != null
					&& Double.parseDouble(pom.getValue("minusTolerance").toString()) != NULL_VALUE_PLACEHOLDER) {
				currentValue1 = pom.getValue("minusTolerance").toString();
				currentValue1 = MetricFormatter.parseNumberFromInputUomToOutputUom(currentValue1, -1, uom,
						defaultUnitOfMeasure);
				pom.setValue("minusTolerance", Float.valueOf(currentValue1));
			}
		}

		Hashtable<String, MeasurementValues> table = new Hashtable();
		Vector<String> vector = (Vector) MOAHelper.getMOACollection(measurements.getSizeRun());
		String size;
		String newValue;
		int baseSizeIndex;
		if (USE_INCREMENTAL_GRADING && measurements.getMeasurementsType().equals("GRADINGS")
				&& "incremental".equals(measurements.getValue(GRADING_METHOD_ATTRIBUTE))) {
			baseSizeIndex = vector.indexOf(measurements.getSampleSize());
			mv = (MeasurementValues) pom.getMeasurementValues().get(measurements.getSampleSize());
			currentValue = Float.toString(pom.getSampleValue());
			if (uom != null && (double) mv.getValue() != NULL_VALUE_PLACEHOLDER) {
				newValue = MetricFormatter.parseNumberFromInputUomToOutputUom(currentValue, -1, uom,
						defaultUnitOfMeasure);
				mv.setValue(Float.parseFloat(newValue));
			} else {
				mv.setValue(Float.parseFloat(currentValue));
			}

			table.put(measurements.getSampleSize(), mv);

			String size2;
			float value;
			float value2;
			int i;
			for (i = baseSizeIndex + 1; i < vector.size(); ++i) {
				size = (String) vector.get(i);
				size2 = (String) vector.get(i - 1);
				mv = (MeasurementValues) pom.getMeasurementValues().get(size);
				if (mv != null && (double) mv.getValue() != NULL_VALUE_PLACEHOLDER) {
					mv2 = (MeasurementValues) pom.getMeasurementValues().get(size2);
					value = mv.getValue();
					value2 = mv2.getValue();
					if (uom != null) {
						value = Float.parseFloat(MetricFormatter.parseNumberFromInputUomToOutputUom(
								Float.toString(value), -1, uom, defaultUnitOfMeasure));
					}

					currentValue = String.valueOf(value + value2);
					mv.setValue(Float.parseFloat(currentValue));
				}

				table.put(size, mv);
			}

			for (i = baseSizeIndex - 1; i >= 0; --i) {
				size = (String) vector.get(i);
				size2 = (String) vector.get(i + 1);
				mv = (MeasurementValues) pom.getMeasurementValues().get(size);
				if (mv != null && (double) mv.getValue() != NULL_VALUE_PLACEHOLDER) {
					mv2 = (MeasurementValues) pom.getMeasurementValues().get(size2);
					value = mv.getValue();
					value2 = (mv2 != null) ? mv2.getValue() : 0.0f;
					if (uom != null) {
						value = Float.parseFloat(MetricFormatter.parseNumberFromInputUomToOutputUom(
								Float.toString(value), -1, uom, defaultUnitOfMeasure));
					}

					currentValue = String.valueOf(value + value2);
					mv.setValue(Float.parseFloat(currentValue));
				}
				if(size != null && mv != null){
					table.put(size, mv);
				}
				
			}

			pom.setMeasurementValues(table);
		} else if (uom != null) {
			for (baseSizeIndex = 0; baseSizeIndex < vector.size(); ++baseSizeIndex) {
				size = (String) vector.get(baseSizeIndex);
				mv = (MeasurementValues) pom.getMeasurementValues().get(size);
				if (mv != null && (double) mv.getValue() != NULL_VALUE_PLACEHOLDER) {
					currentValue = String.valueOf(mv.getValue());
					if (currentValue != null) {
						newValue = MetricFormatter.parseNumberFromInputUomToOutputUom(currentValue, -1, uom,
								defaultUnitOfMeasure);
						mv.setValue(Float.parseFloat(newValue));
						table.put(size, mv);
					}
				}
			}

			pom.setMeasurementValues(table);
		}

		return pom;
	}

	protected static boolean initMeasurements(Hashtable<String, Object> dataValues, String fileName) {
		LoadCommon.deleteCache(fileName, "POM_OBJECTS");
		LoadCommon.deleteCache(fileName, "MEASUREMENTS_OBJECTS");
		return true;
	}

	public static LCSPointsOfMeasure getPointsOfMeasure(String flextype, String name, String number,
			LCSMeasurements measurement, String fileName) {
		LCSPointsOfMeasure pom = null;
		Hashtable<String, Object> pomObjects = (Hashtable) LoadCommon.getCache(fileName, "POM_OBJECTS");
		if (pomObjects == null) {
			pomObjects = new Hashtable();
		}

		String key = flextype + "-" + name + "-" + number + "-";
		if (measurement == null) {
			key = key + "LIBRARY";
		} else {
			key = key + "TEMPLATE - " + LoadFlexTyped.getValue(measurement, "name");
		}

		if ((pom = (LCSPointsOfMeasure) pomObjects.get(key)) != null) {
			LoadCommon.display("Retrieved Points Of Measure From Cache ...");
			return pom;
		} else {
			try {
				if ((pom = ExtractMeasurement.getPointsOfMeasure(flextype, name, number, measurement)) != null) {
					putPOMInCache(fileName, pom);
				}
			} catch (WTException var9) {
				LoadCommon.display("\n#WTException : " + var9.getLocalizedMessage());
			}

			return pom;
		}
	}

	public static LCSMeasurements getMeasurement(String flextype, String name, String type, String fileName) {
		LCSMeasurements measurements = null;
		Hashtable<String, Object> measurementsObjects = (Hashtable) LoadCommon.getCache(fileName,
				"MEASUREMENTS_OBJECTS");
		if (measurementsObjects == null) {
			measurementsObjects = new Hashtable();
		}

		if ((measurements = (LCSMeasurements) measurementsObjects.get(flextype + "-" + name + "-" + type)) != null) {
			LoadCommon.display("Retrieved Measurement From Cache ...");
			return measurements;
		} else {
			try {
				if ((measurements = ExtractMeasurement.getMeasurement(flextype, type, name)) != null) {
					putMeasurementsInCache(fileName, measurements);
				}
			} catch (WTException var7) {
				LoadCommon.display("\n#WTException : " + var7.getLocalizedMessage());
			}

			return measurements;
		}
	}

	protected static void putPOMInCache(String fileName, LCSPointsOfMeasure pom) {
		Hashtable<String, Object> pomObjects = (Hashtable) LoadCommon.getCache(fileName, "POM_OBJECTS");
		if (pomObjects == null) {
			pomObjects = new Hashtable();
		}

		String var10000 = LoadCommon.getPathFromFlexType(pom.getFlexType());
		String key = var10000 + "-" + LoadFlexTyped.getValue(pom, "measurementName") + "-"
				+ LoadFlexTyped.getValue(pom, "number") + "-";
		LCSMeasurements measurement = pom.getMeasurements();
		if (measurement == null) {
			key = key + "LIBRARY";
		} else {
			key = key + "TEMPLATE - " + LoadFlexTyped.getValue(measurement, "name");
		}

		pomObjects.put(key, pom);
		LoadCommon.putCache(fileName, "POM_OBJECTS", pomObjects);
	}

	protected static void putMeasurementsInCache(String fileName, LCSMeasurements measurements) {
		Hashtable<String, Object> pomObjects = (Hashtable) LoadCommon.getCache(fileName, "MEASUREMENTS_OBJECTS");
		if (pomObjects == null) {
			pomObjects = new Hashtable();
		}

		String var10000 = LoadCommon.getPathFromFlexType(measurements.getFlexType());
		String key = var10000 + "-" + measurements.getMeasurementsName() + "-" + measurements.getMeasurementsType();
		pomObjects.put(key, measurements);
		LoadCommon.putCache(fileName, "MEASUREMENTS_OBJECTS", pomObjects);
	}

	protected static void loadThumbnail(FlexTyped obj, Hashtable<String, Object> dataValues)
			throws UnsupportedEncodingException, WTPropertyVetoException {
		String value = LoadCommon.getValue(dataValues, "Image", false);
		if (value != null) {
			value = LoadCommon.LOAD_DIRECTORY + "Images" + File.separator + value;
			if ((value = LoadCommon.uploadFile(value)) != null) {
				value = LoadSku.IMAGE_URL + "/" + value;
				if (obj instanceof LCSPart) {
					((LCSPart) obj).setPartPrimaryImageURL(value);
				} else if (obj instanceof LCSRevisionControlled) {
					((LCSRevisionControlled) obj).setPrimaryImageURL(value);
				}
			}

		}
	}

	protected static boolean save(FlexTyped flextyped, Hashtable<String, Object> dataValues, String fileName)
			throws WTException {
		String deriveStringsValue = null;
		if (dataValues.containsKey("DERIVE_STRINGS")) {
			deriveStringsValue = (String) dataValues.get("DERIVE_STRINGS");
		} else {
			deriveStringsValue = "false";
		}

		MethodContext.getContext().put("DERIVE_STRINGS", deriveStringsValue);
		String deriveNumericValue = null;
		if (dataValues.containsKey("DERIVE_NUMERICS")) {
			deriveNumericValue = (String) dataValues.get("DERIVE_NUMERICS");
		} else {
			deriveNumericValue = "false";
		}

		MethodContext.getContext().put("DERIVE_NUMERICS", deriveNumericValue);
		if (!(flextyped instanceof LCSProduct) && !(flextyped instanceof LCSSKU)) {
			LoadFlexTyped.setAttributes(flextyped, dataValues, fileName);
		}

		String updateMode = LoadCommon.getValue(dataValues, "UPDATEMODE");
		LoadCommon.display("Saving " + flextyped + " With UpdateMode = " + updateMode);
		if (PersistenceHelper.isPersistent(flextyped) && updateMode != null) {
			if (updateMode.equalsIgnoreCase("IGNORE")) {
				LoadCommon.display("The Object Has Not Been Saved Yet ...");
			} else if (updateMode.equalsIgnoreCase("NOITERATE_PLUGIN")) {
				LCSLogic.deriveFlexTypeValues(flextyped);
				flextyped = (FlexTyped) LCSLogic.persist((Persistable) flextyped, false, true);
			} else if (updateMode.equalsIgnoreCase("NOITERATE_NOPLUGIN")) {
				PersistenceServerHelper.manager.update((Persistable) flextyped);
				flextyped = (FlexTyped) PersistenceServerHelper.manager.restore((WTObject) flextyped);
				LCSLogic.loadMethodContextCache((Persistable) flextyped);
			} else {
				LoadCommon
						.display("Invalid Value For UPDATEMODE '" + updateMode + "' The Object Has Not Been Saved !!!");
			}

			MethodContext.getContext().remove("DERIVE_STRINGS");
			MethodContext.getContext().remove("DERIVE_NUMERICS");
			LoadCommon.putCache(fileName, flextyped);
			return true;
		} else {
			try {
				MethodContext.getContext().put("EVENT_BASED_VALIDATOR_SERVICE__BYPASS_VALIDATION", "true");
				flextyped = saveObject(flextyped, dataValues);
			} finally {
				MethodContext.getContext().put("EVENT_BASED_VALIDATOR_SERVICE__BYPASS_VALIDATION", "false");
			}

			if (flextyped == null) {
				MethodContext.getContext().remove("DERIVE_STRINGS");
				MethodContext.getContext().remove("DERIVE_NUMERICS");
				return false;
			} else if ((flextyped = LoadCommon.postLoadProcess(flextyped, dataValues, fileName)) == null) {
				MethodContext.getContext().remove("DERIVE_STRINGS");
				MethodContext.getContext().remove("DERIVE_NUMERICS");
				return false;
			} else {
				LoadCommon.putCache(fileName, flextyped);
				if (flextyped instanceof LCSMeasurements) {
					putMeasurementsInCache(fileName, (LCSMeasurements) flextyped);
				} else if (flextyped instanceof LCSPointsOfMeasure) {
					putPOMInCache(fileName, (LCSPointsOfMeasure) flextyped);
				} else if (flextyped instanceof LCSSKU) {
					LoadCommon.putCache(fileName, "CURRENT_PRODUCT", ((LCSSKU) flextyped).getProduct());
				} else if (flextyped instanceof LCSProduct) {
					LoadCommon.putCache(fileName, "CURRENT_PRODUCT", flextyped);
				}

				MethodContext.getContext().remove("DERIVE_STRINGS");
				MethodContext.getContext().remove("DERIVE_NUMERICS");
				return true;
			}
		}
	}

	private static FlexTyped saveObject(FlexTyped flextyped, Hashtable<String, Object> dataValues) throws WTException {

		if (flextyped instanceof LCSMeasurements) {
			flextyped = MEASUREMENTS_LOGIC.saveMeasurements((LCSMeasurements) flextyped);
		} else if (flextyped instanceof LCSPointsOfMeasure) {
			flextyped = POM_LOGIC.savePointsOfMeasure((LCSPointsOfMeasure) flextyped);
		}

		return (FlexTyped) flextyped;
	}

	public static void main(String[] argv) {
		LoadCommon.SERVER_MODE = false;
		Hashtable<String, Object> dataValues = new Hashtable();
		if (argv.length > 0 && argv[0].equalsIgnoreCase("-DELETE")) {
			for (int i = 1; i < argv.length; ++i) {
				ExtractMeasurement.deleteAllMeasurementObjects(argv[i]);
			}

			System.exit(0);
		}

		String value = null;
		String[][] tableValues = LoadCommon.getValuesFromFile(argv[0], "\t");

		for (int i = 0; i < tableValues.length; ++i) {
			value = "";
			if (tableValues[i].length > 1) {
				value = tableValues[i][1];
			}

			dataValues.put(tableValues[i][0], value);
		}

		createPointsOfMeasurement(dataValues, argv[0]);
		LoadCommon.SERVER_MODE = true;
	}
}
