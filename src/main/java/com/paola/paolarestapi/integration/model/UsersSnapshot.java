package com.paola.paolarestapi.integration.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

/*
  JAXB root model for snapshot XML document:
  <users_snapshot>
      <user>...</user>
      ...
  </users_snapshot>
*/
@XmlRootElement(name = "users_snapshot")
@XmlAccessorType(XmlAccessType.FIELD)
public class UsersSnapshot {
    @XmlElement(name = "user")
    private List<SnapshotUser> users = new ArrayList<SnapshotUser>();

    public List<SnapshotUser> getUsers() {
        return users;
    }

    public void setUsers(List<SnapshotUser> users) {
        this.users = users;
    }
}
