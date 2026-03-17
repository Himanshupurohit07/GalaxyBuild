package com.sparc.wc.integration.domain;

public class SparcGenericResponse {
    protected Object meta;
    protected Object data;


    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public static class SparcGenericResponseBuilder {
        @SuppressWarnings("all")
        private Object meta;
        @SuppressWarnings("all")
        private Object data;

        @SuppressWarnings("all")
        SparcGenericResponseBuilder() {
        }

        @SuppressWarnings("all")
        public SparcGenericResponse.SparcGenericResponseBuilder meta(final Object meta) {
            this.meta = meta;
            return this;
        }

        @SuppressWarnings("all")
        public SparcGenericResponse.SparcGenericResponseBuilder data(final Object data) {
            this.data = data;
            return this;
        }

        @SuppressWarnings("all")
        public SparcGenericResponse build() {
            return new SparcGenericResponse(this.meta, this.data);
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "SparcGenericResponse.SparcGenericResponseBuilder(meta=" + this.meta + ", data=" + this.data + ")";
        }
    }

    @SuppressWarnings("all")
    public static SparcGenericResponse.SparcGenericResponseBuilder builder() {
        return new SparcGenericResponse.SparcGenericResponseBuilder();
    }

    @SuppressWarnings("all")
    public Object getMeta() {
        return this.meta;
    }

    @SuppressWarnings("all")
    public Object getData() {
        return this.data;
    }

    @SuppressWarnings("all")
    public void setMeta(final Object meta) {
        this.meta = meta;
    }

    @SuppressWarnings("all")
    public void setData(final Object data) {
        this.data = data;
    }

    @SuppressWarnings("all")
    public SparcGenericResponse() {
    }

    @SuppressWarnings("all")
    public SparcGenericResponse(final Object meta, final Object data) {
        this.meta = meta;
        this.data = data;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "SparcGenericResponse(meta=" + this.getMeta() + ", data=" + this.getData() + ")";
    }
    //</editor-fold>
}
