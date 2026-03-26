package com.paola.paolarestapi.restapi.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paola.paolarestapi.restapi.model.RestApiUser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/*
  Why it is used:
  - Switch mode needs public ReqRes source support.

  How it is used:
  - RestApiResource calls this service when active source is PUBLIC.
  - Used for GET operations (list and single user).

  How it works:
  - Calls ReqRes users API pages and maps JSON to ReqRes-shaped RestApiUser list.
  - Reads API key from REQRES_API_KEY or reqres.api.key and sends it as x-api-key.
*/
public class PublicReqResUserService {
    private static final String BASE_URL = "https://reqres.in/api/users";
    private static final String API_KEY_ENV = "REQRES_API_KEY";
    private static final String API_KEY_SYS_PROP = "reqres.api.key";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public List<RestApiUser> fetchAllUsers() {
        ReqResPageResponse firstPage = fetchPage(1);
        List<RestApiUser> result = mapUsers(firstPage.getData());

        int totalPages = firstPage.getTotalPages() == null ? 1 : firstPage.getTotalPages();
        for (int page = 2; page <= totalPages; page++) {
            ReqResPageResponse response = fetchPage(page);
            result.addAll(mapUsers(response.getData()));
        }
        return result;
    }

    public RestApiUser fetchUserById(Long id) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + "/" + id);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-api-key", readApiKey());
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();

            int status = connection.getResponseCode();
            if (status == 404) {
                return null;
            }
            InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = readAsString(stream);
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("ReqRes single-user call failed. HTTP " + status + " body: " + body);
            }
            ReqResSingleResponse response = OBJECT_MAPPER.readValue(body, ReqResSingleResponse.class);
            if (response.getData() == null) {
                return null;
            }
            return toModel(response.getData());
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to fetch user by id from ReqRes.", exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private ReqResPageResponse fetchPage(int page) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + "?page=" + page);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-api-key", readApiKey());
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();

            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = readAsString(stream);
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("ReqRes page call failed. HTTP " + status + " body: " + body);
            }
            return OBJECT_MAPPER.readValue(body, ReqResPageResponse.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to fetch ReqRes users page " + page, exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private List<RestApiUser> mapUsers(List<ReqResUserItem> items) {
        List<RestApiUser> result = new ArrayList<RestApiUser>();
        if (items == null) {
            return result;
        }
        for (ReqResUserItem item : items) {
            result.add(toModel(item));
        }
        return result;
    }

    private RestApiUser toModel(ReqResUserItem item) {
        return new RestApiUser(
                item.getId() == null ? null : item.getId().longValue(),
                item.getEmail(),
                item.getFirstName(),
                item.getLastName(),
                item.getAvatar()
        );
    }

    private String readAsString(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }

    private String readApiKey() {
        String fromEnv = System.getenv(API_KEY_ENV);
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv.trim();
        }
        String fromProperty = System.getProperty(API_KEY_SYS_PROP);
        if (fromProperty != null && !fromProperty.trim().isEmpty()) {
            return fromProperty.trim();
        }
        throw new IllegalStateException("ReqRes API key is missing. Set REQRES_API_KEY or -Dreqres.api.key.");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ReqResPageResponse {
        @JsonProperty("total_pages")
        private Integer totalPages;
        private List<ReqResUserItem> data;

        public Integer getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(Integer totalPages) {
            this.totalPages = totalPages;
        }

        public List<ReqResUserItem> getData() {
            return data;
        }

        public void setData(List<ReqResUserItem> data) {
            this.data = data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ReqResSingleResponse {
        private ReqResUserItem data;

        public ReqResUserItem getData() {
            return data;
        }

        public void setData(ReqResUserItem data) {
            this.data = data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ReqResUserItem {
        private Integer id;
        private String email;
        @JsonProperty("first_name")
        private String firstName;
        @JsonProperty("last_name")
        private String lastName;
        private String avatar;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }
}
