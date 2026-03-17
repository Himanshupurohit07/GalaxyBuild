package com.sparc.wc.integration.aero.domain;

/**
 * Exception to handle Aero Plugin catch-able execution conditions.
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development" 
 */
public class AeroCustomPluginException extends Exception {
	
	private static final long serialVersionUID = -1258543667977320452L;

	public AeroCustomPluginException() {
		
	}

	public AeroCustomPluginException(String message) {
		super(message);
	}

	public AeroCustomPluginException(Throwable cause) {
		super(cause);
	}

	public AeroCustomPluginException(String message, Throwable cause) {
		super(message, cause);
	}

	public AeroCustomPluginException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
