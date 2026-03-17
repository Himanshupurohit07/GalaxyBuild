package com.sparc.wc.shareFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.sparc.wc.shareFile.SPARCTechPackShareFileAuthenticator.OAuth2Token;

public class SPARCShareFileUtil {
	private static String username;
	private static String password;
	private static String hostname;
	private static String clientId;
	private static String clientSecret;

	private static final Logger LOGGER = LogManager.getLogger(SPARCShareFileUtil.class);

	static {
		try {
			username = LCSProperties.get("com.sparc.wc.product.SPARCTechPackShare.username",
					"geetika.paliwal@acnovate.com");
			password = LCSProperties.get("com.sparc.wc.product.SPARCTechPackShare.password", "U2ltYmFAOTgoKg==");
			hostname = LCSProperties.get("com.sparc.wc.product.SPARCTechPackShare.hostname",
					"sparcgroup.sharefile.com");
			clientId = LCSProperties.get("com.sparc.wc.product.SPARCTechPackShare.clientId",
					"K0GSDXTk49q7AHSHKcCOpOOJ76LmlpBR");
			clientSecret = LCSProperties.get("com.sparc.wc.product.SPARCTechPackShare.clientSecret",
					"3ITfwg7tpI1lUfPokFgGBJle4iG91OD8zcmL8zHqwOp9qhOR");

		} catch (Exception ex) {
			LOGGER.error("SPARCShareFileUtil: Error reading com.lcs.wc.product.* properties", ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public Map<Integer, String> initiateFileShare(String localPath, String folderPath2,
			SPARCShareFileAPICallLogEntry logEntry) {
		Map<Integer, String> response = new HashMap<>();
		OAuth2Token auth2Token = null;

		try {
			LOGGER.debug("hostname : {}" + hostname);
			LOGGER.debug("clientId : {}" + clientId);
			LOGGER.debug("clientSecret : {}" + clientSecret);
			LOGGER.debug("username : {}" + username);
			LOGGER.debug("password : {}" + password);
			auth2Token = SPARCTechPackShareFileAuthenticator.authenticate(hostname, clientId, clientSecret, username,
					password);
		} catch (IOException ioe) {
			LOGGER.error("Exception occurred while calling Authorizing to ShareAPI! Exception Details:", ioe);
			response.put(401, ioe.getLocalizedMessage());
			ioe.printStackTrace();
			return response;
		}
		try {
			String folderIdByPath = SPARCTechPackShareFileClient.getItemIdByPath2(auth2Token, folderPath2);
			if (FormatHelper.hasContent(folderIdByPath))
				response = SPARCTechPackShareFileClient.uploadFile(auth2Token, folderIdByPath, localPath, logEntry);

		} catch (Exception ex) {
			LOGGER.error("Exception occurred while calling ShareAPI Client methods! Exception Details:", ex);
			ex.printStackTrace();
		}
		return response;
	}
}
