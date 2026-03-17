package com.sparc.wc.integration.aero.builders;

import com.sparc.wc.integration.aero.domain.AeroArticleIdsPayload;
import com.sparc.wc.integration.aero.domain.AeroProcessesParam;
import com.sparc.wc.integration.aero.repository.AeroArticleRepository;

import wt.util.WTException;

/**
 * Builds a Aero Article Ids payload for S4 & CAP integrations.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9645 Aero - Article Interface"
 */
public class AeroArticleIdsPayloadBuilder {
	
	private AeroProcessesParam process;
	private AeroArticleIdsPayload articleIds;
	
	/**
	 * Constructs a AeroArticleIdsPayloadBuilder instance.
	 * @param articleIds The Aero Article IDs payload to build.
	 */
	public AeroArticleIdsPayloadBuilder(AeroArticleIdsPayload articleIds) {
		this.articleIds = articleIds;
	}

	public AeroArticleIdsPayloadBuilder setProcess(AeroProcessesParam process) {
		this.process = process;
		return this;
	}
	
	/**
	 * Builds the list of colorway numbers retrieved from PLM.
	 * @throws WTException If a flex api error occurs while retrieving colorway numbers from PLM.
	 */
	private void buildColorwayNumberList() throws WTException {
		
		if (process == null) {
			return;
		}
		
		articleIds.setProcess(process.toString());
		
		articleIds.getColorwayNumbers().addAll(AeroArticleRepository.findArticleColorwayNumbers(process));
		
	}
	
	/**
	 * Builds the total record count of colorway numbers retrieved from PLM.
	 */
	private void buildTotalRecords() {
		articleIds.setTotalRecords(articleIds.getColorwayNumbers().size());
	}
	
	/**
	 * Builds the Aero Article IDs payload.
	 * @return The Aero Article IDs payload built.
	 * @throws WTException If a PLM api issue occurred during lookup for colorway numbers at PLM.
	 */
	public AeroArticleIdsPayload build() throws WTException {
		
		buildColorwayNumberList();
		buildTotalRecords();
		
		return articleIds;
	}

}
