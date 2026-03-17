package com.sparc.wc.integration.lucky.builders;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COMMON_LOGGER_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_ATTR_KEY_PATH_SEPARATOR;

import org.apache.logging.log4j.Logger;

import com.sparc.wc.integration.lucky.domain.LuckyPayloadAttribute;
import com.sparc.wc.integration.util.SparcCompositeDelimiters;

import wt.log4j.LogR;


public class LuckyPayloadAttributeBuilder {

    private static final int MIN_ATTR_TOKENS           = 1;
    private static final int FLEX_ATTRIBUTE_NAME_INDEX = 0;
    private static final int REQUIRED_ATTR_KEY_TOKENS  = 2;
    private static final int ATTR_KEY_INDEX            = 0;
    private static final int ATTR_KEY_VALUE_INDEX      = 1;

    private static final String JSON_ALIAS_ATTR_KEY                  = "alias";
    private static final String USE_ENUM_SECONDARY_KEY_ATTR_KEY      = "enumskey";
    private static final String USE_ENUM_SECONDARY_KEY_AERO_ATTR_KEY = "enumskeyaero";
    private static final String USE_ENUM_DISPLAY_NAME_KEY_ATTR_KEY   = "enumdpname";
    private static final String USE_ENUM_ATTR_NAME_KEY               = "enumattrkeys";
    private static final String LOCK_UPDATE_KEY_ATTR_KEY             = "lockupd";
    private static final String OBJECT_PATH_ATTR_KEY                 = "objpath";
    private static final String OBJECT_REF_PATH_ATTR_KEY             = "objrefpath";
    private static final String OBJECT_REF_ATTR_NAME_ATTR_KEY        = "objrefattr";
    private static final String PLACEHOLDER_ATTR_KEY                 = "placeholder";
    private static final String FORMAT_STRING_ATTR_KEY               = "formatstr";
    private static final String FORMAT_CLASS_ATTR_KEY                = "formatclass";
    private static final String NO_BLANKS_ATTR_KEY                   = "noblanks";
    private static final String COMPOSITE_VALUE_DELIMITER_ATTR_KEY   = "compvaluedelimiterkey";

    private static final Logger LOGGER = LogR.getLogger(AERO_COMMON_LOGGER_NAME);

    private LuckyPayloadAttribute payloadAttribute;

    /**
     * Builds an AeroPayloadAttribute instance.
     * @param payloadAttribute The AeroPayloadAttribute insatance to build.
     */
    public LuckyPayloadAttributeBuilder(LuckyPayloadAttribute payloadAttribute) {
        this.payloadAttribute = payloadAttribute;
    }

    /**
     * Builds the Aero Payload Attribute based on the given concatenated string definition.<br>
     * The expected definition string model must be as follows:<br>
     * <br>- Model: <string>|<attribute-key>:<attribute-value>|....<attribute-key>:<attribute-value>
     * <br>- Note: It is expected, but not mandatory, that attribute keys to be all in lower case. Unknown/unsupported attribute keys will be ignored.
     * <br>- Example #1: scDivision (this is the simplest form, returns the value as is for the given attribute).
     * <br>- Example #2: scDivision|alias:division (this returns the value as is for the given attribute and shows up as 'division' in the json payload).
     * <br>- Example #3: scDivision|enumskey:true (this returns the enum value for "_CUSTOM_scSecondaryKey").
     * <br>- Example #4: scDivision|enumattrkeys:_CUSTOM_scF21SecondaryKey (this returns the enum value for "_CUSTOM_scF21SecondaryKey").
     * <br>- Example #5: scDivision|enumskey:true|enumdpname:true|enumattrkeys:_CUSTOM_scF21SecondaryKey (this returns the value if found for either secondary key (1st), display name (2nd) or enum attribute key "_CUSTOM_scF21SecondaryKey" (3rd)).
     * <br>- Example #6: scISOCode|objpath:scFGFactory->scCountry (this returns the value for scISOCode attribute, which is accessible from object ref 'scCountry', which is also an object ref of 'scFGFactory'.
     * <br>- Example #7: scDummy|placeholder:true (defines a dummy/placeholder attribute for the payload which doesn't exist within any flex object. The respective integration code is responsible for assigning some value to it).
     * <br>
     * <br>The following are the details for the attribute definition:
     * <br>- flex internal attribute name(required): Must always be set at the beginning of the attribute definition.
     * <br>Additional attributes (these are all optional attributes and must be written in a key:value pair and seperated by a pipe symbol '|' ):<br>
     * <br>- alias (json alias name), of type <string>. If no alias is defined, then the internal attribute name is used instead.
     * <br>- enumskey (use enum's secondary key), of type <boolean> (true or false). Default value is false.
     * <br>- enumskeyaero (use enum's aero secondary key), of type <boolean> (true or false). Default value is false.
     * <br>- enumdpname (use enum's display name), of type <boolean> (true or false). Default value is false. If enumskey is set to true, then this will execute if no secondary key value is found.
     * <br>- enumattrkeys (enum's attribute key), of type <string>. Default value is null. Searches the value thru the given Enum's attribute key(s) in the order given (use '->' as separator).
     * The search finishes once a value is found for any of the enum attribute keys or when the value has been searched for all enum attribute keys.
     * If attributes 'enumskeyaero', 'enumskey' or 'enumskeydpname' are set to true, then those will take precedence when searching for the value.
     * <br>- lockupd (lock attribute update), of type <boolean> (true or false). Default value is false. This is used from integration's plugin only.
     * <br>- objpath (object path), of type <string>. Default value is null. A list of linked/chained objects references separated by '->' where the attribute value to retrieve is located at.
     * <br>- objrefpath (object reference flex path), of type <string>. Default value is null. A flex path to an object reference. To use during reverse value lookup.
     * <br>- objrefattr (object reference flex attr name), of type <string>. Default value is null. A flex internal attribute name. To use during reverse value lookup in combination with objrefpath.
     * <br>- placeholder, of type <boolean>. Default value is false. A dummy/placeholder attribute. The respective integration code must assign a value to it.
     * <br>- formatstr, of type <string>. Default value is null. A string format as supported by java.util.Formatter.
     * <br>- formatclass, of type <string>. Default value is null. Converts the value to format to the given class before applying the format. Supported options are: Integer, Long, Float and Double
     * <br>- noblanks, of type <boolean>. Default value is false. Set to allow or not blank values when saving/updating felx object related to aero integrations.
     * <br>- compvaluedelimiterkey, of type <string>. Default value is null. Set to one of the available keys at com.sparc.wc.integration.util.SparcCompositeDelimiters, to replace the default delimiter used by flex.
     * @param attributeDefinition The concatenated attribute definition as a string.
     * @return The constructed instance of the Aero Payload Attribute or <code>null</code> if no attribute definition was supplied.
     * @throws IllegalArgumentException if the given attribute definition lacks the number of valid attributes.
     */
    public LuckyPayloadAttribute build(String attributeDefinition) {

        if (attributeDefinition == null || attributeDefinition.isEmpty()) {
            return payloadAttribute;
        }


        //scColorDescription|alias:sclCLR_DESC|objpath:color
        LOGGER.debug("About to load Aero attribute definition: " + attributeDefinition);

        final String[] attrTokens = attributeDefinition.split("\\|");

        if (attrTokens.length < MIN_ATTR_TOKENS) {
            throw new IllegalArgumentException("Integration API configuration Issue: "
                    + "The attribute definition string provided '" + attributeDefinition + "' is invalid for building an AeroPayloadAttribute instance."
                    + "Please check/fix the respective PLM integration property configuration.");
        }

        payloadAttribute.setFlexAttributeName(attrTokens[FLEX_ATTRIBUTE_NAME_INDEX].trim());

        for (String defToken : attrTokens) {

            String[] attrKeyArray = defToken.split(AERO_ATTR_KEY_PATH_SEPARATOR);

            if (attrKeyArray.length != REQUIRED_ATTR_KEY_TOKENS) {
                continue;
            }

            String attrKeyPrefix = attrKeyArray[ATTR_KEY_INDEX].trim().toLowerCase();
            String attrKeyValueSuffix = attrKeyArray[ATTR_KEY_VALUE_INDEX];

            if(attrKeyPrefix.isBlank() || attrKeyValueSuffix.isBlank()) {
                continue;
            }

            attrKeyValueSuffix = attrKeyValueSuffix.trim();

            if (attrKeyPrefix.equalsIgnoreCase(JSON_ALIAS_ATTR_KEY)) {
                payloadAttribute.setJsonAttributeAlias(attrKeyValueSuffix.trim());
            } else if (attrKeyPrefix.equalsIgnoreCase(USE_ENUM_SECONDARY_KEY_ATTR_KEY)) {
                payloadAttribute.setUseEnumSecondaryKey(Boolean.parseBoolean(attrKeyValueSuffix));
            } else if (attrKeyPrefix.equalsIgnoreCase(USE_ENUM_SECONDARY_KEY_AERO_ATTR_KEY)) {
                payloadAttribute.setUseEnumSecondaryKeyAero(Boolean.parseBoolean(attrKeyValueSuffix));
            } else if (attrKeyPrefix.equalsIgnoreCase(USE_ENUM_DISPLAY_NAME_KEY_ATTR_KEY)) {
                payloadAttribute.setUseEnumDisplayName(Boolean.parseBoolean(attrKeyValueSuffix));
            } else if (attrKeyPrefix.equalsIgnoreCase(LOCK_UPDATE_KEY_ATTR_KEY)) {
                payloadAttribute.setLockAttrUpdate(Boolean.parseBoolean(attrKeyValueSuffix));
            } else if (attrKeyPrefix.equalsIgnoreCase(USE_ENUM_ATTR_NAME_KEY)) {
                payloadAttribute.setEnumAttrKey(attrKeyValueSuffix);
            } else if (attrKeyPrefix.equalsIgnoreCase(OBJECT_PATH_ATTR_KEY)) {
                payloadAttribute.setObjectPath(attrKeyValueSuffix);
            } else if (attrKeyPrefix.equalsIgnoreCase(OBJECT_REF_PATH_ATTR_KEY)) {
                payloadAttribute.setObjectRefPath(attrKeyValueSuffix);
            } else if (attrKeyPrefix.equalsIgnoreCase(OBJECT_REF_ATTR_NAME_ATTR_KEY)) {
                payloadAttribute.setObjectRefAttrName(attrKeyValueSuffix);
            } else if (attrKeyPrefix.equalsIgnoreCase(PLACEHOLDER_ATTR_KEY)) {
                payloadAttribute.setPlaceholder(Boolean.parseBoolean(attrKeyValueSuffix));
            } else if (attrKeyPrefix.equalsIgnoreCase(FORMAT_CLASS_ATTR_KEY)) {
                payloadAttribute.setFormatClass(attrKeyValueSuffix);
            } else if (attrKeyPrefix.equalsIgnoreCase(FORMAT_STRING_ATTR_KEY)) {
                payloadAttribute.setFormat(attrKeyValueSuffix);
            } else if (attrKeyPrefix.equalsIgnoreCase(NO_BLANKS_ATTR_KEY)) {
                payloadAttribute.setNoBlanks(Boolean.parseBoolean(attrKeyValueSuffix));
            } else if (attrKeyPrefix.equalsIgnoreCase(COMPOSITE_VALUE_DELIMITER_ATTR_KEY)) {
                payloadAttribute.setCompositeValueDelimiter(resolveCompositeValueDelimiter(attrKeyValueSuffix));
            } else {
                LOGGER.warn("Unrecognized attribute '" + attrKeyPrefix + "' was found in the attribute definition. It will be ignored.");
            }

        }//end for loop

        //If no JSON alias has been defined for the attribute, then use the attribute name as alias.
        if (payloadAttribute.getJsonAttributeAlias() == null) {
            payloadAttribute.setJsonAttributeAlias(payloadAttribute.getFlexAttributeName());
        }

        return payloadAttribute;
    }

    /**
     * Resolves the delimiter value from the given key.
     * @param delimiterKey The delimiter key to use for extracting the appropriate delimiter value.
     * @return The delimiter value corresponding to the key provided, or <code>null</code> if no value was found for the given key.
     */
    private String resolveCompositeValueDelimiter(String delimiterKey) {

        String delimiterValue = null;

        if (SparcCompositeDelimiters.COMMA.name().equalsIgnoreCase(delimiterKey)) {
            delimiterValue = SparcCompositeDelimiters.COMMA.getValue();
        } else if (SparcCompositeDelimiters.SPACE.name().equalsIgnoreCase(delimiterKey)) {
            delimiterValue = SparcCompositeDelimiters.SPACE.getValue();
        } else if (SparcCompositeDelimiters.EMPTY.name().equalsIgnoreCase(delimiterKey)) {
            delimiterValue = SparcCompositeDelimiters.EMPTY.getValue();
        } else if (SparcCompositeDelimiters.COLON.name().equalsIgnoreCase(delimiterKey)) {
            delimiterValue = SparcCompositeDelimiters.COLON.getValue();
        } else if (SparcCompositeDelimiters.SEMICOLON.name().equalsIgnoreCase(delimiterKey)) {
            delimiterValue = SparcCompositeDelimiters.SEMICOLON.getValue();
        } else if (SparcCompositeDelimiters.UNDERSCORE.name().equalsIgnoreCase(delimiterKey)) {
            delimiterValue = SparcCompositeDelimiters.UNDERSCORE.getValue();
        } else if (SparcCompositeDelimiters.HYPHEN.name().equalsIgnoreCase(delimiterKey)) {
            delimiterValue = SparcCompositeDelimiters.HYPHEN.getValue();
        } else if (SparcCompositeDelimiters.FLEX.name().equalsIgnoreCase(delimiterKey)) {
            delimiterValue = SparcCompositeDelimiters.FLEX.getValue();
        }

        return delimiterValue;
    }

}
