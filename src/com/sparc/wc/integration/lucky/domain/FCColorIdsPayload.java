package com.sparc.wc.integration.lucky.domain;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.sparc.wc.integration.lucky.builders.FCColorsIdsPayloadBuilder;

/**
 * Payload for a set of Color Ids as per defined for Lucky/FC integration.
 * 
 * @author Acnovate
 */
public class FCColorIdsPayload {
	
	private MetaContent meta;
	private DataContent data;
	
	/**
	 * Creates a builder instance to build a FC ColorIds payload.
	 */
	public static FCColorsIdsPayloadBuilder newBuilder() {
		return new FCColorsIdsPayloadBuilder(new FCColorIdsPayload());
	}
	
	private FCColorIdsPayload() {
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
		
		private List<String> colorIds;
		
		public DataContent() {
			colorIds = new ArrayList<String>();
		}

		public int getTotalRecords() {
			return totalRecords;
		}

		public void setTotalRecords(int totalRecords) {
			this.totalRecords = totalRecords;
		}

		public List<String> getColorIds() {
			return colorIds;
		}

		public void setColorIds(List<String> colorIds) {
			this.colorIds = colorIds;
		}

		@Override
		public String toString() {
			return "DataContent [totalRecords=" + totalRecords + ", colorIds=" + colorIds + "]";
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
		return "LuckyFCColorsPayload [meta=" + meta + ", data=" + data + "]";
	}

}
