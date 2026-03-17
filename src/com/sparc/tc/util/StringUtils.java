package com.sparc.tc.util;

import com.sparc.tc.domain.AlwaysTrue;
import com.sparc.tc.domain.AttributeValue;
import com.sparc.tc.domain.CurrencySymbols;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class StringUtils {

    public static final String DEFAULT_LIST_DELIMITER = ", ";

    private StringUtils() {

    }

    public static <T extends Enum<T>> T stringToEnum(final String value, final Class<T> targetEnumType) {

        if (!hasContent(value) || targetEnumType == null) {
            return null;
        }
        try {
            return Enum.valueOf(targetEnumType, value);
        } catch (Exception e) {
            return null;
        }

    }

    public static String[] trim(final String[] values) {
        if (values == null) {
            return values;
        }
        final String[] trimmed = new String[values.length];
        for (int iterator = 0; iterator < values.length; iterator++) {
            trimmed[iterator] = values[iterator].trim();
        }
        return trimmed;
    }

    public static String[] tokenizeToArray(final String value, final String delimiter) {
        if (!hasContent(value)) {
            return new String[0];
        }
        if (!hasContent(delimiter)) {
            return new String[]{value};
        }
        return trim(value.split(delimiter, -1));
    }

    public static List<String> tokenize(final String value, final String delimiter) {
        if (!hasContent(value)) {
            return new LinkedList<>();
        }
        final List<String> tokens = new LinkedList<>();
        if (!hasContent(delimiter)) {
            tokens.add(value);
            return tokens;
        }
        return Arrays.stream(value.split(delimiter, -1)).map(String::trim).collect(Collectors.toList());
    }

    public static boolean hasContent(final String value) {
        return value != null && !value.isEmpty();
    }

    public static String getCurrencyFormat(final Map<String, Object> params) {
        if (params == null) {
            return null;
        }
        if (!params.containsKey(AttributeValue.CURRENCY_TYPE)) {
            return null;
        }
        final CurrencySymbols currencySymbol = StringUtils.stringToEnum((String) params.get(AttributeValue.CURRENCY_TYPE), CurrencySymbols.class);
        if (currencySymbol == null) {
            return null;
        }
        final StringBuilder format = new StringBuilder();
        format.append("\"" + currencySymbol.getSymbol() + "\" #,###");
        final String decimalsFormat = getDecimalsFormat(params);
        if (decimalsFormat != null) {
            format.append(decimalsFormat);
        }
        return format.toString();
    }

    public static String getNumberFormat(final Map<String, Object> params) {
        if (params == null) {
            return null;
        }
        final StringBuilder format = new StringBuilder();
        format.append("#,###");
        final String decimalsFormat = getDecimalsFormat(params);
        if (decimalsFormat != null) {
            format.append(decimalsFormat);
        }
        return format.toString();
    }

    public static String getDecimalsFormat(final Map<String, Object> params) {
        if (params == null) {
            return null;
        }
        if (!params.containsKey(AttributeValue.DECIMALS) || !(params.get(AttributeValue.DECIMALS) instanceof Number)) {
            return null;
        }
        final int decimals = (int) params.get(AttributeValue.DECIMALS);
        if (decimals == 0) {
            return null;
        }
        final StringBuilder format = new StringBuilder();
        format.append(".");
        for (int decimalIterator = 0; decimalIterator < decimals; decimalIterator++) {
            format.append("0");
        }
        return format.toString();
    }

    public static String getDateFormat(final Map<String, Object> params) {
        if (params == null) {
            return null;
        }
        if (!params.containsKey(AttributeValue.DATE_FORMAT) || params.get(AttributeValue.DATE_FORMAT) == null || !(params.get(AttributeValue.DATE_FORMAT) instanceof String)) {
            return null;
        }
        return (String) params.get(AttributeValue.DATE_FORMAT);
    }

    public static Object cast(final Object value, final AttributeValue.Type type) {
        if (value == null || type == null) {
            return null;
        }
        switch (type) {
            case STRING:
            case USE_CELL_TYPE:
                if (value instanceof String || value instanceof List) {
                    return value;
                }
                return value.toString();
            case DATE:
                if (value instanceof Date) {
                    return value;
                }
                return castToDate(value);
            case BOOLEAN:
                if (value instanceof Boolean) {
                    return value;
                }
                if (value != null && stringToEnum(((String) value).toUpperCase(), AlwaysTrue.class) != null) {
                    return true;
                } else {
                    return false;
                }
            case INTEGER:
                if (value instanceof Integer) {
                    return (double) ((Integer) value).intValue();
                }
                return castToDecimal(value);
            case DECIMAL:
            case CURRENCY:
                if (value instanceof Number) {
                    return value;
                } else {
                    return castToDecimal(value);
                }
            default:
                return value;
        }
    }

    public static String convertListToString(final Object value, final Map<String, Object> params) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof List) {
            final List<Object>  listValue     = (List<Object>) value;
            final String        listDelimiter = getListDelimiter(params);
            final StringBuilder sb            = new StringBuilder();
            listValue.forEach(token -> {
                if (token != null) {
                    sb.append(token.toString() + listDelimiter);
                }
            });
            if (sb.length() > 0) {
                sb.delete(sb.length() - listDelimiter.length(), sb.length());
            }
            return sb.toString();
        }
        return null;
    }

    private static String getListDelimiter(final Map<String, Object> params) {
        if (params == null) {
            return DEFAULT_LIST_DELIMITER;
        }
        if (params.containsKey(AttributeValue.LIST_DELIMITER) && StringUtils.hasContent((String) params.get(AttributeValue.LIST_DELIMITER))) {
            return (String) params.get(AttributeValue.LIST_DELIMITER);
        }
        return DEFAULT_LIST_DELIMITER;
    }

    private static Object castToDecimal(final Object value) {
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            //do nothing;
        }
        return null;
    }

    private static Date castToDate(final Object value) {
        try {
            final Instant instant = Instant.parse(value.toString());
            return new Date(instant.getEpochSecond());
        } catch (Exception e) {
            //do nothing
        }
        return null;
    }

}
