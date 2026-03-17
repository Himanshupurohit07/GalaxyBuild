package com.sparc.wc.integration.aero.services;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COMMON_LOGGER_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.API_CALL_TYPE_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.API_CALL_PRINCIPAL_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.API_CALL_STATUS_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.API_CALL_MESSAGE_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.API_CALL_REQUEST_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.API_CALL_RESPONSE_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.API_CALL_REQUEST_TIMESTAMP_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.API_CALL_RESPONSE_TIMESTAMP_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.API_CALL_ERROR_TYPE_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.API_CALL_LOG_ENTRY_ORDER_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.API_CALL_COLORWAY_NUMBER_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.API_CALL_COLORWAY_SEASON_INTERNAL_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_LOG_ENTRY_DEFAULT_STRING_SIZE;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLogEntry;
import com.lcs.wc.foundation.LCSLogEntryLogic;
import com.lcs.wc.util.FormatHelper;
import com.sparc.wc.integration.aero.domain.AeroApiCallLogEntry;
import com.sparc.wc.integration.util.SparcIntegrationUtil;

import wt.log4j.LogR;
import wt.method.MethodContext;

/**
 * Service that provides support to create log entries into PLM.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Added logic to trim size for request, response and message strings to prevent flex exceptions when saving the log entry.<br>
 * - Task #10496: Added logic to handle multiple log entry creation to accommodate long text values within key attributes: 'request', 'response' or 'message'.
 * Note: This new logic replaces the trim fix previously added.<br>
 * - Post UAT Update: Request text is copied over into new additional log entries created when response text is truncated.<br>
 * - Post UAT Update: Log iteration info is now prepended into the message text if message is not truncated.<br>
 * - Task #10623: New Log Order column for log entry. Removed log order info from message attribute.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 */
public class AeroLogEntryService {
	
	private static int MAX_STRING_SIZE = 2000;
	private static final Logger LOGGER = LogR.getLogger(AERO_COMMON_LOGGER_NAME);
	
	static {
		if (!FLEX_LOG_ENTRY_DEFAULT_STRING_SIZE.isEmpty()) {
			try {
				MAX_STRING_SIZE = Integer.parseInt(FLEX_LOG_ENTRY_DEFAULT_STRING_SIZE);
			} catch (Exception e) {
				LOGGER.error("[AeroLogEntryService] Failed to parse FLEX_LOG_ENTRY_DEFAULT_STRING_SIZE as integer.", e);
			}
		}
	}
	
	private AeroLogEntryService() {
		
	}
	
	public static void log(final AeroApiCallLogEntry aeroApiCallLogEntry) {

        if (!FormatHelper.hasContent(aeroApiCallLogEntry.getFlexTypePath()) || !FormatHelper.hasContent(aeroApiCallLogEntry.getApiCallType())) {
            return;
        }
        
        try {
        	
        	final LCSLogEntryLogic logic = new LCSLogEntryLogic();
            final FlexType flexType = FlexTypeCache.getFlexTypeFromPath(aeroApiCallLogEntry.getFlexTypePath());
            
            if (flexType == null) {
                return;
            }
            
            if (aeroApiCallLogEntry.getResponseTime() == null) {
            	aeroApiCallLogEntry.setResponseTime(System.currentTimeMillis());
            }
            if (aeroApiCallLogEntry.getRequestTime() == null) {
            	aeroApiCallLogEntry.setRequestTime(System.currentTimeMillis());
            }
            
            final Timestamp requestTs = Timestamp.from(Instant.ofEpochMilli(aeroApiCallLogEntry.getRequestTime()));
            final Timestamp responseTs = Timestamp.from(Instant.ofEpochMilli(aeroApiCallLogEntry.getResponseTime()));
            
        	List<String> messageChunks = SparcIntegrationUtil.splitString(aeroApiCallLogEntry.getMessage(), MAX_STRING_SIZE);
            List<String> requestChunks = SparcIntegrationUtil.splitString(SparcIntegrationUtil.deserialize(aeroApiCallLogEntry.getRequest()), MAX_STRING_SIZE);
            List<String> responseChunks = SparcIntegrationUtil.splitString(SparcIntegrationUtil.deserialize(aeroApiCallLogEntry.getResponse()), MAX_STRING_SIZE);
            
            int messageChunksCount = messageChunks.size();
            int requestChunksCount =  requestChunks.size();
            int responseChunksCount = responseChunks.size();
            int logEntriesCount = calculateLogEntriesCount(messageChunksCount, requestChunksCount, responseChunksCount);
            
            LOGGER.debug("[AeroLogEntryService] Log entries chunks obtained from key text attributes: Message (" + messageChunksCount + "), Request (" + requestChunksCount + "), Response (" + responseChunksCount + ").");
            LOGGER.debug("[AeroLogEntryService] Number of Log Entries to be created: " + logEntriesCount + ".");
            
            for (int indx = 0; indx < logEntriesCount; indx += 1) {
            	
            	final LCSLogEntry logEntry = LCSLogEntry.newLCSLogEntry();
            	String logEntryIterationInfo = (indx + 1) + " / " + logEntriesCount;
                
                logEntry.setFlexType(flexType);
                logEntry.setValue(API_CALL_TYPE_INTERNAL_NAME, aeroApiCallLogEntry.getApiCallType());
                logEntry.setValue(API_CALL_PRINCIPAL_INTERNAL_NAME, MethodContext.getContext().getUserName());
            	
                if (aeroApiCallLogEntry.getStatus() != null) {
                    logEntry.setValue(API_CALL_STATUS_INTERNAL_NAME, aeroApiCallLogEntry.getStatus().toString());
                }
                
                if (messageChunksCount == 1) {
                	//Message text fits into a single log entry, so we can safely copy it into new log entries.
                	logEntry.setValue(API_CALL_MESSAGE_INTERNAL_NAME, messageChunks.get(0));
                } else if (indx < messageChunksCount) {
                	//Message text is split into multiple chunks because it is too long. In this scenario, we cannot copy the content between log entries.
                	logEntry.setValue(API_CALL_MESSAGE_INTERNAL_NAME, messageChunks.get(indx));
                }
                
                if (requestChunksCount == 1) {
                	//Request text fits into a single log entry, so we can safely copy it into new log entries.
                	logEntry.setValue(API_CALL_REQUEST_INTERNAL_NAME, requestChunks.get(0));
                } else if (indx < requestChunksCount) {
                	//Request text is split in multiple chunks because it is too long. In this scenario, we cannot copy the content between log entries.
                	logEntry.setValue(API_CALL_REQUEST_INTERNAL_NAME, requestChunks.get(indx));
                }
                
                if (responseChunksCount == 1) {
                	//Response text fits into a single log entry, so we can safely copy it into new log entries.
                	logEntry.setValue(API_CALL_RESPONSE_INTERNAL_NAME, responseChunks.get(0));
                } else if (indx < responseChunksCount) {
                	//Response text is split in multiple chunks because it is too long. In this scenario, we cannot copy the content between log entries.
                    logEntry.setValue(API_CALL_RESPONSE_INTERNAL_NAME, responseChunks.get(indx));
                }
                
                if (requestTs != null) {
                    logEntry.setValue(API_CALL_REQUEST_TIMESTAMP_INTERNAL_NAME, requestTs.toLocalDateTime().toString());
                }
                
                if (responseTs != null) {
                	logEntry.setValue(API_CALL_RESPONSE_TIMESTAMP_INTERNAL_NAME, responseTs.toLocalDateTime().toString());
                }
                
                if (aeroApiCallLogEntry.getErrorType() != null) {
                	logEntry.setValue(API_CALL_ERROR_TYPE_INTERNAL_NAME, aeroApiCallLogEntry.getErrorType().toString());
                }
                
                if (aeroApiCallLogEntry.getColorwayNumber() != null) {
                	logEntry.setValue(API_CALL_COLORWAY_NUMBER_INTERNAL_NAME, aeroApiCallLogEntry.getColorwayNumber());
                }
                
                if (aeroApiCallLogEntry.getColorwaySeason() != null) {
                	logEntry.setValue(API_CALL_COLORWAY_SEASON_INTERNAL_NAME, aeroApiCallLogEntry.getColorwaySeason());
                }
                
                logEntry.setValue(API_CALL_LOG_ENTRY_ORDER_INTERNAL_NAME, logEntryIterationInfo);
                
                logic.saveLog(logEntry, true);
                LOGGER.debug("[AeroLogEntryService] Log Entry for " + aeroApiCallLogEntry.getApiCallType() + " has been created (" + (indx + 1) + " of " + logEntriesCount + ").");
                
            }
            
        } catch (Exception e) {
            LOGGER.error("[AeroLogEntryService] Failed to create log entry for " + aeroApiCallLogEntry + ".", e);
        }

    }
	
	/**
     * Determine the amount of log entries required to accommodate the text chunks for key attributes: message, request and response. 
     * @param messageChunksCount The number of string chunks used for the message text.
     * @param requestChunksCount The number of string chunks used for the request text.
     * @param responseChunksCount The number of string chunks used for the response text.
     * @return The calculated number of log entries needed (defaults to 1).
     */
    private static int calculateLogEntriesCount(int messageChunksCount, int requestChunksCount, int responseChunksCount) {
    	
    	int logEntryCount = 1;
    	
    	if (messageChunksCount > logEntryCount) {
    		logEntryCount = messageChunksCount;
    	}
    	
    	if (requestChunksCount > logEntryCount) {
    		logEntryCount = requestChunksCount;
    	}
    	
    	if (responseChunksCount > logEntryCount) {
    		logEntryCount = responseChunksCount;
    	}
    	
    	return logEntryCount;
    }

}
