package com.sparc.wc.bom.services;

import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.specification.FlexSpecification;

import java.util.Collection;

public interface SpecificationService {

    Collection<FlexSpecification> getSpecifications(LCSProduct product, LCSSourcingConfig source, LCSSeason season);
}
