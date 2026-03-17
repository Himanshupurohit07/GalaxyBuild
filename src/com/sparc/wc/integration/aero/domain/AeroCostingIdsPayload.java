package com.sparc.wc.integration.aero.domain;

import java.util.ArrayList;
import java.util.List;

import com.sparc.wc.integration.aero.builders.AeroCostingIdsPayloadBuilder;

/**
 * Payload for a set of Sourcing Config Numbers eligible to Aeropostale Costing integration to S4 and CAP.
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9646 Aero - Costing Interface"
 */
public class AeroCostingIdsPayload {
	
	private int totalRecords;
	private String process;
	private List<String> sourcingConfigNumbers;
	
	public AeroCostingIdsPayload() {
		sourcingConfigNumbers = new ArrayList<String>();
	}
	
	/**
	 * Creates a builder instance to build a Aero Article Ids payload.
	 */
	public static AeroCostingIdsPayloadBuilder newBuilder() {
		return new AeroCostingIdsPayloadBuilder(new AeroCostingIdsPayload());
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

	public List<String> getSourcingConfigNumbers() {
		return sourcingConfigNumbers;
	}

	public void setSourcingConfigNumbers(List<String> sourcingConfigNumbers) {
		if (sourcingConfigNumbers != null) {
			this.sourcingConfigNumbers.addAll(sourcingConfigNumbers);
		}
	}

	@Override
	public String toString() {
		return "AeroCostingIdsPayload [totalRecords=" + totalRecords + ", process=" + process
				+ ", sourcingConfigNumbers=" + sourcingConfigNumbers + "]";
	}
	
}
