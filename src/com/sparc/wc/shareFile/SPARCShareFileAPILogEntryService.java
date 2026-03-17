package com.sparc.wc.shareFile;

import java.sql.Timestamp;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLogEntry;
import com.lcs.wc.foundation.LCSLogEntryLogic;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class SPARCShareFileAPILogEntryService {

	private static final Logger LOGGER = LogManager.getLogger(SPARCShareFileAPILogEntryService.class);
	private static final String LOG_ENTRY_SHAREFILE_INTEGRATION_TYPE = LCSProperties.get(
			"com.sparc.wc.foundation.LCSLogEntry.shareFileIntegrationLogEntry.flexTypePath",
			"Log Entry\\scShareFileIntegrationLogEntry");
	private static final String LOG_ENTRY_API_CALL_TYPE_ATTKEY = LCSProperties
			.get("com.sparc.wc.foundation.LCSLogEntry.shareFileIntegrationLogEntry.apiCallType", "scAPICallType");
	private static final String LOG_ENTRY_REQUEST_ATTKEY = LCSProperties
			.get("com.sparc.wc.foundation.LCSLogEntry.shareFileIntegrationLogEntry.request", "scRequest");
	private static final String LOG_ENTRY_RESPONSE_ATTKEY = LCSProperties
			.get("com.sparc.wc.foundation.LCSLogEntry.shareFileIntegrationLogEntry.response", "scResponse");
	private static final String LOG_ENTRY_REQUEST_TIME_ATTKEY = LCSProperties
			.get("com.sparc.wc.foundation.LCSLogEntry.shareFileIntegrationLogEntry.requestTime", "scRequestTime");
	private static final String LOG_ENTRY_RESPONSE_TIME_ATTKEY = LCSProperties
			.get("com.sparc.wc.foundation.LCSLogEntry.shareFileIntegrationLogEntry.responseTime", "scResponseTime");
	private static final String LOG_ENTRY_STATUS_ATTKEY = LCSProperties
			.get("com.sparc.wc.foundation.LCSLogEntry.shareFileIntegrationLogEntry.status", "scStatus");
	private static final String LOG_ENTRY_PLM_PRODUCT_NO_ATTKEY = LCSProperties
			.get("com.sparc.wc.foundation.LCSLogEntry.shareFileIntegrationLogEntry.plmProductNo", "scPLMProductNo");
	private static final String LOG_ENTRY_SEASON_ATTKEY = LCSProperties
			.get("com.sparc.wc.foundation.LCSLogEntry.shareFileIntegrationLogEntry.season", "scSeason");
	private static final String LOG_ENTRY_SPECIFICATION_ATTKEY = LCSProperties
			.get("com.sparc.wc.foundation.LCSLogEntry.shareFileIntegrationLogEntry.specification", "scSpecification");
	private static final String LOG_ENTRY_PARTNER_ATTKEY = LCSProperties
			.get("com.sparc.wc.foundation.LCSLogEntry.shareFileIntegrationLogEntry.partner", "scPartner");

	public void log(SPARCShareFileAPICallLogEntry shareFileLogEntry) {
		if (!FormatHelper.hasContent(shareFileLogEntry.getFlexTypePath())) {
			return;
		}
		System.out.println("setting logEntry..!!");
		LCSLogEntry logEntry = new LCSLogEntry();
		FlexType flexType;
		try {
			flexType = FlexTypeCache.getFlexTypeFromPath(LOG_ENTRY_SHAREFILE_INTEGRATION_TYPE);
			logEntry.setFlexType(flexType);
			logEntry.setValue(LOG_ENTRY_API_CALL_TYPE_ATTKEY, shareFileLogEntry.getApiCallType());
			Timestamp requestTs = Timestamp.from(Instant.ofEpochMilli(shareFileLogEntry.getRequestTime()));
			Timestamp responseTs = Timestamp.from(Instant.ofEpochMilli(shareFileLogEntry.getResponseTime()));
			logEntry.setValue(LOG_ENTRY_REQUEST_ATTKEY, shareFileLogEntry.getRequest());
			logEntry.setValue(LOG_ENTRY_RESPONSE_ATTKEY, shareFileLogEntry.getResponse());
			logEntry.setValue(LOG_ENTRY_REQUEST_TIME_ATTKEY, requestTs.toLocalDateTime().toString());
			logEntry.setValue(LOG_ENTRY_RESPONSE_TIME_ATTKEY, responseTs.toLocalDateTime().toString());
			logEntry.setValue(LOG_ENTRY_STATUS_ATTKEY, shareFileLogEntry.getStatus().getResponseCode());
			logEntry.setValue(LOG_ENTRY_PLM_PRODUCT_NO_ATTKEY, shareFileLogEntry.getPlmProductNo());
			logEntry.setValue(LOG_ENTRY_SEASON_ATTKEY, shareFileLogEntry.getSeason());
			logEntry.setValue(LOG_ENTRY_SPECIFICATION_ATTKEY, shareFileLogEntry.getSpecification());
			logEntry.setValue(LOG_ENTRY_PARTNER_ATTKEY, shareFileLogEntry.getPartner());

			new LCSLogEntryLogic().saveLog(logEntry, true);

		} catch (WTException | WTPropertyVetoException e) {
			LOGGER.error("Unable to log error message to log entry due to exception!", e);
			e.printStackTrace();
		}
	}
}
