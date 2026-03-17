package com.sparc.wc.integration.lucky.builders;

import com.sparc.wc.integration.lucky.domain.LuckyCostingIdsPayload;
import com.sparc.wc.integration.lucky.domain.LuckyCostingProcessesParam;
import com.sparc.wc.integration.lucky.repository.LuckyCostingRepository;

import wt.util.WTException;

/**
 * Builds a Lucky Costing Ids payload for CAP & FC integration.<br>
 * 
 * @author Acnovate
 * @see Task #8259 3.1 Costing API - FC & CAP (Outbound)
 */
public class LuckyCostingIdsPayloadBuilder {
	
	private LuckyCostingProcessesParam process;
	private LuckyCostingIdsPayload costingIds;
	
	/**
	 * Constructs a LuckyCostingIdsPayloadBuilder instance.
	 * @param costingIds The Lucky Costing IDs payload to build.
	 */
	public LuckyCostingIdsPayloadBuilder(LuckyCostingIdsPayload costingIds) {
		this.costingIds = costingIds;
	}
	
	public LuckyCostingIdsPayloadBuilder setProcess(LuckyCostingProcessesParam process) {
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
		
		costingIds.getData().getSourcingConfigNumbers().addAll(LuckyCostingRepository.findCostingSourcingConfigNumbers(process));
		
	}
	
	/**
	 * Builds the total record count of sourcing config ids retrieved from PLM.
	 */
	private void buildTotalRecords() {
		costingIds.getData().setTotalRecords(costingIds.getData().getSourcingConfigNumbers().size());
	}
	
	/**
	 * Builds the Lucky Costing IDs payload.
	 * @return The Lucky Costing IDs payload built.
	 * @throws WTException If a PLM api issue occurred during lookup for sourcing configurations numbers at PLM.
	 */
	public LuckyCostingIdsPayload build() throws WTException {
		
		buildSourcingConfigNumberList();
		buildTotalRecords();
		
		return costingIds;
	}

}
