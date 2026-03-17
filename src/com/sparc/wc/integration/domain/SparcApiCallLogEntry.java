package com.sparc.wc.integration.domain;

public class SparcApiCallLogEntry {

    public enum Status {
        SUCCESS("SUCCESS"), FAIL("FAIL"), SUCCESS_WARNINGS("SUCCESS-WARNINGS");

        private String status;

        Status(final String status){
            this.status = status;
        }

        public String getStatus(){
            return this.status;
        }

        public String toString(){
            return this.status;
        }
    }

    private String flexTypePath;
    private String apiCallType;
    private Object request;
    private Object response;
    private Long requestTime;
    private Long responseTime;
    private Status status;
    private String message;


    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public static class SparcApiCallLogEntryBuilder {
        @SuppressWarnings("all")
        private String flexTypePath;
        @SuppressWarnings("all")
        private String apiCallType;
        @SuppressWarnings("all")
        private Object request;
        @SuppressWarnings("all")
        private Object response;
        @SuppressWarnings("all")
        private Long requestTime;
        @SuppressWarnings("all")
        private Long responseTime;
        @SuppressWarnings("all")
        private Status status;
        @SuppressWarnings("all")
        private String message;

        @SuppressWarnings("all")
        SparcApiCallLogEntryBuilder() {
        }

        @SuppressWarnings("all")
        public SparcApiCallLogEntry.SparcApiCallLogEntryBuilder flexTypePath(final String flexTypePath) {
            this.flexTypePath = flexTypePath;
            return this;
        }

        @SuppressWarnings("all")
        public SparcApiCallLogEntry.SparcApiCallLogEntryBuilder apiCallType(final String apiCallType) {
            this.apiCallType = apiCallType;
            return this;
        }

        @SuppressWarnings("all")
        public SparcApiCallLogEntry.SparcApiCallLogEntryBuilder request(final Object request) {
            this.request = request;
            return this;
        }

        @SuppressWarnings("all")
        public SparcApiCallLogEntry.SparcApiCallLogEntryBuilder response(final Object response) {
            this.response = response;
            return this;
        }

        @SuppressWarnings("all")
        public SparcApiCallLogEntry.SparcApiCallLogEntryBuilder requestTime(final Long requestTime) {
            this.requestTime = requestTime;
            return this;
        }

        @SuppressWarnings("all")
        public SparcApiCallLogEntry.SparcApiCallLogEntryBuilder responseTime(final Long responseTime) {
            this.responseTime = responseTime;
            return this;
        }

        @SuppressWarnings("all")
        public SparcApiCallLogEntry.SparcApiCallLogEntryBuilder status(final Status status) {
            this.status = status;
            return this;
        }

        @SuppressWarnings("all")
        public SparcApiCallLogEntry.SparcApiCallLogEntryBuilder message(final String message) {
            this.message = message;
            return this;
        }

        @SuppressWarnings("all")
        public SparcApiCallLogEntry build() {
            return new SparcApiCallLogEntry(this.flexTypePath, this.apiCallType, this.request, this.response, this.requestTime, this.responseTime, this.status, this.message);
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "SparcApiCallLogEntry.SparcApiCallLogEntryBuilder(flexTypePath=" + this.flexTypePath + ", apiCallType=" + this.apiCallType + ", request=" + this.request + ", response=" + this.response + ", requestTime=" + this.requestTime + ", responseTime=" + this.responseTime + ", status=" + this.status + ", message=" + this.message + ")";
        }
    }

    @SuppressWarnings("all")
    public static SparcApiCallLogEntry.SparcApiCallLogEntryBuilder builder() {
        return new SparcApiCallLogEntry.SparcApiCallLogEntryBuilder();
    }

    @SuppressWarnings("all")
    public String getFlexTypePath() {
        return this.flexTypePath;
    }

    @SuppressWarnings("all")
    public String getApiCallType() {
        return this.apiCallType;
    }

    @SuppressWarnings("all")
    public Object getRequest() {
        return this.request;
    }

    @SuppressWarnings("all")
    public Object getResponse() {
        return this.response;
    }

    @SuppressWarnings("all")
    public Long getRequestTime() {
        return this.requestTime;
    }

    @SuppressWarnings("all")
    public Long getResponseTime() {
        return this.responseTime;
    }

    @SuppressWarnings("all")
    public Status getStatus() {
        return this.status;
    }

    @SuppressWarnings("all")
    public String getMessage() {
        return this.message;
    }

    @SuppressWarnings("all")
    public void setFlexTypePath(final String flexTypePath) {
        this.flexTypePath = flexTypePath;
    }

    @SuppressWarnings("all")
    public void setApiCallType(final String apiCallType) {
        this.apiCallType = apiCallType;
    }

    @SuppressWarnings("all")
    public void setRequest(final Object request) {
        this.request = request;
    }

    @SuppressWarnings("all")
    public void setResponse(final Object response) {
        this.response = response;
    }

    @SuppressWarnings("all")
    public void setRequestTime(final Long requestTime) {
        this.requestTime = requestTime;
    }

    @SuppressWarnings("all")
    public void setResponseTime(final Long responseTime) {
        this.responseTime = responseTime;
    }

    @SuppressWarnings("all")
    public void setStatus(final Status status) {
        this.status = status;
    }

    @SuppressWarnings("all")
    public void setMessage(final String message) {
        this.message = message;
    }

    @SuppressWarnings("all")
    public SparcApiCallLogEntry() {
    }

    @SuppressWarnings("all")
    public SparcApiCallLogEntry(final String flexTypePath, final String apiCallType, final Object request, final Object response, final Long requestTime, final Long responseTime, final Status status, final String message) {
        this.flexTypePath = flexTypePath;
        this.apiCallType = apiCallType;
        this.request = request;
        this.response = response;
        this.requestTime = requestTime;
        this.responseTime = responseTime;
        this.status = status;
        this.message = message;
    }
    //</editor-fold>
}
