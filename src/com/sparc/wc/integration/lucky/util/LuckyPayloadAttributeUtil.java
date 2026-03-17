package com.sparc.wc.integration.lucky.util;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COMMON_LOGGER_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_OBJECT_PATH_SEPARATOR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_ALT_SECONDARY_KEY_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_ENUM_SECONDARY_KEY_ATTR;

import java.sql.Timestamp;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import org.apache.logging.log4j.Logger;

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
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.MOAHelper;
import com.sparc.wc.integration.lucky.domain.LuckyPayloadAttribute;
import com.sparc.wc.integration.aero.repository.FlexAttrVariableTypes;
import com.sparc.wc.integration.util.SparcCompositeDelimiters;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.sparc.wc.integration.util.SparcQueryUtil;
import com.sparc.wc.util.SparcConstants;

import wt.log4j.LogR;
import wt.util.WTException;


public class LuckyPayloadAttributeUtil {

    private static final Logger LOGGER = LogR.getLogger("LuckyPayloadAttributeUtil");

    private static Map<String,String> incoTermsMap;
    private static final String incoTermsPrefix = "com.sparc.wc.lucky.incoterms.";

    private static final String FULL_CIRCLE_VENDOR_MESSAGE = "Vendor in sourcing configuration blank or missing Full Circle Vendor Number for incoterms on the cost sheet";	

    private static final String CUSTOM_ERROR_MESSAGE = "Factory in sourcing configuration blank or missing Lucky Factory ID";

    static{
        incoTermsMap = LCSProperties.getPropertyEntriesStartWith("com.sparc.wc.lucky.incoterms");
    }

    private LuckyPayloadAttributeUtil() {

    }


    public static Object loadAttributeValue(FlexTyped flexTypedObj,
                                            LuckyPayloadAttribute attribute,
                                            Map<String, Object> payload) throws WTException {

        if (flexTypedObj == null || attribute == null || attribute.getJsonAttributeAlias() == null || payload == null) {
            return null;
        }

        return payload.put(attribute.getJsonAttributeAlias(), getAttributeValue(flexTypedObj, attribute));

    }

    /**
     * Returns the value extracted from the given Flex Object for the given attribute.
     * @param flexObj The Flex object Map containing the attribute value to extract.
     * @param attribute The attribute info.
     * @return The value extracted from the Flex object or <code>null</code> if no value was found.
     */
    public static Object getAttributeValue(FlexObject flexObj, LuckyPayloadAttribute attribute) {

        Object attrValue = null;

        if (flexObj == null || attribute == null || attribute.getFlexAttributeName() == null || attribute.isPlaceholder()) {
            return attrValue;
        }

        return flexObj.getData(attribute.getFlexAttributeName());
    }


    public static Object getAttributeValue(FlexTyped flexTypedObj, LuckyPayloadAttribute attribute) throws WTException {
        return getAttributeValue(flexTypedObj, attribute, false);
    }


    public static Object getAttributeValue(FlexTyped flexTypedObj, LuckyPayloadAttribute attribute, boolean objPathRootOnly) throws WTException {

        Object attrValue = null;

        if (flexTypedObj == null || attribute == null || attribute.getFlexAttributeName() == null || attribute.isPlaceholder()) {
            return attrValue;
        }

        if (attribute.getObjectPath() != null && !attribute.getObjectPath().isBlank()) {

            if (!objPathRootOnly) {

                Object flexChainedObject = traverseFlexTypeChain(flexTypedObj, attribute.getObjectPath(), null);

                if (flexChainedObject instanceof FlexTyped) {
                    attrValue = getFlexTypeValue((FlexTyped)flexChainedObject, attribute);
                } else if (flexChainedObject instanceof FlexObject) {
                    attrValue = getAttributeValue((FlexObject)flexChainedObject, attribute);
                }

            } else {
                attrValue = getAttrObjectPathRoot(flexTypedObj, attribute);
            }

        } else {
            attrValue = getFlexTypeValue(flexTypedObj, attribute);
        }

        return formatEmptyValue(attrValue);
    }

    /**
     * Checks the given value and if found to be empty, a null is returned.
     * @param value The value to check for empty.
     * @return The value as-is if not empty, or <code>null</code> if the value is found to be empty.
     */
    private static Object formatEmptyValue(Object value) {

        if (value != null && value.toString().isBlank()) {
            value = null;
        }

        return value;
    }

    /**
     * Returns the value extracted from the given FlexTyped object for the given attribute.
     * @param flexTypedObj The FlexTyped object containing the attribute value to extract.
     * @param attribute The attribute info.
     * @return The value extracted or <code>null</code> if no value was found.
     * @throws WTException If an error occurs while extracting values from the FlexTyped object.
     */
    private static Object getFlexTypeValue(FlexTyped flexTypedObj, LuckyPayloadAttribute attribute) throws WTException {

        Object attrValue = null;

        if (attribute.isUseEnumSecondaryKeyAero()) {
            attrValue = lookupEnumSecondaryKeyAeroValue(flexTypedObj, attribute.getFlexAttributeName(), attribute.getCompositeValueDelimiter());
        }

        if (attribute.isUseEnumSecondaryKey() && attrValue == null) {
            attrValue = lookupEnumSecondaryKeyValue(flexTypedObj, attribute.getFlexAttributeName(), attribute.getCompositeValueDelimiter());
        }

        if (attribute.isUseEnumDisplayName() && attrValue == null) {
            attrValue = lookupEnumDisplayValue(flexTypedObj, attribute.getFlexAttributeName(), attribute.getCompositeValueDelimiter());
        }

        if (attribute.getEnumAttrKey() != null && !attribute.getEnumAttrKey().isBlank() && attrValue == null) {

            String[] enumAttrTokenArray = attribute.getEnumAttrKey().split(AERO_OBJECT_PATH_SEPARATOR);

            for (String enumAttrKey : enumAttrTokenArray) {
                attrValue = lookupEnumAttrValue(flexTypedObj, attribute.getFlexAttributeName(), enumAttrKey, attribute.getCompositeValueDelimiter());

                if (attrValue != null) {
                    break;
                }

            }//end for loop.

        }

        if(!attribute.isUseEnumSecondaryKeyAero() &&
                !attribute.isUseEnumSecondaryKey() &&
                !attribute.isUseEnumDisplayName() &&
                attribute.getEnumAttrKey() == null) {
            attrValue = flexTypedObj.getValue(attribute.getFlexAttributeName());
        }

        if (attrValue != null && attribute.getFormat() != null && !attribute.getFormat().isBlank()) {

            try {
                attrValue = formatValue(attrValue, attribute.getFormat(), attribute.getFormatClass());
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Unable to apply format to Aero payload attribute: " + attribute + ".", e);
            }

        }

        return attrValue;
    }

    /**
     * Formats the value using the given format and (optional) class.<br>
     * - To perform the format, the String.format() method is used.<br>
     * - Because certain formats requires the value to be of a certain class in order to be formatted, i.e. primitives,
     * an optional format class can be supplied to convert the value.
     * @param value The value to format.
     * @param format The format info as per supported by java.util.Formatter
     * @param formatClass Optional. Converts the value to the given class in order for the format to be applied. Supported classes are: Long, Integer, FLoat and Double.
     * @return The formatted value or the original value if a format couldn't be applied to the value.
     * @trows IllegalArgumentException If an error occurs while attempting to format the given value.
     */
    private static Object formatValue(Object value, String format, String formatClass) {

        if (value == null || format == null) {
            return value;
        }

        Object preFormattedValue = value;

        if (formatClass != null && !formatClass.isBlank()) {
            if (Long.class.getName().indexOf(formatClass) > -1) {
                preFormattedValue = Long.parseLong(value.toString());
            } else if (Integer.class.getName().indexOf(formatClass) > -1) {
                preFormattedValue = Integer.parseInt(value.toString());
            } else if (Double.class.getName().indexOf(formatClass) > -1) {
                preFormattedValue = Double.parseDouble(value.toString());
            } else if (Float.class.getName().indexOf(formatClass) > -1) {
                preFormattedValue = Float.parseFloat(value.toString());
            } else {
                preFormattedValue = value.toString();
            }
        }

        return String.format(format, preFormattedValue);
    }

    /**
     * Traverses the chain of nested FlexTyped objects defined within the attribute's "object path" and returns the last object of said chain.<br>
     * Note: The traverse of the chain will occur so long as the intermediate elements are FlexTyped objects, whereas the last element in the chain
     * may be either a FlexTyped object or a FlexObject (Map).
     * Therefore, if any intermediate element of the chain is not a FlexTyped object (this would likely be due to a misconfiguration),
     * then the traversal will stop at that point (no exception) and return that object.
     * @param rootFlexTypedObj The FlexTyped object considered as starting point (root) of the object chain.
     * @param objectPath The object path as per defined in the attribute definition.
     * @return The last object found in the object path chain.
     * @throws WTException If an error occurs while extracting values from the FlexTyped object given or any subsequent FlexTyped objects in the object path.
     */
    private static Object traverseFlexTypeChain(FlexTyped rootFlexTypedObj, String objectPath, StringBuilder displayNamePath) throws WTException {

        Stack<String> attributeChainStack = new Stack<String>();
        String[] attrTokenArray = objectPath.split(AERO_OBJECT_PATH_SEPARATOR);
        int tokenCount = 0;

        for(tokenCount = attrTokenArray.length; tokenCount > 0; tokenCount -= 1) {
            attributeChainStack.push(attrTokenArray[tokenCount-1]);
        }

        return traverseNextFlexTypeNode(rootFlexTypedObj, attributeChainStack, displayNamePath);
    }

    /**
     * Traverses thru one node to the next of the object path chain of FlexTyped objects thru recursion, until the end of the chain is reached.
     * Or until the object extracted is not FlexTyped object. A stack is used to control the progress thru the chain of objects.
     * @param flexTypedObjNode The FlexTyped object which is part of the object chain.
     * @param attributeChainStack A stack which contains the list of remaining object ref attributes that remain to be traversed from the chain.
     * Each recursive call to this method will reduce the stack until it's last element is processed, or until an object different from
     * FlexTyped object is found.
     * @return The value extracted from the FlexTyped object node given.
     * @throws WTException If an error occurs while extracting values from the FlexTyped object given.
     */
    private static Object traverseNextFlexTypeNode(FlexTyped flexTypedObjNode,
                                                   Stack<String> attributeChainStack,
                                                   StringBuilder displayNamePath) throws WTException {

        String internalAttrName = attributeChainStack.pop();
        Object flexObjValue = flexTypedObjNode.getValue(internalAttrName);

        if (displayNamePath != null) {
            if (displayNamePath.length() > 0) {
                displayNamePath.append(AERO_OBJECT_PATH_SEPARATOR);
            }
            displayNamePath.append(SparcIntegrationUtil.convertToDisplayName(internalAttrName, flexTypedObjNode));
        }

        if (!attributeChainStack.isEmpty() && flexObjValue != null && (flexObjValue instanceof FlexTyped)) {
            flexObjValue = traverseNextFlexTypeNode((FlexTyped)flexObjValue, attributeChainStack, displayNamePath);
        }

        return flexObjValue;
    }

    /**
     * Retrieve the enumeration's secondary key value for given flex type object and attribute name.
     * @param flexObj The flex objext containing the attribute.
     * @param attrName The name of the attribute to lookup for the secondary key value from.
     * @return The enumeration's secondary key for the given attribute.
     * @throws WTException If an error occurs while extracting the secondary key value from the flex object.
     */
    public static String lookupEnumSecondaryKeyValue(FlexTyped flexObj, String attrName) throws WTException {

        return lookupEnumSecondaryKeyValue(flexObj, attrName, null);

    }

    /**
     * Retrieve the enumeration's secondary key value for given flex type object and attribute name.
     * @param flexObj The flex objext containing the attribute.
     * @param attrName The name of the attribute to lookup for the secondary key value from.
     * @param compositeDelimiter A string delimiter to use when extracting values from composite attributes.
     * @return The enumeration's secondary key for the given attribute.
     * @throws WTException If an error occurs while extracting the secondary key value from the flex object.
     */
    public static String lookupEnumSecondaryKeyValue(FlexTyped flexObj, String attrName, String compositeDelimiter) throws WTException {

        return lookupEnumAttrValue(flexObj, attrName, SparcConstants.CUSTOM_SECONDARY_KEY, compositeDelimiter);

    }

    /**
     * Retrieve the enumeration's aero secondary key value for given flex type object and attribute name.
     * @param flexObj The flex objext containing the attribute.
     * @param attrName The name of the attribute to lookup for the aero secondary key value from.
     * @return The enumeration's aero secondary key for the given attribute.
     * @throws WTException If an error occurs while extracting the aero secondary key value from the flex object.
     */
    public static String lookupEnumSecondaryKeyAeroValue(FlexTyped flexObj, String attrName) throws WTException {

        return lookupEnumSecondaryKeyAeroValue(flexObj, attrName, null);

    }

    /**
     * Retrieve the enumeration's aero secondary key value for given flex type object and attribute name.
     * @param flexObj The flex objext containing the attribute.
     * @param attrName The name of the attribute to lookup for the aero secondary key value from.
     * @param compositeDelimiter A string delimiter to use when extracting values from composite attributes.
     * @return The enumeration's aero secondary key for the given attribute.
     * @throws WTException If an error occurs while extracting the aero secondary key value from the flex object.
     */
    public static String lookupEnumSecondaryKeyAeroValue(FlexTyped flexObj, String attrName, String compositeDelimiter) throws WTException {

        return lookupEnumAttrValue(flexObj, attrName, AERO_ENUM_SECONDARY_KEY_ATTR, compositeDelimiter);

    }

    /**
     * Retrieve the enumeration's display name for the given flex type object and attribute name.
     * @param flexObj The flex objext containing the attribute.
     * @param attrName The name of the attribute to lookup for the display name value from.
     * @return The enumeration's displat name for the given attribute.
     * @throws WTException If an error occurs while extracting the display name value from the flex object.
     */
    public static String lookupEnumDisplayValue(FlexTyped flexObj, String attrName) throws WTException {

        return lookupEnumDisplayValue(flexObj, attrName, null);

    }

    /**
     * Retrieve the enumeration's display name for the given flex type object and attribute name.
     * @param flexObj The flex objext containing the attribute.
     * @param attrName The name of the attribute to lookup for the display name value from.
     * @param compositeDelimiter A string delimiter to use when extracting values from composite attributes.
     * @return The enumeration's displat name for the given attribute.
     * @throws WTException If an error occurs while extracting the display name value from the flex object.
     */
    public static String lookupEnumDisplayValue(FlexTyped flexObj, String attrName, String compositeDelimiter) throws WTException {

        return lookupEnumAttrValue(flexObj, attrName, "displayName", compositeDelimiter);

    }

    /**
     * Retrieve the enumeration's value for given flex type object, object attribute name and Enum attribute name.
     * @param flexObj The flex objext containing the attribute to get the value & enum from.
     * @param attrName The name of the attribute to lookup for the Enum key value from.
     * @param enumAttrName The Enum attribute to get the value from, i.e. displayName or _CUSTOM_scSecondaryKey
     * @param compositeDelimiter A string delimiter to use when extracting values from composite attributes.
     * @return The enumeration's value for the given attribute & enum attribute.
     * @throws WTException If an error occurs while extracting the enumeration value from the flex object.
     */
    public static String lookupEnumAttrValue(FlexTyped flexObj, String attrName, String enumAttrName, String compositeDelimiter) throws WTException {

        String value = null;

        if (flexObj == null || attrName == null || enumAttrName == null) {
            return value;
        }

        final Object flexObjValue = flexObj.getValue(attrName);

        if (flexObjValue == null || !(flexObjValue instanceof String)) {
            return value;
        }

        final FlexTypeAttribute flexAttr = flexObj.getFlexType().getAttribute(attrName);

        if (flexAttr != null && flexAttr.getAttValueList() != null) {

            final String attrVarType = flexAttr.getAttVariableType();

            if (FlexAttrVariableTypes.COMPOSITE.name().equalsIgnoreCase(attrVarType)) {
                value = convertToComposite(flexObjValue.toString(), flexAttr, enumAttrName, compositeDelimiter);
            } else if (FlexAttrVariableTypes.MOALIST.name().equalsIgnoreCase(attrVarType)) {
                value = convertMOAToEnumString((String)flexObjValue, flexAttr, enumAttrName, false);
            } else if (FlexAttrVariableTypes.MOAENTRY.name().equalsIgnoreCase(attrVarType)) {
                value = convertMOAToEnumString((String)flexObjValue, flexAttr, enumAttrName, true);
            } else {
                value = flexAttr.getAttValueList().get((String)flexObjValue, enumAttrName);
            }

        }

        return value;

    }

    /**
     * Converts the given delimited string of Composite internal values to their display value counterparts.
     * @param compositeValue The delimited composite value. Note the expected delimiter is the flex default one -> |~*~|.
     * @param flexAttr The flex attribute where to extract the enumrated display value.
     * @param enumAttrName The enumeration attribute name, i.e. secondary key, aero secondary key, displayname.
     * @param newDelimiter The new delimiter to use to concatenate composite values. If null, the default flex delimiter will be used.
     * @return The converted composite string.
     */
    private static String convertToComposite(final String compositeValue, final FlexTypeAttribute flexAttr, String enumAttrName, String newDelimiter) {

        if (compositeValue == null || compositeValue.isEmpty() || flexAttr == null || enumAttrName == null) {
            return null;
        }

        if (newDelimiter == null) {
            newDelimiter = SparcCompositeDelimiters.FLEX.getValue();
        }

        final String compDelim = newDelimiter;
        final String[] percentages = compositeValue.split("\\|~\\*~\\|", -1);

        if (percentages == null || percentages.length == 0) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();

        Arrays.stream(percentages).forEach(percentage -> {
            final String[] composition = percentage.trim().split(" ", -1);

            if (composition != null && composition.length == 2) {
                final String displayComp = composition[0].trim() + " " + flexAttr.getAttValueList().get(composition[1].trim(), enumAttrName);
                sb.append(displayComp + compDelim);
            }
        });

        if (sb.length() > 0) {
            sb.delete(sb.length() - compDelim.length(), sb.length());

        }

        return sb.toString();
    }

    /**
     * Converts the given delimited string of MOA values to its equivalent enumerated values (where available) based on the given enum attribute name.
     * @param moaString The delimited MOA string of values.
     * @param flexAttr The flex attribute where to extract the enumerated value.
     * @param enumAttrName The enumeration attribute name, i.e. secondary key, aero secondary key, displayname.
     * @param keepOriginalValue Use the original value if no enum value was found.
     * @return The string of equivalent enumerated values, provided these exist for the given enum attribute key.
     */
    private static String convertMOAToEnumString(final String moaString, final FlexTypeAttribute flexAttr, String enumAttrName, boolean keepOriginalValue) {

        if (moaString == null || moaString.isEmpty() || flexAttr == null || flexAttr.getAttValueList() == null) {
            return null;
        }

        final String[] listStrings = moaString.split("\\|~\\*~\\|", -1);
        final StringBuilder enumValuesList = new StringBuilder();

        for (String str : listStrings) {

            final String secValue = flexAttr.getAttValueList().get(str, enumAttrName);

            if (secValue != null) {

                if (enumValuesList.length() > 0) {
                    enumValuesList.append(",");
                }

                enumValuesList.append(secValue);

            } else if (keepOriginalValue) {

                if (enumValuesList.length() > 0) {
                    enumValuesList.append(",");
                }

                enumValuesList.append(str);
            }

        }//end for loop.

        return enumValuesList.toString();
    }

    /**
     * Extracts the value from the given flex object using the internal attribute defined as root in the payload attribute's object path definition.<br>
     * @param flexObj The flex object to extract the value for the
     * @param attribute The attribute's definition incl object path.
     * @return The value extracted from the flex object or null if no value was found, or if no flex object, attribute or object path were specified.
     * @throws WTException If an error occurred while attempting to extract the value from the flex object.
     */
    private static Object getAttrObjectPathRoot(FlexTyped flexObj, LuckyPayloadAttribute attribute) throws WTException {

        Object objPathRoot = null;

        if (flexObj == null || attribute == null || attribute.getObjectPath() == null || attribute.getObjectPath().isBlank()) {
            return objPathRoot;
        }

        String[] attrTokenArray = attribute.getObjectPath().split(AERO_OBJECT_PATH_SEPARATOR);

        String rootAttrKey = attrTokenArray[0].trim();

        if (!rootAttrKey.isBlank()) {
            objPathRoot = flexObj.getValue(rootAttrKey);
        }

        return objPathRoot;
    }

    /**
     * Returns a comma separated string of attribute alias names from the given attribute list.
     * @param attributeList The list of attributes to extract the alias names from.
     * @return THe string of comma-separated attribute alias names.
     */
    public static String getAttributeAliasNames(List<LuckyPayloadAttribute> attributeList) {

        StringBuilder aliasNames = new StringBuilder();

        if (attributeList == null || attributeList.isEmpty()) {
            return aliasNames.toString();
        }

        for (LuckyPayloadAttribute attr : attributeList) {

            if (aliasNames.length() > 0) {
                aliasNames.append(", ");
            }

            aliasNames.append(attr.getJsonAttributeAlias());
        }

        return aliasNames.toString();

    }

    /**
     * Returns the display names text extracted from the given FlexTyped object for the given list of attributes.
     * @param flexTypedObj The FlexTyped object containing the attribute display name text to extract, or the root object
     * which contains other dependent chained object(s) containing the attribute display name text to extract.
     * @param attributeList The list of attributes to get their display names for.
     * @return A string with the list of display names.
     * @throws WTException If an error occurs while extracting the display name text from the FlexTyped object.
     */
    public static String getAttributeDisplayNames(FlexTyped flexTypedObj, List<LuckyPayloadAttribute> attributeList) throws WTException {

        StringBuilder displayNames = new StringBuilder();

        if (attributeList == null) {
            return displayNames.toString();
        }

        for (LuckyPayloadAttribute attr : attributeList) {

            if (displayNames.length() > 0) {
                displayNames.append(", ");
            }

            displayNames.append(getAttributeDisplayName(flexTypedObj, attr));
        }

        return displayNames.toString();
    }

    /**
     * Returns the display name text extracted from the given FlexTyped object for the given attribute.
     * @param flexTypedObj The FlexTyped object containing the attribute display name text to extract, or the root object
     * which contains other dependent chained object(s) containing the attribute display name text to extract.
     * @param attribute The attribute info.
     * or false to traverse thru the object path to get the display name text. If objpath is not specified, then this parameter has no effect).
     * @return The attribute display name text extracted from the FlexTyped object or <code>null</code> if an empty string is no display name text was found.
     * @throws WTException If an error occurs while extracting the display name text from the FlexTyped object.
     */
    public static String getAttributeDisplayName(FlexTyped flexTypedObj, LuckyPayloadAttribute attribute) throws WTException {

        String attrDisplayName = "";

        if (flexTypedObj == null || attribute == null || attribute.getFlexAttributeName() == null || attribute.isPlaceholder()) {
            return attrDisplayName;
        }

        if (attribute.getObjectPath() != null && !attribute.getObjectPath().isBlank()) {

            StringBuilder displayNamePath = new StringBuilder();
            Object flexChainedObject = traverseFlexTypeChain(flexTypedObj, attribute.getObjectPath(), displayNamePath);

            if (flexChainedObject instanceof FlexTyped) {
                displayNamePath.append(AERO_OBJECT_PATH_SEPARATOR)
                        .append("" + SparcIntegrationUtil.convertToDisplayName(attribute.getFlexAttributeName(), (FlexTyped)flexChainedObject));
            }

            attrDisplayName = displayNamePath.toString();

        } else {
            attrDisplayName = "" + SparcIntegrationUtil.convertToDisplayName(attribute.getFlexAttributeName(), flexTypedObj);
        }

        return attrDisplayName;
    }

    /**
     * Returns a filtered list of attributes marked as non-blank from the given attributes list.
     * @param attrList The list of attributes to filter.
     * @return The list of attributes marked as non-blank.
     */
    public static List<LuckyPayloadAttribute> filterNonBlanks(List<LuckyPayloadAttribute> attrList) {

        List<LuckyPayloadAttribute> nonBlankAttrList = new ArrayList<>();

        for (LuckyPayloadAttribute attr: attrList) {
            if (attr.isNoBlanks()) {
                nonBlankAttrList.add(attr);
            }
        }

        return nonBlankAttrList;
    }

    /**
     * Checks if any non-blank required attribute values within the given flex object is blank, and throws an exception if so.
     * @param flexObj The flex object containing the attributes values to check.
     * @param attrList The list of attributes to check their values for blanks. Only those attributes with no-blank flag enabled will be evaluated.
     * @throws LCSException If a non-blank required attribute value was found to be blank.
     * @throws WTException If an error occurs while extracting data from the flex object.
     */
    public static void validateNoBlanks(FlexTyped flexObj, List<LuckyPayloadAttribute> attrList) throws LCSException, WTException {

        if (flexObj == null || attrList == null || attrList.isEmpty()) {
            return;
        }

        List<LuckyPayloadAttribute> nonBlankAttrlist = filterNonBlanks(attrList);

        for (LuckyPayloadAttribute attr : nonBlankAttrlist) {

            Object value = getAttributeValue(flexObj, attr);
            if (value == null || value.toString().trim().isEmpty()|| (value instanceof Number && ((Number)value).doubleValue() == 0.0)) {
                throw new LCSException("Requested action is unable to complete.\n\r"
                        + "The following list of attributes cannot be empty (blank): " + getAttributeDisplayNames(flexObj, nonBlankAttrlist) + ".");
            }
        }
    }
    /**
     * Checks if any non-blank required attribute values within the given flex object is blank, and throws an exception if so.
     * @param flexObj The flex object containing the attributes values to check.
     * @param attrList The list of attributes to check their values for blanks. Only those attributes with no-blank flag enabled will be evaluated.
     * @throws LCSException If a non-blank required attribute value was found to be blank.
     * @throws WTException If an error occurs while extracting data from the flex object.
     */
    public static void validateSourcingBlanks(FlexTyped flexObj, List<LuckyPayloadAttribute> attrList) throws LCSException, WTException {

        if (flexObj == null || attrList == null || attrList.isEmpty()) {
            return;
        }

        List<LuckyPayloadAttribute> nonBlankAttrlist = filterNonBlanks(attrList);

        for (LuckyPayloadAttribute attr : nonBlankAttrlist) {

            Object value = getAttributeValue(flexObj, attr);
            if (value == null || value.toString().trim().isEmpty()) {
                throw new LCSException(CUSTOM_ERROR_MESSAGE);
            }
        }
    }

    /**
     *
     * @param flexObj
     * @param attrList
     * @param incoTerms
     * @throws LCSException
     * @throws WTException
     */
    public static void validateFullCircleBlanks(FlexTyped flexObj, List<LuckyPayloadAttribute> attrList,Object incoTerms) throws LCSException, WTException {
        if (flexObj == null || attrList == null || attrList.isEmpty()) {
            return;
        }
        LCSSupplier objVendor = null;
        int i= 0;
        for (LuckyPayloadAttribute attr : attrList) {
            Object value = getAttributeValue(flexObj, attr);
            objVendor= (LCSSupplier)SparcIntegrationUtil.getValueFrom(flexObj,"vendor");
            Object incoValue = SparcIntegrationUtil.getValueFrom(objVendor,incoTermsMap.get(incoTermsPrefix+attr.getFlexAttributeName()));
            if(FormatHelper.equalsWithNull(incoValue,null)){
                i = i + 1;
                continue;
            }
            if(SparcIntegrationUtil.isSame(incoTerms,incoValue)) {
                if (value == null || value.toString().trim().isEmpty()) {
                    throw new LCSException(FULL_CIRCLE_VENDOR_MESSAGE);
                }
            }
            else{
                i = i + 1;
            }
        }
        if(i == 3){
            throw new LCSException(FULL_CIRCLE_VENDOR_MESSAGE);
        }
    }

    /**
     * Returns a filtered list of attributes marked as locked from the given attributes list.
     * @param attrList The list of attributes to filter.
     * @return The list of attributes marked as locked.
     */
    public static List<LuckyPayloadAttribute> filterLocked(List<LuckyPayloadAttribute> attrList) {

        List<LuckyPayloadAttribute> lockedAttrList = new ArrayList<>();

        for (LuckyPayloadAttribute attr: attrList) {
            if (attr.isLockAttrUpdate()) {
                lockedAttrList.add(attr);
            }
        }

        return lockedAttrList;
    }

    /**
     * Checks for any locked required attributes within the given flex object, and throws an exception if found.
     * @param flexObj The flex object containing the attributes to check.
     * @param attrList The list of attributes to check if locked required.
     * @throws LCSException If a locked required attribute was found.
     * @throws WTException If an error occurs while extracting data from the flex object.
     */
    public static void validateLocked(FlexTyped flexObj, List<LuckyPayloadAttribute> attrList) throws LCSException, WTException {

        if (flexObj == null || attrList == null || attrList.isEmpty()) {
            return;
        }

        StringBuilder lockedAttrs = new StringBuilder();
        List<LuckyPayloadAttribute> lockedAttrlist = filterLocked(attrList);

        for (LuckyPayloadAttribute attr : lockedAttrlist) {

            if (lockedAttrs.length() > 0) {
                lockedAttrs.append(", ");
            }

            lockedAttrs.append(getAttributeDisplayName(flexObj, attr));

        }//end for loop.

        if (lockedAttrs.length() > 0) {
            throw new LCSException("Requested action is unable to complete.\n\r"
                    + "The following list of attributes are locked and cannot be modified: " + lockedAttrs.toString() + ".");
        }

    }

    /**
     * Prints the contents of the payload to the given logger.
     * @param payload The Map containing the payload to print.
     * @param logger The logger to print the contents of the payload to.
     */
    public static void logPayload(Map<String, Object> payload,
                                  Logger logger) {

        if (logger == null || payload == null) {
            return;
        }

        logger.debug("Printing payload data...");

        if (payload.isEmpty()) {
            logger.debug("No data found in the payload.");
        } else {
            logger.debug("Found " + payload.size() + " attributes within the payload.");
        }

        int count = 1;

        for (String attrKey : payload.keySet()) {
            logger.debug(count + " -> " + attrKey + ":" + payload.get(attrKey));
            count += 1;
        }

    }

    /**
     * Reverse lookup the internal value for the given attribute within the flex object given.<br>
     * For example, if the attribute is linked to an enum, then this method will find the internal name within the enum for the given value.
     * Or, if the attribute is linked to a reference list, then this method will find the object reference for the given value.<br>
     * Note: Reverse lookup of a value associated to an object reference list attribute type is not supported in this method.
     * @param flexTypeAttribute The flex object attribute.
     * @param value The value to reverse lookup.
     * @return The reversed value if found, or <code>null</code> if not found.
     */
    public static Object lookupInternalValue(final FlexTypeAttribute flexTypeAttribute, final Object value) throws WTException, Exception {

        Object internalValue = value;

        if (flexTypeAttribute == null || value == null) {
            return internalValue;
        }

        final String attrVarType = flexTypeAttribute.getAttVariableType();

        if (FlexAttrVariableTypes.BOOLEAN.name().equalsIgnoreCase(attrVarType)) {
            internalValue = parseBoolean(value);
        } else if (FlexAttrVariableTypes.DATE.name().equalsIgnoreCase(attrVarType)) {
            internalValue = parseTimestamp(value);
        } else if (FlexAttrVariableTypes.COMPOSITE.name().equalsIgnoreCase(attrVarType)) {
            internalValue = parseChoice(flexTypeAttribute, value);
        } else if (FlexAttrVariableTypes.MOALIST.name().equalsIgnoreCase(attrVarType)) {
            internalValue = parseMultiChoice(flexTypeAttribute, value);
        } else if (FlexAttrVariableTypes.MOAENTRY.name().equalsIgnoreCase(attrVarType)) {
            internalValue = parseMultiEntry(value);
        } else if (FlexAttrVariableTypes.CHOICE.name().equalsIgnoreCase(attrVarType)) {
            internalValue = parseChoice(flexTypeAttribute, value);
        } else if (FlexAttrVariableTypes.DRIVEN.name().equalsIgnoreCase(attrVarType)) {
            internalValue = parseChoice(flexTypeAttribute, value);
        }

        return internalValue;
    }

    /**
     * Reverse lookup the internal value for the given attribute within the flex object given.<br>
     * For example, if the attribute is linked to an enum, then this method will find the internal name within the enum for the given value.
     * Or, if the attribute is linked to a reference list, then this method will find the object reference for the given value.
     * @param flexTypedObj The flex object containing the attribute.
     * @param attribute The integration attribute definition.
     * @param value The value to reverse lookup.
     * @return The reversed value if found, or <code>null</code> if not found.
     */
    public static Object lookupInternalValue(FlexTyped flexTypedObj,
                                             LuckyPayloadAttribute attribute,
                                             Object value) throws WTException, Exception {

        Object internalValue = value;
        FlexTypeAttribute flexAttr = null;

        if (flexTypedObj == null || value == null || attribute == null || attribute.getFlexAttributeName() == null) {
            return internalValue;
        }

        flexAttr = flexTypedObj.getFlexType().getAttribute(attribute.getFlexAttributeName());

        if (flexAttr == null) {
            return internalValue;
        }

        String attrVarType = flexAttr.getAttVariableType();
        LOGGER.debug("[lookupInternalValue] attrVarType: " + attrVarType);;

        if (FlexAttrVariableTypes.OBJECT_REF_LIST.name().equalsIgnoreCase(attrVarType)) {
            internalValue = resolveObjectRefListFromValue(flexAttr, attribute, value);
        } else if (FlexAttrVariableTypes.BOOLEAN.name().equalsIgnoreCase(attrVarType)) {
            internalValue = parseBoolean(value);
        } else if (FlexAttrVariableTypes.DATE.name().equalsIgnoreCase(attrVarType)) {
            internalValue = parseTimestamp(value);
        } else if (FlexAttrVariableTypes.COMPOSITE.name().equalsIgnoreCase(attrVarType)) {
            internalValue = parseChoice(flexAttr, value);
        } else if (FlexAttrVariableTypes.MOALIST.name().equalsIgnoreCase(attrVarType)) {
            internalValue = parseMultiChoice(flexAttr, attribute, value);
        } else if (FlexAttrVariableTypes.MOAENTRY.name().equalsIgnoreCase(attrVarType)) {
            internalValue = parseMultiEntry(value);
        } else if (FlexAttrVariableTypes.CHOICE.name().equalsIgnoreCase(attrVarType)) {
            internalValue = parseChoice(flexAttr, attribute, value);
        } else if (FlexAttrVariableTypes.DRIVEN.name().equalsIgnoreCase(attrVarType)) {
            internalValue = parseChoice(flexAttr, attribute, value);
        }

        return internalValue;
    }

    /**
     * Lookup the enum's internal name from a given value.<br>
     * If the value is associated to more than one internal name from the enum, then the first one found will be returned.<br>
     * Note: This is a ported logic from SparcIntegrationUtil.invertChoice()
     * @param attValueList The list of attribute values from the enum.
     * @param enumAttrKey The enum's attribute key to extract the internal name from (if found).
     * @param value The value to use for looking up the enum's internal name.
     * @return The enum's internal name if found or <code>null</code> if not found.
     */
    public static String resolveEnumInternalNameFromValue(AttributeValueList attValueList, String enumAttrKey, Object value) {

        if (enumAttrKey == null || value == null || attValueList == null || attValueList.getKeys() == null || attValueList.getKeys().isEmpty()) {
            return null;
        }

        final Collection<String> keys = attValueList.getKeys();

        return keys.stream().filter(key -> {
            final String enumInternalName = attValueList.get(key, enumAttrKey);
            if (enumInternalName != null && enumInternalName.equals(value.toString())) {
                return true;
            }
            return false;
        }).findFirst().orElse(null);

    }

    /**
     * Attempts to parse the given value to Boolean.<br>
     * The boolean true will be returned only if the following criteria is met:<br>
     * - value given is not <code>null</code>.<br>
     * - value given contains one the following: '1', 'true', 'y' or 'yes'.<br>
     * Note: This is a ported logic from SparcIntegrationUtil.invertBoolean
     * @param value The value to format.
     * @return A Boolean equivalent to the value given or <code>null</code> if no value was provided.
     */
    public static Boolean parseBoolean(Object value) {
        Boolean bValue = null;

        if (value != null) {
            String vStr = value.toString();
            bValue = "1".equals(vStr) ||
                    "true".equalsIgnoreCase(vStr) ||
                    "y".equalsIgnoreCase(vStr) ||
                    "yes".equalsIgnoreCase(vStr);
        }

        return bValue;
    }

    /**
     * Attempts to parse the given value to Timestamp.<br>
     * The value expected must be an expression in milliseconds.<br>
     * Note: This is a ported logic from SparcIntegrationUtil.invertTimestamp
     * @param value The value to parse.
     * @return The Timestamp instance equivalent to the given value or <code>null</code> if no value was provided, or
     * @throws NumberFormatException - If unable to format the value as a long as pre-requisite to the conversion to Timestamp.
     * @throws DateTimeException - if the expression provided as value doesn't represent a valid point-in-time (Instant).
     */
    public static Timestamp parseTimestamp(final Object value) throws NumberFormatException, DateTimeException {

        Timestamp ts = null;

        if (value == null) {
            return ts;
        }

        if (value instanceof String) {
            ts = Timestamp.from(Instant.ofEpochMilli(Long.parseLong(value.toString())));
        } else if (value instanceof Long) {
            ts = Timestamp.from(Instant.ofEpochMilli((Long) value));
        }

        return ts;
    }

    /**
     * Attempts to parse the given choice value a lookup its internal enum attribute name.<br>
     * Note: This is a ported logic from SparcIntegrationUtil.invertChoice
     * @param flexTypeAttribute The flex type attribute associated to the value.
     * @param attribute The integration attribute definition.
     * @param value The value to reverse lookup.
     * @return The internal enum attribute name if found, or <code>null</code> otherwise.
     */
    public static Object parseChoice(final FlexTypeAttribute flexTypeAttribute, final LuckyPayloadAttribute attribute, final Object value) {
        final AttributeValueList attValueList = flexTypeAttribute.getAttValueList();
        if (attValueList == null || attValueList.getKeys() == null || attValueList.getKeys().isEmpty() || attribute == null || value == null) {
            LOGGER.warn("[enumAttrKey] No enums were found for attribute:" + flexTypeAttribute);
            return null;
        }

        final Collection<String> keys = attValueList.getKeys();

        return keys.stream().filter(key -> {

            if (attribute.isUseEnumSecondaryKey()) {
                final String secondaryKeyValue = attValueList.get(key, SparcConstants.CUSTOM_SECONDARY_KEY);
                if (secondaryKeyValue != null && secondaryKeyValue.equals(value.toString())) {
                    return true;
                }
            }

            if (attribute.isUseEnumDisplayName()) {
                final String displayNameValue = attValueList.get(key, "displayName");
                if (displayNameValue != null && displayNameValue.equals(value.toString())) {
                    return true;
                }
            }

            if (attribute.getEnumAttrKey() != null && !attribute.getEnumAttrKey().isBlank()) {
                String[] enumAttrTokenArray = attribute.getEnumAttrKey().split(AERO_OBJECT_PATH_SEPARATOR);

                for (String enumAttrKey : enumAttrTokenArray) {
                    final String displayNameValue = attValueList.get(key, enumAttrKey);
                    if (displayNameValue != null && displayNameValue.equals(value.toString())) {
                        return true;
                    }
                }//end for loop.

            }

            return false;
        }).findFirst().orElse(null);
    }

    /**
     * Attempts to parse the given choice value a lookup its internal enum attribute name.<br>
     * Note: This is a ported logic from SparcIntegrationUtil.invertChoice
     * @param flexTypeAttribute The flex type attribute associated to the value.
     * @param value The value to reverse lookup.
     * @return The internal enum attribute name if found, or <code>null</code> otherwise.
     */
    public static Object parseChoice(final FlexTypeAttribute flexTypeAttribute, final Object value) {

        if (flexTypeAttribute == null || value == null) {
            return null;
        }

        final AttributeValueList attValueList = flexTypeAttribute.getAttValueList();

        if (attValueList == null || attValueList.getKeys() == null || attValueList.getKeys().isEmpty()) {
            return null;
        }

        final Collection<String> keys = attValueList.getKeys();

        return keys.stream().filter(key -> {

            final String aeroSecondaryKeyValue = attValueList.get(key, AERO_ENUM_SECONDARY_KEY_ATTR);
            if (aeroSecondaryKeyValue != null && aeroSecondaryKeyValue.equals(value.toString())) {
                return true;
            }

            final String f21SecondaryKeyValue = attValueList.get(key, F21_SCM_ALT_SECONDARY_KEY_INTERNAL_NAME);
            if (f21SecondaryKeyValue != null && f21SecondaryKeyValue.equals(value.toString())) {
                return true;
            }

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

    /**
     * Attempts to parse the given multi choice value to a Flex MOA delimited string.<br>
     * Note: This is a ported logic from SparcIntegrationUtil.invertMultiChoice
     * @param flexTypeAttribute The parent flex attribute type of the value.
     * @param attribute The integration attribute definition.
     * @param value The value to parse.
     * @return The MOA string.
     */
    public static Object parseMultiChoice(final FlexTypeAttribute flexTypeAttribute, final LuckyPayloadAttribute attribute, final Object value) {
        final List<String> tokens = convertToList(value);
        final List<Object> invertedTokens = tokens.stream().map(token -> parseChoice(flexTypeAttribute, attribute, token)).collect(Collectors.toList());
        return ((invertedTokens != null && invertedTokens.size() > 0) ? MOAHelper.toMOAString(invertedTokens) : null);
    }

    /**
     * Attempts to parse the given multi choice value to a Flex MOA delimited string.<br>
     * Note: This is a ported logic from SparcIntegrationUtil.invertMultiChoice
     * @param flexTypeAttribute The parent flex attribute type of the value.
     * @param value The value to parse.
     * @return The MOA string.
     */
    public static Object parseMultiChoice(final FlexTypeAttribute flexTypeAttribute, final Object value) {
        final List<String> tokens = convertToList(value);
        final List<Object> invertedTokens = tokens.stream().map(token -> parseChoice(flexTypeAttribute, token)).collect(Collectors.toList());
        return ((invertedTokens != null && invertedTokens.size() > 0) ? MOAHelper.toMOAString(invertedTokens) : null);
    }

    /**
     * Attempts to parse the given multi entry value to a Flex MOA delimited string.<br>
     * Note: This is a ported logic from SparcIntegrationUtil.invertMultiEntry
     * @param value The value to parse.
     * @return The MOA string.
     */
    public static Object parseMultiEntry(final Object value) {
        final List<String> tokens = convertToList(value);
        return ((tokens != null && tokens.size() > 0) ? MOAHelper.toMOAString(tokens) : null);
    }

    /**
     * Attempts to parse the given composite value to a Flex MOA delimited string.<br>
     * Note: This is a ported logic from SparcIntegrationUtil.invertComposition
     * @param flexTypeAttribute The parent flex attribute type of the value.
     * @param value The value to parse.
     * @return The MOA string.
     */
    public static Object parseComposite(final FlexTypeAttribute flexTypeAttribute, final LuckyPayloadAttribute attribute, final Object value) {
        final List<String> tokens = convertToList(value);
        final List<Object> invertedTokens = tokens.stream()
                .map(token -> {
                    final String[] compositionAttrs = token.split(" ", -1);
                    if (compositionAttrs == null || compositionAttrs.length < 2) {
                        return null;
                    }
                    final Object composite = parseChoice(flexTypeAttribute, attribute, compositionAttrs[1].trim());
                    if (composite == null) {
                        LOGGER.warn("[parseComposite] The composition of " + compositionAttrs[1] + " is not defined in the enums");
                        return null;
                    }
                    return compositionAttrs[0].trim() + " " + composite;
                })
                .filter(invertedToken -> invertedToken != null)
                .collect(Collectors.toList());
        return ((invertedTokens != null && invertedTokens.size() > 0) ? MOAHelper.toMOAString(invertedTokens) : null);
    }

    /**
     * Converts the given value - assuming to be a comme-separated string - to a list,
     * Or, if the value is a list, then it'll return another list of all the original non-null elements as strings.
     * @param value The value to convert to a list.
     * @return The value converted to a list.
     */
    private static List<String> convertToList(final Object value) {

        List<String> listOfValues = new ArrayList<>();

        if (value == null) {
            return listOfValues;
        }
        if (value instanceof String) {

            if (!((String) value).isEmpty()) {

                if (((String) value).contains("|")) {
                    listOfValues = Arrays.stream(((String) value).split("\\|")).map(str -> str.trim()).collect(Collectors.toList());
                } else {
                    listOfValues = Arrays.stream(((String) value).split(",")).map(str -> str.trim()).collect(Collectors.toList());
                }

            }

        } else if (value instanceof List) {

            for (Object element : ((List<?>)value)) {
                if (element != null) {
                    listOfValues.add(element.toString());
                }
            }

        } else {
            listOfValues.add(value.toString());
        }
        return listOfValues;
    }

    /**
     * Attempts to resolve the given value from the given object reference flex attribute type.
     * @param flexTypeAttribute The flex attribute type from which to reverse lookup the value.
     * @param attribute The integration attribute definition.
     * @param value The value to reverse lookup.
     * @return The value found or <code>null</code> if no value was found.
     * @throws WTException If an error occurs while attempting to execute the object reference lookup query against PLM.
     * @throws Exception If an error cocurs while attempting to build the object reference lookup query criteria.
     */
    private static Object resolveObjectRefListFromValue(final FlexTypeAttribute flexTypeAttribute,
                                                        final LuckyPayloadAttribute attribute,
                                                        final Object value) throws WTException, Exception {

        Object objectRefValue = null;

        if (flexTypeAttribute == null || value == null || attribute == null) {
            return objectRefValue;
        }

        final ForiegnKeyDefinition refDefinition = flexTypeAttribute.getRefDefinition();

        if (refDefinition == null) {
            LOGGER.warn("[resolveObjectRefListFromValue] Expecting a valid refDefinition for flexType:" + flexTypeAttribute);
            return objectRefValue;
        }

        final PreparedQueryStatement pqs = createObjectRefQuery(refDefinition, attribute, value);
        if (pqs == null) {
            LOGGER.warn("[resolveObjectRefListFromValue] Cannot create query for object reference list type attribute:" + flexTypeAttribute);
            return objectRefValue;
        }

        LOGGER.debug("[resolveObjectRefListFromValue] Query: " + pqs);
        SearchResults searchResults = LCSQuery.runDirectQuery(pqs);

        if (searchResults == null || searchResults.getResultsFound() == 0) {
            LOGGER.warn("[resolveObjectRefListFromValue] No results were found for attribute: " + flexTypeAttribute.getAttKey() + ", with value:" + value);
            return objectRefValue;
        }

        LOGGER.debug("[resolveObjectRefListFromValue] Query results: Found " + searchResults.getResultsFound() + " records.");

        @SuppressWarnings("unchecked")
        final Collection<FlexObject> results = searchResults.getResults();

        if (results == null || results.isEmpty()) {
            LOGGER.warn("[resolveObjectRefListFromValue] No results were found for attribute:" + flexTypeAttribute.getAttKey() + ", with value:" + value);
            return objectRefValue;
        }

        final List<Object> columnValues = results.stream().map(fo -> {
            LOGGER.debug("Data column: " + refDefinition.getRefTable() + "." + refDefinition.getRefColumn());
            return fo.get(refDefinition.getRefTable() + "." + refDefinition.getRefColumn());
        }).filter(columnValue -> columnValue != null).collect(Collectors.toList());

        if (columnValues.size() != 1) {
            LOGGER.warn("[resolveObjectRefListFromValue] Found too many/no results matching the value for the given attribute:" + flexTypeAttribute.getAttKey() + ", for value:" + value.toString());
            return objectRefValue;
        }

        final String oid = refDefinition.getIdPrefix() + "" + columnValues.get(0).toString();
        objectRefValue = SparcQueryUtil.findObjectById(oid);

        return objectRefValue;
    }

    /**
     * Creates the query to reverse lookup the given value within the given object reference's flex definition.
     * @param refDefinition The object reference flex definition containing DB table and column details associated with the object reference.
     * @param attribute The attribute definition.
     * @param value The value to reverse lookup.
     * @return The prepared statement query to reverse lookup the object reference's value.
     * @throws Exception If unable to create the query criteria due to missing parameters.
     */
    private static PreparedQueryStatement createObjectRefQuery(final ForiegnKeyDefinition refDefinition,
                                                               final LuckyPayloadAttribute attribute,
                                                               final Object value) throws Exception {

        final PreparedQueryStatement pqs = new PreparedQueryStatement();
        pqs.appendSelectColumn(new QueryColumn(refDefinition.getRefTable(), refDefinition.getRefColumn()));

        pqs.appendFromTable(refDefinition.getRefTable());

        final boolean appendedCriteria = appendObjectRefQueryCriteria(pqs, refDefinition.getRefTable(), attribute, value);
        if (!appendedCriteria) {
            return null;
        }
        return pqs;
    }

    /**
     * Appends the query criteria for reverse looking up the value for the object reference.
     * @param pqs The prepared statement to append the criteria to.
     * @param refTable The DB table associated to the object reference.
     * @param attribute The attribute definition.
     * @param value The value to reverse lookup.
     * @return true if the query criteria was generated/appended successfully, false otherwise.
     * @throws Exception If any required parameters are missing.
     */
    private static boolean appendObjectRefQueryCriteria(final PreparedQueryStatement pqs,
                                                        final String refTable,
                                                        final LuckyPayloadAttribute attribute,
                                                        final Object value) throws Exception {

        boolean criteriaOK = false;

        try {

            if (pqs == null || refTable == null || attribute == null || attribute.getObjectRefPath() == null || attribute.getObjectRefAttrName() == null || value == null) {
                throw new Exception("Missing required arguments.");
            }

            final FlexType flexType = FlexTypeCache.getFlexTypeFromPath(attribute.getObjectRefPath());
            final FlexTypeAttribute flexTypeAttribute = flexType.getAttribute(attribute.getObjectRefAttrName());
            final Object invertedValue = lookupInternalValue(flexTypeAttribute, value);

            if (invertedValue == null) {
                LOGGER.error("[appendObjectRefQueryCriteria] Unable to resolve value, cannot proceed further for creating query of type object reference list");
                return criteriaOK;
            }

            pqs.appendAndIfNeeded();
            pqs.appendCriteria(new Criteria(refTable, flexTypeAttribute.getColumnName(), invertedValue.toString(), Criteria.EQUALS));

            criteriaOK = true;

        } catch (Exception e) {
            LOGGER.error("[appendObjectRefQueryCriteria] Couldn't create query criteria for object reference value lookup.", e);
        }

        return criteriaOK;
    }

}
