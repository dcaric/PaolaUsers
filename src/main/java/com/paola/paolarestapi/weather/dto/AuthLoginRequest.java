package com.paola.paolarestapi.weather.dto;

/*
  Input DTO for login/token issuing endpoint.
  Contains username and requested role for claim mapping.
*/
public class AuthLoginRequest {
    private String username;
    private String role;

    public AuthLoginRequest() {
    }

    public AuthLoginRequest(String username, String role) {
        this.username = username;
        this.role = role;
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
}
