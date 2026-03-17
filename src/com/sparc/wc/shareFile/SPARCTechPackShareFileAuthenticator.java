package com.sparc.wc.shareFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SPARCTechPackShareFileAuthenticator {
	private static final Logger logger = LogManager.getLogger(SPARCTechPackShareFileAuthenticator.class.getName());

	public static class OAuth2Token {

		public String accessToken = "";
		public String refreshToken = "";
		public String tokenType = "";
		public String appcp = "";
		public String apicp = "";
		public String subdomain = "";
		public int expiresIn = 0;

		public OAuth2Token(JsonObject json) {
			if (json != null) {
				accessToken = json.get("access_token").getAsString();
				refreshToken = json.get("refresh_token").getAsString();
				tokenType = json.get("token_type").getAsString();
				appcp = json.get("appcp").getAsString();
				apicp = json.get("apicp").getAsString();
				subdomain = json.get("subdomain").getAsString();
				expiresIn = json.get("expires_in").getAsInt();
			}
		}
	}

	public static OAuth2Token authenticate(String hostname, String clientId, String clientSecret, String username,
			String password) throws IOException {
		URL grantUrl = new URL(String.format("https://%s/oauth/token", hostname));
		HashMap<String, String> params = new HashMap<String, String>();
		StringBuilder queryString = new StringBuilder();
		params.put("grant_type", "password");
		params.put("client_id", clientId);
		params.put("client_secret", clientSecret);
		params.put("username", username);
		byte[] decodedBytes = Base64.getDecoder().decode(password);
		String decodedPass = new String(decodedBytes);
		params.put("password", decodedPass);

		for (Entry<String, String> entry : params.entrySet()) {
			queryString.append(String.format("%s=%s&", entry.getKey(),
					URLEncoder.encode(((String) entry.getValue()).toString(), "UTF-8")));
		}

		queryString.deleteCharAt(queryString.length() - 1);
		logger.debug(queryString);

		HttpURLConnection connection = (HttpURLConnection) grantUrl.openConnection();
		connection.setRequestMethod("POST");
		connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setDoOutput(true);

		logger.debug(connection.getRequestMethod() + " " + connection.getURL());
		connection.connect();
		connection.getOutputStream().write(queryString.toString().getBytes());
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuilder response = new StringBuilder();

		String line;
		while ((line = reader.readLine()) != null) {
			response.append(line);
		}

		// print http response code/message and response body
		logger.debug(connection.getResponseCode() + " " + connection.getResponseMessage());
		logger.debug("Response : " + response);
		JsonObject token = null;
		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			JsonParser parser = new JsonParser();
			token = (JsonObject) parser.parse(response.toString());
			logger.debug("Token : " + token);
		}

		return new OAuth2Token(token);
	}

	public static String getHostname(OAuth2Token token) {
		return String.format("%s.sf-api.com", token.subdomain);
	}
}