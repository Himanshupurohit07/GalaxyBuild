package com.sparc.wc.bom.services;

import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;

import java.util.Collection;

public interface ProductService {
    Collection<LCSProduct> getProductsForSeason(LCSSeason season);
}
