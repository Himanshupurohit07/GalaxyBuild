package com.sparc.wc.shareFile;

import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.specification.FlexSpecification;

public class SPARCShareFileAPICallLogEntry {

	private String flexTypePath;
	private String apiCallType;
	private Object request;
	private Object response;
	private Long requestTime;
	private Long responseTime;
	private Status status;
	private Long plmProductNo;
	private LCSSeason season;
	private FlexSpecification specification;
	private String partner;

	public enum Status {
		SUCCESS("200"), INTERNAL_ERROR("500"), BAD_REQUEST("400"), RESOURCE_NOT_FOUND("404"), UNAUTHORIZED("401");

		private String responseCode;

		Status(final String responseCode) {
			this.responseCode = responseCode;
		}

		public String getResponseCode() {
			return this.responseCode;
		}
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

	public Long getPlmProductNo() {
		return plmProductNo;
	}

	public void setPlmProductNo(Long plmProductNo) {
		this.plmProductNo = plmProductNo;
	}

	public LCSSeason getSeason() {
		return season;
	}

	public void setSeason(LCSSeason season) {
		this.season = season;
	}

	public FlexSpecification getSpecification() {
		return specification;
	}

	public void setSpecification(FlexSpecification specification) {
		this.specification = specification;
	}

	public String getPartner() {
		return partner;
	}

	public void setPartner(String partner) {
		this.partner = partner;
	}

	public static SPARCShareFileAPICallLogEntryBuilder builder() {
		return new SPARCShareFileAPICallLogEntryBuilder();
	}

	public static class SPARCShareFileAPICallLogEntryBuilder {
		private String flexTypePath;
		private String apiCallType;
		private Object request;
		private Object response;
		private Long requestTime;
		private Long responseTime;
		private Status status;
		private Long plmProductNo;
		private LCSSeason season;
		private FlexSpecification specification;
		private String partner;

		private SPARCShareFileAPICallLogEntryBuilder() {
			// private constructor
		}

		public SPARCShareFileAPICallLogEntryBuilder flexTypePath(String flexTypePath) {
			this.flexTypePath = flexTypePath;
			return this;
		}

		public SPARCShareFileAPICallLogEntryBuilder apiCallType(final String apiCallType) {
			this.apiCallType = apiCallType;
			return this;
		}

		public SPARCShareFileAPICallLogEntryBuilder request(final Object request) {
			this.request = request;
			return this;
		}

		public SPARCShareFileAPICallLogEntryBuilder response(final Object response) {
			this.response = response;
			return this;
		}

		public SPARCShareFileAPICallLogEntryBuilder requestTime(final Long requestTime) {
			this.requestTime = requestTime;
			return this;
		}

		public SPARCShareFileAPICallLogEntryBuilder responseTime(final Long responseTime) {
			this.responseTime = responseTime;
			return this;
		}

		public SPARCShareFileAPICallLogEntryBuilder status(final Status status) {
			this.status = status;
			return this;
		}

		public SPARCShareFileAPICallLogEntryBuilder plmProductNo(final Long plmProductNo) {
			this.plmProductNo = plmProductNo;
			return this;
		}

		public SPARCShareFileAPICallLogEntryBuilder season(final LCSSeason season) {
			this.season = season;
			return this;
		}

		public SPARCShareFileAPICallLogEntryBuilder specification(final FlexSpecification specification) {
			this.specification = specification;
			return this;
		}

		public SPARCShareFileAPICallLogEntryBuilder partner(final String partner) {
			this.partner = partner;
			return this;
		}

		public SPARCShareFileAPICallLogEntry build() {

			SPARCShareFileAPICallLogEntry logEntry = new SPARCShareFileAPICallLogEntry();
			logEntry.flexTypePath = flexTypePath;
			logEntry.apiCallType = apiCallType;
			logEntry.request = request;
			logEntry.response = response;
			logEntry.requestTime = requestTime;
			logEntry.responseTime = responseTime;
			logEntry.status = status;
			logEntry.plmProductNo = plmProductNo;
			logEntry.season = season;
			logEntry.specification = specification;
			logEntry.partner = partner;
			return logEntry;
		}
	}
}
