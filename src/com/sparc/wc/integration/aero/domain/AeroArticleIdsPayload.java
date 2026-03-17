package com.sparc.wc.integration.aero.domain;

import java.util.ArrayList;
import java.util.List;

import com.sparc.wc.integration.aero.builders.AeroArticleIdsPayloadBuilder;

/**
 * Payload for a set of Colorway Numbers related to Aeropostale Article integration to S4 and CAP.
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 */
public class AeroArticleIdsPayload {

	private int totalRecords;
	private String process;
	private List<String> colorwayNumbers;
	
	/**
	 * Creates a builder instance to build a Aero Article Ids payload.
	 */
	public static AeroArticleIdsPayloadBuilder newBuilder() {
		return new AeroArticleIdsPayloadBuilder(new AeroArticleIdsPayload());
	}
	
	public AeroArticleIdsPayload() {
		colorwayNumbers = new ArrayList<String>();
	}

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public List<String> getColorwayNumbers() {
		return colorwayNumbers;
	}

	@Override
	public String toString() {
		return "AeroArticleIdsPayload [totalRecords=" + totalRecords + ", process=" + process
				+ ", colorwayNumbers=" + colorwayNumbers + "]";
	}

}
