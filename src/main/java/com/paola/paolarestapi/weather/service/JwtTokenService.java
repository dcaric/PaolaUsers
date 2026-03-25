package com.paola.paolarestapi.weather.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paola.paolarestapi.weather.dto.TokenPairResponse;
import com.paola.paolarestapi.weather.dto.TokenValidationResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/*
  Why it is used:
  - This feature implements security foundations with access + refresh token flow.
  - This service provides minimal JWT support without introducing extra auth frameworks.

  How it is used:
  - WeatherResource /auth/login calls issueTokens(username, role).
  - WeatherResource /auth/refresh calls refresh(refreshToken).
  - WeatherResource /auth/validate calls validate(token) for client-side checks.

  How it works:
  - Builds JWT header/payload JSON and signs with HMAC-SHA256 (HS256).
  - Embeds core claims: issuer, subject(username), role, token_use, iat, exp.
  - Verifies signature + issuer + expiration on validation/refresh.
  - Uses JWT secret from env JWT_SECRET or JVM property jwt.secret (fallback for demo only).
*/
public class JwtTokenService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final String HMAC_ALG = "HmacSHA256";
    private static final String JWT_SECRET_ENV = "JWT_SECRET";
    private static final String JWT_SECRET_PROP = "jwt.secret";
    private static final String DEFAULT_SECRET = "day3-demo-secret-change-this";
    private static final String ISSUER = "paola-rest-api";
    private static final long ACCESS_TTL_SECONDS = 15 * 60;
    private static final long REFRESH_TTL_SECONDS = 7 * 24 * 60 * 60;

    public TokenPairResponse issueTokens(String username, String role) {
        long now = Instant.now().getEpochSecond();
        String accessToken = createToken(username, role, "access", now + ACCESS_TTL_SECONDS);
        String refreshToken = createToken(username, role, "refresh", now + REFRESH_TTL_SECONDS);
        return new TokenPairResponse(accessToken, refreshToken, "Bearer", ACCESS_TTL_SECONDS, role);
    }

    public TokenPairResponse refresh(String refreshToken) {
        Map<String, Object> claims = parseAndValidate(refreshToken, "refresh");
        String username = stringClaim(claims, "sub");
        String role = stringClaim(claims, "role");
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh token does not contain subject.");
        }
        if (role == null || role.trim().isEmpty()) {
            role = "read-only";
        }
        return issueTokens(username, role);
    }

    public TokenValidationResponse validate(String token) {
        try {
            Map<String, Object> claims = parseAndValidate(token, null);
            return new TokenValidationResponse(
                    true,
                    "Token is valid.",
                    stringClaim(claims, "sub"),
                    stringClaim(claims, "role"),
                    stringClaim(claims, "token_use"),
                    longClaim(claims, "exp")
            );
        } catch (Exception exception) {
            return new TokenValidationResponse(false, exception.getMessage());
        }
    }

    private String createToken(String username, String role, String tokenUse, long expEpochSeconds) {
        try {
            Map<String, Object> header = new LinkedHashMap<String, Object>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            long now = Instant.now().getEpochSecond();
            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("iss", ISSUER);
            payload.put("sub", username);
            payload.put("role", role);
            payload.put("token_use", tokenUse);
            payload.put("iat", now);
            payload.put("exp", expEpochSeconds);

            String encodedHeader = encodeJson(header);
            String encodedPayload = encodeJson(payload);
            String unsigned = encodedHeader + "." + encodedPayload;
            String signature = sign(unsigned, resolveSecret());
            return unsigned + "." + signature;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to create token.", exception);
        }
    }

    private Map<String, Object> parseAndValidate(String token, String expectedTokenUse) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid token format.");
            }

            String unsigned = parts[0] + "." + parts[1];
            String expectedSignature = sign(unsigned, resolveSecret());
            if (!MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))
            ) {
                throw new IllegalArgumentException("Token signature is invalid.");
            }

            String payloadJson = new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = OBJECT_MAPPER.readValue(payloadJson, new TypeReference<Map<String, Object>>() {
            });

            String issuer = stringClaim(claims, "iss");
            if (!ISSUER.equals(issuer)) {
                throw new IllegalArgumentException("Token issuer is invalid.");
            }

            long exp = longClaim(claims, "exp");
            long now = Instant.now().getEpochSecond();
            if (exp <= now) {
                throw new IllegalArgumentException("Token has expired.");
            }

            if (expectedTokenUse != null) {
                String tokenUse = stringClaim(claims, "token_use");
                if (!expectedTokenUse.equals(tokenUse)) {
                    throw new IllegalArgumentException("Wrong token type.");
                }
            }
            return claims;
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Token parsing failed.");
        }
    }

    private String sign(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALG);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALG));
        byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return URL_ENCODER.encodeToString(digest);
    }

    private String encodeJson(Object value) throws Exception {
        byte[] jsonBytes = OBJECT_MAPPER.writeValueAsBytes(value);
        return URL_ENCODER.encodeToString(jsonBytes);
    }

    private String resolveSecret() {
        String fromEnv = System.getenv(JWT_SECRET_ENV);
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv.trim();
        }
        String fromProp = System.getProperty(JWT_SECRET_PROP);
        if (fromProp != null && !fromProp.trim().isEmpty()) {
            return fromProp.trim();
        }
        return DEFAULT_SECRET;
    }

    private String stringClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private long longClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(String.valueOf(value));
    }
}
