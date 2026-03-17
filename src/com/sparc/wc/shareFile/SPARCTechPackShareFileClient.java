package com.sparc.wc.shareFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SPARCTechPackShareFileClient {
	private static Gson gson = new Gson();

	public static Map<Integer, String> makeGetAPICall(String url, SPARCTechPackShareFileAuthenticator.OAuth2Token token)
			throws IOException {
		HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Authorization", "Bearer " + token.accessToken);
		connection.setDoOutput(true);
		int responseCode = connection.getResponseCode();
		StringBuilder response = new StringBuilder();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}

			in.close();
		}

		// System.out.println(response);
		Map<Integer, String> builderPair = new HashMap<Integer, String>();
		builderPair.put(responseCode, response.toString());
		connection.disconnect();
		return builderPair;
	}

	public static String makePostAPICall(String url, SPARCTechPackShareFileAuthenticator.OAuth2Token token, String body)
			throws IOException {
		HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Authorization", "Bearer " + token.accessToken);
		connection.setDoOutput(true);
		if (body != null && !body.isEmpty()) {
			OutputStream os = connection.getOutputStream();
			byte[] input = body.getBytes("utf-8");
			os.write(input, 0, input.length);
		}

		int responseCode = connection.getResponseCode();
		StringBuilder response = new StringBuilder();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}

			in.close();
		} else {
			response.append("POST request failed with response code: ").append(responseCode);
		}

		connection.disconnect();
		return response.toString();
	}

	public static String getItemId(String folderId, String itemName,
			SPARCTechPackShareFileAuthenticator.OAuth2Token token) throws IOException {
		String streamId = null;
		String fileListUrl = String.format("https://%s/sf/v3/Items(%s)/Children",
				SPARCTechPackShareFileAuthenticator.getHostname(token), folderId);
		String response = null;
		Map<Integer, String> map = makeGetAPICall(fileListUrl, token);
		Iterator<?> var9 = map.entrySet().iterator();
		if (var9.hasNext()) {
			Entry<Integer, String> entry = (Entry) var9.next();
			response = (String) entry.getValue();
		}

		JsonObject itemsResponse = (JsonObject) gson.fromJson(response, JsonObject.class);
		if (itemsResponse.has("value")) {
			JsonArray items = itemsResponse.getAsJsonArray("value");

			for (int i = 0; i < items.size(); ++i) {
				JsonObject item = items.get(i).getAsJsonObject();
				String itemNameInFolder = item.get("Name").getAsString();
				if (itemNameInFolder.equals(itemName) && item.has("StreamID")) {
					streamId = item.get("StreamID").getAsString();
					// System.out.println("Item ID :" + streamId);
					break;
				}
			}
		}

		return streamId;
	}

	public static String getItemIdByPath2(SPARCTechPackShareFileAuthenticator.OAuth2Token token, String path)
			throws IOException {
		String itemId = null;

		String[] pathComponents = path.split("/");
		try {
			String lastFolderName = pathComponents[pathComponents.length - 1];

			String path2 = path.substring(0, path.indexOf(lastFolderName));

			path2 = path2 + lastFolderName;
			path2 = URLEncoder.encode(path2, "UTF-8");
			String url = String.format("https://%s/sf/v3/Items/ByPath?path=/%s",
					SPARCTechPackShareFileAuthenticator.getHostname(token), path2);
			Integer responseCode = null;
			String response = null;
			Map<Integer, String> map = makeGetAPICall(url, token);
			Iterator<?> var10 = map.entrySet().iterator();
			if (var10.hasNext()) {
				Entry<Integer, String> entry = (Entry) var10.next();
				responseCode = entry.getKey();
				response = entry.getValue();
			}

			if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
				if (pathComponents.length == 1) {
					throw new IOException("Cannot proceed with shareFile creation!!");
				}

				String secondLastFolderPath = path.substring(0, path.indexOf(lastFolderName));
				itemId = createFolder(token, getItemIdByPath2(token, secondLastFolderPath), lastFolderName, "..");
			} else {
				JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
				itemId = jsonObject.get("Id").getAsString();
			}

		} catch (Exception var11) {
			var11.printStackTrace();
		}

		return itemId;
	}

	public static String fullPathId(SPARCTechPackShareFileAuthenticator.OAuth2Token token, String path)
			throws IOException {
		String folderId = null;
		try {
			String url = String.format("https://%s/sf/v3/Items/ByPath?path=/%s",
					SPARCTechPackShareFileAuthenticator.getHostname(token), path);
			Integer responseCode = null;
			String response = null;
			Map<Integer, String> map = makeGetAPICall(url, token);
			Iterator<?> var10 = map.entrySet().iterator();
			if (var10.hasNext()) {
				Entry<Integer, String> entry = (Entry) var10.next();
				responseCode = (Integer) entry.getKey();
				response = (String) entry.getValue();
			}

			if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
				return folderId;
			} else {
				JsonObject jsonObject = (JsonObject) gson.fromJson(response, JsonObject.class);
				folderId = jsonObject.get("Id").getAsString();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return folderId;

	}

	public static String getItemIdByName(SPARCTechPackShareFileAuthenticator.OAuth2Token token, String itemName) {
		String itemId = null;
		String url = String.format("https://%s/sf/v3/Items/Search?term=%s",
				SPARCTechPackShareFileAuthenticator.getHostname(token), itemName);

		try {
			String response = null;
			Map<Integer, String> map = makeGetAPICall(url, token);
			Iterator<?> var8 = map.entrySet().iterator();
			if (var8.hasNext()) {
				Entry<Integer, String> entry = (Entry) var8.next();
				response = (String) entry.getValue();
			}

			JsonObject jsonObject = (JsonObject) gson.fromJson(response, JsonObject.class);
			JsonArray resultsArray = jsonObject.getAsJsonArray("Results");
			Iterator<?> var10 = resultsArray.iterator();

			while (var10.hasNext()) {
				JsonElement result = (JsonElement) var10.next();
				JsonObject item = result.getAsJsonObject();
				if (item.has("FileName") && item.get("FileName").getAsString().equals(itemName)) {
					itemId = item.get("ItemID").getAsString();
					// System.out.println("ItemId by name :" + itemId);
					break;
				}
			}
		} catch (Exception var12) {
			var12.printStackTrace();
		}

		return itemId;
	}

	public static String createFolder(SPARCTechPackShareFileAuthenticator.OAuth2Token token, String parentId,
			String name, String description) throws IOException {
		String url = String.format("https://%s/sf/v3/Items(%s)/Folder",
				SPARCTechPackShareFileAuthenticator.getHostname(token), parentId);
		String folderId = null;
		Map<String, Object> folder = new HashMap<>();
		folder.put("Name", name);
		folder.put("Description", description);
		String body = gson.toJson(folder);
		String response = makePostAPICall(url, token, body);
		JsonObject item = (JsonObject) gson.fromJson(response, JsonObject.class);
		folderId = item.get("Id").getAsString();
		return folderId;
	}

	public static Map<Integer, String> uploadFile(SPARCTechPackShareFileAuthenticator.OAuth2Token token,
			String folderId, String localPath, SPARCShareFileAPICallLogEntry logEntry) throws IOException {
		String fileName = null;
		String url = String.format("https://%s/sf/v3/Items(%s)/Upload",
				SPARCTechPackShareFileAuthenticator.getHostname(token), folderId);
		String response = null;
		Integer responseCode = null;
		Map<Integer, String> map = makeGetAPICall(url, token);
		Iterator<?> var9 = map.entrySet().iterator();
		if (var9.hasNext()) {
			Entry<Integer, String> entry = (Entry) var9.next();
			responseCode = entry.getKey();
			response = entry.getValue();
		}

		JsonObject uploadConfig = (JsonObject) gson.fromJson(response, JsonObject.class);
		JsonElement element = uploadConfig.get("ChunkUri");
		if (element != null) {
			String chunkUri = element.getAsString();
			fileName = multipartUploadFile(localPath, chunkUri);
		} else {
			System.out.println("Did not receive an Upload URL");
		}

		if (logEntry != null) {
			logEntry.setRequest(url);
		}

		return map;
	}

	private static String multipartUploadFile(String localPath, String uploadUrl) throws IOException {
		String fileName = null;
		URL url = new URL(uploadUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		String boundary = "--" + UUID.randomUUID().toString();
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		File file = new File(localPath);
		String filename = file.getName();
		InputStream source = new FileInputStream(file);
		OutputStream target = connection.getOutputStream();
		StringBuffer buffer = new StringBuffer();
		buffer.append("--" + boundary + "\r\n");
		buffer.append("Content-Disposition: form-data; name=File1; filename=\"" + filename + "\"\r\n");
		String contentType = HttpURLConnection.guessContentTypeFromName(filename);
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		buffer.append("Content-Type: " + contentType + "\r\n\r\n");
		target.write(buffer.toString().getBytes());
		byte[] buf = new byte[1048576];

		int len;
		while ((len = source.read(buf, 0, buf.length)) >= 0) {
			target.write(buf, 0, len);
		}

		target.flush();
		target.write(("\r\n--" + boundary + "--\r\n").getBytes());
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuffer response = new StringBuffer();
		String line = null;

		while ((line = reader.readLine()) != null) {
			response.append(line).append("\n");
		}

		reader.close();
		String responseAsString = response.toString();
		String[] parts = responseAsString.split(":");
		fileName = parts[1].trim();
		target.close();
		source.close();
		connection.disconnect();
		return fileName;
	}

	public static String generateDownloadLinkForPublic(SPARCTechPackShareFileAuthenticator.OAuth2Token token,
			String itemId) throws IOException {
		String downloadLink = null;
		String requestBody = "{\n\"Items\": [ { \"Id\":\"" + itemId + "\" } ]\n}";
		String url = String.format("https://%s/sf/v3/Shares", SPARCTechPackShareFileAuthenticator.getHostname(token));

		try {
			String response = makePostAPICall(url, token, requestBody);
			JsonObject jsonObject = (JsonObject) gson.fromJson(response, JsonObject.class);
			downloadLink = jsonObject.get("Uri").getAsString();
			// System.out.println("Link for public : " + downloadLink);
		} catch (Exception var7) {
			var7.printStackTrace();
		}

		return downloadLink;
	}

	public static String generateDownloadLinkForClientUser(SPARCTechPackShareFileAuthenticator.OAuth2Token token,
			String itemId) throws IOException {
		String downloadLink = null;
		String requestBody = "{\"ShareType\": \"Send\",\"Title\": \"Sample Send Share\",\"Items\": [{ \"Id\": \""
				+ itemId
				+ "\" }],\"Recipients\": [{ \"User\": { \"Email\": \"indranilbiswas93@gmail.com\" } }],\"ExpirationDate\": \"2024-07-23\",\"RequireLogin\": true,\"RequireUserInfo\": true,\"MaxDownloads\": -1,\"UsesStreamIDs\": false}";
		String url = String.format("https://%s/sf/v3/Shares", SPARCTechPackShareFileAuthenticator.getHostname(token));
		String response = makePostAPICall(url, token, requestBody);
		JsonObject jsonResponse = (JsonObject) gson.fromJson(response, JsonObject.class);
		downloadLink = jsonResponse.get("Uri").getAsString();
		System.out.println("Link for client user : " + downloadLink);
		return downloadLink;
	}
}