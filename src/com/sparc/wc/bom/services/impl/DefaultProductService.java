package com.sparc.wc.bom.services.impl;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.sparc.wc.bom.services.ProductService;
import com.sparc.wc.bom.helper.SparcBomHelper;

import java.util.Collection;

public class DefaultProductService implements ProductService {

    @Override
    public Collection<LCSProduct> getProductsForSeason(LCSSeason season) {
       Collection<FlexObject> prodFlexObjects = SparcBomHelper.getProductFlexObjects(season);
       Collection<LCSProduct> objProducts =SparcBomHelper.getProductObjects(prodFlexObjects);
       return objProducts;
    }
}
