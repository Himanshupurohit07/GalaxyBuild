package com.sparc.wc.integration.domain;

import com.sparc.wc.integration.util.SparcIntegrationUtil;
import java.util.HashMap;
import java.util.Map;

public class SparcColorwayDetails {
    private String scColorwayNo;
    private Map<String, Object> style = new HashMap<>();
    private Map<String, Object> fc = new HashMap<>();
    private Map<String, Object> cap = new HashMap<>();

    public void addStyleParam(final String key, final Object value) {
        if (key != null && !key.isEmpty()) {
            this.style.put(key, value);
        }
    }

    public void addFcParam(final String key, final Object value) {
        if (key != null && !key.isEmpty()) {
            this.fc.put(key, value);
        }
    }

    public void addCapParam(final String key, final Object value) {
        if (key != null && !key.isEmpty()) {
            this.cap.put(key, value);
        }
    }

    public void setScColorwayNo(final String number) {
        if (number != null && !number.isEmpty()) {
            this.scColorwayNo = SparcIntegrationUtil.addPadding(number);
        }
    }

    public void setScColorwayNo(final long number) {
        if (number != -1) {
            final String clrNum = number + "";
            setScColorwayNo(clrNum);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public String getScColorwayNo() {
        return this.scColorwayNo;
    }

    @SuppressWarnings("all")
    public Map<String, Object> getStyle() {
        return this.style;
    }

    @SuppressWarnings("all")
    public Map<String, Object> getFc() {
        return this.fc;
    }

    @SuppressWarnings("all")
    public Map<String, Object> getCap() {
        return this.cap;
    }

    @SuppressWarnings("all")
    public void setStyle(final Map<String, Object> style) {
        this.style = style;
    }

    @SuppressWarnings("all")
    public void setFc(final Map<String, Object> fc) {
        this.fc = fc;
    }

    @SuppressWarnings("all")
    public void setCap(final Map<String, Object> cap) {
        this.cap = cap;
    }

    @SuppressWarnings("all")
    public SparcColorwayDetails() {
    }
    //</editor-fold>
}
