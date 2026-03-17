package com.sparc.wc.integration.lucky.domain;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.sparc.wc.integration.lucky.builders.LuckyCostingIdsPayloadBuilder;

/**
 * Payload for a set of Sourcing Config Ids for related to Lucky Costing for FC and CAP.
 * 
 * @author Acnovate
 * @see Task #8259 3.1 Costing API - FC & CAP (Outbound)
 */
public class LuckyCostingIdsPayload {
	
	private MetaContent meta;
	private DataContent data;
	
	/**
	 * Creates a builder instance to build a Lucky Costing Ids payload.
	 */
	public static LuckyCostingIdsPayloadBuilder newBuilder() {
		return new LuckyCostingIdsPayloadBuilder(new LuckyCostingIdsPayload());
	}
	
	private LuckyCostingIdsPayload() {
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
		
		private int totalRecords;
		private List<String> sourcingConfigNumbers;
		
		public DataContent() {
			sourcingConfigNumbers = new ArrayList<String>();
		}
		
		public int getTotalRecords() {
			return totalRecords;
		}

		public void setTotalRecords(int totalRecords) {
			this.totalRecords = totalRecords;
		}
		
		public List<String> getSourcingConfigNumbers() {
			return sourcingConfigNumbers;
		}

		public void setSourcingConfigNumbers(List<String> sourcingConfigNumbers) {
			this.sourcingConfigNumbers = sourcingConfigNumbers;
		}

		@Override
		public String toString() {
			return "DataContent [totalRecords=" + totalRecords + ", sourcingConfigNumbers=" + sourcingConfigNumbers
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
		return new GsonBuilder().serializeNulls().create().toJson(this);
	}

	@Override
	public String toString() {
		return "LuckyCostingIdsPayload [meta=" + meta + ", data=" + data + "]";
	}
	
}
