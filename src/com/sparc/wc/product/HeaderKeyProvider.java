package com.sparc.wc.product;
import java.util.Map;
import java.util.List;

public interface HeaderKeyProvider {

    public List<Map.Entry<String, String>> getHeaderKeys();
}
