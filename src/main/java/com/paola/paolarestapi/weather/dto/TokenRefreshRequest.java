package com.paola.paolarestapi.weather.dto;

/*
  Input DTO for refresh operation.
  Carries refresh token used to issue a new token pair.
*/
public class TokenRefreshRequest {
    private String refreshToken;

    public TokenRefreshRequest() {
    }

    public TokenRefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
