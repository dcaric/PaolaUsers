package com.paola.paolarestapi.integration.service;

import com.paola.paolarestapi.integration.model.ReqResUserItem;
import com.paola.paolarestapi.integration.model.SnapshotUser;
import com.paola.paolarestapi.integration.model.UsersSnapshot;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/*
  Generates and locates the local XML snapshot file used in Day 2.

  Snapshot file path:
  - data/reqres-users-snapshot.xml

  Input:
  - List of users fetched from ReqRes.
  Output:
  - JAXB-marshalled XML document matching UsersSnapshot/SnapshotUser model.
*/
public class XmlSnapshotService {
    private static final Path SNAPSHOT_PATH = Paths.get("data", "reqres-users-snapshot.xml");

    public Path writeSnapshot(List<ReqResUserItem> users) {
        try {
            if (SNAPSHOT_PATH.getParent() != null) {
                Files.createDirectories(SNAPSHOT_PATH.getParent());
            }

            UsersSnapshot snapshot = new UsersSnapshot();
            List<SnapshotUser> snapshotUsers = new ArrayList<SnapshotUser>();
            for (ReqResUserItem user : users) {
                SnapshotUser snapshotUser = new SnapshotUser();
                snapshotUser.setId(user.getId());
                snapshotUser.setEmail(user.getEmail());
                snapshotUser.setFirstName(user.getFirstName());
                snapshotUser.setLastName(user.getLastName());
                snapshotUser.setAvatar(user.getAvatar());
                snapshotUsers.add(snapshotUser);
            }
            snapshot.setUsers(snapshotUsers);

            JAXBContext context = JAXBContext.newInstance(UsersSnapshot.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(snapshot, SNAPSHOT_PATH.toFile());
            return SNAPSHOT_PATH.toAbsolutePath();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to generate XML snapshot.", exception);
        }
    }

    public Path getSnapshotPath() {
        return SNAPSHOT_PATH.toAbsolutePath();
    }
}
