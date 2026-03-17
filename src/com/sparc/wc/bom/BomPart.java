package com.sparc.wc.bom;

import com.lcs.wc.flexbom.FlexBOMPart;


public class BomPart {

    private FlexBOMPart objBomPart;

    private String partNumber;

    private Boolean isPrimaryOrNot;

    private String strSourceName;


    private String strProductNo;

    private String strSourcingNo;

    private String strSpecName;

    private String strSpecId;

    private String strBOMStatus;

    private String strMaterialNo;

    private String strMaterialDesc;

    private String strSupplierNo;

    public String getStrSourceName() {
        return strSourceName;
    }

    public void setStrSourceName(String strSourceName) {
        this.strSourceName = strSourceName;
    }
    public String getStrSupplierNo() {
        return strSupplierNo;
    }

    public void setStrSupplierNo(String strSupplierNo) {
        this.strSupplierNo = strSupplierNo;
    }

    public String getStrMaterialNo() {
        return strMaterialNo;
    }

    public void setStrMaterialNo(String strMaterialNo) {
        this.strMaterialNo = strMaterialNo;
    }

    public String getStrMaterialDesc() {
        return strMaterialDesc;
    }

    public void setStrMaterialDesc(String strMaterialDesc) {
        this.strMaterialDesc = strMaterialDesc;
    }

    public String getStrBOMStatus() {
        return strBOMStatus;
    }

    public void setStrBOMStatus(String strBOMStatus) {
        this.strBOMStatus = strBOMStatus;
    }

    public FlexBOMPart getObjBomPart() {
        return objBomPart;
    }

    public void setObjBomPart(FlexBOMPart objBomPart) {
        this.objBomPart = objBomPart;
    }

    public String getStrSourcingNo() {
        return strSourcingNo;
    }

    public void setStrSourcingNo(String strSourcingNo) {
        this.strSourcingNo = strSourcingNo;
    }

    public String getStrProductNo() {
        return strProductNo;
    }

    public void setStrProductNo(String strProductNo) {
        this.strProductNo = strProductNo;
    }

    public String getStrSpecName() {
        return strSpecName;
    }

    public void setStrSpecName(String strSpecName) {
        this.strSpecName = strSpecName;
    }

    public String getStrSpecId() {
        return strSpecId;
    }

    public void setStrSpecId(String strSpecId) {
        this.strSpecId = strSpecId;
    }


    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public Boolean getPrimaryOrNot() {
        return isPrimaryOrNot;
    }

    public void setPrimaryOrNot(Boolean primaryOrNot) {
        isPrimaryOrNot = primaryOrNot;
    }
}
