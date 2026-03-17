package com.sparc.wc.bomlink.extractor;

import com.lcs.wc.flexbom.FlexBOMLink;
import com.lcs.wc.material.LCSMaterialQuery;
import com.lcs.wc.supplier.LCSSupplierQuery;
import com.lcs.wc.util.FormatHelper;
import com.sparc.wc.bom.helper.SparcBomHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.sparc.wc.bom.constants.SparcMigrationConstants.*;


public class DefaultBomLinkDataExtractor extends AbstractBomLinkDataExtractor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBomLinkDataExtractor.class);


    /**
     *
     * @param row
     * @param section
     * @param strBomPartId
     * @return
     */
    @Override
    public String extractData(FlexBOMLink row, String section, String strBomPartId,Boolean isAppOrFtw) {
        String strDimensionName = Optional.ofNullable(row.getDimensionName()).orElse("");
        String supplierValue = BomDataExtractorHelper.getSupplierValue(row.getSupplier());
        String strColorway = EMPTY_SPACE;
        String strColor = EMPTY_SPACE;
        String strSourcingNo = EMPTY_SPACE;
        String finalString = EMPTY_SPACE;
        String strMaterialColor = BomDataExtractorHelper.getMaterialColor(row.getMaterialColor());
        String strMatDesc = BomDataExtractorHelper.getMaterialDesc(row);
        String materialValue = EMPTY_SPACE;

        if(LCSMaterialQuery.PLACEHOLDER.equals(row.getChild())){
            materialValue =MATERIAL_PLACEHOLDER;
        }
        else{
            materialValue = BomDataExtractorHelper.getMaterialValue(row.getChild());
        }
        if (LCSSupplierQuery.PLACEHOLDER.equals(row.getSupplier())) {
            supplierValue = SUPPLIER_PLACEHOLDER;
        } else {
            supplierValue = BomDataExtractorHelper.getSupplierValue(row.getSupplier());
        }
        String bomLinkIda2a2 = FormatHelper.getNumericObjectIdFromObject(row);

        System.out.println("row WCPARTID------------------"+row.getWcPart());
		strColor = BomDataExtractorHelper.getColorNo(row.getColor());

        try {
            List<String> baseParts = Arrays.asList(
                    strBomPartId,
                    materialValue,
                    strMatDesc,
                    supplierValue,
                    strDimensionName,
                    section,
                    safe(row.getBranchId()),
                    safe(row.getSortingNumber()),
                    safe(row.getSequence()),
                    strColorway,
                    strColor,
                    strSourcingNo,
                    strMaterialColor,
                    bomLinkIda2a2
            );
            List<String> fullParts= new ArrayList<>(baseParts);
            fullParts =SparcBomHelper.formBomLinkData(fullParts,row,isAppOrFtw);

            fullParts = fullParts.stream()
                    .map(part -> part == null ? "" : part.replaceAll("[\\r\\n]+", "")) // Replace newlines with space
                    .collect(Collectors.toList());

            finalString = String.join(SPECIAL_DELIMITER,fullParts);

        } catch (Exception e) {
            logger.error("Error while forming final string", e);
        }
        return finalString;
    }
}

