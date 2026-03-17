package com.sparc.wc.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTypeQuery;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSQuery;

import wt.util.WTException;

/*
 * This Logger utility get the logger object which can be 
 * used for logging purposes. 
 * 
 * In order to get the Logger object from this class the Syntax is
 * private static final Logger LOGGER = MSLogger.getLogger(<<ClassName>>.class);
 * 
 * The different types of log message are
 * 
 * LOGGER.Info -- Logs all the Info messages
 * LOGGER.Debug -- Logs all the Debug messages
 * LOGGER.Error -- Logs all the Error messages
 * 
 */


/**
 * This is an interface to initialize log and to configure client specific log
 * file using properties file.It gets the Logger object based on the log4j
 * properties file. initializeLogging gets the log4j properties file to
 * configure log4j.
 * 
 * @author Infosys
 */
@SuppressWarnings("rawtypes")
public class SparcLogger{
	private boolean isDebugEnabled;
private static SparcLogger logger;
	public void logInfo(String className, String methodName, String printMessage) {
		System.out.println(className +" " +methodName + " " + printMessage);
	}

	public void logDebug(String className, String methodName, String printMessage) {
		if(isDebugEnabled) {
			System.out.println(className +" " +methodName + " " + printMessage);
		}
	}

	public void logError(String className, String methodName, String printMessage) {
		System.out.println(className +" " +methodName + " " + printMessage);
	}
	
	public static SparcLogger getInstance() {
		if(logger == null) {
			logger = new SparcLogger();
			logger.logInfo(SparcLogger.class.getName(), "getInstance", " Logger created " + logger);
		}
		return logger;
	}
	
	public boolean isDebugEnabled() {
		return isDebugEnabled;
	}
	public void setDebugEnabled(boolean isDebugEnabled) {
		this.isDebugEnabled = isDebugEnabled;
	}
	
	private SparcLogger() {
		
	}
	public boolean getDebugValue(String loggerFrom) {
		try {
			FlexType businessObjectType = FlexTypeCache.getFlexTypeFromPath(SparcConstants.BUSINESS_OBJECT_LOG_CONTROL_PATH);
			FlexTypeQuery ftq = new FlexTypeQuery();
			Collection boColl  = new ArrayList();
			boColl = ftq.findAllObjectsTypedBy(businessObjectType).getResults();
			Iterator itr = boColl.iterator();
			while(itr.hasNext()) {
				FlexObject obj = (FlexObject)itr.next();
				LCSLifecycleManaged businessObject = (LCSLifecycleManaged)LCSQuery.findObjectById(SparcConstants.BUSINESS_OBJECT_OR_OBJECT+obj.getData(SparcConstants.BUSINESS_OBJECT_IDA2A2));
				String boName = businessObject.getName();
				if(loggerFrom.equalsIgnoreCase(boName)) {
					isDebugEnabled = (boolean)businessObject.getValue(SparcConstants.BUSINESS_OBJECT_LOG_VALUE);
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
	
		return isDebugEnabled;
	}
 	
}