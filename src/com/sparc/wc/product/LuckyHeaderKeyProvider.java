package com.sparc.wc.product;
import com.sparc.wc.util.SparcUtil;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LuckyHeaderKeyProvider implements  HeaderKeyProvider{
    private final String headerKeyNames;

    public LuckyHeaderKeyProvider(String headerKeyNames) {
        this.headerKeyNames = headerKeyNames;
    }

    @Override
    public List<Map.Entry<String, String>> getHeaderKeys() {
        return SparcUtil.getListFromBrandContext(headerKeyNames);
    }
}
