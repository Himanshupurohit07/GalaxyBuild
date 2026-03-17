package com.sparc.wc.integration.lucky.domain;


import java.util.*;


import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_PLUGIN_ATTRIBUTES;


public class LuckyPayloadAttributeDefinitions {



    /**
     * Cache for Article attributes for CAP integration.
     */
    private static final Map<String, List<LuckyPayloadAttribute>> LUCKY_CAP_ARTICLE_PAYLOAD_ATTR_MAP = new HashMap<>();





    static {
        loadAttributeDefinitions(LUCKY_COSTING_PLUGIN_ATTRIBUTES, LUCKY_CAP_ARTICLE_PAYLOAD_ATTR_MAP);
    }

    /**
     * Parses and loads the attributes definitions from the given raw definitions map into the given target one used as cache.
     * @param LuckyAttrSourceDefsMap The source Map containing raw attribute mapping definitions.
     * @param LuckyAttrTargetMap The target Map to store the parsed attribute mapping definitions.
     */
    private static void loadAttributeDefinitions(Map<String, Set<String>> LuckyAttrSourceDefsMap,
                                                 Map<String, List<LuckyPayloadAttribute>> LuckyAttrTargetMap) {

        if (LuckyAttrSourceDefsMap == null || LuckyAttrSourceDefsMap.isEmpty() || LuckyAttrTargetMap == null) {
            return;
        }

        LuckyAttrSourceDefsMap.forEach((key, attrDefinitions) -> {
            List<LuckyPayloadAttribute> payloadAttrs = new ArrayList<>();
            List<LuckyPayloadAttribute> noBlanksAttrs = new ArrayList<>();
            List<LuckyPayloadAttribute> lockedAttrs = new ArrayList<>();

            attrDefinitions.forEach((attrDef) -> {
                if (attrDef != null && !attrDef.isBlank()) {
                    LuckyPayloadAttribute attr = LuckyPayloadAttribute.newBuilder().build(attrDef);
                    payloadAttrs.add(attr);

                    if (attr.isNoBlanks()) {
                        noBlanksAttrs.add(attr);
                    }

                    if (attr.isLockAttrUpdate()) {
                        lockedAttrs.add(attr);
                    }
                }
            });
            LuckyAttrTargetMap.put(key, payloadAttrs);

        });

    }



    /**
     * Returns the list of CAP Article payload attributes definitions for the given flex type group key.<br>
     * The flex type group key refers to one of the flex types, for example "PRODUCT" or "COLORWAY".<br>
     * Please refer to the various flex type group key constants available at the SparcIntegrationConstants.
     * These can be recognize as they have the following naming format: Lucky_<flex type ref>_ATTR_GROUP_KEY.
     * @param flexTypeGroupKey The flex type group key that identifies the list of attribute definitions.
     * @return The list of payload attributes or <code>null</code> if no attributes were found for the given flex type group key.
     */
    public static List<LuckyPayloadAttribute> getCostingAttributesDefinitionsForFlexType(String flexTypeGroupKey) {
        return LUCKY_CAP_ARTICLE_PAYLOAD_ATTR_MAP.get(flexTypeGroupKey);
    }



    private LuckyPayloadAttributeDefinitions() {

    }

}
