package com.sparc.wc.bom.services.impl;

import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.sparc.wc.bom.services.SourcingService;
import com.sparc.wc.bom.helper.SparcBomHelper;

import java.util.Collection;

public class DefaultSourcingService implements SourcingService {
    @Override
    public Collection<LCSSourcingConfig> getSourcingConfigs(LCSProduct product, LCSSeason season) {
        return SparcBomHelper.getSources(product,season);
    }
}
