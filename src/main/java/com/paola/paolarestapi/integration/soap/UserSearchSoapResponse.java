package com.paola.paolarestapi.integration.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import java.util.ArrayList;
import java.util.List;

/*
  SOAP response envelope for search operation.
  Contains high-level message, optional snapshot validation issues,
  and resulting user list.
*/
@XmlAccessorType(XmlAccessType.FIELD)
public class UserSearchSoapResponse {
    private String message;
    private List<String> validationMessages = new ArrayList<String>();
    private List<SoapUserResult> users = new ArrayList<SoapUserResult>();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getValidationMessages() {
        return validationMessages;
    }

    public void setValidationMessages(List<String> validationMessages) {
        this.validationMessages = validationMessages;
    }

    public List<SoapUserResult> getUsers() {
        return users;
    }

    public void setUsers(List<SoapUserResult> users) {
        this.users = users;
    }
}
