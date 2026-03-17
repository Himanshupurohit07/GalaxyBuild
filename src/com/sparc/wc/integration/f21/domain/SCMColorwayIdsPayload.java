package com.sparc.wc.integration.f21.domain;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.sparc.wc.integration.f21.builders.ColorwayIdsPayloadBuilder;

/**
 * Payload for a set of Colorway Ids as per defined for SCM integration.
 * 
 * @author Acnovate
 */
public class SCMColorwayIdsPayload {
	
	private MetaContent meta;
	private DataContent data;
	
	/**
	 * Creates a builder instance to build a SCM ColorwayIds payload.
	 */
	public static ColorwayIdsPayloadBuilder newBuilder() {
		return new ColorwayIdsPayloadBuilder(new SCMColorwayIdsPayload());
	}
	
	private SCMColorwayIdsPayload() {
		meta = new MetaContent();
		data = new DataContent();
	}
	
	/**
	 * Payload for meta data content.
	 */
	public class MetaContent {
		
	}
	
	/**
	 * Payload for data content.
	 */
	public class DataContent {
		
		private String fromDate;
		private String toDate;
		
		private int totalRecords;
		
		private List<Long> sourcingConfigNumbers;

		public DataContent() {
			sourcingConfigNumbers = new ArrayList<Long>();
		}
		
		public int getTotalRecords() {
			return totalRecords;
		}

		public void setTotalRecords(int totalRecords) {
			this.totalRecords = totalRecords;
		}
		
		public String getFromDate() {
			return fromDate;
		}

		public void setFromDate(String fromDate) {
			this.fromDate = fromDate;
		}

		public String getToDate() {
			return toDate;
		}

		public void setToDate(String toDate) {
			this.toDate = toDate;
		}
		
		public List<Long> getSourcingConfigNumbers() {
			return sourcingConfigNumbers;
		}

		public void setSourcingConfigNumbers(List<Long> sourcingConfigNumbers) {
			this.sourcingConfigNumbers = sourcingConfigNumbers;
		}
		
		@Override
		public String toString() {
			return "SCMColorwayIds [fromDate=" + fromDate + ", toDate=" + toDate + ", sourceConfigIds=" + sourcingConfigNumbers
					+ "]";
		}
		
	}
	
	public MetaContent getMeta() {
		return meta;
	}

	public void setMeta(MetaContent meta) {
		this.meta = meta;
	}
	
	public DataContent getData() {
		return data;
	}
	
	public void setData(DataContent data) {
		this.data = data;
	}
	
	public String toJSON() {
		//return new Gson().toJson(this);
		return new GsonBuilder().serializeNulls().create().toJson(this);
	}

	@Override
	public String toString() {
		return "SCMColorwayIdsPayload [meta=" + meta + ", data=" + data + "]";
	}

}
