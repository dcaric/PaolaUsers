package com.paola.paolarestapi.restapi.service;

import java.util.Locale;

/*
  Why it is used:
  - Configuration switch toggles between public ReqRes and local custom API source.

  How it is used:
  - RestApiResource reads active source before handling GET endpoints.
  - Value comes from env REST_API_SOURCE or JVM prop rest.api.source.

  How it works:
  - Accepts LOCAL (default) or PUBLIC (case-insensitive).
  - Any unknown value falls back to LOCAL to keep runtime stable.
*/
public class RestApiSourceSwitchService {
    private static final String SOURCE_ENV = "REST_API_SOURCE";
    private static final String SOURCE_PROP = "rest.api.source";

    public RestApiSourceType getActiveSource() {
        String raw = readValue();
        if (raw == null) {
            return RestApiSourceType.LOCAL;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if ("PUBLIC".equals(normalized)) {
            return RestApiSourceType.PUBLIC;
        }
        return RestApiSourceType.LOCAL;
    }

    private String readValue() {
        String fromEnv = System.getenv(SOURCE_ENV);
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv.trim();
        }
        String fromProp = System.getProperty(SOURCE_PROP);
        if (fromProp != null && !fromProp.trim().isEmpty()) {
            return fromProp.trim();
        }
        return null;
    }
}
