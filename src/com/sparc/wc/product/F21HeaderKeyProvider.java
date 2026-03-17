package com.sparc.wc.product;

import com.sparc.wc.util.SparcUtil;

import java.util.List;
import java.util.Map;

public class F21HeaderKeyProvider implements HeaderKeyProvider{
    private final String headerKeyNames;

    public F21HeaderKeyProvider(String headerKeyNames) {
        this.headerKeyNames = headerKeyNames;
    }

    @Override
    public List<Map.Entry<String, String>> getHeaderKeys() {
        return SparcUtil.getListFromBrandContext(headerKeyNames);
    }
}
