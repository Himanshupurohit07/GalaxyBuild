package com.sparc.wc.integration.aero.domain;

/**
 * Log Entry model for Aero integrations.
 * Based on com.sparc.wc.integration.domain.SparcApiCallLogEntry
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development" 
 */
public class AeroApiCallLogEntry {
	
	public enum Status {
        SUCCESS("SUCCESS"), FAIL("FAIL"), SUCCESS_WARNINGS("SUCCESS-WARNINGS"), WARNING("WARNING");

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
	
	public enum ErrorTypes {
		ERROR("ERROR"),
        NOT_FOUND("NOT FOUND"),
        INVALID_INPUT_CRITERIA("INVALID INPUT CRITERIA"),
        INVALID_ATTR_COMBINATION("INVALID ATTR COMBINATION"),
        INVALID_ATTR_BLANK("INVALID ATTR (BLANK)"),
        INVALID_ATTR_LOCKED("INVALID ATTR (LOCKED)");

        private String errorType;

        ErrorTypes(final String errorType){
            this.errorType = errorType;
        }

        public String getErrorType(){
            return this.errorType;
        }

        public String toString(){
            return this.errorType;
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
    private ErrorTypes errorType;
    private String colorwayNumber;
    private String colorwaySeason;

	public AeroApiCallLogEntry() {
		
	}

	public String getFlexTypePath() {
		return flexTypePath;
	}

	public void setFlexTypePath(String flexTypePath) {
		this.flexTypePath = flexTypePath;
	}

	public String getApiCallType() {
		return apiCallType;
	}

	public void setApiCallType(String apiCallType) {
		this.apiCallType = apiCallType;
	}

	public Object getRequest() {
		return request;
	}

	public void setRequest(Object request) {
		this.request = request;
	}

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	public Long getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(Long requestTime) {
		this.requestTime = requestTime;
	}

	public Long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(Long responseTime) {
		this.responseTime = responseTime;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ErrorTypes getErrorType() {
		return errorType;
	}

	public void setErrorType(ErrorTypes errorType) {
		this.errorType = errorType;
	}

	public String getColorwayNumber() {
		return colorwayNumber;
	}

	public void setColorwayNumber(String colorwayNumber) {
		this.colorwayNumber = colorwayNumber;
	}

	public String getColorwaySeason() {
		return colorwaySeason;
	}

	public void setColorwaySeason(String colorwaySeason) {
		this.colorwaySeason = colorwaySeason;
	}
	
	@Override
	public String toString() {
		return "AeroApiCallLogEntry [flexTypePath=" + flexTypePath + ", apiCallType=" + apiCallType + ", request="
				+ request + ", response=" + response + ", requestTime=" + requestTime + ", responseTime=" + responseTime
				+ ", status=" + status + ", message=" + message + ", errorType=" + errorType + ", colorwayNumber="
				+ colorwayNumber + ", colorwaySeason=" + colorwaySeason + "]";
	}

	public static AeroApiCallLogEntryBuilder builder() {
        return new AeroApiCallLogEntryBuilder();
    }
	
	/**
	 * Aero Log Entry Builder.
	 */
	public static class AeroApiCallLogEntryBuilder {
        
		private String flexTypePath;
        private String apiCallType;
        private Object request;
        private Object response;
        private Long requestTime;
        private Long responseTime;
        private Status status;
        private String message;
        private ErrorTypes errorType;
        private String colorwayNumber;
        private String colorwaySeason;

        private AeroApiCallLogEntryBuilder() {
        }
        
        public AeroApiCallLogEntryBuilder flexTypePath(final String flexTypePath) {
            this.flexTypePath = flexTypePath;
            return this;
        }
        
        public AeroApiCallLogEntryBuilder apiCallType(final String apiCallType) {
            this.apiCallType = apiCallType;
            return this;
        }
        
        public AeroApiCallLogEntryBuilder request(final Object request) {
            this.request = request;
            return this;
        }
        
        public AeroApiCallLogEntryBuilder response(final Object response) {
            this.response = response;
            return this;
        }

        public AeroApiCallLogEntryBuilder requestTime(final Long requestTime) {
            this.requestTime = requestTime;
            return this;
        }

        public AeroApiCallLogEntryBuilder responseTime(final Long responseTime) {
            this.responseTime = responseTime;
            return this;
        }

        public AeroApiCallLogEntryBuilder status(final Status status) {
            this.status = status;
            return this;
        }

        public AeroApiCallLogEntryBuilder message(final String message) {
            this.message = message;
            return this;
        }
        
        public AeroApiCallLogEntryBuilder errorType(final ErrorTypes errorType) {
            this.errorType = errorType;
            return this;
        }
        
        public AeroApiCallLogEntryBuilder colorwayNumber(final String colorwayNumber) {
            this.colorwayNumber = colorwayNumber;
            return this;
        }
        
        public AeroApiCallLogEntryBuilder colorwaySeason(final String colorwaySeason) {
            this.colorwaySeason = colorwaySeason;
            return this;
        }
        
        public AeroApiCallLogEntryBuilder colorwaySeason(final String seasonType, final String seasonYear) {
        	if (seasonType != null && seasonYear != null) {
        		this.colorwaySeason = seasonType + "-" + seasonYear;
        	}
            return this;
        }
        
        public AeroApiCallLogEntry build() {
        	
        	AeroApiCallLogEntry logEntry = new AeroApiCallLogEntry();
        	logEntry.flexTypePath = flexTypePath;
        	logEntry.apiCallType = apiCallType;
        	logEntry.request = request;
        	logEntry.response = response;
        	logEntry.requestTime = requestTime;
        	logEntry.responseTime = responseTime;
        	logEntry.status = status;
        	logEntry.message = message;
        	logEntry.errorType = errorType;
        	logEntry.colorwayNumber = colorwayNumber;
        	logEntry.colorwaySeason = colorwaySeason;
        	
            return logEntry;
        }
        
    }

}
