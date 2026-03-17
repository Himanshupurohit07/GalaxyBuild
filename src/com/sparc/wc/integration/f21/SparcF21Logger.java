package com.sparc.wc.integration.f21;

import com.sparc.wc.util.SparcLogger;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.F21_SCM_LOGGER_DEBUG_ENABLED;

/**
 * This is a wrapper to the original SparcLogger custom logging utility class written for the S4 integration.
 * Its purpose is to transparently update the "debug enabled" flag sourced from a Flex's Business Object 
 * during each instance pull, so to avoid the hassle of having to do so within any downstream classes & methods 
 * that make use of this logging utility.<br>
 * FIEX/AMENDMENTS:
 * -Deployment fix to setDebugEnabled. Removed call to original logic as it was failing and preventing the SC api to load properly on windchill startup.
 * The old logic was replaced by a properties to enable/diable debugging level.
 * 
 * @author Acnovate
 * @deprecated Use LogR!
 */

public class SparcF21Logger {
	
	private SparcF21Logger() {
		
	}
	
	/**
	 * Retrieves an instance of the <code>SparcLogger</code> custom logging class.
	 * @return The <code>SparcLogger</code> instance.
	 */
	public static SparcLogger getInstance() {
		
		SparcLogger logger = SparcLogger.getInstance();
		
		logger.setDebugEnabled("true".equalsIgnoreCase(F21_SCM_LOGGER_DEBUG_ENABLED));
		
		return logger;
	}
	
}
