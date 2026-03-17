package com.sparc.wc.bom.services.impl;

import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.specification.FlexSpecification;
import com.sparc.wc.bom.services.SpecificationService;
import com.sparc.wc.bom.helper.SparcBomHelper;

import java.util.Collection;

public class DefaultSpecificationService implements SpecificationService {
    @Override
    public Collection<FlexSpecification> getSpecifications(LCSProduct product, LCSSourcingConfig source, LCSSeason season) {
        return SparcBomHelper.getSpecs(product,season,source);
    }
}
