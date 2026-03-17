package com.sparc.wc.integration.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lcs.wc.client.web.html.HTMLFormOptions;
import com.lcs.wc.client.web.html.select2.Select2ObjectReferenceListAttributeRenderer;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.flextype.ForiegnKeyDefinition;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.MOAHelper;
import com.sparc.tc.domain.AlwaysTrue;
import com.sparc.tc.domain.AttributeValue;
import com.sparc.tc.domain.PlaceHolder;
import com.sparc.tc.util.StringUtils;
import com.sparc.wc.exports.constants.SparcExportConstants;
import com.sparc.wc.integration.constants.SparcIntegrationConstants;
import com.sparc.wc.util.SparcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wt.fc._WTObject;
import wt.util.WTException;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.COLORWAY_NUMBER_LENGTH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.PADDING_CHAR;

/**
 * SparcIntegrationUtil class<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Task #10496: Added method "splitString" to support allocation of text values into multiple log entries. 
 * 
 * @author Acnovate
 */
public class SparcIntegrationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SparcIntegrationUtil.class);

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Select2ObjectReferenceListAttributeRenderer SELECT_2_OBJ_REF_LIST_ATT_RENDERER = new Select2ObjectReferenceListAttributeRenderer();

    private SparcIntegrationUtil() {

    }

    public static String convertToDisplayName(final String internalName, final List<FlexType> flexTypes) {
        if (internalName == null || internalName.isEmpty() || flexTypes == null) {
            return internalName;
        }
        for (FlexType flexType : flexTypes) {
            try {
                final FlexTypeAttribute attribute = flexType.getAttribute(internalName);
                if (attribute != null) {
                    return attribute.getAttDisplay();
                }
            } catch (WTException e) {
                // Ignore exceptions
            }
        }
        return internalName;
    }

    public static String convertToDisplayName(final String internalName, final FlexTyped flexTyped) {
        if (internalName == null || internalName.isEmpty() || flexTyped == null) {
            return null;
        }
        try {
            final FlexType          flexType  = flexTyped.getFlexType();
            final FlexTypeAttribute attribute = flexType.getAttribute(internalName);
            if (attribute != null) {
                return attribute.getAttDisplay();
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static Set<String> convertToDisplayNames(final Set<String> internalNames, final FlexTyped flexTyped) {
        if (flexTyped == null || internalNames == null) {
            return new HashSet<>();
        }
        return internalNames.stream()
                .map(attr -> {
                    try {
                        final FlexType          flexType  = flexTyped.getFlexType();
                        final FlexTypeAttribute attribute = flexType.getAttribute(attr);
                        if (attribute != null) {
                            return attribute.getAttDisplay();
                        }
                    } catch (Exception e) {
                    }
                    return null;
                })
                .filter(name -> name != null)
                .collect(Collectors.toSet());
    }

    public static String addPadding(final String number) {
        final int           paddingLength = COLORWAY_NUMBER_LENGTH - number.length();
        final StringBuilder sb            = new StringBuilder(COLORWAY_NUMBER_LENGTH);
        for (int iterator = 0; iterator < paddingLength; iterator++) {
            sb.append(PADDING_CHAR);
        }
        sb.append(number);
        return sb.toString();
    }

    public static Object getValueFrom(final FlexTyped flexTyped, final String internalName) {
        if (internalName == null || internalName.isEmpty()) {
            return null;
        }
        try {
            return flexTyped.getValue(internalName);
        } catch (Exception e) {
            //do nothing
        }
        return null;
    }

    public static String getChoiceDisplayValue(final FlexTyped source, final String internalName) {
        if (source == null || internalName == null || internalName.isEmpty()) {
            return null;
        }
        try {
            final FlexTypeAttribute attr = source.getFlexType().getAttribute(internalName);
            if (attr == null) {
                LOGGER.error("Cannot find an attribute with internal name:" + internalName + ", on flexType:" + source.getFlexType().getFullNameDisplay(true));
                return null;
            }
            final Object value = source.getValue(internalName);
            if (value == null) {
                return null;
            }
            return attr.getAttValueList().get((String) value, "displayName");
        } catch (Exception e) {
            LOGGER.error("Error while converting choice type, error:" + e.getMessage());
            return null;
        }
    }

    public static Object getValueFrom(final FlexTyped source, final String internalName, final boolean transformValue) {
        if (source == null || internalName == null || internalName.isEmpty()) {
            return null;
        }
        try {
            final Object value = source.getValue(internalName);
            if (!transformValue) {
                return value;
            }
            return transformValue(value, internalName, source);
        } catch (Exception e) {
            //do nothing
            return null;
        }
    }

    public static AttributeValue getAttributeValueFrom(final PlaceHolder placeHolder, final FlexTyped source) {
        if (source == null || placeHolder.getVar() == null || placeHolder.getVar().isEmpty()) {
            return null;
        }
        try {
            LOGGER.debug("Fetching value from:" + source.getFlexType().getFullNameDisplay(true) + ", placeholder:" + placeHolder);
            if (isEnumExport(placeHolder)) {
                return exportEnum(source, placeHolder.getVar());
            }
            if (isObjectRefListExport(placeHolder)) {
                return exportObjRefList(source, placeHolder.getVar());
            }
            final Object value = source.getValue(placeHolder.getVar());
            LOGGER.debug("Value found:" + value);
            final AttributeValue attributeValue = transformToAttributeValue(value, placeHolder.getVar(), source);
            if (attributeValue == null) {
                return null;
            }
            if (placeHolder.hasParams() && placeHolder.getParams().getParameters() != null) {
                if (attributeValue.getParams() == null) {
                    attributeValue.setParams(new HashMap<>());
                }
                attributeValue.getParams().putAll(placeHolder.getParams().getParameters());
                if (hasCustomFormat(placeHolder.getParams().getParameters())) {
                    attributeValue.setType(AttributeValue.Type.USE_CELL_TYPE);
                }
            }
            return attributeValue;
        } catch (Exception e) {
            LOGGER.error("Encountered an error fetching value of:" + placeHolder + ", error:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static boolean hasCustomFormat(final Map<String, String> params) {
        return StringUtils.stringToEnum(params.get(SparcExportConstants.CUSTOM_FORMAT), AlwaysTrue.class) != null;
    }

    public static List<String> exportEnumOfAttribute(final FlexTyped flexTyped, final String internalName) {
        final List<String> enums = new LinkedList<>();
        if (flexTyped == null || internalName == null || internalName.isEmpty()) {
            LOGGER.error("Expecting a valid flexTyped object, but found empty");
            return enums;
        }
        try {
            final FlexType flexType = flexTyped.getFlexType();
            if (flexType == null) {
                LOGGER.error("FlexType of given flex-object is not set, cannot extract enum values");
                return enums;
            }
            final FlexTypeAttribute attribute = flexType.getAttribute(internalName);
            if (attribute == null) {
                LOGGER.error("Trying to export enums, but the attribute was found null for flexType:" + flexType.getFullName(true));
                return enums;
            }
            final Collection<String> keys = attribute.getAttValueList().getKeys();
            if (keys == null || keys.isEmpty()) {
                return enums;
            }
            return keys.stream()
                    .map(key -> {
                        return attribute.getAttValueList().get(key, "displayName");
                    })
                    .filter(value -> value != null)
                    .collect(Collectors.toList());
        } catch (WTException e) {
            LOGGER.error("Encountered an error while fetching enum values for attribute:" + internalName + ", flex-type:" + flexTyped.toString());
        }
        return enums;
    }

    public static AttributeValue exportEnum(final FlexTyped flexTyped, final String internalName) {
        final AttributeValue attributeValue = AttributeValue.builder().type(AttributeValue.Type.STRING).build();
        if (flexTyped == null) {
            LOGGER.error("Expecting a valid flexTyped object, but found empty");
            return attributeValue;
        }
        try {
            LOGGER.debug("Exporting enums for:" + internalName);
            final FlexType flexType = flexTyped.getFlexType();
            if (flexType == null) {
                LOGGER.error("FlexType of given flex-object is not set, cannot extract enum values");
                return attributeValue;
            }
            final FlexTypeAttribute attribute = flexType.getAttribute(internalName);
            if (attribute == null) {
                LOGGER.error("Trying to export enums, but the attribute was found null for flexType:" + flexType.getFullName(true));
                return attributeValue;
            }
            final Collection<String> keys = attribute.getAttValueList().getSelectableValues(null, true);
            LOGGER.debug("Enum keys:" + keys);
            if (keys == null || keys.isEmpty()) {
                attributeValue.setData(new ArrayList<>());
            } else {
                attributeValue.setData(new LinkedList<>(keys));
            }
        } catch (WTException e) {
            LOGGER.error("Encountered an error while fetching enum values for attribute:" + internalName + ", flex-type:" + flexTyped.toString());
        }
        return attributeValue;
    }

    private static AttributeValue exportObjRefList(final FlexTyped flexTyped, final String internalName) {
        final AttributeValue attributeValue = AttributeValue.builder().type(AttributeValue.Type.STRING).build();
        if (flexTyped == null) {
            LOGGER.error("Expecting a valid flexTyped object for exporting the object reference list values, but found empty");
            return attributeValue;
        }
        try {
            LOGGER.debug("Exporting object reference list for:" + internalName);
            final FlexType flexType = flexTyped.getFlexType();
            if (flexType == null) {
                LOGGER.error("FlexType of given flex-object is not set, cannot extract object reference list values");
                return attributeValue;
            }
            final FlexTypeAttribute attribute = flexType.getAttribute(internalName);
            if (attribute == null) {
                LOGGER.error("Trying to export object reference list, but the attribute was found null for flexType:" + flexType.getFullName(true));
                return attributeValue;
            }
            final Map<String, String> objectRefListTable = SELECT_2_OBJ_REF_LIST_ATT_RENDERER.getObjectRefListTable(attribute, null, new HTMLFormOptions(HTMLFormOptions.FormType.UPDATE, null, attribute.getAttObjectLevel(), null, false, null, false, false));
            LOGGER.debug("Object Reference list map:" + objectRefListTable);
            final Collection<String> keys = (objectRefListTable == null ? new ArrayList<>() : objectRefListTable.values());
            LOGGER.debug("Object references: " + keys);
            if (keys.isEmpty()) {
                attributeValue.setData(keys);
            } else {
                final List<String> sortedKeys = keys.stream().sorted().collect(Collectors.toList());
                attributeValue.setData(sortedKeys);
            }
        } catch (WTException e) {
            LOGGER.error("Encountered an error while fetching object reference list values for attribute:" + internalName + ", flex-type:" + flexTyped.toString());
        }
        return attributeValue;
    }

    private static boolean isEnumExport(final PlaceHolder placeHolder) {
        if (placeHolder.getParams() == null || !StringUtils.hasContent(placeHolder.getParams().getParam(SparcExportConstants.FLEX_ENUM_EXPORT_PARAM))) {
            return false;
        }
        return StringUtils.stringToEnum(placeHolder.getParams().getParam(SparcExportConstants.FLEX_ENUM_EXPORT_PARAM).toUpperCase(), AlwaysTrue.class) != null;
    }

    private static boolean isObjectRefListExport(final PlaceHolder placeHolder) {
        if (placeHolder.getParams() == null || !StringUtils.hasContent(placeHolder.getParams().getParam(SparcExportConstants.FLEX_OBJ_REF_LIST_EXPORT_PARAM))) {
            return false;
        }
        return StringUtils.stringToEnum(placeHolder.getParams().getParam(SparcExportConstants.FLEX_OBJ_REF_LIST_EXPORT_PARAM).toUpperCase(), AlwaysTrue.class) != null;
    }

    public static Object transformValue(final FlexType flexType, final String internalName, final Object value) {
        try {

        } catch (Exception e) {
            //do nothing
        }
        return null;
    }

    public static Object getValueFrom(final List<FlexTyped> sources, final String internalName, final boolean transformValue) {
        if (sources == null || sources.isEmpty() || internalName == null || internalName.isEmpty()) {
            return null;
        }
        for (FlexTyped flexTyped : sources) {
            try {
                final Object value = flexTyped.getValue(internalName);
                if (!transformValue) {
                    if (value != null) {
                        return value;
                    }
                } else {
                    return transformValue(value, internalName, flexTyped);
                }

            } catch (Exception e) {
                //do nothing
            }
        }
        return null;
    }

    private static Object transformValue(final Object value, final String internalName, final FlexTyped flexTyped) throws WTException {
        if (value == null || flexTyped == null) {
            return null;
        }
        final FlexTypeAttribute attr        = flexTyped.getFlexType().getAttribute(internalName);
        final String            attrVarType = attr.getAttVariableType();
        if (attrVarType.equals("boolean")) {
            if ((Boolean) value) {
                return "Y";
            } else {
                return "N";
            }
        }
        if (attrVarType.equals("date")) {
            return ((Date) value).toInstant().toEpochMilli();
        }
        if (attrVarType.equals("composite")) {
            return convertToComposite((String) value, attr);
        }
        if (attrVarType.equals("moaList")) {
            return convertToList((String) value, attr);
        }
        if (attrVarType.equals("moaEntry")) {
            return tokenize((String) value);
        }
        if (attrVarType.equals("choice")) {
            return convertChoice((String) value, attr);
        }
        if (attrVarType.equals("object_ref")) {
            return convertObjectRef((FlexTyped) value);
        }
        if (attrVarType.equals("object_ref_list")) {
            return convertObjectRef((FlexTyped) value);
        }
        return value;
    }

    private static AttributeValue transformToAttributeValue(final Object value, final String internalName, final FlexTyped flexTyped) throws WTException {
        if (flexTyped == null) {
            return null;
        }
        final FlexTypeAttribute   attr           = flexTyped.getFlexType().getAttribute(internalName);
        final String              attrVarType    = attr.getAttVariableType();
        final AttributeValue      attributeValue = new AttributeValue();
        final Map<String, Object> params         = new HashMap<>();
        switch (attrVarType) {
            case "currency":
                attributeValue.setType(AttributeValue.Type.CURRENCY);
                params.put(AttributeValue.CURRENCY_TYPE, attr.getCurrencyCode());
                params.put(AttributeValue.DECIMALS, attr.getAttDecimalFigures());
                attributeValue.setParams(params);
                attributeValue.setData(value);
                break;
            case "float":
                attributeValue.setType(AttributeValue.Type.DECIMAL);
                params.put(AttributeValue.DECIMALS, attr.getAttDecimalFigures());
                attributeValue.setParams(params);
                attributeValue.setData(value);
                break;
            case "integer":
                attributeValue.setType(AttributeValue.Type.INTEGER);
                attributeValue.setData(value);
                break;
            case "boolean":
                attributeValue.setType(AttributeValue.Type.BOOLEAN);
                if ((Boolean) value) {
                    attributeValue.setData(true);
                } else {
                    attributeValue.setData(false);
                }
                break;
            case "date":
                attributeValue.setType(AttributeValue.Type.DATE);
                attributeValue.setData(value);
                break;
            case "composite":
                attributeValue.setType(AttributeValue.Type.STRING);
                attributeValue.setData(convertToComposite((String) value, attr));
                break;
            case "moaList":
                attributeValue.setType(AttributeValue.Type.STRING);
                attributeValue.setData(convertToDisplayList((String) value, attr));
                break;
            case "moaEntry":
                attributeValue.setType(AttributeValue.Type.STRING);
                attributeValue.setData(tokenize((String) value));
                break;
            case "choice":
                attributeValue.setType(AttributeValue.Type.STRING);
                attributeValue.setData(convertChoiceToDisplay((String) value, attr));
                break;
            case "object_ref":
            case "object_ref_list":
                attributeValue.setType(AttributeValue.Type.STRING);
                attributeValue.setData(convertObjectRef((FlexTyped) value));
                break;
            default:
                attributeValue.setType(AttributeValue.Type.STRING);
                attributeValue.setData(value);
                break;
        }
        return attributeValue;
    }

    private static Object convertObjectRef(final FlexTyped objectRef) {
        try {
            return objectRef.getValue("name");
        } catch (Exception e) {
            LOGGER.error("Encountered an error while converting object reference to String, error:" + e.getMessage());
            return objectRef;
        }
    }

    private static String convertChoice(final String key, final FlexTypeAttribute attr) throws LCSException {
        if (key == null || attr == null) {
            return null;
        }
        final String secKey = attr.getAttValueList().get(key, SparcConstants.CUSTOM_SECONDARY_KEY);
        if (secKey != null) {
            return secKey;
        }
        return attr.getAttValueList().get(key, "displayName");
    }

    private static String convertChoiceToDisplay(final String key, final FlexTypeAttribute attr) throws LCSException {
        if (key == null || attr == null) {
            return null;
        }
        return attr.getAttValueList().get(key, "displayName");
    }

    private static List<String> tokenize(final String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        final String[] tokenStrs = value.split("\\|~\\*~\\|", -1);
        if (tokenStrs == null || tokenStrs.length == 0) {
            return null;
        }
        return Arrays.stream(tokenStrs).map(token -> token.trim()).collect(Collectors.toList());
    }

    private static List<String> convertToList(final String value, final FlexTypeAttribute attr) {
        if (value == null || value.isEmpty() || attr == null) {
            return null;
        }
        final String[]     listStrings = value.split("\\|~\\*~\\|", -1);
        final List<String> valueList   = new LinkedList<>();
        Arrays.stream(listStrings)
                .map(str -> str.trim())
                .forEach(str -> {
                    final String secValue = attr.getAttValueList().get(str, SparcConstants.CUSTOM_SECONDARY_KEY);
                    if (secValue != null) {
                        valueList.add(secValue);
                    } else {
                        final String displayName = attr.getAttValueList().get(str, "displayName");
                        valueList.add(displayName);
                    }
                });
        return valueList;
    }

    private static List<String> convertToDisplayList(final String value, final FlexTypeAttribute attr) {
        if (value == null || value.isEmpty() || attr == null) {
            return null;
        }
        final String[]     listStrings = value.split("\\|~\\*~\\|", -1);
        final List<String> valueList   = new LinkedList<>();
        Arrays.stream(listStrings)
                .map(str -> str.trim())
                .forEach(str -> {
                    final String displayName = attr.getAttValueList().get(str, "displayName");
                    valueList.add(displayName);
                });
        return valueList;
    }

    public static Object invert(final FlexTypeAttribute flexTypeAttribute, final Object value, final Map<String, String> params) {
        if (value == null || flexTypeAttribute == null) {
            return null;
        }
        final String attrVarType = flexTypeAttribute.getAttVariableType();
        if (attrVarType.equals("boolean")) {
            return invertBoolean(value);
        }
        if (attrVarType.equals("date")) {
            return invertTimestamp(value);
        }
        if (attrVarType.equals("composite")) {
            return invertComposition(flexTypeAttribute, value);
        }
        if (attrVarType.equals("moaList")) {
            return invertMultiChoice(flexTypeAttribute, value);
        }
        if (attrVarType.equals("moaEntry")) {
            return invertMultiEntry(value);
        }
        if (attrVarType.equals("choice")) {
            return invertChoice(flexTypeAttribute, value);
        }
        if (attrVarType.equals("object_ref_list")) {
            return invertObjectRefList(flexTypeAttribute, value, params);
        }
        return value;
    }

    private static Object invertObjectRefList(final FlexTypeAttribute flexTypeAttribute, final Object value, final Map<String, String> params) {
        final ForiegnKeyDefinition refDefinition = flexTypeAttribute.getRefDefinition();
        if (refDefinition == null) {
            LOGGER.error("Expecting a valid refDefinition for flexType:" + flexTypeAttribute);
            return null;
        }
        final PreparedQueryStatement pqs = createObjectRefQuery(refDefinition, value, params);
        if (pqs == null) {
            LOGGER.error("Cannot create query for object reference list type attribute:" + flexTypeAttribute);
            return null;
        }

        final SearchResults searchResults = SparcQueryUtil.runQuery(pqs);
        if (searchResults == null) {
            throw new RuntimeException("No results were found for attribute:" + flexTypeAttribute.getAttKey() + ", with value:" + value);
        }
        final Collection<FlexObject> results = searchResults.getResults();
        if (results == null || results.isEmpty()) {
            throw new RuntimeException("No results were found for attribute:" + flexTypeAttribute.getAttKey() + ", with value:" + value);
        }
        final List<Object> columnValues = results.stream().map(fo -> {
            return fo.get(refDefinition.getRefTable() + "." + refDefinition.getRefColumn());
        }).filter(columnValue -> columnValue != null).collect(Collectors.toList());
        if (columnValues.size() != 1) {
            throw new RuntimeException("Found too many/no results matching the value for the given attribute:" + flexTypeAttribute.getAttKey() + ", for value:" + value.toString());
        }
        final String oid = refDefinition.getIdPrefix() + "" + columnValues.get(0).toString();
        return SparcQueryUtil.findObjectById(oid);
    }

    private static Object invertBoolean(final Object value) {
        if (value.toString().equals("Y")) {
            return true;
        }
        return false;
    }

    private static Object invertChoice(final FlexTypeAttribute flexTypeAttribute, final Object value) {
        final AttributeValueList attValueList = flexTypeAttribute.getAttValueList();
        if (attValueList == null || attValueList.getKeys() == null || attValueList.getKeys().isEmpty()) {
            LOGGER.warn("No enums were found for attribute:" + flexTypeAttribute);
            return null;
        }
        final Collection<String> keys = attValueList.getKeys();
        return keys.stream().filter(key -> {
            final String secondaryKeyValue = attValueList.get(key, SparcConstants.CUSTOM_SECONDARY_KEY);
            if (secondaryKeyValue != null && secondaryKeyValue.equals(value.toString())) {
                return true;
            }
            final String displayNameValue = attValueList.get(key, "displayName");
            if (displayNameValue != null && displayNameValue.equals(value.toString())) {
                return true;
            }
            return false;
        }).findFirst().orElse(null);
    }

    private static Object invertMultiChoice(final FlexTypeAttribute flexTypeAttribute, final Object value) {
        final List<String> tokens         = convertToList(value);
        final List<Object> invertedTokens = tokens.stream().map(token -> invertChoice(flexTypeAttribute, token)).collect(Collectors.toList());
        return MOAHelper.toMOAString(invertedTokens);
    }

    private static Object invertMultiEntry(final Object value) {
        final List<String> tokens = convertToList(value);
        return MOAHelper.toMOAString(tokens);
    }

    private static Object invertTimestamp(final Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof String) {
                return Timestamp.from(Instant.ofEpochMilli(Long.parseLong(value.toString())));
            }
            if (value instanceof Long) {
                return Timestamp.from(Instant.ofEpochMilli((Long) value));
            }
        } catch (Exception e) {
            LOGGER.error("Encountered an error while converting value to timestamp, error:" + e.toString());
        }
        return null;
    }

    private static Object invertComposition(final FlexTypeAttribute flexTypeAttribute, final Object value) {
        final List<String> tokens = convertToList(value);
        final List<Object> invertedTokens = tokens.stream()
                .map(token -> {
                    final String[] compositionAttrs = token.split(" ", -1);
                    if (compositionAttrs == null || compositionAttrs.length < 2) {
                        return null;
                    }
                    final Object composite = invertChoice(flexTypeAttribute, compositionAttrs[1].trim());
                    if (composite == null) {
                        LOGGER.warn("The composition of " + compositionAttrs[1] + " is not defined in the enums");
                        return null;
                    }
                    return compositionAttrs[0].trim() + " " + composite;
                })
                .filter(invertedToken -> invertedToken != null)
                .collect(Collectors.toList());
        return MOAHelper.toMOAString(invertedTokens);
    }

    private static List<String> convertToList(final Object value) {
        if (value == null) {
            return new ArrayList<>();
        }
        if (value instanceof String && !((String) value).isEmpty()) {
            return Arrays.stream(((String) value).split("\\|", -1)).map(str -> str.trim()).collect(Collectors.toList());
        }
        if (value instanceof LinkedList) {
            return (List<String>) value;
        }
        return new ArrayList<>();
    }

    private static PreparedQueryStatement createObjectRefQuery(final ForiegnKeyDefinition refDefinition, final Object value, final Map<String, String> params) {
        final PreparedQueryStatement pqs = new PreparedQueryStatement();
        pqs.appendSelectColumn(new QueryColumn(refDefinition.getRefTable(), refDefinition.getRefColumn()));
        pqs.appendFromTable(refDefinition.getRefTable());
        final boolean appendedCriteria = appendCriteria(pqs, refDefinition.getRefTable(), value, params);
        if (!appendedCriteria) {
            return null;
        }
        return pqs;
    }

    private static boolean appendCriteria(final PreparedQueryStatement pqs, final String refTable, final Object value, final Map<String, String> params) {
        final String flexTypeStr = params.get("FLEX_TYPE");
        if (flexTypeStr == null || flexTypeStr.isEmpty()) {
            return false;
        }
        try {
            final FlexType flexType         = FlexTypeCache.getFlexTypeFromPath(flexTypeStr);
            final String   attrInternalName = params.get("ATTRIBUTE");
            if (attrInternalName == null || attrInternalName.isEmpty()) {
                return false;
            }
            final FlexTypeAttribute flexTypeAttribute = flexType.getAttribute(attrInternalName);
            if (flexTypeAttribute == null) {
                return false;
            }
            final Object invertedValue = invert(flexTypeAttribute, value, null);
            if (invertedValue == null) {
                LOGGER.error("Found inverted value as null, cannot proceed further for creating query of type object reference list");
                return false;
            }
            pqs.appendAndIfNeeded();
            pqs.appendCriteria(new Criteria(refTable, flexTypeAttribute.getColumnName(), invertedValue.toString(), Criteria.EQUALS));
            return true;
        } catch (WTException e) {
            return false;
        }
    }

    private static String convertToComposite(final String value, final FlexTypeAttribute attr) {
        if (value == null || value.isEmpty() || attr == null) {
            return null;
        }
        final String[] percentages = value.split("\\|~\\*~\\|", -1);
        if (percentages == null || percentages.length == 0) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        Arrays.stream(percentages).forEach(percentage -> {
            final String[] composition = percentage.trim().split(" ", -1);
            if (composition != null && composition.length == 2) {
                final String displayComp = convertInternalCompToDisplayComp(composition[0], composition[1], attr);
                sb.append(displayComp + SparcIntegrationConstants.COMPOSITION_CONTENT_DELIMITER);
            }
        });
        sb.delete(sb.length() - SparcIntegrationConstants.COMPOSITION_CONTENT_DELIMITER.length(), sb.length());
        return sb.toString();
    }

    private static String convertInternalCompToDisplayComp(final String percent, final String composite, final FlexTypeAttribute attr) {
        final String displayName = attr.getAttValueList().get(composite.trim(), "displayName");
        return roundOffPercentage(percent.trim()) + "% " + displayName;
    }

    private static int roundOffPercentage(final String percentStr) {
        if (percentStr.endsWith("%")) {
            final String trimmedPercentStr = percentStr.substring(0, percentStr.length() - 1);
            return (int) Double.parseDouble(trimmedPercentStr);
        }
        return (int) Double.parseDouble(percentStr);
    }

    public static <T extends Enum<T>> T getEnumFromString(final Class<T> enumType, final String constant, final T defaultValue) {
        if (enumType == null || constant == null || constant.isEmpty()) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumType, constant.trim());
        } catch (Exception e) {
            //do nothing
        }
        return defaultValue;
    }

    public static String deserialize(final Object value) {
        if (value == null) {
            return "";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean divergenceCheck(final Object deltaChange, final Object existing) {
        if (deltaChange == null && existing == null) {
            return false;
        }
        if (deltaChange == null || existing == null) {
            return true;
        }
        final boolean equals = !deltaChange.toString().equals(existing.toString());
        return equals;
    }

    public static boolean changeCheck(final Object deltaChange, final Object existing) {
        LOGGER.debug("Change check--------deltaChange-"+deltaChange+"existing------------"+existing);
        if (deltaChange == null && existing == null) {
            return false;
        }
        if (deltaChange == null && existing != null) {
            return false;
        }

        if (deltaChange != null && existing == null) {
            return true;
        }
        final boolean equals = !deltaChange.toString().equals(existing.toString());
        return equals;
    }
    public static Set<String> getAllAttributes(final FlexType flexType, final String level) {
        if (flexType == null) {
            return new HashSet<>();
        }

        try {
            return flexType.getAllAttributes(null, level).stream().map(flexTypeAttribute -> flexTypeAttribute.getAttKey()).collect(Collectors.toSet());
        } catch (Exception e) {
            return new HashSet<>();
        }

    }

    public static Set<String> getAllAttributes(final FlexType flexType, final String scope, final String level) {
        if (flexType == null) {
            return new HashSet<>();
        }

        try {
            return flexType.getAllAttributes(scope, level).stream().map(flexTypeAttribute -> flexTypeAttribute.getAttKey()).collect(Collectors.toSet());
        } catch (Exception e) {
            return new HashSet<>();
        }

    }

    public static void addTemporalFields(final _WTObject wtObject, final String temporalPattern, final Map<String, Object> data) {
        if (wtObject == null || temporalPattern == null || temporalPattern.isEmpty() || data == null) {
            return;
        }
        final SimpleDateFormat sdf             = new SimpleDateFormat(temporalPattern);
        final Timestamp        createTimestamp = wtObject.getCreateTimestamp();
        final Timestamp        modifyTimestamp = wtObject.getModifyTimestamp();
        try {
            data.put("dateCreated", sdf.format(createTimestamp));
            data.put("dateModified", sdf.format(modifyTimestamp));
        } catch (Exception e) {
            LOGGER.error("Encountered an error while adding temporal fields, there might be problems with supplied pattern, error:" + e.getMessage());
        }
    }

    public static boolean hasAttribute(final PlaceHolder placeHolder, final FlexType flexType, final String className) {
        if (placeHolder == null || flexType == null || !StringUtils.hasContent(placeHolder.getFlexType()) || !StringUtils.hasContent(placeHolder.getVar()) || !StringUtils.hasContent(className)) {
            return false;
        }
        LOGGER.debug("Checking className:" + className + ", placeholder:" + placeHolder);
        if (!className.equals(placeHolder.getFlexType())) {
            return false;
        }
        try {
            final FlexTypeAttribute attr = flexType.getAttribute(placeHolder.getVar());
            if (attr == null) {
                return false;
            }
            LOGGER.debug("Attribute level:" + attr.getAttObjectLevel() + ", scope:" + attr.getAttScope());
            if (hasLevel(placeHolder) && !attr.getAttObjectLevel().equals(getLevel(placeHolder))) {
                return false;
            }
            if (hasScope(placeHolder) && !attr.getAttScope().equals(getScope(placeHolder))) {
                return false;
            }
            return true;
        } catch (WTException e) {
            LOGGER.debug("FlexType:" + flexType.getTypeClass() + ", doesn't contain:" + placeHolder.getVar());
        }
        return false;
    }

    private static boolean hasScope(final PlaceHolder placeHolder) {
        if (placeHolder.getParams() == null || !placeHolder.getParams().hasParam(SparcExportConstants.FLEX_SCOPE)) {
            return false;
        }
        return true;
    }

    private static boolean hasLevel(final PlaceHolder placeHolder) {
        if (placeHolder.getParams() == null || !placeHolder.getParams().hasParam(SparcExportConstants.FLEX_LEVEL)) {
            return false;
        }
        return true;
    }

    private static String getLevel(final PlaceHolder placeHolder) {
        return placeHolder.getParams().getParam(SparcExportConstants.FLEX_LEVEL);
    }

    private static String getScope(final PlaceHolder placeHolder) {
        return placeHolder.getParams().getParam(SparcExportConstants.FLEX_SCOPE);
    }

    public static AttributeValue getValue(final PlaceHolder placeHolder, final FlexTyped flexTyped) {
        if (placeHolder == null || placeHolder.getVar() == null || placeHolder.getVar().isEmpty() || flexTyped == null) {
            return null;
        }

        return null;
    }

    /**
     * Trims the given string to the given size provided it is not null or empty, and the given size to trim to is lower than the string's actual size.<br>
     *
     * @param strToTrim The string to trim.
     * @param size      The size to trim the given string to. Must be a positive value greater than zero.
     * @return The trimmed string.
     */
    public static String trimToSize(String strToTrim, int size) {

        if (strToTrim == null || strToTrim.isEmpty() || size < 1) {
            return strToTrim;
        }

        return strToTrim.substring(0, Math.min(strToTrim.length(), size));
    }
    
    /**
     * Splits the given string into chunks based on the given length threshold.
     * @param str The string to split into chunks.
     * @param len The length threshold to be reached before the satring gets split.
     * @return A list of string chunks based on the original string provided. 
     */
    public static List<String> splitString(String str, int len) {
        List<String> results = new ArrayList<>();
        
        if (str == null || len <= 0) {
        	return results;
        }
        
        int length = str.length();

        for (int i = 0; i < length; i += len) {
            results.add(str.substring(i, Math.min(length, i + len)));
        }

        return results;
    }

    public static Set<String> getChangedAttributes(FlexTyped current, FlexTyped previous, Set<String> attributes) throws WTException {

        Set<String> changedAttrs = new HashSet<>();

        if (current == null || previous == null || attributes == null || attributes.isEmpty()) {
            return changedAttrs;
        }

        for (String  attr : attributes) {
            Object currAttrValue = getValueFrom(current,attr);
            Object attrPrevValue = getValueFrom(previous,attr);
			
			if (currAttrValue instanceof Boolean && currAttrValue == null) {
                currAttrValue = false;
			}
			if (attrPrevValue instanceof Boolean && attrPrevValue == null) {
				attrPrevValue = false;
			}
            if (currAttrValue != null && currAttrValue.toString().isEmpty()) {
                currAttrValue = null;
            }
            if (attrPrevValue != null && attrPrevValue.toString().isEmpty()) {
                attrPrevValue = null;
            }
			
            if (SparcIntegrationUtil.divergenceCheck(attrPrevValue, currAttrValue)) {
                changedAttrs.add(attr);
            }
        }//end for loop.
        return changedAttrs;
    }
    public static boolean isSame(final Object deltaChange, final Object existing) {
        if (deltaChange == null && existing == null) {
            return true;
        }
        if (deltaChange == null || existing == null) {
            return false;
        }
        return deltaChange.equals(existing);
    }
}
