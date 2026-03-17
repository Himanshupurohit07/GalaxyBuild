package com.sparc.wc.integration.aero.loaders;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * Defines the base model for a Payload Inbound Loader.<br>
 * @param <P> Represents the object to use as payload to load - where data is contained.
 * @param <R> Represents the object to use as output/result of the loading of the payload.
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9647 Aero - Colorway Inbound"
 */
public interface AeroInboundPayloadLoader<P, R> {
	
	public AeroInboundPayloadLoader<P, R> setPayload(P requestPayload);
	
	/**
	 * Loads the payload into PLM.
	 * @return The info on the updates performed.
	 * @throws WTException When an error occurs while accessing or retrieving the various data elements from PLM.
	 * @throws WTPropertyVetoException When an error occurs while updating the control flags at PLM.
	 * @throws Exception When a different error, i.e. validation fail, occurs.
	 */
	public R load() throws WTException, WTPropertyVetoException, Exception;

}
