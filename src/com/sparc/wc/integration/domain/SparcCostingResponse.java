package com.sparc.wc.integration.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SparcCostingResponse {
    private String sourcingConfigNumber;
    private List<Map<String, Object>> costsheets;

    public void addCostsheet(final Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        if (costsheets == null) {
            costsheets = new LinkedList<>();
        }
        costsheets.add(data);
    }

    public SparcGenericResponse genericResponse() {
        return SparcGenericResponse.builder().data(this).build();
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public String getSourcingConfigNumber() {
        return this.sourcingConfigNumber;
    }

    @SuppressWarnings("all")
    public List<Map<String, Object>> getCostsheets() {
        return this.costsheets;
    }

    @SuppressWarnings("all")
    public void setSourcingConfigNumber(final String sourcingConfigNumber) {
        this.sourcingConfigNumber = sourcingConfigNumber;
    }

    @SuppressWarnings("all")
    public void setCostsheets(final List<Map<String, Object>> costsheets) {
        this.costsheets = costsheets;
    }

    @SuppressWarnings("all")
    public SparcCostingResponse() {
    }
    //</editor-fold>
}
