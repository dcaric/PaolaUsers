package com.paola.paolarestapi.integration.service;

import com.paola.paolarestapi.integration.dto.ReqResImportResponse;
import com.paola.paolarestapi.integration.model.ReqResUserItem;
import com.paola.paolarestapi.users.persistence.UserEntity;
import com.paola.paolarestapi.users.repository.UserRepository;

import java.nio.file.Path;
import java.util.List;

/*
  Orchestrates the complete Day 2 import pipeline.

  Flow:
  1) Fetch users from ReqRes.
  2) Map and save users into local H2 DB (UserEntity + UserRepository).
  3) Generate local XML snapshot file.
  4) Validate generated snapshot with XSD.
  5) Return execution summary via ReqResImportResponse.
*/
public class ReqResImportService {
    private final ReqResClientService reqResClientService = new ReqResClientService();
    private final UserRepository userRepository = new UserRepository();
    private final XmlSnapshotService xmlSnapshotService = new XmlSnapshotService();
    private final SnapshotValidationService snapshotValidationService = new SnapshotValidationService();

    public ReqResImportResponse importUsersAndCreateSnapshot() {
        List<ReqResUserItem> users = reqResClientService.fetchAllUsers();
        int saved = 0;
        for (ReqResUserItem user : users) {
            UserEntity entity = new UserEntity();
            entity.setEmail(user.getEmail());
            entity.setFirstName(user.getFirstName());
            entity.setLastName(user.getLastName());
            entity.setAvatar(user.getAvatar());
            userRepository.save(entity);
            saved++;
        }

        Path snapshot = xmlSnapshotService.writeSnapshot(users);
        List<String> validationMessages = snapshotValidationService.validate(snapshot);

        ReqResImportResponse response = new ReqResImportResponse();
        response.setFetchedFromReqRes(users.size());
        response.setSavedToDatabase(saved);
        response.setSnapshotPath(snapshot.toString());
        response.setSnapshotValidationMessages(validationMessages);
        return response;
    }
}
