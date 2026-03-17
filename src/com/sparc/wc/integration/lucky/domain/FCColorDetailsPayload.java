package com.sparc.wc.integration.lucky.domain;

import com.google.gson.GsonBuilder;
import com.sparc.wc.integration.lucky.builders.FCColorDetailsPayloadBuilder;

/**
 * Payload for a Color detail data as per defined for Lucky/FC integration.
 * 
 * @author Acnovate
 */
public class FCColorDetailsPayload {
	
	private MetaContent meta;
	private DataContent data;
	
	/**
	 * Creates a builder instance to build a FC Color details payload.
	 */
	public static FCColorDetailsPayloadBuilder newBuilder() {
		return new FCColorDetailsPayloadBuilder(new FCColorDetailsPayload());
	}
	
	public FCColorDetailsPayload() {
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
		
		private String sclCLR_CDE;
		private String sclCLR_DESC;
		private String sclCLR_CATCLR;
		private String sclNewcolorFlag;
		
		public DataContent() {
			
		}

		public String getSclCLR_CDE() {
			return sclCLR_CDE;
		}

		public void setSclCLR_CDE(String sclCLR_CDE) {
			this.sclCLR_CDE = sclCLR_CDE;
		}

		public String getSclCLR_DESC() {
			return sclCLR_DESC;
		}

		public void setSclCLR_DESC(String sclCLR_DESC) {
			this.sclCLR_DESC = sclCLR_DESC;
		}

		public String getSclCLR_CATCLR() {
			return sclCLR_CATCLR;
		}

		public void setSclCLR_CATCLR(String sclCLR_CATCLR) {
			this.sclCLR_CATCLR = sclCLR_CATCLR;
		}

		public String getSclNewcolorFlag() {
			return sclNewcolorFlag;
		}

		public void setSclNewcolorFlag(String sclNewcolorFlag) {
			this.sclNewcolorFlag = sclNewcolorFlag;
		}

		@Override
		public String toString() {
			return "DataContent [sclCLR_CDE=" + sclCLR_CDE + ", sclCLR_DESC=" + sclCLR_DESC + ", sclCLR_CATCLR="
					+ sclCLR_CATCLR + ", sclNewcolorFlag=" + sclNewcolorFlag + "]";
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
		return "FCColorDetailsPayload [meta=" + meta + ", data=" + data + "]";
	}

}
