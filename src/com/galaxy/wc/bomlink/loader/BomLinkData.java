package com.galaxy.wc.bomlink.loader;

import java.util.List;
import java.util.ArrayList;

public class BomLinkData {

    private String prodID;
    private String bomName;
    private String bomMasterId;
    private String matId;
    private String matName;
    private String suppId;
    private String dimensionName;
    private String section;
    private int branchId;
    private int sortingNumber;
    private int sequence;
    private String colorway;
    private String color;
    private String sourcingNo;
    private String materialColor;
    private String scComponent;
    private String scAppPlacement;
    private String scBOMDetailedContent;
    private Double scCalculationSizePercent;
    private Double scCIF;
    private double quantity;
    private double lossAdjustment;
    private double priceOverride;
    private String comments;
    private double scGrossQuantity;
    private String strBOMLinkIDA2A2;

    private String strLineNo;

    private String colorDesc;
	
	private String strHighLight;

    public String getStrHighLight() {
        return strHighLight;
    }

    public void setStrHighLight(String strHighLight) {
        this.strHighLight = strHighLight;
    }

    public String getColorDesc() {
        return colorDesc;
    }

    public void setColorDesc(String colorDesc) {
        this.colorDesc = colorDesc;
    }

    public String getStrLineNo() {
        return strLineNo;
    }

    public void setStrLineNo(String strLineNo) {
        this.strLineNo = strLineNo;
    }

    public String getStrBOMLinkIDA2A2() {
        return strBOMLinkIDA2A2;
    }

    public void setStrBOMLinkIDA2A2(String strBOMLinkIDA2A2) {
        this.strBOMLinkIDA2A2 = strBOMLinkIDA2A2;
    }

    public double getScGrossQuantity() {
        return scGrossQuantity;
    }

    public void setScGrossQuantity(double scGrossQuantity) {
        this.scGrossQuantity = scGrossQuantity;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getProdID() {
        return prodID;
    }

    public void setProdID(String prodID) {
        this.prodID = prodID;
    }

    public String getBomName() {
        return bomName;
    }

    public void setBomName(String bomName) {
        this.bomName = bomName;
    }

    public void setBomMasterId(String bomMasterId) {
        this.bomMasterId = bomMasterId;
    }

    public String getMatId() {
        return matId;
    }

    public void setMatId(String matId) {
        this.matId = matId;
    }

    public String getMatName() {
        return matName;
    }

    public void setMatName(String matName) {
        this.matName = matName;
    }

    public String getSuppId() {
        return suppId;
    }

    public void setSuppId(String suppId) {
        this.suppId = suppId;
    }

    public void setDimensionName(String dimensionName) {
        this.dimensionName = dimensionName;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public int getSortingNumber() {
        return sortingNumber;
    }

    public void setSortingNumber(int sortingNumber) {
        this.sortingNumber = sortingNumber;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getColorway() {
        return colorway;
    }

    public void setColorway(String colorway) {
        this.colorway = colorway;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSourcingNo() {
        return sourcingNo;
    }

    public void setSourcingNo(String sourcingNo) {
        this.sourcingNo = sourcingNo;
    }

    public String getMaterialColor() {
        return materialColor;
    }

    public void setMaterialColor(String materialColor) {
        this.materialColor = materialColor;
    }

    public String getScComponent() {
        return scComponent;
    }

    public void setScComponent(String scComponent) {
        this.scComponent = scComponent;
    }

    public String getScAppPlacement() {
        return scAppPlacement;
    }

    public void setScAppPlacement(String scAppPlacement) {
        this.scAppPlacement = scAppPlacement;
    }

    public String getScBOMDetailedContent() {
        return scBOMDetailedContent;
    }

    public void setScBOMDetailedContent(String scBOMDetailedContent) {
        this.scBOMDetailedContent = scBOMDetailedContent;
    }

    public Double getScCalculationSizePercent() {
        return scCalculationSizePercent;
    }

    public void setScCalculationSizePercent(Double scCalculationSizePercent) {
        this.scCalculationSizePercent = scCalculationSizePercent;
    }

    public Double getScCIF() {
        return scCIF;
    }

    public void setScCIF(Double scCIF) {
        this.scCIF = scCIF;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getLossAdjustment() {
        return lossAdjustment;
    }

    public void setLossAdjustment(double lossAdjustment) {
        this.lossAdjustment = lossAdjustment;
    }

    public double getPriceOverride() {
        return priceOverride;
    }

    public void setPriceOverride(double priceOverride) {
        this.priceOverride = priceOverride;
    }

    private final List<BomLinkData> variatedLinks = new ArrayList<>();

    public void addVariatedLink(BomLinkData variatedLink) {
        variatedLinks.add(variatedLink);
    }

    public List<BomLinkData> getVariatedLinks() {
        return variatedLinks;
    }

    // Constructor, getters, setters, builder, etc. – omitted for brevity.
    public static BomLinkData fromCSV(String line) {
        String[] tokens = line.split("\\^", -1); // Allow empty fields
        BomLinkData data = new BomLinkData();
        data.prodID = tokens[0];
        data.bomName = tokens[1];
        data.bomMasterId = tokens[2];
        data.matId = tokens[3];
        data.matName = tokens[4];
        data.suppId = tokens[5];
        data.dimensionName = tokens[6];
        data.section = tokens[7];
        data.branchId = Integer.parseInt(tokens[8]);
        data.sortingNumber = Integer.parseInt(tokens[9]);
        data.sequence = Integer.parseInt(tokens[10]);
        data.colorway = tokens[11];
        data.color = tokens[12];
        data.sourcingNo = tokens[13];
        data.materialColor = tokens[14];
        data.strBOMLinkIDA2A2 = tokens[15];
        data.scComponent = tokens[16];
        data.scAppPlacement = tokens[17];
        data.scBOMDetailedContent = tokens[18];
        data.scCalculationSizePercent = Double.parseDouble(tokens[19]);
        data.scCIF = Double.parseDouble(tokens[20]);
        data.quantity = Double.parseDouble(tokens[21]);
        data.lossAdjustment = Double.parseDouble(tokens[22]);
        data.priceOverride = Double.parseDouble(tokens[23]);
        data.comments =tokens[24];
        data.scGrossQuantity = Double.parseDouble(tokens[25]);
        data.colorDesc = tokens[26];
        data.strHighLight = tokens[27];
        data.strLineNo = tokens[28];
        return data;
    }

    public String getBomMasterId() { return bomMasterId; }
    public int getBranchId() { return branchId; }
    public String getDimensionName() { return dimensionName; }

    @Override
    public String toString() {
        return bomMasterId + "-"+dimensionName+"-"+section+"-"+branchId+"-"+section+"-"+matId+"-"+suppId;
    }
}