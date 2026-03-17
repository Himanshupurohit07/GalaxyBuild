package com.sparc.wc.integration.aero.builders;

import com.sparc.wc.integration.aero.domain.AeroCostingIdsPayload;
import com.sparc.wc.integration.aero.domain.AeroProcessesParam;
import com.sparc.wc.integration.aero.repository.AeroCostingRepository;

import wt.util.WTException;

/**
 * Builds a Aero Costing Ids payload for S4 & CAP integrations.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9646 Aero - Costing Interface"
 */
public class AeroCostingIdsPayloadBuilder {
	
	private AeroProcessesParam process;
	private AeroCostingIdsPayload costingIds;
	
	public AeroCostingIdsPayloadBuilder() {
		
	}
	
	/**
	 * Constructs an instance of this builder.
	 * @param costingIds The Aero Costing IDs payload to build.
	 */
	public AeroCostingIdsPayloadBuilder(AeroCostingIdsPayload costingIds) {
		this.costingIds = costingIds;
	}

	public AeroCostingIdsPayloadBuilder setProcess(AeroProcessesParam process) {
		this.process = process;
		return this;
	}
	
	/**
	 * Builds the list of sourcing config ids retrieved from PLM.
	 * @throws WTException If a flex api error occurs while retrieving sourcing configuration numbers from PLM.
	 */
	private void buildSourcingConfigNumberList() throws WTException {
		
		if (process == null) {
			return;
		}
		
		costingIds.setProcess(process.toString());
		
		costingIds.getSourcingConfigNumbers().addAll(AeroCostingRepository.findCostingSourcingConfigNumbers(process));
		
	}
	
	/**
	 * Builds the total record count of sourcing config ids retrieved from PLM.
	 */
	private void buildTotalRecords() {
		costingIds.setTotalRecords(costingIds.getSourcingConfigNumbers().size());
	}
	
	/**
	 * Builds the Aero Costing IDs payload.
	 * @return The Aero Costing IDs payload built.
	 * @throws WTException If a PLM api issue occurred during lookup for sourcing configurations numbers at PLM.
	 */
	public AeroCostingIdsPayload build() throws WTException {
		
		buildSourcingConfigNumberList();
		buildTotalRecords();
		
		return costingIds;
	}
}
