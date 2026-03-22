package com.paola.paolarestapi.integration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paola.paolarestapi.integration.model.ReqResUserItem;
import com.paola.paolarestapi.integration.model.ReqResUsersResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/*
  Minimal HTTP client for public ReqRes users API.

  Responsibilities:
  - Fetch all pages from https://reqres.in/api/users.
  - Deserialize JSON into ReqResUsersResponse / ReqResUserItem models.
  - Return a flat list of users for import/snapshot steps.
*/
public class ReqResClientService {
    private static final String BASE_URL = "https://reqres.in/api/users";
    private static final String API_KEY_ENV = "REQRES_API_KEY";
    private static final String API_KEY_SYS_PROP = "reqres.api.key";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public List<ReqResUserItem> fetchAllUsers() {
        List<ReqResUserItem> allUsers = new ArrayList<ReqResUserItem>();
        ReqResUsersResponse firstPage = fetchPage(1);
        if (firstPage.getData() != null) {
            allUsers.addAll(firstPage.getData());
        }

        int totalPages = firstPage.getTotalPages() == null ? 1 : firstPage.getTotalPages();
        for (int page = 2; page <= totalPages; page++) {
            ReqResUsersResponse response = fetchPage(page);
            if (response.getData() != null) {
                allUsers.addAll(response.getData());
            }
        }
        return allUsers;
    }

    private ReqResUsersResponse fetchPage(int page) {
        HttpURLConnection connection = null;
        try {
            String apiKey = readApiKey();
            URL url = new URL(BASE_URL + "?page=" + page);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-api-key", apiKey);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();

            int statusCode = connection.getResponseCode();
            InputStream stream = statusCode >= 200 && statusCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            if (stream == null) {
                throw new IllegalStateException("ReqRes response stream is empty. HTTP " + statusCode);
            }

            String body = readAsString(stream);
            if (statusCode < 200 || statusCode >= 300) {
                throw new IllegalStateException("ReqRes call failed. HTTP " + statusCode + " body: " + body);
            }
            return OBJECT_MAPPER.readValue(body, ReqResUsersResponse.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to fetch ReqRes page " + page, exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readAsString(InputStream inputStream) throws Exception {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }

    private String readApiKey() {
        String valueFromEnv = System.getenv(API_KEY_ENV);
        if (valueFromEnv != null && !valueFromEnv.trim().isEmpty()) {
            return valueFromEnv.trim();
        }
        String valueFromProperty = System.getProperty(API_KEY_SYS_PROP);
        if (valueFromProperty != null && !valueFromProperty.trim().isEmpty()) {
            return valueFromProperty.trim();
        }
        throw new IllegalStateException(
                "ReqRes API key is missing. Set env var REQRES_API_KEY or JVM property reqres.api.key."
        );
    }
}
