package com.sparc.wc.integration.lucky.builders;

import com.sparc.wc.integration.lucky.domain.FCColorIdsPayload;
import com.sparc.wc.integration.lucky.repository.FCColorRepository;

/**
 * Builds a Lucky/FC Color Ids payload.<br>
 * 
 * @author Acnovate
 */
public class FCColorsIdsPayloadBuilder {
	
	private FCColorIdsPayload colorIds;

	public FCColorsIdsPayloadBuilder(FCColorIdsPayload colorIds) {
		this.colorIds = colorIds;
	}
	
	/**
	 * Builds the list of FC color ids from PLM.
	 */
	private void buildColorIdsList() throws Exception {
		
		colorIds.getData().getColorIds().addAll(
				FCColorRepository.findColorIds());
		
	}
	
	/**
	 * Builds the total record count of colorway ids retrieved from PLM.
	 */
	private void buildTotalRecords() {
		colorIds.getData().setTotalRecords(colorIds.getData().getColorIds().size());
	}
	
	public FCColorIdsPayload build() throws Exception {
		
		buildColorIdsList();
		buildTotalRecords();
		
		return colorIds;
	}

}
