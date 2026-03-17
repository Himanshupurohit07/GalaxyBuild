package com.sparc.wc.integration.aero.builders;

import com.sparc.wc.integration.aero.domain.AeroCostingDetailsPayload;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * Defines the base layout for a Costing Details payloads builder.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9646 Aero - Costing Interface"
 */
public interface AeroCostingDetailsPayloadBuilder {
	
	public AeroCostingDetailsPayloadBuilder setSourcingConfigNumber(Long sourcingConfigNumber);
	
	/**
	 * Builds the Costing Details payload.
	 * @return The Costing Details payload built.
	 * @throws WTException When an error occurs while accessing or retrieving the various data elements from PLM.
	 * @throws WTPropertyVetoException When an error occurs while updating the control flags at PLM.
	 * @throws Exception When a different error, i.e. validation fail, occurs.
	 */
	public AeroCostingDetailsPayload build() throws WTException, WTPropertyVetoException, Exception;
	
}
