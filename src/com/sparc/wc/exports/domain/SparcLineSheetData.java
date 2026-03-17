package com.sparc.wc.exports.domain;

import java.util.List;

public class SparcLineSheetData {

    public enum ExportType {
        DETAILED, HIGHLEVEL;
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
//</editor-fold>
    public enum Division {
        APPAREL, FOOTWEAR, NONAPPAREL, LICENSED, HARDWARE;
    }


    public static class CostSheet {
        private String costSheetOid;
        private String skuSeasonOid;


        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public static class CostSheetBuilder {
            @SuppressWarnings("all")
            private String costSheetOid;
            @SuppressWarnings("all")
            private String skuSeasonOid;

            @SuppressWarnings("all")
            CostSheetBuilder() {
            }

            @SuppressWarnings("all")
            public SparcLineSheetData.CostSheet.CostSheetBuilder costSheetOid(final String costSheetOid) {
                this.costSheetOid = costSheetOid;
                return this;
            }

            @SuppressWarnings("all")
            public SparcLineSheetData.CostSheet.CostSheetBuilder skuSeasonOid(final String skuSeasonOid) {
                this.skuSeasonOid = skuSeasonOid;
                return this;
            }

            @SuppressWarnings("all")
            public SparcLineSheetData.CostSheet build() {
                return new SparcLineSheetData.CostSheet(this.costSheetOid, this.skuSeasonOid);
            }

            @Override
            @SuppressWarnings("all")
            public String toString() {
                return "SparcLineSheetData.CostSheet.CostSheetBuilder(costSheetOid=" + this.costSheetOid + ", skuSeasonOid=" + this.skuSeasonOid + ")";
            }
        }

        @SuppressWarnings("all")
        public static SparcLineSheetData.CostSheet.CostSheetBuilder builder() {
            return new SparcLineSheetData.CostSheet.CostSheetBuilder();
        }

        @SuppressWarnings("all")
        public String getCostSheetOid() {
            return this.costSheetOid;
        }

        @SuppressWarnings("all")
        public String getSkuSeasonOid() {
            return this.skuSeasonOid;
        }

        @SuppressWarnings("all")
        public void setCostSheetOid(final String costSheetOid) {
            this.costSheetOid = costSheetOid;
        }

        @SuppressWarnings("all")
        public void setSkuSeasonOid(final String skuSeasonOid) {
            this.skuSeasonOid = skuSeasonOid;
        }

        @SuppressWarnings("all")
        public CostSheet() {
        }

        @SuppressWarnings("all")
        public CostSheet(final String costSheetOid, final String skuSeasonOid) {
            this.costSheetOid = costSheetOid;
            this.skuSeasonOid = skuSeasonOid;
        }
        //</editor-fold>
    }

    private String seasonOid;
    private ExportType exportType;
    private Division division;
    private List<CostSheet> costSheetList;

    public boolean isValid() {
        return exportType != null && division != null;
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public static class SparcLineSheetDataBuilder {
        @SuppressWarnings("all")
        private String seasonOid;
        @SuppressWarnings("all")
        private ExportType exportType;
        @SuppressWarnings("all")
        private Division division;
        @SuppressWarnings("all")
        private List<CostSheet> costSheetList;

        @SuppressWarnings("all")
        SparcLineSheetDataBuilder() {
        }

        @SuppressWarnings("all")
        public SparcLineSheetData.SparcLineSheetDataBuilder seasonOid(final String seasonOid) {
            this.seasonOid = seasonOid;
            return this;
        }

        @SuppressWarnings("all")
        public SparcLineSheetData.SparcLineSheetDataBuilder exportType(final ExportType exportType) {
            this.exportType = exportType;
            return this;
        }

        @SuppressWarnings("all")
        public SparcLineSheetData.SparcLineSheetDataBuilder division(final Division division) {
            this.division = division;
            return this;
        }

        @SuppressWarnings("all")
        public SparcLineSheetData.SparcLineSheetDataBuilder costSheetList(final List<CostSheet> costSheetList) {
            this.costSheetList = costSheetList;
            return this;
        }

        @SuppressWarnings("all")
        public SparcLineSheetData build() {
            return new SparcLineSheetData(this.seasonOid, this.exportType, this.division, this.costSheetList);
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "SparcLineSheetData.SparcLineSheetDataBuilder(seasonOid=" + this.seasonOid + ", exportType=" + this.exportType + ", division=" + this.division + ", costSheetList=" + this.costSheetList + ")";
        }
    }

    @SuppressWarnings("all")
    public static SparcLineSheetData.SparcLineSheetDataBuilder builder() {
        return new SparcLineSheetData.SparcLineSheetDataBuilder();
    }

    @SuppressWarnings("all")
    public String getSeasonOid() {
        return this.seasonOid;
    }

    @SuppressWarnings("all")
    public ExportType getExportType() {
        return this.exportType;
    }

    @SuppressWarnings("all")
    public Division getDivision() {
        return this.division;
    }

    @SuppressWarnings("all")
    public List<CostSheet> getCostSheetList() {
        return this.costSheetList;
    }

    @SuppressWarnings("all")
    public void setSeasonOid(final String seasonOid) {
        this.seasonOid = seasonOid;
    }

    @SuppressWarnings("all")
    public void setExportType(final ExportType exportType) {
        this.exportType = exportType;
    }

    @SuppressWarnings("all")
    public void setDivision(final Division division) {
        this.division = division;
    }

    @SuppressWarnings("all")
    public void setCostSheetList(final List<CostSheet> costSheetList) {
        this.costSheetList = costSheetList;
    }

    @SuppressWarnings("all")
    public SparcLineSheetData() {
    }

    @SuppressWarnings("all")
    public SparcLineSheetData(final String seasonOid, final ExportType exportType, final Division division, final List<CostSheet> costSheetList) {
        this.seasonOid = seasonOid;
        this.exportType = exportType;
        this.division = division;
        this.costSheetList = costSheetList;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "SparcLineSheetData(seasonOid=" + this.getSeasonOid() + ", exportType=" + this.getExportType() + ", division=" + this.getDivision() + ", costSheetList=" + this.getCostSheetList() + ")";
    }
    //</editor-fold>
}
