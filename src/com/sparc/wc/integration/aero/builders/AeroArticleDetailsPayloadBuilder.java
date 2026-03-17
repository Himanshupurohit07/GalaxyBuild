package com.sparc.wc.integration.aero.builders;

import com.sparc.wc.integration.aero.domain.AeroArticleDetailsPayload;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * Defines the base layout for an Article Details payloads builder.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9645 Aero - Article Interface"
 */
public interface AeroArticleDetailsPayloadBuilder {
	
	public AeroArticleDetailsPayloadBuilder setColorwayNumber(Long colorwayNumber);
	
	/**
	 * Builds the Article Details payload.
	 * @return The list of Article Details payload built.
	 * @throws WTException When an error occurs while accessing or retrieving the various data elements from PLM.
	 * @throws WTPropertyVetoException When an error occurs while updating the control flags at PLM.
	 * @throws Exception When a different error, i.e. validation fail, occurs.
	 */
	public AeroArticleDetailsPayload build() throws WTException, WTPropertyVetoException, Exception;
	
}
