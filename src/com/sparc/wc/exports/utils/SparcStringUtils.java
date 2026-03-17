package com.sparc.wc.exports.utils;

import com.lcs.wc.util.FormatHelper;

public final class SparcStringUtils {

    private SparcStringUtils() {

    }

    public static boolean hasContent(final String value) {
        return value != null && !value.isEmpty();
    }

    public static <T extends Enum<T>> T stringToEnum(final String value, final Class<T> targetEnumType) {

        if (!FormatHelper.hasContent(value) || targetEnumType == null) {
            return null;
        }
        try {
            return Enum.valueOf(targetEnumType, value);
        } catch (Exception e) {
            return null;
        }
    }

    public static short toShort(final String value) {
        try {
            return Short.parseShort(value);
        } catch (Exception e) {
            //do nothing
        }
        return -1;
    }
}
