package com.paola.paolarestapi.users.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

// API model for incoming request data on `/api/users`.
// It receives both JSON (Jackson) and XML (JAXB) payloads and acts as the first
// parsed Java object after schema validation. This object is not persisted directly:
// Through UserMapper.toEntity(...) in UserMapper.java.
// It copies fields from UserPayload to a new UserEntity:
// - payload.email -> entity.email
// - payload.firstName -> entity.firstName
// - payload.lastName -> entity.lastName
// - payload.avatar -> entity.avatar
// Then UserRepository.save(...) persists that UserEntity.
@XmlRootElement(name = "user")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserPayload {
    @JsonProperty("email")
    @XmlElement(name = "email", required = true)
    private String email;

    @JsonProperty("first_name")
    @XmlElement(name = "first_name", required = true)
    private String firstName;

    @JsonProperty("last_name")
    @XmlElement(name = "last_name", required = true)
    private String lastName;

    @JsonProperty("avatar")
    @XmlElement(name = "avatar", required = true)
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

