package com.sparc.wc.integration.services;

import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLogEntry;
import com.lcs.wc.foundation.LCSLogEntryLogic;
import com.lcs.wc.util.FormatHelper;
import com.sparc.wc.integration.constants.SparcIntegrationConstants;
import com.sparc.wc.integration.domain.SparcApiCallLogEntry;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import org.apache.logging.log4j.Logger;
import wt.log4j.LogR;
import wt.method.MethodContext;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FLEX_LOG_ENTRY_DEFAULT_STRING_SIZE;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * Service that provides support to create log entries into PLM.<br>
 *
 * FIXES/AMENDMENTS:<br>
 * - Task #10245 (Hypercare): Added logic to trim size for request, response and message strings to prevent flex exceptions when saving the log entry.<br>
 * - Task #10496: Added logic to handle multiple log entry creation to accommodate long text values within key attributes: 'request', 'response' or 'message'.
 * Note: This new logic replaces the trim fix previously added for Task #10245.
 * - Post UAT Update: Request text is copied over into new additional log entries created when response text is truncated.<br>
 * - Post UAT Update: Log iteration info is now prepended into the message text if message is not truncated.<br>
 * - Task #10623: New Log Order column for log entry. Removed log order info from message attribute.<br>
 *
 * @author Acnovate
 */
public class SparcLogEntryService {

    private static int MAX_STRING_SIZE = 2000;
    private static final Logger LOGGER = LogR.getLogger(SparcLogEntryService.class.getName());

    static {
        if (!FLEX_LOG_ENTRY_DEFAULT_STRING_SIZE.isEmpty()) {
            try {
                MAX_STRING_SIZE = Integer.parseInt(FLEX_LOG_ENTRY_DEFAULT_STRING_SIZE);
            } catch (Exception e) {
                LOGGER.error("[SparcLogEntryService] Failed to parse FLEX_LOG_ENTRY_DEFAULT_STRING_SIZE as integer.", e);
            }
        }
    }

    public void log(final SparcApiCallLogEntry sparcApiCallLogEntry) {

        if (!FormatHelper.hasContent(sparcApiCallLogEntry.getFlexTypePath()) || !FormatHelper.hasContent(sparcApiCallLogEntry.getApiCallType())) {
            return;
        }
        
        try {
        	
        	final LCSLogEntryLogic logic = new LCSLogEntryLogic();
        	final FlexType flexType = FlexTypeCache.getFlexTypeFromPath(sparcApiCallLogEntry.getFlexTypePath());
        	
            if (flexType == null) {
                return;
            }
        	
            if (sparcApiCallLogEntry.getResponseTime() == null) {
                sparcApiCallLogEntry.setResponseTime(System.currentTimeMillis());
            }
            if (sparcApiCallLogEntry.getRequestTime() == null) {
                sparcApiCallLogEntry.setRequestTime(System.currentTimeMillis());
            }
            
            final Timestamp requestTs = getTimestamp(sparcApiCallLogEntry.getRequestTime());
            final Timestamp responseTs = getTimestamp(sparcApiCallLogEntry.getRequestTime());
            
        	List<String> messageChunks = SparcIntegrationUtil.splitString(sparcApiCallLogEntry.getMessage(), MAX_STRING_SIZE);
            List<String> requestChunks = SparcIntegrationUtil.splitString(SparcIntegrationUtil.deserialize(sparcApiCallLogEntry.getRequest()), MAX_STRING_SIZE);
            List<String> responseChunks = SparcIntegrationUtil.splitString(SparcIntegrationUtil.deserialize(sparcApiCallLogEntry.getResponse()), MAX_STRING_SIZE);
            
            int messageChunksCount = messageChunks.size();
            int requestChunksCount =  requestChunks.size();
            int responseChunksCount = responseChunks.size();
            int logEntriesCount = calculateLogEntriesCount(messageChunksCount, requestChunksCount, responseChunksCount);
            
            LOGGER.debug("[SparcLogEntryService] Log entries chunks obtained from key text attributes: Message (" + messageChunksCount + "), Request (" + requestChunksCount + "), Response (" + responseChunksCount + ").");
            LOGGER.debug("[SparcLogEntryService] Number of Log Entries to be created: " + logEntriesCount + ".");
            
            for (int indx = 0; indx < logEntriesCount; indx += 1) {
            	
            	final LCSLogEntry logEntry = LCSLogEntry.newLCSLogEntry();
            	String logEntryIterationInfo = (indx + 1) + " / " + logEntriesCount;
            	
                logEntry.setFlexType(flexType);
                logEntry.setValue(SparcIntegrationConstants.API_CALL_TYPE_INTERNAL_NAME, sparcApiCallLogEntry.getApiCallType());
                logEntry.setValue(SparcIntegrationConstants.API_CALL_PRINCIPAL_INTERNAL_NAME, MethodContext.getContext().getUserName());
                
                if (sparcApiCallLogEntry.getStatus() != null) {
                    logEntry.setValue(SparcIntegrationConstants.API_CALL_STATUS_INTERNAL_NAME, sparcApiCallLogEntry.getStatus().toString());
                }
            	
                if (messageChunksCount == 1) {
                	//Message text fits into a single log entry, so we can safely copy it into new log entries.
                	logEntry.setValue(SparcIntegrationConstants.API_CALL_MESSAGE_INTERNAL_NAME, messageChunks.get(0));
                } else if (indx < messageChunksCount) {
                	//Message text is split into multiple chunks because it is too long. In this scenario, we cannot copy the content between log entries.
                	logEntry.setValue(SparcIntegrationConstants.API_CALL_MESSAGE_INTERNAL_NAME, messageChunks.get(indx));
                }
                
                if (requestChunksCount == 1) {
                	//Request text fits into a single log entry, so we can safely copy it into new log entries.
                	logEntry.setValue(SparcIntegrationConstants.API_CALL_REQUEST_INTERNAL_NAME, requestChunks.get(0));
                } else if (indx < requestChunksCount) {
                	//Request text is split in multiple chunks because it is too long. In this scenario, we cannot copy the content between log entries.
                	logEntry.setValue(SparcIntegrationConstants.API_CALL_REQUEST_INTERNAL_NAME, requestChunks.get(indx));
                }
                
                if (responseChunksCount == 1) {
                	//Response text fits into a single log entry, so we can safely copy it into new log entries.
                	logEntry.setValue(SparcIntegrationConstants.API_CALL_RESPONSE_INTERNAL_NAME, responseChunks.get(0));
                } else if (indx < responseChunksCount) {
                	//Response text is split in multiple chunks because it is too long. In this scenario, we cannot copy the content between log entries.
                    logEntry.setValue(SparcIntegrationConstants.API_CALL_RESPONSE_INTERNAL_NAME, responseChunks.get(indx));
                }
                
                if (requestTs != null) {
                    logEntry.setValue(SparcIntegrationConstants.API_CALL_REQUEST_TIMESTAMP_INTERNAL_NAME, requestTs.toLocalDateTime().toString());
                }
                
                if (responseTs != null) {
                    logEntry.setValue(SparcIntegrationConstants.API_CALL_RESPONSE_TIMESTAMP_INTERNAL_NAME, responseTs.toLocalDateTime().toString());
                }
                
                logEntry.setValue(SparcIntegrationConstants.API_CALL_LOG_ENTRY_ORDER_INTERNAL_NAME, logEntryIterationInfo);
                
                logic.saveLog(logEntry, true);
                LOGGER.debug("[SparcLogEntryService] Log Entry for " + sparcApiCallLogEntry.getApiCallType() + " has been created " + logEntryIterationInfo + ".");
                
            }//end for loop.
        	
        } catch (Exception e) {
            LOGGER.error("[SparcLogEntryService] Failed to create log entry: flexTypePath=" + sparcApiCallLogEntry.getFlexTypePath() +
                    ", apiCallType=" + sparcApiCallLogEntry.getApiCallType() +
                    ", request=" + sparcApiCallLogEntry.getRequest() +
                    ", response=" + sparcApiCallLogEntry.getResponse() +
                    ", requestTime=" + sparcApiCallLogEntry.getRequestTime() +
                    ", responseTime=" + sparcApiCallLogEntry.getResponseTime() +
                    ", status=" + sparcApiCallLogEntry.getStatus() +
                    ", message=" + sparcApiCallLogEntry.getMessage() + ".", e);
        }

    }
    
    /**
     * Determine the amount of log entries required to accommodate the text chunks for key attributes: message, request and response. 
     * @param messageChunksCount The number of string chunks used for the message text.
     * @param requestChunksCount The number of string chunks used for the request text.
     * @param responseChunksCount The number of string chunks used for the response text.
     * @return The calculated number of log entries needed (defaults to 1).
     */
    private int calculateLogEntriesCount(int messageChunksCount, int requestChunksCount, int responseChunksCount) {
    	
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

    private Timestamp getTimestamp(final long timeInMillis) {
        try {
            return Timestamp.from(Instant.ofEpochMilli(timeInMillis));
        } catch (Exception e) {
            return null;
        }
    }

}
