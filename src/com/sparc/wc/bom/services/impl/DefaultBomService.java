package com.sparc.wc.bom.services.impl;

import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.specification.FlexSpecification;
import com.sparc.wc.bom.services.BomService;
import com.sparc.wc.bom.helper.SparcBomHelper;

import java.util.Collection;

public class DefaultBomService implements BomService {
    @Override
    public Collection<FlexBOMPart> getBOMParts(LCSProduct product, LCSSourcingConfig source, FlexSpecification spec) {
        return SparcBomHelper.getBOMs(product,source,spec);
    }
}
