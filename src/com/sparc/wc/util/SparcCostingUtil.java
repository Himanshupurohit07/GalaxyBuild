package com.sparc.wc.util;

import com.lcs.wc.db.*;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.*;
import com.lcs.wc.flextype.FlexType;
import java.sql.Timestamp;

import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import org.apache.logging.log4j.Logger;
import wt.enterprise.RevisionControlled;
import wt.log4j.LogR;
import wt.util.WTException;
import com.lcs.wc.util.LCSProperties;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.function.BiPredicate;


import com.lcs.wc.country.LCSCountry;

import static com.lcs.wc.util.FormatHelper.getNumericVersionIdFromObject;
import static com.sparc.wc.util.SparcCostingConstants.SOURCING_QUERY;


public class SparcCostingUtil {

    private static Map<String,String> htsAttributesMap;

    private static final String  HTS_PREFIX = "com.sparc.wc.costing.hts.";

    static {
        htsAttributesMap = LCSProperties.getPropertyEntriesStartWith(HTS_PREFIX);
    }

    private static final Logger LOGGER = LogR.getLogger(SparcCostingUtil.class.getName());
    public static LCSSourcingConfig getSourcingConfig(LCSProductCostSheet objCostSheet) {
        LCSSourcingConfig objSourcingConfig = null;

        if (objCostSheet.getSourcingConfigMaster() != null) {
            try {
                objSourcingConfig = (LCSSourcingConfig) VersionHelper.latestIterationOf(objCostSheet.getSourcingConfigMaster());
            } catch (WTException e) {
                LOGGER.error("Exception thrown while getting the Sourcing Config Object--"+e.getMessage());
                e.printStackTrace();
            }
        }

        return objSourcingConfig;
    }

    public static Collection<LCSCostSheet> getCostSheetsForSourcingConfig(LCSSourcingConfig objSourcingConfig) {
        Collection<LCSCostSheet> costSheets = new ArrayList<>();
        try {
            costSheets=LCSCostSheetQuery.getAllCostSheetsForSourcingConfig(objSourcingConfig);
        } catch (WTException e) {
            LOGGER.error("Exception while getting the costsheets---"+e.getMessage());
        }
        return costSheets;
    }

    public static LCSSeason getLatestSeason(LCSProductCostSheet objCostSheet) {
        LCSSeason objSeason = null;
        try {
            objSeason = (LCSSeason) VersionHelper.latestIterationOf(objCostSheet.getSeasonMaster());
        } catch (WTException e) {
            LOGGER.error("Exception while getting the Season---"+e.getMessage());
            e.printStackTrace();
        }
        return objSeason;
    }

    public static LCSSourceToSeasonLink getSourceSeasonLink(LCSSourcingConfig objSourcingConfig, LCSCostSheet objCostSheet) throws WTException {
        LCSSourceToSeasonLink sourceToSeasonLink = null;
        try{
            sourceToSeasonLink = SOURCING_QUERY.getSourceToSeasonLink((LCSSourcingConfigMaster) objSourcingConfig.getMaster(), objCostSheet.getSeasonMaster());
        }catch(WTException ex){
            LOGGER.error("Exception while getting the Source to season link---"+ex.getMessage());
            ex.printStackTrace();
        }
        return sourceToSeasonLink;
    }

    public static LCSCountry getCountry(LCSSourcingConfig objSourcingConfig, String strFinishedGoodsFctKey, String strCountryKey) {
        LCSCountry objCountryOrigin = null;
        try {
            LCSSupplier objSupplier = FormatHelper.equalsWithNull(objSourcingConfig.getValue(strFinishedGoodsFctKey), null) ? null : (LCSSupplier) objSourcingConfig.getValue(strFinishedGoodsFctKey);
            objCountryOrigin = FormatHelper.equalsWithNull(objSupplier, null) ? null : FormatHelper.equalsWithNull(objSupplier.getValue(strCountryKey), null) ? null : (LCSCountry) objSupplier.getValue(strCountryKey);
        } catch (WTException ex) {
            LOGGER.error("Exception while getting the Source to season link---" + ex.getMessage());
            ex.printStackTrace();
        }
        return objCountryOrigin;
    }

    public static LCSCountry getCountry(LCSProductCostSheet objCostSheet,String strKeyName) {
         LCSCountry objCountryOrigin = null;
        try{
            objCountryOrigin = objCostSheet.getValue(strKeyName) != null ? (LCSCountry) objCostSheet.getValue(strKeyName) : null;
        }catch(WTException ex){
            LOGGER.error("Exception while getting the Source to season link---"+ex.getMessage());
            ex.printStackTrace();
        }
        return objCountryOrigin;
    }



    public static PreparedQueryStatement createQueryStatement(Timestamp objTimestamp, String strHTSCodeValue, String originCountryId, String desCountryId, FlexType boType) {
        PreparedQueryStatement statement = new PreparedQueryStatement();
        try {
			String strColumnName = boType.getAttribute(SparcCostingConstants.COUNTRY_ORIGIN_KEY).getColumnName();
            statement.appendFromTable(LCSLifecycleManaged.class);
            statement.appendSelectColumn(new QueryColumn(LCSLifecycleManaged.class, SparcCostingConstants.OBJECT_IDENTIFIER));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, SparcCostingConstants.TYPE_IDENTIFIER), "?", Criteria.EQUALS),
                    new Long(FormatHelper.getNumericFromOid(FormatHelper.getNumericObjectIdFromObject(boType))));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, boType.getAttribute(SparcCostingConstants.HTS_CODE_KEY).getColumnDescriptorName()), strHTSCodeValue, Criteria.EQUALS));
            statement.appendAndIfNeeded();
            statement.appendOpenParen();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, boType.getAttribute(SparcCostingConstants.COUNTRY_ORIGIN_KEY).getColumnDescriptorName()), "?", Criteria.EQUALS),
                    new Long(Long.parseLong(originCountryId)));
            statement.appendOrIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, boType.getAttribute(SparcCostingConstants.COUNTRY_ORIGIN_KEY).getColumnDescriptorName()), "?", Criteria.EQUALS),
                    new Long(0));
            statement.appendOrIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, boType.getAttribute(SparcCostingConstants.COUNTRY_ORIGIN_KEY).getColumnDescriptorName()), "", Criteria.IS_NULL));
            statement.appendClosedParen();
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, boType.getAttribute(SparcCostingConstants.DESTINATION_COUNTRY_KEY).getColumnDescriptorName()), "?", Criteria.EQUALS),
                    new Long(Long.parseLong(desCountryId)));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, boType.getAttribute(SparcCostingConstants.START_DATE_KEY).getColumnDescriptorName()), objTimestamp, Criteria.LESS_THAN_EQUAL));
            statement.appendAndIfNeeded();
			statement.appendOpenParen();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, boType.getAttribute(SparcCostingConstants.END_DATE_KEY).getColumnDescriptorName()), objTimestamp, Criteria.GREATER_THAN_EQUAL));
            statement.appendOr();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, boType.getAttribute(SparcCostingConstants.END_DATE_KEY).getColumnDescriptorName()), "", Criteria.IS_NULL));
            statement.appendClosedParen();
            statement.appendCriteria(new Criteria() {
                private static final long serialVersionUID =1L;
                @Override
                public String getSqlExpression() {
                    return "ORDER BY " + strColumnName + " DESC NULLS LAST";
                }

            });
           
			System.out.println("Statement to String-----SQL QUERY-----------------"+statement.toString());
			
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return statement;
    }

    public static SearchResults runQuery(PreparedQueryStatement statement) {
       SearchResults objResults = null;
        try {
            objResults =LCSQuery.runDirectQuery(statement);
        }catch(Exception ex){
           ex.printStackTrace();
        }
        return objResults;
    }

    /**
     *
     * @param results
     * @return
     */
    public static List<LCSLifecycleManaged> getListOfBOObjects(SearchResults results) {
        List<LCSLifecycleManaged> listOfBOObjects = new ArrayList<>();

        if (results != null && results.getResultsFound() > 0) {
            Vector vec = results.getResults();
            Enumeration<FlexObject> enumeration = vec.elements();

            while (enumeration.hasMoreElements()) {
                FlexObject flexObject = enumeration.nextElement();
                LCSLifecycleManaged boObject = null;
                try {
                    boObject = (LCSLifecycleManaged) LCSQuery.findObjectById("OR:com.lcs.wc.foundation.LCSLifecycleManaged:" + flexObject.getString("LCSLifecycleManaged.IDA2A2"));
                } catch (WTException e) {
                    e.printStackTrace();
                }
                listOfBOObjects.add(boObject);
            }
        }
        return listOfBOObjects;
    }


    public static LCSLifecycleManaged findMatchingRow(List<LCSLifecycleManaged> listOfBOObjects) {
        System.out.println("new Finding Matching Row Start");
        LCSLifecycleManaged matchingRow = listOfBOObjects.stream()
                .min(Comparator.comparing(objManaged -> {
                    try {
                        return objManaged.getValue(SparcCostingConstants.END_DATE_KEY) != null ?
                                (Date) objManaged.getValue(SparcCostingConstants.END_DATE_KEY) : new Date(Long.MAX_VALUE);
                    } catch (WTException e) {
                        e.printStackTrace();
                        return new Date(Long.MAX_VALUE);
                    }
                }))
                .orElse(null);
        System.out.println("new Finding Matching Row End");
        System.out.println("matching row--------------"+matchingRow);
        return matchingRow;
    }

    public static List<String> convertCommaSepValuesintoList(String strHTSCodes) {
        List<String> htsKeys = Arrays.asList(strHTSCodes.split(SparcCostingConstants.COMMA_DELIMITER));
        return htsKeys;
    }

    public static List<String> getHTSCodeValues(LCSSourceToSeasonLink objSourceSeasLink, List<String> strHTSCodeKeys) {
        List<String> strHTSCodeValues = strHTSCodeKeys.stream()
                .map(strHTSKey -> {
                    try {
                        LCSLifecycleManaged objHTSManaged = objSourceSeasLink.getValue(strHTSKey) != null ?
                                (LCSLifecycleManaged) objSourceSeasLink.getValue(strHTSKey) : null;

                        return FormatHelper.equalsWithNull(objHTSManaged, null) ?
                                SparcCostingConstants.EMPTY_STRING : (String) objHTSManaged.getValue("scHTSCodeAssignment");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return SparcCostingConstants.EMPTY_STRING; // Handle the exception according to your requirement
                    }
                })
                .collect(Collectors.toList());


        return strHTSCodeValues;
    }

    public static String getHTSCodeValue(LCSSourceToSeasonLink objSourceSeasLink,String strHTSCodeKey){
        try {
            LCSLifecycleManaged objHTSManaged = objSourceSeasLink.getValue(strHTSCodeKey) != null ?
                    (LCSLifecycleManaged) objSourceSeasLink.getValue(strHTSCodeKey) : null;
            return FormatHelper.equalsWithNull(objHTSManaged, null) ?
                    SparcCostingConstants.EMPTY_STRING : (String) objHTSManaged.getValue("scHTSCodeAssignment");
        }catch(WTException ex){
            ex.printStackTrace();
        }
        return SparcCostingConstants.EMPTY_STRING;
    }

    public static String getCustomValue(FlexTyped flexTypedObject, String key) {
        try {
            String strVariableType = flexTypedObject.getFlexType().getAttribute(key).getAttVariableType();
            if ("object_ref".equalsIgnoreCase(strVariableType)) {
                if ("scDestinationCountryUSA".equalsIgnoreCase(key)) {
                    return getNumericVersionIdFromObject(SparcCostingConstants.countryQuery.findCountryByNameType(SparcCostingConstants.DEST_COUNTRY_VALUE, null));
                } else if (flexTypedObject.getValue(key) instanceof LCSCountry) {
                    return flexTypedObject.getValue(key) != null ? getNumericVersionIdFromObject((LCSCountry)flexTypedObject.getValue(key)) : null;
                }
            } else if ("choice".equalsIgnoreCase(strVariableType)) {
                return flexTypedObject.getValue(key) != null ? (String) flexTypedObject.getValue(key) : " ";
            } else {
                return flexTypedObject.getValue(key) != null ? (String) flexTypedObject.getValue(key) : " ";
            }
        } catch (WTException ex) {
            ex.printStackTrace();
        }
        return " ";
    }


    public static PreparedQueryStatement createQueryStatement(Timestamp objTimestamp, Map<String, String> objMapAttributes, FlexType boType, Boolean isFreight) {
        PreparedQueryStatement statement = new PreparedQueryStatement();

        try {
            String strBrandValue = objMapAttributes.get(SparcCostingConstants.BRAND_TEXT);

            Boolean isF21 = !"reebok".equalsIgnoreCase(strBrandValue) && !"Lucky".equalsIgnoreCase(strBrandValue);

            statement.appendFromTable(LCSLifecycleManaged.class);
            statement.appendSelectColumn(new QueryColumn(LCSLifecycleManaged.class, SparcCostingConstants.OBJECT_IDENTIFIER));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, SparcCostingConstants.TYPE_IDENTIFIER), "?", Criteria.EQUALS),
                    new Long(FormatHelper.getNumericFromOid(FormatHelper.getNumericObjectIdFromObject(boType))));


            // Build the query using modular methods
            buildCriteriaForAttributes(statement, objMapAttributes, boType);
            buildDateCriteria(statement, boType, objTimestamp);

        } catch (WTException ex) {
            ex.printStackTrace();
        }

        System.out.println("statement----------Freight Category---------------------" + statement.toString());
        return statement;
    }



    private static void buildCriteriaForAttributes(PreparedQueryStatement statement, Map<String, String> objMapAttributes, FlexType boType) {
        objMapAttributes.forEach((key, value) -> {
                    if (key.isEmpty()) {
                        return;
                    }
                    String[] columnKeys = key.split(SparcCostingConstants.TILDE_DELIMITER);
                    boolean isText = "text".equalsIgnoreCase(columnKeys[1]);
                    boolean isBlank = "blank".equalsIgnoreCase(columnKeys[2]);
                    String columnName = null;

                    try {
                        columnName = boType.getAttribute(columnKeys[0]).getColumnDescriptorName();
                        statement.appendAndIfNeeded();
                        buildCriteria(statement, columnName, value, isText,isBlank);
                    } catch (WTException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    private static void buildCriteria(PreparedQueryStatement statement, String columnName, String value, boolean isText, boolean isBlank) {
        try {
            /*if (isNullCheck.test(columnName, isText)) {
                buildIsNullCriteria(statement, columnName);
            }*/
            if (isText && isBlank) {
                buildTextBlankCriteria(statement, columnName, value);
            } else if (isText && !isBlank) {
                buildTextEqualsCriteria(statement, columnName, value);
            } else {
                buildNumericCriteria(statement, columnName, value, isBlank);
            }
        }catch(WTException ex){
            ex.printStackTrace();
        }
    }



    private static void buildTextBlankCriteria(PreparedQueryStatement statement, String columnName, String value) throws WTException{
        statement.appendOpenParen();
        statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, columnName), "", Criteria.IS_NULL));
        statement.appendOr();
        statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, columnName), "", Criteria.IS_NOT_NULL));
        statement.appendClosedParen();
    }


    private static void buildTextEqualsCriteria(PreparedQueryStatement statement, String columnName, String value) throws WTException{
        statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, columnName), value, Criteria.EQUALS));
    }


    private static void buildNumericCriteria(PreparedQueryStatement statement, String columnName, String value, boolean isBlank) throws WTException{
        if (isBlank) {
            statement.appendOpenParen();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, columnName), "", Criteria.IS_NOT_NULL));
            statement.appendOr();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, columnName), "", Criteria.IS_NULL));
            statement.appendClosedParen();
        } else {
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, columnName), "?", Criteria.EQUALS), Long.parseLong(value));
        }
    }



    private static void buildDateCriteria(PreparedQueryStatement statement, FlexType boType, Timestamp objTimestamp) {
        try {
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, boType.getAttribute(SparcCostingConstants.START_DATE_KEY).getColumnDescriptorName()), objTimestamp, Criteria.LESS_THAN_EQUAL));
            statement.appendAndIfNeeded();
            statement.appendOpenParen();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, boType.getAttribute(SparcCostingConstants.END_DATE_KEY).getColumnDescriptorName()), objTimestamp, Criteria.GREATER_THAN_EQUAL));
            statement.appendOr();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, boType.getAttribute(SparcCostingConstants.END_DATE_KEY).getColumnDescriptorName()), "", Criteria.IS_NULL));
            statement.appendClosedParen();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }



    private static List<LCSLifecycleManaged> filteredObjectsBasedOnPass(List<LCSLifecycleManaged> listOfFreightObjects,Map<String,String> mappedValues){
        // Define a map of conditions and their corresponding messages or actions
        Map<Predicate<LCSLifecycleManaged>, String> conditionMap = new LinkedHashMap<>();


        String strCountryOriginKey = mappedValues.get(SparcCostingConstants.COUNTRY_ORIGIN_KEY);
        String strDestinationKey = mappedValues.get(SparcCostingConstants.DESTINATION_KEY);
        String strCategory = mappedValues.get(SparcCostingConstants.CATEGORY_KEY);



        // Define the first condition and its corresponding message
        Predicate<LCSLifecycleManaged> firstCondition = obj -> {
            try {
                boolean check;
                if (FormatHelper.hasContent(strCategory) && strCategory.equalsIgnoreCase((String)obj.getValue(SparcCostingConstants.CATEGORY_KEY)) &&
                        Objects.nonNull(obj.getValue(SparcCostingConstants.COUNTRY_ORIGIN_KEY)) && strCountryOriginKey != null &&
                        FormatHelper.hasContent(strCountryOriginKey) && strCountryOriginKey.equalsIgnoreCase(getNumericVersionIdFromObject((LCSCountry) obj.getValue(SparcCostingConstants.COUNTRY_ORIGIN_KEY)))
                        && FormatHelper.hasContent((String)obj.getValue(SparcCostingConstants.DESTINATION_KEY)) &&
                        FormatHelper.hasContent(strDestinationKey) && strDestinationKey.equalsIgnoreCase((String)obj.getValue(SparcCostingConstants.DESTINATION_KEY))
                ) {
                    check = true;
                }
                else {
                    check = false;
                }
                System.out.println("first check-----first condition--------"+check);
                return check;

            } catch (WTException e) {
                e.printStackTrace();
                return false;
            }
        };


        Predicate<LCSLifecycleManaged> secondCondition = obj -> {
            try {
                boolean check;
                if (FormatHelper.hasContent(strCategory) && strCategory.equalsIgnoreCase((String)obj.getValue(SparcCostingConstants.CATEGORY_KEY)) &&
                        Objects.nonNull(obj.getValue(SparcCostingConstants.COUNTRY_ORIGIN_KEY)) && strCountryOriginKey != null &&
                        FormatHelper.hasContent(strCountryOriginKey) && strCountryOriginKey.equalsIgnoreCase(getNumericVersionIdFromObject((LCSCountry) obj.getValue(SparcCostingConstants.COUNTRY_ORIGIN_KEY)))
                        && !FormatHelper.hasContent((String)obj.getValue(SparcCostingConstants.DESTINATION_KEY))
                ) {
                    check = true;
                }
                else {
                    check = false;
                }
                System.out.println("first check----second condition---------"+check);
                return check;

            } catch (WTException e) {
                e.printStackTrace();
                return false;
            }
        };

        Predicate<LCSLifecycleManaged> thirdCondition = obj -> {
            try {
                boolean check;
                if (FormatHelper.hasContent(strCategory) && strCategory.equalsIgnoreCase((String)obj.getValue(SparcCostingConstants.CATEGORY_KEY)) &&
                        Objects.isNull(obj.getValue(SparcCostingConstants.COUNTRY_ORIGIN_KEY))
                        && FormatHelper.hasContent((String)obj.getValue(SparcCostingConstants.DESTINATION_KEY)) &&
                        FormatHelper.hasContent(strDestinationKey) && strDestinationKey.equalsIgnoreCase((String)obj.getValue(SparcCostingConstants.DESTINATION_KEY))
                ) {
                    check = true;
                }
                else {
                    check = false;
                }
                System.out.println("first check----Third condition---------"+check);
                return check;

            } catch (WTException e) {
                e.printStackTrace();
                return false;
            }
        };

        Predicate<LCSLifecycleManaged> fourthCondition = obj -> {
            try {
                boolean check;
                if (FormatHelper.hasContent(strCategory) && strCategory.equalsIgnoreCase((String)obj.getValue(SparcCostingConstants.CATEGORY_KEY)) &&
                        Objects.isNull(obj.getValue(SparcCostingConstants.COUNTRY_ORIGIN_KEY))
                        && !FormatHelper.hasContent((String)obj.getValue(SparcCostingConstants.DESTINATION_KEY))
                ) {
                    check = true;
                }
                else {
                    check = false;
                }
                System.out.println("first check-----fourth condition--------"+check);
                return check;

            } catch (WTException e) {
                e.printStackTrace();
                return false;
            }
        };


        Predicate<LCSLifecycleManaged> fifthCondition = obj -> {
            try {
                boolean check;
                if (!FormatHelper.hasContent((String)obj.getValue(SparcCostingConstants.CATEGORY_KEY)) &&
                        Objects.nonNull(obj.getValue(SparcCostingConstants.COUNTRY_ORIGIN_KEY)) && strCountryOriginKey != null &&
                        FormatHelper.hasContent(strCountryOriginKey) && strCountryOriginKey.equalsIgnoreCase(getNumericVersionIdFromObject((LCSCountry) obj.getValue(SparcCostingConstants.COUNTRY_ORIGIN_KEY)))
                        && FormatHelper.hasContent((String)obj.getValue(SparcCostingConstants.DESTINATION_KEY)) &&
                        FormatHelper.hasContent(strDestinationKey) && strDestinationKey.equalsIgnoreCase((String)obj.getValue(SparcCostingConstants.DESTINATION_KEY))
                )
                                  {
                    check = true;
                }
                else {
                    check = false;
                }
                System.out.println("first check-----fifth condition--------"+check);
                return check;

            } catch (WTException e) {
                e.printStackTrace();
                return false;
            }
        };

        Predicate<LCSLifecycleManaged> sixthCondition = obj -> {
            try {
                boolean check;
                if (!FormatHelper.hasContent((String)obj.getValue(SparcCostingConstants.CATEGORY_KEY)) &&
                        Objects.nonNull(obj.getValue(SparcCostingConstants.COUNTRY_ORIGIN_KEY)) && strCountryOriginKey != null &&
                        FormatHelper.hasContent(strCountryOriginKey) && strCountryOriginKey.equalsIgnoreCase(getNumericVersionIdFromObject((LCSCountry) obj.getValue(SparcCostingConstants.COUNTRY_ORIGIN_KEY)))
                        && !FormatHelper.hasContent((String)obj.getValue(SparcCostingConstants.DESTINATION_KEY))
                )
                {
                    check = true;
                }
                else {
                    check = false;
                }
                System.out.println("first check----- sixth condition--------"+check);
                return check;

            } catch (WTException e) {
                e.printStackTrace();
                return false;
            }
        };

        Predicate<LCSLifecycleManaged> seventhCondition = obj -> {
            try {
                boolean check;
                if (!FormatHelper.hasContent((String)obj.getValue(SparcCostingConstants.CATEGORY_KEY)) &&
                        Objects.isNull(obj.getValue(SparcCostingConstants.COUNTRY_ORIGIN_KEY)) &&
                        FormatHelper.hasContent((String)obj.getValue(SparcCostingConstants.DESTINATION_KEY)) && FormatHelper.hasContent(strDestinationKey) && strDestinationKey.equalsIgnoreCase((String)obj.getValue(SparcCostingConstants.DESTINATION_KEY))
                )
                {
                    check = true;
                }
                else {
                    check = false;
                }
                System.out.println("first check----- seventh condition--------"+check);
                return check;

            } catch (WTException e) {
                e.printStackTrace();
                return false;
            }
        };


        Predicate<LCSLifecycleManaged> eighthCondition = obj -> {
            try {
                boolean check;
                if (!FormatHelper.hasContent((String)obj.getValue(SparcCostingConstants.CATEGORY_KEY)) &&
                        Objects.isNull(obj.getValue(SparcCostingConstants.COUNTRY_ORIGIN_KEY)) &&
                        !FormatHelper.hasContent((String)obj.getValue(SparcCostingConstants.DESTINATION_KEY))
                )
                {
                    check = true;
                }
                else {
                    check = false;
                }
                System.out.println("eighthCondition--------------"+check);
                return check;

            } catch (WTException e) {
                e.printStackTrace();
                return false;
            }
        };
        conditionMap.put(firstCondition, "First Condition Result: ");
        conditionMap.put(secondCondition, "Second Condition Result: ");
        conditionMap.put(thirdCondition,"Third Condition Result: ");
        conditionMap.put(fourthCondition,"Fourth Condition Result: ");
        conditionMap.put(fifthCondition,"Fifth Condition Result: ");
        conditionMap.put(sixthCondition,"Sixth Condition Result: ");
        conditionMap.put(seventhCondition,"Seventh Condition Result: ");
        conditionMap.put(eighthCondition,"Eighth Condition Result: ");
        List<LCSLifecycleManaged> result = new ArrayList<>();
        for (Map.Entry<Predicate<LCSLifecycleManaged>, String> entry : conditionMap.entrySet()) {
            Predicate<LCSLifecycleManaged> condition = entry.getKey();
            String message = entry.getValue();
            List<LCSLifecycleManaged> conditionResult = filterObjects(listOfFreightObjects,condition);
            if (!conditionResult.isEmpty()) {
                System.out.println(message + conditionResult);
                result.addAll(conditionResult);
                break; // Exit the loop if any condition matches
            }
        }
        return  result;
    }



    private static List<LCSLifecycleManaged> filterObjects(List<LCSLifecycleManaged> listOfFreightObjects, Predicate<LCSLifecycleManaged> filterPredicate) {
        return listOfFreightObjects.stream()
                .filter(filterPredicate)
                .collect(Collectors.toList());
    }


    public static Map<String,String> getMappedAttributeKeyValues(final LCSProductCostSheet objCostSheet,final String strFreightAttributes){
        List<String> strFreightInputAttributes = SparcCostingUtil.convertCommaSepValuesintoList(strFreightAttributes);
        Map<String, String> mapAttributes = new HashMap<>();
        String[] strBoKeys;
        for (String strAtt : strFreightInputAttributes) {
            strBoKeys = strAtt.split(SparcCostingConstants.TILDE_DELIMITER);
            mapAttributes.put(strBoKeys[1] + SparcCostingConstants.TILDE_DELIMITER + strBoKeys[2]+ SparcCostingConstants.TILDE_DELIMITER +strBoKeys[3], SparcCostingUtil.getCustomValue(objCostSheet, strBoKeys[0]));
        }
        return mapAttributes;
    }

    public static Map<String,String> getMappedAttKeyValues(final LCSProductCostSheet objCostSheet,final String strFreightAttributes){
        List<String> strFreightInputAttributes = SparcCostingUtil.convertCommaSepValuesintoList(strFreightAttributes);
        Map<String, String> mapAttributes = new HashMap<>();
        String[] strBoKeys;
        for (String strAtt : strFreightInputAttributes) {
            strBoKeys = strAtt.split(SparcCostingConstants.TILDE_DELIMITER);
            mapAttributes.put(strBoKeys[1], SparcCostingUtil.getCustomValue(objCostSheet, strBoKeys[0]));
        }
        return mapAttributes;
    }


    public static Map<String,Object> calculateCostForHTSCode(List<String> strHTSCodeKeys, LCSSourceToSeasonLink sourceSeasonLink,
                                                             String originCountryId, String desCountryId, Date objCostDate,
                                                             LCSProductCostSheet objCostSheet, FlexType boType,Map<String,Object> htsAttributes) {
        String strHTSCodeValue = "";
        String[] strHTSWeightKey = null;
        
        double dutyPercent = 0.0;
        double tariffPer = 0.0;
        double totalFlatDuty = 0.0;
        double weightPer = 0.0;
        double dutyDollar =0.0;
        double totalDuty = 0.0;
        double totalTariffPer = 0.0;
        String attKeys= "";
        try {
			Timestamp objTimestamp = new Timestamp(objCostDate.getTime());
            for (String strHTSCode : strHTSCodeKeys) {
                LCSLifecycleManaged matchingHTSRow = null;
                strHTSWeightKey = strHTSCode.split(SparcCostingConstants.TILDE_DELIMITER);
                strHTSCodeValue = SparcCostingUtil.getHTSCodeValue(sourceSeasonLink, strHTSWeightKey[0]);

                if (!FormatHelper.hasContent(strHTSCodeValue)) {
                    System.out.println("HTS Code value is blank");
                    continue;
                }
                attKeys = htsAttributesMap.get(HTS_PREFIX+strHTSWeightKey[0]);

                PreparedQueryStatement statement = SparcCostingUtil.createQueryStatement(objTimestamp, strHTSCodeValue, originCountryId, desCountryId, boType);
                SearchResults results = SparcCostingUtil.runQuery(statement);
                List<LCSLifecycleManaged> listOfHTSObjects = SparcCostingUtil.getListOfBOObjects(results);
                if(listOfHTSObjects.size() == 1){
                    matchingHTSRow = listOfHTSObjects.get(0);
                }else{
                    matchingHTSRow = SparcCostingUtil.findMatchingRow(listOfHTSObjects);
                }
                if (matchingHTSRow != null) {
                    dutyPercent = getDoubleValue(matchingHTSRow, SparcCostingConstants.DUTY_PERCENT);
                    tariffPer = getDoubleValue(matchingHTSRow, SparcCostingConstants.COST_TARRIF_PERCENT_KEY);
                    dutyDollar = getDoubleValue(matchingHTSRow, SparcCostingConstants.FLAT_DUTY_DOLLAR_KEY);
                    weightPer = getDoubleValue(sourceSeasonLink, strHTSWeightKey[1]);
                    totalDuty += calculateFormula(dutyPercent, weightPer);
                    totalTariffPer += calculateFormula(tariffPer, weightPer);
                    totalFlatDuty += calculateFormula(dutyDollar, weightPer);
                    htsAttributes.put(attKeys.split(SparcCostingConstants.TILDE_DELIMITER)[0], dutyPercent);
                    htsAttributes.put(attKeys.split(SparcCostingConstants.TILDE_DELIMITER)[1], tariffPer);
                    htsAttributes.put(attKeys.split(SparcCostingConstants.TILDE_DELIMITER)[2], dutyDollar);
                    htsAttributes.put(strHTSWeightKey[1], weightPer);
                    htsAttributes.put("scHTSMatchingRow",(String)matchingHTSRow.getValue("name"));
                }
                else{
                    htsAttributes.put("scHTSMatchingRow","There is no matching row associated with this criteria");
                }

            }


        } catch(Exception ex){
            ex.printStackTrace();
			htsAttributes.put("scHTSMatchingRow",ex.getMessage());
        }

        return htsAttributes;
    }

    public static Map<String, Object> populateFreightRate(LCSProductCostSheet objCostSheet, Date ObjCostDate, FlexType boType, Map<String, Object> freightAttributes) {
        LCSLifecycleManaged matchingFreightRow = null;
        Map<String, String> mapAttributeKeyValues = new HashMap<>();
        Map<String, String> mapAttrKeyValues = new HashMap<>();
        try {
            Timestamp objTimeStamp = new Timestamp(ObjCostDate.getTime());
            mapAttributeKeyValues = getMappedAttributeKeyValues(objCostSheet, SparcCostingConstants.FREIGHT_INPUT_ATTRIBUTES);
            mapAttrKeyValues=getMappedAttKeyValues(objCostSheet,SparcCostingConstants.FREIGHT_INPUT_ATTRIBUTES);

            PreparedQueryStatement statement = SparcCostingUtil.createQueryStatement(objTimeStamp, mapAttributeKeyValues, boType, true);
			System.out.println("statement Freight Category-------Latest------------"+statement.toString());
            SearchResults results = SparcCostingUtil.runQuery(statement);
            List<LCSLifecycleManaged> listOfFreightObjects = SparcCostingUtil.getListOfBOObjects(results);
            listOfFreightObjects = SparcCostingUtil.filteredObjectsBasedOnPass(listOfFreightObjects,mapAttrKeyValues);

			System.out.println("listOfFreightObjects----------------"+listOfFreightObjects);
            if (!listOfFreightObjects.isEmpty()) {
                matchingFreightRow = SparcCostingUtil.findMatchingRow(listOfFreightObjects);
            }

			System.out.println("matchingFreightRow----Freight Rate------------"+matchingFreightRow);
            if (!FormatHelper.equalsWithNull(matchingFreightRow, null)) {
                freightAttributes.put(SparcCostingConstants.FREIGHT_DOLLARS1_KEY,  (Double)matchingFreightRow.getValue(SparcCostingConstants.FREIGHT_RATE));
                freightAttributes.put("scFreightMatchingRow",(String)matchingFreightRow.getValue("name"));
            }else{
                freightAttributes.put("scFreightMatchingRow","There is no matching row associated with this criteria");
            }



        } catch (Exception ex) {
            ex.printStackTrace();
			 freightAttributes.put("scFreightMatchingRow",ex.getMessage());
        }
        return freightAttributes;
    }

    public static Map<String,Object> populateInternalLoadRate(final LCSProductCostSheet objCostSheet, Date ObjCostDate, FlexType boType,final String strAttributes,
                                                              final Boolean isInternalLookUp,Map<String,Object> othInternalMapAttributes) {
        LCSLifecycleManaged matchingInternalLoadRow = null;
        Map<String,String> mapAttributeKeyValues = new HashMap<>();
        Timestamp objTimeStamp;
        SearchResults results = null;
        List<LCSLifecycleManaged> listOfInternalObjects = new ArrayList<>();
        try {
            objTimeStamp = new Timestamp(ObjCostDate.getTime());
            mapAttributeKeyValues = getMappedAttributeKeyValues(objCostSheet, strAttributes);
            PreparedQueryStatement statement = SparcCostingUtil.createQueryStatement(objTimeStamp, mapAttributeKeyValues, boType,false);
            results = SparcCostingUtil.runQuery(statement);
            listOfInternalObjects = SparcCostingUtil.getListOfBOObjects(results);


            if (listOfInternalObjects.size() == 1) {
                matchingInternalLoadRow = listOfInternalObjects.get(0);
            } else {
                matchingInternalLoadRow = SparcCostingUtil.findMatchingRow(listOfInternalObjects);
            }
            if (!FormatHelper.equalsWithNull(matchingInternalLoadRow, null) && isInternalLookUp) {
                othInternalMapAttributes.put("scInternalLoad1Percent", (Double) matchingInternalLoadRow.getValue(SparcCostingConstants.INTERNAL_BO_PERCENT_US));
                othInternalMapAttributes.put("scInternalMatchingRow", (String)matchingInternalLoadRow.getValue("name"));
            }
            if(FormatHelper.equalsWithNull(matchingInternalLoadRow,null) && isInternalLookUp){
                othInternalMapAttributes.put("scInternalMatchingRow", "There is no matching row associated for this criteria");
            }

            if (!FormatHelper.equalsWithNull(matchingInternalLoadRow, null) && !isInternalLookUp) {
                othInternalMapAttributes.put(SparcCostingConstants.OTHER_COST_PERCENT, (Double) matchingInternalLoadRow.getValue(SparcCostingConstants.OTHER_BO_PERCENT));
                othInternalMapAttributes.put("scOtherMatchingRow", (String)matchingInternalLoadRow.getValue("name"));
            }

            if(FormatHelper.equalsWithNull(matchingInternalLoadRow,null) && !isInternalLookUp){
                othInternalMapAttributes.put("scOtherMatchingRow", "There is no matching row associated for this criteria");
            }

        } catch (Exception e) {
            LOGGER.error("There is an error while setting the attributes for the costsheet");
            e.printStackTrace();
			othInternalMapAttributes.put("scInternalMatchingRow",e.getMessage());
			othInternalMapAttributes.put("scOtherMatchingRow",e.getMessage());
			
        }
        return othInternalMapAttributes;
    }

    public static double getDoubleValue(FlexTyped obj, String key) {
        Double value = 0.0;
        try {
            value = (Double) obj.getValue(key);
        } catch (WTException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static double calculateFormula(double percentage, double weight) {
        return (percentage * weight) / 100.0;
    }

    public static FlexType getFlexType(String typePath){
        FlexType boType = null;
        try {
             boType = FlexTypeCache.getFlexTypeFromPath(typePath);
        } catch (WTException e) {
            e.printStackTrace();
        }
        return boType;
    }
}
