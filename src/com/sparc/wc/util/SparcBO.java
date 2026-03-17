package com.sparc.wc.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSLogEntry;
import com.lcs.wc.foundation.LCSLogEntryQuery;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.util.LCSException;

import wt.fc.WTObject;
import wt.util.WTException;

/**
 * Custom plugin used for deletion of Log Entries through Update of Business Object\Logs Control Delete Log Entry.<br>
 * FIXES/AMENDMENTS (By Acnovate):<br>
 * - Task #10137 (Sparc-Integrations): Enhanced existing logic to expand the selection of the log entry type to delete (related to other api integrations).<br>
 * - Task #10276 (UAT): Enhance integration log entry functionality to ensure each brand's logs can be deleted for a specific date range per deferred functionality in Aero integration spec.<br>
 * 
 * @author Infosys
 * @author Acnovate (enhancements).
 */
public class SparcBO {
	
	static SparcLogger logger = SparcLogger.getInstance();
	
	private static final String DELETE_TYPE_OLDER_THAN_SIX_MONTHS = "scdeleteSix";
	private static final String DELETE_TYPE_ALL = "scDeleteAll";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void deleteIntegrationLogs(WTObject wtobj) throws LCSException {
		
		String logEntryID = null;
		String methodName = "deleteIntegrationLogs";
		if(logger != null) {
			logger.getDebugValue(SparcConstants.INTEGRATION_DEBUG_CALL);
		}
		logger.logInfo(SparcBO.class.getName(), methodName, " Start");
		LCSLifecycleManaged BO = (LCSLifecycleManaged)wtobj;
		if("Delete Log Entry".equalsIgnoreCase(BO.getName())){
			try{
				Map criteria = new HashMap();
				
				Date selectedStartDate = (Date)BO.getValue("scStartDate");
				Date selectedEndDate = (Date)BO.getValue("scEndDate");
				String boDeleteType = (String)BO.getValue("scDeleteType");
				String selectedLogEntry = (String)BO.getValue("scTargetLogEntry");
				
				logger.logInfo(SparcBO.class.getName(), methodName, " Selected Start Date: " + selectedStartDate + "; Selected End Date: " + selectedEndDate);
				logger.logInfo(SparcBO.class.getName(), methodName, " Delete Type " + boDeleteType);
				logger.logInfo(SparcBO.class.getName(), methodName, " Log Entry selected to delete is: '" + selectedLogEntry + "'.");
				
				if (selectedLogEntry == null || selectedLogEntry.isBlank()) {
					throw new LCSException("Requested action is unable to complete. No Target Log Entry has been selected.");
				}
				
				//Prioritize selection of start and end dates range, before the selection of delete type.
				if (selectedStartDate != null || selectedEndDate != null) {
					
					if (selectedStartDate == null) {
						throw new LCSException("Requested action is unable to complete. No Start Date has been selected.");
					} else if (selectedEndDate == null) {
						throw new LCSException("Requested action is unable to complete. No End Date has been selected.");
					} else if (selectedStartDate.after(selectedEndDate)) {
						throw new LCSException("Requested action is unable to complete. Start Date selected is after the End Date selected.");
					}
					
					criteria.put("LCSLOGENTRY_CREATESTAMPA2FromDateString", selectedStartDate.toString());
					criteria.put("LCSLOGENTRY_CREATESTAMPA2ToDateString", selectedEndDate.toString());
					
					logger.logInfo(SparcBO.class.getName(), 
							methodName, 
							"Selected to delete log entries records for " + selectedLogEntry + " between dates " + selectedStartDate.toString() + " and " + selectedEndDate);
					
				} else if (DELETE_TYPE_OLDER_THAN_SIX_MONTHS.equalsIgnoreCase(boDeleteType)) {
					Date date = new Date();
					Calendar c = Calendar.getInstance(); 
					c.setTime(date); 
					c.add(Calendar.MONTH, -6);
					//criteria.put("LCSLOGENTRY_CREATESTAMPA2FromDateString",c.getTime().toString());
					//criteria.put("LCSLOGENTRY_CREATESTAMPA2ToDateString", date.toString());
					criteria.put("LCSLOGENTRY_CREATESTAMPA2ToDateString", c.getTime().toString());
					logger.logInfo(SparcBO.class.getName(), methodName, "Selected to delete log entries records for " + selectedLogEntry + " that are older than date: " + date.toString());
					//logger.logInfo(SparcBO.class.getName(), methodName, " From Date "+c.getTime().toString());
					//criteria.put("relevantActivity", "FIND_LOGENTRY");
				} else if (DELETE_TYPE_ALL.equalsIgnoreCase(boDeleteType)) {
					logger.logInfo(SparcBO.class.getName(), methodName, "WARNING: All log entry records for " + selectedLogEntry + " will be deleted!");
				} else {
					throw new LCSException("Requested action is unable to complete. No valid delete action type has been selected.");
				}
					
				Collection oidList = null;
				Collection attList = new ArrayList();
				
				FlexType logFlexType = FlexTypeCache.getFlexTypeFromPath("Log Entry\\" + selectedLogEntry);
				LCSLogEntryQuery query = new LCSLogEntryQuery();
				
				SearchResults results = query.findLogEntriesByCriteria(criteria, logFlexType, attList, null, oidList);
				
				int logCount = 0;
				int resultsBatchCount = 1;
				int totalResults = results.getResultsFound();
				int deletedCount = 0;
				
				logger.logInfo(SparcBO.class.getName(), methodName, " Search Results size is "+ totalResults);
				
				//Note: Only a fixed amount of log entry records are being retrieved by this flex log entry query api (currently 50), 
				//if this number is lower than the total available records found by the query, then the query will be executed multiple times in order to delete all records.
				while (results != null && results.getResultsFound() > 0) {
					
					logCount = 0;
					
					Iterator logEntries = results.getResults().iterator();
					
					while(logEntries.hasNext()) {
						FlexObject fobj = (FlexObject)logEntries.next();
						logEntryID = fobj.getData("LCSLOGENTRY.IDA2A2");
						LCSLogEntry logEntry = (LCSLogEntry)LCSQuery.findObjectById("OR:com.lcs.wc.foundation.LCSLogEntry:" + logEntryID);
						//logger.logInfo(SparcBO.class.getName(), methodName, " Log Entry name is "+logEntry.getValue("scName"));
						LCSLogic.deleteObject(logEntry);
						logCount += 1;
					}
					
					deletedCount += logCount; 
					
					logger.logInfo(SparcBO.class.getName(), 
							methodName, 
							"[Batch #" +  resultsBatchCount + "] Deleted "+ logCount + " log entry records for '" + selectedLogEntry + "'. Total log entry records deleted so far: " + deletedCount + "). There are " + (totalResults - deletedCount) + " out of " + totalResults + " records remaining.");
					
					//Run the query again to fetch the next results batch.
					results = query.findLogEntriesByCriteria(criteria, logFlexType, attList, null, oidList);
					resultsBatchCount += 1;
				}
				
			} catch(LCSException lcsEx) {
				logger.logError(SparcBO.class.getName(), methodName, " LCSException is "+lcsEx);
				throw lcsEx;
			} catch(WTException e) {
				logger.logInfo(SparcBO.class.getName(), methodName, " Last log entry ID found before error " + logEntryID + ".");
				logger.logError(SparcBO.class.getName(), methodName, " Exception is "+e);
				e.printStackTrace();
			}
		}
		logger.logInfo(SparcBO.class.getName(), methodName, " End");
	}
}