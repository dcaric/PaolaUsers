package com.paola.paolarestapi.weather.dto;

/*
  Output DTO for token validation endpoint.
  Returns validity status and decoded claims summary.
*/
public class TokenValidationResponse {
    private boolean valid;
    private String message;
    private String username;
    private String role;
    private String tokenUse;
    private long expiresAtEpochSeconds;

    public TokenValidationResponse() {
    }

    public TokenValidationResponse(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public TokenValidationResponse(
            boolean valid,
            String message,
            String username,
            String role,
            String tokenUse,
            long expiresAtEpochSeconds
    ) {
        this.valid = valid;
        this.message = message;
        this.username = username;
        this.role = role;
        this.tokenUse = tokenUse;
        this.expiresAtEpochSeconds = expiresAtEpochSeconds;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTokenUse() {
        return tokenUse;
    }

    public void setTokenUse(String tokenUse) {
        this.tokenUse = tokenUse;
    }

    public long getExpiresAtEpochSeconds() {
        return expiresAtEpochSeconds;
    }

    public void setExpiresAtEpochSeconds(long expiresAtEpochSeconds) {
        this.expiresAtEpochSeconds = expiresAtEpochSeconds;
    }
}
