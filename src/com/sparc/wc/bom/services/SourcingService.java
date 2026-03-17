package com.sparc.wc.bom.services;

import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSSourcingConfig;

import java.util.Collection;

public interface SourcingService {
    Collection<LCSSourcingConfig> getSourcingConfigs(LCSProduct product, LCSSeason season);
}
