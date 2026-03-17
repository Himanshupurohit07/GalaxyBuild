package com.sparc.wc.bom.services;

import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.specification.FlexSpecification;
import com.sparc.wc.bom.BomPart;

import java.util.Collection;
import java.util.List;

public interface BomExportService {

    void export(List<BomPart> bomParts,String seasonName,String seasonTypePath);

    void setFileIndex();
}
