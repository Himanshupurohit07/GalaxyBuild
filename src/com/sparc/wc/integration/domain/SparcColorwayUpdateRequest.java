package com.sparc.wc.integration.domain;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

public class SparcColorwayUpdateRequest {

    public static class Criteria {
        @NotNull
        private String scColorwayNo;
        @NotNull
        private String seasonType;
        @DecimalMin("1970")
        private int year;

        @SuppressWarnings("all")
        public String getScColorwayNo() {
            return this.scColorwayNo;
        }

        @SuppressWarnings("all")
        public String getSeasonType() {
            return this.seasonType;
        }

        @SuppressWarnings("all")
        public int getYear() {
            return this.year;
        }

        @SuppressWarnings("all")
        public void setScColorwayNo(final String scColorwayNo) {
            this.scColorwayNo = scColorwayNo;
        }

        @SuppressWarnings("all")
        public void setSeasonType(final String seasonType) {
            this.seasonType = seasonType;
        }

        @SuppressWarnings("all")
        public void setYear(final int year) {
            this.year = year;
        }

        @SuppressWarnings("all")
        public Criteria() {
        }
    }

    private Criteria criteria;
    private Map<String, Object> attrs = new HashMap<>();

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public Criteria getCriteria() {
        return this.criteria;
    }

    @SuppressWarnings("all")
    public Map<String, Object> getAttrs() {
        return this.attrs;
    }

    @SuppressWarnings("all")
    public void setCriteria(final Criteria criteria) {
        this.criteria = criteria;
    }

    @SuppressWarnings("all")
    public void setAttrs(final Map<String, Object> attrs) {
        this.attrs = attrs;
    }

    @SuppressWarnings("all")
    public SparcColorwayUpdateRequest() {
    }
    //</editor-fold>
}
