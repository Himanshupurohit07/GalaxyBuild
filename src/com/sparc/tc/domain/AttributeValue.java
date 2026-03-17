package com.sparc.tc.domain;

import java.util.Map;

public class AttributeValue {
    public static final String CURRENCY_TYPE = "CURRENCY_TYPE";
    public static final String DECIMALS = "DECIMALS";
    public static final String DATE_FORMAT = "DATE_FORMAT";
    public static final String LIST_DELIMITER = "LD";


//<editor-fold defaultstate="collapsed" desc="delombok">
//</editor-fold>
    public enum Type {
        STRING, INTEGER, DECIMAL, BOOLEAN, DATE, CURRENCY, USE_CELL_TYPE;
    }

    private Object data;
    private Type type;
    private Map<String, Object> params;


    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public static class AttributeValueBuilder {
        @SuppressWarnings("all")
        private Object data;
        @SuppressWarnings("all")
        private Type type;
        @SuppressWarnings("all")
        private Map<String, Object> params;

        @SuppressWarnings("all")
        AttributeValueBuilder() {
        }

        @SuppressWarnings("all")
        public AttributeValue.AttributeValueBuilder data(final Object data) {
            this.data = data;
            return this;
        }

        @SuppressWarnings("all")
        public AttributeValue.AttributeValueBuilder type(final Type type) {
            this.type = type;
            return this;
        }

        @SuppressWarnings("all")
        public AttributeValue.AttributeValueBuilder params(final Map<String, Object> params) {
            this.params = params;
            return this;
        }

        @SuppressWarnings("all")
        public AttributeValue build() {
            return new AttributeValue(this.data, this.type, this.params);
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "AttributeValue.AttributeValueBuilder(data=" + this.data + ", type=" + this.type + ", params=" + this.params + ")";
        }
    }

    @SuppressWarnings("all")
    public static AttributeValue.AttributeValueBuilder builder() {
        return new AttributeValueBuilder();
    }

    @SuppressWarnings("all")
    public Object getData() {
        return this.data;
    }

    @SuppressWarnings("all")
    public Type getType() {
        return this.type;
    }

    @SuppressWarnings("all")
    public Map<String, Object> getParams() {
        return this.params;
    }

    @SuppressWarnings("all")
    public void setData(final Object data) {
        this.data = data;
    }

    @SuppressWarnings("all")
    public void setType(final Type type) {
        this.type = type;
    }

    @SuppressWarnings("all")
    public void setParams(final Map<String, Object> params) {
        this.params = params;
    }

    @SuppressWarnings("all")
    public AttributeValue() {
    }

    @SuppressWarnings("all")
    public AttributeValue(final Object data, final Type type, final Map<String, Object> params) {
        this.data = data;
        this.type = type;
        this.params = params;
    }
    //</editor-fold>
}
