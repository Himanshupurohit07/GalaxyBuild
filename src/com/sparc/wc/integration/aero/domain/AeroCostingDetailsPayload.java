package com.sparc.wc.integration.aero.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sparc.wc.integration.aero.builders.AeroCAPCostingDetailsPayloadBuilder;
import com.sparc.wc.integration.aero.builders.AeroCostingDetailsPayloadBuilder;
import com.sparc.wc.integration.aero.builders.AeroS4CostingDetailsPayloadBuilder;

/**
 * Payload for Costing details for Aeropostale Costing integration to S4 and CAP.
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9646 Aero - Costing Interface"
 */
public class AeroCostingDetailsPayload {
	
	private String sourcingConfigNumber;
	private List<Map<String, Object>> costsheets;
	
	/**
	 * Creates a builder instance to build a Aero Costing payload for CAP or S4.
	 */
	public static AeroCostingDetailsPayloadBuilder newBuilder(AeroProcessesParam process) {
		
		AeroCostingDetailsPayloadBuilder instance = null;
		
		if (AeroProcessesParam.S4 == process) {
			instance = new AeroS4CostingDetailsPayloadBuilder(new AeroCostingDetailsPayload());
		} else if (AeroProcessesParam.CAP == process) {
			instance = new AeroCAPCostingDetailsPayloadBuilder(new AeroCostingDetailsPayload());
		}
		
		return instance;
	}
	
	public AeroCostingDetailsPayload() {
		costsheets = new ArrayList<>();
	}
	
	public String getSourcingConfigNumber() {
		return sourcingConfigNumber;
	}

	public void setSourcingConfigNumber(String sourcingConfigNumber) {
		this.sourcingConfigNumber = sourcingConfigNumber;
	}

	public List<Map<String, Object>> getCostsheets() {
		return costsheets;
	}

	public void setCostsheets(List<Map<String, Object>> costsheets) {
		if(costsheets != null) {
			this.costsheets.addAll(costsheets);
		}
	}
	
	public void addCostSheet(Map<String, Object> costsheet) {
		this.costsheets.add(costsheet);
	}

	@Override
	public String toString() {
		return "AeroCostingDetailsPayload [sourcingConfigNumber=" + sourcingConfigNumber + ", costsheets=" + costsheets
				+ "]";
	}
	
}
