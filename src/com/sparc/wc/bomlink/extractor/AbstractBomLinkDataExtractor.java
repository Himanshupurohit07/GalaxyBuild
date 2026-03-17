package com.sparc.wc.bomlink.extractor;

import java.util.Objects;

public abstract class AbstractBomLinkDataExtractor implements BomLinkDataExtractor {

    /**
     *
     * @param value
     * @return
     */
    public static String safe(Object value) {
        return Objects.toString(value, "");
    }


}
