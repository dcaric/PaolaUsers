package com.paola.paolarestapi.restapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
  Input DTO for custom REST API POST/PUT endpoints.
  Mirrors ReqRes user payload field naming.
*/
public class RestApiUserWriteRequest {
    private String email;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    private String avatar;

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
