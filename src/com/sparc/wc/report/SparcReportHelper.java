package com.sparc.wc.report;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.util.FormatHelper;
import wt.util.WTException;
import wt.util.WTRuntimeException;

import java.util.logging.Logger;

public class SparcReportHelper {


    private static final Logger LOGGER =
            Logger.getLogger(SparcReportHelper.class.getName());

    /* -------------------- Constants -------------------- */

    private static final String PREFIX_OR = "OR:";
    private static final String PREFIX_VR = "VR:";

    private static final String USER_LIST = "userList";
    private static final String DRIVEN = "driven";

    private static final String FULL_NAME = "FULLNAME";
    private static final String VALUE = "VALUE";

    /* -------------------- Public API -------------------- */

    /**
     * Returns the display value of an attribute based on attribute type
     *
     * @param objectID  Object ID
     * @param attKey    Attribute key
     * @param className FlexPLM class name
     * @return Display value or empty string
     * @throws WTException Windchill exception
     */
    public static String getDisplayValueForAtts(String objectID,
                                                String attKey,
                                                String className) throws WTException {

        FlexTyped flexObj = getFlexObject(className, objectID);
        if (flexObj == null) {
             LOGGER.info("Flex object not found for ID: " + objectID);
            return "";
        }

        FlexType flexType = flexObj.getFlexType();
        if (flexType == null || !flexType.attributeExist(attKey)) {
            LOGGER.info("Attribute does not exist: " + attKey);
            return "";
        }

        FlexTypeAttribute attribute = flexType.getAttribute(attKey);
        if (attribute == null) {
            return "";
        }

        String variableType = attribute.getAttVariableType();

        try {
            if (USER_LIST.equalsIgnoreCase(variableType)) {
                return resolveUserListValue(flexObj, attKey);
            }

            if (DRIVEN.equalsIgnoreCase(variableType)) {
                return resolveDrivenValue(flexObj, attribute, attKey);
            }

        } catch (Exception e) {
            LOGGER.severe("Error resolving display value for attribute: "
                    + attKey + " | Object ID: " + objectID + " | " + e.getMessage());
        }

        return "";
    }


    /**
     * Retrieves FlexTyped object using OR and VR prefixes
     */
    private static FlexTyped getFlexObject(String className, String objectID) {
        for (String prefix : new String[]{PREFIX_OR, PREFIX_VR}) {
            try {
                return (FlexTyped) LCSQuery.findObjectById(
                        prefix + className + ":" + objectID);
            } catch (WTException | WTRuntimeException e) {
                LOGGER.fine("Object not found with prefix: " + prefix);
            }
        }
        return null;
    }

    /**
     * Resolves display value for userList attribute
     */
    private static String resolveUserListValue(FlexTyped flexObj, String attKey) {
        Object value = null;
        try {
            value = flexObj.getValue(attKey);
        if (value instanceof FlexObject) {
            return ((FlexObject) value).getString(FULL_NAME);
        }
        }
        catch(Exception ex) {
        }
        return "";
    }

    /**
     * Resolves display value for driven (enum) attribute
     */
    private static String resolveDrivenValue(FlexTyped flexObj,
                                             FlexTypeAttribute attribute,
                                             String attKey) {

        String keyValue = "";
        try {
            keyValue = (String) flexObj.getValue(attKey);
            if (!FormatHelper.hasContentAllowZero(keyValue)) {
                return "";
            }

            AttributeValueList valueList = attribute.getAttValueList();
            if (valueList == null) {
                LOGGER.warning("Attribute value list is null for attribute: " + attKey);
                return "";
            }

            return valueList.get(keyValue, VALUE);
        }catch(Exception ex){

        }
        return keyValue;
    }
}

